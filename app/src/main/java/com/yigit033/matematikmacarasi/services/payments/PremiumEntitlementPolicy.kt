package com.yigit033.matematikmacarasi.services.payments

/**
 * Current user-facing Premium policy: retain the two existing, independent
 * entitlements from Google Play Billing and legacy Firebase code activation.
 *
 * No verified-parent account, institutional source or login requirement is
 * introduced into the Android app. A future institutional design must go
 * through its own security and migration review before being enabled.
 */
object PremiumEntitlementPolicy {
    fun hasAccess(
        confirmedPlayPurchase: Boolean,
        legacyCodeActivated: Boolean
    ): Boolean = confirmedPlayPurchase || legacyCodeActivated
}
