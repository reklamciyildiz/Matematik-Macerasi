#!/usr/bin/env python3
"""M1 staging guard: no retail migration and no direct Firestore license writes."""
from pathlib import Path
root=Path(__file__).resolve().parents[1]
app=root/"app/src/main/java/com/yigit033/matematikmacarasi"
client=(app/"services/payments/PurchaseManager.kt").read_text(encoding="utf-8")
codes=(app/"services/PremiumCodeValidator.kt").read_text(encoding="utf-8")
ui=(app/"screens/SettingsScreen.kt").read_text(encoding="utf-8")
legacy=(app/"services/FirebaseManager.kt").read_text(encoding="utf-8")
policy=(app/"services/payments/PremiumEntitlementPolicy.kt").read_text(encoding="utf-8")
server=(root/"commercial/functions/institution-functions.cjs").read_text(encoding="utf-8")
play_server=(root/"commercial/functions/index.js").read_text(encoding="utf-8")
institution=(root/"commercial/functions/src/institution-policy.cjs").read_text(encoding="utf-8")
rules=(root/"commercial/firestore_billing_rules.fragment").read_text(encoding="utf-8")
for expected in (
    'private const val PREMIUM_SKU = "matematik_macerasi_premium"',
    'override fun onPurchasesUpdated(',
    'private fun handlePurchase(',
    'private fun acknowledgePurchase(',
    'fun restorePurchases()',
    'fun buyPremium(activity:Activity)',
    'savePremiumStatus(premiumFound)',
    'playSubscriptionClient.isPremiumOffline()',
    'val hasAccess=isPremiumSync()',
    # Billing8: both INAPP lifetime and SUBS have Play offer tokens.
    'oneTimePurchaseOfferDetailsList?.firstOrNull()',
    'subscriptionOffer?.offerToken else oneTimeOffer?.offerToken',
    '.setOfferToken(offerToken)',

):
    assert expected in client,expected
for expected in ('legacyCodeActivated: Boolean','confirmedPlayPurchase: Boolean'):
    assert expected in policy,expected
assert 'verifiedInstitution' not in policy
assert '): Boolean = confirmedPlayPurchase || legacyCodeActivated' in policy
# Dormant design must not be exported as callable Cloud Functions.
assert 'Object.assign(exports,require("./institution-functions.cjs"))' not in play_server
assert 'require("./institution-functions.cjs")' not in play_server
assert 'exports.createInstitutionContract' not in play_server
assert 'exports.redeemInstitutionSeatCode' not in play_server
# Cloudflare-mode subscription/license state must come from current signed
# rights, never from a stale mutable UI StateFlow after expiry.
assert 'val isPremium=if(BuildConfig.MM_USE_CLOUDFLARE)' in ui
assert 'enabled=price!=null && catalog is PurchaseManager.CatalogStatus.Ready' in ui
assert 'PremiumPlan.LIFETIME' in ui and 'purchaseManager.buyPremium(it,plan)' in ui
assert 'Google Play fiyatlarını yeniden kontrol et' in ui
assert 'unfetchedProductList' in client and 'enableAutoServiceReconnection()' in client
assert '_premiumState.value=isPremiumSync()' in client
assert 'playSubscriptionClient.isPremiumOffline()' in client
assert 'validateAndActivateCode' in codes
assert 'DifferentCodeAlreadyActivated' in codes and 'DifferentCodeAlreadyActivated' in ui
assert 'premium_codes' in legacy and 'code_activations' in legacy
assert 'incrementCodeUsageAndRecordActivation' in legacy
# Legacy RTDB activation is deliberately unchanged until an authenticated
# migration; do not misrepresent it as atomic or publish the old rules.
assert 'setValue(newCount)' in legacy
assert 'MM_LICENSE_SIGNING_KEY' in server
assert 'mmCommercialAdmin' in server and 'mmParentVerified' in server
assert 'enforceAppCheck:true' in server
assert 'tx.create(codes.doc(hashes[i])' in server
assert 'tx.update(contractRef,{' in server and 'seatsIssued:FieldValue.increment(count)' in server
assert 'policy.canIssue(' in server and 'policy.canRedeem(' in server
assert 'policy.institutionActive(' in server
assert 'policy.effectivePremium(' in server
assert 'getVerifiedPremiumAccess' in server
# All new write endpoints are disabled unless explicitly enabled.
assert 'process.env.MM_M1_BACKEND_ENABLED!=="true"' in server
assert 'process.env.MM_M1_BACKEND_ENABLED!=="true"' in play_server
assert 'requireCommercialLaunch();' in play_server
assert 'isVerifiedProfile(profileSnap.data(),uid)' in server
assert 'getInstitutionContractSummary' in server
assert 'setVerifiedStudentProfile' in server
assert 'redemptions.doc(hash)' in server
assert 'seatsRedeemed:FieldValue.increment(' not in server
assert 'seatsRedeemed:FieldValue.increment(' not in play_server
assert 'firstCodeHash' in server
assert (root/"commercial/functions/src/institution-transactions.test.cjs").exists()
assert 'codeSecret.value()' in server
assert 'function deterministicCode(' in institution
for coll in ("billingTokensV1","parentPremiumEntitlementsV1",
             "institutionContractsV1","institutionCodeBatchesV1",
             "institutionCodesV1","institutionAssignmentsV1",
             "parentStudentProfilesV1","institutionRedemptionsV1"):
    assert f"match /{coll}/" in rules,coll
assert rules.count("allow read, write: if false;")>=8
assert (root/"app/src/test/java/com/yigit033/matematikmacarasi/services/payments/PremiumEntitlementPolicyTest.kt").exists()
print("M1 dormant institutional design, unchanged Play + Firebase code rights and private collections: PASS")
