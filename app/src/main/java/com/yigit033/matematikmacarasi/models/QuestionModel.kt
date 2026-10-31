package com.yigit033.matematikmacarasi.models

/**
 * QuestionModel, oyunlarda kullanılan matematik sorularını temsil eder.
 * Farklı soru türlerini ve doğru cevapları yönetir.
 */
data class QuestionModel(
    val id: String = "",
    val questionType: QuestionType = QuestionType.ADDITION,
    val difficulty: DifficultyLevel = DifficultyLevel.EASY,
    val firstNumber: Int,
    val secondNumber: Int? = null,
    val correctAnswer: Int,
    val options: List<Int> = emptyList(), // Çoktan seçmeli sorular için
    val timeLimit: Int = 0 // Saniye cinsinden soru için süre limiti (0=limit yok)
) {
    /**
     * Soruyu metin olarak döndürür.
     */
    fun getQuestionText(): String {
        return when (questionType) {
            QuestionType.ADDITION -> "$firstNumber + ${secondNumber ?: ""} = ?"
            QuestionType.SUBTRACTION -> "$firstNumber - ${secondNumber ?: ""} = ?"
            QuestionType.MULTIPLICATION -> "$firstNumber × ${secondNumber ?: ""} = ?"
            QuestionType.DIVISION -> "$firstNumber ÷ ${secondNumber ?: ""} = ?"
            QuestionType.NUMBER_RECOGNITION -> "Bu sayı kaçtır? $firstNumber"
            QuestionType.EVEN_ODD -> if (correctAnswer == 1) "$firstNumber Çift mi?" else "$firstNumber Tek mi?"
            QuestionType.DIVISIBILITY -> "$firstNumber sayısı ${secondNumber ?: 2} ile bölünebilir mi?"
            QuestionType.GREATER_THAN -> "$firstNumber > ${secondNumber ?: ""} ?"
            QuestionType.LESS_THAN -> "$firstNumber < ${secondNumber ?: ""} ?"
            QuestionType.EQUALS -> "$firstNumber = ${secondNumber ?: ""} ?"
            QuestionType.PATTERN -> "Örüntünün bir sonraki sayısı nedir?"
            QuestionType.MIXED_BASIC -> "$firstNumber ${getOperationSymbol()} ${secondNumber ?: ""} = ?"
            QuestionType.MIXED_ADVANCED -> "$firstNumber ${getOperationSymbol()} ${secondNumber ?: ""} = ?"
        }
    }
    
    /**
     * İşlem sembolünü döndürür
     */
    private fun getOperationSymbol(): String {
        return when {
            correctAnswer == firstNumber + (secondNumber ?: 0) -> "+"
            correctAnswer == firstNumber - (secondNumber ?: 0) -> "-"
            correctAnswer == firstNumber * (secondNumber ?: 1) -> "×"
            secondNumber != null && secondNumber != 0 && correctAnswer == firstNumber / secondNumber -> "÷"
            else -> "?"
        }
    }
    
    /**
     * Cevabın doğru olup olmadığını kontrol eder.
     */
    fun checkAnswer(answer: Int): Boolean {
        return answer == correctAnswer
    }
    
    companion object {
        /**
         * Belirli bir zorluk seviyesine göre toplama sorusu oluşturur.
         */
        fun createAdditionQuestion(difficulty: DifficultyLevel): QuestionModel {
            val range = when (difficulty) {
                DifficultyLevel.EASY -> 1..10
                DifficultyLevel.MEDIUM -> 1..20
                DifficultyLevel.HARD -> 1..50
                DifficultyLevel.VERY_HARD -> 1..100
                DifficultyLevel.EXPERT -> 10..500
            }
            
            val firstNumber = range.random()
            val secondNumber = range.random()
            val correctAnswer = firstNumber + secondNumber
            
            return QuestionModel(
                questionType = QuestionType.ADDITION,
                difficulty = difficulty,
                firstNumber = firstNumber,
                secondNumber = secondNumber,
                correctAnswer = correctAnswer
            )
        }
        
        /**
         * Belirli bir zorluk seviyesine göre çıkarma sorusu oluşturur.
         */
        fun createSubtractionQuestion(difficulty: DifficultyLevel): QuestionModel {
            val range = when (difficulty) {
                DifficultyLevel.EASY -> 1..10
                DifficultyLevel.MEDIUM -> 1..20
                DifficultyLevel.HARD -> 1..50
                DifficultyLevel.VERY_HARD -> 1..100
                DifficultyLevel.EXPERT -> 10..500
            }
            
            // Negatif sonuçları önlemek için firstNumber > secondNumber olmalı
            val secondNumber = range.random()
            val firstNumber = (secondNumber..range.last).random()
            val correctAnswer = firstNumber - secondNumber
            
            return QuestionModel(
                questionType = QuestionType.SUBTRACTION,
                difficulty = difficulty,
                firstNumber = firstNumber,
                secondNumber = secondNumber,
                correctAnswer = correctAnswer
            )
        }
        
        /**
         * Belirli bir zorluk seviyesine göre çarpma sorusu oluşturur.
         */
        fun createMultiplicationQuestion(difficulty: DifficultyLevel): QuestionModel {
            val range = when (difficulty) {
                DifficultyLevel.EASY -> 1..5
                DifficultyLevel.MEDIUM -> 1..10
                DifficultyLevel.HARD -> 2..12
                DifficultyLevel.VERY_HARD -> 2..20
                DifficultyLevel.EXPERT -> 5..30
            }
            
            val firstNumber = range.random()
            val secondNumber = range.random()
            val correctAnswer = firstNumber * secondNumber
            
            return QuestionModel(
                questionType = QuestionType.MULTIPLICATION,
                difficulty = difficulty,
                firstNumber = firstNumber,
                secondNumber = secondNumber,
                correctAnswer = correctAnswer
            )
        }
        
        /**
         * Belirli bir zorluk seviyesine göre bölme sorusu oluşturur.
         */
        fun createDivisionQuestion(difficulty: DifficultyLevel): QuestionModel {
            val divisorRange = when (difficulty) {
                DifficultyLevel.EASY -> 2..5
                DifficultyLevel.MEDIUM -> 2..10
                DifficultyLevel.HARD -> 2..12
                DifficultyLevel.VERY_HARD -> 2..20
                DifficultyLevel.EXPERT -> 2..50
            }
            
            val resultRange = when (difficulty) {
                DifficultyLevel.EASY -> 1..5
                DifficultyLevel.MEDIUM -> 1..10
                DifficultyLevel.HARD -> 1..20
                DifficultyLevel.VERY_HARD -> 1..50
                DifficultyLevel.EXPERT -> 1..100
            }
            
            // Bölen (divisor) ve bölüm (result) değerlerini belirle
            val secondNumber = divisorRange.random() // bölen
            val correctAnswer = resultRange.random() // bölüm
            
            // Bölünen (dividend) hesapla - kesinlikle tam bölünen bir sayı olacak
            val firstNumber = correctAnswer * secondNumber // bölünen
            
            // Zorluk seviyesine göre sınırları kontrol et
            if (difficulty == DifficultyLevel.EXPERT && firstNumber > 1000) {
                // Uzman seviyede bile çok büyük sayıları sınırla
                return createDivisionQuestion(difficulty)
            }
            
            return QuestionModel(
                questionType = QuestionType.DIVISION,
                difficulty = difficulty,
                firstNumber = firstNumber,
                secondNumber = secondNumber,
                correctAnswer = correctAnswer
            )
        }
        
        /**
         * Belirtilen türde rastgele bir soru oluşturur.
         */
        fun createRandomQuestion(type: QuestionType, difficulty: DifficultyLevel): QuestionModel {
            return when (type) {
                QuestionType.ADDITION -> createAdditionQuestion(difficulty)
                QuestionType.SUBTRACTION -> createSubtractionQuestion(difficulty)
                QuestionType.MULTIPLICATION -> createMultiplicationQuestion(difficulty)
                QuestionType.DIVISION -> createDivisionQuestion(difficulty)
                QuestionType.MIXED_BASIC -> {
                    // Karışık temel işlemler: Toplama, Çıkarma, Çarpma, Bölme
                    val randomType = listOf(
                        QuestionType.ADDITION,
                        QuestionType.SUBTRACTION,
                        QuestionType.MULTIPLICATION,
                        QuestionType.DIVISION
                    ).random()
                    createRandomQuestion(randomType, difficulty)
                }
                QuestionType.MIXED_ADVANCED -> {
                    // İleri seviye karışık işlemler: Toplama, Çıkarma, Çarpma, Bölme
                    val randomType = listOf(
                        QuestionType.ADDITION,
                        QuestionType.SUBTRACTION,
                        QuestionType.MULTIPLICATION,
                        QuestionType.DIVISION
                    ).random()
                    createRandomQuestion(randomType, difficulty)
                }
                else -> createAdditionQuestion(difficulty) // Varsayılan olarak toplama sorusu
            }
        }
    }
}

/**
 * Soru türlerini belirten enum sınıfı.
 */
enum class QuestionType {
    ADDITION,        // Toplama
    SUBTRACTION,     // Çıkarma
    MULTIPLICATION,  // Çarpma
    DIVISION,        // Bölme
    NUMBER_RECOGNITION, // Sayı tanıma
    EVEN_ODD,        // Çift mi tek mi
    DIVISIBILITY,    // Bölünebilirlik
    GREATER_THAN,    // Büyüktür
    LESS_THAN,       // Küçüktür
    EQUALS,          // Eşittir
    PATTERN,         // Örüntü
    MIXED_BASIC,     // Karışık temel işlemler (toplama, çıkarma, çarpma, bölme)
    MIXED_ADVANCED   // İleri seviye karışık işlemler (toplama, çıkarma, çarpma, bölme)
}

/**
 * Zorluk seviyelerini belirten enum sınıfı.
 */
enum class DifficultyLevel {
    EASY,       // Kolay
    MEDIUM,     // Orta
    HARD,       // Zor
    VERY_HARD,  // Çok Zor
    EXPERT      // Uzman
} 