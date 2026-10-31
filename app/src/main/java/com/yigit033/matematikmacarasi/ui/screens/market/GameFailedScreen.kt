package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Oyun başarısız ekranı
 * 
 * @param reason Başarısızlık nedeni
 * @param onPlayAgain Tekrar oyna butonuna tıklandığında çağrılacak fonksiyon
 * @param onBackToMenu Ana menüye dön butonuna tıklandığında çağrılacak fonksiyon
 */
@Composable
fun GameFailedScreen(
    reason: String,
    onPlayAgain: () -> Unit,
    onBackToMenu: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Başlık
            Text(
                text = "Oyun Bitti",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Başarısızlık nedeni
            Text(
                text = reason,
                fontSize = 18.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // İpucu kartı
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "İpucu",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = getTipForFailure(reason),
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Tekrar oyna butonu
            Button(
                onClick = onPlayAgain,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Tekrar Dene",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
                
                Text(
                    text = "Tekrar Dene",
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Ana menüye dön butonu
            OutlinedButton(
                onClick = onBackToMenu,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Ana Menü",
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp)
                )
                
                Text(
                    text = "Ana Menüye Dön",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 16.sp
                )
            }
        }
    }
}

/**
 * Başarısızlık nedenine göre ipucu döndürür
 * 
 * @param reason Başarısızlık nedeni
 * @return İpucu metni
 */
private fun getTipForFailure(reason: String): String {
    return when {
        reason.contains("süre", ignoreCase = true) -> 
            "Zamanı daha iyi yönetmek için önce alışveriş listesini incele ve ürünleri kategorilerine göre topla."
        
        reason.contains("bütçe", ignoreCase = true) -> 
            "Bütçeni aşmamak için sepet tutarını sürekli kontrol et ve gerekirse bazı ürünleri sepetten çıkar."
        
        reason.contains("liste", ignoreCase = true) || reason.contains("ürün", ignoreCase = true) -> 
            "Alışveriş listesindeki tüm ürünleri topladığından emin ol. Listeyi düzenli olarak kontrol et."
        
        else -> 
            "Bir sonraki denemende daha dikkatli ol. Zamanı, bütçeyi ve alışveriş listesini iyi yönet."
    }
} 