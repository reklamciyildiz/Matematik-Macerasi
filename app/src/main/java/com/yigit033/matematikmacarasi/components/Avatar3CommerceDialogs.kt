package com.yigit033.matematikmacarasi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yigit033.matematikmacarasi.models.Avatar

private val cGold = Color(0xFFFFD785)
private val cSea = Color(0xFF0D2949)
private val cWhite = Color(0xFFDDF0FF)

@Composable
private fun CommerceSurface(
    header: String,
    avatar: Avatar,
    mainContent: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = {}) {
        Card(
            modifier = Modifier.fillMaxWidth()
                .border(1.dp, cGold.copy(alpha = 0.7f), RoundedCornerShape(25.dp)),
            backgroundColor = cSea, shape = RoundedCornerShape(25.dp),
            elevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .background(Brush.verticalGradient(
                        listOf(Color(0xFF23538A), Color(0xFF0D2949))))
                    .heightIn(max = 610.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(header, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold,
                    color = Color.White, textAlign = TextAlign.Center)
                Avatar3CharacterStage(
                    avatar = avatar, lookId = 0, accessory = 0,
                    effect = 0, size = 112.dp, animate = true
                )
                Text(avatar.name, fontSize = 17.sp,
                    fontWeight = FontWeight.Bold, color = cGold,
                    textAlign = TextAlign.Center)
                mainContent()
            }
        }
    }
}

@Composable
fun Avatar3NeedStarsDialog(
    avatar: Avatar,
    required: Int,
    balance: Int,
    onDismiss: () -> Unit,
    onPlay: () -> Unit
) {
    CommerceSurface(header = "★ Biraz daha yıldız gerekiyor", avatar = avatar) {
        Text("Bu görünüş veya karakter için gereken yıldız:",
            color = cWhite, fontSize = 12.sp, textAlign = TextAlign.Center)
        Row(
            modifier = Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF153C63)).padding(14.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SENDE", color = cWhite, fontSize = 11.sp)
                Text("★ " + balance, color = Color(0xFF9AF3DD),
                    fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("GEREKEN", color = cWhite, fontSize = 11.sp)
                Text("★ " + required, color = cGold,
                    fontSize = 21.sp, fontWeight = FontWeight.Bold)
            }
        }
        Text("★ " + (required - balance).coerceAtLeast(0) +
                " yıldız daha kazanmalısın.",
            color = cGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Button(
            onClick = onPlay,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = cGold, contentColor = cSea)
        ) { Text("Oyunlara dön ve yıldız kazan", fontWeight = FontWeight.Bold) }
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = cWhite)
        ) { Text("Daha sonra") }
    }
}

@Composable
fun Avatar3UnlockConfirmDialog(
    avatar: Avatar,
    balance: Int,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    CommerceSurface(header = "Karakter koleksiyonuna ekle", avatar = avatar) {
        Text(avatar.description, color = cWhite, fontSize = 12.sp,
            textAlign = TextAlign.Center, lineHeight = 17.sp)
        Text("★ " + avatar.price + " yıldız", color = cGold,
            fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
        Text("Mevcut bakiyen: ★ " + balance,
            fontSize = 12.sp, color = cWhite)
        Text("Onayladığında yıldızların bir kez düşer ve karakterin açılır.",
            fontSize = 11.sp, color = cWhite, textAlign = TextAlign.Center)
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(15.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = cGold, contentColor = cSea)
        ) { Text("★ " + avatar.price + " yıldız ile aç", fontWeight = FontWeight.Bold) }
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = cWhite)
        ) { Text("Vazgeç") }
    }
}