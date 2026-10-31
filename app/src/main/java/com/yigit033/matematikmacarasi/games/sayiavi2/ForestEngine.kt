package com.yigit033.matematikmacarasi.games.sayiavi2

import kotlin.random.Random

/** Gizemli Orman matematik motoru: Android/Compose'dan bağımsız, seed ile sınanabilir. */
object ForestEngine {
    const val SPAN_LENGTH = 4
    const val BRIDGE_SPANS = 3
    const val LEAF_ROUNDS = 3
    const val TRAIL_STEPS = 4
    const val ROOT_ROUNDS = 3
    val plankLengths = listOf(1, 2, 3)

    data class BridgeDecision(val nextFill: Int, val completesSpan: Boolean, val valid: Boolean)
    fun placePlank(currentFill: Int, length: Int): BridgeDecision {
        require(currentFill in 0..3)
        require(length in plankLengths)
        val total = currentFill + length
        return BridgeDecision(if (total == SPAN_LENGTH) 0 else if (total < SPAN_LENGTH) total else currentFill,
            completesSpan = total == SPAN_LENGTH, valid = total <= SPAN_LENGTH)
    }

    data class LeafRound(val values: List<Int>, val incorrectIndex: Int, val correctValue: Int) {
        init {
            require(values.size == 5 && incorrectIndex in 2..4)
            require(values[incorrectIndex] != correctValue)
            require(values[1] - values[0] == 4)
            require(values.indices.filter { it != incorrectIndex }
                .all { values[it] == values[0] + 4 * it })
        }
    }
    fun leafRounds(seed: Long): List<LeafRound> {
        val r = Random(seed xor 0x6215L)
        return (0 until LEAF_ROUNDS).map {
            val start = 4 * r.nextInt(1, 6)
            val incorrect = r.nextInt(2, 5)
            val answer = start + 4 * incorrect
            val values = (0 until 5).map { i ->
                if (i == incorrect) answer + listOf(-2, -1, 1, 2)[r.nextInt(4)]
                else start + 4 * i
            }
            LeafRound(values, incorrect, answer)
        }
    }

    /** Patika düğümleri 4'er ilerler; yanlış yollar 2 veya 6 adım uzaktır. */
    data class TrailStep(val from: Int, val choices: List<Int>, val target: Int) {
        init {
            require(choices.size == 3 && choices.distinct().size == 3)
            require(target in choices && target - from == 4)
        }
    }
    fun trailSteps(seed: Long): List<TrailStep> {
        val r = Random(seed xor 0x4A27L)
        val start = r.nextInt(1, 5) * 4
        return (0 until TRAIL_STEPS).map { i ->
            val from = start + 4 * i
            TrailStep(from, listOf(from + 2, from + 4, from + 6).shuffled(r), from + 4)
        }
    }

    enum class RootKind { COUNT, BACKTRACK, MISSING_GROUP }
    data class RootChallenge(val kind: RootKind, val question: String, val options: List<Int>,
        val correct: Int, val hint: String, val reason: String) {
        init {
            require(options.size == 4 && options.distinct().size == 4 && correct in options)
        }
    }
    fun rootChallenges(seed: Long): List<RootChallenge> {
        val r = Random(seed xor 0x22ABL)
        val groupCount = r.nextInt(3, 6)
        val starting = r.nextInt(5, 10) * 4
        val found = r.nextInt(3, 6)
        fun options(correct: Int): List<Int> {
            val all = linkedSetOf(correct, correct + 4, correct + 2, correct - 4)
            return all.toList().shuffled(r)
        }
        return listOf(
            RootChallenge(RootKind.COUNT,
                "$groupCount küçük yuvanın her birinde 4 tohum var. Toplam kaç tohum var?",
                options(groupCount * 4), groupCount * 4,
                "Her yuvada dört tohum say: 4, 8, 12...",
                "$groupCount yuva × 4 tohum = ${groupCount * 4}."),
            RootChallenge(RootKind.BACKTRACK,
                "Orman bekçisi $starting adım ileride. 4'er geriye 2 adım atarsa nerede olur?",
                options(starting - 8), starting - 8,
                "İki kez dört çıkar: önce $starting - 4, sonra tekrar - 4.",
                "$starting - 4 - 4 = ${starting - 8}."),
            RootChallenge(RootKind.MISSING_GROUP,
                "Sandıkta ${found * 4} fındık var. Her sincap 4 fındık alırsa kaç sincap beslenir?",
                options(found), found,
                "Fındıkları dörderli ayırıp grupları say.",
                "${found * 4} fındık, ${found} tane dörtlü grup oluşturur.")
        )
    }
}

/** Üç ayrı aşama ve final, oturum verisi eski ada kayıtlarından bağımsızdır. */
enum class ForestStage { INTRO, BRIDGE, LEAVES, TRAIL, ROOTS, SUMMARY }

data class ForestSession(
    val stage: ForestStage = ForestStage.INTRO,
    val seed: Long = 0L,
    val startedAt: Long = 0L,
    val bridgeSpan: Int = 0,
    val bridgeFill: Int = 0,
    val leafRound: Int = 0,
    val trailStep: Int = 0,
    val rootRound: Int = 0,
    val attempts: Int = 0,
    val errors: Int = 0,
    val hints: Int = 0
) {
    val accuracy: Int get() = if (attempts == 0) 0 else ((attempts - errors) * 100 / attempts)
    fun answer(correct: Boolean): ForestSession = copy(attempts = attempts + 1,
        errors = errors + if (correct) 0 else 1)
    fun hint(): ForestSession = copy(hints = hints + 1)

    fun valid(): Boolean = seed >= 0 && startedAt >= 0 &&
        bridgeSpan in 0..3 && bridgeFill in 0..3 &&
        leafRound in 0..3 && trailStep in 0..4 && rootRound in 0..3 &&
        attempts >= errors && errors >= 0 && hints >= 0
}
