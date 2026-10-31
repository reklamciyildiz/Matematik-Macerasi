package com.yigit033.matematikmacarasi.games.sayiavi2

import android.content.Context
import android.content.SharedPreferences

/**
 * Sayı Avı 2.0 İlerleme ve Veri Saklama Servisi.
 * Tamamen yerel, offline çalışır.
 * SharedPreferences (`sayi_avi_2_preferences`) kullanarak:
 * - Bölümün ilk tamamlanma durumunu (idempotent ödül için)
 * - Toplam tamamlama sayısını
 * - Toplanan harita parçalarını
 * - Son kalınan aşamayı (uygulamadan çıkıp dönüldüğünde süreç güvenliği) saklar.
 */
class SayiAviStorage private constructor(context: Context) {

    companion object {
        private const val PREFS_NAME = "sayi_avi_2_preferences"

        private const val KEY_MISSION_COMPLETED = "mission_1_completed"
        private const val KEY_STAR_AWARDED = "mission_1_star_awarded"
        private const val KEY_TOTAL_COMPLETIONS = "mission_1_total_completions"
        private const val KEY_MAP_PIECE_1 = "mission_1_map_piece_1"
        private const val KEY_MAP_PIECE_2 = "mission_1_map_piece_2"
        private const val KEY_LAST_STAGE = "mission_1_last_stage"
        private const val KEY_BEST_TIME_SEC = "mission_1_best_time_sec"

        @Volatile
        private var INSTANCE: SayiAviStorage? = null

        fun getInstance(context: Context): SayiAviStorage {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SayiAviStorage(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Bölüm daha önce en az bir kez tamamlandı mı?
     */
    fun isMissionCompleted(): Boolean = prefs.getBoolean(KEY_MISSION_COMPLETED, false)

    /**
     * Yıldız ödülü daha önce verildi mi?
     * Bu değer true ise sonraki tamamlamalarda tekrar yıldız verilmez (idempotent).
     */
    fun isStarAwarded(): Boolean = prefs.getBoolean(KEY_STAR_AWARDED, false)

    /**
     * Toplam tamamlama sayısı
     */
    fun getTotalCompletions(): Int = prefs.getInt(KEY_TOTAL_COMPLETIONS, 0)

    /**
     * 1. Harita parçası kazanıldı mı?
     */
    fun hasMapPiece1(): Boolean = prefs.getBoolean(KEY_MAP_PIECE_1, false)

    /**
     * 2. Harita parçası kazanıldı mı?
     */
    fun hasMapPiece2(): Boolean = prefs.getBoolean(KEY_MAP_PIECE_2, false)

    /**
     * 1. Harita parçasını kaydet
     */
    fun setMapPiece1(collected: Boolean) {
        prefs.edit().putBoolean(KEY_MAP_PIECE_1, collected).apply()
    }

    /**
     * 2. Harita parçasını kaydet
     */
    fun setMapPiece2(collected: Boolean) {
        prefs.edit().putBoolean(KEY_MAP_PIECE_2, collected).apply()
    }

    /**
     * Son kalınan aşamayı kaydet (örn. "STAGE_A", "STAGE_B", "STAGE_C", "COMPLETED")
     */
    fun saveLastStage(stageName: String) {
        prefs.edit().putString(KEY_LAST_STAGE, stageName).apply()
    }

    fun getLastStage(): String? = prefs.getString(KEY_LAST_STAGE, null)

    /**
     * Bölüm tamamlandığında çağrılır:
     * - Tamamlama sayısını artırır
     * - Eğer ilk tamamlama ise yıldız hakkını kaydeder ve true döndürür
     * - Replay ise false döndürür (0 yıldız)
     */
    fun recordMissionCompletion(timeSpentSeconds: Long): Boolean {
        val alreadyAwarded = isStarAwarded()
        val currentCount = getTotalCompletions()
        val bestTime = prefs.getLong(KEY_BEST_TIME_SEC, Long.MAX_VALUE)

        val editor = prefs.edit()
        editor.putBoolean(KEY_MISSION_COMPLETED, true)
        editor.putBoolean(KEY_MAP_PIECE_1, true)
        editor.putBoolean(KEY_MAP_PIECE_2, true)
        editor.putInt(KEY_TOTAL_COMPLETIONS, currentCount + 1)
        editor.putString(KEY_LAST_STAGE, "COMPLETED")

        if (timeSpentSeconds in 1 until bestTime) {
            editor.putLong(KEY_BEST_TIME_SEC, timeSpentSeconds)
        }

        val isFirstTime = !alreadyAwarded
        if (isFirstTime) {
            editor.putBoolean(KEY_STAR_AWARDED, true)
        }

        editor.apply()
        return isFirstTime
    }

    /**
     * Bölümü baştan oynamak için geçici harita parçası durumunu sıfırlar (kazanılmış yıldız korunur)
     */
    fun resetForReplay() {
        prefs.edit()
            .putBoolean(KEY_MAP_PIECE_1, false)
            .putBoolean(KEY_MAP_PIECE_2, false)
            .putString(KEY_LAST_STAGE, "STAGE_A")
            .apply()
    }
}

