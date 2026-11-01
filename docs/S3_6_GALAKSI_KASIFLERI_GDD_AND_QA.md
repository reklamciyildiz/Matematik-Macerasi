# S3.6 — Uzay Matematik Macerası 2.0 / Galaksi Kaşifleri

**Teslim:** Android Kotlin/Jetpack Compose, ana uygulamanın `SpaceMathAdventureActivity` giriş noktası. Play Store yayını yapılmaz.

## Oyun tasarım belgesi (GDD)
- 5 tematik gezegen: Zümrüt Yörünge (+), Okyanus Nebulası (−), Kızıl Meteor (×), Ay Kristali (÷), Prizma Galaksisi (karma).
- Her gezegende **3 farklı gerçek etkileşimli görev**:
  1. **Kristal Pilotluğu:** Üç şeritli gerçek zamanlı uzay yolu. Ekrandaki gemiyi soldan sağa sürükleyerek ya da 3 yön tuşuyla yönlendir; kristaller aşağıya inerken doğru işlem sonucunu taşıyan şeritte konumlan. Kristal gemiye gelince motor otomatik yakalar. Yanlış şerit kalkanı bir kademe düşürür (minimum 1), yanlış sayısını artırır, soruyu ve kristalleri aynı tutarak yeni dalgada tekrarlar. Temel 18 sn, Gelişen 15 sn, Ustalık 12 sn.
  2. **Reaktör Atölyesi:** Hedef işlemin sonucunu hesapla; gerçek pompa düğmeleriyle enerji haznesini +1/+5/-1/-5 artırıp azalt. Görsel doluluk göstergesini izle ve reaktörü ateşle. Yanlış ateşleme hata olarak kaydedilir, oyun sonlanmaz.
  3. **Yıldız Devresi:** Rastgele üretilen pozitif/negatif adımlı sayı dizisinin sonraki üç değerini bul; dört yıldız düğümünü **doğru sırada fiziksel dokunmayla** bağla. Hatalı bağlama ilerlemeyi silmez; doğru düğümler bağlı kalır.
- Görev açıklaması, yardım ipucu, geri bildirim ve isteğe bağlı duraklatma içerir. Arka plana geçince gemi otomatik durur.
- Bölüm bitirme: her gezegen için 0/1/2/3 yıldız high-water kaydı (1–3 arası), ilk deneme kanıtı ve hata/ipucu geçmişi. Tekrar oynama aynı yıldızları tekrar vermez.
- Çevrimdışı, zorunlu reklam yok, satın alma CTA yok. Eski ödeme/durum/yıldız kayıtları değiştirilmez.
- Görseller Android Compose Canvas ile özgün vektör kompozisyonlarıdır; **harici premium WebP illüstrasyon varlıkları bu teslimde yoktur**. Oyun mekaniğine öncelik verildi.

## Mimari
- `SpaceExpeditionEngine.kt`: saf Kotlin deterministik soru ve görev durum makinesi. Seed+gezegen+seviye+görev kombinasyonu tek sonuç üretir.
- `SpaceExpeditionScreen.kt`: kaydırılabilir galaksi haritası, dinamik gemi/asteroit/kristal sahnesi, enerji reaktörü, yıldız devre tahtası.
- `SpaceExpeditionStore.kt`: `space_expedition_v2` sürümlü `SharedPreferences`. Bir görev ortasında cihaz kapanırsa sahne/konum/enerji/dizi/ipuçları korunur.
- `SpaceMathAdventureActivity.kt`: önceki tek giriş noktası korunur. Mevcut `StarManager` anahtarları `SpaceMathAdventureActivity_Level_1..5` kullanılır. Tek completion -> tek DailyTask/statistics kaydı. `AdventureSkill.SPACE` üzerinden öğrenme uyarlaması.
- Eski `SpaceMathModels.kt` kullanılmayan miras model olarak korunmuştur; diğer sınıflar bağımlı olabileceğinden silinmedi.

## Test ve kabul
- Saf Kotlin JUnit: 5 gezegen × 3 seviye × 60 seed = **900 tam tur / 2700 görev**; rastgele işlem, kontrollü gemi yönü, pompa, devre zinciri, hata, ipucu, tekrar deneme, durum doğrulama ve ödül.
- Android Compose instrumented test: dünya seçimi, gerçek gemi yön tuşu ve reaktör pompası dokunma. GitHub CI test APK'sını derler; emülatörde gerçek UI testlerini çalıştırmaz.
- Fiziksel cihaz: Sol/sağ sürükleme ve yön tuşu, kristal zamanlaması, yanlış dalga/enerji, doğru dalga, reaktör ve devre görevleri, ipucu/yeniden oynama, 320dp ekran, cihaz arka plana al/geri dön, uçak modu, TalkBack ve yıldız yüksek su seviyesi incelenmeli.
- Play Console Billing/Families AAB kontrolleri hâlâ ayrı yayın kapılarıdır.
