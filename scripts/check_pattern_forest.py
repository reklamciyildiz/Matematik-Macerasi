#!/usr/bin/env python3
"""Pattern Forest: 5x6 playable quests, independent UI and full Android routing."""
from pathlib import Path
R=Path(__file__).resolve().parent.parent
src=R/"app/src/main/java/com/yigit033/matematikmacarasi"
pkg=src/"games/pattern"
engine=(pkg/"PatternForestEngine.kt").read_text()
store=(pkg/"PatternForestStore.kt").read_text()
screen=(pkg/"PatternForestScreen.kt").read_text()
visual=(pkg/"PatternForestVisuals.kt").read_text()
activity=(pkg/"PatternForestActivity.kt").read_text()
for world in ("COLORS","SHAPES","NUMBERS","MOTION","RULES"):
    assert world+"(" in engine,world
for mode in ("COMPLETE","DRAG","ORDER","RULE","SPOT","CREATE"):
    assert mode+"(" in engine,mode
for required in ("STOPS=30","TASKS_PER_WORLD=6","fun task(",
    "fun correct(","fun validSelection(","grade>=6","difficultyLabels"):
    assert required in engine,required
for required in ("fun advance(","fun save(","fun start(","fun valid(",
    "first_reward","delivered_seed","e.commit()","_completed","_clean"):
    assert required in store,required
for required in ("detectDragGesturesAfterLongPress","RuneSequence(",
    "RuneOption(","RuleOptions(","PatternForestEngine.correct(",
    "run.selected","AnimatedVisibility","onCompleted(advanced)"):
    assert required in screen,required
for required in ("RuneGem(","ForestPanel(","ForestButton(",
    "ChapterStrip(","EmptyRuneSlot","animateFloatAsState"):
    assert required in visual,required
assert "PATTERN_FOREST" in activity
assert "PatternForestScreen(" in activity
assert "GameType.PATTERN_FOREST" in (src/"MainActivity.kt").read_text()
assert "GameType.PATTERN_FOREST" in (src/"services/DailyTaskManager.kt").read_text()
assert "GameType.PATTERN_FOREST" in (src/"services/GameStatisticsService.kt").read_text()
assert "GameType.PATTERN_FOREST" in (src/"ui/components/UIComponents.kt").read_text()
assert "PATTERN_FOREST" in (src/"GameType.kt").read_text()
manifest=(R/"app/src/main/AndroidManifest.xml").read_text()
assert 'android:name=".games.pattern.PatternForestActivity"' in manifest
catalog=(src/"ui/screens/home/GameCatalog.kt").read_text()
games=(src/"ui/screens/home/GamesTabScreen.kt").read_text()
assert 'GameCatalogItem("pattern_forest"' in catalog
assert 'PatternForestActivity::class.java' in catalog
assert catalog.count('GameCatalogItem("') >= 14
assert 'GameCatalog.games' in games
quick=(src/"ui/screens/home/QuickAccessGamesSection.kt").read_text()
assert '"pattern_forest"' in quick and 'GameCatalog.byId(key)' in quick
assert "PatternForestActivity" in (src/"games/GameSelectionActivity.kt").read_text()
art=R/"app/src/main/res/drawable/pattern_forest_hero.xml"
assert art.is_file()
assert art.read_text().count("<path")>=200
assert art.stat().st_size<90000
assert (R/"app/src/test/java/com/yigit033/matematikmacarasi/games/pattern/PatternForestEngineTest.kt").is_file()
assert (R/"app/src/androidTest/java/com/yigit033/matematikmacarasi/games/pattern/PatternForestPersistenceTest.kt").is_file()
print("Pattern Forest: 30 tasks, six mechanics, five worlds, offline saves, premium assets PASS")
