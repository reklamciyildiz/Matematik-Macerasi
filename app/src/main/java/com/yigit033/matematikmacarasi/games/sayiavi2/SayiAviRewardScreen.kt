package com.yigit033.matematikmacarasi.games.sayiavi2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R

/**
 * Sayı Avı 2.0 Başarı, Ödül ve Öğrenme Özeti Ekranı.
 * - Kazanılan Yıldız (İlk tamamlama ise 1 yıldız, tekrar oynamada 0 yıldız idempotency).
 * - Harita parçaları ve açık sandık kutlaması.
 * - Gerçek öğrenme çıktıları (kazanım, deneme, ipucu, süre).
 * - 'Yeniden Oyna' ve 'Oyunlar'a Dön' seçenekleri.
 */
@Composable
fun SayiAviRewardScreen(
    isFirstCompletion: Boolean,
    totalCompletions: Int,
    learningSummary: SayiAviLearningSummary?,
    timeSpentSeconds: Long,
    starsCount: Int,
    onReplayClick: () -> Unit,
    onReturnToGamesClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SayiAviColors.OceanDeep)
    ) {
        // Arka Plan
        Image(
            painter = painterResource(id = R.drawable.island_explorer_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Karartma Perdesi
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Black.copy(alpha = 0.35f),
                            Color.Black.copy(alpha = 0.75f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Üst Çubuk
            SayiAviTopBar(
                title = "Görev Tamamlandı!",
                stageSubtitle = "Kumsal Koyu Keşfedildi",
                currentStageNumber = 5,
                mapPiecesCollected = 2,
                starsCount = starsCount,
                onBackClick = onReturnToGamesClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Kutlama Rozeti (Açık Sandık + Yıldız)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = SayiAviColors.OceanNavy.copy(alpha = 0.85f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(2.dp, SayiAviColors.GoldBorder),
                        modifier = Modifier
                            .size(80.dp)
                            .shadow(10.dp, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.chest_open),
                            contentDescription = "Açık Hazine",
                            modifier = Modifier
                                .padding(10.dp)
                                .size(60.dp),
                            contentScale = ContentScale.Fit
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Surface(
                        color = SayiAviColors.OceanNavy.copy(alpha = 0.85f),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(2.dp, SayiAviColors.GoldBorder),
                        modifier = Modifier
                            .size(90.dp)
                            .shadow(12.dp, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.star_reward),
                            contentDescription = "Altın Yıldız",
                            modifier = Modifier
                                .padding(12.dp)
                                .size(66.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Parşömen Ödül ve Gelişim Paneli
                ParchmentCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "🎉 TEBRİKLER KAŞİF!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SayiAviColors.TextNavy,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Kumsal Koyunun kayıp hazinesine başarıyla ulaştın!",
                            fontSize = 13.sp,
                            color = SayiAviColors.TextNavyMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Yıldız Ödülü Bilgisi
                        Surface(
                            color = if (isFirstCompletion) SayiAviColors.EmeraldGreen.copy(alpha = 0.15f) else SayiAviColors.SkyLightBlue.copy(alpha = 0.20f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isFirstCompletion) SayiAviColors.EmeraldGreen else SayiAviColors.OceanBlue
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = SayiAviColors.GoldBright,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (isFirstCompletion) "+1 Altın Yıldız Kazanıldı!" else "Harika Bir Pratik!",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isFirstCompletion) SayiAviColors.EmeraldDark else SayiAviColors.OceanBlue
                                    )
                                    Text(
                                        text = if (isFirstCompletion) {
                                            "İlk tamamlama ödülü yıldız kumbarana eklendi."
                                        } else {
                                            "Bu ada daha önce tamamlandı ($totalCompletions. kez). Becerini pekiştirdin!"
                                        },
                                        fontSize = 11.sp,
                                        color = SayiAviColors.TextNavyMuted
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Öğrenme Çıktıları Başlığı
                        Text(
                            text = "📊 MATEMATİKSEL GELİŞİM ÖZETİ",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = SayiAviColors.OceanBlue,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Metrik Satırları
                        MetricItem(title = "Çalışılan Konu", value = "3'ün Katları")
                        if (learningSummary?.complete == true) {
                            MetricItem(title = "Doğru Hamle Oranı", value = "${learningSummary.actionAccuracyPercent}%")
                            if (learningSummary.counters.groupAttempts > 0)
                                MetricItem(title = "Kabuk Kümeleri", value = "${learningSummary.counters.groupAttempts} deneme")
                            MetricItem(title = "Sayı Tahtası", value = "${learningSummary.counters.aAttempts} kontrol")
                            MetricItem(title = "Şifre Kasası", value = "${learningSummary.counters.bAttempts} deneme")
                            MetricItem(title = "Hazine Rotası", value = "${learningSummary.counters.cAttempts} hamle")
                            if (learningSummary.counters.transferAttempts > 0)
                                MetricItem(title = "Hazine Mührü", value = "${learningSummary.counters.transferAttempts} deneme")
                            MetricItem(title = "Yanlış Hamleler", value = "${learningSummary.errors}")
                            MetricItem(title = "Gösterilen İpuçları", value = "${learningSummary.counters.hintsShown}")
                            MetricItem(title = "İlk Denemede", value = "${learningSummary.firstTryStages}/${learningSummary.playedStages} aşama")
                        } else {
                            MetricItem(title = "Hamle Verileri", value = "Eksik oturum")
                        }
                        MetricItem(title = "Geçen Süre", value = "$timeSpentSeconds sn")
                        if (learningSummary?.complete == true) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(color = SayiAviColors.SkyLightBlue.copy(alpha = 0.25f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    text = "🧭 Sıradaki öneri: ${learningSummary.practiceRecommendation}",
                                    modifier = Modifier.padding(12.dp),
                                    fontSize = 12.sp, fontWeight = FontWeight.Medium,
                                    color = SayiAviColors.TextNavy
                                )
                            }
                        }
                        Text(
                            text = "Bu oran yalnızca oyun içi hamleleri gösterir; öğrenmeyi kanıtlamaz. Oyun araları süreye dahil olabilir.",
                            fontSize = 11.sp, color = SayiAviColors.TextNavyMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Yeniden Oyna Butonu
                        AdventureActionButton(
                            text = "Yeniden Oyna 🔄",
                            onClick = onReplayClick,
                            gradient = SayiAviColors.EmeraldButtonGradient,
                            icon = Icons.Default.Refresh
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Oyunlar Menüsüne Dön Butonu
                        AdventureActionButton(
                            text = "Oyunlar Menüsüne Dön 🏠",
                            onClick = onReturnToGamesClick,
                            gradient = SayiAviColors.BlueButtonGradient,
                            icon = Icons.Default.Home
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
private fun MetricItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            color = SayiAviColors.TextNavyMuted
        )
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = SayiAviColors.TextNavy
        )
    }
}

