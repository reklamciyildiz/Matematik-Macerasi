#!/usr/bin/env python3
"""Safety gate: proposal must be private, inert, and visibly NOT deployable yet."""
import json
from pathlib import Path

root = Path(__file__).resolve().parents[1]
cfg = root / "commercial" / "firebase"
rules = json.loads((cfg / "realtime-database.lockdown.rules.json").read_text(encoding="utf-8"))["rules"]
status = json.loads((cfg / "rollout-status.json").read_text(encoding="utf-8"))
assert status["stage"] == "DRAFT_NOT_DEPLOYED"
assert status["approvedForProduction"] is False
assert status["authRequiredForChildren"] is False
assert rules[".read"] is False
assert rules[".write"] is False
for path in ("premium_codes", "code_activations"):
    assert rules[path][".read"] is False, path
    assert rules[path][".write"] is False, path
assert rules["app_info"] == {".read": True, ".write": False}
# Preserve current client contracts. These direct RTDB calls mean a
# sudden rules lockdown WOULD BREAK legacy code activation/restore.
client = (root/"app/src/main/java/com/yigit033/matematikmacarasi/services/FirebaseManager.kt").read_text(encoding="utf-8")
for expected in ('child("premium_codes")','child("code_activations")',
                 "setValue(newCount)", "activationRef.setValue(activation)"):
    assert expected in client, expected
# Never auto-apply draft rules from repository root.
assert not (root / "firebase.json").exists(), "A root firebase.json requires a manual deployment audit"
print("RTDB staged rule policy PASS; NOT DEPLOYED, release gate remains CLOSED.")
