# Matematik Macerası — Adaptive Learning Architecture v1 (S2.4)

## Ürün kararı

**Profil sınıfı ≠ oyun zorluğu ≠ öğrenme kazanımı ≠ oyun ilerlemesi.**

Profildeki `user_prefs/user_class` yalnızca yeni oyun için varsayılan beceri düzeyini önerir. Öğrenci, beceride **Temel / Gelişen / Ustalık** arasında seçim yapabilir veya otomatik öneriyi kullanabilir. Bütün bölgeler erişilebilir kalır, sınıf değişimi eski harita parçalarını/yıldızları silmez.

## V1 veri akışı

`PlayerProfileService.getUserClass()`
→ `GradePolicy.parse`
→ `AdaptiveLearningStore.progress(AdventureSkill)`
→ `AdaptiveTierSelector` (otomatik / manuel)
→ `SayiAviStorage.beginRun(seed,start,tier)` veya `ForestProgressStore.startNew(tier)`
→ **Oyun boyunca dondurulmuş tier + seed**
→ `recordCompletion(skill,runId,tier,attempts,errors,hints)`
→ sonraki oyun için seviye önerisi.

## Öğrenme seviyesi pilot kuralları

| Profil sınıfı | Başlangıç önerisi |
|---|---|
| Ana Sınıfı, 1–2 | Temel |
| 3 | Gelişen |
| 4–8 | Ustalık |
| Bozuk veya eksik veri | Temel |

Bu eşleştirme **resmi sınıf kazanım tablosu değildir**. Aynı oyun ailelerinin tekrar/beceri antrenmanı için kullanılmasını sağlar; öğretmen ve resmi kazanım denetimi gerekir.

**Uyarlama:** 8+ yanıtlı tamamlanmış oturum, doğruluk ≥%85 ve ipucu 0 ise “güçlü”; doğruluk <%60 veya ipucu ≥3 ise “destek gerekiyor”. İki ardışık güçlü oturum üst seviyeye, iki ardışık zorlanan oturum alt seviyeye **gelecek oyunda** öneri verir. Tek yanlışta değiştirme olmaz. Manuel seçim açıkken otomatik seviye kaymaz.

**Not:** Bu kurallar akademik beceri hâkimiyetini kanıtlamaz; yalnızca oyundaki hamle ve ipucu kayıtlarına dayalı bir başlangıç sezgisidir. Öğretmen/pilot verileriyle iyileştirilmelidir.

## İçerik farklılaştırması

- Kumsal Koyu: temel düzeyde iki somut üçlü grup ve küçük sayı aralığı; gelişen düzey mevcut sayı tahtası/kasa/rota kurgusu; ustalık düzeyinde daha geniş sayı aralığı ve **iki aşamalı hazine hikâye problemleri**.
- Gizemli Orman: yaprak ve patika sayı aralıkları düzeyle değişir; Kadim Ağaç hikâye problemleri düzeye göre farklı sayılarla üretilir. Köprünün dört birimlik yapısal kuralı tüm düzeylerde korunur.
- Yalnızca sayıları büyütmek nihai adaptasyon değildir. Sonraki sürümde problem türü, destek yoğunluğu ve somut/soyut çözüm yolları öğretmenle tasarlanmalıdır.

## Kayıt ve geçmiş uyumluluğu

Yeni dosya: `adaptive_learning_v1` (her beceri için `count_3`, `count_4`). Mevcut harita, yıldız, oyun istatistikleri ve satın alma alanlarından bağımsız.
- Kumsal Koyu'nun eski koşularında seviye kaydı yoksa `GELISEN` kullanılır.
- Gizemli Orman'ın eski koşularında seviye kaydı yoksa `GELISEN` kullanılır.
- Yeni koşu başladığında seçilen beceri düzeyi kayıt altına alınır; profil sınıfı oyun ortasında değişse bile aynı sorular gelir.
- `runId` bazında son tamamlamanın ikinci kez öğrenme geçmişine yazılması engellenir.
- Profil sınıfını değiştirmek yeni otomatik başlangıç önerisini yeniler; mevcut toplam deneme/yanlış ve bitirme geçmişini silmez.
- Manuel tercih otomatik öneriyi askıya alır; istediği zaman geri dönülebilir.
- Öğrenme geçmişi, mevcut uygulamanın tek yerel profilinde saklanır. Çok öğrenci hesabı ileride eklenecekse **öğrenci ID'sine göre veri yalıtımı zorunludur**.

## Kabul testleri

1. 2. sınıf profili için Temel, 3. sınıf için Gelişen önerilir; manuel Ustalık seçilebilir.
2. İki dünya yeni oyun başladığında seçilmiş düzeye göre sayı ve hikâye zorluğunu kullanır.
3. Ortada oyundan çıkıp geri dönünce aynı seed ve tier devam eder.
4. Otomatik seviye geçişi sadece tamamlanan ve yeterli sayıda yanıt içeren oturumlarda çalışır.
5. Tek/çift bildirim aynı runId'yi iki kez kaydetmez.
6. Sınıf değiştirmek yıldız, Premium ve bölge tamamlama kayıtlarını değiştirmez.
7. Kumsal/Gizemli Orman için eski koşular Gelişen varsayımıyla açılır.
8. 151 seed × 3 seviye matematik doğrulama testleri, Android debug derlemesi geçer.
9. Gerçek cihazda küçük ekran, geri tuşu, geçiş/yeniden açma ve profil güncellemesi manuel test edilmelidir.

## Sonraki sürüm

**S2.5:** Tam kazanım-beceri matrisi ve okul program sürümü, öğretmen doğrulaması, uygulama içi beceri raporu ve içerik etiketleri. Bundan sonra **Antik Tapınak** (eş gruplama, çarpmaya geçiş) yeni motorla geliştirilmeli.
