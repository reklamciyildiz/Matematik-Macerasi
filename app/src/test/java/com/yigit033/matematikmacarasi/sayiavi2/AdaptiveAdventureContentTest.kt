package com.yigit033.matematikmacarasi.sayiavi2

import com.yigit033.matematikmacarasi.games.sayiavi2.ForestEngine
import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAviEngine
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveAdventureContentTest {
    @Test fun eachLevelProducesValidRepeatableBeachQuestions() {
        for (tier in LearningTier.entries) for (seed in 1L..150L) {
            val grouping = SayiAviEngine.generateShellGrouping(seed, tier)
            assertEquals(1, grouping.choices.count { it == grouping.total })
            assertEquals(grouping, SayiAviEngine.generateShellGrouping(seed, tier))
            if (tier == LearningTier.TEMEL) assertEquals(2, grouping.groups)
            if (tier == LearningTier.USTALIK) assertTrue(grouping.groups >= 4)

            val board = SayiAviEngine.generateStageAGrid(seed, tier)
            assertEquals(16, board.numbers.distinct().size)
            assertEquals(5, board.targets.size)
            assertEquals(5, board.numbers.count { it % 3 == 0 })
            val vault = SayiAviEngine.generateStageBVault(seed, tier)
            assertEquals(1, vault.options.count { it == vault.correctAnswer })
            val route = SayiAviEngine.generateStageCRoute(seed, tier)
            assertEquals(4, route.targetSequence.size)
            assertEquals(8, route.nodes.map { it.value }.distinct().size)
            val transfers = SayiAviEngine.generateTransferChallenges(seed, tier)
            assertEquals(3, transfers.size)
            transfers.forEach {
                assertEquals(4, it.choices.distinct().size)
                assertEquals(1, it.choices.count { n -> n == it.correct })
                assertTrue(it.prompt.isNotBlank())
            }
        }
    }

    @Test fun everyLevelGeneratesValidForestQuestions() {
        for (tier in LearningTier.entries) for (seed in 1L..150L) {
            val leaves = ForestEngine.leafRounds(seed, tier)
            assertEquals(leaves, ForestEngine.leafRounds(seed, tier))
            assertEquals(3, leaves.size)
            leaves.forEach { round ->
                assertEquals(1, round.values.indices.count { i ->
                    round.values[i] != round.values[0] + 4 * i
                })
            }
            val trail = ForestEngine.trailSteps(seed, tier)
            assertEquals(4, trail.size)
            trail.forEach {
                assertEquals(it.from + 4, it.target)
                assertEquals(3, it.choices.distinct().size)
            }
            val roots = ForestEngine.rootChallenges(seed, tier)
            assertEquals(3, roots.size)
            roots.forEach {
                assertEquals(4, it.options.distinct().size)
                assertEquals(1, it.options.count { option -> option == it.correct })
            }
        }
    }
    @Test fun oldDefaultTierStaysIdenticalToPreviouslyGeneratedGames() {
        for (seed in 1L..40L) {
            assertEquals(SayiAviEngine.generateStageAGrid(seed),
                SayiAviEngine.generateStageAGrid(seed, LearningTier.GELISEN))
            assertEquals(SayiAviEngine.generateStageBVault(seed),
                SayiAviEngine.generateStageBVault(seed, LearningTier.GELISEN))
            assertEquals(ForestEngine.leafRounds(seed),
                ForestEngine.leafRounds(seed, LearningTier.GELISEN))
        }
    }
}
