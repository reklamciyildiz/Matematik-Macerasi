package com.yigit033.matematikmacarasi.sayiavi2

import com.yigit033.matematikmacarasi.games.sayiavi2.TempleEngine
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.*
import org.junit.Test

class TempleEngineTest {
    @Test fun fourRitualsHaveValidMathAcrossAllDifficultyTiers() {
        for (tier in LearningTier.entries) for (seed in 0L..250L) {
            val q = TempleEngine.create(seed, tier)
            assertEquals(q, TempleEngine.create(seed, tier))
            assertEquals(q.mosaic.rows * q.mosaic.columns, q.mosaic.tiles)
            assertTrue(TempleEngine.isBalanced(q.pillars))
            assertEquals(q.pillars.weight, q.pillars.rightRows * q.pillars.rightColumns)
            assertEquals(q.gate.total, q.gate.groups * q.gate.itemsPerGroup)
            assertEquals(4, q.gate.options.distinct().size)
            assertEquals(1, q.gate.options.count { it == q.gate.itemsPerGroup })
            assertEquals(q.mirror.total / q.mirror.divisor, q.mirror.resultAfterDivision)
            if (tier == LearningTier.TEMEL) assertTrue(q.mosaic.tiles <= 16)
            if (tier == LearningTier.USTALIK) assertTrue(q.mosaic.rows >= 3)
        }
    }
    @Test fun physicalTilePlacementDoesNotOverflow() {
        assertEquals(1, TempleEngine.placeMosaicTile(0, 4))
        assertEquals(4, TempleEngine.placeMosaicTile(4, 4))
    }
}
