package com.yigit033.matematikmacarasi.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yigit033.matematikmacarasi.services.AudioService

@Composable
fun SettingsDialog(
    onDismiss: () -> Unit,
    audioService: AudioService,
    isBackupMode: Boolean = false // Yedek mod
) {
    // Ses ve müzik durumu için state
    var isMusicEnabled by remember { mutableStateOf(audioService.isMusicEnabled()) }
    var isSoundEnabled by remember { mutableStateOf(audioService.isSoundEnabled()) }
    
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Başlık ve Kapat Butonu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBackupMode) "Ayarlar (Yedek Mod)" else "Ayarlar",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3)
                    )
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Kapat",
                            tint = Color(0xFF2196F3)
                        )
                    }
                }
                
                // Yedek mod uyarısı göster
                if (isBackupMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Not: Ana ayarlar sayfası açılamadığı için basitleştirilmiş ayarlar gösteriliyor.",
                        fontSize = 14.sp,
                        color = Color(0xFFFF9800),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Müzik Ayarı
                SettingItem(
                    title = "Müzik",
                    description = "Arka plan müziğini aç/kapat",
                    checked = isMusicEnabled,
                    onCheckedChange = { isEnabled ->
                        isMusicEnabled = isEnabled
                        audioService.setMusicEnabled(isEnabled)
                    }
                )
                
                Divider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = Color.LightGray.copy(alpha = 0.5f)
                )
                
                // Ses Efektleri Ayarı
                SettingItem(
                    title = "Ses Efektleri",
                    description = "Oyun seslerini aç/kapat",
                    checked = isSoundEnabled,
                    onCheckedChange = { isEnabled ->
                        isSoundEnabled = isEnabled
                        audioService.setSoundEnabled(isEnabled)
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
            )
            
            Text(
                text = description,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF2196F3),
                checkedTrackColor = Color(0xFF2196F3).copy(alpha = 0.5f)
            )
        )
    }
} 