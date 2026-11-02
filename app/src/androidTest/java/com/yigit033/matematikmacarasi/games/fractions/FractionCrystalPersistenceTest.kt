package com.yigit033.matematikmacarasi.games.fractions

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FractionCrystalPersistenceTest {
    @Test fun allTasksPersistWithCorrectAnswerAndNeverDuplicateDelivery() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        for(grade in listOf(2,3,4,5,6,8)) for(difficulty in 0..2) {
            val prefsName="fraction_device_test_${grade}_$difficulty"
            val store=FractionCrystalStore(context,prefsName)
            try {
                val started=store.newRun(grade,difficulty)
                assertNotNull(started)
                var run=requireNotNull(started)
                repeat(FractionCrystalEngine.TASK_COUNT) { index ->
                    val task=requireNotNull(store.task(run))
                    assertEquals(index,run.index)
                    when(task.mode) {
                        CrystalMode.FILL -> {
                            run=run.copy(litMask=(1 shl task.target.numerator)-1)
                            assertTrue(store.save(run))
                        }
                        CrystalMode.BRIDGE -> {
                            repeat(task.target.numerator) {
                                run=run.copy(built=run.built+1)
                                assertTrue("Build tap not saved",store.save(run))
                            }
                        }
                        else -> assertTrue(FractionCrystalEngine.correct(task,0,task.answer))
                    }
                    val filled=when(task.mode){
                        CrystalMode.FILL->Integer.bitCount(run.litMask)
                        CrystalMode.BRIDGE->run.built
                        else->0
                    }
                    assertTrue(FractionCrystalEngine.correct(task,filled,task.answer))
                    run=run.copy(solved=true,attempts=run.attempts+1)
                    assertTrue("Solved $grade/$difficulty/$index: ${store.lastError}",
                        store.save(run))
                    assertEquals(run,store.load())
                    run=requireNotNull(store.advance(run))
                    assertEquals(run,store.load())
                }
                assertTrue(run.finished)
                assertEquals(1,run.completedRuns)
                assertTrue(store.markDelivered(run.seed))
                assertFalse(store.markDelivered(run.seed))
                assertEquals(12,run.cleanAnswers)
            } finally {
                context.getSharedPreferences(prefsName,Context.MODE_PRIVATE)
                    .edit().clear().commit()
            }
        }
    }
}
