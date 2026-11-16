package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningAnalyticsModelTest {
    private fun evidence(
        id: String, completed: Boolean = false,
        sessions: Int = 0, best: Int? = null,
        due: Boolean = false, focus: String? = null,
        wrong: Int = 0, hints: Int = 0
    ) = LessonLearningEvidence(
        id, id, completed, sessions, best, due, focus, wrong, hints
    )

    @Test fun freeLearnerSeesUsefulSummaryButNotPremiumBreakdown() {
        assertTrue(LearningAnalyticsPolicy.canViewSummary(false))
        assertTrue(LearningAnalyticsPolicy.canViewSummary(true))
        assertFalse(LearningAnalyticsPolicy.canViewTopicBreakdown(false))
        assertFalse(LearningAnalyticsPolicy.canViewMistakeFocus(false))
        assertTrue(LearningAnalyticsPolicy.canViewTopicBreakdown(true))
        assertTrue(LearningAnalyticsPolicy.canViewMistakeFocus(true))
    }

    @Test fun sharedHistoricLessonsAreNotInventedAsGradeSpecific() {
        for (grade in 1..8) {
            assertFalse(LearningAnalyticsPolicy.shouldAttributeLegacy(grade, "add_20"))
            assertFalse(LearningAnalyticsPolicy.shouldAttributeLegacy(grade, "count_3"))
            assertTrue(LearningAnalyticsPolicy.shouldAttributeLegacy(grade, "g${grade}_area"))
            val otherGrade = if (grade == 8) 1 else grade + 1
            assertFalse(LearningAnalyticsPolicy.shouldAttributeLegacy(grade, "g${otherGrade}_area"))
        }
    }

    @Test fun profileNumbersUseOnlyRecordedGradeEvidence() {
        val lessons = listOf(
            evidence("g3_area", true, 2, 83),
            evidence("g3_data", true, 3, 93, due = true, focus = "DATA_READING", wrong = 2),
            evidence("multiply", false)
        )
        val report = GradeLearningSnapshot(3, lessons, unattributedHistoricalSharedLessons = 1)
        assertEquals(3, report.totalLessons)
        assertEquals(2, report.completed)
        assertEquals(5, report.sessions)
        assertEquals(1, report.due)
        assertEquals(93, report.bestPercent)
        assertEquals(1, report.unattributedHistoricalSharedLessons)
        assertEquals("g3_data", report.recentlyDifficult.single().lessonId)
        assertEquals(2f / 3f, report.completionFraction)
        assertEquals(1, LearningAnalyticsPolicy.topicCompleted(
            listOf("g3_area", "multiply"), report.lessons
        ))
    }

    @Test fun emptyClassIsValidAndDoesNotInventProgress() {
        val empty = GradeLearningSnapshot(1, emptyList(), 0)
        assertEquals(0, empty.completed)
        assertEquals(0, empty.sessions)
        assertEquals(0, empty.due)
        assertEquals(0f, empty.completionFraction)
        assertNull(empty.bestPercent)
    }

    @Test fun percentageComputationIsBounded() {
        assertEquals(0, LearningAnalyticsPolicy.accuratePercent(0, 0))
        assertEquals(100, LearningAnalyticsPolicy.accuratePercent(8, 0))
        assertEquals(80, LearningAnalyticsPolicy.accuratePercent(8, 2))
        assertEquals(100, LearningAnalyticsPolicy.accuratePercent(8, -10))
    }
}
