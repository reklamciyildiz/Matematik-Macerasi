package com.yigit033.matematikmacarasi.games.fractions

import kotlin.math.abs
import kotlin.random.Random

/** Exact rational arithmetic: no floating-point equality in scoring. */
data class CrystalFraction(val numerator: Int, val denominator: Int) :
    Comparable<CrystalFraction> {
    init { require(denominator > 0 && numerator >= 0) }
    val canonical: CrystalFraction
        get() {
            val divisor = gcd(numerator, denominator)
            return CrystalFraction(numerator / divisor, denominator / divisor)
        }
    override fun compareTo(other: CrystalFraction): Int =
        (numerator.toLong() * other.denominator).compareTo(
            other.numerator.toLong() * denominator)
    operator fun plus(other: CrystalFraction) = CrystalFraction(
        numerator * other.denominator + other.numerator * denominator,
        denominator * other.denominator).canonical
    fun equivalent(other: CrystalFraction) = compareTo(other) == 0
    fun format() = "$numerator/$denominator"
    companion object {
        private fun gcd(a: Int, b: Int): Int {
            var x = abs(a); var y = abs(b)
            while(y != 0) { val t = x % y; x = y; y = t }
            return x.coerceAtLeast(1)
        }
    }
}

enum class CrystalMode(val title: String, val icon: String) {
    FILL("Kristali Aydınlat", "💎"),
    BRIDGE("Işık Köprüsü", "🌉"),
    COMPARE("Denge Tapınağı", "⚖"),
    PORTAL("Bilgelik Kapısı", "✦")
}

data class CrystalTask(
    val mode: CrystalMode,
    val target: CrystalFraction,
    val rival: CrystalFraction,
    val label: String,
    val skill: String,
    val choices: List<String> = emptyList(),
    val answer: String = "",
    val explanation: String = ""
)

/** 12 short hands-on puzzles, deterministic for one run and replayable with new seeds. */
object FractionCrystalEngine {
    const val TASK_COUNT = 12
    val difficultyNames = listOf("Temel", "Orta", "Zor")

    fun denominators(grade: Int, difficulty: Int): List<Int> = when {
        grade <= 3 -> if (difficulty == 0) listOf(2,4) else listOf(2,4,8)
        grade == 4 -> if (difficulty == 0) listOf(2,3,4) else listOf(2,3,4,5,6,8)
        grade == 5 -> if (difficulty == 0) listOf(2,4,5,10) else listOf(2,4,5,10,20)
        else -> if (difficulty == 0) listOf(2,3,4,5,10) else listOf(2,3,4,5,6,8,10,20)
    }

    fun generate(seed: Long, grade: Int, difficulty: Int): List<CrystalTask> {
        require(seed > 0 && grade in 1..8 && difficulty in 0..2)
        val random = Random(seed)
        val allowed = denominators(grade, difficulty)
        val seen = mutableSetOf<String>()
        return (0 until TASK_COUNT).map { index ->
            val mode = CrystalMode.entries[index % 4]
            var result: CrystalTask? = null
            repeat(120) {
                if (result != null) return@repeat
                val denominator = allowed.random(random)
                val numerator = random.nextInt(1, denominator)
                val target = CrystalFraction(numerator, denominator)
                val rivalDen = if (grade >= 4) allowed.random(random) else denominator
                val rivalNum = random.nextInt(1, rivalDen)
                val rival = CrystalFraction(rivalNum, rivalDen)
                val key = "${mode.name}:${target.canonical}:${rival.canonical}"
                if (seen.add(key)) result = makeTask(mode, target, rival, grade, random, index)
            }
            result ?: error("Fraction task pool exhausted for $grade/$difficulty at $index")
        }
    }

    private fun makeTask(
        mode: CrystalMode, fraction: CrystalFraction, rival: CrystalFraction,
        grade: Int, random: Random, index: Int
    ): CrystalTask {
        val prompt: String
        val skill: String
        val choices: List<String>
        val answer: String
        val explanation: String
        when(mode) {
            CrystalMode.FILL -> {
                prompt = "Kristalin ${fraction.format()} parçasını dokunarak aydınlat."
                skill = "fraction_part_whole"
                choices = emptyList(); answer = fraction.format()
                explanation = "Payda ${fraction.denominator} eş parçayı, pay ${fraction.numerator} boyanacak parçayı gösterir."
            }
            CrystalMode.BRIDGE -> {
                prompt = "Her dokunuşta 1/${fraction.denominator} ekle. Köprüyü tam ${fraction.format()} yap."
                skill = "fraction_unit_composition"
                choices = emptyList(); answer = fraction.format()
                explanation = "${fraction.numerator} tane 1/${fraction.denominator}, ${fraction.format()} eder."
            }
            CrystalMode.COMPARE -> {
                prompt = "Hangi taraftaki kristal daha büyük?"
                skill = "fraction_compare"
                choices = listOf("SOL", "EŞİT", "SAĞ")
                answer = when { fraction > rival -> "SOL"; fraction < rival -> "SAĞ"; else -> "EŞİT" }
                explanation = "Aynı bütünde karşılaştır: ${fraction.format()} ${when(answer) {
                    "SOL" -> ">"; "SAĞ" -> "<"; else -> "="
                }} ${rival.format()}."
            }
            CrystalMode.PORTAL -> {
                if (grade >= 6 && index == 7) {
                    val otherNumerator = random.nextInt(1,
                        fraction.denominator - fraction.numerator + 1)
                    val other = CrystalFraction(otherNumerator, fraction.denominator)
                    val sum = fraction + other
                    prompt = "İki kristal enerjisini birleştir: ${fraction.format()} + ${other.format()}"
                    skill = "fraction_arithmetic"
                    answer = sum.format()
                    val wrong = listOf(
                        CrystalFraction(fraction.numerator+otherNumerator,
                            fraction.denominator*2).canonical.format(),
                        CrystalFraction(fraction.numerator+otherNumerator-1,
                            fraction.denominator).canonical.format(),
                        CrystalFraction(fraction.numerator+otherNumerator+1,
                            fraction.denominator).canonical.format(),
                        CrystalFraction(fraction.numerator,
                            fraction.denominator+otherNumerator).canonical.format()
                    )
                    choices = (wrong.filter{it!=answer}.distinct()
                        .shuffled(random).take(3) + answer).shuffled(random)
                    explanation = "Paydalar eşitse paylar toplanır: ${sum.format()}."
                } else {
                skill = if (grade >= 5) "fraction_representation" else "fraction_equivalence"
                if (grade >= 5 && fraction.denominator in listOf(2,4,5,10,20)) {
                    val percent = fraction.numerator * 100 / fraction.denominator
                    prompt = "${fraction.format()} kesrinin yüzde karşılığını seç."
                    answer = "%$percent"
                    choices = (listOf(percent, (percent+10).coerceAtMost(100),
                        (percent-10).coerceAtLeast(0), 100-percent, 0, 100)
                        .distinct().filter { it in 0..100 }.take(5).map { "%$it" } +
                        answer).distinct().shuffled(random).take(4).let {
                        if (answer in it) it else (it.take(3)+answer).shuffled(random)
                    }
                    explanation = "${fraction.format()} = $percent/100 = %$percent."
                } else {
                    prompt = "Aynı miktarı gösteren kesri bul."
                    answer = CrystalFraction(fraction.numerator*2,fraction.denominator*2).format()
                    choices = listOf(answer,
                        CrystalFraction(fraction.numerator, fraction.denominator+1).format(),
                        CrystalFraction((fraction.numerator+1).coerceAtMost(fraction.denominator),
                            fraction.denominator).format(),
                        CrystalFraction(fraction.numerator,fraction.denominator*2).format())
                        .distinct().shuffled(random)
                    explanation = "Payı ve paydayı aynı sayı ile çarpmak miktarı değiştirmez."
                }
                }
            }
        }
        return CrystalTask(mode,fraction,rival,prompt,skill,choices,answer,explanation)
    }

    fun correct(task: CrystalTask, filled: Int, chosen: String?): Boolean = when(task.mode) {
        CrystalMode.FILL, CrystalMode.BRIDGE -> filled == task.target.numerator
        CrystalMode.COMPARE, CrystalMode.PORTAL -> chosen == task.answer
    }

    /** An incomplete step is not a successful checkpoint. */
    fun validSelection(task: CrystalTask, selected: Set<Int>, filled: Int): Boolean =
        when(task.mode) {
            CrystalMode.FILL -> selected.all { it in 0 until task.target.denominator }
            CrystalMode.BRIDGE -> filled in 0..task.target.denominator
            else -> selected.isEmpty() && filled == 0
        }

    fun skillCodes(grade: Int): List<String> =
        if (grade <= 4) listOf("fraction_part_whole","fraction_unit_composition",
            "fraction_compare","fraction_equivalence")
        else listOf("fraction_part_whole","fraction_unit_composition",
            "fraction_compare","fraction_representation") +
            if(grade>=6) listOf("fraction_arithmetic") else emptyList()
}
