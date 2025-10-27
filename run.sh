#!/bin/bash
# Guideon 애플리케이션 실행 스크립트 (Git Bash)

echo "=== Guideon 애플리케이션 실행 ==="
echo ""

# Java 17 경로 설정
JAVA17_HOME="/c/Program Files/Java/jdk-17"

# Java 17 존재 확인
if [ -d "$JAVA17_HOME" ]; then
    # 현재 세션에 Java 17 설정
    export JAVA_HOME="$JAVA17_HOME"
    export PATH="$JAVA_HOME/bin:$PATH"

    echo "✓ Java 17 설정 완료"
    echo ""

    # JAR 파일 확인
    JAR_FILE="target/regulation-search-1.0.0.jar"

    if [ -f "$JAR_FILE" ]; then
        echo "애플리케이션 시작 중..."
        echo ""

        # 애플리케이션 실행 (UTF-8 인코딩 설정)
        java \
            -Dfile.encoding=UTF-8 \
            -Dconsole.encoding=UTF-8 \
            -Djansi.passthrough=true \
            -jar "$JAR_FILE"
    else
        echo "✗ 오류: JAR 파일을 찾을 수 없습니다: $JAR_FILE"
        echo ""
        echo "먼저 빌드를 실행하세요:"
        echo "  ./build.sh"
        exit 1
    fi
else
    echo "✗ 오류: Java 17을 찾을 수 없습니다: $JAVA17_HOME"
    echo ""
    echo "Java 17을 설치하거나 JAVA17_HOME 변수를 수정하세요."
    exit 1
fi
