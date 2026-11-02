# Kesir Kristalleri — Oyun ve Yayın Kalitesi Sözleşmesi

Durum: Android uygulamasına entegre edilen bağımsız mini oyun; Play Store yayını değil.

## Tasarım
- **Bir giriş, dört farklı etkileşim, 12 görev, sınırsız tekrar.** Tüm Oyunlar ve Hızlı Erişim üzerinden açılır.
- Fantastik mavi ada evreni; 67 katmandan oluşan özgün vektör sahne, interaktif kristal çarkı ve ölçüleri doğru kesir şeritleri.
- **Kristali Aydınlat:** Kristalin eş dilimlerine dokun; seçili payı somut oluştur.
- **Işık Köprüsü:** Artı/eksi ile birim kesir ekle/geri al; hedef uzunlukta köprü kur.
- **Denge Tapınağı:** Farklı paydalı kesirleri yalnızca pay karşılaştırarak değil, eş ölçekli çubuklarla kıyasla.
- **Bilgelik Kapısı:** Denk kesir, 5+ düzeyinde yüzde eşleştirmesi ve 6+ düzeyinde tam aritmetik ile işlem.
- 3 kademeli Temel/Orta/Zor seçimleri, 12 soruda 4 farklı mekanik döngüsü, hareketli doğru cevap geri bildirimi ve mevcut ses servisinden kısa efektler.

## Öğrenme ve sınıf
- 1–2: Eşit paylaşım **serbest keşif**, okul kazanımı veya yeterlilik olarak etiketlenmez.
- 3: Model, bütün-yarım-çeyrek, eş parça ve pay/payda; resmi TYMM: MAT.3.1.9 / MAT.3.1.10 / MAT.3.1.11 ile *ilişkili alıştırmalar*.
  Kaynak: https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/254
- 4: Eski programla ayrı doğrulanacak; birim kesir ve denk kesir tekrar alıştırması, tüm kazanım eşdeğerliği değil.
- 5: Farklı gösterimler, yüzde ve kesir karşılaştırma; MAT.5.1.3 / MAT.5.1.4 ile *ilişkili alıştırmalar*.
  Kaynak: https://tymm.meb.gov.tr/ortaokul-matematik-dersi/unite/449
- 6: Yüzde, kesir ve eş paydalı kesir toplama; MAT.6.1.6 / MAT.6.1.7'deki zengin problem muhakemesinin yalnızca **kısıtlı bir alt becerisi**.
  Kaynak: https://tymm.meb.gov.tr/ortaokul-matematik-dersi/unite/457
- 7–8: Pekiştirme. Kapsam dışındaki ileri kazanımların öğretildiği iddia edilmez; sınıf müfredatının bütünü değildir.
- Seviye yükseltmek yasaklı/ileri içeriği otomatik açmaz. Soru doğruluğu **tam sayılarla çapraz çarpımla** kontrol edilir; yaklaşık ondalık eşitlik yok.
- Çocuk profil sınıfı, oturum başlangıcında sabitlenir. Oyuna dönerken seed/grade/difficulty korunur.

## Kalite, ekonomi, ses, kayıt
- Ana müzik uygulamanın ortak ses sisteminde kalır; ses doğru/yanlış/görev/zafer animasyonlarına eşlik eder.
- **Her dokunuş ve her aşama SQLite/hesap gerektirmeden SharedPreferences commit ile saklanır.** Kaydedilemeyen hamle ilerleme sayılmaz.
- İlerleme tek oturumda 12 görev; çık-gir aynı soru, aynı kristal parça seçimi ve aynı zorlukla devam eder. Yeni macera farklı seed ile yeniden oluşturulur.
- Yanlış ve ipucu ilk denemede bağımsız doğru sayılmaz; beceri bazlı tamamlanan görev sayıları kayıtlıdır. Ebeveyn raporları kanıt olmadan resmi başarı iddiası taşımaz.
- İlk kez bitirme yıldızı tek ödül anahtarıyla sınırlandırılır (3 yıldız); günlük görev/istatistik aynı bitirme seed'i için bir kez gönderilir. Eski oyun/ödül anahtarlarına dokunulmaz.
- **Teknik kabul:** JUnit matematik doğru-yanlış/kararlılık/sınıf sınırları, Android debug ve androidTest APK derlemesi, kaynak/görsel doğrulama.
- **Fiziksel cihaz kabulü ayrıca gereklidir:** 320dp, orta ve tablet ekranları; ekran dönüşü, geri tuşu, uygulamayı zorla kapatma, offline, ses kapama, hızlı çoklu dokunma, TalkBack, mevcut kullanıcı verisini koruma, ses/müzik kesintisi, yıldız ödülü ve aynı kayda ikinci kez girme.
- Sınıf kazanım eşleştirmelerinin pedagojik doğrulanması bir matematik öğretmeni ile yapılmalıdır. Play Store yayını ayrıca onay gerektirir.

## Kod
- games/fractions/FractionCrystalEngine.kt — saf Kotlin matematik ve deterministik içerik üretimi
- games/fractions/FractionCrystalStore.kt — atomik checkpoint ve tekrar ödül koruması
- games/fractions/FractionCrystalsScreen.kt — Compose görünüm, çizim ve etkileşim
- games/fractions/FractionCrystalsActivity.kt — Android giriş/istatistik/ödül
- res/drawable/fraction_crystals_hero.xml — özgün kaynak grafiği
- tests: FractionCrystalEngineTest, FractionCrystalPersistenceTest
- scripts/check_fraction_crystals.py — Android CI ön kontrolü
