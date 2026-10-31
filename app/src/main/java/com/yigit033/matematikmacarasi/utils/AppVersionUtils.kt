package com.yigit033.matematikmacarasi.utils

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * Uygulama versiyon bilgilerini almak için yardımcı sınıf
 */
object AppVersionUtils {
    
    /**
     * Uygulamanın versiyon adını alır (örn: "1.1")
     */
    fun getVersionName(context: Context): String {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName, 
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            packageInfo.versionName ?: "1.0"
        } catch (e: PackageManager.NameNotFoundException) {
            "1.0"
        }
    }
    
    /**
     * Uygulamanın versiyon kodunu alır (örn: 10)
     */
    fun getVersionCode(context: Context): Long {
        return try {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName, 
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            1L
        }
    }
    
    /**
     * Tam versiyon bilgisini alır (örn: "1.1 (10)")
     */
    fun getFullVersionInfo(context: Context): String {
        val versionName = getVersionName(context)
        val versionCode = getVersionCode(context)
        return "$versionName ($versionCode)"
    }
} 