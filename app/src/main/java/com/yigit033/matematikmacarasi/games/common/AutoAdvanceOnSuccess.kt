package com.yigit033.matematikmacarasi.games.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.delay

/**
 * One cancellable transition per solved checkpoint.
 *
 * - The game must persist success before it changes [solved] to true.
 * - Switching task/session or leaving the screen cancels the pending transition.
 * - Resuming a saved solved task also advances, without requiring another button.
 * - The caller owns retry UI if its persisted advance fails (no silent repeat writes).
 * - Completion/reward delivery stays in the game engine/store, not this timer.
 */
@Composable
fun AutoAdvanceOnSuccess(
    sessionKey: Any?,
    stepKey: Any?,
    solved: Boolean,
    delayMillis: Long = 900L,
    onAdvance: () -> Unit
) {
    val latestAdvance by rememberUpdatedState(onAdvance)
    LaunchedEffect(sessionKey, stepKey, solved) {
        if (solved) {
            delay(delayMillis.coerceAtLeast(0L))
            latestAdvance()
        }
    }
}
