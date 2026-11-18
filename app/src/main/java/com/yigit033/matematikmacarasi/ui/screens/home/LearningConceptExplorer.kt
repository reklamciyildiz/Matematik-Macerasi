package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.CurriculumTopic
import com.yigit033.matematikmacarasi.learning.TopicArea

/**
 * Optional hands-on concept preview for younger learners only.
 * It is not a quiz or a completion signal; scores/premium state remain untouched.
 * Advanced grades keep their topic-specific guided diagrams instead.
 */
@Composable
internal fun LearningConceptExplorer(topic: CurriculumTopic) {
    if (topic.grade > 4) return
    when (topic.area) {
        TopicArea.NUMBERS, TopicArea.OPERATIONS -> InteractiveMathLine(topic.id)
        TopicArea.GEOMETRY -> InteractiveBasicShape(topic.id)
        else -> Unit
    }
}

private val explorerCyan = Color(0xFF8CE7DF)
private val explorerText = Color(0xFFC6E1ED)
private val explorerGold = Color(0xFFFFD78B)

@Composable
private fun ExplorerSurface(title: String, explanation: String, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .background(Color(0xFF174765), RoundedCornerShape(19.dp))
            .border(1.dp, Color(0xFF528EA7), RoundedCornerShape(19.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(title, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        Text(explanation, color = explorerText, fontSize = 12.sp, lineHeight = 17.sp)
        content()
    }
}

@Composable
private fun InteractiveMathLine(topicId: String) {
    var marked by rememberSaveable(topicId) { mutableIntStateOf(4) }
    ExplorerSurface("Sayı doğrusunu keşfet",
        "Sayıları soldan sağa sıralarız. Bir adım sağa gidince sayı 1 artar.") {
        Canvas(
            Modifier.fillMaxWidth().height(58.dp)
                .semantics { contentDescription =
                    "Sayı doğrusu 0 ile 10 arasında. İşaretli sayı " + marked }
        ) {
            val left = 13.dp.toPx()
            val right = size.width - left
            val y = size.height * 0.55f
            val tick = (right - left) / 10f
            drawLine(Color(0xFFB8DDEC), Offset(left, y), Offset(right, y),
                strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
            for (n in 0..10) {
                val x = left + n * tick
                drawLine(Color(0xFFB8DDEC), Offset(x, y - 6.dp.toPx()),
                    Offset(x, y + 6.dp.toPx()), 2.dp.toPx())
                if (n == marked) {
                    drawCircle(explorerGold, 10.dp.toPx(), Offset(x,y))
                    drawCircle(Color(0xFF194963), 4.dp.toPx(), Offset(x,y))
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            for (n in 0..10) {
                Text(n.toString(), color = if (n == marked) explorerGold else explorerText,
                    fontSize = 11.sp, fontWeight = if (n == marked)
                        FontWeight.ExtraBold else FontWeight.Normal)
            }
        }
        Text("İşaretli sayı: " + marked,
            color = explorerGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(
                onClick = { marked = (marked - 1).coerceAtLeast(0) },
                enabled = marked > 0,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF286980), contentColor = Color.White)
            ) {
                Text("Bir geri", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Button(
                onClick = { marked = (marked + 1).coerceAtMost(10) },
                enabled = marked < 10,
                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF28798E), contentColor = Color.White)
            ) {
                Text("Bir ileri", fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }
        Text("Bu bir keşif örneğidir; dersin yerine geçmez.",
            color = explorerText, fontSize = 11.sp)
    }
}

@Composable
private fun InteractiveBasicShape(topicId: String) {
    var rectangle by rememberSaveable(topicId) { mutableIntStateOf(0) }
    ExplorerSurface("Şekilleri incele",
        "Köşelerin ve kenarların sayısını karşılaştır. Şekli değiştirerek farkı gör.") {
        Canvas(
            Modifier.fillMaxWidth().height(135.dp).semantics {
                contentDescription = if (rectangle == 0)
                    "Üçgen. Üç kenar ve üç köşe."
                else "Dikdörtgen. Dört kenar ve dört köşe."
            }
        ) {
            val centerX = size.width / 2f
            val minW = size.width.coerceAtMost(270.dp.toPx())
            val shape = Path()
            if (rectangle == 0) {
                shape.moveTo(centerX, 15.dp.toPx())
                shape.lineTo(centerX - minW*.36f, size.height - 14.dp.toPx())
                shape.lineTo(centerX + minW*.36f, size.height - 14.dp.toPx())
                shape.close()
            } else {
                shape.moveTo(centerX - minW*.37f, 18.dp.toPx())
                shape.lineTo(centerX + minW*.37f, 18.dp.toPx())
                shape.lineTo(centerX + minW*.37f, size.height - 17.dp.toPx())
                shape.lineTo(centerX - minW*.37f, size.height - 17.dp.toPx())
                shape.close()
            }
            drawPath(shape, Color(0x5572CDE1))
            drawPath(shape, explorerGold, style = Stroke(width = 4.dp.toPx(),
                cap = StrokeCap.Round))
        }
        Text(
            if (rectangle == 0) "Üçgen · 3 kenar · 3 köşe"
            else "Dikdörtgen · 4 kenar · 4 köşe",
            color = explorerGold, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp
        )
        Button(
            onClick = { rectangle = 1 - rectangle },
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF28798E), contentColor = Color.White)
        ) {
            Text("Diğer şekli incele", fontWeight = FontWeight.Bold)
        }
        Text("Bu temel şekil örneği, sınıfının bütün geometri kazanımlarını kapsamaz.",
            color = explorerText, fontSize = 11.sp, lineHeight = 16.sp)
    }
}
