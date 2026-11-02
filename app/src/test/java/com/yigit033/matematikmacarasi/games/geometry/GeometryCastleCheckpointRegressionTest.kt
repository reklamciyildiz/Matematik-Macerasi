package com.yigit033.matematikmacarasi.games.geometry

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.*
import org.junit.Test

/**
 * Every state generated after a user tap must remain a valid persistence checkpoint,
 * including multi-piece construction and the final room transition.
 */
class GeometryCastleCheckpointRegressionTest {
    @Test fun correctSelectionsNeverGenerateInvalidCheckpoints() {
        for (tier in LearningTier.entries)
            for (version in 1..2)
                for (seed in 1L..130L) {
                    var run = CastleRun(seed=seed,tier=tier,contentVersion=version)
                    repeat(GeometryCastleEngine.TOTAL_TASKS) { index ->
                        val task = GeometryCastleEngine.task(run)!!
                        assertTrue("Initial state invalid at $tier v$version #$index",GeometryCastleEngine.valid(run))
                        when(task.mode) {
                            CastleMode.FIND -> {
                                run = GeometryCastleEngine.answer(run,true).copy(
                                    selected=task.target,solved=true)
                                assertTrue("Correct shape invalid at $tier v$version #$index",GeometryCastleEngine.valid(run))
                            }
                            CastleMode.BUILD -> {
                                if (version == 1) {
                                    // Legacy saves have one socket and intentionally no placed list.
                                    run = GeometryCastleEngine.answer(run, true).copy(
                                        selected = task.target, solved = true
                                    )
                                    assertTrue("Legacy socket invalid",GeometryCastleEngine.valid(run))
                                } else task.requiredPieces.forEachIndexed { part,shape ->
                                    run=run.copy(selected=shape)
                                    assertTrue("Selection invalid $tier/$index/$part",GeometryCastleEngine.valid(run))
                                    run=GeometryCastleEngine.answer(run,true).copy(
                                        selected=null, placed=run.placed+shape,
                                        solved=part==task.requiredPieces.lastIndex)
                                    assertTrue("Placement invalid $tier/$index/$part",GeometryCastleEngine.valid(run))
                                }
                            }
                            CastleMode.SYMMETRY -> {
                                run=run.copy(mirrorMask=GeometryCastleEngine.symmetricRightMask(
                                    task.leftMask,task.gridSize))
                                assertTrue("Mirror tap invalid $tier/$index",GeometryCastleEngine.valid(run))
                                run=GeometryCastleEngine.answer(run,true).copy(solved=true)
                                assertTrue("Mirror solved invalid $tier/$index",GeometryCastleEngine.valid(run))
                            }
                        }
                        val advanced=GeometryCastleEngine.advance(run)
                        assertTrue("Advance invalid at $tier v$version #$index",GeometryCastleEngine.valid(advanced))
                        assertEquals("Incorrect task index",index+1,advanced.room*3+advanced.step)
                        run=advanced
                    }
                    assertTrue(run.finished)
                    assertEquals(1,run.completedRuns)
                }
    }
}
