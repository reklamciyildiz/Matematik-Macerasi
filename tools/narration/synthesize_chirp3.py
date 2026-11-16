#!/usr/bin/env python3
"""
Generate *offline-app* MP3 recordings from authored lesson manifest, with
Google Cloud Chirp 3 HD. Never call this from the Android app.

Requires: pip install google-cloud-texttospeech
           GOOGLE_APPLICATION_CREDENTIALS=/absolute/path/to/credential.json

The repo contains NO keys, NO children's responses, and NO generated HD
recordings by default. Only run after verifying provider and redistribution
terms plus the chosen tr-TR voice by ear.
"""
from __future__ import annotations

import argparse
import base64
import hashlib
import os
from pathlib import Path
import re
import sys
import time


def read_manifest(path: Path) -> list[tuple[str, str]]:
    if not path.is_file():
        raise FileNotFoundError(path)
    result: list[tuple[str, str]] = []
    seen: set[str] = set()
    for line_no, row in enumerate(path.read_text("utf-8").splitlines(), start=1):
        try:
            digest, payload = row.split("\t")
            if not re.fullmatch(r"[a-f0-9]{64}", digest):
                raise ValueError("invalid sha256")
            text = base64.b64decode(payload, validate=True).decode("utf-8")
            if not text.strip() or len(text.encode("utf-8")) > 4500:
                raise ValueError("invalid text length")
            if digest in seen:
                raise ValueError("duplicate hash")
            seen.add(digest)
            result.append((digest, text))
        except (ValueError, UnicodeError) as exc:
            raise ValueError(f"Bad voice manifest line {line_no}: {exc}") from exc
    if not result:
        raise ValueError("Narration manifest empty")
    if len(result) > 3000:
        raise ValueError("Unexpectedly large voice export")
    return result


def cli() -> argparse.Namespace:
    p = argparse.ArgumentParser()
    p.add_argument("--manifest", type=Path, required=True)
    p.add_argument("--output", type=Path,
                   default=Path("app/src/main/assets/lesson_narration/tr-TR"))
    p.add_argument("--voice", default="tr-TR-Chirp3-HD-Kore")
    p.add_argument("--limit", type=int, default=0,
                   help="Generate a small audition set before the full pack.")
    p.add_argument("--dry-run", action="store_true",
                   help="Validate and count narration without credentials.")
    return p.parse_args()


def main() -> int:
    args = cli()
    items = read_manifest(args.manifest)
    if args.limit < 0:
        raise ValueError("--limit must not be negative")
    if args.limit:
        items = items[:args.limit]
    chars = sum(len(text) for _, text in items)
    print(f"Validated {len(items)} distinct authored clips; {chars} Unicode characters")
    print(f"Voice: {args.voice}; output path: {args.output}")
    if args.dry_run:
        return 0
    if not re.fullmatch(r"tr-TR-Chirp3-HD-[A-Za-z]+", args.voice):
        raise ValueError("Use a supported Turkish Chirp 3 HD voice ID")
    if not os.environ.get("GOOGLE_APPLICATION_CREDENTIALS"):
        raise RuntimeError("Build-time Google Cloud TTS credential is required. "
                           "Never place the credential in the Android application.")
    # Defer importing the paid provider SDK until a real synthesis is requested.
    from google.cloud import texttospeech

    client = texttospeech.TextToSpeechClient()
    voice = texttospeech.VoiceSelectionParams(
        language_code="tr-TR", name=args.voice
    )
    audio = texttospeech.AudioConfig(
        audio_encoding=texttospeech.AudioEncoding.MP3
    )
    args.output.mkdir(parents=True, exist_ok=True)
    generated = 0
    reused = 0
    for index, (digest, text) in enumerate(items, start=1):
        destination = args.output / f"{digest}.mp3"
        if destination.exists() and destination.stat().st_size >= 800:
            reused += 1
            continue
        # No user text: the manifest only has original product teaching copy.
        error: Exception | None = None
        for attempt in range(4):
            try:
                response = client.synthesize_speech(
                    input=texttospeech.SynthesisInput(text=text),
                    voice=voice, audio_config=audio
                )
                content = response.audio_content
                if len(content) < 800:
                    raise ValueError("Returned audio is unexpectedly small")
                tmp = destination.with_suffix(".mp3.part")
                tmp.write_bytes(content)
                tmp.replace(destination)
                generated += 1
                error = None
                break
            except Exception as exc:
                error = exc
                if attempt < 3:
                    time.sleep(min(12, 2 ** attempt))
        if error is not None:
            raise RuntimeError(f"Voice synthesis failed at item {index}: {digest}") from error
        if index % 25 == 0:
            print(f"Processed {index}/{len(items)} clips")
    print(f"Pack ready. New={generated}, cached={reused}, total={len(items)}")
    print("Listen to representative clips before including assets in a release.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
