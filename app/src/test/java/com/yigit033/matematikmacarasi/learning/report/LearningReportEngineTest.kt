package com.yigit033.matematikmacarasi.learning.report

import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveProgress
import com.yigit033.matematikmacarasi.learning.adaptive.AdventureSkill
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LearningReportEngineTest {
    private fun p(runs:Int,attempts:Int,errors:Int,hints:Int) =
        AdaptiveProgress(autoTier=LearningTier.GELISEN,profileGrade=3,
            completedRuns=runs,totalAttempts=attempts,totalErrors=errors,
            totalHints=hints)

    @Test fun emptyRecordsNeverClaimMasteryOrInventPercentages() {
        val overview=LearningReportEngine.build(emptyMap())
        assertEquals(4,overview.skills.size)
        assertEquals(0,overview.completedSessions)
        assertEquals(0,overview.practicedSkills)
        assertNull(overview.accuracyPercent)
        overview.skills.forEach { item ->
            assertNull(item.accuracyPercent)
            assertFalse(item.needsPractice)
            assertFalse(item.enoughToDiscuss)
            assertEquals("Henüz tamamlanmış oyun kaydı yok",item.interpretation)
        }
    }

    @Test fun geometricAndMarketEventsAreIncludedWithoutInflatingMastery() {
        val overview=LearningReportEngine.build(mapOf(
            AdventureSkill.THREE_COUNTING to p(2,20,4,1),
            AdventureSkill.GEOMETRY to p(3,32,9,2),
            AdventureSkill.MARKET to p(2,12,3,0)
        ))
        assertEquals(7,overview.completedSessions)
        assertEquals(64,overview.attempts)
        assertEquals(16,overview.errors)
        assertEquals(3,overview.hints)
        assertEquals(75,overview.accuracyPercent)
        assertEquals(3,overview.practicedSkills)
        assertEquals(4,overview.skills.size)
        assertTrue(overview.skills.first {it.skill.id==AdventureSkill.GEOMETRY}
            .interpretation.contains("alıştırmalar"))
        assertTrue(overview.skills.any {it.skill.id==AdventureSkill.FOUR_COUNTING &&
            !it.hasEvidence})
    }

    @Test fun supportSuggestionsRequireMultipleMeaningfulCompletedSessions() {
        val tooSoon=LearningReportEngine.build(mapOf(
            AdventureSkill.GEOMETRY to p(1,5,4,8)))
        assertFalse(tooSoon.skills.first{it.skill.id==AdventureSkill.GEOMETRY}
            .needsPractice)
        val struggling=LearningReportEngine.build(mapOf(
            AdventureSkill.GEOMETRY to p(2,20,9,3)))
        val evidence=struggling.skills.first{it.skill.id==AdventureSkill.GEOMETRY}
        assertTrue(evidence.needsPractice)
        assertEquals(55,evidence.accuracyPercent)
        assertEquals(AdventureSkill.GEOMETRY,struggling.suggested?.skill?.id)
        assertTrue(evidence.nextPractice.contains("simetri"))
    }

    @Test fun hugeAttemptCountDoesNotOverflowPercentageAndKeepsAllSkills() {
        val report=LearningReportEngine.build(mapOf(
            AdventureSkill.MARKET to p(3,Int.MAX_VALUE,400,0)))
        // Truncation intentionally avoids advertising 100% when errors exist.
        assertEquals(99,report.accuracyPercent)
        assertEquals(4,report.skills.size)
        assertTrue(report.skills.any{it.skill.id==AdventureSkill.MARKET &&
            it.skill.region=="Pazar Simülatörü"})
    }
}
