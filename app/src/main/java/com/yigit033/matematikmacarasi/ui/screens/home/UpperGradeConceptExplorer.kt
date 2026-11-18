package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.AdvancedPreviewKind
import com.yigit033.matematikmacarasi.learning.CurriculumTopic
import com.yigit033.matematikmacarasi.learning.UpperGradeConceptPolicy
import kotlin.math.cos
import kotlin.math.sin

private val p3Gold = Color(0xFFFFDC92)
private val p3Teal = Color(0xFF8FEDE0)
private val p3Text = Color(0xFFC7E0EE)
private val p3Blue = Color(0xFF84AEFF)

/** The 5–8 learning previews are optional, read-only with respect to progress.
 * They never replace lessons, scores or Premium entitlement checks.
 */
@Composable
internal fun UpperGradeConceptExplorer(topic: CurriculumTopic) {
    val kind = UpperGradeConceptPolicy.previewFor(topic) ?: return
    var page by rememberSaveable(topic.id) { mutableIntStateOf(0) }
    val step = UpperGradeConceptPolicy.safeStep(kind, page)
    val total = UpperGradeConceptPolicy.steps(kind)
    val title = when (kind) {
        AdvancedPreviewKind.NUMBER_LINE -> if (topic.grade == 7)
            "Rasyonel sayıları sayı doğrusunda incele" else "Sayı doğrusunda konumları karşılaştır"
        AdvancedPreviewKind.FRACTION -> "Kesir ve ondalık miktarları incele"
        AdvancedPreviewKind.ANGLE -> "Açı değiştikçe şekli nasıl değişir?"
        AdvancedPreviewKind.AREA -> "Birim karelerle alanı keşfet"
        AdvancedPreviewKind.DATA -> "Sütun grafiklerinde veriyi karşılaştır"
        AdvancedPreviewKind.CHANCE -> "Olasılığı sonuç sayısıyla ilişkilendir"
        AdvancedPreviewKind.LINEAR -> "Doğrusal ilişkide değişkenleri keşfet"
        AdvancedPreviewKind.RATIO -> "Oran korunurken miktarlar değişir"
        AdvancedPreviewKind.REFLECTION -> "Y eksenine göre yansımayı incele"
        AdvancedPreviewKind.FACTORS -> "Bir sayının çarpanlarını keşfet"
        AdvancedPreviewKind.SQUARES -> "Kare sayılarla karekök bağlantısı"
        AdvancedPreviewKind.VOLUME -> "Birim küplerle hacmi modelle"
        AdvancedPreviewKind.MULTIPLES -> "Eş gruplar ve çarpma ilişkisi"
    }
    Column(
        Modifier.fillMaxWidth()
            .background(Color(0xFF153D60), RoundedCornerShape(20.dp))
            .border(1.dp, Color(0xFF548EA6), RoundedCornerShape(20.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("KONU KEŞİF LABORATUVARI", color = p3Gold,
            fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        Text(title, color = Color.White, fontSize = 17.sp,
            fontWeight = FontWeight.ExtraBold, lineHeight = 22.sp)
        AdvancedMathDiagram(kind, topic.grade, step)
        Text("Örnek " + (step + 1) + " / " + total, fontSize = 11.sp, color = p3Text)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { page = (step - 1).coerceAtLeast(0) },
                enabled = step > 0, modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF326D89))
            ) { Text("Önceki", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
            Button(
                onClick = { page = (step + 1).coerceAtMost(total - 1) },
                enabled = step < total - 1,
                modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF21847F))
            ) { Text("Sonraki örnek", fontSize = 12.sp, fontWeight = FontWeight.Bold) }
        }
        Text("Kavram önizlemesidir; ders tamamlamaz, puan veya Premium hakkı vermez.",
            color = p3Text, fontSize = 11.sp, lineHeight = 16.sp)
    }
}

@Composable
private fun AdvancedMathDiagram(kind: AdvancedPreviewKind, grade: Int, step: Int) {
    when (kind) {
        AdvancedPreviewKind.NUMBER_LINE -> AdvancedSignedNumberLine(grade, step)
        AdvancedPreviewKind.FRACTION -> {
            val (n,d) = UpperGradeConceptPolicy.fractionParts(step)
            val decimals = listOf("0,25","0,50","0,75","1,00")
            Formula(n.toString()+"/"+d+" = "+decimals[step])
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                repeat(d) { i -> MathTile(i<n,Modifier.weight(1f).height(65.dp)) }
            }
            Explanation("Bir bütün 4 eş parçaya bölündü ve "+n+" parçası boyandı.")
        }
        AdvancedPreviewKind.ANGLE -> {
            val angle = UpperGradeConceptPolicy.angleDegrees(step)
            Canvas(Modifier.fillMaxWidth().height(144.dp).semantics {
                contentDescription = angle.toString()+" derecelik açının iki ışını"
            }) {
                val ox=size.width*.42f
                val oy=size.height*.85f
                val len=minOf(size.width*.42f,size.height*.72f)
                val rad=Math.toRadians(angle.toDouble())
                drawLine(p3Teal,Offset(ox,oy),Offset(ox+len,oy),
                    4.dp.toPx(),cap=StrokeCap.Round)
                drawLine(p3Gold,Offset(ox,oy),
                    Offset(ox+len*cos(rad).toFloat(),oy-len*sin(rad).toFloat()),
                    4.dp.toPx(),cap=StrokeCap.Round)
                val r=len*.30f
                drawArc(p3Blue,-angle.toFloat(),angle.toFloat(),false,
                    topLeft=Offset(ox-r,oy-r),size=Size(r*2,r*2),
                    style=Stroke(width=2.dp.toPx()))
                drawCircle(Color.White,5.dp.toPx(),Offset(ox,oy))
            }
            Formula(angle.toString()+"° · "+when {
                angle<90 -> "Dar açı"; angle==90 -> "Dik açı"; else -> "Geniş açı"
            })
        }
        AdvancedPreviewKind.AREA -> {
            val rows=step+1
            Formula("4 sütun × "+rows+" satır = "+(4*rows)+" birim kare")
            repeat(rows) {
                Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                    repeat(4) { MathTile(true,Modifier.weight(1f).height(29.dp)) }
                }
            }
            Explanation("Her küçük kare bir birim alanı temsil eder.")
        }
        AdvancedPreviewKind.DATA -> {
            val a=step+1; val b=3
            Formula("A: "+a+" · B: "+b+" · Fark: "+kotlin.math.abs(a-b))
            Row(Modifier.fillMaxWidth().height(133.dp),
                horizontalArrangement=Arrangement.SpaceEvenly,
                verticalAlignment=Alignment.Bottom) {
                DiagramBar("A",a,p3Teal)
                DiagramBar("B",b,p3Blue)
            }
            Explanation("Sütun yükseklikleri ortak bir ölçekle çizildi.")
        }
        AdvancedPreviewKind.CHANCE -> {
            val (n,total)=UpperGradeConceptPolicy.probabilityRed(step)
            Formula("Kırmızı: "+n+"/"+total+" · Mavi: "+(total-n)+"/"+total)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
                repeat(total) { i ->
                    Box(Modifier.size(29.dp).background(
                        if (i<n) Color(0xFFF3A191) else p3Blue,
                        RoundedCornerShape(15.dp)))
                }
            }
            Explanation("Altı eş olasılıklı sonuçtan "+n+" tanesi kırmızı.")
        }
        AdvancedPreviewKind.LINEAR -> {
            val x=step-2
            Formula("y = 2x + 3")
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                for (i in -2..2) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                        Text("x="+i,color=if(i==x)p3Gold else p3Text,fontSize=12.sp)
                        Box(Modifier.background(if(i==x)Color(0xFF328D8B)
                            else Color(0xFF24546F),RoundedCornerShape(10.dp))
                            .padding(11.dp)) {
                            Text(UpperGradeConceptPolicy.linearY(i).toString(),
                                color=Color.White,fontWeight=FontWeight.Bold)
                        }
                    }
                }
            }
            Explanation("x="+x+" iken y="+UpperGradeConceptPolicy.linearY(x)+
                ". x bir artarsa y iki artar.")
        }
        AdvancedPreviewKind.RATIO -> {
            val (a,b)=UpperGradeConceptPolicy.ratioAmounts(step)
            Formula(a.toString()+" : "+b+" = 2 : 3")
            MathChips(a,p3Teal)
            MathChips(b,p3Blue)
            Explanation("İki miktar aynı katsayıyla çarpılınca oran sabit kalır.")
        }
        AdvancedPreviewKind.REFLECTION -> AdvancedYAxisReflection(step)
        AdvancedPreviewKind.FACTORS -> {
            val n=UpperGradeConceptPolicy.factorNumber(step)
            Formula(n.toString()+" sayısının pozitif bölenleri")
            Text(UpperGradeConceptPolicy.factorsOf(n).joinToString(" · "),
                color=Color.White,fontSize=18.sp,lineHeight=25.sp)
            Explanation("Her bölen "+n+" sayısını kalansız böler.")
        }
        AdvancedPreviewKind.SQUARES -> {
            val side=step+1
            val square=UpperGradeConceptPolicy.squareNumber(step)
            Formula(side.toString()+"² = "+square+" · √"+square+" = "+side)
            repeat(side) {
                Row(horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                    repeat(side) { MathTile(true,Modifier.size(31.dp)) }
                }
            }
            Explanation("Karenin her kenarında "+side+" birim kare var.")
        }
        AdvancedPreviewKind.VOLUME -> {
            val layers=step+1
            Formula("2 × 3 × "+layers+" = "+
                UpperGradeConceptPolicy.volumeUnitCubes(step)+" birim³")
            repeat(layers) { k ->
                Row(horizontalArrangement=Arrangement.spacedBy(6.dp),
                    verticalAlignment=Alignment.CenterVertically) {
                    Text((k+1).toString()+". kat",color=p3Text,fontSize=11.sp)
                    repeat(6) { MathTile(true,Modifier.weight(1f).height(26.dp)) }
                }
            }
            Explanation("Her kat 6 birim küptür; toplam hacim katlarla büyür.")
        }
        AdvancedPreviewKind.MULTIPLES -> {
            val k=step+1
            Formula("3 grup × "+k+" nesne = "+(3*k))
            repeat(3) {
                Row(horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                    repeat(k) { MathTile(true,Modifier.size(29.dp)) }
                }
            }
            Explanation("Eş gruplar tekrarlı toplama ve çarpma ile modellenir.")
        }
    }
}

@Composable
private fun Formula(value:String) {
    Text(value,color=p3Gold,fontSize=18.sp,fontWeight=FontWeight.ExtraBold,
        lineHeight=26.sp)
}

@Composable
private fun Explanation(value:String) {
    Text(value,color=p3Text,fontSize=12.sp,lineHeight=18.sp)
}

@Composable
private fun MathTile(active:Boolean,modifier:Modifier=Modifier) {
    Box(modifier
        .background(if(active)p3Teal else Color(0xFF27516F),
            RoundedCornerShape(6.dp))
        .border(1.dp,Color(0xFF9ECCDF),RoundedCornerShape(6.dp)))
}

@Composable
private fun MathChips(count:Int,color:Color) {
    Row(horizontalArrangement=Arrangement.spacedBy(3.dp)) {
        repeat(count) {
            Box(Modifier.size(17.dp).background(color,RoundedCornerShape(4.dp)))
        }
    }
}

@Composable
private fun DiagramBar(label:String,n:Int,color:Color) {
    Column(horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.spacedBy(5.dp)) {
        Text(n.toString(),color=Color.White,fontWeight=FontWeight.Bold)
        Box(Modifier.width(57.dp).height((n*17).dp)
            .background(color,RoundedCornerShape(topStart=8.dp,topEnd=8.dp)))
        Text(label,color=p3Gold,fontSize=12.sp)
    }
}

@Composable
private fun AdvancedSignedNumberLine(grade:Int,step:Int) {
    val numerator=UpperGradeConceptPolicy.lineNumerator(grade,step)
    val denom=UpperGradeConceptPolicy.lineDenominator(grade)
    fun number(n:Int):String=if(denom==1) n.toString()
        else if(n%2==0)(n/2).toString() else n.toString()+"/2"
    val selected=number(numerator)
    val animated by animateFloatAsState(targetValue=step.toFloat(),
        animationSpec=tween(260),label="Sayı doğrusundaki hareket")
    Formula("İşaretli değer: "+selected)
    Canvas(Modifier.fillMaxWidth().height(49.dp).semantics {
        contentDescription="Sayı doğrusunda işaretli değer "+selected
    }) {
        val left=13.dp.toPx()
        val span=size.width-2*left
        val y=size.height*.55f
        drawLine(p3Text,Offset(left,y),Offset(left+span,y),3.dp.toPx())
        for(i in 0..8) {
            val x=left+i*span/8f
            drawLine(p3Text,Offset(x,y-6.dp.toPx()),
                Offset(x,y+6.dp.toPx()),2.dp.toPx())
        }
        val x=left+animated*span/8f
        drawCircle(p3Gold,10.dp.toPx(),Offset(x,y))
        drawCircle(Color(0xFF153D60),4.dp.toPx(),Offset(x,y))
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
        for(i in 0..8) {
            Text(number(i-4),color=if(i==step)p3Gold else p3Text,fontSize=9.sp,
                fontWeight=if(i==step)FontWeight.Bold else FontWeight.Normal)
        }
    }
    Explanation(if(denom==2)
        "Her aralık 1/2 birimdir. 0'ın solu negatif, sağı pozitiftir."
        else "Her aralık 1 birimdir. 0'ın solu negatif, sağı pozitiftir.")
}

@Composable
private fun AdvancedYAxisReflection(step:Int) {
    val x=UpperGradeConceptPolicy.pointX(step)
    val reflected=UpperGradeConceptPolicy.reflectionAcrossYAxis(x)
    Formula("A("+x+", 2) → A′("+reflected+", 2)")
    Canvas(Modifier.fillMaxWidth().height(150.dp).semantics {
        contentDescription="Y eksenine göre yansıma. X "+x+
            ", yansıyan x "+reflected+", y 2 olarak değişmez."
    }) {
        val ox=size.width*.5f
        val oy=size.height*.75f
        val unit=(size.width*.125f).coerceAtMost(29.dp.toPx())
        drawLine(p3Text,Offset(ox-3.3f*unit,oy),
            Offset(ox+3.3f*unit,oy),2.dp.toPx())
        drawLine(p3Gold,Offset(ox,5.dp.toPx()),
            Offset(ox,size.height-5.dp.toPx()),2.dp.toPx())
        val y=oy-2*unit
        drawLine(p3Blue,Offset(ox+x*unit,y),Offset(ox+reflected*unit,y),
            2.dp.toPx())
        drawCircle(p3Teal,9.dp.toPx(),Offset(ox+x*unit,y))
        drawCircle(p3Gold,if(x==0)5.dp.toPx() else 9.dp.toPx(),
            Offset(ox+reflected*unit,y))
    }
    Explanation("Yansıma kuralı (x, y) → (−x, y). Y eksenine uzaklık korunur.")
}
