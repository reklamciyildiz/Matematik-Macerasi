package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.games.models.market.ShoppingList

/**
 * Market oyunu alt çubuğu
 * 
 * @param level Market seviyesi
 * @param cartTotal Sepet toplamı
 * @param onCheckout Kasaya git butonuna tıklandığında çağrılacak fonksiyon
 * @param onShowCart Sepeti görüntüle butonuna tıklandığında çağrılacak fonksiyon
 */
@Composable
fun MarketBottomBar(
    level: ShoppingList,
    cartTotal: Double,
    onCheckout: () -> Unit,
    onShowCart: () -> Unit = {}
) {
    // Tamamlanan ürün sayısı ve ilerleme
    val completedItems = level.items.count { it.isCompleted }
    val totalItems = level.items.size
    val progress = if (totalItems > 0) completedItems.toFloat() / totalItems else 0f
    
    // Bütçe ve alışveriş durumu
    val allCompleted = level.items.all { it.isCompleted }
    val isWithinBudget = cartTotal <= level.budget
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // İlerleme bilgisi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tamamlanan ürün sayısı
                Text(
                    text = "$completedItems / $totalItems ürün toplandı",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                // Bütçe/Toplam bilgisi
                Text(
                    text = "${String.format("%.2f ₺", cartTotal)} / ${String.format("%.2f ₺", level.budget)}",
                    fontSize = 12.sp,
                    color = if (cartTotal > level.budget) Color.Red else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // İlerleme çubuğu - standar versiyonu kullan
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.LightGray
            )
            
            // Butonlar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sol tarafta sepeti görüntüleme butonu
                OutlinedButton(
                    onClick = onShowCart,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.ShoppingCart,
                        contentDescription = "Sepeti Görüntüle",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Sepeti Görüntüle")
                }
                
                // Sağ tarafta kasaya git butonu
                Button(
                    onClick = onCheckout,
                    enabled = allCompleted && isWithinBudget,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.LightGray
                    ),
                    modifier = Modifier.height(36.dp)
                ) {
                    Text(
                        text = "Kasaya Git",
                        color = if (allCompleted && isWithinBudget) Color.White else Color.DarkGray
                    )
                }
            }
        }
    }
} 