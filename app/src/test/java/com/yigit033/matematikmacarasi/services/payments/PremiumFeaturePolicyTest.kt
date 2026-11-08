package com.yigit033.matematikmacarasi.services.payments

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumFeaturePolicyTest {
    @Test fun freeAndPremiumShopsShareOneFeatureMatrix() {
        assertEquals(3,PremiumFeaturePolicy.marketShopCount(false))
        assertEquals(10,PremiumFeaturePolicy.marketShopCount(true))
        for(index in -1..12) {
            assertEquals(index in 0..2,
                PremiumFeaturePolicy.canEnterMarketShop(index,false))
            assertEquals(index in 0..9,
                PremiumFeaturePolicy.canEnterMarketShop(index,true))
        }
        PremiumFeature.entries.forEach { feature->
            assertFalse(PremiumFeaturePolicy.allowed(feature,false))
            assertTrue(PremiumFeaturePolicy.allowed(feature,true))
        }
        assertEquals(11,PremiumFeaturePolicy.features.size)
        assertTrue(PremiumFeature.FULL_LEARNING_LIBRARY in PremiumFeaturePolicy.features)
    }
}
