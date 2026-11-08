#!/usr/bin/env node
// Local, read-only legacy Firebase vs Cloudflare D1 reconciliation.
// Never logs actual codes, device IDs, code pepper or hashes.
// Generated SQL contains hashes and MUST remain in private/ (gitignored).
import { createHmac } from "node:crypto";
import { readFileSync, writeFileSync } from "node:fs";
import { validPremiumCode } from "../src/code-format.mjs";

let backup, pepper;
try {
  backup = JSON.parse(readFileSync("private/firebase-backup.json", "utf8"));
  pepper = readFileSync("private/code-pepper.txt", "utf8").trim();
  if (pepper.length < 32) throw new Error("weak pepper");
} catch {
  console.error("Missing/invalid private Firebase backup or code pepper.");
  process.exit(2);
}
const object = x => x !== null && typeof x === "object" && !Array.isArray(x);
if (!object(backup?.premium_codes) || !object(backup?.code_activations)) {
  console.error("Legacy data is not in expected Firebase format."); process.exit(2);
}
const codes = Object.entries(backup.premium_codes);
const activations = Object.entries(backup.code_activations);
if (!codes.length || !activations.length) {
  console.error("No code/activation records found. Stop."); process.exit(2);
}
const hash = (kind, value) => createHmac("sha256", pepper)
  .update(kind + ":v1:" + value).digest("hex");
const codeRows = [];
for (const [code, row] of codes) {
  if (!validPremiumCode(code) || !object(row)) {
    console.error("Invalid legacy code record. Stop."); process.exit(2);
  }
  codeRows.push("('" + hash("code", code) + "','" +
    (row.active === false ? "revoked" : "active") + "')");
}
const deviceRows = [];
for (const [device, row] of activations) {
  if (!/^[a-zA-Z0-9_.:-]{8,128}$/.test(device) ||
      !object(row) || typeof row.code !== "string" ||
      !Object.hasOwn(backup.premium_codes, row.code)) {
    console.error("Invalid legacy activation. Stop."); process.exit(2);
  }
  deviceRows.push("('" + hash("device", device) + "','" +
    hash("code", row.code) + "')");
}
const sql = [
  "-- SELECT only. Keep this generated hash-bearing file private.",
  "WITH expected_codes(code_hash,expected_status) AS (VALUES",
  codeRows.join(",\n") + "),",
  "expected_devices(device_hash,code_hash) AS (VALUES",
  deviceRows.join(",\n") + ")",
  "SELECT",
  " (SELECT count(*) FROM expected_codes) AS expected_codes,",
  " (SELECT count(*) FROM expected_codes e JOIN premium_codes p",
  " ON e.code_hash=p.code_hash AND e.expected_status=p.status) AS matching_codes,",
  " (SELECT count(*) FROM expected_devices) AS expected_activations,",
  " (SELECT count(*) FROM expected_devices e JOIN code_activations a",
  " ON a.device_hash=e.device_hash AND a.code_hash=e.code_hash",
  " AND a.status='active') AS matching_activations,",
  " (SELECT count(*) FROM code_activations a LEFT JOIN premium_codes p",
  " ON p.code_hash=a.code_hash WHERE p.code_hash IS NULL) AS orphans,",
  " (SELECT count(*) FROM premium_codes p",
  " WHERE p.max_uses IS NOT NULL AND p.used_count>p.max_uses) AS over_limit;",
].join("\n") + "\n";
const target = "private/verify-legacy-readonly.sql";
try {
  writeFileSync(target, sql, {flag:"wx",mode:0o600});
} catch {
  console.error("Cannot write private audit SQL (perhaps it already exists). Stop.");
  process.exit(2);
}
console.log("Prepared SELECT-only audit file; no database modifications.");
console.log("Legacy counts: " + codes.length + " codes, " + activations.length + " activations.");
console.log("Execute via: npx wrangler d1 execute matematik-macerasi-premium --remote --file " + target);
console.log("Share ONLY the aggregate result columns, NEVER the generated SQL file.");
