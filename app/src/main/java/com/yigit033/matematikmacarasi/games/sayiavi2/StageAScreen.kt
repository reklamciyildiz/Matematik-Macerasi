package com.yigit033.matematikmacarasi.games.sayiavi2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
 * Stage A: 4x4 Sayı Tahtası Ekranı.
 * - 16 benzersiz sayı
 * - Tam 5 adet 3'ün katı (hedef)
 * - 11 adet çeldirici
 * - 5 seçim yapılana kadar 'Kontrol Et' butonu pasiftir.
 * - Hatalı seçimlerde pedagojik kademeli ipuçları ve düzeltme imkanı sunulur.
 */
@Composable
fun StageAScreen(
    gridData: SayiAviEngine.StageAGridData,
    starsCount: Int,
    onStageACompleted: () -> Unit,
    initialAttempts: Int,
    onAttemptRecorded: (Boolean) -> Unit,
    onHintShown: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedNumbers by remember { mutableStateOf(setOf<Int>()) }
    var errorNumbers by remember { mutableStateOf(setOf<Int>()) }
    var attemptCount by remember(gridData) { mutableStateOf(initialAttempts) }
    var showHintDialog by remember { mutableStateOf(false) }
    var currentHintText by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SayiAviColors.OceanDeep)
    ) {
        // Arka Plan: number_board_backdrop.webp
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
                            Color.Black.copy(alpha = 0.50f),
                            Color.Black.copy(alpha = 0.20f),
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
                title = "Aşama 1: Sayı Tahtası",
                stageSubtitle = "Kumsal Koyu (1/3)",
                currentStageNumber = 1,
                mapPiecesCollected = 0,
                starsCount = starsCount,
                onBackClick = onBackClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Soru & İlerleme Kartı
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Surface(
                    color = SayiAviColors.OceanNavy.copy(alpha = 0.90f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, SayiAviColors.GoldBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "🎯 3'ün Katı Olan 5 Taş Seç!",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = SayiAviColors.GoldBright
                            )
                            Text(
                                text = "Seçilen Taş Sayısı: ${selectedNumbers.size} / 5",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (selectedNumbers.size == 5) SayiAviColors.EmeraldGreen else SayiAviColors.SkyLightBlue
                            )
                        }

                        // İpucu Butonu
                        IconButton(
                            onClick = {
                                currentHintText = if (attemptCount == 0) {
                                    "💡 İpucu: Üçer ritmik sayarak tahtadaki sayıları kontrol et."
                                } else {
                                    "💡 3'ün katları 3'e kalansız bölünür. Taşlarını bu kuralla tekrar kontrol et."
                                }
                                showHintDialog = true
                                onHintShown()
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .background(SayiAviColors.SunsetOrange.copy(alpha = 0.25f), CircleShape)
                                .border(1.dp, SayiAviColors.SunsetOrange, CircleShape)
                        ) {
                            Text(text = "💡", fontSize = 18.sp)
                        }
                    }
                }

                // Hata Uyarısı Banner'ı (varsa)
                AnimatedVisibility(visible = errorMessage != null) {
                    Surface(
                        color = SayiAviColors.CoralRed.copy(alpha = 0.90f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "⚠️", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = errorMessage ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4x4 Izgara (Grid)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, RoundedCornerShape(20.dp))
                        .clip(RoundedCornerShape(20.dp))
                        .background(SayiAviColors.OceanNavy.copy(alpha = 0.75f))
                        .border(2.dp, SayiAviColors.GoldBorder, RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        for (row in 0 until 4) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                for (col in 0 until 4) {
                                    val index = row * 4 + col
                                    val number = gridData.numbers[index]
                                    val isSelected = number in selectedNumbers
                                    val isError = number in errorNumbers

                                    val visualState = when {
                                        isError -> NumberTileVisualState.ERROR
                                        isSelected -> NumberTileVisualState.SELECTED
                                        else -> NumberTileVisualState.NORMAL
                                    }

                                    NumberTileView(
                                        number = number,
                                        visualState = visualState,
                                        onClick = {
                                            // Hata işaretini kaldır
                                            if (isError) {
                                                errorNumbers = errorNumbers - number
                                            }
                                            errorMessage = null

                                            if (isSelected) {
                                                selectedNumbers = selectedNumbers - number
                                            } else {
                                                if (selectedNumbers.size < 5) {
                                                    selectedNumbers = selectedNumbers + number
                                                } else {
                                                    errorMessage = "En fazla 5 taş seçebilirsin! Başka bir taş seçmek için önce bir taşın seçimini kaldır."
                                                }
                                            }
                                        },
                                        size = 68.dp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Kontrol Et Butonu (Sadece 5 taş seçildiğinde aktif)
                val isCheckEnabled = selectedNumbers.size == 5

                AdventureActionButton(
                    text = if (isCheckEnabled) "Cevabı Kontrol Et ▶" else "5 Taş Seç (${selectedNumbers.size}/5)",
                    enabled = isCheckEnabled,
                    gradient = SayiAviColors.EmeraldButtonGradient,
                    onClick = {
                        val result = SayiAviEngine.evaluateStageA(
                            selected = selectedNumbers,
                            gridData = gridData,
                            attemptIndex = attemptCount
                        )
                        attemptCount++
                        onAttemptRecorded(result.isSuccess)

                        if (result.isSuccess) {
                            errorNumbers = emptySet()
                            errorMessage = null
                            showSuccessDialog = true
                        } else {
                            errorNumbers = result.wrongSelections.toSet()
                            errorMessage = result.feedbackMessage
                            if (result.pedagogicalHint != null) {
                                currentHintText = result.pedagogicalHint
                                showHintDialog = true
                                onHintShown()
                            }
                        }
                    }
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        // İpucu Diyaloğu
        if (showHintDialog && currentHintText != null) {
            AlertDialog(
                onDismissRequest = { showHintDialog = false },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "💡", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Kaşif İpucu",
                            fontWeight = FontWeight.Bold,
                            color = SayiAviColors.TextNavy
                        )
                    }
                },
                text = {
                    Text(
                        text = currentHintText ?: "",
                        fontSize = 14.sp,
                        color = SayiAviColors.TextNavy,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { showHintDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = SayiAviColors.OceanBlue)
                    ) {
                        Text("Anladım", color = Color.White)
                    }
                },
                containerColor = SayiAviColors.ParchmentLight,
                shape = RoundedCornerShape(16.dp)
            )
        }

        // Başarı & Harita Parçası Kazanma Diyaloğu
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = {}, // Kullanıcı butona basarak ilerlemeli
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🎉", fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1. Harita Parçası Bulundu!",
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
                        Image(
                            painter = painterResource(id = R.drawable.map_piece),
                            contentDescription = "Harita Parçası",
                            modifier = Modifier
                                .size(80.dp)
                                .shadow(8.dp, CircleShape)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Harika iş! 3'ün katı olan 5 gizli sayıyı başarıyla buldun. Şimdi ikinci harita parçasını bulmak için Şifre Kasası'nı çözmelisin!",
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
                            onStageACompleted()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SayiAviColors.EmeraldGreen),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Şifre Kasasına İlerle ▶",
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
