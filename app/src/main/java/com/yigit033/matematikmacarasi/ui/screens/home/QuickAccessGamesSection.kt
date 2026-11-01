package com.yigit033.matematikmacarasi.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.ComingSoonActivity
import com.yigit033.matematikmacarasi.games.geometry.GeometryCastleActivity
import com.yigit033.matematikmacarasi.games.basic.AdditionGameActivity
import com.yigit033.matematikmacarasi.games.interactive.BalloonPopGameActivity
import com.yigit033.matematikmacarasi.games.interactive.MarketSimulatorActivity
import com.yigit033.matematikmacarasi.games.interactive.MathMazeGameActivity
import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAvi2Activity
import com.yigit033.matematikmacarasi.games.interactive.SpaceMathAdventureActivity

data class PremiumGameCardItem(
    val id: String,
    val title: String,
    @DrawableRes val imageRes: Int,
    val bgGradient: List<Color>,
    val borderColor: Color,
    val activityClass: Class<*>
)

/**
 * Onaylı Referansa Sadık "Hızlı Erişim" Bölümü (Sprint H2)
 *
 * Referans: reference/HOME_APPROVED_REFERENCE.png
 * - Başlık: ⚡ "Hızlı Erişim" ve "Tüm Oyunlar ▶"
 * - ZIP paketinden gelen gerçek 4 oyun illüstrasyonu (Dört İşlem, Balon Patlatma, Sayı Avı, Uzay Macerası)
 * - Canlı turuncu, mavi, yeşil ve mor kart yüzeyleri
 * - Yatay kaydırma (1.8-2.3 kart görünür)
 * - Tıklamada gerçek Activity'yi doğrudan başlatır
 */
@Composable
fun QuickAccessGamesSection(
    onSeeAllClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val primaryCards = listOf(
        PremiumGameCardItem(
            id = "four_operations",
            title = "Dört İşlem\nOyunları",
            imageRes = R.drawable.card_four_operations,
            bgGradient = listOf(Color(0xFFFF7A00), Color(0xFFFF3D00)),
            borderColor = Color(0xFFFFB74D),
            activityClass = AdditionGameActivity::class.java
        ),
        PremiumGameCardItem(
            id = "balloon_pop",
            title = "Balon\nPatlatma",
            imageRes = R.drawable.card_balloon_pop,
            bgGradient = listOf(Color(0xFF00B4D8), Color(0xFF0077B6)),
            borderColor = Color(0xFF67E8F9),
            activityClass = BalloonPopGameActivity::class.java
        ),
        PremiumGameCardItem(
            id = "number_hunt",
            title = "Sayı Avı",
            imageRes = R.drawable.card_number_hunt,
            bgGradient = listOf(Color(0xFF10B981), Color(0xFF059669)),
            borderColor = Color(0xFF6EE7B7),
            activityClass = SayiAvi2Activity::class.java
        ),
        PremiumGameCardItem(
            id = "space_math",
            title = "Uzay\nMacerası",
            imageRes = R.drawable.card_space_adventure,
            bgGradient = listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
            borderColor = Color(0xFFC4B5FD),
            activityClass = SpaceMathAdventureActivity::class.java
        ),
        // Ek mini oyunlar (katalog bütünlüğü için)
        PremiumGameCardItem(
            id = "math_maze",
            title = "Matematik\nLabirenti",
            imageRes = R.drawable.card_balloon_pop,
            bgGradient = listOf(Color(0xFF7C3AED), Color(0xFF5B21B6)),
            borderColor = Color(0xFFA78BFA),
            activityClass = MathMazeGameActivity::class.java
        ),
        PremiumGameCardItem(
            id = "market_sim",
            title = "Pazar\nSimülatörü",
            imageRes = R.drawable.card_number_hunt,
            bgGradient = listOf(Color(0xFF059669), Color(0xFF065F46)),
            borderColor = Color(0xFF34D399),
            activityClass = MarketSimulatorActivity::class.java
        ),
        PremiumGameCardItem(
            id = "shapes",
            title = "Şekiller &\nGeometri",
            imageRes = R.drawable.card_four_operations,
            bgGradient = listOf(Color(0xFF0284C7), Color(0xFF0369A1)),
            borderColor = Color(0xFF38BDF8),
            activityClass = GeometryCastleActivity::class.java
        )
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        // Başlık Satırı (Referanstaki sarı şimşek + "Hızlı Erişim" + "Tüm Oyunlar ▶")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Bolt,
                    contentDescription = null,
                    tint = HomeColors.StarGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Hızlı Erişim",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSeeAllClick() }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Tüm Oyunlar",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(3.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Yatay Kayan Canlı Renkli Kartlar
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(primaryCards, key = { it.id }) { card ->
                IllustratedGameCard(
                    card = card,
                    onClick = {
                        val intent = Intent(context, card.activityClass)
                        if (card.activityClass == ComingSoonActivity::class.java) {
                            intent.putExtra("game_name", "Şekiller & Geometri")
                            intent.putExtra("is_category", true)
                        }
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
private fun IllustratedGameCard(
    card: PremiumGameCardItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(148.dp)
            .height(188.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                spotColor = card.bgGradient.first().copy(alpha = 0.45f)
            )
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(card.bgGradient))
            .border(2.dp, card.borderColor.copy(alpha = 0.8f), RoundedCornerShape(22.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Üst İllüstrasyon (1254x1254 HD sanat varlığı)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(126.dp)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(18.dp))
            ) {
                Image(
                    painter = painterResource(id = card.imageRes),
                    contentDescription = card.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Alt Başlık (Referanstaki beyaz kalın tipografi)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = card.title,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
