package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveLearningPolicyTest {
    @Test fun newStudentsStartInTheCoreBand() {
        assertEquals(PracticeBand.CORE, AdaptiveLearningPolicy.initialBand(0, 0, 0, 0))
    }

    @Test fun repeatDifficultyDependsOnRecordedWork() {
        assertEquals(PracticeBand.SUPPORTED, AdaptiveLearningPolicy.initialBand(1, 2, 0, 0))
        assertEquals(PracticeBand.SUPPORTED, AdaptiveLearningPolicy.initialBand(1, 0, 0, 1))
        assertEquals(PracticeBand.EXTENDED, AdaptiveLearningPolicy.initialBand(3, 0, 0, 0))
        assertEquals(PracticeBand.CORE, AdaptiveLearningPolicy.initialBand(1, 0, 0, 0))
    }

    @Test fun wrongOrHintedQuestionMakesNextQuestionSupported() {
        for (tier in PracticeBand.values()) {
            assertEquals(PracticeBand.SUPPORTED,
                AdaptiveLearningPolicy.nextBand(tier, 1, false, false))
            assertEquals(PracticeBand.SUPPORTED,
                AdaptiveLearningPolicy.nextBand(tier, 0, true, false))
            assertEquals(PracticeBand.SUPPORTED,
                AdaptiveLearningPolicy.nextBand(tier, 0, false, true))
        }
    }

    @Test fun cleanWorkGraduallyIncreasesVariation() {
        assertEquals(PracticeBand.CORE, AdaptiveLearningPolicy.nextBand(
            PracticeBand.SUPPORTED, 0, false, false))
        assertEquals(PracticeBand.EXTENDED, AdaptiveLearningPolicy.nextBand(
            PracticeBand.CORE, 0, false, false))
    }

    @Test fun wrongOptionIsEvidenceNotAClaimOfDiagnosedLearningDisability() {
        val lesson = Lesson("g4_same_denominator", "Paydalar", "", "", "", Skill.DEEP_DIVE)
        val q = PracticeQuestion("3/8 ve 5/8", listOf(3, 5, 2, 8), 5, "Paydaları aynı.")
        assertEquals(PracticeFocus.FRACTIONS, AdaptiveLearningPolicy.classify(lesson, q, 3))
        val help = AdaptiveLearningPolicy.response(lesson, q, 3)
        assertTrue(help.firstStep.contains("eş parça"))
        assertTrue(help.heading.contains("kesir"))
    }

    @Test fun oppositeSignSuggestsNumberLine() {
        val lesson = Lesson("g8_coordinate_mirror", "Yansıma", "", "", "", Skill.DEEP_DIVE)
        val q = PracticeQuestion("x = -3", listOf(-3, 3, 2, 4), -3, "")
        assertEquals(PracticeFocus.SIGNS, AdaptiveLearningPolicy.classify(lesson, q, 3))
    }

    @Test fun offByOneSuggestsCheckingTheLastStep() {
        val lesson = Lesson("g2_quarter", "Çeyrek", "", "", "", Skill.DEEP_DIVE)
        val q = PracticeQuestion("16 / 4 = ?", listOf(4, 3, 5, 8), 4, "")
        assertEquals(PracticeFocus.STEP, AdaptiveLearningPolicy.classify(lesson, q, 3))
    }

    @Test fun deterministicBandChoicesRemainValidForEveryGradeAndLesson() {
        for (grade in 1..8) {
            for (lesson in LearningPathCatalog.forGrade("$grade. Sınıf")) {
                val last = LearningPathCatalog.practiceQuestionCount(lesson) - 1
                for (index in listOf(0, last)) {
                    val original = LearningPathCatalog.question(lesson, 10103L, index)
                    assertEquals(original,
                        AdaptiveLearningPolicy.question(lesson, 10103L, index, PracticeBand.CORE))
                    for (tier in PracticeBand.values()) {
                        val q = AdaptiveLearningPolicy.question(lesson, 10103L, index, tier)
                        assertEquals(q, AdaptiveLearningPolicy.question(
                            lesson, 10103L, index, tier))
                        assertEquals(4, q.choices.distinct().size)
                        assertTrue(q.answer in q.choices)
                        assertTrue(q.text.isNotBlank() && q.explanation.isNotBlank())
                    }
                }
            }
        }
    }

    @Test fun supportQuestionIsSameLessonButDifferentVariant() {
        for (grade in 1..8) {
            for (lesson in LearningPathCatalog.forGrade("$grade. Sınıf")) {
                val q = AdaptiveLearningPolicy.question(lesson, 87601L, 0, PracticeBand.CORE)
                val support = AdaptiveLearningPolicy.supportQuestion(lesson, 87601L, 0, q)
                assertEquals(4, support.choices.distinct().size)
                assertTrue(support.answer in support.choices)
                assertTrue(support.text.isNotBlank() && support.explanation.isNotBlank())
                // A bank with insufficient variety is allowed to decline the
                // extra exercise; the UI only advertises it when it differs.
            }
        }
    }

    @Test fun numericalSelectionBiasDoesNotChangeCurriculumTopic() {
        val lesson = LearningPathCatalog.forGrade("8. Sınıf")
            .first { it.id == "g8_coordinate_mirror" }
        val basic = AdaptiveLearningPolicy.question(lesson, 8123L, 0, PracticeBand.SUPPORTED)
        val varied = AdaptiveLearningPolicy.question(lesson, 8123L, 0, PracticeBand.EXTENDED)
        assertTrue(basic.text.contains("yansıtılırsa"))
        assertTrue(varied.text.contains("yansıtılırsa"))
        assertTrue(basic.answer < 0 && varied.answer < 0)
        assertTrue(AdaptiveLearningPolicy.complexity(basic) <=
            AdaptiveLearningPolicy.complexity(varied))
    }

    @Test fun reviewAdviceIsInstructionalNotMasteryCertification() {
        val advice = AdaptiveLearningPolicy.targetedReview(PracticeFocus.PLACE_VALUE)
        assertTrue(advice.contains("basamak"))
        assertTrue(advice.contains("görsel"))
    }
}
