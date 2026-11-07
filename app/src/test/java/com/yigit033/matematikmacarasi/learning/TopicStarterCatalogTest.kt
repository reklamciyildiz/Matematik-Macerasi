package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Guard the release contract: every topic title opens a real, original
 * first lesson, but a topic title is NOT falsely presented as full mastery.
 */
class TopicStarterCatalogTest {
    @Test fun everyFormerlyEmptyHeadingHasOneOriginalStarter() {
        val expected = mapOf(
            1 to 2, 2 to 2, 3 to 2, 4 to 2,
            5 to 4, 6 to 3, 7 to 4, 8 to 3
        )
        val ids=mutableSetOf<String>()
        for (grade in 1..8) {
            val entries=TopicStarterCatalog.all().filter { it.grade == grade }
            assertEquals("Missing starter in grade $grade",expected[grade],entries.size)
            entries.forEach { entry ->
                assertTrue(ids.add(entry.lesson.id))
                assertTrue(entry.lesson.id.startsWith("g" + grade + "_"))
                assertEquals(Skill.TOPIC_STARTER, entry.lesson.skill)
                val topics=GradeCurriculumCatalog.forGrade(grade).topics
                val matching=topics.filter { entry.lesson.id in it.lessonIds }
                assertEquals(1,matching.size)
                assertEquals("g" + grade + "_" + entry.topicKey,matching.single().id)
                assertTrue(entry.steps.size in 3..5)
                val script=entry.teachingScript()
                assertEquals(2,script.check.choices.size)
                assertTrue(script.check.choices.distinct().size==2)
                assertTrue(script.diagramCaption.isNotBlank())
                assertEquals(script,LessonTeachingCatalog.forLesson(entry.lesson))
            }
        }
        assertEquals(22,ids.size)
    }

    @Test fun allGradesHaveRealFirstLessonAtEveryCurriculumTopic() {
        for(grade in 1..8){
            val curriculum=GradeCurriculumCatalog.forGrade(grade)
            assertTrue(curriculum.topics.all { it.lessonIds.isNotEmpty() })
            val lessons=LearningPathCatalog.forGrade("$grade. Sınıf")
            assertEquals(lessons.size,lessons.map { it.id }.distinct().size)
            assertEquals(lessons.map { it.id }.toSet(),
                curriculum.topics.flatMap { it.lessonIds }.toSet())
            for(topic in curriculum.topics) {
                topic.lessonIds.forEach { id ->
                    assertTrue("Could not open $id",lessons.any { it.id==id })
                }
            }
        }
    }

    @Test fun generatedQuestionsRemainDeterministicUniqueAndAnswerable() {
        for(entry in TopicStarterCatalog.all()) {
            for(seed in 1L..120L) for(index in 0..3) {
                val q=TopicStarterQuestionBank.question(entry.lesson,seed,index)
                assertEquals(4,q.choices.size)
                assertEquals(4,q.choices.distinct().size)
                assertEquals(1,q.choices.count { it==q.answer })
                assertTrue(q.answer>=0)
                assertTrue(q.text.isNotBlank()&&q.explanation.isNotBlank())
                assertEquals(q,TopicStarterQuestionBank.question(entry.lesson,seed,index))
            }
        }
    }

    @Test fun visualModelsMatchTheLearningConcept() {
        val s=TopicStarterCatalog.all().associate { it.lesson.id to it }
        assertEquals(TeachingVisual.SYMMETRY,s["g7_mirror"]?.visual)
        assertEquals(TeachingVisual.RIGHT_TRIANGLE,s["g8_right_triangle"]?.visual)
        assertEquals(listOf(3,4,5),s["g8_right_triangle"]?.model)
        assertEquals(TeachingVisual.VOLUME_STACK,s["g8_prism_volume"]?.visual)
        assertEquals(listOf(3,2,2),s["g8_prism_volume"]?.model)
        assertEquals(TeachingVisual.FRACTION_BAR,s["g7_probability"]?.visual)
        assertFalse(s.values.any { it.lesson.teaching.isBlank() })
    }
}
