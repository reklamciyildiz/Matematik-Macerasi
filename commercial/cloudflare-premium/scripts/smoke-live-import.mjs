#!/usr/bin/env node
/**
 * Production smoke test for the *already imported* Firebase RTDB records.
 *
 * Run locally in commercial/cloudflare-premium:
 *   node scripts/smoke-live-import.mjs
 *
 * Never sends the backup to anyone except this project's configured Worker,
 * never logs real codes, device IDs, signatures, hashes or tokens.
 * Only exercises existing activations. Renew updates last_verified_at;
 * no new code seats should be consumed.
 *
 * Safety guard: first test must be an *expired* code on an already activated
 * device. If DB import/pepper mismatch exists, that request cannot create a new
 * activation because the code is expired. Then verify the deployed RSA key and
 * HMAC code hash before testing other existing activations.
 */
import assert from "node:assert/strict";
import { createHmac, createHash, randomBytes, randomUUID, verify } from "node:crypto";
import { readFileSync } from "node:fs";
import { validPremiumCode } from "../src/code-format.mjs";

const endpoint = "https://matematik-macerasi-premium.matematik-macerasi-yigit033.workers.dev";
const load = (path) => readFileSync(path, "utf8");
let source;
let pepper;
let publicKey;
try {
  source = JSON.parse(load("private/firebase-backup.json"));
  pepper = load("private/code-pepper.txt").trim();
  publicKey = load("private/license-public.pem");
  assert.ok(pepper.length >= 32);
  assert.ok(source?.premium_codes && source?.code_activations);
} catch {
  console.error("Gerekli private/ Firebase yedegi, pepper veya public key okunamadi.");
  process.exit(2);
}
const codeHash = code => createHmac("sha256", pepper)
  .update("code:v1:" + code).digest("hex");
const deviceTag = id => createHash("sha256")
  .update("license-device:v1:" + id).digest("hex");

function inspectJwt(token, code, deviceId) {
  assert.equal(typeof token, "string");
  const parts = token.split(".");
  assert.equal(parts.length, 3, "JWT formati bozuk");
  const signedMessage = parts[0] + "." + parts[1];
  assert.ok(verify("RSA-SHA256", Buffer.from(signedMessage),
    publicKey, Buffer.from(parts[2], "base64url")), "RSA imzasi eslesmedi");
  const header = JSON.parse(Buffer.from(parts[0], "base64url").toString());
  assert.equal(header.alg, "RS256");
  const claims = JSON.parse(Buffer.from(parts[1], "base64url").toString());
  assert.equal(claims.aud, "matematik-macerasi-premium");
  assert.equal(claims.code, codeHash(code), "D1 kod ozeti / pepper uyumsuz");
  assert.equal(claims.device_tag, deviceTag(deviceId), "Cihaz lisans etiketi uyumsuz");
  assert.ok(Number.isInteger(claims.iat));
  assert.equal(claims.recheck_at - claims.iat, 60 * 86400);
  assert.equal(claims.exp - claims.recheck_at, 7 * 86400);
  assert.ok(typeof claims.activation === "string" && claims.activation.length > 0);
  return claims;
}

async function post(route, data) {
  const response = await fetch(endpoint + route, {
    method: "POST", headers: { "content-type": "application/json" },
    body: JSON.stringify(data), signal: AbortSignal.timeout(12000)
  });
  const json = await response.json().catch(() => ({}));
  return { status: response.status, body: json };
}

async function testImportedActivation(deviceId, record, n) {
  const code = record.code;
  assert.ok(validPremiumCode(code), "Yedekte desteklenmeyen eski kod formati");
  const response = await post("/v1/activate", { code, deviceId });
  assert.equal(response.status, 200, "Mevcut cihaz #" + n + " dogrulanamadi");
  assert.equal(response.body.status, "ACTIVE");
  const first = inspectJwt(response.body.license, code, deviceId);
  const renewed = await post("/v1/renew",
    { license: response.body.license, deviceId });
  assert.equal(renewed.status, 200, "Cihaz #" + n + " yenileme basarisiz");
  assert.equal(renewed.body.status, "ACTIVE");
  const second = inspectJwt(renewed.body.license, code, deviceId);
  assert.equal(second.activation, first.activation, "Lisans aktivasyonu degisti");
  return renewed.body.license;
}

async function main() {
  const activations = Object.entries(source.code_activations);
  const today = new Date().toISOString().slice(0, 10);
  const expired = activations.find(([, record]) => {
    const row = source.premium_codes[record.code];
    return row && row.active !== false && row.expiry_date &&
      row.expiry_date < today;
  });
  assert.ok(expired, "Guvenli on kontrol icin suresi dolmus eski aktivasyon bulunamadi");
  assert.equal(activations.length, 4,
    "Beklenen 4 aktivasyon degismis: yeni Firebase snapshot gerekli");

  const health = await fetch(endpoint + "/health", { signal: AbortSignal.timeout(12000) });
  assert.equal(health.status, 200);

  // A bad migration/key cannot accidentally consume a slot for expired codes.
  const [expiredDevice, expiredRecord] = expired;
  const token = await testImportedActivation(expiredDevice, expiredRecord, 1);
  console.log("PASS: suresi dolmus eski kod mevcut cihazda calisiyor; imza ve hash dogru");

  let verified = 1;
  for (const [deviceId, record] of activations) {
    if (deviceId === expiredDevice) continue;
    await testImportedActivation(deviceId, record, verified + 1);
    verified++;
  }
  console.log("PASS: " + verified + "/4 eski cihaz aktivasyonu ve kodsuz yenileme");

  const wrongDevice = await post("/v1/renew",
    { license: token, deviceId: "wrong-" + randomUUID() });
  assert.equal(wrongDevice.status, 401);
  console.log("PASS: baska cihaz imzali lisansi kullanamiyor");

  const forbiddenNew = await post("/v1/activate",
    { code: expiredRecord.code, deviceId: "new-" + randomUUID() });
  assert.equal(forbiddenNew.status, 400);
  console.log("PASS: suresi biten kod yeni cihazda kullanilamiyor");

  const unknown = await post("/v1/activate", {
    code: "MM-UNKNOWN-" + randomBytes(20).toString("hex"),
    deviceId: "new-" + randomUUID()
  });
  assert.equal(unknown.status, 400);
  console.log("PASS: bilinmeyen Premium kodu reddediliyor");

  console.log("SONUC: canlı API entegrasyon testi basarili; eski kodlar / cihazlar gizli tutuldu.");
  console.log("NOT: Bu, Android APK / Google Play Billing / 60 gunluk cihaz testi degildir.");
}
main().catch(error => {
  // Always redact backup contents even if an assertion fails.
  console.error("FAIL: " + String(error.message).slice(0, 180));
  process.exitCode = 1;
});
