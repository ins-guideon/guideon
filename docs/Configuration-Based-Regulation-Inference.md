# 규정 유형 추론 로직 구조화 가이드

## 📋 개요

기존의 하드코딩된 규정 유형 추론 로직을 **YAML 설정 파일 기반**으로 구조화하여 유지보수성과 확장성을 대폭 개선했습니다.

**변경 전**: Java 코드에 하드코딩 (매번 컴파일 필요)
**변경 후**: YAML 설정 파일로 관리 (설정만 수정하면 됨)

---

## 🎯 주요 개선 사항

### 1. 설정 기반 관리
- ✅ **재컴파일 불필요**: 설정 파일만 수정하면 즉시 반영
- ✅ **가독성 향상**: YAML 형식으로 직관적인 구조
- ✅ **버전 관리 용이**: Git으로 설정 변경 이력 추적
- ✅ **비개발자 수정 가능**: 간단한 YAML 문법만 이해하면 OK

### 2. 유연한 규칙 정의
- ✅ **우선순위 지원**: priority 값으로 규칙 적용 순서 제어
- ✅ **활성화/비활성화**: enabled 플래그로 규칙 on/off
- ✅ **다중 키워드**: 하나의 규칙에 여러 키워드 정의 가능
- ✅ **다중 규정 유형**: 하나의 규칙이 여러 규정 유형 매핑 가능

### 3. 확장성
- ✅ **새 규칙 추가 쉬움**: YAML 파일에 항목만 추가
- ✅ **테스트 도구 제공**: 설정 변경 후 즉시 테스트 가능
- ✅ **설정 검증**: 잘못된 설정 자동 감지 및 경고

---

## 📁 파일 구조

```
src/main/
├── resources/
│   └── regulation-inference-rules.yaml     # ← 규칙 설정 파일
└── java/com/guideon/
    ├── config/
    │   ├── RegulationInferenceConfig.java  # ← 설정 모델
    │   └── RegulationInferenceConfigLoader.java  # ← 설정 로더
    ├── service/
    │   └── QueryAnalysisService.java       # ← 사용처 (리팩토링됨)
    └── tool/
        └── RegulationInferenceTestTool.java  # ← 테스트 도구
```

---

## 📄 설정 파일 구조 (regulation-inference-rules.yaml)

### 전체 구조

```yaml
# 설정 버전
version: "1.0"

# 규칙 목록
rules:
  - name: "규칙 이름"
    description: "규칙 설명"
    keywords: ["키워드1", "키워드2", ...]
    regulationTypes: ["규정유형1", "규정유형2", ...]
    priority: 100        # 높을수록 우선 (기본값: 100)
    enabled: true        # 활성화 여부 (기본값: true)

# 전역 설정
settings:
  defaultRegulationType: "일반"  # 기본 규정 유형
  maxRegulationTypes: 5          # 최대 반환 개수
  minPriority: 50                # 최소 우선순위
  matchMode: "contains"          # 매칭 방식 (contains/exact/regex)
  logLevel: "INFO"               # 로그 레벨
```

### 규칙 정의 예시

```yaml
rules:
  # 경조사 관련 규칙
  - name: "복리후생 및 휴가"
    description: "복리후생비규정 및 취업규칙 관련 키워드"
    keywords:
      - "경조사"
      - "경조휴가"
      - "경조금"
      - "복지"
      - "복리후생"
      - "휴가"
      - "연차"
      - "병가"
    regulationTypes:
      - "복리후생비규정"
      - "취업규칙"
    priority: 100
    enabled: true

  # 출장 관련 규칙
  - name: "출장 및 여비"
    description: "출장여비지급규정 관련 키워드"
    keywords:
      - "출장"
      - "여비"
      - "교통비"
      - "숙박비"
    regulationTypes:
      - "출장여비지급규정"
    priority: 100
    enabled: true
```

---

## 🔧 사용 방법

### 1. 새로운 규칙 추가

**예시**: "재무 관련 규칙 추가"

```yaml
rules:
  # ... 기존 규칙들 ...

  # 새로운 규칙 추가
  - name: "재무 관리"
    description: "재무 관련 키워드"
    keywords:
      - "재무제표"
      - "결산"
      - "손익계산서"
      - "재무분석"
    regulationTypes:
      - "회계관리규정"
      - "자금관리규정"
    priority: 85
    enabled: true
```

### 2. 기존 규칙 수정

**예시**: 경조사 규칙에 키워드 추가

```yaml
- name: "복리후생 및 휴가"
  keywords:
    - "경조사"
    - "경조휴가"
    - "경조금"
    - "경조사비"      # ← 추가
    - "경조비"        # ← 추가
    - "복지"
    # ... 기타
```

### 3. 규칙 우선순위 조정

**예시**: 경조사 규칙의 우선순위를 높임

```yaml
- name: "복리후생 및 휴가"
  # ...
  priority: 110  # ← 100에서 110으로 상향 (더 우선 적용)
```

### 4. 규칙 비활성화

**예시**: 특정 규칙을 임시로 비활성화

```yaml
- name: "주식 및 스톡옵션"
  # ...
  enabled: false  # ← 이 규칙은 무시됨
```

---

## 🧪 테스트 방법

### 1. 테스트 도구 실행

```bash
mvn exec:java -Dexec.mainClass=com.guideon.tool.RegulationInferenceTestTool
```

### 2. 출력 예시

```
╔════════════════════════════════════════════════════════╗
║  Regulation Inference Configuration Test Tool        ║
╚════════════════════════════════════════════════════════╝

>>> Step 1: Loading Configuration

Version: 1.0
Total Rules: 20
Settings: Settings{defaultRegulationType='일반', maxRegulationTypes=5, ...}

Rules:
─────────────────────────────────────────────────────────

• 복리후생 및 휴가
  Description: 복리후생비규정 및 취업규칙 관련 키워드
  Keywords (20): [경조사, 경조휴가, 경조금, 복지, 복리후생...]
  Regulation Types: [복리후생비규정, 취업규칙]
  Priority: 100 | Enabled: ✓

• 출장 및 여비
  Description: 출장여비지급규정 관련 키워드
  Keywords (10): [출장, 여비, 교통비, 숙박비, 일비...]
  Regulation Types: [출장여비지급규정]
  Priority: 100 | Enabled: ✓

...

>>> Step 2: Testing Inference for Various Queries

═══════════════════════════════════════════════════════

Query: 경조사에 대한 규정을 알려줘
Inferred Types: [복리후생비규정, 취업규칙]
✓  2 regulation type(s) inferred
─────────────────────────────────────────────────────

Query: 해외출장비는 얼마인가요?
Inferred Types: [출장여비지급규정]
✓  1 regulation type(s) inferred
─────────────────────────────────────────────────────

Query: 점심 메뉴 추천해줘
Inferred Types: [일반]
⚠️  No specific regulation matched (using default)
─────────────────────────────────────────────────────

╔════════════════════════════════════════════════════════╗
║  Test Summary                                         ║
╚════════════════════════════════════════════════════════╝

Total Test Queries: 14
Matched Queries: 13 (92.9%)
Default Queries: 1 (7.1%)

✅ Configuration is working correctly!

>>> Step 3: Keyword-specific Tests
═══════════════════════════════════════════════════════

Keyword: 경조사          → Expected: 복리후생비규정         → ✓ PASS
Keyword: 출장           → Expected: 출장여비지급규정       → ✓ PASS
Keyword: 급여           → Expected: 급여규정              → ✓ PASS
Keyword: 재택근무       → Expected: 취업규칙              → ✓ PASS
Keyword: 보안           → Expected: 보안관리규정          → ✓ PASS

✅ Regulation Inference Configuration Test Complete!
```

### 3. 프로그래밍 방식 사용

```java
import com.guideon.config.RegulationInferenceConfigLoader;
import java.util.List;

// 규정 유형 추론
List<String> regulationTypes =
    RegulationInferenceConfigLoader.inferRegulationTypes("경조사 규정");

// 결과: ["복리후생비규정", "취업규칙"]
```

---

## 📊 코드 비교: 변경 전 vs 변경 후

### 변경 전 (하드코딩)

**QueryAnalysisService.java**:
```java
private List<String> inferRegulationTypes(String query) {
    List<String> inferred = new ArrayList<>();

    // 휴가/복리후생 관련 키워드
    if (query.matches(".*(경조사|경조휴가|경조금|복지|복리후생|휴가|연차|병가|출산|육아).*")) {
        inferred.add("복리후생비규정");
        inferred.add("취업규칙");
    }

    // 출장 관련 키워드
    if (query.matches(".*(출장|여비|교통비|숙박비|일비).*")) {
        inferred.add("출장여비지급규정");
    }

    // 급여 관련 키워드
    if (query.matches(".*(급여|월급|임금|보수|수당|상여금|퇴직금).*")) {
        inferred.add("급여규정");
        inferred.add("임원보수규정");
        inferred.add("임원퇴직금지급규정");
    }

    // 근무 관련 키워드
    if (query.matches(".*(근무|출퇴근|재택|시차|근태|야근).*")) {
        inferred.add("취업규칙");
    }

    return inferred;
}
```

**문제점**:
- ❌ 키워드 추가/수정 시 **재컴파일 필요**
- ❌ 정규식이 복잡하고 **가독성 낮음**
- ❌ 규칙 간 **우선순위 없음**
- ❌ **테스트 어려움**
- ❌ 규칙 활성화/비활성화 불가

---

### 변경 후 (설정 기반)

**QueryAnalysisService.java** (간소화됨):
```java
private List<String> inferRegulationTypes(String query) {
    // 설정 파일 기반 추론 사용
    return RegulationInferenceConfigLoader.inferRegulationTypes(query);
}
```

**regulation-inference-rules.yaml**:
```yaml
rules:
  - name: "복리후생 및 휴가"
    keywords:
      - "경조사"
      - "경조휴가"
      - "경조금"
      - "복지"
      - "복리후생"
      - "휴가"
      - "연차"
      - "병가"
      - "출산"
      - "육아"
    regulationTypes:
      - "복리후생비규정"
      - "취업규칙"
    priority: 100
    enabled: true

  - name: "출장 및 여비"
    keywords:
      - "출장"
      - "여비"
      - "교통비"
      - "숙박비"
      - "일비"
    regulationTypes:
      - "출장여비지급규정"
    priority: 100
    enabled: true

  # ... 기타 규칙들
```

**장점**:
- ✅ 키워드 추가/수정 시 **YAML만 수정** (재컴파일 불필요)
- ✅ 구조가 명확하고 **가독성 높음**
- ✅ **우선순위 지원** (priority 필드)
- ✅ **테스트 도구 제공**
- ✅ 규칙 **활성화/비활성화** 가능 (enabled 필드)
- ✅ **비개발자도 수정 가능**

---

## 🏗️ 아키텍처

```
┌─────────────────────────────────────────────────────┐
│  QueryAnalysisService                               │
│  (사용자 질문 분석)                                   │
└─────────────────┬───────────────────────────────────┘
                  │
                  │ inferRegulationTypes(query)
                  ↓
┌─────────────────────────────────────────────────────┐
│  RegulationInferenceConfigLoader                    │
│  (설정 로드 및 추론 엔진)                             │
│                                                       │
│  - load(): RegulationInferenceConfig               │
│  - inferRegulationTypes(query): List<String>       │
│  - matchesRule(): boolean                          │
└─────────────────┬───────────────────────────────────┘
                  │
                  │ loadAs()
                  ↓
┌─────────────────────────────────────────────────────┐
│  regulation-inference-rules.yaml                    │
│  (규칙 설정 파일)                                     │
│                                                       │
│  rules:                                             │
│    - name: "복리후생 및 휴가"                         │
│      keywords: [...]                                │
│      regulationTypes: [...]                         │
│  settings:                                          │
│    defaultRegulationType: "일반"                    │
└─────────────────────────────────────────────────────┘
```

---

## 🎨 고급 사용 예시

### 1. 우선순위를 활용한 세밀한 제어

```yaml
rules:
  # 높은 우선순위: 정확한 용어 매칭
  - name: "임원 퇴직금 (정확)"
    keywords: ["임원퇴직금", "임원 퇴직금"]
    regulationTypes: ["임원퇴직금지급규정"]
    priority: 120  # ← 매우 높음
    enabled: true

  # 중간 우선순위: 일반 퇴직금
  - name: "퇴직금 (일반)"
    keywords: ["퇴직금", "퇴직수당"]
    regulationTypes: ["임원퇴직금지급규정", "급여규정"]
    priority: 90   # ← 보통
    enabled: true
```

**동작**:
- "임원퇴직금" 쿼리 → 120 우선순위 규칙 먼저 적용 → "임원퇴직금지급규정"만 반환
- "퇴직금" 쿼리 → 90 우선순위 규칙 적용 → 여러 규정 반환

### 2. 시즌별 규칙 활성화/비활성화

```yaml
rules:
  - name: "하계휴가 (여름 시즌)"
    keywords: ["하계휴가", "여름휴가"]
    regulationTypes: ["복리후생비규정"]
    enabled: true   # ← 여름에는 true, 겨울에는 false로 변경
```

### 3. 정규식 패턴 사용

```yaml
settings:
  matchMode: "regex"  # ← 정규식 모드 활성화

rules:
  - name: "조항 번호 패턴"
    keywords: ["제\\s*\\d+\\s*조"]  # "제 1 조", "제32조" 등 매칭
    regulationTypes: ["사규관리규정"]
```

---

## 📈 성능 최적화

### 1. 캐싱
- 설정 파일은 **한 번만 로드**되고 메모리에 캐시됨
- 재시작 전까지 추가 디스크 I/O 없음

### 2. 조기 종료
- `maxRegulationTypes` 제한에 도달하면 즉시 반환
- 불필요한 규칙 평가 생략

### 3. 필터링
- `minPriority` 미만 규칙은 처음부터 제외
- `enabled: false` 규칙도 필터링

---

## 🔒 설정 검증

### 자동 검증 항목
1. ✅ 설정 파일 존재 여부
2. ✅ YAML 문법 오류
3. ✅ 필수 필드 누락 (keywords, regulationTypes)
4. ✅ 빈 키워드 리스트
5. ✅ 빈 규정 유형 리스트

### 검증 실패 시 동작
- 로그에 경고 메시지 출력
- 기본 설정으로 폴백
- 애플리케이션은 계속 실행됨

---

## 📚 모범 사례

### 1. 규칙 이름 지정
- ✅ 좋음: "복리후생 및 휴가", "출장 및 여비"
- ❌ 나쁨: "rule1", "test"

### 2. 키워드 선택
- ✅ 구체적이고 명확한 용어 사용
- ✅ 동의어 모두 포함
- ❌ 너무 일반적인 단어 (예: "것", "어떻게")

### 3. 우선순위 설정
- 100: 기본 (대부분의 규칙)
- 110+: 매우 구체적인 규칙
- 90-: 넓은 범위의 규칙

### 4. 규정 유형 매핑
- ✅ 관련성 높은 규정만 포함
- ❌ 너무 많은 규정 (검색 범위 과다)

---

## 🛠️ 트러블슈팅

### 문제 1: 설정이 로드되지 않음

**증상**:
```
ERROR Configuration file not found: regulation-inference-rules.yaml
```

**해결**:
1. 파일이 `src/main/resources/` 에 있는지 확인
2. 파일명 오타 확인 (대소문자 구분)
3. Maven 빌드 후 JAR 내부에 포함되었는지 확인

---

### 문제 2: 특정 쿼리가 매칭되지 않음

**진단**:
```bash
# 테스트 도구로 확인
mvn exec:java -Dexec.mainClass=com.guideon.tool.RegulationInferenceTestTool
```

**해결**:
1. 키워드 목록에 해당 용어가 있는지 확인
2. `matchMode`가 올바른지 확인 (contains/exact/regex)
3. 규칙이 `enabled: true` 인지 확인
4. `priority`가 `minPriority` 이상인지 확인

---

### 문제 3: 너무 많은 규정 유형이 반환됨

**해결**:
1. `maxRegulationTypes` 값을 낮춤
2. 규칙의 우선순위를 조정
3. 키워드를 더 구체적으로 변경

---

## 📝 마이그레이션 가이드

### 기존 하드코딩 코드 → 설정 기반

**1단계**: 기존 코드 분석
```java
// 기존 코드에서 키워드 추출
if (query.matches(".*(경조사|경조휴가|경조금).*")) {
    // 이 부분을 YAML로 옮김
}
```

**2단계**: YAML 규칙 작성
```yaml
- name: "경조사 관련"
  keywords:
    - "경조사"
    - "경조휴가"
    - "경조금"
  regulationTypes:
    - "복리후생비규정"
```

**3단계**: 코드 간소화
```java
// 기존 복잡한 로직 제거
// → RegulationInferenceConfigLoader 사용
List<String> types = RegulationInferenceConfigLoader.inferRegulationTypes(query);
```

---

## 🎯 요약

| 항목 | 변경 전 | 변경 후 |
|------|---------|---------|
| **유지보수성** | 낮음 (코드 수정) | 높음 (설정 수정) |
| **재컴파일** | 필요 | 불필요 |
| **가독성** | 정규식 (낮음) | YAML (높음) |
| **테스트** | 어려움 | 도구 제공 |
| **확장성** | 낮음 | 높음 |
| **우선순위** | 없음 | 지원 |
| **활성화 제어** | 불가 | 가능 |
| **비개발자 수정** | 불가 | 가능 |

---

## 📖 관련 문서

- [Fix-Gyeongjosa-Search-Issue.md](Fix-Gyeongjosa-Search-Issue.md) - 경조사 검색 문제 해결
- [Confidence-Score-Improvements.md](Confidence-Score-Improvements.md) - 신뢰도 점수 개선

---

**작성자**: Claude Code Assistant
**문서 버전**: 1.0.0
**최종 업데이트**: 2025-11-01
