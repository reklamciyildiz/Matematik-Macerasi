package com.yigit033.matematikmacarasi.repositories

import android.content.Context
import android.util.Log
import com.yigit033.matematikmacarasi.models.GameProgress
// import com.yigit033.matematikmacarasi.services.SupabaseService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * CloudGameDataRepository, oyun verilerini yerel olarak saklar ve bulut senkronizasyonu simüle eder.
 * NOT: Gerçek bulut bağlantısı şu anda devre dışı bırakılmıştır ve tüm veriler yerel olarak saklanmaktadır.
 */
class CloudGameDataRepository(
    context: Context
) : GameDataRepository {
    
    // Yerel veri depolama
    private val localRepository = LocalGameDataRepository(context)
    
    // Supabase servisi - devre dışı bırakıldı
    // private val supabaseService = SupabaseService.getInstance(context)
    
    // Bulut ile senkronizasyon için CoroutineScope
    private val scope = CoroutineScope(Dispatchers.IO)
    
    // TAG for logging
    private val TAG = "CloudGameDataRepo"
    
    init {
        // Gerçek bulut senkronizasyonu devre dışı bırakıldı
        Log.d(TAG, "Bulut senkronizasyonu devre dışı bırakılmıştır, tüm veriler yerel olarak saklanacak")
        
        /* Devre dışı bırakılan bulut senkronizasyon kodu
        // Uygulama başlatıldığında arka planda senkronizasyon işlemini başlat
        scope.launch {
            try {
                // Bağlantı durumunu kontrol et
                if (supabaseService.connectionState.first() == SupabaseService.ConnectionState.CONNECTED) {
                    syncLocalToCloud()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Otomatik senkronizasyon hatası", e)
            }
        }
        */
    }
    
    override fun saveProgress(gameId: String, level: Int, score: Int, stars: Int) {
        // Sadece yerel olarak kaydet
        localRepository.saveProgress(gameId, level, score, stars)
        
        /* Devre dışı bırakılan bulut kaydetme kodu
        // Supabase'e asenkron olarak kaydet
        scope.launch {
            try {
                supabaseService.saveGameProgress(gameId, level, score, stars)
            } catch (e: Exception) {
                // Hata durumunda yerel kayıt tutulduğundan veri kaybı olmaz
                Log.e(TAG, "Bulut kaydetme hatası: ${e.message}")
            }
        }
        */
    }
    
    override fun getProgress(gameId: String): GameProgress {
        // Sadece yerel depodan oku
        return localRepository.getProgress(gameId)
        
        /* Devre dışı bırakılan bulut okuma kodu
        // Buluttan okuma işlemini asenkron olarak başlat ve senkronize et
        scope.launch {
            try {
                val cloudProgress = supabaseService.getGameProgress(gameId)
                
                // Bulut verisi varsa ve daha yeni ise yerel veriyi güncelle
                if (cloudProgress.highScore > localProgress.highScore || 
                    cloudProgress.stars > localProgress.stars) {
                    localRepository.saveProgress(
                        gameId = cloudProgress.gameId,
                        level = cloudProgress.level,
                        score = cloudProgress.highScore,
                        stars = cloudProgress.stars
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Bulut senkronizasyon hatası: ${e.message}")
            }
        }
        */
    }
    
    override fun getTotalStars(): Int {
        // Sadece yerel veritabanından oku
        return localRepository.getTotalStars()
        
        /* Devre dışı bırakılan bulut yıldız senkronizasyonu
        // Buluttan toplam yıldızları asenkron olarak getir ve karşılaştır
        scope.launch {
            try {
                val cloudTotalStars = supabaseService.getTotalStars()
                
                // Eğer bulut değeri daha yüksekse, yerel verileri güncelle
                if (cloudTotalStars > localTotalStars) {
                    // Buluttaki her oyun ilerlemesini yerel veriye senkronize et
                    syncCloudToLocal()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Bulut toplam yıldız hatası: ${e.message}")
            }
        }
        */
    }
    
    override fun isGameUnlocked(requiredStars: Int): Boolean {
        // Sadece yerel veritabanından kontrol et
        return localRepository.isGameUnlocked(requiredStars)
    }
    
    /**
     * Tüm yerel verileri bulut ile senkronize eder. (Simülasyon)
     */
    suspend fun syncWithCloud() {
        // Bulut senkronizasyonu devre dışı bırakıldı
        Log.d(TAG, "Bulut senkronizasyonu simülasyonu - gerçek bağlantı yok")
        
        /* Devre dışı bırakılan senkronizasyon
        // İki yönlü senkronizasyon
        syncLocalToCloud()
        syncCloudToLocal()
        */
    }
    
    /**
     * Yerel verileri buluta senkronize eder. (Simülasyon)
     */
    private suspend fun syncLocalToCloud() {
        // Bulut senkronizasyonu devre dışı bırakıldı
        Log.d(TAG, "Yerel -> Bulut senkronizasyon simülasyonu - gerçek bağlantı yok")
        
        /* Devre dışı bırakılan senkronizasyon
        try {
            // Tüm yerel oyun ilerlemelerini al
            val allLocalProgress = getAllLocalGameProgress()
            
            // Buluta senkronize et
            supabaseService.syncLocalDataToCloud(allLocalProgress)
        } catch (e: Exception) {
            Log.e(TAG, "Yerel -> Bulut senkronizasyon hatası: ${e.message}")
        }
        */
    }
    
    /**
     * Bulut verilerini yerele senkronize eder. (Simülasyon)
     */
    private suspend fun syncCloudToLocal() {
        // Bulut senkronizasyonu devre dışı bırakıldı
        Log.d(TAG, "Bulut -> Yerel senkronizasyon simülasyonu - gerçek bağlantı yok")
    }
    
    /**
     * Tüm yerel oyun ilerlemelerini alır.
     */
    private fun getAllLocalGameProgress(): List<GameProgress> {
        return runBlocking {
            val knownGameIds = listOf(
                "addition_game", "subtraction_game", "multiplication_game", "division_game",
                "balloon_pop_game", "number_hunt_game", "math_maze_game", "space_math_adventure"
            )
            
            knownGameIds.map { gameId -> localRepository.getProgress(gameId) }
        }
    }
} 