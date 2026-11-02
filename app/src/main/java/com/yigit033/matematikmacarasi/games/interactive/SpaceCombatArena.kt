package com.yigit033.matematikmacarasi.games.interactive

import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

private val spaceWhite=Color(0xFFF4FBFF)
private val spaceGold=Color(0xFFFFD784)
private val spaceTeal=Color(0xFF79F4E9)
private val spaceMidnight=Color(0xFF070D2D)
private data class SpaceBurst(val x:Float,val y:Float,val value:Int,val key:Int)
private val nebulaColors=listOf(
    Color(0xFF41CFA4),Color(0xFF50C5F0),
    Color(0xFFEC705B),Color(0xFFB699F9),Color(0xFFEF8FCA)
)

/**
 * One real arcade arena used by ALL three missions.
 * Drag the ship freely and shoot moving targets; reactor fuel and star links are
 * now produced by actual projectile collisions, never by selecting an answer card.
 */
@Composable
fun SpaceCombatArena(
    run:SpaceRun,
    puzzle:SpacePuzzle,
    onPosition:(Float)->Unit,
    onHit:(Int)->Unit
) {
    var shipX by remember(run.seed,run.stage) {mutableFloatStateOf(run.shipX)}
    var frame by remember(run.seed,run.stage,run.wave) {
        mutableStateOf(SpaceCombatFrame())
    }
    var paused by rememberSaveable(run.seed,run.stage) {mutableStateOf(true)}
    var lastShotAt by remember(run.seed,run.stage) {mutableLongStateOf(0L)}
    var burst by remember(run.seed,run.stage) {mutableStateOf<SpaceBurst?>(null)}
    var burstId by remember(run.seed,run.stage) {mutableIntStateOf(0)}
    val burstProgress=remember(run.seed,run.stage) {Animatable(1f)}
    LaunchedEffect(burst?.key) {
        if(burst!=null) {
            burstProgress.snapTo(0f)
            burstProgress.animateTo(1f,tween(440))
        }
    }
    val targetValues=remember(run.seed,run.stage) {SpaceCombatPhysics.targets(run)}
    val owner=LocalLifecycleOwner.current
    DisposableEffect(owner){
        val listener=LifecycleEventObserver {_,event->
            if(event==Lifecycle.Event.ON_STOP)paused=true
        }
        owner.lifecycle.addObserver(listener)
        onDispose {owner.lifecycle.removeObserver(listener)}
    }
    val latestHit by rememberUpdatedState(onHit)
    val latestPosition by rememberUpdatedState(onPosition)
    val currentTier=run.tier.ordinal
    val deviceHeightDp=LocalConfiguration.current.screenHeightDp
    val arenaHeight=when {
        deviceHeightDp<690->208.dp
        deviceHeightDp<790->244.dp
        deviceHeightDp<880->300.dp
        else->365.dp
    }
    LaunchedEffect(run.seed,run.stage,run.wave,run.solved,paused) {
        if(paused || run.solved)return@LaunchedEffect
        var last=0L
        while(true) {
            withFrameNanos {now->
                if(last!=0L){
                    val dt=((now-last)/1_000_000_000f).coerceIn(0f,.05f)
                    val result=SpaceCombatPhysics.advance(
                        frame,dt,targetValues,currentTier
                    )
                    frame=result.frame
                    if(result.impact!=null) {
                        val struck=targetValues.firstOrNull {
                            it.value==result.impact
                        }
                        if(struck!=null) {
                            val hitLocation=SpaceCombatPhysics.position(
                                struck,result.frame.elapsed,currentTier
                            )
                            burstId+=1
                            burst=SpaceBurst(hitLocation.x,hitLocation.y,
                                result.impact,burstId)
                        }
                        frame=SpaceCombatFrame()
                        latestHit(result.impact)
                    }
                }
                last=now
            }
        }
    }
    fun move(x:Float) {
        val normalized=x.coerceIn(.06f,.94f)
        shipX=normalized
        latestPosition(normalized)
    }
    fun fire() {
        if(paused || run.solved)return
        val now=SystemClock.uptimeMillis()
        if(now-lastShotAt<170)return
        lastShotAt=now
        frame=SpaceCombatPhysics.fire(frame,shipX)
    }
    val planetColor=nebulaColors[run.planet.ordinal]
    val missionHint=when(run.stage){
        0->"🎯 Doğru sonucu taşıyan meteoru lazerle vur."
        1->"⚡ Kapsülleri vurup enerjiyi ayarla; tam dolunca çekirdeği vur."
        else->"✧ Sonraki sayıyı taşıyan yıldızı sırayla vur."
    }
    Column(
        modifier=Modifier.fillMaxWidth(),
        verticalArrangement=Arrangement.spacedBy(9.dp)
    ) {
        Row(Modifier.fillMaxWidth(),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.SpaceBetween){
            Text("GALAKSİ ATIŞ SAHASI",
                color=spaceGold,fontSize=12.sp,fontWeight=FontWeight.Black)
            OutlinedButton(
                onClick={paused=!paused},
                modifier=Modifier.heightIn(min=44.dp)
                    .testTag("space_pause"),
                shape=RoundedCornerShape(14.dp),
                contentPadding=PaddingValues(horizontal=11.dp),
                border=BorderStroke(1.dp,spaceTeal),
                colors=ButtonDefaults.outlinedButtonColors(contentColor=spaceWhite)
            ){
                Text(if(paused)"▶ BAŞLAT" else "Ⅱ DURDUR",
                    fontSize=12.sp,fontWeight=FontWeight.Bold)
            }
        }
        Text(missionHint,color=spaceWhite,
            fontSize=12.sp,fontWeight=FontWeight.Medium)
        Box(
            modifier=Modifier.fillMaxWidth().height(arenaHeight)
                .clip(RoundedCornerShape(23.dp))
                .border(2.dp,planetColor.copy(alpha=.88f),
                    RoundedCornerShape(23.dp))
                .testTag("space_combat_arena")
                .pointerInput(run.seed,run.stage,paused,run.solved) {
                    detectDragGestures(
                        onDragStart={position->
                            if(!paused&&!run.solved)
                                shipX=(position.x/size.width)
                                    .coerceIn(.06f,.94f)
                        },
                        onDrag={change,_->
                            change.consume()
                            if(!paused&&!run.solved)
                                shipX=(change.position.x/size.width)
                                    .coerceIn(.06f,.94f)
                        },
                        onDragEnd={if(!paused&&!run.solved)
                            latestPosition(shipX)}
                    )
                }
        ){
            val paint=remember {android.graphics.Paint(
                android.graphics.Paint.ANTI_ALIAS_FLAG
            ).apply{
                textAlign=android.graphics.Paint.Align.CENTER
                typeface=android.graphics.Typeface.create(
                    "sans-serif-black",android.graphics.Typeface.BOLD)
            }}
            Canvas(Modifier.fillMaxSize()){
                val w=size.width
                val h=size.height
                val accent=planetColor
                drawRect(Brush.verticalGradient(listOf(
                    spaceMidnight,Color(0xFF182353),Color(0xFF121F4B)
                )))
                // Subtle planetary atmosphere and a distant cosmic horizon.
                drawCircle(
                    Brush.radialGradient(listOf(
                        accent.copy(alpha=.30f),accent.copy(alpha=0f)
                    ),center=Offset(w*.86f,h*.08f),radius=w*.70f),
                    radius=w*.70f,center=Offset(w*.86f,h*.08f)
                )
                drawCircle(
                    Brush.radialGradient(listOf(
                        accent.copy(alpha=.85f),accent.copy(alpha=.25f),
                        Color(0xFF0A1C40)
                    ),center=Offset(w*.84f,-h*.08f),radius=w*.34f),
                    radius=w*.34f,center=Offset(w*.84f,-h*.08f)
                )
                // Animated starfield: two parallax layers, stable seeded pattern.
                for(i in 0..64) {
                    val x=w*((i*37+run.planet.ordinal*11)%101)/101f
                    val speed=if(i%4==0).047f else .023f
                    val y=h*(((i*59%103)/103f+
                        frame.elapsed*speed)%1f)
                    val starSize=if(i%5==0)2.7f else 1.2f
                    drawCircle(spaceWhite.copy(alpha=.23f+(i%4)*.16f),
                        radius=starSize,center=Offset(x,y))
                }
                // Aiming beam / ship firing column.
                val sx=w*shipX
                val sy=h*.85f
                drawLine(
                    Brush.verticalGradient(listOf(
                        spaceTeal.copy(alpha=0f),spaceTeal.copy(alpha=.20f)
                    )),Offset(sx,0f),Offset(sx,sy),
                    strokeWidth=w*.027f
                )
                drawLine(spaceTeal.copy(alpha=.30f),
                    Offset(sx,h*.10f),Offset(sx,sy),
                    strokeWidth=1.8f)
                for(laser in frame.lasers){
                    val at=Offset(w*laser.x,h*laser.y)
                    drawCircle(spaceTeal.copy(alpha=.24f),
                        radius=w*.027f,center=at)
                    drawLine(spaceTeal,
                        Offset(at.x,at.y+h*.036f),
                        Offset(at.x,at.y-h*.039f),
                        strokeWidth=w*.013f)
                    drawLine(spaceWhite,
                        Offset(at.x,at.y+h*.020f),
                        Offset(at.x,at.y-h*.025f),
                        strokeWidth=w*.006f)
                }
                targetValues.forEach{target->
                    val place=SpaceCombatPhysics.position(
                        target,frame.elapsed,currentTier)
                    val x=w*place.x
                    val y=h*place.y
                    val radius=w*(if(targetValues.size>=5).075f else .092f)
                    if(y < -radius*1.2f || y>h+radius) return@forEach
                    val isNode=run.stage==2
                    val color=if(isNode)spaceGold else
                        nebulaColors[(run.planet.ordinal+target.id)%5]
                    drawCircle(color.copy(alpha=.13f),
                        radius=radius*1.44f,center=Offset(x,y))
                    drawCircle(color.copy(alpha=.28f),
                        radius=radius*1.22f,center=Offset(x,y))
                    val shape=Path().apply{
                        val points=if(isNode)10 else 8
                        for(i in 0 until points){
                            val angle=i*2.0*PI/points-PI/2
                            val scale=if(isNode && i%2==1).56f else 1f
                            val px=x+cos(angle).toFloat()*radius*scale
                            val py=y+sin(angle).toFloat()*radius*scale
                            if(i==0)moveTo(px,py) else lineTo(px,py)
                        }
                        close()
                    }
                    drawPath(shape,Brush.linearGradient(listOf(
                        spaceWhite.copy(alpha=.96f),color,
                        color.copy(alpha=.80f)
                    )))
                    drawPath(shape,color=spaceWhite.copy(alpha=.75f),
                        style=Stroke(width=2.6f))
                    drawCircle(spaceWhite.copy(alpha=.41f),
                        radius=radius*.14f,
                        center=Offset(x-radius*.37f,y-radius*.35f))
                    val display=if(target.value==Int.MIN_VALUE)"GO" else
                        (if(target.value>0 && run.stage==1)
                            "+"+target.value else target.value.toString())
                    paint.color=android.graphics.Color.rgb(13,28,61)
                    paint.textSize=if(display.length>=3)radius*.67f
                        else radius*.89f
                    drawIntoCanvas {it.nativeCanvas.drawText(
                        display,x,y+paint.textSize*.31f,paint)}
                    if(run.stage==2 && target.value in run.path){
                        drawCircle(spaceTeal.copy(alpha=.8f),
                            radius=radius*1.18f,center=Offset(x,y),
                            style=Stroke(5f))
                    }
                }
                // Explosion feedback is independent of mission state and survives
                // an immediate answer/round transition.
                burst?.let {impact->
                    val t=burstProgress.value.coerceIn(0f,1f)
                    if(t<1f){
                        val cx=w*impact.x
                        val cy=h*impact.y
                        val radius=w*(.035f+t*.17f)
                        drawCircle(spaceGold.copy(alpha=(1f-t)*.87f),
                            radius=radius,center=Offset(cx,cy),
                            style=Stroke(width=(1f-t)*7f+2f))
                        drawCircle(spaceTeal.copy(alpha=(1f-t)*.65f),
                            radius=radius*.65f,center=Offset(cx,cy),
                            style=Stroke(width=(1f-t)*5f+1f))
                        for(i in 0..13){
                            val a=i*2*PI/14f
                            val spread=radius*(.45f+t*.65f)
                            val px=cx+cos(a).toFloat()*spread
                            val py=cy+sin(a).toFloat()*spread
                            drawCircle(
                                if(i%2==0)spaceGold.copy(alpha=1f-t)
                                    else spaceWhite.copy(alpha=1f-t),
                                radius=w*.008f*(1f-t)+.5f,
                                center=Offset(px,py))
                        }
                    }
                }
                // Ship: layered white-and-cyan hull, canopy, lateral wings,
                // visible engines and animated twin-thruster exhaust.
                val ship=Path().apply{
                    moveTo(sx,sy-h*.084f)
                    lineTo(sx+w*.048f,sy-h*.003f)
                    lineTo(sx+w*.092f,sy+h*.045f)
                    lineTo(sx+w*.060f,sy+h*.069f)
                    lineTo(sx+w*.021f,sy+h*.032f)
                    lineTo(sx-w*.021f,sy+h*.032f)
                    lineTo(sx-w*.060f,sy+h*.069f)
                    lineTo(sx-w*.092f,sy+h*.045f)
                    lineTo(sx-w*.048f,sy-h*.003f)
                    close()
                }
                drawPath(ship,Brush.verticalGradient(listOf(
                    spaceWhite,Color(0xFF7BCDEB),Color(0xFF2F72A8)
                )))
                drawPath(ship,spaceWhite.copy(alpha=.92f),
                    style=Stroke(2.5f))
                drawOval(Brush.verticalGradient(listOf(
                    Color(0xFF113A73),spaceTeal
                )),topLeft=Offset(sx-w*.022f,sy-h*.024f),
                    size=Size(w*.044f,h*.049f))
                val flicker=.65f+.35f*sin(frame.elapsed*17f)
                for(sign in listOf(-1,1)){
                    val x=sx+sign*w*.037f
                    drawLine(spaceTeal.copy(alpha=.4f),
                        Offset(x,sy+h*.050f),
                        Offset(x,sy+h*(.11f+flicker*.020f)),
                        strokeWidth=w*.022f)
                    drawLine(spaceGold,
                        Offset(x,sy+h*.050f),
                        Offset(x,sy+h*(.080f+flicker*.017f)),
                        strokeWidth=w*.009f)
                }
                // Spaceframe perimeter and HUD markings.
                drawLine(accent.copy(alpha=.28f),
                    Offset(w*.06f,h*.07f),
                    Offset(w*.94f,h*.07f),2f)
                drawLine(accent.copy(alpha=.42f),
                    Offset(w*.06f,h*.93f),
                    Offset(w*.94f,h*.93f),2f)
            }
            if(paused && !run.solved){
                Surface(modifier=Modifier.align(Alignment.Center),
                    color=spaceMidnight.copy(alpha=.94f),
                    shape=RoundedCornerShape(17.dp),
                    border=BorderStroke(1.dp,spaceGold)){
                    Column(Modifier.padding(horizontal=18.dp,vertical=14.dp),
                        horizontalAlignment=Alignment.CenterHorizontally,
                        verticalArrangement=Arrangement.spacedBy(5.dp)){
                        Text("🚀 HAZIR MISIN?",color=spaceGold,
                            fontSize=17.sp,fontWeight=FontWeight.Black)
                        Text("Gemiyi sürükle, ardından LAZER ile hedefleri vur.",
                            color=spaceWhite,fontSize=12.sp,
                            textAlign=TextAlign.Center)
                        Text("Başlat düğmesine dokun.",
                            color=spaceTeal,fontSize=11.sp)
                    }
                }
            }
            if(run.solved)Surface(
                modifier=Modifier.align(Alignment.Center),
                color=Color(0xFF135761).copy(alpha=.91f),
                shape=RoundedCornerShape(18.dp),
                border=BorderStroke(2.dp,spaceGold)
            ){
                Text("✓ HEDEF TAMAMLANDI",
                    modifier=Modifier.padding(15.dp),
                    color=spaceWhite,fontWeight=FontWeight.Black)
            }
        }
        Row(Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.spacedBy(9.dp),
            verticalAlignment=Alignment.CenterVertically){
            OutlinedButton(
                onClick={move((shipX-.14f).coerceAtLeast(.06f))},
                enabled=!paused&&!run.solved,
                modifier=Modifier.weight(1f).heightIn(min=58.dp)
                    .testTag("space_steer_left"),
                colors=ButtonDefaults.outlinedButtonColors(
                    contentColor=spaceWhite),
                border=BorderStroke(2.dp,spaceTeal)
            ){Text("◀",fontSize=23.sp)}
            Button(
                onClick={fire()},
                enabled=!paused&&!run.solved,
                modifier=Modifier.weight(1.7f).heightIn(min=68.dp)
                    .testTag("space_fire"),
                shape=RoundedCornerShape(18.dp),
                colors=ButtonDefaults.buttonColors(
                    containerColor=Color(0xFFDF6271),
                    contentColor=spaceWhite),
                border=BorderStroke(2.dp,spaceGold)
            ){
                Text("⚡ LAZER",fontSize=17.sp,fontWeight=FontWeight.Black)
            }
            OutlinedButton(
                onClick={move((shipX+.14f).coerceAtMost(.94f))},
                enabled=!paused&&!run.solved,
                modifier=Modifier.weight(1f).heightIn(min=58.dp)
                    .testTag("space_steer_right"),
                colors=ButtonDefaults.outlinedButtonColors(
                    contentColor=spaceWhite),
                border=BorderStroke(2.dp,spaceTeal)
            ){Text("▶",fontSize=23.sp)}
        }
        Row(Modifier.fillMaxWidth(),
            horizontalArrangement=Arrangement.SpaceBetween){
            Text("Parmağınla gemiyi sürükle",
                color=spaceTeal,fontSize=11.sp)
            Text("Lazer: "+frame.lasers.size+" uçuşta",
                color=spaceWhite,fontSize=11.sp,
                modifier=Modifier.testTag("space_laser_count"))
        }
        if(run.stage==1)Text(
            "REAKTÖR  "+run.charge+"/"+puzzle.answer+
                " enerji · +1 / +5 / +10 / −5 kapsüllerini vur; GO'ya hazırken ateş et.",
            color=spaceGold,fontWeight=FontWeight.Bold,fontSize=12.sp,
            modifier=Modifier.fillMaxWidth()
                .background(Color(0xFF183764),RoundedCornerShape(12.dp))
                .padding(11.dp)
        )
        if(run.stage==2)Text(
            "YILDIZ ZİNCİRİ  "+run.path.size+"/3 bağlandı · "+
                puzzle.sequence.take(run.path.size).joinToString(" → "),
            color=spaceGold,fontWeight=FontWeight.Bold,fontSize=12.sp,
            modifier=Modifier.fillMaxWidth()
                .background(Color(0xFF183764),RoundedCornerShape(12.dp))
                .padding(11.dp)
        )
    }
}
