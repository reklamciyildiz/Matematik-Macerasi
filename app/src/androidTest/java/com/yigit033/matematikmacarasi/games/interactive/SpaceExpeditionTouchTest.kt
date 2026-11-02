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

    @Test fun shipButtonsAndLaserStayOnDedicatedCombatScreen() {
        val state=mutableStateOf<SpaceRun?>(null)
        composeRule.setContent {
            SpaceExpeditionScreen(
                run=state.value,
                recommended=LearningTier.TEMEL,
                profileGrade=1,
                records=emptyMap(),historicalStars=emptyMap(),feedback="",
                onBegin={planet,tier->
                    state.value=SpaceExpeditionEngine.begin(99L,planet,tier,1)
                    true
                },
                onShipPosition={x->
                    state.value=SpaceExpeditionEngine.position(state.value!!,x)
                },
                onFireHit={n->state.value=SpaceExpeditionEngine.fireAt(state.value!!,n)},
                onHint={state.value=SpaceExpeditionEngine.hint(state.value!!)},
                onExit={}
            )
        }
        composeRule.onNodeWithTag("space_planet_AQUA").performScrollTo().performClick()
        composeRule.onNodeWithText("🚀 Okyanus Nebulası Başlat")
            .performScrollTo().performClick()
        composeRule.onNodeWithTag("space_mission_prompt").assertExists()
        composeRule.onNodeWithTag("space_pause").performClick()
        composeRule.onNodeWithTag("space_steer_left").performClick()
        composeRule.runOnIdle {assertTrue(state.value!!.shipX<.5f)}
        composeRule.onNodeWithTag("space_fire").performClick()
        composeRule.onNodeWithTag("space_combat_arena").assertExists()
        composeRule.runOnIdle {
            assertEquals(1,state.value!!.grade)
            assertTrue(SpaceExpeditionEngine.valid(state.value!!))
        }
    }
}
