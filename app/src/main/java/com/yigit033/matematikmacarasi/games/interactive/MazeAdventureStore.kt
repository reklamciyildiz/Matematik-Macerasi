package com.yigit033.matematikmacarasi.games.interactive

import android.content.Context
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

/** Private v2 checkpoint. Never rewrites legacy maze, stars or daily-limit keys. */
data class MazeRegionRecord(
    val completed:Int=0,
    val bestStars:Int=0,
    val mistakes:Int=0,
    val hints:Int=0
)

class MazeAdventureStore(context:Context) {
    private val prefs=context.applicationContext.getSharedPreferences(
        "math_maze_adventure_v2",Context.MODE_PRIVATE)

    @Synchronized fun load():MazeExpedition? {
        if(prefs.getInt("version",0)!=2)return null
        val seed=prefs.getLong("seed",0L)
        val region=MazeRegion.entries.getOrNull(prefs.getInt("region",-1))?:return null
        val tier=LearningTier.fromSaved(prefs.getString("tier",null))
        val run=MazeExpedition(
            seed=seed,region=region,tier=tier,
            cell=prefs.getInt("cell",-1),
            solvedMask=prefs.getInt("mask",0),
            pendingCell=prefs.getInt("pending",-1),
            attempts=prefs.getInt("attempts",0),
            errors=prefs.getInt("errors",0),
            hints=prefs.getInt("hints",0),
            shownHint=prefs.getBoolean("shown_hint",false),
            completed=prefs.getBoolean("completed",false)
        )
        return if(MazeAdventureEngine.valid(run))run else null
    }

    @Synchronized fun save(run:MazeExpedition):Boolean {
        if(!MazeAdventureEngine.valid(run))return false
        return prefs.edit()
            .putInt("version",2)
            .putLong("seed",run.seed)
            .putInt("region",run.region.ordinal)
            .putString("tier",run.tier.name)
            .putInt("cell",run.cell)
            .putInt("mask",run.solvedMask)
            .putInt("pending",run.pendingCell)
            .putInt("attempts",run.attempts)
            .putInt("errors",run.errors)
            .putInt("hints",run.hints)
            .putBoolean("shown_hint",run.shownHint)
            .putBoolean("completed",run.completed)
            .commit()
    }

    /** Per-run delivery guard; a checkpoint can be replayed without farming rewards. */
    @Synchronized fun markCompletionDelivered(seed:Long):Boolean {
        if(seed<=0L || prefs.getLong("delivered",0L)==seed)return false
        return prefs.edit().putLong("delivered",seed).commit()
    }

    @Synchronized fun runsCompleted():Int=prefs.getInt("completed_runs",0)

    @Synchronized fun regionRecord(region:MazeRegion):MazeRegionRecord {
        val key="region_${region.ordinal}_"
        return MazeRegionRecord(
            completed=prefs.getInt(key+"completed",0).coerceAtLeast(0),
            bestStars=prefs.getInt(key+"best_stars",0).coerceIn(0,3),
            mistakes=prefs.getInt(key+"errors",0).coerceAtLeast(0),
            hints=prefs.getInt(key+"hints",0).coerceAtLeast(0)
        )
    }

    /** Regional practice evidence is local, cumulative and recorded once per run. */
    @Synchronized fun recordUniqueCompletion(run:MazeExpedition):Boolean {
        if(!run.completed || run.seed<=0L ||
            prefs.getLong("counted_seed",0L)==run.seed)return false
        val key="region_${run.region.ordinal}_"
        val old=regionRecord(run.region)
        return prefs.edit()
            .putLong("counted_seed",run.seed)
            .putInt("completed_runs",runsCompleted()+1)
            .putInt(key+"completed",old.completed+1)
            .putInt(key+"best_stars",maxOf(old.bestStars,MazeAdventureEngine.stars(run)))
            .putInt(key+"errors",old.mistakes+run.errors)
            .putInt(key+"hints",old.hints+run.hints)
            .commit()
    }
}
