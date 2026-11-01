package com.yigit033.matematikmacarasi.games.interactive

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpaceExpeditionTouchTest {
    @get:Rule val composeRule=createComposeRule()
    @Test fun launchPlanetAndSteerSpaceship() {
        val state=mutableStateOf<SpaceRun?>(null)
        composeRule.setContent {
            SpaceExpeditionScreen(
                run=state.value,recommended=LearningTier.TEMEL,
                records=emptyMap(),historicalStars=emptyMap(),feedback="",
                onBegin={planet,tier->
                    state.value=SpaceExpeditionEngine.begin(99L,planet,tier);true
                },
                onSteer={lane->state.value=SpaceExpeditionEngine.steer(state.value!!,lane)},
                onIntercept={},onFuel={},onIgnite={},onConnect={},
                onHint={},onNext={},onExit={}
            )
        }
        composeRule.onNodeWithTag("space_planet_AQUA").performScrollTo().performClick()
        composeRule.onNodeWithText("🚀 Okyanus Nebulası Keşfini Başlat")
            .performScrollTo().performClick()
        composeRule.onNodeWithTag("space_lane_0").performScrollTo().performClick()
        composeRule.onNodeWithTag("space_pause").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(SpacePlanet.AQUA,state.value!!.planet)
            assertEquals(0,state.value!!.lane)
        }
    }
    @Test fun reactorPumpAndCircuitNodesAreInteractive() {
        val reactorState=mutableStateOf(
            SpaceRun(123L,SpacePlanet.TERRA,LearningTier.TEMEL,
                stage=1,attempts=1,firstTry=1)
        )
        composeRule.setContent {
            SpaceExpeditionScreen(
                run=reactorState.value,recommended=LearningTier.TEMEL,
                records=emptyMap(),historicalStars=emptyMap(),feedback="",
                onBegin={_,_->false},onSteer={},onIntercept={},
                onFuel={delta->reactorState.value=SpaceExpeditionEngine.adjustFuel(
                    reactorState.value,delta)},
                onIgnite={reactorState.value=SpaceExpeditionEngine.ignite(reactorState.value)},
                onConnect={},onHint={},onNext={},onExit={}
            )
        }
        composeRule.onNodeWithText("▶ Kaydedilen Göreve Dön")
            .performScrollTo().performClick()
        composeRule.onNodeWithTag("space_fuel_5").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(5,reactorState.value.charge)
            assertTrue(SpaceExpeditionEngine.valid(reactorState.value))
        }
    }
}
