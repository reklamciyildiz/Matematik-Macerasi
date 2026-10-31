package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yigit033.matematikmacarasi.games.models.market.MarketLevels
import com.yigit033.matematikmacarasi.games.models.market.ShoppingList

/**
 * Seviye seçim diyalog bileşeni - Yeni, daha interaktif tasarım
 * 
 * @param onLevelSelected Seviye seçildiğinde çağrılacak fonksiyon
 * @param onDismiss İptal edildiğinde çağrılacak fonksiyon
 */
@Composable
fun LevelSelectDialog(
    onLevelSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    // Seviye listesi - güvenli erişim
    val levels = try {
        MarketLevels.levels.takeIf { it.isNotEmpty() } 
            ?: throw IllegalStateException("Seviye listesi boş")
    } catch (e: Exception) {
        e.printStackTrace()
        // Hata durumunda boş liste gösterilmesini önlemek için
        // burada basit bir liste oluşturabilirdik, ancak
        // bu çözümü MarketLevels sınıfında uyguladık
        MarketLevels.levels // MarketLevels zaten güvenli bir liste döndürecek
    }
    
    // En az bir adet seviye olduğundan emin ol
    if (levels.isEmpty()) {
        // Hiç seviye yoksa hata mesajı göster
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Hata") },
            text = { Text("Seviye listesi yüklenemedi. Lütfen daha sonra tekrar deneyin.") },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Tamam")
                }
            }
        )
        return
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Başlık
                Text(
                    text = "Seviye Seç",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Açıklama
                Text(
                    text = "Alışveriş becerileri için en uygun seviyeyi seç",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Seviye listesi
                LazyColumn(
                    modifier = Modifier.weight(1f, fill = false),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    itemsIndexed(levels) { index, level ->
                        // Seviye kartı arkaplan renkleri
                        val cardColors = when (level.difficulty) {
                            1 -> Color(0xFF8BC34A) // Kolay - Yeşil
                            2 -> Color(0xFF29B6F6) // Orta - Mavi
                            3 -> Color(0xFFFFB74D) // Zor - Turuncu
                            in 4..5 -> Color(0xFFE57373) // Çok Zor - Kırmızı
                            else -> Color(0xFF9575CD) // Diğer - Mor
                        }
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onLevelSelected(index) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                // Üst kısım - renkli başlık alanı
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    cardColors,
                                                    cardColors.copy(alpha = 0.7f)
                                                )
                                            ),
                                            shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                                        )
                                        .padding(vertical = 12.dp, horizontal = 16.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        // Seviye adı
                                        Text(
                                            text = level.name,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        
                                        // Zorluk seviyesi yıldızları
                                        LevelDifficultyStars(difficulty = level.difficulty)
                                    }
                                }
                                
                                // İçerik - açıklama ve detaylar
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    // Seviye bilgi öğeleri
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Açıklama
                                        Text(
                                            text = level.description,
                                            fontSize = 14.sp,
                                            color = Color.DarkGray,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        
                                        // Süre
                                        LevelInfoItem(
                                            icon = Icons.Default.AccessTime,
                                            text = "${level.timeInSeconds / 60} dakika"
                                        )
                                        
                                        // Bütçe
                                        LevelInfoItem(
                                            icon = Icons.Default.AttachMoney,
                                            text = "${level.budget}₺"
                                        )
                                        
                                        // Alışveriş listesi ürün sayısı
                                        LevelInfoItem(
                                            icon = Icons.Default.CheckCircle,
                                            text = "${level.items.size} ürün"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // İptal butonu
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "İptal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Zorluk seviyesini yıldızlarla gösteren bileşen
 */
@Composable
private fun LevelDifficultyStars(difficulty: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (index < difficulty) Color.White else Color.White.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/**
 * Seviye bilgi öğesi bileşeni
 */
@Composable
private fun LevelInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    tint: Color = Color(0xFF2196F3)
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.DarkGray
        )
    }
} 