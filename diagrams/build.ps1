$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

Set-Location (Join-Path $ScriptDir "..")
$millOutput = (.\mill.bat --no-server --disable-ticker visualize __.compile | Out-String | ConvertFrom-Json)
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
$output = $millOutput | Where-Object { $_ -like "*out.dot*" }

Set-Location $ScriptDir

Write-Host $output

scala-cli run simplify-deps.sc -- $output > indigoengine.dot

npm install --silent
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

node dot-to-png.mjs
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
