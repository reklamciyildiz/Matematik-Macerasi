package com.yigit033.matematikmacarasi.games.sayiavi2

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import kotlin.random.Random

/**
 * Antik Tapınak — Çarpmanın Sırları. Şimdilik saf Kotlin oyun matematiği;
 * harita, yıldız ve Premium üzerinde hiçbir etkisi yoktur.
 *
 * Eş gruplar → eşdeğer dizilimler → eksik çarpan → ters işlem.
 * Dört şıklı test kopyası değil, özgün Compose etkileşimlerine veri sağlar.
 */
object TempleEngine {
    enum class Ritual { MOSAIC, PILLARS, HIDDEN_FACTOR, MIRROR }

    data class Mosaic(val rows: Int, val columns: Int, val tiles: Int) {
        init { require(rows in 2..9 && columns in 2..9 && tiles == rows * columns) }
    }

    data class Pillars(
        val leftRows: Int, val leftColumns: Int,
        val rightRows: Int, val rightColumns: Int
    ) {
        val weight: Int get() = leftRows * leftColumns
        init {
            require(leftRows in 2..9 && leftColumns in 2..9)
            require(rightRows in 2..9 && rightColumns in 2..9)
            require(weight == rightRows * rightColumns)
        }
    }

    data class Gate(
        val groups: Int, val itemsPerGroup: Int,
        val total: Int, val options: List<Int>,
        val hint: String
    ) {
        init {
            require(groups in 2..9 && itemsPerGroup in 2..9)
            require(groups * itemsPerGroup == total)
            require(options.size == 4 && options.distinct().size == 4)
            require(itemsPerGroup in options && options.all { it > 0 })
        }
    }

    data class Mirror(
        val leftFactor: Int, val rightFactor: Int, val total: Int,
        val resultAfterDivision: Int, val divisor: Int
    ) {
        init {
            require(leftFactor in 2..9 && rightFactor in 2..9)
            require(leftFactor * rightFactor == total)
            require(divisor == leftFactor || divisor == rightFactor)
            require(total / divisor == resultAfterDivision)
        }
    }

    data class TempleQuest(
        val seed: Long, val tier: LearningTier,
        val mosaic: Mosaic, val pillars: Pillars,
        val gate: Gate, val mirror: Mirror
    )

    fun create(seed: Long, tier: LearningTier): TempleQuest {
        val random = Random(seed xor 0x5A8E12L)
        val factors = when (tier) {
            LearningTier.TEMEL -> 2..4
            LearningTier.GELISEN -> 2..6
            LearningTier.USTALIK -> 3..9
        }
        fun factor() = random.nextInt(factors.first, factors.last + 1)
        val rows = factor()
        val columns = factor()
        val mosaic = Mosaic(rows, columns, rows * columns)

        // Rotasyonla eşdeğerlik: aynı toplamı farklı yönle temsil et.
        // Bu, çarpanların yer değiştirmesi ilişkisinin görsel başlangıcıdır.
        var pillarRows = factor()
        var pillarColumns = factor()
        if (pillarRows == pillarColumns) {
            pillarColumns = if (pillarColumns < factors.last) pillarColumns + 1 else pillarColumns - 1
        }
        val pillars = Pillars(pillarRows, pillarColumns, pillarColumns, pillarRows)

        val groups = factor()
        val count = factor()
        val options = linkedSetOf(count, count + 1, count + 2, maxOf(1, count - 1))
        var extra = count + 3
        while (options.size < 4) options.add(extra++)
        val gate = Gate(groups, count, groups * count, options.shuffled(random),
            "Toplam $groups eş gruba ayrılıyor. Her grupta kaç parça var?")

        val mirrorLeft = factor()
        val mirrorRight = factor()
        val divider = if (random.nextBoolean()) mirrorLeft else mirrorRight
        val mirror = Mirror(mirrorLeft, mirrorRight, mirrorLeft * mirrorRight,
            mirrorLeft * mirrorRight / divider, divider)
        return TempleQuest(seed, tier, mosaic, pillars, gate, mirror)
    }

    /** Mozaik için her dokunuşta tek karo, son taşımdan sonra tamamlanmış olur. */
    fun placeMosaicTile(current: Int, capacity: Int): Int {
        require(capacity > 0 && current in 0..capacity)
        return (current + 1).coerceAtMost(capacity)
    }

    fun isBalanced(puzzle: Pillars): Boolean =
        puzzle.leftRows * puzzle.leftColumns == puzzle.rightRows * puzzle.rightColumns
}
