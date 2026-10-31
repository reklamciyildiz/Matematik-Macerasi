package com.yigit033.matematikmacarasi.services.payments

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import kotlinx.coroutines.launch

/**
 * Satın alma sistemini test etmek için özel aktivite
 */
class BillingTestActivity : ComponentActivity() {
    private lateinit var purchaseManager: PurchaseManager
    private val TAG = "BillingTestActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            Log.d(TAG, "🚀 BillingTestActivity başlatılıyor...")
            
            // PurchaseManager'ı güvenli şekilde başlat
            purchaseManager = PurchaseManager.getInstance(this)
            Log.d(TAG, "✅ PurchaseManager başarıyla başlatıldı")
            
            setContent {
                MatematikMacerasiTheme {
                    BillingTestScreen(
                        purchaseManager = purchaseManager,
                        activity = this
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ BillingTestActivity başlatılırken hata: ${e.message}", e)
            Toast.makeText(this, "Test aktivitesi başlatılamadı: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingTestScreen(
    purchaseManager: PurchaseManager,
    activity: ComponentActivity
) {
    val context = LocalContext.current
    var testResults by remember { mutableStateOf(listOf<String>()) }
    var isPremium by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Güvenli test result ekleme fonksiyonu
    fun safeAddTestResult(message: String) {
        try {
            val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date())
            testResults = testResults + "[$timestamp] $message"
            Log.d("BillingTest", message)
        } catch (e: Exception) {
            Log.e("BillingTest", "Test result eklenirken hata: ${e.message}", e)
        }
    }
    
    // Premium durumunu güvenli şekilde dinle
    LaunchedEffect(Unit) {
        try {
            purchaseManager.premiumState.collect { premium ->
                isPremium = premium
                safeAddTestResult("Premium durum güncellendi: $premium")
            }
        } catch (e: Exception) {
            Log.e("BillingTest", "Premium state dinlenirken hata: ${e.message}", e)
            safeAddTestResult("❌ Premium durum dinleme hatası: ${e.message}")
        }
    }
    
    // Satın alma durumunu güvenli şekilde dinle
    LaunchedEffect(Unit) {
        try {
            purchaseManager.purchaseStatus.observe(activity) { status ->
                isLoading = false
                val message = when (status) {
                    is PurchaseManager.PurchaseStatus.Success -> "✅ Satın alma başarılı!"
                    is PurchaseManager.PurchaseStatus.Pending -> "⏳ Satın alma beklemede..."
                    is PurchaseManager.PurchaseStatus.Canceled -> "❌ Satın alma iptal edildi"
                    is PurchaseManager.PurchaseStatus.AlreadyOwned -> "ℹ️ Zaten premium sürüme sahipsiniz"
                    is PurchaseManager.PurchaseStatus.ServiceUnavailable -> "❌ Billing servisi kullanılamıyor"
                    is PurchaseManager.PurchaseStatus.ProductNotFound -> "❌ Ürün bulunamadı"
                    is PurchaseManager.PurchaseStatus.Failed -> "❌ Satın alma başarısız: ${status.code}"
                }
                
                safeAddTestResult(message)
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("BillingTest", "Purchase status dinlenirken hata: ${e.message}", e)
            safeAddTestResult("❌ Satın alma durumu dinleme hatası: ${e.message}")
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Billing Test") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Premium Durum Kartı
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPremium) Color(0xFF4CAF50) else Color(0xFFFF9800)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Premium Durum",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = if (isPremium) "✅ Premium Aktif" else "❌ Premium Değil",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
            
            // Test Butonları
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Test İşlemleri",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Button(
                        onClick = {
                            try {
                                isLoading = true
                                safeAddTestResult("🔄 Premium durumu kontrol ediliyor...")
                                purchaseManager.checkPremiumStatusAsync()
                            } catch (e: Exception) {
                                isLoading = false
                                safeAddTestResult("❌ Premium kontrol hatası: ${e.message}")
                                Log.e("BillingTest", "Premium kontrol hatası", e)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text("Premium Durumu Kontrol Et")
                    }
                    
                    Button(
                        onClick = {
                            try {
                                isLoading = true
                                safeAddTestResult("🔄 Satın almalar sorgulanıyor...")
                                purchaseManager.queryPurchases()
                            } catch (e: Exception) {
                                isLoading = false
                                safeAddTestResult("❌ Satın alma sorgulama hatası: ${e.message}")
                                Log.e("BillingTest", "Satın alma sorgulama hatası", e)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        Text("Satın Almaları Sorgula")
                    }
                    
                    Button(
                        onClick = {
                            try {
                                isLoading = true
                                safeAddTestResult("🛒 Premium satın alma başlatılıyor...")
                                Log.d("BillingTest", "🛒 Premium satın alma butonu tıklandı")
                                purchaseManager.buyPremium(activity)
                            } catch (e: Exception) {
                                isLoading = false
                                safeAddTestResult("❌ Satın alma başlatma hatası: ${e.message}")
                                Log.e("BillingTest", "Satın alma başlatma hatası", e)
                                Toast.makeText(context, "Satın alma hatası: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading && !isPremium,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF4081)
                        )
                    ) {
                        Text("Premium Satın Al")
                    }
                    
                    Button(
                        onClick = {
                            try {
                                safeAddTestResult("🔄 Premium durumu sıfırlandı (test için)")
                                purchaseManager.resetPremiumStatus()
                            } catch (e: Exception) {
                                safeAddTestResult("❌ Premium sıfırlama hatası: ${e.message}")
                                Log.e("BillingTest", "Premium sıfırlama hatası", e)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF5722)
                        )
                    ) {
                        Text("Premium Durumu Sıfırla (Test)")
                    }
                    
                    Button(
                        onClick = {
                            testResults = emptyList()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Gray
                        )
                    ) {
                        Text("Sonuçları Temizle")
                    }
                }
            }
            
            // Test Sonuçları
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Test Sonuçları",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    if (testResults.isEmpty()) {
                        Text(
                            text = "Henüz test sonucu yok",
                            color = Color.Gray
                        )
                    } else {
                        testResults.takeLast(10).forEach { result ->
                            Text(
                                text = "• $result",
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            
            // Yükleme Göstergesi
            if (isLoading) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF2196F3)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "İşlem devam ediyor...",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
} 