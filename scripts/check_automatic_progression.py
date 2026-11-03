#!/usr/bin/env python3
"""Guard automatic post-success progression across shipped game worlds."""
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
G = ROOT / "app/src/main/java/com/yigit033/matematikmacarasi/games"
helper = (G / "common/AutoAdvanceOnSuccess.kt").read_text(encoding="utf-8")
for token in ("LaunchedEffect(sessionKey, stepKey, solved)", "rememberUpdatedState(onAdvance)",
              "delay(delayMillis.coerceAtLeast(0L))", "if (solved)"):
    assert token in helper, f"auto-advance helper: {token}"

checks = {
    "geometry/GeometryCastleScreen.kt": ("run.room < 4 && run.solved", "if(!feedbackPositive) CastleAction"),
    "fractions/FractionCrystalsScreen.kt": ("run.solved && !run.finished", "if(!positive) Button"),
    "pattern/PatternForestScreen.kt": ("run.solved && !run.finished", "if(!positive) ForestButton"),
    "time/TimeTowerScreen.kt": ("run.solved && !run.finished", "if(!success) GoldAction"),
    "interactive/BalloonAdventureScreen.kt": ("run?.resolved == true", "if(feedback.isNotBlank()) PrimaryButton"),
    "sayiavi2/StageAScreen.kt": ("showSuccessDialog, delayMillis=1150L", "onStageACompleted()"),
    "sayiavi2/StageBScreen.kt": ("showSuccessDialog, delayMillis=1150L", "onStageBCompleted()"),
    "sayiavi2/StageCScreen.kt": ("isRouteComplete, delayMillis=1000L", "onStageCCompleted()"),
    "sayiavi2/SayiAviPremiumStages.kt": ("AutoAdvanceOnSuccess(data, \"shells\", isCorrect)", "AutoAdvanceOnSuccess(challenges, questionIndex, isCorrect)"),
    "sayiavi2/AdventureReviewScreen.kt": ("AutoAdvanceOnSuccess(seed, index, solved)", "if(advanceFailed) ReviewButton"),
    "sayiavi2/ForestAdventureScreen.kt": ("run.rootRound == 3) { finishForest() }", "if(store.finish(run))"),
    "sayiavi2/TempleAdventureScreen.kt": ("selected == quest.mirror.resultAfterDivision", "if(message.startsWith(\"İlerleme kaydedilemedi\"))"),
}
for path, required in checks.items():
    body = (G / path).read_text(encoding="utf-8")
    assert "AutoAdvanceOnSuccess(" in body, f"missing transition: {path}"
    for token in required:
        assert token in body, f"{path}: {token}"

# These games were already auto-advancing; make sure they keep their native paths.
already_auto = {
    "interactive/MarketAdventureEngine.kt": "return if(correct) nextVisit(checked) else checked",
    "interactive/SpaceExpeditionScreen.kt": "sonraki soru otomatik",
    "sayiavi2/ForestAdventureScreen.kt": "stage = ForestStage.LEAVES",
    "basic/AdditionGameActivity.kt": "postDelayed({",
}
for path, token in already_auto.items():
    assert token in (G / path).read_text(encoding="utf-8"), path
print("Game auto-progression, save recovery and existing automatic routes: PASS")
