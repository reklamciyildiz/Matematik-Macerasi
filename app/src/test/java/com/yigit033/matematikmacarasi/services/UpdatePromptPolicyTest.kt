package com.yigit033.matematikmacarasi.services

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePromptPolicyTest {
    private val now = 2_000_000_000L

    @Test fun newerPlayVersionIsOffered() {
        assertTrue(UpdatePromptPolicy.shouldPrompt(25, 24, 0, 0L, now))
    }

    @Test fun installedOrOlderVersionsAreNotOffered() {
        assertFalse(UpdatePromptPolicy.shouldPrompt(24, 24, 0, 0L, now))
        assertFalse(UpdatePromptPolicy.shouldPrompt(23, 24, 0, 0L, now))
    }

    @Test fun sameVersionRespectsTwentyFourHourSnooze() {
        assertFalse(UpdatePromptPolicy.shouldPrompt(
            25, 24, 25, now - UpdatePromptPolicy.REMIND_AFTER_MS + 1, now
        ))
        assertTrue(UpdatePromptPolicy.shouldPrompt(
            25, 24, 25, now - UpdatePromptPolicy.REMIND_AFTER_MS, now
        ))
    }

    @Test fun anotherVersionMayShowDespitePreviousDismissal() {
        assertTrue(UpdatePromptPolicy.shouldPrompt(26, 24, 25, now - 100L, now))
    }

    @Test fun clockRollbackDoesNotLockUpdateForever() {
        assertTrue(UpdatePromptPolicy.shouldPrompt(25, 24, 25, now + 100L, now))
    }

    @Test fun firstInstallIsNotWhatsNewUpgrade() {
        assertFalse(UpdatePromptPolicy.isUpdatedInstallation(10_000L, 10_000L))
        assertFalse(UpdatePromptPolicy.isUpdatedInstallation(0L, 10_000L))
        assertTrue(UpdatePromptPolicy.isUpdatedInstallation(10_000L, 90_000L))
    }
}
