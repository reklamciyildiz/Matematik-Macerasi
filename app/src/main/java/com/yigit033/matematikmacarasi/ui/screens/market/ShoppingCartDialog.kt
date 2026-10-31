package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.yigit033.matematikmacarasi.games.models.market.CartItem
import com.yigit033.matematikmacarasi.games.models.market.ShoppingCart
import com.yigit033.matematikmacarasi.games.models.market.ShoppingList
import androidx.compose.foundation.BorderStroke

/**
 * Alışveriş sepeti diyalog ekranı
 * 
 * @param cart Alışveriş sepeti
 * @param onDismiss Diyalog kapatıldığında çağrılacak fonksiyon
 * @param onQuantityChange Ürün miktarı değiştirildiğinde çağrılacak fonksiyon
 * @param onRemoveItem Ürün kaldırıldığında çağrılacak fonksiyon
 * @param onCheckout Kasaya git butonuna tıklandığında çağrılacak fonksiyon
 * @param shoppingList Alışveriş listesi
 */
@Composable
fun ShoppingCartDialog(
    cart: ShoppingCart,
    onDismiss: () -> Unit,
    onQuantityChange: (Int, Double) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onCheckout: () -> Unit,
    shoppingList: ShoppingList? = null
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
                        text = "Alışveriş Sepetim",
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
                    text = "Seçtiğin ürünler burada görünüyor. Ürünlerin miktarını değiştirebilir veya sepetten çıkarabilirsin.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Sepette ürün yoksa
                if (cart.items.isEmpty()) {
                    EmptyCartMessage()
                } else {
                    // Ürün listesi
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                    ) {
                        items(cart.items.keys.toList()) { productId ->
                            cart.items[productId]?.let { item ->
                                val isExceedingRequired = shoppingList?.let { list ->
                                    // Ürün alışveriş listesinde var mı kontrol et
                                    val shoppingItem = list.items.find { it.product.id == productId }
                                    
                                    // Eğer ürün listede varsa ve miktarı aşıyorsa, uyarı göster
                                    shoppingItem != null && item.quantity > shoppingItem.requiredQuantity
                                } ?: false
                                
                                CartItemRow(
                                    item = item,
                                    onQuantityChange = { newQuantity -> onQuantityChange(productId, newQuantity) },
                                    onRemove = { onRemoveItem(productId) },
                                    isExceedingRequired = isExceedingRequired,
                                    requiredQuantity = shoppingList?.items?.find { it.product.id == productId }?.requiredQuantity
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Toplam fiyat
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Toplam:",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = String.format("%.2f ₺", cart.getTotalPrice()),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Kasaya git butonu
                Button(
                    onClick = onCheckout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    enabled = cart.items.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = Color.Gray
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(text = "Kasaya Git")
                }
            }
        }
    }
}

/**
 * Boş sepet mesajı
 */
@Composable
private fun EmptyCartMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ShoppingCart,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color.LightGray
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Sepetiniz Boş",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Gray
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Alışveriş listenizdeki ürünleri seçerek sepetinize ekleyin.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

/**
 * Sepet ürün satırı
 */
@Composable
private fun CartItemRow(
    item: CartItem,
    onQuantityChange: (Double) -> Unit,
    onRemove: () -> Unit,
    isExceedingRequired: Boolean = false,
    requiredQuantity: Double? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        border = if (isExceedingRequired) BorderStroke(1.dp, Color(0xFFE57373)) else null,
        colors = CardDefaults.cardColors(
            containerColor = if (isExceedingRequired) Color(0xFFFFEBEE) else Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.product.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = String.format("%.2f ₺", item.product.price),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
                
                // Kaldır butonu
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Kaldır",
                        tint = Color.Red
                    )
                }
            }
            
            // Miktar kontrolü satırı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Azalt butonu
                IconButton(
                    onClick = {
                        val increment = if (item.product.isByWeight) 0.5 else 1.0
                        val newQuantity = (item.quantity - increment).coerceAtLeast(
                            if (item.product.isByWeight) 0.5 else 1.0
                        )
                        onQuantityChange(newQuantity)
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Azalt",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
                
                // Miktar göstergesi
                Text(
                    text = if (item.product.isByWeight) "${item.quantity} kg" else "${item.quantity.toInt()} adet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )
                
                // Arttır butonu
                IconButton(
                    onClick = {
                        val increment = if (item.product.isByWeight) 0.5 else 1.0
                        onQuantityChange(item.quantity + increment)
                    },
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0))
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Arttır",
                        tint = Color.Black,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Toplam fiyat
            Text(
                text = "Toplam: ${String.format("%.2f ₺", item.product.price * item.quantity)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                textAlign = TextAlign.End
            )
            
            // Eğer listeden fazla alınmışsa uyarı göster
            if (isExceedingRequired && requiredQuantity != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .background(
                            color = Color(0xFFFFEBEE),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Uyarı",
                        tint = Color(0xFFE57373),
                        modifier = Modifier.size(16.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Text(
                        text = "Listende bu üründen ${requiredQuantity.toString().replace(".0", "")} " +
                              (if (item.product.isByWeight) "kg" else "adet") +
                              " isteniyor!",
                        fontSize = 12.sp,
                        color = Color(0xFFE57373)
                    )
                }
            }
        }
    }
} 