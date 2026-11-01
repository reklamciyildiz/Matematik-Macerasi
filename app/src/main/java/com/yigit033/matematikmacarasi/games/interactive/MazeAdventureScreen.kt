package com.yigit033.matematikmacarasi.games.interactive

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

private object MazeColors {
    val night=Color(0xFF071B37)
    val ocean=Color(0xFF124675)
    val panel=Color(0xFF164469)
    val teal=Color(0xFF83EEDB)
    val gold=Color(0xFFFFD479)
    val white=Color(0xFFF6FAFF)
    val muted=Color(0xFFBCD8E9)
}
@Composable
private fun MazeTitle(text:String,subtitle:String="") {
    Column(verticalArrangement=Arrangement.spacedBy(4.dp)) {
        Text(text,color=MazeColors.white,fontSize=24.sp,fontWeight=FontWeight.Black)
        if(subtitle.isNotBlank())
            Text(subtitle,color=MazeColors.muted,fontSize=13.sp)
    }
}
@Composable
private fun MazeAction(text:String,outlined:Boolean=false,enabled:Boolean=true,onClick:()->Unit) {
    if(outlined) OutlinedButton(onClick=onClick,enabled=enabled,
        modifier=Modifier.fillMaxWidth().heightIn(min=50.dp),
        shape=RoundedCornerShape(15.dp),
        border=BorderStroke(1.dp,MazeColors.teal),
        colors=ButtonDefaults.outlinedButtonColors(contentColor=MazeColors.white)) {
        Text(text,fontSize=14.sp,fontWeight=FontWeight.Bold)
    } else Button(onClick=onClick,enabled=enabled,
        modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),
        shape=RoundedCornerShape(15.dp),
        colors=ButtonDefaults.buttonColors(containerColor=MazeColors.gold,
            contentColor=MazeColors.night)) {
        Text(text,fontSize=14.sp,fontWeight=FontWeight.ExtraBold)
    }
}
@Composable
private fun MazeScene(region:MazeRegion) {
    val pink=when(region.ordinal%3) {
        0 -> Color(0xFFFFCF94)
        1 -> Color(0xFFA9E9DB)
        else -> Color(0xFFD6B3F7)
    }
    Box(Modifier.fillMaxWidth().height(135.dp),contentAlignment=Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawRoundRect(
                brush=Brush.verticalGradient(listOf(Color(0xFF237CAC),Color(0xFF0A3A67))),
                cornerRadius=CornerRadius(42f,42f))
            val w=size.width;val h=size.height
            drawCircle(MazeColors.gold.copy(alpha=.80f),h*.25f,
                Offset(w*.80f,h*.24f))
            for(i in 0..6) {
                val center=Offset(w*(i+.3f)/6f,h*.20f)
                drawCircle(pink.copy(alpha=.12f),h*.08f,center)
            }
            val p=androidx.compose.ui.graphics.Path().apply {
                moveTo(0f,h*.68f);lineTo(w*.18f,h*.36f)
                lineTo(w*.36f,h*.70f);lineTo(w*.60f,h*.31f)
                lineTo(w*.86f,h*.66f);lineTo(w,h*.45f)
                lineTo(w,h);lineTo(0f,h);close()
            }
            drawPath(p,Color(0xFF255978))
            val hill=androidx.compose.ui.graphics.Path().apply {
                moveTo(0f,h*.90f);quadraticBezierTo(w*.25f,h*.55f,w*.5f,h*.87f)
                quadraticBezierTo(w*.75f,h*.62f,w,h*.83f)
                lineTo(w,h);lineTo(0f,h);close()
            }
            drawPath(hill,Color(0xFF298279))
            drawRoundRect(color=Color(0xFF0F3B55),
                topLeft=Offset(w*.36f,h*.49f),size=Size(w*.27f,h*.42f),
                cornerRadius=CornerRadius(22f,22f))
            drawRoundRect(color=MazeColors.gold,
                topLeft=Offset(w*.46f,h*.65f),size=Size(w*.07f,h*.25f),
                cornerRadius=CornerRadius(24f,24f))
            drawCircle(Color.White.copy(alpha=.6f),6f,Offset(w*.5f,h*.71f))
        }
        Surface(color=MazeColors.night.copy(alpha=.62f),
            shape=RoundedCornerShape(18.dp),
            border=BorderStroke(1.dp,MazeColors.gold.copy(alpha=.6f))) {
            Row(Modifier.padding(horizontal=17.dp,vertical=9.dp),
                verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Text(region.symbol,fontSize=29.sp)
                Text(region.title,color=Color.White,fontSize=18.sp,
                    fontWeight=FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MazeMap(world:MazeWorld,run:MazeExpedition,onMove:(MazeDirection)->Unit) {
    Canvas(Modifier.fillMaxWidth().aspectRatio(1f)
        .pointerInput(run.seed,run.cell,run.solvedMask,run.pendingCell) {
            detectTapGestures { offset ->
                val tile=size.width.toFloat()/world.width
                val col=(offset.x/tile).toInt()
                val row=(offset.y/tile).toInt()
                val dx=col-run.cell%world.width
                val dy=row-run.cell/world.width
                MazeDirection.entries.firstOrNull{it.dx==dx&&it.dy==dy}
                    ?.let(onMove)
            }
        }) {
        val side=size.width/world.width
        drawRoundRect(brush=Brush.verticalGradient(listOf(
            Color(0xFF306F88),Color(0xFF123A62))),
            cornerRadius=CornerRadius(36f,36f))
        for(y in 0 until world.width) for(x in 0 until world.width) {
            val index=y*world.width+x
            val open=index in world.walkable
            val left=x*side+side*.06f
            val top=y*side+side*.06f
            val tileSize=Size(side*.88f,side*.88f)
            drawRoundRect(color=if(open)Color(0xFFBCE8D8) else Color(0xFF143C56),
                topLeft=Offset(left,top),size=tileSize,
                cornerRadius=CornerRadius(side*.2f,side*.2f))
            if(!open) {
                drawRoundRect(color=Color(0xFF37728C).copy(alpha=.65f),
                    topLeft=Offset(left+side*.13f,top+side*.11f),
                    size=Size(side*.44f,side*.09f),
                    cornerRadius=CornerRadius(side*.04f,side*.04f))
            } else {
                drawCircle(Color(0xFF79B9A8).copy(alpha=.3f),
                    side*.04f,Offset(left+side*.30f,top+side*.35f))
            }
            val gateIndex=world.questions.indexOfFirst {it.cell==index}
            if(gateIndex>=0) {
                val unlocked=run.solvedMask and (1 shl gateIndex)!=0
                drawRoundRect(color=if(unlocked)Color(0xFF3FBF9C)
                    else MazeColors.gold,
                    topLeft=Offset(left+side*.10f,top+side*.10f),
                    size=Size(side*.68f,side*.68f),
                    cornerRadius=CornerRadius(side*.17f,side*.17f))
                if(!unlocked) drawCircle(Color(0xFFFFF1BB),
                    side*.13f,Offset(left+side*.44f,top+side*.41f))
            }
            if(index==world.exit) {
                drawCircle(MazeColors.gold.copy(alpha=.45f),side*.45f,
                    Offset(x*side+side*.5f,y*side+side*.5f))
                drawCircle(MazeColors.gold,side*.24f,
                    Offset(x*side+side*.5f,y*side+side*.5f))
            }
        }
        val playerX=(run.cell%world.width+.5f)*side
        val playerY=(run.cell/world.width+.5f)*side
        drawCircle(Color.White.copy(alpha=.25f),side*.47f,
            Offset(playerX,playerY))
        drawCircle(Color(0xFF2186CC),side*.34f,Offset(playerX,playerY))
        drawCircle(Color.White,side*.27f,Offset(playerX,playerY),
            style=Stroke(width=side*.06f))
        drawCircle(Color.White,side*.045f,
            Offset(playerX-side*.1f,playerY-side*.045f))
        drawCircle(Color.White,side*.045f,
            Offset(playerX+side*.1f,playerY-side*.045f))
    }
}

@Composable
private fun MazePad(onMove:(MazeDirection)->Unit,onGuide:()->Unit) {
    Column(horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.spacedBy(9.dp)) {
        OutlinedButton(onClick={onMove.curry(MazeDirection.UP)},
            modifier=Modifier.size(width=115.dp,height=56.dp),
            border=BorderStroke(1.dp,MazeColors.teal),
            colors=ButtonDefaults.outlinedButtonColors(contentColor=MazeColors.white)) {
            Text("↑ Yukarı",fontWeight=FontWeight.Bold)
        }
        Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            listOf(MazeDirection.LEFT,MazeDirection.DOWN,MazeDirection.RIGHT).forEach {dir ->
                val label=when(dir){
                    MazeDirection.LEFT -> "← Sol"
                    MazeDirection.DOWN -> "↓ Aşağı"
                    else -> "Sağ →"
                }
                OutlinedButton(onClick={onMove.curry(dir)},
                    modifier=Modifier.weight(1f).heightIn(min=56.dp),
                    contentPadding=PaddingValues(horizontal=2.dp),
                    border=BorderStroke(1.dp,MazeColors.teal),
                    colors=ButtonDefaults.outlinedButtonColors(contentColor=MazeColors.white)
                ) {Text(label,fontWeight=FontWeight.Bold,fontSize=13.sp)}
            }
        }
        TextButton(onClick=onGuide) {
            Text("🧭 Bir sonraki adımı göster",color=MazeColors.gold,fontSize=12.sp)
        }
    }
}
private fun ((MazeDirection)->Unit).curry(d:MazeDirection):()->Unit = {this(d)}

@Composable
fun MazeAdventureScreen(
    run:MazeExpedition?,
    isPremium:Boolean,
    remainingPlays:Int,
    recommendedTier:LearningTier,
    completedRuns:Int,
    regionRecords:Map<MazeRegion,MazeRegionRecord>,
    feedback:String,
    onStart:(MazeRegion,LearningTier)->Boolean,
    onMove:(MazeDirection)->Unit,
    onAnswer:(Int)->Unit,
    onHint:()->Unit,
    onExit:()->Unit,
    onUpgrade:()->Unit
) {
    var lobby by rememberSaveable {mutableStateOf(true)}
    var selectedRegion by rememberSaveable {mutableIntStateOf(0)}
    var selectedTierName by rememberSaveable {mutableStateOf(recommendedTier.name)}
    var replacing by remember {mutableStateOf<Pair<MazeRegion,LearningTier>?>(null)}
    var showLimit by remember {mutableStateOf(false)}
    var guidance by remember {mutableStateOf("")}
    val selectedTier=LearningTier.fromSaved(selectedTierName)
    BackHandler(enabled=!lobby) {lobby=true;guidance=""}
    fun start(region:MazeRegion,tier:LearningTier) {
        if(run!=null && !run.completed) {
            replacing=region to tier
        } else {
            if(onStart(region,tier)) {lobby=false;guidance=""}
            else showLimit=true
        }
    }
    if(replacing!=null) AlertDialog(onDismissRequest={replacing=null},
        title={Text("Yeni maceraya geçilsin mi?")},
        text={Text("Mevcut yarım labirentin konumu ve çözülmüş kapıları silinir. " +
            "Kaldığın yerden devam edebilirsin.")},
        confirmButton={TextButton(onClick={
            val pair=replacing!!;replacing=null
            if(onStart(pair.first,pair.second)){lobby=false;guidance=""}
            else showLimit=true
        }){Text("Yeni Macera Başlat")}},
        dismissButton={TextButton(onClick={replacing=null}){Text("Vazgeç")}})
    if(showLimit) AlertDialog(onDismissRequest={showLimit=false},
        title={Text("Günlük Keşif Hakkı")},
        text={Text("Ücretsiz oyuncular her gün 5 yeni labirent açabilir. " +
            "Yarın yeniden başlayabilir veya Premium ile sınırsız oynayabilirsin. " +
            "Yarım bıraktığın keşfe ücretsiz devam edebilirsin.")},
        confirmButton={TextButton(onClick={showLimit=false;onUpgrade()}) {
            Text("Yetişkin Bölgesi") }},
        dismissButton={TextButton(onClick={showLimit=false}){Text("Anladım")}})
    val world=remember(run?.seed,run?.region,run?.tier) {
        run?.let{MazeAdventureEngine.generate(it.seed,it.region,it.tier)}
    }
    val question=if(!lobby && run!=null && world!=null && run.pendingCell>=0)
        world.gates[run.pendingCell] else null
    if(question!=null && run!=null) AlertDialog(
        onDismissRequest={},
        title={Text("🔐 Matematik Kapısı")},
        text={
            Column(verticalArrangement=Arrangement.spacedBy(12.dp)) {
                Text(question.prompt,fontSize=24.sp,fontWeight=FontWeight.Black)
                Text("Doğru cevabı seçerek kristal kapıyı aç.",
                    fontSize=13.sp)
                question.choices.forEach {value ->
                    Button(onClick={onAnswer.curryAnswer(value)},
                        modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
                        colors=ButtonDefaults.buttonColors(
                            containerColor=Color(0xFF14577D))) {
                        Text(value.toString(),fontSize=18.sp,color=Color.White)
                    }
                }
                if(run.shownHint)
                    Text("💡 ${question.hint}",color=Color(0xFF1D6F59),
                        fontWeight=FontWeight.Bold)
                if(run.errors>0)
                    Text("Yanlış denemeler: ${run.errors}. Yeniden deneyebilirsin.",
                        fontSize=12.sp,color=Color(0xFF925125))
            }
        },
        confirmButton={TextButton(onClick=onHint,enabled=!run.shownHint) {
            Text("💡 İpucu")}},
        dismissButton={Text("Kapıyı açmak için bir cevap seç",fontSize=11.sp)}
    )
    Column(Modifier.fillMaxSize().background(
        Brush.verticalGradient(listOf(MazeColors.ocean,MazeColors.night)))
        .safeDrawingPadding()
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
        verticalArrangement=Arrangement.spacedBy(14.dp)) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick={
                if(lobby)onExit() else {lobby=true;guidance=""}
            },modifier=Modifier.heightIn(min=48.dp),
                contentPadding=PaddingValues(horizontal=12.dp),
                colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White),
                border=BorderStroke(1.dp,MazeColors.teal)) {
                Text(if(lobby)"← Oyunlar" else "← Harita",fontSize=13.sp)
            }
            Column(Modifier.weight(1f)) {
                Text("MATEMATİK LABİRENTİ",
                    fontWeight=FontWeight.Black,fontSize=17.sp,color=MazeColors.gold)
                Text("Kristal Kapıların Sırrı · 2.0",
                    fontSize=11.sp,color=MazeColors.muted)
            }
            Text(if(isPremium)"👑" else "🧭",fontSize=25.sp)
        }
        if(lobby) {
            MazeScene(MazeRegion.entries[selectedRegion])
            MazeTitle("8 Macera Bölgesi","Bir bölge ve seviyeni seç; çıkıştaki hazineye ulaş.")
            Surface(color=MazeColors.panel,shape=RoundedCornerShape(18.dp),
                border=BorderStroke(1.dp,Color(0xFF487DA2))) {
                Column(Modifier.fillMaxWidth().padding(14.dp),
                    verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text("🏆 $completedRuns tamamlanan keşif",
                        color=MazeColors.gold,fontWeight=FontWeight.Bold)
                    Text(if(isPremium)"👑 Premium · Sınırsız labirent"
                        else "Ücretsiz · Bugün kalan yeni oyun: $remainingPlays / 5",
                        color=MazeColors.white,fontSize=12.sp)
                    if(run!=null && !run.completed) {
                        Text("📌 Yarım kalan: ${run.region.title} · ${run.tier.title} " +
                            "· ${run.solvedCount} kapı açık",
                            color=MazeColors.teal,fontSize=13.sp)
                        MazeAction("▶ Kaldığım Yerden Devam Et",onClick={
                            lobby=false;guidance=""
                        })
                    }
                }
            }
            Text("1. Zorluk Seviyesi",fontSize=17.sp,
                color=MazeColors.gold,fontWeight=FontWeight.ExtraBold)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                LearningTier.entries.forEach {tier ->
                    val picked=tier==selectedTier
                    OutlinedButton(onClick={selectedTierName=tier.name},
                        modifier=Modifier.weight(1f).heightIn(min=49.dp),
                        contentPadding=PaddingValues(2.dp),
                        border=BorderStroke(if(picked)2.dp else 1.dp,
                            if(picked)MazeColors.gold else MazeColors.teal),
                        colors=ButtonDefaults.outlinedButtonColors(
                            containerColor=if(picked)Color(0xFF3C647D) else Color.Transparent,
                            contentColor=Color.White)) {
                        Text(tier.title,fontSize=12.sp,fontWeight=FontWeight.Bold)
                    }
                }
            }
            Text("Önerilen: ${recommendedTier.title} · Seçimini değiştirebilirsin.",
                color=MazeColors.muted,fontSize=11.sp)
            Text("2. Macera Bölgesi",fontSize=17.sp,
                color=MazeColors.gold,fontWeight=FontWeight.ExtraBold)
            MazeRegion.entries.chunked(2).forEach {row ->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                    row.forEach {region ->
                        val picked=region.ordinal==selectedRegion
                        Surface(
                            modifier=Modifier.weight(1f),
                            color=if(picked)Color(0xFF2E7181) else MazeColors.panel,
                            shape=RoundedCornerShape(17.dp),
                            border=BorderStroke(if(picked)2.dp else 1.dp,
                                if(picked)MazeColors.gold else Color(0xFF3C7195)),
                            onClick={selectedRegion=region.ordinal}) {
                            Column(Modifier.padding(12.dp).heightIn(min=113.dp),
                                verticalArrangement=Arrangement.spacedBy(5.dp)) {
                                Text(region.symbol,fontSize=26.sp)
                                Text(region.title,color=MazeColors.white,
                                    fontSize=13.sp,fontWeight=FontWeight.ExtraBold)
                                Text("${MazeAdventureEngine.questionCount(region)} kapı · ${region.subtitle}",
                                    color=MazeColors.muted,fontSize=10.sp,
                                    lineHeight=14.sp)
                                val record=regionRecords[region]
                                if(record!=null && record.completed>0) {
                                    Text("⭐ ${record.bestStars}/3  ·  ${record.completed} keşif",
                                        color=MazeColors.gold,fontSize=10.sp)
                                }
                            }
                        }
                    }
                }
            }
            MazeAction("🚪 ${MazeRegion.entries[selectedRegion].title} Macerasını Başlat") {
                start(MazeRegion.entries[selectedRegion],selectedTier)
            }
            Text("İnternetsiz çalışır · Zaman baskısı yok · " +
                "Matematik soruları çözülmeden çıkış açılamaz.",
                color=MazeColors.muted,fontSize=11.sp)
        } else if(run!=null && world!=null) {
            MazeScene(run.region)
            if(run.completed) {
                MazeTitle("🎉 Hazineyi Buldun!","${run.region.title} tamamlandı")
                Surface(color=MazeColors.panel,shape=RoundedCornerShape(20.dp),
                    border=BorderStroke(1.dp,MazeColors.gold)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp),
                        horizontalAlignment=Alignment.CenterHorizontally,
                        verticalArrangement=Arrangement.spacedBy(10.dp)) {
                        Text("⭐".repeat(MazeAdventureEngine.stars(run)),
                            fontSize=35.sp)
                        Text("${MazeAdventureEngine.score(run)} puan",
                            color=MazeColors.gold,fontSize=23.sp,
                            fontWeight=FontWeight.Black)
                        Text("${run.solvedCount} kapı · ${run.attempts} deneme · " +
                            "${run.errors} hata · ${run.hints} ipucu",
                            color=MazeColors.white,fontSize=12.sp)
                        val record=regionRecords[run.region]
                        Text("Bu bölgedeki en iyi: ${record?.bestStars ?: 0} yıldız",
                            color=MazeColors.teal,fontSize=12.sp)
                        Text("Tekrar oynarken yalnız önceki en iyi yıldızını " +
                            "aşarsan ek yıldız kazanırsın.",
                            color=MazeColors.muted,fontSize=11.sp)
                    }
                }
                MazeAction("🗺️ Yeni Bir Bölge Keşfet",onClick={lobby=true})
                MazeAction("Bu Bölümü Tekrar Oyna",outlined=true) {
                    start(run.region,run.tier)
                }
            } else {
                MazeTitle(run.region.title,"Kristal kapıları aç ve altın portala ulaş.")
                Surface(color=MazeColors.panel,shape=RoundedCornerShape(15.dp)) {
                    Row(Modifier.fillMaxWidth().padding(13.dp),
                        horizontalArrangement=Arrangement.SpaceBetween,
                        verticalAlignment=Alignment.CenterVertically) {
                        Text("🔐 ${run.solvedCount}/${world.questions.size} Kapı",
                            color=MazeColors.gold,fontWeight=FontWeight.Bold)
                        Text("${run.tier.title} · ${MazeAdventureEngine.score(run)} puan",
                            color=MazeColors.muted,fontSize=12.sp)
                    }
                }
                Surface(color=Color(0xFF0F385A),
                    shape=RoundedCornerShape(18.dp),
                    border=BorderStroke(2.dp,MazeColors.teal.copy(alpha=.55f))) {
                    Box(Modifier.fillMaxWidth().padding(8.dp)) {
                        MazeMap(world,run,onMove)
                    }
                }
                Text("🔵 Sen   🟡 Kilitli kapı / çıkış   🟢 Açılan kapı",
                    color=MazeColors.muted,fontSize=12.sp)
                MazePad(onMove=onMove,onGuide={
                    val dir=MazeAdventureEngine.guidance(run,world)
                    guidance=when(dir) {
                        MazeDirection.UP -> "🧭 Yukarı yönde ilerle."
                        MazeDirection.DOWN -> "🧭 Aşağı yönde ilerle."
                        MazeDirection.LEFT -> "🧭 Sola doğru ilerle."
                        MazeDirection.RIGHT -> "🧭 Sağa doğru ilerle."
                        null -> "🎉 Hazineye ulaştın."
                    }
                })
                if(guidance.isNotBlank())
                    Text(guidance,color=MazeColors.gold,fontWeight=FontWeight.Bold,
                        fontSize=13.sp)
                if(feedback.isNotBlank())
                    Surface(color=Color(0xFF20605E),
                        shape=RoundedCornerShape(14.dp)) {
                        Text(feedback,Modifier.fillMaxWidth().padding(12.dp),
                            color=Color.White,fontSize=13.sp)
                    }
                MazeAction("💾 İlerlemeyi Koru ve Haritaya Dön",
                    outlined=true,onClick={lobby=true})
            }
        }
        Spacer(Modifier.height(14.dp))
    }
}
private fun ((Int)->Unit).curryAnswer(value:Int):()->Unit = {this(value)}
