# P3 — Premium Öğren Arayüzü | Uygulama + kabul yol haritası

**Hedef:** Öğren kitaplığı gerçek oynanabilir kısa dersleri, kazanım ilişkisini ve Free/Premium erişimini bozmadan çocukların matematiği keşfettiği görsel bir arayüze dönüşsün. Tam MEB müfredatı veya yapay başarı iddiaları yok.

## P3.0 — Görsel kitaplık ve konu keşfi (kod)

- [x] Altı matematik alanı için özgün WebP kapak atlası: Sayılar, İşlemler, Geometri, Veri, Cebir, Temel Tekrar. Yaklaşık 16 KB, sıfır ücretli görsel API; Python/Pillow ile deterministik üretim.
- [x] İki sütun yalnızca yeterli genişlikte; dar cihazlarda tek sütun. Büyük konu kapakları, başlık, kısa açıklama, gerçek oynanabilir ders ilerlemesi ve Premium etiketi.
- [x] Konu alanına göre yatay filtreler; arama + bitirmediklerim filtresi ile bir arada.
- [x] Detayda aynı görsel, mevcut kazanım ve gerçek kısa ders kayıtları.
- [x] Erişim kararı, mevcut LearningAccessPolicy ve Play/Cloudflare hak kontrolü üzerinden. Kilitli konu detayı açık, ders erişimi yine merkezi kontrolle kapalı.
- [ ] Fiziksel cihaz 320/360/412/600dp, büyük font, TalkBack ve kontrast kontrolü.

## P3.1 — Etkileşimli matematik (kod)

- [x] İlkokul Sayılar/İşlemler: 0–10 sayı doğrusu, doğru 1 birimlik hareketi 280ms adım animasyonuyla gösterir. 0/10 sınırında uygun kontrol pasif.
- [x] İlkokul Geometri: üçgenin 3, dikdörtgenin 4 kenar/köşesini karşılaştıran gerçek çizimler ve en az 48dp buton.
- [x] Önceki derslerin dokunmatik kesir modelleri, geometri/alan/saat grafikleri, üst sınıf parametreli Canvas çizimleri korunur.
- [x] Keşif önizlemesi bir test değildir; ilerleme veya Premium hak yazmaz.
- [ ] Gerçek cihazda matematik doğruluğu ve erişilebilirlik kabulü.

## P3.2 — İleri sınıf derinliği: kod, pedagojik sınırlar ve cihaz kabulü

**Kod kapsamı (tamamlandı; cihaz onayından ayrı):**

- [x] 5–8. sınıflardaki tüm mevcut 30 konu kartı, kendi gerçek konu kimliğine göre 13 farklı matematiksel keşif türünden birine bağlandı. İlgisiz alanlara genel/yanlış model gösterilmiyor.
- [x] Sınıf 5: negatiflere geçmeden doğal sayı doğrusu; çeyrek kesir-ondalık eşdeğerliği; açı; alan; sütun grafiği, olasılık; eş gruplar.
- [x] Sınıf 6: işaretli tam sayı doğrusu; kesir; cebirde y=2x+3 ilişkisi; açı/alan; veri.
- [x] Sınıf 7: yarım birimli işaretli rasyonel sayı doğrusu; 2:3 eşdeğer oranlar; y eksenine yansıma; doğrusal ilişki; veri/olasılık.
- [x] Sınıf 8: çarpanlar (12/18/24/30), kare sayılar ve karekök, doğrusal ilişki, yansıma, katlı birim-küp hacmi, sütun grafiği.
- [x] Bütün keşiflerde Önceki/Sonraki kontrolü en az 52dp; ilk/son örnek sınırları kontrol edilir. Sayı doğrusu hareketi kısa ve matematiksel olarak anlamlıdır. Renk ve metin aynı gerçek miktarı gösterir.
- [x] Salt Kotlin matematik modeli için konu eşleşmesi, sınırlar, oran/kesir, faktörler, yansıma, açı, hacim ve doğrusal ilişkiler JUnit regresyon testleri yazıldı.
- [x] Android Compose UI test kaynakları eklendi: 5. sınıf doğal sayılar, 6. sınıf cebir, 7. sınıf yarım birimli rasyonel doğrusu ve 8. sınıf kare-karekök. Bu testlerin yalnız **derlenmesi**, gerçek cihazda çalıştırıldıkları anlamına gelmez.
- [x] Mevcut ders, soru, çözüm, animasyon, kayıt kimlikleri ve Free/Premium erişim politikası değiştirilmedi. Keşif örnekleri dersin yerine geçmez ve puan kaydetmez.

**Dış ortam gerektiren doğrulamalar (tamamlanmadı):**

- [ ] 320/360/412/600dp, 1x/1.3x/1.6x yazı ölçeği; metin taşması, ekran rotasyonu, kaydırma, etkileşimli kontrol; cihaz/emülatör üzerinde görsel QA
- [ ] TalkBack gerçek cihaz sırası, tüm çizimlerin sözlü açıklamasının anlaşılması, kontrast ve motor beceriye uygun dokunma hedefleri
- [ ] Uygulamada her sınıftan birden fazla gerçek dersin açıklama, yanlış seçenek ve pedagojik doğruluğunun öğretmen/alan uzmanı tarafından değerlendirilmesi. Sadece kod matematik testleri bu onayın yerine geçmez
- [ ] Eski Play/Cloudflare Premium sahipleri, yeni abonelikler, geri yükleme, uygulama yeniden açma, tekrar zamanları ve oyun yönlendirmelerinin **gerçek test cihazı ve Play lisans test hesaplarında** doğrulanması

### P3.2 fiziksel QA matrisi

| Sınıf/konu | Beklenen kanıt |
|---|---|
| 5 / Doğal Sayılar | 0–8, negatif işareti yok; ilk/son sınır |
| 5 / Kesirler | 1/4→4/4; 0,25→1,00 eşdeğer |
| 5–6 / Geometri | 30°, 60°, 90°, 120°, 150°; dar/dik/geniş ayrımı |
| 6 / Sayılar | −4…4; sıfırın iki yanında aynı aralık |
| 6 / Cebir | x=−2 için y=−1; x=−1 için y=1 |
| 7 / Rasyonel sayılar | −2, −3/2, −1, …, 2; 1/2 aralık |
| 7 / Oran | 2:3 → 8:12; her durumda 3a=2b |
| 7–8 / Yansıma | (x,2)→(−x,2); x=0 üzerinde nokta değişmiyor |
| 8 / Çarpanlar | 12,18,24,30'un yalnız kalansız bölenleri |
| 8 / Üslü ve köklü | n² ve √(n²) doğru; 1..4 kare modeli |
| 8 / Hacim | Her katta 2×3 küp; 1–4 kat = 6–24 birim³ |
| 5–8 / Veri, olasılık | Sütun yüksekliği değerle orantılı; kırmızı+n-mavi=6 |
| Free / Premium | Keşif önizlemesi izinli; kilitli ders merkezi Premium kapısından geçiyor |

### Komutlar (Windows, mevcut main ve standart APK)

```powershell
cd C:\active_projects\MatematikMacerasiii
git pull --ff-only origin main
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest
```

Standart debug APK: `app\build\outputs\apk\debug\app-debug.apk`. İkinci APK klasörü/yolu oluşturulmaz.

**Yalnız ayrı test cihazında/emülatörde, mevcut Play verisini riske atmadan** Compose UI testlerini çalıştırmak için:

```powershell
.\gradlew.bat :app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.yigit033.matematikmacarasi.ui.screens.home.UpperGradeConceptExplorerAndroidTest"
```

Cihaz testlerinin sonuçları kaydedilmeden P3.2 için "uçtan uca gerçek cihaz QA tamamlandı" yazılmamalı.

## Yayın kilidi

Android oyun mantığı, SKU, ders ID, veriler, FireBase/D1 veya versionCode değiştirilmez. Otomatik Gradle testinin geçmesi fiziksel cihaz doğrulaması değildir. Üretilmiş stüdyo MP3 ses paketi bu değişiklik kapsamında yok.

**Telefon test senaryoları:** 1. sınıf sayı doğrusunda 0–10; 2. sınıf üçgen/dikdörtgen; 4. sınıf ders içi kesir boyama; 6. ve 8. sınıfta üst sınıf konu kartları; alan filtreleri+arama+bitirmediklerim ve sıfırlama; Free/Premium kilitli/açık ders; geri dönüşte ilerleme ve tekrar kayıtları.
