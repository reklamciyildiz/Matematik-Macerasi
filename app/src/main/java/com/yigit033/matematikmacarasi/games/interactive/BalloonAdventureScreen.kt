package com.yigit033.matematikmacarasi.games.interactive

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

private object BalloonPalette {
    val deep=Color(0xFF081B42)
    val sky=Color(0xFF125F9A)
    val cyan=Color(0xFF9CE9FF)
    val white=Color(0xFFF6FCFF)
    val gold=Color(0xFFFFD888)
    val muted=Color(0xFFBDD8F0)
    val panel=Color(0xFF153A68)
    val island=listOf(Color(0xFFFFC86B),Color(0xFF92D3F9),
        Color(0xFFBFA4FA),Color(0xFF81E9D6),Color(0xFFFF98C6))
    val balloon=listOf(Color(0xFFFF697E),Color(0xFF6BD5F2),
        Color(0xFFFFC75C),Color(0xFFAE92F9))
}

@Composable
private fun SkyIllustration(realm:BalloonRealm,compact:Boolean=false) {
    val accent=BalloonPalette.island[realm.ordinal]
    Box(
        Modifier.fillMaxWidth().height(if(compact)118.dp else 185.dp),
        contentAlignment=Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val w=size.width;val h=size.height
            drawRect(Brush.verticalGradient(listOf(Color(0xFF319ED0),Color(0xFF16578A))))
            drawCircle(BalloonPalette.gold.copy(alpha=.84f),
                radius=h*.24f,center=Offset(w*.79f,h*.25f))
            for(i in 0..4) {
                val x=(i+.3f)*w/4.2f
                val y=h*(.25f+.16f*(i%2))
                drawOval(Color.White.copy(alpha=.25f),Offset(x,y),
                    Size(w*.16f,h*.13f))
            }
            val hills=Path().apply {
                moveTo(0f,h*.76f)
                quadraticBezierTo(w*.15f,h*.46f,w*.36f,h*.72f)
                quadraticBezierTo(w*.59f,h*.34f,w*.78f,h*.68f)
                quadraticBezierTo(w*.91f,h*.51f,w,h*.66f)
                lineTo(w,h);lineTo(0f,h);close()
            }
            drawPath(hills,Color(0xFF1F618B))
            val foreground=Path().apply {
                moveTo(0f,h*.90f)
                quadraticBezierTo(w*.30f,h*.62f,w*.52f,h*.91f)
                quadraticBezierTo(w*.78f,h*.69f,w,h*.84f)
                lineTo(w,h);lineTo(0f,h);close()
            }
            drawPath(foreground,Color(0xFF167C84))
            repeat(3) {index->
                val x=w*(.16f+index*.27f)
                val y=h*(.36f+(index%2)*.14f)
                val tint=BalloonPalette.balloon[(index+realm.ordinal)%4]
                drawLine(Color.White.copy(alpha=.75f),Offset(x,y+h*.11f),
                    Offset(x+3f,y+h*.27f),2f)
                drawOval(tint,Offset(x-w*.045f,y-h*.11f),
                    Size(w*.09f,h*.19f))
                drawOval(Color.White.copy(alpha=.34f),
                    Offset(x-w*.023f,y-h*.079f),Size(w*.022f,h*.06f))
            }
        }
        Surface(
            color=BalloonPalette.deep.copy(alpha=.68f),
            shape=RoundedCornerShape(18.dp),
            border=BorderStroke(1.dp,accent.copy(alpha=.7f))
        ) {
            Row(Modifier.padding(horizontal=16.dp,vertical=9.dp),
                verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                Text(realm.icon,fontSize=27.sp)
                Column {
                    Text(realm.title,color=BalloonPalette.white,
                        fontSize=17.sp,fontWeight=FontWeight.Black)
                    if(!compact)Text(realm.subtitle,color=BalloonPalette.cyan,
                        fontSize=11.sp)
                }
            }
        }
    }
}

@Composable
private fun RowScope.BalloonButton(
    value:Int,index:Int,wrong:Boolean,revealed:Boolean,correct:Boolean,
    onClick:()->Unit
) {
    val enabled=!wrong && !revealed
    val tint=BalloonPalette.balloon[index%BalloonPalette.balloon.size]
    val floaty=rememberInfiniteTransition(label="floating-balloon")
    val bob by floaty.animateFloat(
        initialValue=0f,targetValue=5f,
        animationSpec=infiniteRepeatable(
            animation=tween(durationMillis=1700+index*220),
            repeatMode=RepeatMode.Reverse
        ),label="gentle-hover"
    )
    Surface(
        modifier=Modifier.weight(1f).heightIn(min=144.dp)
            .testTag("balloon_choice_"+value),
        onClick=onClick,
        enabled=enabled,
        color=when {
            correct -> Color(0xFF1B7669)
            wrong -> Color(0xFF244164)
            else -> BalloonPalette.panel
        },
        shape=RoundedCornerShape(22.dp),
        border=BorderStroke(if(correct)3.dp else 1.dp,when {
            correct -> BalloonPalette.gold
            wrong -> BalloonPalette.muted.copy(alpha=.25f)
            else -> tint.copy(alpha=.8f)
        })
    ) {
        Box(Modifier.fillMaxWidth().padding(vertical=6.dp),
            contentAlignment=Alignment.Center) {
            Canvas(
                Modifier.size(width=112.dp,height=126.dp)
                    .offset(y=(-bob).dp)
            ) {
                val w=size.width;val h=size.height
                drawLine(
                    color=Color.White.copy(alpha=if(enabled).8f else .25f),
                    start=Offset(w*.5f,h*.78f),end=Offset(w*.55f,h*.98f),
                    strokeWidth=2.5f
                )
                drawOval(
                    color=if(wrong)tint.copy(alpha=.25f) else tint,
                    topLeft=Offset(w*.11f,h*.02f),
                    size=Size(w*.78f,h*.78f)
                )
                val knot=Path().apply {
                    moveTo(w*.50f,h*.75f)
                    lineTo(w*.43f,h*.85f)
                    lineTo(w*.58f,h*.85f)
                    close()
                }
                drawPath(knot,if(wrong)tint.copy(alpha=.25f) else tint)
                drawOval(
                    color=Color.White.copy(alpha=if(wrong).12f else .42f),
                    topLeft=Offset(w*.27f,h*.13f),
                    size=Size(w*.18f,h*.23f)
                )
                drawOval(
                    color=Color(0xFF12345B).copy(alpha=.14f),
                    topLeft=Offset(w*.51f,h*.16f),
                    size=Size(w*.25f,h*.48f)
                )
            }
            Text(
                text=if(wrong)"✕" else value.toString(),
                modifier=Modifier.offset(y=(-19).dp),
                color=if(wrong)BalloonPalette.muted else Color(0xFF13294C),
                fontSize=26.sp,fontWeight=FontWeight.Black,
                textAlign=TextAlign.Center
            )
            if(correct)Text("✓ DOĞRU",
                color=BalloonPalette.gold,fontSize=11.sp,
                fontWeight=FontWeight.Black,
                modifier=Modifier.align(Alignment.BottomCenter).padding(bottom=5.dp))
        }
    }
}

@Composable
private fun PrimaryButton(label:String,enabled:Boolean=true,onClick:()->Unit) {
    Button(onClick=onClick,enabled=enabled,
        modifier=Modifier.fillMaxWidth().heightIn(min=54.dp),
        shape=RoundedCornerShape(17.dp),
        colors=ButtonDefaults.buttonColors(
            containerColor=BalloonPalette.gold,
            contentColor=BalloonPalette.deep)) {
        Text(label,fontSize=15.sp,fontWeight=FontWeight.ExtraBold)
    }
}

@Composable
fun BalloonAdventureScreen(
    run:BalloonRun?,
    suggestedTier:LearningTier,
    records:Map<BalloonRealm,BalloonRecord>,
    historicalStars:Map<BalloonRealm,Int>,
    feedback:String,
    onStart:(BalloonRealm,LearningTier)->Boolean,
    onChoose:(Int)->Unit,
    onHint:()->Unit,
    onNext:()->Unit,
    onExit:()->Unit
) {
    var lobby by rememberSaveable {mutableStateOf(true)}
    var selectedRealm by rememberSaveable {mutableIntStateOf(0)}
    var selectedTierName by rememberSaveable {mutableStateOf(suggestedTier.name)}
    var replacing by remember {mutableStateOf<Pair<BalloonRealm,LearningTier>?>(null)}
    var startError by remember {mutableStateOf(false)}
    val selectedTier=LearningTier.fromSaved(selectedTierName,suggestedTier)
    BackHandler(enabled=!lobby) {lobby=true}
    fun start(realm:BalloonRealm,tier:LearningTier) {
        if(run!=null && !run.completed)replacing=realm to tier
        else if(onStart(realm,tier)) {lobby=false;startError=false}
        else startError=true
    }
    if(replacing!=null)AlertDialog(
        onDismissRequest={replacing=null},
        title={Text("Yeni maceraya geçilsin mi?")},
        text={Text("Yarım kalan balon yolculuğu kaydı değiştirilecek. İstersen kaldığın yerden devam et.")},
        confirmButton={TextButton(onClick={
            val pair=replacing!!;replacing=null
            if(onStart(pair.first,pair.second)){lobby=false;startError=false}
            else startError=true
        }){Text("Yeni oyuna geç")}},
        dismissButton={TextButton(onClick={replacing=null}){Text("Vazgeç")}}
    )
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(
                BalloonPalette.sky,BalloonPalette.deep,Color(0xFF06132E))))
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal=16.dp,vertical=12.dp),
        verticalArrangement=Arrangement.spacedBy(13.dp)
    ) {
        Row(Modifier.fillMaxWidth(),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            OutlinedButton(
                onClick={if(lobby)onExit() else lobby=true},
                modifier=Modifier.heightIn(min=48.dp),
                contentPadding=PaddingValues(horizontal=12.dp),
                shape=RoundedCornerShape(13.dp),
                border=BorderStroke(1.dp,BalloonPalette.cyan),
                colors=ButtonDefaults.outlinedButtonColors(
                    contentColor=BalloonPalette.white)
            ) {Text(if(lobby)"← Oyunlar" else "← Adalar",fontSize=13.sp)}
            Column(Modifier.weight(1f)) {
                Text("BALON MACERASI",color=BalloonPalette.gold,
                    fontSize=17.sp,fontWeight=FontWeight.Black)
                Text("Gökyüzünün Matematik Sırları · 2.0",
                    color=BalloonPalette.muted,fontSize=10.sp)
            }
            Text("🎈",fontSize=28.sp)
        }
        if(lobby) {
            SkyIllustration(BalloonRealm.entries[selectedRealm])
            Text("5 Gökyüzü Dünyası",color=BalloonPalette.white,
                fontSize=23.sp,fontWeight=FontWeight.Black)
            Text("Balonları doğru sırayla patlat. Matematikte güçlen, her dünyada üç yıldız kazan.",
                color=BalloonPalette.muted,fontSize=13.sp)
            if(run!=null && !run.completed)Surface(
                color=Color(0xFF185F78),shape=RoundedCornerShape(18.dp),
                border=BorderStroke(1.dp,BalloonPalette.cyan)) {
                Column(Modifier.fillMaxWidth().padding(13.dp),
                    verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    Text("📌 Yarım kalan: "+run.realm.title+
                        " · "+run.solved+"/10 balon",
                        color=BalloonPalette.white,fontWeight=FontWeight.Bold)
                    PrimaryButton("▶ Kaldığım Yerden Devam Et") {
                        lobby=false
                    }
                }
            }
            Text("Zorluğunu seç",color=BalloonPalette.gold,
                fontSize=17.sp,fontWeight=FontWeight.ExtraBold)
            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                LearningTier.entries.forEach {tier->
                    val selected=tier==selectedTier
                    OutlinedButton(onClick={selectedTierName=tier.name},
                        modifier=Modifier.weight(1f).heightIn(min=48.dp),
                        contentPadding=PaddingValues(horizontal=2.dp),
                        border=BorderStroke(if(selected)2.dp else 1.dp,
                            if(selected)BalloonPalette.gold else BalloonPalette.cyan),
                        colors=ButtonDefaults.outlinedButtonColors(
                            contentColor=BalloonPalette.white,
                            containerColor=if(selected)Color(0xFF31668A) else Color.Transparent)
                    ) {Text(tier.title,fontSize=12.sp,fontWeight=FontWeight.Bold)}
                }
            }
            Text("Profiline göre önerilen: "+suggestedTier.title+
                ". Dilersen değiştirebilirsin.",
                color=BalloonPalette.muted,fontSize=11.sp)
            Text("Bir dünya seç",color=BalloonPalette.gold,
                fontSize=17.sp,fontWeight=FontWeight.ExtraBold)
            BalloonRealm.entries.chunked(2).forEach {pair->
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                    pair.forEach {realm->
                        val selected=realm.ordinal==selectedRealm
                        val record=records[realm]?:BalloonRecord()
                        val best=maxOf(record.bestStars,historicalStars[realm]?:0)
                        Surface(
                            modifier=Modifier.weight(1f).heightIn(min=145.dp)
                                .testTag("balloon_realm_"+realm.name),
                            onClick={selectedRealm=realm.ordinal},
                            color=if(selected)Color(0xFF2C6987) else BalloonPalette.panel,
                            shape=RoundedCornerShape(17.dp),
                            border=BorderStroke(if(selected)2.dp else 1.dp,
                                if(selected)BalloonPalette.gold else
                                    BalloonPalette.island[realm.ordinal].copy(alpha=.5f))
                        ) {
                            Column(Modifier.padding(12.dp),
                                verticalArrangement=Arrangement.spacedBy(5.dp)) {
                                Text(realm.icon,fontSize=24.sp)
                                Text(realm.title,color=BalloonPalette.white,
                                    fontWeight=FontWeight.Bold,fontSize=13.sp)
                                Text(realm.skill+" · 10 balon",
                                    color=BalloonPalette.muted,fontSize=11.sp)
                                Text("★".repeat(best)+"☆".repeat(3-best),
                                    color=BalloonPalette.gold,fontSize=17.sp)
                                if(record.completions>0)Text(
                                    record.completions.toString()+" tamamlanma",
                                    color=BalloonPalette.cyan,fontSize=10.sp)
                            }
                        }
                    }
                    if(pair.size==1)Spacer(Modifier.weight(1f))
                }
            }
            PrimaryButton("🚀 "+BalloonRealm.entries[selectedRealm].title+" Macerasını Başlat") {
                start(BalloonRealm.entries[selectedRealm],selectedTier)
            }
            if(startError)Text(
                if(feedback.isNotBlank())feedback else "Oyun kaydedilemedi, tekrar dene.",
                color=BalloonPalette.gold,fontSize=12.sp)
            Text("Çevrimdışı oynanır · Zorunlu süre yok · Reklamsız oyun alanı",
                color=BalloonPalette.muted,fontSize=11.sp)
        } else if(run!=null) {
            SkyIllustration(run.realm,compact=true)
            if(run.completed) {
                Surface(
                    color=BalloonPalette.panel,
                    shape=RoundedCornerShape(22.dp),
                    border=BorderStroke(2.dp,BalloonPalette.gold)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment=Alignment.CenterHorizontally,
                        verticalArrangement=Arrangement.spacedBy(12.dp)
                    ) {
                        Text("🏆 GÖKYÜZÜ KAŞİFİ",color=BalloonPalette.gold,
                            fontSize=22.sp,fontWeight=FontWeight.Black)
                        Text("★".repeat(BalloonAdventureEngine.stars(run))+
                            "☆".repeat(3-BalloonAdventureEngine.stars(run)),
                            color=BalloonPalette.gold,fontSize=39.sp)
                        Text(BalloonAdventureEngine.score(run).toString()+" puan",
                            color=BalloonPalette.white,
                            fontSize=26.sp,fontWeight=FontWeight.Black)
                        Text("10 soru · "+run.firstTry+" ilk denemede doğru",
                            color=BalloonPalette.cyan,fontSize=14.sp)
                        Text(run.errors.toString()+" yanlış deneme · "+
                            run.hints+" ipucu · en iyi seri "+run.bestStreak,
                            color=BalloonPalette.muted,fontSize=12.sp,
                            textAlign=TextAlign.Center)
                        val retry=when {
                            run.firstTry<7 -> "Bir daha oynarken acele etme; her işlemi parçalayarak çöz."
                            run.firstTry<9 -> "Yüksek doğruluk! Bir sonraki oyunda hiç ipucu kullanmadan dene."
                            else -> "Güçlü bir tur! Diğer dünyada da aynı yöntemi kullan."
                        }
                        Text(retry,color=BalloonPalette.white,fontSize=13.sp,
                            textAlign=TextAlign.Center)
                    }
                }
                PrimaryButton("🎈 Bu Dünyayı Yeniden Oyna"){
                    start(run.realm,run.tier)
                }
                OutlinedButton(onClick={lobby=true},
                    modifier=Modifier.fillMaxWidth().heightIn(min=50.dp),
                    border=BorderStroke(1.dp,BalloonPalette.cyan),
                    colors=ButtonDefaults.outlinedButtonColors(
                        contentColor=BalloonPalette.white)) {
                    Text("🗺️ Diğer dünyaları keşfet")
                }
            } else {
                Surface(color=BalloonPalette.panel,
                    shape=RoundedCornerShape(16.dp)) {
                    Column(Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(),
                            horizontalArrangement=Arrangement.SpaceBetween) {
                            Text("🎯 "+run.solved+"/10 doğru balon",
                                color=BalloonPalette.white,fontWeight=FontWeight.Bold,
                                fontSize=13.sp)
                            Text("🔥 "+run.streak+" seri",
                                color=BalloonPalette.gold,fontSize=13.sp)
                        }
                        LinearProgressIndicator(
                            progress=run.solved.toFloat()/BalloonAdventureEngine.QUESTIONS,
                            modifier=Modifier.fillMaxWidth().height(7.dp),
                            color=BalloonPalette.gold,
                            trackColor=Color(0xFF305878)
                        )
                        Text(run.tier.title+" · "+run.realm.skill+" · "+
                            BalloonAdventureEngine.score(run)+" puan",
                            color=BalloonPalette.muted,fontSize=12.sp)
                    }
                }
                val q=BalloonAdventureEngine.question(run)
                Text("Soru "+(run.index+1)+" / "+BalloonAdventureEngine.QUESTIONS,
                    color=BalloonPalette.cyan,fontWeight=FontWeight.Bold,fontSize=13.sp)
                Text(q.expression,
                    color=BalloonPalette.white,fontSize=32.sp,
                    fontWeight=FontWeight.Black,
                    modifier=Modifier.fillMaxWidth().testTag("balloon_question"),
                    textAlign=TextAlign.Center)
                Text(if(run.resolved)"Doğru balonu buldun!"
                    else "Sonucu taşıyan balona dokun.",
                    color=BalloonPalette.muted,fontSize=13.sp,
                    textAlign=TextAlign.Center,
                    modifier=Modifier.fillMaxWidth())
                q.choices.chunked(2).forEachIndexed {rowIndex,pair->
                    Row(Modifier.fillMaxWidth(),
                        horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                        pair.forEachIndexed {colIndex,value->
                            val optionIndex=rowIndex*2+colIndex
                            val wrong=run.wrongMask and (1 shl optionIndex)!=0
                            BalloonButton(
                                value=value,index=optionIndex,
                                wrong=wrong,revealed=run.resolved,
                                correct=run.resolved && value==q.answer
                            ) {
                                onChoose(value)
                            }
                        }
                    }
                }
                if(run.resolved) {
                    Surface(color=Color(0xFF185F6B),
                        shape=RoundedCornerShape(18.dp),
                        border=BorderStroke(1.dp,BalloonPalette.cyan)) {
                        Column(Modifier.fillMaxWidth().padding(14.dp),
                            verticalArrangement=Arrangement.spacedBy(8.dp)) {
                            Text("✨ Harika, doğru sonuç: "+q.answer,
                                color=BalloonPalette.gold,
                                fontWeight=FontWeight.Black,fontSize=17.sp)
                            Text(q.explanation,color=BalloonPalette.white,
                                fontSize=14.sp)
                        }
                    }
                    PrimaryButton(
                        if(run.index==BalloonAdventureEngine.QUESTIONS-1)
                            "🏆 Sonuçlarımı Gör" else "Sonraki Balon →",
                        onClick=onNext
                    )
                } else {
                    if(run.wrongMask!=0) {
                        Text("Olmadı. İşlemi tekrar düşün; yanlış balonlar işaretlendi.",
                            color=BalloonPalette.gold,fontSize=13.sp,
                            modifier=Modifier.testTag("balloon_wrong_feedback"))
                    }
                    if(run.hintVisible) {
                        Surface(color=Color(0xFF164F65),
                            shape=RoundedCornerShape(15.dp)) {
                            Text("💡 "+q.hint,
                                color=BalloonPalette.white,fontSize=13.sp,
                                modifier=Modifier.fillMaxWidth().padding(12.dp))
                        }
                    }
                    OutlinedButton(onClick=onHint,enabled=!run.hintVisible,
                        modifier=Modifier.fillMaxWidth().heightIn(min=51.dp),
                        border=BorderStroke(1.dp,BalloonPalette.cyan),
                        colors=ButtonDefaults.outlinedButtonColors(
                            contentColor=BalloonPalette.white)) {
                        Text(if(run.hintVisible)"💡 İpucu gösterildi"
                            else "💡 İpucu al")
                    }
                }
                if(feedback.isNotBlank())Text(feedback,color=BalloonPalette.gold,
                    fontSize=13.sp,fontWeight=FontWeight.Bold)
                Text("Yanlışlar yıldızları etkileyebilir; süre sınırı yok.",
                    color=BalloonPalette.muted,fontSize=11.sp,
                    textAlign=TextAlign.Center,
                    modifier=Modifier.fillMaxWidth())
                TextButton(onClick={lobby=true},modifier=Modifier.fillMaxWidth()) {
                    Text("💾 İlerlemeyi koru ve haritaya dön",
                        color=BalloonPalette.cyan,fontSize=12.sp)
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
