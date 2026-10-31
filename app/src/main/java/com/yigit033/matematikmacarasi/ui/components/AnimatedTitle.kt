package com.yigit033.matematikmacarasi.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.ui.theme.MontserratFont
import kotlinx.coroutines.delay
import kotlin.random.Random

@Composable
fun BouncingLetterText(
    text: String,
    colors: List<Color>,
    modifier: Modifier = Modifier,
    fontSize: Float = 30f,
    delay: Int = 180,
    bounceHeight: Float = 8f
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        text.forEachIndexed { index, char ->
            val colorIndex = index % colors.size
            BouncingLetter(
                letter = char.toString(),
                color = colors[colorIndex],
                fontSize = fontSize,
                startDelay = index * delay,
                bounceHeight = bounceHeight
            )
        }
    }
}

@Composable
fun BouncingLetter(
    letter: String,
    color: Color,
    fontSize: Float,
    startDelay: Int,
    bounceHeight: Float
) {
    var shouldAnimate by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = letter) {
        delay(startDelay.toLong())
        shouldAnimate = true
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (shouldAnimate) -bounceHeight else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1200,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = Modifier
            .offset(y = offsetY.dp)
            .padding(horizontal = 0.1.dp)
    ) {
        Text(
            text = letter,
            color = color,
            fontSize = fontSize.sp,
            fontFamily = MontserratFont,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
fun AnimatedTitle(
    text: String = "Matematik Macerası",
    modifier: Modifier = Modifier
) {
    val colors = listOf(
        Color(0xFFFFFFFF), // Beyaz
        Color(0xFFF8BBD0), // Açık Pembe
        Color(0xFFAED581), // Açık Yeşil
        Color(0xFF81D4FA), // Açık Mavi
        Color(0xFFFFE082), // Açık Sarı
        Color(0xFFB2DFDB), // Açık Teal
        Color(0xFFFFCCBC), // Açık Turuncu
        Color(0xFFD1C4E9), // Açık Mor
        Color(0xFFF5F5F5), // Çok Açık Gri
        Color(0xFFB3E5FC), // Çok Açık Mavi
        Color(0xFFF8BBD0), // Açık Pembe
        Color(0xFFAED581), // Açık Yeşil
        Color(0xFF81D4FA), // Açık Mavi
        Color(0xFFFFE082), // Açık Sarı
        Color(0xFFB2DFDB), // Açık Teal
        Color(0xFFFFCCBC), // Açık Turuncu
        Color(0xFFE3F2FD), // En Açık Indigo
        Color(0xFFE0F2F1)  // En Açık Teal
    )
    
    BouncingLetterText(
        text = text,
        colors = colors,
        modifier = modifier.padding(horizontal = 8.dp),
        fontSize = 30f,
        delay = 180,
        bounceHeight = 8f
    )
} 