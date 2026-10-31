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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R

/**
 * Sayı Avı 2.0 Görev Brifingi Ekranı.
 * Explorer karakteri ve sadık köpeği ile kısa ve pedagojik görev anlatımı.
 */
@Composable
fun SayiAviBriefingScreen(
    starsCount: Int,
    onStartStageA: () -> Unit,
    onBackClick: () -> Unit,
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
                            Color.Black.copy(alpha = 0.60f),
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
                title = "Bölge 1: Kumsal Koyu",
                stageSubtitle = "Görev Brifingi",
                currentStageNumber = 1,
                mapPiecesCollected = 0,
                starsCount = starsCount,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Karakterler Rozeti (Explorer Map + Dog)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                // Sadık Köpek (<100dp)
                Surface(
                    color = SayiAviColors.OceanNavy.copy(alpha = 0.85f),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(2.dp, SayiAviColors.GoldBorder),
                    modifier = Modifier
                        .size(76.dp)
                        .shadow(8.dp, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.companion_dog),
                        contentDescription = "Sadık Dost",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(60.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Kaşif Harita Okurken (<=90dp)
                Surface(
                    color = SayiAviColors.OceanNavy.copy(alpha = 0.85f),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(2.dp, SayiAviColors.GoldBorder),
                    modifier = Modifier
                        .size(90.dp)
                        .shadow(10.dp, CircleShape)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.explorer_map),
                        contentDescription = "Kaşif Çocuk",
                        modifier = Modifier
                            .padding(8.dp)
                            .size(74.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Parşömen Görev Metni Kartı
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                ParchmentCard {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🧭",
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kayıp Harita İpuçları",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = SayiAviColors.TextNavy
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Korsanların gizlediği hazine sandığına ulaşmak için haritanın 2 parçasını birleştirmeliyiz!",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SayiAviColors.TextNavy,
                            lineHeight = 20.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "1. Aşama: Kabukları üçerli kümelere ayır. Sonra sayı tahtası, kasa şifresi, hazine rotası ve son mühür seni bekliyor!",
                            fontSize = 13.sp,
                            color = SayiAviColors.TextNavyMuted,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Pedagojik Hatırlatıcı Kutu
                        Surface(
                            color = SayiAviColors.SkyLightBlue.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SayiAviColors.OceanBlue)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "💡 Ritmik Sayma Kuralı:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SayiAviColors.OceanBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "3'ten başlayıp her seferinde 3 ekleyebilirsin. Şimdi tahtada bu kuralı kullan!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SayiAviColors.TextNavy
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Başla Butonu
                        AdventureActionButton(
                            text = "Görevi Başlat ▶",
                            onClick = onStartStageA,
                            gradient = SayiAviColors.EmeraldButtonGradient
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

