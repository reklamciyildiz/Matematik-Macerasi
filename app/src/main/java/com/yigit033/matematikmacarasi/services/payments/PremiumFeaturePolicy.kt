package com.yigit033.matematikmacarasi.services.payments

/**
 * Stable feature matrix. Entitlement is owned exclusively by PurchaseManager,
 * never by a second flag or a game-specific purchase.
 */
enum class PremiumFeature {
    MARKET_FULL_JOURNEY, MARKET_EXTENDED_SHOPS, MARKET_STALL,
    PREMIUM_AVATARS, ALTERNATE_APP_ICONS, UNLIMITED_MAZE,
    DETAILED_GAME_STATISTICS, DAILY_PREMIUM_BONUS, AD_FREE,
    ADVANCED_GAME_MODES,
    FULL_LEARNING_LIBRARY
}

object PremiumFeaturePolicy {
    const val FREE_MARKET_SHOPS = 3
    const val PREMIUM_MARKET_SHOPS = 10
    val features: Set<PremiumFeature> = PremiumFeature.entries.toSet()
    fun allowed(feature: PremiumFeature,isPremium:Boolean):Boolean = isPremium
    fun marketShopCount(isPremium:Boolean):Int =
        if(isPremium) PREMIUM_MARKET_SHOPS else FREE_MARKET_SHOPS
    fun canEnterMarketShop(index:Int,isPremium:Boolean):Boolean =
        index in 0 until marketShopCount(isPremium)
}
