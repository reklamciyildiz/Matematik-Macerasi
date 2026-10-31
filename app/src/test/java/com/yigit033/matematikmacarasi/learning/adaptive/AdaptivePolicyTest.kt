package com.yigit033.matematikmacarasi.learning.adaptive

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptivePolicyTest {
    @Test fun profileGradesAreParsedWithoutContainsBugs() {
        assertEquals(0, GradePolicy.parse("Ana Sınıfı"))
        assertEquals(1, GradePolicy.parse("1. Sınıf"))
        assertEquals(8, GradePolicy.parse("8. Sınıf"))
        assertNull(GradePolicy.parse("18. Sınıf"))
        assertNull(GradePolicy.parse("tamamlanmadı"))
    }
    @Test fun schoolGradeSuggestsButDoesNotLockLevels() {
        val baseline = AdaptivePolicy.begin(null, 2, AdventureSkill.THREE_COUNTING)
        assertEquals(LearningTier.TEMEL, baseline.currentTier)
        val userChoice = AdaptivePolicy.withManualTier(baseline, LearningTier.USTALIK)
        assertEquals(LearningTier.USTALIK, userChoice.currentTier)
        assertEquals(LearningTier.TEMEL, AdaptivePolicy.withManualTier(userChoice, null).currentTier)
    }
    @Test fun highConfidenceNeedsTwoRunsAndNeverRewardsDuplicateRun() {
        val b = AdaptivePolicy.begin(null, 2, AdventureSkill.THREE_COUNTING)
        val one = AdaptivePolicy.completed(b, 101, LearningTier.TEMEL, 12, 0, 0)
        assertEquals(LearningTier.TEMEL, one.currentTier)
        assertEquals(one, AdaptivePolicy.completed(one, 101, LearningTier.TEMEL, 12, 0, 0))
        val two = AdaptivePolicy.completed(one, 102, LearningTier.TEMEL, 12, 1, 0)
        assertEquals(LearningTier.GELISEN, two.autoTier)
        assertEquals(2, two.completedRuns)
        assertEquals(24, two.totalAttempts)
    }
    @Test fun struggleNeedsTwoSessionsAndManualSelectionNeverOverrides() {
        var p = AdaptivePolicy.begin(null, 4, AdventureSkill.FOUR_COUNTING)
        p = AdaptivePolicy.completed(p, 201, LearningTier.USTALIK, 10, 5, 4)
        assertEquals(LearningTier.USTALIK, p.currentTier)
        p = AdaptivePolicy.completed(p, 202, LearningTier.USTALIK, 10, 5, 4)
        assertEquals(LearningTier.GELISEN, p.currentTier)
        val manual = AdaptivePolicy.withManualTier(p, LearningTier.USTALIK)
        val after = AdaptivePolicy.completed(manual, 203, LearningTier.USTALIK, 10, 0, 0)
        assertEquals(LearningTier.USTALIK, after.currentTier)
        assertEquals(LearningTier.GELISEN, after.autoTier)
    }
    @Test fun profileGradeChangePreservesRecordedLearningHistory() {
        val p = AdaptivePolicy.completed(
            AdaptivePolicy.begin(null, 2, AdventureSkill.FOUR_COUNTING),
            304, LearningTier.TEMEL, 12, 3, 1
        )
        val changed = AdaptivePolicy.begin(p, 4, AdventureSkill.FOUR_COUNTING)
        assertEquals(LearningTier.USTALIK, changed.autoTier)
        assertEquals(p.totalAttempts, changed.totalAttempts)
        assertEquals(p.lastRunId, changed.lastRunId)
        assertTrue(changed.completedRuns > 0)
    }
    @Test fun smallIncompleteSampleDoesNotDriveDifficulty() {
        val s = AdaptivePolicy.begin(null, 2, AdventureSkill.THREE_COUNTING)
        val next = AdaptivePolicy.completed(s, 500, LearningTier.TEMEL, 1, 0, 0)
        assertEquals(LearningTier.TEMEL, next.autoTier)
        assertEquals(0, next.cleanStreak)
    }
}
