# Premium Game World + Four Operations Center — QA / release gate

Scope: Android \`main\`, no Play Store deployment or billing changes.

## Experience
- Games tab: scrolling illustrated grid with 11 **real launchable** destinations, three categories, filtering, and a 4-operations spotlight. The extra independent \`ForestAdventureActivity\` is listed as Orman Keşfi.
- 1st and 2nd grades see multiplication/division with an accurate grade lock; these games are not falsely presented as playable. Both old daily-task operation activities have on-entry grade gates even for deep links.
- Home quick-access card \`four_operations\` launches \`OperationsHubActivity\` instead of addition directly.
- Dedicated Number Hunt WebP now depicts an actual numerical treasure chest; no market stall graphic. Main listing and quick access share the same cover.
- Hub offers addition/subtraction/multiplication/division, recent operation, a playable 12-question grade-aware mixed practice session, grade-aware lock text and practice difficulty.
- Legacy four operation Activity routes remain unchanged for existing daily-task tracking, reward persistence and achievements.
- Shared \`GradeAwareOperationPolicy\` applies the grade cap to all generated questions, independently of user-selected difficulty. Students in grades 1–2 cannot receive multiplication/division.
- School-grade caps: G1 addition/subtraction answers up to 20, G2 up to 100. G3+ includes multiplication and division (integer exact division only); harder levels increase number ranges **inside** their permitted grade.
- Existing operation game view and level selector redesigned with themed WebP art, responsive HUD, 2×2 high contrast options and distinct per-operation colors.

## Art
- Original deterministic Pillow render script \`scripts/generate_game_hub_art.py\` generates 7 distinct compact WebP assets into \`drawable-nodpi\`, size 600×620. Rebuild via workflow \`.github/workflows/game-hub-art.yml\`; scripts require no runtime Python in APK.
- \`scripts/check_game_hub_assets.py\` checks all WebP signatures and canonical route references.

## Device acceptance
1. Pull main; build debug APK; confirm app opens and nav Games tab.
2. Confirm 11 tiles including Orman Keşfi, class grade and category filter. Tap each tile; verify it opens the **named** game (no mistaken Number Hunt market).
3. Tap Home → Four Operations. Confirm hub rather than Addition; open add, subtraction, allowed multiply/divide and Mixed.
4. Grade 1 and 2: multiplication/division locked, daily tasks do not newly assign them, addition and subtraction never exceed class ceilings. Grade 3 and 4: all four allowed. Old tasks created **before** migration may still be cached in local preferences until the next daily task regeneration.
5. In each of 4 legacy games select a difficulty; exercise multiple questions, correct/incorrect results, timers and end/retry; ensure game stats and purchased stars persist.
6. Check 320dp phone, accessibility font scale, portrait scrolling, taps/rotations. CI only compiles androidTest APK; physical tap testing remains necessary.

This is an art+navigation+grade baseline, not empirical proof of premium fun or field-tested school curriculum coverage. Design and real-device acceptance are separate.
