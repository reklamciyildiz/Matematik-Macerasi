package com.yigit033.matematikmacarasi.games.sayiavi2

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.learning.adaptive.AdaptiveProgress
import com.yigit033.matematikmacarasi.learning.adaptive.LearningTier

/**
 * S2.3 Macera Atlası: yalnızca ilk iki bölge oynanabilir.
 * Compose çizimleri görseldir; yıldız, checkpoint ve Premium kuralları ekran tarafından değiştirilmez.
 */
@Composable
fun SayiAviWelcomeScreen(
    isCompletedBefore: Boolean,
    totalCompletions: Int,
    forestCompleted: Boolean,
    starsCount: Int,
    adaptiveProgress: AdaptiveProgress,
    gradeLabel: String,
    onSelectTier: (LearningTier?) -> Unit,
    onStartRegion1: () -> Unit,
    onStartRegion2: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completedRegions = listOf(isCompletedBefore, forestCompleted).count { it }
    Box(modifier = modifier.fillMaxSize().background(AdventureTokens.Night)) {
        Image(
            painter = painterResource(R.drawable.island_explorer_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(
                    AdventureTokens.Night.copy(alpha = .89f),
                    Color(0xFF08404D).copy(alpha = .78f),
                    AdventureTokens.Night.copy(alpha = .96f)
                )
            )
        ))

        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            SayiAviTopBar(
                title = "Sayı Avcıları",
                stageSubtitle = "Macera Atlası",
                currentStageNumber = 0,
                mapPiecesCollected = if (isCompletedBefore) 2 else 0,
                starsCount = starsCount,
                onBackClick = onBackClick
            )

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color(0xFF102D40).copy(alpha = .94f),
                    shape = RoundedCornerShape(22.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp,
                        AdventureTokens.Gold.copy(alpha = .55f))
                ) {
                    Column(modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        Text("BÜYÜK MACERA · DÜNYA 01", fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold, letterSpacing = 1.4.sp,
                            color = AdventureTokens.Gold)
                        Text("Kayıp Haritanın İzinde", fontSize = 24.sp,
                            fontWeight = FontWeight.Black, lineHeight = 28.sp,
                            color = AdventureTokens.Cream)
                        Text(
                            "Kumsal Koyu'nda üçer, Gizemli Orman'da dörder sayarak " +
                                "haritanın sırlarını keşfet. İstediğin açık bölgeden başlayabilirsin.",
                            fontSize = 13.sp, lineHeight = 19.sp,
                            color = Color(0xFFE0ECF0)
                        )
                        AdventureSectionProgress(
                            label = "Tamamlanan bölgeler",
                            completed = completedRegions,
                            total = 2,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("KEŞİF HARİTASI", fontSize = 12.sp, letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.ExtraBold, color = AdventureTokens.Gold)
                    Text("2 BÖLGE AÇIK", fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold, color = Color(0xFFBEE6DA))
                }

                AdventureTierSelector(
                    progress = adaptiveProgress,
                    gradeLabel = gradeLabel,
                    onChange = onSelectTier
                )

                AdventureWorldMap(
                    beachCompleted = isCompletedBefore,
                    forestCompleted = forestCompleted,
                    onBeachClick = onStartRegion1,
                    onForestClick = onStartRegion2
                )

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = AdventureTokens.Night.copy(alpha = .92f),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp,
                        AdventureTokens.Gold.copy(alpha = .35f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.treasure_map_art),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(51.dp).clip(RoundedCornerShape(12.dp))
                        )
                        Column(modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Sıradaki keşfin", fontSize = 12.sp,
                                color = AdventureTokens.Gold, fontWeight = FontWeight.Bold)
                            Text(
                                text = when {
                                    !isCompletedBefore -> "Kumsal Koyu · 5 oyun görevi"
                                    !forestCompleted -> "Gizemli Orman · 4 oyun görevi"
                                    else -> "İki bölge tamamlandı · Tekrar keşfet"
                                },
                                fontSize = 13.sp, color = AdventureTokens.Cream,
                                fontWeight = FontWeight.Medium
                            )
                            if (isCompletedBefore) Text(
                                "Kumsal Koyu toplam bitirme: $totalCompletions",
                                fontSize = 11.sp, color = Color(0xFFB6D3DB)
                            )
                        }
                    }
                }
                Text(
                    "Antik Tapınak geliştirme aşamasında. Kilitli olması bir satın alma şartı değildir.",
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 11.sp, lineHeight = 15.sp,
                    color = Color(0xFFB5C6D0)
                )
            }
        }
    }
}

@Composable
private fun AdventureWorldMap(
    beachCompleted: Boolean,
    forestCompleted: Boolean,
    onBeachClick: () -> Unit,
    onForestClick: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.fillMaxWidth().height(645.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, AdventureTokens.Gold.copy(alpha = .5f),
                RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF174F61), Color(0xFF145B59), Color(0xFF102E42))
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Map land masses, water rings and soft depth cues.
            drawCircle(Color(0xFF46917A).copy(alpha = .42f), radius = w * .40f,
                center = Offset(w * .20f, h * .17f))
            drawCircle(Color(0xFF348F79).copy(alpha = .49f), radius = w * .41f,
                center = Offset(w * .86f, h * .47f))
            drawCircle(Color(0xFF87785C).copy(alpha = .38f), radius = w * .34f,
                center = Offset(w * .18f, h * .80f))
            val route = Path().apply {
                moveTo(w * .31f, h * .12f)
                cubicTo(w * .90f, h * .25f, w * .05f, h * .36f, w * .69f, h * .44f)
                cubicTo(w * .94f, h * .58f, w * .10f, h * .67f, w * .34f, h * .77f)
            }
            drawPath(route, color = Color(0xFF0C3B42).copy(alpha = .85f),
                style = Stroke(width = 11.dp.toPx(), cap = StrokeCap.Round))
            drawPath(route, color = AdventureTokens.Gold.copy(alpha = .88f),
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(10.dp.toPx(), 8.dp.toPx()))))
            for (i in 0..17) {
                val x = (i * 53 % 93 + 4) / 100f * w
                val y = (i * 41 % 91 + 5) / 100f * h
                drawCircle(color = Color.White.copy(alpha = .20f), radius = 2.2.dp.toPx(),
                    center = Offset(x, y))
            }
        }

        val size = (maxWidth - 18.dp).coerceAtMost(180.dp)
        val xBeach = (maxWidth * .31f - size / 2).coerceIn(6.dp, maxWidth - size - 6.dp)
        val xForest = (maxWidth * .69f - size / 2).coerceIn(6.dp, maxWidth - size - 6.dp)
        val xTemple = (maxWidth * .34f - size / 2).coerceIn(6.dp, maxWidth - size - 6.dp)

        AdventureMapNode(
            number = 1, title = "Kumsal Koyu", topic = "Üçer sayma · 5 görev",
            art = R.drawable.island_node, enabled = true,
            finished = beachCompleted, onClick = onBeachClick,
            modifier = Modifier.offset(xBeach, 18.dp).width(size)
        )
        AdventureMapNode(
            number = 2, title = "Gizemli Orman", topic = "Dörder sayma · 4 görev",
            art = R.drawable.forest_node, enabled = true,
            finished = forestCompleted, onClick = onForestClick,
            modifier = Modifier.offset(xForest, 227.dp).width(size)
        )
        AdventureMapNode(
            number = 3, title = "Antik Tapınak", topic = "Geliştirme aşamasında",
            art = R.drawable.temple_node, enabled = false,
            finished = false, onClick = {},
            modifier = Modifier.offset(xTemple, 436.dp).width(size)
        )
    }
}

@Composable
private fun AdventureMapNode(
    number: Int,
    title: String,
    topic: String,
    art: Int,
    enabled: Boolean,
    finished: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pulse by rememberInfiniteTransition(label = "Map island pulse").animateFloat(
        initialValue = .99f, targetValue = 1.04f,
        animationSpec = infiniteRepeatable(tween(1850, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse),
        label = "Map active island"
    )
    Column(
        modifier = modifier
            .then(if (enabled) Modifier
                .clickable(role = Role.Button, onClick = onClick) else Modifier)
            .semantics {
                contentDescription = when {
                    !enabled -> "$title, yakında, henüz oynanamaz"
                    finished -> "$title, tamamlandı, yeniden oyna"
                    else -> "$title, açık, oyna"
                }
            },
        verticalArrangement = Arrangement.spacedBy(5.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(112.dp)) {
            Box(modifier = Modifier.size(108.dp)
                .scale(if (enabled && !finished) pulse else 1f)
                .background(
                    color = if (finished) Color(0xFF4DDE9C).copy(alpha = .3f)
                    else AdventureTokens.Gold.copy(alpha = if (enabled) .27f else .08f),
                    shape = CircleShape
                ))
            Surface(
                modifier = Modifier.size(98.dp).shadow(10.dp, CircleShape),
                shape = CircleShape,
                color = if (enabled) Color(0xFF12443F) else Color(0xFF293D40),
                border = androidx.compose.foundation.BorderStroke(3.dp,
                    if (finished) Color(0xFF75E3A6)
                    else AdventureTokens.Gold.copy(alpha = if (enabled) .95f else .38f))
            ) {
                Image(painter = painterResource(art),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                        .padding(6.dp)
                        .then(if (enabled) Modifier else Modifier.scale(.84f)))
            }
            Surface(
                modifier = Modifier.align(Alignment.BottomEnd).size(30.dp),
                shape = CircleShape,
                color = if (finished) AdventureTokens.Emerald
                    else if (enabled) AdventureTokens.Gold else Color(0xFF45505B),
                border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF102B38))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (finished) Icon(Icons.Default.CheckCircle, null,
                        tint = Color.White, modifier = Modifier.size(18.dp))
                    else if (!enabled) Icon(Icons.Default.Lock, null,
                        tint = Color.White, modifier = Modifier.size(16.dp))
                    else Text("$number", fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold, color = AdventureTokens.Ink)
                }
            }
        }
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0D2735).copy(alpha = if (enabled) .97f else .84f),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp,
                if (enabled) AdventureTokens.Gold.copy(alpha = .7f)
                else Color(0xFF70808B).copy(alpha = .4f))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 9.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(title, color = AdventureTokens.Cream,
                    fontSize = 14.sp, fontWeight = FontWeight.Bold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(topic, color = Color(0xFFB9D7DC),
                    textAlign = TextAlign.Center, fontSize = 10.sp,
                    maxLines = 2, lineHeight = 13.sp)
                Text(
                    text = if (!enabled) "ÇOK YAKINDA"
                        else if (finished) "TEKRAR OYNA ▶" else "KEŞFET ▶",
                    color = if (enabled) AdventureTokens.Gold else Color(0xFFA5B4BC),
                    fontSize = 10.sp, fontWeight = FontWeight.ExtraBold,
                    letterSpacing = .6.sp
                )
            }
        }
    }
}
