package com.yigit033.matematikmacarasi.learning

/**
 * Authored, step-by-step conceptual teaching for Ö6 deep lessons.
 * Every lesson asks WHY, not only what numerical answer to choose.
 */
object DeepLessonTeachingCatalog {
    private fun script(
        visual: TeachingVisual, title: String, early: String, later: String,
        values: List<Int>, caption: String, steps: List<String>,
        check: String, right: String, wrong: String, reason: String
    ) = LessonTeachingScript(
        visual, title, early, later,
        groups = if (visual == TeachingVisual.NUMBER_STEPS) emptyList() else values,
        sequence = if (visual == TeachingVisual.NUMBER_STEPS) values else emptyList(),
        diagramCaption = caption, steps = steps,
        check = LessonConceptCheck(check, listOf(right, wrong), 0, reason,
            "Çizimin hangi sayıları gösterdiğine bakıp adımları yeniden incele.")
    )

    fun forLesson(lesson: Lesson): LessonTeachingScript = when (lesson.id) {
        "g1_make_ten" -> script(TeachingVisual.GROUPS,
            "On olana kadar ekle","Yedi elma var. Üç tane ekle. On oldu.",
            "7 ile 3 birleşince bir tam onluk oluşturur.",listOf(7,3),"7 + 3 = 10",
            listOf("Önce yedi nesneyi say.","On olması için üç nesne daha ekle.",
                "Birlikte on nesne var."),
            "7'yi 10 yapmak için hangi işlem gerekir?",
            "Üç nesne eklemek.","Üç nesne çıkarmak.",
            "7 + 3 = 10 olduğu için üç nesne eklenir.")

        "g1_compare_data" -> script(TeachingVisual.BAR_CHART,
            "Fazla olanları bul","Altı ve dört nesneyi yan yana koy.",
            "A=6 ve B=4 gruplarındaki fazla miktar 6−4 ile bulunur.",
            listOf(6,4),"6 − 4 = 2 fazla",
            listOf("İlk grupta altı nesne var.","İkinci grupta dört nesne var.",
                "Dört taneyi eşleştir; ilk grupta iki fazla kalır."),
            "Farkı bulmak için neyi yapmalıyız?",
            "Büyük miktardan küçüğü çıkarmalıyız.",
            "Grupların hepsini toplamalıyız.",
            "İki grubun ortak kısmı eşleşir; fazlalık iki nesnedir.")

        "g2_quarter" -> script(TeachingVisual.FRACTION_BAR,
            "Bir Bütünün Çeyreği","Bütünü dört eş parçaya böl. Birini boya.",
            "Bir bölü dört, bütündeki dört eş parçanın biridir.",
            listOf(1,4),"1/4 = çeyrek",
            listOf("Bütünü dört eş parçaya ayır.","Parçaların boyutlarının eşit olduğuna bak.",
                "Dört parçadan birini boya."),
            "Çeyreği gösteren parça nasıl olmalıdır?",
            "Dört eş parçadan biri.","İstediğimiz büyüklükte herhangi bir parça.",
            "Bir bölü dört ifadesinde bütün dört eş parçaya ayrılır.")

        "g2_hundred_place" -> script(TeachingVisual.EQUATION,
            "Yüzlük, Onluk, Birlik","Bir yüzlük yüz tane birliktir.",
            "123 sayısı 100 + 20 + 3 diye çözülür.",
            listOf(1,2,3),"100 + 20 + 3 = 123",
            listOf("Bir yüzlük 100 eder.","İki onluk 20 eder.",
                "Üç birlik ekleyince 123 oluşur."),
            "123 sayısındaki 2 neyi anlatır?",
            "İki onluğu yani 20'yi.","İki yüzlüğü.",
            "2, onlar basamağındadır: değeri 20'dir.")

        "g3_share_equal" -> script(TeachingVisual.EQUAL_GROUPS,
            "Herkese Eşit Dağıt","Üç çocuğa altışar nesne verelim.",
            "18 nesne üç eş gruba bölünürse her grupta 6 olur.",
            listOf(6,6,6),"18 ÷ 3 = 6",
            listOf("On sekiz nesneyi bir araya getir.","Üç eş gruba dağıt.",
                "Her grupta altışar nesne kaldı."),
            "Eşit paylaşmada nasıl gruplar oluşturulur?",
            "Her gruptaki nesne sayısı eşit olur.",
            "Bir gruba diğerinden daha çok nesne konur.",
            "18'i üçe bölerken her çocuk altı nesne alır.")

        "g3_quarter_of_set" -> script(TeachingVisual.FRACTION_BAR,
            "Bir Nesne Grubunun Dörtte Biri","Yirmi nesneyi dört eş gruba ayır.",
            "20/4 = 5; her eş grup toplamın dörtte biridir.",
            listOf(1,4),"20 ÷ 4 = 5",
            listOf("Bütün yirmi nesne.","Dört eş gruba beşer nesne koy.",
                "Bir grup, bütünün dörtte biridir."),
            "Yirminin dörtte biri ne anlama gelir?",
            "Dört eş gruptan birinin büyüklüğü.",
            "Yirmiye dört eklenmesi.",
            "Bütün dört eş parçaya ayrılır ve bir parça alınır.")

        "g4_same_denominator" -> script(TeachingVisual.FRACTION_BAR,
            "Paydaları Eşit Kesirler","Sekiz eş parçanın üçünü ve beşini karşılaştır.",
            "3/8 ve 5/8: payda aynıysa daha çok boyalı parçası olan büyüktür.",
            listOf(3,8),"3/8 < 5/8",
            listOf("İki bütün sekiz eş parçaya ayrılmış.",
                "Birinde üç, diğerinde beş parça seçili.",
                "Beş seçili parça daha çok olduğu için 5/8 büyüktür."),
            "Paydalar aynı olduğunda kesir nasıl karşılaştırılır?",
            "Payı büyük olan daha büyüktür.",
            "Payı küçük olan daha büyüktür.",
            "Birçok eş parçadan daha fazlasını almak daha büyük bir miktardır.")

        "g4_two_step" -> script(TeachingVisual.GROUPS,
            "Önce Gruplar, Sonra Eklenenler","Üç kutuda dörder kalem var. Beş kalem daha geldi.",
            "3 × 4 + 5 ifadesinde önce eş gruplar, sonra eklenenler sayılır.",
            listOf(4,4,4,5),"3 × 4 + 5 = 17",
            listOf("Üç kutunun her birinde dört kalem var.",
                "Gruplar toplam 3 × 4 = 12 kalem.",
                "Beş kalem ekle: 12 + 5 = 17."),
            "İki adımlı problemde önce neyi bulmalıyız?",
            "Kutulardaki kalemlerin toplamını.",
            "Sadece sonradan gelen beş kalemi.",
            "Bütün kutulardaki kalemler bulunmadan toplam bilinemez.")

        "g5_parentheses" -> script(TeachingVisual.GROUPS,
            "Parantezin Önceliği","Üç ile dördü topla; sonra ikiyle çarp.",
            "2 × (3+4) = 14, çünkü parantez önce hesaplanır.",
            listOf(3,4,3,4),"2 × (3 + 4) = 14",
            listOf("İlk önce parantezi oku: 3+4.","Parantezin sonucu yedi.",
                "İki tane yediyi topla: on dört."),
            "Parantez varsa hangi işlem önce yapılır?",
            "Parantezin içindeki işlem.","Parantezin dışındaki işlem.",
            "Parantez önceliklidir; sonra dışarıdaki çarpma yapılır.")

        "g5_tenths_add" -> script(TeachingVisual.DECIMAL,
            "Onda Birleri Topla","Bir tam ve iki onda bire üç onda bir ekle.",
            "1,2 + 0,3 = 1,5; onda birler kendi arasında toplanır.",
            listOf(1,2),"1,2 + 0,3 = 1,5",
            listOf("Bir bütün ve iki onda bir var.",
                "Üç onda bir daha ekle.",
                "Beş onda bir ve bir bütün oluşur: 1,5."),
            "Onda birleri toplarken neyi birleştiririz?",
            "Aynı basamak değerindeki parçaları.",
            "Ondalık virgüllerini.",
            "Onda birler, onda birlerle toplanır.")

        "g6_absolute_value" -> script(TeachingVisual.NUMBER_STEPS,
            "Sıfıra Uzaklığı Bul","Eksi sekiz, sıfırdan sekiz birim uzakta.",
            "Mutlak değer bir sayının sıfırdan uzaklığını verir.",
            listOf(-8,-4,0,4,8),"|−8| = 8",
            listOf("Sayı doğrusunda sıfırı bul.","Eksi sekiz sıfırın solundadır.",
                "Sıfıra uzaklığı sekiz birimdir."),
            "Negatif bir sayının mutlak değeri neden pozitif olur?",
            "Uzaklık negatif olamaz.","Negatif işaret hep eksi kalır.",
            "Mutlak değer yönü değil uzaklığın büyüklüğünü gösterir.")

        "g6_pattern_rule" -> script(TeachingVisual.NUMBER_STEPS,
            "Ardışık Terimlerin Kuralı","İki, beş, sekiz, on bir: her seferinde üç eklenir.",
            "Aritmetik örüntüde ortak fark üçse sıradaki terim on dörttür.",
            listOf(2,5,8,11,14),"2, 5, 8, 11, 14",
            listOf("İlk iki sayının farkı üç.",
                "Diğer komşu sayılarda da fark üç.",
                "On birin üzerine üç ekle, on dört olur."),
            "Sabit artışlı örüntüde sonraki terim nasıl bulunur?",
            "Bir önceki terime ortak fark eklenir.",
            "Her zaman ikiyle çarpılır.",
            "Fark aynı kaldığında aynı sayıyı ekleriz.")

        "g7_fraction_sum" -> script(TeachingVisual.FRACTION_BAR,
            "Payları Birleştir, Paydayı Koru","Sekiz eş parçanın ikisiyle üçü birleşir.",
            "2/8 + 3/8 = 5/8. Parça büyüklüğü değişmez.",
            listOf(5,8),"2/8 + 3/8 = 5/8",
            listOf("İlk kesirde sekiz eş parçanın ikisi var.",
                "Üç aynı büyüklükte parça daha ekle.",
                "Toplam beş parça: 5/8."),
            "Paydaları eşit kesirleri toplarken ne sabit kalır?",
            "Payda, yani eş parça büyüklüğü.",
            "Pay ve paydayı birlikte toplarız.",
            "Bütün hâlâ sekiz eş parçaya bölündüğü için payda sekizdir.")

        "g7_percent_quarter" -> script(TeachingVisual.FRACTION_BAR,
            "Yüzde 25 = Dörtte Bir","Dört eş parçadan birini boya.",
            "%25 = 25/100 = 1/4, yani bütünün çeyreği.",
            listOf(1,4),"%25 = 1/4",
            listOf("Bütün yüzde yüzdür.","Yüzde yirmi beş, yüz eş parçanın yirmi beşidir.",
                "Dörde bölünmüş bütünde bir parçadır."),
            "Yüzde yirmi beş neye eşittir?",
            "Bir bölü dört.","Bir bölü iki.",
            "25/100 kesri 1/4 olarak sadeleşir.")

        "g8_lcm" -> script(TeachingVisual.MULTIPLES,
            "Katları Kesiştir","On ikinin ve on sekizin katlarını sırayla yaz.",
            "EKOK(12,18) = 36; iki listenin ilk ortak pozitif katıdır.",
            listOf(12,18),"EKOK(12,18) = 36",
            listOf("12'nin katları 12, 24, 36, 48 diye devam eder.",
                "18'in katları 18, 36, 54, 72 diye devam eder.",
                "İlk ortak pozitif kat 36'dır."),
            "EKOK hesaplanırken hangi ortak kat seçilir?",
            "Sıfırdan büyük en küçük ortak kat.",
            "Sayıların en büyük ortak böleni.",
            "Ortak katlar iki sayının da katıdır; en küçüğü EKOK'tur.")

        "g8_square_root" -> script(TeachingVisual.AREA_GRID,
            "Karesinden Kareköküne","Yedi satır, yedi sütun birim kare düşün.",
            "7 × 7 = 49 olduğundan √49 = 7.",
            listOf(7,7),"√49 = 7",
            listOf("Bir karede yedi sütun var.","Yedi satırda toplam kırk dokuz kare oluşur.",
                "Karenin bir kenarı yedi birimdir: √49 = 7."),
            "Karekök 49 neden 7'dir?",
            "7 × 7 = 49 olduğu için.",
            "49'u yediye eklediğimiz için.",
            "Pozitif karekök, karesi verilen sayıya eşit olan negatif olmayan sayıdır.")

        "g8_linear_value" -> script(TeachingVisual.EQUATION,
            "x'in Değerini Yerine Koy","x dört, katsayı üç ve sabit iki.",
            "y = 3x + 2. x = 4 yerine yazılır: y = 14.",
            listOf(3,2,14),"y = 3x + 2 → x = 4, y = 14",
            listOf("Önce x yerine dört yaz.","Üç ile dördü çarp: on iki.",
                "İki daha ekle: y = 14."),
            "Doğrusal ifadede x yerine 4 yazmak ne demektir?",
            "3x ifadesini 3 × 4 olarak hesaplamak.",
            "3x ifadesinde 3'ü silmek.",
            "3x, üç ile x'in çarpımıdır.")

        "g8_coordinate_mirror" -> script(TeachingVisual.SYMMETRY,
            "Koordinat Düzleminde Yansıma","Nokta sağdaysa yansıması soldadır.",
            "Y eksenine göre (3,2) noktasının görüntüsü (−3,2)'dir.",
            listOf(3,3),"(3, 2) → (−3, 2)",
            listOf("A noktasının x koordinatı artı üç.",
                "Y ekseninin solunda üç birim uzaklıkta eşini bul.",
                "y koordinatı aynı kalır: (−3, 2)."),
            "Y eksenine göre yansımada ne değişir?",
            "x koordinatının işareti.","y koordinatının işareti.",
            "Dikey y eksenine yansımada yatay uzaklık ters yöne taşınır.")
        else -> error("Missing authored Ö6 script for " + lesson.id)
    }
}
