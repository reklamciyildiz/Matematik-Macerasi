package com.yigit033.matematikmacarasi.ui.screens.home

/** Pure, deterministic suggestions; insufficient practice data never implies weakness. */
data class GameSuggestionCandidate(val id:String,val topic:String,val gradeMatched:Boolean)
data class GameSuggestionSignal(val id:String,val attempts:Int,val errors:Int,val hints:Int) {
    val needsPractice:Boolean get()=attempts>=8 &&
        (errors.toLong()*100>=attempts.toLong()*35 || hints>=3)
}
data class GameSuggestion(val id:String,val reason:String)

object GameSuggestionPolicy {
    fun choose(grade:Int,candidates:List<GameSuggestionCandidate>,
        recentIds:List<String>,signals:List<GameSuggestionSignal>,
        limit:Int=2):List<GameSuggestion>{
        if(limit<=0)return emptyList()
        val preferred=when(grade){
            1->listOf("add","time_tower","pattern_forest","geometry","subtract")
            2->listOf("hunt","geometry","pattern_forest","time_tower","add")
            3->listOf("multiply","geometry","fractions","space","pattern_forest")
            4->listOf("fractions","divide","geometry","pattern_forest","time_tower")
            else->listOf("pattern_forest","fractions","time_tower","geometry","divide")
        }
        val weak=signals.filter{it.needsPractice}.map{it.id}.toSet()
        val eligible=candidates.filter{it.gradeMatched}
        val picked=mutableListOf<GameSuggestionCandidate>()
        while(picked.size<limit && picked.size<eligible.size){
            val next=eligible.filterNot{it in picked}.maxByOrNull{item->
                val index=preferred.indexOf(item.id)
                val base=if(index>=0)80-index*8 else 30
                val practice=if(item.id in weak)55 else 0
                val recent=when(recentIds.indexOf(item.id)){
                    0->28
                    1,2->16
                    else->0
                }
                val sameTopic=if(picked.any{it.topic==item.topic})24 else 0
                base+practice-recent-sameTopic
            }?:break
            picked.add(next)
        }
        return picked.map{
            GameSuggestion(it.id,if(it.id in weak)"Ek alıştırma önerisi" else "Sınıfına göre keşif")
        }
    }
}
