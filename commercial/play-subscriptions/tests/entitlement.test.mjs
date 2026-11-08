import assert from "node:assert/strict";
import test from "node:test";
import {
  evaluateVerifiedSubscription, hasAnyPremiumRight, SUBSCRIPTION_PRODUCT
} from "../src/entitlement.mjs";

const now = Date.parse("2026-11-08T12:00:00.000Z");
const future = "2026-12-08T12:00:00.000Z";
const past = "2026-10-08T12:00:00.000Z";
const response = (state, expiryTime = future, basePlanId = "monthly") => ({
  subscriptionState: state,
  lineItems: [{ productId: SUBSCRIPTION_PRODUCT, expiryTime,
    offerDetails: { basePlanId } }],
});

test("active monthly subscription grants recurring Premium", () => {
  assert.deepEqual(evaluateVerifiedSubscription(
    response("SUBSCRIPTION_STATE_ACTIVE"), now
  ), { entitled: true, reason: "ENTITLED", basePlanId: "monthly", expiryTime: future });
});
test("active yearly subscription grants SAME Premium", () => {
  assert.equal(evaluateVerifiedSubscription(
    response("SUBSCRIPTION_STATE_ACTIVE", future, "yearly"), now
  ).entitled, true);
});
test("cancellation retains right ONLY until paid-through date", () => {
  assert.equal(evaluateVerifiedSubscription(
    response("SUBSCRIPTION_STATE_CANCELED"), now
  ).entitled, true);
  assert.equal(evaluateVerifiedSubscription(
    response("SUBSCRIPTION_STATE_CANCELED", past), now
  ).entitled, false);
});
test("grace is entitled; hold, paused, expired, pending are not", () => {
  assert.equal(evaluateVerifiedSubscription(
    response("SUBSCRIPTION_STATE_IN_GRACE_PERIOD"), now
  ).entitled, true);
  for (const state of [
    "SUBSCRIPTION_STATE_ON_HOLD", "SUBSCRIPTION_STATE_PAUSED",
    "SUBSCRIPTION_STATE_EXPIRED", "SUBSCRIPTION_STATE_PENDING",
    "SUBSCRIPTION_STATE_PENDING_PURCHASE_CANCELED", "UNKNOWN",
  ]) assert.equal(evaluateVerifiedSubscription(response(state), now).entitled, false);
});
test("no valid paid period or wrong product can never grant Premium", () => {
  assert.equal(evaluateVerifiedSubscription(response("SUBSCRIPTION_STATE_ACTIVE",past),now).entitled,false);
  assert.equal(evaluateVerifiedSubscription(response("SUBSCRIPTION_STATE_ACTIVE",new Date(now).toISOString()),now).entitled,false);
  const invalid = response("SUBSCRIPTION_STATE_ACTIVE");
  invalid.lineItems[0].productId="another_product";
  assert.equal(evaluateVerifiedSubscription(invalid,now).entitled,false);
  invalid.lineItems[0].productId=SUBSCRIPTION_PRODUCT;
  invalid.lineItems[0].expiryTime="invalid";
  assert.equal(evaluateVerifiedSubscription(invalid,now).entitled,false);
  assert.equal(evaluateVerifiedSubscription(null,now).entitled,false);
});
test("server policy does not mistake other paid sources for a subscription", () => {
  assert.equal(hasAnyPremiumRight({}),false);
  assert.equal(hasAnyPremiumRight({lifetime:true}),true);
  assert.equal(hasAnyPremiumRight({code:true}),true);
  assert.equal(hasAnyPremiumRight({subscription:true}),true);
  assert.equal(hasAnyPremiumRight({lifetime:true,subscription:false}),true);
  assert.equal(hasAnyPremiumRight({code:true,subscription:false}),true);
});
