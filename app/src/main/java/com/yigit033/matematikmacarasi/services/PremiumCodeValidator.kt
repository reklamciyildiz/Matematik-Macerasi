package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.util.Log
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import com.yigit033.matematikmacarasi.utils.NetworkUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Premium kod doğrulama ve aktivasyon işlemlerini yöneten servis.
 * Firebase ve cihaz ID yöneticisi ile entegre çalışır.
 */
class PremiumCodeValidator private constructor(private val context: Context) {

    private val TAG = "PremiumCodeValidator"
    private val firebaseManager by lazy { FirebaseManager.getInstance(context) }
    private val deviceIdManager by lazy { DeviceIdManager.getInstance(context) }
    private val sharedPrefs = context.getSharedPreferences("premium_code_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_CODE_PREMIUM = "is_code_premium"
        private const val KEY_ACTIVATED_CODE = "activated_code"

        @Volatile
        private var INSTANCE: PremiumCodeValidator? = null

        fun getInstance(context: Context): PremiumCodeValidator {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PremiumCodeValidator(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    sealed class ValidationResult {
        object Success : ValidationResult()
        object InvalidCode : ValidationResult()
        object CodeExpired : ValidationResult()
        object CodeLimitReached : ValidationResult()
        object AlreadyActivated : ValidationResult()
        object NetworkError : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    /**
     * Kullanıcının girdiği kodu doğrular ve premium durumu aktive eder.
     */
    suspend fun validateAndActivateCode(code: String): ValidationResult {
        if (!NetworkUtils.isInternetAvailable(context)) {
            Log.e(TAG, "İnternet bağlantısı yok.")
            return ValidationResult.NetworkError
        }

        val deviceId = deviceIdManager.getDeviceId()

        // Cihazın zaten bir kod aktive edip etmediğini kontrol et
        val existingActivation = firebaseManager.getDeviceActivation(deviceId)
        if (existingActivation != null) {
            Log.d(TAG, "Cihaz $deviceId zaten ${existingActivation.code} kodunu aktive etmiş.")
            // Eğer mevcut aktivasyon girilen kodla aynıysa, başarı say
            if (existingActivation.code == code) {
                setPremiumCodeStatus(true, code)
                return ValidationResult.AlreadyActivated
            } else {
                // Farklı bir kodla zaten aktive edilmişse, yeni kodu kabul etme
                return ValidationResult.AlreadyActivated
            }
        }

        val premiumCode = firebaseManager.getPremiumCodeDetails(code)

        if (premiumCode == null || !premiumCode.active) {
            Log.w(TAG, "Geçersiz veya aktif olmayan kod: $code")
            return ValidationResult.InvalidCode
        }

        // Süre kontrolü
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        try {
            val expiryDate = dateFormat.parse(premiumCode.expiry_date)
            if (expiryDate != null && expiryDate.before(Date())) {
                Log.w(TAG, "Kodun süresi dolmuş: $code")
                return ValidationResult.CodeExpired
            }
        } catch (e: Exception) {
            Log.e(TAG, "Tarih formatı hatası: ${premiumCode.expiry_date}", e)
            return ValidationResult.Error("Kodun tarih formatı geçersiz.")
        }

        // Kullanım limiti kontrolü
        if (premiumCode.max_uses != null && premiumCode.used_count >= premiumCode.max_uses) {
            Log.w(TAG, "Kod kullanım limitine ulaşmış: $code")
            return ValidationResult.CodeLimitReached
        }

        // Kodu aktive et ve kullanım sayısını artır
        val success = firebaseManager.incrementCodeUsageAndRecordActivation(code, deviceId)

        return if (success) {
            setPremiumCodeStatus(true, code)
            Log.d(TAG, "✅ Premium kod başarıyla aktive edildi: $code")
            ValidationResult.Success
        } else {
            Log.e(TAG, "❌ Kod aktivasyonu başarısız: $code - Firebase yazma izni kontrolü gerekli!")
            ValidationResult.Error("Firebase yazma hatası. Lütfen internet bağlantınızı kontrol edin.")
        }
    }

    /**
     * Cihazın premium kod ile aktive edilip edilmediğini kontrol eder.
     * SADECE local cache'den okur, Firebase sync manuel olarak yapılmalı!
     */
    fun isCodePremium(): Boolean {
        // Sadece local cache kontrol et, sync yapma!
        val localPremium = sharedPrefs.getBoolean(KEY_IS_CODE_PREMIUM, false)
        if (localPremium) {
            Log.d(TAG, "Premium kod durumu (local): true")
        }
        return localPremium
    }
    
    /**
     * Firebase'den premium durumunu senkronize eder (uygulama silme-yükleme durumu için).
     * Bu fonksiyon arka planda çalışır, kullanıcıyı bekletmez.
     */
    private fun syncPremiumStatusFromFirebase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val deviceId = deviceIdManager.getDeviceId()
                val activation = firebaseManager.getDeviceActivation(deviceId)
                
                if (activation != null) {
                    // Firebase'de aktivasyon var, local'e kaydet
                    setPremiumCodeStatus(true, activation.code)
                    Log.d(TAG, "✅ Premium durumu Firebase'den senkronize edildi: ${activation.code}")
                    
                    // PurchaseManager'ı da güncelle
                    PurchaseManager.getInstance(context).checkPremiumStatusAsync()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Firebase senkronizasyon hatası: ${e.message}", e)
            }
        }
    }
    
    /**
     * Manuel olarak Firebase'den premium durumunu kontrol eder ve günceller.
     * Uygulama başlangıcında veya ayarlar ekranında kullanılabilir.
     */
    suspend fun checkAndSyncPremiumStatus(): Boolean {
        return try {
            val deviceId = deviceIdManager.getDeviceId()
            val activation = firebaseManager.getDeviceActivation(deviceId)
            
            if (activation != null) {
                setPremiumCodeStatus(true, activation.code)
                Log.d(TAG, "✅ Premium durumu senkronize edildi: ${activation.code}")
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Senkronizasyon hatası: ${e.message}", e)
            false
        }
    }

    /**
     * Premium kod durumunu SharedPreferences'a kaydeder.
     */
    private fun setPremiumCodeStatus(isPremium: Boolean, code: String? = null) {
        sharedPrefs.edit().apply {
            putBoolean(KEY_IS_CODE_PREMIUM, isPremium)
            if (isPremium && code != null) {
                putString(KEY_ACTIVATED_CODE, code)
            } else {
                remove(KEY_ACTIVATED_CODE)
            }
            apply()
        }
        // PurchaseManager'daki _premiumState'i HEMEN güncelle!
        try {
            val purchaseManager = PurchaseManager.getInstance(context)
            purchaseManager.refreshPremiumState() // ← Yeni method!
            Log.d(TAG, "✅ PurchaseManager premium state'i güncellendi")
        } catch (e: Exception) {
            Log.e(TAG, "PurchaseManager güncellenemedi: ${e.message}", e)
        }
    }

    /**
     * Premium kod durumunu sıfırlar (test amaçlı).
     */
    fun resetCodePremiumStatus() {
        setPremiumCodeStatus(false)
        deviceIdManager.resetDeviceId()
        Log.w(TAG, "Premium kod durumu sıfırlandı (geliştirici tarafından).")
    }
}

