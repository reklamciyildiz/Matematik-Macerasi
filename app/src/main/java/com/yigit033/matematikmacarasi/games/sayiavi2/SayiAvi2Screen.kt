package com.yigit033.matematikmacarasi.games.sayiavi2

import android.content.Context
import android.media.MediaPlayer
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.yigit033.matematikmacarasi.R
import kotlinx.coroutines.delay

/**
 * Sayı Avı 2.0 Oyun Aşamaları State Machine
 */
enum class SayiAviMissionStage {
    WELCOME,
    BRIEFING,
    STAGE_A_GRID,
    STAGE_B_VAULT,
    STAGE_C_ROUTE,
    REWARD_SUMMARY
}

/**
 * Sayı Avı 2.0 Ana Koordinatör Composable.
 * State Machine'i yönetir, sesleri çalar, yerel veri saklamayı ve geri tuşunu idare eder.
 */
@Composable
fun SayiAvi2Screen(
    currentStars: Int,
    onRewardEarned: (Boolean, SayiAviLearningSummary) -> Unit,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val storage = remember { SayiAviStorage.getInstance(context) }

    // Oyun Aşaması
    var currentStage by remember {
        val lastStage = storage.getLastStage()
        val initialStage = when (lastStage) {
            "STAGE_A" -> SayiAviMissionStage.STAGE_A_GRID
            "STAGE_B" -> SayiAviMissionStage.STAGE_B_VAULT
            "STAGE_C" -> SayiAviMissionStage.STAGE_C_ROUTE
            else -> SayiAviMissionStage.WELCOME
        }
        mutableStateOf(initialStage)
    }

    var showExitDialog by remember { mutableStateOf(false) }

    // Oyun Verileri
    var runSeed by remember { mutableLongStateOf(storage.getRunSeed()) }
    val stageAGridData = remember(runSeed) { SayiAviEngine.generateStageAGrid(if (runSeed == 0L) null else runSeed + 101) }
    val stageBVaultData = remember(runSeed) { SayiAviEngine.generateStageBVault(if (runSeed == 0L) null else runSeed + 202) }
    val stageCRouteData = remember(runSeed) { SayiAviEngine.generateStageCRoute(if (runSeed == 0L) null else runSeed + 303) }

    // Süre Sayacı
    var missionStartTime by remember { mutableLongStateOf(storage.getRunStart()) }
    var totalTimeSeconds by remember { mutableLongStateOf(0L) }
    var isFirstCompletionResult by remember { mutableStateOf(false) }
    var counters by remember { mutableStateOf(storage.getRunCounters()) }
    var lastSummary by remember { mutableStateOf<SayiAviLearningSummary?>(null) }

    fun attempt(stage: Int, correct: Boolean) {
        counters = counters.attempt(stage, correct)
        storage.saveRunCounters(counters)
    }
    fun hint() {
        counters = counters.hint()
        storage.saveRunCounters(counters)
    }
    fun newRun() {
        val now = System.currentTimeMillis()
        storage.beginRun(now, now)
        runSeed = now
        missionStartTime = now
        counters = SayiAviRunCounters()
        lastSummary = null
    }

    // Ses Çalarlar (Güvenli Başlatma)
    val mediaPlayerCorrect = remember {
        try { MediaPlayer.create(context, R.raw.correct_sound) } catch (e: Exception) { null }
    }
    val mediaPlayerWrong = remember {
        try { MediaPlayer.create(context, R.raw.wrong_sound) } catch (e: Exception) { null }
    }
    val mediaPlayerSuccess = remember {
        try { MediaPlayer.create(context, R.raw.success) } catch (e: Exception) { null }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                mediaPlayerCorrect?.release()
                mediaPlayerWrong?.release()
                mediaPlayerSuccess?.release()
            } catch (e: Exception) {
                // Sessiz devam
            }
        }
    }

    fun playCorrectSound() {
        try {
            mediaPlayerCorrect?.seekTo(0)
            mediaPlayerCorrect?.start()
        } catch (e: Exception) {}
    }

    fun playWrongSound() {
        try {
            mediaPlayerWrong?.seekTo(0)
            mediaPlayerWrong?.start()
        } catch (e: Exception) {}
    }

    fun playSuccessSound() {
        try {
            mediaPlayerSuccess?.seekTo(0)
            mediaPlayerSuccess?.start()
        } catch (e: Exception) {}
    }

    // Android Donanım/Yazılım Geri Tuşu İşleyicisi
    BackHandler {
        if (currentStage == SayiAviMissionStage.WELCOME || currentStage == SayiAviMissionStage.REWARD_SUMMARY) {
            onExitGame()
        } else {
            showExitDialog = true
        }
    }

    // Çıkış Onay Diyaloğu
    if (showExitDialog) {
        ExitConfirmationDialog(
            onConfirmExit = {
                showExitDialog = false
                onExitGame()
            },
            onDismiss = { showExitDialog = false }
        )
    }

    when (currentStage) {
        SayiAviMissionStage.WELCOME -> {
            SayiAviWelcomeScreen(
                isCompletedBefore = storage.isMissionCompleted(),
                totalCompletions = storage.getTotalCompletions(),
                starsCount = currentStars,
                onStartRegion1 = {
                    newRun()
                    currentStage = SayiAviMissionStage.BRIEFING
                },
                onBackClick = onExitGame,
                modifier = modifier
            )
        }

        SayiAviMissionStage.BRIEFING -> {
            SayiAviBriefingScreen(
                starsCount = currentStars,
                onStartStageA = {
                    currentStage = SayiAviMissionStage.STAGE_A_GRID
                    storage.saveLastStage("STAGE_A")
                },
                onBackClick = { currentStage = SayiAviMissionStage.WELCOME },
                modifier = modifier
            )
        }

        SayiAviMissionStage.STAGE_A_GRID -> {
            StageAScreen(
                gridData = stageAGridData,
                starsCount = currentStars,
                initialAttempts = counters.aAttempts,
                onAttemptRecorded = { correct -> attempt(1, correct) },
                onHintShown = { hint() },
                onStageACompleted = {
                    playSuccessSound()
                    storage.setMapPiece1(true)
                    storage.saveLastStage("STAGE_B")
                    currentStage = SayiAviMissionStage.STAGE_B_VAULT
                },
                onBackClick = { showExitDialog = true },
                modifier = modifier
            )
        }

        SayiAviMissionStage.STAGE_B_VAULT -> {
            StageBScreen(
                vaultData = stageBVaultData,
                starsCount = currentStars,
                onAttemptRecorded = { correct -> attempt(2, correct) },
                onHintShown = { hint() },
                onStageBCompleted = {
                    playSuccessSound()
                    storage.setMapPiece2(true)
                    storage.saveLastStage("STAGE_C")
                    currentStage = SayiAviMissionStage.STAGE_C_ROUTE
                },
                onBackClick = { showExitDialog = true },
                modifier = modifier
            )
        }

        SayiAviMissionStage.STAGE_C_ROUTE -> {
            StageCScreen(
                routeData = stageCRouteData,
                starsCount = currentStars,
                initialStepIndex = storage.getRouteStep(),
                onRouteProgress = { storage.saveRouteStep(it) },
                onAttemptRecorded = { correct -> attempt(3, correct) },
                onHintShown = { hint() },
                onStageCCompleted = {
                    playSuccessSound()
                    totalTimeSeconds = ((System.currentTimeMillis() - missionStartTime) / 1000).coerceAtLeast(1)
                    val summary = SayiAviLearningSummary(counters, totalTimeSeconds)
                    lastSummary = summary
                    val isFirstTime = storage.recordMissionCompletion(totalTimeSeconds, summary)
                    isFirstCompletionResult = isFirstTime
                    onRewardEarned(isFirstTime, summary)

                    currentStage = SayiAviMissionStage.REWARD_SUMMARY
                },
                onBackClick = { showExitDialog = true },
                modifier = modifier
            )
        }

        SayiAviMissionStage.REWARD_SUMMARY -> {
            SayiAviRewardScreen(
                isFirstCompletion = isFirstCompletionResult,
                totalCompletions = storage.getTotalCompletions(),
                learningSummary = lastSummary ?: storage.getLastSummary(),
                timeSpentSeconds = totalTimeSeconds,
                starsCount = currentStars,
                onReplayClick = {
                    newRun()
                    currentStage = SayiAviMissionStage.STAGE_A_GRID
                },
                onReturnToGamesClick = onExitGame,
                modifier = modifier
            )
        }
    }
}

