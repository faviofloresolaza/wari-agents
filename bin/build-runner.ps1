[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$agentsRoot = Split-Path -Parent $PSScriptRoot
$workspaceRoot = Split-Path -Parent $agentsRoot
$configPath = Join-Path $workspaceRoot '.wari\environment.local.yaml'
if (-not (Test-Path -LiteralPath $configPath -PathType Leaf)) {
    Write-Error "BLOCKED_ENVIRONMENT: falta $configPath"
    exit 2
}
$config = Get-Content -Raw -Encoding utf8 -LiteralPath $configPath
$section = [regex]::Match($config, '(?ms)^\s{2}runner_java:\s*\r?\n(?<body>(?:\s{4}.*\r?\n?)*)')
if (-not $section.Success) { Write-Error 'BLOCKED_ENVIRONMENT: falta tools.runner_java'; exit 2 }
$javaHomeMatch = [regex]::Match($section.Groups['body'].Value, '(?m)^\s{4}java_home:\s*"(?<value>[^"]+)"')
if (-not $javaHomeMatch.Success) { Write-Error 'BLOCKED_ENVIRONMENT: falta runner_java.java_home'; exit 2 }
$javaHome = $javaHomeMatch.Groups['value'].Value
$javac = Join-Path $javaHome 'bin\javac.exe'
$jar = Join-Path $javaHome 'bin\jar.exe'
if (-not (Test-Path -LiteralPath $javac -PathType Leaf) -or -not (Test-Path -LiteralPath $jar -PathType Leaf)) {
    Write-Error "BLOCKED_ENVIRONMENT: JDK Runner incompleto en $javaHome"
    exit 2
}

$classes = Join-Path $agentsRoot 'runner\build\classes'
New-Item -ItemType Directory -Force -Path $classes | Out-Null
$sources = @(
    (Join-Path $agentsRoot 'runner\src\wari\agents\Json.java'),
    (Join-Path $agentsRoot 'runner\src\wari\agents\Runner.java'),
    (Join-Path $agentsRoot 'runner\src\wari\agents\provider\ProviderAdapter.java'),
    (Join-Path $agentsRoot 'runner\src\wari\agents\provider\CodexAdapter.java'),
    (Join-Path $agentsRoot 'runner\src\wari\agents\provider\ProviderAdapters.java')
)
& $javac '--release' '21' '-d' $classes @sources
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
$output = Join-Path $agentsRoot 'runner\wari-agents-runner.jar'
& $jar '--create' '--file' $output '--date=2000-01-01T00:00:00Z' '--main-class' 'wari.agents.Runner' '-C' $classes '.'
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "Runner compilado: $output"
