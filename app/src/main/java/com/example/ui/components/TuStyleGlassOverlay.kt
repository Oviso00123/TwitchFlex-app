package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.ui.theme.TwitchGold
import com.example.ui.theme.TwitchLiveRed
import com.example.ui.theme.TwitchTextMuted

/**
 * Reusable "TU Style Glass" (Liquid Glass) Container.
 *
 * Graphic style:
 * - Rounded corners
 * - Dark semi-transparent background with frosted glass simulation
 * - Luminous refractive iridescent border (Electric purple, Neon cyan, White specular highlight)
 * - Optimized for 60 FPS fluid rendering without GPU overhead
 */
@Composable
fun TuStyleGlassBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    borderWidth: Dp = 1.2.dp,
    borderBrush: Brush = Brush.linearGradient(
        colors = listOf(
            Color(0xCC9146FF), // Electric Purple glow
            Color(0x9900F5D4), // Cyan Neon refraction
            Color(0x66FFFFFF), // Specular white edge
            Color(0xBB772CE8), // Deep violet
            Color(0x889146FF)
        )
    ),
    backgroundBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xD90E0E14), // Dark liquid glass top
            Color(0xBF141420), // Translucent center
            Color(0xEA0A0A10)  // Deep shadow bottom
        )
    ),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .border(borderWidth, borderBrush, shape)
            .drawBehind {
                // Top inner specular glass sheen
                val sheenHeight = size.height * 0.22f
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0x22FFFFFF),
                            Color.Transparent
                        ),
                        startY = 0f,
                        endY = sheenHeight
                    ),
                    size = size.copy(height = sheenHeight)
                )
            },
        content = content
    )
}

/**
 * Reusable "TU Style Glass" Card with internal Column layout.
 */
@Composable
fun TuStyleGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    borderWidth: Dp = 1.2.dp,
    contentPadding: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    TuStyleGlassBox(
        modifier = modifier,
        shape = shape,
        borderWidth = borderWidth
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

/**
 * Reusable Glass Badge for notifications, alerts or goal progress.
 */
@Composable
fun TuStyleGlassBadge(
    text: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    accentColor: Color = Color(0xFF00F5D4),
    onClick: (() -> Unit)? = null
) {
    val baseModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    TuStyleGlassBox(
        modifier = baseModifier,
        shape = RoundedCornerShape(12.dp),
        borderWidth = 1.dp,
        borderBrush = Brush.linearGradient(
            listOf(
                accentColor.copy(alpha = 0.8f),
                Color(0x40FFFFFF),
                accentColor.copy(alpha = 0.4f)
            )
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            icon?.invoke()
            Text(
                text = text,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Landscape Overlay in "TU Style Glass" (Liquid Glass) style.
 *
 * Activated with a quick double tap on screen when camera is full-screen landscape.
 * Contains:
 * 1. Title "Modalità Chat"
 * 2. Live viewers counter (updated in real-time)
 * 3. Button for Sub & Earned Money with interactive details
 */
@Composable
fun LandscapeTuStyleGlassOverlay(
    visible: Boolean,
    viewersCount: Int,
    subCount: Int,
    earnedMoneyEuros: Double,
    chatMessages: List<ChatMessage>,
    onClose: () -> Unit,
    onSubMoneyClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220, easing = FastOutSlowInEasing)) +
                scaleIn(initialScale = 0.94f, animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(180)) +
                scaleOut(targetScale = 0.94f, animationSpec = tween(180)),
        modifier = modifier
    ) {
        // Pulsing live indicator dot
        val infiniteTransition = rememberInfiniteTransition(label = "pulse_live")
        val liveAlpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(700),
                repeatMode = RepeatMode.Reverse
            ),
            label = "live_alpha"
        )

        TuStyleGlassBox(
            modifier = Modifier
                .width(360.dp)
                .padding(14.dp)
                .testTag("landscape_tu_style_glass_overlay"),
            shape = RoundedCornerShape(22.dp),
            borderWidth = 1.5.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .background(TwitchLiveRed.copy(alpha = liveAlpha), CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Modalità Chat",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.3.sp
                        )
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(32.dp)
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .testTag("close_glass_overlay_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Chiudi",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Metric Cards Row: Viewers & Sub/Money
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // 1. Live Viewers Counter
                    TuStyleGlassBox(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("glass_viewers_counter"),
                        shape = RoundedCornerShape(14.dp),
                        borderWidth = 1.dp,
                        borderBrush = Brush.linearGradient(
                            listOf(Color(0xFF00F5D4), Color(0x30FFFFFF))
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Group,
                                    contentDescription = null,
                                    tint = Color(0xFF00F5D4),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "In Live",
                                    color = TwitchTextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "%,d".format(viewersCount),
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    // 2. Sub & Earned Money Interactive Button
                    TuStyleGlassBox(
                        modifier = Modifier
                            .weight(1.3f)
                            .clickable { onSubMoneyClick() }
                            .testTag("glass_sub_money_button"),
                        shape = RoundedCornerShape(14.dp),
                        borderWidth = 1.2.dp,
                        borderBrush = Brush.linearGradient(
                            listOf(TwitchGold, Color(0xFF9146FF), Color(0x60FFFFFF))
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = TwitchGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Sub & Soldi",
                                    color = TwitchGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "$subCount Sub",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("•", color = TwitchTextMuted, fontSize = 12.sp)
                                Text(
                                    text = "€ %.2f".format(earnedMoneyEuros),
                                    color = Color(0xFF00F5D4),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Chat Messages Feed inside Glass Overlay
                Text(
                    text = "Messaggi recenti in tempo reale",
                    color = TwitchTextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Surface(
                    color = Color.Black.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                ) {
                    val recent = chatMessages.takeLast(5)
                    Column(
                        modifier = Modifier
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (recent.isEmpty()) {
                            Text(
                                text = "Nessun messaggio in chat ancora...",
                                color = TwitchTextMuted,
                                fontSize = 11.sp
                            )
                        } else {
                            recent.forEach { msg ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    val uColor = try {
                                        Color(android.graphics.Color.parseColor(msg.colorHex))
                                    } catch (_: Exception) {
                                        Color.White
                                    }
                                    Text(
                                        text = "${msg.username}: ",
                                        color = uColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = msg.message,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 11.sp,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
