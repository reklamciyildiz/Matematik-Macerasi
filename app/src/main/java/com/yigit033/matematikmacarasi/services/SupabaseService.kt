package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.yigit033.matematikmacarasi.config.SupabaseConfig
import com.yigit033.matematikmacarasi.models.GameProgress
import com.yigit033.matematikmacarasi.services.api.AuthResponse
import com.yigit033.matematikmacarasi.services.api.RetrofitClient
import com.yigit033.matematikmacarasi.services.api.SignInAnonymouslyRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import retrofit2.HttpException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Supabase ile ilgili veri modellerini serialize edebilmek için sınıflar
 */
@Serializable
data class SupabaseGameProgress(
    val id: Int? = null,
    val user_id: String,
    val game_id: String,
    val level: Int,
    val score: Int,
    val stars: Int,
    val last_updated: String = ""
)

@Serializable
data class SupabaseUser(
    val id: String,
    val username: String? = null,
    val avatar_url: String? = null,
    val total_stars: Int = 0,
    val created_at: String = ""
)

/**
 * SupabaseService, Supabase servisleri ile iletişimi sağlar.
 * Supabase REST API'sini kullanarak veri alışverişi yapar.
 */
class SupabaseService private constructor(private val context: Context) {
    
    // SharedPreferences için anahtarlar
    companion object {
        private const val TAG = "SupabaseService"
        private const val PREFS_NAME = "supabase_prefs"
        private const val KEY_AUTH_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER_ID = "user_id"
        
        // Singleton yapı
        @Volatile
        private var instance: SupabaseService? = null
        
        fun getInstance(context: Context): SupabaseService {
            return instance ?: synchronized(this) {
                instance ?: SupabaseService(context.applicationContext).also { instance = it }
            }
        }
    }
    
    // Bağlantı durumu
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    // Kullanıcı bilgisi
    private val _currentUser = MutableStateFlow<SupabaseUser?>(null)
    val currentUser: StateFlow<SupabaseUser?> = _currentUser.asStateFlow()
    
    // API istemcileri
    private val supabaseApi = RetrofitClient.createSupabaseApi()
    private val supabaseAuthApi = RetrofitClient.createSupabaseAuthApi()
    
    // SharedPreferences
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // Kimlik doğrulama token'ları
    private var authToken: String? = null
    private var userId: String? = null
    
    // Coroutine scope
    private val scope = CoroutineScope(Dispatchers.IO)
    
    init {
        // Token'ları SharedPreferences'dan yükle
        authToken = prefs.getString(KEY_AUTH_TOKEN, null)
        userId = prefs.getString(KEY_USER_ID, null)
        
        // Bağlantı durumunu izle ve gerekirse otomatik yeniden bağlan
        scope.launch {
            try {
                _connectionState.value = ConnectionState.CONNECTING
                checkAndInitializeSession()
            } catch (e: Exception) {
                Log.e(TAG, "Supabase başlatma hatası", e)
                _connectionState.value = ConnectionState.ERROR
            }
        }
    }
    
    /**
     * Mevcut oturum durumunu kontrol eder ve oturum yoksa anonim oturum açar.
     */
    private suspend fun checkAndInitializeSession() {
        try {
            if (authToken != null && userId != null) {
                // Oturum var, kullanıcı bilgisini al
                fetchCurrentUserData()
                _connectionState.value = ConnectionState.CONNECTED
            } else {
                // Oturum yok, anonim oturum aç
                signInAnonymously()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Oturum kontrolü sırasında hata", e)
            _connectionState.value = ConnectionState.ERROR
        }
    }
    
    /**
     * Anonim olarak oturum açar.
     */
    private suspend fun signInAnonymously() {
        try {
            val response = supabaseAuthApi.signUpAnonymously(
                request = SignInAnonymouslyRequest(mapOf("username" to "Player_${UUID.randomUUID().toString().substring(0, 6)}")),
                apiKey = SupabaseConfig.SUPABASE_ANON_KEY
            )
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                saveAuthData(authResponse)
                createUserProfile()
                _connectionState.value = ConnectionState.CONNECTED
            } else {
                Log.e(TAG, "Anonim giriş başarısız: ${response.errorBody()?.string()}")
                _connectionState.value = ConnectionState.ERROR
            }
        } catch (e: Exception) {
            Log.e(TAG, "Anonim giriş sırasında hata", e)
            _connectionState.value = ConnectionState.ERROR
        }
    }
    
    /**
     * Kimlik doğrulama verilerini kaydeder.
     */
    private fun saveAuthData(authResponse: AuthResponse) {
        authToken = "${authResponse.token_type} ${authResponse.access_token}"
        userId = authResponse.user.id
        
        prefs.edit()
            .putString(KEY_AUTH_TOKEN, authToken)
            .putString(KEY_REFRESH_TOKEN, authResponse.refresh_token)
            .putString(KEY_USER_ID, userId)
            .apply()
    }
    
    /**
     * Kullanıcı profili oluşturur veya mevcut profili getirir.
     */
    private suspend fun createUserProfile() {
        try {
            val userId = userId ?: return
            
            // Kullanıcı zaten var mı kontrol et
            val response = supabaseApi.getUser(
                userId = userId,
                apiKey = "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}",
                authKey = SupabaseConfig.SUPABASE_ANON_KEY
            )
            
            if (response.isSuccessful) {
                val users = response.body()
                if (users.isNullOrEmpty()) {
                    // Yeni kullanıcı profili oluştur
                    val newUser = SupabaseUser(
                        id = userId,
                        username = "Player_${userId.substring(0, 6)}",
                        total_stars = 0,
                        created_at = getCurrentTimeString()
                    )
                    
                    val createResponse = supabaseApi.createUser(
                        user = newUser,
                        apiKey = "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}",
                        authKey = SupabaseConfig.SUPABASE_ANON_KEY
                    )
                    
                    if (createResponse.isSuccessful && createResponse.body() != null) {
                        _currentUser.value = createResponse.body()
                    } else {
                        Log.e(TAG, "Kullanıcı oluşturma başarısız: ${createResponse.errorBody()?.string()}")
                    }
                } else {
                    _currentUser.value = users[0]
                }
            } else {
                Log.e(TAG, "Kullanıcı sorgulama başarısız: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kullanıcı profili oluşturma hatası", e)
        }
    }
    
    /**
     * Mevcut kullanıcının verilerini getirir.
     */
    private suspend fun fetchCurrentUserData() {
        try {
            val userId = userId ?: return
            
            val response = supabaseApi.getUser(
                userId = userId,
                apiKey = "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}",
                authKey = SupabaseConfig.SUPABASE_ANON_KEY
            )
            
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                _currentUser.value = response.body()!![0]
            } else {
                Log.e(TAG, "Kullanıcı verisi getirme başarısız: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kullanıcı verisi getirme hatası", e)
        }
    }
    
    /**
     * Oyun ilerlemesini bulutta günceller.
     */
    suspend fun saveGameProgress(gameId: String, level: Int, score: Int, stars: Int) {
        try {
            if (_connectionState.value != ConnectionState.CONNECTED) {
                Log.w(TAG, "Supabase bağlantısı olmadan ilerleme kaydedilemez")
                return
            }
            
            val userId = userId ?: return
            
            // Önce bu oyun için kaydın olup olmadığını kontrol et
            val response = supabaseApi.getGameProgress(
                userId = userId,
                gameId = gameId,
                apiKey = "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}",
                authKey = SupabaseConfig.SUPABASE_ANON_KEY
            )
            
            if (response.isSuccessful) {
                val progressList = response.body()
                
                if (!progressList.isNullOrEmpty()) {
                    // Mevcut kaydı güncelle (eğer yeni skor daha iyiyse)
                    val existingProgress = progressList[0]
                    if (existingProgress.score < score || existingProgress.stars < stars) {
                        val updateResponse = supabaseApi.updateGameProgress(
                            id = existingProgress.id!!,
                            updates = mapOf(
                                "level" to level,
                                "score" to score,
                                "stars" to stars,
                                "last_updated" to getCurrentTimeString()
                            ),
                            apiKey = "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}",
                            authKey = SupabaseConfig.SUPABASE_ANON_KEY
                        )
                        
                        if (!updateResponse.isSuccessful) {
                            Log.e(TAG, "İlerleme güncelleme başarısız: ${updateResponse.errorBody()?.string()}")
                        }
                    }
                } else {
                    // Yeni kayıt oluştur
                    val newProgress = SupabaseGameProgress(
                        user_id = userId,
                        game_id = gameId,
                        level = level,
                        score = score,
                        stars = stars,
                        last_updated = getCurrentTimeString()
                    )
                    
                    val createResponse = supabaseApi.createGameProgress(
                        progress = newProgress,
                        apiKey = "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}",
                        authKey = SupabaseConfig.SUPABASE_ANON_KEY
                    )
                    
                    if (!createResponse.isSuccessful) {
                        Log.e(TAG, "İlerleme oluşturma başarısız: ${createResponse.errorBody()?.string()}")
                    }
                }
                
                // Toplam yıldız sayısını güncelle
                updateTotalStars()
            } else {
                Log.e(TAG, "İlerleme sorgulama başarısız: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Oyun ilerlemesi kaydetme hatası", e)
        }
    }
    
    /**
     * Toplam yıldız sayısını hesaplar ve kullanıcı profilini günceller.
     */
    private suspend fun updateTotalStars() {
        try {
            val userId = userId ?: return
            
            // Kullanıcının tüm oyunlarındaki yıldızları topla
            val response = supabaseApi.getAllGameProgress(
                userId = userId,
                apiKey = "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}",
                authKey = SupabaseConfig.SUPABASE_ANON_KEY
            )
            
            if (response.isSuccessful && response.body() != null) {
                val totalStars = response.body()!!.sumOf { it.stars }
                
                // Kullanıcı profilini güncelle
                val updateResponse = supabaseApi.updateUser(
                    userId = userId,
                    updates = mapOf("total_stars" to totalStars),
                    apiKey = "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}",
                    authKey = SupabaseConfig.SUPABASE_ANON_KEY
                )
                
                if (updateResponse.isSuccessful && updateResponse.body() != null) {
                    _currentUser.value = updateResponse.body()
                } else {
                    Log.e(TAG, "Toplam yıldız güncelleme başarısız: ${updateResponse.errorBody()?.string()}")
                }
            } else {
                Log.e(TAG, "Oyun ilerlemeleri alınamadı: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Toplam yıldız güncelleme hatası", e)
        }
    }
    
    /**
     * Belirli bir oyunun ilerlemesini getirir.
     */
    suspend fun getGameProgress(gameId: String): GameProgress = withContext(Dispatchers.IO) {
        try {
            if (_connectionState.value != ConnectionState.CONNECTED) {
                Log.w(TAG, "Supabase bağlantısı olmadan ilerleme alınamaz")
                return@withContext GameProgress(gameId = gameId)
            }
            
            val userId = userId ?: return@withContext GameProgress(gameId = gameId)
            
            val response = supabaseApi.getGameProgress(
                userId = userId,
                gameId = gameId,
                apiKey = "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}",
                authKey = SupabaseConfig.SUPABASE_ANON_KEY
            )
            
            if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                val progress = response.body()!![0]
                return@withContext GameProgress(
                    gameId = gameId,
                    level = progress.level,
                    highScore = progress.score,
                    stars = progress.stars
                )
            }
            
            return@withContext GameProgress(gameId = gameId)
        } catch (e: Exception) {
            Log.e(TAG, "Oyun ilerlemesi getirme hatası", e)
            return@withContext GameProgress(gameId = gameId)
        }
    }
    
    /**
     * Toplam yıldız sayısını getirir.
     */
    suspend fun getTotalStars(): Int = withContext(Dispatchers.IO) {
        try {
            if (_connectionState.value != ConnectionState.CONNECTED) {
                Log.w(TAG, "Supabase bağlantısı olmadan toplam yıldız alınamaz")
                return@withContext 0
            }
            
            _currentUser.value?.total_stars ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Toplam yıldız alma hatası", e)
            0
        }
    }
    
    /**
     * Yerel verileri bulut ile senkronize eder.
     */
    suspend fun syncLocalDataToCloud(localProgressList: List<GameProgress>) {
        try {
            if (_connectionState.value != ConnectionState.CONNECTED) {
                Log.w(TAG, "Supabase bağlantısı olmadan senkronizasyon yapılamaz")
                return
            }
            
            localProgressList.forEach { progress ->
                saveGameProgress(
                    gameId = progress.gameId,
                    level = progress.level,
                    score = progress.highScore,
                    stars = progress.stars
                )
            }
            
            Log.d(TAG, "${localProgressList.size} oyun ilerlemesi senkronize edildi")
        } catch (e: Exception) {
            Log.e(TAG, "Senkronizasyon hatası", e)
        }
    }
    
    /**
     * Oturumu kapatır.
     */
    suspend fun signOut() {
        try {
            if (authToken != null) {
                supabaseAuthApi.signOut(
                    authToken = authToken!!,
                    apiKey = SupabaseConfig.SUPABASE_ANON_KEY
                )
            }
            
            // Yerel verileri temizle
            prefs.edit()
                .remove(KEY_AUTH_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_USER_ID)
                .apply()
            
            authToken = null
            userId = null
            _currentUser.value = null
            _connectionState.value = ConnectionState.DISCONNECTED
            
            Log.d(TAG, "Oturum kapatıldı")
        } catch (e: Exception) {
            Log.e(TAG, "Çıkış sırasında hata", e)
        }
    }
    
    /**
     * Kullanıcı adını günceller.
     */
    suspend fun updateUsername(newUsername: String) {
        try {
            if (_connectionState.value != ConnectionState.CONNECTED) {
                Log.w(TAG, "Supabase bağlantısı olmadan kullanıcı adı güncellenemez")
                return
            }
            
            val userId = userId ?: return
            
            val response = supabaseApi.updateUser(
                userId = userId,
                updates = mapOf("username" to newUsername),
                apiKey = "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}",
                authKey = SupabaseConfig.SUPABASE_ANON_KEY
            )
            
            if (response.isSuccessful && response.body() != null) {
                _currentUser.value = response.body()
                Log.d(TAG, "Kullanıcı adı güncellendi: $newUsername")
            } else {
                Log.e(TAG, "Kullanıcı adı güncelleme başarısız: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Kullanıcı adı güncelleme hatası", e)
        }
    }
    
    /**
     * Avatar URL'sini günceller.
     */
    suspend fun updateAvatarUrl(newAvatarUrl: String) {
        try {
            if (_connectionState.value != ConnectionState.CONNECTED) {
                Log.w(TAG, "Supabase bağlantısı olmadan avatar URL'si güncellenemez")
                return
            }
            
            val userId = userId ?: return
            
            val response = supabaseApi.updateUser(
                userId = userId,
                updates = mapOf("avatar_url" to newAvatarUrl),
                apiKey = "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}",
                authKey = SupabaseConfig.SUPABASE_ANON_KEY
            )
            
            if (response.isSuccessful && response.body() != null) {
                _currentUser.value = response.body()
                Log.d(TAG, "Avatar URL güncellendi: $newAvatarUrl")
            } else {
                Log.e(TAG, "Avatar URL güncelleme başarısız: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Avatar URL güncelleme hatası", e)
        }
    }
    
    /**
     * Şu anki zaman damgasını string olarak döndürür.
     */
    private fun getCurrentTimeString(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        return sdf.format(Date())
    }
    
    /**
     * Supabase bağlantısını test eder.
     * @return true eğer bağlantı başarılıysa, false değilse
     */
    suspend fun testConnection(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val response = supabaseApi.testConnection(
                    apiKey = "Bearer ${SupabaseConfig.SUPABASE_ANON_KEY}",
                    authKey = SupabaseConfig.SUPABASE_ANON_KEY
                )
                
                val isConnected = response.isSuccessful
                if (isConnected) {
                    _connectionState.value = ConnectionState.CONNECTED
                } else {
                    _connectionState.value = ConnectionState.ERROR
                    Log.e(TAG, "Bağlantı testi başarısız: ${response.errorBody()?.string()}")
                }
                
                return@withContext isConnected
            } catch (e: Exception) {
                Log.e(TAG, "Bağlantı testi sırasında hata", e)
                _connectionState.value = ConnectionState.ERROR
                return@withContext false
            }
        }
    }
    
    /**
     * Bağlantı durumunu temsil eden enum.
     */
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        ERROR
    }
} 