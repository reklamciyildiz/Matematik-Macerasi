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
                                                else "🔒 3. sınıfta açılır",
                                                color=if(available)gold else Color(0xFF9AB9D2),
                                                fontSize=11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        Surface(
                            onClick={mixed=true},
                            shape=RoundedCornerShape(20.dp),
                            color=Color(0xFF5525A5),
                            border=BorderStroke(2.dp,Color(0xFFD8A4FF))
                        ){
                            Column(Modifier.fillMaxWidth().padding(16.dp),
                                verticalArrangement=Arrangement.spacedBy(8.dp)){
                                Text("✨ KARIŞIK MACERA",color=gold,
                                    fontSize=18.sp,fontWeight=FontWeight.Black)
                                Text("Sınıfına uygun işlemlerden 12 sürpriz soru. Her turda farklı sorular.",
                                    color=ivory,fontSize=13.sp)
                                Text("▶ Oyna",color=cyan,fontWeight=FontWeight.Bold)
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
                        Row(horizontalArrangement=Arrangement.spacedBy(5.dp)){
                            listOf(DifficultyLevel.EASY,DifficultyLevel.MEDIUM,
                                DifficultyLevel.HARD).forEach{item->
                                OutlinedButton(onClick={difficulty=item},
                                    modifier=Modifier.weight(1f),
                                    contentPadding=PaddingValues(horizontal=4.dp),
                                    border=BorderStroke(if(difficulty==item)2.dp else 1.dp,
                                        if(difficulty==item)gold else cyan.copy(alpha=.4f))){
                                    Text(when(item){
                                        DifficultyLevel.EASY->"Temel"
                                        DifficultyLevel.MEDIUM->"Gelişen"
                                        else->"Ustalık"
                                    },color=ivory,fontSize=11.sp)
                                }
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
private fun MixedOperationSession(grade:Int,difficulty:DifficultyLevel,onBack:()->Unit){
    var round by rememberSaveable {mutableIntStateOf(0)}
    var correct by rememberSaveable {mutableIntStateOf(0)}
    var wrong by rememberSaveable {mutableIntStateOf(0)}
    var pending by rememberSaveable {mutableStateOf(false)}
    val allowed=remember(grade){OperationKind.entries.filter{
        GradeAwareOperationPolicy.allowed(grade,it)
    }}
    val question=remember(round,grade,difficulty){
        val op=allowed.random()
        op to GradeAwareOperationPolicy.generate(grade,op,difficulty)
    }
    val choices=remember(round){GradeAwareOperationPolicy.options(question.second)}
    BackHandler {onBack()}
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(
        listOf(Color(0xFF1D64B8),ink))).safeDrawingPadding().padding(16.dp),
        horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.spacedBy(16.dp)){
        TextButton(onClick=onBack,modifier=Modifier.align(Alignment.Start)){
            Text("← İşlem Merkezi",color=ivory)
        }
        Text("🌟 KARIŞIK MACERA",color=gold,fontSize=24.sp,fontWeight=FontWeight.Black)
        Text("$grade. sınıf · $correct doğru · $wrong yanlış",
            color=ivory,fontSize=13.sp)
        LinearProgressIndicator(
            progress={round/12f},
            modifier=Modifier.fillMaxWidth().height(10.dp),
            color=gold,trackColor=Color(0xFF355583))
        if(round>=12){
            Text("🏆 MACERA TAMAMLANDI",fontSize=22.sp,color=gold,
                fontWeight=FontWeight.Black)
            Text("12 soruda $correct doğru cevap",color=ivory,fontSize=18.sp)
            Button(onClick={round=0;correct=0;wrong=0;pending=false}){
                Text("Yeniden oyna")
            }
        }else {
            Spacer(Modifier.height(12.dp))
            Text("Soru ${round+1} / 12",color=cyan,fontSize=15.sp)
            Surface(
                modifier=Modifier.fillMaxWidth().height(155.dp),
                color=Color(0xFF183867),
                shape=RoundedCornerShape(25.dp),
                border=BorderStroke(2.dp,cyan)
            ){
                Box(contentAlignment=Alignment.Center){
                    Text("${question.second.first} ${question.first.symbol} ${question.second.second} = ?",
                        fontSize=35.sp,fontWeight=FontWeight.Black,color=ivory,
                        textAlign=TextAlign.Center)
                }
            }
            choices.chunked(2).forEach{pair->
                Row(horizontalArrangement=Arrangement.spacedBy(12.dp)){
                    pair.forEach{choice->
                        Button(onClick={
                            if(!pending){
                                pending=true
                                if(choice.toInt()==question.second.answer)correct++
                                else wrong++
                                round++
                                pending=false
                            }
                        },
                            modifier=Modifier.weight(1f).height(87.dp),
                            shape=RoundedCornerShape(20.dp),
                            colors=ButtonDefaults.buttonColors(
                                containerColor=Color(0xFF20649A))){
                            Text(choice,color=ivory,fontSize=27.sp,
                                fontWeight=FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}
