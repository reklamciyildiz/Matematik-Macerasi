/**
 * First secure boundary: ONLY a trusted Worker may call Android Publisher API.
 * Does not itself grant rights, acknowledge purchases, or accept mobile JSON.
 * Keep subscriptions feature-gated until account/install binding, RTDN and
 * entitlement lease validation are implemented and tested end-to-end.
 */
import { evaluateVerifiedSubscription } from "./entitlement.mjs";

export const PACKAGE_NAME = "com.yigit033.matematikmacarasi";
const SCOPE = "https://www.googleapis.com/auth/androidpublisher";
export const INTEGRITY_SCOPE = "https://www.googleapis.com/auth/playintegrity";
export const PUBSUB_SCOPE = "https://www.googleapis.com/auth/pubsub";
const SAFE_SCOPES = new Set([SCOPE,INTEGRITY_SCOPE,PUBSUB_SCOPE]);
const TOKEN_URL = "https://oauth2.googleapis.com/token";
const encoder = new TextEncoder();
const b64url = bytes => {
  let raw = "";
  for (const byte of bytes) raw += String.fromCharCode(byte);
  return btoa(raw).replaceAll("+", "-").replaceAll("/", "_").replace(/=+$/, "");
};
const jwtPart = value => b64url(encoder.encode(JSON.stringify(value)));

function serviceAccountInfo(account) {
  if (!account || typeof account !== "object" ||
      typeof account.client_email !== "string" ||
      !/^[^\s@]+@[^\s@]+\.iam\.gserviceaccount\.com$/.test(account.client_email) ||
      typeof account.private_key !== "string" ||
      !account.private_key.includes("-----BEGIN PRIVATE KEY-----")) {
    throw new Error("PLAY_SERVICE_ACCOUNT_NOT_CONFIGURED");
  }
  return account;
}

function importPrivateKey(pem) {
  const base64 = pem.replace("-----BEGIN PRIVATE KEY-----", "")
    .replace("-----END PRIVATE KEY-----", "").replace(/\s/g, "");
  if (!/^[A-Za-z0-9+/=]+$/.test(base64)) throw new Error("BAD_SERVICE_ACCOUNT_KEY");
  return crypto.subtle.importKey("pkcs8",
    Uint8Array.from(atob(base64), c => c.charCodeAt(0)),
    {name:"RSASSA-PKCS1-v1_5",hash:"SHA-256"}, false, ["sign"]);
}

/** Server-side credential exchange. Never package this key in Android. */
export async function createGoogleAccessToken(account, {
  fetchImpl = fetch,
  nowMs = Date.now(), scope = SCOPE,
} = {}) {
  const valid = serviceAccountInfo(account);
  if (!Number.isFinite(nowMs) || nowMs < 0) throw new Error("INVALID_CLOCK");
  if (!SAFE_SCOPES.has(scope)) throw new Error("UNSUPPORTED_GOOGLE_SCOPE");
  const issued = Math.floor(nowMs / 1000);
  const unsigned = jwtPart({alg:"RS256",typ:"JWT"}) + "." +
    jwtPart({iss:valid.client_email,scope,aud:TOKEN_URL,
      iat:issued,exp:issued+3000});
  const key = await importPrivateKey(valid.private_key);
  const signature = b64url(new Uint8Array(await crypto.subtle.sign(
    "RSASSA-PKCS1-v1_5", key, encoder.encode(unsigned))));
  const res = await fetchImpl(TOKEN_URL, {
    method:"POST",headers:{"content-type":"application/x-www-form-urlencoded"},
    body:new URLSearchParams({
      grant_type:"urn:ietf:params:oauth:grant-type:jwt-bearer",
      assertion:unsigned+"."+signature,
    }).toString(),
  });
  if (!res.ok) throw new Error("PLAY_AUTH_FAILED");
  const json = await res.json();
  if (typeof json.access_token !== "string" || json.access_token.length < 20)
    throw new Error("PLAY_AUTH_INVALID_RESPONSE");
  return json.access_token;
}

/**
 * Fetch authoritative subscriptionsv2 response. The Android purchase token
 * is untrusted until THIS server-to-server lookup succeeds. Only the policy
 * result is returned; callers must also bind ownership and acknowledge.
 */
export async function verifyGoogleSubscriptionToken(purchaseToken, {
  serviceAccount, fetchImpl = fetch, nowMs = Date.now(),
  getAccessToken = createGoogleAccessToken,
} = {}) {
  if (typeof purchaseToken !== "string" || purchaseToken.length < 16 ||
      purchaseToken.length > 4096 || /\s/.test(purchaseToken))
    throw new Error("INVALID_PURCHASE_TOKEN");
  const bearer = await getAccessToken(serviceAccount, {fetchImpl,nowMs});
  if (typeof bearer !== "string" || !bearer)
    throw new Error("PLAY_AUTH_FAILED");
  const url = "https://androidpublisher.googleapis.com/androidpublisher/v3/" +
    "applications/" + PACKAGE_NAME + "/purchases/subscriptionsv2/tokens/" +
    encodeURIComponent(purchaseToken);
  const response = await fetchImpl(url, {
    method:"GET",headers:{"authorization":"Bearer "+bearer},
  });
  // Network errors, stale/unknown tokens, permissions failures: fail closed.
  if (!response.ok) throw new Error("PLAY_SUBSCRIPTION_VERIFICATION_FAILED");
  const verified = await response.json();
  const entitlement = evaluateVerifiedSubscription(verified, nowMs);
  return {
    ...entitlement,
    // Only acknowledge after the associated legitimate owner has been
    // recorded and entitlement granted, not merely because API returned 200.
    needsAcknowledgement: verified.acknowledgementState ===
      "ACKNOWLEDGEMENT_STATE_PENDING" && entitlement.entitled,
  };
}
