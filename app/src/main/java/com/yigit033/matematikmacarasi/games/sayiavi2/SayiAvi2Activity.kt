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
            // İşlem ortasında kapanan eski koşulardaki tek seferlik yıldız kaybını onar.
            val storage = SayiAviStorage.getInstance(this)
            if (storage.isMissionCompleted() && !storage.isStarAwarded()) {
                if (starManager.addStars("sayi_avi_2_stage1", 1)) {
                    storage.markStarAwarded()
                } else {
                    Log.w(TAG, "Bekleyen tek seferlik yıldız sonraki açılışta yeniden denenecek")
                }
            }
            // Recover the first-clear star after a crash between checkpoint and wallet update.
            for(grade in 1..8)awardGradeFirstClear(grade)
        } catch (e: Exception) {
            Log.e(TAG, "Servisler başlatılırken hata: ${e.message}", e)
            finish()
            return
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
                        onForestReturn = {
                            currentStars = starManager.getDisplayStars()
                        },
                        onGradeWorldFinished = { grade,attempts,errors,seconds ->
                            handleGradeWorldCompleted(grade,attempts,errors,seconds) {
                                currentStars = it
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

    /**
     * Grade-specific first-clear rewards are separate from the legacy Kumsal Koyu
     * achievement. StarManager's idempotent key is the authoritative guard.
     */
    private fun awardGradeFirstClear(grade:Int):Boolean {
        val store=NumberHuntV3Store(this)
        if(!store.hasCompleted(grade) || store.starAcknowledged(grade))return false
        val rewardId="sayi_avi_3_grade_$grade"
        val wasAlreadyAwarded=starManager.getGameStars(rewardId)>0
        val confirmed=starManager.addStars(rewardId,1)
        if(confirmed)store.acknowledgeStar(grade)
        return confirmed && !wasAlreadyAwarded
    }

    private fun handleGradeWorldCompleted(grade:Int,attempts:Int,errors:Int,
        seconds:Long,onStarsUpdated:(Int)->Unit){
        try {
            val awarded=awardGradeFirstClear(grade)
            if(awarded){
                val total=starManager.getDisplayStars()
                EventBus.updateStars(total)
                onStarsUpdated(total)
            }
            gameStatisticsHelper.recordGameCompletion(
                gameType=GameType.NUMBER_HUNT_GAME,
                level=grade,
                score=if(attempts==0)100 else ((attempts-errors).coerceAtLeast(0)*100/attempts),
                stars=if(awarded)1 else 0,
                gameStartTime=System.currentTimeMillis()-seconds.coerceIn(1,7200)*1000L,
                taskId=dailyTaskId
            )
        }catch(e:Exception){
            Log.e(TAG,"Sınıf macerası sonucu kaydedilemedi: ${e.message}",e)
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
                val awarded = starManager.addStars("sayi_avi_2_stage1", 1)
                if (awarded) {
                    SayiAviStorage.getInstance(this).markStarAwarded()
                    val newTotal = starManager.getDisplayStars()
                    EventBus.updateStars(newTotal)
                    onStarsUpdated(newTotal)
                    Toast.makeText(this, "+1 Yıldız profilinize eklendi!", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "İlk tamamlama ödülü kaydedildi. Yeni toplam: $newTotal")
                } else {
                    Toast.makeText(this,
                        "Yıldız henüz kaydedilemedi; oyunu yeniden açtığında tekrar denenecek.",
                        Toast.LENGTH_LONG).show()
                    Log.e(TAG, "Yıldız kaydı başarısız; tek seferlik ödül hakkı saklandı")
                }
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

