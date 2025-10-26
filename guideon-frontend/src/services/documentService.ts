import { api } from './api';
import type {
  ApiResponse,
  DocumentUploadResponse,
  DocumentListResponse,
} from '@/types';

export const documentService = {
  // 문서 업로드
  uploadDocument: async (file: File, regulationType: string): Promise<DocumentUploadResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('regulationType', regulationType);

    const response = await api.post<ApiResponse<DocumentUploadResponse>>(
      '/documents/upload',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    if (!response.success || !response.data) {
      throw new Error(response.error || response.message || '문서 업로드 중 오류가 발생했습니다.');
    }
    return response.data;
  },

  // 문서 목록 조회
  getDocuments: async (): Promise<DocumentListResponse> => {
    const response = await api.get<ApiResponse<DocumentListResponse>>('/documents');
    if (!response.success || !response.data) {
      throw new Error(response.error || '문서 목록 조회 중 오류가 발생했습니다.');
    }
    return response.data;
  },

  // 문서 삭제
  deleteDocument: async (id: string): Promise<void> => {
    const response = await api.delete<ApiResponse<void>>(`/documents/${id}`);
    if (!response.success) {
      throw new Error(response.error || response.message || '문서 삭제 중 오류가 발생했습니다.');
    }
  },
};
