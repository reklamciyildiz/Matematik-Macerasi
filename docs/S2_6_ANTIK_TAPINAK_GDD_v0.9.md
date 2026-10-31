# ANTİK TAPINAK — Çarpmanın Sırları
## Master Game Design v0.9 · Pedagojik konsept kararı

**Ürün:** Matematik Macerası — Sayı Avcıları  
**Konum:** Kumsal Koyu (üçerli sayma) → Gizemli Orman (dörder sayma) → **Antik Tapınak (eş gruplardan çarpmanın ilişkilerine)**  
**Mevcut durum (2026-11-01):** Dört ritüelin ilk oynanabilir Kotlin/Compose sürümü, ayrı yerel checkpoint, yeniden oynama ve atlas giriş bağlantısı `main`e eklendi. Bu bir **oynanabilir prototiptir**, görsel/pedagojik/cihaz kabulü tamamlanmış ürün değildir. Tapınak, mevcut yıldız ve Premium kayıtlarını değiştirmez; tapınağa özel yıldız dağıtımı ve kapsamlı beceri ölçümü henüz tasarlanmadı.

### 1. Konsept kararı
Bölgeyi başka bir “5'in katını bul” oyunu yapmayacağız. Çocuğun önceki iki bölgede öğrendiği ritmik saymayı ve eş grup kurmayı artık bir yapı inşa etme, denkliği test etme, eksik parçayı bulma ve ters işlem çözme becerisine bağlayacağız.

Bölgenin dramatik hedefi: **Dört Ritüel** tamamlandığında tapınağın saat mekanizması ve ana ışık sütunu çalışır. Sadece seçenek işaretleme değil, fiziksel oyun eylemlerinin doğru matematiksel karşılığı olması beklenir.

### 2. Dört özgün mekanik

1. **Mozaik Odası — Dizilim İnşa Et:** 3×4 gibi bir dikdörtgen mozaik kurar. Oyuncu karoları yerleştirir, satır-sütun-toplam ilişkisini keşfeder. Temel düzeyde görsel destek ve düşük sayı aralıkları.
2. **Denge Sütunları — Eşdeğerliği Kanıtla:** 2×6 ve 6×2 gibi eşdeğer görsel düzenleri döndürür/yeniden yerleştirir; iki mekanizmanın aynı ağırlıkta olduğunu gösterir. Soru kutusu yerine iki etkileşimli düzen.
3. **Kayıp Çarpan Kapısı — Parçaları Paylaştır:** 4 eş grup, 20 taş olduğunda bir grupta kaç taş kaldığını fiziksel olarak ayırır. Toplam-grup sayısı-grup büyüklüğü üçlüsünü ilişkilendirir.
4. **Ayna Mekanizması — Ters Bağlantı:** Kurulan çarpma yapısının bölüm karşılığını görselleştirir. Örneğin 3×4=12'nin 12÷3=4 ilişkisini kanıtlayan kapı dizisi.

### 3. Sınıf ve beceri düzeyine göre uyarlama

| Öğrenci profili | Beklenen kullanım | İçerik sınırı |
|---|---|---|
| 1. sınıf | İsteğe bağlı serbest keşif | Geliştirme aşamasında; çarpma sembolü zorunlu tutulmaz, birinci sınıf resmi kazanımı olarak etiketlenmez |
| 2. sınıf | **Çarpmanın anlamını keşfetme** | Küçük eş gruplar, tekrarlı toplama, görsel modeller; `MAT.2.2.4`/ `MAT.2.2.5` ile ilişkili |
| 3. sınıf | **Çarpma-bölme ilişkisi** | Farklı dizilimler, ters işlem, muhakeme; `MAT.3.2.3`/ `MAT.3.2.4` ile ilişkili |
| 4. sınıf | Temel tekrar/serbest pekiştirme | 2026–27'de eski program geçerlidir; modelin bu sürümü 4. sınıf kazanımının tüm kapsamını değerlendirmez |
| 5–8 | Serbest tekrar | Tam sınıf kapsamı iddiası yoktur |

**Resmi kaynaklar:**
- 2026–27 sınıf-program kapsamı: https://menderes.meb.gov.tr/www/20262027-egitim-ve-ogretim-yili-turkiye-yuzyili-maarif-modeli-temel-egitim-taslak-cerceve-planlari-yayimlandi/icerik/1760/
- 2. sınıf işlemler teması: https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/164
- 3. sınıf işlemler teması: https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/264

### 4. Ürün/pedagoji guardrail'leri
- Oyunda kazanılan bir yıldız, akademik beceri kazanıldı anlamına gelmez.
- Sınıf değişimi, mevcut ilerlemeyi veya satın alma haklarını silmez.
- Her oturum başında seçilen beceri düzeyi dondurulur. Yeni oyun önerisi ancak tamamlanmış yeterli sayıda oturumdan sonra değişebilir.
- Oyunda kazanım kodu **kısmen ilişkili alıştırma** olarak gösterilir; tüm kazanımı ölçtüğü iddia edilmez.
- Bu oyunun Matematik Macerası'nın tek satın alma paketine nasıl bağlanacağı ayrıca değerlendirilir; yeni/ayrı bir Premium SKU açılmaz.
- Materyaller offline üretilebilir; çocuk verisi izinsiz bir analitik sağlayıcıya aktarılmaz.

### 5. V1.0 oynanabilir sürüm kabul ölçütleri
- Dört ritüel birbirinden farklı fiziksel etkileşimler taşır; tapınak kartlı testin yeniden boyanmış hali olmaz.
- Üç düzeyde 250+ seed üzerinde matematik geçerliliği; tek doğru yanıt, eşdeğerlik, sayı aralığı.
- Kalıcı checkpoint; yıldız tek sefer, aynı tamamlamanın iki kere sayılmaması.
- Öğrenme Merkezi ile bağlı yeni ders/oyun akışı, profildeki sınıf/manuel düzeyle uyum.
- Küçük ekran, TalkBack, geri tuşu, ağsız kullanım, uygulamanın yarıda kapanması testleri.
- Gerçek 2–3. sınıf öğrencileri ve öğretmen incelemesi ile pedagojik doğrulama.

### 6. Geliştirme kararının özeti
**S2.5** kaynaklı beceri kataloğu ve dürüst oturum raporudur.  
**S2.6** Antik Tapınak'ın Kotlin/Compose oynanabilir dünyası, kalıcı kayıt, adaptif motor bağlantısı, Android CI ve gerçek cihaz kabulüdür.

Tapınağın Matematik Macerası için gerçek değer önerisi *bir başka sayı katı ezberi* değil, **“ritmik saymadan matematiksel yapıyı keşfetmeye geçiş”** olmasıdır.


## 7. Uygulama ve doğrulama durumu (2026-11-01)

- `TempleAdventureScreen.kt`: Dört ritüel için Compose etkileşimleri, çevrimdışı yerel seed/tier/stage/runs kaydı.
- `SayiAvi2Screen.kt`: Haritadan ayrı `TEMPLE` akışına giriş/çıkış.
- `SayiAviWelcomeScreen.kt`: Üçüncü atlas düğümü açıldı, tamamlanma yerel kayıttan gösterilir.
- İlerleme ritüel geçişlerinde kalıcıdır; bitmemiş bir ritüelin kısmi dokunuşları kaydedilmez.
- **Doğrulanmadı:** Android Gradle assemble/test, CI sonucu, TalkBack, fiziksel cihaz ve öğretmen/çocuk testleri. Bu kontroller geçmeden “release-ready” veya “sorunsuz” iddiasında bulunulmamalı.
- **Kalan kalite işleri:** Premium sanat/animasyon seti, her ritüelde daha güçlü fiziksel manipülasyon, yanlış/ipucu/deneme telemetrisi ve sınıf bağımsız kişisel beceri raporuna temkinli entegrasyon, arayüz erişilebilirliği ve gerçek cihaz testleri.
