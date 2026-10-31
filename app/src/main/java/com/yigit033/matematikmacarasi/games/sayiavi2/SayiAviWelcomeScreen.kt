package com.yigit033.matematikmacarasi.games.sayiavi2

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
 * Sayı Avı 2.0 Karşılama ve Bölge Seçim Ekranı.
 * GDD v1.0 kuralı: Tek açık oyun bölümü (Bölge 1: Kumsal Koyu);
 * sonraki dünyalar (Orman, Tapınak) 'yakında' etiketiyle net olarak kilitli gösterilir, sahte tıklanabilir buton yapılmaz.
 */
@Composable
fun SayiAviWelcomeScreen(
    isCompletedBefore: Boolean,
    totalCompletions: Int,
    starsCount: Int,
    onStartRegion1: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SayiAviColors.OceanDeep)
    ) {
        // Arka Plan Sahnesi
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
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.70f)
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
                title = "Sayı Avı 2.0",
                stageSubtitle = "Bölge Seçimi",
                currentStageNumber = 0,
                mapPiecesCollected = if (isCompletedBefore) 2 else 0,
                starsCount = starsCount,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Başlık & Hoş Geldin Kartı
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Surface(
                    color = SayiAviColors.OceanNavy.copy(alpha = 0.90f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, SayiAviColors.GoldBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.treasure_map_art),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.5.dp, SayiAviColors.GoldBorder, RoundedCornerShape(12.dp))
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Kayıp Hazine Macerası",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = SayiAviColors.TextWhite
                                )
                                Text(
                                    text = "3'ün Katları & Ritmik Sayma",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SayiAviColors.SkyLightBlue
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Gizemli adanın hazinesini bulmak için 3 aşamalı görevi tamamla: Sayı Avı tahtasından 3'ün katlarını bul, Şifre Kasasını aç ve Hazine Rotasını keşfet!",
                            fontSize = 13.sp,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )

                        if (isCompletedBefore) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = SayiAviColors.EmeraldGreen.copy(alpha = 0.20f),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SayiAviColors.EmeraldGreen)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = SayiAviColors.GoldBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Bu ada tamamlandı! ($totalCompletions kez tamamlandı)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SayiAviColors.EmeraldGreen
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bölge Listesi Başlığı
                Text(
                    text = "KEŞİF BÖLGELERİ",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = SayiAviColors.GoldBright,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 1. Bölge: Kumsal Koyu (Açık ve Oynanabilir)
                RegionCard(
                    regionNumber = 1,
                    title = "Kumsal Koyu",
                    theme = "3'ün Katları & Kasa Şifresi",
                    statusText = if (isCompletedBefore) "Tekrar Oyna ▶" else "Maceraya Başla ▶",
                    isUnlocked = true,
                    isCompleted = isCompletedBefore,
                    nodeDrawableRes = R.drawable.island_node,
                    onClick = onStartRegion1
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Bölge: Gizemli Orman (Kilitli - Çok Yakında)
                RegionCard(
                    regionNumber = 2,
                    title = "Gizemli Orman",
                    theme = "4'ün Katları & Antik Köprü",
                    statusText = "Çok Yakında 🔒",
                    isUnlocked = false,
                    isCompleted = false,
                    nodeDrawableRes = R.drawable.forest_node,
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Bölge: Antik Tapınak (Kilitli - Çok Yakında)
                RegionCard(
                    regionNumber = 3,
                    title = "Antik Tapınak",
                    theme = "Çarpım Tablosu Zirvesi",
                    statusText = "Çok Yakında 🔒",
                    isUnlocked = false,
                    isCompleted = false,
                    nodeDrawableRes = R.drawable.temple_node,
                    onClick = {}
                )

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun RegionCard(
    regionNumber: Int,
    title: String,
    theme: String,
    statusText: String,
    isUnlocked: Boolean,
    isCompleted: Boolean,
    nodeDrawableRes: Int,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked, onClick = onClick)
            .shadow(if (isUnlocked) 8.dp else 2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) SayiAviColors.ParchmentLight else Color(0xFF1E293B).copy(alpha = 0.85f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isUnlocked) 2.dp else 1.dp,
            color = if (isUnlocked) SayiAviColors.GoldBorder else Color(0xFF475569)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Küçük Bölge Thumbnail
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        if (isUnlocked) SayiAviColors.SkyLightBlue.copy(alpha = 0.4f) else Color.DarkGray,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        1.dp,
                        if (isUnlocked) SayiAviColors.GoldBorder else Color.Gray,
                        RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = nodeDrawableRes),
                    contentDescription = title,
                    modifier = Modifier.size(52.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Bölge $regionNumber: $title",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isUnlocked) SayiAviColors.TextNavy else Color(0xFFCBD5E1)
                    )
                    if (isCompleted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = SayiAviColors.GoldBright,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = theme,
                    fontSize = 12.sp,
                    color = if (isUnlocked) SayiAviColors.TextNavyMuted else Color(0xFF94A3B8)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = statusText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) SayiAviColors.EmeraldDark else Color(0xFF94A3B8)
                )
            }

            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Kilitli",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Oyna",
                    tint = SayiAviColors.EmeraldGreen,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

