package com.yigit033.matematikmacarasi.learning

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TurkishMathSpeechTest {
    @Test fun readsFractionsWithNaturalTurkishLocatives() {
        assertEquals("sekizde üç", TurkishMathSpeech.normalize("3/8"))
        assertEquals("dörtte bir", TurkishMathSpeech.normalize("1/4"))
        assertEquals("üçte iki", TurkishMathSpeech.normalize("2/3"))
        assertEquals("yirmide bir", TurkishMathSpeech.normalize("1/20"))
        assertEquals("onda yedi", TurkishMathSpeech.normalize("7/10"))
    }

    @Test fun correctlyPronouncesOperationsAndNegativeNumbers() {
        assertEquals("üç çarpı dört eşittir on iki",
            TurkishMathSpeech.normalize("3 × 4 = 12"))
        assertEquals("sekiz artı beş eşittir on üç",
            TurkishMathSpeech.normalize("8 + 5 = 13"))
        assertEquals("eksi üç", TurkishMathSpeech.normalize("−3"))
        assertEquals("eksi dört", TurkishMathSpeech.normalize("-4"))
        assertEquals("altı bölü iki", TurkishMathSpeech.normalize("6 ÷ 2"))
    }

    @Test fun pronouncesTurkishDecimalsAndPercentsWithoutGuessingMagnitude() {
        assertEquals("sıfır virgül beş", TurkishMathSpeech.normalize("0,5"))
        assertEquals("yüzde yirmi beş", TurkishMathSpeech.normalize("%25"))
        assertEquals("yüzde yüz", TurkishMathSpeech.normalize("%100"))
    }

    @Test fun handlesTurkishHundredsThousandsAndRhythm() {
        assertEquals("bin altı yüz kırk altı", TurkishMathSpeech.number(1646))
        assertEquals("yüz", TurkishMathSpeech.number(100))
        assertEquals("dörder geriye say.", TurkishMathSpeech.normalize("4'er geriye say."))
        assertEquals("onar ilerle.", TurkishMathSpeech.normalize("10'ar ilerle."))
    }

    @Test fun neverMutatesMeaningOfChildrenWords() {
        assertEquals("Aşağıdan yukarıya say.", TurkishMathSpeech.normalize("Aşağıdan yukarıya say."))
        assertTrue(TurkishMathSpeech.normalize("4 / 0").contains("/"))
        assertFalse(TurkishMathSpeech.normalize("1/2").contains("/"))
        assertEquals("", TurkishMathSpeech.normalize(" "))
    }
}
