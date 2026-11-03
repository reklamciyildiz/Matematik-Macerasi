package com.yigit033.matematikmacarasi.games.pattern

import android.content.Context
import android.util.Log

data class PatternRun(
    val seed:Long=0L, val grade:Int=3, val difficulty:Int=0, val index:Int=0,
    val picked:Int=-1, val order:String="", val solved:Boolean=false,
    val attempts:Int=0, val errors:Int=0, val hints:Int=0,
    val clean:Int=0, val streak:Int=0, val bestStreak:Int=0,
    val hadError:Boolean=false, val usedHint:Boolean=false,
    val completedRuns:Int=0
) {
    val finished get()=seed>0 && index==PatternForestEngine.STOPS
    val selected:List<Int>
        get()=if(order.isEmpty()) emptyList()
        else order.split(",").mapNotNull{it.toIntOrNull()}
}

class PatternForestStore(context:Context,name:String="pattern_forest_v1") {
    private val prefs=context.applicationContext.getSharedPreferences(name,Context.MODE_PRIVATE)
    @Volatile var lastError:String=""
        private set

    @Synchronized fun load():PatternRun {
        val s=PatternRun(
            seed=prefs.getLong("seed",0),grade=prefs.getInt("grade",3),
            difficulty=prefs.getInt("difficulty",0),index=prefs.getInt("index",0),
            picked=prefs.getInt("picked",-1),order=prefs.getString("order","").orEmpty(),
            solved=prefs.getBoolean("solved",false),
            attempts=prefs.getInt("attempts",0),errors=prefs.getInt("errors",0),
            hints=prefs.getInt("hints",0),clean=prefs.getInt("clean",0),
            streak=prefs.getInt("streak",0),bestStreak=prefs.getInt("best_streak",0),
            hadError=prefs.getBoolean("had_error",false),
            usedHint=prefs.getBoolean("hint_used",false),
            completedRuns=prefs.getInt("runs",0)
        )
        if(valid(s))return s
        Log.e("PatternForestStore","Invalid stored pattern checkpoint: $s")
        lastError="Eski oyun kaydı tutarsız. Yeni bir macera başlatabilirsin."
        return PatternRun(completedRuns=s.completedRuns.coerceAtLeast(0))
    }

    fun challenge(s:PatternRun):PatternTask? =
        if(s.seed>0 && s.index in 0 until PatternForestEngine.STOPS)
            PatternForestEngine.task(s.seed,s.grade,s.difficulty,s.index) else null

    fun valid(s:PatternRun):Boolean {
        if(s.seed<0 || s.grade !in 1..8 || s.difficulty !in 0..2 ||
            s.index !in 0..PatternForestEngine.STOPS ||
            s.attempts<0 || s.errors !in 0..s.attempts ||
            s.hints<0 || s.clean !in 0..PatternForestEngine.STOPS ||
            s.streak !in 0..PatternForestEngine.STOPS ||
            s.bestStreak !in s.streak..PatternForestEngine.STOPS ||
            s.completedRuns<0)return false
        val task=challenge(s)?:return (s.seed==0L || s.finished) &&
            !s.solved && s.picked==-1 && s.order.isEmpty() &&
            (s.seed!=0L || s.index==0)
        val selection=s.selected
        if(s.order!=selection.joinToString(","))return false
        if(!PatternForestEngine.validSelection(task,s.picked,selection))return false
        return !s.solved || PatternForestEngine.correct(task,s.picked,selection)
    }

    private fun commit(next:PatternRun,skill:String?=null,firstTry:Boolean=false):Boolean {
        lastError=""
        if(!valid(next)){
            lastError="Bu hamle geçerli bir örüntü durumuna dönüşmedi."
            Log.e("PatternForestStore","Rejected checkpoint: $next")
            return false
        }
        return try {
            val e=prefs.edit().putLong("seed",next.seed).putInt("grade",next.grade)
                .putInt("difficulty",next.difficulty).putInt("index",next.index)
                .putInt("picked",next.picked).putString("order",next.order)
                .putBoolean("solved",next.solved).putInt("attempts",next.attempts)
                .putInt("errors",next.errors).putInt("hints",next.hints)
                .putInt("clean",next.clean).putInt("streak",next.streak)
                .putInt("best_streak",next.bestStreak)
                .putBoolean("had_error",next.hadError)
                .putBoolean("hint_used",next.usedHint)
                .putInt("runs",next.completedRuns)
            if(skill!=null){
                e.putInt(skill+"_completed",prefs.getInt(skill+"_completed",0)+1)
                if(firstTry)e.putInt(skill+"_clean",prefs.getInt(skill+"_clean",0)+1)
            }
            if(next.finished && next.clean>prefs.getInt("best_clean",0))
                e.putInt("best_clean",next.clean)
            val ok=e.commit()
            if(!ok)lastError="Cihazın depolama alanına yazılamadı. Tekrar dene."
            ok
        }catch(e:RuntimeException){
            Log.e("PatternForestStore","Checkpoint commit failed",e)
            lastError="İlerleme kaydı başarısız oldu. Depolama alanını kontrol et."
            false
        }
    }

    @Synchronized fun save(next:PatternRun):Boolean {
        val old=load()
        if(old.seed==0L || old.finished || next.seed!=old.seed ||
            next.grade!=old.grade || next.difficulty!=old.difficulty ||
            next.index!=old.index || old.solved && !next.solved){
            lastError="Bu bölüm artık geçerli değil. Devam etmek için oyuna tekrar gir."
            return false
        }
        return commit(next)
    }

    @Synchronized fun start(grade:Int,difficulty:Int):PatternRun? {
        val old=load()
        val seed=System.currentTimeMillis().coerceAtLeast(old.seed+1L).coerceAtLeast(1L)
        val run=PatternRun(seed=seed,grade=grade.coerceIn(1,8),
            difficulty=difficulty.coerceIn(0,2),completedRuns=old.completedRuns)
        return if(commit(run))run else null
    }

    @Synchronized fun advance(expected:PatternRun):PatternRun? {
        val stored=load()
        if(stored.seed!=expected.seed || stored.index!=expected.index ||
            !stored.solved || !expected.solved || stored.finished){
            lastError="Bu bölüm zaten tamamlanmış ya da henüz çözülmemiş."
            return null
        }
        val task=challenge(stored)?:return null
        val index=stored.index+1
        val firstTry= !stored.hadError && !stored.usedHint
        val streak=if(firstTry)stored.streak+1 else 0
        val next=stored.copy(
            index=index,picked=-1,order="",solved=false,
            hadError=false,usedHint=false,streak=streak,
            bestStreak=maxOf(stored.bestStreak,streak),
            clean=stored.clean+if(firstTry)1 else 0,
            completedRuns=stored.completedRuns+
                if(index==PatternForestEngine.STOPS)1 else 0
        )
        return if(commit(next,task.world.skill,firstTry))next else null
    }

    fun completedFor(skill:String):Int=prefs.getInt(skill+"_completed",0)
    fun cleanFor(skill:String):Int=prefs.getInt(skill+"_clean",0)
    fun bestClean():Int=prefs.getInt("best_clean",0)
    fun rewardMarked():Boolean=prefs.getBoolean("first_reward",false)
    @Synchronized fun markReward():Boolean =
        prefs.edit().putBoolean("first_reward",true).commit()
    fun deliveredSeed():Long=prefs.getLong("delivered_seed",-1)
    @Synchronized fun markDelivered(seed:Long):Boolean {
        if(seed<=0 || deliveredSeed()==seed)return false
        return prefs.edit().putLong("delivered_seed",seed).commit()
    }
}
