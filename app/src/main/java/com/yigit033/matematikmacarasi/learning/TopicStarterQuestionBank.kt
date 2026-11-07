package com.yigit033.matematikmacarasi.learning

import kotlin.random.Random

/** One mathematically aligned, replayable question family per starter unit.
 * Never equates a single four-question run to mastery of an entire topic.
 */
object TopicStarterQuestionBank {
    fun question(lesson: Lesson, seed: Long, index: Int): PracticeQuestion {
        require(lesson.skill == Skill.TOPIC_STARTER && index in 0..3)
        val random = Random(seed + 3571L * index + lesson.id.hashCode().toLong())
        fun n(from: Int, to: Int): Int = random.nextInt(from, to)
        val answer: Int
        val prompt: String
        val explanation: String
        when (lesson.id) {
            "g1_compare" -> {
                val a = n(3, 9)
                val b = n(1, a)
                val first = if (random.nextBoolean()) a else b
                val second = if (first == a) b else a
                answer = a
                prompt = "A grubunda $first, B grubunda $second nesne var. Daha çok nesneli grupta kaç nesne var?"
                explanation = "$a, $b sayısından büyük olduğundan daha çok olan grupta $a nesne var."
            }
            "g1_sort_data" -> {
                val a = n(4, 10); val b = n(1, a); val c = n(1, b + 1)
                answer = minOf(a, b, c)
                prompt = "A=$a, B=$b, C=$c nesne. En az nesne olan grupta kaç nesne bulunur?"
                explanation = "En küçük sayı $answer olduğu için en az nesne bu gruptadır."
            }
            "g2_corners" -> {
                val shape = if (random.nextBoolean()) "üçgen" else "kare"
                answer = if (shape == "üçgen") 3 else 4
                prompt = "Bir $shape kaç köşelidir?"
                explanation = "$shape, $answer köşeye sahiptir."
            }
            "g2_simple_table" -> {
                val a = n(2, 8); val b = n(8, 14); val c = n(2, 8)
                answer = b
                prompt = "Tablo: A=$a, B=$b, C=$c. En çok nesneli grupta kaç nesne var?"
                explanation = "$b, diğer değerlerden daha büyüktür."
            }
            "g3_three_digit" -> {
                val h = n(1, 10); val t = n(1, 10); val u = n(0, 10)
                val value = 100 * h + 10 * t + u
                answer = t * 10
                prompt = "$value sayısının onlar basamağının değeri kaçtır?"
                explanation = "Onlar basamağındaki $t sayısının değeri $answer'dur."
            }
            "g3_rectangle" -> {
                val a = n(3, 10); val b = n(2, a)
                answer = 2 * (a + b)
                prompt = "Uzun kenarı $a, kısa kenarı $b birim olan dikdörtgenin çevresi kaç birimdir?"
                explanation = "İki uzun ve iki kısa kenar: $a + $b + $a + $b = $answer."
            }
            "g4_large_place" -> {
                val thousands = n(1, 10); val hundreds = n(1, 10)
                val value = thousands * 1000 + hundreds * 100
                answer = thousands * 1000
                prompt = "$value sayısının binler basamağının değeri kaçtır?"
                explanation = "Binler basamağındaki $thousands rakamının değeri $answer."
            }
            "g4_time_units" -> {
                val hours = n(1, 6)
                answer = 60 * hours
                prompt = "$hours saat kaç dakikadır?"
                explanation = "Bir saat 60 dakikadır; $hours × 60 = $answer dakika."
            }
            "g5_large_numbers" -> {
                val thousands = n(2, 10); val hundreds = n(1, 10)
                answer = thousands * 1000 + hundreds * 100
                prompt = "$thousands binlik ve $hundreds yüzlük toplam kaçtır?"
                explanation = "$thousands × 1000 + $hundreds × 100 = $answer."
            }
            "g5_operation_order" -> {
                val a = n(2, 10); val b = n(2, 9); val c = n(2, 9)
                answer = a + b * c
                prompt = "$a + $b × $c = ?"
                explanation = "Önce $b × $c, ardından $a eklenir: $answer."
            }
            "g5_angles" -> {
                val angle = n(15, 90)
                answer = 90 - angle
                prompt = "$angle derecelik açı kaç derece büyütülürse dik açı olur?"
                explanation = "Dik açı 90 derecedir. 90 − $angle = $answer derece."
            }
            "g5_simple_chance" -> {
                val red = n(2, 7); val blue = n(1, 6)
                answer = red + blue
                prompt = "Kutuda $red kırmızı ve $blue mavi bilye var. Mavi gelme olasılığının paydası kaçtır?"
                explanation = "Toplam $answer eş olasılıklı bilye vardır; olasılığın paydası toplam bilye sayısıdır."
            }
            "g6_polygon" -> {
                val edges = n(3, 9)
                answer = edges
                prompt = "$edges kenarlı basit bir çokgenin kaç köşesi vardır?"
                explanation = "Bir çokgenin kenar sayısı köşe sayısına eşittir: $answer."
            }
            "g6_area_units" -> {
                val a = n(3, 10); val b = n(2, 9)
                answer = a * b
                prompt = "$a birim ve $b birim kenarlı dikdörtgenin alanı kaç birim karedir?"
                explanation = "Alan $a × $b = $answer birim karedir."
            }
            "g6_data_range" -> {
                val low = n(2, 9); val high = n(low + 3, low + 13)
                answer = high - low
                prompt = "Verilerin en küçüğü $low, en büyüğü $high. Açıklık kaçtır?"
                explanation = "Açıklık en büyük eksi en küçüktür: $high − $low = $answer."
            }
            "g7_mirror" -> {
                val distance = n(2, 13)
                answer = distance
                prompt = "Bir nokta simetri eksenine $distance birim uzakta. Yansıması eksene kaç birim uzakta?"
                explanation = "Yansımada eksene uzaklık aynı kalır: $distance birim."
            }
            "g7_area_compare" -> {
                val a = n(4, 12); val b = n(2, 10)
                answer = a * b
                prompt = "Kenarları $a ve $b birim olan dikdörtgenin alanı kaç birim karedir?"
                explanation = "Yüzeyin alanı $a × $b = $answer birim kare; çevre hesabı farklıdır."
            }
            "g7_median" -> {
                val a = n(2, 9); val b = n(a + 2, a + 10); val c = n(b + 2, b + 10)
                answer = b
                prompt = "$a, $b, $c sayılarının ortancası kaçtır?"
                explanation = "Sayılar artan sırada; tam ortadaki değer $answer."
            }
            "g7_probability" -> {
                val favorable = n(1, 6)
                val total = favorable + n(2, 8)
                answer = favorable
                prompt = "$total eş olasılıklı durumun $favorable tanesi isteniyor. Olasılık kesrinin payı kaçtır?"
                explanation = "Olasılık $favorable/$total; pay istenen durum sayısıdır."
            }
            "g8_right_triangle" -> {
                val triple = listOf(intArrayOf(3,4,5),intArrayOf(5,12,13),
                    intArrayOf(6,8,10),intArrayOf(8,15,17))[n(0,4)]
                answer = triple[2]
                prompt = "Dik kenarları " + triple[0] + " ve " + triple[1] +
                    " birim olan dik üçgenin hipotenüsü kaç birimdir?"
                explanation = "Pisagor: " + triple[0] + "² + " + triple[1] +
                    "² = " + answer + "²; hipotenüs $answer birim."
            }
            "g8_prism_volume" -> {
                val a = n(2, 7); val b = n(2, 6); val h = n(2, 5)
                answer = a * b * h
                prompt = "Kenar uzunlukları $a, $b ve $h birim olan dikdörtgenler prizmasının hacmi kaç birim küptür?"
                explanation = "Hacim: $a × $b × $h = $answer birim küp."
            }
            "g8_data_probability" -> {
                val favorable = n(1, 9)
                answer = favorable * 10
                prompt = "10 eş olasılıklı durumdan $favorable tanesi isteniyor. Olasılık yüzde kaçtır?"
                explanation = "$favorable/10 = %$answer. Payda 100'e genişletildiğinde pay $answer olur."
            }
            else -> error("Missing starter question family for " + lesson.id)
        }
        require(answer >= 0)
        val options = mutableListOf(answer)
        for (candidate in listOf(
            answer - 2, answer + 2, answer - 1, answer + 1, answer + 3,
            answer + 5, answer + 10, answer * 2, answer / 2, answer + 100,
            answer + 1000
        )) {
            if (candidate >= 0 && candidate !in options) options.add(candidate)
            if (options.size == 4) break
        }
        require(options.size == 4)
        return PracticeQuestion(prompt, options.shuffled(random), answer, explanation)
    }
}
