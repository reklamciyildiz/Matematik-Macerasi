package com.yigit033.matematikmacarasi.games.geometry

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

private val Gold = Color(0xFFFFD78A)
private val Ink = Color(0xFF0F2940)
private val Mint = Color(0xFF7EF1DA)
private val Panel = Color(0xFF17455E)
private val Pale = Color(0xFFE9F7F5)

@Composable
private fun CastleAction(text: String, enabled: Boolean = true, onClick: () -> Unit) {
    Button(
        modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
        onClick = onClick, enabled = enabled,
        shape = RoundedCornerShape(17.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Gold, contentColor = Ink,
            disabledContainerColor = Color(0xFF42647A),
            disabledContentColor = Color(0xFFBDD2D7)
        )
    ) { Text(text, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp) }
}

@Composable
private fun CastleScene(drawableId: Int, description: String, big: Boolean = false) {
    val imageHeight = if(big) 205.dp else 128.dp
    Box(
        Modifier.fillMaxWidth().height(imageHeight)
            .clip(RoundedCornerShape(20.dp))
            .border(2.dp, Gold.copy(alpha = .82f), RoundedCornerShape(20.dp))
    ) {
        Image(painter = painterResource(drawableId),
            contentDescription = description, contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize())
        Box(Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color.Transparent, Ink.copy(alpha = .25f)))
        ))
    }
}

@Composable
private fun ShapeIcon(shape: CastleShape, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val p = Path()
        when(shape) {
            CastleShape.CIRCLE -> {
                drawCircle(color, radius = minOf(w,h) * .35f)
                drawCircle(Color.White.copy(alpha=.8f), radius=minOf(w,h)*.35f,
                    style=Stroke(width=3.0f))
            }
            else -> {
                val sides = when(shape) {
                    CastleShape.TRIANGLE -> 3
                    CastleShape.SQUARE,CastleShape.RECTANGLE -> 4
                    CastleShape.PENTAGON -> 5
                    CastleShape.HEXAGON -> 6
                    else -> 4
                }
                val points = (0 until sides).map { i ->
                    val angle = -Math.PI/2 + i * 2 * Math.PI / sides
                    val radiusX = if(shape == CastleShape.RECTANGLE) w * .43 else w * .34
                    val radiusY = if(shape == CastleShape.RECTANGLE) h * .22 else h * .34
                    Pair(w/2 + (kotlin.math.cos(angle)*radiusX).toFloat(),
                        h/2 + (kotlin.math.sin(angle)*radiusY).toFloat())
                }
                // For square and rectangle, aligned edges are easier for younger learners.
                if(shape == CastleShape.SQUARE || shape == CastleShape.RECTANGLE) {
                    val x = if(shape == CastleShape.SQUARE) w*.24f else w*.11f
                    val y = if(shape == CastleShape.SQUARE) h*.24f else h*.34f
                    val right = w-x
                    val bottom = h-y
                    p.moveTo(x,y);p.lineTo(right,y);p.lineTo(right,bottom)
                    p.lineTo(x,bottom);p.close()
                } else {
                    points.forEachIndexed { index, pair ->
                        if(index==0) p.moveTo(pair.first,pair.second)
                        else p.lineTo(pair.first,pair.second)
                    }
                    p.close()
                }
                drawPath(p,color)
                drawPath(p,Color.White.copy(alpha=.88f),style=Stroke(width=3.2f))
            }
        }
    }
}

@Composable
private fun ShapeChoice(shape: CastleShape, active: Boolean = false, enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().heightIn(min = 118.dp)
            .clickable(enabled=enabled, onClick=onClick)
            .semantics { contentDescription = "${shape.display} taşı" },
        shape=RoundedCornerShape(18.dp),
        color=if(active) Color(0xFF397A77) else Color(0xFF245875),
        border=androidx.compose.foundation.BorderStroke(
            if(active) 3.dp else 1.5.dp, if(active) Gold else Mint)
    ) {
        Column(
            Modifier.padding(8.dp),horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.Center
        ) {
            ShapeIcon(shape, if(active) Gold else Color(0xFF70DDEA),
                Modifier.size(72.dp))
            Text(shape.display, color=Color.White,fontSize=13.sp,
                fontWeight=FontWeight.Bold)
        }
    }
}

@Composable
private fun CastleMirrorBoard(task: CastleTask, mask: Int, enabled: Boolean, onToggle: (Int,Int)->Unit) {
    val size = task.gridSize
    val half = size/2
    Column(
        Modifier.fillMaxWidth().padding(vertical=8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) {
            Text("DESEN", color=Mint,fontWeight=FontWeight.Bold,fontSize=11.sp)
            Text("AYNA", color=Gold,fontWeight=FontWeight.Bold,fontSize=11.sp)
            Text("SEN TAMAMLA",color=Mint,fontWeight=FontWeight.Bold,fontSize=11.sp)
        }
        repeat(size) { row ->
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.spacedBy(4.dp)) {
                repeat(size) { col ->
                    val left = col < half
                    val bitIndex = row * half + (if(left) col else col-half)
                    val colored = if(left)
                        task.leftMask and (1 shl bitIndex) != 0
                    else mask and (1 shl bitIndex) != 0
                    Box(
                        Modifier.weight(1f).height(if(size==6) 43.dp else 54.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if(colored) (if(left) Gold else Mint)
                                else if(left) Color(0xFF32566C) else Color(0xFF235A75))
                            .border(1.dp, if(colored) Color.White else Color(0xFF598EA2),
                                RoundedCornerShape(7.dp))
                            .clickable(enabled=!left && enabled) { onToggle(row,col-half) }
                            .semantics {
                                contentDescription=if(left)
                                    "Sol vitray ${row+1}. satır ${col+1}. sütun, " +
                                        (if(colored) "dolu" else "boş")
                                else "Sağ vitray ${row+1}. satır ${col-half+1}. sütun, " +
                                        (if(colored) "dolu" else "boş")
                            },contentAlignment=Alignment.Center
                    ) { if(colored) Text("✦",color=Ink,fontSize=17.sp) }
                }
            }
        }
    }
}

@Composable
fun GeometryCastleScreen(
    store: GeometryCastleProgressStore,
    suggestedTier: LearningTier,
    onExit: () -> Unit,
    onCompleted: (CastleRun) -> Unit
) {
    var run by remember { mutableStateOf(store.load()) }
    var tier by remember { mutableStateOf(suggestedTier) }
    var feedback by remember { mutableStateOf("") }
    var showExit by remember { mutableStateOf(false) }
    var feedbackPositive by remember { mutableStateOf(true) }
    val scroll = rememberScrollState()
    LaunchedEffect(run.room,run.step,run.seed) { scroll.scrollTo(0) }

    fun persist(next: CastleRun): Boolean {
        if (store.save(next)) { run=next;return true }
        feedback="İlerleme kaydedilemedi. Yeniden dene."
        feedbackPositive=false
        return false
    }
    fun submit(correct: Boolean, selected: CastleShape? = null) {
        if(run.solved || run.room == 4) return
        val answered = GeometryCastleEngine.answer(run,correct)
        val next = if(correct) answered.copy(solved=true,selected=selected) else answered
        if(persist(next)) {
            feedbackPositive=correct
            feedback=if(correct) GeometryCastleEngine.task(run)?.explanation ?: "Doğru!"
                else "Şeklin kenarlarını ve konumunu tekrar kontrol et."
        }
    }
    fun next() {
        if(!run.solved) return
        val next = GeometryCastleEngine.advance(run)
        if(persist(next)) {
            feedback=""
            if(next.finished) onCompleted(next)
        }
    }
    BackHandler { if(run.seed>0 && !run.finished) showExit=true else onExit() }
    if(showExit) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest={showExit=false},
            title={Text("Kaleden ayrıl?")},
            text={Text("İlerlemen kaydedildi. Kaldığın yerden devam edebilirsin.")},
            confirmButton={androidx.compose.material3.TextButton(
                onClick={showExit=false;onExit()}){Text("Haritaya dön")}},
            dismissButton={androidx.compose.material3.TextButton(
                onClick={showExit=false}){Text("Devam et")}}
        )
    }
    val landing = run.seed == 0L || run.finished
    val room = if(run.room in 0..3) CastleRoom.entries[run.room] else CastleRoom.CROWN
    val scene = when {
        landing && !run.finished -> R.drawable.geometry_castle_hero
        landing -> R.drawable.geometry_castle_crown
        run.room == 0 -> R.drawable.geometry_castle_courtyard
        run.room == 1 -> R.drawable.geometry_castle_bridge
        run.room == 2 -> R.drawable.geometry_castle_mirror
        else -> R.drawable.geometry_castle_crown
    }
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(
            listOf(Color(0xFF166B94),Color(0xFF0D3659),Color(0xFF09233E))
        )).verticalScroll(scroll).padding(horizontal=16.dp,vertical=12.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick={if(run.seed>0 && !run.finished) showExit=true else onExit()},
            colors=ButtonDefaults.outlinedButtonColors(contentColor=Color.White),
            border=androidx.compose.foundation.BorderStroke(1.dp,Mint),
            modifier=Modifier.heightIn(min=48.dp)
        ) {Text("← Oyunlar")}
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Text("GEOMETRİ KALESİ",color=Gold,fontSize=18.sp,
                fontWeight=FontWeight.Black,letterSpacing=.7.sp)
            Spacer(Modifier.weight(1f))
            Text(if(landing) tier.title else run.tier.title,color=Mint,fontSize=12.sp)
        }
        val done = if(run.finished) 4 else run.room
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
            repeat(4) { i ->
                Box(Modifier.weight(1f).height(8.dp).clip(CircleShape)
                    .background(if(i<done) Gold else Color(0xFF457588)))
            }
        }
        if(landing) {
            CastleScene(scene,if(run.finished) "Tamamlanmış, ışıldayan Geometri Kalesi"
                else "Büyülü geometri kalesi",big=true)
            if(!run.finished) {
                Text("Kayıp Kristaller",color=Gold,fontSize=25.sp,fontWeight=FontWeight.Black)
                Text("Piko'nun kalesini şekiller, köprüler ve simetrik vitraylarla onar.",
                    color=Pale,fontSize=15.sp,lineHeight=23.sp)
                Text("Oyun seviyesini seç",color=Color.White,
                    fontWeight=FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                    LearningTier.entries.forEach { value ->
                        OutlinedButton(
                            modifier=Modifier.weight(1f).heightIn(min=48.dp),
                            onClick={tier=value},
                            border=androidx.compose.foundation.BorderStroke(1.5.dp,
                                if(tier==value) Gold else Mint.copy(alpha=.55f)),
                            colors=ButtonDefaults.outlinedButtonColors(
                                containerColor=if(tier==value) Gold else Panel,
                                contentColor=if(tier==value) Ink else Color.White)
                        ) {Text(value.title,fontSize=11.sp)}
                    }
                }
                Text("4 oda · 12 etkileşimli görev · Çevrimdışı",
                    color=Mint,fontSize=13.sp)
                CastleAction("Kaleye Gir ▶") {
                    val started=store.begin(tier)
                    if(started!=null) {run=started;feedback=""}
                    else feedback="Yeni keşif kaydedilemedi."
                }
            } else {
                Text("Kale yeniden aydınlandı!",color=Gold,fontSize=24.sp,
                    fontWeight=FontWeight.Black)
                Text("12 / 12 görev tamamlandı",color=Mint,fontSize=16.sp)
                Surface(color=Panel,shape=RoundedCornerShape(20.dp),
                    border=androidx.compose.foundation.BorderStroke(1.dp,Gold)) {
                    Column(Modifier.fillMaxWidth().padding(17.dp),
                        verticalArrangement=Arrangement.spacedBy(9.dp)) {
                        Text("ÖĞRENME GÖZLEMİ",fontWeight=FontWeight.Bold,color=Gold)
                        Text("İlk denemede bağımsız: ${run.firstTryCorrect}/12",color=Color.White)
                        Text("Toplam: ${run.attempts} deneme · ${run.errors} yanlış · ${run.hints} ipucu",
                            color=Color.White,fontSize=13.sp)
                        Text("Tamamlanan keşifler: ${run.completedRuns}",
                            color=Mint,fontSize=13.sp)
                        Text("Bu sonuç yalnızca oyun içi davranışları gösterir; resmî kazanımı kanıtlamaz.",
                            color=Pale,fontSize=11.sp)
                    }
                }
                CastleAction("Yeni Kale Macerası ↻") {
                    val started=store.begin(run.tier)
                    if(started!=null) {run=started;feedback=""}
                    else feedback="Yeni keşif kaydedilemedi."
                }
                CastleAction("Haritaya Dön",onClick=onExit)
            }
        } else {
            Text("${room.title} · Görev ${run.step+1}/3",
                color=Gold,fontSize=23.sp,fontWeight=FontWeight.Black)
            Crossfade(targetState=scene,animationSpec=tween(260),
                label="castle chamber") { art ->
                CastleScene(art,room.title+" kale sahnesi")
            }
            val task=GeometryCastleEngine.task(run)
            if(task != null) {
                Text(task.prompt,fontSize=18.sp,lineHeight=25.sp,
                    fontWeight=FontWeight.Bold,color=Color.White)
                when(task.mode) {
                    CastleMode.FIND,CastleMode.BUILD -> {
                        if(task.mode == CastleMode.BUILD) {
                            Text("KÖPRÜ YUVASI — uygun şekli seç, ardından yuvaya yerleştir.",
                                color=Mint,fontSize=12.sp)
                            Surface(
                                modifier=Modifier.fillMaxWidth().heightIn(min=104.dp)
                                    .clickable(enabled=!run.solved && run.selected != null) {
                                        submit(run.selected==task.target,run.selected)
                                    },
                                color=Color(0xFF194C69),
                                shape=RoundedCornerShape(18.dp),
                                border=androidx.compose.foundation.BorderStroke(2.dp,Gold)
                            ) {
                                Row(Modifier.padding(12.dp),
                                    verticalAlignment=Alignment.CenterVertically,
                                    horizontalArrangement=Arrangement.Center) {
                                    if(run.solved)
                                        ShapeIcon(task.target,Gold,Modifier.size(72.dp))
                                    else Text(if(run.selected==null)
                                        "◇  Taş yuvası"
                                        else "✦  Seçtiğin taşı yerleştir",
                                        color=Color.White,fontSize=18.sp,
                                        fontWeight=FontWeight.Bold)
                                }
                            }
                        } else {
                            Text("Doğru şekle dokun.",color=Mint,fontSize=12.sp)
                        }
                        task.options.chunked(2).forEach { options ->
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                                options.forEach { shape ->
                                    Box(Modifier.weight(1f)) {
                                        ShapeChoice(shape,
                                            active=run.selected==shape,
                                            enabled=!run.solved,
                                            onClick={
                                                if(task.mode==CastleMode.FIND) submit(
                                                    shape==task.target,shape
                                                ) else {
                                                    persist(run.copy(selected=shape))
                                                    feedback="Taş seçildi. Şimdi üstteki yuvaya dokun."
                                                    feedbackPositive=true
                                                }
                                            })
                                    }
                                }
                            }
                        }
                    }
                    CastleMode.SYMMETRY -> {
                        Text("Ortadaki çizgi ayna ekseni. Sağ karelere dokunarak vitrayı tamamla.",
                            color=Mint,fontSize=12.sp)
                        CastleMirrorBoard(task,run.mirrorMask,!run.solved) { row,col ->
                            persist(run.copy(mirrorMask=GeometryCastleEngine.toggleMirror(
                                run.mirrorMask,row,col,task.gridSize)))
                        }
                        CastleAction("Vitrayı Kontrol Et",enabled=!run.solved) {
                            submit(GeometryCastleEngine.isCorrect(task,null,run.mirrorMask))
                        }
                    }
                }
                if(!run.solved && !run.hasHintInTask) {
                    OutlinedButton(
                        onClick={
                            val next=GeometryCastleEngine.hint(run)
                            if(persist(next)) {
                                feedbackPositive=true
                                feedback=when(task.mode) {
                                    CastleMode.FIND -> "İpucu: ${task.target.sides} kenarlı şekli, köşe ve kenarlarını sayarak bul."
                                    CastleMode.BUILD -> "İpucu: Yuvanın kenar ve köşe sayısını taşlarla karşılaştır."
                                    CastleMode.SYMMETRY -> "İpucu: Karolar ayna çizgisine aynı uzaklıkta durmalı."
                                }
                            }
                        },
                        colors=ButtonDefaults.outlinedButtonColors(contentColor=Pale),
                        border=androidx.compose.foundation.BorderStroke(1.dp,Mint)
                    ) {Text("💡 İpucu Al")}
                }
                if(run.hasHintInTask && !run.solved) {
                    Text(when(task.mode) {
                        CastleMode.FIND -> "Kenarları ve köşeleri tek tek say."
                        CastleMode.BUILD -> "Hedef yuvayla aynı kenar ve köşe sayısını bul."
                        CastleMode.SYMMETRY -> "Her sol kareyi orta çizgiye göre sağa yansıt."
                    },color=Mint,fontSize=13.sp)
                }
                if(feedback.isNotBlank())
                    Surface(color=if(feedbackPositive) Color(0xFF245E66)
                        else Color(0xFF724C48),
                        shape=RoundedCornerShape(14.dp)) {
                        Text(feedback,Modifier.padding(13.dp),color=Color.White,
                            fontSize=14.sp)
                    }
                if(run.solved) {
                    CastleAction(if(run.room==3 && run.step==2) "Kaleyi Aydınlat ✦"
                        else if(run.step==2) "Sonraki Odaya Geç →"
                        else "Sonraki Görev →") { next() }
                }
                Text("İlk denemede doğru: ${run.firstTryCorrect} · Yanlış: ${run.errors} · İpucu: ${run.hints}",
                    color=Pale.copy(alpha=.85f),fontSize=11.sp)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
