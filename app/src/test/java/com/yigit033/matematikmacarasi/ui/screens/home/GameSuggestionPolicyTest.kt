package com.yigit033.matematikmacarasi.ui.screens.home

import org.junit.Assert.*
import org.junit.Test

class GameSuggestionPolicyTest {
    private val games=listOf(
        GameSuggestionCandidate("add","operation",true),
        GameSuggestionCandidate("hunt","numbers",true),
        GameSuggestionCandidate("geometry","geometry",true),
        GameSuggestionCandidate("pattern_forest","geometry",true),
        GameSuggestionCandidate("time_tower","time",true),
        GameSuggestionCandidate("space","adventure",false),
        GameSuggestionCandidate("market","time",false)
    )

    @Test fun unsupportedGamesAreExcluded() {
        val result=GameSuggestionPolicy.choose(6,games,emptyList(),emptyList(),3)
        assertEquals(3,result.size)
        assertTrue(result.none{it.id=="space" || it.id=="market"})
    }

    @Test fun reliablePracticeSignalInfluencesRanking() {
        val r=GameSuggestionPolicy.choose(2,games,listOf("hunt"),
            listOf(GameSuggestionSignal("geometry",20,12,4)),2)
        assertEquals("geometry",r.first().id)
        assertEquals("Ek alıştırma önerisi",r.first().reason)
    }

    @Test fun tinySampleDoesNotLabelWeakness() {
        val r=GameSuggestionPolicy.choose(2,games,emptyList(),
            listOf(GameSuggestionSignal("geometry",3,3,3)),2)
        assertTrue(r.all{it.reason=="Sınıfına göre keşif"})
    }

    @Test fun noEligibleGamesNoFalseRecommendation() {
        val r=GameSuggestionPolicy.choose(1,
            listOf(GameSuggestionCandidate("space","adventure",false)),emptyList(),emptyList())
        assertTrue(r.isEmpty())
    }

    @Test fun resultsAreUniqueAndLimited() {
        val r=GameSuggestionPolicy.choose(2,games,emptyList(),emptyList())
        assertEquals(2,r.map{it.id}.distinct().size)
    }
}
