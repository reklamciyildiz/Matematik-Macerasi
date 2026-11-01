package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import kotlin.math.abs
import kotlin.random.Random

/** Fully offline deterministic maze + arithmetic. No Android or Compose dependencies. */
enum class MazeRegion(val title:String,val symbol:String,val subtitle:String,val instruction:String) {
    SUN_MEADOW("Güneş Çayırı","☀️","Toplamanın yolları","Sayıları birleştir"),
    STREAM("Kristal Dere","💎","Çıkarma görevi","Kalanı hesapla"),
    SECRET_GARDEN("Gizem Bahçesi","🌿","Örüntü dedektifi","Sayıların kuralını bul"),
    BRIDGE("Köprüler Vadisi","🌉","Eş grupların gücü","Çarpma köprülerini aç"),
    CAVE("Işık Mağarası","🔮","Paylaşma görevi","Gruplara eşit böl"),
    TREASURE("Hazine Pazarı","🗝️","Alışveriş hesabı","Alışveriş şifrelerini çöz"),
    CASTLE("Bulut Kalesi","🏰","Karma işlemler","Gizli kapıları aç"),
    SUMMIT("Yıldız Zirvesi","🌟","Büyük final","Tüm becerilerini kullan")
}

enum class MazeDirection(val dx:Int,val dy:Int) {
    UP(0,-1), DOWN(0,1), LEFT(-1,0), RIGHT(1,0)
}
data class MazeMathQuestion(
    val cell:Int,val prompt:String,val choices:List<Int>,
    val correct:Int,val hint:String,val explanation:String
)
data class MazeWorld(
    val width:Int,val walkable:Set<Int>,val route:List<Int>,
    val entrance:Int,val exit:Int,val questions:List<MazeMathQuestion>
) {
    val gates:Map<Int,MazeMathQuestion> get()=questions.associateBy { it.cell }
}
data class MazeExpedition(
    val seed:Long,val region:MazeRegion,val tier:LearningTier,
    val cell:Int, val solvedMask:Int=0,val pendingCell:Int=-1,
    val attempts:Int=0,val errors:Int=0,val hints:Int=0,
    val shownHint:Boolean=false,val completed:Boolean=false
) {
    val solvedCount:Int get()=Integer.bitCount(solvedMask)
}

object MazeAdventureEngine {
    const val WIDTH=11
    private const val ROOMS=5
    private val START=1+WIDTH
    fun questionCount(region:MazeRegion):Int = when(region.ordinal) {
        0,1 -> 3
        2,3 -> 4
        4,5 -> 5
        else -> 6
    }
    private fun roomCell(room:Int) = (1+(room/ROOMS)*2)*WIDTH+1+(room%ROOMS)*2

    fun generate(seed:Long,region:MazeRegion,tier:LearningTier):MazeWorld {
        require(seed>0)
        val r=Random(seed xor (0x42AF3D1L+region.ordinal*3191L+tier.ordinal*131L))
        val adjacency=Array(ROOMS*ROOMS){mutableListOf<Int>()}
        val visited=BooleanArray(ROOMS*ROOMS)
        fun carve(node:Int) {
            visited[node]=true
            val row=node/ROOMS
            val col=node%ROOMS
            val next=listOfNotNull(
                (row>0).takeIf{it}?.let{node-ROOMS},
                (row<ROOMS-1).takeIf{it}?.let{node+ROOMS},
                (col>0).takeIf{it}?.let{node-1},
                (col<ROOMS-1).takeIf{it}?.let{node+1}
            ).shuffled(r)
            for(n in next) if(!visited[n]) {
                adjacency[node].add(n);adjacency[n].add(node)
                carve(n)
            }
        }
        carve(0)
        val parent=IntArray(ROOMS*ROOMS){-1}
        val depth=IntArray(ROOMS*ROOMS)
        val queue=ArrayDeque<Int>()
        val seen=BooleanArray(ROOMS*ROOMS);seen[0]=true;queue.add(0)
        while(queue.isNotEmpty()) {
            val curr=queue.removeFirst()
            for(n in adjacency[curr]) if(!seen[n]) {
                seen[n]=true;parent[n]=curr;depth[n]=depth[curr]+1
                queue.add(n)
            }
        }
        val farthest=(1 until ROOMS*ROOMS).maxByOrNull {depth[it]} ?: 24
        val roomRoute=mutableListOf<Int>()
        var cur=farthest
        while(cur>=0){roomRoute.add(cur);cur=parent[cur]}
        roomRoute.reverse()
        val route=mutableListOf<Int>()
        val walkable=mutableSetOf<Int>()
        for(i in 0 until ROOMS*ROOMS) {
            walkable.add(roomCell(i))
            for(n in adjacency[i]) {
                walkable.add((roomCell(i)+roomCell(n))/2)
            }
        }
        for((index,room) in roomRoute.withIndex()) {
            val cell=roomCell(room)
            if(index>0) route.add((route.last()+cell)/2)
            route.add(cell)
        }
        val candidates=roomRoute.drop(1).dropLast(1)
        val count=questionCount(region)
        // Perfect-maze DFS from a corner has a farthest path of at least 8 rooms.
        require(candidates.size>=count){"Not enough mandatory gate cells"}
        val selected=(0 until count).map {i ->
            val index=((i+1)*candidates.size)/(count+1)
            candidates[index]
        }.distinct()
        require(selected.size==count)
        val questions=selected.mapIndexed {i,room ->
            makeQuestion(roomCell(room),region,tier,i,r)
        }
        return MazeWorld(WIDTH,walkable,route,START,roomCell(farthest),questions)
    }

    private fun makeQuestion(cell:Int,region:MazeRegion,tier:LearningTier,
        gate:Int,r:Random):MazeMathQuestion {
        val scale=when(tier){
            LearningTier.TEMEL -> 1
            LearningTier.GELISEN -> 2
            LearningTier.USTALIK -> 3
        }
        val mode=when(region) {
            MazeRegion.SUN_MEADOW -> 0
            MazeRegion.STREAM -> 1
            MazeRegion.SECRET_GARDEN -> 2
            MazeRegion.BRIDGE -> 3
            MazeRegion.CAVE -> 4
            MazeRegion.TREASURE -> 5
            MazeRegion.CASTLE -> (gate%4)
            MazeRegion.SUMMIT -> (gate%6)
        }
        val a:Int;val b:Int;val answer:Int;val prompt:String
        val hint:String;val explanation:String
        when(mode){
            0 -> {
                a=r.nextInt(2,9*scale+3);b=r.nextInt(2,8*scale+3);answer=a+b
                prompt="$a + $b = ?"
                hint="Önce birler ve onlar basamağını ayrı topla."
                explanation="$a ile $b'yi birleştirince $answer olur."
            }
            1 -> {
                b=r.nextInt(2,8*scale+2);a=b+r.nextInt(2,8*scale+5)
                answer=a-b;prompt="$a − $b = ?"
                hint="Büyük sayıdan küçük sayıyı çıkar."
                explanation="$a sayısından $b çıkınca $answer kalır."
            }
            2 -> {
                a=r.nextInt(2,9*scale+1);b=r.nextInt(2,3*scale+3)
                answer=a+3*b
                prompt="$a, ${a+b}, ${a+2*b}, ?"
                hint="Ardışık iki terim arasındaki artışı bul."
                explanation="Her seferinde $b artar; sıradaki sayı $answer."
            }
            3 -> {
                a=r.nextInt(2,3*scale+4);b=r.nextInt(2,3*scale+4)
                answer=a*b;prompt="$a × $b = ?"
                hint="$a tane eş grupta $b nesne olduğunu düşün."
                explanation="$a tane $b, toplam $answer eder."
            }
            4 -> {
                b=r.nextInt(2,3*scale+4);val each=r.nextInt(2,3*scale+5)
                a=b*each;answer=each;prompt="$a ÷ $b = ?"
                hint="$a nesneyi $b eş gruba paylaştır."
                explanation="$a sayısını $b eş gruba bölersen her grupta $answer olur."
            }
            else -> {
                a=r.nextInt(9*scale,9*scale+23);b=r.nextInt(2,8*scale+4)
                answer=a+b;prompt="${a} TL + ${b} TL = ?"
                hint="İki ürünün fiyatını toplarsan toplamı bulursun."
                explanation="$a TL ile $b TL'nin toplamı $answer TL."
            }
        }
        val choices=linkedSetOf(answer)
        val offsets=listOf(1,2,3,5,7,10,11,15,20,30)
            .map {it*if(scale==3)2 else 1}.shuffled(r)
        for(diff in offsets) {
            if(choices.size>=4)break
            val wrong=answer+if(r.nextBoolean())diff else -diff
            if(wrong>=0) choices.add(wrong)
        }
        var i=1
        while(choices.size<4){choices.add(answer+i++*4)}
        return MazeMathQuestion(cell,prompt,choices.shuffled(r),answer,hint,explanation)
    }

    fun begin(seed:Long,region:MazeRegion,tier:LearningTier):MazeExpedition {
        val world=generate(seed,region,tier)
        return MazeExpedition(seed,region,tier,world.entrance)
    }
    fun valid(run:MazeExpedition):Boolean {
        if(run.seed<=0||run.attempts<0||run.errors !in 0..run.attempts||
            run.hints<0 || run.pendingCell < -1) return false
        val world=generate(run.seed,run.region,run.tier)
        val max=(1 shl world.questions.size)-1
        if(run.solvedMask and max.inv()!=0 || run.cell !in world.walkable) return false
        if(run.pendingCell!=-1 && (
            run.pendingCell !in world.gates ||
            world.questions.indexOfFirst {it.cell==run.pendingCell}.let{
                it<0 || (run.solvedMask and (1 shl it))!=0
            }
        ))return false
        if(run.completed && (
            run.cell!=world.exit || run.solvedMask!=max || run.pendingCell!=-1
        ))return false
        return true
    }
    fun step(run:MazeExpedition,world:MazeWorld,dir:MazeDirection):MazeExpedition {
        if(run.completed||run.pendingCell!=-1) return run
        val x=run.cell%world.width+dir.dx
        val y=run.cell/world.width+dir.dy
        if(x !in 0 until world.width || y !in 0 until world.width)return run
        val next=y*world.width+x
        if(next !in world.walkable)return run
        val gateIdx=world.questions.indexOfFirst {it.cell==next}
        if(gateIdx>=0 && run.solvedMask and (1 shl gateIdx)==0)
            return run.copy(pendingCell=next,shownHint=false)
        return run.copy(cell=next,completed=next==world.exit &&
            run.solvedCount==world.questions.size)
    }
    fun answer(run:MazeExpedition,world:MazeWorld,selected:Int):MazeExpedition {
        if(run.pendingCell<0 || run.completed)return run
        val index=world.questions.indexOfFirst{it.cell==run.pendingCell}
        if(index<0)return run
        val question=world.questions[index]
        if(selected !in question.choices)return run
        if(selected!=question.correct) return run.copy(
            attempts=run.attempts+1,errors=run.errors+1)
        return run.copy(
            cell=question.cell,
            solvedMask=run.solvedMask or (1 shl index),
            pendingCell=-1,shownHint=false,attempts=run.attempts+1)
    }
    fun hint(run:MazeExpedition):MazeExpedition =
        if(run.pendingCell<0 || run.shownHint)return run
        else run.copy(shownHint=true,hints=run.hints+1)
    fun stars(run:MazeExpedition):Int = if(!run.completed)0 else when {
        run.errors==0 && run.hints==0 -> 3
        run.errors<=2 && run.hints<=1 -> 2
        else -> 1
    }
    fun score(run:MazeExpedition):Int =
        ((run.solvedCount*125)-(run.errors*12)-(run.hints*20)).coerceAtLeast(0)+
            if(run.completed)300 else 0
    /** Returns next deterministic safe step to final exit (even off main route). */
    fun guidance(run:MazeExpedition,world:MazeWorld):MazeDirection? {
        if(run.completed)return null
        val queue=ArrayDeque<Int>();queue.add(run.cell)
        val parent=mutableMapOf(run.cell to -1)
        while(queue.isNotEmpty()) {
            val c=queue.removeFirst()
            if(c==world.exit)break
            for(dir in MazeDirection.entries){
                val x=c%world.width+dir.dx
                val y=c/world.width+dir.dy
                if(x !in 0 until world.width||y !in 0 until world.width)continue
                val n=y*world.width+x
                if(n in world.walkable && n !in parent) {
                    parent[n]=c;queue.add(n)
                }
            }
        }
        var at=world.exit
        if(at !in parent)return null
        while(parent[at]!=run.cell && parent[at]!=-1)at=parent[at]!!
        val dx=at%world.width-run.cell%world.width
        val dy=at/world.width-run.cell/world.width
        return MazeDirection.entries.firstOrNull {it.dx==dx && it.dy==dy}
    }
}
