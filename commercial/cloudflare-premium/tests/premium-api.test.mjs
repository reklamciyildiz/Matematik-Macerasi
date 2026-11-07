import test from "node:test";
import assert from "node:assert/strict";
import { generateKeyPairSync, randomBytes } from "node:crypto";
import worker, { hashValue, verifyLicense, validCode } from "../src/index.mjs";

const keys = generateKeyPairSync("rsa", { modulusLength: 2048,
  publicKeyEncoding: { type: "spki", format: "pem" },
  privateKeyEncoding: { type: "pkcs8", format: "pem" }
});
class FakeD1 {
  codes = new Map();
  activations = new Map();
  prepare(sql) {
    return { bind: (...params) => ({
      sql, params,
      first: async () => {
        if (sql.includes("FROM code_activations WHERE device_hash=?")) {
          return this.activations.get(params[0]) ?? null;
        }
        if (sql.includes("SELECT status FROM premium_codes")) {
          const row = this.codes.get(params[0]);
          return row ? { status: row.status } : null;
        }
        if (sql.includes("JOIN premium_codes c")) {
          const activation = this.activations.get(params[0]);
          const code = activation && this.codes.get(activation.code_hash);
          return activation?.status === "active" && code?.status === "active"
            ? { code_hash: activation.code_hash,
                activation_id: activation.activation_id } : null;
        }
        throw new Error("unexpected read: " + sql);
      },
      run: async () => {
        if (sql.includes("SET last_verified_at")) {
          const row=this.activations.get(params[1]);
          if (row) row.last_verified_at=params[0];
          return { success: true };
        }
        throw new Error("unexpected write: " + sql);
      }
    }) };
  }
  async batch([insert, update]) {
    const [deviceHash, id, now, verifyAt, codeHash, date] = insert.params;
    const row = this.codes.get(codeHash);
    if (this.activations.has(deviceHash)) throw new Error("UNIQUE");
    if (!row || row.status !== "active" ||
        (row.expires_on && row.expires_on < date) ||
        (row.max_uses !== null && row.used_count >= row.max_uses)) {
      return [{ meta: { changes: 0 } }, { meta: { changes: 0 } }];
    }
    assert.equal(update.params[0], codeHash);
    this.activations.set(deviceHash, { code_hash: codeHash,
      activation_id: id, status: "active", activated_at: now, last_verified_at: verifyAt });
    row.used_count += 1;
    return [{ meta: { changes: 1 } }, { meta: { changes: 1 } }];
  }
}
function setup() {
  return {
    DB: new FakeD1(), CODE_PEPPER: randomBytes(32).toString("hex"),
    LICENSE_PRIVATE_KEY: keys.privateKey, LICENSE_PUBLIC_KEY: keys.publicKey,
    REDEEM_LIMITER: { limit: async () => ({ success: true }) },
    RENEW_LIMITER: { limit: async () => ({ success: true }) }
  };
}
async function call(env, path, data) {
  const response = await worker.fetch(new Request("https://example.workers.dev" + path, {
    method: "POST", headers: { "content-type": "application/json",
      "cf-connecting-ip": "127.0.0.1" }, body: JSON.stringify(data)
  }), env);
  return { status: response.status, data: await response.json() };
}
async function seed(env, code, max_uses = 2) {
  const hash = await hashValue(env, "code", code);
  env.DB.codes.set(hash, { used_count: 0, max_uses, status: "active", expires_on: null });
  return hash;
}

test("legacy printable code formats are accepted, non-printable formats are rejected", async () => {
  assert.equal(validCode("MM+12@AB"), true);
  assert.equal(validCode("ABC DEF8"), true);
  assert.equal(validCode("ÖĞREN123"), true);
  assert.equal(validCode("ABC\\nDEF8"), false);
  assert.equal(validCode(" ABCDEF8"), false);
  const env = setup();
  const code = "MM+12@AB";
  await seed(env, code, 1);
  const reply = await call(env, "/v1/activate", { code, deviceId: "device-0001" });
  assert.equal(reply.status, 200);
  assert.ok((await verifyLicense(env, reply.data.license))?.activation);
});

test("first activation grants a signature that verifies, duplicate does not consume another seat", async () => {
  const env = setup();
  const code = "MM-EXAMPLE-CODE-TEST";
  const hash = await seed(env, code, 1);
  const first = await call(env, "/v1/activate", { code, deviceId: "device-0001" });
  assert.equal(first.status, 200);
  assert.ok((await verifyLicense(env, first.data.license)).activation);
  const again = await call(env, "/v1/activate", { code, deviceId: "device-0001" });
  assert.equal(again.status, 200);
  assert.equal(env.DB.codes.get(hash).used_count, 1);
  const other = await call(env, "/v1/activate", { code, deviceId: "device-0002" });
  assert.equal(other.status, 400);
});

test("other code on the same device is denied; unknown codes do not create rows", async () => {
  const env = setup();
  await seed(env, "MM-ALPHA-TEST-CODE", 5);
  await seed(env, "MM-BRAVO-TEST-CODE", 5);
  assert.equal((await call(env, "/v1/activate", { code: "MM-ALPHA-TEST-CODE",
    deviceId: "device-0001" })).status, 200);
  assert.equal((await call(env, "/v1/activate", { code: "MM-BRAVO-TEST-CODE",
    deviceId: "device-0001" })).status, 409);
  assert.equal((await call(env, "/v1/activate", { code: "NOT-IN-DATABASE",
    deviceId: "device-0003" })).status, 400);
  assert.equal(env.DB.activations.size, 1);
});

test("signed token renews after 60 days, and a revoked code cannot renew", async () => {
  const env = setup();
  const code = "MM-RENEW-TEST-CODE";
  const hash = await seed(env, code);
  const first = await call(env, "/v1/activate", { code, deviceId: "device-0001" });
  const original = Date.now;
  try {
    Date.now = () => original() + 70 * 86400_000;
    const renewed = await call(env, "/v1/renew",
      { license: first.data.license, deviceId: "device-0001" });
    assert.equal(renewed.status, 200); // expired token can still prove original activation
    const otherDevice = await call(env, "/v1/renew",
      { license: first.data.license, deviceId: "device-FAKE" });
    assert.equal(otherDevice.status, 401);
    env.DB.codes.get(hash).status = "revoked";
    const revoked = await call(env, "/v1/renew",
      { license: renewed.data.license, deviceId: "device-0001" });
    assert.equal(revoked.status, 403);
  } finally { Date.now = original; }
});

test("tampered token and throttling are rejected", async () => {
  const env = setup();
  const code = "MM-TAMPER-TEST-CODE";
  await seed(env, code);
  const token = (await call(env, "/v1/activate",
    { code, deviceId: "device-0001" })).data.license;
  const tampered = token.slice(0, -3) + "AAA";
  assert.equal((await call(env, "/v1/renew",
    { license: tampered, deviceId: "device-0001" })).status, 401);
  env.REDEEM_LIMITER = { limit: async () => ({ success: false }) };
  assert.equal((await call(env, "/v1/activate",
    { code, deviceId: "device-0004" })).status, 429);
});
