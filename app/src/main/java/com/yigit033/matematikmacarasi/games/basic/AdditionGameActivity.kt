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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.activities.BaseGameActivity
import com.yigit033.matematikmacarasi.games.basic.AgeGroup
import com.yigit033.matematikmacarasi.games.basic.EnhancedMathGameScreen
import com.yigit033.matematikmacarasi.games.basic.getDifficultyAgeGroup
import com.yigit033.matematikmacarasi.models.DifficultyLevel
import com.yigit033.matematikmacarasi.services.DailyTaskManager
import com.yigit033.matematikmacarasi.ui.components.GameTopBar
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import com.yigit033.matematikmacarasi.games.models.QuestionTracker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min
import kotlin.math.max
import kotlin.random.Random

/**
 * AdditionGameActivity, toplama oyunu ekranını yönetir.
 * Kullanıcıya iki sayı gösterilir ve toplamını hesaplaması istenir.
 */
class AdditionGameActivity : BaseGameActivity() {
    
    // BaseGameActivity'den gelen abstract property'yi override et
    override val gameType: GameType = GameType.ADDITION_GAME
    
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
    
    // Sorulan soruları takip eden tracker
    private val questionTracker = QuestionTracker()
    
    // Maksimum yeniden deneme sayısı
    private val MAX_RETRY_COUNT = 20
    
    /**
     * Aktivite ömrünü kontrol etmek için kaydedilen flag
     */
    private var isGameEnding = false
    
    // Hata durumu için bir state tanımlayalım
    private var hasError by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Başlangıçta oyunu başlatmıyoruz, zorluk seviyesi seçimini bekliyoruz
    }
    
    @Composable
    override fun GameContent() {
        val grade=GradeAwareOperationPolicy.grade(
            com.yigit033.matematikmacarasi.services.PlayerProfileService
                .getInstance(this).getUserClass()
        )
        if(!GradeAwareOperationPolicy.allowed(grade,OperationKind.ADD)){
            OperationGradeLockedScreen(
                grade=grade,
                operation=OperationKind.ADD,
                onBack={finish()}
            )
            return
        }
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
                            Log.e("AdditionGameActivity", "Oyun yeniden başlatılırken hata: ${e.message}")
                            e.printStackTrace()
                            Toast.makeText(
                                this@AdditionGameActivity,
                                "Oyun yeniden başlatılamadı. Lütfen ana menüye dönün.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    },
                    onMainMenu = {
                        // Ana menüye dön
                        try {
                            android.content.Intent(this@AdditionGameActivity, Class.forName("com.yigit033.matematikmacarasi.MainActivity")).also { intent ->
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
                                    Log.w("AdditionGameActivity", "Ses efektleri temizlenirken hata: ${e.message}")
                                }
                                
                                finish() // Mevcut aktiviteyi kapat
                            }
                        } catch (e: Exception) {
                            // Hata oluşursa güvenli bir şekilde aktiviteyi kapat
                            Log.e("AdditionGameActivity", "Ana menüye dönülürken hata: ${e.message}")
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
                            Log.e("AdditionGameActivity", "Sonraki seviyeye geçilirken hata: ${e.message}")
                            e.printStackTrace()
                            Toast.makeText(
                                this@AdditionGameActivity,
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
                    title = "Toplama İşlemi",
                    onDifficultySelected = { difficulty ->
                        try {
                            selectedDifficulty = difficulty
                            showDifficultySelection = false // Zorluk seviyesi seçildi, oyunu başlat
                            initializeGameWithDifficulty(difficulty) // Zorluk seviyesine göre oyunu başlat
                        } catch (e: Exception) {
                            // Hata oluşursa kaydet ve kullanıcıya bildir
                            Log.e("AdditionGameActivity", "Zorluk seviyesi seçilirken hata: ${e.message}")
                            e.printStackTrace()
                            
                            // Varsayılan zorluk seviyesi ile devam et
                            showDifficultySelection = false
                            initializeGameWithDifficulty(com.yigit033.matematikmacarasi.models.DifficultyLevel.EASY)
                            
                            Toast.makeText(
                                this@AdditionGameActivity,
                                "Bir hata oluştu, kolay seviye ile devam ediliyor.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
            else -> {
                // Ana oyun ekranı
                val questionText = "Toplama İşlemleri"
                val operationText = "$firstNumber + $secondNumber = ?"
                
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
                            Log.e("AdditionGameActivity", "Cevap kontrol edilirken hata: ${e.message}")
                            e.printStackTrace()
                            
                            // Bir sonraki soruya geç
                            android.os.Handler(mainLooper).postDelayed({
                                try {
                                    generateQuestion()
                                } catch (e: Exception) {
                                    // Soru oluşturulamazsa oyunu bitir
                                    Log.e("AdditionGameActivity", "Soru oluşturulurken hata: ${e.message}")
                                    e.printStackTrace()
                                    handleGameEnd()
                                }
                            }, 1000)
                        }
                    },
                    gameTitle = "Toplama İşlemini Çöz",
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
            Log.e("AdditionGameActivity", "Oyun başlatılırken hata: ${e.message}")
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
                android.content.Intent(this, Class.forName("com.yigit033.matematikmacarasi.MainActivity")).also { intent ->
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    finish()
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
            Log.e("AdditionGameActivity", "Oyun özeti gösterilirken hata: ${e.message}")
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
     * BaseGameActivity'nin endGame metodunu override edemediğimiz için
     * Oyun sonu işlemlerini yönetecek metot
     */
    private fun handleGameEnd() {
        try {
            isGameEnding = true
            
            // Günlük görev işleme - GameType kullanarak daha güvenilir
            try {
                // DailyTaskManager'i kullanarak görev ilerlemesini gameType ile güncelle
                dailyTaskManager.updateTaskProgressByGameType(gameType)
                Log.d("AdditionGameActivity", "Günlük görev güncellendi (gameType: $gameType)")
            } catch (e: Exception) {
                Log.e("AdditionGameActivity", "Günlük görev güncellenirken hata: ${e.message}")
                e.printStackTrace()
            }
            
            // Tamamlanan oyun sayısını artır
            gameStatisticsService.incrementCompletedGames(gameType)
            
            // MainActivity'nin StarManager'ını kullanarak yıldızları kaydet
            var earnedStars = 0
            try {
                // Doğrudan StarManager'ı kullan (MainActivity cast etmeden)
                val starManager = com.yigit033.matematikmacarasi.services.StarManager.getInstance(this)
                
                // Oyun sonuçlarını işle ve yıldızları kaydet
                val gameId = "addition_game_${currentLevel}"
                
                // Yıldız hesaplama
                val percentage = (correctAnswersCount.toFloat() / maxQuestions.toFloat()) * 100
                earnedStars = when {
                    percentage >= 90 -> 3 // Mükemmel performans: 3 yıldız
                    percentage >= 70 -> 2 // İyi performans: 2 yıldız
                    percentage >= 50 -> 1 // Ortalama performans: 1 yıldız
                    else -> 0 // Zayıf performans: yıldız yok
                }
                
                // Yıldızları ekle
                if (earnedStars > 0) {
                    val success = starManager.addStars(gameId, earnedStars)
                    Log.d("AdditionGameActivity", "Yıldız ekleme: $success (Oyun: $gameId, Yıldız: $earnedStars)")
                    
                    // Daha detaylı bir Toast mesajı
                    val totalStars = starManager.getTotalStars()
                    Toast.makeText(
                        this,
                        "$earnedStars yıldız kazandınız! Toplam yıldızınız: $totalStars",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Log.e("AdditionGameActivity", "Yıldız ekleme işlemi sırasında hata: ${e.message}")
                e.printStackTrace()
            }
            
            // Geçiş reklamı göster - BaseGameActivity'deki gibi
            try {
                Log.d("AdditionGameActivity", "Oyun sonu reklamı gösteriliyor")
                // super yoluyla BaseGameActivity'deki adManager'a erişim
                super.endGame()
            } catch (e: Exception) {
                Log.e("AdditionGameActivity", "Reklam gösterimi sırasında hata: ${e.message}", e)
            }
            
            // Oyun sonuç ekranını göster
            showGameSummary = true
            
        } catch (e: Exception) {
            Log.e("AdditionGameActivity", "Oyun sonlandırılırken hata: ${e.message}")
            e.printStackTrace()
            
            // Hata olsa bile oyun sonuç ekranını göstermeye çalış
            try {
                showGameSummary = true
            } catch (e: Exception) {
                // En kötü ihtimalle aktiviteyi sonlandır
                finish()
            }
        }
    }
    
    override fun generateQuestion() {
        if(questionCount>=maxQuestions){handleGameEnd();return}
        val grade=GradeAwareOperationPolicy.grade(
            com.yigit033.matematikmacarasi.services.PlayerProfileService
                .getInstance(this).getUserClass()
        )
        if(!GradeAwareOperationPolicy.allowed(grade,OperationKind.ADD)){
            handleGameEnd()
            return
        }
        val q=GradeAwareOperationPolicy.generate(
            grade,OperationKind.ADD,selectedDifficulty
        )
        firstNumber=q.first
        secondNumber=q.second
        correctAnswer=q.answer
        options=GradeAwareOperationPolicy.options(q)
        questionTracker.isOperationAsked(firstNumber,secondNumber,"+")
        answerFeedbackState=AnswerFeedbackState.IDLE
        lastSelectedOption=""
        questionCount++
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
                            // Açıkça farklı cevaplar
                            listOf(-2, -1, 1, 2).random()
                        }
                        2 -> { // Orta - 2-3. Sınıf
                            // Biraz daha yakın yanıltıcılar
                            (-3..3).random().let { if (it == 0) 1 else it }
                        }
                        3 -> { // Zor - 4-5. Sınıf
                            // Daha karmaşık yanıltıcılar 
                            val baseOffset = (-5..5).random().let { if (it == 0) 1 else it }
                            // Bazen toplama yerine çıkarma yapılmış gibi hata ekle
                            if (Math.random() < 0.3 && firstNumber > secondNumber) {
                                // Çıkarma yapılmış gibi
                                (firstNumber - secondNumber) - correctAnswer
                            } else if (Math.random() < 0.3 && correctAnswer > 10) {
                                // Basamak hatası benzetimi
                                val tensDigit = correctAnswer / 10
                                val onesDigit = correctAnswer % 10
                                (tensDigit * 10 + (onesDigit + 1) % 10) - correctAnswer
                            } else {
                                baseOffset
                            }
                        }
                        4 -> { // Çok Zor - 6-7. Sınıf
                            // İki basamaklı sayılarda elde hatası benzetimi
                            if (Math.random() < 0.4 && correctAnswer > 20) {
                                // Elde yanlış yapılmış gibi
                                val onesSum = (firstNumber % 10) + (secondNumber % 10)
                                
                                if (onesSum > 9) {
                                    // Elde gerekliydi ama almamış gibi
                                    correctAnswer - (onesSum / 10) * 10
                                } else {
                                    // Gereksiz elde almış gibi
                                    correctAnswer + 10
                                }
                            } else if (Math.random() < 0.3) {
                                // İşlem hatası - sadece birler basamaklarını toplamış
                                val onesSum = (firstNumber % 10 + secondNumber % 10) % 10
                                val tensSum = (firstNumber / 10) + (secondNumber / 10)
                                (tensSum * 10 + onesSum) - correctAnswer
                            } else {
                                (-8..8).random().let { if (it == 0) 2 else it }
                            }
                        }
                        else -> { // Uzman - 8. Sınıf ve üstü
                            // Çok yakın ve zor yanıltıcılar
                            if (Math.random() < 0.4) {
                                // İşlem değiştirmiş gibi
                                if (Math.random() < 0.5 && firstNumber > secondNumber) {
                                    // Çıkarma yapmış gibi
                                    (firstNumber - secondNumber) - correctAnswer
                                } else if (Math.random() < 0.5) {
                                    // Çarpma yapmış gibi (küçük sayılar seç)
                                    val smallFirst = firstNumber % 10
                                    val smallSecond = secondNumber % 10
                                    (smallFirst * smallSecond) - correctAnswer
                                } else {
                                    // Basamakları birleştirmiş gibi
                                    val combined = if (firstNumber < 10 && secondNumber < 10) {
                                        firstNumber * 10 + secondNumber
                                    } else {
                                        (firstNumber % 10) * 10 + (secondNumber % 10)
                                    }
                                    combined - correctAnswer
                                }
                            } else if (Math.random() < 0.4 && correctAnswer > 10) {
                                // Basamak hataları
                                val tensDigit = correctAnswer / 10
                                val onesDigit = correctAnswer % 10
                                
                                if (tensDigit > 0) {
                                    listOf(
                                        tensDigit * 10 + ((onesDigit + 1) % 10), // Birler basamağı +1
                                        (tensDigit + 1) * 10 + onesDigit,        // Onlar basamağı +1
                                        (tensDigit - 1).coerceAtLeast(0) * 10 + onesDigit  // Onlar basamağı -1
                                    ).random() - correctAnswer
                                } else {
                                    // Küçük sayılarda sabit offset
                                    listOf(5, 10, -5).random()
                                }
                            } else {
                                // Normal yanıltıcı - yakın değerler
                                (-10..10).random().let { if (it == 0) 1 else it }
                            }
                        }
                    }
                    
                    val wrongAnswer = correctAnswer + offset
                    
                    // Negatif cevapları kontrol et
                    if (wrongAnswer >= 0 && !optionsList.contains(wrongAnswer.toString())) {
                        optionsList.add(wrongAnswer.toString())
                    }
                } catch (e: Exception) {
                    Log.e("AdditionGameActivity", "Yanıltıcı seçenek oluşturulurken hata: ${e.message}")
                    // Bu yanıltıcıyı atla ve devam et
                    continue
                }
            }
            
            // Eğer hala 4 seçenek elde edemediyse, basit seçenekler ekle
            while (optionsList.size < 4) {
                val simpleOffset = listOf(1, 2, 3, 4, 5, -1, -2, -3).random()
                val simpleWrong = correctAnswer + simpleOffset
                
                if (!optionsList.contains(simpleWrong.toString()) && simpleWrong >= 0) {
                    optionsList.add(simpleWrong.toString())
                }
            }
            
            // Seçenekleri karıştır
            optionsList.shuffle()
            
            options = optionsList
        } catch (e: Exception) {
            Log.e("AdditionGameActivity", "Cevap seçenekleri oluşturulurken hata: ${e.message}")
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
            Log.e("AdditionGameActivity", "Cevap kontrolü sırasında hata: ${e.message}")
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
                            Log.e("AdditionGameActivity", "Oyun sonlandırılırken hata: ${e.message}")
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
                            Log.e("AdditionGameActivity", "Sonraki soru oluşturulurken hata: ${e.message}")
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
                            Log.e("AdditionGameActivity", "Oyun sonlandırılırken hata: ${e.message}")
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
                            Log.e("AdditionGameActivity", "Sonraki soru oluşturulurken hata: ${e.message}")
                            e.printStackTrace()
                            
                            // Oyunu güvenli bir şekilde sonlandır
                            handleGameEnd()
                        }
                    }, 1000)
                }
            }
        } catch (e: Exception) {
            Log.e("AdditionGameActivity", "Kullanıcı cevabı kontrol edilirken hata: ${e.message}")
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
            Log.e("AdditionGameActivity", "Durum kaydedilirken hata: ${e.message}")
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
            Log.e("AdditionGameActivity", "Durum geri yüklenirken hata: ${e.message}")
            e.printStackTrace()
            
            // Varsayılan değerlere dön
            isGameEnding = false
        }
    }
    
    private fun updateDifficultyBasedOnLevel() {
        // Artık zorluk seviyesi oyun başında seçiliyor, otomatik güncelleme yapılmıyor
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
            Log.e("AdditionGameActivity", "Oyun başlatılırken hata: ${e.message}")
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

// AnswerFeedbackState enum sınıfı artık ayrı bir dosyada tanımlandı 