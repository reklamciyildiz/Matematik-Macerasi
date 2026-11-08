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
    @Test fun defaultDifficultyNeverPointsFreeUsersAtLockedModes() {
        for(grade in 1..8) {
            assertTrue(PremiumGameModePolicy.defaultDifficulty(grade,false)==0)
            assertTrue(PremiumGameModePolicy.defaultDifficulty(grade,true)==
                if(grade<=2)0 else 1)
        }
    }
    @Test fun arithmeticOffersFullBasicWithoutBuyingButAdvancedRequiresPremium() {
        assertTrue(PremiumGameModePolicy.allowsArithmeticDifficulty(0,false))
        for (level in 1..4) {
            assertFalse(PremiumGameModePolicy.allowsArithmeticDifficulty(level,false))
            assertTrue(PremiumGameModePolicy.allowsArithmeticDifficulty(level,true))
        }
    }
    @Test fun worldBasedAdventureUsesSeparateBasicSaveWithSameRightChecks() {
        val names=listOf("geometry","balloon","space","hunt")
            .map(PremiumGameModePolicy::freeCheckpointStore)
        assertTrue(names.distinct().size==4)
        for(game in listOf("geometry","balloon","space","hunt")) {
            assertTrue(PremiumGameModePolicy.canStart(game,0,false))
            for(tier in 1..2){
                assertFalse(PremiumGameModePolicy.canStart(game,tier,false))
                assertTrue(PremiumGameModePolicy.canStart(game,tier,true))
            }
        }
    }
    @Test fun otherGamesRemainFreeAndSchoolGradeIsNotLocked() {
        for(id in listOf("add","subtract","forest"))
            for(difficulty in 0..2) {
                assertTrue(PremiumGameModePolicy.canStart(id,difficulty,false))
            }
    }
}
