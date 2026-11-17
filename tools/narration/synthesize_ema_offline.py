#!/usr/bin/env python3
"""
Create deterministic, high-quality, OFFLINE Turkish narration assets.

No cloud API. No payment account. No service account credentials.
Open-source EMA Lightning: Apache-2.0 licensed model/code.
Uses existing gradle-produced manifest.tsv containing ONLY authored lessons.

Requirements on the developer's own computer:
  Python 3.11-3.13
  python -m pip install ema-lightning
  ffmpeg executable on PATH (free open-source tool)
  Run model download ONCE while online, then synthesize locally on CPU/GPU.

Review all third-party license notices before distributing assets.
Only include reviewed MP3 recordings; never include model weights in APK.
"""
from __future__ import annotations

import argparse
import base64
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile

ALLOWED_NAME = re.compile(r"^[0-9a-f]{64}$")


def manifest_rows(path: Path) -> list[tuple[str, str]]:
    if not path.is_file():
        raise FileNotFoundError(f"Cannot find voice manifest: {path}")
    rows = []
    keys = set()
    for number, line in enumerate(path.read_text("utf-8").splitlines(), 1):
        try:
            digest, encoded = line.split("\t")
            if not ALLOWED_NAME.fullmatch(digest) or digest in keys:
                raise ValueError("Invalid or duplicate digest")
            spoken = base64.b64decode(encoded, validate=True).decode("utf-8")
            if not spoken or len(spoken) > 2000 or "\x00" in spoken:
                raise ValueError("Invalid or oversized speech")
            keys.add(digest)
            rows.append((digest, spoken))
        except (ValueError, UnicodeError) as error:
            raise ValueError(f"Invalid manifest row {number}: {error}") from error
    if not rows or len(rows) > 3000:
        raise ValueError("Unexpected manifest size")
    return rows


def cli() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Free local Turkish lesson voicepack maker")
    parser.add_argument("--manifest", type=Path,
                        default=Path("app/build/narration/manifest.tsv"))
    parser.add_argument("--output", type=Path,
                        default=Path("app/src/main/assets/lesson_narration/tr-TR"))
    parser.add_argument("--limit", type=int, default=8,
                        help="Start with 8 audition clips. Use 0 for all clips.")
    parser.add_argument("--dry-run", action="store_true",
                        help="Validate manifest only; no model download or audio creation.")
    parser.add_argument("--seed", type=int, default=73)
    parser.add_argument("--speed", type=float, default=0.97)
    return parser.parse_args()


def main() -> int:
    args = cli()
    if args.limit < 0 or args.seed < 0 or not 0.25 <= args.speed <= 4.0:
        raise ValueError("Invalid nonnegative limit/seed or EMA speech speed")
    all_rows = manifest_rows(args.manifest)
    rows = all_rows[:args.limit] if args.limit else all_rows
    print(f"Valid original lesson texts: {len(all_rows)}; selected: {len(rows)}")
    print("Engine: EMA Lightning offline (Apache-2.0), no remote synthesis API")
    print(f"Output: {args.output}")
    if args.dry_run:
        return 0
    if shutil.which("ffmpeg") is None:
        raise RuntimeError("Install free FFmpeg and add it to PATH.")
    try:
        from ema_lightning import EMA
    except ImportError as exc:
        raise RuntimeError("Install free engine: python -m pip install ema-lightning") from exc
    # First use fetches open model weights. This is a one-time free download,
    # not a remote inference API. Subsequent synthesis runs locally.
    generator = EMA()
    args.output.mkdir(parents=True, exist_ok=True)
    produced = skipped = 0
    for i, (digest, speech) in enumerate(rows, 1):
        final = args.output / f"{digest}.mp3"
        if final.exists() and final.stat().st_size > 1500:
            skipped += 1
            continue
        with tempfile.TemporaryDirectory() as temporary:
            wav = Path(temporary) / "lesson.wav"
            encoded = Path(temporary) / "lesson.mp3"
            generator.say(speech, path=str(wav), speed=args.speed, seed=args.seed)
            command = [
                "ffmpeg", "-hide_banner", "-loglevel", "error", "-y",
                "-i", str(wav), "-ac", "1", "-ar", "24000",
                "-codec:a", "libmp3lame", "-q:a", "3", str(encoded)
            ]
            subprocess.run(command, check=True)
            if not encoded.exists() or encoded.stat().st_size < 1500:
                raise RuntimeError(f"Invalid audio for sample {i}: {digest}")
            encoded.replace(final)
            produced += 1
        if i % 20 == 0:
            print(f"Completed {i}/{len(rows)}")
    print(f"Generated {produced}; reused {skipped}. Listen before committing files.")
    print("No paid cloud calls occurred. Keep the APK voicepack size under review.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
