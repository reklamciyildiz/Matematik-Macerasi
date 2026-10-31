package com.yigit033.matematikmacarasi.games.sayiavi2

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R

/**
 * Sayı Avı 2.0 Üst Başlık Çubuğu.
 * Geri butonu, aşama rozeti (1/5 .. 5/5), harita parçası göstergesi ve yıldız sayacı.
 */
@Composable
fun SayiAviTopBar(
    title: String,
    stageSubtitle: String?,
    currentStageNumber: Int, // 1..5 (veya 0 karşılama)
    mapPiecesCollected: Int, // 0, 1, 2
    starsCount: Int,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(SayiAviColors.TopBarScrim)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Sol: Geri butonu ve Başlık
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(SayiAviColors.OceanNavy.copy(alpha = 0.85f), CircleShape)
                        .border(1.dp, SayiAviColors.GoldBorder.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = SayiAviColors.GoldBright,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = SayiAviColors.TextWhite
                    )
                    if (stageSubtitle != null) {
                        Text(
                            text = stageSubtitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = SayiAviColors.SkyLightBlue
                        )
                    }
                }
            }

            // Sağ: Rozetler (Harita parçaları + Yıldız)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Harita Parçaları Sayacı (Stage 1..3 için)
                if (currentStageNumber in 1..5) {
                    Surface(
                        color = SayiAviColors.OceanNavy.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SayiAviColors.GoldBorder.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.map_piece),
                                contentDescription = "Harita Parçası",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "$mapPiecesCollected/2",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = SayiAviColors.GoldBright
                            )
                        }
                    }
                }

                // Yıldız Rozeti
                Surface(
                    color = SayiAviColors.OceanNavy.copy(alpha = 0.85f),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, SayiAviColors.GoldBorder.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Yıldız",
                            tint = SayiAviColors.GoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = starsCount.toString(),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SayiAviColors.TextWhite
                        )
                    }
                }
            }
        }
        if (currentStageNumber in 1..5) {
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { currentStageNumber / 5f },
                modifier = Modifier.fillMaxWidth().height(4.dp),
                color = SayiAviColors.GoldBright,
                trackColor = SayiAviColors.OceanBlue.copy(alpha = 0.4f)
            )
        }
    }
}

/**
 * Sayı Taşı Durumu
 */
enum class NumberTileVisualState {
    NORMAL,
    SELECTED,
    ERROR,
    DISABLED
}

/**
 * Stage A 4x4 Izgara Sayı Taşı Bileşeni.
 * Arka plan olarak resmi WebP kaynaklarını (`number_tile_*.webp`) kullanır;
 * üzerindeki sayıyı yerel, ölçeklenebilir Compose metni olarak çizer.
 */
@Composable
fun NumberTileView(
    number: Int,
    visualState: NumberTileVisualState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp
) {
    val scale by animateFloatAsState(
        targetValue = when (visualState) {
            NumberTileVisualState.SELECTED -> 1.08f
            NumberTileVisualState.ERROR -> 0.95f
            else -> 1.0f
        },
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label = "tile_scale"
    )

    val backgroundRes = when (visualState) {
        NumberTileVisualState.SELECTED -> R.drawable.number_tile_selected
        NumberTileVisualState.ERROR -> R.drawable.number_tile_error
        NumberTileVisualState.DISABLED -> R.drawable.number_tile_disabled
        NumberTileVisualState.NORMAL -> R.drawable.number_tile_normal
    }

    val textColor = when (visualState) {
        NumberTileVisualState.SELECTED -> SayiAviColors.TextWhite
        NumberTileVisualState.ERROR -> SayiAviColors.TextWhite
        NumberTileVisualState.DISABLED -> Color.Gray
        NumberTileVisualState.NORMAL -> SayiAviColors.TextNavy
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .shadow(
                elevation = if (visualState == NumberTileVisualState.SELECTED) 8.dp else 4.dp,
                shape = RoundedCornerShape(12.dp)
            )
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                enabled = visualState != NumberTileVisualState.DISABLED,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = backgroundRes),
            contentDescription = "Sayı Taşı: $number",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.matchParentSize()
        )

        Text(
            text = number.toString(),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Özel Macera Butonu (Yeşil, Turuncu, Mavi veya Pasif).
 * Min dokunma alanı >= 50dp, yüksek çözünürlüklü dokunma geri bildirimi.
 */
@Composable
fun AdventureActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    gradient: androidx.compose.ui.graphics.Brush = SayiAviColors.EmeraldButtonGradient,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    textColor: Color = SayiAviColors.TextWhite
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Gray.copy(alpha = 0.4f)
        ),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(
                elevation = if (enabled) 8.dp else 0.dp,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .background(
                    if (enabled) gradient else androidx.compose.ui.graphics.Brush.linearGradient(
                        listOf(Color(0xFF6B7280), Color(0xFF4B5563))
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.5.dp,
                    color = if (enabled) SayiAviColors.GoldBorder.copy(alpha = 0.8f) else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) textColor else Color(0xFFD1D5DB)
                )
            }
        }
    }
}

/**
 * Parşömen Kart Yüzeyi.
 * İçerisinde görev açıklaması, soru yönergesi ve diyalogların okunaklı sunumu.
 */
@Composable
fun ParchmentCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        color = SayiAviColors.ParchmentLight,
        border = androidx.compose.foundation.BorderStroke(2.dp, SayiAviColors.GoldBorder)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SayiAviColors.ParchmentSurface)
                .padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * Oyundan Çıkış Onay Diyaloğu
 */
@Composable
fun ExitConfirmationDialog(
    onConfirmExit: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Maceradan Ayrıl?",
                fontWeight = FontWeight.Bold,
                color = SayiAviColors.TextNavy
            )
        },
        text = {
            Text(
                text = "Şu anda devam eden bir görevin var. Çıkarsan son aşama kaydedilecek ve dilediğin zaman kaldığın yerden devam edebileceksin.",
                color = SayiAviColors.TextNavyMuted
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmExit,
                colors = ButtonDefaults.buttonColors(containerColor = SayiAviColors.CoralRed)
            ) {
                Text("Çıkış Yap", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Oynamaya Devam Et", color = SayiAviColors.OceanBlue)
            }
        },
        containerColor = SayiAviColors.ParchmentLight,
        shape = RoundedCornerShape(16.dp)
    )
}

