# Ollama Quick Setup Script for Windows (PowerShell)
# Run this script to download and configure Ollama models for your RAG ChatBot

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Ollama Setup for RAG ChatBot" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

# Step 1: Check if Ollama is installed
Write-Host "Step 1: Checking if Ollama is installed..." -ForegroundColor Yellow
try {
    $version = ollama --version 2>$null
    if ($version) {
        Write-Host "✅ Ollama is installed: $version" -ForegroundColor Green
    } else {
        Write-Host "❌ Ollama is NOT installed" -ForegroundColor Red
        Write-Host "`nPlease install Ollama first:" -ForegroundColor Yellow
        Write-Host "1. Visit https://ollama.ai/download" -ForegroundColor White
        Write-Host "2. Download and install OllamaSetup.exe" -ForegroundColor White
        Write-Host "3. Run this script again`n" -ForegroundColor White
        exit 1
    }
} catch {
    Write-Host "❌ Ollama is NOT installed" -ForegroundColor Red
    Write-Host "`nPlease install Ollama first:" -ForegroundColor Yellow
    Write-Host "1. Visit https://ollama.ai/download" -ForegroundColor White
    Write-Host "2. Download and install OllamaSetup.exe" -ForegroundColor White
    Write-Host "3. Run this script again`n" -ForegroundColor White
    exit 1
}

# Step 2: Check if Ollama service is running
Write-Host "`nStep 2: Checking if Ollama service is running..." -ForegroundColor Yellow
try {
    $response = Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -ErrorAction SilentlyContinue
    Write-Host "✅ Ollama service is running" -ForegroundColor Green
} catch {
    Write-Host "⚠️  Ollama service is not responding" -ForegroundColor Yellow
    Write-Host "   Please start Ollama from the Start menu" -ForegroundColor White
    Write-Host "   Waiting 10 seconds for you to start it..." -ForegroundColor White
    Start-Sleep -Seconds 10
    
    try {
        $response = Invoke-RestMethod -Uri "http://localhost:11434/api/tags" -ErrorAction SilentlyContinue
        Write-Host "✅ Ollama service is now running" -ForegroundColor Green
    } catch {
        Write-Host "❌ Cannot connect to Ollama" -ForegroundColor Red
        Write-Host "   Please start Ollama and run this script again" -ForegroundColor White
        exit 1
    }
}

# Step 3: Check installed models
Write-Host "`nStep 3: Checking installed models..." -ForegroundColor Yellow
$installedModels = ollama list | Select-String -Pattern "llama|nomic" 

Write-Host "`nCurrent models:" -ForegroundColor White
ollama list

# Step 4: Download models
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Downloading AI Models" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Choose chat model to download:" -ForegroundColor Yellow
Write-Host "1. llama3.2:1b  (Lightweight - 1.3GB, best for 4GB RAM)" -ForegroundColor White
Write-Host "2. llama3.2:3b  (Balanced - 2GB, RECOMMENDED)" -ForegroundColor White
Write-Host "3. llama3.1:8b  (Best Quality - 4.7GB, needs 8GB+ RAM)" -ForegroundColor White
Write-Host "4. Skip chat model download`n" -ForegroundColor White

$choice = Read-Host "Enter your choice (1-4)"

switch ($choice) {
    "1" { 
        Write-Host "`nDownloading llama3.2:1b (1.3GB)..." -ForegroundColor Green
        ollama pull llama3.2:1b
        $chatModel = "llama3.2:1b"
    }
    "2" {
        Write-Host "`nDownloading llama3.2:3b (2GB)..." -ForegroundColor Green
        ollama pull llama3.2:3b
        $chatModel = "llama3.2:3b"
    }
    "3" {
        Write-Host "`nDownloading llama3.1:8b (4.7GB)..." -ForegroundColor Green
        ollama pull llama3.1:8b
        $chatModel = "llama3.1:8b"
    }
    "4" {
        Write-Host "Skipping chat model download..." -ForegroundColor Yellow
        $chatModel = "llama3.2:3b"  # Default
    }
    default {
        Write-Host "Invalid choice. Downloading llama3.2:3b (recommended)..." -ForegroundColor Yellow
        ollama pull llama3.2:3b
        $chatModel = "llama3.2:3b"
    }
}

# Download embedding model
Write-Host "`nDownloading embedding model: nomic-embed-text (274MB)..." -ForegroundColor Green
ollama pull nomic-embed-text

# Step 5: Verify downloads
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Verification" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Installed models:" -ForegroundColor Yellow
ollama list

# Step 6: Update application.properties
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Configuration" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

$appPropertiesPath = "src\main\resources\application.properties"
if (Test-Path $appPropertiesPath) {
    Write-Host "Updating application.properties with model: $chatModel" -ForegroundColor Yellow
    
    $content = Get-Content $appPropertiesPath
    $content = $content -replace "spring.ai.ollama.chat.options.model=.*", "spring.ai.ollama.chat.options.model=$chatModel"
    $content | Set-Content $appPropertiesPath
    
    Write-Host "✅ Configuration updated" -ForegroundColor Green
} else {
    Write-Host "⚠️  application.properties not found at $appPropertiesPath" -ForegroundColor Yellow
}

# Step 7: Final summary
Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "  Setup Complete! ✅" -ForegroundColor Cyan
Write-Host "========================================`n" -ForegroundColor Cyan

Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Stop your application if running (Ctrl+C)" -ForegroundColor White
Write-Host "2. Rebuild: mvn clean install" -ForegroundColor White
Write-Host "3. Run: mvn spring-boot:run" -ForegroundColor White
Write-Host "4. Test with: Invoke-RestMethod http://localhost:8081/api/rag/ask?question=test`n" -ForegroundColor White

Write-Host "Configuration:" -ForegroundColor Yellow
Write-Host "- Chat Model: $chatModel" -ForegroundColor White
Write-Host "- Embedding Model: nomic-embed-text" -ForegroundColor White
Write-Host "- Ollama URL: http://localhost:11434" -ForegroundColor White
Write-Host "- Server Port: 8081`n" -ForegroundColor White

Write-Host "For detailed instructions, see OLLAMA_SETUP_GUIDE.md`n" -ForegroundColor Cyan
