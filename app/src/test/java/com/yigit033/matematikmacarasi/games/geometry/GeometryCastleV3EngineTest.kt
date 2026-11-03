package com.yigit033.matematikmacarasi.games.geometry

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.*
import org.junit.Test

/** Proves new 20-stage content and all legacy saves are structurally compatible. */
class GeometryCastleV3EngineTest {
    @Test fun twentyCorrectCheckpointsForEveryGradeTierAndSeed() {
        for (grade in 1..8) for(tier in LearningTier.entries)
            for(seed in 1L..35L) {
                var run=CastleRun(seed=seed,tier=tier,grade=grade,contentVersion=3)
                val expected=GeometryCastleEngine.tasks(
                    seed,tier,contentVersion=3,grade=grade)
                assertEquals(20,expected.size)
                assertEquals(expected,GeometryCastleEngine.tasks(
                    seed,tier,contentVersion=3,grade=grade))
                assertEquals(20,GeometryCastleEngine.totalTasks(run))
                assertEquals(5,GeometryCastleEngine.stepsPerRoom(run))
                for(index in 0 until 20){
                    val q=requireNotNull(GeometryCastleEngine.task(run))
                    assertEquals("Wrong question index $index",expected[index],q)
                    assertTrue(GeometryCastleEngine.valid(run))
                    assertEquals("Unsolved task must not advance",
                        run,GeometryCastleEngine.advance(run))
                    run=when(q.mode){
                        CastleMode.FIND -> {
                            assertTrue(q.target in GeometryCastleEngine.available(tier,grade))
                            assertTrue(q.target in q.options)
                            GeometryCastleEngine.answer(run,true)
                                .copy(selected=q.target,solved=true)
                        }
                        CastleMode.BUILD -> {
                            var r=run
                            q.requiredPieces.forEachIndexed{piece,shape ->
                                r=r.copy(selected=shape)
                                assertTrue("Selected piece must be valid",GeometryCastleEngine.valid(r))
                                r=GeometryCastleEngine.answer(r,true).copy(
                                    selected=null,placed=r.placed+shape,
                                    solved=piece==q.requiredPieces.lastIndex)
                                assertTrue("Placed piece must be valid $grade/$tier/$seed/$index/$piece",
                                    GeometryCastleEngine.valid(r))
                            }
                            r
                        }
                        CastleMode.SYMMETRY -> {
                            val mask=GeometryCastleEngine.symmetricRightMask(
                                q.leftMask,q.gridSize)
                            val r=run.copy(mirrorMask=mask)
                            assertTrue(GeometryCastleEngine.valid(r))
                            GeometryCastleEngine.answer(r,true).copy(solved=true)
                        }
                        CastleMode.MEASURE -> {
                            assertTrue(q.measureAnswer in 1..q.measureMax)
                            assertEquals(0,q.measureAnswer%q.measureStep)
                            val r=run.copy(measureValue=q.measureAnswer)
                            assertTrue(GeometryCastleEngine.valid(r))
                            assertTrue(GeometryCastleEngine.isCorrect(
                                q,null,0,q.measureAnswer))
                            assertFalse(GeometryCastleEngine.isCorrect(q,null,0,0))
                            assertFalse(GeometryCastleEngine.valid(
                                r.copy(measureValue=q.measureMax+1)))
                            GeometryCastleEngine.answer(r,true).copy(solved=true)
                        }
                    }
                    assertTrue("Answer invalid $grade/$tier/$seed/$index",
                        GeometryCastleEngine.valid(run))
                    val advanced=GeometryCastleEngine.advance(run)
                    assertTrue("Advanced invalid $grade/$tier/$seed/$index",
                        GeometryCastleEngine.valid(advanced))
                    assertEquals(index+1,GeometryCastleEngine.taskIndex(advanced))
                    run=advanced
                }
                assertTrue(run.finished)
                assertEquals(20,run.firstTryCorrect)
                assertEquals(
                    expected.sumOf { q ->
                        if(q.mode==CastleMode.BUILD)q.requiredPieces.size else 1
                    },
                    run.attempts
                )
                assertEquals(1,run.completedRuns)
                assertEquals(0,run.measureValue)
            }
    }
    @Test fun singleSocketLegacyBridgeAllowsCorrectStoneWithoutNewPlacementField() {
        for(tier in LearningTier.entries)for(seed in 1L..60L){
            val before=CastleRun(seed=seed,tier=tier,contentVersion=1,room=1,step=1)
            val task=requireNotNull(GeometryCastleEngine.task(before))
            assertEquals(CastleMode.BUILD,task.mode)
            val picked=before.copy(selected=task.target)
            assertTrue(GeometryCastleEngine.valid(picked))
            val solved=GeometryCastleEngine.answer(picked,true)
                .copy(selected=task.target,placed=emptyList(),solved=true)
            assertTrue("Legacy single-slot correct state must be valid",
                GeometryCastleEngine.valid(solved))
            assertFalse("Previously crashed on new placed field in legacy state",
                GeometryCastleEngine.valid(solved.copy(placed=listOf(task.target))))
            assertTrue(GeometryCastleEngine.valid(GeometryCastleEngine.advance(solved)))
        }
    }
    @Test fun gradeCeilingsApplyDespiteHardestTier() {
        for(grade in 1..8)for(tier in LearningTier.entries){
            val tasks=GeometryCastleEngine.tasks(
                20261103L,tier,contentVersion=3,grade=grade)
            assertEquals(20,tasks.size)
            val shapes=GeometryCastleEngine.available(tier,grade)
            tasks.filter{it.mode==CastleMode.FIND ||
                it.mode==CastleMode.BUILD}.forEach{
                assertTrue(it.target in shapes)
                assertTrue(it.requiredPieces.all{piece->piece in shapes})
            }
            tasks.filter{it.mode==CastleMode.MEASURE}.forEach {
                if(grade<=2)assertEquals("edges",it.measureKind)
                else if(grade==3)assertEquals("vertices",it.measureKind)
                else if(grade==4)assertEquals("perimeter",it.measureKind)
                else if(grade==5)assertTrue(it.measureKind in listOf("area","perimeter"))
                else assertTrue(it.measureKind in
                    listOf("angle","triangle_area","perimeter"))
            }
        }
    }
}
