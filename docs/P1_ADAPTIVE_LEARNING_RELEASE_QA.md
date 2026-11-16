# P1 — Akıllı Öğrenme Motoru (16 Kasım 2026)

**Kapsam:** Matematik Macerası Android / Kotlin / Compose, 1–8. sınıflarda mevcut 72 özgün kısa ders, mevcut 4/8 soruluk düzen, yeni bir hizmet veya abonelik ürünü değil.

## Uygulanan akış

1. Dersin mevcut konu anlatımı ve adım adım kavram kontrolü aynen korunur.
2. Alıştırma sorusunda yanlış seçim yapıldığında **seçilen yanlış sayı ve o dersin konu ailesine göre** öğretici bir ipucu sunulur. Bu **muhtemel pratik odağıdır**, matematiksel yanılgının kesin teşhisi değildir.
3. İlgili konuya ait mevcut **ders illüstrasyonu** resimli hatırlatma olarak gösterilir. Örnek model ile rastgele sorunun sayıları farklı olabileceği arayüzde açıkça belirtilir; sayılar eşmiş gibi gösterilmez.
4. İlk hatadan sonra çocuk isteyerek **Benzer Bir Soruyla Tekrar Et** butonuna basabilir. Aynı asıl soruda 2 yanlış varsa tek defa otomatik benzer soru sunulur. Bu soru aynı dersin doğrulanmış jeneratörüyle oluşturulur, soru sayısını artırmaz, Premium kontrolünü aşmaz. Benzer varyant üretilemiyorsa buton gösterilmez.
5. Yardım sorusunu çözen öğrenci asıl soruya döner; doğru cevap sonrasında dersin önceden var olan **otomatik sonraki soru** ve bitiş davranışı korunur. Destek sorusunda tekrar hata yapılırsa çözümlü örnek açılır ve öğrenci ana sorusuna dönebilir.
6. Sınıfa ait soru bankası değişmez. **İleri soru düzeyi** otomatik rastgele yeni konu açmaz: aynı dersin farklı sayı büyüklüğündeki geçerli soruları arasından seçer. Düzey bir başarı/ustalık sertifikası değildir.
7. Zorluk/yardım düzeyi yalnız **sonraki soruya** uygulanır: hatalar, ipucu veya yardım sonrası destekli; doğru ve ipucusuz ilerleme sonrası yeniden temel ve çeşitlendirilmiş sorular. Görünen soru, cevap verilirken kesinlikle değişmez.
8. Tamamlanan oturumda yerel olarak tutulan hata odaklarına göre tekrar önerisi kişiselleştirilir; aynı odakta en az 2 hata varsa tekrar ertesi güne önerilebilir. Ders kitablığındaki 'Tekrar Etme Zamanı' listesi konu odağını gösterir.

## Geçmiş veriler ve erişim

- **Korunur:** `p2_learning_pilot_v1` içindeki `done_*`, `best_*`, `runs_*`, `phase_*`, `index_*`, `session_seed_*`, `session_wrong_*`, `session_hints_*` ve önceden hesaplanmış tekrar zamanları.
- P1 verileri aynı yerel tercihlerde **ayrı `p1_*` anahtarlarında** saklanır. Yeni kurulmuş veya tamamlanıp tekrar başlatılan bir oturumda adaptif varyantlar devreye girer. **Yarıda kalmış eski oturumların orijinal soruları değiştirilmez.**
- Yeni bir Premium/abonelik ürünü tanımlanmadı; Premium ders motoru kontrolü aynı `LearningAccessPolicy` ile yapılıyor. Ders kazançları, yıldızlar, avatarlar etkilenmez.
- Bu veriler cihaz üzerinde çalışır. Sunucuya yeni çocuk verisi gönderilmez ve cihazlar arası eşitleme iddiası yoktur.
- Yalnızca sayısal büyüklük ve işlem yapısı üzerinden **yaklaşık bir varyant sıralaması** uygulanır. Gerçek pedagojik zorluk derecelendirmesi veya yapay zekâ tanısı değildir.
- Resmî müfredatın tamamı henüz kapsanmıyor; mevcut 72 kısa dersin aynı ders aileleri içinde adaptasyonu yapılmıştır.

## Test matrisi

### Otomatik testler
- [x] `AdaptiveLearningPolicyTest`: yeni ve eski oturum zorluk politikası, yanlış seçenek → konu odağı, değişmeyen CORE soru, destek varyantı üretimi, 1–8. sınıftaki **tüm mevcut dersler** için 0 ve son indeks denetimi, dört farklı cevap, negatif koordinat ve destek sorusu doğruluğu
- [x] Birim testleri GitHub Actions Android Update + Learning QA iş akışına eklendi
- [ ] Son commit için gerçek GitHub Actions sonucu **yeşil** doğrulanmalı. İş akışının eklenmiş olması çalıştığı anlamına gelmez.

### Cihaz / kabul testleri (üretim öncesi zorunlu)
- [ ] 1–8. sınıfların en az birer dersinde ilk yanlış → konuya bağlı rehberlik, ikinci yanlış → benzer soru; görsel modelin asıl soruya ait sayılarla karıştırılmaması
- [ ] Destek sorusunda doğru, 2 yanlış, geri dönme ve tekrar açma akışlarının her biri
- [ ] Asıl soru doğru olduğunda otomatik geçişin, son soruda bitişin korunması
- [ ] İpucu ve destek kullanıldığında sonraki sorunun destekli düzeye geçmesi; temiz cevaptan sonra kademeli normale dönmesi
- [ ] Uygulama arka plana atılıp yeniden açıldığında eski oturumun **sorusunun aynı kalması**; yeni P1 destek oturumunun ve yanlış kaydının geri gelmesi
- [ ] Önceden tamamlanmış ders, yıldızlar, Premium hakkı, ücretsiz iki ders ve eski 4/8 soruluk kayıtların korunması
- [ ] 320/360/412dp ekran, TalkBack, büyük yazı tipi, çevrimdışı Türkçe TTS ve ses kapalı kullanım
- [ ] Real-device Firebase/Cloudflare/Play satın alma ve restore regresyonu; release signing ve AAB doğrulaması

**Yayın kararı:** Güncellemeyi sırf kod GitHub'a aktarıldığı için üretime yayınlama. CI + cihaz kabul testlerinin yeşil olması gereklidir.
