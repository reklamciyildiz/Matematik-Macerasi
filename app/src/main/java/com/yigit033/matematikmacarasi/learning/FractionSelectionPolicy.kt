package com.yigit033.matematikmacarasi.learning

/** Tappable equal-part teaching model, not a scored mini-game. */
object FractionSelectionPolicy {
    fun toggle(selected: Set<Int>, part: Int, denominator: Int): Set<Int> {
        require(denominator in 2..12 && part in 0 until denominator)
        require(selected.all { it in 0 until denominator })
        return if (part in selected) selected - part else selected + part
    }

    fun count(selected: Set<Int>, denominator: Int): Int {
        require(denominator in 2..12 && selected.all { it in 0 until denominator })
        return selected.size
    }
}
