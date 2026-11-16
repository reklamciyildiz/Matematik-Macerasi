package com.yigit033.matematikmacarasi.services

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

/**
 * Single active update coordinator.
 *
 * Google Play owns update availability, installation and version ordering.
 * Firebase's old VersionCheckService is retained only for already installed
 * legacy APKs; it is no longer called by the active home screen.
 *
 * This service is application-scoped and never holds an Activity reference.
 * The UI is only rendered from MainActivity after its launch artwork ends.
 */
class UpdateCheckService private constructor(context: Context) {
    companion object {
        private const val TAG = "UpdateCenter"
        private const val PREFS_NAME = "update_center_v2"
        private const val CHECKED_AT = "checked_at"
        private const val DISMISSED_CODE = "dismissed_code"
        private const val DISMISSED_AT = "dismissed_at"
        private const val INSTALL_POSTPONED_AT = "install_postponed_at"
        private const val WHATS_NEW_SEEN = "whats_new_seen"
        private const val FIRST_UPDATE_CENTER_VERSION = 24

        @Volatile private var instance: UpdateCheckService? = null
        fun getInstance(context: Context): UpdateCheckService =
            instance ?: synchronized(this) {
                instance ?: UpdateCheckService(context.applicationContext).also { instance = it }
            }
    }

    private val appContext = context.applicationContext
    private val manager: AppUpdateManager = AppUpdateManagerFactory.create(appContext)
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var showUpdateDialog by mutableStateOf(false)
        private set
    var isInstallReady by mutableStateOf(false)
        private set
    var isDownloading by mutableStateOf(false)
        private set
    var downloadProgress by mutableIntStateOf(0)
        private set
    var showWhatsNewDialog by mutableStateOf(false)
        private set
    var availableVersionCode by mutableIntStateOf(0)
        private set

    private var checking = false

    private val installListener = InstallStateUpdatedListener { installState ->
        when (installState.installStatus()) {
            InstallStatus.DOWNLOADING -> {
                isDownloading = true
                showUpdateDialog = false
                val total = installState.totalBytesToDownload()
                downloadProgress = if (total > 0L) {
                    ((installState.bytesDownloaded() * 100L) / total).toInt().coerceIn(0, 100)
                } else 0
            }
            InstallStatus.DOWNLOADED -> {
                isDownloading = false
                isInstallReady = shouldOfferInstallation()
                showUpdateDialog = false
            }
            InstallStatus.INSTALLED -> {
                isDownloading = false
                isInstallReady = false
                downloadProgress = 0
            }
            InstallStatus.FAILED, InstallStatus.CANCELED -> {
                isDownloading = false
                downloadProgress = 0
                // The game remains usable; a later check can offer the update again.
            }
        }
    }

    init {
        runCatching { manager.registerListener(installListener) }
            .onFailure { Log.w(TAG, "Listener registration unavailable", it) }
        maybeShowWhatsNew()
    }

    /** Call on the home Activity's onResume. Never interrupt a game Activity. */
    fun checkForUpdates(activity: Activity) {
        if (activity.isFinishing || activity.isDestroyed || checking) return
        val now = System.currentTimeMillis()
        val lastCheck = prefs.getLong(CHECKED_AT, 0L)
        if (now >= lastCheck && now - lastCheck < UpdatePromptPolicy.CHECK_AFTER_MS &&
            !isDownloading && !isInstallReady) return
        checking = true
        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                checking = false
                prefs.edit().putLong(CHECKED_AT, System.currentTimeMillis()).apply()
                when (info.installStatus()) {
                    InstallStatus.DOWNLOADED -> {
                        isDownloading = false
                        isInstallReady = shouldOfferInstallation()
                        showUpdateDialog = false
                    }
                    InstallStatus.DOWNLOADING -> {
                        isDownloading = true
                        showUpdateDialog = false
                    }
                }
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE) && !isInstallReady &&
                    !isDownloading) {
                    val code = info.availableVersionCode()
                    availableVersionCode = code
                    if (UpdatePromptPolicy.shouldPrompt(
                            code, currentVersionCode(),
                            prefs.getInt(DISMISSED_CODE, 0),
                            prefs.getLong(DISMISSED_AT, 0L), System.currentTimeMillis()
                        )) {
                        showUpdateDialog = true
                    }
                } else if (!isInstallReady && !isDownloading) {
                    showUpdateDialog = false
                }
            }
            .addOnFailureListener {
                checking = false
                Log.w(TAG, "Play update check unavailable: " + it.javaClass.simpleName)
                // No fabricated update prompt when Play Store is unavailable.
            }
    }

    /** Always obtain a fresh AppUpdateInfo: PlayCore tokens are single-use. */
    fun startFlexibleUpdate(
        activity: Activity,
        launcher: ActivityResultLauncher<IntentSenderRequest>
    ) {
        showUpdateDialog = false
        manager.appUpdateInfo
            .addOnSuccessListener { info ->
                if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                    info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                    try {
                        val started = manager.startUpdateFlowForResult(
                            info, launcher,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                        )
                        if (!started) openPlayStore(activity)
                    } catch (e: Exception) {
                        Log.w(TAG, "In-app flow failed; opening Play", e)
                        openPlayStore(activity)
                    }
                } else if (info.installStatus() == InstallStatus.DOWNLOADED) {
                    isInstallReady = shouldOfferInstallation()
                } else {
                    openPlayStore(activity)
                }
            }
            .addOnFailureListener {
                Log.w(TAG, "Play flow unavailable; opening store", it)
                openPlayStore(activity)
            }
    }

    fun completeDownloadedUpdate() {
        if (!isInstallReady) return
        manager.completeUpdate().addOnFailureListener {
            Log.w(TAG, "Play couldn't complete downloaded update", it)
            // Keep the ready action available for retry; never trap the player.
        }
    }

    fun dismissUpdateDialog() {
        showUpdateDialog = false
        if (availableVersionCode > 0) {
            prefs.edit()
                .putInt(DISMISSED_CODE, availableVersionCode)
                .putLong(DISMISSED_AT, System.currentTimeMillis())
                .apply()
        }
    }

    fun onUpdateFlowCancelled() {
        isDownloading = false
        dismissUpdateDialog()
    }

    fun onUpdateFlowFailed() {
        isDownloading = false
        showUpdateDialog = false
        // A failed Play flow should be rechecked on the next foreground visit,
        // not hidden for 24 hours as though the family chose "Daha Sonra".
        prefs.edit().putLong(CHECKED_AT, 0L).apply()
    }

    fun postponeInstallation() {
        // The download stays available. Offer installation on a later home visit.
        isInstallReady = false
        prefs.edit()
            .putLong(INSTALL_POSTPONED_AT, System.currentTimeMillis())
            .putLong(CHECKED_AT, 0L)
            .apply()
    }

    fun dismissWhatsNew() {
        showWhatsNewDialog = false
        prefs.edit().putInt(WHATS_NEW_SEEN, currentVersionCode()).apply()
    }

    fun openWhatsNew() {
        showWhatsNewDialog = true
    }

    private fun maybeShowWhatsNew() {
        val code = currentVersionCode()
        if (code < FIRST_UPDATE_CENTER_VERSION || prefs.getInt(WHATS_NEW_SEEN, 0) >= code) return
        val updated = runCatching {
            val info = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
            UpdatePromptPolicy.isUpdatedInstallation(info.firstInstallTime, info.lastUpdateTime)
        }.getOrDefault(false)
        if (updated) {
            showWhatsNewDialog = true
        } else {
            // A new installation isn't an upgrade; don't show a misleading dialog.
            prefs.edit().putInt(WHATS_NEW_SEEN, code).apply()
        }
    }

    private fun shouldOfferInstallation(): Boolean {
        val postponed = prefs.getLong(INSTALL_POSTPONED_AT, 0L)
        val now = System.currentTimeMillis()
        return postponed <= 0L || now < postponed ||
            now - postponed >= UpdatePromptPolicy.REMIND_AFTER_MS
    }

    @Suppress("DEPRECATION")
    private fun currentVersionCode(): Int = runCatching {
        val info = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
        if (Build.VERSION.SDK_INT >= 28) info.longVersionCode.toInt()
        else info.versionCode
    }.getOrDefault(0)

    private fun openPlayStore(activity: Activity) {
        val url = Uri.parse("https://play.google.com/store/apps/details?id=" + appContext.packageName)
        val playIntent = Intent(Intent.ACTION_VIEW, url).setPackage("com.android.vending")
        runCatching { activity.startActivity(playIntent) }.onFailure {
            runCatching { activity.startActivity(Intent(Intent.ACTION_VIEW, url)) }
                .onFailure { error -> Log.w(TAG, "Store unavailable", error) }
        }
    }
}
