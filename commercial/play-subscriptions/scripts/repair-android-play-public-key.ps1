#requires -Version 5.1
# Fix Android's Play subscription RSA public-key property from the EXISTING local
# subscription signing keypair. Never rotates keys or prints the private key.
$ErrorActionPreference = 'Stop'

$repoRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..\..\..'))
$subsDir = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '..'))
$publicPath = Join-Path $subsDir 'private\play-lease-public.b64.txt'
$privatePath = Join-Path $subsDir 'private\play-lease-private.pem'
$propertiesPath = Join-Path $repoRoot 'gradle.properties'

foreach ($file in @($publicPath, $privatePath, $propertiesPath)) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        throw "Required local file not found: $file. Do NOT generate a new signing keypair."
    }
}

$publicKey = [System.IO.File]::ReadAllText($publicPath).Trim()
if ($publicKey -notmatch '^[A-Za-z0-9+/=]{200,}$') {
    throw 'Existing Play subscription RSA public key is not valid Base64 DER.'
}
if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    throw 'Node.js is required to verify the existing local RSA keypair.'
}

# Derive a public key from the EXISTING PEM. This is a read-only check.
# NEVER upload/display the private key or generate a replacement.
$js = 'const fs=require("node:fs");const c=require("node:crypto");try{const k=c.createPublicKey(fs.readFileSync(process.argv[1],"utf8"));process.stdout.write(k.export({type:"spki",format:"der"}).toString("base64"));}catch(e){process.stderr.write("Cannot read or derive the existing Play subscription keypair.");process.exit(1);}'
$derived = & node -e $js $privatePath
if ($LASTEXITCODE -ne 0 -or ([string]$derived).Trim() -ne $publicKey) {
    throw 'Local RSA public/private keys do not match. Do not replace the Cloudflare secret.'
}

$contents = [System.IO.File]::ReadAllText($propertiesPath, [System.Text.Encoding]::UTF8)
$pattern = '(?m)^[ \t]*MM_PLAY_SUBS_PUBLIC_KEY_B64[ \t]*=[^\r\n]*'
$matches = [regex]::Matches($contents, $pattern)
if ($matches.Count -gt 1) {
    throw 'Duplicate MM_PLAY_SUBS_PUBLIC_KEY_B64 entries in project gradle.properties. Remove duplicates first.'
}
$entry = 'MM_PLAY_SUBS_PUBLIC_KEY_B64=' + $publicKey
if ($matches.Count -eq 1) {
    $contents = $contents.Replace($matches[0].Value, $entry)
} else {
    if (-not $contents.EndsWith("`n")) { $contents += [Environment]::NewLine }
    $contents += $entry + [Environment]::NewLine
}

# Gradle user-home properties take precedence over the project file. Fail safely
# if they would override the repaired local value.
$userHomeGradle = Join-Path $HOME '.gradle\gradle.properties'
if (Test-Path -LiteralPath $userHomeGradle -PathType Leaf) {
    $other = [System.IO.File]::ReadAllText($userHomeGradle)
    $found = [regex]::Matches($other, $pattern)
    if ($found.Count -gt 0) {
        $userKey = $found[$found.Count - 1].Value -replace '^[^=]*=', ''
        if ($userKey.Trim() -ne $publicKey) {
            throw "Conflicting MM_PLAY_SUBS_PUBLIC_KEY_B64 in $userHomeGradle. Correct/remove that overriding value first."
        }
    }
}

# Write the public key only, never touch any Worker secret or other Gradle flags.
[System.IO.File]::WriteAllText($propertiesPath, $contents, (New-Object System.Text.UTF8Encoding($false)))

$needed = @{
    'MM_SUBSCRIPTIONS_ENABLED' = 'true'
    'MM_PLAY_SUBS_INTERNAL_QA_BUILD' = 'true'
    'MM_PLAY_SUBS_URL' = 'https://matematik-macerasi-play-subscriptions.matematik-macerasi-yigit033.workers.dev'
    'MM_PLAY_INTEGRITY_PROJECT_NUMBER' = '740643037501'
}
foreach ($key in $needed.Keys) {
    $m = [regex]::Match($contents, ('(?m)^[ \t]*' + [regex]::Escape($key) + '[ \t]*=[ \t]*([^\r\n#]*)'))
    if (-not $m.Success -or $m.Groups[1].Value.Trim() -ne $needed[$key]) {
        Write-Warning "$key is absent or differs from the expected internal Play QA value; check the local file."
    }
}
Write-Host 'PLAY_SUBS_PUBLIC_KEY_READY: local RSA pair verified; project gradle.properties updated.'
Write-Host 'The deployed Worker private key itself cannot be compared remotely; confirm using real Play license-tester purchases.'
Write-Host 'Do not share private/play-lease-private.pem, app signing passwords or the Google service-account JSON.'
