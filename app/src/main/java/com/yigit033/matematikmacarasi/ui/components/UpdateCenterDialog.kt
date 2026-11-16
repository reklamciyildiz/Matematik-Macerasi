package com.yigit033.matematikmacarasi.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Shared premium modal for Play update prompts, install-ready and what's-new.
 * It never displays unsupported release claims and never interrupts a game.
 */
enum class UpdateCenterMode { AVAILABLE, READY, WHATS_NEW }

private val Night = Color(0xFF0D244C)
private val Midnight = Color(0xFF172E60)
private val Gold = Color(0xFFFFD47D)
private val Mint = Color(0xFF91F2CF)
private val Pale = Color(0xFFF1F5FF)

@Composable
fun UpdateCenterDialog(
    mode: UpdateCenterMode,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit
) {
    val config = LocalConfiguration.current
    val title = when (mode) {
        UpdateCenterMode.AVAILABLE -> "Yeni bir macera seni bekliyor!"
        UpdateCenterMode.READY -> "Güncellemen hazır!"
        UpdateCenterMode.WHATS_NEW -> "Matematik Macerası yenilendi!"
    }
    val intro = when (mode) {
        UpdateCenterMode.AVAILABLE -> "Daha iyi bir Matematik Macerası deneyimi için yeni sürüme geç."
        UpdateCenterMode.READY -> "Yeni sürüm indirildi. Hazır olduğunda oyuna güncel sürümle devam edebilirsin."
        UpdateCenterMode.WHATS_NEW -> "Yeni sürümüne hoş geldin! Macerana başlamadan önce nelerin değiştiğini keşfet."
    }
    val cta = when (mode) {
        UpdateCenterMode.AVAILABLE -> "Şimdi Güncelle"
        UpdateCenterMode.READY -> "Yeniden Başlat"
        UpdateCenterMode.WHATS_NEW -> "Maceraya Başla"
    }
    Dialog(
        onDismissRequest = onSecondary,
        properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true, usePlatformDefaultWidth = false)
    ) {
        Box(
            Modifier.fillMaxWidth(0.93f)
                .heightIn(max = (config.screenHeightDp * 0.86f).dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Brush.verticalGradient(listOf(Night, Midnight, Color(0xFF243B71))))
                .border(BorderStroke(1.5.dp, Gold.copy(alpha = 0.60f)), RoundedCornerShape(30.dp))
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 25.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.size(82.dp)
                        .clip(CircleShape)
                        .background(Brush.radialGradient(listOf(Color(0xFFFFEFC3), Gold, Color(0xFFE99E4A))))
                        .border(2.dp, Color.White.copy(alpha = 0.65f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (mode == UpdateCenterMode.READY) Icons.Default.CheckCircle
                            else if (mode == UpdateCenterMode.WHATS_NEW) Icons.Default.Stars
                            else Icons.Default.SystemUpdate,
                        contentDescription = null,
                        tint = Night,
                        modifier = Modifier.size(43.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text("MATEMATİK MACERASI", fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp, color = Gold)
                Spacer(Modifier.height(8.dp))
                Text(title, fontSize = 24.sp, lineHeight = 29.sp, fontWeight = FontWeight.ExtraBold,
                    color = Color.White, textAlign = TextAlign.Center)
                Spacer(Modifier.height(10.dp))
                Text(intro, fontSize = 14.sp, lineHeight = 20.sp, color = Pale.copy(alpha = 0.89f),
                    textAlign = TextAlign.Center)
                Spacer(Modifier.height(20.dp))
                Column(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.09f))
                        .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (mode == UpdateCenterMode.WHATS_NEW) {
                        FeatureLine("Daha akıcı güncellemeler", "Artık yeni sürüme geçmek çok daha kolay.")
                        FeatureLine("Yenilikleri keşfet", "Önemli değişiklikleri uygulamanın içinde gör.")
                        FeatureLine("Daha iyi bir deneyim", "Küçük iyileştirmeler ve kararlılık düzenlemeleri.")
                    } else {
                        FeatureLine("Yeni sürüm", "En güncel deneyimi Google Play güvencesiyle edin.")
                        FeatureLine("Güvenli geçiş", "Oyun ilerlemen ve Premium hakların korunur.")
                        if (mode == UpdateCenterMode.AVAILABLE)
                            FeatureLine("Kontrol sende", "Şimdi güncelle veya oyuna devam et.")
                    }
                }
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onPrimary,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 54.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Night)
                ) {
                    Icon(if (mode == UpdateCenterMode.WHATS_NEW) Icons.Default.RocketLaunch
                        else Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(9.dp))
                    Text(cta, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
                if (mode != UpdateCenterMode.WHATS_NEW) {
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onSecondary, modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)) {
                        Text("Daha Sonra", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    if (mode == UpdateCenterMode.READY) "Yeniden başlatma, güncelleme kurulumunu tamamlar."
                    else if (mode == UpdateCenterMode.WHATS_NEW) "İyi eğlenceler!"
                    else "Güncelleme ücretsizdir. Google Play üzerinden yapılır.",
                    fontSize = 11.sp, lineHeight = 16.sp, color = Pale.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun FeatureLine(title: String, description: String) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Icon(Icons.Default.Stars, contentDescription = null, tint = Mint, modifier = Modifier.size(21.dp))
        Column {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(2.dp))
            Text(description, fontSize = 12.sp, lineHeight = 17.sp, color = Pale.copy(alpha = 0.82f))
        }
    }
}

@Composable
fun UpdateDownloadBanner(progress: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 12.dp),
        shape = RoundedCornerShape(17.dp),
        color = Night,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.SystemUpdate, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                Text("Yeni macera hazırlanıyor", fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    color = Color.White, modifier = Modifier.weight(1f))
                if (progress > 0) Text("%" + progress, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
            LinearProgressIndicator(
                progress = progress.coerceIn(0, 100) / 100f,
                modifier = Modifier.fillMaxWidth().height(5.dp).clip(CircleShape),
                color = Gold,
                trackColor = Color.White.copy(alpha = 0.18f)
            )
        }
    }
}
