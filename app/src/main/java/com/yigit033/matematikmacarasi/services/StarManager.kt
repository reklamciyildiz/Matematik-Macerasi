package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import java.lang.Math
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Oyunlardan kazanılan yıldızları yöneten servis sınıfı.
 * Bu sınıf, yıldızları kaydeder, yükler ve maksimum limit uygular.
 */
class StarManager private constructor(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "star_manager_prefs"
        private const val KEY_TOTAL_STARS = "total_stars"
        private const val KEY_DISPLAY_STARS = "display_stars" // Avatar ve UI için kullanılan yıldızlar
        private const val KEY_GAME_STARS = "game_stars_"
        
        // Maksimum yıldız limiti - Avatar sisteminin desteklediği üst sınır
        private const val MAX_STARS = 1000
        
        @Volatile
        private var instance: StarManager? = null
        
        fun getInstance(context: Context): StarManager {
            return instance ?: synchronized(this) {
                instance ?: StarManager(context).also { 
                    instance = it
                    // Initialize by syncing with repository
                    it.syncStarsWithRepository()
                }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    /**
     * Toplam yıldız sayısını döndürür.
     * Bu, avatarlar için harcanan yıldızları da içerir.
     */
    fun getTotalStars(): Int {
        val gameStars = sharedPreferences.getInt(KEY_TOTAL_STARS, 0)
        
        // Maksimum limiti uygula
        val result = if (gameStars > MAX_STARS) MAX_STARS else gameStars
        
        // EventBus'a yıldız sayısını bildir - doğrudan sonucu kullan, getDisplayStars() kullanma
        EventBus.updateStars(result)
        
        return result
    }
    
    /**
     * Görüntülenecek yıldız sayısını döndürür.
     * Bu değer ana ekranlarda görüntülenir ve avatar açmak için kullanılabilir.
     */
    fun getDisplayStars(): Int {
        // Display stars değeri yoksa total stars ile başlat (geriye dönük uyumluluk)
        if (!sharedPreferences.contains(KEY_DISPLAY_STARS)) {
            // getTotalStars çağırmak yerine doğrudan KEY_TOTAL_STARS'ı al
            val totalStars = sharedPreferences.getInt(KEY_TOTAL_STARS, 0)
            sharedPreferences.edit().putInt(KEY_DISPLAY_STARS, totalStars).apply()
            return totalStars
        }
        
        return sharedPreferences.getInt(KEY_DISPLAY_STARS, 0)
    }
    
    /**
     * Oyunlardan gerçekte kazanılan yıldız sayısını döndürür.
     * Bu, oyuncu için daha doğru bir gösterimdir - her oyunda en fazla 3 yıldız ve 5 oyun.
     */
    fun getAdjustedDisplayStars(context: Context): Int {
        try {
            // Her oyun en fazla 3 seviye ve her seviyede en fazla 3 yıldız olabilir
            // 5 oyun x 3 seviye x 3 yıldız = 45 maksimum yıldız
            val MAX_POSSIBLE_STARS = 45
            
            // Olası tüm oyun kimliklerini al
            val allGameIds = getAllGameIds()
            
            // Tüm oyunlardan kazanılan yıldızları topla (maksimum 3 yıldız per oyun)
            var totalGameStars = 0
            val gameStarsLog = StringBuilder("Oyun Yıldızları: ")
            
            allGameIds.forEach { gameId ->
                val stars = getGameStars(gameId)
                if (stars > 0) {
                    gameStarsLog.append("[$gameId: $stars] ")
                    totalGameStars += stars // Her seviye için tüm yıldızları ekle
                }
            }
            
            // Toplam yıldız sayısını MAX_POSSIBLE_STARS ile sınırlandır
            val cappedStars = Math.min(totalGameStars, MAX_POSSIBLE_STARS)
            
            // Açılmış avatarların maliyetlerini hesapla
            val avatarService = AvatarService(context)
            val unlockedAvatars = avatarService.avatars.filter { it.isUnlocked && it.id != 1 } // ID 1 ücretsiz
            val spentStars = unlockedAvatars.sumOf { it.price }
            val unlockedAvatarIds = unlockedAvatars.map { it.id }
            
            // Kalan yıldız sayısı: Kazanılan yıldızlar - Harcanan yıldızlar, 0'dan küçük olamaz
            val resultStars = Math.max(0, cappedStars - spentStars)
            
            Log.d("StarManager", "Yıldız Hesabı: Toplam Oyun Yıldızları=$totalGameStars, " +
                    "Sınırlı Yıldızlar=$cappedStars, " +
                    "Açık Avatarlar=$unlockedAvatarIds, " +
                    "Harcanan Yıldızlar=$spentStars, " +
                    "Sonuç Yıldızlar=$resultStars")
            Log.d("StarManager", gameStarsLog.toString())
            
            return resultStars
        } catch (e: Exception) {
            Log.e("StarManager", "Düzeltilmiş yıldız sayısı hesaplanırken hata: ${e.message}", e)
            return 0
        }
    }
    
    /**
     * Tüm olası oyun kimliklerini döndürür (farklı seviyeler dahil)
     */
    private fun getAllGameIds(): List<String> {
        return listOf(
            // Temel oyunlar - seviye 1,2,3,4,5
            "addition_game_level_1", "addition_game_level_2", "addition_game_level_3", "addition_game_level_4", "addition_game_level_5",
            "subtraction_game_level_1", "subtraction_game_level_2", "subtraction_game_level_3", "subtraction_game_level_4", "subtraction_game_level_5",
            "multiplication_game_level_1", "multiplication_game_level_2", "multiplication_game_level_3", "multiplication_game_level_4", "multiplication_game_level_5",
            "division_game_level_1", "division_game_level_2", "division_game_level_3", "division_game_level_4", "division_game_level_5",
            
            // Eski format için geriye dönük uyumluluk (geçiş dönemi için)
            "addition_game_1", "addition_game_2", "addition_game_3",
            "subtraction_game_1", "subtraction_game_2", "subtraction_game_3",
            "multiplication_game_1", "multiplication_game_2", "multiplication_game_3",
            "division_game_1", "division_game_2", "division_game_3",
            
            // İnteraktif oyunlar - seviye bazlı
            "SpaceMathAdventureActivity_level_1", "SpaceMathAdventureActivity_level_2", "SpaceMathAdventureActivity_level_3",
            "BalloonPopGameActivity_level_1", "BalloonPopGameActivity_level_2", "BalloonPopGameActivity_level_3",
            "NumberHuntGameActivity_level_1", "NumberHuntGameActivity_level_2", "NumberHuntGameActivity_level_3",
            "MathMazeGameActivity_level_1", "MathMazeGameActivity_level_2", "MathMazeGameActivity_level_3",
            "MathMazeGameActivity_level_4", "MathMazeGameActivity_level_5",
            
            // Market Simulator oyunu - seviye bazlı
            "MarketSimulatorActivity_level_1", "MarketSimulatorActivity_level_2", "MarketSimulatorActivity_level_3",
            
            // İnteraktif oyunların ana ID'leri
            "SpaceMathAdventureActivity", 
            "BalloonPopGameActivity", 
            "NumberHuntGameActivity", 
            "MathMazeGameActivity",
            "MarketSimulatorActivity"
        )
    }
    
    /**
     * Belirli bir oyundan kazanılan yıldızları döndürür.
     */
    fun getGameStars(gameId: String): Int {
        val stars = sharedPreferences.getInt("$KEY_GAME_STARS$gameId", 0)
        if (stars > 0) {
            Log.d("StarManager", "Oyun $gameId için $stars yıldız bulundu")
        }
        return stars
    }
    
    /**
     * Bu metod, reklam izleme sonrası ödül olarak yıldız eklemeyi sağlar.
     * @param rewardAmount Eklenecek yıldız miktarı
     * @return Yıldız ekleme işlemi başarılı oldu mu?
     */
    suspend fun addRewardedStars(rewardAmount: Int): Boolean {
        Log.d("StarManager", "addRewardedStars çağrıldı: $rewardAmount yıldız eklenecek")
        
        if (rewardAmount <= 0) {
            Log.e("StarManager", "Geçersiz ödül miktarı: $rewardAmount, 0'dan büyük olmalı")
            return false
        }
        
        try {
            // Mevcut değerleri al
            val currentDisplayStars = getDisplayStars()
            val currentTotalStars = sharedPreferences.getInt(KEY_TOTAL_STARS, 0)
            
            // Yeni değerleri hesapla
            val newDisplayStars = currentDisplayStars + rewardAmount
            val newTotalStars = Math.min(MAX_STARS, currentTotalStars + rewardAmount)
            
            Log.d("StarManager", "Reklam ödülü: Mevcut Display=$currentDisplayStars, " +
                  "Mevcut Total=$currentTotalStars, " +
                  "Yeni Display=$newDisplayStars, " +
                  "Yeni Total=$newTotalStars")
            
            // ÖNEMLİ: TRANSACTION OLARAK tüm değerleri commit ile kaydet!
            val editor = sharedPreferences.edit()
            editor.putInt(KEY_DISPLAY_STARS, newDisplayStars)
            editor.putInt(KEY_TOTAL_STARS, newTotalStars)
            
            // Değişiklikleri hemen uygula (apply yerine COMMIT kullan)
            val result = editor.commit()
            
            if (!result) {
                Log.e("StarManager", "Reklam ödülü kaydedilemedi: SharedPreferences işlemi başarısız")
                return false
            }
            
            // Diğer depolama alanlarını GÜVENİLİR şekilde güncelle (commit ile)
            try {
                val gameProgressPrefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
                val gameProgressEditor = gameProgressPrefs.edit()
                gameProgressEditor.putInt("total_stars", newTotalStars)
                gameProgressEditor.putInt("display_stars", newDisplayStars)
                val currentRewardedStars = gameProgressPrefs.getInt("rewarded_stars", 0)
                gameProgressEditor.putInt("rewarded_stars", currentRewardedStars + rewardAmount)
                val gameProgressResult = gameProgressEditor.commit() // COMMIT
                if (gameProgressResult) Log.d("StarManager", "Game progress repository güncellendi")
                else Log.e("StarManager", "Game progress repository güncellenemedi")

                val backupPrefs = context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE)
                val backupResult = backupPrefs.edit()
                    .putInt("total_stars", newTotalStars)
                    .putInt("display_stars", newDisplayStars)
                    .putInt("rewarded_stars", currentRewardedStars + rewardAmount)
                    .commit() // COMMIT
                if (backupResult) Log.d("StarManager", "Yedek depolama güncellendi")
                else Log.e("StarManager", "Yedek depolama güncellenemedi")
                
            } catch (e: Exception) {
                Log.e("StarManager", "Depolama güncellemesi sırasında hata: ${e.message}", e)
                // Ana kayıt zaten yapıldı, devam edilebilir ama log önemli
            }
            
            // Doğrulama yap (Double-check)
            val verifiedDisplayStars = getDisplayStars()
            if (verifiedDisplayStars != newDisplayStars) {
                Log.e("StarManager", "Kritik tutarsızlık: Olması gereken=$newDisplayStars, Gerçek=$verifiedDisplayStars")
                // Acil düzeltme (try-catch içinde)
                try {
                   sharedPreferences.edit().putInt(KEY_DISPLAY_STARS, newDisplayStars).commit()
                   context.getSharedPreferences("game_progress", Context.MODE_PRIVATE).edit().putInt("display_stars", newDisplayStars).commit()
                   context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE).edit().putInt("display_stars", newDisplayStars).commit()
                   Log.d("StarManager", "Tutarsızlık düzeltme girişimi yapıldı")
                } catch (e: Exception) {
                   Log.e("StarManager", "Tutarsızlık düzeltme hatası: ${e.message}", e)
                }
            } else {
                 Log.d("StarManager", "Yıldız doğrulaması başarılı: $verifiedDisplayStars")
            }
            
            // Başarılı kayıttan sonra UI'ı GÜVENİLİR şekilde güncelle
            // CoroutineScope(Dispatchers.Main).launch { // <-- REMOVE LAUNCH
            try {
                // Sadece forceStarNotification kullan - DOĞRUDAN ÇAĞIR
                EventBus.forceStarNotification(newDisplayStars)
                Log.d("StarManager", "Reklam ödülü için bildirim yapıldı: $newDisplayStars")
            } catch (e: Exception) {
                Log.e("StarManager", "Reklam ödülü bildirim hatası: ${e.message}", e)
            }
            // } // <-- REMOVE LAUNCH END
            
            Log.d("StarManager", "Reklam izleme sonrası $rewardAmount yıldız başarıyla eklendi")
            return true
        } catch (e: Exception) {
            Log.e("StarManager", "Reklam ödül yıldızları eklenirken kritik hata: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Avatar satın alma gibi işlemler için yıldız harcama
     * @param amount Harcanacak yıldız miktarı
     * @return İşlem başarılı olduysa true, değilse false
     */
    fun spendStars(amount: Int): Boolean {
        if (amount <= 0) {
            Log.e("StarManager", "Yıldız harcanamadı: Miktar 0 veya negatif olamaz")
            return false
        }
        
        try {
            // ÖNEMLİ: Hem display stars hem de total stars değerlerini al
            val currentDisplayStars = getDisplayStars()
            val currentTotalStars = sharedPreferences.getInt(KEY_TOTAL_STARS, 0)
            
            // Yeterli yıldız var mı kontrol et
            if (currentDisplayStars < amount) {
                Log.e("StarManager", "Yıldız harcanamadı: Yetersiz yıldız (Mevcut: $currentDisplayStars, İstenen: $amount)")
                return false
            }
            
            // Yeni değerleri hesapla
            val newDisplayStars = currentDisplayStars - amount
            
            // KRİTİK: Tüm değerleri tek bir işlemde ve commit ile kaydet!
            val editor = sharedPreferences.edit()
            editor.putInt(KEY_DISPLAY_STARS, newDisplayStars)
            
            // İşlemi HEMEN uygula ve sonucu kontrol et
            val result = editor.commit()
            
            if (!result) {
                Log.e("StarManager", "Yıldız harcanamadı: SharedPreferences commit işlemi başarısız")
                return false
            }
            
            Log.d("StarManager", "$amount yıldız başarıyla harcandı. " + 
                  "Eski display: $currentDisplayStars, Yeni display: $newDisplayStars, " +
                  "Total stars: $currentTotalStars")
            
            // KRİTİK: Tüm repository'leri güncelle ve HER ZAMAN COMMIT KULLAN!
            try {
                // 1. Game progress repository
                val repoPrefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
                val repoResult = repoPrefs.edit()
                    .putInt("display_stars", newDisplayStars)
                    .commit() // commit ile HEMEN uygula
                
                if (repoResult) {
                    Log.d("StarManager", "Repository (game_progress) yıldızları güncellendi: $newDisplayStars")
                } else {
                    Log.e("StarManager", "Repository (game_progress) yıldızları güncellenemedi!")
                }
                
                // 2. Kendi preferences'ımızı da yedekle
                sharedPreferences.edit()
                    .putInt(KEY_DISPLAY_STARS, newDisplayStars)
                    .commit()
                
                // 3. Star manager backup
                val backupPrefs = context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE)
                backupPrefs.edit()
                    .putInt("display_stars", newDisplayStars)
                    .commit()
                Log.d("StarManager", "Tüm depolamalar güncellendi: $newDisplayStars")
            } catch (e: Exception) {
                Log.e("StarManager", "Repository güncellemesi hatası: ${e.message}")
                // Kritik hata değil, devam et
            }
            
            // KRİTİK: Güçlü bildirim için ana thread'de EventBus'a bildir
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // İlk bildirim - hemen
                    EventBus.forceStarNotification(newDisplayStars)
                    Log.d("StarManager", "Yıldız harcaması için ilk acil bildirim: $newDisplayStars")
                    
                    // Normal bildirim
                    EventBus.updateStars(newDisplayStars)
                    
                    // Kısa gecikme ve doğrulama ile tekrar bildir
                    kotlinx.coroutines.delay(250)
                    val verifiedStars = getDisplayStars()
                    
                    // Tutarsızlık varsa düzeltmeye çalış
                    if (verifiedStars != newDisplayStars) {
                        Log.w("StarManager", "Tutarsızlık tespit edildi: Beklenen=$newDisplayStars, Gerçek=$verifiedStars")
                        // Düzeltmeye çalış
                        sharedPreferences.edit().putInt(KEY_DISPLAY_STARS, newDisplayStars).commit()
                        context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
                            .edit().putInt("display_stars", newDisplayStars).commit()
                        context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE)
                            .edit().putInt("display_stars", newDisplayStars).commit()
                        
                        // Tekrar bildirim yap
                        EventBus.forceStarNotification(newDisplayStars)
                        EventBus.updateStars(newDisplayStars)
                    }
                    
                    Log.d("StarManager", "Yıldız harcaması bildirimleri tamamlandı")
                } catch (e: Exception) {
                    Log.e("StarManager", "EventBus bildirimi hatası (harcama): ${e.message}", e)
                    // Hata olsa bile son bir deneme daha yap
                    try {
                        EventBus.updateStars(newDisplayStars)
                    } catch (e2: Exception) {
                        Log.e("StarManager", "Son bildirim denemesi de başarısız: ${e2.message}")
                    }
                }
            }
            
            return true
        } catch (e: Exception) {
            Log.e("StarManager", "Yıldız harcanırken hata oluştu: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Belirli bir oyun için kazanılan yıldızları ekler.
     * @param gameId Oyun kimliği
     * @param stars Kazanılan yıldız sayısı
     * @return İşlem başarılı olduysa true
     */
    fun addStars(gameId: String, stars: Int): Boolean {
        Log.d("StarManager", "addStars çağrıldı: gameId=$gameId, stars=$stars")
        
        if (stars <= 0) {
            Log.e("StarManager", "Yıldız eklenemedi: Yıldız sayısı 0 veya negatif olamaz")
            return false
        }
        
        try {
            // Mevcut değerleri al
            val currentGameStars = getGameStars(gameId)
            val currentDisplayStars = getDisplayStars()
            val currentTotalStars = sharedPreferences.getInt(KEY_TOTAL_STARS, 0)
            
            // FIX: Aynı seviye için tekrarlanan yıldız kazanımını önle
            // Eğer bu seviyeden daha önce yıldız kazanılmışsa, sadece farkı ekle
            val newGameStars = stars // Yeni kazanılan yıldızlar
            
            // Eğer bu seviyeden daha önce yıldız kazanılmışsa ve yeni yıldız sayısı eskisinden küçük veya eşitse
            // hiç yıldız ekleme - avatar için yıldız sayısını artırma
            if (currentGameStars >= stars) {
                Log.d("StarManager", "Bu seviye ($gameId) için zaten ${currentGameStars} yıldız kazanılmış. " +
                        "Yeni kazanılan $stars yıldız için avatar yıldızı eklenmeyecek.")
                
                // Sadece oyun yıldızlarını güncelle (daha düşük veya eşit olsa bile)
                val editor = sharedPreferences.edit()
                editor.putInt("$KEY_GAME_STARS$gameId", stars)
                val result = editor.commit()
                
                return result
            }
            
            // Eğer yeni yıldız sayısı daha yüksekse, sadece farkı ekle
            val additionalStars = stars - currentGameStars
            
            Log.d("StarManager", "Yıldız Hesaplaması: Oyun($gameId)=" +
                "Önceki=${currentGameStars}, " +
                "Yeni Kazanılan=${stars}, " +
                "Eklenecek=${additionalStars}, " +
                "EskiDisplay=${currentDisplayStars}")
            
            // Maksimum sınıra kadar yıldızları ekle
            val newDisplayStars = Math.min(MAX_STARS, currentDisplayStars + additionalStars)
            val newTotalStars = Math.min(MAX_STARS, currentTotalStars + additionalStars)
            
            // Tüm değerleri tek bir transaction'da kaydet
            val editor = sharedPreferences.edit()
            editor.putInt("$KEY_GAME_STARS$gameId", stars) // Yeni yıldız sayısını kaydet
            editor.putInt(KEY_DISPLAY_STARS, newDisplayStars)
            editor.putInt(KEY_TOTAL_STARS, newTotalStars)
            
            // Değişiklikleri hemen uygula
            val result = editor.commit()
            
            if (!result) {
                Log.e("StarManager", "Yıldızlar kaydedilemedi: SharedPreferences işlemi başarısız")
                return false
            }
            
            // Diğer depolama alanlarını güncelle
            try {
                // Game progress repository güncelle
                val gameProgressPrefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
                gameProgressPrefs.edit()
                    .putInt("total_stars", newTotalStars)
                    .putInt("display_stars", newDisplayStars)
                    .putInt("$KEY_GAME_STARS$gameId", stars)
                    .commit()
                
                // Yedek depolamayı güncelle
                context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("total_stars", newTotalStars)
                    .putInt("display_stars", newDisplayStars)
                    .putInt("$KEY_GAME_STARS$gameId", stars)
                    .commit()
                
                Log.d("StarManager", "Tüm depolamalar güncellendi")
            } catch (e: Exception) {
                Log.e("StarManager", "Depolama güncellemesi sırasında hata: ${e.message}", e)
            }
            
            // UI'ı güncelle
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Acil bildirim
                    EventBus.forceStarNotification(newDisplayStars)
                    
                    // Normal bildirim
                    EventBus.updateStars(newDisplayStars)
                    
                    // Doğrulama kontrolü
                    kotlinx.coroutines.delay(250)
                    val verifiedStars = getDisplayStars()
                    if (verifiedStars != newDisplayStars) {
                        Log.w("StarManager", "Yıldız tutarsızlığı: Beklenen=$newDisplayStars, Gerçek=$verifiedStars")
                        // Düzeltme dene
                        sharedPreferences.edit().putInt(KEY_DISPLAY_STARS, newDisplayStars).commit()
                        EventBus.forceStarNotification(newDisplayStars)
                    }
                } catch (e: Exception) {
                    Log.e("StarManager", "EventBus bildirimi hatası: ${e.message}", e)
                }
            }
            
            Log.d("StarManager", "Yıldızlar başarıyla eklendi: gameId=$gameId, stars=$stars")
            return true
        } catch (e: Exception) {
            Log.e("StarManager", "Yıldız eklenirken hata: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Belirli bir oyunun yıldızlarını sıfırlar.
     * @param gameId Oyun kimliği
     */
    fun resetGameStars(gameId: String) {
        try {
            val currentGameStars = getGameStars(gameId)
            val currentTotalStars = sharedPreferences.getInt(KEY_TOTAL_STARS, 0)
            
            val editor = sharedPreferences.edit()
            editor.putInt("$KEY_GAME_STARS$gameId", 0)
            
            // Toplam yıldız sayısından çıkar
            val newTotalStars = currentTotalStars - currentGameStars
            editor.putInt(KEY_TOTAL_STARS, if (newTotalStars < 0) 0 else newTotalStars)
            
            editor.apply()
        } catch (e: Exception) {
            Log.e("StarManager", "Oyun yıldızları sıfırlanırken hata oluştu", e)
        }
    }
    
    /**
     * Tüm yıldızları sıfırlar.
     */
    fun resetAllStars() {
        try {
            // StarManager'daki tüm verileri temizle
            val editor = sharedPreferences.edit()
            editor.clear().commit() // commit kullanarak hemen uygulanmasını sağla
            
            // Repository'deki yıldız verilerini de temizle
            try {
                val repoPrefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
                repoPrefs.edit().putInt("total_stars", 0).commit()
                
                // Ayrıca tüm oyun ilerlemelerini sıfırla
                val allKeys = repoPrefs.all.keys.toList()
                val editor = repoPrefs.edit()
                
                allKeys.forEach { key ->
                    if (key.contains("_stars") || key.contains("_level") || key.contains("_score")) {
                        editor.remove(key)
                    }
                }
                editor.commit()
            } catch (e: Exception) {
                Log.e("StarManager", "Repository yıldızları sıfırlanırken hata: ${e.message}", e)
            }
            
            Log.d("StarManager", "Tüm yıldızlar başarıyla sıfırlandı")
        } catch (e: Exception) {
            Log.e("StarManager", "Tüm yıldızlar sıfırlanırken hata oluştu", e)
        }
    }
    
    /**
     * LocalGameDataRepository ile yıldız sayısını senkronize eder.
     * Bu metod, repository'deki total_stars değerini StarManager'a aktarır.
     * ÖNEMLİ: Bu işlem display_stars değerini ETKİLEMEZ.
     */
    fun syncStarsWithRepository() {
        try {
            // Mevcut display_stars değerini koru
            val currentDisplayStars = getDisplayStars()
            
            // Repository'den yıldız sayısını al
            val repository = com.yigit033.matematikmacarasi.repositories.LocalGameDataRepository(context)
            val repoTotalStars = repository.getTotalStars()
            
            // Mevcut total yıldız sayısını al
            val currentTotalStars = getTotalStars()
            
            // Eğer repository'deki yıldız sayısı daha fazlaysa, sadece total_stars'ı güncelle
            if (repoTotalStars > currentTotalStars) {
                Log.d("StarManager", "Senkronizasyon: Repository'den total_stars güncellendi: $repoTotalStars " +
                      "(eski: $currentTotalStars, display_stars değişmedi: $currentDisplayStars)")
                
                // Sadece total_stars'ı güncelle, display_stars'a dokunma
                val editor = sharedPreferences.edit()
                editor.putInt(KEY_TOTAL_STARS, repoTotalStars)
                editor.commit()
                
                // EventBus'a bildir - UI için display_stars değerini gönder
                EventBus.updateStars(currentDisplayStars)
            } 
            // Eğer StarManager'daki yıldız sayısı daha fazlaysa, repository'yi güncelle
            else if (currentTotalStars > repoTotalStars) {
                Log.d("StarManager", "Senkronizasyon: Repository'ye total_stars gönderildi: $currentTotalStars " +
                      "(eski: $repoTotalStars, display_stars değişmedi: $currentDisplayStars)")
                
                // SharedPreferences üzerinden doğrudan güncelleme
                val repoPrefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
                repoPrefs.edit().putInt("total_stars", currentTotalStars).commit()
                
                // EventBus'a bildir - UI için display_stars değerini gönder
                EventBus.updateStars(currentDisplayStars)
            }
        } catch (e: Exception) {
            Log.e("StarManager", "Yıldız senkronizasyonu sırasında hata: ${e.message}", e)
        }
    }
    
    /**
     * Bugüne kadar kazanılan toplam yıldız sayısını döndürür.
     * Bu metod, hem mevcut yıldızları hem de harcanmış yıldızları içerir.
     * İstatistikler için kullanılır.
     */
    fun getTotalEarnedStars(): Int {
        try {
            // Tüm olası oyun kimliklerini al
            val allGameIds = getAllGameIds()
            
            var totalEarnedStars = 0
            
            // Her oyunda kazanılan yıldızları topla
            allGameIds.forEach { gameId ->
                val stars = getGameStars(gameId)
                if (stars > 0) {
                    totalEarnedStars += stars
                }
            }
            
            return totalEarnedStars
        } catch (e: Exception) {
            Log.e("StarManager", "Toplam kazanılan yıldız sayısı alınırken hata: ${e.message}", e)
            return getTotalStars() // Hata durumunda en azından mevcut yıldızları döndür
        }
    }
    
    /**
     * Toplam yıldız sayısını doğrudan ayarlar.
     * Bu metot dikkatli kullanılmalıdır, genellikle test veya sıfırlama için.
     * @param stars Yeni toplam yıldız sayısı
     * @return İşlem başarılı olduysa true, değilse false
     */
    fun setTotalStars(stars: Int): Boolean {
        if (stars < 0) {
            Log.e("StarManager", "Yıldız sayısı negatif olamaz: $stars")
            return false
        }
        
        try {
            // KRİTİK: Mevcut display_stars değerini koru
            val currentDisplayStars = getDisplayStars()
            
            // Yeni yıldız sayısını kaydet
            val editor = sharedPreferences.edit()
            editor.putInt(KEY_TOTAL_STARS, stars)
            val result = editor.commit() // commit kullanarak hemen uygula
            
            if (result) {
                Log.d("StarManager", "Toplam yıldız sayısı ayarlandı: $stars (Display yıldızları değişmedi: $currentDisplayStars)")
                
                // Repository'yi de güncelle
                val repoPrefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
                repoPrefs.edit().putInt("total_stars", stars).commit()
                
                // KRİTİK: Display stars'ı DEĞİŞTİRMİYORUZ
                // Aşağıdaki satır kaldırıldı çünkü Profile'daki toplam yıldızları güncellerken
                // display_stars (mağazada harcanan yıldızlar) değişmemeli
                // sharedPreferences.edit().putInt(KEY_DISPLAY_STARS, stars).apply()
                
                // EventBus'u güncelle - display stars değerini gönder, total stars değil
                EventBus.updateStars(currentDisplayStars)
            } else {
                Log.e("StarManager", "Toplam yıldız sayısı kaydedilemedi")
            }
            
            return result
        } catch (e: Exception) {
            Log.e("StarManager", "Toplam yıldız sayısı ayarlanırken hata: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Harcanan yıldızları geri iade eder (satın alma iptal edildiğinde)
     * @param amount Geri verilecek yıldız miktarı
     * @return İşlem başarılı olduysa true, değilse false
     */
    fun addStars(amount: Int): Boolean {
        if (amount <= 0) {
            Log.e("StarManager", "Yıldız eklenemedi: Miktar 0 veya negatif olamaz")
            return false
        }
        
        try {
            // Mevcut display yıldız sayısını al
            val currentDisplayStars = getDisplayStars()
            
            // Yeni display yıldız sayısını hesapla
            val newDisplayStars = Math.min(currentDisplayStars + amount, MAX_STARS)
            
            // Display yıldızları ekle
            val editor = sharedPreferences.edit()
            editor.putInt(KEY_DISPLAY_STARS, newDisplayStars)
            
            // İşlemi hemen uygula (apply yerine commit kullan)
            val result = editor.commit()
            
            // Log bilgisi ekle
            if (result) {
                Log.d("StarManager", "$amount yıldız başarıyla iade edildi. Yeni display yıldız: $newDisplayStars")
                
                // EventBus ile yıldız sayısının değiştiğini bildir
                try {
                    EventBus.updateStars(newDisplayStars)
                } catch (e: Exception) {
                    Log.e("StarManager", "EventBus güncelleme hatası: ${e.message}", e)
                    // EventBus hatası kritik değil, devam et
                }
            } else {
                Log.e("StarManager", "Yıldız eklenemedi: SharedPreferences commit işlemi başarısız")
            }
            
            return result
        } catch (e: Exception) {
            Log.e("StarManager", "Yıldız eklenirken hata oluştu: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Görüntülenecek yıldız sayısını ayarlar.
     * Bu değer, kullanıcının görüntülenen ve avatar açmak için kullanılabilecek yıldız sayısıdır.
     * @param stars Ayarlanacak display yıldız sayısı
     * @return İşlem başarılı olduysa true, değilse false
     */
    fun setDisplayStars(stars: Int): Boolean {
        if (stars < 0) {
            Log.e("StarManager", "Display yıldız sayısı negatif olamaz: $stars")
            return false
        }
        
        try {
            // Mevcut değerleri al ve log'a yaz
            val currentDisplayStars = getDisplayStars()
            Log.d("StarManager", "setDisplayStars başladı: Mevcut=$currentDisplayStars, Yeni=$stars")
            
            // 1. AVATAR SİSTEMİ KRİTİK: Tüm değerleri HEMEN kaydet (commit)!
            val editor = sharedPreferences.edit()
            editor.putInt(KEY_DISPLAY_STARS, stars)
            val result = editor.commit() // commit kullanarak hemen uygula
            
            if (!result) {
                Log.e("StarManager", "Display yıldız sayısı kaydedilemedi")
                return false
            }
            
            // 2. KRİTİK: Anında UI bildirimi gönder
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // İlk bildirim - anında gönder
                    EventBus.forceStarNotification(stars)
                    Log.d("StarManager", "Birincil bildirim gönderildi: $stars")
                    
                    // Kısa bir gecikme ile ikinci bildirim (recomposition için)
                    delay(150)
                    EventBus.forceStarNotification(stars)
                    Log.d("StarManager", "İkincil bildirim gönderildi: $stars")
                } catch (e: Exception) {
                    Log.e("StarManager", "UI bildirim hatası: ${e.message}", e)
                }
            }
            
            // 3. TÜM depolama alanlarını güncelle (commit ile)
            try {
                // Game progress repository
                val repoPrefs = context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
                repoPrefs.edit()
                    .putInt("display_stars", stars)
                    .commit() // commit ile HEMEN uygula
                
                // Yedek depolama da güncelle
                context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putInt("display_stars", stars)
                    .commit()
                
                Log.d("StarManager", "Tüm depolamalar güncellendi: $stars")
            } catch (e: Exception) {
                Log.e("StarManager", "Depolama güncellemesi hatası: ${e.message}", e)
                // Ana kayıt zaten yapıldı, devam edebiliriz
            }
            
            // 4. Doğrulama kontrolü
            val verifiedStars = getDisplayStars()
            if (verifiedStars != stars) {
                Log.e("StarManager", "KRİTİK TUTARSIZLIK: Ayarlanan=$stars, Okunan=$verifiedStars")
                
                // Tutarsızlık durumunda son bir düzeltme denemesi
                try {
                    // Tüm depolamalara tekrar kaydet
                    sharedPreferences.edit().putInt(KEY_DISPLAY_STARS, stars).commit()
                    context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
                        .edit().putInt("display_stars", stars).commit()
                    context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE)
                        .edit().putInt("display_stars", stars).commit()
                    
                    // Tekrar bildirim gönder
                    CoroutineScope(Dispatchers.Main).launch {
                        EventBus.forceStarNotification(stars)
                        Log.d("StarManager", "Son düzeltme bildirimi gönderildi: $stars")
                    }
                } catch (e: Exception) {
                    Log.e("StarManager", "Son düzeltme hatası: ${e.message}", e)
                }
                
                // Tutarsızlık olsa bile ilk kayıt başarılı olduğundan true döndür
                return true
            }
            
            Log.d("StarManager", "setDisplayStars başarılı: $stars")
            return true
        } catch (e: Exception) {
            Log.e("StarManager", "Display yıldız ayarlama hatası: ${e.message}", e)
            return false
        }
    }
} 