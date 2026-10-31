package com.yigit033.matematikmacarasi.sayiavi2

import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAviEngine
import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAviRunCounters
import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAviLearningSummary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumIslandEngineTest {
    @Test fun groupingHasOneCorrectAnswerAndUniqueChoices() {
        for (seed in 0L..200L) {
            val q = SayiAviEngine.generateShellGrouping(seed)
            assertTrue(q.groups in 2..4)
            assertEquals(3, q.shellsPerGroup)
            assertEquals(4, q.choices.distinct().size)
            assertEquals(1, q.choices.count { it == q.total })
            assertEquals(q, SayiAviEngine.generateShellGrouping(seed))
        }
    }

    @Test fun transferTasksAreValidAndDeterministic() {
        for (seed in 0L..200L) {
            val tasks = SayiAviEngine.generateTransferChallenges(seed)
            assertEquals(3, tasks.size)
            assertEquals(SayiAviEngine.TransferKind.values().toList(), tasks.map { it.kind })
            assertEquals(tasks, SayiAviEngine.generateTransferChallenges(seed))
            tasks.forEach { q ->
                assertTrue(q.correct > 0)
                assertEquals(4, q.choices.distinct().size)
                assertEquals(1, q.choices.count { it == q.correct })
                assertTrue(q.prompt.isNotBlank())
                assertTrue(q.explanation.isNotBlank())
            }
        }
    }

    @Test fun newCountersRoundtripAndOlderSevenNumberRecordsStillLoad() {
        val run = SayiAviRunCounters().attempt(4, false).attempt(4, true)
            .attempt(1, true).attempt(2, true)
            .attempt(3, true).attempt(3, true).attempt(3, true).attempt(3, true)
            .attempt(5, true).attempt(5, false).attempt(5, true).attempt(5, true).hint()
        assertEquals(run, SayiAviRunCounters.decode(run.encode()))
        assertEquals(5, SayiAviLearningSummary(run, 150).playedStages)
        assertEquals(2, SayiAviLearningSummary(run, 150).errors)
        val legacy = SayiAviRunCounters.decode("1,0,1,0,4,0,0")
        assertEquals(0, legacy?.groupAttempts)
        assertEquals(0, legacy?.transferAttempts)
        assertEquals(3, legacy?.let { SayiAviLearningSummary(it, 50).playedStages })
        assertFalse(SayiAviRunCounters.decode("1,2,1,0,4,0,0") != null)
        assertTrue(SayiAviRunCounters.decode("broken") == null)
    }
}
