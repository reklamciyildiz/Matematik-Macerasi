package com.yigit033.matematikmacarasi.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * İstatistik detayları için popup dialog.
 * Premium kullanıcıların her bir istatistik kartına tıklanıldığında gösterilir.
 */
@Composable
fun StatisticsDetailPopup(
    title: String,
    items: Map<String, Int>,
    formatValue: (Int) -> String = { it.toString() },
    accentColor: Color,
    onDismiss: () -> Unit
) {
    val maxValue = items.values.maxOrNull() ?: 1
    
    // Boş durumu için standart değer (0 değerleri olan oyunlar boş sayılmasın)
    val hasData = items.isNotEmpty()
    
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
            elevation = 8.dp,
            backgroundColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Başlık ve Kapat butonu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "İstatistik Detayları",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color(0xFF9E9E9E)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Alt başlık
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = accentColor
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Eğer veri yoksa uyarı göster
                if (!hasData) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Henüz veri bulunmamaktadır",
                            color = Color(0xFF757575),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Oyun listesi - Önce değeri 0 olmayan ve sonra da 0 olanları göster
                    val sortedItems = items.entries
                        .sortedWith(compareByDescending<Map.Entry<String, Int>> { it.value }
                                    .thenBy { it.key })
                        
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp) // Yükseklik sınırlaması
                    ) {
                        // Oynanmış oyunlar için başlık
                        if (sortedItems.any { it.value > 0 }) {
                            item {
                                Text(
                                    text = "Oynanmış Oyunlar",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF424242),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            // Oynanmış oyunlar
                            items(sortedItems.filter { it.value > 0 }) { (gameName, value) ->
                                StatisticDetailItem(
                                    gameName = gameName,
                                    value = value,
                                    formatValue = formatValue,
                                    maxValue = maxValue,
                                    accentColor = accentColor
                                )
                            }
                        }
                        
                        // Henüz oynanmamış oyunlar için başlık
                        if (sortedItems.any { it.value == 0 }) {
                            item {
                                Text(
                                    text = "Henüz Oynanmamış Oyunlar",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF757575),
                                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                                )
                            }
                            
                            // Oynanmamış oyunlar
                            items(sortedItems.filter { it.value == 0 }) { (gameName, value) ->
                                StatisticDetailItem(
                                    gameName = gameName,
                                    value = value,
                                    formatValue = formatValue,
                                    maxValue = maxValue,
                                    accentColor = Color(0xFFAAAAAA), // Soluk renk
                                    isInactive = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Detay listesindeki her bir satır için görünüm
 */
@Composable
private fun StatisticDetailItem(
    gameName: String,
    value: Int,
    formatValue: (Int) -> String,
    maxValue: Int,
    accentColor: Color,
    isInactive: Boolean = false // Oynanmamış oyunlar için soluk gösterim
) {
    val progressPercentage = remember { 
        if (maxValue > 0) value.toFloat() / maxValue.toFloat() else 0f 
    }
    val animatedProgress by animateFloatAsState(targetValue = progressPercentage)
    
    // Transparency for inactive items
    val contentAlpha = if (isInactive) 0.5f else 1.0f
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            // Modifier.alpha yerine direct transparentleri ayarlayacağız
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = gameName,
                fontSize = 14.sp,
                color = if (isInactive) Color(0xFF9E9E9E) else Color(0xFF424242),
                modifier = Modifier.weight(1f)
            )
            
            Text(
                text = formatValue(value),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accentColor.copy(alpha = contentAlpha)
            )
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // İlerleme çubuğu - İnaktif durum için ayrı renk
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color(0xFFEEEEEE))
        ) {
            if (progressPercentage > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(accentColor.copy(alpha = if (isInactive) 0.3f else 0.7f))
                )
            }
        }
    }
} 