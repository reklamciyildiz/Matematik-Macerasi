package com.yigit033.matematikmacarasi.games.models

/**
 * Temel soru modeli
 */
data class QuestionModel(
    val id: Long = System.currentTimeMillis(), // Benzersiz soru ID'si
    val questionText: String,
    val correctAnswer: String,
    val options: List<String> = emptyList(),
    val difficultyLevel: Int = 1,
    val category: String = "general",
    val imageResId: Int? = null,
    val timeToSolve: Int = 0
)

/**
 * Oyun oturumunda sorulan soruları takip eden sınıf.
 * Bu sınıf, aynı soruların tekrar sorulmamasını sağlar.
 */
class QuestionTracker {
    // Her oyun oturumunda sorulmuş soruları izler
    private val askedQuestions = mutableSetOf<String>()
    
    /**
     * Sorunun daha önce sorulup sorulmadığını kontrol eder.
     * Eğer soru daha önce sorulmadıysa, onu kayıtlara ekler.
     * 
     * @param questionIdentifier Soruyu benzersiz şekilde tanımlayan String (ör: "3+5=?")
     * @return Soru daha önce sorulduysa true, aksi takdirde false
     */
    fun isQuestionAsked(questionIdentifier: String): Boolean {
        if (askedQuestions.contains(questionIdentifier)) {
            return true
        }
        
        // Soruyu kaydet
        askedQuestions.add(questionIdentifier)
        return false
    }
    
    /**
     * Belirli bir operasyonda sorunun sorulup sorulmadığını kontrol eder.
     * 
     * @param firstOperand İlk operand 
     * @param secondOperand İkinci operand
     * @param operationType İşlem türü ("+", "-", "×", "÷")
     * @return Soru daha önce sorulduysa true, aksi takdirde false
     */
    fun isOperationAsked(firstOperand: Int, secondOperand: Int, operationType: String): Boolean {
        val questionIdentifier = "$firstOperand$operationType$secondOperand"
        return isQuestionAsked(questionIdentifier)
    }
    
    /**
     * Tracker'ı sıfırlar, tüm soru kayıtlarını temizler
     */
    fun reset() {
        askedQuestions.clear()
    }
    
    /**
     * Sorulmuş soru sayısını döndürür
     */
    fun getAskedQuestionCount(): Int {
        return askedQuestions.size
    }
} 