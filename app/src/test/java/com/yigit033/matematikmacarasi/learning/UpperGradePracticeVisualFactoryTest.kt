package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpperGradePracticeVisualFactoryTest {
    private val targets = setOf(
        "g5_area", "g5_mean", "g5_simple_chance",
        "g6_area_units", "g6_data_range", "g6_signed",
        "g6_equation", "g6_ratio",
        "g7_area_compare", "g7_median", "g7_probability",
        "g7_equation", "g7_mirror", "g7_proportion",
        "g8_data_probability", "g8_gcd", "g8_power",
        "g8_equation", "g8_right_triangle", "g8_prism_volume"
    )

    @Test fun allSelectedSecondaryLessonsRenderTheirOwnQuestionParameters() {
        val lessons = (5..8).flatMap {
            LearningPathCatalog.forGrade("$it. Sınıf")
        }.associateBy { it.id }
        for (id in targets) {
            val lesson = requireNotNull(lessons[id])
            for (seed in 1L..70L) for (index in 0..3) {
                val q = LearningPathCatalog.question(lesson,seed,index)
                val m = PracticeVisualFactory.forQuestion(id,q.text)
                assertNotNull("No visual for $id: " + q.text,m)
                assertTrue(m!!.values.isNotEmpty())
                assertTrue(m.accessibilityText().isNotBlank())
                assertEquals(m,PracticeVisualFactory.forQuestion(id,q.text))
            }
        }
    }

    @Test fun arithmeticAndGeometryValuesStayGroundedInTheDisplayedProblem() {
        val a = PracticeVisualFactory.forQuestion(
            "g8_right_triangle",
            "Dik kenarları 5 ve 12 birim olan dik üçgenin hipotenüsü kaç birimdir?"
        )!!
        assertEquals(PracticeVisualKind.RIGHT_TRIANGLE,a.kind)
        assertEquals(listOf(5,12),a.values)

        val b = PracticeVisualFactory.forQuestion(
            "g8_prism_volume",
            "Kenar uzunlukları 3, 4 ve 2 birim olan dikdörtgenler prizmasının hacmi kaç birim küptür?"
        )!!
        assertEquals(PracticeVisualKind.VOLUME_LAYERS,b.kind)
        assertEquals(listOf(3,4,2),b.values)

        val c = PracticeVisualFactory.forQuestion(
            "g6_signed","Sıcaklık −7 °C iken 13 °C artarsa kaç °C olur?"
        )!!
        assertEquals(PracticeVisualKind.SIGNED_WALK,c.kind)
        assertEquals(listOf(7,13),c.values)
    }

    @Test fun malformedOrUnknownInputNeverCreatesAFalseMathematicalDiagram() {
        assertNull(PracticeVisualFactory.forQuestion("g8_prism_volume",
            "3 kenarlı bir daire düşünüyoruz"))
        assertNull(PracticeVisualFactory.forQuestion("g8_power",
            "sayısal olmayan üslü ifade"))
        assertNull(PracticeVisualFactory.forQuestion("g5_area",
            "Kenarları ??? olan şekil"))
    }
}
