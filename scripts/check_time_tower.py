#!/usr/bin/env python3
"""Zaman Kulesi source gate: real scenes, deterministic engine, saves, manifest and game routes."""
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
src=ROOT/"app/src/main/java/com/yigit033/matematikmacarasi"
engine=(src/"games/time/TimeTowerEngine.kt").read_text()
store=(src/"games/time/TimeTowerStore.kt").read_text()
screen=(src/"games/time/TimeTowerScreen.kt").read_text()
visuals=(src/"games/time/TimeTowerVisuals.kt").read_text()
activity=(src/"games/time/TimeTowerActivity.kt").read_text()
game_type=(src/"GameType.kt").read_text()
daily=(src/"services/DailyTaskManager.kt").read_text()
stats=(src/"services/GameStatisticsService.kt").read_text()
main=(src/"MainActivity.kt").read_text()
worlds=["CLOCK","READ","TRAIN","CALENDAR","ROUTE"]
for world in worlds:
    assert f"{world}(" in engine,world
    assert f"TimeWorld.{world}" in screen,world
for key in ("STOPS=25","STOPS_PER_WORLD=5","fun clockStep","fun answerCorrect",
            "fun weekdayMondayFirst","fun addDays","fun daysUntil","val correctOrder"):
    assert key in engine,key
for key in ("fun save(","fun advance(","fun start(","first_reward_recorded",
            "delivered_seed","_completed","editor.commit()","fun valid("):
    assert key in store,key
for key in ("ClockFace(","CalendarBoard(","TrainTimeline(","RouteBoard(",
            "GoldAction(","BackHandler","onCompleted(advanced)","rememberSaveable"):
    assert key in screen,key
for key in ("fingerToTime","awaitEachGesture","Paint.ANTI_ALIAS_FLAG",
            "CalendarBoard","RouteBoard","animateFloat"):
    assert key in visuals,key
assert "FRACTION_CRYSTALS" in game_type and "TIME_TOWER" in game_type
assert "class DailyTaskManager" in daily and "GameType.TIME_TOWER" in daily
assert "GameType.TIME_TOWER" in main and "GameType.TIME_TOWER" in stats
assert "TIME_TOWER" in activity and "markRewardRecorded" in activity
manifest=(ROOT/"app/src/main/AndroidManifest.xml").read_text()
assert 'android:name=".games.time.TimeTowerActivity"' in manifest
games=(src/"ui/screens/home/GamesTabScreen.kt").read_text()
quick=(src/"ui/screens/home/QuickAccessGamesSection.kt").read_text()
old=(src/"games/GameSelectionActivity.kt").read_text()
assert 'GameCatalogItem("time_tower"' in games
assert "13 macera" in games
assert 'id = "time_tower"' in quick
assert "TimeTowerActivity" in old
art=ROOT/"app/src/main/res/drawable/time_tower_hero.xml"
assert art.exists()
visual=art.read_text()
assert visual.count("<path")>=170
assert art.stat().st_size<80000
assert (ROOT/"app/src/test/java/com/yigit033/matematikmacarasi/games/time/TimeTowerEngineTest.kt").is_file()
assert (ROOT/"app/src/androidTest/java/com/yigit033/matematikmacarasi/games/time/TimeTowerPersistenceTest.kt").is_file()
print("Zaman Kulesi: 25 quests, five modes, visual assets, gameplay routing and persistence gate PASS")
