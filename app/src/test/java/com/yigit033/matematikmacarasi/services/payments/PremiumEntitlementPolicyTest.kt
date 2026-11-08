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

    @Test fun absenceOfAllRightsDoesNotGrantPremium() {
        assertFalse(PremiumEntitlementPolicy.hasAccess(false,false,false))
    }
    @Test fun anActiveMonthlyOrYearlySubscriptionGrantsTheSamePremium() {
        assertTrue(PremiumEntitlementPolicy.hasAccess(false,false,true))
    }
    @Test fun expiredSubscriptionDoesNotBecomePermanentOwnership() {
        assertTrue(PremiumEntitlementPolicy.hasAccess(false,false,true))
        assertFalse(PremiumEntitlementPolicy.hasAccess(false,false,false))
    }
    @Test fun lifetimeAndValidCodeSurviveSubscriptionExpiry() {
        assertTrue(PremiumEntitlementPolicy.hasAccess(true,false,false))
        assertTrue(PremiumEntitlementPolicy.hasAccess(false,true,false))
        assertTrue(PremiumEntitlementPolicy.hasAccess(true,true,false))
    }
}
