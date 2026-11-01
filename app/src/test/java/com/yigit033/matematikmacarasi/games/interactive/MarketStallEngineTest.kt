package com.yigit033.matematikmacarasi.games.interactive

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketStallEngineTest {
    @Test fun purchasesSalesAndProfitRespectActualStockAndCustomerDemand() {
        var s=StallState()
        assertTrue(MarketStallEngine.valid(s))
        assertEquals(s,MarketStallEngine.sell(s,1,1700))
        s=MarketStallEngine.buy(s,1)
        assertEquals(1,MarketStallEngine.stock(s,1))
        assertEquals(13800,s.walletKurus)
        assertEquals(s,MarketStallEngine.sell(s,1,2000)) // customer rejects
        s=MarketStallEngine.sell(s,1,1700)
        assertEquals(0,MarketStallEngine.stock(s,1))
        assertEquals(15500,s.walletKurus)
        assertEquals(500,s.grossProfitKurus)
        assertEquals(1,s.sales)
        assertEquals(1,s.purchases)
        assertEquals(s,MarketStallEngine.sell(s,1,1700))
    }
    @Test fun invalidInventoryAndNegativeMoneyAreRejected() {
        val item=MarketStallEngine.products.first()
        assertFalse(MarketStallEngine.valid(StallState(walletKurus=-1)))
        assertFalse(MarketStallEngine.valid(StallState(
            inventory=listOf(MarketLine(999,2)))))
        assertFalse(MarketStallEngine.valid(StallState(
            inventory=listOf(MarketLine(item.id,1),MarketLine(item.id,1)))))
        assertFalse(MarketStallEngine.valid(StallState(
            inventory=listOf(MarketLine(item.id,99)))))
        assertFalse(MarketStallEngine.valid(StallState(shopLevel=6)))
        val noMoney=StallState(walletKurus=0)
        assertEquals(noMoney,MarketStallEngine.buy(noMoney,item.id))
    }
    @Test fun cannotMagicallyMultiplyStockOrSellAboveFairPrice() {
        var state=StallState(walletKurus=150000)
        repeat(MarketStallEngine.capacity(state)+2) {
            state=MarketStallEngine.buy(state,1)
        }
        assertEquals(MarketStallEngine.capacity(state),
            MarketStallEngine.stock(state,1))
        val before=state
        assertEquals(before,MarketStallEngine.sell(state,1,2000))
        val firstPrice=MarketStallEngine.prices(
            MarketStallEngine.products.first()).first()
        state=MarketStallEngine.sell(state,1,firstPrice)
        assertEquals(MarketStallEngine.capacity(before)-1,
            MarketStallEngine.stock(state,1))
        assertTrue(state.grossProfitKurus>0)
        assertTrue(MarketStallEngine.valid(state))
    }
    @Test fun capacityUpgradesConsumeCoinsAndIncreaseLimitOnlyWhenAffordable() {
        var s=StallState()
        assertEquals(4,MarketStallEngine.capacity(s))
        s=MarketStallEngine.upgrade(s)
        assertEquals(2,s.shopLevel)
        assertEquals(11000,s.walletKurus)
        assertEquals(6,MarketStallEngine.capacity(s))
        val poor=StallState(walletKurus=100)
        assertEquals(poor,MarketStallEngine.upgrade(poor))
        assertEquals(StallState(shopLevel=5),MarketStallEngine.upgrade(StallState(shopLevel=5)))
    }
    @Test fun allGoodsHaveUniqueAndProfitableDenominationChoices() {
        assertEquals(6,MarketStallEngine.products.size)
        assertEquals(6,MarketStallEngine.products.map{it.id}.distinct().size)
        MarketStallEngine.products.forEach { product ->
            val prices=MarketStallEngine.prices(product)
            assertEquals(3,prices.distinct().size)
            assertTrue(prices[0]>product.wholesaleKurus)
            assertTrue(prices[1]>product.wholesaleKurus)
            assertTrue(prices[2]>product.fairPriceKurus)
        }
    }
}