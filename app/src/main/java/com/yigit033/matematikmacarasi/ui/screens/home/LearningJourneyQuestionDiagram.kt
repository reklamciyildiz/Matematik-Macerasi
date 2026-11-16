package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val sea = Color(0xFF173E5C)
private val cream = Color(0xFFFFDD9A)
private val turquoise = Color(0xFF85E3D3)
private val faint = Color(0xFFE4F4FF)

/** Exact values are extracted from the *current generated problem*, not
 * from the separate, fixed teaching-script example. Never draw an arbitrary
 * equation and claim it depicts the student's question.
 */
@Composable
fun LearningJourneyQuestionDiagram(
    lessonId: String,
    questionText: String,
    modifier: Modifier = Modifier
) {
    val integers = Regex("""\d+""").findAll(questionText)
        .mapNotNull { it.value.toIntOrNull() }.toList()
    if (integers.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth()
            .background(sea, RoundedCornerShape(17.dp))
            .border(1.dp, Color(0xFF558EA3), RoundedCornerShape(17.dp))
            .padding(13.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text("SAYILARI GÖR", fontSize = 11.sp,
            color = cream, fontWeight = FontWeight.ExtraBold)
        when (lessonId) {
            "g2_backward_count" -> {
                if (integers.size >= 3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        integers.take(3).forEachIndexed { i, value ->
                            if (i > 0) Text("←", color = cream, fontSize = 19.sp)
                            NumberChip(value.toString(), Modifier.weight(1f))
                        }
                        Text("← ?", color = cream, fontWeight = FontWeight.ExtraBold)
                    }
                    Text("Oklar geriye gidiyor. İki sayı arasındaki fark aynı kalmalı.",
                        fontSize = 12.sp, color = faint, lineHeight = 17.sp)
                }
            }
            "g2_inverse_ops" -> {
                if (integers.size >= 2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()) {
                        NumberChip(integers[0].toString(), Modifier.weight(1f))
                        Text("−", color = cream, fontSize = 24.sp)
                        NumberChip(integers[1].toString(), Modifier.weight(1f))
                        Text("= ?", color = cream, fontSize = 20.sp)
                    }
                    Text("Çıkarma işlemini, cevabı toplama ile kontrol ederek çöz.",
                        fontSize = 12.sp, color = faint)
                }
            }
            "g3_story_two_step" -> {
                if (integers.size >= 3) {
                    val groups = integers[0].coerceIn(1, 6)
                    val per = integers[1].coerceIn(1, 10)
                    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        repeat(groups) { index ->
                            Row(verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Kutu ${index + 1}", color = faint, fontSize = 12.sp)
                                Text("● ".repeat(per), color = turquoise, fontSize = 13.sp)
                            }
                        }
                    }
                    Text("Kutu dışına ${integers[2]} nesne daha eklendi. Önce kutuları say.",
                        color = cream, fontSize = 12.sp)
                }
            }
            "g3_data_difference" -> {
                if (integers.size >= 2) {
                    DataBar("Elma", integers[0], Color(0xFFFFC27E))
                    DataBar("Armut", integers[1], turquoise)
                    Text("Sütunların eşit olan kısımlarını düşün; aradaki farkı bul.",
                        color = faint, fontSize = 12.sp)
                }
            }
            "g4_round_tens" -> {
                val value = integers[0]
                val lower = value / 10 * 10
                val upper = lower + 10
                Text("Hangi komşu onluğa daha yakın?", color = faint,
                    fontSize = 12.sp, fontWeight = FontWeight.Bold)
                LinearProgressIndicator(
                    progress = { (value - lower) / 10f },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = cream, trackColor = Color(0xFF376782)
                )
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(lower.toString(), color = turquoise, fontSize = 14.sp)
                    Text(value.toString(), color = cream, fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold)
                    Text(upper.toString(), color = turquoise, fontSize = 14.sp)
                }
            }
            "g4_unit_fraction_compare" -> {
                if (integers.size >= 4) {
                    // 1/denominator A and 1/denominator B: match textual pairs.
                    val left = integers[1].coerceIn(2, 20)
                    val right = integers[3].coerceIn(2, 20)
                    FractionRow(left, turquoise)
                    FractionRow(right, cream)
                    Text("Bütünler aynı. Daha çok parçaya bölünen bütündeki bir parça daha küçüktür.",
                        color = faint, fontSize = 12.sp, lineHeight = 17.sp)
                }
            }
        }
    }
}

@Composable
private fun NumberChip(value: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.heightIn(min = 40.dp)
            .background(Color(0xFF295D7B), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(value, color = Color.White, fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp)
    }
}

@Composable
private fun DataBar(title: String, amount: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Text("$title  $amount", color = faint,
            fontSize = 12.sp, modifier = Modifier.width(88.dp))
        LinearProgressIndicator(
            progress = { (amount / 20f).coerceIn(0f, 1f) },
            modifier = Modifier.weight(1f).height(13.dp),
            color = color, trackColor = Color(0xFF345E79)
        )
    }
}

@Composable
private fun FractionRow(denominator: Int, accent: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text("1/$denominator", modifier = Modifier.width(40.dp),
            color = faint, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            repeat(denominator) { i ->
                Box(
                    modifier = Modifier.weight(1f).height(26.dp)
                        .background(
                            if (i == 0) accent else Color(0xFF335975),
                            RoundedCornerShape(3.dp)
                        )
                )
            }
        }
    }
}
