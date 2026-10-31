package com.yigit033.matematikmacarasi.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.widget.Toast

/**
 * Network utility fonksiyonları
 */
object NetworkUtils {
    
    /**
     * İnternet bağlantısının olup olmadığını kontrol eder.
     */
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            @Suppress("DEPRECATION")
            networkInfo?.isConnectedOrConnecting == true
        }
    }
    
    /**
     * İnternet bağlantısı olmadığında kullanıcıya Toast mesajı gösterir.
     */
    fun showNoInternetToast(context: Context) {
        Toast.makeText(
            context,
            "İnternet bağlantısı yok. Lütfen bağlantınızı kontrol edin.",
            Toast.LENGTH_SHORT
        ).show()
    }
}
