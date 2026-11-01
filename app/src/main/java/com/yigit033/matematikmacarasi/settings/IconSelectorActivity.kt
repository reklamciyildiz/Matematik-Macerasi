package com.yigit033.matematikmacarasi.settings

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yigit033.matematikmacarasi.MainActivity
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.screens.SettingsActivity
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import kotlinx.coroutines.flow.collect

/**
 * Alternatif ikonları seçmek için kullanılan ekran
 */
class IconSelectorActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                IconSelectorScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
    
    /**
     * Uygulama ikonunu değiştir
     */
    private fun changeAppIcon(iconType:String):Boolean {
        val updated=IconLauncherManager.apply(applicationContext,iconType)
        Toast.makeText(this,
            if(updated) "İkon tercihin kaydedildi. Telefon ana ekranı birkaç saniye içinde güncellenebilir."
            else "İkon değiştirilemedi. Önceki ikon korunuyor.",
            Toast.LENGTH_LONG).show()
        return updated
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun IconSelectorScreen(onBackClick: () -> Unit) {
        val context = LocalContext.current
        val iconPreferenceManager = IconPreferenceManager.getInstance(context)
        var selectedIcon by remember { mutableStateOf(iconPreferenceManager.getSelectedIcon()) }
        
        // Premium durumunu kontrol et
        var isPremium by remember { mutableStateOf(false) }
        var showPremiumDialog by remember { mutableStateOf(false) }
        var isLoading by remember { mutableStateOf(true) }
        var selectedPremiumIcon by remember { mutableStateOf("") }
        
        // PurchaseManager'ı güvenli bir şekilde başlat
        var purchaseManager: PurchaseManager? = null
        try {
            purchaseManager = PurchaseManager.getInstance(context)
        } catch (e: Exception) {
            Log.e("IconSelector", "PurchaseManager başlatılamadı: ${e.message}")
        }
        
        // Premium durumunu kontrol et
        LaunchedEffect(Unit) {
            try {
                // Önce hızlı senkron kontrol yap (SharedPreferences üzerinden)
                isPremium = purchaseManager?.isPremiumSync() ?: false
                isLoading = false
                
                // Sonra arka planda asenkron olarak premium durumunu kontrol et
                purchaseManager?.checkPremiumStatusAsync()
                
                // Premium durumu değişimlerini dinle
                purchaseManager?.premiumState?.collect { premiumState ->
                    isPremium = premiumState
                    Log.d("IconSelector", "Premium durumu güncellendi: $isPremium")
                }
            } catch (e: Exception) {
                Log.e("IconSelector", "Premium durumu kontrol edilirken hata: ${e.message}")
                isLoading = false
            }
        }
        
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Uygulama İkonunu Değiştir",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.Filled.ArrowBack, 
                                contentDescription = "Geri",
                                tint = Color(0xFF075FA5)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.White,
                        titleContentColor = Color(0xFF075FA5)
                    )
                )
            },
            containerColor = Color(0xFFE3F4FF) // Açık mor arka plan
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE3F4FF), // Açık mor
                                Color(0xFFB9E4FF)  // Biraz daha koyu mor
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "İstediğin ikonu seç",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF075FA5),
                        modifier = Modifier.padding(bottom = 32.dp)
                    )
                    
                    // Yükleme göstergesi
                    if (isLoading) {
                        androidx.compose.material3.CircularProgressIndicator(
                            color = Color(0xFF075FA5),
                            modifier = Modifier.padding(top = 32.dp)
                        )
                    } else {
                        // Varsayılan ikon - Matematik Macerası (herkese açık)
                    ModernIconOption(
                        title = "Matematik Macerası",
                        description = "Renkli matematik macerası ikonu",
                        iconResId = R.drawable.ic_math_adventure_foreground,
                        isSelected = selectedIcon == IconPreferenceManager.ICON_DEFAULT,
                        backgroundColor = Color(0xFF2196F3), // Mavi
                            isLocked = false,
                        onSelect = {
                            if(changeAppIcon(IconPreferenceManager.ICON_DEFAULT)) {
                                        selectedIcon = IconPreferenceManager.ICON_DEFAULT
                                    }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                        // Robot ikon - Premium gerektirir
                    ModernIconOption(
                        title = "Matematik Robotu",
                        description = "Sevimli robot ikonu",
                        iconResId = R.drawable.ic_math_robot_foreground,
                        isSelected = selectedIcon == IconPreferenceManager.ICON_ROBOT,
                        backgroundColor = Color(0xFF4CAF50), // Yeşil
                            isLocked = !isPremium && selectedIcon != IconPreferenceManager.ICON_ROBOT,
                        onSelect = {
                            purchaseManager?.withPremiumCheck(
                                onPremium = {
                                    // Premium kullanıcılar için veya zaten seçili ise
                                    if(changeAppIcon(IconPreferenceManager.ICON_ROBOT)) {
                                        selectedIcon = IconPreferenceManager.ICON_ROBOT
                                    }
                                },
                                onNonPremium = {
                                    // Zaten kullandığı ikon ise değişikliğe izin ver
                                    if (selectedIcon == IconPreferenceManager.ICON_ROBOT) {
                                        if(changeAppIcon(IconPreferenceManager.ICON_ROBOT)) {
                                        selectedIcon = IconPreferenceManager.ICON_ROBOT
                                    }
                                    } else {
                                        // Premium gerektiğini göster
                                        selectedPremiumIcon = IconPreferenceManager.ICON_ROBOT
                                        showPremiumDialog = true
                                    }
                                },
                                onError = { e ->
                                    // Hata durumunda, eğer zaten kullanıyorsa izin ver
                                    if (selectedIcon == IconPreferenceManager.ICON_ROBOT) {
                                        if(changeAppIcon(IconPreferenceManager.ICON_ROBOT)) {
                                        selectedIcon = IconPreferenceManager.ICON_ROBOT
                                    }
                                    } else {
                                        Log.e("IconSelector", "Premium kontrol hatası: ${e.message}")
                                        // Premium dialogunu göster
                                        selectedPremiumIcon = IconPreferenceManager.ICON_ROBOT
                                        showPremiumDialog = true
                                    }
                                }
                            ) ?: run {
                                // PurchaseManager null ise sadece varsayılan ikona izin ver
                                if (selectedIcon == IconPreferenceManager.ICON_ROBOT) {
                                    if(changeAppIcon(IconPreferenceManager.ICON_ROBOT)) {
                                        selectedIcon = IconPreferenceManager.ICON_ROBOT
                                    }
                                } else {
                                    selectedPremiumIcon = IconPreferenceManager.ICON_ROBOT
                                    showPremiumDialog = true
                                }
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                        // Uzay ikon - Premium gerektirir
                    ModernIconOption(
                        title = "Uzay Matematik",
                            description = "Uzay temalı ikon",
                        iconResId = R.drawable.ic_math_space_foreground,
                        isSelected = selectedIcon == IconPreferenceManager.ICON_SPACE,
                        backgroundColor = Color(0xFFFF9800), // Turuncu
                            isLocked = !isPremium && selectedIcon != IconPreferenceManager.ICON_SPACE,
                        onSelect = {
                            purchaseManager?.withPremiumCheck(
                                onPremium = {
                                    // Premium kullanıcılar için veya zaten seçili ise
                                    if(changeAppIcon(IconPreferenceManager.ICON_SPACE)) {
                                        selectedIcon = IconPreferenceManager.ICON_SPACE
                                    }
                                },
                                onNonPremium = {
                                    // Zaten kullandığı ikon ise değişikliğe izin ver
                                    if (selectedIcon == IconPreferenceManager.ICON_SPACE) {
                                        if(changeAppIcon(IconPreferenceManager.ICON_SPACE)) {
                                        selectedIcon = IconPreferenceManager.ICON_SPACE
                                    }
                                    } else {
                                        // Premium gerektiğini göster
                                        selectedPremiumIcon = IconPreferenceManager.ICON_SPACE
                                        showPremiumDialog = true
                                    }
                                },
                                onError = { e ->
                                    // Hata durumunda, eğer zaten kullanıyorsa izin ver
                                    if (selectedIcon == IconPreferenceManager.ICON_SPACE) {
                                        if(changeAppIcon(IconPreferenceManager.ICON_SPACE)) {
                                        selectedIcon = IconPreferenceManager.ICON_SPACE
                                    }
                                    } else {
                                        Log.e("IconSelector", "Premium kontrol hatası: ${e.message}")
                                        // Premium dialogunu göster
                                        selectedPremiumIcon = IconPreferenceManager.ICON_SPACE
                                        showPremiumDialog = true
                                    }
                                }
                            ) ?: run {
                                // PurchaseManager null ise sadece varsayılan ikona izin ver
                                if (selectedIcon == IconPreferenceManager.ICON_SPACE) {
                                    if(changeAppIcon(IconPreferenceManager.ICON_SPACE)) {
                                        selectedIcon = IconPreferenceManager.ICON_SPACE
                                    }
                                } else {
                                    selectedPremiumIcon = IconPreferenceManager.ICON_SPACE
                                    showPremiumDialog = true
                                }
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Text(
                        text = "İkon değişikliği uygulamayı yeniden başlatmadan yapılır. Ana ekranın güncellenmesi telefon modeline göre gecikebilir.",
                        fontSize = 14.sp,
                        color = Color(0xFF666666),
                            textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
        
        // Premium bilgilendirme dialogu
        if (showPremiumDialog) {
            Dialog(onDismissRequest = { showPremiumDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Premium ikonu
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .background(
                                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                                    shape = CircleShape
                                )
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Premium",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Premium Özellik",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF075FA5)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Bu ikon mevcut Premium üyeliğin parçasıdır. Erişimin varsa yeniden satın alma yapma; üyelik durumunu Ayarlar'dan kontrol et.",
                            fontSize = 16.sp,
                            color = Color(0xFF666666),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { showPremiumDialog = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE0E0E0),
                                    contentColor = Color(0xFF666666)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Vazgeç",
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Button(
                                onClick = {
                                    // The icon picker never initiates purchases directly.
                                    // Return to the canonical subscription/settings surface.
                                    showPremiumDialog = false
                                    context.startActivity(Intent(context,SettingsActivity::class.java))
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFD700),
                                    contentColor = Color(0xFF075FA5)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Premium Bilgileri",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    fun ModernIconOption(
        title: String,
        description: String,
        iconResId: Int,
        isSelected: Boolean,
        backgroundColor: Color,
        isLocked: Boolean,
        onSelect: () -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable { onSelect() },
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(
                defaultElevation = if (isSelected) 8.dp else 4.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = if (isSelected) {
                androidx.compose.foundation.BorderStroke(2.dp, backgroundColor)
            } else {
                null
            }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // İkon Konteyneri
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .background(
                            backgroundColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = backgroundColor.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp)
                        .alpha(if (isLocked) 0.6f else 1f),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = iconResId),
                        contentDescription = title,
                        modifier = Modifier.size(64.dp)
                    )
                    
                    // Kilit ikonu (eğer kilitliyse)
                    if (isLocked) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Kilitli",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // İkon Detayları
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF212121)
                    )
                    
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = Color(0xFF757575)
                    )
                    
                    // Premium gereklilik bilgisi - isLocked true olduğunda her ikonu için de göster
                    if (isLocked) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Premium",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(16.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(4.dp))
                            
                            Text(
                                text = "Premium Özellik",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFFFFD700)
                            )
                        }
                    }
                }
                
                // Seçim İkonu
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(backgroundColor, shape = CircleShape),
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
            }
        }
    }
} 