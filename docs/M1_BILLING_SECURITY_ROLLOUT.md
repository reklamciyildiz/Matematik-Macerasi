# M1 — Google Play Ticari Güvenlik, Aşama 1 (sunucu temeli)

Durum: **KOD HAZIR, CANLIDA ETKİN DEĞİL**. GitHub'da sunucu bileşeni vardır;
Firebase Functions'a deploy EDİLMEDİ, Google Play Console veya ürün fiyatları
değiştirilmedi, Android istemci hâlâ mevcut tek seferlik SKU'yu kullanır.

## Neden önce sunucu?

Mevcut Android `PurchaseManager`, Google Play'deki `PURCHASED` durumunu
istemciden okuyarak yerel `premium_prefs` alanında saklıyor; Play Developer API
ile bağımsız kontrol yok. Premium kod ve eski satın alımları etkileyen bir
yükseltmeyi bu nedenle kullanıcı/müşteri geçiş planı olmadan zorlamıyoruz.

### Kaynaklar

- Android API: `purchases.productsv2.getproductpurchasev2`
- Android ürün kimliği: `matematik_macerasi_premium`
- Paket: `com.yigit033.matematikmacarasi`
- Satın alma sınıfı: tek seferlik, tüketilmeyen `INAPP`, abonelik değil

## Eklenen bileşenler

- `commercial/functions/index.js`: Firebase callable `verifyGooglePremium`
  ve `getParentPremium`; Firebase Auth ve App Check gerekli.
- `commercial/functions/src/policy.cjs`: Google'dan dönen PURCHASED/PENDING/
  CANCELLED durumlarının güvenli sınıflandırması, hesap bağlama ve token hash'i.
- Firestore sunucu koleksiyonları: `billingTokensV1` ve
  `parentPremiumEntitlementsV1`; çocuk verisi, tam ödeme tokenı veya kart
  numarası saklanmaz. Firestore security rules'da istemci okuma/yazma yasak.
- Aynı ödeme tokenının farklı ebeveyn UID'lerine bağlanması yasak;
  tombstone kayıtları sayesinde iade edilmiş tokenın tekrar verilmesi engellenir.
- Play RTDN: güvenilir Pub/Sub kanalındaki iade/iptal bildiriminde
  server-side sahiplik yenilenir; başarısız ACK tekrar denenir.
- `reconcilePlayRefunds`: her 12 saatte son 29 günü kapsayan
  Voided Purchases API kontrolü (30 günlük API sınırını aşmaz).
- Node politika testleri CI'da çalışır. Gerçek satın alma, Firebase ve Play
  kimlik bilgileri CI ortamında KULLANILMAZ.

## Açmadan önce zorunlu adımlar

1. Kullanıcı onaylı Firebase/GCP projesi: Blaze, Cloud Functions v2,
   Cloud Firestore, Pub/Sub, Cloud Scheduler. Bölge `europe-west1`.
2. Ebeveyne ait Firebase Authentication hesabı veya güvenilir kimlik
   köprüsü oluştur. Anonim çocuk profili sahiplik için yeterli değil;
   mevcut uygulama Firebase ebeveyn girişi zorunlu tutmuyor.
3. Android Play Integrity ve Firebase App Check'i yapılandır; callable
   endpoint App Check doğrulanmadan erişim vermiyor.
4. Google Play Console'da Cloud Functions servis hesabına gerekli
   API yetkilerini ver; `androidpublisher` erişimini test et.
5. Pub/Sub topic `mm-google-play-billing` kur; Google Play RTDN ayarlarını
   bu topic'e yönlendir ve Play'e publisher yetkisi tanı.
6. Firestore rules: `billingTokensV1` ve
   `parentPremiumEntitlementsV1` için istemci erişimini kapalı tut.
   `commercial/firestore_billing_rules.fragment` şablonunu mevcut rules dosyasına
   ekle; mevcut kuralları bu fragmanla tamamen değiştirme.
7. Google Play'de mevcut sahiplikleri yeni ebeveyn hesaplarıyla
   güvenle eşleme: eski Premium hakkına dokunmadan, tek kullanımlık
   onaylı geçiş. İadeli ve farklı hesap tokenlarını negatif test et.
8. İstemciye callable SDK ve satın alma hak durumları ekle, **özellikle**
   ilk satın alma ve restore aşamalarında sunucu doğrulamasını zorunlu
   yapmadan önce Play test kanalında dene.
9. Satın alma, PENDING→PURCHASED, ACK başarısızlığı, iade, chargeback,
   farklı hesap, uygulama silme/yeniden yükleme, eski premium kod
   ve çevrimdışı güvenli erişim davranışlarını test et.
10. Eksik API izinleri, RTDN veya veritabanı erişimi varsa release'i DURDUR.
    Varsayılan olarak client-only yeni satışlara devam edilmesini kabul etme.

## iOS / M2 / M3 sırası

**M1-B:** Ebeveyn kimliği, Android callable entegrasyonu, geçmiş hak göçü,
gerçek Play satın alımı ve iade senaryoları. Canlı deploy/ayar değişimi
ancak Play Console ve Firebase erişimleriyle kullanıcı onayından sonra.

**M2:** Mevcut kullanıcı haklarını bozmadan ebeveyn odaklı Premium satış
ekranı. Fiyat, Google Play'den gerçek `formattedPrice` ile gösterilmeli;
sabit sabit TL yazılmamalı. Reklamlar kapalı kalmalı.

**M3:** İmzalı release AAB, Families/veri güvenliği, internal testing,
kapalı pilot, kullanıcı davranışını mahremiyete uygun ölçme ve testçi
satın alımları. Test APK'sı gerçek ödeme kanıtı değildir.

**iOS:** Compose Multiplatform ile ortak oyun motorları ve Compose
ekranları; Android'e özel Activity, Context, R.drawable, Firebase
ve Play Billing kodları `expect/actual` veya platform servis
arayüzlerine ayrılacak. iOS ödeme sistemi StoreKit 2, Apple
sunucu bildirimi ise doğrulanmış JWS + App Store Server API olacaktır.
iOS ürününü Android satın almasıyla aynı hak olarak sunacaksak
**aynı ebeveyn hesabı** ve ortak server-authoritative entitlement
tasarımı gerekir. Bu kararı mağazada vaat etmeden önce ver.

## Çalıştırma (yalnız statik test)

```bash
node --test commercial/functions/src/policy.test.cjs
node --check commercial/functions/index.js
```

### Uygulanabilirlik sınırı

Bu kod **canlı ödeme altyapısının tamamlandığını kanıtlamaz**. Firebase
kimlik ve yetkilendirme, Google Play API izinleri, yayın güvenliği,
RTDN teslimi, Firestore transaction davranışı ve gerçek Play ödeme
testi henüz dış sistemlerde doğrulanmalıdır.

## M1.2 — Kurumsal lisans paketi / merkezi hak değerlendirmesi (kod hazır)

Sunucu tarafına aşağıdaki callable fonksiyonlar eklendi; Firebase'e **deploy yapılmadı**:

- `createInstitutionContract`: yalnız sunucuda verilmiş `mmCommercialAdmin`
  claim'li yönetici, önceden doğruladığı kurumsal faturaya karşı sözleşme açar.
  Aynı fatura referansıyla tekrar oluşturmak yeni lisans üretmez.
- `issueInstitutionSeatCodes`: sözleşme kalan kapasitesi içinde 1–200 ayrı
  tek kullanımlık kod üretir. 200 lisanslık pakette 201. kod reddedilir.
  `MM_LICENSE_SIGNING_KEY` Secret Manager secret gerekir; aynı istek kimliği
  tekrar edilirse aynı kod paketi güvenle yeniden teslim edilir.
- `redeemInstitutionSeatCode`: **Firebase Auth + App Check + güvenilir yönetici
  tarafından tanımlanan `mmParentVerified` claim** gerektirir. Firestore
  transaction ile kod, kullanıcıya bağlı lisans ve sözleşme sayacı **atomik**
  güncellenir. Cihaz kimliği öğrenci lisansı yerine kullanılmaz.
- `revokeInstitutionSeatCode` ve `updateInstitutionContractStatus`:
  kurum lisansının iptali, askıya alınması. **Google Play satın alımına dokunmaz.**
- `getVerifiedPremiumAccess`: Play satın alımının sunucu hakkı ile kurum
  lisansını **ayrı kaynaklar** olarak değerlendirir, biri iptal edildiğinde
  diğerini silmez. Eski kodlar yalnızca doğrulanmış bir geçişten sonra
  sunucu hakkı olabilir.

Veri koleksiyonları: `institutionContractsV1`,
`institutionCodeBatchesV1`, `institutionCodesV1`,
`institutionAssignmentsV1`. Kodlar yalnız güçlü SHA-256 hash olarak
saklanır; temiz metin yalnız yetkili yöneticinin kod paketini teslim
aldığı çağrı yanıtında bulunur. Öğrenci adı, doğum tarihi veya e-posta
sunucu lisans kayıtlarına yazılmaz.

Bu sürümde yönetici kendi Firestore claim'ini, ebeveyn claim'ini ya da
kurum sözleşmesini mobil istemciden oluşturamaz. Claim'ler Firebase Admin
SDK üzerinden güvenilir operatör tarafından atanmalıdır.

### ÖNEMLİ: Geçiş kapıları

1. Firebase Auth ebeveyn hesabı, App Check ve Admin claim'leri
   yapılandırılmadı; yeni fonksiyonlar **mevcut Android uygulamasına bağlanmadı**.
2. Firestore client kurallarında dört yeni kurum koleksiyonu ve iki ödeme
   koleksiyonunun tüm doğrudan istemci okuma/yazması kapatılmalı.
3. Eski iki Google Play satışı, `PurchaseManager` ve
   `premium_codes` / `code_activations` kayıtları aynen kalır.
4. Gerçek Firebase emulator'ında 200 paralel kod kullanma, kodun ikinci
   hesap tarafından tekrar kullanımı, bağlantı kopma ve tekrar deneme
   senaryoları doğrulanmalı. Node testleri statik/politika testleridir,
   **Firebase yarış testi yerine geçmez**.
5. Kurumsal satış ve dağıtımın Google Play/App Store politikası ile
   hukuki/faturalandırma uyumluluğu ayrıca onaylanmalı; tek başına harici
   lisans kodu kullanımı mağaza muafiyeti anlamına gelmez.
