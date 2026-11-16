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

### HD sesleri doğru üretme akışı (sadece geliştirici tarafı)

Google Cloud'da Text-to-Speech API'yi ve faturalandırmayı etkinleştir; uygun yetkilendirmeyle üretim için bir servis hesabı oluştur. Hesap JSON içeriğinin Base64 değerini yalnız GitHub Actions secrets içine `MM_NARRATION_TTS_SERVICE_ACCOUNT_B64` adıyla koy. JSON anahtarını **asla APK, Gradle veya GitHub commit'ine ekleme**.

1. GitHub Actions → **Build Licensed Turkish HD Narration Pack** → Run workflow.
2. İlk denemede **audition_only=true**; Türkçe Chirp 3 HD sesini seç (başlangıç seçeneği `tr-TR-Chirp3-HD-Kore`). Farklı sesleri çocuk kullanımına uygun doğallık, matematik telaffuzu, sıcaklık ve duraklamalara göre karşılaştır.
3. Workflow, gerçek Kotlin ders metinlerini ve adımlarını manifest'e döker; yalnız sabit, uygulamanın kendisine ait metni sağlayıcıya gönderir. Çocukların yanıtları veya mikrofon verileri asla servisle paylaşılmaz.
4. Test seslerini `turkish-hd-lesson-voice-...` artifact'inden dinle ve onayla. Sonra **audition_only=false** ile tüm paket üretilir.
5. Sesleri `app/src/main/assets/lesson_narration/tr-TR/` içine yerleştir; bunları ayrı bir kod incelemesine sok. Her ses kaydı MP3 olmalı, bozuk/eksik dosya olmamalı, hem 1–2 hem 3–4 hem 5–8 seviyesinde örnekler kulağa incelenmeli.
6. Yerel APK/AAB dosya boyutunu karşılaştır; tek tip sesin tüm 1–8 derslerinde gerçekten kullanıldığını cihazda dinle. Mesaj ve soru içerikleri ses paketi olmadan mevcut çevrimdışı TTS ile çalışır.

Google Cloud resmî belgelerine göre Türkçe Chirp 3 HD sesleri desteklenir ve üretilen dosyalar hizmet koşullarına uyumlu olarak uygulamalarda kullanılabilir. Fakat kalite **dinleme testi yapılmadan garanti edilemez** ve hizmet ücretli kota/faturalandırmaya bağlıdır:

- https://docs.cloud.google.com/text-to-speech/docs/list-voices-and-types
- https://docs.cloud.google.com/text-to-speech/quotas
- https://cloud.google.com/text-to-speech/pricing

### Son QA kapıları

- [x] Yeni P3 kazanım/oturum verisinin sınıf bağlamıyla tutulmasına yönelik kod
- [x] Eski ortak ders kimliklerine ait geçmişin sınıflara uydurularak dağıtılmaması
- [x] Free/Premium öğrenme konu ayrıntılarının ayrı politika ile korunması
- [x] Yeni Türkçe matematik konuşma testleri, stüdyo kayıt çalma ve çevrimdışı TTS fallback kodu
- [x] Lisanslı HD kaydı yalnız sunucu tarafında üretmek için güvenli manifest/CI aracı
- [ ] Yeni branch için GitHub Android build + otomatik testler başarılı (ayrı doğrulanmalı)
- [ ] Gerçek yüksek kaliteli MP3 paketinin onaylanıp depoya eklenmesi
- [ ] Son cihazda Free/Premium özet-derinlik ve Play/Cloudflare lisans regresyonu
- [ ] Sınıf değiştirince doğru sınıfın raporlanması; eski ortak ders kayıtları dürüst uyarısı
- [ ] USB/BT kulaklık, ekran kapalı, müzik ducking, TTS yokken ve ses ayarı kapalıyken test
- [ ] Sayı doğrusu, kesir, yüzde, işlem önceliği ve Türkçe ses uyumluluğu dinleme QA
- [ ] 320dp ve büyük fontla panel taşması, ekran okuyucu testi
- [ ] Google Play üretim sürümü AAB testleri

**Yayın:** Kod PR'ının yeşil olması, yüksek kaliteli seslerin tamamlandığı veya üretime çıkılabileceği anlamına gelmez.
