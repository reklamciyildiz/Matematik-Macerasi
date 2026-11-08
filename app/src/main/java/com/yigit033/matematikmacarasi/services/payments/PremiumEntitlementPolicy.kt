package com.yigit033.matematikmacarasi.services.payments

/**
 * Three paid billing options grant the exact same features:
 * existing lifetime Play INAPP, existing signed Premium codes, or an actively
 * entitled Play SUBS product (monthly/yearly). SUBS is NOT permanent.
 *
 * No verified-parent account, institutional source or login requirement is
 * introduced into the Android app. A future institutional design must go
 * through its own security and migration review before being enabled.
 */
object PremiumEntitlementPolicy {
    fun hasAccess(
        confirmedPlayPurchase: Boolean,
        legacyCodeActivated: Boolean,
        activePlaySubscription: Boolean = false
    ): Boolean = confirmedPlayPurchase || legacyCodeActivated || activePlaySubscription
}
