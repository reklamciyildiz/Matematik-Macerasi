package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FractionSelectionPolicyTest {
    @Test fun paintingAndUnpaintingAnyPartAlwaysPreservesValidFraction() {
        for (denominator in 2..12) {
            var selected = emptySet<Int>()
            for (part in 0 until denominator) {
                selected = FractionSelectionPolicy.toggle(selected,part,denominator)
                assertEquals(part + 1,FractionSelectionPolicy.count(selected,denominator))
                assertTrue(part in selected)
            }
            for (part in 0 until denominator) {
                selected = FractionSelectionPolicy.toggle(selected,part,denominator)
                assertEquals(denominator - part - 1, selected.size)
                assertFalse(part in selected)
            }
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidPartsCannotBeToggled() {
        FractionSelectionPolicy.toggle(setOf(0),5,3)
    }
}
