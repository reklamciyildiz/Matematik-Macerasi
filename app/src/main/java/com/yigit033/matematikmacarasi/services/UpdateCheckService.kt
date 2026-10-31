package com.yigit033.matematikmacarasi.services

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.install.model.UpdateAvailability
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Uygulama güncelleme kontrolü ve yönetimi için servis
 * Play Store'dan güncelleme durumunu kontrol eder ve kullanıcıya bildirir
 * Android 14 (SDK 34) uyumlu yeni app-update kütüphanesi kullanır
 */
class UpdateCheckService private constructor(private val context: Context) {
    
    companion object {
        private const val TAG = "UpdateCheckService"
        private const val PREFS_NAME = "update_check_prefs"
        private const val LAST_CHECK_TIME = "last_check_time"
        private const val LAST_SHOWN_VERSION = "last_shown_version"
        private const val CHECK_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 saat
        
        @Volatile
        private var INSTANCE: UpdateCheckService? = null
        
        fun getInstance(context: Context): UpdateCheckService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: UpdateCheckService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    // UI State
    var showUpdateDialog by mutableStateOf(false)
        private set
    
    var updateInfo: AppUpdateInfo? = null
        private set
    
    /**
     * Güncelleme kontrolü yapar - ana ekran açılışında çağrılır
     */
    fun checkForUpdates(activity: Activity) {
        try {
            // Son kontrol zamanını kontrol et
            val lastCheckTime = prefs.getLong(LAST_CHECK_TIME, 0)
            val currentTime = System.currentTimeMillis()
            
            if (currentTime - lastCheckTime < CHECK_INTERVAL_MS) {
                Log.d(TAG, "Güncelleme kontrolü için henüz erken (24 saat geçmedi)")
                return
            }
            
            // Play Store'dan güncelleme bilgilerini al
            appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                try {
                    Log.d(TAG, "Güncelleme durumu: ${appUpdateInfo.updateAvailability()}")
                    Log.d(TAG, "Mevcut versiyon kodu: ${getCurrentVersionCode()}")
                    Log.d(TAG, "Mevcut güncelleme kodu: ${appUpdateInfo.availableVersionCode()}")
                    
                    if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                        // Güncelleme mevcut - kullanıcıya göster
                        val currentVersionCode = getCurrentVersionCode()
                        val lastShownVersion = prefs.getInt(LAST_SHOWN_VERSION, 0)
                        
                        // Aynı versiyonu tekrar gösterme
                        if (appUpdateInfo.availableVersionCode() != lastShownVersion) {
                            updateInfo = appUpdateInfo
                            showUpdateDialog = true
                            
                            Log.d(TAG, "Yeni güncelleme mevcut! Dialog gösteriliyor...")
                        } else {
                            Log.d(TAG, "Bu güncelleme daha önce gösterildi")
                        }
                    } else {
                        Log.d(TAG, "Güncelleme mevcut değil")
                    }
                    
                    // Son kontrol zamanını güncelle
                    prefs.edit().putLong(LAST_CHECK_TIME, currentTime).apply()
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Güncelleme bilgisi işlenirken hata: ${e.message}", e)
                }
            }.addOnFailureListener { exception ->
                Log.e(TAG, "Güncelleme kontrolü başarısız: ${exception.message}", exception)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "checkForUpdates sırasında hata: ${e.message}", e)
        }
    }
    
    /**
     * Kullanıcı güncellemeyi kabul ettiğinde Play Store'a yönlendirir
     */
    fun acceptUpdate(activity: Activity) {
        try {
            updateInfo?.let { info ->
                // Gösterilen versiyonu kaydet (tekrar gösterme)
                prefs.edit().putInt(LAST_SHOWN_VERSION, info.availableVersionCode()).apply()
                
                // Play Store'a yönlendir
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                    setPackage("com.android.vending") // Play Store uygulamasını zorla
                }
                
                if (intent.resolveActivity(activity.packageManager) != null) {
                    activity.startActivity(intent)
                    Log.d(TAG, "Play Store'a yönlendirildi")
                } else {
                    // Play Store yoksa browser'da aç
                    val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                    }
                    activity.startActivity(browserIntent)
                    Log.d(TAG, "Browser'da Play Store açıldı")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Play Store'a yönlendirme hatası: ${e.message}", e)
        } finally {
            dismissUpdateDialog()
        }
    }
    
    /**
     * Kullanıcı güncellemeyi reddederse dialog'u kapat
     */
    fun dismissUpdateDialog() {
        showUpdateDialog = false
        updateInfo = null
        Log.d(TAG, "Güncelleme dialog'u kapatıldı")
    }
    
    /**
     * Mevcut uygulama versiyon kodunu al
     */
    private fun getCurrentVersionCode(): Int {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            @Suppress("DEPRECATION")
            packageInfo.versionCode
        } catch (e: Exception) {
            Log.e(TAG, "Versiyon kodu alınamadı: ${e.message}", e)
            0
        }
    }
    
    /**
     * Test için güncelleme dialog'unu zorla göster
     */
    fun forceShowUpdateDialog() {
        showUpdateDialog = true
        Log.d(TAG, "Güncelleme dialog'u test için zorla gösterildi")
    }
} 