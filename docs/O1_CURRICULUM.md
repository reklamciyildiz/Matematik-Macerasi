# Ö1 — Matematik Macerası 1–8. Sınıf Konu/Kazanım Mimari Kararı

> **8 Kasım 2026 içerik güncellemesi:** 1–8. sınıfta 59 konu/temel-tekrar kartının her biri artık en az bir özgün kısa derse açılıyor; toplam 54 farklı kısa ders (66 sınıf/ders gösterimi) bulunuyor. Aşağıdaki 7 Kasım pilot tablosu **tarihsel ilk kapsam**tır, güncel içeriğin tamamı değildir. En güncel kapsam ve yayın sınırları için [Öğren Genişletmesi](LEARNING_EXPANSION_RELEASE_GATE.md) dosyasına bakın. Bu, MEB'in bütün alt kazanımlarını kapsadığımız anlamına gelmez.

**Tarih:** 7 Kasım 2026 · **Öğretim yılı:** 2026–2027
**Durum:** Konu/çıktı planlama indeksi kodda var; **tam resmî kazanım eşlemesi, kitap içerikleri ve tüm derslerin geliştirilmesi henüz tamamlanmadı.**

## Resmî program sürümü ve kaynaklar
MEB'in 2026–2027 resmî açıklamasına göre **1, 2, 3, 5, 6 ve 7. sınıflar Türkiye Yüzyılı Maarif Modeli'nde**, **4 ve 8. sınıflar önceki öğretim programlarında**. 4 ve 8. sınıfı otomatik olarak TYMM kazanımı gibi göstermeyin.

- [MEB 2026–2027 Temel Eğitim yıllık planları](https://tymm.meb.gov.tr/taslak-cerceve-planlari/temel-egitim)
- [MEB 2026–2027 uygulama ayrımı](https://menderes.meb.gov.tr/www/20262027-egitim-ve-ogretim-yili-turkiye-yuzyili-maarif-modeli-temel-egitim-taslak-cerceve-planlari-yayimlandi/icerik/1760/)
- [MEB İlkokul matematik programı — sınıflar 1–4](https://tymm.meb.gov.tr/ogretim-programlari/ders/ilkokul-matematik-dersi)
- [MEB Ortaokul matematik programı — sınıflar 5–8](https://tymm.meb.gov.tr/ogretim-programlari/ders/ortaokul-matematik-dersi)
- [MEB önceki programlar, özellikle 4 ve 8](https://mufredat.meb.gov.tr/Programlar.aspx)

### Uygulamada saklanacaklar
**Kaynak türü:** 2026–2027 öğretim programı sürümü.
**Topic ID:** grade-scoped sabit kimlik (ör. g1_addition, g8_powers).
**Alana göre sınıflandırma:** Sayılar ve Nicelikler / İşlemler / Geometri ve Ölçme / Veri ve Olasılık / Örüntüler ve Cebir / Temel Tekrar.
**Öğrenme hedefi:** Sınıfa uygun ve özgün, anlaşılır hedefler. **Bunlar resmî MAT.… kodlarının birebir kopyası değildir.**
**Gerçek hazır içerik:** `lessonIds` listesi doluysa kısa ders oynanabilir; diğerleri yalnızca konu planı ve hedef özeti sunar.
**Oyun bağı:** `relatedGame` meta verisidir; tamamlanan derste doğrudan oyun derin bağlantısı ayrıca kontrol edilir.
**Çocuk deneyimi:** 1–2 yaşça erken okur için görsel ve kısa metin; 3–4 destekli örnek; 5–8 çözümlü analitik metin ve diyagram.

## Sınıf bazında içerik alanları

| Sınıf | Program | Planlanan konu aileleri | Şu an gerçekten oynanabilir kısa ders |
|---|---|---|---|
| 1 | TYMM | 20'ye kadar sayılar, karşılaştırma, ritmik sayma, toplama, çıkarma, şekiller, veri | `count_2`, `add_10` |
| 2 | TYMM | Basamaklar, ritmik sayma, dört işlem temeli, gruplama, yarım/çeyrek, saat, şekiller, veri | `count_3`, `count_4`, `add_20`, `multiply_intro` |
| 3 | TYMM | Doğal sayılar, örüntü, işlemler, kesirler, geometri, zaman/para, veri | `count_3`, `count_4`, `multiply` |
| 4 | Önceki | Doğal sayılar, çok adımlı işlemler, örüntü, kesir, geometri, ölçme, grafik | `count_3`, `count_4`, `multiply` |
| 5 | TYMM | Doğal sayılar, kesir-ondalık, işlemler, geometri, ölçme, istatistik, olasılık | yalnız **temel tekrar**: `divisible_15`, `multiply` |
| 6 | TYMM | Sayılar, kesir-ondalık-oran, cebir, çokgenler, alan, istatistik/olasılık | yalnız **temel tekrar** |
| 7 | TYMM | Sayılar, cebir, orantısal düşünme, dönüşüm, ölçme, istatistik, olasılık | yalnız **temel tekrar** |
| 8 | Önceki | Çarpanlar/katlar, üslü-köklü, cebir, üçgenler, cisimler, veri/olasılık | yalnız **temel tekrar** |

Liste bir **ürün mimarisi planıdır**, MEB'in tam öğrenme çıktıları, dönem sırası, haftalık ders saati veya resmî kazanım kodları listesi **değildir**. Önce bütün resmî çıktıları ve sınıf/yıl sırasını uzmanla tek tek eşleştirmek gerekir. Bu adım yapılmadan "MEB müfredatının tamamı" iddiası kullanılmaz.

## Ö2 — Ürün ekranları
1. Öğren ana sayfası: seçilen sınıf ve geçerli program sürümü.
2. **Kaldığın Yerden Devam Et:** kaydedilen son gerçek kısa ders, anlatım/örnek/uygulama aşaması, soru sırası ve oturum tohumu.
3. Konu kitaplığı: konu ailelerine göre gruplandırılmış başlıklar; hazır olmayan konular yine öğrenme hedeflerini gösterir, hiçbir uydurma derse yönlendirme yapmaz.
4. Konu ayrıntısı: özgün iki öğrenme hedefi, gerçek kısa ders kartları ve ilgili oyun önerisi.
5. Ders: kısa anlatım → örnek → dört soruluk pratik → uygun **mevcut** oyun. Üç aşama ve oturum kaydı cihazda kalır.
6. Kısa ders tamamlanması yalnızca `p2_learning_pilot_v1/done_*` üzerinden ölçülür; bu **kazanımda ustalık, tüm müfredat ilerlemesi veya pedagojik değerlendirme değildir**.
7. Öğrenci sınıfı değişince daha önce biten derslerin geçmişi silinmez. Bulut profili veya ek ödeme/preference eklenmez.

## Yayın öncesi gerçek kabul testleri
- [ ] Her sınıfta yalnız ilgili topic seti, yaş grubu metin düzeyi ve doğru program etiketi.
- [ ] 5–8. sınıfta temel tekrar haricindeki konu listesinde **oynanabilir** etiketi yok.
- [ ] Başlangıç dersini aç, örnek aşamasında kapat/aç → doğru aşamadan devam.
- [ ] Pratikte yanlış/ipuçlu sorudan sonra kapat/aç → kaydedilen soru, seed, sayaçlar korunur.
- [ ] Doğru cevaptan otomatik geçiş, dört soru bitince gerçek tamamlanma kaydı.
- [ ] Son ders ilgili oyuna yönlendirir (oyun sınıf politikası ayrıca QA).
- [ ] Tamamlanan dersleri profil/Ayarlar istatistiğiyle tutarlı göster.
- [ ] Küçük mobil ekran, büyük yazı, erişilebilir dokunma hedefleri.
- [ ] Tam TYMM/önceki program eşlemesinin eğitim uzmanı tarafından onaylanması.
- [ ] Ö3: kapsamlı, özgün görseller, seslendirme, etkileşimli konu anlatımları ve geniş kazanım içerikleri **ayrı üretim aşamasıdır**.
