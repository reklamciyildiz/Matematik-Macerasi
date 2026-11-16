package com.yigit033.matematikmacarasi.learning

import android.content.Context
import android.content.SharedPreferences

/**
 * Local, grade-scoped analytics layered over the existing Ö2 progress.
 *
 * Do not migrate the legacy shared IDs ("add_20", "multiply" etc.) into a
 * chosen class: they did not record the grade, so such a migration would
 * fabricate achievement. Grade-unique "g3_*" history is safe to display.
 *
 * No network, no advertising ID, no personal data or child identifier.
 * Reuses the existing profile's local device scope.
 */
class LearningAnalyticsStore(context: Context) {
    private val prefs: SharedPreferences = context.applicationContext
        .getSharedPreferences("p3_learning_analytics_v1", Context.MODE_PRIVATE)
    private val legacy: SharedPreferences = context.applicationContext
        .getSharedPreferences("p2_learning_pilot_v1", Context.MODE_PRIVATE)

    private fun key(grade: Int, lessonId: String): String {
        require(grade in 1..8 && lessonId.isNotBlank() && lessonId.length < 160)
        return "grade_${grade}_lesson_$lessonId"
    }

    @Synchronized
    fun recordCompleted(
        grade: Int, lessonId: String, seed: Long,
        percent: Int, wrong: Int, hints: Int,
        reviewDueAt: Long, focus: String?
    ) {
        val k = key(grade, lessonId)
        require(seed > 0L)
        // The completion callback may be re-entered after an Activity restart.
        // A seed identifies one lesson session, so count it only once.
        if (prefs.getLong("${k}_seed", 0L) == seed) return
        val previousBest = prefs.getInt("${k}_best", 0)
        prefs.edit()
            .putLong("${k}_seed", seed)
            .putBoolean("${k}_done", true)
            .putInt("${k}_sessions", prefs.getInt("${k}_sessions", 0) + 1)
            .putInt("${k}_best", maxOf(previousBest, percent.coerceIn(0, 100)))
            .putInt("${k}_last_wrong", wrong.coerceAtLeast(0))
            .putInt("${k}_last_hints", hints.coerceAtLeast(0))
            .putLong("${k}_review_due", reviewDueAt.coerceAtLeast(0L))
            .apply {
                if (focus.isNullOrBlank()) remove("${k}_focus")
                else putString("${k}_focus", focus.take(100))
            }
            .apply()
    }

    fun snapshot(
        grade: Int, lessons: List<Lesson>,
        now: Long = System.currentTimeMillis()
    ): GradeLearningSnapshot {
        require(grade in 1..8)
        val seen = HashSet<String>()
        val evidence = lessons.filter { seen.add(it.id) }.map { lesson ->
            val id = lesson.id
            val k = key(grade, id)
            val legacySafe = LearningAnalyticsPolicy.shouldAttributeLegacy(grade, id)
            // The legacy grade-specific record includes new sessions because
            // the old learning engine remains the authoritative progress store.
            // Never sum old and new sessions for those IDs: double counting.
            val completed = if (legacySafe) legacy.getBoolean("done_$id", false)
                else prefs.getBoolean("${k}_done", false)
            val sessions = if (legacySafe) legacy.getInt("runs_$id", 0)
                else prefs.getInt("${k}_sessions", 0)
            val best = if (legacySafe) {
                if (legacy.contains("best_$id")) legacy.getInt("best_$id", 0) else null
            } else {
                if (prefs.contains("${k}_best")) prefs.getInt("${k}_best", 0) else null
            }
            val dueAt = if (legacySafe) legacy.getLong("review_due_$id", 0L)
                else prefs.getLong("${k}_review_due", 0L)
            val focus = if (legacySafe) legacy.getString("p1_last_focus_$id", null)
                else prefs.getString("${k}_focus", null)
            val wrong = if (legacySafe) legacy.getInt("last_wrong_$id", 0)
                else prefs.getInt("${k}_last_wrong", 0)
            val hints = if (legacySafe) legacy.getInt("last_hints_$id", 0)
                else prefs.getInt("${k}_last_hints", 0)
            LessonLearningEvidence(
                lessonId = id, title = lesson.title,
                completed = completed, sessions = sessions.coerceAtLeast(0),
                bestPercent = best?.coerceIn(0, 100),
                reviewDue = completed && dueAt > 0 && dueAt <= now,
                focus = focus?.takeIf { it.isNotBlank() },
                lastWrong = wrong.coerceAtLeast(0),
                lastHints = hints.coerceAtLeast(0)
            )
        }
        val unassignedShared = evidence.count { item ->
            !LearningAnalyticsPolicy.shouldAttributeLegacy(grade, item.lessonId) &&
                legacy.getBoolean("done_${item.lessonId}", false) &&
                prefs.getInt("${key(grade, item.lessonId)}_sessions", 0) <= 0
        }
        return GradeLearningSnapshot(grade, evidence, unassignedShared)
    }
}
