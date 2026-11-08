package com.yigit033.matematikmacarasi.services.payments

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumGameModePolicyTest {
    @Test fun twoCompleteBaseDifficultiesStayFree() {
        for(id in listOf("fractions","time_tower","pattern_forest")) {
            for(difficulty in 0..1) {
                assertTrue(PremiumGameModePolicy.canStart(id,difficulty,false))
                assertFalse(PremiumGameModePolicy.requiresPremium(id,difficulty))
            }
            assertFalse(PremiumGameModePolicy.canStart(id,2,false))
            assertTrue(PremiumGameModePolicy.canStart(id,2,true))
        }
    }
    @Test fun otherGamesRemainFreeAndSchoolGradeIsNotLocked() {
        for(id in listOf("add","subtract","geometry","hunt","forest","space","balloon"))
            for(difficulty in 0..2) {
                assertTrue(PremiumGameModePolicy.canStart(id,difficulty,false))
            }
    }
}
