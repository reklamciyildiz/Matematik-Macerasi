package com.yigit033.matematikmacarasi.games.geometry

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.hypot

class GeometryCastleExpansionTest {
    @Test fun newCatalogueRespectsAgeBandsAndOffersTenShapes() {
        assertEquals(4,GeometryCastleEngine.available(LearningTier.TEMEL).size)
        assertEquals(6,GeometryCastleEngine.available(LearningTier.GELISEN).size)
        assertEquals(10,GeometryCastleEngine.available(LearningTier.USTALIK).size)
        assertTrue(CastleShape.RHOMBUS in GeometryCastleEngine.available(LearningTier.USTALIK))
        assertFalse(CastleShape.TRAPEZOID in GeometryCastleEngine.available(LearningTier.GELISEN))
        assertFalse(CastleShape.OCTAGON in GeometryCastleEngine.available(LearningTier.TEMEL))
    }

    @Test fun everyNewQuestionHasFourDistinctOptionsAndCorrectGradedBridgeSockets() {
        for(tier in LearningTier.entries) for(seed in 1L..300L) {
            val tasks = GeometryCastleEngine.tasks(seed,tier,contentVersion=2)
            assertEquals(12,tasks.size)
            assertEquals(tasks,GeometryCastleEngine.tasks(seed,tier,contentVersion=2))
            val builds = tasks.filter { it.mode==CastleMode.BUILD }
            assertEquals(4,builds.size)
            val expectedPieces = when(tier) {
                LearningTier.TEMEL -> 1
                LearningTier.GELISEN -> 2
                LearningTier.USTALIK -> 3
            }
            tasks.filter { it.mode!=CastleMode.SYMMETRY }.forEach { task ->
                assertEquals(4,task.options.size)
                assertEquals(4,task.options.distinct().size)
                assertTrue(task.options.all { it in GeometryCastleEngine.available(tier) })
                assertTrue(task.prompt.isNotBlank())
                assertTrue(task.explanation.isNotBlank())
            }
            builds.forEach { task ->
                assertEquals(expectedPieces,task.requiredPieces.size)
                assertEquals(task.requiredPieces.size,task.requiredPieces.distinct().size)
                assertTrue(task.requiredPieces.all { it in task.options })
                task.requiredPieces.forEachIndexed { index, shape ->
                    assertTrue(GeometryCastleEngine.isCorrectPiece(task,index,shape))
                    assertTrue(task.options.filter { it!=shape }.all {
                        !GeometryCastleEngine.isCorrectPiece(task,index,it)
                    })
                }
                assertFalse(GeometryCastleEngine.isCorrectPiece(task,-1,task.target))
                assertFalse(GeometryCastleEngine.isCorrectPiece(task,4,task.target))
            }
            // Mastery showcases eight distinct target silhouettes each run.
            if(tier==LearningTier.USTALIK) {
                val shapeTasks=tasks.filter { it.mode!=CastleMode.SYMMETRY }
                assertEquals(8,shapeTasks.map { it.target }.distinct().size)
            }
        }
    }

    @Test fun alreadyStartedGamesRetainOriginalSixShapeGenerator() {
        val legacyNames = setOf(CastleShape.CIRCLE,CastleShape.TRIANGLE,
            CastleShape.SQUARE,CastleShape.RECTANGLE,CastleShape.PENTAGON,CastleShape.HEXAGON)
        for(tier in LearningTier.entries) for(seed in 1L..100L) {
            val oldRun=CastleRun(seed=seed,tier=tier,contentVersion=1)
            assertTrue(GeometryCastleEngine.valid(oldRun))
            val tasks=GeometryCastleEngine.tasks(seed,tier,contentVersion=1)
            assertEquals(12,tasks.size)
            assertEquals(tasks.first(),GeometryCastleEngine.task(oldRun))
            assertTrue(tasks.all { it.target in legacyNames })
            assertTrue(tasks.filter { it.mode==CastleMode.BUILD }
                .all { it.requiredPieces.size==1 })
            assertTrue(tasks.filter { it.mode!=CastleMode.SYMMETRY }
                .all { it.options.all { choice -> choice in legacyNames } })
        }
    }

    @Test fun bridgeCannotBeFinishedByFabricatingOrSkippingPieces() {
        val run=CastleRun(seed=3821L,tier=LearningTier.USTALIK,
            contentVersion=2,room=1)
        val task=GeometryCastleEngine.task(run)!!
        assertEquals(3,task.requiredPieces.size)
        assertFalse(GeometryCastleEngine.valid(run.copy(solved=true)))
        assertFalse(GeometryCastleEngine.valid(run.copy(
            placed=listOf(task.options.first { it!=task.requiredPieces.first() }))))
        assertEquals(run,GeometryCastleEngine.advance(run))
        var current=run
        for((i,shape) in task.requiredPieces.withIndex()) {
            val checked=GeometryCastleEngine.answer(current,true)
            current=checked.copy(placed=current.placed+shape,solved=i==2)
            assertTrue(GeometryCastleEngine.valid(current))
            if(i<2) assertEquals(current,GeometryCastleEngine.advance(current))
        }
        val progressed=GeometryCastleEngine.advance(current)
        assertEquals(1,progressed.room)
        assertEquals(1,progressed.step)
        assertTrue(progressed.placed.isEmpty())
        assertEquals(1,progressed.firstTryCorrect)
        assertEquals(3,progressed.attempts)
    }

    @Test fun difficultShapeRetryOccursOnlyInAppropriateTier() {
        for(shape in CastleShape.entries) {
            val mastery=GeometryCastleEngine.tasks(73L,LearningTier.USTALIK,
                weakShape=shape,contentVersion=2)
            assertEquals(shape,mastery.first().target)
            val beginning=GeometryCastleEngine.tasks(73L,LearningTier.TEMEL,
                weakShape=shape,contentVersion=2)
            assertTrue(beginning.first().target in GeometryCastleEngine.available(
                LearningTier.TEMEL))
        }
    }

    @Test fun polygonGeometryHasCorrectCountsParallelPairsAndAngles() {
        for(shape in CastleShape.entries) {
            val vertices=GeometryShapeGeometry.vertices(shape)
            assertEquals(shape.sides,vertices.size)
            assertTrue(vertices.all { it.first in 0f..1f && it.second in 0f..1f })
        }
        fun edges(shape: CastleShape): List<Pair<Double,Double>> {
            val p=GeometryShapeGeometry.vertices(shape)
            return p.indices.map { i ->
                Pair((p[(i+1)%p.size].first-p[i].first).toDouble(),
                    (p[(i+1)%p.size].second-p[i].second).toDouble())
            }
        }
        fun parallelPairs(shape: CastleShape): Int {
            val es=edges(shape)
            return listOf(0 to 2,1 to 3).count { (a,b) ->
                abs(es[a].first*es[b].second-es[a].second*es[b].first)<1e-5
            }
        }
        fun lengths(shape: CastleShape)=edges(shape).map { (x,y) -> hypot(x,y) }
        assertEquals(2,parallelPairs(CastleShape.SQUARE))
        assertEquals(2,parallelPairs(CastleShape.RECTANGLE))
        assertEquals(2,parallelPairs(CastleShape.RHOMBUS))
        assertEquals(2,parallelPairs(CastleShape.PARALLELOGRAM))
        assertEquals(1,parallelPairs(CastleShape.TRAPEZOID))
        val square=lengths(CastleShape.SQUARE)
        assertTrue(square.maxOrNull()!!-square.minOrNull()!! < 1e-5)
        val rhombus=lengths(CastleShape.RHOMBUS)
        assertTrue(rhombus.maxOrNull()!!-rhombus.minOrNull()!! < 1e-5)
        val slanted=edges(CastleShape.RHOMBUS)
        val dot=slanted[0].first*slanted[1].first+slanted[0].second*slanted[1].second
        assertTrue(abs(dot)>.01)
        val parallelogram=lengths(CastleShape.PARALLELOGRAM)
        assertTrue(parallelogram.maxOrNull()!!-parallelogram.minOrNull()!! > .10)
    }
}
