package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

/**
 * Günlük oyun sınırlarını yöneten servis sınıfı
 * Premium olmayan kullanıcılar için belirli oyunlarda günlük sınır uygular
 */
class DailyGameLimitService private constructor(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "daily_game_limits"
        private const val TAG = "DailyGameLimitService"
        
        // Oyun sınırları (premium olmayan kullanıcılar için)
        private const val MATH_MAZE_DAILY_LIMIT = 5
        
        // Tarih formatı
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        @Volatile
        private var instance: DailyGameLimitService? = null
        
        fun getInstance(context: Context): DailyGameLimitService {
            return instance ?: synchronized(this) {
                instance ?: DailyGameLimitService(context.applicationContext).also { 
                    instance = it 
                }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Bugünün tarihini string olarak döndürür
     */
    private fun getTodayString(): String {
        return dateFormat.format(Date())
    }
    
    /**
     * Matematik Labirenti oyunu için günlük sınırı kontrol eder
     * @return true: Oyun oynayabilir, false: Sınır aşıldı
     */
    fun canPlayMathMaze(isPremium: Boolean): Boolean {
        return try {
            // Premium kullanıcılar sınırsız oynayabilir
            if (isPremium) {
                Log.d(TAG, "Premium kullanıcı - Matematik Labirenti sınırsız")
                return true
            }
            
            val today = getTodayString()
            val playCountKey = "math_maze_count_$today"
            val currentCount = sharedPreferences.getInt(playCountKey, 0)
            
            val canPlay = currentCount < MATH_MAZE_DAILY_LIMIT
            Log.d(TAG, "Matematik Labirenti günlük kontrol: $currentCount/$MATH_MAZE_DAILY_LIMIT, Oynayabilir: $canPlay")
            
            canPlay
        } catch (e: Exception) {
            Log.e(TAG, "Matematik Labirenti sınır kontrolü hatası: ${e.message}", e)
            true // Hata durumunda oynamaya izin ver
        }
    }
    
    /**
     * Matematik Labirenti oyunu oynandığında çağırılır
     */
    fun recordMathMazePlay(): Boolean {
        return try {
            val today = getTodayString()
            val playCountKey = "math_maze_count_$today"
            val currentCount = sharedPreferences.getInt(playCountKey, 0)
            val newCount = currentCount + 1
            
            val success = sharedPreferences.edit()
                .putInt(playCountKey, newCount)
                .commit()
            
            if (success) {
                Log.d(TAG, "Matematik Labirenti oyun sayısı kaydedildi: $newCount")
            } else {
                Log.e(TAG, "Matematik Labirenti oyun sayısı kaydedilemedi")
            }
            
            success
        } catch (e: Exception) {
            Log.e(TAG, "Matematik Labirenti oyun kaydı hatası: ${e.message}", e)
            false
        }
    }
    
    /**
     * Matematik Labirenti için bugün kaç oyun oynandığını döndürür
     */
    fun getMathMazePlayCount(): Int {
        return try {
            val today = getTodayString()
            val playCountKey = "math_maze_count_$today"
            sharedPreferences.getInt(playCountKey, 0)
        } catch (e: Exception) {
            Log.e(TAG, "Matematik Labirenti oyun sayısı alınırken hata: ${e.message}", e)
            0
        }
    }
    
    /**
     * Matematik Labirenti için kalan oyun hakkını döndürür
     */
    fun getRemainingMathMazePlays(isPremium: Boolean): Int {
        return try {
            if (isPremium) {
                return Int.MAX_VALUE // Sınırsız
            }
            
            val currentCount = getMathMazePlayCount()
            val remaining = MATH_MAZE_DAILY_LIMIT - currentCount
            maxOf(0, remaining)
        } catch (e: Exception) {
            Log.e(TAG, "Kalan oyun hakkı hesaplanırken hata: ${e.message}", e)
            0
        }
    }
    
    /**
     * Matematik Labirenti günlük sınırını döndürür
     */
    fun getMathMazeDailyLimit(): Int = MATH_MAZE_DAILY_LIMIT
    
    /**
     * Eski günlerin verilerini temizler (performans için)
     */
    fun cleanupOldData() {
        try {
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -7) // 7 gün öncesi
            val cutoffDate = dateFormat.format(calendar.time)
            
            val allKeys = sharedPreferences.all.keys.toList()
            val editor = sharedPreferences.edit()
            var removedCount = 0
            
            for (key in allKeys) {
                if (key.contains("_count_")) {
                    val dateInKey = key.substringAfterLast("_")
                    if (dateInKey < cutoffDate) {
                        editor.remove(key)
                        removedCount++
                    }
                }
            }
            
            if (removedCount > 0) {
                editor.apply()
                Log.d(TAG, "Eski günlük veriler temizlendi: $removedCount kayıt")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Eski veri temizleme hatası: ${e.message}", e)
        }
    }
    
    /**
     * Test için tüm verileri sıfırlar
     */
    fun resetAllLimits() {
        try {
            sharedPreferences.edit().clear().apply()
            Log.d(TAG, "Tüm günlük sınır verileri sıfırlandı")
        } catch (e: Exception) {
            Log.e(TAG, "Sınır verileri sıfırlanırken hata: ${e.message}", e)
        }
    }
} 