package com.yigit033.matematikmacarasi.games.interactive

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private val cloudWhite=Color(0xFFE7F7FF)
private val ink=Color(0xFF132F52)
private val gold=Color(0xFFFFDA7F)
private val balloonTints=listOf(
    Color(0xFFFF758C), Color(0xFF79E3F5),
    Color(0xFFFFC966), Color(0xFFB6A1FF)
)

/**
 * Real-time flight arena. Balloons have independent positions, sway and clickable
 * moving hitboxes; when a whole wave leaves the screen the same question respawns.
 * Progress is only advanced by an explicit tap on the correct moving target.
 */
@Composable
fun BalloonSkyArena(
    run:BalloonRun,
    question:BalloonQuestion,
    onChoose:(Int)->Unit,
    onEscape:()->Unit
) {
    var paused by rememberSaveable(run.seed,run.index) {mutableStateOf(false)}
    var progress by remember(run.seed,run.index,run.wave) {mutableFloatStateOf(0f)}
    val lifeOwner=LocalLifecycleOwner.current
    DisposableEffect(lifeOwner) {
        val observer=LifecycleEventObserver {_,event->
            if(event==Lifecycle.Event.ON_STOP)paused=true
        }
        lifeOwner.lifecycle.addObserver(observer)
        onDispose {lifeOwner.lifecycle.removeObserver(observer)}
    }
    val seconds=if(run.pace==BalloonPace.CHALLENGE)12f else 23f
    LaunchedEffect(run.seed,run.index,run.wave,run.resolved,paused) {
        if(run.resolved || paused)return@LaunchedEffect
        var previous=0L
        while(progress<1f) {
            withFrameNanos {now->
                if(previous>0L) {
                    // Bound a single step after a stalled frame to avoid unfair time jumps.
                    val elapsed=(now-previous).coerceIn(0L,80_000_000L)
                    progress=(progress+elapsed.toFloat()/(seconds*1_000_000_000f))
                        .coerceAtMost(1f)
                }
                previous=now
            }
        }
        onEscape()
    }
    val accent=when(run.realm) {
        BalloonRealm.SUN->Color(0xFFFFCF7D)
        BalloonRealm.CLOUD->Color(0xFFA6E6FF)
        BalloonRealm.WIND->Color(0xFFC5ACFF)
        BalloonRealm.STAR->Color(0xFF9DE8D8)
        BalloonRealm.RAINBOW->Color(0xFFFFAED5)
    }
    Column(verticalArrangement=androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
        Row(
            modifier=Modifier.fillMaxWidth(),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            Text("🎯 UÇAN BALONU YAKALA",
                color=gold,fontSize=12.sp,fontWeight=FontWeight.ExtraBold)
            OutlinedButton(
                onClick={paused=!paused},
                modifier=Modifier.height(41.dp).testTag("balloon_pause"),
                contentPadding=androidx.compose.foundation.layout.PaddingValues(horizontal=11.dp),
                shape=RoundedCornerShape(13.dp),
                border=BorderStroke(1.dp,cloudWhite.copy(alpha=.65f)),
                colors=ButtonDefaults.outlinedButtonColors(contentColor=cloudWhite)
            ) {Text(if(paused)"▶ Devam" else "Ⅱ Durdur",fontSize=12.sp)}
        }
        BoxWithConstraints(
            modifier=Modifier.fillMaxWidth().height(410.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(Color(0xFF2B85B9))
                .border(2.dp,accent.copy(alpha=.65f),RoundedCornerShape(25.dp))
                .testTag("balloon_sky_arena")
        ) {
            val realm=run.realm
            Canvas(Modifier.fillMaxSize()) {
                val w=size.width
                val h=size.height
                val top=when(realm) {
                    BalloonRealm.SUN->Color(0xFF238DC5)
                    BalloonRealm.CLOUD->Color(0xFF327FAE)
                    BalloonRealm.WIND->Color(0xFF5E6EB7)
                    BalloonRealm.STAR->Color(0xFF375F99)
                    BalloonRealm.RAINBOW->Color(0xFF3F8EBB)
                }
                drawRect(Brush.verticalGradient(listOf(
                    top,Color(0xFF56B0D1),Color(0xFF93D9D0)
                )))
                drawCircle(
                    brush=Brush.radialGradient(listOf(
                        gold.copy(alpha=.8f),gold.copy(alpha=0f)
                    ),center=Offset(w*.80f,h*.16f),radius=h*.24f),
                    radius=h*.24f,center=Offset(w*.80f,h*.16f)
                )
                drawCircle(gold.copy(alpha=.93f),h*.065f,Offset(w*.80f,h*.15f))
                for(i in 0..6) {
                    val x=w*((i*.197f+.06f)%1f)
                    val y=h*(.15f+(i%4)*.175f)
                    val sway=sin(progress*PI.toFloat()*2f+i.toFloat())*w*.022f
                    drawOval(cloudWhite.copy(alpha=.36f),
                        topLeft=Offset(x+sway,y),size=Size(w*.22f,h*.045f))
                    drawOval(cloudWhite.copy(alpha=.22f),
                        topLeft=Offset(x+sway+w*.04f,y-h*.025f),
                        size=Size(w*.14f,h*.075f))
                }
                for(i in 0..12){
                    val x=w*((i*0.313f+.14f)%1f)
                    val y=h*((i*0.229f+.07f)%1f)
                    drawCircle(Color.White.copy(alpha=.25f+(i%3)*.12f),
                        radius=1.8f+(i%3)*1.1f,center=Offset(x,y))
                }
                val mountains=Path().apply {
                    moveTo(0f,h*.90f)
                    quadraticBezierTo(w*.15f,h*.76f,w*.32f,h*.91f)
                    quadraticBezierTo(w*.55f,h*.68f,w*.75f,h*.89f)
                    quadraticBezierTo(w*.88f,h*.76f,w,h*.84f)
                    lineTo(w,h);lineTo(0f,h);close()
                }
                drawPath(mountains,Color(0xFF207C87))
                val island=Path().apply {
                    moveTo(-w*.08f,h*.97f)
                    quadraticBezierTo(w*.24f,h*.79f,w*.48f,h*.99f)
                    quadraticBezierTo(w*.77f,h*.87f,w*1.1f,h*.94f)
                    lineTo(w*1.1f,h);lineTo(0f,h);close()
                }
                drawPath(island,Color(0xFF248C88))
                drawLine(gold.copy(alpha=.45f),
                    Offset(0f,h*.065f),Offset(w,h*.065f),2f)
            }
            val arenaWidth=maxWidth.value
            val arenaHeight=maxHeight.value
            val targetWidth=minOf(101f,(arenaWidth-25f)/2f)
            val targetHeight=124f
            val travelX=(arenaWidth-targetWidth).coerceAtLeast(0f)
            val xF=listOf(.035f,.91f,.09f,.87f)
            val yF=listOf(.16f,.19f,.52f,.56f)
            question.choices.forEachIndexed {index,value->
                val wrong=(run.wrongMask and (1 shl index))!=0
                val solved=run.resolved && value==question.answer
                val dx=sin((progress*2.6f+index*1.6f)*PI.toFloat())*8f
                val left=(travelX*xF[index]+dx).coerceIn(0f,travelX)
                val top=arenaHeight*yF[index]-
                    progress*(arenaHeight*.83f+targetHeight*.72f)
                FlyingBalloon(
                    value=value,index=index,wrong=wrong,
                    solved=solved,disabled=run.resolved,
                    modifier=Modifier.offset(x=left.dp,y=top.dp)
                        .size(width=targetWidth.dp,height=targetHeight.dp),
                    onClick={if(!paused)onChoose(value)}
                )
            }
            if(paused && !run.resolved)Surface(
                modifier=Modifier.align(Alignment.Center),
                color=ink.copy(alpha=.92f),
                shape=RoundedCornerShape(17.dp),
                border=BorderStroke(1.dp,gold)
            ) {
                Text("⏸ Oyun duraklatıldı",
                    modifier=Modifier.padding(15.dp),
                    fontSize=15.sp,fontWeight=FontWeight.Bold,color=cloudWhite)
            }
            Surface(
                modifier=Modifier.align(Alignment.BottomCenter)
                    .padding(bottom=9.dp),
                color=ink.copy(alpha=.79f),
                shape=RoundedCornerShape(13.dp)
            ) {
                Text(
                    if(run.resolved)"✨ Doğru balon yakalandı!"
                    else if(paused)"Dokunarak devam et"
                    else if(run.pace==BalloonPace.CHALLENGE)
                        "⚡ Dalgayı kaçırma! "+run.escapes+" kaçan"
                    else "🪁 Balonlar yükseliyor • kaçırırsan yeniden gelir",
                    modifier=Modifier.padding(horizontal=12.dp,vertical=8.dp),
                    color=cloudWhite,fontSize=11.sp,fontWeight=FontWeight.Medium,
                    textAlign=TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FlyingBalloon(
    value:Int,
    index:Int,
    wrong:Boolean,
    solved:Boolean,
    disabled:Boolean,
    modifier:Modifier,
    onClick:()->Unit
) {
    val visible=!wrong && !solved
    val pop by animateFloatAsState(
        targetValue=if(wrong || solved)1f else 0f,
        animationSpec=tween(430),label="balloon-burst"
    )
    val color=balloonTints[index%balloonTints.size]
    Surface(
        modifier=modifier
            .testTag("balloon_choice_"+value)
            .semantics {contentDescription="Uçan balon, "+value},
        onClick=onClick,
        enabled=!wrong && !disabled,
        color=Color.Transparent,
        shape=RoundedCornerShape(42.dp)
    ) {
        Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                val w=size.width;val h=size.height
                val cx=w*.5f;val cy=h*.40f
                if(visible) {
                    drawLine(Color.White.copy(alpha=.85f),
                        Offset(cx,h*.72f),Offset(cx+w*.05f,h*.98f),
                        strokeWidth=2.4f)
                    val knot=Path().apply {
                        moveTo(cx,h*.70f);lineTo(cx-w*.075f,h*.77f)
                        lineTo(cx+w*.075f,h*.77f);close()
                    }
                    drawPath(knot,color)
                    drawOval(Brush.linearGradient(listOf(
                        color.copy(alpha=.84f),color,color.copy(alpha=.86f)
                    )),Offset(w*.08f,h*.025f),Size(w*.84f,h*.69f))
                    drawOval(Color.White.copy(alpha=.48f),
                        Offset(w*.22f,h*.12f),Size(w*.15f,h*.21f))
                    drawOval(ink.copy(alpha=.12f),
                        Offset(w*.60f,h*.22f),Size(w*.14f,h*.36f))
                } else if(pop<1f) {
                    for(i in 0..11) {
                        val angle=i*2.0*PI/12.0
                        val radius=w*(.10f+pop*.52f)
                        val x=cx+cos(angle).toFloat()*radius
                        val y=cy+sin(angle).toFloat()*radius
                        drawCircle(
                            if(solved)gold.copy(alpha=1f-pop)
                                else color.copy(alpha=1f-pop),
                            radius=4f*(1f-pop)+1f,center=Offset(x,y)
                        )
                    }
                    drawCircle(if(solved)gold.copy(alpha=(1f-pop)*.7f)
                        else Color.White.copy(alpha=(1f-pop)*.7f),
                        radius=w*.36f+pop*w*.18f,
                        center=Offset(cx,cy),style=Stroke(3f))
                }
            }
            if(visible)Text(
                value.toString(),
                modifier=Modifier.offset(y=(-13).dp),
                color=ink,fontSize=27.sp,fontWeight=FontWeight.Black
            )
            if(solved)Text("✓",color=gold,
                fontSize=38.sp,fontWeight=FontWeight.Black)
        }
    }
}
