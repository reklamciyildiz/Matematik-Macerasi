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

/** Click dispatch on Android; the CI compiles this APK, a device must execute it. */
@RunWith(AndroidJUnit4::class)
class SpaceExpeditionTouchTest {
    @get:Rule val composeRule=createComposeRule()

    @Test fun planetLaunchShipSteeringAndLaserTriggerAreClickable() {
        val state=mutableStateOf<SpaceRun?>(null)
        composeRule.setContent {
            SpaceExpeditionScreen(
                run=state.value,recommended=LearningTier.TEMEL,
                records=emptyMap(),historicalStars=emptyMap(),feedback="",
                onBegin={planet,tier->
                    state.value=SpaceExpeditionEngine.begin(99L,planet,tier);true
                },
                onSteer={},onIntercept={},
                onShipPosition={x->
                    state.value=SpaceExpeditionEngine.position(state.value!!,x)
                },
                onFireHit={value->
                    state.value=SpaceExpeditionEngine.fireAt(state.value!!,value)
                },
                onFuel={},onIgnite={},onConnect={},
                onHint={},onNext={},onExit={}
            )
        }
        composeRule.onNodeWithTag("space_planet_AQUA").performScrollTo().performClick()
        composeRule.onNodeWithText("🚀 Okyanus Nebulası Keşfini Başlat")
            .performScrollTo().performClick()
        composeRule.onNodeWithTag("space_pause").performScrollTo().performClick()
        composeRule.onNodeWithTag("space_steer_left").performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(SpacePlanet.AQUA,state.value!!.planet)
            assertTrue(state.value!!.shipX<.5f)
        }
        composeRule.onNodeWithTag("space_fire").performScrollTo().performClick()
        composeRule.onNodeWithTag("space_combat_arena").assertExists()
    }

    @Test fun reactorMissionAlsoHasTheSameRealShootingControls() {
        val state=mutableStateOf(
            SpaceRun(123L,SpacePlanet.TERRA,LearningTier.TEMEL,
                stage=1,attempts=1,firstTry=1)
        )
        composeRule.setContent {
            SpaceExpeditionScreen(
                run=state.value,recommended=LearningTier.TEMEL,
                records=emptyMap(),historicalStars=emptyMap(),feedback="",
                onBegin={_,_->false},onSteer={},onIntercept={},
                onShipPosition={},onFireHit={value->
                    state.value=SpaceExpeditionEngine.fireAt(state.value,value)
                },
                onFuel={},onIgnite={},onConnect={},
                onHint={},onNext={},onExit={}
            )
        }
        composeRule.onNodeWithText("▶ Kaydedilen Göreve Dön")
            .performScrollTo().performClick()
        composeRule.onNodeWithTag("space_pause").performScrollTo().performClick()
        composeRule.onNodeWithTag("space_fire").performScrollTo().performClick()
        composeRule.runOnIdle {assertTrue(SpaceExpeditionEngine.valid(state.value))}
    }
}
