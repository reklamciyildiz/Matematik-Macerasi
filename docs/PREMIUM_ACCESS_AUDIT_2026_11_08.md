# Matematik Macerası: Free / Premium kaynak denetimi — 8 Kasım 2026

## Mevcut haklar
- Oyunların tamamının ücretsiz giriş deneyimi var; sınıf koşulları ücret kilidi değildir.
- Dört işlem (toplama/çıkarma/çarpma/bölme) ve Karışık Mod: 5 zorluktan Temel Free; Orta, Zor, Çok Zor, Uzman Premium (4/5).
- Sayı Avı 3.1, Geometri Kalesi, Balon Patlatma, Uzay Macerası: Temel Free, Orta ve Zor Premium (2/3). Kayıtlı ücretli seviyeler korunur.
- Orman Keşfi, Antik Tapınak ve klasik eğitim pekiştirmeleri: Free.
- Kesir Kristalleri, Zaman Kulesi, Örüntü Ormanı: Temel tamamen Free (1/3 zorluk); Orta ve Zor Premium (2/3 zorluk). Kayıtlı ücretli zorluk ilerlemesi hak iptalinde silinmez. Eski Orta/Zor kaydı Premium hakkı olmayan oyuncuyu Temel'den mahrum etmesin diye **ayrı bir Temel kayıt alanı** açılır; kullanıcı duraklatma ekranından Temel'e geçebilir, asıl Premium kayıt üzerinde yazılmaz.
- Öğren: seçilen her sınıfta 2 ayrı alandan kalıcı Free ders; Premium'da 72 farklı / 84 sınıf-ders sunumu. Eski tamamlanmış ders erişimleri korunur.
- Pazar: Free 3 mağaza / 12 karar; Premium 10 mağaza / 40 karar + Kendi Tezgâhım.
- Labirent: Free günde 5 yeni oyun; Premium sınırsız.
- Avatar: ilk 6 yıldızla (1 varsayılan açık), diğer 9 Premium + yıldızla açılır. Robot ve Uzay ikonları Premium.
- Profil öğrenme özeti Free; ayrıntılı oyun istatistikleri Premium.
- Günlük 5–7 görev iki hakta da açık; Premium ek günlük 5 yıldız alır.
- Çocuk güvenlik kontrolü nedeniyle reklamlar Free ve Premium'da kapalı.
- Ömür boyu Google Play ürünü `matematik_macerasi_premium` değişmedi. Geçerli Premium kodları korunur; aylık/yıllık SUBS seçenekleri aynı Premium yetkisini açacak biçimde kodlandı, ancak yayın bayrağı ve güvenilir süre sonu doğrulaması tamamlanmadığından halen **kapalıdır**. Ayrı uygulama içi yetişkin sorusu yoktur.

## Kod düzeltmeleri
- Premium hakkı yokken önceden seçilmiş özel avatar yerine geçici varsayılan avatar gösterilir; açılmış avatarlar ve kayıtlı tercih silinmez. Hakkın geri yüklenmesiyle önceki avatar yeniden görünür.
- Premium Pazar kaydı sahibi hakkını kaybederse kaydı silinmeden korunur; devam etmek yerine Ayarlar'dan geri yükleme ekranına geçilir. Kaydedilen premium oyunu Free ile oynama/değiştirme durdurulur. v2 kayıtları da kontrol edilir.
- Pazar premium kontrolü geçerli `PurchaseManager.isPremiumSync()` üzerinden yapılır; ekran dönüşünde durum yenilenir.
- Yeni günlük görevler seçilen sınıfa göre filtrelenir. Mevcut görevler ve ödüller, veri kaybı yaşamamak için gün içinde sıfırlanmaz; yeni seçim sonraki günlük görev üretiminden itibaren geçerlidir.
- Kullanılmayan eski çarpma-sorulu satın alma denemesi kaldırıldı; Play checkout değişmedi.

## Bu sürümün içerik/erişim denetimi
- Ana oyun kataloğundaki 14 başlığın hepsinde Free başlatılabilen gerçek içerik bulunur.
- Dört işlem oyunlarında ve Karışık Mod'da zorluk seçimi/bizzat oyun başlatma kontrolü vardır.
- Geometri, Balon, Uzay ve Sayı Avı için eski ücretli checkpoint korunur; Free kullanıcısı Basic seviyede ayrı kayıtla oynayabilir.
- Pazar, Labirent, öğrenme kütüphanesi, istatistik, avatar ve ikonların önceki sınırları korunur.
- Bu kontroller APK içindedir. Güvenilir Play abonelik süresi ve token doğrulaması sunucu gerektirir.

## Yayın sınırları
- Bunlar APK içi erişim kontrolleridir; kriptografik sunucu yetkilendirmesi değildir. Play Developer API doğrulaması, güvenilir backend, Cloudflare hak eşlemesi ayrı iş kapsamıdır.
- Gerçek Play testçisiyle satın alma, restore, iade/iptal ve offline koşulları; çocuk hedefli Families ve imzalı release AAB ayrıca denetlenmelidir.
