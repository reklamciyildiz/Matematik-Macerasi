package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.learning.PracticeVisualKind
import com.yigit033.matematikmacarasi.learning.CounterArrangement
import com.yigit033.matematikmacarasi.learning.PracticeVisualModel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val cyan = Color(0xFF79E3DC)
private val gold = Color(0xFFFFD98B)
private val light = Color(0xFFF0FAFF)
private val surface = Color(0xFF103955)

/** Only displays operands from the currently visible question. */
@Composable
internal fun PracticeQuestionIllustration(
    model: PracticeVisualModel,
    grade: Int,
    modifier: Modifier = Modifier
) {
    val v = model.values
    Column(
        modifier = modifier.fillMaxWidth()
            .background(Color(0xFF174B65), RoundedCornerShape(17.dp))
            .border(1.dp, Color(0xFF508EA5), RoundedCornerShape(17.dp))
            .semantics { contentDescription = model.description }
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (model.kind) {
            PracticeVisualKind.ADD_GROUPS -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GroupOfObjects(v[0], grade, false)
                    Text("+", color = gold, fontSize = 23.sp,
                        fontWeight = FontWeight.ExtraBold)
                    GroupOfObjects(v[1], grade, false)
                }
                Text(v[0].toString() + " ve " + v[1] +
                    " nesneyi birleştir", color = light, fontSize = 12.sp)
            }
            PracticeVisualKind.TAKE_AWAY -> {
                GroupOfObjects(v[0], grade, false, removed = v[1])
                Text(v[0].toString() + " nesneden " + v[1] +
                    " tanesi ayrıldı", color = light, fontSize = 12.sp)
            }
            PracticeVisualKind.COMPARE_GROUPS -> {
                v.forEachIndexed { index, value ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Text(if (index == 0) "A" else "B",
                            color = gold, fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(18.dp))
                        GroupOfObjects(value, grade, true)
                        Text(value.toString(), color = light, fontSize = 13.sp)
                    }
                }
            }
            PracticeVisualKind.THREE_BARS -> {
                val max = v.maxOrNull()?.coerceAtLeast(1) ?: 1
                Row(
                    modifier = Modifier.fillMaxWidth().height(114.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.Bottom
                ) {
                    v.forEachIndexed { index, value ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(value.toString(), color = gold, fontSize = 12.sp)
                            Box(
                                Modifier.width(47.dp)
                                    .height((value * 71f / max).coerceAtLeast(8f).dp)
                                    .background(
                                        if (index % 2 == 0) cyan else
                                            Color(0xFF85A8E2),
                                        RoundedCornerShape(topStart = 7.dp,
                                            topEnd = 7.dp)
                                    )
                            )
                            Text(('A' + index).toString(), color = light,
                                fontSize = 12.sp)
                        }
                    }
                }
            }
            PracticeVisualKind.TENS_AND_ONES -> {
                Text("ONLUK", color = gold, fontWeight = FontWeight.Bold,
                    fontSize = 11.sp)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(v[0].coerceAtMost(9)) {
                        Box(
                            Modifier.width(23.dp).height(54.dp)
                                .background(Color(0xFF3882A3), RoundedCornerShape(6.dp))
                                .border(1.dp, cyan, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("10", color = light,
                                fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }
                Text("BİRLİK", color = gold, fontWeight = FontWeight.Bold,
                    fontSize = 11.sp)
                GroupOfObjects(v[1], grade, true)
            }
            PracticeVisualKind.TWO_EQUAL_PARTS,
            PracticeVisualKind.THREE_EQUAL_PARTS -> {
                val den = if (model.kind == PracticeVisualKind.TWO_EQUAL_PARTS) 2 else 3
                val each = v[0] / den
                Text("Her kutu bir eş parça", color = light, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(den) {
                        Box(
                            modifier = Modifier.weight(1f)
                                .background(surface, RoundedCornerShape(9.dp))
                                .border(1.dp, cyan, RoundedCornerShape(9.dp))
                                .padding(5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            GroupOfObjects(each, grade, true)
                        }
                    }
                }
            }
            PracticeVisualKind.FRACTION_PARTS -> {
                val numerator=v[1];val denominator=v[2]
                Text(denominator.toString() + " eş parçadan " + numerator +
                    " tanesi seçiliyor", color = light, fontSize = 12.sp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    repeat(denominator) { i ->
                        Box(
                            Modifier.weight(1f).height(76.dp)
                                .background(
                                    if (i < numerator) cyan
                                    else Color(0xFF315C78),
                                    RoundedCornerShape(7.dp)
                                )
                                .border(1.dp, gold, RoundedCornerShape(7.dp))
                        )
                    }
                }
            }
            PracticeVisualKind.CLOCK_START -> {
                val hour=v[0]
                Canvas(Modifier.size(150.dp)) {
                    val r=size.minDimension*.46f
                    drawCircle(Color(0xFF24526C),r)
                    drawCircle(gold,r,style=Stroke(3.dp.toPx()))
                    repeat(12) { h ->
                        val rad=(h*30-90)*PI/180
                        val from=Offset(
                            center.x+cos(rad).toFloat()*r*.80f,
                            center.y+sin(rad).toFloat()*r*.80f)
                        val to=Offset(
                            center.x+cos(rad).toFloat()*r*.92f,
                            center.y+sin(rad).toFloat()*r*.92f)
                        drawLine(light,from,to,2.dp.toPx())
                    }
                    val angle=(hour*30-90)*PI/180
                    drawLine(cyan,center,
                        Offset(center.x+cos(angle).toFloat()*r*.57f,
                            center.y+sin(angle).toFloat()*r*.57f),
                        6.dp.toPx(),cap=StrokeCap.Round)
                    drawLine(gold,center,Offset(center.x,center.y-r*.82f),
                        3.dp.toPx(),cap=StrokeCap.Round)
                    drawCircle(light,4.dp.toPx(),center)
                }
                Text("Başlangıç saati: " + v[0] + ".00",
                    color = gold, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold)
            }
            PracticeVisualKind.TRIANGLE, PracticeVisualKind.SQUARE -> {
                Canvas(Modifier.size(135.dp)) {
                    if (model.kind == PracticeVisualKind.TRIANGLE) {
                        val path=Path().apply {
                            moveTo(size.width*.50f,size.height*.10f)
                            lineTo(size.width*.10f,size.height*.89f)
                            lineTo(size.width*.90f,size.height*.89f)
                            close()
                        }
                        drawPath(path,cyan)
                        drawPath(path,gold,style=Stroke(3.dp.toPx()))
                    } else {
                        val p=Offset(size.width*.15f,size.height*.15f)
                        val dims=Size(size.width*.70f,size.height*.70f)
                        drawRect(cyan,p,dims)
                        drawRect(gold,p,dims,style=Stroke(3.dp.toPx()))
                    }
                }
                Text("Köşeleri tek tek say", color = light, fontSize = 12.sp)
            }
            PracticeVisualKind.PERIMETER -> {
                Canvas(Modifier.size(width=210.dp,height=125.dp)) {
                    val p=Offset(size.width*.10f,size.height*.13f)
                    val dims=Size(size.width*.80f,size.height*.73f)
                    drawRect(Color(0xFF275F78),p,dims)
                    drawRect(cyan,p,dims,style=Stroke(4.dp.toPx()))
                }
                Text("Uzun kenar " + v[0] + " · Kısa kenar " + v[1],
                    color = gold, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun GroupOfObjects(
    count: Int, grade: Int, compact: Boolean, removed: Int = 0
) {
    // On a 320dp device, two early-grade groups still fit beside "+".
    val width = if (compact) 13.dp else if (grade <= 2) 22.dp else 19.dp
    val perRow = if (compact) 5 else 4
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        CounterArrangement.rows(count, perRow).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                row.forEach { idx ->
                    Box(Modifier.size(width),contentAlignment=Alignment.Center) {
                        if (grade <= 2 && !compact) {
                            Image(
                                painter = painterResource(R.drawable.ic_apple),
                                contentDescription = null,
                                modifier = Modifier.size(width)
                            )
                        } else {
                            Box(Modifier.size(width)
                                .background(
                                    if (CounterArrangement.isRemoved(idx, count, removed)) Color(0xFFB0ADB0) else cyan,
                                    CircleShape))
                        }
                        if (CounterArrangement.isRemoved(idx, count, removed)) {
                            Canvas(Modifier.size(width)) {
                                drawLine(Color(0xFFFFBC96),
                                    Offset(0f,size.height),
                                    Offset(size.width,0f),
                                    strokeWidth=3.dp.toPx(),cap=StrokeCap.Round)
                            }
                        }
                    }
                }
            }
        }
    }
}
