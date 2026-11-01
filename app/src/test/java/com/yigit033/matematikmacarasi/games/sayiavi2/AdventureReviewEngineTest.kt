package com.yigit033.matematikmacarasi.games.sayiavi2

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdventureReviewEngineTest {
    @Test fun reviewIsDeterministicAndContainsThreeDifferentLearningTasks() {
        for (kind in AdventureReviewKind.entries) {
            for (tier in LearningTier.entries) {
                for (seed in 1L..100L) {
                    val first = AdventureReviewEngine.create(kind, tier, seed)
                    val second = AdventureReviewEngine.create(kind, tier, seed)
                    assertEquals(first, second)
                    assertEquals(3, first.size)
                    assertEquals(3, first.map { it.prompt }.distinct().size)
                    first.forEach { q ->
                        assertEquals(4, q.options.size)
                        assertEquals(4, q.options.distinct().size)
                        assertTrue(q.correct in q.options)
                        assertTrue(q.options.all { it > 0 })
                        assertTrue(q.focus in 2..9)
                        assertTrue(q.groups in 2..9)
                        assertTrue(q.perGroup in 2..9)
                        assertTrue(q.explanation.isNotBlank())
                        assertTrue(q.hint.isNotBlank())
                    }
                }
            }
        }
    }

    @Test fun focusedRepeatChangesLastTaskWithoutChangingTheSkill() {
        for (kind in AdventureReviewKind.entries) {
            val revised = AdventureReviewEngine.create(
                kind, LearningTier.USTALIK, seed = 197L, weakFocus = 8
            )
            assertEquals(8, revised.last().focus)
            if (kind == AdventureReviewKind.MULTIPLICATION) {
                assertTrue(revised.last().prompt.contains("÷ 8"))
            } else {
                assertTrue(revised.last().prompt.contains("8 eş grupta"))
            }
        }
    }

    @Test fun focusedPracticeNeverExceedsSelectedTier() {
        for (kind in AdventureReviewKind.entries) {
            val items = AdventureReviewEngine.create(kind, LearningTier.TEMEL, 15L, weakFocus = 9)
            assertEquals(4, items.last().focus)
        }
    }

    @Test fun focusedRepeatIsTierBoundedForEveryKindAndEveryWeakFactor() {
        val ranges = mapOf(
            LearningTier.TEMEL to 2..4,
            LearningTier.GELISEN to 3..6,
            LearningTier.USTALIK to 5..9
        )
        for ((tier, range) in ranges) {
            for (kind in AdventureReviewKind.entries) {
                for (focus in 2..9) {
                    for (seed in 1L..30L) {
                        val review = AdventureReviewEngine.create(
                            kind, tier, seed, weakFocus = focus
                        )
                        assertEquals(focus.coerceIn(range.first, range.last),
                            review.last().focus)
                        assertTrue(review.all { it.focus in range })
                        assertTrue(review.all { it.groups in 2..9 && it.perGroup in 2..9 })
                        assertTrue(review.all {
                            it.correct in it.options && it.options.distinct().size == 4
                        })
                    }
                }
            }
        }
    }

    @Test fun allQuestionsAreMathematicallyCorrect() {
        for (tier in LearningTier.entries) {
            for (seed in 1L..100L) {
                for (kind in AdventureReviewKind.entries) {
                    val items = AdventureReviewEngine.create(kind, tier, seed)
                    val last = items.last()
                    if (kind == AdventureReviewKind.MULTIPLICATION) {
                        // The inverse problem returns its other factor.
                        assertEquals(last.groups * last.correct,
                            last.groups * last.perGroup)
                    } else {
                        assertEquals(last.groups * last.perGroup, last.correct)
                    }
                }
            }
        }
    }
}
