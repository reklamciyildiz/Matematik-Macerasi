package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Timer
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.models.market.ShoppingCart
import kotlinx.coroutines.delay

/**
 * Geliştirilmiş Oyun Tamamlandı Ekranı
 * 
 * @param score Oyun puanı
 * @param stars Kazanılan yıldız sayısı (1-3)
 * @param shoppingCart Alışveriş sepeti
 * @param change Para üstü
 * @param onPlayAgain Tekrar oyna butonuna tıklandığında çağrılacak fonksiyon
 * @param onBackToMenu Ana menüye dön butonuna tıklandığında çağrılacak fonksiyon
 */
@Composable
fun GameCompletedScreen(
    score: Int,
    stars: Int,
    shoppingCart: ShoppingCart,
    change: Double,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    // Animasyon için state
    var showConfetti by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }
    var progressValue by remember { mutableStateOf(0f) }
    var showTrophy by remember { mutableStateOf(false) }
    
    // Animasyon başlatma - aşamalı olarak göster
    LaunchedEffect(Unit) {
        // Konfeti göster
        showConfetti = true
        delay(300)
        
        // Ödül göster
        showTrophy = true
        delay(500)
        
        // İçeriği göster
        showContent = true
        
        // İlerleme çubuğu animasyonu - daha yavaş ve görünür
        for (i in 1..25) {
            progressValue = i / 25f
            delay(40)
        }
    }
    
    // Yıldız animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "stars")
    val starScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "starScale"
    )
    
    // Ödül animasyonu
    val trophyScale by animateFloatAsState(
        targetValue = if (showTrophy) 1f else 0f,
        animationSpec = tween(800, easing = EaseOutBack),
        label = "trophyScale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1565C0),  // Daha koyu mavi
                        Color(0xFF64B5F6)   // Daha açık mavi
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Arkaplan öğeleri - konfeti ve kutlama efektleri
        if (showConfetti) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter
            ) {
                // Konfeti efekti
                Image(
                    painter = painterResource(id = R.drawable.confetti),
                    contentDescription = "Konfeti",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                )
            }
        }
        
        // Ana içerik
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(1000)) + expandVertically(animationSpec = tween(1000))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Başarı simgesi ve ödül
                Box(
                    modifier = Modifier.padding(top = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (showTrophy) {
                        // Animasyonlu ödül kupası
                        Box(
                            modifier = Modifier
                                .size(150.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF9C4))
                                .scale(trophyScale)
                                .border(4.dp, Color(0xFFFFD700), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            // Kutu içinde başarı kupası
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = "Ödül",
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(70.dp)
                                )
                                
                                // Yıldızlar görsel
                                Row(
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    for (i in 1..stars) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Başlık - Animasyonlu metin boyutu
                Text(
                    text = "Harika İş!",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color(0xFF1976D2),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )
                
                // Açıklama metni
                Text(
                    text = buildAnnotatedString {
                        append("Alışverişini ")
                        withStyle(style = SpanStyle(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFEB3B),
                            fontSize = 22.sp
                        )) {
                            append("başarıyla")
                        }
                        append(" tamamladın!")
                    },
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Yıldız sırası - daha büyük ve animasyonlu
                Row(
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..3) {
                        Icon(
                            imageVector = if (i <= stars) Icons.Filled.Star else Icons.Filled.StarBorder,
                            contentDescription = "Yıldız $i",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier
                                .size(60.dp)
                                .padding(horizontal = 4.dp)
                                .scale(if (i <= stars) starScale else 1f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Puan Kartı - daha çekici ve renkli
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Leaderboard,
                                contentDescription = null,
                                tint = Color(0xFF1976D2),
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(end = 8.dp)
                            )
                            
                            Text(
                                text = "Kazandığın Puan",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF1976D2)
                            )
                        }
                        
                        Text(
                            text = "$score",
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D2)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Daha görünür ilerleme çubuğu
                        LinearProgressIndicator(
                            progress = progressValue,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = Color(0xFF4CAF50),
                            trackColor = Color(0xFFE0E0E0)
                        )
                    }
                }
                
                // Alışveriş özeti kartı - daha düzenli
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE1F5FE)  // Açık mavi arka plan
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Başlık çubuğu
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.ShoppingCart,
                                contentDescription = null,
                                tint = Color(0xFF1976D2),
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(end = 8.dp)
                            )
                            
                            Text(
                                text = "Alışveriş Özetin",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1976D2)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Ürün sayısı
                        val itemCount = shoppingCart.getItemCount()
                        val totalPrice = shoppingCart.getTotalPrice()
                        val givenMoney = change + totalPrice
                        
                        // Renkli kutularda bilgiler
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFBBDEFB),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                // Toplam öğe sayısı
                                InfoRow(
                                    label = "Aldığın Ürün Sayısı:",
                                    value = "$itemCount adet",
                                    valueColor = Color(0xFF1976D2)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Toplam harcama
                                InfoRow(
                                    label = "Toplam Harcaman:",
                                    value = "${String.format("%.2f", totalPrice)} ₺",
                                    valueColor = Color(0xFF1976D2)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Verilen para
                                InfoRow(
                                    label = "Verdiğin Para:",
                                    value = "${String.format("%.2f", givenMoney)} ₺",
                                    valueColor = Color(0xFF1976D2)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Para üstü - daha vurgulu
                                InfoRow(
                                    label = "Para Üstü:",
                                    value = "${String.format("%.2f", change)} ₺",
                                    valueColor = Color(0xFF4CAF50),
                                    valueFontWeight = FontWeight.Bold,
                                    valueFontSize = 18.sp
                                )
                            }
                        }
                    }
                }
                
                // Eğitim özeti kartı
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)  // Açık yeşil arka plan
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Başlık çubuğu
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Filled.School,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier
                                    .size(28.dp)
                                    .padding(end = 8.dp)
                            )
                            
                            Text(
                                text = "Neler Öğrendin",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Para üstü hesaplama
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = Color(0xFFC8E6C9),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Para Üstü Hesaplaması:",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2E7D32)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Hesaplama formülü - vurgulu gösterim
                                Text(
                                    text = buildAnnotatedString {
                                        // Verilen para (change + toplam harcama)
                                        append("Verdiğin Para: ")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))) {
                                            append(String.format("%.2f ₺", change + shoppingCart.getTotalPrice()))
                                        }
                                        append("\nEksi (-)\nToplam Tutar: ")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))) {
                                            append(String.format("%.2f ₺", shoppingCart.getTotalPrice()))
                                        }
                                        append("\nEşittir (=)\nPara Üstü: ")
                                        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32), fontSize = 18.sp)) {
                                            append(String.format("%.2f ₺", change))
                                        }
                                    },
                                    fontSize = 16.sp,
                                    color = Color(0xFF33691E),
                                    lineHeight = 24.sp
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Daha büyük ve renkli butonlar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Tekrar oyna butonu
                    Button(
                        onClick = onPlayAgain,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 8.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Tekrar",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp)
                        )
                        
                        Text(
                            text = "Tekrar Oyna",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Ana menüye dön butonu
                    Button(
                        onClick = onBackToMenu,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1976D2)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Home,
                            contentDescription = "Ana Menü",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 4.dp)
                        )
                        
                        Text(
                            text = "Ana Menü",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Bilgi satırı bileşeni - label ve değer gösterir
 */
@Composable
private fun InfoRow(
    label: String,
    value: String,
    valueColor: Color = Color.Black,
    valueFontWeight: FontWeight = FontWeight.Medium,
    valueFontSize: TextUnit = 16.sp
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color(0xFF37474F)
        )
        
        Text(
            text = value,
            fontSize = valueFontSize,
            fontWeight = valueFontWeight,
            color = valueColor
        )
    }
}

/**
 * Öğrenme noktası bileşeni
 */
@Composable
private fun LearningPoint(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF1976D2))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFF424242)
            )
        }
    }
}

/**
 * Öğrenme noktası bileşeni (AnnotatedString versiyonu)
 */
@Composable
private fun LearningPoint(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: androidx.compose.ui.text.AnnotatedString
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF1976D2))
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D2)
            )
            
            Spacer(modifier = Modifier.height(2.dp))
            
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color(0xFF424242)
            )
        }
    }
} 