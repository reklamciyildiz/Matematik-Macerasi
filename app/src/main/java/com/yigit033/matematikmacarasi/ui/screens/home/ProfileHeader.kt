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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.components.AnimatedAvatarView
import com.yigit033.matematikmacarasi.models.Avatar

/**
 * Üst Profil Başlık Çubuğu (Sprint H2 — Referansa Sadık)
 *
 * Mavi gökyüzü / ada görseli üzerinde yüzen, yüksek kontrastlı ve okunabilir
 * gerçek oyuncu bilgilerini gösterir.
 */
@Composable
fun ProfileHeader(
    userName: String,
    userClass: String,
    currentAvatar: Avatar,
    userStars: Int,
    uncompletedTaskCount: Int,
    onProfileClick: () -> Unit,
    onDailyTasksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sol Taraf: Avatar ve Kullanıcı Bilgileri
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .clickable { onProfileClick() }
                .padding(2.dp)
        ) {
            // Avatar Çerçevesi + Altın Halka + Sınıf Rozeti
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F2642))
                        .border(2.5.dp, HomeColors.StarGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    AnimatedAvatarView(
                        avatar = currentAvatar,
                        size = 48.dp,
                        modifier = Modifier.size(48.dp)
                    )
                }

                // Küçük Altın Seviye/Sınıf Rozeti (Referanstaki "12" gibi)
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(HomeColors.StarGold)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (userClass.contains(".")) userClass.split(".")[0].trim() else "1",
                        color = Color(0xFF78350F),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Kullanıcı Adı ve İlerleme / Sınıf Hapı
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = userName.ifBlank { "Matematik Gezgini" },
                    color = Color.White,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Sınıf / Seviye Rozeti (Referanstaki sarı-mavi ilerleme hapı stili)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color(0xFF0A2540).copy(alpha = 0.82f))
                        .border(1.dp, Color(0xFF38BDF8).copy(alpha = 0.5f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = HomeColors.StarGold,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = userClass.ifBlank { "1. Sınıf" },
                        color = Color(0xFFE0F2FE),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Sağ Taraf: Yıldız Sayacı ve Günlük Görev Rozeti (Referanstaki gibi cam kapsül butonlar)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Yıldız Kapsülü (Referanstaki sarı yıldız + 245 + mavi artı butonu stili)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(Color(0xFF0E2A4A).copy(alpha = 0.85f))
                    .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), RoundedCornerShape(50.dp))
                    .clickable { onProfileClick() }
                    .padding(start = 8.dp, end = 5.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Yıldızlar",
                    tint = HomeColors.StarGold,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = userStars.toString(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Detay",
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }

            // Günlük Görev Takvim Kısayolu (Referanstaki kırmızı badge'li takvim butonu)
            BadgedBox(
                badge = {
                    if (uncompletedTaskCount > 0) {
                        Badge(
                            containerColor = Color(0xFFEF4444),
                            contentColor = Color.White,
                            modifier = Modifier.size(18.dp)
                        ) {
                            Text(
                                text = uncompletedTaskCount.toString(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0E2A4A).copy(alpha = 0.85f))
                        .border(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                        .clickable { onDailyTasksClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.DateRange,
                        contentDescription = "Günlük Görevler",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
