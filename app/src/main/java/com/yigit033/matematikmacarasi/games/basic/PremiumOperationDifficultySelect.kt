package com.yigit033.matematikmacarasi.games.basic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.yigit033.matematikmacarasi.models.DifficultyLevel
import com.yigit033.matematikmacarasi.services.PlayerProfileService

private val indigo=Color(0xFF081836)
private val light=Color(0xFFF5FDFF)
private val gold=Color(0xFFFFDB75)
@Composable
fun PremiumOperationDifficultySelect(
    title:String,
    onDifficultySelected:(DifficultyLevel)->Unit
){
    val context=LocalContext.current
    val grade=GradeAwareOperationPolicy.grade(
        PlayerProfileService.getInstance(context).getUserClass())
    val op=when{
        title.contains("Çıkarma")->OperationKind.SUBTRACT
        title.contains("Çarpma")->OperationKind.MULTIPLY
        title.contains("Bölme")->OperationKind.DIVIDE
        else->OperationKind.ADD
    }
    val image=when(op){
        OperationKind.ADD->R.drawable.card_addition_premium
        OperationKind.SUBTRACT->R.drawable.card_subtraction_premium
        OperationKind.MULTIPLY->R.drawable.card_multiplication_premium
        OperationKind.DIVIDE->R.drawable.card_division_premium
    }
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(
            listOf(Color(0xFF0A78B1),Color(0xFF173967),indigo)))
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(15.dp),
        horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.spacedBy(11.dp)
    ){
        Text("⭐ MATEMATİK MACERASI",color=gold,fontSize=15.sp,
            fontWeight=FontWeight.Black)
        Text(op.title+" Ustalığı",fontSize=26.sp,color=light,
            fontWeight=FontWeight.Black)
        Box(Modifier.fillMaxWidth().height(185.dp)
            .clip(RoundedCornerShape(22.dp))){
            Image(painterResource(image),contentDescription=null,
                contentScale=ContentScale.Crop,modifier=Modifier.fillMaxSize())
            Box(Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Transparent,
                    indigo.copy(alpha=.88f)))))
            Text("$grade. SINIF · ${op.symbol}",
                modifier=Modifier.align(Alignment.BottomStart).padding(12.dp),
                color=light,fontWeight=FontWeight.Black,fontSize=18.sp)
        }
        Surface(shape=RoundedCornerShape(15.dp),
            color=Color(0xFF17446D),
            border=BorderStroke(1.dp,Color(0xFF6EE7F6))){
            Text("Sınıfının matematik sınırları bütün seviyelerde korunur.",
                modifier=Modifier.fillMaxWidth().padding(12.dp),
                color=light,fontSize=12.sp,textAlign=TextAlign.Center)
        }
        DifficultyLevel.entries.forEachIndexed{index,difficulty->
            val cap=GradeAwareOperationPolicy.maxAnswer(grade,difficulty)
            val labels=listOf("Başlangıç","Keşif","Cesur Kaşif",
                "Usta Pilot","Galaksi Ustası")
            val colors=listOf(Color(0xFF15C889),Color(0xFF1FAAD9),
                Color(0xFF7267DE),Color(0xFFE484A1),Color(0xFFDAA23D))
            Surface(
                onClick={onDifficultySelected(difficulty)},
                modifier=Modifier.fillMaxWidth().heightIn(min=88.dp),
                shape=RoundedCornerShape(17.dp),
                color=Color(0xFF172D61),
                border=BorderStroke(2.dp,colors[index])
            ){
                Row(Modifier.fillMaxWidth().padding(13.dp),
                    verticalAlignment=Alignment.CenterVertically,
                    horizontalArrangement=Arrangement.spacedBy(12.dp)){
                    Surface(color=colors[index],
                        shape=RoundedCornerShape(13.dp),
                        modifier=Modifier.size(52.dp)){
                        Box(contentAlignment=Alignment.Center){
                            Text("${index+1}",fontSize=23.sp,
                                fontWeight=FontWeight.Black,color=indigo)
                        }
                    }
                    Column(Modifier.weight(1f)){
                        Text(labels[index],color=light,
                            fontSize=16.sp,fontWeight=FontWeight.Bold)
                        Text(if(op==OperationKind.ADD || op==OperationKind.SUBTRACT)
                            "$cap'e kadar sonuç"
                            else "${grade}. sınıfa uygun işlem grupları",
                            color=Color(0xFFB8DDF0),fontSize=12.sp)
                    }
                    Text("▶",color=gold,fontSize=23.sp)
                }
            }
        }
    }
}
