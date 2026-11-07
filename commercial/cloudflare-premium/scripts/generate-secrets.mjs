#!/usr/bin/env node
/**
 * Generate all Cloudflare Premium API keys locally, without printing secrets.
 * Run once from commercial/cloudflare-premium:
 *   node scripts/generate-secrets.mjs
 * Never upload private/* to GitHub or share it in chat.
 */
import { generateKeyPairSync, randomBytes } from "node:crypto";
import { existsSync, mkdirSync, writeFileSync } from "node:fs";
import { join } from "node:path";
import { fileURLToPath } from "node:url";
import { dirname } from "node:path";

const root = dirname(dirname(fileURLToPath(import.meta.url)));
const dir = join(root, "private");
const names = [
  "secrets.json",
  "code-pepper.txt",
  "license-private.pem",
  "license-public.pem",
  "android-public-key.b64",
];
if (names.some(name => existsSync(join(dir, name)))) {
  console.error("Existing keys found in private/. Nothing was overwritten.");
  console.error("Do not regenerate keys: doing so would break existing licenses/codes.");
  process.exit(1);
}
mkdirSync(dir, { recursive: true, mode: 0o700 });
const { privateKey, publicKey } = generateKeyPairSync("rsa", {
  modulusLength: 3072,
  publicKeyEncoding: { type: "spki", format: "pem" },
  privateKeyEncoding: { type: "pkcs8", format: "pem" },
});
const pepper = randomBytes(32).toString("hex");
const files = {
  "secrets.json": JSON.stringify({
    CODE_PEPPER: pepper,
    LICENSE_PRIVATE_KEY: privateKey,
    LICENSE_PUBLIC_KEY: publicKey,
  }, null, 2) + "\n",
  "code-pepper.txt": pepper + "\n",
  "license-private.pem": privateKey,
  "license-public.pem": publicKey,
  "android-public-key.b64": publicKey
    .replace(/-----BEGIN PUBLIC KEY-----|-----END PUBLIC KEY-----|\s/g, "") + "\n",
};
for (const [name, data] of Object.entries(files)) {
  writeFileSync(join(dir, name), data, { flag: "wx", mode: 0o600 });
}
console.log("Local RSA-3072 signing keys and 256-bit CODE_PEPPER generated.");
console.log("Files saved to commercial/cloudflare-premium/private/ (gitignored).");
console.log("NO secrets were printed. Store an encrypted backup of private/.");
console.log("Next: npx wrangler deploy --secrets-file private/secrets.json");
