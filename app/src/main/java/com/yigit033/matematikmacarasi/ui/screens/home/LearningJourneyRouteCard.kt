package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.LearningQuest

private val questGold = Color(0xFFFFD88B)
private val questMint = Color(0xFF94F0D8)
private val questNight = Color(0xFF0E3652)

@Composable
fun LearningJourneyRouteCard(
    quest: LearningQuest,
    lessonTitles: Map<String, String>,
    lessonDone: (String) -> Boolean,
    canOpen: (String) -> Boolean,
    onOpen: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val done = quest.completedCount(lessonDone)
    val complete = done == quest.lessonIds.size
    val next = quest.nextLesson(lessonDone)
    val available = canOpen(next)

    Column(
        modifier = modifier.fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A5774), questNight, Color(0xFF102C4B))
                ),
                RoundedCornerShape(22.dp)
            )
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp)
                    .background(Color(0xFF2D7187), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (complete) Icons.Filled.CheckCircle else Icons.Filled.RocketLaunch,
                    contentDescription = null,
                    tint = questGold,
                    modifier = Modifier.size(27.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    quest.title, color = Color.White, fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold, lineHeight = 22.sp
                )
                Text(
                    quest.invitation,
                    color = Color(0xFFC8E0EA), fontSize = 12.sp, lineHeight = 17.sp
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                if (complete) "Tüm kısa dersler tamamlandı" else "Yolculuk ilerlemem",
                color = questMint, fontWeight = FontWeight.SemiBold, fontSize = 12.sp
            )
            Text("$done / ${quest.lessonIds.size}",
                color = questGold, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
        }
        LinearProgressIndicator(
            progress = { done.toFloat() / quest.lessonIds.size },
            modifier = Modifier.fillMaxWidth(),
            color = questMint, trackColor = Color(0xFF385D74)
        )
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            quest.lessonIds.forEachIndexed { index, lessonId ->
                val checked = lessonDone(lessonId)
                val accessible = canOpen(lessonId)
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            if (checked) Color(0xFF1E645F) else Color(0xFF1C4763),
                            RoundedCornerShape(13.dp)
                        )
                        .clickable { onOpen(lessonId) }
                        .heightIn(min = 51.dp)
                        .padding(horizontal = 11.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Box(
                        Modifier.size(27.dp).background(
                            if (checked) questMint else Color(0xFF345D7D), CircleShape
                        ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (checked) Icon(
                            Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(19.dp),
                            tint = questNight
                        ) else Text(
                            (index + 1).toString(),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        lessonTitles[lessonId] ?: "Kısa Ders",
                        modifier = Modifier.weight(1f),
                        color = Color.White,
                        fontSize = 12.sp, lineHeight = 17.sp,
                        fontWeight = if (checked) FontWeight.Medium else FontWeight.SemiBold
                    )
                    if (!accessible) {
                        Icon(Icons.Filled.Lock, contentDescription = "Premium ders",
                            tint = questGold, modifier = Modifier.size(18.dp))
                    } else if (checked) {
                        Text("Tekrar", color = questMint, fontSize = 11.sp)
                    } else {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Dersi aç",
                            tint = questGold, modifier = Modifier.size(19.dp))
                    }
                }
            }
        }
        Button(
            onClick = { onOpen(next) },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = questGold, contentColor = questNight
            )
        ) {
            Icon(Icons.Filled.Star, contentDescription = null,
                modifier = Modifier.size(19.dp))
            Spacer(Modifier.width(7.dp))
            Text(
                when {
                    !available -> "Premium Dersi İncele"
                    complete -> "Yolculuğu Tekrar Oyna"
                    else -> "Yolculuğa Devam Et"
                },
                fontWeight = FontWeight.ExtraBold, fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
