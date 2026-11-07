package com.yigit033.matematikmacarasi.learning

/**
 * Subject-specific native diagram data for grades 5–8.
 * Only illustrations whose inputs can be recovered unambiguously
 * from the current question are displayed; never guess missing values.
 */
object UpperGradePracticeVisualFactory {
    private fun read(text: String, pattern: String): List<Int>? =
        Regex(pattern).find(text)?.groupValues?.drop(1)?.mapNotNull { it.toIntOrNull() }

    fun forQuestion(lessonId: String, text: String): PracticeVisualModel? {
        val kind: PracticeVisualKind
        val values: List<Int>
        val caption: String
        when (lessonId) {
            "g5_area", "g6_area_units", "g7_area_compare" -> {
                kind = PracticeVisualKind.AREA_GRID
                values = when (lessonId) {
                    "g5_area" -> read(text,
                        """(\d+) birim uzunluk ve (\d+) birim genişlikteki alan""")
                    "g6_area_units" -> read(text,
                        """(\d+) birim ve (\d+) birim kenarlı""")
                    else -> read(text, """Kenarları (\d+) ve (\d+) birim""")
                } ?: return null
                caption = "${values[0]} sütun, ${values[1]} satır birim kare."
            }
            "g5_mean", "g7_median" -> {
                kind = PracticeVisualKind.DATA_BARS
                values = read(text, """(\d+), (\d+), (\d+) sayılarının""") ?: return null
                caption = "Sırasıyla ${values[0]}, ${values[1]} ve ${values[2]} veri sütunu."
            }
            "g6_data_range" -> {
                kind = PracticeVisualKind.DATA_BARS
                values = read(text,
                    """en küçüğü (\d+), en büyüğü (\d+)""") ?: return null
                caption = "En küçük veri ${values[0]}, en büyük veri ${values[1]}."
            }
            "g5_simple_chance" -> {
                kind = PracticeVisualKind.CHANCE_CHIPS
                values = read(text,
                    """Kutuda (\d+) kırmızı ve (\d+) mavi bilye""") ?: return null
                caption = "${values[0]} kırmızı ve ${values[1]} mavi bilye, hepsi eş olasılıklı."
            }
            "g7_probability" -> {
                kind = PracticeVisualKind.CHANCE_CHIPS
                val n = read(text,
                    """(\d+) eş olasılıklı durumun (\d+) tanesi""") ?: return null
                values = listOf(n[1], n[0] - n[1])
                caption = "${n[0]} eş olasılıklı durumun ${n[1]} tanesi isteniyor."
            }
            "g8_data_probability" -> {
                kind = PracticeVisualKind.CHANCE_CHIPS
                val n = read(text,
                    """10 eş olasılıklı durumdan (\d+) tanesi""") ?: return null
                values = listOf(n[0], 10 - n[0])
                caption = "10 eş olasılıklı durumun ${n[0]} tanesi isteniyor."
            }
            "g6_signed" -> {
                kind = PracticeVisualKind.SIGNED_WALK
                values = read(text,
                    """Sıcaklık −(\d+) °C iken (\d+) °C artarsa""") ?: return null
                caption = "Başlangıç eksi ${values[0]}, ardından ${values[1]} birim artış."
            }
            "g6_equation", "g7_equation", "g8_equation" -> {
                kind = PracticeVisualKind.ALGEBRA_BALANCE
                values = when (lessonId) {
                    "g6_equation" -> read(text, """x \+ (\d+) = (\d+)""")
                    "g7_equation" -> read(text, """(\d+)x \+ (\d+) = (\d+)""")
                    else -> read(text, """(\d+)\(x \+ (\d+)\) = (\d+)""")
                } ?: return null
                caption = "Denklemin iki tarafı eşit. ${text.substringBefore("denkleminde").trim()}"
            }
            "g8_power" -> {
                kind = PracticeVisualKind.POWER_FACTORS
                values = read(text, """(\d+) üssü (\d+) ifadesinin""") ?: return null
                caption = "${values[0]} sayısı ${values[1]} kez çarpan olarak kullanılıyor."
            }
            "g8_right_triangle" -> {
                kind = PracticeVisualKind.RIGHT_TRIANGLE
                values = read(text, """Dik kenarları (\d+) ve (\d+) birim""") ?: return null
                caption = "Dik açının iki kenarı ${values[0]} ve ${values[1]} birim."
            }
            "g8_prism_volume" -> {
                kind = PracticeVisualKind.VOLUME_LAYERS
                values = read(text,
                    """Kenar uzunlukları (\d+), (\d+) ve (\d+) birim""") ?: return null
                caption = "${values[0]} sütun, ${values[1]} satır, ${values[2]} birim-küp katı."
            }
            "g7_mirror" -> {
                kind = PracticeVisualKind.MIRROR_POINTS
                values = read(text, """simetri eksenine (\d+) birim uzakta""") ?: return null
                caption = "Bir nokta simetri eksenine ${values[0]} birim uzaklıkta."
            }
            "g8_gcd" -> {
                kind = PracticeVisualKind.FACTOR_LISTS
                values = read(text, """(\d+) ve (\d+) sayılarının EBOB""") ?: return null
                caption = "${values[0]} ve ${values[1]} sayılarının bölenlerini karşılaştır."
            }
            "g6_ratio" -> {
                kind = PracticeVisualKind.RATIO_GROUPS
                val n = read(text,
                    """2 ölçek meyveye 3 ölçek su gerekir. (\d+) ölçek meyve""") ?: return null
                values = listOf(2, 3, n[0])
                caption = "Başlangıç tarifi 2 ölçek meyveye 3 ölçek su; şimdi ${n[0]} ölçek meyve var."
            }
            "g7_proportion" -> {
                kind = PracticeVisualKind.RATIO_GROUPS
                values = read(text,
                    """(\d+) bilet (\d+) TL ise (\d+) bilet""") ?: return null
                caption = "${values[0]} bilet ${values[1]} lira; ${values[2]} biletin toplam fiyatı soruluyor."
            }
            else -> return null
        }
        if (values.isEmpty() || values.any { it < 0 || it > 150 }) return null
        if (kind == PracticeVisualKind.CHANCE_CHIPS && values.sum() !in 2..30) return null
        if (kind == PracticeVisualKind.VOLUME_LAYERS && values.any { it < 1 || it > 8 }) return null
        if (kind == PracticeVisualKind.AREA_GRID && values.any { it !in 1..16 }) return null
        if (kind == PracticeVisualKind.POWER_FACTORS &&
            (values[0] !in 1..10 || values[1] !in 1..7)) return null
        return PracticeVisualModel(kind, values, caption)
    }
}
