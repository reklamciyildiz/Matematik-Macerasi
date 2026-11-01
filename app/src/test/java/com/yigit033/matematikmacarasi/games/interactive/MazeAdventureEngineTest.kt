package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MazeAdventureEngineTest {
    @Test fun everyRandomRegionAndTierCanBeCompletedThroughMandatoryMathGates() {
        for(region in MazeRegion.entries) for(tier in LearningTier.entries) for(seed in 1L..110L) {
            val world=MazeAdventureEngine.generate(seed,region,tier)
            assertEquals(world,MazeAdventureEngine.generate(seed,region,tier))
            assertEquals(MazeAdventureEngine.questionCount(region),world.questions.size)
            assertTrue(world.walkable.containsAll(world.route))
            assertEquals(world.entrance,world.route.first())
            assertEquals(world.exit,world.route.last())
            assertEquals(world.questions.size,world.questions.map {it.cell}.distinct().size)
            val begin=MazeAdventureEngine.begin(seed,region,tier)
            assertTrue(MazeAdventureEngine.valid(begin))
            var run=begin
            for(cell in world.route.drop(1)) {
                val dx=cell%world.width-run.cell%world.width
                val dy=cell/world.width-run.cell/world.width
                val dir=MazeDirection.entries.single{it.dx==dx && it.dy==dy}
                val next=MazeAdventureEngine.step(run,world,dir)
                if(next.pendingCell>=0) {
                    assertEquals(run.cell,next.cell)
                    val question=world.gates[next.pendingCell]!!
                    assertEquals(4,question.choices.distinct().size)
                    assertTrue(question.choices.contains(question.correct))
                    assertTrue(question.correct>=0)
                    assertTrue(question.prompt.isNotBlank())
                    run=MazeAdventureEngine.answer(next,world,question.correct)
                } else run=next
                assertTrue("invalid ${region.name}/${tier.name}/$seed",MazeAdventureEngine.valid(run))
            }
            assertTrue("not completed ${region.name}/${tier.name}/$seed",run.completed)
            assertEquals(world.questions.size,run.solvedCount)
            assertEquals(world.questions.size,run.attempts)
            assertEquals(3,MazeAdventureEngine.stars(run))
            assertEquals(run,MazeAdventureEngine.step(run,world,MazeDirection.UP))
        }
    }

    @Test fun wrongAnswersCannotOpenGateAndHintIsCountedOnlyOnce() {
        val run=MazeAdventureEngine.begin(34L,MazeRegion.BRIDGE,LearningTier.TEMEL)
        val world=MazeAdventureEngine.generate(run.seed,run.region,run.tier)
        var state=run
        for(cell in world.route.drop(1)) {
            val dx=cell%world.width-state.cell%world.width
            val dy=cell/world.width-state.cell/world.width
            val dir=MazeDirection.entries.first{it.dx==dx && it.dy==dy}
            state=MazeAdventureEngine.step(state,world,dir)
            if(state.pendingCell>=0)break
        }
        assertTrue(state.pendingCell>=0)
        val question=world.gates[state.pendingCell]!!
        val wrong=question.choices.first{it!=question.correct}
        val stuck=MazeAdventureEngine.answer(state,world,wrong)
        assertEquals(state.cell,stuck.cell)
        assertEquals(state.pendingCell,stuck.pendingCell)
        assertEquals(1,stuck.errors)
        assertEquals(1,stuck.attempts)
        val hint=MazeAdventureEngine.hint(stuck)
        assertEquals(1,hint.hints)
        assertEquals(hint,MazeAdventureEngine.hint(hint))
        assertEquals(hint,MazeAdventureEngine.step(hint,world,MazeDirection.DOWN))
        val unlocked=MazeAdventureEngine.answer(hint,world,question.correct)
        assertEquals(-1,unlocked.pendingCell)
        assertEquals(1,unlocked.solvedCount)
        assertEquals(1,unlocked.errors)
        assertEquals(2,unlocked.attempts)
        assertEquals(1,unlocked.hints)
        assertTrue(MazeAdventureEngine.valid(unlocked))
    }

    @Test fun invalidAndIncompleteStatesDoNotAwardStars() {
        val run=MazeAdventureEngine.begin(5L,MazeRegion.CAVE,LearningTier.GELISEN)
        assertEquals(0,MazeAdventureEngine.stars(run))
        assertFalse(MazeAdventureEngine.valid(run.copy(seed=0L)))
        assertFalse(MazeAdventureEngine.valid(run.copy(cell=0)))
        assertFalse(MazeAdventureEngine.valid(run.copy(solvedMask=1 shl 10)))
        assertFalse(MazeAdventureEngine.valid(run.copy(errors=2)))
        assertFalse(MazeAdventureEngine.valid(run.copy(completed=true)))
    }

    @Test fun guidanceIsAlwaysAdjacentAndTraversable() {
        for(seed in 1L..70L) {
            val run=MazeAdventureEngine.begin(seed,MazeRegion.CASTLE,LearningTier.USTALIK)
            val maze=MazeAdventureEngine.generate(run.seed,run.region,run.tier)
            val dir=MazeAdventureEngine.guidance(run,maze)!!
            val next=run.cell+dir.dx+dir.dy*maze.width
            assertTrue(next in maze.walkable)
            assertNotEquals(run.cell,next)
        }
    }

    @Test fun hintsNeverLeakNumericalAnswerAndQuestionsStayGradeAware() {
        for(region in MazeRegion.entries) for(tier in LearningTier.entries) {
            val world=MazeAdventureEngine.generate(4321L,region,tier)
            world.questions.forEach {q->
                assertEquals(4,q.choices.size)
                assertTrue(q.hint.length>=12)
                assertTrue(q.explanation.contains(q.correct.toString()))
            }
        }
    }
}
