package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.*
import org.junit.Test

class SpaceCombatPhysicsTest {
    @Test fun projectileMustCrossTargetToRegisterImpact() {
        val target=SpaceTarget(0,42,.5f,.65f)
        val y=SpaceCombatPhysics.position(target,0f,0).y
        val frame=SpaceCombatFrame(lasers=listOf(SpaceLaser(1,.5f,y+.015f)))
        val hit=SpaceCombatPhysics.advance(frame,.01f,listOf(target),0)
        assertEquals(42,hit.impact)
        assertTrue(hit.frame.lasers.isEmpty())
        val miss=SpaceCombatPhysics.advance(
            SpaceCombatFrame(lasers=listOf(SpaceLaser(1,.92f,.6f))),
            .01f,listOf(target),0)
        assertNull(miss.impact)
    }
    @Test fun freeSteeringAndShotsAreIndependentOfCorrectAnswer() {
        val run=SpaceExpeditionEngine.begin(117L,SpacePlanet.PYRA,LearningTier.TEMEL,1)
        assertEquals(.28f,SpaceExpeditionEngine.position(run,.28f).shipX,.0001f)
        assertEquals(.94f,SpaceExpeditionEngine.position(run,4f).shipX,.0001f)
        val targets=SpaceCombatPhysics.targets(run)
        assertEquals(3,targets.size)
        assertEquals(SpaceExpeditionEngine.puzzle(run).crystals,targets.map{it.value})
        var frame=SpaceCombatFrame()
        frame=SpaceCombatPhysics.fire(frame,.28f)
        assertEquals(1,frame.lasers.size)
        assertEquals(0,run.stage)
    }
    @Test fun wrongMeteorKeepsQuestionButCorrectMeteorSpawnsNext() {
        var run=SpaceExpeditionEngine.begin(911L,SpacePlanet.AQUA,LearningTier.GELISEN,2)
        val q=SpaceExpeditionEngine.puzzle(run)
        run=SpaceExpeditionEngine.fireAt(run,q.crystals.first{it!=q.answer})
        assertEquals(0,run.stage)
        assertEquals(1,run.wave)
        assertEquals(q,SpaceExpeditionEngine.puzzle(run))
        run=SpaceExpeditionEngine.fireAt(run,q.answer)
        assertEquals(1,run.stage)
        assertNotEquals(q,SpaceExpeditionEngine.puzzle(run))
        assertTrue(SpaceExpeditionEngine.valid(run))
    }
}
