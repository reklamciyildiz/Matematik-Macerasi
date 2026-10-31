package com.yigit033.matematikmacarasi.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.yigit033.matematikmacarasi.R
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ColorfulPuzzleIcon(
    modifier: Modifier = Modifier
) {
    // Puzzle parçaları için animasyon değerleri
    val infiniteTransition = rememberInfiniteTransition(label = "puzzleAnimation")
    
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutBack),
            repeatMode = RepeatMode.Reverse
        ),
        label = "puzzle1Scale"
    )
    
    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutBack, delayMillis = 200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "puzzle2Scale"
    )
    
    val scale3 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutBack, delayMillis = 400),
            repeatMode = RepeatMode.Reverse
        ),
        label = "puzzle3Scale"
    )
    
    Box(
        modifier = modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Yapboz parçası 1 (Sol üst)
        Icon(
            painter = painterResource(id = R.drawable.ic_shapes),
            contentDescription = "Puzzle Part 1",
            tint = Color(0xFFE91E63), // Pembe
            modifier = Modifier
                .size(28.dp * scale1)
                .offset((-8).dp, (-8).dp)
        )
        
        // Yapboz parçası 2 (Sağ üst)
        Icon(
            painter = painterResource(id = R.drawable.ic_shapes),
            contentDescription = "Puzzle Part 2",
            tint = Color(0xFF4CAF50), // Yeşil
            modifier = Modifier
                .size(28.dp * scale2)
                .offset(8.dp, (-8).dp)
        )
        
        // Yapboz parçası 3 (Sağ alt)
        Icon(
            painter = painterResource(id = R.drawable.ic_shapes),
            contentDescription = "Puzzle Part 3",
            tint = Color(0xFF2196F3), // Mavi
            modifier = Modifier
                .size(28.dp * scale3)
                .offset(8.dp, 8.dp)
        )
        
        // Yapboz parçası 4 (Sol alt)
        Icon(
            painter = painterResource(id = R.drawable.ic_shapes),
            contentDescription = "Puzzle Part 4",
            tint = Color(0xFFFF9800), // Turuncu
            modifier = Modifier
                .size(28.dp * scale1)
                .offset((-8).dp, 8.dp)
        )
    }
}

@Composable
fun AnimatedClockIcon(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "clockAnimation")
    
    val hourHandAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "hourAngle"
    )
    
    val minuteHandAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "minuteAngle"
    )
    
    Box(
        modifier = modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Saat yüzü
            drawCircle(
                color = Color(0xFFFFECB3),
                radius = size.minDimension / 2,
                style = Stroke(width = size.minDimension * 0.08f)
            )
            
            // Saat noktaları
            for (i in 0 until 12) {
                val angle = Math.toRadians((i * 30).toDouble())
                val radius = size.minDimension * 0.4
                val x = (size.width / 2 + cos(angle) * radius).toFloat()
                val y = (size.height / 2 + sin(angle) * radius).toFloat()
                
                drawCircle(
                    color = Color(0xFF795548),
                    radius = size.minDimension * 0.025f,
                    center = Offset(x, y)
                )
            }
            
            // Saat merkezi
            drawCircle(
                color = Color(0xFFFF5722),
                radius = size.minDimension * 0.05f
            )
            
            // Akrep (saat kolu)
            rotate(hourHandAngle) {
                drawLine(
                    color = Color(0xFF3F51B5),
                    start = Offset(size.width / 2, size.height / 2),
                    end = Offset(size.width / 2, size.height / 2 - size.minDimension * 0.2f),
                    strokeWidth = size.minDimension * 0.04f
                )
            }
            
            // Yelkovan (dakika kolu)
            rotate(minuteHandAngle) {
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(size.width / 2, size.height / 2),
                    end = Offset(size.width / 2, size.height / 2 - size.minDimension * 0.3f),
                    strokeWidth = size.minDimension * 0.02f
                )
            }
        }
    }
}

@Composable
fun RobotWithBlinkingEyesIcon(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "robotAnimation")
    
    val eyeSize by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(300, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(2000, StartOffsetType.FastForward)
        ),
        label = "eyeBlink"
    )
    
    val antennaScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOutBack),
            repeatMode = RepeatMode.Reverse
        ),
        label = "antennaScale"
    )
    
    Box(
        modifier = modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Robot gövdesi
        Icon(
            painter = painterResource(id = R.drawable.ic_algorithm),
            contentDescription = "Robot Body",
            tint = Color(0xFF9C27B0), // Mor
            modifier = Modifier.size(48.dp)
        )
        
        // Robot gözleri
        Box(
            modifier = Modifier
                .offset((-8).dp, (-8).dp)
                .size(8.dp * eyeSize)
                .background(Color(0xFFFF4081)) // Pembe göz
        )
        
        Box(
            modifier = Modifier
                .offset(8.dp, (-8).dp)
                .size(8.dp * eyeSize)
                .background(Color(0xFFFF4081)) // Pembe göz
        )
        
        // Robot anteni
        Box(
            modifier = Modifier
                .offset(y = (-28).dp * antennaScale)
                .size(6.dp)
                .background(Color(0xFF4CAF50)) // Yeşil anten
        )
    }
}

@Composable
fun GameControllerWithStarsIcon(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gameAnimation")
    
    // Joystick dönüş animasyonu
    val rotation by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "controllerRotation"
    )
    
    val density = LocalDensity.current
    val iconSize = with(density) { 56.dp.toPx() }
    
    // 5 yıldız için ayrı animasyonlar
    val starAnimations = (0 until 5).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1500,
                    delayMillis = index * 300,
                    easing = EaseInOutCubic
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "star$index"
        )
    }
    
    Box(
        modifier = modifier.size(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Joystick ikonu
        Icon(
            imageVector = Icons.Default.SportsEsports,
            contentDescription = "Game Controller",
            tint = Color(0xFF673AB7), // Mor
            modifier = Modifier
                .size(40.dp)
                .rotate(rotation)
        )
        
        // Dönen yıldızlar
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerX = size.width / 2
            val centerY = size.height / 2
            val radius = size.minDimension / 2.5f
            
            starAnimations.forEachIndexed { index, animation ->
                val angle = (index * 72 + animation.value * 40) % 360
                val angleRad = Math.toRadians(angle.toDouble())
                val x = (centerX + cos(angleRad) * radius).toFloat()
                val y = (centerY + sin(angleRad) * radius).toFloat()
                val starSize = size.minDimension * 0.15f * animation.value
                
                drawCircle(
                    color = Color(0xFFFF9800), // Turuncu
                    radius = starSize,
                    center = Offset(x, y)
                )
            }
        }
    }
} 