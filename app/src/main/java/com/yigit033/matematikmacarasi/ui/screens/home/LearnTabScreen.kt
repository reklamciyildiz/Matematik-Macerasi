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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    var showRemainingOnly by rememberSaveable(userClass) { mutableStateOf(false) }
    var topicQuery by rememberSaveable(userClass) { mutableStateOf("") }
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
    val reviewDueLessons = lessons.filter { progress.isReviewDue(it.id) }
        .sortedBy { progress.nextReviewAt(it.id) }
    val grade = curriculum.grade
    val searchText = topicQuery.trim().lowercase(java.util.Locale.forLanguageTag("tr-TR"))
    val listedTopics = curriculum.topics.filter { topic ->
        val unfinished = topic.lessonIds.any { !progress.completed(it) }
        val matches = searchText.isBlank() ||
            (topic.title + " " + topic.summary + " " + topic.area.label + " " +
                topic.outcomes.joinToString(" ")).lowercase(
                java.util.Locale.forLanguageTag("tr-TR")).contains(searchText)
        matches && (!showRemainingOnly || unfinished)
    }
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
                "Her konu için en az bir başlangıç dersi mevcut. Bunlar tüm müfredat kazanımlarının tamamı değildir.",
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

        if (reviewDueLessons.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(Color(0xFF1D5368), RoundedCornerShape(19.dp))
                    .border(1.dp, Color(0xFFE2B978), RoundedCornerShape(19.dp))
                    .padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.AutoStories, contentDescription = null,
                        tint = hubGold, modifier = Modifier.size(21.dp))
                    Spacer(Modifier.width(9.dp))
                    Text("Tekrar Etme Zamanı", color = Color.White,
                        fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
                Text(
                    "Önceden tamamladığın kısa dersleri yeni sorularla tekrar edebilirsin. " +
                        "Bu öneriler yalnızca cihazındaki denemelerden hesaplanır.",
                    color = hubText, fontSize = 12.sp, lineHeight = 18.sp
                )
                reviewDueLessons.take(3).forEach { lesson ->
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(Color(0xFF133A56), RoundedCornerShape(13.dp))
                            .clickable {
                                selectedTopicId = GradeCurriculumCatalog
                                    .topicForLesson(curriculum.grade, lesson.id)?.id
                                selectedLessonId = lesson.id
                            }
                            .heightIn(min = 51.dp).padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(lesson.title, color = Color.White,
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Tekrar Et", color = hubGold,
                            fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Filled.ChevronRight,
                            contentDescription = "Dersi tekrar aç", tint = hubGold)
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Text("Konu Kitaplığım",
                color = Color.White, fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold)
            Text(
                "Sınıfına uygun başlangıç derslerini konu başlıklarına göre keşfet.",
                color = hubText, fontSize = 12.sp, lineHeight = 17.sp
            )
            if (grade >= 3) {
                OutlinedTextField(
                    value = topicQuery,
                    onValueChange = { topicQuery = it.take(80) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = {
                        Text("Konu, kavram veya kazanım ara",
                            color = hubText, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null,
                            tint = hubCyan)
                    },
                    trailingIcon = {
                        if (topicQuery.isNotBlank()) {
                            Box(
                                modifier = Modifier.size(48.dp)
                                    .clickable { topicQuery = "" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Close,
                                    contentDescription = "Aramayı temizle",
                                    tint = hubCyan)
                            }
                        }
                    },
                    shape = RoundedCornerShape(15.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = hubCyan,
                        unfocusedBorderColor = Color(0xFF5088A5),
                        focusedContainerColor = hubOcean,
                        unfocusedContainerColor = hubOcean
                    )
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                listOf(false to "Tüm Konular", true to "Henüz Bitirmediklerim")
                    .forEach { (remaining, label) ->
                        Box(
                            modifier = Modifier.weight(1f)
                                .background(
                                    if (showRemainingOnly == remaining)
                                        Color(0xFF287586) else hubOcean,
                                    RoundedCornerShape(13.dp)
                                )
                                .border(
                                    1.dp,
                                    if (showRemainingOnly == remaining)
                                        hubCyan else Color(0xFF466D83),
                                    RoundedCornerShape(13.dp)
                                )
                                .clickable { showRemainingOnly = remaining }
                                .heightIn(min = 48.dp)
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label, color = Color.White,
                                fontSize = if (grade <= 2) 12.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        }
                    }
            }
            Text(
                listedTopics.size.toString() + " konu gösteriliyor · " +
                    lessons.size.toString() + " kullanılabilir kısa ders",
                color = hubText, fontSize = 11.sp
            )
        }

        if (listedTopics.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(hubOcean, RoundedCornerShape(17.dp))
                    .padding(19.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Bu filtrede konu bulunamadı",
                    color = Color.White, fontWeight = FontWeight.Bold)
                Text(
                    "Arama kelimesini değiştirebilir veya tüm konuları gösterebilirsin.",
                    color = hubText, fontSize = 12.sp
                )
                Button(
                    onClick = { topicQuery = ""; showRemainingOnly = false },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = hubCyan)
                ) {
                    Text("Tüm Konuları Göster", color = hubOcean,
                        fontWeight = FontWeight.Bold)
                }
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
private fun CurriculumTopicCard(
    topic: CurriculumTopic,
    done: Int,
    count: Int,
    onClick: () -> Unit
) {
    val accent = when (topic.area) {
        TopicArea.NUMBERS -> Color(0xFF90DDF2)
        TopicArea.OPERATIONS -> Color(0xFFFFD18A)
        TopicArea.GEOMETRY -> Color(0xFFB9B0FA)
        TopicArea.DATA -> Color(0xFF8EE1BE)
        TopicArea.ALGEBRA -> Color(0xFFFFBAA2)
        TopicArea.REVIEW -> Color(0xFFD2C8A7)
    }
    val icon = when (topic.area) {
        TopicArea.GEOMETRY -> Icons.Filled.School
        TopicArea.DATA -> Icons.Filled.BarChart
        TopicArea.REVIEW -> Icons.Filled.CheckCircle
        else -> Icons.Filled.AutoStories
    }
    Row(
        modifier = Modifier.fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    listOf(Color(0xFF215A70), Color(0xFF123951))
                ), RoundedCornerShape(18.dp)
            )
            .border(
                1.dp, accent.copy(alpha = 0.48f),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .heightIn(min = 82.dp)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Box(
            modifier = Modifier.size(43.dp)
                .background(
                    accent.copy(alpha = 0.18f),
                    RoundedCornerShape(13.dp)
                )
                .border(
                    1.dp, accent.copy(alpha = 0.45f),
                    RoundedCornerShape(13.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null,
                tint = accent, modifier = Modifier.size(23.dp))
        }
        Column(
            Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(topic.title, color = Color.White,
                fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(
                topic.summary, color = hubText,
                fontSize = 11.sp, lineHeight = 15.sp
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LinearProgressIndicator(
                    progress = { if (count == 0) 0f
                        else done.toFloat() / count.toFloat() },
                    modifier = Modifier.weight(1f).height(6.dp),
                    color = accent,
                    trackColor = Color(0xFF315D76)
                )
                Text(
                    done.toString() + "/" + count.toString(),
                    color = accent, fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                when {
                    count == 0 -> "Ders henüz eklenmedi"
                    done == count -> "Kısa dersler tamamlandı · Tekrar et"
                    else -> "Konuya giriş dersleri hazır"
                },
                color = accent, fontSize = 11.sp
            )
        }
        Icon(
            Icons.Filled.ChevronRight,
            contentDescription = "Konuyu incele: " + topic.title,
            tint = accent, modifier = Modifier.size(22.dp)
        )
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
                                else "Resimli anlatım · Adım adım örnek · Kavrama kontrolü",
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
