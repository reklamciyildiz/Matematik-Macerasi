package com.yigit033.matematikmacarasi.games.basic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.yigit033.matematikmacarasi.models.DifficultyLevel
import com.yigit033.matematikmacarasi.games.basic.AnswerFeedbackState

/**
 * Geliştirilmiş matematik oyun ekranı - 6-14 yaş grubu için özelleştirilmiş daha modern tasarım
 * Bu ekran, standart MathGameScreen'in geliştirilmiş ve daha modern bir versiyonudur.
 */

// Cevap durumunu temsil eden enum sınıfı - yeniden adlandırıldı
enum class EnhancedAnswerState {
    CORRECT, INCORRECT, UNANSWERED
}

// Yaş grubunu temsil eden enum
enum class AgeGroup {
    YOUNG_KIDS, // 5-7 yaş
    KIDS,       // 7-9 yaş
    PRETEENS,   // 9-11 yaş
    TEENS,      // 11-13 yaş
    ADVANCED    // 13+ yaş
}

/**
 * Zorluk seviyesine göre yaş grubunu döndüren yardımcı fonksiyon
 */
@Composable
fun getDifficultyAgeGroup(difficultyLevel: DifficultyLevel): AgeGroup {
    return when (difficultyLevel) {
        DifficultyLevel.EASY -> AgeGroup.YOUNG_KIDS       // 5-7 yaş
        DifficultyLevel.MEDIUM -> AgeGroup.KIDS           // 7-9 yaş
        DifficultyLevel.HARD -> AgeGroup.PRETEENS         // 9-11 yaş
        DifficultyLevel.VERY_HARD -> AgeGroup.TEENS       // 11-13 yaş
        DifficultyLevel.EXPERT -> AgeGroup.ADVANCED       // 13+ yaş
    }
}

/**
 * Tema ve karakter resimlerini yaş grubuna göre sağlayan yardımcı fonksiyon
 */
@Composable
fun getOperationThemeResources(
    operationType: String,
    ageGroup: AgeGroup
): Pair<Brush, Int> {
    val backgroundGradient = when (operationType) {
        "addition" -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF81C784), // Açık yeşil
                    Color(0xFF4CAF50), // Yeşil
                    Color(0xFF388E3C)  // Koyu yeşil
                )
            )
            else -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF2E7D32), // Koyu yeşil
                    Color(0xFF388E3C), // Orta yeşil
                    Color(0xFF43A047)  // Açık yeşil
                )
            )
        }
        "subtraction" -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFEF9A9A), // Açık kırmızı
                    Color(0xFFE57373), // Kırmızı
                    Color(0xFFEF5350)  // Koyu kırmızı
                )
            )
            else -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFD32F2F), // Koyu kırmızı
                    Color(0xFFE53935), // Orta kırmızı
                    Color(0xFFF44336)  // Açık kırmızı
                )
            )
        }
        "multiplication" -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFCE93D8), // Açık mor
                    Color(0xFFBA68C8), // Mor
                    Color(0xFFAB47BC)  // Koyu mor
                )
            )
            else -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF7B1FA2), // Koyu mor
                    Color(0xFF8E24AA), // Orta mor
                    Color(0xFF9C27B0)  // Açık mor
                )
            )
        }
        "division" -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF90CAF9), // Açık mavi
                    Color(0xFF64B5F6), // Mavi
                    Color(0xFF42A5F5)  // Koyu mavi
                )
            )
            else -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1565C0), // Koyu mavi
                    Color(0xFF1976D2), // Orta mavi
                    Color(0xFF1E88E5)  // Açık mavi
                )
            )
        }
        else -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF90A4AE), // Açık gri-mavi
                    Color(0xFF78909C), // Gri-mavi
                    Color(0xFF607D8B)  // Koyu gri-mavi
                )
            )
            else -> Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF455A64), // Koyu gri-mavi
                    Color(0xFF546E7A), // Orta gri-mavi
                    Color(0xFF607D8B)  // Açık gri-mavi
                )
            )
        }
    }
    
    // İşlem türüne ve yaş grubuna göre karakter resmi
    val characterImage = when (operationType) {
        "addition" -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> R.drawable.ic_addition_character
            else -> R.drawable.ic_addition_character_advanced
        }
        "subtraction" -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> R.drawable.ic_subtraction_character
            else -> R.drawable.ic_subtraction_character_advanced
        }
        "multiplication" -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> R.drawable.ic_multiplication_character
            else -> R.drawable.ic_multiplication_character_advanced
        }
        "division" -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> R.drawable.ic_division_character
            else -> R.drawable.ic_division_character_advanced
        }
        else -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> R.drawable.ic_generic_character
            else -> R.drawable.ic_generic_character_advanced
        }
    }
    
    return Pair(backgroundGradient, characterImage)
}

/**
 * İşleme göre tema sağlayan bileşen
 */
@Composable
fun EnhancedOperationTheme(
    operationText: String,
    modifier: Modifier = Modifier,
    ageGroup: AgeGroup = AgeGroup.KIDS
) {
    val operationType = when {
        "+" in operationText -> "addition"
        "-" in operationText -> "subtraction"
        "×" in operationText || "x" in operationText -> "multiplication"
        "÷" in operationText || "/" in operationText -> "division"
        else -> "generic"
    }
    
    val backgroundColor = when (operationType) {
        "addition" -> Color(0xFF4CAF50)       // Yeşil
        "subtraction" -> Color(0xFFF44336)    // Kırmızı
        "multiplication" -> Color(0xFF9C27B0) // Mor
        "division" -> Color(0xFF2196F3)       // Mavi
        else -> Color(0xFF607D8B)             // Gri mavi
    }

    // İşlem karakteri ve animasyonu
    val characterResource = when (operationType) {
        "addition" -> if (ageGroup == AgeGroup.YOUNG_KIDS || ageGroup == AgeGroup.KIDS) 
                        R.drawable.ic_addition_character else R.drawable.ic_addition_character_advanced
        "subtraction" -> if (ageGroup == AgeGroup.YOUNG_KIDS || ageGroup == AgeGroup.KIDS) 
                        R.drawable.ic_subtraction_character else R.drawable.ic_subtraction_character_advanced
        "multiplication" -> if (ageGroup == AgeGroup.YOUNG_KIDS || ageGroup == AgeGroup.KIDS) 
                        R.drawable.ic_multiplication_character else R.drawable.ic_multiplication_character_advanced
        "division" -> if (ageGroup == AgeGroup.YOUNG_KIDS || ageGroup == AgeGroup.KIDS) 
                        R.drawable.ic_division_character else R.drawable.ic_division_character_advanced
        else -> if (ageGroup == AgeGroup.YOUNG_KIDS || ageGroup == AgeGroup.KIDS) 
                        R.drawable.ic_generic_character else R.drawable.ic_generic_character_advanced
    }

    // İşlem karakterinin animasyonu
    val infiniteTransition = rememberInfiniteTransition(label = "characterAnimation")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "scaleAnimation"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(2f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor.copy(alpha = 0.15f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            // İşlem sembolü arka planı
            when (operationType) {
                "addition" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Artı sembolü desenli arka plan
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.TopStart)
                                .alpha(0.2f),
                            tint = backgroundColor
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_add),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.BottomEnd)
                                .alpha(0.2f),
                            tint = backgroundColor
                        )
                        
                        // İşlem karakteri (varsayılan resim kaynağı kullanılıyor)
                        // Not: Gerçek uygulamada bu resimlerin olduğundan emin olun
                        if (characterResource != 0) { // Resource ID 0 değilse göster
                            Image(
                                painter = painterResource(id = characterResource),
                                contentDescription = "Toplama Karakteri",
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.CenterEnd)
                                    .scale(scale)
                            )
                        }
                    }
                }
                "subtraction" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Eksi sembolü desenli arka plan
                        Icon(
                            painter = painterResource(id = R.drawable.ic_subtraction),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.TopEnd)
                                .alpha(0.2f),
                            tint = backgroundColor
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_subtraction),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.BottomStart)
                                .alpha(0.2f),
                            tint = backgroundColor
                        )
                        
                        // İşlem karakteri
                        if (characterResource != 0) {
                            Image(
                                painter = painterResource(id = characterResource),
                                contentDescription = "Çıkarma Karakteri",
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.CenterStart)
                                    .scale(scale)
                            )
                        }
                    }
                }
                "multiplication" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Çarpım işaretleri
                        Icon(
                            painter = painterResource(id = R.drawable.ic_multiplication),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.TopEnd)
                                .alpha(0.2f),
                            tint = backgroundColor
                        )
                        Icon(
                            painter = painterResource(id = R.drawable.ic_multiplication),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.BottomStart)
                                .alpha(0.2f),
                            tint = backgroundColor
                        )
                        
                        // İşlem karakteri
                        if (characterResource != 0) {
                            Image(
                                painter = painterResource(id = characterResource),
                                contentDescription = "Çarpma Karakteri",
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.Center)
                                    .scale(scale)
                            )
                        }
                    }
                }
                "division" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        // Bölme işareti
                        Icon(
                            painter = painterResource(id = R.drawable.ic_division),
                            contentDescription = null,
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.Center)
                                .alpha(0.2f),
                            tint = backgroundColor
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopStart)
                                .clip(CircleShape)
                                .background(backgroundColor.copy(alpha = 0.2f))
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.TopEnd)
                                .clip(CircleShape)
                                .background(backgroundColor.copy(alpha = 0.2f))
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.BottomStart)
                                .clip(CircleShape)
                                .background(backgroundColor.copy(alpha = 0.2f))
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .align(Alignment.BottomEnd)
                                .clip(CircleShape)
                                .background(backgroundColor.copy(alpha = 0.2f))
                        )
                        
                        // İşlem karakteri
                        if (characterResource != 0) {
                            Image(
                                painter = painterResource(id = characterResource),
                                contentDescription = "Bölme Karakteri",
                                modifier = Modifier
                                    .size(80.dp)
                                    .align(Alignment.CenterEnd)
                                    .scale(scale)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Geliştirilmiş matematik oyun ekranı bileşeni
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
 * @param ageGroup Yaş grubu (Opsiyonel)
 */
@Composable
fun EnhancedMathGameScreen(
    questionText:String,
    operationText:String,
    options:List<String>,
    currentQuestionIndex:Int,
    totalQuestions:Int,
    score:Int,
    timeLimit:Int=10,
    onAnswerSelected:(String)->Unit,
    gameTitle:String,
    feedbackState:AnswerFeedbackState?=null,
    correctAnswerText:String?=null,
    visualContent:@Composable ()->Unit={},
    ageGroup:AgeGroup=AgeGroup.KIDS
){
    val operation=when {
        "÷" in operationText->OperationKind.DIVIDE
        "×" in operationText || "x" in operationText->OperationKind.MULTIPLY
        "-" in operationText->OperationKind.SUBTRACT
        else->OperationKind.ADD
    }
    val cover=when(operation){
        OperationKind.ADD->R.drawable.card_addition_premium
        OperationKind.SUBTRACT->R.drawable.card_subtraction_premium
        OperationKind.MULTIPLY->R.drawable.card_multiplication_premium
        OperationKind.DIVIDE->R.drawable.card_division_premium
    }
    val accent=when(operation){
        OperationKind.ADD->Color(0xFF6CE5FF)
        OperationKind.SUBTRACT->Color(0xFFFF94B7)
        OperationKind.MULTIPLY->Color(0xFF74F1BA)
        OperationKind.DIVIDE->Color(0xFFFFD275)
    }
    var remainingTime by remember(currentQuestionIndex) {
        mutableIntStateOf(timeLimit.coerceAtLeast(1))
    }
    var selected by remember(currentQuestionIndex){mutableStateOf<String?>(null)}
    val correct=feedbackState==AnswerFeedbackState.CORRECT
    val wrong=feedbackState==AnswerFeedbackState.INCORRECT ||
        feedbackState==AnswerFeedbackState.TIMEOUT
    val answered=selected!=null || correct || wrong
    val haptic=LocalHapticFeedback.current
    val grade=GradeAwareOperationPolicy.grade(
        com.yigit033.matematikmacarasi.services.PlayerProfileService
            .getInstance(androidx.compose.ui.platform.LocalContext.current).getUserClass())
    LaunchedEffect(currentQuestionIndex,timeLimit,selected){
        while(selected==null && remainingTime>0){
            delay(1000)
            if(selected==null)remainingTime--
        }
        if(remainingTime==0 && selected==null){
            selected=""
            onAnswerSelected("")
        }
    }
    val pulseTransition=rememberInfiniteTransition(label="math_orbit")
    val pulse by pulseTransition.animateFloat(
        initialValue=.78f,targetValue=1.0f,
        animationSpec=infiniteRepeatable(
            animation=tween(1300,easing=LinearEasing),
            repeatMode=RepeatMode.Reverse),
        label="math_pulse"
    )
    fun pick(value:String){
        if(!answered){
            selected=value
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onAnswerSelected(value)
        }
    }
    Box(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(
            Color(0xFF0A5999),Color(0xFF101F55),Color(0xFF070F33)
        )))
    ){
        Image(
            painter=painterResource(cover),
            contentDescription=null,
            contentScale=androidx.compose.ui.layout.ContentScale.Crop,
            modifier=Modifier.fillMaxSize().alpha(.22f)
        )
        Column(
            modifier=Modifier.fillMaxSize()
                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                .padding(horizontal=13.dp,vertical=11.dp),
            verticalArrangement=Arrangement.spacedBy(9.dp)
        ){
            Row(Modifier.fillMaxWidth(),
                verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.SpaceBetween){
                Column(Modifier.weight(1f)){
                    Text("✨ $gameTitle",color=Color(0xFFFFE28B),
                        fontSize=20.sp,fontWeight=FontWeight.Black,maxLines=1)
                    Text("$grade. sınıf · "+operation.title+" macerası",
                        color=Color(0xFFC4EEFF),fontSize=12.sp)
                }
                Surface(shape=RoundedCornerShape(13.dp),
                    color=Color(0xFF142D64),
                    border=androidx.compose.foundation.BorderStroke(1.dp,accent)){
                    Text("★ $score",modifier=Modifier.padding(10.dp),
                        color=Color(0xFFFFE28B),
                        fontWeight=FontWeight.Black)
                }
            }
            Row(Modifier.fillMaxWidth(),
                verticalAlignment=Alignment.CenterVertically,
                horizontalArrangement=Arrangement.spacedBy(8.dp)){
                Text("Soru $currentQuestionIndex/$totalQuestions",
                    fontSize=13.sp,fontWeight=FontWeight.Bold,color=Color.White)
                LinearProgressIndicator(
                    progress={currentQuestionIndex.toFloat()/
                        totalQuestions.coerceAtLeast(1)},
                    modifier=Modifier.weight(1f).height(8.dp),
                    color=accent,trackColor=Color(0xFF28568D))
                Text("⏱ $remainingTime",color=if(remainingTime<=5)
                    Color(0xFFFFCC77) else Color.White,
                    fontSize=13.sp,fontWeight=FontWeight.Bold)
            }
            Surface(shape=RoundedCornerShape(23.dp),
                color=Color(0xFF112956),
                border=androidx.compose.foundation.BorderStroke(2.dp,accent)){
                Column(Modifier.fillMaxWidth().padding(12.dp),
                    horizontalAlignment=Alignment.CenterHorizontally,
                    verticalArrangement=Arrangement.spacedBy(6.dp)){
                    Text(questionText,color=Color(0xFFBBDDF0),
                        fontSize=14.sp,fontWeight=FontWeight.Bold)
                    androidx.compose.foundation.Canvas(
                        Modifier.fillMaxWidth().height(100.dp)){
                        val cw=size.width;val ch=size.height
                        for(index in 0..10){
                            val x=cw*(.06f+index*.088f)
                            val y=ch*(.25f+(index%3)*.25f)
                            drawCircle(accent.copy(alpha=.20f+.45f*pulse),
                                radius=5f+(index%3)*2f,
                                center=androidx.compose.ui.geometry.Offset(x,y))
                        }
                        drawCircle(accent.copy(alpha=.11f*pulse),
                            radius=ch*.5f,center=androidx.compose.ui.geometry.Offset(
                                cw*.5f,ch*.5f))
                    }
                    Text(operationText,
                        color=Color.White,fontSize=36.sp,
                        fontWeight=FontWeight.Black,
                        textAlign=TextAlign.Center)
                    Text("Doğru sonucu seç, yıldızları topla",
                        color=accent,fontSize=12.sp)
                }
            }
            if(correct || wrong){
                Surface(shape=RoundedCornerShape(15.dp),
                    color=if(correct)Color(0xFF176E59) else Color(0xFF893A5A)){
                    Text(if(correct)"✦ Doğru! Devam ediyorsun."
                        else "Bir daha düşün! Doğru cevap: ${correctAnswerText?:""}",
                        color=Color.White,fontWeight=FontWeight.Bold,
                        fontSize=14.sp,
                        modifier=Modifier.fillMaxWidth().padding(11.dp),
                        textAlign=TextAlign.Center)
                }
            }
            options.chunked(2).forEach{pair->
                Row(Modifier.fillMaxWidth(),
                    horizontalArrangement=Arrangement.spacedBy(10.dp)){
                    pair.forEach{value->
                        val isCorrect=correctAnswerText==value && (correct||wrong)
                        val isWrong=selected==value && wrong
                        Surface(
                            modifier=Modifier.weight(1f).heightIn(min=77.dp),
                            onClick={pick(value)},
                            enabled=!answered,
                            shape=RoundedCornerShape(20.dp),
                            border=androidx.compose.foundation.BorderStroke(2.dp,
                                if(isCorrect)Color(0xFF85FFB8)
                                else if(isWrong)Color(0xFFFFA9B0) else accent),
                            color=if(isCorrect)Color(0xFF187C62)
                                else if(isWrong)Color(0xFF9C4364)
                                else Color(0xFF1A3F78)
                        ){
                            Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){
                                Text(value,color=Color.White,
                                    fontSize=27.sp,fontWeight=FontWeight.Black)
                            }
                        }
                    }
                    if(pair.size==1)Spacer(Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(10.dp))
        }
    }
}
@Composable
fun EnhancedAnswerOption(
    text: String,
    isSelected: Boolean,
    isCorrect: Boolean?, // null: normal durum, true: doğru, false: yanlış
    onClick: () -> Unit,
    ageGroup: AgeGroup = AgeGroup.KIDS,
    modifier: Modifier = Modifier
) {
    // Pulsating animation for selected option
    val pulseState = remember { mutableStateOf(1f) }
    val pulseAnimation = animateFloatAsState(
        targetValue = if (isSelected && isCorrect == null) pulseState.value else 1f
    )
    
    // Cevap seçildiğinde pulsasyonu başlat
    LaunchedEffect(isSelected) {
        if (isSelected && isCorrect == null) {
            while (true) {
                pulseState.value = 0.95f
                delay(300)
                pulseState.value = 1.05f
                delay(300)
            }
        }
    }
    
    // Yaş grubuna göre buton yüksekliği
    val buttonHeight = when (ageGroup) {
        AgeGroup.YOUNG_KIDS -> 76.dp // Daha büyük butonlar
        AgeGroup.KIDS -> 72.dp
        else -> 68.dp // Daha kompakt butonlar
    }
    
    // Yaş grubuna göre yazı tipi boyutu
    val baseFontSize = when (ageGroup) {
        AgeGroup.YOUNG_KIDS -> 30.sp
        AgeGroup.KIDS -> 28.sp
        else -> 24.sp
    }
    
    // Yaş grubuna göre köşe yuvarlama derecesi
    val cornerRadius = when (ageGroup) {
        AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> 20.dp
        else -> 16.dp
    }
    
    // Kartın arka plan rengi - Yaş gruplarına göre daha canlı veya sofistike
    val backgroundColor = when {
        isSelected && isCorrect == true -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> Color(0xFF66BB6A) // Canlı yeşil
            else -> Color(0xFF43A047) // Sofistike yeşil
        }
        isSelected && isCorrect == false -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> Color(0xFFEF5350) // Canlı kırmızı
            else -> Color(0xFFE53935) // Sofistike kırmızı
        }
        !isSelected && isCorrect == true -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> Color(0xFFA5D6A7)  // Açık parlak yeşil
            else -> Color(0xFF81C784)  // Sofistike açık yeşil
        }
        isSelected -> when (ageGroup) {
            AgeGroup.YOUNG_KIDS, AgeGroup.KIDS -> Color(0xFF42A5F5) // Canlı mavi
            else -> Color(0xFF1E88E5) // Sofistike mavi
        }
        else -> Color.White // Normal durum - her yaş için aynı
    }
    
    // Metin rengi
    val textColor = when {
        isSelected || (!isSelected && isCorrect == true) -> Color.White
        else -> Color(0xFF333333)
    }
    
    // Parıltı animasyonu - sadece çocuklar için
    val showSparkle = remember { mutableStateOf(false) }
    val infiniteTransition = rememberInfiniteTransition(label = "sparkleAnimation")
    val sparkleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "sparkleAnimationAlpha"
    )
    
    LaunchedEffect(isCorrect) {
        if (isCorrect == true && (ageGroup == AgeGroup.YOUNG_KIDS || ageGroup == AgeGroup.KIDS)) {
            showSparkle.value = true
            delay(3000) // 3 saniye sonra kapat
            showSparkle.value = false
        }
    }
    
    // Gölge elevasyon düzeyi
    val elevationLevel = when {
        isSelected -> 10.dp
        else -> 6.dp
    }
    
    Card(
        modifier = modifier
            .height(buttonHeight)
            .clip(RoundedCornerShape(cornerRadius))
            .clickable(onClick = onClick, enabled = isCorrect == null)
            .then(
                if (isSelected && isCorrect == null) 
                    Modifier.scale(pulseAnimation.value) 
                else 
                    Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = elevationLevel
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            // Metin boyutu dinamik olarak ayarlanıyor
            val fontSize = when {
                text.length > 6 -> 16.sp  // Çok uzun cevaplar
                text.length > 4 -> 20.sp  // Uzun cevaplar
                text.length > 2 -> 24.sp  // Orta uzunlukta
                else -> baseFontSize       // Kısa cevaplar
            }
            
            androidx.compose.material3.Text(
                text = text,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
                color = textColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Parıltı efekti - sadece çocuklar için doğru cevapta
            if (showSparkle.value && isCorrect == true && 
                (ageGroup == AgeGroup.YOUNG_KIDS || ageGroup == AgeGroup.KIDS)) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_sparkle),
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .alpha(sparkleAlpha)
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_sparkle),
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.BottomStart)
                        .alpha(sparkleAlpha)
                )
            }
        }
    }
} 