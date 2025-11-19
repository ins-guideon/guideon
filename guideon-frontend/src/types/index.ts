// 사용자 타입
export interface User {
  id: string;
  username: string;
  name: string;
  email: string;
  role: 'ADMIN' | 'USER';
}

// 질문 분석 결과
export interface QueryAnalysisResult {
  keywords: string[];
  regulationTypes: string[];
  questionIntent: string;
  optimizedQuery: string;
}

// 규정 참조 정보
export interface RegulationReference {
  documentName: string;
  content: string;
  relevanceScore: number;
  chapter?: string;
  article?: string;
}

// 규정 검색 결과
export interface RegulationSearchResult {
  answer: string;
  confidenceScore: number;
  references: RegulationReference[];
  analysis: QueryAnalysisResult;
  timestamp?: string;
}

// 질문 이력
export interface QuestionHistory {
  id: string;
  question: string;
  result: RegulationSearchResult;
  createdAt: string;
  isFavorite: boolean;
}

// 규정 문서
export interface RegulationDocument {
  id: string;
  name: string;
  type: string;
  content: string;
  uploadedAt: string;
  indexedAt?: string;
  status: 'pending' | 'indexed' | 'error';
  fileSize: number;
}

// 규정 유형
export const REGULATION_TYPES = [
  '이사회규정',
  '접대비사용규정',
  '윤리규정',
  '출장여비지급규정',
  '주식매수선택권운영규정',
  '노사협의회규정',
  '취업규칙',
  '매출채권관리규정',
  '금융자산 운용규정',
  '문서관리규정',
  '재고관리규정',
  '계약검토규정',
  '사규관리규정',
  '임원퇴직금지급규정',
  '임원보수규정',
  '주주총회운영규정',
  '경비지급규정',
  '복리후생비규정',
  '보안관리규정',
  '위임전결규정',
  '우리사주운영규정',
  '내부정보관리규정',
  '회계관리규정',
  '특수관계자 거래규정',
  '조직 및 업무분장규정',
  '자금관리규정',
  '인장관리규정',
] as const;

export type RegulationType = (typeof REGULATION_TYPES)[number];

// 질문 의도 타입
export const QUESTION_INTENTS = [
  '정보조회',
  '절차안내',
  '기준확인',
  '자격요건',
  '예외사항',
] as const;

export type QuestionIntent = (typeof QUESTION_INTENTS)[number];

// 통계 데이터
export interface Statistics {
  totalQuestions: number;
  totalRegulations: number;
  averageConfidence: number;
  popularKeywords: Array<{ keyword: string; count: number }>;
  questionsByDate: Array<{ date: string; count: number }>;
  regulationUsage: Array<{ type: string; count: number }>;
  satisfactionRate: number;
}

// API 응답 타입
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  message?: string;
  errors?: Record<string, string>;
}

// 페이지네이션
export interface Pagination {
  page: number;
  pageSize: number;
  total: number;
}

export interface PaginatedResponse<T> {
  items: T[];
  pagination: Pagination;
}

// 애플리케이션 설정
export interface AppSettings {
  apiKey?: string;
  model: 'gemini-2.5-flash' | 'gemini-2.5-pro';
  maxResults: number;
  minConfidence: number;
  enableNotifications: boolean;
}

// 문서 업로드 관련 타입
export interface DocumentUploadRequest {
  file: File;
  regulationType: string;
}

export interface DocumentUploadResponse {
  id: string;
  fileName: string;
  regulationType: string;
  fileSize: number;
  uploadTimestamp: number;
  status: 'pending' | 'indexed' | 'error';
  message: string;
}

export interface DocumentInfo {
  id: string;
  fileName: string;
  regulationType: string;
  fileSize: number;
  uploadTimestamp: number;
  status: 'pending' | 'indexed' | 'error';
}

export interface DocumentListResponse {
  documents: DocumentInfo[];
  totalCount: number;
}

export interface DocumentDetailResponse {
  id: string;
  fileName: string;
  regulationType: string;
  uploadTime: number;
  content: string;
  uploaderName: string;
  fileSize: number;
  status: string;
}
