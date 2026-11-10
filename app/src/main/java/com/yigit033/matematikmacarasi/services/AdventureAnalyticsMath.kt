package com.yigit033.matematikmacarasi.services

/**
 * A game can be stored under the current Activity ID AND one or more older
 * per-level formats. The same completion is often written to both.
 *
 * Never add the main and legacy counters together: that double counts plays.
 * Use the strongest supported representation and reject corrupt negatives.
 * No historical data is deleted or silently rewritten.
 */
internal object AdventureAnalyticsMath {
    fun canonicalValue(direct: Int, vararg legacyFormats: List<Int>): Int {
        val bestLegacy = legacyFormats.maxOfOrNull { values ->
            values.fold(0L) { sum, value ->
                (sum + value.coerceAtLeast(0).toLong()).coerceAtMost(Int.MAX_VALUE.toLong())
            }.toInt()
        } ?: 0
        return maxOf(direct.coerceAtLeast(0), bestLegacy)
    }
    fun safeSum(values: Collection<Int>): Int =
        values.fold(0L) { sum, value ->
            (sum + value.coerceAtLeast(0).toLong()).coerceAtMost(Int.MAX_VALUE.toLong())
        }.toInt()
}
