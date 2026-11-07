# Ö3 — Matematik Macerası ders anlatım motoru

**Tarih:** 7 Kasım 2026. **Kapsam:** Yalnızca mevcut oynanabilir kısa derslerin öğretim deneyimi; Ö1 müfredat taslağındaki henüz geliştirilmemiş konular *açık ders değildir*. Google Play AAB yayın onayı verilmemiştir.

## Eğitim deneyimi
Üç aşama, bir gerçek oyun yönlendirmesi:
1. **Gör/oku/dinle:** Konu anlatımı ve erişilebilir, matematiksel gösterim.
2. **Adım adım düşün:** Resme veya büyük butona dokunarak aynı çözümlü örneği bir adım ilerlet. Son adımda **kavram sorusu**: neden bu işlem uygulanır? Hatalı cevapta ilgili açıklama; doğru cevapta kısa alıştırmaya geçiş.
3. **Kavramayı dene:** Önceki dört farklı soru üreticisi/seed ve doğru cevaptan otomatik ilerleme; yanlışlara ipucu. Tekrar denemelerinin sayısı saklanır.
4. **Uygula:** Konunun mevcut matematik oyununa yönlendirme. Yeni ikinci oyun merkezi üretilmez.

### Yaş seviyesine göre
- **1–2. sınıf:** kısa Türkçe anlatım, daha büyük metin ve renkli elma vektör illüstrasyonları, cihaz uygunsa ilk açılışta sesli okuma; sayı şeridi ve büyük hedefler.
- **3–4. sınıf:** adım adım açıklama ve grup modelleri; çocuk istediğinde sesli dinleme.
- **5–8. sınıf:** daha analitik anlatım, kompakt sayı/grup çizimleri; hâlihazırda oynanabilir olan içerik açıkça **temel tekrar** olarak kalır.

Görseller ilk sürümde uygulamanın mevcut **vektör elma çizimi** ve dinamik sayısal modellerle üretiliyor; ayrı WebP çizim paketleri veya stüdyo kaydı seslendirmeleri **bu sürümde üretilmedi**. İşlem modelinin sayı ve gruplarıyla tutarlı olması, dekoratif görsellikten önceliklidir.

### Ses ve çocuk güvenliği
- Android'in mevcut \`TextToSpeech\` motorundan yalnız **çevrimdışı Türkçe ses** seçilir (\`Voice.isNetworkConnectionRequired == false\`). Bu nedenle TTS metni seslendirmek için ek bulut servisine gönderilmez.
- TTS motoruna erişmek için Android 11+ Manifest \`queries\` tanımı vardır; yeni mikrofon, reklam izni veya kullanıcı kimliği gerektirmez.
- Yerel Türkçe ses cihazda kurulu değilse metin ve tüm ders işlevleri çalışmaya devam eder; ekranda durum açıklanır. Ses kalitesi cihazdaki yüklenmiş sese bağlıdır.
- Ses ayarında ses efektleri kapalıysa TTS başlamaz. Uygulama ses servisi konuşma sırasında müziği yumuşakça kısar, bitince geri açar.
- Ekran kapanışı veya uygulama arka plana alındığında konuşma durur, ses motoru uygun biçimde serbest bırakılır.
- 1–2. sınıfta varsayılan sesli anlatım, diğer sınıflarda **Dinle** butonu. Her durumda metin eşdeğeri vardır.

### Kayıt / ekonomi uyumluluğu
Mevcut \`p2_learning_pilot_v1\` SharedPreferences, \`done_*\`, \`runs_*\`, \`best_*\` anahtarları **değiştirilmedi**. Yeni anahtarlar: \`worked_step_*\`, \`concept_passed_*\`, \`concept_errors_*\`; daha önceki \`phase_*\`, \`index_*\`, \`session_seed_*\` korunuyor.

Öğrenme ilerlemesi, öğrencinin gerçekten bir kazanımda *ustalaştığının kanıtı değildir*. Yıldız, Premium ve ödeme akışı değişmedi.

### Şu anki oynanabilir kısa dersler
\`count_2\`, \`count_3\`, \`count_4\`, \`add_10\`, \`add_20\`, \`multiply_intro\`, \`multiply\`, \`divisible_15\`. Ö1'deki diğer konu başlıkları planlama kartı olarak görünür.

## Yayın öncesi QA
- [ ] Pixel 7 Android 15 emülatör ve fiziksel cihazda 1, 2, 3, 4, 6 ve 8. sınıfların ders tasarımlarını kontrol et.
- [ ] İnternet yokken önceden indirilmiş Türkçe sesin çalıştığını; ses yoksa dersin sessiz ve eksiksiz çalıştığını doğrula.
- [ ] "Ses Efektleri" kapalıyken seslendirme ve efekt çıkmamalı; görsel/metin anlatım devam etmeli.
- [ ] Arka plana geçince TTS durmalı; ana tema geri dönmeli; aynı ekrana girip çıkınca konuşma motoru sızmamalı.
- [ ] Elma, gruplama ve ritmik sayma çizimleri doğru matematik miktarlarını göstermeli.
- [ ] Görsele dokunmak ve "Sonraki Çözüm Adımı" aynı sonucu vermeli; bir adım iki kez sayılmamalı.
- [ ] Kavram sorusunun doğru/yanlış seçeneği farklı tohumlarda yer değiştirmeli; yanlışta ileri geçmemeli.
- [ ] Uygulamayı kapat/aç: örnek adımı, soru sırası ve yanlış sayısı korunmalı. Bitirince eski kazanımlar korunmalı.
- [ ] Son soruda doğru cevap → ders özeti → ilgili **mevcut oyun**; oyun sınıf/erişim politikası ayrıca test edilmeli.
- [ ] 320–360dp ekran, büyük font, TalkBack ve azaltılmış animasyon kullanımı gerçek cihazda test edilmeli.
- [ ] Öğretim metinleri ve doğru cevap açıklamaları eğitim uzmanı tarafından içerik doğruluğu açısından kontrol edilmeli.
- [ ] Google Play'e gönderilmeden önce çocuklar için SDK/politika, satın alma ve Cloudflare release güvenliği ayrıca gözden geçirilmeli.

## Sonraki içerik üretimi
Her konu için sınıfa özgü özgün medya, kapsamlı örnek havuzu, dikkatli ses tasarımı, düzey bazlı anlamlı etkileşimler ve ölçülebilir tekrar sistemi ayrı içerik geliştirme aşamasıdır. Mevcut Ö3 motoru bu içerikleri daha sonra genişletmek içindir; **1–8. sınıf müfredatının tamamlandığı iddiasında bulunulmaz.**
