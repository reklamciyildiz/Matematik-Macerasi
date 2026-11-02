package com.yigit033.matematikmacarasi.games.interactive

import androidx.annotation.DrawableRes
import com.yigit033.matematikmacarasi.R

/** 640x1050 optimized WebP worlds and transparent sprites, no runtime network. */
internal object SpacePremiumArt {
    @DrawableRes
    fun background(planet:SpacePlanet):Int=when(planet){
        SpacePlanet.TERRA->R.drawable.space_bg_terra_premium
        SpacePlanet.AQUA->R.drawable.space_bg_aqua_premium
        SpacePlanet.PYRA->R.drawable.space_bg_pyra_premium
        SpacePlanet.LUNA->R.drawable.space_bg_luna_premium
        SpacePlanet.NOVA->R.drawable.space_bg_nova_premium
    }
}
