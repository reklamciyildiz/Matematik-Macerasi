package com.yigit033.matematikmacarasi.services.payments

/** One legacy non-consumable INAPP product, one SUBS product with two base plans. */
enum class PremiumPlan(
    val productId:String, val basePlanId:String?, val label:String
) {
    MONTHLY("matematik_macerasi_premium_subscription","monthly","Aylık Premium"),
    YEARLY("matematik_macerasi_premium_subscription","yearly","Yıllık Premium"),
    LIFETIME("matematik_macerasi_premium",null,"Ömür Boyu Premium");
    val subscription:Boolean get()=basePlanId!=null
}
object PremiumPlanPolicy {
    fun purchasable(plan:PremiumPlan,enabled:Boolean):Boolean =
        plan==PremiumPlan.LIFETIME || enabled
}
