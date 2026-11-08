package com.yigit033.matematikmacarasi.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.screens.AvatarStoreActivity
import com.yigit033.matematikmacarasi.screens.SettingsActivity
import com.yigit033.matematikmacarasi.settings.IconSelectorActivity
import com.yigit033.matematikmacarasi.services.AvatarService
import com.yigit033.matematikmacarasi.services.EventBus
import com.yigit033.matematikmacarasi.services.GameStatisticsService
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import android.util.Log
import com.yigit033.matematikmacarasi.components.AnimatedAvatarView
import com.yigit033.matematikmacarasi.learning.LearningPathCatalog
import com.yigit033.matematikmacarasi.services.StarManager
import android.app.Activity
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import com.yigit033.matematikmacarasi.components.StatisticsDetailPopup

class ProfileActivity : ComponentActivity() {
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var avatarService: AvatarService
    private lateinit var gameStatisticsService: GameStatisticsService
    private lateinit var starManager: StarManager
    
    private val avatarStoreLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            try {
                EventBus.syncProfileToHomePage(this)
                EventBus.updateAvatarId(avatarService.getCurrentAvatar().id)
            } catch (e: Exception) {
                Log.e("ProfileActivity", "Avatar dönüşünde senkronizasyon hatası: ${e.message}", e)
            }
        }
    }

    // Add these properties at the class level
    private var userName: String = ""
    private var userAge: String = ""
    private var userGrade: String = ""
    private var userSchool: String = ""
    private var isPremium: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window,false)
        
        // Initialize services
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        avatarService = AvatarService(this)
        gameStatisticsService = GameStatisticsService.getInstance(this)
        starManager = StarManager.getInstance(this)
        
        // Kullanıcı bilgilerini yükle
        loadUserData()
        
        setContent {
            MatematikMacerasiTheme {
                val view=LocalView.current
                SideEffect {
                    window.statusBarColor=android.graphics.Color.TRANSPARENT
                    window.navigationBarColor=android.graphics.Color.TRANSPARENT
                    WindowCompat.getInsetsController(window,view)
                        .isAppearanceLightStatusBars=false
                    WindowCompat.getInsetsController(window,view)
                        .isAppearanceLightNavigationBars=false
                }
                ProfileScreen(
                    name = sharedPreferences.getString("user_name", "") ?: "",
                    currentClass = sharedPreferences.getString("user_class", "1. Sınıf") ?: "1. Sınıf",
                    avatarService = avatarService,
                    gameStatisticsService = gameStatisticsService,
                    onNameChange = { newName ->
                        sharedPreferences.edit().putString("user_name", newName).apply()
                    },
                    onClassChange = { newClass ->
                        sharedPreferences.edit().putString("user_class", newClass).apply()
                    },
                    onSaveClick = {
                        try {
                            // EventBus'a profil güncellemesini bildir
                            val currentUserName = sharedPreferences.getString("user_name", "") ?: ""
                            EventBus.updateProfileData(currentUserName, System.currentTimeMillis())
                            Log.d("ProfileActivity", "Profil bilgileri kaydedildi ve EventBus'a bildirildi: $currentUserName")
                            
                            // Profil ve ana sayfa arasında yıldız ve avatar bilgilerini senkronize et
                            EventBus.syncProfileToHomePage(this)
                            Log.d("ProfileActivity", "Ana sayfaya dönmeden önce yıldız ve avatar verileri senkronize edildi")
                        } catch (e: Exception) {
                            Log.e("ProfileActivity", "Profil bildirimi yapılırken hata: ${e.message}", e)
                        }
                        
                        // Aktiviteyi kapat
                        setResult(RESULT_OK)
                        finish()
                    },
                    onAvatarStoreClick = {
                        try {
                            avatarStoreLauncher.launch(Intent(this, AvatarStoreActivity::class.java))
                        } catch (e: Exception) {
                            Log.e("ProfileActivity", "Avatar mağazası açılamadı: ${e.message}", e)
                        }
                    },
                    onSettingsClick = {
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onAppIconClick = {
                        startActivity(Intent(this, IconSelectorActivity::class.java))
                    }
                )
            }
        }
    }
    
    /**
     * Kullanıcı bilgilerini SharedPreferences'ten yükler
     */
    private fun loadUserData() {
        try {
            val prefs = getSharedPreferences("user_profile", Context.MODE_PRIVATE)
            
            // Kişisel bilgileri yükle
            userName = prefs.getString("user_name", "") ?: ""
            userAge = prefs.getInt("user_age", 0).toString()
            userGrade = prefs.getString("user_grade", "") ?: ""
            userSchool = prefs.getString("user_school", "") ?: ""
            isPremium = prefs.getBoolean("is_premium", false)
            
            Log.d("ProfileActivity", "Kullanıcı bilgileri yüklendi: $userName, $userAge, $userGrade, $userSchool, Premium: $isPremium")
        } catch (e: Exception) {
            Log.e("ProfileActivity", "Kullanıcı bilgileri yüklenirken hata: ${e.message}", e)
        }
    }
}

@Composable
fun ProfileScreen(
    name: String,
    currentClass: String,
    avatarService: AvatarService,
    gameStatisticsService: GameStatisticsService,
    onNameChange: (String) -> Unit,
    onClassChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onAvatarStoreClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onAppIconClick: () -> Unit
) {
    val currentContext = LocalContext.current
    var starValue by remember { mutableStateOf(0) }
    var isPremium by remember { mutableStateOf(false) }
    
    // Yıldız sayısını başlangıçta al
    LaunchedEffect(Unit) {
        // Başlangıçta GameStatisticsService'den güncel verileri al
        starValue = getStarCount(currentContext)
        
        // EventBus üzerinden gelen yıldız güncellemelerini dinle
        EventBus.starUpdatedEvents.collect { event ->
            Log.d("ProfileScreen", "EventBus'tan yıldız güncellemesi alındı: ${event.stars}")
            // EventBus'tan gelen güncelleme bile olsa, gerçek değeri almak için getStarCount'u kullan
            starValue = getStarCount(currentContext)
        }
    }
    
    // PurchaseManager'ı güvenli bir şekilde başlat
    var purchaseManager: com.yigit033.matematikmacarasi.services.payments.PurchaseManager? = null
    
    // PurchaseManager'ı güvenli bir şekilde başlat
    LaunchedEffect(Unit) {
        try {
            // Önce Firebase'den senkronize et (uygulama silme-yükleme durumu için)
            val codeValidator = com.yigit033.matematikmacarasi.services.PremiumCodeValidator.getInstance(currentContext)
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                val synced = codeValidator.checkAndSyncPremiumStatus()
                if (synced) {
                    Log.d("ProfileScreen", "✅ Firebase'den premium durumu senkronize edildi!")
                }
            }
            
            purchaseManager = com.yigit033.matematikmacarasi.services.payments.PurchaseManager.getInstance(currentContext)
            
            // Sync'ten sonra hızlı senkron kontrol yap - artık güncel veri
            isPremium = purchaseManager?.isPremiumSync() ?: false
            Log.d("ProfileScreen", "Premium durum kontrol edildi: $isPremium")
            
            // Sonra arka planda async kontrol yap ve değişiklikleri dinle
            purchaseManager?.checkPremiumStatusAsync()
            
            // Premium durumunu dinle ve isPremium değişkenini güncelle
            purchaseManager?.premiumState?.collect { premiumState ->
                isPremium = premiumState
            }
        } catch (e: Exception) {
            Log.e("ProfileScreen", "PurchaseManager başlatılamadı: ${e.message}", e)
            // Uygulama açılışında premium durumu kontrol edilemediğini kullanıcıya bildirmiyoruz
            // Sessizce log'a kaydediyoruz ve varsayılan olarak premium olmayan durumu kullanıyoruz
        }
    }
    
    // Context'i al
    val context = LocalContext.current
    
    // EventBus'tan güncel avatar ID'sini collectAsState ile al
    val currentAvatarId = EventBus.currentAvatarId.collectAsState()
    
    // Avatar ID'sine göre avatarı al
    val currentAvatar = remember(currentAvatarId.value, isPremium) {
        avatarService.getCurrentAvatar()
    }
    
    var userName by remember { mutableStateOf(name) }
    var selectedClass by remember { mutableStateOf(currentClass) }
    var expanded by remember { mutableStateOf(false) }

    // Honest lesson progress is available to Free and Premium users alike.
    val lessonProgressPrefs = remember(currentContext) {
        currentContext.getSharedPreferences("p2_learning_pilot_v1", Context.MODE_PRIVATE)
    }
    val classLessons = remember(selectedClass) { LearningPathCatalog.forGrade(selectedClass) }
    val finishedLessons = classLessons.count {
        lessonProgressPrefs.getBoolean("done_${it.id}", false)
    }
    
    // İstatistikleri al
    val totalScore = gameStatisticsService.getTotalScore()
    val completedGamesCount = gameStatisticsService.getCompletedGamesCount()
    val totalPlayTimeMinutes = gameStatisticsService.getTotalPlayTimeMinutes()
    val formattedPlayTime = gameStatisticsService.formatPlayTime(totalPlayTimeMinutes)
    val favoriteGame = gameStatisticsService.getFavoriteGame()
    
    val classOptions = listOf(
        "Ana Sınıfı", "1. Sınıf", "2. Sınıf", "3. Sınıf", "4. Sınıf", 
        "5. Sınıf", "6. Sınıf", "7. Sınıf", "8. Sınıf"
    )
    
    // Gradyan arka plan renkleri
    val gradientColors = listOf(
        Color(0xFF0D395D),
        Color(0xFF0A2F51),
        Color(0xFF061C35)
    )

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF0D395D))
            .safeDrawingPadding()
    ) {
        // Gradyan arka plan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = gradientColors
                    )
                )
        )
        
        Column(modifier = Modifier.fillMaxSize().padding(top = 6.dp)) {
            // Safe-area aware header: the parent already applies system insets.
            TopAppBar(
                title = {
                    Column {
                        Text("Kaşif Profilim", color = Color.White,
                            fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("Macera kimliğin ve ilerlemen",
                            color = Color(0xFFAEDAE8), fontSize = 11.sp)
                    }
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(
                        onClick = onSaveClick,
                        modifier = Modifier.padding(start = 5.dp).size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF245B77))
                            .border(1.dp, Color(0xFF5F9CB5), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kaşif profilinden geri dön",
                            tint = Color.White,
                            modifier = Modifier.size(25.dp)
                        )
                    }
                }
            )
            
            // İçerik - Scrollable yapıyoruz
            androidx.compose.foundation.rememberScrollState().let { scrollState ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .verticalScroll(scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Premium explorer identity card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = 5.dp,
                        shape = RoundedCornerShape(24.dp),
                        backgroundColor = Color(0xFF12476D),
                        border = BorderStroke(1.dp, Color(0xFFFFD47B))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar alanı
                            Box {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0B2B4B))
                                        .padding(8.dp)
                                        .border(
                                            width = 2.dp,
                                            color = Color(0xFFFFD47B),
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Animasyonlu Avatar Görünümü
                                    AnimatedAvatarView(
                                        avatar = currentAvatar,
                                        modifier = Modifier.fillMaxSize(),
                                        contentDescription = "Avatar"
                                    )
                                }
                                
                                // Düzenleme butonu
                                FloatingActionButton(
                                    onClick = onAvatarStoreClick,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .align(Alignment.BottomEnd)
                                        .offset(x = 4.dp, y = 4.dp),
                                    backgroundColor = Color(0xFF087F9D),
                                    contentColor = Color.White
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Avatar Değiştir",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = userName.ifBlank { "Matematik Gezgini" },
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = selectedClass,
                                color = Color(0xFFACE8F3),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if(isPremium) "✦ PREMIUM KAŞİF" else "✦ MATEMATİK KAŞİFİ",
                                color = Color(0xFFFFD47B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            // Yıldızlar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFFE7A8),
                                                Color(0xFFFFCE61)
                                            )
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFFFEB3B).copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(30.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Yıldızlar",
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${starValue} Yıldız",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7B6608)
                                )
                            }
                            
                            // Premium olmayan kullanıcılar için Premium butonu
                            if (!isPremium) {
                                Spacer(modifier = Modifier.height(16.dp))
                                
                                OutlinedButton(
                                    onClick = {
                                        // Premium satın alma
                                        try {
                                            val activity = context as? Activity
                                            if (activity != null) {
                                                // Direkt olarak satın alma işlemini başlat
                                                purchaseManager?.buyPremium(activity)
                                            } else {
                                                Log.e("ProfileScreen", "Context Activity değil")
                                                Toast.makeText(
                                                    context,
                                                    "Premium satın alma başlatılamadı.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("ProfileScreen", "Premium satın alma başlatılamadı: ${e.message}", e)
                                            Toast.makeText(
                                                context,
                                                "Premium satın alma başlatılamadı.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        backgroundColor = Color(0xFFFFF59D),
                                        contentColor = Color(0xFF7B6608)
                                    ),
                                    border = BorderStroke(1.dp, Color(0xFFFFB300)),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Premium",
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "Premium'a Yükselt",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Real grade-aware learning progress, not fabricated achievements.
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        elevation = 3.dp,
                        shape = RoundedCornerShape(19.dp),
                        backgroundColor = Color(0xFF174767),
                        border = BorderStroke(1.dp, Color(0xFF508FA9))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(17.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoStories, contentDescription = null,
                                    tint = Color(0xFFFFD47B), modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(9.dp))
                                Text("Öğrenme Yolculuğum", color = Color.White,
                                    fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                            }
                            Text(
                                "${selectedClass} · ${finishedLessons} / ${classLessons.size} kısa ders tamamlandı",
                                color = Color(0xFFD6EAF3), fontSize = 13.sp
                            )
                            LinearProgressIndicator(
                                progress = if (classLessons.isEmpty()) 0f
                                    else finishedLessons.toFloat() / classLessons.size,
                                modifier = Modifier.fillMaxWidth().height(7.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                color = Color(0xFF74E4D5),
                                backgroundColor = Color(0xFF315F78)
                            )
                            Text(
                                if (selectedClass.substringBefore(".").trim().toIntOrNull()?.let { it >= 5 } == true)
                                    "Şimdilik temel tekrar dersleri sunuluyor; tam sınıf müfredatı değildir."
                                else "Kısa derslerini Öğren bölümünde sürdürebilirsin.",
                                color = Color(0xFFB8D9E5), fontSize = 11.sp
                            )
                        }
                    }

                    // Bu sayfalar zaten projede mevcut; profil üzerinden tek yerde bulunurlar.
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color(0xFF12476D),
                        border = BorderStroke(1.dp, Color(0xFF367D9D))
                    ) {
                        Column(Modifier.fillMaxWidth().padding(17.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text("Kişiselleştirme ve Ayarlar",
                                color = Color(0xFFFFD47B),fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold)
                            Text("Kaşifini ve oyun deneyimini buradan düzenle.",
                                fontSize = 12.sp,color = Color(0xFFBDD9EC))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onAvatarStoreClick,
                                    modifier = Modifier.weight(1f).heightIn(min = 108.dp),
                                    border = BorderStroke(1.dp, Color(0xFF69C8DC)),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 7.dp, vertical = 12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White,
                                        backgroundColor = Color(0xFF1B5978))
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(Icons.Default.Face, contentDescription = null,
                                            tint = Color(0xFFFFD47B),
                                            modifier = Modifier.size(26.dp))
                                        Text("Avatar Galerisi", color = Color.White,
                                            fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center, maxLines = 2)
                                        Text("Karakterini seç",
                                            color = Color(0xFFBDDCE9), fontSize = 10.sp,
                                            textAlign = TextAlign.Center)
                                    }
                                }
                                OutlinedButton(
                                    onClick = onAppIconClick,
                                    modifier = Modifier.weight(1f).heightIn(min = 108.dp),
                                    border = BorderStroke(1.dp, Color(0xFF69C8DC)),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(horizontal = 7.dp, vertical = 12.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White,
                                        backgroundColor = Color(0xFF1B5978))
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(Icons.Default.Palette, contentDescription = null,
                                            tint = Color(0xFFFFD47B),
                                            modifier = Modifier.size(26.dp))
                                        Text("İkon Stüdyosu", color = Color.White,
                                            fontSize = 13.sp, fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center, maxLines = 2)
                                        Text("Uygulamanı süsle",
                                            color = Color(0xFFBDDCE9), fontSize = 10.sp,
                                            textAlign = TextAlign.Center)
                                    }
                                }
                            }
                            Button(
                                onClick = onSettingsClick,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFFFFD47B),
                                    contentColor = Color(0xFF09263F)),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Settings,"Ayarlar")
                                Spacer(Modifier.width(8.dp))
                                Text("Ayarlar · Ses, Bildirim ve Premium")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Personalization panel: compact, accessible and consistent
                    // with the dark fantasy-ocean world. No white HTML-like forms.
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                        elevation = 3.dp,
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = Color(0xFF153E5B),
                        border = BorderStroke(1.dp, Color(0xFF467D99))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(17.dp),
                            verticalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                                        .background(Color(0xFF255E79)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Person, contentDescription = null,
                                        tint = Color(0xFFFFD782), modifier = Modifier.size(23.dp))
                                }
                                Spacer(Modifier.width(11.dp))
                                Column {
                                    Text("Kaşif Bilgilerim", color = Color.White,
                                        fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("İsmini ve öğrenme seviyeni düzenle",
                                        color = Color(0xFFB7D6E4), fontSize = 11.sp)
                                }
                            }

                            OutlinedTextField(
                                value = userName,
                                onValueChange = {
                                    userName = it
                                    onNameChange(it)
                                },
                                label = {
                                    Text("Kaşif adın (takma ad)", fontSize = 12.sp)
                                },
                                placeholder = {
                                    Text("Örn. Matematik Kaşifi",
                                        color = Color(0xFF8DB5C7), fontSize = 13.sp)
                                },
                                singleLine = true,
                                leadingIcon = {
                                    Icon(Icons.Default.Face, contentDescription = null,
                                        tint = Color(0xFF84DDD7))
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    textColor = Color.White,
                                    focusedBorderColor = Color(0xFF72DCD6),
                                    unfocusedBorderColor = Color(0xFF5584A0),
                                    backgroundColor = Color(0xFF0E304A),
                                    focusedLabelColor = Color(0xFFB6F1E6),
                                    unfocusedLabelColor = Color(0xFFB5D6E5),
                                    cursorColor = Color(0xFF72DCD6)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )

                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = selectedClass,
                                    onValueChange = { },
                                    label = { Text("Sınıf düzeyi", fontSize = 12.sp) },
                                    readOnly = true,
                                    leadingIcon = {
                                        Icon(Icons.Default.School, contentDescription = null,
                                            tint = Color(0xFF84DDD7))
                                    },
                                    trailingIcon = {
                                        Icon(
                                            if (expanded) Icons.Default.KeyboardArrowUp
                                            else Icons.Default.KeyboardArrowDown,
                                            contentDescription = null,
                                            tint = Color(0xFFB6E9F0)
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        textColor = Color.White,
                                        focusedBorderColor = Color(0xFF72DCD6),
                                        unfocusedBorderColor = Color(0xFF5584A0),
                                        backgroundColor = Color(0xFF0E304A),
                                        focusedLabelColor = Color(0xFFB6F1E6),
                                        unfocusedLabelColor = Color(0xFFB5D6E5),
                                        cursorColor = Color(0xFF72DCD6)
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                // Whole class selector is a reliable touch target,
                                // not only the tiny chevron.
                                Box(
                                    Modifier.matchParentSize()
                                        .clickable { expanded = true }
                                )
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(Color(0xFF173F5D))
                                        .border(1.dp, Color(0xFF508EAA),
                                            RoundedCornerShape(12.dp))
                                ) {
                                    classOptions.forEach { option ->
                                        DropdownMenuItem(onClick = {
                                            selectedClass = option
                                            onClassChange(option)
                                            expanded = false
                                        }) {
                                            Row(
                                                Modifier.fillMaxWidth()
                                                    .clip(RoundedCornerShape(9.dp))
                                                    .background(
                                                        if (option == selectedClass)
                                                            Color(0xFF2A6580)
                                                        else Color.Transparent
                                                    )
                                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (option == selectedClass) {
                                                    Icon(Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFFD782),
                                                        modifier = Modifier.size(17.dp))
                                                    Spacer(Modifier.width(7.dp))
                                                }
                                                Text(option, color = Color.White,
                                                    fontWeight = if (option == selectedClass)
                                                        FontWeight.Bold else FontWeight.Normal)
                                            }
                                        }
                                    }
                                }
                            }
                            Text(
                                "Sınıfı değiştirince öğrenme patikandaki dersler de buna göre güncellenir. Önceki oyun kayıtların silinmez.",
                                color = Color(0xFFB6D5E2), fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(9.dp))

                    // Compact two-column explorer statistics. The source and
                    // premium-gating semantics remain unchanged.
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                        elevation = 3.dp,
                        shape = RoundedCornerShape(22.dp),
                        backgroundColor = Color(0xFF153E5B),
                        border = BorderStroke(1.dp, Color(0xFF467D99))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(17.dp),
                            verticalArrangement = Arrangement.spacedBy(11.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    Modifier.size(42.dp).clip(RoundedCornerShape(13.dp))
                                        .background(Color(0xFF255E79)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.BarChart, contentDescription = null,
                                        tint = Color(0xFFFFD782),
                                        modifier = Modifier.size(23.dp))
                                }
                                Spacer(Modifier.width(11.dp))
                                Column {
                                    Text("Macera İstatistiklerim", color = Color.White,
                                        fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                    Text("Gerçek oyun kayıtlarına dayalı özet",
                                        color = Color(0xFFB7D6E4), fontSize = 11.sp)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                StatisticsItemModern(
                                    iconRes = R.drawable.ic_star,
                                    title = "Kazanılan Yıldız",
                                    value = gameStatisticsService.getTotalEarnedStars().toString(),
                                    color = Color(0xFFFFD47B),
                                    isPremium = true,
                                    modifier = Modifier.weight(1f)
                                )
                                StatisticsItemModern(
                                    iconRes = R.drawable.ic_timer,
                                    title = "Oyun Süresi",
                                    value = formattedPlayTime,
                                    color = Color(0xFF84D7F4),
                                    isPremium = isPremium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                StatisticsItemModern(
                                    iconRes = R.drawable.ic_gamepad,
                                    title = "Biten Oyunlar",
                                    value = completedGamesCount.toString(),
                                    color = Color(0xFF8CE3BF),
                                    isPremium = isPremium,
                                    modifier = Modifier.weight(1f)
                                )
                                StatisticsItemModern(
                                    iconRes = R.drawable.ic_favorite,
                                    title = "Favori Oyun",
                                    value = favoriteGame,
                                    color = Color(0xFFFFB4C6),
                                    isPremium = isPremium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (!isPremium) {
                                Text(
                                    "Oyun süresi, biten oyunlar ve favori oyun detayları Premium ile açılır.",
                                    color = Color(0xFFFFD78A), fontSize = 11.sp,
                                    lineHeight = 16.sp
                                )
                            } else {
                                Text(
                                    "Bir istatistiğe dokunarak oyunlarına göre ayrıntıyı incele.",
                                    color = Color(0xFFB5D6E3), fontSize = 11.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(13.dp))

                    // Kaydet butonu
                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFFFFD47B),
                            contentColor = Color(0xFF102D44)
                        ),
                        shape = RoundedCornerShape(28.dp),
                        elevation = ButtonDefaults.elevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 8.dp
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Kaydet",
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "DEĞİŞİKLİKLERİ KAYDET",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Modern istatistik öğesi gösterimi
 */
@Composable
private fun StatisticsItemModern(
    iconRes: Int,
    title: String,
    value: String,
    color: Color,
    isPremium: Boolean,
    modifier: Modifier = Modifier
) {
    var showDetailPopup by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val gameStatisticsService = remember(context) {
        com.yigit033.matematikmacarasi.services.GameStatisticsService(context)
    }

    Card(
        modifier = modifier.heightIn(min = 142.dp)
            .clickable(enabled = isPremium) { showDetailPopup = true },
        shape = RoundedCornerShape(17.dp),
        backgroundColor = Color(0xFF0D2C46),
        border = BorderStroke(1.dp,
            if (isPremium) Color(0xFF427C99) else Color(0xFF355672)),
        elevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier.size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Icon(
                    imageVector = if (isPremium) Icons.Default.KeyboardArrowRight
                                  else Icons.Default.Lock,
                    contentDescription = if (isPremium) "Ayrıntıları göster"
                                         else "Premium gerekir",
                    tint = if (isPremium) Color(0xFFAED8E9) else Color(0xFFFFD47B),
                    modifier = Modifier.size(19.dp)
                )
            }
            Text(
                text = title,
                fontSize = 12.sp,
                color = Color(0xFFB9D8E6),
                fontWeight = FontWeight.Medium,
                maxLines = 2
            )
            Text(
                text = if (isPremium) value else "Premium ile açılır",
                fontSize = if (isPremium) 17.sp else 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isPremium) Color.White else Color(0xFFFFD47B),
                maxLines = 2
            )
        }
    }

    // Her istatistik kartı için uygun detay popup'ını göster
    if (showDetailPopup && isPremium) {
        when (title) {
            "Toplam Yıldız", "Kazanılan Yıldız" -> {
                val starsPerGame = gameStatisticsService.getStarsPerGameDetails()
                StatisticsDetailPopup(
                    title = "Oyunlara Göre Yıldız Dağılımı",
                    items = starsPerGame,
                    accentColor = color,
                    onDismiss = { showDetailPopup = false }
                )
                // Debug için yıldız bilgilerini logla
                Log.d("ProfileActivity", "Yıldız dağılımı: $starsPerGame")
            }
            
            "Oyun Süresi" -> {
                val timePerGame = gameStatisticsService.getGamePlayTimeDetails()
                StatisticsDetailPopup(
                    title = "Oyunlara Göre Süre Dağılımı",
                    items = timePerGame,
                    formatValue = { seconds ->
                        val minutes = seconds / 60
                        val hours = minutes / 60
                        when {
                            hours > 0 -> "$hours saat ${minutes % 60} dk"
                            minutes > 0 -> "$minutes dk"
                            else -> "$seconds sn"
                        }
                    },
                    accentColor = color,
                    onDismiss = { showDetailPopup = false }
                )
                // Debug için süre bilgilerini logla
                Log.d("ProfileActivity", "Süre dağılımı: $timePerGame")
            }
            
            "Tamamlanan Oyun", "Biten Oyunlar" -> {
                val gamesCount = gameStatisticsService.getGamePlayCountDetails()
                StatisticsDetailPopup(
                    title = "Tamamlanan Oyun Sayıları",
                    items = gamesCount,
                    formatValue = { count -> "$count kez" },
                    accentColor = color,
                    onDismiss = { showDetailPopup = false }
                )
                // Debug için oyun sayılarını logla
                Log.d("ProfileActivity", "Tamamlanan oyun sayıları: $gamesCount")
            }
            
            "Favori Oyun" -> {
                val gamesCount = gameStatisticsService.getGamePlayCountDetails()
                StatisticsDetailPopup(
                    title = "Oyun Oynama Sayıları",
                    items = gamesCount,
                    formatValue = { count -> "$count kez" },
                    accentColor = color,
                    onDismiss = { showDetailPopup = false }
                )
                // Debug için oyun sayılarını logla
                Log.d("ProfileActivity", "Oyun oynama sayıları: $gamesCount")
            }
        }
    }
}

// Yıldız sayısını getiren yardımcı fonksiyon
private fun getStarCount(context: Context): Int {
    try {
        // StarManager'dan display stars değerini al (avatar mağazasında kullanılabilen yıldızlar)
        val starManager = StarManager.getInstance(context)
        val displayStars = starManager.getDisplayStars()
        
        // Detaylı log
        Log.d("ProfileActivity", "Display yıldız sayısı alındı: $displayStars")
        
        return displayStars
    } catch (e: Exception) {
        Log.e("ProfileActivity", "Display yıldız sayısı alınırken hata: ${e.message}", e)
        return 0
    }
} 