# S2.5 — Kaynak Doğrulamalı Müfredat–Beceri Matrisi ve Öğrenme Raporları

**Program yılı:** 2026–2027  
**İçerik sürümü:** `CurriculumSkillCatalog.CATALOG_VERSION = 2026-27-pilot-1`  
**Durum:** Kodlandı; gerçek cihaz kabulü ve pedagojik öğretmen incelemesi ayrıca gerekli.

## 1. Ürün kararı
Profil sınıfı, başlangıç önerisidir; oyun ilerleme, yıldız ve kazanım yeterliliği değildir.
Oyun sonuçları şu üç katmanda ayrı tutulur:
- **Resmi öğrenme çıktısı:** Yalnızca kodu ve kaynağı doğrulanan sınıf-program ilişkisi.
- **Ürün becerisi:** Üçer/dörder gruplama, toplama veya çarpmanın anlamına ilişkin çalışma.
- **Oturum gözlemi:** Deneme, yanlış ve ipucu sayıları; tek başına okul başarısı veya öğrenme tanısı değildir.

## 2. Program sürümü
MEB 2026–2027 uygulanma bilgisi: 1–3, 5–7 TYMM; 4 ve 8 eski öğretim programı. Ana sınıfı kapsam dışı.

Ana kaynak: https://menderes.meb.gov.tr/www/20262027-egitim-ve-ogretim-yili-turkiye-yuzyili-maarif-modeli-temel-egitim-taslak-cerceve-planlari-yayimlandi/icerik/1760/

## 3. Doğrulanmış uygulama eşleştirmeleri

| Profil | Uygulama becerisi | Çıktı kodu | İlişki | Sınır |
|---|---|---|---|---|
| 1 | İkişer ritmik sayma | MAT.1.1.5 | İlişkili alıştırma | Tüm ritmik sayma kapsamını ölçmez |
| 1 | 10 içinde toplama | MAT.1.2.1 | İlişkili alıştırma | Günlük yaşam problemi çözümlemesi tam ölçülmez |
| 2 | Üçlü gruplama | MAT.2.2.4 | İlişkili alıştırma | Çarpma-bölme çözümlemesinin küçük kısmı |
| 2 | Dörtlü gruplama | MAT.2.2.4 | İlişkili alıştırma | Çarpma-bölme çözümlemesinin küçük kısmı |
| 2 | 20 içinde toplama | MAT.2.2.1 | İlişkili alıştırma | Problemler basit tutulur |
| 2 | Çarpmayı keşfet | MAT.2.2.4 | İlişkili alıştırma | 2–5 arası eş gruplama, 4 soru |
| 3 | Çarpma ile gruplama | MAT.3.2.3 | İlişkili alıştırma | Muhakemenin bütün süreçleri ölçülmez |
| 3 | Üçer/dörder gruplama | MAT.2.2.4 | **Önceki sınıftan tekrar** | 3. sınıf yeni kazanımı diye gösterilmez |
| 4 | Üçer/dörder örüntü | M.4.1.1.6 | İlişkili alıştırma | Örüntü oluşturma/açıklama tam ölçülmez |

4. sınıf çarpma dersinde hâlihazırdaki kısa çarpım tablosu oyununun bire bir karşılamadığı eski program `M.4.1.4.x` kodları **kasıtlı olarak eşleştirilmedi**. 5–8. sınıf için mevcut uygulama içerikleri yalnızca **temel tekrar pilotudur**, tam müfredat eşleştirmesi değildir.

## 4. Resmi kaynaklar
- 1. sınıf sayılar: https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/45
- 1. sınıf işlemler: https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/99
- 2. sınıf işlemler: https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/164
- 3. sınıf işlemler: https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/264
- 4. sınıf **eski** program (2018): https://mufredat.meb.gov.tr/Dosyalar/201813017165445-MATEMAT%C4%B0K%20%C3%96%C4%9ERET%C4%B0M%20PROGRAMI%202018v.pdf

## 5. Gerçek öğrenme raporunun kapsamı
Öğrenme Merkezi'nin sonunda **Üçerli eş gruplama / Dörtlü eş gruplama** kartları:
- Profil sınıfına göre ilgili MEB öğrenme çıktısı veya açıkça “eşleştirme yok” bilgisi
- Uyarlama motorundaki beceri seviyesi
- Uygulamanın önceki sürümlerinden geriye dönük olarak uydurulmayan, gerçekten yeni sistemde kaydedilen oturum/deneme/yanlış/ipucu sayıları
- Oyun içi doğru hamle oranı ve veri yetersizse açık durum
- Yeni oturum veya görsel grupla çalışma önerisi

Ebeveyn/öğretmen gözüyle **bu rapor bir tanı, sınıfı geçme kararı veya tüm MEB kazanımını tamamlama belgesi değildir**. Öğrenme Merkezi'nde önceki pilot derslerin tamamlanma istatistikleri ayrı kalır.

## 6. Veri gizliliği ve uyumluluk
- `user_prefs/user_class`, `adaptive_learning_v1`, `p2_learning_pilot_v1` korunur.
- Billing, Premium, reklam, yıldız, önceki oyuncu istatistikleri ve oyun başarıları değişmez.
- Veri okumaları cihazda ve offline gerçekleşir.
- Öğrenci kimliği olmadığı için birden fazla çocuğun aynı yerel profili kullanması durumunda istatistikler karışabilir; çoklu öğrenci profili olmadan okul/öğretmen raporu yayınlanmamalıdır.

## 7. Kalite kapısı
- Kazanım kodu–sınıf–program eşleştirme testleri
- Çarpmanın giriş sorularında 2–5 eş grup sınırı ve deterministik matematik testleri
- İki macera için eski kayıt ve tek yıldız davranışını bozmadığını doğrulama
- Android derlemesi + birim testler
- Gerçek cihazda ekran yoğunluğu, sınıf değiştirme, oyun dönüşünde raporun yenilenmesi
- Öğretmen denetimi: öğrenme çıktı kodu ve oyun eylemi örtüşmesi

## 8. Karar ve sıradaki aşama
**S2.5 pilot kapsamı:** Uygulamada gerçek veriye dayalı kazanım etiketleri ve beceri raporları. Sınıf 1–8 tam program içerik kaplaması değildir.

**S2.6:** Antik Tapınak oyun dünyası (4 özgün mekanik), checkpoint/ödül, adaptif seviyeyle entegrasyon ve gerçek cihaz testleri. İlk karar: `docs/S2_6_ANTIK_TAPINAK_GDD_v0.9.md`.

**İyi gittiğimizi ne gösterir?** Öğrencinin görev yapmasıyla öğrenme hedefi arasında tutarlı bağ, düşük yanlış eşleştirme oranı, anlaşılır hata geri bildirimi ve öğretmen/çocuk testleri. Çok sayıda oyun kartı tek başına kalite veya ticari talep göstermez.
