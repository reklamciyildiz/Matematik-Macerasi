package com.yigit033.matematikmacarasi.services

import com.yigit033.matematikmacarasi.GameType

/** Only grade-appropriate newly generated daily quests; existing progress stays. */
object DailyQuestGradePolicy {
    fun eligibleForGrade(grade:Int):List<GameType> {
        require(grade in 1..8)
        return GameType.entries.filter { game ->
            when(game) {
                GameType.MULTIPLICATION_GAME, GameType.DIVISION_GAME -> grade>=2
                GameType.SPACE_MATH_ADVENTURE -> grade<=4
                GameType.MARKET_SIMULATOR -> grade>=2
                else -> true
            }
        }
    }
}
