package com.yigit033.matematikmacarasi.services

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumCodeBuildPolicyTest {
    @Test fun flagsAnExistingSignedLicenseWhenApkDisablesItsVerifier() {
        assertTrue(PremiumCodeBuildPolicy.isIncompatible(false,true))
    }
    @Test fun matchingModeOrNoLicenseDoesNotRaiseFalseAlarm() {
        assertFalse(PremiumCodeBuildPolicy.isIncompatible(true,true))
        assertFalse(PremiumCodeBuildPolicy.isIncompatible(false,false))
        assertFalse(PremiumCodeBuildPolicy.isIncompatible(true,false))
    }
}
