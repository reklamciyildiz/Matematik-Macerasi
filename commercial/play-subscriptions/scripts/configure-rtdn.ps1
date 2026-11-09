#requires -Version 5.1
# Only configures RTDN polling; purchases / sale entitlement remain disabled.
param([Parameter(Mandatory=$true)][string]$ProjectId)
$ErrorActionPreference="Stop"
Set-Location (Split-Path -Parent $PSScriptRoot)
if ($ProjectId -notmatch '^[a-z][a-z0-9-]{4,28}[a-z0-9]$') {
    throw "Invalid Google Cloud project ID. Use the exact project ID from Cloud Shell."
}
$path=Join-Path (Get-Location).Path "wrangler.toml"
if (-not (Test-Path $path)) {
    throw "No local wrangler.toml. Run scripts/setup-staging.ps1 first."
}
$content=Get-Content -LiteralPath $path -Raw
if ($content -notmatch 'SUBSCRIPTIONS_SERVER_ENABLED\s*=\s*"false"') {
    throw "Safety block: sales must be disabled for RTDN bootstrap."
}
if ($content -notmatch 'database_id\s*=\s*"3ad1286a-45b3-412e-b09e-0cbbf4943549"') {
    throw "Safety block: this is not the dedicated subscription D1 instance."
}
$sub="projects/$ProjectId/subscriptions/mm-play-rtdn-pull"
$content=$content.Replace('RTDN_PULL_ENABLED = "false"','RTDN_PULL_ENABLED = "true"')
$content=[regex]::Replace($content,'PUBSUB_SUBSCRIPTION_ID\s*=\s*"[^"]*"',
    ('PUBSUB_SUBSCRIPTION_ID = "'+$sub+'"'))
if ($content -notmatch 'RTDN_PULL_ENABLED\s*=\s*"true"' -or
    $content -notmatch [regex]::Escape($sub)) {
    throw "Failed to safely configure RTDN settings."
}
[System.IO.File]::WriteAllText($path,$content,
    (New-Object System.Text.UTF8Encoding($false)))
Write-Host "Deploying RTDN polling only; Play subscription sales stay DISABLED ..."
& npx --yes wrangler@4.158.0 deploy
if ($LASTEXITCODE -ne 0) { throw "Cloudflare RTDN staging deployment failed" }
Write-Host "RTDN_POLL_READY. App paid subscriptions STILL DISABLED."
Write-Host "Use the Play Console test message and verify backend logs/Google notification delivery."
