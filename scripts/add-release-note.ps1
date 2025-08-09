$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$rootDir = Resolve-Path (Join-Path $scriptDir "..")
$jsonPath = Join-Path $rootDir "release-notes.json"

if (-not (Test-Path $jsonPath)) {
    Write-Host "release-notes.json not found in root. Creating a new one..."
    '{ "changelog": [] }' | Set-Content -Path $jsonPath -Encoding UTF8
}

$jsonContent = Get-Content -Path $jsonPath -Raw
$data = $null

try {
    $data = ConvertFrom-Json $jsonContent
}
catch {
    Write-Warning "Failed to parse JSON. Creating new structure."
    $data = [PSCustomObject]@{ changelog = @() }
}

if (-not $data.changelog) {
    $data.changelog = @()
}

$versionName = Read-Host "Enter version name (e.g., 1.0.4)"
$description = Read-Host "Enter description (can be 'null' or text)"
if ($description.Trim().ToLower() -eq "null") {
    $description = $null
}
$releaseType = Read-Host "Enter release type (alpha, beta, rc, stable, pr)"
$hasVersionName = Read-Host "Has version name? (true/false)"
$supportsHtml = Read-Host "Supports HTML? (true/false)"

$hasVersionNameBool = $false
$supportsHtmlBool = $false
try {
    $hasVersionNameBool = [System.Convert]::ToBoolean($hasVersionName)
}
catch {
}
try {
    $supportsHtmlBool = [System.Convert]::ToBoolean($supportsHtml)
}
catch {
}

$epochMillis = [int64](([DateTimeOffset]::UtcNow).ToUnixTimeMilliseconds())

$newNote = [PSCustomObject]@{
    versionName = $versionName
    description = $description
    releaseDate = $epochMillis
    releaseType = $releaseType
    hasVersionName = $hasVersionNameBool
    supportsHtml = $supportsHtmlBool
}

# Append new note to the changelog array
$data.changelog += $newNote

# Convert whole object back to JSON and save
$data | ConvertTo-Json -Depth 5 | Set-Content -Path $jsonPath -Encoding UTF8

Write-Host "New release note added to $jsonPath"