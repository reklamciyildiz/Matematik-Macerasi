package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningPathCatalogTest {
    @Test fun lessonsAdaptToGradeWithoutPretendingToBeFullCurriculum() {
        assertEquals("g1_count20", LearningPathCatalog.forGrade("1. Sınıf").first().id)
        assertEquals("g2_place", LearningPathCatalog.forGrade("2. Sınıf").first().id)
        assertTrue(LearningPathCatalog.forGrade("2. Sınıf").any { it.id == "count_4" })
        assertTrue(LearningPathCatalog.forGrade("2. Sınıf").any { it.id == "multiply_intro" })
        assertTrue(LearningPathCatalog.forGrade("3. Sınıf").any { it.id == "count_4" })
        assertEquals("g3_three_digit", LearningPathCatalog.forGrade("3. Sınıf").first().id)
        assertEquals("g7_rational", LearningPathCatalog.forGrade("7. Sınıf").first().id)
    }
    @Test fun gradeTwoMultiplicationIntroUsesSmallEqualGroups() {
        val intro = LearningPathCatalog.forGrade("2. Sınıf").first { it.id == "multiply_intro" }
        for (seed in 1L..200L) {
            val q = LearningPathCatalog.question(intro, seed, 0)
            assertTrue(q.answer in 4..25)
            assertTrue(q.text.contains("eş grup"))
        }
    }
    @Test fun generatedQuestionsHaveOneCorrectUniqueAnswer() {
        for (grade in 1..8) for (lesson in LearningPathCatalog.forGrade("$grade. Sınıf")) {
            for (seed in 1L..50L) for (i in 0..3) {
                val q = LearningPathCatalog.question(lesson, seed, i)
                assertEquals(4, q.choices.size)
                assertEquals(4, q.choices.distinct().size)
                assertEquals(1, q.choices.count { it == q.answer })
                // Coordinate reflections legitimately have negative answers.
                assertTrue(q.answer in -1000..10000)
                assertEquals(q, LearningPathCatalog.question(lesson, seed, i))
            }
        }
    }
}
