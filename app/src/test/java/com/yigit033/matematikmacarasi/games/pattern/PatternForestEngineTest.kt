package com.yigit033.matematikmacarasi.games.pattern

import org.junit.Assert.*
import org.junit.Test

class PatternForestEngineTest {
    @Test fun sixMechanicsAndFiveWorldsAlwaysHaveValidSolutions() {
        for(grade in 1..8)for(difficulty in 0..2)for(seed in 1L..85L){
            val tasks=(0 until PatternForestEngine.STOPS).map { index->
                PatternForestEngine.task(seed,grade,difficulty,index)
            }
            assertEquals(30,tasks.size)
            tasks.forEachIndexed { index,t->
                assertEquals(PatternForestEngine.world(index),t.world)
                assertEquals(PatternForestEngine.mechanic(index),t.mechanic)
                assertEquals(grade,t.grade)
                assertEquals(difficulty,t.difficulty)
                assertTrue(t.shown.size>=6)
                assertTrue(t.instruction.isNotBlank())
                assertTrue(t.explanation.isNotBlank())
                assertEquals(t,PatternForestEngine.task(seed,grade,difficulty,index))
                when(t.mechanic) {
                    PatternMechanic.COMPLETE,PatternMechanic.DRAG->{
                        assertEquals(1,t.answer.size)
                        assertTrue(t.answer.single() in t.options)
                        assertEquals(t.options.distinct().size,t.options.size)
                        assertTrue(t.options.size>=3)
                        val correct=t.options.indexOf(t.answer.single())
                        assertTrue(PatternForestEngine.correct(t,correct,emptyList()))
                        assertFalse(PatternForestEngine.correct(t,-1,emptyList()))
                        assertTrue(PatternForestEngine.validSelection(t,correct,emptyList()))
                        assertFalse(PatternForestEngine.validSelection(t,t.options.size,emptyList()))
                    }
                    PatternMechanic.ORDER,PatternMechanic.CREATE->{
                        assertEquals(3,t.answer.size)
                        // Robust construction independent of duplicated repeated colors.
                        val uniquePicks=mutableSetOf<Int>()
                        val actual=t.answer.map {tile ->
                            val idx=t.options.indices.first{j->
                                t.options[j]==tile &&
                                    (t.mechanic==PatternMechanic.CREATE || j !in uniquePicks)
                            }
                            if(t.mechanic==PatternMechanic.ORDER)uniquePicks.add(idx)
                            idx
                        }
                        assertTrue(PatternForestEngine.correct(t,-1,actual))
                        assertTrue(PatternForestEngine.validSelection(t,-1,actual))
                        assertFalse(PatternForestEngine.correct(t,-1,emptyList()))
                        if(t.mechanic==PatternMechanic.ORDER)
                            assertFalse(PatternForestEngine.validSelection(t,-1,listOf(0,0)))
                        else assertTrue(PatternForestEngine.validSelection(t,-1,listOf(0,0)))
                    }
                    PatternMechanic.RULE->{
                        assertTrue(t.ruleChoices.size>=3)
                        assertTrue(t.correctRuleIndex in t.ruleChoices.indices)
                        assertEquals(t.ruleChoices.distinct().size,t.ruleChoices.size)
                        assertTrue(PatternForestEngine.correct(t,t.correctRuleIndex,emptyList()))
                        assertFalse(PatternForestEngine.correct(t,-1,emptyList()))
                    }
                    PatternMechanic.SPOT->{
                        assertTrue(t.corruptIndex in 2 until t.shown.lastIndex)
                        assertTrue(PatternForestEngine.correct(t,t.corruptIndex,emptyList()))
                        assertFalse(PatternForestEngine.correct(t,-1,emptyList()))
                    }
                }
                if(grade==1) {
                    t.shown.filter{it.kind==PatternKind.NUMBER}.forEach {
                        assertTrue("Grade 1 numeric terms must be positive",it.value in 0..100)
                    }
                    t.answer.filter{it.kind==PatternKind.NUMBER}.forEach {
                        assertTrue(it.value in 0..100)
                    }
                }
                if(grade<=3)t.shown.filter{it.kind==PatternKind.NUMBER}.forEach {
                    assertTrue(it.value>=0)
                }
            }
        }
    }

    @Test fun newSeedsCreateDifferentButReplayableChallenges() {
        val a=(0 until 30).map{PatternForestEngine.task(1222L,5,2,it)}
        val b=(0 until 30).map{PatternForestEngine.task(1666L,5,2,it)}
        assertTrue(a.zip(b).count{it.first!=it.second}>=20)
    }

    @Test fun difficultyNeverReplacesFirstGradeWithAdvancedAlgebra() {
        for(d in 0..2){
            for(i in 0 until 30){
                val t=PatternForestEngine.task(999L,1,d,i)
                assertTrue(t.shown.filter{it.kind==PatternKind.NUMBER}.all{it.value<=100})
                assertFalse(t.ruleChoices.any{it.contains("n²")})
            }
        }
    }
}
