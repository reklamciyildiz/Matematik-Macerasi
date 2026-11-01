package com.yigit033.matematikmacarasi.games.interactive

import android.content.Context
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

/** Independent from the former MarketSimulator/ShopCart progress and all purchase data. */
class MarketAdventureStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "market_adventure_premium_v1",Context.MODE_PRIVATE
    )

    @Synchronized fun load(): MarketAdventureRun {
        val basket=prefs.getString("basket","").orEmpty().split(",")
            .mapNotNull { entry ->
                val parts=entry.split(":")
                if(parts.size!=2) null else {
                    val id=parts[0].toIntOrNull()
                    val count=parts[1].toIntOrNull()
                    if(id!=null && count!=null) MarketLine(id,count) else null
                }
            }
        val phase=MarketAdventurePhase.entries.firstOrNull {
            it.name==prefs.getString("phase",null)
        } ?: MarketAdventurePhase.WELCOME
        val run=MarketAdventureRun(
            seed=prefs.getLong("seed",0L),
            tier=LearningTier.fromSaved(prefs.getString("tier",null),LearningTier.TEMEL),
            visit=prefs.getInt("visit",0),phase=phase,
            basket=basket,paidKurus=prefs.getInt("paid",0),
            errors=prefs.getInt("errors",0),hints=prefs.getInt("hints",0),
            attempts=prefs.getInt("attempts",0),
            perfectDecisions=prefs.getInt("perfect",0),
            taskHadError=prefs.getBoolean("task_error",false),
            taskHadHint=prefs.getBoolean("task_hint",false),
            completedRuns=prefs.getInt("runs",0),
            contentVersion=prefs.getInt("content_version",1),
            premiumJourney=prefs.getBoolean("premium_journey",false),
            singleShop=prefs.getBoolean("single_shop",false),
            startShop=prefs.getInt("start_shop",0)
        )
        return if(MarketAdventureEngine.valid(run)) run else
            MarketAdventureRun(completedRuns=prefs.getInt("runs",0).coerceAtLeast(0))
    }

    @Synchronized fun save(next: MarketAdventureRun): Boolean {
        if(!MarketAdventureEngine.valid(next)) return false
        return prefs.edit()
            .putLong("seed",next.seed)
            .putString("tier",next.tier.name)
            .putInt("visit",next.visit).putString("phase",next.phase.name)
            .putString("basket",next.basket.joinToString(",") {
                "${it.productId}:${it.quantity}"
            })
            .putInt("paid",next.paidKurus)
            .putInt("errors",next.errors).putInt("hints",next.hints)
            .putInt("attempts",next.attempts)
            .putInt("perfect",next.perfectDecisions)
            .putBoolean("task_error",next.taskHadError)
            .putBoolean("task_hint",next.taskHadHint)
            .putInt("runs",next.completedRuns)
            .putInt("content_version",next.contentVersion)
            .putBoolean("premium_journey",next.premiumJourney)
            .putBoolean("single_shop",next.singleShop)
            .putInt("start_shop",next.startShop)
            .commit()
    }

    @Synchronized fun begin(tier: LearningTier, premiumEntitlement: Boolean,
        selectedShop: Int? = null): MarketAdventureRun? {
        if(selectedShop!=null && (selectedShop !in 0 until
            MarketPremiumCatalog.shops.size ||
            (!premiumEntitlement && selectedShop>=MarketPremiumCatalog.FREE_SHOPS)))
            return null
        val old=load()
        val seed=System.currentTimeMillis().coerceAtLeast(old.seed+1).coerceAtLeast(1L)
        val start=MarketAdventureRun(seed=seed,tier=tier,
            phase=MarketAdventurePhase.SHOP,completedRuns=old.completedRuns,
            contentVersion=3,premiumJourney=premiumEntitlement,
            singleShop=selectedShop!=null,startShop=selectedShop?:0,
            visit=selectedShop?:0)
        return if(save(start)) start else null
    }

    /** Best reward, not a count. Replays can improve it up to 3 stars. */
    fun bestAwardedStars(): Int = prefs.getInt("best_stars",0).coerceIn(0,3)

    @Synchronized fun markBestStars(value:Int): Boolean {
        if(value !in 1..3) return false
        return prefs.edit().putInt("best_stars",
            maxOf(bestAwardedStars(),value)).commit()
    }

    /**
     * Per-run delivery marker. Exactly once for each completed run; the
     * existing StarManager itself also enforces a high-water award.
     */
    @Synchronized fun markDelivered(seed:Long): Boolean {
        if(seed<=0 || prefs.getLong("delivered",-1L)==seed) return false
        return prefs.edit().putLong("delivered",seed).commit()
    }

    fun isFirstCompletion()=prefs.getInt("runs",0)>0
}
