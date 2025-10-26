import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type { AppSettings } from '@/types';

interface SettingsState extends AppSettings {
  // Actions
  updateSettings: (settings: Partial<AppSettings>) => void;
  resetSettings: () => void;
}

const defaultSettings: AppSettings = {
  model: 'gemini-2.5-flash',
  maxResults: 5,
  minConfidence: 0.7,
  enableNotifications: true,
};

export const useSettingsStore = create<SettingsState>()(
  persist(
    (set) => ({
      ...defaultSettings,

      updateSettings: (settings) =>
        set((state) => ({
          ...state,
          ...settings,
        })),

      resetSettings: () => set(defaultSettings),
    }),
    {
      name: 'guideon-settings',
    }
  )
);
