package com.yigit033.matematikmacarasi.games.sayiavi2

import android.content.Context
import android.content.SharedPreferences
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

/**
 * Separate, versioned progress for 1–8 grade quests. Existing Sayı Avı 2.0
 * checkpoints, earned stars, and legacy replay history remain untouched.
 *
 * Every solved transition is committed before UI advances, including the final
 * checkpoint. Reopening a solved task advances once, never grants rewards twice.
 */
class NumberHuntV3Store(context:Context,preferenceName:String="number_hunt_worlds_v3") {
    private val prefs=context.applicationContext
        .getSharedPreferences(preferenceName,Context.MODE_PRIVATE)

    data class Session(val grade:Int,val seed:Long,val tier:LearningTier,
        val index:Int,val attempts:Int,val errors:Int,val hints:Int,
        val startedAt:Long,val solved:Boolean,val finished:Boolean,
        val contentVersion:Int=1,val partial:Set<Int> = emptySet()) {
        val taskCount:Int get()=if(contentVersion>=NumberHuntRichContent.VERSION)
            NumberHuntRichContent.TASK_COUNT else NumberHuntGradeEngine.TASK_COUNT
        val accuracy:Int get()=if(attempts==0)100 else
            ((attempts-errors).coerceAtLeast(0)*100/attempts).coerceIn(0,100)
    }
    private fun prefix(grade:Int):String="grade_${grade}_"

    @Synchronized fun load(grade:Int):Session? {
        if(grade !in 1..8)return null
        val p=prefix(grade)
        val seed=prefs.getLong(p+"seed",0L)
        if(seed==0L)return null
        val contentVersion=prefs.getInt(p+"content_version",1)
            .coerceIn(1,NumberHuntRichContent.VERSION)
        val maximum=if(contentVersion>=NumberHuntRichContent.VERSION)
            NumberHuntRichContent.TASK_COUNT else NumberHuntGradeEngine.TASK_COUNT
        val selected=prefs.getString(p+"partial_steps","").orEmpty()
            .split(',').mapNotNull { it.toIntOrNull()?.takeIf{n->n in 0..31} }
            .toSet()
        return Session(grade,seed,
            LearningTier.fromSaved(prefs.getString(p+"tier",null)),
            prefs.getInt(p+"index",0).coerceIn(0,maximum),
            prefs.getInt(p+"attempts",0).coerceAtLeast(0),
            prefs.getInt(p+"errors",0).coerceAtLeast(0),
            prefs.getInt(p+"hints",0).coerceAtLeast(0),
            prefs.getLong(p+"started",0L),prefs.getBoolean(p+"solved",false),
            prefs.getBoolean(p+"finished",false),contentVersion,selected)
    }

    @Synchronized fun begin(grade:Int,tier:LearningTier,seed:Long,startedAt:Long,
        contentVersion:Int=NumberHuntRichContent.VERSION):Boolean {
        if(grade !in 1..8 || seed==0L || startedAt<=0L ||
            contentVersion !in 1..NumberHuntRichContent.VERSION)return false
        val p=prefix(grade)
        return prefs.edit().putLong(p+"seed",seed)
            .putInt(p+"content_version",contentVersion)
            .putString(p+"partial_steps","")
            .putString(p+"tier",tier.name).putInt(p+"index",0)
            .putInt(p+"attempts",0).putInt(p+"errors",0)
            .putInt(p+"hints",0).putLong(p+"started",startedAt)
            .putBoolean(p+"solved",false).putBoolean(p+"finished",false)
            .putInt(p+"hint_index",-1)
            .commit()
    }

    @Synchronized fun attempt(grade:Int,index:Int,correct:Boolean):Boolean {
        val session=load(grade)?:return false
        if(session.index!=index || session.finished || session.solved)return false
        val p=prefix(grade)
        return prefs.edit().putInt(p+"attempts",session.attempts+1)
            .putInt(p+"errors",session.errors+if(correct)0 else 1).commit()
    }

    @Synchronized fun hint(grade:Int,index:Int):Boolean {
        val session=load(grade)?:return false
        if(session.index!=index || session.finished || session.solved)return false
        val p=prefix(grade)
        if(prefs.getInt(p+"hint_index",-1)==index)return true
        return prefs.edit().putInt(p+"hints",session.hints+1)
            .putInt(p+"hint_index",index).commit()
    }

    /** A correct solution and its attempt are written atomically. */
    @Synchronized fun solve(grade:Int,index:Int):Boolean {
        val session=load(grade)?:return false
        if(session.index!=index || session.finished)return false
        if(session.solved)return true
        val p=prefix(grade)
        return prefs.edit().putInt(p+"attempts",session.attempts+1)
            .putBoolean(p+"solved",true).commit()
    }

    /**
     * Persist each correct sub-action of a four-step puzzle. A duplicate click
     * cannot increase progress; malicious/out-of-order jumps are rejected.
     */
    @Synchronized fun savePartial(grade:Int,index:Int,next:Set<Int>):Boolean {
        val session=load(grade)?:return false
        if(session.index!=index || session.finished || session.solved ||
            session.contentVersion<NumberHuntRichContent.VERSION)return false
        if(next==session.partial)return true
        if(next.size!=session.partial.size+1 ||
            !next.containsAll(session.partial) || next.any{it !in 0..31})
            return false
        return prefs.edit().putString(prefix(grade)+"partial_steps",
            next.sorted().joinToString(",")).commit()
    }

    @Synchronized fun markSolved(grade:Int,index:Int):Boolean {
        val session=load(grade)?:return false
        if(session.index!=index || session.finished)return false
        if(session.solved)return true
        return prefs.edit().putBoolean(prefix(grade)+"solved",true).commit()
    }

    /** Advances only once, and only after a persisted successful solution. */
    @Synchronized fun advance(grade:Int,expectedIndex:Int):Boolean {
        val session=load(grade)?:return false
        if(session.index!=expectedIndex || !session.solved ||
            session.finished || expectedIndex !in 0 until session.taskCount)
            return false
        val p=prefix(grade)
        return prefs.edit().putInt(p+"index",expectedIndex+1)
            .putString(p+"partial_steps","")
            .putBoolean(p+"solved",false).putInt(p+"hint_index",-1).commit()
    }

    /** True only when a fresh run was durably completed (idempotent per seed). */
    @Synchronized fun finish(grade:Int,expectedSeed:Long):Boolean {
        val session=load(grade)?:return false
        if(session.seed!=expectedSeed ||
            session.index!=session.taskCount ||
            session.finished)return false
        val p=prefix(grade)
        return prefs.edit().putBoolean(p+"finished",true)
            .putBoolean(p+"ever_completed",true)
            .putInt(p+"completed_runs",completionCount(grade)+1).commit()
    }

    fun completionCount(grade:Int):Int=
        if(grade in 1..8)prefs.getInt(prefix(grade)+"completed_runs",0).coerceAtLeast(0) else 0

    fun hasCompleted(grade:Int):Boolean=
        grade in 1..8 && prefs.getBoolean(prefix(grade)+"ever_completed",false)

    /** StarManager is the authoritative idempotency guard; this is an acknowledgement. */
    fun starAcknowledged(grade:Int):Boolean=
        grade in 1..8 && prefs.getBoolean(prefix(grade)+"star_acknowledged",false)

    @Synchronized fun acknowledgeStar(grade:Int):Boolean =
        grade in 1..8 && prefs.edit()
            .putBoolean(prefix(grade)+"star_acknowledged",true).commit()
}
