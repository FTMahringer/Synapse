param(
    [switch]$NonInteractive
)

$ErrorActionPreference = "Stop"
$RootDir = Resolve-Path (Join-Path $PSScriptRoot "..")
$ComposeDir = Join-Path $RootDir "installer\compose"

function Ask-Value {
    param(
        [string]$Prompt,
        [string]$Default
    )

    if ($NonInteractive) {
        return $Default
    }

    $value = Read-Host "$Prompt [$Default]"
    if ([string]::IsNullOrWhiteSpace($value)) {
        return $Default
    }
    return $value
}

function Require-Command {
    param([string]$Name)

    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Missing required command: $Name"
    }
}

function Write-EnvFile {
    param(
        [string]$SystemName,
        [string]$InstallMode,
        [string]$PublicDomain,
        [string]$PrimaryModelProvider,
        [string]$EchoEnabled,
        [string]$GitProvider,
        [string]$PostgresPassword,
        [string]$JwtSecret,
        [string]$SecretsEncryptionKey
    )

    $envFile = Join-Path $RootDir ".env"
    $content = @(
        "SYSTEM_NAME=$SystemName"
        "INSTALL_MODE=$InstallMode"
        "PUBLIC_DOMAIN=$PublicDomain"
        "PRIMARY_MODEL_PROVIDER=$PrimaryModelProvider"
        "ECHO_ENABLED=$EchoEnabled"
        "GIT_PROVIDER=$GitProvider"
        "DASHBOARD_DEFAULT_USERNAME=admin"
        "DASHBOARD_DEFAULT_PASSWORD=admin"
        "POSTGRES_DB=synapse"
        "POSTGRES_USER=synapse"
        "POSTGRES_PASSWORD=$PostgresPassword"
        "JWT_SECRET=$JwtSecret"
        "SECRETS_ENCRYPTION_KEY=$SecretsEncryptionKey"
        "REDIS_URL=redis://redis:6379"
        "QDRANT_URL=http://qdrant:6333"
        "GRAFANA_ADMIN_USER=admin"
        "GRAFANA_ADMIN_PASSWORD=admin"
    )

    Set-Content -Path $envFile -Value $content -Encoding UTF8
    Write-Host "Wrote $envFile"
}

Write-Host "SYNAPSE installer"
Require-Command docker

$systemName = Ask-Value "System name" "SYNAPSE"
$installMode = Ask-Value "Install mode: quick, dev, or production" "quick"
$publicDomain = Ask-Value "Public domain or localhost" "localhost"
$primaryModelProvider = Ask-Value "Primary model provider" "anthropic"
$echoEnabled = Ask-Value "Enable ECHO debug agent: true or false" "true"
$gitProvider = Ask-Value "Git provider: none, github, gitlab, forgejo, or gitea" "none"
$postgresPassword = Ask-Value "Postgres password" "synapse_dev_password"
$jwtSecret = Ask-Value "JWT secret" "CHANGE_ME_IN_PRODUCTION_THIS_MUST_BE_AT_LEAST_256_BITS_LONG_FOR_HS256"
$secretsEncryptionKey = Ask-Value "Secrets encryption key" "dev_key_32_bytes_change_me_now!!"

if ($installMode -notin @("quick", "dev", "production")) {
    throw "Invalid install mode: $installMode"
}

Write-EnvFile `
    -SystemName $systemName `
    -InstallMode $installMode `
    -PublicDomain $publicDomain `
    -PrimaryModelProvider $primaryModelProvider `
    -EchoEnabled $echoEnabled `
    -GitProvider $gitProvider `
    -PostgresPassword $postgresPassword `
    -JwtSecret $jwtSecret `
    -SecretsEncryptionKey $secretsEncryptionKey

$composeFile = Join-Path $ComposeDir "docker-compose.yml"
if ($installMode -eq "production") {
    $composeFile = Join-Path $ComposeDir "docker-compose.prod.yml"
}

docker compose -f $composeFile --env-file (Join-Path $RootDir ".env") up -d

Write-Host "Install complete."
Write-Host "Dashboard: http://$publicDomain`:3000"
Write-Host "Backend: http://$publicDomain`:8080"
