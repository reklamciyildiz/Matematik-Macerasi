package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.PracticeVisualKind
import com.yigit033.matematikmacarasi.learning.PracticeVisualModel

private val pale = Color(0xFFE6F7FC)
private val teal = Color(0xFF85DCD7)
private val yellow = Color(0xFFFFD78B)
private val blue = Color(0xFF80A7E9)
private val dark = Color(0xFF174560)

/**
 * More abstract models for older learners. Diagrams show the problem's
 * inputs without presenting the final numerical solution as a label.
 */
@Composable
internal fun UpperGradePracticeIllustration(model: PracticeVisualModel) {
    val v = model.values
    when (model.kind) {
        PracticeVisualKind.AREA_GRID -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(v[1]) { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                        repeat(v[0]) { col ->
                            Box(Modifier.size(15.dp)
                                .background(
                                    if ((row + col) % 2 == 0) teal else blue,
                                    RoundedCornerShape(3.dp)))
                        }
                    }
                }
            }
        }

        PracticeVisualKind.DATA_BARS -> {
            val largest = v.maxOrNull()!!.coerceAtLeast(1)
            Row(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                v.forEachIndexed { idx, value ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(value.toString(), color = pale, fontSize = 13.sp,
                            fontWeight = FontWeight.Bold)
                        Box(
                            Modifier.size(
                                width = 46.dp,
                                height = (value.toFloat() / largest * 100f)
                                    .coerceAtLeast(8f).dp
                            ).background(
                                if (idx % 2 == 0) teal else blue,
                                RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp))
                        )
                        Text(('A' + idx).toString(), color = yellow,
                            fontSize = 12.sp)
                    }
                }
            }
        }

        PracticeVisualKind.CHANCE_CHIPS -> {
            val total = v[0] + v[1]
            Column(verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                (0 until total).chunked(8).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        row.forEach { idx ->
                            Box(Modifier.size(27.dp)
                                .background(
                                    if (idx < v[0]) yellow else blue,
                                    CircleShape)
                                .border(1.dp, pale, CircleShape))
                        }
                    }
                }
                Text("Her daire eş olasılıklı bir sonucu gösterir.",
                    color = pale, fontSize = 11.sp)
            }
        }

        PracticeVisualKind.SIGNED_WALK -> {
            Canvas(Modifier.fillMaxWidth().height(105.dp)) {
                val baseline = size.height * .67f
                val xzero = size.width * .42f
                val step = size.width * .023f
                val start = xzero - v[0] * step
                val end = start + v[1] * step
                drawLine(pale,
                    Offset(size.width * .06f, baseline),
                    Offset(size.width * .94f, baseline), 2.dp.toPx())
                for (n in -14..20 step 2) {
                    val x = xzero + n * step
                    if (x in size.width * .05f..size.width * .95f) {
                        drawLine(pale.copy(alpha = .6f),
                            Offset(x, baseline - 5.dp.toPx()),
                            Offset(x, baseline + 5.dp.toPx()), 1.dp.toPx())
                    }
                }
                drawCircle(yellow, 6.dp.toPx(), Offset(start, baseline))
                drawLine(teal,
                    Offset(start, baseline - 18.dp.toPx()),
                    Offset(end, baseline - 18.dp.toPx()),
                    3.dp.toPx(), cap = StrokeCap.Round)
                drawCircle(teal, 4.dp.toPx(),
                    Offset(end, baseline - 18.dp.toPx()))
                drawCircle(pale, 3.dp.toPx(), Offset(xzero, baseline))
            }
            Text("Başlangıç: −" + v[0] + " · Sağa ilerleme: +" + v[1],
                color = yellow, fontSize = 12.sp)
        }

        PracticeVisualKind.ALGEBRA_BALANCE -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    Modifier.weight(1f).height(66.dp)
                        .background(dark, RoundedCornerShape(12.dp))
                        .border(1.dp, teal, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Bilinmeyen x", color = pale, fontSize = 13.sp,
                        fontWeight = FontWeight.Bold)
                }
                Text("=", color = yellow, fontSize = 24.sp,
                    fontWeight = FontWeight.Bold)
                Box(
                    Modifier.weight(1f).height(66.dp)
                        .background(dark, RoundedCornerShape(12.dp))
                        .border(1.dp, blue, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Eşitlik", color = pale, fontSize = 13.sp,
                        fontWeight = FontWeight.Bold)
                }
            }
            Text("Eşitlik bozulmadan iki tarafa aynı işlem uygulanır.",
                color = pale, fontSize = 12.sp, textAlign = TextAlign.Center)
        }

        PracticeVisualKind.POWER_FACTORS -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                repeat(v[1]) { index ->
                    if (index > 0) {
                        Text(" × ", color = yellow, fontSize = 18.sp,
                            fontWeight = FontWeight.Bold)
                    }
                    Box(
                        Modifier.size(42.dp)
                            .background(dark, RoundedCornerShape(9.dp))
                            .border(1.dp, teal, RoundedCornerShape(9.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(v[0].toString(), color = pale,
                            fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text("Taban her çarpanda aynıdır; çarpımı sen hesapla.",
                color = pale, fontSize = 12.sp)
        }

        PracticeVisualKind.RIGHT_TRIANGLE -> {
            Canvas(Modifier.fillMaxWidth().height(157.dp)) {
                val scale = minOf(
                    size.width * .57f / v[1].toFloat(),
                    size.height * .75f / v[0].toFloat()
                )
                val origin = Offset(size.width * .20f, size.height * .86f)
                val up = Offset(origin.x, origin.y - v[0] * scale)
                val right = Offset(origin.x + v[1] * scale, origin.y)
                val path = Path().apply {
                    moveTo(origin.x, origin.y)
                    lineTo(up.x, up.y)
                    lineTo(right.x, right.y)
                    close()
                }
                drawPath(path, Color(0xFF245B76))
                drawPath(path, teal, style = Stroke(4.dp.toPx()))
                val marker = 13.dp.toPx()
                val angleMark=Path().apply {
                    moveTo(origin.x,origin.y-marker)
                    lineTo(origin.x+marker,origin.y-marker)
                    lineTo(origin.x+marker,origin.y)
                }
                drawPath(angleMark,yellow,style=Stroke(2.dp.toPx()))
            }
            Text("Dik kenarlar: " + v[0] + " ve " + v[1] +
                " birim · Hipotenüsü hesapla",
                color = pale, fontSize = 12.sp, textAlign = TextAlign.Center)
        }

        PracticeVisualKind.VOLUME_LAYERS -> {
            Column(horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(9.dp)) {
                repeat(v[2]) { layer ->
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(v[1]) { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                repeat(v[0]) { col ->
                                    Box(Modifier.size(15.dp)
                                        .background(
                                            if ((row + col + layer) % 2 == 0) teal
                                            else blue,
                                            RoundedCornerShape(3.dp)))
                                }
                            }
                        }
                    }
                }
            }
            Text("Taban ve katları kullan; birim küpleri hesapla.",
                color = pale, fontSize = 12.sp)
        }

        PracticeVisualKind.MIRROR_POINTS -> {
            Canvas(Modifier.fillMaxWidth().height(110.dp)) {
                val middle=size.width*.5f
                val y=size.height*.55f
                val dist=minOf(v[0].toFloat()*size.width*.018f,
                    size.width*.35f)
                drawLine(yellow,
                    Offset(middle,size.height*.10f),
                    Offset(middle,size.height*.90f),
                    3.dp.toPx())
                drawLine(pale.copy(alpha=.7f),
                    Offset(middle-dist,y),
                    Offset(middle+dist,y),
                    2.dp.toPx())
                drawCircle(teal,7.dp.toPx(),Offset(middle-dist,y))
                drawCircle(blue,7.dp.toPx(),Offset(middle+dist,y))
            }
            Text("Aynı eksenin iki yanındaki noktaları karşılaştır.",
                color = pale, fontSize = 12.sp)
        }

        PracticeVisualKind.FACTOR_LISTS -> {
            v.forEach { number ->
                val factors = (1..number).filter { number % it == 0 }
                Text(
                    number.toString() + " sayısının bölenleri: " +
                        factors.joinToString(" · "),
                    color = pale, fontSize = 12.sp, lineHeight = 18.sp
                )
            }
            Text("Her iki listede ortak olanları işaretle.",
                color = yellow, fontSize = 12.sp)
        }

        PracticeVisualKind.RATIO_GROUPS -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                v.forEachIndexed { index, value ->
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            Modifier.fillMaxWidth().height(67.dp)
                                .background(
                                    if (index % 2 == 0) Color(0xFF265F78)
                                    else Color(0xFF2F6680),
                                    RoundedCornerShape(12.dp))
                                .border(1.dp,
                                    if (index % 2 == 0) teal else blue,
                                    RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(value.toString(), color = pale,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
            Text("Nicelikler arasındaki ilişkiyi kur.",
                color = yellow, fontSize = 12.sp)
        }

        else -> Unit // Primary-school diagrams use their own renderers.
    }
}
