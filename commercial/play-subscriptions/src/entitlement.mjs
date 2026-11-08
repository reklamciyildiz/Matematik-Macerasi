/**
 * PURE policy only. NEVER accept an Android client-provided subscription JSON.
 * Call this AFTER a trusted server fetches purchases.subscriptionsv2.get from
 * Google Play Developer API with its own service-account credentials.
 * No credentials, purchase tokens, identities or endpoints are included here.
 */
export const SUBSCRIPTION_PRODUCT = "matematik_macerasi_premium_subscription";
export const SUBSCRIPTION_BASE_PLANS = Object.freeze(["monthly", "yearly"]);

const ENTITLED_STATES = new Set([
  "SUBSCRIPTION_STATE_ACTIVE",
  "SUBSCRIPTION_STATE_IN_GRACE_PERIOD",
  "SUBSCRIPTION_STATE_CANCELED", // Paid-through cancellation, NOT expiry.
]);

/**
 * @param {object} verified Google subscriptionsv2.get JSON fetched server-side.
 * @param {number} nowMs Server UTC clock, milliseconds since epoch.
 * @returns {{entitled:boolean,reason:string,basePlanId:string|null,expiryTime:string|null}}
 */
export function evaluateVerifiedSubscription(verified, nowMs = Date.now()) {
  const deny = (reason) =>
    ({ entitled: false, reason, basePlanId: null, expiryTime: null });
  if (!verified || typeof verified !== "object" || Array.isArray(verified))
    return deny("MISSING_VERIFIED_PURCHASE");
  if (!Number.isFinite(nowMs) || nowMs < 0)
    return deny("INVALID_SERVER_CLOCK");
  if (!ENTITLED_STATES.has(verified.subscriptionState))
    return deny("NON_ENTITLED_STATE");

  const items = Array.isArray(verified.lineItems) ? verified.lineItems : [];
  const valid = items.filter((line) => {
    if (line?.productId !== SUBSCRIPTION_PRODUCT) return false;
    if (typeof line.expiryTime !== "string") return false;
    const expires = Date.parse(line.expiryTime);
    return Number.isFinite(expires) && expires > nowMs;
  }).sort((a, b) => Date.parse(b.expiryTime) - Date.parse(a.expiryTime));
  if (valid.length === 0) return deny("NO_ACTIVE_PREMIUM_LINE_ITEM");
  const item = valid[0];
  // Missing basePlanId is not evidence of fraud after server-side verification;
  // the product ID and valid entitlement are the authority.
  const plan = item.offerDetails?.basePlanId;
  return {
    entitled: true,
    reason: "ENTITLED",
    basePlanId: SUBSCRIPTION_BASE_PLANS.includes(plan) ? plan : null,
    expiryTime: item.expiryTime,
  };
}

/** Source of rights: ONE Premium feature set, never a new plan-specific tier. */
export function hasAnyPremiumRight({ lifetime = false, code = false, subscription = false } = {}) {
  return lifetime === true || code === true || subscription === true;
}
