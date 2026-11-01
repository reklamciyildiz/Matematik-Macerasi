package com.yigit033.matematikmacarasi.games.interactive

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertExists
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

/** Regression test of real Compose click dispatch, not only pure engine transitions. */
@RunWith(AndroidJUnit4::class)
class MazeAdventureTouchTest {
    @get:Rule val composeRule=createComposeRule()

    private fun openRun(initial:MazeExpedition):androidx.compose.runtime.MutableState<MazeExpedition> {
        val state=mutableStateOf(initial)
        val world=MazeAdventureEngine.generate(initial.seed,initial.region,initial.tier)
        composeRule.setContent {
            MazeAdventureScreen(
                run=state.value,
                isPremium=true,
                remainingPlays=5,
                recommendedTier=initial.tier,
                completedRuns=0,
                regionRecords=emptyMap(),
                feedback="",
                onStart={_,_->false},
                onMove={dir->state.value=MazeAdventureEngine.step(state.value,world,dir)},
                onAnswer={value->state.value=MazeAdventureEngine.answer(state.value,world,value)},
                onHint={state.value=MazeAdventureEngine.hint(state.value)},
                onExit={},
                onUpgrade={}
            )
        }
        composeRule.onNodeWithText("▶ Kaldığım Yerden Devam Et").performClick()
        return state
    }

    @Test fun directionPadClickChangesPlayerPosition() {
        val initial=MazeAdventureEngine.begin(42L,MazeRegion.SUN_MEADOW,LearningTier.TEMEL)
        val world=MazeAdventureEngine.generate(initial.seed,initial.region,initial.tier)
        val direction=MazeAdventureEngine.guidance(initial,world)!!
        val state=openRun(initial)
        composeRule.onNodeWithTag("maze_direction_"+direction.name)
            .performScrollTo().performClick()
        composeRule.waitForIdle()
        assertTrue(state.value.cell!=initial.cell || state.value.pendingCell>=0)
    }

    @Test fun gateWrongAnswerAndRightAnswerAreActuallyClickable() {
        val initial=MazeAdventureEngine.begin(71L,MazeRegion.BRIDGE,LearningTier.TEMEL)
        val world=MazeAdventureEngine.generate(initial.seed,initial.region,initial.tier)
        var atGate=initial
        for(cell in world.route.drop(1)) {
            val dx=cell%world.width-atGate.cell%world.width
            val dy=cell/world.width-atGate.cell/world.width
            val dir=MazeDirection.entries.single {it.dx==dx && it.dy==dy}
            atGate=MazeAdventureEngine.step(atGate,world,dir)
            if(atGate.pendingCell>=0)break
        }
        assertTrue(atGate.pendingCell>=0)
        val question=world.gates[atGate.pendingCell]!!
        val wrong=question.choices.first {it!=question.correct}
        val state=openRun(atGate)
        composeRule.onNodeWithTag("maze_gate_dialog").assertExists()
        composeRule.onNodeWithTag("maze_gate_answer_"+wrong).performClick()
        composeRule.waitForIdle()
        assertEquals(1,state.value.errors)
        assertEquals(atGate.pendingCell,state.value.pendingCell)
        composeRule.onNodeWithTag("maze_gate_answer_"+question.correct).performClick()
        composeRule.waitForIdle()
        assertEquals(-1,state.value.pendingCell)
        assertEquals(1,state.value.solvedCount)
        composeRule.onNodeWithTag("maze_gate_dialog").assertDoesNotExist()
    }
}
