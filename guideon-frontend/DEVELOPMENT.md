# 프론트엔드 개발 가이드

## 현재 상태 (2025-10-17)

### ✅ 완료된 기능
- React + TypeScript 기본 구조
- Ant Design UI 컴포넌트
- React Router 라우팅
- TanStack Query (React Query) 설정
- Zustand 상태 관리
- Axios API 클라이언트

### ⚠️ 임시 조치
다음 기능들은 백엔드 API가 구현되지 않아 **임시로 비활성화** 되어 있습니다:

#### 1. 인증 (Authentication)
**파일**: `src/stores/authStore.ts`

```typescript
// 현재: 더미 사용자로 자동 로그인
// TODO: 백엔드 인증 API 구현 후 주석 해제
```

**필요한 백엔드 API**:
- `POST /api/auth/login` - 로그인
- `POST /api/auth/logout` - 로그아웃
- `GET /api/auth/me` - 현재 사용자 정보

#### 2. 대시보드 통계
**파일**: `src/pages/Dashboard.tsx`

```typescript
// 현재: enabled: false (API 호출 안함)
// TODO: 백엔드 통계 API 구현 후 enabled: true로 변경
```

**필요한 백엔드 API**:
- `GET /api/statistics` - 통계 데이터
- `GET /api/qa/history` - 질문 이력

## 백엔드 API 연동 방법

### 1. 인증 API 추가 시

[authStore.ts](src/stores/authStore.ts)의 `loadUser` 함수에서 주석을 해제하고 더미 코드를 삭제:

```typescript
loadUser: async () => {
  if (!authService.isAuthenticated()) {
    set({ user: null, isAuthenticated: false });
    return;
  }

  set({ isLoading: true });
  try {
    const user = await authService.getCurrentUser();
    set({ user, isAuthenticated: true, isLoading: false });
  } catch (error) {
    set({ user: null, isAuthenticated: false, isLoading: false });
  }
},
```

### 2. 통계 API 추가 시

[Dashboard.tsx](src/pages/Dashboard.tsx)의 `useQuery`에서 `enabled: true`로 변경:

```typescript
const { data: stats } = useQuery({
  queryKey: ['statistics'],
  queryFn: () => regulationService.getStatistics(),
  enabled: true, // 여기 변경!
});
```

## 현재 작동하는 페이지

### ✅ Q&A 페이지 (`/qa`)
- 질문 입력 폼 ✅
- 백엔드 연동: `POST /api/qa/search` (구현됨)

**사용 예시**:
```bash
curl -X POST http://localhost:8080/api/qa/search \
  -H "Content-Type: application/json" \
  -d '{"question": "연차는 몇 일?"}'
```

### ✅ 규정 관리 (`/regulations`)
- 규정 유형 목록 조회 ✅
- 백엔드 연동: `GET /api/regulations/types` (구현됨)

## 필요한 백엔드 API 목록

### 우선순위 높음 (핵심 기능)
- ✅ `POST /api/qa/analyze` - 질문 분석 (구현됨)
- ✅ `POST /api/qa/search` - 규정 검색 (구현됨)
- ✅ `GET /api/regulations/types` - 규정 유형 목록 (구현됨)
- ❌ `POST /api/regulations/upload` - 규정 업로드 (파일 업로드 필요)

### 우선순위 중간 (대시보드)
- ❌ `GET /api/statistics` - 통계 데이터
- ❌ `GET /api/qa/history` - 질문 이력

### 우선순위 낮음 (부가 기능)
- ❌ `POST /api/auth/login` - 로그인
- ❌ `POST /api/auth/logout` - 로그아웃
- ❌ `GET /api/auth/me` - 사용자 정보
- ❌ `POST /api/qa/history/:id/favorite` - 즐겨찾기
- ❌ `POST /api/qa/history/:id/rate` - 답변 평가

## 개발 서버 실행

```bash
# 프론트엔드만
npm run dev

# 백엔드 + 프론트엔드
cd ..
start-all.bat
```

## 트러블슈팅

### Axios 에러 발생
**증상**: 첫 페이지 로딩 시 axios 관련 에러

**원인**:
1. 인증 API (`/api/auth/me`) 호출 실패
2. 통계 API (`/api/statistics`) 호출 실패

**해결**:
- [x] 인증: 더미 사용자로 자동 로그인 (임시)
- [x] 통계: API 호출 비활성화 (임시)

### CORS 에러
**해결**: 백엔드 `WebConfig.java`에서 CORS 설정 완료

### API 404 에러
**원인**: 백엔드에 해당 엔드포인트 미구현

**확인 방법**:
```bash
# 백엔드 API 목록 확인
curl http://localhost:8080/
```

## 다음 단계

1. **백엔드 API 구현**
   - [ ] 파일 업로드 API (`/api/regulations/upload`)
   - [ ] 통계 API (`/api/statistics`)
   - [ ] 이력 API (`/api/qa/history`)

2. **프론트엔드 임시 조치 해제**
   - [ ] 인증 실제 API 연동
   - [ ] 대시보드 실제 데이터 표시

3. **기능 개선**
   - [ ] 에러 처리 개선
   - [ ] 로딩 상태 표시
   - [ ] 반응형 디자인 개선

## 참고 자료

- **백엔드 API 문서**: [../README.md#rest-api-명세](../README.md)
- **Vite 설정**: [vite.config.ts](vite.config.ts)
- **API 클라이언트**: [src/services/api.ts](src/services/api.ts)
