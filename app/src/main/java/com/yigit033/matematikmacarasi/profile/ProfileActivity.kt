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
import com.yigit033.matematikmacarasi.services.StarManager
import android.app.Activity
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
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
        
        // Initialize services
        sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        avatarService = AvatarService(this)
        gameStatisticsService = GameStatisticsService.getInstance(this)
        starManager = StarManager.getInstance(this)
        
        // Kullanıcı bilgilerini yükle
        loadUserData()
        
        setContent {
            MatematikMacerasiTheme {
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
    val currentAvatar = remember(currentAvatarId.value) { avatarService.getCurrentAvatar() }
    
    var userName by remember { mutableStateOf(name) }
    var selectedClass by remember { mutableStateOf(currentClass) }
    var expanded by remember { mutableStateOf(false) }
    
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
        Color(0xFF81D4FA), // Açık mavi
        Color(0xFFB3E5FC), // Daha açık mavi
        Color(0xFFE1F5FE)  // En açık mavi
    )

    Box(
        modifier = Modifier.fillMaxSize()
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
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Toolbar
            TopAppBar(
                title = { 
                    Text(
                        "Profil Düzenle",
                        color = Color(0xFF1565C0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ) 
                },
                backgroundColor = Color.Transparent,
                elevation = 0.dp,
                navigationIcon = {
                    IconButton(onClick = onSaveClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = Color(0xFF1565C0)
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
                    // Profil Kartı
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color.White
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
                                        .background(Color(0xFFE3F2FD))
                                        .padding(8.dp)
                                        .border(
                                            width = 2.dp,
                                            color = Color(0xFF2196F3),
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
                                    backgroundColor = Color(0xFF2196F3),
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
                            
                            // Yıldızlar
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                Color(0xFFFFF9C4),
                                                Color(0xFFFFF59D)
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
                    
                    // Bu sayfalar zaten projede mevcut; profil üzerinden tek yerde bulunurlar.
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color.White
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(9.dp)) {
                            Text("Kişiselleştirme ve Ayarlar",
                                color = Color(0xFF1565C0),fontSize = 18.sp,
                                fontWeight = FontWeight.Bold)
                            Text("Oyuncunu ve oyun deneyimini buradan düzenle.",
                                fontSize = 12.sp,color = Color.DarkGray)
                            OutlinedButton(
                                onClick = onAvatarStoreClick,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                border = BorderStroke(1.dp,Color(0xFF38BDF8)),
                                shape = RoundedCornerShape(13.dp)
                            ) {
                                Icon(Icons.Default.Face,"Avatar",tint = Color(0xFF1565C0))
                                Spacer(Modifier.width(8.dp))
                                Text("Avatarımı Değiştir",color = Color(0xFF1565C0))
                            }
                            OutlinedButton(
                                onClick = onAppIconClick,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
                                border = BorderStroke(1.dp,Color(0xFF38BDF8)),
                                shape = RoundedCornerShape(13.dp)
                            ) {
                                Icon(Icons.Default.Palette,"Uygulama ikonu",tint = Color(0xFF1565C0))
                                Spacer(Modifier.width(8.dp))
                                Text("Uygulama İkonunu Değiştir",color = Color(0xFF1565C0))
                            }
                            Button(
                                onClick = onSettingsClick,
                                modifier = Modifier.fillMaxWidth().heightIn(min = 50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = Color(0xFF1565C0),contentColor = Color.White),
                                shape = RoundedCornerShape(13.dp)
                            ) {
                                Icon(Icons.Default.Settings,"Ayarlar")
                                Spacer(Modifier.width(8.dp))
                                Text("Ayarlar · Ses, Bildirim ve Premium")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Kullanıcı Bilgileri Kartı
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                        ) {
                            // Başlık
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Kişisel Bilgiler",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Kişisel Bilgiler",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                            }
                            
                            // İsim alanı
                            OutlinedTextField(
                                value = userName,
                                onValueChange = { 
                                    userName = it
                                    onNameChange(it)
                                },
                                label = { 
                                    Text(
                                        "Ad - Soyad",
                                        color = Color(0xFF2196F3),
                                        fontSize = 14.sp
                                    ) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "İsim",
                                        tint = Color(0xFF2196F3)
                                    )
                                },
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    focusedBorderColor = Color(0xFF2196F3),
                                    unfocusedBorderColor = Color(0xFFBDBDBD),
                                    backgroundColor = Color.White,
                                    focusedLabelColor = Color(0xFF2196F3),
                                    unfocusedLabelColor = Color(0xFF757575),
                                    cursorColor = Color(0xFF2196F3)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            
                            // Sınıf seçici
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                OutlinedTextField(
                                    value = selectedClass,
                                    onValueChange = { },
                                    label = { 
                                        Text(
                                            "Sınıf",
                                            color = Color(0xFF2196F3),
                                            fontSize = 14.sp
                                        ) 
                                    },
                                    readOnly = true,
                                    trailingIcon = {
                                        Icon(
                                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Sınıf Seçimi",
                                            modifier = Modifier.clickable { expanded = !expanded },
                                            tint = Color(0xFF2196F3)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = "Sınıf",
                                            tint = Color(0xFF2196F3)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { expanded = !expanded },
                                    colors = TextFieldDefaults.outlinedTextFieldColors(
                                        focusedBorderColor = Color(0xFF2196F3),
                                        unfocusedBorderColor = Color(0xFFBDBDBD),
                                        backgroundColor = Color.White,
                                        focusedLabelColor = Color(0xFF2196F3),
                                        unfocusedLabelColor = Color(0xFF757575),
                                        cursorColor = Color(0xFF2196F3)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.9f)
                                        .background(Color.White)
                                        .border(
                                            width = 1.dp,
                                            color = Color(0xFFBDBDBD),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                ) {
                                    classOptions.forEach { option ->
                                        DropdownMenuItem(
                                            onClick = {
                                                selectedClass = option
                                                onClassChange(option)
                                                expanded = false
                                            }
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(8.dp)
                                                    .background(
                                                        if (option == selectedClass) Color(0xFFE3F2FD) else Color.White,
                                                        RoundedCornerShape(4.dp)
                                                    )
                                            ) {
                                                if (option == selectedClass) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color(0xFF2196F3),
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                }
                                                
                                                Text(
                                                    text = option,
                                                    color = if (option == selectedClass) Color(0xFF2196F3) else Color.Black
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // İstatistikler Kartı - Tek kart ama premium durumuna göre içeriği değişiyor
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        elevation = 2.dp,
                        shape = RoundedCornerShape(16.dp),
                        backgroundColor = Color.White
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            // Başlık
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.BarChart,
                                    contentDescription = "İstatistikler",
                                    tint = Color(0xFF2196F3),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "İstatistikler",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1565C0)
                                )
                            }
                            
                            // Toplam Yıldız her kullanıcı için görünür
                            StatisticsItemModern(
                                iconRes = R.drawable.ic_star,
                                title = "Toplam Yıldız",
                                value = gameStatisticsService.getTotalEarnedStars().toString(),
                                color = Color(0xFFF9A825),
                                isPremium = true // Yıldız sayısı her zaman görünür - premium kontrolü yok
                            )
                            
                            // Diğer istatistikler premium kontrolüne tabi
                            StatisticsItemModern(
                                iconRes = R.drawable.ic_timer,
                                title = "Oyun Süresi",
                                value = formattedPlayTime,
                                color = Color(0xFF42A5F5),
                                isPremium = isPremium
                            )
                            
                            StatisticsItemModern(
                                iconRes = R.drawable.ic_gamepad,
                                title = "Tamamlanan Oyun",
                                value = completedGamesCount.toString(),
                                color = Color(0xFF66BB6A),
                                isPremium = isPremium
                            )
                            
                            StatisticsItemModern(
                                iconRes = R.drawable.ic_favorite,
                                title = "Favori Oyun",
                                value = favoriteGame,
                                color = Color(0xFFEC407A),
                                isPremium = isPremium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Kaydet butonu
                    Button(
                        onClick = onSaveClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = Color(0xFF2196F3),
                            contentColor = Color.White
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
                            text = "KAYDET",
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
    isPremium: Boolean
) {
    // Popup görüntüleme durumu
    var showDetailPopup by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val gameStatisticsService = remember { com.yigit033.matematikmacarasi.services.GameStatisticsService(context) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(enabled = isPremium) {
                if (isPremium) {
                    showDetailPopup = true
                } else {
                    // Premium olmayan kullanıcılar için bir şey yapma
                    // Zaten isPremium kontrolü ile tıklama engellenmiş olacak
                }
            },
        elevation = 1.dp,
        shape = RoundedCornerShape(8.dp),
        backgroundColor = Color.White,
        border = BorderStroke(
            width = 1.dp,
            color = Color(0xFFEEEEEE)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Ana içerik
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // İkon arka planı
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(color.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Başlık ve değer
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    
                    // Premium değilse gizli değer göster
                    Text(
                        text = if (isPremium) value else "●●●●",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                }
                
                // Tıklanabilir olduğunu gösteren sağ ikon
                if (isPremium) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowRight,
                        contentDescription = "Detayları Göster",
                        tint = color,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Premium badge - sadece premium olmayan kullanıcılar için
            if (!isPremium) {
                // Premium kilit overlay'i
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.White.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .background(
                                color = Color(0xFFFFF59D),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Premium",
                            tint = Color(0xFF7B6608),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Premium Özellik",
                            color = Color(0xFF7B6608),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
    
    // Her istatistik kartı için uygun detay popup'ını göster
    if (showDetailPopup && isPremium) {
        when (title) {
            "Toplam Yıldız" -> {
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
            
            "Tamamlanan Oyun" -> {
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