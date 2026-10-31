package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningPathCatalogTest {
    @Test fun lessonsAdaptToGradeWithoutPretendingToBeFullCurriculum() {
        assertEquals("count_2", LearningPathCatalog.forGrade("1. Sınıf").first().id)
        assertEquals("count_3", LearningPathCatalog.forGrade("2. Sınıf").first().id)
        assertTrue(LearningPathCatalog.forGrade("2. Sınıf").any { it.id == "count_4" })
        assertTrue(LearningPathCatalog.forGrade("2. Sınıf").any { it.id == "multiply_intro" })
        assertTrue(LearningPathCatalog.forGrade("3. Sınıf").any { it.id == "count_4" })
        assertEquals("multiply", LearningPathCatalog.forGrade("3. Sınıf").first().id)
        assertEquals("divisible_15", LearningPathCatalog.forGrade("7. Sınıf").first().id)
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
                assertTrue(q.answer > 0)
                assertEquals(q, LearningPathCatalog.question(lesson, seed, i))
            }
        }
    }
}
