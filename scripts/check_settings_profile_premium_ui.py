#!/usr/bin/env python3
"""S3.3.1 visual contract smoke checks for native Compose Settings/Profile."""
from pathlib import Path
base=Path(__file__).resolve().parents[1]
root=base/"app/src/main/java/com/yigit033/matematikmacarasi"
settings=(root/"screens/SettingsScreen.kt").read_text(encoding="utf-8")
host=(root/"screens/SettingsActivity.kt").read_text(encoding="utf-8")
profile=(root/"profile/ProfileActivity.kt").read_text(encoding="utf-8")
checks=[
    ("old purple stack removed","0xFFCE93D8" not in settings),
    ("readable section surface","color=SettingsPalette.panel" in settings),
    ("no forced white content card","CardDefaults.cardColors(" not in settings),
    ("restore button retained","Satın Alımları Geri Yükle" in settings),
    ("restore feedback readable","Text(resultText,color=SettingsPalette.secondary" in settings),
    ("premium active state retained","Premium Aktif" in settings),
    ("four core feature rows","Free ve Premium" in settings and
         "PremiumFeaturePolicy.marketShopCount" in settings),
    ("audio controls retained","audioService.setMusicEnabled" in settings and
         "audioService.setSoundEnabled" in settings),
    ("notification preference retained",'putBoolean("notifications_enabled",it)' in settings),
    ("icon access retained","IconSelectorActivity::class.java" in settings),
    ("premium code redemption retained","validateAndActivateCode(code)" in settings),
    ("premium single billing gate","purchaseManager.buyPremium(it)" in settings),
    ("profile hero preserved","AnimatedAvatarView(" in profile),
    ("profile editing preserved",'onNameChange(it)' in profile and 'onClassChange(option)' in profile),
    ("profile personalization retained","Kişiselleştirme ve Ayarlar" in profile and
         "onSettingsClick" in profile and "onAvatarStoreClick" in profile),
    ("profile stats preserved","StatisticsItemModern(" in profile),
    ("profile not legacy light backdrop","Color(0xFF81D4FA)" not in profile),
    ("edge-to-edge host","WindowCompat.setDecorFitsSystemWindows(window,false)" in host and
         "safeDrawingPadding()" in host),
    ("profile edge-to-edge","WindowCompat.setDecorFitsSystemWindows(window,false)" in profile and
         "safeDrawingPadding()" in profile),
]
errors=[message for message,ok in checks if not ok]
if errors:
    for e in errors:print("ERROR:",e)
    raise SystemExit(1)
print("PASS: premium Settings/Profile surface contrast, navigation, functionality and system bars")
