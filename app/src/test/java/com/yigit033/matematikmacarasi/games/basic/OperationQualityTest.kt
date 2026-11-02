package com.yigit033.matematikmacarasi.games.basic

import com.yigit033.matematikmacarasi.models.DifficultyLevel
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class OperationQualityTest {
    @Test fun eachGradeUsesValidArithmeticAndFourDistinctChoices() {
        for (grade in 1..8) {
            for (level in DifficultyLevel.entries) {
                for (kind in OperationKind.entries.filter {
                    GradeAwareOperationPolicy.allowed(grade, it)
                }) {
                    val rng = Random(grade * 200 + level.ordinal * 10 + kind.ordinal)
                    repeat(75) {
                        val q = GradeAwareOperationPolicy.generate(grade, kind, level, rng)
                        assertTrue(q.first > 0 && q.second > 0)
                        assertTrue(q.answer >= 0)
                        when (kind) {
                            OperationKind.ADD -> assertEquals(q.first + q.second, q.answer)
                            OperationKind.SUBTRACT -> assertEquals(q.first - q.second, q.answer)
                            OperationKind.MULTIPLY -> assertEquals(q.first * q.second, q.answer)
                            OperationKind.DIVIDE -> {
                                assertEquals(0, q.first % q.second)
                                assertEquals(q.first / q.second, q.answer)
                            }
                        }
                        assertTrue(q.answer <= GradeAwareOperationPolicy.maxAnswer(grade, level))
                        val options = GradeAwareOperationPolicy.options(q, kind, rng)
                        assertEquals(4, options.size)
                        assertEquals(4, options.toSet().size)
                        assertTrue(options.contains(q.answer.toString()))
                        assertTrue(options.all { it.toInt() >= 0 })
                    }
                }
            }
        }
    }

    @Test fun noRepeatedQuestionsWhenPoolHasEnoughItems() {
        for ((grade, op) in listOf(
            1 to OperationKind.ADD,
            1 to OperationKind.SUBTRACT,
            2 to OperationKind.ADD,
            3 to OperationKind.MULTIPLY,
            3 to OperationKind.DIVIDE
        )) {
            val seen = mutableSetOf<String>()
            val random = Random(grade * 31 + op.ordinal)
            repeat(10) {
                val q = GradeAwareOperationPolicy.generateUnique(
                    grade, op, DifficultyLevel.EASY, seen, random
                )
                assertEquals(it + 1, seen.size)
                assertTrue(seen.contains(GradeAwareOperationPolicy.questionKey(op, q)))
            }
        }
    }

    @Test fun swapsOfCommutativeOperandsAreNotRepeated() {
        val left = GradeQuestion(7, 8, 56)
        val right = GradeQuestion(8, 7, 56)
        assertEquals(
            GradeAwareOperationPolicy.questionKey(OperationKind.MULTIPLY, left),
            GradeAwareOperationPolicy.questionKey(OperationKind.MULTIPLY, right)
        )
        assertNotEquals(
            GradeAwareOperationPolicy.questionKey(OperationKind.DIVIDE, left),
            GradeAwareOperationPolicy.questionKey(OperationKind.DIVIDE, right)
        )
    }

    @Test fun mixedModeBalancesAndRespectsGradeLocks() {
        for (grade in listOf(1, 2, 3, 5, 8)) {
            val engine = MixedOperationEngine(grade, DifficultyLevel.HARD, Random(842 + grade))
            val allowed = OperationKind.entries.filter {
                GradeAwareOperationPolicy.allowed(grade, it)
            }
            val seen = mutableSetOf<String>()
            repeat(12) { index ->
                val (op, q) = engine.next()
                assertTrue(op in allowed)
                assertTrue(seen.add(GradeAwareOperationPolicy.questionKey(op, q)))
                engine.record(op, index != 3 && index != 7)
            }
            val counts = allowed.map(engine::count)
            assertEquals(12, counts.sum())
            assertTrue("Operations should stay represented", counts.minOrNull()!! >= 2)
            assertTrue("Wrong answers should not starve a skill", counts.maxOrNull()!! - counts.minOrNull()!! <= 3)
        }
    }

    @Test fun advancedDistractorsAreNotJustConsecutiveIntegers() {
        val question = GradeQuestion(7, 8, 56)
        val values = GradeAwareOperationPolicy.options(
            question, OperationKind.MULTIPLY, Random(23)
        ).map(String::toInt)
        assertEquals(4, values.toSet().size)
        assertTrue(values.any { it != 56 && kotlin.math.abs(it - 56) >= 7 })
    }
}
