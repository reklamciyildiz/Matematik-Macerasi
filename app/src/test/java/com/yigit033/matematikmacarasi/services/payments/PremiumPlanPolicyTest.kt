package com.yigit033.matematikmacarasi.services.payments

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PremiumPlanPolicyTest {
    @Test fun legacyLifetimeIdIsUnchanged() {
        assertEquals("matematik_macerasi_premium",PremiumPlan.LIFETIME.productId)
        assertFalse(PremiumPlan.LIFETIME.subscription)
        assertEquals(null,PremiumPlan.LIFETIME.basePlanId)
    }
    @Test fun twoBillingPeriodsShareOneSubscription() {
        assertEquals(PremiumPlan.MONTHLY.productId,PremiumPlan.YEARLY.productId)
        assertEquals("monthly",PremiumPlan.MONTHLY.basePlanId)
        assertEquals("yearly",PremiumPlan.YEARLY.basePlanId)
        assertTrue(PremiumPlan.MONTHLY.subscription)
        assertTrue(PremiumPlan.YEARLY.subscription)
    }
    @Test fun releaseGateProtectsCustomers() {
        assertFalse(PremiumPlanPolicy.purchasable(PremiumPlan.MONTHLY,false))
        assertFalse(PremiumPlanPolicy.purchasable(PremiumPlan.YEARLY,false))
        assertTrue(PremiumPlanPolicy.purchasable(PremiumPlan.LIFETIME,false))
        assertTrue(PremiumPlanPolicy.purchasable(PremiumPlan.MONTHLY,true))
    }
}
