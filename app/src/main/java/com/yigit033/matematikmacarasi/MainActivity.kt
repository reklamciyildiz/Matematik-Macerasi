package com.yigit033.matematikmacarasi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.games.basic.AdditionGameActivity
import com.yigit033.matematikmacarasi.games.basic.SubtractionGameActivity
import com.yigit033.matematikmacarasi.games.basic.MultiplicationGameActivity
import com.yigit033.matematikmacarasi.games.basic.DivisionGameActivity
import com.yigit033.matematikmacarasi.games.GameSelectionActivity
import com.yigit033.matematikmacarasi.games.ComingSoonActivity
import com.yigit033.matematikmacarasi.games.interactive.BalloonPopGameActivity
import com.yigit033.matematikmacarasi.games.interactive.NumberHuntGameActivity
import com.yigit033.matematikmacarasi.games.interactive.MathMazeGameActivity
import com.yigit033.matematikmacarasi.games.interactive.SpaceMathAdventureActivity
import com.yigit033.matematikmacarasi.profile.ProfileActivity
import com.yigit033.matematikmacarasi.screens.SettingsActivity
import com.yigit033.matematikmacarasi.settings.IconSelectorActivity
import com.yigit033.matematikmacarasi.ui.components.AnimatedBackground
import com.yigit033.matematikmacarasi.ui.components.ChildFriendlyButton
import com.yigit033.matematikmacarasi.ui.components.ChildFriendlyCard
import com.yigit033.matematikmacarasi.ui.components.ClickableCard
import com.yigit033.matematikmacarasi.ui.components.StaticBackground
import com.yigit033.matematikmacarasi.ui.components.SubtitleText
import com.yigit033.matematikmacarasi.ui.components.TitleText
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import com.yigit033.matematikmacarasi.services.AudioService
import com.yigit033.matematikmacarasi.services.AvatarService
import com.yigit033.matematikmacarasi.services.DailyTaskManager
import com.yigit033.matematikmacarasi.services.EventBus
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.services.payments.AdManager
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import android.util.Log
import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import coil.Coil
import coil.annotation.ExperimentalCoilApi
import com.google.gson.Gson
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.annotation.DrawableRes
import android.content.pm.PackageManager
import com.yigit033.matematikmacarasi.components.AnimatedAvatarView
import com.yigit033.matematikmacarasi.ui.components.ColorfulPuzzleIcon
import com.yigit033.matematikmacarasi.ui.components.RobotWithBlinkingEyesIcon
import com.yigit033.matematikmacarasi.ui.components.AnimatedClockIcon
import com.yigit033.matematikmacarasi.ui.components.GameControllerWithStarsIcon
import androidx.compose.animation.core.*
import androidx.compose.ui.zIndex
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import android.widget.Toast
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material3.LinearProgressIndicator
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.yigit033.matematikmacarasi.services.GameStatisticsService
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.utils.NetworkUtils
import com.yigit033.matematikmacarasi.services.payments.BillingTestActivity
import com.yigit033.matematikmacarasi.services.DailyGameLimitService
import com.yigit033.matematikmacarasi.services.DailyPremiumBonusService
import com.yigit033.matematikmacarasi.services.UpdateCheckService
import com.yigit033.matematikmacarasi.ui.components.UpdateDialog
import com.yigit033.matematikmacarasi.ui.components.CompactBannerAdComponent
import com.yigit033.matematikmacarasi.ui.screens.home.HomeScreenNew

class MainActivity : ComponentActivity() {
    private var lastResumeTime = 0L
    private var audioService: AudioService? = null
    private lateinit var dailyTaskManager: DailyTaskManager
    private lateinit var sharedPreferences: SharedPreferences
    private var purchaseManager: PurchaseManager? = null
    private var adManager: AdManager? = null
    private lateinit var dailyGameLimitService: DailyGameLimitService
    private lateinit var premiumBonusService: DailyPremiumBonusService
    private lateinit var updateCheckService: UpdateCheckService
    
    companion object {
        private const val TAG = "MainActivity"
    }
    
    @OptIn(ExperimentalCoilApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Android 12+ Splash Screen API kurulumu
        installSplashScreen()
        
        enableEdgeToEdge()
        
        // Uygulama tercihlerini yükle
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        
        // EventBus'ı başlat - KRİTİK
        EventBus.initialize(applicationContext)
        
        // İnternet bağlantısı kontrolü
        if (!NetworkUtils.isInternetAvailable(this)) {
            Log.w(TAG, "İnternet bağlantısı bulunamadı")
            Toast.makeText(
                this,
                "İnternet bağlantısı yok! Bazı özellikler ve reklamlar kullanılamayabilir.",
                Toast.LENGTH_LONG
            ).show()
        }
        
        initializeServices()
        initializePaymentAndAdSystems()
        initializeUpdateCheckService()
        clearImageCache()
        
        // EventBus'a profil değişikliklerini bildir
        EventBus.updateProfileData(getUserName(), System.currentTimeMillis())
        
        setContent {
            MatematikMacerasiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomeScreenNew(lastResumeTime)
                }
            }
        }
    }
    
    private fun initializeServices() {
        try {
            audioService = AudioService.getInstance(this)
            audioService?.let { service ->
                val soundStatus = service.checkSoundFilesStatus()
                Log.d("MainActivity", "Ses Dosyaları Durumu: $soundStatus")
            }
            dailyTaskManager = DailyTaskManager.getInstance(this)
            dailyGameLimitService = DailyGameLimitService.getInstance(this)
            premiumBonusService = DailyPremiumBonusService.getInstance(this)
            
            // Süre formatını düzeltme işlemini uygula
            try {
                val gameStatisticsService = GameStatisticsService.getInstance(this)
                val fixedCount = gameStatisticsService.fixBasicOperationGameTimeFormat()
                if (fixedCount > 0) {
                    Log.d("MainActivity", "Süre formatı düzeltme işlemi başarılı: $fixedCount kayıt düzeltildi")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Süre formatı düzeltme işlemi başarısız: ${e.message}", e)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Servisler başlatılamadı", e)
        }
    }
    
    /**
     * Ödeme sistemlerini ve reklam sistemlerini başlatır.
     */
    private fun initializePaymentAndAdSystems() {
        try {
            // Purchase Manager'ı başlat
            purchaseManager = PurchaseManager.getInstance(this)
            Log.d("MainActivity", "PurchaseManager başlatıldı")
            
            // Firebase'den premium kod durumunu senkronize et (uygulama silme-yükleme durumu için)
            syncPremiumCodeFromFirebase()
            
            // AdManager'ı başlat
            adManager = AdManager.getInstance(this)
            Log.d("MainActivity", "AdManager başlatıldı")
            
            // Premium durumunu kontrol et
            val isPremium = purchaseManager?.isPremium() ?: false
            Log.d("MainActivity", "Premium durum: $isPremium")
            
            // Varolan satın almaları sorgula
            purchaseManager?.queryPurchases()
            
            // Premium kullanıcılar için günlük bonus kontrolü
            if (isPremium) {
                try {
                    val bonusGiven = premiumBonusService.checkAndClaimAutomaticBonus()
                    if (bonusGiven) {
                        Log.d("MainActivity", "Premium günlük bonus verildi: ${premiumBonusService.getDailyBonusAmount()} yıldız")
                        // Toast mesajı HomeScreen'de gösterilecek
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Premium bonus kontrol hatası: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Ödeme ve reklam sistemleri başlatılamadı", e)
        }
    }
    
    /**
     * Güncelleme kontrol servisini başlatır
     */
    private fun initializeUpdateCheckService() {
        try {
            updateCheckService = UpdateCheckService.getInstance(this)
            Log.d("MainActivity", "UpdateCheckService başlatıldı")
        } catch (e: Exception) {
            Log.e("MainActivity", "UpdateCheckService başlatılamadı", e)
        }
    }
    
    /**
     * Firebase'den premium kod durumunu senkronize eder (uygulama silme-yükleme durumu için).
     * Device ID aynı kaldığı için Firebase'de aktivasyon kaydı varsa, lokal cache'i günceller.
     */
    private fun syncPremiumCodeFromFirebase() {
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val codeValidator = com.yigit033.matematikmacarasi.services.PremiumCodeValidator.getInstance(this@MainActivity)
                val synced = codeValidator.checkAndSyncPremiumStatus()
                
                if (synced) {
                    Log.d("MainActivity", "✅ Premium durumu Firebase'den senkronize edildi!")
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        // Premium durumu değişti, UI'yi güncelle
                        purchaseManager?.checkPremiumStatusAsync()
                    }
                } else {
                    Log.d("MainActivity", "Firebase'de aktivasyon bulunamadı")
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Firebase senkronizasyon hatası: ${e.message}", e)
            }
        }
    }
    
    @OptIn(ExperimentalCoilApi::class)
    private fun clearImageCache() {
        try {
            val imageLoader = Coil.imageLoader(this)
            imageLoader.memoryCache?.clear()
            imageLoader.diskCache?.clear()
        } catch (e: Exception) {
            Log.e("MainActivity", "Görüntü önbelleği temizlenemedi", e)
        }
    }
    
    override fun onResume() {
        super.onResume()
        lastResumeTime = System.currentTimeMillis()
        
        // Oyunlardan geri dönüldüğünde müziğin kesintisiz devam etmesi için
        val continueMusicFromGame = intent.getBooleanExtra("continue_music", false)
        if (continueMusicFromGame) {
            // Bu flag'i temizleyelim
            intent.removeExtra("continue_music")
            Log.d(TAG, "Oyundan ana ekrana dönüş: Müzik devam ediyor")
        }
        
        // Her durumda müziği başlat/devam ettir
        audioService?.resumeBackgroundMusic()
        
        // Güncelleme kontrolü yap (arka planda)
        try {
            if (::updateCheckService.isInitialized) {
                updateCheckService.checkForUpdates(this)
                Log.d("MainActivity", "Güncelleme kontrolü yapıldı")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Güncelleme kontrolü hatası: ${e.message}", e)
        }
        
        try {
            if (::dailyTaskManager.isInitialized) {
                val updatedTasks = dailyTaskManager.getTodayTasks()
                EventBus.updateDailyTasks(updatedTasks)
                Log.d("MainActivity", "Görevler güncellendi")
            }
            
            val userName = getUserName()
            EventBus.updateProfileData(userName, lastResumeTime)
            
            // Bildirimden geldiyse günlük görevleri göster
            val openDailyTasks = intent.getBooleanExtra("open_daily_tasks", false)
            if (openDailyTasks) {
                // showDailyTasks değerini true olarak ayarla - Composable içinde kullanılacak
                intent.removeExtra("open_daily_tasks") // Bir kez kullandıktan sonra temizle
                Log.d("MainActivity", "Bildirimden gelindi, günlük görevler açılacak")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "onResume güncelleme hatası: ${e.message}", e)
        }
    }
    
    override fun onPause() {
        super.onPause()
        audioService?.pauseBackgroundMusic()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        
        // Uygulama tamamen kapanıyorsa, ses kaynaklarını serbest bırak
        // Aktiviteler arası geçişlerde müziği korumak için isFinishing kontrolü ekliyoruz
        if (isFinishing) {
            // Uygulama gerçekten kapatılıyorsa tüm kaynakları temizle
            audioService?.release()
            audioService = null
            Log.d(TAG, "MainActivity tamamen kapatılıyor, tüm ses kaynakları temizlendi")
        } else {
            // Aktiviteler arası geçişlerde sadece efekt seslerini temizle, müziği devam ettir
            try {
                Log.d(TAG, "MainActivity yeniden başlatılacak, müzik devam ediyor")
            } catch (e: Exception) {
                Log.e(TAG, "Ses servisi işlemi sırasında hata: ${e.message}", e)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            // Profil sayfasından dönüldüğünde lastResumeTime'ı güncelle
            lastResumeTime = System.currentTimeMillis()
            
            // Avatar'ı da manuel olarak güncelle (EventBus ile birlikte çalışması için ek güvence)
            try {
                val avatarService = AvatarService(this)
                val currentAvatarId = avatarService.getCurrentAvatar().id
                Log.d("MainActivity", "Profil sayfasından dönüşte avatar güncelleniyor: $currentAvatarId")
                
                // Avatar ID'sini EventBus'a bildir
                EventBus.updateAvatarId(currentAvatarId)
            } catch (e: Exception) {
                Log.e("MainActivity", "Profil dönüşünde avatar güncellenirken hata: ${e.message}", e)
            }
        }
    }
    
    // Kullanıcı adını getir
    fun getUserName(): String {
        return try {
            sharedPreferences.getString("user_name", "Matematik Gezgini") ?: "Matematik Gezgini"
        } catch (e: Exception) {
            Log.e("MainActivity", "Kullanıcı adı alınamadı: ${e.message}", e)
            "Matematik Gezgini"
        }
    }

    fun navigateToIconSelector() {
        try {
            Log.d(TAG, "İkon seçici başlatılıyor...")
            // Basit ve direkt yaklaşım - önce bunu dene
            val intent = Intent(this, IconSelectorActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "İkon seçici birincil yöntem ile başlatılamadı: ${e.message}", e)
            
            try {
                // Yedek yöntem - eğer direkt referans çalışmazsa paket adı ile dene
                val backupIntent = Intent()
                backupIntent.setClassName(
                    "com.yigit033.matematikmacarasi", 
                    "com.yigit033.matematikmacarasi.settings.IconSelectorActivity"
                )
                Log.d(TAG, "İkon seçici ikincil yöntem ile deneniyor...")
                startActivity(backupIntent)
            } catch (e2: Exception) {
                Log.e(TAG, "İkon seçici yedek yöntem ile de başlatılamadı: ${e2.message}", e2)
                Toast.makeText(
                    this, 
                    "İkon seçici şu anda açılamıyor, lütfen daha sonra tekrar deneyin.", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

// Günlük görev türlerini tanımlayan enum sınıfı
enum class DailyTaskType {
    DIVISION, ADDITION, SUBTRACTION, MULTIPLICATION, MATCHING, COMPARISON, ANY
}

// Günlük görev veri sınıfı
data class DailyTask(
    val id: Int,
    val type: DailyTaskType,
    val description: String,
    val targetCount: Int,
    val currentCount: Int = 0,
    val reward: Int = 10, // Tamamlanınca kazanılacak puan/yıldız
    val gameType: GameType, // İlgili oyun türü
    val isCompleted: Boolean = currentCount >= targetCount
)

// Günlük görevleri yöneten sınıf
class DailyTaskManager(private val context: Context) {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences("daily_tasks", Context.MODE_PRIVATE)
    }
    
    private val gson: Gson by lazy {
        Gson()
    }
    
    // Bugünün tarihini al
    private fun getCurrentDateKey(): String {
        return try {
            val dateFormat = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
            dateFormat.format(java.util.Date())
        } catch (e: Exception) {
            Log.e("DailyTaskManager", "Tarih formatı oluşturulamadı", e)
            ""
        }
    }
    
    // Bugün için görevleri al
    fun getTodayTasks(): List<DailyTask> {
        return try {
            val currentDateKey = getCurrentDateKey()
            if (currentDateKey.isEmpty()) return emptyList()
            
            val lastGeneratedDate = sharedPreferences.getString("last_generated_date", "")
            
            if (lastGeneratedDate != currentDateKey) {
                return generateDailyTasks().also { saveTodayTasks(it) }
            }
            
            val tasksJson = sharedPreferences.getString("tasks_$currentDateKey", null)
            tasksJson?.let {
                try {
                    gson.fromJson(it, Array<DailyTask>::class.java).toList()
                } catch (e: Exception) {
                    Log.e("DailyTaskManager", "JSON dönüşüm hatası", e)
                    generateDailyTasks()
                }
            } ?: generateDailyTasks()
            
        } catch (e: Exception) {
            Log.e("DailyTaskManager", "Görevler alınamadı", e)
            emptyList()
        }
    }
    
    // Görevleri kaydet
    fun saveTodayTasks(tasks: List<DailyTask>) {
        try {
            val currentDateKey = getCurrentDateKey()
            if (currentDateKey.isEmpty()) return
            
            val editor = sharedPreferences.edit()
            val tasksJson = gson.toJson(tasks)
            
            editor.putString("tasks_$currentDateKey", tasksJson)
            editor.putString("last_generated_date", currentDateKey)
            editor.apply()
        } catch (e: Exception) {
            Log.e("DailyTaskManager", "Görevler kaydedilemedi", e)
        }
    }
    
    // Görev ilerlemesini güncelle
    fun updateTaskProgress(taskId: Int) {
        try {
            val tasks = getTodayTasks().toMutableList()
            val taskIndex = tasks.indexOfFirst { it.id == taskId }
            
            if (taskIndex != -1) {
                val task = tasks[taskIndex]
                if (task.currentCount < task.targetCount) {
                    tasks[taskIndex] = task.copy(currentCount = task.currentCount + 1)
                    saveTodayTasks(tasks)
                }
            }
        } catch (e: Exception) {
            Log.e("DailyTaskManager", "Görev ilerlemesi güncellenemedi", e)
        }
    }
    
    // Rastgele günlük görevler üret
    private fun generateDailyTasks(): List<DailyTask> {
        return try {
            val random = java.util.Random()
            val tasks = mutableListOf<DailyTask>()
            val gameTypes = GameType.values()
            val usedGameTypes = mutableSetOf<GameType>()
            var taskId = 1
            
            // Her gün için 5-6 görev oluştur
            val taskCount = random.nextInt(2) + 5 // 5-6 görev
            
            while (tasks.size < taskCount && usedGameTypes.size < gameTypes.size) {
                // Rastgele bir oyun seç
                val gameType = gameTypes[random.nextInt(gameTypes.size)]
                
                // Aynı oyundan en fazla bir görev olsun
                if (gameType in usedGameTypes) continue
                
                // Oyun türüne göre hedef sayısını belirle 
                val targetCount = random.nextInt(3) + 1 // 1-3 arası
                
                // Oyun türüne göre görev türü ve açıklaması oluştur
                val (taskType, description) = when (gameType) {
                    GameType.ADDITION_GAME -> Pair(
                        DailyTaskType.ADDITION,
                        "$targetCount Toplama Oyunu Oyna"
                    )
                    GameType.SUBTRACTION_GAME -> Pair(
                        DailyTaskType.SUBTRACTION,
                        "$targetCount Çıkarma Oyunu Oyna"
                    )
                    GameType.MULTIPLICATION_GAME -> Pair(
                        DailyTaskType.MULTIPLICATION,
                        "$targetCount Çarpma Oyunu Oyna"
                    )
                    GameType.DIVISION_GAME -> Pair(
                        DailyTaskType.DIVISION,
                        "$targetCount Bölme Oyunu Oyna"
                    )
                    GameType.BALLOON_POP_GAME -> Pair(
                        DailyTaskType.ANY,
                        "$targetCount Balon Patlatma Oyunu Oyna"
                    )
                    GameType.NUMBER_HUNT_GAME -> Pair(
                        DailyTaskType.MATCHING,
                        "$targetCount Sayı Avı Oyunu Oyna"
                    )
                    GameType.MATH_MAZE_GAME -> Pair(
                        DailyTaskType.COMPARISON,
                        "$targetCount Matematik Labirenti Oyunu Oyna"
                    )
                    GameType.SPACE_MATH_ADVENTURE -> Pair(
                        DailyTaskType.ANY,
                        "$targetCount Uzay Matematik Macerası Oyna"
                    )
                    GameType.SHAPE_RECOGNITION -> Pair(
                        DailyTaskType.MATCHING,
                        "$targetCount Şekil Tanıma Oyunu Oyna"
                    )
                    GameType.MARKET_SIMULATOR -> Pair(
                        DailyTaskType.ANY,
                        "$targetCount Market Simülasyonu Oyna"
                    )
                }
                
                tasks.add(
                    DailyTask(
                        id = taskId++,
                        type = taskType,
                        description = description,
                        targetCount = targetCount,
                        gameType = gameType
                    )
                )
                
                usedGameTypes.add(gameType)
            }
            
            // Her zaman en az bir "Herhangi Bir" görevi ekle
            tasks.add(
                DailyTask(
                    id = taskId,
                    type = DailyTaskType.ANY,
                    description = "Herhangi Bir Oyunda İlerle",
                    targetCount = 1,
                    gameType = GameType.values()[random.nextInt(GameType.values().size)]
                )
            )
            
            tasks
        } catch (e: Exception) {
            Log.e("DailyTaskManager", "Görevler oluşturulamadı", e)
            emptyList()
        }
    }
    
    // Görevin tamamlandı mı kontrolü
    fun isTaskCompleted(taskId: Int): Boolean {
        val tasks = getTodayTasks()
        val task = tasks.find { it.id == taskId } ?: return false
        return task.currentCount >= task.targetCount
    }
    
    // Tüm görevlerin tamamlandı mı kontrolü
    fun areAllTasksCompleted(): Boolean {
        val tasks = getTodayTasks()
        return tasks.all { it.currentCount >= it.targetCount }
    }
    
    // Tamamlanan görevlerin ödüllerini ver
    fun claimRewards(): Int {
        val tasks = getTodayTasks()
        var totalRewards = 0
        
        tasks.forEach { task ->
            if (task.currentCount >= task.targetCount) {
                totalRewards += task.reward
            }
        }
        
        // Ödül verildikten sonra görevleri sıfırlama işlemi yapılabilir
        // Bu örnekte şimdilik sadece toplam ödül miktarını döndürüyoruz
        
        return totalRewards
    }
    
    fun getTotalStars(): Int {
        return try {
            val tasks = getTodayTasks()
            tasks.filter { it.isCompleted }.sumOf { it.reward }
        } catch (e: Exception) {
            Log.e("DailyTaskManager", "Toplam yıldız hesaplanamadı", e)
            0
        }
    }
}

@Composable
fun HomeScreen(lastResumeTime: Long = 0, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    val configuration = LocalConfiguration.current
    
    // Büyük ekran tespiti
    val isLargeScreen = configuration.screenWidthDp >= 600 // Tablet boyutu
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    
    // State değişkenleri
    var userName by remember { mutableStateOf("Matematik Gezgini") }
    val avatarService = remember { AvatarService(context) }
    var currentAvatar by remember { mutableStateOf(avatarService.getCurrentAvatar()) }
    val dailyTaskManager = remember { DailyTaskManager(context) }
    var showDailyTasks by remember { mutableStateOf(false) }
    var dailyTasks by remember { mutableStateOf(dailyTaskManager.getTodayTasks()) }
    
    // Yıldız sayısı için state değişkeni - EventBus'tan GÖZLEMLE
    val userStars by EventBus.currentStars.collectAsState()
    
    // MainActivity erişimi
    val mainActivity = context as? MainActivity
    
    // Version check için state
    var showUpdateDialog by remember { mutableStateOf(false) }
    var isForceUpdate by remember { mutableStateOf(false) }
    var updateMessage by remember { mutableStateOf("") }
    var updateUrl by remember { mutableStateOf("") }
    val versionCheckService = remember { com.yigit033.matematikmacarasi.services.VersionCheckService.getInstance(context) }
    
    // Version check - Uygulama açılışında bir kere
    LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            when (val result = versionCheckService.checkForUpdate()) {
                is com.yigit033.matematikmacarasi.services.VersionCheckService.UpdateStatus.ForceUpdateRequired -> {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isForceUpdate = true
                        updateMessage = result.versionInfo.force_update_message
                        updateUrl = result.versionInfo.update_url
                        showUpdateDialog = true
                    }
                }
                is com.yigit033.matematikmacarasi.services.VersionCheckService.UpdateStatus.UpdateAvailable -> {
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        isForceUpdate = false
                        updateMessage = result.versionInfo.update_message
                        updateUrl = result.versionInfo.update_url
                        showUpdateDialog = true
                    }
                }
                else -> {
                    // No update needed or error
                }
            }
        }
    }
    
    // Kullanıcı adını güncelle (lastResumeTime değiştiğinde)
    LaunchedEffect(lastResumeTime) {
        mainActivity?.let {
            userName = it.getUserName()
            
            // Görevleri direkt olarak yeniden yükle ve güncel durumu al
            val freshTasks = dailyTaskManager.getTodayTasks()
            dailyTasks = freshTasks
            
            // EventBus üzerinden diğer ekranlara da bildir
            EventBus.updateDailyTasks(freshTasks)
            
            Log.d("HomeScreen", "Görevler yeniden yüklendi: ${freshTasks.size} görev, " +
                    "${freshTasks.count { !it.isCompleted }} tamamlanmamış")
        }
    }
    
    // Avatar değişikliklerini dinle
    LaunchedEffect(Unit) {
        EventBus.avatarChangedEvents.collect { event ->
            try {
                currentAvatar = avatarService.getAvatarById(event.avatarId)
                Log.d("HomeScreen", "Avatar güncellendi: ID = ${event.avatarId}")
            } catch (e: Exception) {
                Log.e("HomeScreen", "Avatar değiştirilirken hata: ${e.message}", e)
            }
        }
    }
    
    // Günlük görevleri dinle
    LaunchedEffect(Unit) {
        EventBus.dailyTasks.collect { updatedTasks ->
            dailyTasks = updatedTasks
            val uncompletedTaskCount = updatedTasks.count { !it.isCompleted }
            Log.d("HomeScreen", "Günlük görevler güncellendi: ${updatedTasks.size} görev, " +
                    "$uncompletedTaskCount tamamlanmamış görev kaldı")
        }
    }
    
    // Profil güncellemelerini dinle
    LaunchedEffect(Unit) {
        EventBus.profileUpdateTime.collect { updateTime ->
            if (updateTime > 0) {
                try {
                    // Kullanıcı adını güncelle
                    val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                    val updatedUserName = sharedPreferences.getString("user_name", userName) ?: userName
                    
                    if (updatedUserName != userName) {
                        userName = updatedUserName
                        Log.d("HomeScreen", "Kullanıcı adı güncellendi: $userName")
                    }
                } catch (e: Exception) {
                    Log.e("HomeScreen", "Profil bilgileri güncellenirken hata: ${e.message}", e)
                }
            }
        }
    }

    // Arka plan
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF7B1FA2), // Koyu mor
                        Color(0xFF9C27B0), // Mor
                        Color(0xFFE1BEE7)  // Açık mor
                    )
                )
            )
    ) {
        // Ana içerik
        if (isLargeScreen) {
            // Büyük ekranlar için yatay düzen
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Sol panel - Profil ve günlük görevler
                Column(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight()
                        .padding(start = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Profil Bilgileri (Büyük ekran için daha büyük)
                    ProfileSection(
                        userName = userName,
                        currentAvatar = currentAvatar,
                        userStars = userStars,
                        dailyTasks = dailyTasks,
                        onProfileClick = {
                            val intent = Intent(context, ProfileActivity::class.java)
                            (context as MainActivity).startActivityForResult(intent, 1001)
                        },
                        onDailyTasksClick = { showDailyTasks = true },
                        isLargeScreen = true
                    )
                }
                
                // Sağ panel - Oyunlar
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight()
                        .verticalScroll(scrollState)
                        .padding(end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Uygulama Başlığı
                    ColorfulAnimatedTitle()
                    
                    // Oyun içerikleri
                    GameContentSection(
                        context = context,
                        isLargeScreen = true,
                        screenWidth = configuration.screenWidthDp
                    )
                }
            }
        } else {
            // Küçük ekranlar için dikey düzen (mevcut tasarım)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Üst Bar
                ProfileSection(
                    userName = userName,
                    currentAvatar = currentAvatar,
                    userStars = userStars,
                    dailyTasks = dailyTasks,
                    onProfileClick = {
                        val intent = Intent(context, ProfileActivity::class.java)
                        (context as MainActivity).startActivityForResult(intent, 1001)
                    },
                    onDailyTasksClick = { showDailyTasks = true },
                    isLargeScreen = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Uygulama Başlığı
                ColorfulAnimatedTitle()

                // Oyun içerikleri
                GameContentSection(
                    context = context,
                    isLargeScreen = false,
                    screenWidth = configuration.screenWidthDp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Alt Bar Butonları - Banner reklam ile güncellenmiş
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ayarlar Butonu
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8929AD)) // Mor arka plan
                    .clickable {
                        val intent = Intent(context, SettingsActivity::class.java)
                        context.startActivity(intent)
                    }
                    .border(width = 2.dp, color = Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Ayarlar",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // 🎯 KOMPAKT BANNER REKLAM - Butonlar arası
            CompactBannerAdComponent(
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            
            // İkon Değiştirme Butonu
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8929AD)) // Mor arka plan
                    .clickable {
                        val intent = Intent(context, IconSelectorActivity::class.java)
                        context.startActivity(intent)
                    }
                    .border(width = 2.dp, color = Color.White, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Apps,
                    contentDescription = "İkon Değiştir",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }

    // Güncelleme dialog'u
    if (showUpdateDialog) {
        com.yigit033.matematikmacarasi.ui.dialogs.UpdateDialog(
            isForceUpdate = isForceUpdate,
            message = updateMessage,
            onUpdateClick = {
                versionCheckService.openPlayStore(updateUrl)
                if (!isForceUpdate) {
                    showUpdateDialog = false
                }
            },
            onDismiss = {
                if (!isForceUpdate) {
                    showUpdateDialog = false
                }
            }
        )
    }
    
    // Günlük görevler dialog
    if (showDailyTasks) {
        DailyTasksDialog(
            onDismiss = { showDailyTasks = false },
            onTaskClick = { task ->
                val intent = when (task.gameType) {
                    GameType.ADDITION_GAME -> Intent(context, AdditionGameActivity::class.java)
                    GameType.SUBTRACTION_GAME -> Intent(context, SubtractionGameActivity::class.java)
                    GameType.MULTIPLICATION_GAME -> Intent(context, MultiplicationGameActivity::class.java)
                    GameType.DIVISION_GAME -> Intent(context, DivisionGameActivity::class.java)
                    GameType.BALLOON_POP_GAME -> Intent(context, BalloonPopGameActivity::class.java)
                    GameType.NUMBER_HUNT_GAME -> Intent(context, com.yigit033.matematikmacarasi.games.sayiavi2.SayiAvi2Activity::class.java)
                    GameType.MATH_MAZE_GAME -> Intent(context, MathMazeGameActivity::class.java)
                    GameType.SPACE_MATH_ADVENTURE -> Intent(context, SpaceMathAdventureActivity::class.java)
                    GameType.SHAPE_RECOGNITION -> Intent(context, GameSelectionActivity::class.java)
                    GameType.MARKET_SIMULATOR -> Intent(context, GameSelectionActivity::class.java)
                }
                intent.putExtra("daily_task_id", task.id)
                context.startActivity(intent)
            },
            dailyTasks = dailyTasks
        )
    }
    
    // Güncelleme dialog'u - Play Store güncelleme kontrolü
    val updateCheckService = remember { UpdateCheckService.getInstance(context) }
    if (updateCheckService.showUpdateDialog) {
        UpdateDialog(
            onUpdateClick = { 
                updateCheckService.acceptUpdate(context as Activity)
            },
            onDismiss = { 
                updateCheckService.dismissUpdateDialog()
            }
        )
    }
}

@Composable
fun ProfileSection(
    userName: String,
    currentAvatar: com.yigit033.matematikmacarasi.models.Avatar,
    userStars: Int,
    dailyTasks: List<DailyTask>,
    onProfileClick: () -> Unit,
    onDailyTasksClick: () -> Unit,
    isLargeScreen: Boolean
) {
    if (isLargeScreen) {
        // Büyük ekranlar için dikey profil düzeni
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            // Avatar (daha büyük)
            Card(
                shape = CircleShape,
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(3.dp, Color(0xFFFFD700)),
                modifier = Modifier
                    .size(120.dp)
                    .clickable { onProfileClick() }
            ) {
                AnimatedAvatarView(
                    avatar = currentAvatar,
                    modifier = Modifier.fillMaxSize().padding(6.dp),
                    contentDescription = currentAvatar.name
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // İsim (daha büyük)
            Text(
                text = userName,
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Yıldız sayısı (daha büyük)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFFFF9C4).copy(alpha = 0.9f),
                                Color(0xFFFFF59D).copy(alpha = 0.9f)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .border(
                        width = 2.dp,
                        color = Color(0xFFFFEB3B).copy(alpha = 0.6f),
                        shape = RoundedCornerShape(50.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Yıldız",
                    tint = Color(0xFFFFD700),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$userStars",
                    color = Color(0xFF7B6608),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Günlük görevler butonu (daha büyük)
            Box {
                Button(
                    onClick = onDailyTasksClick,
                    modifier = Modifier.size(64.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(width = 3.dp, color = Color.White.copy(alpha = 0.8f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Günlük Görevler",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Tamamlanmamış görev bildirimi
                val uncompletedTaskCount = dailyTasks.count { !it.isCompleted }
                if (uncompletedTaskCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .offset(x = 40.dp, y = (-12).dp)
                            .background(Color(0xFFFF5252), CircleShape)
                            .zIndex(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uncompletedTaskCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    } else {
        // Küçük ekranlar için yatay profil düzeni (mevcut tasarım)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profil Bilgileri
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .clickable { onProfileClick() }
            ) {
                // Avatar
                Card(
                    shape = CircleShape,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(2.dp, Color(0xFFFFD700)),
                    modifier = Modifier.size(72.dp)
                ) {
                    AnimatedAvatarView(
                        avatar = currentAvatar,
                        modifier = Modifier.fillMaxSize().padding(4.dp),
                        contentDescription = currentAvatar.name
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    // İsim
                    Text(
                        text = userName,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    
                    // Yıldız sayısı
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(45.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFFFFF9C4).copy(alpha = 0.8f),
                                        Color(0xFFFFF59D).copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .border(
                                width = 1.dp,
                                color = Color(0xFFFFEB3B).copy(alpha = 0.5f),
                                shape = RoundedCornerShape(30.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Yıldız",
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$userStars",
                            color = Color(0xFF7B6608),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Günlük görevler butonu
            Box {
                Button(
                    onClick = onDailyTasksClick,
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EE),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(width = 2.dp, color = Color.White.copy(alpha = 0.7f)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Günlük Görevler",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Tamamlanmamış görev bildirimi
                val uncompletedTaskCount = dailyTasks.count { !it.isCompleted }
                if (uncompletedTaskCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .offset(x = 32.dp, y = (-10).dp)
                            .background(Color(0xFFFF5252), CircleShape)
                            .zIndex(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uncompletedTaskCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GameContentSection(
    context: Context,
    isLargeScreen: Boolean,
    screenWidth: Int
) {
    if (isLargeScreen) {
        // Büyük ekranlar için grid düzeni
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Temel İşlemler - Grid olarak
            Text(
                text = "Temel İşlemler",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            // 2x2 Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BasicOperationCard(
                        icon = R.drawable.ic_addition,
                        title = "Toplama",
                        color = Color(0xFF2196F3),
                        onClick = {
                            val intent = Intent(context, AdditionGameActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    BasicOperationCard(
                        icon = R.drawable.ic_subtraction,
                        title = "Çıkarma",
                        color = Color(0xFFE91E63),
                        onClick = {
                            val intent = Intent(context, SubtractionGameActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    BasicOperationCard(
                        icon = R.drawable.ic_multiplication,
                        title = "Çarpma",
                        color = Color(0xFF4CAF50),
                        onClick = {
                            val intent = Intent(context, MultiplicationGameActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                    BasicOperationCard(
                        icon = R.drawable.ic_division,
                        title = "Bölme",
                        color = Color(0xFFFF9800),
                        onClick = {
                            val intent = Intent(context, DivisionGameActivity::class.java)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Diğer Konular
            Text(
                text = "Diğer Konular",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            // 2x2 Grid
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GameCard(
                        icon = R.drawable.ic_shapes,
                        title = "Şekiller & Matematik",
                        color = Color(0xFF009688),
                        badgeCount = 0,
                        onClick = {
                            val intent = Intent(context, ComingSoonActivity::class.java)
                            intent.putExtra("game_name", "Şekiller & Matematik")
                            intent.putExtra("color_code", 0xFF009688.toInt())
                            intent.putExtra("game_description", "geometrik şekiller ve özelliklerini")
                            intent.putExtra("is_category", true)
                            context.startActivity(intent)
                        },
                        customIcon = { 
                            Box(modifier = Modifier.size(40.dp)) {
                                ColorfulPuzzleIcon()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GameCard(
                        icon = R.drawable.ic_algorithm,
                        title = "Algoritma & Matematik",
                        color = Color(0xFF9C27B0),
                        badgeCount = 2,
                        onClick = {
                            val intent = Intent(context, ComingSoonActivity::class.java)
                            intent.putExtra("game_name", "Algoritma & Matematik")
                            intent.putExtra("color_code", 0xFF9C27B0.toInt())
                            intent.putExtra("game_description", "algoritma ve kodlama mantığını")
                            intent.putExtra("is_category", true)
                            context.startActivity(intent)
                        },
                        customIcon = { 
                            Box(modifier = Modifier.size(40.dp)) {
                                RobotWithBlinkingEyesIcon()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    GameCard(
                        icon = R.drawable.ic_clock,
                        title = "Gerçek & Matematik",
                        color = Color(0xFFFF5722),
                        badgeCount = 1,
                        onClick = {
                            val intent = Intent(context, GameSelectionActivity::class.java)
                            intent.putExtra("category", "real")
                            context.startActivity(intent)
                        },
                        customIcon = { 
                            Box(modifier = Modifier.size(40.dp)) {
                                AnimatedClockIcon()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    GameCard(
                        icon = R.drawable.ic_fun,
                        title = "Eğlence & Matematik",
                        color = Color(0xFF673AB7),
                        badgeCount = 3,
                        onClick = {
                            val intent = Intent(context, GameSelectionActivity::class.java)
                            intent.putExtra("category", "interactive")
                            context.startActivity(intent)
                        },
                        customIcon = { 
                            Box(modifier = Modifier.size(40.dp)) {
                                GameControllerWithStarsIcon()
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    } else {
        // Küçük ekranlar için mevcut tasarım
        Column {
            // Temel İşlemler Rolling Cards
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Horizontalscroll durumu
                val horizontalScrollState = rememberScrollState()
                
                // Başlık bölümü ve kaydırma göstergesi
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Temel İşlemler",
                        color = Color.White,
                        fontSize = (screenWidth * 0.05f).coerceIn(20f, 26f).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Scroll indikatörü
                    androidx.compose.animation.AnimatedVisibility(
                        visible = horizontalScrollState.value == 0,
                        enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInHorizontally(),
                        exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.slideOutHorizontally()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 16.dp)
                        ) {
                            Text(
                                text = "Kaydır", 
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = (screenWidth * 0.03f).coerceIn(14f, 18f).sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = "Sağa Kaydır",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size((screenWidth * 0.05f).coerceIn(20f, 26f).dp)
                            )
                        }
                    }
                }
                
                // Ekran genişliğine göre kartlar arası boşluğu ayarla
                val cardSpacing = (screenWidth * 0.015f).coerceIn(4f, 10f).dp
                
                Row(
                    modifier = Modifier
                        .horizontalScroll(horizontalScrollState)
                        .padding(bottom = 20.dp)
                ) {
                    BasicOperationCard(
                        icon = R.drawable.ic_addition,
                        title = "Toplama",
                        color = Color(0xFF2196F3),
                        onClick = {
                            val intent = Intent(context, AdditionGameActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.width(cardSpacing))
                    BasicOperationCard(
                        icon = R.drawable.ic_subtraction,
                        title = "Çıkarma",
                        color = Color(0xFFE91E63),
                        onClick = {
                            val intent = Intent(context, SubtractionGameActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.width(cardSpacing))
                    BasicOperationCard(
                        icon = R.drawable.ic_multiplication,
                        title = "Çarpma",
                        color = Color(0xFF4CAF50),
                        onClick = {
                            val intent = Intent(context, MultiplicationGameActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                    Spacer(modifier = Modifier.width(cardSpacing))
                    BasicOperationCard(
                        icon = R.drawable.ic_division,
                        title = "Bölme",
                        color = Color(0xFFFF9800),
                        onClick = {
                            val intent = Intent(context, DivisionGameActivity::class.java)
                            context.startActivity(intent)
                        }
                    )
                }
            }

            // Diğer Konular Grid
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                // Ekran genişliği küçük olduğunda boşlukları küçült
                val gridSpacing = (screenWidth * 0.03f).coerceIn(8f, 16f).dp
                
                Text(
                    text = "Diğer Konular",
                    color = Color.White,
                    fontSize = (screenWidth * 0.05f).coerceIn(20f, 26f).sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(gridSpacing)
                ) {
                    // Sol kolon
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(gridSpacing)
                    ) {
                        GameCard(
                            icon = R.drawable.ic_shapes,
                            title = "Şekiller & Matematik",
                            color = Color(0xFF009688),
                            badgeCount = 0,
                            onClick = {
                                val intent = Intent(context, ComingSoonActivity::class.java)
                                intent.putExtra("game_name", "Şekiller & Matematik")
                                intent.putExtra("color_code", 0xFF009688.toInt())
                                intent.putExtra("game_description", "geometrik şekiller ve özelliklerini")
                                intent.putExtra("is_category", true)
                                context.startActivity(intent)
                            },
                            customIcon = { 
                                Box(modifier = Modifier.size((screenWidth * 0.05f).coerceIn(20f, 32f).dp)) {
                                    ColorfulPuzzleIcon()
                                }
                            }
                        )
                        
                        GameCard(
                            icon = R.drawable.ic_algorithm,
                            title = "Algoritma & Matematik",
                            color = Color(0xFF9C27B0),
                            badgeCount = 2,
                            onClick = {
                                val intent = Intent(context, ComingSoonActivity::class.java)
                                intent.putExtra("game_name", "Algoritma & Matematik")
                                intent.putExtra("color_code", 0xFF9C27B0.toInt())
                                intent.putExtra("game_description", "algoritma ve kodlama mantığını")
                                intent.putExtra("is_category", true)
                                context.startActivity(intent)
                            },
                            customIcon = { 
                                Box(modifier = Modifier.size((screenWidth * 0.05f).coerceIn(20f, 32f).dp)) {
                                    RobotWithBlinkingEyesIcon()
                                }
                            }
                        )
                    }
                    
                    // Sağ kolon
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(gridSpacing)
                    ) {
                        GameCard(
                            icon = R.drawable.ic_clock,
                            title = "Gerçek & Matematik",
                            color = Color(0xFFFF5722),
                            badgeCount = 1,
                            onClick = {
                                val intent = Intent(context, GameSelectionActivity::class.java)
                                intent.putExtra("category", "real")
                                context.startActivity(intent)
                            },
                            customIcon = { 
                                Box(modifier = Modifier.size((screenWidth * 0.05f).coerceIn(20f, 32f).dp)) {
                                    AnimatedClockIcon()
                                }
                            }
                        )
                        
                        GameCard(
                            icon = R.drawable.ic_fun,
                            title = "Eğlence & Matematik",
                            color = Color(0xFF673AB7),
                            badgeCount = 3,
                            onClick = {
                                val intent = Intent(context, GameSelectionActivity::class.java)
                                intent.putExtra("category", "interactive")
                                context.startActivity(intent)
                            },
                            customIcon = { 
                                Box(modifier = Modifier.size((screenWidth * 0.05f).coerceIn(20f, 32f).dp)) {
                                    GameControllerWithStarsIcon()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BasicOperationCard(
    @DrawableRes icon: Int,
    title: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .then(
                if (modifier == Modifier) {
                    Modifier
                        .width(120.dp)
                        .height(150.dp)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(0.8f)
                }
            )
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Arka plan dekorasyon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 25.dp, y = -25.dp)
                    .background(
                        color = color.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
            
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // İkon alanı - ortada
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = icon),
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                // Başlık alanı
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        color = color,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GameCard(
    @DrawableRes icon: Int,
    title: String,
    color: Color,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    customIcon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .then(
                if (modifier == Modifier) {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.1f)
                } else {
                    Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.0f)
                }
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Arka plan dekorasyon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 30.dp, y = -30.dp)
                    .background(
                        color = color.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            )
            
            if (badgeCount > 0) {
                Badge(
                    modifier = Modifier
                        .padding(12.dp)
                        .align(Alignment.TopEnd),
                    containerColor = Color(0xFFC62828)
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // İkon bölümü
                Box(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .size(60.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(modifier = Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        customIcon()
                    }
                }
                
                // Başlık bölümü
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = color,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    MatematikMacerasiTheme {
        HomeScreen()
    }
}

@Composable
fun DailyTasksDialog(
    onDismiss: () -> Unit,
    onTaskClick: (task: DailyTask) -> Unit,
    dailyTasks: List<DailyTask>
) {
    var currentTasks by remember { mutableStateOf(dailyTasks) }
    val completedTaskCount = currentTasks.count { it.isCompleted }
    val totalTaskCount = currentTasks.size
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    // Ekrana göre ölçeklendirme
    val dialogPadding = (screenWidth * 0.03f).coerceIn(12f, 20f).dp
    val titleSize = (screenWidth * 0.05f).coerceIn(20f, 24f).sp
    val headerSize = (screenWidth * 0.04f).coerceIn(16f, 18f).sp
    val messageSize = (screenWidth * 0.035f).coerceIn(14f, 16f).sp
    
    // EventBus'tan task güncellemelerini dinle
    LaunchedEffect(Unit) {
        EventBus.dailyTasks.collect { updatedTasks ->
            currentTasks = updatedTasks
            Log.d("DailyTasksDialog", "Görevler güncellendi: ${updatedTasks.size} görev")
        }
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dialogPadding),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(
                defaultElevation = 8.dp
            ),
            border = BorderStroke(
                width = 2.dp,
                color = Color(0xFFFFA000) // Daha canlı bir sarı çerçeve
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Günlük Görevler",
                        fontWeight = FontWeight.Bold,
                        fontSize = titleSize,
                        color = Color(0xFF333333)
                    )
                    
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Kapat",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { onDismiss() }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // İlerleme bilgisi (Tamamlanan / Toplam)
                Text(
                    text = "$completedTaskCount / $totalTaskCount görev tamamlandı",
                    fontSize = messageSize,
                    color = Color(0xFF666666),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentTasks) { task ->
                        TaskItem(
                            task = task,
                            onClick = { 
                                if (!task.isCompleted) {
                                    // Oyun türü ve gerçek görev ID'si birlikte taşınır.
                                    onTaskClick(task)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TaskItem(
    task: DailyTask,
    onClick: () -> Unit
) {
    val progress = if (task.targetCount > 0) task.currentCount.toFloat() / task.targetCount.toFloat() else 0f
    val isCompleted = task.isCompleted
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp
    
    // Dinamik ölçeklendirme
    val cornerRadius = (screenWidth * 0.03f).coerceIn(12f, 20f).dp
    val iconSize = (screenWidth * 0.05f).coerceIn(20f, 28f).dp
    val textSize = (screenWidth * 0.033f).coerceIn(13f, 16f).sp
    val progressIndicatorSize = (screenWidth * 0.065f).coerceIn(24f, 32f).dp
    val progressTextSize = (screenWidth * 0.025f).coerceIn(9f, 11f).sp
    
    // Görev türüne göre renk ve ikon belirle
    val (iconTint, iconResId) = when (task.gameType) {
        GameType.DIVISION_GAME -> Pair(Color(0xFF2962FF), R.drawable.ic_division_task)
        GameType.ADDITION_GAME -> Pair(Color(0xFF43A047), R.drawable.ic_addition_task)
        GameType.SUBTRACTION_GAME -> Pair(Color(0xFFD81B60), R.drawable.ic_subtraction_task)
        GameType.MULTIPLICATION_GAME -> Pair(Color(0xFF6200EA), R.drawable.ic_multiplication_task)
        GameType.NUMBER_HUNT_GAME -> Pair(Color(0xFFF57C00), R.drawable.ic_matching_task)
        GameType.SHAPE_RECOGNITION -> Pair(Color(0xFF00BCD4), R.drawable.ic_matching_task)
        GameType.MATH_MAZE_GAME -> Pair(Color(0xFF00897B), R.drawable.ic_comparison_task)
        GameType.BALLOON_POP_GAME -> Pair(Color(0xFFFF5252), R.drawable.ic_balloon_task)
        GameType.SPACE_MATH_ADVENTURE -> Pair(Color(0xFF3949AB), R.drawable.ic_spaceship)
        GameType.MARKET_SIMULATOR -> Pair(Color(0xFF8D6E63), R.drawable.ic_shopping_cart)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp)
            .clickable(enabled = !isCompleted) { onClick() },
        shape = RoundedCornerShape(cornerRadius),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCompleted) 0.dp else 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isCompleted) {
                Color(0xFFE8F5E9) // Açık yeşil arka plan - tamamlandı
            } else {
                Color.White
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sol İkon
            Box(
                modifier = Modifier
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(iconSize * 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Görev açıklaması ve ilerleme çubuğu
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = task.description,
                    fontSize = textSize,
                    fontWeight = FontWeight.Medium,
                    color = if (isCompleted) {
                        Color(0xFF388E3C)
                    } else {
                        Color(0xFF424242)
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // İlerleme çubuğu
                LinearProgressIndicator(
                    progress = { progress }, // Lambda olarak geçildi
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isCompleted) {
                        Color(0xFF4CAF50)
                    } else {
                        iconTint
                    },
                    trackColor = if (isCompleted) {
                        Color(0xFFC8E6C9)
                    } else {
                        Color(0xFFEEEEEE)
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Durum göstergesi - tamamlandı işareti veya dairesel ilerleme
            Box(
                modifier = Modifier
                    .size(progressIndicatorSize)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) {
                            Color(0xFF4CAF50) // Tamamlandıysa yeşil
                        } else {
                            iconTint.copy(alpha = 0.2f) // Tamamlanmadıysa ikon renginin açığı
                        }
                    )
                    .border(1.dp, if (isCompleted) Color.Transparent else iconTint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Tamamlandı",
                        tint = Color.White,
                        modifier = Modifier.size(progressIndicatorSize * 0.6f)
                    )
                } else {
                    Text(
                        text = "${task.currentCount}/${task.targetCount}",
                        color = iconTint, // Tamamlanmadıysa ikon rengi
                        fontSize = progressTextSize,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

// Kullanıcı adını almak için yardımcı fonksiyon
private fun getUserName(context: Context): String {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return prefs.getString("user_name", "") ?: ""
}

// Kullanıcı sınıfını almak için yardımcı fonksiyon
private fun getUserGrade(context: Context): String {
    val prefs = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    return prefs.getString("user_class", "1. Sınıf") ?: "1. Sınıf"
}

// Renkli ve Animasyonlu Matematik Macerası Başlığı
@Composable
fun ColorfulAnimatedTitle() {
    val colors = listOf(
        Color(0xFFFFFFFF), // M - Beyaz
        Color(0xFFA5D6A7), // a - Açık Yeşil
        Color(0xFF90CAF9), // t - Açık Mavi
        Color(0xFFFFCC80), // e - Açık Turuncu
        Color(0xFFF48FB1), // m - Açık Pembe
        Color(0xFFB39DDB), // a - Açık Mor
        Color(0xFF81D4FA), // t - Açık Mavi
        Color(0xFFFFAB91), // i - Açık Turuncu/Somon
        Color(0xFFF8BBD0), // k - Açık Pembe
        Color(0xFFFFFFFF), // Boşluk - Beyaz
        Color(0xFFCE93D8), // M - Açık Mor
        Color(0xFFA5D6A7), // a - Açık Yeşil
        Color(0xFF90CAF9), // c - Açık Mavi
        Color(0xFFFFCC80), // e - Açık Turuncu
        Color(0xFFF48FB1), // r - Açık Pembe
        Color(0xFFB39DDB), // a - Açık Mor
        Color(0xFF81D4FA), // s - Açık Mavi
        Color(0xFFFFFFFF)  // ı - Beyaz
    )
    
    val text = "Matematik Macerası"
    
    // Sadece tek bir ana animasyon kullan
    val infiniteTransition = rememberInfiniteTransition(label = "titleAnimation")
    val baseAnimation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "baseAnimation"
    )
    
    Row(
        modifier = Modifier.padding(bottom = 32.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        text.forEachIndexed { index, char ->
            val color = colors[index % colors.size]
            
            // Her karakter için sinüs dalgası şeklinde yukarı-aşağı hareket oluştur
            // Sin fonksiyonu -1 ile 1 arasında değer verir
            val angle = (baseAnimation * 360f + index * 20f) % 360f
            val radians = Math.toRadians(angle.toDouble())
            val offset = (Math.sin(radians) * 4.0).toFloat() // 4dp maksimum hareket
            
            Text(
                text = char.toString(),
                color = color,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.offset(y = offset.dp)
            )
        }
    }
}