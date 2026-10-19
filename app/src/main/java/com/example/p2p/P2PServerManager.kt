package com.example.p2p

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.ServerSocket
import java.net.Socket
import java.util.concurrent.CopyOnWriteArrayList

class P2PServerManager(
    private val onChatReceived: (sender: String, message: String) -> Unit,
    private val onSubReceived: (subscriber: String, amount: Double) -> Unit,
    private val onClientCountChanged: (Int) -> Unit
) {

    private var serverSocket: ServerSocket? = null
    private val clientSockets = CopyOnWriteArrayList<Socket>()
    private val authenticatedClients = CopyOnWriteArrayList<Socket>()
    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var isStreaming = false

    var currentPin: String = ""
    var streamInfoProvider: (() -> LocalStreamServer.StreamInfoData)? = null

    fun startServer(port: Int = 8080, pin: String = "") {
        if (isStreaming) stopServer()
        currentPin = pin
        isStreaming = true

        serverScope.launch {
            try {
                serverSocket = ServerSocket(port)
                while (isStreaming && serverSocket != null && !serverSocket!!.isClosed) {
                    val client = serverSocket?.accept() ?: break
                    client.tcpNoDelay = true
                    clientSockets.add(client)
                    onClientCountChanged(clientSockets.size)

                    serverScope.launch {
                        handleClient(client)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            socket.soTimeout = 0 // Keep persistent for streaming
            val rawIn = socket.getInputStream()
            val rawOut = socket.getOutputStream()
            val pushbackIn = java.io.PushbackInputStream(rawIn, 16)

            val firstBytes = ByteArray(4)
            val readCount = pushbackIn.read(firstBytes)
            if (readCount <= 0) {
                closeClient(socket)
                return
            }
            pushbackIn.unread(firstBytes, 0, readCount)

            // Check if it's an HTTP request (GET / HTTP/1.1) from a browser
            val headerStart = String(firstBytes, 0, minOf(4, readCount))
            if (headerStart.startsWith("GET") || headerStart.startsWith("POST")) {
                handleHttpFallback(pushbackIn, rawOut, socket)
                return
            }

            // Otherwise, it's our binary P2PStreamProtocol
            val dis = DataInputStream(BufferedInputStream(pushbackIn))
            val dos = DataOutputStream(BufferedOutputStream(rawOut))

            // If PIN is required, mark unauthenticated initially
            if (currentPin.isBlank()) {
                authenticatedClients.add(socket)
                sendAuthStatus(dos, true, "Benvenuto nella live locale!")
            } else {
                sendAuthStatus(dos, false, "PIN_REQUIRED")
            }

            while (isStreaming && !socket.isClosed && socket.isConnected) {
                P2PStreamProtocol.readPacket(dis) { type, data ->
                    if (type == PacketType.JSON_EVENT) {
                        val jsonStr = String(data, Charsets.UTF_8)
                        handleIncomingJson(socket, dos, jsonStr)
                    }
                }
            }
        } catch (_: Exception) {
        } finally {
            closeClient(socket)
        }
    }

    private fun handleIncomingJson(socket: Socket, dos: DataOutputStream, jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            when (json.optString("type")) {
                "AUTH_PIN" -> {
                    val enteredPin = json.optString("pin")
                    if (currentPin.isBlank() || enteredPin == currentPin) {
                        authenticatedClients.add(socket)
                        sendAuthStatus(dos, true, "PIN Corretto! Accesso garantito.")
                    } else {
                        sendAuthStatus(dos, false, "PIN Errato. Riprova.")
                    }
                }
                "CHAT" -> {
                    val sender = json.optString("user", "Amico P2P")
                    val message = json.optString("message", "")
                    if (message.isNotBlank()) {
                        onChatReceived(sender, message)
                        broadcastJsonEvent(jsonStr)
                    }
                }
                "SUB" -> {
                    val user = json.optString("user", "Spettatore P2P")
                    val amount = json.optDouble("amount", 2.50)
                    onSubReceived(user, amount)
                    broadcastJsonEvent(
                        JSONObject().apply {
                            put("type", "SUB_ALERT")
                            put("user", user)
                            put("amount", amount)
                        }.toString()
                    )
                }
                "PING" -> {
                    val pong = JSONObject().apply {
                        put("type", "PONG")
                        put("time", System.currentTimeMillis())
                    }.toString()
                    P2PStreamProtocol.sendFrame(dos, PacketType.JSON_EVENT, pong.toByteArray(Charsets.UTF_8))
                }
            }
        } catch (_: Exception) {}
    }

    private fun sendAuthStatus(dos: DataOutputStream, authorized: Boolean, message: String) {
        val payload = JSONObject().apply {
            put("type", "AUTH_STATUS")
            put("authorized", authorized)
            put("message", message)
        }.toString().toByteArray(Charsets.UTF_8)
        P2PStreamProtocol.sendFrame(dos, PacketType.JSON_EVENT, payload)
    }

    fun broadcastFrame(jpegBytes: ByteArray) {
        if (clientSockets.isEmpty()) return

        serverScope.launch {
            val iterator = clientSockets.iterator()
            while (iterator.hasNext()) {
                val socket = iterator.next()
                if (currentPin.isNotBlank() && !authenticatedClients.contains(socket)) {
                    continue
                }
                try {
                    val dos = DataOutputStream(socket.getOutputStream())
                    P2PStreamProtocol.sendFrame(dos, PacketType.VIDEO_FRAME, jpegBytes)
                } catch (_: Exception) {
                    closeClient(socket)
                }
            }
        }
    }

    fun broadcastJsonEvent(jsonString: String) {
        if (clientSockets.isEmpty()) return
        serverScope.launch {
            val bytes = jsonString.toByteArray(Charsets.UTF_8)
            val iterator = clientSockets.iterator()
            while (iterator.hasNext()) {
                val socket = iterator.next()
                try {
                    val dos = DataOutputStream(socket.getOutputStream())
                    P2PStreamProtocol.sendFrame(dos, PacketType.JSON_EVENT, bytes)
                } catch (_: Exception) {
                    closeClient(socket)
                }
            }
        }
    }

    private fun handleHttpFallback(input: InputStream, output: OutputStream, socket: Socket) {
        try {
            val reader = java.io.BufferedReader(java.io.InputStreamReader(input))
            val line = reader.readLine() ?: return socket.close()
            val path = line.split(" ").getOrNull(1) ?: "/"

            if (path.startsWith("/api/frame")) {
                val frame = generatePlaceholderJpeg()
                val headers = "HTTP/1.1 200 OK\r\nContent-Type: image/jpeg\r\nAccess-Control-Allow-Origin: *\r\nContent-Length: ${frame.size}\r\n\r\n"
                output.write(headers.toByteArray())
                output.write(frame)
                output.flush()
            } else {
                val info = streamInfoProvider?.invoke() ?: LocalStreamServer.StreamInfoData(
                    "Zyro Streamer", "IRL", 10, 0, "00:05:00", true
                )
                val html = """
                    <!DOCTYPE html><html><head><title>Zyro P2P</title>
                    <meta name="viewport" content="width=device-width,initial-scale=1">
                    <style>body{background:#0E0E10;color:#fff;font-family:sans-serif;text-align:center;padding:20px;}</style>
                    </head><body>
                    <h2 style="color:#9146FF">👑 Zyro Live Locale</h2>
                    <p>Streamer: <strong>${info.channelName}</strong> (${info.category})</p>
                    <img src="/api/frame" style="max-width:100%;border-radius:12px;border:2px solid #9146FF;margin:10px 0;" />
                    <p style="color:#00F5D4">📡 Connesso in P2P locale.</p>
                    </body></html>
                """.trimIndent()
                val headers = "HTTP/1.1 200 OK\r\nContent-Type: text/html\r\nContent-Length: ${html.toByteArray().size}\r\n\r\n"
                output.write((headers + html).toByteArray())
                output.flush()
            }
        } catch (_: Exception) {} finally {
            socket.close()
        }
    }

    private fun generatePlaceholderJpeg(): ByteArray {
        val bitmap = Bitmap.createBitmap(360, 240, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#0E0E10"))
        val paint = Paint().apply {
            color = Color.parseColor("#9146FF")
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ZYRO LIVE P2P", 180f, 120f, paint)
        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, out)
        return out.toByteArray()
    }

    private fun closeClient(socket: Socket) {
        try {
            socket.close()
        } catch (_: Exception) {}
        clientSockets.remove(socket)
        authenticatedClients.remove(socket)
        onClientCountChanged(clientSockets.size)
    }

    fun stopServer() {
        isStreaming = false
        clientSockets.forEach { try { it.close() } catch (_: Exception) {} }
        clientSockets.clear()
        authenticatedClients.clear()
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (_: Exception) {}
        onClientCountChanged(0)
    }
}
