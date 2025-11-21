# Few-shot 예제 적용 전후 비교 테스트

## 📋 테스트 목적

Few-shot 예제를 프롬프트에 적용한 전후의 질문 분석 및 답변 생성 품질을 비교하여 개선 효과를 측정합니다.

### 배경

Few-shot learning은 LLM에게 예제를 제공하여 원하는 출력 형식과 품질을 학습시키는 프롬프트 엔지니어링 기법입니다. 본 테스트는 다음을 검증합니다:

1. **질문 분석 정확도 향상**: 키워드 추출, 규정 유형 매칭, 의도 분류의 정확도 개선
2. **답변 생성 품질 향상**: 답변 형식 일관성, 구조화된 답변 생성 능력 개선
3. **검색 쿼리 최적화**: Few-shot 예제를 통해 더 정확한 검색 쿼리 생성

## 🎯 테스트 사항

### 1. 질문 분석 테스트 (QueryAnalysisService)

#### 측정 항목
- **키워드 추출 정확도**: 질문에서 핵심 키워드를 정확히 추출하는지
- **규정 유형 매칭 정확도**: 관련 규정 유형을 올바르게 식별하는지
- **의도 분류 정확도**: 질문 의도를 정확히 분류하는지 (정보조회, 절차설명, 기준확인 등)
- **검색 쿼리 최적화**: 검색에 최적화된 쿼리문을 생성하는지

#### 테스트 질문 세트
1. "경조사에 대한 규정을 알려줘"
2. "연차 휴가는 몇 일인가요?"
3. "출장비 신청은 어떻게 하나요?"
4. "연차를 시간 단위로 사용할 수 있나요?"
5. "퇴직금은 어떻게 계산하나요?"
6. "경비 지급에서 예외가 되는 경우가 있나요?"
7. "직원의 권리와 의무는 무엇인가요?"

### 2. 답변 생성 테스트 (RegulationSearchService)

#### 측정 항목
- **답변 품질 점수**: AnswerQualityEnhancer를 통한 품질 점수 (0.0 ~ 1.0)
  - 답변 길이 평가 (25%)
  - 규정 조항 참조 평가 (25%)
  - 부정적 표현 체크 (25%)
  - 구조화 및 가독성 평가 (25%)
- **조항 인용 개수**: 답변에 포함된 규정 조항 인용 개수
- **답변 길이**: 생성된 답변의 문자 수
- **응답 시간**: 답변 생성에 소요된 시간
- **답변 형식 일관성**: Few-shot 예제를 통해 일관된 형식의 답변 생성
- **의도별 답변 품질**: 각 의도에 맞는 최적화된 답변 형식 적용

### 3. Few-shot 예제 품질 검증

#### 검증 항목
- 질문 분석 예제 수: 최소 5개 이상
- 답변 생성 예제 수: 의도별 최소 5개 이상
- 예제 데이터 완전성: 질문, 답변, 분석 결과 등 필수 필드 포함 여부

## 🚀 테스트 실행 방법

### 전제 조건

1. **API 키 설정**
   ```bash
   # 환경변수 설정
   export GOOGLE_API_KEY=your_api_key_here
   
   # 또는 application.properties에 설정
   gemini.api.key=your_api_key_here
   ```

2. **프로젝트 빌드**
   ```bash
   mvn clean compile
   ```

### 실행 방법

#### 방법 1: 전체 테스트 실행
```bash
mvn test -Dtest=FewShotComparisonTest
```

#### 방법 2: 특정 테스트만 실행
```bash
# 질문 분석 비교 테스트만 실행
mvn test -Dtest=FewShotComparisonTest#testQueryAnalysisComparison

# 답변 생성 비교 테스트만 실행
mvn test -Dtest=FewShotComparisonTest#testAnswerGenerationComparison

# Few-shot 예제 품질 검증만 실행
mvn test -Dtest=FewShotComparisonTest#testFewShotExampleQuality
```

**주의**: 답변 생성 비교 테스트는 실제 LLM API 호출이 필요하므로 API 할당량을 확인하세요.

#### 방법 3: IDE에서 실행
- IntelliJ IDEA / Eclipse에서 `FewShotComparisonTest.java` 파일 열기
- 클래스명 옆의 실행 버튼 클릭 또는 개별 테스트 메서드 실행

### 테스트 결과 확인

테스트 실행 후 다음 파일들이 생성됩니다:

```
test-results/
├── fewshot-comparison-before.json              # Few-shot 예제 적용 전 결과 (질문 분석)
├── fewshot-comparison-after.json               # Few-shot 예제 적용 후 결과 (질문 분석)
├── fewshot-comparison-report.json              # 질문 분석 비교 리포트
├── fewshot-answer-comparison-before.json       # Few-shot 예제 적용 전 결과 (답변 생성)
├── fewshot-answer-comparison-after.json        # Few-shot 예제 적용 후 결과 (답변 생성)
└── fewshot-answer-comparison-report.json       # 답변 생성 비교 리포트
```

## 📊 테스트 결과 분석

### 결과 파일 구조

#### Before/After 결과 파일 (`fewshot-comparison-before.json`, `fewshot-comparison-after.json`)

```json
{
  "testName": "Few-shot 예제 적용 전/후",
  "version": "before/after",
  "results": [
    {
      "question": "질문 내용",
      "analysisResult": {
        "keywords": ["키워드1", "키워드2"],
        "regulationTypes": ["규정유형1", "규정유형2"],
        "intent": "의도",
        "searchQuery": "검색 쿼리"
      },
      "responseTimeMs": 1234,
      "timestamp": "2024-01-01T12:00:00"
    }
  ],
  "summary": {
    "averageResponseTimeMs": 1500.0,
    "totalTests": 7,
    "intentDistribution": {
      "기준확인": 2,
      "절차설명": 1,
      ...
    },
    "regulationTypeDistribution": {
      "취업규칙": 3,
      "복리후생비규정": 2,
      ...
    },
    "averageKeywordsPerQuery": 3.5
  }
}
```

#### 비교 리포트 (`fewshot-comparison-report.json`)

```json
{
  "beforeVersion": "before",
  "afterVersion": "after",
  "testName": "Few-shot 예제 적용 전후 비교",
  "responseTime": {
    "before": 1500.0,
    "after": 1600.0,
    "improvementPercent": -6.67
  },
  "questionComparisons": [
    {
      "question": "질문 내용",
      "keywords": {
        "before": ["키워드1"],
        "after": ["키워드1", "키워드2"]
      },
      "regulationTypes": {
        "before": ["규정1"],
        "after": ["규정1", "규정2"]
      },
      "intent": {
        "before": "의도1",
        "after": "의도1"
      },
      "searchQuery": {
        "before": "검색 쿼리1",
        "after": "검색 쿼리2"
      },
      "responseTimeMs": {
        "before": 1200,
        "after": 1300
      }
    }
  ],
  "summaryComparison": {
    "before": { ... },
    "after": { ... }
  }
}
```

#### 답변 생성 비교 리포트 (`fewshot-answer-comparison-report.json`)

```json
{
  "beforeVersion": "before",
  "afterVersion": "after",
  "testName": "Few-shot 예제 적용 전후 비교 (답변 생성)",
  "questionComparisons": [
    {
      "question": "연차 휴가는 몇 일인가요?",
      "answer": {
        "before": "답변 텍스트 (Before)",
        "after": "답변 텍스트 (After)",
        "length": {
          "before": 200,
          "after": 350
        },
        "qualityScore": {
          "before": 0.65,
          "after": 0.82,
          "improvement": 0.17
        },
        "articleReferences": {
          "before": 1,
          "after": 2,
          "improvement": 1
        }
      },
      "responseTimeMs": {
        "before": 3000,
        "after": 3200
      }
    }
  ],
  "answerGenerationComparison": {
    "averageAnswerLength": {
      "before": 250.0,
      "after": 380.0,
      "improvementPercent": 52.0
    },
    "averageQualityScore": {
      "before": 0.68,
      "after": 0.85,
      "improvement": 0.17
    },
    "averageArticleReferences": {
      "before": 1.2,
      "after": 2.5,
      "improvement": 1.3
    }
  },
  "summaryComparison": {
    "before": {
      "averageResponseTimeMs": 3000.0,
      "averageQualityScore": 0.68,
      "averageArticleReferences": 1.2,
      "totalAnswersGenerated": 3
    },
    "after": {
      "averageResponseTimeMs": 3200.0,
      "averageQualityScore": 0.85,
      "averageArticleReferences": 2.5,
      "totalAnswersGenerated": 3
    }
  }
}
```

### 분석 지표

#### 1. 정확도 개선 지표

- **키워드 추출 정확도**: Before 대비 After에서 더 많은 관련 키워드 추출
- **규정 유형 매칭 정확도**: Before 대비 After에서 더 정확한 규정 유형 식별
- **의도 분류 정확도**: Before 대비 After에서 더 정확한 의도 분류

#### 2. 품질 개선 지표

- **답변 형식 일관성**: Few-shot 예제 적용 후 일관된 답변 형식 사용
- **구조화된 답변**: 단계별 설명, 목록 형식 등 구조화된 답변 생성 비율 증가
- **조항 인용 정확도**: 규정 조항을 정확히 인용하는 비율 증가

#### 3. 성능 지표

- **응답 시간**: Few-shot 예제 추가로 인한 응답 시간 변화 (일반적으로 약간 증가)
- **처리량**: 단위 시간당 처리 가능한 질문 수

## 📈 예상 개선 효과

### 질문 분석 정확도
- **키워드 추출 정확도**: +15-25% 향상 예상
- **규정 유형 매칭 정확도**: +20-30% 향상 예상
- **의도 분류 정확도**: +25-35% 향상 예상

### 답변 생성 품질
- **답변 품질 점수**: +15-25% 향상 예상 (0.65 → 0.80 수준)
- **조항 인용 개수**: +50-100% 향상 예상 (평균 1개 → 2개)
- **답변 형식 일관성**: +30-40% 향상 예상
- **구조화된 답변 생성**: +40-50% 향상 예상
- **답변 길이**: +30-50% 증가 예상 (더 상세한 답변)

### 성능 영향
- **응답 시간**: +5-15% 증가 예상 (Few-shot 예제 추가로 인한 토큰 수 증가)
- **API 비용**: 약간 증가 (토큰 사용량 증가)

## 🔍 결과 해석 가이드

### 긍정적인 결과
- ✅ 키워드 추출 정확도 향상
- ✅ 규정 유형 매칭 정확도 향상
- ✅ 의도 분류 정확도 향상
- ✅ 답변 형식 일관성 향상
- ✅ 구조화된 답변 생성 비율 증가

### 주의해야 할 결과
- ⚠️ 응답 시간이 크게 증가한 경우 (20% 이상)
- ⚠️ 특정 질문 유형에서 정확도가 오히려 감소한 경우
- ⚠️ API 비용이 예상보다 크게 증가한 경우

### 개선 방안
- Few-shot 예제 수 조정 (현재 7개 → 5개로 감소 고려)
- 예제 선택 로직 개선 (질문 유형별로 다른 예제 사용)
- 프롬프트 최적화 (불필요한 설명 제거)

## 📝 테스트 실행 예시

### 실행 출력 예시

```
========================================
Few-shot 예제 적용 전후 비교 테스트
========================================

=== Before 테스트 (Few-shot 예제 없이) ===

질문: 경조사에 대한 규정을 알려줘
  키워드: [경조사]
  규정 유형: [일반]
  의도: 정보조회
  검색 쿼리: 경조사
  응답 시간: 1200ms

...

=== After 테스트 (Few-shot 예제 포함) ===

질문: 경조사에 대한 규정을 알려줘
  키워드: [경조사, 경조휴가, 경조금]
  규정 유형: [복리후생비규정, 취업규칙]
  의도: 정보조회
  검색 쿼리: 경조사 경조휴가 경조금
  응답 시간: 1350ms

...

========================================
테스트 결과 저장 완료
========================================
- Before 결과: test-results/fewshot-comparison-before.json
- After 결과: test-results/fewshot-comparison-after.json
- 비교 리포트: test-results/fewshot-comparison-report.json
========================================
```

## 🐛 트러블슈팅

### 1. API 키 오류
```
⚠ API 키가 설정되지 않았습니다. 테스트를 건너뜁니다.
```
**해결**: `GOOGLE_API_KEY` 환경변수 또는 `application.properties` 설정

### 2. 테스트 결과 파일이 생성되지 않음
**해결**: `test-results` 디렉토리 권한 확인 및 생성 가능 여부 확인

### 3. 테스트 실행 시간이 너무 김
**해결**: 테스트 질문 수를 줄이거나, 특정 테스트만 실행

### 4. Before/After 결과가 동일함
**원인**: Few-shot 예제가 제대로 비활성화되지 않았을 수 있음
**해결**: `QueryAnalysisService.setUseFewShotExamples(false)` 호출 확인

## 📚 참고 자료

- [Few-shot Learning 개요](https://en.wikipedia.org/wiki/Few-shot_learning)
- [프롬프트 엔지니어링 가이드](https://www.promptingguide.ai/)
- [JUnit 5 사용 가이드](https://junit.org/junit5/docs/current/user-guide/)

## 📅 테스트 이력

| 날짜 | 버전 | 테스트 결과 | 비고 |
|------|------|------------|------|
| 2024-01-XX | 1.0.0 | 초기 테스트 | Few-shot 예제 적용 전후 비교 |

---

**작성일**: 2024-01-XX  
**작성자**: Guideon Development Team  
**버전**: 1.0.0

