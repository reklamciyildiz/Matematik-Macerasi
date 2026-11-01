package com.yigit033.matematikmacarasi.games.sayiavi2

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.tween
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.yigit033.matematikmacarasi.R
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

/**
 * Standalone offline temple slice. No stars, billing or legacy stage keys are mutated.
 * Each solved ritual is checkpointed before the next screen is shown.
 */
private class TempleCheckpoint(context: Context) {
    private val prefs = context.getSharedPreferences("temple_adventure_v1", Context.MODE_PRIVATE)
    val seed: Long get() = prefs.getLong("seed", 0L)
    val stage: Int get() = prefs.getInt("stage", 0).coerceIn(0, 4)
    val tier: LearningTier get() = LearningTier.fromSaved(prefs.getString("tier", null))
    val runs: Int get() = prefs.getInt("runs", 0)
    val attempts: Int get() = prefs.getInt("attempts", 0)
    val errors: Int get() = prefs.getInt("errors", 0)
    val hints: Int get() = prefs.getInt("hints", 0)
    fun stageAttempts(index: Int): Int = prefs.getInt("ritual_${index}_attempts", 0)
    fun stageErrors(index: Int): Int = prefs.getInt("ritual_${index}_errors", 0)
    fun stageHints(index: Int): Int = prefs.getInt("ritual_${index}_hints", 0)
    fun stageProgress(index: Int): Int = prefs.getInt("ritual_${index}_progress", if (index == 3) -1 else 0)
    fun saveStageProgress(index: Int, value: Int) {
        if (index in 0..3 && value >= -1 && seed > 0L) {
            prefs.edit().putInt("ritual_${index}_progress", value).apply()
        }
    }
    @Synchronized fun record(correct: Boolean, hint: Boolean = false): Boolean {
        if (seed <= 0L || stage == 4) return false
        return prefs.edit()
            .putInt("attempts", attempts + if (hint) 0 else 1)
            .putInt("errors", errors + if (!correct && !hint) 1 else 0)
            .putInt("hints", hints + if (hint) 1 else 0)
            .putInt("ritual_${stage}_attempts", stageAttempts(stage) + if (hint) 0 else 1)
            .putInt("ritual_${stage}_errors", stageErrors(stage) + if (!correct && !hint) 1 else 0)
            .putInt("ritual_${stage}_hints", stageHints(stage) + if (hint) 1 else 0)
            .commit()
    }
    @Synchronized fun start(seed: Long, tier: LearningTier): Boolean {
        val editor = prefs.edit().putLong("seed", seed).putString("tier", tier.name)
            .putInt("stage", 0).putInt("attempts", 0).putInt("errors", 0)
            .putInt("hints", 0)
        repeat(4) { i ->
            editor.putInt("ritual_${i}_attempts", 0)
            editor.putInt("ritual_${i}_errors", 0)
            editor.putInt("ritual_${i}_hints", 0)
            editor.putInt("ritual_${i}_progress", if (i == 3) -1 else 0)
        }
        return editor.commit()
    }
    @Synchronized fun advance(next: Int): Boolean {
        val previous = stage
        if (seed <= 0 || next != previous + 1 || next !in 1..4) return false
        val edit = prefs.edit().putInt("stage", next)
        if (next == 4) edit.putInt("runs", runs + 1)
        return edit.commit()
    }
}

/**
 * Native Android vector artwork: bundled in drawable, scales without network or image decoder.
 * Continuous pulse is limited to the small energy badge, not the entire illustration.
 */
@Composable
private fun TempleArtwork(drawableId: Int, description: String, compact: Boolean = false) {
    val loop = rememberInfiniteTransition(label = "Temple magic")
    val shine by loop.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), repeatMode = RepeatMode.Reverse),
        label = "Crystal shimmer"
    )
    Box(
        Modifier.fillMaxWidth().height(if (compact) 164.dp else 206.dp)
            .clip(RoundedCornerShape(22.dp))
            .border(2.dp, Color(0xFFCFA45C), RoundedCornerShape(22.dp))
    ) {
        Image(
            painter = painterResource(drawableId),
            contentDescription = description,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(
                Color(0xFF071B31).copy(alpha = 0.04f),
                Color(0xFF071B31).copy(alpha = 0.07f),
                Color(0xFF071B31).copy(alpha = 0.46f)
            ))
        ))
        Box(
            Modifier.align(Alignment.BottomEnd).padding(13.dp).size(30.dp)
                .graphicsLayer { alpha = shine; scaleX = 0.9f + 0.1f * shine; scaleY = 0.9f + 0.1f * shine }
                .background(Color(0xFF54F6E3), CircleShape)
                .border(2.dp, Color.White.copy(alpha = 0.75f), CircleShape),
            contentAlignment = Alignment.Center
        ) { Text("✦", color = Color(0xFF0C3846), fontSize = 18.sp) }
    }
}

@Composable
private fun TempleButton(
    onClick: () -> Unit, enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        onClick = onClick, enabled = enabled,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF2CB7F),
            contentColor = Color(0xFF102E43),
            disabledContainerColor = Color(0xFF465965),
            disabledContentColor = Color(0xFF9BB0BE)
        ),
        content = content
    )
}

@Composable
private fun TempleArray(rows: Int, columns: Int) {
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(columns) {
                    Box(
                        Modifier.size(22.dp).background(Color(0xFFFFD77C), RoundedCornerShape(5.dp))
                            .border(1.dp, Color(0xFFEFFFFB), RoundedCornerShape(5.dp)),
                        contentAlignment = Alignment.Center
                    ) { Text("◆", color = Color(0xFF123148), fontSize = 11.sp) }
                }
            }
        }
    }
}

@Composable
fun TempleAdventureScreen(tier: LearningTier, onExit: () -> Unit) {
    val context = LocalContext.current
    val checkpoint = remember(context) { TempleCheckpoint(context) }
    var seed by remember { mutableLongStateOf(checkpoint.seed) }
    var stage by remember { mutableIntStateOf(checkpoint.stage) }
    var runTier by remember { mutableStateOf(checkpoint.tier) }
    var placed by remember(seed, stage) { mutableIntStateOf(checkpoint.stageProgress(0)) }
    var rotated by remember(seed, stage) { mutableStateOf(checkpoint.stageProgress(1) == 1) }
    val rotateScale by animateFloatAsState(
        targetValue = if (rotated) 1f else .8f,
        animationSpec = tween(440), label = "Pillars rotate reveal"
    )
    var distributed by remember(seed, stage) { mutableIntStateOf(checkpoint.stageProgress(2)) }
    var selected by remember(seed, stage) { mutableIntStateOf(checkpoint.stageProgress(3)) }
    var message by remember(seed, stage) { mutableStateOf("") }
    var hintSeen by remember(seed, stage) { mutableStateOf(checkpoint.stageHints(stage) > 0) }
    var attemptCount by remember(seed, stage) { mutableIntStateOf(checkpoint.attempts) }
    var errorCount by remember(seed, stage) { mutableIntStateOf(checkpoint.errors) }
    var hintCount by remember(seed, stage) { mutableIntStateOf(checkpoint.hints) }
    var showReview by remember { mutableStateOf(false) }
    fun record(correct: Boolean, hint: Boolean = false): Boolean {
        if (checkpoint.record(correct, hint)) {
            attemptCount = checkpoint.attempts
            errorCount = checkpoint.errors
            hintCount = checkpoint.hints
            return true
        }
        message = "İlerleme kaydedilemedi."
        return false
    }
    val quest = remember(seed, runTier) { TempleEngine.create(seed, runTier) }
    val names = listOf("Mozaik Odası", "Denge Sütunları", "Kayıp Çarpan Kapısı", "Ayna Mekanizması")

    fun advance() {
        if (checkpoint.advance(stage + 1)) {
            stage = checkpoint.stage
            message = ""
        } else message = "İlerleme kaydedilemedi. Tekrar dene."
    }

    if (showReview) {
        AdventureReviewScreen(
            kind = AdventureReviewKind.MULTIPLICATION,
            tier = runTier,
            onExit = { showReview = false }
        )
        return
    }
    BackHandler(onBack = onExit)
    val scrollState = rememberScrollState()
    LaunchedEffect(seed, stage) { scrollState.scrollTo(0) }
    val isLanding = seed == 0L || stage == 4
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(
                Color(0xFF1E5464), Color(0xFF102C42), Color(0xFF081B30)
            )))
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        // Keep actions high-contrast; the theme's default outlined button was purple on blue.
        OutlinedButton(
            onClick = onExit,
            modifier = Modifier.heightIn(min = 48.dp),
            shape = RoundedCornerShape(15.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF83E7DB)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEBFFF9))
        ) { Text("← Haritaya dön", fontSize = 14.sp, fontWeight = FontWeight.SemiBold) }

        if (isLanding) {
            Text("ANTİK TAPINAK", fontSize = 14.sp, fontWeight = FontWeight.Black,
                color = Color(0xFFFFD77C), letterSpacing = 1.sp)
            Text("Çarpmanın Sırları", fontSize = 25.sp, fontWeight = FontWeight.Black,
                color = Color(0xFFFFE4A2))
            // Entrance artwork is deliberately only shown on the introduction / completion.
            TempleArtwork(R.drawable.temple_art_hero, "Turkuaz ışıklı antik tapınak girişi")
            Text("Seviye: " + runTier.title, fontSize = 13.sp, color = Color(0xFFDCEFF3))
        } else {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("ANTİK TAPINAK", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.7.sp, color = Color(0xFFFFD77C))
                Spacer(Modifier.weight(1f))
                Text(runTier.title, fontSize = 13.sp, color = Color(0xFFB6EFE3))
            }
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (seed == 0L) "4 farklı matematik ritüeli"
                else if (stage == 4) "Dört ritüel tamamlandı"
                else "Ritüel ${stage + 1} / 4",
                color = Color(0xFFB0EEE2), fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.weight(1f))
            Text("${stage.coerceIn(0, 4)} / 4",
                color = Color(0xFFFFD77C), fontSize = 13.sp)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            repeat(4) { index ->
                Box(
                    Modifier.weight(1f).height(8.dp).clip(CircleShape)
                        .background(if (index < stage) Color(0xFFFFD77C) else Color(0xFF456A78))
                )
            }
        }
        if (isLanding) {
            Text(if (stage == 4) "Tapınağın ışığı yeniden parlıyor!"
                else "Dört odada matematiksel düzenekleri çalıştır.",
                color = Color.White, fontSize = 18.sp)
            Text("Tamamlanan keşifler: " + checkpoint.runs, color = Color.White)
            if (stage == 4) {
                Text("Bu tur: $attemptCount deneme · $errorCount yanlış · $hintCount ipucu",
                    color = Color(0xFF9FE9DA))
                names.forEachIndexed { index, title ->
                    Text("$title: ${checkpoint.stageAttempts(index)} hamle, ${checkpoint.stageErrors(index)} yanlış",
                        color = Color.White, fontSize = 12.sp)
                }
                Text("Bu veriler oyun içi gözlemdir; kalıcı öğrenme veya MEB kazanımı tamamlandı anlamına gelmez.",
                    color = Color(0xFFB7DBE6), fontSize = 11.sp)
            }
            if (stage == 4) {
                TempleButton(onClick = { showReview = true }) { Text("🧩 3 Soruluk Tekrar") }
            }
            TempleButton(onClick = {
                val now = System.currentTimeMillis().coerceAtLeast(seed + 1L).coerceAtLeast(1L)
                if (checkpoint.start(now, tier)) {
                    seed = now
                    runTier = tier
                    stage = 0
                } else message = "Yeni oturum kaydedilemedi."
            }) { Text(if (stage == 4) "Yeni keşif" else "Tapınağa gir") }
        } else {
            Text(names[stage], color = Color(0xFFFFD77C),
                fontWeight = FontWeight.Black, fontSize = 24.sp)
            val roomArt = when (stage) {
                0 -> R.drawable.temple_art_mosaic
                1 -> R.drawable.temple_art_pillars
                2 -> R.drawable.temple_art_gate
                else -> R.drawable.temple_art_mirror
            }
            // Exactly one scene image during gameplay; no duplicated hero banner.
            Crossfade(targetState = roomArt, animationSpec = tween(340),
                label = "Ritual scene") { art ->
                TempleArtwork(art, names[stage] + " ortamı", compact = true)
            }
            Text(
                listOf(
                    "Kadim mozaikleri taş taş inşa et.",
                    "Sütunların eşdeğer düzenini keşfet.",
                    "Taşları eşit gruplara ayırıp kapıyı aç.",
                    "Çarpma ve bölmenin bağlantısını çöz."
                )[stage],
                color = Color(0xFFB6EFE3), fontSize = 13.sp, lineHeight = 18.sp
            )
            when (stage) {
                0 -> {
                    val q = quest.mosaic
                    Text("Bir mozaik oluştur: " + q.rows + " sıra, her sırada " +
                        q.columns + " taş.", color = Color.White)
                    repeat(q.rows) { r ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                            repeat(q.columns) { c ->
                                val index = r * q.columns + c
                                Box(
                                    Modifier.size(31.dp).padding(2.dp)
                                        .background(if (index < placed) Color(0xFFFFC75D)
                                            else Color(0xFF355B70), RoundedCornerShape(6.dp))
                                        .border(1.dp, Color.White.copy(alpha = .4f),
                                            RoundedCornerShape(6.dp))
                                        .clickable {
                                            if (index == placed && placed < q.tiles) {
                                                placed = TempleEngine.placeMosaicTile(placed, q.tiles)
                                                checkpoint.saveStageProgress(0, placed)
                                            }
                                        }
                                        .semantics {
                                            contentDescription = "Mozaik taşı " + (index + 1) +
                                                (if (index < placed) ", yerleştirildi" else ", boş")
                                        },
                                    contentAlignment = Alignment.Center
                                ) { Text(if (index < placed) "◆" else "+", color = Color.White) }
                            }
                        }
                    }
                    Text("Kalan boş yuva: " + (q.tiles - placed), color = Color.White)
                    if (placed == q.tiles) {
                        Text("Mozaikte toplam kaç taş var?", color = Color(0xFF9FE9DA), fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(q.tiles, q.tiles + q.rows, q.tiles - q.columns)
                                .shuffled(kotlin.random.Random(seed + 401L))
                                .forEach { candidate ->
                                    OutlinedButton(
                                        modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                                        onClick = {
                                            if (selected != q.tiles) {
                                                selected = candidate
                                                if (candidate != q.tiles) record(false)
                                                message = if (candidate == q.tiles)
                                                    "${q.rows} × ${q.columns} = ${q.tiles}. Doğru!"
                                                else "Sıraları ve her sıradaki taşları yeniden say."
                                            }
                                        },
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF83E7DB)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) { Text("$candidate") }
                                }
                        }
                    }
                    TempleButton(enabled = placed < q.tiles, onClick = {
                        placed = (placed + q.columns).coerceAtMost(q.tiles)
                        checkpoint.saveStageProgress(0, placed)
                    }) { Text("Bir sırayı yerleştir") }
                    TempleButton(enabled = placed == q.tiles && selected == q.tiles,
                        onClick = { if (record(true)) advance() }) {
                        Text("Mozaik tamamlandı")
                    }
                }
                1 -> {
                    val q = quest.pillars
                    Text("Soldaki düzeni döndürerek iki tarafın eşit olduğunu kanıtla.",
                        color = Color.White)
                    Text(q.leftRows.toString() + " × " + q.leftColumns.toString() +
                        "    =    " + (if (rotated) q.rightRows.toString() +
                        " × " + q.rightColumns.toString() else "? × ?"),
                        color = Color(0xFFFFD77C), fontSize = 24.sp)
                    TempleArray(q.leftRows, q.leftColumns)
                    if (rotated) {
                        Text("Döndürülmüş eşdeğer dizilim", color = Color(0xFF9FE9DA))
                        Box(Modifier.fillMaxWidth().graphicsLayer {
                            scaleX = rotateScale
                            scaleY = rotateScale
                        }) { TempleArray(q.rightRows, q.rightColumns) }
                    }
                    TempleButton(onClick = {
                        rotated = !rotated
                        checkpoint.saveStageProgress(1, if (rotated) 1 else 0)
                    }) {
                        Text(if (rotated) "Dizilimi geri çevir" else "Sütunları döndür")
                    }
                    if (rotated) {
                        Text("Her iki dizilimde kaç taş var?", color = Color(0xFF9FE9DA),
                            fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(q.weight, q.weight + q.leftRows, q.weight - q.leftColumns)
                                .shuffled(kotlin.random.Random(seed + 402L))
                                .forEach { candidate ->
                                    OutlinedButton(
                                        modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                                        onClick = {
                                            if (selected != q.weight) {
                                                selected = candidate
                                                if (candidate != q.weight) record(false)
                                                message = if (candidate == q.weight)
                                                    "Sıra ve sütunların yeri değişse de toplam aynı!"
                                                else "Taşları sırayla say ve diğer dizilimle karşılaştır."
                                            }
                                        },
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF83E7DB)),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                                    ) { Text("$candidate") }
                                }
                        }
                    }
                    TempleButton(enabled = rotated && selected == q.weight &&
                        TempleEngine.isBalanced(q),
                        onClick = { if (record(true)) advance() }) { Text("Dengeyi doğrula") }
                }
                2 -> {
                    val q = quest.gate
                    Text(q.total.toString() + " taşı " + q.groups +
                        " eş gruba paylaştır. Her grupta kaç taş olur?", color = Color.White)
                    Text("Taşları eş gruplara sırayla dağıt.", color = Color(0xFF9FE9DA))
                    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                        repeat((q.groups + 2) / 3) { groupRow ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                repeat(3) { offset ->
                                    val group = groupRow * 3 + offset
                                    if (group < q.groups) {
                                        val count = distributed / q.groups +
                                            if (group < distributed % q.groups) 1 else 0
                                        Surface(Modifier.weight(1f),
                                            shape = RoundedCornerShape(13.dp),
                                            color = Color(0xFF214B60),
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp, Color(0xFF6DDBD5))) {
                                            Column(
                                                Modifier.padding(vertical = 11.dp, horizontal = 4.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Text("✦", color = Color(0xFFFFD77C), fontSize = 20.sp)
                                                Text("Grup " + (group + 1), color = Color.White,
                                                    fontSize = 11.sp)
                                                Text("$count taş", color = Color(0xFFFFD77C),
                                                    fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    } else {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                    TempleButton(enabled = distributed < q.total,
                        onClick = {
                            distributed = (distributed + q.groups).coerceAtMost(q.total)
                            checkpoint.saveStageProgress(2, distributed)
                        }) {
                        Text("Her gruba birer taş dağıt: $distributed / ${q.total}")
                    }
                    TempleButton(enabled = distributed > 0,
                        onClick = {
                            distributed = (distributed - q.groups).coerceAtLeast(0)
                            checkpoint.saveStageProgress(2, distributed)
                        }) {
                        Text("Son dağıtımı geri al")
                    }
                    if (distributed == q.total) {
                        TempleArray(q.groups, q.itemsPerGroup)
                        Text("${q.groups} × ${q.itemsPerGroup} = ${q.total}", color = Color(0xFF9FE9DA))
                    }
                    TempleButton(enabled = distributed == q.total,
                        onClick = { if (record(true)) advance() }) { Text("Kapıyı aç") }
                }
                3 -> {
                    val q = quest.mirror
                    Text(q.leftFactor.toString() + " × " + q.rightFactor +
                        " = " + q.total + ". Aynadaki eksik sonucu bul:",
                        color = Color.White)
                    Text(q.total.toString() + " ÷ " + q.divisor + " = ?",
                        color = Color(0xFFFFD77C), fontSize = 27.sp)
                    TempleArray(q.leftFactor, q.rightFactor)
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(q.resultAfterDivision,
                            q.resultAfterDivision + 1,
                            (q.resultAfterDivision - 1).coerceAtLeast(1))
                            .distinct().forEach { value ->
                                val chosen = selected == value
                                OutlinedButton(
                                    modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp, if (chosen) Color(0xFFFFD77C) else Color(0xFF83E7DB)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = if (chosen) Color(0xFFFFD77C)
                                            else Color(0xFF214B60),
                                        contentColor = if (chosen) Color(0xFF0E2C40) else Color.White
                                    ),
                                    onClick = {
                                        if (selected != q.resultAfterDivision &&
                                            record(value == q.resultAfterDivision)) {
                                            selected = value
                                            checkpoint.saveStageProgress(3, selected)
                                            message = if (value == q.resultAfterDivision)
                                                "Ters işlem bağlantısını buldun."
                                            else "Çarpma dizilimine tekrar bak."
                                        }
                                    }
                                ) { Text(value.toString(), fontWeight = FontWeight.Bold, fontSize = 18.sp) }
                            }
                    }
                    TempleButton(enabled = selected == q.resultAfterDivision,
                        onClick = { advance() }) { Text("Tapınağı aydınlat") }
                }
            }
            if (stage >= 2) {
                OutlinedButton(
                    onClick = {
                        if (!hintSeen && record(false, hint = true)) hintSeen = true
                    },
                    enabled = !hintSeen,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF83E7DB)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFFEFFFFB),
                        disabledContentColor = Color(0xFFB5D4D4)
                    )
                ) { Text(if (hintSeen) "İpucu gösterildi" else "İpucu al") }
                if (hintSeen) Text(if (stage == 2) "Toplam taşı eş grup sayısına böl." else "Çarpımdan bir çarpana bölersen diğerini bulursun.", color = Color(0xFF9FE9DA))
            }
            Text("$attemptCount deneme · $errorCount yanlış · $hintCount ipucu", color = Color(0xFFB7DBE6))
            if (message.isNotEmpty()) Text(message, color = Color(0xFFFFD77C))
        }
        Spacer(Modifier.height(12.dp))
        Text("Bu oyun öğrenme alıştırmasıdır; tek başına kazanım yeterliliği belgesi değildir.",
            color = Color(0xFFBED7E3), fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}
