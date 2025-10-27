import { api } from './api';
import type { ApiResponse, User } from '@/types';

export interface LoginRequest {
  username: string;
  password: string;
  rememberMe?: boolean;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export const authService = {
  // 로그인
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<ApiResponse<LoginResponse>>(
      '/auth/login',
      credentials
    );
    if (!response.success || !response.data) {
      throw new Error(response.error || '로그인에 실패했습니다.');
    }

    // 토큰 저장
    localStorage.setItem('auth_token', response.data.token);

    return response.data;
  },

  // 로그아웃
  logout: async (): Promise<void> => {
    try {
      await api.post('/auth/logout');
    } finally {
      localStorage.removeItem('auth_token');
    }
  },

  // 현재 사용자 정보 조회
  getCurrentUser: async (): Promise<User> => {
    const response = await api.get<ApiResponse<User>>('/auth/me');
    if (!response.success || !response.data) {
      throw new Error(response.error || '사용자 정보 조회에 실패했습니다.');
    }
    return response.data;
  },

  // 토큰 확인
  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('auth_token');
  },
};
