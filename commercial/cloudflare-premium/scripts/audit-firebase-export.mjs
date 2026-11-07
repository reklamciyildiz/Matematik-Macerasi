#!/usr/bin/env node
/**
 * Read-only audit of an exported Firebase RTDB Premium data snapshot.
 * Displays counts and data-health flags only: never prints actual Premium
 * codes, device IDs, activation records or the full source JSON.
 *
 * Run from commercial/cloudflare-premium:
 *   node scripts/audit-firebase-export.mjs
 */
import { readFileSync } from "node:fs";
import { resolve } from "node:path";

const filepath = process.argv[2] || "private/firebase-backup.json";
let document;
try {
  document = JSON.parse(readFileSync(resolve(filepath), "utf8"));
} catch {
  console.error("Yedek okunamadi. Dosya yolunu kontrol et.");
  process.exit(2);
}
const codes = document?.premium_codes;
const activations = document?.code_activations;
const isObject = value => value !== null && typeof value === "object" && !Array.isArray(value);
if (!isObject(codes) || !isObject(activations)) {
  console.error("Beklenen premium_codes veya code_activations koleksiyonu eksik/gecersiz.");
  process.exit(2);
}

const codeEntries = Object.entries(codes);
const activationEntries = Object.entries(activations);
const activationCounts = new Map();
let unknownReferences = 0;
let invalidDeviceIds = 0;
let invalidTimestamps = 0;
let invalidActivations = 0;

for (const [deviceId, row] of activationEntries) {
  if (!/^[A-Za-z0-9_.:-]{8,128}$/.test(deviceId)) invalidDeviceIds++;
  if (!isObject(row) || typeof row.code !== "string") {
    invalidActivations++;
    continue;
  }
  if (!Object.hasOwn(codes, row.code)) unknownReferences++;
  activationCounts.set(row.code, (activationCounts.get(row.code) || 0) + 1);
  if (!Number.isFinite(Number(row.activated_at)) || Number(row.activated_at) <= 0) {
    invalidTimestamps++;
  }
}
const today = new Date().toISOString().slice(0, 10);
let blockers = 0;
let warnings = 0;
console.log("=== Firebase Premium veri tutarlilik raporu (salt okunur) ===");
console.log("Premium kod sayisi:", codeEntries.length);
console.log("Cihaz aktivasyon sayisi:", activationEntries.length);
console.log("Kodlar (gercek kodlar gizlenir):");

for (const [index, [code, row]] of codeEntries.entries()) {
  const problems = [];
  if (!/^[A-Za-z0-9_-]{6,128}$/.test(code) || !isObject(row)) {
    problems.push("GECERSIZ_KOD_VEYA_KAYIT");
    blockers++;
    console.log("Kod #" + (index+1) + ": " + problems.join(", "));
    continue;
  }
  const used = row.used_count ?? 0;
  const max = row.max_uses ?? null;
  const count = activationCounts.get(code) || 0;
  const expiry = row.expiry_date || null;
  if (!Number.isSafeInteger(used) || used < 0) { problems.push("GECERSIZ_USED_COUNT"); blockers++; }
  if (max !== null && (!Number.isSafeInteger(max) || max < 1)) { problems.push("GECERSIZ_MAX_USES"); blockers++; }
  if (max !== null && count > max) { problems.push("AKTIVASYON_KOTAYI_ASIYOR"); blockers++; }
  if (max !== null && Number.isSafeInteger(used) && used > max) { problems.push("KULLANIM_KOTAYI_ASIYOR"); blockers++; }
  if (used !== count) { problems.push("SAYAC_AKTIVASYON_UYUSMUYOR"); warnings++; }
  if (expiry && (!/^\d{4}-\d{2}-\d{2}$/.test(expiry) || !Number.isFinite(Date.parse(expiry + "T00:00:00Z")))) {
    problems.push("GECERSIZ_BITIS_TARIHI");
    blockers++;
  } else if (expiry && expiry < today) {
    problems.push("BITIS_TARIHI_GECMIS");
    warnings++;
  }
  if (row.active === false) { problems.push("PASIF_KOD"); warnings++; }
  if (row.active !== undefined && typeof row.active !== "boolean") { problems.push("GECERSIZ_ACTIVE"); blockers++; }
  console.log("Kod #" + (index + 1) + ": aktif=" + (row.active !== false) +
    ", kullanilan=" + used + ", kayitli_cihaz=" + count +
    ", limit=" + (max === null ? "sinirsiz" : max) +
    ", bitis=" + (expiry ? (expiry < today ? "gecmis" : "var") : "yok") +
    ", durum=" + (problems.length ? problems.join("|") : "OK"));
}
if (unknownReferences) { console.log("BILINMEYEN_KODA_REFERANS:", unknownReferences); blockers += unknownReferences; }
if (invalidDeviceIds) { console.log("GECERSIZ_CIHAZ_KIMLIGI:", invalidDeviceIds); blockers += invalidDeviceIds; }
if (invalidActivations) { console.log("GECERSIZ_AKTIVASYON:", invalidActivations); blockers += invalidActivations; }
if (invalidTimestamps) { console.log("GECERSIZ_AKTIVASYON_TARIHI:", invalidTimestamps); warnings += invalidTimestamps; }
console.log("KRITIK_SORUN:", blockers);
console.log("INCELEME_GEREKTIREN_UYARI:", warnings);
console.log("Bu rapor hicbir dosyayi veya D1/Firebase verisini degistirmedi.");
if (blockers) process.exitCode = 1;
