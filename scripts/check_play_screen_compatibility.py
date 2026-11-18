#!/usr/bin/env python3
"""Release gate: portrait is a UI preference, not a required hardware feature."""
from pathlib import Path
from xml.etree import ElementTree as ET
import re

root = Path(__file__).resolve().parents[1]
manifest_path = root / "app/src/main/AndroidManifest.xml"
doc = ET.parse(manifest_path).getroot()
android = "{http://schemas.android.com/apk/res/android}"
portrait_feature = [
    feature for feature in doc.findall("uses-feature")
    if feature.get(android + "name") == "android.hardware.screen.portrait"
]
assert len(portrait_feature) == 1, "Declare portrait hardware exactly once"
assert portrait_feature[0].get(android + "required") == "false", (
    "Play Console blocks devices when portrait screen hardware is required"
)
# Keep existing per-game orientation behavior; don't alter the games to bypass Play.
portrait_games = [
    activity for activity in doc.find("application").findall("activity")
    if activity.get(android + "screenOrientation") == "portrait"
]
assert len(portrait_games) == 5, "Unexpected change to existing portrait-locked games"
gradle = (root / "app/build.gradle").read_text(encoding="utf-8")
code = re.search(r"(?m)^\s*versionCode\s+(\d+)\s*$", gradle)
assert code is not None and int(code.group(1)) >= 25, (
    "Play Console already accepted versionCode 24; a replacement AAB needs a new code"
)
print("Play device compatibility preflight passed: portrait optional; games unchanged; upload code valid")
