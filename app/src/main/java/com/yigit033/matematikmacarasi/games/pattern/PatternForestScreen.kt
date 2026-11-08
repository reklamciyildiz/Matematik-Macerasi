package com.yigit033.matematikmacarasi.games.pattern

import com.yigit033.matematikmacarasi.games.common.AutoAdvanceOnSuccess
import com.yigit033.matematikmacarasi.games.common.PremiumChallengePause
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import com.yigit033.matematikmacarasi.services.payments.PremiumGameModePolicy
import com.yigit033.matematikmacarasi.screens.SettingsActivity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.services.AudioService

@Composable
private fun RuneSequence(task:PatternTask,run:PatternRun,
    enabled:Boolean,onSpot:(Int)->Unit,
    onTargetBounds:(Rect)->Unit) {
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(9.dp)) {
        Text(if(task.mechanic==PatternMechanic.SPOT)
            "Hatalı olan taşı işaretle" else "ORMANIN İZİ",
            color=ForestPalette.jade,fontSize=12.sp,fontWeight=FontWeight.Black)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement=Arrangement.spacedBy(7.dp),
            verticalAlignment=Alignment.CenterVertically) {
            task.shown.forEachIndexed { position,tile->
                if(position==task.missingIndex) {
                    Box(Modifier.width(73.dp).onGloballyPositioned{
                        onTargetBounds(it.boundsInRoot())
                    }) {
                        val chosen=task.options.getOrNull(run.picked)
                        if(chosen!=null)RuneTile(chosen,modifier=Modifier.fillMaxWidth(),
                            selected=true)
                        else EmptyRuneSlot(Modifier.fillMaxWidth(),highlight=true)
                    }
                } else {
                    RuneTile(tile,modifier=Modifier.width(73.dp),
                        selected=task.mechanic==PatternMechanic.SPOT && run.picked==position,
                        enabled=enabled,
                        onClick=if(task.mechanic==PatternMechanic.SPOT)
                            ({onSpot(position)}) else null)
                }
            }
            if(task.mechanic==PatternMechanic.COMPLETE)
                EmptyRuneSlot(Modifier.width(73.dp),highlight=true)
            if(task.mechanic==PatternMechanic.ORDER ||
                task.mechanic==PatternMechanic.CREATE) {
                repeat(3) { i ->
                    val tile=task.options.getOrNull(run.selected.getOrNull(i)?:-1)
                    if(tile==null)EmptyRuneSlot(Modifier.width(73.dp),
                        label=(i+1).toString())
                    else RuneTile(tile,modifier=Modifier.width(73.dp),selected=true)
                }
            }
        }
    }
}

/** Real drop target collision, with tap as accessible fallback. */
@Composable
private fun RuneOption(
    index:Int,tile:PatternTile,selected:Boolean,
    allowDrag:Boolean,targetBounds:Rect?,enabled:Boolean,
    onPick:(Int)->Unit,modifier:Modifier=Modifier
) {
    var localRect by remember { mutableStateOf<Rect?>(null) }
    var origin by remember { mutableStateOf(Offset.Zero) }
    var shift by remember { mutableStateOf(Offset.Zero) }
    var dragging by remember { mutableStateOf(false) }
    val gesture=if(allowDrag && enabled) Modifier
        .onGloballyPositioned { localRect=it.boundsInRoot() }
        .pointerInput(index,targetBounds) {
            detectDragGesturesAfterLongPress(
                onDragStart={ start->
                    origin=(localRect?.topLeft ?:Offset.Zero)+start
                    dragging=true;shift=Offset.Zero
                },
                onDrag={change,delta->
                    shift+=delta;change.consume()
                },
                onDragEnd={
                    if(targetBounds?.contains(origin+shift)==true)onPick(index)
                    dragging=false;shift=Offset.Zero
                },
                onDragCancel={ dragging=false;shift=Offset.Zero }
            )
        } else Modifier
    Box(modifier.zIndex(if(dragging)20f else 0f).then(gesture)
        .graphicsLayer(translationX=shift.x,translationY=shift.y)) {
        RuneTile(tile,modifier=Modifier.fillMaxWidth(),
            selected=selected||dragging,enabled=enabled,
            showLabel=true,onClick={onPick(index)})
    }
}

@Composable
private fun OptionGrid(task:PatternTask,run:PatternRun,
    enabled:Boolean,dragTarget:Rect?,onPick:(Int)->Unit) {
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(9.dp)) {
        Text(when(task.mechanic) {
            PatternMechanic.ORDER->"TAŞLARI SIRAYLA SEÇ"
            PatternMechanic.CREATE->"ÜÇ YENİ TAŞ OLUŞTUR"
            PatternMechanic.DRAG->"TAŞI BOŞ YUVAYA SÜRÜKLE VEYA DOKUN"
            else->"TAŞINI SEÇ"
        },color=ForestPalette.gold,fontSize=12.sp,fontWeight=FontWeight.Black)
        task.options.withIndex().toList().chunked(3).forEach { line ->
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                for((index,tile) in line){
                    RuneOption(
                        index,tile,
                        selected=if(task.mechanic==PatternMechanic.ORDER ||
                            task.mechanic==PatternMechanic.CREATE)
                                index in run.selected else run.picked==index,
                        allowDrag=task.mechanic==PatternMechanic.DRAG,
                        targetBounds=dragTarget,enabled=enabled &&
                            (task.mechanic!=PatternMechanic.ORDER ||
                                index !in run.selected),
                        onPick=onPick,modifier=Modifier.weight(1f))
                }
                repeat(3-line.size){Spacer(Modifier.weight(1f))}
            }
        }
    }
}

@Composable
private fun RuleOptions(task:PatternTask,run:PatternRun,
    enabled:Boolean,onPick:(Int)->Unit) {
    Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(8.dp)) {
        task.ruleChoices.forEachIndexed { i,label->
            Surface(modifier=Modifier.fillMaxWidth()
                .clickable(enabled=enabled){onPick(i)},
                shape=RoundedCornerShape(14.dp),
                color=if(i==run.picked)Color(0xFF367A8A) else Color(0xFF1D4C69),
                border=BorderStroke(if(i==run.picked)2.dp else 1.dp,
                    if(i==run.picked)ForestPalette.gold
                    else ForestPalette.jade.copy(alpha=.45f))) {
                Row(Modifier.padding(14.dp),
                    verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                    Text(listOf("A","B","C","D")[i],color=ForestPalette.gold,
                        fontWeight=FontWeight.Black,fontSize=20.sp)
                    Text(label,color=ForestPalette.cream,
                        fontWeight=FontWeight.Bold,fontSize=14.sp)
                }
            }
        }
    }
}

@Composable
fun PatternForestScreen(store:PatternForestStore,profileGrade:Int,
    onExit:()->Unit,onCompleted:(PatternRun)->Unit) {
    val context=LocalContext.current
    val audio=remember(context){AudioService.getInstance(context)}
    val purchaseManager=remember(context){PurchaseManager.getInstance(context)}
    val premiumFlow by purchaseManager.premiumState.collectAsState()
    val hasPremium=premiumFlow || purchaseManager.isPremiumSync()
    fun openPremiumSettings() {
        context.startActivity(Intent(context,SettingsActivity::class.java))
    }
    var run by remember { mutableStateOf(store.load()) }
    var difficulty by rememberSaveable { mutableIntStateOf(
        if(profileGrade<=2)0 else 1) }
    var message by remember { mutableStateOf("") }
    var positive by remember { mutableStateOf(true) }
    var restart by remember { mutableStateOf(false) }

    var showFreeBasic by remember { mutableStateOf(false) }
    if(run.seed>0L && !run.finished &&
        !PremiumGameModePolicy.canStart("pattern_forest",run.difficulty,hasPremium)) {
        if(showFreeBasic) {
            // The old Medium/Hard checkpoint stays in the original store.
            // A completely separate Basic slot remains playable for Free users.
            val basicStore=remember(context) {
                PatternForestStore(context,PremiumGameModePolicy.freeCheckpointStore("pattern_forest"))
            }
            PatternForestScreen(
                store=basicStore,profileGrade=profileGrade,
                onExit=onExit,onCompleted=onCompleted
            )
        } else {
            PremiumChallengePause(onSettings={openPremiumSettings()},
                onExit=onExit,onPlayBasic={showFreeBasic=true})
        }
        return
    }
    var dragBounds by remember(run.seed,run.index) {
        mutableStateOf<Rect?>(null)
    }
    val task=remember(run.seed,run.index,run.grade,run.difficulty) {
        store.challenge(run)
    }
    val scroll=rememberScrollState()
    BackHandler {onExit()}

    fun persist(next:PatternRun):Boolean {
        if(store.save(next)){run=next;return true}
        positive=false;message=store.lastError.ifBlank{
            "Kayıt başarısız. Bu hamleyi tekrar deneyebilirsin."}
        return false
    }

    fun choose(index:Int) {
        val t=task?:return
        if(run.solved || run.finished)return
        if(t.mechanic==PatternMechanic.ORDER ||
            t.mechanic==PatternMechanic.CREATE) {
            if(run.selected.size>=3)return
            if(t.mechanic==PatternMechanic.ORDER && index in run.selected)return
            if(persist(run.copy(picked=-1,order=
                (run.selected+index).joinToString(",")))) {
                audio.playCoinSound()
                message=""
            }
        }else {
            if(persist(run.copy(picked=index,order=""))) {
                audio.playTapSound();message=""
            }
        }
    }
    fun undo() {
        if(run.selected.isEmpty() || run.solved)return
        if(persist(run.copy(order=run.selected.dropLast(1).joinToString(",")))) {
            audio.playTapSound();message=""
        }
    }
    fun clear() {
        if(run.solved)return
        if(persist(run.copy(order="",picked=-1)))message=""
    }
    fun submit() {
        val t=task?:return
        if(run.solved || run.finished)return
        if(!PatternForestEngine.validSelection(t,run.picked,run.selected)){
            positive=false;message="Seçimini kontrol et ve tekrar dene.";return
        }
        val ready=when(t.mechanic) {
            PatternMechanic.ORDER,PatternMechanic.CREATE->run.selected.size==3
            else->run.picked>=0
        }
        if(!ready){
            positive=false
            message=if(t.mechanic==PatternMechanic.ORDER ||
                t.mechanic==PatternMechanic.CREATE)
                    "Önce üç taşı doğru sırayla yerleştir."
                else "Önce bir taş veya kural seç."
            return
        }
        val correct=PatternForestEngine.correct(t,run.picked,run.selected)
        val updated=run.copy(
            solved=correct,attempts=run.attempts+1,
            errors=run.errors+if(correct)0 else 1,
            hadError=run.hadError||!correct
        )
        if(persist(updated)){
            positive=correct
            message=if(correct)"✦ Ormanın enerjisi geri dönüyor!  "+t.explanation
                else "Bu sefer olmadı. "+when(t.world) {
                    PatternWorld.COLORS->"Tekrar eden renk grubuna bak."
                    PatternWorld.SHAPES->"Şekilleri sırayla izle."
                    PatternWorld.NUMBERS->"İki sayı arasındaki farkı incele."
                    PatternWorld.MOTION->"Yön ve boyutun nasıl değiştiğini kontrol et."
                    PatternWorld.RULES->"Kuralı birden fazla adıma uygula."
                }
            if(correct)audio.playCorrectSound() else audio.playWrongSound()
        }
    }
    fun next() {
        val advanced=store.advance(run)
        if(advanced!=null){
            run=advanced;message=""
            if(advanced.finished){
                audio.playVictorySound()
                onCompleted(advanced)
            }else if(advanced.index%6==0)audio.playLevelCompleteSound()
            else audio.playCoinSound()
        } else {
            positive=false
            message=store.lastError.ifBlank{"Sonraki görev kaydedilemedi."}
        }
    }
    fun start() {
        if(!PremiumGameModePolicy.canStart("pattern_forest",difficulty,hasPremium)) {
            openPremiumSettings()
            return
        }
        val next=store.start(profileGrade,difficulty)
        if(next!=null) {
            run=next;message="";audio.playQuestCompleteSound()
        }else{
            positive=false;message=store.lastError.ifBlank{"Macera başlatılamadı."}
        }
    }

    if(restart){
        AlertDialog(onDismissRequest={restart=false},
            title={Text("Yeni örüntü macerası?")},
            text={Text("Yarım kalan görevlerin yerine yeni örüntüler üretilecek. Daha önce kazandığın yıldızlar korunur.")},
            confirmButton={TextButton(onClick={restart=false;start()}){
                Text("YENİ MACERA")}},
            dismissButton={TextButton(onClick={restart=false}){
                Text("VAZGEÇ")}})
    }
    AutoAdvanceOnSuccess(run.seed, run.index,
        run.seed > 0L && run.solved && !run.finished) { next() }
    LaunchedEffect(run.seed,run.index){scroll.scrollTo(0)}
    Column(Modifier.fillMaxSize()
        .background(Brush.verticalGradient(listOf(
            Color(0xFF148AA6),Color(0xFF166B7E),ForestPalette.deep)))
        .verticalScroll(scroll).padding(horizontal=13.dp,vertical=9.dp),
        verticalArrangement=Arrangement.spacedBy(13.dp)) {
        Row(Modifier.fillMaxWidth(),
            verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.SpaceBetween) {
            TextButton(onClick=onExit){
                Text("← Oyunlar",color=ForestPalette.cream,
                    fontWeight=FontWeight.Bold)
            }
            if(run.seed>0 && !run.finished)
                TextButton(onClick={restart=true}){
                    Text("↻ Yeni macera",color=ForestPalette.gold,
                        fontWeight=FontWeight.Bold)
                }
        }
        if(run.seed==0L||run.finished) {
            Box(Modifier.fillMaxWidth().height(260.dp)
                .clip(RoundedCornerShape(23.dp))) {
                Image(painterResource(R.drawable.pattern_forest_hero),
                    contentDescription="Işıltılı ağaçlar, örüntü taşı patikası ve büyülü orman kapısı",
                    contentScale=ContentScale.Crop,
                    modifier=Modifier.fillMaxSize())
                Box(Modifier.fillMaxSize().background(
                    Brush.verticalGradient(listOf(
                        Color.Transparent,ForestPalette.deep.copy(alpha=.94f)))))
                Column(Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                    Text("🌿 ÖRÜNTÜ ORMANI",color=ForestPalette.gold,
                        fontSize=25.sp,fontWeight=FontWeight.Black)
                    Text("Ritmin İzinde  ·  5 Dünya  ·  30 Görev",
                        color=ForestPalette.cream,
                        fontSize=13.sp,fontWeight=FontWeight.Bold)
                }
            }
            if(run.finished) {
                ForestPanel {
                    Text("🏆 KURAL KRİSTALİ UYANDI!",
                        color=ForestPalette.gold,fontSize=21.sp,
                        fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
                    Text("30/30 görev tamamlandı",
                        color=ForestPalette.jade,fontWeight=FontWeight.Black)
                    Text("İpucusuz ilk denemede: "+run.clean+"/30",
                        color=ForestPalette.cream,fontWeight=FontWeight.Bold)
                    Text(run.errors.toString()+" yanlış · "+run.hints+
                        " ipucu · En uzun seri "+run.bestStreak,
                        color=ForestPalette.mist,fontSize=13.sp)
                    LinearProgressIndicator(progress={run.clean/30f},
                        color=ForestPalette.gold,
                        trackColor=Color(0xFF315D77),
                        modifier=Modifier.fillMaxWidth().height(10.dp)
                            .clip(RoundedCornerShape(9.dp)))
                    PatternWorld.entries.forEach { w ->
                        Text(w.symbol+" "+w.title+": "+
                            store.completedFor(w.skill)+" görev, "+
                            store.cleanFor(w.skill)+" bağımsız doğru",
                            color=ForestPalette.cream,fontSize=12.sp)
                    }
                    Text("En iyi bağımsız sonuç: "+store.bestClean()+"/30",
                        color=ForestPalette.gold,fontWeight=FontWeight.Bold)
                    Text("Bu sonuçlar oyun içi gözlemdir; resmi yeterlilik belgesi değildir.",
                        color=ForestPalette.mist,fontSize=11.sp,
                        textAlign=TextAlign.Center)
                }
            }else{
                ForestPanel {
                    Text("Beş ormanın kaybolan ritmini bul!",
                        color=ForestPalette.cream,fontSize=18.sp,
                        fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
                    Text("Renkler • Şekiller • Sayılar • Yönler • Gizli Kurallar",
                        color=ForestPalette.jade,fontSize=13.sp,
                        textAlign=TextAlign.Center)
                    Text(if(profileGrade<=2)
                        "Sınıf "+profileGrade+": somut ve görsel örüntüler"
                        else "Sınıf "+profileGrade+": örüntü kuralı ve matematiksel akıl yürütme",
                        color=ForestPalette.mist,fontSize=12.sp,
                        textAlign=TextAlign.Center)
                    Text("Zamana karşı yarış yok; düşünerek çöz.",
                        color=ForestPalette.gold,fontSize=12.sp)
                }
            }
            ChapterStrip(if(run.finished)30 else 0)
            Text("ZORLUK SEÇ",color=ForestPalette.gold,
                fontWeight=FontWeight.Black)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                PatternForestEngine.difficultyLabels.forEachIndexed { i,label ->
                    val locked=!PremiumGameModePolicy.canStart("pattern_forest",i,hasPremium)
                    Surface(modifier=Modifier.weight(1f).heightIn(min=54.dp)
                        .clickable{if(locked)openPremiumSettings() else {
                            difficulty=i;audio.playTapSound()
                        }},
                        shape=RoundedCornerShape(15.dp),
                        color=if(i==difficulty)Color(0xFF387C84) else Color(0xFF205477),
                        border=BorderStroke(if(i==difficulty)2.dp else 1.dp,
                            if(i==difficulty)ForestPalette.gold
                            else ForestPalette.jade.copy(alpha=.6f))) {
                        Box(Modifier.padding(8.dp),contentAlignment=Alignment.Center) {
                            Text(if(locked)"🔒 "+label else label,color=ForestPalette.cream,
                                fontWeight=FontWeight.Black)
                        }
                    }
                }
            }
            ForestButton(if(run.finished)"↻ YENİ MACERA" else "▶ MACERAYA BAŞLA",
                modifier=Modifier.fillMaxWidth(),onClick=::start)
        }else if(task!=null) {
            Text("🌿 ÖRÜNTÜ ORMANI",
                color=ForestPalette.gold,fontSize=22.sp,
                fontWeight=FontWeight.Black)
            Row(Modifier.fillMaxWidth(),
                horizontalArrangement=Arrangement.SpaceBetween){
                Text(run.grade.toString()+". sınıf · "+
                    PatternForestEngine.difficultyLabels[run.difficulty],
                    color=ForestPalette.jade,fontSize=12.sp,
                    fontWeight=FontWeight.Bold)
                Text("GÖREV "+(run.index+1)+"/30",
                    color=ForestPalette.gold,fontSize=13.sp,
                    fontWeight=FontWeight.Black)
            }
            LinearProgressIndicator(progress={run.index/30f},
                color=ForestPalette.gold,trackColor=Color(0xFF2E6777),
                modifier=Modifier.fillMaxWidth().height(10.dp)
                    .clip(RoundedCornerShape(8.dp)))
            ChapterStrip(run.index)
            ForestPanel {
                Text(task.world.symbol+" "+task.world.title.uppercase(),
                    color=ForestPalette.gold,fontSize=17.sp,
                    fontWeight=FontWeight.Black)
                Text(task.mechanic.title,color=ForestPalette.cream,
                    fontSize=19.sp,fontWeight=FontWeight.Black,
                    textAlign=TextAlign.Center)
                Text(task.instruction,color=ForestPalette.mist,
                    fontSize=15.sp,lineHeight=22.sp,
                    textAlign=TextAlign.Center)
                RuneSequence(task,run,enabled=!run.solved,
                    onSpot=::choose,onTargetBounds={dragBounds=it})
                when(task.mechanic) {
                    PatternMechanic.SPOT -> {
                        Text("Yukarıdaki dizide yanlış giden taşı seç.",
                            color=ForestPalette.jade,fontSize=12.sp)
                    }
                    PatternMechanic.RULE ->
                        RuleOptions(task,run,!run.solved,onPick=::choose)
                    else -> OptionGrid(task,run,!run.solved,dragBounds,onPick=::choose)
                }
                if(!run.solved) {
                    Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                        if(run.selected.isNotEmpty()) {
                            OutlinedButton(onClick=::undo,modifier=Modifier.weight(1f),
                                border=BorderStroke(1.dp,ForestPalette.jade)) {
                                Text("↶ Geri al",color=ForestPalette.cream)
                            }
                        }
                        if(run.picked>=0 || run.selected.isNotEmpty()) {
                            OutlinedButton(onClick=::clear,modifier=Modifier.weight(1f),
                                border=BorderStroke(1.dp,ForestPalette.jade)) {
                                Text("↺ Temizle",color=ForestPalette.cream)
                            }
                        }
                    }
                    ForestButton("✦ CEVABI KONTROL ET",
                        modifier=Modifier.fillMaxWidth(),onClick=::submit)
                }
            }
            AnimatedVisibility(message.isNotBlank()) {
                Surface(shape=RoundedCornerShape(16.dp),
                    color=if(positive)Color(0xFF156A66) else Color(0xFF784766),
                    border=BorderStroke(1.dp,ForestPalette.jade.copy(alpha=.55f))) {
                    Text(message,Modifier.fillMaxWidth().padding(15.dp),
                        color=ForestPalette.cream,fontSize=13.sp,
                        fontWeight=FontWeight.Bold,lineHeight=20.sp)
                }
            }
            if(run.solved) {
                Text("✓ Örüntü tamamlandı · Otomatik ilerleniyor…",
                    color=ForestPalette.jade,fontWeight=FontWeight.Bold)
                if(!positive) ForestButton("İlerlemeyi yeniden dene",
                    modifier=Modifier.fillMaxWidth(),onClick=::next)
            }else if(!run.usedHint) {
                OutlinedButton(
                    onClick={
                        if(persist(run.copy(hints=run.hints+1,usedHint=true))) {
                            positive=true
                            message="💡 "+when(task.world) {
                                PatternWorld.COLORS->"Tekrar eden renk grubunu bir kutu gibi düşün."
                                PatternWorld.SHAPES->"Şekil dizisinin başı nerede yeniden başlıyor?"
                                PatternWorld.NUMBERS->"Her adımda kaç arttığını veya azaldığını say."
                                PatternWorld.MOTION->"Yönün veya boyutun değişimini yavaşça izle."
                                PatternWorld.RULES->"Önerdiğin kuralı dizideki başka adımda dene."
                            }
                            audio.playTapSound()
                        }
                    },modifier=Modifier.fillMaxWidth(),
                    border=BorderStroke(1.dp,ForestPalette.jade)) {
                    Text("💡 Küçük bir ipucu al",color=ForestPalette.cream)
                }
            }
            Text("Süre baskısı yok · Her hamle kaydedilir · Yeni oyunda yeni kurallar",
                color=ForestPalette.mist,fontSize=11.sp,textAlign=TextAlign.Center,
                modifier=Modifier.fillMaxWidth())
        }
        if((run.seed==0L || run.finished)&&message.isNotBlank()) {
            Text(message,color=ForestPalette.gold,fontSize=13.sp)
        }
        Spacer(Modifier.height(18.dp))
    }
}
