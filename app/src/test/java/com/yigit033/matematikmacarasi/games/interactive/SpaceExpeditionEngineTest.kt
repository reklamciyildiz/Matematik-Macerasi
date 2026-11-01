package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.*
import org.junit.Test

class SpaceExpeditionEngineTest {
    @Test fun allPlanetsAndTiersHaveDeterministicSolvableMissions() {
        for(planet in SpacePlanet.entries)
            for(tier in LearningTier.entries)
                for(seed in 1L..60L) {
                    var run=SpaceExpeditionEngine.begin(seed,planet,tier)
                    assertTrue(SpaceExpeditionEngine.valid(run))
                    val flight=SpaceExpeditionEngine.puzzle(run)
                    assertEquals(flight,SpaceExpeditionEngine.puzzle(seed,planet,tier,0))
                    assertEquals(3,flight.crystals.size)
                    assertEquals(3,flight.crystals.distinct().size)
                    assertTrue(flight.crystals.contains(flight.answer))
                    val right=flight.crystals.indexOf(flight.answer)
                    run=SpaceExpeditionEngine.steer(run,right)
                    assertEquals(right,run.lane)
                    run=SpaceExpeditionEngine.intercept(run)
                    assertTrue(run.solved)
                    assertEquals(1,run.attempts)
                    assertTrue(SpaceExpeditionEngine.valid(run))
                    run=SpaceExpeditionEngine.next(run)
                    assertEquals(1,run.stage)
                    val reactor=SpaceExpeditionEngine.puzzle(run)
                    assertTrue(reactor.answer>0)
                    assertEquals(reactor,SpaceExpeditionEngine.puzzle(seed,planet,tier,1))
                    // User has to pump actual energy units, not choose an option.
                    while(run.charge+5<=reactor.answer)run=SpaceExpeditionEngine.adjustFuel(run,5)
                    while(run.charge<reactor.answer)run=SpaceExpeditionEngine.adjustFuel(run,1)
                    assertEquals(reactor.answer,run.charge)
                    run=SpaceExpeditionEngine.ignite(run)
                    assertTrue(run.solved)
                    assertTrue(SpaceExpeditionEngine.valid(run))
                    run=SpaceExpeditionEngine.next(run)
                    val circuit=SpaceExpeditionEngine.puzzle(run)
                    assertEquals(3,circuit.sequence.size)
                    assertEquals(4,circuit.nodes.size)
                    assertEquals(4,circuit.nodes.distinct().size)
                    assertEquals(circuit,SpaceExpeditionEngine.puzzle(seed,planet,tier,2))
                    circuit.sequence.forEachIndexed {index,value->
                        run=SpaceExpeditionEngine.connect(run,value)
                        assertEquals(index==2,run.solved)
                        assertTrue(SpaceExpeditionEngine.valid(run))
                    }
                    run=SpaceExpeditionEngine.next(run)
                    assertTrue(SpaceExpeditionEngine.valid(run))
                    assertTrue(run.completed)
                    assertEquals(3,run.firstTry)
                    assertEquals(3,SpaceExpeditionEngine.stars(run))
                    assertEquals(run,SpaceExpeditionEngine.next(run))
                }
    }
    @Test fun wrongFlightSelectionCostsShieldsAndRetryKeepsSameMath() {
        var run=SpaceExpeditionEngine.begin(
            456L,SpacePlanet.TERRA,LearningTier.GELISEN)
        val q=SpaceExpeditionEngine.puzzle(run)
        val wrong=q.crystals.indexOfFirst{it!=q.answer}
        run=SpaceExpeditionEngine.steer(run,wrong)
        run=SpaceExpeditionEngine.intercept(run)
        assertEquals(1,run.wave)
        assertEquals(1,run.errors)
        assertEquals(1,run.attempts)
        assertEquals(0,run.stage)
        assertEquals(2,run.shields)
        assertEquals(q,SpaceExpeditionEngine.puzzle(run))
        assertTrue(run.tainted)
        assertTrue(SpaceExpeditionEngine.valid(run))
        run=SpaceExpeditionEngine.steer(run,q.crystals.indexOf(q.answer))
        run=SpaceExpeditionEngine.intercept(run)
        assertTrue(run.solved)
        assertEquals(0,run.firstTry)
        assertEquals(3,run.attempts)
        assertEquals(1,run.errors) // 2 attempts: miss and success
        assertEquals(2,run.attempts)
        assertEquals(run,SpaceExpeditionEngine.intercept(run))
        assertTrue(SpaceExpeditionEngine.valid(run))
    }
    @Test fun reactorCannotBeSolvedBySkippingOrOvercharging() {
        val start=SpaceExpeditionEngine.begin(
            893L,SpacePlanet.PYRA,LearningTier.TEMEL)
        var run=SpaceExpeditionEngine.steer(
            start,SpaceExpeditionEngine.puzzle(start).crystals.indexOf(
                SpaceExpeditionEngine.puzzle(start).answer))
        run=SpaceExpeditionEngine.next(SpaceExpeditionEngine.intercept(run))
        assertEquals(1,run.stage)
        val target=SpaceExpeditionEngine.puzzle(run).answer
        val empty=SpaceExpeditionEngine.ignite(run)
        assertEquals(1,empty.errors)
        assertEquals(2,empty.attempts)
        run=empty
        run=SpaceExpeditionEngine.adjustFuel(run,10000)
        assertEquals(target+10,run.charge)
        run=SpaceExpeditionEngine.adjustFuel(run,-10000)
        assertEquals(0,run.charge)
        run=SpaceExpeditionEngine.adjustFuel(run,target)
        assertEquals(target,run.charge)
        val solved=SpaceExpeditionEngine.ignite(run)
        assertEquals(1,solved.stage)
        assertEquals(0,solved.charge)
        assertEquals(1,solved.errors)
        assertEquals(2,solved.attempts-1)
        assertTrue(SpaceExpeditionEngine.valid(solved))
    }
    @Test fun circuitRejectsWrongAndDuplicateNodesWithoutLosingCorrectProgress() {
        val seed=377L
        val planet=SpacePlanet.AQUA
        val tier=LearningTier.TEMEL
        val run0=SpaceExpeditionEngine.begin(seed,planet,tier)
        val correctLane=SpaceExpeditionEngine.puzzle(run0).crystals.indexOf(
            SpaceExpeditionEngine.puzzle(run0).answer)
        val stage1=SpaceExpeditionEngine.next(
            SpaceExpeditionEngine.intercept(SpaceExpeditionEngine.steer(run0,correctLane)))
        val charge=SpaceExpeditionEngine.puzzle(stage1).answer
        val stage2=SpaceExpeditionEngine.next(
            SpaceExpeditionEngine.ignite(SpaceExpeditionEngine.adjustFuel(stage1,charge)))
        val puzzle=SpaceExpeditionEngine.puzzle(stage2)
        assertTrue(puzzle.sequence.last()>0)
        var current=SpaceExpeditionEngine.connect(stage2,puzzle.sequence[0])
        assertEquals(listOf(puzzle.sequence[0]),current.path)
        assertEquals(current,SpaceExpeditionEngine.connect(current,puzzle.sequence[0]))
        val wrong=puzzle.nodes.first{it!=puzzle.sequence[1]&&it!=puzzle.sequence[0]}
        current=SpaceExpeditionEngine.connect(current,wrong)
        assertEquals(1,current.errors)
        assertEquals(listOf(puzzle.sequence[0]),current.path)
        assertTrue(SpaceExpeditionEngine.valid(current))
        current=SpaceExpeditionEngine.connect(current,puzzle.sequence[1])
        current=SpaceExpeditionEngine.connect(current,puzzle.sequence[2])
        assertTrue(current.solved)
        assertTrue(SpaceExpeditionEngine.valid(current))
    }
    @Test fun hintCannotBeFarmedAndInvalidStateCannotAdvance() {
        var run=SpaceExpeditionEngine.begin(
            119L,SpacePlanet.NOVA,LearningTier.USTALIK)
        assertFalse(SpaceExpeditionEngine.valid(run.copy(seed=0)))
        assertFalse(SpaceExpeditionEngine.valid(run.copy(lane=4)))
        assertFalse(SpaceExpeditionEngine.valid(run.copy(stage=4)))
        assertEquals(0,SpaceExpeditionEngine.stars(run))
        run=SpaceExpeditionEngine.hint(run)
        assertTrue(run.hintVisible)
        assertEquals(run,SpaceExpeditionEngine.hint(run))
        val right=SpaceExpeditionEngine.puzzle(run).crystals.indexOf(
            SpaceExpeditionEngine.puzzle(run).answer)
        run=SpaceExpeditionEngine.intercept(SpaceExpeditionEngine.steer(run,right))
        assertTrue(run.solved)
        assertEquals(0,run.firstTry)
        assertEquals(run,SpaceExpeditionEngine.hint(run))
        assertEquals(0,SpaceExpeditionEngine.stars(run))
    }
}
