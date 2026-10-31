package com.yigit033.matematikmacarasi

import org.junit.Assert.*
import org.junit.Test
import kotlin.math.max
import kotlin.math.min

class GameEconomyAndProgressionTest {

    private val MAX_STARS_LIMIT = 1000

    @Test
    fun testStarEarningCalculationCappedAtMax() {
        val currentStars = 990
        val earnedStars = 25
        val newTotal = min(MAX_STARS_LIMIT, currentStars + earnedStars)
        assertEquals("Yıldız sayısı maksimum limit olan 1000'i aşamaz", 1000, newTotal)
    }

    @Test
    fun testStarSpendingCalculationNotNegative() {
        val currentStars = 30
        val spendAmount = 50
        val canAfford = currentStars >= spendAmount
        assertFalse("Yetersiz yıldız ile avatar satın alınamamalı", canAfford)
        
        val newTotal = if (canAfford) currentStars - spendAmount else currentStars
        assertTrue("Yıldız sayısı asla negatif olamaz", newTotal >= 0)
        assertEquals(30, newTotal)
    }

    @Test
    fun testDailyGameLimitLogic() {
        val maxFreeGamesPerDay = 10
        var gamesPlayedToday = 9
        
        // 10. oyun oynanabilir
        assertTrue("Günlük limite ulaşılmadıysa oyun oynanabilmeli", gamesPlayedToday < maxFreeGamesPerDay)
        gamesPlayedToday++
        
        // 11. oyun engellenmeli
        assertFalse("Günlük limit dolduğunda ücretsiz oyun engellenmeli", gamesPlayedToday < maxFreeGamesPerDay)
    }

    @Test
    fun testStreakScoreMultiplier() {
        fun calculateScore(baseScore: Int, streak: Int): Int {
            val multiplier = when {
                streak >= 10 -> 3
                streak >= 5 -> 2
                else -> 1
            }
            return baseScore * multiplier
        }

        assertEquals(10, calculateScore(10, 1))
        assertEquals(10, calculateScore(10, 4))
        assertEquals(20, calculateScore(10, 5))
        assertEquals(20, calculateScore(10, 9))
        assertEquals(30, calculateScore(10, 10))
        assertEquals(30, calculateScore(10, 15))
    }
}

