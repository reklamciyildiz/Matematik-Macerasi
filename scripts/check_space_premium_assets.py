#!/usr/bin/env python3
"""Static premium Space art regression tests. No Pillow needed on Android CI."""
from pathlib import Path
import struct
ROOT=Path(__file__).resolve().parent.parent
ASSET_ROOT=ROOT/"app/src/main/res/drawable-nodpi"
NAMES=["bg_terra","bg_aqua","bg_pyra","bg_luna","bg_nova",
       "ship","meteor_1","meteor_2","meteor_3","meteor_4",
       "laser","impact"]
for name in NAMES:
    f=ASSET_ROOT/("space_"+name+"_premium.webp")
    assert f.is_file(),f"Missing premium WebP: {f}"
    data=f.read_bytes()
    assert len(data)>=1000 and len(data)<360000,(name,len(data))
    assert data[:4]==b"RIFF" and data[8:12]==b"WEBP",f"Invalid WebP header: {name}"
    size=struct.unpack_from("<I",data,4)[0]+8
    assert size<=len(data),f"Truncated WebP: {name}"
    # Production images are 2D WebP, not renamed PNG/JPEG.
    assert data[12:16] in (b"VP8 ",b"VP8L",b"VP8X"),(name,data[12:16])
    print(f"OK: {f.name} ({len(data)//1024} KiB)")
src=(ROOT/"app/src/main/java/com/yigit033/matematikmacarasi/games/interactive")
arena=(src/"SpaceCombatArena.kt").read_text()
screen=(src/"SpaceExpeditionScreen.kt").read_text()
mapper=(src/"SpacePremiumArt.kt").read_text()
for marker in ["SpacePremiumArt.background(run.planet)","space_ship_premium",
               "space_laser_premium","space_impact_premium",
               "space_meteor_1_premium","drawImage(","performHapticFeedback",
               "space_combat_arena","space_fire"]:
    assert marker in arena, f"Premium arena integration missing: {marker}"
for name in ["terra","aqua","pyra","luna","nova"]:
    assert "space_bg_"+name+"_premium" in mapper
assert "SpacePremiumArt.background(planet)" in screen
assert "R.drawable.space_ship_premium" in screen
print("S3.6.3 premium scene art integration and asset signature checks: PASS")
