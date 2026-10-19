package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChannelManagementScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.CreatorDashboardScreen
import com.example.ui.screens.LiveStreamScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.TwitchBlack
import com.example.ui.theme.TwitchBorder
import com.example.ui.theme.TwitchFintoTheme
import com.example.ui.theme.TwitchSurface
import com.example.ui.theme.TwitchTextMuted
import com.example.viewmodel.StreamViewModel

enum class MainTab(val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    LIVE("Live", Icons.Default.Videocam),
    HUB("Hub Canale", Icons.Default.AccountBox),
    TYCOON("Gestionale", Icons.Default.AttachMoney),
    COMMUNITY("Social & P2P", Icons.Default.People),
    SETTINGS("Impostazioni", Icons.Default.Settings)
}

@Composable
fun MainApp(viewModel: StreamViewModel) {
    val context = LocalContext.current
    val userProfile by viewModel.userProfile.collectAsState()
    val themeAccent by viewModel.themeAccent.collectAsState()
    val liveState by viewModel.liveState.collectAsState()

    var selectedTabIndex by remember { mutableIntStateOf(0) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasAudioPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[Manifest.permission.CAMERA] ?: hasCameraPermission
        hasAudioPermission = permissions[Manifest.permission.RECORD_AUDIO] ?: hasAudioPermission
    }

    LaunchedEffect(Unit) {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    TwitchFintoTheme(accent = themeAccent) {
        if (!userProfile.isRegistered) {
            AuthScreen(
                onLoginSuccess = { channel, pin, birth, avatar, bio ->
                    viewModel.registerOrLogin(channel, pin, birth, avatar, bio)
                }
            )
        } else {
            Scaffold(
                bottomBar = {
                    if (!liveState.isLive) {
                        NavigationBar(
                            containerColor = TwitchSurface,
                            modifier = Modifier
                                .border(1.dp, TwitchBorder)
                                .testTag("main_bottom_nav")
                        ) {
                            MainTab.values().forEachIndexed { index, tab ->
                                val isSelected = selectedTabIndex == index
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { selectedTabIndex = index },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.label,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = TwitchTextMuted,
                                        unselectedTextColor = TwitchTextMuted,
                                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                                )
                            }
                        }
                    }
                },
                containerColor = TwitchBlack
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    when (MainTab.values()[selectedTabIndex]) {
                        MainTab.LIVE -> LiveStreamScreen(
                            viewModel = viewModel,
                            hasCameraPermission = hasCameraPermission,
                            hasAudioPermission = hasAudioPermission,
                            onOpenHub = { selectedTabIndex = MainTab.HUB.ordinal }
                        )
                        MainTab.HUB -> ChannelManagementScreen(
                            viewModel = viewModel
                        )
                        MainTab.TYCOON -> CreatorDashboardScreen(
                            viewModel = viewModel
                        )
                        MainTab.COMMUNITY -> CommunityScreen(
                            viewModel = viewModel
                        )
                        MainTab.SETTINGS -> SettingsScreen(
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
