package com.yigit033.matematikmacarasi.games.models

/**
 * Labirent seviyesi
 */
data class MazeLevel(
    val level: Int,          // Seviye numarası
    val size: Int,           // Labirent boyutu (n x n)
    val questionCount: Int,  // Soru sayısı
    val timeInSeconds: Int,  // Süre (saniye)
    val difficultyLevel: Int, // Zorluk seviyesi (1-5)
    val operationType: Int = 0 // İşlem türü: 0=Toplama, 1=Çıkarma, 2=Çarpma, 3=Karışık(Toplama, Çıkarma, Çarpma), 4=İleri seviye(Toplama, Çıkarma, Çarpma, Bölme), 5=Toplama+Çıkarma Karışık, 6=Çarpma+Bölme Karışık
) {
    // Seviyenin MazeLevels.levels listesindeki indeksini döndürür (0 tabanlı)
    val levelIndex: Int
        get() = level - 1
}

/**
 * Predefined labirent seviyeleri
 */
object MazeLevels {
    val levels = listOf(
        MazeLevel(
            level = 1,
            size = 9,             // 9x9 labirent
            questionCount = 2,    // 2 soru
            timeInSeconds = 120,
            difficultyLevel = 1,  // 1-10 arası sayılar
            operationType = 0     // Toplama işlemleri
        ),
        MazeLevel(
            level = 2,
            size = 9,             // 9x9 labirent
            questionCount = 2,    // 2 soru
            timeInSeconds = 120,
            difficultyLevel = 1,  // 1-10 arası sayılar
            operationType = 1     // Çıkarma işlemleri
        ),
        MazeLevel(
            level = 3,
            size = 11,            // 11x11 labirent
            questionCount = 4,    // 4 soru
            timeInSeconds = 180,
            difficultyLevel = 2,  // 1-20 arası sayılar
            operationType = 5     // Toplama-Çıkarma karışık
        ),
        MazeLevel(
            level = 4,
            size = 13,            // 13x13 labirent
            questionCount = 6,    // 6 soru
            timeInSeconds = 240,
            difficultyLevel = 1,  // 1-5 arası sayılar (çarpma için daha küçük sayılar)
            operationType = 2     // Çarpma işlemleri
        ),
        MazeLevel(
            level = 5,
            size = 13,            // 13x13 labirent
            questionCount = 6,    // 6 soru
            timeInSeconds = 240,
            difficultyLevel = 1,  // 1-5 arası sayılar (bölme için daha küçük sayılar)
            operationType = 4     // Bölme işlemleri (bu işlem tipinin bölmeye özgü kodunu generator içinde ele alacağız)
        ),
        MazeLevel(
            level = 6,
            size = 15,            // 15x15 labirent
            questionCount = 8,    // 8 soru
            timeInSeconds = 300,
            difficultyLevel = 2,  // 1-12 arası sayılar
            operationType = 6     // Çarpma-Bölme karışık (bu işlem tipinin karışık çarpma-bölme için kodunu generator içinde ele alacağız)
        ),
        MazeLevel(
            level = 7,
            size = 17,            // 17x17 labirent
            questionCount = 10,   // 10 soru
            timeInSeconds = 360,
            difficultyLevel = 4,  // Toplama/Çıkarma: 20-100, Çarpma: 1-20, Bölme: 1-50
            operationType = 4     // İleri seviye işlemler (tümü karışık)
        ),
        MazeLevel(
            level = 8,
            size = 17,            // 17x17 labirent
            questionCount = 10,   // 10 soru
            timeInSeconds = 360,
            difficultyLevel = 5,  // Toplama/Çıkarma: 50-200, Çarpma: 1-30, Bölme: 1-100
            operationType = 4     // İleri seviye işlemler (tümü karışık)
        )
    )
} 