package com.yigit033.matematikmacarasi.games.time

import com.yigit033.matematikmacarasi.games.common.AutoAdvanceOnSuccess
import com.yigit033.matematikmacarasi.games.common.PremiumChallengePause
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import com.yigit033.matematikmacarasi.services.payments.PremiumGameModePolicy
import com.yigit033.matematikmacarasi.screens.SettingsActivity
import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.services.AudioService
import kotlin.math.roundToInt

@Composable
private fun GoldAction(title:String,enabled:Boolean=true,modifier:Modifier=Modifier,
    onClick:()->Unit) {
    Button(onClick=onClick,enabled=enabled,
        modifier=modifier.fillMaxWidth().heightIn(min=56.dp),
        shape=RoundedCornerShape(17.dp),
        colors=ButtonDefaults.buttonColors(containerColor=TimeStyle.gold,
            contentColor=TimeStyle.ink,disabledContainerColor=Color(0xFF849CB3)),
        contentPadding=PaddingValues(8.dp)) {
        Text(title,fontWeight=FontWeight.Black,fontSize=15.sp,textAlign=TextAlign.Center)
    }
}

@Composable
private fun ChapterRibbon(progress:Int) {
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement=Arrangement.spacedBy(7.dp)) {
        TimeWorld.entries.forEachIndexed { index, world ->
            val started=progress>=index*5
            val finished=progress>=(index+1)*5
            Surface(shape=RoundedCornerShape(13.dp),
                color=if(finished)Color(0xFF246C84)
                else if(started)Color(0xFF325D99) else Color(0xFF152A54),
                border=BorderStroke(1.dp,if(started)TimeStyle.gold else Color(0xFF48668C))) {
                Column(Modifier.width(100.dp).padding(9.dp),
                    horizontalAlignment=Alignment.CenterHorizontally) {
                    Text(if(finished)"✓ ${world.icon}" else "${world.icon}  ${index+1}",
                        color=TimeStyle.gold,fontSize=17.sp,fontWeight=FontWeight.Black)
                    Text(world.title,color=TimeStyle.white,fontSize=10.sp,
                        fontWeight=FontWeight.Bold,maxLines=2,textAlign=TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun ClockAdjustControls(
    minute:Int,step:Int,enabled:Boolean,
    onChange:(Int)->Unit
) {
    val buttons=listOf(
        "−1 saat" to -60,
        "+1 saat" to 60,
        "−$step dk" to -step,
        "+$step dk" to step
    )
    Column(verticalArrangement=Arrangement.spacedBy(9.dp)) {
        buttons.chunked(2).forEach { row->
            Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                row.forEach{ (label,delta)->
                    TimeTile(label,enabled=enabled,modifier=Modifier.weight(1f)) {
                        val next=((minute+delta)%1440+1440)%1440
                        if(next!=minute)onChange(next)
                    }
                }
            }
        }
        Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
            TimeTile("☀ ÖÖ",selected=minute<720,enabled=enabled,
                modifier=Modifier.weight(1f)) {
                if(minute>=720)onChange(minute-720)
            }
            TimeTile("🌙 ÖS",selected=minute>=720,enabled=enabled,
                modifier=Modifier.weight(1f)) {
                if(minute<720)onChange(minute+720)
            }
        }
    }
}

@Composable
fun TimeTowerScreen(
    store:TimeTowerStore,
    profileGrade:Int,
    onExit:()->Unit,
    onCompleted:(TowerRun)->Unit
) {
    val context=LocalContext.current
    val audio=remember(context){AudioService.getInstance(context)}
    val purchaseManager=remember(context){PurchaseManager.getInstance(context)}
    val premiumFlow by purchaseManager.premiumState.collectAsState()
    val hasPremium=premiumFlow || purchaseManager.isPremiumSync()
    fun openPremiumSettings() {
        context.startActivity(Intent(context,SettingsActivity::class.java))
    }
    var run by remember { mutableStateOf(store.load()) }
    var choiceDifficulty by rememberSaveable {
        mutableIntStateOf(PremiumGameModePolicy.defaultDifficulty(profileGrade,hasPremium))
    }
    LaunchedEffect(hasPremium) {
        if(!hasPremium)choiceDifficulty=0
    }
    var message by remember { mutableStateOf("") }
    var success by remember { mutableStateOf(true) }
    var confirmRestart by remember { mutableStateOf(false) }
    var selectedHand by remember(run.seed,run.index) { mutableStateOf(ClockHand.MINUTE) }
    var showFreeBasic by remember { mutableStateOf(false) }
    if(run.seed>0L && !run.finished &&
        !PremiumGameModePolicy.canStart("time_tower",run.difficulty,hasPremium)) {
        if(showFreeBasic) {
            // The old Medium/Hard checkpoint stays in the original store.
            // A completely separate Basic slot remains playable for Free users.
            val basicStore=remember(context) {
                TimeTowerStore(context,PremiumGameModePolicy.freeCheckpointStore("time_tower"))
            }
            TimeTowerScreen(
                store=basicStore,profileGrade=profileGrade,
                onExit=onExit,onCompleted=onCompleted
            )
        } else {
            PremiumChallengePause(onSettings={openPremiumSettings()},
                onExit=onExit,onPlayBasic={showFreeBasic=true})
        }
        return
    }

    val scroll=rememberScrollState()
    val challenge=remember(run.seed,run.index,run.grade,run.difficulty) {
        store.challenge(run)
    }
    BackHandler{onExit()}

    fun persist(next:TowerRun):Boolean {
        if(store.save(next)){
            run=next
            return true
        }
        success=false
        message=store.lastError.ifBlank{"Kayıt başarısız oldu. Yeniden deneyebilirsin."}
        return false
    }
    fun setMinute(value:Int) {
        if(challenge==null || run.solved || value==run.cursorMinutes)return
        if(persist(run.copy(cursorMinutes=value))) {
            audio.playTapSound()
            message=""
        }
    }
    fun setDate(value:Int) {
        if(challenge==null || run.solved || value==run.chosenDate)return
        if(persist(run.copy(chosenDate=value))) {
            audio.playTapSound()
            message=""
        }
    }
    fun pushEvent(id:Int) {
        if(challenge==null || run.solved || id in run.routeIds)return
        if(persist(run.copy(route=(run.routeIds+id).joinToString(",")))) {
            audio.playCoinSound()
            message=""
        }
    }
    fun undoEvent() {
        if(run.routeIds.isEmpty() || run.solved)return
        if(persist(run.copy(route=run.routeIds.dropLast(1).joinToString(","))))
            audio.playTapSound()
    }
    fun submit() {
        val task=challenge?:return
        if(run.solved || run.finished)return
        when(task.world) {
            TimeWorld.CALENDAR->if(run.chosenDate==0){
                success=false;message="Önce takvimden bir tarih seç.";return
            }
            TimeWorld.ROUTE->if(run.routeIds.size!=task.events.size){
                success=false;message="Önce tüm etkinlikleri yolculuk sırasına yerleştir.";return
            }
            else->{}
        }
        val correct=TimeTowerEngine.answerCorrect(task,run.cursorMinutes,
            run.chosenDate,run.routeIds)
        val next=run.copy(
            attempts=run.attempts+1,
            errors=run.errors+if(correct)0 else 1,
            hadError=run.hadError||!correct,
            solved=correct,
            streak=if(correct)run.streak else 0
        )
        if(persist(next)){
            success=correct
            message=if(correct)"✦ Kapı açıldı!  ${task.explanation}"
                else "Bu kez olmadı. ${TimeTowerEngine.hint(task)}"
            if(correct)audio.playCorrectSound() else audio.playWrongSound()
        }
    }
    fun next() {
        val previous=run
        val advanced=store.advance(previous)
        if(advanced!=null) {
            run=advanced;message=""
            if(advanced.finished){
                audio.playVictorySound()
                onCompleted(advanced)
            } else if(advanced.index%5==0) {
                audio.playLevelCompleteSound()
            } else audio.playCoinSound()
        } else {
            success=false
            message=store.lastError.ifBlank{"İlerleme kaydedilemedi."}
        }
    }
    fun begin() {
        if(!PremiumGameModePolicy.canStart("time_tower",choiceDifficulty,hasPremium)) {
            openPremiumSettings()
            return
        }
        val fresh=store.start(profileGrade,choiceDifficulty)
        if(fresh!=null) {
            run=fresh;message=""
            audio.playQuestCompleteSound()
        } else {
            success=false
            message=store.lastError.ifBlank{"Oyun başlatılamadı."}
        }
    }

    if(confirmRestart) {
        AlertDialog(
            onDismissRequest={confirmRestart=false},
            title={Text("Yeni macera başlatılsın mı?")},
            text={Text("Mevcut 25 görevlik yolculuğun yarım kalan ilerlemesi sıfırlanacak. Tamamlanmış ödüllerin korunur.")},
            confirmButton={
                TextButton(onClick={confirmRestart=false;begin()}) {
                    Text("YENİ MACERA")
                }
            },
            dismissButton={
                TextButton(onClick={confirmRestart=false}) {
                    Text("VAZGEÇ")
                }
            }
        )
    }

    AutoAdvanceOnSuccess(run.seed, run.index,
        run.seed > 0L && run.solved && !run.finished) { next() }
    LaunchedEffect(run.seed,run.index) { scroll.scrollTo(0) }
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(
            listOf(Color(0xFF117FB4),Color(0xFF154A87),TimeStyle.ink)))
            .verticalScroll(scroll).padding(horizontal=13.dp,vertical=9.dp),
        verticalArrangement=Arrangement.spacedBy(13.dp)
    ) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,
            horizontalArrangement=Arrangement.SpaceBetween) {
            TextButton(onClick=onExit) {
                Text("← Oyunlar",color=TimeStyle.white,fontWeight=FontWeight.Bold)
            }
            if(run.seed>0 && !run.finished)
                TextButton(onClick={confirmRestart=true}) {
                    Text("↻ Yeni macera",color=TimeStyle.gold,fontWeight=FontWeight.Bold)
                }
        }

        if(run.seed==0L || run.finished) {
            Box(Modifier.fillMaxWidth().height(258.dp).clip(RoundedCornerShape(23.dp))) {
                Image(painterResource(R.drawable.time_tower_hero),
                    contentDescription="Sihirli saat kulesi, raylar, ışıklı dişliler ve zaman treni",
                    modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                    listOf(Color.Transparent,TimeStyle.ink.copy(alpha=.93f)))))
                Column(Modifier.align(Alignment.BottomStart).padding(17.dp)) {
                    Text("🕰️ ZAMAN KULESİ",color=TimeStyle.gold,
                        fontSize=25.sp,fontWeight=FontWeight.Black)
                    Text("Saatin Muhafızları  ·  5 Dünya  ·  25 Görev",
                        color=TimeStyle.white,fontSize=13.sp,fontWeight=FontWeight.Bold)
                }
            }
            if(run.finished) {
                TimePanel {
                    Text("🏆 KULE YENİDEN ÇALIŞIYOR!",color=TimeStyle.gold,
                        fontSize=20.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
                    Text("25/25 görevi tamamladın",color=TimeStyle.ice,
                        fontWeight=FontWeight.Black,fontSize=16.sp)
                    Text("Bağımsız doğrular: ${run.clean}/25",color=TimeStyle.white,
                        fontWeight=FontWeight.Bold)
                    Text("${run.errors} yanlış  ·  ${run.hints} ipucu  ·  En iyi seri: ${run.bestStreak}",
                        color=TimeStyle.secondary,fontSize=13.sp)
                    LinearProgressIndicator(progress={run.clean/25f},
                        color=TimeStyle.gold,trackColor=Color(0xFF446B93),
                        modifier=Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(8.dp)))
                    TimeWorld.entries.forEach { world->
                        Text("${world.title}: ${store.skillCompleted(world.skill)} bölüm · " +
                            "${store.skillClean(world.skill)} bağımsız doğru",
                            color=TimeStyle.white,fontSize=12.sp)
                    }
                    Text("En iyi tam puan: ${store.bestClean()}/25",
                        color=TimeStyle.gold,fontWeight=FontWeight.Bold)
                    Text("Oyun gözlemleri resmi kazanım yeterlilik belgesi değildir.",
                        color=TimeStyle.secondary,fontSize=11.sp)
                }
            } else {
                TimePanel {
                    Text("Kulenin beş mekanizmasını onar!",
                        color=TimeStyle.white,fontSize=17.sp,fontWeight=FontWeight.Black)
                    Text(if(profileGrade==1)
                        "1. sınıf · saat ve takvim keşfi (serbest pekiştirme)"
                        else "$profileGrade. sınıfa uygun zaman modelleri",
                        color=TimeStyle.ice,fontSize=13.sp)
                    Text("Analog saat • Dijital saat • Süre • Takvim • Günlük plan",
                        color=TimeStyle.secondary,fontSize=12.sp,textAlign=TextAlign.Center)
                    Text("Acele etme: süre baskısı yok, her hamlen kaydedilir.",
                        color=TimeStyle.gold,fontSize=12.sp,textAlign=TextAlign.Center)
                }
            }
            ChapterRibbon(if(run.finished)25 else 0)
            Text("ZORLUK SEÇ",color=TimeStyle.gold,fontWeight=FontWeight.Black)
            Row(horizontalArrangement=Arrangement.spacedBy(7.dp)){
                TimeTowerEngine.difficultyNames.forEachIndexed { idx,label->
                    val locked=!PremiumGameModePolicy.canStart("time_tower",idx,hasPremium)
                    TimeTile(if(locked)"🔒 "+label else label,
                        selected=choiceDifficulty==idx,
                        modifier=Modifier.weight(1f)) {
                        if(locked)openPremiumSettings() else {
                            choiceDifficulty=idx
                            audio.playTapSound()
                        }
                    }
                }
            }
            GoldAction(if(run.finished)"↻ YENİ MACERA" else "▶ MACERAYA BAŞLA"){
                begin()
            }
        } else {
            Text("🕰️ ZAMAN KULESİ",color=TimeStyle.gold,
                fontWeight=FontWeight.Black,fontSize=22.sp)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                Text("${run.grade}. sınıf  ·  ${TimeTowerEngine.difficultyNames[run.difficulty]}",
                    color=TimeStyle.ice,fontWeight=FontWeight.Bold,fontSize=12.sp)
                Text("GÖREV ${run.index+1}/25",color=TimeStyle.gold,
                    fontWeight=FontWeight.Black,fontSize=13.sp)
            }
            LinearProgressIndicator(progress={run.index/25f},color=TimeStyle.gold,
                trackColor=Color(0xFF3A6596),
                modifier=Modifier.fillMaxWidth().height(9.dp).clip(RoundedCornerShape(8.dp)))
            ChapterRibbon(run.index)
            if(challenge!=null) {
                TimePanel {
                    Text("${challenge.world.icon} ${challenge.world.title.uppercase()}",
                        color=TimeStyle.gold,fontSize=17.sp,fontWeight=FontWeight.Black)
                    Text(challenge.title,color=TimeStyle.white,
                        fontSize=19.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
                    Text(challenge.instruction,color=TimeStyle.secondary,
                        fontSize=15.sp,textAlign=TextAlign.Center,lineHeight=22.sp)
                    when(challenge.world) {
                        TimeWorld.CLOCK-> {
                            Text("HEDEF  ${TimeTowerEngine.formatTime(challenge.answerMinute)}",
                                color=TimeStyle.gold,fontSize=23.sp,
                                fontWeight=FontWeight.Black)
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                                TimeTile("Kısa ibre · AKREP",
                                    selected=selectedHand==ClockHand.HOUR,
                                    enabled=!run.solved,modifier=Modifier.weight(1f)) {
                                    selectedHand=ClockHand.HOUR
                                }
                                TimeTile("Uzun ibre · YELKOVAN",
                                    selected=selectedHand==ClockHand.MINUTE,
                                    enabled=!run.solved,modifier=Modifier.weight(1f)) {
                                    selectedHand=ClockHand.MINUTE
                                }
                            }
                            ClockFace(run.cursorMinutes,interactive=!run.solved,
                                hand=selectedHand,minuteStep=challenge.stepMinutes) {
                                setMinute(it)
                            }
                            Text("ŞU AN  ${TimeTowerEngine.formatTime(run.cursorMinutes)}",
                                color=TimeStyle.ice,fontWeight=FontWeight.Black,fontSize=19.sp)
                            ClockAdjustControls(run.cursorMinutes,challenge.stepMinutes,
                                enabled=!run.solved,onChange=::setMinute)
                        }
                        TimeWorld.READ-> {
                            Text("İpucu: ${TimeTowerEngine.partOfDay(challenge.answerMinute)}",
                                color=TimeStyle.gold,fontWeight=FontWeight.Bold,fontSize=13.sp)
                            ClockFace(challenge.answerMinute,interactive=false,
                                hand=ClockHand.MINUTE,minuteStep=challenge.stepMinutes)
                            Text("DİJİTAL CEVABIN",
                                color=TimeStyle.ice,fontWeight=FontWeight.Bold)
                            Text(TimeTowerEngine.formatTime(run.cursorMinutes),
                                color=TimeStyle.gold,fontWeight=FontWeight.Black,fontSize=27.sp)
                            ClockAdjustControls(run.cursorMinutes,challenge.stepMinutes,
                                enabled=!run.solved,onChange=::setMinute)
                        }
                        TimeWorld.TRAIN-> {
                            Text("KALKIŞ  ${TimeTowerEngine.formatTime(challenge.startMinute)}",
                                color=TimeStyle.ice,fontWeight=FontWeight.Black,fontSize=15.sp)
                            Text("YOLCULUK  ${challenge.elapsedMinutes} DAKİKA",
                                color=TimeStyle.gold,fontSize=20.sp,
                                fontWeight=FontWeight.Black)
                            var pending by remember(run.seed,run.index,run.cursorMinutes) {
                                mutableIntStateOf(run.cursorMinutes)
                            }
                            TrainTimeline(
                                progress=(pending-challenge.startMinute).toFloat()/
                                    challenge.rangeMinutes,
                                departure="🚉 Kalkış",
                                arrival="🏰 Kule")
                            Text("SEÇİLEN VARIŞ: ${TimeTowerEngine.formatTime(pending)}",
                                color=TimeStyle.gold,fontWeight=FontWeight.Black,fontSize=20.sp)
                            val maximum=(challenge.rangeMinutes/challenge.stepMinutes).toFloat()
                            Slider(
                                value=((pending-challenge.startMinute)/challenge.stepMinutes).toFloat(),
                                onValueChange={ pos->
                                    pending=challenge.startMinute +
                                        pos.roundToInt().coerceIn(0,maximum.toInt())*
                                            challenge.stepMinutes
                                },
                                onValueChangeFinished={setMinute(pending)},
                                enabled=!run.solved,valueRange=0f..maximum,
                                steps=(maximum.toInt()-1).coerceAtLeast(0),
                                colors=SliderDefaults.colors(
                                    thumbColor=TimeStyle.gold,activeTrackColor=TimeStyle.ice)
                            )
                            Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                                TimeTile("− ${challenge.stepMinutes} dk",
                                    enabled=!run.solved,modifier=Modifier.weight(1f)) {
                                    setMinute((run.cursorMinutes-challenge.stepMinutes)
                                        .coerceAtLeast(challenge.startMinute))
                                }
                                TimeTile("+ ${challenge.stepMinutes} dk",
                                    enabled=!run.solved,modifier=Modifier.weight(1f)) {
                                    setMinute((run.cursorMinutes+challenge.stepMinutes)
                                        .coerceAtMost(challenge.startMinute+challenge.rangeMinutes))
                                }
                            }
                            if(challenge.answerMinute>=1440)
                                Text("Gece yarısını aşarsan sonraki güne geçersin.",
                                    color=TimeStyle.secondary,fontSize=12.sp)
                        }
                        TimeWorld.CALENDAR-> {
                            Text("BAŞLANGIÇ  ${challenge.firstDate.display()}",
                                color=TimeStyle.ice,fontSize=15.sp,fontWeight=FontWeight.Black)
                            CalendarBoard(challenge,run.chosenDate,enabled=!run.solved,
                                onSelect=::setDate)
                        }
                        TimeWorld.ROUTE-> {
                            Text("KULE PROGRAMI",color=TimeStyle.gold,
                                fontSize=15.sp,fontWeight=FontWeight.Black)
                            RouteBoard(challenge,run.routeIds,enabled=!run.solved,
                                onAdd=::pushEvent,onUndo=::undoEvent)
                        }
                    }
                    if(!run.solved) {
                        GoldAction("✦ CEVABI KONTROL ET",onClick=::submit)
                    }
                }
                AnimatedVisibility(message.isNotBlank()) {
                    Surface(
                        color=if(success)Color(0xFF136A6B) else Color(0xFF694568),
                        shape=RoundedCornerShape(16.dp),
                        border=BorderStroke(1.dp,
                            if(success)TimeStyle.ice else TimeStyle.lilac)
                    ) {
                        Text(message,Modifier.fillMaxWidth().padding(14.dp),
                            color=TimeStyle.white,fontSize=13.sp,
                            fontWeight=FontWeight.Bold,lineHeight=19.sp)
                    }
                }
                if(run.solved) {
                    Text("✓ Kapı açıldı · Otomatik ilerleniyor…",color=TimeStyle.ice,
                        fontWeight=FontWeight.Bold)
                    if(!success) GoldAction("İlerlemeyi yeniden dene",onClick=::next)
                } else if(!run.usedHint) {
                    OutlinedButton(onClick={
                        if(persist(run.copy(hints=run.hints+1,usedHint=true))){
                            success=true
                            message="💡 ${TimeTowerEngine.hint(challenge)}"
                            audio.playTapSound()
                        }
                    },modifier=Modifier.fillMaxWidth(),
                        border=BorderStroke(1.dp,TimeStyle.ice),
                        shape=RoundedCornerShape(14.dp)) {
                        Text("💡 Küçük bir ipucu al",color=TimeStyle.white)
                    }
                }
            }
            Text("Her hamle cihazda kaydedilir · Ses ayarlarına uyumlu · Süre sınırı yok",
                color=TimeStyle.secondary,fontSize=11.sp,textAlign=TextAlign.Center,
                modifier=Modifier.fillMaxWidth())
        }
        if((run.seed==0L || run.finished)&&message.isNotBlank())
            Text(message,color=TimeStyle.gold,fontSize=13.sp)
        Spacer(Modifier.height(18.dp))
    }
}
