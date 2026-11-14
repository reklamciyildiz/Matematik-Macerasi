# Matematik Macerası — Avatar 3.0 (koleksiyon + karakter sanat yönetimi)

## Karar

Mevcut **15** avatarı ve **1–15 ID** değerlerini koru. 6 Free + 9 Premium,
avatar açma/kolleksiyon, yıldız fiyatları, kullanıcıların kazandığı avatarlar
ve mevcut seçili karakter verileri korunmalıdır. Üretimdeki üyelik/doğrulama
altyapısına müdahale edilmez. Premium avatar, Premium üyelik kadar
**yıldız bedeli** de gerektirir; "Premium'a geçince 9 avatar otomatik açılır"
iddiası yanlıştır.

### Uygulanmış yazılım adımları

- Animasyonlu karakter **sadece büyük önizlemede**; mağaza kartları statik.
- Avatar renkleri karakter kimliğine göre **deterministik**; rasgele renk yok.
- Tek seçili karakter paneli, statü/bakiye/koleksiyon özeti, ekranda az metin.
- Uyarlanabilir kart ızgarası, Free/Premium/Önceden açılan filtreleri.
- Premium'a yükselt butonu doğrudan satın alma başlatmaz;
  standart Ayarlar ekranında **aylık/yıllık/ömür boyu** seçeneklerini gösterir.
- AvatarService yıldızları bir kez düşer; arayüz eski bakiyeyi tekrar
  hesaplayıp yazmaz. Unlock işlemi aynı anda tekrarlanamaz.
- Premium süresi dolan hesapların önceden yıldızla açılmış avatar ID'leri
  silinmez; ancak erişim, Premium yetkisi geri gelene dek kısıtlıdır.
- Önceki Premium kod eşitlemesi korunur, Play abonelik gözlemini bloke etmez.

## Sanat yönü

2.5D hissi veren, canlı renkli fantastik adalar evreni.
Her karakter ayrı bir kişilik, benzer görsel ağırlık ve perspektif,
tek ana ışık (sol üst) ve ince mavi/altın rim light taşımalı.
Küçük yaşa uygun; komik, sempatik, korkutmayan siluetler.
Görselin içinde **yazı, fiyat, yıldız rozeti, kilit ve çerçeve yok**:
kilit/rozet/UI uygulamanın sorumluluğunda.

| ID | Var olan ad (değişmez) | Erişim | Karakter yönü |
|---:|---|---|---|
| 1 | Sıfır Sayısı | Free | Sevimli, canlı sıfır maskotu |
| 2 | Matematik Robotu | Free | Turkuaz yardımcı robot |
| 3 | İşlem Canavarı | Free | Sevimli dört-işlem yaratığı |
| 4 | Matematik Çocuk | Free | Meraklı genç matematikçi |
| 5 | Dahi Kız | Free | Kitaplı, gözlüklü yaratıcı kaşif |
| 6 | Kaşif Öğrenci | Free | Pusulalı, sırt çantalı ada kâşifi |
| 7 | Matematik Büyücüsü | Premium | Mor-altın büyücü, matematik sembolleri |
| 8 | Sayıların Piri | Premium | Sayı tomarları taşıyan bilge |
| 9 | Şekiller Dehası | Premium | Renkli geometri mucidi |
| 10 | Abaküs Beyin | Premium | Abaküs kullanan mucit |
| 11 | Heyecanlı Matematikçi | Premium | Coşkulu problem çözücü |
| 12 | Bilge Çocuk | Premium | Parlayan kitaplı bilge çocuk |
| 13 | Einstein | Premium | Tarihi bilim insanının çocuk dostu karikatürü |
| 14 | Şekiller Uzmanı | Premium | Geometrik araçlar kullanan uzman |
| 15 | Zeka Küpü | Premium | Canlı 3D bulmaca küpü |

## Kaynak standartları

**Önce prototip:** ID 6 (Free Kaşif Öğrenci) ve ID 7 (Premium Matematik
Büyücüsü). Kullanıcı konseptteki kalite ve yönü onayladıktan sonra 13'ü
aynı karakter stiline dönüştür. Mevcut avatar dosyaları **bu aşamada
silinmez / değiştirilmez**.

**Üretim asseti gereksinimleri:**
- Her karakter ayrı, merkezlenmiş, şeffaf arka planlı 1024×1024 WebP.
- Karakter tüm gövdesiyle görünür, kenarlardan en az %12 boşluk.
- Mobil 78dp kart ve 96dp büyük önizleme için iki ölçekte okunabilir.
- Harici font, telifli karakter, watermark, reklam ve yazı içermez.
- Hedef sıkıştırma <150 KB/karakter; güçlü görsellik önemliyse
  gerekçelendirilmiş istisnalar kayıt altına alınabilir.
- Üretim uygulaması dosya eşlemesi: `res/drawable-nodpi/avatar3_06.webp`,
  `avatar3_07.webp` vb. Kaynaklar eklendiği commit'te
  `AvatarService` resource mapping güncellenmeli.
- Premium/Free ayrımı *erişim* düzeyindedir; resim kalitesi Free'yi
  kasıtlı olarak düşük göstermemeli.
- Lottie veya kısa animasyon yalnızca önizlemede, çoklu kartta asla.

**Konsept paketleri:** ChatGPT oturumunda üretilen iki mağaza/karakter
mockup görseli ve 15 avatar asset haritası kullanıcıya ZIP olarak teslim edilir.
Bu görseller temiz karakter WebP dosyaları olmadığından **Android
drawable dizinine doğrudan kopyalanmaz**.

## Doğrulama kapısı

1. Android CI, debug derlemesi, mevcut Google Play/Cloudflare ödeme testi.
2. 15 ID ve açık avatarlar Android güncellemesi öncesinde/sonrasında aynı.
3. Free hesapta 7–15 erişim kapalı; Premium hesapta yıldızla açılabiliyor.
4. Premium sona erdiğinde sahiplik silinmiyor; seçme engelleniyor.
5. Yıldız harcaması tek kez ve bakiye kalıcı; üst üste dokunmada çift düşme yok.
6. Mağazada ızgara sürükleme akıcı; aynı anda 15 animasyon çalışmıyor.
7. Premium yönlendirmesi üç planlı Ayarlar ekranını açıyor.
8. 320dp telefon, tablet, karanlık mod, sistem yazı büyütmesi için QA.
9. **Gerçek WebP karakter üretimi henüz tamamlanmadıysa tüm 15 karakterin
   grafiksel yenilenmesi yapıldı iddia edilmez.**
