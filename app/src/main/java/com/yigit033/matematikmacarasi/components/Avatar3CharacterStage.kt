package com.yigit033.matematikmacarasi.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.models.Avatar

/** One Lottie character identity, with three selectable stage looks.
 * Decorations change for real; no non-existent costumes are advertised.
 */
@Composable
fun Avatar3CharacterStage(
    avatar: Avatar,
    lookId: Int,
    accessory: Int,
    effect: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    animate: Boolean = false
) {
    val palette = when (lookId) {
        1 -> listOf(Color(0xFF13839D), Color(0xFF155C8D), Color(0xFF111B50))
        2 -> listOf(Color(0xFFA66A21), Color(0xFF593989), Color(0xFF19275A))
        else -> listOf(Color(0xFF266C94), Color(0xFF20456D), Color(0xFF112641))
    }
    val accent = when (lookId) {
        1 -> Color(0xFF96FFE7)
        2 -> Color(0xFFFFDB84)
        else -> Color(0xFF91DFFF)
    }
    val pulse = if (animate && effect != 0) {
        val transition = rememberInfiniteTransition()
        transition.animateFloat(
            initialValue = 0.35f,
            targetValue = 0.9f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1550),
                repeatMode = RepeatMode.Reverse
            )
        ).value
    } else 0.45f

    Box(
        modifier = modifier.size(size)
            .clip(CircleShape)
            .background(Brush.radialGradient(palette))
            .border(2.dp, accent.copy(alpha = 0.85f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (lookId != 0 || effect != 0) {
            Canvas(Modifier.fillMaxSize()) {
                val radius = this.size.minDimension / 2f
                val light = if (effect == 2) Color(0xFFFFE09B) else Color(0xFFB4EEFF)
                for (i in 0 until 7) {
                    val theta = (i * 6.2831853f / 7f)
                    val distance = radius * 0.74f
                    drawCircle(
                        color = light.copy(alpha = if (effect == 0) 0.34f else pulse),
                        radius = radius * (if (lookId == 2) 0.055f else 0.034f),
                        center = Offset(
                            center.x + kotlin.math.cos(theta) * distance,
                            center.y + kotlin.math.sin(theta) * distance
                        )
                    )
                }
                if (effect != 0) {
                    drawCircle(
                        color = light.copy(alpha = pulse * 0.65f),
                        radius = radius * 0.92f,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx())
                    )
                }
            }
        }
        AnimatedAvatarView(
            avatar = avatar,
            size = size * 0.79f,
            animate = animate,
            modifier = Modifier.size(size * 0.79f),
            contentDescription = avatar.name
        )
        if (accessory != 0) {
            Text(
                text = if (accessory == 1) "✦" else "♛",
                modifier = Modifier.align(Alignment.TopEnd)
                    .offset(x = (-4).dp, y = 3.dp),
                color = if (accessory == 1) Color(0xFF97FCE7) else Color(0xFFFFDB84),
                fontSize = (size.value / 5.4f).coerceIn(12f, 25f).sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}