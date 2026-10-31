package com.yigit033.matematikmacarasi.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.annotation.DrawableRes
import com.yigit033.matematikmacarasi.DailyTask
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.ui.theme.AnimationDurations
import com.yigit033.matematikmacarasi.ui.theme.BackgroundBlueLight
import com.yigit033.matematikmacarasi.ui.theme.BackgroundBlueMedium
import com.yigit033.matematikmacarasi.ui.theme.PrimaryBlue
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Çocuk dostu, yuvarlak köşeli ve gölgeli buton.
 * 
 * @param text Buton üzerindeki metin
 * @param onClick Tıklama işlevi
 * @param modifier Buton için modifier
 * @param enabled Butonun etkin olup olmadığı
 */
@Composable
fun ChildFriendlyButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(24.dp)
            ),
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Çocuk dostu, yuvarlak köşeli ve gölgeli kart.
 * 
 * @param modifier Kart için modifier
 * @param content Kart içeriği
 */
@Composable
fun ChildFriendlyCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * Animasyonlu arka plan.
 * Turuncu ve mavi tonlarında dinamik gradyan ve hafif dalgalanma efekti içerir.
 * 
 * @param modifier Arka plan için modifier
 * @param content İçerik
 */
@Composable
fun AnimatedBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Sonsuz animasyon geçişi
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    
    // Renk geçişi için animasyon değeri
    val colorTransition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 4000, // 4 saniye süre
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "colorTransition"
    )
    
    // Turuncu ve Mavi renk tanımları (istenilen renkler)
    val orangeColor = Color(0xFFFF9800) // Turuncu başlangıç rengi
    val blueColor = Color(0xFF42A5F5)   // Mavi bitiş rengi
    
    // Ara renkleri içeren gradyan listesi
    val gradientColors = listOf(
        // ColorTransition 0f'den 1f'e giderken renkler değişecek
        // Lerp fonksiyonu ile iki renk arasında geçiş yapılıyor
        lerp(orangeColor, blueColor, colorTransition),
        lerp(blueColor, orangeColor, 1f - colorTransition)
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * Sabit arka plan.
 * Mavi tonlarında gradyan içerir, animasyon içermez.
 * 
 * @param modifier Arka plan için modifier
 * @param content İçerik
 */
@Composable
fun StaticBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Gradyan renkleri
    val gradientColors = listOf(
        BackgroundBlueLight,
        BackgroundBlueMedium,
        BackgroundBlueLight
    )
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(1.dp)
        ) {
            content()
        }
    }
}

/**
 * Animasyonlu görünürlük geçişi ile içerik gösterme.
 * 
 * @param visible İçeriğin görünür olup olmadığı
 * @param content Gösterilecek içerik
 */
@Composable
fun FadeInContent(
    visible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.MEDIUM,
                easing = FastOutSlowInEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = AnimationDurations.SHORT,
                easing = LinearEasing
            )
        )
    ) {
        content()
    }
}

/**
 * Tıklanabilir kart bileşeni.
 * 
 * @param onClick Tıklama işlevi
 * @param modifier Kart için modifier
 * @param content Kart içeriği
 */
@Composable
fun ClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

/**
 * Başlık metni bileşeni.
 * 
 * @param text Başlık metni
 * @param modifier Metin için modifier
 */
@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.displayMedium,
        color = MaterialTheme.colorScheme.primary,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

/**
 * Alt başlık metni bileşeni.
 * 
 * @param text Alt başlık metni
 * @param modifier Metin için modifier
 */
@Composable
fun SubtitleText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

/**
 * Temel İşlemler kartı
 * 
 * Bu bileşen, ana ekrandaki temel matematik işlemlerini (toplama, çıkarma, çarpma, bölme)
 * gösteren kartları oluşturur.
 * 
 * @param icon Kartın üst kısmında gösterilecek ikon
 * @param title Kartın alt kısmında gösterilecek başlık
 * @param color Kartın tema rengi (ikon rengi ve arka plan tonları için kullanılır)
 * @param onClick Karta tıklandığında çalıştırılacak işlev
 */
@Composable
fun BasicOperationCard(
    @DrawableRes icon: Int,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    // Ekran genişliğini al ve dinamik boyut hesapla
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    // Dinamik boyutlar (küçük ekranlar için daha küçük değerler)
    val cardWidth = (screenWidth * 0.35f).coerceIn(130f, 170f).dp
    val cardHeight = (cardWidth.value * 0.95f).dp
    val iconSize = (cardWidth.value * 0.5f).dp
    val iconInnerSize = (iconSize.value * 0.56f).dp
    val cornerRadius = (cardWidth.value * 0.1f).dp
    val titleSize = (screenWidth * 0.028f).coerceIn(14f, 16f).sp
    
    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(cardHeight)
            .padding(vertical = 4.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(width = 2.dp, color = Color(0xFFFFD700), shape = RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Dekoratif arkaplan dairesi - boyutları ekran boyutuna göre ayarlandı
            Box(
                modifier = Modifier
                    .size((cardWidth.value * 0.5f).dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (cardWidth.value * 0.15f).dp, y = (-cardWidth.value * 0.15f).dp)
                    .background(
                        color = color.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
            
            // Ana içerik düzeni
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // İkon kutusu
                Box(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxWidth()
                        .padding(top = (cardWidth.value * 0.05f).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(iconSize)
                            .clip(RoundedCornerShape((cornerRadius.value * 0.7f).dp))
                            .background(color.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = icon),
                            contentDescription = title,
                            tint = color,
                            modifier = Modifier.size(iconInnerSize)
                        )
                    }
                }
                
                // Alt bölüm - Başlık alanı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.35f)
                        .background(
                            color = color.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(
                                topStart = (cornerRadius.value * 0.7f).dp, 
                                topEnd = (cornerRadius.value * 0.7f).dp, 
                                bottomStart = 0.dp, 
                                bottomEnd = 0.dp
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1565C0),
                        fontSize = titleSize,
                        modifier = Modifier.padding(horizontal = (cardWidth.value * 0.05f).dp, vertical = (cardWidth.value * 0.03f).dp)
                    )
                }
            }
        }
    }
}

/**
 * Diğer Konular kartı
 * 
 * Bu bileşen, ana ekrandaki "Diğer Konular" bölümündeki kartları oluşturur.
 * İki bölümlü (üstte ikon, altta başlık) dikdörtgen kartlar şeklindedir.
 * 
 * @param icon Kartın üst kısmında gösterilecek ikon
 * @param title Kartın alt kısmında gösterilecek başlık
 * @param color Kartın tema rengi (ikon rengi ve arka plan tonları için kullanılır)
 * @param badgeCount Sağ üst köşede gösterilecek bildirim sayısı (0 ise gösterilmez)
 * @param customIcon Özel ikon bileşeni (null ise standart ikon kullanılır)
 * @param onClick Karta tıklandığında çalıştırılacak işlev
 */
@Composable
fun GameCard(
    @DrawableRes icon: Int,
    title: String,
    color: Color,
    badgeCount: Int = 0,
    customIcon: (@Composable () -> Unit)? = null,
    onClick: () -> Unit
) {
    // Ekran genişliğini al ve dinamik boyut hesapla
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    // Dinamik boyutlar (küçük ekranlar için daha küçük değerler)
    val cardHeight = (screenWidth * 0.25f).coerceIn(140f, 180f).dp
    val iconSize = (screenWidth * 0.13f).coerceIn(65f, 85f).dp
    val iconInnerSize = (screenWidth * 0.085f).coerceIn(42f, 55f).dp
    val cornerRadius = (screenWidth * 0.037f).coerceIn(16f, 24f).dp
    val titleSize = (screenWidth * 0.03f).coerceIn(14f, 18f).sp
    
    Card(
        modifier = Modifier
            .fillMaxWidth(0.495f)
            .height(cardHeight)
            .padding(horizontal = 0.dp, vertical = 4.dp)
            .shadow(
                elevation = 5.dp,
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(width = 2.dp, color = Color(0xFFFFD700), shape = RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Dekoratif arkaplan dairesi - boyutları ve ofset ekran boyutuna göre ayarlandı
            Box(
                modifier = Modifier
                    .size((cardHeight.value * 0.8f).dp)
                    .align(Alignment.TopEnd)
                    .offset(x = (cardHeight.value * 0.4f).dp, y = (-cardHeight.value * 0.4f).dp)
                    .background(
                        color = color.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
            
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Üst bölüm - İkon alanı (60%)
                Box(
                    modifier = Modifier
                        .weight(0.6f)
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (badgeCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(
                                        text = badgeCount.toString(),
                                        fontSize = (titleSize.value * 0.6f).sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(iconSize)
                                    .clip(RoundedCornerShape((cornerRadius.value * 0.8f).dp))
                                    .background(color.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (customIcon != null) {
                                    customIcon()
                                } else {
                                    Icon(
                                        painter = painterResource(id = icon),
                                        contentDescription = title,
                                        tint = color,
                                        modifier = Modifier.size(iconInnerSize)
                                    )
                                }
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .size(iconSize)
                                .clip(RoundedCornerShape((cornerRadius.value * 0.8f).dp))
                                .background(color.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (customIcon != null) {
                                customIcon()
                            } else {
                                Icon(
                                    painter = painterResource(id = icon),
                                    contentDescription = title,
                                    tint = color,
                                    modifier = Modifier.size(iconInnerSize)
                                )
                            }
                        }
                    }
                }
                
                // Alt bölüm - Başlık alanı (40%)
                Box(
                    modifier = Modifier
                        .weight(0.4f)
                        .fillMaxWidth()
                        .background(
                            color = color.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(
                                topStart = (cornerRadius.value * 0.7f).dp, 
                                topEnd = (cornerRadius.value * 0.7f).dp,
                                bottomStart = 0.dp, 
                                bottomEnd = 0.dp
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1565C0),
                        fontSize = titleSize,
                        lineHeight = (titleSize.value * 1.2f).sp,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun DailyTaskItem(
    task: DailyTask,
    onTaskClick: () -> Unit
) {
    val progress = task.currentCount.toFloat() / task.targetCount.toFloat()
    val isCompleted = task.isCompleted
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onTaskClick() }
            .background(Color(0xFFF5F5F5))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val (iconTint, iconResId) = when (task.gameType) {
            GameType.DIVISION_GAME -> Pair(Color.Blue, com.yigit033.matematikmacarasi.R.drawable.ic_division_task)
            GameType.ADDITION_GAME -> Pair(Color(0xFF4CAF50), com.yigit033.matematikmacarasi.R.drawable.ic_addition_task)
            GameType.SUBTRACTION_GAME -> Pair(Color(0xFFE91E63), com.yigit033.matematikmacarasi.R.drawable.ic_subtraction_task)
            GameType.MULTIPLICATION_GAME -> Pair(Color(0xFF673AB7), com.yigit033.matematikmacarasi.R.drawable.ic_multiplication_task)
            GameType.NUMBER_HUNT_GAME, GameType.SHAPE_RECOGNITION -> 
                Pair(Color(0xFFFF9800), com.yigit033.matematikmacarasi.R.drawable.ic_matching_task)
            GameType.MATH_MAZE_GAME -> Pair(Color(0xFF009688), com.yigit033.matematikmacarasi.R.drawable.ic_comparison_task)
            GameType.BALLOON_POP_GAME -> Pair(Color(0xFFE91E63), com.yigit033.matematikmacarasi.R.drawable.ic_balloon_task)
            GameType.SPACE_MATH_ADVENTURE -> Pair(Color(0xFF3F51B5), com.yigit033.matematikmacarasi.R.drawable.ic_spaceship)
            GameType.MARKET_SIMULATOR -> Pair(Color(0xFF8D6E63), com.yigit033.matematikmacarasi.R.drawable.ic_shopping_cart)
        }
        
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.wrapContentWidth(Alignment.Start)
        ) {
            Text(
                text = task.description,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp),
                color = iconTint,
                trackColor = Color(0xFFE0E0E0)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(
                    if (isCompleted) Color(0xFF4CAF50) else Color(0xFF2196F3)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    text = "${task.currentCount}/${task.targetCount}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun DailyTasksDialog(
    tasks: List<DailyTask>,
    onDismiss: () -> Unit,
    onTaskCompleted: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Günlük Görevler",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            LazyColumn {
                items(tasks) { task ->
                    DailyTaskItem(
                        task = task,
                        onTaskClick = { if (!task.isCompleted) onTaskCompleted(task.id) }
                    )
                    Divider(
                        color = Color.LightGray,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Tamam")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}

/**
 * Sabit gradyan arka plan.
 * Belirtilen renklerle yukarıdan aşağıya doğru statik gradient oluşturur.
 * 
 * @param startColor Gradient başlangıç rengi
 * @param endColor Gradient bitiş rengi
 * @param modifier Arka plan için modifier
 * @param content İçerik
 */
@Composable
fun StaticGradientBackground(
    startColor: Color = Color(0xFFFF9800), // Varsayılan turuncu
    endColor: Color = Color(0xFF42A5F5),   // Varsayılan açık mavi
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Gradyan renkleri
    val gradientColors = listOf(startColor, endColor)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors
                )
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            content()
        }
    }
} 