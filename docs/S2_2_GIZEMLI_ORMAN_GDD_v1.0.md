# S2.2 — Gizemli Orman | Game Design & Engineering Specification v1.0

**Ürün:** Matematik Macerası / Sayı Avcıları  
**Bölge:** 2 — Gizemli Orman  
**Durum:** Android Kotlin + Jetpack Compose oyun kodu uygulanmıştır; GitHub debug testleri geçmiştir. Gerçek çocuk/cihaz kabul testleri ayrıca gerekir.

## 1. Eğitim hedefi
Ana beceri: dörder ritmik sayma, dörtlü eş gruplama ve +4/-4 sayı ilişkileri. Birden fazla etkinlik aynı problemi tekrar etmeyecek; somut modelden örüntü keşfine, mekânsal karara, ardından sözel probleme aktarım sağlanacak.

Eğitim içeriği *tam müfredatın karşılığı değildir*. 2. sınıf düzeyine yönelik örnek bölüm olarak geliştirildi; resmi kazanım eşleştirmesi ve öğretmen validasyonu sonraki kalite kapısıdır.

## 2. Deneyim
| Aşama | Oyuncu eylemi | Matematiksel hedef | Tamamlama |
|---|---|---|---|
| 1. Kütük Köprüsü | 1/2/3 birimlik kütükleri üç boşluğa yerleştirir; taşma hatası engellenir | 4'e tamamlayan toplamlar, parça-bütün | Üç 4-birimlik açıklığın inşası |
| 2. Şifreli Yapraklar | Beş yapraktan +4 örüntüsünü bozan yaprağı seçer | Ritmik sayma hatasını ayırt etme | Üç örüntü kontrolü |
| 3. Işıklı Patika | +2/+4/+6 alternatiflerinden doğru sıçramayı seçer | Dörder ilerleme, ardışık karar | Dört doğru geçit |
| 4. Kadim Ağaç | Eş grup sayma, geriye iki adım, dörderli bölme bağlamı | Öğrenilen kuralı yeni probleme aktarma | Üç problem |

Her koşunun matematik verisi `seed` ile deterministiktir; uygulama kapatılıp açıldığında aynı koşu devam eder.

## 3. Görsel sistem
Köprü modülü görsel olarak fiziksel kütük boşluklarını doldurur. Yapraklar interaktif yeşil kartlardır. Patika tamamlanan geçitleri ışıkla gösterir. Arka plan, Jetpack Compose Canvas ile sahneye özel katmanlı orman siluetleri ve ateş böceği noktalarından oluşturulur. Mevcut `forest_node.webp` varlığı ve marka renk mantığı korunur.

**Tasarım dürüstlüğü:** Yeni özgün illustrasyon veya karakter animasyonu bu sprintte üretilmedi; statik oyun varlıkları + programatik görseller kullanıldı. Tam animasyon/asset geçişi ayrı görsel kalite kapısıdır.

## 4. Veri, ödül ve güvenilirlik
- Yeni ilerleme depolama adı: `forest_adventure_v1`.
- Kumsal Koyu'nun `sayi_avi_2_preferences` kayıtları değiştirilmez.
- Orman yıldız anahtarı: `sayi_avi_forest_stage1` (ilk tamamlamada en fazla 1 yıldız).
- Bir oyun bitişi, `ForestProgressStore.finish()` kontrolüyle bir kez işlenir; tekrar oynamak yeni koşudur.
- Ödül kaydı yarıda kalırsa, `StarManager` tekil oyun anahtarıyla sonraki girişte telafi edilir.
- İstatistik mevcut `GameStatisticsHelper` ile `NUMBER_HUNT_GAME` seviye 2 altında kaydedilir.
- Görev/satın alma hakları, Play Store ürün kimliği ve reklam davranışı değişmez.

## 5. Oturum istatistikleri
Doğru/yanlış hamleler, ipucu sayısı, ilk ve tekrar tamamlanma, en iyi hamle oranı ölçülür. Bu oran akademik başarı, beceri hâkimiyeti veya kalıcı öğrenme göstergesi değildir. Ebeveyn raporu için ayrıca bilimsel olarak uygun ölçüm gerekir.

## 6. Otomatik testler
`ForestEngineTest.kt`:
- 251 farklı seed üzerinde 3 yaprak turu, 4 patika kararı ve 3 final problemi;
- tek doğru yanıt, ayırt edilebilir hatalı yaprak ve deterministik üretim;
- kütüklerde taşma/4'e tamamlama;
- doğruluk ve ipucu sayaçları.

GitHub Actions: `:app:testDebugUnitTest :app:assembleDebug`.
CI'de kullanılan Firebase yapılandırması sadece derleme içindir; gerçek Firebase, Billing ve reklam senaryolarını doğrulamaz.

## 7. Gerçek cihaz kabul senaryoları
1. Oyun haritasında Bölge 2 açık, Bölge 3 hâlâ “Çok Yakında”.
2. Köprüye 3+2 ekleme taşma olarak reddedilir, 3+1 ve 2+2 doğru sonuç verir.
3. Üç yaprak turunda yalnızca bozuk yaprak ilerletir.
4. Patikada +2/+6 ilerlemez, +4 ilerletir.
5. Kadim ağaç üç farklı problemle açılır.
6. Her aşamada uygulamadan çıkıp geri girince aynı tur/ilerleme devam eder.
7. Tamamlanınca tek yıldız, tekrar bitirince sıfır ilave yıldız.
8. Forest bitişi Kumsal Koyu'nun harita parçası, yıldız veya tamamlanma sayısını değiştirmez.
9. Küçük ekran, büyük yazı, geri tuşu, internetsiz kullanım, erişilebilir dokunma hedefleri gözden geçirilir.
10. Premium satın alınmış hesapta reklam ve satın alma haklarının korunması Play Store test kanalıyla doğrulanır.

## 8. Ticari sonraki kalite kapıları
Gerçek çocukların oynarken yardımsız anlayabildiği, görevlerden vazgeçmediği, hata açıklamalarını kavradığı doğrulanmadan “premium kalite tamamlandı” iddiası kurulmaz. Sonraki görsel üretim sprinti için özgün orman ortamı, karakter geri bildirimi, hareket animasyonu ve ses tasarımı yapılabilir. Öğretmen geri bildirimi ve veli pilotlarıyla ödeme değeri test edilmelidir.
