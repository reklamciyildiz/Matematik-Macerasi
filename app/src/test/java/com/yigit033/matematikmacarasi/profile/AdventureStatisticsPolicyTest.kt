package com.yigit033.matematikmacarasi.profile

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdventureStatisticsPolicyTest {
    @Test fun freePlayerCanSeeOnlyBasicSummary() {
        assertTrue(AdventureStatisticsPolicy.showValue(AdventureStatistic.STARS, false))
        assertTrue(AdventureStatisticsPolicy.showValue(AdventureStatistic.COMPLETIONS, false))
        assertFalse(AdventureStatisticsPolicy.showValue(AdventureStatistic.PLAY_TIME, false))
        assertFalse(AdventureStatisticsPolicy.showValue(AdventureStatistic.FAVORITE, false))
        AdventureStatistic.entries.forEach {
            assertFalse(AdventureStatisticsPolicy.allowBreakdown(it, false))
        }
    }

    @Test fun premiumPlayerSeesAllValuesAndBreakdowns() {
        AdventureStatistic.entries.forEach {
            assertTrue(AdventureStatisticsPolicy.showValue(it, true))
            assertTrue(AdventureStatisticsPolicy.allowBreakdown(it, true))
        }
    }
}
