import { create } from 'zustand';
import type { User } from '@/types';
import { authService } from '@/services/authService';

interface AuthState {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  error: string | null;

  // Actions
  login: (username: string, password: string, rememberMe?: boolean) => Promise<void>;
  logout: () => Promise<void>;
  loadUser: () => Promise<void>;
  clearError: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  user: null,
  isAuthenticated: authService.isAuthenticated(),
  isLoading: false,
  error: null,

  login: async (username, password, rememberMe = false) => {
    set({ isLoading: true, error: null });
    try {
      const { user } = await authService.login({ username, password, rememberMe });
      set({ user, isAuthenticated: true, isLoading: false });
    } catch (error) {
      set({
        error: error instanceof Error ? error.message : '로그인에 실패했습니다.',
        isLoading: false,
      });
      throw error;
    }
  },

  logout: async () => {
    set({ isLoading: true });
    try {
      await authService.logout();
      set({ user: null, isAuthenticated: false, isLoading: false });
    } catch (error) {
      set({ isLoading: false });
      throw error;
    }
  },

  loadUser: async () => {
    // 임시: 인증 API가 없으므로 기본 사용자로 설정
    // TODO: 백엔드 인증 API 구현 후 주석 해제
    /*
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
    */

    // 임시: 기본 사용자로 자동 로그인
    const dummyUser: User = {
      id: '1',
      username: 'admin',
      name: '관리자',
      email: 'admin@guideon.com',
      role: 'ADMIN',
    };
    set({ user: dummyUser, isAuthenticated: true, isLoading: false });
  },

  clearError: () => set({ error: null }),
}));
