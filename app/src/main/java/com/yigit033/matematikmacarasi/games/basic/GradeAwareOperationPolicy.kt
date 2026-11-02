package com.yigit033.matematikmacarasi.games.basic

import com.yigit033.matematikmacarasi.models.DifficultyLevel
import kotlin.random.Random

/** Hard school-grade ceiling, independent of the difficulty tier. */
enum class OperationKind(val title:String,val symbol:String,val minGrade:Int) {
    ADD("Toplama","+",1), SUBTRACT("Çıkarma","−",1),
    MULTIPLY("Çarpma","×",3), DIVIDE("Bölme","÷",3)
}
data class GradeQuestion(val first:Int,val second:Int,val answer:Int)
object GradeAwareOperationPolicy {
    fun grade(value:String?):Int {
        val first=Regex("""\b([1-8])\s*\.""").find(value?:"")?.groupValues?.get(1)?.toIntOrNull()
        return (first?:1).coerceIn(1,8)
    }
    fun allowed(grade:Int,op:OperationKind)=grade>=op.minGrade
    fun maxAnswer(grade:Int,difficulty:DifficultyLevel):Int {
        val level=difficulty.ordinal.coerceIn(0,4)
        return when(grade.coerceIn(1,8)) {
            1->listOf(10,12,15,18,20)[level]
            2->listOf(20,40,60,80,100)[level]
            3->listOf(50,100,150,250,500)[level]
            4->listOf(100,250,500,800,1000)[level]
            else->listOf(250,500,1000,2000,4000)[level]
        }
    }
    fun generate(grade:Int,op:OperationKind,difficulty:DifficultyLevel,random:Random=Random.Default):GradeQuestion {
        require(allowed(grade,op)) {"Operation not permitted for grade $grade"}
        val cap=maxAnswer(grade,difficulty)
        return when(op){
            OperationKind.ADD->{
                val sum=random.nextInt(3,cap+1)
                val a=random.nextInt(1,sum)
                GradeQuestion(a,sum-a,sum)
            }
            OperationKind.SUBTRACT->{
                val a=random.nextInt(3,cap+1)
                val b=random.nextInt(1,a)
                GradeQuestion(a,b,a-b)
            }
            OperationKind.MULTIPLY->{
                val factor=if(grade==3)listOf(4,5,6,8,10)[difficulty.ordinal.coerceIn(0,4)]
                    else listOf(6,8,10,12,15)[difficulty.ordinal.coerceIn(0,4)]
                val a=random.nextInt(2,factor+1)
                val b=random.nextInt(2,factor+1)
                GradeQuestion(a,b,a*b)
            }
            OperationKind.DIVIDE->{
                val factor=if(grade==3)listOf(4,5,6,8,10)[difficulty.ordinal.coerceIn(0,4)]
                    else listOf(6,8,10,12,15)[difficulty.ordinal.coerceIn(0,4)]
                val b=random.nextInt(2,factor+1)
                val result=random.nextInt(2,factor+1)
                GradeQuestion(b*result,b,result)
            }
        }
    }
    fun options(q:GradeQuestion,random:Random=Random.Default):List<String>{
        val set=linkedSetOf(q.answer)
        var distance=1
        while(set.size<4){
            val candidate=q.answer+(if(distance%2==0)-1 else 1)*distance
            if(candidate>=0)set.add(candidate)
            distance++
        }
        return set.toList().shuffled(random).map(Int::toString)
    }
}
