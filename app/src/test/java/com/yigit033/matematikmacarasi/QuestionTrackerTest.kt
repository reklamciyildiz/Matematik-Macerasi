package com.yigit033.matematikmacarasi

import com.yigit033.matematikmacarasi.games.models.QuestionTracker
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuestionTrackerTest {

    private lateinit var tracker: QuestionTracker

    @Before
    fun setUp() {
        tracker = QuestionTracker()
    }

    @Test
    fun testFirstTimeQuestionReturnsFalseAndMarksAsked() {
        val questionId = "3+5=?"
        assertFalse("İlk kez sorulan soru daha önce sorulmuş olmamalı", tracker.isQuestionAsked(questionId))
        assertTrue("Aynı soru ikinci kez sorulduğunda kayıtlı olarak dönmeli", tracker.isQuestionAsked(questionId))
    }

    @Test
    fun testOperationAskedFormat() {
        val first = 12
        val second = 4
        val op = "+"
        assertFalse(tracker.isOperationAsked(first, second, op))
        assertTrue(tracker.isOperationAsked(first, second, op))
        
        // Farklı işlem türü bağımsız izlenmeli
        assertFalse(tracker.isOperationAsked(first, second, "-"))
    }

    @Test
    fun testResetClearsTracker() {
        val q1 = "10*2=?"
        tracker.isQuestionAsked(q1)
        assertTrue(tracker.isQuestionAsked(q1))
        
        tracker.reset()
        assertFalse("Reset sonrasında soru yeniden sorulabilir olmalı", tracker.isQuestionAsked(q1))
    }
}

