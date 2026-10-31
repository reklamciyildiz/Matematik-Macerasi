package com.yigit033.matematikmacarasi.sayiavi2

import com.yigit033.matematikmacarasi.games.sayiavi2.ForestEngine
import com.yigit033.matematikmacarasi.games.sayiavi2.ForestSession
import com.yigit033.matematikmacarasi.games.sayiavi2.ForestStage
import org.junit.Assert.*
import org.junit.Test

class ForestEngineTest {
    @Test fun bridgeNeverOverfillsAndBuildsThreeSpans() {
        assertEquals(ForestEngine.BridgeDecision(3, false, true), ForestEngine.placePlank(0, 3))
        assertEquals(ForestEngine.BridgeDecision(0, true, true), ForestEngine.placePlank(3, 1))
        assertEquals(ForestEngine.BridgeDecision(3, false, false), ForestEngine.placePlank(3, 2))
        assertEquals(ForestEngine.BridgeDecision(2, false, true), ForestEngine.placePlank(0, 2))
        assertEquals(ForestEngine.BridgeDecision(0, true, true), ForestEngine.placePlank(2, 2))
    }

    @Test fun everyForestSeedHasExactlyOneBrokenLeafAndFourStepRoutes() {
        for (seed in 0L..250L) {
            val leaves = ForestEngine.leafRounds(seed)
            val trail = ForestEngine.trailSteps(seed)
            val roots = ForestEngine.rootChallenges(seed)
            assertEquals(3, leaves.size)
            assertEquals(4, trail.size)
            assertEquals(3, roots.size)
            assertEquals(leaves, ForestEngine.leafRounds(seed))
            assertEquals(trail, ForestEngine.trailSteps(seed))
            assertEquals(roots, ForestEngine.rootChallenges(seed))
            leaves.forEach { round ->
                assertEquals(1, round.values.indices.count { i ->
                    round.values[i] != round.values[0] + 4 * i
                })
                assertEquals(round.correctValue, round.values[0] + 4 * round.incorrectIndex)
            }
            trail.forEach { item ->
                assertEquals(item.from + 4, item.target)
                assertEquals(3, item.choices.distinct().size)
                assertEquals(1, item.choices.count { it == item.target })
            }
            assertEquals(ForestEngine.RootKind.values().toList(), roots.map { it.kind })
            roots.forEach { item ->
                assertEquals(4, item.options.distinct().size)
                assertEquals(1, item.options.count { it == item.correct })
                assertTrue(item.question.isNotBlank())
            }
        }
    }

    @Test fun countersTrackAttemptsErrorsAndHintsWithoutMakingFalseLearningClaim() {
        val s = ForestSession(stage = ForestStage.BRIDGE, seed = 12L, startedAt = 1L)
            .answer(true).answer(false).hint().answer(true)
        assertEquals(3, s.attempts)
        assertEquals(1, s.errors)
        assertEquals(1, s.hints)
        assertEquals(66, s.accuracy)
        assertTrue(s.valid())
        assertFalse(s.copy(errors = 8).valid())
    }
}
