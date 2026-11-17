# Profil 3.0 + Öğrenme Anlatıcısı 2.0 — uygulama ve yayın kontrolü

**16 Kasım 2026 | Android | Matematik Macerası | P2'den sonraki geliştirme**

## Profil 3.0

- Oyun istatistikleri **asla öğrenme sayılarıyla toplanmaz**. Mevcut `GameStatisticsService` aynı dört oyun metriğini beslemeyi sürdürür: ilerleme yıldızları, oyun süresi, biten oyunlar, favori oyun.
- Yeni **Öğrenme İstatistiklerim** paneli, ayrı `LearningAnalyticsStore` kaynağından sınıfa bağlanabilen ders verilerini gösterir: tamamlanan kısa dersler, oturumlar, tekrarı gelenler, görülen en yüksek alıştırma sonucu.
- **Free:** bu temel öğrenme özetlerinin tamamını görür. **Premium:** bunlara ek konu başlıklarına göre ilerleme ve son zorlanılan konuların kısa dökümünü görür. Bu kontrol `LearningAnalyticsPolicy` ile tanımlanır. Eski `AdventureStatisticsPolicy` oyun ayrıntı kilitleri **değişmez**.
- Öğrenme metrikleri oyundaki yıldızları, para birimini, premium durumunu veya başka bir ödülü artırmaz.
- Oturum bitişinde kullanılan gerçek sınıf bilgisi `grade` ve oturum `seed` ile yeni, cihaz-yerel `p3_learning_analytics_v1` verisine kaydedilir. Aynı oturum iki kere yazılırsa yeniden sayılmaz.
- Güncellemeden önceki `g2_*`, `g3_*` gibi **sınıfı benzersiz ders kimlikleri** eski `p2_learning_pilot_v1` verisinden gösterilebilir. 1–8. sınıfta ortak kullanılan `count_3`, `add_20` vb. **eski** kayıtlar geriye dönük hangi sınıfa ait olduğu bilinmediğinden herhangi bir sınıfın istatistiğine atfedilmez. Arayüz durumu dürüstçe açıklar.
- Bu nedenle güncellemeden sonra bazı eski sınıfların yeni raporunda Öğren sekmesinin eski toplamlarından düşük sonuç görülebilir. **Veriler silinmiş değildir**; uydurma sınıf dağılımı yapılmaz. Gelecekteki ders bitişleri gerçek sınıfa yazılır.
- Uygulama içi ders raporu pedagojik/akademik ustalık belgesi değildir. Gerçek çalışma süresi ölçülmediği için sahte **öğrenme süresi** gösterilmez.

## Öğrenme Anlatıcısı 2.0 — mevcut gerçek durum

Üç parçalı mimari:

1. `TurkishMathSpeech.normalize`: cihaz üzerinde Türkçe matematik okunuşu (örn. `3 × 4 = 12` → `üç çarpı dört eşittir on iki`; `3/8` → `sekizde üç`; `1646` → `bin altı yüz kırk altı`).
2. `PremiumLessonAudio`: yalnızca **gerçekten var olan, incelemeden geçmiş MP3 ses** dosyasını uygulamanın derlenmiş asset'lerinden çalar. Dosya adı kaynak anlatım metninin SHA-256'sıdır: ders metni değişirse otomatik eski kaydı kullanmaz. `MediaPlayer` durdurma/arka plan kapatma ve mevcut ses-müzik denetimlerine bağlıdır. Ağ izni, API anahtarı, çocuk sesi kaydı veya yükleme yoktur.
3. `LessonNarrator`: HD ses paketi ve ilgili MP3 varsa öncelikle bunu, yoksa cihazdaki yüksek kaliteli **çevrimdışı Türkçe TTS**'yi kullanır. Kayıt yokken kullanıcının gerçek olmayan "profesyonel ses" görmesini engeller. Dinamik/kişisel sorular cihaz üzerinde okunur.

**Çok önemli teslim sınırı:** Bu kod değişikliği tek başına **yeni bir insan kalitesinde ses dosyasının APK'da bulunduğu** anlamına GELMEZ. Google Cloud / fal / stüdyo sağlayıcısıyla üretilmiş ve kulağa iyi gelen gerçek MP3'leri henüz bu depoya eklemedik. Bu dosyalar olmadan hâlihazırdaki cihaz TTS motoru duyulacaktır; telaffuz daha düzgündür ama timbre dramatik şekilde değişmiş sayılmaz.

### Ücretsiz ve çevrimdışı profesyonel ses üretimi (sıfır API ücreti)

**Google Cloud Chirp 3 HD üretim betiği ve GitHub ücretli servis iş akışı kaldırıldı.**
Ücretli API/abonelik/faturalandırma **kullanılmamalıdır**.

**Seçilen ücretsiz üretici:** [EMA Lightning](https://github.com/canberk7/ema-lightning).
Kod ve model **Apache-2.0**, ticari kullanıma açık ve yerel CPU/GPU üzerinde çalışır. Üretilen MP3'ler Android'e gömülür; kullanıcı cihazında öğrenme sesi için sunucuya istek gönderilmez. **Ayrı stüdyo ses kaydı mevcut değilse bu motorun mutlaka daha doğal geleceği garanti edilmez; gerçek dinleme değerlendirmesi zorunludur.**

1. Windows bilgisayarında Python 3.11–3.13, açık kaynak FFmpeg ve `python -m pip install ema-lightning` ile ücretsiz üreticiyi kur. Model ilk çalıştırmada ücretsiz açık ağırlıkları indirebilir; sonraki sentez yerel olur.
2. Proje kökünde PowerShell: `$env:MM_NARRATION_EXPORT="1"; .\gradlew.bat :app:testDebugUnitTest --tests "com.yigit033.matematikmacarasi.learning.LessonVoiceManifestTest"; Remove-Item Env:MM_NARRATION_EXPORT`. Bu işlem yalnız uygulamada yazılmış sabit ders metinlerini `app/build/narration/manifest.tsv` dosyasına çıkarır.
3. `python tools/narration/synthesize_ema_offline.py --dry-run` ile manifest'i ve ses adedi verisini kontrol et.
4. `python tools/narration/synthesize_ema_offline.py --limit 8` komutuyla **yalnız sekiz dinleme örneği** üret; matematik okunuşlarını, çocuklara uygunluğu, telaffuz ve kalitesini değerlendir.
5. Beğenildiyse `python tools/narration/synthesize_ema_offline.py --limit 0` ile tüm sabit anlatımları yerel bilgisayarda üret; APK boyutunu ve lisans bildirimini incele. Üretimi otomatik tetikleme; satın alma, kredi kartı veya bulut hesabı gerekmez.
6. Önceden eklenen `PremiumLessonAudio` asset path'i bu MP3'lerle uyumlu: `app/src/main/assets/lesson_narration/tr-TR/`. **Gerçek MP3'ler bu commit ile eklenmedi; kullanıcı şu anda önceki Android ses motorunu duyacaktır.**

Sıfır ücretli API çağrısı garanti edilmesi, tüm geliştirme faaliyetlerinin sıfır kaynak maliyeti olduğu anlamına gelmez (kişisel bilgisayar elektrik tüketimi, cihaz, indirme interneti ve hesabın GitHub Actions kota koşulları ayrı konudur). Bu yol **ses üretiminde ayrı faturalandırma kullanmaz**.

Seslerin üretildiğini veya premium ses kalitesinin doğrulandığını söylemeden önce gerçekten dinle.

### Son QA kapıları

- [x] Yeni P3 kazanım/oturum verisinin sınıf bağlamıyla tutulmasına yönelik kod
- [x] Eski ortak ders kimliklerine ait geçmişin sınıflara uydurularak dağıtılmaması
- [x] Free/Premium öğrenme konu ayrıntılarının ayrı politika ile korunması
- [x] Yeni Türkçe matematik konuşma testleri, stüdyo kayıt çalma ve çevrimdışı TTS fallback kodu
- [x] Ücretsiz, Apache-2.0 açık kaynak modeli kullanarak yerel MP3 üretim aracı; ücretli Cloud betiği kaldırıldı
- [ ] Yeni branch için GitHub Android build + otomatik testler başarılı (ayrı doğrulanmalı)
- [ ] Ücretsiz EMA Lightning ile gerçek MP3 paketinin dinlenerek onaylanması ve depoya eklenmesi
- [ ] Son cihazda Free/Premium özet-derinlik ve Play/Cloudflare lisans regresyonu
- [ ] Sınıf değiştirince doğru sınıfın raporlanması; eski ortak ders kayıtları dürüst uyarısı
- [ ] USB/BT kulaklık, ekran kapalı, müzik ducking, TTS yokken ve ses ayarı kapalıyken test
- [ ] Sayı doğrusu, kesir, yüzde, işlem önceliği ve Türkçe ses uyumluluğu dinleme QA
- [ ] 320dp ve büyük fontla panel taşması, ekran okuyucu testi
- [ ] Google Play üretim sürümü AAB testleri

**Yayın:** Kod PR'ının yeşil olması, yüksek kaliteli seslerin tamamlandığı veya üretime çıkılabileceği anlamına gelmez.
