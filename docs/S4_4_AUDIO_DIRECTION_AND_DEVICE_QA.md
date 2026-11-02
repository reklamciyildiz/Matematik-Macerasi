# S4.4 — Matematik Macerası 2.0 / Audio Direction + Android QA

**Scope:** Original, playable offline music + sound pack; no purchases, user data, backend or Play Store deployment changed.

## Audio direction
- **Identity:** cheerful floating-island adventure; bright plucked melody, gentle layered harmony, light bass and toy-like percussion. Original procedural composition, not an existing song or a stock/sample-pack recording.
- **Theme:** `mm2_adventure_theme.wav`, 29-second composed loop, 22,050 Hz / PCM16 mono. Android ExoPlayer loops the track; level effects briefly duck its output.
- **Core feedback:** `mm2_correct` (short upward chime), `mm2_wrong` (gentle non-punitive descending response), `mm2_success` (full victory), `mm2_failure` (soft loss), `mm2_level` (short stage finish), `mm2_quest` (daily quest).
- **Micro interactions:** `mm2_coin`, `mm2_combo`, `mm2_ui`, `mm2_pop`, `mm2_whoosh`, `mm2_laser`, `mm2_impact`. 14 original offline raw assets in total.
- **Mix:** Effects via one reusable SoundPool, maximum six simultaneous streams; music via one focus-aware ExoPlayer. Independent saved 0–100% music/effect mix levels. Completion/failure briefly lowers music level; effects respect the Sound toggle.
- **Safety:** No unpleasant loud alarm or punitive buzzer; no spoken instructions or copyrighted melodies; sound is supplemental to visible feedback. No external network required for audio.
- **Fallback:** If a sample has not finished loading or fails, no hard crash. All old raw MP3s remain in the repository until all legacy direct-use routes are checked and removed deliberately.

## Screen integration
- Dört İşlem individual correct/wrong via existing `AudioService` callbacks, level-complete screen plays a distinct stinger.
- Karışık Macera gives feedback for each selection and a victory cue at run completion.
- Balon Patlatma and Uzay Matematik correct/wrong use shared AudioService, victory at fresh completion.
- Matematik Labirenti already uses the shared service, and inherits new victory/feedback cues.
- Sayı Avı 2.0 and Gizemli Orman now use the shared service instead of independently allocated sound players.
- DailyTaskManager triggers quest cue when a task becomes completed, not for intermediate progress.
- Legacy Sayı Avı uses shared correct/wrong/victory/failure, including timed loss.
- Settings → Ses ve Müzik has independent volumes, toggles and sound previews.

## Test gates
- `scripts/check_premium_audio.py` validates all PCM WAV files (format, duration, amplitude) and source wiring.
- `.github/workflows/android-debug-ci.yml` runs asset checks plus Android unit tests and debug APK compile.
- CI **does not listen**, measure speaker output, execute instrumented tests on physical Android, or certify musical/artistic quality.

## Physical Android acceptance (release-blocking)
1. From a clean foreground start, toggle music off/on. Confirm playback resumes only when enabled; volume sliders retain values after app restart.
2. With music at 25% and effects at 75%, run correct and wrong answers quickly; no delayed first sound, crackling, clipping or simultaneous overpowering effects.
3. Test independent effect mute while music remains audible, then inverse. Check zero volumes and Android hardware volume controls.
4. Check home ↔ game ↔ settings navigation repeatedly; no stacked music or restart on routine resume, and no audio remains playing after the app enters background.
5. Test each of the four operations, mixed 12-step completion, Maze, Balloon, Space, Number Hunt 2.0, Forest, daily-task completion and legacy Number Hunt timed failure.
6. Test interruption by a phone call, voice navigation, Bluetooth disconnect and another music app; check Android audio focus and that playback respects users' choices.
7. Test 2–3 low-end Android devices (speaker + wired/Bluetooth headphones); evaluate fatigue after 15 minutes. Confirm feedback conveys right/wrong even **without** sound.
8. Verify the 29s theme loop transition is not noticeably clicky; if audible, remix and replace rather than masking with volume.
9. Do not call the pack 'studio-mastered' or claim measured user satisfaction without listening sessions. Procedural source is original, but final premium quality requires physical listening and iteration.

**Release status:** Code change and CI are technical prerequisites, not Play Store approval.
