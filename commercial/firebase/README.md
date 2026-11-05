# Firebase RTDB Premium Kod Güvenliği — Kilitleme Taslağı

**DURUM: TASLAK / YAYINLANMADI (5 Kasım 2026).**
`realtime-database.lockdown.rules.json` içindeki kuralları bugün
Firebase Console'a kopyalayıp `Publish` yapmayın.
Mevcut Android uygulaması doğrudan RTDB'den okuyor/yazıyor; bu
kurallar bugün yayımlanırsa kod etkinleştirme ve uygulama yeniden
kurulunca Premium hakkını geri yükleme bozulabilir.

## Canlı ortamda görülen (kullanıcı tarafından sağlanan ekran görüntüsü)

- `premium_codes` herkese açık okunuyor.
- `premium_codes/$code/used_count` anonim olarak yazılabiliyor.
- `code_activations/$deviceId` herkese açık okunabiliyor;
  `!data.exists()` koşuluyla kimlik doğrulamasız oluşturulabiliyor.
- `app_info` herkesçe okunabilir; yazma kapalı.
- Firebase Authentication kurulmamış; mevcut üründe zorunlu
  ebeveyn/çocuk hesabı istenmiyor.
- Google Play Billing satın alma/restore sistemi farklıdır; iki gerçek
  satın alımın hakları bu geçişte korunmalıdır.

Bu kurallar, güvenli yetkilendirme değildir. `.validate` tür kontrolü
yapar, müşteri kimliğinin veya Premium hakkının kanıtı değildir.
`Android_ID` de doğrulanmış bir kullanıcı kimliği değildir.

## Hedef mimari: hesap zorunluluğu yok

1. Uygulamada Firebase App Check + Android Play Integrity kullanımı
   hazırlanır; debug/sideload APK kullanımı ayrıca değerlendirilir.
   App Check, gerçek kullanıcı kimliğini **ispatlamaz**, kötüye kullanımı
   azaltan uygulama/cihaz doğrulamasıdır.
2. Premium kod doğrulama, kontenjan kontrolü ve tek seferlik kullanımı
   güvenilir sunucuya taşınır. Sunucu Firebase Admin SDK ile kayıtları
   işler, istemci artık `used_count` veya `code_activations` yazmaz.
   Kodun aktifliği ve bitiş tarihi sunucuda denetlenir.
3. Hesapsız cihaz için birden çok kurulum ve cihaz transferi tasarlanır.
   App Check tek başına `200 kod = 200 farklı öğrenci` garantisi vermez.
   Bunun ticari sınırları sözleşmede açıkça yazılır.
4. Mevcut eski kodları ve aktivasyonları salt okunur denetleyerek güvenilir
   hak aktarımı, iptal ve geri yükleme stratejisi belirlenir.
   Bugünkü anonim-yazılabilir kayıtlara otomatik mutlak güven verilmez.
5. İki gerçek Play satın alımı, eski kodla erişim ve yeniden kurulum
   farklı Android cihazlarda test edilir; sunucu kesintisinde
   güvenli, süreli erişim davranışı belirlenir.
6. Yeni istemci yayına çıkarılır, kullanıcıların sürüm geçişi ölçülür.
   En eski kullanılan sürümler için zorunlu güncelleme stratejisi
   önceden test edilir.
7. RTDB Emulator üzerinde yetkisiz okuma/yazma RED, `app_info` okuma
   ALLOW, sunucu Admin SDK erişimi ve istemci entegrasyon testleri geçer.
8. Ancak tüm kabul kriterleri tamamlandıktan sonra hedef kurallar
   Firebase Console'a **manuel ve onaylı değişiklik olarak** yayımlanır.

## Yayınlama kararı

- `rollout-status.json`: `approvedForProduction=false` olarak kalır.
- `firebase.json` ve otomatik deploy işleri eklenmedi. GitHub push
  hiçbir canlı Firebase kuralını değiştirmiyor.
- Kural dosyası gizli anahtar içermez; gerçek Premium kodları, gerçek
  cihaz kimlikleri ve satın alma tokenları kaynak koda eklenmemeli.
- Firebase Realtime Database Admin SDK kuralları bypass edebildiğinden
  sunucu tarafında ayrıca App Check, yetki, kota ve veri doğruluğu
  tedbirleri gerekir.

## Kontrol edilen resmi kaynaklar

- https://firebase.google.com/docs/database/security
- https://firebase.google.com/docs/rules/insecure-rules
- https://firebase.google.com/docs/app-check
- https://firebase.google.com/docs/app-check/android/play-integrity-provider
- https://support.google.com/googleplay/android-developer/answer/9893335

## Diğer ürün öncelikleri (bu geçişten SONRA)

**Premium:** Gerçek satın alma ile açılan Market, Labirent sınırı,
özel avatar/ikon, ek görev ve aileye anlamlı ilerleme özetleri
arasında tutarlı değer teklifi. Mevcut müşteri hakları azaltılmayacak.

**Reklam:** `AdManager.CHILD_AD_DELIVERY_DISABLED=true` değişmeden
kalır. Gradle `play-services-ads:22.4.0` kullanıyor;
Families sertifikalı SDK sürümü / içerik / yaş politikası incelemesi
olmadan reklam açılmaz. Test ödül veya banner alanı ile kullanıcıya
reklam vaadinde bulunulmaz.

**Avatar:** Görsel 3B/sahne tabanlı seçici, kilit önizlemesi,
kazanılabilir aksesuarlar ve görevlerle açılan kozmetikler. Çocukları
yüksek harcamaya yönlendiren rastgele ücretli kutular, sahte kıtlık
ve zorlayıcı ödeme mekanikleri kullanılmaz.
