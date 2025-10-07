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

- **Java 17+**
- **LangChain4j**: LLM 오케스트레이션 및 RAG 구현
- **Google Gemini API**:
  - `gemini-2.5-flash`: 질문 분석 (빠른 응답)
  - `gemini-2.5-pro`: 답변 생성 (정확도 우선)
- **Embedding Model**: AllMiniLM-L6-v2 (로컬 실행)
- **Vector Store**: In-Memory (개발용, 운영시 Qdrant 권장)
- **Maven**: 빌드 도구

## 🚀 시작하기

### 1. 환경 설정

Google Gemini API 키 발급 후 환경변수 설정:

```bash
# Windows (PowerShell)
$env:GOOGLE_API_KEY="your_api_key_here"

# Windows (CMD)
set GOOGLE_API_KEY=your_api_key_here

# Linux/Mac
export GOOGLE_API_KEY=your_api_key_here
```

### 2. 빌드

```bash
# 의존성 설치 및 실행 가능한 JAR 빌드
mvn clean package -DskipTests
```

### 3. 실행

```bash
# Windows
run.bat

# 또는 직접 실행
"C:\Program Files\Java\jdk-17\bin\java" -jar target\regulation-search-1.0.0.jar
```

**주의사항:**
- Java 17 이상이 필요합니다
- `GOOGLE_API_KEY` 환경변수가 설정되어 있어야 합니다
- 처음 실행 시 의존성 다운로드로 시간이 소요될 수 있습니다

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

## 📁 프로젝트 구조

```
guideon/
├── pom.xml                                    # Maven 설정
├── CLUADE.md                                  # 시스템 설계 문서
├── README.md                                  # 이 파일
└── src/main/java/com/guideon/
    ├── RegulationQASystem.java               # 메인 클래스
    ├── model/
    │   ├── QueryAnalysisResult.java          # 질문 분석 결과 모델
    │   ├── RegulationSearchResult.java       # 검색 결과 모델
    │   └── RegulationReference.java          # 규정 참조 정보 모델
    └── service/
        ├── QueryAnalysisService.java         # 자연어 질문 분석 서비스
        └── RegulationSearchService.java      # RAG 기반 검색 서비스
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

## 🚧 향후 개선 사항

- [ ] Qdrant 벡터 DB 통합
- [ ] PDF/Word 문서 파서 추가
- [ ] 조항 번호 자동 추출
- [ ] REST API 서버 구현
- [ ] 웹 UI 추가
- [ ] 문서 버전 관리
- [ ] 사용자 피드백 수집
- [ ] 검색 품질 모니터링

## 📄 라이선스

MIT License