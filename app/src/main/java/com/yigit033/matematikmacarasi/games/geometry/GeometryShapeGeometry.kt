package com.yigit033.matematikmacarasi.games.geometry

import kotlin.math.cos
import kotlin.math.sin

/**
 * Unit-square coordinates shared by Compose drawing and pure JVM tests.
 * Shape families remain precise regardless of the selected screen density.
 */
object GeometryShapeGeometry {
    fun vertices(shape: CastleShape): List<Pair<Float, Float>> = when (shape) {
        CastleShape.CIRCLE -> emptyList() // curved outline; rendered with drawCircle
        CastleShape.SQUARE -> listOf(
            .22f to .22f, .78f to .22f, .78f to .78f, .22f to .78f)
        CastleShape.RECTANGLE -> listOf(
            .10f to .34f, .90f to .34f, .90f to .66f, .10f to .66f)
        CastleShape.RHOMBUS -> listOf(
            .50f to .25f, .92f to .50f, .50f to .75f, .08f to .50f)
        CastleShape.PARALLELOGRAM -> listOf(
            .26f to .28f, .88f to .28f, .74f to .72f, .12f to .72f)
        CastleShape.TRAPEZOID -> listOf(
            .34f to .22f, .66f to .22f, .90f to .78f, .10f to .78f)
        CastleShape.TRIANGLE -> regular(3)
        CastleShape.PENTAGON -> regular(5)
        CastleShape.HEXAGON -> regular(6)
        CastleShape.OCTAGON -> regular(8)
    }

    private fun regular(sides: Int): List<Pair<Float, Float>> =
        (0 until sides).map { index ->
            val angle = -Math.PI / 2 + index * 2 * Math.PI / sides
            Pair(.5f + (cos(angle) * .38).toFloat(),
                .5f + (sin(angle) * .38).toFloat())
        }
}
