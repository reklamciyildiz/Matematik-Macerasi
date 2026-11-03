#!/usr/bin/env python3
"""Release regression gate for Geometry Castle v3 and responsive home navigation."""
from pathlib import Path
ROOT=Path(__file__).resolve().parent.parent
src=ROOT/"app/src/main/java/com/yigit033/matematikmacarasi"
geom=src/"games/geometry"
eng=(geom/"GeometryCastleEngine.kt").read_text(encoding="utf8")
store=(geom/"GeometryCastleProgressStore.kt").read_text(encoding="utf8")
ui=(geom/"GeometryCastleScreen.kt").read_text(encoding="utf8")
workshop=(geom/"GeometryCastleMeasure.kt").read_text(encoding="utf8")
activity=(geom/"GeometryCastleActivity.kt").read_text(encoding="utf8")
home=(src/"ui/screens/home/HomeScreenNew.kt").read_text(encoding="utf8")
nav=(src/"ui/screens/home/HomeBottomNavigation.kt").read_text(encoding="utf8")
for term in ("PREMIUM_TOTAL_TASKS = 20","PREMIUM_STEPS_PER_ROOM = 5",
             "contentVersion<=1->legacyTasks","contentVersion==2->expandedTasks",
             "else->premiumTasks","fun premiumTasks(","fun available(tier:LearningTier,grade:Int)",
             "CastleMode.MEASURE","measureAnswer","measureValue",
             "geometry_area","geometry_perimeter","geometry_angle",
             "val nextStep = if(run.step == stepsPerRoom(run)-1)"):
    assert term in eng,term
for term in ("grade = prefs.getInt","measureValue = prefs.getInt",
             'putInt("grade",next.grade)',"fun begin(tier: LearningTier, grade: Int? = null)",
             "if(grade==null)2 else 3","GeometryCastleEngine.taskIndex",
             "next != GeometryCastleEngine.advance(previous)"):
    assert term in store,term
for term in ("run.contentVersion <= 1","placed=emptyList(),solved=true",
             "GeometryMeasureBoard(","profileGrade: Int","store.begin(tier,profileGrade)",
             "GeometryCastleEngine.totalTasks(run)",
             "statusBarsPadding().navigationBarsPadding()"):
    assert term in ui,term
assert "PlayerProfileService.getInstance(this).getUserClass()" in activity
assert "GeometryCastleScreen(" in activity and "profileGrade=" in activity
assert "onValueChangeFinished=" in workshop
assert 'val drawingWidth=if(kind=="triangle_area")2*w else w' in workshop
assert 'val width=unit*drawingWidth;val height=unit*h' in workshop
assert "task.measureKind" in workshop
assert "onValue:(Int)->Unit" in workshop
assert "GeometrySketch" in workshop
assert "navigationBarsPadding()" in nav
assert ".padding(bottom = 12.dp)" in nav
assert ".heightIn(min = 84.dp)" in nav
assert ".height(84.dp)" not in nav
assert "TextOverflow.Ellipsis" in nav
assert "HomeBottomNavigationBar(" in home
assert "Modifier.align(Alignment.BottomCenter)" not in home
assert 'Spacer(modifier = Modifier.height(96.dp))' not in home
assert (ROOT/"app/src/test/java/com/yigit033/matematikmacarasi/games/geometry/GeometryCastleV3EngineTest.kt").exists()
assert (ROOT/"app/src/androidTest/java/com/yigit033/matematikmacarasi/games/geometry/GeometryCastleV3PersistenceTest.kt").exists()
print("Geometry v3/legacy bridge and Android bottom navigation source QA PASS")
