#requires -Version 5.1
param(
    [string]$GoogleServiceAccountJson = "",
    [string]$ExistingDatabaseId = ""
)
# Separate staging Worker / D1. Both entitlement feature flags remain FALSE.
# No existing Premium code database or Google Play production is changed.
$ErrorActionPreference = "Stop"
Set-Location (Split-Path -Parent $PSScriptRoot)
$dbName = "matematik-macerasi-play-subscriptions"
$legacyDbId = "278757f5-3ff1-490e-854d-d7eb6898f296"
$expectedEmail = "mm-play-billing-verifier@matematik-macerasi.iam.gserviceaccount.com"

function Run-Wrangler([string[]]$arguments) {
    & npx --yes wrangler@4.158.0 @arguments
    if ($LASTEXITCODE -ne 0) { throw "Wrangler failure: $($arguments[0]) $($arguments[1])" }
}
function Get-SubscriptionD1 {
    $output = & npx --yes wrangler@4.158.0 d1 list --json
    if ($LASTEXITCODE -ne 0) { throw "Cloudflare D1 listing failed" }
    # A fresh npx directory can emit an interactive "Need to install" prompt.
    # Treat a non-JSON stdout as a bootstrap error, never as a D1 listing.
    $jsonText = ($output -join [Environment]::NewLine).Trim()
    if (-not $jsonText.StartsWith("[")) {
        throw "Cloudflare did not return D1 JSON. Check auth: npx --yes wrangler@4.158.0 whoami ; login if needed."
    }
    try {
        $dbs = @(ConvertFrom-Json -InputObject $jsonText -ErrorAction Stop)
    } catch {
        throw "Cloudflare D1 output is not valid JSON. Check Wrangler login and retry."
    }
    $found = @($dbs | Where-Object { $_.name -eq $dbName })
    if ($found.Count -gt 1) { throw "Duplicate D1 database name" }
    if ($found.Count -eq 0) { return $null }
    return $found[0]
}
if (-not (Get-Command npx -ErrorAction SilentlyContinue)) { throw "Node.js/npx missing" }
Write-Host "=== Play subscriptions: isolated staging setup ==="
$db = Get-SubscriptionD1
if ($null -eq $db -and $ExistingDatabaseId) {
    throw "A D1 UUID override was provided, but no database with the required name exists. Refusing to create a replacement."
}
if ($null -eq $db) {
    Write-Host "Creating a separate subscription database ..."
    Run-Wrangler -arguments @("d1", "create", $dbName, "--location", "eeur")
    $db = Get-SubscriptionD1
}
if ($null -eq $db) { throw "Subscription database not returned by Cloudflare" }
# Wrangler D1 list can expose its ID under different property names.
# When the list omits a usable ID, accept an explicitly supplied UUID
# copied from Wrangler's successful d1 create output. NEVER create another DB.
$uuidPattern = '^[0-9a-fA-F]{8}(-[0-9a-fA-F]{4}){3}-[0-9a-fA-F]{12}
if ($dbId -eq $legacyDbId) { throw "SAFETY BLOCK: existing Premium code D1" }

$template = Get-Content ".\wrangler.toml.example" -Raw
if (-not $template.Contains('SUBSCRIPTIONS_SERVER_ENABLED = "false"') -or
    -not $template.Contains('RTDN_PULL_ENABLED = "false"')) {
    throw "Unsafe Wrangler feature flags"
}
$configPath = Join-Path (Get-Location).Path "wrangler.toml"
if (Test-Path $configPath) {
    $oldConfig = Get-Content $configPath -Raw
    if ($oldConfig -match 'SUBSCRIPTIONS_SERVER_ENABLED\s*=\s*"true"' -or
        $oldConfig -match 'RTDN_PULL_ENABLED\s*=\s*"true"') {
        throw "SAFETY BLOCK: existing enabled config; refusing overwrite"
    }
}
$config = $template.Replace("REPLACE_WITH_REAL_D1_UUID", $dbId)
[System.IO.File]::WriteAllText($configPath,$config,
    (New-Object System.Text.UTF8Encoding($false)))
Write-Host "Separated D1 UUID: $dbId"
Write-Host "Applying migration ONLY to $dbName ..."
Run-Wrangler -arguments @("d1","migrations","apply",$dbName,"--remote")
Write-Host "Deploying disabled staging Worker ..."
Run-Wrangler -arguments @("deploy")
if (-not $GoogleServiceAccountJson) {
    Write-Host "STAGING_READY. Sales DISABLED. Google credentials NOT configured."
    Write-Host "To connect credentials, rerun with -GoogleServiceAccountJson <local JSON path>."
    exit 0
}
$keyPath = (Resolve-Path -LiteralPath $GoogleServiceAccountJson).Path
$key = Get-Content -LiteralPath $keyPath -Raw | ConvertFrom-Json
if ($key.type -ne "service_account" -or $key.client_email -ne $expectedEmail -or
    -not $key.private_key) {
    throw "Wrong JSON key: must be the dedicated mm-play-billing-verifier account."
}
Write-Host "Uploading Google Play service-account credentials as Worker secret ..."
Get-Content -LiteralPath $keyPath -Raw | & npx --yes wrangler@4.158.0 secret put GOOGLE_SERVICE_ACCOUNT_JSON
if ($LASTEXITCODE -ne 0) { throw "Service-account secret upload failed" }

$privateDir = Join-Path (Get-Location).Path "private"
if (-not (Test-Path $privateDir)) {
    New-Item -ItemType Directory $privateDir | Out-Null
}
if (-not (Test-Path (Join-Path $privateDir "play-lease-private.pem"))) {
    & node ".\scripts\generate-play-lease-key.mjs"
    if ($LASTEXITCODE -ne 0) { throw "Play lease key generation failed" }
}
$privatePem = Join-Path $privateDir "play-lease-private.pem"
$publicBase64 = Join-Path $privateDir "play-lease-public.b64.txt"
if (-not (Test-Path $publicBase64)) { throw "Missing Play lease public key" }
Write-Host "Uploading dedicated Play lease private key as Worker secret ..."
Get-Content -LiteralPath $privatePem -Raw | & npx --yes wrangler@4.158.0 secret put PLAY_LEASE_PRIVATE_KEY
if ($LASTEXITCODE -ne 0) { throw "Play lease secret upload failed" }
Write-Host "SECRETS_READY. Sales still DISABLED; no charges or payment activation."
Write-Host "Android public signing key is stored at private/play-lease-public.b64.txt"
Write-Host "Play Integrity Cloud project number: 740643037501"
Write-Host "Never send the JSON, private PEM, or purchase token to a chat."

$knownIds = @([string]$db.uuid, [string]$db.id, [string]$db.database_id, [string]$db.databaseId) |
    Where-Object { $_ -match $uuidPattern }
$discoveredId = @($knownIds | Select-Object -First 1)
$dbId = if ($discoveredId.Count -gt 0) { [string]$discoveredId[0] } else { "" }
if ($ExistingDatabaseId) {
    if ($ExistingDatabaseId -notmatch $uuidPattern) {
        throw "ExistingDatabaseId must be a valid D1 UUID."
    }
    if ($dbId -and $dbId -ne $ExistingDatabaseId) {
        throw "Safety block: provided D1 UUID differs from Cloudflare's listed database ID."
    }
    $dbId = $ExistingDatabaseId
}
if ($dbId -notmatch $uuidPattern) {
    throw "D1 list did not expose a valid UUID. Rerun with -ExistingDatabaseId and the UUID printed by Wrangler d1 create."
}
if ($dbId -eq $legacyDbId) { throw "SAFETY BLOCK: existing Premium code D1" }

$template = Get-Content ".\wrangler.toml.example" -Raw
if (-not $template.Contains('SUBSCRIPTIONS_SERVER_ENABLED = "false"') -or
    -not $template.Contains('RTDN_PULL_ENABLED = "false"')) {
    throw "Unsafe Wrangler feature flags"
}
$configPath = Join-Path (Get-Location).Path "wrangler.toml"
if (Test-Path $configPath) {
    $oldConfig = Get-Content $configPath -Raw
    if ($oldConfig -match 'SUBSCRIPTIONS_SERVER_ENABLED\s*=\s*"true"' -or
        $oldConfig -match 'RTDN_PULL_ENABLED\s*=\s*"true"') {
        throw "SAFETY BLOCK: existing enabled config; refusing overwrite"
    }
}
$config = $template.Replace("REPLACE_WITH_REAL_D1_UUID", $dbId)
[System.IO.File]::WriteAllText($configPath,$config,
    (New-Object System.Text.UTF8Encoding($false)))
Write-Host "Separated D1 UUID: $dbId"
Write-Host "Applying migration ONLY to $dbName ..."
Run-Wrangler -arguments @("d1","migrations","apply",$dbName,"--remote")
Write-Host "Deploying disabled staging Worker ..."
Run-Wrangler -arguments @("deploy")
if (-not $GoogleServiceAccountJson) {
    Write-Host "STAGING_READY. Sales DISABLED. Google credentials NOT configured."
    Write-Host "To connect credentials, rerun with -GoogleServiceAccountJson <local JSON path>."
    exit 0
}
$keyPath = (Resolve-Path -LiteralPath $GoogleServiceAccountJson).Path
$key = Get-Content -LiteralPath $keyPath -Raw | ConvertFrom-Json
if ($key.type -ne "service_account" -or $key.client_email -ne $expectedEmail -or
    -not $key.private_key) {
    throw "Wrong JSON key: must be the dedicated mm-play-billing-verifier account."
}
Write-Host "Uploading Google Play service-account credentials as Worker secret ..."
Get-Content -LiteralPath $keyPath -Raw | & npx --yes wrangler@4.158.0 secret put GOOGLE_SERVICE_ACCOUNT_JSON
if ($LASTEXITCODE -ne 0) { throw "Service-account secret upload failed" }

$privateDir = Join-Path (Get-Location).Path "private"
if (-not (Test-Path $privateDir)) {
    New-Item -ItemType Directory $privateDir | Out-Null
}
if (-not (Test-Path (Join-Path $privateDir "play-lease-private.pem"))) {
    & node ".\scripts\generate-play-lease-key.mjs"
    if ($LASTEXITCODE -ne 0) { throw "Play lease key generation failed" }
}
$privatePem = Join-Path $privateDir "play-lease-private.pem"
$publicBase64 = Join-Path $privateDir "play-lease-public.b64.txt"
if (-not (Test-Path $publicBase64)) { throw "Missing Play lease public key" }
Write-Host "Uploading dedicated Play lease private key as Worker secret ..."
Get-Content -LiteralPath $privatePem -Raw | & npx --yes wrangler@4.158.0 secret put PLAY_LEASE_PRIVATE_KEY
if ($LASTEXITCODE -ne 0) { throw "Play lease secret upload failed" }
Write-Host "SECRETS_READY. Sales still DISABLED; no charges or payment activation."
Write-Host "Android public signing key is stored at private/play-lease-public.b64.txt"
Write-Host "Play Integrity Cloud project number: 740643037501"
Write-Host "Never send the JSON, private PEM, or purchase token to a chat."
