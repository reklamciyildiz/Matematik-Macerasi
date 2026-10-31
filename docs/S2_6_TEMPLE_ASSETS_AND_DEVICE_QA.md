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
