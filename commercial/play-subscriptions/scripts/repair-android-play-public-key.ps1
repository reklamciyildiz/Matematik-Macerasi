#requires -Version 5.1
# Avoid capturing long, no-newline Base64 in Windows PowerShell native stdout.
# The Node helper validates/writes ONLY the existing RSA public key and
# project gradle.properties; the signing private PEM and Worker stay unchanged.
$ErrorActionPreference = 'Stop'
$helper = Join-Path $PSScriptRoot 'repair-android-play-public-key.mjs'
if (-not (Test-Path -LiteralPath $helper -PathType Leaf)) {
    throw 'Missing key repair helper script. Run git pull first.'
}
if (-not (Get-Command node -ErrorAction SilentlyContinue)) {
    throw 'Node.js is required for key repair.'
}
& node $helper
if ($LASTEXITCODE -ne 0) {
    throw 'Existing Play RSA key repair failed. Do not create a new private key or disable the Gradle validation.'
}
