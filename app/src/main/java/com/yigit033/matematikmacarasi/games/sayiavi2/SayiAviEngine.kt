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


    /** Fiziksel üçerli grup oluşturma; her koşuda değişen 2–4 grup. */
    data class ShellGrouping(val groups: Int, val shellsPerGroup: Int, val choices: List<Int>) {
        val total: Int get() = groups * shellsPerGroup
        init {
            require(groups in 2..4 && shellsPerGroup == 3)
            require(choices.size == 4 && choices.distinct().size == 4 && total in choices)
        }
    }

    fun generateShellGrouping(seed: Long): ShellGrouping {
        val rng = Random(seed xor 0x2E42L)
        val groups = rng.nextInt(2, 5)
        val total = groups * 3
        return ShellGrouping(groups, 3,
            listOf(total, total - 3, total + 3, total + 1).shuffled(rng))
    }

    enum class TransferKind { TOTAL, EQUAL_GROUPS, ADD_ONE_GROUP }
    data class TransferChallenge(
        val kind: TransferKind,
        val prompt: String,
        val correct: Int,
        val choices: List<Int>,
        val hint: String,
        val explanation: String
    ) {
        init { require(correct > 0 && choices.size == 4 && choices.distinct().size == 4 && correct in choices) }
    }

    /** Nesne gruplamasını kasa, malzeme ve ikmal problemlerine aktarır. */
    fun generateTransferChallenges(seed: Long): List<TransferChallenge> {
        val rng = Random(seed xor 0x37F21L)
        val bagCount = rng.nextInt(2, 5)
        val shellCount = rng.nextInt(3, 6)
        val lanternCount = rng.nextInt(2, 5)
        fun choices(correct: Int): List<Int> {
            val alternatives = linkedSetOf(correct, correct + 3, correct + 1, maxOf(1, correct - 3))
            var next = correct + 2
            while (alternatives.size < 4) alternatives.add(next++)
            return alternatives.toList().shuffled(rng)
        }
        return listOf(
            TransferChallenge(TransferKind.TOTAL,
                "Her kesede 3 pusula var. $bagCount kese kaç pusula eder?",
                bagCount * 3, choices(bagCount * 3),
                "Her kesenin 3 pusulasını birlikte say: 3, 6, 9...",
                "$bagCount kese × 3 pusula = ${bagCount * 3} pusula."),
            TransferChallenge(TransferKind.EQUAL_GROUPS,
                "$shellCount üçerli paket için kaç kabuk gerekir?",
                shellCount * 3, choices(shellCount * 3),
                "Bir pakette 3 kabuk var. Her paket için 3 daha ekle.",
                "$shellCount paket için ${shellCount * 3} kabuk gerekir."),
            TransferChallenge(TransferKind.ADD_ONE_GROUP,
                "$lanternCount kutuda üçer fener var. Bir kutu daha gelirse toplam kaç fener olur?",
                (lanternCount + 1) * 3, choices((lanternCount + 1) * 3),
                "Önce kutu sayısına 1 ekle. Sonra üçer say.",
                "${lanternCount + 1} kutunun her birinde 3 fener: ${(lanternCount + 1) * 3}.")
        )
    }

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
            require(targetSequence.size == 4 && targetSequence.zipWithNext().all { (a, b) -> b - a == 3 })
            require(nodes.size == 8 && nodes.map { it.value }.distinct().size == 8)
            require(nodes.filter { it.isTarget }.sortedBy { it.targetStepIndex }.map { it.value } == targetSequence)
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
            attemptIndex == 0 -> "İpucu: Taşları üçer ritmik sayarak kontrol et."
            else -> "İpucu: Üçün katlarını bulmak için sayıların 3'e kalansız bölünüp bölünmediğini kontrol et."
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
        val startMultiplier = rng.nextInt(1, 9) // 1..8, her seferinde +3
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
        val rng = if (seed != null) Random(seed) else Random.Default
        val start = if (seed == null || seed == 0L) 6 else listOf(3, 6, 9, 12, 15).random(rng)
        val targets = (0..3).map { start + 3 * it }
        val distractors = if (seed == null || seed == 0L) listOf(8, 10, 14, 17)
            else (2..(start + 20)).filter { it !in targets }.shuffled(rng).take(4)
        val values = listOf(targets[0], distractors[0], targets[1], distractors[1],
            targets[2], distractors[2], targets[3], distractors[3])
        val xy = listOf(0.22f to 0.78f, 0.68f to 0.74f, 0.38f to 0.58f, 0.78f to 0.52f,
            0.52f to 0.38f, 0.24f to 0.34f, 0.74f to 0.20f, 0.35f to 0.16f)
        val nodes = values.mapIndexed { i, value ->
            RouteNode(i + 1, value, i % 2 == 0, if (i % 2 == 0) i / 2 else -1,
                xy[i].first, xy[i].second)
        }
        return StageCRouteData(nodes, targets)
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
            val msg = if (currentStepIndex == 0) {
                "Haritada başlangıç sayısını bul!"
            } else {
                "Doğru sayıdan 3 ilerleyerek yeniden dene."
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

