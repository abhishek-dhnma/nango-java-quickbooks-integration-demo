param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$MavenArgs = @("spring-boot:run")
)

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path

# 1. Load .env file if present
$envFile = Join-Path $scriptDir ".env"
if (Test-Path $envFile) {
    Write-Host "[Nango] Loading environment variables from .env..." -ForegroundColor Cyan
    Get-Content $envFile | ForEach-Object {
        $line = $_.Trim()
        if ($line -and -not $line.StartsWith("#") -and $line.Contains("=")) {
            $parts = $line.Split("=", 2)
            $name = $parts[0].Trim()
            $value = $parts[1].Trim()
            [System.Environment]::SetEnvironmentVariable($name, $value, [System.EnvironmentVariableTarget]::Process)
        }
    }
} else {
    Write-Host "[Nango] No .env file found. Using existing shell environment." -ForegroundColor Yellow
}

# 2. Ensure JAVA_HOME is configured
if (-not $env:JAVA_HOME) {
    $knownJdks = @(
        "C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot",
        "C:\Program Files\Java\jdk-17"
    )
    foreach ($jdk in $knownJdks) {
        if (Test-Path $jdk) {
            $env:JAVA_HOME = $jdk
            break
        }
    }
}

# 3. Locate Maven
$mvnCmd = "mvn"
if (-not (Get-Command "mvn" -ErrorAction SilentlyContinue)) {
    $wrapperMvn = "C:\Users\abhishek dhiman\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd"
    if (Test-Path $wrapperMvn) {
        $mvnCmd = $wrapperMvn
    }
}

# 4. Check if port 8080 is occupied and free it automatically
if ($MavenArgs -contains "spring-boot:run" -or $MavenArgs.Count -eq 0) {
    $existing = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | Select-Object -ExpandProperty OwningProcess -Unique
    if ($existing) {
        Write-Host "[Nango] Port 8080 is occupied by PID $existing. Freeing port..." -ForegroundColor Yellow
        Stop-Process -Id $existing -Force -ErrorAction SilentlyContinue
        Start-Sleep -Seconds 1
    }
}

# 5. Run Maven target
Write-Host "[Nango] Executing: $mvnCmd $($MavenArgs -join ' ')" -ForegroundColor Green
& $mvnCmd $MavenArgs
