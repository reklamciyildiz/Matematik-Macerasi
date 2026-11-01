#!/usr/bin/env python3
"""Fail CI if premium Geometry Castle visuals are absent, corrupted or disconnected.

Standard library only; works on the repository without Android SDK or Pillow.
"""
from pathlib import Path
import hashlib
import sys

ROOT = Path(__file__).resolve().parents[1]
DRAWABLE = ROOT / "app/src/main/res/drawable-nodpi"
SCREEN = ROOT / (
    "app/src/main/java/com/yigit033/matematikmacarasi/games/geometry/"
    "GeometryCastleScreen.kt"
)
QUICK_ACCESS = ROOT / (
    "app/src/main/java/com/yigit033/matematikmacarasi/ui/screens/home/"
    "QuickAccessGamesSection.kt"
)
EXPECTED = {
    "geometry_castle_hero_premium.webp": "17f678b03bd09467672c733b5c675575a6e8e094df45e1925ac332115d7f68fd",
    "geometry_castle_courtyard_premium.webp": "d48b4c5e2a0d435c09dbbd2da9a619ee47cd77109e9ccf6ba586ee8be70165f3",
    "geometry_castle_bridge_premium.webp": "b17db23f5c9bb2bde58139053e80e5a4798de6627e14f0334ee17d21433c1241",
    "geometry_castle_mirror_premium.webp": "4d0dfdbebdc4d25813396e681cd42480edf45df81287b344c7034f2f5ad4bf0f",
    "geometry_castle_crown_premium.webp": "652d0f23ef2c5a492c4d9e75579f6d52abc1650b0ce16d04ddf318df0619e12f",
    "geometry_castle_complete_premium.webp": "4993a8bfbcbe1c000d19b879b4803344164143a6009c9bb3d6ff3b118b8ba02c",
    "geometry_piko_premium.webp": "c11d123c7a7e8fffa7b65091b41939e6b80f21df5424c4e0bc3c7a1f6ee42a4c",
}

def main() -> int:
    screen = SCREEN.read_text(encoding="utf-8")
    quick = QUICK_ACCESS.read_text(encoding="utf-8")
    failures = []
    total = 0
    for name, checksum in EXPECTED.items():
        path = DRAWABLE / name
        if not path.is_file():
            failures.append(f"{name}: missing")
            continue
        data = path.read_bytes()
        total += len(data)
        if not (data[:4] == b"RIFF" and data[8:12] == b"WEBP"):
            failures.append(f"{name}: invalid WebP signature")
        if hashlib.sha256(data).hexdigest() != checksum:
            failures.append(f"{name}: sha256 mismatch")
        if not 4_000 <= len(data) <= 150_000:
            failures.append(f"{name}: unexpected size ({len(data)} bytes)")
        if f"R.drawable.{name.removesuffix('.webp')}" not in screen:
            failures.append(f"{name}: GeometryCastleScreen does not reference resource")
    if "R.drawable.geometry_castle_hero_premium" not in quick:
        failures.append("QuickAccessGamesSection does not use premium hero")
    if failures:
        for failure in failures:
            print("ERROR:", failure, file=sys.stderr)
        return 1
    print(f"PASS: {len(EXPECTED)} premium WebPs connected, {total} compressed bytes")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
