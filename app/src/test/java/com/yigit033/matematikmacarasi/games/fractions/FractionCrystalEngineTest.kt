package com.yigit033.matematikmacarasi.games.fractions

import org.junit.Assert.*
import org.junit.Test

class FractionCrystalEngineTest {
    @Test fun exactRationalOperationsAndNoFloatingPointComparison() {
        assertTrue(CrystalFraction(2,4).equivalent(CrystalFraction(1,2)))
        assertTrue(CrystalFraction(3,8) < CrystalFraction(2,5))
        assertTrue(CrystalFraction(7,8) > CrystalFraction(3,4))
        assertEquals(CrystalFraction(3,4),CrystalFraction(1,4)+CrystalFraction(1,2))
        assertEquals(CrystalFraction(2,3), CrystalFraction(4,6).canonical)
    }

    @Test fun thousandsOfRunsAreDeterministicPlayableAndAgeBounded() {
        for (grade in 1..8) for(difficulty in 0..2) for(seed in 1L..100L) {
            val tasks=FractionCrystalEngine.generate(seed,grade,difficulty)
            assertEquals(12,tasks.size)
            assertEquals(tasks,FractionCrystalEngine.generate(seed,grade,difficulty))
            tasks.forEachIndexed { index,task ->
                assertEquals(CrystalMode.entries[index%4],task.mode)
                assertTrue(task.target.denominator in
                    FractionCrystalEngine.denominators(grade,difficulty))
                assertTrue(task.target.numerator in 1 until task.target.denominator)
                assertTrue(task.label.isNotBlank())
                assertTrue(task.explanation.isNotBlank())
                val selected=(0 until task.target.numerator).toSet()
                if(task.mode==CrystalMode.FILL || task.mode==CrystalMode.BRIDGE) {
                    assertTrue(FractionCrystalEngine.correct(task,task.target.numerator,null))
                    assertFalse(FractionCrystalEngine.correct(task,0,null))
                } else {
                    assertTrue("Correct answer should appear among options at $grade/$seed",
                        task.answer in task.choices)
                    assertEquals(task.choices.distinct().size,task.choices.size)
                    assertTrue(FractionCrystalEngine.correct(task,0,task.answer))
                    assertFalse(FractionCrystalEngine.correct(task,0,"NOT_A_CHOICE"))
                }
                if(task.mode==CrystalMode.COMPARE){
                    val expected=when {
                        task.target>task.rival->"SOL"
                        task.target<task.rival->"SAĞ"
                        else->"EŞİT"
                    }
                    assertEquals(expected,task.answer)
                }
                assertTrue(FractionCrystalEngine.validSelection(task,
                    if(task.mode==CrystalMode.FILL) selected else emptySet(),
                    if(task.mode==CrystalMode.BRIDGE) task.target.numerator else 0))
            }
        }
    }

    @Test fun lowGradesAreOnlyEnrichmentAndDoNotUseHigherGradeDecimals() {
        for (grade in 1..4) for(seed in 1L..50L) {
            val portals=FractionCrystalEngine.generate(seed,grade,1)
                .filter { it.mode == CrystalMode.PORTAL }
            assertTrue(portals.all { it.skill=="fraction_equivalence" })
            assertTrue(portals.none { "%" in it.answer })
        }
        assertTrue(FractionCrystalEngine.skillCodes(5).contains("fraction_representation"))
    }

    @Test fun differentSeedsChangeAtLeastMostPuzzleTargets() {
        val a=FractionCrystalEngine.generate(887L,5,2).map{it.target}
        val b=FractionCrystalEngine.generate(888L,5,2).map{it.target}
        assertTrue(a.zip(b).count{it.first!=it.second}>=6)
    }

    @Test fun selectionBoundariesAndEqualFractions() {
        val task=FractionCrystalEngine.generate(72L,3,0).first()
        assertTrue(FractionCrystalEngine.validSelection(task,setOf(0,1),0))
        assertFalse(FractionCrystalEngine.validSelection(task,setOf(task.target.denominator),0))
        assertFalse(FractionCrystalEngine.correct(task,task.target.numerator-1,null))
    }
}
