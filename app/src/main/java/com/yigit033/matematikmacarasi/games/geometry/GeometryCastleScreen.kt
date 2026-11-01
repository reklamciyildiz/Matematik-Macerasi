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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
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
    // Shorter decorative scene on small phones: the actual puzzle stays above the fold.
    val compact = LocalConfiguration.current.screenHeightDp < 700
    val imageHeight = if (big) (if (compact) 158.dp else 205.dp)
        else (if (compact) 100.dp else 124.dp)
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

private fun regularPolygon(sides: Int): List<Pair<Float,Float>> =
    (0 until sides).map { i ->
        val angle = -Math.PI / 2 + i * 2 * Math.PI / sides
        Pair(.5f + (kotlin.math.cos(angle) * .38).toFloat(),
            .5f + (kotlin.math.sin(angle) * .38).toFloat())
    }

@Composable
private fun ShapeIcon(shape: CastleShape, color: Color, modifier: Modifier = Modifier,
    outlineOnly: Boolean = false) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val p = Path()
        when(shape) {
            CastleShape.CIRCLE -> {
                // Çember is an outline, not a filled disk (daire).
                drawCircle(if(outlineOnly) Gold else color,
                    radius=minOf(w,h)*.35f,style=Stroke(width=if(outlineOnly) 6f else 7f))
                if(!outlineOnly) drawCircle(Color.White.copy(alpha=.86f),
                    radius=minOf(w,h)*.35f,style=Stroke(width=2.0f))
            }
            else -> {
                // Normalized geometry keeps each new quadrilateral mathematically distinct.
                // Rhombus: four equal sides, no right angles.
                // Parallelogram: two parallel pairs, unequal adjacent sides, oblique angles.
                // Trapezoid: exactly one parallel pair.
                val vertices: List<Pair<Float,Float>> = when(shape) {
                    CastleShape.SQUARE -> listOf(
                        .22f to .22f, .78f to .22f, .78f to .78f, .22f to .78f)
                    CastleShape.RECTANGLE -> listOf(
                        .10f to .34f, .90f to .34f, .90f to .66f, .10f to .66f)
                    CastleShape.RHOMBUS -> listOf(
                        .50f to .25f, .92f to .50f, .50f to .75f, .08f to .50f)
                    CastleShape.PARALLELOGRAM -> listOf(
                        .26f to .28f, .88f to .28f, .74f to .72f, .12f to .72f)
                    CastleShape.TRAPEZOID -> listOf(
                        .34f to .22f, .66f to .22f, .90f to .78f, .10f to .78f)
                    CastleShape.TRIANGLE -> regularPolygon(3)
                    CastleShape.PENTAGON -> regularPolygon(5)
                    CastleShape.HEXAGON -> regularPolygon(6)
                    CastleShape.OCTAGON -> regularPolygon(8)
                    CastleShape.CIRCLE -> emptyList()
                }
                vertices.forEachIndexed { index, vertex ->
                    if(index == 0) p.moveTo(vertex.first * w,vertex.second * h)
                    else p.lineTo(vertex.first * w,vertex.second * h)
                }
                p.close()
                if(!outlineOnly) drawPath(p,color)
                drawPath(p,if(outlineOnly) Gold else Color.White.copy(alpha=.88f),
                    style=Stroke(width=if(outlineOnly) 5f else 3.2f))
            }
        }
    }
}

@Composable
private fun ShapeChoice(shape: CastleShape, active: Boolean = false, enabled: Boolean = true,
    rotationDegrees: Float = 0f, onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue=if(active) 1.035f else 1f,
        animationSpec=tween(170),label="selected geometry stone"
    )
    Surface(
        modifier = Modifier.fillMaxWidth().heightIn(min = 118.dp)
            .graphicsLayer { scaleX=scale; scaleY=scale }
            .clickable(enabled=enabled,role=Role.Button,onClick=onClick)
            .semantics { contentDescription = "${shape.display} şekli, seçmek için dokun" },
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
                Modifier.size(72.dp).graphicsLayer { rotationZ = rotationDegrees })
            // Names stay available to TalkBack but do not reveal a visual question's answer.
            Text("Seç", color=Color.White,fontSize=13.sp,
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
                        Modifier.weight(1f).height(if(size==6) 48.dp else 54.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(if(colored) (if(left) Gold else Mint)
                                else if(left) Color(0xFF32566C) else Color(0xFF235A75))
                            .border(1.dp, if(colored) Color.White else Color(0xFF598EA2),
                                RoundedCornerShape(7.dp))
                            .clickable(enabled=!left && enabled,role=Role.Button) { onToggle(row,col-half) }
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
            if(!correct && GeometryCastleEngine.task(run)?.mode != CastleMode.SYMMETRY) {
                GeometryCastleEngine.task(run)?.let { store.noteWeakShape(it.target) }
            }
            feedbackPositive=correct
            feedback=if(correct) GeometryCastleEngine.task(run)?.explanation ?: "Doğru!"
                else "Şeklin kenarlarını ve konumunu tekrar kontrol et."
        }
    }
    fun placePiece(task: CastleTask) {
        val chosen = run.selected ?: return
        val index = run.placed.size
        if(run.solved || task.mode != CastleMode.BUILD ||
            index !in task.requiredPieces.indices) return
        val correct = GeometryCastleEngine.isCorrectPiece(task,index,chosen)
        val attempted = GeometryCastleEngine.answer(run,correct)
        val next = if(correct) attempted.copy(
            placed=run.placed+chosen,selected=null,
            solved=index+1==task.requiredPieces.size
        ) else attempted
        if(persist(next)) {
            if(!correct) store.noteWeakShape(task.requiredPieces[index])
            feedbackPositive=correct
            feedback=if(correct)
                if(next.solved) "Tüm parçalar yerine oturdu! " + task.explanation
                else "Harika! Şimdi sıradaki siluete uygun bir taş seç."
            else "Bu taş yuvaya uymadı. Kenarların ve köşelerin düzenini karşılaştır."
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
        landing && !run.finished -> R.drawable.geometry_castle_hero_premium
        landing -> R.drawable.geometry_castle_complete_premium
        run.room == 0 -> R.drawable.geometry_castle_courtyard_premium
        run.room == 1 -> R.drawable.geometry_castle_bridge_premium
        run.room == 2 -> R.drawable.geometry_castle_mirror_premium
        else -> R.drawable.geometry_castle_crown_premium
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
                // Piko already appears in the hero. Do not stack a second oversized portrait.
                Text("Kayıp Kristaller",color=Gold,fontSize=24.sp,
                    fontWeight=FontWeight.Black)
                Text("Piko seni bekliyor!",color=Mint,fontSize=13.sp)
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
                Text("Bu seviyede ${GeometryCastleEngine.available(tier).size} farklı şekil var.",
                    color=Pale,fontSize=13.sp)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) {
                    listOf("Avlu","Köprü","Ayna","Taç").forEachIndexed { index, name ->
                        Surface(Modifier.weight(1f),
                            shape=RoundedCornerShape(11.dp),
                            color=Color(0xFF1E5673),
                            border=androidx.compose.foundation.BorderStroke(1.dp,Mint.copy(alpha=.62f))) {
                            Column(Modifier.padding(vertical=8.dp,horizontal=3.dp),
                                horizontalAlignment=Alignment.CenterHorizontally) {
                                Text(listOf("◇","▰","✧","♛")[index],fontSize=21.sp,color=Gold)
                                Text(name,fontSize=11.sp,color=Color.White)
                            }
                        }
                    }
                }
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
                        listOf(
                            "Şekil tanıma" to "geometry_classify",
                            "Şekillerle inşa" to "geometry_compose",
                            "Simetriyi tanıma" to "geometry_symmetry_recognize",
                            "Simetri oluşturma" to "geometry_symmetry_build"
                        ).forEach { (title,skill) ->
                            val (done,wrong,help) = store.skillEvidence(skill)
                            if(done>0) Text("$title: $done görev · $wrong yanlış · $help ipucu",
                                color=Pale,fontSize=12.sp)
                        }
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
            Text("${room.title} · Görev ${run.step+1}/3 · ${run.room*3+run.step+1}/12",
                color=Gold,fontSize=21.sp,fontWeight=FontWeight.Black)
            Crossfade(targetState=scene,animationSpec=tween(260),
                label="castle chamber") { art ->
                CastleScene(art,room.title+" kale sahnesi")
            }
            val task=GeometryCastleEngine.task(run)
            if(task != null) {
                Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                    Image(painter=painterResource(R.drawable.geometry_piko_premium),
                        contentDescription=null,modifier=Modifier.size(42.dp))
                    Text(task.prompt,fontSize=18.sp,lineHeight=25.sp,
                        fontWeight=FontWeight.Bold,color=Color.White,
                        modifier=Modifier.weight(1f))
                }
                when(task.mode) {
                    CastleMode.FIND,CastleMode.BUILD -> {
                        if(task.mode == CastleMode.BUILD) {
                            val pieces=task.requiredPieces
                            Text("KÖPRÜ ATÖLYESİ · ${run.placed.size}/${pieces.size} taş yerleştirildi",
                                color=Mint,fontSize=13.sp,fontWeight=FontWeight.Bold)
                            Text(if(pieces.size==1)
                                "Siluete uygun şekli seç ve yuvaya dokun."
                                else "Köprüdeki ${pieces.size} yuvayı sırayla doldur. Döndürülmüş şekilleri de tanı.",
                                color=Pale,fontSize=12.sp)
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement=Arrangement.spacedBy(7.dp)) {
                                pieces.forEachIndexed { slot,targetShape ->
                                    val filled = slot < run.placed.size ||
                                        (run.contentVersion==1 && run.solved)
                                    val activeSlot = slot == run.placed.size && !run.solved
                                    Surface(
                                        modifier=Modifier.weight(1f).heightIn(min=114.dp)
                                            .clickable(enabled=activeSlot && run.selected!=null,
                                                role=Role.Button) { placePiece(task) }
                                            .semantics {
                                                contentDescription=when {
                                                    filled -> "${slot+1}. köprü yuvası tamamlandı"
                                                    activeSlot -> "${slot+1}. köprü yuvası, taşı seçip yerleştir"
                                                    else -> "${slot+1}. köprü yuvası kilitli"
                                                }
                                            },
                                        color=if(filled) Color(0xFF316D65)
                                            else Color(0xFF194C69),
                                        shape=RoundedCornerShape(17.dp),
                                        border=androidx.compose.foundation.BorderStroke(
                                            if(activeSlot) 2.dp else 1.dp,
                                            if(activeSlot) Gold else Mint.copy(alpha=.55f))
                                    ) {
                                        Column(Modifier.padding(7.dp),
                                            verticalArrangement=Arrangement.Center,
                                            horizontalAlignment=Alignment.CenterHorizontally) {
                                            ShapeIcon(targetShape,Gold,
                                                Modifier.size(if(pieces.size==3) 55.dp else 66.dp),
                                                outlineOnly=!filled)
                                            Text(if(filled) "✓ ${slot+1}" else "${slot+1}. yuva",
                                                color=if(filled) Mint else Color.White,
                                                fontSize=12.sp,fontWeight=FontWeight.Bold)
                                        }
                                    }
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
                                            rotationDegrees=if(run.contentVersion>=2 &&
                                                run.tier!=LearningTier.TEMEL)
                                                (((run.room*3+run.step)+shape.ordinal)%4)*45f
                                                else 0f,
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
                val currentFeedback = if(run.solved && feedback.isBlank())
                    task.explanation else feedback
                if(currentFeedback.isNotBlank())
                    Surface(color=if(run.solved || feedbackPositive) Color(0xFF245E66)
                        else Color(0xFF724C48),
                        shape=RoundedCornerShape(14.dp)) {
                        Text(currentFeedback,Modifier.padding(13.dp),color=Color.White,
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
