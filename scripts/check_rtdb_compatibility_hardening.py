#!/usr/bin/env python3
"""Static guards for interim RTDB rules; not a Firebase emulator or live test."""
import json
from pathlib import Path

root=Path(__file__).resolve().parents[1]
directory=root/"commercial/firebase"
rules=json.loads((directory/"realtime-database.compatibility-hardening.rules.json").read_text(encoding="utf-8"))["rules"]
legacy=(root/"app/src/main/java/com/yigit033/matematikmacarasi/services/FirebaseManager.kt").read_text(encoding="utf-8")
for x in (".read",".write"):
    assert rules[x] is False
assert rules["app_info"]=={".read":True,".write":False}
assert rules["premium_codes"][".read"] is False
assert rules["code_activations"][".read"] is False
assert rules["premium_codes"]["$code"][".read"] is True
assert rules["code_activations"]["$deviceId"][".read"] is True
count=rules["premium_codes"]["$code"]["used_count"][".write"]
for expected in ("newData.val() === 1","newData.val() === data.val() + 1",
                 "newData.val() <= data.parent().child('max_uses').val()",
                 "child('active')", "newData.isNumber()"):
    assert expected in count,expected
activation=rules["code_activations"]["$deviceId"][".write"]
for expected in ("!data.exists()","newData.exists()","newData.hasChildren",
                 "root.child('premium_codes')","child('active')"):
    assert expected in activation,expected
assert 'child("premium_codes").child(code)' in legacy
assert 'child("code_activations").child(deviceId)' in legacy
assert "setValue(newCount)" in legacy
assert "activationRef.setValue(activation)" in legacy
print("Compatibility RTDB rules JSON and legacy-path static checks PASS; NOT deployed.")
