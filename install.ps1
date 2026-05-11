<#
.SYNOPSIS
  SYNAPSE Installer - Windows
.DESCRIPTION
  Detects OS, installs Go if needed, builds and runs the Go TUI installer.
  Also installs the 'synapse' CLI command to your PATH for host-side use.
#>

$ErrorActionPreference = "Stop"

$RootDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$CliDir = Join-Path $RootDir "packages/cli"
$BinDir = "$env:LOCALAPPDATA\synapse\bin"

$ESC = [char]27
$Bold   = $ESC + "[1m"
$Blue   = $ESC + "[38;2;123;159;224m"
$Green  = $ESC + "[38;2;76;175;135m"
$Yellow = $ESC + "[38;2;255;200;50m"
$Red    = $ESC + "[38;2;255;80;80m"
$Reset  = $ESC + "[0m"

Clear-Host

Write-Host ($Bold + $Blue + "   +----------------------------------------------+")
Write-Host ($Bold + $Blue + "   |                                              |")
Write-Host ($Bold + $Blue + "   |           SYNAPSE INSTALLER                  |")
Write-Host ($Bold + $Blue + "   |           Bootstrap Script                   |")
Write-Host ($Bold + $Blue + "   |                                              |")
Write-Host ($Bold + $Blue + "   +----------------------------------------------+" + $Reset)
Write-Host ""

# Step 1: Detect OS
Write-Host ($Bold + $Blue + "Detecting system..." + $Reset)

$pkgMgr = "unknown"
$hasWinget = Get-Command winget -ErrorAction SilentlyContinue
$hasChoco = Get-Command choco -ErrorAction SilentlyContinue
$hasScoop = Get-Command scoop -ErrorAction SilentlyContinue

if ($hasWinget) { $pkgMgr = "winget" }
elseif ($hasChoco) { $pkgMgr = "choco" }
elseif ($hasScoop) { $pkgMgr = "scoop" }

Write-Host "  OS:            Windows"
Write-Host "  Package mgr:   $pkgMgr"
Write-Host ""

# Step 2: Check / Install Go
$hasGo = Get-Command go -ErrorAction SilentlyContinue
if ($hasGo) {
    $ver = go version
    Write-Host ($Green + "OK Go " + $ver + " found" + $Reset)
} else {
    Write-Host ($Yellow + "WARN Go not found - installing..." + $Reset)

    if ($pkgMgr -eq "winget") {
        winget install GoLang.Go
    } elseif ($pkgMgr -eq "choco") {
        choco install -y golang
    } elseif ($pkgMgr -eq "scoop") {
        scoop install go
    } else {
        Write-Host ($Yellow + "WARN No known package manager. Downloading Go manually..." + $Reset)
        $url = "https://go.dev/dl/go1.26.windows-amd64.msi"
        $msi = "$env:TEMP\go1.26.windows-amd64.msi"
        Invoke-WebRequest -Uri $url -OutFile $msi
        Start-Process msiexec.exe -Wait -ArgumentList "/i $msi /quiet"
        Remove-Item $msi -Force
    }

    $machinePath = [System.Environment]::GetEnvironmentVariable("Path", "Machine")
    $userPath = [System.Environment]::GetEnvironmentVariable("Path", "User")
    $env:Path = $machinePath + ";" + $userPath

    $hasGo2 = Get-Command go -ErrorAction SilentlyContinue
    if ($hasGo2) {
        $ver = go version
        Write-Host ($Green + "OK Go " + $ver + " installed" + $Reset)
    } else {
        Write-Host ($Red + "FAIL Go installation failed. Please install manually: https://go.dev/dl/" + $Reset)
        exit 1
    }
}
Write-Host ""

# Step 3: Build the installer and CLI binaries
Write-Host ($Bold + $Blue + "Building SYNAPSE CLI..." + $Reset)

# Ensure binary directory exists
[System.IO.Directory]::CreateDirectory($BinDir) | Out-Null

$originalDir = Get-Location
Set-Location $CliDir

$hasGoSum = Test-Path "go.sum"
if (-not $hasGoSum) {
    go mod tidy
}

# Build to the local bin dir (not TEMP — Windows blocks TEMP executables)
$buildResult = go build -o "$BinDir\synapse.exe" .
if ($LASTEXITCODE -ne 0) {
    Set-Location $originalDir
    Write-Host ($Red + "FAIL Build failed" + $Reset)
    exit 1
}

Set-Location $originalDir
Write-Host ($Green + "OK synapse.exe installed to " + $BinDir + $Reset)

# Step 4: Add to PATH if not already there
$userPath = [System.Environment]::GetEnvironmentVariable("Path", "User")
if ($userPath -notlike "*$BinDir*") {
    $newPath = $userPath + ";" + $BinDir
    [System.Environment]::SetEnvironmentVariable("Path", $newPath, "User")
    $env:Path = $env:Path + ";" + $BinDir
    Write-Host ($Green + "OK Added " + $BinDir + " to your PATH" + $Reset)
    Write-Host ($Yellow + "NOTE: You may need to restart your terminal for PATH changes to take effect." + $Reset)
} else {
    Write-Host ($Green + "OK Already in PATH" + $Reset)
}
Write-Host ""

# Step 5: Run the installer
Write-Host ($Bold + $Blue + "Launching interactive installer..." + $Reset)
Write-Host ""

& "$BinDir\synapse.exe" install

Write-Host ""
Write-Host ($Green + "OK Installation complete!" + $Reset)
Write-Host ($Green + "OK You can now use 'synapse' from any terminal." + $Reset)
Write-Host ($Yellow + "TIP: Run 'synapse --help' to see available commands." + $Reset)
