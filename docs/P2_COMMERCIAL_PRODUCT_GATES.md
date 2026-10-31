# P2 — Ticari Ürün Kalite Kapıları

> Durum: pilot. Uygulama şu anda yayına hazır yeni bir sürüm olarak onaylanmadı.

## Korunan sistemler
- Mevcut Google Play ürün kimliği `matematik_macerasi_premium`, satın alma ve Premium kod akışları değiştirilmeyecek.
- Eski yıldız, oyun ilerleme, günlük görev, profil ve satın alma anahtarları taşınmayacak/silinmeyecek.
- Sayı Avı 2.0 ve eski oyunlar değiştirilmedi; yeni öğrenme pilotu ayrı `p2_learning_pilot_v1` tercih alanını kullanır.
- Yeni derslerin hiçbirinin ödülü, ücretli hak veya Play Store ürünü değildir.
- Google Play'e yayın, sürüm yükseltme ve production değişikliği kullanıcı onayı gerektirir.

## Bu commit ile gerçekten çalışan kapsam
1. Profilde seçilen sınıfa göre iki derslik, dört sorulu öğrenme pilotu.
2. Soruların tek doğru seçenekli olduğuna dair deterministik birim testi.
3. Gerçek yanlış hamle/ipuçlarından hesaplanan doğru hamle oranı ve tamamlanan dersler için yerel ilerleme.
4. Ana sayfadaki önerilen oyun ve dersin sınıfa göre değişmesi.
5. Eksik konu anlatımlarının **Çok Yakında** olarak gösterilmesi; sahte ilerleme yok.

## Sınırlılıklar (satış iddiasına dönüştürülmemeli)
- 1–4. sınıflar yalnızca iki pilot ders görür; 5–8. sınıflar için içerik açıkça **temel tekrar** olarak işaretlidir.
- Sınıf bazlı öneri, çocuğun başarı verisine göre kişiselleştirme değildir.
- Tek yerel oyuncu profili vardır: bir cihazda birden fazla çocuk için ayrık veriler henüz yoktur.
- Ders doğru hamle oranı, akademik kazanımın kanıtı değildir; dersin yarısında çıkılırsa yalnızca tamamlanmış oturum kaydedilir.
- Görsel/animasyon, erişilebilirlik, ekran boyutu, çevrimdışı davranış, ebeveyn kontrolü ve Google Play aile/politika uygunluğu yayın öncesi ayrıca test gerektirir.
- Buradaki yazılım/ünit testler Premium ürününün Play Console üzerinde geçerli olduğunu veya satın alma/geri yükleme akışının çalıştığını kanıtlamaz.

## Yerel doğrulama kapısı
```powershell
git status
git pull --ff-only origin main
.\gradlew.bat :app:clean :app:testDebugUnitTest :app:assembleDebug
```
Başarılı derleme sonrası şu akışlar gerçek cihazda doğrulanır:
- Kullanıcı adı/sınıf değiştir → ana sayfa önerileri ve Öğrenme Patikam eşleşiyor mu?
- 1/2/3/5. sınıflar ayrı ayrı: ders sorularını tamamla; yanlış dene, ipucu göster, çıkıp yeniden gir; tamamlanma/oturum sayısı tutarlı mı?
- Sayı Avı ve günlük görevler eskisi gibi çalışıyor mu?
- Önceden Premium satın alınmış hesapta Premium statüsü, reklam bastırma, satın alma geri yükleme ve bonuslar korunuyor mu? 
- Mevcut verilerin üzerine güncelleme; kaldırıp yeniden yüklemeyin (yerel veriler kaybolabilir).
- Küçük ekran, büyük font ve Android geri tuşu; profil ve öğrenme akışları takılmadan çalışıyor mu?

## Ticari geçiş için karar kapıları
- Hedef müşteri ayrıştırılmalı: ebeveyn B2C ve kurum B2B için değer/ödeme önerisi ayrı ölçülmeli.
- Ödeme öncesi güven: çocuk kullanım güvenliği, reklam uygunluğu, veri gizliliği, gerçek ebeveyn raporu.
- İlk gerçek kullanıcı pilotlarında ders başlangıç/tamamlama ve tekrar ziyaret oranı ölçülmeli (hedef eşikleri testten sonra belirlenecek).
- Mevcut Premium müşterilerinin haklarını etkileyen her fiyat/paket değişikliği ayrıca incelenmeli; bu sprintte yok.
