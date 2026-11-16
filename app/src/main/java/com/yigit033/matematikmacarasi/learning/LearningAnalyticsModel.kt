package com.yigit033.matematikmacarasi.learning

/**
 * Profile 3.0. Only evidence attributable to the selected grade is used.
 * Historic shared lesson IDs cannot safely be assigned retrospectively.
 * This is an app practice report, NOT a curricular mastery assessment.
 */
data class LessonLearningEvidence(
    val lessonId: String,
    val title: String,
    val completed: Boolean,
    val sessions: Int,
    val bestPercent: Int?,
    val reviewDue: Boolean,
    val focus: String?,
    val lastWrong: Int,
    val lastHints: Int
)

data class GradeLearningSnapshot(
    val grade: Int,
    val lessons: List<LessonLearningEvidence>,
    val unattributedHistoricalSharedLessons: Int
) {
    init { require(grade in 1..8) }
    val totalLessons: Int get() = lessons.size
    val completed: Int get() = lessons.count { it.completed }
    val sessions: Int get() = lessons.sumOf { it.sessions.coerceAtLeast(0) }
    val due: Int get() = lessons.count { it.reviewDue }
    val bestPercent: Int? get() = lessons.mapNotNull { it.bestPercent }.maxOrNull()
    val recentlyDifficult: List<LessonLearningEvidence>
        get() = lessons.filter { it.focus != null && (it.lastWrong > 0 || it.lastHints > 0) }
            .sortedWith(compareByDescending<LessonLearningEvidence> { it.lastWrong + it.lastHints }
                .thenBy { it.lessonId })
    val completionFraction: Float
        get() = if (totalLessons == 0) 0f else completed.toFloat() / totalLessons
}

object LearningAnalyticsPolicy {
    fun canViewSummary(isPremium: Boolean): Boolean = true
    fun canViewTopicBreakdown(isPremium: Boolean): Boolean = isPremium
    fun canViewMistakeFocus(isPremium: Boolean): Boolean = isPremium

    fun accuratePercent(correct: Int, wrong: Int): Int =
        if (correct <= 0) 0 else (100L * correct / (correct.toLong() + wrong.coerceAtLeast(0)))
            .toInt().coerceIn(0, 100)

    fun shouldAttributeLegacy(grade: Int, lessonId: String): Boolean =
        grade in 1..8 && lessonId.startsWith("g${grade}_")

    fun topicCompleted(lessonIds: Collection<String>, lessons: List<LessonLearningEvidence>): Int {
        val ids = lessonIds.toSet()
        return lessons.count { it.lessonId in ids && it.completed }
    }
}
