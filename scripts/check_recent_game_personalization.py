#!/usr/bin/env python3
"""Recent activity capture, truthfully labeled progress and recommendation criteria."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
base=root/"app/src/main/java/com/yigit033/matematikmacarasi/ui/screens/home"
src=root/"app/src/main/java/com/yigit033/matematikmacarasi"
catalog=(base/"GameCatalog.kt").read_text()
application=(src/"modules/Application.kt").read_text()
rank=(base/"GameSuggestionPolicy.kt").read_text()
adapter=(base/"GamePersonalization.kt").read_text()
recent=(base/"RecentGamesSection.kt").read_text()
quick=(base/"QuickAccessGamesSection.kt").read_text()
facts=(base/"GameCardFacts.kt").read_text()
recommended=(base/"RecommendedSection.kt").read_text()
for name in ("recent_opened_ids","MAX_RECENT=6","recordActivityOpened(activity:Activity)",
             "lastOpenedId(context:Context)","recordOpened(context:Context,id:String)"):
    assert name in catalog,name
assert 'GameLaunchHistory.recordActivityOpened(activity)' in application
assert 'if(items.isEmpty())return' in recent
assert 'Son Açtıkların' in recent and 'Yeniden aç' in recent
assert 'attempts>=8' in rank and "Ek alıştırma önerisi" in rank
assert 'recorded.profileGrade' not in adapter or 'recorded.profileGrade!=grade' in adapter
assert 'p.profileGrade!=grade' in adapter and 'snapshotForReport(skill)' in adapter
assert 'GameCardFacts(' in facts and 'GameCardFacts(' in quick
assert 'game.gradeNote(grade)' in recommended and 'game.difficultyNote()' in recommended
assert (root/"app/src/test/java/com/yigit033/matematikmacarasi/ui/screens/home/GameSuggestionPolicyTest.kt").exists()
print("Game facts, recent game tracking and transparent recommendations: PASS")
