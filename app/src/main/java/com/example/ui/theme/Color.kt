package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Twitch Dark Theme Foundation
val TwitchBlack = Color(0xFF0E0E10)
val TwitchSurface = Color(0xFF18181B)
val TwitchSurfaceElevated = Color(0xFF1F1F23)
val TwitchBorder = Color(0xFF2E2E35)
val TwitchText = Color(0xFFEFEFF1)
val TwitchTextMuted = Color(0xFFADADB8)
val TwitchLiveRed = Color(0xFFEB0400)
val TwitchGold = Color(0xFFFFD700)
val TwitchPrimeBlue = Color(0xFF00B5D8)

// Accent Colors
val AccentElectricPurple = Color(0xFF9146FF)
val AccentCyanNeon = Color(0xFF00F5D4)
val AccentEmeraldGreen = Color(0xFF00F076)
val AccentCrimsonRed = Color(0xFFFF2A6D)
val AccentKickGreen = Color(0xFF53FC18)
val AccentTrovoTeal = Color(0xFF00E676)

enum class ThemeAccent(val displayName: String, val color: Color, val hex: String) {
    PURPLE("Viola Elettrico", AccentElectricPurple, "#9146FF"),
    CYAN("Cyan Neon", AccentCyanNeon, "#00F5D4"),
    GREEN("Emerald Green", AccentEmeraldGreen, "#00F076"),
    RED("Crimson Red", AccentCrimsonRed, "#FF2A6D"),
    KICK("KickFlex Neon", AccentKickGreen, "#53FC18"),
    TROVO("TrovoVerse Teal", AccentTrovoTeal, "#00E676")
}
