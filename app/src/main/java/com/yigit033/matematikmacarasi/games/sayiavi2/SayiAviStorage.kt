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
        private const val RUN_SEED = "g2_run_seed"
        private const val RUN_START = "g2_run_start"
        private const val RUN_COUNTERS = "g2_run_counters"
        private const val ROUTE_STEP = "g2_route_step"
        private const val GROUP_DONE = "g3_group_done"
        private const val GROUP_PARTIAL = "g3_group_partial"
        private const val SEAL_INDEX = "g3_seal_index"
        private const val LAST_SUMMARY = "g2_last_summary"

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

    /** StarManager para kaydını doğruladıktan sonra işaretlenir; yarım kalmış ödül sonraki açılışta telafi edilir. */
    fun markStarAwarded() {
        if (isMissionCompleted()) prefs.edit().putBoolean(KEY_STAR_AWARDED, true).apply()
    }

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

    fun getRunSeed(): Long = prefs.getLong(RUN_SEED, 0L)
    fun getRunStart(): Long = prefs.getLong(RUN_START, System.currentTimeMillis())

    fun beginRun(seed: Long, start: Long) {
        prefs.edit()
            .putLong(RUN_SEED, seed).putLong(RUN_START, start)
            .putString(KEY_LAST_STAGE, "STAGE_GROUP")
            .putBoolean(KEY_MAP_PIECE_1, false).putBoolean(KEY_MAP_PIECE_2, false)
            .putString(RUN_COUNTERS, SayiAviRunCounters().encode())
            .putInt(ROUTE_STEP, 0)
            .putInt(GROUP_DONE, 0).putInt(GROUP_PARTIAL, 0)
            .putInt(SEAL_INDEX, 0)
            .apply()
    }

    fun getRunCounters(): SayiAviRunCounters =
        SayiAviRunCounters.decode(prefs.getString(RUN_COUNTERS, null)) ?: SayiAviRunCounters()

    fun saveRunCounters(value: SayiAviRunCounters) {
        prefs.edit().putString(RUN_COUNTERS, value.encode()).apply()
    }

    /** Yeni bölümlerde ara kayıt: kabuklar ile mühür sorusu kalınan yerden açılır. */
    fun getFinishedGroups(): Int = prefs.getInt(GROUP_DONE, 0).coerceIn(0, 4)
    fun getPartialShells(): Int = prefs.getInt(GROUP_PARTIAL, 0).coerceIn(0, 2)
    fun saveGroupingProgress(groups: Int, partial: Int) {
        prefs.edit().putInt(GROUP_DONE, groups.coerceIn(0, 4))
            .putInt(GROUP_PARTIAL, partial.coerceIn(0, 2)).apply()
    }
    fun getSealQuestionIndex(): Int = prefs.getInt(SEAL_INDEX, 0).coerceIn(0, 2)
    fun saveSealQuestionIndex(index: Int) {
        prefs.edit().putInt(SEAL_INDEX, index.coerceIn(0, 2)).apply()
    }

    fun getRouteStep(): Int = prefs.getInt(ROUTE_STEP, 0).coerceIn(0, 4)

    fun saveRouteStep(value: Int) {
        prefs.edit().putInt(ROUTE_STEP, value.coerceIn(0, 4)).apply()
    }

    fun getLastSummary(): SayiAviLearningSummary? {
        val parts = prefs.getString(LAST_SUMMARY, null)?.split("|") ?: return null
        if (parts.size != 2) return null
        val run = SayiAviRunCounters.decode(parts[0]) ?: return null
        val time = parts[1].toLongOrNull() ?: return null
        return SayiAviLearningSummary(run, time)
    }

    /**
     * Bölüm tamamlandığında çağrılır:
     * - Tamamlama sayısını artırır
     * - Eğer ilk tamamlama ise yıldız hakkını kaydeder ve true döndürür
     * - Replay ise false döndürür (0 yıldız)
     */
    fun recordMissionCompletion(timeSpentSeconds: Long, summary: SayiAviLearningSummary): Boolean {
        // Aynı koşunun tamamlanması tekrar sayılmamalı.
        if (getLastStage() == "COMPLETED") return false
        val alreadyAwarded = isStarAwarded()
        val currentCount = getTotalCompletions()
        val bestTime = prefs.getLong(KEY_BEST_TIME_SEC, Long.MAX_VALUE)

        val editor = prefs.edit()
        editor.putBoolean(KEY_MISSION_COMPLETED, true)
        editor.putBoolean(KEY_MAP_PIECE_1, true)
        editor.putBoolean(KEY_MAP_PIECE_2, true)
        editor.putInt(KEY_TOTAL_COMPLETIONS, currentCount + 1)
        editor.putString(KEY_LAST_STAGE, "COMPLETED")
        editor.putString(LAST_SUMMARY, summary.counters.encode() + "|" + summary.elapsedSeconds)

        if (timeSpentSeconds in 1 until bestTime) {
            editor.putLong(KEY_BEST_TIME_SEC, timeSpentSeconds)
        }

        // Ödül hakkını burada tüketme: StarManager.addStars başarısız olabilir.
        // Aktivite ödül başarıyla kaydedildiğinde markStarAwarded() çağırır.
        editor.apply()
        return !alreadyAwarded
    }

    /**
     * Bölümü baştan oynamak için geçici harita parçası durumunu sıfırlar (kazanılmış yıldız korunur)
     */
    fun resetForReplay() {
        prefs.edit()
            .putBoolean(KEY_MAP_PIECE_1, false)
            .putBoolean(KEY_MAP_PIECE_2, false)
            .putString(KEY_LAST_STAGE, "STAGE_A")
            .putInt(GROUP_DONE, 0).putInt(GROUP_PARTIAL, 0).putInt(SEAL_INDEX, 0)
            .apply()
    }
}

