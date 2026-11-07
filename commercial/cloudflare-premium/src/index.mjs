/*
 * Matematik Macerası Premium API. Does not process Google Play purchases.
 * Environment secrets: CODE_PEPPER, LICENSE_PRIVATE_KEY (PKCS8 PEM),
 * LICENSE_PUBLIC_KEY (SPKI PEM); binding: DB; rate limit bindings.
 * No raw codes, personal identifiers or activation tokens are logged.
 */
const DAY = 86_400;
const RECHECK = 60 * DAY;
const GRACE = 7 * DAY;
const encoder = new TextEncoder();
const hex = bytes => [...new Uint8Array(bytes)].map(b => b.toString(16).padStart(2, "0")).join("");
const b64u = bytes => {
  let out = "";
  for (const b of new Uint8Array(bytes)) out += String.fromCharCode(b);
  return btoa(out).replace(/\+/g, "-").replace(/\//g, "_").replace(/=+$/g, "");
};
const unb64u = s => {
  if (!/^[A-Za-z0-9_-]+$/.test(s)) throw new Error("BAD_B64");
  const str = atob(s.replace(/-/g, "+").replace(/_/g, "/"));
  return Uint8Array.from(str, c => c.charCodeAt(0));
};
const pemBytes = pem => {
  if (typeof pem !== "string") throw new Error("KEY_NOT_CONFIGURED");
  const base64 = pem.replace(/-----BEGIN [^-]+-----/g, "")
    .replace(/-----END [^-]+-----/g, "").replace(/\s/g, "");
  if (!/^[A-Za-z0-9+/=]+$/.test(base64)) throw new Error("BAD_PEM");
  return Uint8Array.from(atob(base64), c => c.charCodeAt(0));
};
const json = (body, status = 200) => new Response(JSON.stringify(body), {
  status, headers: { "content-type": "application/json; charset=utf-8",
    "cache-control": "no-store", "x-content-type-options": "nosniff" }
});
const invalid = (status = 400) => json({ error: "CODE_UNAVAILABLE" }, status);
const validDevice = s => typeof s === "string" &&
  s.length >= 8 && s.length <= 128 && /^[a-zA-Z0-9_.:-]+$/.test(s);
const validCode = s => typeof s === "string" &&
  s.length >= 6 && s.length <= 128 &&
  /^[A-Za-z0-9_-]+$/.test(s);

async function digestTag(value) {
  return hex(await crypto.subtle.digest("SHA-256", encoder.encode(value)));
}
async function hashValue(env, kind, value) {
  const key = await crypto.subtle.importKey("raw", encoder.encode(env.CODE_PEPPER),
    { name: "HMAC", hash: "SHA-256" }, false, ["sign"]);
  return hex(await crypto.subtle.sign("HMAC", key, encoder.encode(kind + ":v1:" + value)));
}
async function rsaKey(pem, use) {
  return crypto.subtle.importKey(use === "sign" ? "pkcs8" : "spki",
    pemBytes(pem), { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" },
    false, [use]);
}
async function issueLicense(env, codeHash, deviceId, activationId) {
  const now = Math.floor(Date.now() / 1000);
  const header = b64u(encoder.encode(JSON.stringify({ alg: "RS256", typ: "JWT", kid: "mm1" })));
  const payload = b64u(encoder.encode(JSON.stringify({
    v: 1, aud: "matematik-macerasi-premium",
    code: codeHash, activation: activationId,
    device_tag: await digestTag("license-device:v1:" + deviceId),
    iat: now, recheck_at: now + RECHECK, exp: now + RECHECK + GRACE
  })));
  const data = header + "." + payload;
  const key = await rsaKey(env.LICENSE_PRIVATE_KEY, "sign");
  const sig = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", key, encoder.encode(data));
  return data + "." + b64u(sig);
}
async function verifyLicense(env, license) {
  if (typeof license !== "string" || license.length > 4000) return null;
  const parts = license.split(".");
  if (parts.length !== 3) return null;
  try {
    const header = JSON.parse(new TextDecoder().decode(unb64u(parts[0])));
    if (header.alg !== "RS256" || header.kid !== "mm1" || header.typ !== "JWT") return null;
    const key = await rsaKey(env.LICENSE_PUBLIC_KEY, "verify");
    const valid = await crypto.subtle.verify("RSASSA-PKCS1-v1_5", key,
      unb64u(parts[2]), encoder.encode(parts[0] + "." + parts[1]));
    if (!valid) return null;
    const claims = JSON.parse(new TextDecoder().decode(unb64u(parts[1])));
    if (claims.v !== 1 || claims.aud !== "matematik-macerasi-premium" ||
        typeof claims.code !== "string" || !/^[0-9a-f]{64}$/.test(claims.code) ||
        typeof claims.activation !== "string" || claims.activation.length > 64 ||
        typeof claims.device_tag !== "string" ||
        !/^[0-9a-f]{64}$/.test(claims.device_tag) ||
        !Number.isInteger(claims.iat) || !Number.isInteger(claims.exp) ||
        !Number.isInteger(claims.recheck_at)) return null;
    // The old signed token MAY be expired. Renewal requires server-side active
    // activation; allowing late online renewal avoids asking for the code again.
    if (claims.iat > Math.floor(Date.now() / 1000) + 120) return null;
    return claims;
  } catch { return null; }
}
async function bodyOf(request) {
  if (!request.headers.get("content-type")?.toLowerCase().startsWith("application/json"))
    throw new Error("BAD_CONTENT_TYPE");
  const raw = await request.text();
  if (raw.length > 4096) throw new Error("TOO_LARGE");
  const body = JSON.parse(raw);
  if (!body || Array.isArray(body) || typeof body !== "object") throw new Error("BAD_BODY");
  return body;
}
async function throttle(request, limiter) {
  if (!limiter) throw new Error("RATE_LIMITER_NOT_CONFIGURED");
  // CF-Connecting-IP is controlled by Cloudflare; never trust custom client headers.
  const ip = request.headers.get("cf-connecting-ip") || "unidentified";
  return (await limiter.limit({ key: ip })).success;
}
async function activate(request, env) {
  if (!await throttle(request, env.REDEEM_LIMITER)) return json({ error: "TOO_MANY_REQUESTS" }, 429);
  let body;
  try { body = await bodyOf(request); } catch { return invalid(); }
  if (!validCode(body.code) || !validDevice(body.deviceId)) return invalid();
  const codeHash = await hashValue(env, "code", body.code);
  const deviceHash = await hashValue(env, "device", body.deviceId);
  const existing = await env.DB.prepare(
    "SELECT code_hash, activation_id, status FROM code_activations WHERE device_hash=?"
  ).bind(deviceHash).first();
  if (existing) {
    if (existing.code_hash !== codeHash) return json({ error: "DEVICE_ALREADY_ACTIVE" }, 409);
    if (existing.status !== "active") return invalid();
    const current = await env.DB.prepare(
      "SELECT status FROM premium_codes WHERE code_hash=?"
    ).bind(codeHash).first();
    if (!current || current.status !== "active") return invalid();
    return json({ status: "ACTIVE",
      license: await issueLicense(env, codeHash, body.deviceId, existing.activation_id) });
  }
  const activationId = crypto.randomUUID();
  const now = Math.floor(Date.now() / 1000);
  try {
    const statements = [
      env.DB.prepare(`INSERT INTO code_activations
        (device_hash,code_hash,activation_id,status,activated_at,last_verified_at)
        SELECT ?,code_hash,?,'active',?,? FROM premium_codes
        WHERE code_hash=? AND status='active'
          AND (expires_on IS NULL OR expires_on >= ?)
          AND (max_uses IS NULL OR used_count < max_uses)`
      ).bind(deviceHash, activationId, now, now, codeHash,
        new Date().toISOString().slice(0, 10)),
      env.DB.prepare(`UPDATE premium_codes SET used_count=used_count+1
        WHERE code_hash=? AND EXISTS
        (SELECT 1 FROM code_activations WHERE device_hash=?
         AND activation_id=? AND code_hash=?)`
      ).bind(codeHash, deviceHash, activationId, codeHash)
    ];
    const result = await env.DB.batch(statements);
    if (result[0].meta?.changes !== 1 || result[1].meta?.changes !== 1) return invalid();
  } catch (err) {
    // Unique constraint on device_hash makes two concurrent redemptions safe.
    // On a genuine duplicate, re-read and return an idempotent license.
    const retry = await env.DB.prepare(
      "SELECT code_hash,activation_id,status FROM code_activations WHERE device_hash=?"
    ).bind(deviceHash).first();
    if (retry?.code_hash === codeHash && retry.status === "active") {
      const current = await env.DB.prepare(
        "SELECT status FROM premium_codes WHERE code_hash=?"
      ).bind(codeHash).first();
      if (current?.status === "active") return json({ status: "ACTIVE",
        license: await issueLicense(env, codeHash, body.deviceId, retry.activation_id) });
    }
    if (retry) return json({ error: "DEVICE_ALREADY_ACTIVE" }, 409);
    throw err;
  }
  return json({ status: "ACTIVE",
    license: await issueLicense(env, codeHash, body.deviceId, activationId) });
}
async function renew(request, env) {
  if (!await throttle(request, env.RENEW_LIMITER)) return json({ error: "TOO_MANY_REQUESTS" }, 429);
  let body;
  try { body = await bodyOf(request); } catch { return invalid(401); }
  if (!validDevice(body.deviceId)) return invalid(401);
  const claims = await verifyLicense(env, body.license);
  if (!claims || claims.device_tag !== await digestTag("license-device:v1:" + body.deviceId))
    return invalid(401);
  const deviceHash = await hashValue(env, "device", body.deviceId);
  const current = await env.DB.prepare(`
    SELECT a.code_hash, a.activation_id FROM code_activations a
    JOIN premium_codes c ON c.code_hash=a.code_hash
    WHERE a.device_hash=? AND a.status='active' AND c.status='active'
  `).bind(deviceHash).first();
  if (!current || current.activation_id !== claims.activation ||
      current.code_hash !== claims.code) return json({ error: "LICENSE_REVOKED" }, 403);
  await env.DB.prepare("UPDATE code_activations SET last_verified_at=? WHERE device_hash=?")
    .bind(Math.floor(Date.now() / 1000), deviceHash).run();
  return json({ status: "ACTIVE",
    license: await issueLicense(env, current.code_hash, body.deviceId, current.activation_id) });
}
export default {
  async fetch(request, env) {
    try {
      const url = new URL(request.url);
      if (request.method === "GET" && url.pathname === "/health") {
        return json({ ok: true, service: "mm-premium-v1" });
      }
      if (request.method !== "POST") return json({ error: "NOT_FOUND" }, 404);
      if (!env.DB || !env.CODE_PEPPER || !env.LICENSE_PRIVATE_KEY ||
          !env.LICENSE_PUBLIC_KEY) return json({ error: "NOT_CONFIGURED" }, 503);
      if (url.pathname === "/v1/activate") return activate(request, env);
      if (url.pathname === "/v1/renew") return renew(request, env);
      return json({ error: "NOT_FOUND" }, 404);
    } catch {
      // Never leak credentials or SQL errors to the caller.
      return json({ error: "SERVICE_UNAVAILABLE" }, 503);
    }
  }
};
export { hashValue, digestTag, issueLicense, verifyLicense, validCode, validDevice };
