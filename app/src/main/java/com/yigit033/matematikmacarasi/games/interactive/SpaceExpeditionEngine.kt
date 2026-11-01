package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import kotlin.random.Random

/** Deterministic mission rules. No Android, timers or network. */
enum class SpacePlanet(val title:String,val detail:String,val sign:String) {
    TERRA("Zümrüt Yörünge","Sayı enerjisini birleştir","✦"),
    AQUA("Okyanus Nebulası","Kalan yakıtı hesapla","≈"),
    PYRA("Kızıl Meteor","Güç gruplarını yönet","◆"),
    LUNA("Ay Kristali","Enerjiyi eşit dağıt","☾"),
    NOVA("Prizma Galaksisi","Dört işlem ustalığı","✧")
}
enum class SpaceMission(val title:String) {
    FLIGHT("Kristal Pilotluğu"),
    REACTOR("Reaktör Atölyesi"),
    CIRCUIT("Yıldız Devresi")
}
data class SpacePuzzle(
    val prompt:String,
    val answer:Int,
    val explanation:String,
    val hint:String,
    val crystals:List<Int> = emptyList(),
    val sequence:List<Int> = emptyList(),
    val nodes:List<Int> = emptyList()
)
data class SpaceRun(
    val seed:Long,
    val planet:SpacePlanet,
    val tier:LearningTier,
    val stage:Int=0,
    val lane:Int=1,
    val wave:Int=0,
    val charge:Int=0,
    val path:List<Int> = emptyList(),
    val solved:Boolean=false,
    val completed:Boolean=false,
    val attempts:Int=0,
    val errors:Int=0,
    val hints:Int=0,
    val firstTry:Int=0,
    val tainted:Boolean=false,
    val hintVisible:Boolean=false,
    val shields:Int=3
) {
    val mission:SpaceMission get()=SpaceMission.entries[stage.coerceIn(0,2)]
}
object SpaceExpeditionEngine {
    const val STAGES=3
    fun begin(seed:Long,planet:SpacePlanet,tier:LearningTier):SpaceRun {
        require(seed>0)
        return SpaceRun(seed,planet,tier)
    }
    fun puzzle(run:SpaceRun):SpacePuzzle =
        puzzle(run.seed,run.planet,run.tier,run.stage.coerceIn(0,2))

    fun puzzle(seed:Long,planet:SpacePlanet,tier:LearningTier,stage:Int):SpacePuzzle {
        require(seed>0 && stage in 0 until STAGES)
        val rng=Random(seed xor (planet.ordinal*113117L+stage*39971L+
            tier.ordinal*97339L+0x5A61B3L))
        val scale=tier.ordinal+1
        if(stage==2) {
            val descending=planet==SpacePlanet.AQUA
            val start=if(descending)rng.nextInt(18,25)*scale
                else rng.nextInt(1,6)*scale
            val step=rng.nextInt(2,5)*scale*(if(descending)-1 else 1)
            val sequence=(1..3).map {start+it*step}
            val falseNode=sequence.last()+step
            val nodes=(sequence+falseNode).shuffled(rng)
            return SpacePuzzle(
                prompt="$start ile başla. Her adımda "+kotlin.math.abs(step)+
                    (if(descending)" azalt." else " artır.")+
                    " Üç yıldızı sırayla bağla.",
                answer=sequence.last(),
                explanation="Devre sırası: "+(listOf(start)+sequence).joinToString(" → "),
                hint="Her yeni yıldızda "+kotlin.math.abs(step)+
                    (if(descending)" çıkar." else " ekle."),
                sequence=sequence,nodes=nodes
            )
        }
        val operation=if(planet==SpacePlanet.NOVA)
            (stage+((seed%4).toInt()))%4 else planet.ordinal
        val a:Int;val b:Int;val target:Int;val formula:String
        val explanation:String;val hint:String
        when(operation){
            0->{
                a=rng.nextInt(3,8*scale+4);b=rng.nextInt(2,6*scale+3)
                target=a+b;formula="$a + $b"
                explanation="$a enerji ve $b enerji birleşince $target eder."
                hint="İki enerji grubunu birleştir."
            }
            1->{
                b=rng.nextInt(2,4*scale+3);target=rng.nextInt(2,5*scale+4)
                a=b+target;formula="$a − $b"
                explanation="$a enerjiden $b çıkarınca $target kalır."
                hint="Önce toplam enerjiyi düşün, tüketileni çıkar."
            }
            2->{
                a=rng.nextInt(2,3*scale+3);b=rng.nextInt(2,3*scale+3)
                target=a*b;formula="$a × $b"
                explanation="$a grup, her birinde $b enerji: $target."
                hint="$b sayısını $a kez tekrarlı topla."
            }
            else->{
                b=rng.nextInt(2,3*scale+3);target=rng.nextInt(2,3*scale+3)
                a=b*target;formula="$a ÷ $b"
                explanation="$a enerji $b gruba eşit dağıtılınca $target düşer."
                hint="$b ile çarpınca $a veren sayıyı bul."
            }
        }
        if(stage==1) {
            return SpacePuzzle(
                prompt="Reaktöre $formula işleminin sonucu kadar enerji yükle.",
                answer=target,explanation=explanation,hint=hint
            )
        }
        val options=linkedSetOf(target)
        var d=1
        while(options.size<3){
            val candidate=target+(if(d%2==0)-1 else 1)*(d+1)
            if(candidate>=0)options.add(candidate)
            d++
        }
        val crystals=options.toList().shuffled(rng)
        return SpacePuzzle(
            prompt="$formula = ? Doğru kristali geminle yakala.",
            answer=target,explanation=explanation,hint=hint,crystals=crystals
        )
    }
    fun valid(run:SpaceRun):Boolean {
        if(run.seed<=0 || run.stage !in 0..STAGES ||
            run.lane !in 0..2 || run.wave<0 || run.charge<0 ||
            run.attempts<0 || run.errors<0 || run.hints<0 ||
            run.firstTry<0 || run.shields !in 1..3 ||
            run.completed!=(run.stage==STAGES) ||
            run.completed && run.solved ||
            run.solved && run.hintVisible ||
            run.hintVisible && !run.tainted ||
            run.firstTry>run.stage+(if(run.solved)1 else 0) ||
            run.attempts!=run.stage+(if(run.solved)1 else 0)+run.errors ||
            run.hints>run.stage+1
        )return false
        if(run.completed)return run.charge==0 && run.path.isEmpty() &&
            !run.tainted && !run.hintVisible
        val p=puzzle(run)
        if(run.charge>p.answer+10 || (run.stage!=1 && run.charge!=0) ||
            (run.stage!=2 && run.path.isNotEmpty()) ||
            run.path.size>p.sequence.size || run.solved &&
            (run.charge!=0 || run.path.isNotEmpty()))return false
        if(run.stage==2 && run.path!=p.sequence.take(run.path.size))return false
        return true
    }
    fun steer(run:SpaceRun,lane:Int):SpaceRun =
        if(!valid(run)||run.completed||run.solved||run.stage!=0||lane !in 0..2)run
        else run.copy(lane=lane)
    /** The crystal wave reaches the ship; exactly one selected lane is intercepted. */
    fun intercept(run:SpaceRun):SpaceRun {
        if(!valid(run)||run.completed||run.solved||run.stage!=0)return run
        val q=puzzle(run)
        if(q.crystals[run.lane]==q.answer)return finishStage(run)
        return failure(run.copy(wave=run.wave+1))
    }
    fun adjustFuel(run:SpaceRun,amount:Int):SpaceRun {
        if(!valid(run)||run.completed||run.solved||run.stage!=1)return run
        val target=puzzle(run).answer
        return run.copy(charge=(run.charge+amount).coerceIn(0,target+10))
    }
    fun ignite(run:SpaceRun):SpaceRun {
        if(!valid(run)||run.completed||run.solved||run.stage!=1)return run
        return if(run.charge==puzzle(run).answer)finishStage(run) else failure(run)
    }
    fun connect(run:SpaceRun,node:Int):SpaceRun {
        if(!valid(run)||run.completed||run.solved||run.stage!=2)return run
        val q=puzzle(run)
        if(node !in q.nodes || node in run.path)return run
        if(node!=q.sequence[run.path.size])return failure(run)
        val next=run.copy(path=run.path+node)
        return if(next.path.size==q.sequence.size)
            finishStage(next.copy(path=emptyList())) else next
    }
    fun hint(run:SpaceRun):SpaceRun =
        if(!valid(run)||run.completed||run.solved||run.hintVisible)run
        else run.copy(hints=run.hints+1,tainted=true,hintVisible=true)
    fun next(run:SpaceRun):SpaceRun {
        if(!valid(run)||!run.solved)return run
        val stage=run.stage+1
        return run.copy(stage=stage,completed=stage==STAGES,
            solved=false,tainted=false,hintVisible=false,
            charge=0,path=emptyList(),lane=1,wave=0)
    }
    private fun failure(run:SpaceRun):SpaceRun =
        run.copy(attempts=run.attempts+1,errors=run.errors+1,
            shields=(run.shields-1).coerceAtLeast(1),tainted=true)
    private fun finishStage(run:SpaceRun):SpaceRun =
        run.copy(solved=true,attempts=run.attempts+1,
            firstTry=run.firstTry+(if(run.tainted)0 else 1),
            hintVisible=false,charge=0,path=emptyList())
    fun stars(run:SpaceRun):Int=if(!run.completed)0 else when {
        run.firstTry==STAGES && run.hints==0 ->3
        run.firstTry>=2 && run.errors<=2 ->2
        else ->1
    }
    fun score(run:SpaceRun):Int =
        (run.firstTry*230+(run.stage-run.firstTry).coerceAtLeast(0)*120+
            run.shields*30-run.errors*35-run.hints*20).coerceAtLeast(0)
}
