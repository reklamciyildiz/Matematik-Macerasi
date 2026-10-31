package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yigit033.matematikmacarasi.games.models.market.ShoppingList
import com.yigit033.matematikmacarasi.games.models.market.ShoppingListItem

/**
 * Alışveriş listesi diyalog ekranı
 * 
 * @param shoppingList Alışveriş listesi
 * @param onDismiss Diyalog kapatıldığında çağrılacak fonksiyon
 */
@Composable
fun ShoppingListDialog(
    shoppingList: ShoppingList,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Başlık ve kapatma butonu satırı
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Alışveriş Listem",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Kapatma butonu
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color.Gray
                        )
                    }
                }
                
                // Açıklama metni - Çocuk dostu
                Text(
                    text = "Marketten alman gereken ürünler burada görünüyor. Ürünleri sepete ekledikçe listeden tamamlanmış olarak işaretlenecek!",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                // İlerleme çubuğu
                val completedItems = shoppingList.items.count { it.isCompleted }
                val totalItems = shoppingList.items.size
                val progress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f
                
                // İlerleme metni
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$completedItems / $totalItems tamamlandı",
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                    
                    Text(
                        text = "%${(progress * 100).toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // İlerleme çubuğu
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = Color.LightGray
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Alışveriş listesini göster
                if (shoppingList.items.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Alışveriş listesinde henüz ürün yok",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 200.dp, max = 300.dp)
                                .padding(8.dp)
                        ) {
                            items(shoppingList.items) { item ->
                                ShoppingListItemDetailView(item = item)
                                
                                // Son item değilse ayraç ekle
                                if (item != shoppingList.items.last()) {
                                    HorizontalDivider(
                                        color = Color.LightGray,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Tamam butonu
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
                        text = "Alışverişe Devam Et",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

/**
 * Alışveriş listesi öğesi detaylı görünümü
 * 
 * @param item Alışveriş listesi öğesi
 */
@Composable
private fun ShoppingListItemDetailView(item: ShoppingListItem) {
    val product = item.product
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Tamamlanma durumu ikonu
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = if (item.isCompleted) Color(0xFFE8F5E9) else Color(0xFFEEEEEE),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (item.isCompleted) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = if (item.isCompleted) "Tamamlandı" else "Tamamlanmadı",
                tint = if (item.isCompleted) Color(0xFF4CAF50) else Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        
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
                    text = String.format("İhtiyaç: %.1f %s", item.requiredQuantity, if (product.isByWeight) "kg" else "adet"),
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Ürün fiyatı
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "₺${String.format("%.2f", product.price * item.requiredQuantity)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (item.isCompleted) Color.Gray else MaterialTheme.colorScheme.primary
            )
            
            // Alınan miktar (eğer tamamlandıysa)
            if (item.isCompleted) {
                Text(
                    text = "✓ Alındı",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50),
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
} 