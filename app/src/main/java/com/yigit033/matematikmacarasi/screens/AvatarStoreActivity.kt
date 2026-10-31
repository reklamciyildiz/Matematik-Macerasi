package com.yigit033.matematikmacarasi.screens

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.models.Avatar
import com.yigit033.matematikmacarasi.services.AvatarService
import com.yigit033.matematikmacarasi.services.EventBus
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import android.util.Log
import android.widget.Toast
import com.airbnb.lottie.LottieDrawable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.yigit033.matematikmacarasi.components.AnimatedAvatarView
import com.yigit033.matematikmacarasi.models.AvatarAnimationType
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.yigit033.matematikmacarasi.services.StarManager
import kotlinx.coroutines.delay
import com.yigit033.matematikmacarasi.utils.NetworkUtils
import com.yigit033.matematikmacarasi.services.payments.AdManager
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import com.yigit033.matematikmacarasi.services.DailyPremiumBonusService
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontStyle

class AvatarStoreActivity : ComponentActivity() {

    private lateinit var avatarService: AvatarService
    private var avatarChanged = false
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    
    // Reklam Yöneticisi - Aktivite seviyesinde tanımla
    private var adManager: com.yigit033.matematikmacarasi.services.payments.AdManager? = null
    
    // Satın alma Yöneticisi - Aktivite seviyesinde tanımla
    private var purchaseManager: com.yigit033.matematikmacarasi.services.payments.PurchaseManager? = null
    
    // Log yapan ama aktiviteyi sonlandırmayan exception handler
    private val uncaughtExceptionHandler = Thread.UncaughtExceptionHandler { thread, throwable ->
        Log.e("AvatarStoreActivity", "Beklenmeyen hata: ${throwable.message}", throwable)
        // Sadece log, activity'yi sonlandırmayacağız
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Global hata yakalayıcıyı sadece bu aktivite için ayarla, sistem genelinde değil
        // Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)
        
        // Avatar servisini başlat
        avatarService = AvatarService(this)
        
        // AdManager'ı aktivite seviyesinde başlat
        try {
            Log.d("AvatarStoreActivity", "AdManager başlatılıyor...")
            adManager = com.yigit033.matematikmacarasi.services.payments.AdManager.getInstance(applicationContext)
            Log.d("AvatarStoreActivity", "AdManager başarıyla başlatıldı: ${adManager != null}")
        } catch (e: Exception) {
            Log.e("AvatarStoreActivity", "AdManager başlatma hatası: ${e.message}", e)
        }
        
        // PurchaseManager'ı aktivite seviyesinde başlat
        try {
            Log.d("AvatarStoreActivity", "PurchaseManager başlatılıyor...")
            purchaseManager = com.yigit033.matematikmacarasi.services.payments.PurchaseManager.getInstance(this)
            Log.d("AvatarStoreActivity", "PurchaseManager başarıyla başlatıldı: ${purchaseManager != null}")
        } catch (e: Exception) {
            Log.e("AvatarStoreActivity", "PurchaseManager başlatma hatası: ${e.message}", e)
        }
        
        setContent {
            MatematikMacerasiTheme {
                AvatarStoreScreen(
                    avatarService = avatarService,
                    adManager = adManager, // AdManager'ı Composable'a geçir
                    purchaseManager = purchaseManager, // PurchaseManager'ı Composable'a geçir
                    onBackClick = {
                        navigateBack()
                    },
                    onAvatarChanged = { avatarId ->
                        // Avatar değişikliği bayrak değişkenini işaretle
                        avatarChanged = true
                        Log.d("AvatarStoreActivity", "Avatar değişikliği işaretlendi - Avatar ID: $avatarId")
                        
                        // Event Bus üzerinden avatar değişikliği olayını yayınla
                        coroutineScope.launch {
                            try {
                                EventBus.publishAvatarChanged(avatarId)
                            } catch (e: Exception) {
                                Log.e("AvatarStoreActivity", "Avatar değişikliği yayınlanırken hata: ${e.message}", e)
                            }
                        }
                    }
                )
            }
        }
    }
    
    // Aktivite yaşam döngüsü yönetimi
    override fun onStart() {
        super.onStart()
        // EventBus'a kayıt olma işlemi buraya eklenebilir (gerekirse)
    }
    
    override fun onStop() {
        // EventBus'tan kayıt silme işlemi buraya eklenebilir (gerekirse)
        super.onStop()
    }
    
    override fun onPause() {
        super.onPause()
        // Animasyonları durdur ve kaynakları serbest bırak
        try {
            // Lottie önbelleğini doğrudan temizleyemiyoruz, GC'ye yardımcı olalım
            System.gc()
        } catch (e: Exception) {
            Log.e("AvatarStoreActivity", "Bellek temizlenirken hata: ${e.message}", e)
        }
    }
    
    override fun onDestroy() {
        // Tüm kaynakları temizle
        try {
            // Lottie önbelleğini doğrudan temizleyemiyoruz, GC'ye yardımcı olalım
            System.gc()
        } catch (e: Exception) {
            Log.e("AvatarStoreActivity", "Bellek temizlenirken hata: ${e.message}", e)
        }
        
        // Coroutine scope'u iptal et
        coroutineScope.launch {
            try {
                // Son avatar durumunu güncelle
                if (avatarChanged) {
                    val currentAvatarId = avatarService.getCurrentAvatar().id
                    // Force güncelleme yaparak tüm ekranların avatarı güncellemesini sağla
                    EventBus.forceAvatarNotification(currentAvatarId)
                    Log.d("AvatarStoreActivity", "onDestroy sırasında avatar son kez bildirildi: $currentAvatarId")
                }
            } catch (e: Exception) {
                Log.e("AvatarStoreActivity", "onDestroy'da EventBus güncellenirken hata: ${e.message}", e)
            }
        }
        
        super.onDestroy()
    }
    
    // Geri tuşuna basıldığında güvenli çıkış
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        try {
            // Sonucu ayarla
            if (avatarChanged) {
                setResult(Activity.RESULT_OK)
                Log.d("AvatarStoreActivity", "Avatar değişikliği sonucu gönderildi")
            } else {
                setResult(Activity.RESULT_CANCELED)
            }
            
            // Çıkmadan önce yıldız sayısını senkronize et
            coroutineScope.launch {
                try {
                    // Profil ve ana sayfa arasında yıldız ve avatar bilgilerini senkronize et
                    EventBus.syncProfileToHomePage(this@AvatarStoreActivity)
                    Log.d("AvatarStoreActivity", "Ana sayfaya dönmeden önce yıldız ve avatar verileri senkronize edildi")
                    
                    // Kısa bir gecikme ekle
                    delay(100)
                    
                    // Normal şekilde sonlandır
                    withContext(Dispatchers.Main) {
                        super@AvatarStoreActivity.onBackPressed()
                    }
                } catch (e: Exception) {
                    Log.e("AvatarStoreActivity", "Yıldız senkronizasyonu hatası: ${e.message}", e)
                    // Hata durumunda yine de çık
                    super@AvatarStoreActivity.onBackPressed()
                }
            }
        } catch (e: Exception) {
            Log.e("AvatarStoreActivity", "onBackPressed hatası: ${e.message}", e)
            finish() // En kötü durumda normal finish() ile çık
        }
    }
    
    // Güvenli geri dönüş metodu
    private fun navigateBack() {
        try {
            // Sonucu ayarla
            if (avatarChanged) {
                setResult(Activity.RESULT_OK)
                Log.d("AvatarStoreActivity", "Avatar değişikliği sonucu gönderildi")
            } else {
                setResult(Activity.RESULT_CANCELED)
            }
            
            // Normal şekilde sonlandır
            finish()
        } catch (e: Exception) {
            Log.e("AvatarStoreActivity", "navigateBack hatası: ${e.message}", e)
            finish() // En kötü durumda normal finish() ile çık
        }
    }
}

@Composable
fun AvatarStoreScreen(
    avatarService: AvatarService,
    adManager: com.yigit033.matematikmacarasi.services.payments.AdManager?,
    purchaseManager: com.yigit033.matematikmacarasi.services.payments.PurchaseManager?,
    onBackClick: () -> Unit,
    onAvatarChanged: (Int) -> Unit
) {
    val avatars = remember { avatarService.avatars }
    val currentAvatarId = remember { mutableStateOf(avatarService.getCurrentAvatar().id) }
    
    // Yıldız sayısını izlemek için bir mutableState
    val context = LocalContext.current
    val starManager = remember { StarManager.getInstance(context) }
    
    // Direkt yıldız sayısını tutan state ve bunu güncellemek için fonksiyon
    var userStars by remember { mutableStateOf(starManager.getDisplayStars()) }
    
    // Yıldız sayısını manuel olarak güncelleme fonksiyonu
    val refreshStars: () -> Unit = {
        val currentStars = starManager.getDisplayStars()
        Log.d("AvatarStoreScreen", "Yıldız sayısı yenileniyor: $currentStars")
        userStars = currentStars
    }
    
    // Coroutine scope for periodic refreshes
    val coroutineScope = rememberCoroutineScope()
    
    // Hata durumlarını izlemek için
    val contextErrorMsg = remember { mutableStateOf<String?>(null) }
    
    // Premium durumunu kontrol et
    var isPremium by remember { mutableStateOf(false) }
    var purchaseManager: com.yigit033.matematikmacarasi.services.payments.PurchaseManager? = null
    
    // Premium bonus servisi
    val premiumBonusService = remember { DailyPremiumBonusService.getInstance(context) }
    val dailyBonusAmount = remember { premiumBonusService.getDailyBonusAmount() }
    val hasTodaysBonusClaimed = remember(isPremium) { 
        if (isPremium) premiumBonusService.hasTodaysBonusBeenClaimed() else false 
    }
    
    // Premium kullanıcılar için otomatik bonus kontrolü
    LaunchedEffect(isPremium) {
        if (isPremium) {
            try {
                val bonusGiven = premiumBonusService.checkAndClaimAutomaticBonus()
                if (bonusGiven) {
                    // Bonus verildi, yıldız sayısını güncelle
                    val updatedStars = starManager.getDisplayStars()
                    userStars = updatedStars
                    
                    Toast.makeText(
                        context,
                        "Premium günlük bonus: ${premiumBonusService.getDailyBonusAmount()} yıldız kazandınız! 🎁",
                        Toast.LENGTH_LONG
                    ).show()
                    
                    Log.d("AvatarStoreScreen", "Premium günlük bonus verildi: ${premiumBonusService.getDailyBonusAmount()} yıldız")
                }
            } catch (e: Exception) {
                Log.e("AvatarStoreScreen", "Premium bonus kontrol hatası: ${e.message}", e)
            }
        }
    }
    
    // KRİTİK: Ekran açıldığında ve her satın alma işleminden sonra yıldız sayısını güncelle
    LaunchedEffect(Unit) {
        try {
            // Güncel yıldız sayısını yükle
            val currentStars = starManager.getDisplayStars()
            userStars = currentStars
            
            // EventBus üzerinden de güncelleyelim
            EventBus.forceStarNotification(currentStars)
            
            // KRİTİK: Periyodik olarak SharedPreferences'dan direkt okuma yap
            coroutineScope.launch {
                while(true) {
                    delay(500) // Yarım saniyede bir güncel yıldız sayısını kontrol et
                    val latestStars = context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE)
                        .getInt("display_stars", 0)
                    if (latestStars != userStars) {
                        Log.d("AvatarStoreScreen", "DOĞRUDAN ŞPREFERENCE GÜNCELLEMESİ - Eski: $userStars, Yeni: $latestStars")
                        userStars = latestStars
                    }
                }
            }
            
            Log.d("AvatarStoreScreen", "Mağaza ekranı başlatıldı, güncel yıldızlar: $currentStars")
        } catch (e: Exception) {
            Log.e("AvatarStoreScreen", "Yıldız sayısını alırken hata: ${e.message}", e)
        }
    }
    
    // EventBus ile yıldız güncellemelerini dinle
    LaunchedEffect(Unit) {
        try {
            EventBus.starUpdatedEvents.collect { event ->
                Log.d("AvatarStoreScreen", "EventBus üzerinden yıldız güncellemesi alındı: ${event.stars}")
                userStars = event.stars
            }
        } catch (e: Exception) {
            Log.e("AvatarStoreScreen", "Yıldız değişiklikleri izlenirken hata: ${e.message}", e)
        }
    }
    
    // Listen to star updates from currentStars StateFlow
    LaunchedEffect(Unit) {
        try {
            EventBus.currentStars.collect { stars ->
                Log.d("AvatarStoreScreen", "EventBus StateFlow üzerinden yıldız güncellemesi alındı: $stars")
                if (userStars != stars) {
                    userStars = stars
                }
            }
        } catch (e: Exception) {
            Log.e("AvatarStoreScreen", "StateFlow yıldız değişiklikleri izlenirken hata: ${e.message}", e)
        }
    }
    
    // EventBus üzerinden gelen avatar değişimlerini dinle
    LaunchedEffect(Unit) {
        EventBus.avatarChangedEvents.collect { event ->
            Log.d("AvatarStoreScreen", "EventBus'tan avatar değişikliği alındı: ${event.avatarId}")
            currentAvatarId.value = event.avatarId
        }
    }
    
    // PurchaseManager'ı güvenli bir şekilde başlat ve premium durumunu kontrol et
    LaunchedEffect(Unit) {
        try {
            // Önce Firebase'den senkronize et (uygulama silme-yükleme durumu için)
            val codeValidator = com.yigit033.matematikmacarasi.services.PremiumCodeValidator.getInstance(context)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val synced = codeValidator.checkAndSyncPremiumStatus()
                if (synced) {
                    Log.d("AvatarStoreScreen", "✅ Firebase'den premium durumu senkronize edildi!")
                }
            }
            
            purchaseManager = com.yigit033.matematikmacarasi.services.payments.PurchaseManager.getInstance(context)
            isPremium = purchaseManager?.isPremiumSync() ?: false
            Log.d("AvatarStoreScreen", "Premium durumu kontrol edildi: $isPremium")
            
            purchaseManager?.checkPremiumStatusAsync()
            
            launch {
                purchaseManager?.premiumState?.collect { premiumState ->
                    Log.d("AvatarStoreScreen", "Premium durumu güncellendi: $premiumState")
                    isPremium = premiumState
                }
            }
        } catch (e: Exception) {
            Log.e("AvatarStoreScreen", "PurchaseManager başlatılamadı: ${e.message}", e)
        }
    }
    
    // Avatar satın alma işlemi tamamlandığında yıldız sayısını hemen yenile
    val unlockAvatar: (Int) -> Unit = { avatarId ->
        try {
            Log.d("AvatarStoreScreen", "Avatar kilidi açma işlemi başlatılıyor: $avatarId")
            val unlockSuccess = avatarService.unlockAvatar(avatarId)
            
            if (unlockSuccess) {
                Log.d("AvatarStoreScreen", "Avatar kilidi başarıyla açıldı: $avatarId")
                Toast.makeText(context, "Avatar başarıyla açıldı!", Toast.LENGTH_SHORT).show()
                
                // Yeni avatarı otomatik olarak seç
                avatarService.setCurrentAvatar(avatarId)
                onAvatarChanged(avatarId)
                
                // KRİTİK: Yıldız sayısını hemen güncelle
                coroutineScope.launch {
                    delay(150) // Kısa bir bekleme
                    val updatedStars = starManager.getDisplayStars()
                    Log.d("AvatarStoreScreen", "Avatar kilidi açıldıktan sonra yıldız güncellemesi: $updatedStars")
                    userStars = updatedStars
                    
                    // EventBus'a da bildir
                    EventBus.forceStarNotification(updatedStars)
                }
            } else {
                Log.e("AvatarStoreScreen", "Avatar kilidi açılamadı: $avatarId")
                Toast.makeText(context, "Avatar kilidi açılamadı, lütfen tekrar deneyin.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("AvatarStoreScreen", "Avatar kilidi açma hatası: ${e.message}", e)
            Toast.makeText(context, "Beklenmeyen bir hata oluştu.", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Popup durumları
    var showNotEnoughStarsDialog by remember { mutableStateOf(false) }
    var showPurchaseConfirmDialog by remember { mutableStateOf(false) }
    var showPremiumRequiredDialog by remember { mutableStateOf(false) }
    var selectedAvatarForInfo by remember { mutableStateOf<Avatar?>(null) }
    var avatarNeedingPremium by remember { mutableStateOf<Avatar?>(null) }
    
    // Reklam yükleme durumu
    var isAdLoading by remember { mutableStateOf(false) }
    
    // Hata mesajlarını göstermek için
    if (contextErrorMsg.value != null) {
        LaunchedEffect(contextErrorMsg.value) {
            Toast.makeText(context, contextErrorMsg.value, Toast.LENGTH_SHORT).show()
            contextErrorMsg.value = null // Gösterildikten sonra temizle
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Gradyan arka plan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF800080), // Mor (uygulamanın ana tema rengi - üst)
                            Color(0xFF5D35A0), // Mor-Mavi geçiş
                            Color(0xFF4169E1)  // Mavi (alt)
                        )
                    )
                )
        )
        
        // Yıldız efektleri yerine basit bir dekoratif element kullanalım
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Üst köşede dekoratif element
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.TopEnd)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.2f),
                                Color.Transparent
                            )
                        )
                    )
            )
            
            // Alt köşede dekoratif element
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomStart)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.15f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Toolbar
            TopAppBar(
                title = { 
                    Text(
                        "Avatar Mağazası",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color.White
                        )
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                contentColor = Color.White,
                actions = {
                    // Yıldız sayısı
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Yıldızlar",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$userStars",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            )
            
            // Başlık
            Text(
                text = "Avatar Seçin",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(vertical = 8.dp),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = Color.White
            )
            
            // Avatar grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 16.dp)
            ) {
                items(avatars) { avatar ->
                        AvatarItem(
                            avatar = avatar,
                            isSelected = avatar.id == currentAvatarId.value,
                            onClick = {
                            try {
                                Log.d("AvatarStoreScreen", "Avatar tıklaması: ID=${avatar.id}, İsim=${avatar.name}, Açık=${avatar.isUnlocked}")
                                
                                // Avatar kilidini aç veya seç
                                if (avatar.isUnlocked) {
                                    // Eğer avatar zaten açıksa, seçimi güncelle
                                        avatarService.setCurrentAvatar(avatar.id)
                                        onAvatarChanged(avatar.id)
                                    // Kullanıcıya bildir
                                    Toast.makeText(context, "${avatar.name} seçildi", Toast.LENGTH_SHORT).show()
                                } else {
                                    // Satın alma için detay kontrolünü yap
                                    selectedAvatarForInfo = avatar
                                    
                                    // Premium kontrolü ve yıldız kontrolü yap
                                    val canUnlock = avatarService.canUnlockAvatar(avatar.id, userStars, isPremium)
                                    Log.d("AvatarStoreScreen", "Avatar ${avatar.id} kilidi açılabilir mi: $canUnlock")
                                    
                                    if (!canUnlock) {
                                        // Premium kontrol et
                                        if (avatar.id > 6 && !isPremium) {
                                            // Premium gerekli, ilgili dialogi göster
                                            avatarNeedingPremium = avatar
                                            showPremiumRequiredDialog = true
                                            Log.d("AvatarStoreScreen", "Premium gerekli dialog gösteriliyor: ${avatar.name}")
                                        } else {
                                            // Yeterli yıldız yok, yetersiz yıldız dialogunu göster
                                            showNotEnoughStarsDialog = true
                                            Log.d("AvatarStoreScreen", "Yetersiz yıldız dialog gösteriliyor: ${avatar.name}")
                                    }
                                } else {
                                        // Yeterli yıldız varsa, satın alma onayı göster
                                        showPurchaseConfirmDialog = true
                                        Log.d("AvatarStoreScreen", "Satın alma dialog gösteriliyor: ${avatar.name}")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e("AvatarStoreScreen", "Avatar tıklamasında hata: ${e.message}", e)
                                Toast.makeText(context, "İşlem gerçekleştirilemedi", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
        
        // Yetersiz yıldız diyaloğu
        if (showNotEnoughStarsDialog && selectedAvatarForInfo != null) {
            val remainingStars = selectedAvatarForInfo!!.price - userStars
            
            Dialog(onDismissRequest = { showNotEnoughStarsDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color.White,
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Başlık ve yıldız ikonu
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Yıldız",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Yetersiz Yıldız",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF5D35A0)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Avatar resmi - animasyonlu border ile
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFE8F5E9),
                                            Color(0xFFB2DFDB)
                                        )
                                    )
                                )
                                .border(
                                    width = 3.dp,
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFF5D35A0),
                                            Color(0xFF7E57C2),
                                            Color(0xFF9575CD),
                                            Color(0xFF5D35A0)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            // Animasyonlu Avatar Görünümü
                            AnimatedAvatarView(
                                avatar = selectedAvatarForInfo!!,
                                modifier = Modifier.size(100.dp),
                                contentDescription = selectedAvatarForInfo!!.name
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Avatar adı - öne çıkan tasarım
                        Text(
                            text = selectedAvatarForInfo!!.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D35A0),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Avatar açıklaması
                        Text(
                            text = selectedAvatarForInfo!!.description,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Gerekli ve mevcut yıldız bilgisi - görsel tasarım
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF5F5F5))
                                .padding(vertical = 16.dp, horizontal = 16.dp)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Yetersiz yıldız mesajı
                                Text(
                                    text = "Bu avatarı açmak için:",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF424242)
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // Mevcut yıldız
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Mevcut",
                                            fontSize = 14.sp,
                                            color = Color(0xFF757575)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Mevcut Yıldız",
                                                tint = Color(0xFFFFC107),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "$userStars",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (userStars < selectedAvatarForInfo!!.price) 
                                                    Color(0xFFE53935) else Color(0xFF43A047)
                                            )
                                        }
                                    }
                                    
                                    // Ayırıcı
                                    Text(
                                        text = " / ",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF757575),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    
                                    // Gerekli yıldız
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Gerekli",
                                            fontSize = 14.sp,
                                            color = Color(0xFF757575)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = "Gerekli Yıldız",
                                                tint = Color(0xFFFFC107),
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${selectedAvatarForInfo!!.price}",
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF5D35A0)
                                            )
                                        }
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Yetersiz yıldız mesajı
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFECB3))
                                        .padding(vertical = 12.dp, horizontal = 16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = "Bilgi",
                                            tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${remainingStars} yıldıza daha ihtiyacınız var",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF5D4037)
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Premium kullanıcılar için özel mesaj, premium olmayan kullanıcılar için reklam butonu
                        if (isPremium) {
                            // Premium kullanıcılar için özel mesaj
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFD700).copy(alpha = 0.2f)
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 4.dp
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_premium),
                                            contentDescription = "Premium",
                                            tint = Color(0xFFFFD700),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Premium Üye",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF5D35A0)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Her gün otomatik olarak $dailyBonusAmount yıldız bonus kazanıyorsunuz! 🎁",
                                        fontSize = 14.sp,
                                        color = Color(0xFF424242),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                    
                                    if (!hasTodaysBonusClaimed) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Bugünkü bonusunuz henüz alınmadı, uygulama açılışında otomatik verilecek.",
                                            fontSize = 12.sp,
                                            color = Color(0xFF666666),
                                            textAlign = TextAlign.Center,
                                            fontStyle = FontStyle.Italic
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "Bugünkü bonusunuz alındı! Yarın tekrar gelin. ✨",
                                            fontSize = 12.sp,
                                            color = Color(0xFF4CAF50),
                                            textAlign = TextAlign.Center,
                                            fontStyle = FontStyle.Italic
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Premium kullanıcılar için reklam izleme kartı
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFFFF3E0) // Açık turuncu arka plan
                                ),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                                border = BorderStroke(2.dp, Color(0xFFFF9800))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_star),
                                            contentDescription = "Reklam Yıldızı",
                                            tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Ekstra Yıldız Kazan",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE65100)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(8.dp))
                                    
                                    Text(
                                        text = "Günlük bonusuna ek; reklam izle ve 1 yıldız daha kazan! 🎬",
                                        fontSize = 14.sp,
                                        color = Color(0xFF424242),
                                        textAlign = TextAlign.Center,
                                        lineHeight = 20.sp
                                    )
                                    
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Reklam izleme butonu
                                    Button(
                                        onClick = {
                                            if (isAdLoading) {
                                                Toast.makeText(context, "Reklam yükleniyor, lütfen bekleyin...", Toast.LENGTH_SHORT).show()
                                                return@Button
                                            }
                                            
                                            isAdLoading = true
                                            Toast.makeText(context, "Reklam yükleniyor...", Toast.LENGTH_SHORT).show()
                                            
                                            try {
                                                // Context'i kontrol et
                                                val activity = context as? Activity
                                                if (activity == null || adManager == null) {
                                                    Log.e("AvatarStoreScreen", "Activity context veya AdManager bulunamadı")
                                                    isAdLoading = false
                                                    Toast.makeText(context, "Reklam gösterilemedi", Toast.LENGTH_SHORT).show()
                                                    return@Button
                                                }
                                                
                                                // Reklam gösterme fonksiyonunu çağır
                                                adManager.showRewardedAd(
                                                    activity = activity,
                                                    onRewarded = { rewardAmount ->
                                                        // Ödül verildi
                                                        Log.d("AvatarStoreScreen", "Premium kullanıcı $rewardAmount yıldız kazandı")
                                                        isAdLoading = false
                                                        
                                                        // Coroutine başlatarak suspend fonksiyonu çağır
                                                        CoroutineScope(Dispatchers.IO).launch {
                                                            try {
                                                                val success = starManager.addRewardedStars(rewardAmount)
                                                                if (success) {
                                                                    val updatedStars = starManager.getDisplayStars()
                                                                    
                                                                    withContext(Dispatchers.Main) {
                                                                        userStars = updatedStars
                                                                        
                                                                        Toast.makeText(
                                                                            context,
                                                                            "🎉 $rewardAmount yıldız kazandınız! Toplam: $updatedStars",
                                                                            Toast.LENGTH_LONG
                                                                        ).show()
                                                                        
                                                                        Log.d("AvatarStoreScreen", "Premium reklam ödülü başarıyla eklendi: $updatedStars toplam yıldız")
                                                                    }
                                                                } else {
                                                                    withContext(Dispatchers.Main) {
                                                                        Toast.makeText(
                                                                            context,
                                                                            "Yıldız eklenirken hata oluştu!",
                                                                            Toast.LENGTH_SHORT
                                                                        ).show()
                                                                        Log.e("AvatarStoreScreen", "Premium reklam ödülü eklenemedi")
                                                                    }
                                                                }
                                                            } catch (e: Exception) {
                                                                withContext(Dispatchers.Main) {
                                                                    isAdLoading = false
                                                                    Log.e("AvatarStoreScreen", "Premium reklam ödülü işleme hatası: ${e.message}", e)
                                                                    Toast.makeText(
                                                                        context,
                                                                        "Beklenmeyen hata: ${e.message}",
                                                                        Toast.LENGTH_SHORT
                                                                    ).show()
                                                                }
                                                            }
                                                        }
                                                    },
                                                    onAdNotAvailable = {
                                                        isAdLoading = false
                                                        Log.d("AvatarStoreScreen", "Premium kullanıcı için reklam bulunamadı")
                                                        Toast.makeText(
                                                            context,
                                                            "Şu anda reklam bulunamadı. Lütfen daha sonra tekrar deneyin.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                )
                                            } catch (e: Exception) {
                                                isAdLoading = false
                                                Log.e("AvatarStoreScreen", "Premium reklam gösterme işlemi başlatılamadı: ${e.message}", e)
                                                Toast.makeText(
                                                    context, 
                                                    "Reklam sistemi şu anda kullanılamıyor. Lütfen daha sonra tekrar deneyin.", 
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        enabled = !isAdLoading,
                                        colors = ButtonDefaults.buttonColors(
                                            backgroundColor = if (isAdLoading) Color.Gray else Color(0xFFFF9800),
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            if (isAdLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = Color.White
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "Hazırlanıyor...",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            } else {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_star),
                                                    contentDescription = "Reklam İzle",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = "İzle ve Kazan",
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Premium olmayan kullanıcılar için reklam butonu
                            Button(
                                onClick = {
                                    if (isAdLoading) {
                                        Toast.makeText(context, "Reklam yükleniyor, lütfen bekleyin...", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    
                                    isAdLoading = true
                                    Toast.makeText(context, "Reklam yükleniyor...", Toast.LENGTH_SHORT).show()
                                    
                                    try {
                                        // AdManager'ı kontrol et
                                        if (adManager == null) {
                                            Log.d("AvatarStoreScreen", "Reklam yöneticisi bulunamadı, test modu etkinleştiriliyor")
                                            // Test modu: AdManager yoksa direkt yıldız ekle
                                            CoroutineScope(Dispatchers.IO).launch {
                                                try {
                                                    // KRİTİK: Doğrudan SharedPreferences üzerinden mevcut değeri al
                                                    val currentDisplayStars = context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE)
                                                        .getInt("display_stars", 0)
                                                    val newStars = currentDisplayStars + 1 // 1 yıldız ekle
                                                    
                                                    // KRİTİK: Tüm yerlere aynı anda kaydet
                                                    val smp = context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE)
                                                    smp.edit().putInt("display_stars", newStars).commit()
                                                    
                                                    context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
                                                        .edit().putInt("display_stars", newStars).commit()
                                                    
                                                    val success = starManager.setDisplayStars(newStars)
                                                    
                                                    withContext(Dispatchers.Main) {
                                                        // UI'ı güncelle
                                                        userStars = newStars
                                                        
                                                        // Dialog'u kapat
                                                        showNotEnoughStarsDialog = false
                                                        
                                                        // Bilgi mesajı göster
                                                        Toast.makeText(context, "TEST MODU: 1 yıldız kazandınız! Yeni toplam: $newStars", 
                                                            Toast.LENGTH_SHORT).show()
                                                        
                                                        isAdLoading = false
                                                    }
                                                } catch (e: Exception) {
                                                    withContext(Dispatchers.Main) {
                                                        Log.e("AvatarStoreScreen", "Test modu yıldız ekleme hatası: ${e.message}", e)
                                                        isAdLoading = false
                                                        Toast.makeText(context, "Test: Yıldız eklenemedi", Toast.LENGTH_SHORT).show()
                                                    }
                                                }
                                            }
                                            return@Button
                                        }
                                        
                                        // Context'i kontrol et
                                        val activity = context as? Activity
                                        if (activity == null) {
                                            Log.e("AvatarStoreScreen", "Context Activity değil")
                                            isAdLoading = false
                                            Toast.makeText(context, "Reklam gösterilemiyor (Context hatası)", Toast.LENGTH_SHORT).show()
                                            return@Button
                                        }
                                        
                                        // Reklam gösterme fonksiyonunu çağır
                                        adManager.showRewardedAd(
                                            activity = activity,
                                            onRewarded = { rewardAmount ->
                                                // Ödül verildi
                                                Log.d("AvatarStoreScreen", "$rewardAmount yıldız ekleniyor")
                                                isAdLoading = false
                                                
                                                // Coroutine başlatarak suspend fonksiyonu çağır
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    try {
                                                        // KRİTİK: Doğrudan SharedPreferences üzerinden mevcut değeri al
                                                        val currentDisplayStars = context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE)
                                                            .getInt("display_stars", 0)
                                                        val newStars = currentDisplayStars + rewardAmount // yıldız ekle
                                                        
                                                        // KRİTİK: Tüm yerlere aynı anda kaydet
                                                        val smp = context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE)
                                                        smp.edit().putInt("display_stars", newStars).commit()
                                                        
                                                        context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
                                                            .edit().putInt("display_stars", newStars).commit()
                                                        
                                                        // StarManager'a da bildir
                                                        starManager.setDisplayStars(newStars)
                                                        
                                                        // UI güncellemeleri Main thread'e geri dönmeli
                                                        withContext(Dispatchers.Main) {
                                                            // Dialog'u kapat
                                                            showNotEnoughStarsDialog = false
                                                            
                                                            // DOĞRUDAN UI DEĞİŞKENİNİ GÜNCELLE!
                                                            userStars = newStars
                                                            
                                                            // EventBus'a bildir
                                                            try {
                                                                CoroutineScope(Dispatchers.Main).launch {
                                                                    EventBus.forceStarNotification(newStars)
                                                                }
                                                            } catch (e: Exception) {
                                                                Log.e("AvatarStoreScreen", "EventBus bildirimi hatası: ${e.message}", e)
                                                            }
                                                            
                                                            Toast.makeText(
                                                                context,
                                                                "$rewardAmount yıldız kazandınız! Yeni toplam: $newStars",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    } catch (e: Exception) {
                                                        // Hata yönetimi - Main thread'e geri dön
                                                        withContext(Dispatchers.Main) {
                                                            isAdLoading = false
                                                            Log.e("AvatarStoreScreen", "Yıldız ekleme hatası: ${e.message}", e)
                                                            Toast.makeText(
                                                                context,
                                                                "Yıldız eklenirken bir sorun oluştu",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    }
                                                }
                                            },
                                            onAdNotAvailable = {
                                                // Reklam gösterilemedi - Production için uygun mesaj göster
                                                isAdLoading = false
                                                Log.d("AvatarStoreScreen", "Ödüllü reklam gösterilemedi")
                                                
                                                // Production'da test modu YOK - Kullanıcıya uygun mesaj göster
                                                CoroutineScope(Dispatchers.Main).launch {
                                                    try {
                                                        // Dialog'u kapat
                                                        showNotEnoughStarsDialog = false
                                                        
                                                        // Kullanıcıya bilgilendirici mesaj göster
                                                        Toast.makeText(
                                                            context,
                                                            "Reklam şu anda kullanılamıyor. Lütfen internet bağlantınızı kontrol edin ve daha sonra tekrar deneyin.",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    } catch (e: Exception) {
                                                        Log.e("AvatarStoreScreen", "Reklam hata mesajı gösterilirken hata: ${e.message}", e)
                                                    }
                                                }
                                            }
                                        )
                                    } catch (e: Exception) {
                                        isAdLoading = false
                                        Log.e("AvatarStoreScreen", "Reklam gösterme işlemi başlatılamadı: ${e.message}", e)
                                        
                                        // Hata durumunda da test modu YOK - Kullanıcıya uygun mesaj göster
                                        CoroutineScope(Dispatchers.Main).launch {
                                            try {
                                                // Dialog'u kapat
                                                showNotEnoughStarsDialog = false
                                                
                                                // Kullanıcıya hata mesajı göster
                                                Toast.makeText(
                                                    context, 
                                                    "Reklam sistemi şu anda kullanılamıyor. Lütfen uygulamayı yeniden başlatın veya daha sonra tekrar deneyin.", 
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            } catch (e2: Exception) {
                                                Log.e("AvatarStoreScreen", "Hata mesajı gösterilirken hata: ${e2.message}", e2)
                                            }
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                enabled = !isAdLoading, // Yükleme sırasında butonu devre dışı bırak
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (isAdLoading) Color.Gray else Color(0xFFFFC107),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isAdLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Reklam Hazırlanıyor...",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    } else {
                                        Icon(
                                            painter = painterResource(id = R.drawable.ic_star), // Mevcut yıldız ikonunu kullan
                                            contentDescription = "Reklam İzle",
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Reklam İzle ve 1 Yıldız Kazan",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Vazgeç butonu (Outlined stili ile)
                        OutlinedButton(
                            onClick = { showNotEnoughStarsDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            border = BorderStroke(1.dp, Color(0xFF5D35A0)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                backgroundColor = Color.White,
                                contentColor = Color(0xFF5D35A0)
                            )
                        ) {
                            Text(
                                text = "Vazgeç",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
        
        // Premium gerekli diyaloğu
        if (showPremiumRequiredDialog) {
            Dialog(onDismissRequest = { showPremiumRequiredDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .fillMaxHeight(0.8f)
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    backgroundColor = Color.White,
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Premium Başlık
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_premium),
                                contentDescription = "Premium",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Premium Gerekli",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = Color(0xFF5D35A0)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Avatar bilgisi
                        Text(
                            text = "${avatarNeedingPremium?.name ?: "Bu avatar"} açmak için Premium üyelik gerekiyor.",
                            color = Color(0xFF424242),
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Ayırıcı
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            color = Color(0xFFEEEEEE),
                            thickness = 1.dp
                        )
                        
                        // Kaydırılabilir Premium İçerik Alanı
                        val scrollState = rememberScrollState()
                        
                        // Scroll durumuna göre gösterge ikonunun görünürlüğünü hesapla
                        val scrollIndicatorVisible = scrollState.value < scrollState.maxValue && !scrollState.isScrollInProgress
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                                    .padding(horizontal = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Premium Başlığı
                                Text(
                                    text = "🎮 Matematik Kahramanlarının Abisi Burada! 🎮",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = Color(0xFF5D35A0),
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Tanıtım İçeriği - Görsel Olarak Daha Çekici
                                Text(
                                    text = "Selam küçük kahramanlar! 👋\n" +
                                          "Sizler için hazırladığım bu özel dünyada, Premium'a geçerek matematik serüveninize süper güçler katabilirsiniz! 🚀",
                                    fontSize = 15.sp,
                                    color = Color(0xFF424242),
                                    textAlign = TextAlign.Center,
                                    lineHeight = 22.sp
                                )
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Premium Özellikler Başlığı
                                Text(
                                    text = "Premium'la:",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF5D35A0),
                                    textAlign = TextAlign.Center
                                )
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                // Premium Özellikleri - Özel Stillendirilmiş
                                PremiumFeatureCard(text = "7-15 arası tüm avatarların kilidini açın", icon = "🧙‍♂️")
                                PremiumFeatureCard(text = "Her gün otomatik 5 yıldız bonus kazanın", icon = "🎁")
                                PremiumFeatureCard(text = "Oyunlardaki gizli istatistikleri keşfedin", icon = "📊")
                                PremiumFeatureCard(text = "Uygulama ikonunuzu kendinize özel yapın", icon = "🎨")
                                PremiumFeatureCard(text = "Reklam canavarlarına veda edin", icon = "🚫")
                                PremiumFeatureCard(text = "Özel aktivitelerle yeteneklerinizi geliştirin", icon = "🧩")
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                // Motivasyon Metni
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    backgroundColor = Color(0xFFF5F5F5),
                                    elevation = 2.dp
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Ve unutmayın:",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color(0xFF5D35A0)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = "Desteğinizle ben de sizler için daha da harika uygulamalar geliştirebilirim!\n" +
                                                  "Bir abiniz olarak her zaman sizin iyiliğinizi düşünüyorum. 💬\n" +
                                                  "Haydi, birlikte büyüyelim ve başarıya ulaşalım! 🌟",
                                            fontSize = 14.sp,
                                            color = Color(0xFF333333),
                                            textAlign = TextAlign.Center,
                                            lineHeight = 20.sp
                                        )
                                    }
                                }
                            }
                            
                            // Scroll gösterge ikonu
                            if (scrollIndicatorVisible) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 8.dp)
                                        .size(36.dp)
                                        .background(
                                            color = Color.White.copy(alpha = 0.8f),
                                            shape = CircleShape
                                        )
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Aşağı kaydır",
                                        tint = Color(0xFF5D35A0)
                                    )
                                }
                            }
                        }
                        
                        // Butonlar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Vazgeç Butonu
                            OutlinedButton(
                                onClick = { 
                                    try {
                                        showPremiumRequiredDialog = false
                                    } catch (e: Exception) {
                                        Log.e("AvatarStoreScreen", "Premium dialog kapatma hatası: ${e.message}", e)
                                    }
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF5D35A0)
                                ),
                                border = BorderStroke(1.dp, Color(0xFF5D35A0)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "Vazgeç", 
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF5D35A0)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            // Premium'a Yükselt Butonu
                            Button(
                                onClick = { 
                                    try {
                                        showPremiumRequiredDialog = false
                                        Log.d("AvatarStoreScreen", "🛒 Premium butonu tıklandı")
                                        
                                        // Önce temel kontrolleri yap
                                        if (purchaseManager == null) {
                                            Log.e("AvatarStoreScreen", "❌ PurchaseManager null")
                                            Toast.makeText(
                                                context, 
                                                "Satın alma sistemi başlatılamadı. Lütfen uygulamayı yeniden başlatın.", 
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@Button
                                        }
                                        
                                        // Context kontrolü - Ayarlar sayfasındaki gibi düzeltildi
                                        if (context !is ComponentActivity) {
                                            Log.e("AvatarStoreScreen", "❌ Context ComponentActivity değil: ${context.javaClass.simpleName}")
                                            Toast.makeText(
                                                context, 
                                                "Satın alma ekranı açılamıyor. Lütfen avatar mağazasını yeniden açın.", 
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@Button
                                        }
                                        
                                        // İnternet bağlantısı kontrolü
                                        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                                        val activeNetwork = connectivityManager.activeNetworkInfo
                                        val isConnected = activeNetwork?.isConnectedOrConnecting == true
                                        
                                        if (!isConnected) {
                                            Log.e("AvatarStoreScreen", "❌ İnternet bağlantısı yok")
                                            Toast.makeText(
                                                context, 
                                                "İnternet bağlantınızı kontrol edin ve tekrar deneyin.", 
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@Button
                                        }
                                        
                                        // Google Play Store kontrolü
                                        try {
                                            context.packageManager.getPackageInfo("com.android.vending", 0)
                                            Log.d("AvatarStoreScreen", "✅ Google Play Store mevcut")
                                        } catch (e: Exception) {
                                            Log.e("AvatarStoreScreen", "❌ Google Play Store bulunamadı: ${e.message}")
                                            Toast.makeText(
                                                context, 
                                                "Google Play Store bulunamadı. Lütfen Google Play Store'u güncelleyin.", 
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@Button
                                        }
                                        
                                        // Google Play Services kontrolü
                                        try {
                                            val gmsPackage = context.packageManager.getPackageInfo("com.google.android.gms", 0)
                                            Log.d("AvatarStoreScreen", "✅ Google Play Services mevcut, versiyon: ${gmsPackage.versionName}")
                                        } catch (e: Exception) {
                                            Log.e("AvatarStoreScreen", "❌ Google Play Services bulunamadı: ${e.message}")
                                            Toast.makeText(
                                                context, 
                                                "Google Play Services bulunamadı. Lütfen Google Play Services'i güncelleyin.", 
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@Button
                                        }
                                        
                                        Log.d("AvatarStoreScreen", "✅ Tüm kontroller başarılı, satın alma başlatılıyor...")
                                        
                                        // Satın alma işlemini başlat - Düzeltildi
                                        purchaseManager.buyPremium(context as ComponentActivity)
                                        
                                        Toast.makeText(
                                            context, 
                                            "Google Play Store açılıyor...", 
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        
                                        Log.d("AvatarStoreScreen", "🔄 Premium satın alma başlatıldı")
                                        
                                    } catch (e: Exception) {
                                        Log.e("AvatarStoreScreen", "❌ Premium butonu tıklama hatası: ${e.message}", e)
                                        
                                        // Hata türüne göre özel mesajlar
                                        val errorMessage = when {
                                            e.message?.contains("BILLING_UNAVAILABLE") == true -> 
                                                "Bu cihazda satın alma özelliği desteklenmiyor."
                                            e.message?.contains("SERVICE_UNAVAILABLE") == true -> 
                                                "Google Play Store servisi şu anda kullanılamıyor. Lütfen daha sonra tekrar deneyin."
                                            e.message?.contains("DEVELOPER_ERROR") == true -> 
                                                "Uygulama konfigürasyon hatası. Lütfen uygulamayı güncelleyin."
                                            e.message?.contains("NETWORK") == true -> 
                                                "İnternet bağlantı sorunu. Lütfen bağlantınızı kontrol edin."
                                            else -> 
                                                "Satın alma işlemi başlatılamadı: ${e.message}"
                                        }
                                        
                                        Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFFFFD700),
                                    contentColor = Color(0xFF5D35A0)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1.5f)
                                    .height(48.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_premium),
                                        contentDescription = null,
                                        tint = Color(0xFF5D35A0),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Premium'a Yükselt",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color(0xFF5D35A0),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Satın alma onay diyaloğu
        if (showPurchaseConfirmDialog && selectedAvatarForInfo != null) {
            // CoroutineScope tanımlanması burada
            val coroutineScope = rememberCoroutineScope()
            
            Dialog(onDismissRequest = { showPurchaseConfirmDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    backgroundColor = Color.White,
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Avatar Satın Al",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D35A0)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Box(
                            modifier = Modifier
                                .size(120.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFFE8F5E9),
                                            Color(0xFFB2DFDB)
                                        )
                                    )
                                )
                                .border(
                                    width = 3.dp,
                                    brush = Brush.sweepGradient(
                                        colors = listOf(
                                            Color(0xFF5D35A0),
                                            Color(0xFF7E57C2),
                                            Color(0xFF9575CD),
                                            Color(0xFF5D35A0)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            AnimatedAvatarView(
                                avatar = selectedAvatarForInfo!!,
                                modifier = Modifier.size(100.dp),
                                contentDescription = selectedAvatarForInfo!!.name
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = selectedAvatarForInfo!!.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D35A0)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = selectedAvatarForInfo!!.description,
                            fontSize = 16.sp,
                            color = Color(0xFF757575),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Fiyat bilgisi - görsel tasarım
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF1F8E9))
                                .padding(vertical = 12.dp, horizontal = 16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Yıldız",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${selectedAvatarForInfo!!.price} yıldız",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF558B2F)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "Bakiye: $userStars",
                                fontSize = 16.sp,
                                color = Color(0xFF558B2F)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // İptal butonu
                            OutlinedButton(
                                onClick = { showPurchaseConfirmDialog = false },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = Color(0xFF5D35A0)
                                ),
                                border = BorderStroke(1.dp, Color(0xFF5D35A0)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("İptal", fontSize = 16.sp)
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            // Satın al butonu
                            Button(
                                onClick = {
                                    try {
                                        showPurchaseConfirmDialog = false
                                        Log.d("AvatarStoreScreen", "Avatar satın alma başlatılıyor: ${selectedAvatarForInfo!!.id}")
                                        
                                        // KRİTİK: Doğrudan manuel hesaplama yapıp yıldızları UI'da güncelle
                                        val avatarPrice = selectedAvatarForInfo!!.price
                                        val currentStars = userStars // mevcut UI'da gösterilen yıldız değeri
                                        
                                        // Satın alma işlemini başlat
                                        coroutineScope.launch(Dispatchers.IO) {
                                            val unlockResult = avatarService.unlockAvatar(selectedAvatarForInfo!!.id)
                                            if (unlockResult) {
                                                // Yeni avatar'ı otomatik olarak seç
                                                avatarService.setCurrentAvatar(selectedAvatarForInfo!!.id)
                                                
                                                // KRİTİK: Tüm SharedPreferences'ları manuel güncelle
                                                val newStars = currentStars - avatarPrice
                                                context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE)
                                                    .edit().putInt("display_stars", newStars).commit()
                                                
                                                context.getSharedPreferences("game_progress", Context.MODE_PRIVATE)
                                                    .edit().putInt("display_stars", newStars).commit()
                                                
                                                // UI thread'e dön ve UI'ı güncelle
                                                withContext(Dispatchers.Main) {
                                                    // DOĞRUDAN UI DEĞİŞKENİNİ GÜNCELLE!
                                                    userStars = newStars
                                                    
                                                    // EventBus'a bildir
                                                    EventBus.forceStarNotification(newStars)
                                                    
                                                    // Kullanıcıya bilgi ver
                                                    Toast.makeText(context, "${selectedAvatarForInfo!!.name} satın alındı! Kalan yıldızlar: $newStars", 
                                                        Toast.LENGTH_SHORT).show()
                                                    
                                                    // Avatar değişikliğini bildir
                                                    onAvatarChanged(selectedAvatarForInfo!!.id)
                                                }
                                            } else {
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(context, "Satın alma başarısız", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e("AvatarStoreScreen", "Satın alma işleminde hata: ${e.message}", e)
                                        Toast.makeText(context, "Satın alma başarısız", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF4CAF50),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(28.dp)
                            ) {
                                Text("Satın Al", fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AvatarItem(
    avatar: Avatar,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    // Kart rengi ve kenarlık rengi
    val cardColor = if (isSelected) Color(0xFFE3F2FD) else Color.White
    val borderColor = if (isSelected) Color(0xFF2196F3) else Color(0xFFE0E0E0)
    
    // Kilitli avatarlar için arka plan rengi
    val backgroundTint = when {
        !avatar.isUnlocked -> Color(0xFFF5F5F5) // Daha soluk bir arka plan
        isSelected -> Color(0xFFE3F2FD)  // Seçili avatarlar için hafif mavi
        else -> Color.White
    }
    
    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .aspectRatio(0.85f) // Biraz daha uzun kart
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = borderColor,
                shape = RoundedCornerShape(20.dp)
            ),
        backgroundColor = backgroundTint,
        elevation = if (isSelected) 8.dp else 4.dp
    ) {
        Box {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Avatar resmi container
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFFE1F5FE),
                                    Color(0xFFB3E5FC)
                                )
                            )
                        )
                        .border(
                            width = 2.dp,
                            color = if (isSelected) Color(0xFF2196F3) else Color(0xFFBDBDBD),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Avatar görünümü
                    AnimatedAvatarView(
                        avatar = avatar,
                            modifier = Modifier.size(72.dp),
                        contentDescription = avatar.name
                        )
                        
                    // Kilit overlay'i
                    if (!avatar.isUnlocked) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xAA000000), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_lock),
                                contentDescription = "Kilitli",
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    } 
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                // Avatar adı - daima görünür olacak şekilde
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSelected) Color(0x1A2196F3) else Color(0x1A000000),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(vertical = 4.dp, horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                Text(
                    text = avatar.name,
                        fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    color = if (isSelected) Color(0xFF2196F3) else Color(0xFF424242)
                )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Premium rozeti - Premium avatarlar için (id > 6)
                if (avatar.id > 6) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .background(
                                color = Color(0xFFFFF9C4), // Açık sarı arka plan
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(vertical = 2.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_premium),
                                contentDescription = "Premium",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Premium",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF795548)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Durum bilgisi: seçili, seç butonu veya kilitli bilgisi
                if (avatar.isUnlocked) {
                    if (isSelected) {
                        // Seçili durum gösterge kutusu
                        Box(
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .background(
                                    color = Color(0xFF4CAF50),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Seçili",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    // Kilitli olmayan ve seçili olmayan avatarlar için gri "seç" butonu
                    else {
                        Box(
                            modifier = Modifier
                                .padding(vertical = 4.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF9E9E9E),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Seç",
                                color = Color(0xFF9E9E9E),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // Kilitli avatarlar için yıldız fiyatı
                    Box(
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .background(
                                color = if (avatar.id > 6) Color(0xFFE1BEE7) else Color(0xFFFFF9C4), // Premium avatarlar için mor tint
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            // Premium için kilit ikonu, normal için yıldız ikonu
                            if (avatar.id > 6) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Premium Gerekli",
                                    tint = Color(0xFF9C27B0),
                                    modifier = Modifier.size(16.dp)
                                )
                            } else {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Yıldız",
                                tint = Color(0xFFFFC107),
                                    modifier = Modifier.size(16.dp)
                            )
                            }
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = if (avatar.id > 6) "Gerekli" else "${avatar.price}",
                                color = if (avatar.id > 6) Color(0xFF9C27B0) else Color(0xFF795548),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
            
            // Seçili avatar için köşede tik işareti
            if (avatar.isUnlocked && isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                        .background(Color(0xFF4CAF50), CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Seçildi",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            // Premium işareti köşede - Sadece ID > 6 olanlar için
            if (avatar.id > 6 && !avatar.isUnlocked) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp)
                        .size(28.dp)
                        .background(Color(0xFF9C27B0), CircleShape)
                        .border(1.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_premium),
                        contentDescription = "Premium Gerekli",
                        tint = Color(0xFFFFD700),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumFeatureItem(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            color = Color(0xFF424242),
            fontSize = 14.sp
        )
    }
}

/**
 * Premium özellik kartı - daha çekici ve kullanıcı dostu tasarım
 */
@Composable
private fun PremiumFeatureCard(text: String, icon: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(10.dp),
        backgroundColor = Color(0xFFF8F8F8),
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emoji ikonu
            Text(
                text = icon,
                fontSize = 20.sp,
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFFEEEEEE), shape = CircleShape)
                    .padding(6.dp),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Text(
                text = text,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF333333)
            )
        }
    }
}

/**
 * SharedPreferences nesnesini alma yardımcı fonksiyonu
 */
private fun sharedPreferences(context: Context): SharedPreferences {
    return context.getSharedPreferences("star_manager_prefs", Context.MODE_PRIVATE)
} 