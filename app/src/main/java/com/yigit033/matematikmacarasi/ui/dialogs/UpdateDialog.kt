package com.yigit033.matematikmacarasi.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Güncelleme dialog'u
 * @param isForceUpdate Zorla güncelleme mi?
 * @param message Gösterilecek mesaj
 * @param onUpdateClick Güncelle butonuna tıklandığında
 * @param onDismiss İptal butonuna tıklandığında (sadece optional update için)
 */
@Composable
fun UpdateDialog(
    isForceUpdate: Boolean,
    message: String,
    onUpdateClick: () -> Unit,
    onDismiss: () -> Unit = {}
) {
    Dialog(
        onDismissRequest = { if (!isForceUpdate) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isForceUpdate,
            dismissOnClickOutside = !isForceUpdate
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // İkon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = if (isForceUpdate) {
                                    listOf(Color(0xFFFF5252), Color(0xFFFF1744))
                                } else {
                                    listOf(Color(0xFF4CAF50), Color(0xFF2E7D32))
                                }
                            ),
                            shape = RoundedCornerShape(40.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isForceUpdate) Icons.Default.Warning else Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Başlık
                Text(
                    text = if (isForceUpdate) "⚠️ Zorunlu Güncelleme" else "🎉 Yeni Güncelleme!",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isForceUpdate) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Mesaj
                Text(
                    text = message,
                    fontSize = 16.sp,
                    color = Color(0xFF555555),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Butonlar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isForceUpdate) Arrangement.Center else Arrangement.SpaceBetween
                ) {
                    // İptal butonu (sadece optional update için)
                    if (!isForceUpdate) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF666666)
                            )
                        ) {
                            Text(
                                text = "Daha Sonra",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    // Güncelle butonu
                    Button(
                        onClick = onUpdateClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isForceUpdate) Color(0xFFFF5252) else Color(0xFF4CAF50)
                        )
                    ) {
                        Text(
                            text = if (isForceUpdate) "Şimdi Güncelle" else "Güncelle",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

