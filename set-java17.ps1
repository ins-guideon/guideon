# Java 17로 JAVA_HOME 환경 변수 설정 스크립트
# 관리자 권한으로 실행해야 합니다

$javaPath = "C:\Program Files\Java\jdk-17"

Write-Host "현재 JAVA_HOME 설정 확인 중..." -ForegroundColor Yellow
$currentJavaHome = [System.Environment]::GetEnvironmentVariable('JAVA_HOME','Machine')
Write-Host "현재 시스템 JAVA_HOME: $currentJavaHome" -ForegroundColor Cyan

Write-Host "`nJava 17 경로 확인 중..." -ForegroundColor Yellow
if (Test-Path $javaPath) {
    Write-Host "Java 17 발견: $javaPath" -ForegroundColor Green

    Write-Host "`n시스템 환경 변수 JAVA_HOME을 Java 17로 설정 중..." -ForegroundColor Yellow
    [System.Environment]::SetEnvironmentVariable('JAVA_HOME', $javaPath, 'Machine')

    Write-Host "사용자 환경 변수 JAVA_HOME도 Java 17로 설정 중..." -ForegroundColor Yellow
    [System.Environment]::SetEnvironmentVariable('JAVA_HOME', $javaPath, 'User')

    Write-Host "`n성공적으로 JAVA_HOME이 설정되었습니다!" -ForegroundColor Green
    Write-Host "새 JAVA_HOME: $javaPath" -ForegroundColor Green

    Write-Host "`n주의: 변경사항을 적용하려면 다음을 수행하세요:" -ForegroundColor Yellow
    Write-Host "1. 열려있는 모든 터미널/명령 프롬프트를 닫으세요" -ForegroundColor White
    Write-Host "2. IDE(VS Code, IntelliJ 등)를 재시작하세요" -ForegroundColor White
    Write-Host "3. 새 터미널에서 'mvn --version'으로 확인하세요" -ForegroundColor White
} else {
    Write-Host "오류: Java 17을 찾을 수 없습니다: $javaPath" -ForegroundColor Red
    Write-Host "Java 17이 설치되어 있는지 확인하세요." -ForegroundColor Red
}

Write-Host "`n설치된 Java 버전들:" -ForegroundColor Yellow
Get-ChildItem "C:\Program Files\Java" | Select-Object Name
