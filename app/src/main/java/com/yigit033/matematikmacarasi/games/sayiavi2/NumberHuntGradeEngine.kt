package com.yigit033.matematikmacarasi.games.sayiavi2

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import kotlin.random.Random

/**
 * Sayı Avı 3.0: 8 school-grade policies, 4 themed worlds, 4 tactile mechanics.
 *
 * Questions are deterministic for a persisted run seed. The content is a selected
 * practice curriculum, NOT a claim of certified MEB outcome alignment.
 */
object NumberHuntGradeEngine {
    const val TASKS_PER_ROOM=4
    const val ROOM_COUNT=4
    const val TASK_COUNT=TASKS_PER_ROOM*ROOM_COUNT

    data class World(val key:String,val title:String,val subtitle:String,
        val grades:IntRange,val focus:String)
    val worlds=listOf(
        World("beach","Kayıp Sayı Adası","Kabuklar ve sayılar",1..2,"Sayma ve sayı ilişkileri"),
        World("temple","Gizemli Sayı Tapınağı","Antik geçitler",3..4,"İşlemler ve örüntüler"),
        World("crystals","Kristal Sayı Madenleri","Işıldayan sayı kristalleri",5..6,"Bölenler, kesirler ve ondalıklar"),
        World("universe","Sayıların Gizli Evreni","Sayı portalları",7..8,"Rasyonel, üslü ve köklü sayılar")
    )
    fun world(grade:Int):World {
        require(grade in 1..8)
        return worlds.first{grade in it.grades}
    }

    enum class Mechanic(val title:String, val icon:String) {
        SCAN("Sayı İzleri","✦"),BRIDGE("Kayıp Köprü","◆"),
        DIAL("Enerji Kilidi","◉"),PAIRS("Sayı Mührü","✧"),
        CIPHER("Şifre Analizi","⌘"),TRAIL("Yıldız Yolu","✦")
    }
    data class Token(val label:String,val value:Int)
    data class MatchPair(val id:Int,val left:String,val right:String)
    sealed class Task {
        abstract val prompt:String
        abstract val hint:String
        abstract val explanation:String
        abstract val mechanic:Mechanic
    }
    data class Scan(override val prompt:String,
        val options:List<Int>,val targets:Set<Int>,
        override val hint:String,override val explanation:String):Task(){
        override val mechanic=Mechanic.SCAN
    }
    data class Bridge(override val prompt:String,
        val tokens:List<Token>,override val hint:String,
        override val explanation:String):Task(){
        override val mechanic=Mechanic.BRIDGE
    }
    data class Dial(override val prompt:String,val target:Int,
        val initial:Int,val lower:Int,val upper:Int,val step:Int=1,
        val suffix:String="",override val hint:String,
        override val explanation:String):Task(){
        override val mechanic=Mechanic.DIAL
    }
    data class Pairs(override val prompt:String,
        val pairs:List<MatchPair>,override val hint:String,
        override val explanation:String):Task(){
        override val mechanic=Mechanic.PAIRS
    }
    data class Cipher(override val prompt:String,
        val choices:List<String>,val answer:String,
        override val hint:String,override val explanation:String):Task(){
        override val mechanic=Mechanic.CIPHER
    }
    data class Checkpoint(val prompt:String,val choices:List<String>,val answer:String)
    data class Trail(override val prompt:String,val checkpoints:List<Checkpoint>,
        override val hint:String,override val explanation:String):Task(){
        override val mechanic=Mechanic.TRAIL
    }

    fun generate(grade:Int,tier:LearningTier,seed:Long):List<Task>{
        require(grade in 1..8 && seed!=0L)
        val rng=Random(seed xor (grade.toLong() shl 19) xor tier.ordinal.toLong())
        return buildList {
            repeat(4){add(scan(grade,tier,it,rng))}
            repeat(4){add(bridge(grade,tier,it,rng))}
            repeat(4){add(dial(grade,tier,it,rng))}
            repeat(4){add(pairs(grade,tier,it,rng))}
        }.also{tasks->
            require(tasks.size==TASK_COUNT)
            tasks.forEach{
                when(it){
                    is Scan->require(it.options.size==12 &&
                        it.options.distinct().size==12 &&
                        it.targets.size==4 && it.targets.all(it.options::contains))
                    is Bridge->require(it.tokens.size==4 &&
                        it.tokens.map(Token::value).distinct().size==4)
                    is Dial->require(it.target in it.lower..it.upper &&
                        it.initial in it.lower..it.upper && it.step>0)
                    is Pairs->require(it.pairs.size==3 &&
                        it.pairs.map(MatchPair::right).distinct().size==3)
                    is Cipher->require(it.choices.size==4 && it.answer in it.choices)
                    is Trail->require(it.checkpoints.size==4 &&
                        it.checkpoints.all{point->point.answer in point.choices})
                }
            }
        }
    }

    private fun isPrime(n:Int):Boolean=
        n>=2 && (2..kotlin.math.sqrt(n.toDouble()).toInt()).none{n%it==0}

    private fun scan(grade:Int,tier:LearningTier,i:Int,rng:Random):Scan {
        val advanced=tier==LearningTier.USTALIK
        val targets:List<Int>
        val title:String
        val hint:String
        val candidates:List<Int>
        when(grade){
            1 -> {
                val start=3+i*4+rng.nextInt(1,if(advanced)12 else 5)
                targets=(start..start+3).toList()
                title="Yan yana gelen dört sayıyı keşfet: ${targets.first()} ile ${targets.last()} arası"
                hint="Aralıktaki tüm sayıları birer birer say."
                candidates=(1..65).filterNot{it in targets}
            }
            6 -> {
                val primes=(2..97).filter(::isPrime).shuffled(rng)
                targets=primes.take(4).sorted()
                title="Asal sayı kristallerini avla"
                hint="Asal sayının yalnızca iki pozitif böleni vardır: 1 ve kendisi."
                candidates=(4..105).filterNot(::isPrime)
            }
            7 -> {
                val step=listOf(2,3,4,5)[i]
                val first=step*(rng.nextInt(2,if(advanced)7 else 5))
                targets=(0..3).map{-(first+step*it)}
                title="Negatif tarafta ${step}'ün katlarını bul"
                hint="Sıfırın solundaki sayılar negatiftir; işarete dikkat et."
                candidates=(-65..8).filter{it !in targets && (it>=0 || (-it)%step!=0)}
            }
            8 -> {
                val squares=(2..13).map{it*it}
                targets=squares.shuffled(rng).take(4).sorted()
                title="Karekökü tam sayı olan dört taşı bul"
                hint="Bir doğal sayının kendisiyle çarpımından oluşan sayılara bak."
                candidates=(5..170).filter{n->
                    n !in targets && kotlin.math.sqrt(n.toDouble()).toInt().let{it*it!=n}
                }
            }
            else -> {
                val step=when(grade){
                    2->listOf(3,4,5,10)[i]
                    3->listOf(4,6,7,8)[i]
                    4->listOf(6,8,9,12)[i]
                    else->listOf(6,7,8,9)[i]
                }
                val start=step*rng.nextInt(1,if(advanced)10 else 6)
                targets=(0..3).map{start+it*step}
                title="${step}'er artan dört sayıyı bul"
                hint="Her doğru sayı bir öncekinden ${step} fazladır."
                candidates=(1..200).filter{it !in targets && it%step!=0}
            }
        }
        val options=(targets+candidates.shuffled(rng).take(8)).shuffled(rng)
        return Scan(title,options,targets.toSet(),hint,
            "Doğru izler: ${targets.sorted().joinToString(" · ")}")
    }

    private fun bridge(grade:Int,tier:LearningTier,i:Int,rng:Random):Bridge {
        val base=i+if(tier==LearningTier.TEMEL)2 else 4
        val values: List<Token> = when(grade){
            1->(0..3).map{Token("${base+it*2}",base+it*2)}
            2->(1..4).map{Token("${it*(i+3)}",it*(i+3))}
            3->(2..5).map{Token("${i+3} × ${it}",(i+3)*it)}
            4->(3..6).map{Token("${(i+3)*it} ÷ ${i+3}",it)}
            5->listOf(Token("1/10",10),Token("1/5",20),
                Token("1/4",25),Token("1/2",50),Token("3/4",75),
                Token("2/5",40),Token("3/5",60),Token("4/5",80)).shuffled(rng).take(4)
            6->listOf(4,6,9,11,13,16,18,22).shuffled(rng).take(4)
                .map{Token("${it/10},${it%10}",it)}
            7->listOf(Token("-3/2",-150),Token("-1",-100),
                Token("-1/2",-50),Token("1/4",25),Token("3/4",75),
                Token("-2",-200),Token("1/2",50),Token("-1/4",-25))
                .shuffled(rng).take(4)
            else->listOf(Token("√16",4),Token("√25",5),Token("√49",7),
                Token("2³",8),Token("3²",9),Token("√121",11),
                Token("√169",13),Token("2⁴",16)).shuffled(rng).take(4)
        }
        val title=when(grade){
            5->"Kesir taşlarını küçükten büyüğe sırala"
            6->"Ondalık kristalleri küçükten büyüğe sırala"
            7->"Rasyonel sayı köprüsünü küçükten büyüğe kur"
            8->"Üslü ve köklü taşları değere göre sırala"
            else->"Köprüyü küçük sayıdan büyüğe doğru kur"
        }
        return Bridge(title,values.shuffled(rng),
            "Soldan sağa artan sayıları seç. İşlemleri önce hesaplayabilirsin.",
            "Doğru sıra: ${values.sortedBy{it.value}.joinToString(" → "){it.label}}")
    }

    private fun dial(grade:Int,tier:LearningTier,i:Int,rng:Random):Dial {
        val bonus=if(tier==LearningTier.USTALIK)4 else 0
        val question:String
        val answer:Int
        val suffix:String
        val step:Int
        val lower:Int
        val upper:Int
        when(grade){
            1->{ val a=4+i+bonus/2;val b=3+i
                question="$a + $b = ?";answer=a+b;lower=0;upper=30;step=1;suffix=""
            }
            2->{val a=3+i;val b=2+i+bonus/2
                question="$a × $b = ?";answer=a*b;lower=0;upper=70;step=1;suffix=""
            }
            3->{val a=5+i;val b=3+i
                question="$a × $b - 2 = ?";answer=a*b-2;lower=0;upper=100;step=1;suffix=""
            }
            4->{val v=6+i;val d=3+i
                question="${v*d} ÷ $d + $i = ?";answer=v+i;lower=0;upper=35;step=1;suffix=""
            }
            5->{val v=5+i+bonus/2
                question="${v*6} sayısının yarısı kaçtır?";answer=v*3;lower=0;upper=60;step=1;suffix=""
            }
            6->{val percentage=listOf(25,50,75,100)[i]
                question="Bütünün ${percentage}/100'ü yüzde kaçtır?"
                answer=percentage;lower=0;upper=100;step=5;suffix="%"
            }
            7->{val a=5+i+bonus;val b=3+i
                question="(-$a) + (-$b) = ?";answer=-(a+b);lower=-45;upper=10;step=1;suffix=""
            }
            else->{val root=listOf(9,12,15,10)[i];val power=8
                question="√${root*root} + 2³ = ?"
                answer=root+power;lower=0;upper=45;step=1;suffix=""
            }
        }
        val shift=step*(if(rng.nextBoolean())3 else -3)
        val initial=(answer+shift).coerceIn(lower,upper)
        return Dial(question,answer,initial,lower,upper,step,suffix,
            "İşlemi sırayla çöz; düğmeleri kullanarak enerji değerini ayarla.",
            "Doğru enerji: $answer$suffix")
    }

    private fun pairs(grade:Int,tier:LearningTier,i:Int,rng:Random):Pairs {
        val extra=if(tier==LearningTier.USTALIK)2 else 0
        val content: List<Pair<String,String>> = when(grade){
            1->(0..2).map{j->val a=3+i+j;val b=2+j+extra
                "$a + $b" to "${a+b}"}
            2->(0..2).map{j->val a=3+i+j;val b=2+j+extra
                "$a × $b" to "${a*b}"}
            3->(0..2).map{j->val a=4+i+j;val b=3+j+extra
                "$a × $b" to "${a*b}"}
            4->(0..2).map{j->val result=4+i+j
                "${(result)*(j+3)} ÷ ${j+3}" to "$result"}
            5->(0..2).map{j->val a=12+(i+j)*4
                "$a sayısının yarısı" to "${a/2}"}
            6->listOf("0,5" to "1/2","0,25" to "1/4","0,75" to "3/4",
                "0,2" to "1/5","0,4" to "2/5","0,6" to "3/5",
                "0,8" to "4/5","0,125" to "1/8").shuffled(rng).take(3)
            7->listOf("-1/2" to "-0,5","-3/2" to "-1,5",
                "1/4" to "0,25","-1/4" to "-0,25","3/4" to "0,75",
                "-2/5" to "-0,4","5/4" to "1,25","-7/4" to "-1,75")
                .shuffled(rng).take(3)
            else->listOf("√81" to "9","2⁵" to "32","√144" to "12",
                "3³" to "27","√169" to "13","2⁴" to "16",
                "√196" to "14","5²" to "25").shuffled(rng).take(3)
        }
        // Most grade 1..5 generators create increasing distinct answers.
        return Pairs(
            "Sayı mühürlerini doğru eşleriyle bağla",
            content.mapIndexed{index,(a,b)->MatchPair(index,a,b)}.shuffled(rng),
            "Önce soldaki ifadeyi seç, sonra sağdaki eşdeğer değere dokun.",
            "Mühürler eşleşti: ${content.joinToString(" · "){(a,b)->"$a = $b"}}"
        )
    }
}
