package com.yigit033.matematikmacarasi.games.basic

import com.yigit033.matematikmacarasi.models.DifficultyLevel
import kotlin.random.Random

/**
 * One self-contained mixed run. Operation exposure stays balanced while an
 * incorrect operation gains a small, delayed review priority. No hidden grade
 * escalation and no duplicate equations until the available pool is exhausted.
 */
class MixedOperationEngine(
    private val grade: Int,
    private val difficulty: DifficultyLevel,
    private val random: Random = Random.Default
) {
    private val kinds = OperationKind.entries.filter {
        GradeAwareOperationPolicy.allowed(grade, it)
    }
    private val asked = mutableSetOf<String>()
    private val exposure = kinds.associateWith { 0 }.toMutableMap()
    private val needsReview = kinds.associateWith { 0 }.toMutableMap()
    private var previous: OperationKind? = null

    init {
        require(kinds.isNotEmpty()) { "No operations available for grade $grade" }
    }

    fun next(): Pair<OperationKind, GradeQuestion> {
        // Exposure dominates: weak areas receive another opportunity without
        // starving the other operations, and back-to-back repeats are discouraged.
        val lowest = kinds.minOf { priority(it) }
        val candidates = kinds.filter { priority(it) == lowest }
        val operation = candidates.random(random)
        val question = GradeAwareOperationPolicy.generateUnique(
            grade, operation, difficulty, asked, random
        )
        exposure[operation] = exposure.getValue(operation) + 1
        previous = operation
        return operation to question
    }

    fun record(operation: OperationKind, correct: Boolean) {
        require(operation in kinds)
        val current = needsReview.getValue(operation)
        needsReview[operation] = if (correct) (current - 1).coerceAtLeast(0)
            else (current + 1).coerceAtMost(3)
    }

    fun count(operation: OperationKind): Int = exposure[operation] ?: 0

    private fun priority(operation: OperationKind): Int {
        val seen = exposure.getValue(operation)
        val review = needsReview.getValue(operation)
        val repeatPenalty = if (previous == operation && kinds.size > 1) 2 else 0
        return seen * 4 - review * 2 + repeatPenalty
    }
}
