# Java 17 환경 설정 가이드

이 프로젝트는 Spring Boot 3.2.0을 사용하므로 **Java 17 이상**이 필요합니다.

## 현재 상황 확인

```bash
# Java 버전 확인
java -version

# Maven이 사용하는 Java 버전 확인
mvn --version
```

Maven이 "Java version: 1.8.x"를 표시하면 JAVA_HOME 설정이 필요합니다.

## 빠른 해결 방법 (현재 터미널 세션만)

### 🚀 방법 A: 빌드 스크립트 사용 (가장 쉬움!)

#### Git Bash
```bash
./build.sh
```

#### PowerShell
```powershell
.\build.ps1
```

빌드 스크립트가 자동으로 Java 17 설정 후 빌드를 실행합니다.

---

### 방법 B: 환경 변수 직접 설정

#### Git Bash
```bash
# 프로젝트 루트에서 실행
source ./set-java17.sh

# 빌드 실행
mvn clean package
```

#### PowerShell
```powershell
# 임시로 환경 변수 설정 (현재 세션만)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

# 설정 확인
mvn --version

# 빌드 실행
mvn clean package

# 또는 한 줄로:
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"; $env:PATH="$env:JAVA_HOME\bin;$env:PATH"; mvn clean package
```

## 영구 해결 방법

### 방법 1: Windows 시스템 환경 변수 설정 (권장)

#### PowerShell 스크립트 사용 (관리자 권한 필요)
```powershell
# 관리자 권한으로 PowerShell 실행 후
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
.\set-java17.ps1
```

#### 수동 설정
1. Windows 검색에서 "환경 변수" 입력
2. "시스템 환경 변수 편집" 선택
3. "환경 변수" 버튼 클릭
4. 시스템 변수에서 `JAVA_HOME` 찾기
5. 값을 `C:\Program Files\Java\jdk-17`로 변경
6. `Path` 변수 편집:
   - `%JAVA_HOME%\bin` 항목을 최상단으로 이동
   - 또는 Java 8 관련 경로보다 위에 배치
7. "확인" 클릭
8. **모든 터미널/명령 프롬프트/IDE를 재시작**

### 방법 2: Git Bash 프로필 설정

```bash
# ~/.bashrc 파일에 추가
echo 'export JAVA_HOME="/c/Program Files/Java/jdk-17"' >> ~/.bashrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.bashrc

# 설정 적용
source ~/.bashrc

# 확인
mvn --version
```

### 방법 3: VS Code 설정 (VS Code 사용 시)

1. VS Code 설정 열기 (Ctrl+,)
2. "java.configuration.runtimes" 검색
3. settings.json에 다음 추가:
```json
{
  "java.configuration.runtimes": [
    {
      "name": "JavaSE-17",
      "path": "C:\\Program Files\\Java\\jdk-17",
      "default": true
    }
  ],
  "java.home": "C:\\Program Files\\Java\\jdk-17"
}
```

## 설정 확인

모든 설정 완료 후:

```bash
# Java 버전 확인 (17.x.x가 나와야 함)
java -version

# Maven 버전 확인 (Java version: 17.x.x가 나와야 함)
mvn --version

# 프로젝트 빌드
cd /c/workspace2/guideon
mvn clean package
```

## 빌드 및 실행

### 빌드만
```bash
mvn clean compile
```

### 전체 패키지 빌드
```bash
mvn clean package
```

### 테스트 스킵하고 빌드
```bash
mvn clean package -DskipTests
```

### 애플리케이션 실행
```bash
mvn spring-boot:run
```

또는 빌드된 JAR 직접 실행:
```bash
java -jar target/regulation-search-1.0.0.jar
```

## 문제 해결

### "invalid flag: --release" 오류
- Maven이 Java 8을 사용하고 있다는 의미
- 위의 영구 해결 방법 중 하나를 적용하세요

### "invalid target release: 17" 오류
- 컴파일러가 Java 17을 지원하지 않는 JDK를 사용중
- JAVA_HOME이 올바르게 설정되었는지 확인

### IDE에서 빌드는 되는데 Maven에서 안 됨
- IDE와 터미널의 Java 버전이 다른 것
- 터미널의 JAVA_HOME을 확인하고 설정

## 추가 정보

- Java 17 다운로드: https://www.oracle.com/java/technologies/downloads/#java17
- Spring Boot 3.x 시스템 요구사항: Java 17 이상
- 현재 프로젝트 설정:
  - Spring Boot: 3.2.0
  - Java: 17
  - Maven Compiler Plugin: 3.10.1
