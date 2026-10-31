package com.yigit033.matematikmacarasi.games.models.market

import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.mutableStateMapOf

/**
 * Alışveriş sepetindeki bir ürünü temsil eden veri sınıfı
 * 
 * @property product Sepetteki ürün
 * @property quantity Ürün miktarı (adet veya kg)
 */
data class CartItem(
    val product: Product,
    val quantity: Double = 1.0
) {
    /**
     * Ürün için toplam fiyatı hesaplar
     * @return Toplam fiyat
     */
    fun getTotalPrice(): Double {
        return product.getDiscountedPrice() * quantity
    }
}

/**
 * Alışveriş sepetini temsil eden sınıf
 */
class ShoppingCart {
    // Ürün id'si -> CartItem şeklinde map
    private val _items: SnapshotStateMap<Int, CartItem> = mutableStateMapOf()
    val items: Map<Int, CartItem> get() = _items
    
    /**
     * Sepete ürün ekler
     * @param product Eklenecek ürün
     * @param quantity Eklenecek miktar
     */
    fun addItem(product: Product, quantity: Double = 1.0) {
        val existing = _items[product.id]
        if (existing != null) {
            _items[product.id] = existing.copy(quantity = existing.quantity + quantity)
        } else {
            _items[product.id] = CartItem(product, quantity)
        }
    }
    
    /**
     * Sepetten ürün çıkarır
     * @param productId Çıkarılacak ürünün ID'si
     */
    fun removeItem(productId: Int) {
        _items.remove(productId)
    }
    
    /**
     * Sepetteki bir ürünün miktarını günceller
     * @param productId Güncellenecek ürünün ID'si
     * @param quantity Yeni miktar
     */
    fun updateQuantity(productId: Int, quantity: Double) {
        _items[productId]?.let { item ->
            if (quantity <= 0) {
                removeItem(productId)
            } else {
                _items[productId] = item.copy(quantity = quantity)
            }
        }
    }
    
    /**
     * Sepetteki ürünlerin toplam fiyatını hesaplar
     * @return Toplam fiyat
     */
    fun getTotalPrice(): Double {
        return _items.values.sumOf { it.getTotalPrice() }
    }
    
    /**
     * Sepetteki ürün sayısını döndürür
     * @return Ürün sayısı
     */
    fun getItemCount(): Int {
        return _items.size
    }
    
    /**
     * Sepeti temizler
     */
    fun clear() {
        _items.clear()
    }
    
    /**
     * Belirli bir ürünün sepette olup olmadığını kontrol eder
     * @param productId Kontrol edilecek ürün ID'si
     * @return Ürün sepette varsa true, yoksa false
     */
    fun contains(productId: Int): Boolean {
        return _items.containsKey(productId)
    }
} 