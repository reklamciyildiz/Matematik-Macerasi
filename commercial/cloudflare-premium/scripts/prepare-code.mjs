#!/usr/bin/env node
// Generate high-entropy codes or revoke known codes without exposing the HMAC key.
// CODE_PEPPER is a local environment secret; only the HMAC digest goes to D1.
import { randomBytes, createHmac } from "node:crypto";
import { writeFileSync } from "node:fs";
const [action, ...args] = process.argv.slice(2);
const get = flag => { const i=args.indexOf(flag); return i < 0 ? undefined : args[i+1]; };
const pepper = process.env.CODE_PEPPER;
if (!pepper || pepper.length < 32) {
  console.error("Set CODE_PEPPER (32+ characters) locally. Never commit it.");
  process.exit(1);
}
const sha = code => createHmac("sha256", pepper).update("code:v1:" + code).digest("hex");
const quote = s => "'" + String(s).replaceAll("'", "''") + "'";
const out = get("--sql");
if (!out || !/\.sql$/i.test(out)) {
  console.error("Usage: node scripts/prepare-code.mjs create --uses 3 --sql ./seed.sql [--institution TEXT] [--expires YYYY-MM-DD]");
  console.error("    or node scripts/prepare-code.mjs revoke --code CODE --sql ./revoke.sql");
  process.exit(1);
}
if (action === "create") {
  const code = "MM-" + randomBytes(20).toString("hex").toUpperCase();
  const max = get("--uses") === "unlimited" ? null : Number(get("--uses") ?? 1);
  const institution = get("--institution") ?? "";
  const expires = get("--expires") ?? null;
  if ((max !== null && (!Number.isSafeInteger(max) || max < 1)) ||
      (expires && !/^\d{4}-\d{2}-\d{2}$/.test(expires)) ||
      institution.length > 120) throw new Error("Invalid options");
  const sql = `INSERT INTO premium_codes
(code_hash,institution,max_uses,used_count,expires_on,status,created_at)
VALUES (${quote(sha(code))}, ${quote(institution)}, ${max===null?"NULL":max}, 0,
${expires?quote(expires):"NULL"}, 'active', ${Math.floor(Date.now()/1000)});
`;
  writeFileSync(out, sql, { flag: "wx", mode: 0o600 });
  console.log("NEW CODE (save privately; only displayed once): " + code);
  console.log("Hashed SQL written to: " + out);
} else if (action === "revoke") {
  const code = get("--code");
  if (!code || code.length > 128) throw new Error("Supply --code");
  writeFileSync(out, `UPDATE premium_codes SET status='revoked' WHERE code_hash=${quote(sha(code))};\n`, {
    flag: "wx", mode: 0o600
  });
  console.log("Revoke SQL written to: " + out);
} else { throw new Error("Unknown action"); }
