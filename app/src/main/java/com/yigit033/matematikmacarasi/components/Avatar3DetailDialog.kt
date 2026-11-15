package com.yigit033.matematikmacarasi.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yigit033.matematikmacarasi.models.Avatar
import com.yigit033.matematikmacarasi.services.Avatar3Customization
import com.yigit033.matematikmacarasi.services.Avatar3LookCatalog

private val ocean = Color(0xFF0D2949)
private val pearl = Color(0xFFE1F3FF)
private val amber = Color(0xFFFFD785)
private val mint = Color(0xFF9FFBE0)

/** Full-screen-friendly character workshop: no blank, non-functional tabs. */
@Composable
fun Avatar3DetailDialog(
    avatar: Avatar,
    equippedAvatarId: Int,
    balance: Int,
    premiumActive: Boolean,
    customization: Avatar3Customization,
    onDismiss: () -> Unit,
    onEquip: (Avatar) -> Unit,
    onUnlockAvatar: (Avatar) -> Unit,
    onNeedStars: (Int) -> Unit,
    onNeedPremium: (Avatar) -> Unit,
    onUpdate: () -> Unit
) {
    val context = LocalContext.current
    val looks = remember(avatar.id) { Avatar3LookCatalog.looks(avatar.id) }
    var tab by remember(avatar.id) { mutableStateOf(0) }
    var previewLook by remember(avatar.id) {
        mutableStateOf(customization.selectedLook(avatar.id))
    }
    var accessory by remember(avatar.id) {
        mutableStateOf(customization.selectedAccessory(avatar.id))
    }
    var effect by remember(avatar.id) {
        mutableStateOf(customization.selectedEffect(avatar.id))
    }
    LaunchedEffect(avatar.id) {
        customization.recordPreview(avatar.id, 0)
    }
    val chosen = looks[previewLook]
    val owned = customization.lookOwned(avatar.id, previewLook)
    val canUse = avatar.isUnlocked && (avatar.id <= 6 || premiumActive)

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.94f).fillMaxHeight(0.91f)
                .border(1.dp, amber.copy(alpha = 0.7f), RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp), backgroundColor = ocean, elevation = 14.dp
        ) {
            Column(
                modifier = Modifier.background(
                    Brush.verticalGradient(listOf(Color(0xFF275C8F), Color(0xFF173A64), ocean))
                ).verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("AVATAR ATÖLYESİ", color = amber, fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold)
                        Text(avatar.name, color = Color.White, fontSize = 22.sp,
                            lineHeight = 25.sp, maxLines = 2, fontWeight = FontWeight.ExtraBold)
                    }
                    Text("✕", color = pearl, fontSize = 18.sp,
                        modifier = Modifier.clip(RoundedCornerShape(50))
                            .background(Color(0xFF285477)).clickable { onDismiss() }
                            .padding(horizontal = 15.dp, vertical = 10.dp))
                }
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF0B284B)).padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Avatar3CharacterStage(avatar, previewLook, accessory, effect,
                        size = 156.dp, animate = true)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(chosen.title, color = Color.White, fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold)
                        Text(
                            when {
                                !avatar.isUnlocked -> "Önce karakterin kilidini aç"
                                !canUse -> "Premium erişimi gerekli"
                                owned -> "Koleksiyonunda"
                                else -> "Önizleme · Henüz açılmadı"
                            },
                            color = if (canUse && owned) mint else amber, fontSize = 12.sp
                        )
                    }
                    Text("★ " + balance, color = amber,
                        fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("Görünüşler", "Aksesuarlar", "Efektler", "Rozetler")
                        .forEachIndexed { index, title ->
                            Text(title,
                                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                    .background(if (tab == index) amber else Color(0xFF285174))
                                    .clickable { tab = index }
                                    .padding(horizontal = 13.dp, vertical = 11.dp),
                                color = if (tab == index) ocean else pearl,
                                fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                }
                when (tab) {
                    0 -> {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            looks.forEach { look ->
                                val active = previewLook == look.id
                                val hasLook = customization.lookOwned(avatar.id, look.id)
                                Column(
                                    modifier = Modifier.width(116.dp)
                                        .clip(RoundedCornerShape(17.dp))
                                        .background(if (active) Color(0xFF285982) else Color(0xFF173A60))
                                        .border(if (active) 2.dp else 1.dp,
                                            if (active) amber else Color(0xFF527FA5),
                                            RoundedCornerShape(17.dp))
                                        .clickable {
                                            previewLook = look.id
                                            customization.recordPreview(avatar.id, look.id)
                                        }.padding(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Avatar3CharacterStage(avatar, look.id, accessory, effect,
                                        size = 75.dp, animate = false)
                                    Text(look.title, color = Color.White, fontSize = 12.sp,
                                        lineHeight = 14.sp, fontWeight = FontWeight.Bold,
                                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center)
                                    Text(
                                        when {
                                            !avatar.isUnlocked -> "Önizle"
                                            hasLook && (look.id != 2 || premiumActive) ->
                                                "✓ Kullanılabilir"
                                            look.premiumRequired -> "♛ Premium"
                                            else -> "Önizle"
                                        },
                                        color = if (hasLook) mint else amber,
                                        fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(
                            "Renk stilleri karakterin kendi Lottie çizimini de değiştirir. Yeni bir kıyafet değildir; yıldız harcamaz.",
                            color = pearl.copy(alpha = 0.8f), fontSize = 12.sp,
                            lineHeight = 16.sp)
                    }
                    1 -> {
                        ChoiceGroup(listOf("Sade", "Işık Yıldızı", "Altın Taç"),
                            accessory, true) { value ->
                            accessory = value
                            if (canUse) {
                                if (customization.setAccessory(avatar.id, value)) onUpdate()
                            } else {
                                Toast.makeText(context,
                                    "Aksesuarı önizliyorsun. Kaydetmek için önce avatarı aç.",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                        Text(if (canUse) "Ücretsiz aksesuar · seçimin kaydedilir."
                            else "Ücretsiz dene · kaydetmek için avatarı aç.",
                            color = pearl.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                    2 -> {
                        ChoiceGroup(listOf("Efektsiz", "Işıltılı Parıltı", "Altın Halo"),
                            effect, true) { value ->
                            effect = value
                            if (canUse) {
                                if (customization.setEffect(avatar.id, value)) onUpdate()
                            } else {
                                Toast.makeText(context,
                                    "Efekti önizliyorsun. Kaydetmek için önce avatarı aç.",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                        Text(if (canUse) "Ücretsiz efekt · seçimin kaydedilir."
                            else "Ücretsiz dene · kaydetmek için avatarı aç.",
                            color = pearl.copy(alpha = 0.85f), fontSize = 12.sp)
                    }
                    else -> {
                        val tried = customization.previewCount(avatar.id)
                        val badges = listOf(
                            Triple("Atölye Kaşifi", "Avatar atölyesini ziyaret et.", true),
                            Triple("Stil Meraklısı", "İki farklı görünüşü dene (${tried.coerceAtMost(2)}/2).", tried >= 2),
                            Triple("Karakter Sahibi", "Bu avatarın kilidini yıldızlarla aç.", avatar.isUnlocked),
                            Triple("Galaksi Ustası",
                                "Premium erişimiyle Galaksi stilini seç.",
                                avatar.isUnlocked && premiumActive &&
                                    customization.selectedLook(avatar.id) == 2)
                        )
                        badges.forEach { badge ->
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (badge.third) Color(0xFF1E5968)
                                        else Color(0xFF193A60))
                                    .clickable {
                                        Toast.makeText(context, badge.second,
                                            Toast.LENGTH_SHORT).show()
                                    }
                                    .padding(13.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text((if (badge.third) "✓ " else "🔒 ") + badge.first,
                                    color = if (badge.third) mint else pearl,
                                    fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(badge.second, color = pearl.copy(alpha = 0.85f),
                                    fontSize = 11.sp)
                            }
                        }
                    }
                }
                if (tab == 0) {
                    val action = when {
                        !avatar.isUnlocked -> "Karakteri aç · ★ " + avatar.price
                        !canUse || (chosen.premiumRequired && !premiumActive) ->
                            "Premium seçeneklerini gör"
                        owned && customization.selectedLook(avatar.id) == previewLook ->
                            "✓ Bu görünüş seçili"
                        owned -> "Bu görünüşü kullan"
                        else -> "Renk stilini kullan"
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !(owned && canUse &&
                            customization.selectedLook(avatar.id) == previewLook),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(backgroundColor = amber,
                            contentColor = ocean, disabledBackgroundColor = Color(0xFF358578),
                            disabledContentColor = Color.White),
                        onClick = {
                            when {
                                !avatar.isUnlocked -> onUnlockAvatar(avatar)
                                !canUse || (chosen.premiumRequired && !premiumActive) ->
                                    onNeedPremium(avatar)
                                owned -> if (customization.selectLook(avatar.id, previewLook)) {
                                    onUpdate()
                                    Toast.makeText(context, "Görünüş seçildi", Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    // No stars for Lottie recoloring: no currency debit.
                                    if (customization.selectLook(avatar.id, previewLook)) {
                                        onUpdate()
                                        Toast.makeText(context, "Renk stili seçildi",
                                            Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    ) { Text(action, fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                }
                if (canUse && equippedAvatarId != avatar.id) {
                    OutlinedButton(
                        onClick = { onEquip(avatar) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) { Text("Bu karakteri kullan", fontWeight = FontWeight.Bold) }
                }
                Text("Eski avatar ve yıldızların korunur. Renk paletleri yıldızla satılmaz; " +
                    "Premium palet için aktif üyelik gerekir.",
                    color = pearl.copy(alpha = 0.75f), fontSize = 11.sp,
                    lineHeight = 15.sp)
            }
        }
    }

}

@Composable
private fun ChoiceGroup(
    choices: List<String>, selected: Int, enabled: Boolean, onSelect: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        choices.forEachIndexed { index, title ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                    .background(if (selected == index) Color(0xFF286F77) else Color(0xFF193A60))
                    .clickable(enabled = enabled) { onSelect(index) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, color = Color.White, fontSize = 13.sp,
                    fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                if (selected == index) Text("✓", color = mint)
            }
        }
        if (!enabled) Text("Özelleştirmek için önce bu avatarı aç.",
            color = pearl.copy(alpha = 0.75f), fontSize = 12.sp)
    }
}