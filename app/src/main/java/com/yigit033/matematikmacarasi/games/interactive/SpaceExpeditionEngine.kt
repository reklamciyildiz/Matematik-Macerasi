package com.yigit033.matematikmacarasi.games.interactive

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import kotlin.random.Random

/** Shooting is the only answer interaction: no reactor sliders or modal questions. */
enum class SpacePlanet(val title:String,val detail:String,val sign:String) {
    TERRA("Zümrüt Yörünge","Sayı yıldızları","✦"),
    AQUA("Okyanus Nebulası","Mavi meteorlar","≈"),
    PYRA("Kızıl Meteor","Kızıl galaksi","◆"),
    LUNA("Ay Kristali","Ay ışığı","☾"),
    NOVA("Prizma Galaksisi","Son keşif","✧")
}

data class SpacePuzzle(
    val expression:String,
    val answer:Int,
    val explanation:String,
    val hint:String,
    val crystals:List<Int>
) {
    val prompt:String get()="$expression = ?"
}

data class SpaceRun(
    val seed:Long,
    val planet:SpacePlanet,
    val tier:LearningTier,
    /** Original profile grade captured at expedition start, clamped to primary 1..4. */
    val grade:Int=1,
    /** Completed questions within the continuously scrolling 15-hit expedition. */
    val stage:Int=0,
    val shipX:Float=.5f,
    val wave:Int=0,
    val completed:Boolean=false,
    val attempts:Int=0,
    val errors:Int=0,
    val hints:Int=0,
    val firstTry:Int=0,
    val tainted:Boolean=false,
    val hintVisible:Boolean=false,
    val combo:Int=0,
    val bestCombo:Int=0,
    val shields:Int=3
)

/** Deterministic grade-gated questions; the school grade is a hard curriculum ceiling. */
object SpaceExpeditionEngine {
    const val STAGES=15
    fun begin(seed:Long,planet:SpacePlanet,tier:LearningTier,grade:Int=1):SpaceRun {
        require(seed>0L)
        return SpaceRun(seed,planet,tier,grade.coerceIn(1,4))
    }
    fun puzzle(run:SpaceRun):SpacePuzzle =
        puzzle(run.seed,run.planet,run.tier,run.stage.coerceIn(0,STAGES-1),run.grade)

    fun puzzle(seed:Long,planet:SpacePlanet,tier:LearningTier,stage:Int,grade:Int):SpacePuzzle {
        require(seed>0L && stage in 0 until STAGES && grade in 1..4)
        val rng=Random(seed xor (planet.ordinal.toLong()*113117L+
            stage*39971L+tier.ordinal*97339L+grade*17471L+0x5A61B3L))
        val op=when(grade){
            1->if(planet==SpacePlanet.AQUA || (stage%5==4))1 else 0
            2->if(planet==SpacePlanet.AQUA || stage%4==3)1 else 0
            3->when(planet){
                SpacePlanet.PYRA->2
                SpacePlanet.LUNA->3
                SpacePlanet.AQUA->1
                else->(stage+planet.ordinal)%4
            }
            else->when(planet){
                SpacePlanet.TERRA->0
                SpacePlanet.AQUA->1
                SpacePlanet.PYRA->2
                SpacePlanet.LUNA->3
                SpacePlanet.NOVA->(stage+seed.toInt().ushr(2))%4
            }
        }
        val difficulty=tier.ordinal
        val a:Int;val b:Int;val answer:Int
        val symbol:String;val explanation:String;val hint:String
        when(op){
            0->{
                val cap=when(grade){
                    1->listOf(10,15,20)[difficulty]
                    2->listOf(20,50,100)[difficulty]
                    3->listOf(50,100,200)[difficulty]
                    else->listOf(100,250,500)[difficulty]
                }
                answer=rng.nextInt(4,cap+1)
                a=rng.nextInt(1,answer);b=answer-a
                symbol="+"
                explanation="$a ile $b toplanınca $answer olur."
                hint="İki sayıyı birleştir. Küçük parçadan başlayabilirsin."
            }
            1->{
                val cap=when(grade){
                    1->listOf(10,15,20)[difficulty]
                    2->listOf(20,50,100)[difficulty]
                    3->listOf(50,100,200)[difficulty]
                    else->listOf(100,250,500)[difficulty]
                }
                a=rng.nextInt(4,cap+1);b=rng.nextInt(1,a)
                answer=a-b
                symbol="−"
                explanation="$a içinden $b çıkarılınca $answer kalır."
                hint="Büyük sayıdan küçük olanı çıkar."
            }
            2->{
                val maxFactor=when(grade){
                    3->listOf(5,7,10)[difficulty]
                    else->listOf(6,9,12)[difficulty]
                }
                a=rng.nextInt(2,maxFactor+1)
                b=rng.nextInt(2,maxFactor+1)
                answer=a*b;symbol="×"
                explanation="$a tane $b, toplam $answer yapar."
                hint="$b sayısını $a kez toplayabilirsin."
            }
            else->{
                val maxFactor=when(grade){
                    3->listOf(5,7,10)[difficulty]
                    else->listOf(6,9,12)[difficulty]
                }
                b=rng.nextInt(2,maxFactor+1)
                answer=rng.nextInt(2,maxFactor+1)
                a=answer*b;symbol="÷"
                explanation="$a, $b eşit gruba ayrılırsa her grupta $answer olur."
                hint="$b ile hangi sayıyı çarparsak $a olur?"
            }
        }
        val choices=linkedSetOf(answer)
        val distance=when(grade){1->2;2->4;3->5;else->8}
        var tries=0
        while(choices.size<3 && tries<100){
            tries++
            val delta=rng.nextInt(1,distance+2)*(if(tries%2==0)-1 else 1)
            val other=answer+delta
            if(other>=0 && other!=answer)choices.add(other)
        }
        var candidate=answer+1
        while(choices.size<3){choices.add(candidate++ )}
        return SpacePuzzle(
            expression="$a $symbol $b",
            answer=answer,
            explanation=explanation,
            hint=hint,
            crystals=choices.toList().shuffled(rng)
        )
    }

    fun valid(run:SpaceRun):Boolean {
        if(run.seed<=0 || run.grade !in 1..4 || run.stage !in 0..STAGES ||
            !run.shipX.isFinite() || run.shipX !in 0.06f..0.94f ||
            run.wave<0 || run.errors<0 || run.hints<0 || run.attempts<0 ||
            run.firstTry !in 0..run.stage || run.combo !in 0..run.stage ||
            run.bestCombo !in 0..run.stage || run.combo>run.bestCombo ||
            run.shields !in 1..3 || run.completed!=(run.stage==STAGES) ||
            run.attempts!=run.stage+run.errors ||
            run.hints>run.stage+1 || run.completed && (run.tainted || run.hintVisible) ||
            run.hintVisible && !run.tainted)return false
        return true
    }
    fun position(run:SpaceRun,x:Float):SpaceRun {
        if(!valid(run)||run.completed||!x.isFinite())return run
        val value=x.coerceIn(.06f,.94f)
        return if(kotlin.math.abs(value-run.shipX)<.005f)run
            else run.copy(shipX=value)
    }
    /** One confirmed laser collision: correct hits immediately spawn the next question. */
    fun fireAt(run:SpaceRun,value:Int):SpaceRun {
        if(!valid(run)||run.completed)return run
        val q=puzzle(run)
        if(value !in q.crystals)return run
        if(value!=q.answer)return run.copy(
            attempts=run.attempts+1,
            errors=run.errors+1,
            wave=run.wave+1,
            tainted=true,
            combo=0,
            shields=(run.shields-1).coerceAtLeast(1)
        )
        val nextStage=run.stage+1
        val streak=run.combo+1
        return run.copy(
            stage=nextStage,
            completed=nextStage==STAGES,
            attempts=run.attempts+1,
            firstTry=run.firstTry+(if(!run.tainted)1 else 0),
            combo=streak,
            bestCombo=maxOf(run.bestCombo,streak),
            tainted=false,
            hintVisible=false,
            wave=0
        )
    }
    fun hint(run:SpaceRun):SpaceRun =
        if(!valid(run)||run.completed||run.hintVisible)run
        else run.copy(hints=run.hints+1,tainted=true,hintVisible=true)

    fun stars(run:SpaceRun):Int=if(!run.completed)0 else when {
        run.firstTry>=13 && run.errors<=2 && run.hints<=1->3
        run.firstTry>=10 && run.errors<=6->2
        else->1
    }
    fun score(run:SpaceRun):Int=
        (run.stage*100+run.firstTry*30+run.bestCombo*12-
            run.errors*22-run.hints*15).coerceAtLeast(0)
}
