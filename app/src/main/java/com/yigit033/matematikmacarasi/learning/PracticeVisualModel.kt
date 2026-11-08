package com.yigit033.matematikmacarasi.learning

/**
 * A question's diagram must be generated from THAT question's parameters,
 * never from an unrelated fixed worked example.
 * Parsing is strict. Unknown formats show text only rather than wrong artwork.
 */
enum class PracticeVisualKind {
    ADD_GROUPS, TAKE_AWAY, COMPARE_GROUPS, THREE_BARS,
    TENS_AND_ONES, TWO_EQUAL_PARTS, THREE_EQUAL_PARTS,
    FRACTION_PARTS, CLOCK_START, TRIANGLE, SQUARE, PERIMETER,
    AREA_GRID, DATA_BARS, CHANCE_CHIPS, SIGNED_WALK, ALGEBRA_BALANCE,
    POWER_FACTORS, RIGHT_TRIANGLE, VOLUME_LAYERS, MIRROR_POINTS, FACTOR_LISTS,
    RATIO_GROUPS, MULTIPLES_LISTS, LINEAR_INPUT,
    TEN_FRAME, HUNDREDS, EQUAL_SHARE, FRACTION_COMPARE, TWO_STEP
}

data class PracticeVisualModel(
    val kind: PracticeVisualKind,
    val values: List<Int>,
    val description: String
) {
    init { require(values.isNotEmpty() && values.all { it >= 0 }) }

    /** Equivalent information for learners using TalkBack.
     * The narration is calculated from the current problem, never a stock caption.
     */
    fun accessibilityText(): String = when (kind) {
        PracticeVisualKind.ADD_GROUPS ->
            "${values[0]} nesnelik bir grup ve ${values[1]} nesnelik ikinci grup."
        PracticeVisualKind.TAKE_AWAY ->
            "${values[0]} nesneden ${values[1]} tanesinin üzeri çizilmiş."
        PracticeVisualKind.COMPARE_GROUPS ->
            "A grubu ${values[0]} nesne; B grubu ${values[1]} nesne."
        PracticeVisualKind.THREE_BARS ->
            values.mapIndexed { index, n -> "${'A' + index} grubu: $n" }.joinToString(". ")
        PracticeVisualKind.TENS_AND_ONES ->
            "${values[0]} onluk, ${values[1]} birlik."
        PracticeVisualKind.TWO_EQUAL_PARTS ->
            "${values[0]} nesne iki eş gruba ayrılmış."
        PracticeVisualKind.THREE_EQUAL_PARTS ->
            "${values[0]} nesne üç eş gruba ayrılmış."
        PracticeVisualKind.FRACTION_PARTS ->
            "${values[0]} nesnenin ${values[1]} bölü ${values[2]} kesri."
        PracticeVisualKind.CLOCK_START ->
            "Saat ${values[0]} sıfır sıfırı gösteren saat kadranı."
        PracticeVisualKind.TRIANGLE -> "Üç kenarlı ve üç köşeli üçgen."
        PracticeVisualKind.SQUARE -> "Dört kenarlı ve dört köşeli kare."
        PracticeVisualKind.PERIMETER ->
            "Kenarları ${values[0]} ve ${values[1]} birim olan dikdörtgen."
        PracticeVisualKind.TEN_FRAME ->
            "Onluk tabloda ${values[0]} dolu, ${10-values[0]} boş yer var."
        PracticeVisualKind.HUNDREDS ->
            "${values[0]} yüzlük, ${values[1]} onluk ve ${values[2]} birlik."
        PracticeVisualKind.EQUAL_SHARE ->
            "${values[0]} nesne ${values[1]} eş gruba dağıtılıyor."
        PracticeVisualKind.FRACTION_COMPARE ->
            "Paydası ${values[2]} olan iki kesrin payı ${values[0]} ve ${values[1]}."
        PracticeVisualKind.TWO_STEP ->
            "${values[0]} kutu, her kutuda ${values[1]} nesne ve sonradan ${values[2]} nesne ekleniyor."
        else -> description // Advanced models supply exact operands with their own caption.
    }
}

object PracticeVisualFactory {
    private fun groups(text: String, regex: String): List<Int>? =
        Regex(regex).find(text)?.groupValues?.drop(1)?.mapNotNull { it.toIntOrNull() }

    fun forQuestion(lessonId: String, text: String): PracticeVisualModel? {
        fun build(kind: PracticeVisualKind, numbers: List<Int>?, desc: String):
            PracticeVisualModel? = numbers?.takeIf { it.all { n -> n in 0..100 } }
                ?.let { PracticeVisualModel(kind, it, desc) }
        return when (lessonId) {
            "add_10", "add_20" ->
                build(PracticeVisualKind.ADD_GROUPS,
                    groups(text, """(\d+) \+ (\d+) = \?"""),
                    "Toplamada birleştirilen iki nesne grubu")
            "g1_count20" ->
                build(PracticeVisualKind.TENS_AND_ONES,
                    groups(text, """10 nesneye (\d+) nesne""")?.let { listOf(1,it[0]) },
                    "Bir onluk ve üzerine eklenen birlikler")
            "g1_takeaway" ->
                build(PracticeVisualKind.TAKE_AWAY,
                    groups(text, """(\d+) elmadan (\d+) elma"""),
                    "Eksiltilen elmaların üzeri çizilidir")
            "g1_compare" ->
                build(PracticeVisualKind.COMPARE_GROUPS,
                    groups(text, """A grubunda (\d+), B grubunda (\d+)"""),
                    "A ve B gruplarının büyüklüklerini karşılaştır")
            "g1_sort_data", "g2_simple_table", "g4_chart" ->
                build(PracticeVisualKind.THREE_BARS,
                    groups(text, """A=(\d+), B=(\d+), C=(\d+)"""),
                    "Üç gruba ait sütun değerleri")
            "g3_chart" ->
                build(PracticeVisualKind.COMPARE_GROUPS,
                    groups(text, """kitap (\d+), defter (\d+)"""),
                    "Kitap ve defter sayılarının görsel karşılaştırması")
            "g2_place" ->
                build(PracticeVisualKind.TENS_AND_ONES,
                    groups(text, """(\d+) onluk ve (\d+) birlik"""),
                    "Onluk çubukları ve birlik nesneleri")
            "g2_half" ->
                build(PracticeVisualKind.TWO_EQUAL_PARTS,
                    groups(text, """(\d+) nesnenin yarısı"""),
                    "İki eş parça")
            "g3_third" ->
                build(PracticeVisualKind.THREE_EQUAL_PARTS,
                    groups(text, """(\d+) nesnenin üçte biri"""),
                    "Üç eş parça")
            "g4_fraction" ->
                build(PracticeVisualKind.FRACTION_PARTS,
                    groups(text, """(\d+) sayısının (\d+)/(\d+) kesri"""),
                    "Bütünün eş parçaları ve seçilen parçalar")
            "g2_clock" ->
                build(PracticeVisualKind.CLOCK_START,
                    groups(text, """Saat (\d+) iken (\d+) saat sonra"""),
                    "Soruda verilen başlangıç saati")
            "g2_corners" ->
                if (text.startsWith("Bir üçgen")) PracticeVisualModel(
                    PracticeVisualKind.TRIANGLE, listOf(3), "Üçgenin kenar ve köşeleri")
                else if (text.startsWith("Bir kare")) PracticeVisualModel(
                    PracticeVisualKind.SQUARE, listOf(4), "Karenin kenar ve köşeleri")
                else null
            "g1_make_ten" ->
                build(PracticeVisualKind.TEN_FRAME,
                    groups(text, """(\d+) nesnenin 10 olması için"""),
                    "Onluk tablo: dolu ve boş yerler")
            "g1_compare_data" ->
                build(PracticeVisualKind.COMPARE_GROUPS,
                    groups(text, """Grafikte A=(\d+), B=(\d+) nesne"""),
                    "İki gruptaki nesnelerin farkı")
            "g2_quarter", "g3_quarter_of_set" ->
                build(PracticeVisualKind.FRACTION_PARTS,
                    groups(text, """(\d+) nesnenin (?:çeyreği|dörtte biri)""")
                        ?.let { listOf(it[0],1,4) },
                    "Dört eş parçaya bölünmüş bütün")
            "g2_hundred_place" ->
                build(PracticeVisualKind.HUNDREDS,
                    groups(text, """(\d+) yüzlük, (\d+) onluk ve (\d+) birlik"""),
                    "Yüzlük, onluk ve birlik blokları")
            "g3_share_equal" ->
                build(PracticeVisualKind.EQUAL_SHARE,
                    groups(text, """(\d+) nesne (\d+) çocuğa eşit"""),
                    "Nesnelerin eş gruplara paylaştırılması")
            "g4_same_denominator" ->
                build(PracticeVisualKind.FRACTION_COMPARE,
                    groups(text, """(\d+)/(\d+) ve (\d+)/(\d+) kesirlerinden""")
                        ?.let { listOf(it[0],it[2],it[1]) },
                    "Paydaları eşit iki kesrin parça karşılaştırması")
            "g4_two_step" ->
                build(PracticeVisualKind.TWO_STEP,
                    groups(text, """(\d+) kutunun her birinde (\d+) kalem var. (\d+) kalem daha"""),
                    "Eş kalem kutularına sonradan eklenen kalemler")
            "g3_rectangle", "g4_perimeter" ->
                build(PracticeVisualKind.PERIMETER,
                    groups(text, """(?:Uzun kenarı|Kenarları) (\d+)(?:, kısa kenarı| ve) (\d+)"""),
                    "Dikdörtgenin karşılıklı eş kenarları")
            else -> UpperGradePracticeVisualFactory.forQuestion(lessonId, text)
        }
    }
}
