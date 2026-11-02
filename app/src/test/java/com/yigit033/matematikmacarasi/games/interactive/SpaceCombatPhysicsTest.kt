package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.*
import org.junit.Test

class SpaceCombatPhysicsTest {
    @Test fun lasersMustPhysicallyHitMovingTargets() {
        val target=SpaceTarget(0,42,.5f,.65f)
        val current=SpaceCombatPhysics.position(target,0f,0)
        val shot=SpaceCombatFrame(lasers=listOf(SpaceLaser(1,.5f,current.y+.015f)))
        val hit=SpaceCombatPhysics.advance(shot,.01f,listOf(target),0)
        assertEquals(42,hit.impact)
        assertTrue(hit.frame.lasers.isEmpty())
        val miss=SpaceCombatPhysics.advance(
            SpaceCombatFrame(lasers=listOf(SpaceLaser(1,.93f,.60f))),
            .01f,listOf(target),0)
        assertNull(miss.impact)
    }
    @Test fun freeSteeringAndWrongMeteorCauseRealDamage() {
        val run=SpaceExpeditionEngine.begin(113L,SpacePlanet.NOVA,LearningTier.GELISEN)
        assertEquals(.32f,SpaceExpeditionEngine.position(run,.32f).shipX,.0001f)
        assertEquals(.06f,SpaceExpeditionEngine.position(run,-20f).shipX,.0001f)
        assertEquals(run,SpaceExpeditionEngine.position(run,Float.NaN))
        val p=SpaceExpeditionEngine.puzzle(run)
        assertEquals(run,SpaceExpeditionEngine.fireAt(run,Int.MAX_VALUE))
        val wrong=SpaceExpeditionEngine.fireAt(run,p.crystals.first{it!=p.answer})
        assertEquals(1,wrong.errors)
        assertEquals(2,wrong.shields)
        assertEquals(1,wrong.wave)
        val correct=SpaceExpeditionEngine.fireAt(wrong,p.answer)
        assertTrue(correct.solved)
        assertTrue(SpaceExpeditionEngine.valid(correct))
    }
    @Test fun allWorldsCanBeCompletedWithLaserHitsOnly() {
        for(world in SpacePlanet.entries)
            for(tier in LearningTier.entries)
                for(seed in 1L..45L){
                    var run=SpaceExpeditionEngine.begin(seed,world,tier)
                    val p0=SpaceExpeditionEngine.puzzle(run)
                    assertEquals(3,SpaceCombatPhysics.targets(run).size)
                    run=SpaceExpeditionEngine.fireAt(run,p0.answer)
                    assertTrue(run.solved)
                    run=SpaceExpeditionEngine.next(run)
                    val target=SpaceExpeditionEngine.puzzle(run).answer
                    while(run.charge+5<=target)run=SpaceExpeditionEngine.fireAt(run,5)
                    while(run.charge<target)run=SpaceExpeditionEngine.fireAt(run,1)
                    run=SpaceExpeditionEngine.fireAt(run,Int.MIN_VALUE)
                    assertTrue(run.solved)
                    run=SpaceExpeditionEngine.next(run)
                    val p2=SpaceExpeditionEngine.puzzle(run)
                    for(node in p2.sequence){
                        run=SpaceExpeditionEngine.fireAt(run,node)
                        assertTrue(SpaceExpeditionEngine.valid(run))
                    }
                    assertTrue(run.solved)
                    run=SpaceExpeditionEngine.next(run)
                    assertTrue(run.completed)
                    assertEquals(3,SpaceExpeditionEngine.stars(run))
                    assertTrue(SpaceExpeditionEngine.valid(run))
                }
    }
    @Test fun reactorCannotBeIgnitedUntilFuelCorrectAndWrongStarIsPenalized() {
        var run=SpaceExpeditionEngine.begin(118L,SpacePlanet.TERRA,LearningTier.TEMEL)
        run=SpaceExpeditionEngine.next(
            SpaceExpeditionEngine.fireAt(run,SpaceExpeditionEngine.puzzle(run).answer))
        val missed=SpaceExpeditionEngine.fireAt(run,Int.MIN_VALUE)
        assertEquals(1,missed.errors)
        assertEquals(2,missed.shields)
        val fuel=SpaceExpeditionEngine.puzzle(run).answer
        run=SpaceExpeditionEngine.next(
            SpaceExpeditionEngine.fireAt(
                SpaceExpeditionEngine.adjustFuel(missed,fuel),Int.MIN_VALUE))
        val q=SpaceExpeditionEngine.puzzle(run)
        val wrong=q.nodes.first{it!=q.sequence.first()}
        val after=SpaceExpeditionEngine.fireAt(run,wrong)
        assertEquals(run.errors+1,after.errors)
        assertEquals(run.path,after.path)
        assertTrue(SpaceExpeditionEngine.valid(after))
    }
}
