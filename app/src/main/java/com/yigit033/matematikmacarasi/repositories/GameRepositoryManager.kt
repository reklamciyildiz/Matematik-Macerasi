package com.yigit033.matematikmacarasi.repositories

import android.content.Context
import android.content.SharedPreferences
import com.yigit033.matematikmacarasi.services.ConnectionMode

/**
 * GameRepositoryManager, uygulamanın hangi veri deposunu kullanacağını yönetir.
 * Uygulama ayarlarına bağlı olarak yerel veya bulut veri deposunu sağlar.
 */
class GameRepositoryManager private constructor(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "repository_settings"
        private const val KEY_USE_CLOUD = "use_cloud_repository"
        private const val KEY_CONNECTION_MODE = "connection_mode"
        
        @Volatile
        private var instance: GameRepositoryManager? = null
        
        fun getInstance(context: Context): GameRepositoryManager {
            return instance ?: synchronized(this) {
                instance ?: GameRepositoryManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    // Ayarlar
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Lazy initialized repositories
    private val localRepository by lazy { LocalGameDataRepository(context) }
    internal val cloudRepository by lazy { CloudGameDataRepository(context) }
    
    // Varsayılan bağlantı modu (çevrimdışı)
    private var connectionMode = ConnectionMode.OFFLINE
    
    init {
        // Bağlantı modunu yükle
        val modeName = prefs.getString(KEY_CONNECTION_MODE, ConnectionMode.OFFLINE.name)
        connectionMode = ConnectionMode.valueOf(modeName ?: ConnectionMode.OFFLINE.name)
    }
    
    /**
     * Uygulamanın kullanması gereken repository'yi döndürür.
     * Ayarlara ve bağlantı moduna bağlı olarak yerel veya bulut repository olabilir.
     */
    fun getRepository(): GameDataRepository {
        val useCloud = prefs.getBoolean(KEY_USE_CLOUD, false)
        
        // Bulut repository'yi sadece online modda ve bulut kullanımı etkinse kullan
        return if (useCloud && connectionMode == ConnectionMode.ONLINE) {
            cloudRepository
        } else {
            localRepository
        }
    }
    
    /**
     * Repository tipini buluta ayarlar veya kaldırır.
     * @param useCloud true ise bulut repository kullanılır,
     *                 false ise yerel repository kullanılır.
     */
    fun setUseCloudRepository(useCloud: Boolean) {
        prefs.edit().putBoolean(KEY_USE_CLOUD, useCloud).apply()
    }
    
    /**
     * Mevcut repository ayarını döndürür.
     * @return true ise bulut repository kullanılıyor,
     *         false ise yerel repository kullanılıyor.
     */
    fun isUsingCloudRepository(): Boolean {
        return prefs.getBoolean(KEY_USE_CLOUD, false)
    }
    
    /**
     * Bağlantı modunu ayarlar.
     * @param mode Yeni bağlantı modu (ONLINE veya OFFLINE)
     */
    fun setConnectionMode(mode: ConnectionMode) {
        connectionMode = mode
        prefs.edit().putString(KEY_CONNECTION_MODE, mode.name).apply()
    }
    
    /**
     * Mevcut bağlantı modunu döndürür.
     */
    fun getConnectionMode(): ConnectionMode {
        return connectionMode
    }
    
    /**
     * Bulut repository'yi döndürür.
     * Not: Bu metot, bulut repository ile doğrudan etkileşim gerektiren
     * durumlar için kullanılır (örn. manuel senkronizasyon).
     */
    fun getCloudRepository(): CloudGameDataRepository {
        return cloudRepository
    }
} 