import { api } from './api';
import type {
  ApiResponse,
  RegulationSearchResult,
  RegulationDocument,
  PaginatedResponse,
  QuestionHistory,
  Statistics,
} from '@/types';

export const regulationService = {
  // 질문하기
  askQuestion: async (question: string): Promise<RegulationSearchResult> => {
    const response = await api.post<ApiResponse<RegulationSearchResult>>(
      '/qa/ask',
      { question }
    );
    if (!response.success || !response.data) {
      throw new Error(response.error || '질문 처리 중 오류가 발생했습니다.');
    }
    return response.data;
  },

  // 규정 문서 목록 조회
  getRegulations: async (params?: {
    page?: number;
    pageSize?: number;
    type?: string;
  }): Promise<PaginatedResponse<RegulationDocument>> => {
    const response = await api.get<ApiResponse<PaginatedResponse<RegulationDocument>>>(
      '/regulations',
      { params }
    );
    if (!response.success || !response.data) {
      throw new Error(response.error || '규정 목록 조회 중 오류가 발생했습니다.');
    }
    return response.data;
  },

  // 규정 문서 상세 조회
  getRegulation: async (id: string): Promise<RegulationDocument> => {
    const response = await api.get<ApiResponse<RegulationDocument>>(
      `/regulations/${id}`
    );
    if (!response.success || !response.data) {
      throw new Error(response.error || '규정 조회 중 오류가 발생했습니다.');
    }
    return response.data;
  },

  // 규정 문서 업로드
  uploadRegulation: async (file: File, type: string): Promise<RegulationDocument> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('type', type);

    const response = await api.post<ApiResponse<RegulationDocument>>(
      '/regulations/upload',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    if (!response.success || !response.data) {
      throw new Error(response.error || '규정 업로드 중 오류가 발생했습니다.');
    }
    return response.data;
  },

  // 규정 문서 삭제
  deleteRegulation: async (id: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/regulations/${id}`);
    if (!response.success) {
      throw new Error(response.error || '규정 삭제 중 오류가 발생했습니다.');
    }
  },

  // 질문 이력 조회
  getHistory: async (params?: {
    page?: number;
    pageSize?: number;
    startDate?: string;
    endDate?: string;
    regulationType?: string;
  }): Promise<PaginatedResponse<QuestionHistory>> => {
    const response = await api.get<ApiResponse<PaginatedResponse<QuestionHistory>>>(
      '/qa/history',
      { params }
    );
    if (!response.success || !response.data) {
      throw new Error(response.error || '이력 조회 중 오류가 발생했습니다.');
    }
    return response.data;
  },

  // 즐겨찾기 토글
  toggleFavorite: async (historyId: string): Promise<void> => {
    const response = await api.post<ApiResponse<void>>(
      `/qa/history/${historyId}/favorite`
    );
    if (!response.success) {
      throw new Error(response.error || '즐겨찾기 처리 중 오류가 발생했습니다.');
    }
  },

  // 통계 조회
  getStatistics: async (): Promise<Statistics> => {
    const response = await api.get<ApiResponse<Statistics>>('/statistics');
    if (!response.success || !response.data) {
      throw new Error(response.error || '통계 조회 중 오류가 발생했습니다.');
    }
    return response.data;
  },

  // 답변 평가
  rateAnswer: async (
    historyId: string,
    rating: 'helpful' | 'not_helpful'
  ): Promise<void> => {
    const response = await api.post<ApiResponse<void>>(
      `/qa/history/${historyId}/rate`,
      { rating }
    );
    if (!response.success) {
      throw new Error(response.error || '평가 처리 중 오류가 발생했습니다.');
    }
  },
};
