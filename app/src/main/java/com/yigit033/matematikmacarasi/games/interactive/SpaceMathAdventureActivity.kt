package com.yigit033.matematikmacarasi.games.interactive

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.models.Asteroid
import com.yigit033.matematikmacarasi.games.models.Laser
import com.yigit033.matematikmacarasi.games.models.SpaceGameState
import com.yigit033.matematikmacarasi.games.models.SpaceLevels
import com.yigit033.matematikmacarasi.games.models.SpaceMathHelper
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.activities.BaseGameActivity
import com.yigit033.matematikmacarasi.services.DailyTaskManager
import com.yigit033.matematikmacarasi.services.PlayerProfileService
import androidx.compose.animation.AnimatedVisibility
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.yigit033.matematikmacarasi.services.payments.AdManager
import android.util.Log
import com.yigit033.matematikmacarasi.utils.NetworkUtils

// Özel easing fonksiyonu için cubic bezier
private val EaseOutBack = androidx.compose.animation.core.CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f)

/**
 * Uzay Matematik Macerası Oyunu Aktivitesi
 * Uzay gemisiyle meteoritler arasında ilerleyerek matematik sorularını çözme oyunu
 */
class SpaceMathAdventureActivity : ComponentActivity() {
    
    private var mediaPlayerCorrect: MediaPlayer? = null
    private var mediaPlayerWrong: MediaPlayer? = null
    private var mediaPlayerLaser: MediaPlayer? = null
    private var mediaPlayerExplosion: MediaPlayer? = null
    private var mediaPlayerSuccess: MediaPlayer? = null
    
    // AdManager ekleyelim
    private lateinit var adManager: AdManager
    
    private val handler = Handler(Looper.getMainLooper())
    private val gameTimer = object : Runnable {
        override fun run() {
            if (gameState.timeRemaining > 0 && !gameState.isGameOver && !gameState.isGameCompleted) {
                gameState = gameState.copy(timeRemaining = gameState.timeRemaining - 1)
                
                // Süre bitti mi kontrol et
                if (gameState.timeRemaining <= 0) {
                    endGame(completed = false)
                }
                
                handler.postDelayed(this, 1000)
            }
        }
    }
    
    // Oyun durumu
    private var gameState by mutableStateOf(createInitialGameState())
    private val activeLasers = mutableStateListOf<Laser>()
    private val asteroids = mutableStateListOf<Asteroid>()
    private var showFeedback by mutableStateOf(false)
    private var isCorrectAnswer by mutableStateOf(false)
    
    // Süre cezası gösterimi için state'ler
    private var showTimePenalty by mutableStateOf(false)
    private var timePenaltyValue by mutableStateOf(0)
    
    // Zorluk seviyesi seçim dialogu için state
    private var showLevelSelectDialog by mutableStateOf(true)
    private var selectedLevelIndex by mutableStateOf(0)
    
    // Performans için sabitlerimiz
    private val MAX_LASERS = 10
    private val MAX_ASTEROIDS = 15
    
    // Günlük görev takibi için
    private lateinit var dailyTaskManager: DailyTaskManager
    private var dailyTaskId: Int = -1
    
    // İstatistik servisi
    private lateinit var gameStatisticsService: com.yigit033.matematikmacarasi.services.GameStatisticsService
    private lateinit var gameStatisticsHelper: com.yigit033.matematikmacarasi.services.GameStatisticsHelper
    
    // Oyuncu profil servisi
    private lateinit var playerProfileService: PlayerProfileService
    
    // Oyun başlangıç zamanı
    private var gameStartTime: Long = 0
    
    // Oyunu bitiren metodda güncelleyelim
    private val gameType = GameType.SPACE_MATH_ADVENTURE
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // İnternet bağlantısı kontrolü
            if (!NetworkUtils.isInternetAvailable(this)) {
                Log.w("SpaceMathAdventureActivity", "İnternet bağlantısı yok, oyun başlatılamıyor")
                Toast.makeText(
                    this,
                    "Bu oyunu oynamak için internet bağlantısı gereklidir!",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return
            }
            
            // Ses dosyalarını yükle (güvenli başlatma)
            mediaPlayerCorrect = try { MediaPlayer.create(this, R.raw.correct_sound) } catch (e: Exception) { null }
            mediaPlayerWrong = try { MediaPlayer.create(this, R.raw.wrong_sound) } catch (e: Exception) { null }
            mediaPlayerLaser = try { MediaPlayer.create(this, R.raw.laser_sound) } catch (e: Exception) { null }
            mediaPlayerExplosion = try { MediaPlayer.create(this, R.raw.explosion_sound) } catch (e: Exception) { null }
            mediaPlayerSuccess = try { MediaPlayer.create(this, R.raw.success) } catch (e: Exception) { null }
            
            // Günlük görev yöneticisini başlat
            dailyTaskManager = DailyTaskManager.getInstance(this)
            
            // İstatistik servisini başlat
            gameStatisticsService = com.yigit033.matematikmacarasi.services.GameStatisticsService(this)
            gameStatisticsHelper = com.yigit033.matematikmacarasi.services.GameStatisticsHelper(this)
            
            // AdManager'ı başlat
            adManager = AdManager.getInstance(this)
            // Oyun başında reklamı hazırla
            adManager.loadInterstitialAd()
            
            // Oyuncu profil servisini başlat
            playerProfileService = PlayerProfileService(this)
            
            // Oyunun başlangıç zamanını kaydet
            gameStartTime = System.currentTimeMillis()
            
            // Intent'ten görev ID'sini al (varsa)
            dailyTaskId = intent.getIntExtra("daily_task_id", -1)
            
            // Oyuncunun yaşına göre varsayılan seviyeyi belirle
            val playerAge = playerProfileService.getPlayerAge()
            selectedLevelIndex = SpaceLevels.getLevelIndexForAge(playerAge)
            
            setContent {
                MatematikMacerasiTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (showLevelSelectDialog) {
                            LevelSelectDialog(
                                defaultLevel = selectedLevelIndex,
                                onLevelSelected = { levelIndex ->
                                    selectedLevelIndex = levelIndex
                                    showLevelSelectDialog = false
                                    
                                    // Seçilen seviyeye göre oyunu başlat
                                    gameState = createInitialGameState(levelIndex)
                                    
                                    // İlk asteroitleri oluştur
                                    val question = SpaceMathHelper.generateQuestion(gameState.level)
                                    gameState = gameState.copy(currentQuestion = question)
                                    val initialAsteroids = SpaceMathHelper.generateAsteroids(question, gameState.level)
                                    asteroids.clear()
                                    asteroids.addAll(initialAsteroids)
                                    
                                    // Oyunu başlat
                                    startGame()
                                },
                                onCancel = { finish() }
                            )
                        } else {
                            SpaceGameScreen()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Ses dosyaları yüklenemedi: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    @Composable
    fun LevelSelectDialog(
        defaultLevel: Int = 0,
        onLevelSelected: (Int) -> Unit,
        onCancel: () -> Unit
    ) {
        // Ekran arkaplanı - yıldızlı uzay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF000033),
                            Color(0xFF000066),
                            Color(0xFF000099)
                        )
                    )
                )
        ) {
            // Yıldızlar
            StarryBackground()
            
            // Gezegenler ve uzay nesneleri (arka plan süslemeleri)
            SpaceDecorations()
            
            // Dialog içeriği
            Dialog(onDismissRequest = { onCancel() }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF1A237E).copy(alpha = 0.95f)
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Başlık alanı
                        Text(
                            text = "Matematik Uzay Macerası",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFE8EAF6),
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        
                        // Uzay gemisi ikonu
                        Box(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF3F51B5),
                                            Color(0xFF1A237E)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_spaceship),
                                contentDescription = "Uzay Gemisi",
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(4.dp),
                                colorFilter = ColorFilter.tint(Color.White)
                            )
                        }
                        
                        Text(
                            text = "Seviye Seçimi",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFFC5CAE9),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        
                        // Seviye kartları
                        LevelCard(
                            title = "1. Seviye (6-8 Yaş)",
                            description = "Basit toplama işlemleri (1-10 arası)\n" +
                                    "• Sorular: ${SpaceLevels.levels[0].questionsToComplete} toplama sorusu\n" +
                                    "• Süre: ${SpaceLevels.levels[0].timeInSeconds} saniye\n" +
                                    "• Çok yavaş hareket eden meteoritler",
                            color = Color(0xFF4CAF50),
                            levelIcon = R.drawable.ic_planet_1,
                            isRecommended = defaultLevel == 0,
                            onClick = { onLevelSelected(0) }
                        )
                        
                        LevelCard(
                            title = "2. Seviye (6-8 Yaş)",
                            description = "Basit çıkarma işlemleri (1-15 arası)\n" +
                                    "• Sorular: ${SpaceLevels.levels[1].questionsToComplete} çıkarma sorusu\n" +
                                    "• Süre: ${SpaceLevels.levels[1].timeInSeconds} saniye\n" +
                                    "• Yavaş hareket eden meteoritler",
                            color = Color(0xFF4CAF50),
                            levelIcon = R.drawable.ic_planet_2,
                            isRecommended = defaultLevel == 1,
                            onClick = { onLevelSelected(1) }
                        )
                        
                        LevelCard(
                            title = "3. Seviye (7-10 Yaş)",
                            description = "Basit çarpma işlemleri (1-5 arası)\n" +
                                    "• Sorular: ${SpaceLevels.levels[2].questionsToComplete} çarpma sorusu\n" +
                                    "• Süre: ${SpaceLevels.levels[2].timeInSeconds} saniye\n" +
                                    "• Orta hızda meteoritler",
                            color = Color(0xFFFFA000),
                            levelIcon = R.drawable.ic_planet_3,
                            isRecommended = defaultLevel == 2,
                            onClick = { onLevelSelected(2) }
                        )
                        
                        LevelCard(
                            title = "4. Seviye (7-10 Yaş)",
                            description = "Basit bölme işlemleri (1-10 arası)\n" +
                                    "• Sorular: ${SpaceLevels.levels[3].questionsToComplete} bölme sorusu\n" +
                                    "• Süre: ${SpaceLevels.levels[3].timeInSeconds} saniye\n" +
                                    "• Hızlı hareket eden meteoritler",
                            color = Color(0xFFFFA000),
                            levelIcon = R.drawable.ic_planet_4,
                            isRecommended = defaultLevel == 3,
                            onClick = { onLevelSelected(3) }
                        )
                        
                        LevelCard(
                            title = "5. Seviye (6-10 Yaş)",
                            description = "Gelişmiş toplama ve çıkarma (1-50 arası)\n" +
                                    "• Sorular: ${SpaceLevels.levels[4].questionsToComplete} karışık soru\n" +
                                    "• Süre: ${SpaceLevels.levels[4].timeInSeconds} saniye\n" +
                                    "• Daha hızlı hareket eden meteoritler",
                            color = Color(0xFFFF5722),
                            levelIcon = R.drawable.ic_planet_5,
                            isRecommended = defaultLevel == 4,
                            onClick = { onLevelSelected(4) }
                        )
                        
                        LevelCard(
                            title = "6. Seviye (7-11 Yaş)",
                            description = "Çarpma (1-12) ve bölme (1-50) işlemleri\n" +
                                    "• Sorular: ${SpaceLevels.levels[5].questionsToComplete} karışık soru\n" +
                                    "• Süre: ${SpaceLevels.levels[5].timeInSeconds} saniye\n" +
                                    "• Çok hızlı hareket eden meteoritler",
                            color = Color(0xFFFF5722),
                            levelIcon = R.drawable.ic_planet_6,
                            isRecommended = defaultLevel == 5,
                            onClick = { onLevelSelected(5) }
                        )
                        
                        LevelCard(
                            title = "7. Seviye (9-12 Yaş)",
                            description = "Orta-zor seviye karışık işlemler\n" +
                                    "• Toplama/çıkarma (20-100 arası)\n" +
                                    "• Çarpma/bölme (1-30 arası)\n" +
                                    "• Sorular: ${SpaceLevels.levels[6].questionsToComplete} karışık soru\n" +
                                    "• Süre: ${SpaceLevels.levels[6].timeInSeconds} saniye",
                            color = Color(0xFF3F51B5),
                            levelIcon = R.drawable.ic_planet_7,
                            isRecommended = defaultLevel == 6,
                            onClick = { onLevelSelected(6) }
                        )
                        
                        LevelCard(
                            title = "8. Seviye (11+ Yaş)",
                            description = "Zor seviye karışık işlemler\n" +
                                    "• Toplama/çıkarma (50-200 arası)\n" +
                                    "• Çarpma (1-50) ve bölme (5-200) işlemleri\n" +
                                    "• Sorular: ${SpaceLevels.levels[7].questionsToComplete} karışık soru\n" +
                                    "• Süre: ${SpaceLevels.levels[7].timeInSeconds} saniye",
                            color = Color(0xFF3F51B5),
                            levelIcon = R.drawable.ic_planet_8,
                            isRecommended = defaultLevel == 7,
                            onClick = { onLevelSelected(7) }
                        )
                        
                        // Çıkış butonu
                        Button(
                            onClick = { onCancel() },
                            modifier = Modifier
                                .padding(top = 16.dp, bottom = 8.dp)
                                .fillMaxWidth(0.6f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A237E)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF5C6BC0))
                        ) {
                            Text(
                                text = "GERİ DÖN",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    private fun SpaceDecorations() {
        // Animasyon için Infinite Transition
        val infiniteTransition = rememberInfiniteTransition(label = "spaceAnim")
        
        // Gezegenler için yavaşça dönen animasyon
        val rotation by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(30000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "planetRotation"
        )
        
        // Farklı pozisyonlarda gezegen ve yıldız kümeleri
        Box(modifier = Modifier.fillMaxSize()) {
            // Gezegen 1 (Kırmızı gezegen - sağ üst köşe)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .offset(x = 250.dp, y = 50.dp)
                    .rotate(rotation)
                    .alpha(0.6f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFFE57373),
                        radius = 40.dp.toPx(),
                        center = Offset(size.width / 2, size.height / 2)
                    )
                }
            }
            
            // Gezegen 2 (Mavi gezegen - sol alt köşe)
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .offset(x = (-20).dp, y = 500.dp)
                    .rotate(rotation * 0.7f)
                    .alpha(0.5f)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color(0xFF64B5F6),
                        radius = 30.dp.toPx(),
                        center = Offset(size.width / 2, size.height / 2)
                    )
                }
            }
            
            // Yıldız kümesi 1
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(0.7f)
            ) {
                for (i in 0..20) {
                    val x = Random.nextFloat() * size.width
                    val y = Random.nextFloat() * size.height
                    drawCircle(
                        color = Color.White,
                        radius = (1 + Random.nextFloat() * 2).dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
    
    @Composable
    private fun LevelCard(
        title: String,
        description: String,
        color: Color,
        levelIcon: Int,
        isRecommended: Boolean,
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
                .padding(vertical = 8.dp)
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
                containerColor = Color(0xFF303F9F)
            ),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(
                width = 1.dp,
                color = color.copy(alpha = 0.7f)
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Seviye ikonu
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
                            )
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = levelIcon),
                            contentDescription = title,
                            modifier = Modifier.size(32.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Seviye başlığı
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Açıklama
                Text(
                    text = description,
                    fontSize = 14.sp,
                    color = Color(0xFFE8EAF6),
                    lineHeight = 18.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Başla butonu - Gradyan renkli
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
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "BAŞLA",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Roket ikonu
                        Icon(
                            painter = painterResource(id = R.drawable.ic_spaceship),
                            contentDescription = "Başla",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
    
    private fun startGame() {
        // Zamanlayıcıyı başlat
        handler.postDelayed(gameTimer, 1000)
    }
    
    private fun createInitialGameState(levelIndex: Int = 0): SpaceGameState {
        val level = if (levelIndex < SpaceLevels.levels.size) {
            SpaceLevels.levels[levelIndex]
        } else {
            SpaceLevels.levels[0] // Varsayılan ilk seviye
        }
        return SpaceGameState(
            level = level,
            timeRemaining = level.timeInSeconds
        )
    }
    
    @Composable
    fun SpaceGameScreen() {
        // Oyun döngüsü - her 50ms'de bir güncelleme
        LaunchedEffect(Unit) {
            while (true) {
                if (!gameState.isGameOver && !gameState.isGameCompleted) {
                    updateGame()
                }
                delay(50) // ~20 FPS (yeterli ve performanslı)
            }
        }
        
        // Uzay arka planı
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF000033),
                            Color(0xFF000066),
                            Color(0xFF000099)
                        )
                    )
                )
        ) {
            // Yıldızlar
            StarryBackground()
            
            // Oyun ekranı
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Üst bilgi çubuğu - Süre cezası gösterimi için özelleştirildi
                Box(modifier = Modifier.fillMaxWidth()) {
                    GameTopBar(
                        gameName = "Matematik Uzay Macerası",
                        level = gameState.level.level,
                        timeRemaining = gameState.timeRemaining,
                        score = gameState.score,
                        totalTime = gameState.level.timeInSeconds,
                        onBackClick = { finish() }
                    )
                    
                    // Süre cezası gösterimi
                    if (showTimePenalty) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 32.dp, end = 36.dp)
                                .background(
                                    color = Color(0xFFE53935),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "-$timePenaltyValue",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Soru ekranı
                QuestionBox(question = gameState.currentQuestion?.question ?: "")
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Oyun alanı
                GameArea(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Ateş butonu
                FireButton(
                    onShoot = { shoot() },
                    enabled = !gameState.isGameOver && !gameState.isGameCompleted
                )
            }
            
            // Oyun sonu ekranı
            if (gameState.isGameOver || gameState.isGameCompleted) {
                GameOverDialog(
                    isCompleted = gameState.isGameCompleted,
                    score = gameState.score,
                    stars = gameState.stars,
                    onRestart = { restartGame() },
                    onNextLevel = { startNextLevel() },
                    onHome = { finish() }
                )
            }
            
            // Doğru/yanlış geri bildirimi
            if (showFeedback) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCorrectAnswer) "Doğru!" else "Yanlış!",
                        color = if (isCorrectAnswer) Color.Green else Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 48.sp
                    )
                }
            }
        }
    }
    
    @Composable
    fun StarryBackground() {
        val stars = remember {
            List(100) {
                Triple(
                    Random.nextFloat(), // x
                    Random.nextFloat(), // y
                    Random.nextFloat() * 2 + 1 // size (1-3dp)
                )
            }
        }
        
        val infiniteTransition = rememberInfiniteTransition(label = "starTwinkle")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "starAlpha"
        )
        
        Canvas(modifier = Modifier.fillMaxSize()) {
            stars.forEach { (x, y, size) ->
                drawCircle(
                    color = Color.White.copy(alpha = alpha * (0.5f + Random.nextFloat() * 0.5f)),
                    radius = size.dp.toPx(),
                    center = Offset(x * this.size.width, y * this.size.height)
                )
            }
        }
    }
    
    @Composable
    fun QuestionBox(question: String) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF003366)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = question,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = Color.White
            )
        }
    }
    
    @Composable
    fun GameArea(modifier: Modifier = Modifier) {
        // Ekran boyutlarını al
        val configuration = LocalConfiguration.current
        val density = LocalDensity.current
        
        // Uzay gemisi pozisyonu
        var spaceshipX by remember { mutableStateOf(gameState.spaceship.x) }
        var spaceshipY by remember { mutableStateOf(gameState.spaceship.y) }
        
        // Pozisyonu yumuşak bir şekilde güncellemek için animasyonlu değerler
        val animatedSpaceshipX by animateFloatAsState(
            targetValue = gameState.spaceship.x,
            animationSpec = tween(200),
            label = "spaceshipXAnimation"
        )
        
        val animatedSpaceshipY by animateFloatAsState(
            targetValue = gameState.spaceship.y,
            animationSpec = tween(200),
            label = "spaceshipYAnimation"
        )
        
        // Hareket etmekte olup olmadığını takip et
        var isDragging by remember { mutableStateOf(false) }
        
        Box(
            modifier = modifier
                .background(Color.Transparent)
                // Uzay gemisini hareket ettirme - titremeleri önlemek için iyileştirildi
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { 
                            isDragging = true 
                        },
                        onDragEnd = { 
                            isDragging = false 
                            // Sürükleme bittiğinde son pozisyonu güncelle
                            gameState = gameState.copy(
                                spaceship = gameState.spaceship.copy(
                                    x = spaceshipX,
                                    y = spaceshipY
                                )
                            )
                        },
                        onDragCancel = {
                            isDragging = false
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            
                            // Ekran boyutuna göre hesaplama
                            val screenWidth = configuration.screenWidthDp.toFloat()
                            val screenHeight = configuration.screenHeightDp.toFloat()
                            
                            // Daha hassas ve düzgün hareket için doğrudan piksel değişikliğini kullan
                            val deltaX = dragAmount.x / density.density / screenWidth * 1.5f
                            val deltaY = dragAmount.y / density.density / screenHeight * 1.5f
                            
                            // Yeni pozisyonu hesapla ve sınırları kontrol et
                            spaceshipX = (spaceshipX + deltaX).coerceIn(0.05f, 0.95f)
                            spaceshipY = (spaceshipY + deltaY).coerceIn(0.3f, 0.8f)  // Hareket sınırlarını güncelledik (0.4f, 0.95f'den 0.3f, 0.8f'ye)
                            
                            // Uzay gemisinin pozisyonunu hemen güncelle (sürükleme sırasında)
                            gameState = gameState.copy(
                                spaceship = gameState.spaceship.copy(
                                    x = spaceshipX,
                                    y = spaceshipY
                                )
                            )
                        }
                    )
                }
        ) {
            // Uzay gemisi - animasyonlu pozisyon kullan
            Spaceship(
                x = if (isDragging) spaceshipX else animatedSpaceshipX,
                y = if (isDragging) spaceshipY else animatedSpaceshipY
            )
            
            // Lazerler
            activeLasers.forEach { laser ->
                LaserView(x = laser.x, y = laser.y)
            }
            
            // Asteroidler
            asteroids.forEach { asteroid ->
                AsteroidView(asteroid = asteroid)
            }
        }
    }
    
    @Composable
    fun Spaceship(x: Float, y: Float) {
        val configuration = LocalConfiguration.current
        val width = configuration.screenWidthDp
        val height = configuration.screenHeightDp // Doğru ekran yüksekliğini kullanalım

        // Uzay gemisi boyutu - ekran genişliğinin %12'si
        val spaceshipSize = (width * 0.12f).dp
        
        // Uzay gemisi pozisyonu - düzgün konumlandırma için
        val xPos = (width * x - spaceshipSize.value / 2).dp
        val yPos = (height * y - spaceshipSize.value / 2).dp
        
        // Daha iyi görünüm için uzay gemisine gölge ekleyelim
        Column(
            modifier = Modifier
                .size(spaceshipSize)
                .offset(x = xPos, y = yPos)
                .shadow(8.dp, shape = RoundedCornerShape(spaceshipSize / 2))
                .padding(2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Roketin üst kısmı (uç)
            Box(
                modifier = Modifier
                    .size(spaceshipSize.times(0.4f))
                    .clip(CircleShape)
                    .background(Color(0xFFFF5252))
            )
            
            // Roketin gövdesi
            Box(
                modifier = Modifier
                    .size(width = spaceshipSize, height = spaceshipSize.times(0.7f))
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF3F51B5),
                                Color(0xFF7986CB)
                            )
                        )
                    )
            )
            
            // Roket ateşi efekti - yanıp sönen animasyon ekleyelim
            val infiniteTransition = rememberInfiniteTransition(label = "rocketFlame")
            val flameAlpha by infiniteTransition.animateFloat(
                initialValue = 0.7f,
                targetValue = 1.0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(300, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "flameAlpha"
            )
            
            Box(
                modifier = Modifier
                    .size(width = spaceshipSize.times(0.6f), height = spaceshipSize.times(0.3f))
                    .clip(RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFFF9800).copy(alpha = flameAlpha),
                                Color(0xFFFF5722).copy(alpha = flameAlpha)
                            )
                        )
                    )
            )
        }
    }
    
    @Composable
    fun LaserView(x: Float, y: Float) {
        val configuration = LocalConfiguration.current
        val width = configuration.screenWidthDp
        val height = configuration.screenHeightDp // Doğru ekran yüksekliğini kullanalım
        
        // Lazer boyutu
        val laserWidth = 4.dp
        val laserHeight = 16.dp
        
        // Lazer pozisyonu - düzgün konumlandırma için
        val xPos = (width * x - laserWidth.value / 2).dp
        val yPos = (height * y - laserHeight.value / 2).dp
        
        // Lazeri daha görünür yapma - parlama efekti ekleme
        Box(
            modifier = Modifier
                .width(laserWidth)
                .height(laserHeight)
                .offset(x = xPos, y = yPos)
                .shadow(4.dp, RoundedCornerShape(2.dp))
                .background(Color(0xFFFF4081), RoundedCornerShape(2.dp))
        )
    }
    
    @Composable
    fun AsteroidView(asteroid: Asteroid) {
        val configuration = LocalConfiguration.current
        val width = configuration.screenWidthDp
        val height = configuration.screenHeightDp
        
        // Asteroid boyutu - ekran genişliğinin %15'i (daha da büyütüldü)
        val baseSize = (width * 0.15f)
        val asteroidSize = (baseSize * asteroid.size).dp
        
        // Asteroid pozisyonu
        val xPos = (width * asteroid.x - asteroidSize.value / 2).dp
        val yPos = (height * asteroid.y - asteroidSize.value / 2).dp
        
        // Sayının uzunluğuna göre font boyutunu belirle
        val text = asteroid.value.toString()
        val fontSize = when (text.length) {
            1 -> 24.sp    // Tek basamaklı sayılar için çok büyük font
            2 -> 20.sp    // Çift basamaklı sayılar için biraz daha küçük
            3 -> 16.sp    // Üç basamaklı sayılar için daha küçük
            else -> 14.sp // Daha uzun sayılar için en küçük
        }
        
        // Asteroid rengini belirle - doğru cevap için özel renk
        val asteroidColors = if (asteroid.isCorrectAnswer) {
            listOf(
                Color(0xFF8D6E63),
                Color(0xFF5D4037)
            )
        } else {
            listOf(
                Color(0xFF795548),
                Color(0xFF4E342E)
            )
        }
        
        // Ana asteroid görünümü
        Box(
            modifier = Modifier
                .size(asteroidSize)
                .offset(x = xPos, y = yPos)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    spotColor = Color.Black.copy(alpha = 0.6f)
                )
                .background(
                    brush = Brush.radialGradient(
                        colors = asteroidColors
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Sayı için arka plan çemberi
            Box(
                modifier = Modifier
                    .size(asteroidSize.times(0.7f))
                    .background(
                        Color(0xFF4E342E).copy(alpha = 0.7f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Sayı değeri için sade, basit bir görünüm
                Text(
                    text = text,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = fontSize,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(1f, 1f),
                            blurRadius = 2f
                        )
                    ),
                    modifier = Modifier
                        .padding(2.dp),
                    // Taşma durumları için
                    maxLines = 1,
                    // Belirlenen sınırlara göre metin alanını ölçeklendir
                    softWrap = false
                )
            }
        }
    }
    
    @Composable
    fun FireButton(onShoot: () -> Unit, enabled: Boolean) {
        var isPressed by remember { mutableStateOf(false) }
        val scale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = tween(100),
            label = "buttonScale"
        )
        
        Button(
            onClick = {
                isPressed = true
                onShoot()
                handler.postDelayed({ isPressed = false }, 150)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .scale(scale),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "ATEŞ ET",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
    
    @Composable
    fun GameOverDialog(
        isCompleted: Boolean,
        score: Int,
        stars: Int,
        onRestart: () -> Unit,
        onNextLevel: () -> Unit,
        onHome: () -> Unit
    ) {
        // Animasyon için state'ler
        var showContent by remember { mutableStateOf(false) }
        var animatedScore by remember { mutableStateOf(0) }
        
        // Animasyonları aşamalı başlat
        LaunchedEffect(Unit) {
            showContent = true
            // Skor animasyonu
            val scoreStep = score / 10
            for (i in 1..10) {
                animatedScore = (score * (i / 10f)).toInt()
                delay(50)
            }
            animatedScore = score // Tam değeri göster
        }
        
        // Yıldız animasyonu
        val infiniteTransition = rememberInfiniteTransition(label = "starAnimation")
        val starScale = infiniteTransition.animateFloat(
            initialValue = 0.8f,
            targetValue = 1.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "starScale"
        )
        
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isCompleted) 
                        Color(0xFF1A237E).copy(alpha = 0.95f) 
                    else 
                        Color(0xFF4A148C).copy(alpha = 0.95f)
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
                    // Başlık
                    Text(
                        text = if (isCompleted) "Harika!" else "Oyun Bitti",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) Color(0xFFFFD700) else Color(0xFFE57373),
                        modifier = Modifier.scale(
                            animateFloatAsState(
                                targetValue = if (showContent) 1f else 0.5f,
                                animationSpec = tween(500),
                                label = "titleScale"
                            ).value
                        )
                    )
                    
                    // Alt başlık
                    Text(
                        text = if (isCompleted) 
                                "Seviye ${gameState.level.level} Tamamlandı!" 
                              else 
                                "Üzülme, tekrar deneyebilirsin",
                        fontSize = 16.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                    )
                    
                    // Uzay gemisi resmi
                    Image(
                        painter = painterResource(
                            id = if (isCompleted) R.drawable.spaceship else R.drawable.asteroid_icon
                        ),
                        contentDescription = if (isCompleted) "Uzay Gemisi" else "Asteroid",
                        modifier = Modifier
                            .size(80.dp)
                            .padding(vertical = 8.dp)
                            .scale(
                                animateFloatAsState(
                                    targetValue = if (showContent) 1f else 0f,
                                    animationSpec = tween(500),
                                    label = "shipScale"
                                ).value
                            ),
                        colorFilter = if (!isCompleted) ColorFilter.tint(Color.Gray) else null
                    )
                    
                    // Skor kartı
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .alpha(
                                animateFloatAsState(
                                    targetValue = if (showContent) 1f else 0f,
                                    animationSpec = tween(500),
                                    label = "cardAlpha"
                                ).value
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF283593)
                        ),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "PUANIN",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            
                            Text(
                                text = "$animatedScore",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            // Doğru cevaplar
                            Text(
                                text = "${gameState.correctAnswers}/${gameState.level.questionsToComplete} Doğru Cevap",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            
                            // İlerleme çubuğu
                            LinearProgressIndicator(
                                progress = { gameState.correctAnswers.toFloat() / gameState.level.questionsToComplete },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isCompleted) Color(0xFF4CAF50) else Color(0xFFFFA000),
                                trackColor = Color.White.copy(alpha = 0.3f)
                            )
                        }
                    }
                    
                    // Yıldızlar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .alpha(
                                animateFloatAsState(
                                    targetValue = if (showContent) 1f else 0f,
                                    animationSpec = tween(700),
                                    label = "starsAlpha"
                                ).value
                            ),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        repeat(3) { i ->
                            val isActive = i < stars
                            Icon(
                                painter = painterResource(id = R.drawable.ic_star),
                                contentDescription = "Yıldız ${i + 1}",
                                tint = if (isActive) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .size(48.dp)
                                    .scale(if (isActive) starScale.value else 1f)
                            )
                        }
                    }
                    
                    // Kalan süre (oyun tamamlandıysa)
                    if (isCompleted) {
                        Text(
                            text = "Kalan Süre: ${gameState.timeRemaining} saniye",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                        )
                    }
                    
                    // Butonlar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .alpha(
                                animateFloatAsState(
                                    targetValue = if (showContent) 1f else 0f,
                                    animationSpec = tween(900),
                                    label = "buttonsAlpha"
                                ).value
                            ),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Ana Menü Butonu
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF78909C))
                                .clickable { onHome() }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "Ana",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Menü",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        // Tekrar Oyna Butonu
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF2196F3))
                                .clickable { onRestart() }
                                .padding(vertical = 12.dp)
                        ) {
                            Text(
                                text = "Tekrar",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Oyna",
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                        
                        // Sonraki Seviye Butonu (sadece oyun tamamlandıysa)
                        if (isCompleted) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color(0xFF4CAF50))
                                    .clickable { onNextLevel() }
                                    .padding(vertical = 12.dp)
                            ) {
                                Text(
                                    text = "İleri",
                                    fontSize = 16.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    private fun updateGame() {
        if (gameState.isGameOver || gameState.isGameCompleted) return
        
        // Asteroitleri hareket ettir
        val updatedAsteroids = mutableListOf<Asteroid>()
        
        for (asteroid in asteroids) {
            // Yeni Y pozisyonu hesapla
            val newY = asteroid.y + asteroid.speed
            
            // Ekranın dışına çıktı mı?
            if (newY > 1.0f) {
                // Doğru cevabı kaçırdıysak
                if (asteroid.isCorrectAnswer) {
                    // Yanlış cevap geri bildirimi göster
                    showFeedback = true
                    isCorrectAnswer = false
                    mediaPlayerWrong?.start()
                    
                    // Süre cezası uygula (5 saniye düş)
                    val timePenalty = 5
                    
                    // Süre cezasını göster
                    timePenaltyValue = timePenalty
                    showTimePenalty = true
                    handler.postDelayed({ showTimePenalty = false }, 500)
                    
                    // Kalan süreyi güncelle (0'dan küçük olmasını engelle)
                    val newTimeRemaining = maxOf(0, gameState.timeRemaining - timePenalty)
                    gameState = gameState.copy(timeRemaining = newTimeRemaining)
                    
                    // Süre bittiyse oyunu bitir
                    if (newTimeRemaining <= 0) {
                        endGame(completed = false)
                        return
                    }
                    
                    // Biraz bekleyip yeni soru oluştur
                    handler.postDelayed({
                        if (!isFinishing) {  // Aktivite hala aktif mi kontrol et
                            showFeedback = false
                            createNewQuestion()
                        }
                    }, 1500)
                }
                continue // Bu asteroidi dikkate alma
            }
            
            // Güncelle
            updatedAsteroids.add(asteroid.copy(y = newY))
        }
        
        // Asteroid listesini güncelle
        asteroids.clear()
        asteroids.addAll(updatedAsteroids)
        
        // Lazer ışınlarını hareket ettir
        val updatedLasers = mutableListOf<Laser>()
        
        for (laser in activeLasers) {
            // Yeni Y pozisyonu hesapla - yukarı doğru hareket
            val newY = laser.y - 0.02f
            
            // Ekranın dışına çıktı mı?
            if (newY < 0f) {
                continue // Bu lazeri dikkate alma
            }
            
            // Lazeri güncelle
            val updatedLaser = laser.copy(y = newY)
            updatedLasers.add(updatedLaser)
            
            // Çarpışma kontrolü
            for (asteroid in asteroids.toList()) {  // ConcurrentModificationException'ı önlemek için toList() kullanıyoruz
                if (checkCollision(updatedLaser, asteroid)) {
                    // Çarpışma oldu
                    mediaPlayerExplosion?.start()
                    
                    // Doğru cevabı vurduk mu?
                    if (asteroid.isCorrectAnswer) {
                        // Doğru cevap
                        showFeedback = true
                        isCorrectAnswer = true
                        mediaPlayerCorrect?.start()
                        
                        // Skor ve doğru cevap sayısını artır
                        gameState = gameState.copy(
                            score = gameState.score + 10,
                            correctAnswers = gameState.correctAnswers + 1
                        )
                        
                        // Seviye tamamlandı mı?
                        if (gameState.correctAnswers >= gameState.level.questionsToComplete) {
                            endGame(completed = true)
                        } else {
                            // Biraz bekleyip yeni soru oluştur
                            handler.postDelayed({
                                if (!isFinishing) {  // Aktivite hala aktif mi kontrol et
                                    showFeedback = false
                                    createNewQuestion()
                                }
                            }, 1500)
                        }
                    } else {
                        // Yanlış cevap
                        showFeedback = true
                        isCorrectAnswer = false
                        mediaPlayerWrong?.start()
                        
                        // Süre cezası uygula (5 saniye düş)
                        val timePenalty = 5
                        
                        // Süre cezasını göster
                        timePenaltyValue = timePenalty
                        showTimePenalty = true
                        handler.postDelayed({ showTimePenalty = false }, 500)
                        
                        // Kalan süreyi güncelle (0'dan küçük olmasını engelle)
                        val newTimeRemaining = maxOf(0, gameState.timeRemaining - timePenalty)
                        gameState = gameState.copy(timeRemaining = newTimeRemaining)
                        
                        // Süre bittiyse oyunu bitir
                        if (newTimeRemaining <= 0) {
                            endGame(completed = false)
                            return
                        }
                        
                        // Biraz bekleyip geri bildirimi kapat
                        handler.postDelayed({
                            if (!isFinishing) {  // Aktivite hala aktif mi kontrol et
                                showFeedback = false
                            }
                        }, 1500)
                    }
                    
                    // Çarpılan asteroid ve lazeri kaldır
                    asteroids.remove(asteroid)
                    updatedLasers.remove(updatedLaser)
                    break
                }
            }
        }
        
        // Lazer listesini güncelle
        activeLasers.clear()
        activeLasers.addAll(updatedLasers)
        
        // Tüm asteroitler yok olduysa yeni soru oluştur
        if (asteroids.isEmpty() && !showFeedback) {
            createNewQuestion()
        }
    }
    
    private fun shoot() {
        // Ateş sesi çal
        mediaPlayerLaser?.start()
        
        // Lazer sayısını kontrol et - performans için
        if (activeLasers.size >= MAX_LASERS) {
            return
        }
        
        // Uzay gemisinin pozisyonundan yeni bir lazer oluştur
        val newLaser = Laser(
            x = gameState.spaceship.x,
            y = gameState.spaceship.y - 0.05f // Geminin biraz yukarısına
        )
        
        // Lazer listesine ekle
        activeLasers.add(newLaser)
    }
    
    private fun checkCollision(laser: Laser, asteroid: Asteroid): Boolean {
        // Basit çarpışma kontrolü
        val dx = laser.x - asteroid.x
        val dy = laser.y - asteroid.y
        val distance = kotlin.math.sqrt(dx * dx + dy * dy)
        
        // Çarpışma mesafesi - asteroid boyutu ile orantılı
        val collisionThreshold = 0.06f * asteroid.size
        
        return distance < collisionThreshold
    }
    
    private fun createNewQuestion() {
        try {
            // Yeni soru oluştur
            val question = SpaceMathHelper.generateQuestion(gameState.level)
            gameState = gameState.copy(currentQuestion = question)
            
            // Log ekleyelim
            android.util.Log.d("SpaceMathAdventureActivity", 
                "Yeni soru oluşturuldu - Seviye: ${gameState.level.level}, Soru: ${question.question}, Cevap: ${question.answer}")
            
            try {
                // Yeni asteroitler oluştur
                val newAsteroids = SpaceMathHelper.generateAsteroids(question, gameState.level)
                
                // Asteroid listesini temizle ve yenileri ekle
                asteroids.clear()
                asteroids.addAll(newAsteroids.take(MAX_ASTEROIDS))
                
                android.util.Log.d("SpaceMathAdventureActivity", 
                    "Yeni asteroidler oluşturuldu. Sayı: ${asteroids.size}")
            } catch (e: Exception) {
                // Asteroid oluşturma hatası
                android.util.Log.e("SpaceMathAdventureActivity", 
                    "Asteroid oluşturulurken hata oluştu: ${e.message}")
                
                // Basit asteroid seti oluştur
                val fallbackAsteroids = createFallbackAsteroids(question.answer, gameState.level.level)
                asteroids.clear()
                asteroids.addAll(fallbackAsteroids)
            }
        } catch (e: Exception) {
            // Genel bir hata - seviyeye göre uygun fallback soru oluştur
            android.util.Log.e("SpaceMathAdventureActivity", "Soru oluşturulurken hata: ${e.message}", e)
            
            val level = gameState.level.level
            val fallbackQuestion = createFallbackQuestion(level)
            gameState = gameState.copy(currentQuestion = fallbackQuestion)
            
            // Basit asteroitler oluştur
            val fallbackAsteroids = createFallbackAsteroids(fallbackQuestion.answer, level)
            asteroids.clear()
            asteroids.addAll(fallbackAsteroids)
            
            Toast.makeText(this, "Soru oluşturulamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Seviye bazlı yedek sorular oluşturur
     */
    private fun createFallbackQuestion(level: Int): com.yigit033.matematikmacarasi.games.models.SpaceMathQuestion {
        return when {
            level <= 2 -> {
                // 1-2. seviyeler için basit toplama/çıkarma
                if (level == 1) {
                    com.yigit033.matematikmacarasi.games.models.SpaceMathQuestion(
                        question = "3 + 2 = ?",
                        answer = 5,
                        operationType = com.yigit033.matematikmacarasi.games.models.SpaceOperationType.ADDITION
                    )
                } else {
                    com.yigit033.matematikmacarasi.games.models.SpaceMathQuestion(
                        question = "5 - 2 = ?",
                        answer = 3,
                        operationType = com.yigit033.matematikmacarasi.games.models.SpaceOperationType.SUBTRACTION
                    )
                }
            }
            level <= 4 -> {
                // 3-4. seviyeler için basit çarpma/karışık
                if (level == 3) {
                    com.yigit033.matematikmacarasi.games.models.SpaceMathQuestion(
                        question = "4 + 3 = ?",
                        answer = 7,
                        operationType = com.yigit033.matematikmacarasi.games.models.SpaceOperationType.ADDITION
                    )
                } else {
                    com.yigit033.matematikmacarasi.games.models.SpaceMathQuestion(
                        question = "3 × 2 = ?",
                        answer = 6,
                        operationType = com.yigit033.matematikmacarasi.games.models.SpaceOperationType.MULTIPLICATION
                    )
                }
            }
            level <= 6 -> {
                // 5-6. seviyeler için basit bölme/karışık
                if (level == 5) {
                    com.yigit033.matematikmacarasi.games.models.SpaceMathQuestion(
                        question = "8 ÷ 2 = ?",
                        answer = 4,
                        operationType = com.yigit033.matematikmacarasi.games.models.SpaceOperationType.DIVISION
                    )
                } else {
                    com.yigit033.matematikmacarasi.games.models.SpaceMathQuestion(
                        question = "4 × 3 = ?",
                        answer = 12,
                        operationType = com.yigit033.matematikmacarasi.games.models.SpaceOperationType.MULTIPLICATION
                    )
                }
            }
            else -> {
                // 7-8. seviyeler için orta seviye işlemler
                com.yigit033.matematikmacarasi.games.models.SpaceMathQuestion(
                    question = "24 ÷ 6 = ?",
                    answer = 4,
                    operationType = com.yigit033.matematikmacarasi.games.models.SpaceOperationType.DIVISION
                )
            }
        }
    }
    
    /**
     * Yedek asteroid seti oluşturur
     */
    private fun createFallbackAsteroids(correctAnswer: Int, level: Int): List<Asteroid> {
        // Seviyeye göre asteroid sayısını belirle
        val asteroidCount = kotlin.math.min(level + 3, 6)
        
        val result = mutableListOf<Asteroid>()
        
        // Doğru cevap için asteroid
        result.add(Asteroid(
            id = 0, 
            value = correctAnswer, 
            x = 0.5f, 
            y = -0.1f, 
            speed = 0.005f * level,
            isCorrectAnswer = true
        ))
        
        // Diğer yanlış cevaplar için asteroidler
        for (i in 1 until asteroidCount) {
            // Doğru cevaptan biraz farklı bir değer
            val offset = if (i % 2 == 0) i else -i
            val wrongValue = kotlin.math.max(1, correctAnswer + offset)
            
            result.add(Asteroid(
                id = i, 
                value = wrongValue, 
                x = 0.2f + (i * 0.6f / asteroidCount), 
                y = -0.1f - (i * 0.05f), 
                speed = 0.005f * level,
                isCorrectAnswer = false
            ))
        }
        
        return result
    }
    
    private fun endGame(completed: Boolean) {
        try {
            // Zamanlayıcıyı durdur
            handler.removeCallbacks(gameTimer)
            
            // Yıldızları hesapla - tek bir kez hesaplıyoruz
            val stars = SpaceMathHelper.calculateStars(gameState)
            
            // Oyun durumunu güncelle
            gameState = gameState.copy(
                isGameCompleted = completed,
                isGameOver = !completed,
                stars = stars
            )
            
            // Günlük görev ilerlemesini güncelle
            if (completed) {
                try {
                    // İki farklı yöntemle görev ilerlemesini güncelle
                    
                    // 1. Spesifik görev ID'si varsa onunla güncelle
                    if (dailyTaskId > 0) {
                        dailyTaskManager.updateTaskProgress(dailyTaskId)
                        android.util.Log.d("SpaceMathAdventureActivity", "Spesifik günlük görev güncellendi: $dailyTaskId")
                    }
                    
                    // 2. GameType kullanarak ilgili görevleri güncelle
                    dailyTaskManager.updateTaskProgressByGameType(gameType)
                    android.util.Log.d("SpaceMathAdventureActivity", "Günlük görev güncellendi (gameType: $gameType)")
                } catch (e: Exception) {
                    android.util.Log.e("SpaceMathAdventureActivity", "Günlük görev güncellenirken hata: ${e.message}")
                    e.printStackTrace()
                }
            }
            
            // Oyun istatistiklerini güncelle
            try {
                // GameStatisticsHelper ile oyun istatistiklerini kaydet
                gameStatisticsHelper.recordGameCompletion(
                    gameType = gameType,
                    level = selectedLevelIndex + 1,
                    score = gameState.score,
                    stars = stars,
                    gameStartTime = gameStartTime,
                    taskId = dailyTaskId
                )
                
                android.util.Log.d("SpaceMathAdventureActivity", 
                    "GameStatisticsHelper.recordGameCompletion çağrıldı: Seviye=${selectedLevelIndex + 1}, Skor=${gameState.score}, Yıldız=$stars")
                
                // Sadece GameProgressService ile ilerlemeyi kaydet (Bu blok zaten yıldızları ve ilerlemeyi kaydediyor)
                 try {
                     val gameProgressService = com.yigit033.matematikmacarasi.services.GameProgressService.getInstance(this)
                     val gameIdWithLevel = "SpaceMathAdventureActivity_Level_${selectedLevelIndex + 1}"
                     gameProgressService.saveProgress(gameIdWithLevel, selectedLevelIndex + 1, gameState.score, stars)
                     android.util.Log.d("SpaceMathAdventureActivity", 
                         "GameProgressService ile ilerleme kaydedildi: ID=$gameIdWithLevel, Skor=${gameState.score}, Yıldız=$stars")

                     // EventBus'ı manuel olarak tetikleyelim (StarManager yerine)
                     try {
                         val starManager = com.yigit033.matematikmacarasi.services.StarManager.getInstance(this)
                         
                         // Yıldızları doğrudan StarManager'a ekleyelim - SpaceMathAdventureActivity için özel sorun çözümü
                         // Seviye bilgisini içeren doğru ID formatını kullanalım
                         val gameIdWithLevel = "SpaceMathAdventureActivity_Level_${selectedLevelIndex + 1}"
                         starManager.addStars(gameIdWithLevel, stars)
                         android.util.Log.d("SpaceMathAdventureActivity", "StarManager'a yıldızlar eklendi: ID=$gameIdWithLevel, Stars=$stars")
                         
                         starManager.syncStarsWithRepository() // Repodan güncel değeri almasını sağla
                         val finalTotalStars = starManager.getTotalStars() // Güncel toplamı al
                         
                         // Normal güncelleme
                         com.yigit033.matematikmacarasi.services.EventBus.updateStars(finalTotalStars)
                         
                         // Acil güncelleme (UI'ın anında yansıması için)
                         CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                             try {
                                 com.yigit033.matematikmacarasi.services.EventBus.forceStarNotification(finalTotalStars)
                                 android.util.Log.d("SpaceMathAdventureActivity", "EventBus forceStarNotification başarılı: $finalTotalStars")
                             } catch (e: Exception) {
                                 android.util.Log.e("SpaceMathAdventureActivity", "EventBus acil bildirim hatası: ${e.message}")
                             }
                         }
                         
                         android.util.Log.d("SpaceMathAdventureActivity", "EventBus manuel tetiklendi: Toplam Yıldız=$finalTotalStars")
                     } catch (e: Exception) {
                          android.util.Log.e("SpaceMathAdventureActivity", "EventBus manuel tetiklenirken hata: ${e.message}")
                     }

                 } catch (e: Exception) {
                     android.util.Log.e("SpaceMathAdventureActivity", "GameProgress kaydedilirken hata: ${e.message}")
                     e.printStackTrace()
                 }
                
                // Kullanıcıya kazandığı yıldızları bildir
                Toast.makeText(
                    this,
                    "$stars yıldız kazandınız! Toplam yıldızlarınıza eklendi.",
                    Toast.LENGTH_LONG
                ).show()
                
                // Başarı sesi çal
                if (completed) {
                    mediaPlayerSuccess?.start()
                }
                
                // Oyun bitişinde geçiş reklamı göster
                try {
                    if (::adManager.isInitialized) {
                        android.util.Log.d("SpaceMathAdventureActivity", "Oyun sonunda interstitial reklam gösterilecek")
                        showInterstitialAd()
                    } else {
                        android.util.Log.w("SpaceMathAdventureActivity", "AdManager başlatılmamış, reklam gösterilemiyor")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SpaceMathAdventureActivity", "Reklam gösterimi sırasında hata: ${e.message}", e)
                }
                
            } catch (e: Exception) {
                android.util.Log.e("SpaceMathAdventureActivity", "İstatistikler güncellenirken hata: ${e.message}")
                e.printStackTrace()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Oyun sonlandırılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun restartGame() {
        try {
            // Mevcut seviyeyi yeniden başlat
            val currentLevelIndex = gameState.level.level - 1
            val currentLevel = SpaceLevels.levels[currentLevelIndex]
            
            // Yeni oyun durumu oluştur
            gameState = SpaceGameState(
                level = currentLevel,
                timeRemaining = currentLevel.timeInSeconds
            )
            
            // Yeni soru oluştur
            createNewQuestion()
            
            // Lazer listesini temizle
            activeLasers.clear()
            
            // Zamanlayıcıyı yeniden başlat
            handler.removeCallbacks(gameTimer)
            handler.postDelayed(gameTimer, 1000)
        } catch (e: Exception) {
            Toast.makeText(this, "Oyun yeniden başlatılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun startNextLevel() {
        try {
            // Bir sonraki seviyeyi kontrol et
            val nextLevelIndex = gameState.level.level
            
            // Son seviyedeysek ana menüye dön
            if (nextLevelIndex >= SpaceLevels.levels.size) {
                finish()
                return
            }
            
            val nextLevel = SpaceLevels.levels[nextLevelIndex]
            
            // Yeni oyun durumu oluştur
            gameState = SpaceGameState(
                level = nextLevel,
                timeRemaining = nextLevel.timeInSeconds
            )
            
            // Yeni soru oluştur
            createNewQuestion()
            
            // Lazer listesini temizle
            activeLasers.clear()
            
            // Zamanlayıcıyı yeniden başlat
            handler.removeCallbacks(gameTimer)
            handler.postDelayed(gameTimer, 1000)
        } catch (e: Exception) {
            Toast.makeText(this, "Sonraki seviye başlatılamadı: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onDestroy() {
        // Zamanlayıcıyı temizle
        handler.removeCallbacks(gameTimer)
        
        // MediaPlayer'ları temizle
        try {
            mediaPlayerCorrect?.release()
            mediaPlayerWrong?.release()
            mediaPlayerLaser?.release()
            mediaPlayerExplosion?.release()
            mediaPlayerSuccess?.release()
        } catch (e: Exception) {
            // MediaPlayer zaten serbest bırakılmış olabilir, sessizce devam et
        }
        
        // Referansları null'a ayarla
        mediaPlayerCorrect = null
        mediaPlayerWrong = null
        mediaPlayerLaser = null
        mediaPlayerExplosion = null
        mediaPlayerSuccess = null
        
        // AdManager kaynaklarını temizle
        try {
            if (::adManager.isInitialized) {
                adManager.destroy()
            }
        } catch (e: Exception) {
            android.util.Log.e("SpaceMathAdventureActivity", "AdManager temizlenirken hata: ${e.message}")
        }
        
        super.onDestroy()
    }
    
    private fun showInterstitialAd() {
        // Geliştirilmiş agresif reklam stratejisi
        try {
            // Önce internet bağlantısı kontrolü yap
            if (!NetworkUtils.isInternetAvailable(this)) {
                Log.d("SpaceMathAdventureActivity", "İnternet bağlantısı yok, reklam gösterilemiyor")
                NetworkUtils.showNoInternetToast(this)
                return
            }
            
            // AdManager'ı kontrol et ve yeni agresif reklam metodunu kullan
            if (::adManager.isInitialized) {
                Log.d("SpaceMathAdventureActivity", "Oyun sonunda agresif reklam gösterilecek")
                adManager.showInterstitialAdAfterGame(this)
            } else {
                Log.w("SpaceMathAdventureActivity", "AdManager başlatılmamış")
            }
        } catch (e: Exception) {
            Log.e("SpaceMathAdventureActivity", "Reklam gösterimi sırasında hata: ${e.message}", e)
        }
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
    // Seviyeye göre tamamlanması gereken toplam soru sayısını bul
    val totalQuestions = when {
        level <= 4 -> 10  // İlk 4 seviye için 10 soru
        level <= 6 -> 12  // 5. ve 6. seviye için 12 soru
        level == 7 -> 14  // 7. seviye için 14 soru
        else -> 16        // 8. seviye için 16 soru
    }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Geri butonu ve oyun adı
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF3F51B5))
                .padding(vertical = 8.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Geri Dön",
                tint = Color.White,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { onBackClick() }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = gameName,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Bilgi kartları (skor, süre ve seviye)
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
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
            
            // Soru/ilerleme göstergesi
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
                
                val destroyedAsteroids = score / 10 // Her 10 puan bir asteroit yok edildiğini varsayıyoruz
                Text(
                    text = "$destroyedAsteroids/$totalQuestions", // Seviyeye göre toplam soru sayısı
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
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
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
} 