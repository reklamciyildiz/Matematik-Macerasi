#!/usr/bin/env python3
"""Avatar 3.0 source regression guard. Android compile and device QA are separate."""
from pathlib import Path

root = Path(__file__).resolve().parents[1] / "app/src/main/java/com/yigit033/matematikmacarasi"
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

checks = {
    "15 existing avatar identities retained":
        all(f"id = {i}," in service for i in range(1, 16)),
    "6 Free and 9 Premium policy remains":
        "avatarId > 6" in service and "avatar.id > 6" in store,
    "legacy avatar selections and purchases retained":
        '"current_avatar_id"' in service and '"unlocked_avatar_ids"' in service,
    "premium avatar cannot equip without entitlement":
        "isPremiumSync()" in service and "if (avatarId > 6" in service,
    "character gallery opens atelier":
        "detailAvatar = avatar" in store and "Avatar3DetailDialog(" in store,
    "single animated hero, static gallery":
        "animate = true" in hero and "animate = false" in store and
        "AvatarStaticLottieArtwork" in view,
    "Lottie previews and gallery images share animation source":
        "LottieCompositionSpec.RawRes(avatar.animationResId)" in view and
        "AvatarStaticLottieArtwork(avatar, contentDescription)" in view,
    "optional WebP fallback preserved":
        "context.assets.open(filename)" in view and
        "val artwork = pilotArtwork" in view and
        "else -> AvatarStaticArtwork(avatar, contentDescription)" in view,
    "WebP decoding stays off UI thread":
        "produceState<ImageBitmap?>" in view and
        "withContext(Dispatchers.IO)" in view and "inSampleSize = sample" in view,
    "two column on narrow phones":
        "GridCells.Adaptive(minSize = 128.dp)" in store,
    "one avatar click handler":
        "val onAvatarTapped: (Avatar) -> Unit" in store,
    "look inventory separate from legacy avatar ID":
        "avatar3_customization_v1" in custom and
        "look_selected_" in custom and "looks_owned_" in custom,
    "look transactions serialized and stars debited once":
        "synchronized(purchaseLock)" in custom and
        "wallet.setDisplayStars(before - look.starCost)" in custom,
    "premium look access is enforced at purchase and selection":
        "Avatar3LookPurchaseResult.PREMIUM_REQUIRED" in custom and
        "look.premiumRequired" in custom and "premiumActive()" in custom,
    "premium expiry keeps purchased inventory":
        "_unlockedAvatarIds.addAll(tempUnlockedIds)" in service and
        "return 0" in custom and "selectedKey(avatarId)" in custom,
    "no parallel avatar wallet edits in UI":
        "avatarService.unlockAvatar(target.id)" in store and
        'putInt("display_stars"' not in store,
    "accessory/effect state saved":
        "fun setAccessory(" in custom and "fun setEffect(" in custom and
        "customization.setAccessory(" in atelier and
        "customization.setEffect(" in atelier,
    "badges based on progress, not simulated":
        "customization.unlockedLooks(" in atelier and
        "customization.lookOwned(" in atelier,
    "active customization visible in hero and cards":
        "lookId = customization.selectedLook(avatar.id)" in store and
        "Avatar3CharacterStage(" in hero and "Avatar3CharacterStage(" in store,
    "old white dialogs replaced":
        'text = "Yetersiz Yıldız"' not in store and
        "Avatar3NeedStarsDialog(" in store and
        "Avatar3UnlockConfirmDialog(" in store,
    "no fake star reward promise":
        "Oyunlara dön ve yıldız kazan" in commerce and
        "onPlay" in store,
    "premium routes to canonical settings":
        "SettingsActivity::class.java" in store and
        "purchaseManager.buyPremium(context as ComponentActivity)" not in store,
    "legacy premium code synchronization remains":
        "checkAndSyncPremiumStatus()" in store,
}
for name, passed in checks.items():
    print(("PASS " if passed else "FAIL ") + name)
if not all(checks.values()):
    raise SystemExit("Avatar 3.0 regression failed")
print("PASS: source-level avatar workshop checks; Android and Play QA still required")
