# S3.2 — Öğrenci ve Ebeveyn İçin Öğrenme Raporları (ilk sürüm)

Durum: GitHub main'e entegrasyon, Android/JUnit CI ve fiziksel telefon kabulü için hazırlanmıştır. 2026-11-01.

## Ürün kararı

Mevcut `AdventureLearningReport` bileşeni korunarak ikinci bir paralel rapor sistemi açılmadı. `LearningReportEngine.kt` dört adaptif oyun alanından (`AdaptiveLearningStore`) gerçekte tamamlanmış oturumları okur:
1. Üçerli gruplama / Kumsal Koyu
2. Dörtlü gruplama / Gizemli Orman
3. Geometrik şekiller ve simetri / Geometri Kalesi
4. Alışveriş, fiyat ve para hesabı / Pazar Simülatörü

Önceden yalnız ilk iki beceri görünüyordu; Geometri ve Pazar artık dahil edildi.

## Kullanıcı akışı

- Ana sayfa **Gelişimim → Detayları Gör** düğmesi, Öğren sekmesine gider. Rapor ekranın başındadır; önceki altı derslik eğitim pilotu olduğu gibi devam eder.
- **Çocuk**: hangi becerileri çalıştığını, kaç oyun tamamladığını, yeterli oyun varsa doğru deneme yüzdesini ve bir sonraki macera fikrini görür.
- **Ebeveyn**: her oyun için kayıtlı tamamlanmış oturum, deneme, yanlış, ipucu ve mevcut oyun zorluk kademesi; cihaz içi açıklayıcı gözlemler ve somut tekrar önerileri.
- Activity'den uygulamaya geri dönüldüğünde lifecycle ON_RESUME olayı ile rapor güncellenir; önceki oyun sayıları bellekte eskimiş kalmamalı.
- **Üçerli ve dörtlü sayma**: mevcut CurriculumSkillCatalog tarafından sağlanan pilot resmî program ilişkisi yalnız varsa gösterilir. **Geometri ve Pazar** için doğrulanmamış MEB kazanımı uydurulmaz.

## Verinin yorumu ve dürüstlük sınırı

- `AdaptiveLearningStore` yalnızca yeni uygulama sürümlerinde kaydedilen **tamamlanmış oyun oturumlarını** toplar. Tamamlanmamış işler ve eski sürümlerde aktarılmamış kayıtlar dahil değildir.
- Deneme oranı `(denemeler - yanlış hamleler)/denemeler`; **öğrencinin öğrenme başarısı, kalıcı bilgi, tanı, sınıf başarısı ya da MEB kazanımı** değildir.
- 0 deneme varsa yüzde **gösterilmez**, eksik kanıtla "öğrendi" yargısı üretilmez.
- Ebeveyn için "tekrar alıştırması faydalı" yorumu, en az **2 bitmiş oturum + 8 deneme** ve **yanlışların %35 veya üzeri** veya **3+ ipucu** gözlendiğinde oluşturulur. Bu yalnızca bir çalışma önerisidir.
- Oyun seviyeleri profilin sınıf önerisiyle başlar; oyun içinde seçilen kademe otomatik okul seviyesi veya müfredat yeterliliği sayılmaz.
- İstatistikler bu cihazdaki tek oyuncu profiline bağlıdır. Birden fazla çocukla aynı cihaz kullanılıyorsa bu kayıtlar ayrı kişiler adına güvenle raporlanamaz.

## Gizlilik ve sonraki öğretmen paneli

- Bu faz **yalnızca yerel, salt-okunur** rapor: yeni ağ çağrısı, öğretmen hesabı, okul paylaşımı, tanımlayıcı çocuk verisi toplama, veri dışa aktarma veya yeni Premium SKU yok.
- Öğretmen/okul için gerçek bir çok-öğrencili rapor **henüz açılmadı**: veli izinleri, öğrenci/veri ayrıştırması, okul kimlik/rol yönetimi, erişim kaydı, veri saklama ve silme kararları olmadan mevcut tek cihaz verisiyle okul paneli üretmek yanlış olur.
- Raporlar, çocuk ve ebeveyn açısından erişilebilir bir temel değer olduğundan ilk sürümde yeni bir ücret kilidi eklenmedi.

## Testler

`app/src/test/.../learning/report/LearningReportEngineTest.kt`:
- dört becerinin görünmesi;
- sıfır kayıt varsa yüzdelik ve yüksek başarı uydurulmaması;
- geometri/pazar denemelerinin oranlara doğru katılması;
- çalışma önerisi için anlamlı en az iki oturum ve eşik;
- yüksek deneme sayılarında yüzdelik hesabının taşmaması.

`app/src/test/.../ui/screens/home/GameSkillReportTest.kt` eski biçim için geriye dönük uyumluluk testidir.

PowerShell:
```powershell
git pull origin main
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

### Telefon QA

1. Ana Sayfa → **Gelişimim → Detayları Gör** → Öğren sekmesinin **en üstünde** Öğrenme Yolculuğum.
2. **Çocuk / Ebeveyn** düğmeleri geçiş yapar, dört beceri listelenir.
3. Bitmiş Geometri ve Pazar oturumları istatistiklerde görünür; daha eski sürüm kayıtları aktarılmadıysa 'Henüz oynanmadı' mesajı dürüsttür.
4. Sıfır denemede %100 gibi sahte başarı yok, ebeveyn görünümünde yardım önerisi var.
5. Yeni bir macerayı bitirip Öğren'e geri dönünce sayı güncellenir; oyunun ilerlemesi, yıldızları, Premium kilitler ve satın alma bilgisi değişmemeli.
6. Ekran döndürme, düşük ekran genişliği ve büyük yazı ile kartlar birbirine girmemeli.
