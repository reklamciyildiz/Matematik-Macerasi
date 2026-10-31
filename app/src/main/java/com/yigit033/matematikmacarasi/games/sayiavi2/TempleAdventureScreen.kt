package com.yigit033.matematikmacarasi.games.sayiavi2

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.yigit033.matematikmacarasi.R
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
    fun record(correct: Boolean, hint: Boolean = false): Boolean {
        if (seed <= 0L || stage == 4) return false
        return prefs.edit()
            .putInt("attempts", attempts + if (hint) 0 else 1)
            .putInt("errors", errors + if (!correct && !hint) 1 else 0)
            .putInt("hints", hints + if (hint) 1 else 0).commit()
    }
    fun start(seed: Long, tier: LearningTier): Boolean =
        prefs.edit().putLong("seed", seed).putString("tier", tier.name)
            .putInt("stage", 0).putInt("attempts", 0).putInt("errors", 0)
            .putInt("hints", 0).commit()
    fun advance(next: Int): Boolean {
        val previous = stage
        if (next != previous + 1 || next !in 1..4) return false
        val edit = prefs.edit().putInt("stage", next)
        if (next == 4) edit.putInt("runs", runs + 1)
        return edit.commit()
    }
}

@Composable
fun TempleAdventureScreen(tier: LearningTier, onExit: () -> Unit) {
    val context = LocalContext.current
    val checkpoint = remember(context) { TempleCheckpoint(context) }
    var seed by remember { mutableLongStateOf(checkpoint.seed) }
    var stage by remember { mutableIntStateOf(checkpoint.stage) }
    var runTier by remember { mutableStateOf(checkpoint.tier) }
    var placed by remember(seed, stage) { mutableIntStateOf(0) }
    var rotated by remember(seed, stage) { mutableStateOf(false) }
    var distributed by remember(seed, stage) { mutableIntStateOf(0) }
    var selected by remember(seed, stage) { mutableIntStateOf(0) }
    var message by remember(seed, stage) { mutableStateOf("") }
    var hintSeen by remember(seed, stage) { mutableStateOf(false) }
    var attemptCount by remember(seed, stage) { mutableIntStateOf(checkpoint.attempts) }
    var errorCount by remember(seed, stage) { mutableIntStateOf(checkpoint.errors) }
    var hintCount by remember(seed, stage) { mutableIntStateOf(checkpoint.hints) }
    fun record(correct: Boolean, hint: Boolean = false) {
        if (checkpoint.record(correct, hint)) {
            attemptCount = checkpoint.attempts
            errorCount = checkpoint.errors
            hintCount = checkpoint.hints
        } else message = "İlerleme kaydedilemedi."
    }
    val quest = remember(seed, runTier) { TempleEngine.create(seed, runTier) }
    val names = listOf("Mozaik Odası", "Denge Sütunları", "Kayıp Çarpan Kapısı", "Ayna Mekanizması")

    fun advance() {
        if (checkpoint.advance(stage + 1)) {
            stage = checkpoint.stage
            message = ""
        } else message = "İlerleme kaydedilemedi. Tekrar dene."
    }

    BackHandler(onBack = onExit)
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF1E5464), Color(0xFF102C42), Color(0xFF081B30))))
            .verticalScroll(rememberScrollState()).padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OutlinedButton(onClick = onExit) { Text("← Macera haritası") }
        Image(painter = painterResource(R.drawable.temple_node), contentDescription = "Antik Tapınak", modifier = Modifier.fillMaxWidth().height(140.dp), contentScale = ContentScale.Fit)
        Text("✦ ANTİK TAPINAK ✦", fontSize = 27.sp, fontWeight = FontWeight.Black, color = Color(0xFFFFD77C))
        Text("Çarpmanın Sırları · " + runTier.title,
            color = Color(0xFFEAF4FA), fontSize = 15.sp)
        Text("Ritüeller: " + stage + "/4", color = Color(0xFF9FE9DA), fontSize = 14.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            repeat(4) { index -> Box(Modifier.weight(1f).height(9.dp).background(if (index < stage) Color(0xFFFFD77C) else Color(0xFF456A78), CircleShape)) }
        }
        if (seed == 0L || stage == 4) {
            Text(if (stage == 4) "Tapınak aydınlandı! Dört ritüel tamamlandı."
                else "Dört odada matematiksel düzenekleri çalıştır.",
                color = Color.White, fontSize = 20.sp)
            Text("Tamamlanan keşifler: " + checkpoint.runs, color = Color.White)
            if (stage == 4) Text("Bu tur: $attemptCount deneme · $errorCount yanlış · $hintCount ipucu", color = Color(0xFF9FE9DA))
            Button(onClick = {
                val now = System.currentTimeMillis().coerceAtLeast(1L)
                if (checkpoint.start(now, tier)) {
                    seed = now
                    runTier = tier
                    stage = 0
                } else message = "Yeni oturum kaydedilemedi."
            }) { Text(if (stage == 4) "Yeni keşif" else "Tapınağa gir") }
        } else {
            Text(names[stage], color = Color(0xFFFFD77C),
                fontWeight = FontWeight.Bold, fontSize = 23.sp)
            Surface(color = Color(0xFF18394E), shape = RoundedCornerShape(20.dp), border = androidx.compose.foundation.BorderStroke(1.dp, Color(0x88FFD77C)), modifier = Modifier.fillMaxWidth()) {
                Text(listOf("Kadim mozaikleri taş taş inşa et.", "Sütunların dengeli düzenini açığa çıkar.", "Kayıp çarpanı eş gruplarla keşfet.", "Aynada ters işlemin sırrını çöz.")[stage], Modifier.padding(15.dp), color = Color.White)
            }
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
                                            if (index == placed && placed < q.tiles) placed =
                                                TempleEngine.placeMosaicTile(placed, q.tiles)
                                        }
                                        .semantics {
                                            contentDescription = "Mozaik taşı " + (index + 1) +
                                                if (index < placed) ", yerleştirildi" else ", boş"
                                        },
                                    contentAlignment = Alignment.Center
                                ) { Text(if (index < placed) "◆" else "+", color = Color.White) }
                            }
                        }
                    }
                    Text("Yerleştirilen taş: " + placed + "/" + q.tiles, color = Color.White)
                    Button(enabled = placed == q.tiles, onClick = { record(true); advance() }) {
                        Text("Mozaik tamamlandı")
                    }
                }
                1 -> {
                    val q = quest.pillars
                    Text("Soldaki düzeni döndürerek iki tarafın eşit olduğunu kanıtla.",
                        color = Color.White)
                    Text(q.leftRows.toString() + " × " + q.leftColumns.toString() +
                        "    =    " + if (rotated) q.rightRows.toString() +
                        " × " + q.rightColumns.toString() else "? × ?",
                        color = Color(0xFFFFD77C), fontSize = 24.sp)
                    Button(onClick = { rotated = !rotated }) {
                        Text(if (rotated) "Dizilimi geri çevir" else "Sütunları döndür")
                    }
                    Button(enabled = rotated && TempleEngine.isBalanced(q),
                        onClick = { record(true); advance() }) { Text("Dengeyi doğrula") }
                }
                2 -> {
                    val q = quest.gate
                    Text(q.total.toString() + " taşı " + q.groups +
                        " eş gruba paylaştır. Her grupta kaç taş olur?", color = Color.White)
                    Text("Taşları eş gruplara sırayla dağıt.", color = Color(0xFF9FE9DA))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        repeat(q.groups) { group ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Grup " + (group + 1), color = Color.White, fontSize = 11.sp)
                                val count = distributed / q.groups + if (group < distributed % q.groups) 1 else 0
                                Text("$count taş", color = Color(0xFFFFD77C), fontSize = 12.sp)
                            }
                        }
                    }
                    Button(enabled = distributed < q.total, onClick = { distributed++ }) {
                        Text("Sıradaki gruba taş koy: $distributed / ${q.total}")
                    }
                    Button(enabled = distributed > 0, onClick = { distributed-- }) { Text("Son taşı geri al") }
                    if (distributed == q.total) Text("${q.groups} × ${q.itemsPerGroup} = ${q.total}", color = Color(0xFF9FE9DA))
                    Button(enabled = distributed == q.total,
                        onClick = { record(true); advance() }) { Text("Kapıyı aç") }
                }
                3 -> {
                    val q = quest.mirror
                    Text(q.leftFactor.toString() + " × " + q.rightFactor +
                        " = " + q.total + ". Aynadaki eksik sonucu bul:",
                        color = Color.White)
                    Text(q.total.toString() + " ÷ " + q.divisor + " = ?",
                        color = Color(0xFFFFD77C), fontSize = 27.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                        listOf(q.resultAfterDivision,
                            q.resultAfterDivision + 1,
                            (q.resultAfterDivision - 1).coerceAtLeast(1))
                            .distinct().forEach { value ->
                                OutlinedButton(onClick = {
                                    selected = value
                                    record(value == q.resultAfterDivision)
                                    message = if (value == q.resultAfterDivision)
                                        "Ters işlem bağlantısını buldun."
                                    else "Çarpma dizilimine tekrar bak."
                                }) { Text(value.toString()) }
                            }
                    }
                    Button(enabled = selected == q.resultAfterDivision,
                        onClick = { advance() }) { Text("Tapınağı aydınlat") }
                }
            }
            if (stage >= 2) {
                OutlinedButton(onClick = {
                    if (!hintSeen) { record(false, hint = true); hintSeen = true }
                }) { Text(if (hintSeen) "İpucu gösterildi" else "İpucu al") }
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
