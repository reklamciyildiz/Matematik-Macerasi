package com.yigit033.matematikmacarasi.ui.screens.market

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.yigit033.matematikmacarasi.R

/**
 * Oyun tanıtım diyaloğu
 * 
 * @param onDismiss Diyalog kapatıldığında çağrılacak fonksiyon
 */
@Composable
fun GameTutorialDialog(
    onDismiss: () -> Unit,
    onNext: () -> Unit,
    currentPage: Int = 1
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (currentPage) {
                    1 -> TutorialPage1()
                    2 -> TutorialPage2()
                    3 -> TutorialPage3()
                }

                // İlerleme noktaları
                Row(
                    modifier = Modifier.padding(vertical = 24.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(3) { index ->
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (index == currentPage - 1) Color(0xFF6200EA)
                                    else Color(0xFFE0E0E0),
                                    CircleShape
                                )
                        )
                        if (index < 2) Spacer(modifier = Modifier.width(8.dp))
                    }
                }

                // İleri butonu
                Button(
                    onClick = onNext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6200EA)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentPage < 3) "İleri" else "Başla",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = if (currentPage < 3) "İleri" else "Başla",
                        tint = Color.White,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                // Atla linki
                Text(
                    text = "Atla",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF666666),
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .clickable(onClick = onDismiss)
                )
            }
        }
    }
}

/**
 * Birinci tutorial sayfası - Oyunun amacı
 */
@Composable
private fun TutorialPage1() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Başlık
        Text(
            text = "Oyunu Tanıyalım",
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF6200EA),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Market ikonu
        Image(
            painter = painterResource(id = R.drawable.market_tutorial_icon),
            contentDescription = "Market Oyunu",
            modifier = Modifier
                .size(120.dp)
                .padding(vertical = 16.dp)
        )
        
        // Açıklama
        Text(
            text = "Market Alışverişi Oyunu",
            style = MaterialTheme.typography.titleMedium,
            color = Color(0xFF6200EA),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Bu oyunda bir alışveriş listesindeki ürünleri marketten toplayacak ve bütçeni doğru kullanarak alışverişi tamamlayacaksın!",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * İkinci tutorial sayfası - Nasıl oynanır
 */
@Composable
private fun TutorialPage2() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "Nasıl Oynanır?",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Adımlar
        TutorialStep(
            number = 1,
            text = "Alışveriş listesini kontrol et ve hangi ürünleri alman gerektiğine bak."
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TutorialStep(
            number = 2,
            text = "Ürünleri kategorilerine göre filtrele ve alışveriş listesindeki ürünleri sepete ekle."
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TutorialStep(
            number = 3,
            text = "Bütçeni aşmadan alışveriş listendeki tüm ürünleri topla."
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        TutorialStep(
            number = 4,
            text = "İşin bittiğinde kasaya git ve ödeme yap. Para üstünü hesapla."
        )
    }
}

/**
 * Üçüncü tutorial sayfası - İpuçları
 */
@Composable
private fun TutorialPage3() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Text(
            text = "İpuçları",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // İpuçları
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F7FF)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                TutorialTip(
                    text = "Bütçeni kontrol etmeyi unutma! Fazla para harcamamaya dikkat et."
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TutorialTip(
                    text = "Kilogram ile satılan ürünleri (meyve, sebze) istenen miktarda almaya çalış."
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TutorialTip(
                    text = "Alışveriş listesine istediğin zaman tıklayarak tam listeyi görebilirsin."
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                TutorialTip(
                    text = "Zamanın yetişmezse oyunu kaybedersin, hızlı hareket et!"
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Artık alışverişe hazırsın!",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/**
 * Tutorial adım komponenti
 */
@Composable
private fun TutorialStep(
    number: Int,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // Adım numarası
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // Adım açıklaması
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Tutorial ipucu komponenti
 */
@Composable
private fun TutorialTip(
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        // İpucu ikonu
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // İpucu metni
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.DarkGray,
            modifier = Modifier.weight(1f)
        )
    }
} 