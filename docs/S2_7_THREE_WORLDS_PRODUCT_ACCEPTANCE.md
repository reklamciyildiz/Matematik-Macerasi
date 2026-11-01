# Matematik Macerası 2.0 — Üç Bölge Kabul ve Kapanış Planı

**Güncelleme:** 2026-11-01 · Kumsal Koyu / Gizemli Orman / Antik Tapınak

## Ürün durumu — açık ayrım

| Bölüm | Ana oyun | Kalıcı ilerleme | Adaptif zorluk | Kısa kavram tekrarı | Android kabul |
|---|---|---|---|---|---|
| Kumsal Koyu | Beş görevi olan oyun döngüsü | Mevcut | 3'er sayma için mevcut | Yeni, sonuç ekranından | Bekliyor |
| Gizemli Orman | Dört aşamalı oyun döngüsü | Mevcut | 4'er sayma için mevcut | Yeni, sonuç ekranından | Bekliyor |
| Antik Tapınak | Dört ritüel + görsel oda mekanikleri | Mevcut, ayrı anahtar alanı | Oturum seviyesi | Yeni, sonuç ekranından | Bekliyor |

**Yeni:** `AdventureReviewEngine.kt` ve `AdventureReviewScreen.kt`, üç bölge için ortak üç soruluk isteğe bağlı pekiştirme. Her oturumda yeni deterministik sayılar seçilir. Hatalı işlemin çarpan/grup odağı yerel olarak saklanır; sonraki tekrar aynı kavramı farklı bağlamda sorar. Tekrar oturumları yıldız, Premium, reklam veya ana görev tamamlanmasını değiştirmez. Seviye dışına kaçan zorluk otomatik sınırlandırılır.

**Antik Tapınak iyileştirmesi:** Mozaik ve Denge aşamalarında yalnızca yerleştirme/döndürme yeterli değildir; toplamı doğru bulmak zorunludur. Mozaik tamamlanmadan doğru toplamı gösterecek sayaç kaldırıldı.

## Kabul senaryoları (ilk yayın öncesi zorunlu)

1. `git pull origin main` ardından `./gradlew :app:testDebugUnitTest :app:assembleDebug` başarılı olmalı.
2. Kumsal Koyu: 5 görevi baştan sona tamamla; ilk ve tekrar ödülleri aynı verilmemeli; son ekranda `3 Soruluk Tekrar` bulunmalı.
3. Gizemli Orman: 4 aşama, yanlış hamle, ipucu, yarıda çık ve devam, yıldız yalnızca ilk başarıda; son ekranda `3 Soruluk Tekrar`.
4. Antik Tapınak: Mozaik ve Denge'de yanlış toplamı seç, yalnızca doğru toplamla sonraki ritüele geç; Grup Kapısı ve Ayna mekanizmasını tamamla; son ekranda `3 Soruluk Tekrar`.
5. Her bölgenin kısa tekrarında üç soruyu yanlış ve doğru cevaplarla dene. Sonuçta yanlış ve ipucu sayaçları doğru olmalı; aynı bölgeye tekrar girdiğinde yeni sorular ve gerekirse hatalı kavramı yeniden gör.
6. Uygulamayı her bölümün ortasında kapat/aç; ana oyun checkpointleri kaybolmamalı. Kısa pekiştirme oturumu ana checkpoint'i değiştirmemeli.
7. İnternet kapalıyken oyun, ana kayıt ve kısa tekrar çalışmalı.
8. 320–360 dp küçük Android telefon ve büyük telefon/tablet: seçenekler taşmamalı; büyük yazı boyutunda erişim kontrol edilmeli.
9. TalkBack, Android geri tuşu, yön değiştirme, animasyon azaltma ve fiziksel performans testleri.
10. Üç bölümün sonunda ebeveyn/öğretmen raporlarında yalnızca ölçülmüş beceriler görüntülensin; `kazanım kazanıldı` gibi kanıtsız ifadeler kullanılmasın.

## Kapanış eşikleri

- **Teknik:** Android derlemesi başarılı, crash yok, geriye dönük kayıt kaybı yok.
- **Eğitim:** Üç bölümden en az iki çocukla gözlemlenmiş oynanış; ipucu sonrası anlama ve yanlışların yeniden denenmesi; resmi yeterlilik iddiası yok.
- **Ticari:** Reklam, Premium ve yıldız davranışı eski sürüme göre bozulmamış; Play Families/politika denetimi ayrı yürütülmüş.
- **UX:** İlk ekranda soru görünürlüğü, dokunmatik alanlar, okunabilirlik ve yeterli bölüm özgünlüğü kullanıcı testinde doğrulanmış.

**Bu dosya kabul planıdır, kabul belgesi değildir.** GitHub'a gönderilmiş kod tek başına bu koşulların geçtiğini göstermez. SDK ve fiziksel cihaz testleri henüz bu oturumda yapılmadı.
