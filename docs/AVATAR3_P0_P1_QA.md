# Matematik Macerası — Avatar 3.0 P0 + P1 acceptance

## What this release actually includes

- All **15 original avatar IDs, star prices, previously purchased ownerships** remain unchanged.
- P0: cross-instance purchase serialization; durable ownership check inside lock;
  stale delayed balance restore removed; Activity no longer forces GC on UI thread.
- P1: **ID 6 Kaşif Öğrenci** retains its original classic Lottie animation.
  Forest (look 1) and Space (look 2) use **separate original vector Lottie
  resources**, not just a tint or a circle swap.
- Forest vector uses green field clothing, safari hat, binoculars and backpack;
  Space uses a light astronaut suit, helmet, jetpack and chest emblem. Both
  have gentle idle movement, eye blinks and a hand greeting.
- **Three high-detail transparent WebP character sprites are shipped as app
  assets**, for Classic, Forest and Space: `app/src/main/assets/avatars3/explorer_06_*.webp`.
  The gallery, home, profile and workshop default to these premium stills
  with subtle Compose bobbing; the workshop offers an explicit **"Lottie
  animasyonunu izle"** switch for the separate playable vector animations.
  The stills are not true 3D rigged animations; do not claim otherwise.
- Forest look is included with an owned Kaşif; Space look requires active
  Premium. **Neither recoloring nor this pilot spends any additional stars.**

## Nonnegotiable safety

1. Do not reassign IDs 1–15 or change their historical star prices.
2. Do not delete an owned avatar, even after Premium expires; disable
   selection of Premium-only content while entitlement is absent.
3. A free user cannot equip a Premium avatar/look; previews remain available.
4. Do not debit stars for skins that are only color changes.
5. Child-directed rewarded ads stay disabled and cannot mint fake credits.
6. The UI must not update stars using a stale delay after any purchase.

## Automatic checks

- `python scripts/check_avatar3_gallery_source.py`
- `./gradlew :app:testDebugUnitTest :app:assembleDebug`
- GitHub Actions: **Android debug quality gate**, **Android R8 optimized QA build**.

The source checker validates each pilot JSON animation, its vector shape set
and timeline, the production WebP signatures and sizes, mappings to look IDs,
off-main image loading, and the workshop Lottie switch. Static tests and
successful Android builds do not prove visual quality, accessibility,
frame pacing, or an actual Google Play purchase.

## Manual 320dp, 360dp and 600dp/device QA

| Case | Action | Expected |
| --- | --- | --- |
| Classic Kaşif | Preview avatar ID 6 look 0 | Detailed classic artwork, legacy ID/name/price, original Lottie on toggle |
| Forest Kaşif | Tap look 1 | Distinct high-detail green forest costume and own Lottie animation on toggle |
| Space Kaşif | Tap look 2 | Distinct high-detail astronaut costume and own Lottie animation on toggle |
| Motion | Switch to Lottie in each pilot | Idle bob, blink and greeting hand in the new forest/space Lotties; no freeze |
| Artwork performance | Reopen and scroll all characters | Production WebPs load off-main, no memory spike or unbounded decode |
| Gallery consistency | See character ID 6 across shop, home, profile | Same selected costume identity everywhere |
| Preview honesty | Stay in detailed image mode | Gentle illustration bob only; no claim that still portrait is a rigged Lottie |
| Locked preview | Avatar ID 6 not owned; tap 1/2 | Preview works, no accidental equip or stars spent |
| Avatar unlock | Have 20 stars; buy avatar ID 6 | Confirm once; one 20-star debit; ownership persists |
| Double tap | Repeatedly confirm buy on slow device | At most one debit; second request sees persisted ownership |
| Appearance save | Own avatar 6; select forest | Selected in shop, profile and home after restart |
| Premium false | Try Space | Preview works; cannot save/equip Space |
| Premium true | Try Space | Save and render Space; no extra star cost |
| Premium expires | Space selected, entitlement disappears | Keep ownership history; temporarily show Classic |
| Wallet race | Buy avatar, immediately earn stars from game | Earned stars must not revert to old purchase-time value |
| Scroll | Repeatedly browse all 15 cards | No jank, crash, or forced UI-thread GC |
| Screen size | 320dp/360dp and 600dp | No clipped controls; filter tabs and modal are reachable |
| Badge progression | Tap two looks and view Rozetler | Progress updates and explains unlocked conditions |

## Release blocker

Do not upload a production AAB until the **latest** commit builds cleanly,
the pilot passes Android device QA, entitlement is tested both ways, and the
star purchase is verified with real stored balances (not mocked values).
