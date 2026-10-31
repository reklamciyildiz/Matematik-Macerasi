package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.util.Log
import com.yigit033.matematikmacarasi.GameType

/**
 * BaseGameActivity'den türemeyen oyun aktiviteleri için istatistik kayıt yardımcısı.
 * Bu sınıf, oyun istatistiklerinin tutarlı bir formatla kaydedilmesini sağlar.
 */
class GameStatisticsHelper(private val context: Context) {

    private val TAG = "GameStatisticsHelper"
    private var gameStatisticsService: GameStatisticsService? = null
    private var dailyTaskManager: DailyTaskManager? = null
    
    init {
        try {
            gameStatisticsService = GameStatisticsService.getInstance(context)
            dailyTaskManager = DailyTaskManager.getInstance(context)
            Log.d(TAG, "GameStatisticsHelper başarıyla başlatıldı")
        } catch (e: Exception) {
            Log.e(TAG, "İnit edilirken hata: ${e.message}", e)
        }
    }

    /**
     * Oyun tamamlandığında istatistikleri kaydeder
     * 
     * @param gameType Oyun türü (GameType enum'u)
     * @param level Oyun seviyesi
     * @param score Oyun puanı
     * @param stars Kazanılan yıldız sayısı
     * @param gameStartTime Oyun başlangıç zamanı (milisaniye)
     * @param taskId İlgili günlük görev ID'si (varsa)
     */
    fun recordGameCompletion(
        gameType: GameType,
        level: Int, 
        score: Int, 
        stars: Int,
        gameStartTime: Long,
        taskId: Int = -1
    ) {
        try {
            // 1. Tutarlı ID formatını oluştur
            val gameId = getNormalizedGameId(gameType, level)
            
            // 2. Oyun süresini hesapla (saniye cinsinden)
            val gameTimeInSeconds = ((System.currentTimeMillis() - gameStartTime) / 1000).toInt()
            
            // 3. Süreyi doğrula ve sınırla (0-3600 saniye arası)
            val validGameTimeSeconds = when {
                gameTimeInSeconds <= 0 -> {
                    Log.w(TAG, "Geçersiz oyun süresi: $gameTimeInSeconds saniye, 1 saniye olarak ayarlandı")
                    1 // Minimum 1 saniye
                }
                gameTimeInSeconds > 3600 -> {
                    Log.w(TAG, "Çok uzun oyun süresi: $gameTimeInSeconds saniye, 3600 saniye ile sınırlandı")
                    3600 // Maksimum 1 saat (3600 saniye)
                }
                else -> gameTimeInSeconds
            }
            
            // 4. GameStatisticsService ile istatistikleri kaydet
            gameStatisticsService?.recordGameCompletion(
                gameType = gameId,
                score = score,
                stars = stars,
                timeSpentSeconds = validGameTimeSeconds
            )
            Log.d(TAG, "GameStatisticsService.recordGameCompletion çağrıldı: ID=$gameId, Skor=$score, Yıldız=$stars, Süre=${validGameTimeSeconds}s")
            
            // 5. Günlük görev ilerlemesini güncelle
            dailyTaskManager?.updateTaskProgressByGameType(gameType)
            
            // 6. Belirli görev ID'si varsa onu da güncelle
            if (taskId > 0) {
                dailyTaskManager?.updateTaskProgress(taskId)
                Log.d(TAG, "Belirli görev güncellendi (ID: $taskId)")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Oyun istatistikleri kaydedilirken hata: ${e.message}", e)
        }
    }
    
    /**
     * Oyun için normalize edilmiş ID oluşturur (BaseGameActivity'deki mantıkla uyumlu)
     */
    private fun getNormalizedGameId(gameType: GameType, level: Int): String {
        val result = if (isBasicOperationGame(gameType)) {
            // Temel işlem oyunları: addition_game_level_1, subtraction_game_level_2 gibi
            val baseGameId = when (gameType) {
                GameType.ADDITION_GAME -> "addition_game"
                GameType.SUBTRACTION_GAME -> "subtraction_game"
                GameType.MULTIPLICATION_GAME -> "multiplication_game"
                GameType.DIVISION_GAME -> "division_game"
                else -> gameType.toString().lowercase().replace("_game", "")
            }
            
            // Seviye bazlı ID oluştur
            "${baseGameId}_level_$level"
        } else {
            // İnteraktif oyunlar: Aktivite adı ve seviye
            val activityId = gameStatisticsService?.getGameIdString(gameType)
            
            // Sayı Avı oyunu için özel kontrol
            if (gameType == GameType.NUMBER_HUNT_GAME) {
                // NumberHuntGameActivity gibi aktivite ismiyle kaydedilmeli
                val activityName = gameStatisticsService?.getGameIdString(gameType) ?: "NumberHuntGameActivity"
                
                // Farklı seviyeler ve kurallar için tutarlı bir format
                "${activityName}_level_$level"
            } else if (level > 1) {
                // Eğer seviye 1'den büyükse, seviye bilgisini ekle
                "${activityId}_level_$level"
            } else {
                // Varsayılan seviye için sadece aktivite adı
                activityId ?: ""
            }
        }
        
        // Sonucu logla
        Log.d(TAG, "getNormalizedGameId: GameType=$gameType, Level=$level → ID=$result")
        return result
    }
    
    /**
     * Verilen oyun türünün temel işlem oyunu olup olmadığını kontrol eder
     */
    private fun isBasicOperationGame(gameType: GameType?): Boolean {
        return gameType == GameType.ADDITION_GAME ||
               gameType == GameType.SUBTRACTION_GAME ||
               gameType == GameType.MULTIPLICATION_GAME ||
               gameType == GameType.DIVISION_GAME
    }
} 