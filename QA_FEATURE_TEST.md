# 질문하기 기능 테스트 가이드

## 🎯 기능 개요
질문하기 기능은 사용자가 자연어로 규정에 대해 질문하면 AI가 분석하여 답변을 제공하는 기능입니다.

## 📋 구현 상태
✅ **백엔드 API** - 완료
- 엔드포인트: `POST /api/qa/ask`
- 컨트롤러: `src/main/java/com/guideon/controller/QAController.java:52`
- 서비스: `src/main/java/com/guideon/service/QAService.java:35`

✅ **프론트엔드** - 완료
- 페이지: `guideon-frontend/src/pages/QAPage.tsx`
- 서비스: `guideon-frontend/src/services/regulationService.ts:13`
- 라우트: `/qa`

## 🚀 실행 방법

### 1. 백엔드 서버 시작
```bash
# 프로젝트 루트에서
./run-server.bat

# 또는
mvn spring-boot:run
```

서버가 http://localhost:8080 에서 실행됩니다.

### 2. 프론트엔드 개발 서버 시작
```bash
cd guideon-frontend
npm install  # 처음 한 번만
npm run dev
```

프론트엔드가 http://localhost:3000 에서 실행됩니다.

## 🧪 테스트 시나리오

### 시나리오 1: 기본 질문
1. 브라우저에서 http://localhost:3000/qa 접속
2. 질문 입력창에 다음 질문 입력:
   ```
   해외 출장시 숙박비는 얼마까지 지원되나요?
   ```
3. "질문하기" 버튼 클릭
4. 결과 확인:
   - ✅ 질문 분석 결과 (키워드, 규정 유형, 의도)
   - ✅ AI 답변
   - ✅ 신뢰도 점수
   - ✅ 근거 규정 목록

### 시나리오 2: 복잡한 질문
```
임원 퇴직금 지급 기준과 절차에 대해 알려주세요.
```

### 시나리오 3: 예외 상황
```
회계 문서는 몇 년간 보관해야 하나요?
```

## 📡 API 엔드포인트 상세

### POST /api/qa/ask

**요청 예시:**
```json
{
  "question": "해외 출장시 숙박비는 얼마까지 지원되나요?"
}
```

**응답 예시:**
```json
{
  "success": true,
  "data": {
    "answer": "해외 출장시 숙박비는...",
    "analysis": {
      "keywords": ["해외출장", "숙박비", "지원"],
      "regulationTypes": ["출장여비지급규정"],
      "questionIntent": "기준확인"
    },
    "references": [
      {
        "documentName": "출장여비지급규정",
        "content": "제5조 (숙박비) ...",
        "relevanceScore": 0.95
      }
    ],
    "confidenceScore": 0.87
  }
}
```

## 🔍 디버깅 팁

### 백엔드 로그 확인
```bash
# application.yml의 로깅 레벨 설정:
logging:
  level:
    com.guideon: DEBUG
```

### 프론트엔드 네트워크 확인
1. 브라우저 개발자 도구 (F12)
2. Network 탭에서 `/api/qa/ask` 요청 확인
3. Request/Response 페이로드 검사

### 일반적인 문제 해결

**문제 1: CORS 에러**
- 해결: `WebConfig.java:29`에서 CORS 설정 확인
- 프론트엔드 origin이 허용되었는지 확인

**문제 2: 404 Not Found**
- 해결: 백엔드 서버가 8080 포트에서 실행 중인지 확인
- Vite 프록시 설정 확인 (`vite.config.ts:16`)

**문제 3: 타입 에러**
- 해결: 프론트엔드 타입 정의가 백엔드 응답과 일치하는지 확인
- `src/types/index.ts:27` 참조

## 📝 추가 기능 (선택 사항)

### 답변 평가 기능
```typescript
// QAPage.tsx:58 구현 필요
const handleRating = async (rating: 'helpful' | 'not_helpful') => {
  // historyId가 필요함 - 백엔드에서 반환하도록 수정 필요
}
```

### 질문 이력 저장
백엔드에서 질문/답변을 데이터베이스에 저장하려면:
1. History 엔티티 생성
2. HistoryRepository 구현
3. QAService에서 저장 로직 추가

## ✅ 체크리스트

- [ ] 백엔드 서버 실행 확인 (http://localhost:8080)
- [ ] 프론트엔드 서버 실행 확인 (http://localhost:3000)
- [ ] /qa 페이지 접속 가능
- [ ] 질문 입력 및 제출 가능
- [ ] 답변 정상 표시
- [ ] 질문 분석 결과 표시
- [ ] 근거 규정 목록 표시
- [ ] 신뢰도 점수 표시
- [ ] 답변 복사 기능 동작
- [ ] 로딩 상태 표시
- [ ] 에러 처리 정상 동작

## 🎨 UI 개선 제안

현재 QAPage는 다음 기능들을 제공합니다:
- 깔끔한 카드 레이아웃
- 실시간 로딩 인디케이터
- 신뢰도 기반 경고 메시지
- 근거 규정 시각화
- 답변 복사 기능
- 답변 평가 버튼

추가 가능한 기능:
- [ ] 질문 히스토리 사이드바
- [ ] 추천 질문 템플릿
- [ ] 음성 입력
- [ ] 다크 모드
- [ ] 질문 북마크
