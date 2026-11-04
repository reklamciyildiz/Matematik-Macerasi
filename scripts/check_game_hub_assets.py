#!/usr/bin/env python3
"""S4 game catalog resource integrity and route regression check."""
from pathlib import Path
root=Path(__file__).resolve().parent.parent
res=root/"app/src/main/res/drawable-nodpi"
names=['addition','subtraction','multiplication','division','number_hunt','maze','forest']
for name in names:
    path=res/f"card_{name}_premium.webp"
    assert path.is_file(),f"Missing illustrated WebP: {path}"
    data=path.read_bytes()
    assert data[:4]==b'RIFF' and data[8:12]==b'WEBP',(path,'invalid WebP')
    assert 3000<len(data)<300000,(path,len(data))
    print("WebP OK",name,len(data))
main=root/"app/src/main/java/com/yigit033/matematikmacarasi"
q=(main/"ui/screens/home/QuickAccessGamesSection.kt").read_text()
c=(main/"ui/screens/home/GamesTabScreen.kt").read_text()
catalog=(main/"ui/screens/home/GameCatalog.kt").read_text()
h=(main/"games/basic/OperationsHubActivity.kt").read_text()
g=(main/"games/basic/GradeAwareOperationPolicy.kt").read_text()
assert "GameCatalog.byId(key)" in q and "GameLaunchHistory.open(context,card.game)" in q
for v in ["OperationsHubActivity::class.java", "R.drawable.card_four_operations",
          "R.drawable.card_number_hunt_premium", "ForestAdventureActivity",
          "SayiAvi2Activity","SpaceMathAdventureActivity",
          "GeometryCastleActivity", "GameCatalogItem(\"hunt\""]:
    assert v in catalog,v
for v in ["GameCatalog.games", "GameCatalog.operationsHub", "GameLaunchHistory.open(context,item)"]:
    assert v in c,v
for v in ["OperationKind.entries","MixedOperationSession","grade","last_operation"]:
    assert v in h,v
for v in ["fun maxAnswer","fun allowed","OperationKind.MULTIPLY",
          "OperationKind.DIVIDE","fun generate"]:
    assert v in g,v
manifest=(root/"app/src/main/AndroidManifest.xml").read_text()
assert 'android:name=".games.basic.OperationsHubActivity"' in manifest
for op in ["Addition","Subtraction","Multiplication","Division"]:
    act=(main/f"games/basic/{op}GameActivity.kt").read_text()
    assert 'GradeAwareOperationPolicy.generateUnique(' in act, f'{op}: unique-question generation not wired' 
    assert 'practiceState.effectiveDifficulty(selectedDifficulty)' in act, f'{op}: support tier not wired' 
    assert 'GradeAwareOperationPolicy.options(q,OperationKind.' in act, f'{op}: contextual distractors not wired'
    assert 'PremiumOperationDifficultySelect(' in act
assert (main/"games/basic/MixedOperationEngine.kt").is_file()
assert (main/"games/basic/OperationPracticeState.kt").is_file()
print("Game hub entry mapping / grade gates / adaptive operation sessions: PASS")
