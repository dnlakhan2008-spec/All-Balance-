package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GlassPrimaryAccent

/**
 * Animated moving blob background for the fluid glass look.
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    // Infinite animation to drift the liquid blobs slowly
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidBlobs")
    
    val tx1 by infiniteTransition.animateFloat(
        initialValue = -100f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob1X"
    )
    val ty1 by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 150f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob1Y"
    )
    val ty2 by infiniteTransition.animateFloat(
        initialValue = 150f,
        targetValue = -50f,
        animationSpec = infiniteRepeatable(
            animation = tween(18000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Blob2Y"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(com.example.ui.theme.DeepDarkBack) // Obsidian base from Vibrant Palette
            .drawBehind {
                val width = size.width
                val height = size.height

                // Draw Liquid Glow Orb 1 (Top-Right / Vibrant Cyan 500)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x3B06B6D4), Color(0x0006B6D4)),
                        center = Offset(width * 0.82f + tx1, height * 0.18f + ty1),
                        radius = width * 0.7f
                    ),
                    center = Offset(width * 0.82f + tx1, height * 0.18f + ty1),
                    radius = width * 0.7f
                )

                // Draw Liquid Glow Orb 2 (Bottom-Left / Vibrant Purple 500)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x338B5CF6), Color(0x008B5CF6)),
                        center = Offset(width * 0.08f, height * 0.82f + ty2),
                        radius = width * 0.75f
                    ),
                    center = Offset(width * 0.08f, height * 0.82f + ty2),
                    radius = width * 0.75f
                )

                // Draw a subtle secondary fuchsia orb centered for premium depth
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x1AD946EF), Color(0x00D946EF)),
                        center = Offset(width * 0.5f, height * 0.5f),
                        radius = width * 0.5f
                    ),
                    center = Offset(width * 0.5f, height * 0.5f),
                    radius = width * 0.5f
                )
            }
    ) {
        content()
    }
}

/**
 * Beautiful liquid glass styled button with reactive animation states,
 * glass reflection paths, and glowing borders.
 */
@Composable
fun LiquidGlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: List<Color> = listOf(Color(0x33FFFFFF), Color(0x0AFFFFFF)),
    borderColors: List<Color> = listOf(Color(0x4DFFFFFF), Color(0x13FFFFFF)),
    testTag: String = "liquid_glass_button",
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth physics-based spring scale and elevation
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "ButtonScale"
    )

    val glowAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.8f else 0.4f,
        animationSpec = tween(150),
        label = "BorderGlow"
    )

    // Inner refraction swipe overlay animation
    val infiniteTransition = rememberInfiniteTransition(label = "GlossFlow")
    val glossOffset by infiniteTransition.animateFloat(
        initialValue = -250f,
        targetValue = 600f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "GlossX"
    )

    val shape = RoundedCornerShape(16.dp)

    Box(
        modifier = modifier
            .testTag(testTag)
            .scale(scale)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = if (enabled) colors else listOf(
                        Color(0x11FFFFFF),
                        Color(0x03FFFFFF)
                    )
                )
            )
            .border(
                BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = borderColors.map { it.copy(alpha = it.alpha * if (enabled) glowAlpha else 0.5f) }
                    )
                ),
                shape = shape
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            )
            // Custom drawing of visual liquid gloss layer
            .drawBehind {
                // Wave/refraction shape cut across top-left to middle-right
                val glossPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width * 0.7f, 0f)
                    quadraticTo(
                        size.width * 0.4f, size.height * 0.5f,
                        0f, size.height * 0.8f
                    )
                    close()
                }
                drawPath(
                    path = glossPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x10FFFFFF), Color(0x00FFFFFF))
                    )
                )

                // Swipe of shimmer glass highlight
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0x25FFFFFF),
                            Color.Transparent
                        ),
                        start = Offset(glossOffset, 0f),
                        end = Offset(glossOffset + 120f, size.height)
                    ),
                    size = Size(size.width, size.height)
                )
            }
            .padding(vertical = 14.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            content()
        }
    }
}

/**
 * Circle-shaped iconic glass button.
 */
@Composable
fun LiquidGlassIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    testTag: String = "glass_icon_button"
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.93f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioHighBouncy),
        label = "IconButtonScale"
    )

    Box(
        modifier = modifier
            .testTag(testTag)
            .size(48.dp)
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(Color(0x2BFFFFFF), Color(0x0AFFFFFF)),
                    radius = 80f
                )
            )
            .border(
                BorderStroke(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x40FFFFFF), Color(0x0AFFFFFF))
                    )
                ),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

/**
 * Beautiful Glassmorphic container mimicking premium plastic/glass elements.
 */
@Composable
fun LiquidGlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    borderColors: List<Color> = listOf(Color(0x40FFFFFF), Color(0x06FFFFFF)),
    backgroundBrush: Brush = Brush.verticalGradient(
        colors = listOf(Color(0x1FFFFFFF), Color(0x0BFFFFFF))
    ),
    testTag: String = "liquid_glass_card",
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .clip(shape)
            .background(backgroundBrush)
            .border(
                BorderStroke(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(colors = borderColors)
                ),
                shape = shape
            )
            // Liquid shine gloss overlay (static card curve)
            .drawBehind {
                val glassPath = Path().apply {
                    moveTo(0f, 0f)
                    lineTo(size.width, 0f)
                    quadraticTo(
                        size.width * 0.5f, size.height * 0.4f,
                        0f, size.height * 0.6f
                    )
                    close()
                }
                drawPath(
                    path = glassPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x0EFFFFFF), Color(0x00FFFFFF))
                    )
                )
            }
    ) {
        content()
    }
}

/**
 * Styled text field matching glass theme. Meets 48dp touch targets easily.
 */
@Composable
fun LiquidGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    testTag: String = "glass_text_field"
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .testTag(testTag)
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                BorderStroke(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0x33FFFFFF), Color(0x0AFFFFFF))
                    )
                ),
                shape = RoundedCornerShape(16.dp)
            ),
        label = if (label.isNotEmpty()) { { Text(label, color = Color(0xAAFFFFFF)) } } else null,
        placeholder = if (placeholder.isNotEmpty()) { { Text(placeholder, color = Color(0x66FFFFFF)) } } else null,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = singleLine,
        visualTransformation = visualTransformation,
        colors = TextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color(0xFFDDDDDD),
            focusedContainerColor = Color(0x0EFFFFFF),
            unfocusedContainerColor = Color(0x05FFFFFF),
            disabledContainerColor = Color.Transparent,
            cursorColor = GlassPrimaryAccent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}
