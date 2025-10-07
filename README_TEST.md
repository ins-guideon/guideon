# 📋 테스트 가이드

## 테스트 클래스 개요

본 프로젝트는 서비스별 기능을 검증하는 3개의 테스트 클래스를 제공합니다.

### 1. QueryAnalysisServiceTest
**위치**: `src/test/java/com/guideon/service/QueryAnalysisServiceTest.java`

**테스트 내용**:
- 서비스 초기화 테스트
- 연차휴가 관련 질문 분석
- 출장비 관련 질문 분석
- 경비 관련 질문 분석
- 빈 문자열 입력 처리
- 복잡한 질문 분석
- 여러 질문 연속 처리

**주요 검증 사항**:
- AI를 통한 키워드 자동 추출
- 규정 유형 자동 식별
- 질문 의도 파악
- 검색 쿼리 최적화

### 2. RegulationSearchServiceTest
**위치**: `src/test/java/com/guideon/service/RegulationSearchServiceTest.java`

**테스트 내용**:
- 서비스 초기화 및 문서 인덱싱
- 연차휴가 규정 검색
- 출장비 규정 검색
- 법인카드 한도 검색
- 없는 규정 검색 (Fallback 처리)
- 복합 질문 검색
- 연속 검색 성능 테스트

**주요 검증 사항**:
- RAG 기반 벡터 검색
- AI 답변 생성
- 근거 조항 제공
- 신뢰도 점수 계산
- Fallback 메시지 처리

### 3. RegulationQASystemTest
**위치**: `src/test/java/com/guideon/RegulationQASystemTest.java`

**테스트 내용**:
- 전체 시스템 초기화
- End-to-End 질의응답 테스트
- 다양한 규정 질문 처리
- 연속 질의응답 성능 테스트
- 시스템 상태 확인

**주요 검증 사항**:
- 질문 분석 → 검색 → 답변 생성 파이프라인
- 실제 사용자 시나리오 검증
- 전체 시스템 통합 동작

---

## 🚀 테스트 실행 방법

### 전제 조건

테스트 실행 전 API 키를 설정해야 합니다:

```bash
# 환경변수 설정
set GOOGLE_API_KEY=your_api_key_here

# 또는 application.properties에 직접 입력
# gemini.api.key=your_api_key_here
```

⚠️ **주의**: API 키가 없으면 일부 테스트가 건너뛰어집니다.

---

### 방법 1: 모든 테스트 실행

```bash
# Windows
test-all.bat

# 또는 Maven 직접 실행
mvn test
```

### 방법 2: 특정 서비스 테스트만 실행

```bash
# Windows
test-service.bat QueryAnalysisService
test-service.bat RegulationSearchService
test-service.bat RegulationQASystem

# 또는 Maven 직접 실행
mvn test -Dtest=QueryAnalysisServiceTest
mvn test -Dtest=RegulationSearchServiceTest
mvn test -Dtest=RegulationQASystemTest
```

### 방법 3: IDE에서 실행

**IntelliJ IDEA / Eclipse**:
1. 테스트 클래스 파일 열기
2. 클래스명 옆의 실행 버튼 클릭
3. 개별 테스트 메서드도 실행 가능

---

## 📊 테스트 출력 예시

### QueryAnalysisServiceTest 실행 결과
```
✓ QueryAnalysisService 초기화 성공

=== 질문 분석 테스트 ===
질문: 연차 휴가는 몇 일인가요?

결과:
- 키워드: [연차, 휴가, 일수]
- 규정 유형: [취업규칙]
- 질문 의도: 기준확인
- 검색 쿼리: 연차 휴가 일수
```

### RegulationSearchServiceTest 실행 결과
```
✓ RegulationSearchService 초기화 성공
✓ 샘플 문서 3개 인덱싱 완료

=== 연차휴가 검색 테스트 ===
질문: 연차 휴가는 몇 일인가요?

답변:
1년간 80% 이상 출근한 근로자에게 15일의 유급휴가가 부여됩니다...

근거 조항 수: 2
신뢰도: 87.50%
관련 규정 발견: true
```

### RegulationQASystemTest 실행 결과
```
✓ RegulationQASystem 초기화 성공
✓ 샘플 문서 업로드 완료

=== 연차휴가 질의응답 테스트 ===
질문: 연차 휴가는 몇 일인가요?

답변:
1년간 80% 이상 출근한 근로자에게는 15일의 유급휴가가 부여되며...

근거 조항 수: 2
신뢰도: 85.23%
```

---

## 🎯 테스트 커버리지

각 테스트 클래스는 다음 시나리오를 검증합니다:

### 기본 기능 테스트
- ✅ 서비스 초기화
- ✅ 설정 파일 로드
- ✅ API 연동

### 정상 동작 테스트
- ✅ 단순 질문 처리
- ✅ 복잡한 질문 처리
- ✅ 여러 질문 연속 처리

### 예외 처리 테스트
- ✅ 빈 문자열 입력
- ✅ 없는 규정 질문
- ✅ API 키 미설정

### 성능 테스트
- ✅ 응답 시간 측정
- ✅ 연속 처리 성능

---

## ⚙️ 테스트 설정 변경

### 테스트 타임아웃 조정

`pom.xml`에서 설정:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.2</version>
    <configuration>
        <testFailureIgnore>false</testFailureIgnore>
        <forkCount>1</forkCount>
        <reuseForks>true</reuseForks>
    </configuration>
</plugin>
```

### 특정 테스트 제외

```bash
mvn test -Dtest=!RegulationQASystemTest
```

---

## 🐛 트러블슈팅

### 1. API 키 오류
```
⚠ API 키가 설정되지 않았습니다. 일부 테스트를 건너뜁니다.
```
**해결**: `GOOGLE_API_KEY` 환경변수 또는 `application.properties` 설정

### 2. 컴파일 오류
```bash
mvn clean compile
```

### 3. 의존성 문제
```bash
mvn clean install -U
```

### 4. 캐시 문제
```bash
mvn clean
rm -rf target/
```

---

## 📝 테스트 작성 가이드

### 새로운 테스트 추가하기

```java
@Test
@DisplayName("새로운 기능 테스트")
void testNewFeature() {
    if (service == null) {
        System.out.println("⚠ 테스트 건너뜀: API 키 미설정");
        return;
    }

    // Given
    String input = "테스트 입력";

    // When
    Result result = service.process(input);

    // Then
    assertNotNull(result);
    assertEquals(expected, result.getValue());
}
```

### 테스트 베스트 프랙티스

1. **명확한 테스트명**: `@DisplayName` 사용
2. **Given-When-Then 패턴** 사용
3. **API 키 체크**: 키가 없을 때 graceful skip
4. **적절한 Assertion**: 예상 결과 검증
5. **독립적인 테스트**: 테스트 간 의존성 최소화

---

## 📚 참고 자료

- [JUnit 5 User Guide](https://junit.org/junit5/docs/current/user-guide/)
- [Maven Surefire Plugin](https://maven.apache.org/surefire/maven-surefire-plugin/)
- [AssertJ Documentation](https://assertj.github.io/doc/)

---

## 🤝 기여하기

새로운 테스트를 추가하거나 개선사항이 있다면 PR을 보내주세요!

1. 테스트 케이스 작성
2. 로컬에서 실행 확인
3. PR 생성
4. 코드 리뷰 대기
