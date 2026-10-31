package com.yigit033.matematikmacarasi.settings

import android.content.Context
import android.content.SharedPreferences

/**
 * Uygulama ikonlarını yönetmek için kullanılan sınıf
 */
class IconPreferenceManager(context: Context) {
    
    companion object {
        private const val PREF_NAME = "icon_preferences"
        private const val KEY_SELECTED_ICON = "selected_icon"
        
        // İkon tipleri
        const val ICON_DEFAULT = "default"
        const val ICON_ROBOT = "robot"
        const val ICON_SPACE = "space"
        
        // Singleton instance
        @Volatile
        private var instance: IconPreferenceManager? = null
        
        fun getInstance(context: Context): IconPreferenceManager {
            return instance ?: synchronized(this) {
                instance ?: IconPreferenceManager(context.applicationContext).also { instance = it }
            }
        }
    }
    
    private val preferences: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    /**
     * Seçili ikonu kaydet
     */
    fun saveSelectedIcon(iconType: String) {
        preferences.edit().putString(KEY_SELECTED_ICON, iconType).apply()
    }
    
    /**
     * Seçili ikonu getir
     */
    fun getSelectedIcon(): String {
        return preferences.getString(KEY_SELECTED_ICON, ICON_DEFAULT) ?: ICON_DEFAULT
    }
} 