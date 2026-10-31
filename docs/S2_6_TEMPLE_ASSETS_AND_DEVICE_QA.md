# Antik Tapınak — GitHub Görsel Entegrasyon Haritası (2026-11-01)

## Doğrudan projeye eklenen Android görselleri

| Android drawable adı | GitHub dosyası | Kullanım |
|---|---|---|
| `temple_art_hero` | `app/src/main/res/drawable/temple_art_hero.xml` | Tapınak giriş ekranı ve tamamlanma |
| `temple_art_mosaic` | `app/src/main/res/drawable/temple_art_mosaic.xml` | 1. ritüel / Mozaik Odası |
| `temple_art_pillars` | `app/src/main/res/drawable/temple_art_pillars.xml` | 2. ritüel / Denge Sütunları |
| `temple_art_gate` | `app/src/main/res/drawable/temple_art_gate.xml` | 3. ritüel / Kayıp Çarpan Kapısı |
| `temple_art_mirror` | `app/src/main/res/drawable/temple_art_mirror.xml` | 4. ritüel / Ayna Mekanizması |

**Format kararı:** Bu dosyalar SVG web dosyası değil, Android'in yerel **VectorDrawable XML** formatıdır. Kotlin `painterResource(R.drawable.temple_art_...)` ile doğrudan kullanılır. Android 24+ desteği vardır, ağ bağlantısı gerekmez. Çözünürlükten bağımsız çizilir, ayrıca SVG dönüştürme veya manuel asset taşıma adımı yoktur.

**Görsel tasarım sınırı:** GitHub'a eklenenler oyun içi hafif, ölçeklenebilir vektör çizimleridir. Önceki ChatGPT sohbetinde üretilen detaylı 3D bitmap görselleri değildir; onların GitHub'a aktarıldığı iddia edilmemelidir.

## Compose entegrasyonu

- `TempleAdventureScreen.kt`: giriş/oda resimleri, art paneli, hafif sihirli parıltı animasyonu, oda görsel geçişi, altın temalı butonlar.
- `app/build.gradle`: `androidx.compose.animation:animation` açık bağımlılığı.
- Ritüel içi taş/sütun/grup adımları cihazda yerel `temple_adventure_v1` tercihlerine yazılır. Tamamlanma ilerlemesi ve önceki sayılar korunur.
- Premium satın alma, reklam, genel yıldız ve Kumsal/Orman kaydına dokunulmadı.

## Derleme

```powershell
git pull origin main
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug
```

## Telefon kabul kontrolü

1. Sayı Avcıları atlasından Antik Tapınak'a gir; önceki bölgeler ve mevcut yıldızlar aynı kalsın.
2. Girişte farklı tapınak resmi, her ritüelde farklı oda resmi ve altın/turkuaz tema görünsün.
3. Mozaik: satır ekle, kapat/aç, kaldığın taştan devam et; bütün taşlar yerleşmeden geçiş olmasın.
4. Denge: sütunları döndür, resmi/eşitlik dizilimini kontrol et, kapat/aç ve geç.
5. Kapı: gruplara birer taş dağıt, geri al, kapat/aç; doğru eş gruplar tamamlanmadan açılmasın.
6. Ayna: yanlış yanıt ve ipucu dene, kapat/aç; doğru yanıt sonrasında yeniden açınca skor bozulmasın.
7. Dördüncü ritüel bitince sonuç raporundaki deneme/hata/ipucu değerlerini kontrol et.
8. Yeniden oyna; yeni sorular gelirken eski tamamlanma sayısı korunmalı, eski yıldızlar artmamalı.
9. En az bir küçük ekran ve TalkBack ile okunabilirlik/dokunma testleri; offline modda yeniden çalıştır.

**Kabul durumu:** GitHub'a kod ve görsel dosyaları yazıldı; Android SDK ortamında Gradle derlemesi ve gerçek cihaz testi bu sohbet içinde çalıştırılmadı. Bu testler geçmeden sürüm “sorunsuz” veya “release-ready” olarak kabul edilmemelidir.

## S2.6 ekran yerleşimi kabulü (2026-11-01)

- **Giriş / tamamlama**: Genel `temple_art_hero` yalnızca bu iki ekranda görünür.
- **Oyun içinde**: Tek bir oda görseli gösterilir; eski üst hero tekrar edilmez.
- **İlk ekran alanı**: Haritaya dön butonu, seviye, ritüel/ilerleme, oda başlığı ve tek oda görseli kompakt düzendedir; matematik görevi gereksiz kaplamaların arkasında kalmaz.
- **Erişilebilir kontrast**: Geri, ipucu ve cevap seçenekleri varsayılan mor renk yerine tapınak için açık renk kullanır.
- **Navigasyon**: Ritüel değişiminde / yeniden oynamada liste başa kayar.
- **Regresyon**: Ustalık seviyesinde büyük çarpan dizilimleri, 3-9 grup ekranı ve 3 cevaplı Ayna ekranı küçük telefonda kontrol edilmeli.
- **Gerçek cihaz geçiş koşulu**: Her ritüel başlamadan oyun içindeki yalnızca bir görselin ve matematik kontrolünün görünür olduğunu doğrula. Android derlemesi ve TalkBack/gerçek cihaz kabulü hâlâ yerelde yapılmalıdır.

## Öncelikli devam planı

1. **S2.6 Kabul**: Derleme + telefonlarda ritüel 1-4, geri dönüş, yeniden başlatma, offline, yön değiştirme, küçük ekran.
2. **S2.7 Öğrenme kalitesi**: Tek hamlede ilerleme yerine cevap kalitesini ölçme, yanlış öğrenilene kısa tekrar, beceri bazlı geribildirim ve çocuk/öğretmen pilotu.
3. **S2.8 Rapor ürünleştirmesi**: Öğrenci profili, ebeveyn/öğretmen için oturum ve kazanım ilişkilendirmesi; ölçülmeyen kazanımlar hakkında iddia yok.
4. **S2.9 Yayın kalitesi / gelir**: Paket ve mağaza politikaları, erişilebilirlik, performans, satın alma/test.
