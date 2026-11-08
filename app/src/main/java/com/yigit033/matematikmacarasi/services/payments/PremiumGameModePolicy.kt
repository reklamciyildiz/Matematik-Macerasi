package com.yigit033.matematikmacarasi.services.payments

/** Optional expert challenges, not school-grade or core lesson locks.
 * Two complete lower difficulties remain free. In-progress checkpoints are
 * NEVER deleted when an entitlement changes.
 */
object PremiumGameModePolicy {
    private val advancedGames=setOf("fractions","time_tower","pattern_forest")
    const val PREMIUM_DIFFICULTY_INDEX=2
    fun requiresPremium(gameId:String,difficulty:Int):Boolean =
        gameId in advancedGames && difficulty==PREMIUM_DIFFICULTY_INDEX
    fun canStart(gameId:String,difficulty:Int,hasPremium:Boolean):Boolean =
        hasPremium || !requiresPremium(gameId,difficulty)
}
