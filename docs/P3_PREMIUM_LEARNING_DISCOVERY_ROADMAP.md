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

## P3.2 — Pedagoji ve ileri sınıflar için açık kalite kapısı

- [ ] 5–8. sınıfta ileri sayı doğrusu, açı, cebir, veri ve kesir modellerini gerçek konu içeriğine göre değerlendirme. İlkokul örnekleri ileri sınıfta yanlış kazanım gibi gösterilmiyor.
- [ ] Her sınıf ve matematik alanında örnek derslerde gerçek matematik ve yanlış cevap açıklaması öğretmen kontrolü.
- [ ] 1x/1.3x/1.6x font, 320–600dp, TalkBack, yeniden açma ve animasyon/yanlış seçim akışlarını gerçek cihazda denetleme.
- [ ] Eski kayıtlar, sonraki ders geçişi, Free/Premium, eski Premium kodu, aylık/yıllık abonelik ve oyun yönlendirmelerini gerçek Play test hesabında doğrulama.

## Yayın kilidi

Android oyun mantığı, SKU, ders ID, veriler, FireBase/D1 veya versionCode değiştirilmez. Otomatik Gradle testinin geçmesi fiziksel cihaz doğrulaması değildir. Üretilmiş stüdyo MP3 ses paketi bu değişiklik kapsamında yok.

**Telefon test senaryoları:** 1. sınıf sayı doğrusunda 0–10; 2. sınıf üçgen/dikdörtgen; 4. sınıf ders içi kesir boyama; 6. ve 8. sınıfta üst sınıf konu kartları; alan filtreleri+arama+bitirmediklerim ve sıfırlama; Free/Premium kilitli/açık ders; geri dönüşte ilerleme ve tekrar kayıtları.
