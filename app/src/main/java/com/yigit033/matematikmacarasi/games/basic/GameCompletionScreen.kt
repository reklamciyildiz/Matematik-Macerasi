package com.yigit033.matematikmacarasi.games.basic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

/**
 * Oyun tamamlandığında gösterilen çocuk dostu tebrik ekranı
 * 
 * @param score Oyunda kazanılan toplam puan
 * @param correctAnswers Doğru cevap sayısı
 * @param wrongAnswers Yanlış cevap sayısı
 * @param currentLevel Mevcut oyun seviyesi
 * @param maxLevel Maksimum oyun seviyesi
 * @param onPlayAgain Tekrar oyna butonuna tıklandığında çağrılacak callback
 * @param onMainMenu Ana menü butonuna tıklandığında çağrılacak callback
 * @param onNextLevel Sonraki seviye butonuna tıklandığında çağrılacak callback
 */
@Composable
fun GameCompletionScreen(
    score: Int,
    correctAnswers: Int, 
    wrongAnswers: Int,
    currentLevel: Int,
    maxLevel: Int = 5,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit,
    onNextLevel: () -> Unit
) {
    val isLastLevel = currentLevel >= maxLevel
    
    // Yıldızlar için animasyon
    var showStars by remember { mutableStateOf(false) }
    var showButtons by remember { mutableStateOf(false) }
    var showScore by remember { mutableStateOf(false) }
    
    // Animasyonları sırayla tetikle
    LaunchedEffect(key1 = true) {
        delay(300)
        showStars = true
        delay(800)
        showScore = true
        delay(500)
        showButtons = true
    }
    
    // Performans yüzdesini hesapla
    val totalQuestions = correctAnswers + wrongAnswers
    val performancePercentage = if (totalQuestions > 0) {
        (correctAnswers.toFloat() / totalQuestions.toFloat()) * 100f
    } else {
        0f
    }
    
    // Yıldız sayısını hesapla
    val stars = when {
        performancePercentage >= 80 -> 3
        performancePercentage >= 50 -> 2
        else -> 1
    }
    
    // Performans notu
    val performanceNote = when {
        performancePercentage >= 80 -> "Harika!"
        performancePercentage >= 50 -> "İyi İş!"
        else -> "Tekrar Dene!"
    }

    // Arka plan gradient rengi
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF90CAF9), // Açık mavi üst
            Color(0xFFBBDEFB)  // Daha açık mavi alt
        )
    )
    
    Dialog(
        onDismissRequest = { /* Dialog'un dışına tıklandığında kapatılmasını engelliyoruz */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Başlık
                Text(
                    text = "Tebrikler!",
                    color = Color(0xFF2196F3),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                
                // Açıklama
                Text(
                    text = "Seviye Tamamlandı!",
                    color = Color(0xFF4CAF50),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Yıldızlar
                AnimatedVisibility(
                    visible = showStars,
                    enter = fadeIn(animationSpec = tween(500)) + 
                            expandVertically(animationSpec = tween(500))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Yıldızları göster
                        for (i in 1..3) {
                            StarIcon(
                                active = i <= stars,
                                delay = i * 200L
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Skor ve İstatistikler
                AnimatedVisibility(
                    visible = showScore,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Skorun: $score",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatisticItem(
                                label = "Doğru",
                                value = correctAnswers.toString(),
                                color = Color(0xFF4CAF50)
                            )
                            
                            StatisticItem(
                                label = "Yanlış",
                                value = wrongAnswers.toString(),
                                color = Color(0xFFF44336)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Performans: %${performancePercentage.toInt()}",
                            fontSize = 16.sp,
                            color = Color(0xFF2196F3)
                        )
                        
                        Text(
                            text = performanceNote,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                performancePercentage >= 80 -> Color(0xFF4CAF50)
                                performancePercentage >= 50 -> Color(0xFFFFC107)
                                else -> Color(0xFFF44336)
                            }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Butonlar
                AnimatedVisibility(
                    visible = showButtons,
                    enter = fadeIn(animationSpec = tween(500))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Ana Menü Butonu - tam genişlikte
                        GameButton(
                            text = "Ana Menü",
                            onClick = onMainMenu,
                            backgroundColor = Color(0xFF64B5F6),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Tekrar Oyna Butonu - tam genişlikte
                        GameButton(
                            text = "Tekrar Oyna",
                            onClick = onPlayAgain,
                            backgroundColor = Color(0xFF4CAF50),
                            modifier = Modifier.fillMaxWidth(0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Sonraki Seviye Butonu - Son seviye değilse göster
                        if (!isLastLevel) {
                            GameButton(
                                text = "Sonraki Seviye",
                                onClick = onNextLevel,
                                backgroundColor = Color(0xFFFFC107),
                                modifier = Modifier.fillMaxWidth(0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StarIcon(active: Boolean, delay: Long = 0) {
    var animated by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = true) {
        delay(delay)
        animated = true
    }
    
    val scale by animateFloatAsState(
        targetValue = if (animated) 1f else 0f,
        animationSpec = tween(
            durationMillis = 500,
            easing = LinearEasing
        ),
        label = "Star Scale Animation"
    )
    
    // Pulsating animation for active stars
    val infiniteTransition = rememberInfiniteTransition(label = "Star Pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Star Pulse Animation"
    )
    
    val starScale = if (active) scale * (if (animated) pulse else 1f) else scale
    val starColor = if (active) Color(0xFFFFC107) else Color(0xFFE0E0E0)
    
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .scale(starScale)
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = "Star",
            tint = starColor,
            modifier = Modifier.size(48.dp)
        )
    }
}

@Composable
fun StatisticItem(label: String, value: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray
        )
    }
}

@Composable
fun GameButton(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
            .padding(8.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,  // Satır yüksekliği ekleyerek metnin düzgün görünmesini sağlıyoruz
            maxLines = 1         // Tek satırda göster
        )
    }
} 