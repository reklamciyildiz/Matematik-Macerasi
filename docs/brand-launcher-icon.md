# Matematik Macerası — yeni marka ikonu

- Onaylanan 2026-11-11 illüstrasyonundan `drawable-nodpi/ic_brand_mark.webp` kullanılır.
- **Yeni kurulumlar:** `BrandIconActivity` etkin; uygulamanın standart simgesi yeni markadır.
- **Yükseltme:** `IconUpgradeReceiver` mevcut `icon_preferences.selected_icon` seçimini yeniden uygular. Daha önce `default` kaydeden kullanıcı eski klasik ikonda kalır; boş tercih yeni markayı kullanır.
- Eski klasik (`DefaultIconActivity`), robot ve uzay launcher alias seçenekleri korunur.
- Android 8+ adaptive ikon XML; Android 7-7.1 için bitmap XML; Android 13+ için ayrı tek renkli pusula işareti.
- Simgeleri `Ayarlar → Uygulama İkonunu Değiştir` ekranından değiştir.
- Google Play mağaza simgesi ayrı, **512×512 PNG** olarak Play Console'da ayrıca güncellenmelidir; repodaki launcher değişikliği mağaza simgesini otomatik değiştirmez.

## QA
1. Temiz yüklemede sadece yeni marka simgesi görünsün; uygulama açılabilsin.
2. Yeni→Klasik→Robot→Uzay→Yeni geçişlerini test et; her geçişten sonra simgeden aç.
3. Simgelerden herhangi biri seçiliyken APK yükselt; seçim korunmalı ve uygulama çalışmalı.
4. Launcher önbelleği nedeniyle değişiklik gecikebilir; ana ekrandan kısayolu yeniden sabitleyerek test et.
5. Android 7.0, 8+, Android 13+ temalı ikon ve farklı cihaz launcherlarda simge/maske görünümünü kontrol et.
6. `versionCode 19` Play Console'daki **en yüksek** yüklenmiş sürüm kodundan büyük değilse yerel olarak yükselt.
7. Release AAB oluşturulmadan önce repodaki Cloudflare / Play abonelik QA guard'larını **gerçekten** tamamla; sırf imzalı build üretmek için tamamlandı bayrağı girme.
