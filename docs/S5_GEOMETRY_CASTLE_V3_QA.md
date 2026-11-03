# Geometri Kalesi 3.0 — Gerçek telefon kabul belgesi

## Bu değişikliğin nedeni

**Kullanıcı cihazındaki eski sürüm hatası:** Geometri Kalesi, Köprü Atölyesi 2/3 (toplam 5/12), çember seçildikten sonra "Bu adım doğrulanamadı" hatası. Görselde tek boş yuva var ve orta seviye seçilmiş. Bu durum eski içerik sürümü v1 ile uyumludur. Ekranın **v1 doğru cevap işleme kodu** yeni v2 `placed` listesine bir taş eklerken, `GeometryCastleEngine.valid()` v1'de bu alanın boş olmasını zorunlu tutuyordu. Bu yüzden doğru cevap reddedilebiliyordu.

**Düzeltme:** v1 köprü cevaplarında `selected=chosen`, `placed=[]`, `solved=true` olarak kalır; yalnız v2/v3 çoklu yuvalarda `placed` ilerletilir. Eski kayıtlar silinmedi, otomatik sıfırlanmadı. Yeni Android testleri tam bu senaryoyu bir v1 SharedPreferences kaydı ile tekrar oluşturur.

**Alt menü sorunu:** İkon dairesi, yazı ve iç boşlukların toplam yüksekliği, navigasyon satırının içerik yüksekliğini aşıyordu. Ana ekranın alt menüsü ayrıca yüzen bir katman olarak içeriğin üzerinde duruyordu. Menü, `Column` içinde kalıcı yer kaplayan bir bölgeye taşındı; satır 84 dp oldu ve `navigationBarsPadding` ile sistem gezinme alanı korunuyor. Gereksiz 84/96 dp içerik boşlukları kaldırıldı. Geometry Castle ekranındaki sistem durum/gezinti çubuğu çakışması da `statusBarsPadding` ve `navigationBarsPadding` ile düzeltildi.

## v3 oyun kapsamı

- Dört alanın her birinde **5 görev**, toplam **20 görev**; eski v1/v2 maceralar **12 görev** ve mevcut seed/contentVersion ile aynen devam ediyor.
- Şekil Avlusu: sınıfa göre izin verilen şekilleri tanıma, özelliklerini keşfetme ve kenar/köşe sayma.
- Köprü Atölyesi: farklı şekil siluetlerini birer/ikişer/üçer taşla tamamlama; çevre/alan için şekli çizimli ölçme.
- Ayna Kulesi: 4×4 veya üst seviyede 6×6 vitrayda gerçek düşey simetri; hücrelere dokunarak desen oluşturma.
- Taç Salonu: görsel tanıma, ölçme, inşa ve simetri karışık final.
- **Yeni ölçüm mekaniği:** birim karelerle alan, dört kenar toplamıyla çevre, taban/yükseklikli üçgen alanı, iki ışınla açı; +/− butonları ve dokunmatik kaydırıcı. Görevin doğru cevabı doğrudan görüntülenmez.
- Mevcut yedi adet premium lisanslı/ürün içi WebP sahnesi ve karakter varlığı korunur. Yeni resim indirmeye/izin almaya gerek kalmaz; ölçüm çizimleri Android Canvas ile yapılır.
- Çocuklara zaman baskısı yok, yanlış cevap ve ipucu bağımsız başarı olarak sayılmaz. Doğru/yanlış, bölüm bitişi ve taş yerleştirme sesleri ortak ses sistemini kullanır.

## Sınıfa göre sınırlar

| Sınıf | Yeni görevler |
|---|---|
| 1–2 | Üçgen, kare, dikdörtgen, çember; düz kenar/köşe sayma, somut inşa ve simetri |
| 3 | Çokgenlerin kenar ve köşelerini keşif; simetri ve parça yerleştirme |
| 4 | Dikdörtgende çevre ölçümü, kenar/köşe ilişkileri, simetri |
| 5 | Dikdörtgen çevre/alanı, daha geniş şekil havuzu |
| 6–8 | Alan/üçgen alanı, açı ölçme ve çevre; daha büyük simetri deseni ve karma final |

Sınıf bir **içerik tavanıdır**; Temel/Orta/Zor seçimi 2. sınıfa alan formülü getirmez. 2026–27'de TYMM 1,2,3,5,6,7 sınıflarda uygulanır; 4 ve 8 sınıflarda önceki program kullanılır (MEB):
https://tegm.meb.gov.tr/www/2026-2027-egitim-ogretim-yili-taslak-cerceve-planlar-yayinlandi/icerik/1316
2. sınıfın şekil tanıma ve geometrik yapı çalışması:
https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/165
Bu bir **konu/alıştırma eşleştirmesi**dir; tüm sınıfın geometrik kazanımlarının öğretildiği iddia edilmez.

## Teknik kalite ve fiziksel kabul

- `GeometryCastleEngine.kt`: sürüme göre 3 veya 5 görev/oda, grade-aware deterministic görev üretimi ve sonuç doğrulama.
- `GeometryCastleProgressStore.kt`: eski v1/v2 kaydı okuyup koruma, yeni v3 grade/measure değerlerinin atomik kayıt altına alınması, yanlış/yinelenmiş geçişlerin reddi.
- `GeometryCastleScreen.kt`: eski köprü cevabı koruması, premium WebP sahneler ve yeni ölçüm etkileşimi.
- `GeometryCastleMeasure.kt`: Canvas çizimleri, hesaplama ve kaydırıcı.
- `HomeScreenNew.kt`, `HomeBottomNavigation.kt`: fiziksel sistem çubukları ve gerçek içerik yüksekliğine duyarlı alt menü.
- JVM testleri: `GeometryCastleCheckpointRegressionTest`, `GeometryCastleV3EngineTest` ve önceki testler.
- Android instrumented testleri: `GeometryCastlePersistenceTest` ve `GeometryCastleV3PersistenceTest`; **GitHub CI bunları derler fakat cihazda çalıştırmaz**.

**Kabul senaryoları:** (1) eski 5/12 Köprü görevinde çemberi seçip yuvaya yerleştir; (2) eski macerayı bitirip yeni macerada 20 görev açıldığını gör; (3) 2. sınıfta alan/açı çıkmadığını denetle; (4) 4,5,6+ sınıflarında ölçüm doğruluğunu sınayarak ilerle; (5) telefonun sistem gezinme çubuğu üç tuşlu/gesture ayarlarında, küçük ekran ve büyük yazıda alt menünün taşmaması; (6) uçak modu, uygulamayı kapat–aç, hızlı art arda dokunma, yıldızın tekrar verilmemesi; (7) TalkBack ile seçenek erişimi ve sürükleme alternatifi.

**Yayın kararı:** Android otomatik derleme/test sonuçları ile fiziksel cihaz kabulü ayrıdır. Yayın için ikisinin de geçmesi gerekir.
