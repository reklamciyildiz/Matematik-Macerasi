# S3.0 Geometri Kalesi — Entegrasyon, Asset Haritası ve Telefon Kabulü

Tarih: 2026-11-01 · GitHub main geliştirme teslimatı

## Yeni oyun dosyaları

- `games/geometry/GeometryCastleEngine.kt`: Android bağımsız, deterministik saf Kotlin geometri motoru, 3 zorluk, 4 oda × 3 görev, simetri aynalama ve doğrulama, ilk deneme ölçümü.
- `games/geometry/GeometryCastleProgressStore.kt`: yerel `geometry_castle_v1` checkpoint, odada kaldığı adım, parça seçimi, dokunulan vitray hücreleri, ipucu/yanlış, öğrenci becerileri, tekrar oynama ve yıldız geri kazanma.
- `games/geometry/GeometryCastleScreen.kt`: Compose kale giriş haritası, Şekil Avlusu, Köprü Atölyesi, Ayna Kulesi, Taç Salonu, bölüm ilerlemesi, Piko ve özet.
- `games/geometry/GeometryCastleActivity.kt`: tek kanonik Android giriş, star high-water mark, tamamlanma, günlük görev ve ayrı `AdventureSkill.GEOMETRY` kaydı.
- `app/src/test/java/com/yigit033/matematikmacarasi/games/geometry/GeometryCastleEngineTest.kt`: 300 seed × 3 seviye doğrulaması, 12 görev / koşu, çoktan seçmeli benzersizliği, simetri yansıtma, ilk-deneme ve ödül/ilerleme kapıları, sonraki koşuda zayıf şekle tekrar.

## Doğrudan Android drawable asset'leri

| Dosya | Görev |
|---|---|
| `res/drawable/geometry_castle_hero.xml` | Giriş/harita sahnesi |
| `res/drawable/geometry_castle_courtyard.xml` | Şekil Avlusu |
| `res/drawable/geometry_castle_bridge.xml` | Köprü Atölyesi |
| `res/drawable/geometry_castle_mirror.xml` | Ayna Kulesi |
| `res/drawable/geometry_castle_crown.xml` | Taç Salonu, final |
| `res/drawable/geometry_piko.xml` | Taş-golem rehber Piko |

**2026-11-01 Premium WebP yükseltmesi:** Yukarıdaki vektörler geri dönüş/saklı eski varlık olarak korundu. Oyun artık Android `drawable-nodpi` içine GitHub üzerinden **doğrudan eklenen 7 gerçek WebP** kullanıyor. Bunlar 2.5D sahneler ve Piko'nun şeffaf karakter çizimidir; harici asset kopyalama, manuel yükleme veya kod ajanına görsel üretme görevi yoktur. Matematiksel şekiller ve simetri tahtası, çözümün doğruluğu için Compose vektörü olarak kaldı.

| Yeni WebP | Çözünürlük | Boyut |
|---|---:|---:|
| `geometry_castle_hero_premium.webp` | 720×405 | 35,572 B |
| `geometry_castle_courtyard_premium.webp` | 608×342 | 37,144 B |
| `geometry_castle_bridge_premium.webp` | 608×342 | 33,958 B |
| `geometry_castle_mirror_premium.webp` | 608×342 | 41,492 B |
| `geometry_castle_crown_premium.webp` | 608×342 | 37,086 B |
| `geometry_castle_complete_premium.webp` | 720×405 | 45,012 B |
| `geometry_piko_premium.webp` | 240×360 (alfa) | 13,280 B |

Bu dosyalar topluca yaklaşık 244 KB olup GitHub'daki SHA eşleşmeleri ve WebP imzaları doğrulandı. Telefon üzerindeki görünürlük ve yaşa uygunluk için kullanıcı kabulü hâlâ gereklidir.

## Görünür oyun etkileşimleri

1. **Şekil Avlusu**: Açıklamaya göre 4 seçenekten geometrik şekli bul. Kare ile dikdörtgen farklı silüette. Yanlış cevap raporlanır, doğru cevap sonrası devam.
2. **Köprü Atölyesi**: Hedef silüeti gör, taş seç, yuvaya bas; sadece doğru eşleşmede parça yerleşir. Yuvada doğru şeklin adı yazılmaz.
3. **Ayna Kulesi**: Sol kareli desen sabit, sağ kareli deseni tek tek doldur/geri al. Motor dikey eksene göre indeks tersini doğrular. Temel seviyede tek karo; gelişen ve ustalıkta daha çok karo.
4. **Taç Salonu**: Seçme, inşa ve simetriyi arka arkaya tek finalde uygula.
5. **Sonuç**: 12 görev; bağımsız ilk doğru, hatalar, ipuçları, beceri bazında gerçek görev kanıtı. Tekrar oynama eski yıldızı bir daha vermez. Yeni seed, önceki yanlış seçilen şeklin yeni oyun girişinde tekrar çalışılmasını sağlayabilir.

## Bağlanan eski girişler

- `GamesTabScreen.kt` — Şekiller & Geometri kartı.
- `QuickAccessGamesSection.kt` — hızlı erişim kartı, kale art.
- `HomeScreenNew.kt` — günlük görev derin bağlantısı.
- `MainActivity.kt` — eski kategori ve günlük görev yolları.
- `GameSelectionActivity.kt` — eskiden ayrı coming-soon olan iki şekil kartı yerine tek Geometri Kalesi.
- `AndroidManifest.xml` — güvenli, exported=false GeometryCastleActivity.
- `AdaptiveLearningPolicy.kt` — sayma becerilerini kirletmeyen GEOMETRY skill ID.

## Derleme (Windows PowerShell)

```powershell
git pull origin main
.\gradlew.bat clean :app:testDebugUnitTest :app:assembleDebug
```

Projedeki gerçek `app/google-services.json` ve yerel geliştirme ayarları korunmalıdır. GitHub CI'nin geçici Firebase test fixture'ı **yalnızca CI derlemesi içindir**, gerçek cihaz testine uygun değildir.

## Telefon kabul listesi (manuel)

1. **Katalog**: Ana oyun listesi, hızlı erişim, eski kategori menüsü ve günlük görevden aynı kaleye girilir, `Yapım Aşamasında` çıkmaz.
2. **Giriş**: Piko, kale görseli, 4 oda haritası, Temel/Gelişen/Ustalık seçenekleri ve 'Kaleye Gir' çalışır.
3. **Şekil Avlusu**: Yanlış şekil seç; hata artar, başarı kilidi açılmaz. Doğru şekil seçince görev ilerler.
4. **Köprü**: Hedef silüeti gör, doğru/yanlış taş seçip yuvaya yerleştir; yanlış olan başarı sayılmasın.
5. **Ayna**: 4×4 ve 6×6 desenlerde sağa yansıma doğru; satır/sütun yer değişiklikleri yanlış sayılır. Karolar geri alınabilir.
6. **Taç Salonu**: Üç farklı mekanik ardışık; 12/12 görev sonunda sonuç ve tamamlanmış kale görünür.
7. **Devam**: Odanın ortasında Android geri, uygulamayı kapat/aç ve ekran döndür; seçilen taş ve vitray yerleşimi korunur.
8. **Tekrar**: Yeniden oyna; seed ve sorular değişir, toplam tamamlanma artar, daha önce kazanılan ilk yıldız tekrar verilmez.
9. **Öğrenme**: İpucu aldıktan sonra çözülen görev bağımsız ilk-deneme sayılmaz; yanlış yapılan şekil sonraki turda tekrar gelebilir; sınıf üstü şekiller otomatik dayatılmaz.
10. **Çevrimdışı, TalkBack, 320–360dp küçük cihaz, büyük yazı, tablet, ısınma, dokunma ve animasyon** kontrolleri yapılır. Yanlış öğrenme veya mali/ödül etkisi olmamalı.
11. **Regresyon**: Sayı Avcıları Kumsal/Orman/Tapınak, eski yıldızlar, Premium ve reklam kodlarına dokunulmamıştır; kısa tur regresyon testi yine yapılmalı.

## Kabul statüsü

- **Yapıldı**: GitHub'da kod, eski vektörler, yedi premium WebP, tüm oyun girişleri ve JUnit test dosyaları; statik referans, asset imzası/boyutu, bağlantı ve parantez kontrolü.
- **Bu ortamda yapılamadı**: Son WebP/UX commit'lerinden sonra Android SDK ile Gradle test/assemble, fiziksel cihaz açılışı, görsel piksel kabulü, son kullanıcı testi. Kullanıcı önceki kod tabanında 64 testi ve dört odayı başarıyla denedi, ancak yeni görseller için telefon testi ayrı gereklidir. Bu aşamalar geçmeden `kusursuz` veya `Play Store'a hazır` denmez.
- **Tasarım sınırı**: Başlangıç için dört mekanik ve temel tekrar katmanı geliştirildi; gerçek kullanıcı deneyiminden sonra daha zengin 2.5D art, sürükle-bırak ve farklı çok geçerli kompozisyonlar iyileştirme adayıdır.

## Bir sonraki karar kapısı
Derleme + 4 oda cihaz testleri başarılıysa Geometri Kalesi ilk release candidate kabul edilebilir. Ardından katalog veri bütünlüğü ve diğer legacy oyunların premium yenilenmesine geçilir; kullanıcı onayı olmadan mağazaya yayın yapılmaz.
