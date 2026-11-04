package com.yigit033.matematikmacarasi.games.sayiavi2

import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import com.yigit033.matematikmacarasi.games.sayiavi2.NumberHuntGradeEngine.Task
import com.yigit033.matematikmacarasi.games.sayiavi2.NumberHuntGradeEngine.Scan
import com.yigit033.matematikmacarasi.games.sayiavi2.NumberHuntGradeEngine.Bridge
import com.yigit033.matematikmacarasi.games.sayiavi2.NumberHuntGradeEngine.Dial
import com.yigit033.matematikmacarasi.games.sayiavi2.NumberHuntGradeEngine.Pairs
import com.yigit033.matematikmacarasi.games.sayiavi2.NumberHuntGradeEngine.Cipher
import com.yigit033.matematikmacarasi.games.sayiavi2.NumberHuntGradeEngine.Trail
import com.yigit033.matematikmacarasi.games.sayiavi2.NumberHuntGradeEngine.Token
import com.yigit033.matematikmacarasi.games.sayiavi2.NumberHuntGradeEngine.MatchPair
import com.yigit033.matematikmacarasi.games.sayiavi2.NumberHuntGradeEngine.Checkpoint
import kotlin.math.sqrt
import kotlin.random.Random

/**
 * Sayı Avı 3.1 content version 2. Four chapters, six different mechanics per
 * chapter, grade-specific math relationships. Pure and seed-deterministic.
 * Legacy sessions keep generating their original 16 puzzles unchanged.
 */
object NumberHuntRichContent {
    const val VERSION=2
    const val CHAPTERS=4
    const val TASKS_PER_CHAPTER=6
    const val TASK_COUNT=CHAPTERS*TASKS_PER_CHAPTER
    val chapterTitles=listOf("İlk İzler","Gizli Bağlantılar","Kayıp Anahtarlar","Büyük Hazine")
    /** A short mathematical goal paired with each adventure chapter. */
    fun chapterMission(grade:Int,chapter:Int):String{
        require(grade in 1..8 && chapter in 0..3)
        return when(grade){
            1->listOf("Tek ve çift sayı ipuçlarını takip et.",
                "Sayıları karşılaştırarak köprüleri aç.",
                "Toplama ve çıkarma ile anahtarları keşfet.",
                "Sayıları birleştir, son sandığa ulaş.")[chapter]
            2->listOf("Ritmik saymanın gizli izlerini bul.",
                "Çarpma ve gruplama bağlantılarını keşfet.",
                "Onlukları ve işlemleri çöz.",
                "Sayı mühürlerini doğru eşleştir.")[chapter]
            3,4->listOf("Çarpım ve bölme sayılarını araştır.",
                "Örüntü kurallarını keşfet.",
                "Çok adımlı işlemlerle anahtarı aç.",
                "İşlem zincirlerini son kez çöz.")[chapter]
            5,6->listOf("Bölenler ve asal sayılarla iz sür.",
                "Kesir ve ondalık köprülerini kur.",
                "Oran ve yüzde değerlerini çöz.",
                "Kristal şifrelerini farklı yollarla doğrula.")[chapter]
            7->listOf("Negatif tam sayı izlerini yakala.",
                "Rasyonel sayıları ve işaretleri karşılaştır.",
                "Mutlak değer ve işlem kurallarını kullan.",
                "Dört portaldan geçerek sayı hazinesini aç.")[chapter]
            else->listOf("Tam kare ve küp sayıların izini sür.",
                "Üslü ve köklü ifadelerin değerlerini bul.",
                "Bilimsel gösterimin sayı kodlarını çöz.",
                "Farklı gösterimleri eşleştirerek finali aç.")[chapter]
        }
    }

    fun generate(grade:Int,tier:LearningTier,seed:Long):List<Task>{
        require(grade in 1..8 && seed!=0L)
        val r=Random(seed xor -7046029254386353131L xor
            (grade.toLong() shl 40) xor tier.ordinal.toLong())
        val result=buildList{
            repeat(4){chapter->
                addAll(listOf(
                    scan(grade,chapter,tier,r),
                    bridge(grade,chapter,tier,r),
                    dial(grade,chapter,tier,r),
                    pairs(grade,chapter,tier,r),
                    cipher(grade,chapter,tier,r),
                    trail(grade,chapter,tier,r)
                ).shuffled(r))
            }
        }
        require(result.size==TASK_COUNT &&
            result.map{it.prompt}.distinct().size==TASK_COUNT)
        result.forEach{task->
            when(task){
                is Scan->require(task.options.size==12 &&
                    task.options.distinct().size==12 && task.targets.size==4 &&
                    task.targets.all(task.options::contains))
                is Bridge->require(task.tokens.size==4 &&
                    task.tokens.map{it.value}.distinct().size==4)
                is Dial->require(task.target in task.lower..task.upper &&
                    task.initial in task.lower..task.upper && task.step>=1)
                is Pairs->require(task.pairs.size==3 &&
                    task.pairs.map{it.right}.distinct().size==3 &&
                    task.pairs.map{it.left}.distinct().size==3)
                is Cipher->require(task.choices.size==4 &&
                    task.choices.distinct().size==4 && task.answer in task.choices)
                is Trail->require(task.checkpoints.size==4 &&
                    task.checkpoints.all{it.choices.size==4 &&
                        it.choices.distinct().size==4 && it.answer in it.choices})
            }
        }
        return result
    }

    private data class Rule(val title:String,val hint:String,
        val domain:IntRange,val accept:(Int)->Boolean)
    private fun prime(n:Int):Boolean=n>=2 &&
        (2..sqrt(n.toDouble()).toInt()).none{n%it==0}
    private fun square(n:Int):Boolean=n>=0 &&
        sqrt(n.toDouble()).toInt().let{it*it==n}
    private fun cube(n:Int):Boolean=(1..12).any{it*it*it==n}
    private fun power2(n:Int):Boolean=n>=2 && (n and (n-1))==0

    private fun scan(g:Int,c:Int,t:LearningTier,r:Random):Scan{
        val extra=if(t==LearningTier.USTALIK)24 else 0
        val rule=when(g){
            1->when(c){
                0->Rule("Tek sayıları yakala","Birer say ve tekleri bul.",1..40){it%2!=0}
                1->Rule("Çift sayı kabuklarını keşfet","İkişer sayabilirsin.",2..45){it%2==0}
                2->Rule("İki basamaklı sayıları avla","10 veya daha büyük olanları seç.",1..30){it in 10..19}
                else->Rule("10'dan büyük tek sayı izleri","Hem 10'dan büyük hem tek olmalı.",1..50){it>10 && it%2!=0}
            }
            2->when(c){
                0->Rule("Üçer artan sayıları avla","3'ün katlarına bak.",1..130){it%3==0}
                1->Rule("Beşer sayılan taşları bul","0 veya 5 ile biten sayılar.",1..130){it%5==0}
                2->Rule("Onluk hazineleri keşfet","10, 20, 30 ...",1..130){it%10==0}
                else->Rule("Dörder artan izleri bul","4, 8, 12 ...",1..130){it%4==0}
            }
            3->when(c){
                0->Rule("6'nın katlarını ara","Altışar sayabilirsin.",1..180){it%6==0}
                1->Rule("8'in katlarından dördünü avla","Çarpım tablosunu kullan.",1..180){it%8==0}
                2->Rule("3'e tam bölünebilen taşlar","Rakamları toplamını dene.",10..200){it%3==0}
                else->Rule("9'un katlarını bul","Dokuzar artan sayıları ara.",1..200){it%9==0}
            }
            4->when(c){
                0->Rule("12'nin katlarını avla","12 ile tam bölünenler.",1..270){it%12==0}
                1->Rule("4'e kalansız bölünenleri bul","Son iki basamağa bak.",20..220){it%4==0}
                2->Rule("2'ye ve 5'e bölünenleri keşfet","10'un katlarıdır.",1..250){it%10==0}
                else->Rule("Bir düzineyi aşan çift sayılar","12'den büyük ve çift.",1..100){it>12 && it%2==0}
            }
            5->when(c){
                0->Rule("Asal sayı taşlarını avla","Sadece iki pozitif böleni vardır.",2..(110+extra)){prime(it)}
                1->Rule("3'e ve 4'e bölünenler","12'nin katlarını seç.",1..260){it%12==0}
                2->Rule("Tam kare taşlarını keşfet","Bir sayının kendisiyle çarpımı.",2..(200+extra)){square(it)}
                else->Rule("5'in katı ama 10'un değil","Son basamağı 5 olanlar.",1..240){it%10==5}
            }
            6->when(c){
                0->Rule("Asal kristallerin izini sür","1 asal değildir.",2..(120+extra)){prime(it)}
                1->Rule("2 ve 3'e bölünenleri bul","6'nın katlarını seç.",1..200){it%6==0}
                2->Rule("9'a tam bölünenleri ayırt et","Rakamlar toplamına bak.",20..260){it%9==0}
                else->Rule("Karekökü tam olan sayıları avla","Tam kareleri bul.",4..(225+extra)){square(it)}
            }
            7->when(c){
                0->Rule("Negatif çift tam sayıları avla","Negatif ve 2'nin katı olmalı.",-90..30){it<0 && it%2==0}
                1->Rule("Mutlak değeri 10'dan büyük negatifler","-10'un solunda olanlar.",-45..20){it< -10}
                2->Rule("Negatif 3 katlarını avla","-3, -6, -9 ...",-95..20){it<0 && it%3==0}
                else->Rule("Negatif tek sayı izleri","İşarete ve tekliğe bak.",-80..30){it<0 && it%2!=0}
            }
            else->when(c){
                0->Rule("Karekökü tam sayı olan taşlar","Tam kare sayıları bul.",2..(200+extra)){square(it)}
                1->Rule("Küp sayı kristalleri","8, 27, 64, 125 ...",2..(290+extra)){cube(it)}
                2->Rule("İkinin pozitif kuvvetlerini yakala","2, 4, 8, 16 ...",2..(300+extra)){power2(it)}
                else->Rule("Karekökü tek olan tam kareler","Karekök sonucunun tekliğine bak.",2..(300+extra)){
                    square(it) && sqrt(it.toDouble()).toInt()%2!=0
                }
            }
        }
        val yes=rule.domain.filter(rule.accept)
        val no=rule.domain.filterNot(rule.accept)
        require(yes.size>=4 && no.size>=8)
        val correct=yes.shuffled(r).take(4)
        return Scan("Sayı İzleri · "+rule.title,
            (correct+no.shuffled(r).take(8)).shuffled(r),correct.toSet(),
            rule.hint,"Doğru taşlar: "+correct.sorted().joinToString(" · "))
    }

    private fun bridge(g:Int,c:Int,t:LearningTier,r:Random):Bridge{
        val bump=if(t==LearningTier.USTALIK)7 else 2
        val pool: List<Token> = when(g){
            1->(2..25).map{Token(it.toString(),it)}
            2->(2..17).map{Token((it*5).toString(),it*5)}
            3->(2..18).map{Token((c+3).toString()+" × "+it,(c+3)*it)}
            4->(2..18).map{Token(((c+4)*it).toString()+" ÷ "+(c+4),it)}
            5->listOf(Token("1/8",125),Token("1/5",200),
                Token("1/4",250),Token("3/10",300),Token("2/5",400),
                Token("1/2",500),Token("3/5",600),Token("3/4",750),
                Token("4/5",800),Token("7/8",875),Token("9/10",900))
            6->listOf(5,8,12,15,22,24,27,31,36,41,45,49,52)
                .map{Token((it/10).toString()+","+(it%10),it)}
            7->when(c){
                0->(-18..14).map{Token(it.toString(),it)}
                1->listOf(Token("-7/4",-175),Token("-3/2",-150),
                    Token("-5/4",-125),Token("-1",-100),Token("-3/4",-75),
                    Token("-1/2",-50),Token("1/4",25),Token("1/2",50),
                    Token("3/4",75),Token("5/4",125),Token("3/2",150))
                2->(2..17).map{Token("(-"+it+") + "+(c+bump),c+bump-it)}
                else->listOf(Token("-2,5",-250),Token("-1,75",-175),
                    Token("-1,5",-150),Token("-0,75",-75),Token("-0,25",-25),
                    Token("0",0),Token("0,5",50),Token("1,25",125),
                    Token("1,75",175),Token("2,5",250))
            }
            else->when(c){
                0->(3..19).map{Token("√"+(it*it),it)}
                1->(2..9).map{Token("2^"+it,1 shl it)}
                2->(2..16).map{Token(it.toString()+" × 10²",it*100)}
                else->listOf(Token("√9",3),Token("√25",5),Token("2³",8),
                    Token("3²",9),Token("√121",11),Token("2⁴",16),
                    Token("√361",19),Token("5²",25),Token("√900",30))
            }
        }
        val selected=pool.distinctBy{it.value}.shuffled(r).take(4)
        require(selected.size==4)
        return Bridge("Kayıp Köprü · Değerleri küçükten büyüğe sırala · "+
                chapterTitles[c],selected.shuffled(r),
            "Önce ifadenin değerini hesapla; sayısal sırayla ilerle.",
            "Doğru sıra: "+selected.sortedBy{it.value}.joinToString(" → "){it.label})
    }

    private fun dial(g:Int,c:Int,t:LearningTier,r:Random):Dial{
        val bonus=when(t){LearningTier.TEMEL->0;LearningTier.GELISEN->2;else->5}
        val a=3+r.nextInt(2,8)+bonus
        val b=2+r.nextInt(2,6)
        val q:String
        val result:Int
        when(g){
            1->when(c){
                0->{q="$a + $b";result=a+b}
                1->{q=(a+b).toString()+" - "+a;result=b}
                2->{q="İki sandıkta "+a+" ve "+b+" taş. Toplam?";result=a+b}
                else->{q=(a+2*b).toString()+" - "+a;result=2*b}
            }
            2->when(c){
                0->{q="$a × $b";result=a*b}
                1->{q=(a*b).toString()+" ÷ "+b;result=a}
                2->{q=(a+b+12).toString()+" - "+a+" - "+b;result=12}
                else->{q=b.toString()+" tane "+a+"'er taş kaç eder?";result=a*b}
            }
            3->when(c){
                0->{q="$a × $b + 3";result=a*b+3}
                1->{q=(a*b).toString()+" ÷ "+a+" + 5";result=b+5}
                2->{q="Her rafta "+b+" taş, "+a+" raf. 2 taş daha eklendi. Toplam?";result=a*b+2}
                else->{q=a.toString()+" × ("+b+" + 1)";result=a*(b+1)}
            }
            4->when(c){
                0->{q="("+(a*b)+" ÷ "+b+") × 3";result=a*3}
                1->{q=(a*b+12).toString()+" - "+(a*b);result=12}
                2->{q=a.toString()+" × "+b+" + "+b;result=b*(a+1)}
                else->{q=(a*b).toString()+" taş "+a+" kutuya paylaştırılırsa?";result=b}
            }
            5->when(c){
                0->{q=(a*12).toString()+" sayısının 1/4'ü";result=a*3}
                1->{q=(a*b).toString()+" sayısının iki katı";result=a*b*2}
                2->{q="("+a+" + "+b+") × 3";result=(a+b)*3}
                else->{q=(a*10).toString()+" sayısının %10'u";result=a}
            }
            6->when(c){
                0->{q=(a*20).toString()+" sayısının %25'i";result=a*5}
                1->{q=(a*10).toString()+" sayısının %40'ı";result=a*4}
                2->{q="5 × ("+a+" + "+b+") - "+(a*5);result=b*5}
                else->{q=(a*10).toString()+" sayısının %20'si";result=a*2}
            }
            7->when(c){
                0->{q="(-$a) + (-$b)";result=-(a+b)}
                1->{q="(-$a) - (-$b)";result=b-a}
                2->{q="(-$a) × $b";result=-(a*b)}
                else->{q=(a*b).toString()+" ÷ (-"+b+")";result=-a}
            }
            else->when(c){
                0->{q="√"+(a*a)+" + 2³";result=a+8}
                1->{q="3³ - √"+(b*b);result=27-b}
                2->{q="√"+((a+b)*(a+b))+" - 2²";result=a+b-4}
                else->{q="("+a+" × 10³) ÷ 10²";result=a*10}
            }
        }
        val low=(result-14).coerceAtLeast(if(g==7)-120 else 0)
        val high=result+14
        return Dial("Enerji Kilidi · "+chapterTitles[c]+": "+q+" = ?",
            result,(result+if(r.nextBoolean())3 else -3).coerceIn(low,high),
            low,high,1,"",
            "Önce işlem önceliğini veya sayı ilişkisini çöz; kadranı kullan.",
            "Doğru değer: "+result)
    }

    private fun pairs(g:Int,c:Int,t:LearningTier,r:Random):Pairs{
        val bonus=if(t==LearningTier.USTALIK)4 else 1
        val pool:List<Pair<String,String>> = when(g){
            1->(1..14).map{j->val a=j+c+bonus; (a.toString()+" + 2") to (a+2).toString()}
            2->(2..15).map{j->val a=j+c; (a.toString()+" × 3") to (a*3).toString()}
            3->(2..15).map{j->val a=j+c; (a.toString()+" × 4") to (a*4).toString()}
            4->(2..15).map{j->val a=j+c; ((a*6).toString()+" ÷ 6") to a.toString()}
            5->listOf("1/2" to "0,5","1/4" to "0,25","3/4" to "0,75",
                "1/5" to "0,2","2/5" to "0,4","3/5" to "0,6",
                "4/5" to "0,8","1/8" to "0,125","7/10" to "0,7","9/10" to "0,9")
            6->listOf("1/2" to "%50","1/4" to "%25","3/4" to "%75",
                "1/5" to "%20","2/5" to "%40","3/5" to "%60",
                "4/5" to "%80","1/10" to "%10","9/10" to "%90","7/10" to "%70")
            7->when(c){
                0->(2..15).map{j->("(-"+j+") + (-2)") to (-j-2).toString()}
                1->listOf("-1/2" to "-0,5","-3/2" to "-1,5","1/4" to "0,25",
                    "-1/4" to "-0,25","3/4" to "0,75","-2/5" to "-0,4",
                    "5/4" to "1,25","-7/4" to "-1,75")
                2->(2..15).map{j->("|-"+j+"|") to j.toString()}
                else->(2..15).map{j->("(-"+j+") × 2") to (-j*2).toString()}
            }
            else->when(c){
                0->(3..17).map{j->("√"+(j*j)) to j.toString()}
                1->(2..12).map{j->(j.toString()+"²") to (j*j).toString()}
                2->(2..12).map{j->(j.toString()+" × 10²") to (j*100).toString()}
                else->listOf("2³" to "8","3²" to "9","2⁴" to "16",
                    "3³" to "27","2⁵" to "32","4³" to "64",
                    "5²" to "25","√100" to "10","√196" to "14")
            }
        }
        val selected=pool.distinctBy{it.second}.shuffled(r).take(3)
        require(selected.size==3)
        return Pairs("Sayı Mührü · Eş değerleri bağla · "+chapterTitles[c],
            selected.mapIndexed{idx,p->MatchPair(idx,p.first,p.second)}.shuffled(r),
            "Soldaki matematiksel ifadeyi sağdaki eş değerle buluştur.",
            selected.joinToString(" · "){it.first+" = "+it.second})
    }

    private fun choiceNumbers(answer:Int,r:Random):List<String> =
        listOf(answer,answer+2,answer-3,answer+5).map{it.toString()}.shuffled(r)

    private fun cipher(g:Int,c:Int,t:LearningTier,r:Random):Cipher{
        val k=r.nextInt(4,11)+if(t==LearningTier.USTALIK)5 else 0
        val question:String
        val answer:String
        val choices:List<String>
        val hint:String
        when(g){
            1->{val n=k+c*2
                question=n.toString()+"'den sonra hangi sayı gelir?"
                answer=(n+1).toString();choices=choiceNumbers(n+1,r)
                hint="Bir sonraki sayı bir fazladır."
            }
            2->{val n=k+c
                question=n.toString()+" sayısının 10 fazlası kaçtır?"
                answer=(n+10).toString();choices=choiceNumbers(n+10,r)
                hint="Onlar basamağını düşün."
            }
            3->{val n=k+c
                question=(n*4).toString()+" taş 4 kişiye eşit paylaştırılırsa kaç düşer?"
                answer=n.toString();choices=choiceNumbers(n,r)
                hint="4'e bölme işlemini kullan."
            }
            4->{val n=k+c
                question="Örüntü: "+n+", "+(n+4)+", "+(n+8)+", ... Sonraki?"
                answer=(n+12).toString();choices=choiceNumbers(n+12,r)
                hint="Her adımda aynı miktar artıyor."
            }
            5->when(c){
                0->{
                    val chosen=listOf(23,29,31,37,41).random(r)
                    question="Aşağıdaki sayılardan hangisi asaldır?"
                    answer=chosen.toString()
                    choices=(listOf(21,25,27,33,35,39,45,49).shuffled(r)
                        .take(3).map{it.toString()}+answer).shuffled(r)
                    hint="Asal sayıların tam iki pozitif böleni bulunur."
                }
                1->{
                    val (fraction,decimal)=listOf(
                        "1/4" to "0,25","3/4" to "0,75",
                        "2/5" to "0,4","3/5" to "0,6",
                        "1/8" to "0,125","7/10" to "0,7").random(r)
                    question=decimal+" hangi kesre eşdeğerdir?"
                    answer=fraction
                    choices=(listOf("1/3","2/3","1/2","4/5","3/8","5/8")
                        .filterNot{it==answer}.shuffled(r).take(3)+answer).shuffled(r)
                    hint="Ondalık gösterimi bir kesre çevirip sadeleştir."
                }
                2->{
                    val (n,count)=listOf(24 to 8,36 to 9,40 to 8,
                        48 to 10,60 to 12,72 to 12,84 to 12).random(r)
                    question=n.toString()+" sayısının kaç pozitif böleni vardır?"
                    answer=count.toString()
                    choices=choiceNumbers(count,r)
                    hint="Asal çarpanlarına ayırıp üslerin bir fazlasını çarp."
                }
                else->{
                    val (numerator,denominator)=listOf(3 to 5,
                        1 to 4,3 to 4,2 to 5,7 to 10).random(r)
                    val result=100*numerator/denominator
                    question=numerator.toString()+"/"+denominator+" kesri yüzde kaçtır?"
                    answer="%"+result
                    choices=choiceNumbers(result,r).map{"%"+it}
                    hint="Kesri 100 üzerinden düşün."
                }
            }
            6->when(c){
                0->{
                    val number=listOf(36,42,54,66,78,84).random(r)
                    question="Hangi sayı hem 2'ye hem 3'e kalansız bölünür?"
                    answer=number.toString()
                    choices=listOf(number,number+1,number-1,number+2)
                        .map{it.toString()}.shuffled(r)
                    hint="6'nın katlarını bul."
                }
                1->{
                    val (decimal,fraction)=listOf("0,125" to "1/8",
                        "0,25" to "1/4","0,2" to "1/5","0,75" to "3/4",
                        "0,4" to "2/5","0,5" to "1/2").random(r)
                    question=decimal+" hangi kesre eşittir?"
                    answer=fraction
                    choices=(listOf("1/3","1/6","3/5","2/3","4/5","3/8")
                        .shuffled(r).take(3)+answer).shuffled(r)
                    hint="Ondalık sayıyı paydası 10, 100 veya 1000 olan kesir olarak düşün."
                }
                2->{
                    val (whole,percent)=listOf(240 to 25,150 to 20,
                        80 to 75,320 to 50,200 to 40,360 to 25).random(r)
                    val value=whole*percent/100
                    question=whole.toString()+" sayısının %"+percent+"'i kaçtır?"
                    answer=value.toString();choices=choiceNumbers(value,r)
                    hint="Yüzdeyi kesir olarak yaz: yüzde "+percent+"."
                }
                else->{
                    val (a,b,lcm)=listOf(Triple(8,12,24),Triple(6,15,30),
                        Triple(9,12,36),Triple(10,15,30),
                        Triple(14,21,42),Triple(12,18,36)).random(r)
                    question=a.toString()+" ve "+b+"'nin en küçük ortak katı kaçtır?"
                    answer=lcm.toString();choices=choiceNumbers(lcm,r)
                    hint="İkisinin katlarına bak; ilk ortak değeri bul."
                }
            }
            7->when(c){
                0->{
                    val (decimal,fraction)=listOf(
                        "-0,75" to "-3/4","-0,5" to "-1/2",
                        "-1,25" to "-5/4","-0,25" to "-1/4",
                        "-1,5" to "-3/2").random(r)
                    question=decimal+" sayısına eşdeğer kesri bul."
                    answer=fraction
                    choices=(listOf("3/4","1/4","-1/3","-2/3","-4/3","1/2")
                        .filterNot{it==answer}.shuffled(r).take(3)+answer).shuffled(r)
                    hint="Payı paydaya böl; eksi işaretini kaybetme."
                }
                1->{
                    val a=k+7;val b=k+3;val n=k-2
                    question="Sayı doğrusunda en küçük değeri seç."
                    answer=(-a).toString()
                    choices=listOf(-a,-b,-n,k).map{it.toString()}.shuffled(r)
                    hint="Negatif sayıda mutlak değeri daha büyük olan daha küçüktür."
                }
                2->{
                    val signPositive=r.nextBoolean()
                    question=if(signPositive)"(-3) × (-"+k+") işleminin işareti?"
                        else "(-3) × "+k+" işleminin işareti?"
                    answer=if(signPositive)"Pozitif" else "Negatif"
                    choices=listOf("Pozitif","Negatif","Sıfır","Belirsiz").shuffled(r)
                    hint="Aynı işaretli çarpanların çarpımı pozitif, farklıların negatiftir."
                }
                else->{
                    val a=k+4;val b=k-1
                    question="|-"+a+"| + (-"+b+") işleminin değeri nedir?"
                    val result=a-b
                    answer=result.toString();choices=choiceNumbers(result,r)
                    hint="Mutlak değer uzaklıktır. İşaretli toplama yap."
                }
            }
            else->when(c){
                0->{
                    val notSquare=listOf(18,20,27,45,63,75).random(r)
                    question="Karekökü tam sayı OLMAYAN ifadeyi bul."
                    answer="√"+notSquare
                    choices=(listOf("√16","√25","√36","√49","√64")
                        .shuffled(r).take(3)+answer).shuffled(r)
                    hint="Tam kareler, bir tam sayının karesidir."
                }
                1->{
                    val (base,exponent,result)=listOf(Triple(2,3,8),
                        Triple(3,2,9),Triple(4,2,16),Triple(2,4,16)).random(r)
                    question=base.toString()+"⁻"+exponent+" hangi kesre eşittir?"
                    answer="1/"+result
                    choices=listOf(answer,(-result).toString(),result.toString(),
                        "-1/"+result).shuffled(r)
                    hint="Negatif kuvvet, 1 bölü pozitif kuvvet demektir."
                }
                2->{
                    val a=r.nextInt(2,9);val b=r.nextInt(1,10)
                    val coefficient=10*a+b
                    val value=coefficient*100
                    question=a.toString()+","+b+" × 10³ kaç eder?"
                    answer=value.toString()
                    choices=listOf(value,value/10,value*10,coefficient)
                        .map{it.toString()}.shuffled(r)
                    hint="Virgülü sağa üç basamak kaydır."
                }
                else->{
                    val a=r.nextInt(3,14)
                    val b=r.nextInt(2,5)
                    val result=a+(1 shl b)
                    question="√"+(a*a)+" + 2"+when(b){2->"²";3->"³";else->"⁴"}+" kaçtır?"
                    answer=result.toString();choices=choiceNumbers(result,r)
                    hint="Önce kökü ve kuvveti hesapla."
                }
            }
        }
        return Cipher("Şifre Analizi · "+chapterTitles[c]+": "+question,
            choices,answer,hint,"Doğru şifre: "+answer)
    }

    private fun trail(g:Int,c:Int,t:LearningTier,r:Random):Trail{
        val extra=if(t==LearningTier.USTALIK)4 else 0
        val points=(0..3).map{step->
            val n=3+r.nextInt(2,9)+extra+step
            val m=2+r.nextInt(1,5)+c
            val q:String
            val result:Int
            when(g){
                1->{q="$n + $m";result=n+m}
                2->{q="$n × $m";result=n*m}
                3->{q="("+n+" × "+m+") - 2";result=n*m-2}
                4->{q=(n*m).toString()+" ÷ "+m+" + "+step;result=n+step}
                5->{q=(n*4).toString()+" sayısının 1/4'ü";result=n}
                6->{q=(n*10).toString()+" sayısının %20'si";result=n*2}
                7->when((step+c)%4){
                    0->{q="(-$n) + (-$m)";result=-(n+m)}
                    1->{q="(-$n) - (-$m)";result=m-n}
                    2->{q="(-$n) × 2";result=-n*2}
                    else->{q="|-$n| + $m";result=n+m}
                }
                else->when((step+c)%4){
                    0->{q="√"+(n*n)+" + 2²";result=n+4}
                    1->{q="3³ - √"+(m*m);result=27-m}
                    2->{q="√"+((n+m)*(n+m));result=n+m}
                    else->{q="("+n+" × 10²) ÷ 10";result=n*10}
                }
            }
            Checkpoint("Portal "+(step+1)+"/4: "+q+" = ?",
                choiceNumbers(result,r),result.toString())
        }
        return Trail("Yıldız Yolu · Dört matematik portalını aç · "+chapterTitles[c],
            points,"Her portalı doğru çöz; ara ilerlemen kaydedilir.",
            "Portal çözümleri: "+points.joinToString(" → "){it.answer})
    }
}
