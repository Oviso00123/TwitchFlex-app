package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun TwitchFintoTheme(
    accent: ThemeAccent = ThemeAccent.PURPLE,
    content: @Composable () -> Unit
) {
    val darkColors = darkColorScheme(
        primary = accent.color,
        onPrimary = Color.White,
        primaryContainer = accent.color.copy(alpha = 0.2f),
        onPrimaryContainer = accent.color,
        secondary = accent.color.copy(alpha = 0.8f),
        onSecondary = Color.White,
        tertiary = TwitchPrimeBlue,
        background = TwitchBlack,
        onBackground = TwitchText,
        surface = TwitchSurface,
        onSurface = TwitchText,
        surfaceVariant = TwitchSurfaceElevated,
        onSurfaceVariant = TwitchTextMuted,
        outline = TwitchBorder,
        error = TwitchLiveRed
    )

    MaterialTheme(
        colorScheme = darkColors,
        typography = Typography,
        content = content
    )
}
