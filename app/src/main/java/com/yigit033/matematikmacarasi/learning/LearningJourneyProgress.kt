package com.yigit033.matematikmacarasi.learning

import android.content.Context
import android.content.SharedPreferences

/**
 * Ö2: A single local source of truth for the existing *pilot* lessons.
 * Preserves old p2_learning_pilot_v1 completion / best / runs keys.
 * This is device-local progress, not cloud sync and not proof of mastery.
 */
class LearningJourneyProgress(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences("p2_learning_pilot_v1", Context.MODE_PRIVATE)

    fun completed(id: String): Boolean = prefs.getBoolean("done_$id", false)
    fun best(id: String): Int? =
        if (prefs.contains("best_$id")) prefs.getInt("best_$id", 0) else null
    fun runs(id: String): Int = prefs.getInt("runs_$id", 0)
    fun lastLesson(): String? = prefs.getString("last_lesson_id", null)
    fun stage(id: String): Int = prefs.getInt("phase_$id", 0).coerceIn(0, 3)
    fun questionIndex(id: String): Int = prefs.getInt("index_$id", 0).coerceIn(0, 7)
    fun wrong(id: String): Int = prefs.getInt("session_wrong_$id", 0).coerceAtLeast(0)
    fun hints(id: String): Int = prefs.getInt("session_hints_$id", 0).coerceAtLeast(0)
    fun seed(id: String): Long = prefs.getLong("session_seed_$id", 0L)

    fun nextReviewAt(id: String): Long = prefs.getLong("review_due_$id", 0L)
    fun lastReviewSignal(id: String): ReviewSignal? =
        prefs.getString("review_signal_$id", null)
            ?.let { value -> ReviewSignal.values().firstOrNull { it.name == value } }
    fun isReviewDue(id: String, now: Long = System.currentTimeMillis()): Boolean =
        LearningReviewPolicy.dueIncludingLegacy(
            completed(id), runs(id), now, nextReviewAt(id)
        )
    fun lastReviewComment(id: String): String? =
        prefs.getString("review_note_$id", null)


    /** Which illustrated example step was already read (0-based). */
    fun workedStep(id: String): Int = prefs.getInt("worked_step_$id", 0).coerceIn(0, 5)
    fun saveWorkedStep(id: String, step: Int) {
        require(step in 0..5)
        prefs.edit().putInt("worked_step_$id", step).apply()
    }
    fun conceptCheckPassed(id: String): Boolean =
        prefs.getBoolean("concept_passed_$id", false)
    fun conceptErrors(id: String): Int =
        prefs.getInt("concept_errors_$id", 0).coerceAtLeast(0)
    fun recordConceptError(id: String) {
        prefs.edit().putInt("concept_errors_$id", conceptErrors(id) + 1).apply()
    }
    fun markConceptCheckPassed(id: String) {
        prefs.edit().putBoolean("concept_passed_$id", true).apply()
    }

    fun open(id: String) {
        val editor = prefs.edit().putString("last_lesson_id", id)
        // A completed session remains in history but replay starts fresh.
        if (stage(id) == 3) {
            editor.putInt("phase_$id", 0)
                .putInt("index_$id", 0)
                .putInt("session_wrong_$id", 0)
                .putInt("session_hints_$id", 0)
                .putInt("worked_step_$id", 0)
                .putBoolean("concept_passed_$id", false)
                .putInt("concept_errors_$id", 0)
                .putLong("session_seed_$id", System.nanoTime().coerceAtLeast(1L))
        } else if (seed(id) == 0L) {
            editor.putLong("session_seed_$id", System.nanoTime().coerceAtLeast(1L))
        }
        editor.apply()
    }

    fun setStage(id: String, next: Int) {
        require(next in 0..3)
        prefs.edit().putInt("phase_$id", next).apply()
    }

    fun setQuestionIndex(id: String, next: Int) {
        require(next in 0..7)
        prefs.edit().putInt("index_$id", next).apply()
    }

    fun recordWrong(id: String) {
        prefs.edit().putInt("session_wrong_$id", wrong(id) + 1).apply()
    }

    fun recordHint(id: String) {
        prefs.edit().putInt("session_hints_$id", hints(id) + 1).apply()
    }

    fun complete(id: String, wrong: Int, hints: Int, totalQuestions: Int = 4) {
        require(totalQuestions in 4..8)
        val accuracy = 100 * totalQuestions / (totalQuestions + wrong.coerceAtLeast(0))
        val advice = LearningReviewPolicy.recommend(
            LearningSessionEvidence(
                wrong.coerceAtLeast(0), hints.coerceAtLeast(0),
                conceptErrors(id), runs(id),
                prefs.getInt("clean_streak_$id", 0).coerceAtLeast(0)
            )
        )
        val now = System.currentTimeMillis()
        prefs.edit()
            .putBoolean("done_$id", true)
            .putInt("best_$id", maxOf(best(id) ?: 0, accuracy))
            .putInt("runs_$id", runs(id) + 1)
            .putInt("last_wrong_$id", wrong)
            .putInt("last_hints_$id", hints)
            .putLong("last_completed_$id", now)
            .putLong("review_due_$id",
                LearningReviewPolicy.nextReviewAt(now, advice.waitDays))
            .putString("review_signal_$id", advice.signal.name)
            .putString("review_note_$id", advice.explanation)
            .putInt("clean_streak_$id", advice.cleanStreak)
            .putInt("phase_$id", 3)
            .apply()
    }
}
