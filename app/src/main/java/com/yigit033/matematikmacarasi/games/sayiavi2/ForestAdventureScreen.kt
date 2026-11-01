package com.yigit033.matematikmacarasi.games.sayiavi2

import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveLearningStore
import com.yigit033.matematikmacarasi.learning.adaptive.AdventureSkill
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

private object ForestPalette {
    val Night = Color(0xFF061C1A)
    val Dark = Color(0xFF103B32)
    val Teal = Color(0xFF165E50)
    val Emerald = Color(0xFF137A53)
    val Gold = Color(0xFFF9CB72)
    val Cream = Color(0xFFFFF6E4)
    val Ink = Color(0xFF19342D)
    val Muted = Color(0xFF4E665F)
    val Wrong = Color(0xFF9B4033)
}

@Composable
private fun ForestBackground() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawRect(brush = Brush.verticalGradient(
            colors = listOf(ForestPalette.Dark, ForestPalette.Night),
            startY = 0f, endY = h
        ))
        // Procedural silhouettes: no borrowed external image or extra SDK required.
        repeat(8) { i ->
            val x = w * ((i + 0.3f) / 7.6f)
            val top = h * (0.10f + (i % 3) * 0.06f)
            drawLine(color = Color(0xFF194B3F),
                start = Offset(x, h), end = Offset(x, top), strokeWidth = w * 0.038f)
            drawCircle(color = Color(0xFF24624C), radius = w * 0.21f,
                center = Offset(x, top + w * 0.11f))
            drawCircle(color = Color(0xFF30755D).copy(alpha = .68f), radius = w * 0.13f,
                center = Offset(x + w * 0.035f, top - w * 0.028f))
        }
        repeat(13) { i ->
            val x = w * ((i + 1f) / 14f)
            val y = h * (.13f + (i * 7 % 10) * 0.067f)
            drawCircle(ForestPalette.Gold.copy(alpha = 0.38f), radius = 3.5f,
                center = Offset(x, y))
        }
    }
}

/** Forest stage progression uses an isolated session, without touching Kumsal Koyu. */
@Composable
fun ForestAdventureScreen(
    store: ForestProgressStore,
    starCount: Int,
    onFinished: (ForestSession) -> Unit,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val adaptiveStore = remember { AdaptiveLearningStore(context) }
    var skillProgress by remember { mutableStateOf(adaptiveStore.progress(AdventureSkill.FOUR_COUNTING)) }
    val haptics = LocalHapticFeedback.current
    var lastActionPositive by remember { mutableStateOf(true) }
    var run by remember { mutableStateOf(store.load()) }
    var message by remember { mutableStateOf("") }
    var showExit by remember { mutableStateOf(false) }
    var showReview by remember { mutableStateOf(false) }
    var hintVisible by remember(run.stage, run.bridgeSpan, run.leafRound, run.trailStep, run.rootRound) {
        mutableStateOf(false)
    }
    val soundCorrect = remember { runCatching { MediaPlayer.create(context, R.raw.correct_sound) }.getOrNull() }
    val soundWrong = remember { runCatching { MediaPlayer.create(context, R.raw.wrong_sound) }.getOrNull() }
    DisposableEffect(Unit) {
        onDispose {
            runCatching { soundCorrect?.release() }
            runCatching { soundWrong?.release() }
        }
    }
    fun play(correct: Boolean) {
        lastActionPositive = correct
        if (correct) haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        runCatching {
            (if (correct) soundCorrect else soundWrong)?.apply { seekTo(0); start() }
        }
    }
    fun update(next: ForestSession) {
        if (store.save(next)) run = next
        else message = "İlerleme kaydedilemedi. Yeniden deneyebilirsin."
    }
    fun hint() {
        if (!hintVisible) {
            hintVisible = true
            update(run.hint())
        }
    }
    if (showReview) {
        AdventureReviewScreen(
            kind = AdventureReviewKind.COUNT_FOUR,
            tier = run.tier,
            onExit = { showReview = false }
        )
        return
    }
    BackHandler(enabled = run.stage != ForestStage.INTRO && run.stage != ForestStage.SUMMARY) {
        showExit = true
    }
    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false },
            title = { Text("Ormandan ayrıl?") },
            text = { Text("İlerleme cihazda kaydedildi. Daha sonra kaldığın yerden devam edebilirsin.") },
            confirmButton = { TextButton(onClick = { showExit = false; onExit() }) { Text("Ayrıl") } },
            dismissButton = { TextButton(onClick = { showExit = false }) { Text("Devam Et") } }
        )
    }
    val stepsDone = when (run.stage) {
        ForestStage.INTRO, ForestStage.BRIDGE -> 0
        ForestStage.LEAVES -> 1
        ForestStage.TRAIL -> 2
        ForestStage.ROOTS -> 3
        ForestStage.SUMMARY -> 4
    }
    Box(modifier = Modifier.fillMaxSize().background(ForestPalette.Night)) {
        ForestBackground()
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    modifier = Modifier.size(48.dp).clickable {
                        if (run.stage == ForestStage.INTRO || run.stage == ForestStage.SUMMARY) onExit()
                        else showExit = true
                    },
                    shape = CircleShape, color = ForestPalette.Dark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, ForestPalette.Gold)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("‹", fontSize = 30.sp, color = ForestPalette.Gold)
                    }
                }
                Image(
                    painter = painterResource(R.drawable.forest_node),
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    contentScale = ContentScale.Fit
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text("GİZEMLİ ORMAN", fontSize = 18.sp,
                        fontWeight = FontWeight.Black, color = ForestPalette.Cream)
                    Text("4'er sayma • Antik Köprü", fontSize = 12.sp, color = ForestPalette.Gold)
                }
                Text("★ $starCount", color = ForestPalette.Gold,
                    fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = ForestPalette.Night.copy(alpha = .82f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ForestPalette.Teal)
            ) {
                Column(modifier = Modifier.padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)) {
                    AdventureSectionProgress(
                        label = "ORMAN KEŞİF ROTASI",
                        completed = stepsDone,
                        total = 4,
                        accent = ForestPalette.Gold
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("Köprü", "Yaprak", "Patika", "Ağaç").forEachIndexed { i, title ->
                            Text(title, fontSize = 11.sp,
                                color = if (i < stepsDone) ForestPalette.Gold else ForestPalette.Cream)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                when (run.stage) {
                    ForestStage.INTRO -> {
                        ForestPanel {
                            ForestHeading("🌲", "Ormanın sırrı", "Açıklıkları onar, hatalı izleri bul ve kadim ağacı uyandır.")
                            Text("Bu bölümde dörder ritmik saymayı 4 farklı oyunla kullanacaksın.",
                                fontSize = 14.sp, color = ForestPalette.Muted)
                            Text("1. Kütük Köprüsü  ·  2. Şifreli Yapraklar\n3. Işıklı Patika  ·  4. Kadim Ağaç",
                                fontSize = 13.sp, lineHeight = 22.sp, color = ForestPalette.Ink)
                            if (store.hasCompleted()) Text(
                                "Daha önce ${store.completedRuns()} kez tamamlandı. En iyi hamle oranı: ${store.bestAccuracy()}%",
                                fontSize = 12.sp, color = ForestPalette.Emerald)
                            AdventureTierSelector(
                                progress = skillProgress,
                                gradeLabel = adaptiveStore.gradeLabel(),
                                onChange = { tier ->
                                    if (adaptiveStore.selectTier(AdventureSkill.FOUR_COUNTING, tier)) {
                                        skillProgress = adaptiveStore.progress(AdventureSkill.FOUR_COUNTING)
                                    }
                                }
                            )
                            ForestButton("Maceraya Başla ▶") {
                                val next = store.startNew(
                                    adaptiveStore.progress(AdventureSkill.FOUR_COUNTING).currentTier
                                )
                                run = next
                                message = ""
                            }
                        }
                    }
                    ForestStage.BRIDGE -> {
                        ForestPanel {
                            ForestHeading("🪵", "Kütük Köprüsü",
                                "Üç köprü açıklığını, her biri toplam 4 birim olacak şekilde onar.")
                            Text("Dört birimlik her açıklığı farklı uzunlukta kütüklerle doldurabilirsin.",
                                fontSize = 13.sp, color = ForestPalette.Muted)
                            repeat(ForestEngine.BRIDGE_SPANS) { span ->
                                val filled = when {
                                    span < run.bridgeSpan -> 4
                                    span == run.bridgeSpan -> run.bridgeFill
                                    else -> 0
                                }
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Açıklık ${span + 1}", fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp, color = ForestPalette.Ink)
                                        Text("$filled / 4", fontSize = 12.sp, color = ForestPalette.Muted)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()) {
                                        repeat(4) { slot ->
                                            val plankColor by animateColorAsState(
                                                targetValue = if (slot < filled) Color(0xFFB77B3F)
                                                    else Color(0xFFDCE4CE),
                                                label = "Kütük yerleştirme"
                                            )
                                            Box(
                                                modifier = Modifier.weight(1f).height(42.dp)
                                                    .background(plankColor, RoundedCornerShape(8.dp))
                                                    .border(1.dp, ForestPalette.Teal.copy(alpha = .45f),
                                                        RoundedCornerShape(8.dp)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(if (slot < filled) "▰" else "·",
                                                    fontSize = 21.sp,
                                                    color = if (slot < filled) Color.White else ForestPalette.Teal)
                                            }
                                        }
                                    }
                                }
                            }
                            Text("Bir kütük seç: uzunluğu 1, 2 veya 3 birim.",
                                fontWeight = FontWeight.Medium, fontSize = 13.sp, color = ForestPalette.Ink)
                            Row(horizontalArrangement = Arrangement.spacedBy(9.dp),
                                modifier = Modifier.fillMaxWidth()) {
                                ForestEngine.plankLengths.forEach { length ->
                                    Button(
                                        onClick = {
                                            if (run.stage == ForestStage.BRIDGE && run.bridgeSpan < 3) {
                                                val move = ForestEngine.placePlank(run.bridgeFill, length)
                                                play(move.valid)
                                                val answered = run.answer(move.valid)
                                                val next = if (move.completesSpan) {
                                                    if (run.bridgeSpan == 2) answered.copy(
                                                        bridgeSpan = 3, bridgeFill = 0, stage = ForestStage.LEAVES
                                                    ) else answered.copy(
                                                        bridgeSpan = run.bridgeSpan + 1, bridgeFill = 0
                                                    )
                                                } else answered.copy(bridgeFill = move.nextFill)
                                                update(next)
                                                message = when {
                                                    !move.valid -> "Kütük fazla uzun. Açıklık toplam 4 birim olmalı."
                                                    move.completesSpan -> "Tam 4 birim! Köprü açıklığı tamamlandı."
                                                    else -> "Şimdi ${4 - next.bridgeFill} birimlik boşluk var."
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(54.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestPalette.Emerald)
                                    ) {
                                        Text("$length", fontWeight = FontWeight.Black,
                                            fontSize = 22.sp, color = Color.White)
                                    }
                                }
                            }
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                TextButton(onClick = { hint() }) {
                                    Text("💡 İpucu", color = ForestPalette.Teal)
                                }
                                TextButton(
                                    onClick = { update(run.copy(bridgeFill = 0)); message = "" },
                                    enabled = run.bridgeFill > 0
                                ) { Text("Bu açıklığı sıfırla", color = ForestPalette.Muted) }
                            }
                            if (hintVisible) Text("4 = 1 + 3 veya 2 + 2 olabilir. Kütüklerini toplarken 4'ü geçme.",
                                color = ForestPalette.Ink, fontSize = 13.sp)
                        }
                    }
                    ForestStage.LEAVES -> {
                        val q = ForestEngine.leafRounds(run.seed, run.tier)[run.leafRound.coerceIn(0, 2)]
                        ForestPanel {
                            ForestHeading("🍃", "Şifreli Yapraklar",
                                "Bir yaprak yanlış sayı taşıyor. Dörder artma kuralını kontrol et.")
                            Text("Yaprak grubu ${run.leafRound + 1} / 3",
                                fontWeight = FontWeight.Bold, color = ForestPalette.Teal, fontSize = 13.sp)
                            Text("Doğru kural: her adımda +4. Hatalı yaprağa dokun.",
                                fontSize = 13.sp, color = ForestPalette.Muted)
                            Row(horizontalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.fillMaxWidth()) {
                                q.values.forEachIndexed { index, value ->
                                    Surface(
                                        modifier = Modifier.weight(1f).height(76.dp)
                                            .clickable {
                                                if (run.stage == ForestStage.LEAVES) {
                                                    val right = index == q.incorrectIndex
                                                    play(right)
                                                    val answered = run.answer(right)
                                                    if (right) {
                                                        update(if (run.leafRound == 2) answered.copy(
                                                            leafRound = 3, stage = ForestStage.TRAIL
                                                        ) else answered.copy(leafRound = run.leafRound + 1))
                                                        message = "Hatalı yaprak $value. Doğrusu ${q.correctValue} olmalı!"
                                                    } else {
                                                        update(answered)
                                                        message = "Bu yaprak doğru sırada. Dörder ilerlemeyi kontrol et."
                                                    }
                                                }
                                            },
                                        shape = RoundedCornerShape(topStart = 24.dp, bottomEnd = 24.dp,
                                            topEnd = 8.dp, bottomStart = 8.dp),
                                        color = if (index % 2 == 0) ForestPalette.Emerald else ForestPalette.Teal
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("$value", color = Color.White, fontWeight = FontWeight.Bold,
                                                fontSize = 19.sp)
                                        }
                                    }
                                }
                            }
                            TextButton(onClick = { hint() }) {
                                Text("💡 İpucu", color = ForestPalette.Teal)
                            }
                            if (hintVisible) Text(
                                "İlk iki yaprağın farkı 4. Aynı farkı sıradaki sayılarda da ara.",
                                fontSize = 13.sp, color = ForestPalette.Ink
                            )
                        }
                    }
                    ForestStage.TRAIL -> {
                        val q = ForestEngine.trailSteps(run.seed, run.tier)[run.trailStep.coerceIn(0, 3)]
                        ForestPanel {
                            ForestHeading("✨", "Işıklı Patika",
                                "Her doğru dörderlik sıçrama orman yolunda yeni bir ışık yakar.")
                            Text("Açılan yol: ${run.trailStep} / 4 geçit",
                                fontWeight = FontWeight.Bold, color = ForestPalette.Teal, fontSize = 13.sp)
                            Row(modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) { i ->
                                    Surface(
                                        modifier = Modifier.size(43.dp),
                                        shape = CircleShape,
                                        color = if (i <= run.trailStep) ForestPalette.Gold else Color(0xFFD6E2D7)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(if (i <= run.trailStep) "✦" else "·",
                                                fontSize = 20.sp, color = ForestPalette.Ink)
                                        }
                                    }
                                }
                            }
                            Text("Şu an: ${q.from}", color = ForestPalette.Ink,
                                fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                            Text("Hangi taşa sıçrarsan tam 4 ilerlersin?",
                                color = ForestPalette.Muted, fontSize = 14.sp)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()) {
                                q.choices.forEach { candidate ->
                                    Button(
                                        onClick = {
                                            if (run.stage == ForestStage.TRAIL) {
                                                val right = candidate == q.target
                                                play(right)
                                                val answered = run.answer(right)
                                                if (right) {
                                                    update(if (run.trailStep == 3) answered.copy(
                                                        trailStep = 4, stage = ForestStage.ROOTS
                                                    ) else answered.copy(trailStep = run.trailStep + 1))
                                                    message = "Doğru yol: ${q.from} + 4 = ${q.target}"
                                                } else {
                                                    update(answered)
                                                    message = "Bu taş +4 ilerletmiyor. Dört ekleyerek yeniden dene."
                                                }
                                            }
                                        },
                                        modifier = Modifier.weight(1f).height(58.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestPalette.Teal)
                                    ) { Text("$candidate", color = Color.White, fontSize = 20.sp) }
                                }
                            }
                            TextButton(onClick = { hint() }) {
                                Text("💡 İpucu", color = ForestPalette.Teal)
                            }
                            if (hintVisible) Text("Bulunduğun sayıya 2 değil, 6 değil, tam 4 ekle.",
                                fontSize = 13.sp, color = ForestPalette.Ink)
                        }
                    }
                    ForestStage.ROOTS -> {
                        val questions = ForestEngine.rootChallenges(run.seed, run.tier)
                        ForestPanel {
                            ForestHeading("🌳", "Kadim Ağacın Sırrı",
                                "Artık öğrendiğin bilgiyi yeni durumlarda kullanma zamanı.")
                            if (run.rootRound < 3) {
                                val q = questions[run.rootRound]
                                Text("Mühür ${run.rootRound + 1} / 3",
                                    fontWeight = FontWeight.Bold, color = ForestPalette.Teal,
                                    fontSize = 13.sp)
                                Text(q.question, fontSize = 19.sp,
                                    lineHeight = 26.sp, fontWeight = FontWeight.Bold,
                                    color = ForestPalette.Ink)
                                q.options.chunked(2).forEach { options ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp),
                                        modifier = Modifier.fillMaxWidth()) {
                                        options.forEach { value ->
                                            Button(
                                                onClick = {
                                                    if (run.stage == ForestStage.ROOTS && run.rootRound < 3) {
                                                        val right = value == q.correct
                                                        play(right)
                                                        val answered = run.answer(right)
                                                        if (right) {
                                                            update(answered.copy(rootRound = run.rootRound + 1))
                                                            message = q.reason
                                                        } else {
                                                            update(answered)
                                                            message = "Bu cevap uymuyor. Dörder sayma kuralını kullan."
                                                        }
                                                    }
                                                },
                                                modifier = Modifier.weight(1f).height(60.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = ForestPalette.Emerald
                                                )
                                            ) { Text("$value", fontSize = 19.sp, color = Color.White) }
                                        }
                                    }
                                }
                                TextButton(onClick = { hint() }) {
                                    Text("💡 İpucu", color = ForestPalette.Teal)
                                }
                                if (hintVisible) Text(q.hint, fontSize = 13.sp, color = ForestPalette.Ink)
                            } else {
                                Text("Üç mühür çözüldü. Orman yeniden ışıldıyor!",
                                    fontSize = 16.sp, color = ForestPalette.Ink)
                                ForestButton("🏆 Kadim Ağacı Uyandır") {
                                    if (run.stage == ForestStage.ROOTS && run.rootRound == 3 &&
                                        store.finish(run)) {
                                        val finished = run
                                        run = finished.copy(stage = ForestStage.SUMMARY)
                                        message = ""
                                        onFinished(finished)
                                    } else {
                                        message = "Tamamlanma kaydedilemedi; tekrar deneyebilirsin."
                                    }
                                }
                            }
                        }
                    }
                    ForestStage.SUMMARY -> {
                        ForestPanel {
                            ForestHeading("🏆", "Orman Kurtarıldı",
                                "Dört ayrı matematik göreviyle kadim köprüyü ve ağacı yeniden canlandırdın.")
                            Text("Görevler tamamlandı: 4 / 4",
                                fontWeight = FontWeight.Bold, color = ForestPalette.Emerald,
                                fontSize = 16.sp)
                            ForestStat("Oynanan beceri düzeyi", run.tier.title)
                            ForestStat("Doğru hamle oranı", "${run.accuracy}%")
                            ForestStat("Toplam hamle", "${run.attempts}")
                            ForestStat("Yanlış hamle", "${run.errors}")
                            ForestStat("İpuçları", "${run.hints}")
                            ForestStat("En iyi oturum oranı", "${store.bestAccuracy()}%")
                            ForestStat("Toplam bitirme", "${store.completedRuns()}")
                            Text("Bu ölçüler yalnızca oyun içi hamleleri gösterir; matematikte kalıcı öğrenmeyi kanıtlamaz.",
                                fontSize = 11.sp, color = ForestPalette.Muted)
                            Text(if (run.errors > 0 || run.hints > 0)
                                "Öneri: +4 kuralını farklı başlangıç sayılarıyla tekrar çalış."
                                else "Öneri: Yeni örüntüler ve zihinden işlemlerle ilerle.",
                                fontSize = 13.sp, color = ForestPalette.Ink)
                            ForestButton("🧩 3 Soruluk Tekrar") { showReview = true }
                            ForestButton("🔁 Yeniden Oyna") {
                                skillProgress = adaptiveStore.progress(AdventureSkill.FOUR_COUNTING)
                                run = store.startNew(skillProgress.currentTier)
                                message = ""
                            }
                            TextButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
                                Text("Bölge Haritasına Dön", color = ForestPalette.Teal)
                            }
                        }
                    }
                }
                AdventureFeedbackBanner(
                    message = message,
                    positive = lastActionPositive,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ForestPanel(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Surface(
        color = ForestPalette.Cream,
        shape = RoundedCornerShape(21.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, ForestPalette.Gold),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(17.dp),
            verticalArrangement = Arrangement.spacedBy(13.dp), content = content)
    }
}

@Composable
private fun ForestHeading(emoji: String, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(emoji, fontSize = 34.sp)
        Column {
            Text(title, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold,
                color = ForestPalette.Ink, lineHeight = 26.sp)
            Text(subtitle, fontSize = 13.sp, color = ForestPalette.Muted, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun ForestStat(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = ForestPalette.Muted, fontSize = 13.sp)
        Text(value, color = ForestPalette.Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ForestButton(label: String, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(55.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ForestPalette.Emerald),
        shape = RoundedCornerShape(14.dp)) {
        Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}
