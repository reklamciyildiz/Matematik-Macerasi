package com.yigit033.matematikmacarasi.games.basic

import android.app.Dialog
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.activities.BaseGameActivity
import androidx.compose.ui.platform.ComposeView
import com.yigit033.matematikmacarasi.GameType
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import com.yigit033.matematikmacarasi.games.basic.EnhancedMathGameScreen
import com.yigit033.matematikmacarasi.services.DailyTaskManager
import com.yigit033.matematikmacarasi.ui.components.GameTopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.max
import kotlin.random.Random
import com.yigit033.matematikmacarasi.games.basic.AgeGroup
import com.yigit033.matematikmacarasi.games.basic.getDifficultyAgeGroup
import com.yigit033.matematikmacarasi.models.DifficultyLevel
import com.yigit033.matematikmacarasi.games.models.QuestionTracker

/**
 * MultiplicationGameActivity, çarpma oyunu ekranını yönetir.
 * Kullanıcıya iki sayı gösterilir ve çarpımını hesaplaması istenir.
 */
class MultiplicationGameActivity : BaseGameActivity() {
    
    // BaseGameActivity'den gelen abstract property'yi override et
    override val gameType: GameType = GameType.MULTIPLICATION_GAME
    
    // Oyun durumu
    private var firstNumber by mutableStateOf(0)
    private var secondNumber by mutableStateOf(0)
    private var correctAnswer by mutableStateOf(0)
    private var questionCount by mutableStateOf(0)
    private var maxQuestions by mutableStateOf(10)
    private var timeLimit by mutableStateOf(15) // Saniye cinsinden süre limiti
    
    // Oyun istatistikleri
    private var correctAnswersCount by mutableStateOf(0)
    private var wrongAnswersCount by mutableStateOf(0)
    
    // Cevap kontrol durum değişkenleri
    private var answerFeedbackState by mutableStateOf(AnswerFeedbackState.IDLE)
    private var lastSelectedOption by mutableStateOf("")
    
    // Çoktan seçmeli cevap seçenekleri
    private var options by mutableStateOf<List<String>>(emptyList())
    
    // Oyun ekranı durumları
    private var showDifficultySelection by mutableStateOf(true)
    private var showGameSummary by mutableStateOf(false) // Oyun özet ekranını göster/gizle
    
    // Seçilen zorluk seviyesi
    private var selectedDifficulty by mutableStateOf(DifficultyLevel.EASY)
    
    /**
     * Aktivite ömrünü kontrol etmek için kaydedilen flag
     */
    private var isGameEnding = false
    
    // Hata durumu için bir state tanımlayalım
    private var hasError by mutableStateOf(false)
    
    // Sorulan soruları takip eden tracker
    private val questionTracker = QuestionTracker()
    
    // Maksimum yeniden deneme sayısı
    private val MAX_RETRY_COUNT = 20
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Başlangıçta oyunu başlatmıyoruz, zorluk seviyesi seçimini bekliyoruz
    }
    
    @Composable
    override fun GameContent() {
        // Compose'ta try-catch kullanılamaz, ancak hata durumları için logic ekleyebiliriz
        when {
            showGameSummary -> {
                // Oyun özet ekranını göster
                GameCompletionScreen(
                    score = score,
                    correctAnswers = correctAnswersCount,
                    wrongAnswers = wrongAnswersCount,
                    currentLevel = currentLevel,
                    maxLevel = 5,
                    onPlayAgain = {
                        // Aynı seviyeyi tekrar başlat
                        try {
                        showGameSummary = false
                        isGameEnding = false
                        score = 0
                        questionCount = 0
                        correctAnswersCount = 0
                        wrongAnswersCount = 0
                        startGame()
                        } catch (e: Exception) {
                            // Hata oluşursa kaydet ve kullanıcıya bildir
                            Log.e("MultiplicationGameActivity", "Oyun yeniden başlatılırken hata: ${e.message}")
                            e.printStackTrace()
                            Toast.makeText(
                                this@MultiplicationGameActivity,
                                "Oyun yeniden başlatılamadı. Lütfen ana menüye dönün.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    onMainMenu = {
                        // Ana menüye dön
                        try {
                            Intent(this@MultiplicationGameActivity, Class.forName("com.yigit033.matematikmacarasi.MainActivity")).also { intent ->
                                // FLAG_ACTIVITY_CLEAR_TOP yerine daha nazik bayraklar kullanıyoruz
                                // Bu, MainActivity'nin yeniden oluşturulmasını değil, öne getirilmesini sağlar
                                intent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                
                                // Müziğin devam etmesi için extra bilgi ekleyelim
                                intent.putExtra("continue_music", true)
                                
                                startActivity(intent)
                                
                                // Mevcut aktiviteyi temiz bir şekilde kapat
                                // Önce ses efektlerini nazikçe temizleyelim
                                try {
                                    audioService.clearEffectSounds()
                                } catch (e: Exception) {
                                    Log.w("MultiplicationGameActivity", "Ses efektleri temizlenirken hata: ${e.message}")
                                }
                                
                                finish() // Mevcut aktiviteyi kapat
                            }
                        } catch (e: Exception) {
                            // Hata oluşursa güvenli bir şekilde aktiviteyi kapat
                            Log.e("MultiplicationGameActivity", "Ana menüye dönülürken hata: ${e.message}")
                            e.printStackTrace()
                            finish() // En azından mevcut aktiviteyi kapat
                        }
                    },
                    onNextLevel = {
                        // Sonraki seviyeye geç
                        try {
                        showGameSummary = false
                        isGameEnding = false
                        if (currentLevel < 5) {
                            val nextLevel = when (selectedDifficulty) {
                                DifficultyLevel.EASY -> DifficultyLevel.MEDIUM
                                DifficultyLevel.MEDIUM -> DifficultyLevel.HARD
                                DifficultyLevel.HARD -> DifficultyLevel.VERY_HARD
                                DifficultyLevel.VERY_HARD -> DifficultyLevel.EXPERT
                                DifficultyLevel.EXPERT -> DifficultyLevel.EXPERT
                            }
                            
                            // Yeni seviyeyi başlat
                            score = 0
                            questionCount = 0
                            correctAnswersCount = 0
                            wrongAnswersCount = 0
                            selectedDifficulty = nextLevel
                            
                            // Yeni seviye ile oyunu başlat
                            initializeGameWithDifficulty(nextLevel)
                            }
                        } catch (e: Exception) {
                            // Hata oluşursa kaydet ve kullanıcıya bildir
                            Log.e("MultiplicationGameActivity", "Sonraki seviyeye geçilirken hata: ${e.message}")
                            e.printStackTrace()
                            Toast.makeText(
                                this@MultiplicationGameActivity,
                                "Sonraki seviyeye geçilemedi. Lütfen ana menüye dönün.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            }
            showDifficultySelection -> {
                // Zorluk seviyesi seçim ekranı
                DifficultySelectionScreen(
                    title = "Çarpma İşlemi",
                    onDifficultySelected = { difficulty ->
                        try {
                        selectedDifficulty = difficulty
                        showDifficultySelection = false // Zorluk seviyesi seçildi, oyunu başlat
                        initializeGameWithDifficulty(difficulty) // Zorluk seviyesine göre oyunu başlat
                        } catch (e: Exception) {
                            // Hata oluşursa kaydet ve kullanıcıya bildir
                            Log.e("MultiplicationGameActivity", "Zorluk seviyesi seçilirken hata: ${e.message}")
                            e.printStackTrace()
                            
                            // Varsayılan zorluk seviyesi ile devam et
                            showDifficultySelection = false
                            initializeGameWithDifficulty(DifficultyLevel.EASY)
                            
                            Toast.makeText(
                                this@MultiplicationGameActivity,
                                "Bir hata oluştu, kolay seviye ile devam ediliyor.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
            else -> {
                // Ana oyun ekranı
                val questionText = "Çarpma İşlemleri"
                val operationText = "$firstNumber × $secondNumber = ?"
                
                // options'ın boş olup olmadığını kontrol et
                val safeOptions = if (options.isEmpty()) {
                    listOf("0", "1", "2", "3") // Fallback seçenekler
                } else {
                    options
                }
                
                // Öğrencinin yaşını kontrol et (intent üzerinden gelen veya varsayılan değer)
                val studentAge = intent.getIntExtra("student_age", 7) // Varsayılan yaş 7 olsun
                
                // Her durumda gelişmiş uzay temalı ekranı kullan
                EnhancedMathGameScreen(
                    questionText = questionText,
                    operationText = operationText,
                    options = safeOptions,
                    currentQuestionIndex = questionCount,
                    totalQuestions = maxQuestions,
                    score = score,
                    timeLimit = timeLimit,
                    onAnswerSelected = { selectedAnswer ->
                        try {
                            checkUserAnswer(selectedAnswer)
                        } catch (e: Exception) {
                            // Hata oluşursa kaydet ve devam et
                            Log.e("MultiplicationGameActivity", "Cevap kontrol edilirken hata: ${e.message}")
                            e.printStackTrace()
                            
                            // Bir sonraki soruya geç
                            android.os.Handler(mainLooper).postDelayed({
                                try {
                                    generateQuestion()
                                } catch (e: Exception) {
                                    // Soru oluşturulamazsa oyunu bitir
                                    Log.e("MultiplicationGameActivity", "Soru oluşturulurken hata: ${e.message}")
                                    e.printStackTrace()
                                    handleGameEnd()
                                }
                            }, 1000)
                        }
                    },
                    gameTitle = "Çarpma İşlemini Çöz",
                    feedbackState = answerFeedbackState,
                    correctAnswerText = correctAnswer.toString(),
                    ageGroup = getDifficultyAgeGroup(selectedDifficulty)
                )
            }
        }
        
        // Hata durumunda gösterilecek ekran için bir state kullanabiliriz
        // Bu şekilde Compose kurallarına uygun bir yaklaşım elde ederiz
        if (hasError) {
            // Basit bir hata ekranı göster
            Column(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text(
                    text = "Oyun yüklenirken bir hata oluştu",
                    fontSize = 20.sp,
                    color = androidx.compose.ui.graphics.Color.Red,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                
                androidx.compose.material3.Button(
                    onClick = {
                        finish() // Aktiviteyi kapat
                    }
                ) {
                    Text("Ana Menüye Dön")
                }
            }
        }
    }
    
    /**
     * Oyun özetini gösterir ve ardından oyunu sonlandırır
     */
    private fun showGameSummary() {
        try {
            // Burada artık dialog açmak yerine doğrudan aktivite içeriğini değiştiriyoruz
            showGameSummary = true
            
        } catch (e: Exception) {
            // Hata oluşursa loglayalım ve güvenli bir şekilde oyunu bitirelim
            Log.e("MultiplicationGameActivity", "Oyun özeti gösterilirken hata: ${e.message}")
            e.printStackTrace()
            
            // Hatayı kullanıcıya bildir
            Toast.makeText(
                this, 
                "Oyun özeti gösterilirken bir hata oluştu. Lütfen tekrar deneyin.", 
                Toast.LENGTH_LONG
            ).show()
            
            // Ana menüye dön
            try {
                super.endGame()
            } catch (e: Exception) {
                finish() // En kötü durumda aktiviteyi kapat
            }
        }
    }
    
    /**
     * Oyun sonu işlemlerini yöneten metod
     */
    private fun handleGameEnd() {
        // Eğer oyun henüz bitmemişse
        if (!isGameEnding) {
            isGameEnding = true
            
            try {
                // Tamamlanan oyun sayısını artır
                gameStatisticsService.incrementCompletedGames(gameType)
                
                // Günlük görev ilerlemesini güncelle - GameType kullanarak daha güvenilir
                try {
                    // DailyTaskManager'i kullanarak görev ilerlemesini gameType ile güncelle
                    dailyTaskManager.updateTaskProgressByGameType(gameType)
                    Log.d("MultiplicationGameActivity", "Günlük görev güncellendi (gameType: $gameType)")
                } catch (e: Exception) {
                    Log.e("MultiplicationGameActivity", "Günlük görev güncellenirken hata: ${e.message}")
                    e.printStackTrace()
                }
                
                // Yıldız sistemi entegrasyonu
                try {
                    // Doğrudan StarManager'ı kullan
                    val starManager = com.yigit033.matematikmacarasi.services.StarManager.getInstance(this)
                    
                    // Oyun sonuçlarını işle ve yıldızları kaydet
                    val gameId = "multiplication_game_${currentLevel}"
                    
                    // Yıldız hesaplama
                    val percentage = (correctAnswersCount.toFloat() / maxQuestions.toFloat()) * 100
                    val earnedStars = when {
                        percentage >= 90 -> 3 // Mükemmel performans: 3 yıldız
                        percentage >= 70 -> 2 // İyi performans: 2 yıldız
                        percentage >= 50 -> 1 // Ortalama performans: 1 yıldız
                        else -> 0 // Zayıf performans: yıldız yok
                    }
                    
                    // Yıldızları ekle
                    if (earnedStars > 0) {
                        val success = starManager.addStars(gameId, earnedStars)
                        Log.d("MultiplicationGameActivity", "Yıldız ekleme: $success (Oyun: $gameId, Yıldız: $earnedStars)")
                        
                        // Daha detaylı bir Toast mesajı
                        val totalStars = starManager.getTotalStars()
                        Toast.makeText(
                            this,
                            "$earnedStars yıldız kazandınız! Toplam yıldızınız: $totalStars",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("MultiplicationGameActivity", "Yıldız ekleme işlemi sırasında hata: ${e.message}")
                    e.printStackTrace()
                }
                
                // Geçiş reklamı göster - BaseGameActivity'deki gibi
                try {
                    Log.d("MultiplicationGameActivity", "Oyun sonu reklamı gösteriliyor")
                    // super yoluyla BaseGameActivity'deki adManager'a erişim
                    super.endGame()
                } catch (e: Exception) {
                    Log.e("MultiplicationGameActivity", "Reklam gösterimi sırasında hata: ${e.message}", e)
                }
                
                // Özet ekranını göster, bu ekrandan kullanıcı ana menüye dönebilir
                showGameSummary()
                
                // BaseGameActivity'nin endGame metodunu çağırmayarak,
                // kontrolü ele alıyoruz ve özet ekranındaki butonlarla
                // kullanıcının ne yapmak istediğine göre yönlendiriyoruz
            } catch (e: Exception) {
                Log.e("MultiplicationGameActivity", "Oyun sonlandırılırken hata: ${e.message}")
                e.printStackTrace()
                
                // En güvenli yol - aktiviteyi kapat
                try {
                    super.endGame() // Temel endGame metodunu çağır
                } catch (e: Exception) {
                    e.printStackTrace()
                    
                    // Dahi çalışmazsa direkt kapat
                    finish()
                }
            }
        }
    }
    
    private fun initializeGameWithDifficulty(difficulty: DifficultyLevel) {
        try {
        // Zorluk seviyesine göre oyun parametrelerini ayarla
        when (difficulty) {
            DifficultyLevel.EASY -> {
                currentLevel = 1
                    timeLimit = 15
                    maxQuestions = 10
            }
            DifficultyLevel.MEDIUM -> {
                currentLevel = 2
                    timeLimit = 12
                    maxQuestions = 10
            }
            DifficultyLevel.HARD -> {
                currentLevel = 3
                    timeLimit = 10
                    maxQuestions = 12
            }
            DifficultyLevel.VERY_HARD -> {
                currentLevel = 4
                    timeLimit = 8
                    maxQuestions = 15
            }
            DifficultyLevel.EXPERT -> {
                currentLevel = 5
                    timeLimit = 6
                    maxQuestions = 15
                }
        }
        
        // Oyun parametrelerini sıfırla
        score = 0
        questionCount = 0
        
        // Oyunu başlat
        startGame()
        } catch (e: Exception) {
            Log.e("MultiplicationGameActivity", "Oyun başlatılırken hata: ${e.message}")
            e.printStackTrace()
            
            // Varsayılan değerlerle devam et
            currentLevel = 1
            timeLimit = 15
            maxQuestions = 10
            score = 0
            questionCount = 0
            
            // Yine de oyunu başlatmayı dene
            try {
                startGame()
            } catch (e: Exception) {
                // Başlatılamazsa kullanıcıya bildir
                Toast.makeText(
                    this,
                    "Oyun başlatılamadı. Lütfen tekrar deneyin.",
                    Toast.LENGTH_LONG
                ).show()
                
                // Ana menüye dön
                Intent(this, Class.forName("com.yigit033.matematikmacarasi.MainActivity")).also { intent ->
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
                }
            }
        }
    }
    
    override fun generateQuestion() {
        try {
            // Eğer tüm sorular tamamlandıysa, oyunu sonlandır
            if (questionCount >= maxQuestions) {
                handleGameEnd()
                return
            }
            
            // Soru oluşturma ve kontrol için değişkenler
            var isNewQuestion = false
            var retryCount = 0
            
            // Benzersiz bir soru bulana kadar veya maksimum deneme sayısına ulaşana kadar devam et
            while (!isNewQuestion && retryCount < MAX_RETRY_COUNT) {
                // Zorluk seviyesine göre sayı aralıklarını belirle
                when (currentLevel) {
                    1 -> { // Seviye 1 (6-7 yaş / 1-2. Sınıf)
                        // Temel çarpma faktörleri ve sayı gruplarına giriş
                        firstNumber = (1..3).random()
                        secondNumber = (1..5).random()
                        
                        // Çarpım sonucu maksimum 15 olacak şekilde kontrol et
                        if (firstNumber * secondNumber > 15) {
                            // Eğer 15'ten büyükse, sayıları küçült
                            if (firstNumber > 2) firstNumber = 2
                            if (secondNumber > 5) secondNumber = 5
                        }
                    }
                    2 -> { // Seviye 2 (7-9 yaş / 2-3. Sınıf)
                        // Temel çarpım tablosu çalışmaları
                        firstNumber = (2..5).random()
                        secondNumber = (2..5).random()
                        
                        // 2'li, 3'lü, 4'lü ve 5'li çarpım tabloları odaklı
                        // Kolay hatırlanabilir kalıplar için belirli kombinasyonlar
                        if (Math.random() < 0.3) {
                            // 2'li çarpım tablosu vurgusu
                            if (Math.random() < 0.5) {
                                firstNumber = 2
                                secondNumber = (1..10).random()
                            } else {
                                firstNumber = (1..10).random()
                                secondNumber = 2
                            }
                        } else if (Math.random() < 0.3) {
                            // 5'li çarpım tablosu vurgusu
                            if (Math.random() < 0.5) {
                                firstNumber = 5
                                secondNumber = (1..10).random()
                            } else {
                                firstNumber = (1..10).random()
                                secondNumber = 5
                            }
                        }
                    }
                    3 -> { // Seviye 3 (9-10 yaş / 3-4. Sınıf)
                        // Genişletilmiş çarpım tablosu
                        firstNumber = (2..9).random()
                        secondNumber = (2..9).random()
                        
                        // 10 ile çarpma kuralı entegrasyonu
                        if (Math.random() < 0.2) {
                            // 10 ile çarpma pratikleri
                            if (Math.random() < 0.5) {
                                firstNumber = 10
                                secondNumber = (1..10).random()
                            } else {
                                firstNumber = (1..10).random()
                                secondNumber = 10
                            }
                        }
                    }
                    4 -> { // Seviye 4 (10-12 yaş / 4-6. Sınıf)
                        // İki basamaklı sayıyı tek basamaklı sayıyla çarpma
                        firstNumber = (10..30).random()
                        secondNumber = (2..9).random()
                        
                        // 10'un katlarıyla çarpma pratikleri
                        if (Math.random() < 0.3) {
                            // 10'un katlarını vurgula
                            firstNumber = (1..3).random() * 10
                        }
                    }
                    5 -> { // Seviye 5 (12-14 yaş / 6-8. Sınıf)
                        // İki basamaklı sayıları çarpma - aşamalı zorluk artışı
                        val progressPercentage = (questionCount.toFloat() / maxQuestions.toFloat()) * 100

                        when {
                            progressPercentage < 30 -> {
                                // Aşama 1 (İlk %30 soru): Daha basit iki basamaklı çarpımlar
                                firstNumber = (10..25).random()
                                secondNumber = (10..15).random()
                            }
                            progressPercentage < 70 -> {
                                // Aşama 2 (Sonraki %40 soru): Orta zorlukta iki basamaklı çarpımlar
                                firstNumber = (15..40).random()
                                secondNumber = (10..20).random()
                            }
                            else -> {
                                // Aşama 3 (Son %30 soru): Daha zor iki basamaklı çarpımlar
                                firstNumber = (20..50).random()
                                secondNumber = (15..30).random()
                            }
                        }
                    }
                }
                
                correctAnswer = firstNumber * secondNumber
                
                // Sonuç çok büyük olmasın - eğer 1000'den büyükse, sayıları küçült
                if (correctAnswer > 1000) {
                    // Seviyeye göre uygun bir şekilde küçült
                    when (currentLevel) {
                        1, 2 -> {
                            firstNumber = (1..5).random()
                            secondNumber = (1..5).random()
                        }
                        3 -> {
                            firstNumber = (2..9).random()
                            secondNumber = (2..9).random()
                        }
                        4 -> {
                            firstNumber = (10..20).random()
                            secondNumber = (2..5).random()
                        }
                        else -> {
                            firstNumber = (10..30).random()
                            secondNumber = (5..15).random()
                        }
                    }
                    correctAnswer = firstNumber * secondNumber
                }
                
                // Bu sorunun daha önce sorulup sorulmadığını kontrol et
                isNewQuestion = !questionTracker.isOperationAsked(firstNumber, secondNumber, "×")
                
                // Sayıların yer değiştirdiği durumu da kontrol et (örn. 5×3 ile 3×5 aynı sorudur)
                if (isNewQuestion && firstNumber != secondNumber) {
                    isNewQuestion = !questionTracker.isOperationAsked(secondNumber, firstNumber, "×")
                }
                
                retryCount++
            }
            
            // Maksimum deneme sayısına ulaşıldıysa, soru sayısını arttırma ve yeni bir soru üretme
            if (retryCount >= MAX_RETRY_COUNT) {
                Log.w("MultiplicationGameActivity", "Maksimum deneme sayısına ulaşıldı, benzersiz soru bulunamadı")
                // Deneme sayısına ulaşılsa bile bu soruyu kaydediyoruz ki tekrar denemeyelim
                questionTracker.isOperationAsked(firstNumber, secondNumber, "×")
            }
            
            // Cevap seçeneklerini oluştur
            generateAnswerOptions()
            
            // Cevap durumunu sıfırla
            answerFeedbackState = AnswerFeedbackState.IDLE
            lastSelectedOption = ""
            
            // Soru sayısını artır
            questionCount++
            
        } catch (e: Exception) {
            Log.e("MultiplicationGameActivity", "Soru oluşturulurken hata: ${e.message}")
            e.printStackTrace()
            
            // Basit bir soru oluşturarak devam et
            try {
                firstNumber = (2..5).random()
                secondNumber = (2..5).random()
                correctAnswer = firstNumber * secondNumber
                
                // Tracker'a ekle
                questionTracker.isOperationAsked(firstNumber, secondNumber, "×")
                
                // Basit cevap seçenekleri
                options = listOf(
                    correctAnswer.toString(),
                    (correctAnswer + 1).toString(),
                    (correctAnswer + 2).toString(),
                    (correctAnswer - 1).toString()
                ).shuffled()
                
                // Cevap durumunu sıfırla
                answerFeedbackState = AnswerFeedbackState.IDLE
                lastSelectedOption = ""
                
                // Soru sayısını artır
                questionCount++
            } catch (e: Exception) {
                // Bu noktada bile başarısız olursa, oyunu sonlandır
                Log.e("MultiplicationGameActivity", "Basit soru oluşturulurken de hata: ${e.message}")
                e.printStackTrace()
                handleGameEnd()
            }
        }
    }
    
    /**
     * Çoktan seçmeli cevap seçeneklerini oluşturur
     * Doğru cevap ve yanıltıcı 3 cevap daha ekler
     */
    private fun generateAnswerOptions() {
        try {
        val optionsList = mutableListOf<String>()
        
        // Doğru cevabı ekle
        optionsList.add(correctAnswer.toString())
        
            // Yanlış seçenekler ekle - 10 deneme ile sınırla
            var attempts = 0
            while (optionsList.size < 4 && attempts < 10) {
                attempts++
                try {
                    // Yaş seviyesine göre uyarlanmış yanıltıcı seçenekler oluştur
                    val offset = when (currentLevel) {
                        1 -> { // Kolay - Anasınıfı ve 1. Sınıf
                            // Bariz hatalar - yakın veya benzer sayılar
                            listOf(
                                1,                                      // +1 (bariz hata)
                                -1,                                     // -1 (bariz hata)
                                firstNumber + secondNumber - correctAnswer, // Toplama yapmış
                                (firstNumber * 10) - correctAnswer      // 10 ile çarpmış
                            ).random()
                        }
                        2 -> { // Orta - 2-3. Sınıf
                            // Çarpım tablosu hataları
                            if (Math.random() < 0.5) {
                                // +/- birkaç sayı
                                (1..5).random() * if (Math.random() < 0.5) 1 else -1
                            } else {
                                // Yaygın çarpım hatası - komşu sayıların çarpımı
                                val wrongFirst = if (Math.random() < 0.5) firstNumber + 1 else firstNumber - 1
                                val wrongSecond = if (Math.random() < 0.5) secondNumber + 1 else secondNumber
                                (wrongFirst * wrongSecond) - correctAnswer
                            }
                        }
                        3 -> { // Zor - 4-5. Sınıf
                            // Çarpım hataları
                            if (Math.random() < 0.3) {
                                // Basamak hatası - onlar basamağında hata
                                if (correctAnswer > 10) {
                                    val digitsOff = (1..3).random()
                                    digitsOff * 10 * if (Math.random() < 0.5) 1 else -1
                                } else {
                                    // Küçük sayılarda +/- bir miktar
                                    (2..6).random() * if (Math.random() < 0.5) 1 else -1
                                }
                            } else if (Math.random() < 0.3) {
                                // İşlem hatası - toplama yapmış
                                (firstNumber + secondNumber) - correctAnswer
                            } else {
                                // Yaygın çarpım hatası - bir basamağı eksik çarpmış
                                if (firstNumber > 9) {
                                    val firstDigit = firstNumber / 10
                                    val secondDigit = firstNumber % 10
                                    (secondDigit * secondNumber) - correctAnswer
                                } else if (secondNumber > 9) {
                                    val firstDigit = secondNumber / 10
                                    val secondDigit = secondNumber % 10
                                    (firstNumber * secondDigit) - correctAnswer
                                } else {
                                    // Her iki sayı da tek basamaklıysa, yakın bir hata
                                    (correctAnswer + (5..10).random() * if (Math.random() < 0.5) 1 else -1) - correctAnswer
                                }
                            }
                        }
                        4 -> { // Çok Zor - 6-7. Sınıf
                            // Karmaşık çarpım hataları
                            if (Math.random() < 0.4) {
                                // İki basamaklı çarpımda basamak hatası
                                if (correctAnswer > 100) {
                                    // Onlar basamağında kayma
                                    val shift = 10 * (1..2).random()
                                    shift * if (Math.random() < 0.5) 1 else -1
                                } else {
                                    // Küçük sayılarda rastgele
                                    (5..15).random() * if (Math.random() < 0.5) 1 else -1
                                }
                            } else if (Math.random() < 0.3) {
                                // Sadece birler basamağını çarpmış, onlar basamağını toplamış
                                if (firstNumber > 9 && secondNumber > 9) {
                                    val firstOnes = firstNumber % 10
                                    val firstTens = firstNumber / 10
                                    val secondOnes = secondNumber % 10
                                    val secondTens = secondNumber / 10
                                    
                                    ((firstOnes * secondOnes) + (firstTens + secondTens) * 10) - correctAnswer
                                } else {
                                    // Biri tek basamaklıysa, normal bir hata
                                    (correctAnswer + (10..20).random() * if (Math.random() < 0.5) 1 else -1) - correctAnswer
                                }
                            } else {
                                // Normal bir hata
                                (10..20).random() * if (Math.random() < 0.5) 1 else -1
                            }
                        }
                        else -> { // Uzman - 8. Sınıf ve üstü
                            // Çok karmaşık çarpım hataları
                            if (Math.random() < 0.3) {
                                // Farklı basamakları yanlış çarpma
                                if (firstNumber > 9 && secondNumber > 9) {
                                    val firstOnes = firstNumber % 10
                                    val firstTens = firstNumber / 10
                                    val secondOnes = secondNumber % 10
                                    val secondTens = secondNumber / 10
                                    
                                    // Çapraz çarpım hatası
                                    ((firstOnes * secondTens * 10) + (firstTens * secondOnes * 10)) - correctAnswer
                                } else {
                                    // Biraz daha basit bir hata
                                    (20..30).random() * if (Math.random() < 0.5) 1 else -1
                                }
                            } else if (Math.random() < 0.3) {
                                // İki basamaklı çarpımda hata
                                if (correctAnswer > 200) {
                                    // Büyük sayılarda yüzler basamağında hata
                                    100 * (1..3).random() * if (Math.random() < 0.5) 1 else -1
                                } else {
                                    // Küçük sayılarda onlar basamağında hata
                                    10 * (1..5).random() * if (Math.random() < 0.5) 1 else -1
                                }
                            } else {
                                // Karmaşık çarpma işleminde normal hata
                                (15..40).random() * if (Math.random() < 0.5) 1 else -1
                            }
                        }
                    }
                    
                    val wrongAnswer = correctAnswer + offset
                    
                    // Geçerli bir sayı mı kontrol et (negatif olmasın)
                    if (wrongAnswer > 0 && !optionsList.contains(wrongAnswer.toString())) {
                        optionsList.add(wrongAnswer.toString())
                    }
                } catch (e: Exception) {
                    Log.e("MultiplicationGameActivity", "Yanıltıcı seçenek oluşturulurken hata: ${e.message}")
                    // Bu yanıltıcıyı atla ve devam et
                    continue
                }
            }
            
            // Eğer hala 4 seçenek elde edemediyse, basit seçenekler ekle
            while (optionsList.size < 4) {
                val simpleOffset = listOf(1, 2, 3, 4, 5, -1, -2, -3).random()
                val simpleWrong = correctAnswer + simpleOffset
                
                if (!optionsList.contains(simpleWrong.toString()) && simpleWrong > 0) {
                    optionsList.add(simpleWrong.toString())
                }
        }
        
        // Seçenekleri karıştır
        optionsList.shuffle()
        
        options = optionsList
        } catch (e: Exception) {
            Log.e("MultiplicationGameActivity", "Cevap seçenekleri oluşturulurken hata: ${e.message}")
            e.printStackTrace()
            
            // Basit seçenekler oluştur
            try {
                val simple = listOf(
                    correctAnswer.toString(),
                    (correctAnswer + 1).toString(),
                    (correctAnswer + 2).toString(),
                    (correctAnswer - 1).toString()
                ).shuffled()
                
                options = simple
            } catch (e: Exception) {
                // Son çare olarak en basit seçenekleri oluştur
                options = listOf("0", "1", "2", "3")
            }
        }
    }
    
    override fun checkAnswer(answer: Any): Boolean {
        try {
        return when (answer) {
            is String -> answer == correctAnswer.toString()
            is Int -> answer == correctAnswer
            else -> false
            }
        } catch (e: Exception) {
            Log.e("MultiplicationGameActivity", "Cevap kontrolü sırasında hata: ${e.message}")
            e.printStackTrace()
            return false // Hata durumunda yanlış kabul et
        }
    }
    
    private fun checkUserAnswer(selectedOption: String) {
        try {
        lastSelectedOption = selectedOption
        
        if (selectedOption.isNotEmpty()) {
            val isCorrect = checkAnswer(selectedOption)
            
            // Cevap durumunu güncelle
            answerFeedbackState = if (isCorrect) {
                correctAnswersCount++
                AnswerFeedbackState.CORRECT
            } else {
                wrongAnswersCount++
                AnswerFeedbackState.INCORRECT
            }
            
            if (isCorrect) {
                score += calculatePoints(currentLevel)
                audioService.playCorrectSound()
            } else {
                audioService.playWrongSound()
            }
            
            // Son soru mu kontrol et
            if (questionCount >= maxQuestions) {
                // Kısa bir gecikme sonra oyunu bitir
                android.os.Handler(mainLooper).postDelayed({
                        try {
                    handleGameEnd()
                        } catch (e: Exception) {
                            Log.e("MultiplicationGameActivity", "Oyun sonlandırılırken hata: ${e.message}")
                            e.printStackTrace()
                            finish() // En kötü durumda aktiviteyi kapat
                        }
                }, 1000)
            } else {
                // Sonraki soruya geç
                android.os.Handler(mainLooper).postDelayed({
                        try {
                    generateQuestion()
                        } catch (e: Exception) {
                            Log.e("MultiplicationGameActivity", "Sonraki soru oluşturulurken hata: ${e.message}")
                            e.printStackTrace()
                            
                            // Oyunu güvenli bir şekilde sonlandır
                            handleGameEnd()
                        }
                }, 1000)
            }
        } else {
            // Zamanı geçmiş veya boş cevap
            answerFeedbackState = AnswerFeedbackState.TIMEOUT
            wrongAnswersCount++
            audioService.playWrongSound()
            
            // Son soru mu kontrol et
            if (questionCount >= maxQuestions) {
                // Kısa bir gecikme sonra oyunu bitir
                android.os.Handler(mainLooper).postDelayed({
                        try {
                    handleGameEnd()
                        } catch (e: Exception) {
                            Log.e("MultiplicationGameActivity", "Oyun sonlandırılırken hata: ${e.message}")
                            e.printStackTrace()
                            finish() // En kötü durumda aktiviteyi kapat
                        }
                }, 1000)
            } else {
                // Sonraki soruya geç
                android.os.Handler(mainLooper).postDelayed({
                        try {
                    generateQuestion()
                        } catch (e: Exception) {
                            Log.e("MultiplicationGameActivity", "Sonraki soru oluşturulurken hata: ${e.message}")
                            e.printStackTrace()
                            
                            // Oyunu güvenli bir şekilde sonlandır
                            handleGameEnd()
                        }
                }, 1000)
            }
        }
        } catch (e: Exception) {
            Log.e("MultiplicationGameActivity", "Kullanıcı cevabı kontrol edilirken hata: ${e.message}")
            e.printStackTrace()
            
            // Güvenli bir şekilde sonraki soruya geç
            android.os.Handler(mainLooper).postDelayed({
                try {
                    generateQuestion()
                } catch (e: Exception) {
                    handleGameEnd()
                }
            }, 1000)
        }
    }
    
    /**
     * Ekran rotasyonu gibi durumlarda state'i korumak için
     */
    override fun onSaveInstanceState(outState: Bundle) {
        try {
        super.onSaveInstanceState(outState)
        outState.putBoolean("isGameEnding", isGameEnding)
        } catch (e: Exception) {
            Log.e("MultiplicationGameActivity", "Durum kaydedilirken hata: ${e.message}")
            e.printStackTrace()
        }
    }
    
    /**
     * State'i geri yüklemek için
     */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        try {
        super.onRestoreInstanceState(savedInstanceState)
        isGameEnding = savedInstanceState.getBoolean("isGameEnding", false)
        } catch (e: Exception) {
            Log.e("MultiplicationGameActivity", "Durum geri yüklenirken hata: ${e.message}")
            e.printStackTrace()
            
            // Varsayılan değerlere dön
            isGameEnding = false
        }
    }
    
    override fun updateDifficulty() {
        // Artık zorluk seviyesi oyun başında seçiliyor, otomatik güncelleme yapılmıyor
    }
    
    /**
     * Oyunu başlat
     */
    override fun startGame() {
        try {
            // QuestionTracker'ı sıfırla
            questionTracker.reset()
            
            // İlk soruyu oluştur
            generateQuestion()
        } catch (e: Exception) {
            Log.e("MultiplicationGameActivity", "Oyun başlatılırken hata: ${e.message}")
            e.printStackTrace()
            
            // Kullanıcıya bildir
            Toast.makeText(
                this, 
                "Oyun başlatılamadı. Lütfen tekrar deneyin.", 
                Toast.LENGTH_LONG
            ).show()
            
            // Ana menüye dön
            finish()
        }
    }
} 