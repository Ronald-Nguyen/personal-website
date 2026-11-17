# Helper for running the website on the Windows laptop, also over SSH.
# Usage (inside C:\Users\ronal\personal-website):
#   .\site.ps1 up       build and start (default)
#   .\site.ps1 down     stop
#   .\site.ps1 restart  restart without rebuild
#   .\site.ps1 logs     follow logs
#   .\site.ps1 ps       container status
param([string]$cmd = "up")

# The Windows credential store (desktop/wincred) is unavailable in SSH sessions.
# Use a separate config whose credential helper reports "not found", so public
# images can be pulled anonymously.
$sshDockerConfig = Join-Path $env:USERPROFILE ".docker-ssh"
$helper = Join-Path $sshDockerConfig "bin\docker-credential-none.cmd"
if (-not (Test-Path $helper)) {
    $enc = New-Object Text.UTF8Encoding $false
    New-Item -ItemType Directory -Force (Split-Path $helper) | Out-Null
    $body = "@echo off`r`nif /I `"%1`"==`"get`" (`r`n  more >nul`r`n  echo credentials not found in native keychain`r`n  exit /b 1`r`n)`r`nif /I `"%1`"==`"list`" (`r`n  echo {}`r`n  exit /b 0`r`n)`r`nmore >nul`r`nexit /b 0`r`n"
    [IO.File]::WriteAllText($helper, $body, $enc)
    [IO.File]::WriteAllText("$sshDockerConfig\config.json", '{"auths":{},"credsStore":"none"}', $enc)
}
$env:DOCKER_CONFIG = $sshDockerConfig
$env:PATH = "$(Split-Path $helper);$env:PATH"

Set-Location $PSScriptRoot

docker version --format "{{.Server.Version}}" *> $null
if ($LASTEXITCODE -ne 0) {
    Write-Host "Docker Desktop laeuft nicht. Starte es ueber die Aufgabe 'DockerDesktopStart' ..."
    Start-ScheduledTask -TaskName DockerDesktopStart -ErrorAction SilentlyContinue
    for ($i = 0; $i -lt 40; $i++) {
        Start-Sleep 5
        docker version --format "{{.Server.Version}}" *> $null
        if ($LASTEXITCODE -eq 0) { break }
    }
}

switch ($cmd) {
    "up"      { docker compose up --build -d; docker compose ps }
    "down"    { docker compose down }
    "restart" { docker compose restart; docker compose ps }
    "logs"    { docker compose logs -f --tail 100 }
    "ps"      { docker compose ps }
    default   { Write-Host "Unbekannt: $cmd (up|down|restart|logs|ps)" }
}
