package com.yigit033.matematikmacarasi.games.sayiavi2

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import kotlin.random.Random

/** Extra practice is independent of stars, purchases and the main game checkpoints. */
enum class AdventureReviewKind(val title: String) {
    COUNT_THREE("Kumsal Koyu"),
    COUNT_FOUR("Gizemli Orman"),
    MULTIPLICATION("Antik Tapınak")
}

data class AdventureReviewQuestion(
    val prompt: String,
    val options: List<Int>,
    val correct: Int,
    val explanation: String,
    val hint: String,
    val focus: Int,
    val groups: Int,
    val perGroup: Int
) {
    init {
        require(options.size == 4 && options.distinct().size == 4)
        require(correct in options && options.all { it > 0 })
        require(groups in 2..9 && perGroup in 2..9)
    }
}

/** Deterministic, grade-independent question generator; grade only recommends a starting tier. */
object AdventureReviewEngine {
    fun create(
        kind: AdventureReviewKind,
        tier: LearningTier,
        seed: Long,
        weakFocus: Int? = null
    ): List<AdventureReviewQuestion> {
        val random = Random(seed xor 0x7D09A1L)
        val range = when (tier) {
            LearningTier.TEMEL -> 2..4
            LearningTier.GELISEN -> 3..6
            LearningTier.USTALIK -> 5..9
        }
        fun factor(): Int = random.nextInt(range.first, range.last + 1)
        fun options(answer: Int, step: Int): List<Int> {
            val choices = linkedSetOf(answer)
            val offsets = listOf(-step, step, -1, 1, -2 * step, 2 * step, 2, -2)
            for (offset in offsets) if (answer + offset > 0) choices.add(answer + offset)
            var extra = answer + 3
            while (choices.size < 4) choices.add(extra++)
            return choices.take(4).shuffled(random)
        }
        fun question(
            text: String, answer: Int, why: String, hint: String,
            focus: Int, groups: Int, perGroup: Int, step: Int
        ) = AdventureReviewQuestion(
            text, options(answer, step), answer, why, hint, focus, groups, perGroup
        )

        if (kind == AdventureReviewKind.MULTIPLICATION) {
            val a = factor()
            val b = factor()
            val c = factor()
            val d = (weakFocus ?: factor()).coerceIn(2, 9)
            return listOf(
                question("$a sıra, her sırada $b taş var. Toplam kaç taş?",
                    a * b, "$a × $b = ${a * b}",
                    "Her sıradaki taş sayısını sıra sayısı kadar topla.",
                    b, a, b, b),
                question("$c × ? = ${c * b}. Eksik çarpan kaçtır?",
                    b, "${c * b} ÷ $c = $b",
                    "Toplam taşı eş grup sayısına böl.",
                    b, c, b, 1),
                question("${d * a} ÷ $d = ?",
                    a, "$d × $a = ${d * a}. Bölme bunu tersine çevirir.",
                    "Çarpmanın ters işlemini düşün.",
                    d, d, a, 1)
            )
        }
        val step = if (kind == AdventureReviewKind.COUNT_THREE) 3 else 4
        val a = factor()
        val b = factor()
        val groups = (weakFocus ?: factor()).coerceIn(2, 9)
        val start = a * step
        val middle = b * step
        return listOf(
            question("$start, ${start + step}, ?  Sayıları $step'er ilerlet.",
                start + step * 2, "$start + $step + $step = ${start + step * 2}",
                "Bir sonraki sayıya $step ekle.",
                a, a, step, step),
            question("$middle, ?, ${middle + 2 * step}. Eksik sayı kaç?",
                middle + step, "$middle + $step = ${middle + step}",
                "İki komşu sayı arasındaki fark $step.",
                b, b, step, step),
            question("$groups eş grupta $step taş var. Toplam kaç taş?",
                groups * step, "$groups × $step = ${groups * step}",
                "Her gruba $step taş ekle ve hepsini say.",
                groups, groups, step, step)
        )
    }
}
