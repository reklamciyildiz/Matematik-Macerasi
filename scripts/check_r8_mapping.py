#!/usr/bin/env python3
"""Validate an R8 mapping from an actual minified build (NOT a Play score)."""
import re
import sys
from pathlib import Path

if len(sys.argv) != 2:
    raise SystemExit("Usage: check_r8_mapping.py /path/to/mapping.txt")
path = Path(sys.argv[1])
if not path.is_file() or path.stat().st_size < 256:
    raise SystemExit("R8_FAIL: No nonempty mapping.txt; optimized build not demonstrated")

mapping = path.read_text(encoding="utf-8", errors="replace")
classes = [
    (m.group(1), m.group(2))
    for line in mapping.splitlines()
    if (m := re.match(r"^([^ #]+) -> ([^ :]+):$", line))
]
renamed = [(source, target) for source, target in classes if source != target]
# Confirm at least some application classes were kept and actually obfuscated.
app_prefix = "com.yigit033.matematikmacarasi."
app_renamed = [pair for pair in renamed if pair[0].startswith(app_prefix)]
print(f"R8 class mappings: {len(classes)}, renamed: {len(renamed)}, "
      f"app classes renamed: {len(app_renamed)}, mapping bytes: {path.stat().st_size}")
if not app_renamed:
    raise SystemExit("R8_FAIL: No renamed application class; check keep rules and R8 build")
print("R8_MAPPING_READY: real code obfuscation present")
print("NOTE: mapping alone cannot determine Play's DEX shrink/obfuscation percentages")
