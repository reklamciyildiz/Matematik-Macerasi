package com.yigit033.matematikmacarasi.services

import org.junit.Assert.assertEquals
import org.junit.Test

class AdventureAnalyticsMathTest {
    @Test fun directAndLevelRecordsAreNotDoubleCounted() {
        assertEquals(3, AdventureAnalyticsMath.canonicalValue(3, listOf(1, 2)))
        assertEquals(5, AdventureAnalyticsMath.canonicalValue(3, listOf(2, 3)))
    }
    @Test fun alternateLegacyFormatsAreNotDoubleCounted() {
        assertEquals(4, AdventureAnalyticsMath.canonicalValue(0,
            listOf(2, 2), listOf(2, 2)))
    }
    @Test fun historicOnlyRecordsAreStillVisible() {
        assertEquals(7, AdventureAnalyticsMath.canonicalValue(0, listOf(3, 4)))
    }
    @Test fun corruptNegativeValuesCannotProduceNegativeStats() {
        assertEquals(2, AdventureAnalyticsMath.canonicalValue(-7, listOf(-3, 2)))
    }
    @Test fun sumsSaturateInsteadOfOverflowing() {
        assertEquals(Int.MAX_VALUE,
            AdventureAnalyticsMath.safeSum(listOf(Int.MAX_VALUE, 9)))
    }
}
