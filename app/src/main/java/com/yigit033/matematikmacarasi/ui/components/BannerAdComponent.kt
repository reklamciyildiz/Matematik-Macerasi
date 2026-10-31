package com.yigit033.matematikmacarasi.ui.components

import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.yigit033.matematikmacarasi.services.payments.AdManager
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager

/**
 * Alt bar için kompakt banner reklam bileşeni
 * Ayarlar ve İkon Değiştir butonları arasında gösterilir
 */
@Composable
fun CompactBannerAdComponent(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPremium by remember { mutableStateOf(false) }
    var isAdLoaded by remember { mutableStateOf(false) }
    var adError by remember { mutableStateOf<String?>(null) }
    
    // Premium durumunu kontrol et
    LaunchedEffect(Unit) {
        try {
            val purchaseManager = PurchaseManager.getInstance(context)
            isPremium = purchaseManager.isPremiumSync()
            
            // Premium durumu değişikliklerini dinle
            purchaseManager.premiumState.collect { premiumState ->
                isPremium = premiumState
            }
        } catch (e: Exception) {
            Log.e("CompactBannerAd", "Premium durumu kontrol edilirken hata: ${e.message}", e)
            isPremium = false
        }
    }
    
    // Premium kullanıcılar için reklam gösterme
    if (isPremium) {
        return
    }
    
    // Doğrulanmamış test ID'sinin canlı yayında çocuklara gösterilmesini engelle
    // Yalnızca debug test ortamında gösterilir
    val isDebug = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    if (!isDebug) {
        return
    }
    
    // Reklam yüklenmediyse veya hata varsa boş alan bırak
    if (!isAdLoaded && adError != null) {
        return
    }
    
    Card(
        modifier = modifier
            .width(200.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                AdView(context).apply {
                    // Test banner reklam ID'si
                    adUnitId = "ca-app-pub-3940256099942544/6300978111"
                    
                    // Kompakt banner boyutu
                    setAdSize(AdSize(200, 50))
                    
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    
                    // Reklam yükleme listener'ı
                    adListener = object : com.google.android.gms.ads.AdListener() {
                        override fun onAdLoaded() {
                            super.onAdLoaded()
                            isAdLoaded = true
                            adError = null
                            Log.d("CompactBannerAd", "Kompakt banner reklam başarıyla yüklendi")
                        }
                        
                        override fun onAdFailedToLoad(loadAdError: com.google.android.gms.ads.LoadAdError) {
                            super.onAdFailedToLoad(loadAdError)
                            isAdLoaded = false
                            adError = loadAdError.message
                            Log.e("CompactBannerAd", "Kompakt banner reklam yüklenemedi: ${loadAdError.message}")
                        }
                        
                        override fun onAdClicked() {
                            super.onAdClicked()
                            Log.d("CompactBannerAd", "Kompakt banner reklam tıklandı")
                        }
                    }
                    
                    // Reklam isteği oluştur ve yükle
                    try {
                        val extras = android.os.Bundle().apply { putString("npa", "1") }
                        val adRequest = AdRequest.Builder()
                            .addNetworkExtrasBundle(com.google.ads.mediation.admob.AdMobAdapter::class.java, extras)
                            .build()
                        loadAd(adRequest)
                        Log.d("CompactBannerAd", "Kompakt banner reklam yükleme başlatıldı")
                    } catch (e: Exception) {
                        Log.e("CompactBannerAd", "Reklam yükleme hatası: ${e.message}", e)
                        adError = e.message
                    }
                }
            }
        )
    }
}

/**
 * Yedek banner bileşeni - reklam yüklenemediğinde gösterilir
 */
@Composable
private fun PlaceholderBanner(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(200.dp)
            .height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Gray.copy(alpha = 0.1f)
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Reklam Yükleniyor...",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
} 