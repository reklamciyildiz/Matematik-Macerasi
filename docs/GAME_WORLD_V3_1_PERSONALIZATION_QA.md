# Game World 3.1 — Kart Bilgileri ve Ana Sayfa

## Değişiklikler
- Onaylı kaşifli ada hero, mevcut kapak görselleri, günlük görev ve alt menü korunmuştur.
- Oyun kartları öğrenme konusu, gerçek sınıf ilişkisi ve zorluk bilgisini tek veri kaynağından alır.
- Hızlı Erişim son açılan iki oyun ve sınıfa uygun iki öneriyi öne taşır, diğer oyunları korur.
- Ana sayfada son açılmış üç oyun gösterilir. Bu "kaldığın yerden" veya kaydedilmiş
  bir görev bulunduğu anlamına gelmez.
- GameLaunchHistory en fazla 6 farklı oyun kimliği saklar. Application yaşam döngüsünden,
  farklı başlangıç noktalarından açılan tüm katalog oyunları kaydedilir.
- 1–8. sınıf, 1–4. sınıf, sabit ikinci sınıf becerileri ve sadece zorluk temelli
  oyunların arayüz açıklamaları birbirine karıştırılmaz.
- Yalnızca mevcut adaptif öğrenme kaydında en az 8 deneme ve anlamlı hata/ipucu
  varsa "Ek alıştırma önerisi" sunulur; hiçbir zaman kesin öğrenme teşhisi değildir.
- Sınıf değişince diğer sınıfta yapılmış denemeler öğrenme önerisine taşınmaz.

## Fiziksel cihaz kabul
1. 1., 2., 4., 6. sınıfta sınıf/konu/zorluk satırlarını karşılaştır.
2. Bir oyunu günlük görevden aç; ana sayfaya dönünce son açılanlarda görünmeli.
3. İki oyunu sırayla aç, listede ikisi de görünmeli, tekrar açınca kopyalanmamalı.
4. Çok dar ekranlarda ve büyük Android fontunda kart alt satırları kesilmemeli.
5. Her kart doğru Activity'yi açmalı; ödül, yıldız, satın alma ve ilerleme değişmemeli.
6. CI birim testleri, GameSuggestionPolicyTest, kaynak kontrolleri ve debug derleme geçmeli.
