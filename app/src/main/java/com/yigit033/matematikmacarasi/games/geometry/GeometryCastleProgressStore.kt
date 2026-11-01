package com.yigit033.matematikmacarasi.games.geometry

import android.content.Context
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

/** All geometry checkpoints are deliberately isolated from legacy mini-games and billing. */
class GeometryCastleProgressStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        "geometry_castle_v1", Context.MODE_PRIVATE
    )

    @Synchronized fun load(): CastleRun {
        val selected = prefs.getString("selected", null)?.let { v ->
            CastleShape.entries.firstOrNull { it.name == v }
        }
        val run = CastleRun(
            seed = prefs.getLong("seed", 0),
            tier = LearningTier.fromSaved(prefs.getString("tier", null), LearningTier.TEMEL),
            room = prefs.getInt("room", 0),
            step = prefs.getInt("step", 0),
            selected = selected,
            solved = prefs.getBoolean("solved", false),
            mirrorMask = prefs.getInt("mirror", 0),
            attempts = prefs.getInt("attempts", 0),
            errors = prefs.getInt("errors", 0),
            hints = prefs.getInt("hints", 0),
            firstTryCorrect = prefs.getInt("first_try", 0),
            hasErrorInTask = prefs.getBoolean("task_error", false),
            hasHintInTask = prefs.getBoolean("task_hint", false),
            completedRuns = prefs.getInt("runs", 0)
        )
        return if (GeometryCastleEngine.valid(run)) run else CastleRun(
            completedRuns = prefs.getInt("runs", 0).coerceAtLeast(0)
        )
    }

    @Synchronized fun save(next: CastleRun): Boolean {
        if (!GeometryCastleEngine.valid(next)) return false
        val editor = prefs.edit()
            .putLong("seed", next.seed).putString("tier",next.tier.name)
            .putInt("room",next.room).putInt("step",next.step)
            .putBoolean("solved",next.solved)
            .putInt("mirror",next.mirrorMask)
            .putInt("attempts",next.attempts).putInt("errors",next.errors)
            .putInt("hints",next.hints).putInt("first_try",next.firstTryCorrect)
            .putBoolean("task_error",next.hasErrorInTask)
            .putBoolean("task_hint",next.hasHintInTask)
            .putInt("runs",next.completedRuns)
        if(next.selected == null) editor.remove("selected")
        else editor.putString("selected",next.selected.name)
        return editor.commit()
    }

    @Synchronized fun begin(tier: LearningTier): CastleRun? {
        val old = load()
        val seed = System.currentTimeMillis().coerceAtLeast(old.seed + 1).coerceAtLeast(1)
        val next = CastleRun(seed=seed,tier=tier,completedRuns=old.completedRuns)
        return if(save(next)) next else null
    }

    fun hasEverCompleted(): Boolean = prefs.getInt("runs", 0) > 0
    fun rewardRecorded(): Boolean = prefs.getBoolean("reward_recorded", false)
    fun markRewardRecorded(): Boolean =
        prefs.edit().putBoolean("reward_recorded",true).commit()

    /** Each completed run is delivered once to the daily task bridge, including on resume. */
    @Synchronized fun markDailyDelivered(seed: Long): Boolean {
        if(seed <= 0) return false
        if(prefs.getLong("delivered_run", -1) == seed) return false
        return prefs.edit().putLong("delivered_run",seed).commit()
    }
}
