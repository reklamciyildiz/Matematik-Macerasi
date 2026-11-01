package com.yigit033.matematikmacarasi.learning.adaptive

/** Sınıf bir öneridir; oyun seviyesini veya bir önceki kazanımı kalıcı olarak kilitlemez. */
enum class LearningTier(val title: String) {
    TEMEL("Temel"), GELISEN("Gelişen"), USTALIK("Ustalık");
    companion object {
        fun fromSaved(value: String?, fallback: LearningTier = GELISEN): LearningTier =
            entries.firstOrNull { it.name == value } ?: fallback
    }
}

enum class AdventureSkill(val key: String) {
    THREE_COUNTING("count_3"),
    FOUR_COUNTING("count_4"),
    GEOMETRY("geometry_castle"),
    MARKET("market_adventure"),
    SPACE("space_expedition")
}

/** Öğrenci profilinin `user_class` değeri; Ana Sınıfı ve hatalı kayıtlar güvenle ayrıştırılır. */
object GradePolicy {
    fun parse(raw: String): Int? {
        if (raw.trim().equals("Ana Sınıfı", ignoreCase = true)) return 0
        val number = Regex("""^\s*([1-8])\s*(?:\.|\s|$)""").find(raw)?.groupValues?.get(1)
            ?.toIntOrNull()
        return number
    }

    /** Pilot pedagojik öneri; resmi müfredat yeterlilik değerlendirmesi değildir. */
    fun starterTier(grade: Int?, skill: AdventureSkill): LearningTier = when {
        grade == null || grade <= 2 -> LearningTier.TEMEL
        grade == 3 -> LearningTier.GELISEN
        else -> LearningTier.USTALIK
    }
}

data class AdaptiveProgress(
    val autoTier: LearningTier,
    val profileGrade: Int?,
    val manualTier: LearningTier? = null,
    val cleanStreak: Int = 0,
    val supportStreak: Int = 0,
    val completedRuns: Int = 0,
    val totalAttempts: Int = 0,
    val totalErrors: Int = 0,
    val totalHints: Int = 0,
    val lastRunId: Long = -1L
) {
    val currentTier: LearningTier get() = manualTier ?: autoTier
}

/** Üst üste iki anlamlı güçlü/zorlanılan oturum olmadan zorluk değiştirme. */
object AdaptivePolicy {
    fun begin(existing: AdaptiveProgress?, grade: Int?, skill: AdventureSkill): AdaptiveProgress {
        if (existing == null) return AdaptiveProgress(GradePolicy.starterTier(grade, skill), grade)
        if (existing.profileGrade == grade) return existing
        // Sınıf değişimi gelecekteki otomatik öneriyi günceller; geçmiş sayaçlar korunur.
        return existing.copy(
            autoTier = GradePolicy.starterTier(grade, skill),
            profileGrade = grade,
            cleanStreak = 0,
            supportStreak = 0
        )
    }

    fun withManualTier(progress: AdaptiveProgress, selected: LearningTier?): AdaptiveProgress =
        progress.copy(manualTier = selected, cleanStreak = 0, supportStreak = 0)

    fun completed(
        progress: AdaptiveProgress, runId: Long, playedTier: LearningTier,
        attempts: Int, errors: Int, hints: Int
    ): AdaptiveProgress {
        require(runId > 0L && attempts >= 0 && errors in 0..attempts && hints >= 0)
        if (progress.lastRunId == runId) return progress
        val totalled = progress.copy(
            lastRunId = runId, completedRuns = progress.completedRuns + 1,
            totalAttempts = progress.totalAttempts + attempts,
            totalErrors = progress.totalErrors + errors,
            totalHints = progress.totalHints + hints
        )
        // Elle seçilen seviye otomatik değiştirilmez. Kayıt başka seviyeye aitse de yükseltme yapılmaz.
        if (progress.manualTier != null || progress.autoTier != playedTier || attempts < 8) {
            return totalled.copy(cleanStreak = 0, supportStreak = 0)
        }
        val accuracy = if (attempts == 0) 0.0 else (attempts - errors).toDouble() / attempts
        val strong = accuracy >= 0.85 && hints == 0
        val struggling = accuracy < 0.60 || hints >= 3
        val clean = if (strong) progress.cleanStreak + 1 else 0
        val support = if (struggling) progress.supportStreak + 1 else 0
        return when {
            clean >= 2 -> totalled.copy(autoTier = when (playedTier) {
                LearningTier.TEMEL -> LearningTier.GELISEN
                LearningTier.GELISEN -> LearningTier.USTALIK
                LearningTier.USTALIK -> LearningTier.USTALIK
            }, cleanStreak = 0, supportStreak = 0)
            support >= 2 -> totalled.copy(autoTier = when (playedTier) {
                LearningTier.USTALIK -> LearningTier.GELISEN
                LearningTier.GELISEN -> LearningTier.TEMEL
                LearningTier.TEMEL -> LearningTier.TEMEL
            }, cleanStreak = 0, supportStreak = 0)
            else -> totalled.copy(cleanStreak = clean, supportStreak = support)
        }
    }
}
