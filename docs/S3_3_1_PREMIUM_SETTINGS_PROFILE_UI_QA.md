# S3.3.1 — Premium Ayarlar ve Kaşif Profili

**Tarih:** 1 Kasım 2026. **Kapsam:** mevcut Kotlin/Compose Android uygulaması; yeni oyun, Premium ürünü, Firebase şeması veya yıldız ekonomisi oluşturulmadı.

## Sorun ve kanıt
Telefonda kullanıcı tarafından paylaşılan üç ekran görüntüsü:
- **Satın Alımları Geri Yükle** alanındaki açıklamalar beyaz arka planda beyaz metin olarak görünmüyordu.
- **Free / Premium İçerikleri** alanındaki açıklamalar aynı nedenle tamamen boş bir beyaz panel gibi gözüküyordu.
- Önceki **SettingsCard** tüm içerikleri ikinci bir beyaz kartla sarıyordu, ancak sonraki kartların yeni metinleri `Color.White` ile yazılmıştı: zemin–metin kontrast hatası.
- Aşırı büyük ara boşluklar; pembe/mor/mavi/yeşil rastgele paneller; ana sayfanın mavi adalar stilinden kopukluk.
- Android durum çubuğunun düz beyaz bant olarak kalması; Premium durumu ve aksiyonların zayıf görsel önceliği.

## Uygulanan çözüm
- `screens/SettingsScreen.kt` eski uzun ekran (1294 satır) yerine kompakt, tek tip lacivert/ocean/gold **Ayarlar** tasarımı.
- Tek katman `SettingsSection`: koyu yüzey, tutarlı border, açık kontrastlı başlık/açıklama ve 12–16dp spacing. İkinci iç beyaz kart **yok**.
- En üstte Premium durum özet kartı; sahip kullanıcıya yeniden satın alma önerilmez. Free için yalnız mevcut `PurchaseManager.buyPremium()` yetişkin kapısına yönlendirme.
- **Free/Premium** kıyaslaması net satırlar olarak, kart yüksekliği içeriğe göre; artık kaybolan veya beyaz panelli metin yok.
- **Satın alma geri yükleme**: buton, yükleme/bulundu/bulunamadı/bağlantı hatası metni görünecek; callback `restorePurchases` korunur.
- **Premium kodu**: mevcut `PremiumCodeValidator.validateAndActivateCode` ve `refreshPremiumState` kullanılır; hata durumları kullanıcıya mesaj verir.
- **Ses/müzik**: eski `AudioService` set/get işlevleri doğrudan kullanılır.
- **Bildirimler**: eski `matematik_macerasi_ayarlar` içindeki `notifications_enabled` anahtarı korunur; `NotificationService` üzerinde aç/kapat işlevi sürer. Saat 19.00 bilgi metnidir.
- **Uygulama ikonu**: aynı `IconSelectorActivity` girişi korunur.
- **Sürüm bilgileri**: `AppVersionUtils.getFullVersionInfo`, geliştirici adı korunur.
- `SettingsActivity` ve `ProfileActivity` Android edge-to-edge pencere ve `safeDrawingPadding()` ile durum/alt gezinme alanlarını yönetir. Şeffaf sistem çubukları, koyu panel arka planı ve açık ikon renkleri kullanılır.
- Profil arka planı koyu okyanus; **Kaşif Profilim** üst başlık; avatar/yıldız/Premium rozeti içeren lacivert–altın hero; görünür **Kişiselleştirme ve Ayarlar** eylemleri aynı palette. Kişisel bilgi, sınıf ve istatistikler okunabilir açık kartlarda tutuldu.
- Oyuncu verileri, satın alma kayıtları, günlük görev ilerlemesi, yıldızlar, Premium ürün kimliği **değişmedi**.

## İşlevsel ve görsel QA
1. Premium cihazda Ayarlar ekranına gir; en üstte **Premium Aktif** görünmeli. Geri yükleme ve Free/Premium metinleri artık boş beyaz panel değil, okunaklı koyu kartlarda görünmeli.
2. Sayfa küçük ekranda baştan sona kaydırılmalı; sistem saati, pil ve alttaki gesture bar hiçbir butona binmemeli.
3. İnternet açıkken geri yükleme: bulundu mesajı; çevrimdışı hata mesajı; mevcut Premium hakkı kaybolmamalı.
4. Müzik/ses efektleri anahtarı gerçekten ses servisine yansıyıp ekranı tekrar açınca korunmalı.
5. Bildirim anahtarı ve hatırlatma ayarı korunmalı; Android 13+ cihaz izni kapalı ise metin doğru açıklama sağlamalı.
6. Robot/Uzay/varsayılan ikon seçimi, profil üzerinden navigasyon, geri dönüş ve yeniden açılışta çalışmalı.
7. Premium kodu varsa girişi Free profilden denenmeli; mevcut ürün kimliği/satın alma akışı değişmemeli. Satın alma gerçekten başlatılmadan yetişkin kapısı test edilebilir.
8. Profilde avatar, oyuncu adı, sınıf seçimi, yıldız, istatistikler, Kaydet/Geri akışı kontrol edilmeli.
9. Telefonun ekran boyutu ve sistem yazı boyutu farklı ayarlarda kart içinde metin/anahtar taşması olmamalı. TalkBack etiketleri ayrıca gerçek cihazda sınanmalı.
10. Karanlık/açık sistem teması, ekran döndürme, arka plandan geri dönme ve oyun istatistikleri güncellemeleri test edilmeli.

## Kod kalite kapıları
- `scripts/check_settings_profile_premium_ui.py` UI kontrast/işlev/navigasyon kaynak kontrollerini CI'da çalıştırır.
- Önceki `check_personalization_routes.py`, `check_s3_3_release_preflight.py` ve tüm `app:testDebugUnitTest` / `app:assembleDebug` korunur.
- Bu kaynak kontrolleri **gerçek ekran görüntüsü veya fiziksel cihaz deneyi yerine geçmez**. CI geçse bile kullanıcı telefon kabulü olmadan son UX onayı verilmemeli.

## Derleme
```powershell
git pull origin main
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```
