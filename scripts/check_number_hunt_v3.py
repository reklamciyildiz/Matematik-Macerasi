#!/usr/bin/env python3
"""Sayı Avı 3.0: grade content, legacy migration, persistence and premium UX guards."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
base=root/"app/src/main/java/com/yigit033/matematikmacarasi"
world=(base/"games/sayiavi2/NumberHuntGradeEngine.kt").read_text(encoding="utf-8")
store=(base/"games/sayiavi2/NumberHuntV3Store.kt").read_text(encoding="utf-8")
ui=(base/"games/sayiavi2/NumberHuntV3Screen.kt").read_text(encoding="utf-8")
rich=(base/"games/sayiavi2/NumberHuntRichContent.kt").read_text(encoding="utf-8")
host=(base/"games/sayiavi2/SayiAvi2Screen.kt").read_text(encoding="utf-8")
legacy=(base/"games/sayiavi2/SayiAviStorage.kt").read_text(encoding="utf-8")
activity=(base/"games/sayiavi2/SayiAvi2Activity.kt").read_text(encoding="utf-8")
catalog=(base/"ui/screens/home/GameCatalog.kt").read_text(encoding="utf-8")
home=(base/"ui/screens/home/GamePersonalization.kt").read_text(encoding="utf-8")
welcome=(base/"games/sayiavi2/SayiAviWelcomeScreen.kt").read_text(encoding="utf-8")
for s in ('1..2','3..4','5..6','7..8','const val TASK_COUNT=',
          'SCAN(','BRIDGE(','DIAL(','PAIRS(',
          'Asal sayı kristallerini avla','Karekökü tam sayı olan',
          'Rasyonel sayı köprüsünü','Üslü ve köklü taşları'):
    assert s in world,s
assert 'fun generate(grade:Int,tier:LearningTier,seed:Long)' in world
for k in ('number_hunt_worlds_v3','@Synchronized fun begin(',
          '@Synchronized fun solve(', '@Synchronized fun advance(',
          '@Synchronized fun finish(','putBoolean(p+"ever_completed",true)',
          'putBoolean(p+"solved",true).commit()','starAcknowledged('):
    assert k in store,k
assert 'sayi_avi_2_preferences' in legacy
assert 'SayiAviMissionStage.WORLD_ATLAS' in host
assert 'mutableStateOf(SayiAviMissionStage.WORLD_ATLAS)' in host
assert 'openClassicCheckpoint()' in host
assert '"STAGE_C" -> SayiAviMissionStage.STAGE_C_ROUTE' in host
assert 'onStartGradeWorld' in welcome
assert 'GameGradeMode.GRADE_1_8' in catalog
assert 'id=="hunt" -> "$grade. sınıfa göre sayı avları"' in catalog
assert 'if(id=="hunt" && grade!=2)' in home
for k in ('NumberHuntPuzzle(', 'NumberHuntGradeEngine.Mechanic.entries',
          'onWorldFinished','store.advance(grade,run.index)',
          'store.finish(grade,run.seed)','statusBarsPadding()',
          'navigationBarsPadding()','confirmOverwrite'):
    assert k in ui,k
for k in ('sayi_avi_3_grade_$grade','getGameStars(rewardId)',
          'acknowledgeStar(grade)','level=grade'):
    assert k in activity,k
assert (root/"app/src/test/java/com/yigit033/matematikmacarasi/sayiavi2/NumberHuntGradeEngineTest.kt").exists()
assert (root/"app/src/test/java/com/yigit033/matematikmacarasi/sayiavi2/NumberHuntRichContentTest.kt").exists()
for token in ('const val TASK_COUNT=CHAPTERS*TASKS_PER_CHAPTER',
              'scan(grade,chapter,tier,r)','bridge(grade,chapter,tier,r)',
              'dial(grade,chapter,tier,r)','pairs(grade,chapter,tier,r)',
              'cipher(grade,chapter,tier,r)','trail(grade,chapter,tier,r)',
              'result.map{it.prompt}.distinct().size==TASK_COUNT'):
    assert token in rich,token
for token in ('content_version','NumberHuntRichContent.VERSION',
              'partial_steps','@Synchronized fun savePartial(',
              'expectedIndex !in 0 until session.taskCount',
              'session.index!=session.taskCount'):
    assert token in store,token
for token in ('NumberHuntRichContent.generate(grade,run.tier,run.seed)',
              'useSavedPartial=modern','onPartial={savePartial(it)}',
              'run.taskCount','is NumberHuntGradeEngine.Cipher',
              'is NumberHuntGradeEngine.Trail'):
    assert token in ui,token
print("Sayı Avı 3.0 world policy, progression, legacy safety and rewards: PASS")
