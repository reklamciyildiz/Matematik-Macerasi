package com.yigit033.matematikmacarasi.games.geometry

import android.graphics.Paint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

private val MeasureGold=Color(0xFFFFD68C)
private val MeasureCyan=Color(0xFF84F2E6)
private val MeasureWhite=Color(0xFFF6FEFA)
private val MeasureNavy=Color(0xFF102F52)

@Composable
fun GeometryMeasureBoard(task:CastleTask,value:Int,enabled:Boolean,
    onValue:(Int)->Unit,onVerify:()->Unit) {
    val step=task.measureStep
    val maximum=task.measureMax
    var draft by remember(task){mutableIntStateOf(value)}
    LaunchedEffect(value){draft=value}
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(12.dp),
        horizontalAlignment=Alignment.CenterHorizontally){
        Surface(shape=RoundedCornerShape(22.dp),color=Color(0xFF174966),
            border=BorderStroke(1.5.dp,MeasureCyan.copy(alpha=.7f))) {
            Column(Modifier.fillMaxWidth().padding(14.dp),
                horizontalAlignment=Alignment.CenterHorizontally){
                Text("✦ ÖLÇÜM ATÖLYESİ",color=MeasureGold,
                    fontWeight=FontWeight.Black,fontSize=16.sp)
                Text(when(task.measureKind){
                    "edges"->"DÜZ KENARLARI SAY"
                    "vertices"->"KÖŞELERİ SAY"
                    "perimeter"->"ÇEVREYİ HESAPLA"
                    "area","triangle_area"->"KAPLANAN ALANI HESAPLA"
                    else->"AÇIYI ÖLÇ"
                },color=MeasureCyan,fontSize=12.sp,fontWeight=FontWeight.Bold)
                GeometrySketch(task,Modifier.fillMaxWidth().height(180.dp))
                Text("KRİSTAL ÖLÇER",color=MeasureCyan,
                    fontSize=11.sp,fontWeight=FontWeight.Black)
                Text(value.toString()+" "+task.measureUnit,color=MeasureGold,
                    fontSize=32.sp,fontWeight=FontWeight.Black,
                    modifier=Modifier.semantics {
                        contentDescription="Ölçer: "+value+" "+task.measureUnit
                    })
            }
        }
        val jump=if(step==1)5 else step*2
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
            listOf(-jump,-step,step,jump).forEach{delta->
                val next=(value+delta).coerceIn(0,maximum)
                OutlinedButton(onClick={onValue(next)},
                    enabled=enabled && next!=value,
                    modifier=Modifier.weight(1f).heightIn(min=54.dp),
                    shape=RoundedCornerShape(13.dp),contentPadding=PaddingValues(3.dp),
                    border=BorderStroke(1.dp,MeasureCyan.copy(alpha=.7f)),
                    colors=ButtonDefaults.outlinedButtonColors(
                        containerColor=MeasureNavy,contentColor=MeasureWhite)){
                    Text((if(delta>0)"+" else "")+delta,fontSize=14.sp,
                        fontWeight=FontWeight.Black)
                }
            }
        }
        Slider(value=draft.toFloat(),
            onValueChange={draft=((it/step).roundToInt()*step).coerceIn(0,maximum)},
            onValueChangeFinished={if(draft!=value)onValue(draft)},
            valueRange=0f..maximum.toFloat(),
            steps=(maximum/step-1).coerceAtLeast(0),enabled=enabled,
            colors=SliderDefaults.colors(thumbColor=MeasureGold,
                activeTrackColor=MeasureCyan,inactiveTrackColor=Color(0xFF53768F)))
        Text("Şekli incele, ölçümünü ayarla, sonra doğrula.",
            color=MeasureWhite,fontSize=12.sp,textAlign=TextAlign.Center)
        Button(onClick=onVerify,enabled=enabled,
            modifier=Modifier.fillMaxWidth().heightIn(min=56.dp),
            shape=RoundedCornerShape(17.dp),
            colors=ButtonDefaults.buttonColors(
                containerColor=MeasureGold,contentColor=MeasureNavy)){
            Text("✦ ÖLÇÜMÜ DOĞRULA",fontSize=15.sp,fontWeight=FontWeight.Black)
        }
    }
}
@Composable
private fun GeometrySketch(task:CastleTask,modifier:Modifier=Modifier) {
    val kind=task.measureKind
    Canvas(modifier.semantics {
        contentDescription=when(kind){
            "perimeter"->"Dikdörtgenin kenarları "+
                task.measureWidth+" ve "+task.measureHeight+" birim"
            "area"->"Kareli dikdörtgen; "+
                task.measureWidth+" sütun ve "+task.measureHeight+" satır"
            "triangle_area"->"Üçgenin tabanı "+(2*task.measureWidth)+
                ", yüksekliği "+task.measureHeight
            "angle"->"İki ışından oluşan açının ölçüsünü bul"
            else->task.target.display+" şeklinin kenar ve köşelerini say"
        }
    }){
        val cx=size.width/2f;val cy=size.height/2f
        val paint=Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color=android.graphics.Color.WHITE
            textAlign=Paint.Align.CENTER
            textSize=13.sp.toPx()
            typeface=android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD)
        }
        if(kind=="edges"||kind=="vertices"){
            val points=GeometryShapeGeometry.vertices(task.target)
            val p=Path()
            points.forEachIndexed{i,v->
                if(i==0)p.moveTo(cx-size.height*.4f+v.first*size.height*.8f,
                    cy-size.height*.4f+v.second*size.height*.8f)
                else p.lineTo(cx-size.height*.4f+v.first*size.height*.8f,
                    cy-size.height*.4f+v.second*size.height*.8f)
            }
            p.close()
            if(task.target==CastleShape.CIRCLE)drawCircle(MeasureCyan,
                radius=size.height*.31f,center=Offset(cx,cy),
                style=Stroke(5.dp.toPx()))
            else {
                drawPath(p,Color(0xFF397EA8))
                drawPath(p,MeasureGold,style=Stroke(4.dp.toPx()))
            }
        }else if(kind=="angle"){
            val r=min(size.height*.38f,size.width*.28f)
            val a=task.measureAnswer*PI/180
            val origin=Offset(cx-r*.75f,cy+r*.5f)
            val start=Offset(origin.x+r*1.35f,origin.y)
            val end=Offset(origin.x+(r*1.35f*cos(a)).toFloat(),
                origin.y-(r*1.35f*sin(a)).toFloat())
            drawLine(MeasureGold,origin,start,strokeWidth=7.dp.toPx())
            drawLine(MeasureCyan,origin,end,strokeWidth=7.dp.toPx())
            drawCircle(Color.White,radius=7.dp.toPx(),center=origin)
            val arc=Path().apply{
                moveTo(origin.x+r*.48f,origin.y)
                for(i in 1..40){
                    val th=a*i/40
                    lineTo(origin.x+(r*.48f*cos(th)).toFloat(),
                        origin.y-(r*.48f*sin(th)).toFloat())
                }
            }
            drawPath(arc,MeasureGold,style=Stroke(3.dp.toPx()))
            // Readable protractor: 15° marks and every 30° numbered.
            // Angles cannot be estimated reliably from rays without a scale.
            for(mark in 0..12){
                val th=mark*15.0*PI/180.0
                val inner=if(mark%2==0)r*.59f else r*.63f
                val outer=r*.73f
                drawLine(if(mark%2==0)MeasureGold else MeasureCyan.copy(alpha=.65f),
                    Offset(origin.x+(inner*cos(th)).toFloat(),
                        origin.y-(inner*sin(th)).toFloat()),
                    Offset(origin.x+(outer*cos(th)).toFloat(),
                        origin.y-(outer*sin(th)).toFloat()),
                    strokeWidth=if(mark%2==0)2.dp.toPx() else 1.dp.toPx())
            }
            drawIntoCanvas{canvas->
                val marked=Paint(paint).apply{textSize=9.sp.toPx()}
                for(mark in 0..6){
                    val th=mark*30.0*PI/180.0
                    canvas.nativeCanvas.drawText((mark*30).toString(),
                        origin.x+(r*.88f*cos(th)).toFloat(),
                        origin.y-(r*.88f*sin(th)).toFloat(),
                        marked)
                }
                canvas.nativeCanvas.drawText(
                    "?",origin.x+r*.42f,origin.y-r*.32f,paint)
            }
        }else{
            val w=task.measureWidth;val h=task.measureHeight
            val unit=min(size.width*.62f/w,size.height*.62f/h)
            val width=unit*w;val height=unit*h
            val x=cx-width/2f;val y=cy-height/2f
            if(kind=="triangle_area"){
                val tri=Path().apply{
                    moveTo(cx,y);lineTo(cx+width/2f,y+height)
                    lineTo(cx-width/2f,y+height);close()
                }
                drawPath(tri,Color(0xFF317DA4))
                drawPath(tri,MeasureGold,style=Stroke(3.dp.toPx()))
                drawLine(MeasureCyan,Offset(cx,y),Offset(cx,y+height),
                    strokeWidth=2.dp.toPx())
            }else{
                drawRect(Color(0xFF2E729B),Offset(x,y),
                    androidx.compose.ui.geometry.Size(width,height))
                if(kind=="area"){
                    for(col in 1 until w)drawLine(MeasureCyan.copy(alpha=.6f),
                        Offset(x+unit*col,y),Offset(x+unit*col,y+height),
                        strokeWidth=1.dp.toPx())
                    for(row in 1 until h)drawLine(MeasureCyan.copy(alpha=.6f),
                        Offset(x,y+unit*row),Offset(x+width,y+unit*row),
                        strokeWidth=1.dp.toPx())
                }
                drawRect(MeasureGold,Offset(x,y),
                    androidx.compose.ui.geometry.Size(width,height),
                    style=Stroke(3.dp.toPx()))
            }
            drawIntoCanvas{canvas->
                canvas.nativeCanvas.drawText(
                    (if(kind=="triangle_area")2*w else w).toString()+" birim",
                    cx,cy+height/2f+18.dp.toPx(),paint)
                canvas.nativeCanvas.drawText(h.toString()+" birim",
                    cx+width/2f+34.dp.toPx(),cy,paint)
            }
        }
    }
}
