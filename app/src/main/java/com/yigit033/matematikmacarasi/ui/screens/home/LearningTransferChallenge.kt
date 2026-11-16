package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.learning.LearningPathCatalog
import com.yigit033.matematikmacarasi.learning.Lesson
import com.yigit033.matematikmacarasi.learning.Skill

/**
 * All grades get a real, replayable one-question transfer challenge after
 * the lesson evaluation. It is not a hidden fifth paid quiz, leaderboard,
 * virtual currency, or a false "mastery" badge.
 *
 * Reuses only the already authorised grade-scoped lesson question bank.
 */
@Composable
fun LearningTransferChallenge(
    lesson: Lesson,
    sessionSeed: Long,
    modifier: Modifier = Modifier
) {
    var round by remember(lesson.id) { mutableIntStateOf(0) }
    val question = remember(lesson.id, sessionSeed, round) {
        LearningPathCatalog.question(
            lesson, sessionSeed + 1_102_117L + round * 71_011L, 0
        )
    }
    var chosen by remember(lesson.id, round) { mutableStateOf<Int?>(null) }
    var attempts by remember(lesson.id, round) { mutableIntStateOf(0) }
    val solved = chosen == question.answer

    Column(
        modifier = modifier.fillMaxWidth()
            .background(Color(0xFF1B5067), RoundedCornerShape(21.dp))
            .padding(15.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(
                Icons.Filled.AutoAwesome, contentDescription = null,
                tint = Color(0xFFFFD58C)
            )
            Text(
                "5/5 · OYUNLA PEKİŞTİR",
                color = Color(0xFFFFD58C),
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
        Text(
            "Mini Oyun · Bilgi Sandığı",
            color = Color.White, fontSize = 19.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            "Hazır olduğunda aynı konudan bir macera sorusunu çöz. " +
                "Yeni görev ile farklı sayılarla tekrar oynayabilirsin.",
            color = Color(0xFFCCE5EC), fontSize = 12.sp, lineHeight = 18.sp
        )
        Text(
            question.text,
            color = Color.White, fontSize = 18.sp,
            fontWeight = FontWeight.Bold, lineHeight = 25.sp
        )
        if (lesson.skill == Skill.JOURNEY) {
            LearningJourneyQuestionDiagram(lesson.id, question.text)
        }
        question.choices.chunked(2).forEach { choices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                choices.forEach { option ->
                    val correctSelected = solved && option == question.answer
                    Button(
                        onClick = {
                            if (!solved) {
                                chosen = option
                                if (!correctSelected && option != question.answer) attempts++
                            }
                        },
                        enabled = !solved,
                        modifier = Modifier.weight(1f).heightIn(min = 52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (correctSelected)
                                Color(0xFF187E6B) else Color(0xFF2A6686),
                            disabledContainerColor = if (correctSelected)
                                Color(0xFF187E6B) else Color(0xFF2A6686),
                            disabledContentColor = Color.White
                        )
                    ) {
                        Text(option.toString(), color = Color.White,
                            fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (chosen != null) {
            Text(
                if (solved) "Bilgi sandığını açtın! " + question.explanation
                else if (attempts >= 2) "Çözümü hatırla: " + question.explanation
                else "Henüz değil. Verilen sayıları yeniden inceleyip başka bir cevap dene.",
                color = if (solved) Color(0xFFADFFE0) else Color(0xFFFFE0B1),
                fontSize = 13.sp, lineHeight = 19.sp
            )
        }
        if (solved) {
            OutlinedButton(
                onClick = { round += 1 },
                modifier = Modifier.fillMaxWidth().heightIn(min = 49.dp),
                border = BorderStroke(1.dp, Color(0xFF8FE1C8)),
                shape = RoundedCornerShape(13.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null,
                    tint = Color(0xFF8FE1C8))
                Text(" Yeni Görev", color = Color.White,
                    fontWeight = FontWeight.Bold)
            }
        }
        Text(
            "Bu mini oyun ek puan veya kazanım tamamlama belgesi vermez.",
            color = Color(0xFFBEDBE5), fontSize = 11.sp, lineHeight = 16.sp
        )
    }
}
