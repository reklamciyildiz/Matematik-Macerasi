package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

/**
 * Independent oracle reconstructs each generated problem's arithmetic
 * from its rendered question, not from the generator's claimed answer.
 */
class TopicStarterMathTruthTest {
    private fun capture(text: String, re: String): List<Int> {
        val match=Regex(re).find(text)
        assertNotNull("Unrecognised question: $text",match)
        return match!!.groupValues.drop(1).map { it.toInt() }
    }

    @Test fun everyStarterAnswerIsTrueAcrossGeneratedVariations() {
        for (entry in TopicStarterCatalog.all()) {
            for (seed in 1L..80L) for (index in 0..3) {
                val q=TopicStarterQuestionBank.question(entry.lesson,seed,index)
                val values: List<Int>
                val expected=when(entry.lesson.id) {
                    "g1_compare" -> {
                        values=capture(q.text,
                            """A grubunda (\d+), B grubunda (\d+)""")
                        values.maxOrNull()!!
                    }
                    "g1_sort_data", "g2_simple_table" -> {
                        values=capture(q.text,
                            """A=(\d+), B=(\d+), C=(\d+)""")
                        if (entry.lesson.id=="g1_sort_data")
                            values.minOrNull()!! else values.maxOrNull()!!
                    }
                    "g2_corners" ->
                        if (q.text.startsWith("Bir üçgen")) 3 else 4
                    "g3_three_digit" -> {
                        values=capture(q.text, """(\d+) sayısının onlar""")
                        values[0]/10%10*10
                    }
                    "g3_rectangle" -> {
                        values=capture(q.text,
                            """Uzun kenarı (\d+), kısa kenarı (\d+)""")
                        2*(values[0]+values[1])
                    }
                    "g4_large_place" -> {
                        values=capture(q.text, """(\d+) sayısının binler""")
                        values[0]/1000*1000
                    }
                    "g4_time_units" -> {
                        values=capture(q.text, """(\d+) saat kaç dakikadır""")
                        values[0]*60
                    }
                    "g5_large_numbers" -> {
                        values=capture(q.text,
                            """(\d+) binlik ve (\d+) yüzlük""")
                        values[0]*1000+values[1]*100
                    }
                    "g5_operation_order" -> {
                        values=capture(q.text, """(\d+) \+ (\d+) × (\d+)""")
                        values[0]+values[1]*values[2]
                    }
                    "g5_angles" -> {
                        values=capture(q.text, """(\d+) derecelik açı""")
                        90-values[0]
                    }
                    "g5_simple_chance" -> {
                        values=capture(q.text,
                            """Kutuda (\d+) kırmızı ve (\d+) mavi""")
                        values.sum()
                    }
                    "g6_polygon" -> {
                        values=capture(q.text, """(\d+) kenarlı basit""")
                        values[0]
                    }
                    "g6_area_units" -> {
                        values=capture(q.text,
                            """(\d+) birim ve (\d+) birim kenarlı""")
                        values[0]*values[1]
                    }
                    "g6_data_range" -> {
                        values=capture(q.text,
                            """en küçüğü (\d+), en büyüğü (\d+)""")
                        values[1]-values[0]
                    }
                    "g7_mirror" -> {
                        values=capture(q.text,
                            """simetri eksenine (\d+) birim uzakta""")
                        values[0]
                    }
                    "g7_area_compare" -> {
                        values=capture(q.text, """Kenarları (\d+) ve (\d+) birim""")
                        values[0]*values[1]
                    }
                    "g7_median" -> {
                        values=capture(q.text,
                            """(\d+), (\d+), (\d+) sayılarının ortancası""")
                        values.sorted()[1]
                    }
                    "g7_probability" -> {
                        values=capture(q.text,
                            """(\d+) eş olasılıklı durumun (\d+) tanesi""")
                        values[1]
                    }
                    "g8_right_triangle" -> {
                        values=capture(q.text,
                            """Dik kenarları (\d+) ve (\d+) birim""")
                        val sum=values[0]*values[0]+values[1]*values[1]
                        val hyp=sqrt(sum.toDouble()).toInt()
                        assertEquals(sum,hyp*hyp)
                        hyp
                    }
                    "g8_prism_volume" -> {
                        values=capture(q.text,
                            """Kenar uzunlukları (\d+), (\d+) ve (\d+) birim""")
                        values.fold(1){total,v->total*v}
                    }
                    "g8_data_probability" -> {
                        values=capture(q.text,
                            """10 eş olasılıklı durumdan (\d+) tanesi""")
                        values[0]*10
                    }
                    else -> error("Missing math oracle: "+entry.lesson.id)
                }
                assertEquals(entry.lesson.id+": "+q.text,expected,q.answer)
                assertTrue(q.choices.contains(expected))
            }
        }
    }
}
