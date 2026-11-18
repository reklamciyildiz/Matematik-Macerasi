package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Onaylı Referansa Sadık "Gelişimim" Bölümü (Sprint H2)
 *
 * Referans: reference/HOME_APPROVED_REFERENCE.png
 * - Açık renkli büyük kart yüzeyi
 * - 4 pastel renkli istatistik kutusu (Sarı, Mavi, Yeşil, Pembe)
 * - Yalnızca gerçek doğrulanmış veriler (yıldız, tamamlanan oyun, puan, görevler)
 * - "Detayları Gör ▶" ile ProfileActivity bağlantısı
 */
@Composable
fun ProgressOverviewCard(
    totalScore: Int,
    completedGamesCount: Int,
    userStars: Int,
    dailyTasksCompleted: Int,
    dailyTasksTotal: Int,
    onViewDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Split the previous deeply nested Compose tree into small composable
    // methods. Same children, spacing, colors and click behavior, but Kotlin
    // bytecode generation no longer has to optimize one enormous lambda.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFF0F172A).copy(alpha = 0.12f)
            )
            .clip(RoundedCornerShape(24.dp))
            .background(HomeColors.LightSectionSurface)
            .border(1.dp, HomeColors.LightSectionBorder, RoundedCornerShape(24.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ProgressOverviewHeader(onViewDetailsClick = onViewDetailsClick)
            Spacer(modifier = Modifier.height(12.dp))
            ProgressOverviewStats(
                totalScore = totalScore,
                completedGamesCount = completedGamesCount,
                userStars = userStars,
                dailyTasksCompleted = dailyTasksCompleted,
                dailyTasksTotal = dailyTasksTotal
            )
        }
    }
}

@Composable
private fun ProgressOverviewHeader(onViewDetailsClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.BarChart,
                contentDescription = null,
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Gelişimim",
                color = HomeColors.TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { onViewDetailsClick() }
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            Text(
                text = "Detayları Gör",
                color = Color(0xFF0284C7),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(2.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun ProgressOverviewStats(
    totalScore: Int,
    completedGamesCount: Int,
    userStars: Int,
    dailyTasksCompleted: Int,
    dailyTasksTotal: Int
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PastelStatCard(
                title = "Toplam Yıldız",
                value = userStars.toString(),
                icon = Icons.Filled.Star,
                iconTint = HomeColors.StarAmber,
                bgColor = HomeColors.StatAmberBg,
                borderColor = HomeColors.StatAmberBorder,
                textColor = HomeColors.StatAmberText,
                modifier = Modifier.weight(1f)
            )
            PastelStatCard(
                title = "Tamamlanan Oyun",
                value = completedGamesCount.toString(),
                icon = Icons.Filled.EmojiEvents,
                iconTint = Color(0xFF2563EB),
                bgColor = HomeColors.StatBlueBg,
                borderColor = HomeColors.StatBlueBorder,
                textColor = HomeColors.StatBlueText,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PastelStatCard(
                title = "Toplam Puan",
                value = totalScore.toString(),
                icon = Icons.Filled.SignalCellularAlt,
                iconTint = Color(0xFF059669),
                bgColor = HomeColors.StatGreenBg,
                borderColor = HomeColors.StatGreenBorder,
                textColor = HomeColors.StatGreenText,
                modifier = Modifier.weight(1f)
            )
            DailyGoalStatCard(
                completed = dailyTasksCompleted,
                total = dailyTasksTotal,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DailyGoalStatCard(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier
) {
    val target = total.coerceAtLeast(1)
    val progress = (completed.toFloat() / target.toFloat()).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(HomeColors.StatPinkBg)
            .border(1.2.dp, HomeColors.StatPinkBorder, RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.TrackChanges,
                    contentDescription = null,
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$completed / $target",
                    color = HomeColors.StatPinkText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Günün Hedefi",
                color = HomeColors.TextDarkMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color(0xFFE11D48),
                trackColor = Color(0xFFFECDD3),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun PastelStatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color,
    borderColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.2.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = value,
                    color = textColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = title,
                    color = HomeColors.TextDarkMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
            }
        }
    }
}
