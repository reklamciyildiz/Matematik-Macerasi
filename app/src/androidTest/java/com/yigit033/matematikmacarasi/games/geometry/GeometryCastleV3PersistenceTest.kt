package com.yigit033.matematikmacarasi.games.geometry

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GeometryCastleV3PersistenceTest {
    @Test fun legacyBridgeSnapshotLikeExistingUserCanCompleteAndContinue() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        val name="geometry_legacy_bridge_v1_recovery"
        val prefs=context.getSharedPreferences(name,Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        val tier=LearningTier.GELISEN
        val seed=(1L..200L).first{candidate ->
            val run=CastleRun(seed=candidate,tier=tier,contentVersion=1,room=1,step=1)
            GeometryCastleEngine.task(run)?.target==CastleShape.CIRCLE
        }
        val saved=prefs.edit().putLong("seed",seed)
            .putString("tier",tier.name).putInt("room",1).putInt("step",1)
            .putInt("content_version",1).commit()
        assertTrue(saved)
        val store=GeometryCastleProgressStore(context,name)
        try {
            var run=store.load()
            assertEquals(1,run.room)
            assertEquals(1,run.step)
            assertEquals(1,run.contentVersion)
            val q=requireNotNull(GeometryCastleEngine.task(run))
            assertEquals(CastleShape.CIRCLE,q.target)
            run=run.copy(selected=q.target)
            assertTrue("Old selected shape failed",store.save(run))
            run=GeometryCastleEngine.answer(run,true).copy(
                selected=q.target,solved=true,placed=emptyList()
            )
            assertTrue("Old correct bridge answer rejected: "+
                store.lastSaveIssue,store.save(run))
            assertEquals(run,store.load())
            run=GeometryCastleEngine.advance(run)
            assertTrue("Old progression cannot advance: "+
                store.lastSaveIssue,store.save(run))
            assertEquals(1,run.room)
            assertEquals(2,run.step)
            assertEquals(run,store.load())
        }finally{prefs.edit().clear().commit()}
    }

    @Test fun twentyRealDeviceCheckpointsIncludingMeasurementAreDurable() {
        val context=InstrumentationRegistry.getInstrumentation().targetContext
        for(grade in listOf(2,4,6,8))for(tier in LearningTier.entries){
            val name="geometry_v3_device_"+grade+"_"+tier.name
            val prefs=context.getSharedPreferences(name,Context.MODE_PRIVATE)
            prefs.edit().clear().commit()
            val store=GeometryCastleProgressStore(context,name)
            try{
                var run=requireNotNull(store.begin(tier,grade))
                assertEquals(3,run.contentVersion)
                assertEquals(grade,run.grade)
                repeat(20){idx->
                    val q=requireNotNull(GeometryCastleEngine.task(run))
                    when(q.mode){
                        CastleMode.FIND->{
                            run=GeometryCastleEngine.answer(run,true)
                                .copy(selected=q.target,solved=true)
                            assertTrue("FIND $idx: "+store.lastSaveIssue,store.save(run))
                        }
                        CastleMode.BUILD->{
                            q.requiredPieces.forEachIndexed{slot,shape->
                                run=run.copy(selected=shape)
                                assertTrue("PICK $idx/$slot: "+store.lastSaveIssue,
                                    store.save(run))
                                run=GeometryCastleEngine.answer(run,true)
                                    .copy(selected=null,placed=run.placed+shape,
                                        solved=slot==q.requiredPieces.lastIndex)
                                assertTrue("PLACE $idx/$slot: "+store.lastSaveIssue,
                                    store.save(run))
                            }
                        }
                        CastleMode.SYMMETRY->{
                            run=run.copy(mirrorMask=GeometryCastleEngine.symmetricRightMask(
                                q.leftMask,q.gridSize))
                            assertTrue("MIRROR $idx",store.save(run))
                            run=GeometryCastleEngine.answer(run,true).copy(solved=true)
                            assertTrue("MIRROR SOLVE $idx",store.save(run))
                        }
                        CastleMode.MEASURE->{
                            run=run.copy(measureValue=q.measureAnswer)
                            assertTrue("MEASURE $idx",store.save(run))
                            run=GeometryCastleEngine.answer(run,true).copy(solved=true)
                            assertTrue("MEASURE SOLVE $idx",store.save(run))
                        }
                    }
                    assertTrue(run.solved)
                    assertEquals(run,store.load())
                    val before=run
                    run=GeometryCastleEngine.advance(run)
                    assertTrue("ADVANCE $idx: "+store.lastSaveIssue,store.save(run))
                    assertEquals(run,store.load())
                    assertFalse("Duplicate task completion must not re-count",
                        store.save(before))
                }
                assertTrue(run.finished)
                assertEquals(20,run.firstTryCorrect)
                assertEquals(1,run.completedRuns)
                assertEquals(grade,run.grade)
            }finally{prefs.edit().clear().commit()}
        }
    }
}
