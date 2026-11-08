package com.yigit033.matematikmacarasi.games.common

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Pause, never erase, a Premium checkpoint when the subscription is absent. */
@Composable
fun PremiumChallengePause(onSettings:()->Unit,onExit:()->Unit,onPlayBasic:()->Unit) {
    BackHandler(onBack=onExit)
    Column(Modifier.fillMaxSize().background(Brush.verticalGradient(
        listOf(Color(0xFF174D75),Color(0xFF0B2A47))))
        .safeDrawingPadding().padding(24.dp),
        horizontalAlignment=Alignment.CenterHorizontally,
        verticalArrangement=Arrangement.Center) {
        Text("🔒",fontSize=42.sp)
        Spacer(Modifier.height(12.dp))
        Text("Orta ve Zor Seviye · Premium",
            color=Color(0xFFFFD980),fontSize=25.sp,
            fontWeight=FontWeight.ExtraBold,textAlign=TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        Text("Bu maceradaki ilerlemen korunuyor. Premium erişimin geri " +
            "yüklendiğinde kaldığın yerden devam edebilirsin. " +
            "Temel seviye ücretsiz olarak oynanabilir.",
            color=Color(0xFFE2F2F9),fontSize=15.sp,
            lineHeight=23.sp,textAlign=TextAlign.Center)
        Spacer(Modifier.height(24.dp))
        Button(onClick=onPlayBasic,modifier=Modifier.fillMaxWidth().heightIn(min=54.dp),
            shape=RoundedCornerShape(17.dp),
            colors=ButtonDefaults.buttonColors(containerColor=Color(0xFFFFD980),
                contentColor=Color(0xFF0C2944))) {
            Text("Ücretsiz Temel Seviyede Oyna",fontWeight=FontWeight.Bold)
        }
        Spacer(Modifier.height(9.dp))
        OutlinedButton(onClick=onSettings,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
            shape=RoundedCornerShape(17.dp)) {
            Text("Premium Hakkını Kontrol Et",color=Color.White)
        }
        Spacer(Modifier.height(9.dp))
        OutlinedButton(onClick=onExit,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),
            shape=RoundedCornerShape(17.dp)){
            Text("Oyunlara Dön",color=Color.White)
        }
    }
}
