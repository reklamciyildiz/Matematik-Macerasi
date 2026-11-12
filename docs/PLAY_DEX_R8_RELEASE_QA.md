# Play Console DEX uyarısı — Matematik Macerası (Kasım 2026)

## Uyarının gerçek anlamı

Play Console **1.3.2 (20)** için **Kod karartma %0** gösteriyorsa
bu sürümün üretim derlemesinde R8 kapalı olduğu anlamına gelebilir.
Bu abonelik, Premium kodu veya Play satın alma ACK hatası değildir.

Google'ın **Şubat 2027** hedefi, DEX büyüklüğü eşiklerini aşan
uygulamalar için üç ayrı ölçüttür: obfuscation, shrinking,
optimization (her biri >= %25). Oyunlar için toplam DEX boyutu
eşiği 50 MB, diğer uygulamalar için 10 MB. Play Console'daki
**App bundle explorer** gerçek AAB için ölçümü gösterir.
R8 mapping.txt olması üç oranın da >= %25 olduğunu KANITLAMAZ.

Resmi kaynaklar:
- https://support.google.com/googleplay/android-developer/answer/17492799
- https://developer.android.com/google/play/vitals/code-optimization

## Neden varsayılan release R8'i otomatik açmadık?

Premium satın alma (Billing 8), Play Integrity, Cloudflare imzalı lisans,
Firebase, Retrofit/Gson, farklı oyun/Compose ekranları mevcut.
Optimize edilmiş APK derlenebilir ama runtime'da kod silinmiş olabilir.
Özellikle Gson 2.10.1 ve Retrofit 2.9.0 için dar reflection
koruma kuralları eklendi. Tüm pakete `-keep` eklemek %25 hedefini
bozabileceğinden yasaklandı.

Üretimde R8 hâlâ yalnızca açık opt-in ile etkinleşir:
`-PMM_R8_RELEASE_ENABLED=true`. Böylece daha önce onaylı ve
çalışan production derlemesi sürpriz şekilde bozulmaz.

## Tek komutla optimized AAB ve APK (yalnızca Play dahili test!)

Önce mevcut geliştirici Gradle güvenlik ayarları ve upload keystore
yerel makinede hazır olmalı. Bunları GitHub'a koyma.

```powershell
cd C:\active_projects\MatematikMacerasiii
git pull --ff-only origin main
powershell -ExecutionPolicy Bypass -File .\scripts\build-optimized-play-qa.ps1 -ForInternalTesting
```

Komut debug birim testlerini, R8 **release** APK/AAB'yi derler ve
`mapping.txt` içinde en az bir uygulama sınıfının karartıldığını kontrol eder.
Son çıktı: `R8_OPTIMIZED_AAB_READY_FOR_INTERNAL_TEST`.

Çıktı konumları:
- `app/build/outputs/bundle/release/app-release.aab` — Play'e yüklenecek imzalı release
- `app/build/outputs/apk/release/app-release.apk` — lokal QA (Play Store purchase & integrity için uygun test değildir)
- `app/build/outputs/mapping/release/mapping.txt` — çökme retrace için gizli sakla

**Dikkat:** `./gradlew bundleRelease` komutunu R8 bayrağı olmadan
tekrar çalıştırırsan AAB optimizasyonsuz olarak üzerine yazılabilir.
Yalnızca opt-in script ile son oluşturduğun dosyayı yükle.

## Gerçek cihaz doğrulaması

Google Play dahili test kanalından **Play imzalı** sürümü yükle:
1. Ana ekran, profil, ayarlar, avatar seçimi ve en az iki farklı oyun.
2. Öğren dersleri: 1. sınıf ve 8. sınıf görsel/soru akışları.
3. Cloudflare Premium kodu aktivasyonu ve daha önce satın alınmış
   ömür boyu Premium hakkının geri yüklenmesi (var olan hesaplarla).
4. Free test hesaplarında Google Play Monthly/Yearly test kartı:
   fiyatın gelmesi, ödeme, ACK, Premium aktivasyon, yeniden başlatma,
   5/30 dakika yenileme/sona erme.
5. Çevrimdışı/yavaş internet koşulunda Premium hakkı ve geri yükleme.
6. Açılış, arka plandan dönüş, eski profillerin ve oyun kayıtlarının
   sorunsuz olması.

Bu testler tamamlanmadan optimized build'i herkese yayma.

## CI denetimi

`.github/workflows/android-r8-qa.yml` çalışır ve R8 altında abonelik
ve kod yollarını CI için sahte backend yapılandırmasıyla derler.
Bu QA APK **yayınlanamaz**. `mapping.txt` gerçek kod karartma
olduğunu doğrular. Ayrı `android-debug-ci.yml` Kotlin compiler
heap sorunları için büyük Compose ekranını alt fonksiyonlara ayırarak
derleme testlerini çalıştırır.

**Son aşama:** Play bundle explorer üzerinden yeni sürümün üç DEX
optimizasyon puanını oku. Eski 1.3.2 (20) uyarısı kendiliğinden
düzelmez; yeni optimize AAB raporuyla değişmelidir.
