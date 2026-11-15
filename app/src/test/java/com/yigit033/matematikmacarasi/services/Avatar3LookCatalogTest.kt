package com.yigit033.matematikmacarasi.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure model tests for the Avatar 3.0 look catalog.
 * Does not run billing flows or pretend to verify on-device rendering.
 */
class Avatar3LookCatalogTest {
    @Test fun everyOriginalAvatarKeepsThreeStableAppearanceIds() {
        for (avatarId in 1..15) {
            val looks = Avatar3LookCatalog.looks(avatarId)
            assertEquals("Avatar $avatarId", 3, looks.size)
            assertEquals(listOf(0, 1, 2), looks.map { it.id })
            assertEquals(3, looks.map { it.title }.toSet().size)
        }
    }

    @Test fun cosmeticLooksNeverSpendStars() {
        for (avatarId in 1..15) {
            Avatar3LookCatalog.looks(avatarId).forEach {
                assertEquals("Avatar $avatarId look ${it.id}", 0, it.starCost)
            }
        }
    }

    @Test fun premiumGateIsOnThirdStyleOnly() {
        for (avatarId in 1..15) {
            val looks = Avatar3LookCatalog.looks(avatarId)
            assertFalse(looks[0].premiumRequired)
            assertFalse(looks[1].premiumRequired)
            assertTrue(looks[2].premiumRequired)
        }
    }

    @Test fun explorerLooksAreRealCostumesNotPaletteLabels() {
        val looks = Avatar3LookCatalog.looks(6)
        assertEquals("Klasik Kaşif", looks[0].title)
        assertEquals("Orman Kaşifi", looks[1].title)
        assertEquals("Uzay Kaşifi", looks[2].title)
        assertTrue(looks[1].subtitle.contains("orman"))
        assertTrue(looks[2].subtitle.contains("Kask"))
    }
}
