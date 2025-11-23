import { api } from './api';
import type { ApiResponse, AppSettings } from '@/types';

export const settingsService = {
    // 설정 조회
    getSettings: async (): Promise<AppSettings> => {
        const response = await api.get<ApiResponse<AppSettings>>('/settings');
        if (!response.success || !response.data) {
            throw new Error(response.error || '설정 조회 중 오류가 발생했습니다.');
        }
        return response.data;
    },

    // 설정 업데이트
    updateSettings: async (settings: Partial<AppSettings>): Promise<AppSettings> => {
        const response = await api.put<ApiResponse<AppSettings>>('/settings', settings);
        if (!response.success || !response.data) {
            throw new Error(response.error || '설정 업데이트 중 오류가 발생했습니다.');
        }
        return response.data;
    },
};

