package com.yigit033.matematikmacarasi.games.sayiavi2

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveLearningStore
import com.yigit033.matematikmacarasi.learning.adaptive.AdventureSkill
import com.yigit033.matematikmacarasi.services.EventBus
import com.yigit033.matematikmacarasi.services.GameStatisticsHelper
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme

/** İkinci bölge, eski Sayı Avcıları aktivitesinden bağımsızdır. */
class ForestAdventureActivity : ComponentActivity() {
    companion object {
        private const val TAG = "ForestAdventure"
        private const val REWARD_ID = "sayi_avi_forest_stage1"
    }
    private lateinit var store: ForestProgressStore
    private lateinit var stars: StarManager
    private lateinit var statistics: GameStatisticsHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            store = ForestProgressStore(this)
            stars = StarManager.getInstance(this)
            statistics = GameStatisticsHelper(this)
            recoverPendingStar()
        } catch (e: Exception) {
            Log.e(TAG, "Başlatma hatası", e)
            finish()
            return
        }

        setContent {
            MatematikMacerasiTheme {
                var starCount by remember { mutableIntStateOf(stars.getDisplayStars()) }
                Surface(modifier = Modifier.fillMaxSize()) {
                    ForestAdventureScreen(
                        store = store,
                        starCount = starCount,
                        onExit = { finish() },
                        onFinished = { session ->
                            val firstReward = !store.wasStarAwarded()
                            val awardedNow = firstReward && stars.addStars(REWARD_ID, 1)
                            if (awardedNow) store.markStarAwarded()
                            val latest = stars.getDisplayStars()
                            starCount = latest
                            if (awardedNow) EventBus.updateStars(latest)
                            AdaptiveLearningStore(this).recordCompletion(
                                AdventureSkill.FOUR_COUNTING, session.seed, session.tier,
                                session.attempts, session.errors, session.hints
                            )
                            statistics.recordGameCompletion(
                                gameType = GameType.NUMBER_HUNT_GAME,
                                level = 2,
                                score = session.accuracy,
                                stars = if (awardedNow) 1 else 0,
                                gameStartTime = session.startedAt.coerceAtLeast(1L),
                                taskId = intent.getIntExtra("daily_task_id", -1)
                            )
                        }
                    )
                }
            }
        }
    }

    private fun recoverPendingStar() {
        if (store.hasCompleted() && !store.wasStarAwarded()) {
            if (stars.addStars(REWARD_ID, 1)) {
                store.markStarAwarded()
                EventBus.updateStars(stars.getDisplayStars())
            } else Log.w(TAG, "Bekleyen bölge ödülü sonraki açılışta tekrar denenecek")
        }
    }
}
