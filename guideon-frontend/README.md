# Guideon Frontend

규정 Q&A 시스템의 React 기반 프론트엔드 애플리케이션입니다.

## 기술 스택

- **React 18** - UI 라이브러리
- **TypeScript** - 타입 안정성
- **Vite** - 빌드 도구
- **React Router v6** - 라우팅
- **TanStack Query (React Query)** - 서버 상태 관리
- **Zustand** - 클라이언트 상태 관리
- **Ant Design** - UI 컴포넌트 라이브러리
- **Axios** - HTTP 클라이언트
- **Recharts** - 차트 라이브러리

## 시작하기

### 1. 의존성 설치

```bash
npm install
```

### 2. 환경 변수 설정

`.env.example` 파일을 복사하여 `.env` 파일을 생성합니다:

```bash
cp .env.example .env
```

### 3. 개발 서버 실행

```bash
npm run dev
```

브라우저에서 http://localhost:3000 으로 접속합니다.

### 4. 빌드

```bash
npm run build
```

빌드된 파일은 `dist` 디렉토리에 생성됩니다.

### 5. 프리뷰

```bash
npm run preview
```

## 주요 기능

### 1. 로그인 (`/login`)
- 사용자 인증
- 자동 로그인 옵션

### 2. 대시보드 (`/`)
- 전체 통계 확인
- 최근 질문 이력
- 인기 키워드

### 3. 질문하기 (`/qa`)
- 자연어 질문 입력
- 실시간 질문 분석 표시
- AI 답변 및 근거 규정 조회
- 답변 평가

### 4. 문서 업로드 (`/documents`)
- 문서 파일 업로드 (PDF, DOC, DOCX, TXT)
- 텍스트 추출 및 확인
- 확정 및 인덱싱

### 5. 문서 조회 (`/documents/view`)
- 업로드된 문서 목록 조회
- 문서 상세 정보 확인

### 6. 문서 관리 (`/documents/manage`)
- 문서 목록 조회 및 관리
- 문서 삭제
- 새 문서 업로드 페이지로 이동

### 7. 검색 이력 (`/history`)
- 과거 질문 조회
- 날짜/유형별 필터링
- 즐겨찾기 기능

### 8. 통계 (`/analytics`)
- 일별 질문 추이
- 규정별 활용도
- 인기 키워드 분석

### 9. 설정 (`/settings`)
- API 키 설정
- 모델 선택 (Flash/Pro)
- 검색 옵션 조정

## 프로젝트 구조

```
src/
├── components/          # 재사용 가능한 컴포넌트
│   ├── common/         # 공통 컴포넌트
│   ├── layout/         # 레이아웃 컴포넌트
│   ├── qa/             # Q&A 관련 컴포넌트
│   └── regulation/     # 규정 관련 컴포넌트
├── pages/              # 페이지 컴포넌트
│   ├── Login.tsx
│   ├── Dashboard.tsx
│   ├── QAPage.tsx
│   ├── DocumentUpload.tsx
│   ├── DocumentView.tsx
│   ├── DocumentManagement.tsx
│   ├── History.tsx
│   ├── Analytics.tsx
│   └── Settings.tsx
├── services/           # API 서비스
│   ├── api.ts
│   ├── authService.ts
│   ├── regulationService.ts
│   └── documentService.ts
├── stores/             # Zustand 스토어
│   ├── authStore.ts
│   └── settingsStore.ts
├── types/              # TypeScript 타입 정의
│   └── index.ts
├── App.tsx             # 루트 컴포넌트
└── main.tsx            # 엔트리 포인트
```

## API 연동

기본적으로 `/api` 경로로 프록시 설정되어 있습니다.
백엔드 서버는 `http://localhost:8080` 에서 실행되어야 합니다.

프록시 설정은 `vite.config.ts` 파일에서 변경할 수 있습니다:

```typescript
server: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    },
  },
}
```

## 백엔드 API 스펙

다음 엔드포인트가 구현되어야 합니다:

### 인증
- `POST /api/auth/login` - 로그인
- `POST /api/auth/logout` - 로그아웃
- `GET /api/auth/me` - 현재 사용자 정보

### Q&A
- `POST /api/qa/ask` - 질문하기
- `GET /api/qa/history` - 질문 이력 조회
- `POST /api/qa/history/:id/favorite` - 즐겨찾기 토글
- `POST /api/qa/history/:id/rate` - 답변 평가

### 문서 관리
- `GET /api/documents/view` - 문서 목록 조회
- `GET /api/documents/view/:id` - 문서 상세 조회
- `POST /api/documents/extract-text` - 텍스트 추출
- `POST /api/documents/:id/confirm` - 확정 및 인덱싱
- `DELETE /api/documents/:id` - 문서 삭제

### 통계
- `GET /api/statistics` - 통계 데이터 조회

## 라이선스

MIT License
