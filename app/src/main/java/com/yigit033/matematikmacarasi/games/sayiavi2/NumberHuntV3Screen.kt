package com.yigit033.matematikmacarasi.games.sayiavi2

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import com.yigit033.matematikmacarasi.services.AudioService
import kotlinx.coroutines.delay
import kotlin.math.roundToInt
import kotlin.random.Random

private val ink=Color(0xFF081C30)
private val ocean=Color(0xFF123F5E)
private val cream=Color(0xFFFFF5DC)
private val amber=Color(0xFFFFD075)
private val mint=Color(0xFF83EFCB)

private fun worldArtwork(worldKey:String):Int=when(worldKey){
    "beach"->R.drawable.island_explorer_hero
    "temple"->R.drawable.temple_node
    "crystals"->R.drawable.fraction_crystals_hero
    else->R.drawable.space_bg_nova_premium
}
private fun worldAccent(worldKey:String):Color=when(worldKey){
    "beach"->Color(0xFF59D8DD)
    "temple"->Color(0xFFFFC66E)
    "crystals"->Color(0xFFB7A2FF)
    else->Color(0xFF7AAEFE)
}

@Composable
private fun HuntPanel(modifier:Modifier=Modifier,content:@Composable ColumnScope.()->Unit){
    Surface(modifier=modifier.fillMaxWidth(),
        shape=RoundedCornerShape(22.dp),
        color=ink.copy(alpha=.94f),
        border=BorderStroke(1.dp,amber.copy(alpha=.45f))){
        Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp),
            content=content)
    }
}

@Composable
private fun HuntTile(label:String,modifier:Modifier=Modifier,
    active:Boolean=false,disabled:Boolean=false,onClick:()->Unit){
    val bg=when{
        active->Color(0xFF17765E)
        disabled->Color(0xFF183F51)
        else->Color(0xFF19567D)
    }
    Surface(
        modifier=modifier.heightIn(min=58.dp)
            .clickable(enabled=!disabled,onClick=onClick),
        shape=RoundedCornerShape(16.dp),
        color=bg,
        border=BorderStroke(if(active)2.dp else 1.dp,
            if(active)mint else amber.copy(alpha=.54f))
    ){
        Box(Modifier.fillMaxWidth().padding(horizontal=5.dp,vertical=9.dp),
            contentAlignment=Alignment.Center){
            Text(label,color=if(disabled)Color(0xFF9BBECA) else cream,
                fontSize=18.sp,lineHeight=21.sp,fontWeight=FontWeight.Black,
                textAlign=TextAlign.Center,maxLines=2,
                overflow=TextOverflow.Ellipsis)
        }
    }
}

@Composable
private fun NumberHuntPuzzle(
    task:NumberHuntGradeEngine.Task,
    solved:Boolean,
    useSavedPartial:Boolean,
    partial:Set<Int>,
    onPartial:(Set<Int>)->Unit,
    onCorrect:()->Unit,
    onWrong:()->Unit,
    onGoodStep:()->Unit
){
    // V3.1 persists every intermediate move, while legacy V3 sessions retain
    // their previous UI behavior without changing the saved task/version.
    var oldSelections by remember(task){mutableStateOf(emptySet<Int>())}
    val picked=if(useSavedPartial) partial else oldSelections
    fun accept(next:Set<Int>){
        if(useSavedPartial)onPartial(next)
        else {
            oldSelections=next
            onGoodStep()
        }
    }
    when(task){
        is NumberHuntGradeEngine.Scan -> {
            Text("Doğru sayı taşları · ${if(solved)4 else picked.size}/4",
                color=mint,fontSize=12.sp,fontWeight=FontWeight.Bold)
            task.options.withIndex().toList().chunked(3).forEach{row->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){
                    row.forEach{(index,value)->
                        HuntTile(value.toString(),Modifier.weight(1f),
                            active=solved && value in task.targets || index in picked,
                            disabled=solved || index in picked){
                            if(value in task.targets){
                                val next=picked+index
                                if(next.size==4)onCorrect() else accept(next)
                            }else onWrong()
                        }
                    }
                }
            }
        }
        is NumberHuntGradeEngine.Bridge -> {
            val ordered=remember(task){task.tokens.sortedBy{it.value}}
            Text("Kurulan köprü · ${if(solved)4 else picked.size}/4",
                color=mint,fontSize=12.sp,fontWeight=FontWeight.Bold)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)){
                repeat(4){step->
                    Surface(Modifier.weight(1f).height(42.dp),
                        color=if(step<picked.size || solved)Color(0xFF147A67) else ocean,
                        shape=RoundedCornerShape(12.dp),
                        border=BorderStroke(1.dp,amber.copy(alpha=.35f))){
                        Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){
                            Text(if(step<picked.size || solved)"✓" else "${step+1}",
                                color=cream,fontSize=17.sp,fontWeight=FontWeight.Bold)
                        }
                    }
                }
            }
            task.tokens.withIndex().toList().chunked(2).forEach{row->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){
                    row.forEach{(index,token)->
                        HuntTile(token.label,Modifier.weight(1f),
                            active=index in picked,disabled=solved || index in picked){
                            if(ordered[picked.size].value==token.value){
                                val next=picked+index
                                if(next.size==4)onCorrect() else accept(next)
                            }else onWrong()
                        }
                    }
                }
            }
        }
        is NumberHuntGradeEngine.Dial -> {
            var value by remember(task){mutableIntStateOf(task.initial)}
            Text("Enerji kadranını ayarla",color=mint,
                fontSize=12.sp,fontWeight=FontWeight.Bold)
            Surface(Modifier.fillMaxWidth(),shape=RoundedCornerShape(24.dp),
                color=Color(0xFF133E69),border=BorderStroke(2.dp,amber)){
                Text("$value${task.suffix}",Modifier.fillMaxWidth().padding(20.dp),
                    textAlign=TextAlign.Center,color=cream,fontSize=41.sp,
                    fontWeight=FontWeight.Black)
            }
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp),
                verticalAlignment=Alignment.CenterVertically){
                HuntTile("−",Modifier.width(58.dp),
                    disabled=solved || value<=task.lower){
                    value=(value-task.step).coerceAtLeast(task.lower)
                }
                Slider(value=value.toFloat(),onValueChange={raw->
                    if(!solved){
                        val snap=((raw-task.lower)/task.step).roundToInt()*task.step+task.lower
                        value=snap.coerceIn(task.lower,task.upper)
                    }
                },valueRange=task.lower.toFloat()..task.upper.toFloat(),
                    modifier=Modifier.weight(1f),enabled=!solved)
                HuntTile("+",Modifier.width(58.dp),
                    disabled=solved || value>=task.upper){
                    value=(value+task.step).coerceAtMost(task.upper)
                }
            }
            Button(onClick={if(value==task.target)onCorrect() else onWrong()},
                modifier=Modifier.fillMaxWidth().heightIn(min=54.dp),enabled=!solved,
                colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF0F956A))){
                Text("ENERJİ KİLİDİNİ AÇ",fontWeight=FontWeight.Black,color=Color.White)
            }
        }
        is NumberHuntGradeEngine.Pairs -> {
            var selectedId by remember(task){mutableIntStateOf(-1)}
            val rights=remember(task){task.pairs.reversed()}
            Text("Açılan mühürler · ${if(solved)3 else picked.size}/3",
                color=mint,fontSize=12.sp,fontWeight=FontWeight.Bold)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(9.dp)){
                    task.pairs.forEach{pair->
                        HuntTile(pair.left,Modifier.fillMaxWidth(),
                            active=pair.id==selectedId || pair.id in picked,
                            disabled=solved || pair.id in picked){selectedId=pair.id}
                    }
                }
                Column(Modifier.weight(1f),verticalArrangement=Arrangement.spacedBy(9.dp)){
                    rights.forEach{pair->
                        HuntTile(pair.right,Modifier.fillMaxWidth(),
                            active=pair.id in picked,
                            disabled=solved || pair.id in picked){
                            if(selectedId!=-1){
                                if(pair.id==selectedId){
                                    val next=picked+pair.id
                                    selectedId=-1
                                    if(next.size==3)onCorrect() else accept(next)
                                }else onWrong()
                            }
                        }
                    }
                }
            }
        }
        is NumberHuntGradeEngine.Cipher -> {
            Text("Dört şifreden yalnız biri doğru",color=mint,
                fontSize=12.sp,fontWeight=FontWeight.Bold)
            task.choices.chunked(2).forEach{row->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){
                    row.forEach{option->
                        HuntTile(option,Modifier.weight(1f),
                            active=solved && option==task.answer,disabled=solved){
                            if(option==task.answer)onCorrect() else onWrong()
                        }
                    }
                }
            }
        }
        is NumberHuntGradeEngine.Trail -> {
            val completed=if(solved)4 else picked.size.coerceIn(0,4)
            Text("Portal rotası · $completed/4",color=mint,
                fontSize=12.sp,fontWeight=FontWeight.Bold)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)){
                repeat(4){step->
                    Surface(Modifier.weight(1f).height(42.dp),
                        shape=RoundedCornerShape(14.dp),
                        color=if(step<completed)Color(0xFF147A67) else ocean,
                        border=BorderStroke(1.dp,amber.copy(alpha=.55f))){
                        Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){
                            Text(if(step<completed)"✓" else "${step+1}",
                                color=cream,fontWeight=FontWeight.Black)
                        }
                    }
                }
            }
            val checkpoint=task.checkpoints[completed.coerceAtMost(3)]
            Text(checkpoint.prompt,color=cream,fontSize=17.sp,
                lineHeight=23.sp,fontWeight=FontWeight.Bold)
            checkpoint.choices.chunked(2).forEach{row->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(9.dp)){
                    row.forEach{choice->
                        HuntTile(choice,Modifier.weight(1f),disabled=solved){
                            if(choice==checkpoint.answer){
                                val next=picked+completed
                                if(next.size==4)onCorrect() else accept(next)
                            }else onWrong()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NumberHuntV3Screen(profileGrade:Int,currentStars:Int,
    onWorldFinished:(grade:Int,attempts:Int,errors:Int,seconds:Long)->Unit,
    onClassic:()->Unit,onExit:()->Unit,modifier:Modifier=Modifier){
    val context=LocalContext.current
    val store=remember(context){NumberHuntV3Store(context)}
    val audio=remember(context){AudioService.getInstance(context)}
    var grade by remember(profileGrade){mutableIntStateOf(profileGrade.coerceIn(1,8))}
    var tier by remember(grade){
        mutableStateOf(store.load(grade)?.tier ?: LearningTier.GELISEN)
    }
    var session by remember(grade){mutableStateOf(store.load(grade))}
    var playing by remember{mutableStateOf(false)}
    var confirmOverwrite by remember{mutableStateOf(false)}
    var warning by remember(grade,session?.seed,session?.index){mutableStateOf<String?>(null)}
    var showHint by remember(grade,session?.seed,session?.index){mutableStateOf(false)}
    val latestFinished by rememberUpdatedState(onWorldFinished)
    val world=NumberHuntGradeEngine.world(grade)
    val accent=worldAccent(world.key)

    fun newRun(){
        val now=System.currentTimeMillis().coerceAtLeast(1L)
        val seed=now xor Random.nextLong().let{if(it==0L)1L else it}
        if(store.begin(grade,tier,if(seed==0L)now else seed,now)){
            session=store.load(grade)
            playing=true
        }else warning="Oyun kaydı başlatılamadı. Tekrar dene."
        confirmOverwrite=false
    }
    fun correct(){
        val current=session ?: return
        if(current.solved || current.index !in 0 until NumberHuntGradeEngine.TASK_COUNT)return
        if(store.solve(grade,current.index)){
            audio.playCorrectSound()
            session=store.load(grade)
        } else warning="Doğru cevap kaydedilemedi. Tekrar dene."
    }
    fun wrong(){
        val current=session ?: return
        if(current.solved)return
        if(store.attempt(grade,current.index,false)){
            audio.playWrongSound()
            session=store.load(grade)
            warning="Bu iz doğru değil. Yeniden dene."
        }else warning="Hamle kaydedilemedi. Yeniden dene."
    }
    fun goodStep(){audio.playCorrectSound();warning=null}
    fun savePartial(next:Set<Int>){
        val current=session ?: return
        if(store.savePartial(grade,current.index,next)){
            session=store.load(grade)
            goodStep()
        }else warning="Ara görev kaydedilemedi. Yeniden dene."
    }

    BackHandler(enabled=playing){playing=false}
    LaunchedEffect(grade,session?.seed,session?.index,session?.solved,playing){
        val run=session ?: return@LaunchedEffect
        if(!playing)return@LaunchedEffect
        if(run.index==run.taskCount && !run.finished){
            if(store.finish(grade,run.seed)){
                audio.playVictorySound()
                val elapsed=((System.currentTimeMillis()-run.startedAt)/1000L).coerceAtLeast(1L)
                latestFinished(grade,run.attempts,run.errors,elapsed)
            }else warning="Tamamlama kaydedilemedi. Yeniden dene."
            session=store.load(grade)
        }else if(run.solved && run.index<run.taskCount){
            delay(950L)
            if(store.advance(grade,run.index)){
                session=store.load(grade)
                warning=null
            }else warning="İlerleme kaydedilemedi. Tekrar dene."
        }
    }

    Box(modifier.fillMaxSize().background(ink)){
        Image(painter=painterResource(worldArtwork(world.key)),
            contentDescription=null,contentScale=ContentScale.Crop,
            modifier=Modifier.fillMaxSize(),alpha=.24f)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
            listOf(ink.copy(alpha=.55f),ocean.copy(alpha=.79f),ink.copy(alpha=.98f)))))
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .statusBarsPadding().navigationBarsPadding()
            .padding(horizontal=14.dp,vertical=10.dp),
            verticalArrangement=Arrangement.spacedBy(14.dp)){
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(9.dp)){
                TextButton(onClick={if(playing)playing=false else onExit()}){
                    Text(if(playing)"‹ Harita" else "‹ Çıkış",color=amber,
                        fontWeight=FontWeight.Black,fontSize=14.sp)
                }
                Column(Modifier.weight(1f)){
                    Text("SAYI AVCILARI 3.0",color=cream,fontWeight=FontWeight.Black,
                        fontSize=17.sp)
                    Text("$grade. sınıf · ${world.title}",color=mint,
                        fontSize=11.sp,maxLines=1,overflow=TextOverflow.Ellipsis)
                }
                Surface(color=ocean.copy(alpha=.9f),shape=RoundedCornerShape(12.dp),
                    border=BorderStroke(1.dp,amber.copy(alpha=.6f))){
                    Text("★ $currentStars",Modifier.padding(9.dp),
                        color=amber,fontWeight=FontWeight.Black)
                }
            }

            val run=session
            if(!playing){
                HuntPanel{
                    Image(painter=painterResource(worldArtwork(world.key)),
                        contentDescription=null,contentScale=ContentScale.Crop,
                        modifier=Modifier.fillMaxWidth().height(122.dp)
                            .clip(RoundedCornerShape(17.dp)))
                    Text("DÖRT DÜNYA • SEKİZ SINIF",color=amber,
                        fontSize=11.sp,letterSpacing=1.3.sp,fontWeight=FontWeight.Black)
                    Text(world.title,color=cream,fontSize=24.sp,lineHeight=28.sp,
                        fontWeight=FontWeight.Black)
                    Text(world.focus+" · "+world.subtitle,color=Color(0xFFD6E9EE),
                        fontSize=13.sp,lineHeight=18.sp)
                    Text("Yeni macerada 24 görev, 6 farklı oyun mekaniği ve her oynayışta değişen bulmacalar var. Sınıf içeriği, seçtiğin seviye zorluğu belirler.",
                        color=Color(0xFFAFCFDB),fontSize=12.sp,lineHeight=17.sp)
                }
                Text("SINIFINI SEÇ",color=amber,fontWeight=FontWeight.Black,fontSize=13.sp)
                Row(Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement=Arrangement.spacedBy(7.dp)){
                    for(g in 1..8){
                        Surface(Modifier.width(58.dp).height(50.dp)
                            .clickable{grade=g;playing=false},
                            shape=RoundedCornerShape(14.dp),
                            color=if(g==grade)Color(0xFF1D907B) else ocean,
                            border=BorderStroke(if(g==grade)2.dp else 1.dp,
                                if(g==grade)amber else accent.copy(alpha=.55f))){
                            Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){
                                Text("$g.",color=cream,fontWeight=FontWeight.Black,fontSize=17.sp)
                            }
                        }
                    }
                }
                Text("SEVİYE",color=amber,fontWeight=FontWeight.Black,fontSize=13.sp)
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(7.dp)){
                    LearningTier.entries.forEach{option->
                        Surface(Modifier.weight(1f).height(48.dp)
                            .clickable{tier=option},
                            shape=RoundedCornerShape(13.dp),
                            color=if(tier==option)Color(0xFF217D6D) else ocean,
                            border=BorderStroke(1.dp,if(tier==option)amber else accent)){
                            Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){
                                Text(option.title,color=cream,fontWeight=FontWeight.Bold,
                                    fontSize=13.sp)
                            }
                        }
                    }
                }
                NumberHuntGradeEngine.worlds.forEach{entry->
                    val selected=entry==world
                    val complete=entry.grades.any(store::hasCompleted)
                    Surface(Modifier.fillMaxWidth().clickable{
                            grade=if(grade in entry.grades)grade else entry.grades.first
                            playing=false
                        },shape=RoundedCornerShape(18.dp),
                        color=if(selected)Color(0xFF1D5D70) else ink.copy(alpha=.91f),
                        border=BorderStroke(if(selected)2.dp else 1.dp,
                            if(selected)amber else worldAccent(entry.key).copy(alpha=.55f))){
                        Row(Modifier.padding(10.dp),verticalAlignment=Alignment.CenterVertically,
                            horizontalArrangement=Arrangement.spacedBy(10.dp)){
                            Image(painter=painterResource(worldArtwork(entry.key)),
                                contentDescription=null,contentScale=ContentScale.Crop,
                                modifier=Modifier.size(76.dp).clip(RoundedCornerShape(13.dp)))
                            Column(Modifier.weight(1f),
                                verticalArrangement=Arrangement.spacedBy(4.dp)){
                                Text("${entry.grades.first}–${entry.grades.last}. SINIF",
                                    color=worldAccent(entry.key),fontSize=10.sp,
                                    fontWeight=FontWeight.Black)
                                Text(entry.title,color=cream,fontSize=14.sp,
                                    fontWeight=FontWeight.Black,maxLines=2)
                                Text(entry.focus,color=Color(0xFFD3E9F2),
                                    fontSize=11.sp,maxLines=2)
                            }
                            Text(if(selected)"◉" else if(complete)"✓" else "›",
                                color=amber,fontSize=20.sp,fontWeight=FontWeight.Black)
                        }
                    }
                }
                if(run!=null){
                    Button(onClick={playing=true},
                        modifier=Modifier.fillMaxWidth().heightIn(min=56.dp),
                        colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF158F6D))){
                        Text(when {
                            run.index==run.taskCount && !run.finished ->
                                "SON KAYDI TAMAMLA"
                            run.finished -> "SONUCU GÖR"
                            else -> "KAYDEDİLEN GÖREVE DÖN · ${run.index+1}/${run.taskCount}"
                        },fontWeight=FontWeight.Black)
                    }
                }
                Button(onClick={
                    if(run!=null && !run.finished)confirmOverwrite=true else newRun()
                },modifier=Modifier.fillMaxWidth().heightIn(min=56.dp),
                    colors=ButtonDefaults.buttonColors(containerColor=accent)){
                    Text("✦ YENİ SAYI AVI BAŞLAT · $grade. SINIF",
                        color=ink,fontWeight=FontWeight.Black)
                }
                TextButton(onClick=onClassic,modifier=Modifier.fillMaxWidth()){
                    Text("Klasik Kumsal Koyu ve eski ilerlemem",color=cream,
                        textAlign=TextAlign.Center)
                }
                Text("Önceki sınıfların dünyalarını tekrar oynayabilirsin. Bu içerikler seçilmiş alıştırmalardır; resmî kazanım değerlendirmesi değildir.",
                    color=Color(0xFFBAD1DF),fontSize=11.sp,lineHeight=16.sp,
                    textAlign=TextAlign.Center)
            }else if(run!=null && run.index>=run.taskCount &&
                !run.finished){
                HuntPanel{
                    Text("Son görevin tamamlandı ancak kayıt doğrulanamadı.",
                        color=amber,fontSize=18.sp,fontWeight=FontWeight.Bold)
                    Text("Ödül ve ilerlemeyi kaybetmemek için kaydı yeniden dene.",
                        color=cream,fontSize=13.sp)
                    Button(onClick={
                        if(store.finish(grade,run.seed)){
                            audio.playVictorySound()
                            val elapsed=((System.currentTimeMillis()-run.startedAt)/1000L)
                                .coerceAtLeast(1L)
                            onWorldFinished(grade,run.attempts,run.errors,elapsed)
                        }
                        session=store.load(grade)
                    },modifier=Modifier.fillMaxWidth()){
                        Text("TAMAMLAMAYI KAYDET")
                    }
                }
            }else if(run!=null && run.index>=run.taskCount){
                HuntPanel{
                    Text("🏆 MACERA TAMAMLANDI",color=amber,
                        fontSize=16.sp,fontWeight=FontWeight.Black)
                    Text(world.title,color=cream,fontSize=24.sp,fontWeight=FontWeight.Black)
                    Text("$grade. sınıf · ${run.tier.title} · ${run.taskCount} görev",
                        color=mint,fontSize=13.sp)
                    LinearProgressIndicator(progress={1f},
                        modifier=Modifier.fillMaxWidth().height(7.dp),
                        color=mint,trackColor=ocean)
                    Text("Doğru hamle oranı: %${run.accuracy}",color=cream,
                        fontSize=18.sp,fontWeight=FontWeight.Bold)
                    Text("${run.attempts} deneme · ${run.errors} yanlış · ${run.hints} ipucu",
                        color=Color(0xFFC6E0EE),fontSize=13.sp)
                    Text(if(store.starAcknowledged(grade))
                        "★ Bu sınıfın ilk tamamlama yıldızı alındı."
                        else "İlk tamamlama yıldızı varsa kayıt doğrulaması yapılıyor.",
                        color=amber,fontSize=12.sp)
                    Text("Bu sonuç oyun içi hamlelerini ölçer; öğrenme kazanımını tek başına kanıtlamaz.",
                        color=Color(0xFFB4C9D5),fontSize=11.sp)
                    Button(onClick={playing=false},
                        modifier=Modifier.fillMaxWidth().heightIn(min=54.dp),
                        colors=ButtonDefaults.buttonColors(containerColor=Color(0xFF0F956A))){
                        Text("DÜNYA HARİTASINA DÖN",fontWeight=FontWeight.Black)
                    }
                    TextButton(onClick={newRun()},modifier=Modifier.fillMaxWidth()){
                        Text("Yeniden oyna · yeni sorular",color=amber)
                    }
                }
            }else if(run!=null){
                val index=run.index
                val tasks=remember(run.seed,run.tier,grade,run.contentVersion){
                    if(run.contentVersion>=NumberHuntRichContent.VERSION)
                        NumberHuntRichContent.generate(grade,run.tier,run.seed)
                    else NumberHuntGradeEngine.generate(grade,run.tier,run.seed)
                }
                val task=tasks[index]
                val modern=run.contentVersion>=NumberHuntRichContent.VERSION
                val room=index/if(modern)NumberHuntRichContent.TASKS_PER_CHAPTER else 4
                val title=if(modern)NumberHuntRichContent.chapterTitles[room]
                    else NumberHuntGradeEngine.Mechanic.entries[room].title
                HuntPanel{
                    Text("DÜNYA ${NumberHuntGradeEngine.worlds.indexOf(world)+1} · BÖLÜM ${room+1}/4",
                        color=amber,fontSize=12.sp,fontWeight=FontWeight.Black)
                    Text("${task.mechanic.icon} $title · ${task.mechanic.title}",color=cream,
                        fontSize=22.sp,fontWeight=FontWeight.Black)
                    Text("Görev ${index+1}/${run.taskCount} · ${run.tier.title}",color=mint,
                        fontSize=12.sp)
                    LinearProgressIndicator(progress={(index+if(run.solved)1 else 0)/run.taskCount.toFloat()},
                        modifier=Modifier.fillMaxWidth().height(8.dp),
                        color=accent,trackColor=ocean)
                    Text(task.prompt,color=cream,fontSize=19.sp,lineHeight=25.sp,
                        fontWeight=FontWeight.Bold)
                }
                HuntPanel{
                    key(run.seed,index){
                        NumberHuntPuzzle(task,run.solved,
                            useSavedPartial=modern,
                            partial=run.partial,
                            onPartial={savePartial(it)},
                            onCorrect={correct()},onWrong={wrong()},onGoodStep={goodStep()})
                    }
                    if(run.solved){
                        Text("✓ TAMAMLANDI · Sıradaki göreve geçiliyor…",
                            color=mint,fontWeight=FontWeight.Black,fontSize=13.sp)
                        Text(task.explanation,color=Color(0xFFC9E3EF),fontSize=12.sp)
                    }else{
                        if(warning!=null)Text(warning.orEmpty(),color=amber,
                            fontSize=12.sp,fontWeight=FontWeight.Bold)
                        if(showHint){
                            Text("💡 ${task.hint}",color=mint,fontSize=12.sp,lineHeight=17.sp)
                        }else{
                            TextButton(onClick={
                                if(store.hint(grade,index)){
                                    showHint=true;session=store.load(grade)
                                }else warning="İpucu kaydedilemedi."
                            }){Text("💡 İpucu göster",color=amber)}
                        }
                    }
                }
                if(run.solved && warning?.startsWith("İlerleme kaydedilemedi")==true){
                    Button(onClick={
                        if(store.advance(grade,index))session=store.load(grade)
                    },modifier=Modifier.fillMaxWidth()){
                        Text("İLERLEMEYİ YENİDEN DENE")
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
        }
    }
    if(confirmOverwrite){
        AlertDialog(onDismissRequest={confirmOverwrite=false},
            title={Text("Yeni macera başlatılsın mı?")},
            text={Text("Bu sınıftaki tamamlanmamış yeni macera sıfırlanır. Eski Sayı Avı kayıtları ve kazanılmış yıldızlar korunur.")},
            confirmButton={
                TextButton(onClick={newRun()}){Text("Yeni başlat")}
            },dismissButton={
                TextButton(onClick={confirmOverwrite=false}){Text("Vazgeç")}
            })
    }
}
