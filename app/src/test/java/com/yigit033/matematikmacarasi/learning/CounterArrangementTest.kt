package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CounterArrangementTest {
    @Test fun removedItemsMatchSubtractionAcrossEveryWrapSize() {
        for (count in 1..100) for (removed in 0..count) {
            for (perRow in 1..12) {
                val indices = CounterArrangement.rows(count, perRow).flatten()
                assertEquals((0 until count).toList(), indices)
                assertEquals(removed, indices.count {
                    CounterArrangement.isRemoved(it, count, removed)
                })
                assertEquals(count - removed, indices.count {
                    !CounterArrangement.isRemoved(it, count, removed)
                })
            }
        }
    }
    @Test fun laterRowsDoNotResetRemovalCounter() {
        val indices = CounterArrangement.rows(9, 4)
        assertEquals(listOf(listOf(0,1,2,3),listOf(4,5,6,7),listOf(8)),indices)
        assertTrue(CounterArrangement.isRemoved(4,9,5))
        assertFalse(CounterArrangement.isRemoved(5,9,5))
    }
}
