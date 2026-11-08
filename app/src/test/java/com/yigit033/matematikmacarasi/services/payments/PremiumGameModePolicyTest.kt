package com.yigit033.matematikmacarasi.services.payments

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumGameModePolicyTest {
    @Test fun onlyBasicDifficultyIsFreeAndBothAdvancedDifficultiesNeedPremium() {
        for(id in listOf("fractions","time_tower","pattern_forest")) {
            assertTrue(PremiumGameModePolicy.canStart(id,0,false))
            assertFalse(PremiumGameModePolicy.requiresPremium(id,0))
            for(difficulty in 1..2) {
                assertTrue(PremiumGameModePolicy.requiresPremium(id,difficulty))
                assertFalse(PremiumGameModePolicy.canStart(id,difficulty,false))
                assertTrue(PremiumGameModePolicy.canStart(id,difficulty,true))
            }
            // A stored advanced checkpoint must remain restricted if access lapses.
            assertFalse(PremiumGameModePolicy.canStart(id,1,false))
            assertFalse(PremiumGameModePolicy.canStart(id,2,false))
        }
    }
    @Test fun otherGamesRemainFreeAndSchoolGradeIsNotLocked() {
        for(id in listOf("add","subtract","geometry","hunt","forest","space","balloon"))
            for(difficulty in 0..2) {
                assertTrue(PremiumGameModePolicy.canStart(id,difficulty,false))
            }
    }
}
