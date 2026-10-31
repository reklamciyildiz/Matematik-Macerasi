package com.yigit033.matematikmacarasi.games.sayiavi2

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 * Stage B: Şifre Kasası Ekranı.
 * - Örüntü: 6, 9, 12, ? -> Doğru cevap 15.
 * - Kapalı sandık (`chest_closed.webp`) doğru yanıtla açılır (`chest_open.webp`).
 * - 2. Harita Parçası kazanılır.
 */
@Composable
fun StageBScreen(
    vaultData: SayiAviEngine.StageBVaultData,
    starsCount: Int,
    onStageBCompleted: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedOption by remember { mutableStateOf<Int?>(null) }
    var wrongOption by remember { mutableStateOf<Int?>(null) }
    var isChestOpen by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var explanationText by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SayiAviColors.OceanDeep)
    ) {
        // Arka Plan
        Image(
            painter = painterResource(id = R.drawable.number_board_backdrop),
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
                title = "Aşama 2: Şifre Kasası",
                stageSubtitle = "Kumsal Koyu (2/3)",
                currentStageNumber = 2,
                mapPiecesCollected = if (isChestOpen) 2 else 1,
                starsCount = starsCount,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Sandık Görseli (Kapalı / Açık)
                Surface(
                    color = SayiAviColors.OceanNavy.copy(alpha = 0.85f),
                    shape = CircleShape,
                    border = androidx.compose.foundation.BorderStroke(2.dp, SayiAviColors.GoldBorder),
                    modifier = Modifier
                        .size(110.dp)
                        .shadow(12.dp, CircleShape)
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isChestOpen) R.drawable.chest_open else R.drawable.chest_closed
                        ),
                        contentDescription = if (isChestOpen) "Açık Kasa" else "Kapalı Kasa",
                        modifier = Modifier
                            .padding(14.dp)
                            .size(82.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Parşömen Soru Kartı
                ParchmentCard {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "🔐", fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Kasanın Şifresini Çöz",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = SayiAviColors.TextNavy
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Sandık kilitli! Kilidi açmak için 3'er artan sayı dizilimini tamamla:",
                            fontSize = 13.sp,
                            color = SayiAviColors.TextNavyMuted,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Örüntü Gösterimi: 6 ➔ 9 ➔ 12 ➔ [ ? ]
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            vaultData.sequence.forEach { number ->
                                Surface(
                                    color = SayiAviColors.OceanNavy,
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, SayiAviColors.GoldBorder),
                                    modifier = Modifier.size(52.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = number.toString(),
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = SayiAviColors.GoldBright
                                        )
                                    }
                                }

                                Text(
                                    text = " ➔ ",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SayiAviColors.TextNavyMuted
                                )
                            }

                            // Soru İşareti Taşı
                            Surface(
                                color = if (isChestOpen) SayiAviColors.EmeraldGreen else SayiAviColors.SunsetOrange,
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, SayiAviColors.GoldBorder),
                                modifier = Modifier.size(52.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = if (isChestOpen) vaultData.correctAnswer.toString() else "?",
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Fark Kuralı Açıklaması
                        Surface(
                            color = SayiAviColors.SkyLightBlue.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "+3 Kuralı: Sayılar her adımda 3 fazlasına ulaşıyor.",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = SayiAviColors.OceanBlue,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Hata & Pedagojik Açıklama Banner'ı
                AnimatedVisibility(visible = errorMessage != null) {
                    Surface(
                        color = SayiAviColors.CoralRed.copy(alpha = 0.95f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "❌", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            if (explanationText != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = explanationText ?: "",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFECEC)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Seçenek Taşları Başlığı
                Text(
                    text = "KASAYI AÇACAK SAYIYI SEÇ:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = SayiAviColors.GoldBright,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Seçenek Taşları
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    vaultData.options.forEach { option ->
                        val isWrong = option == wrongOption
                        val isSelected = option == selectedOption

                        val visualState = when {
                            isWrong -> NumberTileVisualState.ERROR
                            isSelected -> NumberTileVisualState.SELECTED
                            else -> NumberTileVisualState.NORMAL
                        }

                        NumberTileView(
                            number = option,
                            visualState = visualState,
                            onClick = {
                                selectedOption = option
                                val result = SayiAviEngine.evaluateStageB(option, vaultData)

                                if (result.isSuccess) {
                                    wrongOption = null
                                    errorMessage = null
                                    isChestOpen = true
                                    showSuccessDialog = true
                                } else {
                                    wrongOption = option
                                    errorMessage = result.feedbackMessage
                                    explanationText = result.pedagogicalExplanation
                                }
                            },
                            size = 72.dp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }

        // Başarı Diyaloğu
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {},
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎉", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "2. Harita Parçası Bulundu!",
                            fontWeight = FontWeight.Bold,
                            color = SayiAviColors.TextNavy
                        )
                    }
                },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.chest_open),
                                contentDescription = "Açık Kasa",
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Image(
                                painter = painterResource(id = R.drawable.map_piece),
                                contentDescription = "Harita Parçası",
                                modifier = Modifier.size(64.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Tebrikler! 12'den sonra 3 ekleyerek 15 şifresini buldun ve kasayı açtın! Haritanın iki parçası da birleşti. Şimdi Hazine Rotasına çıkarak sandığa ulaşabilirsin!",
                            fontSize = 13.sp,
                            color = SayiAviColors.TextNavyMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showSuccessDialog = false
                            onStageBCompleted()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SayiAviColors.EmeraldGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Hazine Rotasına Devam Et ▶",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                containerColor = SayiAviColors.ParchmentLight,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

