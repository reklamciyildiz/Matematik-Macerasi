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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.models.Avatar

private val golden = Color(0xFFFFD783)
private val light = Color(0xFFE3F3FF)

/**
 * Avatar 3.0: one selected-preview composition, not 15 concurrent animations.
 * IDs, unlock prices and Premium permissions remain owned by AvatarService.
 */
@Composable
fun AvatarCollectionHero(
    avatar: Avatar,
    equippedId: Int,
    unlockedCount: Int,
    totalCount: Int,
    starBalance: Int,
    premiumActive: Boolean,
    lookId: Int = 0,
    accessory: Int = 0,
    effect: Int = 0,
    onPrimaryAction: (Avatar) -> Unit
) {
    val requiresPremium = avatar.id > 6 && !premiumActive
    val accessible = avatar.isUnlocked && !requiresPremium
    val equipped = accessible && equippedId == avatar.id
    val actionLabel = when {
        equipped -> "Kullanımda"
        requiresPremium -> "Premium ile keşfet"
        accessible -> "Bu avatarı seç"
        avatar.price == 0 -> "Ücretsiz aç"
        else -> "${avatar.price} yıldız ile aç"
    }
    val accent = if (avatar.id > 6) golden else Color(0xFF89E9D6)
    val shape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF295C8D), Color(0xFF17416C), Color(0xFF122C52))
                )
            )
            .border(1.dp, accent.copy(alpha = 0.65f), shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("KARAKTER KOLEKSİYONU", color = accent,
                    fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                Text("Kendi kahramanını seç",
                    color = Color.White, fontSize = 21.sp,
                    fontWeight = FontWeight.ExtraBold, maxLines = 2)
            }
            Text("$unlockedCount / $totalCount",
                color = light, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF092744).copy(alpha = 0.7f))
                    .border(1.dp, accent.copy(alpha = 0.8f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Avatar3CharacterStage(
                    avatar = avatar,
                    lookId = lookId,
                    accessory = accessory,
                    effect = effect,
                    size = 96.dp,
                    animate = true
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    avatar.name, color = Color.White, fontSize = 19.sp,
                    fontWeight = FontWeight.Bold, maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    if(avatar.description.isBlank())
                        "Her kahramanın farklı bir matematik macerası var."
                    else avatar.description,
                    color = light, fontSize = 12.sp, lineHeight = 17.sp,
                    maxLines = 3, overflow = TextOverflow.Ellipsis
                )
                Text(
                    when {
                        equipped -> "✓ Aktif avatarın"
                        requiresPremium -> "♛ Premium karakter"
                        accessible -> "✓ Koleksiyonunda"
                        else -> "★ $starBalance yıldızın var"
                    },
                    color = accent, fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Button(
            onClick = { onPrimaryAction(avatar) },
            enabled = !equipped,
            modifier = Modifier.fillMaxWidth().height(46.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = golden,
                contentColor = Color(0xFF18314D),
                disabledBackgroundColor = Color(0xFF368675),
                disabledContentColor = Color.White
            )
        ) {
            Text(actionLabel, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
        }
        Text(
            if(premiumActive)
                "Premium aktif · Karakterlerin kilidini yıldızlarınla aç."
            else
                "6 ücretsiz, 9 Premium karakter · Yıldızların her zaman korunur.",
            color = light.copy(alpha = 0.82f), fontSize = 11.sp
        )
    }
}
