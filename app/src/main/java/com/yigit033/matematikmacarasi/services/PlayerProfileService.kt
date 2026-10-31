package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.content.SharedPreferences

/**
 * PlayerProfileService, oyuncu profilini yöneten servistir.
 * Oyuncunun yaşı, sınıfı gibi temel bilgilerini sağlar.
 */
class PlayerProfileService(context: Context) {
    
    companion object {
        private const val PREFS_NAME = "user_prefs"
        private const val DEFAULT_AGE = 8
        private const val DEFAULT_CLASS = "1. Sınıf"
        
        // Singleton instance
        @Volatile
        private var instance: PlayerProfileService? = null
        
        fun getInstance(context: Context): PlayerProfileService {
            return instance ?: synchronized(this) {
                instance ?: PlayerProfileService(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    /**
     * Oyuncunun yaşını döndürür.
     * Sınıf bilgisine göre otomatik hesaplanır ya da varsayılan değer kullanılır.
     */
    fun getPlayerAge(): Int {
        val userClass = getUserClass()
        return when {
            userClass.contains("1.") -> 7  // 1. sınıf
            userClass.contains("2.") -> 8  // 2. sınıf
            userClass.contains("3.") -> 9  // 3. sınıf
            userClass.contains("4.") -> 10 // 4. sınıf
            userClass.contains("5.") -> 11 // 5. sınıf
            userClass.contains("6.") -> 12 // 6. sınıf
            userClass.contains("7.") -> 13 // 7. sınıf
            userClass.contains("8.") -> 14 // 8. sınıf
            else -> DEFAULT_AGE // Varsayılan yaş
        }
    }
    
    /**
     * Oyuncunun sınıfını döndürür.
     */
    fun getUserClass(): String {
        return sharedPreferences.getString("user_class", DEFAULT_CLASS) ?: DEFAULT_CLASS
    }
    
    /**
     * Oyuncunun adını döndürür.
     */
    fun getUserName(): String {
        return sharedPreferences.getString("user_name", "") ?: ""
    }
    
    /**
     * Oyuncunun adını günceller.
     */
    fun updateUserName(name: String) {
        sharedPreferences.edit().putString("user_name", name).apply()
    }
    
    /**
     * Oyuncunun sınıfını günceller.
     */
    fun updateUserClass(userClass: String) {
        sharedPreferences.edit().putString("user_class", userClass).apply()
    }
} 