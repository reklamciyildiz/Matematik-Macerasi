package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.models.market.Product
import com.yigit033.matematikmacarasi.games.models.market.ProductCategory
import com.yigit033.matematikmacarasi.games.models.market.ShoppingCart

/**
 * Ürün ızgarası görünümü
 * 
 * @param products Tüm ürünler
 * @param selectedCategory Seçili kategori
 * @param onProductClick Ürüne tıklandığında çağrılacak fonksiyon
 * @param modifier Modifier
 */
@Composable
fun ProductGrid(
    products: List<Product>,
    selectedCategory: ProductCategory,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    // Grid durumunu performans için sakla
    val gridState = rememberLazyGridState()
    
    // Ekran genişliğine göre dinamik sütun sayısı hesaplanması
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val productCardOptimalWidth = 110.dp
    val columns = (screenWidth / productCardOptimalWidth).toInt().coerceAtLeast(2)
    
    // Filtre edilmiş ürünleri hesapla ve önbelleğe al
    val filteredProducts = remember(products, selectedCategory) {
        if (selectedCategory == ProductCategory.ALL) {
            products
        } else {
            products.filter { it.category == selectedCategory }
        }
    }
    
    // Eğer filtrelenmiş ürün yoksa mesaj göster
    if (filteredProducts.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Bu kategoride ürün bulunamadı",
                fontSize = 16.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(columns),
            state = gridState,
            contentPadding = PaddingValues(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = modifier
        ) {
            items(
                items = filteredProducts,
                // Performans için stable key kullan
                key = { product -> product.id }
            ) { product ->
                ProductCard(
                    product = product,
                    onClick = { onProductClick(product) }
                )
            }
        }
    }
}

/**
 * Ürün kartı bileşeni
 * 
 * @param product Ürün
 * @param onClick Tıklandığında çağrılacak fonksiyon
 * @param modifier Modifier
 */
@Composable
private fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(0.8f) // Sabit en/boy oranı
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            // Ürün görseli
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f)
                    .padding(8.dp)
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
                    )
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
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .padding(vertical = 4.dp)
            ) {
                // Ürün adı
                Text(
                    text = product.name,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Fiyat bilgisi
                Text(
                    text = String.format("%.2f ₺%s", product.price, if (product.isByWeight) "/kg" else ""),
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
} 