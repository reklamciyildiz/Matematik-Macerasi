package com.yigit033.matematikmacarasi.games.basic

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.activities.BaseGameActivity
import android.app.Dialog
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import com.yigit033.matematikmacarasi.GameType
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
 * DivisionGameActivity, bölme oyunu ekranını yönetir.
 * Kullanıcıya iki sayı gösterilir ve bölümünü hesaplaması istenir.
 * Bölme işleminde küçük yaştaki çocuklar için tam bölünme durumları tercih edilir.
 */
class DivisionGameActivity : BaseGameActivity() {
    
    // BaseGameActivity'den gelen abstract property'yi override et
    override val gameType: GameType = GameType.DIVISION_GAME
    
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
                            Log.e("DivisionGameActivity", "Oyun yeniden başlatılırken hata: ${e.message}")
                            e.printStackTrace()
                            android.widget.Toast.makeText(
                                this@DivisionGameActivity,
                                "Oyun yeniden başlatılamadı. Lütfen ana menüye dönün.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    onMainMenu = {
                        // Ana menüye dön
                        try {
                            android.content.Intent(this@DivisionGameActivity, Class.forName("com.yigit033.matematikmacarasi.MainActivity")).also { intent ->
                                // FLAG_ACTIVITY_CLEAR_TOP yerine daha nazik bayraklar kullanıyoruz
                                // Bu, MainActivity'nin yeniden oluşturulmasını değil, öne getirilmesini sağlar
                                intent.flags = android.content.Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                
                                // Müziğin devam etmesi için extra bilgi ekleyelim
                                intent.putExtra("continue_music", true)
                                
                                startActivity(intent)
                                
                                // Mevcut aktiviteyi temiz bir şekilde kapat
                                // Önce ses efektlerini nazikçe temizleyelim
                                try {
                                    audioService.clearEffectSounds()
                                } catch (e: Exception) {
                                    Log.w("DivisionGameActivity", "Ses efektleri temizlenirken hata: ${e.message}")
                                }
                                
                                finish() // Mevcut aktiviteyi kapat
                            }
                        } catch (e: Exception) {
                            // Hata oluşursa güvenli bir şekilde aktiviteyi kapat
                            Log.e("DivisionGameActivity", "Ana menüye dönülürken hata: ${e.message}")
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
                            Log.e("DivisionGameActivity", "Sonraki seviyeye geçilirken hata: ${e.message}")
                            e.printStackTrace()
                            android.widget.Toast.makeText(
                                this@DivisionGameActivity,
                                "Sonraki seviyeye geçilemedi. Lütfen ana menüye dönün.",
                                android.widget.Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                )
            }
            showDifficultySelection -> {
                // Zorluk seviyesi seçim ekranı
                DifficultySelectionScreen(
                    title = "Bölme İşlemi",
                    onDifficultySelected = { difficulty ->
                        try {
                            selectedDifficulty = difficulty
                            showDifficultySelection = false // Zorluk seviyesi seçildi, oyunu başlat
                            initializeGameWithDifficulty(difficulty) // Zorluk seviyesine göre oyunu başlat
                        } catch (e: Exception) {
                            // Hata oluşursa kaydet ve kullanıcıya bildir
                            Log.e("DivisionGameActivity", "Zorluk seviyesi seçilirken hata: ${e.message}")
                            e.printStackTrace()
                            
                            // Varsayılan zorluk seviyesi ile devam et
                            showDifficultySelection = false
                            initializeGameWithDifficulty(DifficultyLevel.EASY)
                            
                            Toast.makeText(
                                this@DivisionGameActivity,
                                "Bir hata oluştu, kolay seviye ile devam ediliyor.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
            else -> {
                // Ana oyun ekranı
                val questionText = when (currentLevel) {
                    1 -> "Bölme işlemini çöz: Paylaştırma"
                    2 -> "Bölme işlemini çöz: Gruplama"
                    else -> "Bölme işlemini çöz: Kalanlı"
                }
                
                val operationText = "$firstNumber ÷ $secondNumber = ?"
                
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
                            Log.e("DivisionGameActivity", "Cevap kontrol edilirken hata: ${e.message}")
                            e.printStackTrace()
                            
                            // Bir sonraki soruya geç
                            android.os.Handler(mainLooper).postDelayed({
                                try {
                                    generateQuestion()
                                } catch (e: Exception) {
                                    // Soru oluşturulamazsa oyunu bitir
                                    Log.e("DivisionGameActivity", "Soru oluşturulurken hata: ${e.message}")
                                    e.printStackTrace()
                                    handleGameEnd()
                                }
                            }, 1000)
                        }
                    },
                    gameTitle = "Bölme İşlemini Çöz",
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
            Log.e("DivisionGameActivity", "Oyun başlatılırken hata: ${e.message}")
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
                android.widget.Toast.makeText(
                    this,
                    "Oyun başlatılamadı. Lütfen tekrar deneyin.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                
                // Ana menüye dön
                android.content.Intent(this, Class.forName("com.yigit033.matematikmacarasi.MainActivity")).also { intent ->
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
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
                        // Çok temel bölme işlemleri (bölüm 1-5 arası)
                        secondNumber = (2..5).random() // Bölen
                        correctAnswer = (1..3).random() // Bölüm
                        
                        // Bölünen = bölüm * bölen (tam bölünme)
                        firstNumber = correctAnswer * secondNumber
                    }
                    2 -> { // Seviye 2 (7-9 yaş / 2-3. Sınıf)
                        // Temel bölme işlemleri (çarpım tablosu ile ilişkilendirme)
                        secondNumber = (2..10).random() // Bölen
                        correctAnswer = (2..5).random() // Bölüm
                        
                        // Bölünen = bölüm * bölen (tam bölünme)
                        firstNumber = correctAnswer * secondNumber
                        
                        // 2, 5 ve 10 için özel vurgu
                        if (Math.random() < 0.3) {
                            secondNumber = listOf(2, 5, 10).random() // Kolay bölenler
                            correctAnswer = (1..10).random()
                            firstNumber = correctAnswer * secondNumber // Tam bölünen
                        }
                    }
                    3 -> { // Seviye 3 (9-10 yaş / 3-4. Sınıf)
                        // Daha kapsamlı bölme işlemleri
                        secondNumber = (2..12).random() // Bölen 
                        correctAnswer = (2..10).random() // Bölüm
                        
                        // %70 ihtimalle kalansız, %30 ihtimalle kalanlı bölme
                        if (Math.random() < 0.7) {
                            // Bölünen = bölüm * bölen (tam bölünme)
                            firstNumber = correctAnswer * secondNumber
                        } else {
                            // Bölünen = bölüm * bölen + kalan (kalanlı bölme, kalan gösterilmez)
                            val remainder = (1 until secondNumber).random()
                            firstNumber = (correctAnswer * secondNumber) + remainder
                        }
                    }
                    4 -> { // Seviye 4 (10-12 yaş / 4-6. Sınıf)
                        // Daha zor bölme işlemleri, iki basamaklı bölünenler
                        secondNumber = (2..12).random() // Bölen
                        correctAnswer = (3..12).random() // Bölüm
                        
                        // %60 ihtimalle kalansız, %40 ihtimalle kalanlı bölme
                        if (Math.random() < 0.6) {
                            // Bölünen = bölüm * bölen (tam bölünme)
                            firstNumber = correctAnswer * secondNumber
                        } else {
                            // Bölünen = bölüm * bölen + kalan (kalanlı bölme, kalan gösterilmez)
                            val remainder = (1 until secondNumber).random()
                            firstNumber = (correctAnswer * secondNumber) + remainder
                        }
                        
                        // 10'un katlarının bölünmesi vurgulansın
                        if (Math.random() < 0.3) {
                            firstNumber = ((firstNumber / 10) * 10) // En yakın 10'un katına yuvarla
                            if (firstNumber < 30) firstNumber = 30
                            if (firstNumber > 99) firstNumber = 90
                            
                            // Yeniden hesapla
                            correctAnswer = firstNumber / secondNumber
                            // Bölünen = bölüm * bölen olsun (kalansız)
                            firstNumber = correctAnswer * secondNumber
                        }
                        
                        // Bölünenin 30-99 arasında olduğundan emin ol
                        if (firstNumber < 30) {
                            correctAnswer = 8
                            firstNumber = correctAnswer * secondNumber
                        } else if (firstNumber > 99) {
                            // Sınırlandır
                            correctAnswer = 7
                            firstNumber = correctAnswer * secondNumber
                        }
                    }
                    5 -> { // Seviye 5 (12-14 yaş / 6-8. Sınıf)
                        // Kapsamlı bölme işlemleri - aşamalı zorluk
                        val progressPercentage = (questionCount.toFloat() / maxQuestions.toFloat()) * 100
                        
                        when {
                            progressPercentage < 20 -> {
                                // Aşama 1 (İlk %20 soru): Orta zorlukta bölmeler
                                secondNumber = (5..15).random() // Bölen
                                correctAnswer = (4..10).random() // Bölüm
                                
                                // %50 kalansız, %50 kalanlı işlemler
                                if (Math.random() < 0.5) {
                                    // Kalansız
                                    firstNumber = correctAnswer * secondNumber // Bölünen (50-100 arası)
                                } else {
                                    // Kalanlı
                                    val remainder = (1..(secondNumber-1)).random() // Kalan
                                    firstNumber = (correctAnswer * secondNumber) + remainder // Bölünen + kalan
                                }
                                
                                // Bölünenin 50-100 arasında olduğundan emin ol
                                if (firstNumber < 50) {
                                    correctAnswer = 10
                                    firstNumber = correctAnswer * secondNumber
                                } else if (firstNumber > 100) {
                                    correctAnswer = 6
                                    firstNumber = correctAnswer * secondNumber
                                }
                            }
                            progressPercentage < 60 -> {
                                // Aşama 2 (Sonraki %40 soru): Daha zor bölmeler
                                secondNumber = (8..25).random() // Bölen
                                correctAnswer = (5..10).random() // Bölüm
                                
                                // %50 kalansız, %50 kalanlı işlemler
                                if (Math.random() < 0.5) {
                                    // Kalansız
                                    firstNumber = correctAnswer * secondNumber // Bölünen (80-150 arası)
                                } else {
                                    // Kalanlı
                                    val remainder = (1..(secondNumber/2)).random() // Kalan (bölenin yarısına kadar)
                                    firstNumber = (correctAnswer * secondNumber) + remainder // Bölünen + kalan
                                }
                                
                                // Bölünenin 80-150 arasında olduğundan emin ol
                                if (firstNumber < 80) {
                                    correctAnswer = 10
                                    firstNumber = correctAnswer * secondNumber
                                } else if (firstNumber > 150) {
                                    correctAnswer = 6
                                    firstNumber = correctAnswer * secondNumber
                                }
                            }
                            else -> {
                                // Aşama 3 (Son %40 soru): En zor bölmeler
                                secondNumber = (10..30).random() // Bölen
                                correctAnswer = (6..12).random() // Bölüm
                                
                                // %50 kalansız, %50 kalanlı işlemler
                                if (Math.random() < 0.5) {
                                    // Kalansız
                                    firstNumber = correctAnswer * secondNumber // Bölünen (100-200 arası)
                                } else {
                                    // Kalanlı
                                    val remainder = (1..(secondNumber/2)).random() // Kalan (bölenin yarısına kadar)
                                    firstNumber = (correctAnswer * secondNumber) + remainder // Bölünen + kalan
                                }
                                
                                // Bölünenin 100-200 arasında olduğundan emin ol
                                if (firstNumber < 100) {
                                    correctAnswer = 10
                                    firstNumber = correctAnswer * secondNumber
                                } else if (firstNumber > 200) {
                                    correctAnswer = 6
                                    firstNumber = correctAnswer * secondNumber
                                }
                            }
                        }
                    }
                }
                
                // Bu sorunun daha önce sorulup sorulmadığını kontrol et
                isNewQuestion = !questionTracker.isOperationAsked(firstNumber, secondNumber, "÷")
                
                retryCount++
            }
            
            // Maksimum deneme sayısına ulaşıldıysa, soru sayısını arttırma ve yeni bir soru üretme
            if (retryCount >= MAX_RETRY_COUNT) {
                Log.w("DivisionGameActivity", "Maksimum deneme sayısına ulaşıldı, benzersiz soru bulunamadı")
                // Deneme sayısına ulaşılsa bile bu soruyu kaydediyoruz ki tekrar denemeyelim
                questionTracker.isOperationAsked(firstNumber, secondNumber, "÷")
            }
            
            // Cevap seçeneklerini oluştur
            generateAnswerOptions()
            
            // Cevap durumunu sıfırla
            answerFeedbackState = AnswerFeedbackState.IDLE
            lastSelectedOption = ""
            
            // Soru sayısını artır
            questionCount++
        } catch (e: Exception) {
            Log.e("DivisionGameActivity", "Soru oluşturulurken hata: ${e.message}")
            e.printStackTrace()
            
            // Basit bir soru oluşturarak devam et
            try {
                correctAnswer = 2 // Bölüm
                secondNumber = 2 // Bölen
                firstNumber = 4 // Bölünen
                
                // Tracker'a ekle
                questionTracker.isOperationAsked(firstNumber, secondNumber, "÷")
                
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
                Log.e("DivisionGameActivity", "Basit soru oluşturulurken de hata: ${e.message}")
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
                                1,                                     // +1 (bariz hata)
                                -1,                                    // -1 (bariz hata)
                                firstNumber - secondNumber - correctAnswer, // Çıkarma yapmış
                                secondNumber                           // Böleni cevap olarak yazmış
                            ).random()
                        }
                        2 -> { // Orta - 2-3. Sınıf
                            // Bölme tablosu hataları
                            if (Math.random() < 0.5) {
                                // +/- birkaç sayı
                                (1..2).random() * if (Math.random() < 0.5) 1 else -1
                            } else {
                                // Yaygın bölme hatası
                                if (Math.random() < 0.6) {
                                    // Çıkarma gibi düşünmüş
                                    firstNumber - secondNumber - correctAnswer
                                } else {
                                    // Bölen ve bölüneni karıştırmış
                                    if (secondNumber > 1 && firstNumber % secondNumber == 0) {
                                        firstNumber / secondNumber - correctAnswer
                                    } else {
                                        // Normal bir hata
                                        (1..3).random() * if (Math.random() < 0.5) 1 else -1
                                    }
                                }
                            }
                        }
                        3 -> { // Zor - 4-5. Sınıf
                            // Bölme hataları
                            if (Math.random() < 0.3) {
                                // Ters bölme yapmış (bölenin bölünene bölünmesi)
                                if (secondNumber > 0 && firstNumber > 0) {
                                    (secondNumber / firstNumber) - correctAnswer
                                } else {
                                    // Yakın bir yanıltıcı
                                    (1..3).random()
                                }
                            } else if (Math.random() < 0.3) {
                                // İşlem hatası - çarpma veya toplama yapmış
                                if (Math.random() < 0.5) {
                                    // Toplama yapmış
                                    (firstNumber + secondNumber) - correctAnswer
                                } else {
                                    // Çarpma yapmış (aslında tersini yapmış)
                                    (firstNumber * secondNumber) - correctAnswer
                                }
                            } else {
                                // Normal bir hata
                                (1..5).random() * if (Math.random() < 0.5) 1 else -1
                            }
                        }
                        4 -> { // Çok Zor - 6-7. Sınıf
                            // Karmaşık bölme hataları
                            if (Math.random() < 0.4) {
                                // Bölme algoritması hatası
                                if (correctAnswer > 10) {
                                    // Onlar basamağında hata
                                    val onesDigit = correctAnswer % 10
                                    val tensDigit = correctAnswer / 10
                                    
                                    // Onlar basamağında +/- 1 hata
                                    (tensDigit + if (Math.random() < 0.5) 1 else -1) * 10 + onesDigit - correctAnswer
                                } else {
                                    // Küçük sayılarda normal bir hata
                                    (2..5).random() * if (Math.random() < 0.5) 1 else -1
                                }
                            } else if (Math.random() < 0.3) {
                                // Kalanlı bölme hatası
                                val remain = (1..secondNumber-1).random()
                                (firstNumber + remain) / secondNumber - correctAnswer
                            } else {
                                // Normal bir hata
                                (1..5).random() * if (Math.random() < 0.5) 1 else -1
                            }
                        }
                        else -> { // Uzman - 8. Sınıf ve üstü
                            // Çok karmaşık bölme hataları
                            if (Math.random() < 0.3 && firstNumber > 50) {
                                // Büyük sayılarda bölme hatası
                                (5..10).random() * if (Math.random() < 0.5) 1 else -1
                            } else if (Math.random() < 0.4) {
                                // Karmaşık bölme algoritması hatası
                                if (firstNumber > 100) {
                                    // Bölüm yerine kalanı yazmış gibi
                                    firstNumber % (secondNumber + 1)
                                } else {
                                    // Yakın bir yanıltıcı
                                    (3..8).random() * if (Math.random() < 0.5) 1 else -1
                                }
                            } else {
                                // Normal bir hata
                                (2..10).random() * if (Math.random() < 0.5) 1 else -1
                            }
                        }
                    }
                    
                    val wrongAnswer = correctAnswer + offset
                    
                    // Geçerli bir sayı mı kontrol et (negatif olmasın)
                    if (wrongAnswer > 0 && !optionsList.contains(wrongAnswer.toString())) {
                        optionsList.add(wrongAnswer.toString())
                    }
                } catch (e: Exception) {
                    Log.e("DivisionGameActivity", "Yanıltıcı seçenek oluşturulurken hata: ${e.message}")
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
            Log.e("DivisionGameActivity", "Cevap seçenekleri oluşturulurken hata: ${e.message}")
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
            Log.e("DivisionGameActivity", "Cevap kontrolü sırasında hata: ${e.message}")
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
                            Log.e("DivisionGameActivity", "Oyun sonlandırılırken hata: ${e.message}")
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
                            Log.e("DivisionGameActivity", "Sonraki soru oluşturulurken hata: ${e.message}")
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
                            Log.e("DivisionGameActivity", "Oyun sonlandırılırken hata: ${e.message}")
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
                            Log.e("DivisionGameActivity", "Sonraki soru oluşturulurken hata: ${e.message}")
                            e.printStackTrace()
                            
                            // Oyunu güvenli bir şekilde sonlandır
                            handleGameEnd()
                        }
                    }, 1000)
                }
            }
        } catch (e: Exception) {
            Log.e("DivisionGameActivity", "Kullanıcı cevabı kontrol edilirken hata: ${e.message}")
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
     * Oyun özetini gösterir ve ardından oyunu sonlandırır
     */
    private fun showGameSummary() {
        try {
            // Burada artık dialog açmak yerine doğrudan aktivite içeriğini değiştiriyoruz
            showGameSummary = true
            
        } catch (e: Exception) {
            // Hata oluşursa loglayalım ve güvenli bir şekilde oyunu bitirelim
            Log.e("DivisionGameActivity", "Oyun özeti gösterilirken hata: ${e.message}")
            e.printStackTrace()
            
            // Hatayı kullanıcıya bildir
            android.widget.Toast.makeText(
                this, 
                "Oyun özeti gösterilirken bir hata oluştu. Lütfen tekrar deneyin.", 
                android.widget.Toast.LENGTH_LONG
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
                    Log.d("DivisionGameActivity", "Günlük görev güncellendi (gameType: $gameType)")
                } catch (e: Exception) {
                    Log.e("DivisionGameActivity", "Günlük görev güncellenirken hata: ${e.message}")
                    e.printStackTrace()
                }
                
                // Yıldız sistemi entegrasyonu
                try {
                    // Doğrudan StarManager'ı kullan
                    val starManager = com.yigit033.matematikmacarasi.services.StarManager.getInstance(this)
                    
                    // Oyun sonuçlarını işle ve yıldızları kaydet
                    val gameId = "division_game_${currentLevel}"
                    
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
                        Log.d("DivisionGameActivity", "Yıldız ekleme: $success (Oyun: $gameId, Yıldız: $earnedStars)")
                        
                        // Daha detaylı bir Toast mesajı
                        val totalStars = starManager.getTotalStars()
                        android.widget.Toast.makeText(
                            this,
                            "$earnedStars yıldız kazandınız! Toplam yıldızınız: $totalStars",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Log.e("DivisionGameActivity", "Yıldız ekleme işlemi sırasında hata: ${e.message}")
                    e.printStackTrace()
                }
                
                // Geçiş reklamı göster - BaseGameActivity'deki gibi
                try {
                    Log.d("DivisionGameActivity", "Oyun sonu reklamı gösteriliyor")
                    // super yoluyla BaseGameActivity'deki adManager'a erişim
                    super.endGame()
                } catch (e: Exception) {
                    Log.e("DivisionGameActivity", "Reklam gösterimi sırasında hata: ${e.message}", e)
                }
                
                // Özet ekranını göster, bu ekrandan kullanıcı ana menüye dönebilir
                showGameSummary()
                
                // BaseGameActivity'nin endGame metodunu çağırmayarak,
                // kontrolü ele alıyoruz ve özet ekranındaki butonlarla
                // kullanıcının ne yapmak istediğine göre yönlendiriyoruz
            } catch (e: Exception) {
                Log.e("DivisionGameActivity", "Oyun sonlandırılırken hata: ${e.message}")
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
    
    /**
     * Ekran rotasyonu gibi durumlarda state'i korumak için
     */
    override fun onSaveInstanceState(outState: Bundle) {
        try {
            super.onSaveInstanceState(outState)
            outState.putBoolean("isGameEnding", isGameEnding)
        } catch (e: Exception) {
            Log.e("DivisionGameActivity", "Durum kaydedilirken hata: ${e.message}")
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
            Log.e("DivisionGameActivity", "Durum geri yüklenirken hata: ${e.message}")
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
            Log.e("DivisionGameActivity", "Oyun başlatılırken hata: ${e.message}")
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