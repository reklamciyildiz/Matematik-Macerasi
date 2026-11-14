package com.yigit033.matematikmacarasi.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
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

/**
 * Compact parent-facing gate. No direct BillingClient checkout in the avatar
 * gallery; all three payment plans are presented by canonical Settings.
 * Premium unlocks ACCESS to special characters, not free star purchases.
 */
@Composable
fun AvatarPremiumGateDialog(
    avatar: Avatar?,
    onDismiss: () -> Unit,
    onViewPlans: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        val gold = Color(0xFFFFD783)
        val shape = RoundedCornerShape(26.dp)
        Card(
            modifier = Modifier.fillMaxWidth()
                .border(1.dp, gold.copy(alpha = 0.7f), shape),
            shape = shape,
            elevation = 14.dp,
            backgroundColor = Color(0xFF12365A)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF234B83), Color(0xFF12365A), Color(0xFF0D2A4C))
                        )
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("♛ PREMIUM KOLEKSİYONU", color = gold,
                    fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                avatar?.let {
                    Box(
                        modifier = Modifier.size(116.dp)
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color(0xFF153C65))
                            .border(1.dp, gold, RoundedCornerShape(25.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedAvatarView(
                            avatar = it, size = 95.dp,
                            contentDescription = it.name, animate = false
                        )
                    }
                    Text(it.name, color = Color.White, fontSize = 21.sp,
                        fontWeight = FontWeight.ExtraBold)
                }
                Text(
                    "Bu kahraman Premium koleksiyonunda. Kilidini açabilmek " +
                        "için Premium hakkı ve yeterli oyun yıldızı gerekiyor.",
                    color = Color(0xFFD7E9FF), fontSize = 14.sp,
                    lineHeight = 20.sp, textAlign = TextAlign.Center
                )
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    listOf(
                        "★ 9 özel avatarı keşfet",
                        "★ Genişletilmiş derslere ve oyunlara eriş",
                        "★ Ayrıntılı macera istatistiklerini incele"
                    ).forEach { feature ->
                        Text(feature, color = Color(0xFFE7F5FF), fontSize = 13.sp,
                            fontWeight = FontWeight.Medium)
                    }
                }
                Button(
                    onClick = onViewPlans,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(17.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = gold, contentColor = Color(0xFF17334F)
                    )
                ) {
                    Text("Premium seçeneklerini gör",
                        fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(46.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    )
                ) {
                    Text("Şimdilik ücretsiz oyna", fontSize = 13.sp)
                }
                Text(
                    "Satın alma seçenekleri ve fiyatlar ebeveyn gözetiminde " +
                        "Google Play üzerinden görüntülenir.",
                    color = Color(0xFFA8C6D8), fontSize = 11.sp,
                    textAlign = TextAlign.Center, lineHeight = 15.sp
                )
            }
        }
    }
}
