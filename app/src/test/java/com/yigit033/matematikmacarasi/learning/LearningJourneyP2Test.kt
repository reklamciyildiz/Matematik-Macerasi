package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure Kotlin catalog and mathematical correctness checks; no Android device. */
class LearningJourneyP2Test {
    @Test fun sixNewLessonsAreReallyAuthoredAndMappedToOfficialPartialOutcomes() {
        val units = LearningJourneyLessonCatalog.all()
        assertEquals(6, units.size)
        assertEquals(setOf(2, 3, 4), units.map { it.grade }.toSet())
        assertEquals(6, units.map { it.lesson.id }.toSet().size)
        assertEquals(
            setOf("MAT.2.1.4", "MAT.2.2.3", "MAT.3.2.6", "MAT.3.4.1",
                "M.4.1.1.4", "M.4.1.6.2"),
            units.map { it.officialCode }.toSet()
        )
        units.forEach { unit ->
            assertTrue(unit.source.startsWith("https://"))
            assertTrue(unit.scopeNote.isNotBlank())
            assertEquals(Skill.JOURNEY, unit.lesson.skill)
            val script = LessonTeachingCatalog.forLesson(unit.lesson)
            assertTrue(script.steps.size >= 3)
            assertTrue(script.steps.all { it.isNotBlank() })
            assertTrue(script.diagramCaption.isNotBlank())
            assertTrue(script.check.explanation.isNotBlank())
        }
    }

    @Test fun allGradesRetainValidCurriculumAndConnectedRoutes() {
        for (grade in 1..8) {
            val lessons = LearningPathCatalog.forGrade("$grade. Sınıf")
            val ids = lessons.map { it.id }
            assertEquals(ids.size, ids.toSet().size)
            val curriculum = GradeCurriculumCatalog.forGrade(grade)
            assertEquals(ids.toSet(), curriculum.topics.flatMap { it.lessonIds }.toSet())
            val routes = LearningJourneyCatalog.forGrade(grade)
            assertTrue("Grade $grade needs genuine routes", routes.isNotEmpty())
            routes.forEach { route ->
                assertEquals(grade, route.grade)
                assertTrue(route.lessonIds.all { it in ids })
                assertEquals(route.lessonIds.size, route.lessonIds.toSet().size)
                assertEquals(0, route.completedCount { false })
                assertFalse(route.finished { false })
                assertEquals(route.lessonIds.first(), route.nextLesson { false })
                assertTrue(route.finished { true })
                assertEquals(route.lessonIds.last(), route.nextLesson { true })
            }
        }
    }

    @Test fun twoToFourHaveThreeAuthoredJourneyRoutes() {
        for (grade in 2..4) {
            assertEquals(3, LearningJourneyCatalog.forGrade(grade).size)
            assertEquals(2, LearningJourneyLessonCatalog.forGrade(grade).size)
            LearningJourneyLessonCatalog.forGrade(grade).forEach { lesson ->
                assertTrue(LearningJourneyCatalog.forGrade(grade).any {
                    lesson.id in it.lessonIds
                })
                assertEquals(8, LearningPathCatalog.practiceQuestionCount(lesson))
            }
        }
    }

    @Test fun newEightQuestionFamiliesAreDeterministicAndMathematicallyCorrect() {
        for (unit in LearningJourneyLessonCatalog.all()) {
            for (seed in 0L..120L) {
                for (index in 0..7) {
                    val q = LearningPathCatalog.question(unit.lesson, seed, index)
                    assertEquals(q, LearningPathCatalog.question(unit.lesson, seed, index))
                    assertEquals(4, q.choices.size)
                    assertEquals(4, q.choices.distinct().size)
                    assertTrue(q.answer in q.choices)
                    assertTrue(q.text.isNotBlank() && q.explanation.isNotBlank())
                    val numbers = Regex("""\d+""").findAll(q.text)
                        .map { it.value.toInt() }.toList()
                    when (unit.lesson.id) {
                        "g2_backward_count" -> {
                            // First three printed terms. The fourth is an unknown.
                            assertTrue(numbers[0] <= 100)
                            val gap = numbers[0] - numbers[1]
                            assertEquals(gap, numbers[1] - numbers[2])
                            assertEquals(numbers[2] - gap, q.answer)
                            assertTrue(gap in 2..5)
                        }
                        "g2_inverse_ops" -> {
                            assertTrue(numbers[0] <= 100)
                            assertEquals(numbers[0] - numbers[1], q.answer)
                        }
                        "g3_story_two_step" ->
                            assertEquals(numbers[0] * numbers[1] + numbers[2], q.answer)
                        "g3_data_difference" ->
                            assertEquals(numbers[0] - numbers[1], q.answer)
                        "g4_round_tens" -> {
                            assertTrue(numbers[0] in 1000..9999)
                            assertEquals((numbers[0] + 5) / 10 * 10, q.answer)
                        }
                        "g4_unit_fraction_compare" -> {
                            assertTrue(numbers[1] in 2..20 && numbers[3] in 2..20)
                            assertEquals(minOf(numbers[1], numbers[3]), q.answer)
                        }
                    }
                }
            }
        }
    }

    @Test fun theP1AdaptiveEngineCanReuseAllNewQuestionsWithoutChangingSkill() {
        for (unit in LearningJourneyLessonCatalog.all()) {
            val lesson = unit.lesson
            for (band in PracticeBand.values()) {
                val q = AdaptiveLearningPolicy.question(lesson, 153L, 0, band)
                assertTrue(q.answer in q.choices)
                val support = AdaptiveLearningPolicy.supportQuestion(lesson, 153L, 0, q)
                assertTrue(support.answer in support.choices)
                assertTrue(support.text.isNotBlank())
            }
        }
    }

    @Test fun existingGrandfatheredPremiumPolicyAndIntroLessonsRemainUnmodified() {
        for (grade in 1..8) {
            val free = LearningAccessPolicy.freeLessonIds(grade)
            assertEquals(2, free.size)
            for (newLesson in LearningJourneyLessonCatalog.forGrade(grade)) {
                // A new Premium lesson may only open through the normal policy.
                assertEquals(
                    newLesson.id in free,
                    LearningAccessPolicy.accessible(grade, newLesson.id, false)
                )
                assertTrue(LearningAccessPolicy.accessible(grade, newLesson.id, true))
            }
        }
    }
}
