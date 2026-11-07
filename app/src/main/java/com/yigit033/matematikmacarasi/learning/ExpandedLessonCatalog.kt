package com.yigit033.matematikmacarasi.learning

/**
 * Production-intent introductory units for real grade concepts. They are short
 * units, not a complete MEB programme. IDs are grade-scoped and immutable.
 * Each has a distinct authored explanation, model and problem generator.
 */
internal data class ExpandedUnit(
    val grade: Int, val topicKey: String, val lesson: Lesson
)

object ExpandedLessonCatalog {
    private fun u(
        grade: Int, topic: String, id: String, name: String,
        teaching: String, example: String, hint: String
    ) = ExpandedUnit(grade, topic, Lesson(id, name, teaching, example, hint, Skill.EXTENDED))

    private val units: List<ExpandedUnit> = listOf(
        u(1,"count","g1_count20","10'dan 20'ye Sayılar",
            "Onluk bir grup ve üzerine gelen birlikleri say.",
            "10 + 4 = 14","Önce onluğu say, sonra birlikleri ekle."),
        u(1,"subtraction","g1_takeaway","10 İçinde Çıkarma",
            "Nesnelerin bir kısmını ayırdığımızda kalanı buluruz.",
            "7 − 3 = 4","Yedi nesneden üçünü kaldır; kalanı say."),
        u(1,"shapes","g1_shapes","Üçgen ve Dörtgenler",
            "Şeklin çevresinde kaç düz kenar olduğunu incele.",
            "Üçgen: 3 kenar","Kenarları parmağınla tek tek takip et."),

        u(2,"place","g2_place","Onluk ve Birlik",
            "Bir onluk 10 birliktir. Sayıyı iki parçaya ayır.",
            "3 onluk 4 birlik = 34","Onlukları onar, birlikleri birer say."),
        u(2,"fractions","g2_half","Yarımı Görerek Bul",
            "Bütünü iki eş parçaya ayırınca her parça yarımdır.",
            "12'nin yarısı 6'dır","İki grubun eşit olduğundan emin ol."),
        u(2,"time","g2_clock","Saatte Tam Saatler",
            "Akrep saatleri, yelkovan dakikayı gösterir.",
            "3'ten 2 saat sonra 5 olur.","Saatleri birer birer ilerlet."),

        u(3,"fractions","g3_third","Üçte Bir Kavramı",
            "Bir bütünü üç eş parçaya ayır, birini incele.",
            "12'nin üçte biri 4'tür.","Üç eş gruba paylaş."),
        u(3,"time","g3_money","Alışverişte Para Üstü",
            "Ödediğin para ile ürün fiyatının farkını bul.",
            "50 TL − 35 TL = 15 TL","Ödediğin paradan fiyatı çıkar."),
        u(3,"data","g3_chart","Grafikten Fark Bul",
            "Grafikte iki sütunun yüksekliğini karşılaştır.",
            "8 kitap − 5 defter = 3 fazla","Büyük sütundan küçüğü çıkar."),

        u(4,"fractions","g4_fraction","Bir Çokluğun Kesri",
            "Bütünü eş parçalara bölüp istediğimiz parçaları seçeriz.",
            "12'nin 3/4'ü 9'dur.","Önce dörde böl, sonra üçünü al."),
        u(4,"geometry","g4_perimeter","Dikdörtgende Çevre",
            "Çevre dört kenarın uzunlukları toplamıdır.",
            "5 + 3 + 5 + 3 = 16","İki uzun, iki kısa kenarı ekle."),
        u(4,"data","g4_chart","Sütun Grafiğinden Toplam",
            "Sütunların gösterdiği değerleri birlikte topla.",
            "4 + 6 + 3 = 13","Her sütunun değerini sırayla topla."),

        u(5,"fractions","g5_decimal","Ondalık Sayıyı Çözümle",
            "Bir tam 10 onda birden oluşur.",
            "3 tam 7 onda bir = 37 onda bir","Tam kısmı 10 ile çarp, onda birleri ekle."),
        u(5,"measure","g5_area","Dikdörtgenin Alanı",
            "Alanı birim karelerle ölçeriz: satır × sütun.",
            "5 × 3 = 15 birim kare","Bir satırdaki kare sayısını satır sayısıyla çarp."),
        u(5,"data","g5_mean","Aritmetik Ortalama",
            "Verileri topla, veri sayısına böl.",
            "(4 + 6 + 8) / 3 = 6","Toplamı kaç veri olduğuna böl."),

        u(6,"numbers","g6_signed","Tam Sayı Doğrusu",
            "Negatiften sağa ilerlemek sayıyı artırır.",
            "−6 + 9 = 3","Sıfıra kaç adım kaldığını say."),
        u(6,"fractions","g6_ratio","Oranla Eş Ölçek",
            "İki miktar aynı katla büyürse oran değişmez.",
            "2 meyve + 3 su → 4 meyve + 6 su","İki miktarı da aynı sayı ile çarp."),
        u(6,"algebra","g6_equation","Bir Bilinmeyenli Eşitlik",
            "Eşitliği korumak için iki taraftan aynı sayıyı çıkar.",
            "x + 7 = 19 → x = 12","Bilinmeyeni yalnız bırak."),

        u(7,"numbers","g7_rational","Denk Kesirlerle İlişkiler",
            "Payı ve paydayı aynı sayıyla çarpmak değeri değiştirmez.",
            "2/3 = 6/9","Payı ve paydayı aynı katla genişlet."),
        u(7,"ratio","g7_proportion","Doğru Orantı",
            "Birim miktar sabitse iki nicelik birlikte büyür.",
            "4 bilet 12 TL → 7 bilet 21 TL","Önce bir tanenin fiyatını bul."),
        u(7,"algebra","g7_equation","Cebirsel Denklem Çözme",
            "Önce sabit terimi, sonra katsayıyı ayır.",
            "3x + 5 = 20 → x = 5","Önce 5 çıkar, sonra 3'e böl."),

        u(8,"factors","g8_gcd","Ortak Bölenleri Bul",
            "EBOB her iki sayıyı da bölen en büyük sayıdır.",
            "EBOB(12, 18) = 6","Her ikisinin bölenlerini karşılaştır."),
        u(8,"powers","g8_power","Üslü İfadeyi Anla",
            "Üs, bir sayının kaç kez çarpıldığını gösterir.",
            "2³ = 2 × 2 × 2 = 8","Üs kadar çarpma yap."),
        u(8,"algebra","g8_equation","Parantezli Denklem",
            "Parantezi aç, sonra bilinmeyeni bul.",
            "2(x + 3) = 18 → x = 6","Önce ikiye böl, ardından üç çıkar.")
    )

    fun forGrade(grade: Int): List<Lesson> =
        units.filter { it.grade == grade }.map { it.lesson }

    fun lessonIdsForTopic(grade: Int, topicKey: String): List<String> =
        units.filter { it.grade == grade && it.topicKey == topicKey }
            .map { it.lesson.id }

    fun topicKeyForLesson(grade: Int, id: String): String? =
        units.firstOrNull { it.grade == grade && it.lesson.id == id }?.topicKey

    fun byId(id: String): Lesson? = units.firstOrNull { it.lesson.id == id }?.lesson
    fun all(): List<Lesson> = units.map { it.lesson }
}
