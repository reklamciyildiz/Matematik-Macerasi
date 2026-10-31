package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.models.GameProgress
import com.yigit033.matematikmacarasi.repositories.LocalGameDataRepository
import kotlin.math.max

/**
 * GameStatisticsService, oyun istatistiklerini toplar ve analiz eder.
 * Profil sayfasında gösterilecek dinamik istatistikleri sağlar.
 */
class GameStatisticsService(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "game_progress"
        private const val TOTAL_PLAY_TIME_KEY = "total_play_time" // Dakika cinsinden
        private const val GAMES_COMPLETED_KEY = "games_completed"
        private const val FAVORITE_GAME_KEY = "favorite_game"
        private const val TAG = "GameStatisticsService"
        
        // Singleton instance
        @Volatile
        private var instance: GameStatisticsService? = null
        
        // Singleton pattern - thread-safe instance oluşturma
        fun getInstance(context: Context): GameStatisticsService {
            return instance ?: synchronized(this) {
                instance ?: GameStatisticsService(context).also { 
                    instance = it
                }
            }
        }
        
        // Oyun kimlikleri
        private val GAME_IDS = listOf(
            "AdditionGameActivity", "SubtractionGameActivity", 
            "MultiplicationGameActivity", "DivisionGameActivity",
            "BalloonPopGameActivity", "NumberHuntGameActivity", 
            "MathMazeGameActivity", "SpaceMathAdventureActivity",
            "ShapeRecognitionGameActivity", "MarketSimulatorActivity"
        )
        
        // Oyun isim eşleşmeleri
        private val GAME_NAMES = mapOf(
            "AdditionGameActivity" to "Toplama Oyunu",
            "SubtractionGameActivity" to "Çıkarma Oyunu",
            "MultiplicationGameActivity" to "Çarpma Oyunu",
            "DivisionGameActivity" to "Bölme Oyunu",
            "BalloonPopGameActivity" to "Balon Patlatma",
            "NumberHuntGameActivity" to "Sayı Avı",
            "MathMazeGameActivity" to "Matematik Labirenti",
            "SpaceMathAdventureActivity" to "Uzay Matematik Macerası",
            "ShapeRecognitionGameActivity" to "Şekil Tanıma",
            "MarketSimulatorActivity" to "Market Simülasyonu"
        )
        
        // İstatistik hesaplamalarında kullanılacak geçerli oyun ID'leri listesi
        private val VALID_GAME_IDS = GAME_NAMES.keys.toList()
    }
    
    // SharedPreferences
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gameDataRepository = LocalGameDataRepository(context)
    
    /**
     * Toplam puan hesapla - tüm oyunların en yüksek skorlarının toplamı
     */
    fun getTotalScore(): Int {
        var totalScore = 0
        try {
            for (gameId in VALID_GAME_IDS) {
                val progress = gameDataRepository.getProgress(gameId)
                totalScore += progress.highScore
            }
        } catch (e: Exception) {
            Log.e(TAG, "Toplam skor hesaplanırken hata", e)
        }
        return totalScore
    }
    
    /**
     * Toplam tamamlanan oyun sayısını hesapla
     */
    fun getCompletedGamesCount(): Int {
        return try {
            // Tüm oyunların oynanma sayılarını topla
            var totalCount = 0
            for (gameId in VALID_GAME_IDS) {
                totalCount += getGamePlayCount(gameId)
            }
            totalCount
        } catch (e: Exception) {
            Log.e(TAG, "Tamamlanan oyun sayısı alınırken hata", e)
            0
        }
    }
    
    /**
     * Oyun tamamlandığında çağırılır (oyun türü olmadan)
     * Bu metod, GameType gerektirmeyen oyunlarda toplam oynanan oyun sayısını artırmak için kullanılır
     */
    fun incrementCompletedGames() {
        try {
            val currentTotalCount = getCompletedGamesCount()
            prefs.edit().putInt(GAMES_COMPLETED_KEY, currentTotalCount + 1).apply()
            Log.d(TAG, "Toplam tamamlanan oyun sayısı arttırıldı: ${currentTotalCount + 1}")
        } catch (e: Exception) {
            Log.e(TAG, "Oyun tamamlama istatistikleri güncellenirken hata", e)
        }
    }
    
    /**
     * Oyun tamamlandığında çağırılır (oyun türü ile)
     * Bu metod, belirli bir oyun türü için oyun sayısını artırmak için kullanılır
     */
    fun incrementCompletedGames(gameType: GameType) {
        try {
            val currentTotalCount = getCompletedGamesCount()
            prefs.edit().putInt(GAMES_COMPLETED_KEY, currentTotalCount + 1).apply()
            Log.d(TAG, "Toplam tamamlanan oyun sayısı arttırıldı: ${currentTotalCount + 1}")
            
            val gameId = getGameIdString(gameType)
            incrementGamePlayCount(gameId)
            Log.d(TAG, "Oyun oynandı olarak işaretlendi: $gameId")
            
            // Yeni eklenen metot çağrıları
            markGamePlayed(gameId)
            updateLastPlayedTime(gameId)
            
            Log.d(TAG, "$gameId ($gameType) oyunu tamamlandı, oynanma sayısı arttırıldı.")
        } catch (e: Exception) {
            Log.e(TAG, "Oyun tamamlama istatistikleri güncellenirken hata", e)
        }
    }
    
    /**
     * GameType enum'unu SharedPreferences'da kullanılan ID string'ine çevirir
     */
    internal fun getGameIdString(gameType: GameType): String {
        return when (gameType) {
            GameType.ADDITION_GAME -> "AdditionGameActivity"
            GameType.SUBTRACTION_GAME -> "SubtractionGameActivity"
            GameType.MULTIPLICATION_GAME -> "MultiplicationGameActivity"
            GameType.DIVISION_GAME -> "DivisionGameActivity"
            GameType.BALLOON_POP_GAME -> "BalloonPopGameActivity"
            GameType.NUMBER_HUNT_GAME -> "NumberHuntGameActivity"
            GameType.MATH_MAZE_GAME -> "MathMazeGameActivity"
            GameType.SPACE_MATH_ADVENTURE -> "SpaceMathAdventureActivity"
            GameType.SHAPE_RECOGNITION -> "ShapeRecognitionGameActivity"
            GameType.MARKET_SIMULATOR -> "MarketSimulatorActivity"
        }
    }
    
    /**
     * Belirli bir oyunun oynanma sayısını artırır
     */
    fun incrementGamePlayCount(gameId: String) {
        if (!VALID_GAME_IDS.contains(gameId)) {
            Log.w(TAG, "Geçersiz gameId için oynanma sayısı artırma denemesi: $gameId")
            return
        }
        try {
            val currentCount = prefs.getInt("game_play_count_$gameId", 0)
            prefs.edit().putInt("game_play_count_$gameId", currentCount + 1).apply()
            Log.d(TAG, "$gameId oynanma sayısı: ${currentCount + 1}")
        } catch (e: Exception) {
            Log.e(TAG, "$gameId oynanma sayısı artırılırken hata", e)
        }
    }
    
    /**
     * Belirli bir oyunun oynanma sayısını getirir
     */
    private fun getGamePlayCount(gameId: String): Int {
        return try {
            // Önce doğrudan ID ile kontrol et
            val directCount = prefs.getInt("game_play_count_$gameId", 0)
            if (directCount > 0) {
                return directCount
            }
            
            // Temel işlem oyunlarını kontrol et (addition_game -> addition_game_level_1, addition_game_level_2, ...)
            val basicGamePrefixes = listOf(
                "addition_game", "subtraction_game", "multiplication_game", "division_game"
            )
            
            for (prefix in basicGamePrefixes) {
                if (gameId == "AdditionGameActivity" && prefix == "addition_game" ||
                    gameId == "SubtractionGameActivity" && prefix == "subtraction_game" ||
                    gameId == "MultiplicationGameActivity" && prefix == "multiplication_game" ||
                    gameId == "DivisionGameActivity" && prefix == "division_game") {
                    
                    // Seviye bazlı ID'leri topla
                    var totalCount = 0
                    for (level in 1..5) {
                        val levelGameId = "${prefix}_$level"
                        totalCount += prefs.getInt("game_play_count_$levelGameId", 0)
                    }
                    
                    if (totalCount > 0) {
                        Log.d(TAG, "Temel işlem oyunu toplam sayım: $gameId ($prefix) = $totalCount kez")
                        return totalCount
                    }
                }
            }
            
            // İnteraktif oyunlar için seviye bazlı ID kontrolü
            if (gameId.endsWith("Activity")) {
                var totalCount = 0
                for (level in 1..5) {
                    val levelFormat = "${gameId}_level_$level"
                    totalCount += prefs.getInt("game_play_count_$levelFormat", 0)
                }
                
                if (totalCount > 0) {
                    Log.d(TAG, "İnteraktif oyun seviye toplam sayım: $gameId = $totalCount kez")
                    return totalCount
                }
            }
            
            // Hiçbir şey bulunamadıysa 0 döndür
            0
        } catch (e: Exception) {
            Log.e(TAG, "$gameId oynanma sayısı alınırken hata", e)
            0
        }
    }
    
    /**
     * Toplam oyun süresini dakika cinsinden getirir
     */
    fun getTotalPlayTimeMinutes(): Int {
        return try {
            // Tüm oyunların süresini saniye cinsinden topla ve dakikaya çevir
            var totalSeconds = 0
            for (gameId in VALID_GAME_IDS) {
                totalSeconds += prefs.getInt("game_time_$gameId", 0)
            }
            // En az 1 dakika göster, eğer saniye varsa
            if (totalSeconds > 0) {
                max(1, totalSeconds / 60)
            } else {
                0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Toplam oyun süresi alınırken hata", e)
            0
        }
    }
    
    /**
     * Oyun süresini dakika cinsinden ekler
     */
    fun addPlayTime(minutes: Int) {
        if (minutes <= 0) return
        try {
            val currentTime = getTotalPlayTimeMinutes()
            prefs.edit().putInt(TOTAL_PLAY_TIME_KEY, currentTime + minutes).apply()
            Log.d(TAG, "Oyun süresi eklendi: $minutes dakika. Toplam: ${currentTime + minutes} dakika.")
        } catch (e: Exception) {
            Log.e(TAG, "Oyun süresi eklenirken hata", e)
        }
    }
    
    /**
     * Toplam yıldız sayısını getirir
     */
    fun getTotalStars(): Int {
        return try {
            // Tüm oyunlardan kazanılan toplam yıldız sayısını hesapla
            var totalStars = 0
            
            // 1. Önce temel işlem oyunlarını seviye bazlı olarak topla
            val basicGamePrefixes = listOf(
                "addition_game",
                "subtraction_game",
                "multiplication_game", 
                "division_game"
            )
            
            // Her temel oyun için, her seviyeden kazanılan yıldızları topla
            for (prefix in basicGamePrefixes) {
                for (level in 1..5) {
                    // Her seviye için tek bir yıldız sayısı hesapla
                    var starsForThisLevel = 0
                    
                    // Öncelikle yeni ID formatını kontrol et
                    val levelGameId = "${prefix}_level_$level"
                    
                    try {
                        val earnedStars = gameDataRepository.getProgress(levelGameId).stars
                        if (earnedStars > 0) {
                            starsForThisLevel = earnedStars
                            Log.d(TAG, "Temel oyun kazanılan yıldız: $levelGameId: $earnedStars")
                        }
                    } catch (e: Exception) {
                        // İlk format bulunamadı, alternatif formatı dene
                        try {
                            // Alternatif format (BaseGameActivity.getNormalizedGameId'den)
                            val altLevelGameId = "${prefix}_$level"
                            val earnedStars = gameDataRepository.getProgress(altLevelGameId).stars
                            if (earnedStars > 0) {
                                starsForThisLevel = earnedStars
                                Log.d(TAG, "Temel oyun kazanılan yıldız (alt format): $altLevelGameId: $earnedStars")
                            }
                        } catch (e2: Exception) {
                            // Bu seviye için hiçbir kayıt bulunamadı - sessizce devam et
                            Log.d(TAG, "Temel oyun için yıldız bulunamadı: ${prefix}_level_$level veya ${prefix}_$level")
                        }
                    }
                    
                    // Bu seviye için hesaplanan yıldızları toplam yıldızlara ekle
                    totalStars += starsForThisLevel
                }
            }
            
            // 2. İnteraktif oyunlar için ana ID'leri ve seviye bazlı ID'leri kontrol et
            val interactiveGameIds = listOf(
                "BalloonPopGameActivity",
                "NumberHuntGameActivity", 
                "MathMazeGameActivity", 
                "SpaceMathAdventureActivity",
                "ShapeRecognitionGameActivity", 
                "MarketSimulatorActivity"
            )
            
            // İnteraktif oyunların hem temel ID'lerini hem de seviye bazlı ID'lerini kontrol et
            for (gameId in interactiveGameIds) {
                // Ana oyun ID'sini kontrol et
                var earnedStars = gameDataRepository.getProgress(gameId).stars
                totalStars += earnedStars
                if (earnedStars > 0) {
                    Log.d(TAG, "İnteraktif oyun kazanılan yıldız (Ana ID): $gameId: $earnedStars")
                }
                
                // Sayı Avı için özel kontrol - her seviye için en fazla 3 yıldız
                if (gameId == "NumberHuntGameActivity") {
                    // Her seviye için ayrı ayrı kontrol et, ama her seviyeden en fazla 3 yıldız
                    for (level in 1..8) { // Maksimum 8 seviye olabilir
                        var maxStarsForLevel = 0
                        
                        // Seviye formatlarını kontrol et
                        val levelFormat = "${gameId}_level_$level"
                        try {
                            // Repository'den ilerlemeyi al
                            val progress = gameDataRepository.getProgress(levelFormat)
                            if (progress.stars > maxStarsForLevel) {
                                maxStarsForLevel = progress.stars
                            }
                        } catch (e: Exception) {
                            // Bu seviye için kayıt yok, devam et
                        }
                        
                        // Bu seviyenin en yüksek yıldızını toplama ekle (her seviye için max 3)
                        if (maxStarsForLevel > 0) {
                            totalStars += maxStarsForLevel
                            Log.d(TAG, "Sayı Avı seviye $level için en yüksek yıldız: $maxStarsForLevel")
                        }
                    }
                } else {
                    // Diğer interaktif oyunlar için seviye bazlı ID'leri kontrol et
                    for (level in 1..8) { // Maksimum 8 seviye olabilir
                        // Farklı seviye ID formatlarını kontrol et
                        val formats = listOf(
                            "${gameId}_Level_$level", // Büyük L formatı 
                            "${gameId}_level_$level"  // Küçük l formatı
                        )
                        
                        for (levelFormat in formats) {
                            try {
                                val progress = gameDataRepository.getProgress(levelFormat)
                                if (progress.stars > 0) {
                                    totalStars += progress.stars
                                    Log.d(TAG, "İnteraktif oyun kazanılan yıldız (Seviye): $levelFormat: ${progress.stars}")
                                }
                            } catch (e: Exception) {
                                // Bu format için yıldız bulunamadı - sessizce devam et
                            }
                        }
                    }
                }
            }
            
            Log.d(TAG, "Toplam kazanılan yıldızlar: $totalStars")
            return totalStars
        } catch (e: Exception) {
            Log.e(TAG, "Toplam kazanılan yıldız alınırken hata", e)
            0
        }
    }
    
    /**
     * Kazanılan toplam yıldız sayısını getirir
     */
    fun getTotalEarnedStars(): Int {
        return try {
            // Tüm oyunlardan kazanılan toplam yıldız sayısını hesapla
            var totalStars = 0
            
            // Detaylı log için
            val logBuilder = StringBuilder("Temel Oyun Yıldızları: ")
            
            // 1. Önce temel işlem oyunlarını seviye bazlı olarak topla
            val basicGamePrefixes = listOf(
                "addition_game",
                "subtraction_game",
                "multiplication_game", 
                "division_game"
            )
            
            // Her temel oyun için, her seviyeden kazanılan yıldızları topla
            Log.d(TAG, "YILDIZ SORUN TESPITI - Temel işlem oyunları yıldızlarını kontrol ediyorum")
            for (prefix in basicGamePrefixes) {
                Log.d(TAG, "YILDIZ SORUN TESPITI - $prefix oyunu için yıldızları kontrol ediyorum")
                for (level in 1..5) {
                    // Seviye başına yıldız sayısını takip et - bir seviye için tek bir yıldız sayısı olmalı
                    var starsForThisLevel = 0
                    
                    // FORMAT 1: prefix_level_X (Örnek: addition_game_level_1)
                    val levelGameId = "${prefix}_level_$level"
                    
                    try {
                        val progress = gameDataRepository.getProgress(levelGameId)
                        val earnedStars = progress.stars
                        if (earnedStars > 0) {
                            starsForThisLevel = earnedStars
                            logBuilder.append("[$levelGameId: $earnedStars] ")
                            Log.d(TAG, "Temel oyun kazanılan yıldız (format1): $levelGameId: $earnedStars")
                        }
                    } catch (e: Exception) {
                        // Format 1 bulunamadı, sessizce devam et ve Format 2'yi dene
                        Log.d(TAG, "YILDIZ SORUN TESPITI - Format1 bulunamadı: $levelGameId - ${e.message}")
                    }
                    
                    // Eğer Format 1'de yıldız bulunamadıysa Format 2'yi kontrol et
                    if (starsForThisLevel == 0) {
                        // FORMAT 2: prefix_X (Örnek: addition_game_1) - eski format
                        val altLevelGameId = "${prefix}_$level"
                        try {
                            val progress = gameDataRepository.getProgress(altLevelGameId)
                            val earnedStars = progress.stars
                            if (earnedStars > 0) {
                                starsForThisLevel = earnedStars
                                logBuilder.append("[$altLevelGameId: $earnedStars] ")
                                Log.d(TAG, "Temel oyun kazanılan yıldız (format2): $altLevelGameId: $earnedStars")
                            }
                        } catch (e: Exception) {
                            // Format 2 de bulunamadı, sessizce devam et
                            Log.d(TAG, "YILDIZ SORUN TESPITI - Format2 bulunamadı: $altLevelGameId - ${e.message}")
                        }
                        
                        // Hala yıldız bulunamadıysa StarManager'ı kontrol et
                        if (starsForThisLevel == 0) {
                            try {
                                val starManager = StarManager.getInstance(context)
                                
                                // Format 1 kontrolü (addition_game_level_1)
                                val smStars1 = starManager.getGameStars(levelGameId)
                                if (smStars1 > 0) {
                                    starsForThisLevel = smStars1
                                    logBuilder.append("[SM-$levelGameId: $smStars1] ")
                                    Log.d(TAG, "StarManager'dan temel oyun yıldızı (format1): $levelGameId: $smStars1")
                                } else {
                                    // Format 2 kontrolü (addition_game_1)
                                    val smStars2 = starManager.getGameStars(altLevelGameId)
                                    if (smStars2 > 0) {
                                        starsForThisLevel = smStars2
                                        logBuilder.append("[SM-$altLevelGameId: $smStars2] ")
                                        Log.d(TAG, "StarManager'dan temel oyun yıldızı (format2): $altLevelGameId: $smStars2")
                                    }
                                }
                            } catch (e: Exception) {
                                // StarManager kontrolü başarısız oldu, sessizce devam et
                                Log.w(TAG, "StarManager'dan yıldız kontrol hatası: ${e.message}")
                                Log.d(TAG, "YILDIZ SORUN TESPITI - StarManager kontrolü başarısız: ${e.message}")
                            }
                        }
                    }
                    
                    // Bu seviye için hesaplanan yıldızları toplam yıldızlara ekle
                    totalStars += starsForThisLevel
                    if (starsForThisLevel > 0) {
                        Log.d(TAG, "YILDIZ SORUN TESPITI - Seviye $level için toplamda $starsForThisLevel yıldız eklendi")
                    }
                }
            }
            
            Log.d(TAG, logBuilder.toString())
            
            // 2. İnteraktif oyunlar için ana ID'leri ve seviye bazlı ID'leri kontrol et
            val interactiveGameIds = listOf(
                "BalloonPopGameActivity",
                "NumberHuntGameActivity", 
                "MathMazeGameActivity", 
                "SpaceMathAdventureActivity",
                "ShapeRecognitionGameActivity", 
                "MarketSimulatorActivity"
            )
            
            // İnteraktif oyunların hem temel ID'lerini hem de seviye bazlı ID'lerini kontrol et
            for (gameId in interactiveGameIds) {
                // Ana oyun ID'sini kontrol et
                var earnedStars = gameDataRepository.getProgress(gameId).stars
                totalStars += earnedStars
                if (earnedStars > 0) {
                    Log.d(TAG, "İnteraktif oyun kazanılan yıldız (Ana ID): $gameId: $earnedStars")
                }
                
                // Sayı Avı için özel kontrol - her seviye için en fazla 3 yıldız
                if (gameId == "NumberHuntGameActivity") {
                    // Her seviye için ayrı ayrı kontrol et, ama her seviyeden en fazla 3 yıldız
                    for (level in 1..8) { // Maksimum 8 seviye olabilir
                        var maxStarsForLevel = 0
                        
                        // Seviye formatlarını kontrol et
                        val levelFormat = "${gameId}_level_$level"
                        try {
                            // Repository'den ilerlemeyi al
                            val progress = gameDataRepository.getProgress(levelFormat)
                            if (progress.stars > maxStarsForLevel) {
                                maxStarsForLevel = progress.stars
                            }
                        } catch (e: Exception) {
                            // Bu seviye için kayıt yok, devam et
                        }
                        
                        // Bu seviyenin en yüksek yıldızını toplama ekle (her seviye için max 3)
                        if (maxStarsForLevel > 0) {
                            totalStars += maxStarsForLevel
                            Log.d(TAG, "Sayı Avı seviye $level için en yüksek yıldız: $maxStarsForLevel")
                        }
                    }
                } else {
                    // Diğer interaktif oyunlar için seviye bazlı ID'leri kontrol et
                    for (level in 1..8) { // Maksimum 8 seviye olabilir
                        // Farklı seviye ID formatlarını kontrol et
                        val formats = listOf(
                            "${gameId}_Level_$level", // Büyük L formatı 
                            "${gameId}_level_$level"  // Küçük l formatı
                        )
                        
                        for (levelFormat in formats) {
                            try {
                                val progress = gameDataRepository.getProgress(levelFormat)
                                if (progress.stars > 0) {
                                    totalStars += progress.stars
                                    Log.d(TAG, "İnteraktif oyun kazanılan yıldız (Seviye): $levelFormat: ${progress.stars}")
                                }
                            } catch (e: Exception) {
                                // Bu format için yıldız bulunamadı - sessizce devam et
                            }
                        }
                    }
                }
            }
            
            Log.d(TAG, "Toplam kazanılan yıldızlar: $totalStars")
            return totalStars
        } catch (e: Exception) {
            Log.e(TAG, "Toplam kazanılan yıldız alınırken hata", e)
            0
        }
    }
    
    /**
     * Favori oyunu getirir (en çok oynanan oyun)
     */
    fun getFavoriteGame(): String {
        try {
            var maxPlayCount = -1
            var mostPlayedGameId = ""
            var logMsg = "Tüm oyunların oynama sayıları: "
            
            // VALID_GAME_IDS içindeki tüm oyunları kontrol et
            for (gameId in VALID_GAME_IDS) {
                val playCount = getGamePlayCount(gameId)
                logMsg += "[$gameId: $playCount] "
                
                if (playCount > maxPlayCount) {
                    maxPlayCount = playCount
                    mostPlayedGameId = gameId
                } else if (playCount == maxPlayCount && playCount > 0) {
                    // Eşitlik durumunda şimdilik ilk bulunanı tercih ediyoruz
                }
            }
            
            // Temel işlem oyunlarını seviye bazlı ID'lerden kontrol et
            val basicGamePrefixes = mapOf(
                "addition_game" to "AdditionGameActivity",
                "subtraction_game" to "SubtractionGameActivity", 
                "multiplication_game" to "MultiplicationGameActivity",
                "division_game" to "DivisionGameActivity"
            )
            
            // Seviye bazlı temel oyunları kontrol et
            for ((prefix, activityName) in basicGamePrefixes) {
                var totalPlaysForGame = 0
                
                // Her seviyeyi kontrol et (1-5 arası)
                for (level in 1..5) {
                    val levelGameId = "${prefix}_$level"
                    val count = prefs.getInt("game_play_count_$levelGameId", 0)
                    if (count > 0) {
                        totalPlaysForGame += count
                        logMsg += "[$levelGameId: $count] "
                    }
                }
                
                // En çok oynanan oyun olup olmadığını kontrol et
                if (totalPlaysForGame > maxPlayCount) {
                    maxPlayCount = totalPlaysForGame
                    mostPlayedGameId = activityName
                    Log.d(TAG, "Yeni favori oyun (seviye bazlı): $activityName, Sayı: $totalPlaysForGame")
                }
            }
            
            Log.d(TAG, logMsg)
            Log.d(TAG, "Favori oyun hesaplandı: En çok oynanan ID='$mostPlayedGameId', Sayı=$maxPlayCount")

            return if (maxPlayCount > 0 && mostPlayedGameId.isNotEmpty()) {
                val favoriteGameName = GAME_NAMES[mostPlayedGameId] ?: "Bilinmiyor"
                
                // Favori oyun ismini SharedPreferences'e kaydet
                val storedFavorite = prefs.getString(FAVORITE_GAME_KEY, null)
                if (storedFavorite != favoriteGameName) {
                    prefs.edit().putString(FAVORITE_GAME_KEY, favoriteGameName).apply()
                    Log.d(TAG, "SharedPreferences'daki favori oyun güncellendi: $favoriteGameName")
                }
                favoriteGameName
            } else {
                // Eğer hiç oyun oynanmadıysa
                "Henüz Yok"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Favori oyun belirlenirken hata", e)
            return "Bilinmiyor"
        }
    }
    
    /**
     * Oyun süresini formatla (2 saat 15 dk gibi)
     */
    fun formatPlayTime(minutes: Int): String {
        if (minutes < 0) return "0 dk"
        val hours = minutes / 60
        val mins = minutes % 60
        
        return when {
            hours > 0 && mins > 0 -> "$hours saat $mins dk"
            hours > 0 -> "$hours saat"
            else -> "$mins dk"
        }
    }
    
    /**
     * Tüm istatistikleri sıfırla (test için)
     */
    fun resetAllStatistics() {
        try {
            val editor = prefs.edit()
            editor.putInt(TOTAL_PLAY_TIME_KEY, 0)
            editor.putInt(GAMES_COMPLETED_KEY, 0)
            editor.remove(FAVORITE_GAME_KEY)

            for (gameId in VALID_GAME_IDS) {
                editor.putInt("game_play_count_$gameId", 0)
            }

            editor.apply()
            Log.i(TAG, "Tüm oyun istatistikleri sıfırlandı.")
        } catch (e: Exception) {
            Log.e(TAG, "İstatistikler sıfırlanırken hata", e)
        }
    }
    
    /**
     * Oyun tamamlandığında istatistikleri kaydeder
     */
    fun recordGameCompletion(
        gameType: String,
        score: Int,
        stars: Int,
        timeSpentSeconds: Int
    ) {
        try {
            // Önce bu bir temel işlem oyunu mu kontrol et
            val basicGamePrefixes = listOf(
                "addition_game", "subtraction_game", 
                "multiplication_game", "division_game"
            )
            
            // Log kaydı - hangi oyunun bittiğini göster
            Log.d(TAG, "recordGameCompletion: gameType=$gameType, score=$score, stars=$stars, time=${timeSpentSeconds}s")
            
            var gameId = gameType
            var level = 1 // Varsayılan seviye
            
            // Farklı seviye formatlarını ele al
            if (gameType.contains("_level_")) {
                // NumberHuntGameActivity_level_1 veya balloon_pop_level_1 formatı
                val parts = gameType.split("_level_")
                if (parts.size == 2) {
                    gameId = parts[0]
                    level = parts[1].toIntOrNull() ?: 1
                    Log.d(TAG, "Seviye bazlı oyun tespit edildi: $gameId, Seviye: $level")
                }
            } else if (gameType.contains("GAME_")) {
                // GameType.BALLOON_POP_GAME gibi Enum değerleri
                try {
                    val enumTypeStr = gameType.replace("GameType.", "")
                    val mappedId = when (enumTypeStr) {
                        "ADDITION_GAME" -> "AdditionGameActivity"
                        "SUBTRACTION_GAME" -> "SubtractionGameActivity"
                        "MULTIPLICATION_GAME" -> "MultiplicationGameActivity"
                        "DIVISION_GAME" -> "DivisionGameActivity"
                        "BALLOON_POP_GAME" -> "BalloonPopGameActivity"
                        "NUMBER_HUNT_GAME" -> "NumberHuntGameActivity"
                        "MATH_MAZE_GAME" -> "MathMazeGameActivity"
                        "SPACE_MATH_ADVENTURE" -> "SpaceMathAdventureActivity"
                        "SHAPE_RECOGNITION" -> "ShapeRecognitionGameActivity"
                        "MARKET_SIMULATOR" -> "MarketSimulatorActivity"
                        else -> null
                    }
                    
                    if (mappedId != null) {
                        gameId = mappedId
                        Log.d(TAG, "Enum GameType formatı tespit edildi ve dönüştürüldü: $gameType -> $gameId")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Enum dönüşümü yapılamadı: $gameType", e)
                }
            }
            
            // Normalize edilmiş gameId aldıktan sonra, VALID_GAME_IDS ile uyumlu bir ID oluştur
            var normalizedGameId = gameId
            
            // ActivityName soneki kontrolü
            if (!gameId.endsWith("Activity") && !basicGamePrefixes.any { gameId.startsWith(it) }) {
                // Bazı oyunlar aktivite adını içermeyebilir, eklemeyi dene
                val possibleId = "${gameId}Activity"
                if (VALID_GAME_IDS.contains(possibleId)) {
                    normalizedGameId = possibleId
                    Log.d(TAG, "ID düzeltildi: $gameId -> $normalizedGameId")
                }
            }
            
            // Tutarlı gameId için son kontrol
            if (!VALID_GAME_IDS.contains(normalizedGameId) && !basicGamePrefixes.any { normalizedGameId.startsWith(it) }) {
                // Hala geçerli bir ID bulamadık, en yakın eşleşmeyi bulmaya çalış
                val bestMatch = VALID_GAME_IDS.firstOrNull { 
                    it.contains(normalizedGameId, ignoreCase = true) || 
                    normalizedGameId.contains(it.replace("Activity", ""), ignoreCase = true) 
                }
                
                if (bestMatch != null) {
                    normalizedGameId = bestMatch
                    Log.d(TAG, "ID en yakın eşleşme ile düzeltildi: $gameId -> $normalizedGameId")
                } else {
                    // Son çare: varsayılan ID'yi kullan
                    Log.w(TAG, "Geçersiz oyun ID'si için son çare: $gameId -> $normalizedGameId")
                }
            }
            
            // Oyun başına oynanma sayısını artır
            var gameIdToIncrement = normalizedGameId
            
            // Seviye bazlı temel oyunlar için özel ID formatı kullan
            if (basicGamePrefixes.any { normalizedGameId.startsWith(it) }) {
                gameIdToIncrement = "${normalizedGameId}_$level"
                Log.d(TAG, "Temel oyun seviye bazlı ID oluşturuldu: $gameIdToIncrement")
            } else if (level > 1) {
                // Temel olmayan oyunlar için: Eğer seviye bilgisi varsa ana ID'yi kullan
                Log.d(TAG, "Seviye bilgisi olan interaktif oyun: kullanılan ID=$normalizedGameId (seviye=$level)")
            }
            
            // Ana kayıt işlemi - toplam sayı için
            incrementTotalGameCompletionCount()
            
            // Oyun bazlı oynanma sayısı artır (eğer ID geçerliyse)
            if (VALID_GAME_IDS.contains(normalizedGameId)) {
                incrementGamePlayCount(normalizedGameId)
                // Oyunu oynandı olarak işaretle ve son oynama zamanını güncelle
                markGamePlayed(normalizedGameId)
                updateLastPlayedTime(normalizedGameId)
                Log.d(TAG, "Oyun oynama sayısı artırıldı ve son oynama zamanı güncellendi: $normalizedGameId")
            } else {
                Log.w(TAG, "Oynanma sayısı artırılamadı, geçersiz ID: $normalizedGameId")
            }
            
            // Seviye bazlı oyunlar için (temel oyunlar) seviyeye özel sayaç
            if (basicGamePrefixes.any { normalizedGameId.startsWith(it) }) {
                val levelSpecificId = "${normalizedGameId}_$level"
                prefs.edit().putInt("game_play_count_$levelSpecificId", 
                    prefs.getInt("game_play_count_$levelSpecificId", 0) + 1).apply()
                Log.d(TAG, "Seviye bazlı oynanma sayısı artırıldı: $levelSpecificId")
            }
            
            // Sayı Avı oyunu ve diğer interaktif oyunlar için seviye bazlı kayıt
            if (normalizedGameId == "NumberHuntGameActivity" || 
                normalizedGameId == "BalloonPopGameActivity" || 
                normalizedGameId == "MathMazeGameActivity" || 
                normalizedGameId == "SpaceMathAdventureActivity") {
                
                // Seviye bilgisi varsa, seviye bazlı sayaç
                if (level > 0) {
                    val levelSpecificId = "${normalizedGameId}_level_$level"
                    prefs.edit().putInt("game_play_count_$levelSpecificId", 
                        prefs.getInt("game_play_count_$levelSpecificId", 0) + 1).apply()
                    Log.d(TAG, "İnteraktif oyun seviye bazlı oynanma sayısı artırıldı: $levelSpecificId")
                }
            }
            
            // Oyun süresini kaydet (saniye cinsinden)
            val gameTimePrefKey = "game_time_$normalizedGameId"
            val currentSeconds = prefs.getInt(gameTimePrefKey, 0)
            prefs.edit().putInt(gameTimePrefKey, currentSeconds + timeSpentSeconds).apply()
            Log.d(TAG, "Oyun süresi güncellendi: $normalizedGameId, +${timeSpentSeconds}s, Toplam: ${currentSeconds + timeSpentSeconds}s")
            
            // Skoru ve yıldızları kaydet (GameProgress ile)
            try {
                // Mevcut kayıtla karşılaştır, daha iyi skorsa güncelle
                val existingProgress = gameDataRepository.getProgress(normalizedGameId)
                
                // En iyi değerleri sakla
                val newHighScore = kotlin.math.max(score, existingProgress.highScore)
                val newStars = kotlin.math.max(stars, existingProgress.stars)
                gameDataRepository.saveProgress(
                    gameId = normalizedGameId,
                    level = level,
                    score = newHighScore,
                    stars = newStars
                )
                
                Log.d(TAG, "Oyun ilerlemesi kaydedildi: $normalizedGameId, Seviye: $level, Skor: $newHighScore, Yıldız: $newStars")
                
                // Sayı Avı oyunu ve diğer interaktif oyunlar için seviye bazlı kayıt
                if (normalizedGameId == "NumberHuntGameActivity" || 
                    normalizedGameId == "BalloonPopGameActivity" || 
                    normalizedGameId == "MathMazeGameActivity" || 
                    normalizedGameId == "SpaceMathAdventureActivity") {
                    
                    // Seviye bilgisi varsa, seviye bazlı yıldız ve skor kaydet
                    if (level > 0) {
                        val levelSpecificId = "${normalizedGameId}_level_$level"
                        try {
                            // Seviye bazlı mevcut ilerlemeler
                            val existingLevelProgress = gameDataRepository.getProgress(levelSpecificId)
                            val levelHighScore = kotlin.math.max(score, existingLevelProgress.highScore)
                            val levelStars = kotlin.math.max(stars, existingLevelProgress.stars)
                            
                            // Her seviye için ilerlemeleri kaydet
                            gameDataRepository.saveProgress(
                                gameId = levelSpecificId,
                                level = level,
                                score = levelHighScore,
                                stars = levelStars
                            )
                            
                            Log.d(TAG, "İnteraktif oyun seviye bazlı ilerleme kaydedildi: $levelSpecificId, Skor: $levelHighScore, Yıldız: $levelStars")
                        } catch (e: Exception) {
                            // Mevcut kayıt yoksa, direkt yeni oluştur
                            gameDataRepository.saveProgress(
                                gameId = levelSpecificId,
                                level = level,
                                score = score,
                                stars = stars
                            )
                            Log.d(TAG, "İnteraktif oyun seviye bazlı ilk ilerleme kaydedildi: $levelSpecificId, Skor: $score, Yıldız: $stars")
                        }
                    }
                }
            } catch (e: Exception) {
                // Repository erişiminde hata - direkt kaydet
                Log.e(TAG, "Mevcut kayıtlar alınırken hata, doğrudan yeni kayıt oluşturuluyor", e)
                gameDataRepository.saveProgress(
                    gameId = normalizedGameId,
                    level = level,
                    score = score,
                    stars = stars
                )
                
                // Sayı Avı oyunu ve diğer interaktif oyunlar için seviye bazlı kayıt (hata durumunda da)
                if (normalizedGameId == "NumberHuntGameActivity" || 
                    normalizedGameId == "BalloonPopGameActivity" || 
                    normalizedGameId == "MathMazeGameActivity" || 
                    normalizedGameId == "SpaceMathAdventureActivity") {
                    
                    // Seviye bilgisi varsa, seviye bazlı yıldız ve skor kaydet
                    if (level > 0) {
                        val levelSpecificId = "${normalizedGameId}_level_$level"
                        gameDataRepository.saveProgress(
                            gameId = levelSpecificId,
                            level = level,
                            score = score,
                            stars = stars
                        )
                        Log.d(TAG, "Hata sonrası interaktif oyun seviye bazlı ilerleme kaydedildi: $levelSpecificId, Skor: $score, Yıldız: $stars")
                    }
                }
            }
            
            // İşlem sonrası toplam yıldızları kontrol et
            try {
                val totalStars = getTotalStars()
                Log.d(TAG, "İşlem sonrası toplam yıldızlar: $totalStars")
                
                // Seviye bazlı oyunlar için kontrol et
                if (normalizedGameId == "NumberHuntGameActivity" && level > 0) {
                    val levelSpecificId = "${normalizedGameId}_level_$level"
                    val progress = gameDataRepository.getProgress(levelSpecificId)
                    Log.d(TAG, "Seviye bazlı ilerleme kontrolü - $levelSpecificId: Skor=${progress.highScore}, Yıldız=${progress.stars}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "İşlem sonrası kontroller sırasında hata", e)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Oyun tamamlama istatistikleri güncellenirken hata", e)
        }
    }
    
    /**
     * Toplam oyun tamamlama sayısını arttırır
     */
    private fun incrementTotalGameCompletionCount() {
        try {
            val currentTotalCount = prefs.getInt(GAMES_COMPLETED_KEY, 0)
            prefs.edit().putInt(GAMES_COMPLETED_KEY, currentTotalCount + 1).apply()
            Log.d(TAG, "Toplam tamamlanan oyun sayısı arttırıldı: ${currentTotalCount + 1}")
        } catch (e: Exception) {
            Log.e(TAG, "Toplam oyun tamamlama sayısı artırılırken hata", e)
        }
    }
    
    /**
     * Bir oyunun oynandığını işaretler ve sık oynanan oyunları otomatik olarak favori yapar
     * Bu metot, favori oyunu güncel tutmak için gereklidir
     */
    fun markGamePlayed(gameId: String) {
        try {
            val playCount = getGamePlayCount(gameId)
            
            // Eğer bu oyun belirli bir eşiği geçtiyse (örn. 5 kez oynandıysa), favori olarak işaretle
            if (playCount >= 5) {
                val gameName = GAME_NAMES[gameId] ?: gameId
                updateFavoriteGame(gameName)
                Log.d(TAG, "$gameId oyunu favori olarak işaretlendi (oynanma sayısı: $playCount)")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Oyun favori olarak işaretlenirken hata: $gameId", e)
        }
    }
    
    /**
     * Favori oyunu günceller ve SharedPreferences'e kaydeder
     */
    private fun updateFavoriteGame(gameName: String) {
        try {
            // Sadece geçerli bir oyun adı ise kaydet
            if (gameName.isNotEmpty() && gameName != "Bilinmiyor" && gameName != "Henüz Yok") {
                prefs.edit().putString(FAVORITE_GAME_KEY, gameName).apply()
                Log.d(TAG, "Favori oyun güncellendi: $gameName")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Favori oyun güncellenirken hata", e)
        }
    }
    
    /**
     * Bir oyunun son oynama zamanını günceller
     * Bu metot, en son oynanan oyunları takip etmek için gereklidir
     */
    fun updateLastPlayedTime(gameId: String) {
        try {
            val currentTimeMillis = System.currentTimeMillis()
            prefs.edit().putLong("last_played_${gameId}", currentTimeMillis).apply()
            Log.d(TAG, "$gameId için son oynama zamanı güncellendi: $currentTimeMillis")
        } catch (e: Exception) {
            Log.e(TAG, "Son oynama zamanı güncellenirken hata: $gameId", e)
        }
    }
    
    /**
     * Oyun süresini saniye cinsinden kaydeder
     * @param gameType Oyun türü (string olarak)
     * @param timeInSeconds Oyunda geçirilen süre (saniye cinsinden)
     */
    fun recordGameTime(gameType: String, timeInSeconds: Int) {
        try {
            Log.d(TAG, "recordGameTime başlatılıyor: gameType=$gameType, timeInSeconds=$timeInSeconds")
            
            // Kullanılacak gameId'yi belirle
            val gameId = if (VALID_GAME_IDS.contains(gameType)) {
                // Doğrudan geçerli bir gameId ise kullan
                gameType
            } else {
                try {
                    // Enum olarak çevirmeyi dene
                    val gameTypeEnum = GameType.valueOf(gameType)
                    getGameIdString(gameTypeEnum)
                } catch (e: Exception) {
                    Log.w(TAG, "recordGameTime: GameType dönüştürme hatası: $gameType", e)
                    return
                }
            }
            
            // Saniyeyi dakikaya çevir (en az 1 dakika olacak şekilde)
            val minutesSpent = max(1, timeInSeconds / 60)
            
            // Toplam oyun süresine ekle
            addPlayTime(minutesSpent)
            
            // Oyuna özel süre istatistiği
            val gameTimePrefKey = "game_time_$gameId"
            val currentGameTime = prefs.getInt(gameTimePrefKey, 0)
            val newTotalTime = currentGameTime + timeInSeconds
            
            prefs.edit().putInt(gameTimePrefKey, newTotalTime).apply()
            
            Log.d(TAG, "$gameId oyunu için süre kaydedildi: +$timeInSeconds saniye, " +
                    "toplam ${formatSeconds(newTotalTime)}")
        } catch (e: Exception) {
            Log.e(TAG, "Oyun süresi kaydedilirken hata", e)
        }
    }
    
    /**
     * Saniye cinsinden zamanı formatlı şekilde döndürür (3s, 2d 5s, 1s 15d 30s gibi)
     */
    private fun formatSeconds(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        
        return when {
            hours > 0 -> "$hours s ${minutes}d ${secs}s"
            minutes > 0 -> "${minutes}d ${secs}s"
            else -> "${secs}s"
        }
    }
    
    /**
     * Her oyun için kazanılan yıldızları içeren bir map döndürür
     * Premium kullanıcılar için detaylı istatistik popup'ı için kullanılır
     */
    fun getStarsPerGameDetails(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        try {
            // Tüm oyun isimlerini listeye ekle (hiç oynanmamış olsalar bile)
            for ((_, displayName) in GAME_NAMES) {
                result[displayName] = 0 // Varsayılan olarak 0 yıldız
            }
            
            // Temel işlem oyunlarını seviye bazlı ID'lerden topla - activity adlarını doğru eşleştir
            val basicGamePrefixes = mapOf(
                "addition_game" to "Toplama Oyunu",
                "subtraction_game" to "Çıkarma Oyunu",
                "multiplication_game" to "Çarpma Oyunu",
                "division_game" to "Bölme Oyunu"
            )
            
            // Doğrudan activity adı ile eşleştireceğimiz bir map (GAME_NAMES'e karşılık gelen)
            val activityNameMap = mapOf(
                "addition_game" to "AdditionGameActivity",
                "subtraction_game" to "SubtractionGameActivity", 
                "multiplication_game" to "MultiplicationGameActivity",
                "division_game" to "DivisionGameActivity"
            )
            
            // Seviye bazlı temel oyunları kontrol et
            for ((prefix, displayName) in basicGamePrefixes) {
                var totalStarsForGame = 0
                
                // Her seviyeyi kontrol et (1-5 arası)
                for (level in 1..5) {
                    // Seviye için yıldız sayısını takip et
                    var starsForThisLevel = 0
                    
                    // İlk olarak yeni formatı kontrol et (addition_game_level_1)
                    val levelFormatId = "${prefix}_level_$level"
                    try {
                        val progress = gameDataRepository.getProgress(levelFormatId)
                        if (progress.stars > 0) {
                            starsForThisLevel = progress.stars
                            Log.d(TAG, "Seviyeli oyun yıldızları (yeni format): $levelFormatId = ${progress.stars} yıldız")
                        }
                    } catch (e: Exception) {
                        // Bu formatta kayıt yok - sorun değil
                    }
                    
                    // Yeni formatta bulunamadıysa eski formatı kontrol et (addition_game_1)
                    if (starsForThisLevel == 0) {
                        val oldLevelGameId = "${prefix}_$level"
                        try {
                            val progress = gameDataRepository.getProgress(oldLevelGameId)
                            if (progress.stars > 0) {
                                starsForThisLevel = progress.stars
                                Log.d(TAG, "Seviyeli oyun yıldızları (eski format): $oldLevelGameId = ${progress.stars} yıldız")
                            }
                        } catch (e: Exception) {
                            // Bu formatta da kayıt yok - sorun değil
                        }
                    }
                    
                    // Hala bulunamadıysa StarManager'dan kontrol et
                    if (starsForThisLevel == 0) {
                        try {
                            val starManager = StarManager.getInstance(context)
                            val smStars1 = starManager.getGameStars(levelFormatId) // Yeni format kontrolü
                            if (smStars1 > 0) {
                                starsForThisLevel = smStars1
                                Log.d(TAG, "StarManager'dan temel oyun yıldızı (yeni format): $levelFormatId: $smStars1")
                            } else {
                                val smStars2 = starManager.getGameStars("${prefix}_$level") // Eski format kontrolü
                                if (smStars2 > 0) {
                                    starsForThisLevel = smStars2
                                    Log.d(TAG, "StarManager'dan temel oyun yıldızı (eski format): ${prefix}_$level: $smStars2")
                                }
                            }
                        } catch (e: Exception) {
                            // StarManager kontrolü başarısız - sessizce devam et
                        }
                    }
                    
                    // Bu seviye için bulunan yıldızları toplama ekle
                    totalStarsForGame += starsForThisLevel
                }
                
                // İki farklı yere yıldızları ekle:
                // 1. Doğrudan displayName için (eski stil)
                result[displayName] = totalStarsForGame
                
                // 2. GAME_NAMES'teki karşılığı için (popup karşılaştırması için)
                val activityName = activityNameMap[prefix]
                if (activityName != null) {
                    val gameName = GAME_NAMES[activityName] ?: displayName
                    result[gameName] = totalStarsForGame
                    
                    if (totalStarsForGame > 0) {
                        Log.d(TAG, "Toplam seviyeli oyun yıldızları (activity): $gameName = $totalStarsForGame yıldız")
                    }
                }
                
                if (totalStarsForGame > 0) {
                    Log.d(TAG, "Toplam seviyeli oyun yıldızları: $displayName = $totalStarsForGame yıldız")
                }
            }
            
            // İnteraktif oyunlar için seviye bazlı ID desteği ekle
            val interactiveGamePrefixes = mapOf(
                "BalloonPopGameActivity" to "Balon Patlatma",
                "NumberHuntGameActivity" to "Sayı Avı",
                "MathMazeGameActivity" to "Matematik Labirenti",
                "SpaceMathAdventureActivity" to "Uzay Matematik Macerası"
            )
            
            // Oyun başına yıldız topla
            for ((gameId, displayName) in interactiveGamePrefixes) {
                var totalStarsForGame = 0
                
                // Ana ID'den yıldız
                val mainProgress = gameDataRepository.getProgress(gameId)
                totalStarsForGame += mainProgress.stars
                
                if (mainProgress.stars > 0) {
                    Log.d(TAG, "İnteraktif oyun ana yıldız: $gameId = ${mainProgress.stars} yıldız")
                }
                
                // Seviye bazlı ek ID'leri kontrol et - bazı interaktif oyunlar seviye bazlı ID kullanabilir
                for (level in 1..5) {
                    // Farklı seviye ID formatlarını kontrol et
                    val formats = listOf(
                        "${gameId}_level_$level",
                        "${gameId}_Level_$level",
                        "${gameId.replace("Activity", "")}_level_$level",
                        "${gameId.lowercase().replace("activity", "")}_$level"
                    )
                    
                    for (levelFormat in formats) {
                        try {
                            val progress = gameDataRepository.getProgress(levelFormat)
                            if (progress.stars > 0) {
                                totalStarsForGame += progress.stars
                                Log.d(TAG, "İnteraktif oyun seviyeli yıldız: $levelFormat = ${progress.stars} yıldız")
                            }
                        } catch (e: Exception) {
                            // Belirli bir seviye ID'si için kayıt bulunamadı - normal durum
                            // Sessizce devam et
                        }
                    }
                }
                
                // İnteraktif oyun için toplam yıldız sonucunu güncelle
                result[displayName] = totalStarsForGame
                if (totalStarsForGame > 0) {
                    Log.d(TAG, "İnteraktif oyun toplam yıldız: $displayName = $totalStarsForGame yıldız")
                }
            }
            
            // Diğer oyunlar: Şekil Tanıma ve Market Simülatörü için standart kontrol
            val otherGameIds = listOf(
                "ShapeRecognitionGameActivity", 
                "MarketSimulatorActivity"
            )
            
            for (gameId in otherGameIds) {
                val progress = gameDataRepository.getProgress(gameId)
                val stars = progress.stars
                val gameName = GAME_NAMES[gameId] ?: gameId
                
                // İlgili oyun adı için yıldızları güncelle
                result[gameName] = stars
                if (stars > 0) {
                    Log.d(TAG, "Diğer oyun yıldız detayı: $gameName = $stars yıldız")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Oyun yıldızları alınırken hata", e)
            e.printStackTrace() // Daha detaylı hata izlemesi için
        }
        return result.toList().sortedByDescending { (_, value) -> value }.toMap()
    }
    
    /**
     * Her oyun için oynama sayısını içeren bir map döndürür
     * Premium kullanıcılar için detaylı istatistik popup'ı için kullanılır
     */
    fun getGamePlayCountDetails(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        try {
            // Debug log
            Log.d(TAG, "getGamePlayCountDetails çağrıldı - popup içeriği oluşturuluyor")
            
            // Tüm oyun isimlerini listeye ekle (hiç oynanmamış olsalar bile)
            for ((gameId, displayName) in GAME_NAMES) {
                // İlgili oyun için gerçek oynanma sayısını al
                val playCount = getGamePlayCount(gameId)
                
                // Gerçek değeri güncelle (varsayılan 0 yerine)
                result[displayName] = playCount
                
                if (playCount > 0) {
                    Log.d(TAG, "Oyun oynanma detayı: $displayName = $playCount kez")
                }
            }
            
            // Temel işlem oyunlarını seviye bazlı ID'lerden topla
            val basicGamePrefixes = mapOf(
                "addition_game" to "Toplama Oyunu",
                "subtraction_game" to "Çıkarma Oyunu",
                "multiplication_game" to "Çarpma Oyunu",
                "division_game" to "Bölme Oyunu"
            )
            
            // Seviye bazlı temel oyunları kontrol et
            for ((prefix, displayName) in basicGamePrefixes) {
                var totalPlaysForGame = 0
                
                // Her seviyeyi kontrol et (1-5 arası)
                for (level in 1..5) {
                    val levelGameId = "${prefix}_level_$level"
                    val count = prefs.getInt("game_play_count_$levelGameId", 0)
                    if (count > 0) {
                        totalPlaysForGame += count
                        Log.d(TAG, "Seviyeli oyun oynanma sayısı: $levelGameId = $count kez")
                    }
                }
                
                // Eğer varsa, toplam oynanma sayısını güncelle
                if (totalPlaysForGame > 0) {
                    result[displayName] = totalPlaysForGame
                    Log.d(TAG, "Toplam seviyeli oyun oynanma sayısı: $displayName = $totalPlaysForGame kez")
                }
            }
            
            // Map'teki 0 değerlerini kaldırma
            val nonZeroResults = result.filter { (_, count) -> count > 0 }
            
            // Debug log
            Log.d(TAG, "getGamePlayCountDetails tamamlandı - Toplam oyun sayısı: ${nonZeroResults.size}")
            nonZeroResults.forEach { (game, count) ->
                Log.d(TAG, "Tamamlanan oyun: $game = $count kez")
            }
            
            return nonZeroResults.toList().sortedByDescending { (_, value) -> value }.toMap()
        } catch (e: Exception) {
            Log.e(TAG, "Oyun oynama sayıları alınırken hata", e)
            e.printStackTrace() // Daha detaylı hata izlemesi için
            return emptyMap()
        }
    }
    
    /**
     * Her oyun için oynama sürelerini içeren bir map döndürür (saniye cinsinden)
     * Premium kullanıcılar için detaylı istatistik popup'ı için kullanılır
     */
    fun getGamePlayTimeDetails(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        try {
            // Debug log
            Log.d(TAG, "getGamePlayTimeDetails çağrıldı - popup içeriği oluşturuluyor")
            
            // Tüm GAME_NAMES içindeki oyunları kontrol et
            for ((gameId, displayName) in GAME_NAMES) {
                // Her oyun için süreyi doğrudan al
                val seconds = prefs.getInt("game_time_$gameId", 0)
                result[displayName] = seconds
                
                if (seconds > 0) {
                    Log.d(TAG, "Oyun süresi (ana ID): $displayName = $seconds saniye")
                }
            }
            
            // Temel işlem oyunlarını seviye bazlı ID'lerden topla
            val basicGamePrefixes = mapOf(
                "addition_game" to "Toplama Oyunu",
                "subtraction_game" to "Çıkarma Oyunu",
                "multiplication_game" to "Çarpma Oyunu",
                "division_game" to "Bölme Oyunu"
            )
            
            // Seviye bazlı temel oyunları kontrol et
            for ((prefix, displayName) in basicGamePrefixes) {
                var totalSecondsForGame = 0
                
                // Her seviyeyi kontrol et (1-5 arası)
                for (level in 1..5) {
                    val levelGameId = "${prefix}_level_$level"
                    val seconds = prefs.getInt("game_time_$levelGameId", 0)
                    if (seconds > 0) {
                        totalSecondsForGame += seconds
                        Log.d(TAG, "Seviyeli oyun süresi: $levelGameId = $seconds saniye")
                    }
                }
                
                // Eğer süre varsa, mevcut değeri güncelle
                if (totalSecondsForGame > 0) {
                    result[displayName] = totalSecondsForGame
                    Log.d(TAG, "Toplam seviyeli oyun süresi: $displayName = $totalSecondsForGame saniye")
                }
            }
            
            // İnteraktif oyunlar için seviye bazlı ID desteği ekle
            val interactiveGamePrefixes = mapOf(
                "BalloonPopGameActivity" to "Balon Patlatma",
                "NumberHuntGameActivity" to "Sayı Avı",
                "MathMazeGameActivity" to "Matematik Labirenti",
                "SpaceMathAdventureActivity" to "Uzay Matematik Macerası"
            )
            
            // Oyun başına süre topla
            for ((gameId, displayName) in interactiveGamePrefixes) {
                var totalSecondsForGame = result[displayName] ?: 0
                
                // Seviye bazlı ek ID'leri kontrol et - bazı interaktif oyunlar seviye bazlı ID kullanabilir
                for (level in 1..5) {
                    // Farklı seviye ID formatlarını kontrol et
                    val formats = listOf(
                        "${gameId}_level_$level",
                        "${gameId}_Level_$level",
                        "${gameId.replace("Activity", "")}_level_$level"
                    )
                    
                    for (levelFormat in formats) {
                        val seconds = prefs.getInt("game_time_$levelFormat", 0)
                        if (seconds > 0) {
                            totalSecondsForGame += seconds
                            Log.d(TAG, "İnteraktif oyun seviyeli süre: $levelFormat = $seconds saniye")
                        }
                    }
                }
                
                // İnteraktif oyun için toplam süre sonucunu güncelle
                if (totalSecondsForGame > 0) {
                    result[displayName] = totalSecondsForGame
                    Log.d(TAG, "İnteraktif oyun toplam süre: $displayName = $totalSecondsForGame saniye")
                }
            }
            
            // Map'teki 0 değerlerini kaldırma
            val nonZeroResults = result.filter { (_, seconds) -> seconds > 0 }
            
            // Debug log
            Log.d(TAG, "getGamePlayTimeDetails tamamlandı - Toplam oyun sayısı: ${nonZeroResults.size}")
            nonZeroResults.forEach { (game, seconds) ->
                Log.d(TAG, "Oynanmış oyun süresi: $game = $seconds saniye")
            }
            
            return nonZeroResults.toList().sortedByDescending { (_, value) -> value }.toMap()
        } catch (e: Exception) {
            Log.e(TAG, "Oyun süreleri alınırken hata", e)
            e.printStackTrace() // Daha detaylı hata izlemesi için
            return emptyMap()
        }
    }
    
    /**
     * Her oyun için en yüksek skorları içeren bir map döndürür
     * Premium kullanıcılar için detaylı istatistik popup'ı için kullanılır
     */
    fun getHighScoresPerGameDetails(): Map<String, Int> {
        val result = mutableMapOf<String, Int>()
        try {
            for (gameId in VALID_GAME_IDS) {
                val progress = gameDataRepository.getProgress(gameId)
                val highScore = progress.highScore
                if (highScore > 0) {
                    val gameName = GAME_NAMES[gameId] ?: gameId
                    result[gameName] = highScore
                    Log.d(TAG, "Yüksek skor detayı: $gameName = $highScore puan")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Oyun başına en yüksek skorlar alınırken hata", e)
        }
        return result.toList().sortedByDescending { (_, value) -> value }.toMap()
    }
    
    /**
     * Temel işlem oyunlarının süre formatını dakikadan saniyeye dönüştürür.
     * 
     * Bu metod bir kerelik bir bakım işlemidir. Temel işlem oyunlarının süre verilerini
     * dakika formatından saniye formatına dönüştürür.
     * 
     * Bir uygulama güncellemesinden sonra ilk açılışta bir kere çağırılmalıdır.
     * 
     * @return Düzeltilen süre kaydı sayısı
     */
    fun fixBasicOperationGameTimeFormat(): Int {
        var fixedCount = 0
        try {
            Log.d(TAG, "Temel işlem oyunları için süre formatı düzeltme işlemi başlatılıyor...")
            
            // Temel işlem oyunlarının ID prefixleri
            val basicGamePrefixes = listOf(
                "addition_game", "subtraction_game", 
                "multiplication_game", "division_game"
            )
            
            // Ana ID'leri kontrol et
            for (prefix in basicGamePrefixes) {
                // Dakika formatında kaydedilmiş olabilecek ana ID'yi kontrol et
                val mainTimeKey = "game_time_$prefix"
                val mainTimeMinutes = prefs.getInt(mainTimeKey, 0)
                
                if (mainTimeMinutes > 0) {
                    // Dakikayı saniyeye çevir (60 saniye/dakika)
                    val mainTimeSeconds = mainTimeMinutes * 60
                    
                    // Saniye olarak güncelle
                    prefs.edit().putInt(mainTimeKey, mainTimeSeconds).apply()
                    Log.d(TAG, "Ana ID süre formatı düzeltildi: $prefix - $mainTimeMinutes dk -> $mainTimeSeconds sn")
                    fixedCount++
                }
                
                // Seviye bazlı ID'leri kontrol et
                for (level in 1..5) {
                    val levelTimeKey = "game_time_${prefix}_level_$level"
                    val levelTimeMinutes = prefs.getInt(levelTimeKey, 0)
                    
                    if (levelTimeMinutes > 0) {
                        // Dakikayı saniyeye çevir (60 saniye/dakika)
                        val levelTimeSeconds = levelTimeMinutes * 60
                        
                        // Saniye olarak güncelle
                        prefs.edit().putInt(levelTimeKey, levelTimeSeconds).apply()
                        Log.d(TAG, "Seviye bazlı ID süre formatı düzeltildi: ${prefix}_level_$level - $levelTimeMinutes dk -> $levelTimeSeconds sn")
                        fixedCount++
                    }
                }
            }
            
            Log.d(TAG, "Süre formatı düzeltme işlemi tamamlandı. Toplam düzeltilen kayıt: $fixedCount")
        } catch (e: Exception) {
            Log.e(TAG, "Süre formatı düzeltilirken hata oluştu: ${e.message}", e)
        }
        
        return fixedCount
    }

    /**
     * StarManager ile yıldızları senkronize eder.
     * Bu metod, temel işlem oyunları için yıldız verilerini düzeltir ve istatistiklerin doğru
     * görüntülenmesini sağlar.
     */
    fun syncStarsWithStarManager() {
        try {
            val starManager = StarManager.getInstance(context)
            
            // Temel işlem oyunları için ID formatlarını kontrol et
            val basicGamePrefixes = listOf(
                "addition_game",
                "subtraction_game",
                "multiplication_game", 
                "division_game"
            )
            
            val logBuilder = StringBuilder("Temel oyun yıldız senkronizasyonu: ")
            var syncCount = 0
            
            // 1. Her temel oyun ve seviye kombinasyonu için kontrol et
            for (prefix in basicGamePrefixes) {
                for (level in 1..5) {
                    // İki farklı ID formatı
                    val formatWithLevel = "${prefix}_level_$level" // Yeni format: addition_game_level_1
                    val formatWithoutLevel = "${prefix}_$level"    // Eski format: addition_game_1
                    
                    try {
                        // İlk format (addition_game_level_1) için kontrol
                        val starsInFormat1 = starManager.getGameStars(formatWithLevel)
                        
                        // İkinci format (addition_game_1) için kontrol
                        val starsInFormat2 = starManager.getGameStars(formatWithoutLevel)
                        
                        // Sadece eski formatta (addition_game_1) veri varsa ve yeni formatta yoksa
                        if (starsInFormat2 > 0 && starsInFormat1 == 0) {
                            // StarManager'da yeni formatta da kaydet
                            val result = starManager.addStars(formatWithLevel, starsInFormat2)
                            
                            if (result) {
                                syncCount++
                                logBuilder.append("[$formatWithoutLevel -> $formatWithLevel: $starsInFormat2] ")
                                Log.d(TAG, "Eski format senkronize edildi: $formatWithoutLevel -> $formatWithLevel, Yıldız: $starsInFormat2")
                            }
                        }
                        
                        // Repository'de veri yoksa ama StarManager'da varsa
                        try {
                            // GameDataRepository'den kontrol et
                            gameDataRepository.getProgress(formatWithLevel).stars
                        } catch (e: Exception) {
                            // Repository'de kayıt yok, ama StarManager'da varsa ekle
                            val starsInStarManager = starManager.getGameStars(formatWithLevel)
                            if (starsInStarManager > 0) {
                                gameDataRepository.saveProgress(
                                    gameId = formatWithLevel,
                                    level = level,
                                    score = 100, // Varsayılan skor
                                    stars = starsInStarManager
                                )
                                
                                syncCount++
                                logBuilder.append("[SM->Repo $formatWithLevel: $starsInStarManager] ")
                                Log.d(TAG, "StarManager'dan Repository'ye senkronize edildi: $formatWithLevel, Yıldız: $starsInStarManager")
                            }
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Yıldız senkronizasyonu sırasında hata: ${e.message}")
                    }
                }
            }
            
            if (syncCount > 0) {
                Log.d(TAG, "$syncCount oyun ID'si senkronize edildi. $logBuilder")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Yıldız senkronizasyonu sırasında hata oluştu", e)
        }
    }

    // Parametre ekleyen bir overload, belirli bir oyunu senkronize et
    fun syncStarsWithStarManager(gameId: String, level: Int) {
        try {
            val starManager = StarManager.getInstance(context)
            
            // Temel işlem oyunu mu?
            val basicGamePrefixes = listOf(
                "addition_game",
                "subtraction_game",
                "multiplication_game", 
                "division_game"
            )
            
            // GameType'tan prefix'i çıkar
            val prefix = when {
                gameId.contains("ADDITION") -> "addition_game"
                gameId.contains("SUBTRACTION") -> "subtraction_game"
                gameId.contains("MULTIPLICATION") -> "multiplication_game"
                gameId.contains("DIVISION") -> "division_game"
                else -> {
                    // Temel işlem oyunu değil, sadece formatı senkronize et
                    return
                }
            }
            
            // İki farklı ID formatı
            val formatWithLevel = "${prefix}_level_$level" // Yeni format
            val formatWithoutLevel = "${prefix}_$level"    // Eski format
            
            // İlk format için kontrol
            val starsInFormat1 = starManager.getGameStars(formatWithLevel)
            
            // İkinci format için kontrol
            val starsInFormat2 = starManager.getGameStars(formatWithoutLevel)
            
            // Sadece eski formatta veri varsa ve yeni formatta yoksa
            if (starsInFormat2 > 0 && starsInFormat1 == 0) {
                // StarManager'da yeni formatta da kaydet
                val result = starManager.addStars(formatWithLevel, starsInFormat2)
                
                if (result) {
                    Log.d(TAG, "Eski format senkronize edildi: $formatWithoutLevel -> $formatWithLevel, Yıldız: $starsInFormat2")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Yıldız senkronizasyonu sırasında hata oluştu: ${e.message}", e)
        }
    }
} 