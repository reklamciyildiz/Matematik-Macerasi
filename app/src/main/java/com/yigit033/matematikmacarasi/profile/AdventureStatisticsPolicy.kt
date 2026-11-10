package com.yigit033.matematikmacarasi.profile

/**
 * Profile analytics are a benefit, never the source of purchase entitlement.
 * Free: useful lifetime summary. Premium: complete per-game breakdown.
 * This pure policy is intentionally shared by profile rendering and unit tests.
 */
internal enum class AdventureStatistic { STARS, COMPLETIONS, PLAY_TIME, FAVORITE }

internal object AdventureStatisticsPolicy {
    fun showValue(statistic: AdventureStatistic, isPremium: Boolean): Boolean =
        isPremium || statistic == AdventureStatistic.STARS ||
            statistic == AdventureStatistic.COMPLETIONS

    fun allowBreakdown(statistic: AdventureStatistic, isPremium: Boolean): Boolean =
        isPremium // Even free summary does not expose premium per-game analytics.

    fun description(isPremium: Boolean): String =
        if (isPremium) "Oyunlara göre detayları incelemek için kartlara dokun."
        else "Yıldızların ve bitirdiğin oyunlar ücretsiz. Oyun bazlı analizler Premium'a özel."
}
