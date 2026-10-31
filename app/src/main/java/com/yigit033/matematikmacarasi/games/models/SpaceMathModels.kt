package com.yigit033.matematikmacarasi.games.models

import kotlin.random.Random

/**
 * Uzay Matematik Macerası oyunu için operasyon türlerini tanımlar.
 */
enum class SpaceOperationType {
    ADDITION,       // Toplama
    SUBTRACTION,    // Çıkarma
    MULTIPLICATION, // Çarpma
    DIVISION,       // Bölme
    MIXED           // Karışık
}

/**
 * Uzay Matematik Macerası oyunu seviye bilgilerini tanımlar.
 *
 * @property level Seviye numarası
 * @property operationType İşlem türü
 * @property minNumber Minimum sayı
 * @property maxNumber Maksimum sayı
 * @property asteroidSpeed Asteroid hızı (0.005f - 0.02f arası)
 * @property asteroidCount Ekranda aynı anda olan asteroid sayısı
 * @property questionsToComplete Seviyeyi tamamlamak için gerekli doğru cevap sayısı
 * @property timeInSeconds Seviye için verilen süre (saniye)
 */
data class SpaceLevel(
    val level: Int,
    val operationType: SpaceOperationType,
    val minNumber: Int,
    val maxNumber: Int,
    val asteroidSpeed: Float,
    val asteroidCount: Int,
    val questionsToComplete: Int,
    val timeInSeconds: Int
)

/**
 * Uzay gemisini temsil eder.
 * 
 * @property x X konumu (0-1 arası)
 * @property y Y konumu (0-1 arası)
 * @property size Boyut çarpanı
 */
data class Spaceship(
    var x: Float = 0.5f,
    var y: Float = 0.6f,
    val size: Float = 1.0f
)

/**
 * Asteroidi temsil eder.
 * 
 * @property id Benzersiz ID
 * @property value Asteroid üzerindeki değer
 * @property x X konumu (0-1 arası)
 * @property y Y konumu (0-1 arası)
 * @property speed Düşme hızı
 * @property size Boyut çarpanı
 * @property isCorrectAnswer Doğru cevap mı?
 */
data class Asteroid(
    val id: Int,
    val value: Int,
    var x: Float,
    var y: Float,
    val speed: Float,
    val size: Float = 1.0f,
    val isCorrectAnswer: Boolean = false
)

/**
 * Lazer ışınını temsil eder.
 * 
 * @property x X konumu (0-1 arası)
 * @property y Y konumu (0-1 arası)
 */
data class Laser(
    var x: Float,
    var y: Float
)

/**
 * Matematik sorusunu temsil eder.
 * 
 * @property question Soru metni
 * @property answer Doğru cevap
 * @property operationType İşlem türü
 */
data class SpaceMathQuestion(
    val question: String,
    val answer: Int,
    val operationType: SpaceOperationType
)

/**
 * Oyun durumunu temsil eder.
 * 
 * @property level Seviye
 * @property score Skor
 * @property timeRemaining Kalan süre
 * @property currentQuestion Mevcut soru
 * @property asteroids Ekrandaki asteroidler
 * @property spaceship Uzay gemisi
 * @property correctAnswers Doğru cevap sayısı
 * @property isGameOver Oyun bitti mi?
 * @property isGameCompleted Oyun tamamlandı mı?
 * @property stars Kazanılan yıldız sayısı
 */
data class SpaceGameState(
    val level: SpaceLevel,
    var score: Int = 0,
    var timeRemaining: Int = level.timeInSeconds,
    var currentQuestion: SpaceMathQuestion? = null,
    var asteroids: List<Asteroid> = emptyList(),
    var spaceship: Spaceship = Spaceship(),
    var correctAnswers: Int = 0,
    var isGameOver: Boolean = false,
    var isGameCompleted: Boolean = false,
    var stars: Int = 0
)

/**
 * Uzay Matematik Macerası oyunu için seviyeler.
 * Seviyeler, yaş gruplarına ve matematik becerilerine göre özelleştirilmiştir.
 */
object SpaceLevels {
    val levels = listOf(
        // 1. Seviye: 10 toplama sorusu (1-10 arası)
        SpaceLevel(
            level = 1,
            operationType = SpaceOperationType.ADDITION,
            minNumber = 1,
            maxNumber = 10,
            asteroidSpeed = 0.0025f,
            asteroidCount = 4,
            questionsToComplete = 10,
            timeInSeconds = 180
        ),
        
        // 2. Seviye: 10 çıkarma sorusu (1-10 arası)
        SpaceLevel(
            level = 2,
            operationType = SpaceOperationType.SUBTRACTION,
            minNumber = 1,
            maxNumber = 10,
            asteroidSpeed = 0.004f,
            asteroidCount = 5,
            questionsToComplete = 10,
            timeInSeconds = 170
        ),
        
        // 3. Seviye: 12 toplama/çıkarma sorusu (1-20 arası)
        SpaceLevel(
            level = 3,
            operationType = SpaceOperationType.MIXED, // Toplama ve çıkarma karışık
            minNumber = 1,
            maxNumber = 20,
            asteroidSpeed = 0.005f,
            asteroidCount = 6,
            questionsToComplete = 12,
            timeInSeconds = 160
        ),
        
        // 4. Seviye: 10 çarpma sorusu (1-5 arası)
        SpaceLevel(
            level = 4,
            operationType = SpaceOperationType.MULTIPLICATION,
            minNumber = 1,
            maxNumber = 5,
            asteroidSpeed = 0.006f,
            asteroidCount = 6,
            questionsToComplete = 10,
            timeInSeconds = 150
        ),
        
        // 5. Seviye: 10 bölme sorusu (1-5 arası, kalansız)
        SpaceLevel(
            level = 5,
            operationType = SpaceOperationType.DIVISION,
            minNumber = 1,
            maxNumber = 5,
            asteroidSpeed = 0.0065f,
            asteroidCount = 6,
            questionsToComplete = 10,
            timeInSeconds = 150
        ),
        
        // 6. Seviye: 12 çarpma/bölme sorusu (çarpma: 1-12, bölme: 1-12, kalansız)
        SpaceLevel(
            level = 6,
            operationType = SpaceOperationType.MIXED, // Çarpma ve bölme karışık
            minNumber = 1,
            maxNumber = 12,
            asteroidSpeed = 0.007f,
            asteroidCount = 6,
            questionsToComplete = 12,
            timeInSeconds = 140
        ),
        
        // 7. Seviye: 14 karışık soru (toplama/çıkarma: 20-100, çarpma: 1-20, bölme: 1-50, çoğunlukla kalansız)
        SpaceLevel(
            level = 7,
            operationType = SpaceOperationType.MIXED, // Tüm işlemler karışık
            minNumber = 20, // Bu değer toplama/çıkarma için kullanılacak
            maxNumber = 100, // Bu değer toplama/çıkarma için kullanılacak
            asteroidSpeed = 0.0075f,
            asteroidCount = 6, // Asteroid sayısını azalttık
            questionsToComplete = 14,
            timeInSeconds = 140
        ),
        
        // 8. Seviye: 16 karışık soru (toplama/çıkarma: 50-200, çarpma: 5-50, bölme: 1-100, kalansız & dikkatli ayarlı)
        SpaceLevel(
            level = 8,
            operationType = SpaceOperationType.MIXED, // Tüm işlemler karışık
            minNumber = 50, // Bu değer toplama/çıkarma için kullanılacak
            maxNumber = 200, // Bu değer toplama/çıkarma için kullanılacak
            asteroidSpeed = 0.008f,
            asteroidCount = 7, // Asteroid sayısını azalttık
            questionsToComplete = 16,
            timeInSeconds = 130
        )
    )
    
    /**
     * Belirtilen yaş için uygun seviye indeksini döndürür
     * @param age Oyuncunun yaşı
     * @return Uygun seviye indeksi
     */
    fun getLevelIndexForAge(age: Int): Int {
        return when {
            age <= 7 -> 0  // 6-7 yaş: 1. seviye
            age == 8 -> 1  // 7-8 yaş: 2. seviye
            age == 9 -> 2  // 8-9 yaş: 3. seviye
            age == 10 -> 3 // 9-10 yaş: 4. seviye
            age == 11 -> 4 // 10-11 yaş: 5. seviye
            age == 12 -> 5 // 11-12 yaş: 6. seviye
            age == 13 -> 6 // 12-13 yaş: 7. seviye
            else -> 7      // 13-14+ yaş: 8. seviye
        }
    }
}

/**
 * Oyun mantığı yardımcı sınıfı.
 */
object SpaceMathHelper {
    
    /**
     * Belirtilen seviye için matematik sorusu oluşturur.
     */
    fun generateQuestion(level: SpaceLevel): SpaceMathQuestion {
        // Seviyeye özel işlem seçimi
        val operationType = when {
            // 1, 2, 4, 5. seviyeler için standart işlemler
            level.operationType != SpaceOperationType.MIXED -> level.operationType
            
            // 3. seviye: Sadece toplama ve çıkarma karışık (1-20 arası)
            level.level == 3 -> {
                listOf(
                    SpaceOperationType.ADDITION,
                    SpaceOperationType.SUBTRACTION
                ).random()
            }
            
            // 6. seviye: Sadece çarpma ve bölme karışık (1-12 arası)
            level.level == 6 -> {
                listOf(
                    SpaceOperationType.MULTIPLICATION,
                    SpaceOperationType.DIVISION
                ).random()
            }
            
            // 7. seviye: Tüm işlemler karışık, eşit dağılım
            level.level == 7 -> {
                listOf(
                    SpaceOperationType.ADDITION,
                    SpaceOperationType.SUBTRACTION,
                    SpaceOperationType.MULTIPLICATION,
                    SpaceOperationType.DIVISION
                ).random()
            }
            
            // 8. seviye: Tüm işlemler karışık, eşit dağılım
            level.level == 8 -> {
                listOf(
                    SpaceOperationType.ADDITION,
                    SpaceOperationType.SUBTRACTION,
                    SpaceOperationType.MULTIPLICATION,
                    SpaceOperationType.DIVISION
                ).random()
            }
            
            // Varsayılan durum (diğer seviyeler için)
            else -> {
                listOf(
                    SpaceOperationType.ADDITION,
                    SpaceOperationType.SUBTRACTION,
                    SpaceOperationType.MULTIPLICATION,
                    SpaceOperationType.DIVISION
                ).random()
            }
        }
        
        // Seviye ve işlem türüne göre sayı aralıklarını ayarla
        val (effectiveMin, effectiveMax) = getNumberRangeForOperation(level, operationType)
        
        // Seçilen işlem türüne göre soru oluştur
        return when (operationType) {
            SpaceOperationType.ADDITION -> generateAdditionQuestion(effectiveMin, effectiveMax)
            SpaceOperationType.SUBTRACTION -> generateSubtractionQuestion(effectiveMin, effectiveMax)
            SpaceOperationType.MULTIPLICATION -> generateMultiplicationQuestion(effectiveMin, effectiveMax)
            SpaceOperationType.DIVISION -> generateDivisionQuestion(effectiveMin, effectiveMax)
            else -> generateAdditionQuestion(effectiveMin, effectiveMax) // Varsayılan durum
        }
    }
    
    /**
     * Seviye ve işlem türüne göre uygun sayı aralıklarını belirler.
     */
    private fun getNumberRangeForOperation(level: SpaceLevel, operation: SpaceOperationType): Pair<Int, Int> {
        return when {
            // 7. seviye: İşleme göre farklı aralıklar
            level.level == 7 -> {
                when (operation) {
                    // Toplama ve çıkarma için 20-100 arası
                    SpaceOperationType.ADDITION, SpaceOperationType.SUBTRACTION -> 
                        Pair(20, 100)
                    
                    // Çarpma için 1-20 arası
                    SpaceOperationType.MULTIPLICATION -> 
                        Pair(1, 20)
                    
                    // Bölme için 1-50 arası
                    SpaceOperationType.DIVISION -> 
                        Pair(1, 50)
                    
                    else -> Pair(level.minNumber, level.maxNumber)
                }
            }
            
            // 8. seviye: İşleme göre farklı aralıklar
            level.level == 8 -> {
                when (operation) {
                    // Toplama ve çıkarma için 50-200 arası
                    SpaceOperationType.ADDITION, SpaceOperationType.SUBTRACTION -> 
                        Pair(50, 200)
                    
                    // Çarpma için 5-50 arası
                    SpaceOperationType.MULTIPLICATION -> 
                        Pair(5, 50)
                    
                    // Bölme için 1-100 arası
                    SpaceOperationType.DIVISION -> 
                        Pair(1, 100)
                    
                    else -> Pair(level.minNumber, level.maxNumber)
                }
            }
            
            // Diğer seviyeler için standart aralıklar
            else -> Pair(level.minNumber, level.maxNumber)
        }
    }
    
    /**
     * Toplama sorusu oluşturur.
     */
    private fun generateAdditionQuestion(min: Int, max: Int): SpaceMathQuestion {
        try {
            // Güvenli aralıkları hesapla
            val safeMin = kotlin.math.max(1, min)
            val safeMax = kotlin.math.max(safeMin + 1, max)
            
            // Sayıları belirle
            val a = Random.nextInt(safeMin, safeMax + 1)
            val b = Random.nextInt(safeMin, safeMax + 1)
            val answer = a + b
            
            return SpaceMathQuestion(
                question = "$a + $b = ?",
                answer = answer,
                operationType = SpaceOperationType.ADDITION
            )
        } catch (e: Exception) {
            android.util.Log.e("SpaceMathHelper", "Toplama sorusu oluşturulurken hata: ${e.message}")
            
            // Hata durumunda basit bir toplama sorusu döndür
            return SpaceMathQuestion(
                question = "2 + 3 = ?",
                answer = 5,
                operationType = SpaceOperationType.ADDITION
            )
        }
    }
    
    /**
     * Çıkarma sorusu oluşturur.
     */
    private fun generateSubtractionQuestion(min: Int, max: Int): SpaceMathQuestion {
        try {
            // Güvenli aralıkları hesapla
            val safeMin = kotlin.math.max(1, min)
            val safeMax = kotlin.math.max(safeMin + 1, max)
            
            // İlk sayıyı belirle
            val a = Random.nextInt(safeMin, safeMax + 1)
            
            // İkinci sayıyı belirle (her zaman a'dan küçük veya eşit olacak şekilde)
            val b = Random.nextInt(safeMin, a + 1)
            
            // Sonucu hesapla ve pozitif olduğunu garantile
            val answer = a - b
            
            // Son kontrol
            if (answer < 0) {
                android.util.Log.e("SpaceMathHelper", "Çıkarma işleminde negatif sonuç oluştu! a=$a, b=$b, sonuç=$answer")
                // Fallback: Pozitif sonuç veren güvenli bir soru döndür
                return SpaceMathQuestion(
                    question = "5 - 2 = ?",
                    answer = 3,
                    operationType = SpaceOperationType.SUBTRACTION
                )
            }
            
            return SpaceMathQuestion(
                question = "$a - $b = ?",
                answer = answer,
                operationType = SpaceOperationType.SUBTRACTION
            )
        } catch (e: Exception) {
            android.util.Log.e("SpaceMathHelper", "Çıkarma sorusu oluşturulurken hata: ${e.message}")
            
            // Hata durumunda basit bir çıkarma sorusu döndür
            return SpaceMathQuestion(
                question = "5 - 2 = ?",
                answer = 3,
                operationType = SpaceOperationType.SUBTRACTION
            )
        }
    }
    
    /**
     * Çarpma sorusu oluşturur.
     */
    private fun generateMultiplicationQuestion(min: Int, max: Int): SpaceMathQuestion {
        try {
            // Güvenli aralıkları hesapla
            val safeMin = kotlin.math.max(1, min)
            val safeMax = kotlin.math.max(safeMin + 1, max)
            
            // Sayıları belirle
            val a = Random.nextInt(safeMin, safeMax + 1)
            val b = Random.nextInt(safeMin, safeMax + 1)
            
            // Sonucu hesapla ve kontrol et
            val answer = a * b
            
            // Sonuç çok büyük mü diye kontrol edelim
            if (answer > 1000) {
                android.util.Log.w("SpaceMathHelper", "Çarpma işleminde çok büyük sonuç oluştu, basitleştiriliyor: $a × $b = $answer")
                // Daha küçük sayılarla işlem oluştur
                return SpaceMathQuestion(
                    question = "4 × 5 = ?",
                    answer = 20,
                    operationType = SpaceOperationType.MULTIPLICATION
                )
            }
            
            return SpaceMathQuestion(
                question = "$a × $b = ?",
                answer = answer,
                operationType = SpaceOperationType.MULTIPLICATION
            )
        } catch (e: Exception) {
            android.util.Log.e("SpaceMathHelper", "Çarpma sorusu oluşturulurken hata: ${e.message}")
            
            // Hata durumunda basit bir çarpma sorusu döndür
            return SpaceMathQuestion(
                question = "4 × 5 = ?",
                answer = 20,
                operationType = SpaceOperationType.MULTIPLICATION
            )
        }
    }
    
    /**
     * Bölme sorusu oluşturur.
     * Tam bölünebilir sorular üretilir (kalansız).
     */
    private fun generateDivisionQuestion(min: Int, max: Int): SpaceMathQuestion {
        try {
            // Seviye zorluğuna göre strateji belirle
            val isAdvancedLevel = max > 50
            val isVeryAdvancedLevel = max >= 100
            
            if (isVeryAdvancedLevel) {
                // 8. seviye: Tam kalansız ve dikkatli ayarlanmış bölme
                
                // Bölen için uygun bölenleri seç (daha kolay bölünebilen sayılar)
                val easyDivisors = listOf(2, 3, 4, 5, 6, 8, 10)
                val b = easyDivisors.random()
                
                // Bölüm (cevap) için uygun aralık (cevap çok büyük olmamalı)
                val maxQuotient = kotlin.math.min(max / b, 20)
                val answer = Random.nextInt(3, maxQuotient + 1)
                
                // Bölüneni hesapla (tam bölünmesi için)
                val a = answer * b
                
                return SpaceMathQuestion(
                    question = "$a ÷ $b = ?", 
                    answer = answer,
                    operationType = SpaceOperationType.DIVISION
                )
            } else if (isAdvancedLevel) {
                // 7. seviye: Çoğunlukla kalansız bölme soruları
                
                // %90 oranında kalansız, %10 oranda kalanlı soru üret
                // (çoğunlukla kalansız gereksinimi için)
                val generateExactDivision = Random.nextDouble() < 0.9
                
                if (generateExactDivision) {
                    try {
                        // Bölen için uygun aralık (orta seviyede)
                        val minDivisor = kotlin.math.max(min, 2)
                        val maxDivisor = kotlin.math.min(10, max / 5)
                        val b = Random.nextInt(minDivisor, maxDivisor + 1)
                        
                        // Bölüm (cevap) için uygun aralık
                        val minQuotient = kotlin.math.max(2, min / b)
                        val maxQuotient = kotlin.math.min(max / b, 15)
                        
                        // Geçersiz aralık kontrolü
                        if (minQuotient > maxQuotient) {
                            throw IllegalArgumentException("Geçersiz bölüm aralığı: $minQuotient > $maxQuotient")
                        }
                        
                        val answer = Random.nextInt(minQuotient, maxQuotient + 1)
                        
                        // Bölüneni hesapla (tam bölünmesi için)
                        val a = answer * b
                        
                        return SpaceMathQuestion(
                            question = "$a ÷ $b = ?", 
                            answer = answer,
                            operationType = SpaceOperationType.DIVISION
                        )
                    } catch (e: Exception) {
                        android.util.Log.e("SpaceMathHelper", "Gelişmiş bölme sorusu oluşturulurken hata: ${e.message}")
                        
                        // Basit bir bölme sorusuna geri dön
                        val b = 2
                        val answer = 4
                        val a = answer * b
                        
                        return SpaceMathQuestion(
                            question = "$a ÷ $b = ?", 
                            answer = answer,
                            operationType = SpaceOperationType.DIVISION
                        )
                    }
                } else {
                    // Nadir olarak, makul seviyede zor bir bölme sorusu 
                    // (burada cevap hala tam sayı olmalı)
                    val b = Random.nextInt(2, 10)
                    val answer = Random.nextInt(2, 10)
                    val a = answer * b
                    
                    return SpaceMathQuestion(
                        question = "$a ÷ $b = ?", 
                        answer = answer,
                        operationType = SpaceOperationType.DIVISION
                    )
                }
            } else {
                // Temel seviye: Daha basit bölme soruları (seviye 5 ve 6)
                
                try {
                    // Bölüm (cevap) önce belirle (min 1, max 12 arası olsun)
                    val answer = if (min <= 1) 
                        Random.nextInt(1, kotlin.math.min(10, max)) 
                    else 
                        Random.nextInt(min, kotlin.math.min(12, max) + 1)
                    
                    // Bölen için uygun aralık (2-10 arası)
                    val b = Random.nextInt(2, kotlin.math.min(10, max / 2) + 1)
                    
                    // Bölüneni hesapla (tam bölünmesi için)
                    val a = answer * b
                    
                    return SpaceMathQuestion(
                        question = "$a ÷ $b = ?", 
                        answer = answer,
                        operationType = SpaceOperationType.DIVISION
                    )
                } catch (e: Exception) {
                    android.util.Log.e("SpaceMathHelper", "Temel bölme sorusu oluşturulurken hata: ${e.message}")
                    
                    // Basit bir bölme sorusuna geri dön
                    return SpaceMathQuestion(
                        question = "8 ÷ 2 = ?", 
                        answer = 4,
                        operationType = SpaceOperationType.DIVISION
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("SpaceMathHelper", "Bölme sorusu oluşturulurken hata: ${e.message}")
            
            // Hata durumunda basit bir bölme sorusu döndür
            return SpaceMathQuestion(
                question = "6 ÷ 3 = ?",
                answer = 2,
                operationType = SpaceOperationType.DIVISION
            )
        }
    }
    
    /**
     * Soru için asteroid dizisi oluşturur.
     */
    fun generateAsteroids(question: SpaceMathQuestion, level: SpaceLevel): List<Asteroid> {
        try {
            val asteroids = mutableListOf<Asteroid>()
            val correctAnswer = question.answer
            
            // Doğru cevap için asteroid oluştur
            try {
                val correctAsteroid = createAsteroid(
                    id = 0,
                    value = correctAnswer,
                    isCorrectAnswer = true,
                    speed = level.asteroidSpeed
                )
                asteroids.add(correctAsteroid)
            } catch (e: Exception) {
                android.util.Log.e("SpaceMathHelper", "Doğru cevap asteroidi oluşturulamadı: ${e.message}")
                // Basit bir asteroid oluşturalım
                asteroids.add(Asteroid(
                    id = 0,
                    value = correctAnswer,
                    x = 0.5f,
                    y = -0.1f,
                    speed = level.asteroidSpeed,
                    isCorrectAnswer = true
                ))
            }
            
            // Yanlış cevaplar için asteroid oluştur
            val usedValues = mutableSetOf(correctAnswer)
            
            // Seviyeye göre asteroid sayısını belirle (sınırlı tutuyoruz)
            val actualAsteroidCount = kotlin.math.min(level.asteroidCount, level.level + 3) // Seviyeye bağlı olarak sınırla
            
            // Her bir yanlış asteroid için
            for (i in 1 until actualAsteroidCount) {
                // Doğru cevaba yakın ama farklı değerler üret
                var wrongValue: Int
                var attempts = 0
                
                // Seviye zorluğuna göre yanıltıcı değerler üret
                // Sonsuz döngüye girmemek için maksimum deneme sayısı
                val maxAttempts = 10
                
                do {
                    attempts++
                    
                    try {
                        if (level.level >= 7) {
                            // Üst seviyeler için daha sofistike yanıltıcı cevaplar
                            if (question.operationType == SpaceOperationType.DIVISION) {
                                wrongValue = generateDivisionWrongAnswer(correctAnswer)
                            } else if (question.operationType == SpaceOperationType.MULTIPLICATION) {
                                wrongValue = generateMultiplicationWrongAnswer(correctAnswer, question.question)
                            } else {
                                // Toplama ve çıkarma için yanıltıcı değerler
                                val offsetRange = when {
                                    correctAnswer > 100 -> -10..10
                                    correctAnswer > 50 -> -5..5
                                    else -> -3..3
                                }
                                
                                val offset = offsetRange.filter { it != 0 }.random()
                                wrongValue = correctAnswer + offset
                            }
                        } else {
                            // Alt seviyeler için daha basit yanıltıcı değerler
                            val offsetRange = when {
                                level.level <= 2 -> -2..2 // Çok kolay seviyeler
                                level.level <= 4 -> -3..3 // Kolay seviyeler 
                                else -> -5..5 // Orta seviyeler
                            }
                            
                            val offset = offsetRange.filter { it != 0 }.random()
                            wrongValue = correctAnswer + offset
                        }
                        
                        // Negatif değerleri engelle (küçük çocuklar için)
                        if (wrongValue < 0) {
                            wrongValue = Random.nextInt(1, correctAnswer * 2 + 1)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SpaceMathHelper", "Yanlış cevap değeri üretilirken hata: ${e.message}")
                        // Basit bir alternatif değer üret
                        wrongValue = correctAnswer + i
                        
                        // Negatif olmadığından emin ol
                        if (wrongValue < 1) wrongValue = i + 1
                    }
                    
                    // Maksimum deneme sayısına ulaşıldıysa çık
                    if (attempts >= maxAttempts) {
                        // Benzersiz bir değer üretemediysek, basit bir değer ekle
                        wrongValue = correctAnswer + i * 10
                        break
                    }
                } while (wrongValue == correctAnswer || usedValues.contains(wrongValue))
                
                usedValues.add(wrongValue)
                
                try {
                    // Asteroid oluştur
                    val asteroid = createAsteroid(
                        id = i,
                        value = wrongValue,
                        isCorrectAnswer = false,
                        speed = level.asteroidSpeed
                    )
                    asteroids.add(asteroid)
                } catch (e: Exception) {
                    android.util.Log.e("SpaceMathHelper", "Yanlış cevap için asteroid oluşturulurken hata: ${e.message}")
                    // Basit bir asteroid ekle
                    asteroids.add(Asteroid(
                        id = i,
                        value = wrongValue,
                        x = 0.3f + (i * 0.1f),
                        y = -0.2f - (i * 0.05f),
                        speed = level.asteroidSpeed,
                        isCorrectAnswer = false
                    ))
                }
            }
            
            return asteroids
        } catch (e: Exception) {
            android.util.Log.e("SpaceMathHelper", "Asteroid dizisi oluşturulurken genel hata: ${e.message}")
            
            // Hata durumunda basit bir fallback asteroid dizisi döndür
            val result = mutableListOf<Asteroid>()
            
            // Doğru cevap için basit asteroid
            result.add(Asteroid(
                id = 0,
                value = question.answer,
                x = 0.5f,
                y = -0.1f,
                speed = 0.005f,
                isCorrectAnswer = true
            ))
            
            // Birkaç basit yanlış cevap
            for (i in 1..3) {
                val wrongValue = question.answer + i
                result.add(Asteroid(
                    id = i,
                    value = wrongValue,
                    x = 0.3f + (i * 0.2f),
                    y = -0.1f - (i * 0.1f),
                    speed = 0.005f,
                    isCorrectAnswer = false
                ))
            }
            
            return result
        }
    }
    
    /**
     * Bölme işlemleri için yanıltıcı cevap üretir
     */
    private fun generateDivisionWrongAnswer(correctAnswer: Int): Int {
        val options = mutableListOf<Int>()
        
        // Doğru cevabın yakınında değerler
        options.add(correctAnswer + 1)
        options.add(correctAnswer - 1)
        
        // Yaygın hatalar (örn. yer değiştirme, eksik/fazla basamak)
        if (correctAnswer >= 10) {
            val digits = correctAnswer.toString().toCharArray()
            if (digits.size >= 2) {
                val swapped = "${digits[1]}${digits[0]}".toInt()
                options.add(swapped)
            }
        }
        
        return options.filter { it > 0 && it != correctAnswer }.randomOrNull() ?: (correctAnswer + 2)
    }
    
    /**
     * Çarpma işlemleri için yanıltıcı cevap üretir
     */
    private fun generateMultiplicationWrongAnswer(correctAnswer: Int, questionText: String): Int {
        val options = mutableListOf<Int>()
        
        try {
            // Sorudaki sayıları ayıkla
            val numbers = questionText.split("×").map { it.trim().replace("=", "").replace("?", "").trim().toIntOrNull() }
                                  .filterNotNull()
            
            if (numbers.size == 2) {
                // Yaygın çarpım hataları
                options.add(numbers[0] + numbers[1]) // Toplamıyla karıştırma hatası
                options.add(correctAnswer + numbers[0]) // Bir faktör kadar fazla
                options.add(correctAnswer - numbers[0]) // Bir faktör kadar eksik
            }
        } catch (e: Exception) {
            android.util.Log.e("SpaceMathHelper", "Çarpma sorusu için yanıltıcı değer oluşturulurken hata: ${e.message}")
        }
        
        // Yakın değerler de ekle
        options.add(correctAnswer + 1)
        options.add(correctAnswer - 1)
        options.add(correctAnswer + 2)
        
        return options.filter { it > 0 && it != correctAnswer }.randomOrNull() ?: (correctAnswer + 5)
    }
    
    /**
     * Rastgele konumda bir asteroid oluşturur.
     */
    private fun createAsteroid(
        id: Int,
        value: Int,
        isCorrectAnswer: Boolean,
        speed: Float
    ): Asteroid {
        // Ekranın üst kısmında rastgele bir X konumu
        val x = Random.nextFloat() * 0.8f + 0.1f // 0.1 ile 0.9 arası
        
        // Ekranın dışında, üstte başlat
        val y = -0.1f - (Random.nextFloat() * 0.2f) // -0.1 ile -0.3 arası
        
        // Boyut çarpanı (değerlere göre değişebilir)
        val size = 1.0f + (Random.nextFloat() * 0.5f - 0.25f) // 0.75 ile 1.25 arası
        
        return Asteroid(
            id = id,
            value = value,
            x = x,
            y = y,
            speed = speed * (0.8f + Random.nextFloat() * 0.4f), // 0.8x ile 1.2x arası hız varyasyonu
            size = size,
            isCorrectAnswer = isCorrectAnswer
        )
    }
    
    /**
     * Lazer ve asteroid çarpışmasını kontrol eder.
     */
    fun checkLaserAsteroidCollision(laser: Laser, asteroid: Asteroid): Boolean {
        // Basit bir dairesel çarpışma kontrolü
        val asteroidRadius = 0.05f * asteroid.size // 0.05 birim varsayılan asteroid yarıçapı
        val laserRadius = 0.01f // 0.01 birim lazer yarıçapı
        
        val dx = laser.x - asteroid.x
        val dy = laser.y - asteroid.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        
        return distance < (asteroidRadius + laserRadius)
    }
    
    /**
     * Yıldız sayısını hesaplar.
     */
    fun calculateStars(gameState: SpaceGameState): Int {
        // Doğru cevap oranı hesaplanır
        val totalAnswers = gameState.correctAnswers + (gameState.level.questionsToComplete - gameState.correctAnswers)
        val correctAnswerPercentage = (gameState.correctAnswers.toFloat() / gameState.level.questionsToComplete) * 100
        
        // Doğru cevap oranına göre yıldız sayısı belirlenir
        return when {
            correctAnswerPercentage >= 80f -> 3  // %80 ve üzeri doğru: 3 yıldız 
            correctAnswerPercentage >= 50f -> 2   // %50 ve üzeri doğru: 2 yıldız
            else -> 1                            // Diğer durumlar: 1 yıldız
        }
    }
} 