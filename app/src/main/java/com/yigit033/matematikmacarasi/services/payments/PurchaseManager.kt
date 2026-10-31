package com.yigit033.matematikmacarasi.services.payments

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.yigit033.matematikmacarasi.services.PremiumCodeValidator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob

/**
 * Google Play Store satın alma işlemlerini yöneten sınıf.
 * Bu sınıf, premium sürüm satın alma ve durumunu kontrol etme işlemlerini gerçekleştirir.
 */
class PurchaseManager private constructor(
    private val context: Context
) : PurchasesUpdatedListener {
    
    companion object {
        private const val TAG = "PurchaseManager"
        private const val PREMIUM_SKU = "matematik_macerasi_premium"
        private const val DEBUG_MODE = false // Production için false
        private const val DEBUG_PREMIUM = false // Production için false
        
        @Volatile
        private var INSTANCE: PurchaseManager? = null
        
        fun getInstance(context: Context): PurchaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PurchaseManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    // BillingClient - PurchasesUpdatedListener ile başlatılıyor
    private val pendingPurchasesParams = PendingPurchasesParams.newBuilder()
        .enableOneTimeProducts()
        .build()

    private val billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(this) // this = PurchasesUpdatedListener
        .enablePendingPurchases(pendingPurchasesParams)
        .build()
    
    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    // Premium kod validator
    private val codeValidator by lazy { 
        PremiumCodeValidator.getInstance(context)
    }
    
    // Premium durumu için StateFlow
    private val _premiumState = MutableStateFlow(false)
    val premiumState: StateFlow<Boolean> = _premiumState.asStateFlow()
    
    // Satın alma durumu için LiveData
    private val _purchaseStatus = MutableLiveData<PurchaseStatus>()
    val purchaseStatus: LiveData<PurchaseStatus> = _purchaseStatus
    
    // Satın alma durumları
    sealed class PurchaseStatus {
        object Success : PurchaseStatus()
        object Pending : PurchaseStatus()
        object Canceled : PurchaseStatus()
        object AlreadyOwned : PurchaseStatus()
        object ServiceUnavailable : PurchaseStatus()
        object ProductNotFound : PurchaseStatus()
        data class Failed(val code: Int) : PurchaseStatus()
    }
    
    init {
        Log.d(TAG, "🚀 PurchaseManager başlatılıyor...")
        connectToBillingService()
    }
    
    /**
     * PurchasesUpdatedListener implementasyonu
     * Bu fonksiyon satın alma işlemleri tamamlandığında otomatik olarak çağrılır
     */
    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: MutableList<Purchase>?) {
        val responseCode = billingResult.responseCode
        val debugMessage = billingResult.debugMessage
        
        Log.d(TAG, "📦 onPurchasesUpdated çağrıldı:")
        Log.d(TAG, "   - Response Code: $responseCode")
        Log.d(TAG, "   - Debug Message: $debugMessage")
        Log.d(TAG, "   - Purchases Count: ${purchases?.size ?: 0}")
        
        when (responseCode) {
            BillingClient.BillingResponseCode.OK -> {
                Log.d(TAG, "✅ Satın alma başarılı!")
                purchases?.let { purchaseList ->
                    processPurchases(purchaseList)
                }
            }
            BillingClient.BillingResponseCode.USER_CANCELED -> {
                Log.d(TAG, "❌ Kullanıcı satın almayı iptal etti")
                _purchaseStatus.value = PurchaseStatus.Canceled
            }
            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                Log.e(TAG, "❌ Google Play servisi kullanılamıyor")
                _purchaseStatus.value = PurchaseStatus.ServiceUnavailable
            }
            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                Log.e(TAG, "❌ Billing servisi kullanılamıyor")
                _purchaseStatus.value = PurchaseStatus.ServiceUnavailable
            }
            BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                Log.e(TAG, "❌ Ürün bulunamadı")
                _purchaseStatus.value = PurchaseStatus.ProductNotFound
            }
            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                Log.e(TAG, "❌ Geliştirici hatası: $debugMessage")
                _purchaseStatus.value = PurchaseStatus.Failed(responseCode)
            }
            BillingClient.BillingResponseCode.ERROR -> {
                Log.e(TAG, "❌ Genel hata: $debugMessage")
                _purchaseStatus.value = PurchaseStatus.Failed(responseCode)
            }
            else -> {
                Log.e(TAG, "❌ Bilinmeyen hata: $responseCode - $debugMessage")
                _purchaseStatus.value = PurchaseStatus.Failed(responseCode)
            }
        }
    }
    
    /**
     * Google Play Billing servisine bağlanır.
     */
    private fun connectToBillingService() {
        if (billingClient.isReady) {
            Log.d(TAG, "✅ BillingClient zaten bağlı")
            return
        }
        
        Log.d(TAG, "🔄 BillingClient bağlantısı başlatılıyor...")
        
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                val responseCode = billingResult.responseCode
                val debugMessage = billingResult.debugMessage
                
                Log.d(TAG, "📋 onBillingSetupFinished çağrıldı:")
                Log.d(TAG, "   - Response Code: $responseCode")
                Log.d(TAG, "   - Debug Message: $debugMessage")
                
                when (responseCode) {
                    BillingClient.BillingResponseCode.OK -> {
                        Log.d(TAG, "✅ Billing servisine bağlantı başarılı!")
                    // Kullanıcının premium durumunu kontrol et
                    queryPurchases()
                    }
                    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                        Log.e(TAG, "❌ Billing servisi bu cihazda kullanılamıyor")
                        Log.e(TAG, "🔧 Olası nedenler:")
                        Log.e(TAG, "   - Google Play Store yüklü değil")
                        Log.e(TAG, "   - Google Play Services güncel değil")
                        Log.e(TAG, "   - Cihaz Google Play'i desteklemiyor")
                        _purchaseStatus.postValue(PurchaseStatus.ServiceUnavailable)
                    }
                    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                        Log.e(TAG, "❌ Google Play Store servisi şu anda kullanılamıyor")
                        Log.e(TAG, "🔧 Olası nedenler:")
                        Log.e(TAG, "   - İnternet bağlantısı yok")
                        Log.e(TAG, "   - Google Play Store geçici olarak erişilemez")
                        Log.e(TAG, "   - Sunucu bakımda")
                        _purchaseStatus.postValue(PurchaseStatus.ServiceUnavailable)
                    }
                    BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                        Log.e(TAG, "❌ DEVELOPER ERROR - Geliştirici hatası:")
                        Log.e(TAG, "   - Debug Message: $debugMessage")
                        Log.e(TAG, "🔧 Kontrol edilecekler:")
                        Log.e(TAG, "   1. AndroidManifest.xml'de BILLING permission var mı")
                        Log.e(TAG, "   2. Google Play Billing Library doğru sürümde mi")
                        Log.e(TAG, "   3. Uygulama imzası Google Play Console ile uyumlu mu")
                        _purchaseStatus.postValue(PurchaseStatus.ServiceUnavailable)
                    }
                    else -> {
                        Log.e(TAG, "❌ Bilinmeyen billing bağlantı hatası:")
                        Log.e(TAG, "   - Response Code: $responseCode")
                        Log.e(TAG, "   - Debug Message: $debugMessage")
                    _purchaseStatus.postValue(PurchaseStatus.ServiceUnavailable)
                    }
                }
            }
            
            override fun onBillingServiceDisconnected() {
                Log.w(TAG, "⚠️ Billing servisi bağlantısı kesildi")
                Log.d(TAG, "🔄 5 saniye sonra yeniden bağlanma deneniyor...")
                
                // 5 saniye içinde yeniden bağlanmayı dene
                scope.launch {
                    delay(5000)
                    if (!billingClient.isReady) {
                        Log.d(TAG, "🔄 Billing servisine yeniden bağlanma girişimi")
                    connectToBillingService()
                    } else {
                        Log.d(TAG, "✅ BillingClient zaten tekrar bağlandı")
                    }
                }
            }
        })
    }
    
    /**
     * Satın alma işlemini onaylar.
     */
    private fun processPurchases(purchases: List<Purchase>) {
                    for (purchase in purchases) {
                        handlePurchase(purchase)
        }
    }
    
    /**
     * Tek bir satın alma işlemini işler.
     */
    private fun handlePurchase(purchase: Purchase) {
        Log.d(TAG, "🔄 Satın alma işleniyor:")
        Log.d(TAG, "   - Ürün ID: ${purchase.products}")
        Log.d(TAG, "   - Purchase Token: ${purchase.purchaseToken}")
        Log.d(TAG, "   - Purchase State: ${purchase.purchaseState}")
        Log.d(TAG, "   - Is Acknowledged: ${purchase.isAcknowledged}")
        
        // Premium ürünü kontrol et
            if (purchase.products.contains(PREMIUM_SKU)) {
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    Log.d(TAG, "✅ Premium ürün başarıyla satın alındı!")
                    
                    // Premium durumunu güncelle
                _premiumState.value = true
                savePremiumStatus(true)
                    _purchaseStatus.value = PurchaseStatus.Success
                    
                    // Satın almayı onayla (acknowledge)
                    if (!purchase.isAcknowledged) {
                        acknowledgePurchase(purchase)
                    }
                }
                Purchase.PurchaseState.PENDING -> {
                    Log.d(TAG, "⏳ Premium satın alma beklemede...")
                    _purchaseStatus.value = PurchaseStatus.Pending
                }
                else -> {
                    Log.w(TAG, "⚠️ Bilinmeyen satın alma durumu: ${purchase.purchaseState}")
                }
            }
        } else {
            Log.w(TAG, "⚠️ Bilinmeyen ürün satın alındı: ${purchase.products}")
        }
    }
    
    /**
     * Satın almayı onaylar (acknowledge).
     */
    private fun acknowledgePurchase(purchase: Purchase) {
        Log.d(TAG, "🔄 Satın alma onaylanıyor...")
        
        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        
        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
            
            Log.d(TAG, "📋 Satın alma onaylama sonucu:")
            Log.d(TAG, "   - Response Code: $responseCode")
            Log.d(TAG, "   - Debug Message: $debugMessage")
            
            if (responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "✅ Satın alma başarıyla onaylandı")
                    } else {
                Log.e(TAG, "❌ Satın alma onaylanamadı: $responseCode - $debugMessage")
            }
        }
    }
    
    /**
     * Mevcut satın almaları sorgular.
     */
    fun queryPurchases() {
        if (!billingClient.isReady) {
            Log.e(TAG, "BillingClient hazır değil, yeniden bağlanılıyor...")
            connectToBillingService()
            return
        }
        
        scope.launch {
            try {
                val params = QueryPurchasesParams.newBuilder()
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
                
                val purchasesResult = billingClient.queryPurchasesAsync(params)
                
                if (purchasesResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val purchases = purchasesResult.purchasesList
                    Log.d(TAG, "Satın almalar sorgulandı, ${purchases.size} adet satın alma bulundu")
                    
                    // Premium sürüm kontrol et
                    var premiumFound = false
                    for (purchase in purchases) {
                        if (purchase.products.contains(PREMIUM_SKU) && 
                            purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                            
                            premiumFound = true
                            if (!purchase.isAcknowledged) {
                                acknowledgePurchase(purchase)
                            }
                        }
                    }
                    
                    // Premium kod aktivasyonu varsa onu da kontrol et!
                    val hasCodePremium = codeValidator.isCodePremium()
                    val finalPremiumStatus = premiumFound || hasCodePremium
                    
                    _premiumState.value = finalPremiumStatus
                    savePremiumStatus(premiumFound) // Sadece Google Play durumunu kaydet
                    
                    if (premiumFound) {
                        Log.d(TAG, "Premium sürüm bulundu ve aktif edildi")
                    } else {
                        Log.d(TAG, "Premium sürüm bulunamadı")
                    }
                } else {
                    Log.e(TAG, "Satın alma sorgulama hatası: ${purchasesResult.billingResult.responseCode}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Satın alma sorgulama hatası", e)
            }
        }
    }
    
    /**
     * Premium durumunu SharedPreferences'a kaydeder.
     */
    private fun savePremiumStatus(isPremium: Boolean) {
        val prefs = context.getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("is_premium", isPremium).apply()
    }
    
    /**
     * Premium durumunu SharedPreferences'dan alır.
     */
    private fun getPremiumStatus(): Boolean {
        val prefs = context.getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("is_premium", false)
    }
    
    /**
     * Premium durumunu kontrol eder.
     * Bu metod doğrudan çağırmak UI thread'i bloke edebilir.
     * Mümkünse flow üzerinden dinleyin ya da bu metodu arka planda çağırın.
     * 
     * Premium durumu: Google Play satın alma VEYA Firebase kod aktivasyonu
     */
    fun isPremium(): Boolean {
        try {
            // Geliştirme aşamasında debug moduna göre sonuç döndür
            if (DEBUG_MODE) {
                Log.w(TAG, "DEBUG MODU: Premium durumu ${if (DEBUG_PREMIUM) "AÇIK" else "KAPALI"} olarak simüle ediliyor!")
                return DEBUG_PREMIUM
            }
            
            // Önce local cache kontrolü yaparak hızlı sonuç döndür
            val cachedValue = _premiumState.value
            
            // Eğer cache var ise onu döndür
            if (cachedValue) {
                Log.d(TAG, "Premium durum (cache'den): true")
                return true
            }
            
            // Cache yoksa SharedPreferences'dan kontrol et (bu da hızlıdır)
            val prefs = context.getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)
            val prefValue = prefs.getBoolean("is_premium", false)
            
            // Eğer SharedPreferences'da premium ise, bunu güncelle ve döndür
            if (prefValue) {
                Log.d(TAG, "Premium durum (Google Play'den): true")
                _premiumState.value = true
                return true
            }
            
            // Firebase kod aktivasyon kontrolü
            val hasCodePremium = codeValidator.isCodePremium()
            
            if (hasCodePremium) {
                Log.d(TAG, "Premium durum (kod ile): true")
                _premiumState.value = true
                return true
            }
            
            // Google Play Store ödemelerini kontrol etmeden önce premium durumu false olarak döndür
            // Böylece kullanıcının deneyimi kesilmez
            Log.d(TAG, "Premium durum: false (arka planda billing kontrol edilecek)")
            
            // Asenkron olarak Google Play'den sorgulama yap - sonuç için bekletme
            scope.launch(Dispatchers.IO) {
                try {
                    if (!billingClient.isReady) {
                        Log.d(TAG, "BillingClient hazır değil, bağlanılıyor...")
                        connectToBillingService()
                        // Bağlantı kurulana kadar bekle (maksimum 2 saniye)
                        var attempts = 0
                        while (!billingClient.isReady && attempts < 20) {
                            delay(100)
                            attempts++
                        }
                    }
                    
                    if (billingClient.isReady) {
                        Log.d(TAG, "BillingClient hazır, satın almalar sorgulanıyor...")
                        queryPurchases()
                    } else {
                        Log.w(TAG, "BillingClient bağlantısı zaman aşımına uğradı")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Arka planda premium durumu kontrol edilirken hata: ${e.message}", e)
                }
            }
            
            // Varsayılan olarak false döndür
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Premium kontrol hatası: ${e.message}", e)
            // Hata durumunda false döndür
            return false
        }
    }
    
    /**
     * Premium durumunun senkron olarak Shared Preferences'dan alınması.
     * Bu metod herhangi bir ağ isteği yapmaz, sadece yerel veriyi kontrol eder.
     * Bu metod UI thread'de güvenle çağrılabilir.
     * 
     * Premium durumu: Google Play satın alma VEYA Firebase kod aktivasyonu
     */
    fun isPremiumSync(): Boolean {
        try {
            // ** Düzeltme: Debug modunu kontrol et **
            if (DEBUG_MODE) {
                Log.w(TAG, "DEBUG MODU (Sync): Premium durumu ${if (DEBUG_PREMIUM) "AÇIK" else "KAPALI"} olarak simüle ediliyor!")
                return DEBUG_PREMIUM
            }
            
            // Önce cache kontrolü yap
            if (_premiumState.value) {
                return true
            }
            
            // 1. Google Play satın alma kontrolü
            val prefs = context.getSharedPreferences("premium_prefs", Context.MODE_PRIVATE)
            val hasGooglePlayPremium = prefs.getBoolean("is_premium", false)
            
            if (hasGooglePlayPremium) {
                return true
            }
            
            // 2. Firebase kod aktivasyon kontrolü
            val hasCodePremium = codeValidator.isCodePremium()
            
            if (hasCodePremium) {
                Log.d(TAG, "Premium durum (kod ile): true")
                return true
            }
            
            return false
        } catch (e: Exception) {
            Log.e(TAG, "Premium senkron kontrol hatası: ${e.message}", e)
            return false
        }
    }
    
    /**
     * Premium durumunu arka planda kontrol etmeye başlar.
     * Bu metod, aktivite oluşturulduğunda çağrılabilir.
     */
    fun checkPremiumStatusAsync() {
        scope.launch {
            try {
                Log.d(TAG, "Arka planda premium durumu kontrol ediliyor...")
                
                // Önce kod kontrolü yap ve StateFlow'u güncelle
                val isPremiumNow = isPremiumSync()
                if (_premiumState.value != isPremiumNow) {
                    _premiumState.value = isPremiumNow
                    Log.d(TAG, "✅ Premium state güncellendi: $isPremiumNow")
                }
                
                // Sonra Billing Client ile Google Play kontrolü yap
                if (!billingClient.isReady) {
                    connectToBillingService()
                } else {
                    queryPurchases()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Premium durumu arka planda kontrol edilirken hata: ${e.message}", e)
            }
        }
    }
    
    /**
     * Premium state'i hemen günceller (kod aktivasyonundan sonra).
     */
    fun refreshPremiumState() {
        val isPremiumNow = isPremiumSync()
        if (_premiumState.value != isPremiumNow) {
            _premiumState.value = isPremiumNow
            Log.d(TAG, "✅ Premium state manuel olarak güncellendi: $isPremiumNow")
        }
    }
    
    /**
     * Premium sürümü satın alma işlemini başlatır.
     * Bu metod, kullanıcı arayüzünden kolayca çağrılabilir.
     */
    fun buyPremium(activity: Activity) {
        Log.d(TAG, "🚀 Premium satın alma isteği alındı")
        
        // Önce temel kontrolleri yap
        try {
            // İnternet bağlantısı kontrolü
            val connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
            val activeNetwork = connectivityManager.activeNetworkInfo
            val isConnected = activeNetwork?.isConnectedOrConnecting == true
            
            if (!isConnected) {
                Log.e(TAG, "❌ İnternet bağlantısı yok")
                _purchaseStatus.value = PurchaseStatus.ServiceUnavailable
                return
            }
            
            // Google Play Store varlığı kontrolü
            val packageManager = activity.packageManager
            try {
                packageManager.getPackageInfo("com.android.vending", 0)
                Log.d(TAG, "✅ Google Play Store mevcut")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Google Play Store bulunamadı: ${e.message}")
                _purchaseStatus.value = PurchaseStatus.ServiceUnavailable
                return
            }
            
            // Google Play Services kontrolü
            try {
                val gmsPackage = packageManager.getPackageInfo("com.google.android.gms", 0)
                Log.d(TAG, "✅ Google Play Services mevcut, versiyon: ${gmsPackage.versionName}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Google Play Services bulunamadı: ${e.message}")
                _purchaseStatus.value = PurchaseStatus.ServiceUnavailable
                return
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Temel kontroller sırasında hata: ${e.message}", e)
            _purchaseStatus.value = PurchaseStatus.ServiceUnavailable
            return
        }
        
        if (!billingClient.isReady) {
            Log.e(TAG, "❌ BillingClient hazır değil! Bağlantı kuruluyor...")
            _purchaseStatus.value = PurchaseStatus.ServiceUnavailable
            
            // Bağlantı kurulumu için özel bir callback ekleyin
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    Log.d(TAG, "🔄 Yeniden bağlantı sonucu: ${billingResult.responseCode}, ${billingResult.debugMessage}")
                    
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "✅ BillingClient bağlandı, satın alma akışı başlatılıyor...")
                        launchPurchaseFlow(activity)
                    } else {
                        Log.e(TAG, "❌ BillingClient bağlantı hatası: ${billingResult.responseCode}, ${billingResult.debugMessage}")
                        
                        // Hata koduna göre özel mesajlar
                        when (billingResult.responseCode) {
                            BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                                Log.e(TAG, "🔧 Billing servisi bu cihazda desteklenmiyor")
                            }
                            BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                                Log.e(TAG, "🔧 Google Play Store servisi şu anda kullanılamıyor")
                            }
                            BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                                Log.e(TAG, "🔧 Geliştirici hatası - Uygulama imzası veya konfigürasyon sorunu")
                            }
                        }
                        
                        _purchaseStatus.postValue(PurchaseStatus.ServiceUnavailable)
                    }
                }
                
                override fun onBillingServiceDisconnected() {
                    Log.e(TAG, "❌ Satın alma sırasında bağlantı kesildi")
                    _purchaseStatus.postValue(PurchaseStatus.ServiceUnavailable)
                }
            })
            return
        }
        
        launchPurchaseFlow(activity)
    }
    
    /**
     * Premium durumunu sıfırlar (test için)
     * Bu metod yalnızca geliştirici seçeneklerinden çağrılmalıdır.
     */
    fun resetPremiumStatus() {
        try {
            _premiumState.value = false
            savePremiumStatus(false)
            Log.w(TAG, "Premium durumu sıfırlandı (geliştirici tarafından)")
        } catch (e: Exception) {
            Log.e(TAG, "Premium durumu sıfırlanırken hata: ${e.message}", e)
        }
    }
    
    /**
     * Premium fonksiyonunu kullanan her yerde kullanılabilecek
     * yardımcı bir metot. Hem PurchaseManager'ın başarısız olduğu
     * durumları ele alır hem de test modunu kontrol eder.
     * 
     * @param onPremium Premium durumunda çalışacak kod bloğu
     * @param onNonPremium Premium olmayan durumda çalışacak kod bloğu
     * @param onError Hata durumunda çalışacak kod bloğu
     */
    fun withPremiumCheck(
        onPremium: () -> Unit,
        onNonPremium: () -> Unit,
        onError: ((Exception) -> Unit)? = null
    ) {
        try {
            if (DEBUG_MODE) {
                if (DEBUG_PREMIUM) {
                    Log.w(TAG, "DEBUG MODU: Premium içerik erişiliyor (simülasyon)")
                    onPremium()
                } else {
                    Log.w(TAG, "DEBUG MODU: Premium içerik erişimi reddedildi (simülasyon)")
                    onNonPremium()
                }
                return
            }
            
            val isPremium = isPremium()
            if (isPremium) {
                onPremium()
            } else {
                onNonPremium()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Premium kontrol sırasında hata: ${e.message}", e)
            onError?.invoke(e) ?: onNonPremium()
        }
    }
    
    /**
     * Premium satın alma akışını başlatır.
     */
    fun launchPurchaseFlow(activity: Activity) {
        Log.d(TAG, "🚀 Premium satın alma akışı başlatılıyor...")
        
        if (!billingClient.isReady) {
            Log.e(TAG, "❌ BillingClient hazır değil! Durum kontrol ediliyor...")
            _purchaseStatus.value = PurchaseStatus.ServiceUnavailable
            
            // Bağlantı durumunu kontrol et ve yeniden bağlan
            scope.launch {
                try {
                    Log.d(TAG, "🔄 BillingClient yeniden bağlanıyor...")
                    connectToBillingService()
                    
                    // 3 saniye bekle ve tekrar dene
                    delay(3000)
                    if (billingClient.isReady) {
                        Log.d(TAG, "✅ BillingClient hazır, satın alma akışı tekrar başlatılıyor...")
                        launchPurchaseFlow(activity)
                    } else {
                        Log.e(TAG, "❌ BillingClient 3 saniye sonra hala hazır değil")
                        _purchaseStatus.postValue(PurchaseStatus.ServiceUnavailable)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ BillingClient yeniden bağlantı hatası: ${e.message}", e)
                    _purchaseStatus.postValue(PurchaseStatus.ServiceUnavailable)
                }
            }
            return
        }
        
        // Zaten premium ise işlemi durdur
        if (_premiumState.value) {
            Log.d(TAG, "ℹ️ Kullanıcı zaten premium sürüme sahip")
            _purchaseStatus.value = PurchaseStatus.AlreadyOwned
            return
        }
        
        Log.d(TAG, "🔍 Ürün detayları sorgulanıyor: $PREMIUM_SKU")
        
        // Ürün detaylarını sorgula
        val queryProductDetailsParams = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(PREMIUM_SKU)
                        .setProductType(BillingClient.ProductType.INAPP)
                        .build()
                )
            )
            .build()
        
        billingClient.queryProductDetailsAsync(queryProductDetailsParams) { billingResult, queryProductDetailsResult ->
            val responseCode = billingResult.responseCode
            val debugMessage = billingResult.debugMessage
            val productDetailsList = queryProductDetailsResult.productDetailsList ?: emptyList()
            
            Log.d(TAG, "📋 Ürün detayları sonucu:")
            Log.d(TAG, "   - Response Code: $responseCode")
            Log.d(TAG, "   - Debug Message: $debugMessage")
            Log.d(TAG, "   - Ürün sayısı: ${productDetailsList.size}")
            
            when (responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    if (productDetailsList.isNotEmpty()) {
                        // Ürün detayları bulundu, satın alma akışını başlat
                        val productDetails = productDetailsList.first()
                        
                        Log.d(TAG, "✅ Ürün detayları bulundu:")
                        Log.d(TAG, "   - İsim: ${productDetails.name}")
                        Log.d(TAG, "   - Açıklama: ${productDetails.description}")
                        Log.d(TAG, "   - Fiyat: ${productDetails.oneTimePurchaseOfferDetails?.formattedPrice}")
                        
                        try {
                            // Ürünü satın alma akışını başlat
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(
                                    listOf(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                            .setProductDetails(productDetails)
                                            .build()
                                    )
                                )
                                .build()
                            
                            Log.d(TAG, "🛒 Google Play satın alma ekranı açılıyor...")
                            val launchResult = billingClient.launchBillingFlow(activity, billingFlowParams)
                            
                            if (launchResult.responseCode != BillingClient.BillingResponseCode.OK) {
                                Log.e(TAG, "❌ Satın alma akışı başlatılamadı:")
                                Log.e(TAG, "   - Response Code: ${launchResult.responseCode}")
                                Log.e(TAG, "   - Debug Message: ${launchResult.debugMessage}")
                                _purchaseStatus.value = PurchaseStatus.Failed(launchResult.responseCode)
                            } else {
                                Log.d(TAG, "✅ Satın alma akışı başarıyla başlatıldı")
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "❌ Satın alma akışı başlatılırken exception oluştu: ${e.message}", e)
                            _purchaseStatus.value = PurchaseStatus.Failed(-1)
                        }
                    } else {
                        Log.e(TAG, "❌ KRİTİK HATA: Ürün detayları bulunamadı!")
                        Log.e(TAG, "🔧 Çözüm adımları:")
                        Log.e(TAG, "   1. Google Play Console'da '$PREMIUM_SKU' ürününü oluşturun")
                        Log.e(TAG, "   2. Ürünü 'Active' duruma getirin")
                        Log.e(TAG, "   3. Ürün ID'sinin tam olarak '$PREMIUM_SKU' olduğundan emin olun")
                        _purchaseStatus.value = PurchaseStatus.ProductNotFound
                    }
                }
                BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
                    Log.e(TAG, "❌ Billing servisi bu cihazda kullanılamıyor")
                    _purchaseStatus.value = PurchaseStatus.ServiceUnavailable
                }
                BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
                    Log.e(TAG, "❌ Google Play Store servisi şu anda kullanılamıyor")
                    _purchaseStatus.value = PurchaseStatus.ServiceUnavailable
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    Log.d(TAG, "ℹ️ Kullanıcı işlemi iptal etti")
                    _purchaseStatus.value = PurchaseStatus.Canceled
                }
                BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
                    Log.e(TAG, "❌ Ürün mevcut değil - Google Play Console'da kontrol edin")
                    _purchaseStatus.value = PurchaseStatus.ProductNotFound
                }
                BillingClient.BillingResponseCode.DEVELOPER_ERROR -> {
                    Log.e(TAG, "❌ DEVELOPER ERROR - Geliştirici hatası:")
                    Log.e(TAG, "   - Debug Message: $debugMessage")
                    Log.e(TAG, "🔧 Kontrol edilecekler:")
                    Log.e(TAG, "   1. Ürün ID doğru mu: '$PREMIUM_SKU'")
                    Log.e(TAG, "   2. Uygulama imzası Google Play Console ile uyumlu mu")
                    Log.e(TAG, "   3. Test hesabı düzgün yapılandırılmış mı")
                    _purchaseStatus.value = PurchaseStatus.Failed(responseCode)
                }
                else -> {
                    Log.e(TAG, "❌ Bilinmeyen ürün sorgulama hatası:")
                    Log.e(TAG, "   - Response Code: $responseCode")
                    Log.e(TAG, "   - Debug Message: $debugMessage")
                    _purchaseStatus.value = PurchaseStatus.Failed(responseCode)
                }
            }
        }
    }
} 