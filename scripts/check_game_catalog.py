#!/usr/bin/env python3
"""Catalog contract: honest grade labeling, shared routes and accessible groups."""
from pathlib import Path
import re

root=Path(__file__).resolve().parent.parent
base=root/"app/src/main/java/com/yigit033/matematikmacarasi/ui/screens/home"
catalog=(base/"GameCatalog.kt").read_text(encoding="utf-8")
games=(base/"GamesTabScreen.kt").read_text(encoding="utf-8")
quick=(base/"QuickAccessGamesSection.kt").read_text(encoding="utf-8")
recommended=(base/"RecommendedSection.kt").read_text(encoding="utf-8")
home=(base/"HomeScreenNew.kt").read_text(encoding="utf-8")
ids=re.findall(r'GameCatalogItem\("([a-z_]+)"',catalog)
assert len(ids)==15,ids
assert len(set(ids))==len(ids),ids
assert "operations" in ids
assert catalog.count("GameTopic.") >= 15
for category in ("OPERATIONS","NUMBERS","REASONING","REAL_LIFE","ADVENTURES"):
    assert category in catalog
for kind in ("GRADE_1_8","GRADE_1_4","FOUNDATION_2","TIER_ONLY"):
    assert kind in catalog
assert 'grade in 1..4' in catalog and '4. sınıf düzeyi · tekrar' in catalog
assert '2. sınıftan tekrar' in catalog and 'Seviyeni seçerek oyna' in catalog
assert 'if(isLocked(grade))' in catalog
assert 'GameCatalog.games' in games and 'GameTopic.entries' in games
assert 'Sınıfıma göre' in games and 'Sınıfı değiştir' in games
assert 'BoxWithConstraints' in games and 'height(cardHeight)' in games
assert 'maxWidth<310.dp' in games and 'LocalDensity.current.fontScale' in games
assert 'it.isLocked(grade)' in games
assert 'GameLaunchHistory.open(context,item)' in games
assert 'GameCatalog.byId(key)' in quick and 'GameLaunchHistory.open(context,card.game)' in quick
assert 'recentlyOpenedId' in quick and 'sortedBy' in quick
assert 'GameCatalog.suggestionFor(grade, recentlyOpenedId)' in recommended
assert 'recommendedGame.gradeNote(grade)' in recommended
assert 'onChangeGrade = navigateToProfile' in home
assert 'recentlyOpenedId = GameLaunchHistory.lastOpenedId(context)' in home
# No misleading school-grade blanket promise or duplicated game list.
assert 'eğitim içeriği sınıfa göre düzenlenir' not in games
assert 'private fun catalog()' not in games
print("Catalog metadata, honest grade policies, home routing and responsive game groups: PASS")
