package com.yigit033.matematikmacarasi.games.basic

import com.yigit033.matematikmacarasi.models.DifficultyLevel

/**
 * Short-session scaffolding for the four individual operation games.
 * After two consecutive misses, practice temporarily steps down one tier.
 * Two correct answers at the supported tier restore the chosen difficulty.
 * School-grade limits are still enforced separately by the question policy.
 */
class OperationPracticeState {
    private var missedInRow = 0
    private var supported = false
    private var correctInSupport = 0

    fun effectiveDifficulty(selected: DifficultyLevel): DifficultyLevel {
        if (!supported || selected.ordinal == 0) return selected
        return DifficultyLevel.entries[selected.ordinal - 1]
    }

    fun record(correct: Boolean) {
        if (correct) {
            missedInRow = 0
            if (supported) {
                correctInSupport++
                if (correctInSupport >= 2) {
                    supported = false
                    correctInSupport = 0
                }
            }
        } else {
            missedInRow++
            correctInSupport = 0
            if (missedInRow >= 2) {
                supported = true
                missedInRow = 0
            }
        }
    }

    fun reset() {
        missedInRow = 0
        supported = false
        correctInSupport = 0
    }
}
