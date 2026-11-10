package com.yigit033.matematikmacarasi.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * S4 — Dark, accessible game progress insight sheet.
 * Does not fetch Premium status: the parent must enforce entitlement
 * before composing this private per-game breakdown.
 */
@Composable
fun StatisticsDetailPopup(
    title: String,
    items: Map<String, Int>,
    formatValue: (Int) -> String = { it.toString() },
    accentColor: Color,
    onDismiss: () -> Unit
) {
    val played = remember(items) {
        items.filterValues { it > 0 }.entries.sortedWith(
            compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key }
        )
    }
    val biggest = (played.maxOfOrNull { it.value } ?: 0).coerceAtLeast(1)
    val total = played.fold(0L) { count, item ->
        (count + item.value.toLong()).coerceAtMost(Int.MAX_VALUE.toLong())
    }.toInt()
    val night = Color(0xFF0A223A)
    val ink = Color(0xFF0F3754)
    val soft = Color(0xFFBDD7E7)
    val gold = Color(0xFFFFD47B)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 5.dp),
            shape = RoundedCornerShape(26.dp),
            backgroundColor = night,
            border = BorderStroke(1.dp, Color(0xFF477B96)),
            elevation = 12.dp
        ) {
            Column(
                Modifier.fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(ink, night)))
                    .padding(horizontal = 19.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("MACERA ANALİZİ", color = gold, fontSize = 11.sp,
                            letterSpacing = 1.6.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(5.dp))
                        Text(title, color = Color.White, fontSize = 20.sp,
                            lineHeight = 25.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(4.dp))
                        Text("Kayıtlı oyun ilerlemene dayalı detaylar",
                            color = soft, fontSize = 12.sp, lineHeight = 16.sp)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(44.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "İstatistikleri kapat",
                            tint = Color.White, modifier = Modifier.size(23.dp))
                    }
                }

                if (played.isEmpty()) {
                    Column(
                        Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF164361))
                            .padding(horizontal = 18.dp, vertical = 27.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        Text("Henüz oyun kaydı yok", color = Color.White,
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Bir macerayı tamamladığında oyunlara özel veriler burada görünecek.",
                            color = soft, fontSize = 13.sp, lineHeight = 19.sp)
                    }
                } else {
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        InsightMetric("Kayıtlı oyun", "${played.size}",
                            gold, Modifier.weight(1f))
                        InsightMetric("Toplam kayıt", formatValue(total),
                            accentColor, Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically) {
                        Text("OYUN DAĞILIMI", color = soft,
                            fontSize = 11.sp, fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp)
                        Text("${played.size} oyun", color = accentColor,
                            fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().heightIn(max = 340.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)
                    ) {
                        items(played, key = { it.key }) { record ->
                            StatisticDetailItem(
                                gameName = record.key,
                                value = record.value,
                                formatValue = formatValue,
                                maxValue = biggest,
                                accentColor = accentColor
                            )
                        }
                    }
                }
                Text("Yalnızca bu cihazda kaydedilmiş istatistikler gösterilir.",
                    color = soft.copy(alpha = .85f), fontSize = 11.sp, lineHeight = 16.sp)
            }
        }
    }
}

@Composable
private fun InsightMetric(
    label: String, value: String, accentColor: Color, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.clip(RoundedCornerShape(15.dp))
            .background(Color(0xFF204864))
            .padding(horizontal = 12.dp, vertical = 13.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(label, color = Color(0xFFBDD7E7), fontSize = 11.sp,
            maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(value, color = accentColor, fontWeight = FontWeight.ExtraBold,
            fontSize = 17.sp, maxLines = 2, lineHeight = 20.sp,
            overflow = TextOverflow.Ellipsis)
    }
}

@Composable
private fun StatisticDetailItem(
    gameName: String,
    value: Int,
    formatValue: (Int) -> String,
    maxValue: Int,
    accentColor: Color
) {
    val fraction = (value.toFloat() / maxValue.coerceAtLeast(1).toFloat())
        .coerceIn(0f, 1f)
    val animatedFraction by animateFloatAsState(targetValue = fraction)
    Column(
        Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF143C58))
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)) {
            Text(gameName, color = Color.White, fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold, lineHeight = 18.sp,
                modifier = Modifier.weight(1f))
            Text(formatValue(value), color = accentColor, fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold, maxLines = 1)
        }
        Box(Modifier.fillMaxWidth().height(7.dp)
            .clip(RoundedCornerShape(7.dp))
            .background(Color(0xFF2D5772))) {
            Box(Modifier.fillMaxWidth(animatedFraction).fillMaxHeight()
                .clip(RoundedCornerShape(7.dp))
                .background(Brush.horizontalGradient(
                    listOf(accentColor.copy(alpha = .7f), accentColor))))
        }
    }
}
