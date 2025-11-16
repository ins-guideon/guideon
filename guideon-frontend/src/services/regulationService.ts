import { api } from './api';
import type {
  ApiResponse,
  RegulationSearchResult,
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
