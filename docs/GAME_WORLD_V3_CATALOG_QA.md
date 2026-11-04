# Oyun Dünyası 3.0 — Katalog, sınıf kapsamı ve mobil kabul kontrolü

## Amaç
Onaylanmış ada/kaşif ana sayfa tasarımı ve mevcut WebP/vektör kapakları korunur.
Tüm Oyunlar ekranında 14 oyun beş konu grubunda listelenir. Dört İşlem Merkezi ayrı
bir giriş kartıdır; 15. ayrı oyun sayılmaz. Aynı metadata `GameCatalog.kt` içinde
tutulur ve home carousel ile öneri kartı bu veriden beslenir.

## Kategori haritası
- Sayılar ve İşlemler: Toplama, Çıkarma, Çarpma, Bölme (4)
- Sayılar ve Kesirler: Sayı Avı, Orman Keşfi, Kesir Kristalleri (3)
- Geometri ve Mantık: Geometri Kalesi, Örüntü Ormanı (2)
- Zaman ve Günlük Hayat: Zaman Kulesi, Pazar Simülatörü (2)
- Matematik Maceraları: Balon Patlatma, Uzay Macerası, Matematik Labirenti (3)

## Sınıf etiketlerinin güvenilirlik kuralları
- GRADE_1_8: sorular seçili sınıfa göre üretilir, ancak bu tek başına resmî MEB
  kazanım eşleştirmesi veya tam öğrenme değerlendirmesi anlamına gelmez.
- GRADE_1_4: Uzay Macerası 1–4. sınıfa göre soru üretir. 5–8. sınıflarda
  4. sınıf düzeyindeki tekrar olarak *açıkça* gösterilir.
- FOUNDATION_2: Sayı Avı ve Orman Keşfi sabit sınıf-2 eş gruplama konularını
  pekiştirir. Üst sınıf profillerinde "önceki sınıftan tekrar" gösterilir.
- TIER_ONLY: Balon, Labirent ve Pazar kişisel zorluk önerisi yapabilir ancak
  sınıf bazlı müfredat tavanı uygulamıyor. "Seviyeni seçerek oyna" görünür.
- 1. sınıf çocuğunun karşısına çarpma/bölme kilitli gelir; diğer oyunlar
  "Tümü" filtresinde kaybolmadan görülür.
- "Sınıfıma göre" yalnız gerçekten sınıfa göre içerik sunan oyunları gösterir.
- Oyun başlatma geçmişi sadece son açılan oyun id'sini tutar, tamamlama
  veya "kaldığın yerden" kaydı gibi sunulmaz.

## Fiziksel APK kabulü
1. 1., 2., 4., 6. ve 8. sınıflarda kategorileri ve kilitleri kontrol et.
2. 6. sınıfta Uzay kartı "4. sınıf düzeyi · tekrar" göstermeli; Balon ve
   Pazar "6. sınıfa özel" görünmemeli.
3. 2. sınıfta Sayı Avı "2. sınıf · eş gruplama" göstermeli;
   6. sınıfta aynı oyun "2. sınıftan tekrar" demeli.
4. Sınıf profilini değiştirdikten sonra geri dön; ana sayfa önerileri ve
   Tüm Oyunlar etiketleri güncellenmeli.
5. Her kart kapak resminden doğru Activity açılmalı. Çarpma/bölme
   sınıf-1 kilidi korunmalı. Dört İşlem Merkezi normal çalışmalı.
6. Son açılan oyun Hızlı Erişim'de öne gelmeli; katalogda ismi görünmeli.
   Bu kayıt ilerleme, yıldız, premium veya satın alma durumunu değiştirmemeli.
7. 320 dp küçük ekran, büyük font, ekran döndürme, 3 tuşlu/gesture
   navigasyon ve alt gezinme çubuğunda taşma kontrolü yapılmalı.
8. `python3 scripts/check_game_catalog.py` ve GitHub Actions
   `:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest`
   başarılı olmalı. CI gerçek cihaz dokunuş ve görsel kalite testinin yerine geçmez.

## Sınırlar
Öneri sistemi şimdilik sınıf kuralına dayanır, çocuğun öğrenme kazanımını
tahmin eden bir makine öğrenmesi modeli değildir. Yaşa/performansa dayalı
gerçek öğrenme önerileri için önce bütün oyunlardan güvenilir beceri olayları
toplanmalı ve validasyon yapılmalıdır.
