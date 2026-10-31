package com.yigit033.matematikmacarasi.games.sayiavi2

import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import com.yigit033.matematikmacarasi.services.DailyTaskManager
import com.yigit033.matematikmacarasi.services.EventBus
import com.yigit033.matematikmacarasi.services.GameStatisticsHelper
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme

/**
 * Sayı Avı 2.0 — Kayıp Hazine Ana Aktivitesi.
 * GDD v1.0 ve Sprint G1 gereksinimlerini karşılayan,
 * modern Jetpack Compose tabanlı 3 aşamalı oyun aktivitesi.
 */
class SayiAvi2Activity : ComponentActivity() {

    companion object {
        private const val TAG = "SayiAvi2Activity"
    }

    private lateinit var starManager: StarManager
    private lateinit var dailyTaskManager: DailyTaskManager
    private lateinit var gameStatisticsHelper: GameStatisticsHelper
    private var dailyTaskId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            starManager = StarManager.getInstance(this)
            dailyTaskManager = DailyTaskManager.getInstance(this)
            gameStatisticsHelper = GameStatisticsHelper(this)
            dailyTaskId = intent.getIntExtra("daily_task_id", -1)
        } catch (e: Exception) {
            Log.e(TAG, "Servisler başlatılırken hata: ${e.message}", e)
        }

        setContent {
            MatematikMacerasiTheme {
                var currentStars by remember {
                    mutableIntStateOf(
                        try { starManager.getDisplayStars() } catch (e: Exception) { 0 }
                    )
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    SayiAvi2Screen(
                        currentStars = currentStars,
                        onRewardEarned = { isFirstTime, summary ->
                            handleMissionCompleted(isFirstTime, summary) { updatedStars ->
                                currentStars = updatedStars
                            }
                        },
                        onExitGame = {
                            finish()
                        }
                    )
                }
            }
        }
    }

    private fun handleMissionCompleted(
        isFirstTime: Boolean,
        summary: SayiAviLearningSummary,
        onStarsUpdated: (Int) -> Unit
    ) {
        try {
            if (isFirstTime) {
                // 1 Yıldız ekle (Idempotent: addStars("sayi_avi_2_stage1", 1) tekil anahtarla korur)
                starManager.addStars("sayi_avi_2_stage1", 1)
                val newTotal = starManager.getDisplayStars()
                EventBus.updateStars(newTotal)
                onStarsUpdated(newTotal)

                Toast.makeText(this, "+1 Yıldız profilinize eklendi!", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "İlk tamamlama ödülü: 1 yıldız verildi. Yeni toplam: $newTotal")
            } else {
                Log.d(TAG, "Replay: Yıldız verilmedi (idempotency korundu).")
            }

            // Tek seferlik yıldız ödülünü korurken bütün tamamlamaları kaydet.
            gameStatisticsHelper.recordGameCompletion(
                gameType = GameType.NUMBER_HUNT_GAME,
                level = 1,
                score = if (summary.complete) summary.actionAccuracyPercent else 0,
                stars = if (isFirstTime) 1 else 0,
                gameStartTime = System.currentTimeMillis() -
                    summary.elapsedSeconds.coerceIn(1L, 3600L) * 1000L,
                taskId = dailyTaskId
            )
        } catch (e: Exception) {
            Log.e(TAG, "Ödül işleme sırasında hata: ${e.message}", e)
        }
    }
}

