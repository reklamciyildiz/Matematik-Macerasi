#requires -Version 5.1
# Repair only the LOCAL Play subscription public key used by Android Gradle.
# Derive it from the EXISTING PEM; never create/rotate any RSA key or Worker secret.
$ErrorActionPreference = 'Stop'

$repoRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..'))
$subsDir = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$publicPath = Join-Path $subsDir 'private\play-lease-public.b64.txt'
$privatePath = Join-Path $subsDir 'private\play-lease-private.pem'
$propertiesPath = Join-Path $repoRoot 'gradle.properties'
$helperPath = Join-Path $PSScriptRoot 'derive-existing-play-public-key.mjs'

foreach ($file in @($privatePath, $propertiesPath, $helperPath)) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        throw "Missing required local file: $file. Do NOT generate a new signing keypair."
    }
}
if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    throw 'Node.js is required to inspect the EXISTING RSA private key locally.'
}

# Node writes ONLY public SPKI Base64 to stdout; its error output contains no PEM.
$derivedRaw = & node $helperPath $privatePath
if ($LASTEXITCODE -ne 0) {
    throw 'Cannot derive public key from EXISTING private PEM; Worker secret must remain unchanged.'
}
$derived = ([string]$derivedRaw).Trim()
if ($derived -notmatch '^[A-Za-z0-9+/=]{200,}$') {
    throw 'Derived RSA SPKI public key did not pass Base64 validation.'
}

$repairPublicFile = -not (Test-Path -LiteralPath $publicPath -PathType Leaf)
if (-not $repairPublicFile) {
    $existing = [System.IO.File]::ReadAllText($publicPath)
    # Accept BOM and wrapped Base64; normalize to canonical single-line SPKI DER.
    $normalized = [regex]::Replace($existing.TrimStart([char]0xFEFF), '\s+', '')
    if ($normalized -match '^[A-Za-z0-9+/=]{200,}$' -and $normalized -ne $derived) {
        throw 'Existing valid-format public key differs from EXISTING private key. Refusing silent key substitution.'
    }
    $repairPublicFile = ($normalized -ne $derived -or $existing.Trim() -ne $derived)
    if ($normalized -notmatch '^[A-Za-z0-9+/=]{200,}$') {
        Write-Warning 'Stored public key file is malformed; deriving it from the EXISTING local private PEM.'
    }
}

$contents = [System.IO.File]::ReadAllText($propertiesPath, [System.Text.Encoding]::UTF8)
$pattern = '(?m)^[ \t]*MM_PLAY_SUBS_PUBLIC_KEY_B64[ \t]*=[^\r\n]*'
$matches = [regex]::Matches($contents, $pattern)
if ($matches.Count -gt 1) {
    throw 'Duplicate MM_PLAY_SUBS_PUBLIC_KEY_B64 entries. Remove duplicates before repair.'
}
$entry = 'MM_PLAY_SUBS_PUBLIC_KEY_B64=' + $derived
if ($matches.Count -eq 1) {
    $contents = $contents.Replace($matches[0].Value, $entry)
} else {
    if (-not $contents.EndsWith("`n")) { $contents += [Environment]::NewLine }
    $contents += $entry + [Environment]::NewLine
}

# User-home Gradle properties override project Gradle properties; refuse a
# conflicting higher-priority value rather than silently producing a wrong AAB.
$userHomeGradle = Join-Path $HOME '.gradle\gradle.properties'
if (Test-Path -LiteralPath $userHomeGradle -PathType Leaf) {
    $other = [System.IO.File]::ReadAllText($userHomeGradle)
    $found = [regex]::Matches($other, $pattern)
    if ($found.Count -gt 0) {
        $userKey = ($found[$found.Count-1].Value -replace '^[^=]*=', '').Trim()
        if ($userKey -ne $derived) {
            throw "Conflicting MM_PLAY_SUBS_PUBLIC_KEY_B64 in $userHomeGradle. Remove/correct that property."
        }
    }
}

if ($repairPublicFile) {
    # Only the (public) local .b64.txt is repaired; never touch the private PEM.
    if (Test-Path -LiteralPath $publicPath -PathType Leaf) {
        $backup = $publicPath + '.invalid-' + (Get-Date -Format 'yyyyMMdd-HHmmss') + '.bak'
        Copy-Item -LiteralPath $publicPath -Destination $backup -ErrorAction Stop
        Write-Host 'Original malformed public-key file backed up locally under ignored private/.'
    }
    [System.IO.File]::WriteAllText($publicPath, $derived + [Environment]::NewLine,
        (New-Object System.Text.UTF8Encoding($false)))
}

[System.IO.File]::WriteAllText($propertiesPath, $contents,
    (New-Object System.Text.UTF8Encoding($false)))

$needed = @{
    'MM_SUBSCRIPTIONS_ENABLED' = 'true'
    'MM_PLAY_SUBS_INTERNAL_QA_BUILD' = 'true'
    'MM_PLAY_SUBS_URL' = 'https://matematik-macerasi-play-subscriptions.matematik-macerasi-yigit033.workers.dev'
    'MM_PLAY_INTEGRITY_PROJECT_NUMBER' = '740643037501'
}
foreach ($key in $needed.Keys) {
    $m = [regex]::Match($contents, ('(?m)^[ \t]*' + [regex]::Escape($key) + '[ \t]*=[ \t]*([^\r\n#]*)'))
    if (-not $m.Success -or $m.Groups[1].Value.Trim() -ne $needed[$key]) {
        Write-Warning "$key is absent or differs from the expected Internal Testing value."
    }
}
Write-Host 'PLAY_SUBS_PUBLIC_KEY_READY: derived from EXISTING private PEM; local Gradle property corrected.'
Write-Host 'No Worker secret or private key was modified. Real Play purchases must confirm the Worker key matches.'
