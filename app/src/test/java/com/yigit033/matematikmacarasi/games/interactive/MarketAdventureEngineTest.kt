package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertNotEquals
import org.junit.Test

class MarketAdventureEngineTest {
    @Test fun threeVisitsHaveExactCentsAndUniqueOptionsForAllSeedsAndTiers() {
        for(tier in LearningTier.entries) for(seed in 1L..300L) {
            val visits=MarketAdventureEngine.scenarios(seed,tier)
            assertEquals(3,visits.size)
            assertEquals(visits,MarketAdventureEngine.scenarios(seed,tier))
            assertEquals(3,visits.map { it.location }.distinct().size)
            for(visit in visits) {
                assertEquals(4,visit.products.size)
                assertEquals(4,visit.products.map { it.id }.distinct().size)
                assertTrue(visit.needed.isNotEmpty())
                assertTrue(visit.needed.all { n ->
                    n.productId in visit.products.map { it.id } && n.quantity in 1..2
                })
                assertTrue(visit.products.all { it.priceKurus>0 })
                assertTrue(visit.budgetKurus>visit.requiredTotal())
                assertEquals(4,visit.payOptionsKurus.distinct().size)
                assertEquals(4,visit.changeOptionsKurus.distinct().size)
                assertTrue(visit.requiredTotal() in visit.payOptionsKurus)
                assertTrue(visit.cashierPayKurus-visit.requiredTotal() in
                    visit.changeOptionsKurus)
                assertTrue(visit.payOptionsKurus.all { it>0 })
                assertTrue(visit.changeOptionsKurus.all { it>=0 })
                assertTrue(visit.cashierPayKurus>=visit.requiredTotal())
                assertTrue(visit.requiredTotal() % 50 == 0)
            }
        }
    }

    @Test fun completionRequiresExactShoppingTotalAndChangeNoDoubleStars() {
        for(tier in LearningTier.entries) for(seed in 1L..100L) {
            var run=MarketAdventureRun(seed=seed,tier=tier,
                phase=MarketAdventurePhase.SHOP)
            repeat(3) {
                val scene=MarketAdventureEngine.scenario(run)!!
                for(line in scene.needed) {
                    repeat(line.quantity) {
                        run=MarketAdventureEngine.changeQuantity(run,line.productId,1)
                    }
                }
                assertTrue(MarketAdventureEngine.basketExact(run,scene))
                assertEquals(scene.requiredTotal(),MarketAdventureEngine.amount(run,scene))
                run=MarketAdventureEngine.checkBasket(run)
                assertEquals(MarketAdventurePhase.PAYMENT,run.phase)
                run=MarketAdventureEngine.choosePayment(run,scene.requiredTotal())
                assertEquals(MarketAdventurePhase.CHANGE,run.phase)
                assertEquals(scene.cashierPayKurus,run.paidKurus)
                run=MarketAdventureEngine.chooseChange(run,
                    scene.cashierPayKurus-scene.requiredTotal())
            }
            assertTrue(run.finished)
            assertEquals(3,run.visit)
            assertEquals(1,run.completedRuns)
            assertEquals(9,run.perfectDecisions)
            assertEquals(9,run.attempts)
            assertEquals(0,run.errors)
            assertEquals(run,MarketAdventureEngine.chooseChange(run,50))
            assertTrue(MarketAdventureEngine.valid(run))
        }
    }

    @Test fun wrongProductsWrongAmountsAndFakeTransactionsCannotAdvance() {
        val initial=MarketAdventureRun(seed=42,tier=LearningTier.TEMEL,
            phase=MarketAdventurePhase.SHOP)
        val scene=MarketAdventureEngine.scenario(initial)!!
        val decoy=scene.products.first { p ->
            scene.needed.none { it.productId==p.id }
        }
        var current=MarketAdventureEngine.changeQuantity(initial,decoy.id,1)
        val rejected=MarketAdventureEngine.checkBasket(current)
        assertEquals(MarketAdventurePhase.SHOP,rejected.phase)
        assertEquals(1,rejected.errors)
        assertEquals(0,rejected.perfectDecisions)
        current=MarketAdventureEngine.changeQuantity(rejected,decoy.id,-1)
        scene.needed.forEach { line ->
            repeat(line.quantity) {
                current=MarketAdventureEngine.changeQuantity(current,line.productId,1)
            }
        }
        current=MarketAdventureEngine.checkBasket(current)
        assertEquals(MarketAdventurePhase.PAYMENT,current.phase)
        val tooLow=scene.payOptionsKurus.first { it!=scene.requiredTotal() }
        val payWrong=MarketAdventureEngine.choosePayment(current,tooLow)
        assertEquals(MarketAdventurePhase.PAYMENT,payWrong.phase)
        assertEquals(2,payWrong.errors)
        assertEquals(0,payWrong.paidKurus)
        val confirmed=MarketAdventureEngine.choosePayment(payWrong,scene.requiredTotal())
        assertEquals(MarketAdventurePhase.CHANGE,confirmed.phase)
        val correct=scene.cashierPayKurus-scene.requiredTotal()
        val changeWrong=scene.changeOptionsKurus.first { it!=correct }
        val before=MarketAdventureEngine.chooseChange(confirmed,changeWrong)
        assertEquals(MarketAdventurePhase.CHANGE,before.phase)
        assertEquals(3,before.errors)
        assertTrue(MarketAdventureEngine.valid(before))
        assertFalse(MarketAdventureEngine.valid(before.copy(paidKurus=0)))
        assertFalse(MarketAdventureEngine.valid(initial.copy(
            basket=listOf(MarketLine(9999,3)))))
    }

    @Test fun exactCartAndBudgetNeverAllowAnExtraItemOrExcessQuantity() {
        val run=MarketAdventureRun(seed=317L,tier=LearningTier.USTALIK,
            phase=MarketAdventurePhase.SHOP)
        val scene=MarketAdventureEngine.scenario(run)!!
        var current=run
        scene.needed.forEach { line ->
            repeat(line.quantity) {
                current=MarketAdventureEngine.changeQuantity(current,line.productId,1)
            }
        }
        assertTrue(MarketAdventureEngine.basketExact(current,scene))
        val wrong=scene.products.first { p ->
            scene.needed.none { it.productId==p.id }
        }
        val extra=MarketAdventureEngine.changeQuantity(current,wrong.id,1)
        assertFalse(MarketAdventureEngine.basketExact(extra,scene))
        val first=scene.needed.first()
        val more=MarketAdventureEngine.changeQuantity(current,first.productId,1)
        assertFalse(MarketAdventureEngine.basketExact(more,scene))
    }

    @Test fun hintsLimitOncePerDecisionAndDoNotClaimIndependentSuccess() {
        var run=MarketAdventureRun(seed=9L,tier=LearningTier.TEMEL,
            phase=MarketAdventurePhase.SHOP)
        run=MarketAdventureEngine.hint(run)
        assertEquals(1,run.hints)
        assertEquals(run,MarketAdventureEngine.hint(run))
        val scene=MarketAdventureEngine.scenario(run)!!
        for(line in scene.needed) repeat(line.quantity) {
            run=MarketAdventureEngine.changeQuantity(run,line.productId,1)
        }
        run=MarketAdventureEngine.checkBasket(run)
        assertEquals(0,run.perfectDecisions)
        assertEquals(1,run.hints)
        assertFalse(run.taskHadHint)
        run=MarketAdventureEngine.choosePayment(run,scene.requiredTotal())
        assertEquals(1,run.perfectDecisions)
    }

    @Test fun currencyFormatterNeverUsesFloatingPoint() {
        assertEquals("0 TL",MarketAdventureEngine.format(0))
        assertEquals("13 TL",MarketAdventureEngine.format(1300))
        assertEquals("23,50 TL",MarketAdventureEngine.format(2350))
        assertEquals("0,50 TL",MarketAdventureEngine.format(50))
        assertEquals("0 TL",MarketAdventureEngine.format(-12))
    }
}
