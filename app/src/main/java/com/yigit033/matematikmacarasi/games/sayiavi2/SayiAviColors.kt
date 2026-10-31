package com.yigit033.matematikmacarasi.games.sayiavi2

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Sayı Avı 2.0 Tasarım ve Renk Paleti.
 * Onaylanmış Ana Sayfa H2 ve Kayıp Hazine referans konsept panosuna uyumlu,
 * parlak tropik okyanus mavisi, altın sarısı, parşömen kumu ve zümrüt yeşili tonları.
 */
object SayiAviColors {
    // Okyanus ve Gökyüzü
    val OceanDeep = Color(0xFF08172E)
    val OceanNavy = Color(0xFF0E223D)
    val OceanBlue = Color(0xFF0077B6)
    val OceanCyan = Color(0xFF00B4D8)
    val SkyLightBlue = Color(0xFF90E0EF)

    // Altın, Parşömen ve Kum
    val GoldBright = Color(0xFFFFD700)
    val GoldBorder = Color(0xFFD4AF37)
    val SandGold = Color(0xFFE0A96D)
    val ParchmentLight = Color(0xFFFDFBF7)
    val ParchmentWarm = Color(0xFFF7F1DF)
    val ParchmentDark = Color(0xFFEADBBE)

    // Eylem Butonları
    val EmeraldGreen = Color(0xFF10B981)
    val EmeraldDark = Color(0xFF059669)
    val SunsetOrange = Color(0xFFF59E0B)
    val CoralRed = Color(0xFFEF4444)
    val CoralRedDark = Color(0xFFDC2626)

    // Metin Renkleri
    val TextNavy = Color(0xFF0A192F)
    val TextNavyMuted = Color(0xFF4A5568)
    val TextWhite = Color(0xFFFFFFFF)
    val TextGold = Color(0xFFFFE082)

    // Gradient Fırçaları
    val OceanGradient = Brush.verticalGradient(
        colors = listOf(OceanNavy, OceanDeep)
    )

    val TopBarScrim = Brush.verticalGradient(
        colors = listOf(
            Color.Black.copy(alpha = 0.65f),
            Color.Black.copy(alpha = 0.30f),
            Color.Transparent
        )
    )

    val BottomBarScrim = Brush.verticalGradient(
        colors = listOf(
            Color.Transparent,
            Color.Black.copy(alpha = 0.40f),
            Color.Black.copy(alpha = 0.75f)
        )
    )

    val ParchmentSurface = Brush.verticalGradient(
        colors = listOf(ParchmentLight, ParchmentWarm, ParchmentDark)
    )

    val EmeraldButtonGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF34D399), EmeraldGreen, EmeraldDark)
    )

    val OrangeButtonGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFFFBBF24), SunsetOrange, Color(0xFFD97706))
    )

    val BlueButtonGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF38BDF8), OceanBlue, Color(0xFF0284C7))
    )
}

