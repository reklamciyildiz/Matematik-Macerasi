package com.yigit033.matematikmacarasi.games.sayiavi2

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.*
import org.junit.Test

class NumberHuntGradeEngineTest {
    @Test fun allGradesGenerateCompleteAndValidWorlds(){
        for(grade in 1..8)for(tier in LearningTier.entries)for(seed in 1L..50L){
            val tasks=NumberHuntGradeEngine.generate(grade,tier,seed)
            assertEquals(16,tasks.size)
            assertEquals(grade in NumberHuntGradeEngine.world(grade).grades,true)
            assertEquals(4,tasks.filterIsInstance<NumberHuntGradeEngine.Scan>().size)
            assertEquals(4,tasks.filterIsInstance<NumberHuntGradeEngine.Bridge>().size)
            assertEquals(4,tasks.filterIsInstance<NumberHuntGradeEngine.Dial>().size)
            assertEquals(4,tasks.filterIsInstance<NumberHuntGradeEngine.Pairs>().size)
            tasks.filterIsInstance<NumberHuntGradeEngine.Scan>().forEach{
                assertEquals(4,it.targets.size)
                assertTrue(it.options.containsAll(it.targets))
                assertEquals(12,it.options.distinct().size)
            }
            tasks.filterIsInstance<NumberHuntGradeEngine.Bridge>().forEach{
                assertEquals(4,it.tokens.map{token->token.value}.distinct().size)
            }
            tasks.filterIsInstance<NumberHuntGradeEngine.Pairs>().forEach{
                assertEquals(3,it.pairs.map{pair->pair.right}.distinct().size)
            }
        }
    }
    @Test fun runRebuildIsDeterministicAndReplayChangesContent(){
        val first=NumberHuntGradeEngine.generate(6,LearningTier.GELISEN,12445)
        val same=NumberHuntGradeEngine.generate(6,LearningTier.GELISEN,12445)
        val different=NumberHuntGradeEngine.generate(6,LearningTier.GELISEN,12446)
        assertEquals(first,same)
        assertNotEquals(first,different)
    }
    @Test fun upperGradesActuallyUseDifferentMathTopics(){
        val grade2=NumberHuntGradeEngine.generate(2,LearningTier.GELISEN,777)
        val grade6=NumberHuntGradeEngine.generate(6,LearningTier.GELISEN,777)
        val grade8=NumberHuntGradeEngine.generate(8,LearningTier.GELISEN,777)
        assertTrue((grade2[0] as NumberHuntGradeEngine.Scan).prompt.contains("artan"))
        assertTrue((grade6[0] as NumberHuntGradeEngine.Scan).prompt.contains("Asal"))
        assertTrue((grade8[0] as NumberHuntGradeEngine.Scan).prompt.contains("Karekök"))
    }
}
