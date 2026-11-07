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
    fun questionIndex(id: String): Int = prefs.getInt("index_$id", 0).coerceIn(0, 3)
    fun wrong(id: String): Int = prefs.getInt("session_wrong_$id", 0).coerceAtLeast(0)
    fun hints(id: String): Int = prefs.getInt("session_hints_$id", 0).coerceAtLeast(0)
    fun seed(id: String): Long = prefs.getLong("session_seed_$id", 0L)

    /** Which illustrated example step was already read (0-based). */
    fun workedStep(id: String): Int = prefs.getInt("worked_step_$id", 0).coerceIn(0, 5)
    fun saveWorkedStep(id: String, step: Int) {
        require(step in 0..5)
        prefs.edit().putInt("worked_step_$id", step).apply()
    }
    fun conceptCheckPassed(id: String): Boolean =
        prefs.getBoolean("concept_passed_$id", false)
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
        require(next in 0..3)
        prefs.edit().putInt("index_$id", next).apply()
    }

    fun recordWrong(id: String) {
        prefs.edit().putInt("session_wrong_$id", wrong(id) + 1).apply()
    }

    fun recordHint(id: String) {
        prefs.edit().putInt("session_hints_$id", hints(id) + 1).apply()
    }

    fun complete(id: String, wrong: Int, hints: Int) {
        val accuracy = 400 / (4 + wrong.coerceAtLeast(0))
        prefs.edit()
            .putBoolean("done_$id", true)
            .putInt("best_$id", maxOf(best(id) ?: 0, accuracy))
            .putInt("runs_$id", runs(id) + 1)
            .putInt("last_wrong_$id", wrong)
            .putInt("last_hints_$id", hints)
            .putInt("phase_$id", 3)
            .apply()
    }
}
