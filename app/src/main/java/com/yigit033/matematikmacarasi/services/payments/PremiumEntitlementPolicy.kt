package com.yigit033.matematikmacarasi.services.payments

/**
 * Single entitlement decision; the legacy purchase and code storage are kept
 * unchanged until verified Firebase parent accounts are rolled out.
 *
 * This is only a pure access decision — it never creates a grant and never
 * turns a locally supplied "institution" flag into a verified entitlement.
 */
object PremiumEntitlementPolicy {
    fun hasAccess(
        confirmedPlayPurchase:Boolean,
        legacyCodeActivated:Boolean,
        verifiedInstitution:Boolean=false
    ):Boolean = confirmedPlayPurchase || legacyCodeActivated || verifiedInstitution
}
