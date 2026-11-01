package com.yigit033.matematikmacarasi.games.interactive

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.learning.adaptive.GradePolicy
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import com.yigit033.matematikmacarasi.services.AudioService
import com.yigit033.matematikmacarasi.services.DailyGameLimitService
import com.yigit033.matematikmacarasi.services.GameProgressService
import com.yigit033.matematikmacarasi.services.GameStatisticsHelper
import com.yigit033.matematikmacarasi.services.PlayerProfileService
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme

/**
 * S3.4: same Android entry point and historic star/analytics identifiers.
 * One screen + pure engine + separate checkpoint store. No forced internet,
 * global timer, gameplay ads or duplicate legacy maze implementation.
 */
class MathMazeGameActivity:ComponentActivity() {
    private lateinit var store:MazeAdventureStore
    private lateinit var limit:DailyGameLimitService
    private lateinit var purchase:PurchaseManager
    private lateinit var statistics:GameStatisticsHelper
    private lateinit var audio:AudioService
    private var run by mutableStateOf<MazeExpedition?>(null)
    private var feedback by mutableStateOf("")
    private var limitRevision by mutableIntStateOf(0)
    private var completedCount by mutableIntStateOf(0)
    private var progressRevision by mutableIntStateOf(0)
    private var gameStartAt=0L
    private var seedNonce=0L

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window,false)
        window.statusBarColor=android.graphics.Color.TRANSPARENT
        window.navigationBarColor=android.graphics.Color.TRANSPARENT
        WindowCompat.getInsetsController(window,window.decorView)
            .isAppearanceLightStatusBars=false
        WindowCompat.getInsetsController(window,window.decorView)
            .isAppearanceLightNavigationBars=false
        store=MazeAdventureStore(applicationContext)
        limit=DailyGameLimitService.getInstance(applicationContext)
        purchase=PurchaseManager.getInstance(applicationContext)
        statistics=GameStatisticsHelper(applicationContext)
        audio=AudioService.getInstance(applicationContext)
        run=store.load()
        completedCount=store.runsCompleted()
        if(run?.completed==true) deliverCompletion(run!!)
        val grade=GradePolicy.parse(
            PlayerProfileService.getInstance(applicationContext).getUserClass())
        val recommended=when {
            grade==null || grade<=2 -> LearningTier.TEMEL
            grade==3 -> LearningTier.GELISEN
            else -> LearningTier.USTALIK
        }
        purchase.queryPurchases()
        setContent {
            MatematikMacerasiTheme {
                val purchaseFlow by purchase.premiumState.collectAsState()
                val premium=purchaseFlow || purchase.isPremiumSync()
                // Compose observes this revision after a new Free run uses a daily slot.
                @Suppress("UNUSED_VARIABLE")
                val revision=limitRevision + progressRevision
                MazeAdventureScreen(
                    run=run,
                    isPremium=premium,
                    remainingPlays=limit.getRemainingMathMazePlays(premium),
                    recommendedTier=recommended,
                    completedRuns=completedCount,
                    regionRecords=MazeRegion.entries.associateWith {store.regionRecord(it)},
                    feedback=feedback,
                    onStart={chapter,tier -> startRun(chapter,tier,premium)},
                    onMove={dir -> move(dir)},
                    onAnswer={answer -> answerGate(answer)},
                    onHint={applyChange { MazeAdventureEngine.hint(it) }},
                    onExit={finish()},
                    onUpgrade={purchase.buyPremium(this@MathMazeGameActivity)}
                )
            }
        }
    }

    private fun startRun(region:MazeRegion,tier:LearningTier,premium:Boolean):Boolean {
        if(!limit.canPlayMathMaze(premium))return false
        seedNonce++
        val seed=(System.currentTimeMillis()+seedNonce).coerceAtLeast(1L)
        val fresh=runCatching {MazeAdventureEngine.begin(seed,region,tier)}
            .onFailure {Log.e(TAG,"Could not construct a valid maze",it)}
            .getOrNull()?:return false
        // Save first. Failing to persist a run must never count as a paid-for daily attempt.
        if(!store.save(fresh))return false
        if(!premium && !limit.recordMathMazePlay()) {
            Log.w(TAG,"New run saved but daily limit could not be committed")
        }
        gameStartAt=System.currentTimeMillis()
        run=fresh
        feedback="Kilitli kapıları çözerek altın portala ulaş!"
        limitRevision++
        return true
    }

    /** Persist before publishing a Compose state change; never swallow failures silently. */
    private fun applyChange(action:(MazeExpedition)->MazeExpedition):Boolean {
        val before=run?:return false
        if(before.completed)return false
        val next=runCatching {action(before)}.onFailure {
            Log.e(TAG,"Maze interaction failed",it)
            feedback="İşlem gerçekleştirilemedi. Tekrar dene."
        }.getOrNull()?:return false
        if(next==before)return false
        if(!store.save(next)) {
            Log.e(TAG,"Maze interaction checkpoint rejected or could not be persisted")
            feedback="İlerleme kaydedilemedi. Depolama alanını kontrol edip yeniden dene."
            return false
        }
        run=next
        if(next.completed && !before.completed)deliverCompletion(next)
        return true
    }

    private fun move(dir:MazeDirection) {
        val before=run?:return
        if(before.completed)return
        if(before.pendingCell>=0) {
            feedback="Önce matematik kapısındaki soruyu cevapla."
            return
        }
        val world=runCatching {
            MazeAdventureEngine.generate(before.seed,before.region,before.tier)
        }.onFailure {Log.e(TAG,"Could not load maze for movement",it)}
            .getOrNull()?:let {
                feedback="İşlem gerçekleştirilemedi. Tekrar dene."
                return
            }
        val next=MazeAdventureEngine.step(before,world,dir)
        if(next==before) {
            feedback="Bu yönde duvar var. Açık yolu izle veya pusuladan yardım al."
            return
        }
        if(applyChange { MazeAdventureEngine.step(it,world,dir) }) {
            feedback=if(next.pendingCell>=0)
                "🔐 Kapıya ulaştın. Soruyu çözerek ilerle."
            else "Haritada ilerledin. Çıkışa doğru devam et."
        }
    }

    private fun answerGate(selected:Int) {
        val before=run?:return
        if(before.pendingCell<0)return
        val world=runCatching {
            MazeAdventureEngine.generate(before.seed,before.region,before.tier)
        }.onFailure {Log.e(TAG,"Could not load maze for answer",it)}
            .getOrNull()?:let {
                feedback="İşlem gerçekleştirilemedi. Tekrar dene."
                return
            }
        val active=world.gates[before.pendingCell]?:let {
            Log.e(TAG,"Pending math gate not found for cell "+before.pendingCell)
            feedback="İşlem gerçekleştirilemedi. Tekrar dene."
            return
        }
        if(selected !in active.choices)return
        val correct=active.correct==selected
        if(applyChange { MazeAdventureEngine.answer(it,world,selected) }) {
            feedback=if(correct) "✨ "+active.explanation
                else "Bu cevap olmadı. İpucu alabilir veya yeniden deneyebilirsin."
            runCatching {
                if(correct)audio.playCorrectSound() else audio.playWrongSound()
            }.onFailure {Log.w(TAG,"Audio unavailable",it)}
        }
    }

    /** A completed run can only deliver telemetry and legacy stars once. */
    private fun deliverCompletion(result:MazeExpedition) {
        if(!result.completed || !store.markCompletionDelivered(result.seed))return
        val level=result.region.ordinal // legacy MazeLevel.levelIndex is zero-based.
        val stars=MazeAdventureEngine.stars(result)
        val score=MazeAdventureEngine.score(result)
        runCatching {
            // Legacy key is deliberately unchanged so older awards remain high-water.
            StarManager.getInstance(this).addStars("MathMazeGameActivity_$level",stars)
        }.onFailure { Log.e(TAG,"Star delivery failed",it) }
        runCatching {
            GameProgressService.getInstance(this)
                .saveProgress("MathMazeGameActivity",level,score,stars)
        }.onFailure { Log.e(TAG,"Progress sync failed",it) }
        runCatching {
            statistics.recordGameCompletion(
                gameType=GameType.MATH_MAZE_GAME,
                level=level,score=score,stars=stars,
                gameStartTime=if(gameStartAt>0L)gameStartAt
                    else System.currentTimeMillis(),
                taskId=intent.getIntExtra("daily_task_id",-1))
        }.onFailure { Log.e(TAG,"Game stats delivery failed",it) }
        if(store.recordUniqueCompletion(result)) {
            completedCount=store.runsCompleted()
            progressRevision++
        }
        runCatching {audio.playSuccessSound()}.onFailure {Log.w(TAG,"No finish sound",it)}
    }

    private companion object {const val TAG="MazeAdventure2"}
}
