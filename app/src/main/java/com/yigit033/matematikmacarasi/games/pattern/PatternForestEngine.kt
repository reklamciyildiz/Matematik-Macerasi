package com.yigit033.matematikmacarasi.games.pattern

import kotlin.math.abs
import kotlin.random.Random

enum class PatternWorld(val title:String,val symbol:String,val skill:String) {
    COLORS("Renk Patikası","🌈","pattern_color"),
    SHAPES("Şekil Bahçesi","🌼","pattern_shape"),
    NUMBERS("Sayı Yolu","🔢","pattern_number"),
    MOTION("Hareketli Totemler","🧭","pattern_spatial"),
    RULES("Kural Kristali","💠","pattern_reasoning")
}
enum class PatternMechanic(val title:String) {
    COMPLETE("Eksik Taşı Tamamla"), DRAG("Taşı Yerine Sürükle"),
    ORDER("Karışık Taşları Sırala"), RULE("Gizli Kuralı Keşfet"),
    SPOT("Hatalı Taşı Yakala"), CREATE("Örüntüyü Sen İnşa Et")
}
enum class PatternKind { COLOR, SHAPE, NUMBER, DIRECTION, SIZE, DUAL }
data class PatternTile(val kind:PatternKind,val value:Int,val accent:Int=0) {
    fun label():String=when(kind) {
        PatternKind.COLOR->listOf("Kırmızı","Mavi","Sarı","Yeşil","Mor","Turuncu")[value%6]
        PatternKind.SHAPE->listOf("Daire","Üçgen","Kare","Yıldız","Altıgen","Kalp")[value%6]
        PatternKind.NUMBER->value.toString()
        PatternKind.DIRECTION->listOf("Yukarı","Sağ","Aşağı","Sol")[value%4]
        PatternKind.SIZE->listOf("Küçük","Orta","Büyük")[value%3]
        PatternKind.DUAL->PatternTile(PatternKind.COLOR,value).label()+" "+
            PatternTile(PatternKind.SHAPE,accent).label()
    }
}
data class PatternTask(
    val world:PatternWorld,val mechanic:PatternMechanic,val grade:Int,
    val difficulty:Int,val instruction:String,val explanation:String,
    val shown:List<PatternTile>,val answer:List<PatternTile> = emptyList(),
    val options:List<PatternTile> = emptyList(),val missingIndex:Int = -1,
    val corruptIndex:Int = -1,val ruleChoices:List<String> = emptyList(),
    val correctRuleIndex:Int = -1
)
object PatternForestEngine {
    const val STOPS=30
    const val TASKS_PER_WORLD=6
    val difficultyLabels=listOf("Temel","Orta","Zor")
    fun world(index:Int):PatternWorld {
        require(index in 0 until STOPS)
        return PatternWorld.entries[index/TASKS_PER_WORLD]
    }
    fun mechanic(index:Int):PatternMechanic {
        require(index in 0 until STOPS)
        return PatternMechanic.entries[index%TASKS_PER_WORLD]
    }
    private data class Recipe(val kind:PatternKind,val cycle:List<PatternTile>,
        val initial:Int=0,val step:Int=0,val alt:Int=0,val alternating:Boolean=false) {
        fun at(index:Int):PatternTile {
            if(kind!=PatternKind.NUMBER)return cycle[index%cycle.size]
            val diff=if(alternating)(index/2)*(step+alt)+(index%2)*step else index*step
            return PatternTile(PatternKind.NUMBER,initial+diff)
        }
        fun rule():String=if(kind==PatternKind.NUMBER) {
            if(alternating)"Bir "+signed(step)+", bir "+signed(alt)
            else "Her adımda "+signed(step)
        }else when(kind) {
            PatternKind.DIRECTION->if(cycle.size==4)"Yönler her adımda sağa döner"
                else "2 yön dönüşümlü tekrar eder"
            PatternKind.SIZE->cycle.size.toString()+" boyut dönüşümlü tekrar eder"
            PatternKind.DUAL->cycle.size.toString()+" renk–şekil ikilisi tekrar eder"
            PatternKind.COLOR->cycle.size.toString()+" renk sırayla tekrar eder"
            PatternKind.SHAPE->cycle.size.toString()+" şekil sırayla tekrar eder"
            else->""
        }
    }
    private fun signed(n:Int)=if(n>=0)"+$n" else n.toString()
    private fun recipe(seed:Long,grade:Int,diff:Int,index:Int):Recipe {
        val random=Random(seed xor ((index+1L)*1442695040888963407L))
        val w=world(index)
        val kind=when(w) {
            PatternWorld.COLORS->PatternKind.COLOR
            PatternWorld.SHAPES->PatternKind.SHAPE
            PatternWorld.NUMBERS->PatternKind.NUMBER
            PatternWorld.MOTION->if(index%2==0)PatternKind.DIRECTION else PatternKind.SIZE
            PatternWorld.RULES->when(index%3) {
                0->PatternKind.NUMBER
                1->PatternKind.DUAL
                else->PatternKind.DIRECTION
            }
        }
        if(kind==PatternKind.NUMBER) {
            val deltas=when {
                grade==1->listOf(1,2,5,10)
                grade==2->listOf(1,2,3,5,10)
                grade==3->listOf(2,3,5,10)
                grade==4->listOf(2,3,4,5,10)
                grade==5->listOf(2,3,4,5,6,8)
                else->listOf(3,4,5,6,7,9,12)
            }
            var step=deltas.random(random)
            val alternate=grade>=6 && diff>=1 && index%4==0
            val alt=if(alternate)deltas.filter{it!=step}.random(random) else step
            val decrease=grade>=4 && diff>=1 && random.nextInt(3)==0
            if(decrease)step=-step
            val other=if(decrease)-alt else alt
            val farthest=when {grade>=7->16;grade==6->12;grade==5->10;else->9}
            val base=if(decrease)(farthest+1)*maxOf(abs(step),abs(other))+
                random.nextInt(1,16)
                else if(grade<=2)random.nextInt(1,6)
                else random.nextInt(1,if(grade<=4)55 else 90)
            return Recipe(kind,emptyList(),base,step,other,alternate)
        }
        val length=when {
            kind==PatternKind.DIRECTION->if(diff==0)2 else 4
            kind==PatternKind.SIZE->if(diff==0)2 else 3
            grade<=1->2
            grade<=2->if(diff==2)3 else 2
            grade<=4->if(diff==0)2 else 3
            else->if(diff==2)4 else 3
        }
        val values=when(kind) {
            PatternKind.DIRECTION->if(length==4)listOf(0,1,2,3) else listOf(0,2)
            PatternKind.SIZE->(0 until length).toList()
            else->(0..5).shuffled(random).take(length)
        }
        val cycle=values.mapIndexed { i,v->
            if(kind==PatternKind.DUAL)PatternTile(kind,v,(i+random.nextInt(0,3))%6)
            else PatternTile(kind,v)
        }
        return Recipe(kind,cycle)
    }
    fun task(seed:Long,grade:Int,difficulty:Int,index:Int):PatternTask {
        require(seed>0 && grade in 1..8 && difficulty in 0..2 && index in 0 until STOPS)
        val rng=Random(seed+index.toLong()*65537L)
        val recipe=recipe(seed,grade,difficulty,index)
        val mode=mechanic(index)
        val w=world(index)
        val prefix=if(recipe.kind!=PatternKind.NUMBER && recipe.cycle.size==4)8
            else if(difficulty==2)7 else 6
        val shown=(0 until prefix).map(recipe::at)
        val intro=when(w) {
            PatternWorld.COLORS->"Renk taşlarının ritmini keşfet."
            PatternWorld.SHAPES->"Şekillerin tekrar eden sırasını bul."
            PatternWorld.NUMBERS->"Sayıların nasıl değiştiğini keşfet."
            PatternWorld.MOTION->"Totemlerin yön ve boyut hareketini incele."
            PatternWorld.RULES->"Kristalin gizli matematik kuralını aç."
        }
        var explanation="Kural: "+recipe.rule()+". "+
            if(recipe.kind==PatternKind.NUMBER)"Ardışık terimler arasındaki farkı kontrol et."
            else "Düzen aynı sırayla yeniden başlar."
        val answer=when(mode) {
            PatternMechanic.COMPLETE->listOf(recipe.at(prefix))
            PatternMechanic.DRAG->listOf(recipe.at(prefix-2))
            PatternMechanic.ORDER,PatternMechanic.CREATE->
                (prefix until prefix+3).map(recipe::at)
            else->emptyList()
        }
        var visual=shown
        var missing=-1;var corrupt=-1
        var choices=emptyList<PatternTile>()
        var ruleChoices=emptyList<String>();var rightRule=-1
        val instruction=when(mode) {
            PatternMechanic.COMPLETE->{
                choices=pool(recipe,answer,rng,4)
                "$intro Sonraki doğru taşı seç."
            }
            PatternMechanic.DRAG->{
                missing=prefix-2
                choices=pool(recipe,answer,rng,4)
                "$intro Eksik taşı boş yuvaya sürükle veya dokunarak yerleştir."
            }
            PatternMechanic.ORDER->{
                choices=answer.shuffled(rng)
                if(choices==answer && answer.distinct().size>1)
                    choices=answer.drop(1)+answer.first()
                "$intro Dağılmış üç taşı doğru sıraya diz."
            }
            PatternMechanic.RULE->{
                if(recipe.kind==PatternKind.NUMBER && grade>=5 &&
                    !recipe.alternating) {
                    // Far-term reasoning rather than yet another basic next-number choice.
                    val nth=when {grade<=5->10;grade==6->12;else->16}
                    val term=recipe.at(nth-1).value
                    val diff=kotlin.math.abs(recipe.step)
                    val decoys=listOf(term+diff,term-diff,term+2*diff,
                        term+diff+1,term-2*diff).filter{it>=0 && it!=term}
                        .distinct().take(3)
                    ruleChoices=(decoys+term).map{it.toString()}.shuffled(rng)
                    rightRule=ruleChoices.indexOf(term.toString())
                    explanation="İlk terim "+recipe.initial+
                        ", her adımda "+recipe.step+
                        " değişir. "+nth+". terim = "+
                        recipe.initial+" + ("+(nth-1)+" × "+recipe.step+
                        ") = "+term+"."
                    "$intro "+nth+". sıradaki sayıyı kuralı kullanarak bul."
                }else{
                    ruleChoices=(listOf(recipe.rule())+wrongRules(recipe))
                        .distinct().take(4).shuffled(rng)
                    rightRule=ruleChoices.indexOf(recipe.rule())
                    "$intro Bu örüntü hangi kurala uyuyor?"
                }
            }
            PatternMechanic.SPOT->{
                corrupt=rng.nextInt(2,prefix-1)
                val original=recipe.at(corrupt)
                val wrong=pool(recipe,listOf(original),rng,4).first{it!=original}
                visual=shown.toMutableList().also{it[corrupt]=wrong}
                "$intro Kuralı bozan taşı yakala."
            }
            PatternMechanic.CREATE->{
                choices=pool(recipe,answer,rng,5)
                "$intro Sonraki üç taşı kendin seçerek örüntüyü oluştur."
            }
        }
        return PatternTask(w,mode,grade,difficulty,instruction,explanation,
            visual,answer,choices,missing,corrupt,ruleChoices,rightRule)
    }
    private fun pool(recipe:Recipe,answers:List<PatternTile>,rng:Random,count:Int):List<PatternTile> {
        val all=answers.distinct().toMutableList()
        val variants:List<PatternTile> = when(recipe.kind) {
            PatternKind.NUMBER->{
                val n=answers.first().value;val d=abs(recipe.step).coerceAtLeast(1)
                listOf(n+d,n-d,n+2*d,n+1,n-1,n+3*d,n+5,n+7)
                    .filter{it>=0}.distinct().map{PatternTile(recipe.kind,it)}
            }
            PatternKind.DUAL->(0..5).flatMap{v->(0..5).map{a->PatternTile(recipe.kind,v,a)}}
            PatternKind.DIRECTION->(0..3).map{PatternTile(recipe.kind,it)}
            PatternKind.SIZE->(0..2).map{PatternTile(recipe.kind,it)}
            else->(0..5).map{PatternTile(recipe.kind,it)}
        }.shuffled(rng)
        for(item in variants)if(item !in all && all.size<count)all.add(item)
        return all.shuffled(rng)
    }
    private fun wrongRules(r:Recipe):List<String> {
        if(r.kind==PatternKind.NUMBER) {
            val step=r.step
            return if(r.alternating)listOf(
                "Her adımda +"+abs(step),"Her adımda +"+abs(r.alt),
                "Bir +"+(abs(step)+1)+", bir +"+abs(r.alt),
                "Her adımda -"+abs(step))
            else listOf(
                "Her adımda "+signed(step+if(step>=0)1 else -1),
                "Her adımda "+signed(step+if(step>=0)2 else -2),
                "Her adımda "+signed(-step),
                "Bir +"+abs(step)+", bir +"+(abs(step)+1))
        }
        return when(r.kind) {
            PatternKind.DIRECTION->listOf("Yönler her adımda sola döner",
                "3 yön sırayla tekrar eder","Aynı yön hiç değişmez","Yönler rastgele sıralanır")
            PatternKind.SIZE->listOf("1 boyut hep aynı kalır","4 boyut dönüşümlü tekrar eder",
                "Boyutlar hiç tekrarlanmaz","Sadece renkler değişir")
            PatternKind.DUAL->listOf("Sadece renk tekrarlanır",
                "Sadece şekil tekrarlanır","5 renk–şekil ikilisi tekrar eder",
                "Sıra hiç tekrarlanmaz")
            PatternKind.COLOR->listOf("1 renk hep aynıdır","5 renk sırayla tekrar eder",
                "Renkler rastgele değişir","6 renk sırayla tekrar eder")
            PatternKind.SHAPE->listOf("1 şekil hep aynıdır","5 şekil sırayla tekrar eder",
                "Şekiller rastgele değişir","6 şekil sırayla tekrar eder")
            else->emptyList()
        }
    }
    fun correct(t:PatternTask,choice:Int,selected:List<Int>):Boolean=when(t.mechanic) {
        PatternMechanic.COMPLETE,PatternMechanic.DRAG->
            choice in t.options.indices && t.options[choice]==t.answer.single()
        PatternMechanic.RULE->choice==t.correctRuleIndex
        PatternMechanic.SPOT->choice==t.corruptIndex
        PatternMechanic.ORDER,PatternMechanic.CREATE->
            selected.size==t.answer.size &&
                selected.all{it in t.options.indices} &&
                selected.map{t.options[it]}==t.answer
    }
    fun validSelection(t:PatternTask,choice:Int,selected:List<Int>):Boolean {
        if(selected.size>t.answer.size || selected.any{it !in t.options.indices})return false
        return when(t.mechanic) {
            PatternMechanic.COMPLETE,PatternMechanic.DRAG->
                selected.isEmpty() && choice in -1..t.options.lastIndex
            PatternMechanic.RULE->selected.isEmpty() && choice in -1..t.ruleChoices.lastIndex
            PatternMechanic.SPOT->selected.isEmpty() && choice in -1..t.shown.lastIndex
            PatternMechanic.ORDER->choice==-1 && selected.distinct().size==selected.size
            PatternMechanic.CREATE->choice==-1
        }
    }
}
