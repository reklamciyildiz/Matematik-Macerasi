# S3.0.1 — Geometri Kalesi kapsam genişletme ve cihaz kabulü

Tarih: 2026-11-01. Durum: Android cihaz kabulü bekleniyor.

## Üç kademe — gerçek içerik farkı

| Kademe | Kullanılabilir şekiller | Köprüde görev başına taş |
|---|---|---:|
| Temel | Çember, üçgen, kare, dikdörtgen | 1 |
| Gelişen | Temel + beşgen, altıgen | 2 |
| Ustalık | Gelişen + eşkenar dörtgen, paralelkenar, yamuk, sekizgen | 3 |

- Kare, özel bir dikdörtgendir; sınıflandırma sorularında "kare olmayan dikdörtgen" örneği kullanılır. Çember, dolu daireyle karıştırılmamak için **çizgi** olarak çizilir.
- Eşkenar dörtgen dört eşit kenarlı ve **dik açısı bulunmayan bir örnek** olarak; paralelkenar eşit olmayan bitişik kenarlı ve eğik açılı; yamuk ise yalnız bir çift paralel kenarlı **örnek** olarak gösterilir. Matematiksel şekil ailesinin her olası özel alt türü bu ilk kapsamın dışında kalır.
- Şekiller renk ve dönüş yönünden farklı olabilir. Görsel cevaplar şeklin adını etiketlemez; TalkBack içeriği ise erişilebilir kalır.
- Ustalık seviyesinde her yeni 12 görevlik turda sekiz şekil hedef olarak **farklı** seçilir; kalan şekiller başka turlarda veya seçeneklerde görünür. Yeni tura girerken hatalı öğrenilen şekle öncelik tanınır.
- Giriş ve bitişte **Şekil Koleksiyonu**: öğrenilen seviyedeki şekillerin isimleri, vektör görünümü ve ayırt edici özellikleri; aktif soru çözülürken açılmaz.

## Oyun akışı

- Şekil Avlusu: üç özellik tabanlı sınıflandırma görevi.
- Köprü Atölyesi: üç görev, her görevde 1/2/3 ayrı siluet ve **sırayla seç-yerleştir**. Yanlış taş yerleşmez, doğru taş doldurulmuş görünür. Seçilen ve doldurulan parçalar `geometry_castle_v1` içinde kaydedilir.
- Ayna Kulesi: üç simetri görevi, kademe ve seed'e göre artan desen varyantları.
- Taç Salonu: şekil tanıma + çok parçalı köprü bulmacası + simetri; final.
- Her oturum yine 12 görevdir. Yıldız, reklam, Premium ve eski Sayı Avcıları kayıtları bu değişiklikte güncellenmedi.

## Geriye dönük uyumluluk

- Eski oturumlar otomatik `content_version=1` sayılır ve **orijinal altı şekilli soru üretim motoru** üzerinden aynı seed, aynı oda ve aynı şıklarla devam eder.
- `begin()` ile başlayan her yeni oyun `content_version=2` kullanır; eski yıldız sayısı ve tamamlanan keşif sayısı korunur.
- Yerleştirilen taş dizisi ile aktif yuvanın beklediği taşlar eşleşmezse ilerleme geçersizdir; yanlış taş ile oda atlanamaz.
- Kısmi köprü ilerlemesi, ekran döndürme ve uygulamayı tekrar açma sonrasında geri yüklenir.
- Şekil noktaları `GeometryShapeGeometry.kt` altında saf Kotlin ile hesaplanır, Compose Canvas aynı koordinatları çizerek matematiksel/doğruluk tutarlılığı sağlar.

## Terminal (PowerShell)

```powershell
git pull origin main
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

## Telefon QA kontrol listesi

1. Uygulamada önceki yarım Geometri Kalesi oyununu aç: soru/yuvanın çözümü değişmemeli, ilerleme kaybolmamalı.
2. Yeni oyunu **Temel** seviyede başlat: dört temel şekil, bir taşlı köprü; dört odadan final ekranına kadar oynanır.
3. Yeni oyunu **Gelişen** seviyede başlat: beşgen/altıgen; köprüde iki taş, yanlış yerleşim reddedilir.
4. Yeni oyunu **Ustalık** seviyede başlat: eğik dörtgenler ve sekizgen; köprüde üç farklı siluet. Özellikle yamuk/paralelkenar/eşkenar dörtgen çizimlerini ayırt edebil.
5. Köprüde bir taş yerleştir, Android geri tuşuyla çık, yeniden aç; yerleşim ve doğru görev kaydı korunmalı.
6. Şekil Koleksiyonu'na girişten ulaş; dört/altı/on şekil ve doğru tanımlamalar görünür. Oyun ortasında cevap veren rehber açılamaz.
7. Simetri görevleri, önceki yıldızlar, günlük görev, bütün oyunlara dönüş ve çevrimdışı çalışma bozulmamış olmalı.
8. Küçük telefon, erişilebilirlik ve büyük yazı boyutunda şekiller tam görünür ve hedeflere dokunulabilir olmalı.

## Kabul standardı

Kod statik kontrolü ve saf Kotlin JUnit testleri repoya eklenmiştir. **Yeni sürüm için Gradle derlemesi ve gerçek telefon QA henüz burada yürütülemedi.** Bunlar geçmeden Play Store'a hazır veya hatasız olduğu iddia edilmemelidir. Sonraki proje fazı: S3.1 Pazar Simülatörü.
