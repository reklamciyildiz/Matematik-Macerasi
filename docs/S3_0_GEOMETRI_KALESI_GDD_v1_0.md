# S3.0 — Geometri Kalesi: Kayıp Kristaller | GDD v1.0 (tasarım önerisi)
Tarih: 2026-11-01 · Durum: **GDD taslağı / teknik uygulama öncesi tasarım kabulü**

## 1. Ürün kararı ve kapsam

- **Mevcut kart kullanılır:** `Şekiller & Geometri`. Yeni oyun kartı açılmaz.
- Oyun içi başlık: **Geometri Kalesi — Kayıp Kristaller**.
- Android native Kotlin / Jetpack Compose. Tek cihazda çevrimdışı, dış hesap ve ağ zorunluluğu olmadan.
- Pedagojik başlangıç hedefi: 1–3. sınıfın farklı basamakları; 4–8. sınıflarda otomatik "MEB kazanımını karşılama" iddiası yoktur. Üst sınıf öğrencisi tekrar olarak oynayabilir.
- Tek bir oyun haritası, 4 farklı **etkileşimli mekanik**, yaklaşık 8–12 dakika ilk hedef seans süresi. Süre başarıyı belirleyen zorunlu kısıt değildir.
- Var olan Sayı Avcıları yıldız, checkpoint, Premium ve reklam anahtarları değiştirilmez; bu oyun `geometry_castle_v1` adlı ayrı bir kayıt alanıyla başlar.
- "Şekil Tanıma" eski boş Activity'si ikinci bir bağımsız oyuna dönüşmez; katalog karışıklığını önlemek için mevcut geometri kartı tek kanonik giriş olur.

## 2. Oyunun anlatısı (ara sahneler 5–8 saniye, atlanabilir)

Gökyüzündeki Geometri Kalesi'nin dört odasına güç veren kristaller dağılmıştır. Çocuk, minik taş-golem rehber **Piko** ile kaleyi yeniden canlandıran tasarımcı rolündedir. Piko doğru cevabı söylemez; gözlem yapmaya ve başka bir yöntem denemeye yönlendirir. Her oda sonrası kalenin bir bölümü haritada fiziksel olarak onarılmış görünür. Finalde kale tamamlanır; kısa kutlama ve öğrenme özeti gösterilir.

Görsel tema: mevcut parlak mavi fantastik ada evreni ile tutarlı, altın mimari + turkuaz/buz-mavisi kristal enerjisi + pastel mercan/menekşe vurguları. Karanlık/ürkütücü zindan yok; çocukların kavrayabileceği net silüetler.

## 3. Dört farklı oyun mekaniği

### 3.1 Şekil Avlusu — Keşfet ve sınıflandır
- Sahne: kale avlusunda pencere, çatı, sancak, tekerlek, taş gibi **nesneler** var.
- Oyuncu görev kartındaki ölçüte uyan şekilleri dokunarak seçer: "Üç köşesi olanları bul", "Yuvarlak olanları ayır", "Aynı şekli farklı yönde tanı".
- Nesnenin rengi, boyu, dönüşü yanıltıcı olabilir; matematiksel özellik doğru çözüm ölçütüdür.
- **Temel:** üçgen, kare, dikdörtgen, çember; en fazla 3 seçenek, özellikler görsel ve sözel.
- **Gelişen:** dönüşmüş/ölçeklenmiş şekiller, daire-çember farkına basit örnekler, 4–5 seçenek.
- **Ustalık:** farklı dörtgenleri ve 5–8 kenarlı çokgenleri kenar sayısına göre sınıflandırma, dikkatli terminoloji.
- Yanlışta: kırmızı başarısızlık patlaması değil; doğru kenarları kısa renkle vurgulama. İpucu: bir kenarı sayma/işaretleme.
- Ölçülen davranış: `shape_classification`, doğru seçim, yanlış seçim, ipucu, ilk deneme.

### 3.2 Köprü Atölyesi — Şekillerle inşa
- Kale köprüsündeki silüet yuvaları **gerçek şekil parçalarıyla** doldurulur. Çocuk parça seçip yuvaya yerleştirir; uygun düzeyde 90° döndürme, geri al ve değiştir işlemleri vardır.
- **Temel:** 3–4 büyük şekil, belirgin eş konturlar ve sürükle/bırak alternatifine ek olarak **dokun-seç + dokun-yerleştir**.
- **Gelişen:** iki küçük parça ile bir model oluşturma; kare/dikdörtgen/üçgen kompozisyonları.
- **Ustalık:** birden çok geçerli çözümü olan 6–8 parçalı desen; tüm parçaları tek tek ezberlemeden planlama.
- Başarı: toplam şablon geometrisi doğru ve çakışmasız kaplandığında. 2–3 adım sonunda köprü kurulur.
- Konum denetimi deterministiktir; dönüşler, çakışma ve sınır testi yapılır. İlk sürüm fizik simülasyonu gibi sunulmaz.
- Ölçülen davranış: `shape_composition`; yanlış yerleşim, döndürme, geri alma ve ipucu ayrı sayılır. **Geri alma yanlış cevap değildir.**

### 3.3 Ayna Kulesi — Simetriyi keşfet
- Kale vitrayının bir yarısı kaybolmuştur; oyuncu karşı tarafı oluşturur.
- **Temel (1. sınıf tekrarı):** eşini bulma/görsel karşılaştırma; "simetri doğrusunu çiz" gibi üst düzey görev veya resmî kazanım iddiası YOK.
- **Gelişen (2. sınıf):** simetrik olan/olmayan düzenekleri ayırt etme; basit tek dikey eksende karşı parçayı yerleştirme.
- **Ustalık (3. sınıf):** yatay/dikey eksende kareli ızgaradaki eş hücreleri karşı tarafa tamamlama; dinamik 4×4–6×6 çalışma alanı.
- Çözüm algoritması: eksen ile kaynak hücrelerin ayna koordinatları kıyaslanır. Renkler tek başına ipucu olmaz; desen/konum etkilidir.
- İpucu: yalnızca **bir** eş hücrenin konumunu gösterir; bütün çözümü otomatik doldurmaz.
- Ölçülen davranış: `symmetry_recognition` ve uygun düzeyde `symmetry_construction`. Bunlar ayrı becerilerdir.

### 3.4 Taç Salonu — Kalenin Büyük İnşası (final)
- Çocuk, önceki üç mekanikten kazandıklarıyla tacın / ana salonun son geometrik desenini tamamlar.
- Tek bir "hangisi doğru?" sorusu değildir. **3 aşamalı büyük yapı**: uygun şekli seç, doğru yuvaya yerleştir, son desenin eşini veya simetrisini tamamla.
- Temel düzeyde son kısım görsel eşleştirmedir; gelişen/ustalıkta uygun simetri veya uzamsal ilişki uygulanır.
- Tamamlandığında kalede **gerçek çevresel dönüşüm** olur: ışıklar yanar, eksik vitray tamamlanır ve köprü açılır; yalnızca bir "Tebrikler" modalıyla bitmez.
- Finalde ödül ilk kez **tamamlanma** için bir kez işlenir; tekrar oyunlarda yıldız suistimali önlenir.

## 4. Zorluk, ilerleme ve tekrar

### Zorluk
- Yaş/sınıf ilk öneridir, kesin sınır değildir. Üç kademe: `TEMEL`, `GELISEN`, `USTALIK`.
- 1. sınıf için başlangıç `TEMEL`; 2. sınıf için `GELISEN` ihtiyatlı öneri, öğretmen/ebeveyn manuel seçebilir; 3. sınıf için `USTALIK` ancak otomatik artış performansla doğrulanmalı.
- Her oda alt görevleri seviyesine göre değişir, yalnızca sayıları büyütmek **adaptif öğrenme sayılmaz**.
- Uygun yaşta olmayan kavramı yükseltme uğruna zorunlu kılma. Daha küçük çocuk ve erişilebilirlik için yardım modu ayrı olabilir.

### Tekrar oynama
- Her oda en az 10 **önceden doğrulanmış** varyant/şablon havuzuna sahiptir (artırılabilir); seed ile deterministik seçilir.
- Aynı oturumdan çık-gir veya cihaz döndürmede seed / mevcut adım korunur. Yeni oturumda farklı görev sırası ve görsel kombinasyonları üretilir.
- Yanlış öğrenilen bir **beceri** sonraki oturumda farklı örnekle tekrar gelir; cevap ezberleten aynı soru zorla tekrar gösterilmez.
- Her oda bitince oyuncu kısa pekiştirmeyi isterse açabilir; zorunlu arka arkaya 20 çoktan seçmeli soru yok.
- İpucuyla çözülen soru "tam bağımsız ilk deneme" sayılmaz; yine de oyuncu ilerleyebilir.

### Öğrenme kanıtı
- Beceriler: `geometry_identify`, `geometry_classify`, `geometry_compose`, `geometry_symmetry_recognize`, `geometry_symmetry_build` (uygun yaşta).
- Her görev: `runId`, `seed`, `tier`, `skillId`, `taskId`, `attempts`, `errors`, `hints`, `independentFirstTry`, `completed`, `duration`.
- Eski oturuma tekrar girme mükerrer ilerleme/ödül oluşturamaz. Günlük görev yalnızca tamamlanmış oturumu bir kez sayar.
- Raporda "şekil tanıma alıştırmaları: x/y", "simetri görevlerinde destek gereksinimi" gibi gözleme dayalı dil. "Tüm kazanımı öğrendi" denmez.
- Geometri için ayrı `AdventureSkill` kayıtları, 3'er/4'er saymanın otomatik seviyesini yanlışlıkla etkilemez.

## 5. Ekran akışı ve mobil UX

`Şekiller & Geometri` kartı → Kale haritası (4 oda) → Oda hedefi (kısa) → Tam ekran etkileşimli puzzle → Kısa, neden-sonuç açıklaması → Oda onarımı → Sonraki oda → Kale finali / gözleme dayalı rapor → Tekrar / haritaya dön.

- Her ekranda **en fazla bir** atmosferik dekoratif ana görsel. Girişte büyük kale panoraması; görevlerde küçük oda sahnesi + görünür oyun tahtası. Antik Tapınak'taki çift hero sorunu tekrarlanmaz.
- Ekranın ilk görünür alanında gerçek oynanış bulunur; uzun başlıklar, iki büyük görsel ve üç CTA üst üste olmaz.
- Büyük dokunmatik alan hedefi **≥48dp**; uzaktan anlaşılır kontrast, farklı renk körlükleri için sembol + renk, konuşma gerektirmeyen kullanım.
- Drag & drop **opsiyonel**, dokun-seç/dokun-yerleştir **eşdeğer işlevli**; hiçbir görev çok hassas parmak sürükleme gerektirmez.
- Kapat/aç, Android geri, erişilebilirlik, ses kapama, azaltılmış hareket tercihi, küçük ekran (320–360dp), büyük ekran / tablet.
- Çocuk oyunda kaybetme ekranına takılmaz; geri alma ve düşük tehditli ipuçları; satın alma/reklam oda ortasında gösterilmez.

## 6. Görsel asset ve animasyon sözleşmesi

**Önce görseller, sonra kaynak entegrasyonu.** ChatGPT tarafından tek biçemli asset seti oluşturulur; kod ajanı görselleri "improvize" etmez. Stil, ana Sayı Avcıları evreninin parlak mavi fantastik ada tarzını korur.

- WebP önerisi: 1 kale giriş/harita panoraması; 4 ayrı oda sahnesi; 1 tamamlanmış kale panoraması. Gözle anlaşılır 2.5D derinlik / tutarlı ışık yönü.
- Ayrı şeffaf WebP veya Android VectorDrawable: Piko rehberi (3 ifade), geometri şekil taşları, köprü modülleri, vitray karo seti, 4 kristal ve dekoratif tamamlanma efekti.
- Geometrik çözüm doğruluğu bitmap üstüne gömülü değildir; **oyun tahtası Kotlin Canvas/Compose vektörleri** ile çizilir. UI elementlerinin odadaki gerçek konumu programatik ve deterministiktir.
- Animasyon: oda girişi (200–350ms), doğru parça snap (120–220ms), ayna eşlenme (200–400ms), kale tamamlama (yaklaşık 1.5–2sn, geçilebilir).
- Sürekli parıltı/pulse hareketi kullanıcı "azaltılmış hareket" seçtiğinde durur. Dekoratif animasyon oyunun doğruluğunu belirlemez.
- Tek pakette asset mapping manifesti + WebP boyut/doğrulama raporu. Sonra doğrudan GitHub `app/src/main/res` içine dosya ve Kotlin entegrasyonu.

## 7. Teknik entegrasyon

- Yeni tek giriş `GeometryCastleActivity`, oynanış `GeometryCastleScreen`; ekran, veri ve engine ayrılır.
- Saf Kotlin `GeometryCastleEngine` (soru/şablon üretimi + doğruluk denetimi) ve JUnit testleri; `GeometryCastleProgressStore` ile `geometry_castle_v1` checkpoint.
- Mevcut `GamesTabScreen.kt`, `QuickAccessGamesSection.kt`, `GameSelectionActivity.kt`, `HomeScreenNew.kt` ve eski `MainActivity.kt` yolları **tek** kanonik Activity'ye bağlanacak. Menüde Şekil Tanıma ve Geometri Macerası'nın ayrı "coming soon" kartları başsız bırakılmayacak; kanonik oyuna yönlendirilecek veya katalogdan tutarlı biçimde çıkarılacak.
- `GameType.SHAPE_RECOGNITION` günlük görev üretiminde şu an **uygulanmamış oyuna yönleniyor**. Geometri tamamlanana kadar görev havuzundan çıkarılır veya kanonik oyuna bağlanır ve ödül kaydı gerçek tamamlama ile entegre edilir.
- İlk kabul edilmeden mevcut `ComingSoonActivity` yönlendirmesi kaldırılmaz. Premium satın alma/billing ve diğer oyunların storage anahtarlarına dokunulmaz.
- Gerçek Android derlemesi, unit tests ve cihaz testi yapılmadan "release-ready" denmez.

## 8. Doğrulama & kabul kapıları

1. Üç zorlukta her odanın **en az 300 farklı seed** matematiksel doğrulama testi; yanlış çözümler kabul edilmez, geçerli bütün çözümler kabul edilir.
2. Dört oda tam oynanır; sırasıyla açılır, yanlış cevapta bilgi verici geri bildirim verir.
3. Çık-gir, uygulamayı zorla kapat, cihaz döndür, internet kapat; puzzle yerleşimleri ve tamamlanma kaybolmaz.
4. 48dp dokunma, TalkBack odak sırası, küçük ekran + büyük yazı, tablet, 60fps performans hedefi gerçek cihazda kontrol edilir; düşük cihazda gereksiz efekt azaltılır.
5. En az 3 farklı seviyeden çocukla kısa kullanıcı oturumu; öğretmen/veli gözleminde görev yönergeleri anlaşılır mı, parça seçimi kolay mı, zorlanılan yerler belirlenir.
6. Ana oyun yıldızları, görev sayaçları, reklam/billing ve önceki bölgeler için regresyon.
7. Öğrenme raporu yalnızca gerçek eylemleri gösterir; müfredat kodları sadece doğrulanmış **ilişkili alıştırma** şeklinde eklenir.

## 9. Ürün önceliği ve gerçekçi sınır

**P0:** çalışan 4 mekanik + çocuk dostu navigasyon + saf Kotlin engine/test + offline checkpoint.
**P1:** premium tutarlı art set + animasyon + ses kontrolleri + zorluk/adaptif tekrar + bölüm sonuçları.
**P2:** öğretmen/ebeveyn raporu bağlantısı ve içerik çoğaltma, yalnızca ilk iki aşama cihaz kabulünden sonra.

4× farklı etkileşimli mekanik, yüzeysel 20 "şeklin adı nedir" sorusundan daha pahalıdır ama ürün için esas ayrıştırıcıdır. Ölçülemeden genişleyen seviyeler ertelenir.

## 10. Resmî içerik kaynakları (ilişkili, tam yeterlilik iddiası yok)
- İlkokul 1. sınıf uzamsal ilişkiler/eş nesneler: https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/109
- İlkokul 1. sınıf geometrik şekilleri ayırt etme/sınıflama: https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/113
- İlkokul 2. sınıf geometri modelleri: https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/165
- İlkokul 2. sınıf simetri: https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/166
- İlkokul 3. sınıf şekil özellikleri: https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/275
- İlkokul 3. sınıf simetri: https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/284

Bu doküman oyun üretimi için kapsam ve doğrulama sözleşmesidir; henüz Kotlin oyununun üretildiği veya çocuk testinin tamamlandığı anlamına gelmez.
