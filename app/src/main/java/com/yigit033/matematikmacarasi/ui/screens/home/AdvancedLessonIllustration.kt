package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.learning.LessonTeachingScript
import com.yigit033.matematikmacarasi.learning.TeachingVisual
import com.yigit033.matematikmacarasi.learning.FractionSelectionPolicy
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val ink = Color(0xFFF6FAFF)
private val sea = Color(0xFF84E2DF)
private val amber = Color(0xFFFFD78A)
private val base = Color(0xFF123953)
private val outline = Color(0xFF4E87A1)

/** Math diagrams are structured, scalable native drawing — not decorative
 * images detached from what the explanation says.
 */
@Composable
internal fun AdvancedLessonIllustration(
    script: LessonTeachingScript,
    grade: Int,
    step: Int,
    complete: Boolean
) {
    val v = script.groups
    val phase = if (complete) script.steps.lastIndex else step
    // A child can paint/unpaint equal parts in the guided lesson.
    // This state is exploratory and never changes completion, payment or scores.
    var paintedParts by remember(script.headline) {
        mutableStateOf(emptySet<Int>())
    }
    when (script.visual) {
        TeachingVisual.TAKE_AWAY -> {
            val total = v[0]
            val removed = v[1]
            Text(
                if (grade <= 2) "Elmalara dokunup sayabiliriz"
                else "Her sembol bir eş para/nesne birimini temsil eder",
                color = ink, fontSize = 12.sp, textAlign = TextAlign.Center
            )
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(base, RoundedCornerShape(14.dp)).padding(9.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                (0 until total).chunked(5).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        row.forEach { i ->
                            Box(modifier = Modifier.size(if (grade <= 2) 42.dp else 35.dp),
                                contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(R.drawable.ic_apple),
                                    contentDescription = null,
                                    modifier = Modifier.size(if (grade <= 2) 36.dp else 29.dp)
                                )
                                if (phase >= 1 && i < removed) {
                                    Canvas(Modifier.size(37.dp)) {
                                        drawLine(Color(0xFFFFB495),
                                            start = Offset(0f, size.height),
                                            end = Offset(size.width, 0f),
                                            strokeWidth = 5.dp.toPx(), cap = StrokeCap.Round)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Text(
                if (phase >= 1) (total - removed).toString() + " tane kaldı"
                else total.toString() + " tane var",
                color = amber, fontWeight = FontWeight.Bold
            )
        }

        TeachingVisual.PLACE_VALUE -> {
            Text("ONLUKLAR", color = amber, fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(v[0]) {
                    Box(
                        Modifier.padding(3.dp).width(if (grade <= 2) 34.dp else 25.dp)
                            .height(if (grade <= 2) 69.dp else 58.dp)
                            .background(Color(0xFF226F8D), RoundedCornerShape(9.dp))
                            .border(1.dp, sea, RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("10", fontWeight = FontWeight.Bold,
                            color = Color.White, fontSize = 15.sp)
                    }
                }
            }
            if (phase >= 1) {
                Text("BİRLİKLER", color = amber, fontSize = 12.sp,
                    fontWeight = FontWeight.ExtraBold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    repeat(v[1]) {
                        Box(
                            Modifier.size(if (grade <= 2) 25.dp else 20.dp)
                                .background(sea, CircleShape)
                        )
                    }
                }
            }
        }

        TeachingVisual.FRACTION_BAR -> {
            val chosen = v[0]
            val denominator = v[1]
            Text(
                if (complete) "Örnek: " + chosen + "/" + denominator
                else "Parçalara dokunup " + chosen + "/" + denominator +
                    " kesrini kendin boya",
                color = ink, fontSize = 13.sp, textAlign = TextAlign.Center
            )
            Row(
                modifier = Modifier.fillMaxWidth()
                    .background(base, RoundedCornerShape(12.dp)).padding(7.dp),
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                repeat(denominator) { part ->
                    val highlight = if (complete) part < chosen
                        else part in paintedParts
                    Box(
                        modifier = Modifier.weight(1f)
                            .height(if (grade <= 3) 88.dp else 65.dp)
                            .semantics {
                                contentDescription = (part + 1).toString() +
                                    ". eş parça; " +
                                    (if (highlight) "boyalı" else "boyasız")
                            }
                            .clickable(enabled = !complete) {
                                paintedParts = FractionSelectionPolicy.toggle(
                                    paintedParts, part, denominator)
                            }
                            .background(
                                if (highlight) sea else Color(0xFF35637A),
                                RoundedCornerShape(7.dp)
                            )
                            .border(1.dp,
                                if (highlight) amber else outline,
                                RoundedCornerShape(7.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!complete) {
                            Text(if (highlight) "✓" else "",
                                color = base, fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
            Text(
                if (complete) chosen.toString() + "/" + denominator +
                    " · Örnekteki boyalı parçalar"
                else paintedParts.size.toString() + "/" + denominator +
                    " boyalı · Hedef " + chosen + "/" + denominator,
                color = amber, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                fontSize = 13.sp
            )
        }

        TeachingVisual.SHAPE -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Canvas(Modifier.size(103.dp)) {
                        val triangle = Path().apply {
                            moveTo(size.width / 2f, size.height * .1f)
                            lineTo(size.width * .08f, size.height * .9f)
                            lineTo(size.width * .92f, size.height * .9f)
                            close()
                        }
                        drawPath(triangle, Color(0xFF65CFCB))
                        drawPath(triangle, amber, style = Stroke(width = 3.dp.toPx()))
                    }
                    Text("Üçgen · 3 kenar", color = ink,
                        fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                if (phase >= 1) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Canvas(Modifier.size(103.dp)) {
                            val left = size.width * .1f
                            val top = size.height * .14f
                            drawRect(Color(0xFF397DB1), Offset(left, top),
                                androidx.compose.ui.geometry.Size(
                                    size.width * .8f, size.height * .72f))
                            drawRect(amber, Offset(left, top),
                                androidx.compose.ui.geometry.Size(
                                    size.width * .8f, size.height * .72f),
                                style = Stroke(3.dp.toPx()))
                        }
                        Text("Dörtgen · 4 kenar", color = ink,
                            fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        TeachingVisual.CLOCK -> {
            val hour = if (phase >= 1) v[1] else v[0]
            Canvas(Modifier.size(170.dp)) {
                drawCircle(Color(0xFF214E6B), radius = size.minDimension * .46f)
                drawCircle(amber, radius = size.minDimension * .46f,
                    style = Stroke(3.dp.toPx()))
                for (h in 0 until 12) {
                    val a = (h * 30.0 - 90.0) * PI / 180.0
                    val outer = size.minDimension * .42f
                    val inner = size.minDimension * .35f
                    drawLine(ink,
                        Offset(center.x + cos(a).toFloat() * inner,
                            center.y + sin(a).toFloat() * inner),
                        Offset(center.x + cos(a).toFloat() * outer,
                            center.y + sin(a).toFloat() * outer),
                        2.dp.toPx())
                }
                val angle = (hour * 30.0 - 90.0) * PI / 180.0
                drawLine(sea, center,
                    Offset(center.x + cos(angle).toFloat() * size.minDimension * .25f,
                        center.y + sin(angle).toFloat() * size.minDimension * .25f),
                    strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
                drawLine(amber, center,
                    Offset(center.x, center.y - size.minDimension * .37f),
                    strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(ink, 4.dp.toPx(), center)
            }
            Text(if (phase >= 1) "Saat " + v[1] + ".00"
                 else "Saat " + v[0] + ".00",
                color = amber, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }

        TeachingVisual.BAR_CHART -> {
            val heightMax = v.maxOrNull() ?: 1
            Row(
                modifier = Modifier.fillMaxWidth().height(164.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                v.forEachIndexed { i, value ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(value.toString(), color = amber,
                            fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Box(
                            Modifier.width(if (v.size > 3) 33.dp else 45.dp)
                                .height((value.toFloat() / heightMax * 103f)
                                    .coerceAtLeast(14f).dp)
                                .background(if (i % 2 == 0) sea else Color(0xFF6C9BE5),
                                    RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                        )
                        Text(('A' + i).toString(), color = ink, fontSize = 12.sp)
                    }
                }
            }
            if (phase >= 1) Text("Sütunlar miktarları karşılaştırır.",
                color = ink, fontSize = 12.sp)
        }

        TeachingVisual.AREA_GRID -> {
            val columns = v[0]
            val rows = v[1]
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(if (phase >= 1) rows else 1) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(columns) { col ->
                            Box(
                                Modifier.size(if (grade <= 5) 30.dp else 23.dp)
                                    .background(
                                        if ((row + col) % 2 == 0) sea
                                        else Color(0xFF59BFCB),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .border(1.dp, Color(0xFF24637D),
                                        RoundedCornerShape(4.dp))
                            )
                        }
                    }
                }
            }
            Text(columns.toString() + " sütun × " + rows + " satır = " +
                (columns * rows) + " birim kare",
                color = amber, fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center)
        }

        TeachingVisual.PERIMETER -> {
            Text(v[0].toString() + " cm", color = amber,
                fontWeight = FontWeight.Bold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(v[1].toString() + " cm", color = amber, fontSize = 12.sp)
                Box(
                    Modifier.padding(horizontal = 8.dp)
                        .size(width = 178.dp, height = 112.dp)
                        .background(Color(0xFF1C5F78), RoundedCornerShape(5.dp))
                        .border(4.dp, if (phase >= 1) amber else sea,
                            RoundedCornerShape(5.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("İç bölge\nalan", color = ink, textAlign = TextAlign.Center,
                        fontSize = 11.sp)
                }
                Text(v[1].toString() + " cm", color = amber, fontSize = 12.sp)
            }
            Text(v[0].toString() + " cm · Dört kenar toplanır",
                color = amber, fontSize = 12.sp)
        }

        TeachingVisual.DECIMAL -> {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(v[0]) {
                    Box(
                        Modifier.size(42.dp)
                            .background(Color(0xFF267F85), RoundedCornerShape(8.dp))
                            .border(1.dp, amber, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("1", color = ink, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
            Text("TAM BÜTÜNLER", color = ink, fontSize = 11.sp)
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(10) { i ->
                    Box(
                        Modifier.weight(1f).height(53.dp)
                            .background(
                                if (phase >= 1 && i < v[1]) sea else Color(0xFF315D76),
                                RoundedCornerShape(4.dp))
                    )
                }
            }
            Text("10 eş parçadan " + v[1] + " tanesi onda bir",
                color = amber, fontSize = 12.sp)
        }

        TeachingVisual.EQUATION -> {
            val expression = script.diagramCaption.substringBefore("→").trim()
            val sides = expression.split("=").map { it.trim() }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.weight(1f).height(95.dp)
                        .background(Color(0xFF245D77), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(sides.firstOrNull().orEmpty(), color = ink,
                        fontSize = 17.sp, fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center)
                }
                Text("=", color = amber, fontSize = 23.sp,
                    fontWeight = FontWeight.ExtraBold)
                Box(
                    Modifier.weight(1f).height(95.dp)
                        .background(Color(0xFF245D77), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(sides.getOrElse(1) { "?" }, color = ink,
                        fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }
            Text(
                if (phase >= 1) "Eşitliğin iki tarafına da aynı işlem uygulanır."
                else "İki taraf dengede olmalı.",
                color = amber, fontSize = 12.sp, textAlign = TextAlign.Center
            )
        }

        TeachingVisual.MULTIPLES -> {
            val first = v[0]
            val second = v[1]
            val aMultiples = (1..5).map { first * it }
            val bMultiples = (1..5).map { second * it }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    first.toString() + " katları: " +
                        aMultiples.take(if (phase >= 1) 5 else 3).joinToString(" · "),
                    color = ink, fontSize = 13.sp, lineHeight = 19.sp
                )
                if (phase >= 1) {
                    Text(
                        second.toString() + " katları: " +
                            bMultiples.joinToString(" · "),
                        color = sea, fontSize = 13.sp, lineHeight = 19.sp
                    )
                }
                if (phase >= 2) {
                    val common = aMultiples.intersect(bMultiples.toSet()).minOrNull()
                    if (common != null) {
                        Text(
                            "İlk ortak pozitif kat: " + common,
                            color = amber, fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        TeachingVisual.FACTORS -> {
            val shown = if (phase >= 1) 2 else 1
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                v.take(shown).forEach { number ->
                    val factors = (1..number).filter { number % it == 0 }
                    Text("$number bölenleri: " + factors.joinToString(", "),
                        color = ink, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
            if (phase >= 2) {
                val common = (1..v.minOrNull()!!).filter {
                    v.all { number -> number % it == 0 }
                }
                Text("Ortak bölenler: " + common.joinToString(", "),
                    color = amber, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold)
            }
        }

        TeachingVisual.POWER_CHAIN -> {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                v.take(if (phase >= 1) v.size else 2).forEachIndexed { i, n ->
                    if (i > 0) Text("×", color = amber, fontSize = 22.sp)
                    Box(
                        Modifier.size(55.dp)
                            .background(Color(0xFF26627B), RoundedCornerShape(13.dp))
                            .border(1.dp, sea, RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(n.toString(), color = ink,
                            fontSize = 23.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text("Aynı sayı tekrar tekrar çarpılır.",
                color = amber, fontSize = 12.sp)
        }

        TeachingVisual.SYMMETRY -> {
            Canvas(Modifier.fillMaxWidth().height(150.dp)) {
                val middle = size.width / 2f
                val baseline = size.height * .75f
                drawLine(amber,
                    Offset(middle, size.height * .08f),
                    Offset(middle, size.height * .93f),
                    strokeWidth = 2.5.dp.toPx())
                val left = Path().apply {
                    moveTo(middle - size.width * .15f, size.height * .2f)
                    lineTo(middle - size.width * .32f, baseline)
                    lineTo(middle - size.width * .08f, baseline)
                    close()
                }
                drawPath(left, sea)
                drawPath(left, ink, style = Stroke(2.dp.toPx()))
                if (phase >= 1) {
                    val right = Path().apply {
                        moveTo(middle + size.width * .15f, size.height * .2f)
                        lineTo(middle + size.width * .32f, baseline)
                        lineTo(middle + size.width * .08f, baseline)
                        close()
                    }
                    drawPath(right, Color(0xFF7E9DE3))
                    drawPath(right, ink, style = Stroke(2.dp.toPx()))
                    // Equal distances from the vertical symmetry axis.
                    drawLine(amber, Offset(middle - size.width * .15f,
                        size.height * .2f), Offset(middle + size.width * .15f,
                        size.height * .2f), strokeWidth = 1.5.dp.toPx())
                }
            }
            Text("Ortadaki çizgiye göre karşılıklı noktalar eş uzaklıktadır.",
                color = amber, fontSize = 12.sp, textAlign = TextAlign.Center)
        }

        TeachingVisual.ANGLE -> {
            val degrees = if (phase >= 1) v[1] else v[0]
            Canvas(Modifier.fillMaxWidth().height(153.dp)) {
                val pivot = Offset(size.width * .35f, size.height * .82f)
                val arm = minOf(size.width * .43f, size.height * .65f)
                val angle = degrees * PI / 180.0
                drawLine(sea, pivot,
                    Offset(pivot.x + arm, pivot.y),
                    5.dp.toPx(), cap = StrokeCap.Round)
                drawLine(amber, pivot,
                    Offset(pivot.x + (cos(angle) * arm).toFloat(),
                        pivot.y - (sin(angle) * arm).toFloat()),
                    5.dp.toPx(), cap = StrokeCap.Round)
                if (degrees == 90) {
                    val square = 18.dp.toPx()
                    drawLine(ink,
                        Offset(pivot.x + square, pivot.y),
                        Offset(pivot.x + square, pivot.y - square),
                        2.dp.toPx())
                    drawLine(ink,
                        Offset(pivot.x + square, pivot.y - square),
                        Offset(pivot.x, pivot.y - square),
                        2.dp.toPx())
                }
                drawCircle(ink, 4.dp.toPx(), pivot)
            }
            Text(degrees.toString() + "° · " +
                if (degrees < 90) "Dar açı" else "Dik açı",
                color = amber, fontSize = 16.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }

        TeachingVisual.RIGHT_TRIANGLE -> {
            Canvas(Modifier.fillMaxWidth().height(168.dp)) {
                // Preserve the displayed 3:4 leg ratio; arbitrary viewport
                // aspect ratios must not distort the teaching geometry.
                val unit = minOf(size.width * .64f / 4f,
                    size.height * .70f / 3f)
                val origin = Offset(size.width * .20f, size.height * .85f)
                val up = Offset(origin.x, origin.y - 3f * unit)
                val right = Offset(origin.x + 4f * unit, origin.y)
                val triangle = Path().apply {
                    moveTo(origin.x, origin.y)
                    lineTo(up.x, up.y)
                    lineTo(right.x, right.y)
                    close()
                }
                drawPath(triangle, Color(0xFF275A7A))
                drawPath(triangle, sea, style = Stroke(width = 4.dp.toPx()))
                val boxSize = 18.dp.toPx()
                val square = Path().apply {
                    moveTo(origin.x, origin.y - boxSize)
                    lineTo(origin.x + boxSize, origin.y - boxSize)
                    lineTo(origin.x + boxSize, origin.y)
                }
                drawPath(square, amber, style = Stroke(2.dp.toPx()))
            }
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly) {
                Text("Dik kenar: 3", color = ink, fontSize = 13.sp)
                Text("Dik kenar: 4", color = ink, fontSize = 13.sp)
                if (phase >= 1) Text("Hipotenüs: 5", color = amber,
                    fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        TeachingVisual.VOLUME_STACK -> {
            val columns = v[0]
            val rows = v[1]
            val layers = v[2]
            Column(verticalArrangement = Arrangement.spacedBy(9.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                repeat(if (phase >= 1) layers else 1) { layer ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(rows) {
                            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                repeat(columns) {
                                    Box(Modifier.size(25.dp)
                                        .background(
                                            if (layer % 2 == 0) sea
                                            else Color(0xFF7A99E1),
                                            RoundedCornerShape(4.dp))
                                        .border(1.dp, Color(0xFF1D5471),
                                            RoundedCornerShape(4.dp)))
                                }
                            }
                        }
                    }
                }
            }
            Text(
                "Taban: " + (columns * rows) +
                    " birim kare · Kat sayısı: " + layers +
                    " · Hacim: " + (columns * rows * layers) + " birim küp",
                color = amber, fontSize = 12.sp, textAlign = TextAlign.Center
            )
        }

        else -> Unit // Legacy NUMBER_STEPS and GROUPS render separately.
    }
}
