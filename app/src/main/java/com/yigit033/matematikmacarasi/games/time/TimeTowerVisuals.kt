package com.yigit033.matematikmacarasi.games.time

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

object TimeStyle {
    val ink=Color(0xFF091B42)
    val ocean=Color(0xFF122E65)
    val panel=Color(0xFF1C3F7B)
    val gold=Color(0xFFFFD98A)
    val ice=Color(0xFF75F1F6)
    val white=Color(0xFFF5FAFF)
    val lilac=Color(0xFFC9A8FF)
    val secondary=Color(0xFFC9E5F5)
}

@Composable
fun TimePanel(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit) {
    Surface(modifier=modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),
        color=TimeStyle.panel.copy(alpha=.98f),
        border=BorderStroke(1.5.dp,TimeStyle.ice.copy(alpha=.62f))) {
        Column(Modifier.padding(15.dp),verticalArrangement=Arrangement.spacedBy(12.dp),
            horizontalAlignment=Alignment.CenterHorizontally,content=content)
    }
}

@Composable
fun TimeTile(text:String,selected:Boolean=false,enabled:Boolean=true,
    modifier:Modifier=Modifier,onClick:()->Unit) {
    Surface(modifier=modifier.heightIn(min=54.dp).clickable(enabled=enabled,onClick=onClick),
        color=if(selected)Color(0xFF337DB0) else Color(0xFF193D70),
        shape=RoundedCornerShape(16.dp),
        border=BorderStroke(if(selected)2.dp else 1.dp,
            if(selected)TimeStyle.gold else TimeStyle.ice.copy(alpha=.45f))) {
        Box(Modifier.padding(horizontal=12.dp,vertical=12.dp),contentAlignment=Alignment.Center) {
            Text(text,color=if(enabled)TimeStyle.white else Color(0xFF89A2BD),
                fontSize=14.sp,fontWeight=FontWeight.Black,
                textAlign=TextAlign.Center)
        }
    }
}

enum class ClockHand { HOUR, MINUTE }

fun fingerToTime(pointer:Offset, width:Float,height:Float,current:Int,
    hand:ClockHand,minuteStep:Int):Int {
    if(width<=0 || height<=0 || current !in 0..1439)return current
    val dx=pointer.x-width/2f
    val dy=pointer.y-height/2f
    val angle=((Math.toDegrees(atan2(dy.toDouble(),dx.toDouble()))+90+360)%360)
    return if(hand==ClockHand.MINUTE) {
        val raw=((angle/360.0*60)/minuteStep).roundToInt()*minuteStep%60
        current/60*60+raw
    } else {
        val hour=(((angle/30.0).roundToInt())%12)
        val half=if(current>=720)12 else 0
        (half+hour)*60+current%60
    }
}

/** Animated proper clock with proportional hour-hand position and drag/tap rotation. */
@Composable
fun ClockFace(time:Int,interactive:Boolean,hand:ClockHand,
    minuteStep:Int,modifier:Modifier=Modifier,onChange:(Int)->Unit={}) {
    var preview by remember(time) { mutableIntStateOf(time) }
    val glowTransition=rememberInfiniteTransition(label="clock aura")
    val aura by glowTransition.animateFloat(
        initialValue=.36f,targetValue=.78f,
        animationSpec=infiniteRepeatable(tween(2300),RepeatMode.Reverse),
        label="clock glow"
    )
    val display=if(interactive)preview else time
    val minutes=display%60
    val hours=display/60%12
    val hourAngle=(((hours+minutes/60f)*30f)-90f)*(PI/180).toFloat()
    val minuteAngle=((minutes*6f)-90f)*(PI/180).toFloat()
    Canvas(modifier.size(242.dp)
        .semantics { contentDescription=if(interactive)
            "Dokunarak saat ibrelerini döndür. Seçili saat ${TimeTowerEngine.formatTime(display)}"
            else "Analog saat; akrep ve yelkovana bak." }
        .then(if(interactive) Modifier.pointerInput(time,hand,minuteStep) {
            awaitEachGesture {
                val down=awaitFirstDown(requireUnconsumed=false)
                var current=fingerToTime(down.position,size.width.toFloat(),
                    size.height.toFloat(),time,hand,minuteStep)
                preview=current
                var pressed=true
                while(pressed) {
                    val event=awaitPointerEvent()
                    val change=event.changes.firstOrNull()?:break
                    pressed=change.pressed
                    if(pressed) {
                        current=fingerToTime(change.position,size.width.toFloat(),
                            size.height.toFloat(),time,hand,minuteStep)
                        preview=current
                        change.consume()
                    }
                }
                if(current!=time)onChange(current)
            }
        } else Modifier)
    ) {
        val radius=min(size.width,size.height)*.45f
        val c=Offset(size.width/2f,size.height/2f)
        drawCircle(brush=Brush.radialGradient(
            listOf(TimeStyle.ice.copy(alpha=aura*.37f),
                TimeStyle.ice.copy(alpha=0f)),center=c,radius=radius*1.17f),
            radius=radius*1.17f,center=c)
        drawCircle(TimeStyle.gold.copy(alpha=.18f),radius=radius*1.05f,center=c)
        drawCircle(brush=Brush.radialGradient(
            listOf(Color(0xFFF6FBF2),Color(0xFFD9F4F1),Color(0xFF90D4E2)),
            center=Offset(c.x-radius*.28f,c.y-radius*.32f),radius=radius*1.5f
        ),radius=radius*.92f,center=c)
        drawCircle(TimeStyle.gold,radius=radius*.96f,center=c,style=Stroke(6.dp.toPx()))
        drawCircle(Color(0xFF315888).copy(alpha=.75f),radius=radius*.85f,center=c,
            style=Stroke(1.6.dp.toPx()))
        for(i in 0 until 60) {
            val angle=(i*6.0-90.0)*PI/180
            val isHour=i%5==0
            val start=if(isHour).72f else .78f
            val a=Offset(c.x+radius*start*cos(angle).toFloat(),
                c.y+radius*start*sin(angle).toFloat())
            val b=Offset(c.x+radius*.84f*cos(angle).toFloat(),
                c.y+radius*.84f*sin(angle).toFloat())
            drawLine(if(isHour)Color(0xFF204E81) else Color(0xFF5E91AE),
                a,b,strokeWidth=if(isHour)3.0.dp.toPx() else 1.2.dp.toPx(),
                cap=StrokeCap.Round)
        }
        drawIntoCanvas { canvas->
            val labelPaint=Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textAlign=Paint.Align.CENTER
                textSize=17.sp.toPx()
                typeface=android.graphics.Typeface.create(
                    android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD)
                color=android.graphics.Color.rgb(22,65,105)
            }
            for(hour in 1..12) {
                val ang=(hour*30.0-90)*PI/180
                canvas.nativeCanvas.drawText(hour.toString(),
                    c.x+radius*.63f*cos(ang).toFloat(),
                    c.y+radius*.63f*sin(ang).toFloat()+labelPaint.textSize*.32f,
                    labelPaint)
            }
        }
        val hourEnd=Offset(c.x+radius*.49f*cos(hourAngle),
            c.y+radius*.49f*sin(hourAngle))
        val minuteEnd=Offset(c.x+radius*.71f*cos(minuteAngle),
            c.y+radius*.71f*sin(minuteAngle))
        drawLine(Color(0xFF142C65),c,hourEnd,strokeWidth=9.dp.toPx(),cap=StrokeCap.Round)
        drawLine(Color(0xFF2BB3D6),c,minuteEnd,strokeWidth=6.dp.toPx(),cap=StrokeCap.Round)
        drawCircle(TimeStyle.gold,radius=11.dp.toPx(),center=c)
        drawCircle(Color(0xFFFFFFFF),radius=5.dp.toPx(),center=c)
        drawCircle(Color(0xFF2F5385),radius=2.8.dp.toPx(),center=c)
        for(i in 0..5) {
            val an=(i*60-90)*PI/180
            val x=c.x+radius*.99f*cos(an).toFloat()
            val y=c.y+radius*.99f*sin(an).toFloat()
            drawCircle(TimeStyle.ice,radius=2.dp.toPx(),center=Offset(x,y))
        }
    }
}

@Composable
fun TrainTimeline(progress:Float,departure:String,arrival:String) {
    val t=rememberInfiniteTransition(label="rail sparks")
    val glow by t.animateFloat(.3f,.8f,
        animationSpec=infiniteRepeatable(tween(1900),RepeatMode.Reverse),
        label="train glow")
    Canvas(Modifier.fillMaxWidth().height(120.dp)
        .semantics { contentDescription="Ray üzerindeki tren, ilerleme yüzde ${(progress*100).roundToInt()}" }) {
        val left=20.dp.toPx();val right=size.width-20.dp.toPx()
        val y=size.height*.66f
        drawLine(TimeStyle.gold,Offset(left,y+12.dp.toPx()),
            Offset(right,y+12.dp.toPx()),strokeWidth=4.dp.toPx(),cap=StrokeCap.Round)
        drawLine(Color(0xFF64BFE3),Offset(left,y-3.dp.toPx()),
            Offset(right,y-3.dp.toPx()),strokeWidth=4.dp.toPx())
        repeat(17) { index->
            val x=left+(right-left)*index/16f
            drawLine(Color(0xFFEACF9A),Offset(x,y-8.dp.toPx()),
                Offset(x,y+15.dp.toPx()),strokeWidth=3.dp.toPx())
        }
        val trainX=left+(right-left)*progress.coerceIn(0f,1f)
        val trainY=y-29.dp.toPx()
        drawCircle(TimeStyle.ice.copy(alpha=glow*.30f),
            radius=45.dp.toPx(),center=Offset(trainX,trainY))
        drawRoundRect(color=Color(0xFF7654B7),
            topLeft=Offset(trainX-34.dp.toPx(),trainY-12.dp.toPx()),
            size=Size(68.dp.toPx(),29.dp.toPx()),
            cornerRadius=androidx.compose.ui.geometry.CornerRadius(7.dp.toPx()))
        drawRoundRect(color=TimeStyle.gold,
            topLeft=Offset(trainX-27.dp.toPx(),trainY-23.dp.toPx()),
            size=Size(39.dp.toPx(),14.dp.toPx()),
            cornerRadius=androidx.compose.ui.geometry.CornerRadius(5.dp.toPx()))
        drawRect(Color(0xFF9EF5FA),topLeft=Offset(trainX-19.dp.toPx(),trainY-19.dp.toPx()),
            size=Size(16.dp.toPx(),10.dp.toPx()))
        drawCircle(Color(0xFF102B54),radius=7.dp.toPx(),
            center=Offset(trainX-20.dp.toPx(),trainY+17.dp.toPx()))
        drawCircle(Color(0xFF102B54),radius=7.dp.toPx(),
            center=Offset(trainX+22.dp.toPx(),trainY+17.dp.toPx()))
        for(i in 0..2) {
            drawCircle(TimeStyle.gold.copy(alpha=.65f-i*.16f),
                radius=(5-i).dp.toPx(),center=Offset(trainX-45.dp.toPx()-i*8.dp.toPx(),
                    trainY-18.dp.toPx()-i*4.dp.toPx()))
        }
    }
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
        Text(departure,color=TimeStyle.ice,fontSize=13.sp,fontWeight=FontWeight.Bold)
        Text(arrival,color=TimeStyle.gold,fontSize=13.sp,fontWeight=FontWeight.Bold)
    }
}

@Composable
fun CalendarBoard(challenge:TimeChallenge,selectedDate:Int,
    enabled:Boolean,onSelect:(Int)->Unit) {
    var shown by remember(challenge) {
        mutableStateOf(TowerDate(challenge.firstDate.year,challenge.firstDate.month,1))
    }
    val weekdays=listOf("Pt","Sa","Ça","Pe","Cu","Ct","Pa")
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(9.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.SpaceBetween) {
            TimeTile("‹",modifier=Modifier.width(55.dp),enabled=enabled) {
                val previous=shown.addDays(-1)
                shown=TowerDate(previous.year,previous.month,1)
            }
            Text("${TowerDate.MONTHS[shown.month-1]} ${shown.year}",color=TimeStyle.gold,
                fontWeight=FontWeight.Black,fontSize=17.sp)
            TimeTile("›",modifier=Modifier.width(55.dp),enabled=enabled) {
                val next=shown.addDays(TowerDate.daysInMonth(shown.year,shown.month))
                shown=TowerDate(next.year,next.month,1)
            }
        }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
            weekdays.forEach { day ->
                Box(Modifier.weight(1f),contentAlignment=Alignment.Center) {
                    Text(day,color=TimeStyle.ice,fontSize=11.sp,fontWeight=FontWeight.Bold)
                }
            }
        }
        val firstOffset=shown.weekdayMondayFirst()
        val days=TowerDate.daysInMonth(shown.year,shown.month)
        val rows=(firstOffset+days+6)/7
        repeat(rows) { row->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(3.dp)) {
                repeat(7) { column->
                    val day=row*7+column-firstOffset+1
                    if(day !in 1..days) {
                        Box(Modifier.weight(1f).height(43.dp))
                    } else {
                        val date=TowerDate(shown.year,shown.month,day)
                        val isChosen=date.code==selectedDate
                        val isOrigin=date==challenge.firstDate
                        Box(Modifier.weight(1f).height(43.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if(isChosen)TimeStyle.gold
                                else if(isOrigin)Color(0xFF236D93) else Color(0xFF173761)
                            )
                            .border(if(isOrigin)1.5.dp else .5.dp,
                                if(isOrigin)TimeStyle.ice else Color(0xFF41638E),
                                RoundedCornerShape(10.dp))
                            .clickable(enabled=enabled){onSelect(date.code)}
                            .semantics { contentDescription="${date.display()}${if(isOrigin)" başlangıç" else ""}" },
                            contentAlignment=Alignment.Center) {
                            Text("$day",color=if(isChosen)TimeStyle.ink else TimeStyle.white,
                                fontWeight=FontWeight.Black,fontSize=12.sp)
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
            Text("● Başlangıç",color=TimeStyle.ice,fontSize=11.sp)
            Text("● Seçimin",color=TimeStyle.gold,fontSize=11.sp)
        }
        if(selectedDate!=0)
            Text("Seçilen: ${TowerDate.fromCode(selectedDate)?.display().orEmpty()}",
                color=TimeStyle.gold,fontWeight=FontWeight.Black,fontSize=14.sp)
    }
}

@Composable
fun RouteBoard(challenge:TimeChallenge,selection:List<Int>,enabled:Boolean,
    onAdd:(Int)->Unit,onUndo:()->Unit) {
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(10.dp)) {
        Text("YOLCULUK PLANI",color=TimeStyle.ice,fontWeight=FontWeight.Black,fontSize=13.sp)
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
            repeat(challenge.events.size){ position->
                val id=selection.getOrNull(position)
                val event=challenge.events.firstOrNull{it.id==id}
                Box(Modifier.weight(1f).heightIn(min=67.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if(event==null)Color(0xFF193864) else Color(0xFF285F89))
                    .border(1.dp,if(event==null)Color(0xFF5079A2) else TimeStyle.gold,
                        RoundedCornerShape(10.dp)),
                    contentAlignment=Alignment.Center) {
                    Column(horizontalAlignment=Alignment.CenterHorizontally) {
                        Text(if(event==null)"${position+1}" else "✓",color=TimeStyle.gold,
                            fontSize=17.sp,fontWeight=FontWeight.Black)
                        Text(event?.let{TimeTowerEngine.formatTime(it.minute)} ?: "—",
                            color=TimeStyle.white,fontSize=10.sp)
                    }
                }
            }
        }
        challenge.events.forEach { event->
            val used=event.id in selection
            TimeTile(
                text="${if(used)"✓ " else "⏱ "}${event.title}  ·  ${TimeTowerEngine.formatTime(event.minute)}",
                selected=used,enabled=enabled&&!used,
                modifier=Modifier.fillMaxWidth()
            ){onAdd(event.id)}
        }
        if(selection.isNotEmpty() && enabled)
            OutlinedButton(onClick=onUndo,modifier=Modifier.fillMaxWidth(),
                border=BorderStroke(1.dp,TimeStyle.ice)){
                Text("↶ Son seçimi geri al",color=TimeStyle.white)
            }
    }
}
