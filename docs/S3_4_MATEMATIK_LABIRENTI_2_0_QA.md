# S3.4 — Matematik Labirenti 2.0 / Kristal Kapıların Sırrı

**Kapsam:** Kotlin / Jetpack Compose ile tek Android oyununun gerçek 2.0 dönüşümü; eski Activity kodu silinmiş ve mevcut `MathMazeGameActivity` giriş noktası korunmuştur. Yeni oyun ikinci bir uygulama veya eski oyunun yanı sıra açılan kopya değildir.

## Yeni oyun deneyimi
- Sekiz temalı bölge: **Güneş Çayırı (toplama)**, **Kristal Dere (çıkarma)**, **Gizem Bahçesi (örüntü)**, **Köprüler Vadisi (çarpma)**, **Işık Mağarası (bölme)**, **Hazine Pazarı (para)**, **Bulut Kalesi (karma)**, **Yıldız Zirvesi (final)**.
- **Deterministik 11×11 tam labirent**: 5×5 oda ağacı DFS ile üretilir; bir kapıya ulaşıldığında başka rota kullanarak onu bypass etmek mümkün değildir. Her kapı başlangıçtan en uzak çıkışa uzanan benzersiz yol üzerindedir.
- Bölgeye göre **3–6 soru kapısı**; sorular matematiksel olarak doğrulanmış dört ayrı sayı seçeneği sunar. Doğru cevaptan sonra kapı açılır; yanlış cevap oyun kaydına geçer, kapıyı açmaz. İpucu isteğe bağlıdır ve her kapı için bir kez sayılır.
- **Temel/Gelişen/Ustalık** zorlukları profilin sınıfına göre önerilir; kullanıcı elle seçebilir. Öğrenme aralığı/kazanım belgesi değildir.
- Kaydedilmiş tam harita seed'i, bölge, kademe, oyuncu konumu, açılan kapılar, yanlış/ipuçları ve tamamlanma v2 tercihlerinde saklanır.
- Ana sayfa, kayıtlı oyun varsa **Kaldığım Yerden Devam Et** gösterir; yeni oyuna başlama yarım kaydın üzerine yazmadan önce onay sorar.
- Kaydırılabilir mobil ekran, 11×11 renkli dokunulabilir harita, yön tuşları, mini pusula ve matematik kapısı seçenekleri; süre sayacı **yok**, zorunlu internet veya reklam **yok**.
- Bölümler mobil ekranda farklı isim, hikâye, simge ve işlem tipiyle sunulur. Arka plan görüntüleri performanslı Compose Canvas çizimleridir; yüksek detaylı WebP görsel paketi eklenmiş olduğu iddia edilmez.

## Mevcut altyapıyla uyumluluk
- Android Manifest, ana ekrandaki oyun kartı ve günlük görev türü: **değişmedi**.
- Free oyuncu için `DailyGameLimitService` ile **günde 5 yeni tur**; mevcut yarım tura dönmek yeni oyun sayılmaz. Premium için sınırsız yeni tur; mevcut `PurchaseManager` ve merkezi yetişkin satın alma kapısı kullanılır.
- Yıldız kazanma `StarManager.addStars("MathMazeGameActivity_${levelIndex}",stars)` ile **eski 0–7 seviye anahtarını** korur; ödül high-water kuralıyla tekrar oynanarak yıldız çoğaltılmaz. 3 yıldız: sıfır yanlış ve sıfır ipucu; 2 yıldız: en fazla iki yanlış ve bir ipucu; diğer tamamlamalar 1 yıldız.
- `GameStatisticsHelper` tamamlanan turu tek defa kaydeder; `GameProgressService` eski oyun kimliğiyle raporlar. İstatistiklerin uygulama dışı Firebase/Play kabulü bu sprintte doğrulanmış sayılmaz.
- Eski 2.896 satırlık Activity yerine yeni yaklaşık 176 satırlık bağlantı Activity'si; `MazeAdventureEngine`, `MazeAdventureStore`, `MazeAdventureScreen` modüler yapıya ayrıldı.
- Eski `MazeGenerator.kt` gibi modeller diğer modüllerle ilişkili olabileceği için henüz silinmedi; **çalışma zamanında MathMazeGameActivity tarafından kullanılmıyor**. Önceki oyunun tamamlanmamış oturumları eski v1 uygulamasında kalıcı saklanmadığı için bu yeni ekrana otomatik aktarılmaz; mevcut yıldız/statistik/günlük haklar korunur.

## Otomatik testler
`MazeAdventureEngineTest.kt`: 8 bölge × 3 kademe × 110 seed = 2.640 tam harita bitiş testi; bir rota hiçbir zaman kapı çözülmeden tamamlanamaz; şıklar farklıdır; yanlış cevabın kapıyı açmaması; ipucunun tek sefer sayılması; adım yönlendirmesi; tamamlanmamış oyunun yıldız vermemesi; kayıt doğrulama ve deterministik tekrar üretimi.

## Fiziksel cihaz kabul planı
1. Android debug APK'yı **üstüne kur**; önceki Premium, avatar, yıldız ve günlük haklar kaybolmamalı.
2. İlk giriş 8 bölgeli haritayı göstermeli. Profil sınıfına göre önerilen kademe ile elle değiştirilen kademe farklı görev üretmeli.
3. Bir kapıya git, yanlış cevap ver → kapı açılmamalı; ipucu al, doğru cevapla geç, diğer kapılara ulaş.
4. Tüm kristalleri çözmeden çıkışa ulaşmak, bitişe geçişi sağlamamalı. Kapılar üzerinde zorunlu rota olduğunu gözlemle.
5. Bir bölümün ortasında Android geri → harita → **Kaldığım Yerden Devam Et**; uygulamayı kapat/aç ve aynı konum, aynı çözülmüş kristaller, aynı soru devam etmeli.
6. Sekiz bölgeyi en az iki farklı kademede dene. Toplama/çıkarma/örüntü/çarpma/bölme/para hesaplarının cevabı geçerli olmalı.
7. Bölüm bitince yıldız ve puan gösterilmeli. Aynı bölümü tekrar bitirdiğinde aynı yıldızlar iki kez eklenmemeli.
8. Free test hesabıyla ilk beş yeni oyundan sonra altıncı yeni oyuna izin verilmemeli, **var olan yarım tura devam izni korunmalı**. Premium kullanıcıda limit görünmemeli.
9. Uçak modunda yeni harita üretimi, mevcut oyundan devam, soru çözme ve yerel kayıtlar çalışmalı; hiçbir reklam açılmamalı.
10. Düşük çözünürlük (320–360dp), büyük yazı, TalkBack, ekran döndürme, Android geri tuşu, çok hızlı çift tıklama ve yavaş cihaz üzerinde görsel / performans kabulü.
11. Oyun tamamlandıktan sonra günlük görev kartı ve oyuncu istatistikleri yalnızca bir kez güncellenmeli. Cihazın tam eski seviyedeki yıldızlarını kıyasla.

## Sonraki ürün kararı
Fiziksel cihaz ve çocuk kullanıcı testi geçmeden yalnızca GitHub CI üzerinden 2.0'ın ticari açıdan kabul edilmiş bir premium oyun olduğu iddia edilmeyecek. Ardından Balon Patlatma veya Uzay Matematik Macerası için ayrı oyun tasarım ve teknik borç değerlendirmesi yapılabilir.

**Play Store üretim sürümü** hâlâ S3.3 yayımlama kapılarından geçmelidir: imzalı release AAB, Play satın alma geri yükleme, gerçek Play Console Families/Data Safety ve ödeme doğrulama gereklilikleri. Bu sprint Play Store'a otomatik deploy yapmaz.
