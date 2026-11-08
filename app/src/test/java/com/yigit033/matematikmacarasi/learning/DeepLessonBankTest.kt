package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class DeepLessonBankTest {
    @Test fun everyGradeHasAuthoredAndMappedDeepUnits() {
        val ids = mutableSetOf<String>()
        for (grade in 1..8) {
            val deep = DeepLessonCatalog.forGrade(grade)
            assertEquals(if (grade == 8) 4 else 2, deep.size)
            for (lesson in deep) {
                assertTrue(ids.add(lesson.id))
                assertEquals(Skill.DEEP_DIVE, lesson.skill)
                val parent = GradeCurriculumCatalog.topicForLesson(grade,lesson.id)
                assertTrue(parent != null && parent.area != TopicArea.REVIEW)
                val script = LessonTeachingCatalog.forLesson(lesson)
                assertTrue(script.steps.size in 3..5)
                assertTrue(script.diagramCaption.isNotBlank())
                assertEquals(2,script.check.choices.size)
                assertNotEquals(script.check.choices[0],script.check.choices[1])
            }
        }
        assertEquals(18,ids.size)
    }

    @Test fun generatedDeepProblemsHaveOneAnswerAndReplayDeterministically() {
        for (unit in DeepLessonCatalog.all()) {
            for (seed in 1L..160L) for (i in 0..7) {
                val q=DeepLessonQuestionBank.question(unit.lesson,seed,i)
                assertEquals(4,q.choices.size)
                assertEquals(4,q.choices.distinct().size)
                assertEquals(1,q.choices.count{it==q.answer})
                assertEquals(q,DeepLessonQuestionBank.question(unit.lesson,seed,i))
                assertTrue(q.text.isNotBlank() && q.explanation.isNotBlank())
            }
        }
    }

    @Test fun gradeEightNewSkillsHaveIndependentlyVerifiedMath() {
        val lessons=DeepLessonCatalog.forGrade(8).associateBy{it.id}
        for (seed in 1L..200L) for (i in 0..7) {
            for (id in lessons.keys) {
                val q=DeepLessonQuestionBank.question(lessons.getValue(id),seed,i)
                val expected = when(id) {
                    "g8_lcm" -> {
                        val m=Regex("""(\d+) ve (\d+) sayılarının EKOK""").find(q.text)!!
                        val a=m.groupValues[1].toInt()
                        val b=m.groupValues[2].toInt()
                        val factorsA=(1..a).filter{a%it==0}
                        val factorsB=(1..b).filter{b%it==0}
                        a*b/factorsA.intersect(factorsB.toSet()).maxOrNull()!!
                    }
                    "g8_square_root" -> {
                        val n=Regex("""√(\d+)""").find(q.text)!!.groupValues[1].toInt()
                        val answer=sqrt(n.toDouble()).toInt()
                        assertEquals(n,answer*answer)
                        answer
                    }
                    "g8_linear_value" -> {
                        val m=Regex("""y = (\d+)x \+ (\d+) ve x = (\d+)""").find(q.text)!!
                        val n=m.groupValues.drop(1).map{it.toInt()}
                        n[0]*n[2]+n[1]
                    }
                    "g8_coordinate_mirror" -> {
                        val m=Regex("""A\((\d+), (\d+)\) noktası y eksenine""")
                            .find(q.text)!!
                        -m.groupValues[1].toInt()
                    }
                    else -> error("Math oracle missing for $id")
                }
                assertEquals(id + ": " + q.text,expected,q.answer)
            }
        }
    }
}
