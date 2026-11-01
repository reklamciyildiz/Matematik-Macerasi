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
            focusShape = prefs.getString("focus", null)?.let { name ->
                CastleShape.entries.firstOrNull { it.name == name }
            },
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
        val previous = load()
        val sameRun = previous.seed > 0 && previous.seed == next.seed
        val previousTaskId = previous.room * 3 + previous.step
        val nextTaskId = next.room * 3 + next.step
        val completedTask = sameRun && previous.room < 4 && previous.solved &&
            nextTaskId == previousTaskId + 1
        val newRun = next.seed > 0 && next.seed != previous.seed && next.room == 0
        val editor = prefs.edit()
        if (newRun) {
            editor.putInt("counted_attempts",0).putInt("counted_errors",0)
                .putInt("counted_hints",0)
        }
        if (completedTask) {
            GeometryCastleEngine.task(previous)?.let { task ->
                val skill = task.skill
                val deltaAttempts = (previous.attempts -
                    prefs.getInt("counted_attempts",0)).coerceAtLeast(0)
                val deltaErrors = (previous.errors -
                    prefs.getInt("counted_errors",0)).coerceAtLeast(0)
                val deltaHints = (previous.hints -
                    prefs.getInt("counted_hints",0)).coerceAtLeast(0)
                editor.putInt(skill + "_attempts",
                    prefs.getInt(skill + "_attempts",0) + deltaAttempts)
                editor.putInt(skill + "_errors",
                    prefs.getInt(skill + "_errors",0) + deltaErrors)
                editor.putInt(skill + "_hints",
                    prefs.getInt(skill + "_hints",0) + deltaHints)
                editor.putInt(skill + "_completed",
                    prefs.getInt(skill + "_completed",0) + 1)
                editor.putInt("counted_attempts",previous.attempts)
                    .putInt("counted_errors",previous.errors)
                    .putInt("counted_hints",previous.hints)
            }
        }
            .putLong("seed", next.seed).putString("tier",next.tier.name)
            .putInt("room",next.room).putInt("step",next.step)
            .putBoolean("solved",next.solved)
            .putInt("mirror",next.mirrorMask)
            .putInt("attempts",next.attempts).putInt("errors",next.errors)
            .putInt("hints",next.hints).putInt("first_try",next.firstTryCorrect)
            .putBoolean("task_error",next.hasErrorInTask)
            .putBoolean("task_hint",next.hasHintInTask)
            .putInt("runs",next.completedRuns)
        if(next.focusShape == null) editor.remove("focus")
        else editor.putString("focus",next.focusShape.name)
        if(next.selected == null) editor.remove("selected")
        else editor.putString("selected",next.selected.name)
        return editor.commit()
    }

    @Synchronized fun begin(tier: LearningTier): CastleRun? {
        val old = load()
        val seed = System.currentTimeMillis().coerceAtLeast(old.seed + 1).coerceAtLeast(1)
        val next = CastleRun(seed=seed,tier=tier,completedRuns=old.completedRuns,
            focusShape=weakShape())
        return if(save(next)) next else null
    }

    fun skillEvidence(skill: String): Triple<Int,Int,Int> {
        val valid = setOf("geometry_classify","geometry_compose",
            "geometry_symmetry_recognize","geometry_symmetry_build")
        if(skill !in valid) return Triple(0,0,0)
        return Triple(prefs.getInt(skill + "_completed",0),
            prefs.getInt(skill + "_errors",0),
            prefs.getInt(skill + "_hints",0))
    }

    fun weakShape(): CastleShape? = prefs.getString("weak_shape", null)?.let { name ->
        CastleShape.entries.firstOrNull { it.name == name }
    }
    fun noteWeakShape(shape: CastleShape): Boolean =
        prefs.edit().putString("weak_shape",shape.name).commit()
    fun clearWeakShape(): Boolean = prefs.edit().remove("weak_shape").commit()

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
