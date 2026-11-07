package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExpandedLessonBankTest {
    @Test fun everyGradeHasThreeActualLessonsMappedToPlannedTopics() {
        val all = mutableSetOf<String>()
        for (grade in 1..8) {
            val expanded = ExpandedLessonCatalog.forGrade(grade)
            assertEquals(3, expanded.size)
            for (lesson in expanded) {
                assertTrue(all.add(lesson.id))
                assertEquals(Skill.EXTENDED, lesson.skill)
                val topic = GradeCurriculumCatalog.topicForLesson(grade, lesson.id)
                assertTrue(topic != null)
                assertTrue(topic!!.area != TopicArea.REVIEW)
                assertTrue(topic.playable)
            }
        }
        assertEquals(24, all.size)
    }

    @Test fun questionBankNeverGeneratesInvalidOrAmbiguousChoices() {
        for (lesson in ExpandedLessonCatalog.all()) {
            for (seed in 1L..150L) {
                for (index in 0..3) {
                    val q = ExpandedLessonQuestionBank.question(lesson, seed, index)
                    assertTrue(q.answer > 0)
                    assertEquals(4, q.choices.size)
                    assertEquals(4, q.choices.distinct().size)
                    assertEquals(1, q.choices.count { it == q.answer })
                    assertTrue(q.text.isNotBlank() && q.explanation.isNotBlank())
                    assertEquals(q, ExpandedLessonQuestionBank.question(lesson, seed, index))
                }
            }
        }
    }

    @Test fun scriptedModelsStayMathematicallyCoherent() {
        val scripts = ExpandedLessonCatalog.all()
            .associate { it.id to ExpandedLessonTeachingCatalog.forLesson(it) }
        assertEquals(24, scripts.size)
        for (script in scripts.values) {
            assertEquals(2, script.check.choices.size)
            assertEquals(3, script.steps.size.coerceAtMost(3))
            assertTrue(script.check.correctIndex in 0..1)
            assertNotEquals(script.check.choices[0], script.check.choices[1])
            assertTrue(script.diagramCaption.isNotBlank())
        }
        assertEquals(TeachingVisual.TAKE_AWAY, scripts["g1_takeaway"]!!.visual)
        assertEquals(listOf(7, 3), scripts["g1_takeaway"]!!.groups)
        assertEquals(TeachingVisual.SHAPE, scripts["g1_shapes"]!!.visual)
        assertEquals(TeachingVisual.CLOCK, scripts["g2_clock"]!!.visual)
        assertEquals(TeachingVisual.FRACTION_BAR, scripts["g4_fraction"]!!.visual)
        assertEquals(listOf(3,4), scripts["g4_fraction"]!!.groups)
        assertEquals(TeachingVisual.PERIMETER, scripts["g4_perimeter"]!!.visual)
        assertEquals(TeachingVisual.DECIMAL, scripts["g5_decimal"]!!.visual)
        assertEquals(TeachingVisual.EQUATION, scripts["g8_equation"]!!.visual)
        assertEquals("−6 + 9 = 3", scripts["g6_signed"]!!.diagramCaption)
    }

    @Test fun oldPilotLessonsRemainTheFirstEntryForEachGrade() {
        assertEquals("count_2", LearningPathCatalog.forGrade("1. Sınıf").first().id)
        assertEquals("count_3", LearningPathCatalog.forGrade("2. Sınıf").first().id)
        assertEquals("multiply", LearningPathCatalog.forGrade("3. Sınıf").first().id)
        assertEquals("divisible_15", LearningPathCatalog.forGrade("8. Sınıf").first().id)
    }
}
