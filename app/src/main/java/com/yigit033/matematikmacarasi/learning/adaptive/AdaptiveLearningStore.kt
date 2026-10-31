package com.yigit033.matematikmacarasi.learning.adaptive

import android.content.Context
import com.yigit033.matematikmacarasi.services.PlayerProfileService

/**
 * v1: mevcut tek oyunculu yerel profile bağlı. Bölge yıldızları ve kayıtlarından bağımsız.
 * Çok oyunculu profil eklenirse ad alanı öğrenci ID'siyle ayrılmalıdır.
 */
class AdaptiveLearningStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "adaptive_learning_v1", Context.MODE_PRIVATE
    )
    private val profile = PlayerProfileService.getInstance(context.applicationContext)

    private fun grade(): Int? = GradePolicy.parse(profile.getUserClass())
    fun gradeLabel(): String = profile.getUserClass()

    private fun loadRaw(skill: AdventureSkill): AdaptiveProgress? {
        val p = skill.key + "_"
        if (!prefs.contains(p + "tier")) return null
        return AdaptiveProgress(
            autoTier = LearningTier.fromSaved(prefs.getString(p + "tier", null)),
            profileGrade = prefs.getInt(p + "grade", -1).takeIf { it >= 0 },
            manualTier = prefs.getString(p + "manual", null)?.let { saved ->
                LearningTier.entries.firstOrNull { it.name == saved }
            },
            cleanStreak = prefs.getInt(p + "clean", 0).coerceAtLeast(0),
            supportStreak = prefs.getInt(p + "support", 0).coerceAtLeast(0),
            completedRuns = prefs.getInt(p + "runs", 0).coerceAtLeast(0),
            totalAttempts = prefs.getInt(p + "attempts", 0).coerceAtLeast(0),
            totalErrors = prefs.getInt(p + "errors", 0).coerceAtLeast(0),
            totalHints = prefs.getInt(p + "hints", 0).coerceAtLeast(0),
            lastRunId = prefs.getLong(p + "last_run", -1L)
        )
    }

    @Synchronized
    private fun write(skill: AdventureSkill, progress: AdaptiveProgress): Boolean {
        val p = skill.key + "_"
        val e = prefs.edit()
            .putString(p + "tier", progress.autoTier.name)
            .putInt(p + "grade", progress.profileGrade ?: -1)
            .putInt(p + "clean", progress.cleanStreak)
            .putInt(p + "support", progress.supportStreak)
            .putInt(p + "runs", progress.completedRuns)
            .putInt(p + "attempts", progress.totalAttempts)
            .putInt(p + "errors", progress.totalErrors)
            .putInt(p + "hints", progress.totalHints)
            .putLong(p + "last_run", progress.lastRunId)
        if (progress.manualTier == null) e.remove(p + "manual")
        else e.putString(p + "manual", progress.manualTier.name)
        return e.commit()
    }

    @Synchronized
    fun progress(skill: AdventureSkill): AdaptiveProgress {
        val old = loadRaw(skill)
        val next = AdaptivePolicy.begin(old, grade(), skill)
        if (old != next) write(skill, next)
        return next
    }

    @Synchronized
    fun selectTier(skill: AdventureSkill, tier: LearningTier?): Boolean {
        val next = AdaptivePolicy.withManualTier(progress(skill), tier)
        return write(skill, next)
    }

    @Synchronized
    fun recordCompletion(skill: AdventureSkill, runId: Long, tier: LearningTier,
                         attempts: Int, errors: Int, hints: Int): Boolean {
        if (runId <= 0 || attempts < 0 || errors !in 0..attempts || hints < 0) return false
        val before = progress(skill)
        if (before.lastRunId == runId) return false
        // Seçilen sınıf oyunun ortasında değişmiş olabilir: oynanan seviye aynı kalır.
        val next = AdaptivePolicy.completed(before, runId, tier, attempts, errors, hints)
        return write(skill, next)
    }
}
