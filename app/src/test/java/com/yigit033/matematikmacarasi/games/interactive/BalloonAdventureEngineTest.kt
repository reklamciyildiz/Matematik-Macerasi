package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BalloonAdventureEngineTest {
    @Test fun eachWorldTierAndSeedCanFinishTenCorrectQuestions() {
        for(realm in BalloonRealm.entries)
            for(tier in LearningTier.entries)
                for(seed in 1L..75L) {
                    var run=BalloonAdventureEngine.begin(seed,realm,tier)
                    assertTrue(BalloonAdventureEngine.valid(run))
                    repeat(BalloonAdventureEngine.QUESTIONS) {index ->
                        val q=BalloonAdventureEngine.question(run)
                        assertEquals(q,BalloonAdventureEngine.question(seed,realm,tier,index))
                        assertEquals(4,q.choices.size)
                        assertEquals(4,q.choices.distinct().size)
                        assertTrue(q.choices.contains(q.answer))
                        assertTrue(q.answer>=0)
                        assertTrue(q.expression.isNotBlank())
                        assertTrue(q.hint.isNotBlank())
                        assertTrue(q.explanation.contains(q.answer.toString()))
                        val selected=BalloonAdventureEngine.choose(run,q.answer)
                        assertTrue(selected.resolved)
                        assertEquals(run.index,selected.index)
                        assertEquals(index+1,selected.solved)
                        assertEquals(index+1,selected.attempts)
                        assertEquals(selected,BalloonAdventureEngine.choose(selected,q.answer))
                        run=BalloonAdventureEngine.continueAfterAnswer(selected)
                        assertEquals(index+1,run.index)
                        assertTrue(BalloonAdventureEngine.valid(run))
                    }
                    assertTrue(run.completed)
                    assertEquals(10,run.firstTry)
                    assertEquals(10,run.bestStreak)
                    assertEquals(3,BalloonAdventureEngine.stars(run))
                    assertEquals(run,BalloonAdventureEngine.continueAfterAnswer(run))
                }
    }

    @Test fun mistakesAreCountedOnceAndCorrectBalloonRemains() {
        var run=BalloonAdventureEngine.begin(2026L,BalloonRealm.STAR,LearningTier.GELISEN)
        val q=BalloonAdventureEngine.question(run)
        val wrong=q.choices.first {it!=q.answer}
        run=BalloonAdventureEngine.choose(run,wrong)
        assertEquals(1,run.errors)
        assertEquals(1,run.attempts)
        assertTrue(run.tainted)
        assertEquals(run,BalloonAdventureEngine.choose(run,wrong))
        assertTrue(BalloonAdventureEngine.valid(run))
        val wrongTwo=q.choices.first {it!=q.answer && it!=wrong}
        run=BalloonAdventureEngine.choose(run,wrongTwo)
        assertEquals(2,run.errors)
        assertEquals(2,run.attempts)
        run=BalloonAdventureEngine.choose(run,q.answer)
        assertTrue(run.resolved)
        assertEquals(1,run.solved)
        assertEquals(0,run.firstTry)
        assertEquals(3,run.attempts)
        assertTrue(BalloonAdventureEngine.valid(run))
        val next=BalloonAdventureEngine.continueAfterAnswer(run)
        assertEquals(1,next.index)
        assertEquals(0,next.wrongMask)
        assertFalse(next.tainted)
    }

    @Test fun hintIsOptionalAndCannotBeDuplicatedOrUsedOnRevealedAnswer() {
        var run=BalloonAdventureEngine.begin(600L,BalloonRealm.WIND,LearningTier.USTALIK)
        run=BalloonAdventureEngine.hint(run)
        assertTrue(run.hintVisible)
        assertEquals(1,run.hints)
        assertEquals(run,BalloonAdventureEngine.hint(run))
        run=BalloonAdventureEngine.choose(run,BalloonAdventureEngine.question(run).answer)
        assertEquals(0,run.firstTry)
        assertEquals(run,BalloonAdventureEngine.hint(run))
        assertEquals(0,run.index)
        run=BalloonAdventureEngine.continueAfterAnswer(run)
        assertEquals(1,run.index)
        assertFalse(run.hintVisible)
        assertTrue(BalloonAdventureEngine.valid(run))
    }

    @Test fun invalidStateCannotFarmScoreAndNoStarsBeforeCompletion() {
        val start=BalloonAdventureEngine.begin(90L,BalloonRealm.RAINBOW,LearningTier.TEMEL)
        assertEquals(0,BalloonAdventureEngine.stars(start))
        assertFalse(BalloonAdventureEngine.valid(start.copy(seed=0L)))
        assertFalse(BalloonAdventureEngine.valid(start.copy(index=11)))
        assertFalse(BalloonAdventureEngine.valid(start.copy(errors=2)))
        assertFalse(BalloonAdventureEngine.valid(start.copy(completed=true)))
        assertFalse(BalloonAdventureEngine.valid(start.copy(firstTry=1)))
        assertEquals(start,BalloonAdventureEngine.choose(start,Int.MIN_VALUE))
        assertEquals(start,BalloonAdventureEngine.continueAfterAnswer(start))
        val first=BalloonAdventureEngine.question(start)
        val wrong=first.choices.first {it!=first.answer}
        val once=BalloonAdventureEngine.choose(start,wrong)
        assertNotEquals(start,once)
        assertEquals(once,BalloonAdventureEngine.choose(once,wrong))
    }

    @Test fun eachWorldHasItsOwnCurricularOperation() {
        val tier=LearningTier.GELISEN
        assertTrue(BalloonAdventureEngine.question(19L,BalloonRealm.SUN,tier,0).expression.contains("+"))
        assertTrue(BalloonAdventureEngine.question(19L,BalloonRealm.CLOUD,tier,0).expression.contains("−"))
        assertTrue(BalloonAdventureEngine.question(19L,BalloonRealm.WIND,tier,0).expression.contains("×"))
        assertTrue(BalloonAdventureEngine.question(19L,BalloonRealm.STAR,tier,0).expression.contains("÷"))
        val mixed=(0..3).map {
            BalloonAdventureEngine.question(19L,BalloonRealm.RAINBOW,tier,it).expression
        }
        assertTrue(mixed.any {it.contains("+")})
        assertTrue(mixed.any {it.contains("−")})
        assertTrue(mixed.any {it.contains("×")})
        assertTrue(mixed.any {it.contains("÷")})
    }
}
