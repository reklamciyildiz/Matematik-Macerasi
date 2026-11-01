# S3.1.3 — Pazar Macerası giriş akışı / telefon UX kabulü

2026-11-01. Mevcut mağaza motoru, Premium hakları, kayıt ve yıldız mimarisi değiştirilmedi.

## Tespit edilen gerçek problemler

1. `MarketAdventureScreen` bileşeni `store.load()` ile açılıp aşamayı doğrudan `run.phase` olarak aldığı için daha önce yarım bırakılmış bir oturumda Pazar Haritası atlanıp manavın ortasına giriliyordu. Kullanıcı başlangıç düğmesini ve Temel/Gelişen/Ustalık seçimini hiç göremiyordu.
2. Başlangıç sayfasında on mağaza, seviyeden ve tam macera başlatmadan **önce** sıralanıyordu; yanlışlıkla önerilen seviyede hızlı oyun başlaması kolaydı.
3. Android durum çubuğu (saat, Wi-Fi, pil) ve üst geri düğmesi aynı alana girebiliyordu. Premium işletme ekranında da bu eksik vardı.
4. Yeni oyun seçilince eski kısmi sepet ilerlemesi `store.begin()` çağrısı ile sessizce üstüne yazılabiliyordu.
5. Premium kullanıcıya hızlı mağaza seçimi yanlışlıkla yalnız `else` dalında kalabiliyordu; Free/Pro ekranları tutarlı değildi.
6. On mağazanın tamamının dikey uzun listesi ana eylemi aşağı itiyordu.

## S3.1.3 ekran sözleşmesi

- **Her yeni Activity açılışı:** önce Pazar Haritası görünür. İçerik sürüm 1, 2 veya 3 olan kaydedilmiş yarım oyunlar, kullanıcının açıkça `Kaldığım Yerden Devam Et` demesiyle açılır.
- Yarım oyun kartında **gerçek** kaydedilmiş seviye, durak ve kısa/tam oyun bilgisi gösterilir. Devam için `MarketAdventureStore.begin()` çağrılmaz, yalnız mevcut ilerleme ekrana getirilir.
- Başlangıç için sıralama: başlık/hero → varsa devam et → zorluk seviyesi → tam macera başlat → Premium işletme veya satın alma seçeneği → tek mağazalık kısa oyun.
- Yeni tam oyun veya kısa oyun başlatılacaksa ve eskisi bitmediyse **onay diyaloğu** gösterilir; iptal mevcut SharedPreferences içeriğine dokunmaz.
- `Gelişen` etiketi iki anlama gelmez: haritada `Seçim: Gelişen`; oynarken `Gelişen · Tam/Hızlı`. Önceki kayıt devam ederse eski seviyesini aynen kullanır.
- Üç başlangıç mağazası kısa oyun olarak hemen gösterilir. `Tüm 10 Mağazayı Göster` komutuyla gerisi açılır, Premium kilitleri mevcut `PurchaseManager` yetkisine göre kalır.
- Oyun içindeki `Pazar Haritası` Android geri düğmesi ve üst düğmeyle çalışır. Bunun için kayıt silinmez; gerçek uygulama çıkışı yalnız haritadaki `Oyunlara Dön` ile olur.
- Android status/navigation alanları için `safeDrawingPadding()` uygulanır; işletme alt ekranındaki sistem çubuğu da korunur.
- Oyundan haritaya dönerken dikey kaydırma ilk konuma alınır. Seviye tercihi cihaz dönüşünde korunur.
- Oyunun matematik, ürün fiyatı, seed, 120 ürün kataloğu, üç katman, yıldız ve Premium yetki kuralları değişmedi.

## Kabul testleri

1. Daha önce 1/10 manav alışverişi yarıda bırakılmış bir Premium telefonda Pazar kartına basınca **harita + Devam Et** görünmeli, manav doğrudan açılmamalı.
2. Devam Et → Gelişen 1/10 manav açılmalı; sepet önceden seçili miktarları aynı göstermeli.
3. Pazar Haritası → Temel seç → Tam Macera: önce değiştirme onayı, **Vazgeç** eski Gelişen oturumunu korumalı; yalnız **Yeni Oyuna Başla** yeni Temel turu kaydetmeli.
4. Satın alma aktif cihazda 10 mağazanın tümü, kendi tezgâhı ve **Tek Mağazalık Hızlı Oyun** görünmeli.
5. İlk ekranda seviyeler ve Tam Macera başlat düğmesi, uzun mağaza listesinin **üstünde** bulunmalı.
6. Durum çubuğundaki saat/batarya, üst geri düğmesiyle üst üste gelmemeli. Alt gezinme çubuğu son satırı kapatmamalı; Android geri tuşu Pazar Haritası'na dönmeli.
7. Aktif turu bitir → özet → haritaya dön → yeni oyun; seviye seçimi korunmalı, tekrar kazançları ve premium kilitler bozulmamalı.
8. İnternetsiz açılış ve Google Play/Premium kod senaryosu yerel testten bağımsız olarak gerçek cihazda tekrar doğrulanmalı.

## Geliştirici

```powershell
git pull origin main
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Ek CI yapısal kontrolü: `scripts/check_market_adventure_assets.py`.

Bu belge fiziksel cihaz testi yapılmış olduğu anlamına gelmez. CI derleme/JUnit geçse bile gerçek Android cihaz ve Premium satın alma akışı ayrı kabul gerektirir.
