[CmdletBinding()]
param(
    [string]$WorkspaceRoot
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($WorkspaceRoot)) {
    $WorkspaceRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
}
$agentsRoot = Split-Path -Parent $PSScriptRoot
$workspaceAgents = Join-Path $WorkspaceRoot 'AGENTS.md'
$localDirectory = Join-Path $WorkspaceRoot '.wari'
$localConfig = Join-Path $localDirectory 'environment.local.yaml'
$codexDirectory = Join-Path $WorkspaceRoot '.codex'
$codexSkills = Join-Path $codexDirectory 'skills'

if (-not (Test-Path -LiteralPath $workspaceAgents)) {
    Copy-Item -LiteralPath (Join-Path $agentsRoot 'templates\workspace\AGENTS.md') -Destination $workspaceAgents
    Write-Host "CREATED $workspaceAgents"
} else {
    Write-Host "PRESERVED $workspaceAgents"
}

if (-not (Test-Path -LiteralPath $localDirectory)) {
    New-Item -ItemType Directory -Path $localDirectory | Out-Null
}
if (-not (Test-Path -LiteralPath $localConfig)) {
    Copy-Item -LiteralPath (Join-Path $agentsRoot 'config\environment.template.yaml') -Destination $localConfig
    Write-Host "CREATED $localConfig; complete los placeholders antes de trabajar."
} else {
    Write-Host "PRESERVED $localConfig"
}

if (-not (Test-Path -LiteralPath $codexSkills)) {
    New-Item -ItemType Directory -Path $codexSkills -Force | Out-Null
}

$managedSkills = Get-ChildItem -LiteralPath (Join-Path $agentsRoot 'skills') -Directory |
    Where-Object { $_.Name -like 'wari-*' -and (Test-Path -LiteralPath (Join-Path $_.FullName 'SKILL.md') -PathType Leaf) }
foreach ($skill in $managedSkills) {
    $destination = Join-Path $codexSkills $skill.Name
    if (-not (Test-Path -LiteralPath $destination)) {
        New-Item -ItemType Directory -Path $destination | Out-Null
    }
    $sourceManifest = Join-Path $skill.FullName 'SKILL.md'
    $sourceText = Get-Content -Raw -Encoding utf8 -LiteralPath $sourceManifest
    if ($sourceText -notmatch '(?s)\A---\s*\r?\n.*?\r?\n---') {
        throw "SKILL.md sin frontmatter válido: $sourceManifest"
    }
    $frontmatter = $Matches[0]
    $sourceRelative = "../../../wari-agents/skills/$($skill.Name)/SKILL.md"
    $entrypoint = @"
$frontmatter

# Entrypoint WARI administrado

Lee completamente [$($skill.Name)]($sourceRelative) y sigue sus instrucciones.
El archivo enlazado en `wari-agents` es la unica fuente de verdad. No copies ni
modifiques esas instrucciones desde este entrypoint generado.
"@
    $utf8WithoutBom = New-Object System.Text.UTF8Encoding($false)
    [System.IO.File]::WriteAllText((Join-Path $destination 'SKILL.md'), $entrypoint, $utf8WithoutBom)
    Write-Host "SYNCED Codex skill $($skill.Name)"
}

Write-Host 'PASS instalación idempotente del workspace. Se sincronizaron skills; no se crearon tareas, ramas, worktrees ni sesiones.'
