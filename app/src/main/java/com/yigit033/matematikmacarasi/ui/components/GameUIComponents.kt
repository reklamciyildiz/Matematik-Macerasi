package com.yigit033.matematikmacarasi.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.ui.theme.AccentGreen
import com.yigit033.matematikmacarasi.ui.theme.AccentRed
import com.yigit033.matematikmacarasi.ui.theme.AnimationDurations
import kotlinx.coroutines.delay

/**
 * Oyun üst bar bileşeni.
 * 
 * @param gameName Oyun adı
 * @param level Mevcut seviye
 * @param timeRemaining Kalan süre (saniye)
 * @param score Mevcut skor
 * @param totalTime Toplam oyun süresi (saniye)
 * @param onBackClick Geri butonuna tıklandığında çağrılacak fonksiyon
 */
@Composable
fun GameTopBar(
    gameName: String,
    level: Int,
    timeRemaining: Int,
    score: Int,
    totalTime: Int,
    onBackClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Üst Kısım: Geri Butonu, Oyun Adı ve Seviye
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Geri Butonu
                Icon(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = "Geri",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onBackClick() },
                    tint = MaterialTheme.colorScheme.primary
                )
                
                // Oyun Adı
                Text(
                    text = gameName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // Seviye
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Seviye $level",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Zaman çubuğu
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Süre:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "$timeRemaining sn",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = timeRemaining.toFloat() / totalTime.toFloat(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (timeRemaining > totalTime / 3) MaterialTheme.colorScheme.primary else AccentRed,
                    trackColor = Color.LightGray,
                    strokeCap = StrokeCap.Round
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Skor Göstergesi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Skor:",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Soru alanı bileşeni.
 * 
 * @param question Soru metni
 * @param modifier Modifiye edici
 * @param content İçerik composable fonksiyonu
 */
@Composable
fun QuestionArea(
    question: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Soru Metni
            Text(
                text = question,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // İçerik alanı (resimler, görseller, vb.)
            content()
        }
    }
}

/**
 * Cevap seçeneği bileşeni.
 * 
 * @param text Seçenek metni
 * @param onClick Tıklama işlevi
 * @param isCorrect Seçeneğin doğru olup olmadığı (null = henüz değerlendirilmedi)
 * @param enabled Seçeneğin etkin olup olmadığı
 */
@Composable
fun AnswerOption(
    text: String,
    onClick: () -> Unit,
    isCorrect: Boolean? = null,
    enabled: Boolean = true
) {
    val backgroundColor = when(isCorrect) {
        true -> AccentGreen.copy(alpha = 0.2f)
        false -> AccentRed.copy(alpha = 0.2f)
        null -> Color.White
    }
    
    val borderColor = when(isCorrect) {
        true -> AccentGreen
        false -> AccentRed
        null -> MaterialTheme.colorScheme.primary
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isCorrect == true) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .scale(if (isCorrect == true) scale else 1f)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(enabled = enabled && isCorrect == null) { onClick() }
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            // Doğru/Yanlış göstergeleri
            if (isCorrect != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    painter = painterResource(
                        id = if (isCorrect) R.drawable.ic_check else R.drawable.ic_close
                    ),
                    contentDescription = if (isCorrect) "Doğru" else "Yanlış",
                    tint = if (isCorrect) AccentGreen else AccentRed,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

/**
 * Geri bildirim animasyonu bileşeni.
 * 
 * @param isCorrect Cevabın doğru olup olmadığı
 * @param visible Animasyonun görünür olup olmadığı
 */
@Composable
fun FeedbackAnimation(
    isCorrect: Boolean,
    visible: Boolean
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(durationMillis = AnimationDurations.SHORT)),
        exit = fadeOut(tween(durationMillis = AnimationDurations.SHORT))
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (isCorrect) {
                // Doğru cevap animasyonu
                Image(
                    painter = painterResource(id = R.drawable.ic_correct_answer),
                    contentDescription = "Doğru",
                    modifier = Modifier.size(120.dp)
                )
            } else {
                // Yanlış cevap animasyonu
                Image(
                    painter = painterResource(id = R.drawable.ic_wrong_answer),
                    contentDescription = "Yanlış",
                    modifier = Modifier.size(120.dp)
                )
            }
        }
    }
}

/**
 * Oyun tamamlandı ekranı.
 * 
 * @param score Toplam skor
 * @param stars Kazanılan yıldız sayısı (1-3)
 * @param onNextLevel Sonraki seviye butonuna tıklandığında çağrılacak fonksiyon
 * @param onRestart Yeniden başlat butonuna tıklandığında çağrılacak fonksiyon
 * @param onHome Ana menüye dön butonuna tıklandığında çağrılacak fonksiyon
 */
@Composable
fun GameCompletedScreen(
    score: Int,
    stars: Int,
    onNextLevel: () -> Unit,
    onRestart: () -> Unit,
    onHome: () -> Unit
) {
    var showConfetti by remember { mutableStateOf(false) }
    
    LaunchedEffect(key1 = Unit) {
        showConfetti = true
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.9f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Başlık
            Text(
                text = "Tebrikler!",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Yıldızlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                for (i in 1..3) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_star),
                        contentDescription = "Yıldız",
                        modifier = Modifier.size(48.dp),
                        tint = if (i <= stars) MaterialTheme.colorScheme.tertiary else Color.Gray.copy(alpha = 0.3f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Skor
            Text(
                text = "Skorun: $score",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Butonlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Ana Menü
                ChildFriendlyButton(
                    text = "Ana Menü",
                    onClick = onHome,
                    modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Yeniden Oyna
                ChildFriendlyButton(
                    text = "Yeniden Oyna",
                    onClick = onRestart,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Sonraki Seviye
            ChildFriendlyButton(
                text = "Sonraki Seviye",
                onClick = onNextLevel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
} 