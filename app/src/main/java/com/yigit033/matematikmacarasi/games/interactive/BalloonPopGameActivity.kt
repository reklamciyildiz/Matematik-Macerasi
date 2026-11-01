package com.yigit033.matematikmacarasi.games.interactive

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.learning.adaptive.GradePolicy
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import com.yigit033.matematikmacarasi.services.AudioService
import com.yigit033.matematikmacarasi.services.GameProgressService
import com.yigit033.matematikmacarasi.services.GameStatisticsHelper
import com.yigit033.matematikmacarasi.services.PlayerProfileService
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme

/**
 * The existing manifest and game cards keep this Activity as their only entry.
 * No BaseGameActivity network requirement or legacy game-end advertisement call.
 */
class BalloonPopGameActivity:ComponentActivity() {
    private lateinit var store:BalloonAdventureStore
    private lateinit var audio:AudioService
    private lateinit var stars:StarManager
    private lateinit var statistics:GameStatisticsHelper
    private var run by mutableStateOf<BalloonRun?>(null)
    private var feedback by mutableStateOf("")
    private var recordsRevision by mutableStateOf(0)
    private var sessionStartedAt=0L
    private var seedNonce=0L
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        store=BalloonAdventureStore(this)
        audio=AudioService.getInstance(applicationContext)
        stars=StarManager.getInstance(applicationContext)
        statistics=GameStatisticsHelper(applicationContext)
        run=store.load()
        run?.takeIf {it.completed}?.let {deliver(it)}
        val grade=GradePolicy.parse(
            PlayerProfileService.getInstance(this).getUserClass())
        val suggested=when {
            grade==null || grade<=2 -> LearningTier.TEMEL
            grade==3 -> LearningTier.GELISEN
            else -> LearningTier.USTALIK
        }
        setContent {
            MatematikMacerasiTheme {
                @Suppress("UNUSED_VARIABLE")
                val revision=recordsRevision
                BalloonAdventureScreen(
                    run=run,
                    suggestedTier=suggested,
                    records=BalloonRealm.entries.associateWith {realm ->
                        store.record(realm)
                    },
                    historicalStars=BalloonRealm.entries.associateWith {realm ->
                        stars.getGameStars(rewardKey(realm))
                    },
                    feedback=feedback,
                    onStart={realm,tier->start(realm,tier)},
                    onChoose={choice->change { BalloonAdventureEngine.choose(it,choice) }},
                    onHint={change { BalloonAdventureEngine.hint(it) }},
                    onNext={change { BalloonAdventureEngine.continueAfterAnswer(it) }},
                    onExit={finish()}
                )
            }
        }
    }
    private fun start(realm:BalloonRealm,tier:LearningTier):Boolean {
        val now=(System.currentTimeMillis()+ ++seedNonce).coerceAtLeast(1L)
        val next=runCatching {BalloonAdventureEngine.begin(now,realm,tier)}
            .onFailure {Log.e(TAG,"Unable to create balloon game",it)}
            .getOrNull()?:return false
        if(!store.save(next)) {
            feedback="Yeni oyun kaydedilemedi. Tekrar dene."
            return false
        }
        sessionStartedAt=System.currentTimeMillis()
        run=next
        feedback=""
        return true
    }
    private fun change(action:(BalloonRun)->BalloonRun) {
        val before=run?:return
        if(before.completed)return
        val next=runCatching {action(before)}.onFailure {
            Log.e(TAG,"Balloon action failed",it)
            feedback="Bir hata oluştu, tekrar dene."
        }.getOrNull()?:return
        if(before==next)return
        if(!store.save(next)) {
            feedback="İlerleme kaydedilemedi. Depolama alanını kontrol et."
            return
        }
        feedback=""
        run=next
        if(next.resolved && !before.resolved)runCatching {
            audio.playCorrectSound()
        }.onFailure {Log.w(TAG,"Sound not available",it)}
        else if(next.errors>before.errors)runCatching {
            audio.playWrongSound()
        }.onFailure {Log.w(TAG,"Sound not available",it)}
        if(next.completed && !before.completed)deliver(next)
    }
    private fun rewardKey(realm:BalloonRealm):String=
        "BalloonPopGameActivity_level_"+(realm.ordinal+1)
    private fun deliver(result:BalloonRun) {
        if(!result.completed || !BalloonAdventureEngine.valid(result))return
        if(!stars.addStars(rewardKey(result.realm),BalloonAdventureEngine.stars(result))) {
            Log.w(TAG,"Balloon stars were not saved. Will retry on next open.")
        }
        if(store.recordUniqueCompletion(result))recordsRevision++
        if(store.markStatsDelivered(result.seed)) {
            runCatching {
                statistics.recordGameCompletion(
                    gameType=GameType.BALLOON_POP_GAME,
                    level=result.realm.ordinal+1,
                    score=BalloonAdventureEngine.score(result),
                    stars=BalloonAdventureEngine.stars(result),
                    gameStartTime=if(sessionStartedAt>0L)sessionStartedAt else result.seed,
                    taskId=intent.getIntExtra("daily_task_id",-1)
                )
                GameProgressService.getInstance(this).saveProgress(
                    "BalloonPopGameActivity",result.realm.ordinal+1,
                    BalloonAdventureEngine.score(result),BalloonAdventureEngine.stars(result)
                )
            }.onFailure {Log.e(TAG,"Completion delivery issue",it)}
        }
    }
    private companion object {const val TAG="BalloonAdventure2"}
}
