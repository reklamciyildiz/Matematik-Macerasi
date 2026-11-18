package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.yigit033.matematikmacarasi.learning.CurriculumTopic
import com.yigit033.matematikmacarasi.learning.LearningAccessPolicy
import com.yigit033.matematikmacarasi.learning.LearningAccessMigration
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import com.yigit033.matematikmacarasi.learning.GradeCurriculumCatalog
import com.yigit033.matematikmacarasi.learning.InstructionBand
import com.yigit033.matematikmacarasi.learning.LearningJourneyProgress
import com.yigit033.matematikmacarasi.learning.LearningPathCatalog
import com.yigit033.matematikmacarasi.learning.LearningJourneyCatalog
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
    val reducedColumnsForLargeText = LocalDensity.current.fontScale > 1.2f
    val progress = remember(context) { LearningJourneyProgress(context) }
    val curriculum = remember(userClass) { GradeCurriculumCatalog.forClass(userClass) }
    val lessons = remember(userClass) { LearningPathCatalog.forGrade(userClass) }
    val purchaseManager = remember(context) { PurchaseManager.getInstance(context) }
    val purchaseFlow by purchaseManager.premiumState.collectAsState()
    var revision by remember { mutableIntStateOf(0) }
    val migration = remember(context) { LearningAccessMigration(context) }
    val grandfathered = remember(context) { migration.grandfatheredIds() }
    val grade = curriculum.grade
    // The reactive Play/Cloudflare flow invalidates this local snapshot
    // after purchase restoration, without trusting stale union state.
    val isPremium = remember(purchaseFlow, revision) {
        purchaseManager.isPremiumSync()
    }
    val canOpen: (String) -> Boolean = { id ->
        LearningAccessPolicy.accessible(grade, id,
            purchaseManager.isPremiumSync(), grandfathered)
    }
    var selectedTopicId by rememberSaveable(userClass) { mutableStateOf<String?>(null) }
    var selectedLessonId by rememberSaveable(userClass) { mutableStateOf<String?>(null) }
    var activeJourneyId by rememberSaveable(userClass) { mutableStateOf<String?>(null) }
    var lockedLessonId by rememberSaveable(userClass) { mutableStateOf<String?>(null) }
    var reportExpanded by rememberSaveable(userClass) { mutableStateOf(false) }
    var showRemainingOnly by rememberSaveable(userClass) { mutableStateOf(false) }
    var selectedAreaName by rememberSaveable(userClass) { mutableStateOf<String?>(null) }
    var topicQuery by rememberSaveable(userClass) { mutableStateOf("") }
    val homeScroll = rememberScrollState()
    val journeys = remember(userClass) { LearningJourneyCatalog.forGrade(grade) }
    val lessonTitles = remember(userClass) { lessons.associate { it.id to it.title } }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                purchaseManager.refreshPremiumState()
                revision++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    lockedLessonId?.let { lockedId ->
        val title = lessons.firstOrNull { it.id == lockedId }?.title ?: "Öğren Dersi"
        LearningPremiumDialog(title = title, onDismiss = { lockedLessonId = null })
    }
    val openLesson: (String) -> Unit = { id ->
        activeJourneyId = null
        if (canOpen(id)) {
            selectedTopicId = GradeCurriculumCatalog.topicForLesson(grade, id)?.id
            selectedLessonId = id
            revision++
        } else {
            lockedLessonId = id
        }
    }
    val openJourneyLesson: (String, String) -> Unit = { id, routeId ->
        if (canOpen(id)) {
            selectedTopicId = null // return to the actual learning-journey library
            selectedLessonId = id
            activeJourneyId = routeId
            revision++
        } else {
            lockedLessonId = id // do not silently bypass the Premium entitlement
        }
    }

    // Take a stable snapshot; Compose's delegated mutableState var is not
    // smart-castable and must not be captured nullable by route callbacks.
    val activeLesson = selectedLessonId
    if (activeLesson != null) {
        val activeQuest = journeys.firstOrNull {
            it.id == activeJourneyId && activeLesson in it.lessonIds
        }
        val currentPosition = activeQuest?.lessonIds?.indexOf(activeLesson) ?: -1
        val routeNextLessonId = activeQuest?.lessonIds?.getOrNull(currentPosition + 1)
        LearningPracticeSection(
            userClass = userClass,
            startLessonId = activeLesson,
            nextJourneyLessonId = routeNextLessonId,
            onOpenNextJourneyLesson = { id ->
                activeJourneyId?.let { routeId -> openJourneyLesson(id, routeId) }
            },
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
            lessonAccessible = canOpen,
            onBack = { selectedTopicId = null },
            onStart = openLesson,
            modifier = modifier
        )
        return
    }
    val last = progress.lastLesson()?.let { id -> lessons.firstOrNull { it.id == id } }
    val next = if (last != null && !progress.completed(last.id) && canOpen(last.id)) last
        else lessons.firstOrNull { !progress.completed(it.id) && canOpen(it.id) }
            ?: lessons.firstOrNull { !progress.completed(it.id) }
            ?: last ?: lessons.firstOrNull()
    val completed = lessons.count { progress.completed(it.id) }
    val reviewDueLessons = lessons.filter { progress.isReviewDue(it.id) }
        .sortedBy { progress.nextReviewAt(it.id) }
    val searchText = topicQuery.trim().lowercase(java.util.Locale.forLanguageTag("tr-TR"))
    val listedTopics = curriculum.topics.filter { topic ->
        val unfinished = topic.lessonIds.any { !progress.completed(it) }
        val matches = searchText.isBlank() ||
            (topic.title + " " + topic.summary + " " + topic.area.label + " " +
                topic.outcomes.joinToString(" ")).lowercase(
                java.util.Locale.forLanguageTag("tr-TR")).contains(searchText)
        matches && (!showRemainingOnly || unfinished) &&
            (selectedAreaName == null || selectedAreaName == topic.area.name)
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
                    Text("Matematik Keşif Kitaplığım", color = Color.White,
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
                if (isPremium)
                    "Premium etkin · Tüm mevcut kısa dersler açık. Tam müfredat kapsamı değildir."
                else
                    "Her sınıfta 2 ücretsiz başlangıç dersi; diğerleri Premium. Tam müfredat değildir.",
                color = Color(0xFFBBD7E6), fontSize = 11.sp, lineHeight = 16.sp
            )
        }

        if (journeys.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "Öğrenme Yolculuklarım",
                    color = Color.White,
                    fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    "Kavramı keşfet, çözümlü örneği incele, kendin çöz, kısa değerlendirme yap ve oyunla pekiştir. Bu yollar hazır kısa derslerden oluşur; tam müfredat kapsamı değildir.",
                    color = hubText, fontSize = 12.sp, lineHeight = 18.sp
                )
                Text(
                    "Diğer yolculuklar için yana kaydır.",
                    color = hubCyan, fontSize = 11.sp
                )
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(journeys, key = { it.id }) { quest ->
                        LearningJourneyRouteCard(
                            quest = quest,
                            lessonTitles = lessonTitles,
                            lessonDone = { progress.completed(it) },
                            canOpen = canOpen,
                            onOpen = { lessonId ->
                                openJourneyLesson(lessonId, quest.id)
                            },
                            modifier = Modifier.width(292.dp)
                        )
                    }
                }
            }
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
                        openLesson(next.id)
                    },
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                    shape = RoundedCornerShape(13.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = hubGold)
                ) {
                    Text(if (canOpen(next.id)) "Derse Devam Et" else "Premium Dersi İncele",
                        color = Color(0xFF09283D), fontWeight = FontWeight.ExtraBold)
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
                            .clickable { openLesson(lesson.id) }
                            .heightIn(min = 54.dp).padding(11.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(lesson.title, color = Color.White,
                                fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            progress.lastSessionFocus(lesson.id)?.let { focus ->
                                Text(
                                    "Önerilen odak: " + focus.label,
                                    color = hubCyan, fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            if (progress.lastSessionFocus(lesson.id) == null) {
                                Text("Kısa pekiştirme çalışması",
                                    color = hubText, fontSize = 11.sp)
                            }
                        }
                        Spacer(Modifier.width(7.dp))
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
            Text("Konuları Keşfet",
                color = Color.White, fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold)
            Text(
                "Resimli konuları incele; görsel örneklerle matematiği adım adım öğren.",
                color = hubText, fontSize = 12.sp, lineHeight = 17.sp
            )
            Text("MATEMATİK ALANLARI", color = hubGold,
                fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    MathAreaFilterChip("Hepsi", selectedAreaName == null) {
                        selectedAreaName = null
                    }
                }
                items(TopicArea.values().filter { area ->
                    curriculum.topics.any { it.area == area }
                }, key = { it.name }) { area ->
                    val title = when (area) {
                        TopicArea.NUMBERS -> "Sayılar"
                        TopicArea.OPERATIONS -> "İşlemler"
                        TopicArea.GEOMETRY -> "Geometri"
                        TopicArea.DATA -> "Veri"
                        TopicArea.ALGEBRA -> "Cebir"
                        TopicArea.REVIEW -> "Tekrar"
                    }
                    MathAreaFilterChip(title, selectedAreaName == area.name) {
                        selectedAreaName = if (selectedAreaName == area.name)
                            null else area.name
                    }
                }
            }
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
                    listedTopics.flatMap { it.lessonIds }.distinct().size.toString() +
                    " kısa ders",
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
                    onClick = {
                        topicQuery = ""
                        showRemainingOnly = false
                        selectedAreaName = null
                    },
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
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    Spacer(Modifier.weight(1f))
                    Text(topics.size.toString() + " konu", color = hubCyan,
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                BoxWithConstraints(Modifier.fillMaxWidth()) {
                    // Two cards only when minimum readable width is available.
                    if (maxWidth >= 356.dp && !reducedColumnsForLargeText) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            topics.chunked(2).forEach { pair ->
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    pair.forEach { topic ->
                                        CurriculumTopicCard(
                                            topic = topic,
                                            done = topic.lessonIds.count { progress.completed(it) },
                                            count = topic.lessonIds.size,
                                            lockedCount = topic.lessonIds.count { !canOpen(it) },
                                            onClick = { selectedTopicId = topic.id },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(11.dp)) {
                            topics.forEach { topic ->
                                CurriculumTopicCard(
                                    topic = topic,
                                    done = topic.lessonIds.count { progress.completed(it) },
                                    count = topic.lessonIds.size,
                                    lockedCount = topic.lessonIds.count { !canOpen(it) },
                                    onClick = { selectedTopicId = topic.id }
                                )
                            }
                        }
                    }
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
private fun MathAreaFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val background = if (selected) Color(0xFF28798E) else Color(0xFF173D5B)
    Box(
        modifier = Modifier
            .background(background, RoundedCornerShape(14.dp))
            .border(1.dp, if (selected) hubCyan else Color(0xFF486F85),
                RoundedCornerShape(14.dp))
            .clickable(role = Role.Button, onClickLabel = "Alan filtresi: " + label,
                onClick = onClick)
            .semantics { this.selected = selected }
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) Color.White else hubText,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold)
    }
}

@Composable
private fun CurriculumTopicCard(
    topic: CurriculumTopic,
    done: Int,
    count: Int,
    lockedCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = when (topic.area) {
        TopicArea.NUMBERS -> Color(0xFF90DDF2)
        TopicArea.OPERATIONS -> Color(0xFFFFD18A)
        TopicArea.GEOMETRY -> Color(0xFFCCBCFF)
        TopicArea.DATA -> Color(0xFF8EE1BE)
        TopicArea.ALGEBRA -> Color(0xFFFFBADE)
        TopicArea.REVIEW -> Color(0xFFFFD49C)
    }
    val allLocked = count > 0 && lockedCount == count
    val status = when {
        count == 0 -> "Henüz ders yok"
        allLocked -> "Premium dersler"
        lockedCount > 0 ->
            (count - lockedCount).toString() + " açık · " + lockedCount + " Premium"
        done == count -> "Kısa dersler tamam"
        else -> "Dersler keşfe hazır"
    }
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(listOf(Color(0xFF235B77), Color(0xFF123953))),
                RoundedCornerShape(20.dp))
            .border(1.dp, accent.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            .clickable(role = Role.Button,
                onClickLabel = "Konuyu incele: " + topic.title, onClick = onClick)
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(Modifier.fillMaxWidth()) {
            LearningTopicCover(topic.area, Modifier.fillMaxWidth().height(110.dp))
            Row(
                modifier = Modifier.align(Alignment.TopEnd)
                    .padding(8.dp)
                    .background(Color(0xDF12364E), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (allLocked) Icons.Filled.Lock
                    else if (count > 0 && done == count) Icons.Filled.CheckCircle
                    else Icons.Filled.AutoStories,
                    contentDescription = null, tint = accent,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(if (allLocked) "PREMIUM" else done.toString() + "/" + count.toString(),
                    color = Color.White, fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold)
            }
        }
        Text(
            topic.title, color = Color.White, fontSize = 15.sp,
            lineHeight = 18.sp, fontWeight = FontWeight.ExtraBold,
            maxLines = 2, overflow = TextOverflow.Ellipsis
        )
        Text(
            topic.summary, color = hubText, fontSize = 11.sp, lineHeight = 15.sp,
            maxLines = 3, minLines = 2, overflow = TextOverflow.Ellipsis
        )
        LinearProgressIndicator(
            progress = { if (count == 0) 0f else done.toFloat() / count },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = accent, trackColor = Color(0xFF315D76)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(status, color = accent, fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold, lineHeight = 14.sp,
                maxLines = 2, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, contentDescription = null,
                tint = accent, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun CurriculumTopicDetail(
    topic: CurriculumTopic,
    userClass: String,
    lessonComplete: (String) -> Boolean,
    lessonAccessible: (String) -> Boolean,
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
        LearningTopicCover(topic.area,
            modifier = Modifier.fillMaxWidth().height(155.dp))
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
        LearningConceptExplorer(topic)
        if (topic.playable) {
            Text("Kullanılabilir Kısa Dersler", color = Color.White,
                fontSize = 17.sp, fontWeight = FontWeight.Bold)
            topic.lessonIds.forEach { id ->
                val lesson = lessons.firstOrNull { it.id == id }
                if (lesson != null) {
                    val accessible = lessonAccessible(id)
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(Color(0xFF183F5D), RoundedCornerShape(15.dp))
                            .border(1.dp, Color(0xFF4E90A4), RoundedCornerShape(15.dp))
                            .clickable { onStart(id) }
                            .padding(15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(if (!accessible) Icons.Filled.Lock
                             else if (lessonComplete(id)) Icons.Filled.CheckCircle
                             else Icons.Filled.PlayArrow,
                             contentDescription = null,
                             tint = if (accessible) hubCyan else hubGold)
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f)) {
                            Text(lesson.title, color = Color.White,
                                fontWeight = FontWeight.Bold)
                            Text(
                                if (!accessible) "Premium · Yetişkin seçeneklerini inceleyebilir"
                                 else if (lessonComplete(id)) "Tekrar çalış"
                                 else "Ders açık · Resimli anlatım · Kavrama kontrolü",
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
