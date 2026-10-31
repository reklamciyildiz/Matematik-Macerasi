package com.yigit033.matematikmacarasi.games.sayiavi2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R

/**
 * Kumsal Koyu: üçer ritmik sayma becerisini nesne gruplama ve yeni bağlamlara aktarım ile genişletir.
 * Yalnızca mevcut sanat varlıklarını kullanır. Minimum dokunma hedefleri >= 48 dp.
 */
@Composable
private fun IslandChallengeFrame(
    title: String,
    subtitle: String,
    currentStep: Int,
    starsCount: Int,
    mapPieces: Int,
    backdrop: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize().background(SayiAviColors.OceanDeep)) {
        Image(
            painter = painterResource(id = backdrop),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier.fillMaxSize().background(
                Brush.verticalGradient(
                    listOf(
                        SayiAviColors.OceanNavy.copy(alpha = 0.84f),
                        SayiAviColors.OceanDeep.copy(alpha = 0.68f),
                        SayiAviColors.OceanDeep.copy(alpha = 0.95f)
                    )
                )
            )
        )
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SayiAviTopBar(
                title = title,
                stageSubtitle = subtitle,
                currentStageNumber = currentStep,
                mapPiecesCollected = mapPieces,
                starsCount = starsCount,
                onBackClick = onBackClick
            )
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = SayiAviColors.OceanNavy.copy(alpha = 0.90f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SayiAviColors.GoldBorder)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        AdventureSectionProgress(
                            label = "KUMSAL KOYU · GÖREV",
                            completed = currentStep,
                            total = 5,
                            accent = SayiAviColors.GoldBright
                        )
                    }
                }
                content()
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
fun ShellGroupingScreen(
    data: SayiAviEngine.ShellGrouping,
    starsCount: Int,
    onAttemptRecorded: (Boolean) -> Unit,
    onHintShown: () -> Unit,
    initialFinishedGroups: Int,
    initialPartialShells: Int,
    onGroupingProgress: (Int, Int) -> Unit,
    onCompleted: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var finishedGroups by remember(data) { mutableIntStateOf(initialFinishedGroups.coerceIn(0, data.groups)) }
    var partialShells by remember(data) {
        mutableIntStateOf(if (initialFinishedGroups >= data.groups) 0 else initialPartialShells.coerceIn(0, 2))
    }
    var isCorrect by remember(data) { mutableStateOf(false) }
    var showHint by remember(data) { mutableStateOf(false) }
    var feedback by remember(data) { mutableStateOf("") }
    var advanced by remember(data) { mutableStateOf(false) }
    val ready = finishedGroups == data.groups
    val shellsCount = finishedGroups * 3 + partialShells

    IslandChallengeFrame(
        title = "Kabuk Kümeleri",
        subtitle = "Üçer grupla ve keşfet",
        currentStep = 1,
        starsCount = starsCount,
        mapPieces = 0,
        backdrop = R.drawable.island_explorer_hero,
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        ParchmentCard {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("🐚 Kumsaldaki gizli iz", fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold, color = SayiAviColors.TextNavy)
                Text(
                    "Harita için ${data.groups} tane üçlü kabuk kümesi oluştur. Her kümede tam 3 kabuk olmalı.",
                    fontSize = 14.sp, lineHeight = 20.sp, color = SayiAviColors.TextNavy
                )
                Text("${finishedGroups} / ${data.groups} küme hazır",
                    fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SayiAviColors.OceanBlue)
                LinearProgressIndicator(
                    progress = { finishedGroups.toFloat() / data.groups },
                    modifier = Modifier.fillMaxWidth(),
                    color = SayiAviColors.EmeraldGreen
                )
                Column(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    repeat(data.groups) { index ->
                        val count = when {
                            index < finishedGroups -> 3
                            index == finishedGroups -> partialShells
                            else -> 0
                        }
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (count == 3) SayiAviColors.EmeraldGreen.copy(alpha = 0.16f)
                                    else Color.White.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp,
                                if (count == 3) SayiAviColors.EmeraldGreen
                                else SayiAviColors.GoldBorder.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(9.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Küme ${index + 1}", fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp, color = SayiAviColors.TextNavy)
                                Text(
                                    (0 until 3).joinToString("  ") { if (it < count) "🐚" else "○" },
                                    fontSize = 23.sp, color = SayiAviColors.TextNavy
                                )
                            }
                        }
                    }
                }
                if (!ready) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (finishedGroups < data.groups) {
                                    val next = partialShells + 1
                                    if (next == 3) {
                                        finishedGroups++
                                        partialShells = 0
                                    } else partialShells = next
                                    onGroupingProgress(finishedGroups, partialShells)
                                }
                            },
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SayiAviColors.EmeraldDark)
                        ) {
                            Text("+ Kabuk Ekle", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = {
                                if (partialShells > 0) partialShells--
                                else if (finishedGroups > 0) {
                                    finishedGroups--
                                    partialShells = 2
                                }
                                onGroupingProgress(finishedGroups, partialShells)
                            },
                            enabled = shellsCount > 0,
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SayiAviColors.OceanBlue)
                        ) { Text("Geri Al", color = Color.White) }
                    }
                }
                AnimatedVisibility(visible = ready) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Bütün kümeler hazır! Toplam kaç kabuk var?",
                            fontSize = 16.sp, fontWeight = FontWeight.Bold,
                            color = SayiAviColors.TextNavy)
                        data.choices.chunked(2).forEach { row ->
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                row.forEach { choice ->
                                    Button(
                                        onClick = {
                                            if (!isCorrect) {
                                                val correct = choice == data.total
                                                onAttemptRecorded(correct)
                                                if (correct) {
                                                    isCorrect = true
                                                    feedback = "${data.groups} × 3 = ${data.total}. Üçer sayarak buldun!"
                                                } else feedback = "Tekrar sayalım: 3, 6, 9..."
                                            }
                                        },
                                        enabled = !isCorrect,
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = SayiAviColors.OceanBlue
                                        )
                                    ) {
                                        Text("$choice", fontSize = 19.sp,
                                            color = Color.White, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        AdventureFeedbackBanner(
                            message = feedback,
                            positive = isCorrect,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (!isCorrect && !showHint) {
                            TextButton(onClick = { showHint = true; onHintShown() }) {
                                Text("💡 İpucu Göster", color = SayiAviColors.OceanBlue)
                            }
                        }
                        if (showHint && !isCorrect) Text(
                            "Her tamamladığın küme 3 kabuk demek. Küme sayısı kadar üçer say.",
                            fontSize = 12.sp, color = SayiAviColors.TextNavyMuted
                        )
                        if (isCorrect) AdventureActionButton(
                            text = "Sayı Tahtasına Geç ▶",
                            onClick = {
                                if (!advanced) {
                                    advanced = true
                                    onCompleted()
                                }
                            },
                            gradient = SayiAviColors.EmeraldButtonGradient
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TreasureSealScreen(
    challenges: List<SayiAviEngine.TransferChallenge>,
    starsCount: Int,
    onAttemptRecorded: (Boolean) -> Unit,
    onHintShown: () -> Unit,
    initialQuestionIndex: Int,
    onQuestionProgress: (Int) -> Unit,
    onCompleted: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    require(challenges.size == 3)
    var questionIndex by remember(challenges) { mutableIntStateOf(initialQuestionIndex.coerceIn(0, 2)) }
    var isCorrect by remember(challenges, questionIndex) { mutableStateOf(false) }
    var showHint by remember(challenges, questionIndex) { mutableStateOf(false) }
    var feedback by remember(challenges, questionIndex) { mutableStateOf("") }
    var advanced by remember(challenges) { mutableStateOf(false) }
    val current = challenges[questionIndex]

    IslandChallengeFrame(
        title = "Hazine Mührü",
        subtitle = "Bilgini yeni görevlerde kullan",
        currentStep = 5,
        starsCount = starsCount,
        mapPieces = 2,
        backdrop = R.drawable.route_ocean_backdrop,
        onBackClick = onBackClick,
        modifier = modifier
    ) {
        Surface(
            color = SayiAviColors.OceanNavy.copy(alpha = 0.88f),
            shape = RoundedCornerShape(18.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, SayiAviColors.GoldBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.chest_closed),
                    contentDescription = null,
                    modifier = Modifier.size(76.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("SON MÜHÜR", fontSize = 12.sp,
                        fontWeight = FontWeight.Bold, color = SayiAviColors.GoldBright)
                    Text("Hazineyi açmak için bilgini üç farklı durumda kullan.",
                        fontSize = 13.sp, lineHeight = 18.sp, color = Color.White)
                }
            }
        }
        ParchmentCard {
            Column(verticalArrangement = Arrangement.spacedBy(13.dp)) {
                Text("Görev ${questionIndex + 1} / 3", fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, color = SayiAviColors.OceanBlue)
                LinearProgressIndicator(
                    progress = { (questionIndex + if (isCorrect) 1 else 0) / 3f },
                    modifier = Modifier.fillMaxWidth(),
                    color = SayiAviColors.EmeraldGreen
                )
                Text(current.prompt, fontSize = 19.sp,
                    fontWeight = FontWeight.Bold, lineHeight = 26.sp,
                    color = SayiAviColors.TextNavy)
                current.choices.chunked(2).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()) {
                        row.forEach { option ->
                            Button(
                                onClick = {
                                    if (!isCorrect) {
                                        val correct = option == current.correct
                                        onAttemptRecorded(correct)
                                        if (correct) {
                                            isCorrect = true
                                            feedback = current.explanation
                                        } else feedback = "Bu sonuç uymuyor. İstersen ipucuna bak!"
                                    }
                                },
                                enabled = !isCorrect,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SayiAviColors.OceanBlue
                                ),
                                modifier = Modifier.weight(1f).height(58.dp)
                            ) {
                                Text("$option", color = Color.White, fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
                AdventureFeedbackBanner(
                    message = feedback,
                    positive = isCorrect,
                    modifier = Modifier.fillMaxWidth()
                )
                if (!isCorrect && !showHint) {
                    TextButton(onClick = { showHint = true; onHintShown() }) {
                        Text("💡 Bir İpucu Al", color = SayiAviColors.OceanBlue)
                    }
                }
                if (showHint && !isCorrect) Text(current.hint,
                    fontSize = 13.sp, color = SayiAviColors.TextNavyMuted)
                if (isCorrect) AdventureActionButton(
                    text = if (questionIndex == 2) "🏆 Hazineyi Aç!" else "Sonraki Görev ▶",
                    onClick = {
                        if (questionIndex == 2) {
                            if (!advanced) {
                                advanced = true
                                onCompleted()
                            }
                        } else {
                            questionIndex++
                            onQuestionProgress(questionIndex)
                        }
                    },
                    gradient = SayiAviColors.OrangeButtonGradient
                )
            }
        }
        Text(
            "Yanlış denemeler ve ipuçları öğrenme özeti için kaydedilir. " +
                "Bu görev yıldız veya satın alma hakkını ayrıca artırmaz.",
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            color = SayiAviColors.TextWhite
        )
    }
}
