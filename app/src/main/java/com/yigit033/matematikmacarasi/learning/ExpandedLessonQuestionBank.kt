package com.yigit033.matematikmacarasi.learning

import kotlin.random.Random

/** Deterministic grade-specific problem bank; preserves existing numeric-choice UI. */
object ExpandedLessonQuestionBank {
    fun question(lesson: Lesson, seed: Long, index: Int): PracticeQuestion {
        require(index in 0..3 && lesson.skill == Skill.EXTENDED)
        val random = Random(seed + index * 7919L + lesson.id.hashCode().toLong())
        fun pick(a: Int, b: Int) = random.nextInt(a,b)
        val answer: Int
        val prompt: String
        val explain: String
        when (lesson.id) {
            "g1_count20" -> {
                // Both endpoints are part of the declared 10–20 concept.
                val n=pick(0,11); answer=10+n
                prompt="10 nesneye $n nesne eklenirse toplam kaç olur?"
                explain="Bir onluk ve $n birlik, $answer eder."
            }
            "g1_takeaway" -> {
                val n=pick(6,11); val r=pick(1,n); answer=n-r
                prompt="$n elmadan $r elma ayrılırsa kaç elma kalır?"
                explain="$n nesneden $r tanesini çıkarınca $answer kalır."
            }
            "g1_shapes" -> {
                val n=pick(0,3); val names=listOf("Üçgen","Kare","Dikdörtgen")
                answer=if(n==0) 3 else 4
                prompt=names[n]+" kaç kenarlıdır?"
                explain=names[n]+if(n==0) " üç kenarlıdır." else " dört kenarlıdır."
            }
            "g2_place" -> {
                val t=pick(2,9); val u=pick(1,10); answer=10*t+u
                prompt="$t onluk ve $u birlik kaç eder?"
                explain="$t onluk "+(10*t)+" eder. "+(10*t)+" + $u = $answer."
            }
            "g2_half" -> {
                val h=pick(2,11);answer=h
                prompt=(h*2).toString()+" nesnenin yarısı kaç nesnedir?"
                explain=(h*2).toString()+" nesneyi iki eş gruba ayırınca her grupta $h olur."
            }
            "g2_clock" -> {
                val h=pick(1,9);val k=pick(1,4);answer=h+k
                prompt="Saat $h iken $k saat sonra saat kaç olur?"
                explain="$h saatinden $k adım ilerlersen $answer olur."
            }
            "g3_third" -> {
                val h=pick(2,10);answer=h
                prompt=(h*3).toString()+" nesnenin üçte biri kaç nesnedir?"
                explain=(h*3).toString()+" nesneyi üç eş gruba ayırınca her grupta $answer kalır."
            }
            "g3_money" -> {
                val paid=pick(5,11)*10;val price=pick(1,paid/5)*5;answer=paid-price
                prompt="$paid TL ile $price TL'lik ürün alınırsa kaç TL para üstü kalır?"
                explain="Para üstü: $paid − $price = $answer."
            }
            "g3_chart" -> {
                val a=pick(8,16);val b=pick(2,8);answer=a-b
                prompt="Grafik: kitap $a, defter $b. Kitap kaç tane fazladır?"
                explain="İki sütun farkı: $a − $b = $answer."
            }
            "g4_fraction" -> {
                val d=pick(2,6);val n=pick(1,d);val unit=pick(2,10)
                val whole=d*unit;answer=n*unit
                prompt="$whole sayısının $n/$d kesri kaçtır?"
                explain="$whole sayısını $d eş parçaya böl: $unit. $n parçayı al: $answer."
            }
            "g4_perimeter" -> {
                val a=pick(5,15);val b=pick(2,a);answer=2*(a+b)
                prompt="Kenarları $a ve $b cm olan dikdörtgenin çevresi kaç cm?"
                explain="Dört kenarı topla: $a + $b + $a + $b = $answer."
            }
            "g4_chart" -> {
                val a=pick(2,8);val b=pick(2,8);val c=pick(2,8);answer=a+b+c
                prompt="Grafikte A=$a, B=$b, C=$c. Toplam kaçtır?"
                explain="Sütunların değerlerini topla: $a + $b + $c = $answer."
            }
            "g5_decimal" -> {
                val a=pick(1,8);val b=pick(1,10);answer=10*a+b
                prompt="$a tam $b onda bir toplam kaç onda birdir?"
                explain="$a tam "+(10*a)+" onda bir eder; $b daha ekle: $answer."
            }
            "g5_area" -> {
                val a=pick(4,12);val b=pick(2,9);answer=a*b
                prompt="$a birim uzunluk ve $b birim genişlikteki alan kaç birim kare?"
                explain="Her satırda $a kare, $b satır: $a × $b = $answer."
            }
            "g5_mean" -> {
                val a=pick(5,19);val d=pick(1,5);answer=a
                prompt=(a-d).toString()+", $a, "+(a+d)+" sayılarının ortalaması kaçtır?"
                explain="Toplam "+(3*a)+"; üç sayıya böl: $answer."
            }
            "g6_signed" -> {
                val a=pick(2,11);val rise=pick(a+2,a+12);answer=rise-a
                prompt="Sıcaklık −$a °C iken $rise °C artarsa kaç °C olur?"
                explain="−$a noktasından $rise birim sağa ilerle: $answer."
            }
            "g6_ratio" -> {
                val k=pick(2,8);answer=3*k
                prompt="2 ölçek meyveye 3 ölçek su gerekir. "+(2*k)+
                    " ölçek meyve için kaç ölçek su gerekir?"
                explain="İki miktarı aynı $k sayısı ile çarp: 3 × $k = $answer."
            }
            "g6_equation" -> {
                val x=pick(4,22);val a=pick(3,13);answer=x
                prompt="x + $a = "+(x+a)+" eşitliğinde x kaçtır?"
                explain="Her iki taraftan $a çıkar: x = $answer."
            }
            "g7_rational" -> {
                val a=pick(1,5);val b=pick(a+1,10);val k=pick(2,6);answer=a*k
                prompt="$a/$b kesrine denk, paydası "+(b*k)+" olan kesrin payı kaçtır?"
                explain="Payda $k katına çıktıysa pay da $k katına çıkar: $answer."
            }
            "g7_proportion" -> {
                val price=pick(2,9);val q=pick(3,7);val target=pick(7,12)
                answer=price*target
                prompt="$q bilet "+(q*price)+" TL ise $target bilet kaç TL'dir?"
                explain="Bir bilet $price TL. $target bilet: $answer TL."
            }
            "g7_equation" -> {
                val x=pick(2,14);val k=pick(2,6);val a=pick(1,10);answer=x
                prompt=k.toString()+"x + $a = "+(k*x+a)+" denkleminde x kaçtır?"
                explain="Önce $a çıkar, sonra $k ile böl: x = $answer."
            }
            "g8_gcd" -> {
                val common=pick(2,14)
                val pair=listOf(2 to 3,3 to 4,4 to 5,5 to 6)[pick(0,4)]
                val a=common*pair.first;val b=common*pair.second
                answer=common
                prompt="$a ve $b sayılarının EBOB'u kaçtır?"
                explain=pair.first.toString()+" ve "+pair.second+
                    " aralarında asaldır; EBOB = $answer."
            }
            "g8_power" -> {
                val a=pick(2,5);val exponent=pick(2,5)
                answer=(1..exponent).fold(1){value,_->value*a}
                prompt="$a üssü $exponent ifadesinin değeri kaçtır?"
                explain="$a sayısını $exponent kez çarp: $answer."
            }
            "g8_equation" -> {
                val x=pick(2,14);val a=pick(1,9);val k=pick(2,6);answer=x
                prompt="$k(x + $a) = "+(k*(x+a))+" denkleminde x kaçtır?"
                explain="Önce $k ile böl, sonra $a çıkar: x = $answer."
            }
            else -> error("Unknown expanded lesson: "+lesson.id)
        }
        require(answer > 0)
        val choices=mutableListOf(answer)
        for(v in listOf(answer-2,answer+2,answer-1,answer+1,
            answer+3,answer+5,answer*2,answer/2,answer+10)){
            if(v>0 && v !in choices)choices.add(v)
            if(choices.size==4)break
        }
        require(choices.size==4)
        return PracticeQuestion(prompt,choices.shuffled(random),answer,explain)
    }
}
