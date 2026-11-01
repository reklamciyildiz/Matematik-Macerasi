package com.yigit033.matematikmacarasi.games.interactive

/**
 * A persistent toy-economy. Entirely fictional cash, with no purchases, IAP,
 * trading, banking or wagering. Pure Kotlin for exhaustive unit testing.
 */
data class StallItem(
    val id:Int,val name:String,val emoji:String,
    val wholesaleKurus:Int,val fairPriceKurus:Int
)
data class StallState(
    val walletKurus:Int=15000,
    val inventory:List<MarketLine> = emptyList(),
    val shopLevel:Int=1,
    val sales:Int=0,
    val grossProfitKurus:Int=0,
    val earnedKurus:Int=0,
    val purchases:Int=0
)
object MarketStallEngine {
    val products:List<StallItem> = listOf(
        StallItem(1,"Elma Sepeti","🍎",1200,1700),
        StallItem(2,"Kurabiye","🍪",900,1400),
        StallItem(3,"Çiçek","💐",1800,2500),
        StallItem(4,"Oyuncak","🧸",2300,3100),
        StallItem(5,"Defter","📒",1400,2000),
        StallItem(6,"Meyve Suyu","🧃",800,1300)
    )
    fun capacity(state:StallState)=2+state.shopLevel*2
    fun upgradePrice(state:StallState)=4000*state.shopLevel
    fun stock(state:StallState,id:Int) =
        state.inventory.firstOrNull{it.productId==id}?.quantity ?: 0
    fun prices(item:StallItem):List<Int> =
        listOf(item.wholesaleKurus+200,item.fairPriceKurus,
            item.fairPriceKurus+300)
    fun valid(state:StallState):Boolean=
        state.walletKurus in 0..10_000_000 &&
        state.shopLevel in 1..5 &&
        state.sales>=0 && state.purchases>=0 &&
        state.grossProfitKurus>=0 && state.earnedKurus>=0 &&
        state.inventory.size<=products.size &&
        state.inventory.map {it.productId}.distinct().size==state.inventory.size &&
        state.inventory.all {line ->
            products.any {it.id==line.productId} &&
                line.quantity in 1..capacity(state)
        }
    fun buy(state:StallState,id:Int):StallState {
        if(!valid(state)) return state
        val item=products.firstOrNull{it.id==id} ?: return state
        val current=stock(state,id)
        if(current>=capacity(state)||state.walletKurus<item.wholesaleKurus)return state
        return state.copy(
            walletKurus=state.walletKurus-item.wholesaleKurus,
            inventory=state.inventory.filterNot{it.productId==id}+
                MarketLine(id,current+1),
            purchases=state.purchases+1)
    }
    /** Customers accept at most the clearly advertised fair price. */
    fun sell(state:StallState,id:Int,priceKurus:Int):StallState {
        if(!valid(state)||stock(state,id)<=0)return state
        val item=products.firstOrNull{it.id==id} ?: return state
        if(priceKurus !in prices(item) || priceKurus>item.fairPriceKurus)return state
        val remaining=stock(state,id)-1
        return state.copy(
            walletKurus=state.walletKurus+priceKurus,
            inventory=state.inventory.filterNot{it.productId==id}+
                if(remaining>0)listOf(MarketLine(id,remaining))else emptyList(),
            sales=state.sales+1,
            earnedKurus=state.earnedKurus+priceKurus,
            grossProfitKurus=state.grossProfitKurus+
                (priceKurus-item.wholesaleKurus))
    }
    fun upgrade(state:StallState):StallState {
        if(!valid(state)||state.shopLevel>=5 ||
            state.walletKurus<upgradePrice(state)) return state
        return state.copy(
            walletKurus=state.walletKurus-upgradePrice(state),
            shopLevel=state.shopLevel+1)
    }
}