package com.yigit033.matematikmacarasi.games.basic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.models.DifficultyLevel
import com.yigit033.matematikmacarasi.services.PlayerProfileService
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import kotlin.random.Random

private val ink=Color(0xFF091635)
private val ivory=Color(0xFFF4FCFF)
private val gold=Color(0xFFFFD775)
private val cyan=Color(0xFF66E6FA)
private fun tint(op:OperationKind):Color=when(op){
    OperationKind.ADD->Color(0xFF29A6FF)
    OperationKind.SUBTRACT->Color(0xFFFF6388)
    OperationKind.MULTIPLY->Color(0xFF22CA8E)
    OperationKind.DIVIDE->Color(0xFFFFB74B)
}
private fun imageFor(op:OperationKind):Int=when(op){
    OperationKind.ADD->R.drawable.card_four_operations
    OperationKind.SUBTRACT->R.drawable.card_balloon_pop
    OperationKind.MULTIPLY->R.drawable.card_number_hunt_premium
    OperationKind.DIVIDE->R.drawable.card_space_adventure
}

/** Single curated entry point. No accidental redirect to addition. */
class OperationsHubActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){
        super.onCreate(savedInstanceState)
        val prefs=getSharedPreferences("operation_hub_v1",MODE_PRIVATE)
        val grade=GradeAwareOperationPolicy.grade(
            PlayerProfileService.getInstance(this).getUserClass())
        setContent {
            MatematikMacerasiTheme {
                var mixed by rememberSaveable {mutableStateOf(false)}
                var difficulty by rememberSaveable {mutableStateOf(DifficultyLevel.EASY)}
                var last by remember {mutableStateOf(
                    OperationKind.entries.firstOrNull{
                        it.name==prefs.getString("last_operation",null)
                    }
                )}
                fun launch(op:OperationKind){
                    if(!GradeAwareOperationPolicy.allowed(grade,op))return
                    prefs.edit().putString("last_operation",op.name).apply()
                    last=op
                    val clazz=when(op){
                        OperationKind.ADD->AdditionGameActivity::class.java
                        OperationKind.SUBTRACT->SubtractionGameActivity::class.java
                        OperationKind.MULTIPLY->MultiplicationGameActivity::class.java
                        OperationKind.DIVIDE->DivisionGameActivity::class.java
                    }
                    startActivity(Intent(this,clazz))
                }
                if(mixed) MixedOperationSession(
                    grade=grade,difficulty=difficulty,
                    onBack={mixed=false}
                ) else {
                    Column(
                        Modifier.fillMaxSize()
                            .background(Brush.verticalGradient(listOf(
                                Color(0xFF0876AE),Color(0xFF142660),ink)))
                            .verticalScroll(rememberScrollState())
                            .safeDrawingPadding().padding(14.dp),
                        verticalArrangement=Arrangement.spacedBy(12.dp)
                    ){
                        Row(verticalAlignment=Alignment.CenterVertically){
                            TextButton(onClick={finish()}){Text("← Geri",color=ivory)}
                            Spacer(Modifier.weight(1f))
                            Text("🎮 4 İŞLEM MERKEZİ",color=gold,
                                fontWeight=FontWeight.Black,fontSize=17.sp)
                        }
                        Box(Modifier.fillMaxWidth().height(174.dp)
                            .clip(RoundedCornerShape(23.dp))) {
                            Image(
                                painterResource(R.drawable.card_four_operations),
                                contentDescription=null,
                                contentScale=ContentScale.Crop,
                                modifier=Modifier.fillMaxSize()
                            )
                            Box(Modifier.fillMaxSize().background(
                                Brush.verticalGradient(listOf(Color.Transparent,
                                    ink.copy(alpha=.87f)))))
                            Column(Modifier.align(Alignment.BottomStart).padding(14.dp)){
                                Text("MATEMATİK KAHRAMANI",color=gold,
                                    fontSize=20.sp,fontWeight=FontWeight.Black)
                                Text("$grade. sınıfına uygun maceralar",
                                    color=ivory,fontSize=13.sp)
                            }
                        }
                        Text("Dört farklı yol, tek matematik macerası!",
                            color=ivory,fontWeight=FontWeight.Black,fontSize=17.sp)
                        Text("Sınıfının kapsamı korunur. Daha zor bir seviye seçmek yeni işlemleri açmaz.",
                            color=Color(0xFFB7DDF5),fontSize=12.sp)
                        OperationKind.entries.chunked(2).forEach{pair->
                            Row(horizontalArrangement=Arrangement.spacedBy(10.dp)){
                                pair.forEach{op->
                                    val available=GradeAwareOperationPolicy.allowed(grade,op)
                                    Surface(
                                        onClick={launch(op)},
                                        enabled=available,
                                        modifier=Modifier.weight(1f).height(153.dp),
                                        shape=RoundedCornerShape(20.dp),
                                        color=Color(0xFF152F60),
                                        border=BorderStroke(2.dp,
                                            tint(op).copy(alpha=if(available)1f else .30f))
                                    ){
                                        Column(Modifier.fillMaxSize().padding(11.dp),
                                            verticalArrangement=Arrangement.SpaceBetween) {
                                            Text(op.symbol,color=tint(op),fontSize=32.sp,
                                                fontWeight=FontWeight.Black)
                                            Text(op.title,color=ivory,fontSize=18.sp,
                                                fontWeight=FontWeight.Black)
                                            Text(if(available)"▶ Maceraya başla"
                                                else "🔒 2. sınıfta açılır",
                                                color=if(available)gold else Color(0xFF9AB9D2),
                                                fontSize=11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        Box(
                            Modifier.fillMaxWidth().height(169.dp)
                                .clip(RoundedCornerShape(23.dp))
                                .clickable { mixed=true }
                        ) {
                            Image(
                                painterResource(R.drawable.card_four_operations),
                                contentDescription=null,
                                modifier=Modifier.fillMaxSize(),
                                contentScale=ContentScale.Crop
                            )
                            Box(Modifier.fillMaxSize().background(
                                Brush.verticalGradient(listOf(
                                    Color(0x663B147E),Color(0xEE15123D)
                                ))
                            ))
                            Column(
                                Modifier.align(Alignment.BottomStart).padding(17.dp),
                                verticalArrangement=Arrangement.spacedBy(6.dp)
                            ){
                                Text("🌟 KARIŞIK MACERA",
                                    color=gold,fontSize=21.sp,fontWeight=FontWeight.Black)
                                Text("3 aşama · 12 görev · doğru serisi ve ustalık puanı",
                                    color=ivory,fontSize=12.sp)
                                Text("▶ Maceraya katıl",color=cyan,
                                    fontSize=14.sp,fontWeight=FontWeight.Black)
                            }
                        }
                        if(last!=null && GradeAwareOperationPolicy.allowed(grade,last!!)){
                            Button(onClick={launch(last!!)},
                                modifier=Modifier.fillMaxWidth().heightIn(min=53.dp),
                                colors=ButtonDefaults.buttonColors(containerColor=gold,
                                    contentColor=ink)){
                                Text("↻ Son oynanan: ${last!!.title}",fontWeight=FontWeight.Black)
                            }
                        }
                        Text("Karışık macera zorluğu",color=ivory,
                            fontWeight=FontWeight.Bold,fontSize=14.sp)
                        DifficultyLevel.entries.chunked(3).forEach { tierRow ->
                            Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) {
                                tierRow.forEach { item ->
                                    OutlinedButton(
                                        onClick={difficulty=item},
                                        modifier=Modifier.weight(1f).heightIn(min=50.dp),
                                        shape=RoundedCornerShape(14.dp),
                                        contentPadding=PaddingValues(horizontal=4.dp),
                                        border=BorderStroke(
                                            if(difficulty==item)2.dp else 1.dp,
                                            if(difficulty==item)gold else cyan.copy(alpha=.4f)
                                        ),
                                        colors=ButtonDefaults.outlinedButtonColors(
                                            containerColor=if(difficulty==item)
                                                Color(0xFF5A4389) else Color(0xFF192F62)
                                        )
                                    ){
                                        Text(
                                            when(item){
                                                DifficultyLevel.EASY->"Temel"
                                                DifficultyLevel.MEDIUM->"Gelişen"
                                                DifficultyLevel.HARD->"Ustalık"
                                                DifficultyLevel.VERY_HARD->"Çok Zor"
                                                DifficultyLevel.EXPERT->"Uzman"
                                            },
                                            color=ivory,fontSize=12.sp,
                                            textAlign=TextAlign.Center
                                        )
                                    }
                                }
                                repeat(3-tierRow.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun OperationGradeLockedScreen(grade:Int,operation:OperationKind,onBack:()->Unit){
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(
            listOf(Color(0xFF147BAA),ink))).padding(24.dp),
        verticalArrangement=Arrangement.Center,
        horizontalAlignment=Alignment.CenterHorizontally
    ){
        Text("🛡️",fontSize=68.sp)
        Spacer(Modifier.height(18.dp))
        Text("${operation.title} macerası henüz açılmadı",
            color=gold,fontSize=22.sp,fontWeight=FontWeight.Black,
            textAlign=TextAlign.Center)
        Text("Profilinde $grade. sınıf seçili. Bu işlem için daha ileri sınıf kapsamı gerekiyor.",
            color=ivory,fontSize=14.sp,textAlign=TextAlign.Center)
        Spacer(Modifier.height(20.dp))
        Button(onClick=onBack){Text("Geri dön")}
    }
}
@Composable
private fun MixedOperationSession(
    grade: Int,
    difficulty: DifficultyLevel,
    onBack: () -> Unit
) {
    val audioContext = androidx.compose.ui.platform.LocalContext.current
    val audio = remember(audioContext) {
        com.yigit033.matematikmacarasi.services.AudioService.getInstance(audioContext)
    }
    var run by remember { mutableIntStateOf(0) }
    var round by remember(run) { mutableIntStateOf(0) }
    var correct by remember(run) { mutableIntStateOf(0) }
    var wrong by remember(run) { mutableIntStateOf(0) }
    var streak by remember(run) { mutableIntStateOf(0) }
    var bestStreak by remember(run) { mutableIntStateOf(0) }
    var points by remember(run) { mutableIntStateOf(0) }

    val engine = remember(grade, difficulty, run) {
        MixedOperationEngine(grade, difficulty)
    }
    val current = remember(round, engine) {
        if (round < 12) engine.next() else null
    }
    val choices = remember(round, engine) {
        current?.let { GradeAwareOperationPolicy.options(it.second, it.first) }
            ?: emptyList()
    }
    var picked by remember(round, run) { mutableStateOf<String?>(null) }
    val wasCorrect = picked != null && picked?.toIntOrNull() == current?.second?.answer

    LaunchedEffect(picked) {
        val selected = picked
        val item = current
        if (selected != null && item != null) {
            val right = selected.toIntOrNull() == item.second.answer
            engine.record(item.first, right)
            if (right) {
                audio.playCorrectSound()
                correct++
                streak++
                bestStreak = maxOf(bestStreak, streak)
                points += 10 + (streak.coerceAtMost(5) * 2)
            } else {
                audio.playWrongSound()
                wrong++
                streak = 0
            }
            // Leave the correct answer visible long enough to learn from mistakes.
            kotlinx.coroutines.delay(if (right) 950L else 1700L)
            round++
        }
    }

    LaunchedEffect(round, run) {
        if (round == 12) audio.playVictorySound()
    }
    BackHandler { onBack() }
    val phase = (round / 4).coerceAtMost(2)
    val phaseNames = listOf("KEŞİF", "YILDIZ YOLU", "USTALIK")
    val accents = listOf(Color(0xFF77E9FF), Color(0xFFFFD775), Color(0xFFBD9BFF))
    val accent = accents[phase]

    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(
                Color(0xFF1265AA), Color(0xFF18265D), Color(0xFF08132D)
            )))
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        TextButton(onClick = onBack) {
            Text("← Dört İşlem Merkezi", color = ivory)
        }
        Text(
            "✨ KARIŞIK MATEMATİK MACERASI",
            color = gold,
            fontSize = 21.sp,
            fontWeight = FontWeight.Black
        )
        Text(
            "$grade. SINIF  ·  ${when(difficulty) {
                DifficultyLevel.EASY -> "TEMEL"
                DifficultyLevel.MEDIUM -> "GELİŞEN"
                DifficultyLevel.HARD -> "USTALIK"
                DifficultyLevel.VERY_HARD -> "ÇOK ZOR"
                DifficultyLevel.EXPERT -> "UZMAN"
            }}",
            color = cyan, fontSize = 12.sp, fontWeight = FontWeight.Bold
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("⭐ $points PUAN", "✓ $correct DOĞRU", "🔥 $streak SERİ").forEach { label ->
                Surface(
                    color = Color(0xFF193B68),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = .6f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        label,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 12.dp),
                        color = ivory, fontSize = 11.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            phaseNames.forEachIndexed { index, name ->
                Surface(
                    color = if (index <= phase) Color(0xFF27558A) else Color(0xFF162D55),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        if (index == phase) 2.dp else 1.dp,
                        if (index == phase) accent else Color(0xFF436085)
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        Modifier.padding(vertical = 9.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            when(index) { 0 -> "🧭"; 1 -> "🌟"; else -> "🏆" },
                            fontSize = 20.sp
                        )
                        Text(name, color = ivory, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        LinearProgressIndicator(
            progress = { round / 12f },
            modifier = Modifier.fillMaxWidth().height(9.dp)
                .clip(RoundedCornerShape(8.dp)),
            color = accent, trackColor = Color(0xFF355583)
        )

        if (round >= 12 || current == null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(25.dp),
                color = Color(0xFF183B6A),
                border = BorderStroke(2.dp, gold)
            ) {
                Column(
                    Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("🏆", fontSize = 54.sp)
                    Text("MACERA TAMAMLANDI!", color = gold, fontSize = 23.sp,
                        textAlign = TextAlign.Center, fontWeight = FontWeight.Black)
                    Text("$points PUAN", color = ivory, fontSize = 28.sp,
                        fontWeight = FontWeight.Black)
                    Text(
                        "12 soruda $correct doğru · $wrong yanlış\n" +
                            "En uzun doğru serin: $bestStreak",
                        color = ivory, textAlign = TextAlign.Center, fontSize = 15.sp
                    )
                    Text(
                        when {
                            correct >= 11 -> "Mükemmel! İşlemleri çok iyi ayırt ettin."
                            correct >= 8 -> "Güzel ilerleme! Kaçırdığın işlemleri yeniden dene."
                            else -> "Pratik yapmaya devam et. Yanlışların öğrenme fırsatı."
                        },
                        color = cyan, fontSize = 13.sp, textAlign = TextAlign.Center
                    )
                    Button(
                        onClick = { run++ },
                        colors = ButtonDefaults.buttonColors(containerColor = gold, contentColor = ink),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
                    ) {
                        Text("↻ Yeni bir macera", fontWeight = FontWeight.Black)
                    }
                    TextButton(onClick = onBack) {
                        Text("İşlem merkezine dön", color = ivory)
                    }
                }
            }
        } else {
            Text(
                "BÖLÜM ${phase + 1}  ·  ${phaseNames[phase]}  ·  SORU ${round + 1}/12",
                color = accent, fontSize = 13.sp, fontWeight = FontWeight.Black
            )
            Surface(
                modifier = Modifier.fillMaxWidth().heightIn(min = 164.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF1A3868),
                border = BorderStroke(2.dp, accent)
            ) {
                Column(
                    Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "${current!!.first.title.uppercase()} GÖREVİ",
                        color = accent, fontSize = 13.sp, fontWeight = FontWeight.Black
                    )
                    Text(
                        "${current.second.first} ${current.first.symbol} ${current.second.second} = ?",
                        color = ivory, fontSize = 37.sp, fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Doğru cevabı seç ve yıldız serini koru!",
                        color = Color(0xFFBDDDF4), fontSize = 12.sp
                    )
                }
            }
            choices.chunked(2).forEach { pair ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    pair.forEach { choice ->
                        val isAnswer = choice.toIntOrNull() == current.second.answer
                        val selected = picked == choice
                        val buttonColor = when {
                            picked != null && isAnswer -> Color(0xFF147B56)
                            picked != null && selected -> Color(0xFFAA3650)
                            else -> Color(0xFF26599B)
                        }
                        Button(
                            onClick = { if (picked == null) picked = choice },
                            enabled = picked == null,
                            modifier = Modifier.weight(1f).heightIn(min = 76.dp),
                            shape = RoundedCornerShape(19.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonColor,
                                disabledContainerColor = buttonColor,
                                disabledContentColor = ivory
                            ),
                            border = BorderStroke(2.dp,
                                if (picked != null && isAnswer) Color(0xFF75F8BF)
                                else Color(0xFF8DBCF4))
                        ) {
                            Text(
                                choice, color = ivory, fontSize = 25.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
            }
            if (picked != null) {
                Surface(
                    color = if (wasCorrect) Color(0xFF165C58) else Color(0xFF6D3049),
                    shape = RoundedCornerShape(15.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        if (wasCorrect) "✓ Doğru! Serini büyüttün. Yeni soruya geçiliyor…"
                        else "✦ Doğru cevap: ${current.second.answer}. " +
                            "Bu işlemi tekrar çalışacağız.",
                        modifier = Modifier.padding(13.dp),
                        color = ivory, fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                Text(
                    "Yanlış cevapta doğru sonucu görürsün. Bu turda süre sınırı yok.",
                    color = Color(0xFFB7DDF5), fontSize = 12.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
