package com.yigit033.matematikmacarasi.services

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

/**
 * Uygulama versiyonu kontrol servisi.
 * Firebase'den en son versiyon bilgisini çekerek güncelleme gerekip gerekmediğini kontrol eder.
 */
class VersionCheckService private constructor(private val context: Context) {

    private val TAG = "VersionCheckService"
    private val database by lazy { Firebase.database.reference }

    companion object {
        @Volatile
        private var INSTANCE: VersionCheckService? = null

        fun getInstance(context: Context): VersionCheckService {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VersionCheckService(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Uygulama versiyon bilgisi
     */
    data class AppVersionInfo(
        val latest_version: String = "1.0",
        val min_required_version: String = "1.0",
        val update_url: String = "",
        val force_update: Boolean = false,
        val update_message: String = "Yeni bir güncelleme mevcut! Daha iyi deneyim için uygulamayı güncelleyin.",
        val force_update_message: String = "Bu sürüm artık desteklenmiyor. Devam etmek için uygulamayı güncellemeniz gerekiyor."
    )

    /**
     * Güncelleme durumu
     */
    sealed class UpdateStatus {
        object NoUpdateNeeded : UpdateStatus()
        data class UpdateAvailable(val versionInfo: AppVersionInfo) : UpdateStatus()
        data class ForceUpdateRequired(val versionInfo: AppVersionInfo) : UpdateStatus()
        data class Error(val message: String) : UpdateStatus()
    }

    /**
     * Mevcut uygulama versiyonunu al
     */
    fun getCurrentVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            packageInfo.versionName ?: "1.0"
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Versiyon bilgisi alınamadı: ${e.message}", e)
            "1.0"
        }
    }

    /**
     * Firebase'den en son versiyon bilgisini asenkron olarak kontrol et
     */
    suspend fun checkForUpdate(): UpdateStatus {
        return try {
            val snapshot = database.child("app_info").get().await()
            val versionInfo = snapshot.getValue(AppVersionInfo::class.java)

            if (versionInfo == null) {
                Log.w(TAG, "Firebase'de app_info bulunamadı")
                return UpdateStatus.NoUpdateNeeded
            }

            val currentVersion = getCurrentVersion()
            Log.d(TAG, "Mevcut versiyon: $currentVersion, Son versiyon: ${versionInfo.latest_version}")

            // Versiyon karşılaştırması
            when {
                isVersionLower(currentVersion, versionInfo.min_required_version) -> {
                    // Zorla güncelleme gerekli
                    Log.w(TAG, "Zorla güncelleme gerekli!")
                    UpdateStatus.ForceUpdateRequired(versionInfo)
                }
                isVersionLower(currentVersion, versionInfo.latest_version) -> {
                    // Güncelleme mevcut ama zorunlu değil
                    Log.d(TAG, "Güncelleme mevcut")
                    UpdateStatus.UpdateAvailable(versionInfo)
                }
                else -> {
                    // Güncel
                    Log.d(TAG, "Uygulama güncel")
                    UpdateStatus.NoUpdateNeeded
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Versiyon kontrolü sırasında hata: ${e.message}", e)
            UpdateStatus.Error(e.message ?: "Bilinmeyen hata")
        }
    }

    /**
     * İki versiyonu karşılaştır (semantic versioning: X.Y.Z)
     * @return currentVersion < targetVersion ise true
     */
    private fun isVersionLower(currentVersion: String, targetVersion: String): Boolean {
        return try {
            val current = parseVersion(currentVersion)
            val target = parseVersion(targetVersion)

            when {
                current[0] < target[0] -> true
                current[0] > target[0] -> false
                current[1] < target[1] -> true
                current[1] > target[1] -> false
                current[2] < target[2] -> true
                else -> false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Versiyon karşılaştırma hatası: ${e.message}", e)
            false
        }
    }

    /**
     * Versiyon string'ini parse et (X.Y.Z formatı)
     */
    private fun parseVersion(version: String): List<Int> {
        return version.split(".")
            .take(3)
            .map { it.toIntOrNull() ?: 0 }
            .plus(listOf(0, 0, 0))
            .take(3)
    }

    /**
     * Play Store'a yönlendir
     */
    fun openPlayStore(updateUrl: String? = null) {
        try {
            val url = updateUrl?.takeIf { it.isNotBlank() }
                ?: "https://play.google.com/store/apps/details?id=${context.packageName}"
            
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                data = android.net.Uri.parse(url)
                addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.d(TAG, "Play Store açıldı: $url")
        } catch (e: Exception) {
            Log.e(TAG, "Play Store açılamadı: ${e.message}", e)
        }
    }
}

