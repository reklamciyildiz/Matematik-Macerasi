package com.yigit033.matematikmacarasi.games.interactive

import android.content.Context
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

/** Isolated v2 checkpoint: legacy BalloonPop, purchases and achievements untouched. */
data class BalloonRecord(
    val completions:Int=0,val bestStars:Int=0,val bestStreak:Int=0,
    val mistakes:Int=0,val hints:Int=0
)
class BalloonAdventureStore(context:Context) {
    private val prefs=context.applicationContext.getSharedPreferences(
        "balloon_adventure_v2",Context.MODE_PRIVATE)
    @Synchronized fun load():BalloonRun? {
        if(prefs.getInt("version",0)!=2)return null
        val realm=BalloonRealm.entries.getOrNull(prefs.getInt("realm",-1))?:return null
        val tier=LearningTier.fromSaved(prefs.getString("tier",null))
        val run=BalloonRun(
            seed=prefs.getLong("seed",0L),realm=realm,tier=tier,
            pace=BalloonPace.entries.firstOrNull {
                it.name==prefs.getString("pace",BalloonPace.RELAXED.name)
            }?:BalloonPace.RELAXED,
            escapes=prefs.getInt("escapes",0),
            wave=prefs.getInt("wave",0),
            index=prefs.getInt("index",0),
            attempts=prefs.getInt("attempts",0),
            errors=prefs.getInt("errors",0),
            hints=prefs.getInt("hints",0),
            firstTry=prefs.getInt("first_try",0),
            streak=prefs.getInt("streak",0),
            bestStreak=prefs.getInt("best_streak",0),
            wrongMask=prefs.getInt("wrong_mask",0),
            tainted=prefs.getBoolean("tainted",false),
            hintVisible=prefs.getBoolean("hint_visible",false),
            resolved=prefs.getBoolean("resolved",false),
            completed=prefs.getBoolean("completed",false)
        )
        return run.takeIf(BalloonAdventureEngine::valid)
    }
    @Synchronized fun save(run:BalloonRun):Boolean {
        if(!BalloonAdventureEngine.valid(run))return false
        return prefs.edit()
            .putInt("version",2)
            .putLong("seed",run.seed)
            .putInt("realm",run.realm.ordinal)
            .putString("tier",run.tier.name)
            .putString("pace",run.pace.name)
            .putInt("escapes",run.escapes)
            .putInt("wave",run.wave)
            .putInt("index",run.index)
            .putInt("attempts",run.attempts)
            .putInt("errors",run.errors)
            .putInt("hints",run.hints)
            .putInt("first_try",run.firstTry)
            .putInt("streak",run.streak)
            .putInt("best_streak",run.bestStreak)
            .putInt("wrong_mask",run.wrongMask)
            .putBoolean("tainted",run.tainted)
            .putBoolean("hint_visible",run.hintVisible)
            .putBoolean("resolved",run.resolved)
            .putBoolean("completed",run.completed)
            .commit()
    }
    @Synchronized fun record(realm:BalloonRealm):BalloonRecord {
        val p="realm_"+realm.ordinal+"_"
        return BalloonRecord(
            completions=prefs.getInt(p+"completed",0).coerceAtLeast(0),
            bestStars=prefs.getInt(p+"stars",0).coerceIn(0,3),
            bestStreak=prefs.getInt(p+"streak",0).coerceAtLeast(0),
            mistakes=prefs.getInt(p+"errors",0).coerceAtLeast(0),
            hints=prefs.getInt(p+"hints",0).coerceAtLeast(0)
        )
    }
    /** Counters and per-realm high-water are applied once per completed seed. */
    @Synchronized fun recordUniqueCompletion(run:BalloonRun):Boolean {
        if(!run.completed || !BalloonAdventureEngine.valid(run))return false
        val p="realm_"+run.realm.ordinal+"_"
        if(prefs.getLong(p+"last_seed",0L)==run.seed)return false
        val before=record(run.realm)
        return prefs.edit()
            .putLong(p+"last_seed",run.seed)
            .putInt(p+"completed",before.completions+1)
            .putInt(p+"stars",maxOf(before.bestStars,BalloonAdventureEngine.stars(run)))
            .putInt(p+"streak",maxOf(before.bestStreak,run.bestStreak))
            .putInt(p+"errors",before.mistakes+run.errors)
            .putInt(p+"hints",before.hints+run.hints)
            .commit()
    }
    /** Independent from stars: one run cannot submit game completion multiple times. */
    @Synchronized fun markStatsDelivered(seed:Long):Boolean {
        if(seed<=0 || prefs.getLong("stats_seed",0L)==seed)return false
        return prefs.edit().putLong("stats_seed",seed).commit()
    }
}
