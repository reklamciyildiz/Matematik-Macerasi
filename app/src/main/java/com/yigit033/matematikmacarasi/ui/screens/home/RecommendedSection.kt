package com.yigit033.matematikmacarasi.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAvi2Activity

/**
 * Onaylı Referansa Sadık "Senin İçin Önerilen" Bölümü (Sprint H2)
 *
 * Referans: reference/HOME_APPROVED_REFERENCE.png
 * - Beyaz/açık mavi büyük yumuşatılmış zemin yüzeyi
 * - İki canlı yatay öneri kartı (Sayı Avı oyun kartı & Katlar Konusu öğrenme kartı)
 * - Yatay kaydırma ile dar ekranda taşma önlenir
 * - Tıklamada gerçek oyun / öğrenme akışına yönlendirir
 */
@Composable
fun RecommendedSection(
    playerAge: Int,
    userClass: String,
    onNavigateToLearn: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

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
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Başlık Satırı (Referanstaki 🎯 "Senin İçin Önerilen" + "Tümünü Gör ▶")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.TrackChanges,
                        contentDescription = null,
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Senin İçin Önerilen",
                        color = HomeColors.TextDark,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { onNavigateToLearn() }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Tümünü Gör",
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

            Spacer(modifier = Modifier.height(12.dp))

            // İki Canlı Yatay Öneri Kartı (Yatay kaydırma ile)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Önerilen Oyun: Sayı Avı (Referanstaki sahil/ada temalı kart)
                item {
                    RecommendedGameCard(
                        title = "Sayı Avı",
                        subtitle = "Katları keşfet ve puan topla!",
                        badge = "Oyun",
                        onPlayClick = {
                            val intent = Intent(context, SayiAvi2Activity::class.java)
                            context.startActivity(intent)
                        }
                    )
                }

                // 2. Önerilen Öğrenme: Katlar Konusu (Referanstaki mor kitaplı kart)
                item {
                    RecommendedLearnCard(
                        title = "Katlar Konusu",
                        subtitle = "Eğlenceli anlatımlar ve alıştırmalar",
                        badge = "Öğren",
                        onLearnClick = onNavigateToLearn
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendedGameCard(
    title: String,
    subtitle: String,
    badge: String,
    onPlayClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(144.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF065F46), Color(0xFF047857), Color(0xFF0D9488))
                )
            )
            .border(1.5.dp, Color(0xFF34D399).copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        // Arka plan resim dokusu
        Image(
            painter = painterResource(id = R.drawable.card_number_hunt),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 0.25f,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Rozet
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF10B981))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = subtitle,
                    color = Color(0xFFE6FFFA),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            // Canlı Mavi "▶ Hemen Oyna" Butonu (Referanstaki gibi)
            Button(
                onClick = onPlayClick,
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0284C7)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8)),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(34.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Hemen Oyna",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun RecommendedLearnCard(
    title: String,
    subtitle: String,
    badge: String,
    onLearnClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(260.dp)
            .height(144.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF581C87), Color(0xFF7E22CE), Color(0xFF9333EA))
                )
            )
            .border(1.5.dp, Color(0xFFC084FC).copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                // Rozet
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF7C3AED))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badge,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = subtitle,
                    color = Color(0xFFF3E8FF),
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }

            // Canlı Mor "▶ Şimdi Öğren" Butonu (Referanstaki gibi)
            Button(
                onClick = onLearnClick,
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9333EA)
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD8B4FE)),
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(34.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoStories,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Şimdi Öğren",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
