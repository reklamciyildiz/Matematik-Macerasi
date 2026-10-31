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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.DailyTask

/**
 * Onaylı Referansa Sadık "Bugünkü Görevin" Parşömen Kartı (Sprint H2)
 *
 * Referans: reference/HOME_APPROVED_REFERENCE.png
 * - Sıcak krem/parşömen panel stili (ParchmentBg gradient + altın/kahve çerçeve).
 * - Pusula ikonu ve "Bugünkü Görevin" başlığı.
 * - Dinamik gerçek günlük görev başlığı ve açıklaması.
 * - Büyük canlı yeşil "Göreve Başla ▶" butonu.
 * - Gerçek oyuna doğrudan yönlendirme.
 */
@Composable
fun DailyQuestHeroCard(
    heroTask: DailyTask?,
    hasUncompletedTasks: Boolean,
    onLaunchTask: (DailyTask) -> Unit,
    onViewAllTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = Color(0xFF1E3A8A).copy(alpha = 0.35f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(HomeColors.ParchmentGradient)
            .border(
                width = 2.dp,
                color = HomeColors.ParchmentBorder,
                shape = RoundedCornerShape(22.dp)
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Üst Rozet: Pusula + "Bugünkü Görevin"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Pusula İkon Çerçevesi
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E3A8A))
                        .border(1.dp, HomeColors.StarGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Explore,
                        contentDescription = null,
                        tint = HomeColors.StarGold,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "Bugünkü Görevin",
                    color = HomeColors.ParchmentTitleDark,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.3.sp
                )

                if (heroTask != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(Color(0xFFFEF3C7))
                            .border(1.dp, HomeColors.StarAmber.copy(alpha = 0.6f), RoundedCornerShape(50.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = HomeColors.StarAmber,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "+${heroTask.reward}",
                            color = HomeColors.StatAmberText,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 2. Dinamik Görev Başlığı (Referanstaki enerjik tipografi)
            if (heroTask != null) {
                Text(
                    text = heroTask.description,
                    color = HomeColors.ParchmentTitleBlue,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    lineHeight = 21.sp,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Alt açıklama
                Text(
                    text = "Görevini tamamla, puanları ve yıldızları kazan!",
                    color = HomeColors.ParchmentSubtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    lineHeight = 14.sp
                )

                // İlerleme Çubuğu (Gerekiyorsa)
                val isDone = heroTask.isCompleted
                val progress = if (heroTask.targetCount > 0) {
                    (heroTask.currentCount.toFloat() / heroTask.targetCount.toFloat()).coerceIn(0f, 1f)
                } else 0f

                if (heroTask.targetCount > 1) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(0.9f),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${heroTask.currentCount} / ${heroTask.targetCount} Yapıldı",
                            color = HomeColors.ParchmentSubtitle,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = HomeColors.QuestGreenTop,
                        trackColor = Color(0xFFE2D4B7),
                        strokeCap = StrokeCap.Round
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Büyük Canlı Yeşil Görev Butonu (Referanstaki "Göreve Başla ▶")
                if (!isDone) {
                    Button(
                        onClick = { onLaunchTask(heroTask) },
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(48.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(50.dp),
                                spotColor = HomeColors.QuestGreenShadow
                            ),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = HomeColors.QuestGreenTop
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, HomeColors.QuestGreenBorder),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Göreve Başla",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onViewAllTasks,
                        modifier = Modifier
                            .fillMaxWidth(0.95f)
                            .height(46.dp),
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF15803D)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Tamamlandı! Harikasın 🎉",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                // Görev Yok Durumu
                Text(
                    text = "Yeni Görevler Yolda!",
                    color = HomeColors.ParchmentTitleDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aşağıdaki oyunlardan birini seçerek maceraya devam et.",
                    color = HomeColors.ParchmentSubtitle,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
