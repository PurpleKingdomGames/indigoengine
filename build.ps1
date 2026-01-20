# PowerShell build script for Windows

$ErrorActionPreference = "Stop"

.\mill.bat --no-server -j1 __.compile
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

.\mill.bat --no-server -j1 __.reformat
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

.\mill.bat --no-server -j1 __.fix
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

.\mill.bat --no-server -j1 __.fastLinkJS
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

.\mill.bat --no-server -j1 __.fastLinkJSTest
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

.\mill.bat --no-server -j1 __.test
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

.\mill.bat --no-server -j1 __.publishLocal
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

# Will return when sbt 2.0 supports Scala.js

# SBT Indigo
# Write-Host ">>> SBT-Indigo"
# Set-Location sbt-indigo
# .\build.ps1
# Set-Location ..
