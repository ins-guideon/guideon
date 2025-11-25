import { api } from './api';
import type {
  ApiResponse,
  RegulationSearchResult,
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
};
