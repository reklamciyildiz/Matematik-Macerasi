package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.learning.LessonTeachingScript
import com.yigit033.matematikmacarasi.learning.TeachingVisual

private val gold = Color(0xFFFFD886)
private val cyan = Color(0xFF7EDFD9)
private val muted = Color(0xFFBAD7E7)

/**
 * Ö3: visual teaching with native vector illustrations already bundled in the app.
 * Grade 1–2: large illustrated apples; 3–4: groups of tactile counters;
 * grade 5–8: compact abstract groups/number steps. Never a second mini-game.
 */
@Composable
internal fun LessonTeachingIllustration(
    script: LessonTeachingScript,
    grade: Int,
    revealedStep: Int,
    showComplete: Boolean = false,
    onRevealNext: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val early = grade <= 2
    val middle = grade in 3..4
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier.fillMaxWidth()
            .then(
                if (onRevealNext != null) Modifier
                    .semantics { contentDescription = "Resme dokunarak sonraki işlem adımını gör" }
                    .clickable(onClick = onRevealNext, role = Role.Button)
                else Modifier
            )
            .animateContentSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF1B5873), Color(0xFF0C304E))),
                shape
            )
            .border(1.dp, Color(0xFF5D91AB), shape)
            .padding(horizontal = 13.dp, vertical = 17.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(script.headline, fontWeight = FontWeight.ExtraBold,
            fontSize = if (early) 18.sp else 16.sp,
            color = Color.White, textAlign = TextAlign.Center)
        when (script.visual) {
            TeachingVisual.NUMBER_STEPS -> {
                val visible = if (showComplete) script.sequence.size
                    else script.visibleNumbers(revealedStep)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    script.sequence.take(visible).forEachIndexed { index, number ->
                        if (index > 0) {
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null,
                                tint = gold,
                                modifier = Modifier.size(if (early) 20.dp else 15.dp)
                            )
                        }
                        Box(
                            modifier = Modifier.padding(horizontal = 2.dp)
                                .size(if (early) 49.dp else if (middle) 46.dp else 43.dp)
                                .background(
                                    if (index == visible - 1) Color(0xFF267E89)
                                    else Color(0xFF224763), CircleShape
                                )
                                .border(
                                    1.dp,
                                    if (index == visible - 1) gold else Color(0xFF5794A8),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(number.toString(),
                                color = if (index == visible - 1) gold else Color.White,
                                fontSize = if (early) 18.sp else 16.sp,
                                fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
                Text("Her ok aynı büyüklükte bir adımı gösterir.",
                    color = muted, fontSize = 11.sp, textAlign = TextAlign.Center)
            }
            TeachingVisual.GROUPS, TeachingVisual.EQUAL_GROUPS -> {
                val count = if (showComplete) script.groups.size
                    else script.visibleGroups(revealedStep)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    script.groups.take(count).forEachIndexed { index, itemCount ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier.size(32.dp)
                                    .background(Color(0xFF305F78), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (script.visual == TeachingVisual.EQUAL_GROUPS)
                                        (index + 1).toString() else
                                        (if (index == 0) "1" else "+"),
                                    color = gold, fontWeight = FontWeight.Bold
                                )
                            }
                            Column(
                                modifier = Modifier.weight(1f)
                                    .background(Color(0xFF113B57), RoundedCornerShape(13.dp))
                                    .border(1.dp, Color(0xFF426B84),
                                        RoundedCornerShape(13.dp))
                                    .padding(horizontal = 8.dp, vertical = 9.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                (0 until itemCount).chunked(5).forEach { row ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        row.forEach {
                                            if (early) {
                                                // Draw bundled crisp apple VECTOR at native resolution.
                                                Image(
                                                    painter = painterResource(R.drawable.ic_apple),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(29.dp)
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier.size(
                                                        if (middle) 23.dp else 18.dp
                                                    )
                                                        .background(
                                                            if (index == 0) gold else cyan,
                                                            RoundedCornerShape(6.dp)
                                                        )
                                                        .border(1.dp,
                                                            Color(0xFF295F68),
                                                            RoundedCornerShape(6.dp))
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Text(itemCount.toString(), color = Color.White,
                                fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                        }
                    }
                }
            }
            else -> AdvancedLessonIllustration(
                script = script,
                grade = grade,
                step = revealedStep,
                complete = showComplete
            )
        }
        if (onRevealNext != null) {
            Text("Resme dokunarak sonraki adımı göster",
                color = cyan, fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center)
        }
        if (showComplete || revealedStep >= script.steps.lastIndex) {
            Box(
                modifier = Modifier.fillMaxWidth()
                    .background(Color(0xFF265F72), RoundedCornerShape(13.dp))
                    .padding(11.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(script.diagramCaption, color = gold,
                    fontSize = if (early) 17.sp else 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center)
            }
        }
    }
}
