# Örüntü Ormanı — Ritmin İzinde | Oyun tasarımı ve Android kabul kapısı

**Durum:** Android oyun kodu, 5 dünya × 6 görev, ödül, kaynak çizimi ve testler GitHub main branch'inde. Play Store'a yayın yok.

## Beş farklı oyun dünyası

1. **Renk Patikası:** Renkli kristallerle tekrar eden 2, 3 veya 4 elemanlı örüntüler. Çocuk tekrarı keşfeder.
2. **Şekil Bahçesi:** Daire, üçgen, kare, yıldız, altıgen ve kalp ile tekrar, dizilim, hatalı şekil analizi.
3. **Sayı Yolu:** Sınıfa uygun artma/azalma ve üst sınıflarda dönüşümlü artış. Tam sayılarla kural denetimi.
4. **Hareketli Totemler:** Yön ve boyut örüntüleri; 90 derece dönüş veya büyük–küçük karşıtlığı.
5. **Kural Kristali:** Sayısal, birleşik renk–şekil ve yön kuralı çıkarımı. Görsel ile sayısal muhakemenin birleşimi.

## Altı gerçek etkileşim türü

- **Tamamla:** Sonraki doğru kristal taşını seç.
- **Sürükle/bırak:** Eksik taşı gerçek hedef alana taşı; erişilebilirlik için dokunarak seçme de çalışır.
- **Sırala:** Dağılmış üç taşı sırayla yerleştir; her parça tek kez kullanılabilir.
- **Kuralı bul:** Kuralı açıklayan seçeneği seç. Yanlış ve doğru kurallar birbirinden farklıdır.
- **Hatalıyı yakala:** Diziyi bozan taşı doğrudan örüntüden seç.
- **Kendi örüntünü kur:** Sonraki üç taşı oluştur; aynı renk/şekil birden fazla kullanılabilir.

Her bölüm 6 görev, bütün macera 30 görev. 30 görevin tamamı bitince final/kazanım ekranı açılır; her yeni seed farklı örüntü üretir. Süre baskısı yok; hedef oynanış yaklaşık 15–25 dakika (cihaz testinde gözlemlenmeli).

## Sınıf ve öğrenme yaklaşımı

- **1. sınıf:** Tekrar eden görsel diziler ve 1, 2, 5, 10'luk sınırlı artışlarla ritmik sayma; kısa kural anlatımı.
- **2. sınıf:** AB/AAB ve daha uzun renk/şekil tekrarları, basit sayı örüntüsü.
- **3. sınıf:** 2/3 gruplu tekrarlar, 2/5/10 ritmik artışlar, yön ve şekil sırası.
- **4. sınıf:** Artan/azalan sayı dizileri, üçlü görsel gruplar, yanlış taşı bulma.
- **5. sınıf:** Birden fazla adımda kuralı doğrulama ve görsel/sayısal genelleme; MAT.5.2.3 ile ilişkili alıştırmalar.
- **6–8. sınıflar:** Dönüşümlü artış, artan/azalan sayı kuralını daha az ipucuyla keşfetme. Bu oyunda resmi tüm müfredat kapsamı iddiası yok.

**Resmi kaynaklar:** https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/45 (MAT.1.1.6) ve
https://tymm.meb.gov.tr/ortaokul-matematik-dersi/unite/450 (MAT.5.2.3).
2026–2027'de 1,2,3,5,6,7 yeni programda; 4 ve 8 için geçerli eski programı ayrı değerlendirmek gerekir:
https://tymm.meb.gov.tr/taslak-cerceve-planlari/temel-egitim

Temel/Orta/Zor sınıfı aşan ileri konuların açılması anlamına gelmez. Öğretmen doğrulaması olmadan 'tam müfredat uyumu' denmez.

## Ürün altyapısı

- `PatternForestEngine`: doğruluğu denetlenebilen saf Kotlin, tekrarlanabilir 30 soruluk seed motoru.
- `PatternForestStore`: `SharedPreferences.commit()` ile her dokunuşta kayıt; doğru cevap olmadan görev geçişi yok. İlerleme, ipucu, hata, seri, beceri kanıtı aynı oturumda saklanır.
- `PatternForestVisuals` ve `PatternForestScreen`: çocuk dostu 6 taş ailesi, dokunmatik kontroller, sürükle hedef çarpışması, portal animasyonu ve açıklamalı geri bildirim.
- `pattern_forest_hero.xml`: 230 şekilden oluşan özgün ve lisans gerektirmeyen renkli fantastik orman sahnesi; ek internet/indirme gerektirmez.
- **Ekonomi:** Yalnız ilk kez tamamlamada 3 yıldız. İstatistik ve günlük görev tamamlaması aynı seed için bir kez iletilir (eski sistemin hata sınırları geçerli).
- **Entegrasyon:** Tüm Oyunlar, Hızlı Erişim, eski oyun menüsü, profil istatistiği ve günlük görevler.
- **Öğrenme:** dünya/beceri bazında toplam ve ipucusuz ilk deneme sayıları. Yeterlilik raporu değil.

## Kalite kapısı

- `scripts/check_pattern_forest.py` kaynak, 5×6 tasarım, görsel ve yönlendirme kontrolü.
- `PatternForestEngineTest` birçok sınıf, zorluk, seed, doğru-yanlış ve tekrar senaryosu.
- `PatternForestPersistenceTest` Android cihaz/emülatörde bütün görevleri tamamlayıp kayıt ve ödül tekrarı doğrulaması.
- GitHub Actions debug APK, JVM testleri ve androidTest APK **derlemesini** çalıştırır; fiziksel cihaz instrumented testleri CI içinde otomatik çalıştırılmaz.
- **Gerçek telefon kabulü:** düşük ekran genişliği 320/360dp, tablet, TalkBack, uzun bas-sürükle-bırak, dokunma alternatifi, yanlış cevap→tekrar, 30 görev bitişi, ara→geri dönme, hızlı art arda dokunma, ses kapalı, internet kapalı, uygulama arka planda, yıldızın bir kez verilmesi ve önceki oyun verisinin korunması.

**Release notu:** Bu dosya APK kabul testi rehberidir, mağaza yayını onayı değildir.
