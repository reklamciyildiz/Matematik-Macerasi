# Sayı Avı 3.0 — Sekiz Sınıf, Dört Oyun Dünyası

## Oyun akışı

Yeni Sayı Avı açıldığında sınıf dünyaları atlası açılır. Bitmemiş eski Kumsal Koyu
koşularına atlasın "Klasik Kumsal Koyu ve eski ilerlemem" bağlantısıyla dönülür;
eski görevin aşaması ve önceki ödüller sıfırlanmaz.
Profil sınıfı varsayılandır. Oyuncu 1–8 arasında farklı sınıfları keşfedebilir; seçilen
sınıf soru konusunu, Temel / Orta / Zor ise problem parametrelerini belirler.

- **1–2:** Kayıp Sayı Adası; sayma ve ritmik örüntüler
- **3–4:** Gizemli Sayı Tapınağı; işlemler ve sayısal düzenler
- **5–6:** Kristal Sayı Madenleri; bölenler, asal sayılar, kesirler, ondalıklar
- **7–8:** Sayıların Gizli Evreni; tam/rasyonel sayılar, üsler, kökler

Her koşu **4 bölüm × 4 görev = 16 görev** içerir. Bölümler:
Sayı İzleri (12 taştan doğru 4 sayı), Kayıp Köprü (sıralama),
Enerji Kilidi (dokunmatik sayı ayarı) ve Sayı Mührü (üç eşleştirme).
Tüm görevlerde yanlışlar/ipuçları sayılır; doğru tamamlanma ardından
kısa geri bildirim ve otomatik sonraki görev vardır. Tekrar koşularda tohum
değişir; içerik ve sıralamalar yeniden üretilir. Bazı matematik konuları
yaşa uygun şekilde aynı kalabilir; bütün görevler benzersiz olma garantisi verilmez.

## Kayıt, ödül ve geçiş güvenliği

- Eski `sayi_avi_2_preferences` ve tamamlanan Kumsal Koyu yıldızı **korunur**.
- Yeni görevler `number_hunt_worlds_v3` alanında, sınıf bazında bağımsız kaydedilir.
- Her doğru çözülen görev atomik olarak yazılır, ardından tek kez ilerlenir.
- İşlem kilitlenirse tekrar deneme seçeneği verilir; başarısız kaydı başarı
  olarak göstermeyiz.
- Her sınıfın ilk tamamlanmasında **en fazla bir** yıldız verilir. `StarManager`
  için sınıfa özel idempotent anahtar kullanılır. Eski yıldız anahtarı değiştirilmez.
- Tamamlanma sadece başarılı kalıcı kayıt sonrası istatistiklere gönderilir.
- Bilinmeyen müfredat eşleştirmeleri resmî kazanım gibi gösterilmez.
- Sonuçlar oyun performansını gösterir; öğrenmenin kanıtı değildir.

## APK kabul / geriye dönük test

1. İkinci sınıf profilinde oyun kartı 2. sınıfa göre içerik sunmalı.
2. Altıncı sınıfta asal sayı bulma ve ondalık karşılaştırma, sekizinci
   sınıfta tam kare, üs ve kök soruları görülmeli.
3. 1–8 arasında sınıf değiştir: her sınıfın kayıt/puanı ayrı kalmalı.
4. Her mekanikte doğru seçimden sonraki görev **manuel buton olmadan** açılmalı.
5. Son doğru cevaptan hemen sonra uygulamayı kapat/aç: ilerleme çift işlememeli.
6. Eski bir **3/5** Kumsal Koyu kaydını uygulama silmeden güncelle:
   klasik ilerleme tekrar açılabilmeli ve yeni veriler eskiyi silmemeli.
7. Aynı sınıfın görevlerini iki kez tamamla: ilk tamamlanma yıldızı tekrar verilmemeli.
8. Oyun oynarken 3 tuşlu ve gesture navigasyon, 320dp ekran, büyük yazı,
   düşük performanslı cihaz, geri tuşu ve uygulama yeniden açma testleri.
9. CI birim testi, debug APK, Android test APK ve `check_number_hunt_v3.py` geçmeli.

## Bilinen sınırlar

Bu ilk sürüm dört temel oyun mekaniğini sekiz sınıfa uyarlıyor. Ders kitabı/müfredat
bazlı kazanım validasyonu, farklı yaşlara özel karakter ve ortam görsellerinin tek tek
yeniden üretilmesi, özel erişilebilirlik denetimleri ve kapsamlı çocuk testleri
Play Store sürümü kabul edilmeden ayrıca yapılmalıdır.


## Sayı Avı 3.1 – tekrar oynanabilirlik ve içerik çeşitliliği

- Yeni başlayan koşularda (content_version=2): **24 görev = 4 bölüm × 6 mekanik**.
- 6 mekanik: sayı izleri, köprü sıralama, enerji kadranı, eşleştirme,
  şifre analizi ve dört kontrol noktalı sayı yolu.
- Bölüm içinde mekanikler karıştırılır. Her sınıfta dört farklı
  matematik konusu/predikatı kullanılır; zorluk ve koşu tohumu varyasyon üretir.
- Önceden başlamış v3 content_version=1 koşular **16 görevlik eski soru
  setleriyle** oynanır; kayıtlar yeniden başlatılmaz veya geriye dönük dönüştürülmez.
- Yeni sürümde küçük doğru hamleler (seçilen doğru taşlar/portal geçişi)
  işlem sırası ile kalıcı olarak saklanır. Geri dönüldüğünde aynı bulmaca
  tamamlanabilir.
- Düşük örneklem ve 7–8. sınıf içerik doğruluğu için farklı koşu tohumlarında
  otomatik jenerasyon testleri çalıştırılır.

### Cihaz kabulü

1. 7. sınıfta 24 soru: negatif tam sayılar, rasyonel karşılaştırma,
   mutlak değer; aynı mekanik arka arkaya blok hâlinde gelmemeli.
2. 8. sınıfta tam kare/küp, üslü, köklü ve bilimsel gösterim bulunmalı.
3. 1–6. sınıflarda matematik konuları gerçekten değişmeli.
4. Bir bulmacada 2–3 doğru taşı seçip uygulamayı kapat/aç;
   ara hamle kaldığı yerden devam etmeli.
5. Eski v3 bitmemiş koşu 16 görev olarak tamamlanmalı;
   yeni koşu 24 görev ile başlamalı.
6. Yeni koşu eski yıldızları yeniden vermemeli; sonuç istatistikleri
   yalnız tamamlanmış koşuyu saymalı.
