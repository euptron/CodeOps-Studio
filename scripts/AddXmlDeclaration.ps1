# Adds XML declaration to XML files if missing.
# Skips excluded directories and AndroidManifest.xml.
# Creates backups in a single folder before modifying.
# Skips files locked by other processes.
# TODO: Fix saving in UTF-8 with BOM to just UTF-8

$rootPath = "C:\Users\HomePC\Desktop\euptron\AndroidStudioProjects\CodeOps Studio"
$excludeDirs = @("build", ".gradle", ".idea", "out", "bin", "gen")
$xmlDeclaration = '<?xml version="1.0" encoding="utf-8"?>'

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupRoot = Join-Path $rootPath ("XML_Backups_" + $timestamp)
New-Item -Path $backupRoot -ItemType Directory -Force | Out-Null

$updatedFiles = @()
$skippedLocked = @()

function UpdateXmlDeclarationInDirectory {
    param (
        [string]$path
    )

    $dirName = Split-Path $path -Leaf
    if ($excludeDirs -contains $dirName) {
        return
    }

    Get-ChildItem -Path $path -File -Filter *.xml -ErrorAction SilentlyContinue | ForEach-Object {
        if ($_.Name -ne "AndroidManifest.xml") {
            $lines = Get-Content $_.FullName -Encoding UTF8
            if ($lines.Count -eq 0 -or $lines[0].Trim() -ne $xmlDeclaration) {
                $relativePath = Resolve-Path $_.FullName | ForEach-Object { $_.Path.Substring($rootPath.Length).TrimStart("\") }
                $backupPath = Join-Path $backupRoot $relativePath
                $backupDir = Split-Path $backupPath -Parent
                New-Item -Path $backupDir -ItemType Directory -Force | Out-Null

                Copy-Item -Path $_.FullName -Destination $backupPath -Force

                try {
                    $newContent = @($xmlDeclaration) + $lines
                    Set-Content -Path $_.FullName -Value $newContent -Encoding UTF8 -ErrorAction Stop
                    $updatedFiles += $_.FullName
                }
                catch {
                    Write-Warning "File locked, skipped: $( $_.FullName )"
                    $skippedLocked += $_.FullName
                }
            }
        }
    }

    Get-ChildItem -Path $path -Directory -ErrorAction SilentlyContinue | ForEach-Object {
        UpdateXmlDeclarationInDirectory $_.FullName
    }
}

UpdateXmlDeclarationInDirectory $rootPath

Write-Host "`nXML check and update completed."
Write-Host "Files updated: $( $updatedFiles.Count )"
Write-Host "Backups stored in: $backupRoot"

if ($updatedFiles.Count -gt 0) {
    Write-Host "`nUpdated files:"
    $updatedFiles | ForEach-Object { Write-Host " - $_" }
}

if ($skippedLocked.Count -gt 0) {
    Write-Host "`nSkipped locked files ($( $skippedLocked.Count )):"
    $skippedLocked | ForEach-Object { Write-Host " - $_" }
}
