package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.data.DaySubscriber
import com.example.data.SponsorMissionType
import com.example.data.StreamCategory
import com.example.data.StreamSessionSummary
import com.example.data.UserBadge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.example.ui.components.CameraPreview
import com.example.ui.components.LandscapeTuStyleGlassOverlay
import com.example.ui.components.TuStyleButton
import com.example.ui.components.TuStyleElectricBlue
import com.example.ui.components.TuStyleGlassBox
import com.example.ui.components.TuStyleGlassCard
import com.example.ui.components.TuStyleNeonCyan
import com.example.ui.components.TuStylePurple
import com.example.ui.theme.TwitchBlack
import com.example.ui.theme.TwitchBorder
import com.example.ui.theme.TwitchGold
import com.example.ui.theme.TwitchLiveRed
import com.example.ui.theme.TwitchSurface
import com.example.ui.theme.TwitchSurfaceElevated
import com.example.ui.theme.TwitchText
import com.example.ui.theme.TwitchTextMuted
import com.example.viewmodel.StreamViewModel
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
fun LiveStreamScreen(
    viewModel: StreamViewModel,
    hasCameraPermission: Boolean,
    hasAudioPermission: Boolean,
    modifier: Modifier = Modifier,
    onOpenHub: (() -> Unit)? = null
) {
    val liveState by viewModel.liveState.collectAsState()
    val userProfile by viewModel.userProfile.collectAsState()
    val micAmplitude by viewModel.voiceDetector.amplitude.collectAsState()
    val silenceSeconds by viewModel.voiceDetector.silenceDurationSeconds.collectAsState()
    val isSpeaking by viewModel.voiceDetector.isSpeaking.collectAsState()
    val daySubscribers by viewModel.daySubscribers.collectAsState()
    val postStreamSummary by viewModel.postStreamSummary.collectAsState()
    val activeSubGoal by viewModel.activeSubGoal.collectAsState()
    val onlyCamMode by viewModel.onlyCamMode.collectAsState()

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val systemLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var manualLandscapeOverride by remember { mutableStateOf(false) }
    val isLandscape = systemLandscape || manualLandscapeOverride

    var hudVisible by remember { mutableStateOf(true) }
    var showLandscapeGlassOverlay by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var showEndStreamConfirmation by remember { mutableStateOf(false) }
    var showDaySubscribersDialog by remember { mutableStateOf(false) }
    var showSubathonMilestonesDialog by remember { mutableStateOf(false) }
    var selectedChatMessageForModeration by remember { mutableStateOf<ChatMessage?>(null) }
    var quickChatText by remember { mutableStateOf("") }

    // Advanced Draggable, Pinnable, Resizable Chat States
    var chatOffsetX by remember { mutableFloatStateOf(16f) }
    var chatOffsetY by remember { mutableFloatStateOf(340f) }
    var chatWidthDp by remember { mutableFloatStateOf(270f) }
    var chatHeightDp by remember { mutableFloatStateOf(190f) }
    var isChatPinned by remember { mutableStateOf(false) }
    var showChatFloatingBar by remember { mutableStateOf(false) }
    var isChatOverlayVisible by remember { mutableStateOf(true) }

    // Draggable Sub Goal Widget (Positioned to the RIGHT side)
    var subGoalOffsetX by remember { mutableFloatStateOf(-12f) }
    var subGoalOffsetY by remember { mutableFloatStateOf(76f) }

    // Draggable Donation Alert Widget (5 seconds persistent, movable anywhere, non-deletable)
    var donationAlertOffsetX by remember { mutableFloatStateOf(0f) }
    var donationAlertOffsetY by remember { mutableFloatStateOf(0f) }

    // Draggable Bottom HUD offset
    var hudOffsetX by remember { mutableFloatStateOf(0f) }
    var hudOffsetY by remember { mutableFloatStateOf(0f) }

    if (!liveState.isLive) {
        PreLiveStartView(
            userProfile = userProfile,
            currentCategory = liveState.currentCategory,
            streamTitle = liveState.streamTitle,
            onTitleChange = { viewModel.updateStreamTitle(it) },
            onCategoryClick = { showCategoryDialog = true },
            onStartStream = { viewModel.startStream() },
            localIp = liveState.localIpAddress,
            onOpenHub = onOpenHub,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(TwitchBlack)
                .testTag("live_screen_container")
                // Collapsible HUD on single tap, "TU Style Glass" overlay on double tap, restore chat on long press
                .pointerInput(isLandscape) {
                    detectTapGestures(
                        onTap = {
                            hudVisible = !hudVisible
                        },
                        onDoubleTap = {
                            if (isLandscape) {
                                showLandscapeGlassOverlay = !showLandscapeGlassOverlay
                            }
                        },
                        onLongPress = {
                            // Long-press anywhere on screen restores hidden chat overlay
                            isChatOverlayVisible = true
                        }
                    )
                }
        ) {
            // 1. Camera Preview - Takes full screen seamlessly
            CameraPreview(
                isFrontCamera = liveState.isFrontCamera,
                hasCameraPermission = hasCameraPermission,
                onFrameCaptured = { jpegBytes -> viewModel.updateCameraFrame(jpegBytes) },
                modifier = Modifier.fillMaxSize()
            )

            // 2. INDICATORE DI ENERGIA AMBIENTALE (Luminoso dinamico sfumato verde chiaro e scuro lungo i bordi)
            AnimatedVisibility(
                visible = liveState.latestDonationAlert != null,
                enter = fadeIn(tween(300)),
                exit = fadeOut(tween(600)),
                modifier = Modifier.fillMaxSize()
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "ambient_energy_pulse")
                val pulseIntensity by infiniteTransition.animateFloat(
                    initialValue = 0.45f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "ambient_glow_alpha"
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("ambient_energy_indicator")
                        .drawBehind {
                            // Top border glow: Light green to dark green gradient
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF00F5D4).copy(alpha = 0.85f * pulseIntensity),
                                        Color(0xFF10B981).copy(alpha = 0.50f * pulseIntensity),
                                        Color(0xFF047857).copy(alpha = 0.20f * pulseIntensity),
                                        Color.Transparent
                                    ),
                                    startY = 0f,
                                    endY = 56.dp.toPx()
                                ),
                                size = size.copy(height = 56.dp.toPx())
                            )
                            // Bottom border glow
                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF047857).copy(alpha = 0.20f * pulseIntensity),
                                        Color(0xFF10B981).copy(alpha = 0.50f * pulseIntensity),
                                        Color(0xFF00F5D4).copy(alpha = 0.85f * pulseIntensity)
                                    ),
                                    startY = size.height - 56.dp.toPx(),
                                    endY = size.height
                                ),
                                topLeft = Offset(0f, size.height - 56.dp.toPx()),
                                size = size.copy(height = 56.dp.toPx())
                            )
                            // Left border glow
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF00F5D4).copy(alpha = 0.70f * pulseIntensity),
                                        Color(0xFF047857).copy(alpha = 0.30f * pulseIntensity),
                                        Color.Transparent
                                    ),
                                    startX = 0f,
                                    endX = 42.dp.toPx()
                                ),
                                size = size.copy(width = 42.dp.toPx())
                            )
                            // Right border glow
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color(0xFF047857).copy(alpha = 0.30f * pulseIntensity),
                                        Color(0xFF00F5D4).copy(alpha = 0.70f * pulseIntensity)
                                    ),
                                    startX = size.width - 42.dp.toPx(),
                                    endX = size.width
                                ),
                                topLeft = Offset(size.width - 42.dp.toPx(), 0f),
                                size = size.copy(width = 42.dp.toPx())
                            )
                        }
                )
            }

            // Sleep stream or AFK dark overlay if active
            if (liveState.subathonState.isSleepStreamMode) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.88f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌙", fontSize = 48.sp)
                        Text("SLEEP STREAM IN CORSO", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        Text("Lo streamer sta riposando. Chat lenta attiva & musica Lo-Fi...", color = TwitchTextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.toggleSleepStream() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Sveglia Streamer ☀️", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
            }

            // AFK SCREEN OVERLAY
            if (liveState.afkState.isAfk) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.92f))
                        .testTag("afk_screen_overlay"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Text("☕", fontSize = 52.sp)
                        Text("TORNO SUBITO!", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                        Text("Pausa caffè per lo streamer • Musica Lo-Fi in riproduzione 🎵", color = TwitchTextMuted, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // AFK Chat Minigame Card
                        Card(
                            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFF00F5D4), RoundedCornerShape(12.dp)),
                            colors = CardDefaults.cardColors(containerColor = TwitchSurfaceElevated),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🎮 MINI-GIOCO CHAT ATTIVO", color = Color(0xFF00F5D4), fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                }
                                Text("I bot e la community stanno giocando al Quiz Trivia & Lotteria in chat!", color = TwitchTextMuted, fontSize = 11.sp, modifier = Modifier.padding(vertical = 4.dp))
                                liveState.afkState.quizWinner?.let { winner ->
                                    Text("🏆 Ultimo Vincitore Quiz: $winner (+10 pt VIP)", color = TwitchGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                liveState.afkState.lotteryWinner?.let { winner ->
                                    Text("🎡 Estratto Lotteria: $winner!", color = Color(0xFF00F5D4), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { viewModel.toggleAfkMode() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00F5D4)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("resume_live_from_afk_button")
                        ) {
                            Text("Riprendi la Diretta Ora 🚀", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Subtle gradient shade for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = if (hudVisible) 0.75f else 0.15f),
                                Color.Transparent,
                                Color.Black.copy(alpha = if (hudVisible) 0.85f else 0.2f)
                            )
                        )
                    )
            )

            // HUD Collapsible Wrapper
            AnimatedVisibility(
                visible = hudVisible,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {

                    if (!onlyCamMode) {
                        // Draggable Widget: Sub Goal in Liquid Glass style (Positioned on the RIGHT side)
                        TuStyleGlassBox(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .offset { IntOffset(subGoalOffsetX.roundToInt(), subGoalOffsetY.roundToInt()) }
                                .pointerInput(Unit) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        subGoalOffsetX += dragAmount.x
                                        subGoalOffsetY += dragAmount.y
                                    }
                                }
                                .clickable { onOpenHub?.invoke() }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("draggable_sub_goal_widget"),
                            shape = RoundedCornerShape(12.dp),
                            borderWidth = 1.dp
                        ) {
                            val currentGoal = activeSubGoal
                            val goalTitle = currentGoal?.let { "${it.type.emoji} ${it.title}" } ?: "🎯 Nessun Goal"
                            val currentVal = currentGoal?.currentAmount ?: 0
                            val targetVal = currentGoal?.targetAmount ?: 0
                            val progress = if (targetVal > 0) (currentVal.toFloat() / targetVal).coerceIn(0f, 1f) else 0f

                            Column(modifier = Modifier.width(140.dp).padding(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(goalTitle, color = TwitchGold, fontWeight = FontWeight.Bold, fontSize = 10.sp, maxLines = 1)
                                    Text(if (targetVal > 0) "$currentVal/$targetVal" else "Crea Goal", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
                                }
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .padding(top = 4.dp),
                                    color = TwitchGold,
                                    trackColor = TwitchSurfaceElevated
                                )
                            }
                        }
                    }

                    // TOP STATUS BAR & HEADER
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        TopLiveStatusBar(
                            durationSeconds = liveState.durationSeconds,
                            viewers = liveState.viewersCount,
                            category = liveState.currentCategory,
                            onCategoryClick = { showCategoryDialog = true },
                            sessionEarnings = liveState.sessionSubEarnings + liveState.sessionDonationEarnings + liveState.sessionHourlyEarnings + liveState.sessionSponsorEarnings,
                            silenceSeconds = silenceSeconds,
                            micAmplitude = micAmplitude,
                            isSpeaking = isSpeaking,
                            isNightStream = liveState.isNightStream,
                            isLandscape = isLandscape,
                            onToggleLandscape = { manualLandscapeOverride = !manualLandscapeOverride },
                            onToggleSubscribers = { showDaySubscribersDialog = true },
                            todaySubCount = daySubscribers.size
                        )

                        // SUBATHON DYNAMIC BANNER (quando categoria è MARATONA)
                        if (liveState.subathonState.isEnabled && !onlyCamMode) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .background(Brush.horizontalGradient(listOf(Color(0xFFFF5722), Color(0xFFFF9800))), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("subathon_banner")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("⏱️ MARATONA (SUBATHON): ${formatTime(liveState.subathonState.secondsRemaining)}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                        Text("+30s per ogni Sub ⭐ • +10s per ogni € donato 💎", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(
                                            onClick = { showSubathonMilestonesDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text("Traguardi", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { viewModel.toggleSleepStream() },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.6f)),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(28.dp)
                                        ) {
                                            Text(if (liveState.subathonState.isSleepStreamMode) "Sveglia ☀️" else "Sleep 🌙", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        // FLASH SPONSOR MISSION CARD
                        if (!onlyCamMode) liveState.activeSponsorMission?.let { mission ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .background(Color(android.graphics.Color.parseColor(mission.brand.colorHex)).copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                                    .border(1.dp, Color.White, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                                    .testTag("active_sponsor_mission_card")
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(mission.brand.emoji, fontSize = 16.sp)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("CONTRATTO FLASH: ${mission.title}", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                        }
                                        Text("+${"%.2f".format(mission.rewardEuros)} €", color = Color.White, fontWeight = FontWeight.Black, fontSize = 13.sp)
                                    }
                                    Text(mission.description, color = Color.White.copy(alpha = 0.95f), fontSize = 10.sp, modifier = Modifier.padding(vertical = 2.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("⏱️ Tempo: ${mission.secondsRemaining}s", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (mission.type == SponsorMissionType.SHOW_PRODUCT_30S) {
                                                Button(
                                                    onClick = { viewModel.setHoldingProduct(!mission.isHoldingProduct) },
                                                    shape = RoundedCornerShape(6.dp),
                                                    colors = ButtonDefaults.buttonColors(containerColor = if (mission.isHoldingProduct) Color(0xFF00F5D4) else Color.White),
                                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                    modifier = Modifier.height(28.dp)
                                                ) {
                                                    Text(if (mission.isHoldingProduct) "Inquadrato! 📹" else "Mostra Prodotto", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                            Button(
                                                onClick = { viewModel.completeSponsorMission() },
                                                shape = RoundedCornerShape(6.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text("Completa ✅", color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // VIEWER BOUNTY CARD (Sfida su donazione)
                        if (!onlyCamMode) liveState.activeViewerBounty?.let { bounty ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .background(Color(0xFF6200EA).copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                                    .border(1.dp, TwitchGold, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                                    .testTag("active_viewer_bounty_card")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("🎁 SFIDA BOUNTY DA ${bounty.donorName} (+${"%.2f".format(bounty.bountyEuros)} €)", color = TwitchGold, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                        Text("\"${bounty.challengeDescription}\"", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 11.sp)
                                        Text("Tempo rimasto: ${bounty.secondsLeft}s", color = TwitchTextMuted, fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.completeViewerBounty() },
                                        shape = RoundedCornerShape(6.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = TwitchGold),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.height(30.dp)
                                    ) {
                                        Text("Fatto! ✅", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // HARDWARE GLITCH ALERT (Guasto Tecnico)
                        liveState.activeHardwareGlitch?.let { glitch ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .background(TwitchLiveRed.copy(alpha = 0.92f), RoundedCornerShape(10.dp))
                                    .border(2.dp, Color.White, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                                    .testTag("hardware_glitch_alert")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("⚠️ ${glitch.type.emoji} ${glitch.type.title} (${glitch.secondsLeft}s)", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
                                        Text(glitch.type.description, color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp)
                                    }
                                    Button(
                                        onClick = { viewModel.tapFixHardwareGlitch() },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text("${glitch.type.fixAction} (${glitch.tapsCompleted}/${glitch.type.tapsNeeded})", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        // Stream Title Banner
                        Text(
                            text = liveState.streamTitle,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        // SUB ALERT BANNER (LIQUID GLASS)
                        liveState.latestSubAlert?.let { sub ->
                            AnimatedVisibility(visible = true, enter = slideInVertically() + fadeIn(), exit = slideOutVertically() + fadeOut()) {
                                TuStyleGlassBox(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    borderWidth = 1.2.dp,
                                    borderBrush = Brush.linearGradient(listOf(TwitchGold, Color(0xFF9146FF), Color.White))
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text("⭐ ${sub.subscriberName} si è abbonato! (${sub.tier.displayName})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text("+${"%.2f".format(sub.payoutAmount)} €", color = TwitchGold, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        // DONATION ALERT (LIQUID GLASS - SPOSTABILE MA NON ELIMINABILE PER 5 SECONDI)
                        liveState.latestDonationAlert?.let { don ->
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(donationAlertOffsetX.roundToInt(), donationAlertOffsetY.roundToInt()) }
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            donationAlertOffsetX += dragAmount.x
                                            donationAlertOffsetY += dragAmount.y
                                        }
                                    }
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .testTag("draggable_donation_alert")
                            ) {
                                TuStyleGlassBox(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    borderWidth = 1.4.dp,
                                    borderBrush = Brush.linearGradient(
                                        listOf(
                                            Color(0xFF00F5D4),
                                            Color(0xFF10B981),
                                            Color(0xFF047857),
                                            Color.White
                                        )
                                    ),
                                    backgroundBrush = Brush.verticalGradient(
                                        listOf(
                                            Color(0xE60A261A),
                                            Color(0xD90D3822),
                                            Color(0xEB061A10)
                                        )
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DragIndicator,
                                                contentDescription = "Trascina avviso donazione",
                                                tint = Color(0xFF00F5D4).copy(alpha = 0.8f),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = "💎 ${don.donorName}",
                                                        color = Color(0xFF00F5D4),
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 12.sp
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "ha donato con TTS",
                                                        color = Color.White.copy(alpha = 0.7f),
                                                        fontSize = 10.sp
                                                    )
                                                }
                                                if (don.message.isNotBlank()) {
                                                    Text(
                                                        text = "\"${don.message}\"",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 11.sp,
                                                        maxLines = 2
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "+${"%.2f".format(don.euros)} €",
                                            color = Color(0xFF00F5D4),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 15.sp,
                                            modifier = Modifier.padding(start = 8.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // RANDOM EVENT (RNG) ALERT
                        liveState.activeRandomEvent?.let { event ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp)
                                    .background(TwitchLiveRed.copy(alpha = 0.9f), RoundedCornerShape(10.dp))
                                    .border(2.dp, Color.White, RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                                    .testTag("random_event_alert")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${event.type.emoji} ${event.type.title} (${event.secondsRemaining}s)",
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.sp
                                        )
                                        Text(
                                            text = event.type.description,
                                            color = Color.White.copy(alpha = 0.9f),
                                            fontSize = 10.sp
                                        )
                                    }
                                    Button(
                                        onClick = { viewModel.resolveActiveRandomEvent() },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(event.type.actionLabel, color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // ADVANCED DRAGGABLE, PINNABLE & RESIZABLE CHAT (LIQUID GLASS)
                    if (isChatOverlayVisible && !onlyCamMode) {
                        Box(
                            modifier = Modifier
                                .offset { IntOffset(chatOffsetX.roundToInt(), chatOffsetY.roundToInt()) }
                        ) {
                        Column {
                            // Floating Command Bar (GUI) above chat on drag release
                            AnimatedVisibility(
                                visible = showChatFloatingBar && !isChatPinned,
                                enter = fadeIn() + slideInVertically { it / 2 },
                                exit = fadeOut() + slideOutVertically { it / 2 }
                            ) {
                                TuStyleGlassBox(
                                    modifier = Modifier
                                        .padding(bottom = 6.dp)
                                        .testTag("chat_floating_command_bar"),
                                    shape = RoundedCornerShape(12.dp),
                                    borderWidth = 1.dp
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Posizione modificata",
                                            color = TwitchTextMuted,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        // 1. Pin / Fissa button
                                        Button(
                                            onClick = {
                                                isChatPinned = true
                                                showChatFloatingBar = false
                                            },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier
                                                .height(26.dp)
                                                .testTag("pin_chat_button")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PushPin,
                                                contentDescription = "Fissa",
                                                tint = Color.White,
                                                modifier = Modifier.size(12.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Fissa",
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        IconButton(
                                            onClick = { showChatFloatingBar = false },
                                            modifier = Modifier.size(22.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Chiudi barra",
                                                tint = TwitchTextMuted,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // The Chat Container in Tu Style Liquid Glass
                            TuStyleGlassBox(
                                modifier = Modifier
                                    .width(chatWidthDp.dp)
                                    .height(chatHeightDp.dp)
                                    .testTag("advanced_liquid_glass_chat")
                                    // Drag gesture (active when not pinned)
                                    .pointerInput(isChatPinned) {
                                        if (!isChatPinned) {
                                            detectDragGestures(
                                                onDrag = { change, dragAmount ->
                                                    change.consume()
                                                    chatOffsetX = (chatOffsetX + dragAmount.x).coerceIn(0f, 600f)
                                                    chatOffsetY = (chatOffsetY + dragAmount.y).coerceIn(40f, 1200f)
                                                },
                                                onDragEnd = {
                                                    showChatFloatingBar = true
                                                }
                                            )
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                borderWidth = 1.2.dp
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp, vertical = 6.dp)
                                    ) {
                                        // Chat Header Row
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                if (!isChatPinned) {
                                                    Icon(
                                                        imageVector = Icons.Default.DragIndicator,
                                                        contentDescription = "Trascina chat",
                                                        tint = Color.White.copy(alpha = 0.6f),
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                }
                                                Text(
                                                    text = "Chat Live",
                                                    color = Color.White,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                // Pin status pill / toggle
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    modifier = Modifier
                                                        .clickable {
                                                            isChatPinned = !isChatPinned
                                                            if (!isChatPinned) {
                                                                showChatFloatingBar = true
                                                            }
                                                        }
                                                        .background(
                                                            if (isChatPinned) Color(0xFF00F5D4).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                                                            RoundedCornerShape(6.dp)
                                                        )
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        .testTag("toggle_pin_chat_header")
                                                ) {
                                                    Icon(
                                                        imageVector = if (isChatPinned) Icons.Default.Lock else Icons.Default.LockOpen,
                                                        contentDescription = if (isChatPinned) "Bloccata" else "Sbloccata",
                                                        tint = if (isChatPinned) Color(0xFF00F5D4) else TwitchTextMuted,
                                                        modifier = Modifier.size(11.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = if (isChatPinned) "Fissata" else "Mobile",
                                                        color = if (isChatPinned) Color(0xFF00F5D4) else TwitchTextMuted,
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }

                                                Spacer(modifier = Modifier.width(6.dp))

                                                // Prominent 'X' button to hide chat overlay completely
                                                Box(
                                                    modifier = Modifier
                                                        .size(22.dp)
                                                        .background(TwitchLiveRed.copy(alpha = 0.3f), CircleShape)
                                                        .border(1.dp, TwitchLiveRed.copy(alpha = 0.8f), CircleShape)
                                                        .clickable { isChatOverlayVisible = false }
                                                        .testTag("hide_chat_overlay_x_button"),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Nascondi Chat",
                                                        tint = Color.White,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Chat messages list
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .weight(1f)
                                                .padding(bottom = 6.dp)
                                        ) {
                                            LiveChatFeed(
                                                messages = liveState.chatMessages,
                                                onMessageClick = { selectedChatMessageForModeration = it }
                                            )
                                        }
                                    }

                                    // 2. RESIZE BUTTON IN BOTTOM-RIGHT CORNER (Two diagonal arrows)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(3.dp)
                                            .size(24.dp)
                                            .background(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                                RoundedCornerShape(6.dp)
                                            )
                                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                            .pointerInput(Unit) {
                                                detectDragGestures { change, dragAmount ->
                                                    change.consume()
                                                    val newW = (chatWidthDp + dragAmount.x / density.density).coerceIn(180f, 420f)
                                                    val newH = (chatHeightDp + dragAmount.y / density.density).coerceIn(120f, 450f)
                                                    chatWidthDp = newW
                                                    chatHeightDp = newH
                                                }
                                            }
                                            .testTag("resize_chat_handle"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInFull,
                                            contentDescription = "Ridimensiona Chat",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    }

                    if (!isChatOverlayVisible && !onlyCamMode) {
                        TuStyleGlassBox(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(start = 14.dp, bottom = 68.dp)
                                .clickable { isChatOverlayVisible = true }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("restore_chat_hint_pill"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💬 Chat nascosta (Tocca o premi a lungo)", color = Color.White.copy(alpha = 0.9f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // BOTTOM CONTROL PANEL (Tu Style Glass, non-obstructive in landscape)
                    if (!onlyCamMode) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                            .background(
                                if (isLandscape) TwitchBlack.copy(alpha = 0.55f)
                                else TwitchBlack.copy(alpha = 0.88f)
                            )
                            .border(1.dp, if (isLandscape) TuStylePurple.copy(alpha = 0.35f) else TwitchBorder)
                            .padding(
                                horizontal = if (isLandscape) 28.dp else 10.dp,
                                vertical = if (isLandscape) 4.dp else 6.dp
                            )
                    ) {

                        // Chat text input row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = quickChatText,
                                onValueChange = { quickChatText = it },
                                placeholder = { Text("Scrivi in chat o usa comandi (!discord)...", fontSize = 11.sp, color = TwitchTextMuted) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("streamer_chat_input"),
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
                                    if (quickChatText.isNotBlank()) {
                                        viewModel.sendStreamerChat(quickChatText.trim())
                                        quickChatText = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                                    .testTag("send_streamer_chat_button")
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Invia", tint = Color.White, modifier = Modifier.size(16.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Bottom Action Controls in Tu Style Creator Design (Drag & Drop freely enabled)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TuStyleButton(
                                icon = if (liveState.isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                label = if (liveState.isMuted) "Muto" else "Mic",
                                gradientColors = if (liveState.isMuted) listOf(TwitchLiveRed, Color(0xFF880022)) else listOf(TuStylePurple, TuStyleNeonCyan),
                                glowColor = if (liveState.isMuted) TwitchLiveRed else TuStyleNeonCyan,
                                onClick = { viewModel.toggleMute() },
                                testTag = "toggle_mute_button",
                                width = 44.dp,
                                height = 40.dp
                            )

                            TuStyleButton(
                                icon = Icons.Default.Cameraswitch,
                                label = if (liveState.isFrontCamera) "Fronte" else "Retro",
                                gradientColors = listOf(TuStylePurple, TuStyleElectricBlue),
                                glowColor = TuStyleElectricBlue,
                                onClick = { viewModel.toggleCamera() },
                                testTag = "toggle_camera_button",
                                width = 44.dp,
                                height = 40.dp
                            )

                            TuStyleButton(
                                icon = Icons.Default.Coffee,
                                label = "AFK",
                                gradientColors = listOf(Color(0xFFFF8800), Color(0xFFFFB100)),
                                glowColor = Color(0xFFFFB100),
                                onClick = { viewModel.toggleAfkMode() },
                                testTag = "toggle_afk_button",
                                width = 44.dp,
                                height = 40.dp
                            )

                            TuStyleButton(
                                icon = Icons.Default.Category,
                                label = "Cat",
                                gradientColors = listOf(TuStylePurple, Color(0xFFC084FC)),
                                glowColor = TuStylePurple,
                                onClick = { showCategoryDialog = true },
                                testTag = "change_category_button",
                                width = 44.dp,
                                height = 40.dp
                            )

                            TuStyleButton(
                                icon = Icons.Default.Campaign,
                                label = "Clip",
                                gradientColors = listOf(TuStyleNeonCyan, TuStyleElectricBlue),
                                glowColor = TuStyleNeonCyan,
                                onClick = { viewModel.cutAndSaveClip("Momento Epico su ${liveState.currentCategory.name}") },
                                testTag = "cut_clip_button",
                                width = 44.dp,
                                height = 40.dp
                            )

                            TuStyleButton(
                                icon = Icons.Default.Group,
                                label = "Sub (${daySubscribers.size})",
                                gradientColors = listOf(Color(0xFFFFB100), TwitchGold),
                                glowColor = TwitchGold,
                                onClick = { showDaySubscribersDialog = true },
                                testTag = "subscribers_dialog_button",
                                width = 44.dp,
                                height = 40.dp
                            )

                            TuStyleButton(
                                icon = Icons.Default.Stop,
                                label = "Stop",
                                gradientColors = listOf(TwitchLiveRed, Color(0xFF990000)),
                                glowColor = TwitchLiveRed,
                                onClick = { showEndStreamConfirmation = true },
                                testTag = "end_stream_button",
                                width = 44.dp,
                                height = 40.dp
                            )
                        }
                    }
                } else {
                    // In Only Cam mode, show only a discreet floating stop button
                    IconButton(
                        onClick = { showEndStreamConfirmation = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(20.dp)
                            .size(48.dp)
                            .background(TwitchLiveRed.copy(alpha = 0.85f), CircleShape)
                            .border(1.dp, Color.White, CircleShape)
                            .testTag("only_cam_stop_button")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Termina Live", tint = Color.White, modifier = Modifier.size(24.dp))
                    }
                }
            }

            // Floating indicator when HUD is hidden
            if (!hudVisible) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 16.dp)
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text("👁️ Tocca per HUD • Doppio tap per Overlay Glass", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
            }

            // "TU STYLE GLASS" OVERLAY IN LANDSCAPE (DOUBLE-TAP ACTIVATED)
            if (isLandscape) {
                LandscapeTuStyleGlassOverlay(
                    visible = showLandscapeGlassOverlay,
                    viewersCount = liveState.viewersCount,
                    subCount = daySubscribers.size,
                    earnedMoneyEuros = liveState.sessionSubEarnings + liveState.sessionDonationEarnings + liveState.sessionHourlyEarnings + liveState.sessionSponsorEarnings,
                    chatMessages = liveState.chatMessages,
                    onClose = { showLandscapeGlassOverlay = false },
                    onSubMoneyClick = { showDaySubscribersDialog = true },
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    // MODERATION DIALOG (TIMEOUT & BAN)
    selectedChatMessageForModeration?.let { msg ->
        AlertDialog(
            onDismissRequest = { selectedChatMessageForModeration = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Modera Utente: ${msg.username}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column {
                    Text("Messaggio:", color = TwitchTextMuted, fontSize = 11.sp)
                    Text("\"${msg.message}\"", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, modifier = Modifier.padding(vertical = 4.dp))
                    if (msg.isFilteredByAutoMod) {
                        Text("🛡️ Questo messaggio è stato filtrato da AutoMod con asterischi.", color = TwitchGold, fontSize = 11.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Seleziona l'azione disciplinare da applicare:", color = TwitchTextMuted, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.timeoutUser(msg.id, msg.username)
                            selectedChatMessageForModeration = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB100))
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Timeout 30s", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                    Button(
                        onClick = {
                            viewModel.banHater(msg.id, msg.username)
                            selectedChatMessageForModeration = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TwitchLiveRed)
                    ) {
                        Text("🔨 Ban Permanente", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedChatMessageForModeration = null }) {
                    Text("Annulla", color = Color.White)
                }
            },
            containerColor = TwitchSurface
        )
    }

    // DAILY SUBSCRIBERS POPUP MENU
    if (showDaySubscribersDialog) {
        AlertDialog(
            onDismissRequest = { showDaySubscribersDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, contentDescription = null, tint = TwitchGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Abbonati del Giorno (${daySubscribers.size})", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth().height(260.dp)) {
                    if (daySubscribers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Nessun abbonato ancora oggi.\nI nuovi sub compariranno qui con nomi e cognomi reali!", color = TwitchTextMuted, fontSize = 12.sp)
                        }
                    } else {
                        LazyColumn {
                            items(daySubscribers) { sub ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                        .background(TwitchSurfaceElevated, RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(sub.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("${sub.tier.displayName} • ${sub.timeFormatted}", color = TwitchTextMuted, fontSize = 11.sp)
                                    }
                                    Text("+${"%.2f".format(sub.payoutAmount)} €", color = TwitchGold, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showDaySubscribersDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Chiudi", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = TwitchSurface
        )
    }

    // SUBATHON MILESTONES DIALOG
    if (showSubathonMilestonesDialog) {
        AlertDialog(
            onDismissRequest = { showSubathonMilestonesDialog = false },
            title = {
                Text("🎯 Traguardi Subathon Maratona", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Raggiungi i traguardi di sub per sbloccare le sfide in diretta!", color = TwitchTextMuted, fontSize = 11.sp, modifier = Modifier.padding(bottom = 8.dp))
                    liveState.subathonState.milestones.forEach { m ->
                        val reached = liveState.sessionSubsCount >= m.targetSubs
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(if (reached) Color(0xFF00F5D4).copy(alpha = 0.2f) else TwitchSurfaceElevated, RoundedCornerShape(8.dp))
                                .border(1.dp, if (reached) Color(0xFF00F5D4) else TwitchBorder, RoundedCornerShape(8.dp))
                            .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("${m.targetSubs} Sub Goal", color = if (reached) Color(0xFF00F5D4) else Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(m.rewardText, color = TwitchTextMuted, fontSize = 11.sp)
                            }
                            Text(if (reached) "SBLOCCATO! 🎉" else "In corso", color = if (reached) Color(0xFF00F5D4) else TwitchTextMuted, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSubathonMilestonesDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Chiudi", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = TwitchSurface
        )
    }

    // END STREAM CONFIRMATION DIALOG
    if (showEndStreamConfirmation) {
        AlertDialog(
            onDismissRequest = { showEndStreamConfirmation = false },
            title = { Text("Terminare la Diretta?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("⏱️ Durata: ${formatTime(liveState.durationSeconds)}", color = TwitchTextMuted)
                    Text("👥 Picco Spettatori: ${liveState.peakViewers}", color = TwitchTextMuted)
                    Text("⭐ Nuovi Sub: ${liveState.sessionSubsCount}", color = TwitchTextMuted)
                    Text("🏛️ Tasse di Zyro trattenute: -5,50 €", color = TwitchLiveRed, fontWeight = FontWeight.Bold)
                    val gross = liveState.sessionSubEarnings + liveState.sessionDonationEarnings + liveState.sessionHourlyEarnings + liveState.sessionSponsorEarnings
                    val net = maxOf(0.0, gross - 5.50)
                    Text(
                        text = "💰 Guadagno Netto Stimato: ${"%.2f".format(net)} €",
                        color = TwitchGold,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.endStream()
                        showEndStreamConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TwitchLiveRed)
                ) {
                    Text("Termina Ora", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndStreamConfirmation = false }) {
                    Text("Continua Live", color = Color.White)
                }
            },
            containerColor = TwitchSurface
        )
    }

    // POST-LIVE DASHBOARD SUMMARY WITH GEMINI AI COACHING
    postStreamSummary?.let { summary ->
        PostStreamSummaryDialog(
            summary = summary,
            onDismiss = { viewModel.dismissPostStreamSummary() }
        )
    }

    // CATEGORY SELECTION DIALOG
    if (showCategoryDialog) {
        CategorySelectionDialog(
            currentCategory = liveState.currentCategory,
            onSelect = {
                viewModel.setCategory(it)
                showCategoryDialog = false
            },
            onDismiss = { showCategoryDialog = false }
        )
    }
}

@Composable
fun PostStreamSummaryDialog(
    summary: StreamSessionSummary,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("📊 Resoconto Live & Analisi Gemini", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                // Key metrics cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = TwitchSurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("⏱️ Durata", color = TwitchTextMuted, fontSize = 10.sp)
                            Text(formatTime(summary.durationSeconds), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = TwitchSurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("👥 Picco Spect", color = TwitchTextMuted, fontSize = 10.sp)
                            Text("${summary.peakViewers}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = TwitchSurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("⭐ Nuovi Sub", color = TwitchTextMuted, fontSize = 10.sp)
                            Text("+${summary.totalSubs}", color = TwitchGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Breakdown economico con tasse Zyro
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = TwitchSurfaceElevated),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("Ripartizione Economica Sessione:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Guadagno Lordo Sub:", color = TwitchTextMuted, fontSize = 11.sp)
                            Text("+${"%.2f".format(summary.grossSubEarnings)} €", color = Color.White, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tariffa Oraria Zyro:", color = TwitchTextMuted, fontSize = 11.sp)
                            Text("+${"%.2f".format(summary.grossHourlyEarnings)} €", color = Color.White, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Donazioni Bits TTS & Sponsor:", color = TwitchTextMuted, fontSize = 11.sp)
                            Text("+${"%.2f".format(summary.grossDonationEarnings + summary.grossSponsorEarnings)} €", color = Color.White, fontSize = 11.sp)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("🏛️ Tasse di Zyro trattenute:", color = TwitchLiveRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("-${"%.2f".format(summary.zyroTax)} €", color = TwitchLiveRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                        if (summary.platformCut > 0.0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("🏢 Cut Piattaforma (10€ ogni 10 Sub):", color = TwitchLiveRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text("-${"%.2f".format(summary.platformCut)} €", color = TwitchLiveRed, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Guadagno Netto Finale:", color = TwitchGold, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                            Text("${"%.2f".format(summary.netEarnings)} €", color = TwitchGold, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gemini AI Coach Section
                Text("🤖 Analisi & Consigli Coach IA Gemini:", color = Color(0xFF00F5D4), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .padding(top = 4.dp)
                        .background(TwitchBlack, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFF00F5D4).copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    LazyColumn {
                        item {
                            Text(
                                text = summary.aiCoachFeedback.ifBlank { "Generazione analisi in corso con Gemini..." },
                                color = TwitchText,
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Chiudi Riepilogo", fontWeight = FontWeight.Bold)
            }
        },
        containerColor = TwitchSurface
    )
}

@Composable
fun TopLiveStatusBar(
    durationSeconds: Long,
    viewers: Int,
    category: StreamCategory,
    onCategoryClick: () -> Unit,
    sessionEarnings: Double,
    silenceSeconds: Int,
    micAmplitude: Float,
    isSpeaking: Boolean,
    isNightStream: Boolean,
    isLandscape: Boolean,
    onToggleLandscape: () -> Unit,
    onToggleSubscribers: () -> Unit,
    todaySubCount: Int
) {
    TuStyleGlassBox(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        borderWidth = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
        // LIVE badge & Timer
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .background(TwitchLiveRed, RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text("LIVE", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = formatTime(durationSeconds),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        // Viewers count
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("👥", fontSize = 11.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$viewers",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            )
        }

        // Category badge (shortened so it never blocks camera preview)
        Box(
            modifier = Modifier
                .background(TwitchSurfaceElevated, RoundedCornerShape(6.dp))
                .border(1.dp, TwitchBorder, RoundedCornerShape(6.dp))
                .clickable { onCategoryClick() }
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "${category.iconEmoji} ${category.shortName}",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }

        // Subscribers button
        Box(
            modifier = Modifier
                .background(TwitchGold.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                .border(1.dp, TwitchGold, RoundedCornerShape(6.dp))
                .clickable { onToggleSubscribers() }
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = "⭐ Sub ($todaySubCount)",
                color = TwitchGold,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Landscape rotation toggle
        IconButton(
            onClick = onToggleLandscape,
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                Icons.Default.ScreenRotation,
                contentDescription = "Ruota Schermo",
                tint = if (isLandscape) Color(0xFF00F5D4) else Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LiveChatFeed(
    messages: List<ChatMessage>,
    onMessageClick: (ChatMessage) -> Unit
) {
    val listState = rememberLazyListState()
    var isUserInteracting by remember { mutableStateOf(false) }

    // Detect user manual scrolling; resume smooth auto-scrolling when interaction ends
    LaunchedEffect(listState.isScrollInProgress) {
        if (listState.isScrollInProgress) {
            isUserInteracting = true
        } else {
            delay(2000)
            isUserInteracting = false
        }
    }

    LaunchedEffect(messages.size, isUserInteracting) {
        if (messages.isNotEmpty()) {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val isNearBottom = lastVisible >= messages.size - 3
            if (!isUserInteracting || isNearBottom) {
                try {
                    listState.scrollToItem(messages.size - 1)
                } catch (_: Exception) {}
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize()
    ) {
        items(messages, key = { it.id }) { msg ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .combinedClickable(
                        onClick = { onMessageClick(msg) },
                        onLongClick = { onMessageClick(msg) }
                    )
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    if (msg.badge != UserBadge.VIEWER) {
                        Text(
                            text = "${msg.badge.emoji} ",
                            fontSize = 11.sp
                        )
                    }
                    if (msg.isFilteredByAutoMod) {
                        Text(
                            text = "[AutoMod] ",
                            color = Color(0xFFFF9800),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                    val userColor = try {
                        Color(android.graphics.Color.parseColor(msg.colorHex))
                    } catch (_: Exception) {
                        Color.White
                    }
                    Text(
                        text = "${msg.username}: ",
                        color = userColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = msg.message,
                        color = if (msg.isBanned) TwitchTextMuted else Color.White,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun PreLiveStartView(
    userProfile: com.example.data.UserProfile,
    currentCategory: StreamCategory,
    streamTitle: String,
    onTitleChange: (String) -> Unit,
    onCategoryClick: () -> Unit,
    onStartStream: () -> Unit,
    localIp: String,
    modifier: Modifier = Modifier,
    onOpenHub: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TwitchBlack)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📹", fontSize = 54.sp)
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Pronto a Trasmettere?",
            color = Color.White,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp
        )
        Text(
            text = "Canale: ${userProfile.channelName.ifBlank { "Streamer" }} ${userProfile.avatarEmoji}",
            color = TwitchTextMuted,
            fontSize = 13.sp,
            modifier = Modifier.padding(top = 4.dp, bottom = 18.dp)
        )

        OutlinedTextField(
            value = streamTitle,
            onValueChange = onTitleChange,
            label = { Text("Titolo della Live") },
            modifier = Modifier.fillMaxWidth().testTag("stream_title_input"),
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = TwitchSurface,
                unfocusedContainerColor = TwitchSurface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = TwitchBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category card button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCategoryClick() }
                .testTag("pre_live_category_selector"),
            colors = CardDefaults.cardColors(containerColor = TwitchSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, TwitchBorder),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Categoria Selezionata", color = TwitchTextMuted, fontSize = 11.sp)
                    Text("${currentCategory.iconEmoji} ${currentCategory.name}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Text("Cambia >", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Hub Creator quick button
        if (onOpenHub != null) {
            Button(
                onClick = onOpenHub,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("pre_live_open_hub_button"),
                colors = ButtonDefaults.buttonColors(containerColor = TwitchSurfaceElevated),
                border = androidx.compose.foundation.BorderStroke(1.dp, TuStyleNeonCyan),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("🎛️ Gestione Canale & Hub Creator", color = TuStyleNeonCyan, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onStartStream,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("start_stream_big_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TwitchLiveRed),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text("VAI IN ONDA (AVVIA LIVE)", fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
    }
}

@Composable
fun CategorySelectionDialog(
    currentCategory: StreamCategory,
    onSelect: (StreamCategory) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleziona Categoria", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            LazyColumn(modifier = Modifier.height(350.dp)) {
                items(StreamCategory.ALL) { cat ->
                    val isSelected = cat.id == currentCategory.id
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelect(cat) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else TwitchSurfaceElevated
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else TwitchBorder
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(cat.iconEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(cat.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(cat.description, color = TwitchTextMuted, fontSize = 10.sp, lineHeight = 13.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Chiudi", color = Color.White) }
        },
        containerColor = TwitchSurface
    )
}



fun formatTime(totalSeconds: Long): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", h, m, s)
}
