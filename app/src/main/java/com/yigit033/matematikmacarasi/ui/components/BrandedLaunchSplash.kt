package com.yigit033.matematikmacarasi.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.awaitPointerEventScope
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R

/**
 * Exact reference-derived illustrated cold-launch overlay. The embedded static
 * loading text/bar was removed from WebP, so Compose owns the actual animation.
 * Never blocks billing or internet and never claims a fabricated completion %.
 */
@Composable
fun BrandedLaunchSplash() {
    val pulse = rememberInfiniteTransition(label = "Launch gold glimmer")
    val x by pulse.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Indeterminate loading glimmer"
    )
    Box(
        Modifier.fillMaxSize()
            // While the illustrated launch layer is visible, prevent a tap
            // from accidentally activating a home button underneath it.
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent().changes.forEach { it.consume() }
                    }
                }
            }
            .background(Color(0xFF072447)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.mm_magic_splash_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 72.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Yükleniyor...",
                color = Color(0xFFFFF0CA),
                fontSize = 25.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            val pill = RoundedCornerShape(50)
            Box(
                Modifier.fillMaxWidth(0.69f).height(17.dp)
                    .clip(pill)
                    .background(Color(0xFF102E58))
                    .border(1.5.dp, Color(0xFFFFCD6F), pill)
                    .padding(3.dp)
            ) {
                // Indeterminate light sweep: not a fake 75% complete.
                Box(
                    Modifier.fillMaxWidth(0.31f).fillMaxHeight()
                        .offset(x = ((x - 0.3f) * 230f).dp)
                        .clip(pill)
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0x00FFE58F),
                                    Color(0xFFFFF6CA),
                                    Color(0xFFFFC554),
                                    Color(0x00FFDA80)
                                )
                            )
                        )
                )
            }
        }
    }
}
