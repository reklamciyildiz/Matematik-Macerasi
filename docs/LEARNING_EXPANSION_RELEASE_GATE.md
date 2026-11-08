# Matematik Macerası — Öğren Ürün Genişletmesi / Yayın Öncesi Kanıt Kaydı

> **Ö6 ilerleme notu (8 Kasım 2026):** Bu dosya 54 kısa derslik *Ö3 genişletmesi*nin tarihsel QA kaydıdır. Yeni 18 Ö6 derinleşme dersi ve mevcut Premium hakkıyla freemium erişim eklendikten sonraki güncel kapsam **72 farklı kısa ders** olmuştur. En güncel kabul kapıları ve satış koşulları için [Ö6 — Ders Derinliği ve Premium](O6_DEEP_CURRICULUM_FREEMIUM_RELEASE_GATES.md) dosyasını temel alın. Bütün MEB alt kazanımları eksiksiz değildir.

**Öğretim yılı:** 2026–2027 · **Ürün:** Android / Kotlin / Compose  
**Kapsam:** Ö1+Ö2+Ö3 üzerine yapılan ilk çok-sınıflı içerik, görsel model ve adaptif tekrar paketi.

## 1. Sınıfa göre gerçekten oynanabilir içerik — 8 Kasım 2026 güncellemesi

| Sınıf | Önceki kısa ders | İlk genişletme | Yeni konu girişleri | Açılabilir kısa ders |
|---|---:|---:|---:|---:|
| 1 | 2 | 3 | 2 | 7 |
| 2 | 4 | 3 | 2 | 9 |
| 3 | 3 | 3 | 2 | 8 |
| 4 | 3 | 3 | 2 | 8 |
| 5 | 2 temel tekrar | 3 | 4 | 9 |
| 6 | 2 temel tekrar | 3 | 3 | 8 |
| 7 | 2 temel tekrar | 3 | 4 | 9 |
| 8 | 2 temel tekrar | 3 | 3 | 8 |

**54 farklı kısa ders kimliği:** 8 eski ders + 24 sınıfa özgü ders + 22 yeni konu giriş dersi. Eski derslerin birden fazla sınıfta sunulmasıyla toplam **66 sınıf/ders gösterimi** vardır. 1–8. sınıflardaki **59 konu kartının** her birinde en az bir açılabilir kısa ders bulunur; dördü 5–8. sınıf temel tekrar kartlarıdır.

**Bu, 59 konu ailesinin bütün alt kazanımlarının tamamlandığı anlamına gelmez.** Bir konu başlığında tek giriş dersi olması, MEB öğretim programındaki tüm öğrenme çıktılarının işlenmesi değildir. Resmî kazanım kodları ve tam yıllık plan henüz eğitim uzmanı tarafından eşleştirilip onaylanmadı. Play Store tanıtımında "tam MEB müfredatı" denmemelidir.

Yeni içerik ile birlikte:
- 22 giriş dersinin her biri özgün bir anlatım, matematiksel model ve 4 soruluk değişken problem ailesi içerir.
- Soruya özel resimler, sabit çözümlü örneği göstermek yerine ilgili sorunun **gerçek sayılarından** üretilir; desteklenmeyen biçimler çizim uydurmadan metinle devam eder.
- Konu arama (3–8. sınıf), tamamlanmamış dersleri filtreleme, kategori renkleri, ilerleme çizgileri ve 48dp+ dokunma hedefleri eklendi.
- Yeni yansıma, dik üçgen, hacim ve açı diagramları konu anlatımını doğru görselleştirecek şekilde tasarlandı.

## 2. Özgün konuya uygun diyagram türleri

- **Nesne sayma / onluk-birlik:** Onluk çubukları, birlik nesneleri, sınıfa uygun boyut.
- **Çıkarma / para üstü:** Gerçek nesnelerden eksilenleri üstü çizili gösterme.
- **Düzlemsel geometri:** Üçgen ve dörtgenin kenarlarını gerçek kapalı şekillerle gösterme.
- **Saat:** Akrep ve yelkovanı doğru konumlara çizilmiş kadran.
- **Kesirler:** Pay/payda kadar eşit bölmeli bütün, aşamalı boyama.
- **Sütun grafiği:** Sayısal değere orantılı sütun yüksekliği.
- **Alan:** Birim kare satır/sütun ızgarası.
- **Çevre:** Dört kenarın ölçülü çevre gösterimi.
- **Ondalıklar:** Bütünler ve onda bir dilimleri.
- **Tam sayılar / ritmik sayma:** Eşit adımlı sayı şeridi.
- **Oran / cebir:** Karşılaştırmalı sütunlar ve eşitliğin iki tarafı.
- **EBOB:** İki sayının bölenlerinin ayrı ayrı ve ortak gösterimi.
- **Üslü sayı:** Tekrarlanan çarpanların görselleştirilmesi.

Çizimler **native Compose vektör/Canvas** bileşenleridir; cihaz boyutuna göre ölçeklenebilir ve öğrenme modeline dokunarak aşama ilerletir. Hazır illüstrasyon olarak uygulamanın orijinal **elma vektör varlığı** yeniden kullanılır. Yeni WebP sanat paketi veya kaydedilmiş stüdyo TTS sesi bu değişiklikle oluşturulmadı; varmış gibi sunulmaz.

## 3. Yaşa ve veriye göre yaklaşım
- **1–2:** Kısa cümleler, büyük elma/şekil illüstrasyonları, cihazda Türkçe offline TTS mevcutsa otomatik dinletme.
- **3–4:** Görsel + işlemin adım adım açıklaması, isteğe bağlı ses.
- **5–8:** Analitik açıklama ve kavramsal çizim, isteğe bağlı ses.
- Mevcut dört soruluk oturumlarda rastgele ama tekrar üretilebilir sorular; yeni dersler için ders kimliği ve oturum tohumu kullanılır.
- Tamamlama kaydı, en iyi hamle oranı, tekrar sayısı, kavrama sorusu yanlışları ve kullanılan ipuçları **cihazda** saklanır; eski oyuncu hakları değiştirilmez.
- **Tekrar önerisi:** Hata ve ipucu kullanımına bağlı olarak 1/2/3/7 gün sonra dersin tekrarını sunar. Başarıyı **ustalık kanıtı** olarak adlandırmaz.

## 3.1. 8 Kasım kalite sertleştirmesi

- **Gerçek veriyle görsel doğruluk:** Birden fazla satırda çizilen nesnelerde çıkarılan miktar global indeksle sayılıyor. 1–100 arası her nesne miktarı, 0–N çıkarma ve 1–12 arası satır genişliği için ayrı otomatik regresyon var.
- **Küçük sınıf sesli erişimi:** Konu anlatımına ek olarak çözümlü örneğin kavrama sorusu/seçenekleri ve alıştırmanın seçenekleri de Türkçe cihaz içi TTS'ye veriliyor. Sesli metin uzunluğuna göre öğretim açıklamasının bitmesi beklenip kavrama kontrolünden kısa alıştırmaya otomatik ilerleniyor. Sonraki/sıradaki butonu zorunlu değil.
- **Ses yarışları:** QUEUE_FLUSH ile eski konuşmanın onStop bildirimi yeni konuşmayı durdurmasın diye her sesli okuma ayrı benzersiz utterance ID ile kontrol ediliyor.
- **Eski kullanıcılar:** Daha önce bitirilmiş, tekrar zamanı kayıtlı olmayan derslere güvenli bir ilk tekrar önerisi sunuluyor. Başarı ve Premium hakları değiştirilmiyor.
- **Üst sınıf alıştırma görselleri:** 5–8. sınıflarda seçilmiş 20 kazanım için alan ızgarası, veri sütunu, olasılık sonuçları, negatif sayı şeridi, denklem dengesi, tekrar eden üs çarpanları, dik üçgen, prizma katları, yansıma, ortak bölenler ve nicelik oranları gibi veriye bağlı küçük modeller eklendi.
- **Görsel erişilebilirliği:** TalkBack etiketleri görselin ne anlattığını ve problemde kullanılan gerçek nesne/sayı miktarlarını içeriyor; rastgele önceki örneğin alt metni kullanılmıyor.
- **Dokunarak kesir keşfi:** Kesir anlatımının rehberli örneğinde öğrenci eş parçalara dokunarak boyayabilir veya geri alabilir; seçili pay gerçek zamanlı gösterilir. Bu bağımsız bir oyun veya ödül kapısı değil, dersin içindeki matematik aracıdır.
- **Doğru ölçek:** Dikdörtgen kenar oranları soru parametrelerinden hesaplanıyor.
- **Uygunluk:** Yaş grubuna göre görsel yoğunluğu ayarlanıyor. Cihazın offline Türkçe TTS sesi yoksa metinli deneyim devam ediyor. Kaydedilmiş profesyonel ses dosyaları dahil edilmiş değil.

### Son Android cihaz kabul senaryoları

1. 1. sınıfta 9 elmadan 6 çıkarma sorusunda tam altı nesnenin üstünün çizildiğini, kalan nesnelerin alt satırlarda yanlış işaretlenmediğini doğrula.
2. 1. sınıfta sesli anlatım açıkken kavrama sorusu ve cevaplarının okunduğunu; doğru cevaptan sonra herhangi bir butona basmadan alıştırmaya geçildiğini kontrol et.
3. Seslendirme sürerken geri dön, başka dersi aç; eski ses ve durdurma callback'lerinin yeni dersi susturmadığını doğrula.
4. 2. sınıfta yarım, 4. sınıfta kesir ve çevre; 6. sınıfta eksi sıcaklık ve cebir; 8. sınıfta dik üçgen, EBOB ve hacim görsellerini gerçek soru sayılarıyla karşılaştır.
5. Uygulamayı dersin ortasında kapatıp aç: aynı konu, aşama, soru ve ipuçları tutarlı olmalı. Sonra dersi tekrar açınca farklı oturum soruları gelmeli.
6. 320/360/412 dp ekranlarda, yazı büyütmede ve TalkBack ile seslendirme kapalı/açık durumlarını ayrı ayrı dene.
7. Mevcut ücretli hakları/oyun ödüllerini ve Cloudflare Premium'u yeniden test et; Öğren güncellemesi bunların davranışını değiştirmemeli.

## 4. Ürün kabul matrisi / yayın blokajları

### Kod doğrulama (GitHub CI)
- [x] 22 yeni konu giriş dersi, 22 soru bankası ailesi ve konu eşlemesi eklendi
- [x] 54 farklı kısa ders için açılış akışı sağlandı
- [x] Üretilen gerçek soru sayılarına bağlı küçük sınıf görselleri ve regresyon testleri eklendi
- [x] 24 ders kimliği, sınıf ve konu eşlemesi eklendi
- [x] 24 yeni hesaplama üreten sorular ve özel açıklamalar eklendi
- [x] 24 aşamalı, kavram sorulu anlatım senaryosu eklendi
- [x] Sınıfa uygun görsel modeller eklendi
- [x] Cihaz içi tekrar önerisi ve eski kayıt uyumluluğu eklendi
- [x] Eski uygulama sürümlerinin tamamlanan dersleri için tek seferlik tekrar görünürlüğü eklendi
- [x] Üst sınıflar için canlı soruya bağlı görseller ve binlerce örneği doğrulayan testler eklendi
- [x] Satır kaymasında çıkarma nesnelerini doğru sayan testler ve yaşa uygun sesli seçenek akışı eklendi
- [ ] **Son commit için CI yeşil** (kesin kanıt CI sayfasında kontrol edilecek)
- [ ] Android emülatör gerçek UI ve ses/dokunma testi
- [ ] Gerçek cihazda 320/360/412 dp ve font ölçeği / TalkBack kontrolü
- [ ] Her bir kavramın öğretmen tarafından doğrulanması (örnekler, yanlış seçenekler, yönergeler)
- [ ] MEB'e ait resmî tam kazanım kodlarının ders bazlı doğrulanmış eşlemesi
- [ ] Her konu ailesi için bütün alt kazanımlarda kapsamlı ders paketi ve ses kaydı kalite değerlendirmesi
- [ ] Çocuk hedefli Play Store izinler/SDK/Billing/Cloudflare yayın sürümü güvenlik kontrolü

**Yayın kararı:** Bu eğitim paketi cihaz ve içerik kontrolü yapılmadan Play Store'a tam müfredat iddiasıyla gönderilmeyecek. Google Play AAB, ücretli hakları ve Cloudflare release yapılandırması ayrıca onaylanmalıdır.

## 5. Yeni kod sahipleri
- LearningPathCatalog: eski dersleri koruyan katalog / soru yönlendirme
- ExpandedLessonCatalog: 24 sınıf ve konu eşleşmesi
- ExpandedLessonQuestionBank: 24 özgün, seeded sayısal soru tipi
- LessonTeachingCatalog + ExpandedLessonTeachingCatalog: kavram anlatımı ve yanlış anlama kontrolü
- LessonTeachingIllustration + AdvancedLessonIllustration + PracticeQuestionIllustration: yaşa ve soru sayılarına uygun vektörel diyagram
- TopicStarterCatalog + TopicStarterQuestionBank: 22 yeni giriş dersi, anlatım senaryosu ve değişken soru ailesi
- PracticeVisualFactory + UpperGradePracticeVisualFactory: soru metninden güvenli görsel model kurma, bilinmeyen şablonda gösterimi kapatma
- CounterArrangement: çok satırlı nesne görsellerinde doğru çıkarma indekslemesi
- UpperGradePracticeIllustration: üst sınıflara uygun ölçülü vektör gösterimi
- LearningJourneyProgress + LearningReviewPolicy: kayıt koruma ve tekrar önerisi
- LearnTabScreen: konu kitaplığı, gerçek hazır dersler, kaldığın yer, tekrar önerileri
- LearningPracticeSection: üç aşama, seslendirme, dokunma, kavrama kontrolü ve ilgili mevcut oyun
