package com.example.p2p

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket

class P2PClientManager {

    private var socket: Socket? = null
    private val clientScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isAuthorized = MutableStateFlow(true)
    val isAuthorized: StateFlow<Boolean> = _isAuthorized.asStateFlow()

    private val _pingMs = MutableStateFlow(12L)
    val pingMs: StateFlow<Long> = _pingMs.asStateFlow()

    private val _kbps = MutableStateFlow(0)
    val kbps: StateFlow<Int> = _kbps.asStateFlow()

    private var lastPingSentTime = 0L

    fun connectToStream(
        hostIp: String,
        port: Int = 8080,
        pin: String = "",
        onFrameReceived: (Bitmap) -> Unit,
        onJsonEventReceived: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        disconnect()

        clientScope.launch {
            try {
                val sock = Socket()
                sock.tcpNoDelay = true
                sock.connect(InetSocketAddress(hostIp, port), 4000)
                socket = sock
                _isConnected.value = true

                val dis = DataInputStream(BufferedInputStream(sock.getInputStream()))
                val dos = DataOutputStream(BufferedOutputStream(sock.getOutputStream()))

                if (pin.isNotBlank()) {
                    sendAuthPin(pin)
                }

                // Start ping / bandwidth loop
                launch {
                    var bytesLastSecond = 0
                    while (isActive && _isConnected.value) {
                        lastPingSentTime = System.currentTimeMillis()
                        sendRawJson("""{"type":"PING"}""")
                        delay(1000)
                    }
                }

                var bytesCounter = 0
                var lastTime = System.currentTimeMillis()

                while (isActive && !sock.isClosed && sock.isConnected) {
                    P2PStreamProtocol.readPacket(dis) { type, data ->
                        bytesCounter += data.size + 5
                        val now = System.currentTimeMillis()
                        if (now - lastTime >= 1000) {
                            _kbps.value = (bytesCounter * 8) / 1024
                            bytesCounter = 0
                            lastTime = now
                        }

                        when (type) {
                            PacketType.VIDEO_FRAME -> {
                                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
                                bitmap?.let { onFrameReceived(it) }
                            }
                            PacketType.JSON_EVENT -> {
                                val str = String(data, Charsets.UTF_8)
                                try {
                                    val json = JSONObject(str)
                                    when (json.optString("type")) {
                                        "PONG" -> {
                                            _pingMs.value = System.currentTimeMillis() - lastPingSentTime
                                        }
                                        "AUTH_STATUS" -> {
                                            _isAuthorized.value = json.optBoolean("authorized", true)
                                        }
                                        else -> onJsonEventReceived(json)
                                    }
                                } catch (_: Exception) {}
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                _isConnected.value = false
                onError("Impossibile connettersi: ${e.message}")
            }
        }
    }

    fun sendAuthPin(pin: String) {
        val payload = JSONObject().apply {
            put("type", "AUTH_PIN")
            put("pin", pin)
        }.toString()
        sendRawJson(payload)
    }

    fun sendChatMessage(user: String, message: String) {
        val payload = JSONObject().apply {
            put("type", "CHAT")
            put("user", user)
            put("message", message)
        }.toString()
        sendRawJson(payload)
    }

    fun sendSubEvent(user: String, amount: Double = 2.50) {
        val payload = JSONObject().apply {
            put("type", "SUB")
            put("user", user)
            put("amount", amount)
        }.toString()
        sendRawJson(payload)
    }

    private fun sendRawJson(jsonStr: String) {
        clientScope.launch {
            try {
                socket?.let { sock ->
                    if (!sock.isClosed) {
                        val dos = DataOutputStream(sock.getOutputStream())
                        val bytes = jsonStr.toByteArray(Charsets.UTF_8)
                        P2PStreamProtocol.sendFrame(dos, PacketType.JSON_EVENT, bytes)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun disconnect() {
        _isConnected.value = false
        try {
            socket?.close()
            socket = null
        } catch (_: Exception) {}
    }
}
