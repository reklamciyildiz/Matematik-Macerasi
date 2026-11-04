package com.yigit033.matematikmacarasi.ui.screens.home

import android.content.Context
import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveLearningStore
import com.yigit033.matematikmacarasi.learning.adaptive.AdventureSkill

/** Reads existing recorded attempts; never writes progress when home is opened. */
object GamePersonalization {
    fun recommendations(context:Context,grade:Int,recentIds:List<String>):List<GameSuggestion>{
        val store=AdaptiveLearningStore(context.applicationContext)
        val mapping=listOf(
            "hunt" to AdventureSkill.THREE_COUNTING,
            "forest" to AdventureSkill.FOUR_COUNTING,
            "geometry" to AdventureSkill.GEOMETRY,
            "market" to AdventureSkill.MARKET,
            "space" to AdventureSkill.SPACE
        )
        val signals=mapping.mapNotNull{(id,skill)->
            val p=store.snapshotForReport(skill) ?: return@mapNotNull null
            if(p.profileGrade!=grade)return@mapNotNull null
            GameSuggestionSignal(id,p.totalAttempts,p.totalErrors,p.totalHints)
        }
        val eligible=GameCatalog.games.map{
            GameSuggestionCandidate(it.id,it.topic.name,it.isGradeMatched(grade))
        }
        return GameSuggestionPolicy.choose(grade,eligible,recentIds,signals)
    }
}
