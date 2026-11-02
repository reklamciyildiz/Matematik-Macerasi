package com.yigit033.matematikmacarasi

import android.app.Application
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.yigit033.matematikmacarasi.services.AudioService
import android.util.Log
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import com.airbnb.lottie.LottieTask
import com.yigit033.matematikmacarasi.services.AvatarService
import com.yigit033.matematikmacarasi.services.EventBus
import com.yigit033.matematikmacarasi.services.NotificationService
import java.util.concurrent.Executors

/**
 * Matematik Macerası uygulaması için Application sınıfı.
 * Global yapılandırmaları ve optimizasyonları içerir.
 */
class MathAdventureApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        setupAudioLifecycle()

        // Global hata yakalayıcı
        setupGlobalExceptionHandler()
        
        // Lottie optimizasyonları
        setupLottieOptimizations()
        
        // EventBus'ı başlat
        initializeEventBus()
        
        // Avatar animasyonlarını önceden yükle
        preloadAvatarAnimations()
        
        // Bildirim sistemini başlat
        initializeNotificationSystem()
        
        Log.d(TAG, "Uygulama başlatıldı ve optimizasyonlar yapılandırıldı")
    }
    
    /**
     * Music belongs to the visible app, not to an Activity.
     * The delayed background pause prevents brief interruptions during navigation.
     */
    private fun setupAudioLifecycle() {
        val handler = Handler(Looper.getMainLooper())
        var visibleActivities = 0
        val pause = Runnable {
            if (visibleActivities == 0) {
                AudioService.getInstance(applicationContext).onAppBackground()
            }
        }
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                handler.removeCallbacks(pause)
                visibleActivities++
                val gameplay = activity.javaClass.name.contains(
                    ".games."
                )
                AudioService.getInstance(applicationContext).onAppForeground(gameplay)
            }
            override fun onActivityStopped(activity: Activity) {
                visibleActivities = (visibleActivities - 1).coerceAtLeast(0)
                if (visibleActivities == 0) handler.postDelayed(pause, 550L)
            }
            override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }

    /**
     * Global hata yakalama mekanizması
     */
    private fun setupGlobalExceptionHandler() {
        val defaultExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Beklenmeyen hata: ${throwable.message}", throwable)
            
            // Burada hata raporlama servisi eklenebilir
            
            // Varsayılan işleyiciye devret
            defaultExceptionHandler?.uncaughtException(thread, throwable)
        }
    }
    
    /**
     * Lottie animasyon kütüphanesi için performans optimizasyonları
     */
    private fun setupLottieOptimizations() {    
        try {
            // Animasyonlar için tek bir thread havuzu kullan
            LottieTask.EXECUTOR = Executors.newFixedThreadPool(2)
            
            // Lottie diğer optimizasyonları
            LottieCompositionFactory.setMaxCacheSize(50)
            
            Log.d(TAG, "Lottie optimizasyonları yapılandırıldı")
        } catch (e: Exception) {
            Log.e(TAG, "Lottie optimizasyonları yapılandırılırken hata: ${e.message}", e)
        }
    }
    
    /**
     * Avatar animasyonlarını önceden yükle
     */
    private fun preloadAvatarAnimations() {
        try {
            // Avatar servisini başlat
            val avatarService = AvatarService.getInstance(applicationContext)
            val avatars = avatarService.avatars
            
            // Tüm avatar animasyonlarını önceden yükle
            for (avatar in avatars) {
                if (avatar.animationType == com.yigit033.matematikmacarasi.models.AvatarAnimationType.LOTTIE && 
                    avatar.animationResId != 0) {
                    
                    // Lottie JSON dosyasını asenkron olarak yükle
                    LottieCompositionFactory.fromRawRes(applicationContext, avatar.animationResId)
                    
                    Log.d(TAG, "Avatar animasyonu ön yüklendi: ${avatar.name}, ID: ${avatar.animationResId}")
                }
            }
            
            Log.d(TAG, "Animasyon ön yüklemesi tamamlandı")
        } catch (e: Exception) {
            Log.e(TAG, "Animasyon ön yüklemesi sırasında hata: ${e.message}", e)
        }
    }
    
    /**
     * EventBus'ı başlat ve varsayılan değerleri ayarla
     */
    private fun initializeEventBus() {
        try {
            // EventBus'ı sıfırla
            EventBus.reset()
            Log.d(TAG, "EventBus başlatıldı")
        } catch (e: Exception) {
            Log.e(TAG, "EventBus başlatılırken hata: ${e.message}", e)
        }
    }
    
    /**
     * Bildirim sistemini başlat
     */
    private fun initializeNotificationSystem() {
        try {
            // Bildirim servisini al
            val notificationService = NotificationService.getInstance(applicationContext)
            
            // Bildirim kanallarını oluştur
            notificationService.createNotificationChannels()
            
            // Bildirim ayarlarını yükle
            notificationService.loadNotificationSettings()
            
            Log.d(TAG, "Bildirim sistemi başlatıldı")
        } catch (e: Exception) {
            Log.e(TAG, "Bildirim sistemi başlatılırken hata: ${e.message}", e)
        }
    }
    
    override fun onLowMemory() {
        super.onLowMemory()
        
        // Düşük bellek durumunda önbellekleri temizle
        try {
            // Lottie önbelleğini temizleme işlemi - API'de doğrudan metod olmadığı için GC'ye bırakıyoruz
            System.gc()
            Log.d(TAG, "Düşük bellek durumunda GC çağrıldı")
        } catch (e: Exception) {
            Log.e(TAG, "Bellek temizlenirken hata: ${e.message}", e)
        }
    }
    
    companion object {
        private const val TAG = "MathAdventureApp"
    }
} 