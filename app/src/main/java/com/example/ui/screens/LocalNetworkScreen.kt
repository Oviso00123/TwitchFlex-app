package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.p2p.DiscoveredStream
import com.example.p2p.LocalStreamServer
import com.example.ui.theme.TwitchBlack
import com.example.ui.theme.TwitchBorder
import com.example.ui.theme.TwitchGold
import com.example.ui.theme.TwitchLiveRed
import com.example.ui.theme.TwitchSurface
import com.example.ui.theme.TwitchSurfaceElevated
import com.example.ui.theme.TwitchText
import com.example.ui.theme.TwitchTextMuted
import com.example.viewmodel.StreamViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

@Composable
fun LocalNetworkScreen(
    viewModel: StreamViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val liveState by viewModel.liveState.collectAsState()
    val discoveredStreams by viewModel.streamDiscovery.discoveredStreams.collectAsState()

    var manualIp by remember { mutableStateOf("") }
    var activeViewerStream by remember { mutableStateOf<DiscoveredStream?>(null) }

    // Start listening on UDP port 8081 for nearby streamers
    DisposableEffect(Unit) {
        viewModel.streamDiscovery.startListening(scope)
        onDispose {
            viewModel.streamDiscovery.stopListening()
        }
    }

    if (activeViewerStream != null) {
        // IN-APP P2P LOCAL VIEWER PLAYER
        InAppLocalViewerPlayer(
            stream = activeViewerStream!!,
            onClose = { activeViewerStream = null },
            modifier = modifier
        )
    } else {
        // MAIN P2P DISCOVERY & SERVER SCREEN
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(TwitchBlack)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF00F5D4).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiTethering,
                        contentDescription = null,
                        tint = Color(0xFF00F5D4),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "📡 Live nella tua Rete / Hotspot",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Condividi e guarda live su Wi-Fi locale senza internet",
                        fontSize = 12.sp,
                        color = TwitchTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. STREAMER LOCAL SERVER CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("p2p_server_card"),
                colors = CardDefaults.cardColors(containerColor = TwitchSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Il Tuo Server P2P",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 15.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    if (liveState.localServerRunning) Color(0xFF00F5D4).copy(alpha = 0.2f) else TwitchSurfaceElevated,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (liveState.localServerRunning) "🟢 Server Attivo (Porta 8080)" else "⚪ In Attesa di Live",
                                color = if (liveState.localServerRunning) Color(0xFF00F5D4) else TwitchTextMuted,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val serverUrl = "http://${LocalStreamServer.getLocalIpAddress()}:8080"
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(TwitchSurfaceElevated, RoundedCornerShape(8.dp))
                            .border(1.dp, TwitchBorder, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                text = "Link Web Viewer per gli amici:",
                                fontSize = 11.sp,
                                color = TwitchTextMuted
                            )
                            Text(
                                text = serverUrl,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(
                                text = "Qualsiasi amico sul tuo Wi-Fi o Hotspot può aprire questo link per guardare la diretta, chattare e fare Sub!",
                                fontSize = 11.sp,
                                color = TwitchText
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Guarda la mia live su Twitch Finto!")
                                putExtra(Intent.EXTRA_TEXT, "Entra nella mia live locale su: $serverUrl")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Condividi Live Locale"))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("share_p2p_link_button"),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Condividi Link con Amici in Rete", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. DISCOVERED STREAMS ON NETWORK
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📡 Streamer Rilevati in Rete (${discoveredStreams.size})",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
                IconButton(onClick = { viewModel.streamDiscovery.startListening(scope) }) {
                    Icon(Icons.Default.Refresh, contentDescription = "Aggiorna", tint = Color(0xFF00F5D4))
                }
            }

            if (discoveredStreams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TwitchSurface, RoundedCornerShape(12.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔍", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Nessun altro streamer attivo rilevato al momento",
                            color = TwitchText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Connettiti alla stessa rete Wi-Fi o Hotspot di un amico, oppure inserisci il suo IP qui sotto.",
                            color = TwitchTextMuted,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                discoveredStreams.forEach { stream ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = TwitchSurface),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "👑", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stream.channelName,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "${stream.category} • IP: ${stream.ip}:${stream.port}",
                                    color = TwitchTextMuted,
                                    fontSize = 11.sp
                                )
                            }
                            Button(
                                onClick = { activeViewerStream = stream },
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4)),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Guarda", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. MANUAL IP CONNECT
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = TwitchSurface),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Connessione Manuale per IP",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Inserisci l'indirizzo IP del dispositivo dello streamer (es. 192.168.43.1):",
                        fontSize = 11.sp,
                        color = TwitchTextMuted,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = manualIp,
                            onValueChange = { manualIp = it },
                            placeholder = { Text("192.168.X.X", color = TwitchTextMuted, fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("manual_ip_input"),
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = TwitchSurfaceElevated,
                                unfocusedContainerColor = TwitchSurfaceElevated,
                                focusedBorderColor = Color(0xFF00F5D4),
                                unfocusedBorderColor = TwitchBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (manualIp.isNotBlank()) {
                                    val cleanIp = manualIp.trim().removePrefix("http://").substringBefore(":")
                                    val stream = DiscoveredStream(
                                        ip = cleanIp,
                                        port = 8080,
                                        channelName = "Live su $cleanIp",
                                        category = "P2P",
                                        viewers = 1
                                    )
                                    viewModel.streamDiscovery.addManualStream(cleanIp)
                                    activeViewerStream = stream
                                }
                            },
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("connect_manual_ip_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4))
                        ) {
                            Text("Connetti", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InAppLocalViewerPlayer(
    stream: DiscoveredStream,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var chatInput by remember { mutableStateOf("") }
    var viewerMessages by remember { mutableStateOf(listOf("Benvenuto nella live locale!")) }
    var frameTimestamp by remember { mutableStateOf(System.currentTimeMillis()) }
    var subSuccessMessage by remember { mutableStateOf<String?>(null) }

    val frameUrl = "http://${stream.ip}:${stream.port}/api/frame?t=$frameTimestamp"
    val client = remember { OkHttpClient() }

    // Periodic frame refresh loop
    LaunchedEffect(Unit) {
        while (true) {
            delay(200) // ~5 FPS for frame polling in viewer
            frameTimestamp = System.currentTimeMillis()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(TwitchBlack)
            .testTag("in_app_viewer_player")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Player Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TwitchSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stream.channelName,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "📡 In onda da ${stream.ip}:${stream.port}",
                        color = Color(0xFF00F5D4),
                        fontSize = 11.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .background(TwitchLiveRed, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("LIVE", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                }
            }

            // Video Frame Player Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = frameUrl,
                    contentDescription = "Video Live",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Sub Banner / Action Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TwitchSurfaceElevated)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Supporta lo streamer:",
                    color = TwitchTextMuted,
                    fontSize = 12.sp
                )
                Button(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            try {
                                val body = JSONObject().apply {
                                    put("username", "Amico_P2P")
                                    put("tier", "Livello 1")
                                }.toString()
                                val req = Request.Builder()
                                    .url("http://${stream.ip}:${stream.port}/api/sub")
                                    .post(body.toRequestBody("application/json".toMediaType()))
                                    .build()
                                client.newCall(req).execute()
                                withContext(Dispatchers.Main) {
                                    subSuccessMessage = "🎉 Sub inviata con successo allo streamer!"
                                }
                            } catch (_: Exception) {}
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TwitchGold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("send_p2p_sub_button")
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Fai Sub (€3.99)", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            }

            subSuccessMessage?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TwitchGold.copy(alpha = 0.2f))
                        .padding(8.dp)
                ) {
                    Text(text = it, color = TwitchGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Chat Feed
            Text(
                text = "💬 Chat in Tempo Reale",
                fontWeight = FontWeight.Bold,
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                items(viewerMessages) { msg ->
                    Text(
                        text = msg,
                        color = Color.White,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }

            // Chat Input
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(TwitchSurface)
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = { Text("Scrivi in live...", color = TwitchTextMuted, fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("p2p_viewer_chat_input"),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = TwitchSurfaceElevated,
                        unfocusedContainerColor = TwitchSurfaceElevated,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = TwitchBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        if (chatInput.isNotBlank()) {
                            val msg = chatInput.trim()
                            viewerMessages = viewerMessages + "Tu: $msg"
                            chatInput = ""
                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val body = JSONObject().apply {
                                        put("username", "Amico_P2P")
                                        put("message", msg)
                                    }.toString()
                                    val req = Request.Builder()
                                        .url("http://${stream.ip}:${stream.port}/api/chat")
                                        .post(body.toRequestBody("application/json".toMediaType()))
                                        .build()
                                    client.newCall(req).execute()
                                } catch (_: Exception) {}
                            }
                        }
                    },
                    modifier = Modifier
                        .size(46.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                        .testTag("send_p2p_chat_button")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
