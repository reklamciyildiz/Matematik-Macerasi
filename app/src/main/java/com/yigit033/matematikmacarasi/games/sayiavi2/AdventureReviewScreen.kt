package com.yigit033.matematikmacarasi.games.sayiavi2

import android.content.Context
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

/**
 * Review attempts are saved separately from the original region achievements.
 * A misconception encountered here is revisited with a different question on the next review.
 */
internal class AdventureReviewHistory(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "adventure_reinforcement_v1", Context.MODE_PRIVATE
    )

    fun weakFocus(kind: AdventureReviewKind): Int? =
        prefs.getInt(kind.name + "_weak", 0).takeIf { it in 2..9 }

    fun noteError(kind: AdventureReviewKind, factor: Int): Boolean {
        if (factor !in 2..9) return false
        return prefs.edit().putInt(kind.name + "_weak", factor).commit()
    }

    fun totalReviews(kind: AdventureReviewKind): Int =
        prefs.getInt(kind.name + "_reviews", 0).coerceAtLeast(0)

    fun record(kind: AdventureReviewKind, errors: Int, hints: Int): Boolean {
        if (errors < 0 || hints < 0) return false
        val p = kind.name
        val edit = prefs.edit().putInt(p + "_reviews", totalReviews(kind) + 1)
            .putInt(p + "_last_errors", errors).putInt(p + "_last_hints", hints)
        if (errors == 0 && hints == 0) edit.remove(p + "_weak")
        return edit.commit()
    }
}

@Composable
private fun ReviewButton(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
        enabled = enabled, onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFFF5D386), contentColor = Color(0xFF102B3F)
        )
    ) { Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp) }
}

@Composable
private fun ReviewGroups(question: AdventureReviewQuestion) {
    // Only preview visual equal groups when the problem explicitly asks about groups or rows.
    if (!question.prompt.contains("grup") && !question.prompt.contains("sıra")) return
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        repeat(question.groups) { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp),
                modifier = Modifier.semantics {
                    contentDescription = "${row + 1}. eş grup, ${question.perGroup} taş"
                }) {
                repeat(question.perGroup) {
                    Box(Modifier.size(15.dp).background(
                        Color(0xFFFFD77C), CircleShape
                    ).border(1.dp, Color.White, CircleShape))
                }
            }
        }
    }
}

@Composable
fun AdventureReviewScreen(
    kind: AdventureReviewKind,
    tier: LearningTier,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val history = remember(context) { AdventureReviewHistory(context) }
    val seed = remember(kind, tier) { System.currentTimeMillis().coerceAtLeast(1L) }
    val questions = remember(kind, tier, seed) {
        AdventureReviewEngine.create(kind, tier, seed, history.weakFocus(kind))
    }
    var index by remember(kind, seed) { mutableIntStateOf(0) }
    var attempts by remember(kind, seed) { mutableIntStateOf(0) }
    var errors by remember(kind, seed) { mutableIntStateOf(0) }
    var hints by remember(kind, seed) { mutableIntStateOf(0) }
    var solved by remember(index, seed) { mutableStateOf(false) }
    var hintShown by remember(index, seed) { mutableStateOf(false) }
    var feedback by remember(index, seed) { mutableStateOf("") }
    var saveFailed by remember { mutableStateOf(false) }
    BackHandler(onBack = onExit)

    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(
                Color(0xFF1D5765), Color(0xFF0D2E43), Color(0xFF081C31)
            )))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        OutlinedButton(onClick = onExit, modifier = Modifier.heightIn(min = 48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF96EFE1))
        ) { Text("← Bölgeye dön") }
        Text("${kind.title} · Pekiştirme", color = Color(0xFFFFD77C),
            fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text("Kısa tekrar • ${tier.title}", color = Color(0xFFD9F8F1), fontSize = 14.sp)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            repeat(3) { section ->
                Box(Modifier.weight(1f).height(8.dp).background(
                    if (section < index) Color(0xFFFFD77C) else Color(0xFF44717D),
                    CircleShape
                ))
            }
        }
        if (index < questions.size) {
            val q = questions[index]
            Text("Soru ${index + 1} / ${questions.size}",
                color = Color(0xFF8CECE1), fontSize = 13.sp)
            Surface(
                color = Color(0xFF1E4658),
                shape = RoundedCornerShape(22.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCEA95E))
            ) {
                Column(Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text(q.prompt, color = Color.White, fontSize = 20.sp,
                        fontWeight = FontWeight.Bold, lineHeight = 28.sp)
                    ReviewGroups(q)
                    q.options.chunked(2).forEach { pair ->
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            pair.forEach { answer ->
                                Surface(
                                    modifier = Modifier.weight(1f).heightIn(min = 62.dp)
                                        .clickable(enabled = !solved) {
                                            attempts++
                                            if (answer == q.correct) {
                                                solved = true
                                                feedback = q.explanation
                                            } else {
                                                errors++
                                                if (!history.noteError(kind, q.focus))
                                                    saveFailed = true
                                                feedback = "Tekrar dene: " + q.hint
                                            }
                                        }.semantics {
                                            contentDescription = "Cevap seçeneği $answer"
                                        },
                                    shape = RoundedCornerShape(15.dp),
                                    color = Color(0xFF27637A),
                                    border = androidx.compose.foundation.BorderStroke(
                                        2.dp, Color(0xFF8DE9D9))
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text("$answer", color = Color.White, fontSize = 25.sp,
                                            fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            if (!hintShown) {
                                hintShown = true
                                hints++
                            }
                        },
                        enabled = !solved && !hintShown,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE4F8F4),
                            disabledContentColor = Color(0xFFBED1D4)
                        )
                    ) { Text(if (hintShown) "İpucu gösterildi" else "İpucu al") }
                    if (hintShown) Text(q.hint, color = Color(0xFF9EF6E7))
                    if (feedback.isNotBlank()) Text(feedback,
                        color = if (solved) Color(0xFF9EF6E7) else Color(0xFFFFD77C),
                        fontSize = 14.sp)
                    ReviewButton(if (index == 2) "Tekrarı bitir" else "Sonraki soru",
                        enabled = solved) {
                        if (index == questions.lastIndex) {
                            if (!history.record(kind, errors, hints)) saveFailed = true
                        }
                        index++
                    }
                }
            }
        } else {
            Surface(color = Color(0xFF1E4658),
                shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Tekrar tamamlandı", color = Color(0xFFFFD77C),
                        fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text("3 görev · $attempts deneme · $errors yanlış · $hints ipucu",
                        color = Color.White)
                    Text(if (errors > 0 || hints > 0)
                        "Zorlandığın ilişkiyi sonraki tekrar oturumunda farklı örnekle yeniden göreceksin."
                        else "Bu oturumda destek gerekmedi. Kalıcılığı sonraki oturumlarda kontrol et.",
                        color = Color(0xFFBDE7E4))
                    Text("Toplam kısa tekrar: ${history.totalReviews(kind)}",
                        color = Color(0xFFFFD77C))
                    Text("Bu sonuç oyun içi pratik verisidir; resmi kazanım yeterliliği değildir.",
                        color = Color(0xFFBDE7E4), fontSize = 11.sp)
                    ReviewButton("Bölgeye dön", onClick = onExit)
                }
            }
        }
        if (saveFailed) Text("Kayıt alınamadı. Cihaz depolamasını kontrol et.",
            color = Color(0xFFFFDB8A), fontSize = 13.sp)
        Spacer(Modifier.height(10.dp))
    }
}
