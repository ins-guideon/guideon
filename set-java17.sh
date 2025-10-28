#!/bin/bash
# Git Bash용 Java 17 환경 설정 스크립트

JAVA17_HOME="/c/Program Files/Java/jdk-17"

echo "=== Java 17 환경 설정 ==="
echo ""

# 현재 JAVA_HOME 확인
echo "현재 JAVA_HOME: $JAVA_HOME"
echo ""

# Java 17 경로 확인
if [ -d "$JAVA17_HOME" ]; then
    echo "✓ Java 17 발견: $JAVA17_HOME"

    # JAVA_HOME 설정
    export JAVA_HOME="$JAVA17_HOME"
    export PATH="$JAVA_HOME/bin:$PATH"

    echo "✓ JAVA_HOME이 Java 17로 설정되었습니다"
    echo ""

    # Java 버전 확인
    echo "Java 버전 확인:"
    java -version
    echo ""

    echo "Maven 버전 확인:"
    mvn --version
    echo ""

    echo "=== 설정 완료 ==="
    echo ""
    echo "이 설정은 현재 터미널 세션에만 적용됩니다."
    echo "영구적으로 적용하려면 다음 중 하나를 수행하세요:"
    echo ""
    echo "1. ~/.bashrc 파일에 다음 줄을 추가:"
    echo "   export JAVA_HOME=\"/c/Program Files/Java/jdk-17\""
    echo "   export PATH=\"\$JAVA_HOME/bin:\$PATH\""
    echo ""
    echo "2. 또는 관리자 권한으로 PowerShell에서 set-java17.ps1 실행:"
    echo "   powershell -ExecutionPolicy Bypass -File set-java17.ps1"

else
    echo "✗ 오류: Java 17을 찾을 수 없습니다: $JAVA17_HOME"
    echo ""
    echo "설치된 Java 버전들:"
    ls -la "/c/Program Files/Java" | grep "^d" | awk '{print $NF}'
fi
