package com.yigit033.matematikmacarasi.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.yigit033.matematikmacarasi.models.Avatar
import com.yigit033.matematikmacarasi.models.AvatarAnimationType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Avatar 3.0 rendering policy.
 *
 * Collection thumbnails are static (no Lottie composition or infinite animation)
 * and the selected preview alone is animated. This keeps low-memory devices
 * responsive when the user scrolls through the collection.
 *
 * Deterministic colors prevent a character changing identity across sessions
 * or recompositions. Existing ID / image / Lottie mappings remain intact.
 */
@Composable
fun AnimatedAvatarView(
    avatar: Avatar,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    contentDescription: String? = avatar.name,
    animate: Boolean = true
) {
    val context = LocalContext.current
    // Optional, versioned production-ready WebP overrides. If no asset has
    // been installed, preserve existing Lottie/XML art without breaking AAB
    // builds, player ownership or current avatar identity.
    // Decode optional WebP artwork off the UI thread. Full-size 1024px
    // images in a scrolling gallery otherwise cause visible frame stalls
    // and allocate far more bitmap memory than a small character needs.
    // The legacy drawable/Lottie stays visible until loading finishes.
    val pilotArtwork by produceState<ImageBitmap?>(null, context, avatar.id, animate, size) {
        value = withContext(Dispatchers.IO) {
            val suffix = if (animate) "full" else "card"
            val filename = "avatars3/avatar3_${avatar.id.toString().padStart(2, '0')}_$suffix.webp"
            try {
                val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                context.assets.open(filename).use { stream ->
                    BitmapFactory.decodeStream(stream, null, bounds)
                }
                val targetPx = (size.value * context.resources.displayMetrics.density)
                    .toInt().coerceAtLeast(128)
                var sample = 1
                while (bounds.outWidth / (sample * 2) >= targetPx &&
                    bounds.outHeight / (sample * 2) >= targetPx
                ) {
                    sample *= 2
                }
                val options = BitmapFactory.Options().apply {
                    inSampleSize = sample
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                context.assets.open(filename).use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)?.asImageBitmap()
                }
            } catch (_: Exception) {
                null
            }
        }
    }
    val palette = remember(avatar.id) {
        val backgrounds = listOf(
            Color(0xFF244F83), Color(0xFF3B3A80),
            Color(0xFF236A72), Color(0xFF684783)
        )
        val borders = listOf(
            Color(0xFF81D8FF), Color(0xFFB8A2FF),
            Color(0xFF89E4CD), Color(0xFFFFD783)
        )
        val index = (avatar.id - 1).coerceAtLeast(0) % backgrounds.size
        backgrounds[index] to borders[index]
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(palette.first)
            .border(2.dp, palette.second, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val hasAnimation = animate &&
            avatar.animationType == AvatarAnimationType.LOTTIE &&
            avatar.animationResId != 0
        when {
            pilotArtwork != null -> Image(
                bitmap = pilotArtwork,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize().padding(2.dp),
                contentScale = ContentScale.Fit
            )
            hasAnimation -> AnimatedAvatarArtwork(avatar, contentDescription)
            else -> AvatarStaticArtwork(avatar, contentDescription)
        }
    }
}

@Composable
private fun AvatarStaticArtwork(avatar: Avatar, contentDescription: String?) {
    Image(
        painter = painterResource(id = avatar.imageResId),
        contentDescription = contentDescription,
        modifier = Modifier.fillMaxSize().padding(8.dp),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun AnimatedAvatarArtwork(avatar: Avatar, contentDescription: String?) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(avatar.animationResId)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        isPlaying = composition != null,
        speed = 1f
    )
    if (composition == null || (composition?.duration ?: 0f) <= 0f) {
        AvatarStaticArtwork(avatar, contentDescription)
    } else {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.fillMaxSize().padding(4.dp),
            contentScale = ContentScale.Fit
        )
    }
}
