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
        assertEquals(9,PremiumFeaturePolicy.features.size)
    }

    @Test fun adultPurchaseChallengeNeverAcceptsWrongOrUnconfirmedAnswers() {
        for(seed in listOf(0,1,2,13,27,99,Int.MIN_VALUE,Int.MAX_VALUE)) {
            val q=AdultPurchaseChallenge.create(seed)
            assertTrue(q.left in 12..24)
            assertTrue(q.right in 7..15)
            assertFalse(AdultPurchaseChallenge.accepted(q,q.answer.toString(),false))
            assertFalse(AdultPurchaseChallenge.accepted(q,(q.answer+1).toString(),true))
            assertFalse(AdultPurchaseChallenge.accepted(q,"",true))
            assertFalse(AdultPurchaseChallenge.accepted(q,"banana",true))
            assertTrue(AdultPurchaseChallenge.accepted(q,q.answer.toString(),true))
            assertTrue(AdultPurchaseChallenge.accepted(q," ${q.answer} ",true))
        }
    }
}