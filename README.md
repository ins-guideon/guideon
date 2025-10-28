# 📘 규정 Q&A 시스템 (Guideon)

AI 기반 규정 검색 시스템입니다.

## 🎯 주요 기능

### 1. 자연어 질문 분석 (Query Analysis)
- **AI 기반 질문 분석**: Google Gemini를 사용하여 사용자의 자연어 질문을 구조화
- **키워드 추출**: 질문에서 핵심 키워드 자동 추출
- **규정 유형 매칭**: 27개 사규 유형 중 관련 규정 자동 식별
- **질문 의도 파악**: 정보조회/절차안내/기준확인/자격요건/예외사항 분류
- **검색 쿼리 최적화**: 벡터 검색에 최적화된 쿼리로 변환

### 2. RAG 기반 규정 검색
- **벡터 검색**: 임베딩 기반 의미 검색으로 관련 규정 조항 탐색
- **답변 생성**: 검색된 규정을 근거로 LLM이 정확한 답변 생성
- **출처 표시**: 답변의 근거가 되는 규정명, 조항, 관련도 점수 제공
- **신뢰도 평가**: 검색 결과의 신뢰도 점수 계산

## 🏗 아키텍처

```
사용자 질문 (자연어)
    ↓
QueryAnalysisService (Gemini API)
    ↓
구조화된 분석 결과
    - 키워드
    - 규정 유형
    - 질문 의도
    - 검색 쿼리
    ↓
RegulationSearchService (RAG)
    ↓
벡터 검색 (Embedding Store)
    ↓
관련 규정 조항 검색
    ↓
답변 생성 (Gemini API)
    ↓
결과 반환 (답변 + 근거 조항 + 신뢰도)
```

## 📦 기술 스택

### Backend
- **Java 17+**
- **Spring Boot 3.2**: REST API 서버
- **LangChain4j**: LLM 오케스트레이션 및 RAG 구현
- **Google Gemini API**:
  - `gemini-2.5-flash`: 질문 분석 (빠른 응답)
  - `gemini-2.5-pro`: 답변 생성 (정확도 우선)
- **Embedding Model**: AllMiniLM-L6-v2 (로컬 실행)
- **Vector Store**: In-Memory (개발용, 운영시 Qdrant 권장)
- **Maven**: 빌드 도구

## 🚀 시작하기

### 사전 요구사항

- **Java 17 이상**: [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) 또는 [OpenJDK](https://adoptium.net/)
- **Maven 3.6+**: [다운로드](https://maven.apache.org/download.cgi)
- **Google Gemini API Key**: [발급 받기](https://makersuite.google.com/app/apikey)

### 1. 환경 설정

#### Google Gemini API 키 설정

**방법 1: 환경변수 설정 (권장)**

```bash
# Windows (PowerShell)
$env:GOOGLE_API_KEY="your_api_key_here"

# Windows (CMD)
set GOOGLE_API_KEY=your_api_key_here

# Linux/Mac
export GOOGLE_API_KEY=your_api_key_here
```

**방법 2: application.properties 파일 수정**

`src/main/resources/application.properties` 파일에서 API 키 직접 입력:

```properties
gemini.api.key=your_actual_api_key_here
```

⚠️ **보안 주의**: API 키를 코드에 포함하여 Git에 커밋하지 마세요!

### 2. 의존성 설치 및 빌드

```bash
# Maven 의존성 다운로드 및 JAR 파일 빌드
mvn clean package -DskipTests
```

빌드가 완료되면 `target/regulation-search-1.0.0.jar` 파일이 생성됩니다.

### 3. 서비스 실행

#### 🚀 방법 1: 풀스택 실행 (백엔드 + 프론트엔드) - 추천!

```bash
# 백엔드(8080)와 프론트엔드(3000)를 동시에 실행
start-all.bat
```

이 명령어는:
- ✅ **백엔드 서버**: `http://localhost:8080` (별도 창)
- ✅ **프론트엔드**: `http://localhost:3000` (별도 창)

브라우저에서 `http://localhost:3000`으로 접속하면 전체 애플리케이션을 사용할 수 있습니다!

#### 방법 2: 백엔드만 실행

```bash
# Spring Boot 서버만 실행 (포트 8080)
run-server.bat

# 또는 Maven 직접 사용
mvn spring-boot:run
```

서버가 시작되면 `http://localhost:8080`에서 REST API를 사용할 수 있습니다.

#### 방법 3: 프론트엔드만 실행

```bash
# 프론트엔드 개발 서버만 실행 (포트 3000)
cd guideon-frontend
start-frontend.bat

# 또는 npm 직접 사용
npm run dev
```

⚠️ **주의**: 프론트엔드만 실행하는 경우 백엔드 서버가 `http://localhost:8080`에서 실행 중이어야 합니다.

#### 방법 4: JAR 파일 빌드 후 실행 (프로덕션)

```bash
# 1. JAR 빌드
mvn clean package -DskipTests

# 2. 실행
java -jar target/regulation-search-1.0.0.jar
```

#### 방법 5: IDE에서 실행

- **Main Class**: `com.guideon.GuideonApplication`
- **VM Options**: `-Dserver.port=8080`

#### 포트 변경

다른 포트에서 실행하려면:

```bash
# 방법 1: application.yml 수정
server:
  port: 9090

# 방법 2: 환경변수
export SERVER_PORT=9090
mvn spring-boot:run

# 방법 3: 커맨드 라인 옵션
java -jar target/regulation-search-1.0.0.jar --server.port=9090
```

### 4. API 엔드포인트 확인

서버가 시작되면 다음 엔드포인트를 사용할 수 있습니다:

#### Health Check
```bash
curl http://localhost:8080/actuator/health
```

#### 질문 분석
```bash
curl -X POST http://localhost:8080/api/qa/analyze \
  -H "Content-Type: application/json" \
  -d '{"question": "연차 휴가는 몇 일인가요?"}'
```

#### 규정 검색 (통합)
```bash
curl -X POST http://localhost:8080/api/qa/search \
  -H "Content-Type: application/json" \
  -d '{"question": "해외 출장시 숙박비는 얼마까지 지원되나요?"}'
```

#### 규정 유형 목록 조회
```bash
curl http://localhost:8080/api/regulations/types
```

#### 규정 문서 업로드
```bash
curl -X POST http://localhost:8080/api/regulations/upload \
  -H "Content-Type: application/json" \
  -d '{
    "filePath": "c:/workspace/regulations/취업규칙.txt",
    "regulationType": "취업규칙"
  }'
```

### 5. 서버 로그 확인

정상 시작 시 다음과 같은 로그가 표시됩니다:

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

2025-10-17 ... : Starting GuideonApplication using Java 17...
2025-10-17 ... : ConfigLoader loaded from classpath: application.properties
2025-10-17 ... : QueryAnalysisService initialized with 27 regulation types
2025-10-17 ... : RegulationSearchService initialized
2025-10-17 ... : Tomcat started on port(s): 8080 (http)
2025-10-17 ... : Started GuideonApplication in 5.234 seconds
```

### 문제 해결

#### ⚠️ Maven 빌드 실패: "invalid flag: --release"

이 오류는 **Maven이 Java 8을 사용**하고 있을 때 발생합니다.

**해결 방법:**

```bash
# 1. 현재 Maven이 사용하는 Java 버전 확인
mvn -version
# Java version이 1.8이면 문제!

# 2. JAVA_HOME을 Java 17로 설정
# Windows (PowerShell)
$env:JAVA_HOME="C:\Program Files\Java\jdk-17"
$env:PATH="C:\Program Files\Java\jdk-17\bin;$env:PATH"

# Windows (CMD)
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=C:\Program Files\Java\jdk-17\bin;%PATH%

# Linux/Mac
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk
export PATH=$JAVA_HOME/bin:$PATH

# 3. 다시 빌드
mvn clean compile
```

**편리한 방법 (Windows):**

제공된 배치 스크립트를 사용하세요 (자동으로 Java 17 설정):

```bash
# 빌드
build.bat

# 서버 실행
run-server.bat
```

#### Java 버전 확인
```bash
java -version
# 출력: java version "17.0.x" 이상이어야 함

mvn -version
# 중요: Java version도 17이어야 함!
```

#### Maven 버전 확인
```bash
mvn -version
# 출력: Apache Maven 3.6.x 이상이어야 함
```

#### API 키 확인
```bash
# Windows (PowerShell)
echo $env:GOOGLE_API_KEY

# Windows (CMD)
echo %GOOGLE_API_KEY%

# Linux/Mac
echo $GOOGLE_API_KEY
```

#### 일반적인 오류

1. **`invalid flag: --release`**: Maven이 Java 8 사용 → JAVA_HOME을 Java 17로 변경
2. **`GOOGLE_API_KEY not found`**: 환경변수가 설정되지 않았거나 application.yml에 API 키가 없음
3. **`ClassNotFoundException`**: Maven 빌드를 다시 실행 (`mvn clean package`)
4. **`Port 8080 already in use`**: 다른 프로세스가 포트를 사용중 → 포트 변경 또는 프로세스 종료

### 4. 사용 예시

```java
// 시스템 초기화
String apiKey = System.getenv("GOOGLE_API_KEY");
RegulationQASystem system = new RegulationQASystem(apiKey);

// 규정 문서 업로드 (인덱싱)
system.uploadRegulationDocument("path/to/취업규칙.txt", "취업규칙");
system.uploadRegulationDocument("path/to/경비지급규정.txt", "경비지급규정");

// 자연어 질문
String question = "해외 출장시 숙박비는 얼마까지 지원되나요?";
RegulationSearchResult result = system.askQuestion(question);

// 결과 확인
System.out.println("답변: " + result.getAnswer());
System.out.println("신뢰도: " + result.getConfidenceScore());
result.getReferences().forEach(ref -> {
    System.out.println("근거: " + ref.getDocumentName() + " - " + ref.getContent());
});
```

## 📡 REST API 명세

### 1. Q&A API

#### POST `/api/qa/analyze` - 질문 분석
사용자의 자연어 질문을 분석합니다.

**Request:**
```json
{
  "question": "연차 휴가는 몇 일인가요?"
}
```

**Response:**
```json
{
  "success": true,
  "message": "질문 분석이 완료되었습니다",
  "analysis": {
    "originalQuery": "연차 휴가는 몇 일인가요?",
    "keywords": ["연차", "휴가", "일수"],
    "regulationTypes": ["취업규칙", "복리후생비규정"],
    "intent": "기준확인",
    "searchQuery": "연차 휴가 일수 기준"
  }
}
```

#### POST `/api/qa/search` - 규정 검색
질문을 분석하고 관련 규정을 검색하여 답변을 생성합니다.

**Request:**
```json
{
  "question": "해외 출장시 숙박비는 얼마까지 지원되나요?"
}
```

**Response:**
```json
{
  "success": true,
  "message": "검색이 완료되었습니다",
  "result": {
    "answer": "해외 출장시 숙박비는 국가별로 차등 지원됩니다...",
    "references": [
      {
        "documentName": "출장여비지급규정",
        "clause": "제5조",
        "content": "해외 출장 숙박비는...",
        "pageNumber": 3,
        "relevanceScore": 0.89
      }
    ],
    "confidenceScore": 0.87,
    "hasAnswer": true
  }
}
```

### 2. 규정 관리 API

#### GET `/api/regulations/types` - 규정 유형 목록
지원하는 27개 규정 유형 목록을 조회합니다.

**Response:**
```json
{
  "success": true,
  "types": ["이사회규정", "접대비사용규정", ...],
  "count": 27
}
```

#### POST `/api/regulations/upload` - 규정 문서 업로드
규정 문서를 업로드하고 벡터 DB에 인덱싱합니다.

**Request:**
```json
{
  "filePath": "c:/workspace/regulations/취업규칙.txt",
  "regulationType": "취업규칙"
}
```

**Response:**
```json
{
  "success": true,
  "message": "규정 문서가 성공적으로 업로드되었습니다",
  "regulationType": "취업규칙",
  "filePath": "c:/workspace/regulations/취업규칙.txt"
}
```

## 📁 프로젝트 구조

```
guideon/
├── pom.xml                                    # Maven 설정 (Spring Boot)
├── CLUADE.md                                  # 시스템 설계 문서
├── README.md                                  # 이 파일
└── src/main/
    ├── java/com/guideon/
    │   ├── GuideonApplication.java           # Spring Boot 메인 클래스
    │   ├── controller/                       # REST API Controllers
    │   │   ├── QAController.java             # Q&A API
    │   │   └── RegulationController.java     # 규정 관리 API
    │   ├── service/                          # Business Logic
    │   │   ├── QueryAnalysisService.java     # 질문 분석 서비스
    │   │   └── RegulationSearchService.java  # RAG 검색 서비스
    │   ├── model/                            # Domain Models
    │   │   ├── QueryAnalysisResult.java
    │   │   ├── RegulationSearchResult.java
    │   │   └── RegulationReference.java
    │   ├── dto/                              # Data Transfer Objects
    │   │   ├── QuestionRequest.java
    │   │   ├── AnalysisResponse.java
    │   │   ├── SearchResponse.java
    │   │   └── UploadRequest.java
    │   ├── config/                           # Configuration
    │   │   ├── GuideonConfig.java            # Service Beans
    │   │   ├── WebConfig.java                # CORS 설정
    │   │   └── ConfigLoader.java             # Properties Loader
    │   └── exception/                        # Exception Handling
    │       └── GlobalExceptionHandler.java
    └── resources/
        ├── application.yml                   # Spring Boot 설정
        └── application.properties            # Legacy 설정
```

## 🔑 핵심 클래스

### QueryAnalysisService
자연어 질문을 AI로 분석하여 구조화된 검색 쿼리로 변환

**주요 메서드:**
- `analyzeQuery(String userQuery)`: 질문 분석 및 키워드 추출

**분석 결과:**
- 키워드 목록
- 관련 규정 유형
- 질문 의도
- 최적화된 검색 쿼리

### RegulationSearchService
RAG 기반 벡터 검색 및 답변 생성

**주요 메서드:**
- `indexDocument(Document doc, String type)`: 규정 문서 인덱싱
- `search(QueryAnalysisResult analysis)`: 규정 검색 및 답변 생성

**검색 과정:**
1. 벡터 임베딩 생성
2. 유사도 기반 관련 조항 검색
3. 검색된 조항을 컨텍스트로 LLM 답변 생성
4. 신뢰도 점수 계산

## 🎨 지원하는 규정 유형 (27종)

이사회규정, 접대비사용규정, 윤리규정, 출장여비지급규정, 주식매수선택권운영규정, 노사협의회규정, 취업규칙, 매출채권관리규정, 금융자산 운용규정, 문서관리규정, 재고관리규정, 계약검토규정, 사규관리규정, 임원퇴직금지급규정, 임원보수규정, 주주총회운영규정, 경비지급규정, 복리후생비규정, 보안관리규정, 위임전결규정, 우리사주운영규정, 내부정보관리규정, 회계관리규정, 특수관계자 거래규정, 조직 및 업무분장규정, 자금관리규정, 인장관리규정

## 🔄 워크플로우 예시

```
입력: "연차 휴가는 몇 일인가요?"

[QueryAnalysisService 분석]
→ 키워드: [연차, 휴가, 일수]
→ 규정 유형: [취업규칙, 복리후생비규정]
→ 질문 의도: 기준확인
→ 검색 쿼리: "연차 휴가 일수 기준"

[RegulationSearchService 검색]
→ 벡터 검색: 관련 조항 5개 발견
→ 컨텍스트: "취업규칙 제XX조 연차휴가는..."
→ AI 답변 생성: "연차 휴가는 근속년수에 따라..."

[결과 반환]
→ 답변: "연차 휴가는 1년 근무시 15일이 부여됩니다..."
→ 근거: 취업규칙 제32조 (관련도: 0.89)
→ 신뢰도: 0.87
```

## ⚠️ 주의사항

1. **API 키 보안**: `GOOGLE_API_KEY`를 코드에 하드코딩하지 마세요
2. **인덱싱 필수**: 질문하기 전에 반드시 규정 문서를 인덱싱해야 합니다
3. **개발 환경**: 현재 In-Memory 저장소 사용중 (재시작시 데이터 소실)
4. **운영 환경**: Qdrant 등 영구 벡터 DB로 교체 권장

## 🎨 React 기반 UI 개발

### 화면 목록

#### 1. 로그인 화면 (`/login`)
- 사용자 인증
- 세션 관리
- 자동 로그인 옵션

#### 2. 메인 대시보드 (`/`)
- 질문 입력 인터페이스
- 최근 질문 이력
- 즐겨찾기 규정 목록
- 통계 대시보드 (질문 수, 규정 활용도)

#### 3. 질문 & 답변 화면 (`/qa`)
- 자연어 질문 입력 폼
- 실시간 질문 분석 표시
  - 추출된 키워드
  - 관련 규정 유형
  - 질문 의도
- AI 답변 표시
  - 답변 내용
  - 신뢰도 점수
  - 근거 규정 조항 목록
- 답변 평가 (도움됨/도움안됨)
- 답변 공유 기능

#### 4. 규정 관리 화면 (`/regulations`)
- 규정 문서 목록 (27개 유형별 분류)
- 규정 문서 업로드 인터페이스
- 규정 문서 미리보기
- 문서 버전 관리
- 인덱싱 상태 표시

#### 5. 검색 이력 화면 (`/history`)
- 과거 질문 목록
- 질문별 답변 조회
- 필터링 (날짜, 규정 유형, 신뢰도)
- 즐겨찾기 추가/제거

#### 6. 통계 및 분석 화면 (`/analytics`)
- 질문 통계 (일별/주별/월별)
- 규정별 활용도
- 신뢰도 분포
- 인기 키워드
- 답변 만족도 통계

#### 7. 설정 화면 (`/settings`)
- API 키 관리
- 모델 설정 (Gemini Flash/Pro 선택)
- 검색 옵션 (최대 결과 수, 최소 신뢰도)
- 알림 설정

### 기술 스택

#### Frontend
- **React 18+**: UI 라이브러리
- **TypeScript**: 타입 안정성
- **React Router v6**: SPA 라우팅
- **Vite**: 빌드 도구 (빠른 개발 서버)
- **TanStack Query (React Query)**: 서버 상태 관리
- **Zustand**: 클라이언트 상태 관리 (경량)
- **Axios**: HTTP 클라이언트

#### UI 컴포넌트
- **Ant Design** 또는 **Material-UI (MUI)**: UI 컴포넌트 라이브러리
- **Tailwind CSS**: 유틸리티 CSS 프레임워크
- **React Markdown**: 답변 렌더링
- **React Syntax Highlighter**: 코드 하이라이팅
- **Recharts**: 통계 차트

#### 개발 도구
- **ESLint**: 코드 린팅
- **Prettier**: 코드 포맷팅
- **Vitest**: 단위 테스트
- **React Testing Library**: 컴포넌트 테스트

#### Backend 연동
- **REST API**: Spring Boot 백엔드 연동
- **WebSocket** (선택): 실시간 분석 결과 스트리밍

### 프로젝트 구조
```
guideon-frontend/
├── public/
├── src/
│   ├── assets/              # 이미지, 폰트 등
│   ├── components/          # 재사용 가능한 컴포넌트
│   │   ├── common/         # 공통 컴포넌트 (Button, Input 등)
│   │   ├── layout/         # 레이아웃 컴포넌트
│   │   ├── qa/             # Q&A 관련 컴포넌트
│   │   └── regulation/     # 규정 관련 컴포넌트
│   ├── pages/              # 페이지 컴포넌트
│   │   ├── Login.tsx
│   │   ├── Dashboard.tsx
│   │   ├── QAPage.tsx
│   │   ├── Regulations.tsx
│   │   ├── History.tsx
│   │   ├── Analytics.tsx
│   │   └── Settings.tsx
│   ├── hooks/              # 커스텀 훅
│   ├── services/           # API 서비스
│   ├── stores/             # Zustand 스토어
│   ├── types/              # TypeScript 타입 정의
│   ├── utils/              # 유틸리티 함수
│   ├── App.tsx
│   └── main.tsx
├── package.json
├── tsconfig.json
├── vite.config.ts
└── tailwind.config.js
```

## 🚧 향후 개선 사항

- [ ] Qdrant 벡터 DB 통합
- [ ] PDF/Word 문서 파서 추가
- [ ] 조항 번호 자동 추출
- [ ] REST API 서버 구현 (Spring Boot)
- [x] React 기반 웹 UI 개발
- [ ] 문서 버전 관리
- [ ] 사용자 피드백 수집
- [ ] 검색 품질 모니터링
- [ ] WebSocket 실시간 통신
- [ ] PWA 지원 (오프라인 사용)

## 📄 라이선스

MIT License#   g u i d e o n 
 
 #   g u i d e o n 
 
 
