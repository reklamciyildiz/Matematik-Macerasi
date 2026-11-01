package com.yigit033.matematikmacarasi.games.interactive

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveLearningStore
import com.yigit033.matematikmacarasi.learning.adaptive.AdventureSkill
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import com.yigit033.matematikmacarasi.services.AudioService
import com.yigit033.matematikmacarasi.services.EventBus
import com.yigit033.matematikmacarasi.services.GameProgressService
import com.yigit033.matematikmacarasi.services.GameStatisticsHelper
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme

/**
 * Canonical entry for all existing SpaceMath links and game cards.
 * Pure engine + versioned local store; no legacy online gate or disruptive ads.
 */
class SpaceMathAdventureActivity:ComponentActivity() {
    private lateinit var store:SpaceExpeditionStore
    private lateinit var stars:StarManager
    private lateinit var audio:AudioService
    private lateinit var statistics:GameStatisticsHelper
    private lateinit var adaptive:AdaptiveLearningStore
    private var run by mutableStateOf<SpaceRun?>(null)
    private var feedback by mutableStateOf("")
    private var recordsRevision by mutableIntStateOf(0)
    private var nonce=0L
    private var startedAt=0L

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        runCatching {
            store=SpaceExpeditionStore(this)
            stars=StarManager.getInstance(applicationContext)
            audio=AudioService.getInstance(applicationContext)
            statistics=GameStatisticsHelper(applicationContext)
            adaptive=AdaptiveLearningStore(this)
            run=store.load()
            run?.takeIf {it.completed}?.let(::deliver)
        }.onFailure {
            Log.e(TAG,"Failed to open space game",it)
            finish()
            return
        }
        setContent {
            MatematikMacerasiTheme {
                @Suppress("UNUSED_VARIABLE")
                val revision=recordsRevision
                SpaceExpeditionScreen(
                    run=run,
                    recommended=adaptive.progress(AdventureSkill.SPACE).currentTier,
                    records=SpacePlanet.entries.associateWith {store.record(it)},
                    historicalStars=SpacePlanet.entries.associateWith {
                        stars.getGameStars(rewardKey(it))
                    },
                    feedback=feedback,
                    onBegin={planet,tier->start(planet,tier)},
                    onSteer={lane->transition {SpaceExpeditionEngine.steer(it,lane)}},
                    onIntercept={transition {SpaceExpeditionEngine.intercept(it)}},
                    onFuel={amount->transition {SpaceExpeditionEngine.adjustFuel(it,amount)}},
                    onIgnite={transition {SpaceExpeditionEngine.ignite(it)}},
                    onConnect={node->transition {SpaceExpeditionEngine.connect(it,node)}},
                    onHint={transition {SpaceExpeditionEngine.hint(it)}},
                    onNext={transition {SpaceExpeditionEngine.next(it)}},
                    onExit={finish()}
                )
            }
        }
    }
    private fun start(planet:SpacePlanet,tier:LearningTier):Boolean {
        val time=System.currentTimeMillis()
        val seed=maxOf(time, (run?.seed?:0L)+1, ++nonce).coerceAtLeast(1)
        val next=SpaceExpeditionEngine.begin(seed,planet,tier)
        if(!store.save(next)) {
            feedback="Yeni keşif kaydedilemedi. Tekrar dene."
            return false
        }
        startedAt=time
        feedback=""
        run=next
        return true
    }
    private fun transition(action:(SpaceRun)->SpaceRun) {
        val before=run?:return
        if(before.completed)return
        val next=runCatching {action(before)}.onFailure {
            Log.e(TAG,"Space mission action failed",it)
            feedback="Etkileşim gerçekleştirilemedi, tekrar dene."
        }.getOrNull()?:return
        if(before==next)return
        if(!store.save(next)) {
            feedback="İlerleme kaydedilemedi; cihaz depolamasını kontrol et."
            return
        }
        feedback=when {
            next.errors>before.errors ->
                "⚠️ Enerji kaybı! Doğru işlemi düşün, göreve devam edebilirsin."
            next.solved && !before.solved -> "✨ Görev tamamlandı! Açıklamayı incele."
            else -> ""
        }
        run=next
        if(next.solved && !before.solved)runCatching {
            audio.playCorrectSound()
        }.onFailure {Log.w(TAG,"Correct SFX unavailable",it)}
        else if(next.errors>before.errors)runCatching {
            audio.playWrongSound()
        }.onFailure {Log.w(TAG,"Wrong SFX unavailable",it)}
        if(next.completed && !before.completed)deliver(next)
    }
    /** Preserve legacy first five reward keys and their existing best values. */
    private fun rewardKey(planet:SpacePlanet):String =
        "SpaceMathAdventureActivity_Level_"+(planet.ordinal+1)

    private fun deliver(result:SpaceRun) {
        if(!result.completed || !SpaceExpeditionEngine.valid(result))return
        val earned=SpaceExpeditionEngine.stars(result)
        if(stars.addStars(rewardKey(result.planet),earned)) {
            EventBus.updateStars(stars.getDisplayStars())
        } else Log.w(TAG,"Star checkpoint failed. Will retry when game opens again.")
        if(store.recordUnique(result))recordsRevision++
        if(store.markDelivered(result.seed)) {
            runCatching {
                statistics.recordGameCompletion(
                    gameType=GameType.SPACE_MATH_ADVENTURE,
                    level=result.planet.ordinal+1,
                    score=SpaceExpeditionEngine.score(result),
                    stars=earned,
                    gameStartTime=if(startedAt>0)startedAt else result.seed,
                    taskId=intent.getIntExtra("daily_task_id",-1)
                )
                GameProgressService.getInstance(this).saveProgress(
                    "SpaceMathAdventureActivity_Level_"+(result.planet.ordinal+1),
                    result.planet.ordinal+1,SpaceExpeditionEngine.score(result),earned
                )
            }.onFailure {Log.e(TAG,"Space statistics failed",it)}
        }
        runCatching {
            adaptive.recordCompletion(
                AdventureSkill.SPACE,result.seed,result.tier,
                result.attempts,result.errors,result.hints
            )
        }.onFailure {Log.w(TAG,"Space adaptive history write failed",it)}
    }
    private companion object {const val TAG="SpaceExpedition2"}
}
