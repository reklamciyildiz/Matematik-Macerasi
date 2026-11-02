#!/usr/bin/env python3
"""S4.4 Audio quality gate: original PCM assets, routing and controls."""
from pathlib import Path
import wave
import struct

ROOT = Path(__file__).resolve().parent.parent
raw = ROOT / "app/src/main/res/raw"
durations = {
    "mm2_adventure_theme": (26, 34),
    "mm2_correct": (.3, .8),
    "mm2_wrong": (.3, .8),
    "mm2_success": (1, 2),
    "mm2_failure": (.7, 1.4),
    "mm2_level": (1, 2),
    "mm2_quest": (.6, 1.5),
    "mm2_coin": (.2, .6),
    "mm2_combo": (.3, .8),
    "mm2_ui": (.08, .25),
    "mm2_pop": (.1, .4),
    "mm2_whoosh": (.2, .5),
    "mm2_laser": (.2, .5),
    "mm2_impact": (.2, .7),
}
for name, (low, high) in durations.items():
    path = raw / f"{name}.wav"
    assert path.exists(), f"Missing audio asset: {path}"
    with wave.open(str(path), "rb") as w:
        assert w.getcomptype() == "NONE", name
        assert w.getnchannels() == 1, name
        assert w.getsampwidth() == 2, name
        assert w.getframerate() == 22050, name
        duration = w.getnframes() / w.getframerate()
        assert low <= duration <= high, (name, duration)
        raw_bytes = w.readframes(w.getnframes())
        samples = struct.iter_unpack("<h", raw_bytes)
        peak = max(abs(value) for (value,) in samples)
        assert 1500 < peak < 32766, (name, peak)
        print(f"Audio OK: {name} ({duration:.2f}s, peak {peak})")

src = (ROOT / "app/src/main/java/com/yigit033/matematikmacarasi/services/AudioService.kt").read_text()
for name in durations:
    assert f"R.raw.{name}" in src, f"Audio cue not wired: {name}"
for expected in (
    "SoundPool.Builder()", "Player.REPEAT_MODE_ONE",
    "playFailureSound()", "playQuestCompleteSound()",
    "playLevelCompleteSound()", "setMusicVolume(value:",
    "setEffectsVolume(value:", "ConcurrentHashMap.newKeySet",
):
    assert expected in src, f"Missing audio behavior: {expected}"

settings = (ROOT / "app/src/main/java/com/yigit033/matematikmacarasi/screens/SettingsScreen.kt").read_text()
for term in ("getMusicVolume()", "getEffectsVolume()", "playVictorySound()", "Slider("):
    assert term in settings, term
print("S4.4 premium audio source and WAV quality checks: PASS")
