package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.games.models.market.CartItem
import com.yigit033.matematikmacarasi.games.models.market.ShoppingCart

/**
 * Alışveriş sepetini gösteren bileşen
 * 
 * @param cart Alışveriş sepeti
 * @param onQuantityChange Ürün miktarı değiştiğinde çağrılacak fonksiyon
 * @param onRemoveItem Ürün sepetten çıkarıldığında çağrılacak fonksiyon
 */
@Composable
fun ShoppingCartView(
    cart: ShoppingCart,
    onQuantityChange: (Int, Double) -> Unit,
    onRemoveItem: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Başlık ve toplam
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Alışveriş Sepeti",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Text(
                    text = "Toplam: ${String.format("%.2f ₺", cart.getTotalPrice())}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            HorizontalDivider(color = Color.LightGray)
            
            // Sepet boş mu kontrol et
            if (cart.items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Sepetiniz boş",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                // Sepet öğeleri
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    items(cart.items.values.toList()) { item ->
                        CartItemView(
                            item = item,
                            onQuantityChange = onQuantityChange,
                            onRemove = onRemoveItem
                        )
                        
                        HorizontalDivider(
                            color = Color.LightGray,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Sepetteki bir ürünü gösteren satır bileşeni
 * 
 * @param item Sepet öğesi
 * @param onQuantityChange Miktar değiştiğinde çağrılacak fonksiyon
 * @param onRemove Kaldır butonuna tıklandığında çağrılacak fonksiyon
 */
@Composable
private fun CartItemView(
    item: CartItem,
    onQuantityChange: (Int, Double) -> Unit,
    onRemove: (Int) -> Unit
) {
    val product = item.product
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Ürün bilgileri
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp)
        ) {
            Text(
                text = product.name,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Birim Fiyat: ${String.format("%.2f ₺", product.price)}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                
                if (product.isByWeight) {
                    Text(
                        text = "/kg",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
        }
        
        // Miktar kontrolü
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miktar azaltma butonu
            IconButton(
                onClick = {
                    val decrement = if (product.isByWeight) 0.5 else 1.0
                    val newQuantity = (item.quantity - decrement).coerceAtLeast(if (product.isByWeight) 0.5 else 1.0)
                    onQuantityChange(product.id, newQuantity)
                },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                Text(
                    text = "−",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            // Miktar gösterimi
            Text(
                text = if (product.isByWeight) String.format("%.1f", item.quantity) else item.quantity.toInt().toString(),
                modifier = Modifier.padding(horizontal = 8.dp),
                fontWeight = FontWeight.Medium
            )
            
            // Miktar artırma butonu
            IconButton(
                onClick = {
                    val increment = if (product.isByWeight) 0.5 else 1.0
                    onQuantityChange(product.id, item.quantity + increment)
                },
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray)
            ) {
                Text(
                    text = "+",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Kaldır butonu
        IconButton(
            onClick = { onRemove(product.id) },
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFFFEBEE))
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = "Kaldır",
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
        }
    }
    
    // Toplam fiyat
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        Text(
            text = "Toplam: ${String.format("%.2f ₺", product.price * item.quantity)}",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
} 