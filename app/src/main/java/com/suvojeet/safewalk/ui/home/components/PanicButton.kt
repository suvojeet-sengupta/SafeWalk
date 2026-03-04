package com.suvojeet.safewalk.ui.home.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.suvojeet.safewalk.ui.theme.PanicRed
import com.suvojeet.safewalk.ui.theme.PanicRedDark
import com.suvojeet.safewalk.ui.theme.PanicRedLight

@Composable
fun PanicButton(
    isPanicActive: Boolean,
    onPanicTrigger: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "panic_pulse")

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isPanicActive) 1.15f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPanicActive) 600 else 1200,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_scale",
    )

    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = if (isPanicActive) 600 else 1200,
                easing = FastOutSlowInEasing,
            ),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glow_alpha",
    )

    var isPressed by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        // Outer glow ring
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(pulseScale * 1.2f)
                .clip(CircleShape)
                .background(
                    PanicRed.copy(alpha = glowAlpha * 0.3f),
                    CircleShape,
                ),
        )

        // Middle ring
        Box(
            modifier = Modifier
                .size(180.dp)
                .scale(pulseScale * 1.1f)
                .clip(CircleShape)
                .background(
                    PanicRed.copy(alpha = glowAlpha * 0.5f),
                    CircleShape,
                ),
        )

        // Main button
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(160.dp)
                .scale(if (isPressed) 0.92f else pulseScale)
                .shadow(
                    elevation = if (isPanicActive) 24.dp else 16.dp,
                    shape = CircleShape,
                    ambientColor = PanicRed,
                    spotColor = PanicRed,
                )
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            PanicRedLight,
                            PanicRed,
                            PanicRedDark,
                        ),
                    ),
                    shape = CircleShape,
                )
                .border(
                    width = 3.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            PanicRedLight.copy(alpha = 0.8f),
                            PanicRed.copy(alpha = 0.4f),
                        ),
                    ),
                    shape = CircleShape,
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        },
                        onLongPress = {
                            onPanicTrigger()
                        },
                    )
                },
        ) {
            Text(
                text = if (isPanicActive) "🚨" else "SOS",
                fontSize = if (isPanicActive) 40.sp else 32.sp,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onError,
                letterSpacing = 4.sp,
            )
        }
    }
}
