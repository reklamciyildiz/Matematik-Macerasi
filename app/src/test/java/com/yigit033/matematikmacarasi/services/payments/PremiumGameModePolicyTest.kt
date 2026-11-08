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
    @Test fun inaccessiblePaidCheckpointsDoNotBlockASeparateBasicSave() {
        val slots=mapOf(
            "fractions" to "fraction_crystals_v1",
            "time_tower" to "time_tower_v1",
            "pattern_forest" to "pattern_forest_v1"
        )
        for((game,paidSlot) in slots) {
            val freeSlot=PremiumGameModePolicy.freeCheckpointStore(game)
            assertFalse(freeSlot==paidSlot)
            assertTrue(freeSlot.contains("free_checkpoint"))
        }
        assertTrue(slots.keys.map(PremiumGameModePolicy::freeCheckpointStore).distinct().size==3)
    }
    @Test fun otherGamesRemainFreeAndSchoolGradeIsNotLocked() {
        for(id in listOf("add","subtract","geometry","hunt","forest","space","balloon"))
            for(difficulty in 0..2) {
                assertTrue(PremiumGameModePolicy.canStart(id,difficulty,false))
            }
    }
}
