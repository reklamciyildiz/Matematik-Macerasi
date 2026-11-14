#!/usr/bin/env python3
"""S3.3.1 visual contract smoke checks for native Compose Settings/Profile."""
from pathlib import Path
base=Path(__file__).resolve().parents[1]
root=base/"app/src/main/java/com/yigit033/matematikmacarasi"
settings=(root/"screens/SettingsScreen.kt").read_text(encoding="utf-8")
host=(root/"screens/SettingsActivity.kt").read_text(encoding="utf-8")
profile=(root/"profile/ProfileActivity.kt").read_text(encoding="utf-8")
avatar=(root/"screens/AvatarStoreActivity.kt").read_text(encoding="utf-8")
popup=(root/"components/StatisticsDetailPopup.kt").read_text(encoding="utf-8")
policy=(root/"profile/AdventureStatisticsPolicy.kt").read_text(encoding="utf-8")
checks=[
    ("old purple stack removed","0xFFCE93D8" not in settings),
    ("readable section surface","color=SettingsPalette.panel" in settings),
    ("no forced white content card","CardDefaults.cardColors(" not in settings),
    ("restore button retained","Satın Alımları Geri Yükle" in settings),
    ("restore feedback readable","Text(resultText,color=SettingsPalette.secondary" in settings),
    ("premium active state retained","Premium Aktif" in settings),
    ("free-only optional plan comparison",
         "if (!isPremium)" in settings and
         "if (showPlanComparison)" in settings and
         "Tüm hakları karşılaştır" in settings and
         "PremiumFeaturePolicy.marketShopCount" in settings),
    ("audio controls retained","audioService.setMusicEnabled" in settings and
         "audioService.setSoundEnabled" in settings),
    ("notification preference retained",'putBoolean("notifications_enabled",it)' in settings),
    ("icon access retained","IconSelectorActivity::class.java" in settings),
    ("premium code redemption retained","validateAndActivateCode(code)" in settings),
    ("direct lifetime Play checkout from Settings",
         "purchaseManager.buyPremium(it,plan)" in settings and
         "PremiumPlan.LIFETIME" in settings and
         "enabled=price!=null && catalog is PurchaseManager.CatalogStatus.Ready" in settings),
    ("profile hero preserved and reflects Avatar 3.0",
         "Avatar3PlayerPortrait(" in profile and "onAvatarStoreClick" in profile),
    ("profile editing preserved",'onNameChange(it)' in profile and 'onClassChange(option)' in profile),
    ("profile personalization retained","Kişiselleştirme ve Ayarlar" in profile and
         "onSettingsClick" in profile and "onAvatarStoreClick" in profile),
    ("profile stats preserved","StatisticsItemModern(" in profile),
    ("profile not legacy light backdrop","Color(0xFF81D4FA)" not in profile),
    ("edge-to-edge host","WindowCompat.setDecorFitsSystemWindows(window,false)" in host and
         "safeDrawingPadding()" in host),
    ("profile edge-to-edge","WindowCompat.setDecorFitsSystemWindows(window,false)" in profile and
         "safeDrawingPadding()" in profile),
    ("avatar cutout-safe layout",".safeDrawingPadding()" in avatar and
         "modifier = Modifier.padding(start = 5.dp)" in avatar and
         ".size(52.dp)" in avatar),
    ("profile accessible back area","contentDescription = \"Kaşif profilinden geri dön\"" in profile and
         ".size(52.dp)" in profile),
    ("settings accessible back area","Modifier.size(52.dp).clickable(onClick=onBackClick)" in settings),
    ("no profile white surface panels","backgroundColor = Color.White" not in profile and
         ".background(Color.White.copy(alpha" not in profile),
    ("new personal info surface",'Text("Kaşif Bilgilerim"' in profile and
         '"Sınıf düzeyi"' in profile),
    ("compact statistics tiles",'Text("Macera İstatistiklerim"' in profile and
         'modifier = Modifier.weight(1f)' in profile),
    ("statistics details retained",'"İlerleme Yıldızları"' in profile and
         '"Biten Oyunlar"' in profile and "StatisticsDetailPopup(" in profile),
    ("free summaries and premium breakdown protected",
         "AdventureStatisticsPolicy.showValue" in profile and
         "AdventureStatisticsPolicy.allowBreakdown" in profile and
         "isPremium" in policy),
    ("statistics overlay respects dark theme",
         "backgroundColor = Color.White" not in popup and
         "Henüz oyun kaydı yok" in popup),
]
errors=[message for message,ok in checks if not ok]
if errors:
    for e in errors:print("ERROR:",e)
    raise SystemExit(1)
print("PASS: premium Settings/Profile surface contrast, navigation, functionality and system bars")
