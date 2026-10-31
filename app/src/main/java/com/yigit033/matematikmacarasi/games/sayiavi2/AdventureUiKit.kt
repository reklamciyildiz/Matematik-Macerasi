package com.yigit033.matematikmacarasi.games.sayiavi2

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
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
