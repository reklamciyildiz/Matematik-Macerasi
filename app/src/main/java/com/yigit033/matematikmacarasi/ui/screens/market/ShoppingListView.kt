package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.games.models.market.ShoppingList
import com.yigit033.matematikmacarasi.games.models.market.ShoppingListItem

/**
 * Alışveriş listesi görünümü
 * 
 * @param level Market seviyesi
 * @param onShowFullList Tam listeyi göstermek için tıklandığında çağrılacak fonksiyon
 * @param modifier Modifier
 */
@Composable
fun ShoppingListView(
    level: ShoppingList,
    onShowFullList: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onShowFullList),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Başlık ve arama ikonu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alışveriş Listesi",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                // Arama ikonu - Tam liste görüntüleme butonu
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Tam Listeyi Göster",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // İlerleme özeti
            ProgressSummary(level = level)
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.LightGray
            )
            
            // Liste boş mu kontrol et
            if (level.items.isEmpty()) {
                // Boş liste mesajı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Alışveriş listesi boş",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                }
            } else {
                // Alışveriş listesi özeti
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Özet bilgisi
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${level.items.size} ürün",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = "Toplam: ${String.format("%.2f", level.items.sumOf { it.product.price * it.requiredQuantity })} ₺",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Alışveriş listesini görüntüle butonu
                    Button(
                        onClick = onShowFullList,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Alışveriş Listesini Görüntüle",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Alışveriş Listesini Görüntüle",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * İlerleme özeti bileşeni
 * 
 * @param level Market seviyesi
 */
@Composable
private fun ProgressSummary(level: ShoppingList) {
    val completedItems = level.items.count { it.isCompleted }
    val totalItems = level.items.size
    val progressValue = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // İlerleme metni
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$completedItems / $totalItems tamamlandı",
                fontSize = 14.sp,
                color = Color.Gray
            )
            
            Text(
                text = "%${(progressValue * 100).toInt()}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        // İlerleme çubuğu - normal ilerleme çubuğu kullan, lambda versiyonu yerine
        val progress = remember(completedItems, totalItems) {
            if (totalItems > 0) completedItems.toFloat() / totalItems else 0f
        }
        
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            progress = progress,
            color = MaterialTheme.colorScheme.primary,
            trackColor = Color.LightGray
        )
    }
}

/**
 * Alışveriş listesi öğesi görünümü
 * 
 * @param item Alışveriş listesi öğesi
 */
@Composable
private fun ShoppingListItemView(item: ShoppingListItem) {
    val product = item.product
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tamamlanma durumu ikonu
        Icon(
            imageVector = if (item.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = if (item.isCompleted) "Tamamlandı" else "Tamamlanmadı",
            tint = if (item.isCompleted) Color.Green else Color.Gray,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Ürün bilgileri
        Column(
            modifier = Modifier.weight(1f)
        ) {
            // Ürün adı
            Text(
                text = product.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                color = if (item.isCompleted) Color.Gray else Color.Black
            )
            
            // Miktar ve birim
            Row(
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Text(
                    text = String.format("%.1f %s", item.requiredQuantity, if (product.isByWeight) "kg" else "adet"),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Fiyat
        Text(
            text = "₺${String.format("%.2f", product.price * item.requiredQuantity)}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (item.isCompleted) Color.Gray else MaterialTheme.colorScheme.primary
        )
    }
} 