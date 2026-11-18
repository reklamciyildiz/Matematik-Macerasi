package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class UpperGradeConceptPolicyTest {
    @Test fun everyRealSecondaryTopicHasAMappedPreview() {
        val expected = mapOf(
            5 to mapOf(
                "numbers" to AdvancedPreviewKind.NUMBER_LINE,
                "fractions" to AdvancedPreviewKind.FRACTION,
                "operations" to AdvancedPreviewKind.MULTIPLES,
                "geometry" to AdvancedPreviewKind.ANGLE,
                "measure" to AdvancedPreviewKind.AREA,
                "data" to AdvancedPreviewKind.DATA,
                "chance" to AdvancedPreviewKind.CHANCE,
                "review" to AdvancedPreviewKind.MULTIPLES
            ),
            6 to mapOf(
                "numbers" to AdvancedPreviewKind.NUMBER_LINE,
                "fractions" to AdvancedPreviewKind.FRACTION,
                "algebra" to AdvancedPreviewKind.LINEAR,
                "geometry" to AdvancedPreviewKind.ANGLE,
                "measure" to AdvancedPreviewKind.AREA,
                "data" to AdvancedPreviewKind.DATA,
                "review" to AdvancedPreviewKind.MULTIPLES
            ),
            7 to mapOf(
                "numbers" to AdvancedPreviewKind.NUMBER_LINE,
                "algebra" to AdvancedPreviewKind.LINEAR,
                "ratio" to AdvancedPreviewKind.RATIO,
                "transform" to AdvancedPreviewKind.REFLECTION,
                "measure" to AdvancedPreviewKind.AREA,
                "data" to AdvancedPreviewKind.DATA,
                "prob" to AdvancedPreviewKind.CHANCE,
                "review" to AdvancedPreviewKind.MULTIPLES
            ),
            8 to mapOf(
                "factors" to AdvancedPreviewKind.FACTORS,
                "powers" to AdvancedPreviewKind.SQUARES,
                "algebra" to AdvancedPreviewKind.LINEAR,
                "geometry" to AdvancedPreviewKind.REFLECTION,
                "solids" to AdvancedPreviewKind.VOLUME,
                "data" to AdvancedPreviewKind.DATA,
                "review" to AdvancedPreviewKind.MULTIPLES
            )
        )
        for ((grade, examples) in expected) {
            val actual = GradeCurriculumCatalog.forGrade(grade).topics
            assertEquals("grade "+grade+" inventory",examples.size,actual.size)
            for (topic in actual) {
                val suffix=topic.id.removePrefix("g"+grade+"_")
                assertEquals("wrong preview "+grade+"/"+suffix,
                    examples[suffix],UpperGradeConceptPolicy.previewFor(topic))
                assertNotNull(UpperGradeConceptPolicy.previewFor(topic))
            }
        }
    }

    @Test fun earlyGradesNeverReceiveAdvancedExamples() {
        for (grade in 1..4) {
            GradeCurriculumCatalog.forGrade(grade).topics.forEach {
                assertNull(UpperGradeConceptPolicy.previewFor(it))
            }
        }
    }

    @Test fun numberTypesAndBoundsAreGradeSpecific() {
        val type=AdvancedPreviewKind.NUMBER_LINE
        assertEquals(9,UpperGradeConceptPolicy.steps(type))
        for (s in -20..20) {
            val safe=UpperGradeConceptPolicy.safeStep(type,s)
            assertTrue(safe in 0..8)
            assertEquals(safe,UpperGradeConceptPolicy.lineNumerator(5,s))
            assertEquals(safe-4,UpperGradeConceptPolicy.lineNumerator(6,s))
            assertEquals(safe-4,UpperGradeConceptPolicy.lineNumerator(7,s))
        }
        assertEquals(1,UpperGradeConceptPolicy.lineDenominator(5))
        assertEquals(1,UpperGradeConceptPolicy.lineDenominator(6))
        assertEquals(2,UpperGradeConceptPolicy.lineDenominator(7))
    }

    @Test fun areaVolumeFractionRatioProbabilityAndPowersAreCorrect() {
        for (s in 0..3) {
            val (n,d)=UpperGradeConceptPolicy.fractionParts(s)
            assertEquals(s+1,n)
            assertEquals(4,d)
            val (a,b)=UpperGradeConceptPolicy.ratioAmounts(s)
            assertEquals(a*3,b*2)
            assertEquals((s+1)*(s+1),UpperGradeConceptPolicy.squareNumber(s))
            assertEquals(6*(s+1),UpperGradeConceptPolicy.volumeUnitCubes(s))
        }
        for (s in 0..4) {
            val (favorable,total)=UpperGradeConceptPolicy.probabilityRed(s)
            assertEquals(6,total)
            assertTrue(favorable in 1 until total)
        }
    }

    @Test fun anglesLinearRelationsReflectionsAndFactorsAreTrue() {
        assertEquals(listOf(30,60,90,120,150),
            (0..4).map { UpperGradeConceptPolicy.angleDegrees(it) })
        for (x in -50..50) {
            assertEquals(x,UpperGradeConceptPolicy.reflectionAcrossYAxis(
                UpperGradeConceptPolicy.reflectionAcrossYAxis(x)))
            assertEquals(2*x+3,UpperGradeConceptPolicy.linearY(x))
            assertEquals(2,UpperGradeConceptPolicy.linearY(x+1)-
                UpperGradeConceptPolicy.linearY(x))
        }
        for (s in 0..3) {
            val n=UpperGradeConceptPolicy.factorNumber(s)
            val factors=UpperGradeConceptPolicy.factorsOf(n)
            assertFalse(factors.isEmpty())
            assertEquals(1,factors.first())
            assertEquals(n,factors.last())
            assertTrue(factors.all { n%it==0 })
        }
    }
}
