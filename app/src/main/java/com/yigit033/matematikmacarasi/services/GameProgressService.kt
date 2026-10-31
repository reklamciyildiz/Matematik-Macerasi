package com.yigit033.matematikmacarasi.services

import android.content.Context
import com.yigit033.matematikmacarasi.models.GameProgress
import com.yigit033.matematikmacarasi.repositories.GameRepositoryManager
import com.yigit033.matematikmacarasi.repositories.CloudGameDataRepository

/**
 * GameProgressService, oyun ilerlemelerini yönetir.
 * Repository pattern kullanarak veri erişimi sağlar.
 */
class GameProgressService private constructor(private val context: Context) {

    companion object {
        @Volatile
        private var instance: GameProgressService? = null
        
        fun getInstance(context: Context): GameProgressService {
            return instance ?: synchronized(this) {
                instance ?: GameProgressService(context.applicationContext).also { instance = it }
            }
        }
    }
    
    // Repository manager
    private val repositoryManager = GameRepositoryManager.getInstance(context)
    
    /**
     * Bir oyunun ilerlemesini kaydeder.
     * @param gameId Oyunun benzersiz kimliği
     * @param level Oyun seviyesi
     * @param score Oyun puanı
     * @param stars Kazanılan yıldız sayısı
     */
    fun saveProgress(gameId: String, level: Int, score: Int, stars: Int) {
        try {
            repositoryManager.getRepository().saveProgress(gameId, level, score, stars)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    /**
     * Bir oyunun ilerlemesini alır.
     * @param gameId Oyunun benzersiz kimliği
     * @return GameProgress nesnesi olarak oyun ilerlemesi
     */
    fun getProgress(gameId: String): GameProgress {
        try {
            return repositoryManager.getRepository().getProgress(gameId)
        } catch (e: Exception) {
            e.printStackTrace()
            return GameProgress(gameId = gameId)
        }
    }
    
    /**
     * Toplam yıldız sayısını alır.
     * @return Tüm oyunlardan kazanılan toplam yıldız sayısı
     */
    fun getTotalStars(): Int {
        try {
            return repositoryManager.getRepository().getTotalStars()
        } catch (e: Exception) {
            e.printStackTrace()
            return 0
        }
    }
    
    /**
     * Mevcut yıldız sayısı için oyunu kilidi açılmış mı kontrol eder.
     * @param requiredStars Gereken yıldız sayısı
     * @return Oyunun kilidi açılmışsa true
     */
    fun isGameUnlocked(requiredStars: Int): Boolean {
        try {
            return repositoryManager.getRepository().isGameUnlocked(requiredStars)
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    /**
     * Bulut senkronizasyonunu etkinleştirir veya devre dışı bırakır.
     * Bu şu an için sadece ayarı değiştirir, ileride Supabase entegrasyonu eklenecektir.
     * @param enabled Bulut senkronizasyonu etkinse true
     */
    fun setCloudSyncEnabled(enabled: Boolean) {
        repositoryManager.setUseCloudRepository(enabled)
    }
    
    /**
     * Bulut senkronizasyonunun etkin olup olmadığını kontrol eder.
     * @return Bulut senkronizasyonu etkinse true
     */
    fun isCloudSyncEnabled(): Boolean {
        return repositoryManager.isUsingCloudRepository()
    }
    
    /**
     * CloudGameDataRepository'ye erişim sağlar.
     * Bu metod, bulut senkronizasyonu için kullanılır.
     * @return CloudGameDataRepository nesnesi
     * @throws IllegalStateException Bulut repository kullanılmıyorsa
     */
    fun getCloudRepository(): CloudGameDataRepository {
        val repository = repositoryManager.getRepository()
        if (repository is CloudGameDataRepository) {
            return repository
        } else {
            throw IllegalStateException("Bulut repository aktif değil")
        }
    }
} 