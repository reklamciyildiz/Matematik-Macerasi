#!/usr/bin/env python3
"""Fail Android CI on malformed Market Adventure vectors or disconnected routes."""
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SRC = ROOT / "app/src/main"
DRAW = SRC / "res/drawable"
SCREEN = SRC / "java/com/yigit033/matematikmacarasi/games/interactive/MarketAdventureScreen.kt"
ACTIVITY = SRC / "java/com/yigit033/matematikmacarasi/games/interactive/MarketSimulatorActivity.kt"
QUICK = SRC / "java/com/yigit033/matematikmacarasi/ui/screens/home/QuickAccessGamesSection.kt"
NAMES = ["hero","manav","firin","toys","stationery","clothing","stall","sports","pets","grocery","cafe"]
ANDROID = "{http://schemas.android.com/apk/res/android}"

def main():
    errors=[]
    screen=SCREEN.read_text(encoding="utf-8")
    activity=ACTIVITY.read_text(encoding="utf-8")
    quick=QUICK.read_text(encoding="utf-8")
    for name in NAMES:
        slug="market_adventure_"+name
        path=DRAW/(slug+".xml")
        if not path.exists():
            errors.append(slug+": missing")
            continue
        try: tree=ET.parse(path).getroot()
        except ET.ParseError as e:
            errors.append(slug+": invalid XML "+str(e))
            continue
        if tree.tag!="vector" or tree.get(ANDROID+"viewportWidth") is None:
            errors.append(slug+": invalid VectorDrawable")
        paths=tree.findall("path")
        if len(paths)<80:errors.append(slug+": expected rich illustration")
        for p in paths:
            d=p.get(ANDROID+"pathData")
            color=p.get(ANDROID+"fillColor")
            if not d or not color or not re.fullmatch(r"#[0-9a-fA-F]{8}|#[0-9a-fA-F]{6}",color):
                errors.append(slug+": invalid path/color")
                break
        if "R.drawable."+slug not in screen:
            errors.append(slug+": screen missing drawable reference")
    if "R.drawable.market_adventure_hero" not in quick:
        errors.append("quick card still displays wrong game art")
    if "MarketAdventureScreen(" not in activity:
        errors.append("existing market Activity not routed to new game")
    if "showInterstitialAdAfterGame" in activity or "Handler(" in activity:
        errors.append("legacy intrusive timers or interstitial code remain active")
    # Former MarketSimulator's Float/Double cart and dialog packages are deleted;
    # compilation must not depend on either package any longer.
    old_model=SRC/"java/com/yigit033/matematikmacarasi/games/models/market"
    old_ui=SRC/"java/com/yigit033/matematikmacarasi/ui/screens/market"
    if old_model.exists() or old_ui.exists():
        errors.append("old market UI/model package unexpectedly restored")
    source_dir=SRC/"java"
    for file in source_dir.rglob("*.kt"):
        code=file.read_text(encoding="utf-8")
        if "import com.yigit033.matematikmacarasi.games.models.market." in code or \
           "import com.yigit033.matematikmacarasi.ui.screens.market." in code:
            errors.append("stale market import in "+str(file.relative_to(ROOT)))
    if "MarketAdventureEngine.decisionCount(run)" not in activity:
        errors.append("market analytics still normalized to legacy three-stop scores")
    if "MarketAdventureEngine.chooseBonus" not in screen:
        errors.append("fourth-step market puzzles are not connected to Compose")
    premium=(SRC/"java/com/yigit033/matematikmacarasi/games/interactive/MarketPremiumCatalog.kt")
    stall=(SRC/"java/com/yigit033/matematikmacarasi/games/interactive/MarketStallScreen.kt")
    store=(SRC/"java/com/yigit033/matematikmacarasi/games/interactive/MarketAdventureStore.kt")
    if not premium.exists() or not stall.exists() or not store.exists():
        errors.append("premium market catalog or economy implementation missing")
    else:
        catalog=premium.read_text(encoding="utf-8")
        if "shop(9," not in catalog or "FREE_SHOPS=3" not in catalog:
            errors.append("ten-store Free/Pro catalog not present")
        if "hasPremium" not in screen or "MarketStallScreen(" not in screen:
            errors.append("market screen is not wired to Premium and stall economy")
        if ("purchaseManager.isPremiumSync()" not in activity or
            "purchaseManager.buyPremium(this)" not in activity):
            errors.append("existing PurchaseManager entitlement and billing not wired")
        if "contentVersion=3" not in store or "singleShop" not in store:
            errors.append("version-three stored progress and quick sessions missing")
    if errors:
        for error in errors:print("ERROR:",error,file=sys.stderr)
        return 1
    print("PASS: eleven scenes, 10-store catalog, premium gate, virtual economy, legacy cleanup")
    return 0

if __name__=="__main__":
    sys.exit(main())
