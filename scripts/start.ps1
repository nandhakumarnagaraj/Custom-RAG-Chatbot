# RAG ChatBot - Quick Start Script
# This script helps you start all required services

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  RAG ChatBot - Quick Start" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Function to check if a command exists
function Test-Command {
    param($Command)
    try {
        if (Get-Command $Command -ErrorAction Stop) {
            return $true
        }
    }
    catch {
        return $false
    }
}

# Function to check if a port is in use
function Test-Port {
    param($Port)
    $connection = Test-NetConnection -ComputerName localhost -Port $Port -WarningAction SilentlyContinue
    return $connection.TcpTestSucceeded
}

# Check prerequisites
Write-Host "Checking prerequisites..." -ForegroundColor Yellow
Write-Host ""

$allGood = $true

# Check Java
if (Test-Command "java") {
    $javaVersion = java -version 2>&1 | Select-String "version" | Select-Object -First 1
    Write-Host "[OK] Java is installed: $javaVersion" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Java is not installed!" -ForegroundColor Red
    Write-Host "  Download from: https://adoptium.net/" -ForegroundColor Yellow
    $allGood = $false
}

# Check Maven
if (Test-Command "mvn") {
    $mvnVersion = mvn -version | Select-String "Apache Maven" | Select-Object -First 1
    Write-Host "[OK] Maven is installed: $mvnVersion" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Maven is not installed!" -ForegroundColor Red
    Write-Host "  Download from: https://maven.apache.org/download.cgi" -ForegroundColor Yellow
    $allGood = $false
}

# Check Python
if (Test-Command "python") {
    $pythonVersion = python --version
    Write-Host "[OK] Python is installed: $pythonVersion" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Python is not installed!" -ForegroundColor Red
    Write-Host "  Download from: https://www.python.org/downloads/" -ForegroundColor Yellow
    $allGood = $false
}

# Check Ollama
if (Test-Command "ollama") {
    Write-Host "[OK] Ollama is installed" -ForegroundColor Green
    
    # Check if models are installed
    $models = ollama list
    if ($models -match "llama3.2:3b" -and $models -match "nomic-embed-text") {
        Write-Host "[OK] Required Ollama models are installed" -ForegroundColor Green
    } else {
        Write-Host "[WARNING] Required Ollama models not found!" -ForegroundColor Yellow
        Write-Host "  Run: ollama pull llama3.2:3b" -ForegroundColor Yellow
        Write-Host "  Run: ollama pull nomic-embed-text" -ForegroundColor Yellow
        $allGood = $false
    }
} else {
    Write-Host "[ERROR] Ollama is not installed!" -ForegroundColor Red
    Write-Host "  Download from: https://ollama.ai/download" -ForegroundColor Yellow
    $allGood = $false
}

# Check ChromaDB
if (Test-Command "chroma") {
    Write-Host "[OK] ChromaDB is installed" -ForegroundColor Green
} else {
    Write-Host "[WARNING] ChromaDB is not installed!" -ForegroundColor Yellow
    Write-Host "  Run: pip install 'chromadb>=0.4.0,<1.0.0'" -ForegroundColor Yellow
    Write-Host "  Run: pip install 'numpy<2.0'" -ForegroundColor Yellow
}

Write-Host ""

if (-not $allGood) {
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  Please install missing prerequisites" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Press any key to exit..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

# Check if JAR file exists
$jarFile = "target\rag-0.0.1-SNAPSHOT.jar"
if (-not (Test-Path $jarFile)) {
    Write-Host "JAR file not found. Building the project..." -ForegroundColor Yellow
    Write-Host ""
    
    mvn clean package -DskipTests
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host ""
        Write-Host "[ERROR] Build failed!" -ForegroundColor Red
        Write-Host "Press any key to exit..."
        $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        exit 1
    }
    
    Write-Host ""
    Write-Host "[OK] Build successful!" -ForegroundColor Green
    Write-Host ""
}

# Check if ports are available
Write-Host "Checking ports..." -ForegroundColor Yellow

if (Test-Port 8000) {
    Write-Host "[WARNING] Port 8000 is already in use (ChromaDB)" -ForegroundColor Yellow
    $response = Read-Host "Do you want to continue anyway? (y/n)"
    if ($response -ne "y") {
        exit 0
    }
}

if (Test-Port 8080) {
    Write-Host "[ERROR] Port 8080 is already in use (Spring Boot)" -ForegroundColor Red
    Write-Host "  Please stop the application using port 8080" -ForegroundColor Yellow
    Write-Host "Press any key to exit..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Starting Services" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Start ChromaDB in a new window
Write-Host "Starting ChromaDB..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "chroma run --host localhost --port 8000"
Start-Sleep -Seconds 3

# Wait for ChromaDB to be ready
$chromaReady = $false
$attempts = 0
while (-not $chromaReady -and $attempts -lt 10) {
    if (Test-Port 8000) {
        $chromaReady = $true
        Write-Host "[OK] ChromaDB is running on port 8000" -ForegroundColor Green
    } else {
        Write-Host "  Waiting for ChromaDB to start..." -ForegroundColor Yellow
        Start-Sleep -Seconds 2
        $attempts++
    }
}

if (-not $chromaReady) {
    Write-Host "[ERROR] ChromaDB failed to start!" -ForegroundColor Red
    Write-Host "Press any key to exit..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

Write-Host ""

# Start Spring Boot application in a new window
Write-Host "Starting Spring Boot application..." -ForegroundColor Yellow
Start-Process powershell -ArgumentList "-NoExit", "-Command", "java -jar $jarFile"
Start-Sleep -Seconds 5

# Wait for Spring Boot to be ready
$springReady = $false
$attempts = 0
while (-not $springReady -and $attempts -lt 30) {
    if (Test-Port 8080) {
        $springReady = $true
        Write-Host "[OK] Spring Boot application is running on port 8080" -ForegroundColor Green
    } else {
        Write-Host "  Waiting for Spring Boot to start..." -ForegroundColor Yellow
        Start-Sleep -Seconds 2
        $attempts++
    }
}

if (-not $springReady) {
    Write-Host "[ERROR] Spring Boot application failed to start!" -ForegroundColor Red
    Write-Host "  Check the application window for error messages" -ForegroundColor Yellow
    Write-Host "Press any key to exit..."
    $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  All Services Started Successfully!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "API is available at: http://localhost:8080/api/rag" -ForegroundColor Cyan
Write-Host ""
Write-Host "Test the API:" -ForegroundColor Yellow
Write-Host "  Upload: curl -X POST http://localhost:8080/api/rag/upload -F 'file=@sample-data\test.txt'" -ForegroundColor White
Write-Host "  Ask: curl 'http://localhost:8080/api/rag/ask?question=What%20is%20this%20about?'" -ForegroundColor White
Write-Host ""
Write-Host "To stop the services, close the PowerShell windows." -ForegroundColor Yellow
Write-Host ""
Write-Host "Press any key to exit this window..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
