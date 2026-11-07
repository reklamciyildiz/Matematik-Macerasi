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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Icon
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
import com.yigit033.matematikmacarasi.learning.LessonTeachingCatalog
import com.yigit033.matematikmacarasi.learning.LessonNarrator
import com.yigit033.matematikmacarasi.services.AudioService
import com.yigit033.matematikmacarasi.learning.Skill
import com.yigit033.matematikmacarasi.learning.LearningJourneyProgress
import com.yigit033.matematikmacarasi.learning.LearningPathCatalog
import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAvi2Activity
import com.yigit033.matematikmacarasi.games.sayiavi2.ForestAdventureActivity
import com.yigit033.matematikmacarasi.games.basic.AdditionGameActivity
import com.yigit033.matematikmacarasi.games.basic.MultiplicationGameActivity
import com.yigit033.matematikmacarasi.games.basic.DivisionGameActivity
import com.yigit033.matematikmacarasi.games.basic.SubtractionGameActivity
import com.yigit033.matematikmacarasi.games.geometry.GeometryCastleActivity
import com.yigit033.matematikmacarasi.games.fractions.FractionCrystalsActivity
import com.yigit033.matematikmacarasi.games.time.TimeTowerActivity
import com.yigit033.matematikmacarasi.games.interactive.MarketSimulatorActivity
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
    val grade = userClass.substringBefore(".").trim().toIntOrNull() ?: 1
    val earlyReader = grade <= 2
    val script = remember(lesson.id) { LessonTeachingCatalog.forLesson(lesson) }
    val narrator = remember(lesson.id) { LessonNarrator(context) }
    val sounds = remember(context) { AudioService.getInstance(context) }
    DisposableEffect(narrator) {
        onDispose { narrator.close() }
    }
    val lessonLifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lessonLifecycleOwner, narrator) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                narrator.stop()
            }
        }
        lessonLifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lessonLifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Old lesson completion / question session keys remain fully compatible.
    var stage by remember(lesson.id) {
        progress.open(lesson.id)
        mutableIntStateOf(progress.stage(lesson.id).coerceIn(0, 2))
    }
    val seed = remember(lesson.id) { progress.seed(lesson.id) }
    var index by remember(lesson.id) { mutableIntStateOf(progress.questionIndex(lesson.id)) }
    var wrong by remember(lesson.id) { mutableIntStateOf(progress.wrong(lesson.id)) }
    var hints by remember(lesson.id) { mutableIntStateOf(progress.hints(lesson.id)) }
    var workedStep by remember(lesson.id) {
        mutableIntStateOf(progress.workedStep(lesson.id).coerceIn(0, script.steps.lastIndex))
    }
    var conceptSelected by remember(lesson.id) { mutableIntStateOf(-1) }
    var conceptWrong by remember(lesson.id) { mutableIntStateOf(0) }
    var hinted by remember(lesson.id, index) { mutableStateOf(false) }
    var right by remember(lesson.id, index) { mutableStateOf(false) }
    var feedback by remember(lesson.id, index) { mutableStateOf("") }
    var finished by remember(lesson.id) { mutableStateOf(false) }
    val q = remember(lesson.id, seed, index) {
        LearningPathCatalog.question(lesson, seed, index)
    }
    val checkChoices = remember(lesson.id, seed) {
        if (seed % 2L == 0L) script.check.choices else script.check.choices.reversed()
    }
    val correctConceptIndex = checkChoices.indexOf(
        script.check.choices[script.check.correctIndex]
    )
    val accuracy = 400 / (4 + wrong)
    val moveTo: (Int) -> Unit = { next ->
        narrator.stop()
        stage = next
        progress.setStage(lesson.id, next)
    }
    val speechText = when {
        finished -> "Ders tamamlandı. Dört soruluk çalışmanı bitirdin."
        stage == 0 -> script.headline + ". " + script.explanationFor(grade)
        stage == 1 -> script.steps[workedStep]
        else -> q.text
    }

    // Young readers get a narrated first pass; older students can request audio.
    // No speech runs if Effects/Sound is disabled in the app settings.
    LaunchedEffect(narrator.available, stage, workedStep, index, finished) {
        if (earlyReader && narrator.available && narrator.audioAllowed &&
            !finished && stage in 0..2) {
            narrator.say(speechText)
        }
    }
    // Correct responses advance automatically; bad attempts do not count as learning.
    LaunchedEffect(right, index, finished) {
        if (right && !finished) {
            delay(1250)
            if (index == 3) {
                onFinish(wrong, hints)
                finished = true
            } else {
                index += 1
                progress.setQuestionIndex(lesson.id, index)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 15.dp, vertical = 12.dp)
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    listOf(Color(0xFF174765), Color(0xFF0E2D48))
                ),
                RoundedCornerShape(23.dp)
            )
            .border(1.dp, Color(0xFF508FA6), RoundedCornerShape(23.dp))
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .clickable(onClick = { narrator.stop(); onBack() })
                .heightIn(min = 52.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Ders ekranından konu kitaplığına dön",
                tint = HomeColors.MagicCyan)
            Spacer(Modifier.width(9.dp))
            Text("Konu Kitaplığına Dön", color = HomeColors.MagicCyan,
                fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        Text(lesson.title, fontSize = if (earlyReader) 23.sp else 21.sp,
            fontWeight = FontWeight.ExtraBold, color = Color.White)
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                when {
                    finished -> "DERS TAMAMLANDI"
                    stage == 0 -> "1/3 · KONUYU ANLA"
                    stage == 1 -> "2/3 · ÖRNEĞİ İNCELE"
                    else -> "3/3 · KAVRAMA KONTROLÜ"
                },
                modifier = Modifier.weight(1f),
                color = Color(0xFFFFD887), fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            OutlinedButton(
                onClick = {
                    if (narrator.speaking) narrator.stop()
                    else narrator.say(speechText)
                },
                enabled = narrator.available && narrator.audioAllowed,
                modifier = Modifier.heightIn(min = 46.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = 12.dp, vertical = 5.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, Color(0xFF75B8CC)
                ),
                shape = RoundedCornerShape(15.dp)
            ) {
                Icon(
                    when {
                        narrator.speaking -> Icons.Filled.Stop
                        narrator.available && narrator.audioAllowed -> Icons.Filled.VolumeUp
                        else -> Icons.Filled.VolumeOff
                    },
                    contentDescription = if (narrator.speaking) "Seslendirmeyi durdur"
                        else "Ders metnini sesli dinle",
                    tint = HomeColors.MagicCyan, modifier = Modifier.size(19.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(if (narrator.speaking) "Durdur" else "Dinle",
                    color = HomeColors.MagicCyan, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold)
            }
        }
        if (!narrator.loading && !narrator.available) {
            Text(
                "Türkçe çevrimdışı ses bu cihazda yüklü değil. Metni okuyabilirsin; seslendirme için cihazın metin okuma ayarlarından Türkçe çevrimdışı ses yükleyebilirsin.",
                color = Color(0xFFC2D8E5), fontSize = 11.sp,
                lineHeight = 15.sp
            )
        } else if (!narrator.audioAllowed) {
            Text("Ses, uygulamanın Ses Efektleri ayarından kapalı.",
                color = Color(0xFFC2D8E5), fontSize = 11.sp)
        }

        LinearProgressIndicator(
            progress = { when {
                finished -> 1f
                stage == 0 -> 0.12f
                stage == 1 -> .32f + .25f *
                    (workedStep + 1).toFloat() / script.steps.size.toFloat()
                else -> .62f + .38f * index.toFloat() / 4f
            } },
            modifier = Modifier.fillMaxWidth(),
            color = HomeColors.MagicCyan, trackColor = Color(0xFF2E5271)
        )

        if (finished) {
            Text("Kısa ders tamamlandı", color = HomeColors.MagicCyan,
                fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
            Text("4 soru · " + wrong + " yanlış deneme · " + hints + " ipucu",
                color = Color.White, fontSize = 14.sp)
            Text(
                if (progress.conceptErrors(lesson.id) == 0)
                    "Kavram sorusu: ilk denemede doğru"
                else "Kavram sorusu: " + progress.conceptErrors(lesson.id) +
                    " kez yeniden denendi",
                color = Color(0xFFE1DBA6), fontSize = 12.sp
            )
            Text("Bu çalışmadaki doğru hamle oranı: " + accuracy + "%",
                color = Color.White, fontSize = 14.sp)
            Text(
                "Bu sonuç tek başına konuyu tamamen öğrendiğin anlamına gelmez.",
                color = HomeColors.TextSecondary, fontSize = 12.sp, lineHeight = 17.sp
            )
            val target = when (lesson.id) {
                "count_2", "count_3" -> SayiAvi2Activity::class.java
                "count_4" -> ForestAdventureActivity::class.java
                "add_10", "add_20" -> AdditionGameActivity::class.java
                "multiply_intro", "multiply" -> MultiplicationGameActivity::class.java
                "divisible_15" -> DivisionGameActivity::class.java
                "g1_count20", "g2_place" -> SayiAvi2Activity::class.java
                "g1_takeaway" -> SubtractionGameActivity::class.java
                "g1_shapes", "g4_perimeter", "g5_area" ->
                    GeometryCastleActivity::class.java
                "g2_half", "g3_third", "g4_fraction", "g7_rational" ->
                    FractionCrystalsActivity::class.java
                "g2_clock" -> TimeTowerActivity::class.java
                "g3_money" -> MarketSimulatorActivity::class.java
                // No related deployed game for the remaining middle-school concepts:
                // do not offer an unrelated game with misleading skill matching.
                else -> null
            }
            if (target != null) {
                Button(
                    onClick = {
                        narrator.stop()
                        context.startActivity(Intent(context, target))
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
                ) {
                    Text("Öğrendiğini İlgili Oyunda Uygula",
                        color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                Text(
                    "Bu kazanımı doğrudan çalıştıran bir matematik oyunu henüz yok. " +
                        "Dersi tekrar ederek farklı sorular deneyebilirsin.",
                    color = HomeColors.TextSecondary,
                    fontSize = 12.sp, lineHeight = 18.sp
                )
            }
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)
            ) {
                Text("Konu Kitaplığına Dön")
            }
        } else if (stage == 0) {
            // Exposition: explain a single mathematical idea with visual evidence.
            Text(script.explanationFor(grade), color = Color.White,
                fontSize = if (earlyReader) 18.sp else if (grade <= 4) 16.sp else 15.sp,
                lineHeight = if (earlyReader) 27.sp else 22.sp)
            LessonTeachingIllustration(
                script = script, grade = grade,
                revealedStep = script.steps.lastIndex, showComplete = true
            )
            Text(
                if (earlyReader) "Resme bak, nesneleri say ve birlikte düşün."
                else "Modelin her bölümünü gözlemle, işlemin nedenini düşün.",
                color = HomeColors.TextSecondary,
                fontSize = 13.sp, lineHeight = 19.sp
            )
            Button(
                onClick = { moveTo(1) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 55.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HomeColors.MagicCyan)
            ) {
                Text("Adım Adım Çözüme Geç",
                    color = Color(0xFF09283B), fontWeight = FontWeight.Bold)
            }
        } else if (stage == 1) {
            // Guided discovery: image and large button are alternative targets.
            val revealNext: () -> Unit = {
                if (workedStep < script.steps.lastIndex) {
                    narrator.stop()
                    workedStep += 1
                    progress.saveWorkedStep(lesson.id, workedStep)
                    sounds.playTapSound()
                }
            }
            Text("Birlikte adım adım inceleyelim", color = Color.White,
                fontSize = 17.sp, fontWeight = FontWeight.Bold)
            LessonTeachingIllustration(
                script = script,
                grade = grade,
                revealedStep = workedStep,
                onRevealNext = if (workedStep < script.steps.lastIndex)
                    revealNext else null
            )
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(Color(0xFF245775), RoundedCornerShape(14.dp))
                    .padding(13.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    "ADIM " + (workedStep + 1) + " / " + script.steps.size,
                    color = Color(0xFFFFD887),
                    fontSize = 11.sp, fontWeight = FontWeight.Bold
                )
                Text(script.steps[workedStep], color = Color.White,
                    fontSize = if (earlyReader) 17.sp else 15.sp,
                    lineHeight = if (earlyReader) 24.sp else 21.sp)
            }
            if (workedStep < script.steps.lastIndex) {
                Button(
                    onClick = revealNext,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 53.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomeColors.MagicCyan)
                ) {
                    Text("Sonraki Çözüm Adımını Göster",
                        color = Color(0xFF09283B), fontWeight = FontWeight.Bold)
                }
            } else {
                // Actual conceptual check is about the mathematical relationship,
                // not merely picking the result of a calculation.
                Column(
                    modifier = Modifier.fillMaxWidth()
                        .background(Color(0xFF11354F), RoundedCornerShape(17.dp))
                        .border(1.dp, Color(0xFF438695), RoundedCornerShape(17.dp))
                        .padding(13.dp),
                    verticalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    Text("Anladım mı?", fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD887), fontSize = 16.sp)
                    Text(script.check.prompt, color = Color.White,
                        fontSize = if (earlyReader) 17.sp else 15.sp,
                        lineHeight = 22.sp)
                    checkChoices.forEachIndexed { choiceIndex, choice ->
                        val chosen = conceptSelected == choiceIndex
                        OutlinedButton(
                            onClick = {
                                conceptSelected = choiceIndex
                                if (choiceIndex != correctConceptIndex) {
                                    conceptWrong++
                                    progress.recordConceptError(lesson.id)
                                    sounds.playWrongSound()
                                    narrator.say(script.check.wrongHint)
                                } else {
                                    progress.markConceptCheckPassed(lesson.id)
                                    sounds.playCorrectSound()
                                    narrator.say(script.check.explanation)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().heightIn(min = 55.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (chosen && choiceIndex == correctConceptIndex)
                                    Color(0xFF7BE1C0)
                                else if (chosen) Color(0xFFFFB997)
                                else Color(0xFF5E93AB)
                            ),
                            shape = RoundedCornerShape(13.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (chosen) Color(0xFF2C6073)
                                else Color(0xFF1A4663)
                            )
                        ) {
                            Text(choice, color = Color.White,
                                fontSize = 13.sp, lineHeight = 18.sp)
                        }
                    }
                    if (conceptSelected >= 0) {
                        val rightChoice = conceptSelected == correctConceptIndex
                        Text(
                            if (rightChoice) script.check.explanation
                            else script.check.wrongHint,
                            color = if (rightChoice) Color(0xFF91E3D5)
                            else Color(0xFFFFCCAC),
                            fontSize = 13.sp, lineHeight = 19.sp
                        )
                        if (rightChoice) {
                            Button(
                                onClick = { moveTo(2) },
                                modifier = Modifier.fillMaxWidth().heightIn(min = 55.dp),
                                shape = RoundedCornerShape(13.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = HomeColors.MagicCyan)
                            ) {
                                Text("Kısa Alıştırmaya Geç", color = Color(0xFF09283B),
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } else {
            // Four varied, deterministic questions, retaining the previous
            // system's auto-advance and saved seed/attempt counters.
            Text("Soru " + (index + 1) + " / 4",
                color = HomeColors.TextSecondary, fontSize = 13.sp)
            Text(q.text, fontWeight = FontWeight.ExtraBold, color = Color.White,
                fontSize = if (earlyReader) 23.sp else 20.sp, lineHeight = 29.sp)
            q.choices.chunked(2).forEach { choices ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(9.dp)
                ) {
                    choices.forEach { choice ->
                        Button(
                            onClick = {
                                if (choice == q.answer) {
                                    right = true
                                    feedback = q.explanation
                                    sounds.playCorrectSound()
                                    narrator.stop()
                                } else {
                                    wrong++
                                    progress.recordWrong(lesson.id)
                                    sounds.playWrongSound()
                                    feedback = if (wrong > 1) script.check.wrongHint
                                    else "Bu henüz doğru değil. Sayıları yeniden incele."
                                    narrator.say(feedback)
                                }
                            },
                            enabled = !right,
                            modifier = Modifier.weight(1f).heightIn(min = 59.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (right && choice == q.answer)
                                    Color(0xFF1C8D78) else Color(0xFF285C82)
                            ),
                            shape = RoundedCornerShape(13.dp)
                        ) {
                            Text(choice.toString(), fontSize = 21.sp,
                                color = Color.White)
                        }
                    }
                }
            }
            if (feedback.isNotEmpty()) {
                Text(feedback, color = if (right) HomeColors.MagicCyan
                    else Color(0xFFFFD1AE), fontSize = 14.sp, lineHeight = 20.sp)
            }
            if (!right && !hinted) {
                OutlinedButton(
                    onClick = {
                        hinted = true
                        hints++
                        progress.recordHint(lesson.id)
                        narrator.say(lesson.hint)
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, Color(0xFF5EAFC3)
                    ),
                    shape = RoundedCornerShape(13.dp)
                ) {
                    Text("İpucu Göster", color = HomeColors.MagicCyan,
                        fontWeight = FontWeight.Bold)
                }
            }
            if (hinted && !right) {
                Text(lesson.hint, color = Color.White,
                    fontSize = 13.sp, lineHeight = 19.sp)
            }
            if (right) {
                Text("Doğru! " + (if (index == 3) "Dersin tamamlanıyor…"
                     else "Sonraki soruya geçiliyor…"),
                    color = HomeColors.MagicCyan, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold)
            }
        }
    }
}
