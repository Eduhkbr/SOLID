param(
    [string]$Project = ('aula-verify-' + [guid]::NewGuid().ToString('N').Substring(0,8)),
    [string]$Python = 'python3',
    [switch]$KeepResources,
    [switch]$SkipBuild
)
$ErrorActionPreference = 'Stop'
if (-not (Get-Command $Python -ErrorAction SilentlyContinue)) { throw "Python não encontrado: $Python" }
if ($Project -notmatch '^aula-verify-[a-z0-9-]+$') { throw 'Use prefixo aula-verify- e somente letras minúsculas, números e hífens.' }
$repo = Split-Path $PSScriptRoot -Parent
$evidence = Join-Path $repo ".validation/$Project"
New-Item -ItemType Directory -Force $evidence | Out-Null
$envFile = Join-Path $evidence 'test.env'
Copy-Item (Join-Path $repo '.env.example') $envFile
Add-Content $envFile "`nPAYMENT_IMAGE=aula-payment:$Project`nRECEIPT_IMAGE=aula-receipt:$Project"
$base = @('compose','--env-file',$envFile,'-p',$Project,'-f',(Join-Path $repo 'docker-compose.yml'))
$testArgs = $base + @('-f',(Join-Path $repo 'compose.tests.yml'))
function Invoke-Docker([string[]]$Arguments) {
    & docker @Arguments
    if ($LASTEXITCODE -ne 0) { throw "Docker falhou: $($Arguments[0..1] -join ' ')" }
}
# Nunca adotar um projeto de outra execução, nem sobrescrever seus dados.
$existing = & docker ps -aq --filter "label=com.docker.compose.project=$Project"
if ($LASTEXITCODE -ne 0) { throw 'Docker Engine inacessível. A suíte não pode ser aprovada sem executá-lo.' }
if ($existing) { throw 'Já existem contêineres com esse projeto. Escolha outro identificador.' }
try {
    Invoke-Docker ($base + @('config','--quiet'))
    if (-not $SkipBuild) {
        Invoke-Docker ($base + @('build','payment-api'))
        Invoke-Docker ($base + @('build','receipt-api'))
    }
    Invoke-Docker ($base + @('up','-d','--wait','postgres'))
    # Sequenciais: as suítes usam bancos exclusivos desta execução e limpam fixtures.
    Invoke-Docker ($testArgs + @('run','--rm','payment-tests'))
    Invoke-Docker ($testArgs + @('run','--rm','receipt-tests'))
    Invoke-Docker ($base + @('up','-d','--no-build','--wait','--wait-timeout','180'))
    & $Python (Join-Path $PSScriptRoot 'verify_lab.py') --project $Project --env-file $envFile --evidence-dir $evidence
    if ($LASTEXITCODE -ne 0) { throw 'Regressão funcional reprovada; consulte runtime.json.' }
} finally {
    if (-not $KeepResources) {
        # O nome exclusivo identifica exatamente os volumes descartáveis desta suíte.
        & docker @testArgs down --volumes --remove-orphans
        if (-not $SkipBuild) { & docker image rm "aula-payment:$Project" "aula-receipt:$Project" }
    }
}
Write-Output "Suíte aprovada. Evidências: $evidence"
