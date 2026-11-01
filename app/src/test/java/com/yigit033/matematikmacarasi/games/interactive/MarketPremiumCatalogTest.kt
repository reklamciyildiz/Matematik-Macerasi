package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketPremiumCatalogTest {
    @Test fun catalogIncludesTenDistinctStoresAndOneHundredTwentyUniqueProducts() {
        val shops=MarketPremiumCatalog.shops
        assertEquals(10,shops.size)
        assertEquals(120,MarketPremiumCatalog.productCount)
        assertEquals(10,shops.map{it.title}.distinct().size)
        assertTrue(shops.take(3).none{it.isPro})
        assertTrue(shops.drop(3).all{it.isPro})
        assertEquals(120,shops.flatMap{it.products}.map{it.id}.distinct().size)
        shops.forEach { store ->
            assertEquals(12,store.products.size)
            assertEquals(12,store.products.map{it.name}.distinct().size)
            assertTrue(store.products.all { it.priceKurus>0 && it.unit.isNotBlank() })
        }
    }

    @Test fun allTiersGiveDeterministicValidSixItemShelvesAndPayOptions() {
        for(tier in LearningTier.entries) for(seed in 1L..120L) {
            val scenarios=MarketPremiumCatalog.scenarios(seed,tier)
            assertEquals(scenarios,MarketPremiumCatalog.scenarios(seed,tier))
            assertEquals(10,scenarios.size)
            for((i,scenario) in scenarios.withIndex()) {
                assertEquals(MarketPremiumCatalog.shops[i].title,scenario.location)
                assertEquals(6,scenario.products.size)
                assertEquals(6,scenario.products.map{it.id}.distinct().size)
                assertTrue(scenario.needed.all {n ->
                    n.productId in scenario.products.map{it.id} && n.quantity in 1..2 })
                assertEquals(when(tier) {
                    LearningTier.TEMEL -> 2
                    LearningTier.GELISEN -> 3
                    LearningTier.USTALIK -> 4
                },scenario.needed.size)
                assertEquals(4,scenario.payOptionsKurus.distinct().size)
                assertEquals(4,scenario.changeOptionsKurus.distinct().size)
                assertTrue(scenario.requiredTotal() in scenario.payOptionsKurus)
                assertTrue(scenario.cashierPayKurus-scenario.requiredTotal() in
                    scenario.changeOptionsKurus)
                assertTrue(scenario.budgetKurus>scenario.requiredTotal())
                val bonus=scenario.bonus!!
                assertTrue(bonus.correctKurus in bonus.choicesKurus)
                assertEquals(4,bonus.choicesKurus.distinct().size)
            }
        }
    }

    private fun solveFull(seed:Long,tier:LearningTier,pro:Boolean,shop:Int?):MarketAdventureRun {
        var run=MarketAdventureRun(seed=seed,tier=tier,contentVersion=3,
            premiumJourney=pro,singleShop=shop!=null,
            startShop=shop?:0,visit=shop?:0,
            phase=MarketAdventurePhase.SHOP)
        repeat(MarketAdventureEngine.visitCount(run)) {
            val scene=MarketAdventureEngine.scenario(run)!!
            scene.needed.forEach {line ->
                repeat(line.quantity) {
                    run=MarketAdventureEngine.changeQuantity(run,line.productId,1)
                }
            }
            run=MarketAdventureEngine.checkBasket(run)
            assertEquals(MarketAdventurePhase.PAYMENT,run.phase)
            run=MarketAdventureEngine.choosePayment(run,scene.requiredTotal())
            assertEquals(MarketAdventurePhase.CHANGE,run.phase)
            run=MarketAdventureEngine.chooseChange(run,
                scene.cashierPayKurus-scene.requiredTotal())
            assertEquals(MarketAdventurePhase.BONUS,run.phase)
            assertFalse(run.finished)
            run=MarketAdventureEngine.chooseBonus(run,scene.bonus!!.correctKurus)
            assertTrue(MarketAdventureEngine.valid(run))
        }
        return run
    }

    @Test fun freeJourneyIsTwelveDecisionsAndPremiumJourneyIsForty() {
        for(tier in LearningTier.entries) for(seed in 1L..35L) {
            val free=solveFull(seed,tier,false,null)
            assertTrue(free.finished)
            assertEquals(3,free.visit)
            assertEquals(12,free.attempts)
            assertEquals(12,free.perfectDecisions)
            assertEquals(3,MarketAdventureEngine.starsFor(free))
            val premium=solveFull(seed,tier,true,null)
            assertTrue(premium.finished)
            assertEquals(10,premium.visit)
            assertEquals(40,premium.attempts)
            assertEquals(40,premium.perfectDecisions)
            assertEquals(3,MarketAdventureEngine.starsFor(premium))
        }
    }

    @Test fun freeStoresCannotBeForgedIntoLockedPremiumQuickSessions() {
        assertFalse(MarketAdventureEngine.valid(MarketAdventureRun(
            seed=18L,contentVersion=3,visit=3,startShop=3,
            singleShop=true,premiumJourney=false,phase=MarketAdventurePhase.SHOP)))
        for(storeIndex in 0..2) {
            val run=solveFull(91L,LearningTier.TEMEL,false,storeIndex)
            assertTrue(run.finished)
            assertEquals(storeIndex+1,run.visit)
            assertEquals(4,run.perfectDecisions)
        }
        for(storeIndex in 3..9) {
            val run=solveFull(91L,LearningTier.USTALIK,true,storeIndex)
            assertTrue(run.finished)
            assertEquals(4,run.attempts)
        }
        assertFalse(MarketAdventureEngine.valid(MarketAdventureRun(
            seed=88L,contentVersion=2,premiumJourney=true,
            phase=MarketAdventurePhase.SHOP)))
    }

    @Test fun resumedLegacyRunsKeepThreeOrSixStopsAndSameQuestions() {
        for(tier in LearningTier.entries) {
            val old=MarketAdventureRun(seed=72L,tier=tier,
                phase=MarketAdventurePhase.SHOP)
            val upgraded=old.copy(contentVersion=2)
            assertTrue(MarketAdventureEngine.valid(old))
            assertTrue(MarketAdventureEngine.valid(upgraded))
            assertEquals(3,MarketAdventureEngine.scenarios(72L,tier,1).size)
            assertEquals(6,MarketAdventureEngine.scenarios(72L,tier,2).size)
            assertEquals(3,MarketAdventureEngine.visitCount(old))
            assertEquals(6,MarketAdventureEngine.visitCount(upgraded))
        }
    }
}