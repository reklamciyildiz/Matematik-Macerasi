# Ö6 — Ders Derinliği + Öğren Premium Erişimi

**8 Kasım 2026 · Android Kotlin/Compose · Yayın onayı değildir**

## Kanıtlanmış mevcut kapsam ve uygulanan strateji

Öğren, oyunlar sekmesinden ayrı bir ders kütüphanesidir. Ö6 öncesi 54 farklı giriş dersi (66 sınıf/ders gösterimi), 59 konu/temel-tekrar kartına yayılmıştı. Ö6 ile **18 yeni kavramsal derinleşme dersi** eklenmiştir. Mevcut kütüphane toplam **72 farklı kısa ders** ve **84 sınıf/ders gösterimi** sağlar. Bu, herhangi bir sınıfın bütün resmî kazanımlarının tamamlandığı anlamına gelmez.

| Sınıf | Önceki kısa ders | Ö6 derinleşme | Sınıf içi toplam | Kalıcı ücretsiz tanıtım |
|---|---:|---:|---:|---:|
| 1 | 7 | 2 | 9 | 2 |
| 2 | 9 | 2 | 11 | 2 |
| 3 | 8 | 2 | 10 | 2 |
| 4 | 8 | 2 | 10 | 2 |
| 5 | 9 | 2 | 11 | 2 |
| 6 | 8 | 2 | 10 | 2 |
| 7 | 9 | 2 | 11 | 2 |
| 8 | 8 | 4 | 12 | 2 |

### Yeni Ö6 dersleri

- **1. sınıf:** 10'a tamamlama; basit grafikte fark bulma.
- **2. sınıf:** Çeyrek; yüzlük/onluk/birlik.
- **3. sınıf:** Eşit paylaşarak bölme; çokluğun çeyreği.
- **4. sınıf:** Paydaları eşit kesirleri karşılaştırma; iki adımlı işlem.
- **5. sınıf:** Parantezli işlemler; onda birleri toplama.
- **6. sınıf:** Mutlak değer; örüntü kuralı.
- **7. sınıf:** Eş paydalı kesir toplamı; yüzde 25.
- **8. sınıf:** EKOK; tam karelerin karekökü; doğrusal ifadede değer bulma; y eksenine göre koordinat yansıması.

Her derinleşme dersi ayrı kimlik, kendine özgü çözümlü örnek, adım adım konu anlatımı, neden/nasıl soran kavrama kontrolü ve **8 değişken alıştırma sorusu** içerir. Eski giriş dersleri **4 sorudur**. Doğru yanıtlar açıklamaları otomatik gösterir. İlerleme kimlikleri ile eski soru oturumları korunur.

### Ticari erişim kararı

**Model:** Her sınıfta iki **farklı konu başlığının** ilk tanıtım dersi sonsuza dek ücretsiz; kalan dersler mevcut **Matematik Macerası Premium** ile açık. Ayrı bir abonelik, satın alma SKU'su veya ikinci lisans kontrolü yapılmaz.

- Google Play'in mevcut tüketilmeyen, **tek seferlik** ürünü ve doğrulanmış Cloudflare Premium kodları, aynı \`PurchaseManager.isPremiumSync()\` üzerinden değerlendirilir.
- Kilit yalnızca UI simgesi değildir: \`LearningPracticeSection\` ders motoru doğrudan başlatılsa da aynı erişim kontrolünü uygulamak zorundadır.
- Eski sürümde zaten **tamamlanmış** dersler bir seferlik migrasyonla korunur. Bu kayıt yeni Premium dersleri kapsamaz. Öğrenme ilerlemesi veya avatar/yıldız verisi silinmez.
- Kilitli ders kartları, konu açıklaması ve ücretsiz kapsam görüntülenebilir. Öğrenciyi satın almaya doğrudan yönlendiren otomatik akış başlatılmaz.
- Yetişkin Premium seçeneklerini **Ayarlar** bölümünden açar; mevcut Google Play satın alma/geri yükleme/kod etkinleştirme akışları yeniden kullanılır.
- Google Play ödeme sayfasındaki fiyat ve aile hesabı onayları esas alınır. Yerel kodda ikinci fiyat, otomatik ödeme veya abonelik gösterilmez.
- Play Billing/Cloudflare doğrulamalarının sunucu taraflı güvenlik kontrolleri ve iadeler dahil **gerçek Play testçisiyle** testleri henüz tamamlanmadı. Sırf JVM testleri geçti diye satış sisteminin güvenli olduğu varsayılmamalı.
- Premium kilidi APK içinde bulunan öğretim materyalini kriptografik olarak saklamaz. İleri seviye korsanlık önlemi için sunucu yetkilendirmesi ve içerik dağıtım modeli ayrıca gerekir; cihaz içi koşul yalnız normal kullanımda özellik erişimi içindir.

### Öğretim programı: resmî durum

**2026–2027:** TYMM 1, 2, 3, 5, 6, 7. sınıflar; 4 ve 8. sınıfta önceki matematik programı. MEB'in ilgili kapsam duyurusu:
- https://tegm.meb.gov.tr/www/2026-2027-egitim-ogretim-yili-taslak-cerceve-planlar-yayinlandi/icerik/1316
- https://mufredat.meb.gov.tr/Programlar.aspx

Mevcut kartlardaki kısa hedef ifadeleri özgün ürün planlama metinleridir, **resmî MEB kazanım kodlarının birebir doğrulanmış transkripsiyonu değildir**.

8. sınıfın alt alanlarını kapsayan resmî eski programda ayrıca aşağıdaki öğrenme kümeleri vardır; mevcut materyal tümünü öğretmez: üs kuralları, bilimsel gösterim, köklü ifadeyi a√b biçiminde yazma, köklülerle işlemler, olasılık ve grafik yorumlama, cebirsel özdeşlikler, eşitsizlikler, üçgenlerin genel özellikleri, eşlik-benzerlik, farklı dönüşümler, geometrik cisimlerin yüzey alanı ve hacmi.

**Pazar iddiası sınırı:** "1–8. sınıf resmî matematik müfredatı eksiksiz" veya "bütün kazanımlarda ustalaştırır" denmez. "Sınıfa göre kısa konu anlatımları, adım adım çözüm, kavrama kontrolü ve daha uzun Premium alıştırmaları" denebilir. Uzman akademik içerik incelemesi bu kod aşamasında tamamlanmadı.

## Kalite kapısı

- [x] Ücretsiz iki ayrı konu tanıtımı / sınıf erişim politikası
- [x] Tek Premium yetki kaynağı ve sınıf/oyun ilerlemesinden bağımsız erişim
- [x] Doğrudan ders motoru çağrısında da Premium kontrolü
- [x] Önceki ücretsiz tamamlamalar için tek seferlik koruma
- [x] Ö6 için 18 konuya özel içerik, kavrama sorusu ve tekrar üretilebilir soru bankası
- [x] Derinleşme oturumlarının 8 soruya genişletilmesi, eski 4 soruluk kayıtların korunması
- [x] 8. sınıf kök/EKOK/doğrusal/koordinat için soru parametrelerine bağlı native görsel
- [ ] Son commit için CI yeşil: derleme, tüm birim testleri, Android test APK derlemesi, Firebase kuralları
- [ ] Gerçek emülatörde 1–8 her sınıfta giriş ve kilit, Premium kod/Play, geri yükleme ve veri koruma
- [ ] 320/360/412dp ekran + büyük yazı + TalkBack + sessiz/offline Türkçe TTS kalite denetimi
- [ ] Google Play lisans testçisiyle satın alma, geri yükleme, iade ve iptal davranışlarının doğrulanması
- [ ] Öğretmen tarafından bütün konuların resmî kazanım kodlarına bağlanarak tek tek onaylanması
- [ ] Kalan alt kazanımlar için gerçek öğretim materyali, soru çeşitleri, erişilebilir medyalar
- [ ] AAB imza, sürüm kodu, aile politikası ve üretim Cloudflare güvenlik denetimi

**Son yayın kararı:** AAB'yi sırf bu dosya veya birim testleri dolayısıyla yayıma hazır sayma. Özellikle yeni Premium kilidini mevcut lisans testleriyle uçtan uca doğrula.
