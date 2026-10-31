package com.yigit033.matematikmacarasi.games.sayiavi2

import kotlin.random.Random

/**
 * Sayı Avı 2.0 Matematik ve Seviye Motoru.
 * GDD v1.0 ve VSCODE_SPRINT_G1_PROMPT kurallarına göre:
 * - Stage A: 4x4 tahta, tam 16 benzersiz sayı, KESİN 5 adet 3'ün katı ve 11 adet çeldirici (asla 3'ün katı değil).
 * - Stage B: 6, 9, 12, [?] örüntüsü -> doğru cevap 15.
 * - Stage C: 6 -> 9 -> 12 -> 15 sıralı hazine rotası adımları.
 *
 * %100 saf Kotlin, Android framework bağımlılığı içermez, birim testlerde deterministik seed ile test edilebilir.
 */
object SayiAviEngine {

    val CANONICAL_STAGE_A_NUMBERS = listOf(7, 12, 4, 15, 8, 11, 6, 17, 13, 18, 10, 5, 9, 14, 16, 19)
    val CANONICAL_STAGE_A_TARGETS = listOf(6, 9, 12, 15, 18)

    data class StageAGridData(
        val numbers: List<Int>,
        val targets: List<Int>,
        val distractors: List<Int>
    ) {
        init {
            require(numbers.size == 16) { "Tahtada tam 16 sayı olmalı, mevcut: ${numbers.size}" }
            require(numbers.distinct().size == 16) { "Tüm 16 sayı benzersiz olmalı!" }
            require(targets.size == 5) { "Kesinlikle 5 hedef sayı olmalı, mevcut: ${targets.size}" }
            require(targets.all { it % 3 == 0 }) { "Hedeflerin tümü 3'ün katı olmalı!" }
            require(distractors.size == 11) { "Kesinlikle 11 çeldirici olmalı, mevcut: ${distractors.size}" }
            require(distractors.all { it % 3 != 0 }) { "Çeldiricilerin hiçbiri 3'ün katı OLMAMALIDIR!" }
        }
    }

    data class StageAResult(
        val isSuccess: Boolean,
        val correctSelections: List<Int>,
        val wrongSelections: List<Int>,
        val missingTargetsCount: Int,
        val feedbackTitle: String,
        val feedbackMessage: String,
        val pedagogicalHint: String?
    )

    data class StageBVaultData(
        val sequence: List<Int>,
        val correctAnswer: Int,
        val options: List<Int>,
        val ruleDescription: String
    ) {
        init {
            require(sequence.size == 3) { "Dizilimde 3 öncül sayı olmalı" }
            require(options.contains(correctAnswer)) { "Seçenekler doğru cevabı içermeli" }
            require(options.distinct().size == options.size) { "Seçenekler benzersiz olmalı" }
        }
    }

    data class StageBResult(
        val isSuccess: Boolean,
        val selectedAnswer: Int,
        val correctAnswer: Int,
        val feedbackTitle: String,
        val feedbackMessage: String,
        val pedagogicalExplanation: String
    )

    data class RouteNode(
        val id: Int,
        val value: Int,
        val isTarget: Boolean,
        val targetStepIndex: Int, // 0 for 6, 1 for 9, 2 for 12, 3 for 15, or -1 for distractors
        val xPercent: Float,      // 0.0f..1.0f relative position across backdrop
        val yPercent: Float       // 0.0f..1.0f
    )

    data class StageCRouteData(
        val nodes: List<RouteNode>,
        val targetSequence: List<Int>
    ) {
        init {
            require(targetSequence == listOf(6, 9, 12, 15)) { "Hedef sıra [6, 9, 12, 15] olmalıdır" }
            require(nodes.count { it.isTarget } == 4) { "Tam 4 adet hedef ada bulunmalıdır" }
        }
    }

    data class StageCStepResult(
        val isSequenceComplete: Boolean,
        val isValidStep: Boolean,
        val expectedValue: Int,
        val clickedValue: Int,
        val feedbackMessage: String
    )

    data class LearningMetrics(
        val attemptsStageA: Int = 0,
        val errorsStageA: Int = 0,
        val hintsUsedStageA: Int = 0,
        val attemptsStageB: Int = 0,
        val attemptsStageC: Int = 0,
        val totalTimeSeconds: Long = 0,
        val isFirstCompletion: Boolean = true
    )

    /**
     * Stage A 4x4 Grid Üretimi:
     * - seed verilirse deterministik üretir.
     * - seed == 0L ise GDD'de belirtilen kanonik listeyi döndürür.
     */
    fun generateStageAGrid(seed: Long? = null): StageAGridData {
        if (seed == 0L) {
            val targets = CANONICAL_STAGE_A_TARGETS
            val distractors = CANONICAL_STAGE_A_NUMBERS.filter { it % 3 != 0 }
            return StageAGridData(
                numbers = CANONICAL_STAGE_A_NUMBERS,
                targets = targets,
                distractors = distractors
            )
        }

        val rng = if (seed != null) Random(seed) else Random.Default

        // 3'ün katı havuzu: 3, 6, 9, 12, 15, 18, 21, 24, 27, 30, 33, 36
        val targetPool = (1..12).map { it * 3 }.shuffled(rng)
        val selectedTargets = targetPool.take(5).sorted()

        // Çeldirici havuzu: 1..35 arası ve 3'e bölünmeyen sayılar
        val distractorPool = (1..35).filter { it % 3 != 0 }.shuffled(rng)
        val selectedDistractors = distractorPool.take(11)

        val combinedNumbers = (selectedTargets + selectedDistractors).shuffled(rng)

        return StageAGridData(
            numbers = combinedNumbers,
            targets = selectedTargets,
            distractors = selectedDistractors
        )
    }

    /**
     * Stage A Seçim Değerlendirmesi:
     * - Kullanıcı tam 5 taş seçtiğinde çağrılır.
     */
    fun evaluateStageA(
        selected: Set<Int>,
        gridData: StageAGridData,
        attemptIndex: Int
    ): StageAResult {
        require(selected.size == 5) { "Kontrol için tam 5 taş seçilmelidir (Seçilen: ${selected.size})" }

        val targetSet = gridData.targets.toSet()
        val correctSelections = selected.filter { it in targetSet }
        val wrongSelections = selected.filter { it !in targetSet }
        val isSuccess = wrongSelections.isEmpty() && correctSelections.size == 5
        val missingCount = 5 - correctSelections.size

        val hint = when {
            isSuccess -> null
            attemptIndex == 0 -> "İpucu: 3'er 3'er saymayı hatırla: 3, 6, 9, 12, 15, 18..."
            else -> "Pedagojik İpucu: 3'ün katları cetveli: 3, 6, 9, 12, 15, 18, 21, 24. Bir sayının 3'ün katı olması için 3'e kalansız bölünmesi gerekir!"
        }

        val title = if (isSuccess) "Harika Keşif!" else "Tekrar Kontrol Et"
        val message = if (isSuccess) {
            "Tebrikler! 3'ün katı olan 5 gizli sayıyı buldun ve 1. Harita Parçasını kazandın!"
        } else {
            "Seçtiğin taşlardan ${wrongSelections.size} tanesi 3'ün katı değil. Hatalı taşları kaldırıp yenilerini seçebilirsin."
        }

        return StageAResult(
            isSuccess = isSuccess,
            correctSelections = correctSelections,
            wrongSelections = wrongSelections,
            missingTargetsCount = missingCount,
            feedbackTitle = title,
            feedbackMessage = message,
            pedagogicalHint = hint
        )
    }

    /**
     * Stage B Şifre Kasası Üretimi:
     * - Örüntü: start, start+3, start+6, [?]
     * - Default (seed == null veya 0): 6, 9, 12, ? -> 15.
     */
    fun generateStageBVault(seed: Long? = null): StageBVaultData {
        if (seed == null || seed == 0L) {
            return StageBVaultData(
                sequence = listOf(6, 9, 12),
                correctAnswer = 15,
                options = listOf(13, 14, 15, 18),
                ruleDescription = "Sayılar 3'er artarak ilerliyor: 6, 9, 12, [ ? ]"
            )
        }

        val rng = Random(seed)
        // Başlangıç sayısı: 3'ün katı olan bir sayı (3, 6, 9)
        val startMultiplier = rng.nextInt(1, 4) // 1, 2 veya 3
        val start = startMultiplier * 3
        val seq = listOf(start, start + 3, start + 6)
        val correct = start + 9

        // Çeldiriciler: correct - 2, correct - 1, correct + 3
        val distractors = listOf(correct - 2, correct - 1, correct + 3).filter { it > 0 && it != correct }
        val options = (listOf(correct) + distractors.take(3)).shuffled(rng)

        return StageBVaultData(
            sequence = seq,
            correctAnswer = correct,
            options = options,
            ruleDescription = "Sayılar 3'er artarak ilerliyor: ${seq[0]}, ${seq[1]}, ${seq[2]}, [ ? ]"
        )
    }

    /**
     * Stage B Değerlendirme
     */
    fun evaluateStageB(selectedAnswer: Int, data: StageBVaultData): StageBResult {
        val isSuccess = selectedAnswer == data.correctAnswer
        val title = if (isSuccess) "Kasa Açıldı!" else "Şifre Yanlış"
        val message = if (isSuccess) {
            "Doğru şifre! Kasa açıldı ve 2. Harita Parçasını ele geçirdin!"
        } else {
            "Bu sayı örüntüye uymuyor. Farklara dikkat ederek tekrar dene."
        }
        val explanation = "Kural: Her adımda +3 ekleniyor. " +
                "${data.sequence[0]} (+3) = ${data.sequence[1]} (+3) = ${data.sequence[2]} (+3) = ${data.correctAnswer}"

        return StageBResult(
            isSuccess = isSuccess,
            selectedAnswer = selectedAnswer,
            correctAnswer = data.correctAnswer,
            feedbackTitle = title,
            feedbackMessage = message,
            pedagogicalExplanation = explanation
        )
    }

    /**
     * Stage C Hazine Rotası Üretimi:
     * - Hedef sıra: 6 -> 9 -> 12 -> 15.
     * - Distraktör taşlar: 8, 10, 14, 17.
     * - Doğal konumlandırma koordinatları (xPercent, yPercent).
     */
    fun generateStageCRoute(seed: Long? = null): StageCRouteData {
        val targetSequence = listOf(6, 9, 12, 15)

        // Ada taşlarının koordinatları (okyanus manzarası üzerinde doğal bir patika)
        // 6: Kumsal başlangıcı (sol alt)
        // 9: İlk mercan resifi (orta-sol)
        // 12: Kayalık ada (orta-sağ)
        // 15: Hazine adası girişi (üst sağ)
        val nodes = listOf(
            RouteNode(id = 1, value = 6, isTarget = true, targetStepIndex = 0, xPercent = 0.22f, yPercent = 0.78f),
            RouteNode(id = 2, value = 8, isTarget = false, targetStepIndex = -1, xPercent = 0.68f, yPercent = 0.74f),
            RouteNode(id = 3, value = 9, isTarget = true, targetStepIndex = 1, xPercent = 0.38f, yPercent = 0.58f),
            RouteNode(id = 4, value = 10, isTarget = false, targetStepIndex = -1, xPercent = 0.78f, yPercent = 0.52f),
            RouteNode(id = 5, value = 12, isTarget = true, targetStepIndex = 2, xPercent = 0.52f, yPercent = 0.38f),
            RouteNode(id = 6, value = 14, isTarget = false, targetStepIndex = -1, xPercent = 0.24f, yPercent = 0.34f),
            RouteNode(id = 7, value = 15, isTarget = true, targetStepIndex = 3, xPercent = 0.74f, yPercent = 0.20f),
            RouteNode(id = 8, value = 17, isTarget = false, targetStepIndex = -1, xPercent = 0.35f, yPercent = 0.16f)
        )

        return StageCRouteData(
            nodes = nodes,
            targetSequence = targetSequence
        )
    }

    /**
     * Stage C Adım Değerlendirme
     */
    fun evaluateStageCStep(
        clickedNodeId: Int,
        currentStepIndex: Int, // 0 for expecting 6, 1 for expecting 9, etc.
        data: StageCRouteData
    ): StageCStepResult {
        val node = data.nodes.find { it.id == clickedNodeId }
            ?: return StageCStepResult(false, false, 0, 0, "Bilinmeyen taş")

        val expectedValue = data.targetSequence.getOrElse(currentStepIndex) { -1 }

        if (node.isTarget && node.value == expectedValue) {
            val isComplete = (currentStepIndex + 1) == data.targetSequence.size
            val msg = if (isComplete) {
                "Hazineye ulaştın! Bütün adımlar başarıyla tamamlandı!"
            } else {
                "Doğru adım: ${node.value}! Sıradaki taş için 3 fazlasını ara."
            }
            return StageCStepResult(
                isSequenceComplete = isComplete,
                isValidStep = true,
                expectedValue = expectedValue,
                clickedValue = node.value,
                feedbackMessage = msg
            )
        } else {
            val msg = if (expectedValue == 6) {
                "Yolculuğa ilk adım olan 6 taşıyla başlamalısın!"
            } else {
                "Bu taş rotaya uymuyor. Sıradaki adım $expectedValue olmalı (+3 kuralı)!"
            }
            return StageCStepResult(
                isSequenceComplete = false,
                isValidStep = false,
                expectedValue = expectedValue,
                clickedValue = node.value,
                feedbackMessage = msg
            )
        }
    }
}

