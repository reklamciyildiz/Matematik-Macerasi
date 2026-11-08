package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeepLessonPracticeVisualTest {
    @Test fun eighthGradeDeepLessonsHaveStrictlyGroundedQuestionIllustrations() {
        for (lesson in DeepLessonCatalog.forGrade(8)) {
            for (seed in 1L..100L) for (index in 0..3) {
                val q = DeepLessonQuestionBank.question(lesson,seed,index)
                val model = PracticeVisualFactory.forQuestion(lesson.id,q.text)
                assertNotNull(lesson.id+": "+q.text,model)
                assertTrue(model!!.values.isNotEmpty())
                assertTrue(model.accessibilityText().isNotBlank())
                assertEquals(model,PracticeVisualFactory.forQuestion(lesson.id,q.text))
            }
        }
    }

    @Test fun gradeEightNewDiagramsNeverInventAnUnrelatedProblem() {
        val lcm=PracticeVisualFactory.forQuestion("g8_lcm",
            "18 ve 27 sayılarının EKOK'u kaçtır?")
        assertEquals(PracticeVisualKind.MULTIPLES_LISTS,lcm?.kind)
        assertEquals(listOf(18,27),lcm?.values)

        val root=PracticeVisualFactory.forQuestion("g8_square_root","√144 kaçtır?")
        assertEquals(PracticeVisualKind.AREA_GRID,root?.kind)
        assertEquals(listOf(12,12),root?.values)
        assertNull(PracticeVisualFactory.forQuestion("g8_square_root","√143 kaçtır?"))

        val equation=PracticeVisualFactory.forQuestion("g8_linear_value",
            "y = 4x + 3 ve x = 6 ise y kaçtır?")
        assertEquals(PracticeVisualKind.LINEAR_INPUT,equation?.kind)
        assertEquals(listOf(4,3,6),equation?.values)

        val mirror=PracticeVisualFactory.forQuestion("g8_coordinate_mirror",
            "A(5, 7) noktası y eksenine yansıtılırsa yeni x koordinatı kaç olur?")
        assertEquals(PracticeVisualKind.MIRROR_POINTS,mirror?.kind)
        assertEquals(listOf(5,7),mirror?.values)
    }
}
