#requires -Version 5.1
# Builds an R8-minified, correctly signed Android RELEASE bundle for
# Play internal testing. Never uploads a file or changes release tracks.
param([switch]$ForInternalTesting)
$ErrorActionPreference = "Stop"

if (-not $ForInternalTesting) {
    throw "Safety opt-in missing: -ForInternalTesting. Does NOT authorize Production."
}
$root = Split-Path -Parent $PSScriptRoot
Set-Location -LiteralPath $root
if (-not (Test-Path -LiteralPath ".\gradlew.bat")) {
    throw "Run from the correct MatematikMacerasiii repository."
}
Write-Host "Running Android unit tests and R8-optimized signed APK/AAB release..."
Write-Host "Existing cloudflare, signing and Play internal test release guards still apply."

# Use a project-specific -P override; do not rewrite the developer's
# local gradle.properties or any keystore, RSA key or Cloudflare secret.
& .\gradlew.bat :app:testDebugUnitTest :app:assembleRelease :app:bundleRelease --no-daemon "-PMM_R8_RELEASE_ENABLED=true"
if ($LASTEXITCODE -ne 0) {
    throw "Optimized build failed. DO NOT upload any previously generated AAB."
}

$bundle = Join-Path $root "app\build\outputs\bundle\release\app-release.aab"
$mapping = Join-Path $root "app\build\outputs\mapping\release\mapping.txt"
if (-not (Test-Path -LiteralPath $bundle)) {
    throw "Missing signed release AAB."
}
if (-not (Test-Path -LiteralPath $mapping)) {
    throw "R8 mapping missing. This bundle is not verified as minified."
}
if ((Get-Item -LiteralPath $mapping).Length -lt 1024) {
    throw "R8 mapping unexpectedly small. Verify proguard config before upload."
}
$matchedAppClass = Select-String -LiteralPath $mapping -Pattern '^com\.yigit033\.matematikmacarasi\.[^ ]+ -> [^: ]+:$' |
    Where-Object {
        $parts = $_.Line -split ' -> '
        $parts.Count -eq 2 -and
        $parts[0] -ne $parts[1].TrimEnd(':')
    } | Select-Object -First 1
if (-not $matchedAppClass) {
    throw "No renamed app class in R8 mapping; no verified obfuscation."
}

Write-Host "R8_OPTIMIZED_AAB_READY_FOR_INTERNAL_TEST"
Get-Item -LiteralPath $bundle | Select-Object FullName,Length,LastWriteTime
Write-Host ("R8 mapping saved locally: " + $mapping)
Write-Host "Do not share mapping.txt publicly. Upload only after real Play internal QA."
