# Matematik Macerası — Aylık/Yıllık Play Premium: iki gerçek test ve yayın kapısı (2026-11-10)

**Durum:** CI mock testleri geçiyor; **gerçek Play lisans test satın alımı henüz yapılmadı**. RTDN test mesajları alınıp onaylandı (1/1, 3/3). Bunlar gerçek ödeme kanıtı değildir.

## Tek Premium hakkı
- INAPP `matematik_macerasi_premium`: mevcut ömür boyu hak; değişmeyecek.
- SUBS `matematik_macerasi_premium_subscription`: `monthly`, `yearly` base planları; **ikisi de aynı Premium içeriği** açar.
- Play subscription Premium'u yalnız Play Developer API + Play Integrity doğrulaması sonrası sunucunun imzaladığı, cihaz bağlı ve en fazla 24 saatlik lease ile açılır. Abonelik süresiz Premium kaydına yazılamaz.
- `SUBSCRIPTIONS_SERVER_ENABLED=false` olan Worker **satın alımı doğrulamaz**; kapalı backend ile abonelik satan build yayımlanmaz.

## Mevcut CI kanıtı
- [Subscription mock integration](https://github.com/reklamciyildiz/Matematik-Macerasi/actions/runs/42686755842): aylık/yıllık ACTIVE durumunda RS256 lease, hold'da yetki reddi, uygunsuz Play Integrity'de reddetme.
- [Android debug and unit CI](https://github.com/reklamciyildiz/Matematik-Macerasi/actions/runs/42686755903): successful. **Bu CI-only Firebase fixture gerçek Play testi değildir.**

## İmzalı AAB ile dahili test açılışı
1. Play Console'da Monthly ve Yearly base planlarının aktif, fiyatların doğru ve test hesabının **License testing** listesinde olduğundan emin ol. Test kullanıcısını **Internal testing** listesine ekle; uygulamayı Play Store'daki opt-in bağlantısından kur.
2. Cloudflare Worker'da AYNI dedicated D1, mevcut gizli anahtarlar ve çalışan RTDN ayarları korunarak abonelik doğrulamasını dahili test için aç. Yerel PowerShell'de `commercial/play-subscriptions` klasöründe `powershell -ExecutionPolicy Bypass -File .\\scripts\\enable-internal-play-qa.ps1 -ForInternalTesting` çalıştır. Script yalnız doğru D1/RTDN bağlamında dağıtır ve `/health` sonucu `enabled:true,rtdnEnabled:true` olmasını kontrol eder. **Yalnızca Internal Testing için; Play Console Production'a yayınlama izni değildir.** Service account JSON/Pem paylaşma.
3. Bilgisayardaki **gitignore edilen** `gradle.properties` içine (public key/URL'ler doğru olduğunda):
   ```properties
   MM_RELEASE_PREMIUM_MODE=cloudflare
   MM_USE_CLOUDFLARE=true
   MM_LEGACY_CODE_IMPORT_QA_COMPLETE=true
   MM_SUBSCRIPTIONS_ENABLED=true
   MM_PLAY_SUBS_INTERNAL_QA_BUILD=true
   MM_PLAY_SUBS_LIVE_QA_COMPLETE=false
   MM_PLAY_SUBS_URL=https://matematik-macerasi-play-subscriptions.matematik-macerasi-yigit033.workers.dev
   MM_PLAY_INTEGRITY_PROJECT_NUMBER=740643037501
   # MM_CLOUDFLARE_URL, MM_CLOUDFLARE_PUBLIC_KEY_B64,
   # MM_PLAY_SUBS_PUBLIC_KEY_B64, KEYSTORE_PASSWORD, KEY_PASSWORD
   # must already be provided locally with their *correct* values.
   ```
   **Önemli:** `MM_LEGACY_CODE_IMPORT_QA_COMPLETE=true` sadece eski Firebase kodlarının D1 aktarımı ve mevcut kullanıcı geri-yükleme testi gerçekten tamamlandıysa kullanılmalı. Emin değilsen yayın engelini atlama.
4. `git pull --ff-only origin main`; ardından `.\gradlew.bat :app:clean :app:testDebugUnitTest :app:bundleRelease`. Çıktı `app\build\outputs\bundle\release\app-release.aab`. Yalnız **Internal testing** kanalına yükle; prod'a yükleme. `versionCode 15` Play'de kullanıldıysa artır. Play App Signing upload key sertifikasını doğrula.

## Gerçek satın alma — sadece iki kritik test
**Test A: Aylık Premium.** Play'den yüklenmiş AAB ile lisans test kullanıcısının hesabından `monthly` satın al; ödeme yönteminde **Test card, always approves** seç. Aylık Premium etkin mi? Orta/Zor oyunlar, premium avatar/market açılıyor mu? Uygulamayı tamamen kapatıp yeniden açınca hâlâ açık mı? Ayarlar > satın alma geri yükle çalışıyor mu? Sunucu `/v1/verify` 200 dönmeli; uygulamanın Premium kod ve ömür boyu hakları değişmemeli.

**Test B: Yıllık Premium.** **Farklı** lisans test hesabı kullan (aynı abonelik ürününün mevcut `monthly` alıcısıyla çakışmamak için). `yearly` al ve Test A'daki aynı Premium özelliklerini, yeniden açma ve restore'u kontrol et.

**Herhangi biri başarısızsa** üretime açma: kullanıcı ücretlendirilip Premium alamayabilir.

## Testler geçince
- İki planın da gerçek Play test ödemesi + sunucu doğrulaması + uygulama restart/restore testleri geçmeli. En az bir expired/hold erişim kaybı gözden geçirilmeli; mock testler gerçek RTDN yenilemesini temsil etmez.
- Local `MM_PLAY_SUBS_INTERNAL_QA_BUILD` bayrağı bir **Play kanalı kilidi değildir**; yanlışlıkla Production'a yükleme engeli oluşturmaz. İç test AAB'sini ancak kanıtları onayladıktan sonra aşamalı yayınla. Yeni bir release AAB gerekiyorsa `MM_PLAY_SUBS_INTERNAL_QA_BUILD=false`, `MM_PLAY_SUBS_LIVE_QA_COMPLETE=true` yalnız QA geçince kullanılmalı.
- Privacy policy, Families/kids hedef kitle, Data safety, app content, mağaza ekran görüntüleri, signing, versiyon ve Play Console incelemesi de gereklidir. Bu hesap düzeyindeki işlemler GitHub CI ile yapılamaz.

Resmi Google kaynakları:
- https://developer.android.com/google/play/billing/test
- https://support.google.com/googleplay/android-developer/answer/9845334
