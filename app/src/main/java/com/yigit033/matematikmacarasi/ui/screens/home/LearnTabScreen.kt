package com.yigit033.matematikmacarasi.ui.screens.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.yigit033.matematikmacarasi.learning.CurriculumTopic
import com.yigit033.matematikmacarasi.learning.GradeCurriculumCatalog
import com.yigit033.matematikmacarasi.learning.InstructionBand
import com.yigit033.matematikmacarasi.learning.LearningJourneyProgress
import com.yigit033.matematikmacarasi.learning.LearningPathCatalog
import com.yigit033.matematikmacarasi.learning.TopicArea

private val hubOcean = Color(0xFF163E5E)
private val hubPanel = Color(0xFF174765)
private val hubCyan = Color(0xFF8CE7DF)
private val hubGold = Color(0xFFFFD78B)
private val hubText = Color(0xFFC7DFEB)

/**
 * Ö2 — Learning is a real lesson library, NOT a second game hub.
 * A topic in the curriculum index is distinct from a finished course.
 */
@Composable
fun LearnTabScreen(
    userClass: String,
    onNavigateToGames: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val progress = remember(context) { LearningJourneyProgress(context) }
    val curriculum = remember(userClass) { GradeCurriculumCatalog.forClass(userClass) }
    val lessons = remember(userClass) { LearningPathCatalog.forGrade(userClass) }
    var selectedTopicId by rememberSaveable(userClass) { mutableStateOf<String?>(null) }
    var selectedLessonId by rememberSaveable(userClass) { mutableStateOf<String?>(null) }
    var reportExpanded by rememberSaveable(userClass) { mutableStateOf(false) }
    var showAllTopics by rememberSaveable(userClass) { mutableStateOf(false) }
    val homeScroll = rememberScrollState()
    var revision by remember { mutableIntStateOf(0) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) revision++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (selectedLessonId != null) {
        LearningPracticeSection(
            userClass = userClass,
            startLessonId = selectedLessonId,
            onExit = { selectedLessonId = null; revision++ },
            onNavigateToGames = onNavigateToGames,
            modifier = modifier
        )
        return
    }

    val chosenTopic = curriculum.topics.firstOrNull { it.id == selectedTopicId }
    if (chosenTopic != null) {
        BackHandler { selectedTopicId = null }
        CurriculumTopicDetail(
            topic = chosenTopic,
            userClass = userClass,
            lessonComplete = { progress.completed(it) },
            onBack = { selectedTopicId = null },
            onStart = { lessonId ->
                selectedLessonId = lessonId
                revision++
            },
            modifier = modifier
        )
        return
    }
    val last = progress.lastLesson()?.let { id -> lessons.firstOrNull { it.id == id } }
    val next = if (last != null && !progress.completed(last.id)) last
        else lessons.firstOrNull { !progress.completed(it.id) } ?: last ?: lessons.firstOrNull()
    val completed = lessons.count { progress.completed(it.id) }
    val listedTopics = if (showAllTopics) curriculum.topics else curriculum.topics.filter { it.playable }
    val topicsByArea = listedTopics.groupBy { it.area }
    val ageLine = when (curriculum.band) {
        InstructionBand.VISUAL_AUDIO -> "Önce gör, sonra birlikte dene."
        InstructionBand.VISUAL_GUIDED -> "Örnekleri incele, adım adım uygula."
        InstructionBand.EXPLANATION_DIAGRAM -> "Kavramı öğren, çözümü sorgula, uygula."
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(homeScroll)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFF1E607F), Color(0xFF12405E), Color(0xFF0F304D))
                    ), RoundedCornerShape(23.dp)
                )
                .border(1.dp, Color(0xFF5B99B1), RoundedCornerShape(23.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(47.dp).background(
                        Color(0xFF276987), RoundedCornerShape(15.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.AutoStories, contentDescription = null,
                        tint = hubGold, modifier = Modifier.size(27.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Matematik Kitaplığım", color = Color.White,
                        fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
                    Text(userClass.ifBlank { "1. Sınıf" } + " · " + ageLine,
                        color = hubText, fontSize = 11.sp, lineHeight = 16.sp)
                }
            }
            Text(
                "2026–27 · " + curriculum.program,
                color = hubGold, fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
            if (userClass == "Ana Sınıfı") {
                Text("Ana sınıfı için özel program henüz yok. Burada 1. sınıf giriş konuları gösterilir.",
                    color = hubGold, fontSize = 11.sp, lineHeight = 16.sp)
            }
            Text(
                curriculum.topics.size.toString() + " konu başlığı · " +
                    curriculum.playableTopics.toString() + " konuda kullanılabilir kısa ders",
                color = hubText, fontSize = 12.sp
            )
            Text(
                "Konu listesi öğrenme hedeflerimizi gösterir; gri işaretli dersler henüz açılmadı.",
                color = Color(0xFFBBD7E6), fontSize = 11.sp, lineHeight = 16.sp
            )
        }

        if (next != null) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(Color(0xFF174765), RoundedCornerShape(19.dp))
                    .border(1.dp, hubGold.copy(alpha = 0.65f), RoundedCornerShape(19.dp))
                    .padding(17.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("KALDIĞIN YERDEN DEVAM ET", color = hubGold,
                        fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = hubGold)
                }
                Text(next.title, color = Color.White,
                    fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
                Text(
                    when (progress.stage(next.id)) {
                        1 -> "Çözümlü örnek aşamasındasın"
                        2 -> "Kısa alıştırma aşamasındasın"
                        else -> if (progress.completed(next.id)) "Bu kısa dersi yeniden çalışabilirsin"
                                else "Konu anlatımından başla"
                    },
                    color = hubText, fontSize = 12.sp
                )
                LinearProgressIndicator(
                    progress = { when (progress.stage(next.id)) {
                        1 -> 0.35f
                        2 -> 0.70f
                        3 -> 1f
                        else -> 0f
                    } },
                    modifier = Modifier.fillMaxWidth(),
                    color = hubCyan, trackColor = Color(0xFF325E75)
                )
                Button(
                    onClick = {
                        selectedTopicId = GradeCurriculumCatalog
                            .topicForLesson(curriculum.grade, next.id)?.id
                        selectedLessonId = next.id
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = hubGold)
                ) {
                    Text("Derse Devam Et", color = Color(0xFF09283D),
                        fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(5.dp))
                    Icon(Icons.Filled.ChevronRight, contentDescription = null,
                        tint = Color(0xFF09283D))
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
                .background(hubOcean, RoundedCornerShape(17.dp))
                .border(1.dp, Color(0xFF3E819B), RoundedCornerShape(17.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Kısa Ders İlerlemem", color = Color.White,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(completed.toString() + "/" + lessons.size.toString(),
                    color = hubCyan, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = { if (lessons.isEmpty()) 0f
                    else completed.toFloat() / lessons.size },
                modifier = Modifier.fillMaxWidth(),
                color = hubCyan, trackColor = Color(0xFF325E75)
            )
            Text(
                "Bu sayı yalnızca hazır kısa dersleri sayar; müfredatın tamamlanma oranı değildir.",
                color = hubText, fontSize = 11.sp, lineHeight = 16.sp
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (showAllTopics) "Konu Kitaplığım" else "Şimdi Öğrenebileceklerim",
                    color = Color.White, fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    if (showAllTopics) "Planlanan ve hazır konular"
                    else "Oynanabilir kısa dersler",
                    color = hubText, fontSize = 11.sp
                )
            }
            Box(
                modifier = Modifier.background(Color(0xFF285D79), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF4D91AB), RoundedCornerShape(12.dp))
                    .clickable { showAllTopics = !showAllTopics }
                    .heightIn(min = 44.dp)
                    .padding(horizontal = 11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (showAllTopics) "Hazır Dersler" else "Tüm Konular",
                    color = hubCyan, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        topicsByArea.forEach { (area, topics) ->
            Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        when (area) {
                            TopicArea.DATA -> Icons.Filled.BarChart
                            TopicArea.GEOMETRY -> Icons.Filled.School
                            else -> Icons.Filled.AutoStories
                        },
                        contentDescription = null, tint = hubGold,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(area.label, color = hubText,
                        fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                topics.forEach { topic ->
                    val readyCount = topic.lessonIds.size
                    val doneCount = topic.lessonIds.count { progress.completed(it) }
                    CurriculumTopicCard(
                        topic = topic,
                        done = doneCount,
                        count = readyCount,
                        onClick = { selectedTopicId = topic.id }
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth()
                .background(hubPanel, RoundedCornerShape(17.dp))
                .border(1.dp, Color(0xFF3E819B), RoundedCornerShape(17.dp))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickable { reportExpanded = !reportExpanded }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.BarChart, contentDescription = null,
                        tint = hubCyan, modifier = Modifier.size(21.dp))
                    Spacer(Modifier.width(9.dp))
                    Column {
                        Text("Gelişim Raporlarım", color = Color.White,
                            fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Oyun denemeleri ve tekrar önerileri", color = hubText,
                            fontSize = 11.sp)
                    }
                }
                Icon(Icons.Filled.ChevronRight,
                    contentDescription = if (reportExpanded) "Raporu kapat" else "Raporu aç",
                    tint = hubCyan)
            }
            if (reportExpanded) {
                AdventureLearningReport(userClass = userClass, refreshKey = revision)
                Spacer(Modifier.height(8.dp))
            }
        }

        Button(
            onClick = onNavigateToGames,
            modifier = Modifier.fillMaxWidth().heightIn(min = 49.dp),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HomeColors.MagicCyan)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null,
                tint = Color(0xFF09283B))
            Spacer(Modifier.width(6.dp))
            Text("Matematik Oyunlarına Git", color = Color(0xFF09283B),
                fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun CurriculumTopicCard(topic: CurriculumTopic, done: Int, count: Int,
                                onClick: () -> Unit) {
    val active = topic.playable
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(if (active) Color(0xFF194D68) else Color(0xFF153750),
                RoundedCornerShape(16.dp))
            .border(1.dp,
                if (active) Color(0xFF4D9A9F) else Color(0xFF345E78),
                RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(41.dp)
                .background(if (active) Color(0xFF2E777E) else Color(0xFF28506A),
                    RoundedCornerShape(13.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (active) Icons.Filled.AutoStories else Icons.Filled.School,
                contentDescription = null,
                tint = if (active) hubCyan else Color(0xFF97B6C7)
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(topic.title, color = Color.White,
                fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(topic.summary, color = hubText, fontSize = 11.sp, lineHeight = 15.sp)
            Text(
                if (active) done.toString() + "/" + count.toString() + " kısa ders · " +
                    (if (done == count) "Tamamlandı" else "Oynanabilir")
                else "Konu planlandı · Ders henüz hazırlanıyor",
                color = if (active) hubCyan else Color(0xFFC4CAD0),
                fontSize = 10.sp
            )
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = "Konuyu incele",
            tint = if (active) hubCyan else Color(0xFF9FB9C8))
    }
}

@Composable
private fun CurriculumTopicDetail(
    topic: CurriculumTopic,
    userClass: String,
    lessonComplete: (String) -> Boolean,
    onBack: () -> Unit,
    onStart: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val lessons = remember(userClass) { LearningPathCatalog.forGrade(userClass) }
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable(onClick = onBack)
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Konu kitaplığına geri dön",
                tint = hubCyan)
            Spacer(Modifier.width(7.dp))
            Text("Konu Kitaplığına Dön", color = hubCyan,
                fontWeight = FontWeight.Bold)
        }
        Column(
            modifier = Modifier.fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(Color(0xFF1B5A76), Color(0xFF123953))),
                    RoundedCornerShape(20.dp)
                )
                .border(1.dp, Color(0xFF6197AE), RoundedCornerShape(20.dp))
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(topic.area.label + " · " + userClass,
                color = hubGold, fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold)
            Text(topic.title, color = Color.White,
                fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
            Text(topic.summary, color = hubText,
                fontSize = 13.sp, lineHeight = 19.sp)
            Text("Bu konuda neler öğreneceğim?", color = Color.White,
                fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
            topic.outcomes.forEach { outcome ->
                Row(verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null,
                        tint = hubCyan, modifier = Modifier.size(18.dp))
                    Text(outcome, color = hubText, fontSize = 13.sp,
                        lineHeight = 18.sp)
                }
            }
        }
        if (topic.playable) {
            Text("Kullanılabilir Kısa Dersler", color = Color.White,
                fontSize = 17.sp, fontWeight = FontWeight.Bold)
            topic.lessonIds.forEach { id ->
                val lesson = lessons.firstOrNull { it.id == id }
                if (lesson != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(Color(0xFF183F5D), RoundedCornerShape(15.dp))
                            .border(1.dp, Color(0xFF4E90A4), RoundedCornerShape(15.dp))
                            .clickable { onStart(id) }
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(if (lessonComplete(id)) Icons.Filled.CheckCircle
                             else Icons.Filled.PlayArrow,
                            contentDescription = null, tint = hubCyan)
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(lesson.title, color = Color.White,
                                fontWeight = FontWeight.Bold)
                            Text(
                                if (lessonComplete(id)) "Tekrar çalış"
                                else "Anlatım · Örnek · 4 soruluk pratik",
                                color = hubText, fontSize = 11.sp
                            )
                        }
                        Icon(Icons.Filled.ChevronRight,
                            contentDescription = "Dersi aç", tint = hubCyan)
                    }
                }
            }
            Text(
                "Mevcut kısa dersler giriş niteliğindedir. Bu konuya ait tüm " +
                    "öğrenme hedeflerinin tamamlandığı anlamına gelmez.",
                color = hubText, fontSize = 11.sp, lineHeight = 16.sp
            )
            if (topic.relatedGame != null) {
                Text(
                    "Ders sonrasında önerilen macera: " + topic.relatedGame,
                    color = hubGold, fontSize = 12.sp
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(Color(0xFF203B53), RoundedCornerShape(15.dp))
                    .border(1.dp, Color(0xFF526C7F), RoundedCornerShape(15.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text("Ders içeriği hazırlanıyor", color = hubGold,
                    fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Bu konu, sınıfın öğrenme planında yer alıyor. Henüz bu " +
                        "konuda açılabilir bir ders bulunmadığı için boş bir " +
                        "ekrana veya yanlış sınıf sorularına yönlendirmiyoruz.",
                    color = hubText, fontSize = 12.sp, lineHeight = 18.sp
                )
            }
        }
        Spacer(Modifier.height(20.dp))
    }
}
