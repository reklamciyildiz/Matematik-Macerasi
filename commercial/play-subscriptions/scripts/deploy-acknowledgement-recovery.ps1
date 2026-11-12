#requires -Version 5.1
# Deploy only the existing Google Play subscription Worker (not Premium code Worker).
# No Google Play release is triggered; no credentials are printed or rewritten.
param([switch]$ForSubscriptionAckRepair)
$ErrorActionPreference = "Stop"
if (-not $ForSubscriptionAckRepair) {
    throw "Explicit opt-in required: -ForSubscriptionAckRepair."
}
Set-Location (Split-Path -Parent $PSScriptRoot)
$path=Join-Path (Get-Location).Path "wrangler.toml"
if (-not (Test-Path -LiteralPath $path)) {
    throw "Missing local subscription Worker wrangler.toml."
}
$content=Get-Content -LiteralPath $path -Raw
$checks=@(
    'name\s*=\s*"matematik-macerasi-play-subscriptions"',
    'database_id\s*=\s*"3ad1286a-45b3-412e-b09e-0cbbf4943549"',
    'SUBSCRIPTIONS_SERVER_ENABLED\s*=\s*"true"',
    'RTDN_PULL_ENABLED\s*=\s*"true"',
    'PUBSUB_SUBSCRIPTION_ID\s*=\s*"projects/matematik-macerasi/subscriptions/mm-play-rtdn-pull"'
)
foreach($check in $checks) {
    if($content -notmatch $check) {
        throw "Worker config safety check failed; refusing deployment."
    }
}
# License tests can refund unacknowledged purchases after ~3 min.
# RTDN pull every 1 minute, with immediate Android client fallback after
# server-verified entitlements, makes this recoverable without unsafe ack.
$five='crons\s*=\s*\[\s*"\*/5 \* \* \* \*"\s*\]'
$one='crons\s*=\s*\[\s*"\* \* \* \* \*"\s*\]'
if($content -match $five) {
    $content=[regex]::Replace($content,$five,'crons = ["* * * * *"]')
    [System.IO.File]::WriteAllText($path,$content,
        (New-Object System.Text.UTF8Encoding($false)))
} elseif ($content -notmatch $one) {
    throw "Unexpected cron configuration. Expected */5 or * * * * *; refusing change."
}
Write-Host "Running subscription Worker policy and recovery tests..."
& node --test tests/*.test.mjs
if ($LASTEXITCODE -ne 0) { throw "Subscription recovery tests failed; deployment cancelled." }
Write-Host "Deploying subscription acknowledgement recovery; 1-minute RTDN poll."
& npx --yes wrangler@4.158.0 deploy
if ($LASTEXITCODE -ne 0) { throw "Worker deploy failed. Existing installation may still be live." }
$healthUrl="https://matematik-macerasi-play-subscriptions.matematik-macerasi-yigit033.workers.dev/health"
$result=Invoke-RestMethod -Uri $healthUrl -Method Get -TimeoutSec 15
if (-not $result.ok -or -not $result.enabled -or -not $result.rtdnEnabled) {
    throw "Health check failed: expected enabled=true and rtdnEnabled=true."
}
Write-Host "PLAY_ACK_RECOVERY_DEPLOYED; health=ok,enabled,rtdnEnabled."
Write-Host "Real acknowledgement status and Play purchases still require device QA."
