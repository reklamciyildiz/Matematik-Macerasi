package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import kotlin.random.Random

/**
 * S3.5 Balloon Sky 2.0: fully offline, deterministic, no timers, advertisements or
 * Android dependencies. Choices and progression are verified by pure Kotlin tests.
 */
enum class BalloonRealm(val title:String,val subtitle:String,val icon:String,val skill:String) {
    SUN("Güneş Bahçesi","Sayıları birleştir","☀️","Toplama"),
    CLOUD("Bulut Koyu","Kalanı keşfet","☁️","Çıkarma"),
    WIND("Rüzgâr Vadisi","Grupları çarp","🌬️","Çarpma"),
    STAR("Yıldız İskelesi","Eşit paylaş","⭐","Bölme"),
    RAINBOW("Gökkuşağı Zirvesi","Dört işlemin finali","🌈","Karma")
}
data class BalloonQuestion(
    val expression:String,
    val answer:Int,
    val choices:List<Int>,
    val hint:String,
    val explanation:String
)
enum class BalloonPace(val title:String,val description:String) {
    RELAXED("Keşif","Balonlar yavaş uçar; kaçırınca ceza yok"),
    CHALLENGE("Rüzgâr Yarışı","Daha hızlı balonlar; kaçırınca puan ve seri kaybı")
}
data class BalloonRun(
    val seed:Long,
    val realm:BalloonRealm,
    val tier:LearningTier,
    val pace:BalloonPace=BalloonPace.RELAXED,
    val escapes:Int=0,
    val wave:Int=0,
    val index:Int=0,
    val attempts:Int=0,
    val errors:Int=0,
    val hints:Int=0,
    val firstTry:Int=0,
    val streak:Int=0,
    val bestStreak:Int=0,
    val wrongMask:Int=0,
    val tainted:Boolean=false,
    val hintVisible:Boolean=false,
    val resolved:Boolean=false,
    val completed:Boolean=false
) {
    val solved:Int get()=index+(if(resolved)1 else 0)
}
object BalloonAdventureEngine {
    const val QUESTIONS=10
    private const val BALLOON_COUNT=4

    fun begin(seed:Long,realm:BalloonRealm,tier:LearningTier,
        pace:BalloonPace=BalloonPace.RELAXED):BalloonRun {
        require(seed>0L)
        return BalloonRun(seed,realm,tier,pace)
    }
    fun question(run:BalloonRun):BalloonQuestion=question(
        run.seed,run.realm,run.tier,run.index.coerceIn(0,QUESTIONS-1)
    )
    fun question(seed:Long,realm:BalloonRealm,tier:LearningTier,index:Int):BalloonQuestion {
        require(seed>0 && index in 0 until QUESTIONS)
        val r=Random(seed xor (0x4D29B3L+(realm.ordinal+1)*14561L+
            (tier.ordinal+1)*71123L+(index+1)*49999L))
        val mode=if(realm==BalloonRealm.RAINBOW) index%4 else realm.ordinal
        val scale=when(tier){
            LearningTier.TEMEL->1
            LearningTier.GELISEN->2
            LearningTier.USTALIK->3
        }
        val a:Int;val b:Int;val answer:Int;val expression:String
        val hint:String;val explanation:String
        when(mode) {
            0 -> {
                a=r.nextInt(2,9*scale+4);b=r.nextInt(2,8*scale+4)
                answer=a+b;expression="$a + $b = ?"
                hint="İki grubu bir araya getirerek say."
                explanation="$a ile $b birlikte $answer eder."
            }
            1 -> {
                b=r.nextInt(2,7*scale+4);a=b+r.nextInt(2,8*scale+5)
                answer=a-b;expression="$a − $b = ?"
                hint="Önce bütünü düşün, ardından eksileni çıkar."
                explanation="$a içinden $b çıkarınca $answer kalır."
            }
            2 -> {
                a=r.nextInt(2,3*scale+5);b=r.nextInt(2,3*scale+5)
                answer=a*b;expression="$a × $b = ?"
                hint="$a grupta $b tane nesne olduğunu hayal et."
                explanation="$a grup $b nesne toplam $answer yapar."
            }
            else -> {
                b=r.nextInt(2,3*scale+5);val per=r.nextInt(2,3*scale+5)
                a=b*per;answer=per;expression="$a ÷ $b = ?"
                hint="$a nesneyi $b eşit gruba paylaştır."
                explanation="$a nesne $b gruba ayrılınca her grupta $answer olur."
            }
        }
        val options=linkedSetOf(answer)
        val offsets=listOf(1,2,3,4,5,7,9,11,13,17,20).shuffled(r)
        for(delta in offsets) {
            if(options.size==BALLOON_COUNT)break
            val candidate=answer+delta*(if(r.nextBoolean())1 else -1)
            if(candidate>=0)options.add(candidate)
        }
        var supplement=1
        while(options.size<BALLOON_COUNT)options.add(answer+supplement++*3)
        return BalloonQuestion(expression,answer,options.shuffled(r),hint,explanation)
    }
    fun valid(run:BalloonRun):Boolean {
        if(run.seed<=0 || run.index !in 0..QUESTIONS ||
            run.attempts<0 || run.errors<0 || run.hints<0 ||
            run.firstTry<0 || run.streak<0 || run.bestStreak<run.streak ||
            run.wrongMask !in 0..15 || run.escapes<0 || run.wave<run.escapes ||
            run.resolved && run.completed ||
            run.completed != (run.index==QUESTIONS))return false
        if(run.firstTry>run.solved || run.attempts!=run.solved+run.errors ||
            run.hints>run.solved+1 || run.bestStreak>run.firstTry ||
            run.hintVisible && (!run.tainted || run.resolved))return false
        if(run.index==QUESTIONS &&
            (run.wrongMask!=0 || run.tainted || run.hintVisible ||
             run.resolved || run.streak>run.firstTry))return false
        if(run.index<QUESTIONS) {
            val q=question(run)
            val correctIndex=q.choices.indexOf(q.answer)
            if(correctIndex !in 0..3 || run.wrongMask and (1 shl correctIndex)!=0)
                return false
            if(run.wrongMask!=0 && !run.tainted)return false
        }
        return true
    }
    fun choose(run:BalloonRun,choice:Int):BalloonRun {
        if(!valid(run) || run.completed || run.resolved)return run
        val q=question(run)
        val optionIndex=q.choices.indexOf(choice)
        if(optionIndex<0)return run
        val bit=1 shl optionIndex
        if(run.wrongMask and bit!=0)return run
        if(choice!=q.answer) {
            return run.copy(
                attempts=run.attempts+1,errors=run.errors+1,
                wrongMask=run.wrongMask or bit,tainted=true,streak=0
            )
        }
        val clean=!run.tainted && !run.hintVisible
        val nextStreak=if(clean)run.streak+1 else 0
        return run.copy(
            attempts=run.attempts+1,
            firstTry=run.firstTry+(if(clean)1 else 0),
            streak=nextStreak,bestStreak=maxOf(run.bestStreak,nextStreak),
            hintVisible=false,resolved=true
        )
    }
    /** Escape is one complete wave; replay the SAME math question, never skip it. */
    fun escape(run:BalloonRun):BalloonRun {
        if(!valid(run) || run.completed || run.resolved)return run
        val challenge=run.pace==BalloonPace.CHALLENGE
        return run.copy(
            wave=run.wave+1,
            escapes=run.escapes+(if(challenge)1 else 0),
            wrongMask=0,
            tainted=run.tainted || challenge,
            streak=if(challenge)0 else run.streak
        )
    }
    fun hint(run:BalloonRun):BalloonRun=
        if(!valid(run) || run.completed || run.resolved || run.hintVisible)run
        else run.copy(hints=run.hints+1,hintVisible=true,tainted=true,streak=0)
    fun continueAfterAnswer(run:BalloonRun):BalloonRun {
        if(!valid(run) || !run.resolved)return run
        val next=run.index+1
        return run.copy(index=next,resolved=false,
            completed=next==QUESTIONS,
            wrongMask=0,hintVisible=false,tainted=false)
    }
    fun stars(run:BalloonRun):Int=if(!run.completed)0 else when {
        run.firstTry>=9 && run.hints==0 && run.escapes==0->3
        run.firstTry>=7 && run.escapes<=2->2
        else->1
    }
    fun score(run:BalloonRun):Int=
        (run.firstTry*110+(run.solved-run.firstTry)*65+
            run.bestStreak*15-run.hints*10-run.errors*20-run.escapes*30+
            (if(run.completed)180 else 0)).coerceAtLeast(0)
}
