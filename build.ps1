# Guideon 프로젝트 빌드 스크립트 (PowerShell)
# Java 17을 사용하여 Maven 빌드 실행

# UTF-8 콘솔 인코딩 설정
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 > $null

Write-Host "=== Guideon 프로젝트 빌드 ===" -ForegroundColor Green
Write-Host ""

# Java 17 경로 설정
$JAVA17_HOME = "C:\Program Files\Java\jdk-17"

Write-Host "현재 JAVA_HOME 확인..." -ForegroundColor Yellow
Write-Host "시스템 JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Cyan

# Java 17 존재 확인
if (Test-Path $JAVA17_HOME) {
    Write-Host "✓ Java 17 발견: $JAVA17_HOME" -ForegroundColor Green

    # 현재 세션에 Java 17 설정
    $env:JAVA_HOME = $JAVA17_HOME
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

    Write-Host "✓ 현재 세션에 Java 17 설정 완료" -ForegroundColor Green
    Write-Host ""

    # Maven 버전 확인
    Write-Host "Maven 버전 확인:" -ForegroundColor Yellow
    mvn --version
    Write-Host ""

    # 빌드 시작
    Write-Host "빌드 시작..." -ForegroundColor Yellow
    Write-Host ""

    # 빌드 실행
    mvn clean package -DskipTests

    $buildResult = $LASTEXITCODE

    Write-Host ""
    if ($buildResult -eq 0) {
        Write-Host "=== 빌드 성공! ===" -ForegroundColor Green
        Write-Host ""
        Write-Host "생성된 파일: target\regulation-search-1.0.0.jar" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "실행 방법:" -ForegroundColor Yellow
        Write-Host "  java -jar target\regulation-search-1.0.0.jar" -ForegroundColor White
        Write-Host "  또는" -ForegroundColor White
        Write-Host "  mvn spring-boot:run" -ForegroundColor White
    } else {
        Write-Host "=== 빌드 실패 ===" -ForegroundColor Red
        Write-Host "위의 오류 메시지를 확인하세요." -ForegroundColor Red
        exit $buildResult
    }
} else {
    Write-Host "✗ 오류: Java 17을 찾을 수 없습니다: $JAVA17_HOME" -ForegroundColor Red
    Write-Host ""
    Write-Host "설치된 Java 버전들:" -ForegroundColor Yellow
    Get-ChildItem "C:\Program Files\Java" -ErrorAction SilentlyContinue | Select-Object Name
    Write-Host ""
    Write-Host "Java 17을 설치하거나 JAVA17_HOME 변수를 수정하세요." -ForegroundColor Red
    exit 1
}
