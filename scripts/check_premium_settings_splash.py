#!/usr/bin/env python3
"""Reference-faithful pricing and branded startup safety checks, not device QA."""
from pathlib import Path
import xml.etree.ElementTree as ET

root = Path(__file__).resolve().parents[1]
src = root / "app/src/main/java/com/yigit033/matematikmacarasi"
read = lambda p: (src / p).read_text(encoding="utf-8")
offer = read("screens/PremiumAdventureUI.kt")
settings = read("screens/SettingsScreen.kt")
main = read("MainActivity.kt")
splash = read("ui/components/BrandedLaunchSplash.kt")
host = read("screens/SettingsActivity.kt")
manifest = ET.parse(root / "app/src/main/AndroidManifest.xml").getroot()
themes = ET.parse(root / "app/src/main/res/values/themes.xml").getroot()

def valid_webp(filename: str) -> bool:
    path = root / "app/src/main/res/drawable-nodpi" / filename
    if not path.exists():
        return False
    data = path.read_bytes()
    return data[:4] == b"RIFF" and data[8:12] == b"WEBP" and 1_000 < len(data) < 260_000

checks = {
    "exact reference-derived optimized fantasy assets":
        all(valid_webp(name) for name in (
            "mm_premium_kids.webp", "mm_premium_crown.webp",
            "mm_premium_footer.webp", "mm_premium_castle.webp",
            "mm_magic_splash_bg.webp")),
    "pricing screen has actual interactive plan selection":
        "rememberSaveable" in offer and
        ".selectable(selected=selected,role=Role.RadioButton,onClick=onSelect)" in offer and
        "chosenName=plan.name" in offer,
    "selected plan drives checkout once on button tap":
        "onClick={onPurchase(chosen)}" in offer and
        "purchaseManager.buyPremium(it,plan)" in settings and
        "onPurchase = { plan ->" in settings,
    "no hardcoded reference mockup amounts in checkout":
        all(price not in offer for price in ("54,90", "349,99", "799,90")) and
        "val enabled = prices[chosen] != null && catalog is PurchaseManager.CatalogStatus.Ready" in offer,
    "checkout double guards user supplied Play offer":
        "if (prices[plan] != null &&" in settings and
        "catalog is PurchaseManager.CatalogStatus.Ready" in settings,
    "subscriptions can be disabled without showing nonexistent products":
        "if(subscriptionsEnabled)" in offer and
        "else listOf(PremiumPlan.LIFETIME)" in offer,
    "Play catalog error/retry and pending state handled":
        "onRetryPrices" in offer and
        "Google Play fiyatlarını yeniden kontrol et" in offer and
        "PurchaseStatus.Pending" in offer,
    "code redemption and purchase restoration unchanged":
        "onCodeClick = { codeDialog = true }" in settings and
        "Premium Kodum Var" in offer and
        "Satın Alımları Geri Yükle" in settings and
        "validateAndActivateCode(code)" in settings,
    "free premium comparison remains expandable":
        "PremiumAdventureComparison(" in settings and
        "onToggle = { showPlanComparison = !showPlanComparison }" in settings and
        "PremiumFeaturePolicy.marketShopCount" in offer,
    "active subscribers can manage their Play plan":
        "onManageSubscription =" in settings and
        "play.google.com/store/account/subscriptions" in settings,
    "system splash comes before Activity super lifecycle":
        "installSplashScreen()\n        super.onCreate(savedInstanceState)" in main and
        "Theme.App.Starting" in (root / "app/src/main/AndroidManifest.xml").read_text(encoding="utf-8"),
    "cold-launch-only branded splash and faded exit":
        "val coldLaunch = savedInstanceState == null" in main and
        "AnimatedVisibility(" in main and "fadeOut(" in main and
        "BrandedLaunchSplash()" in main,
    "branded splash does not fake a real loading percentage":
        "Indeterminate loading glimmer" in splash and
        "Yükleniyor..." in splash and
        "LinearProgressIndicator(progress =" not in splash and
        "progress = 0.75f" not in splash,
    "system and Compose background share night navy":
        "@color/mm_splash_navy" in (root / "app/src/main/res/values/themes.xml").read_text(encoding="utf-8") and
        "#072447" in (root / "app/src/main/res/values/colors.xml").read_text(encoding="utf-8"),
    "startup does not alter icon aliases, monetization or app purchases":
        "HomeScreenNew(lastResumeTime)" in main and
        "initializePaymentAndAdSystems()" in main and
        "Premium Kodum Var" in offer and
        "safeDrawingPadding()" in host,
}
for name, passed in checks.items():
    print(("PASS " if passed else "FAIL ") + name)
if not all(checks.values()):
    raise SystemExit("Premium Settings/Splash regression FAILED")
print("PASS: fantasy design integration, Play gating and cold-start source contracts")
