package com.yigit033.matematikmacarasi.activities

import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.services.AudioService
import com.yigit033.matematikmacarasi.services.DailyTaskManager
import com.yigit033.matematikmacarasi.services.GameProgressService
import com.yigit033.matematikmacarasi.services.GameStatisticsService
import com.yigit033.matematikmacarasi.services.EventBus
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.services.payments.AdManager
import com.yigit033.matematikmacarasi.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * BaseGameActivity, tüm oyun aktiviteleri için temel sınıftır.
 * Oyun durumu yönetimi, ses efektleri ve ilerleme kaydı gibi ortak işlevleri içerir.
 */
abstract class BaseGameActivity : ComponentActivity() {
    
    // Servisler
    protected lateinit var gameProgressService: GameProgressService
    protected lateinit var audioService: AudioService
    protected lateinit var dailyTaskManager: DailyTaskManager
    protected lateinit var gameStatisticsService: GameStatisticsService
    protected lateinit var starManager: StarManager
    protected lateinit var adManager: AdManager
    
    // Oyun Durumu
    protected var currentLevel by mutableStateOf(1)
    protected var score by mutableStateOf(0)
    protected var stars by mutableStateOf(0)
    protected var isGameRunning by mutableStateOf(false)
    
    // Günlük görev ID'si (eğer varsa - bu doğrudan kullanılmayacak, gameType kullanılacak)
    // protected var dailyTaskId: Int = -1
    
    // Oyunun türünü belirten abstract property
    // Her alt sınıf bu property'yi override etmeli
    abstract val gameType: GameType
    
    // Oyunun başlangıç zamanı
    private var gameStartTime: Long = 0
    
    // Yeni eklenen değişkenler
    private lateinit var handler: Handler
    private lateinit var gameTimer: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // İnternet bağlantısı kontrolü
            if (!NetworkUtils.isInternetAvailable(this)) {
                Log.w("BaseGameActivity", "İnternet bağlantısı yok, oyun başlatılamıyor")
                Toast.makeText(
                    this,
                    "Bu oyunu oynamak için internet bağlantısı gereklidir!",
                    Toast.LENGTH_LONG
                ).show()
                finish()
                return
            }
            
            // Servisleri başlat
            initializeServices()
            
            // Intent'ten görev ID'sini al (varsa) - Artık doğrudan kullanılmıyor
            // dailyTaskId = intent.getIntExtra("daily_task_id", -1)
            
            // Oyunun başlangıç zamanını kaydet
            gameStartTime = System.currentTimeMillis()
            
            setContent {
                GameTheme {
                    // A surface container using the 'background' color from the theme
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        GameContent()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("BaseGameActivity", "onCreate sırasında hata: ${e.message}", e)
            finish() // Aktiviteyi güvenli bir şekilde sonlandır
        }
    }
    
    private fun initializeServices() {
        try {
            gameProgressService = GameProgressService.getInstance(this)
            audioService = AudioService.getInstance(this)
            // DailyTaskManager ve GameStatisticsService getInstance ile alınmalı (singleton)
            dailyTaskManager = DailyTaskManager.getInstance(this)
            gameStatisticsService = GameStatisticsService.getInstance(this) // Singleton kullan
            starManager = StarManager.getInstance(this) // StarManager servisini başlatma
            
            // AdManager'ı başlat
            adManager = AdManager.getInstance(this)
            // Oyun başında interstitial reklamı hazırla
            adManager.loadInterstitialAd()
            
            // Önceki kod:
            // audioService.resetMediaPlayers() // Bu tüm sesleri sıfırlıyordu
            
            // Yeni kod: Sadece efekt seslerini hazırla, müziği etkileme
            audioService.clearEffectSounds()
        } catch (e: Exception) {
            Log.e("BaseGameActivity", "Servisler başlatılırken hata: ${e.message}", e)
            // Hata durumunda aktivitenin devam etmesini engellemek daha güvenli olabilir
             Toast.makeText(this, "Uygulama başlatılırken bir sorun oluştu.", Toast.LENGTH_LONG).show()
             finish()
        }
    }
    
    /**
     * Oyun içeriğini oluşturan Composable fonksiyon.
     * Her oyun alt sınıfı bu fonksiyonu implemente etmelidir.
     */
    @Composable
    abstract fun GameContent()
    
    /**
     * Oyun temasını oluşturan Composable fonksiyon.
     */
    @Composable
    open fun GameTheme(content: @Composable () -> Unit) {
        MaterialTheme(
            content = content
        )
    }
    
    // Temel Oyun Metotları
    
    /**
     * Yeni bir soru oluşturur.
     */
    abstract fun generateQuestion()
    
    /**
     * Kullanıcının cevabını kontrol eder.
     * @param answer Kullanıcının verdiği cevap
     * @return Cevabın doğru olup olmadığı
     */
    abstract fun checkAnswer(answer: Any): Boolean
    
    /**
     * Oyun zorluğunu günceller.
     */
    abstract fun updateDifficulty()
    
    /**
     * Oyunu başlatır.
     */
    protected open fun startGame() { // open yapıldı, gerekirse override edilebilir
        try {
            score = 0
            stars = 0 // Yıldızları da sıfırla
            isGameRunning = true
            gameStartTime = System.currentTimeMillis() // Başlangıç zamanını burada tekrar ayarla
            
            // Önceki kod: 
            // audioService.playBackgroundMusic() // Bu müziği yeniden başlatıyor
            
            // Yeni kod: Müziği devam ettir - eğer çalmıyorsa başlat, çalıyorsa aynı kalsın
            audioService.resumeBackgroundMusic()
            
            generateQuestion()
        } catch (e: Exception) {
            Log.e("BaseGameActivity", "Oyun başlatılırken hata: ${e.message}", e)
            // Hata durumunda kullanıcıya bilgi verilebilir
            Toast.makeText(this, "Oyun başlatılırken bir sorun oluştu.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    /**
     * Oyun sonlandırma metodu, her oyun kendi görevlerine göre override edebilir.
     */
    protected open fun endGame() {
        try {
            Log.d("BaseGameActivity", "Oyun sonlandırılıyor (${gameType.name})")
            
            // Oyun durumu işaretleme
            isGameRunning = false
            
            // Ses kontrolü
            audioService.pauseBackgroundMusic()
            
            // AGRESİF REKLAM STRATEJİSİ - Premium olmayanlar için her oyun sonrası reklam
            try {
                if (this::adManager.isInitialized) {
                    // Yeni metodu kullan - bu her oyun sonrası kontrol eder
                    Log.d("BaseGameActivity", "Agresif reklam stratejisi ile oyun sonu reklamı gösterilecek")
                    adManager.showInterstitialAdAfterGame(this)
                } else {
                    Log.w("BaseGameActivity", "AdManager başlatılmamış, reklam gösterilemiyor")
                }
            } catch (e: Exception) {
                Log.e("BaseGameActivity", "Reklam gösterimi sırasında hata: ${e.message}", e)
            }
            
            // Oyun bitişinde görev ilerlemesini gameType ile güncelle
            if (gameType != null) {
                dailyTaskManager.updateTaskProgressByGameType(gameType)
                Log.d("BaseGameActivity", "Günlük görev güncellendi (gameType: $gameType)")
            }
            
            // Sonuçları kaydet
            saveGameProgress()
            
            // Sistematik istatistik kaydı yapalım 
            recordGameStatistics()
            
            // Yıldızları tüm sayfalarda senkronize et
            syncStarsToAllPages()
            
        } catch (e: Exception) {
            Log.e("BaseGameActivity", "endGame sırasında hata: ${e.message}", e)
        }
    }
    
    /**
     * Oyundan kazanılan yıldızları tüm sayfalarda senkronize eder
     */
    private fun syncStarsToAllPages() {
        try {
            // StarManager'dan güncel yıldız sayısını al
            val currentDisplayStars = starManager.getDisplayStars()
            
            // EventBus aracılığıyla tüm sayfalara yıldız güncellemesi gönder
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Acil bildirim - throttling olmadan
                    EventBus.forceStarNotification(currentDisplayStars)
                    Log.d("BaseGameActivity", "Yıldız senkronizasyonu başarılı: $currentDisplayStars")
                    
                    // Kısa bir gecikme sonrası tekrar bildir (recomposition için)
                    delay(200)
                    EventBus.forceStarNotification(currentDisplayStars)
                    
                    // Tüm sayfaları güncelle (anasayfa, profil sayfası, avatar mağazası)
                    try {
                        EventBus.syncProfileToHomePage(this@BaseGameActivity)
                        Log.d("BaseGameActivity", "Tüm sayfalar için yıldız senkronizasyonu tamamlandı")
                    } catch (e: Exception) {
                        Log.e("BaseGameActivity", "Sayfa senkronizasyonu hatası: ${e.message}", e)
                    }
                } catch (e: Exception) {
                    Log.e("BaseGameActivity", "Yıldız senkronizasyonu hatası: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("BaseGameActivity", "Yıldız senkronizasyonu başlatılamadı: ${e.message}", e)
        }
    }
    
    /**
     * Oyun sonuçlarını ve istatistikleri kaydeder
     */
    protected open fun recordGameStatistics() {
        try {
            // Oyun süresini hesapla (saniye cinsinden)
            val gameTimeSeconds = ((System.currentTimeMillis() - gameStartTime) / 1000).toInt()
            
            // Süreyi doğrula ve sınırla (0-3600 saniye arası)
            val validGameTimeSeconds = when {
                gameTimeSeconds <= 0 -> {
                    Log.w("BaseGameActivity", "Geçersiz oyun süresi: $gameTimeSeconds saniye, 1 saniye olarak ayarlandı")
                    1 // Minimum 1 saniye
                }
                gameTimeSeconds > 3600 -> {
                    Log.w("BaseGameActivity", "Çok uzun oyun süresi: $gameTimeSeconds saniye, 3600 saniye ile sınırlandı")
                    3600 // Maksimum 1 saat (3600 saniye)
                }
                else -> gameTimeSeconds
            }
            
            // İstatistikleri kaydet - tutarlı ID formatı kullan
            val gameId = getNormalizedGameId()
            
            // Detaylı log
            Log.d("BaseGameActivity", "Oyun İstatistikleri Kaydediliyor - ID: $gameId, Seviye: $currentLevel, " +
                    "Skor: $score, Yıldız: $stars, Süre: ${validGameTimeSeconds}s")
            
            // İstatistik servisi ile kaydediyoruz
            gameStatisticsService.recordGameCompletion(
                gameType = gameId,
                score = score,
                stars = stars,
                timeSpentSeconds = validGameTimeSeconds
            )
            
            // Log çıktısı
            Log.d("BaseGameActivity", "İstatistikler kaydedildi: $gameId - $score puan, $stars yıldız, ${validGameTimeSeconds}s süre")
        } catch (e: Exception) {
            Log.e("BaseGameActivity", "Oyun istatistikleri kaydedilirken hata: ${e.message}", e)
        }
    }
    
    /**
     * Oyunun benzersiz ID'sini oluşturur
     * Seviye bazlı oyunlar için _level_X formatını destekler
     */
    protected open fun getNormalizedGameId(): String {
        return if (isBasicOperationGame(gameType)) {
            // Temel işlem oyunları: addition_game_level_1, subtraction_game_level_2 gibi
            val baseGameId = when (gameType) {
                GameType.ADDITION_GAME -> "addition_game"
                GameType.SUBTRACTION_GAME -> "subtraction_game"
                GameType.MULTIPLICATION_GAME -> "multiplication_game"
                GameType.DIVISION_GAME -> "division_game"
                else -> gameType.toString().lowercase().replace("_game", "")
            }
            
            // Seviye bazlı ID oluştur
            "${baseGameId}_level_$currentLevel"
        } else {
            // İnteraktif oyunlar: Aktivite adı ve seviye
            val activityId = gameStatisticsService.getGameIdString(gameType)
            if (currentLevel > 1) {
                // Eğer seviye 1'den büyükse, seviye bilgisini ekle
                "${activityId}_level_$currentLevel"
            } else {
                // Varsayılan seviye için sadece aktivite adı
                activityId
            }
        }
    }
    
    /**
     * Oyun ilerleme bilgilerini kaydeder
     */
    protected open fun saveGameProgress() { 
        try {
            // Normalize edilmiş oyun ID'sini al
            val gameId = getNormalizedGameId() 
            
            // GameProgressService ile ilerlemeyi kaydet
            gameProgressService.saveProgress(gameId, currentLevel, score, stars)
            Log.d("BaseGameActivity", "Oyun ilerlemesi kaydedildi: ID=$gameId, Seviye=$currentLevel, Skor=$score, Yıldız=$stars")
        } catch (e: Exception) {
            Log.e("BaseGameActivity", "Oyun ilerlemesi kaydedilirken hata: ${e.message}", e)
        }
    }
    
    /**
     * Verilen oyun türünün temel işlem oyunu olup olmadığını kontrol eder.
     */
    private fun isBasicOperationGame(gameType: GameType): Boolean {
        return gameType == GameType.ADDITION_GAME ||
               gameType == GameType.SUBTRACTION_GAME ||
               gameType == GameType.MULTIPLICATION_GAME ||
               gameType == GameType.DIVISION_GAME
    }
    
    /**
     * Puan hesaplar.
     */
    protected open fun calculatePoints(difficulty: Int = currentLevel): Int { // open yapıldı
        // Basit puanlama, oyunlar override edebilir
        return 10 * difficulty
    }
    
    override fun onPause() {
        super.onPause()
        try {
            if (isGameRunning) { // Sadece oyun çalışıyorsa duraklat
                audioService.pauseBackgroundMusic()
            }
        } catch (e: Exception) {
            Log.w("BaseGameActivity", "onPause sırasında ses duraklatılamadı", e)
        }
    }
    
    override fun onResume() {
        super.onResume()
        try {
            // Önceki kod: audioService.resetMediaPlayers() - bu tüm sesleri sıfırlıyordu
            
            // Sadece efekt seslerini sıfırla, müziği değil
            audioService.clearEffectSounds()
            
            // Her durumda müziği devam ettir, oyundan ana ekrana döndüğümüzde bile çalacak
            audioService.resumeBackgroundMusic()
        } catch (e: Exception) {
            Log.w("BaseGameActivity", "onResume sırasında ses başlatılamadı", e)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        try {
            // Singleton servislerin release edilmemesi önemli
            // Sadece bu aktiviteye özgü kaynaklar temizlenmeli (varsa)
            if (!isChangingConfigurations) {
                 // Ana ekrana dönüşte arka plan müziğini durdurmuyoruz,
                 // sadece efekt seslerini sıfırlıyoruz
                 
                 // Yeni eklediğimiz metodu kullanarak sadece efekt seslerini temizle
                 try {
                     audioService.clearEffectSounds()
                     // AdManager temizliği
                     if (this::adManager.isInitialized) {
                         adManager.destroy()
                     }
                     Log.d("BaseGameActivity", "onDestroy: Ses efektleri ve reklam kaynakları temizlendi, müzik devam ediyor.")
                 } catch (e: Exception) {
                     Log.e("BaseGameActivity", "Kaynaklar temizlenirken hata: ${e.message}")
                 }
            }
        } catch (e: Exception) {
            Log.w("BaseGameActivity", "onDestroy sırasında hata", e)
        }
    }
} 