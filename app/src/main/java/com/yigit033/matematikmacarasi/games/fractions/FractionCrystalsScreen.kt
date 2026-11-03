package com.yigit033.matematikmacarasi.games.fractions

import com.yigit033.matematikmacarasi.games.common.AutoAdvanceOnSuccess
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.services.AudioService
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.PI

private val navy = Color(0xFF081A3D)
private val teal = Color(0xFF64ECF4)
private val gold = Color(0xFFFFD980)
private val pearl = Color(0xFFF1F9FF)
private val purple = Color(0xFFA984FF)
private val panel = Color(0xFF173A68)

@Composable
private fun CrystalLabel(text: String, modifier: Modifier = Modifier, color: Color = pearl) {
    Text(text, modifier, color=color, fontWeight=FontWeight.Bold, fontSize=14.sp,
        textAlign=TextAlign.Center)
}

@Composable
private fun GlowCard(modifier: Modifier=Modifier, content: @Composable ColumnScope.()->Unit) {
    Surface(modifier=modifier.fillMaxWidth(), shape=RoundedCornerShape(24.dp),
        color=panel, border=BorderStroke(1.5.dp,teal.copy(alpha=.55f))) {
        Column(Modifier.padding(15.dp),horizontalAlignment=Alignment.CenterHorizontally,
            verticalArrangement=Arrangement.spacedBy(12.dp),content=content)
    }
}

/** A live tactile pie: each tap toggles precisely one mathematical equal sector. */
@Composable
private fun CrystalDisc(
    denominator: Int, lit: Int, modifier: Modifier=Modifier,
    onSector: ((Int)->Unit)?=null
) {
    val target = remember(denominator,lit) { Integer.bitCount(lit) }
    val glow by animateFloatAsState(
        targetValue=target.toFloat()/denominator,
        animationSpec=tween(260), label="crystal glow"
    )
    Canvas(modifier
        .size(226.dp)
        .semantics { contentDescription =
            "$denominator eş parçalı kristal, $target parça aydınlatıldı" }
        .then(if(onSector!=null) Modifier.pointerInput(denominator) {
            detectTapGestures { click ->
                val center=Offset(size.width/2f,size.height/2f)
                val dx=click.x-center.x; val dy=click.y-center.y
                if(dx*dx+dy*dy <= (min(size.width,size.height)*.49f).let{it*it}) {
                    val angle=((Math.toDegrees(atan2(dy.toDouble(),dx.toDouble()))+90+360)%360)
                    val sector=((angle / 360.0 * denominator).toInt()).coerceIn(0,denominator-1)
                    onSector(sector)
                }
            }
        } else Modifier)
    ) {
        val side=min(size.width,size.height)
        val center=Offset(size.width/2,size.height/2)
        drawCircle(teal.copy(alpha=.09f+.20f*glow),radius=side*.50f,center=center)
        drawCircle(gold.copy(alpha=.62f),radius=side*.48f,center=center,
            style=Stroke(width=5.dp.toPx()))
        val diameter=side*.86f; val origin=Offset(center.x-diameter/2,center.y-diameter/2)
        repeat(denominator) { index ->
            val selected=(lit and (1 shl index))!=0
            drawArc(
                brush=Brush.radialGradient(
                    if(selected) listOf(Color(0xFFCBFDFF),Color(0xFF38D7E4),Color(0xFF1E75E7))
                    else listOf(Color(0xFF53428D),Color(0xFF202E75),Color(0xFF182457)),
                    center=center, radius=side*.50f),
                startAngle=-90f+index*360f/denominator+.8f,
                sweepAngle=360f/denominator-1.6f,useCenter=true,
                topLeft=origin,size=androidx.compose.ui.geometry.Size(diameter,diameter)
            )
        }
        repeat(denominator) { index ->
            val theta=2*PI*(index.toDouble()/denominator-0.25)
            val end=Offset(center.x+diameter*.45f*kotlin.math.cos(theta).toFloat(),
                center.y+diameter*.45f*kotlin.math.sin(theta).toFloat())
            drawLine(gold.copy(alpha=.78f),center,end,strokeWidth=2.dp.toPx())
        }
        drawCircle(gold,radius=side*.055f,center=center)
        drawCircle(Color.White.copy(alpha=.65f),radius=side*.025f,center=center)
    }
}

@Composable
private fun PortionBar(fraction: CrystalFraction, accent: Color) {
    Row(Modifier.fillMaxWidth().height(45.dp).clip(RoundedCornerShape(13.dp))
        .border(2.dp,gold.copy(alpha=.6f),RoundedCornerShape(13.dp))) {
        repeat(fraction.denominator) { index ->
            Box(Modifier.weight(1f).fillMaxHeight()
                .background(if(index<fraction.numerator) accent else Color(0xFF22365E))
                .border(.5.dp,Color.White.copy(alpha=.27f)))
        }
    }
}

@Composable
private fun ChooseCrystal(text:String, selected:Boolean=false,
    modifier:Modifier=Modifier,onClick:()->Unit) {
    Surface(modifier=modifier.clickable(onClick=onClick).heightIn(min=65.dp),
        color=if(selected)Color(0xFF316DA0) else Color(0xFF203E72),
        shape=RoundedCornerShape(17.dp),
        border=BorderStroke(if(selected)2.dp else 1.dp,if(selected)gold else teal.copy(alpha=.55f))) {
        Box(Modifier.padding(horizontal=12.dp,vertical=13.dp),
            contentAlignment=Alignment.Center) {
            Text(text,color=pearl,fontWeight=FontWeight.Black,fontSize=20.sp,
                textAlign=TextAlign.Center)
        }
    }
}

@Composable
fun FractionCrystalsScreen(
    store: FractionCrystalStore,
    grade: Int,
    onExit: ()->Unit,
    onCompleted: (CrystalRun)->Unit
) {
    val context=LocalContext.current
    val audio=remember(context) { AudioService.getInstance(context) }
    var run by remember { mutableStateOf(store.load()) }
    var chosenDifficulty by remember { mutableIntStateOf(if(grade<=3) 0 else 1) }
    var message by remember { mutableStateOf("") }
    var positive by remember { mutableStateOf(true) }
    val scroll=rememberScrollState()
    val task=remember(run.seed,run.index,run.grade,run.difficulty) { store.task(run) }
    BackHandler { onExit() }

    fun persist(next: CrystalRun): Boolean {
        if(store.save(next)) { run=next;return true }
        positive=false
        message=if(store.lastError.isNotBlank()) store.lastError
            else "İlerleme kaydedilemedi. Yeniden deneyebilirsin."
        return false
    }
    fun submit(filled:Int=0, answer:String?=null) {
        if(run.solved||run.finished) return
        val t=task?:return
        val correct=FractionCrystalEngine.correct(t,filled,answer)
        val updated=run.copy(attempts=run.attempts+1,
            errors=run.errors+if(correct)0 else 1,
            hadError=run.hadError||!correct,solved=correct)
        if(persist(updated)) {
            positive=correct
            message=if(correct) "Harika! "+t.explanation
                else "Bir daha düşün. "+when(t.mode) {
                    CrystalMode.FILL -> "Payda kadar eş parça var. Pay kadarını aydınlat."
                    CrystalMode.BRIDGE -> "Her küçük taş birim kesirdir. Köprünün uzunluğunu kontrol et."
                    CrystalMode.COMPARE -> "İki şeridin dolu uzunluğunu karşılaştır."
                    CrystalMode.PORTAL -> "Aynı miktarın başka gösterimini araştır."
                }
            if(correct)audio.playCorrectSound() else audio.playWrongSound()
        }
    }
    fun next() {
        val advanced=store.advance(run)
        if(advanced!=null) {
            run=advanced;message=""
            if(advanced.finished) {
                store.updateBest(advanced)
                audio.playVictorySound()
                onCompleted(advanced)
            } else {
                audio.playLevelCompleteSound()
            }
        } else { positive=false;message=store.lastError.ifBlank { "Sonraki görev kaydedilemedi." } }
    }

    AutoAdvanceOnSuccess(run.seed, run.index,
        run.seed > 0L && run.solved && !run.finished) { next() }
    LaunchedEffect(run.index,run.seed) { scroll.scrollTo(0) }
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(
                Color(0xFF1379B7),Color(0xFF124680),navy)))
            .verticalScroll(scroll).padding(horizontal=15.dp,vertical=12.dp),
        verticalArrangement=Arrangement.spacedBy(13.dp)
    ) {
        TextButton(onClick=onExit) { Text("← Oyunlar",color=pearl,fontWeight=FontWeight.Bold) }
        if(run.seed==0L || run.finished) {
            Box(Modifier.fillMaxWidth().height(245.dp).clip(RoundedCornerShape(25.dp))) {
                Image(painterResource(R.drawable.fraction_crystals_hero),
                    contentDescription="Parlayan kristallerden kurulmuş büyülü kesir adası",
                    modifier=Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(
                    listOf(Color.Transparent,navy.copy(alpha=.86f)))))
                Column(Modifier.align(Alignment.BottomStart).padding(17.dp)) {
                    Text("💎 KESİR KRİSTALLERİ",color=gold,fontSize=24.sp,
                        fontWeight=FontWeight.Black)
                    Text("Dört büyülü mekanik · 12 eşsiz görev",color=pearl,fontSize=12.sp)
                }
            }
            if(run.finished) {
                GlowCard {
                    Text("🏆 KRİSTALLER YENİDEN PARLIYOR!",color=gold,
                        fontSize=20.sp,fontWeight=FontWeight.Black,textAlign=TextAlign.Center)
                    Text("12/12 görev tamamlandı",color=teal,fontWeight=FontWeight.Bold)
                    Text("İlk denemede: ${run.cleanAnswers}/12  •  ${run.attempts} deneme",
                        color=pearl,fontSize=14.sp,textAlign=TextAlign.Center)
                    Text("${run.errors} yanlış · ${run.hints} ipucu",color=pearl)
                    Text("En iyi bağımsız sonuç: ${store.bestClean()}/12",color=gold)
                    FractionCrystalEngine.skillCodes(run.grade).forEach { skill ->
                        Text("${skillTitle(skill)}: ${store.completedForSkill(skill)} görev",
                            color=pearl,fontSize=12.sp)
                    }
                    Text("Bunlar oyun içi gözlemlerdir; resmi kazanım belgesi değildir.",
                        color=Color(0xFFC9E5F7),fontSize=11.sp)
                }
            } else {
                GlowCard {
                    Text("Kırılan kristal köprüleri birlikte onaralım!",
                        color=pearl,fontSize=17.sp,fontWeight=FontWeight.Bold,
                        textAlign=TextAlign.Center)
                    Text(if(grade<=2)"Eşit paylaşım keşfi · Sınıf kazanımı değerlendirmesi değildir"
                        else "$grade. sınıf · uygun kesir modelleri",color=teal,fontSize=13.sp,
                        textAlign=TextAlign.Center)
                }
            }
            Text("Zorluk seç",color=gold,fontWeight=FontWeight.Black,fontSize=17.sp)
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){
                FractionCrystalEngine.difficultyNames.forEachIndexed { index,label ->
                    ChooseCrystal(label,selected=chosenDifficulty==index,
                        modifier=Modifier.weight(1f)) { chosenDifficulty=index }
                }
            }
            Button(onClick={
                val started=store.newRun(grade,chosenDifficulty)
                if(started!=null) {run=started;message="";audio.playQuestCompleteSound()}
                else {positive=false;message=store.lastError}
            },modifier=Modifier.fillMaxWidth().heightIn(min=58.dp),
                colors=ButtonDefaults.buttonColors(containerColor=gold,contentColor=navy),
                shape=RoundedCornerShape(18.dp)){
                Text(if(run.finished)"↻ Yeni Kesir Macerası" else "▶ Maceraya Başla",
                    fontWeight=FontWeight.Black,fontSize=16.sp)
            }
        } else if(task!=null) {
            Text("💎 KESİR KRİSTALLERİ",color=gold,fontSize=22.sp,
                fontWeight=FontWeight.Black)
            Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                Text("$grade. sınıf · ${FractionCrystalEngine.difficultyNames[run.difficulty]}",
                    color=teal,fontSize=12.sp)
                Text("GÖREV ${run.index+1}/12",color=gold,fontSize=13.sp,
                    fontWeight=FontWeight.Black)
            }
            LinearProgressIndicator(progress={ run.index / 12f },color=gold,
                trackColor=Color(0xFF355F89),
                modifier=Modifier.fillMaxWidth().height(9.dp).clip(CircleShape))
            GlowCard {
                Text("${task.mode.icon} ${task.mode.title.uppercase()}",color=gold,
                    fontSize=16.sp,fontWeight=FontWeight.Black)
                Text(task.label,color=pearl,fontSize=17.sp,fontWeight=FontWeight.Bold,
                    textAlign=TextAlign.Center)
                when(task.mode) {
                    CrystalMode.FILL -> {
                        CrystalDisc(task.target.denominator,run.litMask,
                            onSector=if(run.solved)null else { sector ->
                                val mask=run.litMask xor (1 shl sector)
                                if(persist(run.copy(litMask=mask))) {
                                    audio.playCoinSound();message=""
                                }
                            })
                        Text("Seçilen: ${Integer.bitCount(run.litMask)}/${task.target.denominator}",
                            color=teal,fontWeight=FontWeight.Black)
                        if(!run.solved) Button(
                            onClick={submit(filled=Integer.bitCount(run.litMask))},
                            colors=ButtonDefaults.buttonColors(containerColor=gold,contentColor=navy),
                            modifier=Modifier.fillMaxWidth().heightIn(min=52.dp)
                        ) {Text("KRİSTALİ MÜHÜRLE",fontWeight=FontWeight.Black)}
                    }
                    CrystalMode.BRIDGE -> {
                        PortionBar(CrystalFraction(run.built,task.target.denominator),teal)
                        Text("Köprü: ${run.built}/${task.target.denominator}",
                            color=gold,fontSize=19.sp,fontWeight=FontWeight.Black)
                        Row(horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                            ChooseCrystal("−",modifier=Modifier.weight(1f)) {
                                if(persist(run.copy(built=(run.built-1).coerceAtLeast(0))))
                                    audio.playTapSound()
                            }
                            ChooseCrystal("+ 1/${task.target.denominator}",
                                modifier=Modifier.weight(2f)) {
                                if(persist(run.copy(built=(run.built+1)
                                        .coerceAtMost(task.target.numerator))))
                                    audio.playCoinSound()
                            }
                        }
                        if(!run.solved) Button(
                            onClick={submit(filled=run.built)},
                            modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),
                            colors=ButtonDefaults.buttonColors(containerColor=gold,contentColor=navy)
                        ){Text("KÖPRÜYÜ TAMAMLA",fontWeight=FontWeight.Black)}
                    }
                    CrystalMode.COMPARE -> {
                        Text("SOL · ${task.target.format()}",color=teal,
                            fontWeight=FontWeight.Black)
                        PortionBar(task.target,teal)
                        Text("SAĞ · ${task.rival.format()}",color=purple,
                            fontWeight=FontWeight.Black)
                        PortionBar(task.rival,purple)
                        if(!run.solved) Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                            task.choices.forEach{option->
                                ChooseCrystal(option,modifier=Modifier.weight(1f)){
                                    submit(answer=option)
                                }
                            }
                        }
                    }
                    CrystalMode.PORTAL -> {
                        CrystalDisc(task.target.denominator,
                            (1 shl task.target.numerator)-1)
                        Text("${task.target.format()}",color=gold,fontSize=26.sp,
                            fontWeight=FontWeight.Black)
                        if(!run.solved) task.choices.chunked(2).forEach{ pair ->
                            Row(horizontalArrangement=Arrangement.spacedBy(9.dp)) {
                                pair.forEach{choice->
                                    ChooseCrystal(choice,modifier=Modifier.weight(1f)){
                                        submit(answer=choice)
                                    }
                                }
                                if(pair.size==1)Spacer(Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            if(message.isNotBlank()) Surface(shape=RoundedCornerShape(16.dp),
                color=if(positive)Color(0xFF156C65) else Color(0xFF72425A)){
                Text(message,Modifier.fillMaxWidth().padding(14.dp),
                    color=pearl,fontWeight=FontWeight.Bold,fontSize=13.sp)
            }
            if(run.solved) {
                Text("✓ Kristal tamamlandı · Otomatik ilerleniyor…",color=gold,
                    fontWeight=FontWeight.Bold)
                if(!positive) Button(onClick={next()},
                    modifier=Modifier.fillMaxWidth().heightIn(min=54.dp)) {
                    Text("İlerlemeyi yeniden dene")
                }
            }
            if(!run.solved && !run.usedHint) {
                OutlinedButton(onClick={
                    if(persist(run.copy(hints=run.hints+1,usedHint=true))) {
                        audio.playTapSound();positive=true
                        message=when(task.mode){
                            CrystalMode.FILL->"Payda ${task.target.denominator}: bütün o kadar eş parçaya ayrıldı."
                            CrystalMode.BRIDGE->"Her eklediğin parça 1/${task.target.denominator}. Toplam ${task.target.numerator} parça gerekiyor."
                            CrystalMode.COMPARE->"İki barın dolu uzunluklarına bak; sadece paylara değil."
                            CrystalMode.PORTAL->"Denk kesirde pay ve payda aynı oranda değişir."
                        }
                    }
                },modifier=Modifier.fillMaxWidth(),
                    border=BorderStroke(1.dp,teal)){
                    Text("💡 Küçük bir ipucu al",color=pearl)
                }
            }
            Text("Dokunarak keşfet · Süre baskısı yok · Kaldığın yer kaydedilir",
                color=Color(0xFFB8DDF0),fontSize=11.sp,
                modifier=Modifier.fillMaxWidth(),textAlign=TextAlign.Center)
        }
        if((run.seed==0L||run.finished)&&message.isNotBlank())
            Text(message,color=gold,fontSize=13.sp)
        Spacer(Modifier.height(16.dp))
    }
}

private fun skillTitle(skill:String):String = when(skill) {
    "fraction_part_whole"->"Parça–bütün"
    "fraction_unit_composition"->"Birim kesirlerden oluşturma"
    "fraction_compare"->"Büyüklük karşılaştırma"
    "fraction_equivalence"->"Denk kesir"
    "fraction_representation"->"Gösterimler"
    "fraction_arithmetic"->"Kesirlerle toplama"
    else->skill
}
