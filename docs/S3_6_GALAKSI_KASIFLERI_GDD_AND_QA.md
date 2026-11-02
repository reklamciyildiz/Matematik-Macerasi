# S3.6 — Uzay Matematik Macerası / Sürekli Meteor Avı (3 Kasım öncesi revizyon)

## Geçerli oynanış
- **Ana aksiyon mekaniği tek ve tutarlı:** Gemi sürüklenerek veya sol/sağ tuşlarıyla yönetilir. Lazer mermileri hareketli üç meteordan birine fiziksel temas etmelidir.
- **15 ardışık matematik sorusu:** Her doğru isabetten sonra motor, aynı savaş sahasında otomatik yeni soru ve yeni meteorlar üretir. "Sonraki tur" butonu, reaktör enerji pompası ve yıldız devresi artık oynanışta yoktur.
- Yanlış isabet aynı soruyu tekrar ettirir; seri sıfırlanır, bir hata kaydedilir. Çocuk elenmez; kalkan en az 1 kalır. İpucu kullanılabilir ve o soru için ilk deneme yıldızı verilmez.
- Yıldızlar tamamlanan 15 hedefin ilk deneme doğruluğuna, yanlışlara ve ipuçlarına dayanır; önceki yıldız yüksek-su seviyesi korunur.

## Sınıfa duyarlı kural
- Öğrenci profilindeki `user_class` önce mevcut `GradePolicy` ile ayrıştırılır; oyuna başlarken `grade` 1..4 arasında sabitlenir. Profil sınıfı belli değilse **1. sınıfın temkinli kapsamı** seçilir.
- **1. sınıf:** Sadece toplama ve çıkarma; işlem cevapları Temel ≤10, Gelişen ≤15, Ustalık ≤20. Çarpma ve bölme üretilemez.
- **2. sınıf:** Toplama ve çıkarma; Temel ≤20, Gelişen ≤50, Ustalık ≤100. Çarpma/bölme varsayılan seviyede açılmaz. Bu bilinçli olarak temkinli bir içerik seçimidir.
- **3. sınıf:** Daha büyük toplama/çıkarma, küçük çarpım tablosu ve tam bölünebilen bölme.
- **4. sınıf:** Dört işlem, daha büyük sayılar.
- `LearningTier` (Temel/Gelişen/Ustalık) sadece ilgili sınıfın **içindeki** rakam büyüklüğünü seçer; sınıf sınırını aşamaz. Bu ürün içeriği resmi MEB kazanım denklik sertifikası değildir.
- Sınıf/tier/seed + soru indeksi soruyu deterministik üretir; cihaz kapanıp açılsa soru ve doğru meteor değişmez.

## UI ve performans
- Lobi dikey kaydırılabilir; **oyun ekranı kaydırılmaz.** Üstte kısa başlık ve tek satır durum; ortada matematik ifadesi; altında kalan yüksekliği dolduran fiziksel lazer/meteor Canvas sahası ve sabit kontrol sırası.
- Uçuş başlangıçta duraklatılmıştır; kullanıcı BAŞLAT der. Uygulama arka plana geçince otomatik durur.
- Lazer sesi mevcut uygulama ayarına saygılı `SoundPool` ile çalınır; sahne yerel çizilir. Premium WebP sprite/art paketi henüz yoktur; bunun varlığı iddia edilmez.
- `SpaceExpeditionStore` v3 formatı profil sınıfını, soru indeksini, hata, ipucu, gemi konumu ve en iyi seriyi kalıcı kaydeder. v2 açık turları korunabildiği ölçüde yeni soruya eşlenir; tamamlanmış v2 kayıtları ise önceki ödül/rekor anahtarlarında saklıdır.

## Otomatik test ve fiziksel cihaz kabulü
- JUnit: dört sınıf × beş gezegen × üç seviye × 35 seed = 2100 tur / **31500 geçerli isabet**; soru doğruluğu, 1–2. sınıf işlem sınırları, ceza ve aynı sorunun deterministik tekrarını doğrular.
- Compose instrumented test APK: lobi açma, hareket kontrolü, lazer butonu ve durum geçerliliği. CI APK'yı **derler**, emülatörde çalıştırmaz.
- Telefonda: 320–360 dp ekranda gemi ve alt kontrollerin görünmesi; 15 sorunun kesintisiz geçişi; doğru/yanlış fiziksel çarpışma; ses açık/kapalı; duraklatma; arka plan/geri dönüş; yeniden yükleme; uçak modu; profil sınıfına göre işlemler; Play Billing ve mevcut yıldızların değişmemesi kontrol edilmelidir.

## Önceki sürümden fark
Önceki dokuz görevli reaktör/yıldız rejimi artık **geçerli yol değildir**. Bu doküman, aynı klasörde bulunan eski S3.6 planının üzerine geçen güncel oyun kararıdır.
