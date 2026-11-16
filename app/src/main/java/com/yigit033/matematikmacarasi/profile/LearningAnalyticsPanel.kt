package com.yigit033.matematikmacarasi.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.GradeCurriculumCatalog
import com.yigit033.matematikmacarasi.learning.GradeLearningSnapshot
import com.yigit033.matematikmacarasi.learning.LearningAnalyticsPolicy
import com.yigit033.matematikmacarasi.learning.PracticeFocus

private val ice = Color(0xFFBDE7F1)
private val gold = Color(0xFFFFD68B)
private val mint = Color(0xFF9DEDD7)

@Composable
internal fun LearningAnalyticsPanel(
    selectedClass: String, snapshot: GradeLearningSnapshot?, isPremium: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        backgroundColor = Color(0xFF133F5C),
        border = BorderStroke(1.dp, Color(0xFF4A8BA0)),
        elevation = 3.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Default.AutoStories, contentDescription = null,
                    tint = gold, modifier = Modifier.height(25.dp))
                Column {
                    Text("Öğrenme İstatistiklerim", color = Color.White,
                        fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("$selectedClass · Cihazdaki öğrenme kayıtları",
                        color = ice, fontSize = 11.sp)
                }
            }
            if (snapshot == null) {
                Text("Bu sınıf seçimi için sayısal ders raporu henüz bulunmuyor.",
                    color = ice, fontSize = 13.sp)
                return@Column
            }
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Tamamlanan kısa ders", color = ice, fontSize = 12.sp)
                Text("${snapshot.completed} / ${snapshot.totalLessons}",
                    color = mint, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
            }
            LinearProgressIndicator(
                progress = snapshot.completionFraction,
                modifier = Modifier.fillMaxWidth().height(8.dp),
                color = mint, backgroundColor = Color(0xFF345C74)
            )
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                LearningMetricTile("Ders oturumu", snapshot.sessions.toString(),
                    Color(0xFF9CCFF4), Modifier.weight(1f))
                LearningMetricTile("Tekrar zamanı", snapshot.due.toString(),
                    Color(0xFFFFE09B), Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                LearningMetricTile("En iyi alıştırma",
                    snapshot.bestPercent?.let { "%$it" } ?: "—",
                    mint, Modifier.weight(1f))
                LearningMetricTile("Hazır kısa ders",
                    snapshot.totalLessons.toString(),
                    Color(0xFFD6BEFF), Modifier.weight(1f))
            }
            if (snapshot.unattributedHistoricalSharedLessons > 0) {
                Text(
                    "Geçmişte sınıf bilgisi olmadan tamamlanan ${snapshot.unattributedHistoricalSharedLessons} " +
                        "ortak ders bu sınıfın istatistiklerine eklenmedi. " +
                        "Eski oyun ve öğrenme kayıtların silinmedi.",
                    color = Color(0xFFFFD4A4), fontSize = 11.sp, lineHeight = 17.sp
                )
            }
            Text(
                "Yüzdeler yalnız uygulamadaki kısa derslere aittir; resmî müfredatın " +
                    "tamamlanmasını veya akademik başarıyı ölçmez.",
                color = ice, fontSize = 11.sp, lineHeight = 16.sp
            )
            Divider(color = Color(0xFF3D7289))
            if (!LearningAnalyticsPolicy.canViewTopicBreakdown(isPremium)) {
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Icon(Icons.Default.Lock, contentDescription = null,
                        tint = gold)
                    Column {
                        Text("Konu bazlı ayrıntılar · Premium",
                            color = Color.White, fontSize = 13.sp,
                            fontWeight = FontWeight.Bold)
                        Text(
                            "Temel ders ilerlemesi ve tekrar bilgisi ücretsiz. " +
                                "Konu dağılımı ve hata odakları Premium'da gösterilir.",
                            color = ice, fontSize = 11.sp, lineHeight = 16.sp
                        )
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    Icon(Icons.Default.Insights, contentDescription = null, tint = mint)
                    Text("Konuya göre çalışma", color = Color.White, fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold)
                }
                val topics = GradeCurriculumCatalog.forGrade(snapshot.grade).topics
                    .filter { it.lessonIds.isNotEmpty() }
                topics.forEach { topic ->
                    val topicLessons = snapshot.lessons.filter { it.lessonId in topic.lessonIds }
                    if (topicLessons.any { it.completed || it.sessions > 0 }) {
                        val done = topicLessons.count { it.completed }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(topic.title, modifier = Modifier.weight(1f),
                                color = ice, fontSize = 12.sp, lineHeight = 17.sp)
                            Spacer(Modifier.width(9.dp))
                            Text("$done / ${topicLessons.size} ders",
                                color = gold, fontSize = 12.sp,
                                fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = if (topicLessons.isEmpty()) 0f
                                else done.toFloat() / topicLessons.size,
                            modifier = Modifier.fillMaxWidth().height(5.dp),
                            color = mint, backgroundColor = Color(0xFF345C74)
                        )
                    }
                }
                val focused = snapshot.recentlyDifficult.take(3)
                if (LearningAnalyticsPolicy.canViewMistakeFocus(isPremium) && focused.isNotEmpty()) {
                    Spacer(Modifier.height(3.dp))
                    Text("Son çalışma önerileri", color = gold,
                        fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    focused.forEach { item ->
                        val focusTitle = PracticeFocus.values()
                            .firstOrNull { it.name == item.focus }?.label
                            ?: "Adım adım tekrar"
                        Text(
                            "• ${item.title}: $focusTitle",
                            color = ice, fontSize = 12.sp, lineHeight = 18.sp
                        )
                    }
                } else {
                    Text(
                        "Henüz konuya özel tekrar gerektiren sınıf kayıtları bulunmuyor.",
                        color = ice, fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun LearningMetricTile(
    title: String, value: String, accent: Color, modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF0D304B), RoundedCornerShape(15.dp))
            .heightIn(min = 75.dp).padding(horizontal = 11.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(title, color = ice, fontSize = 11.sp, lineHeight = 15.sp)
        Text(value, color = accent, fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold)
    }
}
