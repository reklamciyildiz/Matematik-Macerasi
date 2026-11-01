#!/usr/bin/env python3
"""S3.3 source preflight; not a substitute for Play Console review or real billing."""
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parents[1]
SRC=ROOT/"app/src/main/java/com/yigit033/matematikmacarasi"
read=lambda path:(SRC/path).read_text(encoding="utf-8")
manifest=ET.parse(ROOT/"app/src/main/AndroidManifest.xml").getroot()
a="{http://schemas.android.com/apk/res/android}"
t="{http://schemas.android.com/tools}"
app=manifest.find("application")
checks=[]
def require(ok,message):
    if not ok:checks.append(message)

permissions={x.get(a+"name"):x for x in manifest.findall("uses-permission")}
require("com.android.vending.BILLING" in permissions,"Google Play BILLING permission missing")
for p in ("com.google.android.gms.permission.AD_ID","android.permission.ACCESS_ADSERVICES_AD_ID"):
    require(permissions.get(p) is not None and permissions[p].get(t+"node")=="remove",
        p+" advertising ID permission not explicitly stripped")
billing_test=next((x for x in app.findall("activity")
                    if x.get(a+"name")==".services.payments.BillingTestActivity"),None)
require(billing_test is not None and billing_test.get(a+"enabled")=="false",
        "debug-only Billing Test activity is enabled for release")
gradle=(ROOT/"app/build.gradle").read_text(encoding="utf-8")
require("targetSdk 36" in gradle,"target SDK 36 target missing")
require("billing-ktx:8.0.0" in gradle,"review and maintain Play Billing 8 baseline")
purchase=read("services/payments/PurchaseManager.kt")
require('PREMIUM_SKU = "matematik_macerasi_premium"' in purchase,
        "legacy non-consumable Premium product ID changed")
require("private fun launchPurchaseFlow(" in purchase,
        "purchase checkout can bypass central adult gate")
require("AdultPurchaseChallenge.accepted" in purchase and
        "startPurchaseAfterAdultGate(" in purchase,
        "central adult approval absent")
require("fun restorePurchases()" in purchase and "val restoreState:" in purchase,
        "explicit restore result channel missing")
require("Purchase.PurchaseState.PURCHASED" in purchase and
        "Purchase.PurchaseState.PENDING" in purchase,
        "pending/purchased transition not handled")
require("acknowledgePurchase(purchase)" in purchase,
        "non-consumable acknowledgement path missing")
require("Purchase Token: ${purchase.purchaseToken}" not in purchase,
        "purchase token accidentally logged")
settings=read("screens/SettingsScreen.kt")
require("Satın Alımları Geri Yükle" in settings and
        "Free ve Premium" in settings,
        "parents cannot see restore and Free/Premium comparison in Settings")
policy=read("services/payments/PremiumFeaturePolicy.kt")
require("FREE_MARKET_SHOPS = 3" in policy and
        "PREMIUM_MARKET_SHOPS = 10" in policy,
        "market feature matrix changed unexpectedly")
market=read("games/interactive/MarketPremiumCatalog.kt")
require("PremiumFeaturePolicy.FREE_MARKET_SHOPS" in market,
        "market does not reuse central Premium policy")
avatar=read("services/AvatarService.kt")
require("Fail closed" in avatar or "Fail closed:" in avatar,
        "Premium avatar exception branch must fail closed")
ad=read("services/payments/AdManager.kt")
application=read("modules/Application.kt")
require("CHILD_AD_DELIVERY_DISABLED = true" in ad,
        "Children's ad requests must remain switched off until certified")
require("if(CHILD_AD_DELIVERY_DISABLED) return false" in ad,
        "ad impression gating is missing")
require("if(CHILD_AD_DELIVERY_DISABLED) return" in ad,
        "rewarded ad loading not gated")
require("initializeAdMob()" not in application,
        "app startup still initializes AdMob")
if checks:
    for x in checks:print("ERROR:",x)
    raise SystemExit(1)
print("PASS: S3.3 entitlement, adult gate, restoration UI, ads off, legacy SKU, debug activity")
print("NOTE: Play Console declarations, AAB signing, purchase token backend verification and child SDK review remain external acceptance gates")
