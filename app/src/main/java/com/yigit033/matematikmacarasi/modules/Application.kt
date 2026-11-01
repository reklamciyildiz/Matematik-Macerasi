package com.yigit033.matematikmacarasi.modules

import android.app.Application
import android.util.Log
import com.yigit033.matematikmacarasi.services.NotificationService
import com.yigit033.matematikmacarasi.settings.IconLauncherManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Uygulama sınıfı, uygulama başlatıldığında çalışan kodları içerir.
 */
class MatematikMacerasiApp : Application() {
    
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    // Supabase servisi - devre dışı bırakıldı
    // private lateinit var supabaseService: SupabaseService
    
    override fun onCreate() {
        super.onCreate()

        // Repair MainActivity for devices migrated from the old icon switcher.
        // Never let an icon choice disable the real game's main Activity.
        runCatching { IconLauncherManager.reconcile(applicationContext) }
            .onFailure { Log.e("MatematikMacerasiApp","Launcher recovery failed",it) }

        // Servisleri başlat
        initializeServices()
        
        // AdMob'u başlat
        // Families release gate: no ad SDK initialization until certified
        // child-directed ad stack and Play Console declarations are reviewed.
        Log.i("MatematikMacerasiApp","AdMob initialization disabled for pre-release Families QA")
        
        // Bildirim servisini başlat
        initializeNotifications()
    }
    
    /**
     * Bildirim servisini başlatır ve kanalları oluşturur
     */
    private fun initializeNotifications() {
        try {
            Log.d("MatematikMacerasiApp", "Bildirim servisi başlatılıyor...")
            val notificationService = NotificationService.getInstance(applicationContext)
            
            // Bildirim kanallarını oluştur
            notificationService.createNotificationChannels()
            
            // Bildirim ayarlarını yükle (günlük hatırlatıcıları ayarla)
            notificationService.loadNotificationSettings()
            
            Log.d("MatematikMacerasiApp", "Bildirim servisi başarıyla başlatıldı")
        } catch (e: Exception) {
            Log.e("MatematikMacerasiApp", "Bildirim servisi başlatılırken hata oluştu", e)
        }
    }
    
    /**
     * Uygulama servislerini başlatır.
     */
    private fun initializeServices() {
        try {
            // Supabase servisi başlatma kodu devre dışı bırakıldı
            Log.d("MatematikMacerasiApp", "Supabase servisi devre dışı bırakıldı.")
            
            /* 
            // Supabase servisini başlat
            Log.d("MatematikMacerasiApp", "Supabase servisi başlatılıyor...")
            supabaseService = SupabaseService.getInstance(applicationContext)
            
            // Supabase bağlantısını test et
            applicationScope.launch {
                try {
                    val connected = supabaseService.testConnection()
                    Log.d("MatematikMacerasiApp", "Supabase bağlantı durumu: $connected")
                } catch (e: Exception) {
                    Log.e("MatematikMacerasiApp", "Supabase bağlantı testi sırasında hata", e)
                }
            }
            */
        } catch (e: Exception) {
            Log.e("MatematikMacerasiApp", "Servis başlatma hatası", e)
        }
    }
} 