package com.yigit033.matematikmacarasi.games.sayiavi2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R

/**
 * Stage C: Hazine Rotası Ekranı.
 * - Arka plan: `route_ocean_backdrop.webp`
 * - Çocuk doğru adımları 6 ➔ 9 ➔ 12 ➔ 15 sıralayarak hazine adasına ulaşır.
 * - Ziyaret edilen adalar arasında dinamik Compose Canvas çizgisi çizilir.
 * - Tıklamalar geri alınabilir (Undo).
 * - Son adım hazine sahnesini ve başarı ekranını açar.
 */
@Composable
fun StageCScreen(
    routeData: SayiAviEngine.StageCRouteData,
    starsCount: Int,
    onStageCCompleted: () -> Unit,
    initialStepIndex: Int,
    onRouteProgress: (Int) -> Unit,
    onAttemptRecorded: (Boolean) -> Unit,
    onHintShown: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Ziyaret edilen düğümlerin kimlikleri (sırayla)
    var visitedNodeIds by remember(routeData) { mutableStateOf(
        routeData.nodes.filter { it.isTarget }.sortedBy { it.targetStepIndex }
            .take(initialStepIndex).map { it.id }
    ) }
    var currentStepIndex by remember(routeData) { mutableStateOf(initialStepIndex) }
    var errorNodeId by remember { mutableStateOf<Int?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isRouteComplete by remember(routeData) { mutableStateOf(initialStepIndex >= 4) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SayiAviColors.OceanDeep)
    ) {
        // Arka Plan: route_ocean_backdrop.webp
        Image(
            painter = painterResource(id = R.drawable.route_ocean_backdrop),
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
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // Üst Çubuk
            SayiAviTopBar(
                title = "Aşama 4: Hazine Rotası",
                stageSubtitle = "Kumsal Koyu (4/5)",
                currentStageNumber = 4,
                mapPiecesCollected = 2,
                starsCount = starsCount,
                onBackClick = onBackClick
            )

            // Rota Hedef Kartı
            Surface(
                color = SayiAviColors.OceanNavy.copy(alpha = 0.90f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, SayiAviColors.GoldBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "🗺️ ${routeData.targetSequence.first()} ile başla, üçer ilerle",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SayiAviColors.GoldBright
                        )
                        Text(
                            text = if (isRouteComplete) {
                                "Hazineye Ulaşıldı! Sandığı Aç ▶"
                            } else {
                                if (currentStepIndex == 0) "Başlangıç sayısını bul" else "Son doğru sayıya 3 ekle"
                            },
                            fontSize = 12.sp,
                            color = if (isRouteComplete) SayiAviColors.EmeraldGreen else SayiAviColors.SkyLightBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Geri Al Butonu (Undo)
                    if (visitedNodeIds.isNotEmpty() && !isRouteComplete) {
                        IconButton(
                            onClick = {
                                if (visitedNodeIds.isNotEmpty()) {
                                    visitedNodeIds = visitedNodeIds.dropLast(1)
                                    currentStepIndex = visitedNodeIds.size
                                    onRouteProgress(currentStepIndex)
                                    errorNodeId = null
                                    feedbackMessage = null
                                }
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(SayiAviColors.SunsetOrange.copy(alpha = 0.3f), CircleShape)
                                .border(1.dp, SayiAviColors.SunsetOrange, CircleShape)
                        ) {
                            Text(text = "↩", fontSize = 16.sp, color = Color.White)
                        }
                    }
                }
            }

            // Hata Banner'ı
            AnimatedVisibility(visible = feedbackMessage != null && !isRouteComplete) {
                Surface(
                    color = SayiAviColors.CoralRed.copy(alpha = 0.90f),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = feedbackMessage ?: "",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // İnteraktif Okyanus Haritası (Canvas Çizgileri ve Ada Taşları)
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val areaWidth = maxWidth
                val areaHeight = maxHeight

                // 1. Ziyaret edilen adalar arasındaki dinamik Compose Canvas çizgileri
                Canvas(modifier = Modifier.fillMaxSize()) {
                    if (visitedNodeIds.size >= 2) {
                        for (i in 0 until visitedNodeIds.size - 1) {
                            val startNode = routeData.nodes.find { it.id == visitedNodeIds[i] }
                            val endNode = routeData.nodes.find { it.id == visitedNodeIds[i + 1] }

                            if (startNode != null && endNode != null) {
                                val startX = startNode.xPercent * size.width
                                val startY = startNode.yPercent * size.height
                                val endX = endNode.xPercent * size.width
                                val endY = endNode.yPercent * size.height

                                // Parlak Altın Rota Çizgisi
                                drawLine(
                                    color = Color(0xFFFFD700),
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 6.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                                // Çizgiye derinlik veren gölge
                                drawLine(
                                    color = Color(0xFFD4AF37).copy(alpha = 0.6f),
                                    start = Offset(startX, startY),
                                    end = Offset(endX, endY),
                                    strokeWidth = 10.dp.toPx(),
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                }

                // 2. Taş Düğümleri (Interactive Stepping Stones)
                routeData.nodes.forEach { node ->
                    val isVisited = node.id in visitedNodeIds
                    val isError = node.id == errorNodeId
                    val isTarget = node.isTarget

                    // Koordinat hesaplama (dp cinsinden offset)
                    val offsetX = (areaWidth * node.xPercent) - 34.dp
                    val offsetY = (areaHeight * node.yPercent) - 34.dp

                    Box(
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY)
                            .size(68.dp)
                            .shadow(
                                elevation = if (isVisited) 10.dp else 4.dp,
                                shape = CircleShape
                            )
                            .clip(CircleShape)
                            .background(
                                when {
                                    isVisited -> SayiAviColors.EmeraldGreen
                                    isError -> SayiAviColors.CoralRed
                                    else -> SayiAviColors.OceanNavy.copy(alpha = 0.90f)
                                }
                            )
                            .border(
                                width = if (isVisited) 2.5.dp else 1.5.dp,
                                color = if (isVisited) SayiAviColors.GoldBright else SayiAviColors.GoldBorder,
                                shape = CircleShape
                            )
                            .clickable(
                                enabled = !isVisited && !isRouteComplete,
                                onClick = {
                                    val stepResult = SayiAviEngine.evaluateStageCStep(
                                        clickedNodeId = node.id,
                                        currentStepIndex = currentStepIndex,
                                        data = routeData
                                    )
                                    onAttemptRecorded(stepResult.isValidStep)
                                    if (!stepResult.isValidStep) onHintShown()

                                    if (stepResult.isValidStep) {
                                        visitedNodeIds = visitedNodeIds + node.id
                                        currentStepIndex++
                                        onRouteProgress(currentStepIndex)
                                        errorNodeId = null
                                        feedbackMessage = stepResult.feedbackMessage

                                        if (stepResult.isSequenceComplete) {
                                            isRouteComplete = true
                                        }
                                    } else {
                                        errorNodeId = node.id
                                        feedbackMessage = stepResult.feedbackMessage
                                    }
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = node.value.toString(),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isVisited -> Color.White
                                isError -> Color.White
                                else -> SayiAviColors.GoldBright
                            }
                        )
                    }
                }

                // Hazine Sandığı Rozeti (Rotanın bitişi: 15 taşının yanında üst sağda)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 10.dp, end = 16.dp)
                        .size(80.dp)
                        .shadow(12.dp, CircleShape)
                        .clip(CircleShape)
                        .background(SayiAviColors.OceanNavy.copy(alpha = 0.9f))
                        .border(2.dp, SayiAviColors.GoldBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isRouteComplete) R.drawable.chest_open else R.drawable.chest_closed
                        ),
                        contentDescription = "Hazine Sandığı",
                        modifier = Modifier.size(56.dp)
                    )
                }
            }

            // Alt Eylem Alanı: Hazineye ulaşıldığında buton görünür
            AnimatedVisibility(visible = isRouteComplete) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SayiAviColors.BottomBarScrim)
                        .padding(16.dp)
                ) {
                    AdventureActionButton(
                        text = "🔑 Hazine Mührüne Geç ▶",
                        onClick = onStageCCompleted,
                        gradient = SayiAviColors.OrangeButtonGradient
                    )
                }
            }
        }
    }
}

