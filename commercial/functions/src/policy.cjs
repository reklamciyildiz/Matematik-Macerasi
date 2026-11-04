"use strict";

const crypto = require("node:crypto");

const PACKAGE = "com.yigit033.matematikmacarasi";
const PREMIUM_PRODUCT = "matematik_macerasi_premium";
const VERSION = 1;

function tokenHash(token) {
  if (typeof token !== "string" || token.length < 8 || token.length > 4096 ||
      token.trim() !== token || /[\x00-\x1f\x7f]/.test(token)) {
    throw new Error("INVALID_TOKEN");
  }
  return crypto.createHash("sha256").update(token, "utf8").digest("hex");
}

function accountTag(uid) {
  if (typeof uid !== "string" || !uid.trim() || uid.length > 128) {
    throw new Error("INVALID_ACCOUNT");
  }
  return crypto.createHash("sha256").update("mm-parent-account-v1:" + uid).digest("hex");
}

/** Fail closed: never grant for PENDING, CANCELLED, unknown SKU, consumed or refunded. */
function classifyGoogleProduct(purchase, uid) {
  if (!purchase || typeof purchase !== "object") return "INVALID";
  const state = purchase.purchaseStateContext?.purchaseState;
  if (state === "PENDING") return "PENDING";
  if (state === "CANCELLED") return "CANCELLED";
  if (state !== "PURCHASED") return "INVALID";
  const items = Array.isArray(purchase.productLineItem) ? purchase.productLineItem : [];
  if (items.length !== 1 || items[0]?.productId !== PREMIUM_PRODUCT) return "INVALID";
  const offer = items[0].productOfferDetails;
  if (offer?.consumptionState === "CONSUMPTION_STATE_CONSUMED") return "INVALID";
  if (offer?.refundableQuantity !== undefined && offer.refundableQuantity <= 0) {
    return "CANCELLED";
  }
  // Older purchases may have no obfuscated account ID; preserve their eligibility
  // for migration, but never allow a token to transfer to a different server UID.
  const obfuscated = purchase.obfuscatedExternalAccountId;
  if (obfuscated && obfuscated !== accountTag(uid)) return "WRONG_ACCOUNT";
  return "PURCHASED";
}

module.exports = {PACKAGE, PREMIUM_PRODUCT, VERSION, tokenHash, accountTag, classifyGoogleProduct};
