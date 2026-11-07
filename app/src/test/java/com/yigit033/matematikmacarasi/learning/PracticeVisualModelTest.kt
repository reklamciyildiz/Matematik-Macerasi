package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** The illustrated quantities must match the question currently on screen. */
class PracticeVisualModelTest {
    private val supported = listOf(
        "add_10", "add_20", "g1_count20", "g1_takeaway", "g1_compare",
        "g1_sort_data", "g2_place", "g2_half", "g2_clock",
        "g2_corners", "g2_simple_table", "g3_third",
        "g3_chart", "g4_fraction", "g4_chart", "g3_rectangle",
        "g4_perimeter"
    )

    @Test fun diagramParsersWorkOnRealGeneratedQuestions() {
        for (lessonId in supported) {
            val lesson = (1..8).flatMap {
                LearningPathCatalog.forGrade("$it. Sınıf")
            }.first { it.id == lessonId }
            for (seed in 1L..30L) for (index in 0..3) {
                val question = LearningPathCatalog.question(lesson, seed, index)
                val model = PracticeVisualFactory.forQuestion(
                    lessonId, question.text)
                assertNotNull("$lessonId: " + question.text, model)
                assertTrue(model!!.values.isNotEmpty())
            }
        }
    }

    @Test fun knownVisualsNeverUseAnUnrelatedWorkedExample() {
        val take = PracticeVisualFactory.forQuestion("g1_takeaway",
            "8 elmadan 5 elma ayrılırsa kaç elma kalır?")
        assertEquals(PracticeVisualKind.TAKE_AWAY, take?.kind)
        assertEquals(listOf(8, 5), take?.values)

        val count = PracticeVisualFactory.forQuestion("g1_count20",
            "10 nesneye 9 nesne eklenirse toplam kaç olur?")
        assertEquals(listOf(1, 9), count?.values)

        val half = PracticeVisualFactory.forQuestion("g2_half",
            "18 nesnenin yarısı kaç nesnedir?")
        assertEquals(listOf(18), half?.values)

        val hours = PracticeVisualFactory.forQuestion("g2_clock",
            "Saat 5 iken 2 saat sonra saat kaç olur?")
        assertEquals(listOf(5, 2), hours?.values)
    }

    @Test fun malformedQuestionsFailClosedWithoutInventingArtwork() {
        assertNull(PracticeVisualFactory.forQuestion("g1_takeaway",
            "Ağaçta birkaç meyve var"))
        assertNull(PracticeVisualFactory.forQuestion("unknown",
            "8 + 3 = ?"))
        assertNull(PracticeVisualFactory.forQuestion("g4_fraction",
            "Kesirleri karşılaştır"))
    }
    @Test fun talkbackDescriptionUsesLiveQuestionCounts() {
        val take=PracticeVisualFactory.forQuestion(
            "g1_takeaway", "9 elmadan 6 elma ayrılırsa kaç elma kalır?"
        )!!
        assertTrue(take.accessibilityText().contains("9 nesneden 6"))
        val newQuestion=PracticeVisualFactory.forQuestion(
            "g1_takeaway", "7 elmadan 2 elma ayrılırsa kaç elma kalır?"
        )!!
        assertTrue(newQuestion.accessibilityText().contains("7 nesneden 2"))
        val bars=PracticeVisualFactory.forQuestion(
            "g4_chart", "Grafikte A=4, B=6, C=3. Toplam kaçtır?"
        )!!
        assertTrue(bars.accessibilityText().contains("A grubu: 4"))
        assertTrue(bars.accessibilityText().contains("C grubu: 3"))
        val time=PracticeVisualFactory.forQuestion(
            "g2_clock", "Saat 7 iken 2 saat sonra saat kaç olur?"
        )!!
        assertTrue(time.accessibilityText().contains("Saat 7"))
    }

}
