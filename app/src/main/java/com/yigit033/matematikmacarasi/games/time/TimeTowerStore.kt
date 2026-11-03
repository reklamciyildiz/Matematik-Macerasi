package com.yigit033.matematikmacarasi.games.time

import android.content.Context
import android.util.Log

data class TowerRun(
    val seed:Long=0L,
    val grade:Int=2,
    val difficulty:Int=0,
    val index:Int=0,
    val cursorMinutes:Int=-1,
    val chosenDate:Int=0,
    val route:String="",
    val solved:Boolean=false,
    val attempts:Int=0,
    val errors:Int=0,
    val hints:Int=0,
    val clean:Int=0,
    val streak:Int=0,
    val bestStreak:Int=0,
    val hadError:Boolean=false,
    val usedHint:Boolean=false,
    val completedRuns:Int=0
) {
    val finished get()=seed>0 && index==TimeTowerEngine.STOPS
    val routeIds:List<Int> get()=
        if(route.isBlank()) emptyList() else route.split(",").mapNotNull{it.toIntOrNull()}
}

/** Atomic checkpoints + skill evidence; no edits to previous games' progress or billing. */
class TimeTowerStore(context:Context, name:String="time_tower_v1") {
    private val prefs=context.applicationContext.getSharedPreferences(name,Context.MODE_PRIVATE)
    @Volatile var lastError=""
        private set

    @Synchronized fun load():TowerRun {
        val s=TowerRun(
            seed=prefs.getLong("seed",0L),
            grade=prefs.getInt("grade",2),
            difficulty=prefs.getInt("difficulty",0),
            index=prefs.getInt("index",0),
            cursorMinutes=prefs.getInt("cursor",-1),
            chosenDate=prefs.getInt("date",0),
            route=prefs.getString("route","").orEmpty(),
            solved=prefs.getBoolean("solved",false),
            attempts=prefs.getInt("attempts",0),
            errors=prefs.getInt("errors",0),
            hints=prefs.getInt("hints",0),
            clean=prefs.getInt("clean",0),
            streak=prefs.getInt("streak",0),
            bestStreak=prefs.getInt("best_streak",0),
            hadError=prefs.getBoolean("task_error",false),
            usedHint=prefs.getBoolean("task_hint",false),
            completedRuns=prefs.getInt("completed",0)
        )
        if(valid(s)) return s
        Log.e("TimeTowerStore","Corrupt checkpoint (unmodified on disk): $s")
        lastError="Önceki kayıt tutarsız. Yeni macera başlatabilirsin; ödüllerin korunur."
        return TowerRun(completedRuns=s.completedRuns.coerceAtLeast(0))
    }

    fun challenge(run:TowerRun):TimeChallenge? =
        if(run.seed>0 && run.index in 0 until TimeTowerEngine.STOPS)
            TimeTowerEngine.task(run.seed,run.grade,run.difficulty,run.index)
        else null

    fun valid(run:TowerRun):Boolean {
        if(run.seed<0 || run.grade !in 1..8 || run.difficulty !in 0..2 ||
            run.index !in 0..TimeTowerEngine.STOPS ||
            run.attempts<0 || run.errors !in 0..run.attempts ||
            run.hints<0 || run.clean !in 0..TimeTowerEngine.STOPS ||
            run.streak<0 || run.streak>TimeTowerEngine.STOPS ||
            run.bestStreak<run.streak || run.bestStreak>TimeTowerEngine.STOPS ||
            run.completedRuns<0) return false
        val c=challenge(run)?:return (run.seed==0L || run.finished) &&
            !run.solved && run.cursorMinutes==-1 && run.chosenDate==0 &&
            run.route.isEmpty() && (run.seed!=0L || run.index==0)
        val ids=run.routeIds
        if(ids.size>c.events.size || ids.distinct().size!=ids.size ||
            ids.any{id->c.events.none{it.id==id}} || run.route!=ids.joinToString(",")) return false
        val cleanFields=when(c.world) {
            TimeWorld.CLOCK,TimeWorld.READ->
                run.cursorMinutes in 0 until 1440 && run.chosenDate==0 && ids.isEmpty()
            TimeWorld.TRAIN->run.cursorMinutes in c.startMinute..
                (c.startMinute+c.rangeMinutes) && run.chosenDate==0 &&
                ids.isEmpty() && (run.cursorMinutes-c.startMinute)%c.stepMinutes==0
            TimeWorld.CALENDAR->run.cursorMinutes==-1 && ids.isEmpty() &&
                (run.chosenDate==0 || TowerDate.fromCode(run.chosenDate)!=null)
            TimeWorld.ROUTE->run.cursorMinutes==-1 && run.chosenDate==0
        }
        return cleanFields && (!run.solved || TimeTowerEngine.answerCorrect(
            c,run.cursorMinutes,run.chosenDate,ids))
    }

    private fun commit(next:TowerRun, evidenceSkill:String?=null,
        firstTry:Boolean=false):Boolean {
        lastError=""
        if(!valid(next)){
            lastError="Bu hamle geçerli bir kayıt durumuna dönüşmedi."
            Log.e("TimeTowerStore","Rejected checkpoint: $next")
            return false
        }
        return try {
            val editor=prefs.edit()
                .putLong("seed",next.seed).putInt("grade",next.grade)
                .putInt("difficulty",next.difficulty).putInt("index",next.index)
                .putInt("cursor",next.cursorMinutes).putInt("date",next.chosenDate)
                .putString("route",next.route).putBoolean("solved",next.solved)
                .putInt("attempts",next.attempts).putInt("errors",next.errors)
                .putInt("hints",next.hints).putInt("clean",next.clean)
                .putInt("streak",next.streak).putInt("best_streak",next.bestStreak)
                .putBoolean("task_error",next.hadError)
                .putBoolean("task_hint",next.usedHint)
                .putInt("completed",next.completedRuns)
            if(evidenceSkill!=null){
                editor.putInt("${evidenceSkill}_completed",
                    prefs.getInt("${evidenceSkill}_completed",0)+1)
                if(firstTry)editor.putInt("${evidenceSkill}_clean",
                    prefs.getInt("${evidenceSkill}_clean",0)+1)
            }
            if(next.finished && next.clean>prefs.getInt("best_clean",0))
                editor.putInt("best_clean",next.clean)
            if(!editor.commit()) {
                lastError="Depolama alanına yazılamadı. Aynı hamleyi yeniden dene."
                Log.e("TimeTowerStore","SharedPreferences commit returned false")
                false
            } else true
        }catch(e:RuntimeException) {
            Log.e("TimeTowerStore","Checkpoint write exception",e)
            lastError="Cihaz kaydı başarısız. Depolama alanını kontrol et."
            false
        }
    }

    @Synchronized fun save(next:TowerRun):Boolean {
        val stored=load()
        if(stored.seed>0 && !stored.finished && next.seed==stored.seed &&
            next.index!=stored.index) {
            lastError="Bu görev değişikliği yalnızca tamamlayarak yapılabilir."
            return false
        }
        return commit(next)
    }

    @Synchronized fun start(grade:Int,difficulty:Int):TowerRun? {
        val old=load()
        val seed=System.currentTimeMillis().coerceAtLeast(old.seed+1).coerceAtLeast(1)
        val clampedGrade=grade.coerceIn(1,8)
        val clampedDifficulty=difficulty.coerceIn(0,2)
        val task=TimeTowerEngine.task(seed,clampedGrade,clampedDifficulty,0)
        val fresh=TowerRun(seed=seed,grade=clampedGrade,difficulty=clampedDifficulty,
            cursorMinutes=TimeTowerEngine.initialMinute(task),
            completedRuns=old.completedRuns)
        return if(commit(fresh))fresh else null
    }

    @Synchronized fun advance(run:TowerRun):TowerRun? {
        val previous=load()
        if(previous.seed!=run.seed || previous.index!=run.index || !previous.solved ||
            !run.solved || previous.finished) {
            lastError="Bölüm zaten ilerledi veya henüz tamamlanmadı."
            return null
        }
        val challenge=challenge(previous)?:return null
        val nextIndex=previous.index+1
        val nextTask=if(nextIndex<TimeTowerEngine.STOPS)
            TimeTowerEngine.task(run.seed,run.grade,run.difficulty,nextIndex) else null
        val clean= !run.hadError && !run.usedHint
        val streak=if(clean)previous.streak+1 else 0
        val next=previous.copy(
            index=nextIndex,
            cursorMinutes=if(nextTask!=null)TimeTowerEngine.initialMinute(nextTask) else -1,
            chosenDate=0,route="",solved=false,
            clean=previous.clean+if(clean)1 else 0,streak=streak,
            bestStreak=maxOf(previous.bestStreak,streak),
            hadError=false,usedHint=false,
            completedRuns=previous.completedRuns+if(nextIndex==TimeTowerEngine.STOPS)1 else 0
        )
        return if(commit(next,challenge.world.skill,clean)) next else null
    }

    fun skillCompleted(skill:String)=prefs.getInt("${skill}_completed",0)
    fun skillClean(skill:String)=prefs.getInt("${skill}_clean",0)
    fun bestClean()=prefs.getInt("best_clean",0)

    fun rewardRecorded():Boolean=prefs.getBoolean("first_reward_recorded",false)
    @Synchronized fun markRewardRecorded():Boolean=
        prefs.edit().putBoolean("first_reward_recorded",true).commit()

    fun deliveredSeed():Long=prefs.getLong("delivered_seed",-1)
    @Synchronized fun markDelivered(seed:Long):Boolean {
        if(seed<=0 || deliveredSeed()==seed)return false
        return prefs.edit().putLong("delivered_seed",seed).commit()
    }
}
