# S3.1.2 — Pazar Simülatörü 4.0 / Mağaza kataloğu, sanal ekonomi ve Premium erişim

Durum: **GitHub main'e gönderilen uygulama; CI ve fiziksel Android testleriyle kabul edilecek.**
Tarih: 2026-11-01

## Mimari ve tek oyun
- Tek Android giriş noktası `games/interactive/MarketSimulatorActivity.kt`. İkinci market uygulaması, IAP SKU veya abonelik oluşturulmadı.
- Mevcut `PurchaseManager` ve `matematik_macerasi_premium` Google Play ürününden (ayrıca mevcut Premium kod yetkisinden) hak okunur. Yeni yerel Premium boolean'ı oluşturulmadı.
- Free: **3 mağaza**, Pro/Premium: **10 mağaza**; aynı `MarketAdventureScreen` / `MarketAdventureEngine`.
- Yaş odaklı ödeme dönüşümü: kilitli mağazaya dokunulduğunda yetişkin doğrulama ekranı gösterilir; tamamlanınca mevcut `PurchaseManager.buyPremium()` yöntemi açılır. Hiçbir görev tamamlanırken ödeme istenmez. Satın alma tamamlanmış varsayılmaz.
- Yeni ürün kataloğu `MarketPremiumCatalog.kt`: **10 mağaza × 12 ürün = 120 tanımlı ürün**. Her oturumda mağazanın 12 ürününden farklı **6 raf ürünü** seçilir. Gereken alışveriş listesinin büyüklüğü Temel=2, Gelişen=3, Ustalık=4.
- Sınıf/öğrenme kademesi mevcut `AdaptiveLearningStore` üzerinden önerilir, kullanıcı üç kademe arasında seçim yapabilir.
- Miktarlar adet/kg/litre; fiyatlar ondalıksız `Int` kuruş üzerinden tutulur.
- Her mağazada: **tam liste ve miktar, sepet toplamı, para üstü, mağazaya özgü ekstra matematik görevi**. Uzun turda Free=12, Premium=40 karar. Ayrıca tek mağazalık 4 karar **hızlı oyun** seçilebilir.
- Bonus sorular: ağırlık, litre, harçlık, indirim, fiyat karşılaştırması, bütçe, paket/birim fiyat, kafe kombinasyonu ve satış kârı.
- Mağaza görselleri Kotlin/Compose etkileşiminden ayrı Android vektör varlıklarıdır: hero + 10 özgün mağaza sahnesi. Burada yüksek detaylı WebP entegrasyonunun bittiği iddia edilmiyor.

## Premium işletme mini-oyunu
- `MarketStallEngine.kt` / `MarketStallStore.kt` / `MarketStallScreen.kt`.
- Oyuncu hayalî oyun cüzdanından **toptan stok alır**, altı farklı ürün için **3 fiyat seçeneği** arasından müşteriye satış teklif eder; fiyat müşterinin kabul eşiğinden yüksekse satış gerçekleşmez; uygun fiyatta stok ve kasa matematiksel olarak güncellenir.
- **5 tezgâh geliştirme seviyesi**, kapasite artışı, satış adedi ve hesaplanan brüt kâr; ayrı SharedPreferences ve testli kurallar.
- Sanal oyun parasıdır; gerçek paraya, Google Play satın almasına veya StarManager yıldızlarına dönüştürülmez.
- İşletme mini-oyunu yalnız gerçek Premium yetkisi görülen UI'dan açılır.

## Sürüm ve veri uyumluluğu
- `content_version=1`: eski 3 mağaza / 9 karar ve eski seed/soru mantığı.
- `content_version=2`: önceki 6 mağaza / 24 karar ve aynı seed/soru mantığı.
- `content_version=3`: yeni 10 mağaza kataloğu, kullanıcı yetkisine bağlı 3/10 mağaza veya tek mağazalık pratik.
- Aktif turun içeriği ve mağaza erişimi turun başlangıcındaki yetkiyle kaydedilir, sonra değişmez. Premium haksız yenilenmiş gibi gösterilmez; sonraki turda yetki yeniden kontrol edilir.
- Eski üç ve altı mağazalı yarım kayıtlar sıfırlanmaz.
- Hızlı oyunlarda tekrar yıldız biriktirilemez; tam macera high-water ödül anahtarı korunur.
- Günlük görev yalnız `GameStatisticsHelper` aracılığıyla bir kez ilerletilir (aynı Activity'deki ikinci güncelleme kaldırıldı).

## Doğrulama
PowerShell:
```powershell
git pull origin main
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```
GitHub Actions: `.github/workflows/android-debug-ci.yml`. Statik kaynak doğrulama: `scripts/check_market_adventure_assets.py`.

- `MarketPremiumCatalogTest.kt`: ürün kimliği, deterministic ürün rafları (3 kademe × 120 seed), para şıkları, tam Free ve Premium tur bitişleri, kilitli mağaza ve tek mağaza durakları, eski sürüm korunması.
- `MarketStallEngineTest.kt`: stok alımı, tutarlı kasa, müşteri fiyat sınırı, brüt kâr, stok üst sınırı ve yükseltme maliyetleri.
- Önceki `MarketAdventureEngineTest` ve `MarketAdventureExpansionTest` değişmeden çalışmalı.

### Telefon kabulü
1. Mevcut oyun kartına tıklandığında **bir** Pazar Simülatörü açılır; Free'de ilk üç mağaza oynanır ve 7 mağaza kilitli görünür.
2. Premium kodu veya Play satın alması aktif cihazda aynı 10 mağaza açılır; sahte `premium_prefs` dosyası üretmeyin.
3. Hızlı oyun olarak her üç Free mağazasından birini, Premium'da 7 yeni mağazadan birini bitirin. Hızlı oyun yıldızı global bakiye olarak eklememeli.
4. Tam macera: Free 3 × 4=12, Premium 10 × 4=40 karar. Altı raf ürünü ve değişen alışveriş listesi kontrol edilmeli.
5. Fırın/süt litre hesaplaması, manav kg/gram sorusu, kırtasiyede indirim, spor mağazasında bütçe, oyuncakçıda harçlık, tezgâhta kâr.
6. Kasadan doğru sepet olmadan geçilmez; yanlış toplam veya para üstü ilerletmez.
7. Oyundan çıkıp girince aktif mağaza, adım, ürün miktarları aynı kalmalı. Eski v1 ve v2 kayıtları korunmalı.
8. Premium “Benim Tezgâhım”dan stok al, müşteri fiyatı seç, satışı tamamla, dükkânı büyüt; sanal para gerçek IAP/yıldızı değiştirmemeli.
9. İnternetsiz mevcut Premium yetkisinin önbellek davranışı; satın alma ve Premium kod reaktivasyonunda bağlı Play/Firebase doğrulaması.
10. Küçük Android ekran, büyük yazı, ekran döndürme, TalkBack, Play Families reklam/ebeveyn kapısı ve satın alma akışı ayrıca incelenmeli.

## Açık sınırlar
- Google Play Billing ve Premium kodunun gerçek cihazdaki hak yenilemesi yerel JUnit ile ispatlanamaz; Google Play test satın alması/kodu ile QA gerekir.
- Arayüz tamamen çalışan Compose bileşenlerine bağlı olsa da fiziksel çocuk kullanılabilirliği ve pedagojik etki sahada test edilmelidir.
- Mağaza ekonomisi basit, öngörülebilir müşteri talebi ve sanal para kullanır. Gerçek piyasa simülatörü, karmaşık dinamik fiyatlandırma, vergi hesaplaması veya gerçek cüzdan değildir.
- Görseller vektör sahnelerdir, son kalite WebP illüstrasyon paketi ayrı sanat çalışması ister. Bu bir eksikliği açıklar, performans hatası anlamına gelmez.
