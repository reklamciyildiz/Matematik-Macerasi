/**
 * Validate a STANDARD Play Integrity token server-side before processing a
 * purchase. Google supplies a signed verdict after service-account OAuth.
 * requestHash binds THIS installed app action to the specific purchase token
 * and device ID. Play Integrity alone is NOT proof of the subscription.
 */
import { PACKAGE_NAME, INTEGRITY_SCOPE, createGoogleAccessToken } from "./google-play-verifier.mjs";
const encoder = new TextEncoder();
const hex = bytes => [...new Uint8Array(bytes)].map(b => b.toString(16).padStart(2,"0")).join("");

export async function requestBindingHash(token, deviceId) {
  if (typeof token !== "string" || token.length < 16 || token.length > 4096 ||
      typeof deviceId !== "string" || !/^[A-Za-z0-9_.:-]{8,128}$/.test(deviceId))
    throw new Error("INVALID_BINDING_INPUT");
  return hex(await crypto.subtle.digest("SHA-256",
    encoder.encode("mm-play-subscription:v1:" + deviceId + ":" + token)));
}

export function validateIntegrityVerdict(payload, expectedHash, nowMs = Date.now()) {
  const verdict = payload?.tokenPayloadExternal;
  const request = verdict?.requestDetails;
  const app = verdict?.appIntegrity;
  const device = verdict?.deviceIntegrity;
  if (!request || !app || !device || typeof expectedHash !== "string" ||
      !Number.isFinite(nowMs)) return false;
  const timestamp = Number(request.timestampMillis);
  if (!Number.isFinite(timestamp) || Math.abs(nowMs-timestamp) > 2*60*1000)
    return false;
  if (request.requestPackageName !== PACKAGE_NAME ||
      app.packageName !== PACKAGE_NAME ||
      request.requestHash !== expectedHash ||
      app.appRecognitionVerdict !== "PLAY_RECOGNIZED")
    return false;
  const labels = device.deviceRecognitionVerdict;
  // Recognized legitimate Android device. Handle unusual/old devices through
  // an explicit support process, NEVER skip package or requestHash checks.
  return Array.isArray(labels) && labels.some(label =>
    label === "MEETS_BASIC_INTEGRITY" || label === "MEETS_DEVICE_INTEGRITY" ||
    label === "MEETS_STRONG_INTEGRITY");
}

export async function verifyPlayIntegrity(integrityToken, purchaseToken, deviceId, {
  serviceAccount, fetchImpl = fetch, nowMs = Date.now(),
  getAccessToken = createGoogleAccessToken,
} = {}) {
  if (typeof integrityToken !== "string" || integrityToken.length < 50 ||
      integrityToken.length > 14000) throw new Error("INVALID_INTEGRITY_TOKEN");
  const hash = await requestBindingHash(purchaseToken,deviceId);
  const accessToken = await getAccessToken(serviceAccount, {
    fetchImpl,nowMs,scope:INTEGRITY_SCOPE
  });
  if (typeof accessToken !== "string" || !accessToken)
    throw new Error("PLAY_INTEGRITY_AUTH_FAILED");
  const res = await fetchImpl(
    "https://playintegrity.googleapis.com/v1/" + PACKAGE_NAME + ":decodeIntegrityToken",
    { method:"POST",
      headers:{"content-type":"application/json","authorization":"Bearer "+accessToken},
      body:JSON.stringify({integrity_token:integrityToken})
    });
  if (!res.ok) throw new Error("PLAY_INTEGRITY_DECODE_FAILED");
  const response = await res.json();
  if (!validateIntegrityVerdict(response,hash,nowMs))
    throw new Error("PLAY_INTEGRITY_REJECTED");
  return true;
}
