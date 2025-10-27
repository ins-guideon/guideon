#!/bin/bash
# Guideon 프로젝트 빌드 스크립트 (Git Bash)
# Java 17을 사용하여 Maven 빌드 실행

echo "=== Guideon 프로젝트 빌드 ==="
echo ""

# Java 17 경로 설정
JAVA17_HOME="/c/Program Files/Java/jdk-17"

echo "현재 JAVA_HOME 확인..."
echo "시스템 JAVA_HOME: $JAVA_HOME"
echo ""

# Java 17 존재 확인
if [ -d "$JAVA17_HOME" ]; then
    echo "✓ Java 17 발견: $JAVA17_HOME"

    # 현재 세션에 Java 17 설정
    export JAVA_HOME="$JAVA17_HOME"
    export PATH="$JAVA_HOME/bin:$PATH"

    echo "✓ 현재 세션에 Java 17 설정 완료"
    echo ""

    # Maven 버전 확인
    echo "Maven 버전 확인:"
    mvn --version
    echo ""

    # 빌드 시작
    echo "빌드 시작..."
    echo ""

    # 빌드 실행
    mvn clean package -DskipTests

    BUILD_RESULT=$?

    echo ""
    if [ $BUILD_RESULT -eq 0 ]; then
        echo "=== 빌드 성공! ==="
        echo ""
        echo "생성된 파일: target/regulation-search-1.0.0.jar"
        echo ""
        echo "실행 방법:"
        echo "  java -jar target/regulation-search-1.0.0.jar"
        echo "  또는"
        echo "  mvn spring-boot:run"
    else
        echo "=== 빌드 실패 ==="
        echo "위의 오류 메시지를 확인하세요."
        exit $BUILD_RESULT
    fi
else
    echo "✗ 오류: Java 17을 찾을 수 없습니다: $JAVA17_HOME"
    echo ""
    echo "설치된 Java 버전들:"
    ls -la "/c/Program Files/Java" | grep "^d" | awk '{print $NF}'
    echo ""
    echo "Java 17을 설치하거나 JAVA17_HOME 변수를 수정하세요."
    exit 1
fi
