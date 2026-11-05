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

## Tek velinin birden fazla çocuğu

Kurumsal lisans tek veli UID'sine değil, `(veli UID, çocuk profil ID)`
ikilisinden türetilmiş anonim hash anahtarına bağlanır. Aynı veli iki
farklı çocuk profiline iki farklı kod tanımlayabilir. Aynı kod farklı
çocuk profilinde veya başka velide tekrar kullanılamaz. Kodda öğrenci
adı/e-postası saklanmaz. Çocuk profil ID'si veli hesabına bağlı
kalıcı kimlik olmalıdır; bunun Android/Cloud senkronu hâlâ M1
canlı entegrasyonunda geliştirilmelidir.

## M1 güvenli kapatma / üretim kabul testi (5 Kasım 2026)

**Kod güvenliği** ve **canlı operasyon güvenliği** farklıdır. GitHub CI gerçek
Firebase Auth, Firebase App Check, Firestore Emulator, Play API, Play Console
satışları veya gerçek RTDB kurallarına erişemez.

### Sunucu güvenliği ve kod düzeltmeleri

- `MM_M1_BACKEND_ENABLED` ortam değişkeni varsayılan olarak **KAPALI**.
  Yeni tüm callable ve iade bildirimleri bu bayrak açılmadan veritabanına yazmaz.
- Kurum lisansı cihaz kimliğine değil, sunucuda güvenilir yönetici tarafından
  doğrulanmış `(veli UID, çocuk profil kimliği)` eşleşmesine verilir.
  Rastgele veya kullanıcı tarafından oluşturulmuş bir profil ID'si tek başına
  Premium açmaz. `setVerifiedStudentProfile` yöneticilere özeldir.
- 200 eşzamanlı etkinleştirmede kurum sözleşme belgesinin her seferinde
  güncellenmesi kaldırıldı. Her kodun benzersiz `institutionRedemptionsV1`
  kaydı işlem içinde oluşturulur. `getInstitutionContractSummary` sayıları
  ledger'dan sorgular. Dolayısıyla merkezi sıcak sayaç sorunu azalır.
- Kod üretiminin tekrar denenmesi aynı kod listesini döndürür; imzalama
  secret'ı değişmişse yanlışlıkla başka liste verilmez.
- Kurum kullanım raporu yöneticiye yetkili callable ile sunulur;
  öğrenci/veli adı, e-posta veya sınıf kayıtlarına erişim vermez.
- Node testleri gerçek fonksiyonları Firestore işlem simülatöründe
  200 eşzamanlı kod/200 farklı profil/iptal/Play-ilişki senaryolarıyla
  çalıştırır. **Bu testler gerçek Firestore Emulator testi değildir.**

### Üretim ortamına geçiş için zorunlu kabul koşulları

- [ ] Firebase RTDB/Firestore kuralları, mevcut `premium_codes`,
      `code_activations` kayıtları ve yedekler read-only incelendi.
- [ ] İki Play satışının aktif hakları Console'da doğrulandı. Gerçek token
      veya ödeme kimliği sohbet, günlük kayıtları veya GitHub'a eklenmedi.
- [ ] Firebase Auth doğrulanmış veli hesabı ve rol tabanlı ticari yönetici
      hesabı gerçek ortamda oluşturuldu; App Check zorunluluğu test edildi.
- [ ] Güvenlik kuralları için gerçek Firestore Emulator testleri geçti ve
      bu şablon mevcut rules'a diğer koleksiyonlar silinmeden eklendi.
- [ ] 200 paralel aktivasyon, 201. koltuk, tekrar deneme, askıya alma,
      2 çocuk/1 veli, profili askıya alma **gerçek Emulator'da** geçti.
- [ ] Google Play Developer API, satın alma onayı, iade/iptal ve Pub/Sub
      RTDN ayrı test hesaplarında doğrulandı.
- [ ] Android istemcinin yeni doğrulanmış hakkı çağıran yolu, eski Premium
      sahiplerini düşürmeyen geçiş ve geri alma testleriyle tamamlandı.
- [ ] Mağaza kuralları açısından kurum lisanslarının dijital içerik erişimi
      için harici satış uygunluğu teyit edildi.
- [ ] Ancak tüm maddeler geçilirse sunucu için
      `MM_M1_BACKEND_ENABLED=true` **kontrollü test diliminde** açılabilir.

### Geri alma (rollback)

Bir anomali varsa `MM_M1_BACKEND_ENABLED=false` ile yeni sunucu işlemleri
durdurulur. Eski Play satın alma, geri yükleme ve Firebase kod aktivasyon
ekranı çalışmaya devam eder. Gerçek hakları toplu silme, SKU değiştirme,
Realtime Database kayıtlarını taşıma veya satın alımları yeniden oluşturma
gibi işlemler yapılmaz. Bu bayrak, yeni kodla aktif edilmiş hakların istemci
tarafında nasıl ele alınacağını çözmez; o nedenle yeni istemciyi yayına
almadan önce ayrıca test gerekir.

**M1'in canlı tamamlanma durumu: DIŞ DOĞRULAMA GEREKİYOR.**
