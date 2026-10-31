package com.yigit033.matematikmacarasi.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yigit033.matematikmacarasi.learning.adaptive.GradePolicy
import com.yigit033.matematikmacarasi.learning.curriculum.CurriculumSkillCatalog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.Lesson
import com.yigit033.matematikmacarasi.learning.LearningPathCatalog
import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAvi2Activity
import com.yigit033.matematikmacarasi.games.sayiavi2.ForestAdventureActivity

/** Eski yıldız ve Premium anahtarlarını değiştirmeyen ayrı ders kaydı. */
private class PilotProgress(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("p2_learning_pilot_v1", Context.MODE_PRIVATE)
    fun completed(id: String) = prefs.getBoolean("done_$id", false)
    fun best(id: String): Int? = if (prefs.contains("best_$id")) prefs.getInt("best_$id", 0) else null
    fun runs(id: String) = prefs.getInt("runs_$id", 0)
    fun save(id: String, wrong: Int, hints: Int) {
        val accuracy = 400 / (4 + wrong)
        prefs.edit().putBoolean("done_$id", true)
            .putInt("best_$id", maxOf(best(id) ?: 0, accuracy))
            .putInt("runs_$id", runs(id) + 1)
            .putInt("last_wrong_$id", wrong)
            .putInt("last_hints_$id", hints)
            .apply()
    }
}

@Composable
fun LearningPracticeSection(userClass: String) {
    val context = LocalContext.current
    val store = remember(context) { PilotProgress(context) }
    val lessons = remember(userClass) { LearningPathCatalog.forGrade(userClass) }
    var selectedId by remember(userClass) { mutableStateOf<String?>(null) }
    var version by remember { mutableIntStateOf(0) }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) version++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val selected = lessons.firstOrNull { it.id == selectedId }
    if (selected != null) {
        BackHandler { selectedId = null }
        PracticeLesson(selected,
            onBack = { selectedId = null },
            onFinish = { wrong, hints -> store.save(selected.id, wrong, hints); version++ })
        return
    }
    val completed = remember(version, userClass) { lessons.count { store.completed(it.id) } }
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xFF173654), RoundedCornerShape(18.dp))
            .border(1.dp, HomeColors.MagicCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Text("Öğrenme Patikam", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        Text(
            "${userClass.ifBlank { "1. Sınıf" }} · ${if (userClass.substringBefore(".").toIntOrNull()?.let { it >= 5 } == true) "Temel tekrar pilotu" else "İlk dersler"}",
            fontSize = 12.sp, color = HomeColors.TextSecondary
        )
        Text("$completed / ${lessons.size} ders tamamlandı", fontSize = 13.sp, color = HomeColors.MagicCyan)
        LinearProgressIndicator(progress = { completed.toFloat() / lessons.size },
            modifier = Modifier.fillMaxWidth(), color = HomeColors.MagicCyan,
            trackColor = Color(0xFF2E5271))
        lessons.forEachIndexed { i, lesson ->
            val done = store.completed(lesson.id)
            Column(modifier = Modifier.fillMaxWidth()
                .background(HomeColors.CardSurface, RoundedCornerShape(14.dp))
                .border(1.dp, HomeColors.CardBorderSubtle, RoundedCornerShape(14.dp))
                .clickable { selectedId = lesson.id }.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${i + 1}. ${lesson.title}", fontWeight = FontWeight.Bold,
                        fontSize = 14.sp, color = HomeColors.TextPrimary)
                    Text(if (done) "Tamamlandı" else "Başla", color = HomeColors.MagicCyan, fontSize = 12.sp)
                }
                Text(lesson.teaching, color = HomeColors.TextSecondary, fontSize = 12.sp)
                Text(CurriculumSkillCatalog.displayStatus(GradePolicy.parse(userClass), lesson.id),
                    color = HomeColors.MagicCyan, fontSize = 10.sp)
                if (done) {
                    Text("En iyi doğru hamle: ${store.best(lesson.id)}% · ${store.runs(lesson.id)} oturum",
                        color = HomeColors.MagicCyan, fontSize = 11.sp)
                }
            }
        }
        AdventureLearningReport(userClass, version)
        Text("Her ders 4 soruluk bir başlangıç çalışmasıdır. Oturum puanı öğrenme düzeyini kanıtlamaz. Yıldız ve ücretli hak verilmez.",
            color = HomeColors.TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun PracticeLesson(lesson: Lesson, onBack: () -> Unit, onFinish: (Int, Int) -> Unit) {
    val context = LocalContext.current
    val seed = remember(lesson.id) { System.nanoTime() }
    var index by remember(lesson.id) { mutableIntStateOf(0) }
    var wrong by remember(lesson.id) { mutableIntStateOf(0) }
    var hints by remember(lesson.id) { mutableIntStateOf(0) }
    var hinted by remember(lesson.id, index) { mutableStateOf(false) }
    var right by remember(lesson.id, index) { mutableStateOf(false) }
    var feedback by remember(lesson.id, index) { mutableStateOf("") }
    var finished by remember(lesson.id) { mutableStateOf(false) }
    val q = remember(lesson.id, seed, index) { LearningPathCatalog.question(lesson, seed, index) }
    val percentage = 400 / (4 + wrong)
    Column(modifier = Modifier.fillMaxWidth()
        .background(Color(0xFF173654), RoundedCornerShape(18.dp))
        .border(1.dp, HomeColors.MagicCyan.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
        .padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("← Patikaya Dön", modifier = Modifier.clickable { onBack() },
            fontSize = 13.sp, color = HomeColors.MagicCyan)
        Text(lesson.title, fontSize = 21.sp, fontWeight = FontWeight.Bold, color = Color.White)
        if (finished) {
            Text("Ders tamamlandı", color = HomeColors.MagicCyan, fontSize = 18.sp,
                fontWeight = FontWeight.Bold)
            Text("4 soru · $wrong yanlış deneme · $hints ipucu",
                color = HomeColors.TextPrimary, fontSize = 14.sp)
            Text("Doğru hamle oranı: $percentage%", color = HomeColors.TextPrimary)
            Text("Bu sonuç sadece oturumdaki hamleleri gösterir.",
                color = HomeColors.TextSecondary, fontSize = 11.sp)
            if (lesson.id == "count_3" || lesson.id == "count_4") {
                val adventure = if (lesson.id == "count_3") SayiAvi2Activity::class.java
                    else ForestAdventureActivity::class.java
                Button(
                    onClick = { context.startActivity(Intent(context, adventure)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
                ) {
                    Text(if (lesson.id == "count_3") "Sayı Avcıları'nda Uygula ▶"
                        else "Gizemli Orman'da Uygula ▶", color = Color.White)
                }
            }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) { Text("Patikaya Dön") }
        } else {
            Text(lesson.teaching, color = HomeColors.TextPrimary, fontSize = 14.sp)
            Text("Örnek: ${lesson.example}", color = HomeColors.MagicCyan, fontSize = 13.sp)
            Text("Soru ${index + 1}/4", color = HomeColors.TextSecondary, fontSize = 12.sp)
            LinearProgressIndicator(progress = { index / 4f }, modifier = Modifier.fillMaxWidth(),
                color = HomeColors.MagicCyan, trackColor = Color(0xFF2E5271))
            Text(q.text, fontWeight = FontWeight.ExtraBold, color = Color.White, fontSize = 22.sp)
            q.choices.chunked(2).forEach { choices ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    choices.forEach { choice ->
                        Button(onClick = {
                            if (choice == q.answer) {
                                right = true
                                feedback = q.explanation
                            } else {
                                wrong++
                                feedback = "Bu sonuç doğru değil; yeniden dene veya ipucu al."
                            }
                        }, enabled = !right, modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF285C82))) {
                            Text("$choice", fontSize = 18.sp, color = Color.White)
                        }
                    }
                }
            }
            if (feedback.isNotEmpty()) Text(feedback, fontSize = 13.sp,
                color = if (right) HomeColors.MagicCyan else Color(0xFFFFC7A2))
            if (!right && !hinted) Text("İpucu Göster", modifier = Modifier.clickable {
                hinted = true
                hints++
            }, color = HomeColors.MagicCyan, fontSize = 13.sp)
            if (hinted && !right) Text(lesson.hint,
                color = HomeColors.TextPrimary, fontSize = 13.sp)
            if (right) Button(
                onClick = {
                    if (index == 3) {
                        onFinish(wrong, hints)
                        finished = true
                    } else index++
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
            ) { Text(if (index == 3) "Dersi Tamamla" else "Sonraki Soru") }
        }
    }
}
