package com.yigit033.matematikmacarasi.games.interactive

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.SystemClock
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.math.cos
import kotlin.math.sin
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.services.AudioService
import kotlin.math.PI

private val spaceWhite=Color(0xFFF4FBFF)
private val spaceGold=Color(0xFFFFD784)
private val spaceTeal=Color(0xFF79F4E9)
private val spaceMidnight=Color(0xFF070D2D)
private data class SpaceBurst(val x:Float,val y:Float,val value:Int,val correct:Boolean,val key:Int)
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
    modifier:Modifier=Modifier,
    onPosition:(Float)->Unit,
    onHit:(Int)->Unit
) {
    var shipX by remember(run.seed) {mutableFloatStateOf(run.shipX)}
    var frame by remember(run.seed,run.stage,run.wave) {
        mutableStateOf(SpaceCombatFrame())
    }
    var paused by rememberSaveable(run.seed) {mutableStateOf(true)}
    var lastShotAt by remember(run.seed) {mutableLongStateOf(0L)}
    val context=LocalContext.current.applicationContext
    val haptics=LocalHapticFeedback.current
    val shipSprite=remember(context){ImageBitmap.imageResource(context.resources,R.drawable.space_ship_premium)}
    val laserSprite=remember(context){ImageBitmap.imageResource(context.resources,R.drawable.space_laser_premium)}
    val impactSprite=remember(context){ImageBitmap.imageResource(context.resources,R.drawable.space_impact_premium)}
    val meteorSprites=remember(context){listOf(
        ImageBitmap.imageResource(context.resources,R.drawable.space_meteor_1_premium),
        ImageBitmap.imageResource(context.resources,R.drawable.space_meteor_2_premium),
        ImageBitmap.imageResource(context.resources,R.drawable.space_meteor_3_premium),
        ImageBitmap.imageResource(context.resources,R.drawable.space_meteor_4_premium)
    )}
    val soundPool=remember(context) {
        SoundPool.Builder().setMaxStreams(3).setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        ).build()
    }
    var shotSoundId by remember(context) {mutableIntStateOf(0)}
    var soundLoaded by remember(context) {mutableStateOf(false)}
    DisposableEffect(context,soundPool) {
        soundPool.setOnLoadCompleteListener {_,id,status->
            if(id==shotSoundId && status==0)soundLoaded=true
        }
        shotSoundId=soundPool.load(context,R.raw.laser_sound,1)
        onDispose{soundPool.release()}
    }
    var burst by remember(run.seed) {mutableStateOf<SpaceBurst?>(null)}
    var burstId by remember(run.seed) {mutableIntStateOf(0)}
    val burstProgress=remember(run.seed) {Animatable(1f)}
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
    LaunchedEffect(run.seed,run.stage,run.wave,run.completed,paused) {
        if(paused || run.completed)return@LaunchedEffect
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
                                result.impact,result.impact==puzzle.answer,burstId)
                            haptics.performHapticFeedback(
                                if(result.impact==puzzle.answer)HapticFeedbackType.LongPress
                                else HapticFeedbackType.TextHandleMove
                            )
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
        if(paused || run.completed)return
        val now=SystemClock.uptimeMillis()
        if(now-lastShotAt<170)return
        lastShotAt=now
        val before=frame.lasers.size
        frame=SpaceCombatPhysics.fire(frame,shipX)
        if(frame.lasers.size>before && soundLoaded) {
            runCatching {
                if(AudioService.getInstance(context).isSoundEnabled())
                    soundPool.play(shotSoundId,.25f,.25f,1,0,1f)
            }
        }
    }
    val planetColor=nebulaColors[run.planet.ordinal]
    val missionHint="🎯 Doğru sonucu vur, yeni meteorlar hemen gelsin"
    Column(
        modifier=modifier.fillMaxWidth(),
        verticalArrangement=Arrangement.spacedBy(4.dp)
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
            modifier=Modifier.fillMaxWidth().weight(1f)
                .clip(RoundedCornerShape(23.dp))
                .border(2.dp,planetColor.copy(alpha=.88f),
                    RoundedCornerShape(23.dp))
                .testTag("space_combat_arena")
                .pointerInput(run.seed,run.stage,paused,run.completed) {
                    detectDragGestures(
                        onDragStart={position->
                            if(!paused&&!run.completed)
                                shipX=(position.x/size.width)
                                    .coerceIn(.06f,.94f)
                        },
                        onDrag={change,_->
                            change.consume()
                            if(!paused&&!run.completed)
                                shipX=(change.position.x/size.width)
                                    .coerceIn(.06f,.94f)
                        },
                        onDragEnd={if(!paused&&!run.completed)
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
            Image(
                painter=painterResource(SpacePremiumArt.background(run.planet)),
                contentDescription=null,
                contentScale=ContentScale.FillBounds,
                modifier=Modifier.matchParentSize()
            )
            Canvas(Modifier.fillMaxSize()){
                val w=size.width
                val h=size.height
                val accent=planetColor
                // Premium WebP world is the scene; keep a subtle HUD-friendly veil.
                drawRect(Brush.verticalGradient(listOf(
                    Color(0x9A051128),Color(0x26020923),Color(0x85060721)
                )))
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
                    val px=w*laser.x
                    val py=h*laser.y
                    val lw=w*.060f
                    val lh=h*.13f
                    drawImage(
                        image=laserSprite,
                        dstOffset=IntOffset((px-lw/2).toInt(),(py-lh/2).toInt()),
                        dstSize=IntSize(lw.toInt().coerceAtLeast(2),lh.toInt().coerceAtLeast(3)),
                        filterQuality=FilterQuality.Medium
                    )
                }
                targetValues.forEach{target->
                    val place=SpaceCombatPhysics.position(
                        target,frame.elapsed,currentTier)
                    val x=w*place.x
                    val y=h*place.y
                    val radius=w*(if(targetValues.size>=5).075f else .092f)
                    if(y < -radius*1.2f || y>h+radius) return@forEach
                    // Glow, silhouette and craters baked into transparent WebP.
                    val sprite=meteorSprites[(run.planet.ordinal+target.id)%meteorSprites.size]
                    val diameter=radius*2.65f
                    drawImage(
                        image=sprite,
                        dstOffset=IntOffset((x-diameter/2).toInt(),(y-diameter/2).toInt()),
                        dstSize=IntSize(diameter.toInt().coerceAtLeast(2),diameter.toInt().coerceAtLeast(2)),
                        filterQuality=FilterQuality.Medium
                    )
                    val display=target.value.toString()
                    paint.color=android.graphics.Color.WHITE
                    paint.setShadowLayer(4f,0f,2f,android.graphics.Color.rgb(8,12,48))
                    paint.textSize=if(display.length>=3)radius*.68f else radius*.95f
                    drawIntoCanvas {it.nativeCanvas.drawText(
                        display,x,y+paint.textSize*.31f,paint)}
                    paint.clearShadowLayer()
                }
                // Explosion feedback is independent of mission state and survives
                // an immediate answer/round transition.
                burst?.let {impact->
                    val t=burstProgress.value.coerceIn(0f,1f)
                    if(t<1f){
                        val cx=w*impact.x
                        val cy=h*impact.y
                        val dia=w*(.10f+t*.42f)
                        drawImage(
                            image=impactSprite,
                            dstOffset=IntOffset((cx-dia/2).toInt(),(cy-dia/2).toInt()),
                            dstSize=IntSize(dia.toInt().coerceAtLeast(2),dia.toInt().coerceAtLeast(2)),
                            alpha=(1f-t)*.95f,
                            filterQuality=FilterQuality.Medium
                        )
                        drawCircle(
                            (if(impact.correct)spaceTeal else Color(0xFFFF9468))
                                .copy(alpha=(1f-t)*.88f),
                            radius=w*(.06f+t*.16f),
                            center=Offset(cx,cy),
                            style=Stroke(width=(1f-t)*6f+1f)
                        )
                    }
                }
                // Animated engines sit behind the high-resolution ship sprite.
                val flicker=.7f+.3f*sin(frame.elapsed*18f)
                for(sign in listOf(-1,1)){
                    val ex=sx+sign*w*.033f
                    drawLine(spaceTeal.copy(alpha=.42f),
                        Offset(ex,sy+h*.025f),
                        Offset(ex,sy+h*(.115f+flicker*.018f)),w*.025f)
                    drawLine(spaceGold.copy(alpha=.92f),
                        Offset(ex,sy+h*.025f),
                        Offset(ex,sy+h*(.082f+flicker*.018f)),w*.011f)
                }
                val shipW=w*.295f
                val shipH=h*.25f
                drawImage(
                    image=shipSprite,
                    dstOffset=IntOffset((sx-shipW/2).toInt(),(sy-shipH*.61f).toInt()),
                    dstSize=IntSize(shipW.toInt().coerceAtLeast(3),shipH.toInt().coerceAtLeast(3)),
                    filterQuality=FilterQuality.Medium
                )
                // Spaceframe perimeter and HUD markings.
                drawLine(accent.copy(alpha=.28f),
                    Offset(w*.06f,h*.07f),
                    Offset(w*.94f,h*.07f),2f)
                drawLine(accent.copy(alpha=.42f),
                    Offset(w*.06f,h*.93f),
                    Offset(w*.94f,h*.93f),2f)
            }
            if(paused && !run.completed){
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
            if(run.completed)Surface(
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
                enabled=!paused&&!run.completed,
                modifier=Modifier.weight(1f).heightIn(min=58.dp)
                    .testTag("space_steer_left"),
                colors=ButtonDefaults.outlinedButtonColors(
                    contentColor=spaceWhite),
                border=BorderStroke(2.dp,spaceTeal)
            ){Text("◀",fontSize=23.sp)}
            Button(
                onClick={fire()},
                enabled=!paused&&!run.completed,
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
                enabled=!paused&&!run.completed,
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
    }
}
