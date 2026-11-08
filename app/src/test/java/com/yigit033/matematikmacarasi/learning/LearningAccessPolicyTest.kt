package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningAccessPolicyTest {
    @Test fun twoFreeLessonsSpanDifferentRealTopicsForEveryGrade() {
        for (grade in 1..8) {
            val free = LearningAccessPolicy.freeLessonIds(grade)
            val topics = GradeCurriculumCatalog.forGrade(grade).topics
            val all = LearningPathCatalog.forGrade("$grade. Sınıf")
            assertEquals("grade $grade",2,free.size)
            assertTrue(free.all { id -> all.any { it.id == id } })
            val topicIds = free.map { id ->
                topics.first { id in it.lessonIds }.id
            }
            assertEquals(2,topicIds.distinct().size)
            val areas = free.map { id ->
                topics.first { id in it.lessonIds }.area
            }
            assertEquals("Two free units must show distinct mathematical strands for grade $grade",
                2,areas.distinct().size)
            assertEquals(free, LearningAccessPolicy.freeLessonIds(grade))
        }
    }

    @Test fun existingPremiumUnlocksEveryLessonAndFreeTierCannotBypass() {
        for (grade in 1..8) {
            val free = LearningAccessPolicy.freeLessonIds(grade)
            val all = LearningPathCatalog.forGrade("$grade. Sınıf")
            assertTrue(all.size >= 9)
            for (lesson in all) {
                assertEquals(lesson.id in free,
                    LearningAccessPolicy.accessible(grade,lesson.id,false))
                assertTrue(LearningAccessPolicy.accessible(grade,lesson.id,true))
            }
        }
    }

    @Test fun grandfatheredPreviouslyCompletedLessonsStayAccessibleButDoNotUnlockOthers() {
        val grade=8
        val premiumId=LearningPathCatalog.forGrade("8. Sınıf")
            .first { it.id !in LearningAccessPolicy.freeLessonIds(grade) }.id
        val other=LearningPathCatalog.forGrade("8. Sınıf")
            .first { it.id !in LearningAccessPolicy.freeLessonIds(grade) && it.id != premiumId }.id
        val legacy=setOf(premiumId)
        assertTrue(LearningAccessPolicy.accessible(grade,premiumId,false,legacy))
        assertFalse(LearningAccessPolicy.accessible(grade,other,false,legacy))
    }
}
