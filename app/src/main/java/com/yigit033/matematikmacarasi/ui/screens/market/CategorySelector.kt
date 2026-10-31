package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.models.market.ProductCategory

/**
 * Ürün kategorilerini seçmek için kullanılan bileşen
 * 
 * @param selectedCategory Seçili kategori
 * @param onCategorySelected Kategori seçildiğinde çağrılacak fonksiyon
 * @param modifier Modifier
 */
@Composable
fun CategorySelector(
    selectedCategory: ProductCategory,
    onCategorySelected: (ProductCategory) -> Unit,
    modifier: Modifier = Modifier
) {
    // Performans için lazy list state'i hatırla
    val lazyListState = rememberLazyListState()
    
    // Kategorileri önbelleğe al
    val categories = remember { ProductCategory.values().toList() }
    
    LazyRow(
        modifier = modifier.padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        state = lazyListState
    ) {
        items(
            items = categories,
            // Performans için key kullan - kategoriler değişmeyeceği için ordinal güvenli
            key = { category -> category.ordinal }
        ) { category ->
            CategoryItem(
                category = category,
                isSelected = category == selectedCategory,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

/**
 * Kategori öğesi
 * 
 * @param category Ürün kategorisi
 * @param isSelected Seçili mi
 * @param onClick Tıklandığında çağrılacak fonksiyon
 */
@Composable
private fun CategoryItem(
    category: ProductCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        // Kategori ikonu
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    else Color(0xFFF5F5F5)
                )
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = CircleShape
                )
                .padding(8.dp)
        ) {
            // Kategori ikonunu göster
            Icon(
                painter = painterResource(id = category.iconResId),
                contentDescription = category.displayName,
                tint = when (category) {
                    ProductCategory.ALL -> if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF673AB7)
                    ProductCategory.FRUIT -> if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFFF9800)
                    ProductCategory.VEGETABLE -> if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF4CAF50)
                    ProductCategory.DAIRY -> if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF2196F3)
                    ProductCategory.BAKERY -> if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFFF5722)
                    ProductCategory.SNACKS -> if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFF44336)
                },
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Kategori adı
        Text(
            text = category.displayName,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
} 