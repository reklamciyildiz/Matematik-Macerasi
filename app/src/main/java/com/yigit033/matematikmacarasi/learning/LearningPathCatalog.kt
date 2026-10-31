package com.yigit033.matematikmacarasi.learning

import kotlin.random.Random

/** P2 pilot içerikleri: sınıfa ait bütün müfredatın karşılığı değildir. */
enum class Skill { COUNT_2, COUNT_3, COUNT_4, ADD_10, ADD_20, MULTIPLY_INTRO, MULTIPLY, DIVISIBLE_15 }
data class Lesson(val id: String, val title: String, val teaching: String, val example: String,
    val hint: String, val skill: Skill)
data class PracticeQuestion(val text: String, val choices: List<Int>, val answer: Int, val explanation: String) {
    init { require(choices.size == 4 && choices.distinct().size == 4 && answer in choices) }
}
object LearningPathCatalog {
    fun forGrade(userClass: String): List<Lesson> {
        val grade = userClass.substringBefore(".").trim().toIntOrNull() ?: 1
        val c2 = Lesson("count_2", "İkişer Ritmik Sayma", "Her yeni sayıya ulaşmak için 2 ekle.",
            "2 → 4 → 6 → 8", "Son sayıya 2 ekle.", Skill.COUNT_2)
        val c3 = Lesson("count_3", "Üçer Ritmik Sayma", "Her yeni sayıya ulaşmak için 3 ekle.",
            "3 → 6 → 9 → 12", "Son sayıya 3 ekle.", Skill.COUNT_3)
        val c4 = Lesson("count_4", "Dörder Ritmik Sayma",
            "Her yeni sayıyı bulmak için 4 ekle. Sonra Gizemli Orman'da uygula.",
            "4 → 8 → 12 → 16", "Son sayıdan başlayarak 4 artır.", Skill.COUNT_4)
        val a10 = Lesson("add_10", "10 İçinde Toplama", "İki grubu birleştirerek toplamını bul.",
            "3 + 2 = 5", "Sayıların birinden başlayıp diğer sayı kadar ilerle.", Skill.ADD_10)
        val a20 = Lesson("add_20", "20 İçinde Toplama", "İki sayıyı birleştir ve sonucu kontrol et.",
            "8 + 5 = 13", "Büyük sayıdan başlayarak küçük sayıyı ekle.", Skill.ADD_20)
        val mulIntro = Lesson("multiply_intro", "Çarpmayı Keşfet",
            "Eş sayıda nesneler içeren grupları birlikte say. Çarpma, tekrarlı toplamayı kısaltır.",
            "4 + 4 + 4 = 3 × 4 = 12", "Önce her grubu say. Sonra bütün grupları birleştir.",
            Skill.MULTIPLY_INTRO)
        val mul = Lesson("multiply", "Çarpma ile Gruplama", "Çarpma, eşit grupları hızlı sayma yoludur.",
            "3 × 4 = 4 + 4 + 4 = 12", "Eşit grupları tekrar eden toplama olarak düşün.", Skill.MULTIPLY)
        val div = Lesson("divisible_15", "Temel Tekrar: Bölünebilme",
            "3 ve 5'e kalansız bölünen sayılar, 15'in katlarıdır.",
            "15, 30, 45, 60", "15'er ilerleyerek ortak katları bul.", Skill.DIVISIBLE_15)
        return when {
            grade <= 1 -> listOf(c2, a10)
            grade == 2 -> listOf(c3, c4, a20, mulIntro)
            grade in 3..4 -> listOf(mul, c3, c4)
            else -> listOf(div, mul) // 5-8. sınıflar için açıkça temel tekrar
        }
    }

    /** Tek oturum boyunca aynı seed, aynı soru; yanlış seçenekler doğru cevap olamaz. */
    fun question(lesson: Lesson, seed: Long, index: Int): PracticeQuestion {
        require(index in 0..3)
        val random = Random(seed + index * 7919L)
        val text: String
        val answer: Int
        val explanation: String
        val wrong: List<Int>
        when (lesson.skill) {
            Skill.COUNT_2, Skill.COUNT_3, Skill.COUNT_4 -> {
                val step = when (lesson.skill) {
                    Skill.COUNT_2 -> 2
                    Skill.COUNT_3 -> 3
                    else -> 4
                }
                val start = step * random.nextInt(1, 9)
                answer = start + 3 * step
                text = "$start, ${start + step}, ${start + 2 * step}, ?"
                explanation = "Her seferinde $step ekleyince $answer olur."
                wrong = listOf(answer - step, answer + step, answer + 2 * step)
            }
            Skill.ADD_10, Skill.ADD_20 -> {
                val limit = if (lesson.skill == Skill.ADD_10) 10 else 20
                val a = random.nextInt(2, limit - 1)
                val b = random.nextInt(1, limit - a + 1)
                answer = a + b
                text = "$a + $b = ?"
                explanation = "$a ve $b birleşince $answer eder."
                wrong = listOf(answer - 1, answer + 1, answer + 2)
            }
            Skill.MULTIPLY_INTRO -> {
                val groups = random.nextInt(2, 6)
                val items = random.nextInt(2, 6)
                answer = groups * items
                text = "$groups eş grup, her grupta $items nesne var. Toplam kaç nesne eder?"
                explanation = "$groups tane $items sayısını toplarsan $answer elde edersin."
                wrong = listOf(answer - items, answer + items, answer + 1)
            }
            Skill.MULTIPLY -> {
                val a = random.nextInt(2, 10)
                val b = random.nextInt(2, 10)
                answer = a * b
                text = "$a × $b = ?"
                explanation = "$a grup, her grupta $b nesne = $answer."
                wrong = listOf(answer - a, answer + a, answer + b)
            }
            Skill.DIVISIBLE_15 -> {
                answer = 15 * random.nextInt(2, 10)
                text = "Hangisi hem 3'e hem de 5'e bölünür?"
                explanation = "$answer, 15'in katı olduğu için ikisine de tam bölünür."
                wrong = listOf(answer + 1, answer + 3, answer + 5)
            }
        }
        val values = (listOf(answer) + wrong).distinct().toMutableList()
        var filler = answer + 1
        while (values.size < 4) {
            if (filler !in values) values.add(filler)
            filler++
        }
        return PracticeQuestion(text, values.take(4).shuffled(random), answer, explanation)
    }
}
