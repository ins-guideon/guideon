# 빌드 가이드

## 🚀 가장 쉬운 방법

### Git Bash
```bash
./build.sh
```

### PowerShell
```powershell
.\build.ps1
```

빌드 스크립트가 자동으로:
1. Java 17 환경 설정
2. Maven 빌드 실행 (`mvn clean package -DskipTests`)
3. 결과 출력

## 📋 빌드 결과

성공 시:
- `target/regulation-search-1.0.0.jar` 생성
- 실행 가능한 Spring Boot JAR 파일

## 🏃 실행 방법

### 방법 1: Maven으로 실행
```bash
# Git Bash
source ./set-java17.sh
mvn spring-boot:run

# PowerShell
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"; mvn spring-boot:run
```

### 방법 2: JAR 직접 실행
```bash
java -jar target/regulation-search-1.0.0.jar
```

## ⚠️ 문제 해결

### "invalid flag: --release" 오류
→ Maven이 Java 8을 사용중입니다. 빌드 스크립트를 사용하거나 [JAVA17-SETUP.md](JAVA17-SETUP.md)를 참조하세요.

### "For input string: """ 오류
→ `pom.xml`의 `maven.compiler.release`가 빈 문자열입니다. 해당 줄을 제거하세요.

### Java 버전 확인
```bash
mvn --version
```
출력에 "Java version: 17.x.x"가 표시되어야 합니다.

## 🔍 자세한 설정

Java 17 영구 설정 및 상세 가이드는 [JAVA17-SETUP.md](JAVA17-SETUP.md)를 참조하세요.
