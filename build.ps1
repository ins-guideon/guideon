# Guideon Build Script - Phase 4.2
# UTF-8 Encoding Setup
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 > $null

Write-Host "=== Guideon Build ===" -ForegroundColor Green
Write-Host "Phase 4.2: Korean Analyzer Advanced Features" -ForegroundColor Cyan
Write-Host ""

# Java 17 Path
$JAVA17_HOME = "C:\Program Files\Java\jdk-17"

Write-Host "Checking JAVA_HOME..." -ForegroundColor Yellow
Write-Host "System JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Cyan

# Check Java 17 exists
if (Test-Path $JAVA17_HOME) {
    Write-Host "Java 17 found: $JAVA17_HOME" -ForegroundColor Green

    # Set Java 17 for current session
    $env:JAVA_HOME = $JAVA17_HOME
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

    Write-Host "Java 17 configured for current session" -ForegroundColor Green
    Write-Host ""

    # Maven version check
    Write-Host "Maven version:" -ForegroundColor Yellow
    mvn --version
    Write-Host ""

    # Start build
    Write-Host "Starting build..." -ForegroundColor Yellow
    Write-Host ""

    # Execute build
    mvn clean package -DskipTests

    $buildResult = $LASTEXITCODE

    Write-Host ""
    if ($buildResult -eq 0) {
        Write-Host "=== Build Success! ===" -ForegroundColor Green
        Write-Host ""
        Write-Host "Generated file: target\regulation-search-1.0.0.jar" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Phase 4.2 New Features:" -ForegroundColor Yellow
        Write-Host "  - Synonym Dictionary (150+ synonym groups)" -ForegroundColor Green
        Write-Host "  - Extended User Dictionary (170+ compound nouns)" -ForegroundColor Green
        Write-Host "  - Synonym Expansion in Analyzers" -ForegroundColor Green
        Write-Host ""
        Write-Host "How to run:" -ForegroundColor Yellow
        Write-Host "  java -jar target\regulation-search-1.0.0.jar" -ForegroundColor White
        Write-Host "  or" -ForegroundColor White
        Write-Host "  mvn spring-boot:run" -ForegroundColor White
        Write-Host ""
        Write-Host "Note: Rebuild BM25 index by re-uploading documents" -ForegroundColor Cyan
        Write-Host "      or run ReindexBM25Tool" -ForegroundColor Cyan
    } else {
        Write-Host "=== Build Failed ===" -ForegroundColor Red
        Write-Host "Check error messages above" -ForegroundColor Red
        exit $buildResult
    }
} else {
    Write-Host "Error: Java 17 not found at: $JAVA17_HOME" -ForegroundColor Red
    Write-Host ""
    Write-Host "Installed Java versions:" -ForegroundColor Yellow
    Get-ChildItem "C:\Program Files\Java" -ErrorAction SilentlyContinue | Select-Object Name
    Write-Host ""
    Write-Host "Please install Java 17 or modify JAVA17_HOME variable" -ForegroundColor Red
    exit 1
}
