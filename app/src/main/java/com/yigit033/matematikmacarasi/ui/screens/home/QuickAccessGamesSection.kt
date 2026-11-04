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
import com.yigit033.matematikmacarasi.games.fractions.FractionCrystalsActivity
import com.yigit033.matematikmacarasi.games.time.TimeTowerActivity
import com.yigit033.matematikmacarasi.games.pattern.PatternForestActivity
import com.yigit033.matematikmacarasi.games.basic.OperationsHubActivity
import com.yigit033.matematikmacarasi.games.interactive.BalloonPopGameActivity
import com.yigit033.matematikmacarasi.games.interactive.MarketSimulatorActivity
import com.yigit033.matematikmacarasi.games.interactive.MathMazeGameActivity
import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAvi2Activity
import com.yigit033.matematikmacarasi.games.interactive.SpaceMathAdventureActivity

data class PremiumGameCardItem(
    val game:GameCatalogItem,
    val bgGradient:List<Color>,
    val borderColor:Color
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
    recentlyOpenedId: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // The visual palettes are presentation only. Catalog metadata, routes and covers
    // come from GameCatalog so the home carousel and full game grid cannot diverge.
    val styles=listOf(
        Triple("operations",listOf(Color(0xFFFF7A00),Color(0xFFFF3D00)),Color(0xFFFFB74D)),
        Triple("balloon",listOf(Color(0xFF00B4D8),Color(0xFF0077B6)),Color(0xFF67E8F9)),
        Triple("hunt",listOf(Color(0xFF10B981),Color(0xFF059669)),Color(0xFF6EE7B7)),
        Triple("space",listOf(Color(0xFF8B5CF6),Color(0xFF6D28D9)),Color(0xFFC4B5FD)),
        Triple("maze",listOf(Color(0xFF7C3AED),Color(0xFF5B21B6)),Color(0xFFA78BFA)),
        Triple("market",listOf(Color(0xFF059669),Color(0xFF065F46)),Color(0xFF34D399)),
        Triple("fractions",listOf(Color(0xFF6D53CC),Color(0xFF1D6EA9)),Color(0xFFBBA5FF)),
        Triple("pattern_forest",listOf(Color(0xFF158D80),Color(0xFF0B675D)),Color(0xFF7FE8B9)),
        Triple("time_tower",listOf(Color(0xFFE58C35),Color(0xFFAD5E3D)),Color(0xFFFFD98A)),
        Triple("geometry",listOf(Color(0xFF0284C7),Color(0xFF0369A1)),Color(0xFF38BDF8))
    )
    val originalCards=styles.mapNotNull{(key,gradient,border)->
        GameCatalog.byId(key)?.let{PremiumGameCardItem(it,gradient,border)}
    }
    val preferredId=when(recentlyOpenedId){
        "add","subtract","multiply","divide" -> "operations"
        else -> recentlyOpenedId
    }
    val primaryCards=if(preferredId==null)originalCards else
        originalCards.sortedBy{if(it.game.id==preferredId)0 else 1}

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
            items(primaryCards, key = { it.game.id }) { card ->
                IllustratedGameCard(
                    card = card,
                    onClick = { GameLaunchHistory.open(context,card.game) }
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
                    painter = painterResource(id = card.game.coverRes),
                    contentDescription = card.game.quickTitle,
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
                    text = card.game.quickTitle,
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
