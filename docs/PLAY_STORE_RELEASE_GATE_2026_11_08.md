# Matematik Macerası — Play Store 1.3.0 / code 15 release gate

**2026-11-10 ek:** Aylık/yıllık abonelikli signed AAB, **yalnızca Internal Testing** amacıyla yerel `MM_PLAY_SUBS_INTERNAL_QA_BUILD=true` ve geçerli tüm imza, D1 ve kod-import koşullarıyla oluşturulabilir. Gerçek iki Play lisans test ödemesi geçmeden Production'a çıkarılmaz. Uçtan uca hızlı yol: [`PLAY_SUBSCRIPTION_TWO_TEST_GO_NO_GO.md`](PLAY_SUBSCRIPTION_TWO_TEST_GO_NO_GO.md). `MM_PLAY_SUBS_LIVE_QA_COMPLETE=true` yalnız bu QA tamamlandığında kullanılır. Aşağıdaki 8 Kasım kısıtları, eski varsayılan kapalı durumu açıklar.

Prepared 2026-11-08. Status: **NOT submitted to Play Console**.
Repository has no Play Console authorisation, private signing key, credentials or validated service account. Do not assume a GitHub green check means checkout/refunds/production are verified.

## Current code facts

- Android applicationId: `com.yigit033.matematikmacarasi`
- Target API 36 (required for new apps/updates since August 31, 2026)
- Billing client 8.0.0 (version 7 expired August 31, 2026)
- One-time SKU `matematik_macerasi_premium` retained.
- Cloudflare signed Premium code: 2 devices; 60-day recheck + 7-day offline grace. **Complete legacy Firebase code import, QA and signing-public-key match before release.**
- Monthly/yearly subscriptions: modeled, **DISABLED** until Play developer server verification.
- Android 1.3.0 / versionCode 15 staged. Check Play Console's greatest deployed/testing versionCode; increase again if 15 is already used.

## Release AAB hard stop

`assembleDebug` does not enforce release prerequisites. However `bundleRelease` and `assembleRelease` are blocked unless:
1. `MM_RELEASE_PREMIUM_MODE=cloudflare`, `MM_USE_CLOUDFLARE=true`, and Cloudflare URL and correct public RSA key are supplied via **untracked** local Gradle config.
2. `MM_LEGACY_CODE_IMPORT_QA_COMPLETE=true` set **only after** existing Firebase activations were imported and restored in live D1. Do not set this as a workaround.
3. `MM_SUBSCRIPTIONS_ENABLED=false` until authenticated Play Developer API and RTDN validation exist.
4. Local upload-key file `app/matematik_macerasi.keystore`, `KEYSTORE_PASSWORD`, `KEY_PASSWORD` are present. They MUST NOT be committed or posted in chat. Check upload-key certificate against Play Console. AAB uploads use Play App Signing.

Gradle properties are intentionally absent from GitHub; they are never packaged as source files.

## Safe local build and first upload

```powershell
cd C:\active_projects\MatematikMacerasiii
git pull --ff-only origin main
.\gradlew.bat :app:clean :app:testDebugUnitTest :app:bundleRelease
```

After these prerequisites and a successful signed build, locate `app\build\outputs\bundle\release\app-release.aab`. **Upload to the Play Console Internal testing track first**. Verify package name, unique versionCode, Play App Signing upload certificate, store product visibility, monthly/yearly disabled (lifetime remains), data safety forms, privacy policy, content rating, target audience, families ads/monetization, permissions, billing refund/restore QA, real-device navigation and crash data.

New personal developer accounts created after 2023-11-13 generally need at least 12 opted-in testers continuously for 14 days in closed testing before production access. This is account- and app-dependent; check actual Play Console status.

**Do not** publish to Production, activate subscription checkout or adjust the Firebase/Cloudflare production DB based on code review alone.

## Emulator code entitlement diagnostic (no secrets)

```powershell
$adb = "C:\Android\Sdk\platform-tools\adb.exe"
& $adb devices
& $adb shell run-as com.yigit033.matematikmacarasi ls shared_prefs
```

The output only lists local preference filenames. Check whether `cf_premium_license_v1.xml` exists. Do not send its content or a code/token. If signed license exists but the installed APK was built with `MM_USE_CLOUDFLARE=false`, it cannot verify that signed license. Rebuild with the exact matching **public** key and Worker URL, install with `adb install -r` (without uninstall / clear data) and use Settings > Satın Alma ve Premium Kodunu Geri Yükleme.

## Google Play Console tasks (developer action)

- Complete Google payments merchant profile / tax and country pricing.
- Keep INAPP lifetime SKU in place.
- Add subscription ID `matematik_macerasi_premium_subscription` (do not use the INAPP ID); activate `monthly` (P1M) and `yearly` (P1Y) auto-renewing base plans only once purchase server QA is scheduled. Base-plan IDs are permanent after activation.
- Explicitly disclose recurring price/period/renewal terms at checkout, and let subscribers manage/cancel in Play.
- Configure Google Play Android Publisher API permissions and authenticated Pub/Sub RTDN for the backend.
- Test purchase, renewal, cancellation until paid-through, grace, hold, refund/void, restore, multi-account, legacy lifetime and code.
- Only then lift `MM_SUBSCRIPTIONS_ENABLED` after review of backend proof; avoid offering monthly/yearly based solely on Android `queryPurchasesAsync`.

Official references:
- https://support.google.com/googleplay/android-developer/answer/11926878
- https://developer.android.com/google/play/billing/deprecation-faq
- https://support.google.com/googleplay/android-developer/answer/140504
- https://developer.android.com/google/play/billing/lifecycle/subscriptions
- https://support.google.com/googleplay/android-developer/answer/18265346
- https://support.google.com/googleplay/android-developer/answer/14151465
- https://developer.android.com/studio/publish/app-signing
