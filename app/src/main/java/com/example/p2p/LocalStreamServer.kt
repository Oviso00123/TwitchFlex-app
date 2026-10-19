package com.example.p2p

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.util.Collections
import java.util.concurrent.atomic.AtomicReference

class LocalStreamServer(
    private val port: Int = 8080,
    private val onChatReceived: (sender: String, message: String) -> Unit,
    private val onSubReceived: (subscriber: String, tierName: String) -> Unit,
    private val onDonationReceived: ((donor: String, amount: Double, message: String) -> Unit)? = null
) {

    private var serverSocket: ServerSocket? = null
    private var serverJob: Job? = null
    private val currentJpegFrame = AtomicReference<ByteArray?>(null)

    var streamerInfoProvider: (() -> StreamInfoData)? = null
    var streamDurationSecondsProvider: (() -> Long)? = null

    data class StreamInfoData(
        val channelName: String,
        val category: String,
        val viewers: Int,
        val totalSubs: Int,
        val durationFormatted: String,
        val isLive: Boolean
    )

    fun updateFrame(jpegBytes: ByteArray) {
        currentJpegFrame.set(jpegBytes)
    }

    fun broadcastJpegFrame(jpegBytes: ByteArray) {
        updateFrame(jpegBytes)
    }

    fun startServer(scope: CoroutineScope) {
        if (serverSocket != null && !serverSocket!!.isClosed) return

        serverJob = scope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(port)
                while (isActive && serverSocket != null && !serverSocket!!.isClosed) {
                    val clientSocket = serverSocket!!.accept()
                    scope.launch(Dispatchers.IO) {
                        handleClient(clientSocket)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    private fun handleClient(socket: Socket) {
        try {
            socket.soTimeout = 15000
            val input = BufferedReader(InputStreamReader(socket.getInputStream()))
            val output = socket.getOutputStream()

            val requestLine = input.readLine() ?: return socket.close()
            val parts = requestLine.split(" ")
            if (parts.size < 2) return socket.close()

            val method = parts[0]
            val path = parts[1]

            // Read headers
            var contentLength = 0
            var line: String?
            while (input.readLine().also { line = it } != null) {
                if (line.isNullOrBlank()) break
                if (line!!.startsWith("Content-Length:", ignoreCase = true)) {
                    contentLength = line!!.substringAfter(":").trim().toIntOrNull() ?: 0
                }
            }

            // Read body if POST
            var body = ""
            if (contentLength > 0) {
                val bodyChars = CharArray(contentLength)
                var readTotal = 0
                while (readTotal < contentLength) {
                    val read = input.read(bodyChars, readTotal, contentLength - readTotal)
                    if (read == -1) break
                    readTotal += read
                }
                body = String(bodyChars, 0, readTotal)
            }

            when {
                path == "/" || path == "/index.html" -> serveWebPlayer(output)
                path == "/stream/video" -> serveMjpegStream(socket, output)
                path == "/api/frame" -> serveSingleFrame(output)
                path == "/api/stream_info" -> serveStreamInfo(output)
                method == "POST" && path == "/api/chat" -> handleChatPost(output, body)
                method == "POST" && path == "/api/sub" -> handleSubPost(output, body)
                method == "POST" && path == "/api/donate" -> handleDonatePost(output, body)
                else -> {
                    val notFound = "HTTP/1.1 404 Not Found\r\nContent-Length: 0\r\n\r\n"
                    output.write(notFound.toByteArray())
                    output.flush()
                    socket.close()
                }
            }
        } catch (_: Exception) {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun handleDonatePost(output: OutputStream, body: String) {
        val duration = streamDurationSecondsProvider?.invoke() ?: 0L
        if (duration < 240L) {
            val err = """{"status":"error","message":"Le donazioni possono iniziare solo dopo 4 minuti di live!"}"""
            val headers = "HTTP/1.1 400 Bad Request\r\nContent-Type: application/json; charset=utf-8\r\nAccess-Control-Allow-Origin: *\r\nContent-Length: ${err.toByteArray().size}\r\n\r\n"
            output.write((headers + err).toByteArray())
            output.flush()
            return
        }

        try {
            val json = JSONObject(body)
            val donor = json.optString("username", "Spettatore P2P")
            val amount = json.optDouble("amount", 5.0)
            val message = json.optString("message", "Grande live!")
            onDonationReceived?.invoke(donor, amount, message)
        } catch (_: Exception) {}

        val resp = """{"status":"ok","message":"Donazione registrata con successo!"}"""
        val headers = "HTTP/1.1 200 OK\r\nContent-Type: application/json; charset=utf-8\r\nAccess-Control-Allow-Origin: *\r\nContent-Length: ${resp.toByteArray().size}\r\n\r\n"
        output.write((headers + resp).toByteArray())
        output.flush()
    }

    private fun serveStreamInfo(output: OutputStream) {
        val info = streamerInfoProvider?.invoke() ?: StreamInfoData(
            channelName = "Twitch Finto Streamer",
            category = "IRL",
            viewers = 15,
            totalSubs = 0,
            durationFormatted = "00:00:00",
            isLive = true
        )
        val json = JSONObject().apply {
            put("channelName", info.channelName)
            put("category", info.category)
            put("viewers", info.viewers)
            put("totalSubs", info.totalSubs)
            put("duration", info.durationFormatted)
            put("isLive", info.isLive)
        }.toString()

        val headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: application/json; charset=utf-8\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Content-Length: ${json.toByteArray().size}\r\n\r\n"
        output.write((headers + json).toByteArray())
        output.flush()
    }

    private fun handleChatPost(output: OutputStream, body: String) {
        try {
            val json = JSONObject(body)
            val sender = json.optString("username", "Amico in Rete")
            val message = json.optString("message", "")
            if (message.isNotBlank()) {
                onChatReceived(sender, message)
            }
        } catch (_: Exception) {}

        val resp = """{"status":"ok"}"""
        val headers = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nAccess-Control-Allow-Origin: *\r\nContent-Length: ${resp.length}\r\n\r\n"
        output.write((headers + resp).toByteArray())
        output.flush()
    }

    private fun handleSubPost(output: OutputStream, body: String) {
        try {
            val json = JSONObject(body)
            val subscriber = json.optString("username", "Spettatore P2P")
            val tier = json.optString("tier", "Livello 1")
            onSubReceived(subscriber, tier)
        } catch (_: Exception) {}

        val resp = """{"status":"ok","message":"Sub effettuata con successo!"}"""
        val headers = "HTTP/1.1 200 OK\r\nContent-Type: application/json\r\nAccess-Control-Allow-Origin: *\r\nContent-Length: ${resp.length}\r\n\r\n"
        output.write((headers + resp).toByteArray())
        output.flush()
    }

    private fun serveSingleFrame(output: OutputStream) {
        val frame = currentJpegFrame.get() ?: generatePlaceholderFrame()
        val headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: image/jpeg\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Content-Length: ${frame.size}\r\n\r\n"
        output.write(headers.toByteArray())
        output.write(frame)
        output.flush()
    }

    private fun serveMjpegStream(socket: Socket, output: OutputStream) {
        try {
            val boundary = "frame_boundary_twitch"
            val headers = "HTTP/1.1 200 OK\r\n" +
                    "Content-Type: multipart/x-mixed-replace; boundary=$boundary\r\n" +
                    "Cache-Control: no-cache\r\n" +
                    "Connection: close\r\n\r\n"
            output.write(headers.toByteArray())
            output.flush()

            while (!socket.isClosed && serverSocket != null && !serverSocket!!.isClosed) {
                val frame = currentJpegFrame.get() ?: generatePlaceholderFrame()
                val partHeader = "--$boundary\r\n" +
                        "Content-Type: image/jpeg\r\n" +
                        "Content-Length: ${frame.size}\r\n\r\n"
                output.write(partHeader.toByteArray())
                output.write(frame)
                output.write("\r\n".toByteArray())
                output.flush()
                Thread.sleep(100) // ~10 FPS for network bandwidth efficiency
            }
        } catch (_: Exception) {
            try { socket.close() } catch (_: Exception) {}
        }
    }

    private fun generatePlaceholderFrame(): ByteArray {
        val bitmap = Bitmap.createBitmap(480, 360, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#0E0E10"))

        val paint = Paint().apply {
            color = Color.parseColor("#9146FF")
            textSize = 28f
            isAntiAlias = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("ZYRO LIVE", 240f, 160f, paint)

        paint.color = Color.WHITE
        paint.textSize = 18f
        canvas.drawText("📡 Streaming P2P in Rete Locale", 240f, 210f, paint)

        val out = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 75, out)
        return out.toByteArray()
    }

    private fun serveWebPlayer(output: OutputStream) {
        val html = """
<!DOCTYPE html>
<html lang="it">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Zyro - Live Locale</title>
  <style>
    * { box-sizing: border-box; margin: 0; padding: 0; }
    body { background: #0E0E10; color: #EFEFF1; font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif; display: flex; flex-direction: column; min-height: 100vh; }
    header { background: #18181B; padding: 14px 20px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid #2E2E35; }
    .logo { color: #9146FF; font-weight: 800; font-size: 1.3rem; display: flex; align-items: center; gap: 8px; }
    .badge-live { background: #EB0400; color: white; padding: 4px 10px; border-radius: 4px; font-weight: bold; font-size: 0.8rem; letter-spacing: 1px; }
    .main-container { display: flex; flex: 1; flex-direction: column; max-width: 1200px; margin: 0 auto; width: 100%; padding: 16px; gap: 16px; }
    @media (min-width: 800px) { .main-container { flex-direction: row; } }
    .stream-box { flex: 2; background: #000; border-radius: 12px; overflow: hidden; display: flex; flex-direction: column; position: relative; }
    .video-frame { width: 100%; min-height: 280px; max-height: 520px; object-fit: contain; background: #000; }
    .stream-hud { background: #18181B; padding: 16px; border-radius: 0 0 12px 12px; border: 1px solid #2E2E35; border-top: none; }
    .stream-title { font-size: 1.2rem; font-weight: bold; margin-bottom: 6px; }
    .stream-meta { display: flex; gap: 16px; font-size: 0.9rem; color: #ADADB8; }
    .chat-box { flex: 1; background: #18181B; border-radius: 12px; border: 1px solid #2E2E35; display: flex; flex-direction: column; height: 500px; }
    .chat-header { padding: 12px 16px; border-bottom: 1px solid #2E2E35; font-weight: bold; }
    .chat-messages { flex: 1; padding: 12px; overflow-y: auto; display: flex; flex-direction: column; gap: 8px; font-size: 0.9rem; }
    .chat-message { line-height: 1.4; word-break: break-word; }
    .user { font-weight: bold; color: #9146FF; }
    .chat-inputs { padding: 12px; border-top: 1px solid #2E2E35; display: flex; flex-direction: column; gap: 8px; }
    .input-row { display: flex; gap: 8px; }
    input { background: #0E0E10; border: 1px solid #2E2E35; color: white; padding: 10px; border-radius: 6px; font-size: 0.9rem; }
    input:focus { outline: none; border-color: #9146FF; }
    .btn { background: #9146FF; color: white; border: none; padding: 10px 16px; border-radius: 6px; cursor: pointer; font-weight: bold; transition: 0.2s; }
    .btn:hover { background: #772CE8; }
    .btn-sub { background: #FFB100; color: #000; font-weight: 800; }
    .btn-sub:hover { background: #E59F00; }
  </style>
</head>
<body>
  <header>
    <div class="logo"><span>👑</span> Twitch Finto</div>
    <div style="display:flex; align-items:center; gap:12px;">
      <span class="badge-live">LIVE</span>
      <span id="viewers-count">● ... spettatori</span>
    </div>
  </header>

  <div class="main-container">
    <div class="stream-box">
      <img src="/stream/video" class="video-frame" alt="Stream Camera" onerror="this.src='/api/frame'" />
      <div class="stream-hud">
        <div class="stream-title" id="channel-name">Canale Streamer</div>
        <div class="stream-meta">
          <span>Categoria: <strong id="category" style="color:#00F5D4;">Caricamento...</strong></span>
          <span>Tempo: <strong id="uptime">00:00:00</strong></span>
        </div>
        <div style="margin-top:14px; display:flex; flex-direction:column; gap:8px;">
          <button class="btn btn-sub" onclick="sendSub()">⭐ Fai una Sub al Canale (€3.99)</button>
          <div style="background:#0E0E10; border:1px solid #2E2E35; border-radius:8px; padding:10px; display:flex; flex-direction:column; gap:6px;">
            <span style="font-size:0.85rem; color:#00F5D4; font-weight:bold;">💎 Invia una Donazione Personalizzata</span>
            <div style="display:flex; gap:6px;">
              <input type="number" id="don-amount" placeholder="€" min="1" max="50" value="5" style="width:90px;" />
              <input type="text" id="don-msg" placeholder="Messaggio per lo streamer..." style="flex:1;" />
              <button class="btn" style="background:#00F5D4; color:#000; font-weight:bold;" onclick="sendDonation()">Dona</button>
            </div>
            <span id="don-hint" style="font-size:0.75rem; color:#ADADB8;">(Donazioni attive dopo 4 minuti di live • Max €25, rara €50)</span>
          </div>
        </div>
      </div>
    </div>

    <div class="chat-box">
      <div class="chat-header">💬 Chat della Diretta P2P</div>
      <div class="chat-messages" id="chat-messages">
        <div class="chat-message"><span class="user">Sistema:</span> Benvenuto nella live locale! Puoi guardare e chattare in tempo reale.</div>
      </div>
      <div class="chat-inputs">
        <input type="text" id="username" placeholder="Il tuo nome (es. Marco)" value="Spettatore_Wi-Fi" />
        <div class="input-row">
          <input type="text" id="msg-input" placeholder="Scrivi in chat..." style="flex:1;" onkeydown="if(event.key==='Enter') sendChat()" />
          <button class="btn" onclick="sendChat()">Invia</button>
        </div>
      </div>
    </div>
  </div>

  <script>
    async function updateInfo() {
      try {
        const res = await fetch('/api/stream_info');
        const data = await res.json();
        document.getElementById('channel-name').innerText = data.channelName;
        document.getElementById('category').innerText = data.category;
        document.getElementById('viewers-count').innerText = '● ' + data.viewers + ' spettatori';
        document.getElementById('uptime').innerText = data.duration;
      } catch(e) {}
    }
    setInterval(updateInfo, 3000);
    updateInfo();

    async function sendChat() {
      const u = document.getElementById('username').value.trim() || 'Spettatore';
      const m = document.getElementById('msg-input').value.trim();
      if (!m) return;
      document.getElementById('msg-input').value = '';

      const container = document.getElementById('chat-messages');
      const div = document.createElement('div');
      div.className = 'chat-message';
      div.innerHTML = '<span class="user" style="color:#00F5D4;">' + u + ':</span> ' + m;
      container.appendChild(div);
      container.scrollTop = container.scrollHeight;

      try {
        await fetch('/api/chat', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({username: u, message: m})
        });
      } catch(e) {}
    }

    async function sendSub() {
      const u = document.getElementById('username').value.trim() || 'Spettatore';
      try {
        await fetch('/api/sub', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({username: u, tier: 'Livello 1'})
        });
        alert('🎉 Hai effettuato una Sub al canale di ' + document.getElementById('channel-name').innerText + '!');
      } catch(e) {
        alert('Errore invio sub');
      }
    }

    async function sendDonation() {
      const u = document.getElementById('username').value.trim() || 'Spettatore';
      const a = parseFloat(document.getElementById('don-amount').value) || 5.0;
      const m = document.getElementById('don-msg').value.trim() || 'Grande live!';
      try {
        const res = await fetch('/api/donate', {
          method: 'POST',
          headers: {'Content-Type': 'application/json'},
          body: JSON.stringify({username: u, amount: a, message: m})
        });
        const d = await res.json();
        alert(d.message || 'Donazione inviata!');
      } catch(e) {
        alert('Errore invio donazione. Ricorda che le donazioni si sbloccano dopo 4 minuti di live.');
      }
    }
  </script>
</body>
</html>
        """.trimIndent()

        val headers = "HTTP/1.1 200 OK\r\n" +
                "Content-Type: text/html; charset=utf-8\r\n" +
                "Access-Control-Allow-Origin: *\r\n" +
                "Content-Length: ${html.toByteArray().size}\r\n\r\n"
        output.write((headers + html).toByteArray())
        output.flush()
    }

    fun stopServer() {
        serverJob?.cancel()
        try {
            serverSocket?.close()
            serverSocket = null
        } catch (_: Exception) {}
    }

    companion object {
        fun getLocalIpAddress(): String {
            try {
                val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
                for (intf in interfaces) {
                    val addrs = Collections.list(intf.inetAddresses)
                    for (addr in addrs) {
                        if (!addr.isLoopbackAddress && addr is Inet4Address) {
                            val host = addr.hostAddress ?: ""
                            if (host.isNotBlank() && !host.startsWith("127.")) {
                                return host
                            }
                        }
                    }
                }
            } catch (_: Exception) {}
            return "127.0.0.1"
        }
    }
}
