package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.MutableData
import com.google.firebase.database.ServerValue
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Firebase Realtime Database yönetimi için servis.
 * Premium kod sistemi için veritabanı işlemlerini gerçekleştirir.
 */
class FirebaseManager private constructor(private val context: Context) {

    private val TAG = "FirebaseManager"
    private val database: DatabaseReference by lazy {
        Firebase.database.reference
    }

    companion object {
        @Volatile
        private var INSTANCE: FirebaseManager? = null

        fun getInstance(context: Context): FirebaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Belirtilen kodun detaylarını Firebase'den asenkron olarak getirir.
     */
    suspend fun getPremiumCodeDetails(code: String): PremiumCode? {
        return try {
            val snapshot = database.child("premium_codes").child(code).get().await()
            snapshot.getValue(PremiumCode::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Premium kod detayları getirilirken hata: ${e.message}", e)
            null
        }
    }

    /**
     * Bir kodun kullanım sayısını artırır ve aktivasyonu kaydeder.
     * Transaction kullanarak eşzamanlı erişim sorunlarını çözer.
     */
    suspend fun incrementCodeUsageAndRecordActivation(code: String, deviceId: String): Boolean {
        return try {
            val codeRef = database.child("premium_codes").child(code)
            val activationRef = database.child("code_activations").child(deviceId)

            // Cihazın zaten bir kod aktive edip etmediğini kontrol et
            val existingActivation = activationRef.get().await().getValue(CodeActivation::class.java)
            if (existingActivation != null) {
                Log.d(TAG, "Cihaz $deviceId zaten ${existingActivation.code} kodunu aktive etmiş.")
                return false // Zaten aktive edilmiş
            }

            // Önce kodu oku ve kontrol et
            val snapshot = codeRef.get().await()
            val codeData = snapshot.getValue(PremiumCode::class.java)
            
            if (codeData == null || !codeData.active) {
                Log.e(TAG, "Kod bulunamadı veya aktif değil: $code")
                return false
            }

            if (codeData.max_uses != null && codeData.used_count >= codeData.max_uses) {
                Log.e(TAG, "Kod kullanım limitine ulaşmış: $code")
                return false
            }

            // used_count'u artır
            val newCount = codeData.used_count + 1
            try {
                codeRef.child("used_count").setValue(newCount).await()
                Log.d(TAG, "✅ Kod $code için kullanım sayısı artırıldı: $newCount")
            } catch (e: Exception) {
                Log.e(TAG, "❌ USED_COUNT YAZMA HATASI: ${e.message}", e)
                throw Exception("Firebase'e yazma izni yok (used_count). Rules kontrol et!")
            }

            // Aktivasyonu kaydet
            val activation = CodeActivation(code, System.currentTimeMillis())
            try {
                activationRef.setValue(activation).await()
                Log.d(TAG, "✅ Cihaz $deviceId için aktivasyon kaydedildi.")
            } catch (e: Exception) {
                Log.e(TAG, "❌ AKTİVASYON KAYDETME HATASI: ${e.message}", e)
                throw Exception("Firebase'e yazma izni yok (code_activations). Rules kontrol et!")
            }

            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ KOD AKTİVASYON HATASI: ${e.message}", e)
            false
        }
    }

    /**
     * Belirtilen cihaz ID'si için aktif bir kod aktivasyonu olup olmadığını kontrol eder.
     */
    suspend fun getDeviceActivation(deviceId: String): CodeActivation? {
        return try {
            val snapshot = database.child("code_activations").child(deviceId).get().await()
            snapshot.getValue(CodeActivation::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Cihaz aktivasyonu getirilirken hata: ${e.message}", e)
            null
        }
    }

    /**
     * Firebase bağlantı durumunu izler.
     */
    fun observeConnectionState(): Flow<Boolean> = callbackFlow {
        val connectedRef = database.child(".info/connected")
        val listener = connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                trySend(connected)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Bağlantı durumu dinlenirken hata: ${error.message}")
                trySend(false)
            }
        })
        awaitClose { connectedRef.removeEventListener(listener) }
    }
}

// Veritabanı modelleri
data class PremiumCode(
    val institution: String = "",
    val max_uses: Int? = null, // null ise sınırsız
    var used_count: Int = 0,
    val expiry_date: String = "", // YYYY-MM-DD formatında
    val active: Boolean = true
)

data class CodeActivation(
    val code: String = "",
    val activated_at: Long = 0L
)

