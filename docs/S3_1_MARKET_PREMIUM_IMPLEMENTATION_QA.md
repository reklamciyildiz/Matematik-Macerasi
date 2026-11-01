# S3.1 — Pazar Simülatörü 2.0 | Ürün teslimatı ve telefon kabul listesi

Tarih: 2026-11-01 · Android Kotlin/Jetpack Compose · Branch: main
Durum: **Kod ve kaynaklar repoda; gerçek Android build/cihaz kabulü bu oturumda yapılmadı.**

## Ürün kararları

- Ana menüde ve günlük görevlerde **mevcut** `MarketSimulatorActivity` açılır. Başka bir oyun kartı yaratılmaz.
- Eski 651 satırlık oyun Activity'sindeki açılır öğretici, her üründen sonra otomatik sepet penceresi, zorlayıcı zamanlayıcı ve oyun sonunda agresif reklam akışı bu ekranda kullanılmaz.
- Para hesaplarında `Double` yerine **Int kuruş** kullanılır; küsuratlı Ustalık fiyatları yuvarlama hatasına yol açmaz.
- İlk hedef oturum **3 alışveriş durağı × 3 eğitimsel karar = 9 karar**. Çocuklar zaman sınırı olmadan hesap yapar.
- Üç durak: Güneşli Manav (meyve), Mahalle Fırını (ekmek/atıştırmalık), Oyuncak Atölyesi (eşya/oyuncak). Hedef ürünler, miktarlar, fiyatlar ve yanlış şıklar tekrarda seed ile değişir.
- Alışveriş: ihtiyaç listesine **tam miktarda** ürün eklenir. Liste dışı veya fazla ürünün kasadan geçişine izin verilmez; bütçe sınırı da kontrol edilir.
- Toplam: kasadaki sepet tutarı sorusu sırasında üstteki tutar gizlenir. Çocuk fiyat×miktar işlemleriyle toplamı hesaplar.
- Para üstü: kasiyere verilen tutar, seviyeye göre 50/100/200 TL katına yuvarlanan gerçekçi bir ödeme olarak belirlenir; alışveriş toplamı çıkarılarak para üstü hesaplanır. Doğru/yanlış şıklar doğrulanır.
- Temel/Gelişen/Ustalık: fiyatlar, bazı miktarlar, ondalıklı kuruş, alışveriş listesi büyüklüğü uyarlanır. Sınıf seçimi otomatik ilk öneridir; üç seviye elle de seçilebilir.
- Ölçülen bağımsız başarı: alışveriş listesi, toplam hesaplama ve para üstü olmak üzere 9 karar. Yanlış cevap ve kullanılan ipucu ile çözülen görev bağımsız başarıya yazılmaz.
- Oyuncu kısmen doldurduğu sepeti ve mevcut aşamayı kapat-aç sonrasında geri alır. `market_adventure_premium_v1` yeni depolama alanıdır; eski yıldızlar ve legacy MarketLevels kayıtları değiştirilmez.
- Tamamlama için `market_adventure_first_completion_v1` anahtarında ilk-denemede bağımsız doğru kararlara göre **1–3 yıldız high-water mark** kullanılır. İkinci tur daha iyiyse yalnız fark verilir; aynı başarıyı tekrar oynayarak yıldız biriktirmek mümkün değildir. Başka oyunların StarManager anahtarlarına dokunulmaz. Günlük görev mevcut `GameType.MARKET_SIMULATOR` üzerinden çalışır.
- Öğrenme adaptasyonu: `AdventureSkill.MARKET` ayrı ilerleme alanı; geometri/sayı becerilerinden bağımsız.

## Dosya haritası

| Dosya | Amaç |
|---|---|
| `games/interactive/MarketAdventureEngine.kt` | Android bağımsız 3 ziyaret, ürün/miktar, Int kuruş, bütçe, toplam ve para üstü doğrulaması |
| `games/interactive/MarketAdventureStore.kt` | Çevrimdışı checkpoint, sepet, aşama, hata/ipucu, tamamlanma, yıldız idempotensi |
| `games/interactive/MarketAdventureScreen.kt` | Beş ekranlık Compose akışı: giriş, alışveriş, toplam, para üstü, sonuç |
| `games/interactive/MarketSimulatorActivity.kt` | Kanonik Activity; StarManager, DailyTaskManager, adaptif öğrenme ve istatistik köprüsü |
| `res/drawable/market_adventure_hero.xml` | Yazısız/görsel kontrol içermeyen pazar giriş sahnesi |
| `res/drawable/market_adventure_manav.xml` | Manav görseli |
| `res/drawable/market_adventure_firin.xml` | Fırın görseli |
| `res/drawable/market_adventure_toys.xml` | Oyuncak tezgâhı görseli |
| `app/src/test/.../MarketAdventureEngineTest.kt` | Üç kademe × 300 seed, ekonomi kuralları ve tamamlama testleri |
| `scripts/check_market_adventure_assets.py` | CI'da Android vektörlerinin geçerliliğini ve uygulama bağlantılarını kontrol eder |

## Görsel entegrasyonun dürüst durumu

Tek stil görsel örnekler üretildi; ancak ikili WebP dosyalarını GitHub'a taşıyan ara dosya erişimi **container_session_expired** hatası verdi. Yalnız görsel indirtip kullanıcıya elle taşıtmak yerine **dört yerel Android VectorDrawable** aynı repo içinde üretildi ve bağlandı. Kullanıcı telefonunda doğru görsel/oynanış kabulünü yapabilir. Bu sürüm **yüksek detaylı WebP/3D sanat teslimatı olarak tanımlanmıyor**. Gelecekte sahneler WebP ile yükseltilebilir; matematiksel ürün kartları ve interaktif kontroller Compose kalır.

## Derleme

PowerShell:

```powershell
git pull origin main
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

Geometri Kalesi ve ilk üç Sayı Avcıları dünya kayıtları ayrı kalır. Eski market Activity'si yenisiyle değiştirildiği için eski `LevelSelectDialog` ve `MarketGameScreen` bileşenleri artık kanonik girişte kullanılmıyor.

## Cihaz QA (kısa ama eksiksiz)

1. Oyun Dünyası, hızlı erişim, Gerçek Hayat kategorisi, günlük görev: **Pazar Simülatörü** aynı yeni giriş ekranını açmalı.
2. Temel seviyesi: 3 durağın alışveriş listeleri; yanlış/ekstra ürün varsa kasaya geçiş engellenmeli; sepetten azaltınca güncellenmeli.
3. Kasa toplam ekranında **doğru toplamın üstte görünmediğini** kontrol et. Bir yanlış tutar sonra doğru tutar seç; yanlışlar ve bağımsız karar sayısı doğru olmalı.
4. Para üstünde verilen tutar eksi alışveriş tutarı matematiksel olarak doğru olmalı; yanlış cevapla ilerlenmemeli.
5. Gelişen: bazı ürünlerden 2 adet gerekiyor. Ustalık: 3 hedef ürün ve 50 kuruşlu fiyatları dene. Tutarlar doğru toplanmalı.
6. Sepeti kısmen doldur, Android geriyle çık, aç; aynı üründe miktar ve kaldığın aşama korunmalı. Toplam ve para üstü ekranlarında da çık-gir testi yap.
7. 3 durağı bitir ve yeni tur başlat; **aynı başarıda yeni yıldız kazanmamalı, daha iyi başarıda yalnız fark kazanılmalı (en fazla 3)**, eski yıldızlar azalmamalı. Yeni fiyat/listeler gelmeli.
8. İpucu kullan; bağımsız ilk-deneme puanı haksız artmamalı. 320dp küçük ekran, büyük font, TalkBack, offline ve düşük performanslı telefon kontrolleri.
9. Önceki Geometri Kalesi ve Sayı Avcıları bölgelerinin çalışmasına kısa regresyon testi; reklam/ödeme kısmında sürpriz yönlendirme olmamalı.

## Kabul eşiği

Koda eklenmiş JUnit testleri ve CI statik kontrolü gerçek Android debug derlemesinin yerine geçmez. Kullanıcı `BUILD SUCCESSFUL` çıktısını ve telefon kabulünü doğrulamadan `Play Store'a hazır` denmemelidir.

## Sonraki roadmap

S3.1 telefon testleri geçerse ürünün B2C/B2B satılabilirlik eşiği için **S3.2 öğrenci ve ebeveyn ilerleme raporu**, ardından Premium paketleme/politika denetimi önceliklidir. Sürekli yeni oyun yaratmak ana ticari hedef değildir.
