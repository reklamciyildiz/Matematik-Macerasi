-- Private D1 store: only this Worker can query it. Never bind to client code.
CREATE TABLE IF NOT EXISTS premium_codes (
  code_hash TEXT PRIMARY KEY CHECK(length(code_hash)=64),
  institution TEXT NOT NULL DEFAULT '',
  max_uses INTEGER CHECK(max_uses IS NULL OR max_uses >= 1),
  used_count INTEGER NOT NULL DEFAULT 0 CHECK(used_count >= 0),
  expires_on TEXT,
  status TEXT NOT NULL DEFAULT 'active' CHECK(status IN ('active','revoked')),
  created_at INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS code_activations (
  device_hash TEXT PRIMARY KEY CHECK(length(device_hash)=64),
  activation_id TEXT NOT NULL UNIQUE,
  code_hash TEXT NOT NULL REFERENCES premium_codes(code_hash),
  status TEXT NOT NULL DEFAULT 'active' CHECK(status IN ('active','revoked')),
  activated_at INTEGER NOT NULL,
  last_verified_at INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_activations_by_code ON code_activations(code_hash);
