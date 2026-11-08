package com.yigit033.matematikmacarasi.learning

/**
 * Ö6 product policy: two substantive free introductory lessons per grade,
 * selected from different curriculum topics to demonstrate teaching quality.
 * Existing Play/Cloudflare Premium unlocks the entire lesson catalogue.
 *
 * The free offer is not a time-limited trial and cannot exhaust.
 */
object LearningAccessPolicy {
    const val FREE_INTRO_LESSONS_PER_GRADE = 2

    fun freeLessonIds(grade: Int): Set<String> {
        require(grade in 1..8)
        val topics = GradeCurriculumCatalog.forGrade(grade).topics
            .filter { it.area != TopicArea.REVIEW && it.lessonIds.isNotEmpty() }
        return topics.mapNotNull { it.lessonIds.firstOrNull() }
            .take(FREE_INTRO_LESSONS_PER_GRADE)
            .toSet()
    }

    fun accessible(
        grade: Int, lessonId: String, isPremium: Boolean,
        legacyCompleted: Set<String> = emptySet()
    ): Boolean =
        isPremium || lessonId in freeLessonIds(grade) || lessonId in legacyCompleted

    fun freeCount(grade: Int): Int = freeLessonIds(grade).size
}
