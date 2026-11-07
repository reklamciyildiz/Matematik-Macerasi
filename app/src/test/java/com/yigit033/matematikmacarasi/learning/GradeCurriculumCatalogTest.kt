package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GradeCurriculumCatalogTest {
    @Test fun everyGradeHasHonestPlanningOutcomesAndStableUniqueIds() {
        val seen = mutableSetOf<String>()
        for (grade in 1..8) {
            val curriculum = GradeCurriculumCatalog.forGrade(grade)
            assertEquals(grade, curriculum.grade)
            assertTrue(curriculum.topics.size >= 6)
            assertTrue(curriculum.topics.all { it.grade == grade })
            assertTrue(curriculum.topics.all { it.outcomes.size >= 2 })
            assertTrue(curriculum.topics.all { it.id.startsWith("g" + grade + "_") })
            curriculum.topics.forEach { assertTrue(seen.add(it.id)) }
        }
    }

    @Test fun rolloutAndReadingBandsRespect2026To2027Programmes() {
        assertTrue(GradeCurriculumCatalog.programName(4).contains("Önceki"))
        assertTrue(GradeCurriculumCatalog.programName(8).contains("Önceki"))
        for (grade in listOf(1, 2, 3, 5, 6, 7)) {
            assertTrue(GradeCurriculumCatalog.programName(grade).contains("Maarif"))
        }
        assertEquals(InstructionBand.VISUAL_AUDIO, GradeCurriculumCatalog.band(1))
        assertEquals(InstructionBand.VISUAL_AUDIO, GradeCurriculumCatalog.band(2))
        assertEquals(InstructionBand.VISUAL_GUIDED, GradeCurriculumCatalog.band(3))
        assertEquals(InstructionBand.EXPLANATION_DIAGRAM, GradeCurriculumCatalog.band(8))
    }

    @Test fun onlyExistingPilotLessonsAreMarkedPlayable() {
        for (grade in 1..8) {
            val curriculum = GradeCurriculumCatalog.forGrade(grade)
            val actual = LearningPathCatalog.forGrade("$grade. Sınıf").map { it.id }.toSet()
            val displayed = curriculum.topics.flatMap { it.lessonIds }
            assertEquals(actual, displayed.toSet())
            assertEquals(displayed.size, displayed.distinct().size)
            assertTrue(curriculum.topics.any { !it.playable })
            for (lesson in actual) {
                assertNotNull(GradeCurriculumCatalog.topicForLesson(grade, lesson))
            }
            assertEquals(3, ExpandedLessonCatalog.forGrade(grade).size)
            if (grade >= 5) {
                assertTrue(curriculum.topics.any {
                    it.playable && it.area == TopicArea.REVIEW
                })
                assertTrue(curriculum.topics.any {
                    it.playable && it.area != TopicArea.REVIEW
                })
            }
        }
    }

    @Test fun selectingOtherGradesNeverSilentlyChangesMapping() {
        assertEquals(1, GradeCurriculumCatalog.gradeFromLabel("1. Sınıf"))
        assertEquals(8, GradeCurriculumCatalog.gradeFromLabel("8. Sınıf"))
        assertEquals(1, GradeCurriculumCatalog.gradeFromLabel("Ana Sınıfı"))
        assertEquals(1, GradeCurriculumCatalog.gradeFromLabel("garbled"))
    }
}
