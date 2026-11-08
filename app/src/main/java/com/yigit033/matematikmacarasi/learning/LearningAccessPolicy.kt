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
        val first = topics.firstOrNull() ?: return emptySet()
        // Show the teaching experience in two separate mathematical strands.
        // E.g. numbers + operations, not two adjacent number chapters.
        val second = topics.firstOrNull { it.area != first.area }
            ?: topics.firstOrNull { it.id != first.id }
        return listOfNotNull(
            first.lessonIds.firstOrNull(),
            second?.lessonIds?.firstOrNull()
        ).take(FREE_INTRO_LESSONS_PER_GRADE).toSet()
    }

    fun accessible(
        grade: Int, lessonId: String, isPremium: Boolean,
        legacyCompleted: Set<String> = emptySet()
    ): Boolean =
        isPremium || lessonId in freeLessonIds(grade) || lessonId in legacyCompleted

    fun freeCount(grade: Int): Int = freeLessonIds(grade).size
}
