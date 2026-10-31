package com.yigit033.matematikmacarasi.games.basic

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import kotlinx.coroutines.delay

// Cevap durumunu temsil eden sınıf
enum class AnswerState {
    UNANSWERED, // Henüz cevap verilmemiş
    CORRECT,    // Doğru cevap verilmiş
    INCORRECT   // Yanlış cevap verilmiş
}

/**
 * İşlem türü için görsel temalar oluşturan yardımcı fonksiyon.
 * Her işlem türü için uygun tema rengini ve simgeyi belirler.
 */
@Composable
fun DefaultOperationTheme(operationText: String) {
    val operationType = when {
        operationText.contains("+") -> "addition"
        operationText.contains("-") -> "subtraction"
        operationText.contains("×") || operationText.contains("x") || operationText.contains("*") -> "multiplication"
        operationText.contains("÷") || operationText.contains("/") -> "division"
        else -> "generic"
    }
    
    val (iconResId, backgroundColor, animationType) = when (operationType) {
        "addition" -> Triple(R.drawable.ic_addition, Color(0xFF4CAF50), "scale")
        "subtraction" -> Triple(R.drawable.ic_subtraction, Color(0xFFE91E63), "rotate")
        "multiplication" -> Triple(R.drawable.ic_multiplication, Color(0xFF673AB7), "bounce")
        "division" -> Triple(R.drawable.ic_division, Color(0xFF2196F3), "pulse")
        else -> Triple(R.drawable.ic_question, Color(0xFFFFA000), "fade")
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "infinite")
    
    // Animasyon değerlerini hesapla
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (animationType == "scale" || animationType == "bounce") 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animationType == "rotate") 10f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = if (animationType == "fade") 0.6f else 1f,
        targetValue = if (animationType == "fade") 1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    val secondaryScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (animationType == "pulse") 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "secondaryScale"
    )
    
    val animatedModifier = Modifier
        .scale(if (animationType == "bounce" || animationType == "scale") scale else secondaryScale)
        .rotate(rotation)
        .alpha(alpha)
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        contentAlignment = Alignment.Center
    ) {
        // Arka planda işleme göre desenler
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            for (i in 1..3) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = operationType,
                    modifier = Modifier
                        .size(24.dp)
                        .alpha(0.2f),
                    tint = backgroundColor
                )
            }
        }
        
        // İşlem tipi için ana görsel
        Card(
            modifier = animatedModifier
                .size(80.dp)
                .clip(CircleShape),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor.copy(alpha = 0.9f)
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = operationType,
                    modifier = Modifier.size(40.dp),
                    tint = Color.White
                )
            }
        }
        
        // İşleme göre çeşitli küçük dekoratif elemanlar (örneğin yıldızlar, daireler)
        when (operationType) {
            "addition" -> {
                // Köşelerde küçük artı işaretleri
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_addition),
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopStart)
                            .alpha(0.4f),
                        tint = backgroundColor
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_addition),
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomEnd)
                            .alpha(0.4f),
                        tint = backgroundColor
                    )
                }
            }
            "subtraction" -> {
                // Çapraz çizgiler
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_subtraction),
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.TopEnd)
                            .alpha(0.4f)
                            .rotate(45f),
                        tint = backgroundColor
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_subtraction),
                        contentDescription = null,
                        modifier = Modifier
                            .size(18.dp)
                            .align(Alignment.BottomStart)
                            .alpha(0.4f)
                            .rotate(45f),
                        tint = backgroundColor
                    )
                }
            }
            "multiplication" -> {
                // Köşelerde çarpım işaretleri
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_multiplication),
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.TopEnd)
                            .alpha(0.4f),
                        tint = backgroundColor
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_multiplication),
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .align(Alignment.BottomStart)
                            .alpha(0.4f),
                        tint = backgroundColor
                    )
                }
            }
            "division" -> {
                // Yuvarlak noktalar (bölme işaretindeki noktalar gibi)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopStart)
                            .clip(CircleShape)
                            .background(backgroundColor.copy(alpha = 0.4f))
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.TopEnd)
                            .clip(CircleShape)
                            .background(backgroundColor.copy(alpha = 0.4f))
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.BottomStart)
                            .clip(CircleShape)
                            .background(backgroundColor.copy(alpha = 0.4f))
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(backgroundColor.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

/**
 * Matematik oyun ekranı bileşeni
 * 
 * @param questionText Gösterilecek soru metni
 * @param operationText İşlem metni (ör: "35 ÷ 5 = ?")
 * @param options Cevap seçenekleri listesi
 * @param currentQuestionIndex Mevcut soru indeksi (1'den başlar)
 * @param totalQuestions Toplam soru sayısı
 * @param score Mevcut puan
 * @param timeLimit Saniye cinsinden süre limiti
 * @param onAnswerSelected Kullanıcının bir cevap seçtiğinde çağrılacak fonksiyon
 * @param gameTitle Oyun başlığı
 * @param feedbackState Cevabın doğru/yanlış geri bildirim durumu (Opsiyonel)
 * @param correctAnswerText Doğru cevap metni, yanlış cevap verildiğinde gösterilir (Opsiyonel)
 * @param visualContent Görsel içerik için composable fonksiyon (Opsiyonel)
 */
@Composable
fun MathGameScreen(
    questionText: String,
    operationText: String,
    options: List<String>,
    currentQuestionIndex: Int,
    totalQuestions: Int,
    score: Int,
    timeLimit: Int = 10,
    onAnswerSelected: (String) -> Unit,
    gameTitle: String,
    feedbackState: AnswerFeedbackState? = null,
    correctAnswerText: String? = null,
    visualContent: @Composable () -> Unit = {} // Görsel içerik için parametre
) {
    // Oyun ekranı state'i
    var remainingTime by remember { mutableIntStateOf(timeLimit) }
    var isTimerRunning by remember { mutableStateOf(true) }
    var selectedOption by remember { mutableStateOf("") }
    var answerState by remember { mutableStateOf(AnswerState.UNANSWERED) }
    var correctAnswer by remember { mutableStateOf("") }
    
    // Görsel içerik boş mu diye kontrol eden değer
    val isEmptyVisualContent = remember(visualContent) { 
        // '{}' şeklinde verilmiş boş lambda fonksiyonu ise true döner
        visualContent == {} 
    }
    
    // Eğer oyun aktivitesinden geri bildirim durumu gelirse, güncelle
    LaunchedEffect(feedbackState) {
        if (feedbackState != null) {
            when (feedbackState) {
                AnswerFeedbackState.CORRECT -> answerState = AnswerState.CORRECT
                AnswerFeedbackState.INCORRECT, AnswerFeedbackState.TIMEOUT -> answerState = AnswerState.INCORRECT
                AnswerFeedbackState.IDLE -> answerState = AnswerState.UNANSWERED
            }
            
            // Doğru cevabı oyun aktivitesinden gelen bilgiye göre güncelle
            if (correctAnswerText != null) {
                correctAnswer = correctAnswerText
            }
        }
    }
    
    // Cevap kontrol fonksiyonu
    fun checkAnswer(answer: String) {
        if (isTimerRunning) {
            isTimerRunning = false
            selectedOption = answer
            onAnswerSelected(answer)
        }
    }
    
    // Bu effect, yeni bir soru gösterildiğinde süre sayacını ve diğer state'leri sıfırlar
    LaunchedEffect(key1 = currentQuestionIndex) {
        remainingTime = timeLimit
        isTimerRunning = true
        selectedOption = ""
        answerState = AnswerState.UNANSWERED
        correctAnswer = ""
    }
    
    // Bu effect, süre sayacını yönetir
    LaunchedEffect(key1 = currentQuestionIndex, key2 = isTimerRunning) {
        while (isTimerRunning && remainingTime > 0) {
            delay(1000)
            remainingTime--
        }
        
        if (remainingTime <= 0 && isTimerRunning) {
            // Süre dolduğunda otomatik olarak cevabı gönder
            isTimerRunning = false
            onAnswerSelected("")
        }
    }
    
    // Arkaplan gradyanı
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF90CAF9), // Açık mavi üst
            Color(0xFFBBDEFB), // Daha açık mavi orta
            Color(0xFFE3F2FD)  // Çok açık mavi alt
        )
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Üst bilgi çubuğu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Soru sayacı
                Card(
                    modifier = Modifier,
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFBBDEFB).copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = "Soru $currentQuestionIndex/$totalQuestions",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1565C0),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
                
                // Puan göstergesi
                Card(
                    modifier = Modifier,
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFBBDEFB).copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Puan",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$score",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1565C0)
                        )
                    }
                }
                
                // Zamanlayıcı
                Card(
                    modifier = Modifier,
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFBBDEFB).copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_timer),
                            contentDescription = "Zamanlayıcı",
                            tint = if (remainingTime < 3) Color.Red else Color(0xFF1565C0),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$remainingTime s",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (remainingTime < 3) Color.Red else Color(0xFF1565C0)
                        )
                    }
                }
            }
            
            // İlerleme çubuğu
            LinearProgressIndicator(
                progress = { currentQuestionIndex.toFloat() / totalQuestions.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF4CAF50),
                trackColor = Color(0xFFE0E0E0)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Oyun başlığı
            Text(
                text = gameTitle,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1565C0),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Matematik işlemi kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .weight(1f, fill = false), // Kart esnek genişleyebilir, ama sadece gerektiği kadar
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = questionText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color(0xFF424242),
                        textAlign = TextAlign.Center
                    )
                    
                    // Görsel içerik için alan
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .weight(1f, fill = false), // İçerik alanı esnek olsun
                        contentAlignment = Alignment.Center
                    ) {
                        // Eğer custom görsel içerik yoksa, varsayılan görsel içeriği göster
                        if (isEmptyVisualContent) {
                            DefaultOperationTheme(operationText)
                        } else {
                            visualContent()
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Daha büyük ve dikkat çekici işlem gösterimi
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = operationText,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Cevap seçenekleri - Tamamen responsive layout
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                // Her satırda 2 seçenek göster (2x2 grid)
                val rows = options.chunked(2)
                
                rows.forEach { rowOptions ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowOptions.forEach { option ->
                            AnswerOption(
                                text = option,
                                isSelected = selectedOption == option,
                                isCorrect = when {
                                    answerState == AnswerState.UNANSWERED -> null  // Henüz cevap verilmedi
                                    selectedOption == option && answerState == AnswerState.CORRECT -> true  // Seçilen doğru
                                    selectedOption == option && answerState == AnswerState.INCORRECT -> false // Seçilen yanlış
                                    option == correctAnswer && answerState == AnswerState.INCORRECT -> true // Doğru cevap vurgulanıyor
                                    else -> null // Diğer seçenekler normal görünümde
                                },
                                onClick = {
                                    if (answerState == AnswerState.UNANSWERED) {
                                        checkAnswer(option)
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        // Eğer bir satırda tek sayıda seçenek varsa, boş bir alan ekle
                        if (rowOptions.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Alt padding
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
fun AnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean?, // null: normal durum, true: doğru, false: yanlış
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Kartın arka plan rengi
    val backgroundColor = when {
        isSelected && isCorrect == true -> Color(0xFF4CAF50) // Doğru cevap (yeşil)
        isSelected && isCorrect == false -> Color(0xFFF44336) // Yanlış cevap (kırmızı)
        !isSelected && isCorrect == true -> Color(0xFFAEDFB8)  // Vurgulanan doğru cevap (açık yeşil)
        isSelected -> Color(0xFF2196F3) // Sadece seçilmiş, henüz kontrol edilmemiş (mavi)
        else -> Color.White // Normal durum
    }
    
    // Metin rengi
    val textColor = when {
        isSelected || (!isSelected && isCorrect == true) -> Color.White
        else -> Color(0xFF424242)
    }
    
    Card(
        modifier = modifier
            .height(64.dp) // Daha küçük yükseklikte cevap seçenekleri
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick, enabled = isCorrect == null),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 6.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = when {
                    text.length > 6 -> 16.sp  // Çok uzun cevaplar
                    text.length > 4 -> 18.sp  // Uzun cevaplar
                    text.length > 2 -> 22.sp  // Orta uzunlukta
                    else -> 26.sp             // Kısa cevaplar
                },
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
} 