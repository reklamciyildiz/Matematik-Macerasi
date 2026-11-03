package com.yigit033.matematikmacarasi.games.time

import kotlin.math.abs
import kotlin.random.Random

/** One deterministic 25-stop journey, with five genuinely different interaction types. */
enum class TimeWorld(val title: String, val icon: String, val skill: String) {
    CLOCK("Saat Atölyesi", "🕰️", "time_analog_set"),
    READ("Gizli Saatler", "🔎", "time_analog_read"),
    TRAIN("Zaman Treni", "🚂", "time_elapsed"),
    CALENDAR("Takvim Bahçesi", "📅", "time_calendar"),
    ROUTE("Günün Rotası", "🧭", "time_planning")
}

data class TowerDate(val year: Int, val month: Int, val day: Int): Comparable<TowerDate> {
    init { require(year in 2000..2100 && month in 1..12 && day in 1..daysInMonth(year,month)) }
    override fun compareTo(other: TowerDate): Int = compareValuesBy(this,other,
        TowerDate::year,TowerDate::month,TowerDate::day)
    val code: Int get()=year*10000+month*100+day
    fun addDays(delta: Int): TowerDate {
        require(delta in -366..366)
        var y=year; var m=month; var d=day
        repeat(abs(delta)) {
            d+=if(delta>=0) 1 else -1
            if(d>daysInMonth(y,m)) {
                d=1; m++; if(m>12) {m=1;y++}
            } else if(d<1) {
                m--; if(m<1) {m=12;y--}; d=daysInMonth(y,m)
            }
        }
        return TowerDate(y,m,d)
    }
    fun daysUntil(other: TowerDate): Int {
        if(this==other)return 0
        val direction=if(this<other)1 else -1
        var date=this
        var count=0
        while(date!=other && abs(count)<366) { date=date.addDays(direction);count+=direction }
        require(date==other)
        return count
    }
    /** Monday = 0, ... Sunday = 6. 2000-01-01 was Saturday. */
    fun weekdayMondayFirst(): Int {
        var days=0
        for(y in 2000 until year) days+=if(isLeap(y))366 else 365
        for(m in 1 until month)days+=daysInMonth(year,m)
        days+=day-1
        return (5+days)%7
    }
    fun display()="$day ${MONTHS[month-1]} $year"
    companion object {
        val MONTHS=listOf("Ocak","Şubat","Mart","Nisan","Mayıs","Haziran",
            "Temmuz","Ağustos","Eylül","Ekim","Kasım","Aralık")
        fun isLeap(year:Int)=year%400==0 || year%4==0 && year%100!=0
        fun daysInMonth(year:Int,month:Int)=when(month) {
            2->if(isLeap(year))29 else 28
            4,6,9,11->30
            else->31
        }
        fun fromCode(code:Int):TowerDate?=runCatching {
            TowerDate(code/10000,code/100%100,code%100)
        }.getOrNull()
    }
}
private fun isLeap(year:Int)=TowerDate.isLeap(year)
private fun daysInMonth(year:Int,month:Int)=TowerDate.daysInMonth(year,month)

data class TowerEvent(val id:Int,val title:String,val minute:Int,val duration:Int) {
    val endMinute:Int get()=minute+duration
}
data class TimeChallenge(
    val world:TimeWorld,
    val title:String,
    val instruction:String,
    val explanation:String,
    val grade:Int,
    val stepMinutes:Int,
    val answerMinute:Int=-1,
    val startMinute:Int=-1,
    val elapsedMinutes:Int=0,
    val rangeMinutes:Int=0,
    val firstDate:TowerDate=TowerDate(2026,1,1),
    val answerDate:TowerDate=TowerDate(2026,1,1),
    val events:List<TowerEvent> = emptyList()
) {
    val correctOrder:List<Int> get()=events.sortedBy{it.minute}.map{it.id}
}

object TimeTowerEngine {
    const val STOPS=25
    const val STOPS_PER_WORLD=5
    val difficultyNames=listOf("Temel","Orta","Zor")
    fun worldAt(index:Int):TimeWorld=TimeWorld.entries[index/STOPS_PER_WORLD]
    /** A higher difficulty never opens later-grade time concepts. */
    fun clockStep(grade:Int,difficulty:Int):Int=when {
        grade<=1->60
        grade==2->if(difficulty==0)30 else 15
        grade==3->if(difficulty==0)15 else 5
        grade==4->if(difficulty==0)15 else 5
        grade==5->if(difficulty==2)1 else 5
        else->if(difficulty==0)5 else 1
    }
    fun formatTime(total:Int):String {
        require(total in 0..2880)
        val day=total/1440
        val minute=total%1440
        val txt="${(minute/60).toString().padStart(2,'0')}:${(minute%60).toString().padStart(2,'0')}"
        return if(day>0) "$txt (+$day gün)" else txt
    }
    fun partOfDay(minutes:Int):String=when((minutes%1440)/60) {
        in 5..11->"Sabah"
        in 12..17->"Öğleden sonra"
        in 18..22->"Akşam"
        else->"Gece"
    }

    fun task(seed:Long, grade:Int, difficulty:Int, index:Int):TimeChallenge {
        require(seed>0 && grade in 1..8 && difficulty in 0..2 && index in 0 until STOPS)
        val rng=Random(seed xor ((index.toLong()+1)*104729L))
        val world=worldAt(index)
        val step=clockStep(grade,difficulty)
        val number=index%STOPS_PER_WORLD
        return when(world) {
            TimeWorld.CLOCK,TimeWorld.READ-> {
                val hour=if(grade<=2)rng.nextInt(7,20) else rng.nextInt(5,23)
                val minute=(rng.nextInt(60/step)*step)
                val target=hour*60+minute
                val title=if(world==TimeWorld.CLOCK)"Kulenin saatini ayarla"
                    else "Gizli saati çöz"
                TimeChallenge(world,title,
                    if(world==TimeWorld.CLOCK)
                        "Kule saatini ${formatTime(target)} olacak şekilde ayarla."
                    else "Analog saatin gösterdiği zamanı dijital olarak oluştur.",
                    if(world==TimeWorld.CLOCK)
                        "Akrep saatleri, yelkovan dakikaları gösterir. ${formatTime(target)}."
                    else "Yelkovan 60 dakikada, akrep 12 saatte tam tur atar. ${formatTime(target)}.",
                    grade,step,answerMinute=target)
            }
            TimeWorld.TRAIN-> {
                val unit=if(grade<=2) if(grade==1)60 else if(difficulty==0)30 else 15
                    else step
                val maxTicks=when {
                    grade<=1->2
                    grade==2->5
                    grade<=4->12
                    else->if(unit==1)100 else 20
                }
                val ticks=rng.nextInt(1,maxTicks+1)
                val elapsed=ticks*unit
                val hour=if(grade>=5 && number%2==1)rng.nextInt(21,24)
                    else rng.nextInt(6,18)
                val baseMinute=rng.nextInt(60/unit)*unit
                val start=hour*60+baseMinute
                val range=((elapsed+unit*6)/unit)*unit
                TimeChallenge(world,"Treni zamanında ulaştır",
                    "Tren ${formatTime(start)}'da ayrılıyor. $elapsed dakika sonra saat kaç? Vagonu zaman çizelgesinde ilerlet.",
                    "$elapsed dakika ${elapsed/60} saat ${elapsed%60} dakikadır. Varış: ${formatTime(start+elapsed)}.",
                    grade,unit,answerMinute=start+elapsed,startMinute=start,
                    elapsedMinutes=elapsed,rangeMinutes=range)
            }
            TimeWorld.CALENDAR-> {
                val year=listOf(2024,2025,2026,2027,2028,2029,2030,2032).random(rng)
                val month=if(number==2 && grade>=3)2 else rng.nextInt(1,13)
                val day=if(grade<=2)rng.nextInt(2,21)
                    else rng.nextInt(10,daysInMonth(year,month)+1)
                val start=TowerDate(year,month,day)
                val delta=when {
                    grade<=1->rng.nextInt(1,4)
                    grade==2->rng.nextInt(1,8)
                    grade==3->rng.nextInt(3,15)
                    grade==4->rng.nextInt(5,23)
                    else->rng.nextInt(12,51)
                }
                val answer=start.addDays(delta)
                TimeChallenge(world,"Takvim kapısını aç",
                    "${start.display()} tarihinden $delta gün sonrası hangi tarih? Takvimden seç.",
                    "${start.display()} + $delta gün = ${answer.display()}. Bir ay bitince ertesi ayın 1'i gelir.",
                    grade,step,firstDate=start,answerDate=answer)
            }
            TimeWorld.ROUTE-> {
                val titles=listOf("Kahvaltı", "Bahçe keşfi","Tren yolculuğu",
                    "Kule tamiri","Yıldız töreni")
                val count=when {
                    grade<=2->3
                    grade<=4->4
                    else->5
                }
                val spacing=when {
                    grade<=1->120
                    grade==2->60
                    grade==3->30
                    grade==4->20
                    else->15
                }
                val startHour=rng.nextInt(6,9)
                val start=startHour*60
                val events=(0 until count).map { idx->
                    val jitter=if(grade<=2)0 else rng.nextInt(0,4)*(if(grade<=4)5 else 1)
                    TowerEvent(idx,titles[idx],start+idx*spacing+jitter,
                        if(grade<=2)30 else (spacing/2).coerceAtLeast(5))
                }.shuffled(rng)
                TimeChallenge(world,"Günü sıraya koy",
                    "Etkinlikleri en erkenden en geçe sıralayarak kule programını hazırla.",
                    "Zaman ilerledikçe etkinlikler erken saatten geç saate sıralanır.",
                    grade,step,events=events)
            }
        }
    }
    fun initialMinute(task:TimeChallenge):Int=when(task.world) {
        TimeWorld.CLOCK,TimeWorld.READ->
            ((task.answerMinute-60-task.stepMinutes*2)%1440+1440)%1440
        TimeWorld.TRAIN->task.startMinute
        else->-1
    }
    fun answerCorrect(task:TimeChallenge, selectedMinute:Int,
        chosenDate:Int,chosenOrder:List<Int>):Boolean=when(task.world) {
        TimeWorld.CLOCK,TimeWorld.READ,TimeWorld.TRAIN->
            selectedMinute==task.answerMinute
        TimeWorld.CALENDAR->chosenDate==task.answerDate.code
        TimeWorld.ROUTE->chosenOrder==task.correctOrder
    }
    fun hint(task:TimeChallenge):String=when(task.world) {
        TimeWorld.CLOCK->"Yelkovan tam tur attığında 60 dakika geçer. Önce akrebi, sonra yelkovanı ayarla."
        TimeWorld.READ->"Yelkovanın 12'yi gösterdiği an dakika 00'dır. Akrep iki sayı arasında olabilir."
        TimeWorld.TRAIN->"Dakikaları önce 60'lık saatlere, sonra kalan dakikalara ayır."
        TimeWorld.CALENDAR->"Ayın son gününden sonra yeni ayın 1. günü gelir. Şubatın gün sayısını kontrol et."
        TimeWorld.ROUTE->"Önce saati, saatler eşitse dakikayı karşılaştır."
    }
}
