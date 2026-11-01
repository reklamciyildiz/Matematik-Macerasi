package com.yigit033.matematikmacarasi.games.sayiavi2

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveLearningStore
import com.yigit033.matematikmacarasi.learning.adaptive.AdventureSkill
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

/**
 * Sayı Avı 2.0 Oyun Aşamaları State Machine
 */
enum class SayiAviMissionStage {
    WELCOME,
    BRIEFING,
    STAGE_GROUP_SHELLS,
    STAGE_A_GRID,
    STAGE_B_VAULT,
    STAGE_C_ROUTE,
    STAGE_TRANSFER_SEAL,
    REWARD_SUMMARY,
    TEMPLE,
    REINFORCEMENT
}

/**
 * Sayı Avı 2.0 Ana Koordinatör Composable.
 * State Machine'i yönetir, sesleri çalar, yerel veri saklamayı ve geri tuşunu idare eder.
 */
@Composable
fun SayiAvi2Screen(
    currentStars: Int,
    onRewardEarned: (Boolean, SayiAviLearningSummary) -> Unit,
    onForestReturn: () -> Unit,
    onExitGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val storage = remember { SayiAviStorage.getInstance(context) }
    val adaptiveStore = remember { AdaptiveLearningStore(context) }
    var skillProgress by remember { mutableStateOf(adaptiveStore.progress(AdventureSkill.THREE_COUNTING)) }
    val forestStorage = remember { ForestProgressStore(context) }
    var forestRevision by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val forestCompleted = remember(forestRevision) { forestStorage.hasCompleted() }
    val forestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        forestRevision++
        onForestReturn()
    }

    // Oyun Aşaması
    var currentStage by remember {
        val lastStage = storage.getLastStage()
        val initialStage = when (lastStage) {
            "STAGE_GROUP" -> SayiAviMissionStage.STAGE_GROUP_SHELLS
            "STAGE_A" -> SayiAviMissionStage.STAGE_A_GRID
            "STAGE_B" -> SayiAviMissionStage.STAGE_B_VAULT
            "STAGE_C" -> SayiAviMissionStage.STAGE_C_ROUTE
            "STAGE_SEAL" -> SayiAviMissionStage.STAGE_TRANSFER_SEAL
            else -> SayiAviMissionStage.WELCOME
        }
        mutableStateOf(initialStage)
    }

    var showExitDialog by remember { mutableStateOf(false) }
    var templeTier by remember { mutableStateOf(LearningTier.GELISEN) }

    // Oyun Verileri
    var runSeed by remember { mutableLongStateOf(storage.getRunSeed()) }
    var runTier by remember { mutableStateOf(storage.getRunTier()) }
    val shellGrouping = remember(runSeed, runTier) {
        SayiAviEngine.generateShellGrouping(runSeed + 17L, runTier)
    }
    val transferChallenges = remember(runSeed, runTier) {
        SayiAviEngine.generateTransferChallenges(runSeed + 55L, runTier)
    }
    val stageAGridData = remember(runSeed, runTier) {
        SayiAviEngine.generateStageAGrid(if (runSeed == 0L) 0L else runSeed + 101, runTier)
    }
    val stageBVaultData = remember(runSeed, runTier) {
        SayiAviEngine.generateStageBVault(if (runSeed == 0L) 0L else runSeed + 202, runTier)
    }
    val stageCRouteData = remember(runSeed, runTier) {
        SayiAviEngine.generateStageCRoute(if (runSeed == 0L) 0L else runSeed + 303, runTier)
    }

    // Süre Sayacı
    var missionStartTime by remember { mutableLongStateOf(storage.getRunStart()) }
    var totalTimeSeconds by remember { mutableLongStateOf(0L) }
    var isFirstCompletionResult by remember { mutableStateOf(false) }
    var counters by remember { mutableStateOf(storage.getRunCounters()) }
    var lastSummary by remember { mutableStateOf<SayiAviLearningSummary?>(null) }
    var completionHandled by remember(runSeed) { mutableStateOf(false) }

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
        val selected = adaptiveStore.progress(AdventureSkill.THREE_COUNTING).currentTier
        storage.beginRun(now, now, selected)
        runTier = selected
        runSeed = now
        missionStartTime = now
        counters = SayiAviRunCounters()
        lastSummary = null
        completionHandled = false
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
        SayiAviMissionStage.REINFORCEMENT -> AdventureReviewScreen(
            kind = AdventureReviewKind.COUNT_THREE,
            tier = runTier,
            onExit = { currentStage = SayiAviMissionStage.REWARD_SUMMARY }
        )
        SayiAviMissionStage.TEMPLE -> TempleAdventureScreen(
            tier = templeTier, onExit = { currentStage = SayiAviMissionStage.WELCOME })
        SayiAviMissionStage.WELCOME -> {
            SayiAviWelcomeScreen(
                isCompletedBefore = storage.isMissionCompleted(),
                totalCompletions = storage.getTotalCompletions(),
                forestCompleted = forestCompleted,
                starsCount = currentStars,
                adaptiveProgress = skillProgress,
                gradeLabel = adaptiveStore.gradeLabel(),
                onSelectTier = { chosen ->
                    if (adaptiveStore.selectTier(AdventureSkill.THREE_COUNTING, chosen)) {
                        skillProgress = adaptiveStore.progress(AdventureSkill.THREE_COUNTING)
                    }
                },
                onStartTemple = {
                    templeTier = adaptiveStore.progress(AdventureSkill.THREE_COUNTING).currentTier
                    currentStage = SayiAviMissionStage.TEMPLE
                },
                templeCompleted = context.getSharedPreferences("temple_adventure_v1", Context.MODE_PRIVATE).getInt("runs", 0) > 0,
                onStartRegion2 = {
                    forestLauncher.launch(Intent(context, ForestAdventureActivity::class.java))
                },
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
                    currentStage = SayiAviMissionStage.STAGE_GROUP_SHELLS
                    storage.saveLastStage("STAGE_GROUP")
                },
                onBackClick = { currentStage = SayiAviMissionStage.WELCOME },
                modifier = modifier
            )
        }

        SayiAviMissionStage.STAGE_GROUP_SHELLS -> {
            ShellGroupingScreen(
                data = shellGrouping,
                starsCount = currentStars,
                onAttemptRecorded = { correct ->
                    attempt(4, correct)
                    if (correct) playCorrectSound() else playWrongSound()
                },
                onHintShown = { hint() },
                initialFinishedGroups = storage.getFinishedGroups(),
                initialPartialShells = storage.getPartialShells(),
                onGroupingProgress = { done, partial -> storage.saveGroupingProgress(done, partial) },
                onCompleted = {
                    playCorrectSound()
                    storage.saveLastStage("STAGE_A")
                    currentStage = SayiAviMissionStage.STAGE_A_GRID
                },
                onBackClick = { showExitDialog = true },
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
                    storage.saveLastStage("STAGE_SEAL")
                    currentStage = SayiAviMissionStage.STAGE_TRANSFER_SEAL
                },
                onBackClick = { showExitDialog = true },
                modifier = modifier
            )
        }

        SayiAviMissionStage.STAGE_TRANSFER_SEAL -> {
            TreasureSealScreen(
                challenges = transferChallenges,
                starsCount = currentStars,
                onAttemptRecorded = { correct ->
                    attempt(5, correct)
                    if (correct) playCorrectSound() else playWrongSound()
                },
                onHintShown = { hint() },
                initialQuestionIndex = storage.getSealQuestionIndex(),
                onQuestionProgress = { storage.saveSealQuestionIndex(it) },
                onCompleted = {
                    if (!completionHandled) {
                        completionHandled = true
                        playSuccessSound()
                        totalTimeSeconds = ((System.currentTimeMillis() - missionStartTime) / 1000).coerceAtLeast(1)
                        val summary = SayiAviLearningSummary(counters, totalTimeSeconds)
                        lastSummary = summary
                        val firstCompletion = storage.recordMissionCompletion(totalTimeSeconds, summary)
                        isFirstCompletionResult = firstCompletion
                        currentStage = SayiAviMissionStage.REWARD_SUMMARY
                        if (storage.getLastStage() == "COMPLETED") {
                            adaptiveStore.recordCompletion(
                                AdventureSkill.THREE_COUNTING, runSeed, runTier,
                                summary.attempts, summary.errors, counters.hintsShown
                            )
                            skillProgress = adaptiveStore.progress(AdventureSkill.THREE_COUNTING)
                        }
                        onRewardEarned(firstCompletion, summary)
                    }
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
                onReviewClick = { currentStage = SayiAviMissionStage.REINFORCEMENT },
                onReplayClick = {
                    newRun()
                    currentStage = SayiAviMissionStage.STAGE_GROUP_SHELLS
                },
                onReturnToGamesClick = onExitGame,
                modifier = modifier
            )
        }
    }
}

