package com.yigit033.matematikmacarasi.learning

import kotlin.random.Random

/** Grade-specific conceptual follow-up problems with deterministic questions. */
object DeepLessonQuestionBank {
    fun question(lesson: Lesson, seed: Long, index: Int): PracticeQuestion {
        require(lesson.skill == Skill.DEEP_DIVE && index in 0..3)
        val r = Random(seed + index * 10007L + lesson.id.hashCode().toLong())
        fun n(low: Int, high: Int) = r.nextInt(low, high)
        val answer: Int
        val question: String
        val explanation: String
        when (lesson.id) {
            "g1_make_ten" -> {
                val a=n(2,10); answer=10-a
                question="$a nesnenin 10 olması için kaç nesne eklemeliyiz?"
                explanation="$a + $answer = 10. Onluğu tamamladık."
            }
            "g1_compare_data" -> {
                val a=n(5,11);val b=n(2,a);answer=a-b
                question="Grafikte A=$a, B=$b nesne var. A grubunda kaç nesne daha fazla?"
                explanation="Farkı bul: $a − $b = $answer."
            }
            "g2_quarter" -> {
                val each=n(2,13);answer=each
                question="${4*each} nesnenin çeyreği kaç nesnedir?"
                explanation="${4*each} nesneyi dört eş gruba ayır; bir grupta $answer nesne olur."
            }
            "g2_hundred_place" -> {
                val h=n(1,4);val t=n(1,10);val u=n(0,10)
                answer=h*100+t*10+u
                question="$h yüzlük, $t onluk ve $u birlik kaç eder?"
                explanation="$h yüzlük ${h*100}, $t onluk ${t*10}, $u birlik eder; toplam $answer."
            }
            "g3_share_equal" -> {
                val groups=n(2,7);val each=n(2,12);answer=each
                question="${groups*each} nesne $groups çocuğa eşit dağıtılırsa her birine kaç nesne düşer?"
                explanation="${groups*each} ÷ $groups = $answer. Her grup aynı sayıda nesne alır."
            }
            "g3_quarter_of_set" -> {
                val each=n(2,12);answer=each
                question="${4*each} nesnenin dörtte biri kaçtır?"
                explanation="Dört eş gruptan birinin büyüklüğü ${4*each} ÷ 4 = $answer."
            }
            "g4_same_denominator" -> {
                val den=n(5,11);val a=n(1,den-1);val b=n(a+1,den)
                answer=b
                question="$a/$den ve $b/$den kesirlerinden büyük olanın payı kaçtır?"
                explanation="Paydalar aynı: $b, $a'dan büyük olduğu için $b/$den daha büyüktür."
            }
            "g4_two_step" -> {
                val groups=n(2,7);val each=n(2,9);val added=n(1,12)
                answer=groups*each+added
                question="$groups kutunun her birinde $each kalem var. $added kalem daha eklenirse toplam kaç kalem olur?"
                explanation="$groups × $each = ${groups*each}; sonra $added ekle: $answer."
            }
            "g5_parentheses" -> {
                val factor=n(2,7);val a=n(2,10);val b=n(2,10)
                answer=factor*(a+b)
                question="$factor × ($a + $b) = ?"
                explanation="Önce parantez: $a + $b = ${a+b}. Sonra $factor ile çarp: $answer."
            }
            "g5_tenths_add" -> {
                val whole=n(1,6);val tenths=n(1,5);val extra=n(1,5)
                answer=whole*10+tenths+extra
                question="$whole,$tenths + 0,$extra işlemi kaç onda bir eder?"
                explanation="$whole,$tenths = ${10*whole+tenths} onda bir; $extra daha ekle, $answer."
            }
            "g6_absolute_value" -> {
                val magnitude=n(2,20);answer=magnitude
                question="|−$magnitude| işleminin değeri kaçtır?"
                explanation="−$magnitude sıfıra $magnitude birim uzaklıkta. Mutlak değeri $answer."
            }
            "g6_pattern_rule" -> {
                val a=n(2,11);val step=n(2,8)
                answer=a+4*step
                question="$a, ${a+step}, ${a+2*step}, ${a+3*step}, ? örüntüsünde sıradaki sayı kaçtır?"
                explanation="Her seferinde $step ekle: ${a+3*step} + $step = $answer."
            }
            "g7_fraction_sum" -> {
                val den=n(6,13);val a=n(1,den-2);val b=n(1,den-a)
                answer=a+b
                question="$a/$den + $b/$den toplamının payı kaçtır?"
                explanation="Paydalar eşit olduğundan payları topla: $a + $b = $answer; payda $den kalır."
            }
            "g7_percent_quarter" -> {
                val quarter=n(3,26);answer=quarter
                question="${4*quarter} sayısının %25'i kaçtır?"
                explanation="%25, dörtte birdir: ${4*quarter} ÷ 4 = $answer."
            }
            "g8_lcm" -> {
                val k=n(3,13);val a=k*2;val b=k*3;answer=k*6
                question="$a ve $b sayılarının EKOK'u kaçtır?"
                explanation="$a ve $b için ilk ortak pozitif kat $answer. EKOK = $answer."
            }
            "g8_square_root" -> {
                val k=n(3,16);answer=k
                question="√${k*k} kaçtır?"
                explanation="$k × $k = ${k*k} olduğundan √${k*k} = $answer."
            }
            "g8_linear_value" -> {
                val m=n(2,7);val b=n(1,11);val x=n(2,10)
                answer=m*x+b
                question="y = ${m}x + $b ve x = $x ise y kaçtır?"
                explanation="x yerine $x yaz: $m × $x + $b = $answer."
            }
            "g8_coordinate_mirror" -> {
                val x=n(2,13);val y=n(1,10);answer=-x
                question="A($x, $y) noktası y eksenine yansıtılırsa yeni x koordinatı kaç olur?"
                explanation="Y eksenine yansımada x işaret değiştirir; ($x, $y) → ($answer, $y)."
            }
            else -> error("Unregistered Ö6 deep lesson generator: " + lesson.id)
        }
        val options = linkedSetOf(answer)
        for (value in listOf(answer-1, answer+1, answer-2, answer+2,
            -answer, answer+3, answer+5, answer*2, answer-5, answer+10)) {
            options.add(value)
            if (options.size == 4) break
        }
        require(options.size == 4)
        return PracticeQuestion(question,options.shuffled(r),answer,explanation)
    }
}
