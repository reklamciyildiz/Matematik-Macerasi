package com.yigit033.matematikmacarasi.components

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.models.Avatar
import com.yigit033.matematikmacarasi.models.AvatarAnimationType
import kotlinx.coroutines.delay
import androidx.compose.ui.graphics.painter.Painter
import android.content.res.Resources

/**
 * Animasyonlu Avatar Görünümü
 * 
 * @param avatar Gösterilecek avatar
 * @param modifier Görünüm modifikatörü
 * @param size Avatar boyutu
 * @param contentDescription Erişilebilirlik için içerik açıklaması
 */
@Composable
fun AnimatedAvatarView(
    avatar: Avatar,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    contentDescription: String? = avatar.name
) {
    val context = LocalContext.current
    val TAG = "AvatarView_V5" // Sürüm güncellendi (Tekrar basitleştirildi)
    
    // Lottie spec'i oluştur
    val lottieSpec = remember(avatar.id) {
        if (avatar.animationType == AvatarAnimationType.LOTTIE && avatar.animationResId != 0) {
            try {
                LottieCompositionSpec.RawRes(avatar.animationResId)
            } catch (e: Exception) {
                Log.e(TAG, "Lottie Spec oluşturulurken hata: ${avatar.name}", e)
                LottieCompositionSpec.JsonString("{}") // Hata durumunda boş spec
            }
        } else {
            LottieCompositionSpec.JsonString("{}") // Animasyon yoksa veya tipi LOTTIE değilse boş spec
        }
    }
    
    // Kompozisyonu yükle
    val compositionResult by rememberLottieComposition(
        spec = lottieSpec, 
        onRetry = { failCount, exception ->
            Log.e(TAG, "Animasyon yükleme denemesi $failCount başarısız: ${avatar.name}", exception)
            failCount < 2 
        }
    )

    // Fallback gösterilip gösterilmeyeceğini belirle
    val showFallback = remember(compositionResult, lottieSpec) {
        compositionResult == null || 
        (lottieSpec is LottieCompositionSpec.JsonString && lottieSpec.jsonString == "{}") ||
        (compositionResult?.duration ?: 0f) <= 0f
    }

    // Animasyon progress'i
    val progress by animateLottieCompositionAsState(
        composition = compositionResult,
        iterations = LottieConstants.IterateForever,
        isPlaying = !showFallback, 
        speed = 1.5f,
        restartOnPlay = false
    )

    // Tema renkleri
    val avatarBackgroundColor = remember(avatar.id) { 
        listOf(Color(0xFFE1F5FE), Color(0xFFFCE4EC), Color(0xFFF1F8E9), Color(0xFFFFF8E1)).random()
    }
    val avatarBorderColor = remember(avatar.id) {
        listOf(Color(0xFF03A9F4), Color(0xFFE91E63), Color(0xFF8BC34A), Color(0xFFFFC107)).random()
    }

    // Ana Avatar Kutusu
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(avatarBackgroundColor)
            .border(width = 2.dp, color = avatarBorderColor, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (showFallback) {
            // Fallback durumunda statik resmi göster
            Log.w(TAG, "Statik avatar gösteriliyor (fallback): ${avatar.name} (Res ID: ${avatar.imageResId})")
            Image(
                painter = painterResource(id = avatar.imageResId),
                contentDescription = contentDescription,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            // Lottie Animasyonunu Göster
            Log.d(TAG, "Lottie animasyonu gösteriliyor: ${avatar.name}")
            LottieAnimation(
                composition = compositionResult,
                progress = { progress },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp), 
                contentScale = ContentScale.Fit
            )
        }
    }
    
    // Temizlik efekti
    DisposableEffect(avatar.id) {
         onDispose {
             Log.d(TAG, "AnimatedAvatarView onDispose: ${avatar.name}")
         }
     }
}

// ShowStaticAvatarSafely ve FallbackColorfulAvatar fonksiyonları artık kullanılmıyor,
// bu yüzden kaldırılabilir veya yorum satırı yapılabilir.
// @Composable
// private fun ShowStaticAvatarSafely(...) { ... }
// @Composable
// private fun FallbackColorfulAvatar(...) { ... }
// @Composable
// private fun FallbackAvatar() { ... } 