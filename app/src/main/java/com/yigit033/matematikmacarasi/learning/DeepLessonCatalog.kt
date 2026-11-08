package com.yigit033.matematikmacarasi.learning

/**
 * Ö6: second concept in selected subject families.
 * This is intentionally a verified, teachable set of subskills, not a claim
 * that a chapter heading or an entire MEB learning outcome has been completed.
 */
object DeepLessonCatalog {
    data class Unit(val grade: Int, val topicKey: String, val lesson: Lesson)
    private fun u(
        grade: Int, topic: String, key: String, title: String,
        teaching: String, example: String, hint: String
    ) = Unit(grade, topic, Lesson("g" + grade + "_" + key,
        title, teaching, example, hint, Skill.DEEP_DIVE))

    private val units: List<Unit> = listOf(
        u(1,"addition","make_ten","10'a Tamamlayarak Topla",
            "Bir sayıya ekleyeceklerini önce 10 olacak şekilde ayır.",
            "7 + 3 = 10","On olmak için kaç nesne eksik?"),
        u(1,"data","compare_data","Grafikte Kaç Fazla?",
            "İki grubun sayısını karşılaştır, fazla olan nesneleri bul.",
            "6 − 4 = 2 fazla","İki grubun eşit kısımlarını eşleştir."),
        u(2,"fractions","quarter","Çeyrek: Dörtte Bir",
            "Bir bütün dört eş parçaya ayrıldığında bir parça çeyrektir.",
            "16'nın çeyreği 4","Dört eş gruba ayır."),
        u(2,"place","hundred_place","Yüzlüğü Keşfet",
            "On onluk bir yüzlük eder; yüzlük, onluk ve birlikleri birleştir.",
            "100 + 20 + 3 = 123","Yüzlüğü, onlukları ve birlikleri ayrı say."),
        u(3,"operations","share_equal","Eşit Paylaşarak Böl",
            "Bir miktarı eşit gruplara dağıtırken bölme yaparız.",
            "18 ÷ 3 = 6","Nesneleri gruplara birer birer paylaştır."),
        u(3,"fractions","quarter_of_set","Bir Çokluğun Çeyreği",
            "Nesneleri dört eş gruba ayırınca her grubun büyüklüğü çeyrektir.",
            "20'nin çeyreği 5","Dört eş gruptan birini say."),
        u(4,"fractions","same_denominator","Paydaları Eşit Kesirleri Karşılaştır",
            "Paydalar eşitse payı büyük olan kesir daha büyüktür.",
            "3/8 > 2/8","Eşit parçalardan kaç tanesi seçilmiş?"),
        u(4,"operations","two_step","İki Adımlı İşlem",
            "Önce grupların sayısını hesapla, sonra eklenenleri bul.",
            "3 × 4 + 5 = 17","Önce çarpmayı, sonra toplamayı yap."),
        u(5,"operations","parentheses","Parantezin İçini Önce Çöz",
            "İşlem sırası, parantez içindeki hesapla başlar.",
            "2 × (3 + 4) = 14","Parantezin içini hesapladıktan sonra çarp."),
        u(5,"fractions","tenths_add","Onda Birleri Topla",
            "Ondalık sayılarda aynı basamaktaki değerler toplanır.",
            "1,2 + 0,3 = 1,5","Ondalıkları onda bir biriminde düşün."),
        u(6,"numbers","absolute_value","Mutlak Değer",
            "Mutlak değer sayının sıfıra uzaklığıdır, negatif olmaz.",
            "|−7| = 7","Sayı doğrusunda sıfıra kaç birim uzak?"),
        u(6,"algebra","pattern_rule","Örüntünün Kuralını Bul",
            "Sabit artışlı örüntüde her sırada aynı miktar eklenir.",
            "2, 5, 8, 11, 14","Komşu terimler arasındaki farkı bul."),
        u(7,"numbers","fraction_sum","Paydaları Eşit Kesirlerin Toplamı",
            "Paydaları eşit kesirlerde paylar toplanır, payda korunur.",
            "2/8 + 3/8 = 5/8","Eş parça sayısı değişmez."),
        u(7,"ratio","percent_quarter","Yüzde Yirmi Beşi Bul",
            "Yüzde 25, bir bütünün dörtte biridir.",
            "40'ın %25'i 10","Sayıyı dörde böl."),
        u(8,"factors","lcm","EKOK: İlk Ortak Kat",
            "İki sayının ortak katlarının en küçüğüne EKOK denir.",
            "EKOK(12, 18) = 36","İki sayının katlarını sırayla yaz."),
        u(8,"powers","square_root","Tam Karelerin Karekökü",
            "Karekök, karesi verilen pozitif sayıya götürür.",
            "√49 = 7, çünkü 7 × 7 = 49","Hangi sayı kendisiyle çarpılınca bu sonuç çıkar?"),
        u(8,"algebra","linear_value","Doğrusal İlişkide Değer Bul",
            "y = mx + b ilişkisinde x yerine verilen sayıyı koy.",
            "y = 3x + 2; x = 4 ise y = 14","Önce x'i yerine yaz, sonra çarp ve ekle."),
        u(8,"geometry","coordinate_mirror","Koordinatta Yansıma",
            "Y eksenine göre yansımada x'in işareti değişir; y aynı kalır.",
            "(3, 2) → (−3, 2)","Y ekseninin diğer tarafına eş uzaklıkta git.")
    )

    fun forGrade(grade: Int): List<Lesson> =
        units.filter { it.grade == grade }.map { it.lesson }
    fun lessonIdsForTopic(grade: Int, topicKey: String): List<String> =
        units.filter { it.grade == grade && it.topicKey == topicKey }.map { it.lesson.id }
    fun all(): List<Unit> = units
}
