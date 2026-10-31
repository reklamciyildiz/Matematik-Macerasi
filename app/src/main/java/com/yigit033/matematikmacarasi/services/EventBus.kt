package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.util.Log
import com.yigit033.matematikmacarasi.DailyTask
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.channels.BufferOverflow
import java.util.concurrent.atomic.AtomicLong

/**
 * EventBus, komponentler arasında iletişimi sağlayan bir sınıftır.
 * SharedFlow ve StateFlow kullanarak olayları ve durumları paylaşır.
 */
object EventBus {
    // Avatar değişikliği olayı için SharedFlow - optimize edilmiş yapı
    private val _avatarChangedEvents = MutableSharedFlow<AvatarChangedEvent>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val avatarChangedEvents: SharedFlow<AvatarChangedEvent> = _avatarChangedEvents.asSharedFlow()
    
    // Yıldız güncellemeleri için SharedFlow
    private val _starUpdatedEvents = MutableSharedFlow<StarUpdatedEvent>(
        replay = 1,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val starUpdatedEvents: SharedFlow<StarUpdatedEvent> = _starUpdatedEvents.asSharedFlow()
    
    // Güncel avatar ID'si için StateFlow
    private val _currentAvatarId = MutableStateFlow(1) // Varsayılan değer
    val currentAvatarId: StateFlow<Int> = _currentAvatarId.asStateFlow()
    
    // Güncel yıldız sayısı için StateFlow
    private val _currentStars = MutableStateFlow(0) // Varsayılan değer
    val currentStars: StateFlow<Int> = _currentStars.asStateFlow()
    
    // Günlük görevler (DailyTasks) için StateFlow
    private val _dailyTasks = MutableStateFlow<List<DailyTask>>(emptyList())
    val dailyTasks: StateFlow<List<DailyTask>> = _dailyTasks.asStateFlow()
    
    // Profil güncelleme zamanını takip eden StateFlow
    private val _profileUpdateTime = MutableStateFlow(0L)
    val profileUpdateTime: StateFlow<Long> = _profileUpdateTime.asStateFlow()
    
    // Offline modu takip eden StateFlow
    private val _offlineMode = MutableStateFlow(false)
    val offlineMode: StateFlow<Boolean> = _offlineMode.asStateFlow()
    
    // Throttling için son bildirim zamanı
    private val lastEmitTime = AtomicLong(0)
    private val lastStarEmitTime = AtomicLong(0)
    
    // KRİTİK: Context değişkeni ekliyoruz
    private lateinit var context: Context
    
    // KRİTİK: Context'i başlatmak için init metodu
    fun initialize(appContext: Context) {
        context = appContext.applicationContext
        Log.d("EventBus", "EventBus context başlatıldı")
    }
    
    /**
     * Avatar değişikliği olayını yayınlar
     */
    suspend fun publishAvatarChanged(avatarId: Int) {
        try {
            // Her zaman state'i güncelle
            _currentAvatarId.value = avatarId
            
            // Olayı yayınla
            _avatarChangedEvents.emit(AvatarChangedEvent(avatarId))
            Log.d("EventBus", "Avatar değişikliği yayınlandı: Avatar ID = $avatarId")
        } catch (e: Exception) {
            Log.e("EventBus", "Avatar değişikliği yayınlanırken hata: ${e.message}", e)
        }
    }
    
    /**
     * Yıldız güncellemesi olayını yayınlar
     */
    suspend fun publishStarUpdated(stars: Int) {
        try {
            // Her zaman state'i güncelle
            _currentStars.value = stars
            
            // Olayı yayınla
            _starUpdatedEvents.emit(StarUpdatedEvent(stars))
            Log.d("EventBus", "Yıldız güncellemesi yayınlandı: Yıldız sayısı = $stars")
        } catch (e: Exception) {
            Log.e("EventBus", "Yıldız güncellemesi yayınlanırken hata: ${e.message}", e)
        }
    }
    
    /**
     * Avatar değişikliğini acil olarak bildirir - tüm ekranlar için
     */
    suspend fun forceAvatarNotification(avatarId: Int) {
        try {
            _currentAvatarId.value = avatarId
            _avatarChangedEvents.emit(AvatarChangedEvent(avatarId))
            Log.d("EventBus", "ZORUNLU avatar değişikliği bildirimi yapıldı: $avatarId")
        } catch (e: Exception) {
            Log.e("EventBus", "Zorunlu avatar bildirimi sırasında hata: ${e.message}", e)
        }
    }
    
    /**
     * Yıldız güncellemesini acil olarak bildirir - tüm ekranlar için
     */
    suspend fun forceStarNotification(stars: Int) {
        try {
            // Her zaman state değerini güncelle
            _currentStars.value = stars
            
            // Bildirimi yap
            _starUpdatedEvents.emit(StarUpdatedEvent(stars))
            
            // Emit işlemi başarılı oldu mu kontrol et
            val emitSuccess = _starUpdatedEvents.subscriptionCount.value > 0
            if (emitSuccess) {
                Log.d("EventBus", "ZORUNLU yıldız bildirimi yapıldı: $stars (${_starUpdatedEvents.subscriptionCount.value} alıcı)")
            } else {
                Log.d("EventBus", "ZORUNLU yıldız bildirimi yapıldı: $stars (alıcı yok)")
            }
            
            // KRİTİK: Main UI Thread'de bir kez daha dene - Mağaza Ekranı sorunu çözümü
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // 100ms gecikme ile yeniden dene (Compose recomposition için)
                    kotlinx.coroutines.delay(100)
                    _currentStars.value = stars
                    _starUpdatedEvents.emit(StarUpdatedEvent(stars))
                    Log.d("EventBus", "ZORUNLU yıldız bildirimi Main Thread ile tekrarlandı: $stars")
                    
                    // Bir kez daha dene (farklı bir gecikme ile)
                    kotlinx.coroutines.delay(300)
                    _currentStars.value = stars
                    _starUpdatedEvents.emit(StarUpdatedEvent(stars))
                    Log.d("EventBus", "ZORUNLU yıldız bildirimi ikinci kez tekrarlandı: $stars")
                } catch (e: Exception) {
                    Log.e("EventBus", "Main thread yeniden deneme hatası: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("EventBus", "Zorunlu yıldız bildirimi sırasında hata: ${e.message}", e)
            
            // Hata durumunda yeniden deneme
            try {
                // Kısa bir bekleme ekle
                kotlinx.coroutines.delay(100)
                // Yeniden bildir
                _currentStars.value = stars
                _starUpdatedEvents.emit(StarUpdatedEvent(stars))
                Log.d("EventBus", "ZORUNLU yıldız bildirimi tekrar deneme başarılı: $stars")
            } catch (retryException: Exception) {
                Log.e("EventBus", "ZORUNLU yıldız bildirimi tekrar deneme başarısız: ${retryException.message}", retryException)
            }
        }
    }
    
    // Avatar değiştiğinde çağrılacak metod - yeni API
    suspend fun emitAvatarChangedEvent(avatarId: Int) {
        _avatarChangedEvents.emit(AvatarChangedEvent(avatarId))
    }
    
    // Yıldız güncellendiğinde çağrılacak metod - yeni API
    suspend fun emitStarUpdatedEvent(stars: Int) {
        _starUpdatedEvents.emit(StarUpdatedEvent(stars))
    }
    
    // Avatar değişikliğini ana thread üzerinden bildirme
    fun updateAvatarId(avatarId: Int) {
        try {
            _currentAvatarId.value = avatarId
            CoroutineScope(Dispatchers.Main).launch {
                emitAvatarChangedEvent(avatarId)
            }
        } catch (e: Exception) {
            Log.e("EventBus", "Avatar ID güncellenirken hata: ${e.message}", e)
        }
    }
    
    // Yıldız güncellemesini ana thread üzerinden bildirme
    fun updateStars(stars: Int) {
        try {
            // Önce değeri doğrudan ayarla
            _currentStars.value = stars
            
            // Olayı ana thread üzerinde yayınla
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    emitStarUpdatedEvent(stars)
                    Log.d("EventBus", "Yıldız güncelleme olayı gönderildi (Main thread): $stars")
                    
                    // Güvenilir olması için 200ms sonra tekrar dene
                    kotlinx.coroutines.delay(200)
                    _currentStars.value = stars
                    emitStarUpdatedEvent(stars)
                    Log.d("EventBus", "Yıldız güncelleme olayı tekrarlandı: $stars")
                } catch (e: Exception) {
                    Log.e("EventBus", "Yıldız güncelleme olayı gönderilirken hata (Main thread): ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            Log.e("EventBus", "Yıldız güncelleme hatası: ${e.message}", e)
        }
    }
    
    // Günlük görevleri güncelleme
    fun updateDailyTasks(tasks: List<DailyTask>) {
        _dailyTasks.value = tasks
    }
    
    // Profil bilgilerini güncelleme
    fun updateProfileData(userName: String, updateTime: Long) {
        // Throttling - son 500ms içinde başka bir güncelleme yapıldıysa atla
        val currentTime = System.currentTimeMillis()
        val lastUpdate = lastEmitTime.get()
        
        if (currentTime - lastUpdate > 500) {
            lastEmitTime.set(currentTime)
            _profileUpdateTime.value = updateTime
        }
    }
    
    // EventBus'ı sıfırlama (test için)
    fun reset() {
        try {
            _currentAvatarId.value = 1
            _currentStars.value = 0
            _dailyTasks.value = emptyList()
            _profileUpdateTime.value = 0
            lastEmitTime.set(0)
            lastStarEmitTime.set(0)
            _offlineMode.value = false
            Log.d("EventBus", "EventBus sıfırlandı")
        } catch (e: Exception) {
            Log.e("EventBus", "EventBus sıfırlanırken hata: ${e.message}")
        }
    }
    
    // Offline mod durumunu güncelleme
    fun updateOfflineMode(isOffline: Boolean) {
        _offlineMode.value = isOffline
        Log.d("EventBus", "Çevrimdışı mod değiştirildi: $isOffline")
    }
    
    /**
     * Ekran açılırken doğru yıldız sayısını görüntülemek için
     * ekranlar tarafından çağrılabilen bir yardımcı metot.
     */
    fun fixStarDisplay(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val starManager = StarManager.getInstance(context)
                val currentStars = starManager.getDisplayStars()
                Log.d("EventBus", "fixStarDisplay çağrıldı: $currentStars yıldız")
                
                // Şu anki değer doğru değilse güncelle
                if (_currentStars.value != currentStars) {
                    _currentStars.value = currentStars
                    _starUpdatedEvents.emit(StarUpdatedEvent(currentStars))
                    Log.d("EventBus", "Yıldız görüntüsü düzeltildi: $currentStars")
                }
            } catch (e: Exception) {
                Log.e("EventBus", "Yıldız görüntüsü düzeltilirken hata: ${e.message}", e)
            }
        }
    }
    
    /**
     * Profil sayfasından ana sayfaya geçerken yıldız ve avatar bilgilerini senkronize eder
     */
    fun syncProfileToHomePage(context: Context) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // StarManager'dan mevcut yıldız sayısını al
                val starManager = StarManager.getInstance(context)
                val currentStars = starManager.getDisplayStars()
                
                // AvatarService'den mevcut avatar ID'sini al
                val avatarService = AvatarService.getInstance(context)
                val currentAvatarId = avatarService.getCurrentAvatar().id
                
                Log.d("EventBus", "Profil-Anasayfa senkronizasyonu başlatıldı: Yıldız=$currentStars, Avatar=$currentAvatarId")
                
                // Avatar ve yıldız bilgilerini güncelle ve bildir
                _currentStars.value = currentStars
                _currentAvatarId.value = currentAvatarId
                
                // Bildirimleri gönder
                _starUpdatedEvents.emit(StarUpdatedEvent(currentStars))
                _avatarChangedEvents.emit(AvatarChangedEvent(currentAvatarId))
                
                Log.d("EventBus", "Profil-Anasayfa senkronizasyonu tamamlandı")
                
                // Güvenilir senkronizasyon için 200ms sonra tekrar dene
                kotlinx.coroutines.delay(200)
                _currentStars.value = currentStars
                _starUpdatedEvents.emit(StarUpdatedEvent(currentStars))
                Log.d("EventBus", "Yıldız senkronizasyonu tekrarlandı: $currentStars")
            } catch (e: Exception) {
                Log.e("EventBus", "Profil-Anasayfa senkronizasyonu sırasında hata: ${e.message}", e)
            }
        }
    }
}

// Avatar değişikliği için event sınıfı
data class AvatarChangedEvent(val avatarId: Int)

// Yıldız güncellemesi için event sınıfı
data class StarUpdatedEvent(val stars: Int) 