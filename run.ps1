# Guideon 애플리케이션 실행 스크립트 (PowerShell)

# UTF-8 콘솔 인코딩 설정
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 > $null

Write-Host "=== Guideon 애플리케이션 실행 ===" -ForegroundColor Green
Write-Host ""

# Java 17 경로 설정
$JAVA17_HOME = "C:\Program Files\Java\jdk-17"

# Java 17 존재 확인
if (Test-Path $JAVA17_HOME) {
    # 현재 세션에 Java 17 설정
    $env:JAVA_HOME = $JAVA17_HOME
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

    Write-Host "✓ Java 17 설정 완료" -ForegroundColor Green
    Write-Host ""

    # JAR 파일 확인
    $jarFile = "target\regulation-search-1.0.0.jar"

    if (Test-Path $jarFile) {
        Write-Host "애플리케이션 시작 중..." -ForegroundColor Yellow
        Write-Host ""

        # 애플리케이션 실행 (UTF-8 인코딩 설정)
        java '-Dfile.encoding=UTF-8' '-Dconsole.encoding=UTF-8' '-Djansi.passthrough=true' -jar $jarFile
    } else {
        Write-Host "✗ 오류: JAR 파일을 찾을 수 없습니다: $jarFile" -ForegroundColor Red
        Write-Host ""
        Write-Host "먼저 빌드를 실행하세요:" -ForegroundColor Yellow
        Write-Host "  .\build.ps1" -ForegroundColor White
        exit 1
    }
} else {
    Write-Host "✗ 오류: Java 17을 찾을 수 없습니다: $JAVA17_HOME" -ForegroundColor Red
    Write-Host ""
    Write-Host "Java 17을 설치하거나 JAVA17_HOME 변수를 수정하세요." -ForegroundColor Red
    exit 1
}
