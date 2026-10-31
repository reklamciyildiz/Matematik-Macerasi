package com.yigit033.matematikmacarasi.games.models

import kotlin.random.Random

/**
 * Matematik sorusu
 */
data class MathQuestion(
    val id: Int,                    // Soru ID
    val question: String,           // Soru metni ("3 + 5 = ?" gibi)
    val options: List<String>,      // Seçenekler
    val correctAnswerIndex: Int     // Doğru cevabın indeksi
) {
    // Boş içerikli varsayılan soruyu döndürür
    companion object {
        fun empty(): MathQuestion {
            return MathQuestion(
                id = -1,
                question = "",
                options = listOf("", "", "", ""),
                correctAnswerIndex = 0
            )
        }
        
        /**
         * İşlem tipine göre soru metni oluşturur.
         * @param operationType işlem tipi (1=toplama, 2=çıkarma, 3=çarpma, 4=bölme, 5=toplama-çıkarma karışık, 6=çarpma-bölme karışık)
         * @param level zorluk seviyesi (1-5)
         * @return oluşturulan soru
         */
        fun createForOperationType(operationType: Int, level: Int): MathQuestion {
            // Zorluk seviyesine göre sayı aralığını belirle
            val numberRange = when (level) {
                1 -> 1..10
                2 -> 1..20
                3 -> 1..30
                4 -> 1..50
                5 -> 1..100
                else -> 1..10
            }
            
            // İşlem tipini belirle
            val effectiveOpType = when (operationType) {
                5 -> if (Random.nextBoolean()) 1 else 2  // Toplama-Çıkarma karışık
                6 -> if (Random.nextBoolean()) 3 else 4  // Çarpma-Bölme karışık
                else -> operationType
            }
            
            // Rastgele sayıları seç
            val num1 = numberRange.random()
            val num2 = numberRange.random()
            
            // Çarpma ve bölme işlemleri için daha küçük sayılar
            val factor1 = if (effectiveOpType == 3 || effectiveOpType == 4) {
                (1..kotlin.math.min(10, numberRange.last)).random()
            } else {
                num1
            }
            
            val factor2 = if (effectiveOpType == 3 || effectiveOpType == 4) {
                (1..kotlin.math.min(10, numberRange.last)).random()
            } else {
                num2
            }
            
            // Bölme işlemi için tam sonuç olacak şekilde ayarla
            val (question, answer) = when (effectiveOpType) {
                1 -> Pair("${num1} + ${num2} = ?", (num1 + num2).toString())
                2 -> {
                    // Çıkarma işlemi için negatif sonuç olmamasını sağla
                    val larger = kotlin.math.max(num1, num2)
                    val smaller = kotlin.math.min(num1, num2)
                    Pair("${larger} - ${smaller} = ?", (larger - smaller).toString())
                }
                3 -> Pair("${factor1} × ${factor2} = ?", (factor1 * factor2).toString())
                4 -> {
                    // Bölme işlemi için tam sonuç olmasını sağla
                    val result = (1..5).random()  // Sonuç (bölüm)
                    val divisor = (1..10).random() // Bölen
                    val dividend = divisor * result // Bölünen
                    Pair("${dividend} ÷ ${divisor} = ?", result.toString())
                }
                else -> Pair("${num1} + ${num2} = ?", (num1 + num2).toString())
            }
            
            // Seçenekleri oluştur
            val options = mutableListOf<String>()
            options.add(answer)
            
            // 3 yanlış şık ekle
            while (options.size < 4) {
                val wrongAnswer = when (effectiveOpType) {
                    1 -> (num1 + num2 + (-5..5).filter { it != 0 }.random()).toString()
                    2 -> {
                        val larger = kotlin.math.max(num1, num2)
                        val smaller = kotlin.math.min(num1, num2)
                        (larger - smaller + (-5..5).filter { it != 0 }.random()).toString()
                    }
                    3 -> (factor1 * factor2 + (-10..10).filter { it != 0 }.random()).toString()
                    4 -> {
                        // Bölme için yanıltıcı cevaplar
                        val answerInt = answer.toInt()
                        (answerInt + (-3..3).filter { it != 0 }.random()).toString()
                    }
                    else -> (num1 + num2 + (-5..5).filter { it != 0 }.random()).toString()
                }
                
                // Yanlış cevap doğru cevapla aynı olmamalı
                if (!options.contains(wrongAnswer) && wrongAnswer != answer) {
                    options.add(wrongAnswer)
                }
            }
            
            // Seçenekleri karıştır
            options.shuffle()
            
            // Doğru cevabın indeksini bul
            val correctAnswerIndex = options.indexOf(answer)
            
            return MathQuestion(
                id = 0,
                question = question,
                options = options,
                correctAnswerIndex = correctAnswerIndex
            )
        }
    }
} 