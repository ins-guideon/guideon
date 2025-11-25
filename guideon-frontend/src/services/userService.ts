import { api } from './api';
import type { ApiResponse, User } from '@/types';

export const userService = {
  getUsers: async (): Promise<User[]> => {
    const response = await api.get<ApiResponse<User[]>>('/admin/users');
    if (!response.success || !response.data) {
      throw new Error(response.error || '사용자 목록 조회 중 오류가 발생했습니다.');
    }
    return response.data;
  },

  updateUserRole: async (userId: string, role: User['role']): Promise<User> => {
    const response = await api.put<ApiResponse<User>>(`/admin/users/${userId}/role`, { role });
    if (!response.success || !response.data) {
      throw new Error(response.error || '사용자 역할 변경 중 오류가 발생했습니다.');
    }
    return response.data;
  },
};

