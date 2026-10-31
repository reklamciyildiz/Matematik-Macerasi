package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * Matematik Macerası 2.0 — Onaylı Tasarım Renk Paleti (Sprint H2)
 *
 * Referans: reference/HOME_APPROVED_REFERENCE.png
 * Görsel kimlik: Parlak gökyüzü, deniz ve adalar ortamı, sıcak krem parşömen,
 * canlı yeşil görev butonu, renkli oyun kartları ve açık zeminli istatistik yüzeyleri.
 */
object HomeColors {
    // 1. Üst Dünya & Gökyüzü / Deniz Tonları
    val SkyBlue = Color(0xFF60A5FA)
    val SkyBlueLight = Color(0xFFBAE6FD)
    val OceanBlue = Color(0xFF0284C7)
    val DeepOceanBlue = Color(0xFF0369A1)
    val NavyBackground = Color(0xFF0A2240)

    // 2. Parşömen / Görev Kartı (Warm Parchment / Cream)
    val ParchmentBgTop = Color(0xFFFFFBEB)
    val ParchmentBgBottom = Color(0xFFFDE68A)
    val ParchmentBorder = Color(0xFFD4A359)
    val ParchmentBorderSubtle = Color(0xFFE2B872)
    val ParchmentTitleDark = Color(0xFF1E293B)
    val ParchmentTitleBlue = Color(0xFF1D4ED8)
    val ParchmentSubtitle = Color(0xFF475569)

    // 3. Canlı Yeşil Görev Butonu (Referanstaki parlak yeşil CTA)
    val QuestGreenTop = Color(0xFF22C55E)
    val QuestGreenBottom = Color(0xFF16A34A)
    val QuestGreenBorder = Color(0xFF86EFAC)
    val QuestGreenShadow = Color(0xFF15803D)

    // 4. Hızlı Erişim Kart Renkleri
    val FourOperationsOrange = Color(0xFFFF6D00)
    val BalloonPopCyan = Color(0xFF00B0FF)
    val NumberHuntGreen = Color(0xFF10B981)
    val SpaceAdventurePurple = Color(0xFF8B5CF6)

    // 5. Açık Yüzeyler (Senin İçin Önerilen & Gelişimim)
    val LightSectionSurface = Color(0xFFFFFFFF)
    val LightSectionBg = Color(0xFFF0F7FF)
    val LightSectionBorder = Color(0xFFE2E8F0)
    val TextDark = Color(0xFF0F172A)
    val TextDarkSecondary = Color(0xFF334155)
    val TextDarkMuted = Color(0xFF64748B)

    // 6. Gelişimim İstatistik Kartları (Pastel Tonlar)
    val StatAmberBg = Color(0xFFFEF9C3)
    val StatAmberBorder = Color(0xFFFDE047)
    val StatAmberText = Color(0xFFB45309)

    val StatBlueBg = Color(0xFFEFF6FF)
    val StatBlueBorder = Color(0xFFBFDBFE)
    val StatBlueText = Color(0xFF1D4ED8)

    val StatGreenBg = Color(0xFFECFDF5)
    val StatGreenBorder = Color(0xFFA7F3D0)
    val StatGreenText = Color(0xFF047857)

    val StatPinkBg = Color(0xFFFFF1F2)
    val StatPinkBorder = Color(0xFFFECDD3)
    val StatPinkText = Color(0xFFBE123C)

    // 7. Alt Navigasyon Çubuğu (Referanstaki koyu mavi taban & parlak mavi seçili buton)
    val BottomNavBackground = Color(0xFF08172E)
    val BottomNavBorder = Color(0xFF1E3A60)
    val BottomNavSelected = Color(0xFF2563EB)
    val BottomNavSelectedGlow = Color(0xFF00E5FF)
    val BottomNavUnselected = Color(0xFF8DA4C4)

    // 8. Altın & Yıldız
    val StarGold = Color(0xFFFFD700)
    val StarAmber = Color(0xFFF59E0B)

    // Genel Uyumluluk Renkleri
    val MagicCyan = Color(0xFF00E5FF)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFFB4D0ED)
    val TextAccentCyan = Color(0xFF67E8F9)
    val TextMuted = Color(0xFF7A9BBF)
    val CardSurface = Color(0xFF0A2540).copy(alpha = 0.92f)
    val CardBorderSubtle = Color(0xFF1E3A60)

    // Gradientler
    val ParchmentGradient = Brush.verticalGradient(
        colors = listOf(
            ParchmentBgTop,
            ParchmentBgBottom
        )
    )

    val QuestGreenGradient = Brush.verticalGradient(
        colors = listOf(
            QuestGreenTop,
            QuestGreenBottom
        )
    )

    val HeroScrimGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF021B3A).copy(alpha = 0.55f),
            Color.Transparent
        )
    )

    val QuickAccessBgGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0369A1),
            Color(0xFF0C2B4E)
        )
    )

    val MainWorldGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0284C7),
            Color(0xFF0369A1),
            Color(0xFF082240)
        )
    )
}
