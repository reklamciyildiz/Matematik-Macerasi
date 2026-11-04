package com.yigit033.matematikmacarasi.games.sayiavi2

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.*
import org.junit.Test

class NumberHuntRichContentTest {
    private val engine=NumberHuntRichContent

    @Test fun allGradesAndTiersGenerate24SolvableTasks(){
        for(grade in 1..8)
            for(tier in LearningTier.entries)
                for(seed in 1L..35L){
                    val tasks=engine.generate(grade,tier,seed)
                    assertEquals(24,tasks.size)
                    assertEquals(24,tasks.map{it.prompt}.distinct().size)
                    tasks.chunked(6).forEach{chapter->
                        assertEquals(6,chapter.map{it.mechanic}.distinct().size)
                    }
                    tasks.forEach{task->
                        when(task){
                            is NumberHuntGradeEngine.Scan->{
                                assertEquals(12,task.options.size)
                                assertEquals(4,task.targets.size)
                                assertTrue(task.options.containsAll(task.targets))
                                assertEquals(12,task.options.distinct().size)
                            }
                            is NumberHuntGradeEngine.Bridge->{
                                assertEquals(4,task.tokens.map{it.value}.distinct().size)
                            }
                            is NumberHuntGradeEngine.Dial->{
                                assertTrue(task.target in task.lower..task.upper)
                                assertTrue(task.initial in task.lower..task.upper)
                            }
                            is NumberHuntGradeEngine.Pairs->{
                                assertEquals(3,task.pairs.map{it.left}.distinct().size)
                                assertEquals(3,task.pairs.map{it.right}.distinct().size)
                            }
                            is NumberHuntGradeEngine.Cipher->{
                                assertEquals(4,task.choices.distinct().size)
                                assertTrue(task.answer in task.choices)
                            }
                            is NumberHuntGradeEngine.Trail->{
                                assertEquals(4,task.checkpoints.size)
                                task.checkpoints.forEach{step->
                                    assertEquals(4,step.choices.distinct().size)
                                    assertTrue(step.answer in step.choices)
                                }
                            }
                        }
                    }
                }
    }

    @Test fun restartOfSameSeedIsBitwiseStable(){
        for(grade in 1..8){
            val one=engine.generate(grade,LearningTier.GELISEN,1987231L)
            val twice=engine.generate(grade,LearningTier.GELISEN,1987231L)
            assertEquals(one,twice)
        }
    }

    @Test fun newSeedChangesMostPuzzlesRatherThanJustTileOrder(){
        for(grade in 1..8){
            val a=engine.generate(grade,LearningTier.GELISEN,11737L)
            val b=engine.generate(grade,LearningTier.GELISEN,14229L)
            val different=a.zip(b).count{(x,y)->x!=y}
            assertTrue("Grade $grade changed only $different / 24",different>=15)
        }
    }

    @Test fun laterGradesContainReasoningTopics(){
        val seven=engine.generate(7,LearningTier.USTALIK,12050L).joinToString(" "){it.prompt}
        val eight=engine.generate(8,LearningTier.USTALIK,12050L).joinToString(" "){it.prompt}
        assertTrue(seven.contains("Negatif"))
        assertTrue(seven.contains("Mutlak") || seven.contains("işareti"))
        assertTrue(eight.contains("Karekökü"))
        assertTrue(eight.contains("10³"))
    }

    @Test fun legacySixteenQuestionGenerationRemainsUntouched(){
        val old=NumberHuntGradeEngine.generate(8,LearningTier.GELISEN,1234567L)
        assertEquals(16,old.size)
        assertEquals(24,engine.generate(8,LearningTier.GELISEN,1234567L).size)
        assertTrue(old.none{it is NumberHuntGradeEngine.Cipher ||
            it is NumberHuntGradeEngine.Trail})
    }
}
