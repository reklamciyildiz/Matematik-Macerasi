package com.yigit033.matematikmacarasi.screens

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.services.AudioService
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Ayarlar ekranı aktivitesi.
 * Ses, müzik gibi uygulama ayarlarını yönetir.
 */
class SettingsActivity : ComponentActivity() {
    private var audioService: AudioService? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("SettingsActivity", "======== BAŞLATILIYOR ========")
        Log.d("SettingsActivity", "onCreate başladı")
        
        // AudioService'i başlatmayı dene
        try {
            Log.d("SettingsActivity", "AudioService başlatılıyor")
            audioService = AudioService.getInstance(applicationContext)
            Log.d("SettingsActivity", "AudioService başlatıldı: ${audioService != null}")
        } catch (e: Exception) {
            Log.e("SettingsActivity", "AudioService başlatılamadı: ${e.message}", e)
            // AudioService oluşturma hatası fakat devam ediyoruz
        }
        
        // Çağıran aktiviteyi log'la
        val callerActivity = intent.getStringExtra("caller_activity") ?: "Bilinmiyor"
        Log.d("SettingsActivity", "Çağıran aktivite: $callerActivity")
        
        // Tüm hataları yakalamak için genel try-catch bloğu
        try {
            initializeContent()
        } catch (e: Exception) {
            Log.e("SettingsActivity", "Ayarlar aktivitesi başlatılırken beklenmeyen hata: ${e.message}", e)
            // Basit hata ekranı göster
            setContentForErrorState(e.message ?: "Bilinmeyen bir hata oluştu")
        }
    }
    
    private fun setContentForErrorState(errorMessage: String) {
        setContent {
            MatematikMacerasiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Üzgünüz, ayarlar yüklenirken bir hata oluştu.",
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = errorMessage,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { finish() }
                        ) {
                            Text("Geri Dön")
                        }
                    }
                }
            }
        }
    }
    
    private fun initializeContent() {
        setContent {
            Log.d("SettingsActivity", "setContent içinde")
            
            // SnackbarHostState tanımla
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            
            // Hata ve yükleme durumu için state'ler
            var isLoading by remember { mutableStateOf(true) }
            var errorOccurred by remember { mutableStateOf(false) }
            var errorMessage by remember { mutableStateOf("") }
            
            // State değiştiğinde log
            LaunchedEffect(isLoading, errorOccurred) {
                Log.d("SettingsActivity", "State değişti: isLoading=$isLoading, errorOccurred=$errorOccurred")
            }
            
            // AudioService hazırlama
            val safeAudioService = remember { 
                audioService ?: try {
                    AudioService.getInstance(applicationContext)
                } catch (e: Exception) {
                    Log.e("SettingsActivity", "AudioService yeniden başlatılamadı: ${e.message}", e)
                    errorOccurred = true
                    errorMessage = "AudioService başlatılamadı: ${e.message}"
                    null
                }
            }
            
            // SettingsScreen gösterme durumu için state
            var showSettingsScreenError by remember { mutableStateOf(false) }
            var settingsScreenErrorMessage by remember { mutableStateOf("") }
            
            MatematikMacerasiTheme {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { paddingValues ->
                    // Surface yerine Box kullanıp gradient background ekleyeceğiz
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFE3F2FD), // Çok açık mavi (üst)
                                        Color(0xFFBBDEFB), // Açık mavi (orta)
                                        Color(0xFF90CAF9), // Orta mavi
                                        Color(0xFF2196F3)  // Koyu mavi (alt)
                                    )
                                )
                            )
                    ) {
                        when {
                            isLoading -> {
                                // Yükleme ekranı
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(color = Color.White)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text("Ayarlar yükleniyor...", color = Color.White)
                                    }
                                }
                                
                                // 1 saniye sonra yüklemeyi bitir
                                LaunchedEffect(Unit) {
                                    delay(1000)
                                    isLoading = false
                                }
                            }
                            errorOccurred -> {
                                // Hata ekranı
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Ayarlar yüklenirken hata oluştu",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        Text(
                                            text = errorMessage,
                                            textAlign = TextAlign.Center,
                                            color = Color.White.copy(alpha = 0.8f)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Button(onClick = { finish() }) {
                                            Text("Ana Sayfaya Dön")
                                        }
                                    }
                                }
                            }
                            showSettingsScreenError -> {
                                // SettingsScreen hatası gösterimi
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth(0.8f)
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = "Ayarlar ekranı yüklenirken bir sorun oluştu",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        if (settingsScreenErrorMessage.isNotEmpty()) {
                                            Text(
                                                text = settingsScreenErrorMessage,
                                                textAlign = TextAlign.Center,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                            
                                            Spacer(modifier = Modifier.height(16.dp))
                                        }
                                        
                                        Button(onClick = { finish() }) {
                                            Text("Ana Sayfaya Dön")
                                        }
                                    }
                                }
                            }
                            else -> {
                                // safeAudioService varsa SettingsScreen göster
                                if (safeAudioService != null) {
                                    // Composable içinde try-catch kullanamayız, LaunchedEffect ile hataları yakalayabiliriz
                                    var hasError by remember { mutableStateOf(false) }
                                    var errorMessage by remember { mutableStateOf("") }
                                    
                                    LaunchedEffect(safeAudioService) {
                                        try {
                                            // Hazırlık işlemleri burada yapılabilir
                                            Log.d("SettingsActivity", "SettingsScreen oluşturuluyor")
                                        } catch (e: Exception) {
                                            Log.e("SettingsActivity", "SettingsScreen hazırlık hatası: ${e.message}", e)
                                            hasError = true
                                            errorMessage = e.message ?: "Bilinmeyen bir hata oluştu"
                                            
                                            scope.launch {
                                                snackbarHostState.showSnackbar("Ayarlar ekranı yüklenemedi: ${e.message}")
                                            }
                                        }
                                    }
                                    
                                    if (!hasError) {
                                        SettingsScreen(
                                            onBackClick = { finish() },
                                            audioService = safeAudioService
                                        )
                                    } else {
                                        // Hata olursa basit bir hata ekranı göster
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = "Ayarlar ekranı yüklenemedi",
                                                color = Color.White,
                                                fontSize = 18.sp,
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                textAlign = TextAlign.Center
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Text(
                                                text = errorMessage,
                                                textAlign = TextAlign.Center,
                                                color = Color.White.copy(alpha = 0.8f)
                                            )
                                            
                                            Spacer(modifier = Modifier.height(16.dp))
                                            
                                            Button(onClick = { finish() }) {
                                                Text("Ana Sayfaya Dön")
                                            }
                                        }
                                    }
                                } else {
                                    // AudioService yoksa hata mesajı göster
                                    Log.e("SettingsActivity", "AudioService null olduğu için SettingsScreen gösterilemiyor")
                                    
                                    Column(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "Ses servisi başlatılamadı",
                                            color = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Button(onClick = { finish() }) {
                                            Text("Ana Sayfaya Dön")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Hata yakalama için LaunchedEffect
            LaunchedEffect(Unit) {
                try {
                    // Burada bir şey yapmıyoruz, sadece olası hataları yakalamak için
                } catch (e: Exception) {
                    Log.e("SettingsActivity", "Global LaunchedEffect içinde hata: ${e.message}", e)
                    errorOccurred = true
                    errorMessage = e.message ?: "Bilinmeyen hata"
                }
            }
            
            Log.d("SettingsActivity", "onCreate tamamlandı")
        }
    }
    
    override fun onResume() {
        super.onResume()
        Log.d("SettingsActivity", "onResume")
    }
    
    override fun onPause() {
        super.onPause()
        Log.d("SettingsActivity", "onPause")
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("SettingsActivity", "onDestroy")
    }
} 