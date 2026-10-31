package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.provider.Settings
import android.util.Log
import java.util.UUID

/**
 * Cihazın benzersiz kimliğini yöneten servis.
 * Premium kod aktivasyonlarında cihaz bazlı kısıtlamalar için kullanılır.
 */
class DeviceIdManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "DeviceIdManager"
        private const val PREFS_NAME = "device_prefs"
        private const val KEY_DEVICE_ID = "unique_device_id"

        @Volatile
        private var INSTANCE: DeviceIdManager? = null

        fun getInstance(context: Context): DeviceIdManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DeviceIdManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    private val sharedPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Cihazın benzersiz kimliğini döndürür.
     * Eğer daha önce oluşturulmamışsa, yeni bir kimlik oluşturur ve kaydeder.
     */
    fun getDeviceId(): String {
        var deviceId = sharedPrefs.getString(KEY_DEVICE_ID, null)
        if (deviceId == null) {
            // Android ID'yi dene, yoksa UUID oluştur
            deviceId = try {
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            } catch (e: Exception) {
                null
            }
            
            if (deviceId == null || deviceId == "9774d56d682e549c") { // Emulator veya bazı cihazlarda sabit ID
                deviceId = UUID.randomUUID().toString()
                Log.w(TAG, "Android ID kullanılamadı veya geçersiz, yeni UUID oluşturuldu: $deviceId")
            } else {
                Log.d(TAG, "Android ID kullanıldı: $deviceId")
            }
            saveDeviceId(deviceId)
        } else {
            Log.d(TAG, "Mevcut cihaz ID'si: $deviceId")
        }
        return deviceId
    }

    private fun saveDeviceId(deviceId: String) {
        sharedPrefs.edit().putString(KEY_DEVICE_ID, deviceId).apply()
        Log.d(TAG, "Cihaz ID'si kaydedildi: $deviceId")
    }

    /**
     * Cihaz ID'sini sıfırlar (test amaçlı).
     */
    fun resetDeviceId() {
        sharedPrefs.edit().remove(KEY_DEVICE_ID).apply()
        Log.w(TAG, "Cihaz ID'si sıfırlandı.")
    }
}

