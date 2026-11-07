package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningReviewPolicyTest {
    @Test fun hesitationOrWrongAnswersTriggerEarlierReviewWithoutDeletingProgress() {
        val needHelp = LearningReviewPolicy.recommend(
            LearningSessionEvidence(wrong=4, hints=1, conceptErrors=1, previousRuns=0)
        )
        assertEquals(ReviewSignal.REVISIT_WITH_HELP, needHelp.signal)
        assertEquals(1, needHelp.waitDays)
        assertEquals(0, needHelp.cleanStreak)

        val moderate = LearningReviewPolicy.recommend(
            LearningSessionEvidence(wrong=1, hints=0, conceptErrors=0, previousRuns=2)
        )
        assertEquals(ReviewSignal.REVISIT, moderate.signal)
        assertEquals(2, moderate.waitDays)
    }

    @Test fun repeatCleanSessionsExtendIntervalButDoNotClaimMastery() {
        val first = LearningReviewPolicy.recommend(
            LearningSessionEvidence(0, 0, 0, 0)
        )
        val second = LearningReviewPolicy.recommend(
            LearningSessionEvidence(0, 0, 0, 1, first.cleanStreak)
        )
        val third = LearningReviewPolicy.recommend(
            LearningSessionEvidence(0, 0, 0, 2, second.cleanStreak)
        )
        assertEquals(3, first.waitDays)
        assertEquals(3, second.waitDays)
        assertEquals(7, third.waitDays)
        assertEquals(3, third.cleanStreak)
        assertTrue(third.explanation.contains("kontrol"))
    }

    @Test fun mistakesResetCleanStreak() {
        val outcome = LearningReviewPolicy.recommend(
            LearningSessionEvidence(wrong=0, hints=0, conceptErrors=1,
                previousRuns=5, cleanStreak=5)
        )
        assertEquals(0, outcome.cleanStreak)
        assertEquals(2, outcome.waitDays)
    }

    @Test fun dueDateNeverSuggestsIncompleteOrUnscheduledLesson() {
        val t = 1_900_000_000_000L
        val dueAt = LearningReviewPolicy.nextReviewAt(t, 3)
        assertFalse(LearningReviewPolicy.due(false, dueAt+1, dueAt))
        assertFalse(LearningReviewPolicy.due(true, dueAt-1, dueAt))
        assertFalse(LearningReviewPolicy.due(true, dueAt+1, 0L))
        assertTrue(LearningReviewPolicy.due(true, dueAt, dueAt))
        assertEquals(3L * 86_400_000L, dueAt - t)
    }
}
