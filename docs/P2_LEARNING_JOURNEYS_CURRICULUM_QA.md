# P2 — Beş Aşamalı Öğrenme Yolculukları ve Resmî Program Denetimi

**16 Kasım 2026 · Android Kotlin/Compose · GitHub içerik entegrasyonu; üretim yayın onayı değildir.**

## Kapsam, gerçek içerik ve sınır

Ö6 öncesinde 72 özgün kısa ders / 84 sınıfa göre ders gösterimi bulunuyordu. P2 bu kütüphaneye **6 yeni özgün, sınıfa özgü 8 soruluk ders** ekleyerek katalogda **78 özgün kısa ders / 90 sınıfa göre ders gösterimi** hedefler. Sayılar, kod mantığına göre beklenen envanterdir; son Android derlemesi/cihaz kabulünden önce pazarlama vaadi değildir.

Öncelik: **2–4. sınıfların belirlenmiş alt beceri boşlukları**; 1 ve 5–8. sınıflarda mevcut derslerle yolculuk planlama, 5 aşamalı öğretim akışı ve sınırsız tekrarlanabilir transfer mini oyunu. Bu genişletme, 1–8. sınıfların tüm kazanımlarını yeni içerikle doldurmak anlamına gelmez.

**Resmî program sürümü hatası önlemi:** MEB'in 2026–2027 açıklamasına göre 2 ve 3. sınıflarda TYMM, 4. sınıfta önceki matematik öğretim programı geçerlidir. 4. sınıf çıktıları TYMM koduyla sunulamaz.

- [MEB 2026–2027 uygulama ayrımı](https://tegm.meb.gov.tr/www/2026-2027-egitim-ogretim-yili-taslak-cerceve-planlar-yayinlandi/icerik/1316)
- [TYMM, 2. sınıf Sayılar ve Nicelikler 1](https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/116)
- [TYMM, 2. sınıf İşlemlerden Cebirsel Düşünmeye](https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/164)
- [TYMM, 3. sınıf İşlemlerden Cebirsel Düşünmeye](https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/264)
- [TYMM, 3. sınıf Veriye Dayalı Araştırma](https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/288)
- [MEB Matematik Dersi Öğretim Programı 2018, 4. sınıf](https://mufredat.meb.gov.tr/Dosyalar/201813017165445-MATEMAT%C4%B0K%20%C3%96%C4%9ERET%C4%B0M%20PROGRAMI%202018v.pdf)

## Tek tek incelenen, kodda uygulanan alt beceriler

| Sınıf / program | Yeni ders kimliği | Resmî kod | Kodda gerçekten uygulanan alt beceri | Kapsam kararı |
|---|---|---|---|---|
| 2 / TYMM | `g2_backward_count` | MAT.2.1.4 | İkişer, üçer, dörder, beşer geriye ritmik sayma; sınıra uygun sayılar | **KISMİ**: bütün performans, şekil örüntüsü ve açık uçlu açıklamalar değil |
| 2 / TYMM | `g2_inverse_ops` | MAT.2.2.3 | Toplama sonucunu çıkarma ile sayısal kontrol etme | **KISMİ**: tüm yorumlama stratejileri değil |
| 3 / TYMM | `g3_story_two_step` | MAT.3.2.6 | Eş gruplar ve eklemeyi içeren iki işlemli günlük yaşam problemi | **KISMİ**: problem kurma ve geniş muhakeme süreci değil |
| 3 / TYMM | `g3_data_difference` | MAT.3.4.1 | Tek veri grubunda iki kategorinin çubuk karşılaştırması | **KISMİ**: araştırma sorusu, gerçek veri toplama ve karar verme süreci değil |
| 4 / 2018 | `g4_round_tens` | M.4.1.1.4 | Dört basamaklı sayıları en yakın onluğa yuvarlama | **KISMİ**: en yakın yüzlüğe yuvarlama dâhil değil |
| 4 / 2018 | `g4_unit_fraction_compare` | M.4.1.6.2 | Paydası en çok 20 olan iki birim kesri aynı bütün üzerinde karşılaştırma | **KISMİ**: tüm sıralama durumları değil |

**Kaynak metinler kopyalanmadı:** Ders açıklamaları, ipuçları, görsel modeller ve sorular özgün ürün içeriğidir. Resmî kodlar yalnızca **kısmi beceri eşlemesidir**. Öğretmen/alan uzmanı onayı yapılmadan bir derse “resmî kazanımın tamamı karşılandı” etiketi verilemez.

### İçerikte saptanan uyumsuzluğun düzeltilmesi

Önceki `g2_hundred_place` dersinde 123 gibi 100'ü aşan örnekler vardı. Resmî TYMM `MAT.2.1.2` iki basamaklı çözümleme ve 100'e kadar sayı bağlamında çalışıyor.

P2 kapsamında:
- Aynı ders kimliği, Premium hakları, tamamlanma kayıtları ve 8 soru korunarak öğretim metni ve **yeni oturum soru üreticisi** 100'e kadar düzeltildi.
- `legacyGrade2Hundred` yolu, güncellemeden önce başlanmış ve henüz bitirilmemiş bu tek ders oturumundaki eski soruların değişmemesi için tutuldu.
- 2018/TYMM konuları birbirinin resmî koduyla etiketlenmedi.

### Denetim kapsamının dürüst sınırı

Bu aşamada 2–4. sınıfların **altı yeni alt becerisi kaynak kod düzeyinde tek tek incelenerek doğrulanmış kodlara bağlandı**. Önceden mevcut diğer kısa derslerin tamamına ait resmî alt çıktıların **satır satır öğretmen onaylı audit'i hâlen açık iştir**. Orijinal 31 adet sınıf/ders gösteriminin varlığı, o 31'inin MEB'e tam uyumunun kanıtı değildir. Bütün alt kazanımların eksiksiz kapsandığı reklam iddiası **yasaktır**.

## Çocuk deneyimi: Beş aşama

1. **Kavramı keşfet:** mevcut görsel-destekli özgün ders anlatımı ve sözlü okuma.
2. **Örneği incele:** adım adım örnek; son adımda kavram seçimi.
3. **Kendin çöz:** mevcut oturumun ilk 2 veya 6 sorusu, P1 adaptif yanlış geri bildirimi ile.
4. **Kısa değerlendirme:** mevcut oturumun son 2 sorusu; **ekstra soru gerektirmez**, 4/8 oturum kayıtları ve otomatik doğru-yanıt geçişi korunur. Bu yalnız kısa ürün içi yoklamadır, geçerli bir akademik sınav değildir.
5. **Oyunla pekiştir:** tamamlanan dersin ardından her sınıf ve derste yeniden üretilebilir **Bilgi Sandığı** tek soruluk mini oyunu; ilgili mevcut oyunla gerçek eşleşme varsa ayrıca derin bağlantı. Mini oyun gerçek yıldız/bakiye vermez, Premium iznini aşmaz veya ders tamamlama kaydını değiştirmez.

Yeni altı dersin **soru içi görselleri gerçekten üretilen soru parametreleriyle eşleşir**. Eski derslerin sabit öğretim çizimleri ile dinamik sorularının aynı sayıları göstermediği durumlar ayrı şekilde belirtilmeye devam eder.

## Yolculuklar

2–4. sınıfta her sınıf için üç uzman tarafından son kez gözden geçirilecek yolculuk:
- 2. sınıf: **Sayı Kaşifleri**; **İşlem Dedektifleri**; **Eş Parçalar Diyarı**
- 3. sınıf: **Problem Çözme Yolculuğu**; **Grafik Araştırmacıları**; **Kesir Ustaları**
- 4. sınıf: **Büyük Sayı Atölyesi**; **Kesirler Laboratuvarı**; **Çok Adımlı İşlemler**

1 ve 5–8. sınıflarda gerçek ders varlığına göre ilk üç uygun konudan kısa yollar gösterilir. Bunlar resmî yıllık konu sırasının tamamı değildir. Yolculuk her adımda eski `LearningAccessPolicy` / `PurchaseManager` denetimini yeniden kullanır; mevcut iki ücretsiz ders kuralı değişmez.

## QA ve yayın kapısı

### Kodda eklenen doğrulamalar
- [x] 6 yeni dersin resmî kısmi kodu, kaynak adresi, özgün öğretim aşaması ve benzersiz ID'si
- [x] 1–8. sınıf katalog/journey bağıntıları ve kayıp ders kimliği koruması
- [x] 2–4. sınıfta üçer yolculuk; yeni altı dersin tamamı dahil
- [x] 6 × 121 seed × 8 soru = **5808 deterministik yeni sorunun** cevap matematiğine yönelik JUnit test tanımı
- [x] Ek olarak eski 2. sınıf basamak sorusunun 100'e inmesi ve eski oturum varyantının korunmasını hedefleyen JUnit testleri
- [x] P1 adaptasyonuyla yeni soru bankalarının birlikte çalışmasını hedefleyen regresyon testleri
- [x] GitHub Actions debug derleme/test adımına yeni P2 test sınıfının eklenmesi

### Henüz cihazda doğrulanmamış, üretim öncesi zorunlu
- [ ] GitHub Actions Android debug APK ve bütün P1/P2 birim testleri yeşil
- [ ] 1–8. sınıf yolculuk açılışı ve gerçek Premium kilidi; restore, Play abonelik, Cloudflare kod
- [ ] Altı yeni dersin anlatımı, son iki değerlendirme sorusu, 8/8 tamamlanma, mini oyun tekrar denemeleri
- [ ] 320/360/412dp ekranda içerik taşması, büyük font, TalkBack, ses aç/kapa ve çevrimdışı kullanım
- [ ] Önceden yarıda bırakılmış `g2_hundred_place` sorusunun aynı kalması, tamamlanınca yeni oturumda 100'e düşmesi
- [ ] Uzman öğretmen tarafından kod ve ders/öğrenme çıktısı bazında akademik inceleme
- [ ] Google Play gerçeğiyle AAB, sürüm kodu ve yayın kanalı QA

**Karar:** P2'nin kaynak kod teslimi, tek başına Google Play üretim yayın onayı değildir. Özellikle öğretmen audit'i ve gerçek Android QA hâlen gereklidir.
