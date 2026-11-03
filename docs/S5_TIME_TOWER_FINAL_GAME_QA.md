# Zaman Kulesi — Saatin Muhafızları | Ürün ve Kalite Belgesi

**Hedef:** Kesir Kristalleri'nden daha uzun, dokunmatik etkileşimi güçlü, 5 farklı mekanikle tamamlanabilir bir zaman oyunu. Mevcut Android evrenine entegre; Play Store'a gönderim yapılmaz.

## Bitmiş oyun kapsamı

- **25 görev / 5 dünya / dünya başına 5 görev.** Yeni oturumlarda farklı saatler, tarihler ve etkinlik sıraları; ayni seed ile çıkıp girince birebir aynı görev.
- **Saat Atölyesi:** Akrep-yelkovan seçimi, gerçek kadran üzerinde parmakla yönlendirme, saat/dakika ± kontrolleri, ÖÖ/ÖS seçimi ve animasyonlu yüz.
- **Gizli Saatler:** Analog saati okuyup dijital karşılığını dokunmatik kontrolle oluşturma; dijital cevap önce verilmez.
- **Zaman Treni:** Kalkış saati + yolculuk süresini trenin ray üzerindeki slider'ı ve artı/eksi kontrolleriyle hesaplama; gerektiğinde ertesi güne taşar.
- **Takvim Bahçesi:** Haftanın günlerine hizalı, gerçek ay uzunluklarını bilen, artık yıl ve yıl sonu geçişini destekleyen takvim; tarih seçimi doğrudan takvim üzerinde.
- **Günün Rotası:** Gün içindeki 3–5 etkinliği başlangıç saatlerine göre tek tek seçerek sıralama, geri alma, kontrol.
- **Süre baskısı yok.** Anlamlı çocuk etkileşimleri cevaptan çok süreci öğretir. Final puanı ilk denemede, yardım almadan doğru çözümü ölçer.
- Fantastik mavi ada estetiği: **182 katmanlı özgün Android vektör sahne**, 24 saat/dakika işaretli analog kadran, dişli ve kristal motifleri, tren ve ışıklı ray çizimleri. Görseller çevrimdışı; harici resim izni, PNG yükleme veya stok lisans gerekmez.
- Mevcut 14 özgün ses efektinden doğru/yanlış, dokunuş, yıldız, bölüm, görev, zafer; uygulama çapındaki ana tema müziği ses ayarlarına uyar.

## Sınıf–içerik sınırları

| Sınıf | İçerik özeti | Zorluk yaklaşımı |
|---|---|---|
| 1 | Tam saat, günün bölümleri, kısa takvim keşfi, sabah–akşam sırası | Üç zorlukta da tam saat; resmi kazanım iddiası yok |
| 2 | Tam, yarım, çeyrek saat; günlük zaman rutinleri | Temel 30 dk; Orta/Zor 15 dk |
| 3 | Analog ve dijital saat; dakika, saat, gün, süre tahmini | Temel 15 dk; Orta/Zor 5 dk |
| 4 | Saat, süre ve takvim problemleri | 15–5 dk; geçerli eski program ayrıca doğrulanacak |
| 5 | Gerçek hayat zaman problemleri, farklı tarihler ve saatler | 5–1 dk, takvim aralığı geniş |
| 6–8 | Süre hesabı, gece yarısı geçişi, 1 dk hassasiyet, 5 etkinlik sırası | Pekiştirme + daha karmaşık uygulama; sınıfın tüm programı değil |

**Resmi MEB ilişkileri:**
- 2. sınıf TYMM: MAT.2.1.9, https://tymm.meb.gov.tr/2/unite/125
- 3. sınıf TYMM: MAT.3.1.12, MAT.3.1.13, MAT.3.1.14, https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/254
- 5. sınıf TYMM: MAT.5.1.2 doğal sayılar ve zaman problemleri, https://tymm.meb.gov.tr/ortaokul-matematik-dersi/unite/447
- 2026–27 TYMM uygulaması: 1,2,3,5,6,7; https://tymm.meb.gov.tr/taslak-cerceve-planlari/temel-egitim
- Öğretmen onayı olmadan 'tam müfredat uyumlu' veya başarı belgesi iddiası yapılmamalı.

## Kayıt, rapor, ekonomi

- Kullanıcı sınıfı, Temel/Orta/Zor ve seed yeni macerada sabitlenir. Her oynanış adımı senkron `SharedPreferences.commit()` ile saklanır. Geçersiz ilerleme yazılmaz.
- Yanlış cevap, ipucu, bağımsız doğru, seri ve dünya bazlı tamamlanan/bağımsız doğru sayıları. Bir sonraki bölüme yalnızca doğru yanıt sonrası geçilebilir.
- `TimeTowerStore.advance()` tüm veri + beceri sayacını tek commit'te yazar; tekrar edilen geçişleri engeller. Çıkıp açma sırasında aynı görev sürer.
- İlk tamamlamada 3 yıldız (sabit `time_tower_first_complete_v1` anahtarı); tekrarlayan bitirmelerde tekrar ücretsiz yıldız verilmez. Bir oyun oturumunun istatistik ve günlük görev bildirimi seed ile bir kez iletilir (uygulamanın istatistik servisinin mevcut garantileri ölçüsünde).
- Tüm Oyunlar/Hızlı Erişim/eski oyun seçimi ve günlük görev yönlendirmelerinde kayıtlıdır.
- Kullanıcı verileri, satın alma, RLS, abonelik veya mevcut oyunların ödül key'leri değiştirilmedi.

## Kalite kapıları
1. `scripts/check_time_tower.py`: 25 görev, beş mekanik, Android yönlendirme, sanat dosyası, kayıt/ödül yüzeyleri
2. JVM JUnit `TimeTowerEngineTest`: sınıf/zorluk ve çok sayıda seed, leap year, gün/ay/yıl, gece yarısı, yanlış/doğru cevap, sıralama
3. Android instrumentation `TimeTowerPersistenceTest`: gerçek cihaz/AVD'de 25 görevin her birinde çık–geri gel, bozuk kayıt reddi, beceri sayaçları, ödül tekrarı
4. GitHub Actions: debug APK, JVM unit, androidTest APK **derlemesi**. Android instrumented testler CI'da cihazda çalıştırılmaz.

**Fiziksel cihaz kabulü:** 320dp / 360dp / tablet; analog saat kadranı tıklama ve sürükleme; ses kapatma; saat 12→1/23→0; 24:00 geçişi; şubat 28–29; ay değişimi; zaman treni slider; takvimde ay gezinmesi; rota iptal/geri al; 25. bölüm ve ilk ödül; arka plan/yeniden açma; uçak modu; art arda hızlı dokunma; TalkBack. Bu kapı tamamlanmadan Play Store'a yayın hazır denmez.

**Kod konumları:**
- `games/time/TimeTowerEngine.kt` — saf Kotlin zaman ve tarih matematiği
- `games/time/TimeTowerStore.kt` — atomik çevrimdışı kayıt ve beceri kanıtı
- `games/time/TimeTowerVisuals.kt` — kadran/kalender/tren/etkinlik UI
- `games/time/TimeTowerScreen.kt` — tüm oyun akışı
- `games/time/TimeTowerActivity.kt` — Android, yıldız, günlük görev, oyun istatistiği
- `res/drawable/time_tower_hero.xml` — 182 katmanlı özgün görsel
