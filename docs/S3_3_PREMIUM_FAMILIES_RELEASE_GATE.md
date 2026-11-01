# S3.3 — Free/Premium, ebeveyn satın alma kapısı, geri yükleme ve Play yayın güvenliği

**Durum (1 Kasım 2026):** Kodlama ve Android CI kabulüne sunuldu. **Play Store üretim yayımlama onayı verilmedi.** Canlı Play Console, ödeme hesabı, uygulama imzası ve saha testleri olmadan bunların başarılı olduğunu iddia etmeyin.

## Tek ürün, tek hak

Mevcut `PurchaseManager` ve değişmeyen **tek seferlik, tüketilmeyen Google Play INAPP** ürün kimliği `matematik_macerasi_premium` kullanılır. Yeni abonelik, ürün kimliği, ayrı billing servisi, ikinci premium preference'ı veya ek ödeme sağlayıcısı oluşturulmadı. Eski Premium kod etkinleştirmeleri `PremiumCodeValidator` ile aynı kalır.

| Yetenek | Free | Premium |
| --- | --- | --- |
| Pazar Simülatörü | 3 mağaza / 12 karar | 10 mağaza / 40 karar |
| Pazar rafları | Mağaza başına katalogdan 6 ürün | Aynı çeşitlilik + ilave mağazalar |
| Kendi Tezgâhım | Kapalı | Hayalî parayla stok, satış, kâr ve geliştirme |
| Öğrenme raporu | Dört oyun becerisinin temel raporu | Aynı rapor; önceki profilin ek istatistikleri |
| Avatarlar | İlk 6 avatar | 7–15 numaralı özel avatarlar **yıldızla ayrıca açılır** |
| Uygulama ikonu | Varsayılan | Robot / Uzay seçenekleri |
| Matematik Labirenti | Mevcut günlük limit | Sınırsız |
| Günlük bonus | Standart ödüller | Mevcut Premium yıldız bonusu |
| Reklam | **Geçici olarak kapalı** | **Geçici olarak kapalı** |

Bu tablo, ücretlendirmeden önce anlaşılır biçimde Ayarlar'da gösterilir. `PremiumFeaturePolicy` tek paylaşılan erişim matrisidir ve Pazar mağazaları onun sayılarını kullanır. Oyun skorları ve kayıtları ayrı korunur.

**Dikkat:** Kullanıcı dilinde "Premium üyelik" denilse de mevcut Play ürünü **abonelik değil, tek seferlik kalıcı erişim ürünüdür**. Play Console'daki ürün tanımı ve mağaza fiyatı doğrulanmalıdır.

## Ebeveyn kapısı ve satın alma

- `PurchaseManager.buyPremium(Activity)` artık **tek merkezi satın alma kapısıdır**. Settings, Profile, Market, Avatar Store gibi ekranlar aynı fonksiyona gider.
- Doğrudan `launchPurchaseFlow` artık `private`. Çocuk yanlışlıkla ödeme sayfasına ulaşmasın diye önce değişken çarpma sorusu ve ayrı yetişkin onay kutusu olan dialog gösterilir. Vazgeç işlem başlatmaz.
- **Bu matematik testi gerçek yaş/kimlik doğrulaması değildir.** Amacı istem dışı tıklamayı engellemektir; Google Play Family Link satın alma onayları ve Play ödeme kimlik doğrulaması ayrıca çalışmalıdır. Çocuğa para harcamayı teşvik eden yanılgılı ödül/geri sayım ekranı eklenmez.
- Premium etkinse `buyPremium` yeniden ödeme başlatmaz. Eski Pazar'daki sabit 17×8 kapısı kaldırıldı.
- `Purchase.PurchaseState.PENDING` için Premium verilmez; yalnız `PURCHASED` durumu hak kazandırır ve onaylanmamış satın almalar `acknowledgePurchase` ile bildirilmeye devam eder.
- **Önemli güvenlik sınırı:** Bu uygulama şu anda satın alma tokenlarını Google Play Developer API kullanan **bağımsız güvenilir backend üzerinde doğrulamıyor**. Client-only doğrulama manipülasyona açıktır. Ölçekli ticari yayımlama için sunucu doğrulaması, tekrar kullanım kontrolleri ve güvenilir hak kaydı ayrı zorunlu kabul kapısıdır. Acknowledge işlemleri Play belgesindeki süre içinde doğrulanmalıdır.

## Geri yükleme

Ayarlar → **Satın Alma ve Geri Yükleme → Satın Alımları Geri Yükle**

1. İşlem yeni satın alma başlatmaz, ücret çekmez, ebeveyn kapısı istemez.
2. Google Play Billing INAPP ürünleri tekrar sorgulanır; kayıtlar arasında doğru ürün `PURCHASED` ise hak yeniden yüklenir. Onay gerektiren kayıt için acknowledge tetiklenir.
3. Premium kodu da değerlendirilir; yalnız Play satın alımı yok diye etkin Premium kodu kaybolmaz.
4. Yükleniyor / Play bulundu / kodla Premium / satın alma bulunamadı / bekleyen ödeme / bağlantı hatası durumları kullanıcıya açıklanır.
5. **Play bağlantı hatası önceki cache hakkını silmez**; başarılı Play sorgusunda gerçek sahiplik durumu güncellenir.
6. Google Play ürün geri yükleme testinin doğru Google hesabı ve Play dağıtımlı, doğru imzalı build üzerinde yapılması gerekir; yerel APK ile gerçek ödeme güvenilir şekilde sınanamaz.
7. Kodsuz Free, tek seferlik satın alım, farklı cihazda geri yükleme, iptal, PENDING→PURCHASED, çevrimdışı kullanım, kod kullanımı ve Play oturum değiştirme ayrı QA senaryolarıdır.

## Families ve reklamlar: **kritik yayın kapısı**

Google Play'in çocukları hedefleyen uygulamalara yönelik Families politikaları, reklam SDK sertifikasyonu, reklam biçimi/içerik sınırları, veri uygulamaları ve doğru Play Console beyanları gerektirir.

Bu sürümde:
- `AdManager.CHILD_AD_DELIVERY_DISABLED = true` ile banner, otomatik interstitial, ödüllü reklam istekleri/oynatımı kapatıldı.
- `MatematikMacerasiApp` başlangıcındaki `MobileAds.initialize` çağrısı çıkarıldı.
- Manifest'te `AD_ID` ve `ACCESS_ADSERVICES_AD_ID` izinleri kaldırılmaya devam edilir.
- Reklam yöneticisi artık açılışta otomatik reklam yüklemez. Premium'un reklam görmemesi ilkesi kalır, fakat şu anda **Free oyuncu da reklam görmez**.
- **Uygulamada Ads SDK dependency hâlâ bulunuyor.** Bazı SDK/ContentProvider'lar otomatik başlatılabildiğinden, Google Play'e gönderilecek **birleştirilmiş release manifestinde**, ağ trafiğinde ve veri güvenliği beyanında bu durum ayrıca doğrulanmalı. Bu CI, SDK sertifikasyonu veya politika uygunluğu belgesi yerine geçmez.
- Reklamlardan gelir elde etmek istenirse, reklamları tekrar açmadan önce Families Self-Certified Ads SDK uygunluğu ve çocuklara ilişkin tüm koşullar ayrı bir revizyonla doğrulanmalıdır.

## Release kalite kontrolü

- `.github/workflows/android-debug-ci.yml`: `scripts/check_s3_3_release_preflight.py` ve mevcut görsel/ayar kontrolleri + Android/JUnit derleme.
- `BillingTestActivity` manifest'te `android:enabled="false"`; üretim uygulaması içinde test ödeme butonları açık bırakılmaz.
- Android `targetSdk 36`, Billing 8.0.0 ve legacy package + SKU korunur.
- Mevcut `versionCode=14`, `versionName=1.2.1` **otomatik yükseltilmedi**; Play Console'daki son kullanılan sürüm kodu bilinmeden rastgele artırmak doğru değildir.
- CI yalnız **debug** derler ve Firebase için **sahte/derleme amaçlı** yapılandırma kullanır. CI APK'sı kurulamaz/yayınlanamaz; yayın için doğru imza, Firebase ortamı ve test edilmiş **release AAB** gerekir.
- Play Console'da: uygulama erişim bilgileri, hedef kitle/çocuklar, Data Safety, reklam bildirimi, içerik derecelendirmesi, gizlilik politikası URL'si, IAP ürün ayrıntısı, dahili test / kapalı test, cihaz uyumluluğu, ekran görüntüleri, Families uygunsuzlukları ve yayın statüsü ayrı ayrı denetlenmeli.

### Telefon kabul senaryosu (Play test sürümünde)

1. **Free** yeni cihaz: 3 mağaza açık, 7'si Premium; tezgâh kapalı; üç ücretsiz avatar dışı kilitler tutarlı (ilk 6 avatar yıldız gerektirebilir). Öğrenme raporu ücretsiz.
2. **Premium** mevcut cihaz: 10 mağaza, tezgâh, robot/uzay ikonu, ücretli avatarların yıldızla kilit açması; hiçbir hak tekrar satın alım istememeli.
3. **Satın al:** Settings, Profile, Market, Avatar Store üzerinden satın alma çağrısı her seferinde ebeveyn kapısına gider. Yanlış sonuç veya onaysız deneme Google Play'i açmamalı; doğru sonuçtan sonra gerçek Play ödeme kimlik doğrulaması devam etmeli.
4. **Geri yükle:** Satın alım olan test hesabı → Play bulundu. Diğer hesap → bulunamadı. Premium kod kullanıcıları → kod etkin. Uçak modu → bağlantı hatası, eski cached Premium silinmemeli.
5. **Bekleyen ödeme:** Beklemedeyken hak yok; Play `PURCHASED` dönmeden açılmamalı.
6. **Eski kayıt:** market/geometri/öğrenme ilerlemesi, yıldızlar, avatar seçimleri, ikon tercihleri ve günlük bonuslar kaybolmamalı.
7. **Reklam:** Free dahil tüm oyunlarda banner, geçiş, ödüllü reklam **gösterilmemeli**; güvenlik kapalı olduğunda çocuklara ödüllü reklam vaadiyle yanıltıcı seçenek bırakılmamalı.
8. **AAB:** Release imzası, gerçek Firebase bağımlılıkları, Play license tester ve güncel veri/gizlilik belgeleri tamamlanmadan üretime gönderme.

## Dış doğrulama gerektiren açık kabul kapıları

- [ ] Gerçek Play Console ürün durumu, aile politikası ve yayın engelleri kontrol edildi.
- [ ] AAB release imzalandı, birleştirilmiş manifest ve cihaz ağına çıkan SDK'lar denetlendi.
- [ ] Çocuk hedefli veri güvenliği/gizlilik politikası ve SDK sertifikasyonu doğrulandı.
- [ ] Play üzerinden satın alım, restore, bekleyen işlem, yeniden açılış ve Premium kod senaryoları test edildi.
- [ ] Ticari release için server-authoritative purchase token validation ve saklama stratejisi tamamlandı veya sınırlamalar risk değerlendirmesiyle açıkça kabul edildi.
- [ ] Ekran ve erişilebilirlik testi, velilerin yanlışlıkla satın alma riski, 320–360dp, büyük font, çevrimdışı kontrol edildi.
- [ ] Release için yeni `versionCode`, mağaza metinleri, testçi erişimleri, imzalama ve rollout planı onaylandı.

**Bu maddeler tamamlanmadan "Play Store yayın sürümü tamamen hazır" denmemelidir.**
