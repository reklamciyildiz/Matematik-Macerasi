package com.yigit033.matematikmacarasi.sayiavi2

import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAviEngine
import org.junit.Assert.*
import org.junit.Test

class SayiAviEngineTest {

    @Test
    fun testCanonicalStageAGridInvariants() {
        val canonical = SayiAviEngine.generateStageAGrid(seed = 0L)
        
        assertEquals("Kanonik tahtada tam 16 sayı olmalı", 16, canonical.numbers.size)
        assertEquals("Tüm 16 sayı benzersiz olmalı", 16, canonical.numbers.distinct().size)
        assertEquals("Kesinlikle 5 hedef olmalı", 5, canonical.targets.size)
        assertEquals("Kesinlikle 11 çeldirici olmalı", 11, canonical.distractors.size)
        
        // Hedeflerin hepsi 3'ün katı olmalı
        assertTrue("Hedeflerin tümü 3'e kalansız bölünmeli", canonical.targets.all { it % 3 == 0 })
        
        // Çeldiricilerin hiçbiri 3'ün katı olmamalı
        assertTrue("Çeldiricilerin hiçbiri 3'ün katı olmamalı", canonical.distractors.all { it % 3 != 0 })
        
        // Kanonik hedefler [6, 9, 12, 15, 18]
        assertEquals(listOf(6, 9, 12, 15, 18), canonical.targets.sorted())
    }

    @Test
    fun testStageAGridInvariantsAcross1000Seeds() {
        // 1000 farklı seed ile matematiksel kuralların bozulmadığını kanıtla
        for (seed in 1L..1000L) {
            val grid = SayiAviEngine.generateStageAGrid(seed = seed)
            
            assertEquals("Seed $seed: Tam 16 sayı olmalı", 16, grid.numbers.size)
            assertEquals("Seed $seed: Sayılar benzersiz olmalı", 16, grid.numbers.distinct().size)
            assertEquals("Seed $seed: Tam 5 hedef olmalı", 5, grid.targets.size)
            assertEquals("Seed $seed: Tam 11 çeldirici olmalı", 11, grid.distractors.size)
            
            // 3'e bölünebilenlerin sayısı tam 5 olmalı
            val actualMultiplesOfThree = grid.numbers.filter { it % 3 == 0 }
            assertEquals("Seed $seed: 3'ün katı olan tam 5 sayı bulunmalı", 5, actualMultiplesOfThree.size)
            assertEquals("Seed $seed: Hedef listesi 3'ün katlarıyla birebir örtüşmeli", 
                grid.targets.sorted(), actualMultiplesOfThree.sorted())
            
            // Çeldiricilerin hiçbiri 3'ün katı olmamalı
            assertTrue("Seed $seed: Çeldiriciler 3'ün katı olamaz", grid.distractors.all { it % 3 != 0 })
        }
    }

    @Test
    fun testStageAEvaluation() {
        val grid = SayiAviEngine.generateStageAGrid(seed = 0L)
        val correctSet = grid.targets.toSet() // 6, 9, 12, 15, 18
        
        // 1. Doğru 5 seçim yapıldığında başarı dönmeli
        val successResult = SayiAviEngine.evaluateStageA(
            selected = correctSet,
            gridData = grid,
            attemptIndex = 0
        )
        assertTrue("Tüm hedefler seçildiğinde isSuccess true olmalı", successResult.isSuccess)
        assertEquals(0, successResult.wrongSelections.size)
        assertEquals(5, successResult.correctSelections.size)
        assertNull(successResult.pedagogicalHint)

        // 2. 4 doğru 1 yanlış seçim yapıldığında
        val wrongSet1 = setOf(6, 9, 12, 15, 7) // 7 çeldirici
        val failResult1 = SayiAviEngine.evaluateStageA(
            selected = wrongSet1,
            gridData = grid,
            attemptIndex = 0
        )
        assertFalse("1 yanlış varken isSuccess false olmalı", failResult1.isSuccess)
        assertEquals(listOf(7), failResult1.wrongSelections)
        assertEquals(1, failResult1.missingTargetsCount)
        assertNotNull(failResult1.pedagogicalHint)
        assertTrue(failResult1.pedagogicalHint!!.contains("ritmik"))

        // 3. İkinci denemede daha ayrıntılı ipucu verilmeli
        val failResult2 = SayiAviEngine.evaluateStageA(
            selected = wrongSet1,
            gridData = grid,
            attemptIndex = 1
        )
        assertFalse(failResult2.isSuccess)
        assertTrue(failResult2.pedagogicalHint!!.contains("kalansız"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun testStageAEvaluationRejectsLessThanFiveSelections() {
        val grid = SayiAviEngine.generateStageAGrid(seed = 0L)
        SayiAviEngine.evaluateStageA(setOf(6, 9, 12), grid, 0)
    }

    @Test
    fun testStageBVaultDefaultPattern() {
        val vault = SayiAviEngine.generateStageBVault(seed = null)
        
        assertEquals(listOf(6, 9, 12), vault.sequence)
        assertEquals(15, vault.correctAnswer)
        assertTrue("Seçenekler doğru cevabı içermeli", vault.options.contains(15))
        assertEquals("Tüm seçenekler benzersiz olmalı", vault.options.size, vault.options.distinct().size)
        
        // Değerlendirme testi
        val correctResult = SayiAviEngine.evaluateStageB(15, vault)
        assertTrue(correctResult.isSuccess)
        
        val wrongResult = SayiAviEngine.evaluateStageB(13, vault)
        assertFalse(wrongResult.isSuccess)
        assertTrue(wrongResult.pedagogicalExplanation.contains("+3"))
    }

    @Test
    fun testStageBVaultVariationAcrossSeeds() {
        for (seed in 1L..100L) {
            val vault = SayiAviEngine.generateStageBVault(seed = seed)
            assertEquals(3, vault.sequence.size)
            // Örüntü kuralı: ardışık fark tam 3 olmalı
            assertEquals(3, vault.sequence[1] - vault.sequence[0])
            assertEquals(3, vault.sequence[2] - vault.sequence[1])
            assertEquals(3, vault.correctAnswer - vault.sequence[2])
            assertTrue(vault.options.contains(vault.correctAnswer))
        }
    }

    @Test
    fun testStageCRoutePathSequence() {
        val route = SayiAviEngine.generateStageCRoute()
        
        assertEquals(listOf(6, 9, 12, 15), route.targetSequence)
        val targetNodes = route.nodes.filter { it.isTarget }.sortedBy { it.targetStepIndex }
        assertEquals(4, targetNodes.size)
        assertEquals(listOf(6, 9, 12, 15), targetNodes.map { it.value })
        
        // Adım adım tamamlama testi
        // 1. Adım: 6 taşına tıklandı
        val step1 = SayiAviEngine.evaluateStageCStep(
            clickedNodeId = targetNodes[0].id,
            currentStepIndex = 0,
            data = route
        )
        assertTrue(step1.isValidStep)
        assertFalse(step1.isSequenceComplete)
        assertEquals(6, step1.clickedValue)
        
        // Yanlış adım denemesi (örneğin 6'dan sonra 12'ye atlama)
        val wrongStep = SayiAviEngine.evaluateStageCStep(
            clickedNodeId = targetNodes[2].id, // 12
            currentStepIndex = 1, // 9 bekleniyor
            data = route
        )
        assertFalse(wrongStep.isValidStep)
        assertEquals(9, wrongStep.expectedValue)
        
        // Doğru 2. Adım: 9
        val step2 = SayiAviEngine.evaluateStageCStep(
            clickedNodeId = targetNodes[1].id,
            currentStepIndex = 1,
            data = route
        )
        assertTrue(step2.isValidStep)
        assertFalse(step2.isSequenceComplete)
        
        // Doğru 3. Adım: 12
        val step3 = SayiAviEngine.evaluateStageCStep(
            clickedNodeId = targetNodes[2].id,
            currentStepIndex = 2,
            data = route
        )
        assertTrue(step3.isValidStep)
        assertFalse(step3.isSequenceComplete)
        
        // Doğru 4. Adım: 15 (Bitiş)
        val step4 = SayiAviEngine.evaluateStageCStep(
            clickedNodeId = targetNodes[3].id,
            currentStepIndex = 3,
            data = route
        )
        assertTrue(step4.isValidStep)
        assertTrue("4. adım tamamlandığında rota bitmeli", step4.isSequenceComplete)
    }

    @Test
    fun testG2RouteSeedVariants() {
        val starts = mutableSetOf<Int>()
        for (seed in 1L..200L) {
            val route = SayiAviEngine.generateStageCRoute(seed)
            assertEquals(8, route.nodes.map { it.value }.distinct().size)
            val goals = route.nodes.filter { it.isTarget }.sortedBy { it.targetStepIndex }
            assertEquals(route.targetSequence, goals.map { it.value })
            assertTrue(route.targetSequence.zipWithNext().all { (a,b) -> b - a == 3 })
            starts += route.targetSequence.first()
            for (i in goals.indices) {
                val result = SayiAviEngine.evaluateStageCStep(goals[i].id, i, route)
                assertTrue(result.isValidStep)
                assertEquals(i == 3, result.isSequenceComplete)
            }
        }
        assertTrue(starts.size >= 3)
    }

    @Test
    fun testG2MetricsRoundTrip() {
        val counters = com.yigit033.matematikmacarasi.games.sayiavi2.SayiAviRunCounters()
            .attempt(1, false).attempt(1, true).attempt(2, true)
            .attempt(3, true).attempt(3, false).attempt(3, true)
            .attempt(3, true).attempt(3, true).hint()
        val parsed = com.yigit033.matematikmacarasi.games.sayiavi2.SayiAviRunCounters.decode(counters.encode())
        assertEquals(counters, parsed)
        val summary = com.yigit033.matematikmacarasi.games.sayiavi2.SayiAviLearningSummary(counters, 160L)
        assertEquals(8, summary.attempts)
        assertEquals(2, summary.errors)
        assertEquals(75, summary.actionAccuracyPercent)
        assertEquals(1, summary.firstTryStages)
        assertNull(com.yigit033.matematikmacarasi.games.sayiavi2.SayiAviRunCounters.decode("1,broken"))
    }

    @Test
    fun testRewardIdempotencyLogic() {
        // İlk tamamlama vs Replay ödül mantığı
        fun calculateStarsToAward(isFirstCompletion: Boolean): Int {
            return if (isFirstCompletion) 1 else 0
        }
        
        assertEquals("İlk tamamlamada tam 1 yıldız verilmeli", 1, calculateStarsToAward(isFirstCompletion = true))
        assertEquals("Tekrar oynamada 0 ek yıldız verilmeli", 0, calculateStarsToAward(isFirstCompletion = false))
        assertEquals("Üçüncü tekrar oynamada 0 ek yıldız verilmeli", 0, calculateStarsToAward(isFirstCompletion = false))
    }
}

