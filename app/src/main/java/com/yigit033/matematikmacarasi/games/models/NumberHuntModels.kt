package com.yigit033.matematikmacarasi.games.models

import kotlin.random.Random
import kotlin.math.ceil
import kotlin.math.sqrt

/**
 * Sayı Avı oyunu için sayı modeli.
 * 
 * @property value Sayının değeri
 * @property isMatchingRule Sayının kurala uyup uymadığı
 * @property isSelected Kullanıcı tarafından seçilip seçilmediği
 * @property position Sayının pozisyonu (x, y)
 * @property angle Sayının rotasyon açısı (rastgele)
 */
data class NumberItem(
    val value: Int,
    val isMatchingRule: Boolean,
    var isSelected: Boolean = false,
    val position: Pair<Float, Float>,
    val angle: Float = Random.nextFloat() * 20f - 10f // -10 ile 10 derece arası rastgele
)

/**
 * Sayı Avı oyunundaki zorluk seviyesine göre kuralları ve ayarları tanımlar.
 * 
 * @property minNumber Minimum sayı
 * @property maxNumber Maksimum sayı
 * @property rule Kural açıklaması
 * @property rulePredicate Sayının kurala uygun olup olmadığını kontrol eden fonksiyon
 * @property totalNumbers Oyunda gösterilecek toplam sayı miktarı
 * @property matchingNumbersCount Kurala uygun sayı miktarı
 * @property timeInSeconds Seviye için verilen süre (saniye)
 * @property pointsPerCorrect Doğru seçim başına puan
 * @property pointsPerWrong Yanlış seçim başına ceza puanı
 */
data class NumberHuntLevel(
    val level: Int,
    val minNumber: Int,
    val maxNumber: Int,
    val rule: String,
    val rulePredicate: (Int) -> Boolean,
    val totalNumbers: Int,
    val matchingNumbersCount: Int,
    val timeInSeconds: Int,
    val pointsPerCorrect: Int,
    val pointsPerWrong: Int
) {
    // Seviyenin indeksini döndürür (0 tabanlı)
    val levelIndex: Int
        get() = level - 1
}

/**
 * Sayı Avı oyunu için zorluk seviyelerini tanımlar.
 */
object NumberHuntLevels {
    val levels = listOf(
        // Seviye 1: Basit kurallar (çift/tek sayılar, 50'den küçük), 1-20 arası sayılar
        NumberHuntLevel(
            level = 1,
            minNumber = 1,
            maxNumber = 20,
            rule = "Çift sayıları bul",
            rulePredicate = { it % 2 == 0 },
            totalNumbers = 12,
            matchingNumbersCount = 6,
            timeInSeconds = 60,
            pointsPerCorrect = 10,
            pointsPerWrong = 5
        ),
        
        // Seviye 2: Orta seviye kurallar (3'e bölünenler, 25-75 arası), 1-30 arası sayılar
        NumberHuntLevel(
            level = 2,
            minNumber = 1,
            maxNumber = 30,
            rule = "3'e bölünebilen sayıları bul",
            rulePredicate = { it % 3 == 0 },
            totalNumbers = 15,
            matchingNumbersCount = 7,
            timeInSeconds = 60,
            pointsPerCorrect = 15,
            pointsPerWrong = 8
        ),
        
        // Seviye 3: Zor kurallar (5'e bölünenler, 2 ve 3'e bölünenler, asal sayılar), 1-50 arası sayılar
        NumberHuntLevel(
            level = 3,
            minNumber = 1,
            maxNumber = 50,
            rule = "5'e bölünebilen sayıları bul",
            rulePredicate = { it % 5 == 0 },
            totalNumbers = 18,
            matchingNumbersCount = 8,
            timeInSeconds = 55,
            pointsPerCorrect = 20,
            pointsPerWrong = 10
        ),
        
        // Seviye 4: Çok zor kurallar (3 ve 5'e bölünenler, tam kare sayılar), 1-75 arası sayılar
        NumberHuntLevel(
            level = 4,
            minNumber = 1,
            maxNumber = 75,
            rule = "3'e VE 5'e bölünebilen sayıları bul",
            rulePredicate = { it % 3 == 0 && it % 5 == 0 },
            totalNumbers = 20,
            matchingNumbersCount = 6,
            timeInSeconds = 50,
            pointsPerCorrect = 30,
            pointsPerWrong = 15
        ),
        
        // Seviye 5: Uzman kurallar (karmaşık kombinasyonlar), 1-100 arası sayılar
        NumberHuntLevel(
            level = 5,
            minNumber = 1,
            maxNumber = 100,
            rule = "Asal sayıları bul",
            rulePredicate = { isPrime(it) },
            totalNumbers = 20,
            matchingNumbersCount = 5,
            timeInSeconds = 45,
            pointsPerCorrect = 40,
            pointsPerWrong = 20
        )
    )
    
    /**
     * Farklı zorluk seviyelerinde kullanılacak olan kurallı seviyeler
     */
    val levelRules = listOf(
        // Seviye 1 kuralları (Kolay): Çift/tek sayılar, 50'den küçük
        listOf(
            NumberHuntLevel(
                level = 1,
                minNumber = 1,
                maxNumber = 20,
                rule = "Çift sayıları bul",
                rulePredicate = { it % 2 == 0 },
                totalNumbers = 12,
                matchingNumbersCount = 6,
                timeInSeconds = 60,
                pointsPerCorrect = 10,
                pointsPerWrong = 5
            ),
            NumberHuntLevel(
                level = 1,
                minNumber = 1,
                maxNumber = 20,
                rule = "Tek sayıları bul",
                rulePredicate = { it % 2 != 0 },
                totalNumbers = 12,
                matchingNumbersCount = 6,
                timeInSeconds = 60,
                pointsPerCorrect = 10,
                pointsPerWrong = 5
            ),
            NumberHuntLevel(
                level = 1,
                minNumber = 1,
                maxNumber = 20,
                rule = "10'dan küçük sayıları bul",
                rulePredicate = { it < 10 },
                totalNumbers = 12,
                matchingNumbersCount = 5,
                timeInSeconds = 60,
                pointsPerCorrect = 10,
                pointsPerWrong = 5
            )
        ),
        
        // Seviye 2 kuralları (Orta): 3'e bölünenler, 25-75 arası sayılar
        listOf(
            NumberHuntLevel(
                level = 2,
                minNumber = 1,
                maxNumber = 30,
                rule = "3'e bölünebilen sayıları bul",
                rulePredicate = { it % 3 == 0 },
                totalNumbers = 15,
                matchingNumbersCount = 7,
                timeInSeconds = 60,
                pointsPerCorrect = 15,
                pointsPerWrong = 8
            ),
            NumberHuntLevel(
                level = 2,
                minNumber = 1,
                maxNumber = 30,
                rule = "25'ten büyük sayıları bul",
                rulePredicate = { it > 25 },
                totalNumbers = 15,
                matchingNumbersCount = 5,
                timeInSeconds = 60,
                pointsPerCorrect = 15,
                pointsPerWrong = 8
            )
        ),
        
        // Seviye 3 kuralları (Zor): 5'e bölünenler, 2 ve 3'e bölünenler, asal sayılar
        listOf(
            NumberHuntLevel(
                level = 3,
                minNumber = 1,
                maxNumber = 50,
                rule = "5'e bölünebilen sayıları bul",
                rulePredicate = { it % 5 == 0 },
                totalNumbers = 18,
                matchingNumbersCount = 8,
                timeInSeconds = 55,
                pointsPerCorrect = 20,
                pointsPerWrong = 10
            ),
            NumberHuntLevel(
                level = 3,
                minNumber = 1,
                maxNumber = 50,
                rule = "2'ye VE 3'e bölünebilen sayıları bul",
                rulePredicate = { it % 2 == 0 && it % 3 == 0 },
                totalNumbers = 18,
                matchingNumbersCount = 6,
                timeInSeconds = 55,
                pointsPerCorrect = 20,
                pointsPerWrong = 10
            ),
            NumberHuntLevel(
                level = 3,
                minNumber = 1,
                maxNumber = 50,
                rule = "Asal sayıları bul",
                rulePredicate = { isPrime(it) },
                totalNumbers = 18,
                matchingNumbersCount = 7,
                timeInSeconds = 55,
                pointsPerCorrect = 20,
                pointsPerWrong = 10
            )
        ),
        
        // Seviye 4 kuralları (Çok Zor): 3 ve 5'e bölünenler, tam kare sayılar
        listOf(
            NumberHuntLevel(
                level = 4,
                minNumber = 1,
                maxNumber = 75,
                rule = "3'e VE 5'e bölünebilen sayıları bul",
                rulePredicate = { it % 3 == 0 && it % 5 == 0 },
                totalNumbers = 20,
                matchingNumbersCount = 5,
                timeInSeconds = 50,
                pointsPerCorrect = 30,
                pointsPerWrong = 15
            ),
            NumberHuntLevel(
                level = 4,
                minNumber = 1,
                maxNumber = 100,
                rule = "Tam kare sayıları bul",
                rulePredicate = { sqrt(it.toDouble()) % 1 == 0.0 },
                totalNumbers = 20,
                matchingNumbersCount = 7,
                timeInSeconds = 50,
                pointsPerCorrect = 30,
                pointsPerWrong = 15
            )
        ),
        
        // Seviye 5 kuralları (Uzman): Karmaşık kombinasyonlar
        listOf(
            NumberHuntLevel(
                level = 5,
                minNumber = 1,
                maxNumber = 100,
                rule = "Asal sayıları bul",
                rulePredicate = { isPrime(it) },
                totalNumbers = 20,
                matchingNumbersCount = 5,
                timeInSeconds = 45,
                pointsPerCorrect = 40,
                pointsPerWrong = 20
            ),
            NumberHuntLevel(
                level = 5,
                minNumber = 1,
                maxNumber = 100,
                rule = "Hem 4'e hem de 7'ye bölünebilen sayıları bul",
                rulePredicate = { it % 4 == 0 && it % 7 == 0 },
                totalNumbers = 15,
                matchingNumbersCount = 3,
                timeInSeconds = 45,
                pointsPerCorrect = 40,
                pointsPerWrong = 20
            ),
            NumberHuntLevel(
                level = 5,
                minNumber = 1,
                maxNumber = 100,
                rule = "Rakamları toplamı 10 olan sayıları bul",
                rulePredicate = { 
                    val digits = it.toString().map { c -> c.toString().toInt() }
                    digits.sum() == 10
                },
                totalNumbers = 20,
                matchingNumbersCount = 7,
                timeInSeconds = 45,
                pointsPerCorrect = 40,
                pointsPerWrong = 20
            )
        )
    )
    
    /**
     * Bir sayının asal olup olmadığını kontrol eder.
     */
    private fun isPrime(n: Int): Boolean {
        if (n <= 1) return false
        if (n <= 3) return true
        
        if (n % 2 == 0 || n % 3 == 0) return false
        
        var i = 5
        while (i * i <= n) {
            if (n % i == 0 || n % (i + 2) == 0) return false
            i += 6
        }
        
        return true
    }
}

/**
 * Sayı Avı oyunu için oyun durumu modeli.
 */
data class NumberHuntGameState(
    val level: NumberHuntLevel,
    val numbers: List<NumberItem>,
    val score: Int = 0,
    val timeRemaining: Int = level.timeInSeconds,
    val foundNumbersCount: Int = 0,
    val isGameOver: Boolean = false,
    val isGameCompleted: Boolean = false,
    val stars: Int = 0
)

/**
 * Sayı Avı oyunu için yardımcı fonksiyonlar.
 */
object NumberHuntHelper {
    /**
     * Belirli bir seviye için rastgele sayılar oluşturur.
     */
    fun generateNumbersForLevel(level: NumberHuntLevel): List<NumberItem> {
        // Özel kural tipleri için özelleştirilmiş sayı üretme
        var matchingAndNonMatchingNumbers = when {
            // Asal sayılar için özel durum
            level.rule.contains("Asal") -> 
                generatePrimeNumbers(level)
            
            // Tam kare sayılar için özel durum
            level.rule.contains("Tam kare") -> 
                generatePerfectSquares(level)
            
            // 3'e VE 5'e bölünenler için özel durum
            level.rule.contains("3'e VE 5'e") -> 
                generateDivisibleByThreeAndFive(level)
            
            // 2'ye VE 3'e bölünenler için özel durum
            level.rule.contains("2'ye VE 3'e") -> 
                generateDivisibleByTwoAndThree(level)
            
            // 4'e VE 7'ye bölünenler için özel durum
            level.rule.contains("Hem 4'e hem de 7'ye") -> 
                generateDivisibleByFourAndSeven(level)
            
            // 5'e bölünenler için özel durum
            level.rule.contains("5'e bölünebilen") -> 
                generateDivisibleByFive(level)
            
            // 3'e bölünenler için özel durum
            level.rule.contains("3'e bölünebilen") -> 
                generateDivisibleByThree(level)
                
            // 25'ten büyük sayılar için özel durum
            level.rule.contains("25'ten büyük") -> 
                generateGreaterThan25(level)
                
            // 10'dan küçük sayılar için özel durum
            level.rule.contains("10'dan küçük") -> 
                generateLessThan10(level)
                
            // Çift sayılar için özel durum
            level.rule.contains("Çift sayıları") -> 
                generateEvenNumbers(level)
                
            // Tek sayılar için özel durum
            level.rule.contains("Tek sayıları") -> 
                generateOddNumbers(level)
                
            // Rakamları toplamı 10 olan sayılar için özel durum
            level.rule.contains("Rakamları toplamı 10") -> 
                generateDigitsSumTen(level)
                
            // Genel durum: Kurala göre sayı üretme
            else -> generateDefaultNumbers(level)
        }
        
        // Kurala uyan sayıların sayısını kontrol et
        var (matchingNumbers, nonMatchingNumbers) = matchingAndNonMatchingNumbers
        
        // İSTENEN SAYIDA KURALA UYAN SAYI OLDUĞUNDAN EMİN OL
        // Eğer yeterince kurala uyan sayı yoksa, birden fazla kez deneyerek üretmeyi tekrarla
        var attempts = 0
        while (matchingNumbers.size < level.matchingNumbersCount && attempts < 3) {
            attempts++
            println("UYARI: Yeterli eşleşen sayı yok. Yeniden sayı üretiliyor (Deneme: $attempts)")
            
            // Yeni bir deneme daha yap, bu kez daha geniş bir aralık kullanarak
            val extendedLevel = if (level.maxNumber < 100) {
                level.copy(
                    maxNumber = level.maxNumber * 2, // Aralığı genişlet
                    minNumber = level.minNumber  // Min değeri koru
                )
            } else {
                level // Zaten geniş bir aralık varsa değiştirme
            }
            
            // Yeniden sayı üret
            matchingAndNonMatchingNumbers = when {
                level.rule.contains("Asal") -> generatePrimeNumbers(extendedLevel)
                level.rule.contains("Tam kare") -> generatePerfectSquares(extendedLevel)
                level.rule.contains("3'e VE 5'e") -> generateDivisibleByThreeAndFive(extendedLevel)
                level.rule.contains("2'ye VE 3'e") -> generateDivisibleByTwoAndThree(extendedLevel)
                level.rule.contains("Hem 4'e hem de 7'ye") -> generateDivisibleByFourAndSeven(extendedLevel)
                level.rule.contains("5'e bölünebilen") -> generateDivisibleByFive(extendedLevel)
                level.rule.contains("3'e bölünebilen") -> generateDivisibleByThree(extendedLevel)
                level.rule.contains("25'ten büyük") -> generateGreaterThan25(extendedLevel)
                level.rule.contains("10'dan küçük") -> generateLessThan10(extendedLevel)
                level.rule.contains("Çift sayıları") -> generateEvenNumbers(extendedLevel)
                level.rule.contains("Tek sayıları") -> generateOddNumbers(extendedLevel)
                level.rule.contains("Rakamları toplamı 10") -> generateDigitsSumTen(extendedLevel)
                else -> generateDefaultNumbers(extendedLevel)
            }
            
            val (newMatching, newNonMatching) = matchingAndNonMatchingNumbers
            
            // Eğer daha fazla kurala uyan sayı bulunduysa, listeyi güncelle
            if (newMatching.size > matchingNumbers.size) {
                matchingNumbers = newMatching
                nonMatchingNumbers = newNonMatching
            }
        }
        
        // SON KONTROL: Eğer hala yeterince kurala uyan sayı yoksa, kurala uygun sayıları manuel ekle
        if (matchingNumbers.size < level.matchingNumbersCount) {
            println("UYARI: Yeterli kurala uyan sayı bulunamadı. Manuel sayılar ekleniyor.")
            
            // Rule tiplerine göre uygun sayılar ekle
            val manualNumbers = when {
                level.rule.contains("5'e bölünebilen") -> listOf(5, 10, 15, 20, 25, 30, 35, 40, 45, 50)
                level.rule.contains("3'e bölünebilen") -> listOf(3, 6, 9, 12, 15, 18, 21, 24, 27, 30)
                level.rule.contains("3'e VE 5'e") -> listOf(15, 30, 45, 60, 75, 90, 105, 120, 135, 150)
                level.rule.contains("Çift sayıları") -> listOf(2, 4, 6, 8, 10, 12, 14, 16, 18, 20)
                level.rule.contains("Tek sayıları") -> listOf(1, 3, 5, 7, 9, 11, 13, 15, 17, 19)
                level.rule.contains("Asal") -> listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29)
                level.rule.contains("Tam kare") -> listOf(1, 4, 9, 16, 25, 36, 49, 64, 81, 100)
                else -> (1..100).filter(level.rulePredicate).take(level.matchingNumbersCount)
            }
            
            // Kaç tane ek sayıya ihtiyaç var?
            val neededCount = level.matchingNumbersCount - matchingNumbers.size
            val additionalNumbers = manualNumbers
                .filterNot { it in matchingNumbers } // Zaten eklediğimiz sayıları hariç tut
                .take(neededCount)
            
            // Ek sayıları ekle
            matchingNumbers = (matchingNumbers + additionalNumbers).distinct()
        }
        
        // TAM OLARAK istenen sayıda kurala uyan sayı olduğundan emin ol
        // Fazla varsa, random seçip al
        val finalMatchingNumbers = matchingNumbers.shuffled()
            .take(level.matchingNumbersCount)
        
        // Kalan yerleri kurala uymayan sayılarla doldur
        val remainingCount = level.totalNumbers - finalMatchingNumbers.size
        val finalNonMatchingNumbers = nonMatchingNumbers.shuffled()
            .take(remainingCount.coerceAtMost(nonMatchingNumbers.size))
        
        // Yeterli kurala uymayan sayı yoksa, 1-100 arasından rastgele sayılar ekleyerek tamamla
        val finalNonMatching = if (finalNonMatchingNumbers.size < remainingCount) {
            val randomNumbers = (1..100)
                .filter { !level.rulePredicate(it) && it !in finalNonMatchingNumbers }
                .shuffled()
                .take(remainingCount - finalNonMatchingNumbers.size)
            
            (finalNonMatchingNumbers + randomNumbers).distinct()
        } else {
            finalNonMatchingNumbers
        }
        
        // Üretilen sayılardan NumberItem listesi oluştur
        val numbers = createNumberItems(finalMatchingNumbers, finalNonMatching, level)
        
        // Yeterli sayıda kurala uyan sayı var mı kontrol et
        val matchingCount = numbers.count { it.isMatchingRule }
        
        // Kurala uyan sayı sayısı istenen sayıdan az veya fazla ise, uyarı mesajı yazdır
        if (matchingCount != level.matchingNumbersCount) {
            println("UYARI: Beklenen sayıda kurala uyan sayı oluşturulamadı. İstenen: ${level.matchingNumbersCount}, Oluşturulan: $matchingCount")
            println("Kurala uyan sayılar: ${numbers.filter { it.isMatchingRule }.map { it.value }}")
        } else {
            println("BİLGİ: Kurala uyan sayılar başarıyla oluşturuldu. Sayı: $matchingCount")
            println("Kurala uyan sayılar: ${numbers.filter { it.isMatchingRule }.map { it.value }}")
        }
        
        return numbers
    }

    /**
     * Oluşturulan sayılardan NumberItem listesi üretir.
     */
    private fun createNumberItems(
        matchingNumbers: List<Int>,
        nonMatchingNumbers: List<Int>,
        level: NumberHuntLevel
    ): List<NumberItem> {
        val numbers = mutableListOf<NumberItem>()
        
        // Tüm sayıları birleştir ve karıştır
        val allNumbers = (matchingNumbers + nonMatchingNumbers).shuffled()
        
        // Eğer hiç sayı oluşturulmadıysa, hata gönder
        if (allNumbers.isEmpty()) {
            return createErrorNumbers(level)
        }
        
        // Log uyarısı - debug amaçlı
        if (matchingNumbers.size < level.matchingNumbersCount) {
            // Yeterli eşleşen sayı yok
            println("UYARI: Seviye ${level.level} için yeterli kurala uyan sayı bulunamadı. İstenen: ${level.matchingNumbersCount}, Bulunan: ${matchingNumbers.size}")
        }
        
        // Sayıları dengeli dağıtmak için grid hesaplaması
        val gridSize = ceil(sqrt(allNumbers.size.toDouble())).toInt() + 1
        
        // Izgaradaki hücrelerin boyutu
        val cellWidth = 1.0f / gridSize
        val cellHeight = 1.0f / gridSize
        
        // Her sayı için bir konum oluştur
        val usedPositions = mutableSetOf<Pair<Int, Int>>()
        
        // Sayıların ekran koordinatlarını hesapla
        val numberPositions = allNumbers.mapIndexed { index, value ->
            // Her sayı için bir grid hücresi ata
            var row: Int
            var col: Int
            var gridPos: Pair<Int, Int>
            
            // Çakışmaları önlemek için benzersiz bir pozisyon bul (maksimum 20 deneme)
            var attempts = 0
            do {
                row = Random.nextInt(gridSize)
                col = Random.nextInt(gridSize)
                gridPos = Pair(row, col)
                attempts++
                
                // 20 denemeden sonra rastgele bir pozisyon seç
                if (attempts >= 20) {
                    break
                }
            } while (gridPos in usedPositions)
            
            // Bu pozisyonu işaretle
            usedPositions.add(gridPos)
            
            // Hücre içinde rastgele bir pozisyon ile ekrandaki yerini belirle
            val cellCenterX = (col + 0.5f) * cellWidth
            val cellCenterY = (row + 0.5f) * cellHeight
            
            // Rastgele bir offset ekle ama hücre sınırları içinde kal
            val maxOffset = cellWidth * 0.3f  // Hücre genişliğinin %30'u kadar maksimum offset
            val xOffset = (Random.nextFloat() - 0.5f) * maxOffset
            val yOffset = (Random.nextFloat() - 0.5f) * maxOffset
            
            // Son koordinatları hesapla ve 0.05-0.95 aralığında tut (ekran kenarlarından uzak)
            val finalX = (cellCenterX + xOffset).coerceIn(0.05f, 0.95f)
            val finalY = (cellCenterY + yOffset).coerceIn(0.05f, 0.95f)
            
            // Sayının değeri ve konumu
            Pair(value, Pair(finalX, finalY))
        }

        // Sayıları NumberItem listesine dönüştür
        for ((value, position) in numberPositions) {
            // Sayının kurala uyup uymadığını doğrudan level.rulePredicate ile kontrol et
            val isMatching = level.rulePredicate(value)
            
            // Güvenlik kontrolü: kurala uyan sayılar listesinde mi?
            val inMatchingList = value in matchingNumbers
            
            // Yukarıdaki iki koşul farklı sonuç veriyorsa hata mesajı
            if (isMatching != inMatchingList) {
                println("UYARI: Sayı $value için kural kontrolü ($isMatching) ile liste kontrolü ($inMatchingList) uyuşmuyor!")
            }
            
            // En güvenilir yöntem: rulePredicate ile kontrol etmek
            numbers.add(
                NumberItem(
                    value = value,
                    isMatchingRule = isMatching,
                    position = position
                )
            )
        }
        
        return numbers
    }
    
    /**
     * Bir hata durumunda varsayılan sayılar döndürür.
     */
    private fun createErrorNumbers(level: NumberHuntLevel): List<NumberItem> {
        val defaultNumbers = listOf(2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37)
        val gridSize = 4
        
        return defaultNumbers.mapIndexed { index, value ->
            val row = index / gridSize
            val col = index % gridSize
            
            // Hücre içinde merkeze yakın bir pozisyon
            val cellWidth = 1.0f / gridSize
            val cellHeight = 1.0f / gridSize
            val cellCenterX = (col + 0.5f) * cellWidth
            val cellCenterY = (row + 0.5f) * cellHeight
            
            NumberItem(
                value = value,
                isMatchingRule = level.rulePredicate(value),
                position = Pair(cellCenterX, cellCenterY)
            )
        }
    }
    
    /**
     * Genel sayı üretim mekanizması
     */
    private fun generateDefaultNumbers(level: NumberHuntLevel): Pair<List<Int>, List<Int>> {
        val matchingNumbers = mutableListOf<Int>()
        val nonMatchingNumbers = mutableListOf<Int>()
        
        // Kurala uyan sayıları bul
        for (i in level.minNumber..level.maxNumber) {
            if (level.rulePredicate(i)) {
                matchingNumbers.add(i)
            } else {
                nonMatchingNumbers.add(i)
            }
        }
        
        // Hiç kurala uyan sayı yoksa, mantıklı bir aralıktan sayılar oluştur
        if (matchingNumbers.isEmpty()) {
            for (i in 1..10) {
                matchingNumbers.add(i * 10)
            }
        }
        
        // İstenilen sayıda kurala uyan sayı seç
        val selectedMatchingNumbers = matchingNumbers.shuffled()
            .take(level.matchingNumbersCount.coerceAtMost(matchingNumbers.size))
        
        // Kalan boşlukları kurala uymayan sayılarla doldur
        val remainingCount = level.totalNumbers - selectedMatchingNumbers.size
        val selectedNonMatchingNumbers = nonMatchingNumbers.shuffled()
            .take(remainingCount.coerceAtMost(nonMatchingNumbers.size))
        
        return Pair(selectedMatchingNumbers, selectedNonMatchingNumbers)
    }
    
    /**
     * Asal sayılar için özel üretim
     */
    private fun generatePrimeNumbers(level: NumberHuntLevel): Pair<List<Int>, List<Int>> {
        // 1-200 arası tüm asal sayılar - daha kapsamlı bir liste
        val allPrimes = listOf(
            2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 
            73, 79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 
            157, 163, 167, 173, 179, 181, 191, 193, 197, 199
        )
        
        // Aralıktaki asal sayıları seç
        val primes = allPrimes.filter { it in level.minNumber..level.maxNumber }
        
        // Eğer aralıkta yeterli asal sayı yoksa, güvence olarak bazı sayıları ekle
        val primesToUse = if (primes.size < level.matchingNumbersCount) {
            // Eğer sınırları aşıyorsa, en yakın asal sayıları ekle
            val extraPrimes = allPrimes.filter { it > level.maxNumber }.take(level.matchingNumbersCount - primes.size)
            (primes + extraPrimes).distinct()
        } else {
            primes
        }
        
        // Aralıktaki asal olmayan sayıları bul - 1'i hariç tut (1 asal değil)
        val nonPrimes = (level.minNumber..level.maxNumber)
            .filter { it !in primes && it != 1 } // 1'i hariç tut
            .toMutableList()
        
        // 1'i asal olmayan listesine ekle
        if (1 in level.minNumber..level.maxNumber) {
            nonPrimes.add(0, 1) // 1'i listenin başına ekle
        }
        
        // İstenen sayıda asal sayıyı seç - TAM OLARAK istenen sayıda
        val selectedPrimes = primesToUse.shuffled().take(level.matchingNumbersCount.coerceAtMost(primesToUse.size))
        
        // İstenen sayıda asal olmayan sayıyı seç
        val remainingCount = level.totalNumbers - selectedPrimes.size
        
        // Eğer asal olmayan sayılar yeterli değilse, bazı sayıları tekrarla
        val selectedNonPrimes = if (nonPrimes.size < remainingCount) {
            val repeatedNonPrimes = nonPrimes.toMutableList()
            while (repeatedNonPrimes.size < remainingCount) {
                repeatedNonPrimes.addAll(nonPrimes.shuffled().take(minOf(remainingCount - repeatedNonPrimes.size, nonPrimes.size)))
            }
            repeatedNonPrimes.take(remainingCount)
        } else {
            nonPrimes.shuffled().take(remainingCount)
        }
        
        return Pair(selectedPrimes, selectedNonPrimes)
    }
    
    /**
     * Tam kare sayılar için özel üretim
     */
    private fun generatePerfectSquares(level: NumberHuntLevel): Pair<List<Int>, List<Int>> {
        // 1-225 arası tüm tam kare sayılar (15^2)
        val allPerfectSquares = (1..15).map { it * it }
        
        // Aralıktaki tam kare sayıları seç
        val squares = allPerfectSquares.filter { it in level.minNumber..level.maxNumber }
        
        // Eğer aralıkta yeterli tam kare sayı yoksa, güvence olarak bazı sayıları ekle
        val squaresToUse = if (squares.size < level.matchingNumbersCount) {
            // Eğer sınırları aşıyorsa, en yakın tam kare sayıları ekle
            val extraSquares = allPerfectSquares.filter { it > level.maxNumber }.take(level.matchingNumbersCount - squares.size)
            (squares + extraSquares).distinct()
        } else {
            squares
        }
        
        // Aralıktaki tam kare olmayan sayıları bul
        val nonSquares = (level.minNumber..level.maxNumber).filter { it !in squares }
        
        // İstenen sayıda tam kare sayıyı seç - TAM OLARAK istenen sayıda
        val selectedSquares = squaresToUse.shuffled().take(level.matchingNumbersCount.coerceAtMost(squaresToUse.size))
        
        // İstenen sayıda tam kare olmayan sayıyı seç
        val remainingCount = level.totalNumbers - selectedSquares.size
        
        // Eğer tam kare olmayan sayılar yeterli değilse, bazı sayıları tekrarla
        val selectedNonSquares = if (nonSquares.size < remainingCount) {
            val repeatedNonSquares = nonSquares.toMutableList()
            while (repeatedNonSquares.size < remainingCount) {
                repeatedNonSquares.addAll(nonSquares.shuffled().take(minOf(remainingCount - repeatedNonSquares.size, nonSquares.size)))
            }
            repeatedNonSquares.take(remainingCount)
        } else {
            nonSquares.shuffled().take(remainingCount)
        }
        
        return Pair(selectedSquares, selectedNonSquares)
    }
    
    /**
     * 3'e ve 5'e bölünebilen sayılar için özel üretim
     */
    private fun generateDivisibleByThreeAndFive(level: NumberHuntLevel): Pair<List<Int>, List<Int>> {
        // 15'in katları: 3 ve 5'e bölünebilen sayılar
        val divisibleBy15 = (15..(level.maxNumber / 15) * 15 step 15).toList()
        
        // Aralıktaki uygun sayıları filtrele
        val matchingNumbers = divisibleBy15.filter { it in level.minNumber..level.maxNumber }
        
        // Aralıktaki uygun olmayan sayıları bul
        val nonMatchingNumbers = (level.minNumber..level.maxNumber).filter { it !in matchingNumbers }
        
        // İstenen sayıda uygun sayıyı seç
        val selectedMatching = matchingNumbers.shuffled().take(level.matchingNumbersCount.coerceAtMost(matchingNumbers.size))
        
        // İstenen sayıda uygun olmayan sayıyı seç
        val remainingCount = level.totalNumbers - selectedMatching.size
        val selectedNonMatching = nonMatchingNumbers.shuffled().take(remainingCount.coerceAtMost(nonMatchingNumbers.size))
        
        return Pair(selectedMatching, selectedNonMatching)
    }
    
    /**
     * 2'ye ve 3'e bölünebilen sayılar için özel üretim
     */
    private fun generateDivisibleByTwoAndThree(level: NumberHuntLevel): Pair<List<Int>, List<Int>> {
        // 6'nın katları: 2 ve 3'e bölünebilen sayılar
        val divisibleBy6 = (6..(level.maxNumber / 6) * 6 step 6).toList()
        
        // Aralıktaki uygun sayıları filtrele
        val matchingNumbers = divisibleBy6.filter { it in level.minNumber..level.maxNumber }
        
        // Aralıktaki uygun olmayan sayıları bul
        val nonMatchingNumbers = (level.minNumber..level.maxNumber).filter { it !in matchingNumbers }
        
        // İstenen sayıda uygun sayıyı seç
        val selectedMatching = matchingNumbers.shuffled().take(level.matchingNumbersCount.coerceAtMost(matchingNumbers.size))
        
        // İstenen sayıda uygun olmayan sayıyı seç
        val remainingCount = level.totalNumbers - selectedMatching.size
        val selectedNonMatching = nonMatchingNumbers.shuffled().take(remainingCount.coerceAtMost(nonMatchingNumbers.size))
        
        return Pair(selectedMatching, selectedNonMatching)
    }
    
    /**
     * 4'e ve 7'ye bölünebilen sayılar için özel üretim
     */
    private fun generateDivisibleByFourAndSeven(level: NumberHuntLevel): Pair<List<Int>, List<Int>> {
        // 28'in katları: 4 ve 7'e bölünebilen sayılar - Kesin liste
        val guaranteedMatching = listOf(28, 56, 84, 112, 140, 168, 196, 224, 252, 280, 308, 336, 364, 392)
            .filter { it in level.minNumber..level.maxNumber }
        
        // Manuel yöntemle bulunan sayı yeterli değilse, hesaplayarak bul
        val computedMatching = if (guaranteedMatching.size < level.matchingNumbersCount) {
            (level.minNumber..level.maxNumber).filter { 
                it % 4 == 0 && it % 7 == 0  // 4'e ve 7'ye tam bölünebilen
            }.filterNot { 
                it in guaranteedMatching  // Zaten listelediğimiz sayıları hariç tut
            }
        } else {
            emptyList()
        }
        
        // İki listeyi birleştir
        val allMatching = (guaranteedMatching + computedMatching).distinct()
        
        // Aralıktaki uygun olmayan sayıları bul - Özellikle karıştırıcı sayıları ekle
        val nonMatchingDeceptive = (level.minNumber..level.maxNumber).filter { 
            (it % 4 == 0 && it % 7 != 0) || (it % 4 != 0 && it % 7 == 0)  // Sadece birine bölünenler
        }.take(level.totalNumbers - allMatching.size)
        
        // Tamamen uygun olmayan sayılar
        val nonMatchingRegular = (level.minNumber..level.maxNumber).filter { 
            it % 4 != 0 && it % 7 != 0  // Ne 4'e ne de 7'ye bölünenler
        }
        
        // İki tip uygun olmayan sayıyı birleştir
        val allNonMatching = (nonMatchingDeceptive + nonMatchingRegular.shuffled()).distinct()
        
        // Eğer hiç eşleşen sayı yoksa, varsayılan değerler kullan
        if (allMatching.isEmpty()) {
            return Pair(listOf(28, 56, 84), allNonMatching.shuffled().take(10))
        }
        
        // İstenen sayıda uygun sayıyı seç - Bu seviye için tüm uygun sayıları göster
        val selectedMatching = allMatching.take(level.matchingNumbersCount.coerceAtMost(allMatching.size))
        
        // İstenen sayıda uygun olmayan sayıyı seç
        val remainingCount = level.totalNumbers - selectedMatching.size
        val selectedNonMatching = allNonMatching.shuffled().take(remainingCount.coerceAtMost(allNonMatching.size))
        
        return Pair(selectedMatching, selectedNonMatching)
    }
    
    /**
     * 5'e bölünebilen sayılar için özel üretim
     */
    private fun generateDivisibleByFive(level: NumberHuntLevel): Pair<List<Int>, List<Int>> {
        // 5'in katları
        val divisibleBy5 = (5..(level.maxNumber / 5) * 5 step 5).toList()
        
        // Aralıktaki uygun sayıları filtrele
        val matchingNumbers = divisibleBy5.filter { it in level.minNumber..level.maxNumber }
        
        // Aralıktaki uygun olmayan sayıları bul
        val nonMatchingNumbers = (level.minNumber..level.maxNumber).filter { it !in matchingNumbers }
        
        // İstenen sayıda uygun sayıyı seç
        val selectedMatching = matchingNumbers.shuffled().take(level.matchingNumbersCount.coerceAtMost(matchingNumbers.size))
        
        // İstenen sayıda uygun olmayan sayıyı seç
        val remainingCount = level.totalNumbers - selectedMatching.size
        val selectedNonMatching = nonMatchingNumbers.shuffled().take(remainingCount.coerceAtMost(nonMatchingNumbers.size))
        
        return Pair(selectedMatching, selectedNonMatching)
    }
    
    /**
     * 3'e bölünebilen sayılar için özel üretim
     */
    private fun generateDivisibleByThree(level: NumberHuntLevel): Pair<List<Int>, List<Int>> {
        // 3'ün katları
        val divisibleBy3 = (3..(level.maxNumber / 3) * 3 step 3).toList()
        
        // Aralıktaki uygun sayıları filtrele
        val matchingNumbers = divisibleBy3.filter { it in level.minNumber..level.maxNumber }
        
        // Aralıktaki uygun olmayan sayıları bul
        val nonMatchingNumbers = (level.minNumber..level.maxNumber).filter { it !in matchingNumbers }
        
        // İstenen sayıda uygun sayıyı seç
        val selectedMatching = matchingNumbers.shuffled().take(level.matchingNumbersCount.coerceAtMost(matchingNumbers.size))
        
        // İstenen sayıda uygun olmayan sayıyı seç
        val remainingCount = level.totalNumbers - selectedMatching.size
        val selectedNonMatching = nonMatchingNumbers.shuffled().take(remainingCount.coerceAtMost(nonMatchingNumbers.size))
        
        return Pair(selectedMatching, selectedNonMatching)
    }
    
    /**
     * 25'ten büyük sayılar için özel üretim
     */
    private fun generateGreaterThan25(level: NumberHuntLevel): Pair<List<Int>, List<Int>> {
        // 25'ten büyük sayılar
        val greaterThan25 = (26..level.maxNumber).toList()
        
        // Aralıktaki uygun sayıları filtrele
        val matchingNumbers = greaterThan25.filter { it in level.minNumber..level.maxNumber }
        
        // Aralıktaki uygun olmayan sayıları bul
        val nonMatchingNumbers = (level.minNumber..level.maxNumber).filter { it !in matchingNumbers }
        
        // İstenen sayıda uygun sayıyı seç
        val selectedMatching = matchingNumbers.shuffled().take(level.matchingNumbersCount.coerceAtMost(matchingNumbers.size))
        
        // İstenen sayıda uygun olmayan sayıyı seç
        val remainingCount = level.totalNumbers - selectedMatching.size
        val selectedNonMatching = nonMatchingNumbers.shuffled().take(remainingCount.coerceAtMost(nonMatchingNumbers.size))
        
        return Pair(selectedMatching, selectedNonMatching)
    }
    
    /**
     * 10'dan küçük sayılar için özel üretim
     */
    private fun generateLessThan10(level: NumberHuntLevel): Pair<List<Int>, List<Int>> {
        // 10'dan küçük sayılar (1-9 arası)
        val lessThan10 = (1..9).toList()
        
        // 10 ve üzeri sayılar (karıştırıcı sayılar için)
        val greaterOrEqual10 = (10..50).toList()
        
        // İstenen sayıda 10'dan küçük sayı seç
        val selectedLessThan10 = lessThan10.shuffled().take(level.matchingNumbersCount.coerceAtMost(lessThan10.size))
        
        // İstenen sayıda 10 ve üzeri sayı seç (karıştırıcı sayılar)
        val remainingCount = level.totalNumbers - selectedLessThan10.size
        val selectedGreaterOrEqual10 = greaterOrEqual10.shuffled().take(remainingCount.coerceAtMost(greaterOrEqual10.size))
        
        return Pair(selectedLessThan10, selectedGreaterOrEqual10)
    }
    
    /**
     * Çift sayılar için özel üretim
     */
    private fun generateEvenNumbers(level: NumberHuntLevel): Pair<List<Int>, List<Int>> {
        // Çift sayılar
        val evenNumbers = (level.minNumber..level.maxNumber).filter { it % 2 == 0 }
        
        // Aralıktaki tek sayıları bul
        val oddNumbers = (level.minNumber..level.maxNumber).filter { it % 2 != 0 }
        
        // İstenen sayıda çift sayı seç
        val selectedEven = evenNumbers.shuffled().take(level.matchingNumbersCount.coerceAtMost(evenNumbers.size))
        
        // İstenen sayıda tek sayı seç
        val remainingCount = level.totalNumbers - selectedEven.size
        val selectedOdd = oddNumbers.shuffled().take(remainingCount.coerceAtMost(oddNumbers.size))
        
        return Pair(selectedEven, selectedOdd)
    }
    
    /**
     * Tek sayılar için özel üretim
     */
    private fun generateOddNumbers(level: NumberHuntLevel): Pair<List<Int>, List<Int>> {
        // Tek sayılar
        val oddNumbers = (level.minNumber..level.maxNumber).filter { it % 2 != 0 }
        
        // Aralıktaki çift sayıları bul
        val evenNumbers = (level.minNumber..level.maxNumber).filter { it % 2 == 0 }
        
        // İstenen sayıda tek sayı seç
        val selectedOdd = oddNumbers.shuffled().take(level.matchingNumbersCount.coerceAtMost(oddNumbers.size))
        
        // İstenen sayıda çift sayı seç
        val remainingCount = level.totalNumbers - selectedOdd.size
        val selectedEven = evenNumbers.shuffled().take(remainingCount.coerceAtMost(evenNumbers.size))
        
        return Pair(selectedOdd, selectedEven)
    }
    
    /**
     * Rakamları toplamı 10 olan sayılar için özel üretim
     */
    private fun generateDigitsSumTen(level: NumberHuntLevel): Pair<List<Int>, List<Int>> {
        // Rakamları toplamı 10 olan kesin sayıları manuel olarak tanımla
        // Bu sayıları direk olarak ekliyoruz, hesaplama riski almıyoruz
        val exactMatches = listOf(
            19, 28, 37, 46, 55, 64, 73, 82, 91,  // İki basamaklı
            109, 118, 127, 136, 145, 154, 163, 172, 181, 190,  // Üç basamaklı başta 1
            208, 217, 226, 235, 244, 253, 262, 271, 280,  // Üç basamaklı başta 2
            307, 316, 325, 334, 343, 352, 361, 370,  // Üç basamaklı başta 3
            406, 415, 424, 433, 442, 451, 460,  // Üç basamaklı başta 4
            505, 514, 523, 532, 541, 550,  // Üç basamaklı başta 5
            604, 613, 622, 631, 640,  // Üç basamaklı başta 6
            703, 712, 721, 730,  // Üç basamaklı başta 7
            802, 811, 820,  // Üç basamaklı başta 8
            901, 910  // Üç basamaklı başta 9
        )
        
        // Aralıktaki uygun sayıları filtrele
        val availableMatches = exactMatches.filter { it in level.minNumber..level.maxNumber }
        
        // Eğer yeterli sayıda eşleşme bulunamazsa, hesaplayarak bul (yedek plan)
        val computedMatches = if (availableMatches.size < level.matchingNumbersCount) {
            (level.minNumber..level.maxNumber).filter { 
                val sum = it.toString().sumOf { c -> c.toString().toInt() }
                sum == 10
            }
        } else {
            emptyList()
        }
        
        // İki listeyi birleştir ve benzersiz yap
        val allMatching = (availableMatches + computedMatches).distinct()
        
        // Aralıktaki uygun olmayan sayıları bul
        val nonMatchingNumbers = (level.minNumber..level.maxNumber).filter { 
            val sum = it.toString().sumOf { c -> c.toString().toInt() }
            sum != 10  // Rakamları toplamı 10 olmayan tüm sayılar
        }
        
        // Eğer hiç eşleşen sayı yoksa, varsayılan değerler kullan
        if (allMatching.isEmpty()) {
            return Pair(listOf(19, 28, 37, 46, 55), nonMatchingNumbers.shuffled().take(10))
        }
        
        // İstenen sayıda uygun sayıyı seç
        val selectedMatching = allMatching.shuffled().take(level.matchingNumbersCount.coerceAtMost(allMatching.size))
        
        // İstenen sayıda uygun olmayan sayıyı seç
        val remainingCount = level.totalNumbers - selectedMatching.size
        val selectedNonMatching = nonMatchingNumbers.shuffled().take(remainingCount.coerceAtMost(nonMatchingNumbers.size))
        
        return Pair(selectedMatching, selectedNonMatching)
    }
    
    /**
     * Puanlara göre kazanılan yıldız sayısını hesaplar.
     */
    fun calculateStars(score: Int, level: NumberHuntLevel): Int {
        val maxPossibleScore = level.matchingNumbersCount * level.pointsPerCorrect
        
        return when {
            score >= maxPossibleScore * 0.9 -> 3
            score >= maxPossibleScore * 0.7 -> 2
            score > 0 -> 1
            else -> 0
        }
    }
} 