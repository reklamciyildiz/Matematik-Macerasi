# Matematik Macerası — AAB öncesi ürün kalite sözleşmesi (7 Kasım 2026)

**Kapsam:** Mevcut Android Kotlin/Compose ürünü. Bu belge hedefleri ve kodda gerçekten bulunan erişimleri birbirinden ayırır. AAB yayın onayı değildir.

## 1. Ücretli ürünün gerçek kapsamı (mevcut sürüm)

Tek, tüketilmeyen Google Play ürünü: `matematik_macerasi_premium`. Cloudflare ile kullanılan Premium kodu aynı haklara açılır. **Abonelik değildir.**

| Yetenek | Ücretsiz | Premium | Teknik durum |
|---|---|---|---|
| Temel matematik oyunları | Açık | Açık | Var |
| Öğrenme patikası | Mevcut kısa dersler | Aynı temel dersler | Var; **tam sınıf müfredatı değil** |
| Temel öğrenme kanıtları / oyun raporları | Açık | Açık | Var |
| Pazar | 3 mağaza, 12 karar | 10 mağaza, 40 karar | Kodla sınırlandırılmış |
| Kendi Tezgâhım | Kapalı | Açık | Var |
| Avatarlar | İlk 6; yıldız gerekebilir | 7–15 ayrıca seçilebilir; yıldızla açma mevcut | Var; ekonomi değişmedi |
| Uygulama ikonları | Varsayılan | Alternatifler | Var |
| Matematik Labirenti | Günde 5 oyun | Sınırsız | Kodda doğrulanmış |
| Profil istatistikleri | Yıldız ve öğrenme ilerlemesi | Süre, tamamlanan oyun ve favori oyun dahil | Var |
| Günlük Premium yıldız bonusu | Yok | Var | Var |
| Reklamsız | Evet | Evet | **Reklamlar şu anda herkese kapalı; satılacak ayrıcalık gibi pazarlanmamalı** |

Bu sürüme *henüz olmayan* AI özel ders, MEB müfredatı eksiksiz kapsama, hesaplar arası bulut senkronizasyonu veya tüm derslerde uyarlanabilir motor gibi özellikler eklenmiş gibi gösterilmeyecek.

## 2. Kararlaştırılan UX ilkeleri

- Tek ana dünya: parlak mavi / okyanus / fantastik kaşif estetiği; profil, avatarlar ve öğrenme aynı görsel dilde.
- **Avatar galerisi:** büyük karakter önizlemeleri, 2 sütun mobil grid, mevcut yıldız bakiyesi, Tümü / Ücretsiz / Premium / Açtıklarım filtreleri, belirgin Seçili/Kilitli durumu; dokunma alanı ve uzun isim taşması kontrolü.
- **Avatar veri güvenliği:** Premium geçici olarak doğrulanamadığında geçmişte yıldızla açılmış avatarlar kalıcı olarak silinmeyecek; uygunluk seçim anında doğrulanacak.
- **Profil:** kaşif kimliği + avatar erişimi; gerçek ders ilerleme özeti; kişiselleştirme ve istatistiklerin anlaşılır hiyerarşisi. Yanlış başarı / yapay seviye yok.
- **Öğren:** ilk ekranda yalnız oynanabilir dersler; önce ders, sonra isteğe bağlı açılan aile/gelişim raporu; ilerleme kalıcı; sınıf 5–8 için temel tekrar uyarısı korunacak.
- **Eğitim yaklaşımı:** temel öğrenme Free için erişilebilir kalsın; Premium değerini *gerçekten çalışan* genişletilmiş maceralar, kozmetik ve istatistikler oluştursun. Tam müfredat geliştirilirse çocuklara temel içeriği kapatmadan ilerle.
- **Satın alma:** mevcut kullanıcı talebiyle Google Play Billing doğrudan açılır. Google Play / hesap ailesi onayları geçerlidir. Play politikası ve Families yayımlama kontrolü gereklidir.

## 3. İteratif uygulama ve kabul

### Aşama A — kod temeli / ilk UX revizyonu
- [x] Avatar geçmiş kilit kayıtları Premium entitlement gecikmesinde silinmez.
- [x] Avatar mağazası, mavi temalı filtrelenebilir karakter galerisi.
- [x] Avatar seçme başarısızlığında yanlış `seçildi` mesajı verilmez.
- [x] Profilde gerçek, sınıfa duyarlı kısa ders ilerlemesi.
- [x] Öğren sekmesinde gerçek dersler öne, rapor isteğe bağlı; uzun çalışmayan içerik listesi kalktı.
- [ ] 360dp, 412dp, büyük font ve TalkBack ile *gerçek cihaz* görünüm incelemesi.
- [ ] Mevcut 15 avatarın görsel kalitesinin karşılaştırmalı revizyonu; art paketi / gerektiğinde WebP.
- [ ] Profilin kalan kişisel bilgi ve istatistik kartlarının görsel yoğunluk revizyonu.

### Aşama B — eğitim kalitesi
- [ ] 1–8. sınıf için konu/kazanım matrisi, gerçek kapsam ve soru havuzu tasarımı.
- [ ] Ders başına kısa anlatım → etkileşimli örnek → tekrar oynanabilir uygulama → açıklamalı cevap döngüsü.
- [ ] Tekrar önerileri, erişilebilir ses ve oyunlara kazanım odaklı yönlendirme.
- [ ] İçerik zorluk ve doğruluk testleri; sahte müfredat iddiası yok.

### Aşama C — AAB yayın kabulü
- [ ] Premium Play test kullanıcısıyla doğrudan satın alma, restore, PENDING/PURCHASED kontrolü.
- [ ] Cloudflare RSA lisans + eski Firebase kullanıcı hakları geçiş testi sürüm yükseltmeyle.
- [ ] Çocuk hedefli reklam/SDK, gizlilik ve Google Play Families uyumluluğu denetimi.
- [ ] Play purchase-token doğrulamasının güvenilir sunucu boyutu gözden geçirilir.
- [ ] Release signing, versionCode, AAB, kapalı test kanalı ve çökme izlemesi.
- [ ] Firebase RTDB eski erişim yalnız eski sürüm geçiş politikası uygulanınca kilitlenir.

**Not:** Bu dosyadaki [x] işaretleri yalnız kod değişikliğinin işlendiğini ifade eder; telefon QA onayı değildir. Her adım GitHub CI ve fiziksel cihaz QA'den geçmeden tamamlanmış ürün olarak anılmaz.
