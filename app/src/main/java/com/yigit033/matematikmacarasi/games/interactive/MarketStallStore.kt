package com.yigit033.matematikmacarasi.games.interactive

import android.content.Context

/** Isolated fictional coins. Never touches StarManager or Google Play billing. */
class MarketStallStore(context:Context) {
    private val prefs=context.applicationContext.getSharedPreferences(
        "market_stall_economy_v1",Context.MODE_PRIVATE)
    @Synchronized fun load():StallState {
        val stock=prefs.getString("inventory","").orEmpty().split(",")
            .mapNotNull {entry ->
                val p=entry.split(":")
                if(p.size!=2)null else {
                    val id=p[0].toIntOrNull();val count=p[1].toIntOrNull()
                    if(id!=null && count!=null)MarketLine(id,count) else null
                }
            }
        val state=StallState(
            walletKurus=prefs.getInt("wallet",15000),
            inventory=stock,
            shopLevel=prefs.getInt("level",1),
            sales=prefs.getInt("sales",0),
            grossProfitKurus=prefs.getInt("profit",0),
            earnedKurus=prefs.getInt("earned",0),
            purchases=prefs.getInt("purchases",0)
        )
        return if(MarketStallEngine.valid(state))state else StallState()
    }
    @Synchronized fun save(state:StallState):Boolean {
        if(!MarketStallEngine.valid(state))return false
        return prefs.edit()
            .putInt("wallet",state.walletKurus)
            .putString("inventory",state.inventory.joinToString(","){
                "${it.productId}:${it.quantity}"})
            .putInt("level",state.shopLevel)
            .putInt("sales",state.sales)
            .putInt("profit",state.grossProfitKurus)
            .putInt("earned",state.earnedKurus)
            .putInt("purchases",state.purchases)
            .commit()
    }
}