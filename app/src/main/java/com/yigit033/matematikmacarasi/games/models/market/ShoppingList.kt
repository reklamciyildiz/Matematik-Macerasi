package com.yigit033.matematikmacarasi.games.models.market

/**
 * Alışveriş listesindeki bir ürünü temsil eden veri sınıfı
 * 
 * @property product Satın alınması gereken ürün
 * @property requiredQuantity Gerekli miktar
 * @property isCompleted Bu öğenin tamamlanıp tamamlanmadığı
 */
data class ShoppingListItem(
    val product: Product,
    val requiredQuantity: Double,
    var isCompleted: Boolean = false
)

/**
 * Alışveriş listesini temsil eden veri sınıfı
 * 
 * @property items Alışveriş listesindeki öğeler
 * @property budget Alışveriş bütçesi
 * @property levelName Seviye adı
 * @property timeInSeconds Seviye için zaman sınırı (saniye)
 * @property name Görüntülenecek seviye adı
 * @property description Seviye açıklaması
 * @property difficulty Zorluk seviyesi (1-5)
 */
data class ShoppingList(
    val items: List<ShoppingListItem>,
    val budget: Double,
    val levelName: String,
    val timeInSeconds: Int,
    val name: String = levelName,
    val description: String = "",
    val difficulty: Int = 1
) 