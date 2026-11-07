package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.LearningPathCatalog

/**
 * Student-first learning hub. Show activities that actually work, not a wall
 * of placeholder modules. The parent report is available on demand so it
 * doesn't push the first playable lesson below the fold.
 */
@Composable
fun LearnTabScreen(
    userClass: String,
    onNavigateToGames: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lessons = LearningPathCatalog.forGrade(userClass)
    val gradeNumber = userClass.substringBefore(".").trim().toIntOrNull() ?: 1
    val reviewOnly = gradeNumber >= 5
    var reportExpanded by rememberSaveable { mutableStateOf(false) }
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().background(
                Color(0xFF174D69), shape
            ).border(1.dp, Color(0xFF568DA7), shape).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.AutoStories, contentDescription = null,
                    tint = HomeColors.StarAmber, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(9.dp))
                Text("Öğrenme Yolculuğum", color = Color.White,
                    fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
            }
            Text(
                (userClass.ifBlank { "1. Sınıf" }) + " · " +
                    (if (reviewOnly) "Temel tekrar" else "Sınıfına uygun ilk dersler"),
                color = HomeColors.StarGold, fontWeight = FontWeight.Bold, fontSize = 13.sp
            )
            Text(
                "Önce kısa bir konuyu öğren, dört soruyla dene, sonra macerada uygula.",
                color = Color(0xFFD5ECF2), fontSize = 13.sp, lineHeight = 19.sp
            )
            Text(
                "${lessons.size} oynanabilir kısa ders · İlerlemen cihazında saklanır",
                color = Color(0xFF9DEBDD), fontSize = 12.sp
            )
            if (reviewOnly) {
                Text(
                    "Bu sınıflarda henüz tam müfredat bulunmuyor; gösterilen içerik temel tekrar içindir.",
                    color = Color(0xFFFFD89A), fontSize = 11.sp, lineHeight = 16.sp
                )
            }
        }

        // Most useful action first: actual grade-aware playable practice.
        LearningPracticeSection(userClass = userClass)

        // Parents can open deeper learning evidence without burying lessons.
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(HomeColors.CardSurface, RoundedCornerShape(17.dp))
                .border(1.dp, HomeColors.CardBorderSubtle, RoundedCornerShape(17.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable { reportExpanded = !reportExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BarChart, contentDescription = null,
                        tint = HomeColors.MagicCyan, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Gelişim Raporlarım", color = HomeColors.TextPrimary,
                            fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Gerçek oyun denemeleri ve tekrar önerileri",
                            color = HomeColors.TextSecondary, fontSize = 11.sp)
                    }
                }
                Icon(Icons.Filled.ChevronRight,
                    contentDescription = if (reportExpanded) "Raporu kapat" else "Raporu aç",
                    tint = HomeColors.MagicCyan)
            }
            if (reportExpanded) {
                AdventureLearningReport(userClass = userClass, refreshKey = 0)
                Spacer(Modifier.height(8.dp))
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
                .background(Color(0xFF123B58), RoundedCornerShape(17.dp))
                .border(1.dp, HomeColors.MagicCyan.copy(alpha = 0.35f),
                    RoundedCornerShape(17.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text("Öğrendiğini Oyunda Kullan", color = Color.White,
                fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("Derslerin ardından matematik maceralarıyla farklı sorular çöz.",
                color = HomeColors.TextSecondary, fontSize = 12.sp)
            Button(
                onClick = onNavigateToGames, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HomeColors.MagicCyan)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null,
                    tint = Color(0xFF09283B))
                Spacer(Modifier.width(6.dp))
                Text("Oyunlara Geç", color = Color(0xFF09283B),
                    fontWeight = FontWeight.Bold)
            }
        }

        Text(
            "Yeni konular hazır oldukça buraya eklenecek. Henüz oynanamayan " +
                "dersleri tamamlanmış içerik gibi göstermiyoruz.",
            color = HomeColors.TextSecondary, fontSize = 11.sp, lineHeight = 16.sp
        )
        Spacer(Modifier.height(24.dp))
    }
}
