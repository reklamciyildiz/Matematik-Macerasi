package com.yigit033.matematikmacarasi.services.payments

/** Optional expert challenges, not school-grade or core lesson locks.
 * The complete Basic difficulty is free; Medium and Hard are Premium. Checkpoints are
 * NEVER deleted when an entitlement changes.
 */
object PremiumGameModePolicy {
    private val advancedGames=setOf("fractions","time_tower","pattern_forest",
        "operations","add","subtract","multiply","divide","mixed",
        "geometry","balloon","space","hunt")
    const val FIRST_PREMIUM_DIFFICULTY_INDEX=1
    /** Free users should never land on a locked default difficulty. */
    fun defaultDifficulty(grade:Int,hasPremium:Boolean):Int =
        if(!hasPremium || grade<=2)0 else 1
    /** Separate storage only when a prior Premium checkpoint is inaccessible.
     * Basic progress then remains playable and the paid checkpoint untouched.
     */
    fun freeCheckpointStore(gameId:String):String=when(gameId) {
        "fractions"->"fraction_crystals_free_checkpoint_v1"
        "time_tower"->"time_tower_free_checkpoint_v1"
        "pattern_forest"->"pattern_forest_free_checkpoint_v1"
        "geometry"->"geometry_castle_free_checkpoint_v1"
        "balloon"->"balloon_adventure_free_checkpoint_v1"
        "space"->"space_expedition_free_checkpoint_v1"
        "hunt"->"number_hunt_worlds_free_v3"
        else->error("Unknown game: "+gameId)
    }
    fun requiresPremium(gameId:String,difficulty:Int):Boolean =
        gameId in advancedGames && difficulty>=FIRST_PREMIUM_DIFFICULTY_INDEX
    /** Five arithmetic operation difficulties share the same paywall policy. */
    fun allowsArithmeticDifficulty(difficulty:Int,hasPremium:Boolean):Boolean =
        canStart("operations",difficulty,hasPremium)
    fun canStart(gameId:String,difficulty:Int,hasPremium:Boolean):Boolean =
        hasPremium || !requiresPremium(gameId,difficulty)
}
