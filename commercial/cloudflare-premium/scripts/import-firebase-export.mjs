#!/usr/bin/env node
// OFFLINE, ONE-TIME legacy migration preparation. Never upload RTDB JSON to GitHub.
// Source data might have been client-writable: review counts and activations before
// applying the resulting SQL to production.
import { createHmac, randomUUID } from "node:crypto";
import { readFileSync, writeFileSync } from "node:fs";
const [sourcePath, outputPath] = process.argv.slice(2);
const pepper = process.env.CODE_PEPPER;
if (!sourcePath || !outputPath || !pepper || pepper.length < 32 ||
    !/\.sql$/i.test(outputPath)) {
  console.error("Usage: CODE_PEPPER=... node scripts/import-firebase-export.mjs backup.json import.sql");
  process.exit(1);
}
const source = JSON.parse(readFileSync(sourcePath, "utf8"));
const codes = source.premium_codes || {};
const activations = source.code_activations || {};
const quote = s => "'" + String(s).replaceAll("'", "''") + "'";
const hash = (kind, value) => createHmac("sha256", pepper)
  .update(kind + ":v1:" + value).digest("hex");
const rowCount = new Map();
const safeDate = d => (!d || d === "") ? null :
  /^\d{4}-\d{2}-\d{2}$/.test(d) ? d : (()=>{throw new Error("Bad expiry date")})();
const cleanCount = x => Number.isSafeInteger(x) && x >= 0 ? x : (()=>{throw new Error("Bad usage count")})();
const lines = ["-- REVIEW ALL LEGACY COUNTS/OWNERS BEFORE APPLYING; D1 wraps file imports in its own transaction."];
for (const [device, activation] of Object.entries(activations)) {
  if (!activation || typeof activation.code !== "string" || !Object.hasOwn(codes, activation.code))
    throw new Error("Unknown code in existing device activation");
  if (typeof device !== "string" || !/^[a-zA-Z0-9_.:-]{8,128}$/.test(device))
    throw new Error("Unrecognized device ID; review source");
  rowCount.set(activation.code, (rowCount.get(activation.code) || 0) + 1);
}
for (const [code, data] of Object.entries(codes)) {
  if (!/^[A-Za-z0-9_-]{6,128}$/.test(code)) throw new Error("Legacy code has unsafe format: manually review");
  const max = data.max_uses == null ? null : cleanCount(data.max_uses);
  if (max === 0) throw new Error("Zero max_uses found");
  const used = Math.max(cleanCount(data.used_count ?? 0), rowCount.get(code) || 0);
  if (max !== null && used > max) throw new Error("Over-capacity legacy code: reconcile manually");
  const expiry = safeDate(data.expiry_date);
  lines.push(`INSERT INTO premium_codes (code_hash,institution,max_uses,used_count,expires_on,status,created_at)
VALUES (${quote(hash("code",code))},${quote(String(data.institution || "").slice(0,120))},
${max===null?"NULL":max},${used},${expiry?quote(expiry):"NULL"},
${data.active===false?"'revoked'":"'active'"},${Math.floor(Date.now()/1000)});
`);
}
for (const [device, activation] of Object.entries(activations)) {
  const activatedAt = Number(activation.activated_at);
  const ts = Number.isFinite(activatedAt) && activatedAt > 0
    ? Math.floor(activatedAt/1000) : Math.floor(Date.now()/1000);
  lines.push(`INSERT INTO code_activations
(device_hash,activation_id,code_hash,status,activated_at,last_verified_at)
VALUES (${quote(hash("device",device))},${quote(randomUUID())},
${quote(hash("code",activation.code))},'active',${ts},${ts});
`);
}
// D1 rejects explicit BEGIN/COMMIT in wrangler d1 execute --file.
writeFileSync(outputPath, lines.join("\n")+"\n", {flag:"wx", mode:0o600});
console.log("Prepared SQL (contains hashes, not raw codes): "+outputPath);
console.log("Codes: "+Object.keys(codes).length+"; activations: "+Object.keys(activations).length);
console.warn("STOP: imported counters/ownership came from legacy client-writable records.");
console.warn("Review source backup privately, test offline, and do not deploy before QA.");
