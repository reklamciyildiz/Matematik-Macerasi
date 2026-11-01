package com.yigit033.matematikmacarasi.games.geometry

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GeometryCastleEngineTest {
    @Test fun deterministicAndValidForAllTiersAndSeeds() {
        for (tier in LearningTier.entries) for (seed in 1L..300L) {
            val questions = GeometryCastleEngine.tasks(seed,tier)
            assertEquals(12,questions.size)
            assertEquals(questions,GeometryCastleEngine.tasks(seed,tier))
            questions.forEach { q ->
                when(q.mode) {
                    CastleMode.FIND,CastleMode.BUILD -> {
                        assertEquals(4,q.options.size)
                        assertEquals(4,q.options.distinct().size)
                        assertTrue(q.target in q.options)
                        assertTrue(q.options.all { it in GeometryCastleEngine.available(tier) })
                        assertTrue(GeometryCastleEngine.isCorrect(q,q.target,0))
                        assertTrue(q.options.filter { it != q.target }.all {
                            !GeometryCastleEngine.isCorrect(q,it,0)
                        })
                    }
                    CastleMode.SYMMETRY -> {
                        val answer = GeometryCastleEngine.symmetricRightMask(q.leftMask,q.gridSize)
                        assertTrue(answer>0)
                        assertTrue(GeometryCastleEngine.isCorrect(q,null,answer))
                        assertFalse(GeometryCastleEngine.isCorrect(q,null,answer xor 1))
                    }
                }
            }
            assertEquals(CastleMode.FIND,questions[0].mode)
            assertEquals(CastleMode.BUILD,questions[3].mode)
            assertEquals(CastleMode.SYMMETRY,questions[6].mode)
            assertEquals(CastleMode.FIND,questions[9].mode)
            assertEquals(CastleMode.BUILD,questions[10].mode)
            assertEquals(CastleMode.SYMMETRY,questions[11].mode)
        }
    }

    @Test fun mirroredCellsUseActualReverseColumn() {
        assertEquals(2,GeometryCastleEngine.symmetricRightMask(1,4))
        assertEquals(1,GeometryCastleEngine.symmetricRightMask(2,4))
        assertEquals(4,GeometryCastleEngine.symmetricRightMask(1,6))
        assertEquals(2,GeometryCastleEngine.symmetricRightMask(2,6))
        assertEquals(1,GeometryCastleEngine.symmetricRightMask(4,6))
        assertEquals(1,GeometryCastleEngine.toggleMirror(0,0,0,4))
        assertEquals(0,GeometryCastleEngine.toggleMirror(1,0,0,4))
        assertEquals(0,GeometryCastleEngine.toggleMirror(0,-1,0,4))
        assertEquals(0,GeometryCastleEngine.toggleMirror(0,0,6,6))
    }

    @Test fun completionIsExactlyOnceAndTracksIndependentFirstTry() {
        var session = CastleRun(seed=111L,tier=LearningTier.GELISEN)
        for(taskId in 0 until 12) {
            val task = GeometryCastleEngine.task(session)!!
            assertFalse(session.solved)
            // A solved task cannot advance without a successful completion flag.
            assertEquals(session,GeometryCastleEngine.advance(session))
            session=GeometryCastleEngine.answer(session,true).copy(
                solved=true,
                selected=if(task.mode==CastleMode.SYMMETRY) null else task.target,
                mirrorMask=if(task.mode==CastleMode.SYMMETRY)
                    GeometryCastleEngine.symmetricRightMask(task.leftMask,task.gridSize)
                    else 0
            )
            session=GeometryCastleEngine.advance(session)
            assertEquals(taskId+1,session.room*3+session.step)
        }
        assertTrue(session.finished)
        assertEquals(1,session.completedRuns)
        assertEquals(12,session.firstTryCorrect)
        assertEquals(12,session.attempts)
        assertEquals(0,session.errors)
        assertEquals(session,GeometryCastleEngine.advance(session))
        assertEquals(session,GeometryCastleEngine.answer(session,true))
    }

    @Test fun hintsAndMistakesNeverCountAsIndependentSolutions() {
        val initial = CastleRun(seed=22L)
        val afterWrong = GeometryCastleEngine.answer(initial,false)
        val afterHint = GeometryCastleEngine.hint(afterWrong)
        val repeatedHint = GeometryCastleEngine.hint(afterHint)
        assertEquals(1,repeatedHint.hints)
        val solved = GeometryCastleEngine.answer(repeatedHint,true).copy(solved=true)
        val next=GeometryCastleEngine.advance(solved)
        assertEquals(0,next.firstTryCorrect)
        assertEquals(2,next.attempts)
        assertEquals(1,next.errors)
        assertEquals(1,next.hints)
        assertFalse(next.hasHintInTask)
        assertFalse(next.hasErrorInTask)
    }

    @Test fun noInvalidCastleAdvanceOrOutOfRangePuzzleState() {
        assertFalse(GeometryCastleEngine.valid(CastleRun(seed=5L,room=7)))
        assertFalse(GeometryCastleEngine.valid(CastleRun(seed=5L,mirrorMask=1 shl 20)))
        assertFalse(GeometryCastleEngine.valid(CastleRun(seed=-1L)))
        assertEquals(null,GeometryCastleEngine.task(CastleRun()))
        assertNotEquals(GeometryCastleEngine.tasks(1L,LearningTier.TEMEL),
            GeometryCastleEngine.tasks(123L,LearningTier.TEMEL))
    }
}
