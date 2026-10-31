package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Premium kullanıcılar için günlük bonus yıldız sistemi
 */
class DailyPremiumBonusService private constructor(private val context: Context) {
    
    companion object {
        private const val PREFS_NAME = "daily_premium_bonus"
        private const val TAG = "DailyPremiumBonusService"
        private const val DAILY_BONUS_STARS = 5 // Günlük bonus yıldız sayısı
        private const val LAST_BONUS_DATE_KEY = "last_bonus_date"
        private const val TOTAL_BONUS_RECEIVED_KEY = "total_bonus_received"
        
        // Tarih formatı
        private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        @Volatile
        private var INSTANCE: DailyPremiumBonusService? = null
        
        fun getInstance(context: Context): DailyPremiumBonusService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DailyPremiumBonusService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val starManager by lazy { StarManager.getInstance(context) }
    private val purchaseManager by lazy { 
        try {
            PurchaseManager.getInstance(context)
        } catch (e: Exception) {
            Log.e(TAG, "PurchaseManager başlatılamadı: ${e.message}", e)
            null
        }
    }
    
    /**
     * Bugün premium bonus alınıp alınmadığını kontrol eder
     */
    fun hasTodaysBonusBeenClaimed(): Boolean {
        return try {
            val today = dateFormat.format(Date())
            val lastBonusDate = prefs.getString(LAST_BONUS_DATE_KEY, "")
            val result = today == lastBonusDate
            Log.d(TAG, "Bugünkü bonus kontrol: today=$today, lastBonus=$lastBonusDate, claimed=$result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "Bonus kontrol hatası: ${e.message}", e)
            false
        }
    }
    
    /**
     * Premium kullanıcı için günlük bonusu verir
     * @return Bonus verildi mi?
     */
    fun claimDailyBonus(): Boolean {
        return try {
            // Premium kontrolü
            val isPremium = purchaseManager?.isPremiumSync() ?: false
            if (!isPremium) {
                Log.d(TAG, "Premium olmayan kullanıcı, bonus verilmiyor")
                return false
            }
            
            // Bugün zaten bonus alınmış mı?
            if (hasTodaysBonusBeenClaimed()) {
                Log.d(TAG, "Bugün zaten bonus alınmış")
                return false
            }
            
            // Bonus yıldızları ekle
            val currentStars = starManager.getTotalStars()
            val newStars = currentStars + DAILY_BONUS_STARS
            
            // StarManager'a yıldızları ekle
            starManager.setDisplayStars(newStars)
            
            // Bonus tarihini kaydet
            val today = dateFormat.format(Date())
            prefs.edit()
                .putString(LAST_BONUS_DATE_KEY, today)
                .putInt(TOTAL_BONUS_RECEIVED_KEY, getTotalBonusReceived() + DAILY_BONUS_STARS)
                .apply()
            
            Log.d(TAG, "Premium günlük bonus verildi: $DAILY_BONUS_STARS yıldız. Yeni toplam: $newStars")
            return true
            
        } catch (e: Exception) {
            Log.e(TAG, "Günlük bonus verme hatası: ${e.message}", e)
            false
        }
    }
    
    /**
     * Toplam alınan bonus yıldız sayısını döndürür
     */
    fun getTotalBonusReceived(): Int {
        return prefs.getInt(TOTAL_BONUS_RECEIVED_KEY, 0)
    }
    
    /**
     * Günlük bonus yıldız sayısını döndürür
     */
    fun getDailyBonusAmount(): Int {
        return DAILY_BONUS_STARS
    }
    
    /**
     * Premium kullanıcı için otomatik bonus kontrolü yapar
     * Uygulama açılışında çağrılabilir
     */
    fun checkAndClaimAutomaticBonus(): Boolean {
        return try {
            val isPremium = purchaseManager?.isPremiumSync() ?: false
            if (isPremium && !hasTodaysBonusBeenClaimed()) {
                Log.d(TAG, "Otomatik premium bonus kontrol ediliyor...")
                claimDailyBonus()
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Otomatik bonus kontrol hatası: ${e.message}", e)
            false
        }
    }
} 