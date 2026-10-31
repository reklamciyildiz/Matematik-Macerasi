package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LearnTopicPreview(
    val title: String,
    val description: String,
    val subtopics: List<String>
)

/**
 * "Öğren" Sekmesi Görünümü
 *
 * İnteraktif öğrenme modüllerinin durumunu dürüstçe sunar; sahte tamamlanma
 * veya çalışmayan butonlar içermez. Gelecek müfredatı önizletir.
 */
@Composable
fun LearnTabScreen(
    userClass: String,
    onNavigateToGames: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val topics = listOf(
        LearnTopicPreview(
            title = "Doğal Sayılar & Ritmik Sayma",
            description = "Sayı basamakları, ileri-geri ritmik sayma ve sayı örüntülerini görselleştir.",
            subtopics = listOf("100'e kadar ritmik sayma", "Basamak ve sayı değeri", "Sayı doğruları")
        ),
        LearnTopicPreview(
            title = "Dört İşlem Stratejileri",
            description = "Elden toplama, onluk bozarak çıkarma ve zihinden işlem ipuçları.",
            subtopics = listOf("Toplama kolaylıkları", "Çıkarma püf noktaları", "Tahmin ve kontrol")
        ),
        LearnTopicPreview(
            title = "Çarpım Tablosu Dünyası",
            description = "Çarpma işlemini ezberlemek yerine görsel modeller ve mantıkla kavra.",
            subtopics = listOf("Grup ve dizi modelleri", "9'lar parmak kuralı", "Çarpma hafıza kartları")
        ),
        LearnTopicPreview(
            title = "Kesirler ve Görsel Parçalar",
            description = "Bütün, yarım, çeyrek ve birim kesir kavramlarını pizza ve şekillerle öğren.",
            subtopics = listOf("Eşit parçalara bölme", "Basit kesir modelleri", "Sayı doğrusunda kesirler")
        ),
        LearnTopicPreview(
            title = "Geometri ve Uzamsal Düşünme",
            description = "2D ve 3D geometrik cisimler, köşe, yüzey ve simetri kavramları.",
            subtopics = listOf("Kare, dikdörtgen, üçgen", "Küp ve prizmalar", "Simetri çizgileri")
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Üst Başlık
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.AutoStories,
                contentDescription = null,
                tint = HomeColors.StarAmber,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Öğrenme Portalı",
                    color = HomeColors.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Kısa dersler, gerçek alıştırmalar ve kazanım takibi",
                    color = HomeColors.TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        LearningPracticeSection(userClass = userClass)
        Spacer(modifier = Modifier.height(20.dp))

        // Gelecek dersler; içerik henüz kullanıma açılmadı.
        // Bilgilendirme Bannerı
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E3550))
                .border(1.dp, HomeColors.StarAmber.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.Construction,
                    contentDescription = null,
                    tint = HomeColors.StarAmber,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Yeni Dersler Hazırlanıyor",
                        color = HomeColors.StarGold,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Yukarıdaki iki kısa ders kullanılabilir. Aşağıdaki geniş konu başlıkları henüz açılmadı.",
                        color = HomeColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Gelecek İçerikler",
            color = HomeColors.TextAccentCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Konular Listesi
        topics.forEach { topic ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(HomeColors.CardSurface)
                    .border(1.dp, HomeColors.CardBorderSubtle, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = topic.title,
                            color = HomeColors.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF263345))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Çok Yakında",
                                color = HomeColors.TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = topic.description,
                        color = HomeColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Alt Konu Etiketleri
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        topic.subtopics.take(2).forEach { sub ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF0F2642))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "• $sub",
                                    color = HomeColors.TextAccentCyan,
                                    fontSize = 10.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Oyunlara Yönlendirme Kartı
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(HomeColors.CardSurface)
                .border(1.dp, HomeColors.MagicCyan.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Pratik Yapmaya Devam Et!",
                    color = HomeColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Derslerden sonra mevcut oyunlarda pratik yapmaya devam edebilirsin.",
                    color = HomeColors.TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onNavigateToGames,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = HomeColors.MagicCyan
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF071426),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Oyunlara Git",
                        color = Color(0xFF071426),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

