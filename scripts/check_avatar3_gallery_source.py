#!/usr/bin/env python3
"""Avatar 3.0 static regression checks; real UI / Play QA remains required."""
from pathlib import Path

root = Path(__file__).resolve().parents[1] / "app/src/main/java/com/yigit033/matematikmacarasi"
store = (root / "screens/AvatarStoreActivity.kt").read_text(encoding="utf-8")
service = (root / "services/AvatarService.kt").read_text(encoding="utf-8")
view = (root / "components/AnimatedAvatarView.kt").read_text(encoding="utf-8")
hero = (root / "components/AvatarCollectionHero.kt").read_text(encoding="utf-8")

checks = {
    "all 15 stable avatar IDs": all(f"id = {i}," in service for i in range(1, 16)),
    "six free and nine Premium policy": "avatarId > 6" in service and
        "avatar.id > 6" in store,
    "legacy star ownership preserved": 'unlocked_avatar_ids' in service and
        '"current_avatar_id"' in service,
    "no premium avatar equip without entitlement": "if (avatarId > 6" in service and
        "isPremiumSync()" in service,
    "single collection hero": "AvatarCollectionHero(" in store and
        "animate = true" in hero,
    "static thumbnails": "animate = false" in store and
        "hasAnimation -> AnimatedAvatarArtwork" in view,
    "optional WebP pilot with legacy fallback":
        "context.assets.open(filename)" in view and
        "pilotArtwork != null" in view and
        "else -> AvatarStaticArtwork(avatar, contentDescription)" in view,
    "deterministic avatar palette": "val index = (avatar.id - 1)" in view and
        ".random()" not in view,
    "responsive collection": "GridCells.Adaptive(minSize = 158.dp)" in store,
    "single avatar click handler": "val onAvatarTapped: (Avatar) -> Unit" in store,
    "current Premium manager is not shadowed": "var purchaseManager:" not in
        store[store.index("fun AvatarStoreScreen("):],
    "no repeated half-second polling": "delay(500)" not in store,
    "single source of star purchase": "val actualBalance = starManager.getDisplayStars()" in store
        and 'putInt("display_stars"' not in store[
            store.index("// Satın al butonu"):store.index("@Composable\nfun AvatarItem(")],
    "routes to canonical three-plan paywall": "SettingsActivity::class.java" in store
        and "purchaseManager.buyPremium(context as ComponentActivity)" not in store,
    "legacy Premium code sync kept": "checkAndSyncPremiumStatus()" in store,
    "premium expiry keeps previously unlocked IDs": "_unlockedAvatarIds.addAll(tempUnlockedIds)" in service,
}
for label, valid in checks.items():
    print(("PASS " if valid else "FAIL ") + label)
if not all(checks.values()):
    raise SystemExit("Avatar 3.0 static guard failed")
print("PASS: Avatar collection, wallet, paywall and continuity source checks")
print("NOTE: compiled UI and Play Billing flows must also be tested on device")
