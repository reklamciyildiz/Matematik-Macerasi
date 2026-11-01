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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Actual Compose touch dispatch, not just engine transitions. Requires emulator/device. */
@RunWith(AndroidJUnit4::class)
class BalloonAdventureTouchTest {
    @get:Rule val composeRule=createComposeRule()

    @Test fun wrongBalloonThenCorrectBalloonThenNextScreen() {
        val state=mutableStateOf(
            BalloonAdventureEngine.begin(42L,BalloonRealm.SUN,LearningTier.TEMEL)
        )
        composeRule.setContent {
            BalloonAdventureScreen(
                run=state.value,suggestedTier=LearningTier.TEMEL,
                records=emptyMap(),historicalStars=emptyMap(),feedback="",
                onStart={realm,tier->
                    state.value=BalloonAdventureEngine.begin(73L,realm,tier)
                    true
                },
                onChoose={value->
                    state.value=BalloonAdventureEngine.choose(state.value,value)
                },
                onHint={state.value=BalloonAdventureEngine.hint(state.value)},
                onNext={state.value=BalloonAdventureEngine.continueAfterAnswer(state.value)},
                onExit={}
            )
        }
        composeRule.onNodeWithText("▶ Kaldığım Yerden Devam Et")
            .performScrollTo().performClick()
        val q=BalloonAdventureEngine.question(state.value)
        val wrong=q.choices.first {it!=q.answer}
        composeRule.onNodeWithTag("balloon_choice_"+wrong)
            .performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(1,state.value.errors)
            assertEquals(0,state.value.index)
        }
        composeRule.onNodeWithTag("balloon_choice_"+q.answer)
            .performScrollTo().performClick()
        composeRule.runOnIdle {
            assertTrue(state.value.resolved)
            assertEquals(0,state.value.firstTry)
            assertFalse(state.value.completed)
        }
        composeRule.onNodeWithText("Sonraki Balon →")
            .performScrollTo().performClick()
        composeRule.runOnIdle {assertEquals(1,state.value.index)}
    }

    @Test fun selectingRealmAndTierCreatesExpectedExpedition() {
        val state=mutableStateOf<BalloonRun?>(null)
        composeRule.setContent {
            BalloonAdventureScreen(
                run=state.value,suggestedTier=LearningTier.TEMEL,
                records=emptyMap(),historicalStars=emptyMap(),feedback="",
                onStart={realm,tier->
                    state.value=BalloonAdventureEngine.begin(77L,realm,tier)
                    true
                },
                onChoose={},onHint={},onNext={},onExit={}
            )
        }
        composeRule.onNodeWithTag("balloon_realm_CLOUD")
            .performScrollTo().performClick()
        composeRule.onNodeWithText("Gelişen").performScrollTo().performClick()
        composeRule.onNodeWithText("🚀 Bulut Koyu Macerasını Başlat")
            .performScrollTo().performClick()
        composeRule.runOnIdle {
            assertEquals(BalloonRealm.CLOUD,state.value?.realm)
            assertEquals(LearningTier.GELISEN,state.value?.tier)
        }
    }
}
