# Matematik Macerası — Öğren Ürün Genişletmesi / Yayın Öncesi Kanıt Kaydı

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
- PracticeVisualFactory: soru metninden güvenli görsel model kurma, bilinmeyen şablonda gösterimi kapatma
- LearningJourneyProgress + LearningReviewPolicy: kayıt koruma ve tekrar önerisi
- LearnTabScreen: konu kitaplığı, gerçek hazır dersler, kaldığın yer, tekrar önerileri
- LearningPracticeSection: üç aşama, seslendirme, dokunma, kavrama kontrolü ve ilgili mevcut oyun
