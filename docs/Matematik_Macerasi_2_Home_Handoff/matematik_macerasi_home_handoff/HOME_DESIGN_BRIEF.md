# Matematik Macerası 2.0 — Ana Sayfa Onaylı Tasarım Brief'i

## Referansın rolü
`HOME_REFERENCE_APPROVED.png` (1024 × 1536 px) **görsel tasarım referansıdır**; arayüzde tam ekran resim olarak kullanılmayacak. Kaynak görselde metinler, simgeler ve örnek sayılar görselin içine işlenmiş durumdadır. Gerçek uygulamada metin, buton, gezinme, kartlar, istatistikler ve profil verileri **yerel Compose bileşenleri** olacak. Fantastik mavi ada, kaşif karakteri, oyun kartı illüstrasyonları ayrıca parçalı varlıklar olarak sağlanacaktır.

## Proje sınırları
- Tek aktif uygulama projesi: `C:\active_projects\MatematikMacerasiii` (mevcut Play Store Android ürünü). Orijinal oyunları, profil/avatar servisleri, günlük görevleri, ilerlemeyi ve satın alınmış Premium haklarını koru.
- `C:\active_projects\MatematikMacerasiii_v2_vertical_slice` yalnızca teknik KMP referansıdır; buradaki reddedilmiş Enerji Adası tasarımını taşıma veya proje ile birleştirme.
- Yayın, Play Console, release signing, canlı backend, Git push/commit yok. Önce güvenli yerel geri dönüş noktası, sonra debug test.
- iOS nihai hedef: yeni UI bileşenlerini mümkün olduğunca Android-agnostik Compose olarak geliştir; mevcut Android uygulamasını bu sprintte KMP'ye dönüştürme.

## Hedef kullanıcı ve deneyim
- İlk ürün odağı 8–11 yaş; daha sonra 6–14+ yaşa genişletilebilir.
- Dört sabit alt sekme: **Ana Sayfa | Oyunlar | Öğren | Profil**.
- Ana ekrandaki odak: tek eylemle gerçek günlük göreve/oyuna başlama, ardından hızlı oyun erişimi ve anlamlı ilerleme.
- Aşırı bebeksi değil; canlı, kaliteli, cömert boşluklu, farklı cihaz ölçülerinde okunabilir.

## Ekran ve davranışlar
1. **Profil çubuğu:** Seçili mevcut avatar, gerçek kullanıcı adı/sınıfı, `StarManager` üzerinden doğru yıldız bakiyesi. Gerçek olmayan seviye, 245 yıldız veya elmas sayacı ekleme. Avatar/profil tıklaması gerçek profile gitmeli. Günlük görev ikonu mevcut görev listesine açılmalı.
2. **Hero — Bugünkü Görevin:** Mavi fantastik ada fonu + kaşif görseli + ayrı Compose yazıları ve CTA. `DailyTaskManager` içindeki gerçekten oynanabilir görevi göster; örnek ‘Sayıların Gizemini Çöz!’ ancak mevcut görev gerçek Sayı Avı'na karşılık geliyorsa. Görev yoksa anlamlı boş durum göster; asla sahte ödül/progress çizme. CTA oyun yönlendirmesini gerçekleştirsin.
3. **Hızlı Erişim:** Yatay kayan kartlar; Dört İşlem, Balon Patlatma, Sayı Avı, Uzay Macerası (gerekirse tüm oyunlar). Her kart mevcut Activity'yi gerçek Intent ile açmalı. Üç-dört kart bir anda zorla küçültülmeyecek; yatay kaydırılacak.
4. **Senin İçin Önerilen:** Gerçek sınıf/oyun verilerine göre, gerekçesi belirli en fazla 1 oyun + 1 öğrenme önerisi. Öğren içeriği henüz yoksa çalışıyormuş gibi gösterme; önce mevcut gerçek içeriğe bağla veya ‘Hazırlanıyor’ olarak açıkça etiketle. Kişiselleştirme hesaplanmıyorsa ‘önerilen’ olarak sun, ‘sana özel AI’ iddiasında bulunma.
5. **Gelişimim:** Gerçek doğrulanmış yıldız, tamamlanan oyun/görev istatistiklerini göster. Mevcut serviste güvenilir olmayan seri, kupa, haftalık hedef ve elmas sayılarını gizle. ‘Detayları gör’ gerçek istatistik/profil ekranına gitmeli.
6. **Alt navigasyon:** Ekranın altında sabit, sistem navigation insets ile uyumlu. Ana Sayfa => yeni ekran; Oyunlar => mevcut oyunların erişilebilir gerçek liste/akışı; Öğren => yalnızca mevcut işlevsel alan veya açıkça etiketlenmiş hazırlık görünümü; Profil => mevcut ProfileActivity. Boş sayfa veya sahte buton olmasın.

## Görsel kalite ve asset ilkeleri
- Stil: onaylı görseldeki mavi fantastik ada/kale/doğa sahnesi, dost canlısı kaşif, yüksek kaliteli 2.5D/3D hisli illüstrasyon, parlak ama okunabilir UI.
- İllüstratif **katmanlar ayrı**: üst dünya arka planı, transparan kaşif, görev panelinin dekoru, oyun kartı kapakları, gerektiğinde ödül simgeleri.
- Kod ajanı kendiliğinden blok ada, basit Canvas köyü, rastgele emoji, stok fotoğraf veya uyumsuz tasarım üretmeyecek. Eksik görselleri manifestte açıkça işaretleyecek ve ChatGPT'den üretilecek art paketini bekleyecek.
- Kart metinleri, ikon etiketleri ve UI butonları resmin içine gömülmeyecek; lokalizasyon ve erişilebilirlik için Compose'da kalacak.
- Fontlar okunabilir; etkileşim alanları en az 48dp; içerik ekran kaydırmalı, bottom navigation sabit; Android status/navigation bar ve tablet yerleşimi kontrol edilecek. İkinci yazı/başlık görselin üstüne gelişigüzel bindirilmeyecek.

## Sprint Home-1 sınırı
- Önce mevcut home kodu ve servis çağrılarını doğrula, riskli bağımlılıkları ve geri dönüş planını oluştur.
- Ardından mevcut ana uygulamada **HomeScreen'in modüler UI kabuğunu**, dört sekmeli navigasyonun gerçek davranışını, profil/yıldız/görev/hızlı oyun veri bağlantılarını kur. Mevcut oyunların kendi Activity'leri değiştirilmez.
- Henüz teslim edilmeyen illüstrasyonların yerine geçici şematik yapı kullanılabilir, ama **nihai tasarım gibi sunulamaz**; görsel tamamlanma sadece sanat varlıkları entegre edildikten sonra değerlendirilecek.
- Yeni öğrenme motoru, yeni oyun, yeni para birimi, yıldız migrasyonu, Premium Billing refactor, iOS derlemesi, ads stratejisi bu sprintin kapsamı dışında.
- Android debug derlemesi, temel navigasyon ve veri koruma regresyon testleri; Pixel benzeri küçük telefon ve daha geniş telefon/tablet için ekran görüntüsü.
- Her değişen dosya, PASS/FAIL/NOT TESTED, eksik varlık, mevcut uygulamaya etkiler açık raporlanmalı.

## Kabul şartları
1. Google Play'deki orijinal Android uygulamasının package ID ve imzalama kimliği korunur; release yapılmaz.
2. Kullanıcı adı, seçilen avatar, yıldız ve Premium hakları hiçbir sıfırlama olmadan korunur.
3. Dört tab görünür, her biri tanımlı ve dürüst davranış sergiler.
4. Günlük görev ve hızlı oyun kartları gerçek çalışan oyun akışlarına bağlıdır.
5. Şişkin `MainActivity.kt` içine bütün tasarım yığılmaz; bağımsız Composable'lar kullanılır.
6. Mevcut yıldızların iki kaynaktan gelme problemine bu sprintte tehlikeli `max()`-tabanlı migration uygulanmaz.
7. Debug APK derlenir, cihaz/emülatörde gerçek ekran ve tıklama testleri yapılır.
8. Nihai sanat varlıkları yokken 'premium tasarım tamamlandı' denmez.
