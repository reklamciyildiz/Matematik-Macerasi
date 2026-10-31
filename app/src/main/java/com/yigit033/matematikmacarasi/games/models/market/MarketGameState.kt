package com.yigit033.matematikmacarasi.games.models.market

/**
 * Market oyununun mevcut aşamasını tanımlayan enum sınıfı
 */
enum class MarketGamePhase {
    SHOPPING,  // Alışveriş yapma aşaması
    CHECKOUT,  // Kasada ödeme aşaması
    COMPLETED, // Oyun tamamlandı
    FAILED     // Oyun başarısız oldu
}

/**
 * Market oyunu durumunu temsil eden veri sınıfı
 * 
 * @property level Oyun seviyesi
 * @property availableProducts Marketteki tüm ürünler
 * @property shoppingCart Alışveriş sepeti
 * @property phase Oyunun mevcut aşaması
 * @property timeRemaining Kalan süre (saniye)
 * @property score Oyun puanı
 * @property stars Kazanılan yıldız sayısı (1-3)
 * @property cashGiven Kasada verilen para
 * @property failReason Başarısızlık nedeni
 */
data class MarketGameState(
    val level: ShoppingList,
    val availableProducts: List<Product>,
    val shoppingCart: ShoppingCart = ShoppingCart(),
    val phase: MarketGamePhase = MarketGamePhase.SHOPPING,
    val timeRemaining: Int = 0,
    val score: Int = 0,
    val stars: Int = 0,
    val cashGiven: Double = 0.0, // Kasada ödenen para
    val failReason: String = ""
) 