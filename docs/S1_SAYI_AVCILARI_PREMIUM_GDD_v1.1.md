# Sayı Avcıları · Kumsal Koyu — Premium Oyun GDD v1.1
Sürüm: S1 ilk oynanabilir dikey kesit (Android, Kotlin / Jetpack Compose)
Ürün: Matematik Macerası. Hedef: öncelikle 2. sınıfta üçer ritmik sayma ve eş gruplama.
Durum: Kod tamamlandı; GitHub CI ve Android cihaz doğrulaması ayrı kalite kapılarıdır.

## Ürün vizyonu
Tek bir soruya dört şık seçme uygulaması değil; keşif, somut nesne gruplama, sayı analizi, örüntü, rota ve yeni bağlamda matematik kullanmayı bağlayan kısa bir macera. Çocuk öğrenme hedefini oyun eylemiyle çözmeli, yanlış yaptığında sadece "yanlış" değil anlaşılır bir ipucu görmeli.

## Beş görev
| Aşama | Oyun eylemi | Öğrenme hedefi | Başarı koşulu |
|---|---|---|---|
| 1 Kabuk Kümeleri | Kabukları tek tek sepetlere ekle/geri al, üçlü grupları tamamla, toplamı seç | Eş gruplama ve üçer sayma | Doğru toplam |
| 2 Sayı Tahtası | 16 benzersiz taş içinden tam 5 tane 3'ün katını seç | Kural keşfi, çeldiriciler | Beş hedefin tamamı |
| 3 Şifre Kasası | +3 örüntüsünün eksik sayısını seç | Kural ve sayı örüntüsü | Doğru eksik sayı |
| 4 Hazine Rotası | Dört taşı sırayla bul ve rota çiz | Sıralı işlemler ve planlama | Dört geçerli adım |
| 5 Hazine Mührü | Üç farklı bağlamda eş gruplamayı çöz | Bilgiyi yeni problemlere aktarma | Üç problemin tamamı |

Her oyun ayrı mekanik sunar; bütün görevler aynı matematiksel beceriyi farklı bir zihinsel eylemle çalıştırır. Yeni sahnelerde mevcut resim varlıkları, parşömen paneli ve ada renk sistemi korunmuştur.

## Akış ve geri dönüş
Bölge haritası → görev tanıtımı → 1 → 2 → 3 → 4 → 5 → öğrenme raporu.
Oyuncu güncelleme sonrasında eski STAGE_A/STAGE_B/STAGE_C ilerlemesinden devam edebilir. Yeni koşu STAGE_GROUP ile açılır. Her geçişte checkpoint kaydedilir. Kabuk yerleştirme ve son mühürde soru indeksi de kalıcıdır. Eski yedi-sayı sayaç biçimi okunmaya devam eder; yeni biçim dört ilave sayaç alanı taşır.

## Kullanıcı geri bildirimi
- Doğru yanıt: açıklama, ses ve sonraki göreve belirgin geçiş.
- Yanlış yanıt: yeniden deneme ve isteğe bağlı ipucu; yanlışlar yalnızca bilgi kaydıdır, ceza veya hak kaybı değildir.
- Sonuç: denemeler, yanlış hamleler, ipuçları, ilk denemede başarı ve tek bir tekrar önerisi.
- Bu veriler akademik başarı tanısı değildir; herhangi bir "öğrenme garantisi" sunulmaz.

## Ekonomi ve Premium kararı
Mevcut `sayi_avi_2_stage1` yıldız anahtarı, daha önce kazanılmış yıldızlar ve oyun istatistik ID'si korunur. İlk başarılı bitişte yalnızca 1 yıldız verilir; tekrarlar yeni yıldız üretmez. Önce StarManager kaydı, ardından tek seferlik ödül bayrağı onaylanır. Satın alma ürünü, Billing, reklam veya mevcut Premium paketleri değişmedi. Yeni görevleri ücret duvarına yerleştirmedik: ödeme sınırının hangi öğrenme değerine dayanacağı ebeveyn araştırmasıyla kararlaştırılmalıdır.

## Kalite gereksinimleri
- Matematik motoru Android bağımlılığından bağımsız ve `seed` ile deterministiktir.
- 201 seed üzerinde kabuk gruplarının seçenek tekilliği, üç mühür sorusunun geçerliliği; eski/yeni sayaç çözümü birim testlidir.
- Gerçek cihaz: küçük/büyük ekran, yazı büyüklüğü, Android geri tuşu, ekran döndürme, düşük bellek, eski kayıt yükleme, internetsiz oyun, ücretsiz/Premium hesap, çift dokunma ve uygulama yeniden açma ayrıca test edilir.
- Şu anda yeni görsel sistem mevcut oyun sanatı üzerinden yapılmıştır; yeni dünyaların üretimi ayrı sprinttir. Henüz ebeveyn/çocuk kullanım araştırması yoktur.
- Ölçüm: başlama, her görevde tamamlanma, yanlış ve ipucu oranları, tekrar oynama, ebeveyn memnuniyeti. Kişisel veri toplama artırılmadan, ileride rızaya uygun ölçüm tasarlanmalıdır.

## Ticari sonraki aşama
Kumsal Koyu pilotu gerçek cihaz ve çocuk testlerinde doğrulandıktan sonra 4'ün katları Orman bölgesi, Mini Mühendisler ve Pazar Şehri için ayrı oyun mekaniği ve sınıf-beceri içerik paketi geliştir. Tüm dünyaları aynı dört şıklı test düzenine çevirmekten kaçın.
