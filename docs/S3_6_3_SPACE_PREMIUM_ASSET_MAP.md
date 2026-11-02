# S3.6.3 — Premium Space Art / Asset Map

Version: 2026-11-02 • Branch: main • Scope: SpaceMathAdventureActivity

The art is produced by a **deterministic original illustration generator**, committed as optimized WebP and rendered offline. Design compositions for five worlds were established from game concept previews, but promotional mockups are **not** baked into game screens. There are no baked labels, fake in-app currency counters, or UI panels in the scene images.

## Android resources

All paths are in \`app/src/main/res/drawable-nodpi/\` (unscaled by Android density).

| Resource | Dimensions | Use |
|---|---|---|
| space_bg_terra_premium.webp | 640×1050 RGB | Emerald floating islands, waterfalls, green nebula |
| space_bg_aqua_premium.webp | 640×1050 RGB | Glacial ocean, ice shelf, cyan atmosphere |
| space_bg_pyra_premium.webp | 640×1050 RGB | Lava mountains, magma glow, amber red sky |
| space_bg_luna_premium.webp | 640×1050 RGB | Lunar outpost, craters, moon-blue light |
| space_bg_nova_premium.webp | 640×1050 RGB | Violet crystal archipelago, cosmic clouds |
| space_ship_premium.webp | 320×320 RGBA | Canonical playable spacecraft, transparent hull |
| space_meteor_1_premium.webp | 320×320 RGBA | Blue meteor |
| space_meteor_2_premium.webp | 320×320 RGBA | Orange meteor |
| space_meteor_3_premium.webp | 320×320 RGBA | Violet meteor |
| space_meteor_4_premium.webp | 320×320 RGBA | Teal meteor |
| space_laser_premium.webp | 90×300 RGBA | Energy beam projectile |
| space_impact_premium.webp | 320×320 RGBA | Impact glow/sparks, 440ms composited fade |

## Render pipeline and code mapping

- Generation script: \`scripts/generate_space_premium_art.py\`, Python/Pillow 11.1.0, fixed seed. Only needed for **asset production**, not app runtime.
- Render + commit workflow: \`.github/workflows/space-premium-assets.yml\`.
- CI preflight: \`scripts/check_space_premium_assets.py\` verifies WebP container signatures, all 12 assets, references and size ceilings without Pillow.
- Resource switch: \`SpacePremiumArt.background(planet)\`.
- In-game stage: \`SpaceCombatArena.kt\`. One background \`Image\` per planet; one cached ship sprite, four cached meteor variants, laser and burst sprite. Canvas still draws native parallax, engine flicker and hit indicator. Sprites never alter combat collision positions.
- Map previews: \`SpaceExpeditionScreen.kt\` (\`GalaxyScene\` and five planet cards).
- Existing grade-aware question engine, 15-question flow, stars, purchase records and checkpoint keys remain intact.

## Acceptance gates

- CI must pass \`:app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest\`. UI instrumentation **compiles**, it does not execute on hosted GitHub Actions.
- Physical Android device must confirm: 5 visual themes, controls remain visible at 320–360dp, ship follows drag, target numerals remain readable at smallest DPI, lasers move and actually collide with meteors, green/orange impact ring and existing audio are synchronized, pause/resume and background mode work, 15 hits and class difficulty remain correct.
- Watch for memory/frame drops on weaker phones; optimized resources are 640×1050 and 320×320 rather than full-size promotional composites.
- WebP art is procedurally illustrated at build authoring time. Treat design as a polished **game art iteration**, not guaranteed production-grade artist illustration; device acceptance decides.

