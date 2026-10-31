package com.yigit033.matematikmacarasi.models

import androidx.annotation.DrawableRes
import androidx.annotation.RawRes

/**
 * Avatar animasyon türleri
 */
enum class AvatarAnimationType {
    NONE,       // Animasyon yok, statik avatar
    LOTTIE,     // Lottie JSON animasyonu
    VECTOR      // AnimatedVectorDrawable animasyonu
}

/**
 * Avatar veri modeli
 */
data class Avatar(
    val id: Int,
    val name: String,
    @DrawableRes val imageResId: Int,
    val price: Int = 0,
    val isUnlocked: Boolean = false,
    val animationType: AvatarAnimationType = AvatarAnimationType.NONE,
    @RawRes val animationResId: Int = 0,
    val category: String = "Genel",
    val description: String = ""
) 