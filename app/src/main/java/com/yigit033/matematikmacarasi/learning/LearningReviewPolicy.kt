package com.yigit033.matematikmacarasi.learning

/** A conservative review suggestion, NOT a validated mastery assessment.
 * No data sent to any server. Does not gate lessons, Premium, or rewards.
 */
enum class ReviewSignal { REVISIT_WITH_HELP, REVISIT, CHECK_LATER }

data class LearningSessionEvidence(
    val wrong: Int, val hints: Int, val conceptErrors: Int, val previousRuns: Int,
    val cleanStreak: Int = 0
) {
    init { require(listOf(wrong, hints, conceptErrors, previousRuns, cleanStreak).all { it >= 0 }) }
}

data class LearningReviewAdvice(
    val signal: ReviewSignal,
    val waitDays: Int,
    val cleanStreak: Int,
    val explanation: String
)

object LearningReviewPolicy {
    fun recommend(evidence: LearningSessionEvidence): LearningReviewAdvice {
        val clean = evidence.wrong == 0 && evidence.hints == 0 &&
            evidence.conceptErrors == 0
        val streak = if (clean) (evidence.cleanStreak + 1).coerceAtMost(30) else 0
        return when {
            evidence.wrong >= 3 || evidence.hints >= 2 || evidence.conceptErrors >= 2 ->
                LearningReviewAdvice(
                    ReviewSignal.REVISIT_WITH_HELP, 1, streak,
                    "Bu konuyu resimli anlatımdan tekrar incelemek faydalı olabilir."
                )
            !clean -> LearningReviewAdvice(
                ReviewSignal.REVISIT, 2, streak,
                "Bir sonraki oturumda başka sorularla yeniden deneyelim."
            )
            streak >= 3 -> LearningReviewAdvice(
                ReviewSignal.CHECK_LATER, 7, streak,
                "Farklı oturumlarda tutarlı doğru çözümler gördük; bir hafta sonra kontrol edelim."
            )
            else -> LearningReviewAdvice(
                ReviewSignal.CHECK_LATER, 3, streak,
                "Birkaç gün sonra yeniden denemek bilgiyi hatırlamaya yardımcı olabilir."
            )
        }
    }

    fun nextReviewAt(completedAt: Long, days: Int): Long {
        require(completedAt > 0 && days in 1..30)
        return completedAt + days * 86_400_000L
    }

    fun due(completed: Boolean, now: Long, savedDueTime: Long): Boolean =
        completed && savedDueTime > 0L && now >= savedDueTime
}
