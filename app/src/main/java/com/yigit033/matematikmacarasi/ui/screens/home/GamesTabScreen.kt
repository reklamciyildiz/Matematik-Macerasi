package com.yigit033.matematikmacarasi.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.games.basic.AdditionGameActivity
import com.yigit033.matematikmacarasi.games.basic.DivisionGameActivity
import com.yigit033.matematikmacarasi.games.basic.MultiplicationGameActivity
import com.yigit033.matematikmacarasi.games.basic.SubtractionGameActivity
import com.yigit033.matematikmacarasi.games.ComingSoonActivity
import com.yigit033.matematikmacarasi.games.geometry.GeometryCastleActivity
import com.yigit033.matematikmacarasi.games.interactive.BalloonPopGameActivity
import com.yigit033.matematikmacarasi.games.interactive.MarketSimulatorActivity
import com.yigit033.matematikmacarasi.games.interactive.MathMazeGameActivity
import com.yigit033.matematikmacarasi.games.sayiavi2.SayiAvi2Activity
import com.yigit033.matematikmacarasi.games.interactive.SpaceMathAdventureActivity

data class GameCatalogItem(
    val title: String,
    val description: String,
    val category: String,
    @DrawableRes val iconRes: Int,
    val gradientColors: List<Color>,
    val activityClass: Class<*>
)

/**
 * "Oyunlar" Sekmesi Tam Ekran Görünümü
 *
 * Mevcut 10 oyunun tamamını erişilebilir, kategorize edilmiş ve çalışır durumda sunar.
 */
@Composable
fun GamesTabScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val basicGames = listOf(
        GameCatalogItem(
            title = "Toplama Oyunu",
            description = "Sayıları birleştir, adımları hızlandır ve yıldızları topla.",
            category = "Temel İşlem",
            iconRes = R.drawable.ic_addition,
            gradientColors = listOf(Color(0xFF1D4ED8), Color(0xFF3B82F6)),
            activityClass = AdditionGameActivity::class.java
        ),
        GameCatalogItem(
            title = "Çıkarma Oyunu",
            description = "Farkı bul, eksilen sayıları hesapla ve maceraya devam et.",
            category = "Temel İşlem",
            iconRes = R.drawable.ic_subtraction,
            gradientColors = listOf(Color(0xFFBE123C), Color(0xFFF43F5E)),
            activityClass = SubtractionGameActivity::class.java
        ),
        GameCatalogItem(
            title = "Çarpma Oyunu",
            description = "Çarpım tablosunu eğlenerek ezberle, hızlı cevaplarla rekor kır.",
            category = "Temel İşlem",
            iconRes = R.drawable.ic_multiplication,
            gradientColors = listOf(Color(0xFF047857), Color(0xFF10B981)),
            activityClass = MultiplicationGameActivity::class.java
        ),
        GameCatalogItem(
            title = "Bölme Oyunu",
            description = "Paylaştır, böl ve kalanı hesapla. Bölme ustalığını kanıtla.",
            category = "Temel İşlem",
            iconRes = R.drawable.ic_division,
            gradientColors = listOf(Color(0xFFB45309), Color(0xFFF59E0B)),
            activityClass = DivisionGameActivity::class.java
        )
    )

    val adventureGames = listOf(
        GameCatalogItem(
            title = "Balon Patlatma",
            description = "Doğru sonucu taşıyan balonları patlat, puanları katla.",
            category = "Macera & Eğlence",
            iconRes = R.drawable.ic_balloon_task,
            gradientColors = listOf(Color(0xFF6D28D9), Color(0xFFA78BFA)),
            activityClass = BalloonPopGameActivity::class.java
        ),
        GameCatalogItem(
            title = "Sayı Avcıları",
            description = "Kabukları grupla, beş farklı görevde matematikle hazineyi keşfet.",
            category = "Macera & Eğlence",
            iconRes = R.drawable.ic_comparison_task,
            gradientColors = listOf(Color(0xFF0F766E), Color(0xFF14B8A6)),
            activityClass = SayiAvi2Activity::class.java
        ),
        GameCatalogItem(
            title = "Uzay Macerası",
            description = "Roketinle uzayda matematik görevlerini çözerek ilerle.",
            category = "Macera & Eğlence",
            iconRes = R.drawable.ic_spaceship,
            gradientColors = listOf(Color(0xFF3730A3), Color(0xFF6366F1)),
            activityClass = SpaceMathAdventureActivity::class.java
        ),
        GameCatalogItem(
            title = "Matematik Labirenti",
            description = "Labirentte gizli doğru yolları matematik sorularıyla aç.",
            category = "Macera & Eğlence",
            iconRes = R.drawable.ic_gamepad,
            gradientColors = listOf(Color(0xFF5B21B6), Color(0xFF8B5CF6)),
            activityClass = MathMazeGameActivity::class.java
        )
    )

    val conceptGames = listOf(
        GameCatalogItem(
            title = "Pazar Simülatörü",
            description = "Alışveriş yap, para üstü hesapla ve gerçek hayatta matematiği kullan.",
            category = "Gerçek Hayat",
            iconRes = R.drawable.ic_market_game,
            gradientColors = listOf(Color(0xFF065F46), Color(0xFF10B981)),
            activityClass = MarketSimulatorActivity::class.java
        ),
        GameCatalogItem(
            title = "Şekiller & Geometri",
            description = "Geometrik şekilleri, köşe ve kenar sayılarını eğlenerek öğren.",
            category = "Kavramlar",
            iconRes = R.drawable.ic_shapes,
            gradientColors = listOf(Color(0xFF0369A1), Color(0xFF38BDF8)),
            activityClass = GeometryCastleActivity::class.java
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
                imageVector = Icons.Filled.SportsEsports,
                contentDescription = null,
                tint = HomeColors.MagicCyan,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Oyun Dünyası",
                    color = HomeColors.TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(
                    text = "Tüm matematik maceraları ve mini oyunlar",
                    color = HomeColors.TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // 1. Temel İşlemler
        GameCategoryHeader("Temel İşlemler", "4 Oyun")
        basicGames.forEach { game ->
            GameItemCard(game = game, onClick = {
                context.startActivity(Intent(context, game.activityClass))
            })
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2. Macera & Eğlence
        GameCategoryHeader("Macera & Eğlence", "4 Oyun")
        adventureGames.forEach { game ->
            GameItemCard(game = game, onClick = {
                context.startActivity(Intent(context, game.activityClass))
            })
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. Gerçek Hayat & Kavramlar
        GameCategoryHeader("Gerçek Hayat & Kavramlar", "2 Oyun")
        conceptGames.forEach { game ->
            GameItemCard(game = game, onClick = {
                context.startActivity(Intent(context, game.activityClass))
            })
            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun GameCategoryHeader(title: String, count: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = HomeColors.TextAccentCyan,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = count,
            color = HomeColors.TextMuted,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun GameItemCard(
    game: GameCatalogItem,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(HomeColors.CardSurface)
            .border(1.dp, HomeColors.CardBorderSubtle, RoundedCornerShape(18.dp))
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(game.gradientColors)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = game.iconRes),
                    contentDescription = game.title,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = game.title,
                    color = HomeColors.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = game.description,
                    color = HomeColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 15.sp,
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HomeColors.MagicCyan
                ),
                modifier = Modifier.height(40.dp)
            ) {
                Text(
                    text = "Oyna",
                    color = Color(0xFF071426),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

