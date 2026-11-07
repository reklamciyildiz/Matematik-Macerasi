# Matematik Macerası — Öğren Ürün Genişletmesi / Yayın Öncesi Kanıt Kaydı

**Öğretim yılı:** 2026–2027 · **Ürün:** Android / Kotlin / Compose  
**Kapsam:** Ö1+Ö2+Ö3 üzerine yapılan ilk çok-sınıflı içerik, görsel model ve adaptif tekrar paketi.

## 1. Sınıfa göre gerçekten oynanabilir içerik

| Sınıf | Önceki kısa ders | Yeni özgün ders | Toplam sunulan kısa ders |
|---|---:|---:|---:|
| 1 | 2 | 3 | 5 |
| 2 | 4 | 3 | 7 |
| 3 | 3 | 3 | 6 |
| 4 | 3 | 3 | 6 |
| 5 | 2 temel tekrar | 3 sınıfa özgü | 5 |
| 6 | 2 temel tekrar | 3 sınıfa özgü | 5 |
| 7 | 2 temel tekrar | 3 sınıfa özgü | 5 |
| 8 | 2 temel tekrar | 3 sınıfa özgü | 5 |

**24 yeni benzersiz ders + 8 eski benzersiz ders = 32 ayrı kısa ders kimliği**, farklı sınıflarda yeniden sunulan eski içeriklerle toplam 44 sınıf/ders gösterimi. Her yeni dersin kendi matematiksel anlatımı, gerçek hesaplama üreten 4 soruluk pratik bankası ve *neden* soran kavrama kontrolü bulunur.

Bu rakam **MEB kazanımlarının tamamını karşıladığımız anlamına gelmez**. Her konuya ait bütün alt kazanımları, dönem/kazanım sırasını, uzman değerlendirmelerini ve gerçek öğrenme sonuçlarını henüz kapsamaz.

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
- LessonTeachingIllustration + AdvancedLessonIllustration: yaşa uygun vektörel diyagram
- LearningJourneyProgress + LearningReviewPolicy: kayıt koruma ve tekrar önerisi
- LearnTabScreen: konu kitaplığı, gerçek hazır dersler, kaldığın yer, tekrar önerileri
- LearningPracticeSection: üç aşama, seslendirme, dokunma, kavrama kontrolü ve ilgili mevcut oyun
