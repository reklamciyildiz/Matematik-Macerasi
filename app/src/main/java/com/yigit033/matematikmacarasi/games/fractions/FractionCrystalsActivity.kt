package com.yigit033.matematikmacarasi.games.fractions

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

class FractionCrystalsActivity: ComponentActivity() {
    private lateinit var store: FractionCrystalStore
    private lateinit var starManager: StarManager

    companion object {
        private const val TAG = "FractionCrystals"
        private const val FIRST_REWARD = "fraction_crystals_first_complete_2026"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store=FractionCrystalStore(this)
        starManager=StarManager.getInstance(this)
        if(store.load().finished) deliverCompletion(store.load())
        val grade=GradeAwareOperationPolicy.grade(
            PlayerProfileService.getInstance(this).getUserClass())
        setContent {
            MatematikMacerasiTheme {
                Surface(Modifier.fillMaxSize()) {
                    FractionCrystalsScreen(
                        store=store,grade=grade,
                        onExit={finish()},
                        onCompleted={ run -> deliverCompletion(run) }
                    )
                }
            }
        }
    }

    private fun deliverCompletion(run: CrystalRun) {
        if(!run.finished) return
        if(!store.firstRewardGranted()) {
            runCatching {
                if(starManager.addStars(FIRST_REWARD,3) && store.markFirstReward()) {
                    EventBus.updateStars(starManager.getDisplayStars())
                }
            }.onFailure { Log.e(TAG,"First-time reward not persisted",it) }
        }
        if(store.markDelivered(run.seed)) {
            runCatching {
                GameStatisticsHelper(this).recordGameCompletion(
                    gameType=GameType.FRACTION_CRYSTALS,
                    level=run.difficulty+1,
                    score=run.cleanAnswers*100/FractionCrystalEngine.TASK_COUNT,
                    stars=if(run.completedRuns==1 && store.firstRewardGranted()) 3 else 0,
                    gameStartTime=run.seed,
                    taskId=intent.getIntExtra("daily_task_id",-1)
                )
            }.onFailure { Log.e(TAG,"Statistics/daily-task write failed",it) }
        }
        AudioService.getInstance(this).onGameplayChanged(false)
    }
}
