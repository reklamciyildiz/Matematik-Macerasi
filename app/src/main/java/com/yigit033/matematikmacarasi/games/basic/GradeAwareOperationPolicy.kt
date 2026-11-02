package com.yigit033.matematikmacarasi.games.basic

import com.yigit033.matematikmacarasi.models.DifficultyLevel
import kotlin.math.abs
import kotlin.random.Random

/**
 * Grade is a hard content ceiling. Difficulty changes practice complexity inside it,
 * never unlocks an operation prohibited for the student's selected grade.
 */
enum class OperationKind(val title: String, val symbol: String, val minGrade: Int) {
    ADD("Toplama", "+", 1),
    SUBTRACT("Çıkarma", "−", 1),
    MULTIPLY("Çarpma", "×", 2),
    DIVIDE("Bölme", "÷", 2)
}

data class GradeQuestion(val first: Int, val second: Int, val answer: Int)

object GradeAwareOperationPolicy {
    fun grade(value: String?): Int {
        val parsed = Regex("""^\s*([1-8])\s*(?:\.|\s|$)""")
            .find(value.orEmpty())?.groupValues?.get(1)?.toIntOrNull()
        return (parsed ?: 1).coerceIn(1, 8)
    }

    fun allowed(grade: Int, op: OperationKind): Boolean = grade >= op.minGrade

    fun maxAnswer(grade: Int, difficulty: DifficultyLevel): Int {
        val level = difficulty.ordinal.coerceIn(0, 4)
        return when (grade.coerceIn(1, 8)) {
            1 -> listOf(10, 12, 15, 18, 20)[level]
            2 -> listOf(20, 40, 60, 80, 100)[level]
            3 -> listOf(50, 100, 150, 250, 500)[level]
            4 -> listOf(100, 250, 500, 800, 1000)[level]
            5 -> listOf(250, 500, 1000, 2000, 4000)[level]
            6 -> listOf(500, 1000, 2000, 4000, 6000)[level]
            7 -> listOf(1000, 2000, 4000, 6000, 10000)[level]
            else -> listOf(2000, 4000, 6000, 10000, 20000)[level]
        }
    }

    private fun factorCeiling(grade: Int, level: Int): Int = when (grade.coerceIn(3, 8)) {
        3 -> listOf(6, 7, 8, 9, 10)[level]
        4 -> listOf(7, 9, 10, 12, 15)[level]
        5 -> listOf(10, 12, 15, 18, 20)[level]
        6 -> listOf(12, 15, 18, 22, 25)[level]
        7 -> listOf(15, 18, 22, 27, 30)[level]
        else -> listOf(18, 22, 27, 32, 40)[level]
    }

    /** Division always produces a whole-number quotient; subtraction never goes below zero. */
    fun generate(
        grade: Int,
        op: OperationKind,
        difficulty: DifficultyLevel,
        random: Random = Random.Default
    ): GradeQuestion {
        require(allowed(grade, op)) { "Operation not permitted for grade $grade" }
        val cap = maxAnswer(grade, difficulty)
        val level = difficulty.ordinal.coerceIn(0, 4)
        return when (op) {
            OperationKind.ADD -> {
                val sum = random.nextInt(3, cap + 1)
                val a = random.nextInt(1, sum)
                GradeQuestion(a, sum - a, sum)
            }
            OperationKind.SUBTRACT -> {
                val a = random.nextInt(3, cap + 1)
                val b = random.nextInt(1, a + 1)
                GradeQuestion(a, b, a - b)
            }
            OperationKind.MULTIPLY -> {
                if (grade == 2) {
                    // MEB MAT.2.2.4: 1..5 times 1..10; smaller first tiers.
                    val factor = random.nextInt(1, minOf(5, level + 2) + 1)
                    val other = random.nextInt(1, (if (level == 0) 6 else 10) + 1)
                    if (random.nextBoolean())
                        GradeQuestion(factor, other, factor * other)
                    else GradeQuestion(other, factor, factor * other)
                } else {
                    val ceiling = factorCeiling(grade, level)
                    val a = random.nextInt(2, ceiling + 1)
                    val b = random.nextInt(2, ceiling + 1)
                    GradeQuestion(a, b, a * b)
                }
            }
            OperationKind.DIVIDE -> {
                if (grade == 2) {
                    // MEB MAT.2.2.4: exact division of numbers <=20 by 2..5.
                    val divisor = random.nextInt(2, minOf(5, level + 2) + 1)
                    val quotient = random.nextInt(1, (20 / divisor) + 1)
                    GradeQuestion(divisor * quotient, divisor, quotient)
                } else {
                    val ceiling = factorCeiling(grade, level)
                    val divisor = random.nextInt(2, ceiling + 1)
                    val quotient = random.nextInt(2, ceiling + 1)
                    GradeQuestion(divisor * quotient, divisor, quotient)
                }
            }
        }
    }

    /** Commutative swaps count as the same learning item. */
    fun questionKey(op: OperationKind, question: GradeQuestion): String {
        val a = if (op == OperationKind.ADD || op == OperationKind.MULTIPLY)
            minOf(question.first, question.second) else question.first
        val b = if (op == OperationKind.ADD || op == OperationKind.MULTIPLY)
            maxOf(question.first, question.second) else question.second
        return "${op.name}:$a:$b"
    }

    /**
     * Avoid near-immediate repeats. When a small grade/level question pool is
     * exhausted, gracefully fall back rather than looping forever.
     */
    fun generateUnique(
        grade: Int,
        op: OperationKind,
        difficulty: DifficultyLevel,
        seen: MutableSet<String>,
        random: Random = Random.Default
    ): GradeQuestion {
        repeat(100) {
            val question = generate(grade, op, difficulty, random)
            if (seen.add(questionKey(op, question))) return question
        }
        val fallback = generate(grade, op, difficulty, random)
        seen.add(questionKey(op, fallback))
        return fallback
    }

    /** Distractors mix common calculation errors with near misses, never duplicates. */
    fun options(question: GradeQuestion, random: Random = Random.Default): List<String> =
        options(question, null, random)

    fun options(
        question: GradeQuestion,
        op: OperationKind?,
        random: Random = Random.Default
    ): List<String> {
        val a = question.first
        val b = question.second
        val answer = question.answer
        val candidates = mutableListOf<Int>()
        when (op) {
            OperationKind.MULTIPLY -> {
                candidates.addAll(listOf(a * (b - 1), a * (b + 1), (a - 1) * b, (a + 1) * b))
            }
            OperationKind.DIVIDE -> {
                candidates.addAll(listOf(answer + b, answer - b, answer * 2, b, answer + 1))
            }
            OperationKind.ADD, OperationKind.SUBTRACT, null -> {
                if (answer >= 10) candidates.addAll(listOf(answer - 10, answer + 10))
                if (answer >= 20) candidates.addAll(listOf(answer - 5, answer + 5))
                if (op == OperationKind.SUBTRACT) candidates.add(abs(a - (b + 1)))
            }
        }
        val unique = linkedSetOf(answer)
        candidates.filter { it >= 0 && it != answer }.shuffled(random).forEach {
            if (unique.size < 3) unique.add(it)
        }
        val nearby = listOf(1, -1, 2, -2, 3, -3, 5, -5, 10, -10, 20, -20)
            .map { answer + it }.filter { it >= 0 && it != answer }.shuffled(random)
        for (candidate in nearby) {
            if (unique.size >= 4) break
            unique.add(candidate)
        }
        var offset = 1
        while (unique.size < 4) unique.add(answer + offset++)
        return unique.toList().shuffled(random).map(Int::toString)
    }
}
