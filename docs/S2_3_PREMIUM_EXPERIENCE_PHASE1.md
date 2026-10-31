# S2.3 — Ortak Premium Deneyim Sistemi: Faz 1

## Amaç
Bağımsız küçük oyun kartları yerine bütünlüklü, iki oynanabilir bölgesi bulunan macera atlası oluşturmak. Kumsal Koyu ve Gizemli Orman'da görsel ilerleme ve geri bildirim tutarlılığı sağlamak.

## Yapılanlar
- `SayiAviWelcomeScreen` yeniden tasarlandı: Katmanlı dünya arka planı, kıvrımlı çizilmiş keşif yolu, etkileşimli ada düğümleri, büyük dokunma alanları ve ekrana göre uyarlanan konumlar
- Açık alanlar: Kumsal Koyu ve Gizemli Orman; Antik Tapınak oynanamaz ve sahte buton değildir
- Açık bölgelerde tekrar oynama; tamamlanmış durum görseli mevcut kalıcı kayıttan hesaplanır
- `AdventureUiKit`: Renk token'ları, animasyonlu görev ilerleme çubuğu ve okunabilir olumlu/olumsuz geri bildirim
- Kabuk Kümeleri ve Hazine Mührü, Gizemli Orman ile ortak geri bildirim bileşenini kullanır
- Gizemli Orman'da kütük hücreleri durum değişiminde yumuşak renk geçişi yapar; doğru hamleye isteğe bağlı cihaz haptik geri bildirimi eşlik eder
- Sadece Compose kodları; yeni bağımlılık, görüntü lisansı veya özel izin eklenmedi

## Koruma kuralları
Satın alma/Billing, yıldız kimlikleri, günlük görevler, mevcut SharedPreferences anahtarları ve oyun kuralları değiştirilmedi. Haritadaki Tamamlandı/tekrar açılma durumları eski kayıt depolarından okunur. Varsayılan gerçek kullanıcı verisi silinmez.

## Kabul kontrolü
- Bölge 1 ve 2 açılır, Bölge 3 asla açılmaz.
- Küçük Android ekranda yatay taşma ve yanlış hedefe basma olmaz.
- Ekran okuyucu bölge durumunu duyurur, buton basılabilir alan en az 112 × 112 dp görsel düğüm çevresindedir.
- Farklı metin boyutlarında rota geçişi ve bölge isimleri bozulmaz.
- İlk tamamlamalar, tekrar oynama, yıldızlar ve cihazdan çıkıp geri girme davranışı önceki sürümle aynıdır.
- CI `:app:testDebugUnitTest :app:assembleDebug` geçmelidir; telefon üzerindeki görsel/erişilebilirlik kabul testi ayrı yapılır.

## Sonraki aşama
S2.3 Faz 2: Görsel varlıkları özgün oyun illüstrasyonlarıyla geliştirme, köprü sürükleme etkileşimi için dokunma + erişilebilir alternatif, animasyonların düşük güçlü cihazda kare hızı testi, küçük ekran / TalkBack / font scale denetimi. Bunları cihaz testinden önce tamamlanmış olarak işaretleme.
