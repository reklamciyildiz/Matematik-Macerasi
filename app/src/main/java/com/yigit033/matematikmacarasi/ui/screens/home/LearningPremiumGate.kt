package com.yigit033.matematikmacarasi.ui.screens.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.screens.SettingsActivity

private val paywallGold = Color(0xFFFFD88F)
private val paywallCyan = Color(0xFF8BDAD6)

/**
 * An informational adult-facing Premium entry, never an automatic purchase.
 * All billing, refund/restore and legacy Cloudflare code processing remain in
 * the app's existing Settings/PurchaseManager system.
 */
@Composable
internal fun LearningPremiumDialog(
    title: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = paywallGold) },
        title = {
            Text("Premium ders", fontWeight = FontWeight.ExtraBold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("“$title” dersi Premium kütüphanesinde.")
                Text(
                    "Her sınıfta iki başlangıç dersi ücretsizdir. Premium ile bütün " +
                        "mevcut kısa dersleri ve yeni derinleşme derslerini açabilirsin."
                )
                Text(
                    "Bu ürün mevcut Matematik Macerası Premium hakkına dahildir. " +
                        "Tek seferlik satın alımdır; abonelik değildir.",
                    fontSize = 12.sp
                )
                Text(
                    "Satın alma ve hak geri yükleme işlemlerini bir yetişkinin " +
                        "Ayarlar sayfasında incelemesi gerekir.",
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    context.startActivity(Intent(context, SettingsActivity::class.java))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF145977))
            ) {
                Text("Yetişkin için Premium seçenekleri")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Ücretsiz derslere dön")
            }
        }
    )
}

@Composable
internal fun LearningPremiumBlockedScreen(
    lessonName: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize()
            .background(Color(0xFF0E2F4F))
            .padding(22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null,
            tint = paywallGold, modifier = Modifier.padding(12.dp))
        Text("Premium ders", color = paywallGold,
            fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
        Text(lessonName, color = Color.White,
            fontSize = 18.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
        Text(
            "Bu derse erişmek için mevcut Premium hakkın etkin olmalı. " +
                "Ücretsiz iki başlangıç dersini her zaman oynayabilirsin.",
            color = Color(0xFFE0EAF4), fontSize = 13.sp
        )
        Spacer(modifier = Modifier.padding(10.dp))
        Button(onClick = onBack, modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp)) {
            Text("Konu kitaplığına dön")
        }
    }
}
