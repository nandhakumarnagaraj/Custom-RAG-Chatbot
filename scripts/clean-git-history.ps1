# Clean Git History Script
# This script removes sensitive data from git history

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  Cleaning Git History" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "WARNING: This will rewrite git history!" -ForegroundColor Yellow
Write-Host "This is necessary to remove the OpenAI API key from old commits." -ForegroundColor Yellow
Write-Host ""

$response = Read-Host "Do you want to continue? (yes/no)"
if ($response -ne "yes") {
    Write-Host "Aborted." -ForegroundColor Red
    exit 0
}

Write-Host ""
Write-Host "Step 1: Creating backup..." -ForegroundColor Yellow

# Create backup
$backupDir = "..\rag-backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
Copy-Item -Path "." -Destination $backupDir -Recurse -Force
Write-Host "[OK] Backup created at: $backupDir" -ForegroundColor Green

Write-Host ""
Write-Host "Step 2: Removing .git directory..." -ForegroundColor Yellow
Remove-Item -Path ".git" -Recurse -Force
Write-Host "[OK] Git history removed" -ForegroundColor Green

Write-Host ""
Write-Host "Step 3: Initializing fresh repository..." -ForegroundColor Yellow
git init
git add .
git commit -m "Initial commit - Clean history without secrets"
Write-Host "[OK] Fresh repository created" -ForegroundColor Green

Write-Host ""
Write-Host "Step 4: Adding remote..." -ForegroundColor Yellow
git remote add origin https://github.com/nandhakumarnagaraj/Custom-RAG-Chatbot.git
Write-Host "[OK] Remote added" -ForegroundColor Green

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "  Git History Cleaned!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Next steps:" -ForegroundColor Cyan
Write-Host "1. Run: git push -f origin main" -ForegroundColor White
Write-Host "   (This will force push the clean history)" -ForegroundColor Yellow
Write-Host ""
Write-Host "2. Backup is saved at: $backupDir" -ForegroundColor White
Write-Host ""
Write-Host "Press any key to exit..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
