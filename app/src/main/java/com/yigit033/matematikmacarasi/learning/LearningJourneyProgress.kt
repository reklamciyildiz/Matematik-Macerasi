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

    // P1 keys are additive. Never replace legacy done/best/runs/phase/seed.
    private fun p1(id: String, suffix: String) = "p1_${suffix}_$id"

    fun adaptiveEnabled(id: String): Boolean =
        prefs.getBoolean(p1(id, "enabled"), false)

    fun practiceBand(id: String, index: Int): PracticeBand {
        if (!adaptiveEnabled(id)) return PracticeBand.CORE // preserve old active sessions
        return PracticeBand.values().getOrElse(
            prefs.getInt(p1(id, "band_$index"), PracticeBand.CORE.ordinal)
        ) { PracticeBand.CORE }
    }

    fun questionMistakes(id: String): Int =
        prefs.getInt(p1(id, "current_wrong"), 0).coerceAtLeast(0)

    fun lastWrongChoice(id: String): Int? =
        if (prefs.contains(p1(id, "last_choice"))) prefs.getInt(p1(id, "last_choice"), 0) else null

    fun supportActive(id: String): Boolean =
        prefs.getBoolean(p1(id, "support_active"), false)

    fun supportUsed(id: String): Boolean =
        prefs.getBoolean(p1(id, "support_used"), false)

    fun currentHintUsed(id: String): Boolean =
        prefs.getBoolean(p1(id, "hint_current"), false)

    fun openSupport(id: String) {
        prefs.edit()
            .putBoolean(p1(id, "support_active"), true)
            .putBoolean(p1(id, "support_used"), true)
            .putBoolean(p1(id, "hint_current"), true)
            .apply()
    }

    fun closeSupport(id: String) {
        prefs.edit().putBoolean(p1(id, "support_active"), false).apply()
    }

    fun recordAdaptiveMistake(id: String, choice: Int, focus: PracticeFocus, isMainQuestion: Boolean) {
        val editor = prefs.edit()
            .putInt(p1(id, "current_wrong"), questionMistakes(id) + 1)
            .putInt(p1(id, "focus_${focus.name}"),
                prefs.getInt(p1(id, "focus_${focus.name}"), 0) + 1)
        if (isMainQuestion) editor.putInt(p1(id, "last_choice"), choice)
        editor.apply()
    }

    fun sessionFocus(id: String): PracticeFocus? =
        PracticeFocus.values().map { it to prefs.getInt(p1(id, "focus_${it.name}"), 0) }
            .filter { it.second > 0 }
            .maxByOrNull { it.second }?.first

    fun lastSessionFocus(id: String): PracticeFocus? =
        prefs.getString(p1(id, "last_focus"), null)
            ?.let { name -> PracticeFocus.values().firstOrNull { it.name == name } }

    fun setNextQuestionBand(id: String, next: Int, band: PracticeBand) {
        require(next in 1..7)
        prefs.edit()
            .putInt(p1(id, "band_$next"), band.ordinal)
            .putInt(p1(id, "current_wrong"), 0)
            .remove(p1(id, "last_choice"))
            .putBoolean(p1(id, "support_active"), false)
            .putBoolean(p1(id, "support_used"), false)
            .putBoolean(p1(id, "hint_current"), false)
            .apply()
    }



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
    fun recordConceptError(id: String, focus: PracticeFocus? = null) {
        val editor = prefs.edit().putInt("concept_errors_$id", conceptErrors(id) + 1)
        if (focus != null) {
            editor.putInt(p1(id, "focus_${focus.name}"),
                prefs.getInt(p1(id, "focus_${focus.name}"), 0) + 1)
        }
        editor.apply()
    }
    fun markConceptCheckPassed(id: String) {
        prefs.edit().putBoolean("concept_passed_$id", true).apply()
    }

    fun open(id: String) {
        val editor = prefs.edit().putString("last_lesson_id", id)
        // A completed session remains in history but replay starts fresh.
        if (stage(id) == 3) {
            val initialBand = AdaptiveLearningPolicy.initialBand(
                runs(id),
                prefs.getInt("last_wrong_$id", 0),
                prefs.getInt("last_hints_$id", 0),
                prefs.getInt("last_concept_errors_$id", 0)
            )
            editor.putBoolean(p1(id, "enabled"), true)
                .putInt(p1(id, "band_0"), initialBand.ordinal)
                .putInt(p1(id, "current_wrong"), 0)
                .remove(p1(id, "last_choice"))
                .putBoolean(p1(id, "support_active"), false)
                .putBoolean(p1(id, "support_used"), false)
                .putBoolean(p1(id, "hint_current"), false)
            PracticeFocus.values().forEach { editor.remove(p1(id, "focus_${it.name}")) }
            editor.putInt("phase_$id", 0)
                .putInt("index_$id", 0)
                .putInt("session_wrong_$id", 0)
                .putInt("session_hints_$id", 0)
                .putInt("worked_step_$id", 0)
                .putBoolean("concept_passed_$id", false)
                .putInt("concept_errors_$id", 0)
                .putLong("session_seed_$id", System.nanoTime().coerceAtLeast(1L))
        } else if (seed(id) == 0L) {
            val initialBand = AdaptiveLearningPolicy.initialBand(
                runs(id),
                prefs.getInt("last_wrong_$id", 0),
                prefs.getInt("last_hints_$id", 0),
                prefs.getInt("last_concept_errors_$id", 0)
            )
            editor.putLong("session_seed_$id", System.nanoTime().coerceAtLeast(1L))
                .putBoolean(p1(id, "enabled"), true)
                .putInt(p1(id, "band_0"), initialBand.ordinal)
                .putInt(p1(id, "current_wrong"), 0)
                .remove(p1(id, "last_choice"))
                .putBoolean(p1(id, "support_active"), false)
                .putBoolean(p1(id, "support_used"), false)
                .putBoolean(p1(id, "hint_current"), false)
            PracticeFocus.values().forEach { editor.remove(p1(id, "focus_${it.name}")) }
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
        prefs.edit()
            .putInt("session_hints_$id", hints(id) + 1)
            .putBoolean(p1(id, "hint_current"), true)
            .apply()
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
        val focus = sessionFocus(id)
        val focusAttempts = focus?.let { prefs.getInt(p1(id, "focus_${it.name}"), 0) } ?: 0
        val targeted = focus?.let { AdaptiveLearningPolicy.targetedReview(it) }
        val reviewDays = if (focusAttempts >= 2) 1 else advice.waitDays
        val reviewNote = listOfNotNull(advice.explanation, targeted).joinToString(" ")
        val editor = prefs.edit()
            .putBoolean("done_$id", true)
            .putInt("best_$id", maxOf(best(id) ?: 0, accuracy))
            .putInt("runs_$id", runs(id) + 1)
            .putInt("last_wrong_$id", wrong)
            .putInt("last_hints_$id", hints)
            .putLong("last_completed_$id", now)
            .putLong("review_due_$id",
                LearningReviewPolicy.nextReviewAt(now, reviewDays))
            .putString("review_signal_$id", advice.signal.name)
            .putString("review_note_$id", reviewNote)
            .putInt("clean_streak_$id", advice.cleanStreak)
            .putInt("last_concept_errors_$id", conceptErrors(id))
            .putInt("phase_$id", 3)
        if (focus != null) editor.putString(p1(id, "last_focus"), focus.name)
        else editor.remove(p1(id, "last_focus"))
        editor.apply()
    }
}
