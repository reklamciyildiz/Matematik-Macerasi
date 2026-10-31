package com.yigit033.matematikmacarasi.games

import android.content.Intent
import android.os.Bundle
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.interactive.BalloonPopGameActivity
import com.yigit033.matematikmacarasi.games.interactive.MarketSimulatorActivity
import com.yigit033.matematikmacarasi.games.interactive.MathMazeGameActivity
import com.yigit033.matematikmacarasi.games.interactive.NumberHuntGameActivity
import com.yigit033.matematikmacarasi.games.interactive.SpaceMathAdventureActivity
import com.yigit033.matematikmacarasi.ui.components.StaticBackground
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import com.yigit033.matematikmacarasi.services.DailyGameLimitService
import com.yigit033.matematikmacarasi.services.payments.PurchaseManager
import android.util.Log

class GameSelectionActivity : ComponentActivity() {
    
    private lateinit var dailyGameLimitService: DailyGameLimitService
    private var purchaseManager: PurchaseManager? = null
    
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Servisleri başlat
        dailyGameLimitService = DailyGameLimitService.getInstance(this)
        try {
            purchaseManager = PurchaseManager.getInstance(this)
        } catch (e: Exception) {
            Log.e("GameSelectionActivity", "PurchaseManager başlatılamadı: ${e.message}", e)
        }
        
        // Get the category from the intent
        val category = intent.getStringExtra("category") ?: "interactive"
        
        setContent {
            MatematikMacerasiTheme {
                // Kategori ismini belirle
                val screenTitle = when(category) {
                    "shapes" -> "Şekiller & Matematik"
                    "algorithm" -> "Algoritma & Matematik"
                    "real" -> "Gerçek & Matematik"
                    "interactive" -> "Eğlence & Matematik"
                    else -> "Oyun Seçimi"
                }
                
                // Kategori rengini belirle
                val headerColor = when(category) {
                    "shapes" -> Color(0xFF009688)
                    "algorithm" -> Color(0xFF9C27B0)
                    "real" -> Color(0xFFFF5722)
                    "interactive" -> Color(0xFF673AB7)
                    else -> Color(0xFF3F51B5)
                }
                
                // Kategori için gradient arka plan renklerini belirle
                val backgroundGradient = when(category) {
                    "shapes" -> Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE0F2F1),  // Çok açık yeşil (turkuaz)
                            Color(0xFFB2DFDB),  // Açık yeşil (turkuaz)
                            Color(0xFF80CBC4),  // Orta yeşil (turkuaz)
                            Color(0xFF009688)   // Koyu yeşil (turkuaz - ana renk)
                        )
                    )
                    "algorithm" -> Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF3E5F5),  // Çok açık mor-pembe
                            Color(0xFFE1BEE7),  // Açık mor-pembe
                            Color(0xFFCE93D8),  // Orta mor-pembe
                            Color(0xFF9C27B0)   // Koyu mor-pembe (ana renk)
                        )
                    )
                    "real" -> Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFBE9E7),  // Çok açık turuncu
                            Color(0xFFFFCCBC),  // Açık turuncu
                            Color(0xFFFF8A65),  // Orta turuncu
                            Color(0xFFFF5722)   // Koyu turuncu (ana renk)
                        )
                    )
                    "interactive" -> Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFEDE7F6),  // Çok açık mor
                            Color(0xFFD1C4E9),  // Açık mor
                            Color(0xFFB39DDB),  // Orta mor
                            Color(0xFF673AB7)   // Koyu mor (ana renk)
                        )
                    )
                    else -> Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE3F2FD),  // Çok açık mavi
                            Color(0xFFBBDEFB),  // Açık mavi
                            Color(0xFF90CAF9),  // Orta mavi
                            Color(0xFF2196F3)   // Koyu mavi
                        )
                    )
                }
                
                // Bilgi ikonuna tıklandığında popup göstermek için state
                var showInfoDialog by remember { mutableStateOf(false) }
                
                // Tüm ekran için gradient arka plan
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(backgroundGradient)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(top = 8.dp)
                        ) {
                            // Üst bar
                            TopAppBar(
                                title = { 
                                    Text(
                                        text = screenTitle, 
                                        color = Color.White,
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Medium
                                    ) 
                                },
                                navigationIcon = {
                                    IconButton(onClick = { finish() }) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                            contentDescription = "Geri Dön",
                                            tint = Color.White
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { showInfoDialog = true }) {
                                        Icon(
                                            imageVector = Icons.Filled.Info,
                                            contentDescription = "Bilgi",
                                            tint = Color.White
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = headerColor
                                )
                            )
                            
                            // Bilgi popup dialogu
                            if (showInfoDialog) {
                                GameInfoDialog(
                                    onDismiss = { showInfoDialog = false },
                                    category = category,
                                    categoryColor = headerColor
                                )
                            }
                            
                            // Ana içerik
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            ) {
                                // Başlık
                                Text(
                                    text = "Oyun Seçimi",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                
                                Text(
                                    text = "Oynamak istediğin oyunu seç",
                                    fontSize = 16.sp,
                                    color = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.padding(bottom = 16.dp)
                                )
                                
                                // Kategori içeriğini göster
                                when(category) {
                                    "interactive" -> InteractiveGamesContent()
                                    "shapes" -> ShapesGamesContent()
                                    "algorithm" -> AlgorithmGamesContent()
                                    "real" -> RealWorldGamesContent()
                                    else -> InteractiveGamesContent()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    @Composable
    fun InteractiveGamesContent() {
        // İnteraktif oyunlar için gradient renkler
        val interactiveGradientColors = listOf(
            Color(0xFFEDE7F6),  // Çok açık mor
            Color(0xFFD1C4E9),  // Açık mor
            Color(0xFFB39DDB),  // Orta mor
            Color(0xFF673AB7)   // Koyu mor (ana renk)
        )
        
        // Günlük sınır durumları
        var isPremium by remember { mutableStateOf(false) }
        var mathMazeRemainingPlays by remember { mutableStateOf(10) }
        var canPlayMathMaze by remember { mutableStateOf(true) }
        
        // Premium durumunu kontrol et
        LaunchedEffect(Unit) {
            try {
                purchaseManager?.let { pm ->
                    isPremium = pm.isPremiumSync()
                    mathMazeRemainingPlays = dailyGameLimitService.getRemainingMathMazePlays(isPremium)
                    canPlayMathMaze = dailyGameLimitService.canPlayMathMaze(isPremium)
                }
            } catch (e: Exception) {
                Log.e("GameSelectionActivity", "Premium durum kontrolü hatası: ${e.message}", e)
            }
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = interactiveGradientColors
                    )
                )
                .padding(16.dp)
        ) {
            // İlk satır
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Balon Patlatma
                                    GameCard(
                                        icon = R.drawable.ic_add,
                                        iconTint = Color(0xFFE91E63),
                                        title = "Balon Patlatma",
                                        description = "Balonları patlatarak matematik problemi çöz",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            startActivity(Intent(this@GameSelectionActivity, BalloonPopGameActivity::class.java))
                                        }
                                    )
                                    
                                    // Matematik Uzay
                                    GameCard(
                                        icon = R.drawable.ic_spaceship,
                                        iconTint = Color(0xFF673AB7),
                                        title = "Matematik Uzay",
                                        description = "Uzay ve Matematik",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            startActivity(Intent(this@GameSelectionActivity, SpaceMathAdventureActivity::class.java))
                                        }
                                    )
                                }
                                
            // İkinci satır
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Sayı Avı
                                    GameCard(
                                        icon = R.drawable.ic_arrow_left,
                                        iconTint = Color(0xFF4CAF50),
                                        title = "Sayı Avı",
                                        description = "Sayıları karşılaştır ve İlişkiyi yakala",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            startActivity(Intent(this@GameSelectionActivity, NumberHuntGameActivity::class.java))
                                        }
                                    )
                                    
                                    // Matematik Labirenti - Günlük sınır bilgisi ile
                                    MathMazeGameCard(
                                        modifier = Modifier.weight(1f),
                                        remainingPlays = mathMazeRemainingPlays,
                                        isPremium = isPremium,
                                        canPlay = canPlayMathMaze,
                                        onClick = {
                                            if (canPlayMathMaze) {
                                                startActivity(Intent(this@GameSelectionActivity, MathMazeGameActivity::class.java))
                                            } else {
                                                // Premium satın alma önerisi göster
                                                try {
                                                    purchaseManager?.buyPremium(this@GameSelectionActivity)
                                                } catch (e: Exception) {
                                                    Log.e("GameSelectionActivity", "Premium satın alma başlatılamadı: ${e.message}", e)
                                                }
                                            }
                                        }
                                    )
                                }
                                
            // Yeni oyunlar için mesaj
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Uygulamaya olan ilgi arttığı zaman çeşitli eğlence oyunları eklenecektir :)",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    @Composable
    fun ShapesGamesContent() {
        // Şekiller oyunları için gradient renkler
        val shapesGradientColors = listOf(
            Color(0xFFE0F2F1),  // Çok açık yeşil (turkuaz)
            Color(0xFFB2DFDB),  // Açık yeşil (turkuaz)
            Color(0xFF80CBC4),  // Orta yeşil (turkuaz)
            Color(0xFF009688)   // Koyu yeşil (turkuaz - ana renk)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = shapesGradientColors
                    )
                )
                .padding(16.dp)
        ) {
            // Şekil temalı oyunlar için boş sayfayı göster
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Şekil Tanıma
                GameCard(
                    icon = R.drawable.ic_shapes,
                    iconTint = Color(0xFF009688),
                    title = "Şekil Tanıma",
                    description = "Şekilleri tanı ve özelliklerini öğren",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        // Şekil tanıma oyunu buraya eklenecek
                        // startActivity(Intent(this@GameSelectionActivity, ShapeRecognitionGameActivity::class.java))
                        val intent = Intent(this@GameSelectionActivity, ComingSoonActivity::class.java)
                        intent.putExtra("game_name", "Şekil Tanıma")
                        intent.putExtra("color_code", 0xFF009688.toInt()) // turkuaz renk
                        intent.putExtra("game_description", "geometrik şekilleri tanıma ve özellikleri öğrenme")
                        startActivity(intent)
                    }
                )
                
                // Geometri Macerası
                GameCard(
                    icon = R.drawable.ic_shapes,
                    iconTint = Color(0xFF009688),
                    title = "Geometri Macerası",
                    description = "Geometrik şekiller ve açılar",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        // Geometri oyunu buraya eklenecek
                        // startActivity(Intent(this@GameSelectionActivity, GeometryAdventureActivity::class.java))
                        val intent = Intent(this@GameSelectionActivity, ComingSoonActivity::class.java)
                        intent.putExtra("game_name", "Geometri Macerası")
                        intent.putExtra("color_code", 0xFF009688.toInt()) // turkuaz renk
                        intent.putExtra("game_description", "geometrik şekilleri ve açıları öğrenme")
                        startActivity(intent)
                    }
                )
            }
            
            // Gelecekteki şekil oyunları için mesaj
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Yukarıdaki oyunlar semboliktir, uygulamaya olan ilgi arttığı zaman çeşitli şekil oyunları eklenecektir :)",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    @Composable
    fun AlgorithmGamesContent() {
        // Algoritma oyunları için gradient renkler
        val algorithmGradientColors = listOf(
            Color(0xFFF3E5F5),  // Çok açık mor-pembe
            Color(0xFFE1BEE7),  // Açık mor-pembe
            Color(0xFFCE93D8),  // Orta mor-pembe
            Color(0xFF9C27B0)   // Koyu mor-pembe (ana renk)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = algorithmGradientColors
                    )
                )
                .padding(16.dp)
        ) {
            // Algoritma temalı oyunlar için boş sayfayı göster
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Algoritma Labirenti
                GameCard(
                    icon = R.drawable.ic_algorithm,
                    iconTint = Color(0xFF9C27B0),
                    title = "Algoritma Labirenti",
                    description = "Adımları doğru sırayla takip et",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        // Algoritma oyunu buraya eklenecek
                        // startActivity(Intent(this@GameSelectionActivity, AlgorithmMazeActivity::class.java))
                        val intent = Intent(this@GameSelectionActivity, ComingSoonActivity::class.java)
                        intent.putExtra("game_name", "Algoritma Labirenti")
                        intent.putExtra("color_code", 0xFF9C27B0.toInt()) // mor renk
                        intent.putExtra("game_description", "algoritmik düşünme ve mantıksal adımları izleme")
                        startActivity(intent)
                    }
                )
                
                // Kod Bloğu Yap-Boz
                GameCard(
                    icon = R.drawable.ic_question,
                    iconTint = Color(0xFF9C27B0),
                    title = "Kod Blokları",
                    description = "Kodları doğru sırayla yerleştir",
                    modifier = Modifier.weight(1f),
                    onClick = {
                        // Kod blokları oyunu buraya eklenecek
                        // startActivity(Intent(this@GameSelectionActivity, CodeBlocksActivity::class.java))
                        val intent = Intent(this@GameSelectionActivity, ComingSoonActivity::class.java)
                        intent.putExtra("game_name", "Kod Blokları")
                        intent.putExtra("color_code", 0xFF9C27B0.toInt()) // mor renk
                        intent.putExtra("game_description", "kodlama mantığını anlama ve uygulama")
                        startActivity(intent)
                    }
                )
            }
            
            // Gelecekteki algoritma oyunları için mesaj
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Yukarıdaki oyunlar semboliktir, uygulamaya olan ilgi arttığı zaman çeşitli algoritma oyunları eklenecektir :)",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    @Composable
    fun RealWorldGamesContent() {
        // Gerçek hayat oyunları için gradient renkler
        val realWorldGradientColors = listOf(
            Color(0xFFFBE9E7),  // Çok açık turuncu
            Color(0xFFFFCCBC),  // Açık turuncu
            Color(0xFFFF8A65),  // Orta turuncu
            Color(0xFFFF5722)   // Koyu turuncu (ana renk)
        )
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = realWorldGradientColors
                    )
                )
                .padding(16.dp)
        ) {
            // Gerçek hayat temalı oyunlar için sayfayı göster
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Market Alışverişi Simülatörü
                                    GameCard(
                                        icon = R.drawable.ic_shopping_cart,
                    iconTint = Color(0xFFFF5722),
                                        title = "Market Alışverişi",
                                        description = "Alışveriş yaparak matematik öğren",
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            startActivity(Intent(this@GameSelectionActivity, MarketSimulatorActivity::class.java))
                                        }
                                    )
                                    
                                    // Boş kart (gelecekteki oyunlar için)
                                    Spacer(modifier = Modifier.weight(1f))
                                }
            
            // Gelecekteki gerçek hayat oyunları için mesaj
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Uygulamaya olan ilgi arttığı zaman daha da farklı gerçek hayat oyunları eklenecektir :) ",
                    fontSize = 16.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun GameCard(
    icon: Int,
    iconTint: Color,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(icon),
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = description,
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

/**
 * Oyun bilgilerini gösteren dialog
 */
@Composable
fun GameInfoDialog(
    onDismiss: () -> Unit,
    category: String,
    categoryColor: Color
) {
    // Kategori adını belirle
    val categoryTitle = when(category) {
        "shapes" -> "Şekiller & Matematik"
        "algorithm" -> "Algoritma & Matematik"
        "real" -> "Gerçek & Matematik"
        "interactive" -> "Eğlence & Matematik"
        else -> "Oyun Bilgisi"
    }
    
    // Kategori ikonunu belirle
    val categoryIcon = when(category) {
        "shapes" -> R.drawable.ic_shapes
        "algorithm" -> R.drawable.ic_algorithm
        "real" -> R.drawable.ic_clock
        "interactive" -> R.drawable.ic_fun
        else -> R.drawable.ic_question
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
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
                // Üst başlık bölümü - kategori
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Kategori ikonu
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = categoryIcon),
                            contentDescription = categoryTitle,
                            tint = categoryColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Kategori başlığı
                    Text(
                        text = categoryTitle,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = categoryColor
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Ana bilgi metni
                Text(
                    text = "Yıldız Sistemi ve Avatarlar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Yıldız bilgisi
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(categoryColor.copy(alpha = 0.1f))
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 3 yıldız ikonu
                            repeat(3) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Oyunlarda seviye sayısı çok önemlidir. Mesela bir oyunda 5 seviye varsa..",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = " Unutma! Her seviyeden en fazla 3 yıldız kazanabilirsin",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Buna göre o oyundan toplam 15 yıldız (3 × 5) toplayabilirsin",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Avatar bilgisi
                Text(
                    text = "Yeni avatarları açmak için tüm oyunlarda yıldızları topla!",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Kapat butonu
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(categoryColor)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "OK",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Matematik Labirenti oyunu için özel kart - günlük sınır bilgisi ile
 */
@Composable
fun MathMazeGameCard(
    modifier: Modifier = Modifier,
    remainingPlays: Int,
    isPremium: Boolean,
    canPlay: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (canPlay) Color.White else Color.Gray.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2196F3).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_question),
                    contentDescription = "Matematik Labirenti",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Matematik Labirenti",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            if (isPremium) {
                Text(
                    text = "Sınırsız Oyun ✨",
                    fontSize = 12.sp,
                    color = Color(0xFF4CAF50),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            } else {
                Text(
                    text = if (canPlay) "Kalan: $remainingPlays/5" else "Günlük limit doldu",
                    fontSize = 12.sp,
                    color = if (canPlay) Color(0xFF2196F3) else Color.Red,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
            
            if (!canPlay && !isPremium) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Premium'a geç",
                    fontSize = 10.sp,
                    color = Color(0xFFFF9800),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
} 