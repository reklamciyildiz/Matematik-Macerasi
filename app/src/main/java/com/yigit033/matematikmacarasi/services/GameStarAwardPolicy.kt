package com.yigit033.matematikmacarasi.services

/**
 * StarManager oyun/seviye yıldız kaydı en yüksek başarıyı tutar.
 * Tekrar oynanışta daha düşük skor kazanılmış hakları düşüremez.
 */
internal object GameStarAwardPolicy {
    data class Award(val bestStars: Int, val additionalStars: Int)

    fun evaluate(previousBest: Int, earned: Int): Award {
        require(previousBest >= 0 && earned >= 0)
        return Award(
            bestStars = maxOf(previousBest, earned),
            additionalStars = maxOf(0, earned - previousBest)
        )
    }
}
