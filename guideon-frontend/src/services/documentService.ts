import { api } from './api';
import type {
  ApiResponse,
  DocumentUploadResponse,
  DocumentListResponse,
  DocumentDetailResponse,
} from '@/types';

export const documentService = {
  // 텍스트 추출
  extractText: async (file: File, regulationType: string): Promise<{ text: string }> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('regulationType', regulationType);

    const response = await api.post<ApiResponse<{ text: string }>>(
      '/documents/extract-text',
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    if (!response.success || !response.data) {
      throw new Error(response.error || response.message || '텍스트 추출 중 오류가 발생했습니다.');
    }
    return response.data;
  },

  // 문서 업로드
  uploadDocument: async (file: File, regulationType: string, content: string): Promise<DocumentUploadResponse> => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('regulationType', regulationType);
    formData.append('content', content);

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

  // 문서 목록 조회 (벡터 인덱싱용)
  getDocuments: async (): Promise<DocumentListResponse> => {
    const response = await api.get<ApiResponse<DocumentListResponse>>('/documents');
    if (!response.success || !response.data) {
      throw new Error(response.error || '문서 목록 조회 중 오류가 발생했습니다.');
    }
    return response.data;
  },

  // 문서 조회 목록 (DB에서 조회)
  getDocumentsForView: async (): Promise<DocumentListResponse> => {
    const response = await api.get<ApiResponse<DocumentListResponse>>('/documents/view');
    if (!response.success || !response.data) {
      throw new Error(response.error || '문서 조회 목록 중 오류가 발생했습니다.');
    }
    return response.data;
  },

  // 문서 상세 조회
  getDocumentDetail: async (id: string): Promise<DocumentDetailResponse> => {
    const response = await api.get<ApiResponse<DocumentDetailResponse>>(`/documents/view/${id}`);
    if (!response.success || !response.data) {
      throw new Error(response.error || '문서 상세 조회 중 오류가 발생했습니다.');
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

  // 문서 업데이트
  updateDocument: async (
    documentId: string,
    file: File | null,
    text: string,
    regulationType: string
  ): Promise<DocumentUploadResponse> => {
    const formData = new FormData();
    // 파일이 있을 때만 FormData에 추가
    if (file) {
      formData.append('file', file);
    }
    formData.append('text', text);
    formData.append('regulationType', regulationType);

    const response = await api.post<ApiResponse<DocumentUploadResponse>>(
      `/documents/${documentId}/update`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
      }
    );
    if (!response.success || !response.data) {
      throw new Error(response.error || response.message || '문서 업데이트 중 오류가 발생했습니다.');
    }
    return response.data;
  },
};
