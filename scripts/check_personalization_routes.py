#!/usr/bin/env python3
"""Static Android QA: all personalisation routes and launchers must stay reachable."""
from pathlib import Path
import xml.etree.ElementTree as ET
root=Path(__file__).resolve().parents[1]
k=root/"app/src/main/java/com/yigit033/matematikmacarasi"
a="{http://schemas.android.com/apk/res/android}"
manifest=ET.parse(root/"app/src/main/AndroidManifest.xml").getroot()
app=manifest.find("application")
assert app is not None
activities={x.get(a+"name"):x for x in app.findall("activity")}
aliases={x.get(a+"name"):x for x in app.findall("activity-alias")}
main=activities[".MainActivity"]
assert main.get(a+"enabled")!="false", "MainActivity must always stay enabled"
assert not any(y.get(a+"name")=="android.intent.action.MAIN"
    for f in main.findall("intent-filter") for y in f.findall("action")), \
    "MainActivity must not own a duplicate launcher entry point"
assert set([".DefaultIconActivity",".RobotIconActivity",".SpaceIconActivity"]) <= set(aliases)
for name in [".DefaultIconActivity",".RobotIconActivity",".SpaceIconActivity"]:
    alias=aliases[name]
    assert alias.get(a+"targetActivity")==".MainActivity"
    assert any(any(z.get(a+"name")=="android.intent.action.MAIN"
        for z in f.findall("action")) and
        any(z.get(a+"name")=="android.intent.category.LAUNCHER"
        for z in f.findall("category")) for f in alias.findall("intent-filter"))
assert aliases[".DefaultIconActivity"].get(a+"enabled")=="true"
for name in [".screens.SettingsActivity",".screens.AvatarStoreActivity",
             ".settings.IconSelectorActivity"]:
    assert name in activities and activities[name].get(a+"exported")=="false"
assert app.find("receiver") is not None
receiver=next((v for v in app.findall("receiver")
    if v.get(a+"name")==".settings.IconUpgradeReceiver"),None)
assert receiver is not None and receiver.get(a+"exported")=="false"
def src(path):return (k/path).read_text(encoding="utf-8")
profile=src("profile/ProfileActivity.kt")
assert all(s in profile for s in (
    "onSettingsClick", "onAppIconClick", "onAvatarStoreClick",
    "Kişiselleştirme ve Ayarlar", "SettingsActivity::class.java",
    "IconSelectorActivity::class.java"))
settings=src("screens/SettingsScreen.kt")
assert "IconSelectorActivity::class.java" in settings
assert "Matematik Kahramanlarının Abisi" not in settings
host=src("screens/SettingsActivity.kt")
assert "SettingsScreen(" in host and "safeDrawingPadding()" in host
assert "delay(1000)" not in host
icon=src("settings/IconSelectorActivity.kt")
assert "IconLauncherManager.apply" in icon
assert "restartApplication()" not in icon
assert 'COMPONENT_ENABLED_STATE_DISABLED' not in icon
manager=src("settings/IconLauncherManager.kt")
assert "COMPONENT_ENABLED_STATE_ENABLED" in manager
assert "DONT_KILL_APP" in manager
assert manager.index("if(alias==selected)")<manager.index("if(alias!=selected)")
assert "MainActivity" in manager
print("PASS: Settings, avatar and icon routes; safe three-alias launcher configuration")
