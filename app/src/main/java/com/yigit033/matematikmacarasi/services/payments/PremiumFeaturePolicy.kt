package com.yigit033.matematikmacarasi.services.payments

/**
 * Stable feature matrix. Entitlement is owned exclusively by PurchaseManager,
 * never by a second flag or a game-specific purchase.
 */
enum class PremiumFeature {
    MARKET_FULL_JOURNEY, MARKET_EXTENDED_SHOPS, MARKET_STALL,
    PREMIUM_AVATARS, ALTERNATE_APP_ICONS, UNLIMITED_MAZE,
    DETAILED_GAME_STATISTICS, DAILY_PREMIUM_BONUS, AD_FREE
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

/**
 * An adult confirmation gate limits accidental child purchase entry.
 * A maths question is a deterrent only; it cannot establish identity or age.
 * Play's own purchase approval/authentication still applies at checkout.
 */
object AdultPurchaseChallenge {
    data class Question(val left:Int,val right:Int) {
        val answer: Int get() = left*right
        val prompt:String get() = "$left × $right = ?"
    }
    fun create(seed:Int):Question {
        val value=seed.toLong().and(0x7fffffffL)
        return Question(12+(value%13).toInt(),7+((value/13)%9).toInt())
    }
    fun accepted(question:Question,answer:String,adultConfirmed:Boolean):Boolean =
        adultConfirmed && answer.trim().toIntOrNull()==question.answer
}
