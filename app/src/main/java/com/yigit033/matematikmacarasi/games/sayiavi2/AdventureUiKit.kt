package com.yigit033.matematikmacarasi.games.sayiavi2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.TextButton
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveProgress
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * S2.3 ortak oyun deneyimi bileşenleri.
 * Hem Kumsal Koyu hem Gizemli Orman aynı okunabilir ilerleme ve geri bildirim düzenini kullanır.
 * Satın alma, istatistik veya kayıt mantığı burada bulunmaz.
 */
internal object AdventureTokens {
    val Ink = Color(0xFF14293A)
    val Cream = Color(0xFFFFF8E7)
    val Gold = Color(0xFFFFD57B)
    val Emerald = Color(0xFF1D8669)
    val Positive = Color(0xFFBCECD9)
    val Negative = Color(0xFFFFD7C5)
    val Night = Color(0xFF0B2031)
}

@Composable
internal fun AdventureSectionProgress(
    label: String,
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
    accent: Color = AdventureTokens.Gold
) {
    require(total > 0)
    val fraction by animateFloatAsState(
        targetValue = (completed.toFloat() / total).coerceIn(0f, 1f),
        label = "Section progress"
    )
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                color = AdventureTokens.Cream, letterSpacing = .4.sp)
            Text("${completed.coerceIn(0, total)} / $total",
                fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accent)
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(7.dp),
            color = accent,
            trackColor = Color.White.copy(alpha = .18f)
        )
    }
}

@Composable
internal fun AdventureFeedbackBanner(
    message: String,
    positive: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(visible = message.isNotBlank(), modifier = modifier) {
        Surface(
            color = if (positive) AdventureTokens.Positive else AdventureTokens.Negative,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp,
                if (positive) AdventureTokens.Emerald else Color(0xFFB66449))
        ) {
            Text(
                text = message,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AdventureTokens.Ink
            )
        }
    }
}

/**
 * Seviye seçim ekranı yalnızca oyun başlangıcında kullanılır.
 * Öğrenci profili varsayılanı belirler; manuel tercih öneriyi askıya alır.
 */
@Composable
internal fun AdventureTierSelector(
    progress: AdaptiveProgress,
    gradeLabel: String,
    onChange: (LearningTier?) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = AdventureTokens.Cream,
        border = androidx.compose.foundation.BorderStroke(1.dp, AdventureTokens.Gold)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("BECERİ SEVİYESİ", fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp, color = AdventureTokens.Ink, letterSpacing = .8.sp)
            Text("Profil: $gradeLabel · Önerilen: ${progress.autoTier.title}",
                fontSize = 12.sp, color = AdventureTokens.Ink)
            Text("Bu ayar yalnızca yeni oyunu etkiler. Daha önceki kazanımların korunur.",
                fontSize = 11.sp, color = AdventureTokens.Ink)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LearningTier.entries.forEach { tier ->
                    val selected = progress.currentTier == tier
                    Surface(
                        modifier = Modifier.weight(1f)
                            .height(50.dp)
                            .clickable(role = Role.Button) { onChange(tier) }
                            .semantics {
                                contentDescription = "${tier.title} düzeyi${if (selected) ", seçili" else ""}"
                            },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) AdventureTokens.Emerald else Color(0xFFE0EAE4),
                        border = androidx.compose.foundation.BorderStroke(1.dp,
                            if (selected) AdventureTokens.Emerald else Color(0xFF92B1A8))
                    ) {
                        androidx.compose.foundation.layout.Box(
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Text(tier.title, fontSize = 12.sp, fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else AdventureTokens.Ink)
                        }
                    }
                }
            }
            TextButton(onClick = { onChange(null) }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (progress.manualTier == null) "✓ Otomatik uyarlama açık"
                    else "Otomatik öneriye geri dön",
                    color = AdventureTokens.Emerald,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
