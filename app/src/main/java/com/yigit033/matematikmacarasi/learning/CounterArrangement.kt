package com.yigit033.matematikmacarasi.learning

/** Stable global indices for native counter illustrations.
 * Row splitting must never change how many items are removed.
 */
object CounterArrangement {
    fun rows(count: Int, perRow: Int): List<List<Int>> {
        require(count in 0..100 && perRow in 1..12)
        return (0 until count).chunked(perRow)
    }

    fun isRemoved(globalIndex: Int, total: Int, removed: Int): Boolean {
        require(total in 0..100 && removed in 0..total && globalIndex in 0 until total)
        return globalIndex < removed
    }
}
