package com.yigit033.matematikmacarasi.games.interactive

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.activities.BaseGameActivity
import com.yigit033.matematikmacarasi.models.DifficultyLevel
import com.yigit033.matematikmacarasi.models.QuestionModel
import com.yigit033.matematikmacarasi.models.QuestionType
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import com.yigit033.matematikmacarasi.services.GameProgressService
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.services.EventBus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.roundToInt
import kotlin.random.Random
import androidx.compose.ui.text.style.TextOverflow

/**
 * BalloonPopGameActivity, balon patlatma oyunu için aktivite.
 * Ekranda yükselen balonlarda matematik işlemleri ve sonuçları gösterilir.
 * Kullanıcı doğru sonucu içeren balonu patlatmaya çalışır.
 */
class BalloonPopGameActivity : BaseGameActivity() {
    
    // BaseGameActivity'den gelen abstract property'yi override et
    override val gameType: GameType = GameType.BALLOON_POP_GAME
    
    // Log için TAG sabiti
    private val TAG = "BalloonPopGameActivity"
    
    // Oyun sabitleri
    private val GAME_BALLOON_POP = "balloon_pop" // Oyun ID'si
    
    // Oyun durumu
    private var gameTimeRemaining by mutableStateOf(60) // Varsayılan 60 saniye, seviyeye göre değişecek
    private var currentQuestion by mutableStateOf<QuestionModel?>(null)
    private var balloons = mutableStateListOf<Balloon>()
    private var isGameOver by mutableStateOf(false)
    private var gameStarted by mutableStateOf(false)
    private var correctAnswersCount by mutableStateOf(0)
    private var wrongAnswersCount by mutableStateOf(0)
    private var successRate by mutableStateOf(0f) // Başarı oranı için yeni state ekledim
    private var gameStartTime = 0L // Oyun başlangıç zamanını tutacak değişken
    private var showGameOverScreen by mutableStateOf(false) // Oyun sonu ekranını göstermek için
    private var isGeneratingBalloons by mutableStateOf(false) // Balon oluşturma işlemini kontrol etmek için
    
    // Soru kontrolü için eklenen yeni değişkenler
    private var currentQuestionId by mutableStateOf(0L)
    private var canGenerateNewQuestion by mutableStateOf(true)
    private var previousQuestionLost by mutableStateOf(false)
    private var questionStartTime by mutableStateOf(0L)
    
    // Zorluk seviyesi seçim dialogu için state
    private var showLevelSelectDialog by mutableStateOf(true)
    private var selectedLevelIndex by mutableStateOf(0)
    private var selectedOperationType by mutableStateOf(0) // 0=Toplama, 1=Çıkarma, 2=Çarpma, 3=Bölme, 4=Karışık, 5=İleri seviye
    
    // Renk seçenekleri
    private val balloonColors = listOf(
        Color.Red, Color.Blue, Color.Green, Color(0xFF673AB7), Color.Magenta,
        Color.Cyan, Color(0xFFFFA500), Color(0xFF800080), Color(0xFF00FFFF)
    )
    
    // Balon patlatma efekti için state'ler
    private val poppingBalloons = mutableStateMapOf<Long, Boolean>()
    private var lastPoppedBalloonId by mutableStateOf(0L)
    
    private lateinit var gameStatisticsHelper: com.yigit033.matematikmacarasi.services.GameStatisticsHelper
    
    // Günlük görev ID'si (görev ekranından gelince kullanılacak)
    private var dailyTaskId: Int = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // DailyTaskManager zaten BaseGameActivity'de initialize ediliyor
        // dailyTaskManager = DailyTaskManager.getInstance(this)
        
        // Ses servisini başlat - BaseGameActivity'de zaten initialize ediliyor
        // audioService = com.yigit033.matematikmacarasi.services.AudioService.getInstance(this)
        
        // Oyun başlangıç zamanını hemen kaydet - ÖNEMLİ: İlk kez burada ata
        gameStartTime = System.currentTimeMillis()
        Log.d(TAG, "onCreate: gameStartTime ilk kez atandı: $gameStartTime")
        
        gameStatisticsHelper = com.yigit033.matematikmacarasi.services.GameStatisticsHelper(this)
    }
    
    @Composable
    override fun GameContent() {
        if (showLevelSelectDialog) {
            LevelSelectDialog(
                onLevelSelected = { levelIndex ->
                    selectedLevelIndex = levelIndex
                    selectedOperationType = levelIndex
                    showLevelSelectDialog = false
                    gameStarted = true
                    
                    // Seviyeye göre oyun süresini ayarla
                    gameTimeRemaining = getLevelTime()
                    
                    startGame()
                },
                onCancel = { finish() }
            )
        } else if (showGameOverScreen) {
            GameOverScreen()
        } else if (!gameStarted) {
            StartScreen()
        } else {
            GamePlayScreen()
        }
    }
    
    @Composable
    fun LevelSelectDialog(
        onLevelSelected: (Int) -> Unit,
        onCancel: () -> Unit
    ) {
        Dialog(onDismissRequest = { onCancel() }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 600.dp)
            ) {
                // Statik arka plan - daha kullanıcı dostu
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1A237E), // Koyu mavi
                                    Color(0xFF303F9F), // Orta mavi
                                    Color(0xFF3F51B5)  // Açık mavi
                                )
                            )
                        )
                ) {
                    // Balon desenleri (iyileştirilmiş ve daha tematik)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val screenWidth = size.width
                        val screenHeight = size.height
                        
                        // Daha fazla balon (15 adet) ve daha canlı renklerle
                        repeat(15) { index ->
                            // Rastgele pozisyonlar
                            val x = screenWidth * Random.nextFloat() * 0.8f + screenWidth * 0.1f
                            val y = screenHeight * Random.nextFloat() * 0.8f + screenHeight * 0.1f
                            val size = 15f + (index % 5) * 8f // Daha çeşitli boyutlar
                            
                            // Canlı balon renkleri
                            val color = when (index % 6) {
                                0 -> Color(0xFFFF5252) // Kırmızı
                                1 -> Color(0xFF448AFF) // Mavi
                                2 -> Color(0xFF4CAF50) // Yeşil
                                3 -> Color(0xFFFF9800) // Turuncu
                                4 -> Color(0xFFE040FB) // Mor
                                else -> Color(0xFF673AB7) // Mor (Sarı yerine)
                            }
                            
                            // Balon gövdesi
                            drawCircle(
                                color = color.copy(alpha = 0.4f),
                                radius = size,
                                center = Offset(x, y)
                            )
                            
                            // Balon ipleri - daha belirgin
                            drawLine(
                                color = Color.White.copy(alpha = 0.5f),
                                start = Offset(x, y + size),
                                end = Offset(x + Random.nextFloat() * 10f - 5f, y + size + 30f + Random.nextFloat() * 10f),
                                strokeWidth = 2f
                            )
                        }
                        
                        // Arka planda dekoratif geometrik şekiller
                        repeat(5) {
                            val x = screenWidth * Random.nextFloat()
                            val y = screenHeight * Random.nextFloat()
                            val size = 40f + Random.nextFloat() * 30f
                            
                            // Transparan yıldızlar veya geometrik şekiller
                            val startX = x
                            val startY = y
                            drawCircle(
                                color = Color.White.copy(alpha = 0.1f),
                                radius = size,
                                center = Offset(startX, startY)
                            )
                        }
                    }
                }
                
                // Ana içerik (scroll özelliği ile)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 600.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Başlık
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1A237E).copy(alpha = 0.85f)
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 8.dp
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Başlık ve Logo
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                // Balon ikonu
                                Canvas(modifier = Modifier.size(32.dp)) {
                                    drawCircle(
                                        color = Color(0xFFFF5252),
                                        radius = size.width / 2.2f
                                    )
                                    // Balon ipi
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(size.width / 2, size.height - 5),
                                        end = Offset(size.width / 2, size.height),
                                        strokeWidth = 3f
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = "Balon Patlatma",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Alt başlık
                            Text(
                                text = "Matematik İşlemleri Macerası",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    // Seviye kartları (scroll edilebilir)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Seviye 1
                        LevelCard(
                            level = 1,
                            title = "Toplama İşlemleri",
                            description = "Sayıları toplama ve doğru sonucu bulma",
                            details = listOf(
                                "• Zorluk: Kolay",
                                "• Süre: 75 saniye",
                                "• İşlem: 1-10 arası basit toplama",
                                "• Hedef: En az 10 doğru cevap"
                            ),
                            backgroundColor = Color(0xFF4CAF50),
                            onClick = { onLevelSelected(0) }
                        )
                        
                        // Seviye 2
                        LevelCard(
                            level = 2,
                            title = "Çıkarma İşlemleri",
                            description = "Sayıları çıkarma ve doğru sonucu bulma",
                            details = listOf(
                                "• Zorluk: Orta",
                                "• Süre: 70 saniye",
                                "• İşlem: 1-20 arası çıkarma",
                                "• Hedef: En az 15 doğru cevap"
                            ),
                            backgroundColor = Color(0xFFFFA000),
                            onClick = { onLevelSelected(1) }
                        )
                        
                        // Seviye 3
                        LevelCard(
                            level = 3,
                            title = "Çarpma İşlemleri",
                            description = "Sayıları çarpma ve doğru sonucu bulma",
                            details = listOf(
                                "• Zorluk: Zor",
                                "• Süre: 65 saniye",
                                "• İşlem: 1-10 arası çarpma",
                                "• Hedef: En az 15 doğru cevap"
                            ),
                            backgroundColor = Color(0xFFFF5722),
                            onClick = { onLevelSelected(2) }
                        )
                        
                        // Seviye 4 (Bölme İşlemleri - Yeni Eklendi)
                        LevelCard(
                            level = 4,
                            title = "Bölme İşlemleri",
                            description = "Bölme işlemleri ve doğru sonucu bulma",
                            details = listOf(
                                "• Zorluk: Zor",
                                "• Süre: 65 saniye",
                                "• İşlem: Basit bölme işlemleri",
                                "• Hedef: En az 15 doğru cevap"
                            ),
                            backgroundColor = Color(0xFFFFA000), // Turuncu
                            onClick = { onLevelSelected(3) }
                        )
                        
                        // Seviye 5 (Eski Seviye 4, şimdi 5)
                        LevelCard(
                            level = 5,
                            title = "Karışık İşlemler",
                            description = "Toplama, çıkarma, çarpma ve bölme işlemleri",
                            details = listOf(
                                "• Zorluk: Çok Zor",
                                "• Süre: 60 saniye",
                                "• İşlem: 1-50 arası karışık işlemler",
                                "• Hedef: En az 18 doğru cevap"
                            ),
                            backgroundColor = Color(0xFF9C27B0),
                            onClick = { onLevelSelected(4) }
                        )
                        
                        // Seviye 6 (Eski Seviye 5, şimdi 6)
                        LevelCard(
                            level = 6,
                            title = "İleri Seviye İşlemler",
                            description = "Zorlu karışık matematik işlemleri",
                            details = listOf(
                                "• Zorluk: Uzman",
                                "• Süre: 60 saniye",
                                "• İşlem: 1-100 arası karışık işlemler",
                                "• Hedef: En az 20 doğru cevap"
                            ),
                            backgroundColor = Color(0xFF3F51B5),
                            onClick = { onLevelSelected(5) }
                        )
                    }
                    
                    // Geri butonu
                    Button(
                        onClick = { onCancel() },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 12.dp, end = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Geri",
                                tint = Color.White
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = "Geri Dön",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    private fun LevelCard(
        level: Int,
        title: String,
        description: String,
        details: List<String>,
        backgroundColor: Color,
        onClick: () -> Unit
    ) {
        var isExpanded by remember { mutableStateOf(false) }
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { 
                    isExpanded = !isExpanded
                    if (!isExpanded) onClick()
                }
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = backgroundColor
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 4.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Seviye sayısı (daire içinde)
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = level.toString(),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Başlık ve açıklama
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        
                        Text(
                            text = description,
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                    
                    // Sağ tarafta balon ikonu
                    Canvas(modifier = Modifier.size(24.dp)) {
                        // Balon gövdesi
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = size.width / 2.5f
                        )
                        
                        // Balon ipi
                        drawLine(
                            color = Color.White.copy(alpha = 0.8f),
                            start = Offset(size.width / 2, size.height - 5),
                            end = Offset(size.width / 2, size.height),
                            strokeWidth = 2f
                        )
                    }
                }
                
                // Seviye detaylarını göster/gizle
                if (isExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(12.dp)
                    ) {
                        details.forEach { detail ->
                            Text(
                                text = detail,
                                fontSize = 14.sp,
                                color = Color.White,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                isExpanded = false
                                onClick()
                            },
                            modifier = Modifier.align(Alignment.End),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White
                            )
                        ) {
                            Text(
                                text = "Seviyeyi Başlat",
                                color = backgroundColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    private fun StartScreen() {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Balon Patlatma",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Doğru cevabı olan balonu patlat!",
                fontSize = 18.sp,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    gameStarted = true
                    startGame()
                }
            ) {
                Text(text = "Oyunu Başlat")
            }
        }
    }
    
    @Composable
    private fun GamePlayScreen() {
        var currentTime by remember { mutableStateOf(gameTimeRemaining) }
        
        // Oyun sayacı
        LaunchedEffect(key1 = gameStarted) {
            while (currentTime > 0 && gameStarted && !isGameOver) {
                delay(1000) // 1 saniye bekle
                currentTime--
                gameTimeRemaining = currentTime
                
                if (currentTime <= 0) {
                    showGameOverScreen = true
                    finishGame()
                }
            }
        }
        
        // Balon oluşturma ve yükseltme
        LaunchedEffect(key1 = gameStarted) {
            while (gameStarted && !isGameOver) {
                if (balloons.size < 5 && canGenerateNewQuestion) { // Maksimum 5 balon ve yeni soru oluşturabiliyorsa
                    // Yeni soru ID'si ve zamanı ata
                    currentQuestionId = System.currentTimeMillis()
                    questionStartTime = System.currentTimeMillis()
                    canGenerateNewQuestion = false
                    
                    // Yeni soru oluştur
                    generateQuestion()
                    
                    // Seviyeye göre balon sayısını belirle
                    val balloonCount = getBalloonCountForLevel()
                    
                    // Doğru cevap ve yanlış cevapları belirle
                    val answers = mutableListOf<Int>()
                    currentQuestion?.let { question ->
                        answers.add(question.correctAnswer)
                        
                        // Yanlış cevaplar ekle
                        val range = (question.correctAnswer - 10)..(question.correctAnswer + 10)
                        while (answers.size < balloonCount) {
                            val wrongAnswer = range.random()
                            if (wrongAnswer != question.correctAnswer && wrongAnswer > 0 && wrongAnswer !in answers) {
                                answers.add(wrongAnswer)
                            }
                        }
                        
                        // Cevapları karıştır
                        answers.shuffle()
                        
                        // Her cevap için bir balon oluştur
                        answers.forEachIndexed { index, answer ->
                            val isCorrect = answer == question.correctAnswer
                            
                            // Ekranı horizontal olarak eşit bölgelere ayır
                            val horizontalSector = 0.7f / answers.size
                            
                            // Balonun yatay pozisyonunu belirle - her balon kendi sektöründe
                            val baseX = 0.15f + (index * horizontalSector)
                            // Küçük rastgele varyasyon ekle (kendi sektörü içinde)
                            val randomOffsetX = Random.nextFloat() * (horizontalSector * 0.5f)
                            val finalX = baseX + randomOffsetX
                            
                            // Dikey pozisyonu belirle - birbirinden uzak başlasınlar
                            val startY = 1.1f + (index * 0.15f) // Her biri farklı yükseklikte başlar, ama çok fazla uzakta olmasınlar
                            
                            // Balon boyutunu belirle - boyutlar arası fark az olsun
                            val balloonSize = Random.nextInt(45, 50).toFloat()
                            
                            // Seviyeye göre hız faktörünü belirle
                            val speedFactor = getBalloonSpeed(isCorrect)
                            
                            val balloon = Balloon(
                                id = System.currentTimeMillis() + index,
                                questionId = currentQuestionId,
                                x = finalX,
                                y = startY,
                                radius = balloonSize,
                                color = balloonColors.random(),
                                value = answer,
                                isCorrect = isCorrect,
                                speedFactor = speedFactor
                            )
                            balloons.add(balloon)
                        }
                    }
                }
                
                delay(2500) // 2.5 saniye bekle (biraz daha hızlı soru ritmi)
            }
        }
        
        // Balonları hareket ettir
        LaunchedEffect(key1 = gameStarted) {
            while (gameStarted && !isGameOver) {
                // Her balon için yeni y pozisyonu hesapla
                val balloonsToRemove = mutableListOf<Balloon>()
                var correctBalloonMissed = false
                var correctBalloonQuestionId = 0L
                
                balloons.forEachIndexed { index, balloon ->
                    val newY = balloon.y - 0.005f * balloon.speedFactor
                    
                    if (newY < -0.1f) { // Ekranın üstüne çıktıysa
                        balloonsToRemove.add(balloon)
                        
                        // Eğer bu doğru cevap içeren bir balonsa ve çıktıysa, kullanıcı soruyu kaçırmış demektir
                        if (balloon.isCorrect && balloon.questionId == currentQuestionId) {
                            correctBalloonMissed = true
                            correctBalloonQuestionId = balloon.questionId
                        }
                    } else {
                        balloons[index] = balloon.copy(y = newY)
                    }
                }
                
                // Ekrandan çıkan balonları kaldır
                balloons.removeAll(balloonsToRemove)
                
                // Eğer doğru balon ekrandan çıktıysa ve bu hala mevcut soru ise
                if (correctBalloonMissed && correctBalloonQuestionId == currentQuestionId) {
                    // Kullanıcı doğru balonu kaçırdı, yanlış olarak say
                    wrongAnswersCount++
                    audioService.playWrongSound()
                    previousQuestionLost = true
                    
                    // Şimdi yeni bir soru için hazır
                    canGenerateNewQuestion = true
                    
                    // Bu soruya ait tüm balonları kaldır
                    balloons.removeAll { it.questionId == correctBalloonQuestionId }
                }
                
                // Eğer hiç balon kalmadıysa ve sorunun üzerinden belirli bir süre geçtiyse yeni soru oluşturmaya izin ver
                if (balloons.none { it.questionId == currentQuestionId } && System.currentTimeMillis() - questionStartTime > 1000) {
                    canGenerateNewQuestion = true
                }
                
                delay(16) // ~60 FPS
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF90CAF9), // Üst - açık mavi
                            Color(0xFFE3F2FD)  // Alt - çok açık mavi
                        )
                    )
                )
        ) {
            // Üst bilgi çubuğu
            GameTopBar(
                gameName = "Balon Patlatma",
                timeRemaining = currentTime,
                score = score,
                correctAnswers = correctAnswersCount,
                wrongAnswers = wrongAnswersCount,
                level = getSeviyeAdi(),
                onBackClick = { finish() }
            )
            
            // Balonları çiz
            balloons.forEach { balloon ->
                BalloonItem(balloon) { clickedBalloon ->
                    handleBalloonClick(clickedBalloon)
                }
            }
            
            // Mevcut soru bilgisi
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 120.dp) // Üst bilgi çubuğunun altına
                    .align(Alignment.TopCenter)
            ) {
                currentQuestion?.let { question ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .align(Alignment.Center),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White.copy(alpha = 0.85f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = question.getQuestionText(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF1976D2)
                            )
                            
                            // Doğru/Yanlış sayıları
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Text(
                                    text = "Doğru: $correctAnswersCount",
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Text(
                                    text = "Yanlış: $wrongAnswersCount",
                                    color = Color(0xFFE53935),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    private fun GameTopBar(
        gameName: String,
        timeRemaining: Int,
        score: Int,
        correctAnswers: Int,
        wrongAnswers: Int,
        level: String,
        onBackClick: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Üst kısım - Geri Butonu ve Başlık
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1565C0))
                    .padding(vertical = 8.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Geri butonu
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Geri Dön",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBackClick() }
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Oyun adı
                Text(
                    text = "$gameName - $level",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Alt kısım - Skor, süre bilgileri
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1976D2)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Süre göstergesi
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Süre",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$timeRemaining",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Skor göstergesi
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Skor",
                            color = Color.White,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$score",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    private fun getSeviyeAdi(): String {
        return when (selectedOperationType) {
            0 -> "Seviye 1"
            1 -> "Seviye 2"
            2 -> "Seviye 3"
            3 -> "Seviye 4"
            4 -> "Seviye 5"
            5 -> "Seviye 6"
            else -> "Seviye 1"
        }
    }
    
    @Composable
    private fun BalloonItem(balloon: Balloon, onClick: (Balloon) -> Unit) {
        val screenWidth = LocalConfiguration.current.screenWidthDp
        val screenHeight = LocalConfiguration.current.screenHeightDp
        
        // Ekran boyutlarını dikkate alarak piksel pozisyonunu hesapla
        val xPos = (balloon.x * screenWidth).dp
        val yPos = (balloon.y * screenHeight).dp
        
        // Patlatma animasyonu için
        val isPopping = poppingBalloons[balloon.id] == true
        val scale by animateFloatAsState(
            targetValue = if (isPopping) 1.3f else 1f,
            animationSpec = tween(
                durationMillis = if (isPopping) 200 else 0,
                easing = LinearEasing
            ), 
            label = "pop_scale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (isPopping) 0f else 1f,
            animationSpec = tween(
                durationMillis = if (isPopping) 200 else 0,
                easing = LinearEasing
            ),
            label = "pop_alpha"
        )
        
        // Patlatma efekti - ekranın dışına çıkınca kaldırılacak
        LaunchedEffect(isPopping) {
            if (isPopping) {
                delay(300) // Efektin tamamlanması için bekle
                poppingBalloons.remove(balloon.id)
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Ana balon
            if (!isPopping) {
                Box(
                    modifier = Modifier
                        .size(balloon.radius.dp * 2)
                        .offset(
                            x = xPos - balloon.radius.dp,
                            y = yPos - balloon.radius.dp
                        )
                        .clip(CircleShape)
                        .background(balloon.color)
                        .clickable { 
                            poppingBalloons[balloon.id] = true
                            lastPoppedBalloonId = balloon.id
                            onClick(balloon) 
                        }
                        .scale(scale)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = balloon.value.toString(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            } else {
                // Patlatma animasyonu
                Box(
                    modifier = Modifier
                        .size(balloon.radius.dp * 2.6f)
                        .offset(
                            x = xPos - balloon.radius.dp * 1.3f,
                            y = yPos - balloon.radius.dp * 1.3f
                        )
                        .scale(scale)
                        .alpha(1 - alpha),
                    contentAlignment = Alignment.Center
                ) {
                    // Patlama efektleri
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val center = Offset(size.width / 2, size.height / 2)
                        val patternCount = 8
                        val radiusOuter = size.minDimension / 2
                        
                        // Patlama parçaları
                        for (i in 0 until patternCount) {
                            val angle = (i * (360f / patternCount)) * (Math.PI / 180f).toFloat()
                            val endX = center.x + radiusOuter * kotlin.math.cos(angle)
                            val endY = center.y + radiusOuter * kotlin.math.sin(angle)
                            
                            // Ana parçalar
                            drawLine(
                                color = balloon.color,
                                start = center,
                                end = Offset(endX, endY),
                                strokeWidth = 4f
                            )
                            
                            // Küçük partiküller
                            val smallParticleDistance = radiusOuter * 0.7f
                            val particleX = center.x + smallParticleDistance * kotlin.math.cos(angle + 0.2f)
                            val particleY = center.y + smallParticleDistance * kotlin.math.sin(angle + 0.2f)
                            
                            drawCircle(
                                color = balloon.color,
                                radius = 4f,
                                center = Offset(particleX, particleY)
                            )
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    private fun GameOverScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            // Oyun sonu ekranı içeriği
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .width(340.dp), // Biraz daha geniş kart
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Başlık
                    Text(
                        text = "Oyun Bitti!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3F51B5),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    // Sonuç bilgileri
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Doğru cevaplar
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Doğru",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = correctAnswersCount.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Doğru", 
                                fontSize = 14.sp
                            )
                        }
                        
                        // Yanlış cevaplar
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Yanlış",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = wrongAnswersCount.toString(),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Yanlış", 
                                fontSize = 14.sp
                            )
                        }
                    }
                    
                    // Başarı Oranı Göstergesi - Sınıf değişkeni successRate'i kullan
                    val totalAnswers = correctAnswersCount + wrongAnswersCount
                    val formattedSuccessRate = successRate.toInt()
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF4CAF50).copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Başarı Oranı: %$formattedSuccessRate",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            
                            Text(
                                text = "$correctAnswersCount doğru / $totalAnswers toplam",
                                fontSize = 14.sp,
                                color = Color.DarkGray
                            )
                        }
                    }
                    
                    // Yıldızları direkt olarak sınıf değişkeninden al
                    StarRating(
                        stars = stars,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Yıldız kazanma kriteri açıklaması
                    YildizKriterleri(selectedOperationType)
                    
                    // Seviye başarı durumu
                    val successMessage = when {
                        stars == 3 -> "Harika! Mükemmel bir başarı!"
                        stars == 2 -> "Tebrikler! İyi bir sonuç!"
                        stars == 1 -> "Başarılı! Daha iyisini yapabilirsin!"
                        else -> "Tekrar dene! Başaracaksın!"
                    }
                    
                    Text(
                        text = successMessage,
                        fontSize = 16.sp,
                        color = Color.DarkGray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Butonlar - Maksimum seviyeye ulaşılmadıysa 3 buton, ulaşıldıysa 2 buton göster
                    if (selectedOperationType < 5 && stars > 0) {
                        // 3 buton düzeni: Ana Menü, Tekrar Oyna, Sonraki Seviye
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                            ) {
                                // Ana Menü butonu - Daha geniş tasarım
                                Button(
                                    onClick = {
                                        resetGame()
                                        showLevelSelectDialog = true
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF3F51B5)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp
                                    ),
                                    modifier = Modifier
                                        .width(150.dp)
                                        .height(48.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.width(6.dp))
                                        
                                        Text(
                                            text = "Ana Menü",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                                
                                // Tekrar Oyna butonu - Daha geniş tasarım
                                Button(
                                    onClick = {
                                        resetGame()
                                        gameStarted = true
                                        startGame()
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF4CAF50)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = ButtonDefaults.buttonElevation(
                                        defaultElevation = 6.dp
                                    ),
                                    modifier = Modifier
                                        .width(150.dp)
                                        .height(48.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.width(6.dp))
                                        
                                        Text(
                                            text = "Tekrar",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Sonraki Seviye butonu - tam genişlikte tek buton
                            Button(
                                onClick = {
                                    // Seviyeyi bir arttır ve oyunu başlat
                                    selectedOperationType++
                                    resetGame()
                                    gameStarted = true
                                    startGame()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF9800)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 6.dp
                                ),
                                modifier = Modifier
                                    .fillMaxWidth(0.9f)
                                    .height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Sonraki Seviye",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    
                                    Spacer(modifier = Modifier.width(6.dp))
                                    
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        // 2 buton düzeni: Ana Menü, Tekrar Oyna (son seviyede veya başarısız olunduğunda)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                        ) {
                            // Ana Menü butonu - Daha geniş tasarım
                            Button(
                                onClick = {
                                    resetGame()
                                    showLevelSelectDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF3F51B5)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 6.dp
                                ),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(6.dp))
                                    
                                    Text(
                                        text = "Ana Menü",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                }
                            }
                            
                            // Tekrar Oyna butonu - Daha geniş tasarım
                            Button(
                                onClick = {
                                    resetGame()
                                    gameStarted = true
                                    startGame()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = ButtonDefaults.buttonElevation(
                                    defaultElevation = 6.dp
                                ),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(6.dp))
                                    
                                    Text(
                                        text = "Tekrar Dene",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Yıldız kriterleri açıklama kartı - YENİ EKLENEN FONKSİYON
    @Composable
    private fun YildizKriterleri(selectedLevel: Int) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Text(
                    text = "Yıldız Kazanma Kriterleri:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                
                val thresholds = when (selectedLevel) {
                    0 -> Triple(50, 70, 85) // Seviye 1
                    1 -> Triple(55, 70, 85) // Seviye 2
                    2 -> Triple(55, 75, 90) // Seviye 3
                    3 -> Triple(60, 75, 90) // Seviye 4
                    4 -> Triple(60, 80, 90) // Seviye 5
                    5 -> Triple(65, 80, 90) // Seviye 6
                    else -> Triple(50, 70, 85) // Varsayılan
                }
                
                val minSoru = when (selectedLevel) {
                    0 -> 6 // Seviye 1 için minimum 6 soru
                    1 -> 8 // Seviye 2 için minimum 8 soru
                    2, 3 -> 10 // Seviye 3-4 için minimum 10 soru
                    4, 5 -> 12 // Seviye 5-6 için minimum 12 soru
                    else -> 8 // Varsayılan
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " 1 yıldız: En az %${thresholds.first} başarı oranı",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " 2 yıldız: En az %${thresholds.second} başarı oranı",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " 3 yıldız: En az %${thresholds.third} başarı oranı",
                        fontSize = 12.sp,
                        color = Color(0xFF666666)
                    )
                }
            }
        }
    }
    
    // Yıldız derecelendirmesi için composable
    @Composable
    private fun StarRating(stars: Int, modifier: Modifier = Modifier) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(3) { index ->
                val isFilled = index < stars
                
                // Yıldız animasyonu için
                var animatedScale by remember { mutableStateOf(1f) }
                
                LaunchedEffect(stars) {
                    if (isFilled) {
                        delay(index * 150L)
                        animatedScale = 0f
                        delay(50)
                        animatedScale = 1.2f
                        delay(150)
                        animatedScale = 1f
                    }
                }
                
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Yıldız ${index + 1}",
                    modifier = Modifier
                        .size(48.dp)
                        .scale(if (isFilled) animatedScale else 1f)
                        .padding(4.dp),
                    tint = if (isFilled) Color(0xFFFFD700) else Color.LightGray.copy(alpha = 0.5f)
                )
            }
        }
    }
    
    private fun resetGame() {
        gameTimeRemaining = getLevelTime()  // Seviyeye göre süreyi ayarla
        score = 0
        balloons.clear()
        isGameOver = false
        correctAnswersCount = 0
        wrongAnswersCount = 0
        successRate = 0f
        stars = 0
        showGameOverScreen = false // Oyun sonu ekranını da kapat
    }
    
    private fun handleBalloonClick(balloon: Balloon) {
        // Balonu listeden kaldırma işlemini geciktir (animasyon için)
        val balloonToRemove = balloon
        
        // Sadece mevcut soruya ait balonlar için işlem yap
        if (balloon.questionId == currentQuestionId) {
            if (balloon.isCorrect) {
                // Doğru balonu patlattı
                correctAnswersCount++
                score += calculatePoints()
                audioService.playCorrectSound()
                
                // 250ms sonra balonları kaldır (animasyon süresine izin ver)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(250)
                    balloons.removeAll { it.questionId == currentQuestionId }
                    
                    // Şimdi yeni bir soru için hazır
                    canGenerateNewQuestion = true
                }
            } else {
                // Yanlış balonu patlattı
                wrongAnswersCount++
                audioService.playWrongSound()
                
                // 250ms sonra yanlış balonu kaldır (animasyon süresine izin ver)
                CoroutineScope(Dispatchers.Main).launch {
                    delay(250)
                    balloons.remove(balloonToRemove)
                }
            }
        } else {
            // 250ms sonra balonu kaldır (animasyon süresine izin ver)
            CoroutineScope(Dispatchers.Main).launch {
                delay(250)
                balloons.remove(balloonToRemove)
            }
        }
    }
    
    override fun generateQuestion() {
        // Seçilen operasyon tipine göre soru türünü belirle
        val questionType = when (selectedOperationType) {
            0 -> QuestionType.ADDITION
            1 -> QuestionType.SUBTRACTION
            2 -> QuestionType.MULTIPLICATION
            3 -> QuestionType.DIVISION // Yeni eklenen bölme işlemi
            4 -> QuestionType.MIXED_BASIC // Karışık (toplama, çıkarma, çarpma, bölme)
            5 -> QuestionType.MIXED_ADVANCED // İleri seviye karışık
            else -> QuestionType.ADDITION
        }
        
        // Zorluk seviyesini belirle
        val difficulty = when (selectedOperationType) {
            0 -> DifficultyLevel.EASY
            1 -> DifficultyLevel.MEDIUM
            2 -> DifficultyLevel.HARD
            3 -> DifficultyLevel.HARD // Bölme işlemi için zorluk seviyesi
            4 -> DifficultyLevel.VERY_HARD
            5 -> DifficultyLevel.EXPERT
            else -> DifficultyLevel.EASY
        }
        
        currentQuestion = QuestionModel.createRandomQuestion(
            type = questionType,
            difficulty = difficulty
        )
    }
    
    override fun checkAnswer(answer: Any): Boolean {
        return currentQuestion?.checkAnswer(answer as Int) ?: false
    }
    
    override fun updateDifficulty() {
        // Zorluk seviyesi kullanıcı tarafından başlangıçta seçildiği için
        // burada bir şey yapmaya gerek yok
    }
    
    /**
     * Mevcut skora göre zorluk seviyesini döndürür.
     */
    private fun getDifficultyLevel(): DifficultyLevel {
        return when {
            score < 100 -> DifficultyLevel.EASY
            score < 250 -> DifficultyLevel.MEDIUM
            score < 500 -> DifficultyLevel.HARD
            score < 1000 -> DifficultyLevel.VERY_HARD
            else -> DifficultyLevel.EXPERT
        }
    }
    
    /**
     * Oyun bittiğinde çağrılır, sonuçları değerlendirir.
     */
    private fun finishGame() {
        Log.d(TAG, "finishGame: Oyun bitti, değerlendirme yapılıyor.")
        gameStarted = false
        
        // Oyun bitti - oyun bileşenlerini durdur
        isGeneratingBalloons = false

        // Performans hesaplama
        val totalAnswers = correctAnswersCount + wrongAnswersCount
        // Başarı oranını hesapla (en az 1 cevap verilmişse)
        val calculatedSuccessRate = if (totalAnswers > 0) {
            (correctAnswersCount.toFloat() / totalAnswers.toFloat()) * 100
        } else {
            0f
        }
        
        // Başarı oranı ve yıldız değerlerini güncelle
        successRate = calculatedSuccessRate
        
        // Yıldız hesaplama - float to int dönüşümüyle
        stars = when {
            calculatedSuccessRate >= 90f -> 3
            calculatedSuccessRate >= 70f -> 2
            calculatedSuccessRate >= 50f -> 1
            correctAnswersCount > 0 -> 1 // En az bir doğru varsa 1 yıldız
            else -> 0
        }

        // İstatistikleri loglama - düzeltilmiş log çıktıları
        Log.d(TAG, "finishGame: Doğru cevaplar: $correctAnswersCount, Yanlış cevaplar: $wrongAnswersCount")
        Log.d(TAG, "finishGame: Toplam cevaplar: $totalAnswers, Başarı oranı: $successRate%")
        Log.d(TAG, "finishGame: Kazanılan yıldızlar: $stars")
        
        // Önemli: Seviye bilgisini BaseGameActivity'deki currentLevel'a atıyoruz
        // Bu sayede recordGameStatistics ve saveGameProgress doğru seviye bilgisiyle çalışacak
        currentLevel = selectedOperationType + 1
        
        try {
            // GameStatisticsHelper ile istatistikleri kaydedelim
            gameStatisticsHelper.recordGameCompletion(
                gameType = gameType,
                level = selectedOperationType + 1,
                score = score,
                stars = stars,
                gameStartTime = gameStartTime,
                taskId = dailyTaskId
            )
            
            // BaseGameActivity'nin endGame metodunu çağırarak diğer işlemleri yapalım
            super.endGame()
            
            // Kullanıcıya kazandığı yıldızları bildir
            Toast.makeText(this, 
                "$stars yıldız kazandınız! Toplam yıldızlarınıza eklendi.", 
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Log.e(TAG, "finishGame: İstatistik kaydedilirken hata oluştu: ${e.message}", e)
            Toast.makeText(this, "İstatistikler kaydedilirken bir hata oluştu", Toast.LENGTH_SHORT).show()
        }

        // Oyun bitti ekranını göster
        showGameOverScreen = true
    }
    
    // Seviyeye göre balon sayısını döndüren fonksiyon
    private fun getBalloonCountForLevel(): Int {
        return when (selectedOperationType) {
            0, 1 -> 3  // Seviye 1-2: 3 balon
            2, 3 -> 4  // Seviye 3-4: 4 balon
            4, 5 -> 5  // Seviye 5-6: 5 balon
            else -> 3  // Varsayılan
        }
    }
    
    // Seviyeye göre oyun süresini döndüren fonksiyon
    private fun getLevelTime(): Int {
        return when (selectedOperationType) {
            0 -> 75  // Seviye 1: Daha uzun süre (kolay seviye)
            1 -> 70  // Seviye 2
            2 -> 65  // Seviye 3
            3 -> 65  // Seviye 4
            4 -> 60  // Seviye 5
            5 -> 60  // Seviye 6: Normal süre (zor seviye)
            else -> 60 // Varsayılan
        }
    }
    
    // Seviyeye göre balonların hız faktörünü döndüren fonksiyon
    private fun getBalloonSpeed(isCorrect: Boolean): Float {
        // Taban hız - yükseldikçe hızı artırır
        val baseSpeed = when (selectedOperationType) {
            0 -> 0.35f  // Seviye 1: En yavaş
            1 -> 0.40f
            2 -> 0.45f
            3 -> 0.50f
            4 -> 0.55f
            5 -> 0.60f  // Seviye 6: En hızlı
            else -> 0.45f
        }
        
        // Rastgele varyasyon ekleyerek hızları belirle
        return if (isCorrect) {
            // Doğru cevap içeren balon, diğerlerinden biraz daha yavaş olsun
            Random.nextFloat() * 0.15f + baseSpeed
        } else {
            // Yanlış cevap içeren balonlar, biraz daha hızlı olsun
            Random.nextFloat() * 0.15f + (baseSpeed + 0.05f)
        }
    }
    
    override fun startGame() {
        try {
            score = 0
            stars = 0 // Yıldızları da sıfırla
            isGameOver = false
            correctAnswersCount = 0  // Doğru sayısını sıfırla
            wrongAnswersCount = 0   // Yanlış sayısını sıfırla
            successRate = 0f       // Başarı oranını sıfırla
            
            // Oyun başlangıç zamanını güncelle - Oyun her başladığında
            gameStartTime = System.currentTimeMillis()
            Log.d(TAG, "startGame: gameStartTime güncellendi: $gameStartTime")
            
            // Önceki kod: 
            // audioService.playBackgroundMusic() // Bu müziği yeniden başlatıyor
            
            // Yeni kod: Müziği devam ettir - eğer çalmıyorsa başlat, çalıyorsa aynı kalsın
            audioService.resumeBackgroundMusic()
            
            generateQuestion()
        } catch (e: Exception) {
            Log.e(TAG, "Oyun başlatılırken hata: ${e.message}", e)
            // Hata durumunda kullanıcıya bilgi verilebilir
            android.widget.Toast.makeText(this, "Oyun başlatılırken bir sorun oluştu.", android.widget.Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

/**
 * Balon sınıfı, oyundaki her bir balonu temsil eder.
 */
data class Balloon(
    val id: Long,
    val questionId: Long, // Hangi soruya ait olduğunu belirten ID
    val x: Float, // Ekrandaki x pozisyonu (0-1 arası)
    val y: Float, // Ekrandaki y pozisyonu (0-1 arası)
    val radius: Float, // Balonun yarıçapı (dp)
    val color: Color, // Balonun rengi
    val value: Int, // Balondaki sayı
    val isCorrect: Boolean, // Doğru cevap mı
    val speedFactor: Float // Balonun hız faktörü (1.0 = normal hız)
) 