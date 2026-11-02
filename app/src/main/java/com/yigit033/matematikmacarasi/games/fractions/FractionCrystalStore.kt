package com.yigit033.matematikmacarasi.games.fractions

import android.content.Context
import android.util.Log

data class CrystalRun(
    val seed: Long = 0,
    val grade: Int = 3,
    val difficulty: Int = 0,
    val index: Int = 0,
    val litMask: Int = 0,
    val built: Int = 0,
    val solved: Boolean = false,
    val attempts: Int = 0,
    val errors: Int = 0,
    val hints: Int = 0,
    val cleanAnswers: Int = 0,
    val hadError: Boolean = false,
    val usedHint: Boolean = false,
    val completedRuns: Int = 0
) {
    val finished get() = seed > 0L && index == FractionCrystalEngine.TASK_COUNT
    val progress get() = if (finished) 12 else index
}

/**
 * One checkpoint per user action. A bad write never advances the in-memory state;
 * scores and first-completion rewards are delivered once per completed seed.
 */
class FractionCrystalStore(context: Context, name: String = "fraction_crystals_v1") {
    private val prefs = context.applicationContext.getSharedPreferences(name, Context.MODE_PRIVATE)
    @Volatile var lastError = ""
        private set

    @Synchronized fun load(): CrystalRun {
        val state = CrystalRun(
            seed=prefs.getLong("seed",0), grade=prefs.getInt("grade",3),
            difficulty=prefs.getInt("difficulty",0),index=prefs.getInt("index",0),
            litMask=prefs.getInt("lit",0),built=prefs.getInt("built",0),
            solved=prefs.getBoolean("solved",false),attempts=prefs.getInt("attempts",0),
            errors=prefs.getInt("errors",0),hints=prefs.getInt("hints",0),
            cleanAnswers=prefs.getInt("clean",0),hadError=prefs.getBoolean("had_error",false),
            usedHint=prefs.getBoolean("used_hint",false),
            completedRuns=prefs.getInt("completed",0)
        )
        if (validate(state)) return state
        Log.e("FractionCrystalStore","Invalid stored checkpoint: $state")
        lastError="Kayıt tutarsız. Yeni macera başlatılabilir; eski ödüller korunur."
        return CrystalRun(completedRuns=state.completedRuns.coerceAtLeast(0))
    }

    fun task(run: CrystalRun): CrystalTask? =
        if(run.seed>0 && run.index in 0 until FractionCrystalEngine.TASK_COUNT)
            FractionCrystalEngine.generate(run.seed,run.grade,run.difficulty)[run.index]
        else null

    fun validate(run: CrystalRun): Boolean {
        if(run.seed < 0 || run.grade !in 1..8 || run.difficulty !in 0..2 ||
            run.index !in 0..12 || run.attempts < 0 || run.errors !in 0..run.attempts ||
            run.hints < 0 || run.cleanAnswers !in 0..12 ||
            run.completedRuns < 0 || run.built < 0 || run.litMask < 0 ||
            run.seed==0L && (run.index!=0 || run.solved)) return false
        val task = task(run) ?: return (run.seed==0L || run.index==12) &&
            run.litMask==0 && run.built==0 && !run.solved
        return when(task.mode) {
            CrystalMode.FILL ->
                run.built==0 && run.litMask ushr task.target.denominator == 0
            CrystalMode.BRIDGE -> run.litMask==0 && run.built<=task.target.numerator
            else -> run.litMask==0 && run.built==0
        }
    }

    @Synchronized fun save(next: CrystalRun): Boolean {
        lastError=""
        if(!validate(next)) {
            lastError="Bu hamle geçerli bir oyun durumuna dönüşmedi."
            Log.e("FractionCrystalStore","Rejected checkpoint: $next")
            return false
        }
        return runCatching {
            prefs.edit().putLong("seed",next.seed).putInt("grade",next.grade)
                .putInt("difficulty",next.difficulty).putInt("index",next.index)
                .putInt("lit",next.litMask).putInt("built",next.built)
                .putBoolean("solved",next.solved).putInt("attempts",next.attempts)
                .putInt("errors",next.errors).putInt("hints",next.hints)
                .putInt("clean",next.cleanAnswers)
                .putBoolean("had_error",next.hadError)
                .putBoolean("used_hint",next.usedHint)
                .putInt("completed",next.completedRuns).commit()
        }.getOrElse {
            lastError="Cihazın kayıt alanına yazılamadı."
            Log.e("FractionCrystalStore","Could not save fraction checkpoint",it)
            false
        }.also { if(!it && lastError.isEmpty()) lastError="İlerleme kaydedilemedi." }
    }

    @Synchronized fun newRun(grade: Int, difficulty: Int): CrystalRun? {
        val previous = load()
        val newSeed = System.currentTimeMillis().coerceAtLeast(previous.seed+1).coerceAtLeast(1)
        val fresh = CrystalRun(seed=newSeed, grade=grade.coerceIn(1,8),
            difficulty=difficulty.coerceIn(0,2),completedRuns=previous.completedRuns)
        return if (save(fresh)) fresh else null
    }

    @Synchronized fun advance(run: CrystalRun): CrystalRun? {
        if (run.finished || !run.solved) return null
        val nextIndex=run.index+1
        val advanced = run.copy(
            index=nextIndex,litMask=0,built=0,solved=false,hadError=false,usedHint=false,
            cleanAnswers=run.cleanAnswers+
                if(!run.hadError && !run.usedHint) 1 else 0,
            completedRuns=run.completedRuns+if(nextIndex==12) 1 else 0)
        val skill = task(run)?.skill
        if (!save(advanced)) return null
        if(skill!=null) {
            // A user can re-enter a solved checkpoint without duplicating evidence.
            prefs.edit().putInt(skill+"_completed",prefs.getInt(skill+"_completed",0)+1)
                .apply()
        }
        return advanced
    }

    fun completedForSkill(skill: String): Int =
        prefs.getInt(skill+"_completed",0)

    fun bestClean(): Int = prefs.getInt("best_clean",0)
    @Synchronized fun updateBest(run: CrystalRun) {
        if (run.cleanAnswers > bestClean())
            prefs.edit().putInt("best_clean",run.cleanAnswers).apply()
    }

    fun firstRewardGranted(): Boolean = prefs.getBoolean("first_reward",false)
    @Synchronized fun markFirstReward(): Boolean =
        prefs.edit().putBoolean("first_reward",true).commit()

    @Synchronized fun markDelivered(seed: Long): Boolean {
        if(seed<=0 || prefs.getLong("last_delivered",-1)==seed) return false
        return prefs.edit().putLong("last_delivered",seed).commit()
    }
}
