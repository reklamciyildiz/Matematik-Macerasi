# Oyun içi otomatik ilerleme — Android kabul kontrolü

**Kural:** Doğru yanıtın başarılı şekilde kaydedilmesinden sonra kısa (~0.9 s, harita arası ~1.15 s) görsel/ses geri bildirimi verilir ve bir sonraki görev/oda kendiliğinden açılır. Kazanımın kendisini doğrulayan "Cevabı Kontrol Et", "Kasaya Git" veya son görev sonrası yeniden oyun başlatma eylemleri bu kapsama girmez.

## Değiştirilen oyunlar
- Geometri Kalesi: doğru şekil, bütün köprü taşları, simetri ve ölçüm bitişinde otomatik sonraki görev/oda; **v1/v2 kaydını silmez**.
- Kesir Kristalleri: 12 adımlı macera.
- Örüntü Ormanı: 30 adım ve dünya sınırları.
- Zaman Kulesi: 25 adım ve beş dünya.
- Balon Macerası: doğru balondan sonraki soru, son balonda sonuçlar.
- Sayı Avı Kumsal Koyu: kabuk kümeleri, sayı tahtası, kasa, hazine rotası ve hazine mührü içindeki üç soru.
- Kısa tekrar: doğru cevap sonrası yeni soruya/sonuç ekranına geçiş.
- Kadim Orman ve Keşif Tapınağı: doğru final hamlesinden sonuç ekranına geçiş.

**Zaten otomatik:** Pazar Simülatörü kasadan sonraki aşamalar, Uzay Matematik meteor akışı, Labirent ilerlemesi, Dört İşlem iç soru akışı ve Kadim Orman ara görevleri. Final sonuç / yeniden oyna / oyun seçimi ekranları kullanıcı kontrolünde bırakılır.

## Fiziksel cihaz regresyonu
1. Doğru cevap: tek kısa onay, sonra *ek dokunma olmadan* sonraki görev. Yanlışta yanlış ekranında kalır; yeni deneme yapılabilir.
2. Geometri Kalesi eski 5/12 köprü kaydını APK güncellemesi üzerinden sürdür; otomatik ilerlemeyi ve 20 görevli yeni oyuna geçişi doğrula.
3. Doğru yanıttan hemen sonra uygulamayı kapat/aç: kaydedilmiş çözümlü görev tek kez ilerlemeli. Ödül/yıldız ikinci kez gelmemeli.
4. Son görevde sonuç ekranını gör; ödül ve istatistikler bir kez kaydedilmeli. Sonuç ekranından otomatik yeni bir oyuna geçmemeli.
5. Uçak modunda, küçük ekranlarda, büyük Android yazı ölçeğinde, üç tuşlu/gesture navigasyonda ve hızlı çift dokunmada doğrula.
6. Depolama hatasında görev silinmemeli; sadece hata durumunda **Yeniden dene** eylemi olmalı. Geçiş yalnız kayıt başarılı olursa yapılmalı.
7. Oyundan başarı geri bildirimi sırasında çıkış: bekleyen timer yeni ekranda ateşlenmemeli.
8. CI `testDebugUnitTest`, `assembleDebug`, `assembleDebugAndroidTest` ve `check_automatic_progression.py` PASS olmalı. CI cihaz üzerinde gerçek dokunuş veya ses kalitesini doğrulamaz.
