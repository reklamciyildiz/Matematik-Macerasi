# Üç ödeme modeli — release gate (8 Kasım 2026)

- Ömür boyu: eski INAPP `matematik_macerasi_premium`, aynı SKU, tek ödeme, kalıcı hak.
- Yeni tek SUBS ürün taslağı: `matematik_macerasi_premium_subscription`.
  - `monthly`: otomatik yenilenen aylık base plan.
  - `yearly`: otomatik yenilenen yıllık base plan.
- Tüm planlar **aynı Premium haklarını** açar; başka tier yok.
- Kesir Kristalleri, Zaman Kulesi ve Örüntü Ormanı: **Temel** Free, **Orta + Zor** Premium. Kayıtlı Orta/Zor maceraları hak kaybında silinmez; yeniden etkinleşince devam eder.
- Fiyatlar Android'de Google Play ProductDetails `formattedPrice` üzerinden okunur. Play ürünü bulunmazsa satılamaz.
- Aylık/yıllık hakkı eski `premium_prefs.is_premium` kalıcı INAPP alanına yazılmaz. `queryPurchasesAsync(SUBS)` ile oturumda doğrulanır; ödeme beklerken hak verilmez.
- Geri yükleme hem eski INAPP hem SUBS hem mevcut kodu sorgular; erişim süresi dolunca Premium düşmelidir. Oynama kayıtları ve özel avatar sahipliği silinmez.
- Mevcut satın alma butonları abonelikler aktifken üç seçenekli Ayarlar ekranına gider.
- Satın alma önünde kullanıcı istemediği matematik/yaş sorgusu yoktur; Google Play kendi onay akışını yürütür.

**YAYIN ENGELİ:** `MM_SUBSCRIPTIONS_ENABLED` varsayılanı `false`. Yayına açmadan önce:
1. Play Console ürününü ve base planlarını aç, fiyat/ülke ayarlarını, vergi ve yerel para birimini doğrula.
2. Güvenilir Play Developer API `purchases.subscriptionsv2.get` ve RTDN ile ACTIVE, GRACE, CANCELED (paid-through), ON_HOLD, PAUSED, EXPIRED, REFUNDED ve eski lifetime hakkı kombinasyonlarını doğrula.
3. Mevcut M1 backend taslağını yanlışlıkla açma; onun ebeveyn kimliği ve kod geçiş modeli ayrıca tamamlanmalı.
4. Internal test AAB ile satın alma, restore, yenileme, iptal, iade, hesap değiştirme, çevrimdışı kullanım ve eski kod testlerinden geç.
5. Ancak bunlar bitince bilerek `MM_SUBSCRIPTIONS_ENABLED=true` ver. Bayrak **sunucu doğrulamasının yerine geçmez**.

Play Console hesabına erişim veya yeni ücretli ürün aktivasyonu bu committe yapılmadı.
