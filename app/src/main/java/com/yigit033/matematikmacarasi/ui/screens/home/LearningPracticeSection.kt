package com.yigit033.matematikmacarasi.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.yigit033.matematikmacarasi.learning.adaptive.GradePolicy
import com.yigit033.matematikmacarasi.learning.curriculum.CurriculumSkillCatalog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.Lesson
import com.yigit033.matematikmacarasi.learning.Skill
import com.yigit033.matematikmacarasi.learning.LearningJourneyProgress
import com.yigit033.matematikmacarasi.learning.LearningPathCatalog
import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAvi2Activity
import com.yigit033.matematikmacarasi.games.sayiavi2.ForestAdventureActivity
import com.yigit033.matematikmacarasi.games.basic.AdditionGameActivity
import com.yigit033.matematikmacarasi.games.basic.MultiplicationGameActivity
import com.yigit033.matematikmacarasi.games.basic.DivisionGameActivity
import kotlinx.coroutines.delay

@Composable
fun LearningPracticeSection(
    userClass: String,
    startLessonId: String? = null,
    onExit: () -> Unit = {},
    onNavigateToGames: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val store = remember(context) { LearningJourneyProgress(context) }
    val lessons = remember(userClass) { LearningPathCatalog.forGrade(userClass) }
    var selectedId by remember(userClass, startLessonId) { mutableStateOf(startLessonId) }
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
        val back: () -> Unit = {
            if (startLessonId != null) onExit()
            else selectedId = null
        }
        BackHandler { back() }
        PracticeLesson(selected, userClass, store,
            onBack = back,
            onFinish = { wrong, hints ->
                store.complete(selected.id, wrong, hints)
                version++
            },
            onNavigateToGames = onNavigateToGames,
            modifier = modifier)
        return
    }
    val completed = remember(version, userClass) { lessons.count { store.completed(it.id) } }
    Column(
        modifier = modifier.fillMaxWidth()
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
        Text("Her ders 4 soruluk bir başlangıç çalışmasıdır. Oturum puanı öğrenme düzeyini kanıtlamaz. Yıldız ve ücretli hak verilmez.",
            color = HomeColors.TextSecondary, fontSize = 11.sp)
    }
}

@Composable
private fun PracticeLesson(
    lesson: Lesson,
    userClass: String,
    progress: LearningJourneyProgress,
    onBack: () -> Unit,
    onFinish: (Int, Int) -> Unit,
    onNavigateToGames: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    // A saved phase and a saved seed/index make "continue learning" real.
    // The existing completion key is preserved for history and profile cards.
    var stage by remember(lesson.id) {
        progress.open(lesson.id)
        mutableIntStateOf(progress.stage(lesson.id).coerceIn(0, 2))
    }
    val seed = remember(lesson.id) { progress.seed(lesson.id) }
    var index by remember(lesson.id) { mutableIntStateOf(progress.questionIndex(lesson.id)) }
    var wrong by remember(lesson.id) { mutableIntStateOf(progress.wrong(lesson.id)) }
    var hints by remember(lesson.id) { mutableIntStateOf(progress.hints(lesson.id)) }
    var hinted by remember(lesson.id, index) { mutableStateOf(false) }
    var right by remember(lesson.id, index) { mutableStateOf(false) }
    var feedback by remember(lesson.id, index) { mutableStateOf("") }
    var finished by remember(lesson.id) { mutableStateOf(false) }
    val grade = userClass.substringBefore(".").trim().toIntOrNull() ?: 1
    val childText = grade <= 2
    val q = remember(lesson.id, seed, index) { LearningPathCatalog.question(lesson, seed, index) }
    val accuracy = 400 / (4 + wrong)
    val moveTo: (Int) -> Unit = { next ->
        stage = next
        progress.setStage(lesson.id, next)
    }

    LaunchedEffect(right, index, finished) {
        if (right && !finished) {
            delay(1200)
            if (index == 3) {
                onFinish(wrong, hints)
                finished = true
            } else {
                index++
                progress.setQuestionIndex(lesson.id, index)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .background(Color(0xFF153D5B), RoundedCornerShape(22.dp))
            .border(1.dp, Color(0xFF4B9BAA), RoundedCornerShape(22.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("← Konu Kitaplığına Dön", modifier = Modifier
            .clickable { onBack() }.heightIn(min = 45.dp),
            fontSize = 14.sp, color = HomeColors.MagicCyan,
            fontWeight = FontWeight.Bold)
        Text(lesson.title, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold,
            color = Color.White)
        Text(
            when {
                finished -> "KISA DERS TAMAMLANDI"
                stage == 0 -> "1 / 3 · KONUYU ÖĞREN"
                stage == 1 -> "2 / 3 · ÇÖZÜMLÜ ÖRNEK"
                else -> "3 / 3 · SIRA SENDE"
            },
            color = Color(0xFFFFD887), fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        LinearProgressIndicator(
            progress = { when {
                finished -> 1f
                stage == 0 -> 0.10f
                stage == 1 -> 0.36f
                else -> 0.65f + index / 4f * 0.35f
            } },
            modifier = Modifier.fillMaxWidth(),
            color = HomeColors.MagicCyan, trackColor = Color(0xFF2E5271)
        )

        if (finished) {
            Text("Ders tamamlandı", color = HomeColors.MagicCyan,
                fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
            Text("4 soru · " + wrong + " yanlış deneme · " + hints + " ipucu",
                color = Color.White, fontSize = 14.sp)
            Text("Bu oturumdaki doğru hamle oranı: " + accuracy + "%",
                color = Color.White, fontSize = 14.sp)
            Text("Bu sonuç konunun tamamen öğrenildiğini kanıtlamaz.",
                color = HomeColors.TextSecondary, fontSize = 12.sp)
            val target = when (lesson.id) {
                "count_2", "count_3" -> SayiAvi2Activity::class.java
                "count_4" -> ForestAdventureActivity::class.java
                "add_10", "add_20" -> AdditionGameActivity::class.java
                "multiply_intro", "multiply" -> MultiplicationGameActivity::class.java
                "divisible_15" -> DivisionGameActivity::class.java
                else -> null
            }
            if (target != null) {
                Button(
                    onClick = { context.startActivity(Intent(context, target)) },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 51.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E)),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text("Öğrendiğini İlgili Oyunda Uygula", color = Color.White)
                }
            } else {
                Button(
                    onClick = onNavigateToGames,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 51.dp)
                ) { Text("Matematik Oyunlarına Geç") }
            }
            Button(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
                Text("Konu Kitaplığına Dön")
            }
        } else if (stage == 0) {
            Text(lesson.teaching, color = Color.White,
                fontSize = if (childText) 18.sp else 15.sp,
                lineHeight = if (childText) 27.sp else 22.sp)
            LessonVisualExample(lesson, childText)
            Text(
                if (childText) "Sayıları ve grupları incele. Hazır olunca örneğe geç."
                else "Anlatımı inceleyip örnek üzerinden mantığı birlikte kur.",
                color = HomeColors.TextSecondary, fontSize = 13.sp
            )
            Button(onClick = { moveTo(1) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HomeColors.MagicCyan)) {
                Text("Adım Adım Örneğe Geç", color = Color(0xFF09283B),
                    fontWeight = FontWeight.Bold)
            }
        } else if (stage == 1) {
            Text("Örneği birlikte inceleyelim", color = Color.White,
                fontWeight = FontWeight.Bold, fontSize = 17.sp)
            LessonVisualExample(lesson, childText)
            Text("Örnek: " + lesson.example, color = HomeColors.MagicCyan,
                fontSize = if (childText) 20.sp else 17.sp,
                fontWeight = FontWeight.Bold)
            Text(lesson.hint, color = Color.White, fontSize = 14.sp, lineHeight = 21.sp)
            Button(onClick = { moveTo(2) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                shape = RoundedCornerShape(13.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HomeColors.MagicCyan)) {
                Text("Şimdi Ben Deneyeyim", color = Color(0xFF09283B),
                    fontWeight = FontWeight.Bold)
            }
        } else {
            Text("Soru " + (index + 1) + " / 4", color = HomeColors.TextSecondary,
                fontSize = 13.sp)
            Text(q.text, fontWeight = FontWeight.ExtraBold,
                color = Color.White, fontSize = if (childText) 23.sp else 21.sp,
                lineHeight = 29.sp)
            q.choices.chunked(2).forEach { choices ->
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    choices.forEach { choice ->
                        Button(
                            onClick = {
                                if (choice == q.answer) {
                                    right = true
                                    feedback = q.explanation
                                } else {
                                    wrong++
                                    progress.recordWrong(lesson.id)
                                    feedback = "Bir daha dene. İstersen ipucu kullan."
                                }
                            },
                            enabled = !right, modifier = Modifier.weight(1f)
                                .heightIn(min = 57.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF285C82)),
                            shape = RoundedCornerShape(13.dp)
                        ) { Text(choice.toString(), fontSize = 21.sp, color = Color.White) }
                    }
                }
            }
            if (feedback.isNotEmpty()) {
                Text(feedback, color = if (right) HomeColors.MagicCyan
                    else Color(0xFFFFD1AE), fontSize = 14.sp, lineHeight = 20.sp)
            }
            if (!right && !hinted) {
                Text("İpucu Göster", modifier = Modifier.clickable {
                    hinted = true
                    hints++
                    progress.recordHint(lesson.id)
                }.heightIn(min = 45.dp), color = HomeColors.MagicCyan,
                    fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            if (hinted && !right) Text(lesson.hint,
                color = Color.White, fontSize = 13.sp, lineHeight = 19.sp)
            if (right) Text(
                "Doğru! " + (if (index == 3) "Dersin tamamlanıyor…"
                             else "Sonraki soruya geçiliyor…"),
                color = HomeColors.MagicCyan, fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/** Concept visualization, not a second game. For young readers it uses big,
 * high-contrast counters; older students see the same concise mathematical model.
 * Spoken explanations and richer diagrams belong to the follow-on Ö3 content phase.
 */
@Composable
private fun LessonVisualExample(lesson: Lesson, childText: Boolean) {
    val primary = when (lesson.skill) {
        Skill.ADD_10 -> 3
        Skill.ADD_20 -> 8
        Skill.MULTIPLY, Skill.MULTIPLY_INTRO -> 3
        Skill.COUNT_2, Skill.COUNT_3, Skill.COUNT_4 -> 4
        Skill.DIVISIBLE_15 -> 5
    }
    val secondary = when (lesson.skill) {
        Skill.ADD_10 -> 2
        Skill.ADD_20 -> 5
        Skill.MULTIPLY, Skill.MULTIPLY_INTRO -> 4
        else -> 0
    }
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xFF0F2F49), RoundedCornerShape(17.dp))
            .border(1.dp, Color(0xFF386F86), RoundedCornerShape(17.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        if (childText || secondary > 0) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(primary.coerceAtMost(8)) {
                    Box(Modifier.padding(2.dp).size(if (childText) 21.dp else 14.dp)
                        .background(Color(0xFFFFD782), CircleShape))
                }
                if (secondary > 0) {
                    Text(" + ", color = Color.White, fontSize = 19.sp)
                    repeat(secondary) {
                        Box(Modifier.padding(2.dp).size(if (childText) 21.dp else 14.dp)
                            .background(Color(0xFF76DDD6), CircleShape))
                    }
                }
            }
        }
        Text(lesson.example, modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFFFD782), fontSize = if (childText) 20.sp else 16.sp,
            fontWeight = FontWeight.ExtraBold)
    }
}
