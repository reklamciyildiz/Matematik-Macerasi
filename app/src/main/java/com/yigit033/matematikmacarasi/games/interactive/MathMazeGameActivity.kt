package com.yigit033.matematikmacarasi.games.interactive

import android.content.Context
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.models.Direction
import com.yigit033.matematikmacarasi.games.models.MazeCell
import com.yigit033.matematikmacarasi.games.models.MazeCellType
import com.yigit033.matematikmacarasi.games.models.MazeGameState
import com.yigit033.matematikmacarasi.games.models.MazeGenerator
import com.yigit033.matematikmacarasi.games.models.MazeLevel
import com.yigit033.matematikmacarasi.games.models.MazeLevels
import com.yigit033.matematikmacarasi.games.models.MathQuestion
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import kotlinx.coroutines.delay
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.layout.offset
import com.yigit033.matematikmacarasi.services.AudioService
import com.yigit033.matematikmacarasi.services.DailyTaskManager
import com.yigit033.matematikmacarasi.DailyTaskType
import com.yigit033.matematikmacarasi.services.GameStatisticsService
import com.yigit033.matematikmacarasi.services.GameProgressService
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.activities.BaseGameActivity
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.yigit033.matematikmacarasi.services.GameStatisticsHelper
import com.yigit033.matematikmacarasi.services.payments.AdManager
import com.yigit033.matematikmacarasi.utils.NetworkUtils
import com.yigit033.matematikmacarasi.services.DailyGameLimitService
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager

/**
 * MathMazeGameActivity, matematik labirenti oyunu için aktivite.
 */
class MathMazeGameActivity : ComponentActivity() {
    
    // GameType for this activity
    private val gameType: GameType = GameType.MATH_MAZE_GAME
    
    // Ses servisi
    private lateinit var audioService: AudioService
    
    // AdManager ekleyelim
    private lateinit var adManager: AdManager
    
    // GameStatisticsHelper ekleniyor
    private lateinit var gameStatisticsHelper: GameStatisticsHelper
    
    // Günlük sınır servisi
    private lateinit var dailyGameLimitService: DailyGameLimitService
    
    // Premium kontrol servisi
    private var purchaseManager: PurchaseManager? = null
    
    // Premium durumu
    private var isPremium by mutableStateOf(false)
    
    // Günlük sınır durumu
    private var canPlayToday by mutableStateOf(true)
    private var remainingPlays by mutableStateOf(10)
    private var showLimitDialog by mutableStateOf(false)
    
    // Oyun Zamanlayıcısı
    private val handler = Handler(Looper.getMainLooper())
    private var gameTimerTask: Runnable? = null
    
    // Günlük görev takibi için
    private lateinit var dailyTaskManager: DailyTaskManager
    private var dailyTaskId: Int = -1
    
    // İstatistik servisi
    private lateinit var gameStatisticsService: GameStatisticsService
    
    // Oyun başlangıç zamanı
    private var gameStartTime: Long = 0
    
    // Oyun durumu
    private var gameState = MazeGameState(
        level = MazeLevels.levels.firstOrNull() ?: MazeLevel(
            level = 1,
            size = 9,
            questionCount = 2,
            timeInSeconds = 120,
            difficultyLevel = 1
        ),
        maze = emptyArray(),
        questions = emptyList(),
        playerPosition = Pair(0, 0),
        timeRemaining = 120
    )
    
    // Oyun tamamlandı mı?
    private var isGameComplete = false
    
    // Ses dosyaları
    private var soundCorrect: MediaPlayer? = null
    private var soundWrong: MediaPlayer? = null
    private var soundComplete: MediaPlayer? = null
    
    // Durum değişkenleri
    private var showQuestion = false
    private var currentQuestion: MathQuestion? = null
    private var pendingDirection: Direction? = null
    private var showFeedback = false
    private var isCorrectAnswer = false
    private var consecutiveWrongAnswers = 0
    private var requiredQuestionCount = 0
    private var answeredQuestions = 0
    private val _answeredQuestions = mutableListOf<MathQuestion>()
    
    // Zorluk seviyesi seçim dialogu için state
    private var showLevelSelectDialog by mutableStateOf(true)
    private var selectedLevelIndex by mutableStateOf(0)
    
    // Compose State Değişkenleri - Dışarıdan kontrol edeceğiz
    private val _state = mutableStateOf(gameState)
    private val _showingQuestion = mutableStateOf(showQuestion)
    private val _currentQ = mutableStateOf(currentQuestion)
    
    // Sınıf içine tekrarlayan bildirimleri engellemek için bir bayrak ekleyelim
    private var allQuestionsCompletedToastShown = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // İnternet bağlantısı kontrolü
            if (!NetworkUtils.isInternetAvailable(this)) {
                Log.w("MathMazeGameActivity", "İnternet bağlantısı yok, oyun başlatılamıyor")
                Toast.makeText(
                    this,
                    "Bu oyunu oynamak için internet bağlantısı gereklidir!",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return
            }
            
            // Ses servisini başlat
            audioService = AudioService.getInstance(this)
            
            // Günlük görev yöneticisini başlat
            dailyTaskManager = DailyTaskManager.getInstance(this)
            
            // İstatistik servisini başlat
            gameStatisticsService = GameStatisticsService(this)
            
            // GameStatisticsHelper başlat
            gameStatisticsHelper = GameStatisticsHelper(this)
            
            // AdManager'ı başlat
            adManager = AdManager.getInstance(this)
            // Oyun başında reklamı hazırla
            adManager.loadInterstitialAd()
            
            // Günlük sınır servisini başlat
            dailyGameLimitService = DailyGameLimitService.getInstance(this)
            
            // Premium durumunu kontrol et
            try {
                purchaseManager = PurchaseManager.getInstance(this)
                isPremium = purchaseManager!!.isPremiumSync()
            } catch (e: Exception) {
                Log.e("MathMazeGame", "PurchaseManager başlatılamadı: ${e.message}", e)
                isPremium = false
            }
            
            // Günlük sınır kontrolü
            canPlayToday = dailyGameLimitService.canPlayMathMaze(isPremium)
            remainingPlays = dailyGameLimitService.getRemainingMathMazePlays(isPremium)
            
            Log.d("MathMazeGame", "Günlük sınır durumu - Premium: $isPremium, Oynayabilir: $canPlayToday, Kalan: $remainingPlays")
            
            // Eğer günlük sınır aşıldıysa dialog göster
            if (!canPlayToday) {
                showLimitDialog = true
            }
            
            // Oyunun başlangıç zamanını kaydet
            gameStartTime = System.currentTimeMillis()
            
            // Intent'ten görev ID'sini al (varsa)
            dailyTaskId = intent.getIntExtra("daily_task_id", -1)
            
            // Ses oynatıcılarını oluştur (güvenli başlatma)
            soundCorrect = try { MediaPlayer.create(this, R.raw.correct_sound) } catch (e: Exception) { null }
            soundWrong = try { MediaPlayer.create(this, R.raw.wrong_sound) } catch (e: Exception) { null }
            soundComplete = try { MediaPlayer.create(this, R.raw.success) } catch (e: Exception) { null }
            
            setContent {
                MatematikMacerasiTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        // Günlük sınır dialogu
                        if (showLimitDialog) {
                            DailyLimitDialog(
                                onDismiss = { 
                                    showLimitDialog = false
                                    finish() // Oyunu kapat
                                },
                                onUpgradeToPremium = {
                                    showLimitDialog = false
                                    // Premium satın alma
                                    try {
                                        purchaseManager?.buyPremium(this@MathMazeGameActivity)
                                    } catch (e: Exception) {
                                        Log.e("MathMazeGame", "Premium satın alma başlatılamadı: ${e.message}", e)
                                    }
                                },
                                remainingPlays = remainingPlays,
                                dailyLimit = dailyGameLimitService.getMathMazeDailyLimit()
                            )
                        } else if (showLevelSelectDialog) {
                            LevelSelectDialog(
                                onLevelSelected = { levelIndex ->
                                    selectedLevelIndex = levelIndex
                                    showLevelSelectDialog = false
                                    
                                    // Oyun başlatılmadan önce günlük sınır kontrolü
                                    if (canPlayToday) {
                                        // Oyun sayısını kaydet
                                        dailyGameLimitService.recordMathMazePlay()
                                        remainingPlays = dailyGameLimitService.getRemainingMathMazePlays(isPremium)
                                    
                                    // Seçilen seviyeye göre oyunu başlat
                                    initGame(levelIndex)
                                    startGame()
                                    } else {
                                        showLimitDialog = true
                                    }
                                },
                                onDismiss = { finish() }
                            )
                        } else {
                            MathMazeGameScreen()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("MathMazeGame", "Oyun başlatılırken hata: ${e.message}")
            Toast.makeText(this, "Oyun başlatılırken bir hata oluştu", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    @Composable
    private fun LevelSelectDialog(
        onLevelSelected: (Int) -> Unit,
        onDismiss: () -> Unit
    ) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A237E)
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF1A237E),
                                    Color(0xFF3949AB)
                                )
                            )
                        )
                ) {
                    // Labirent arka plan deseni çizimi
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(800.dp)
                            .alpha(0.15f)
                    ) {
                        val pathWidth = 10.dp.toPx()
                        val cellSize = size.width / 8
                        
                        // Rastgele labirent desen çizgileri
                        val random = kotlin.random.Random(42)
                        for (i in 0..15) {
                            for (j in 0..15) {
                                if (random.nextBoolean()) {
                                    // Yatay çizgi
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(j * cellSize, i * cellSize),
                                        end = Offset((j + 1) * cellSize, i * cellSize),
                                        strokeWidth = pathWidth
                                    )
                                } else {
                                    // Dikey çizgi
                                    drawLine(
                                        color = Color.White,
                                        start = Offset(j * cellSize, i * cellSize),
                                        end = Offset(j * cellSize, (i + 1) * cellSize),
                                        strokeWidth = pathWidth
                                    )
                                }
                            }
                        }
                    }
                    
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Başlık
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            // Labirent ikonu
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF64B5F6))
                                    .padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_algorithm),
                                    contentDescription = "Labirent",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = "MATEMATİK LABİRENTİ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        Text(
                            text = "SEVİYE SEÇİN",
                            fontSize = 16.sp,
                            color = Color(0xFF90CAF9),
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Seviyeler içeren scrollable alan
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Seviye 1: Toplama
                            LevelCard(
                                title = "Seviye 1: Kolay Toplama",
                                details = listOf(
                                    "Labirent: 9x9",
                                    "İşlem: 1-10 arası toplama",
                                    "Soru: ${MazeLevels.levels[0].questionCount} adet",
                                    "Süre: ${MazeLevels.levels[0].timeInSeconds} saniye"
                                ),
                                description = "Küçük labirentte basit toplama soruları çöz",
                                color = Color(0xFF42A5F5),
                                levelIcon = R.drawable.ic_addition,
                                isRecommended = false,
                                age = "6-8 yaş"
                            ) {
                                onLevelSelected(0)
                            }
                            
                            // Seviye 2: Çıkarma
                            LevelCard(
                                title = "Seviye 2: Kolay Çıkarma",
                                details = listOf(
                                    "Labirent: 9x9",
                                    "İşlem: 1-10 arası çıkarma",
                                    "Soru: ${MazeLevels.levels[1].questionCount} adet",
                                    "Süre: ${MazeLevels.levels[1].timeInSeconds} saniye"
                                ),
                                description = "Küçük labirentte basit çıkarma soruları çöz",
                                color = Color(0xFF29B6F6),
                                levelIcon = R.drawable.ic_subtraction,
                                isRecommended = false,
                                age = "6-8 yaş"
                            ) {
                                onLevelSelected(1)
                            }
                            
                            // Seviye 3: Karışık Toplama-Çıkarma
                            LevelCard(
                                title = "Seviye 3: Karışık İşlemler",
                                details = listOf(
                                    "Labirent: 11x11",
                                    "İşlem: 1-20 arası toplama/çıkarma",
                                    "Soru: ${MazeLevels.levels[2].questionCount} adet",
                                    "Süre: ${MazeLevels.levels[2].timeInSeconds} saniye"
                                ),
                                description = "Orta büyüklükte labirentte karışık toplama-çıkarma soruları çöz",
                                color = Color(0xFF26C6DA),
                                levelIcon = R.drawable.ic_algorithm,
                                isRecommended = false,
                                age = "6-9 yaş"
                            ) {
                                onLevelSelected(2)
                            }
                            
                            // Seviye 4: Çarpma
                            LevelCard(
                                title = "Seviye 4: Çarpma İşlemleri",
                                details = listOf(
                                    "Labirent: 13x13",
                                    "İşlem: 1-5 arası çarpma",
                                    "Soru: ${MazeLevels.levels[3].questionCount} adet",
                                    "Süre: ${MazeLevels.levels[3].timeInSeconds} saniye"
                                ),
                                description = "Büyük labirentte çarpma işlemleri çöz",
                                color = Color(0xFF00ACC1),
                                levelIcon = R.drawable.ic_multiplication,
                                isRecommended = false,
                                age = "7-10 yaş"
                            ) {
                                onLevelSelected(3)
                            }
                            
                            // Seviye 5: Bölme
                            LevelCard(
                                title = "Seviye 5: Bölme İşlemleri",
                                details = listOf(
                                    "Labirent: 13x13",
                                    "İşlem: 1-5 arası kalansız bölme",
                                    "Soru: ${MazeLevels.levels[4].questionCount} adet",
                                    "Süre: ${MazeLevels.levels[4].timeInSeconds} saniye"
                                ),
                                description = "Büyük labirentte bölme işlemleri çöz",
                                color = Color(0xFF0097A7),
                                levelIcon = R.drawable.ic_division,
                                isRecommended = false,
                                age = "7-10 yaş"
                            ) {
                                onLevelSelected(4)
                            }
                            
                            // Seviye 6: Karışık Çarpma-Bölme
                            LevelCard(
                                title = "Seviye 6: Çarpma ve Bölme",
                                details = listOf(
                                    "Labirent: 15x15",
                                    "İşlem: 1-12 arası çarpma/bölme",
                                    "Soru: ${MazeLevels.levels[5].questionCount} adet",
                                    "Süre: ${MazeLevels.levels[5].timeInSeconds} saniye"
                                ),
                                description = "Daha büyük labirentte çarpma ve bölme işlemleri çöz",
                                color = Color(0xFF00838F),
                                levelIcon = R.drawable.ic_algorithm,
                                isRecommended = false,
                                age = "7-11 yaş"
                            ) {
                                onLevelSelected(5)
                            }
                            
                            // Seviye 7: İleri Seviye
                            LevelCard(
                                title = "Seviye 7: İleri Seviye",
                                details = listOf(
                                    "Labirent: 17x17",
                                    "İşlem: Toplama/Çıkarma: 20-100",
                                    "İşlem: Çarpma: 1-20, Bölme: 1-50",
                                    "Soru: ${MazeLevels.levels[6].questionCount} adet",
                                    "Süre: ${MazeLevels.levels[6].timeInSeconds} saniye"
                                ),
                                description = "Büyük labirentte karışık ve zor işlemler çöz",
                                color = Color(0xFF1565C0),
                                levelIcon = R.drawable.ic_algorithm,
                                isRecommended = false,
                                age = "9-12 yaş"
                            ) {
                                onLevelSelected(6)
                            }
                            
                            // Seviye 8: Uzman
                            LevelCard(
                                title = "Seviye 8: Uzman Seviye",
                                details = listOf(
                                    "Labirent: 19x19",
                                    "İşlem: Toplama/Çıkarma: 50-200",
                                    "İşlem: Çarpma: 1-30, Bölme: 1-100",
                                    "Soru: ${MazeLevels.levels[7].questionCount} adet",
                                    "Süre: ${MazeLevels.levels[7].timeInSeconds} saniye"
                                ),
                                description = "En zor labirent ve işlemlerle gerçek bir matematik uzmanı ol!",
                                color = Color(0xFF0D47A1),
                                levelIcon = R.drawable.ic_algorithm,
                                isRecommended = false,
                                age = "11+ yaş"
                            ) {
                                onLevelSelected(7)
                            }
                        }
                        
                        // Kapat butonu
                        Button(
                            onClick = { onDismiss() },
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth(0.5f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF5C6BC0)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = "GERİ DÖN",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    fun LevelCard(
        title: String,
        details: List<String>,
        description: String,
        color: Color,
        levelIcon: Int,
        isRecommended: Boolean,
        age: String,
        onClick: () -> Unit
    ) {
        // Kart tıklanması için animasyon
        var isPressed by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = tween(100),
            label = "cardPressAnimation"
        )
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp)
                .scale(scale)
                .shadow(
                    elevation = 6.dp,
                    spotColor = Color.Black.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable {
                    isPressed = true
                    onClick()
                    // Basma animasyonunu sıfırla
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        isPressed = false
                    }, 150)
                },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF303F9F).copy(alpha = 0.9f)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 2.dp,
                color = color
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Seviye ikonu ve numarası
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        color,
                                        color.copy(alpha = 0.7f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = levelIcon),
                            contentDescription = title,
                            modifier = Modifier.size(28.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Seviye başlığı
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_person),
                                contentDescription = "Yaş aralığı",
                                modifier = Modifier.size(14.dp),
                                tint = Color(0xFF90CAF9)
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = age,
                                fontSize = 12.sp,
                                color = Color(0xFF90CAF9),
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Tavsiye ediliyorsa göster
                        if (isRecommended) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_star),
                                    contentDescription = "Tavsiye edilen",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color(0xFFFFD54F)
                                )
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                Text(
                                    text = "Tavsiye Edilen",
                                    fontSize = 12.sp,
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                
                // Açıklama
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Seviye detayları - yeni eklenen kısım
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A237E).copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "SEVİYE DETAYLARI",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF90CAF9),
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                        
                        // Detayları liste halinde göster
                        details.forEach { detail ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(color, CircleShape)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = detail,
                                    fontSize = 12.sp,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Başla butonu
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    color,
                                    color.copy(alpha = 0.7f)
                                )
                            )
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "LABİRENTE GİR",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Maze ikonu
                        Icon(
                            painter = painterResource(id = R.drawable.ic_question),
                            contentDescription = "Oyna",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
    
    @Composable
    private fun SpaceBackground() {
        // Yıldızlar 
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .alpha(0.7f)
        ) {
            // Rastgele yıldızlar çiz
            val random = kotlin.random.Random(42) // Sabit seed ile tutarlı rastgelelik
            repeat(50) {
                val x = random.nextFloat() * size.width
                val y = random.nextFloat() * size.height
                val radius = (1 + random.nextFloat() * 2).dp.toPx()
                val alpha = 0.3f + random.nextFloat() * 0.7f
                
                drawCircle(
                    color = Color.White.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(x, y)
                )
            }
        }
    }
    
    private fun initGame(levelIndex: Int = 0) {
        try {
            // Log kaydı ekle: GameStatisticsHelper'ın durumunu kontrol et
            Log.d("MathMazeGameActivity", "Oyun başlatılıyor - Seviye: $levelIndex, İstatistik Yardımcısı: ${if (::gameStatisticsHelper.isInitialized) "Hazır" else "Hazır değil"}")
            
            // Oyun durumunu oluştur
            val level = if (levelIndex < MazeLevels.levels.size) {
                MazeLevels.levels[levelIndex]
            } else {
                MazeLevels.levels[0] // Varsayılan ilk seviye
            }
            
            val mazeResult = MazeGenerator.generateMaze(level)
            val maze = mazeResult.first
            val playerPosition = mazeResult.second
            
            // Soruları oluştur - net ve görülebilir sorular oluştur
            var questions = MazeGenerator.generateQuestions(level)
            
            // Soruların görülebilir olmasını garantile
            questions = questions.map { question ->
                // Soru metninin boş olup olmadığını kontrol et ve düzelt
                val questionText = if (question.question.isNullOrBlank()) {
                    // Operasyon tipine göre soru metni oluştur
                    // MathQuestion'da operand1 ve operand2 bulunmadığı için doğrudan bir örnek soru metni oluşturalım
                    val randomNum1 = (1..20).random()
                    val randomNum2 = (1..10).random()
                    
                    "${randomNum1} + ${randomNum2} = ?"
                } else {
                    question.question
                }
                
                // Soruyu düzeltilmiş metinle güncelle
                question.copy(question = questionText)
            }
            
            // Soru hücrelerini say
            var questionCount = 0
            for (row in maze.indices) {
                for (col in maze[0].indices) {
                    if (maze[row][col].type == MazeCellType.QUESTION) {
                        questionCount++
                        // Başlangıçta tüm hücreleri ziyaret edilmemiş olarak işaretle
                        maze[row][col].visited = false
                    }
                }
            }
            
            // Durum değişkenlerini sıfırla
            requiredQuestionCount = questionCount
            answeredQuestions = 0
            _answeredQuestions.clear()
            isGameComplete = false
            consecutiveWrongAnswers = 0
            showQuestion = false
            _showingQuestion.value = false
            currentQuestion = null
            _currentQ.value = null
            
            // Loglama ekle
            android.util.Log.d("MathMazeGame", "Labirentte $questionCount soru hücresi bulundu")
            
            // Oyun durumunu güncelle
            gameState = MazeGameState(
                level = level,
                maze = maze,
                questions = questions,
                playerPosition = playerPosition,
                timeRemaining = level.timeInSeconds,
                gameOver = false,
                gameCompleted = false,
                score = 0,
                stars = 0
            )
            
            _state.value = gameState
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Oyun durumu oluşturulamadı: ${e.message}", Toast.LENGTH_SHORT).show()
            throw e
        }
    }
    
    private fun startGame() {
        // Zamanlayıcıyı başlat
        gameTimerTask = object : Runnable {
            override fun run() {
                if (gameState.timeRemaining > 0 && !gameState.gameOver && !gameState.gameCompleted) {
                    // Süreyi azalt
                    gameState = gameState.copy(timeRemaining = gameState.timeRemaining - 1)
                    
                    // State'i güncelle
                    _state.value = gameState
                    
                    // Süre bitti mi kontrol et
                    if (gameState.timeRemaining <= 0) {
                        endGame(false)
                    } else {
                        // Zamanlayıcıyı yeniden başlat
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        }
        
        // Test log kaydı ekle
        Log.d("MathMazeGameActivity", "Zamanlayıcı başlatıldı, GameStatisticsHelper: ${if (::gameStatisticsHelper.isInitialized) "Hazır" else "Hazır değil"}")
        
        handler.postDelayed(gameTimerTask!!, 1000)
    }
    
    private fun endGame(completed: Boolean) {
        // Zamanlayıcıyı durdur
        gameTimerTask?.let { handler.removeCallbacks(it) }
        
        // Aynı oyunu tekrar bitirmesini önle
        if (gameState.gameCompleted || gameState.gameOver) {
            return
        }
        
        // Oyunun tamamlandığını işaretle
        isGameComplete = completed
        
        // Oyun durumunu güncelle
        val stars = if (completed) {
            val percentage = (gameState.timeRemaining.toFloat() / gameState.level.timeInSeconds) * 100
            when {
                percentage >= 70 -> 3
                percentage >= 40 -> 2
                else -> 1
            }
        } else {
            0
        }
        
        // Skor hesapla
        val score = if (completed) {
            // Zaman temelli skor hesaplaması
            val baseScore = answeredQuestions * 100
            val timeBonus = (gameState.timeRemaining * 5)
            baseScore + timeBonus
        } else {
            answeredQuestions * 50  // Tamamlanmamış oyun için daha düşük skor
        }
        
        // Oyun durumunu güncelle
        gameState = gameState.copy(
            gameCompleted = completed,
            gameOver = !completed,
            stars = stars,
            score = score
        )
        
        Log.d("MathMazeGameActivity", "Oyun tamamlandı: Seviye=${gameState.level.levelIndex}, Skor=$score, Yıldız=$stars")
        
        try {
            // Başarısız koşular günlük görev ve tamamlanan oyun sayısını artırmamalı.
            if (completed) {
                gameStatisticsHelper.recordGameCompletion(
                    gameType = gameType,
                    level = gameState.level.levelIndex,
                    score = score,
                    stars = stars,
                    gameStartTime = gameStartTime,
                    taskId = dailyTaskId
                )
                Log.d("MathMazeGameActivity", "Başarılı oyun istatistikleri kaydedildi")
            }
            
            // Oyun ilerlemesini ve yıldızları kaydet
            val gameProgressService = GameProgressService.getInstance(this)
            gameProgressService.saveProgress("MathMazeGameActivity", gameState.level.levelIndex, score, stars)
            
            // StarManager ile yıldızları kaydet
            if (stars > 0) {
                val starManager = StarManager.getInstance(this)
                val gameId = "MathMazeGameActivity_${gameState.level.levelIndex}"
                val success = starManager.addStars(gameId, stars)
                Log.d("MathMazeGameActivity", "Yıldız ekleme: $success (Oyun: $gameId, Yıldız: $stars)")
            }
            
            // Oyun bitişinde geçiş reklamı göster
            try {
                if (::adManager.isInitialized) {
                    Log.d("MathMazeGameActivity", "Oyun sonunda interstitial reklam gösterilecek")
                    showInterstitialAd()
                } else {
                    Log.w("MathMazeGameActivity", "AdManager başlatılmamış, reklam gösterilemiyor")
                }
            } catch (e: Exception) {
                Log.e("MathMazeGameActivity", "Reklam gösterimi sırasında hata: ${e.message}", e)
            }
            
        } catch (e: Exception) {
            Log.e("MathMazeGameActivity", "İstatistik ve ilerleme kaydedilirken hata: ${e.message}", e)
        }
        
        // State'i güncelle
        _state.value = gameState
        
        // Başarı sesi çal
        if (completed) {
            audioService.playSuccessSound()
        } else {
            // Başarısız olma durumunda uygun sesi çal
            audioService.playFailureSound()
        }
        
        // Oyun sonuç diyaloğunu göster - refreshUI ile UI'ı güncelliyoruz
        refreshUI()
    }
    
    @Composable
    fun MathMazeGameScreen() {
        // Dışarıdan kontrol edilen state'leri kullan
        val state by remember { _state }
        val showingQuestion by remember { _showingQuestion }
        val currentQ by remember { _currentQ }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF3F51B5), // Koyu mavi üst
                            Color(0xFFBBDEFB)  // Açık mavi alt
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Üst bilgi çubuğu
                GameTopBar(
                    gameName = "Matematik Labirenti",
                    level = state.level.level,
                    timeRemaining = state.timeRemaining,
                    score = state.score,
                    totalTime = state.level.timeInSeconds,
                    onBackClick = { finish() }
                )
                
                // Soru sayacı
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF303F9F).copy(alpha = 0.7f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Sorular:",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            text = "$answeredQuestions/$requiredQuestionCount",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Labirent gösterimi
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD).copy(alpha = 0.9f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    MazeGrid(
                        maze = state.maze,
                        playerPosition = state.playerPosition
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Yön butonları - Yeni düzen: artı şeklinde
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Yön tuşları için daha fazla alan
                ) {
                    // Yukarı tuşu - ortada üstte
                    Button(
                        onClick = { movePlayer(Direction.UP) },
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.TopCenter)
                            .offset(y = 20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBBDEFB),
                            contentColor = Color(0xFF2196F3)
                        ),
                        shape = CircleShape
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_up),
                            contentDescription = "Yukarı",
                            modifier = Modifier.size(36.dp),
                            tint = Color(0xFF2196F3)
                        )
                    }
                    
                    // Sol tuşu - sol ortada
                    Button(
                        onClick = { movePlayer(Direction.LEFT) },
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.CenterStart)
                            .offset(x = 40.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBBDEFB),
                            contentColor = Color(0xFF2196F3)
                        ),
                        shape = CircleShape
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_left),
                            contentDescription = "Sol",
                            modifier = Modifier.size(36.dp),
                            tint = Color(0xFF2196F3)
                        )
                    }
                    
                    // Sağ tuşu - sağ ortada
                    Button(
                        onClick = { movePlayer(Direction.RIGHT) },
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.CenterEnd)
                            .offset(x = (-40).dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBBDEFB),
                            contentColor = Color(0xFF2196F3)
                        ),
                        shape = CircleShape
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_right),
                            contentDescription = "Sağ",
                            modifier = Modifier.size(36.dp),
                            tint = Color(0xFF2196F3)
                        )
                    }
                    
                    // Aşağı tuşu - ortada altta
                    Button(
                        onClick = { movePlayer(Direction.DOWN) },
                        modifier = Modifier
                            .size(70.dp)
                            .align(Alignment.BottomCenter)
                            .offset(y = (-20).dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFBBDEFB),
                            contentColor = Color(0xFF2196F3)
                        ),
                        shape = CircleShape
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_down),
                            contentDescription = "Aşağı",
                            modifier = Modifier.size(36.dp),
                            tint = Color(0xFF2196F3)
                        )
                    }
                }
            }
            
            // Oyun sonu ekranı
            if (state.gameCompleted || state.gameOver) {
                GameOverDialog(
                    isCompleted = state.gameCompleted,
                    score = state.score,
                    stars = state.stars,
                    onRestartGame = { restartGame() },
                    onMainMenu = { finish() }
                )
            }
            
            // Soru ekranı
            if (showingQuestion && currentQ != null) {
                var selectedOption by remember { mutableStateOf<String?>(null) }
                var showResult by remember { mutableStateOf(false) }
                
                LaunchedEffect(currentQ) {
                    // Soru değiştiğinde seçimi sıfırla
                    selectedOption = null
                    showResult = false
                }
                
                Dialog(onDismissRequest = {}) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Matematik Sorusu",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // SORU METNİNİ DAHA BELİRGİN GÖSTER
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFE3F2FD)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = currentQ!!.question ?: "Matematik Sorusu",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 16.dp),
                                    color = Color(0xFF1565C0)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Seçenekler
                            // Güvenlik kontrolleri: options listesi ve correctAnswerIndex kontrolü yapılır
                            val safeOptions = if (currentQ!!.options.isNullOrEmpty()) {
                                // Eğer seçenekler boşsa varsayılan seçenekler oluştur
                                listOf("1", "2", "3", "4")
                            } else {
                                currentQ!!.options
                            }
                            
                            // Doğru cevap indeksinin geçerli olduğundan emin ol
                            val safeCorrectIndex = if (currentQ!!.correctAnswerIndex >= 0 && currentQ!!.correctAnswerIndex < safeOptions.size) {
                                currentQ!!.correctAnswerIndex
                            } else {
                                0 // Varsayılan olarak ilk seçeneği doğru kabul et
                            }
                            
                            safeOptions.forEachIndexed { index, option ->
                                val isSelected = selectedOption == option
                                val isCorrectAnswer = index == safeCorrectIndex
                                
                                val backgroundColor = when {
                                    !showResult -> if (isSelected) Color(0xFFE3F2FD) else Color(0xFFEEEEEE)
                                    isCorrectAnswer -> Color(0xFFE8F5E9) // Doğru cevap için hafif yeşil
                                    isSelected -> Color(0xFFFFEBEE) // Yanlış seçilen için hafif kırmızı
                                    else -> Color(0xFFEEEEEE)
                                }
                                
                                val textColor = when {
                                    !showResult -> Color.Black
                                    isCorrectAnswer -> Color(0xFF4CAF50) // Doğru cevap için yeşil
                                    isSelected -> Color(0xFFE53935) // Yanlış seçilen için kırmızı
                                    else -> Color.Black
                                }
                                
                                val borderColor = when {
                                    !showResult -> if (isSelected) Color(0xFF2196F3) else Color.Transparent
                                    isCorrectAnswer -> Color(0xFF4CAF50) // Doğru cevap için yeşil sınır
                                    isSelected -> Color(0xFFE53935) // Yanlış seçilen için kırmızı sınır
                                    else -> Color.Transparent
                                }
                                
                                Button(
                                    onClick = { 
                                        if (!showResult) {
                                            selectedOption = option
                                            showResult = true
                                            
                                            // Kullanıcının cevabını işle - kısa bir gecikme ile
                                            Handler(Looper.getMainLooper()).postDelayed({
                                                onAnswerSelected(option)
                                            }, 1000) // 1 saniye sonra cevabı işle
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .border(
                                            width = 2.dp,
                                            color = borderColor,
                                            shape = RoundedCornerShape(8.dp)
                                        ),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = backgroundColor,
                                        contentColor = textColor
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${('A' + index)}: $option", 
                                        fontSize = 18.sp,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }
                            
                            // Ardışık yanlış cevap sayısı göstergesi (test için)
                            if (consecutiveWrongAnswers > 0) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Kalan hak: ${3 - consecutiveWrongAnswers}",
                                    fontSize = 14.sp,
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
    fun GameOverDialog(
        isCompleted: Boolean,
        score: Int,
        stars: Int,
        onRestartGame: () -> Unit,
        onMainMenu: () -> Unit
    ) {
        Dialog(onDismissRequest = {}) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(
                        elevation = 12.dp,
                        spotColor = Color.Black.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Tebrik ikonu veya üzgün yüz ikonu
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = if (isCompleted) {
                                        listOf(
                                            Color(0xFF4CAF50), // Yeşil
                                            Color(0xFF81C784)  // Açık yeşil
                                        )
                                    } else {
                                        listOf(
                                            Color(0xFFF44336), // Kırmızı
                                            Color(0xFFE57373)  // Açık kırmızı
                                        )
                                    }
                                )
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(
                                id = if (isCompleted) R.drawable.ic_check else R.drawable.ic_close
                            ),
                            contentDescription = if (isCompleted) "Başarı" else "Başarısız",
                            modifier = Modifier.size(48.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Başlık
                    Text(
                        text = if (isCompleted) "TEBRİKLER!" else "TEKRAR DENE!",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isCompleted) Color(0xFF2E7D32) else Color(0xFFC62828),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Mesaj
                    Text(
                        text = if (isCompleted) 
                            "Matematik labirentini başarıyla tamamladın!\nMüthiş bir iş çıkardın!" 
                        else 
                            "Bu sefer olmadı, ama vazgeçme!\nYeniden deneyerek daha da iyi olacaksın!",
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF424242),
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Puan kartı - tasarımı geliştir
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCompleted) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_score),
                                    contentDescription = "Skor",
                                    tint = if (isCompleted) Color(0xFF4CAF50) else Color(0xFF9E9E9E),
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Text(
                                    text = "PUANIN",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFF757575)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "$score",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFF757575)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Yıldızlar - daha çekici hale getir
                    Text(
                        text = "KAZANILAN YILDIZLAR",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color(0xFF5D4037) else Color(0xFF757575)
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(3) { i ->
                            // Animasyon için skala değeri
                            val scale by animateFloatAsState(
                                targetValue = if (i < stars) 1f else 0.7f,
                                animationSpec = tween(durationMillis = 300, delayMillis = i * 150),
                                label = "starScale"
                            )
                            
                            Icon(
                                painter = painterResource(id = R.drawable.ic_star),
                                contentDescription = "Yıldız",
                                tint = if (i < stars) Color(0xFFFFD700) else Color(0xFFE0E0E0),
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(horizontal = 4.dp)
                                    .scale(scale)
                            )
                        }
                    }
                    
                    // Yıldız kazanma koşullarını açıklayan bilgi kutusu (daha kompakt hale getirdim)
                    if (isCompleted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFFFFDE7) // Hafif sarı arka plan
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = "YILDIZ KAZANMA KOŞULLARI",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF5D4037),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(6.dp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_star),
                                        contentDescription = "1 Yıldız",
                                        tint = Color(0xFFFFD700),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Labirenti zamanında bitir",
                                        fontSize = 10.sp,
                                        color = Color(0xFF5D4037)
                                    )
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    repeat(2) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_star),
                                            contentDescription = "2 Yıldız",
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sürenin %40'ını kullanmadan bitir",
                                        fontSize = 10.sp,
                                        color = Color(0xFF5D4037)
                                    )
                                }
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 2.dp)
                                ) {
                                    repeat(3) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_star),
                                            contentDescription = "3 Yıldız",
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Sürenin %70'ini kullanmadan bitir",
                                        fontSize = 10.sp,
                                        color = Color(0xFF5D4037)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Butonlar - tamamen yeni tasarım, yan yana yerleşim
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // TEKRAR OYNA butonu
                        Button(
                            onClick = { onRestartGame() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCompleted) Color(0xFF4CAF50) else Color(0xFF2196F3)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .padding(horizontal = 1.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 2.dp
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_replay),
                                    contentDescription = "Tekrar",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "TEKRAR OYNA",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 0.3.sp,
                                    maxLines = 1
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // ANA MENÜ butonu
                        Button(
                            onClick = { onMainMenu() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF9E9E9E)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp)
                                .padding(horizontal = 1.dp),
                            shape = RoundedCornerShape(16.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 6.dp,
                                pressedElevation = 2.dp
                            )
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_home),
                                    contentDescription = "Ana Menü",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "ANA MENÜ",
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    letterSpacing = 0.3.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    override fun onPause() {
        super.onPause()
        gameTimerTask?.let { handler.removeCallbacks(it) }
        audioService.pauseBackgroundMusic()
    }
    
    override fun onResume() {
        super.onResume()
        if (!gameState.gameOver && !gameState.gameCompleted) {
            gameTimerTask?.let {
                handler.removeCallbacks(it)
                handler.postDelayed(it, 1000)
            }
        }
        // Arka plan müziğini sıfırdan başlat
        audioService.playBackgroundMusic()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Ses efektlerini temizle
        soundCorrect?.release()
        soundWrong?.release()
        soundComplete?.release()
        
        soundCorrect = null
        soundWrong = null
        soundComplete = null
        
        // Zamanlayıcıyı durdur
        gameTimerTask?.let { handler.removeCallbacks(it) }
        
        // AdManager kaynaklarını temizle
        try {
            if (::adManager.isInitialized) {
                adManager.destroy()
            }
        } catch (e: Exception) {
            Log.e("MathMazeGameActivity", "AdManager temizlenirken hata: ${e.message}")
        }
    }

    @Composable
    fun MazeGrid(maze: Array<Array<MazeCell>>, playerPosition: Pair<Int, Int>) {
        if (maze.isEmpty() || maze[0].isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "Labirent yüklenemedi",
                    fontSize = 18.sp,
                    color = Color.Red
                )
            }
            return
        }
        
        // Labirent grid'ini oluştur
        val rows = maze.size
        val cols = maze[0].size
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .shadow(4.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD), // Açık mavi merkez
                            Color(0xFFBBDEFB)  // Kenarlar biraz daha koyu
                        )
                    )
                )
                .padding(4.dp)
        ) {
            // Labirent hücrelerini çizme
            LazyVerticalGrid(
                columns = GridCells.Fixed(cols),
                modifier = Modifier.fillMaxSize()
            ) {
                items(rows * cols) { index ->
                    val row = index / cols
                    val col = index % cols
                    val cell = maze[row][col]
                    val isPlayerHere = row == playerPosition.first && col == playerPosition.second
                    
                    MazeCell(
                        cell = cell,
                        isPlayer = isPlayerHere,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                    )
                }
            }
        }
    }
    
    @Composable
    fun MazeCell(cell: MazeCell, isPlayer: Boolean, modifier: Modifier = Modifier) {
        // Renk paleti - daha çocuk dostu ve görsel açıdan ayırt edilebilir
        val wallColor = Color(0xFFB3E5FC) // Açık mavi - duvarlar için
        val pathColor = Color.White       // Yollar için beyaz
        val playerColor = Color(0xFF4CAF50) // Oyuncu yeşil
        val startColor = Color(0xFF2196F3)  // Başlangıç mavi
        val endColor = Color(0xFFFF9800)    // Bitiş turuncu
        val questionColor = Color(0xFFE91E63) // Soru pembe
        val visitedColor = Color(0xFFE0E0E0)  // Ziyaret edilmiş gri
        
        // Hücrenin rengi
        val cellColor = when {
            isPlayer -> playerColor
            cell.type == MazeCellType.START -> startColor
            cell.type == MazeCellType.END -> endColor
            cell.type == MazeCellType.QUESTION -> questionColor
            cell.type == MazeCellType.WALL -> wallColor
            cell.visited -> visitedColor
            else -> pathColor
        }
        
        // Hücrenin gölgesi (duvarlar için yok, diğer hücreler için var)
        val elevation = if (cell.type == MazeCellType.WALL) 0.dp else 2.dp
        
        Box(
            contentAlignment = Alignment.Center,
            modifier = modifier
                .shadow(
                    elevation = elevation,
                    shape = RoundedCornerShape(4.dp)
                )
                .clip(RoundedCornerShape(4.dp))
                .background(cellColor)
                .then(
                    if (cell.type == MazeCellType.WALL) {
                        Modifier.border(
                            width = 0.5.dp,
                            color = Color(0xFF90CAF9), // Duvar sınırları için daha koyu mavi
                            shape = RoundedCornerShape(4.dp)
                        )
                    } else {
                        Modifier
                    }
                )
        ) {
            when {
                isPlayer -> {
                    // Oyuncu ikonu
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = "Oyuncu",
                            tint = playerColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                cell.type == MazeCellType.START -> {
                    Text(
                        text = "B",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                cell.type == MazeCellType.END -> {
                    Text(
                        text = "S",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                cell.type == MazeCellType.QUESTION -> {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "?",
                            color = questionColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    /**
     * Oyuncuyu belirtilen yönde hareket ettirir.
     * Yeni pozisyonun geçerli ve duvarsız olduğunu kontrol eder.
     */
    private fun movePlayer(direction: Direction) {
        try {
            val maze = gameState.maze
            if (maze.isEmpty()) return
            
            val (row, col) = gameState.playerPosition
            var newRow = row
            var newCol = col
            
            // Yönlere göre hesapla
            when (direction) {
                Direction.UP -> newRow--
                Direction.DOWN -> newRow++
                Direction.LEFT -> newCol--
                Direction.RIGHT -> newCol++
            }
            
            // Debug için log
            // Log.d("MathMazeGame", "Move: $direction, Current: ($row,$col), New: ($newRow,$newCol)")
            
            // Sınırları ve duvarları kontrol et
            if (newRow < 0 || newRow >= maze.size || newCol < 0 || newCol >= maze[0].size) {
                // Log.d("MathMazeGame", "Sınırlar dışında, hareketi yoksayıldı")
                return // Sınırlar dışında, hareketi yoksay
            }
            
            val newCell = maze[newRow][newCol]
            
            // Duvar mı kontrol et
            if (newCell.type == MazeCellType.WALL) {
                vibratePhone(50) // Duvar geri bildirimi için titreşim
                // Log.d("MathMazeGame", "Duvar, hareket yoksayıldı")
                return
            }
            
            // Bitiş noktasında mı kontrol et - bitiş noktası kontrolünü öne çekerek ilk sıraya alıyoruz
            if (newCell.type == MazeCellType.END) {
                // Tüm soru hücrelerinin ziyaret edilip edilmediğini kontrol et
                var totalQuestionCells = 0
                var visitedQuestionCells = 0
                var unreachableQuestionCells = 0
                
                for (r in maze.indices) {
                    for (c in maze[0].indices) {
                        if (maze[r][c].type == MazeCellType.QUESTION) {
                            totalQuestionCells++
                            if (maze[r][c].visited) {
                                visitedQuestionCells++
                            } else {
                                // Hücrenin erişilebilir olup olmadığını kontrol et
                                val isReachable = isQuestionCellReachable(maze, newRow, newCol, r, c)
                                if (!isReachable) {
                                    unreachableQuestionCells++
                                    // Erişilemeyen soru hücresini log et
                                    android.util.Log.d("MathMazeGame", "Erişilemeyen soru hücresi tespit edildi: ($r,$c)")
                                }
                            }
                        }
                    }
                }
                
                // Tüm erişilebilir sorular cevaplandı mı?
                if (visitedQuestionCells >= (totalQuestionCells - unreachableQuestionCells)) {
                    // Tüm erişilebilir sorular cevaplandıysa oyunu başarıyla bitir
                    endGame(true)
                    // Logla
                    android.util.Log.d(
                        "MathMazeGame", 
                        "Bitiş noktası: Oyun tamamlandı. Ziyaret edilen sorular: $visitedQuestionCells/$totalQuestionCells (Erişilemeyen: $unreachableQuestionCells)"
                    )
                } else {
                    // Tüm erişilebilir sorular cevaplanmadıysa uyarı ver ve hareketi engelle
                    Toast.makeText(
                        this, 
                        "Labirentten çıkmadan önce erişilebilir tüm soruları cevaplamalısın! ($visitedQuestionCells/${totalQuestionCells - unreachableQuestionCells})", 
                        Toast.LENGTH_SHORT
                    ).show()
                    vibratePhone(100)
                    // Logla
                    android.util.Log.d(
                        "MathMazeGame", 
                        "Bitiş noktası: Tüm sorular cevaplanmadı. Ziyaret edilen sorular: $visitedQuestionCells/$totalQuestionCells (Erişilemeyen: $unreachableQuestionCells)"
                    )
                }
                // Hareket işlemi burada sonlandırılıyor, bitiş noktasına geçilmiyor
                return
            }
            
            // İlerlemeyi işle
            pendingDirection = direction
            
            // Soru hücresi mi kontrol et
            if (newCell.type == MazeCellType.QUESTION && !newCell.visited) {
                showQuestion = true
                _showingQuestion.value = true
                
                // Soru seçimini daha güvenli hale getir
                if (gameState.questions.isNotEmpty()) {
                    val questionIndex = (Math.random() * gameState.questions.size).toInt().coerceIn(0, gameState.questions.size - 1)
                    currentQuestion = gameState.questions[questionIndex]
                    
                    // Sorunun geçerli olduğundan emin ol
                    if (currentQuestion?.options.isNullOrEmpty() || currentQuestion?.correctAnswerIndex == null || 
                        currentQuestion!!.correctAnswerIndex < 0 || currentQuestion!!.correctAnswerIndex >= (currentQuestion?.options?.size ?: 0)) {
                        
                        // Soru verilerini düzelt
                        val fixedOptions = listOf("1", "2", "3", "4")
                        val fixedCorrectIndex = 0
                        
                        // Soru metnini al veya varsayılan oluştur
                        val questionText = currentQuestion?.question ?: "Matematik Sorusu"
                        
                        // Düzeltilmiş soru oluştur
                        currentQuestion = MathQuestion(
                            id = currentQuestion?.id ?: 0,
                            question = questionText,
                            options = fixedOptions,
                            correctAnswerIndex = fixedCorrectIndex
                        )
                        
                        Log.d("MathMazeGame", "Hatalı soru formatı düzeltildi: $questionText")
                    }
                    
                    _currentQ.value = currentQuestion
                } else {
                    // Soru listesi boşsa basit bir soru oluştur
                    currentQuestion = MathQuestion(
                        id = 0,
                        question = "Matematik Sorusu: 1 + 1 = ?",
                        options = listOf("2", "3", "4", "5"),
                        correctAnswerIndex = 0
                    )
                    _currentQ.value = currentQuestion
                    
                    Log.d("MathMazeGame", "Soru listesi boş, basit soru oluşturuldu")
                }
                
                return
            }
            
            // Hareketi tamamla
            completeMove(newRow, newCol)
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Hareket işlenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Hareketin tamamlanmasını işler, oyuncu pozisyonunu günceller.
     */
    private fun completeMove(newRow: Int, newCol: Int) {
        // Mevcut hücreyi ziyaret edilmiş olarak işaretle
        val maze = gameState.maze
        
        // Ziyaret edilecek hücre
        val targetCell = maze[newRow][newCol]
        
        // Soru hücresi ise ve daha önce ziyaret edilmediyse işaretle
        if (targetCell.type == MazeCellType.QUESTION && !targetCell.visited) {
            // Soru hücresi olarak işaretliyoruz - ziyaret durumu soru cevaplanınca değiştirilecek
        } else {
            // Soru hücresi değilse hemen ziyaret edilmiş olarak işaretle
            targetCell.visited = true
        }
        
        // Yeni pozisyonu güncelle
        gameState = gameState.copy(
            playerPosition = Pair(newRow, newCol)
        )
        
        // State'i güncelle
        _state.value = gameState
        
        // Tüm soru hücrelerini kontrol et
        var totalQuestionCells = 0
        var visitedQuestionCells = 0
        var unreachableQuestionCells = 0
        
        for (r in maze.indices) {
            for (c in maze[0].indices) {
                if (maze[r][c].type == MazeCellType.QUESTION) {
                    totalQuestionCells++
                    if (maze[r][c].visited) {
                        visitedQuestionCells++
                    } else {
                        // Hücrenin erişilebilir olup olmadığını kontrol et
                        val isReachable = isQuestionCellReachable(maze, newRow, newCol, r, c)
                        if (!isReachable) {
                            unreachableQuestionCells++
                            // Erişilemeyen soru hücresini log et
                            android.util.Log.d("MathMazeGame", "Erişilemeyen soru hücresi tespit edildi: ($r,$c)")
                        }
                    }
                }
            }
        }
        
        // Soru sayısı güncellemesini doğrudan ziyaret edilen soru hücrelerine göre yap
        // Bu, answeredQuestions değişkeni ile hücrelerin visited durumunu eşitler
        answeredQuestions = visitedQuestionCells
        requiredQuestionCount = totalQuestionCells
        
        // Tüm erişilebilir sorular tamamlandı mı kontrol et
        if (visitedQuestionCells >= (totalQuestionCells - unreachableQuestionCells)) {
            // Tüm erişilebilir sorular çözüldüyse, oyuna devam etmesine izin ver
            if (!isGameComplete && unreachableQuestionCells > 0 && !allQuestionsCompletedToastShown) {
                Toast.makeText(
                    this, 
                    "Erişilebilir tüm sorular tamamlandı! Bitiş noktasına ulaşabilirsin.", 
                    Toast.LENGTH_SHORT
                ).show()
                allQuestionsCompletedToastShown = true // Bildirimin tekrar gösterilmesini engelle
            } else if (visitedQuestionCells >= totalQuestionCells && !allQuestionsCompletedToastShown) {
                // UI'da uyarı göster (sadece bir kez gösteriliyor)
                if (!isGameComplete) {
                    Toast.makeText(
                        this, 
                        "Tüm sorular tamamlandı! Bitiş noktasına ulaşabilirsin.", 
                        Toast.LENGTH_SHORT
                    ).show()
                    allQuestionsCompletedToastShown = true // Bildirimin tekrar gösterilmesini engelle
                }
            }
        }
        
        // Logla
        android.util.Log.d("MathMazeGame", "Hareket tamamlandı: (${newRow},${newCol}) - Soruların durumu: $visitedQuestionCells/$totalQuestionCells (Erişilemeyen: $unreachableQuestionCells)")
    }
    
    /**
     * Belirli bir soru hücresinin mevcut konumdan erişilebilir olup olmadığını kontrol eder
     */
    private fun isQuestionCellReachable(maze: Array<Array<MazeCell>>, startRow: Int, startCol: Int, targetRow: Int, targetCol: Int): Boolean {
        val rows = maze.size
        val cols = maze[0].size
        val visited = Array(rows) { BooleanArray(cols) }
        val queue = java.util.LinkedList<Pair<Int, Int>>()
        
        // Başlangıç noktasını kuyruğa ekle
        queue.add(Pair(startRow, startCol))
        visited[startRow][startCol] = true
        
        // BFS ile hedef hücreye ulaşabilir miyiz kontrol et
        while (queue.isNotEmpty()) {
            val current = queue.poll()
            
            // Null güvenliği
            if (current == null) continue
            
            val row = current.first
            val col = current.second
            
            // Hedef hücreye ulaştık mı?
            if (row == targetRow && col == targetCol) {
                return true
            }
            
            // Tüm 4 yönü kontrol et
            val directions = arrayOf(
                Pair(0, 1),  // Sağ
                Pair(1, 0),  // Aşağı
                Pair(0, -1), // Sol
                Pair(-1, 0)  // Yukarı
            )
            
            for ((rowOffset, colOffset) in directions) {
                val newRow = row + rowOffset
                val newCol = col + colOffset
                
                // Sınırlar içinde mi?
                if (newRow in 0 until rows && newCol in 0 until cols) {
                    // Duvar değil ve daha önce ziyaret edilmemiş mi?
                    if (maze[newRow][newCol].type != MazeCellType.WALL && !visited[newRow][newCol]) {
                        queue.add(Pair(newRow, newCol))
                        visited[newRow][newCol] = true
                    }
                }
            }
        }
        
        // Hedef hücreye ulaşılamadı
        return false
    }
    
    /**
     * Oyuncunun seçtiği cevabı işler
     */
    private fun onAnswerSelected(answer: String) {
        try {
            if (currentQuestion == null || pendingDirection == null) return
            
            // Güvenlik kontrolü: options ve correctAnswerIndex kontrol edilir
            val isCorrect = if (currentQuestion!!.options.isNullOrEmpty() || 
                                currentQuestion!!.correctAnswerIndex < 0 || 
                                currentQuestion!!.correctAnswerIndex >= currentQuestion!!.options.size) {
                // Hatalı durum, varsayılan olarak doğru kabul et
                Log.e("MathMazeGame", "Hatalı soru formatı: options=${currentQuestion!!.options}, index=${currentQuestion!!.correctAnswerIndex}")
                true // Hatalı durumda doğru kabul et, oyunu bozmamak için
            } else {
                // Normal durum
                answer == currentQuestion!!.options[currentQuestion!!.correctAnswerIndex]
            }
            
            showFeedback = true
            isCorrectAnswer = isCorrect
            
            if (isCorrect) {
                // Doğru cevap
                audioService.playCorrectSound()
                
                // Oyuncunun bulunduğu hücreyi al
                val (row, col) = gameState.playerPosition
                val direction = pendingDirection!!
                
                // Yeni pozisyonu hesapla (hareket edilecek hücre)
                val (newRow, newCol) = when (direction) {
                    Direction.UP -> Pair(row - 1, col)
                    Direction.DOWN -> Pair(row + 1, col)
                    Direction.LEFT -> Pair(row, col - 1)
                    Direction.RIGHT -> Pair(row, col + 1)
                    else -> gameState.playerPosition
                }
                
                // Hareket edilecek hücreyi al
                val targetCell = gameState.maze[newRow][newCol]
                
                // Soru hücresini işaretle (çok önemli!)
                if (targetCell.type == MazeCellType.QUESTION && !targetCell.visited) {
                    // Bu hücredeki soruyu çözdük, hücreyi ziyaret edilmiş olarak işaretle
                    targetCell.visited = true
                    
                    // Soru sayısını artır (benzersiz soru içeriği yerine ziyaret edilen hücreleri sayıyoruz)
                    answeredQuestions++
                    
                    // Geri geri bildirim ekle
                    Toast.makeText(
                        this, 
                        "Doğru cevap! Çözülen soru: $answeredQuestions / $requiredQuestionCount", 
                        Toast.LENGTH_SHORT
                    ).show()
                    
                    // UI'ı güncelle
                    refreshUI()
                }
                
                // Puanı güncelle
                gameState = gameState.copy(
                    score = gameState.score + 10
                )
                
                // State'i güncelle
                _state.value = gameState
                
                // Ardışık yanlış cevap sayısını sıfırla
                consecutiveWrongAnswers = 0
                
                // Soru penceresini kapat
                showQuestion = false
                _showingQuestion.value = false
                currentQuestion = null
                _currentQ.value = null
                
                // Yön bilgisini sakla ve sıfırla
                val savedDirection = pendingDirection
                pendingDirection = null
                
                // Hareketi gerçekleştir
                Handler(Looper.getMainLooper()).postDelayed({
                    completeMove(newRow, newCol)
                    
                    // Buradaki Toast mesajını kaldırıyoruz çünkü completeMove içinde zaten gösteriliyor
                }, 800) // 0.8 saniye bekle
                
            } else {
                // Yanlış cevap
                audioService.playWrongSound()
                
                // Ardışık yanlış cevap sayısını artır
                consecutiveWrongAnswers++
                
                // Titreşim geribildirim
                vibrateDeviceError()
                
                // 3 ardışık yanlış cevap kontrolü
                if (consecutiveWrongAnswers >= 3) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        showQuestion = false
                        _showingQuestion.value = false
                        currentQuestion = null
                        _currentQ.value = null
                        pendingDirection = null
                        Toast.makeText(this, "Üç yanlış cevap verdin!", Toast.LENGTH_SHORT).show()
                        endGame(false)
                    }, 1000)
                } else {
                    // 3 ardışık yanlış cevap değilse, yeni bir soru göster
                    Handler(Looper.getMainLooper()).postDelayed({
                        // Mevcut soruyu temizle
                        showQuestion = false
                        _showingQuestion.value = false
                        
                        // Kısa bir süre bekleyip yeni soru göster
                        Handler(Looper.getMainLooper()).postDelayed({
                            // Yeni bir soru seç
                            val questionIndex = (Math.random() * gameState.questions.size).toInt()
                            currentQuestion = gameState.questions[questionIndex]
                            _currentQ.value = currentQuestion
                            
                            // Soru diyaloğunu göster
                            showQuestion = true
                            _showingQuestion.value = true
                            
                            Toast.makeText(this, "Tekrar dene! Kalan hak: ${3 - consecutiveWrongAnswers}", Toast.LENGTH_SHORT).show()
                        }, 500) // 0.5 saniye sonra yeni soru göster
                    }, 1500) // Yanlış cevap geribildirimini 1.5 saniye gösterdikten sonra
                }
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            
            // Hata durumunda varsayılan olarak ilerle
            showQuestion = false
            _showingQuestion.value = false
            currentQuestion = null
            _currentQ.value = null
            pendingDirection = null
            
            // Hata mesajı göster
            Toast.makeText(this, "Cevap işlenirken hata oluştu: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Telefonu titretir
     */
    private fun vibratePhone(duration: Long) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val vibrationEffect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(vibrationEffect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(duration)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * UI'yi yenilemek için yardımcı metod
     */
    private fun refreshUI() {
        runOnUiThread {
            // State'leri güncelle
            _state.value = gameState
            _showingQuestion.value = showQuestion
            _currentQ.value = currentQuestion
            
            // Log.d("MathMazeGame", "UI yenilendi - Player: ${gameState.playerPosition}, Time: ${gameState.timeRemaining}")
        }
    }

    /**
     * Oyunu yeniden başlatan fonksiyon
     */
    private fun restartGame() {
        try {
            // Durum değişkenlerini sıfırla
            showQuestion = false
            _showingQuestion.value = false
            currentQuestion = null
            _currentQ.value = null
            pendingDirection = null
            showFeedback = false
            isCorrectAnswer = false
            consecutiveWrongAnswers = 0
            answeredQuestions = 0
            _answeredQuestions.clear()
            isGameComplete = false
            
            // Bildirim bayrağını sıfırla
            allQuestionsCompletedToastShown = false
            
            // Tüm Toast mesajlarını iptal et
            cancelAllToasts()
            
            // Mevcut Handler'daki tüm işlemleri kaldır
            handler.removeCallbacksAndMessages(null)
            
            // Oyunu yeniden başlat
            initGame(selectedLevelIndex)
            
            // State'i güncelle
            _state.value = gameState
            
            // Zamanlayıcıyı durdur ve yeniden başlat
            gameTimerTask?.let { handler.removeCallbacks(it) }
            startGame()
            
            // Log.d("MathMazeGame", "Oyun yeniden başlatıldı: ${gameState.playerPosition}")
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Oyun yeniden başlatılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Tüm Toast mesajlarını iptal etmeye çalışır
     */
    private fun cancelAllToasts() {
        try {
            // Statik Toast alanına erişim sağla ve tüm Toast bildirimleri iptal etmeye çalış
            val toastFieldClass = Toast::class.java
            val tnField = toastFieldClass.getDeclaredField("sToast")
            tnField.isAccessible = true
            val toastObj = tnField.get(null) as? Toast
            toastObj?.cancel()
        } catch (e: Exception) {
            // Hata olursa sessizce yoksay
            android.util.Log.e("MathMazeGame", "Toast iptal edilirken hata: ${e.message}")
        }
    }

    @Composable
    fun GameTopBar(
        gameName: String,
        level: Int = 1,
        timeRemaining: Int,
        score: Int,
        totalTime: Int,
        onBackClick: () -> Unit
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Üst kısım - Geri Butonu ve Başlık
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF3F51B5))
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
                    text = gameName,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Alt kısım - Skor, süre ve ilerleme bilgileri
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Skor göstergesi
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Skor",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "Skor: $score",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                
                // Süre göstergesi
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_timer),
                        contentDescription = "Süre",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "$timeRemaining s",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
                
                // İlerleme göstergesi
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_question),
                        contentDescription = "Sorular",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = "$answeredQuestions/$requiredQuestionCount",
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }

    private fun vibrateDeviceError() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                val vibrator = vibratorManager.defaultVibrator
                val vibrationEffect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                @Suppress("DEPRECATION")
                val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val vibrationEffect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
                    vibrator.vibrate(vibrationEffect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(200)
                }
            }
        } catch (e: Exception) {
            Log.e("MathMazeGame", "Titreşim çalışırken hata: ${e.message}")
            e.printStackTrace()
        }
    }

    // Oyun tamamlandığında çağrılan fonksiyon
    private fun onGameCompleted(level: Int, timeRemaining: Int) {
        isGameComplete = true
        
        // Ses çal
        audioService.playSuccessSound()
        
        // Günlük görevleri güncelle
        if (dailyTaskId != -1) {
            dailyTaskManager.updateTaskProgress(dailyTaskId)
        }
        
        // Matematik problemleri sayısını artır (soruların sayısı = gameState.questions.size)
        dailyTaskManager.incrementMathProblemsCompleted(3) // Sabit bir değer kullanıyoruz
        
        // Oyun sayısını artır
        dailyTaskManager.incrementGamesPlayed()
        
        // Yıldız sayısını hesapla
        val stars = calculateStars(level, timeRemaining)
        
        // 2 veya daha fazla yıldızla tamamlandıysa
        if (stars >= 2) {
            dailyTaskManager.incrementGamesCompletedWithTwoOrMoreStars()
        }
        
        // Oyun sonuç diyaloğunu göster
        showGameCompletedDialog(level, stars, timeRemaining)
    }
    
    // Oyun tamamlandı diyaloğunu gösterir
    private fun showGameCompletedDialog(level: Int, stars: Int, timeRemaining: Int) {
        try {
            // Toplam kazanılan yıldız bilgisini al
            val starManager = StarManager.getInstance(this)
            val newTotalStars = starManager.getTotalStars()
            
            // Başarı mesajı göster (yıldız bilgisiyle birlikte)
            Toast.makeText(
                this,
                "Seviye $level tamamlandı! $stars yıldız kazandınız! Toplam yıldızınız: $newTotalStars",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            // Hata durumunda basit mesaj göster
            Toast.makeText(
                this,
                "Seviye $level tamamlandı! $stars yıldız kazandınız! Kalan süre: $timeRemaining",
                Toast.LENGTH_LONG
            ).show()
            Log.e("MathMazeGame", "Toplam yıldız bilgisi alınırken hata: ${e.message}", e)
        }
    }

    // Yıldız hesaplama fonksiyonu
    private fun calculateStars(level: Int, timeRemaining: Int): Int {
        val initialTimeSeconds = gameState.level.timeInSeconds
        
        // Kolay seviye için: Süre kaldıysa 3 yıldız, yoksa 2 yıldız
        if (level == 1) {
            return if (timeRemaining > 0) 3 else 2
        }
        // Orta seviye için: Sürenin %30'undan fazlası kaldıysa 3 yıldız, kaldıysa 2 yıldız, yoksa 1 yıldız
        else if (level == 2) {
            return when {
                timeRemaining > initialTimeSeconds * 0.3 -> 3
                timeRemaining > 0 -> 2
                else -> 1
            }
        }
        // Zor seviye için: Sürenin %40'ından fazlası kaldıysa 3 yıldız, %10'undan fazlası kaldıysa 2 yıldız, yoksa 1 yıldız
        else {
            return when {
                timeRemaining > initialTimeSeconds * 0.4 -> 3
                timeRemaining > initialTimeSeconds * 0.1 -> 2
                else -> 1
            }
        }
    }

    private fun showInterstitialAd() {
        // Geliştirilmiş agresif reklam stratejisi
        try {
            // Önce internet bağlantısı kontrolü yap
            if (!NetworkUtils.isInternetAvailable(this)) {
                Log.d("MathMazeGameActivity", "İnternet bağlantısı yok, reklam gösterilemiyor")
                NetworkUtils.showNoInternetToast(this)
                return
            }
            
            // AdManager'ı kontrol et ve yeni agresif reklam metodunu kullan
            if (::adManager.isInitialized) {
                Log.d("MathMazeGameActivity", "Oyun sonunda agresif reklam gösterilecek")
                adManager.showInterstitialAdAfterGame(this)
            } else {
                Log.w("MathMazeGameActivity", "AdManager başlatılmamış")
            }
        } catch (e: Exception) {
            Log.e("MathMazeGameActivity", "Reklam gösterimi sırasında hata: ${e.message}", e)
        }
    }

    /**
     * Günlük sınır dialogu
     */
    @Composable
    private fun DailyLimitDialog(
        onDismiss: () -> Unit,
        onUpgradeToPremium: () -> Unit,
        remainingPlays: Int,
        dailyLimit: Int
    ) {
        Dialog(onDismissRequest = onDismiss) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 8.dp
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // İkon
                    Icon(
                        painter = painterResource(id = R.drawable.ic_algorithm),
                        contentDescription = "Matematik Labirenti",
                        tint = Color(0xFF2196F3),
                        modifier = Modifier.size(64.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Başlık
                    Text(
                        text = "Günlük Oyun Sınırı",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2196F3),
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Açıklama
                    Text(
                        text = "Matematik Labirenti oyununu bugün $dailyLimit kez oynadınız. Premium üyelik ile sınırsız oynayabilirsiniz!",
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Premium avantajları
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF9C4)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Premium Avantajları:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7B6608)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✨ Sınırsız Matematik Labirenti\n🎯 Tüm avatarlar açık\n📊 Detaylı istatistikler\n🚫 Reklamsız deneyim",
                                fontSize = 13.sp,
                                color = Color(0xFF7B6608),
                                lineHeight = 18.sp
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // Butonlar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Gray
                            )
                        ) {
                            Text("Kapat")
                        }
                        
                        Button(
                            onClick = onUpgradeToPremium,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2196F3)
                            )
                        ) {
                            Text("Premium Al")
                        }
                    }
                }
            }
        }
    }
} 