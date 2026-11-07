package com.yigit033.matematikmacarasi.learning

/** Expert-reviewable, authored worked examples. All units have their own
 * mathematical model, misconception prompt and differentiated explanation.
 */
object ExpandedLessonTeachingCatalog {
    private fun s(
        visual: TeachingVisual, title: String, early: String, older: String,
        groups: List<Int>, caption: String, steps: List<String>,
        question: String, right: String, wrong: String, explanation: String
    ): LessonTeachingScript = LessonTeachingScript(
        visual, title, early, older,
        groups = groups, diagramCaption = caption,
        steps = steps,
        check = LessonConceptCheck(question, listOf(right, wrong), 0,
            explanation, "Çizime bak ve adımdaki işlemi yeniden oku.")
    )

    private fun numberLine(
        title: String, early: String, older: String, sequence: List<Int>,
        caption: String, steps: List<String>,
        question: String, right: String, wrong: String, explanation: String
    ): LessonTeachingScript = LessonTeachingScript(
        TeachingVisual.NUMBER_STEPS, title, early, older,
        sequence = sequence, diagramCaption = caption, steps = steps,
        check = LessonConceptCheck(question, listOf(right, wrong), 0,
            explanation, "Sayı doğrusunda başlangıç ve yönü kontrol et.")
    )

    fun forLesson(lesson: Lesson): LessonTeachingScript = when (lesson.id) {
        "g1_count20" -> s(TeachingVisual.PLACE_VALUE,
            "Bir onluk ve birlikleri say",
            "Bu onlukta 10 çubuk var. Dört tane daha ekle. 14 oldu.",
            "10'luk bir grubu ve dört birliği birlikte göster.",
            listOf(1,4), "10 + 4 = 14",
            listOf("Önce onluk grubu say: 10.",
                   "Onluğun yanına dört birlik koy.",
                   "10'dan sonra 11, 12, 13, 14 diye say."),
            "14 sayısı hangi iki gruptan oluşur?",
            "1 onluk ve 4 birlik.","4 onluk ve 1 birlik.",
            "Onluk, on tane birliktir; yanındaki dört birlikle 14 olur.")

        "g1_takeaway" -> s(TeachingVisual.TAKE_AWAY,
            "Eksilince geriye ne kalır?",
            "Yedi elma var. Üçünü ayır. Kalanları say.",
            "Çıkarma, bütünden alınan kısmı ayırıp kalanı bulmaktır.",
            listOf(7,3), "7 − 3 = 4",
            listOf("Önce yedi elmanın hepsini say.",
                   "Üç elmanın üzerini çizerek ayır.",
                   "Geriye kalan dört elmayı say."),
            "Çıkarma yaptığımızda ne arıyoruz?",
            "Kalan nesne sayısını.","Yeni eklenen nesnelerin toplamını.",
            "Yediden üç elma çıkarıldı; geriye dört elma kaldı.")

        "g1_shapes" -> s(TeachingVisual.SHAPE,
            "Şekillerin kenarlarını takip et",
            "Üçgenin üç düz kenarı var. Karenin dört kenarı var.",
            "Şekilleri adından önce kenar sayısıyla karşılaştır.",
            listOf(3,4), "Üçgen: 3 kenar · Dörtgen: 4 kenar",
            listOf("Üçgenin çevresini izle: bir, iki, üç kenar.",
                   "Dörtgenin çevresini izle: bir, iki, üç, dört kenar.",
                   "İki şeklin köşe ve kenar sayılarını karşılaştır."),
            "Üçgen ile kare arasında hangi fark var?",
            "Üçgenin 3, karenin 4 kenarı vardır.",
            "İkisinin de 4 kenarı vardır.",
            "Üçgen üç kenarlı, kare dört kenarlı kapalı bir şekildir.")

        "g2_place" -> s(TeachingVisual.PLACE_VALUE,
            "Sayıları onluk ve birlikle kur",
            "Üç onluk var: 30. Yanına dört birlik ekle: 34.",
            "Basamak değeri: 3 onluk + 4 birlik = 30 + 4.",
            listOf(3,4), "30 + 4 = 34",
            listOf("Üç ayrı onluk grubu oluştur.",
                   "Onluklar toplam otuz eder.",
                   "Dört birlik ekleyince otuz dört olur."),
            "34 sayısındaki 3 neyi gösterir?",
            "3 onluğu yani 30'u.","3 birliği.",
            "Onlar basamağındaki 3 sayısı 30 değerindedir.")

        "g2_half" -> s(TeachingVisual.FRACTION_BAR,
            "Bütünü iki eş parçaya ayır",
            "Bir bütünü tam ortadan ikiye ayır. Bir parça yarımdır.",
            "1/2, bir bütünün iki eş parçasından biridir.",
            listOf(1,2), "1/2 = yarım",
            listOf("Önce bütünü incele.",
                   "Bütünü iki eş parçaya ayır.",
                   "İki eş parçanın birini seç: bu yarımdır."),
            "İki parçadan biri daha büyükse gerçekten yarım olur mu?",
            "Hayır, parçalar eş büyüklükte olmalı.",
            "Evet, iki parça olması yeterli.",
            "Kesirde bütün eş parçalara ayrılır.")

        "g2_clock" -> s(TeachingVisual.CLOCK,
            "Saatte zaman ileri gider",
            "Akrep üçü gösteriyor. İki saat ilerleyelim: dört, beş.",
            "Tam saatlerde yelkovan 12'yi gösterir; akrep saatleri takip eder.",
            listOf(3,5), "03.00 + 2 saat = 05.00",
            listOf("Akrep 3 üzerinde; saat üç.",
                   "Bir saat ilerle: akrep 4'e gelir.",
                   "Bir saat daha ilerle: akrep 5'i gösterir."),
            "Tam saati gösteren yelkovan nerededir?",
            "12'nin üzerindedir.","6'nın üzerindedir.",
            "Yelkovan 12'deyken akrebin gösterdiği tam saat okunur.")

        "g3_third" -> s(TeachingVisual.FRACTION_BAR,
            "Üç eş parçadan biri",
            "Bir bütünü üç eş parçaya ayır. Bir parçayı göster.",
            "1/3, bütünün üç eş payından biridir.",
            listOf(1,3), "1/3 = üçte bir",
            listOf("Bir bütün oluştur.",
                   "Bütünü üç eş parçaya ayır.",
                   "Bir parçayı seç; bu üçte birdir."),
            "Üçte biri göstermek için bütün nasıl bölünür?",
            "3 eş parçaya.","3 farklı büyüklükte parçaya.",
            "Payda 3 olduğu için bütün üç eş parçaya ayrılır.")

        "g3_money" -> s(TeachingVisual.TAKE_AWAY,
            "Para üstünü çıkararak bul",
            "On tane beş liralık para düşün. Yedisini harca. Üçü kalır.",
            "50 TL'den 35 TL çıkarılırken 5 TL'lik birimler kullanılabilir.",
            listOf(10,7), "50 TL − 35 TL = 15 TL",
            listOf("Elimizde 10 tane 5 TL var: 50 TL.",
                   "Ürünün bedeli 7 tane 5 TL: 35 TL.",
                   "Kalan 3 tane 5 TL, yani 15 TL para üstüdür."),
            "Para üstünü bulmak için hangi işlemi yaparız?",
            "Ödenen paradan ürün fiyatını çıkarırız.",
            "Ödenen parayla fiyatı toplarız.",
            "Para üstü verilen para eksi ürün bedelidir.")

        "g3_chart" -> s(TeachingVisual.BAR_CHART,
            "Sütunların boylarını karşılaştır",
            "Sekiz kitap var, beş defter. Hangisi daha çok?",
            "İki veri grubunun farkı sütun yükseklikleri çıkarılarak bulunur.",
            listOf(8,5), "8 − 5 = 3 fazla",
            listOf("Kitap sütunundaki 8 birimi say.",
                   "Defter sütunundaki 5 birimi say.",
                   "Büyük değerden küçüğü çıkar: üç fazla."),
            "İki sütunun farkını bulmak için ne yapılır?",
            "Büyük değerden küçük değer çıkarılır.",
            "Sütunlar yan yana konup değerleri toplanır.",
            "8 ile 5 arasındaki fark 3'tür.")

        "g4_fraction" -> s(TeachingVisual.FRACTION_BAR,
            "Pay ve paydanın görevi",
            "Dört eş parçanın üçünü boya.",
            "3/4 kesrinde payda dört eş parçayı, pay üç seçilen parçayı gösterir.",
            listOf(3,4), "3/4 = dört parçadan üçü",
            listOf("Bütünü dört eş parçaya böl.",
                   "Pay 3 olduğu için üç parçayı işaretle.",
                   "3/4'ü farklı bütünlerde de gösterebilirsin."),
            "3/4 kesrindeki 4 neyi belirtir?",
            "Bütünün kaç eş parçaya ayrıldığını.",
            "Kaç parçanın seçildiğini.",
            "Payda dört, bütünün dört eş parçaya bölündüğünü belirtir.")

        "g4_perimeter" -> s(TeachingVisual.PERIMETER,
            "Dikdörtgenin dört kenarı",
            "Uzun kenar beş, kısa kenar üç. Hepsini topla.",
            "Dikdörtgenin çevresi iki uzun ve iki kısa kenarın toplamıdır.",
            listOf(5,3), "5 + 3 + 5 + 3 = 16",
            listOf("Üst ve alt kenarın her biri 5 cm.",
                   "Sağ ve sol kenarın her biri 3 cm.",
                   "Dört kenarı ekle; çevre 16 cm."),
            "Çevreyi bulurken neyi toplarız?",
            "Şeklin tüm kenar uzunluklarını.","İçindeki kareleri.",
            "Çevre sınırı dolaşan toplam uzunluktur.")

        "g4_chart" -> s(TeachingVisual.BAR_CHART,
            "Üç sütundaki toplam",
            "Dört, altı ve üç nesne var. Hepsini say.",
            "Grafikte toplam, bütün sütun değerlerinin birleşimidir.",
            listOf(4,6,3), "4 + 6 + 3 = 13",
            listOf("İlk sütun dört birim.",
                   "İkinci sütun altı birim.",
                   "Üçüncü sütunu ekle: toplam on üç."),
            "Tüm veri sayısını nasıl hesaplarız?",
            "Bütün sütunların değerlerini toplarız.",
            "Yalnızca en yüksek sütuna bakarız.",
            "Toplamın her kategorideki veriyi içermesi gerekir.")

        "g5_decimal" -> s(TeachingVisual.DECIMAL,
            "Tamlar ve onda birler",
            "Üç bütün ve yedi onda bir göster.",
            "3,7 = 3 tam + 7/10; hepsini onda birle yazarsak 37/10.",
            listOf(3,7), "3,7 = 37 onda bir",
            listOf("Üç tamı göster: her biri 10 onda birdir.",
                   "Üç tam toplam 30 onda birdir.",
                   "Yedi onda bir daha ekle: 37 onda bir."),
            "3,7 sayısındaki 7 hangi basamakta?",
            "Onda birler basamağında.","Onlar basamağında.",
            "Virgülden sonraki ilk basamak onda birler basamağıdır.")

        "g5_area" -> s(TeachingVisual.AREA_GRID,
            "Alanı birim karelerle bul",
            "Beş karelik üç satır çiz.",
            "Dikdörtgen alanı uzunluk × genişliktir. 5 × 3 = 15.",
            listOf(5,3), "5 × 3 = 15 birim kare",
            listOf("Her satırda beş birim kare var.",
                   "Toplam üç satır var.",
                   "Satır ve sütun sayılarını çarp: 15 birim kare."),
            "Alan hangi birimle ifade edilir?",
            "Birim kare ile.","Yalnızca uzunluk birimiyle.",
            "Alan iki boyutlu yer kaplamayı ölçtüğü için kare birimle ifade edilir.")

        "g5_mean" -> s(TeachingVisual.BAR_CHART,
            "Verilerin denge noktası",
            "4, 6 ve 8 sayıları var; ortadaki 6'yı bul.",
            "Aritmetik ortalama, toplamı veri sayısına bölerek bulunur.",
            listOf(4,6,8), "(4 + 6 + 8) ÷ 3 = 6",
            listOf("Verileri topla: 4 + 6 + 8 = 18.",
                   "Kaç veri var? Üç.",
                   "Toplamı üçe böl: ortalama altı."),
            "Ortalama hesaplarken toplam neye bölünür?",
            "Veri sayısına.","En büyük veri değerine.",
            "Bütün verilerin toplamı, kaç değer olduğuna bölünür.")

        "g6_signed" -> numberLine(
            "Negatiften pozitife ilerle",
            "Sıfırın solundan sağa doğru say.",
            "−6 noktasından +9 sağa ilerlemek +3 noktasına ulaştırır.",
            listOf(-6,-3,0,3), "−6 + 9 = 3",
            listOf("Başlangıç noktası −6.",
                   "Sağa üç adım: −3'e gel.",
                   "Bir üç adım daha: sıfıra ulaş.",
                   "Son üç adım: +3'e ulaş."),
            "Sayı doğrusunda sağa gitmek ne anlama gelir?",
            "Sayıyı artırmak.","Sayıyı azaltmak.",
            "Sağa doğru hareket sayı değerini artırır.")

        "g6_ratio" -> s(TeachingVisual.BAR_CHART,
            "Oranı koruyarak ölçekle",
            "İki meyveye üç su. Dörde altı su gerekir.",
            "2:3 oranını ikiyle genişletince 4:6 olur.",
            listOf(2,3,4,6), "2 : 3 = 4 : 6",
            listOf("İlk tarifte 2 meyve, 3 su var.",
                   "Meyveyi iki katına çıkar: 4.",
                   "Su miktarını da iki katına çıkar: 6."),
            "Oranı korumak için ne yapılır?",
            "İki miktar da aynı katla değiştirilir.",
            "Yalnızca bir miktar ikiyle çarpılır.",
            "Orantıda iki miktarın birbirine oranı değişmemelidir.")

        "g6_equation" -> s(TeachingVisual.EQUATION,
            "Eşitliğin iki tarafı",
            "x'e yedi eklenmiş ve on dokuz olmuş. Yediyi geri al.",
            "x + 7 = 19 denkleminde iki taraftan 7 çıkarılır.",
            listOf(7,19), "x + 7 = 19 → x = 12",
            listOf("Sol tarafta x + 7 var.",
                   "Her iki taraftan yedi çıkar.",
                   "x tek kalır: x = 12."),
            "Eşitliğin bir tarafında 7 çıkarırsan ne yapmalısın?",
            "Diğer taraftan da 7 çıkarmalısın.",
            "Diğer tarafa 7 eklemelisin.",
            "Denklem çözerken iki tarafa da aynı işlem uygulanır.")

        "g7_rational" -> s(TeachingVisual.FRACTION_BAR,
            "Denk kesirleri tanı",
            "Bir bütünün üç parçasından ikisini boya.",
            "2/3 kesrinin payı ve paydası üçle çarpılırsa 6/9 elde edilir.",
            listOf(2,3), "2/3 = 6/9",
            listOf("Bütün üç eş parçaya ayrıldı.",
                   "Üç parçanın ikisi seçili.",
                   "Her parçayı üçe böl; altı seçili / dokuz toplam parça."),
            "Denk kesir oluştururken neyi değiştiririz?",
            "Payı ve paydayı aynı sayı ile.",
            "Yalnızca payı.",
            "Pay ve payda aynı katla değişirse kesrin değeri sabit kalır.")

        "g7_proportion" -> s(TeachingVisual.BAR_CHART,
            "Birim değerden orantıya",
            "İki ürün altı lira ise dört ürün on iki liradır.",
            "Birim fiyat 3 TL; miktar iki kat olduğunda toplam bedel de iki kat olur.",
            listOf(2,6,4,12), "2 ürün = 6 TL · 4 ürün = 12 TL",
            listOf("İki ürün altı TL. Bir ürün üç TL.",
                   "Ürün sayısını dört yap; iki katına çıkar.",
                   "Fiyatı da iki katına çıkar: on iki TL."),
            "Doğru orantıda miktar iki katına çıkarsa fiyat ne olur?",
            "Fiyat da iki katına çıkar.","Fiyat yarıya iner.",
            "Birim fiyat aynı kalırsa toplam bedel miktarla aynı oranda değişir.")

        "g7_equation" -> s(TeachingVisual.EQUATION,
            "Katsayıyı ve sabiti ayır",
            "Üç x ve beş toplam yirmi. Önce beşi çıkar.",
            "3x + 5 = 20; 3x = 15 ve x = 5.",
            listOf(3,5,20), "3x + 5 = 20 → x = 5",
            listOf("Her iki taraftan beş çıkar: 3x = 15.",
                   "Üç x'in toplamı on beş.",
                   "Üçe böl; x = 5."),
            "3x + 5 = 20 denkleminde ilk uygun adım nedir?",
            "Her iki taraftan 5 çıkarmak.","Sadece soldaki 3'ü silmek.",
            "Önce sabit terimi diğer tarafa çıkarma işlemiyle taşı.")

        "g8_gcd" -> s(TeachingVisual.FACTORS,
            "Ortak bölenleri karşılaştır",
            "12 ile 18'i eş gruplara ayır. Ortak en büyük grup altıdır.",
            "12'nin ve 18'in ortak bölenlerinin en büyüğü 6'dır.",
            listOf(12,18), "EBOB(12,18) = 6",
            listOf("12'nin bölenlerini sırala: 1,2,3,4,6,12.",
                   "18'in bölenlerini sırala: 1,2,3,6,9,18.",
                   "Ortak bölenlerin en büyüğü altıdır."),
            "EBOB bulurken hangi sayı seçilir?",
            "Her iki sayıyı bölen en büyük sayı.",
            "İki sayının toplamı.",
            "EBOB iki sayının en büyük ortak bölenidir.")

        "g8_power" -> s(TeachingVisual.POWER_CHAIN,
            "Üssün anlamını göster",
            "İkiyi üç kez çarp: iki çarpı iki çarpı iki.",
            "Üs 3, taban 2'nin çarpımda üç kez yer aldığı anlamına gelir.",
            listOf(2,2,2), "2³ = 2 × 2 × 2 = 8",
            listOf("İlk iki çarpan: 2 × 2 = 4.",
                   "Bir 2 daha çarp: 4 × 2 = 8.",
                   "Taban 2, üs 3, sonuç 8."),
            "2³ ifadesinde üs 3 neyi söyler?",
            "Tabanın üç kez çarpıldığını.",
            "Tabana üç ekleneceğini.",
            "Üs, tekrar eden çarpma sayısını gösterir.")

        "g8_equation" -> s(TeachingVisual.EQUATION,
            "Parantezli denklemi çöz",
            "x artı üç önce ikiyle çarpılmış. Toplam on sekiz.",
            "2(x + 3) = 18. Önce 2'ye bölüp sonra 3 çıkar.",
            listOf(2,3,18), "2(x + 3) = 18 → x = 6",
            listOf("İki tarafı ikiye böl: x + 3 = 9.",
                   "İki taraftan üç çıkar.",
                   "x = 6. Yerine koyup kontrol et: 2 × 9 = 18."),
            "2(x + 3) = 18 için ilk kolay adım hangisidir?",
            "Her iki tarafı 2'ye bölmek.","Sadece x'i ikiye bölmek.",
            "Parantezin tamamı ikiyle çarpılmış; iki tarafı da aynı sayıya böl.")
        else -> error("Missing authored teaching script for " + lesson.id)
    }
}
