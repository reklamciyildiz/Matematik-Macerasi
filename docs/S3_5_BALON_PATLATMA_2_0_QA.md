# S3.5 — Balon Patlatma 2.0 / Gökyüzünün Matematik Sırları

Tarih: 1 Kasım 2026 · Kotlin / Jetpack Compose · kanonik Android oyunu

## Oyun tasarımı ve kapsam

- Eski 82 KB'lık zamanlayıcı / reklam / ağ kontrolüne bağlı monolitik Activity kaldırıldı. Manifest, ana ekran kartı ve GameType.BALLOON_POP_GAME aynı kaldı.
- Beş özgün oyun dünyası: Güneş Bahçesi (toplama), Bulut Koyu (çıkarma), Rüzgâr Vadisi (çarpma), Yıldız İskelesi (bölme), Gökkuşağı Zirvesi (karma). Her dünyada 10 soru, Temel/Gelişen/Ustalık.
- Her matematik sorusunda dört benzersiz balon; gerçek `Surface(onClick)` dokunma hedefi; hafif balon yüzme animasyonu, gökyüzü Canvas sahneleri, kontrast HUD, 2×2 balon ızgarası ve 320dp ekranlar için kaydırma.
- Yanlış balon tek deneme kaydeder, daha sonra aynı yanlış balona yeniden basılamaz. Yanlış soru çözülmeden geçilemez. Doğru cevap sonrasında **açıklama + Sonraki Balon**; öğretim metni otomatik ilerlemeyle kaybolmaz.
- İpucu isteğe bağlıdır, soru başına bir kez verilir. Süre baskısı yok, çevrimdışı çalışır, reklam istenmez ve çocuk oyuncu önünde satın alma çağrısı bulunmaz.
- Her yeni seed sabittir: uygulamayı kapatıp açınca aynı soru, şıklar, yanlış seçenekler, açıklama ve bitiş ekranı korunur.
- Performanslı sahne ve balon grafikleri Android Compose/Canvas ile kodda çizilir. **Ayrı yüksek çözünürlüklü bitmap/WebP sanatı eklenmedi**; görsel etki özel gradient, şekil, katman ve animasyona dayanır.

## İlerleme / öğrenme

- `BalloonAdventureEngine.kt`: saf Kotlin doğrulama, çoktan seçmeli soru, yanlış spam önleme, 10 adım, açıklama ve yıldız hesaplama.
- `BalloonAdventureStore.kt`: yeni `balloon_adventure_v2` SharedPreferences; yerel checkpoint, tamamlama, dünya rekorları, hatalar, ipuçları, en iyi seri.
- `BalloonAdventureScreen.kt`: dünya ve kademe seçimleri, durdur/devam et, doğru cevabın açıklaması, sonuç ekranı.
- `BalloonPopGameActivity.kt`: mevcut uygulama giriş noktası, GameStatisticsHelper, GameProgressService, StarManager. Eski yıldız high-water kimlikleri `BalloonPopGameActivity_level_1` ile `_level_5` korunur; yeniden oynayarak aynı yıldız ödülü biriktirilemez.
- 3 yıldız: 10 soruda en az 9 ilk denemede doğru ve 0 ipucu. 2 yıldız: en az 7 ilk denemede doğru. Diğer tamamlamalar: 1 yıldız.
- Bu oyun gerçek eğitim ölçme/değerlendirmesi veya resmi MEB kazanım belgesi değildir; çocuk tek cihaz üzerinde yerel uygulama kayıtlarına göre gelişimini görür.
- Yeni bir Play Billing SKU veya premium abonelik, zorunlu internet, reklam, kişisel veri aktarımı ve bildirim yaratılmadı.

## Otomatik kalite denetimi

- `BalloonAdventureEngineTest.kt`: 5 dünya × 3 kademe × 75 seed = 1.125 tam oyun/11.250 üretilen soru. Benzersiz şıklar, deterministik sonuç, ipucu, hatalı yanıt, yanlış spam engeli, ödül ve state geçerliliği.
- `BalloonAdventureTouchTest.kt`: Compose Android device/emulator üzerinde yanlış/doğru balon dokunma, devam ve dünya/seviye seçimi.
- GitHub CI, birim testleri, debug APK ve Android UI test APK **derlemesini** kontrol eder; cihaz/emülatör üzerinde instrumented testleri çalıştırmaz.

## Telefon kabul listesi

1. Ana sayfa / oyunlar → eski Balon Patlatma kartından 2.0 açılmalı; başka kart yaratılmamalı.
2. Beş dünyayı sırayla seç, Temel/Gelişen/Ustalık aralıklarını kontrol et.
3. Yanlış balona bas → aynı soru + yanlış seçenek işaretli; diğer seçenekler seçilebilir.
4. İpucunu aç → tekrar ipucu puan kazanımı olmamalı. Doğru balon → açıklama → sonraki soru.
5. 10 soru tamamla; 1/2/3 yıldızlar, skor, en iyi seri ve rekorlar doğru kaydedilsin.
6. Oyunun yarısında geri → kaydet → tamamen kapat/aç → kaldığın soruda devam et.
7. Aynı dünyayı tekrar bitir → daha düşük/aynı yıldızlarla ek ödül verme; farklı dünya ayrı yıldız.
8. Uçak modunda aynı oyun çalışsın; reklam, ödeme veya internet uyarısı görünmesin.
9. 320–360dp ekran, büyük font, TalkBack, çift dokunma, ekrandan ayrılma ve ekran döndürme testleri.
10. Öğrenme istatistikleri/daily task aynı bitirme için ikinci kez artmasın; diğer oyunların yıldızları, avatarları ve Premium hakları bozulmasın.

**Önemli:** Uygulamanın Play Store release adayı olması için S3.3 gerçek Play Billing/Families/Data Safety/AAB kapıları ayrıca tamamlanmalıdır. Buradaki CI yalnız Android debug ve JUnit doğrulamasıdır.
