#requires -Version 5.1
<#
 Build a real, developer-local Android debug APK for device review.
 It reads (but never uploads/prints) the developer's real google-services.json.
 The CI fake Firebase fixture must never be packaged for real-device QA.
 No release signing, Play Console upload, or database operations.
#>
param()

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot
Push-Location -LiteralPath $root
try {
    if (-not (Test-Path -LiteralPath ".\gradlew.bat")) {
        throw "Missing gradlew.bat: wrong project root."
    }
    $configPath = Join-Path $root "app\google-services.json"
    if (-not (Test-Path -LiteralPath $configPath -PathType Leaf)) {
        throw "Missing app\google-services.json. Restore your actual local Firebase configuration; never use the GitHub CI placeholder."
    }
    try {
        $cfg = Get-Content -LiteralPath $configPath -Raw -Encoding UTF8 | ConvertFrom-Json
    } catch {
        throw "app\google-services.json is not valid JSON. Do not upload your Firebase configuration to GitHub or this chat."
    }
    $projectId = [string]$cfg.project_info.project_id
    $projectNum = [string]$cfg.project_info.project_number
    $clients = @($cfg.client)
    $matchingClient = @($clients | Where-Object {
        $_.client_info.android_client_info.package_name -eq "com.yigit033.matematikmacarasi"
    })
    if ([string]::IsNullOrWhiteSpace($projectId) -or
        $projectId -eq "ci-build-only" -or
        $projectNum -eq "123456789012" -or
        $matchingClient.Count -eq 0 -or
        (@($matchingClient[0].api_key) | Where-Object {
            $_.current_key -eq "CI_ONLY_NOT_VALID"
        }).Count -ne 0) {
        throw "Firebase JSON looks like the CI-only fixture or targets another application. Refusing to produce a misleading test APK."
    }

    Write-Host "Building local Android debug APK. No production Play release will be created."
    & .\gradlew.bat :app:testDebugUnitTest :app:assembleDebug --no-daemon
    if ($LASTEXITCODE -ne 0) {
        throw "Unit tests or Android debug build failed. Do not install a previous APK."
    }

    $apk = Join-Path $root "app\build\outputs\apk\debug\app-debug.apk"
    if (-not (Test-Path -LiteralPath $apk -PathType Leaf)) {
        throw "Gradle returned success, but app-debug.apk was not found."
    }
    $version = "1.3.6"
    $code = "24"
    $gradle = Get-Content -LiteralPath (Join-Path $root "app\build.gradle") -Raw
    if ($gradle -match '(?m)^\s*versionName\s+"([^"]+)"') { $version = $Matches[1] }
    if ($gradle -match '(?m)^\s*versionCode\s+(\d+)') { $code = $Matches[1] }
    $outDir = Join-Path $root "local-debug-review"
    New-Item -Path $outDir -ItemType Directory -Force | Out-Null
    $output = Join-Path $outDir ("Matematik-Macerasi-{0}-code{1}-debug.apk" -f $version, $code)
    Copy-Item -LiteralPath $apk -Destination $output -Force
    $item = Get-Item -LiteralPath $output
    $hash = Get-FileHash -LiteralPath $output -Algorithm SHA256
    Write-Host ""
    Write-Host "DEBUG_APK_READY"
    Write-Host ("Path: " + $item.FullName)
    Write-Host ("Size MB: " + [Math]::Round($item.Length / 1MB, 2))
    Write-Host ("SHA256: " + $hash.Hash)
    Write-Host "Debug APK is only for manual review; this is not a signed Play production AAB."
    Write-Host "Do not uninstall the production application or clear app data as part of testing."
    Write-Host "If Android reports a signature mismatch, stop: do not uninstall to force installation."
} finally {
    Pop-Location
}
