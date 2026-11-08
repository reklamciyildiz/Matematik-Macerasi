package com.yigit033.matematikmacarasi.services.payments

/** Optional expert challenges, not school-grade or core lesson locks.
 * The complete Basic difficulty is free; Medium and Hard are Premium. Checkpoints are
 * NEVER deleted when an entitlement changes.
 */
object PremiumGameModePolicy {
    private val advancedGames=setOf("fractions","time_tower","pattern_forest")
    const val FIRST_PREMIUM_DIFFICULTY_INDEX=1
    fun requiresPremium(gameId:String,difficulty:Int):Boolean =
        gameId in advancedGames && difficulty>=FIRST_PREMIUM_DIFFICULTY_INDEX
    fun canStart(gameId:String,difficulty:Int,hasPremium:Boolean):Boolean =
        hasPremium || !requiresPremium(gameId,difficulty)
}
