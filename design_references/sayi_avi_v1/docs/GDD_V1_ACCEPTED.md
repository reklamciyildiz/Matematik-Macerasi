# Sayı Avı 2.0 — Kayıp Hazine | GDD v1.0 — İlk oynanabilir vertical slice

## Ürün hedefi
Çocuğa matematik yaptırmak için resimle süslenmiş test değil; aynı beceriyi **üç değişik şekilde** uygulayan 5–8 dakikalık keşif akışı. İlk prototipin hedef yaşı başlangıçta 8–11; görev güçlüğü gerçek sınıf seviyesi/beceriye göre ayarlanmalı. Ebeveynlere erken aşamada 'öğrendi' garantisi verilmez; bireysel performans göstergeleri sunulur.

## Ekran akışı
Oyunlar -> Sayı Avı -> Bölge/Hazine haritası (ilk bölge açık; açılmamış dünyalarda sahte tıklanabilir içerik yok) -> Kısa görev anlatımı -> Görev A Sayı Avı -> A doğru geri bildirim -> Görev B Şifre Kasası -> B doğru geri bildirim -> Görev C Hazine Rotası -> Ödül + Gelişim -> Yeniden Oyna veya Oyunlar.

## İlk bölüm: üç aşama (tek bölüm)
Tema: `3'ün katları / 3'er artan ritmik sayma`.

### A: 4x4 Sayı Avı
- Tam 16 **birbirinden farklı** sayı, bunların **tam beşi** 3'e tam bölünebilir. 5 hedefin dışında tesadüfen 3'ün katı kalan başka sayı bulunamaz. Testte deterministik seed kullanılabilir. İlk sabit örnek: [7,12,4,15,8,11,6,17,13,18,10,5,9,14,16,19]. Hedefler [6,9,12,15,18].
- Her seçime görsel/auditory geri bildirim; seçimi geri alma; dokunma alanı >=48dp. **Kontrol Et** sadece beş seçim yapıldığında etkin. Geçmek için seçilen küme tam olarak doğru beş sayı olmalı; sıralama önemsiz.
- Yanlış seçimde cezalı enerji/kalp kaybı ve yıldız eksiltme YOK. İlk yanlışta kısa yönlendirici ipucu; ikinci yanlışta 3'er ritmik sayma gösterimi. Yanıtı doğrudan vermeye zorlanmadıkça kaçın. Hata mesajı nedenini açıklar.
- Geçildiğinde bir harita parçası ve stageDone kaydı; tekrarlanan tap/rapid taps iki ödül vermemeli.

### B: Şifre Kasası
- Dizilim örneği 6,9,12,?; doğru yanıt 15 (alternatifler 13,15,18). Şifre kasası somut etkileşimli seçeneklerden kurulur; tarihsel örüntü ve kural matematik motorunca üretilir. Aynı doğru cevabı her çalıştırmada sunmak yerine seed destekli deterministik varyasyon üretilebilir, ama hepsi 3 fark kuralını sağlamak zorunda.
- Doğru yanıt sandığı açar, ikinci harita parçası ilerlemesi. Yanlış yanıt açıklama + yeniden deneme, soft failure.

### C: Hazine Rotası
- Çocuk doğru adım sayıları 6 -> 9 -> 12 -> 15 şeklinde sıralayarak hazineye gider. Dört doğru adım görsel patika üzerinde seçilir. Distraktörler yanlış ama makul olmalıdır (örn. 10,14,17). Yol için en az bir belirli doğru çözüm vardır, tıklamalar geri alınabilir. Yol çizgileri Compose Canvas veya gerçek state tabanlı çizilir; sırf resimde basılı oklar oyun mekaniği olmaz.
- Son adım hazine sahnesini/ödül panelini açar.

## Oyun durumu / kayıtlar
Mission: NOT_STARTED -> IN_PROGRESS(A,B,C) -> COMPLETED.
Substage: IDLE -> ACTIVE -> FEEDBACK -> SUCCESS.
- İşletim sistemi süreç öldürme, geri tuşu, oyundan çıkıp dönme durumunda güvenli devam; kullanıcı kendi isteğiyle görevden çıkarsa bir sonraki açışta kalıcı son stage'den sürdür ya da net bir 'baştan başla' sun. Eski oyun verilerine dokunma.
- Sabit reproducible seed (debug/test), üretimde kaydedilen seed veya tanımlı görev varyantı; soru görünümünde değişen seed ile hedef kaymasının önüne geç.
- Kullanıcı izni/velinin izni olmadan yeni uzak analitik/sunucu verisi göndermeyin. Yerel ölçümler yeterli.

## Öğrenme ölçümü
SkillId `multiples_of_3`: attempts, validSelections, mistakes, hintCount, completion, noHintCompletion, replayCount; zaman yalnızca yardımcı sinyal olarak tutulur, tek başarı ölçüsü değildir. Bir görev tamamlandı diye 'öğrendi' sonucu üretme. 2 veya daha fazla farklı varyasyon ve daha sonraki tekrarlarda performans karşılaştırması planla. Çocuk hakkında uydurma yaş/sınıf/kazanım verisi üretme. Ödül sonuç ekranında gerçek sayılar göster.

## Ödül ekonomisi
- Mevcut StarManager kaynak doğruluğunu bozmadan kullan. Sınırsız farm yok: ilk bölüm tamamlama ödülü en fazla bir kez; sonraki tekrarlarda yalnızca görsel tebrik ve öğrenme kaydı. Var olan yıldız sistemini ikinci SharedPreferences kaydı yaratarak çatallama.
- İlk tamamlamada sembolik 'harita parçası' ver; kodda ayrı ücretli değerli para birimi ekleme. Gerekirse badge sanal state olarak tutulur.
- Premium entitlement, IAP, reklam ve içerik kilitlerini yeniden tanımlama. Bu dikey kesitte yeni satın alma ekranı yok.

## İlerideki dünyalar
Plan: ilk dünya Kumsal 12 görev, sonraki Orman ve Tapınak; **bu sprintte yalnızca ilk oynanabilir bölüm**. Bitmemiş dünyaları oynanabilir gibi gösterme.

## Öğrenme ve ürün başarısı testleri
- 5–8 dakikalık ideal süre yalnızca tahmin, gerçek testte ölçülecek.
- Çocukların en az bir tanesinin yardım almadan hedef kuralı açıklayabilmesi ve birkaç gün sonra tekrar oynama isteği araştırılmalı.
- Veli ödeme kararını bu sprintte doğrulanmış kabul etmeyin; gerçek velilerle değer testi gerekecek.
