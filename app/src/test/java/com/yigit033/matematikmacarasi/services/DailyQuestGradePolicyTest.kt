package com.yigit033.matematikmacarasi.services

import com.yigit033.matematikmacarasi.GameType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyQuestGradePolicyTest {
    @Test fun gradeAppropriatePoolsContainAtLeastSevenUniqueGames() {
        for(grade in 1..8) {
            val games=DailyQuestGradePolicy.eligibleForGrade(grade)
            assertTrue("grade $grade",games.size>=7)
            assertEquals(games.size,games.distinct().size)
            assertTrue(GameType.ADDITION_GAME in games)
            assertTrue(GameType.NUMBER_HUNT_GAME in games)
            assertTrue(GameType.PATTERN_FOREST in games)
            assertEquals(grade>=2,GameType.MULTIPLICATION_GAME in games)
            assertEquals(grade>=2,GameType.DIVISION_GAME in games)
            assertEquals(grade>=2,GameType.MARKET_SIMULATOR in games)
            assertEquals(grade<=4,GameType.SPACE_MATH_ADVENTURE in games)
        }
    }
    @Test(expected=IllegalArgumentException::class)
    fun gradesOutsideSchoolRangeAreRejected() {
        DailyQuestGradePolicy.eligibleForGrade(0)
    }
}
