package com.yigit033.matematikmacarasi.games.models.market

import androidx.annotation.DrawableRes
import com.yigit033.matematikmacarasi.R

/**
 * Ürün kategorilerini tanımlayan enum sınıfı
 */
enum class ProductCategory {
    ALL,        // Tüm ürünler
    FRUIT,      // Meyve
    VEGETABLE,  // Sebze
    DAIRY,      // Süt ürünleri
    BAKERY,     // Fırın ürünleri
    SNACKS;     // Atıştırmalıklar
    
    /**
     * Kategori için ekranda görüntülenecek ismi döndürür
     */
    val displayName: String
        get() = when (this) {
            ALL -> "Tümü"
            FRUIT -> "Meyve"
            VEGETABLE -> "Sebze"
            BAKERY -> "Fırın"
            DAIRY -> "Süt Ürünleri"
            SNACKS -> "Atıştırmalık"
        }
    
    /**
     * Kategori için kullanılacak icon kaynağını döndürür
     */
    @get:DrawableRes
    val iconResId: Int
        get() = when (this) {
            ALL -> R.drawable.ic_shopping_cart
            FRUIT -> R.drawable.ic_fruit_category
            VEGETABLE -> R.drawable.ic_vegetable_category
            BAKERY -> R.drawable.ic_bakery_category
            DAIRY -> R.drawable.ic_dairy_category
            SNACKS -> R.drawable.ic_snacks_category
        }
}

/**
 * Market ürünlerini temsil eden veri sınıfı
 * 
 * @property id Ürün benzersiz kimliği
 * @property name Ürün adı
 * @property price Ürün fiyatı
 * @property imageResId Ürün görseli kaynak ID'si
 * @property category Ürün kategorisi
 * @property isByWeight Ürünün kilogram ile mi satıldığı
 * @property discount İndirim oranı (0-100 arası)
 */
data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val imageResId: Int,
    val category: ProductCategory,
    val isByWeight: Boolean = false,
    val discount: Int = 0 // Yüzde olarak indirim (0-100 arası)
) {
    /**
     * İndirimli fiyatı hesaplar
     * @return İndirimli fiyat
     */
    fun getDiscountedPrice(): Double {
        return price * (1 - (discount / 100.0))
    }
    
    /**
     * Biçimlendirilmiş fiyat bilgisini döndürür
     * @return Biçimlendirilmiş fiyat metni
     */
    fun getFormattedPrice(): String {
        return if (isByWeight) {
            String.format("%.2f TL/kg", price)
        } else {
            String.format("%.2f TL", price)
        }
    }
} 