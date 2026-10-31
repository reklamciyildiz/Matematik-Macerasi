package com.yigit033.matematikmacarasi.services.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.yigit033.matematikmacarasi.models.AuthSession

/**
 * Kimlik doğrulama bilgilerini yerel depolamak için kullanılan sınıf
 */
class AuthLocalStorage(context: Context) {
    
    private val TAG = "AuthLocalStorage"
    
    private val PREFS_NAME = "auth_prefs"
    private val KEY_ACCESS_TOKEN = "access_token"
    private val KEY_REFRESH_TOKEN = "refresh_token"
    private val KEY_USER_ID = "user_id"
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Oturum bilgilerini kaydeder
     */
    fun saveSession(accessToken: String, refreshToken: String, userId: String) {
        Log.d(TAG, "Oturum bilgileri kaydediliyor: userId=$userId")
        
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putString(KEY_USER_ID, userId)
            .apply()
    }
    
    /**
     * Oturum bilgilerini yükler
     */
    fun loadSession(): AuthSession? {
        val accessToken = prefs.getString(KEY_ACCESS_TOKEN, null)
        val refreshToken = prefs.getString(KEY_REFRESH_TOKEN, null)
        val userId = prefs.getString(KEY_USER_ID, null)
        
        return if (accessToken != null && userId != null) {
            AuthSession(
                accessToken = accessToken,
                refreshToken = refreshToken ?: "",
                userID = userId
            )
        } else {
            Log.d(TAG, "Kaydedilmiş oturum bilgisi bulunamadı")
            null
        }
    }
    
    /**
     * Oturum bilgilerini temizler
     */
    fun clearSession() {
        Log.d(TAG, "Oturum bilgileri temizleniyor")
        
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER_ID)
            .apply()
    }
} 