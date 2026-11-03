package com.yigit033.matematikmacarasi.games.time

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

/** Physical device/emulator QA: all five mechanics, all 25 checkpoints, resume, no double delivery. */
@RunWith(AndroidJUnit4::class)
class TimeTowerPersistenceTest {
    @Test fun progressResumesEveryTaskWithoutDuplicateSkillEvidence() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        for(grade in listOf(1,2,3,4,5,6,8))for(difficulty in 0..2) {
            val name="time_tower_test_${grade}_${difficulty}"
            val prefs=context.getSharedPreferences(name,Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
            val store=TimeTowerStore(context,name)
            try {
                var state=requireNotNull(store.start(grade,difficulty))
                repeat(TimeTowerEngine.STOPS) { index->
                    val task=requireNotNull(store.challenge(state))
                    assertEquals(index,state.index)
                    state=when(task.world) {
                        TimeWorld.CLOCK,TimeWorld.READ,TimeWorld.TRAIN->
                            state.copy(cursorMinutes=task.answerMinute)
                        TimeWorld.CALENDAR->state.copy(chosenDate=task.answerDate.code)
                        TimeWorld.ROUTE->state.copy(route=task.correctOrder.joinToString(","))
                    }
                    assertTrue("Action $grade/$difficulty/$index: ${store.lastError}",
                        store.save(state))
                    assertEquals("Action not durable",state,store.load())
                    state=state.copy(solved=true,attempts=state.attempts+1)
                    assertTrue("Correct $grade/$difficulty/$index: ${store.lastError}",
                        store.save(state))
                    assertEquals(state,store.load())
                    val previous=state
                    state=requireNotNull(store.advance(state))
                    assertNull("Duplicated advancement",store.advance(previous))
                    assertEquals("Advance not durable",state,store.load())
                }
                assertTrue(state.finished)
                assertEquals(25,state.clean)
                assertEquals(25,state.bestStreak)
                assertEquals(1,state.completedRuns)
                assertEquals(25,store.bestClean())
                TimeWorld.entries.forEach{world->
                    assertEquals(5,store.skillCompleted(world.skill))
                    assertEquals(5,store.skillClean(world.skill))
                }
                assertTrue(store.markDelivered(state.seed))
                assertFalse(store.markDelivered(state.seed))
                assertFalse(store.rewardRecorded())
                assertTrue(store.markRewardRecorded())
                assertTrue(store.rewardRecorded())
                val second=requireNotNull(store.start(grade,difficulty))
                assertEquals(1,second.completedRuns)
                assertEquals(0,second.index)
                assertTrue(store.rewardRecorded())
            } finally {
                prefs.edit().clear().commit()
            }
        }
    }

    @Test fun invalidClockOrCalendarSelectionCannotCorruptSavedProgress() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="time_tower_invalid_${System.nanoTime()}"
        val prefs=context.getSharedPreferences(name,Context.MODE_PRIVATE)
        val store=TimeTowerStore(context,name)
        try {
            val initial=requireNotNull(store.start(3,1))
            assertEquals(TimeWorld.CLOCK,store.challenge(initial)?.world)
            assertFalse(store.save(initial.copy(cursorMinutes=9000)))
            assertEquals(initial,store.load())
            assertFalse(store.save(initial.copy(index=3)))
            assertEquals(initial,store.load())
            assertFalse(store.save(initial.copy(solved=true)))
            assertEquals(initial,store.load())
        } finally {
            prefs.edit().clear().commit()
        }
    }
}
