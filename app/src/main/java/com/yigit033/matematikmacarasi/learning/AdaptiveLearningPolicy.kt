package com.yigit033.matematikmacarasi.learning

import kotlin.math.abs
import kotlin.math.max

/**
 * P1 — deterministic, local-only learning support.
 *
 * These signals are instructional hypotheses, NOT proof of a specific
 * misconception, measured mastery, or a validated psychometric difficulty.
 * All questions come from the existing grade-scoped, reviewed question banks.
 */
enum class PracticeBand { SUPPORTED, CORE, EXTENDED }

enum class PracticeFocus(val label: String, val nextStep: String) {
    COUNTING("Adım adım sayma", "Sayıları sırayla takip et; son adımı yeniden say."),
    GROUPING("Eş gruplar ve işlemler", "Nesneleri eş gruplara ayırıp işlemin her aşamasını kontrol et."),
    FRACTIONS("Eş parçalar ve kesirler", "Bütünün kaç eş parçaya ayrıldığını ve kaç parçanın alındığını incele."),
    PLACE_VALUE("Basamak değerleri", "Birlikleri, onlukları ve yüzlükleri ayrı ayrı göster."),
    SIGNS("İşaretler ve yön", "Sayı doğrusunda sıfırı bul; sağ ve sol yönlerini kontrol et."),
    ORDER("İşlem sırası", "Hangi işlemin önce yapılacağını belirle, sonra sırayla çöz."),
    GEOMETRY("Şekiller ve ölçme", "Görseldeki kenarları, açıları veya ölçüleri tek tek kontrol et."),
    PATTERN("Örüntü kuralı", "İki ardışık terim arasındaki değişimi karşılaştır."),
    DATA_READING("Grafik ve tablo okuma", "Sütunların değerlerini tek tek oku, önce büyüğü sonra küçüğü belirle."),
    ROUNDING("Onluklara yuvarlama", "İki komşu onluğu bul, birler basamağının 5 veya daha büyük olup olmadığına bak."),
    STEP("Son işlem adımı", "Son hamleyi acele etmeden yeniden hesapla."),
    GENERAL("Kavramı tekrar gözden geçirme", "Örnek modeldeki adımları incele ve yeni soruda tekrar dene.")
}

data class GuidedResponse(
    val focus: PracticeFocus,
    val heading: String,
    val firstStep: String,
    val nextStep: String
)

object AdaptiveLearningPolicy {
    /**
     * There is no capability claim in a band. It changes candidate number
     * size/complexity within the student's CURRENT grade and SAME lesson.
     * Always keep the original deterministic question for the CORE band.
     */
    fun initialBand(runs: Int, lastWrong: Int, lastHints: Int, lastConceptErrors: Int): PracticeBand =
        when {
            runs <= 0 -> PracticeBand.CORE
            lastWrong >= 2 || lastHints >= 2 || lastConceptErrors >= 1 -> PracticeBand.SUPPORTED
            lastWrong == 0 && lastHints == 0 && lastConceptErrors == 0 && runs >= 2 ->
                PracticeBand.EXTENDED
            else -> PracticeBand.CORE
        }

    /** Only change the NEXT question. A visible question never mutates mid-attempt. */
    fun nextBand(
        current: PracticeBand,
        wrongAttempts: Int,
        usedHint: Boolean,
        neededSupport: Boolean
    ): PracticeBand = when {
        wrongAttempts > 0 || usedHint || neededSupport -> PracticeBand.SUPPORTED
        current == PracticeBand.SUPPORTED -> PracticeBand.CORE
        current == PracticeBand.CORE -> PracticeBand.EXTENDED
        else -> PracticeBand.EXTENDED
    }

    /** Concept checks have text answers; their instructional focus is the lesson topic. */
    fun focusForLesson(lesson: Lesson): PracticeFocus {
        val id = lesson.id.lowercase()
        return when {
            "data" in id || "chart" in id -> PracticeFocus.DATA_READING
            "round" in id -> PracticeFocus.ROUNDING
            "inverse" in id -> PracticeFocus.COUNTING
            lesson.skill in listOf(Skill.COUNT_2, Skill.COUNT_3, Skill.COUNT_4) ||
                "pattern" in id || "count" in id -> PracticeFocus.PATTERN
            "fraction" in id || "quarter" in id || "half" in id ||
                "third" in id || "percent" in id || "rational" in id ||
                "decimal" in id || "tenths" in id -> PracticeFocus.FRACTIONS
            "place" in id || "hundred" in id -> PracticeFocus.PLACE_VALUE
            "signed" in id || "absolute" in id || "coordinate" in id ||
                "mirror" in id -> PracticeFocus.SIGNS
            "parenthes" in id || "two_step" in id || "equation" in id ||
                "linear" in id -> PracticeFocus.ORDER
            "geometry" in id || "shape" in id || "angle" in id ||
                "area" in id || "perimeter" in id || "volume" in id ||
                "triangle" in id -> PracticeFocus.GEOMETRY
            lesson.skill == Skill.MULTIPLY || lesson.skill == Skill.MULTIPLY_INTRO ||
                "share" in id || "ratio" in id || "proportion" in id ||
                "lcm" in id || "divide" in id -> PracticeFocus.GROUPING
            lesson.skill == Skill.ADD_10 || lesson.skill == Skill.ADD_20 ||
                "add" in id || "takeaway" in id -> PracticeFocus.COUNTING
            else -> PracticeFocus.GENERAL
        }
    }

    fun classify(lesson: Lesson, question: PracticeQuestion, selected: Int): PracticeFocus {
        require(selected != question.answer) { "Only incorrect choices should be classified" }
        if (question.answer != 0 && selected == -question.answer) return PracticeFocus.SIGNS
        if (abs(selected.toLong() - question.answer.toLong()) == 1L) return PracticeFocus.STEP
        return focusForLesson(lesson)
    }

    fun response(
        lesson: Lesson, question: PracticeQuestion, selected: Int
    ): GuidedResponse {
        val focus = classify(lesson, question, selected)
        val comparison = when {
            selected < question.answer -> "Seçtiğin sayı beklenen sonuçtan küçük. İşlemin hangi adımında eksiltmiş olabileceğini incele."
            else -> "Seçtiğin sayı beklenen sonuçtan büyük. Sayarken veya işlem yaparken fazladan ekleme olup olmadığını incele."
        }
        // This is a hypothesis about the selected NUMBER, not a diagnosis.
        return GuidedResponse(
            focus = focus,
            heading = "Birlikte yeniden düşünelim: " + focus.label,
            firstStep = focus.nextStep,
            nextStep = comparison
        )
    }

    private val numbers = Regex("-?\\d+")

    /**
     * Approximate, deterministic numerical complexity: number magnitude,
     * count of mathematical operations and size of the result. It does not
     * claim a psychometrically harder lesson. Selection keeps mathematical
     * correctness inside the existing question banks.
     */
    internal fun complexity(question: PracticeQuestion): Long {
        val values = numbers.findAll(question.text).mapNotNull {
            it.value.toLongOrNull()?.let { n -> if (n == Long.MIN_VALUE) Long.MAX_VALUE else abs(n) }
        }.toList()
        val magnitude = values.fold(0L) { sum, n -> (sum + n.coerceAtMost(100_000)).coerceAtMost(1_000_000) }
        val operations = question.text.count { it in "+−-×÷*/√" }
        return magnitude + abs(question.answer.toLong()).coerceAtMost(100_000) +
            operations * 25L + values.count { it >= 10 } * 12L
    }

    fun question(lesson: Lesson, seed: Long, index: Int, band: PracticeBand): PracticeQuestion {
        val primary = LearningPathCatalog.question(lesson, seed, index)
        if (band == PracticeBand.CORE) return primary
        val candidates = (0..7).map { variant ->
            if (variant == 0) primary else
                LearningPathCatalog.question(lesson, seed + variant * 100_003L, index)
        }
        // Stable ties: list order wins; no random change after a screen rotation.
        return if (band == PracticeBand.SUPPORTED)
            candidates.minByOrNull(::complexity) ?: primary
        else candidates.maxByOrNull(::complexity) ?: primary
    }

    /** Short, same-lesson transfer practice after a mistake; never reuse an answer key. */
    fun supportQuestion(
        lesson: Lesson, seed: Long, index: Int, original: PracticeQuestion
    ): PracticeQuestion {
        var best: PracticeQuestion? = null
        for (attempt in 0 until 16) {
            val candidate = LearningPathCatalog.question(
                lesson, seed + 17_000_011L + attempt * 177_013L, index
            )
            if (candidate.text != original.text || candidate.answer != original.answer) {
                if (best == null || complexity(candidate) < complexity(best)) best = candidate
            }
        }
        return best ?: original
    }

    fun targetedReview(focus: PracticeFocus): String =
        "Sonraki çalışmanda önce ${focus.label.lowercase()} için görsel örneği incele; ardından aynı konudan farklı bir soru çöz."
}
