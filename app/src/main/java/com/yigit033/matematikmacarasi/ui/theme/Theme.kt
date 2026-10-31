package com.yigit033.matematikmacarasi.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.googlefonts.Font
import androidx.core.view.WindowCompat
import com.yigit033.matematikmacarasi.R

// Renk paleti
val PrimaryBlue = Color(0xFF1976D2) // Ana renk
val PrimaryLightBlue = Color(0xFF64B5F6) // Açık mavi
val PrimaryDarkBlue = Color(0xFF0D47A1) // Koyu mavi
val BackgroundBlueLight = Color(0xFFE3F2FD) // Açık mavi arka plan
val BackgroundBlueMedium = Color(0xFFBBDEFB) // Orta mavi arka plan

// Vurgu renkleri
val AccentGreen = Color(0xFF4CAF50) // Doğru yanıtlar için
val AccentYellow = Color(0xFFFFEB3B) // Uyarılar için
val AccentRed = Color(0xFFF44336) // Yanlış yanıtlar için

// Nötr renkler
val TextPrimary = Color(0xFF212121) // Ana metin
val TextSecondary = Color(0xFF757575) // İkincil metin
val DividerColor = Color(0xFFBDBDBD) // Ayraçlar

// Görüntülenecek renk şeması
private val LightColorScheme = lightColors(
    primary = PrimaryBlue,
    primaryVariant = PrimaryDarkBlue,
    secondary = AccentGreen,
    secondaryVariant = Color(0xFF2E7D32),
    background = BackgroundBlueLight,
    surface = Color.White,
    error = AccentRed,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onError = Color.White
)

private val DarkColorScheme = darkColors(
    primary = PrimaryLightBlue,
    primaryVariant = PrimaryDarkBlue,
    secondary = AccentGreen,
    secondaryVariant = Color(0xFF2E7D32),
    background = Color(0xFF0D47A1),
    surface = Color(0xFF121212),
    error = AccentRed,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.Black
)

// Google Fonts provider
val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

// Comic Neue font ailesi
val ComicNeueFont = FontFamily(
    Font(
        googleFont = GoogleFont("Comic Neue"),
        fontProvider = provider
    )
)

// Animasyon sabitleri
object AnimationDurations {
    const val SHORT = 150 // ms
    const val MEDIUM = 300 // ms
    const val LONG = 800 // ms
}

@Composable
fun MatematikMacerasiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colors = colorScheme,
        typography = Typography,
        content = content
    )
}