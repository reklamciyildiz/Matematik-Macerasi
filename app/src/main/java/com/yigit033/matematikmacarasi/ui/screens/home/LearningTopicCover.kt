package com.yigit033.matematikmacarasi.ui.screens.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.drawImage
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.yigit033.matematikmacarasi.R
import com.yigit033.matematikmacarasi.learning.TopicArea
import kotlin.math.roundToInt

/**
 * Original WebP topic covers share one small, deterministic illustrated atlas.
 * Each frame represents actual mathematical concepts, never a paid/locked promise.
 * Bitmap is decoded once per draw composition and stays memory-efficient.
 */
internal fun mathTopicArtworkIndex(area: TopicArea): Int = when (area) {
    TopicArea.NUMBERS -> 0
    TopicArea.OPERATIONS -> 1
    TopicArea.GEOMETRY -> 2
    TopicArea.DATA -> 3
    TopicArea.ALGEBRA -> 4
    TopicArea.REVIEW -> 5
}

@Composable
internal fun LearningTopicCover(
    area: TopicArea,
    modifier: Modifier = Modifier
) {
    val atlas = ImageBitmap.imageResource(R.drawable.learn_topic_atlas)
    Canvas(modifier.clip(RoundedCornerShape(17.dp))) {
        val frameWidth = atlas.width / 6
        drawImage(
            image = atlas,
            srcOffset = IntOffset(mathTopicArtworkIndex(area) * frameWidth, 0),
            srcSize = IntSize(frameWidth, atlas.height),
            dstSize = IntSize(
                size.width.roundToInt().coerceAtLeast(1),
                size.height.roundToInt().coerceAtLeast(1)
            ),
            filterQuality = FilterQuality.Medium
        )
    }
}
