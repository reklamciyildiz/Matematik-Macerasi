#requires -Version 5.1
# Enable the Google Play subscription VERIFY endpoint for real license-tester QA.
# This does NOT publish an AAB, open a Play testing track, or approve production.
param([switch]$ForInternalTesting)
$ErrorActionPreference = "Stop"
if (-not $ForInternalTesting) {
    throw "Explicit opt-in required: -ForInternalTesting. No production launch is implied."
}
Set-Location (Split-Path -Parent $PSScriptRoot)
$path = Join-Path (Get-Location).Path "wrangler.toml"
if (-not (Test-Path -LiteralPath $path)) {
    throw "Missing local wrangler.toml. Run setup-staging.ps1 and configure-rtdn.ps1 first."
}
$content = Get-Content -LiteralPath $path -Raw
if ($content -notmatch 'database_id\s*=\s*"3ad1286a-45b3-412e-b09e-0cbbf4943549"') {
    throw "Safety stop: unexpected Play subscription D1 database ID."
}
if ($content -notmatch 'RTDN_PULL_ENABLED\s*=\s*"true"' -or
    $content -notmatch 'PUBSUB_SUBSCRIPTION_ID\s*=\s*"projects/matematik-macerasi/subscriptions/mm-play-rtdn-pull"') {
    throw "Safety stop: RTDN pull must remain correctly configured."
}
if ($content -notmatch 'SUBSCRIPTIONS_SERVER_ENABLED\s*=\s*"(false|true)"') {
    throw "Safety stop: missing or unexpected subscription server flag."
}
$content = [regex]::Replace($content, 'SUBSCRIPTIONS_SERVER_ENABLED\s*=\s*"(false|true)"',
    'SUBSCRIPTIONS_SERVER_ENABLED = "true"')
[System.IO.File]::WriteAllText($path, $content, (New-Object System.Text.UTF8Encoding($false)))
Write-Host "Deploying subscription VERIFY endpoint for INTERNAL PLAY LICENSE TESTING."
Write-Host "Public Play production release is NOT approved by this script."
& npx --yes wrangler@4.158.0 deploy
if ($LASTEXITCODE -ne 0) { throw "Worker deployment failed. Do NOT test purchases until resolved." }
$healthUrl = "https://matematik-macerasi-play-subscriptions.matematik-macerasi-yigit033.workers.dev/health"
$result = Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec 15
if (-not $result.ok -or -not $result.enabled -or -not $result.rtdnEnabled) {
    throw "Worker health not ready: expected ok=true, enabled=true, rtdnEnabled=true."
}
Write-Host "PLAY_INTERNAL_QA_SERVER_READY; health=ok,enabled,rtdnEnabled. No real purchases verified yet."
