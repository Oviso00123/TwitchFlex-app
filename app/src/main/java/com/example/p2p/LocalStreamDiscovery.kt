package com.example.p2p

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

data class DiscoveredStream(
    val ip: String,
    val port: Int,
    val channelName: String,
    val category: String,
    val viewers: Int,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

class LocalStreamDiscovery {

    private val _discoveredStreams = MutableStateFlow<List<DiscoveredStream>>(emptyList())
    val discoveredStreams: StateFlow<List<DiscoveredStream>> = _discoveredStreams.asStateFlow()

    private var broadcastJob: Job? = null
    private var listenJob: Job? = null
    private val broadcastPort = 8081

    fun startAnnouncing(
        scope: CoroutineScope,
        infoProvider: () -> LocalStreamServer.StreamInfoData
    ) {
        broadcastJob?.cancel()
        broadcastJob = scope.launch(Dispatchers.IO) {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket()
                socket.broadcast = true
                val broadcastAddress = InetAddress.getByName("255.255.255.255")

                while (isActive) {
                    val info = infoProvider()
                    val myIp = LocalStreamServer.getLocalIpAddress()
                    val payload = JSONObject().apply {
                        put("ip", myIp)
                        put("port", 8080)
                        put("channelName", info.channelName)
                        put("category", info.category)
                        put("viewers", info.viewers)
                    }.toString().toByteArray()

                    val packet = DatagramPacket(payload, payload.size, broadcastAddress, broadcastPort)
                    socket.send(packet)
                    delay(3000)
                }
            } catch (_: Exception) {
            } finally {
                socket?.close()
            }
        }
    }

    fun stopAnnouncing() {
        broadcastJob?.cancel()
        broadcastJob = null
    }

    fun startListening(scope: CoroutineScope) {
        listenJob?.cancel()
        listenJob = scope.launch(Dispatchers.IO) {
            var socket: DatagramSocket? = null
            try {
                socket = DatagramSocket(broadcastPort)
                socket.soTimeout = 4000
                val buffer = ByteArray(2048)

                while (isActive) {
                    try {
                        val packet = DatagramPacket(buffer, buffer.size)
                        socket.receive(packet)
                        val text = String(packet.data, 0, packet.length)
                        val json = JSONObject(text)
                        val ip = json.optString("ip", packet.address.hostAddress ?: "")
                        val port = json.optInt("port", 8080)
                        val name = json.optString("channelName", "Streamer Sconosciuto")
                        val cat = json.optString("category", "Live")
                        val viewers = json.optInt("viewers", 1)

                        val stream = DiscoveredStream(
                            ip = ip,
                            port = port,
                            channelName = name,
                            category = cat,
                            viewers = viewers,
                            lastSeenTimestamp = System.currentTimeMillis()
                        )

                        // Update list & filter out streams older than 10 seconds
                        val now = System.currentTimeMillis()
                        val currentList = _discoveredStreams.value
                            .filter { it.ip != ip && (now - it.lastSeenTimestamp) < 10000 }
                            .toMutableList()
                        currentList.add(stream)
                        _discoveredStreams.value = currentList
                    } catch (_: Exception) {}

                    // Cleanup stale streams
                    val now = System.currentTimeMillis()
                    _discoveredStreams.value = _discoveredStreams.value.filter {
                        (now - it.lastSeenTimestamp) < 10000
                    }
                }
            } catch (_: Exception) {
            } finally {
                socket?.close()
            }
        }
    }

    fun addManualStream(ip: String, port: Int = 8080) {
        val stream = DiscoveredStream(
            ip = ip,
            port = port,
            channelName = "Stream su $ip",
            category = "P2P",
            viewers = 1,
            lastSeenTimestamp = System.currentTimeMillis()
        )
        _discoveredStreams.value = listOf(stream) + _discoveredStreams.value.filter { it.ip != ip }
    }

    fun stopListening() {
        listenJob?.cancel()
        listenJob = null
    }
}
