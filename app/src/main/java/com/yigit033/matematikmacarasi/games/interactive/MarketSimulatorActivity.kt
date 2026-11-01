package com.yigit033.matematikmacarasi.games.interactive

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveLearningStore
import com.yigit033.matematikmacarasi.learning.adaptive.AdventureSkill
import com.yigit033.matematikmacarasi.services.EventBus
import com.yigit033.matematikmacarasi.services.GameStatisticsHelper
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme

/**
 * Canonical MARKET_SIMULATOR entry point. The older dialog/timer-driven gameplay
 * remains outside this Activity and cannot silently reset the new journey.
 *
 * First-time star is keyed independently; every later completion is practice.
 */
class MarketSimulatorActivity : ComponentActivity() {
    private lateinit var store: MarketAdventureStore
    private lateinit var starManager: StarManager
    private lateinit var purchaseManager: PurchaseManager
    private lateinit var stallStore: MarketStallStore

    companion object {
        private const val TAG="MarketPremium"
        private const val FIRST_REWARD="market_adventure_first_completion_v1"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        store=MarketAdventureStore(this)
        starManager=StarManager.getInstance(this)
        purchaseManager=PurchaseManager.getInstance(this)
        stallStore=MarketStallStore(this)
        purchaseManager.queryPurchases()
        recoverReward()
        val last=store.load()
        if(last.finished) recordCompleted(last)

        setContent {
            val premiumFlow by purchaseManager.premiumState.collectAsState()
            MatematikMacerasiTheme {
                Surface(Modifier.fillMaxSize()) {
                    MarketAdventureScreen(
                        store=store,
                        stallStore=stallStore,
                        hasPremium=premiumFlow || purchaseManager.isPremiumSync(),
                        onUpgrade={purchaseManager.buyPremium(this)},
                        suggestedTier=AdaptiveLearningStore(this).progress(
                            AdventureSkill.MARKET).currentTier,
                        onExit={finish()},
                        onCompletion={run->recordCompleted(run)}
                    )
                }
            }
        }
    }

    private fun recoverReward() {
        if(!store.isFirstCompletion()) return
        val run=store.load()
        if(!run.finished || run.singleShop) return
        val earned=MarketAdventureEngine.starsFor(run)
        if(earned<=store.bestAwardedStars()) return
        runCatching {
            if(starManager.addStars(FIRST_REWARD,earned) &&
                store.markBestStars(earned)) {
                EventBus.updateStars(starManager.getDisplayStars())
            }
        }.onFailure { Log.e(TAG,"Market best stars could not be recorded",it) }
    }

    private fun recordCompleted(run: MarketAdventureRun) {
        if(!run.finished) return
        if(!run.singleShop) recoverReward()
        if(store.markDelivered(run.seed)) {
            runCatching {
                GameStatisticsHelper(this).recordGameCompletion(
                    gameType=GameType.MARKET_SIMULATOR,
                    level=3,
                    score=(run.perfectDecisions*100/MarketAdventureEngine.decisionCount(run)),
                    stars=if(run.singleShop) 0 else MarketAdventureEngine.starsFor(run),
                    gameStartTime=run.seed,
                    taskId=intent.getIntExtra("daily_task_id",-1)
                )
            }.onFailure { Log.w(TAG,"Market analytics write failed",it) }
        }
        runCatching {
            AdaptiveLearningStore(this).recordCompletion(
                AdventureSkill.MARKET,run.seed,run.tier,
                run.attempts,run.errors,run.hints
            )
        }.onFailure { Log.w(TAG,"Market adaptive learning write failed",it) }
    }
}
