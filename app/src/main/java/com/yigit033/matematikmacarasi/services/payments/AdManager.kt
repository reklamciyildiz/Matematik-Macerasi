package com.yigit033.matematikmacarasi.services.payments

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.annotation.StringRes
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import android.os.Bundle
import com.google.ads.mediation.admob.AdMobAdapter
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import com.yigit033.matematikmacarasi.utils.NetworkUtils
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Reklam gösterimi ve yönetimi için kullanılan sınıf.
 * Premium kullanıcılar için reklamları göstermez.
 * Not: Gerçek reklam gösterimi için Google AdMob kütüphanesi eklenmelidir.
 * Bu sınıf şu anda sadece premium durumuna göre reklam gösterilip gösterilmeyeceğini kontrol eder.
 */
class AdManager private constructor(context: Context) {
    private val TAG = "AdManager"
    
    // Application context kullan - memory leak önlemek ve lifecycle bağımlılığını kaldırmak için
    private val appContext: Context = context.applicationContext
    
    // Test modu - false yaparak gerçek reklamları gösterecek şekilde ayarlıyoruz
    // NOT: Play Store'a yüklemeden önce bu değer false olmalıdır
    private val TEST_MODE = false
    
    // Satın alma yöneticisi - exception korumalı bir şekilde başlat
    private val purchaseManager by lazy {
        try {
            PurchaseManager.getInstance(appContext)
        } catch (e: Exception) {
            Log.e(TAG, "PurchaseManager başlatma hatası: ${e.message}", e)
            null
        }
    }
    
    // Reklam nesneleri
    private var bannerAd: AdView? = null
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    
    // Reklam yükleme işaretçileri
    private val isInterstitialLoading = AtomicBoolean(false)
    private val isRewardedLoading = AtomicBoolean(false)
    
    // Reklam sıklığını kontrol etmek için yeni değişkenler
    private var gameSessionCounter = 0
    private var lastAdShowTime = 0L
    
    // Konfigürasyon sabitleri (Google Play Families & Çocuk Güvenliği Politikası Uyumlu)
    private val MIN_AD_INTERVAL_MS = 120000L // Minimum 120 saniye bekleme
    private val GAMES_BETWEEN_ADS = 4 // En az 4 oyunda bir reklam aralığı
    private val AGGRESSIVE_MODE = false // Agresif mod kesinlikle KAPALI
    private val ENABLE_AUTOMATIC_INTERSTITIALS = false // İlk stabilizasyon aşamasında çocukların oyun akışını kesen otomatik geçiş reklamları devre dışı
    
    companion object {
        // Test ID'leri - Google'ın test ID'leri
        private const val TEST_BANNER_AD_UNIT_ID = "ca-app-pub-3940256099942544/6300978111"
        private const val TEST_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val TEST_REWARDED_AD_UNIT_ID = "ca-app-pub-3940256099942544/5224354917"
        
        // Gerçek Reklam ID'leri
        private const val REAL_REWARDED_AD_UNIT_ID = "ca-app-pub-9425978099676149/4954593567"
        private const val REAL_INTERSTITIAL_AD_UNIT_ID = "ca-app-pub-9425978099676149/3964787396" // Gerçek interstitial reklam ID'si
        
        // Uygulama ID'si
        private const val APP_ID = "ca-app-pub-9425978099676149~7802874919"
        
        @Volatile
        private var instance: AdManager? = null
        
        @Synchronized
        fun getInstance(context: Context): AdManager {
            if (context == null) {
                Log.e("AdManager", "Context null olarak geldi!")
                throw IllegalArgumentException("AdManager için Context null olamaz")
            }
            
            Log.d("AdManager", "getInstance çağrıldı, Context: ${context.javaClass.simpleName}")
            
            return instance ?: synchronized(this) {
                instance ?: try {
                    AdManager(context.applicationContext).also {
                        Log.d("AdManager", "Yeni AdManager örneği oluşturuldu")
                        instance = it
                    }
                } catch (e: Exception) {
                    Log.e("AdManager", "AdManager oluşturma hatası: ${e.message}", e)
                    throw e
                }
            }
        }
        
        // AdManager örneğini sıfırla - test amaçlı kullanılabilir
        fun resetInstance() {
            instance = null
            Log.d("AdManager", "AdManager örneği sıfırlandı")
        }
    }
    
    init {
        Log.d(TAG, "AdManager başlatıldı, appContext: ${appContext.javaClass.simpleName}")
        
        try {
            // Önce MobileAds'in durumunu kontrol et
            val wasInitialized = try {
                MobileAds.getInitializationStatus() != null
            } catch (e: Exception) {
                Log.e(TAG, "MobileAds durum kontrol hatası: ${e.message}", e)
                false
            }
            
            // Eğer zaten başlatılmışsa tekrar başlatma
            if (wasInitialized) {
                Log.d(TAG, "MobileAds zaten başlatılmış, tekrar başlatılmıyor")
                // Yine de reklam yüklemeyi başlat
                loadRewardedAd()
                loadInterstitialAd() // Interstitial reklamları da önceden yükle
            } else {
                // MobileAds'i başlat
                MobileAds.initialize(appContext) { initializationStatus ->
                    val statusMap = initializationStatus.adapterStatusMap
                    statusMap.forEach { (adapter, status) ->
                        Log.d(TAG, "Adapter: $adapter, Durum: ${status.initializationState}, Açıklama: ${status.description}")
                    }
                    
                    // Reklam yüklemeyi başlat
                    loadRewardedAd()
                    loadInterstitialAd() // Interstitial reklamları da önceden yükle
                    
                    Log.d(TAG, "AdMob başarıyla başlatıldı - state: ${initializationStatus.adapterStatusMap["com.google.android.gms.ads.MobileAds"]?.initializationState}")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob başlatma hatası: ${e.message}", e)
            // Hataya rağmen reklam yüklemeyi dene
            try {
                loadRewardedAd()
                loadInterstitialAd() // Interstitial reklamları da önceden yükle
            } catch (e2: Exception) {
                Log.e(TAG, "Reklam yükleme hatası: ${e2.message}", e2)
            }
        }
    }
    
    /**
     * İnternet bağlantısının varlığını kontrol eder
     * @return İnternet bağlantısı varsa true, yoksa false
     */
    fun isInternetAvailable(): Boolean {
        return NetworkUtils.isInternetAvailable(appContext)
    }
    
    /**
     * Reklamların gösterilip gösterilmeyeceğini kontrol eder.
     * Premium kullanıcılar için reklamlar gösterilmez.
     */
    fun shouldShowAds(): Boolean {
        val isPremium = try {
            purchaseManager?.isPremium() ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Premium durumu kontrol edilirken hata: ${e.message}", e)
            false
        }
        Log.d(TAG, "Reklam gösterimi kontrolü: isPremium=$isPremium")
        return !isPremium
    }
    
    /**
     * Geliştirilmiş reklam gösterim kontrolü
     * Premium kullanıcılar için reklamları göstermez
     * Non-premium için agresif reklam stratejisi
     */
    fun shouldShowInterstitialAd(): Boolean {
        // Google Play Families: Çocuk akışını kesen otomatik geçiş reklamları kapalı
        if (!ENABLE_AUTOMATIC_INTERSTITIALS) {
            Log.d(TAG, "Families Politikası: Otomatik geçiş reklamları devre dışı")
            return false
        }
        
        val isPremium = try {
            purchaseManager?.isPremium() ?: false
        } catch (e: Exception) {
            Log.e(TAG, "Premium durumu kontrol edilirken hata: ${e.message}", e)
            false
        }
        
        // Premium kullanıcılar reklam görmez
        if (isPremium) {
            Log.d(TAG, "Premium kullanıcı, reklam gösterilmiyor")
            return false
        }
        
        // Non-premium kullanıcılar için agresif strateji
        gameSessionCounter++
        val currentTime = System.currentTimeMillis()
        val timeSinceLastAd = currentTime - lastAdShowTime
        
        // İlk oyundan sonra hemen reklam göster
        if (gameSessionCounter == 1 && lastAdShowTime == 0L) {
            Log.d(TAG, "İlk oyun tamamlandı, reklam gösterilecek")
            return true
        }
        
        // Agresif mod: Her oyundan sonra reklam (minimum süre kontrolü ile)
        if (AGGRESSIVE_MODE && timeSinceLastAd >= MIN_AD_INTERVAL_MS) {
            Log.d(TAG, "Aggressive mode: Her oyun sonrası reklam gösterilecek")
            return true
        }
        
        // Normal mod: Belirlenen frekansa göre reklam
        if (gameSessionCounter >= GAMES_BETWEEN_ADS && timeSinceLastAd >= MIN_AD_INTERVAL_MS) {
            Log.d(TAG, "Reklam gösterim koşulları sağlandı: games=$gameSessionCounter, timeSince=${timeSinceLastAd}ms")
            return true
        }
        
        Log.d(TAG, "Reklam gösterilmeyecek: games=$gameSessionCounter, timeSince=${timeSinceLastAd}ms")
        return false
    }
    
    /**
     * Reklam gösterildikten sonra sayaçları sıfırla
     */
    fun markAdShown() {
        gameSessionCounter = 0
        lastAdShowTime = System.currentTimeMillis()
        Log.d(TAG, "Reklam gösterildi, sayaçlar sıfırlandı")
    }
    
    /**
     * Premium satın alma sonrası reklamları durdurmak için
     */
    fun onPremiumPurchased() {
        gameSessionCounter = 0
        lastAdShowTime = 0L
        Log.d(TAG, "Premium satın alındı, reklam sayaçları sıfırlandı")
    }
    
    /**
     * Banner reklamını yükler ve gösterir.
     * Bu metod, premium durumuna göre reklam gösterilip gösterilmeyeceğini kontrol eder.
     * @param adContainer Reklamın yerleştirileceği ViewGroup
     */
    /**
     * Google Play Families Policy: Cocuklara yonelik kisisellestirilmemis reklam istegi
     */
    private fun createAdRequest(): AdRequest {
        val extras = Bundle().apply {
            putString("npa", "1")
        }
        return AdRequest.Builder()
            .addNetworkExtrasBundle(AdMobAdapter::class.java, extras)
            .build()
    }

    fun loadBannerAd(adContainer: ViewGroup) {
        if (!shouldShowAds()) {
            Log.d(TAG, "Premium kullanıcı, banner reklam gösterilmiyor")
            adContainer.removeAllViews()
            return
        }
        
        // İnternet bağlantısı kontrolü
        if (!isInternetAvailable()) {
            Log.d(TAG, "İnternet bağlantısı yok, banner reklam gösterilemiyor")
            adContainer.removeAllViews()
            return
        }
        
        try {
            Log.d(TAG, "Banner reklam yükleniyor...")
            
            // Eğer varsa önceki reklamı temizle
            if (bannerAd != null) {
                adContainer.removeAllViews()
            }
            
            // Yeni AdView oluştur
            bannerAd = AdView(appContext).apply {
                adUnitId = TEST_BANNER_AD_UNIT_ID
                setAdSize(AdSize.BANNER)
                
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        Log.d(TAG, "Banner reklam yüklendi")
                    }
                    
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        Log.e(TAG, "Banner reklam yüklenemedi: ${adError.message}")
                    }
                    
                    override fun onAdOpened() {
                        Log.d(TAG, "Banner reklam açıldı")
                    }
                    
                    override fun onAdClosed() {
                        Log.d(TAG, "Banner reklam kapatıldı")
                    }
                }
            }
            
            // AdView'ı container'a ekle
            adContainer.addView(bannerAd)
            
            // Reklam isteği oluştur ve yükle
            val adRequest = createAdRequest()
            bannerAd?.loadAd(adRequest)
            
        } catch (e: Exception) {
            Log.e(TAG, "Banner reklam yükleme hatası", e)
        }
    }
    
    /**
     * Geçiş reklamını yükler.
     * Bu metod, premium durumuna göre reklam gösterilip gösterilmeyeceğini kontrol eder.
     */
    fun loadInterstitialAd() {
        if (!shouldShowAds()) {
            Log.d(TAG, "Premium kullanıcı, geçiş reklamı yüklenmiyor")
            return
        }
        
        // İnternet bağlantısı kontrolü
        if (!isInternetAvailable()) {
            Log.d(TAG, "İnternet bağlantısı yok, geçiş reklamı yüklenemiyor")
            return
        }
        
        // Zaten yükleme yapılıyorsa çık
        if (isInterstitialLoading.getAndSet(true)) {
            Log.d(TAG, "Geçiş reklamı zaten yükleniyor")
            return
        }
        
        try {
            Log.d(TAG, "Geçiş reklamı yükleniyor...")
            
            // Reklam yükleme isteği
            val adRequest = createAdRequest()
            
            // Hangi reklam ID'sini kullanacağımızı belirle
            val adUnitId = if (TEST_MODE) TEST_INTERSTITIAL_AD_UNIT_ID else REAL_INTERSTITIAL_AD_UNIT_ID
            
            // InterstitialAd yükleme
            InterstitialAd.load(
                appContext,
                adUnitId,
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        Log.d(TAG, "Geçiş reklamı yüklendi")
                        interstitialAd = ad
                        isInterstitialLoading.set(false)
                        
                        // Callback'leri ayarla
                        interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                            override fun onAdDismissedFullScreenContent() {
                                Log.d(TAG, "Geçiş reklamı kapatıldı")
                                interstitialAd = null
                                // Otomatik olarak yeni reklam yükle
                                loadInterstitialAd()
                            }
                            
                            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                                Log.e(TAG, "Geçiş reklamı gösterilemedi: ${adError.message}")
                                interstitialAd = null
                            }
                            
                            override fun onAdShowedFullScreenContent() {
                                Log.d(TAG, "Geçiş reklamı gösterildi")
                            }
                        }
                    }
                    
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, "Geçiş reklamı yüklenemedi: ${loadAdError.message}")
                        interstitialAd = null
                        isInterstitialLoading.set(false)
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Geçiş reklamı yükleme hatası", e)
            isInterstitialLoading.set(false)
        }
    }
    
    /**
     * Geçiş reklamını gösterir.
     * Bu metod, premium durumuna göre reklam gösterilip gösterilmeyeceğini kontrol eder.
     */
    fun showInterstitialAd(activity: Activity) {
        if (!shouldShowAds()) {
            Log.d(TAG, "Premium kullanıcı, geçiş reklamı gösterilmiyor")
            return
        }
        
        // İnternet bağlantısı kontrolü
        if (!isInternetAvailable()) {
            Log.d(TAG, "İnternet bağlantısı yok, geçiş reklamı gösterilemiyor")
            NetworkUtils.showNoInternetToast(activity)
            return
        }
        
        if (interstitialAd != null) {
            Log.d(TAG, "Geçiş reklamı gösteriliyor...")
            interstitialAd?.show(activity)
        } else {
            Log.d(TAG, "Geçiş reklamı henüz yüklenmedi, yükleniyor...")
            loadInterstitialAd()
        }
    }
    
    /**
     * Geliştirilmiş oyun sonu interstitial reklam gösterimi
     * Agresif strateji ile premium olmayan kullanıcılar için her oyun sonrası reklam
     */
    fun showInterstitialAdAfterGame(activity: Activity) {
        // Önce gösterim kontrolü yap
        if (!shouldShowInterstitialAd()) {
            Log.d(TAG, "Reklam gösterim koşulları sağlanmadı")
            return
        }
        
        // İnternet bağlantısı kontrolü
        if (!isInternetAvailable()) {
            Log.d(TAG, "İnternet bağlantısı yok, oyun sonu reklamı gösterilemiyor")
            NetworkUtils.showNoInternetToast(activity)
            return
        }
        
        if (interstitialAd != null) {
            Log.d(TAG, "Oyun sonu reklamı gösteriliyor...")
            interstitialAd?.show(activity)
            markAdShown() // Reklam gösterildikten sonra sayaçları sıfırla
        } else {
            Log.d(TAG, "Oyun sonu reklamı henüz yüklenmedi, yükleniyor...")
            loadInterstitialAd()
        }
    }
    
    /**
     * Ödüllü reklamı yükler.
     * Bu metod premium kullanıcılar için de çalışır, çünkü ödüllü reklamlar isteğe bağlıdır.
     */
    fun loadRewardedAd() {
        // İnternet bağlantısı kontrolü
        if (!isInternetAvailable()) {
            Log.d(TAG, "İnternet bağlantısı yok, ödüllü reklam yüklenemiyor")
            return
        }
        
        // Zaten yükleme yapılıyorsa çık
        if (isRewardedLoading.getAndSet(true)) {
            Log.d(TAG, "Ödüllü reklam zaten yükleniyor")
            return
        }
        
        try {
            Log.d(TAG, "Ödüllü reklam yükleniyor...")
            
            // Reklam yükleme isteği
            val adRequest = createAdRequest()
            
            // Hangi reklam ID'sini kullanacağımızı belirle
            val adUnitId = if (TEST_MODE) TEST_REWARDED_AD_UNIT_ID else REAL_REWARDED_AD_UNIT_ID
            
            // RewardedAd yükleme
            RewardedAd.load(
                appContext,
                adUnitId,
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedAd) {
                        Log.d(TAG, "Ödüllü reklam yüklendi")
                        rewardedAd = ad
                        isRewardedLoading.set(false)
                    }
                    
                    override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                        Log.e(TAG, "Ödüllü reklam yüklenemedi: ${loadAdError.message}")
                        rewardedAd = null
                        isRewardedLoading.set(false)
                        
                        // Test modunda bile reklam yüklenemezse sorun olduğunu göster
                        if (TEST_MODE) {
                            Log.e(TAG, "TEST MODU aktif olmasına rağmen reklam yüklenemedi: ${loadAdError.code}")
                        }
                    }
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Ödüllü reklam yükleme hatası: ${e.message}", e)
            isRewardedLoading.set(false)
        }
    }
    
    /**
     * Ödüllü reklamı gösterir.
     * Bu metod premium kullanıcılar için de çalışır, çünkü ödüllü reklamlar isteğe bağlıdır.
     * @param activity İçerik için Activity
     * @param onRewarded Ödül verildiğinde çağrılacak callback
     * @param onAdNotAvailable Reklam bulunamadığında çağrılacak callback, isteğe bağlı
     */
    fun showRewardedAd(
        activity: Activity, 
        onRewarded: (Int) -> Unit,
        onAdNotAvailable: (() -> Unit)? = null
    ) {
        try {
            // Premium kullanıcılar da reklam izleyebilir (yeni sistem)
            val isPremium = purchaseManager?.isPremium() == true
            Log.d(TAG, "Reklam izleme isteği - Premium kullanıcı: $isPremium (Premium kullanıcılar da izleyebilir)")
            
            // İnternet bağlantısı kontrolü
            if (!isInternetAvailable()) {
                Log.d(TAG, "İnternet bağlantısı yok, ödüllü reklam gösterilemiyor")
                NetworkUtils.showNoInternetToast(activity)
                onAdNotAvailable?.invoke()
                return
            }
            
            // Test modu aktifse her zaman ödül ver
            if (TEST_MODE) {
                // Test modunda hemen 1 yıldız verelim
                Log.d(TAG, "TEST MODU: Ödüllü reklam simülasyonu, 1 yıldız veriliyor")
                onRewarded(1)
                return
            }
            
            // Reklam henüz yüklenmemişse
            if (rewardedAd == null) {
                Log.d(TAG, "Ödüllü reklam henüz yüklenmedi, yükleniyor...")
                // Önce reklamı yüklemeyi dene, sonra onAdNotAvailable'ı çağır
                loadRewardedAd()
                onAdNotAvailable?.invoke()
                return
            }
            
            // Reklam yüklenmişse göster
            Log.d(TAG, "Ödüllü reklam gösteriliyor...")
            
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Ödüllü reklam kapatıldı")
                    rewardedAd = null
                    // Otomatik olarak yeni reklam yükle
                    loadRewardedAd()
                }
                
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.e(TAG, "Ödüllü reklam gösterilemedi: ${adError.message}")
                    rewardedAd = null
                    // Reklam daha sonra tekrar yüklenebilsin
                    loadRewardedAd()
                    
                    // Callback'i çağır
                    onAdNotAvailable?.invoke()
                }
                
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "Ödüllü reklam gösterildi")
                }
            }
            
            rewardedAd?.show(activity) { rewardItem ->
                // Kullanıcı ödül kazandı - ödül miktarını 1 yıldız olarak ayarla
                val rewardAmount = 1 // Sabit 1 yıldız ödül
                Log.d(TAG, "Ödüllü reklam izlendi, ödül miktarı: $rewardAmount")
                onRewarded(rewardAmount)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Ödüllü reklam gösterme hatası: ${e.message}", e)
            // Reklam daha sonra tekrar yüklenebilsin
            loadRewardedAd()
            
            // Hata durumunda callback'i çağır
            onAdNotAvailable?.invoke()
        }
    }
    
    /**
     * Reklam nesnelerini temizler ve hafızayı boşaltır.
     * Activity'nin onDestroy metodunda çağrılmalıdır.
     */
    fun destroy() {
        bannerAd?.destroy()
        bannerAd = null
        interstitialAd = null
        rewardedAd = null
        Log.d(TAG, "AdManager kaynakları temizlendi")
    }
} 