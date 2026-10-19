package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// Creator Color Palette for "Tu Style"
val TuStylePurple = Color(0xFF9146FF) // Viola Creator
val TuStyleDeepViolet = Color(0xFF5C16C5)
val TuStyleNeonCyan = Color(0xFF00F5D4) // Azzurro Neon
val TuStyleElectricBlue = Color(0xFF00B5D8)
val TuStyleNeonGreen = Color(0xFF10B981) // Verde Donazioni
val TuStyleDarkGreen = Color(0xFF047857)

/**
 * Custom "TuStyleButton" component for creator HUD & live actions.
 *
 * Features:
 * - Creator-inspired vibrant gradients (Viola Creator #9146FF & Azzurro Neon #00F5D4).
 * - Native Canvas drawing for outer neon glow and internal specular/gloss sheen without GPU overhead.
 * - Ultra-smooth touch scale feedback animation with spring physics (60 FPS, battery efficient).
 * - Optional free Drag and Drop capability to reposition HUD buttons anywhere on screen.
 */
@Composable
fun TuStyleButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradientColors: List<Color> = listOf(TuStylePurple, TuStyleNeonCyan),
    glowColor: Color = TuStyleNeonCyan,
    textColor: Color = Color.White,
    width: Dp = 46.dp,
    height: Dp = 46.dp,
    testTag: String = "",
    enableDrag: Boolean = true,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
    onDragEnd: ((Float, Float) -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth tactile scale spring animation on touch
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tu_button_scale"
    )

    // Free drag and drop coordinates
    var offsetX by remember { mutableFloatStateOf(initialOffsetX) }
    var offsetY by remember { mutableFloatStateOf(initialOffsetY) }

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
            .then(
                if (enableDrag) {
                    Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { change, dragAmount ->
                                change.consume()
                                offsetX += dragAmount.x
                                offsetY += dragAmount.y
                            },
                            onDragEnd = {
                                onDragEnd?.invoke(offsetX, offsetY)
                            }
                        )
                    }
                } else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
                .testTag(testTag)
        ) {
            // Button Body with Canvas Glow and Sheen
            Box(
                modifier = Modifier
                    .size(width = width, height = height)
                    .clip(RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                // Native Canvas rendering for high-performance 60fps outer glow & inner sheen
                Canvas(modifier = Modifier.matchParentSize()) {
                    val corner = CornerRadius(14.dp.toPx(), 14.dp.toPx())

                    // 1. Subtle Outer Neon Ambient Glow
                    drawRoundRect(
                        color = glowColor.copy(alpha = if (isPressed) 0.45f else 0.25f),
                        topLeft = Offset(-1.dp.toPx(), -1.dp.toPx()),
                        size = Size(size.width + 2.dp.toPx(), size.height + 2.dp.toPx()),
                        cornerRadius = corner
                    )

                    // 2. Main Body Creator Gradient (Viola Creator to Azzurro Neon)
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height)
                        ),
                        topLeft = Offset.Zero,
                        size = size,
                        cornerRadius = corner
                    )

                    // 3. Top Inner Specular Sheen (Liquid Gloss)
                    val sheenH = size.height * 0.42f
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.40f),
                                Color.White.copy(alpha = 0.05f),
                                Color.Transparent
                            ),
                            startY = 0f,
                            endY = sheenH
                        ),
                        topLeft = Offset(1.5f, 1.5f),
                        size = Size(size.width - 3f, sheenH),
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )

                    // 4. Refractive Border Stroke
                    drawRoundRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.8f),
                                TuStyleNeonCyan.copy(alpha = 0.6f),
                                TuStylePurple.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        ),
                        topLeft = Offset.Zero,
                        size = size,
                        cornerRadius = corner,
                        style = Stroke(width = 1.2.dp.toPx())
                    )
                }

                // Centered Icon
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Compact readable label below button
            if (label.isNotBlank()) {
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = label,
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
