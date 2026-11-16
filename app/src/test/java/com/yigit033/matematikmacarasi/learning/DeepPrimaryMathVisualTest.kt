package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepPrimaryMathVisualTest {
    @Test fun allDeepPrimaryProblemsHaveMatchingQuestionSpecificModels() {
        for (grade in 1..4) for (lesson in DeepLessonCatalog.forGrade(grade)) {
            for (seed in 1L..60L) for (index in 0..7) {
                val question=DeepLessonQuestionBank.question(lesson,seed,index)
                val model=PracticeVisualFactory.forQuestion(lesson.id,question.text)
                assertNotNull("No artwork for " + lesson.id + ": " + question.text,model)
                assertTrue(model!!.accessibilityText().isNotBlank())
                assertEquals(model,PracticeVisualFactory.forQuestion(
                    lesson.id,question.text))
            }
        }
    }

    @Test fun secondGradePlaceValueIllustrationsSupportBothCurrentAndLegacyQuestions() {
        val current = PracticeVisualFactory.forQuestion(
            "g2_hundred_place", "9 onluk ve 3 birlik kaç eder?"
        )
        assertNotNull(current)
        assertEquals(PracticeVisualKind.TENS_AND_ONES, current!!.kind)
        assertEquals(listOf(9, 3), current.values)
        assertTrue(current.accessibilityText().contains("9 onluk"))

        val previous = PracticeVisualFactory.forQuestion(
            "g2_hundred_place", "1 yüzlük, 2 onluk ve 3 birlik kaç eder?"
        )
        assertNotNull(previous)
        assertEquals(PracticeVisualKind.HUNDREDS, previous!!.kind)
        assertEquals(listOf(1, 2, 3), previous.values)
    }

    @Test fun tenFramesDoNotDrawTheAnswerAsAlreadyFilledItems() {
        val m=PracticeVisualFactory.forQuestion(
            "g1_make_ten","7 nesnenin 10 olması için kaç nesne eklemeliyiz?")!!
        assertEquals(PracticeVisualKind.TEN_FRAME,m.kind)
        assertEquals(listOf(7),m.values)
        assertTrue(m.accessibilityText().contains("3 boş"))
    }

    @Test fun fractionsAndDivisionReflectRealQuestionOperands() {
        val quarter=PracticeVisualFactory.forQuestion(
            "g2_quarter","24 nesnenin çeyreği kaç nesnedir?")!!
        assertEquals(PracticeVisualKind.FRACTION_PARTS,quarter.kind)
        assertEquals(listOf(24,1,4),quarter.values)

        val division=PracticeVisualFactory.forQuestion(
            "g3_share_equal",
            "30 nesne 5 çocuğa eşit dağıtılırsa her birine kaç nesne düşer?")!!
        assertEquals(PracticeVisualKind.EQUAL_SHARE,division.kind)
        assertEquals(listOf(30,5),division.values)

        val compare=PracticeVisualFactory.forQuestion(
            "g4_same_denominator",
            "2/8 ve 5/8 kesirlerinden büyük olanın payı kaçtır?")!!
        assertEquals(PracticeVisualKind.FRACTION_COMPARE,compare.kind)
        assertEquals(listOf(2,5,8),compare.values)
    }
}
