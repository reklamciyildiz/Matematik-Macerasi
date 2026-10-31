package com.yigit033.matematikmacarasi.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameCompletionIntegrityTest {
    @Test fun gameRunCannotBeSettledTwiceButReplayCan() {
        val gate = GameRunCompletionGate()
        assertTrue(gate.tryFinish())
        assertFalse(gate.tryFinish())
        assertFalse(gate.tryFinish())
        gate.reset()
        assertTrue(gate.tryFinish())
        assertFalse(gate.tryFinish())
    }

    @Test fun lowerReplayDoesNotReduceBestOrCreateNewCurrency() {
        var best = 0
        var credited = 0
        for (earned in listOf(3, 1, 3, 2)) {
            val award = GameStarAwardPolicy.evaluate(best, earned)
            credited += award.additionalStars
            best = award.bestStars
        }
        assertEquals(3, best)
        assertEquals(3, credited)
    }

    @Test fun improvingScoreAwardsOnlyDifference() {
        var best = 0
        var credited = 0
        for (earned in listOf(1, 3, 2, 3)) {
            val award = GameStarAwardPolicy.evaluate(best, earned)
            credited += award.additionalStars
            best = award.bestStars
        }
        assertEquals(3, best)
        assertEquals(3, credited)
    }

    @Test fun zeroScoreCannotReduceExistingRecord() {
        assertEquals(GameStarAwardPolicy.Award(3, 0), GameStarAwardPolicy.evaluate(3, 0))
        assertEquals(GameStarAwardPolicy.Award(0, 0), GameStarAwardPolicy.evaluate(0, 0))
    }

    @Test(expected = IllegalArgumentException::class)
    fun negativeStarsAreRejected() {
        GameStarAwardPolicy.evaluate(3, -1)
    }
}
