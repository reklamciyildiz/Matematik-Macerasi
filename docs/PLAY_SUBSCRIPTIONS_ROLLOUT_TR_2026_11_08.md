# Matematik Macerası · Google Play abonelik kuruluş planı (2026-11-08)

Bu dosya **Play Console'a fiyat kaydetmez**, ürün oluşturmaz ve ödeme almaz.
Abonelikler yalnızca Google Play üzerinden kurulacaktır. Çocuk profilleri için
satın alma girişi ebeveynin erişebileceği bölümde olmalı; otomatik yenileme
şartları ve gerçek Play fiyatı satın almadan önce açıkça görünmelidir.

## Türkiye başlangıç önerisi

- Aylık: 79,99 TRY / ay (otomatik yenileme)
- Yıllık: 499,99 TRY / yıl (otomatik yenileme; 12 x aylığa göre ~%47,9 daha düşük)
- Mevcut **ömür boyu INAPP** ürünü: fiyatını ve haklarını koru, kullanıcılar
  satın aldığı kalıcı hakkı kaybetmesin. Eğer ömür boyu ürün yıllık fiyatın
  altındaysa abonelik fiyat kararını tekrar değerlendir.
- Diğer ülkelerin fiyatlarını Play'in yerelleştirmesiyle ayrı kontrol et.
  Çocuklar için yanıltıcı geri sayım, sahte indirim veya varsayılan deneme yok.

## Console

1. Matematik Macerası > Play ile para kazanın > Ürünler > Abonelikler.
2. **Bir** abonelik oluştur: \`matematik_macerasi_premium_subscription\`
   (görünen ad: Matematik Macerası Premium). Mevcut INAPP
   \`matematik_macerasi_premium\` ürününe dokunma.
3. \`monthly\` kimliğinde otomatik yenilenen P1M temel plan;
   Türkiye fiyatı 79,99 TRY; açık şartlar.
4. \`yearly\` kimliğinde otomatik yenilenen P1Y temel plan;
   Türkiye fiyatı 499,99 TRY; açık şartlar.
5. Abonelikleri canlı satılabilir hâle getirmeden önce Android'de güvenli
   sunucu doğrulaması, satın alma token'ı sahipliği, Google Play
   subscriptionsv2, acknowledgement, RTDN, iptal/iade, hesap/cihaz restore
   ve ebeveyn giriş akışlarını dahili testte doğrula.
6. Ürünün / temel planların etkinleşmesiyle **uygulamadaki**
   \`MM_SUBSCRIPTIONS_ENABLED\` ayrı konulardır; sunucu hazır olmadan
   uygulama içindeki bayrağı kesinlikle true yapma.

## Backend hazırlığı

\`src/google-play-verifier.mjs\`: gizli servis hesabıyla OAuth JWT
oluşturur, Android Publisher API \`subscriptionsv2.get\` çağırır,
ardından doğrulanmış Google yanıtını \`src/entitlement.mjs\` politikasına
verir. Bu **tek başına dağıtılabilir çalışan ödeme sunucusu değildir**.
Eksikler: kullanıcı/cihaz hesabı bağlama, güvenilir endpoint,
purchase token tekrarı koruması, sertifikalı istemci/Play Integrity,
D1 state, RTDN, iptal/iade ve entitlements için güvenli signed TTL.
Google servis hesabı JSON'u yalnız Worker secret olarak saklanmalıdır.
Bu aşamada herhangi bir kimlik bilgisi dosyası veya üretim veritabanı
değiştirilmemiştir.

## DEX kod optimizasyonu

Play Console ekranındaki uyarı mevcut sürüm 14 içindir. Şubat 2027
tarihinden itibaren DEX boyutu eşiğini aşan oyun/uygulamalarda R8 ile
optimizasyon/küçültme/karartma ölçümleri önem kazanacak.
Yeni \`MM_R8_RELEASE_ENABLED=true\` sadece **isteğe bağlı QA derlemesi**
içindir; üretimde varsayılanı false. Test için bu yerel Gradle parametresiyle
ayrı imzalı AAB derle, haritalamayı güvenli sakla ve gerçek cihazda tüm
oyunları, lisans yenilemeyi, Google Play INAPP restore'u denetle. Bu ölçüm
Play Console'un istediği eşiklere ulaşıldığını tek başına kanıtlamaz.

Referanslar:
https://support.google.com/googleplay/android-developer/answer/140504
https://support.google.com/googleplay/android-developer/answer/17492799
https://support.google.com/googleplay/android-developer/answer/18265346
https://developer.android.com/google/play/billing/security
https://developer.android.com/google/play/billing/lifecycle/subscriptions
