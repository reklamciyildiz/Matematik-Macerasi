package com.yigit033.matematikmacarasi.games.time

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.games.basic.GradeAwareOperationPolicy
import com.yigit033.matematikmacarasi.services.*
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme

/** Canonical offline time-world entry, independent from existing saves and billing. */
class TimeTowerActivity:ComponentActivity() {
    private lateinit var progress:TimeTowerStore
    private lateinit var stars:StarManager

    companion object {
        private const val TAG="TimeTower"
        private const val FIRST_REWARD="time_tower_first_complete_v1"
    }

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        progress=TimeTowerStore(this)
        stars=StarManager.getInstance(this)
        if(progress.load().finished)deliverCompletion(progress.load())
        val grade=GradeAwareOperationPolicy.grade(
            PlayerProfileService.getInstance(this).getUserClass())
        setContent {
            MatematikMacerasiTheme {
                Surface(modifier=Modifier.fillMaxSize()) {
                    TimeTowerScreen(
                        store=progress,profileGrade=grade,
                        onExit={finish()},
                        onCompleted={ run->deliverCompletion(run) }
                    )
                }
            }
        }
    }

    private fun deliverCompletion(run:TowerRun) {
        if(!run.finished)return
        if(!progress.rewardRecorded()) {
            runCatching {
                if(stars.addStars(FIRST_REWARD,3) && progress.markRewardRecorded()) {
                    EventBus.updateStars(stars.getDisplayStars())
                }
            }.onFailure { Log.w(TAG,"First reward pending; will retry on next launch",it) }
        }
        if(progress.deliveredSeed()!=run.seed) {
            runCatching {
                GameStatisticsHelper(this).recordGameCompletion(
                    gameType=GameType.TIME_TOWER,
                    level=run.difficulty+1,
                    score=run.clean*100/TimeTowerEngine.STOPS,
                    stars=if(run.completedRuns==1 && progress.rewardRecorded())3 else 0,
                    gameStartTime=run.seed,
                    taskId=intent.getIntExtra("daily_task_id",-1)
                )
                if(!progress.markDelivered(run.seed))
                    Log.w(TAG,"Statistics done but delivery checkpoint not recorded")
            }.onFailure { Log.w(TAG,"Time tower stats/task integration failed",it) }
        }
        AudioService.getInstance(this).onGameplayChanged(false)
    }
}
