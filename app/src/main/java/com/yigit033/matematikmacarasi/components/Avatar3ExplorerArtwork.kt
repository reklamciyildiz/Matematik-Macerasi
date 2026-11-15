package com.yigit033.matematikmacarasi.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.yigit033.matematikmacarasi.models.Avatar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * High-detail original Explorer production art: three distinct costumes.
 *
 * The pre-existing Kaşif Lottie and the new forest/space Lotties remain
 * accessible via the workshop's animation toggle. This artwork is a static,
 * alpha-transparent character sprite with lightweight Compose idle motion,
 * NOT a fully rigged 3D / Lottie animation.
 *
 * Small avatars show a torso portrait crop; detail view shows the full body.
 * Only ID 6 uses these assets. Other 14 avatars keep existing Lottie rendering.
 */
internal object Avatar3ExplorerArtLoader {
    private val cache = object : LruCache<String, ImageBitmap>(5) {}

    @Synchronized
    fun cached(key: String): ImageBitmap? = cache.get(key)

    @Synchronized
    fun store(key: String, value: ImageBitmap) {
        cache.put(key, value)
    }

    fun path(lookId: Int): String = when (lookId) {
        1 -> "avatars3/explorer_06_forest.webp"
        2 -> "avatars3/explorer_06_space.webp"
        else -> "avatars3/explorer_06_classic.webp"
    }

    fun load(
        context: android.content.Context,
        lookId: Int,
        portraitCrop: Boolean
    ): ImageBitmap? {
        val path = path(lookId)
        val key = "$path:$portraitCrop"
        cached(key)?.let { return it }
        return try {
            context.assets.open(path).use { stream ->
                val bounds = BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inScaled = false
                }
                val decoded = BitmapFactory.decodeStream(stream, null, bounds)
                    ?: return null
                val result = if (portraitCrop) {
                    // The supplied 512x768 transparent sprites share a
                    // stable silhouette. Crop toward the eyes/chest to
                    // make 64–96dp thumbnails genuinely recognisable.
                    val left = (decoded.width * 0.16f).toInt()
                    val top = (decoded.height * 0.115f).toInt()
                    val cropWidth = (decoded.width * 0.69f).toInt()
                    val cropHeight = (decoded.height * 0.54f).toInt()
                    Bitmap.createBitmap(
                        decoded, left, top,
                        cropWidth.coerceAtMost(decoded.width - left),
                        cropHeight.coerceAtMost(decoded.height - top)
                    ).also {
                        if (it !== decoded) decoded.recycle()
                    }
                } else decoded
                val image = result.asImageBitmap()
                store(key, image)
                image
            }
        } catch (_: Exception) {
            null
        }
    }
}

@Composable
fun Avatar3ExplorerArtwork(
    avatar: Avatar,
    lookId: Int,
    size: Dp,
    modifier: Modifier = Modifier,
    animate: Boolean = true,
    fullBody: Boolean = false
) {
    val context = LocalContext.current
    val cropped = !fullBody
    val art by produceState<ImageBitmap?>(null, context, lookId, cropped) {
        value = withContext(Dispatchers.IO) {
            Avatar3ExplorerArtLoader.load(context.applicationContext, lookId, cropped)
        }
    }
    val idle = if (animate) {
        val transition = rememberInfiniteTransition(label = "Explorer idle")
        transition.animateFloat(
            initialValue = -2f, targetValue = 2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800),
                repeatMode = RepeatMode.Reverse
            ), label = "Explorer bob"
        ).value
    } else 0f

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        val sprite = art
        if (sprite != null) {
            Image(
                bitmap = sprite,
                contentDescription = avatar.name,
                modifier = Modifier.fillMaxSize()
                    .padding(if (cropped) 2.dp else 0.dp)
                    .graphicsLayer { translationY = idle.dp.toPx() },
                contentScale = ContentScale.Fit
            )
        } else {
            // Fail open to the original / separate pilot Lottie asset:
            // a missing WebP never leaves an empty character.
            AnimatedAvatarView(
                avatar = avatar,
                size = size,
                animate = animate,
                lookId = lookId,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
