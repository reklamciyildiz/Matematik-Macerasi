package com.yigit033.matematikmacarasi.repositories

import android.content.Context
import android.content.SharedPreferences
import com.yigit033.matematikmacarasi.models.GameProgress

/**
 * LocalGameDataRepository, oyun verilerini yerel olarak saklamak için GameDataRepository
 * arayüzünü SharedPreferences kullanarak implemente eder.
 */
class LocalGameDataRepository(context: Context) : GameDataRepository {
    
    companion object {
        private const val PREFS_NAME = "game_progress"
    }
    
    // SharedPreferences
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    override fun saveProgress(gameId: String, level: Int, score: Int, stars: Int) {
        try {
            val editor = prefs.edit()
            
            // Önceki kaydedilmiş yıldızları al
            val previousStars = prefs.getInt("${gameId}_stars", 0)
            
            editor.putInt("${gameId}_level", level)
            editor.putInt("${gameId}_score", score)
            
            // Sadece daha yüksek yıldız sayısı elde edilmişse güncelle
            if (stars > previousStars) {
                android.util.Log.d("LocalGameDataRepository", 
                    "Daha yüksek yıldız sayısı elde edildi: $gameId için $previousStars -> $stars")
                
                // Eklenmesi gereken yıldız farkı
                val starDifference = stars - previousStars
                
                // Toplam yıldız sayısını güncelle
                val totalStars = prefs.getInt("total_stars", 0)
                val newTotalStars = totalStars + starDifference
                editor.putInt("total_stars", newTotalStars)
                
                // Oyun için yeni yıldız sayısını güncelle
                editor.putInt("${gameId}_stars", stars)
                
                android.util.Log.d("LocalGameDataRepository", 
                    "Toplam yıldızlar güncelleniyor: $totalStars -> $newTotalStars (fark: +$starDifference)")
            } else {
                android.util.Log.d("LocalGameDataRepository", 
                    "Yıldız güncellemesi yapılmadı: Mevcut ($previousStars) >= Yeni ($stars)")
            }
            
            // Yüksek skoru güncelle
            val currentHighScore = prefs.getInt("${gameId}_high_score", 0)
            if (score > currentHighScore) {
                editor.putInt("${gameId}_high_score", score)
                android.util.Log.d("LocalGameDataRepository", 
                    "Yüksek skor güncellendi: $gameId için $currentHighScore -> $score")
            }
            
            // Değişiklikleri hemen uygula (apply yerine commit kullan)
            val result = editor.commit()
            
            if (result) {
                android.util.Log.d("LocalGameDataRepository", 
                    "İlerleme kaydedildi: gameId=$gameId, level=$level, score=$score, stars=$stars")
                
                // TOPLAM YILDIZ GÜNCELLENDİYSE EVENTBUS'I TETİKLE
                if (stars > previousStars) { 
                    try {
                        val totalStars = prefs.getInt("total_stars", 0)
                        com.yigit033.matematikmacarasi.services.EventBus.updateStars(totalStars)
                        android.util.Log.d("LocalGameDataRepository", "EventBus tetiklendi: Toplam Yıldız=$totalStars")
                    } catch (e: Exception) {
                        android.util.Log.e("LocalGameDataRepository", 
                            "EventBus güncellenirken hata: ${e.message}", e)
                    }
                }
            } else {
                android.util.Log.e("LocalGameDataRepository", 
                    "İlerleme kaydedilemedi: SharedPreferences commit başarısız oldu")
            }
        } catch (e: Exception) {
            android.util.Log.e("LocalGameDataRepository", 
                "İlerleme kaydedilirken hata: ${e.message}", e)
            e.printStackTrace()
        }
    }
    
    override fun getProgress(gameId: String): GameProgress {
        try {
            return GameProgress(
                gameId = gameId,
                level = prefs.getInt("${gameId}_level", 1),
                stars = prefs.getInt("${gameId}_stars", 0),
                highScore = prefs.getInt("${gameId}_high_score", 0)
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return GameProgress(gameId = gameId)
        }
    }
    
    override fun getTotalStars(): Int {
        try {
            return prefs.getInt("total_stars", 0)
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }
    
    override fun isGameUnlocked(requiredStars: Int): Boolean {
        try {
            val totalStars = getTotalStars()
            return totalStars >= requiredStars
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
} 