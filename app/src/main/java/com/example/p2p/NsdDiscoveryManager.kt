package com.example.p2p

import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NsdStreamItem(
    val channelName: String,
    val hostIp: String,
    val port: Int,
    val isPrivate: Boolean = false,
    val lastSeenTimestamp: Long = System.currentTimeMillis()
)

class NsdDiscoveryManager(private val context: Context) {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private val SERVICE_TYPE = "_twitchflex._tcp."

    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null

    private val _discoveredStreams = MutableStateFlow<List<NsdStreamItem>>(emptyList())
    val discoveredStreams: StateFlow<List<NsdStreamItem>> = _discoveredStreams.asStateFlow()

    private var isDiscovering = false
    private var isRegistered = false

    fun registerStreamService(channelName: String, port: Int, isPrivate: Boolean = false) {
        unregisterStreamService()

        val serviceInfo = NsdServiceInfo().apply {
            val privateTag = if (isPrivate) "-LOCKED" else ""
            serviceName = "TwitchFlex-$channelName$privateTag"
            serviceType = SERVICE_TYPE
            setPort(port)
        }

        registrationListener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(service: NsdServiceInfo) {
                isRegistered = true
            }
            override fun onRegistrationFailed(service: NsdServiceInfo, errorCode: Int) {
                isRegistered = false
            }
            override fun onServiceUnregistered(service: NsdServiceInfo) {
                isRegistered = false
            }
            override fun onUnregistrationFailed(service: NsdServiceInfo, errorCode: Int) {
                isRegistered = false
            }
        }

        try {
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener)
        } catch (_: Exception) {}
    }

    fun unregisterStreamService() {
        if (isRegistered && registrationListener != null) {
            try {
                nsdManager.unregisterService(registrationListener)
            } catch (_: Exception) {}
            isRegistered = false
            registrationListener = null
        }
    }

    fun startDiscovery() {
        if (isDiscovering) return

        discoveryListener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(regType: String) {
                isDiscovering = true
            }

            override fun onServiceFound(service: NsdServiceInfo) {
                if (service.serviceType.contains("twitchflex", ignoreCase = true)) {
                    resolveServiceSafely(service)
                }
            }

            override fun onServiceLost(service: NsdServiceInfo) {
                val name = cleanServiceName(service.serviceName)
                _discoveredStreams.value = _discoveredStreams.value.filter { it.channelName != name }
            }

            override fun onDiscoveryStopped(serviceType: String) {
                isDiscovering = false
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                isDiscovering = false
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
                isDiscovering = false
            }
        }

        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, discoveryListener)
        } catch (_: Exception) {}
    }

    private fun resolveServiceSafely(service: NsdServiceInfo) {
        try {
            nsdManager.resolveService(service, object : NsdManager.ResolveListener {
                override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {}
                override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                    val hostIp = serviceInfo.host?.hostAddress ?: ""
                    val isPrivate = serviceInfo.serviceName.contains("-LOCKED")
                    val rawName = cleanServiceName(serviceInfo.serviceName)

                    if (hostIp.isNotBlank()) {
                        val item = NsdStreamItem(
                            channelName = rawName,
                            hostIp = hostIp,
                            port = serviceInfo.port,
                            isPrivate = isPrivate
                        )
                        val updated = _discoveredStreams.value.filter { it.hostIp != hostIp }.toMutableList()
                        updated.add(item)
                        _discoveredStreams.value = updated
                    }
                }
            })
        } catch (_: Exception) {}
    }

    private fun cleanServiceName(name: String): String {
        return name.removePrefix("TwitchFlex-").removeSuffix("-LOCKED")
    }

    fun addManualStream(ip: String, port: Int = 8080) {
        val item = NsdStreamItem(
            channelName = "Diretta su $ip",
            hostIp = ip,
            port = port,
            isPrivate = false
        )
        _discoveredStreams.value = listOf(item) + _discoveredStreams.value.filter { it.hostIp != ip }
    }

    fun stopDiscovery() {
        if (isDiscovering && discoveryListener != null) {
            try {
                nsdManager.stopServiceDiscovery(discoveryListener)
            } catch (_: Exception) {}
            isDiscovering = false
            discoveryListener = null
        }
    }
}
