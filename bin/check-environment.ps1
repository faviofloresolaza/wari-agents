[CmdletBinding()]
param(
    [string]$WorkspaceRoot
)

$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($WorkspaceRoot)) {
    $WorkspaceRoot = Split-Path -Parent (Split-Path -Parent $PSScriptRoot)
}
$config = Join-Path $WorkspaceRoot '.wari\environment.local.yaml'
$errors = [System.Collections.Generic.List[string]]::new()
$warnings = [System.Collections.Generic.List[string]]::new()

if (-not (Test-Path -LiteralPath $config -PathType Leaf)) {
    Write-Error "BLOCKED_ENVIRONMENT: falta $config"
    exit 2
}

$text = Get-Content -Raw -Encoding utf8 -LiteralPath $config
if ($text -match '<[^>]+>') {
    $errors.Add('La configuración local contiene placeholders sin completar.')
}
if ($text -match '(?im)^\s*(password|token|secret|api_key)\s*:') {
    $errors.Add('No se permiten secretos ni credenciales en environment.local.yaml.')
}

function Assert-RequiredQuotedFields {
    param(
        [string]$SectionName,
        [string[]]$FieldNames
    )
    $escapedSection = [regex]::Escape($SectionName)
    $sectionMatch = [regex]::Match($text, "(?ms)^\s{2}${escapedSection}:\s*\r?\n(?<body>(?:\s{4}.*\r?\n?)*)")
    if (-not $sectionMatch.Success) {
        $errors.Add("Falta la sección requerida: $SectionName")
        return
    }
    $body = $sectionMatch.Groups['body'].Value
    foreach ($fieldName in $FieldNames) {
        $escapedField = [regex]::Escape($fieldName)
        $fieldPattern = '(?m)^\s{4}' + $escapedField + ':\s*"[^"]+"\s*$'
        if ($body -notmatch $fieldPattern) {
            $errors.Add("Falta un valor declarado para ${SectionName}.${fieldName}")
        }
    }
}

Assert-RequiredQuotedFields 'git' @('executable', 'detected_version')
Assert-RequiredQuotedFields 'runner_java' @('java_home', 'executable', 'javac_executable', 'jar_executable', 'detected_version')
Assert-RequiredQuotedFields 'application_jdk' @('java_home', 'detected_version')
Assert-RequiredQuotedFields 'maven' @('executable', 'detected_version', 'settings_xml')
Assert-RequiredQuotedFields 'compile' @('cwd')

if ($text -notmatch '(?m)^\s{4}project_compatibility_validated:\s*(true|false)\s*$') {
    $errors.Add('Falta la propiedad application_jdk.project_compatibility_validated o su nombre no es valido.')
}

if ($text -notmatch '(?m)^\s{2}operational_scope:\s*"context_only"\s*$') {
    $errors.Add('application_context.operational_scope debe ser "context_only".')
}

if ($text -notmatch '(?m)^\s{4}base_branch:\s*"produccion"\s*$') {
    $errors.Add('repositories.application.base_branch debe ser "produccion".')
}

if ($text -notmatch '(?m)^\s{2}allowed_project_keys:\s*\["MEWARI",\s*"DWARI"\]\s*$') {
    $errors.Add('work_tracking.allowed_project_keys debe limitarse a MEWARI y DWARI.')
}

if ($text -notmatch '(?m)^\s{2}server_url:\s*"https://[^"\s]+"\s*$') {
    $errors.Add('source_control.server_url debe declarar una URL HTTPS de GitHub Enterprise.')
}

if ($text -notmatch '(?m)^\s{2}allow_direct_production_write:\s*false\s*$') {
    $errors.Add('restrictions.allow_direct_production_write debe permanecer en false.')
}

if ($text -notmatch '(?m)^\s{2}secrets_in_files:\s*false\s*$') {
    $errors.Add('restrictions.secrets_in_files debe permanecer en false.')
}

$forbiddenCommandKeys = [regex]::Matches($text, '(?m)^\s{2}(test|package|websphere_start|websphere_stop|deploy_local):\s*$')
foreach ($match in $forbiddenCommandKeys) {
    $errors.Add("Comando fuera de alcance: $($match.Groups[1].Value)")
}

if ($text -match '(?m)^\s{2}websphere:\s*$') {
    $errors.Add('WebSphere no debe configurarse como herramienta operativa; solo pertenece a application_context.')
}

$compileConfigured = $text -match '(?ms)^\s{2}compile:\s*\r?\n(?:(?:\s{4}.*)\r?\n?)*?\s{4}configured:\s*true\s*$'
$compileCommand = [regex]::Match($text, '(?m)^\s{4}command:\s*[''\"](?<command>.+)[''\"]\s*$')
if ($compileConfigured) {
    if (-not $compileCommand.Success -or $compileCommand.Groups['command'].Value -notmatch '(?i)(^|\s)clean\s+compile(\s|$)') {
        $errors.Add('El comando compile configurado debe ejecutar Maven clean compile.')
    }
}

# Una ruta como C:Program Files es relativa a la unidad, no una ruta absoluta.
$driveRelativePaths = [regex]::Matches($text, '"([A-Za-z]:[^\\/"\r\n][^"\r\n]*)"')
foreach ($match in $driveRelativePaths) {
    $errors.Add("Ruta de Windows no absoluta: $($match.Groups[1].Value)")
}

$pathMatches = [regex]::Matches($text, '"([A-Za-z]:\\[^"\r\n]+)"')
foreach ($match in $pathMatches) {
    $path = $match.Groups[1].Value
    if (-not (Test-Path -LiteralPath $path)) {
        $errors.Add("Ruta declarada inexistente: $path")
    }
}

$section = ''
foreach ($line in ($text -split "`r?`n")) {
    if ($line -match '^\s{2}([a-z_]+):\s*$') { $section = $Matches[1]; continue }
    if ($line -match '^\s{4}configured:\s*false\s*$') {
        if ($section -eq 'compile') {
            $warnings.Add("Comando no configurado: $section")
        }
    }
    if ($section -eq 'application_jdk' -and $line -match '^\s{4}project_compatibility_validated:\s*false\s*$') {
        $warnings.Add('La compatibilidad del JDK de aplicación aún no está validada.')
    }
}

foreach ($warning in $warnings) { Write-Host "WARN $warning" }
if ($errors.Count -gt 0) {
    foreach ($item in $errors) { Write-Host "BLOCKED_ENVIRONMENT $item" }
    exit 2
}

Write-Host "PASS environment.local.yaml: rutas declaradas disponibles; $($warnings.Count) advertencia(s)."
