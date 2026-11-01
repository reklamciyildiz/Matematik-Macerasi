package com.yigit033.matematikmacarasi.games.interactive

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import kotlin.math.PI
import kotlin.math.sin

private object GalaxyPaint {
    val night=Color(0xFF070D2C)
    val dark=Color(0xFF101A46)
    val panel=Color(0xFF18295B)
    val bright=Color(0xFFEAF7FF)
    val quiet=Color(0xFFAED0EE)
    val gold=Color(0xFFFFD775)
    val teal=Color(0xFF69E9DC)
    val planets=listOf(
        Color(0xFF68E6AC),Color(0xFF7ACFEF),Color(0xFFFD8B6B),
        Color(0xFFC4A6FF),Color(0xFFFD9ED1)
    )
}
@Composable
private fun SpaceButton(text:String,enabled:Boolean=true,onClick:()->Unit) {
    Button(
        onClick=onClick,enabled=enabled,
        modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),
        colors=ButtonDefaults.buttonColors(
            containerColor=GalaxyPaint.gold,contentColor=GalaxyPaint.night),
        shape=RoundedCornerShape(17.dp)
    ) {Text(text,fontSize=15.sp,fontWeight=FontWeight.ExtraBold)}
}
@Composable
private fun GalaxyScene(planet:SpacePlanet,compact:Boolean) {
    val tint=GalaxyPaint.planets[planet.ordinal]
    Box(
        modifier=Modifier.fillMaxWidth()
            .height(if(compact)95.dp else 185.dp)
            .clip(RoundedCornerShape(22.dp)),
        contentAlignment=Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w=size.width;val h=size.height
            drawRect(Brush.verticalGradient(listOf(
                Color(0xFF101344),Color(0xFF20366F),GalaxyPaint.night
            )))
            repeat(48){i->
                val x=w*((i*37%101)/101f)
                val y=h*((i*61%97)/97f)
                drawCircle(
                    Color.White.copy(alpha=.25f+(i%4)*.17f),
                    radius=1.2f+(i%3)*.85f,center=Offset(x,y)
                )
            }
            val center=Offset(w*.72f,h*.50f)
            val radius=h*(if(compact).55f else .43f)
            drawCircle(tint.copy(alpha=.16f),radius*1.34f,center)
            drawCircle(Brush.radialGradient(listOf(
                tint.copy(alpha=.95f),tint.copy(alpha=.72f),
                Color(0xFF1E416D)),center,radius),radius,center)
            drawOval(
                color=GalaxyPaint.bright.copy(alpha=.30f),
                topLeft=Offset(center.x-radius*1.27f,center.y-radius*.31f),
                size=Size(radius*2.55f,radius*.65f),
                style=Stroke(width=3.2f)
            )
            drawCircle(GalaxyPaint.bright.copy(alpha=.24f),
                radius=radius*.14f,
                center=Offset(center.x-radius*.35f,center.y-radius*.35f))
            val ship=Path().apply {
                moveTo(w*.20f,h*.34f);lineTo(w*.32f,h*.56f)
                lineTo(w*.22f,h*.54f);lineTo(w*.14f,h*.69f)
                lineTo(w*.11f,h*.49f);close()
            }
            drawPath(ship,GalaxyPaint.bright)
            drawLine(GalaxyPaint.teal,Offset(w*.17f,h*.60f),
                Offset(w*.11f,h*.88f),7f)
            drawCircle(GalaxyPaint.gold,6f,Offset(w*.20f,h*.45f))
        }
        Surface(
            modifier=Modifier.align(Alignment.BottomStart)
                .padding(10.dp),
            shape=RoundedCornerShape(12.dp),
            color=GalaxyPaint.night.copy(alpha=.82f)
        ){
            Text(planet.title+" · "+planet.sign,
                color=GalaxyPaint.bright,fontSize=13.sp,
                fontWeight=FontWeight.Black,
                modifier=Modifier.padding(horizontal=12.dp,vertical=8.dp))
        }
    }
}
@Composable
fun SpaceExpeditionScreen(
    run:SpaceRun?,
    recommended:LearningTier,
    records:Map<SpacePlanet,SpacePlanetRecord>,
    historicalStars:Map<SpacePlanet,Int>,
    feedback:String,
    onBegin:(SpacePlanet,LearningTier)->Boolean,
    onSteer:(Int)->Unit,
    onIntercept:()->Unit,
    onFuel:(Int)->Unit,
    onIgnite:()->Unit,
    onConnect:(Int)->Unit,
    onHint:()->Unit,
    onNext:()->Unit,
    onExit:()->Unit
) {
    var lobby by rememberSaveable {mutableStateOf(true)}
    var choice by rememberSaveable {mutableIntStateOf(0)}
    var selectedTier by rememberSaveable {mutableStateOf(recommended.name)}
    var replaceWith by remember {mutableStateOf<Pair<SpacePlanet,LearningTier>?>(null)}
    var startFailed by remember {mutableStateOf(false)}
    val tier=LearningTier.fromSaved(selectedTier,recommended)
    fun launch(planet:SpacePlanet,t:LearningTier) {
        if(run!=null && !run.completed)replaceWith=planet to t
        else if(onBegin(planet,t)){lobby=false;startFailed=false}
        else startFailed=true
    }
    BackHandler(enabled=!lobby){lobby=true}
    replaceWith?.let {pending->
        AlertDialog(
            onDismissRequest={replaceWith=null},
            title={Text("Yeni keşfe geçilsin mi?")},
            text={Text("Yarım kalan görevin kaydı yenisiyle değişecek.")},
            confirmButton={TextButton(onClick={
                replaceWith=null
                if(onBegin(pending.first,pending.second)){lobby=false;startFailed=false}
                else startFailed=true
            }){Text("Yeni keşif")}},
            dismissButton={TextButton(onClick={replaceWith=null}){Text("Vazgeç")}}
        )
    }
    Column(
        modifier=Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(
                Color(0xFF12347D),GalaxyPaint.night,Color(0xFF03091B)
            )))
            .safeDrawingPadding().verticalScroll(rememberScrollState())
            .padding(horizontal=14.dp,vertical=10.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)
    ) {
        Row(Modifier.fillMaxWidth(),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(11.dp)) {
            OutlinedButton(
                onClick={if(lobby)onExit() else lobby=true},
                modifier=Modifier.height(48.dp),
                contentPadding=PaddingValues(horizontal=11.dp),
                border=BorderStroke(1.dp,GalaxyPaint.teal),
                colors=ButtonDefaults.outlinedButtonColors(contentColor=GalaxyPaint.bright)
            ){Text(if(lobby)"← Oyunlar" else "← Galaksi",fontSize=12.sp)}
            Column(Modifier.weight(1f)) {
                Text("GALAKSİ KAŞİFLERİ",
                    fontSize=18.sp,fontWeight=FontWeight.Black,color=GalaxyPaint.gold)
                Text("Uzay Matematik Macerası 2.0",
                    fontSize=11.sp,color=GalaxyPaint.quiet)
            }
            Text("🚀",fontSize=26.sp)
        }
        if(lobby) {
            GalaxyScene(SpacePlanet.entries[choice],compact=false)
            Text("5 gezegen · 3 etkileşimli görev",
                color=GalaxyPaint.bright,fontSize=20.sp,
                fontWeight=FontWeight.Black)
            Text("Gemini yönlendir, reaktörü çalıştır, yıldızlardan devre kur. Soruları işaretlemek yerine matematiği kullan.",
                color=GalaxyPaint.quiet,fontSize=13.sp)
            if(run!=null && !run.completed) {
                Surface(color=Color(0xFF154D68),
                    shape=RoundedCornerShape(16.dp),
                    border=BorderStroke(1.dp,GalaxyPaint.teal)) {
                    Column(Modifier.padding(13.dp),
                        verticalArrangement=Arrangement.spacedBy(7.dp)) {
                        Text("📍 Devam eden: "+run.planet.title+
                            " · görev "+(run.stage+1)+"/3",
                            color=GalaxyPaint.bright,fontWeight=FontWeight.Bold)
                        SpaceButton("▶ Kaydedilen Göreve Dön") {lobby=false}
                    }
                }
            }
            Text("Öğrenme seviyesi",color=GalaxyPaint.gold,
                fontSize=16.sp,fontWeight=FontWeight.Bold)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                LearningTier.entries.forEach {option->
                    val selected=option==tier
                    OutlinedButton(
                        onClick={selectedTier=option.name},
                        modifier=Modifier.weight(1f).heightIn(min=48.dp),
                        contentPadding=PaddingValues(horizontal=3.dp),
                        border=BorderStroke(if(selected)2.dp else 1.dp,
                            if(selected)GalaxyPaint.gold else GalaxyPaint.quiet),
                        colors=ButtonDefaults.outlinedButtonColors(
                            containerColor=if(selected)GalaxyPaint.panel else Color.Transparent,
                            contentColor=GalaxyPaint.bright)
                    ){Text(option.title,fontSize=12.sp)}
                }
            }
            Text("Önerilen: "+recommended.title+" · İstersen değiştirebilirsin.",
                color=GalaxyPaint.quiet,fontSize=11.sp)
            SpacePlanet.entries.forEach {planet->
                val chosen=planet.ordinal==choice
                val best=maxOf(records[planet]?.bestStars?:0,historicalStars[planet]?:0)
                Surface(
                    modifier=Modifier.fillMaxWidth()
                        .testTag("space_planet_"+planet.name),
                    onClick={choice=planet.ordinal},
                    color=if(chosen)Color(0xFF28597B) else GalaxyPaint.panel,
                    shape=RoundedCornerShape(15.dp),
                    border=BorderStroke(if(chosen)2.dp else 1.dp,
                        if(chosen)GalaxyPaint.gold else
                            GalaxyPaint.planets[planet.ordinal].copy(alpha=.5f))
                ){
                    Row(Modifier.padding(14.dp),
                        verticalAlignment=Alignment.CenterVertically,
                        horizontalArrangement=Arrangement.spacedBy(13.dp)){
                        Text(planet.sign,fontSize=28.sp,
                            color=GalaxyPaint.planets[planet.ordinal])
                        Column(Modifier.weight(1f)){
                            Text(planet.title,fontWeight=FontWeight.Black,
                                fontSize=15.sp,color=GalaxyPaint.bright)
                            Text(planet.detail,fontSize=11.sp,
                                color=GalaxyPaint.quiet)
                        }
                        Text("★".repeat(best)+"☆".repeat(3-best),
                            color=GalaxyPaint.gold,fontSize=15.sp)
                    }
                }
            }
            SpaceButton("🚀 "+SpacePlanet.entries[choice].title+" Keşfini Başlat") {
                launch(SpacePlanet.entries[choice],tier)
            }
            if(startFailed)Text(if(feedback.isBlank())
                "Kaydedilemedi. Tekrar dene." else feedback,
                color=GalaxyPaint.gold)
            Text("Çevrimdışı · reklamsız görev alanı · kaldığın yerden devam",
                color=GalaxyPaint.quiet,fontSize=11.sp)
        } else if(run!=null) {
            if(run.completed){
                GalaxyScene(run.planet,compact=false)
                Surface(color=GalaxyPaint.panel,
                    shape=RoundedCornerShape(24.dp),
                    border=BorderStroke(2.dp,GalaxyPaint.gold)){
                    Column(Modifier.fillMaxWidth().padding(18.dp),
                        horizontalAlignment=Alignment.CenterHorizontally,
                        verticalArrangement=Arrangement.spacedBy(13.dp)){
                        Text("🏆 GÖREV TAMAMLANDI",
                            color=GalaxyPaint.gold,fontSize=23.sp,
                            fontWeight=FontWeight.Black)
                        Text("★".repeat(SpaceExpeditionEngine.stars(run))+
                            "☆".repeat(3-SpaceExpeditionEngine.stars(run)),
                            color=GalaxyPaint.gold,fontSize=35.sp)
                        Text(SpaceExpeditionEngine.score(run).toString()+" puan",
                            color=GalaxyPaint.bright,fontSize=26.sp,
                            fontWeight=FontWeight.Black)
                        Text(run.firstTry.toString()+
                            " bağımsız görev · "+run.errors+" hata · "+
                            run.hints+" ipucu",
                            color=GalaxyPaint.quiet,fontSize=13.sp)
                        Text("Gemiyi kullandın, enerji ürettin ve yıldız devresini tamamladın.",
                            color=GalaxyPaint.bright,fontSize=13.sp,
                            textAlign=TextAlign.Center)
                    }
                }
                SpaceButton("🚀 Bu Gezegeni Yeniden Keşfet"){
                    launch(run.planet,run.tier)
                }
                OutlinedButton(onClick={lobby=true},
                    modifier=Modifier.fillMaxWidth().heightIn(min=50.dp),
                    border=BorderStroke(1.dp,GalaxyPaint.teal),
                    colors=ButtonDefaults.outlinedButtonColors(
                        contentColor=GalaxyPaint.bright)) {Text("🪐 Gezegen haritası")}
            } else {
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement=Arrangement.SpaceBetween,
                    verticalAlignment=Alignment.CenterVertically){
                    Text(run.planet.title+" · "+run.tier.title,
                        color=GalaxyPaint.teal,fontSize=14.sp,
                        fontWeight=FontWeight.Bold)
                    Text("🛡 "+run.shields+"  ★ "+run.firstTry,
                        color=GalaxyPaint.gold,fontSize=13.sp)
                }
                LinearProgressIndicator(
                    progress={run.stage.toFloat()/SpaceExpeditionEngine.STAGES},
                    modifier=Modifier.fillMaxWidth().height(8.dp),
                    color=GalaxyPaint.gold,trackColor=Color(0xFF325079)
                )
                Text("Görev "+(run.stage+1)+"/3 · "+run.mission.title,
                    color=GalaxyPaint.gold,fontSize=16.sp,
                    fontWeight=FontWeight.Black)
                val q=SpaceExpeditionEngine.puzzle(run)
                Surface(color=GalaxyPaint.panel,
                    shape=RoundedCornerShape(15.dp)){
                    Text(q.prompt,
                        modifier=Modifier.fillMaxWidth().padding(14.dp)
                            .testTag("space_mission_prompt"),
                        color=GalaxyPaint.bright,
                        fontSize=16.sp,fontWeight=FontWeight.Bold)
                }
                when(run.mission) {
                    SpaceMission.FLIGHT->SpaceFlightArena(
                        run=run,puzzle=q,onSteer=onSteer,onIntercept=onIntercept
                    )
                    SpaceMission.REACTOR->SpaceReactor(
                        run=run,puzzle=q,onFuel=onFuel,onIgnite=onIgnite
                    )
                    SpaceMission.CIRCUIT->SpaceCircuit(
                        run=run,puzzle=q,onConnect=onConnect
                    )
                }
                if(run.solved){
                    Surface(
                        modifier=Modifier.fillMaxWidth(),
                        shape=RoundedCornerShape(16.dp),
                        color=Color(0xFF14556E),
                        border=BorderStroke(1.dp,GalaxyPaint.teal)
                    ){
                        Column(Modifier.padding(14.dp),
                            verticalArrangement=Arrangement.spacedBy(7.dp)){
                            Text("✨ Görev başarılı!",
                                color=GalaxyPaint.gold,
                                fontSize=17.sp,fontWeight=FontWeight.Bold)
                            Text(q.explanation,color=GalaxyPaint.bright,fontSize=13.sp)
                        }
                    }
                    SpaceButton(if(run.stage==2)
                        "🏆 Gezegen Sonucunu Gör" else "Sonraki Görev →",
                        onClick=onNext)
                } else {
                    if(run.hintVisible)Text("💡 "+q.hint,
                        color=GalaxyPaint.teal,fontSize=13.sp,
                        modifier=Modifier.fillMaxWidth()
                            .background(GalaxyPaint.panel,RoundedCornerShape(12.dp))
                            .padding(12.dp))
                    OutlinedButton(
                        onClick=onHint,enabled=!run.hintVisible,
                        modifier=Modifier.fillMaxWidth().heightIn(min=51.dp),
                        border=BorderStroke(1.dp,GalaxyPaint.teal),
                        colors=ButtonDefaults.outlinedButtonColors(
                            contentColor=GalaxyPaint.bright)
                    ){Text(if(run.hintVisible)"💡 İpucu açık" else "💡 Yardımcı ipucu")}
                }
                if(feedback.isNotBlank())Text(feedback,color=GalaxyPaint.gold,
                    fontSize=12.sp,fontWeight=FontWeight.Bold)
                Text("Hatalar puanı etkiler, ancak çocuk oyundan elenmez.",
                    color=GalaxyPaint.quiet,fontSize=11.sp)
                TextButton(
                    modifier=Modifier.fillMaxWidth(),
                    onClick={lobby=true}
                ){Text("💾 Haritaya dön · ilerlemeyi koru",
                    color=GalaxyPaint.teal,fontSize=12.sp)}
            }
        }
        Spacer(Modifier.height(9.dp))
    }
}

@Composable
private fun SpaceFlightArena(
    run:SpaceRun,puzzle:SpacePuzzle,
    onSteer:(Int)->Unit,onIntercept:()->Unit
) {
    var paused by rememberSaveable(run.seed,run.stage){mutableStateOf(false)}
    var progress by remember(run.seed,run.wave,run.stage){mutableFloatStateOf(0f)}
    val owner=LocalLifecycleOwner.current
    DisposableEffect(owner){
        val listener=LifecycleEventObserver {_,event->
            if(event==Lifecycle.Event.ON_STOP)paused=true
        }
        owner.lifecycle.addObserver(listener)
        onDispose{owner.lifecycle.removeObserver(listener)}
    }
    val currentIntercept by rememberUpdatedState(onIntercept)
    LaunchedEffect(run.seed,run.wave,run.stage,run.solved,paused) {
        if(run.solved || paused)return@LaunchedEffect
        val duration=when(run.tier){
            LearningTier.TEMEL->18f
            LearningTier.GELISEN->15f
            LearningTier.USTALIK->12f
        }
        var last=0L
        while(progress<.80f){
            withFrameNanos {now->
                if(last>0L) {
                    val elapsed=(now-last).coerceIn(0L,80_000_000L)
                    progress=(progress+elapsed/(duration*1_000_000_000f))
                        .coerceAtMost(.80f)
                }
                last=now
            }
        }
        currentIntercept()
    }
    val planetColor=GalaxyPaint.planets[run.planet.ordinal]
    Surface(
        shape=RoundedCornerShape(23.dp),
        color=GalaxyPaint.night,
        border=BorderStroke(2.dp,planetColor.copy(alpha=.75f))
    ){
        Column(Modifier.fillMaxWidth().padding(9.dp),
            verticalArrangement=Arrangement.spacedBy(8.dp)){
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.SpaceBetween,
                verticalAlignment=Alignment.CenterVertically){
                Text("🛰 Şerit değiştir · doğru kristali yakala",
                    color=GalaxyPaint.bright,fontSize=11.sp,
                    fontWeight=FontWeight.Bold)
                OutlinedButton(
                    modifier=Modifier.height(38.dp).testTag("space_pause"),
                    contentPadding=PaddingValues(horizontal=10.dp),
                    onClick={paused=!paused},
                    border=BorderStroke(1.dp,GalaxyPaint.teal),
                    colors=ButtonDefaults.outlinedButtonColors(
                        contentColor=GalaxyPaint.bright)
                ){Text(if(paused)"▶ Devam" else "Ⅱ Durdur",fontSize=11.sp)}
            }
            Box(
                Modifier.fillMaxWidth().height(302.dp)
                    .clip(RoundedCornerShape(17.dp))
                    .pointerInput(run.seed,run.stage,run.solved,paused) {
                        detectDragGestures(
                            onDragStart={point->
                                if(!paused && !run.solved)onSteer(
                                    (point.x/size.width*3f).toInt().coerceIn(0,2))
                            },
                            onDrag={change,_->
                                change.consume()
                                if(!paused && !run.solved)onSteer(
                                    (change.position.x/size.width*3f).toInt()
                                        .coerceIn(0,2))
                            }
                        )
                    }
                    .testTag("space_flight_arena")
            ){
                Canvas(Modifier.fillMaxSize()){
                    val w=size.width;val h=size.height
                    drawRect(Brush.verticalGradient(listOf(
                        Color(0xFF13205A),Color(0xFF254E89),Color(0xFF071631))))
                    repeat(62){i->
                        val x=w*((i*73%101)/101f)
                        val y=h*((i*41%97)/97f)
                        drawCircle(
                            Color.White.copy(alpha=.15f+(i%5)*.13f),
                            radius=1.2f+(i%3)*.8f,
                            center=Offset(x,y))
                    }
                    for(lane in 1..2)drawLine(
                        Color.White.copy(alpha=.2f),
                        Offset(w*lane/3f,0f),Offset(w*lane/3f,h),
                        2f
                    )
                    val shipX=w*(run.lane+.5f)/3f
                    val shipY=h*.80f
                    val jet=Path().apply{
                        moveTo(shipX,shipY-h*.11f)
                        lineTo(shipX-w*.092f,shipY+h*.075f)
                        lineTo(shipX,shipY+h*.025f)
                        lineTo(shipX+w*.092f,shipY+h*.075f)
                        close()
                    }
                    drawPath(jet,GalaxyPaint.bright)
                    drawCircle(planetColor,7f,Offset(shipX,shipY-h*.018f))
                    drawLine(GalaxyPaint.teal,
                        Offset(shipX-w*.023f,shipY+h*.055f),
                        Offset(shipX-w*.025f,shipY+h*.14f),7f)
                    drawLine(GalaxyPaint.gold,
                        Offset(shipX+w*.025f,shipY+h*.055f),
                        Offset(shipX+w*.025f,shipY+h*.12f),5f)
                    val y=-h*.11f+progress*h*1.14f
                    puzzle.crystals.forEachIndexed {index,value->
                        val center=Offset(w*(index+.5f)/3f,y)
                        drawCircle(
                            color=GalaxyPaint.planets[(index+run.planet.ordinal)%5]
                                .copy(alpha=.32f),
                            radius=w*.13f,center=center
                        )
                        val crystal=Path().apply{
                            moveTo(center.x,center.y-h*.066f)
                            lineTo(center.x+w*.055f,center.y)
                            lineTo(center.x,center.y+h*.066f)
                            lineTo(center.x-w*.055f,center.y)
                            close()
                        }
                        drawPath(crystal,Brush.linearGradient(listOf(
                            GalaxyPaint.gold,GalaxyPaint.teal)))
                        drawPath(crystal,GalaxyPaint.bright,style=Stroke(2f))
                    }
                    if(run.solved)drawCircle(
                        GalaxyPaint.gold.copy(alpha=.24f),
                        radius=w*.19f,center=Offset(shipX,shipY))
                }
                Row(Modifier.fillMaxWidth()
                    .offset(y=(-12).dp),
                    horizontalArrangement=Arrangement.SpaceEvenly) {
                    puzzle.crystals.forEachIndexed {index,value->
                        // Numeric labels follow the moving crystal vertically.
                        // This stationary row is intentionally empty: real labels
                        // are drawn as an overlay below at the crystal position.
                        Spacer(Modifier.weight(1f))
                    }
                }
                Row(
                    modifier=Modifier.fillMaxWidth()
                        .offset(y=(((-.11f+progress*1.14f)*302f)-19f).dp),
                    horizontalArrangement=Arrangement.SpaceEvenly
                ) {
                    puzzle.crystals.forEach {value->
                        Box(Modifier.weight(1f).height(48.dp),
                            contentAlignment=Alignment.Center){
                            Text(value.toString(),
                                color=GalaxyPaint.night,fontSize=14.sp,
                                fontWeight=FontWeight.Black,
                                modifier=Modifier.background(
                                    GalaxyPaint.gold.copy(alpha=.95f),
                                    RoundedCornerShape(9.dp))
                                    .padding(horizontal=7.dp,vertical=3.dp))
                        }
                    }
                }
                if(paused && !run.solved)Surface(
                    modifier=Modifier.align(Alignment.Center),
                    color=GalaxyPaint.night.copy(alpha=.94f),
                    shape=RoundedCornerShape(16.dp)
                ){
                    Text("⏸ Uçuş duraklatıldı",
                        color=GalaxyPaint.bright,
                        modifier=Modifier.padding(14.dp),
                        fontWeight=FontWeight.Bold)
                }
            }
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                (0..2).forEach{lane->
                    OutlinedButton(
                        onClick={if(!paused && !run.solved)onSteer(lane)},
                        modifier=Modifier.weight(1f).heightIn(min=54.dp)
                            .testTag("space_lane_"+lane),
                        border=BorderStroke(if(run.lane==lane)2.dp else 1.dp,
                            if(run.lane==lane)GalaxyPaint.gold else GalaxyPaint.teal),
                        colors=ButtonDefaults.outlinedButtonColors(
                            containerColor=if(run.lane==lane)GalaxyPaint.panel
                                else Color.Transparent,
                            contentColor=GalaxyPaint.bright)
                    ){Text(when(lane){0->"◀ Sol";1->"● Orta";else->"Sağ ▶"},
                        fontSize=12.sp,fontWeight=FontWeight.Bold)}
                }
            }
            Text("Kristaller gemiye ulaşınca seçili şeritteki kristal otomatik toplanır. Yanlış kristal kalkanı zayıflatır; aynı görev tekrar gelir.",
                fontSize=11.sp,color=GalaxyPaint.quiet)
        }
    }
}

@Composable
private fun SpaceReactor(
    run:SpaceRun,puzzle:SpacePuzzle,
    onFuel:(Int)->Unit,onIgnite:()->Unit
) {
    val target=puzzle.answer
    Surface(color=GalaxyPaint.panel,
        shape=RoundedCornerShape(23.dp),
        border=BorderStroke(1.dp,GalaxyPaint.teal)
    ){
        Column(Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.spacedBy(13.dp)){
            Text("⚡ ENERJİ ÇEKİRDEĞİ",
                color=GalaxyPaint.gold,fontSize=17.sp,fontWeight=FontWeight.Black)
            Box(Modifier.fillMaxWidth().height(155.dp),
                contentAlignment=Alignment.Center) {
                Canvas(Modifier.fillMaxSize()){
                    val w=size.width;val h=size.height
                    val center=Offset(w*.5f,h*.50f)
                    val radius=h*.43f
                    drawCircle(GalaxyPaint.teal.copy(alpha=.13f),radius*1.27f,center)
                    drawCircle(GalaxyPaint.quiet.copy(alpha=.34f),radius,
                        center,style=Stroke(8f))
                    drawArc(GalaxyPaint.gold,-90f,
                        (360f*run.charge/target.coerceAtLeast(1)).coerceIn(0f,360f),
                        false,
                        topLeft=Offset(center.x-radius,center.y-radius),
                        size=Size(radius*2,radius*2),
                        style=Stroke(12f))
                    drawCircle(
                        Brush.radialGradient(listOf(
                            Color(0xFF4DE0D7),Color(0xFF104A70)
                        ),center,radius*.84f),
                        radius*.78f,center=center)
                }
                Column(horizontalAlignment=Alignment.CenterHorizontally) {
                    Text(run.charge.toString(),
                        fontSize=35.sp,fontWeight=FontWeight.Black,
                        color=GalaxyPaint.bright,
                        modifier=Modifier.testTag("space_charge"))
                    Text("enerji birimi",fontSize=11.sp,color=GalaxyPaint.bright)
                }
            }
            Text("Pompayı çalıştır. İşlemin sonucuyla aynı miktara ulaş.",
                color=GalaxyPaint.quiet,fontSize=12.sp,
                textAlign=TextAlign.Center)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                listOf(-5,-1,1,5).forEach{delta->
                    OutlinedButton(onClick={onFuel(delta)},
                        enabled=!run.solved,
                        modifier=Modifier.weight(1f).heightIn(min=58.dp)
                            .testTag("space_fuel_"+delta),
                        contentPadding=PaddingValues(horizontal=3.dp),
                        border=BorderStroke(1.dp,GalaxyPaint.teal),
                        colors=ButtonDefaults.outlinedButtonColors(
                            contentColor=GalaxyPaint.bright)){
                        Text(if(delta>0)"+$delta" else "$delta",
                            fontSize=15.sp,fontWeight=FontWeight.Black)
                    }
                }
            }
            SpaceButton("🔋 Reaktörü Ateşle",onClick=onIgnite,
                enabled=!run.solved)
        }
    }
}

@Composable
private fun SpaceCircuit(
    run:SpaceRun,puzzle:SpacePuzzle,
    onConnect:(Int)->Unit
) {
    Surface(color=GalaxyPaint.panel,
        shape=RoundedCornerShape(23.dp),
        border=BorderStroke(1.dp,GalaxyPaint.planets[run.planet.ordinal])
    ){
        Column(Modifier.fillMaxWidth().padding(15.dp),
            verticalArrangement=Arrangement.spacedBy(15.dp),
            horizontalAlignment=Alignment.CenterHorizontally) {
            Text("✧ YILDIZ DEVRESİ",fontSize=18.sp,
                fontWeight=FontWeight.Black,color=GalaxyPaint.gold)
            Text("Sıradaki sayıyı bul, yıldızlara dokunarak bağlantıyı kur.",
                color=GalaxyPaint.quiet,fontSize=12.sp,textAlign=TextAlign.Center)
            Canvas(Modifier.fillMaxWidth().height(135.dp)){
                val w=size.width;val h=size.height
                val centers=listOf(Offset(w*.12f,h*.25f),
                    Offset(w*.36f,h*.72f),Offset(w*.65f,h*.26f),
                    Offset(w*.89f,h*.66f))
                centers.forEachIndexed {index,p->
                    drawCircle(GalaxyPaint.teal.copy(alpha=.18f),
                        radius=25f,center=p)
                    drawCircle(GalaxyPaint.gold,
                        radius=7f,center=p)
                    if(index>0)drawLine(
                        if(index-1<run.path.size)GalaxyPaint.gold
                            else GalaxyPaint.quiet.copy(alpha=.35f),
                        centers[index-1],p,
                        strokeWidth=if(index-1<run.path.size)5f else 2f)
                }
            }
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.spacedBy(8.dp)){
                puzzle.nodes.take(2).forEach {n->
                    StarNode(n,run.path.contains(n),run.solved,
                        Modifier.weight(1f)){onConnect(n)}
                }
            }
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.spacedBy(8.dp)){
                puzzle.nodes.drop(2).forEach {n->
                    StarNode(n,run.path.contains(n),run.solved,
                        Modifier.weight(1f)){onConnect(n)}
                }
            }
            Text(if(run.solved)"✓ Devre tamamlandı!"
                else "Bağlanan yıldızlar: "+run.path.size+"/3",
                color=GalaxyPaint.teal,fontSize=13.sp,
                fontWeight=FontWeight.Bold,
                modifier=Modifier.testTag("space_circuit_progress"))
        }
    }
}
@Composable
private fun StarNode(n:Int,connected:Boolean,finished:Boolean,
    modifier:Modifier,onClick:()->Unit){
    Surface(
        modifier=modifier.heightIn(min=86.dp)
            .testTag("space_node_"+n),
        onClick=onClick,
        enabled=!connected && !finished,
        color=if(connected)Color(0xFF176E71) else Color(0xFF263968),
        border=BorderStroke(if(connected)2.dp else 1.dp,
            if(connected)GalaxyPaint.gold else GalaxyPaint.teal),
        shape=RoundedCornerShape(19.dp)){
        Column(Modifier.padding(8.dp),
            horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.Center) {
            Text(if(connected)"✦" else "✧",fontSize=21.sp,
                color=GalaxyPaint.gold)
            Text(n.toString(),fontSize=19.sp,fontWeight=FontWeight.Black,
                color=GalaxyPaint.bright)
        }
    }
}
