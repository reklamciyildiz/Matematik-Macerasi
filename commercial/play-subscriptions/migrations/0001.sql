-- Separate subscription database; never share or mutate code-license D1.
-- Purchase tokens, parent information and codes must NOT be stored as plaintext.
CREATE TABLE IF NOT EXISTS subscription_receipts (
  token_hash TEXT PRIMARY KEY CHECK(length(token_hash)=64),
  status TEXT NOT NULL CHECK(status IN ('entitled','not_entitled')),
  base_plan TEXT,
  expiry_unix INTEGER NOT NULL DEFAULT 0,
  updated_at INTEGER NOT NULL
);
CREATE TABLE IF NOT EXISTS subscription_installs (
  token_hash TEXT NOT NULL REFERENCES subscription_receipts(token_hash),
  device_hash TEXT NOT NULL CHECK(length(device_hash)=64),
  verified_at INTEGER NOT NULL,
  PRIMARY KEY(token_hash,device_hash)
);
CREATE INDEX IF NOT EXISTS idx_receipt_expiry ON subscription_receipts(expiry_unix);
