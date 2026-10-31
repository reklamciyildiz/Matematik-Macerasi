package com.yigit033.matematikmacarasi.ui.screens.home

import org.junit.Assert.*
import org.junit.Test

class GameSkillReportTest {
    @Test fun reportDoesNotClaimMasteryFromMissingEvents() {
        val r = GameSkillReport(0,0,0,0,"Temel")
        assertNull(r.accuracyPercent)
        assertEquals("Başlangıç verisi bekleniyor",r.status)
    }
    @Test fun reportsHonestAggregateAttemptsRatherThanGrade() {
        val r = GameSkillReport(3,30,6,0,"Ustalık")
        assertEquals(80,r.accuracyPercent)
        assertEquals("Oyun içi pratik kaydedildi",r.status)
        assertTrue(r.recommendation.contains("oturum"))
        val supported = GameSkillReport(4,40,19,4,"Temel")
        assertEquals("Tekrar alıştırması önerilir", supported.status)
    }
}
