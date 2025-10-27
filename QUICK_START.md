# 🚀 Guideon 빠른 시작 가이드

## 📋 요구사항

- ✅ **Java 17 이상**
- ✅ **Maven 3.6+**
- ✅ **Node.js 18+** (프론트엔드용)
- ✅ **Google Gemini API Key**

## ⚡ 30초 안에 시작하기

### 1️⃣ API 키 설정

```bash
# Windows (PowerShell)
$env:GOOGLE_API_KEY="your_api_key_here"

# Windows (CMD)
set GOOGLE_API_KEY=your_api_key_here
```

### 2️⃣ 서비스 실행

```bash
# 백엔드 + 프론트엔드 동시 실행 (가장 쉬운 방법!)
start-all.bat
```

### 3️⃣ 접속

- **프론트엔드**: http://localhost:3000
- **백엔드 API**: http://localhost:8080

## 🎯 실행 방법 비교

| 방법 | 명령어 | 설명 | 추천 |
|------|--------|------|------|
| **풀스택** | `start-all.bat` | 백엔드 + 프론트엔드 동시 실행 | ⭐⭐⭐ |
| **백엔드만** | `run-server.bat` | REST API 서버만 실행 | ⭐⭐ |
| **프론트엔드만** | `cd guideon-frontend && start-frontend.bat` | UI만 실행 (백엔드 필요) | ⭐ |
| **빌드 + 실행** | `build.bat` → `java -jar target/*.jar` | JAR로 실행 | 프로덕션용 |

## 📱 화면 구성

### 프론트엔드 (http://localhost:3000)

1. **로그인** (`/login`)
2. **대시보드** (`/`)
   - 통계 확인
   - 최근 질문 이력
3. **질문하기** (`/qa`)
   - 자연어 질문 입력
   - AI 답변 확인
4. **규정 관리** (`/regulations`)
   - 규정 업로드
   - 규정 조회
5. **검색 이력** (`/history`)
6. **통계** (`/analytics`)
7. **설정** (`/settings`)

### 백엔드 API (http://localhost:8080)

```bash
# 헬스 체크
curl http://localhost:8080/actuator/health

# API 정보
curl http://localhost:8080/

# 규정 유형 목록
curl http://localhost:8080/api/regulations/types

# 질문 분석
curl -X POST http://localhost:8080/api/qa/analyze \
  -H "Content-Type: application/json" \
  -d '{"question": "연차 휴가는 몇 일인가요?"}'

# 규정 검색
curl -X POST http://localhost:8080/api/qa/search \
  -H "Content-Type: application/json" \
  -d '{"question": "해외 출장 숙박비는?"}'
```

## ⚠️ 문제 해결

### Maven 빌드 실패: "invalid flag: --release"

```bash
# JAVA_HOME 확인
mvn -version

# Java 8이면 Java 17로 변경
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;%PATH%
```

### 포트 8080 이미 사용 중

```bash
# 프로세스 확인
netstat -ano | findstr :8080

# 프로세스 종료
taskkill /F /PID [PID번호]
```

### 프론트엔드 의존성 설치

```bash
cd guideon-frontend
npm install
```

## 📂 주요 파일

```
guideon/
├── start-all.bat              # 풀스택 실행 (백엔드 + 프론트엔드)
├── run-server.bat             # 백엔드만 실행
├── build.bat                  # Maven 빌드
├── README.md                  # 상세 문서
├── QUICK_START.md            # 이 파일
│
├── src/main/java/com/guideon/
│   ├── GuideonApplication.java           # 메인 클래스
│   ├── controller/                       # REST Controllers
│   ├── service/                          # 비즈니스 로직
│   ├── model/                            # 도메인 모델
│   └── config/                           # 설정
│
└── guideon-frontend/
    ├── start-frontend.bat     # 프론트엔드 실행
    ├── src/                   # React 소스
    └── package.json           # npm 설정
```

## 🔗 더 많은 정보

- **상세 문서**: [README.md](README.md)
- **시스템 설계**: [CLAUDE.md](CLAUDE.md)
- **프론트엔드 문서**: [guideon-frontend/README.md](guideon-frontend/README.md)
- **API 명세**: [README.md#rest-api-명세](README.md#rest-api-명세)

## 🎓 사용 예시

### 1. 규정 문서 업로드

```bash
curl -X POST http://localhost:8080/api/regulations/upload \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "c:/regulations/취업규칙.txt",
    "regulationType": "취업규칙"
  }'
```

### 2. 질문하기

```bash
curl -X POST http://localhost:8080/api/qa/search \
  -H "Content-Type: application/json" \
  -d '{"question": "연차는 몇 일?"}'
```

### 3. 응답 예시

```json
{
  "success": true,
  "result": {
    "answer": "연차 휴가는 1년 근무시 15일이 부여됩니다...",
    "references": [
      {
        "documentName": "취업규칙",
        "clause": "제32조",
        "relevanceScore": 0.89
      }
    ],
    "confidenceScore": 0.87
  }
}
```

---

**Happy Coding! 🚀**
