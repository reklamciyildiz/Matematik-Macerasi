# Matematik Macerası 2.0 — Ana Sayfa H2 Asset Haritası

## Paket dizini
Paketi `C:\active_projects\MatematikMacerasiii\design_references\home_v2\` altına aç.

| Dosya | Rol | Teknik not |
|---|---|---|
| `reference/HOME_APPROVED_REFERENCE.png` | Kullanıcının onayladığı **tek** görsel referans | Uygulamada tek parça ekran olarak kullanma; UI metinleri gömülü |
| `assets/home/hero_fantasy_island_explorer.png` | Üst hero arka plan + kaşif | RGB opak, karakter ve fon birlikte; right-side dinamik görev panelini Compose üst katmanda konumlandır; kaynak görüntüde metin yok |
| `assets/home/card_four_operations.png` | Dört İşlem kartı | 1:1 RGB; kartta `Crop` gerekebilir; metni Compose ekle |
| `assets/home/card_balloon_pop.png` | Balon Patlatma kartı | 1:1 RGB; metni Compose ekle |
| `assets/home/card_number_hunt.png` | Sayı Avı ve uygun öneri kartı | 1:1 RGB; metni Compose ekle |
| `assets/home/card_space_adventure.png` | Uzay Macerası kartı | 1:1 RGB; metni Compose ekle |

## Önemli sınırlamalar
- **Şeffaf varlıklar değiller**: hero ve dört kart resminde gökyüzü/ada arka planları bulunuyor. Kesip şeffafmış gibi göstermek yanlış. Tekil şeffaf maskot ve parşömen ayrı üretilecek.
- Parşömen dekoru, açık kitap/Öğren kartı ve birkaç ortak UI ikonu şu anda **pakette yok**; bunları H2 raporunda eksik olarak belirt.
- Görsel referanstaki 245 yıldız, ikinci para birimi, demo seviyeler ve metinler **gerçek veri değil**. Üründeki mevcut veri ve servisleri kullan.
- Ayrı görsellerden kurulan gerçek Compose katmanları zorunlu. Referans tam ekran ekran görüntüsünü UI diye gösterme.
