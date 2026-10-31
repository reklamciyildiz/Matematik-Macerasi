package com.yigit033.matematikmacarasi.games.basic

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yigit033.matematikmacarasi.R
import androidx.compose.animation.core.*
import com.yigit033.matematikmacarasi.models.DifficultyLevel
import kotlinx.coroutines.delay

/**
 * Zorluk seviyesi seçim ekranı - Matematiksel işlemlere göre uyarlanmış modern ve görsel tasarım
 */

// Zorluk seviyesi tanımlama artık models paketi altında

@Composable
fun DifficultySelectionScreen(
    title: String,
    onDifficultySelected: (DifficultyLevel) -> Unit
) {
    val scrollState = rememberScrollState()
    var showScrollIndicator by remember { mutableStateOf(true) }
    
    // Bilgi dialogu için state
    var showInfoDialog by remember { mutableStateOf(false) }
    
    // İşlem türüne göre renk belirleme
    val themeColor = when {
        title.contains("Toplama") -> Color(0xFF2196F3) // Mavi
        title.contains("Çıkarma") -> Color(0xFFE91E63) // Pembe
        title.contains("Çarpma") -> Color(0xFF4CAF50) // Yeşil
        title.contains("Bölme") -> Color(0xFFFFA000) // Turuncu
        else -> Color(0xFF1565C0) // Varsayılan mavi
    }
    
    // Scroll durumunu izleyen etki
    androidx.compose.runtime.LaunchedEffect(scrollState.value) {
        if (scrollState.value > 0) {
            showScrollIndicator = false
        }
    }

    // Animasyon için 
    val infiniteTransition = rememberInfiniteTransition(label = "info_icon_animation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_animation"
    )

    // İşlem türüne göre arka plan renkleri belirleme
    val gradientColors = when {
        title.contains("Toplama") -> listOf(
            Color(0xFFE3F2FD),  // Çok açık mavi
            Color(0xFFBBDEFB), // Açık mavi
            Color(0xFF64B5F6), // Orta mavi
            Color(0xFF2196F3) // Koyu mavi
        )
        title.contains("Çıkarma") -> listOf(
            Color(0xFFFCE4EC),  // Çok açık pembe
            Color(0xFFF8BBD0), // Açık pembe
            Color(0xFFF06292), // Orta pembe
            Color(0xFFE91E63) // Koyu pembe
        )
        title.contains("Çarpma") -> listOf(
            Color(0xFFE8F5E9),  // Çok açık yeşil
            Color(0xFFC8E6C9), // Açık yeşil
            Color(0xFF81C784), // Orta yeşil
            Color(0xFF4CAF50) // Koyu yeşil
        )
        title.contains("Bölme") -> listOf(
            Color(0xFFFFF3E0),  // Çok açık turuncu
            Color(0xFFFFE0B2), // Açık turuncu
            Color(0xFFFFB74D), // Orta turuncu
            Color(0xFFFFA000) // Koyu turuncu
        )
        else -> listOf(
            Color(0xFFE3F2FD),  // Çok açık mavi alt
            Color(0xFFBBDEFB), // Daha açık mavi orta
            Color(0xFF90CAF9) // Açık mavi üst
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Üst kısım: Başlık ve bilgi butonu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sol tarafta geri butonu pozisyonu için boş alan
                Spacer(modifier = Modifier.width(32.dp))
                
                // Başlık (ortada)
                Text(
                    text = "$title - Seviye Seç",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                
                // Bilgi butonu (sağda) - Yeniden konumlandırıldı ve daha görünür hale getirildi
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(themeColor.copy(alpha = 0.2f))
                        .clickable { showInfoDialog = true }
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Bilgi",
                        tint = themeColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Alt başlık - Bilgi ikonu hakkında açıklama eklendi
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Yaş ve matematik becerine uygun seviyeyi seç",
                    fontSize = 14.sp,
                    color = Color(0xFF5E5E5E),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Bilgi ikonuna dikkat çeken metin
                Text(
                    text = "Yıldız sistemi hakkında bilgi",
                    fontSize = 12.sp,
                    color = themeColor,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Kaydırma göstergesi (scroll state = 0 iken göster)
            if (showScrollIndicator) {
                ScrollIndicator()
            }
            
            // Zorluk seviyeleri - Kaydırılabilir içerik
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                
                // Kolay
                DifficultyLevelCard(
                    title = "Kolay",
                    description = "Anasınıfı - 1. Sınıf (5-7 yaş)",
                    details = "Küçük sayılarla basit işlemler",
                    color = Color(0xFF4CAF50), // Yeşil
                    stars = 1,
                    timeText = "15 sn",
                    numbersText = "1-10 arası",
                    onClick = { onDifficultySelected(DifficultyLevel.EASY) }
                )
                
                // Orta
                DifficultyLevelCard(
                    title = "Orta",
                    description = "2-3. Sınıf (7-9 yaş)",
                    details = "İki basamaklı sayılarla işlemler",
                    color = Color(0xFFFFA000), // Turuncu
                    stars = 2,
                    timeText = "12 sn",
                    numbersText = "1-20 arası",
                    onClick = { onDifficultySelected(DifficultyLevel.MEDIUM) }
                )
                
                // Zor
                DifficultyLevelCard(
                    title = "Zor",
                    description = "4-5. Sınıf (9+ yaş)",
                    details = "Daha büyük sayılarla işlemler",
                    color = Color(0xFFF44336), // Kırmızı
                    stars = 3,
                    timeText = "10 sn",
                    numbersText = "10-50 arası",
                    onClick = { onDifficultySelected(DifficultyLevel.HARD) }
                )
                
                // Çok Zor
                DifficultyLevelCard(
                    title = "Çok Zor",
                    description = "6-7. Sınıf (11+ yaş)",
                    details = "Büyük sayılarla hızlı işlemler",
                    color = Color(0xFF9C27B0), // Mor
                    stars = 4,
                    timeText = "8 sn",
                    numbersText = "20-100 arası",
                    onClick = { onDifficultySelected(DifficultyLevel.VERY_HARD) }
                )
                
                // Uzman
                DifficultyLevelCard(
                    title = "Uzman",
                    description = "8. Sınıf ve üstü (13+ yaş)",
                    details = "Zorlu sayılar ve kısıtlı süre",
                    color = Color(0xFF1E88E5), // Mavi
                    stars = 5,
                    timeText = "6 sn",
                    numbersText = "50-200 arası",
                    onClick = { onDifficultySelected(DifficultyLevel.EXPERT) }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
        
        // Bilgi popup dialogu
        if (showInfoDialog) {
            StarInfoDialog(
                onDismiss = { showInfoDialog = false },
                themeColor = themeColor
            )
        }
    }
}

// Aşağı kaydırma işareti göstergesi
@Composable
fun ScrollIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "scroll_indicator_animation")
    val translationY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce_animation"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        Text(
            text = "Aşağı kaydır",
            fontSize = 12.sp,
            color = Color(0xFF1565C0),
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Icon(
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = "Aşağı Kaydır",
            tint = Color(0xFF1565C0),
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp)
                .offset(y = translationY.dp)
        )
    }
}

@Composable
fun DifficultyLevelCard(
    title: String,
    description: String,
    details: String,
    color: Color,
    stars: Int,
    timeText: String,
    numbersText: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Başlık çubuğu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // İçerik
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Yaş grubu ve açıklama
                Text(
                    text = description,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                
                // Yıldızlar
                Row(
                    modifier = Modifier
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Zorluk: ",
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    
                    // Yıldızları göster
                    for (i in 1..5) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = if (i <= stars) color else Color.LightGray,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(end = 2.dp)
                        )
                    }
                }
                
                // Kısa açıklama
                Text(
                    text = details,
                    fontSize = 14.sp,
                    color = Color(0xFF757575)
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Oyun özellikleri
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Süre
                    DifficultyInfoItem(
                        icon = Icons.Default.AccessTime,
                        text = timeText,
                        color = color
                    )
                    
                    // Sayı aralığı
                    DifficultyInfoItem(
                        icon = Icons.Default.Numbers,
                        text = numbersText,
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
fun DifficultyInfoItem(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            color = Color(0xFF333333)
        )
    }
}

/**
 * Yıldız sistemi hakkında bilgilendirme dialog'u
 */
@Composable
fun StarInfoDialog(
    onDismiss: () -> Unit,
    themeColor: Color
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
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
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Başlık
                Text(
                    text = "Yıldız Sistemi",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = themeColor
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Yıldız bilgisi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(themeColor.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 3 yıldız ikonu
                            repeat(3) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Her seviyeden en fazla 3 yıldız kazanabilirsin",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Her oyunda 5 seviye × 3 yıldız = 15 yıldız",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Avatar bilgisi
                Text(
                    text = "Yeni avatarları açmak için yıldız topla!",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Kapat butonu
                TextButton(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = themeColor
                    )
                ) {
                    Text(
                        text = "TAMAM",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
} 