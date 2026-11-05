package com.yigit033.matematikmacarasi.services.payments

import org.junit.Assert.*
import org.junit.Test

class PremiumEntitlementPolicyTest {
    @Test fun existingPlayPurchaseAloneKeepsPremium() {
        assertTrue(PremiumEntitlementPolicy.hasAccess(true, false))
        assertTrue(PremiumEntitlementPolicy.hasAccess(true, true))
    }

    @Test fun existingFirebaseCodeAloneKeepsPremium() {
        assertTrue(PremiumEntitlementPolicy.hasAccess(false, true))
    }

    @Test fun absenceOfBothExistingRightsDoesNotGrantPremium() {
        assertFalse(PremiumEntitlementPolicy.hasAccess(false, false))
    }
}
