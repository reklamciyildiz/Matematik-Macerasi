# Matematik Macerası — Cloudflare Premium Kod API (Worker + D1)

## Gerçek durum ve geçmiş (18 Kasım 2026 belgeleme düzeltmesi)

**Bu proje yalnızca hazırlık aşamasında değildir.** Eski başlık, 7 Kasım 2026'da
*ilk dağıtım yapılmadan önce* yazılmış ve takip eden kurulumlarda güncellenmemiştir.

- **7 Kasım 2026:** Cloudflare D1 oluşturuldu; gerçek `database_id`
  `wrangler.toml` dosyasına kaydedildi. Cloudflare Worker gerçek ortamda
  dağıtılıp `/v1/activate` ve `/v1/renew` akışları test edildiği önceki
  operatör testlerinde raporlandı. 1. ve 2. cihaz `ACTIVE`, 3. cihaz
  kapasite aşımında HTTP 400 sonucunu verdi.
- **7–8 Kasım 2026:** Firebase'deki eski Premium kod/aktivasyon kayıtları
  için D1 geçişi üzerinde çalışıldı. 8 Kasım'daki kullanıcı denetiminde
  **3/3 kod ve 4/4 aktivasyon** eşleşti; yetim/kota aşımı kayıtları sıfırdı.
  Bu rakamlar o günkü örneklemin sonucudur, bugünkü D1'in canlı sayımı değildir.
- **Android:** `CloudflarePremiumClient` ve `PremiumCodeValidator` üzerinden
  imzalı lisans akışı uygulanmıştır. `MM_USE_CLOUDFLARE` build-time bayrağı
  **yerel `gradle.properties` ile açılır**. Proje varsayılanının `false`
  olması yalnız yapılandırılmamış/debug/CI derlemelerini güvenli tutmak içindir;
  Worker'ın kurulmadığı veya gerçek cihazda kapalı olduğu anlamına gelmez.
- **Play release:** `app/build.gradle` içindeki release kapısı,
  `MM_RELEASE_PREMIUM_MODE=cloudflare` ve `MM_USE_CLOUDFLARE=true`
  olmadan AAB üretimine izin vermez. Yalnız kod değil, eski hak aktarımı
  ve geri yükleme için bağımsız QA işaretleri de aranır.
- **Bağımsız sistem:** Google Play'in aylık/yıllık **abonelik doğrulaması**
  ayrı `commercial/play-subscriptions` Worker'ıdır. Cloudflare **Premium kod**
  API'sinin kurulmuş olması, aboneliklerin canlı satın alma/iptal/restore
  testlerinin tamamlandığını kanıtlamaz.

**Doğrulama sınırı:** Bunlar Git geçmişi ve önceki operatör testlerinin
kanıtladığı *geçmiş* durumdur. Bu README'nin güncellenmesi bugünkü Worker
erişilebilirliğini, Cloudflare secret'larının güncel hâlini, D1'in canlı
veri sayısını, gerçek Play alışverişlerini veya bütün eski APK'ların
geri yüklenmesini yeniden test etmez. Tekrar doğrulama gerekiyorsa salt
okunur kontrol ve güvenli smoke testi yapılmalı; secret'lar, Firebase
kayıtları veya Premium yetkileri rastgele sıfırlanmamalıdır.

Aşağıdaki **Aşama 1–5** adımları geçmişte kullanılan *kurulum/kurtarma
talimatlarıdır*. Mevcut canlı Worker veya D1 üzerinde bu adımları sıfırdan
tekrar çalıştırmayın. Özellikle `generate-secrets.mjs` ile yeni anahtar
üretmek mevcut imzalı lisansları geçersiz kılabilir.

Firebase Spark veya Google Play Billing ayarları bu klasörün okunmasıyla
değişmez. Eski APK'lar RTDB'ye erişebiliyorsa tam Firebase kilitleme
kurallarını göç ve cihaz doğrulaması olmadan yayımlamayın.

## Mimari

- `POST /v1/activate`: tek seferlik kod + cihaz kimliği, D1'de atomik kontenjan
  ve aktivasyon kaydı, RSA ile imzalı yerel Premium lisansı.
- `POST /v1/renew`: uygulamanın sakladığı **imzalı lisans + aynı cihaz kimliği**.
  Kod kullanıcıya yeniden sorulmaz. İlk yenileme 60 gün sonra, çevrimdışı
  tolerans 7 gündür. Daha sonra da imzalı eski token ile *çevrimiçi* yenilenebilir.
- `GET /health`: çalışabilirlik kontrolü, **D1 veya secret'ları test etmez**.
- D1'de gerçek kod, ham cihaz kimliği ya da kullanıcı kişisel verisi tutulmaz;
  sadece Worker'ın gizli HMAC anahtarıyla üretilmiş kod/cihaz özetleri saklanır.
- 30 istek/dakika/IP aktivasyon ve 120 istek/dakika/IP yenileme sınırı vardır.
  Cloudflare'ın rate-limit binding'i yaklaşık ve bölgeseldir; güçlü kimlik
  doğrulama değildir. Cihaz ID'si de köklü cihazlarda taklit edilebilir.
- Google Play Billing hakları ayrıdır, mevcut satın alımlar korunmalıdır.
- Worker'ın PRIVATE RSA key'i ve CODE_PEPPER **yalnız Cloudflare Secrets**'ta
  saklanır. Android yalnızca PUBLIC RSA key içerir.

## Aşama 1 — Cloudflare Free hesap ve D1 (senin işlemlerin)

1. [Cloudflare Dashboard](https://dash.cloudflare.com/) → hesap oluştur / giriş yap.
   Workers Free planını seç; ücretli plana geçmen gerekmiyor.
2. Windows bilgisayarında PowerShell, projenin kök dizininden:

   ```powershell
   git pull --ff-only origin main
   cd commercial/cloudflare-premium
   npm install
   npx wrangler login
   npx wrangler d1 create matematik-macerasi-premium
   ```

3. Komuttaki `database_id` UUID'sini `wrangler.toml` dosyasındaki
   `REPLACE_WITH_YOUR_D1_DATABASE_ID` yerine gir. UUID sır değildir.
4. D1 şemasını yükle:

   ```powershell
   npx wrangler d1 migrations apply matematik-macerasi-premium --remote
   ```

**Not:** Değişiklikler `feat/cloudflare-premium-license` dalındaysa,
ilk adımda `git fetch origin` ve `git switch feat/cloudflare-premium-license`
kullan; kodlar main'e alınmadan `git pull main` yeterli değildir.

## Aşama 2 — Gizli anahtarlar ve Workers deploy (BUNLARI BANA GÖNDERME)

Windows PowerShell üzerinde proje içindeki
`commercial/cloudflare-premium` klasöründeyken:

```powershell
git pull --ff-only origin main
node scripts/generate-secrets.mjs
npx wrangler deploy --secrets-file private/secrets.json
```

- Bu script Node.js'in kendi kripto araçlarıyla RSA-3072 özel/açık anahtarı ve
  256-bit CODE_PEPPER üretir. Gizli değerleri terminale yazdırmaz.
- `private/` gitignore kapsamındadır; yine de ayrı bir **şifreli, güvenli
  yedek** al. Klasörü paylaşma, GitHub'a koyma, sohbetlere gönderme.
- Deploy komutu bütün secret'ları kaynak kodla **aynı dağıtımda** Cloudflare'a
  yükler. Ardışık `secret put` komutlarına veya OpenSSL kurulumuna gerek yok.
- Başarılı dağıtımın `https://...workers.dev` adresini kaydet.
- `npx wrangler secret list` yalnız **secret isimlerini** listeler;
  gizli değerlerin kendisini göstermez.
- `https://...workers.dev/health` yalnız Worker erişimini kontrol eder.
  D1 tablolarını veya gerçek Premium aktivasyonunu doğrulamaz.
- Aynı secret dosyası / anahtarlar kaybolursa mevcut kod özetleri ve
  imzalı lisanslar geri döndürülemez; gelişigüzel yeniden üretme.

Daha sonra yerel yönetim scriptlerini çalıştırırken:

```powershell
$env:CODE_PEPPER = (Get-Content -Raw private/code-pepper.txt).Trim()
```

Bu satırda CODE_PEPPER'ı **ekrana bastırma**. Veritabanına kod eklemeden
önce aktivasyon ve kurtarma akışını test et.

## Aşama 3 — D1 kod oluşturma

```powershell
New-Item -ItemType Directory -Force private | Out-Null
node scripts/prepare-code.mjs create --uses 2 --institution Pilot --sql private/first.sql
npx wrangler d1 execute matematik-macerasi-premium --remote --file private/first.sql
```

Konsolda çıkan `MM-...` kodunu yalnızca kendin sakla. D1'e sadece
kodun HMAC özeti yüklenir. Kod iptali için:

```powershell
node scripts/prepare-code.mjs revoke --code MM-ORNEK-KOD --sql private/revoke.sql
npx wrangler d1 execute matematik-macerasi-premium --remote --file private/revoke.sql
```

`revoke` **mevcut aktivasyonları da** 60 gün + 7 gün içinde etkileyecektir;
bir sonraki başarılı online yenilemede daha erken reddedilir.
Bir kodun yeni aktivasyonlarını durdurmak ama mevcut hakları korumak için
ayrı veri alanı/politika eklenmesi gerekir.

## Aşama 4 — Eski Firebase Premium kullanıcıları (ZORUNLU)

- Firebase RTDB'nin `premium_codes` ve `code_activations` yollarını
  **özel, erişimi kısıtlı bir JSON yedeğine** al. Buraya yükleme.
- Eskiden anonim yazılabildiği için mevcut `used_count`, kod aktifliği
  ve cihaz aktivasyon kayıtları **güvenilir kabul edilemez**: gözle denetle,
  hatalı kayıtları düzelt, test kaydı/şüpheli kodu ayrı tut.
- Sonra (kendi bilgisayarında):

  ```powershell
  node scripts/import-firebase-export.mjs private/firebase-backup.json private/import.sql
  ```

  Aynı CODE_PEPPER ortam değişkeniyle çalışmalıdır. Import SQL'ini önce test
  D1'de doğrula; **canlıya körlemesine uygulama**. `used_count` ile
  aktivasyon sayısını denetle, farklı cihazlarda hak kaybı olmadığını doğrula.
  Mevcut kodlar için D1 boş değilse import UNIQUE hata verecektir;
  test ve production verilerini karıştırma.

## Aşama 5 — Android (sadece doğrulamalardan sonra)

- `private/license-public.pem` dosyasındaki BEGIN/END satırları **hariç**
  bütün Base64 satırlarını tek satır yap. Public anahtar gizli değildir.
- Git'in izlemediği proje kökü `gradle.properties` dosyasına:

  ```properties
  MM_CLOUDFLARE_URL=https://matematik-macerasi-premium.<hesap>.workers.dev
  MM_CLOUDFLARE_PUBLIC_KEY_B64=<public PEM Base64 body>
  MM_USE_CLOUDFLARE=true
  ```

- `./gradlew :app:assembleDebug` ile derle. İlk kez kodla aktivasyon ve 60.
  gün renewal senaryolarını test et. Eski bir cihazda kodun **tekrar
  girilmeden** D1 lisansına yükseltildiğini doğrula. İki Google Play satın
  alımını ve restore akışını fiziksel cihazda yeniden test et.
- Cloudflare modu etkinse Android Premium yetkisi yalnız geçerli
  sunucu-imzalı lisansa bakar; eski Firebase local boolean'ı tek başına
  Premium vermeye devam etmez.
- Cloudflare hazır/deploy edilmiş olsa bile eski APK'lar RTDB'ye
  erişmeye devam eder. Eski APK sürümleri zorunlu güncellenmeden ve yeni
  sürüm/restore testlerinden önce `realtime-database.lockdown.rules.json`
  yayınlamak **kullanıcıların erişimini bozabilir**.

## Testler

```powershell
node --test tests/*.test.mjs
```

Bu testler çekirdek lisans protokolünü ve D1 sorgularını bellek
taklidiyle kontrol eder; Cloudflare'ın gerçek D1 / Android / eski RTDB
verisi ile canlı entegrasyon testinin yerini **tutmaz**.

**Güvenlik sınırı:** Bu çözümün tek başına cihazı kriptografik olarak
kanıtlamadığını unutma: Android cihaz ID'si bir hesap veya güvenilir cihaz
kimliği değildir. Çalınmış token+ID, değiştirilmiş cihazlar ve eski
APK'lar ayrıca risk olarak değerlendirilmelidir. Ebeveyn hesabı zorunlu
tutulmadığından güvenli cihaz değişimi/kurtarma süreci ayrıca tasarlanmalı.
