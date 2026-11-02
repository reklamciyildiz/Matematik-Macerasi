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
        if(prefs.getInt("version",0) !in 2..3)return null
        val planet=SpacePlanet.entries.getOrNull(prefs.getInt("planet",-1))?:return null
        val tier=LearningTier.entries.firstOrNull {
            it.name==prefs.getString("tier",null)
        }?:return null
        // Keep legacy achievements in their original high-water keys.
        // In-progress v2 campaigns are converted to the closest sequential
        // question checkpoint; obsolete reactor/circuit counters are discarded.
        val version=prefs.getInt("version",0)
        if(version==2 && prefs.getBoolean("completed",false))return null
        val oldStage=prefs.getInt("stage",0).coerceIn(
            0,if(version==3)SpaceExpeditionEngine.STAGES else 9
        )
        val oldSolved=version==2 && prefs.getBoolean("solved",false)
        val stage=if(version==2)oldStage+(if(oldSolved)1 else 0)
            else oldStage
        val errors=prefs.getInt("errors",0).coerceAtLeast(0)
        val firstTry=prefs.getInt("first_try",0).coerceIn(0,stage)
        val run=SpaceRun(
            seed=prefs.getLong("seed",0),
            planet=planet,tier=tier,
            grade=if(version==3)prefs.getInt("grade",1) else 1,
            stage=stage,
            shipX=prefs.getFloat("ship_x",.5f),
            wave=prefs.getInt("wave",0).coerceAtLeast(0),
            completed=version==3 && prefs.getBoolean("completed",false),
            attempts=if(version==3)prefs.getInt("attempts",0)
                else stage+errors,
            errors=errors,
            hints=prefs.getInt("hints",0).coerceIn(0,stage+1),
            firstTry=firstTry,
            tainted=if(oldSolved)false else prefs.getBoolean("tainted",false),
            hintVisible=if(oldSolved)false else prefs.getBoolean("hint_visible",false),
            combo=if(version==3)prefs.getInt("combo",0) else 0,
            bestCombo=if(version==3)prefs.getInt("best_combo",0) else 0,
            shields=prefs.getInt("shields",3)
        )
        return run.takeIf(SpaceExpeditionEngine::valid)
    }
    @Synchronized fun save(run:SpaceRun):Boolean {
        if(!SpaceExpeditionEngine.valid(run))return false
        return prefs.edit()
            .putInt("version",3)
            .putLong("seed",run.seed)
            .putInt("planet",run.planet.ordinal)
            .putString("tier",run.tier.name)
            .putInt("grade",run.grade)
            .putFloat("ship_x",run.shipX)
            .putInt("stage",run.stage)
            .putInt("wave",run.wave)
            .putBoolean("completed",run.completed)
            .putInt("attempts",run.attempts)
            .putInt("errors",run.errors)
            .putInt("hints",run.hints)
            .putInt("first_try",run.firstTry)
            .putBoolean("tainted",run.tainted)
            .putBoolean("hint_visible",run.hintVisible)
            .putInt("combo",run.combo)
            .putInt("best_combo",run.bestCombo)
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
