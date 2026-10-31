package com.yigit033.matematikmacarasi.ui.screens.home

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.yigit033.matematikmacarasi.DailyTask
import com.yigit033.matematikmacarasi.DailyTasksDialog
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.ComingSoonActivity
import com.yigit033.matematikmacarasi.games.basic.AdditionGameActivity
import com.yigit033.matematikmacarasi.games.basic.DivisionGameActivity
import com.yigit033.matematikmacarasi.games.basic.MultiplicationGameActivity
import com.yigit033.matematikmacarasi.games.basic.SubtractionGameActivity
import com.yigit033.matematikmacarasi.games.interactive.BalloonPopGameActivity
import com.yigit033.matematikmacarasi.games.interactive.MarketSimulatorActivity
import com.yigit033.matematikmacarasi.games.interactive.MathMazeGameActivity
import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAvi2Activity
import com.yigit033.matematikmacarasi.games.interactive.SpaceMathAdventureActivity
import com.yigit033.matematikmacarasi.profile.ProfileActivity
import com.yigit033.matematikmacarasi.services.AvatarService
import com.yigit033.matematikmacarasi.services.DailyTaskManager
import com.yigit033.matematikmacarasi.services.EventBus
import com.yigit033.matematikmacarasi.services.GameStatisticsService
import com.yigit033.matematikmacarasi.services.PlayerProfileService
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.services.VersionCheckService
import com.yigit033.matematikmacarasi.ui.dialogs.UpdateDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Matematik Macerası 2.0 — Onaylı Referansa Sadık Ana Sayfa (Sprint H2)
 *
 * Referans: reference/HOME_APPROVED_REFERENCE.png
 *
 * Görsel Mimarisi:
 * 1. Üst Hero: Mavi gökyüzü + fantastik adalar + kaşif illüstrasyonu (hero_fantasy_island_explorer.png)
 * 2. Profil Çubuğu: Hero üzerinde yüzen, altın halkalı avatar, gerçek kullanıcı verileri ve yıldız kapsülü
 * 3. Parşömen Görev Kartı: Sağ tarafta kaşifin işaret ettiği krem parşömen + büyük canlı yeşil CTA butonu
 * 4. Hızlı Erişim: Dört işlem, Balon, Sayı Avı ve Uzay macerası için HD oyun illüstrasyonlu renkli kartlar
 * 5. Senin İçin Önerilen: Büyük açık zeminli kart içinde gerçek oyun ve dürüst öğrenme önerileri
 * 6. Gelişimim: Açık renkli kart içinde 4 pastel renkli gerçek istatistik kutusu
 * 7. Sabit Alt Navigasyon: Koyu mavi bar tabanı ve parlak mavi seçili sekme butonu
 */
@Composable
fun HomeScreenNew(
    lastResumeTime: Long = 0L,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val TAG = "HomeScreenNew"

    val screenWidth = configuration.screenWidthDp

    // 1. Sekme Durumu
    var currentTab by remember { mutableStateOf(HomeTab.HOME) }

    // 2. Servis Referansları (Singleton & Güvenli)
    val avatarService = remember { AvatarService.getInstance(context) }
    val dailyTaskManager = remember { DailyTaskManager.getInstance(context) }
    val gameStatisticsService = remember { GameStatisticsService.getInstance(context) }
    val playerProfileService = remember { PlayerProfileService.getInstance(context) }
    val starManager = remember { StarManager.getInstance(context) }
    val versionCheckService = remember { VersionCheckService.getInstance(context) }

    // 3. UI State'leri
    var userName by remember { mutableStateOf(playerProfileService.getUserName().ifBlank { "Matematik Gezgini" }) }
    var userClass by remember { mutableStateOf(playerProfileService.getUserClass().ifBlank { "1. Sınıf" }) }
    var playerAge by remember { mutableIntStateOf(playerProfileService.getPlayerAge()) }
    var currentAvatar by remember { mutableStateOf(avatarService.getCurrentAvatar()) }
    var dailyTasks by remember { mutableStateOf(dailyTaskManager.getTodayTasks()) }
    var totalScore by remember { mutableIntStateOf(gameStatisticsService.getTotalScore()) }
    var completedGamesCount by remember { mutableIntStateOf(gameStatisticsService.getCompletedGamesCount()) }

    // Canlı Yıldız Bakiyesi - EventBus akışından toplanır
    val userStars by EventBus.currentStars.collectAsState()

    // Dialog State'leri
    var showDailyTasksDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isForceUpdate by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf("") }
    var updateUrl by remember { mutableStateOf("") }

    // Geri tuşu kontrolü: Ana Sayfa dışındaki bir sekmedeyken Ana Sayfa'ya döner
    BackHandler(enabled = currentTab != HomeTab.HOME) {
        currentTab = HomeTab.HOME
    }

    // 4. Güncelleme ve Resume Tetikleyicileri
    LaunchedEffect(lastResumeTime) {
        try {
            val freshTasks = dailyTaskManager.getTodayTasks()
            dailyTasks = freshTasks
            EventBus.updateDailyTasks(freshTasks)

            userName = playerProfileService.getUserName().ifBlank { "Matematik Gezgini" }
            userClass = playerProfileService.getUserClass().ifBlank { "1. Sınıf" }
            playerAge = playerProfileService.getPlayerAge()
            totalScore = gameStatisticsService.getTotalScore()
            completedGamesCount = gameStatisticsService.getCompletedGamesCount()

            // Yıldız senkronizasyonunu EventBus üzerinden garanti et
            val currentDisplayStars = starManager.getDisplayStars()
            if (userStars != currentDisplayStars) {
                EventBus.updateStars(currentDisplayStars)
            }
        } catch (e: Exception) {
            Log.e(TAG, "onResume veri yenileme hatası: ${e.message}", e)
        }
    }

    // Avatar değişikliklerini dinle
    LaunchedEffect(Unit) {
        EventBus.avatarChangedEvents.collect { event ->
            try {
                currentAvatar = avatarService.getAvatarById(event.avatarId)
            } catch (e: Exception) {
                Log.e(TAG, "Avatar güncelleme hatası: ${e.message}", e)
            }
        }
    }

    // Günlük görev güncellemelerini dinle
    LaunchedEffect(Unit) {
        EventBus.dailyTasks.collect { updatedTasks ->
            dailyTasks = updatedTasks
        }
    }

    // Profil veri değişikliklerini dinle
    LaunchedEffect(Unit) {
        EventBus.profileUpdateTime.collect { updateTime ->
            if (updateTime > 0) {
                userName = playerProfileService.getUserName().ifBlank { "Matematik Gezgini" }
                userClass = playerProfileService.getUserClass().ifBlank { "1. Sınıf" }
                playerAge = playerProfileService.getPlayerAge()
                totalScore = gameStatisticsService.getTotalScore()
                completedGamesCount = gameStatisticsService.getCompletedGamesCount()
            }
        }
    }

    // Sürüm kontrolü (arka planda bir kere)
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            when (val result = versionCheckService.checkForUpdate()) {
                is VersionCheckService.UpdateStatus.ForceUpdateRequired -> {
                    withContext(Dispatchers.Main) {
                        isForceUpdate = true
                        updateMessage = result.versionInfo.force_update_message
                        updateUrl = result.versionInfo.update_url
                        showUpdateDialog = true
                    }
                }
                is VersionCheckService.UpdateStatus.UpdateAvailable -> {
                    withContext(Dispatchers.Main) {
                        isForceUpdate = false
                        updateMessage = result.versionInfo.update_message
                        updateUrl = result.versionInfo.update_url
                        showUpdateDialog = true
                    }
                }
                else -> {}
            }
        }
    }

    // Ana Görev (Hero Card için ilk tamamlanmamış görev)
    val heroTask = remember(dailyTasks) {
        dailyTasks.firstOrNull { !it.isCompleted } ?: dailyTasks.firstOrNull()
    }
    val uncompletedCount = remember(dailyTasks) {
        dailyTasks.count { !it.isCompleted }
    }
    val completedCount = remember(dailyTasks) {
        dailyTasks.count { it.isCompleted }
    }

    // Profil ekranına yönlendirme fonksiyonu
    val navigateToProfile: () -> Unit = {
        val intent = Intent(context, ProfileActivity::class.java)
        (context as? Activity)?.startActivityForResult(intent, 1001) ?: context.startActivity(intent)
    }

    // Görev tıklama aksiyonu
    val launchTaskGame: (DailyTask) -> Unit = { task ->
        val intent = getGameIntentForType(context, task.gameType)
        intent.putExtra("daily_task_id", task.id)
        context.startActivity(intent)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(HomeColors.MainWorldGradient)
    ) {
        // İçerik Alanı
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            when (currentTab) {
                HomeTab.HOME -> {
                    val scrollState = rememberScrollState()

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                    ) {
                        // 1. ÜST HERO ALANI (Referanstaki Ada + Kaşif + Parşömen Görev Düzeni)
                        val heroHeight = (screenWidth * 1.25f).coerceIn(440f, 520f).dp

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(heroHeight)
                        ) {
                            // Arka Plan İllüstrasyonu (Ada, Gökyüzü, Kaleler ve Sol Taraftaki Kaşif)
                            Image(
                                painter = painterResource(id = R.drawable.hero_fantasy_island_explorer),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Üst Karartma Katmanı (Profil başlığının okunabilirliği için)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .background(HomeColors.HeroScrimGradient)
                            )

                            // Üst Profil Başlık Çubuğu
                            ProfileHeader(
                                userName = userName,
                                userClass = userClass,
                                currentAvatar = currentAvatar,
                                userStars = userStars,
                                uncompletedTaskCount = uncompletedCount,
                                onProfileClick = navigateToProfile,
                                onDailyTasksClick = { showDailyTasksDialog = true },
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .statusBarsPadding()
                            )

                            // Sağ Tarafta Parşömen Görev Kartı (Kaşifin işaret ettiği konum)
                            // Dar ekranlarda %65 genişlik, geniş ekranlarda maksimum 380dp
                            DailyQuestHeroCard(
                                heroTask = heroTask,
                                hasUncompletedTasks = uncompletedCount > 0,
                                onLaunchTask = launchTaskGame,
                                onViewAllTasks = { showDailyTasksDialog = true },
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .fillMaxWidth(0.66f)
                                    .widthIn(max = 380.dp)
                                    .padding(end = 12.dp, bottom = 16.dp)
                            )
                        }

                        // 2. HIZLI ERİŞİM (Referanstaki 4 Canlı İllüstrasyonlu Kart)
                        QuickAccessGamesSection(
                            onSeeAllClick = { currentTab = HomeTab.GAMES }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // 3. SENİN İÇİN ÖNERİLEN (Referanstaki Açık Zeminli İki Büyük Kart)
                        RecommendedSection(
                            playerAge = playerAge,
                            userClass = userClass,
                            onNavigateToLearn = { currentTab = HomeTab.LEARN }
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // 4. GELİŞİMİM (Referanstaki 4 Pastel İstatistik Kartı)
                        ProgressOverviewCard(
                            totalScore = totalScore,
                            completedGamesCount = completedGamesCount,
                            userStars = userStars,
                            dailyTasksCompleted = completedCount,
                            dailyTasksTotal = dailyTasks.size,
                            onViewDetailsClick = navigateToProfile
                        )

                        // Alt navigasyon çubuğu boşluğu
                        Spacer(modifier = Modifier.height(96.dp))
                    }
                }

                HomeTab.GAMES -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .statusBarsPadding()
                    ) {
                        GamesTabScreen(
                            modifier = Modifier.padding(bottom = 84.dp)
                        )
                    }
                }

                HomeTab.LEARN -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .statusBarsPadding()
                    ) {
                        LearnTabScreen(
                            onNavigateToGames = { currentTab = HomeTab.GAMES },
                            modifier = Modifier.padding(bottom = 84.dp)
                        )
                    }
                }

                HomeTab.PROFILE -> {
                    LaunchedEffect(Unit) {
                        navigateToProfile()
                        currentTab = HomeTab.HOME
                    }
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }

        // Sabit Alt Navigasyon Çubuğu
        HomeBottomNavigationBar(
            selectedTab = currentTab,
            onTabSelected = { selected ->
                if (selected == HomeTab.PROFILE) {
                    navigateToProfile()
                } else {
                    currentTab = selected
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Günlük Görevler Modalı
        if (showDailyTasksDialog) {
            DailyTasksDialog(
                onDismiss = { showDailyTasksDialog = false },
                onTaskClick = { task ->
                    showDailyTasksDialog = false
                    val intent = getGameIntentForType(context, task.gameType)
                    intent.putExtra("daily_task_id", task.id)
                    context.startActivity(intent)
                },
                dailyTasks = dailyTasks
            )
        }

        // Güncelleme Dialogu
        if (showUpdateDialog) {
            UpdateDialog(
                isForceUpdate = isForceUpdate,
                message = updateMessage,
                onUpdateClick = {
                    versionCheckService.openPlayStore(updateUrl)
                    if (!isForceUpdate) {
                        showUpdateDialog = false
                    }
                },
                onDismiss = {
                    if (!isForceUpdate) {
                        showUpdateDialog = false
                    }
                }
            )
        }
    }
}

/**
 * GameType enum türünü gerçek oyun Activity Intent'ine dönüştürür.
 */
private fun getGameIntentForType(context: Context, gameType: GameType): Intent {
    return when (gameType) {
        GameType.ADDITION_GAME -> Intent(context, AdditionGameActivity::class.java)
        GameType.SUBTRACTION_GAME -> Intent(context, SubtractionGameActivity::class.java)
        GameType.MULTIPLICATION_GAME -> Intent(context, MultiplicationGameActivity::class.java)
        GameType.DIVISION_GAME -> Intent(context, DivisionGameActivity::class.java)
        GameType.BALLOON_POP_GAME -> Intent(context, BalloonPopGameActivity::class.java)
        GameType.NUMBER_HUNT_GAME -> Intent(context, SayiAvi2Activity::class.java)
        GameType.MATH_MAZE_GAME -> Intent(context, MathMazeGameActivity::class.java)
        GameType.SPACE_MATH_ADVENTURE -> Intent(context, SpaceMathAdventureActivity::class.java)
        GameType.SHAPE_RECOGNITION -> Intent(context, ComingSoonActivity::class.java).apply {
            putExtra("game_name", "Şekiller & Geometri")
            putExtra("is_category", true)
        }
        GameType.MARKET_SIMULATOR -> Intent(context, MarketSimulatorActivity::class.java)
        else -> Intent(context, AdditionGameActivity::class.java)
    }
}

// Günlük görev tıklamaları artık getGameIntentForType() ile yönlendirilir.
