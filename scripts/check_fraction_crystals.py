#!/usr/bin/env python3
"""Build-time fraction content / routes / game assets source gate."""
from pathlib import Path
import re

root=Path(__file__).resolve().parent.parent
java=root/"app/src/main/java/com/yigit033/matematikmacarasi"
engine=(java/"games/fractions/FractionCrystalEngine.kt").read_text()
ui=(java/"games/fractions/FractionCrystalsScreen.kt").read_text()
store=(java/"games/fractions/FractionCrystalStore.kt").read_text()
activity=(java/"games/fractions/FractionCrystalsActivity.kt").read_text()
manifest=(root/"app/src/main/AndroidManifest.xml").read_text()
games=(java/"ui/screens/home/GamesTabScreen.kt").read_text()
quick=(java/"ui/screens/home/QuickAccessGamesSection.kt").read_text()
vector=root/"app/src/main/res/drawable/fraction_crystals_hero.xml"
assert vector.exists() and vector.stat().st_size>4000
assert vector.read_text().count("<path")>=50
for expected in ("CrystalFraction", "operator fun plus", "override fun compareTo",
    "FILL(", "BRIDGE(", "COMPARE(", "PORTAL(",
    "fun generate(", "fun correct(", "fraction_arithmetic"):
    assert expected in engine,expected
for expected in ("CrystalDisc(", "PortionBar(", "persist(", "onCompleted(advanced)",
    "playCorrectSound()", "playWrongSound()", "playVictorySound()", "usedHint"):
    assert expected in ui,expected
for expected in ("last_delivered", "first_reward", "fun validate(", "fun save(",
    "fun advance(", "fun newRun("):
    assert expected in store,expected
assert "FRACTION_CRYSTALS" in activity
assert 'android:name=".games.fractions.FractionCrystalsActivity"' in manifest
assert 'GameCatalogItem("fractions"' in games
assert 'FractionCrystalsActivity::class.java' in quick
assert any(f"{count} macera" in games for count in range(12, 101)), "Game World count must not shrink"
assert (root/"app/src/test/java/com/yigit033/matematikmacarasi/games/fractions/FractionCrystalEngineTest.kt").exists()
assert (root/"app/src/androidTest/java/com/yigit033/matematikmacarasi/games/fractions/FractionCrystalPersistenceTest.kt").exists()
print("Fraction Crystals source, content, game routes and unique award: PASS")
