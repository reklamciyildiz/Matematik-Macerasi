#requires -Version 5.1
# Install ONLY the two approved Avatar 3.0 pilot characters into the local
# Android source tree. No network, deployment, secrets, or automatic git push.
param(
    [Parameter(Mandatory = $true)][string]$ZipPath,
    [switch]$InstallPilot
)
$ErrorActionPreference = 'Stop'
if (-not $InstallPilot) {
    throw "Pass -InstallPilot to opt into preview-art installation."
}
if (-not (Test-Path -LiteralPath $ZipPath)) {
    throw "Pilot WebP ZIP not found: $ZipPath"
}
$repo = Split-Path -Parent $PSScriptRoot
$staging = Join-Path ([System.IO.Path]::GetTempPath()) ("avatar3-" + [Guid]::NewGuid().ToString('N'))
$target = Join-Path $repo 'app\src\main\assets\avatars3'
$names = @(
    'avatar3_06_card.webp',
    'avatar3_06_full.webp',
    'avatar3_07_card.webp',
    'avatar3_07_full.webp'
)
try {
    New-Item -ItemType Directory -Path $staging -Force | Out-Null
    Expand-Archive -LiteralPath $ZipPath -DestinationPath $staging -Force
    $src = Join-Path $staging 'avatars3'
    foreach ($name in $names) {
        $file = Join-Path $src $name
        if (-not (Test-Path -LiteralPath $file)) { throw "Missing pilot asset $name" }
        $bytes = [System.IO.File]::ReadAllBytes($file)
        if ($bytes.Length -lt 1000 -or $bytes.Length -gt 200000) {
            throw "Unexpected file size for $name"
        }
        if ([System.Text.Encoding]::ASCII.GetString($bytes,0,4) -ne 'RIFF' -or
            [System.Text.Encoding]::ASCII.GetString($bytes,8,4) -ne 'WEBP') {
            throw "Not a WebP file: $name"
        }
        $existing = Join-Path $target $name
        if (Test-Path -LiteralPath $existing) {
            $oldHash = (Get-FileHash -LiteralPath $existing -Algorithm SHA256).Hash
            $newHash = (Get-FileHash -LiteralPath $file -Algorithm SHA256).Hash
            if ($oldHash -ne $newHash) {
                throw "Different asset already installed: $name. Review manually."
            }
        }
    }
    New-Item -ItemType Directory -Path $target -Force | Out-Null
    foreach ($name in $names) {
        Copy-Item -LiteralPath (Join-Path $src $name) -Destination (Join-Path $target $name) -Force
    }
    Write-Host 'AVATAR3_PILOT_WEBP_INSTALLED'
    Write-Host "Installed ID 6 (Free explorer) and ID 7 (Premium math wizard) card/full art."
    Write-Host "Other 13 avatar illustrations remain unchanged."
    Write-Host "No release was built or deployed. Use internal testing before Production."
} finally {
    if (Test-Path -LiteralPath $staging) {
        Remove-Item -LiteralPath $staging -Recurse -Force
    }
}
