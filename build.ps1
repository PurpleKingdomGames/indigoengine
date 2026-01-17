# PowerShell build script for Windows

$ErrorActionPreference = "Stop"

.\mill.bat --no-server __.compile
.\mill.bat --no-server __.reformat
.\mill.bat --no-server -j2 __.fix
.\mill.bat --no-server -j2 __.fastLinkJS
.\mill.bat --no-server -j2 __.fastLinkJSTest
.\mill.bat --no-server __.test
.\mill.bat --no-server __.publishLocal

# Will return when sbt 2.0 supports Scala.js

# SBT Indigo
# Write-Host ">>> SBT-Indigo"
# Set-Location sbt-indigo
# .\build.ps1
# Set-Location ..
