package com.yigit033.matematikmacarasi

import com.yigit033.matematikmacarasi.games.models.MathQuestion
import org.junit.Assert.*
import org.junit.Test

class MathQuestionGenerationTest {

    @Test
    fun testAdditionQuestionGeneration() {
        for (level in 1..5) {
            val q = MathQuestion.createForOperationType(operationType = 1, level = level)
            assertEquals("Her soruda tam 4 seçenek olmalı", 4, q.options.size)
            assertTrue("Doğru cevap seçenekler listesinde bulunmalı", q.correctAnswerIndex in 0..3)
            val correctAnswer = q.options[q.correctAnswerIndex]
            assertTrue("Soru metni toplama formatında olmalı", q.question.contains("+"))
            
            // Seçenekler benzersiz olmalı
            assertEquals("Tüm seçenekler birbirinden farklı olmalı", 4, q.options.toSet().size)
            
            // Cevap tam sayı olmalı
            assertNotNull(correctAnswer.toIntOrNull())
        }
    }

    @Test
    fun testSubtractionQuestionGenerationNonNegative() {
        for (level in 1..5) {
            val q = MathQuestion.createForOperationType(operationType = 2, level = level)
            assertEquals(4, q.options.size)
            assertTrue(q.correctAnswerIndex in 0..3)
            val correctAnswer = q.options[q.correctAnswerIndex].toInt()
            
            // Çocuk matematik oyununda çıkarma sonucu negatif olmamalı
            assertTrue("Çıkarma sonucu negatif olamaz: $correctAnswer", correctAnswer >= 0)
            assertTrue(q.question.contains("-"))
        }
    }

    @Test
    fun testMultiplicationQuestionGeneration() {
        for (level in 1..5) {
            val q = MathQuestion.createForOperationType(operationType = 3, level = level)
            assertEquals(4, q.options.size)
            assertTrue(q.correctAnswerIndex in 0..3)
            val correctAnswer = q.options[q.correctAnswerIndex].toInt()
            assertTrue("Çarpma sonucu pozitif olmalı", correctAnswer > 0)
            assertTrue(q.question.contains("×"))
        }
    }

    @Test
    fun testDivisionQuestionGenerationExactResult() {
        for (level in 1..5) {
            val q = MathQuestion.createForOperationType(operationType = 4, level = level)
            assertEquals(4, q.options.size)
            assertTrue(q.correctAnswerIndex in 0..3)
            val result = q.options[q.correctAnswerIndex].toInt()
            assertTrue("Bölme sonucu 1 ile 5 arasında olmalı", result in 1..5)
            assertTrue(q.question.contains("÷"))
        }
    }

    @Test
    fun testEmptyQuestionHelper() {
        val emptyQ = MathQuestion.empty()
        assertEquals(-1, emptyQ.id)
        assertEquals("", emptyQ.question)
        assertEquals(4, emptyQ.options.size)
        assertEquals(0, emptyQ.correctAnswerIndex)
    }
}

