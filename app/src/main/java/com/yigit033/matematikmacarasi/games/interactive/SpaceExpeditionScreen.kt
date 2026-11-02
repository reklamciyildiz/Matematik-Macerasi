package com.yigit033.matematikmacarasi.games.interactive

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import com.yigit033.matematikmacarasi.R
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
private fun GalaxyScene(planet:SpacePlanet,compact:Boolean){
    Box(
        modifier=Modifier.fillMaxWidth()
            .height(if(compact)95.dp else 186.dp)
            .clip(RoundedCornerShape(22.dp)),
        contentAlignment=Alignment.Center
    ){
        Image(
            painter=painterResource(SpacePremiumArt.background(planet)),
            contentDescription=null,
            contentScale=ContentScale.FillBounds,
            modifier=Modifier.matchParentSize()
        )
        Box(
            Modifier.matchParentSize().background(
                Brush.horizontalGradient(listOf(
                    GalaxyPaint.night.copy(alpha=.70f),
                    Color.Transparent,
                    GalaxyPaint.night.copy(alpha=.08f)
                ))
            )
        )
        Image(
            painter=painterResource(R.drawable.space_ship_premium),
            contentDescription=null,
            modifier=Modifier.align(Alignment.CenterStart).padding(start=23.dp)
                .size(if(compact)65.dp else 110.dp)
        )
        Surface(
            modifier=Modifier.align(Alignment.BottomStart).padding(10.dp),
            shape=RoundedCornerShape(12.dp),
            color=GalaxyPaint.night.copy(alpha=.83f),
            border=BorderStroke(1.dp,GalaxyPaint.planets[planet.ordinal].copy(alpha=.5f))
        ){
            Text(planet.title+" · "+planet.sign,
                color=GalaxyPaint.bright,fontSize=14.sp,
                fontWeight=FontWeight.Black,
                modifier=Modifier.padding(horizontal=12.dp,vertical=8.dp))
        }
    }
}
@Composable
fun SpaceExpeditionScreen(
    run:SpaceRun?,
    recommended:LearningTier,
    profileGrade:Int?,
    records:Map<SpacePlanet,SpacePlanetRecord>,
    historicalStars:Map<SpacePlanet,Int>,
    feedback:String,
    onBegin:(SpacePlanet,LearningTier)->Boolean,
    onShipPosition:(Float)->Unit,
    onFireHit:(Int)->Unit,
    onHint:()->Unit,
    onExit:()->Unit
) {
    var lobby by rememberSaveable {mutableStateOf(true)}
    var choice by rememberSaveable {mutableIntStateOf(0)}
    var selectedTier by rememberSaveable {mutableStateOf(recommended.name)}
    var replacing by remember {mutableStateOf<Pair<SpacePlanet,LearningTier>?>(null)}
    var failed by remember {mutableStateOf(false)}
    val tier=LearningTier.fromSaved(selectedTier,recommended)
    fun start(planet:SpacePlanet,t:LearningTier) {
        if(run!=null && !run.completed)replacing=planet to t
        else if(onBegin(planet,t)){lobby=false;failed=false}
        else failed=true
    }
    BackHandler(enabled=!lobby){lobby=true}
    replacing?.let{pair->
        AlertDialog(
            onDismissRequest={replacing=null},
            title={Text("Yeni galaksi turu?")},
            text={Text("Devam eden tur yerine yeni bir tur başlatılacak.")},
            confirmButton={TextButton(onClick={
                replacing=null
                if(onBegin(pair.first,pair.second)){lobby=false;failed=false}
                else failed=true
            }){Text("Başlat")}},
            dismissButton={TextButton(onClick={replacing=null}){Text("Vazgeç")}}
        )
    }
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(
                Color(0xFF132E69),GalaxyPaint.night,Color(0xFF03091B)
            )))
            .safeDrawingPadding()
            .padding(horizontal=10.dp,vertical=5.dp),
        verticalArrangement=Arrangement.spacedBy(5.dp)
    ) {
        Row(Modifier.fillMaxWidth().heightIn(min=43.dp),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(8.dp)){
            OutlinedButton(
                onClick={if(lobby)onExit() else lobby=true},
                modifier=Modifier.heightIn(min=42.dp),
                contentPadding=PaddingValues(horizontal=9.dp),
                border=BorderStroke(1.dp,GalaxyPaint.teal),
                colors=ButtonDefaults.outlinedButtonColors(contentColor=GalaxyPaint.bright)
            ){Text(if(lobby)"← Oyunlar" else "← Harita",fontSize=12.sp)}
            Column(Modifier.weight(1f)){
                Text(if(lobby)"GALAKSİ KAŞİFLERİ" else run?.planet?.title?:"UZAY",
                    fontSize=15.sp,fontWeight=FontWeight.Black,
                    maxLines=1,color=GalaxyPaint.gold)
                Text(if(lobby)"Uzay Matematik 2.0"
                    else run?.grade.toString()+". sınıf · "+run?.tier?.title,
                    fontSize=11.sp,color=GalaxyPaint.quiet)
            }
            Text("🚀",fontSize=23.sp)
        }
        if(lobby) {
            Column(
                modifier=Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement=Arrangement.spacedBy(10.dp)
            ){
                GalaxyScene(SpacePlanet.entries[choice],compact=false)
                Text("15 soruluk kesintisiz meteor avı",
                    color=GalaxyPaint.bright,fontSize=19.sp,
                    fontWeight=FontWeight.Black)
                Text("Geminle nişan al, hareketli meteorları vur. Doğru cevapla yeni soru otomatik gelir.",
                    color=GalaxyPaint.quiet,fontSize=13.sp)
                if(run!=null&&!run.completed) {
                    Surface(shape=RoundedCornerShape(15.dp),
                        color=Color(0xFF164C63),
                        border=BorderStroke(1.dp,GalaxyPaint.teal)){
                        Column(Modifier.fillMaxWidth().padding(12.dp),
                            verticalArrangement=Arrangement.spacedBy(6.dp)){
                            Text("Kaydedilen tur: "+run.stage+"/15 · "+run.planet.title,
                                color=GalaxyPaint.bright,fontWeight=FontWeight.Bold)
                            SpaceButton("▶ Kaydedilen Göreve Dön"){lobby=false}
                        }
                    }
                }
                Surface(shape=RoundedCornerShape(14.dp),
                    color=GalaxyPaint.panel){
                    Column(Modifier.padding(12.dp),
                        verticalArrangement=Arrangement.spacedBy(6.dp)){
                        Text(if(profileGrade==null)
                            "Sınıf belirtilmemiş: 1. sınıf kapsamı kullanılır."
                            else "Profil: "+profileGrade+". sınıf · sorular sınıfa duyarlı",
                            color=GalaxyPaint.teal,fontSize=12.sp)
                        Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){
                            LearningTier.entries.forEach{opt->
                                OutlinedButton(
                                    onClick={selectedTier=opt.name},
                                    modifier=Modifier.weight(1f).heightIn(min=46.dp),
                                    contentPadding=PaddingValues(horizontal=3.dp),
                                    border=BorderStroke(if(opt==tier)2.dp else 1.dp,
                                        if(opt==tier)GalaxyPaint.gold else GalaxyPaint.quiet),
                                    colors=ButtonDefaults.outlinedButtonColors(
                                        containerColor=if(opt==tier)
                                            Color(0xFF264F70) else Color.Transparent,
                                        contentColor=GalaxyPaint.bright)
                                ){Text(opt.title,fontSize=12.sp)}
                            }
                        }
                        Text("Zorluk değişse bile sınıfının dışındaki işlemler açılmaz.",
                            color=GalaxyPaint.quiet,fontSize=11.sp)
                    }
                }
                SpacePlanet.entries.forEach{planet->
                    val picked=choice==planet.ordinal
                    val best=maxOf(records[planet]?.bestStars?:0,historicalStars[planet]?:0)
                    Surface(
                        modifier=Modifier.fillMaxWidth()
                            .testTag("space_planet_"+planet.name),
                        onClick={choice=planet.ordinal},
                        shape=RoundedCornerShape(14.dp),
                        color=if(picked)Color(0xFF28617C) else GalaxyPaint.panel,
                        border=BorderStroke(if(picked)2.dp else 1.dp,
                            if(picked)GalaxyPaint.gold else GalaxyPaint.quiet.copy(alpha=.3f))
                    ){
                        Row(Modifier.padding(12.dp),
                            verticalAlignment=Alignment.CenterVertically,
                            horizontalArrangement=Arrangement.spacedBy(10.dp)){
                            Image(
                                painter=painterResource(SpacePremiumArt.background(planet)),
                                contentDescription=null,
                                contentScale=ContentScale.Crop,
                                modifier=Modifier.size(58.dp).clip(RoundedCornerShape(11.dp))
                            )
                            Column(Modifier.weight(1f)){
                                Text(planet.title,color=GalaxyPaint.bright,
                                    fontSize=15.sp,fontWeight=FontWeight.Black)
                                Text(planet.detail,color=GalaxyPaint.quiet,fontSize=11.sp)
                            }
                            Text("★".repeat(best)+"☆".repeat(3-best),
                                color=GalaxyPaint.gold,fontSize=14.sp)
                        }
                    }
                }
                SpaceButton("🚀 "+SpacePlanet.entries[choice].title+" Başlat"){
                    start(SpacePlanet.entries[choice],tier)
                }
                if(failed)Text(if(feedback.isBlank())
                    "Kayıt yapılamadı." else feedback,color=GalaxyPaint.gold)
            }
        } else if(run!=null && run.completed) {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                horizontalAlignment=Alignment.CenterHorizontally,
                verticalArrangement=Arrangement.spacedBy(13.dp)){
                GalaxyScene(run.planet,compact=false)
                Text("🏆 GALAKSİ TAMAMLANDI",
                    color=GalaxyPaint.gold,fontSize=22.sp,
                    fontWeight=FontWeight.Black)
                Text("★".repeat(SpaceExpeditionEngine.stars(run))+
                    "☆".repeat(3-SpaceExpeditionEngine.stars(run)),
                    fontSize=35.sp,color=GalaxyPaint.gold)
                Text(SpaceExpeditionEngine.score(run).toString()+" puan",
                    fontSize=24.sp,color=GalaxyPaint.bright,
                    fontWeight=FontWeight.Black)
                Text("15 isabet · "+run.errors+" yanlış · en iyi seri "+run.bestCombo,
                    color=GalaxyPaint.quiet,fontSize=13.sp)
                SpaceButton("🚀 Bir Tur Daha"){start(run.planet,run.tier)}
                SpaceButton("🪐 Galaksi Haritası"){lobby=true}
            }
        } else if(run!=null) {
            val q=SpaceExpeditionEngine.puzzle(run)
            Row(Modifier.fillMaxWidth(),
                verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.SpaceBetween){
                Text("🎯 "+(run.stage+1)+"/15   🔥 "+run.combo+" seri",
                    color=GalaxyPaint.bright,fontSize=13.sp,fontWeight=FontWeight.Black)
                Text("🛡 "+run.shields+"   ★ "+SpaceExpeditionEngine.score(run),
                    color=GalaxyPaint.gold,fontSize=13.sp,fontWeight=FontWeight.Black)
            }
            LinearProgressIndicator(
                progress={run.stage.toFloat()/SpaceExpeditionEngine.STAGES},
                modifier=Modifier.fillMaxWidth().height(5.dp),
                color=GalaxyPaint.gold,trackColor=Color(0xFF34567B)
            )
            Surface(shape=RoundedCornerShape(12.dp),
                color=Color(0xFF172F5B)){
                Row(Modifier.fillMaxWidth().padding(horizontal=8.dp,vertical=5.dp),
                    verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.SpaceBetween){
                    Column(Modifier.weight(1f)){
                        Text(q.prompt,
                            modifier=Modifier.testTag("space_mission_prompt"),
                            color=GalaxyPaint.bright,fontSize=25.sp,
                            fontWeight=FontWeight.Black)
                        Text(if(run.hintVisible)"💡 "+q.hint
                            else "Doğru meteoru vur · sonraki soru otomatik",
                            color=GalaxyPaint.teal,fontSize=11.sp,maxLines=2)
                    }
                    OutlinedButton(
                        onClick=onHint,enabled=!run.hintVisible,
                        modifier=Modifier.heightIn(min=43.dp),
                        contentPadding=PaddingValues(horizontal=8.dp),
                        border=BorderStroke(1.dp,GalaxyPaint.teal),
                        colors=ButtonDefaults.outlinedButtonColors(
                            contentColor=GalaxyPaint.bright)
                    ){Text("💡 İpucu",fontSize=11.sp)}
                }
            }
            SpaceCombatArena(
                run=run,puzzle=q,modifier=Modifier.weight(1f),
                onPosition=onShipPosition,onHit=onFireHit
            )
        }
    }
}
