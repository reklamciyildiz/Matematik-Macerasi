package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Scale
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.models.market.Product
import com.yigit033.matematikmacarasi.games.models.market.ProductCategory

/**
 * Market ürünlerini göstermek için kullanılan kart bileşeni
 * 
 * @param product Gösterilecek ürün
 * @param isInShoppingList Ürün alışveriş listesinde mi
 * @param isInCart Ürün sepette mi
 * @param onClick Ürüne tıklandığında çağrılacak fonksiyon
 */
@Composable
fun ProductItemView(
    product: Product,
    isInShoppingList: Boolean = false,
    isInCart: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isInCart -> Color(0xFFE3F2FD) // Sepette ise hafif mavi
                isInShoppingList -> Color(0xFFF9FBE7) // Alışveriş listesinde ise hafif yeşil
                else -> Color.White
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ürün resmi
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (product.category) {
                            ProductCategory.FRUIT -> Color(0xFFE8F5E9)
                            ProductCategory.VEGETABLE -> Color(0xFFE8F5E9)
                            ProductCategory.BAKERY -> Color(0xFFFFF3E0)
                            ProductCategory.DAIRY -> Color(0xFFE1F5FE)
                            ProductCategory.SNACKS -> Color(0xFFFCE4EC)
                            else -> Color(0xFFF5F5F5)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Vector drawable'ları Icon olarak göster
                Icon(
                    painter = painterResource(id = product.imageResId),
                    contentDescription = product.name,
                    modifier = Modifier.size(48.dp),
                    tint = Color.Unspecified
                )
            }
            
            // Ürün bilgileri
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = product.name,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Kilogram bilgisi
                if (product.isByWeight) {
                    Text(
                        text = "Kg başına fiyat",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                
                // İndirim varsa eski fiyat ve yeni fiyat gösterimi
                if (product.discount > 0) {
                    Row {
                        Text(
                            text = String.format("%.2f TL", product.price),
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textDecoration = TextDecoration.LineThrough
                        )
                        
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        Text(
                            text = String.format("%.2f TL", product.getDiscountedPrice()),
                            fontSize = 14.sp,
                            color = Color(0xFFE91E63),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Text(
                        text = "%${product.discount} indirim!",
                        fontSize = 12.sp,
                        color = Color(0xFFE91E63)
                    )
                } else {
                    // Normal fiyat gösterimi
                    Text(
                        text = product.getFormattedPrice(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1976D2)
                    )
                }
            }
            
            // Sepette ise veya alışveriş listesindeyse gösterge
            if (isInCart || isInShoppingList) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isInCart) Color(0xFF1976D2)
                            else Color(0xFF4CAF50)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isInCart) 
                                R.drawable.ic_check 
                            else 
                                R.drawable.ic_shopping_cart
                        ),
                        contentDescription = if (isInCart) "Sepette" else "Listede",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
} 