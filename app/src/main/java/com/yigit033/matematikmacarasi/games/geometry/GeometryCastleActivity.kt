package com.yigit033.matematikmacarasi.games.geometry

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import com.yigit033.matematikmacarasi.services.payments.PremiumGameModePolicy
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.games.basic.GradeAwareOperationPolicy
import com.yigit033.matematikmacarasi.services.PlayerProfileService
import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveLearningStore
import com.yigit033.matematikmacarasi.learning.adaptive.AdventureSkill
import com.yigit033.matematikmacarasi.services.DailyTaskManager
import com.yigit033.matematikmacarasi.services.EventBus
import com.yigit033.matematikmacarasi.services.GameStatisticsHelper
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme

class GeometryCastleActivity : ComponentActivity() {
    companion object {
        private const val TAG = "GeometryCastle"
        private const val REWARD_ID = "geometry_castle_first_completion"
    }

    private lateinit var progress: GeometryCastleProgressStore
    private lateinit var stars: StarManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            progress = GeometryCastleProgressStore(this)
            stars = StarManager.getInstance(this)
            recoverFirstReward()
            if (progress.load().finished) deliverCompletion(progress.load())
        } catch (error: Exception) {
            Log.e(TAG,"Oyun başlatılamadı",error)
            finish()
            return
        }
        setContent {
            val purchase=remember { PurchaseManager.getInstance(this) }
            val premiumState by purchase.premiumState.collectAsState()
            val hasPremium=premiumState || purchase.isPremiumSync()
            val savedPrimary=progress.load()
            val preservePaid=savedPrimary.seed>0L &&
                PremiumGameModePolicy.requiresPremium("geometry",savedPrimary.tier.ordinal) &&
                !hasPremium
            val basicStore=remember {
                GeometryCastleProgressStore(this,
                    PremiumGameModePolicy.freeCheckpointStore("geometry"))
            }
            val currentStore=if(preservePaid)basicStore else progress
            MatematikMacerasiTheme {
                Surface(modifier=Modifier.fillMaxSize()) {
                    GeometryCastleScreen(
                        store=currentStore,
                        profileGrade=GradeAwareOperationPolicy.grade(
                            PlayerProfileService.getInstance(this).getUserClass()),
                        suggestedTier=if(!hasPremium) LearningTier.TEMEL
                            else AdaptiveLearningStore(this).progress(
                                AdventureSkill.GEOMETRY).currentTier,
                        onExit={finish()},
                        onCompleted={run ->
                    com.yigit033.matematikmacarasi.services.AudioService
                        .getInstance(this).playVictorySound()
                    deliverCompletion(run,currentStore)
                }
                    )
                }
            }
        }
    }

    private fun deliverCompletion(
        run: CastleRun,completedStore:GeometryCastleProgressStore=progress
    ) {
        if (!run.finished) return
        recoverFirstReward(completedStore)
        if(run.errors==0 && run.hints==0) completedStore.clearWeakShape()
        if(completedStore.markDailyDelivered(run.seed)) {
            runCatching {
                DailyTaskManager.getInstance(this).recordCompletedGameTask(
                    GameType.SHAPE_RECOGNITION,intent.getIntExtra("daily_task_id",-1))
            }.onFailure { Log.w(TAG,"Günlük görev güncellenemedi",it) }
            runCatching {
                GameStatisticsHelper(this).recordGameCompletion(
                    gameType=GameType.SHAPE_RECOGNITION,
                    level=run.tier.ordinal+1,
                    score=if(run.attempts==0) 0 else
                        ((run.attempts-run.errors)*100/run.attempts),
                    stars=if(run.completedRuns==1 && completedStore.rewardRecorded()) 1 else 0,
                    gameStartTime=run.seed,
                    taskId=intent.getIntExtra("daily_task_id",-1)
                )
            }.onFailure { Log.w(TAG,"İstatistik kaydı alınamadı",it) }
        }
        runCatching {
            AdaptiveLearningStore(this).recordCompletion(
                AdventureSkill.GEOMETRY,run.seed,run.tier,
                run.attempts,run.errors,run.hints
            )
        }.onFailure { Log.w(TAG,"Adaptif öğrenme kaydı alınamadı",it) }
    }

    private fun recoverFirstReward(completedStore:GeometryCastleProgressStore=progress) {
        if (!completedStore.hasEverCompleted() || completedStore.rewardRecorded()) return
        if(stars.addStars(REWARD_ID,1)) {
            if(completedStore.markRewardRecorded())
                EventBus.updateStars(stars.getDisplayStars())
        } else Log.w(TAG,"İlk bitirme yıldızı cihazdaki sonraki girişte tekrar denenecek")
    }
}
