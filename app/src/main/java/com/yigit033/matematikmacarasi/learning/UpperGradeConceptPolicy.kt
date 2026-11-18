package com.yigit033.matematikmacarasi.learning

/**
 * Pure math mapping for optional P3.2 concept previews. Does not unlock lessons,
 * change learning evidence, scores or billing. Values are examples, not a claim
 * that a complete grade outcome has been taught.
 */
enum class AdvancedPreviewKind {
    NUMBER_LINE, FRACTION, ANGLE, AREA, DATA, CHANCE, LINEAR,
    RATIO, REFLECTION, FACTORS, SQUARES, VOLUME, MULTIPLES
}

object UpperGradeConceptPolicy {
    fun previewFor(topic: CurriculumTopic): AdvancedPreviewKind? {
        if (topic.grade !in 5..8) return null
        val suffix = topic.id.removePrefix("g${topic.grade}_")
        return when (suffix) {
            "numbers" -> AdvancedPreviewKind.NUMBER_LINE
            "fractions" -> AdvancedPreviewKind.FRACTION
            "operations", "review" -> AdvancedPreviewKind.MULTIPLES
            "geometry" -> if (topic.grade >= 7) AdvancedPreviewKind.REFLECTION
                else AdvancedPreviewKind.ANGLE
            "measure" -> AdvancedPreviewKind.AREA
            "data" -> AdvancedPreviewKind.DATA
            "chance", "prob" -> AdvancedPreviewKind.CHANCE
            "algebra" -> AdvancedPreviewKind.LINEAR
            "ratio" -> AdvancedPreviewKind.RATIO
            "transform" -> AdvancedPreviewKind.REFLECTION
            "factors" -> AdvancedPreviewKind.FACTORS
            "powers" -> AdvancedPreviewKind.SQUARES
            "solids" -> AdvancedPreviewKind.VOLUME
            else -> null
        }
    }

    /** One based preview state; saveable UI clamps before reading results. */
    fun steps(kind: AdvancedPreviewKind): Int = when (kind) {
        AdvancedPreviewKind.NUMBER_LINE -> 9
        AdvancedPreviewKind.FRACTION -> 4
        AdvancedPreviewKind.ANGLE -> 5
        AdvancedPreviewKind.AREA -> 4
        AdvancedPreviewKind.DATA -> 5
        AdvancedPreviewKind.CHANCE -> 5
        AdvancedPreviewKind.LINEAR -> 5
        AdvancedPreviewKind.RATIO -> 4
        AdvancedPreviewKind.REFLECTION -> 7
        AdvancedPreviewKind.FACTORS -> 4
        AdvancedPreviewKind.SQUARES -> 4
        AdvancedPreviewKind.VOLUME -> 4
        AdvancedPreviewKind.MULTIPLES -> 5
    }

    fun safeStep(kind: AdvancedPreviewKind, step: Int): Int =
        step.coerceIn(0, steps(kind) - 1)

    /** For 7th grade show halves; grades 5/6 use exact whole-number positions. */
    fun lineNumerator(grade: Int, step: Int): Int =
        if (grade == 7) safeStep(AdvancedPreviewKind.NUMBER_LINE, step) - 4
        else safeStep(AdvancedPreviewKind.NUMBER_LINE, step) - 4

    fun lineDenominator(grade: Int): Int = if (grade == 7) 2 else 1

    fun angleDegrees(step: Int): Int =
        listOf(30, 60, 90, 120, 150)[safeStep(AdvancedPreviewKind.ANGLE, step)]

    fun factorNumber(step: Int): Int =
        listOf(12, 18, 24, 30)[safeStep(AdvancedPreviewKind.FACTORS, step)]

    fun factorsOf(number: Int): List<Int> {
        require(number in 1..100)
        return (1..number).filter { number % it == 0 }
    }

    fun pointX(step: Int): Int = safeStep(AdvancedPreviewKind.REFLECTION, step) - 3
    fun reflectionAcrossYAxis(x: Int): Int = -x
    fun linearY(x: Int): Int = 2 * x + 3
    fun ratioAmounts(step: Int): Pair<Int, Int> {
        val factor = safeStep(AdvancedPreviewKind.RATIO, step) + 1
        return 2 * factor to 3 * factor
    }
    fun fractionParts(step: Int): Pair<Int, Int> =
        (safeStep(AdvancedPreviewKind.FRACTION, step) + 1) to 4
    fun squareNumber(step: Int): Int {
        val side = safeStep(AdvancedPreviewKind.SQUARES, step) + 1
        return side * side
    }
    fun volumeUnitCubes(step: Int): Int =
        2 * 3 * (safeStep(AdvancedPreviewKind.VOLUME, step) + 1)
    fun probabilityRed(step: Int): Pair<Int, Int> =
        (safeStep(AdvancedPreviewKind.CHANCE, step) + 1) to 6
}
