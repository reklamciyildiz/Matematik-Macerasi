package com.yigit033.matematikmacarasi.screens

import android.os.Bundle
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.settings.IconSelectorActivity
import com.yigit033.matematikmacarasi.services.AudioService
import com.yigit033.matematikmacarasi.services.GameProgressService
import com.yigit033.matematikmacarasi.services.NotificationService
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import com.yigit033.matematikmacarasi.services.PremiumCodeValidator
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import com.yigit033.matematikmacarasi.utils.AppVersionUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.OutlinedTextField

/**
 * Ayarlar kartı bileşeni
 */
@Composable
fun SettingsCard(
    title: String,
    description: String = "",
    icon: Int? = null,
    containerColor: Color = Color(0xFF42A5F5),
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Başlık kısmı
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // İkon varsa göster - try-catch kullanmadan
                if (icon != null) {
                    // Basit text ile değiştir
                    Text(
                        text = "🔊",
                        fontSize = 18.sp,
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    if (description.isNotEmpty()) {
                        Text(
                            text = description,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Kart içeriği
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

/**
 * Ayarlar ekranı
 */
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    audioService: AudioService
) {
    val context = LocalContext.current
    
    // Yükleme durumu
    var isLoading by remember { mutableStateOf(true) }
    
    // Parçalı yükleme durumları
    var isBasicSettingsLoaded by remember { mutableStateOf(false) }
    var isAudioSettingsLoaded by remember { mutableStateOf(false) }
    var isPremiumStateLoaded by remember { mutableStateOf(false) }
    
    // Yükleme durumu metni
    var loadingText by remember { mutableStateOf("Temel ayarlar yükleniyor...") }
    
    // Ayarlar için state tanımlamaları
    var isMusicEnabled by remember { mutableStateOf(true) }
    var isSoundEnabled by remember { mutableStateOf(true) }
    var isNotificationEnabled by remember { mutableStateOf(true) }
    var isNightModeEnabled by remember { mutableStateOf(false) }
    var isDataSavingEnabled by remember { mutableStateOf(false) }
    var selectedLanguage by remember { mutableStateOf("Türkçe") }
    
    // Premium durumu
    var isPremium by remember { mutableStateOf(false) }
    var purchaseInProgress by remember { mutableStateOf(false) }
    
    // PurchaseManager
    val purchaseManager = remember { PurchaseManager.getInstance(context) }
    
    // Premium kod dialog durumu
    var showCodeDialog by remember { mutableStateOf(false) }
    var codeInput by remember { mutableStateOf("") }
    var codeValidationInProgress by remember { mutableStateOf(false) }
    
    // Servisleri lazy olarak başlat - Duplicate kaldırıldı, yukarıda zaten tanımlı
    val codeValidator = remember {
        try {
            PremiumCodeValidator.getInstance(context)
        } catch (e: Exception) {
            Log.e("SettingsScreen", "CodeValidator başlatılamadı: ${e.message}", e)
            null
        }
    }
    val notificationService = remember { 
        try {
            NotificationService.getInstance(context)
        } catch (e: Exception) {
            Log.e("SettingsScreen", "NotificationService başlatılamadı: ${e.message}", e)
            null
        }
    }
    val scope = rememberCoroutineScope()
    
    // Temel ayarları yükle - bağımsız ve hızlı işlemler
    LaunchedEffect(Unit) {
        scope.launch {
        try {
                loadingText = "Temel ayarlar yükleniyor..."
            
                withContext(Dispatchers.IO) {
            val sharedPrefs = context.getSharedPreferences("matematik_macerasi_ayarlar", ComponentActivity.MODE_PRIVATE)
                    
                    // UI etkilemeyen ayarları arka planda yükle
            isNotificationEnabled = sharedPrefs.getBoolean("notifications_enabled", true)
            isNightModeEnabled = sharedPrefs.getBoolean("night_mode_enabled", false)
            isDataSavingEnabled = sharedPrefs.getBoolean("data_saving_enabled", false)
            selectedLanguage = sharedPrefs.getString("selected_language", "Türkçe") ?: "Türkçe"
                }
                
                isBasicSettingsLoaded = true
                
                // Eğer tüm ayarlar yüklendiyse, yükleme durumunu kapat
                if (isBasicSettingsLoaded && isAudioSettingsLoaded && isPremiumStateLoaded) {
                    isLoading = false
                }
        } catch (e: Exception) {
                Log.e("SettingsScreen", "Temel ayarlar yüklenirken hata: ${e.message}", e)
                isBasicSettingsLoaded = true // Hata olsa bile ilerlemeye devam et
                
                // Eğer tüm ayarlar yüklendiyse, yükleme durumunu kapat
                if (isBasicSettingsLoaded && isAudioSettingsLoaded && isPremiumStateLoaded) {
                    isLoading = false
                }
            }
        }
    }
    
    // Ses ayarlarını yükle - ayrı bir LaunchedEffect ile
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                loadingText = "Ses ayarları yükleniyor..."
                
                // Ses ayarlarını yükle (bu da hızlı çalışır)
                isMusicEnabled = audioService.isMusicEnabled()
                isSoundEnabled = audioService.isSoundEnabled()
                
                isAudioSettingsLoaded = true
                
                // Eğer tüm ayarlar yüklendiyse, yükleme durumunu kapat
                if (isBasicSettingsLoaded && isAudioSettingsLoaded && isPremiumStateLoaded) {
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e("SettingsScreen", "Ses ayarları yüklenirken hata: ${e.message}", e)
                isAudioSettingsLoaded = true // Hata olsa bile ilerlemeye devam et
                
                // Eğer tüm ayarlar yüklendiyse, yükleme durumunu kapat
                if (isBasicSettingsLoaded && isAudioSettingsLoaded && isPremiumStateLoaded) {
                    isLoading = false
                }
            }
        }
    }
    
    // Premium durumunu yükle - ayrı bir LaunchedEffect ile ve timeout eklenmiş halde
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                loadingText = "Premium durum kontrol ediliyor..."
                
                // Önce Firebase'den senkronize et (uygulama silme-yükleme durumu için)
                withContext(Dispatchers.IO) {
                    val synced = codeValidator?.checkAndSyncPremiumStatus() ?: false
                    if (synced) {
                        Log.d("SettingsScreen", "✅ Firebase'den premium durumu senkronize edildi!")
                    }
                }
                
                // Sonra arka planda async kontrol yap
                purchaseManager.checkPremiumStatusAsync()
                
                // StateFlow'dan sürekli dinle
                launch {
                    purchaseManager.premiumState.collect { premiumState ->
                        Log.d("SettingsScreen", "🔄 Premium StateFlow değişti: $premiumState")
                        isPremium = premiumState
                    }
                }
                
                // Yükleme işaretini kapat
                isPremiumStateLoaded = true
                
                // Eğer tüm ayarlar yüklendiyse, yükleme durumunu kapat
                if (isBasicSettingsLoaded && isAudioSettingsLoaded && isPremiumStateLoaded) {
                    isLoading = false
                }
            } catch (e: Exception) {
                Log.e("SettingsScreen", "Premium durum kontrolünde hata: ${e.message}", e)
                isPremiumStateLoaded = true // Hata olsa bile ilerlemeye devam et
                
                // Eğer tüm ayarlar yüklendiyse, yükleme durumunu kapat
                if (isBasicSettingsLoaded && isAudioSettingsLoaded && isPremiumStateLoaded) {
                    isLoading = false
                }
            }
        }
    }
    
    // Global timeout - en kötü durumda bile 5 saniye sonra yükleme ekranını kapat
    LaunchedEffect(Unit) {
        delay(5000) // 5 saniye sonra yükleme durumunu kapat
        isLoading = false
    }
    
    // Satın alma durumunu sadece kullanıcı satın alma butonuna bastığında kontrol et
    // Otomatik kontrol kaldırıldı - sadece manuel satın alma sırasında kontrol yapılacak
    
    // Ayarları kaydetme fonksiyonu
    fun saveSettings(key: String, value: Any) {
        scope.launch {
        try {
                // Ayar kaydetme işlemini arka planda yap
                withContext(Dispatchers.IO) {
            val sharedPrefs = context.getSharedPreferences("matematik_macerasi_ayarlar", ComponentActivity.MODE_PRIVATE)
            with(sharedPrefs.edit()) {
                when (value) {
                    is Boolean -> putBoolean(key, value)
                    is String -> putString(key, value)
                    is Int -> putInt(key, value)
                    is Float -> putFloat(key, value)
                    is Long -> putLong(key, value)
                }
                apply()
                    }
                }
                
                // Bildirim ayarları değiştiyse, NotificationService'i güncelle
                if (key == "notifications_enabled") {
                    if (value as Boolean) {
                        // Bildirimler açıldıysa, bildirim servisini aktifleştir
                        notificationService?.createNotificationChannels()
                        
                        // Kullanıcıya bilgilendirme mesajı göster
                        Toast.makeText(
                            context, 
                            "Günlük görev bildirimleri akşam 19:00'da gösterilecek", 
                            Toast.LENGTH_LONG
                        ).show()
                        
                        // Bildirim ayarlarını yükle
                        withContext(Dispatchers.IO) {
                            notificationService?.loadNotificationSettings()
                        }
                    } else {
                        // Bildirimler kapatıldıysa, bildirim zamanlamalarını iptal et
                        withContext(Dispatchers.IO) {
                            notificationService?.cancelAllNotifications()
                        }
                        
                        Toast.makeText(
                            context, 
                            "Bildirimler kapatıldı", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        } catch (e: Exception) {
            Log.e("SettingsScreen", "Ayarları kaydederken hata: ${e.message}", e)
            Toast.makeText(context, "Ayarlar kaydedilemedi: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    // Normal ekran içeriği
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFCE93D8), // Açık mor (üst)
                        Color(0xFFAB47BC), // Orta açık mor
                        Color(0xFF8E24AA), // Orta koyu mor
                        Color(0xFF6A1B9A)  // Koyu mor (alt)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // İçerik alanı
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Başlık ve geri tuşu
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Geri tuşu
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Geri",
                        tint = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Başlık
                Text(
                    text = "Ayarlar",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            // Yükleme göstergesi
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = loadingText,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                // Premium sürüm kartı
                SettingsCard(
                    title = "Premium Sürüm",
                    description = if (isPremium) "Premium sürüm etkin" else "Tüm reklamları kaldırın, özel içeriklere erişin",
                    containerColor = Color(0xFFE91E63)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = if (isPremium) "Premium Üyelik Aktif" else "Premium Üyelik",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                
                                if (isPremium) {
                                    Text(
                                        text = "Tüm premium özelliklere erişebilirsiniz",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                } else {
                                    Text(
                                        text = "Reklamsız deneyim ve ekstra özellikler",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            
                            if (isPremium) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            } else {
                                if (purchaseInProgress) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(32.dp),
                                        color = Color(0xFFE91E63)
                                    )
                                }
                            }
                        }
                        
                        if (!isPremium && !purchaseInProgress) {
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Büyük ve çekici premium satın alma butonu
                            Button(
                                onClick = {
                                    try {
                                        Log.d("SettingsScreen", "🛒 Premium butonu tıklandı")
                                        
                                        // Önce temel kontrolleri yap
                                        if (purchaseManager == null) {
                                            Log.e("SettingsScreen", "❌ PurchaseManager null")
                                            Toast.makeText(
                                                context, 
                                                "Satın alma sistemi başlatılamadı. Lütfen uygulamayı yeniden başlatın.", 
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@Button
                                        }
                                        
                                        if (context !is ComponentActivity) {
                                            Log.e("SettingsScreen", "❌ Context ComponentActivity değil: ${context.javaClass.simpleName}")
                                            Toast.makeText(
                                                context, 
                                                "Satın alma ekranı açılamıyor. Lütfen ayarları yeniden açın.", 
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@Button
                                        }
                                        
                                        // İnternet bağlantısı kontrolü
                                        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
                                        val activeNetwork = connectivityManager.activeNetworkInfo
                                        val isConnected = activeNetwork?.isConnectedOrConnecting == true
                                        
                                        if (!isConnected) {
                                            Log.e("SettingsScreen", "❌ İnternet bağlantısı yok")
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
                                            Log.d("SettingsScreen", "✅ Google Play Store mevcut")
                                        } catch (e: Exception) {
                                            Log.e("SettingsScreen", "❌ Google Play Store bulunamadı: ${e.message}")
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
                                            Log.d("SettingsScreen", "✅ Google Play Services mevcut, versiyon: ${gmsPackage.versionName}")
                                        } catch (e: Exception) {
                                            Log.e("SettingsScreen", "❌ Google Play Services bulunamadı: ${e.message}")
                                            Toast.makeText(
                                                context, 
                                                "Google Play Services bulunamadı. Lütfen Google Play Services'i güncelleyin.", 
                                                Toast.LENGTH_LONG
                                            ).show()
                                            return@Button
                                        }
                                        
                                        Log.d("SettingsScreen", "✅ Tüm kontroller başarılı, satın alma başlatılıyor...")
                                        
                                        // Satın alma işlemini başlat
                                        purchaseManager.buyPremium(context as ComponentActivity)
                                        purchaseInProgress = true
                                        
                                        // Sadece satın alma başlatıldığında durumu izle
                                        purchaseManager.purchaseStatus.observe(context as ComponentActivity) { status ->
                                            when (status) {
                                                is PurchaseManager.PurchaseStatus.Success -> {
                                                    Toast.makeText(context, "Premium sürüm başarıyla satın alındı!", Toast.LENGTH_SHORT).show()
                                                    purchaseInProgress = false
                                                    isPremium = true
                                                }
                                                is PurchaseManager.PurchaseStatus.Pending -> {
                                                    Toast.makeText(context, "Satın alma işlemi beklemede...", Toast.LENGTH_SHORT).show()
                                                    purchaseInProgress = true
                                                }
                                                is PurchaseManager.PurchaseStatus.Canceled -> {
                                                    Toast.makeText(context, "Satın alma işlemi iptal edildi", Toast.LENGTH_SHORT).show()
                                                    purchaseInProgress = false
                                                }
                                                is PurchaseManager.PurchaseStatus.AlreadyOwned -> {
                                                    Toast.makeText(context, "Zaten premium sürüme sahipsiniz", Toast.LENGTH_SHORT).show()
                                                    purchaseInProgress = false
                                                    isPremium = true
                                                }
                                                is PurchaseManager.PurchaseStatus.ServiceUnavailable -> {
                                                    Toast.makeText(context, "Google Play servisi şu anda kullanılamıyor", Toast.LENGTH_SHORT).show()
                                                    purchaseInProgress = false
                                                }
                                                is PurchaseManager.PurchaseStatus.ProductNotFound -> {
                                                    Toast.makeText(context, "Premium ürün bulunamadı", Toast.LENGTH_SHORT).show()
                                                    purchaseInProgress = false
                                                }
                                                is PurchaseManager.PurchaseStatus.Failed -> {
                                                    Toast.makeText(context, "Satın alma işlemi başarısız oldu: Kod ${status.code}", Toast.LENGTH_SHORT).show()
                                                    purchaseInProgress = false
                                                }
                                            }
                                        }
                                        
                                        Toast.makeText(
                                            context, 
                                            "Google Play Store açılıyor...", 
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        
                                        Log.d("SettingsScreen", "🔄 Premium satın alma başlatıldı")
                                        
                                    } catch (e: Exception) {
                                        Log.e("SettingsScreen", "❌ Premium butonu tıklama hatası: ${e.message}", e)
                                        
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
                                        purchaseInProgress = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF4081)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "Premium'a Yükselt",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Premium Kod Giriş Butonu
                            Button(
                                onClick = {
                                    showCodeDialog = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF4CAF50)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CardGiftcard,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    
                                    Text(
                                        text = "Premium Kod Gir",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Premium özellikleri listesi - Kişisel mesaj olarak değiştirildi
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "Premium ile neler açılır?",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.DarkGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Premium üyelik mevcut satın alma sistemiyle açılır.\\n" +
                                           "• Pazar Simülatörü'nde 10 mağaza ve tezgâh işletme\\n" +
                                           "• Özel avatarlar ve uygulama ikonları\\n" +
                                           "• Reklamsız oyun deneyimi\\n" +
                                           "• Mevcut Premium günlük bonusları\\n\\n" +
                                           "Öğrenme raporunun temel görünümü tüm oyunculara açıktır. " +
                                           "Premium erişimin varsa tekrar satın alma yapmana gerek yok.",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        lineHeight = 18.sp
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ses Ayarları Kartı
            SettingsCard(
                title = "Ses Ayarları",
                    description = "Müzik ve ses efektlerini ayarlayın",
                    containerColor = Color(0xFF2196F3)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Müzik ayarı
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Müzik",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = "Arka plan müziği",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        
                        Switch(
                            checked = isMusicEnabled,
                            onCheckedChange = { isEnabled ->
                                audioService.setMusicEnabled(isEnabled)
                                isMusicEnabled = isEnabled
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF2196F3),
                                checkedTrackColor = Color(0xFF90CAF9)
                            )
                        )
                    }
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                    
                    // Ses efektleri ayarı
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Ses Efektleri",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = "Oyun ses efektleri",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        
                        Switch(
                            checked = isSoundEnabled,
                            onCheckedChange = { isEnabled ->
                                audioService.setSoundEnabled(isEnabled)
                                isSoundEnabled = isEnabled
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF2196F3),
                                checkedTrackColor = Color(0xFF90CAF9)
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Bildirim Ayarları Kartı (Geri eklendi)
            SettingsCard(
                title = "Bildirim Ayarları",
                description = "Bildirimlerinizi özelleştirin",
                containerColor = Color(0xFFEC407A)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Bildirim ayarı
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Bildirimler",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                            Text(
                                text = "Günlük hatırlatıcılar ve ödüller",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        
                        Switch(
                            checked = isNotificationEnabled,
                            onCheckedChange = { isEnabled ->
                                isNotificationEnabled = isEnabled
                                saveSettings("notifications_enabled", isEnabled)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFE91E63),
                                checkedTrackColor = Color(0xFFF8BBD0)
                            )
                        )
                    }
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                    
                        // Bildirim zamanı seçimi
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                    text = "Bildirim Zamanı",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            
                                // Bildirim zamanı butonları (sadece bildirimler açıksa aktif)
                    Row(
                        modifier = Modifier
                                        .alpha(if (isNotificationEnabled) 1f else 0.5f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                                    // Saat 19:00 seçili olduğunu gösteren küçük bir görsel
                        Text(
                                        text = "19:00",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFFE91E63)
                                    )

                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFFE91E63),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(4.dp))


                            Text(
                                        text = "Telefonundan bildirimlere izin vermeyi unutma.",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )

                                    Spacer(modifier = Modifier.width(4.dp))
                                    
                                    
                                }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Uygulama ikonları eskiden yalnız eski ana ekrandan erişilebiliyordu.
            // Mevcut ikon seçicisini ve aynı Premium hak kontrolünü yeniden kullan.
            SettingsCard(
                title = "Uygulama İkonu",
                description = "Matematik Macerası, Robot veya Uzay temasını seç",
                containerColor = Color(0xFF2379A9)
            ) {
                Button(
                    onClick = {
                        context.startActivity(Intent(context, IconSelectorActivity::class.java))
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF103E65))
                ) {
                    Text("🎨 Uygulama İkonunu Değiştir", color = Color.White)
                }
            }

            // Uygulama Bilgileri Kartı
            SettingsCard(
                title = "Uygulama Bilgileri",
                description = "Uygulama ve geliştirici bilgileri",
                containerColor = Color(0xFF26A69A)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Uygulama Versiyonu: ${AppVersionUtils.getFullVersionInfo(context)}",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = Color.LightGray.copy(alpha = 0.5f)
                    )
                    
                    Text(
                        text = "Geliştirici: YGT Studio",
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                }
            }
            
            // Alt boşluk
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
    
    // Premium Kod Giriş Dialog
    if (showCodeDialog) {
        AlertDialog(
            onDismissRequest = { 
                if (!codeValidationInProgress) {
                    showCodeDialog = false
                    codeInput = ""
                }
            },
            title = {
                Text(
                    text = "Premium Kod Gir",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Sihirli premium kodunu girin:",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    OutlinedTextField(
                        value = codeInput,
                        onValueChange = { codeInput = it.uppercase() },
                        label = { Text("Premium Kod") },
                        singleLine = true,
                        enabled = !codeValidationInProgress,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("DERSHANEX-PREMIUM") }
                    )
                    
                    if (codeValidationInProgress) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Kod doğrulanıyor...")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (codeInput.isNotBlank() && !codeValidationInProgress) {
                            codeValidationInProgress = true
                            
                            scope.launch {
                                try {
                                    val result = withContext(Dispatchers.IO) {
                                        codeValidator?.validateAndActivateCode(codeInput)
                                    }
                                    
                                    when (result) {
                                        is PremiumCodeValidator.ValidationResult.Success -> {
                                            Toast.makeText(
                                                context,
                                                "🎉 Premium kod başarıyla aktive edildi!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            isPremium = true
                                            showCodeDialog = false
                                            codeInput = ""
                                        }
                                        is PremiumCodeValidator.ValidationResult.InvalidCode -> {
                                            Toast.makeText(
                                                context,
                                                "❌ Geçersiz kod. Lütfen kontrol edin.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        is PremiumCodeValidator.ValidationResult.CodeExpired -> {
                                            Toast.makeText(
                                                context,
                                                "⏰ Bu kodun süresi dolmuş.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        is PremiumCodeValidator.ValidationResult.CodeLimitReached -> {
                                            Toast.makeText(
                                                context,
                                                "🚫 Bu kod kullanım limitine ulaşmış.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        is PremiumCodeValidator.ValidationResult.AlreadyActivated -> {
                                            Toast.makeText(
                                                context,
                                                "✅ Bu cihaz zaten premium kod ile aktive edilmiş.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            isPremium = true
                                            showCodeDialog = false
                                            codeInput = ""
                                        }
                                        is PremiumCodeValidator.ValidationResult.NetworkError -> {
                                            Toast.makeText(
                                                context,
                                                "🌐 İnternet bağlantısı sorunu. Lütfen tekrar deneyin.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                            is PremiumCodeValidator.ValidationResult.Error -> {
                                                val errorMsg = (result as PremiumCodeValidator.ValidationResult.Error).message
                                                Toast.makeText(
                                                    context,
                                                    "❌ Hata: $errorMsg",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                Log.e("SettingsScreen", "Kod aktivasyon hatası: $errorMsg")
                                            }
                                        else -> {
                                            Toast.makeText(
                                                context,
                                                "❌ Bilinmeyen hata oluştu.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e("SettingsScreen", "Kod doğrulama hatası: ${e.message}", e)
                                    Toast.makeText(
                                        context,
                                        "❌ Bir hata oluştu: ${e.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } finally {
                                    codeValidationInProgress = false
                                }
                            }
                        }
                    },
                    enabled = codeInput.isNotBlank() && !codeValidationInProgress
                ) {
                    Text("Aktive Et")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showCodeDialog = false
                        codeInput = ""
                    },
                    enabled = !codeValidationInProgress
                ) {
                    Text("İptal")
                }
            }
        )
    }
}

/**
 * Premium özelliklerini görüntülemek için özel bir madde öğesi
 */
@Composable
private fun PremiumFeatureItem(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color(0xFFFFD700),
            modifier = Modifier.size(16.dp)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.DarkGray
        )
    }
} 