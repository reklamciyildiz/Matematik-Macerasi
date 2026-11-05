package com.yigit033.matematikmacarasi.services.payments

import org.junit.Assert.*
import org.junit.Test

class PremiumEntitlementPolicyTest {
    @Test fun playPurchaseIsPreservedWithoutAnyInstitutionLicense() {
        assertTrue(PremiumEntitlementPolicy.hasAccess(true,false))
        assertTrue(PremiumEntitlementPolicy.hasAccess(true,true,false))
    }
    @Test fun legacyCodeKeepsPremiumEvenWithoutPlayPurchase(){
        assertTrue(PremiumEntitlementPolicy.hasAccess(false,true,false))
    }
    @Test fun independentlyVerifiedInstitutionLicenseCanUnlockPremium(){
        assertTrue(PremiumEntitlementPolicy.hasAccess(false,false,true))
    }
    @Test fun revokingPlayDoesNotRemoveAnotherVerifiedSource(){
        assertTrue(PremiumEntitlementPolicy.hasAccess(false,true,false))
        assertTrue(PremiumEntitlementPolicy.hasAccess(false,false,true))
        assertFalse(PremiumEntitlementPolicy.hasAccess(false,false,false))
    }
}
