package com.yigit033.matematikmacarasi.learning

/**
 * Pure, deterministic Turkish math-to-speech normalizer.
 * Runs entirely on-device, before either TTS or a pre-recorded voice asset.
 *
 * Do not replace operators in arbitrary words or read a fraction as an
 * unrelated division. This is a concise spoken rendering, not an SSML engine.
 */
object TurkishMathSpeech {
    private val unit = arrayOf(
        "sıfır", "bir", "iki", "üç", "dört", "beş", "altı", "yedi", "sekiz", "dokuz"
    )
    private val ten = arrayOf(
        "", "on", "yirmi", "otuz", "kırk", "elli", "altmış",
        "yetmiş", "seksen", "doksan"
    )
    private val numeric = Regex("""(?<![\p{L}\p{N}])\d{1,7}(?![\p{L}\p{N}])""")
    private val fraction = Regex("""(?<![\p{L}\p{N}])(\d{1,6})\s*/\s*(\d{1,6})(?![\p{L}\p{N}])""")
    private val decimal = Regex("""(?<![\p{L}\p{N}])(\d{1,6}),(\d{1,5})(?![\p{L}\p{N}])""")
    private val percent = Regex("""%\s*(\d{1,6})""")
    private val stepSuffix = Regex("""(?<![\p{L}\p{N}])(2|3|4|5|6|7|8|9|10)['’]?(?:er|şer|ar)(?![\p{L}])""")

    fun number(n: Int): String {
        if (n < 0) return "eksi " + number(-n)
        if (n < 10) return unit[n]
        if (n < 100) return listOf(ten[n / 10], if (n % 10 == 0) "" else unit[n % 10])
            .filter { it.isNotEmpty() }.joinToString(" ")
        if (n < 1000) {
            val h = n / 100
            return listOf(if (h == 1) "yüz" else unit[h] + " yüz",
                if (n % 100 == 0) "" else number(n % 100))
                .filter { it.isNotEmpty() }.joinToString(" ")
        }
        if (n < 1_000_000) {
            val k = n / 1000
            return listOf(if (k == 1) "bin" else number(k) + " bin",
                if (n % 1000 == 0) "" else number(n % 1000))
                .filter { it.isNotEmpty() }.joinToString(" ")
        }
        return n.toString()
    }

    private fun fractionalLocative(denominator: Int): String {
        val stem = number(denominator)
        val last = stem.lastOrNull() ?: return stem
        val vowel = stem.lastOrNull { it in "aeıioöuü" } ?: 'e'
        val front = vowel in "eiöü"
        val hard = last in "pçtkfhsş"
        val suffix = when {
            hard && front -> "te"
            hard -> "ta"
            front -> "de"
            else -> "da"
        }
        return stem + suffix
    }

    fun normalize(source: String): String {
        if (source.isBlank()) return ""
        var text = source.trim()
        text = stepSuffix.replace(text) {
            when (it.groupValues[1]) {
                "2" -> "ikişer"
                "3" -> "üçer"
                "4" -> "dörder"
                "5" -> "beşer"
                "6" -> "altışar"
                "7" -> "yedişer"
                "8" -> "sekizer"
                "9" -> "dokuzar"
                else -> "onar"
            }
        }
        text = fraction.replace(text) {
            val p = it.groupValues[1].toIntOrNull() ?: return@replace it.value
            val d = it.groupValues[2].toIntOrNull() ?: return@replace it.value
            if (d == 0) it.value else fractionalLocative(d) + " " + number(p)
        }
        text = decimal.replace(text) {
            val integer = it.groupValues[1].toIntOrNull() ?: return@replace it.value
            val decimals = it.groupValues[2].map { c -> unit[c.digitToInt()] }.joinToString(" ")
            number(integer) + " virgül " + decimals
        }
        text = percent.replace(text) {
            val pct = it.groupValues[1].toIntOrNull()
            if (pct == null) it.value else "yüzde " + number(pct)
        }
        text = text.replace("≤", " küçük veya eşittir ")
            .replace("≥", " büyük veya eşittir ")
            .replace("≠", " eşit değildir ")
            .replace("×", " çarpı ")
            .replace("÷", " bölü ")
            .replace("→", " sonra ")
            .replace("=", " eşittir ")
            .replace(" + ", " artı ")
            .replace(" − ", " eksi ")
            .replace(" - ", " eksi ")
            .replace("°C", " derece selsiyus")
        // Unary negative values (e.g. -4, −3), without altering hyphenated words.
        text = text.replace(Regex("""(?<![\p{L}\p{N}])[-−](?=\d)"""), "eksi ")
        // Strict integer token boundaries prevent words like 2'de or 3'er
        // from being malformed by partial number replacement.
        text = numeric.replace(text) {
            val n = it.value.toIntOrNull()
            if (n == null) it.value else number(n)
        }
        return text.replace(Regex("""\s{2,}"""), " ").trim()
    }
}
