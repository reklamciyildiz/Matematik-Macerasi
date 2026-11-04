# Matematik Macerası — Android + iOS Ortak Compose Stratejisi

## Mimari kararı

**Ayrı SwiftUI oyunlarını baştan yazmıyoruz.** Mevcut Jetpack Compose UI
kütüphanesini Compose Multiplatform ile ortak `commonMain` kaynağına taşıyoruz.
Android `:app` uygulaması ve Play Billing satın alma hakları bozulmadan
yayında kalabilir. iOS, tek bir Xcode iOS runner üzerinden ortak Compose UI'yi
çalıştırır.

```
:app                        Android (korunacak)
:shared                     Kotlin Multiplatform
  commonMain/               Saf Kotlin soru motorları, görevler, Compose UI
  androidMain/              Android depolama/medya/bildirim köprüleri
  iosMain/                  iOS depolama/medya/StoreKit köprüleri
iosApp/                     Xcode, App Store / TestFlight runner
commercial/functions/       M1 doğrulanmış ebeveyn satın alma hakları
```

## Kod envanteri (4 Kasım 2026)

**Yüksek paylaşım potansiyeli**
- `games/sayiavi2/NumberHuntGradeEngine.kt`,
  `NumberHuntRichContent.kt`: matematik içeriği saf Kotlin, Java/Android API
  kullanmıyor. (Bağımlı `LearningTier` modelleri ayrıştırılmalı.)
- `games/interactive/*Engine.kt`, zaman/örüntü/kesir vb. saf oyun motorları.
- Compose oyun ekranları ve `ui/screens/home`: görsel hiyerarşi, kart ölçüleri,
  metin ve oyun mekaniği kısmen ortaklaştırılabilir.

**Taşınması gereken Android bağımlılıkları**
- `Activity`, `Context`, `SharedPreferences`,
  `androidx.activity.compose.BackHandler`, Android manifest/izinler;
- `R.drawable` / `painterResource` kullanımını ortak kaynak API'siyle değiştir;
  mevcut premium WebP dosyalarını yeniden tasarlama;
- Media/AudioService, titreşim, bildirim, launcher icon, gesture back;
- Google Play Billing `PurchaseManager` ve Android reklam kodu
  (iOS'ta kopyalanamaz); reklam teslimi kapalı kalmalı;
- Firebase Auth / PremiumCodeValidator / cihaz kimliği: ebeveyn hesabı
  ve platform servis sınırları gerekir;
- tarih/yerel zaman, dosya sistemi, güvenli anahtar zinciri.

## Adım adım

1. **Önce M1 kapanış koşulları:** doğrulanmış ebeveyn kimliği, Android Play
   satın alımı, iade ve geri yükleme. İleride iOS'ta aynı haklar vaat edilecekse
   ortak ebeveyn hesap anahtarı tasarlanmadan mağazada bu vaatte bulunma.
2. **iOS-0 — Teknik kanıt:** yeni `:shared` modülünde tek saf motoru (Sayı
   Avı) ortaklaştır; iOS simulatore özel küçük Compose ekranı aç. Bu iş
   mevcut Android `:app` derlemesini değiştirmeden yapılmalı.
3. **iOS-1 — Ortak UI:** tek oyun ekranını ve kart bileşenlerini taşı.
   Görsel kaynaklarını Compose Multiplatform resources formatında kullan.
   `expect/actual` ya da enjekte edilen interface ile Android/Apple
   servislerini ayır.
4. **iOS-2 — Ürün genelinde geçiş:** ana sayfa, tüm oyunlar, oyunlar,
   profil/raporlar/ayarlar; her etapta Android ekran parity testi.
5. **iOS-3 — Ticari özellik:** StoreKit 2 tek seferlik ürün,
   geri yükleme, yetişkin bölgesi, App Store Server API / doğrulanmış
   Server Notifications V2, iOS App Check ve güvenli hak eşleme.
6. **iOS-4 — Kalite:** iPhone SE tipi küçük ekran, büyük Dynamic Type,
   iPad, notch/safe-area, ekran döndürme, VoiceOver, background ses,
   satın alma sandbox/StoreKit testleri, TestFlight ve mağaza incelemesi.

## Kabul kriterleri

- Android `:app:testDebugUnitTest :app:assembleDebug` tüm aşamalarda yeşil.
- Görsel/hareket arayüzü tek Compose kaynak kodundan üretiliyor; aynı
  ekran için Android/iOS arasında iki kopya Compose bileşeni yok.
- Oyun motorları aynı tohumda aynı doğru cevapları üretir; platforma
  özgü sözde-rastgele üretim farkı varsa deterministik seed policy korunur.
- Eski Android ödemeleri ve yıldız/ilerleme kayıtları kaybolmaz.
- iOS StoreKit ödeme kanıtı olmadan Premium açılmaz; Apple kimlik
  ve ödeme kayıtları ayrıca doğrulanır.
- Mac + Xcode ile iOS simülatör ve gerçek cihaz derlemesi/testi gerekir;
  Windows üzerindeki Android APK iOS uygulamasına çevrilemez.

## Dikkat

Mevcut 194 Kotlin kaynak dosyasının tamamının taşınması tek tıklama değildir.
Çoğu Compose UI bileşeni yeniden kullanılabilir; ancak Android'e bağımlı
durum yönetimi, kaynaklar ve platform servisleri düzenlenmelidir.
Bu doküman yol haritasıdır; **iOS modülü henüz oluşturulmadı**.

Resmî kılavuzlar:
- https://kotlinlang.org/docs/multiplatform/faq.html
- https://www.jetbrains.com/help/kotlin-multiplatform-dev/compose-multiplatform-create-first-app.html
- https://developer.apple.com/documentation/storekit
