# S3.1.1 — Pazar Simülatörü 3.0 / Genişletme & Legacy Cleanup

Durum: **GitHub main üzerinde kod + testler eklendi. Android derleme ve cihaz kabulü bekleniyor.**
Tarih: 2026-11-01.

## Ürün / Teknik karar
Mevcut **MarketSimulatorActivity** tek kanonik oyundur. Katalogda veya günlük görevlerde ikinci bir market oyunu oluşturulmadı. Eski BaseGameActivity bağımlı 651 satırlık Activity önceki fazda yenisiyle değiştirilmişti; artık kullanılmayan iki paket ise bu fazda **20 dosya** silinerek kaldırıldı:
- `games/models/market/` — 5 eski model / Double bazlı sepet dosyası
- `ui/screens/market/` — 15 eski ekran, dialog ve UI bileşeni

Yeni sistem yalnızca `MarketAdventureEngine`, `MarketAdventureStore`, `MarketAdventureScreen` ve kanonik `MarketSimulatorActivity` kullanır. Silinen dosyalar Git geçmişinde geri alınabilir. Diğer oyun motorlarının koduna, Premium satın alma entegrasyonuna ve yıldız kayıtlarına dokunulmadı.

## Altı durak, her durakta dört matematik kararı

| Durak | Satın alma | Toplam tutar | Para üstü | Dördüncü görev |
|---|---|---|---|---|
| 1 — Güneşli Manav | Meyve, kg ve miktar | Toplama/çarpma | Çıkarma | kg / gram ve birim fiyat |
| 2 — Mahalle Fırını | Ekmek / atıştırmalık | Toplama | Çıkarma | Litre / mililitre ve süt fiyatı |
| 3 — Oyuncak Atölyesi | Oyuncak seçimi | Bütçe | Para üstü | Harçlık ve tasarruf |
| 4 — Renkli Kırtasiye | Defter, kalem, boya | Çarpma/toplama | Para üstü | Etiket indirimi |
| 5 — Giyim Sokağı | Giyim ürünleri | Tutar hesabı | Para üstü | İki fiyat arasındaki tasarruf |
| 6 — Benim Tezgâhım | Ürün edinme | Tutar hesabı | Para üstü | Satış geliri – maliyet = kâr |

**Toplam 24 doğrulanmış karar / tam tur.** Ürünler, fiyatlar, gerekli miktarlar, doğru ve yanlış seçenekler seed ile üretildiği için tekrarlar değişir. Para **Int kuruş** olarak hesaplanır; floating point kaynaklı küsurat hatası olmaz. Her aşamada yanlış seçenekten sonraki aşamaya ilerleme engellenir; hata ve ipucu ayrı sayılır.

### Grade-aware (heuristik, resmî müfredat yeterliliği iddiası yok)
- **Temel:** 2 kg gibi tam miktar, 2 litre, basit çıkarma, TL olarak belirtilen sabit indirim.
- **Gelişen:** 500 gram/yarım kilo, 3 litre, çoklu ürün ve daha yüksek tutarlar; sabit indirim.
- **Ustalık:** 1 kg 500 gram, 1,5 litre, TL/kuruş, yüzde 10 indirim ve daha yüksek tutarlar.
- Zorluk kullanıcı profilindeki sınıftan önerilir, elle seçilebilir; yalnızca sayıları büyütmek yerine işlem tipi ve miktarlar da değişir.
- 1 kg = 1000 g, 1 L = 1000 mL açıklamaları ve ipuçları öğreticidir.
- Simüle edilen para/alışveriş işlemleridir; **gerçek ödeme, banka hesabı, mağaza ya da satın alma işlemi yoktur.**

## Geriye dönük uyumluluk
- Önceki `market_adventure_premium_v1` kayıtlarında `content_version` yoksa otomatik v1 kabul edilir: 3 durak / 9 karar, aynı seed ve fiyat üretimi.
- Yeni oyun `content_version=2`: 6 durak / 24 karar. Mevcut yarım kayıt silinmez.
- `MarketAdventureEngine.visitCount(run)` ve `decisionCount(run)` skor, bitiş ve doğrulamada sürüme göre hesaplanır.
- Sepet, miktar, aşama, ödenen tutar, hatalar, ipuçları yerel olarak kaydedilir. `BONUS` aşaması yalnız doğru sepet/toplam/para üstü sonrasında açılır.
- Ödül mevcut `market_adventure_first_completion_v1` high-water anahtarında kalır, aynı yıldız tekrar verilmez; daha iyi sonuçta yalnız fark eklenir.
- Eski market kaynakları kaldırılmış olsa da başka oyunların istatistikleri, gelir veya billing akışları değişmemiştir.

## Görsel / arayüz
Altı durak için `drawable/market_adventure_*.xml` sahneleri, büyük dokunmatik ürün kontrolleri, kalıcı sepet/bütçe alanı, dört basamaklı ilerleme, ipucu ve bağımsız başarı özeti. Üç yeni sahne `market_adventure_stationery.xml`, `market_adventure_clothing.xml`, `market_adventure_stall.xml` olarak eklendi. **Bunlar Android VectorDrawable varlıklarıdır; sinematik WebP/3D art üretimi tamamlandı iddiası yoktur.**

## Unit / cihaz kabul
```powershell
git pull origin main
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Telefonda sırasıyla:
1. Oyuna yalnız mevcut Pazar Simülatörü kartından gir; altı durak görünmeli, ikinci market oyunu olmamalı.
2. Önceden yarım kalmış üç duraklı oturum varsa kaldığı yerden aç, bitir; yalnız **yeni tur** başlayınca altı durak gelsin.
3. Her durakta listede istenen adet/kg/litreyi seç; yanlış/ekstra ürün ve bütçe aşımında kasaya gidilmemeli.
4. Kasada toplam, verilen banknot üzerinden para üstü ve dördüncü görevin yanlış/doğru seçeneklerini kontrol et. Yanlışta ilerlememeli.
5. Temel/Gelişen/Ustalık: 500 gram, 1,5 litre, yüzde 10 indirimin yalnız uygun kademelerde görünmesini doğrula.
6. Dördüncü görevlerde kilogram, litre, harçlık, indirim, ucuz tezgâh ve kâr işlemlerinin matematiğini kontrol et.
7. Her aşamada uygulamayı kapat, yeniden aç; sepette ve bonus sorusunda konum kaybolmamalı.
8. Toplam 24 ilk-deneme doğru ile 3 yıldız, hatalarla daha az; daha kötü tekrar eski yıldızı düşürmemeli.
9. Ana ekran, günlük görev, Geometri Kalesi, Sayı Avcıları ve Premium/billing/ads akışlarında regresyon.
10. Ekran döndürme, TalkBack, 320–360 dp ekran, farklı yazı boyutları, çevrimdışı kullanım.

## Doğrulama sınırı
- Saf Kotlin için `MarketAdventureEngineTest` (v1) ve `MarketAdventureExpansionTest` (v2) ekli; yeni test 300 seed × 3 tier, 100 × 3 tier için 24 kararlık tamamlama, yanlış cevap, hint, sahte ödeme, eski kayıt, para hesabı ve seviye ayrımını denetler.
- **Bu oturumda Android SDK/Gradle derlemesi ve fiziksel telefon doğrulanmadı.** Gerçek kabul kullanıcının Gradle sonucu ve cihaz testiyle yapılmalı.
- Tam kapsamlı oyun ekonomisi/serbest fiyat belirleme, stok ve serbest tezgâh yönetimi henüz yapılmadı. Şu anki "Kendi Tezgâhım" simüle edilmiş bir satış/kâr bulmacasıdır; sürekli işletme simülasyonu olarak tanıtılmamalı.
