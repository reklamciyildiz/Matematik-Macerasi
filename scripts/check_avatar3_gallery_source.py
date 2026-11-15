#!/usr/bin/env python3
"""P0 and P1 Avatar 3.0 source + Lottie pilot regression. Device QA remains separate."""
import json
from pathlib import Path

repo = Path(__file__).resolve().parents[1]
root = repo / "app/src/main/java/com/yigit033/matematikmacarasi"
raw = repo / "app/src/main/res/raw"

def source(path):
    return (root / path).read_text(encoding="utf-8")

store = source("screens/AvatarStoreActivity.kt")
service = source("services/AvatarService.kt")
view = source("components/AnimatedAvatarView.kt")
hero = source("components/AvatarCollectionHero.kt")
custom = source("services/Avatar3Customization.kt")
atelier = source("components/Avatar3DetailDialog.kt")
stage = source("components/Avatar3CharacterStage.kt")
commerce = source("components/Avatar3CommerceDialogs.kt")
palette = source("components/Avatar3LottiePalette.kt")

forest = json.loads((raw / "avatar_explorer_forest_pilot.json").read_text(encoding="utf-8"))
space = json.loads((raw / "avatar_explorer_space_pilot.json").read_text(encoding="utf-8"))

def valid_pilot(asset):
    layers = asset.get("layers", [])
    groups = [
        shape for layer in layers
        for shape in layer.get("shapes", [])
        if shape.get("ty") == "gr"
    ]
    return (
        asset.get("v") and asset.get("fr") == 30 and
        asset.get("op") == 90 and asset.get("w") == 512 and
        asset.get("h") == 512 and not asset.get("assets") and
        len(groups) >= 45 and
        any(layer.get("ks", {}).get("p", {}).get("a") == 1 for layer in layers) and
        any("eye" in group.get("nm", "").lower() for group in groups) and
        any("boot" in group.get("nm", "").lower() for group in groups)
    )

checks = {
    "15 legacy avatar identities preserved":
        all(f"id = {i}," in service for i in range(1, 16)),
    "Free and Premium avatar boundaries remain":
        "avatarId > 6" in service and "avatar.id > 6" in store,
    "legacy owned avatar IDs and selected avatar retained":
        '"current_avatar_id"' in service and '"unlocked_avatar_ids"' in service,
    "premium must be active to equip protected avatar":
        "isPremiumSync()" in service and "if (avatarId > 6" in service,
    "shop cards open interactive atelier":
        "detailAvatar = avatar" in store and "Avatar3DetailDialog(" in store,
    "big Lottie preview; cards render static frames":
        "animate = true" in hero and "animate = false" in store and
        "AvatarStaticLottieArtwork" in view,
    "optional WebP decode stays off main thread":
        "produceState<ImageBitmap?>" in view and
        "withContext(Dispatchers.IO)" in view and "inSampleSize = sample" in view,
    "320dp adaptive two-column layout":
        "GridCells.Adaptive(minSize = 128.dp)" in store,
    "look ownership stored separately from legacy avatar ownership":
        "avatar3_customization_v1" in custom and
        "look_selected_" in custom and "looks_owned_" in custom,
    "no cosmetics star debit; no obsolete buyLook":
        "starCost = 0" in custom and "fun buyLook(" not in custom and
        "customization.buyLook(" not in atelier,
    "avatar store uses ONLY AvatarService for avatar purchases":
        "avatarService.unlockAvatar(target.id)" in store and
        'putInt("display_stars"' not in store,
    "global purchase lock across AvatarService instances":
        "synchronized(avatarPurchaseLock)" in service and
        "alreadyOwnedOnDisk" in service,
    "no stale delayed balance correction":
        "starManager.setDisplayStars(newStars)" not in service[
            service.index("updateUnlockedStatus()", service.index("fun unlockAvatar(")):
            service.index("return true", service.index("fun unlockAvatar("))
        ],
    "store exit does not force garbage collection":
        "System.gc()" not in store,
    "free previews for locked avatars":
        "accessory, true)" in atelier and "effect, true)" in atelier and
        "customization.recordPreview(" in atelier,
    "accessories/effects saved only for owned avatar":
        "avatarOwned(avatarId) && value in 0..2" in custom and
        "customization.setAccessory(" in atelier and
        "customization.setEffect(" in atelier,
    "badges show earned progress and conditions":
        "customization.previewCount(" in atelier and
        "Atölye Kaşifi" in atelier and "Stil Meraklısı" in atelier,
    "palette re-renders original vector colors":
        "Avatar3LottiePalette.load(" in view and
        "LottieCompositionSpec.JsonString(json)" in view and
        'node.optString("ty") == "gf"' in palette,
    "new pilot forest vector Lottie is well formed":
        valid_pilot(forest),
    "new pilot space vector Lottie is well formed":
        valid_pilot(space),
    "forest has forest-specific equipment":
        all(token in json.dumps(forest) for token in
            ["safari brim", "left binocular", "forest leaf medal"]),
    "space has astronaut-specific equipment":
        all(token in json.dumps(space) for token in
            ["helmet base", "jetpack", "spacesuit chest", "helmet chin shield"]),
    "pilot maps 6:1 and 6:2 to distinct raw animated Lotties":
        "avatar.id == 6 && lookId == 1 -> R.raw.avatar_explorer_forest_pilot" in view and
        "avatar.id == 6 && lookId == 2 -> R.raw.avatar_explorer_space_pilot" in view and
        "LottieCompositionSpec.RawRes(rawRes)" in view,
    "Kaşif theme names match tangible costume changes":
        'listOf("Klasik Kaşif", "Orman Kaşifi", "Uzay Kaşifi")' in custom,
    "pilot Premium style still requires entitlement":
        "premiumRequired = index == 2" in custom and
        "look.premiumRequired" in custom and
        "premiumActive()" in custom,
    "previous owned Premium looks not deleted after expiry":
        "getStringSet(ownedKey(avatarId)" in custom and
        "return 0" in custom and "_unlockedAvatarIds.addAll(tempUnlockedIds)" in service,
    "shop keeps responsive dark confirmation dialogs":
        "Avatar3NeedStarsDialog(" in store and
        "Avatar3UnlockConfirmDialog(" in store and
        'text = "Yetersiz Yıldız"' not in store,
    "child-safe star earning route is game based":
        "Oyunlara dön ve yıldız kazan" in commerce and
        "onPlay" in store,
    "Premium plans routed to canonical Settings":
        "SettingsActivity::class.java" in store,
    "Premium code sync preserved":
        "checkAndSyncPremiumStatus()" in store,
}
for name, passed in checks.items():
    print(("PASS " if passed else "FAIL ") + name)
if not all(checks.values()):
    raise SystemExit("Avatar 3.0 P0/P1 regression FAILED")
print(f"PASS: {len(checks)} source/asset checks. Android build and real-device QA remain separate.")
