package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Independent maths oracle: parse a generated story and recompute its result.
 * Prevents a wrong generator from passing just because its own answer is
 * consistently listed amongst the four choices.
 */
class ExpandedLessonMathTruthTest {
    private fun numbers(text: String, pattern: String): List<Int> {
        val m = Regex(pattern).find(text)
        assertNotNull("Unexpected prompt: " + text, m)
        return m!!.groupValues.drop(1).map { it.toInt() }
    }

    private fun gcd(a: Int, b: Int): Int {
        var x=a;var y=b
        while (y!=0) { val t=x%y;x=y;y=t }
        return x
    }

    @Test fun everyNewLessonProducesMathematicallyCorrectAnswers() {
        for (lesson in ExpandedLessonCatalog.all()) for (seed in 1L..80L) {
            for (i in 0..3) {
                val q=ExpandedLessonQuestionBank.question(lesson,seed,i)
                val expected=when(lesson.id) {
                    "g1_count20" -> {
                        val n=numbers(q.text, """10 nesneye (\d+) nesne""");10+n[0]
                    }
                    "g1_takeaway" -> {
                        val n=numbers(q.text, """(\d+) elmadan (\d+) elma""");n[0]-n[1]
                    }
                    "g1_shapes" -> if(q.text.startsWith("Üçgen")) 3 else 4
                    "g2_place" -> {
                        val n=numbers(q.text, """(\d+) onluk ve (\d+) birlik""")
                        n[0]*10+n[1]
                    }
                    "g2_half" -> numbers(q.text, """(\d+) nesnenin yarısı""")[0]/2
                    "g2_clock" -> {
                        val n=numbers(q.text, """Saat (\d+) iken (\d+) saat sonra""")
                        n[0]+n[1]
                    }
                    "g3_third" -> numbers(q.text, """(\d+) nesnenin üçte biri""")[0]/3
                    "g3_money" -> {
                        val n=numbers(q.text, """(\d+) TL ile (\d+) TL""")
                        n[0]-n[1]
                    }
                    "g3_chart" -> {
                        val n=numbers(q.text, """kitap (\d+), defter (\d+)""")
                        n[0]-n[1]
                    }
                    "g4_fraction" -> {
                        val n=numbers(q.text, """(\d+) sayısının (\d+)/(\d+) kesri""")
                        n[0]*n[1]/n[2]
                    }
                    "g4_perimeter" -> {
                        val n=numbers(q.text, """Kenarları (\d+) ve (\d+) cm""")
                        2*(n[0]+n[1])
                    }
                    "g4_chart" -> {
                        val n=numbers(q.text, """A=(\d+), B=(\d+), C=(\d+)""")
                        n.sum()
                    }
                    "g5_decimal" -> {
                        val n=numbers(q.text, """(\d+) tam (\d+) onda bir""")
                        10*n[0]+n[1]
                    }
                    "g5_area" -> {
                        val n=numbers(q.text,
                            """(\d+) birim uzunluk ve (\d+) birim genişlikteki alan""")
                        n[0]*n[1]
                    }
                    "g5_mean" -> {
                        val n=numbers(q.text, """(\d+), (\d+), (\d+) sayılarının""")
                        n.sum()/3
                    }
                    "g6_signed" -> {
                        val n=numbers(q.text, """Sıcaklık −(\d+) °C iken (\d+) °C artarsa""")
                        n[1]-n[0]
                    }
                    "g6_ratio" -> {
                        val n=numbers(q.text,
                            """2 ölçek meyveye 3 ölçek su gerekir. (\d+) ölçek""")
                        n[0]/2*3
                    }
                    "g6_equation" -> {
                        val n=numbers(q.text, """x \+ (\d+) = (\d+)""")
                        n[1]-n[0]
                    }
                    "g7_rational" -> {
                        val n=numbers(q.text,
                            """(\d+)/(\d+) kesrine denk, paydası (\d+) olan""")
                        n[0]*n[2]/n[1]
                    }
                    "g7_proportion" -> {
                        val n=numbers(q.text,
                            """(\d+) bilet (\d+) TL ise (\d+) bilet""")
                        n[1]/n[0]*n[2]
                    }
                    "g7_equation" -> {
                        val n=numbers(q.text, """(\d+)x \+ (\d+) = (\d+)""")
                        (n[2]-n[1])/n[0]
                    }
                    "g8_gcd" -> {
                        val n=numbers(q.text, """(\d+) ve (\d+) sayılarının EBOB""")
                        gcd(n[0],n[1])
                    }
                    "g8_power" -> {
                        val n=numbers(q.text, """(\d+) üssü (\d+) ifadesinin""")
                        (1..n[1]).fold(1){value,_->value*n[0]}
                    }
                    "g8_equation" -> {
                        val n=numbers(q.text, """(\d+)\(x \+ (\d+)\) = (\d+)""")
                        n[2]/n[0]-n[1]
                    }
                    else -> error("Missing independent truth assertion for " + lesson.id)
                }
                assertEquals(lesson.id+": "+q.text,expected,q.answer)
            }
        }
    }
}
