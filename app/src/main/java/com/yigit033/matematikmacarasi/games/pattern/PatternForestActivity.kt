package com.yigit033.matematikmacarasi.games.pattern

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.games.basic.GradeAwareOperationPolicy
import com.yigit033.matematikmacarasi.services.AudioService
import com.yigit033.matematikmacarasi.services.EventBus
import com.yigit033.matematikmacarasi.services.GameStatisticsHelper
import com.yigit033.matematikmacarasi.services.PlayerProfileService
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme

/** Offline, child-safe adventure; no network, ads, account or payment dependencies. */
class PatternForestActivity:ComponentActivity() {
    companion object {
        private const val TAG="PatternForest"
        private const val FIRST_REWARD="pattern_forest_first_complete_v1"
    }
    private lateinit var progress:PatternForestStore
    private lateinit var stars:StarManager

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        progress=PatternForestStore(this)
        stars=StarManager.getInstance(this)
        val saved=progress.load()
        if(saved.finished)deliverCompletion(saved)
        val grade=GradeAwareOperationPolicy.grade(
            PlayerProfileService.getInstance(this).getUserClass())
        setContent {
            MatematikMacerasiTheme {
                Surface(Modifier.fillMaxSize()) {
                    PatternForestScreen(
                        store=progress,profileGrade=grade,
                        onExit={finish()},
                        onCompleted={run->deliverCompletion(run)}
                    )
                }
            }
        }
    }
    private fun deliverCompletion(run:PatternRun) {
        if(!run.finished)return
        if(!progress.rewardMarked()){
            runCatching {
                if(stars.addStars(FIRST_REWARD,3) && progress.markReward()){
                    EventBus.updateStars(stars.getDisplayStars())
                }
            }.onFailure{Log.w(TAG,"First reward pending",it)}
        }
        if(progress.deliveredSeed()!=run.seed){
            runCatching {
                GameStatisticsHelper(this).recordGameCompletion(
                    gameType=GameType.PATTERN_FOREST,
                    level=run.difficulty+1,
                    score=run.clean*100/PatternForestEngine.STOPS,
                    stars=if(run.completedRuns==1 && progress.rewardMarked())3 else 0,
                    gameStartTime=run.seed,
                    taskId=intent.getIntExtra("daily_task_id",-1)
                )
                if(!progress.markDelivered(run.seed))
                    Log.w(TAG,"Completion delivered but seed marker not persisted")
            }.onFailure{Log.w(TAG,"Statistics or daily task delivery failed",it)}
        }
        AudioService.getInstance(this).onGameplayChanged(false)
    }
}
