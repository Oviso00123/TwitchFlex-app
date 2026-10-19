package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.ChannelUser
import com.example.data.SubGoal
import com.example.data.SubGoalType
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
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

enum class HubTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PROFILE("Profilo", Icons.Default.Person),
    SUBSCRIBERS("Lista Sub", Icons.Default.Group),
    GOALS("Sub Goal", Icons.Default.TrackChanges),
    ANALYTICS("Analytics", Icons.AutoMirrored.Filled.ShowChart)
}

/**
 * Pannello di Gestione Canale & Hub Creator con grafica Tu Style Glass (Liquid Glass).
 * Diviso in 4 schede principali:
 * 1. Profilo Creator & Personalizzazione
 * 2. Lista Sub & Iscritti
 * 3. Gestore Sub Goal & Obiettivi Live
 * 4. Dashboard Analytics Avanzata
 */
@Composable
fun ChannelManagementScreen(
    viewModel: StreamViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }

    val userProfile by viewModel.userProfile.collectAsState()
    val subGoals by viewModel.subGoals.collectAsState()
    val channelUsers by viewModel.channelUsers.collectAsState()
    val totalSubs by viewModel.totalSubsAllTime.collectAsState()
    val totalEarnings by viewModel.totalEarningsAllTime.collectAsState()
    val totalSeconds by viewModel.totalStreamSecondsAllTime.collectAsState()
    val liveState by viewModel.liveState.collectAsState()
    val postStreamSummary by viewModel.postStreamSummary.collectAsState()
    val viewerHistoryPoints by viewModel.viewerHistoryPoints.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TwitchBlack)
            .testTag("channel_management_screen")
    ) {
        // TOP HEADER BAR IN TU STYLE GLASS
        TuStyleGlassBox(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .testTag("hub_top_header"),
            shape = RoundedCornerShape(16.dp),
            borderWidth = 1.2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile Avatar with circular crop
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(TwitchSurfaceElevated)
                            .border(1.5.dp, TuStyleNeonCyan, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userProfile.profileImageUri.isNullOrBlank()) {
                            AsyncImage(
                                model = userProfile.profileImageUri,
                                contentDescription = "Foto Profilo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(userProfile.avatarEmoji, fontSize = 24.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userProfile.channelName.ifBlank { "MaloReplay" },
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = userProfile.customBadgeEmoji,
                                fontSize = 14.sp
                            )
                        }
                        Text(
                            text = userProfile.handle.ifBlank { "@MaloReplay" },
                            color = TuStyleNeonCyan,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Channel Status Pill
                Box(
                    modifier = Modifier
                        .background(
                            if (liveState.isLive) TwitchLiveRed.copy(alpha = 0.2f) else TuStylePurple.copy(alpha = 0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            if (liveState.isLive) TwitchLiveRed else TuStylePurple,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (liveState.isLive) "🔴 IN DIRETTA" else "⚙️ HUB CREATOR",
                        color = if (liveState.isLive) TwitchLiveRed else TuStyleNeonCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // TAB ROW (4 TABS)
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = TwitchSurface,
            contentColor = Color.White,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = TuStyleNeonCyan,
                    height = 2.5.dp
                )
            },
            modifier = Modifier.border(1.dp, TwitchBorder)
        ) {
            HubTab.values().forEachIndexed { index, tab ->
                val isSelected = selectedTab == index
                Tab(
                    selected = isSelected,
                    onClick = { selectedTab = index },
                    icon = {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.label,
                            tint = if (isSelected) TuStyleNeonCyan else TwitchTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    text = {
                        Text(
                            text = tab.label,
                            color = if (isSelected) Color.White else TwitchTextMuted,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal
                        )
                    },
                    modifier = Modifier.testTag("hub_tab_${tab.name.lowercase()}")
                )
            }
        }

        // TAB CONTENT
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            when (HubTab.values()[selectedTab]) {
                HubTab.PROFILE -> ProfileCustomizationTab(
                    userProfile = userProfile,
                    onSave = { name, handle, bio, avatar, badge, imageUri ->
                        viewModel.updateCreatorProfile(name, handle, bio, avatar, badge, imageUri)
                        Toast.makeText(context, "✅ Profilo e badge aggiornati in tutta l'app!", Toast.LENGTH_SHORT).show()
                    }
                )
                HubTab.SUBSCRIBERS -> SubscribersListTab(
                    channelUsers = channelUsers,
                    onToggleVip = { viewModel.toggleUserVip(it) },
                    onToggleMod = { viewModel.toggleUserMod(it) },
                    onSimulateSub = { user ->
                        viewModel.simulateSubMessage(user)
                        Toast.makeText(context, "⭐ Notifica e TTS Sub simulati per ${user.username}!", Toast.LENGTH_SHORT).show()
                    },
                    onDeleteUser = { userId ->
                        viewModel.deleteChannelUser(userId)
                        Toast.makeText(context, "🗑️ Utente rimosso dall'elenco del canale!", Toast.LENGTH_SHORT).show()
                    }
                )
                HubTab.GOALS -> SubGoalsTab(
                    subGoals = subGoals,
                    onAddGoal = { title, target, current, type ->
                        viewModel.addGoal(title, target, current, type)
                        Toast.makeText(context, "🎯 Nuovo Goal creato!", Toast.LENGTH_SHORT).show()
                    },
                    onRemoveGoal = { goalId ->
                        viewModel.removeGoal(goalId)
                    },
                    onToggleActive = { goalId ->
                        viewModel.toggleGoalActive(goalId)
                    },
                    onDeltaProgress = { goalId, delta ->
                        viewModel.updateGoalProgress(goalId, delta)
                    }
                )
                HubTab.ANALYTICS -> AnalyticsDashboardTab(
                    totalEarnings = totalEarnings + liveState.sessionSubEarnings + liveState.sessionDonationEarnings,
                    totalSeconds = totalSeconds + liveState.durationSeconds,
                    totalSubs = totalSubs + liveState.sessionSubsCount,
                    liveState = liveState,
                    postStreamSummary = postStreamSummary,
                    channelName = userProfile.channelName,
                    viewerHistoryPoints = viewerHistoryPoints
                )
            }
        }
    }
}

// ==========================================
// 1. PROFILO CREATOR & PERSONALIZZAZIONE
// ==========================================
@Composable
private fun ProfileCustomizationTab(
    userProfile: com.example.data.UserProfile,
    onSave: (String, String, String, String, String, String?) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var name by remember(userProfile.channelName) { mutableStateOf(userProfile.channelName) }
    var handle by remember(userProfile.handle) { mutableStateOf(userProfile.handle) }
    var bio by remember(userProfile.bio) { mutableStateOf(userProfile.bio) }
    var avatarEmoji by remember(userProfile.avatarEmoji) { mutableStateOf(userProfile.avatarEmoji) }
    var badgeEmoji by remember(userProfile.customBadgeEmoji) { mutableStateOf(userProfile.customBadgeEmoji) }
    var profileImageUri by remember(userProfile.profileImageUri) { mutableStateOf(userProfile.profileImageUri) }

    var saveFeedbackVisible by remember { mutableStateOf(false) }

    // Android Zero-Permission Photo Picker for gallery images
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            profileImageUri = uri.toString()
        }
    }

    // Camera instant snap launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            // Save bitmap to cache dir and get URI
            try {
                val file = File(context.cacheDir, "creator_profile_${System.currentTimeMillis()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
                }
                profileImageUri = Uri.fromFile(file).toString()
            } catch (_: Exception) {}
        }
    }

    val availableBadges = listOf("👑", "⭐", "🔥", "💎", "⚡", "🚀", "🏆", "🛡️", "👾", "🎯")
    val availableAvatars = listOf("🎮", "🔥", "👑", "💎", "⚡", "🚀", "😎", "🏆", "🦄", "🐱")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Photo Card with circular crop
        TuStyleGlassCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 14.dp
        ) {
            Text(
                text = "Foto Profilo del Canale",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Circular preview
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(TwitchSurfaceElevated)
                        .border(2.dp, TuStyleNeonCyan, CircleShape)
                        .testTag("creator_profile_avatar_preview"),
                    contentAlignment = Alignment.Center
                ) {
                    if (!profileImageUri.isNullOrBlank()) {
                        AsyncImage(
                            model = profileImageUri,
                            contentDescription = "Foto Profilo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Text(avatarEmoji, fontSize = 40.sp)
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TuStylePurple),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("pick_gallery_image_button")
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scegli da Galleria", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { cameraLauncher.launch(null) },
                        colors = ButtonDefaults.buttonColors(containerColor = TwitchSurfaceElevated),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TuStyleNeonCyan),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("snap_camera_photo_button")
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TuStyleNeonCyan, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scatta Foto Ora", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Channel Name & Handle
        TuStyleGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Identità del Canale",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nome Canale Visibile") },
                modifier = Modifier.fillMaxWidth().testTag("channel_name_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TuStyleNeonCyan,
                    unfocusedBorderColor = TwitchBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = handle,
                onValueChange = { input ->
                    val clean = input.trim()
                    handle = if (clean.startsWith("@")) clean else "@$clean"
                },
                label = { Text("Handle Tag (@MaloReplay)") },
                modifier = Modifier.fillMaxWidth().testTag("channel_handle_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TuStylePurple,
                    unfocusedBorderColor = TwitchBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                singleLine = true
            )
        }

        // Bio / Description with character counter
        TuStyleGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bio & Presentazione Canale",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp
                )
                Text(
                    text = "${bio.length}/160",
                    color = if (bio.length > 160) TwitchLiveRed else TwitchTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 160) bio = it },
                placeholder = { Text("Scrivi cosa troveranno gli spettatori sul tuo canale...", color = TwitchTextMuted, fontSize = 12.sp) },
                modifier = Modifier.fillMaxWidth().height(100.dp).testTag("channel_bio_input"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TuStyleNeonCyan,
                    unfocusedBorderColor = TwitchBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        // Channel Badge Selection
        TuStyleGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Badge Personalizzato del Canale (accanto al tuo nome in chat)",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
            )
            Text(
                text = "Seleziona l'icona distintiva che ti identifica come fondatore",
                color = TwitchTextMuted,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                availableBadges.forEach { badge ->
                    val isSelected = badgeEmoji == badge
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) TuStylePurple.copy(alpha = 0.5f) else TwitchSurfaceElevated)
                            .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) TuStyleNeonCyan else TwitchBorder, RoundedCornerShape(8.dp))
                            .clickable { badgeEmoji = badge },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(badge, fontSize = 18.sp)
                    }
                }
            }
        }

        // TASTO SALVA MODIFICHE CON FEEDBACK VERDE
        Button(
            onClick = {
                onSave(name.trim(), handle.trim(), bio.trim(), avatarEmoji, badgeEmoji, profileImageUri)
                saveFeedbackVisible = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = TuStyleNeonCyan),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("save_profile_button")
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Salva Modifiche Canale",
                color = Color.Black,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp
            )
        }

        AnimatedVisibility(
            visible = saveFeedbackVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            TuStyleGlassBox(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                borderWidth = 1.dp,
                borderBrush = Brush.linearGradient(listOf(TuStyleNeonCyan, Color.White))
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✅", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Modifiche applicate istantaneamente in tutta l'applicazione!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. LISTA SUB & ISCRITTI (DATABASE TRASPARENTE)
// ==========================================
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SubscribersListTab(
    channelUsers: List<ChannelUser>,
    onToggleVip: (String) -> Unit,
    onToggleMod: (String) -> Unit,
    onSimulateSub: (ChannelUser) -> Unit,
    onDeleteUser: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterTier by remember { mutableStateOf<Int?>(null) } // null = tutti
    var userToDelete by remember { mutableStateOf<ChannelUser?>(null) }

    val filtered = channelUsers.filter { user ->
        (searchQuery.isBlank() || user.username.contains(searchQuery, ignoreCase = true)) &&
                (selectedFilterTier == null || user.subTier == selectedFilterTier)
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Quick Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Cerca iscritto o sub per nome...", color = TwitchTextMuted, fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TuStyleNeonCyan, modifier = Modifier.size(18.dp)) },
            modifier = Modifier.fillMaxWidth().testTag("subscribers_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TuStyleNeonCyan,
                unfocusedBorderColor = TwitchBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            singleLine = true
        )

        // Tier Filter Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val chips = listOf(
                Pair("Tutti (${channelUsers.size})", null),
                Pair("Tier 1", 1),
                Pair("Tier 2", 2),
                Pair("Tier 3", 3)
            )

            chips.forEach { (label, tier) ->
                val isSelected = selectedFilterTier == tier
                Box(
                    modifier = Modifier
                        .background(if (isSelected) TuStyleNeonCyan else TwitchSurfaceElevated, RoundedCornerShape(20.dp))
                        .border(1.dp, if (isSelected) TuStyleNeonCyan else TwitchBorder, RoundedCornerShape(20.dp))
                        .clickable { selectedFilterTier = tier }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = label,
                        color = if (isSelected) Color.Black else Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                TuStyleGlassCard(modifier = Modifier.fillMaxWidth().testTag("empty_subscribers_card")) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("👥", fontSize = 38.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Nessun utente trovato per \"$searchQuery\"" else "Nessun iscritto o abbonato ancora",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "I follower e gli abbonati appariranno qui automaticamente quando inizieranno a seguirti o abbonarsi durante le tue live!",
                            color = TwitchTextMuted,
                            fontSize = 11.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        } else {
            // Users List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filtered, key = { it.id }) { user ->
                TuStyleGlassBox(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subscriber_row_${user.username}")
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { userToDelete = user }
                        ),
                    shape = RoundedCornerShape(14.dp),
                    borderWidth = 1.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(TwitchSurfaceElevated)
                                    .border(1.dp, if (user.isSub) TwitchGold else TwitchBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.avatarEmoji, fontSize = 20.sp)
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.username,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    if (user.isVip) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("💎 VIP", color = Color(0xFF00F5D4), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                    if (user.isMod) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("🛡️ MOD", color = TuStylePurple, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                                    }
                                }

                                Text(
                                    text = if (user.isSub) "Abbonato Tier ${user.subTier} • ${user.monthsSubbed} mesi • Rinnovo: ${user.renewalDate}"
                                    else "Follower da ${user.followedDaysAgo} giorni",
                                    color = if (user.isSub) TwitchGold else TwitchTextMuted,
                                    fontSize = 10.sp
                                )

                                // Activity progress
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Attività chat: ", color = TwitchTextMuted, fontSize = 9.sp)
                                    LinearProgressIndicator(
                                        progress = { user.chatActivityScore / 100f },
                                        modifier = Modifier.width(60.dp).height(3.dp),
                                        color = if (user.chatActivityScore > 80) TuStyleNeonCyan else Color(0xFFFFB100),
                                        trackColor = TwitchSurfaceElevated
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${user.chatActivityScore}%", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Action Buttons: VIP / Mod / Test Sub
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            IconButton(
                                onClick = { onToggleVip(user.id) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(if (user.isVip) Color(0xFF00F5D4).copy(alpha = 0.2f) else TwitchSurfaceElevated, RoundedCornerShape(8.dp))
                                    .testTag("toggle_vip_${user.username}")
                            ) {
                                Text("💎", fontSize = 14.sp)
                            }

                            IconButton(
                                onClick = { onToggleMod(user.id) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(if (user.isMod) TuStylePurple.copy(alpha = 0.2f) else TwitchSurfaceElevated, RoundedCornerShape(8.dp))
                                    .testTag("toggle_mod_${user.username}")
                            ) {
                                Text("🛡️", fontSize = 14.sp)
                            }

                            // Simulate Sub Message button
                            IconButton(
                                onClick = { onSimulateSub(user) },
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(TwitchGold.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .testTag("simulate_sub_${user.username}")
                            ) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = "Simula Sub", tint = TwitchGold, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = TwitchLiveRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Elimina Utente Canale", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            },
            text = {
                Text(
                    text = "Sei sicuro di voler eliminare ${user.username} dall'elenco della community del canale? Verranno azzerati il suo ruolo e lo storico.",
                    color = TwitchTextMuted,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(user.id)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TwitchLiveRed)
                ) {
                    Text("Elimina", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Annulla", color = Color.White)
                }
            },
            containerColor = TwitchSurfaceElevated,
            shape = RoundedCornerShape(12.dp)
        )
    }
}

// ==========================================
// 3. GESTORE SUB GOAL & OBIETTIVI LIVE
// ==========================================
@Composable
private fun SubGoalsTab(
    subGoals: List<SubGoal>,
    onAddGoal: (String, Int, Int, SubGoalType) -> Unit,
    onRemoveGoal: (String) -> Unit,
    onToggleActive: (String) -> Unit,
    onDeltaProgress: (String, Int) -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var goalToDelete by remember { mutableStateOf<SubGoal?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Action Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Obiettivi & Sub Goal",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Scegli quale barra mostrare in overlay durante lo streaming",
                    color = TwitchTextMuted,
                    fontSize = 10.sp
                )
            }

            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = TuStyleNeonCyan),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("create_new_goal_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Crea Goal", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Active Goal Preview in Tu Style Glass
        val activeGoal = subGoals.firstOrNull { it.isActive }
        TuStyleGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📺 Anteprima Barra In Live (Overlay)",
                    color = TuStyleNeonCyan,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
                if (activeGoal != null) {
                    Text(
                        text = "ATTIVO IN STREAMING",
                        color = TwitchGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 9.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (activeGoal != null) {
                val progress = (activeGoal.currentAmount.toFloat() / activeGoal.targetAmount.coerceAtLeast(1)).coerceIn(0f, 1f)
                val percent = (progress * 100).toInt()

                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${activeGoal.type.emoji} ${activeGoal.title}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${activeGoal.currentAmount} / ${activeGoal.targetAmount} ${activeGoal.type.unit} ($percent%)",
                            color = TwitchGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = TwitchGold,
                        trackColor = TwitchSurfaceElevated
                    )
                }
            } else {
                Text(
                    text = "Nessun obiettivo attivo. Creane uno nuovo!",
                    color = TwitchTextMuted,
                    fontSize = 12.sp
                )
            }
        }

        // Goals List
        Text(
            text = "Tutti gli Obiettivi Creati (${subGoals.size})",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )

        if (subGoals.isEmpty()) {
            TuStyleGlassCard(modifier = Modifier.fillMaxWidth().testTag("empty_sub_goals_card")) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎯", fontSize = 38.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Nessun obiettivo attivo. Creane uno nuovo!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Imposta un traguardo per le Sub, Donazioni o Nuovi Follower. L'avanzamento si aggiornerà automaticamente in tempo reale durante le tue live!",
                        color = TwitchTextMuted,
                        fontSize = 11.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { showCreateDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = TuStyleNeonCyan),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Crea il tuo Primo Goal", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        subGoals.forEach { goal ->
            val progress = (goal.currentAmount.toFloat() / goal.targetAmount.coerceAtLeast(1)).coerceIn(0f, 1f)
            val percent = (progress * 100).toInt()

            TuStyleGlassBox(
                modifier = Modifier.fillMaxWidth().testTag("goal_card_${goal.id}"),
                shape = RoundedCornerShape(14.dp),
                borderWidth = if (goal.isActive) 1.5.dp else 1.dp,
                borderBrush = Brush.linearGradient(
                    if (goal.isActive) listOf(TuStyleNeonCyan, TwitchGold, Color.White)
                    else listOf(TwitchBorder, TwitchBorder)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${goal.type.emoji} ${goal.title}",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                if (goal.isActive) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("• Attivo a schermo", color = TuStyleNeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Text(
                                text = "Tipo: ${goal.type.label} • Progresso: ${goal.currentAmount}/${goal.targetAmount} (${percent}%)",
                                color = TwitchTextMuted,
                                fontSize = 11.sp
                            )
                        }

                        // Toggle Active Checkbox / Pill
                        Box(
                            modifier = Modifier
                                .background(if (goal.isActive) TuStyleNeonCyan else TwitchSurfaceElevated, RoundedCornerShape(8.dp))
                                .border(1.dp, if (goal.isActive) TuStyleNeonCyan else TwitchBorder, RoundedCornerShape(8.dp))
                                .clickable { onToggleActive(goal.id) }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("toggle_goal_active_${goal.id}")
                        ) {
                            Text(
                                text = if (goal.isActive) "✓ Attivo" else "Attiva",
                                color = if (goal.isActive) Color.Black else Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                        color = if (goal.isActive) TuStyleNeonCyan else TuStylePurple,
                        trackColor = TwitchSurfaceElevated
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Progress test buttons & Delete button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Avanzamento rapido:", color = TwitchTextMuted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterVertically))
                            Button(
                                onClick = { onDeltaProgress(goal.id, -1) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TwitchSurfaceElevated),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) { Text("-1", fontSize = 10.sp, color = Color.White) }

                            Button(
                                onClick = { onDeltaProgress(goal.id, +1) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TwitchSurfaceElevated),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) { Text("+1", fontSize = 10.sp, color = Color.White) }

                            Button(
                                onClick = { onDeltaProgress(goal.id, +5) },
                                shape = RoundedCornerShape(6.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = TwitchSurfaceElevated),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.height(26.dp)
                            ) { Text("+5", fontSize = 10.sp, color = TuStyleNeonCyan) }
                        }

                        IconButton(
                            onClick = { goalToDelete = goal },
                            modifier = Modifier.size(28.dp).testTag("delete_goal_${goal.id}")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = TwitchLiveRed, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    // CREATE GOAL DIALOG
    if (showCreateDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newTargetText by remember { mutableStateOf("25") }
        var newInitialText by remember { mutableStateOf("0") }
        var selectedType by remember { mutableStateOf(SubGoalType.NEW_SUBS) }

        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("🎯 Crea Nuovo Sub Goal", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Tipo Obiettivo:", color = TwitchTextMuted, fontSize = 11.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        SubGoalType.values().forEach { t ->
                            val sel = selectedType == t
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(if (sel) TuStyleNeonCyan else TwitchSurfaceElevated, RoundedCornerShape(8.dp))
                                    .border(1.dp, if (sel) TuStyleNeonCyan else TwitchBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedType = t }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${t.emoji} ${t.unit}",
                                    color = if (sel) Color.Black else Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Titolo Obiettivo (es. Nuovo Microfono)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newInitialText,
                            onValueChange = { newInitialText = it },
                            label = { Text("Valore Iniziale") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = newTargetText,
                            onValueChange = { newTargetText = it },
                            label = { Text("Target") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = newTargetText.toIntOrNull() ?: 20
                        val initial = newInitialText.toIntOrNull() ?: 0
                        val title = newTitle.ifBlank { "Nuovo Obiettivo" }
                        onAddGoal(title, target, initial, selectedType)
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TuStyleNeonCyan)
                ) { Text("Salva Goal", color = Color.Black, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Annulla", color = Color.White) }
            },
            containerColor = TwitchSurface
        )
    }

    // CONFIRM DELETE DIALOG
    goalToDelete?.let { goal ->
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            title = { Text("Eliminare questo Obiettivo?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Sei sicuro di voler rimuovere \"${goal.title}\"? L'operazione non può essere annullata.", color = TwitchTextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        onRemoveGoal(goal.id)
                        goalToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TwitchLiveRed)
                ) { Text("Elimina", color = Color.White, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) { Text("Annulla", color = Color.White) }
            },
            containerColor = TwitchSurface
        )
    }
}

// ==========================================
// 4. DASHBOARD ANALYTICS AVANZATA (100% DATI REALI)
// ==========================================
@Composable
private fun AnalyticsDashboardTab(
    totalEarnings: Double,
    totalSeconds: Long,
    totalSubs: Int,
    liveState: com.example.viewmodel.LiveUiState,
    postStreamSummary: com.example.data.StreamSessionSummary?,
    channelName: String,
    viewerHistoryPoints: List<Float> = emptyList()
) {
    val context = LocalContext.current

    // Hours streamed calculation - starts at 0.0h if no live performed
    val hoursStreamed = (totalSeconds / 3600.0)
    val hasStreamed = totalSeconds > 0L || liveState.durationSeconds > 0L

    val avgViewers = if (liveState.durationSeconds > 0 && liveState.viewersCount > 0) {
        ((liveState.viewersCount + liveState.peakViewers) / 2)
    } else if (postStreamSummary != null && postStreamSummary.peakViewers > 0) {
        (postStreamSummary.peakViewers / 2)
    } else 0

    val peakViewers = if (liveState.peakViewers > 0) {
        liveState.peakViewers
    } else {
        postStreamSummary?.peakViewers ?: 0
    }

    val chatActivityPercent = if (hasStreamed && avgViewers > 0 && liveState.chatMessages.isNotEmpty()) {
        ((liveState.chatMessages.size.toFloat() / avgViewers) * 100).toInt().coerceIn(1, 100)
    } else if (hasStreamed && liveState.chatMessages.isNotEmpty()) {
        100
    } else {
        0
    }

    // Financial breakdown: Net = Gross - Fees (0.00 if no streaming / earnings)
    val grossTotal = totalEarnings
    val fixedZyroFee = if (hasStreamed && grossTotal > 0.0) 5.50 else 0.00
    val netTotal = (grossTotal - fixedZyroFee).coerceAtLeast(0.0)

    val aiReportText = if (postStreamSummary != null) {
        postStreamSummary.aiCoachFeedback
    } else if (hasStreamed) {
        "📊 Report Sessione Live: Il canale di $channelName ha registrato un picco di $peakViewers spettatori e $totalSubs sub totali. Continua a trasmettere per sbloccare consigli e strategie avanzate dell'IA!"
    } else {
        "📊 Nessun dato registrato. Avvia la tua prima diretta per sbloccare l'analisi strategica, il feedback e le statistiche in tempo reale dell'IA."
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Line Chart: Spettatori Medi & Picco Massimo (Native Canvas)
        TuStyleGlassCard(modifier = Modifier.fillMaxWidth().testTag("viewers_analytics_card")) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Andamento Spettatori Live",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Media: $avgViewers • Picco Max: $peakViewers spettatori",
                        color = TuStyleNeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Box(
                    modifier = Modifier
                        .background(TuStylePurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .border(1.dp, TuStylePurple, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("60 FPS", color = TuStylePurple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Native Smooth Canvas Line Chart with real points or Empty State
            if (viewerHistoryPoints.size >= 2) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .testTag("analytics_canvas_chart")
                ) {
                    val maxVal = viewerHistoryPoints.maxOrNull()?.coerceAtLeast(10f) ?: 10f
                    val minVal = 0f
                    val widthStep = size.width / (viewerHistoryPoints.size - 1)

                    val path = Path()
                    val fillPath = Path()

                    viewerHistoryPoints.forEachIndexed { i, pt ->
                        val x = i * widthStep
                        val y = size.height - ((pt - minVal) / (maxVal - minVal) * size.height)
                        if (i == 0) {
                            path.moveTo(x, y)
                            fillPath.moveTo(x, size.height)
                            fillPath.lineTo(x, y)
                        } else {
                            val prevX = (i - 1) * widthStep
                            val prevY = size.height - ((viewerHistoryPoints[i - 1] - minVal) / (maxVal - minVal) * size.height)
                            val cx = (prevX + x) / 2
                            path.cubicTo(cx, prevY, cx, y, x, y)
                            fillPath.cubicTo(cx, prevY, cx, y, x, y)
                        }
                    }
                    fillPath.lineTo(size.width, size.height)
                    fillPath.close()

                    // Gradient background under curve
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                TuStyleNeonCyan.copy(alpha = 0.35f),
                                TuStylePurple.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )

                    // Neon Stroke
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(TuStylePurple, TuStyleNeonCyan, Color.White)
                        ),
                        style = Stroke(width = 3.dp.toPx())
                    )

                    // Peak indicator dot
                    val peakIndex = viewerHistoryPoints.indexOf(viewerHistoryPoints.maxOrNull())
                    if (peakIndex >= 0) {
                        val peakX = peakIndex * widthStep
                        val peakY = size.height - ((viewerHistoryPoints[peakIndex] - minVal) / (maxVal - minVal) * size.height)
                        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = Offset(peakX, peakY))
                        drawCircle(color = TuStyleNeonCyan, radius = 3.dp.toPx(), center = Offset(peakX, peakY))
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(TwitchSurfaceElevated, RoundedCornerShape(10.dp))
                        .border(1.dp, TwitchBorder, RoundedCornerShape(10.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📊", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Nessun dato di streaming registrato",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Avvia una diretta per tracciare gli spettatori in tempo reale a 60 FPS",
                            color = TwitchTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }

        // Summary Metric Cards
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Ore Trasmesse
            TuStyleGlassCard(modifier = Modifier.weight(1f)) {
                Text("⏱️ Tempo in Live", color = TwitchTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                val timeFormatted = if (hoursStreamed >= 1.0) "%.1f h".format(hoursStreamed) else "${totalSeconds / 60} min"
                Text(
                    text = timeFormatted,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text("Nel mese corrente", color = TuStyleNeonCyan, fontSize = 9.sp)
            }

            // Chat Engagement
            TuStyleGlassCard(modifier = Modifier.weight(1f)) {
                Text("💬 Coinvolgimento", color = TwitchTextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$chatActivityPercent%",
                    color = Color(0xFF00F5D4),
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp
                )
                Text("Viewer attivi in chat", color = TwitchTextMuted, fontSize = 9.sp)
            }
        }

        // Financial Breakdown (Lordo, Tasse Zyro -5.50€, Netto)
        TuStyleGlassBox(
            modifier = Modifier.fillMaxWidth().testTag("financial_breakdown_card"),
            shape = RoundedCornerShape(16.dp),
            borderWidth = 1.2.dp,
            borderBrush = Brush.linearGradient(listOf(TwitchGold, Color(0xFF00F5D4), Color.White))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💰 Resoconto Finanziario Dettagliato",
                        color = TwitchGold,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.sp
                    )
                    Text("Netto Incassato", color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("• Lordo Sub & Donazioni:", color = Color.White, fontSize = 12.sp)
                    Text("+%.2f €".format(grossTotal), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("• Commissioni / Tasse Zyro (Fisse):", color = TwitchLiveRed, fontSize = 12.sp)
                    Text("-%.2f € per live".format(fixedZyroFee), color = TwitchLiveRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.15f)))
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Totale Guadagno Netto:", color = Color(0xFF00F5D4), fontWeight = FontWeight.Black, fontSize = 14.sp)
                    Text(
                        text = "%.2f €".format(netTotal),
                        color = Color(0xFF00F5D4),
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    )
                }
            }
        }

        // Export & Copia Report IA
        TuStyleGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🤖 Analisi Post-Live IA", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gemini", color = TuStyleNeonCyan, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }

                // Copy to Clipboard button
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Report IA TwitchFlex", aiReportText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "📋 Report IA copiato negli appunti!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TuStylePurple),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(30.dp).testTag("copy_ai_report_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copia Report", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = aiReportText,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}
