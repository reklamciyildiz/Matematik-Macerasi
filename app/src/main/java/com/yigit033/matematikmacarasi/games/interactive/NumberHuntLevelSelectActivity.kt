package com.yigit033.matematikmacarasi.games.interactive

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.models.NumberHuntLevels
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme
import android.util.Log

/**
 * Sayı Avı oyunu için seviye seçim ekranı.
 * Bu aktivite, kullanıcının zorluk seviyesi ve kural seçmesini sağlar.
 */
class NumberHuntLevelSelectActivity : ComponentActivity() {
    
    companion object {
        private const val TAG = "NumberHuntLevelSelect"
    }
    
    // Genişletilmiş seviye, -1 hiçbiri demektir
    private var expandedLevel by mutableStateOf(-1)
    
    // Günlük görev ID'si
    private var dailyTaskId: Int = -1
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Intent'ten görev ID'sini al (varsa)
            dailyTaskId = intent.getIntExtra("daily_task_id", -1)
            
            setContent {
                MatematikMacerasiTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        LevelSelectScreen(
                            onLevelSelected = { level, ruleIndex ->
                                // Intent ile oyun aktivitesini başlat
                                try {
                                    Log.d(TAG, "Seviye $level, Kural $ruleIndex seçildi, oyun başlatılıyor...")
                                    
                                    // Seçilen kuralı kontrol et
                                    val rule = NumberHuntLevels.levelRules[level][ruleIndex]
                                    Log.d(TAG, "Seçilen kural: ${rule.rule}")
                                    
                                    // Özel kurallar için ek kontrol
                                    val isSpecialRule = rule.rule.contains("3'e VE 5'e bölünebilen") || 
                                                       rule.rule.contains("Tam kare") ||
                                                       rule.rule.contains("Rakamları toplamı 10")
                                    
                                    if (isSpecialRule) {
                                        Log.d(TAG, "Özel kural tespit edildi: ${rule.rule}")
                                    }
                                    
                                    val intent = Intent(this, NumberHuntGameActivity::class.java)
                                    intent.putExtra("selected_level", level)
                                    intent.putExtra("selected_rule_index", ruleIndex)
                                    intent.putExtra("daily_task_id", dailyTaskId)
                                    intent.putExtra("is_special_rule", isSpecialRule)
                                    
                                    // Aktiviteyi başlat
                                    startActivity(intent)
                                    finish()
                                } catch (e: Exception) {
                                    Log.e(TAG, "Oyun başlatılırken hata: ${e.message}")
                                    e.printStackTrace()
                                    
                                    // Hata durumunda basit bir seviye başlat
                                    val intent = Intent(this, NumberHuntGameActivity::class.java)
                                    intent.putExtra("selected_level", 0)
                                    intent.putExtra("selected_rule_index", 0)
                                    intent.putExtra("daily_task_id", dailyTaskId)
                                    startActivity(intent)
                                    finish()
                                }
                            },
                            onCancel = { finish() }
                        )
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Seviye seçim ekranı başlatılırken hata: ${e.message}")
            e.printStackTrace()
            finish()
        }
    }
    
    @Composable
    fun LevelSelectScreen(
        onLevelSelected: (level: Int, ruleIndex: Int) -> Unit,
        onCancel: () -> Unit
    ) {
        // Ekran arkaplanı - gradient mavi
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1976D2), // Koyu mavi üst
                            Color(0xFFBBDEFB)  // Açık mavi alt
                        )
                    )
                )
        ) {
            // Ana içerik
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Başlık
                Text(
                    text = "Sayı Avı - Seviye Seç",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                
                // Alt başlık
                Text(
                    text = "Alışveriş becerilerini test etmek için en uygun seviyeyi seç",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // Kaydırılabilir seviye listesi
                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = true),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Her zorluk seviyesi için
                    item {
                        // Seviye 1: Basit kurallar
                        LevelCard(
                            levelName = "Basit Kurallar",
                            levelDescription = "Temel sayı kavramlarıyla tanış",
                            levelColor = Color(0xFF4CAF50),
                            starCount = 1,
                            levelIndex = 0,
                            isExpanded = expandedLevel == 0,
                            rules = NumberHuntLevels.levelRules[0],
                            onExpandClick = {
                                expandedLevel = if (expandedLevel == 0) -1 else 0
                            },
                            onRuleSelected = { ruleIndex ->
                                onLevelSelected(0, ruleIndex)
                            }
                        )
                    }
                    
                    item {
                        // Seviye 2: Orta seviye kurallar
                        LevelCard(
                            levelName = "Orta Seviye Kurallar",
                            levelDescription = "Daha zorlu sayı avları",
                            levelColor = Color(0xFFFFA000),
                            starCount = 2,
                            levelIndex = 1,
                            isExpanded = expandedLevel == 1,
                            rules = NumberHuntLevels.levelRules[1],
                            onExpandClick = {
                                expandedLevel = if (expandedLevel == 1) -1 else 1
                            },
                            onRuleSelected = { ruleIndex ->
                                onLevelSelected(1, ruleIndex)
                            }
                        )
                    }
                    
                    item {
                        // Seviye 3: Zor kurallar
                        LevelCard(
                            levelName = "Zor Kurallar",
                            levelDescription = "Karmaşık sayı kurallarını keşfet",
                            levelColor = Color(0xFFFF5722),
                            starCount = 3,
                            levelIndex = 2,
                            isExpanded = expandedLevel == 2,
                            rules = NumberHuntLevels.levelRules[2],
                            onExpandClick = {
                                expandedLevel = if (expandedLevel == 2) -1 else 2
                            },
                            onRuleSelected = { ruleIndex ->
                                onLevelSelected(2, ruleIndex)
                            }
                        )
                    }
                    
                    item {
                        // Seviye 4: Çok zor kurallar
                        LevelCard(
                            levelName = "Çok Zor Kurallar",
                            levelDescription = "İleri düzey sayı avları",
                            levelColor = Color(0xFF9C27B0),
                            starCount = 4,
                            levelIndex = 3,
                            isExpanded = expandedLevel == 3,
                            rules = NumberHuntLevels.levelRules[3],
                            onExpandClick = {
                                expandedLevel = if (expandedLevel == 3) -1 else 3
                            },
                            onRuleSelected = { ruleIndex ->
                                // Özel kural kontrolü
                                val rule = NumberHuntLevels.levelRules[3][ruleIndex]
                                Log.d(TAG, "Çok Zor Kural seçildi: ${rule.rule}, Index: $ruleIndex")
                                
                                // 3'e VE 5'e bölünebilen sayılar kuralı için özel log
                                if (rule.rule.contains("3'e VE 5'e bölünebilen")) {
                                    Log.d(TAG, "DİKKAT: Özel kural '3'e VE 5'e bölünebilen sayılar' seçildi")
                                }
                                
                                onLevelSelected(3, ruleIndex)
                            }
                        )
                    }
                    
                    item {
                        // Seviye 5: Uzman kurallar
                        LevelCard(
                            levelName = "Uzman Kurallar",
                            levelDescription = "Ustalaşmış sayı avcıları için",
                            levelColor = Color(0xFF3F51B5),
                            starCount = 5,
                            levelIndex = 4,
                            isExpanded = expandedLevel == 4,
                            rules = NumberHuntLevels.levelRules[4],
                            onExpandClick = {
                                expandedLevel = if (expandedLevel == 4) -1 else 4
                            },
                            onRuleSelected = { ruleIndex ->
                                onLevelSelected(4, ruleIndex)
                            }
                        )
                    }
                }
                
                // İptal Butonu
                Button(
                    onClick = { onCancel() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF673AB7)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ana Menüye Dön", fontSize = 16.sp, color = Color.White)
                }
            }
        }
    }
    
    @Composable
    fun LevelCard(
        levelName: String,
        levelDescription: String,
        levelColor: Color,
        starCount: Int,
        levelIndex: Int,
        isExpanded: Boolean,
        rules: List<com.yigit033.matematikmacarasi.games.models.NumberHuntLevel>,
        onExpandClick: () -> Unit,
        onRuleSelected: (Int) -> Unit
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                // Seviye başlığı - tıklanabilir
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(levelColor)
                        .clickable { onExpandClick() }
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            Text(
                                text = levelName,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            Text(
                                text = levelDescription,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                        
                        // Yıldızlar
                        Row {
                            repeat(5) { index ->
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_star),
                                    contentDescription = "Yıldız",
                                    tint = if (index < starCount) Color(0xFFFFD700) else Color.White.copy(alpha = 0.3f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                // Kural listesi - genişletildiğinde görünür
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + scaleIn(initialScale = 0.9f),
                    exit = fadeOut() + scaleOut(targetScale = 0.9f)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // İçindeki kurallar
                        rules.forEachIndexed { index, rule ->
                            // Her kural için log kaydı ekle
                            Log.d(TAG, "Seviye $levelIndex, Kural $index gösteriliyor: ${rule.rule}")
                            
                            Button(
                                onClick = { 
                                    Log.d(TAG, "Seviye $levelIndex, Kural $index seçildi: ${rule.rule}")
                                    onRuleSelected(index) 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = levelColor.copy(alpha = 0.8f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = rule.rule,
                                    fontSize = 14.sp,
                                    color = Color.White,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )
                            }
                        }
                        
                        // Kural sayısını göster
                        Text(
                            text = "Toplam ${rules.size} kural",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
} 