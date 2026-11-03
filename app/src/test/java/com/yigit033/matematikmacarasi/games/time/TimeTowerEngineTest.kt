package com.yigit033.matematikmacarasi.games.time

import org.junit.Assert.*
import org.junit.Test

class TimeTowerEngineTest {

    @Test fun gregorianCalendarLeapWeekdaysAndMonthBoundaries() {
        assertTrue(TowerDate.isLeap(2024))
        assertTrue(TowerDate.isLeap(2000))
        assertFalse(TowerDate.isLeap(2100))
        assertFalse(TowerDate.isLeap(2025))
        assertEquals(29,TowerDate.daysInMonth(2024,2))
        assertEquals(28,TowerDate.daysInMonth(2025,2))
        assertEquals(TowerDate(2024,2,29),TowerDate(2024,2,28).addDays(1))
        assertEquals(TowerDate(2024,3,1),TowerDate(2024,2,28).addDays(2))
        assertEquals(TowerDate(2025,3,1),TowerDate(2025,2,28).addDays(1))
        assertEquals(TowerDate(2025,1,1),TowerDate(2024,12,31).addDays(1))
        assertEquals(TowerDate(2024,12,31),TowerDate(2025,1,1).addDays(-1))
        assertEquals(0,TowerDate(2024,1,1).weekdayMondayFirst())
        assertEquals(3,TowerDate(2024,2,29).weekdayMondayFirst())
        assertEquals(1,TowerDate(2026,11,3).weekdayMondayFirst())
        assertEquals(50,TowerDate(2024,12,20).daysUntil(TowerDate(2025,2,8)))
        assertEquals(-50,TowerDate(2025,2,8).daysUntil(TowerDate(2024,12,20)))
        assertNull(TowerDate.fromCode(20250229))
        assertEquals(TowerDate(2024,2,29),TowerDate.fromCode(20240229))
    }

    @Test fun timeMathAndMidnightDayOffsetCorrect() {
        assertEquals("08:05",TimeTowerEngine.formatTime(485))
        assertEquals("00:00 (+1 gün)",TimeTowerEngine.formatTime(1440))
        assertEquals("01:20 (+1 gün)",TimeTowerEngine.formatTime(1520))
        assertEquals("00:00 (+2 gün)",TimeTowerEngine.formatTime(2880))
        assertEquals("Gece",TimeTowerEngine.partOfDay(90))
        assertEquals("Öğleden sonra",TimeTowerEngine.partOfDay(900))
        assertEquals("Akşam",TimeTowerEngine.partOfDay(1199))
    }

    @Test fun gradeDifficultyNeverUnlocksFutureMinutePrecision() {
        for(diff in 0..2) assertEquals(60,TimeTowerEngine.clockStep(1,diff))
        assertEquals(30,TimeTowerEngine.clockStep(2,0))
        assertEquals(15,TimeTowerEngine.clockStep(2,2))
        assertEquals(15,TimeTowerEngine.clockStep(3,0))
        assertEquals(5,TimeTowerEngine.clockStep(3,2))
        assertEquals(5,TimeTowerEngine.clockStep(4,2))
        assertEquals(1,TimeTowerEngine.clockStep(5,2))
        assertEquals(1,TimeTowerEngine.clockStep(8,2))
    }

    @Test fun fiveDistinctModesAndTwentyFiveTasksForEveryGradeAndTier() {
        for(g in 1..8)for(diff in 0..2)for(seed in 1L..75L) {
            val list=(0 until TimeTowerEngine.STOPS).map {
                TimeTowerEngine.task(seed,g,diff,it)
            }
            assertEquals(25,list.size)
            assertEquals(list,(0 until 25).map{TimeTowerEngine.task(seed,g,diff,it)})
            TimeWorld.entries.forEachIndexed { i,world->
                assertEquals(List(5){world},list.slice(i*5 until i*5+5).map{it.world})
            }
            list.forEachIndexed { index,task->
                assertEquals(g,task.grade)
                assertEquals(TimeTowerEngine.worldAt(index),task.world)
                assertTrue(task.instruction.isNotBlank())
                assertTrue(task.explanation.isNotBlank())
                assertTrue(task.title.isNotBlank())
                assertTrue(task.stepMinutes in listOf(1,5,15,30,60))
                when(task.world) {
                    TimeWorld.CLOCK,TimeWorld.READ-> {
                        assertTrue(task.answerMinute in 0..1439)
                        assertEquals(0,(task.answerMinute%60)%task.stepMinutes)
                        assertTrue(TimeTowerEngine.answerCorrect(task,task.answerMinute,0,emptyList()))
                        assertFalse(TimeTowerEngine.answerCorrect(task,(task.answerMinute+60)%1440,0,emptyList()))
                        assertTrue(TimeTowerEngine.initialMinute(task) in 0..1439)
                        assertNotEquals(task.answerMinute,TimeTowerEngine.initialMinute(task))
                    }
                    TimeWorld.TRAIN-> {
                        assertTrue(task.startMinute in 0..1439)
                        assertTrue(task.elapsedMinutes>0)
                        assertEquals(0,task.elapsedMinutes%task.stepMinutes)
                        assertEquals(task.startMinute+task.elapsedMinutes,task.answerMinute)
                        assertTrue(task.rangeMinutes>=task.elapsedMinutes)
                        assertTrue(task.startMinute+task.rangeMinutes<=2880)
                        assertEquals(task.startMinute,TimeTowerEngine.initialMinute(task))
                        assertTrue(TimeTowerEngine.answerCorrect(task,task.answerMinute,0,emptyList()))
                        assertFalse(TimeTowerEngine.answerCorrect(task,task.startMinute,0,emptyList()))
                    }
                    TimeWorld.CALENDAR-> {
                        val days=task.firstDate.daysUntil(task.answerDate)
                        assertTrue(days>=1)
                        assertTrue(days<=50)
                        if(g==1)assertTrue(days<=3)
                        if(g==2)assertTrue(days<=7)
                        assertTrue(TimeTowerEngine.answerCorrect(task,-1,task.answerDate.code,emptyList()))
                        assertFalse(TimeTowerEngine.answerCorrect(task,-1,task.firstDate.code,emptyList()))
                    }
                    TimeWorld.ROUTE-> {
                        val expected=task.correctOrder
                        assertEquals(expected.size,task.events.size)
                        assertTrue(task.events.size in 3..5)
                        assertEquals(task.events.map{it.id}.toSet(),expected.toSet())
                        assertEquals(task.events.size,task.events.map{it.minute}.distinct().size)
                        assertTrue(TimeTowerEngine.answerCorrect(task,-1,0,expected))
                        assertFalse(TimeTowerEngine.answerCorrect(task,-1,0,expected.reversed()))
                    }
                }
            }
        }
    }

    @Test fun replaySeedsVaryContentButEachSameSeedReplaysIdentically() {
        val a=(0 until 25).map{TimeTowerEngine.task(1234L,5,2,it)}
        val b=(0 until 25).map{TimeTowerEngine.task(5678L,5,2,it)}
        assertTrue(a.zip(b).count{it.first!=it.second}>=20)
    }
}
