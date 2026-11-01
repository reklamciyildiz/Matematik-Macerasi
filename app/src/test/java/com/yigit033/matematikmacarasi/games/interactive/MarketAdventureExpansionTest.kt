package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarketAdventureExpansionTest {
    @Test fun premiumSixStopsOfferRealBudgetAndArithmeticChoices() {
        for(tier in LearningTier.entries) for(seed in 1L..300L) {
            val stages=MarketAdventureEngine.scenarios(seed,tier,contentVersion=2)
            assertEquals(6,stages.size)
            assertEquals(6,stages.map { it.location }.distinct().size)
            assertEquals(stages,MarketAdventureEngine.scenarios(seed,tier,contentVersion=2))
            assertEquals(listOf(MarketBonusKind.WEIGHT,MarketBonusKind.VOLUME,
                MarketBonusKind.SAVINGS,MarketBonusKind.DISCOUNT,
                MarketBonusKind.COMPARE,MarketBonusKind.PROFIT),
                stages.map { it.bonus?.kind })
            stages.forEach { stage ->
                assertTrue(stage.products.size==4)
                assertTrue(stage.products.all { it.priceKurus>0 && it.unit.isNotBlank() })
                assertTrue(stage.needed.all { it.quantity in 1..2 })
                assertEquals(4,stage.payOptionsKurus.distinct().size)
                assertEquals(4,stage.changeOptionsKurus.distinct().size)
                assertEquals(stage.requiredTotal(),
                    stage.payOptionsKurus.first { it==stage.requiredTotal() })
                val bonus=stage.bonus!!
                assertTrue(bonus.correctKurus in bonus.choicesKurus)
                assertEquals(4,bonus.choicesKurus.distinct().size)
                assertTrue(bonus.choicesKurus.all { it>=0 })
                assertTrue(stage.cashierPayKurus>stage.requiredTotal())
            }
        }
    }

    @Test fun allTwentyFourDecisionsAreActuallyRequiredToCompleteJourney() {
        for(tier in LearningTier.entries) for(seed in 1L..100L) {
            var run=MarketAdventureRun(seed=seed,tier=tier,
                phase=MarketAdventurePhase.SHOP,contentVersion=2)
            repeat(6) { visit ->
                assertEquals(visit,run.visit)
                val scene=MarketAdventureEngine.scenario(run)!!
                assertEquals(4,scene.products.size)
                scene.needed.forEach { line ->
                    repeat(line.quantity) {
                        run=MarketAdventureEngine.changeQuantity(run,line.productId,1)
                    }
                }
                assertTrue(MarketAdventureEngine.basketExact(run,scene))
                run=MarketAdventureEngine.checkBasket(run)
                assertEquals(MarketAdventurePhase.PAYMENT,run.phase)
                run=MarketAdventureEngine.choosePayment(run,scene.requiredTotal())
                assertEquals(MarketAdventurePhase.CHANGE,run.phase)
                run=MarketAdventureEngine.chooseChange(run,
                    scene.cashierPayKurus-scene.requiredTotal())
                assertEquals(MarketAdventurePhase.BONUS,run.phase)
                run=MarketAdventureEngine.chooseBonus(run,scene.bonus!!.correctKurus)
                assertTrue(MarketAdventureEngine.valid(run))
            }
            assertTrue(run.finished)
            assertEquals(6,run.visit)
            assertEquals(1,run.completedRuns)
            assertEquals(24,run.attempts)
            assertEquals(24,run.perfectDecisions)
            assertEquals(0,run.errors)
            assertEquals(3,MarketAdventureEngine.starsFor(run))
            assertEquals(run,MarketAdventureEngine.chooseBonus(run,100))
            assertEquals(run,MarketAdventureEngine.chooseChange(run,100))
        }
    }

    @Test fun bonusMistakesAndHintsDoNotAwardIndependentSuccess() {
        var run=MarketAdventureRun(seed=91L,tier=LearningTier.USTALIK,
            phase=MarketAdventurePhase.SHOP,visit=3,contentVersion=2)
        val scene=MarketAdventureEngine.scenario(run)!!
        scene.needed.forEach { line ->
            repeat(line.quantity) {
                run=MarketAdventureEngine.changeQuantity(run,line.productId,1)
            }
        }
        run=MarketAdventureEngine.checkBasket(run)
        run=MarketAdventureEngine.choosePayment(run,scene.requiredTotal())
        run=MarketAdventureEngine.chooseChange(run,
            scene.cashierPayKurus-scene.requiredTotal())
        assertEquals(MarketAdventurePhase.BONUS,run.phase)
        val bonus=scene.bonus!!
        val wrong=bonus.choicesKurus.first { it!=bonus.correctKurus }
        val bad=MarketAdventureEngine.chooseBonus(run,wrong)
        assertEquals(MarketAdventurePhase.BONUS,bad.phase)
        assertEquals(1,bad.errors)
        val withHint=MarketAdventureEngine.hint(bad)
        assertEquals(1,withHint.hints)
        assertEquals(withHint,MarketAdventureEngine.hint(withHint))
        val solved=MarketAdventureEngine.chooseBonus(withHint,bonus.correctKurus)
        assertEquals(4,solved.visit)
        assertEquals(3,solved.perfectDecisions)
        assertEquals(1,solved.errors)
        assertEquals(1,solved.hints)
        assertFalse(solved.taskHadHint)
        assertFalse(solved.taskHadError)
    }

    @Test fun olderCheckpointsStillPlayExactlyThreeStopsAndEarnValidStars() {
        for(tier in LearningTier.entries) {
            val a=MarketAdventureEngine.scenarios(55L,tier)
            val b=MarketAdventureEngine.scenarios(55L,tier,contentVersion=1)
            assertEquals(a,b)
            assertEquals(3,a.size)
            val state=MarketAdventureRun(seed=55L,tier=tier,
                phase=MarketAdventurePhase.SHOP)
            assertEquals(3,MarketAdventureEngine.visitCount(state))
            assertEquals(9,MarketAdventureEngine.decisionCount(state))
            assertTrue(MarketAdventureEngine.valid(state))
            assertFalse(MarketAdventureEngine.valid(state.copy(
                phase=MarketAdventurePhase.BONUS)))
        }
        val full=MarketAdventureRun(seed=5L,visit=3,
            phase=MarketAdventurePhase.COMPLETE,perfectDecisions=9)
        assertTrue(MarketAdventureEngine.valid(full))
        assertEquals(3,MarketAdventureEngine.starsFor(full))
    }

    @Test fun tierSpecificMoneyAndUnitsAreCoherent() {
        val basic=MarketAdventureEngine.scenarios(82L,LearningTier.TEMEL,2)
        val master=MarketAdventureEngine.scenarios(82L,LearningTier.USTALIK,2)
        assertTrue(basic[0].products.all { it.unit=="kg" })
        assertTrue(master[0].products.all { it.unit=="kg" })
        assertTrue(master[5].products.all { it.unit in listOf("adet","kg","litre") })
        assertTrue(basic[0].products.none { it.priceKurus%100!=0 })
        assertTrue(master.any { stage -> stage.products.any { it.priceKurus%100!=0 } })
        assertEquals("23,50 TL",MarketAdventureEngine.format(2350))
    }

    @Test fun ageAppropriateProblemsAvoidPrematurePercentagesAndFractionalMass() {
        val basic=MarketAdventureEngine.scenarios(47L,LearningTier.TEMEL,2)
        val intermediate=MarketAdventureEngine.scenarios(47L,LearningTier.GELISEN,2)
        val advanced=MarketAdventureEngine.scenarios(47L,LearningTier.USTALIK,2)
        assertTrue(basic[0].bonus!!.prompt.contains("2 kg"))
        assertFalse(basic[0].bonus!!.prompt.contains("500 gram"))
        assertTrue(intermediate[0].bonus!!.prompt.contains("500 gram"))
        assertTrue(advanced[0].bonus!!.prompt.contains("1 kg 500 gram"))
        assertFalse(basic[3].bonus!!.prompt.contains("%10"))
        assertFalse(intermediate[3].bonus!!.prompt.contains("%10"))
        assertTrue(advanced[3].bonus!!.prompt.contains("%10"))
        for(stages in listOf(basic,intermediate,advanced)) {
            assertEquals(24,MarketAdventureEngine.decisionCount(
                MarketAdventureRun(contentVersion=2)))
            assertTrue(stages[2].bonus!!.correctKurus>0)
            assertTrue(stages[4].bonus!!.correctKurus>0)
            assertTrue(stages[5].bonus!!.correctKurus>0)
        }
    }

    @Test fun cashierBonusRequiresAnActuallyValidatedPayment() {
        val run=MarketAdventureRun(seed=110L,tier=LearningTier.GELISEN,
            visit=3,phase=MarketAdventurePhase.SHOP,contentVersion=2)
        val scene=MarketAdventureEngine.scenario(run)!!
        var current=run
        scene.needed.forEach { line ->
            repeat(line.quantity) {
                current=MarketAdventureEngine.changeQuantity(current,line.productId,1)
            }
        }
        current=MarketAdventureEngine.checkBasket(current)
        current=MarketAdventureEngine.choosePayment(current,scene.requiredTotal())
        current=MarketAdventureEngine.chooseChange(current,
            scene.cashierPayKurus-scene.requiredTotal())
        assertEquals(MarketAdventurePhase.BONUS,current.phase)
        assertTrue(MarketAdventureEngine.valid(current))
        assertFalse(MarketAdventureEngine.valid(current.copy(paidKurus=0)))
        assertFalse(MarketAdventureEngine.valid(current.copy(
            paidKurus=scene.cashierPayKurus-100)))
        assertFalse(MarketAdventureEngine.valid(current.copy(
            phase=MarketAdventurePhase.PAYMENT)))
    }

    @Test fun noBonusCanBeSkippedOrForged() {
        val run=MarketAdventureRun(seed=9L,tier=LearningTier.GELISEN,
            visit=5,phase=MarketAdventurePhase.SHOP,contentVersion=2)
        assertEquals(run,MarketAdventureEngine.chooseBonus(run,0))
        assertFalse(MarketAdventureEngine.valid(run.copy(
            visit=6,phase=MarketAdventurePhase.SHOP)))
        assertFalse(MarketAdventureEngine.valid(run.copy(
            visit=4,phase=MarketAdventurePhase.COMPLETE)))
        val wrong=run.copy(basket=listOf(MarketLine(99999,2)))
        assertFalse(MarketAdventureEngine.valid(wrong))
    }
}
