package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.*
import org.junit.Test

class SpaceExpeditionEngineTest {
    @Test fun everyGradeTierWorldCompletesFifteenShotsWithoutIntermediateButtons() {
        for(grade in 1..4)for(world in SpacePlanet.entries)
            for(tier in LearningTier.entries)
                for(seed in 1L..35L) {
                    var run=SpaceExpeditionEngine.begin(seed,world,tier,grade)
                    assertTrue(SpaceExpeditionEngine.valid(run))
                    repeat(SpaceExpeditionEngine.STAGES) {index->
                        val question=SpaceExpeditionEngine.puzzle(run)
                        assertEquals(question,
                            SpaceExpeditionEngine.puzzle(seed,world,tier,index,grade))
                        assertEquals(3,question.crystals.size)
                        assertEquals(3,question.crystals.distinct().size)
                        assertTrue(question.crystals.contains(question.answer))
                        assertTrue(question.explanation.isNotBlank())
                        assertTrue(question.hint.isNotBlank())
                        run=SpaceExpeditionEngine.fireAt(run,question.answer)
                        assertEquals(index+1,run.stage)
                        assertEquals(index+1,run.firstTry)
                        assertTrue(SpaceExpeditionEngine.valid(run))
                    }
                    assertTrue(run.completed)
                    assertEquals(15,run.attempts)
                    assertEquals(15,run.combo)
                    assertEquals(15,run.bestCombo)
                    assertEquals(3,SpaceExpeditionEngine.stars(run))
                    assertEquals(run,SpaceExpeditionEngine.fireAt(run,1000))
                }
    }
    @Test fun gradeIsHardOperationCeilingEvenAtUstalik() {
        for(grade in 1..4)for(tier in LearningTier.entries)
            for(world in SpacePlanet.entries)
                for(seed in 1L..35L)for(stage in 0 until SpaceExpeditionEngine.STAGES){
                    val question=SpaceExpeditionEngine.puzzle(seed,world,tier,stage,grade)
                    if(grade<=2) {
                        assertFalse(question.expression.contains("×"))
                        assertFalse(question.expression.contains("÷"))
                    }
                    if(grade==1)assertTrue(question.answer<=20)
                    if(grade==2)assertTrue(question.answer<=100)
                    assertTrue(question.answer>=0)
                }
    }
    @Test fun eachShotPenalizesOnceAndWrongAnswerCannotSkipQuestion() {
        var run=SpaceExpeditionEngine.begin(92L,SpacePlanet.LUNA,LearningTier.TEMEL,1)
        val question=SpaceExpeditionEngine.puzzle(run)
        val wrong=question.crystals.first{it!=question.answer}
        assertEquals(run,SpaceExpeditionEngine.fireAt(run,Int.MAX_VALUE))
        run=SpaceExpeditionEngine.fireAt(run,wrong)
        assertEquals(0,run.stage)
        assertEquals(1,run.errors)
        assertEquals(1,run.attempts)
        assertEquals(0,run.combo)
        assertEquals(2,run.shields)
        assertTrue(run.tainted)
        val correct=SpaceExpeditionEngine.fireAt(run,question.answer)
        assertEquals(1,correct.stage)
        assertEquals(0,correct.firstTry)
        assertEquals(2,correct.attempts)
        assertEquals(1,correct.combo)
        assertFalse(correct.tainted)
        assertTrue(SpaceExpeditionEngine.valid(correct))
    }
    @Test fun hintIsOncePerQuestionAndResetsWhenNewMeteorAppears() {
        var run=SpaceExpeditionEngine.begin(26L,SpacePlanet.NOVA,LearningTier.USTALIK,2)
        run=SpaceExpeditionEngine.hint(run)
        assertTrue(run.hintVisible)
        assertEquals(run,SpaceExpeditionEngine.hint(run))
        assertEquals(1,run.hints)
        run=SpaceExpeditionEngine.fireAt(run,SpaceExpeditionEngine.puzzle(run).answer)
        assertEquals(1,run.stage)
        assertEquals(0,run.firstTry)
        assertFalse(run.hintVisible)
        run=SpaceExpeditionEngine.hint(run)
        assertEquals(2,run.hints)
        assertTrue(SpaceExpeditionEngine.valid(run))
    }
    @Test fun invalidCheckpointsNeverGenerateRewards() {
        val run=SpaceExpeditionEngine.begin(38L,SpacePlanet.TERRA,LearningTier.TEMEL,1)
        assertEquals(0,SpaceExpeditionEngine.stars(run))
        assertFalse(SpaceExpeditionEngine.valid(run.copy(seed=0)))
        assertFalse(SpaceExpeditionEngine.valid(run.copy(grade=8)))
        assertFalse(SpaceExpeditionEngine.valid(run.copy(stage=16)))
        assertFalse(SpaceExpeditionEngine.valid(run.copy(completed=true)))
        assertFalse(SpaceExpeditionEngine.valid(run.copy(attempts=3)))
        assertFalse(SpaceExpeditionEngine.valid(run.copy(shipX=Float.NaN)))
        assertEquals(run,SpaceExpeditionEngine.position(run,Float.NaN))
        val steered=SpaceExpeditionEngine.position(run,.06f)
        assertEquals(.06f,steered.shipX,.0001f)
        assertTrue(SpaceExpeditionEngine.valid(steered))
    }
}
