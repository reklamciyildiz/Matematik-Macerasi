package com.yigit033.matematikmacarasi.games.interactive

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.activities.BaseGameActivity
import com.yigit033.matematikmacarasi.games.models.market.*
import com.yigit033.matematikmacarasi.services.AudioService
import com.yigit033.matematikmacarasi.ui.screens.market.LevelSelectDialog
import com.yigit033.matematikmacarasi.ui.screens.market.MarketGameScreen
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import com.yigit033.matematikmacarasi.GameType
import com.yigit033.matematikmacarasi.services.DailyTaskManager
import android.util.Log
import com.yigit033.matematikmacarasi.services.StarManager
import com.yigit033.matematikmacarasi.services.EventBus
import com.yigit033.matematikmacarasi.services.payments.AdManager

/**
 * Market Alışverişi Simülatörü oyun aktivitesi
 */
class MarketSimulatorActivity : BaseGameActivity() {
    
    // Oyun durumu
    private var gameState by mutableStateOf(createInitialGameState())
    
    // Oyun zamanlayıcısı
    private val handler = Handler(Looper.getMainLooper())
    private var gameTimerTask: Runnable? = null
    
    // Seviye seçimi için dialog durumu
    private var showLevelSelectDialog by mutableStateOf(true)
    private var selectedLevelIndex by mutableStateOf(0)
    private val completionGate = com.yigit033.matematikmacarasi.services.GameRunCompletionGate()
    
    // İlk kez oynama takibi
    private val prefKey = "MARKET_SIMULATOR_FIRST_TIME"
    private var isFirstTime by mutableStateOf(true)
    
    // BaseGameActivity'den abstract property'yi override et
    override val gameType: GameType = GameType.MARKET_SIMULATOR // Eğer bu tip yoksa, GameType'a eklemeli ya da en yakın oyun tipini kullanmalı
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Bilgilendirme ekranını her zaman göstermek için her zaman true olarak ayarla
        isFirstTime = true
        
        // Intent'ten seviye indeksini al
        val levelIndex = intent.getIntExtra("level_index", 0).coerceIn(0, MarketLevels.levels.size - 1)
        selectedLevelIndex = levelIndex
        
        // Not: adManager zaten BaseGameActivity'de başlatıldı, sadece log mesajı ekleyelim
        try {
            // Ödüllü reklamları önceden yüklendiğinden emin olalım
            Log.d("MarketSimulatorActivity", "Reklam yöneticisi hazır ve çalışıyor")
        } catch (e: Exception) {
            Log.e("MarketSimulatorActivity", "Reklam yöneticisi ile ilgili hata oluştu: ${e.message}", e)
        }
    }
    
    @Composable
    override fun GameContent() {
        if (showLevelSelectDialog) {
            LevelSelectDialog(
                onLevelSelected = { index ->
                    selectedLevelIndex = index
                    showLevelSelectDialog = false
                    try {
                        initGame(index)
                    } catch (e: Exception) {
                        // Hata yakalama ve kaydetme
                        e.printStackTrace()
                        showToast("Seviye başlatılamadı: ${e.message}")
                        finish() // Aktiviteyi kapat
                    }
                },
                onDismiss = { finish() }
            )
        } else {
            MarketGameScreen(
                state = gameState,
                onProductClick = { handleProductClick(it) },
                onQuantityChange = { productId, quantity -> updateCartItemQuantity(productId, quantity) },
                onRemoveItem = { removeFromCart(it) },
                onCheckout = { proceedToCheckout() },
                onPaymentSubmit = { amount -> processPayment(amount) },
                onPlayAgain = { restartGame() },
                onBack = { finish() },
                showTutorial = isFirstTime
            )
            
            // Tutorial gösterildi, artık ilk kez değil olarak işaretle
            if (isFirstTime) {
                markTutorialShown()
            }
        }
    }
    
    /**
     * Tutorial gösterildi olarak işaretle
     * Not: Artık işaretlemiyoruz çünkü her seviyede tutorial gösterilmeli
     */
    private fun markTutorialShown() {
        // Bu fonksiyonu değiştirdik, artık ilk kez değil olarak işaretlemiyoruz.
        // Böylece her oyun oynamada tutorial gösterilecek.
        isFirstTime = true
    }
    
    @Composable
    override fun GameTheme(content: @Composable () -> Unit) {
        MatematikMacerasiTheme(content = content)
    }
    
    /**
     * Başlangıç oyun durumunu oluştur
     */
    private fun createInitialGameState(): MarketGameState {
        try {
            // İlk seviyeyi statik listeden yükle (dinamik liste kullanma)
            val level = MarketLevels.levels.firstOrNull() 
                ?: throw IllegalStateException("No market levels defined")
            
            return MarketGameState(
                level = level,
                availableProducts = MarketLevels.getAllProducts(),
                timeRemaining = level.timeInSeconds
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Varsayılan güvenli bir oyun durumu oluştur
            val safeLevel = createSafeDefaultLevel()
            return MarketGameState(
                level = safeLevel,
                availableProducts = MarketLevels.getAllProducts(),
                timeRemaining = safeLevel.timeInSeconds
            )
        }
    }
    
    /**
     * Hiçbir seviye bulunamazsa güvenli bir varsayılan seviye oluştur
     */
    private fun createSafeDefaultLevel(): ShoppingList {
        try {
            // Güvenli bir şekilde en basit seviyeyi oluştur
            val products = MarketLevels.getAllProducts()
            
            // En az 2 ürün olduğundan emin ol
            if (products.size >= 2) {
                return ShoppingList(
                    items = listOf(
                        ShoppingListItem(products[0], 1.0),
                        ShoppingListItem(products[1], 1.0)
                    ),
                    budget = 50.0,
                    levelName = "Basit Alışveriş",
                    timeInSeconds = 180,
                    name = "Basit Alışveriş",
                    description = "Temel alışveriş becerilerini öğren",
                    difficulty = 1
                )
            } else {
                throw IllegalStateException("Product list is empty or too small")
            }
        } catch (e: Exception) {
            // Son çare - boş bir seviye döndür
            e.printStackTrace()
            
            return ShoppingList(
                items = emptyList(),
                budget = 50.0,
                levelName = "Örnek Seviye",
                timeInSeconds = 180,
                name = "Örnek Seviye",
                description = "Temel alışveriş becerilerini öğren",
                difficulty = 1
            )
        }
    }
    
    /**
     * Belirli bir seviyeyi başlat
     * @param levelIndex Seviye indeksi
     */
    private fun initGame(levelIndex: Int) {
        completionGate.reset()
        try {
            // Seviye oluşturma
            val level = try {
                // Dinamik liste oluşturmayı dene
                MarketLevels.generateDynamicShoppingList(levelIndex)
            } catch (e: Exception) {
                // Dinamik liste oluşturulamazsa statik listeyi kullan
                e.printStackTrace()
                showToast("Dinamik liste oluşturulamadı, varsayılan liste kullanılıyor")
                
                if (levelIndex < MarketLevels.levels.size) {
                    MarketLevels.levels[levelIndex]
                } else {
                    MarketLevels.levels[0]
                }
            }
            
            // Oyun durumunu oluştur
            gameState = MarketGameState(
                level = level,
                availableProducts = MarketLevels.getAllProducts(),
                timeRemaining = level.timeInSeconds
            )
            
            // BaseGameActivity'nin startGame metodunu çağır
            super.startGame()
            
            // Zamanlayıcıyı başlat
            startGameTimer()
            
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Oyun başlatılamadı: ${e.message}")
            finish()
        }
    }
    
    /**
     * Toast mesajı göster
     */
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Oyun zamanlayıcısını başlat
     */
    private fun startGameTimer() {
        // Zamanlayıcıyı başlat
        gameTimerTask = object : Runnable {
            override fun run() {
                if (gameState.timeRemaining > 0 && 
                    gameState.phase != MarketGamePhase.COMPLETED && 
                    gameState.phase != MarketGamePhase.FAILED) {
                    
                    // Süreyi azalt
                    gameState = gameState.copy(timeRemaining = gameState.timeRemaining - 1)
                    
                    // Süre doldu mu kontrol et
                    if (gameState.timeRemaining <= 0) {
                        failGame("Süre doldu! Alışverişi zamanında tamamlayamadın.")
                            } else {
                        // Zamanlayıcıyı yeniden başlat
                        handler.postDelayed(this, 1000)
                    }
                }
            }
        }
        
        handler.postDelayed(gameTimerTask!!, 1000)
    }
    
    /**
     * Ürüne tıklama işleyicisi
     * @param product Tıklanan ürün
     */
    private fun handleProductClick(product: Product) {
        // Ürün alışveriş listesinde var mı kontrol et
        val isInShoppingList = gameState.level.items.any { it.product.id == product.id }
        
        // Eğer ürün zaten sepetteyse, miktarını artır
        if (gameState.shoppingCart.contains(product.id)) {
            val item = gameState.shoppingCart.items[product.id]
            val increment = if (product.isByWeight) 0.5 else 1.0
            if (item != null) {
                updateCartItemQuantity(product.id, item.quantity + increment)
            }
        } else {
            // Değilse sepete ekle
            val updatedCart = gameState.shoppingCart.also { 
                it.addItem(product, if (product.isByWeight) 0.5 else 1.0) 
            }
            gameState = gameState.copy(shoppingCart = updatedCart)
        }
        
        // Alışveriş listesi durumunu güncelle
        updateShoppingListStatus()
        
        // Ses geri bildirimi ve uyarılar
        try {
            // Ürünün eklendiğini bildiren toast mesajı
            val productName = product.name
            
            if (isInShoppingList) {
                showToast("$productName alışveriş listesinden sepete eklendi!")
                // Sepete eklenen listede olan ürünler için pozitif ses çal
                AudioService.getInstance(this).playCorrectSound()
            } else {
                // Alışveriş listesinde olmayan ürün için uyarı göster
                showToast("Dikkat! $productName alışveriş listende yok ama yine de sepete ekledin.")
                // Sepete eklenen listede olmayan ürünler için uyarı sesi çal
                AudioService.getInstance(this).playWrongSound()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Sepetteki ürün miktarını güncelle
     * @param productId Ürün ID'si
     * @param quantity Yeni miktar
     */
    private fun updateCartItemQuantity(productId: Int, quantity: Double) {
        val updatedCart = gameState.shoppingCart.also { it.updateQuantity(productId, quantity) }
        gameState = gameState.copy(shoppingCart = updatedCart)
        
        // Alışveriş listesi durumunu güncelle
        updateShoppingListStatus()
    }
    
    /**
     * Ürünü sepetten çıkar
     * @param productId Ürün ID'si
     */
    private fun removeFromCart(productId: Int) {
        val updatedCart = gameState.shoppingCart.also { it.removeItem(productId) }
        gameState = gameState.copy(shoppingCart = updatedCart)
        
        // Alışveriş listesi durumunu güncelle
        updateShoppingListStatus()
    }
    
    /**
     * Alışveriş listesi durumunu güncelle
     */
    private fun updateShoppingListStatus() {
        // Alışveriş listesindeki her bir öğe için sepetteki durumu kontrol et
        val updatedItems = gameState.level.items.map { item ->
            val cartItem = gameState.shoppingCart.items[item.product.id]
            val isCompleted = cartItem != null && cartItem.quantity >= item.requiredQuantity
            item.copy(isCompleted = isCompleted)
        }
        
        // Oyun durumunu güncelle
        gameState = gameState.copy(
            level = gameState.level.copy(items = updatedItems)
        )
        
        // Tüm öğeler tamamlandı mı kontrol et
        checkListCompletion()
    }
    
    /**
     * Alışveriş listesinin tamamlanıp tamamlanmadığını kontrol et
     */
    private fun checkListCompletion() {
        val allItemsCompleted = gameState.level.items.all { it.isCompleted }
        
        if (allItemsCompleted) {
            // Kullanıcıya kasaya gitmeye hazır olduğunu bildir
            showToast("Tüm ürünleri topladın! Kasaya gidebilirsin.")
        }
    }
    
    /**
     * Ödeme ekranına geç
     */
    private fun proceedToCheckout() {
        // Bütçe kontrolü
        val totalCost = gameState.shoppingCart.getTotalPrice()
        
        if (totalCost > gameState.level.budget) {
            showToast("Bütçenin üstüne çıktın! Sepeti düzenlemelisin.")
            return
        }
        
        // Tüm öğeler tamamlandı mı?
        val allItemsCompleted = gameState.level.items.all { it.isCompleted }
        if (!allItemsCompleted) {
            showToast("Henüz alışveriş listesindeki tüm ürünleri toplamadın!")
            return
        }
        
        // Liste dışı ürün kontrolü - yeni eklenen kontrol
        val hasUnlistedItems = gameState.shoppingCart.items.any { (productId, _) ->
            !gameState.level.items.any { it.product.id == productId }
        }
        
        if (hasUnlistedItems) {
            showToast("Sepetinde alışveriş listesinde olmayan ürünler var! Lütfen sadece listedeki ürünleri al.")
            return
        }
        
        // Ödeme aşamasına geç
        gameState = gameState.copy(phase = MarketGamePhase.CHECKOUT)
        
        // Zamanlayıcıyı durdur
        gameTimerTask?.let { handler.removeCallbacks(it) }
    }
    
    /**
     * Oyunu bitirir ve istatistikleri günceller, ancak aktiviteyi KAPATMAZ.
     * Bu metod standart endGame metodunun yerine kullanılır.
     * Aktivite sonlandırma (finish) olmadan istatistikleri günceller.
     */
    private fun saveGameStatsWithoutFinishing() {
        try {
            // Oyunu durdur
            isGameRunning = false
            
            // Oyun süresi olarak tahminî bir değer ata (dakika cinsinden)
            val estimatedPlayTimeMinutes = 5
            gameStatisticsService.addPlayTime(estimatedPlayTimeMinutes)
            
            // recordGameStatistics() zaten tamamlanan oyun sayısını kaydeder.
            // Ek bir incrementCompletedGames() aynı tamamlama için çift sayım üretirdi.
            
            // Günlük görev ilerlemesini güncelle
            updateDailyTaskProgress()
            
            // BaseGameActivity'nin recordGameStatistics metodunu çağır
            // Bu metod, seviye bazlı istatistikleri doğru formatta kaydeder
            recordGameStatistics()
            
            // Normalize edilmiş oyun ID'sini al
            val gameIdWithLevel = getNormalizedGameId()
            
            // StarManager ile manuel olarak yıldızları ekle
            val starManager = com.yigit033.matematikmacarasi.services.StarManager.getInstance(this)
            
            // Yıldızları doğrudan StarManager'a ekleyelim
            val success = starManager.addStars(gameIdWithLevel, stars)
            android.util.Log.d("MarketSimulatorActivity", "StarManager'a yıldızlar eklendi: ID=$gameIdWithLevel, Stars=$stars, Başarılı=$success")
            
            // Repository ile senkronize et
            starManager.syncStarsWithRepository()
            val finalTotalStars = starManager.getTotalStars()
            
            // İstatistiklerin kaydedildiğini logla
            Log.d("MarketSimulatorActivity", "Oyun istatistikleri kaydedildi - Seviye: ${currentLevel}, Yıldız: ${stars}")
            Log.d("MarketSimulatorActivity", "Kayıt ID: ${getNormalizedGameId()}, Toplam Yıldızlar: $finalTotalStars")
            
            // EventBus ile yıldız güncellemesini bildir
            EventBus.updateStars(finalTotalStars)
            
            // Aktiviteyi sonlandırmayı atlıyoruz - burada finish() çağrısı YOK
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("MarketSimulatorActivity", "İstatistik kaydedilirken hata: ${e.message}")
            // Hata durumunda bile aktiviteyi kapatmıyoruz
        }
    }
    
    /**
     * Ödeme işlemini gerçekleştir
     * @param amount Kullanıcının verdiği para miktarı
     */
    private fun processPayment(amount: Double) {
        try {
            if (gameState.phase != MarketGamePhase.CHECKOUT) return
            val totalCost = gameState.shoppingCart.getTotalPrice()
            
            // Para yeterli mi?
            if (amount < totalCost) {
                showToast("Verdiğin para yeterli değil!")
                return
            }
            
            // Bütçe kontrolü
            if (gameState.level.budget > 0 && amount > gameState.level.budget) {
                showToast("Bütçeni aştığın için ödeme yapamazsın!")
                return
            }
            
            // Aynı ödeme aksiyonunun çift tıklama veya yeniden girişte ikinci kez kaydedilmesini engelle.
            if (!completionGate.tryFinish()) return

            // Para üstü hesapla
            val change = amount - totalCost
            
            // Puanı hesapla
            val timeBonus = (gameState.timeRemaining / 10.0).toInt() * 5
            val budgetBonus = if (totalCost <= gameState.level.budget * 0.8) 50 else 0
            val calculatedScore = 100 + timeBonus + budgetBonus
            
            // Yıldız hesapla (1-3 arası)
            val calculatedStars = when {
                calculatedScore >= 200 -> 3
                calculatedScore >= 150 -> 2
                else -> 1
            }
            
            // Zamanlayıcıyı durdur
            stopGameTimer()
            
            // Ses efekti çal - başarı sesi
            try {
                val audioService = com.yigit033.matematikmacarasi.services.AudioService.getInstance(this)
                audioService.playSuccessSound()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            // BaseGameActivity'de kullanılan değişkenleri ayarla
            currentLevel = selectedLevelIndex + 1  // Doğru seviye numarası
            score = calculatedScore  // Skor değişkenini güncelle
            stars = calculatedStars  // Yıldız değişkenini güncelle
            
            // Oyun durumunu güncelle - tamamlama ekranına geçiş için hazırla
            gameState = gameState.copy(
                phase = MarketGamePhase.COMPLETED,
                score = calculatedScore,
                stars = calculatedStars,
                cashGiven = amount
            )
            
            // Seviye ID formatını kullanarak oyun ilerlemesini kaydet
            gameProgressService.saveProgress(
                getNormalizedGameId(),  // BaseGameActivity'nin normalize ID metodunu kullan
                currentLevel, 
                score, 
                stars
            )
            
            // Özel metodu kullanarak istatistikleri güncelle (aktivite sonlandırmadan)
            saveGameStatsWithoutFinishing()
            
            // BAŞARILI OYUN BİTİŞİNDE AGRESİF REKLAM STRATEJİSİ
            try {
                val adManagerInstance = AdManager.getInstance(this)
                Log.d("MarketSimulatorActivity", "Başarılı oyun bitişinde agresif reklam gösterilecek")
                adManagerInstance.showInterstitialAdAfterGame(this)
            } catch (e: Exception) {
                Log.e("MarketSimulatorActivity", "Reklam gösterimi sırasında hata: ${e.message}", e)
            }
            
            // Oyun tamamlandığında tutorial'ı kapatmak için isFirstTime'ı false yap
            isFirstTime = false
            
            // StarManager ile güncel toplam yıldız sayısını al
            val totalStars = com.yigit033.matematikmacarasi.services.StarManager.getInstance(this).getTotalStars()
            
            // Kullanıcıya kazandığı yıldızları bildir
            Toast.makeText(this, 
                "Tebrikler! Seviye ${currentLevel}'den $stars yıldız kazandınız! Toplam: $totalStars yıldız", 
                Toast.LENGTH_LONG
            ).show()
            
            // Tamamlama ekranının gösterilmesi için UI'ı yeniden oluştur
            setContent {
                GameTheme {
                    GameContent()  // Bu, güncellenmiş oyun fazına göre içeriği gösterecek
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showToast("Ödeme işlemi sırasında bir hata oluştu")
        }
    }
    
    /**
     * Oyunu başarısız olarak bitir
     * @param reason Başarısızlık nedeni
     */
    private fun failGame(reason: String) {
        try {
            if (!completionGate.tryFinish()) return
            gameState = gameState.copy(
                phase = MarketGamePhase.FAILED,
                failReason = reason
            )
            
            // Zamanlayıcıyı durdur
            gameTimerTask?.let { handler.removeCallbacks(it) }
            
            // Başarısızlık, BaseGameActivity.endGame() üzerinden ödül/görev kaydı oluşturmaz.
            isGameRunning = false
            audioService.pauseBackgroundMusic()
        } catch (e: Exception) {
            e.printStackTrace()
            finish() // Hata durumunda doğrudan çık
        }
    }
    
    /**
     * Oyunu yeniden başlat
     */
    private fun restartGame() {
        // Oyun durumunu sıfırla
        stopGameTimer()
        
        // Aynı seviyede yeni bir dinamik alışveriş listesi ile oyunu başlat
        initGame(selectedLevelIndex)
    }
    
    override fun generateQuestion() {
        // Market oyununda soru üretimi yok
    }
    
    override fun checkAnswer(answer: Any): Boolean {
        // Market oyununda doğrudan cevap kontrolü yok
        return false
    }
    
    override fun updateDifficulty() {
        // Market oyununda zorluk güncellemesi yok
    }
    
    override fun onDestroy() {
        // AdManager kaynaklarını try-catch bloğu içerisinde temizlemeye çalış
        try {
            // AdManager'ı BaseActivity'den alıp kullanmaya çalışma (erişim hatası var)
            // Bunun yerine AdManager'ı doğrudan getInstance ile al
            AdManager.getInstance(this).destroy()
        } catch (e: Exception) {
            Log.e("MarketSimulatorActivity", "AdManager temizlenirken hata: ${e.message}")
        }
        
        super.onDestroy()
        
        // Zamanlayıcıyı temizle
        gameTimerTask?.let { handler.removeCallbacks(it) }
    }
    
    /**
     * Oyun zamanlayıcısını durdur
     */
    private fun stopGameTimer() {
        gameTimerTask?.let { handler.removeCallbacks(it) }
        gameTimerTask = null
    }
    
    /**
     * Günlük görev ilerlemesini güncelle
     */
    private fun updateDailyTaskProgress() {
        try {
            // DailyTaskManager ile günlük görev ilerlemesini güncelle
            dailyTaskManager.updateTaskProgressByGameType(gameType)
            Log.d("MarketSimulatorActivity", "Günlük görev güncellendi (gameType: $gameType)")
        } catch (e: Exception) {
            Log.e("MarketSimulatorActivity", "Günlük görev güncellenirken hata: ${e.message}")
            e.printStackTrace()
        }
    }
} 