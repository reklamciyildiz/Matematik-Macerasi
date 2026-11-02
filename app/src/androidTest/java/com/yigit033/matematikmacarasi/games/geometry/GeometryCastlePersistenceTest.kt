package com.yigit033.matematikmacarasi.games.geometry

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Device/AVD check: verifies real SharedPreferences commits for every correct tap. */
@RunWith(AndroidJUnit4::class)
class GeometryCastlePersistenceTest {
    @Test fun correctAnswerPersistsAcrossEveryRoomAndDifficulty() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        for (tier in LearningTier.entries) {
            val name = "geometry_test_${tier.name}_${System.nanoTime()}"
            val store = GeometryCastleProgressStore(context, name)
            try {
                val opened = store.begin(tier)
                assertNotNull("Could not start $tier: ${store.lastSaveIssue}", opened)
                var run: CastleRun = requireNotNull(opened)
                for (index in 0 until GeometryCastleEngine.TOTAL_TASKS) {
                    val task = GeometryCastleEngine.task(run)!!
                    when (task.mode) {
                        CastleMode.FIND -> {
                            run = GeometryCastleEngine.answer(run, true).copy(
                                solved = true, selected = task.target
                            )
                            assertTrue("FIND $index ${store.lastSaveIssue}", store.save(run))
                        }
                        CastleMode.BUILD -> {
                            for ((slot, shape) in task.requiredPieces.withIndex()) {
                                run = run.copy(selected = shape)
                                assertTrue("Pick $index/$slot", store.save(run))
                                run = GeometryCastleEngine.answer(run, true).copy(
                                    selected = null, placed = run.placed + shape,
                                    solved = slot == task.requiredPieces.lastIndex
                                )
                                assertTrue("Place $index/$slot ${store.lastSaveIssue}", store.save(run))
                            }
                        }
                        CastleMode.SYMMETRY -> {
                            run = run.copy(mirrorMask = GeometryCastleEngine.symmetricRightMask(
                                task.leftMask, task.gridSize))
                            assertTrue("Mirror grid $index", store.save(run))
                            run = GeometryCastleEngine.answer(run, true).copy(solved = true)
                            assertTrue("Mirror answer $index", store.save(run))
                        }
                    }
                    assertEquals("Persisted answer differs at $index", run, store.load())
                    run = GeometryCastleEngine.advance(run)
                    assertTrue("Advance $index ${store.lastSaveIssue}", store.save(run))
                    assertEquals("Persisted advancement differs at $index", run, store.load())
                }
                assertTrue(run.finished)
                assertEquals(1, run.completedRuns)
            } finally {
                context.getSharedPreferences(name, Context.MODE_PRIVATE).edit().clear().commit()
            }
        }
    }
}
