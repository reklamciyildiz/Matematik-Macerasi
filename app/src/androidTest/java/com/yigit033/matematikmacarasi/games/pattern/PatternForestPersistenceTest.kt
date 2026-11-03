package com.yigit033.matematikmacarasi.games.pattern

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Emulator/device test: actual SharedPreferences writes survive every puzzle and tap. */
@RunWith(AndroidJUnit4::class)
class PatternForestPersistenceTest {
    @Test fun allThirtyPuzzlesResumeWithoutRepeatedRewardOrEvidence() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        for(grade in listOf(1,2,3,4,5,6,7,8))for(diff in 0..2){
            val name="forest_device_test_"+grade+"_"+diff
            val prefs=context.getSharedPreferences(name,Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
            val store=PatternForestStore(context,name)
            try {
                var run=requireNotNull(store.start(grade,diff))
                for(index in 0 until 30){
                    val t=requireNotNull(store.challenge(run))
                    assertEquals(index,run.index)
                    run=when(t.mechanic) {
                        PatternMechanic.COMPLETE,PatternMechanic.DRAG ->
                            run.copy(picked=t.options.indexOf(t.answer.single()))
                        PatternMechanic.RULE -> run.copy(picked=t.correctRuleIndex)
                        PatternMechanic.SPOT -> run.copy(picked=t.corruptIndex)
                        PatternMechanic.ORDER,PatternMechanic.CREATE -> {
                            val used=mutableSetOf<Int>()
                            val ordered=t.answer.map{tile ->
                                val choice=t.options.indices.first { i->
                                    t.options[i]==tile &&
                                        (t.mechanic==PatternMechanic.CREATE || i !in used)
                                }
                                used.add(choice)
                                choice
                            }
                            ordered.forEachIndexed{step,ignored->
                                val partial=ordered.take(step+1).joinToString(",")
                                assertTrue("Partial selection not saved",
                                    store.save(run.copy(order=partial)))
                            }
                            run.copy(order=ordered.joinToString(","))
                        }
                    }
                    assertTrue("Answer selection persisted: "+store.lastError,
                        store.save(run))
                    assertEquals(run,store.load())
                    assertTrue(PatternForestEngine.correct(t,run.picked,run.selected))
                    run=run.copy(solved=true,attempts=run.attempts+1)
                    assertTrue("Solved checkpoint: "+store.lastError,store.save(run))
                    assertEquals(run,store.load())
                    val previous=run
                    run=requireNotNull(store.advance(run))
                    assertNull("Repeated completed stage must not award evidence",
                        store.advance(previous))
                    assertEquals(run,store.load())
                }
                assertTrue(run.finished)
                assertEquals(30,run.clean)
                assertEquals(1,run.completedRuns)
                assertEquals(30,store.bestClean())
                PatternWorld.entries.forEach{world ->
                    assertEquals(6,store.completedFor(world.skill))
                    assertEquals(6,store.cleanFor(world.skill))
                }
                assertTrue(store.markDelivered(run.seed))
                assertFalse(store.markDelivered(run.seed))
                assertFalse(store.rewardMarked())
                assertTrue(store.markReward())
                assertTrue(store.rewardMarked())
                val second=requireNotNull(store.start(grade,diff))
                assertEquals(1,second.completedRuns)
                assertEquals(0,second.index)
                assertTrue(store.rewardMarked())
            } finally {
                prefs.edit().clear().commit()
            }
        }
    }

    @Test fun fabricatedSolvedAnswerAndSkippingCannotCorruptProgress() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="forest_invalid_test"
        val prefs=context.getSharedPreferences(name,Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        val store=PatternForestStore(context,name)
        try{
            val initial=requireNotNull(store.start(3,1))
            assertFalse(store.save(initial.copy(solved=true)))
            assertEquals(initial,store.load())
            assertFalse(store.save(initial.copy(index=15)))
            assertEquals(initial,store.load())
            assertFalse(store.save(initial.copy(picked=200)))
            assertEquals(initial,store.load())
        } finally {
            prefs.edit().clear().commit()
        }
    }
}
