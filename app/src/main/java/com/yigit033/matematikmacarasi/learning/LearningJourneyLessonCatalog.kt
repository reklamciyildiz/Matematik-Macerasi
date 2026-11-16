package com.yigit033.matematikmacarasi.learning

import kotlin.random.Random

/**
 * P2: six complete, authored micro-lessons targeted to documented partial
 * curriculum gaps in grades 2–4. Original MEB texts are NOT redistributed.
 *
 * Every mapping deliberately says PARTIAL: a four/eight-choice digital lesson
 * cannot certify the whole outcome, especially open-ended reasoning/process.
 */
object LearningJourneyLessonCatalog {
    data class Unit(
        val grade: Int,
        val topicKey: String,
        val lesson: Lesson,
        val officialCode: String,
        val source: String,
        val scopeNote: String,
        val script: LessonTeachingScript
    )

    private const val TYMM_2 =
        "https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/116"
    private const val TYMM_2_OPS =
        "https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/164"
    private const val TYMM_3_OPS =
        "https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/264"
    private const val TYMM_3_DATA =
        "https://tymm.meb.gov.tr/ilkokul-matematik-dersi/unite/288"
    private const val OLD_4 =
        "https://mufredat.meb.gov.tr/Dosyalar/201813017165445-MATEMAT%C4%B0K%20%C3%96%C4%9ERET%C4%B0M%20PROGRAMI%202018v.pdf"

    private fun u(
        grade: Int, key: String, topic: String, title: String,
        teaching: String, example: String, hint: String,
        code: String, source: String, note: String,
        visual: TeachingVisual, groups: List<Int> = emptyList(),
        sequence: List<Int> = emptyList(),
        caption: String, steps: List<String>,
        check: String, right: String, distractor: String, reason: String
    ): Unit {
        val lesson = Lesson("g${grade}_$key", title, teaching, example, hint, Skill.JOURNEY)
        val script = LessonTeachingScript(
            visual = visual, headline = title,
            earlyExplanation = teaching, laterExplanation = teaching,
            groups = groups, sequence = sequence, diagramCaption = caption,
            steps = steps,
            check = LessonConceptCheck(
                prompt = check, choices = listOf(right, distractor),
                correctIndex = 0, explanation = reason,
                wrongHint = hint
            )
        )
        return Unit(grade, topic, lesson, code, source, note, script)
    }

    private val units: List<Unit> = listOf(
        u(
            2, "backward_count", "rhythm", "Geriye Ritmik Sayma",
            "Sayı doğrusunda eşit aralıklarla geriye ilerlediğinde her adımda aynı miktarı çıkarırsın.",
            "20, 16, 12, 8, 4", "İki sayı arasındaki farkı bul ve geriye doğru tekrar çıkar.",
            "MAT.2.1.4", TYMM_2,
            "Ritmik saymanın geriye doğru sayısal örnekleri; şekil örüntüsü ve performans süreci kapsam dışı.",
            TeachingVisual.NUMBER_STEPS,
            sequence = listOf(20, 16, 12, 8, 4),
            caption = "Her adımda 4 azalt: 20 → 16 → 12 → 8 → 4",
            steps = listOf(
                "20'den başla ve sayı doğrusunda 4 birim geri git: 16.",
                "16'dan yine 4 çıkar: 12.",
                "12'den 4 çıkar: 8; ardından 4'e ulaş."
            ),
            check = "20, 16, 12, ? örüntüsünde eksik sayı nedir?",
            right = "8; dört azaltmaya devam ederiz.",
            distractor = "10; iki azaltırız.",
            reason = "20−4=16, 16−4=12 ve 12−4=8."
        ),
        u(
            2, "inverse_ops", "addition", "Toplama ve Çıkarma Birbirini Kontrol Eder",
            "Toplayarak bulduğun sonucu çıkarma ile kontrol edebilirsin. İki işlem birbirinin tersidir.",
            "7 + 4 = 11; 11 − 7 = 4",
            "Önce hangi iki sayının birleştiğini, sonra hangi sayının eksiltildiğini düşün.",
            "MAT.2.2.3", TYMM_2_OPS,
            "Toplama ve çıkarmanın terslik ilişkisine ait sayısal örnekler; açık uçlu strateji tartışması kapsam dışı.",
            TeachingVisual.EQUATION, groups = listOf(7, 4, 11),
            caption = "7 + 4 = 11 ↔ 11 − 7 = 4",
            steps = listOf(
                "Yedi nesneye dört nesne ekle: on bir.",
                "On bir nesneden yediyi çıkar.",
                "Geriye dört kalır. Toplama sonucunu çıkarma ile kontrol ettik."
            ),
            check = "6 + 3 = 9. Hangi çıkarma bu toplamayı kontrol eder?",
            right = "9 − 6 = 3",
            distractor = "9 + 6 = 15",
            reason = "Toplamdan bir toplananı çıkarırsak diğer toplananı buluruz."
        ),
        u(
            3, "story_two_step", "operations", "Önce Grupları Say, Sonra Ekle",
            "Günlük yaşamda bazı problemler iki işlem ister: önce eş grupları say, sonra eklenenleri bul.",
            "3 × 4 + 2 = 14",
            "Soruda önce eş grupların toplamını hesapla; sonraki eklemeyi unutma.",
            "MAT.3.2.6", TYMM_3_OPS,
            "Dört işlem gerektiren günlük yaşam problemlerinin iki işlemli sayısal alt örneği; tüm problem kurma süreci kapsam dışı.",
            TeachingVisual.EQUAL_GROUPS, groups = listOf(4, 4, 4, 2),
            caption = "Üç dörtlü grup ve iki ek: 4 + 4 + 4 + 2 = 14",
            steps = listOf(
                "Üç kutuda dörder oyuncak bulunuyor.",
                "Önce eş grupları hesapla: 3 × 4 = 12.",
                "İki oyuncak daha ekle: 12 + 2 = 14."
            ),
            check = "3 kutuda 4'er nesne ve 2 ek nesne var. Hangi işlem sırası doğrudur?",
            right = "Önce 3 × 4, sonra + 2.",
            distractor = "Önce 4 + 2, sonra × 3.",
            reason = "İlk önce bütün kutuları sayarız; ardından dışarıdan eklenenleri toplarız."
        ),
        u(
            3, "data_difference", "data", "Grafikte Farkı Keşfet",
            "Grafikteki sütunların yükseklikleri miktarları gösterir. Farkı bulmak için büyüğünden küçüğünü çıkar.",
            "Elma: 9, armut: 5 → 9 − 5 = 4 fazla",
            "Sütunlardaki miktarları oku, eşit kısımları karşılaştır.",
            "MAT.3.4.1", TYMM_3_DATA,
            "Veriye dayalı tek veri grubunda sayısal karşılaştırma; veri toplama, soru oluşturma ve araştırma süreci kapsam dışı.",
            TeachingVisual.BAR_CHART, groups = listOf(9, 5),
            caption = "9 elma, 5 armut: 4 elma daha fazla",
            steps = listOf(
                "Birinci sütunda dokuz nesne var.",
                "İkinci sütunda beş nesne var.",
                "Beş nesneyi eşleştir; dört nesne fazla kaldı."
            ),
            check = "A=9, B=5 ise farkı bulmak için hangi işlemi yaparsın?",
            right = "9 − 5",
            distractor = "9 + 5",
            reason = "Fazlalık için büyük miktardan küçük miktarı çıkarırız."
        ),
        u(
            4, "round_tens", "place", "En Yakın Onluğa Yuvarla",
            "Sayı iki onluğun arasındadır. Birler basamağı 5 veya daha büyükse üst onluğa yuvarlanır.",
            "1646 → 1650",
            "Önce birler basamağına bak, sonra iki komşu onluğu karşılaştır.",
            "M.4.1.1.4", OLD_4,
            "En çok dört basamaklı sayıları onluğa yuvarlama alt becerisi; yüzlüğe yuvarlama bu dersin kapsamında değil.",
            TeachingVisual.NUMBER_STEPS,
            sequence = listOf(1640, 1642, 1644, 1646, 1650),
            caption = "1646, 1650'ye 1640'tan daha yakındır",
            steps = listOf(
                "1646 iki onluğun arasındadır: 1640 ve 1650.",
                "Birler basamağı 6 olduğundan üst onluğa daha yakın.",
                "Sonuç 1650. Son basamak 5 olsaydı yine üst onluğu seçerdik."
            ),
            check = "1646 sayısı en yakın onluğa yuvarlanınca kaç olur?",
            right = "1650",
            distractor = "1640",
            reason = "Birler basamağı 6 olduğu için 1650'ye yuvarlanır."
        ),
        u(
            4, "unit_fraction_compare", "fractions", "Birim Kesirleri Karşılaştır",
            "Aynı büyüklükte iki bütün daha çok eş parçaya bölünürse her bir parça küçülür.",
            "1/3 > 1/6",
            "Önce aynı büyüklükte iki bütün hayal et; birini daha çok parçaya ayır.",
            "M.4.1.6.2", OLD_4,
            "Paydası en çok 20 olan iki birim kesri modelle karşılaştırma; tüm sıralama stratejileri kapsam dışı.",
            TeachingVisual.FRACTION_BAR, groups = listOf(1, 3),
            caption = "Aynı bütün: 1/3, 1/6'dan büyüktür",
            steps = listOf(
                "Bir bütünü üç eş parçaya ayır, birini seç.",
                "Aynı büyüklükteki diğer bütünü altı eş parçaya ayır, birini seç.",
                "Üçe bölünen parçanın büyüklüğü, altıya bölünenden fazladır."
            ),
            check = "Aynı büyüklükte bütünlerde hangi birim kesir daha büyük?",
            right = "1/3",
            distractor = "1/6",
            reason = "Birim kesirlerde payda küçüldükçe eş parçanın büyüklüğü artar."
        )
    )

    fun forGrade(grade: Int): List<Lesson> =
        units.filter { it.grade == grade }.map { it.lesson }

    fun lessonIdsForTopic(grade: Int, topicKey: String): List<String> =
        units.filter { it.grade == grade && it.topicKey == topicKey }.map { it.lesson.id }

    fun forLesson(lesson: Lesson): LessonTeachingScript =
        requireNotNull(units.firstOrNull { it.lesson.id == lesson.id }) {
            "Missing authored P2 lesson for ${lesson.id}"
        }.script

    fun all(): List<Unit> = units
}

object LearningJourneyQuestionBank {
    fun question(lesson: Lesson, seed: Long, index: Int): PracticeQuestion {
        require(lesson.skill == Skill.JOURNEY && index in 0..7)
        val random = Random(seed + lesson.id.hashCode().toLong() + index * 9173L)
        val answer: Int
        val prompt: String
        val explanation: String
        val distractors: List<Int>
        fun n(from: Int, until: Int): Int = random.nextInt(from, until)
        when (lesson.id) {
            "g2_backward_count" -> {
                val step = listOf(2, 3, 4, 5)[n(0, 4)]
                val max = when (step) { 2 -> 20; 3 -> 30; 4 -> 40; else -> 100 }
                val start = step * n(4, max / step + 1)
                answer = start - 3 * step
                prompt = "$start, ${start-step}, ${start-2*step}, ? · $step'er geriye say."
                explanation = "Her adımda $step çıkar: ${start-2*step} − $step = $answer."
                distractors = listOf(start-2*step, answer-step, start-step, answer+3*step)
            }
            "g2_inverse_ops" -> {
                val a = n(6, 48)
                val b = n(3, 99-a)
                answer = b
                prompt = "${a+b} − $a = ? · Toplama ile kontrol et."
                explanation = "$a + $b = ${a+b}; bu nedenle ${a+b} − $a = $b."
                distractors = listOf(a, a+b, b+1, b-1)
            }
            "g3_story_two_step" -> {
                val group = n(2, 7)
                val each = n(2, 10)
                val extra = n(1, 12)
                answer = group * each + extra
                prompt = "$group sepette ${each}'er elma var. $extra elma daha eklendi. Toplam kaç?"
                explanation = "Önce $group × $each = ${group*each}; ardından $extra ekle: $answer."
                distractors = listOf(group*each, group*each+extra*2, group+each+extra, group*(each+extra))
            }
            "g3_data_difference" -> {
                val a = n(7, 20)
                val b = n(2, a)
                answer = a-b
                prompt = "Grafikte elma=$a, armut=$b. Elma kaç tane fazladır?"
                explanation = "Elma $a, armut $b. Aradaki fark: $a − $b = $answer."
                distractors = listOf(a+b, a, b, answer+1, answer-1)
            }
            "g4_round_tens" -> {
                val tens = n(101, 970)
                val units = n(1, 10)
                val value = tens * 10 + units
                answer = (value + 5) / 10 * 10
                prompt = "$value sayısını en yakın onluğa yuvarla."
                explanation = "Komşu onluklar ${tens*10} ve ${(tens+1)*10}. Birler basamağı $units olduğu için sonuç $answer."
                distractors = listOf(answer-10, answer+10, answer-20, answer+20, answer-30, answer+30)
            }
            "g4_unit_fraction_compare" -> {
                val small = n(2, 11)
                val large = n(small+1, 21)
                answer = small
                prompt = "1/$small ve 1/$large birim kesirlerinden büyük olanın paydası kaçtır?"
                explanation = "Aynı bütünün $small eş parçasından biri, $large eş parçasından birinden büyüktür. Büyük birim kesir 1/$small."
                distractors = listOf(large, small+1, large-1, small-1, 2, 3, 4, 5)
            }
            else -> error("Missing P2 question generator for ${lesson.id}")
        }
        // Distractors must be mathematically plausible. In rounding tasks
        // every option is another multiple of 10; otherwise the child could
        // guess the only value ending in zero without knowing rounding.
        // In unit-fraction tasks every option must be a valid denominator.
        val options = linkedSetOf(answer)
        val fallback = listOf(
            answer - 1, answer + 1, answer - 2, answer + 2,
            answer - 10, answer + 10, answer - 20, answer + 20,
            2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20
        )
        for (candidate in distractors + fallback) {
            if (candidate < 0) continue
            if (lesson.id == "g4_round_tens" && (candidate % 10 != 0 || candidate !in 1000..9999)) continue
            if (lesson.id == "g4_unit_fraction_compare" && candidate !in 2..20) continue
            options.add(candidate)
            if (options.size == 4) break
        }
        require(options.size == 4) { "Not enough valid options for ${lesson.id}" }
        return PracticeQuestion(prompt, options.toList().shuffled(random), answer, explanation)
    }
}
