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
    onShipPosition:(Float)->Unit,
    onFireHit:(Int)->Unit,
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
            Text("5 gezegen · 3 sektör · 9 interaktif görev",
                color=GalaxyPaint.bright,fontSize=20.sp,
                fontWeight=FontWeight.Black)
            Text("Gemini serbestçe yönet, lazerle hareketli meteorları vur, reaktörü enerji kapsülleriyle besle ve yıldızları sırayla etkinleştir.",
                color=GalaxyPaint.quiet,fontSize=13.sp)
            if(run!=null && !run.completed) {
                Surface(color=Color(0xFF154D68),
                    shape=RoundedCornerShape(16.dp),
                    border=BorderStroke(1.dp,GalaxyPaint.teal)) {
                    Column(Modifier.padding(13.dp),
                        verticalArrangement=Arrangement.spacedBy(7.dp)) {
                        Text("📍 Devam eden: "+run.planet.title+
                            " · görev "+(run.stage+1)+"/9",
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
                        Text("9 görev · "+run.firstTry+
                            " ilk denemede doğru · "+run.errors+" hata · "+
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
                Text("Sektör "+(run.stage/3+1)+"/3 · Görev "+
                        (run.stage+1)+"/9 · "+run.mission.title,
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
                SpaceCombatArena(
                    run=run,
                    puzzle=q,
                    onPosition=onShipPosition,
                    onHit=onFireHit
                )
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
                    SpaceButton(if(run.stage==SpaceExpeditionEngine.STAGES-1)
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

