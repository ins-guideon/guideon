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

export interface RegisterRequest {
  username: string;
  password: string;
  name: string;
  email: string;
}

export const authService = {
  // 로그인
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<ApiResponse<LoginResponse>>(
      '/auth/login',
      credentials
    );
    
    if (!response.success) {
      throw new Error(response.message || response.error || '로그인에 실패했습니다.');
    }
    
    if (!response.data) {
      throw new Error('서버 응답에 데이터가 없습니다.');
    }

    // 토큰 저장: rememberMe=true -> localStorage, false -> sessionStorage
    const token = response.data.token;
    if (credentials.rememberMe) {
      localStorage.setItem('auth_token', token);
      sessionStorage.removeItem('auth_token');
    } else {
      sessionStorage.setItem('auth_token', token);
      localStorage.removeItem('auth_token');
    }

    return response.data;
  },

  // 로그아웃
  logout: async (): Promise<void> => {
    sessionStorage.removeItem('auth_token');
    localStorage.removeItem('auth_token');
  },

  // 회원가입
  register: async (payload: RegisterRequest): Promise<User> => {
    const response = await api.post<ApiResponse<User>>('/auth/register', payload);
    
    if (!response.success) {
      if (response.errors) {
        // 유효성 검사 오류가 있는 경우 상세 메시지 생성
        const fieldNames: Record<string, string> = {
          username: '아이디',
          password: '비밀번호',
          name: '이름',
          email: '이메일'
        };
        
        const errorMessages = Object.entries(response.errors)
          .map(([field, msg]) => `${fieldNames[field] || field}: ${msg}`)
          .join('\n');
        throw new Error(errorMessages || '입력값이 올바르지 않습니다.');
      }
      throw new Error(response.message || response.error || '회원가입에 실패했습니다.');
    }
    
    if (!response.data) {
      throw new Error('서버 응답에 데이터가 없습니다.');
    }
    
    return response.data;
  },

  // 현재 사용자 정보 조회
  getCurrentUser: async (): Promise<User> => {
    const response = await api.get<ApiResponse<User>>('/auth/me');
    
    if (!response.success) {
      throw new Error(response.message || response.error || '사용자 정보 조회에 실패했습니다.');
    }
    
    if (!response.data) {
      throw new Error('서버 응답에 데이터가 없습니다.');
    }
    
    return response.data;
  },

  // 토큰 확인
  isAuthenticated: (): boolean => {
    return !!sessionStorage.getItem('auth_token') || !!localStorage.getItem('auth_token');
  },
};
