# M1 — Gerçek kullanıcı haklarını koruyarak kurumsal lisansa geçiş

Durum: **STAGING CODE READY; LIVE FIREBASE NOT CONNECTED**.

## Değişmeyen ve korunan mevcut sistem

- `PurchaseManager` Google Play Billing 8.0.0 ile aynı
  `matematik_macerasi_premium` ürününü satın alır/onaylar/restore eder.
- Önceden yapılmış iki gerçek satışa ilişkin hiçbir Play Console ayarı,
  SKU, ürün fiyatı, token veya Premium cache'i değiştirilmedi.
- Firebase Realtime Database'in `premium_codes` ve `code_activations`
  yapısı değiştirilmedi. **Mevcut kodlar kendi eski yolunda çalışır.**
- Kurum sunucu fonksiyonları Android'den çağrılmıyor ve canlıya deploy edilmedi.

## Tamamlanan kod işleri

- Android `PremiumEntitlementPolicy.hasAccess` Google Play ve eski Firebase
  kod hakkını aynı karar fonksiyonunda değerlendiriyor. Sunucu tarafından
  doğrulanmış kurum hakkı için hazır argüman var fakat **istemci tarafında
  henüz aktif değil**.
- Ayarlar ekranı farklı kod zaten etkinse yanlış başarı mesajı vermiyor.
- Loglardan kod ve cihaz kimliği yazdıran satırlar kaldırıldı.
- Node sunucu: kurum sözleşmesi, tek kullanımlık 20/200 seat kodu üretimi,
  kullanımın tek transaction'da kaydedilmesi, iptal, askıya alma ve
  bağımsız hak değerlendirme tasarlandı.
- Kodlar HMAC ile üretiliyor, yalnız hash saklanıyor, kod paketi
  yetkili yöneticiye döndürülüyor. Sabit secret değiştirilirse
  eski paketler aynı istekle yeniden üretilemez; bu nedenle secret
  rotasyonu kontrollü ve sürümlü geçişle yapılmalıdır.
- Firestore kurum/premium koleksiyonları için **yalnız şablon**
  `commercial/firestore_billing_rules.fragment` içinde doğrudan istemci
  erişimini engelleyen kurallar tanımlandı.
- Testler: 200 kod benzersizliği, 201. koltuğun reddi, iptalden sonra
  hakkın kaybolması, Play hakkının bağımsız kalması, Android hak birleştirme.

## Canlıya çıkmadan önce

1. Google Play Console'daki iki mevcut gerçek satın alımın durumunu
   içerden doğrula; kimlik/tutar/token paylaşma.
2. Firebase projesinin gerçek RTDB kurallarını **read-only** denetle;
   mevcut istemci yazma izinleri önemli güvenlik riski olabilir.
3. Güvenilir ebeveyn Firebase Auth hesabı + App Check kur;
   `mmParentVerified` claim'ini yalnız yetişkin/veli doğrulamasından sonra
   sunucu atamalı; çocuklar kendileri oluşturamamalı.
4. `mmCommercialAdmin` özel claim'ini sadece işletme yöneticilerine
   güvenilir Admin SDK üzerinden ver; mobil cihazdan verilememeli.
5. Firestore rules şablonunu mevcut kurallarla **birleştir**;
   başka veri koleksiyonlarının kurallarını yanlışlıkla silme.
6. Secret Manager'da `MM_LICENSE_SIGNING_KEY` oluştur, en az 32 bayt
   yüksek entropili rastgele değer kullan; asla GitHub'a yazma.
7. Önce Firebase Emulator Suite'de 200 paralel etkinleştirme,
   iki hesabın aynı kodu kullanması, bağlantı kopma/idempotency
   ve sözleşme askıya alma testlerini yap.
8. Gerçek Play test kanalında eski iki satın alma hakkı ve kodlar
   sorunsuz görünmeden yeni hak servislerini istemciye bağlama.
9. Firebase Auth + kurumsal Cloud Functions pilotu canlıya alınınca
   kullanıcı geçişi ve geri alma (rollback) mekanizmasıyla etkinleştir.
10. Kurumlara faturayla kod dağıtımının Google Play/App Store dijital
    ürün politikalarına uygunluğunu ticari sözleşmeden önce teyit et.

## Geçiş sırası

M1.2 güvenli hak modelleme → M1.4 Firebase / kurum pilotu (ilk yalnız
admin yetkili test hesapları) → M1.3 Android Play token eşleştirme →
M1.7 eski hakların güvenli taşınması → M2 Premium ürün teklifi →
M3 Android kontrollü satış pilotu.

**Bu dosya canlıya geçişin tamamlandığı anlamına gelmez.**
