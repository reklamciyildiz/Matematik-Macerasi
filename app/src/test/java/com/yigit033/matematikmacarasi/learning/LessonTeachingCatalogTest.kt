package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LessonTeachingCatalogTest {
    @Test fun everyExistingPilotLessonHasSafeThreePartTeachingAndAConceptualCheck() {
        for (grade in 1..8) {
            for (lesson in LearningPathCatalog.forGrade("$grade. Sınıf")) {
                val script = LessonTeachingCatalog.forLesson(lesson)
                assertTrue(script.headline.isNotBlank())
                assertTrue(script.explanationFor(grade).isNotBlank())
                assertTrue(script.steps.size in 3..5)
                assertTrue(script.steps.all { it.isNotBlank() })
                assertTrue(script.check.choices.size == 2)
                assertTrue(script.check.choices.distinct().size == 2)
                assertTrue(script.check.correctIndex in 0..1)
                assertFalse(script.check.choices[script.check.correctIndex].isBlank())
                assertTrue(script.diagramCaption.isNotBlank())
            }
        }
    }

    @Test fun lowerGradesReceiveShorterVisualExplanationWithoutLosingMathematicalMeaning() {
        val addition = LessonTeachingCatalog.forSkill(Skill.ADD_10)
        assertTrue(addition.earlyExplanation.contains("elma"))
        assertTrue(addition.laterExplanation.contains("Toplama"))
        assertEquals(TeachingVisual.GROUPS, addition.visual)
        assertEquals(listOf(3, 2), addition.groups)
        assertEquals("3 + 2 = 5", addition.diagramCaption)
        assertEquals(5, addition.groups.sum())
    }

    @Test fun twentyAdditionCompletesTenWithoutInventingAGroup() {
        val addition = LessonTeachingCatalog.forSkill(Skill.ADD_20)
        assertEquals(listOf(8, 2, 3), addition.groups)
        assertEquals(13, addition.groups.sum())
        assertTrue(addition.steps[1].contains("10"))
        assertTrue(addition.steps.last().contains("13"))
    }

    @Test fun equalGroupsIllustrateThreeTimesFourRatherThanThreePlusFour() {
        for (skill in listOf(Skill.MULTIPLY, Skill.MULTIPLY_INTRO)) {
            val script = LessonTeachingCatalog.forSkill(skill)
            assertEquals(TeachingVisual.EQUAL_GROUPS, script.visual)
            assertEquals(listOf(4, 4, 4), script.groups)
            assertEquals(12, script.groups.sum())
            assertTrue(script.diagramCaption.contains("3 × 4 = 12"))
            assertTrue(script.check.choices[script.check.correctIndex].contains("grup"))
        }
    }

    @Test fun numberSequencesAlwaysIncreaseByTheStatedStep() {
        for ((skill, step) in listOf(
            Skill.COUNT_2 to 2, Skill.COUNT_3 to 3, Skill.COUNT_4 to 4,
            Skill.DIVISIBLE_15 to 15
        )) {
            val script = LessonTeachingCatalog.forSkill(skill)
            assertEquals(TeachingVisual.NUMBER_STEPS, script.visual)
            for (i in 1 until script.sequence.size) {
                assertEquals(step, script.sequence[i] - script.sequence[i - 1])
            }
            assertEquals(script.sequence.size,
                script.visibleNumbers(script.steps.lastIndex))
        }
    }

    @Test fun guidedExampleRevealsOneMathematicalGroupAtATime() {
        val script = LessonTeachingCatalog.forSkill(Skill.MULTIPLY)
        assertEquals(1, script.visibleGroups(0))
        assertEquals(2, script.visibleGroups(1))
        assertEquals(3, script.visibleGroups(2))
        assertEquals(3, script.visibleGroups(3))
    }
}
