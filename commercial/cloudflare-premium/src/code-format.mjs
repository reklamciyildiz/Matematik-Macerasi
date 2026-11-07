/**
 * Legacy RTDB keys were not restricted to [A-Z0-9_-]. Accept printable
 * letters/numbers/punctuation/symbols (including internal ASCII spaces)
 * while rejecting controls, invisible formatting, and edge whitespace.
 * Hash the exact code as stored; never case-fold or normalize at the server.
 * New codes are still generated with a high-entropy MM-... hex format.
 */
export const validPremiumCode = value =>
  typeof value === "string" &&
  value.length >= 6 && value.length <= 128 &&
  value.trim() === value &&
  /^[\p{L}\p{N}\p{M}\p{P}\p{S} ]+$/u.test(value);
