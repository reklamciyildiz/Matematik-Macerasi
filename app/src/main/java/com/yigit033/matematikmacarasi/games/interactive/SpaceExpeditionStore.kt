package com.yigit033.matematikmacarasi.games.interactive

import android.content.Context
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

data class SpacePlanetRecord(
    val completions:Int=0,val bestStars:Int=0,val bestScore:Int=0
)

/** Isolated v2 checkpoint. Never overwrites legacy SpaceMath or Play purchases. */
class SpaceExpeditionStore(context:Context) {
    private val prefs=context.applicationContext.getSharedPreferences(
        "space_expedition_v2",Context.MODE_PRIVATE
    )
    @Synchronized fun load():SpaceRun? {
        if(prefs.getInt("version",0)!=2)return null
        val planet=SpacePlanet.entries.getOrNull(prefs.getInt("planet",-1))?:return null
        val tier=LearningTier.entries.firstOrNull {
            it.name==prefs.getString("tier",null)
        }?:return null
        val raw=prefs.getString("path","")?:""
        val path=if(raw.isEmpty())emptyList() else raw.split(",").mapNotNull {
            it.toIntOrNull()
        }
        val run=SpaceRun(
            seed=prefs.getLong("seed",0),
            planet=planet,tier=tier,
            stage=prefs.getInt("stage",0),
            lane=prefs.getInt("lane",1),
            wave=prefs.getInt("wave",0),
            charge=prefs.getInt("charge",0),
            path=path,solved=prefs.getBoolean("solved",false),
            completed=prefs.getBoolean("completed",false),
            attempts=prefs.getInt("attempts",0),
            errors=prefs.getInt("errors",0),
            hints=prefs.getInt("hints",0),
            firstTry=prefs.getInt("first_try",0),
            tainted=prefs.getBoolean("tainted",false),
            hintVisible=prefs.getBoolean("hint_visible",false),
            shields=prefs.getInt("shields",3)
        )
        return run.takeIf(SpaceExpeditionEngine::valid)
    }
    @Synchronized fun save(run:SpaceRun):Boolean {
        if(!SpaceExpeditionEngine.valid(run))return false
        return prefs.edit()
            .putInt("version",2)
            .putLong("seed",run.seed)
            .putInt("planet",run.planet.ordinal)
            .putString("tier",run.tier.name)
            .putInt("stage",run.stage)
            .putInt("lane",run.lane)
            .putInt("wave",run.wave)
            .putInt("charge",run.charge)
            .putString("path",run.path.joinToString(","))
            .putBoolean("solved",run.solved)
            .putBoolean("completed",run.completed)
            .putInt("attempts",run.attempts)
            .putInt("errors",run.errors)
            .putInt("hints",run.hints)
            .putInt("first_try",run.firstTry)
            .putBoolean("tainted",run.tainted)
            .putBoolean("hint_visible",run.hintVisible)
            .putInt("shields",run.shields)
            .commit()
    }
    @Synchronized fun record(planet:SpacePlanet):SpacePlanetRecord {
        val key="planet_"+planet.ordinal+"_"
        return SpacePlanetRecord(
            completions=prefs.getInt(key+"runs",0).coerceAtLeast(0),
            bestStars=prefs.getInt(key+"stars",0).coerceIn(0,3),
            bestScore=prefs.getInt(key+"score",0).coerceAtLeast(0)
        )
    }
    @Synchronized fun recordUnique(run:SpaceRun):Boolean {
        if(!run.completed || !SpaceExpeditionEngine.valid(run))return false
        val key="planet_"+run.planet.ordinal+"_"
        if(prefs.getLong(key+"last_seed",-1L)==run.seed)return false
        val before=record(run.planet)
        return prefs.edit()
            .putLong(key+"last_seed",run.seed)
            .putInt(key+"runs",before.completions+1)
            .putInt(key+"stars",maxOf(before.bestStars,SpaceExpeditionEngine.stars(run)))
            .putInt(key+"score",maxOf(before.bestScore,SpaceExpeditionEngine.score(run)))
            .commit()
    }
    @Synchronized fun markDelivered(seed:Long):Boolean {
        if(seed<=0 || prefs.getLong("delivered_seed",-1L)==seed)return false
        return prefs.edit().putLong("delivered_seed",seed).commit()
    }
}
