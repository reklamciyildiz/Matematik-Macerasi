package com.yigit033.matematikmacarasi.games

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.ui.theme.MatematikMacerasiTheme

/**
 * Henüz tamamlanmamış oyunlar veya kategoriler için "Yapım Aşamasında" ekranını gösteren aktivite
 */
class ComingSoonActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Intent'ten bilgileri al
        val gameName = intent.getStringExtra("game_name") ?: "Bu Oyun"
        val colorCode = intent.getIntExtra("color_code", 0xFF3F51B5.toInt())
        val gameDescription = intent.getStringExtra("game_description") ?: "matematik öğrenme oyunu"
        val isCategory = intent.getBooleanExtra("is_category", false)
        
        val headerColor = Color(colorCode)
        
        setContent {
            MatematikMacerasiTheme {
                // Gradient arka plan renkleri
                val backgroundGradient = Brush.verticalGradient(
                    colors = listOf(
                        headerColor.copy(alpha = 0.1f),
                        headerColor.copy(alpha = 0.2f),
                        headerColor.copy(alpha = 0.3f),
                        headerColor.copy(alpha = 0.4f)
                    )
                )
                
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
                                        text = gameName, 
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
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = headerColor
                                )
                            )
                            
                            // Ana içerik
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                // Yapım aşamasında mesajı
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color.White
                                    ),
                                    elevation = CardDefaults.cardElevation(
                                        defaultElevation = 4.dp
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // İkon
                                        val iconResource = when {
                                            isCategory && gameName.contains("Şekiller") -> R.drawable.ic_shapes
                                            isCategory && gameName.contains("Algoritma") -> R.drawable.ic_algorithm
                                            else -> R.drawable.ic_construction
                                        }
                                        
                                        Icon(
                                            painter = painterResource(id = iconResource),
                                            contentDescription = null,
                                            tint = headerColor,
                                            modifier = Modifier.size(64.dp)
                                        )
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        Text(
                                            text = "Yapım Aşamasında",
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = headerColor,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Spacer(modifier = Modifier.height(16.dp))
                                        
                                        if (isCategory) {
                                            // Kategori için özel mesaj
                                            Text(
                                                text = "$gameName kategorisi henüz yapım aşamasındadır.",
                                                fontSize = 18.sp,
                                                color = Color.DarkGray,
                                                textAlign = TextAlign.Center
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Text(
                                                text = "Bu kategori, çocukların $gameDescription eğlenceli bir şekilde öğrenmelerini sağlayacak birbirinden öğretici ve eğlenceli oyunlar içerecektir.",
                                                fontSize = 16.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center
                                            )
                                        } else {
                                            // Oyun için mesaj
                                            Text(
                                                text = "$gameName oyunu henüz yapım aşamasındadır.",
                                                fontSize = 18.sp,
                                                color = Color.DarkGray,
                                                textAlign = TextAlign.Center
                                            )
                                            
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            Text(
                                                text = "Bu oyun, çocukların $gameDescription becerilerini eğlenceli bir şekilde geliştirmek için tasarlanmaktadır.",
                                                fontSize = 16.sp,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.height(24.dp))
                                        
                                        Text(
                                            text = "Uygulamamıza gösterilen ilgi arttıkça yeni oyunlar ve kategoriler eklenecektir. Desteğiniz için teşekkür ederiz!",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.DarkGray,
                                            textAlign = TextAlign.Center
                                        )
                                        
                                        Spacer(modifier = Modifier.height(24.dp))
                                        
                                        Button(
                                            onClick = { finish() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = headerColor
                                            ),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = "Ana Sayfaya Dön",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Medium,
                                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
} 