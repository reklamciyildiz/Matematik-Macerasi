#!/usr/bin/env python3
"""Static regression guard for Play Billing catalog states; not a real purchase test."""
from pathlib import Path

root = Path(__file__).resolve().parents[1]
app = root / "app/src/main/java/com/yigit033/matematikmacarasi"
source = (app / "services/payments/PurchaseManager.kt").read_text(encoding="utf-8")
settings = (app / "screens/SettingsScreen.kt").read_text(encoding="utf-8")

checks = {
    "legacy INAPP retained": 'PREMIUM_SKU = "matematik_macerasi_premium"' in source,
    "subscription product retained": 'PREMIUM_SUBSCRIPTION_SKU = "matematik_macerasi_premium_subscription"' in source,
    "Google Play automatic reconnect": ".enableAutoServiceReconnection()" in source,
    "catalog timeout": "delay(12_000)" in source and "zaman aşımına uğradı" in source,
    "two distinct Play product type requests": "BillingClient.ProductType.INAPP to PREMIUM_SKU" in source and
       "BillingClient.ProductType.SUBS to PREMIUM_SUBSCRIPTION_SKU" in source,
    "Google Play unfetched product diagnostics": "details.unfetchedProductList" in source and
       "unfetched.statusCode" in source,
    "catalog status not permanently loading": "CatalogStatus.Error(" in source and
       "CatalogStatus.Ready(unavailable)" in source,
    "no unreliable app package detection before checkout": 'getPackageInfo("com.android.vending"' not in source and
       'getPackageInfo("com.google.android.gms"' not in source,
    "explicit user-initiated purchase only": "launchPurchaseFlow(activity, plan)" in source,
    "UI only enables current-price ready checkout": "enabled=price!=null && catalog is PurchaseManager.CatalogStatus.Ready" in settings,
    "UI explicit retry action": "Google Play fiyatlarını yeniden kontrol et" in settings,
    "UI visible error state": "catalogState.message" in settings,
    "original code redemption retained": "Premium Kodum Var" in settings,
    "original purchase restoration retained": "Satın Alımları Geri Yükle" in settings,
}
failures = [k for k,v in checks.items() if not v]
if failures:
    for failure in failures:
        print("FAIL:", failure)
    raise SystemExit(1)
print("PASS: Play Billing catalog recovery, error UI, lifetime SKU and purchase guards")
print("NOTE: Play Console product activation and real transactions are NOT verified by this check")
