package com.yigit033.matematikmacarasi.services

/**
 * Pure decisions shared by the update experience and its unit tests.
 * Play Store is the only authority for the existence of a downloadable update.
 */
internal object UpdatePromptPolicy {
    const val REMIND_AFTER_MS = 24L * 60L * 60L * 1000L
    const val CHECK_AFTER_MS = 6L * 60L * 60L * 1000L

    fun shouldPrompt(
        availableCode: Int,
        currentCode: Int,
        dismissedCode: Int,
        dismissedAtMs: Long,
        nowMs: Long
    ): Boolean {
        if (availableCode <= currentCode) return false
        if (dismissedCode != availableCode) return true
        if (dismissedAtMs <= 0L || nowMs < dismissedAtMs) return true
        return nowMs - dismissedAtMs >= REMIND_AFTER_MS
    }

    fun isUpdatedInstallation(firstInstallMs: Long, lastUpdateMs: Long): Boolean =
        firstInstallMs > 0 && lastUpdateMs - firstInstallMs > 10_000L
}
